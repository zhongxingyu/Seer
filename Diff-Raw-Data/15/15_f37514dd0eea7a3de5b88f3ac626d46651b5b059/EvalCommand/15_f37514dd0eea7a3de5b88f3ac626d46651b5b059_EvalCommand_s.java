 package com.talios;
 
 import org.concordion.api.Element;
 import org.concordion.api.Evaluator;
 import org.concordion.api.ResultRecorder;
 import org.concordion.internal.CommandCall;
 import org.concordion.internal.InvalidExpressionException;
 import org.concordion.internal.command.AbstractCommand;
 import org.concordion.internal.util.Check;
 
 import java.lang.reflect.Field;
 
 public class EvalCommand extends AbstractCommand {
 
     @Override
     public void execute(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
         Check.isFalse(commandCall.hasChildCommands(), "Nesting commands inside an 'eval' is not supported");
 
         Element element = commandCall.getElement();
 
         String evalString = extractEvalString(element);
 
         Check.notNull(evalString, "The eval attribute must be set for an element containing eval");
 
         try {
             final String methodName = convertToCamel(evalString) + "()";
             Object result;
 
             try {
                 result = evaluator.evaluate(methodName);
             } catch (InvalidExpressionException e) {
                 throw new RuntimeException(e.getMessage());
             }
 
 
             // Setup title
             Element evalTitle = extractTitle(evalString);
 
             Element resultContainer = new Element("div");
             resultContainer.addAttribute("style", "clear: both");
 
             // Setup example response
             Element commentaryElement = extractCommentary(element);
 
             element.appendChild(evalTitle);
 
             if (commentaryElement != null) {
                 resultContainer.appendChild(commentaryElement);
             }
 
             if (result != null) {
 
                 Element resultElement = extractResult(result);
                 resultContainer.appendChild(resultElement);
             }
             element.appendChild(resultContainer);
 
         } catch (Exception e) {
             Element evalTitle = extractTitle(evalString);

             element.addStyleClass("failure");
             throw new RuntimeException(e);
         }
     }
 
     private Element extractTitle(String evalString) {
         Element evalTitle = new Element("b");
         evalTitle.appendText(evalString);
         evalTitle.addStyleClass("success");
         return evalTitle;
     }
 
     private String extractEvalString(Element element) {
         String evalString;
         try {
             Field field = Element.class.getDeclaredField("xomElement");
             field.setAccessible(true);
             nu.xom.Element xomElement = (nu.xom.Element) field.get(element);
             evalString = xomElement.getAttribute("eval", AppTest.NAMESPACE_TALIOS).getValue();
         } catch (NoSuchFieldException e) {
             throw new RuntimeException(e);
         } catch (IllegalAccessException e) {
             throw new RuntimeException(e);
         }
         return evalString;
     }
 
     private Element extractResult(Object result) {
         Element exampleContent = new Element("p");
         exampleContent.appendText(result.toString());
         Element exampleContainer = new Element("div");
         exampleContainer.appendChild(exampleContent);
         exampleContainer.addStyleClass("example");
         return exampleContainer;
     }
 
     private Element extractCommentary(Element element) {
         Element evalComment = null;
         if (element.hasChildren()) {
             evalComment = new Element("div");
             evalComment.addStyleClass("commentary");
             element.moveChildrenTo(evalComment);
         }
         return evalComment;
     }
 
 
     public String convertToCamel(String s) {
         StringBuilder sb = new StringBuilder();
         String[] parts = s.split(" ");
         for (String part : parts) {
             if (sb.length() == 0) {
                 sb.append(part.toLowerCase());
             } else {
                 sb.append(part.substring(0, 1).toUpperCase()).append(part.substring(1).toLowerCase());
             }
         }
         return sb.toString().replaceAll("(\\.|,|:|;)", "");
     }
 
 }
