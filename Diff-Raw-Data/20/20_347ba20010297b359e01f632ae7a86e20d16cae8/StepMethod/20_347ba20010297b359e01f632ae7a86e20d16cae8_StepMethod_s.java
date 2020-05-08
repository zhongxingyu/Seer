 package org.pepper.core.models;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import org.junit.runners.model.FrameworkMethod;
 
// TODO: need a more descriptive name, or else extract into separate utility class used by GivenFrameworkMethod, WhenFrameworkMethod, ThenFrameworkMethod
 public abstract class StepMethod extends FrameworkMethod {
 
   public StepMethod(Method method) {
     super(method);
   }
 
   // ex. The StepMethod for @Given("a variable x with value 3") would return "Given a variable x with value 3"
   public abstract String getStep();
   /*
    Note: This will simplify lines similar to the following in PepperRunner
 
     if (frameworkMethod.getAnnotation(annotationClass) instanceof Given) {
       step = (frameworkMethod.getAnnotation(Given.class)).value();
     }
   */
 
   public static Object parseArgument(String arg) {
     try {
       return Integer.valueOf(arg);
     }
     catch (NumberFormatException numberFormat) {
       // TODO: log exception
     }
 
     try {
       return Double.valueOf(arg);
     }
     catch (NumberFormatException numberFormat) {
       // TODO: log exception;
     }
 
     return Boolean.valueOf(arg);
   }
 
   List<Object> arguments = new ArrayList<Object>();
 
   public List<Object> getArguments() {
     // Note: This method should be called after a call to matches(String line) method
     // TODO: figure out a better way to implement this functionality
     return arguments;
   }
 
   // ex. "Given I have -3" should match with "Given I have $value"
   public boolean matches(String line) {
     StringTokenizer stepTokenizer = new StringTokenizer(getStep());
     StringTokenizer lineTokenizer = new StringTokenizer(line);
     String stepToken, lineToken;
 
     Class<?>[] parameterTypes = getMethod().getParameterTypes();
     arguments.clear();
 
     // iterate over each word in method and line
     while (stepTokenizer.hasMoreTokens() && lineTokenizer.hasMoreTokens()) {
       stepToken = stepTokenizer.nextToken();
       lineToken = lineTokenizer.nextToken();
 
       // if both words don't match
       if (!stepToken.equals(lineToken)) {
         // if word in method isn't a placeholder, continue with next method
         if (!stepToken.startsWith("$")) {
           return false;
         }
 
         Object obj = parseArgument(lineToken);
         arguments.add(obj);
         String str = "";
 
         //System.out.println("obj -> " + obj.getClass().getName());
         if ("java.lang.Integer".equals(obj.getClass().getName())) {
           str = "int";
         }
         else if ("java.lang.Double".equals(obj.getClass().getName())) {
           str = "double";
         }
         else if ("java.lang.Boolean".equals(obj.getClass().getName())) {
           str = "boolean";
         }
 
         // if we've read more arguments than the method takes, continue with next method
         if (arguments.size() > parameterTypes.length) {
           // TODO: demonstrate purpose of this test
           return false;
         }
 
         // if the given argument type does not match up with the expected argument, continue with next method
         if (!parameterTypes[arguments.size() - 1].getCanonicalName().equals(str)) {
           return false;
         }
       }
     }
 
     // StepMethod matches the given line
     if (!stepTokenizer.hasMoreTokens() && !lineTokenizer.hasMoreTokens()) {
       return true;
     }
 
     // StepMethod does not correspond to the given line.
     return false;
   }
 }
