 package org.xtest;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Queue;
 import java.util.Stack;
 import java.util.concurrent.Callable;
 
 import org.eclipse.xtext.nodemodel.ICompositeNode;
 import org.eclipse.xtext.nodemodel.INode;
 import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
 import org.eclipse.xtext.util.Strings;
 import org.eclipse.xtext.xbase.XBlockExpression;
 import org.eclipse.xtext.xbase.XExpression;
 import org.xtest.xTest.impl.BodyImplCustom;
 
 import com.google.common.base.CharMatcher;
 import com.google.common.base.Function;
 import com.google.common.base.Joiner;
 import com.google.common.base.Predicate;
 import com.google.common.base.Splitter;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 
 /**
  * Utilities for working with stack traces
  * 
  * @author Michael Barry
  */
 @SuppressWarnings("restriction")
 public class XtestUtil {
     /**
      * Prefix of packages that should not show up in sanitized stack traces
      */
     private static final ImmutableSet<String> BLACKLIST = ImmutableSet.of("org.eclipse.xtext",
             "org.xtest", "sun.reflect", "java.lang.reflect");
     /**
      * Prefix of packages that should always show up in sanitized stack traces even if they have a
      * prefix in {@link #BLACKLIST}
      */
     private static final ImmutableSet<String> WHITELIST = ImmutableSet.of("org.eclipse.xtext.lib",
             "org.xtest.lib");
 
     /**
      * Generates a condensed stack trace that omits all internal Xtest methods and the base of the
      * stack that originally invoked executing the Xtest file. Xtest line-number traces are mixed
      * into the stack whenever a closure is invoked
      * 
      * @param startTrace
      *            Base stack trace up to invoking the Xtest interpreter, will be filtered out
      * @param stackTrace
      *            The raw stack trace
      * @param callStack
      *            The stack of Xtest expressions
      * @return The compressed Xtest stack traces
      */
     public static StackTraceElement[] generateXtestStackTrace(StackTraceElement[] startTrace,
             StackTraceElement[] stackTrace, Stack<XExpression> callStack) {
         StackTraceElement[] normalize = removeCommonBase(startTrace, stackTrace);
         StackTraceElement[] withXtestElements = insertXtestElements(normalize, callStack);
         StackTraceElement[] finalTrace = sanitize(withXtestElements);
         return finalTrace;
     }
 
     /**
      * Returns the root cause of the throwable provided
      * 
      * @param exception
      *            The exception
      * @return The root cause of that exception
      */
     public static Throwable getRootCause(Throwable exception) {
         while (exception.getCause() != null) {
             exception = exception.getCause();
         }
         return exception;
     }
 
     /**
      * Get the text of the first line of an expression
      * 
      * @param expression
      *            The expression
      * @return The test of the first line of the expression
      */
     public static String getText(XExpression expression) {
         String name;
         while (expression instanceof XBlockExpression
                 && !((XBlockExpression) expression).getExpressions().isEmpty()) {
             expression = ((XBlockExpression) expression).getExpressions().get(0);
         }
 
         ICompositeNode node = NodeModelUtils.getNode(expression);
         if (node != null) {
             name = getNodeWithoutComments(node).replaceAll("^(\\{|\\s)*", "")
                     .replaceAll("[\n\r].*$", "").trim().replaceAll("\\}$", "");
         } else {
             name = "";
         }
         return name;
     }
 
     /**
      * Run a block of code marking that this is on a new level of the Xtest stack so that
      * {@link #generateXtestStackTrace(StackTraceElement[], StackTraceElement[], Stack)} will insert
      * the Xtest stack trace element here
      * 
      * @param callable
      *            The code to run
      * @return The result of running that code
      * @throws Exception
      *             If an error occurs calling the callable
      */
     public static <T> T runOnNewLevelOfXtestStack(Callable<T> callable) throws Exception {
         return XtestStackMarker.run(callable);
     }
 
     /**
      * Converts any string to java upper-camel-case format. Non java letter-or-digits are dropped
      * and used to tokenize. Leading non-java letters are dropped entirely.
      * 
      * @param input
      *            The input
      * @return The upper-camel-case output
      */
     public static String toUpperCamel(String input) {
         String result = "";
         if (input != null) {
             // 1. Trim non-java letters from start of the input
             input = CharMatcher.JAVA_LETTER.negate().trimLeadingFrom(input);
             // 2. Split on non-java or digit characters
             Iterable<String> split = Splitter.on(CharMatcher.JAVA_LETTER_OR_DIGIT.negate()).split(
                     input);
             // 3. Convert the first character of each token to upper case
             split = Iterables.transform(split, new Function<String, String>() {
                 @Override
                 public String apply(String input) {
                    return Strings.toFirstUpper(input.toLowerCase());
                 }
             });
             // 4. Pack them back together
             result = Joiner.on("").join(split);
         }
         return result;
     }
 
     private static StackTraceElement generateXtestStackTraceElement(XExpression expression) {
         ICompositeNode node = NodeModelUtils.getNode(expression);
         String fileName = getBody(expression).getTypeName();
         int startLine = node.getStartLine();
         return new StackTraceElement(fileName, "\"" + getText(expression) + "\"", fileName,
                 startLine);
     }
 
     private static BodyImplCustom getBody(XExpression expression) {
         Iterable<BodyImplCustom> filter = Iterables.filter(expression.eResource().getContents(),
                 BodyImplCustom.class);
         return Iterables.getFirst(filter, null);
     }
 
     private static String getNodeWithoutComments(ICompositeNode node) {
         INode rootNode = node.getRootNode();
         if (rootNode != null) {
             int offset = node.getOffset();
             int length = node.getLength();
             return rootNode.getText().substring(offset, offset + length);
         }
         return null;
     }
 
     private static StackTraceElement[] insertXtestElements(StackTraceElement[] stackTrace,
             Stack<XExpression> callStack) {
         List<StackTraceElement> list = Lists.newArrayList(stackTrace);
         // Stack iterator starts at base, want to start at top and work down
         List<XExpression> reverse = Lists.reverse(callStack);
         Queue<XExpression> queue = Lists.newLinkedList(reverse);
 
         // Search for Xtest stack markers
         for (int i = 0; i < list.size(); i++) {
             StackTraceElement element = list.get(i);
             if (queue.isEmpty()) {
                 break;
             }
             if (element.getClassName().equals(XtestStackMarker.class.getName())) {
                 StackTraceElement newElement = generateXtestStackTraceElement(queue.poll());
                 list.add(i++, newElement);
             }
         }
 
         // add the rest...
         for (XExpression expression : queue) {
             list.add(generateXtestStackTraceElement(expression));
         }
         return list.toArray(new StackTraceElement[list.size()]);
     }
 
     private static Predicate<String> isStartOf(final String string) {
         return new Predicate<String>() {
             @Override
             public boolean apply(String input) {
                 return string.startsWith(input);
             }
         };
     }
 
     private static StackTraceElement[] removeCommonBase(StackTraceElement[] startTrace,
             StackTraceElement[] stackTrace) {
         ArrayList<StackTraceElement> result = Lists.newArrayList(stackTrace);
         List<StackTraceElement> start = Lists.reverse(Lists.newArrayList(startTrace));
         List<StackTraceElement> stack = Lists.reverse(result);
 
         // While beginnings are the same, remove them
         while (!stack.isEmpty() && !start.isEmpty() && stack.get(0).equals(start.get(0))) {
             stack.remove(0);
             start.remove(0);
         }
 
         return result.toArray(new StackTraceElement[result.size()]);
     }
 
     private static StackTraceElement[] sanitize(StackTraceElement[] stackTrace) {
         List<StackTraceElement> traceList = Lists.newArrayList(stackTrace);
         List<StackTraceElement> toRemove = Lists.newArrayList();
 
         for (StackTraceElement element : traceList) {
             String className = element.getClassName();
 
             Predicate<String> pred = isStartOf(className);
             if (!Iterables.any(WHITELIST, pred) && Iterables.any(BLACKLIST, pred)) {
                 toRemove.add(element);
             }
         }
 
         traceList.removeAll(toRemove);
         return traceList.toArray(new StackTraceElement[traceList.size()]);
     }
 
     private static class XtestStackMarker {
         public static <T> T run(Callable<T> callable) throws Exception {
             return callable.call();
         }
     }
 
 }
