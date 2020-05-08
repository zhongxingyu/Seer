 package org.pepper.core.models;
 
 import static org.hamcrest.core.IsEqual.equalTo;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 
 import java.lang.reflect.Method;
 
 import org.junit.Test;
 import org.pepper.core.annotations.Given;
 
 public class TestStepMethod {
 
   @Test
   public void testMatches() {
     String line = "Given I have -3";
     Class<?> klass = MyClass.class;
     GivenStepMethod givenMethod = null;
 
     for (Method method : klass.getMethods()) {
       if (method.getAnnotation(Given.class) != null) {
         givenMethod = new GivenStepMethod(method);
         if (givenMethod.matches(line)) {
           break;
         }
       }
     }
 
     assertNotNull(givenMethod);
     assertThat(givenMethod.getStep(), equalTo("Given I have $value"));
 
     // TODO: research why a super class can be down casted to subclass?
     // ex. GivenFrameworkMethod givenMethod = (GivenFrameworkMethod) frameworkMethod;
   }
 
   @Test
   public void testParseArgument() {
     Object obj;
 
     // ensure integer arguments get parsed as an integer
     obj = StepMethod.parseArgument("123");
     assertTrue("java.lang.Integer".equals(obj.getClass().getName()));
 
     // ensure that numbers with a decimal get parsed as a double
     obj = StepMethod.parseArgument("1.23");
     assertTrue("java.lang.Double".equals(obj.getClass().getName()));
 
     // ensure that "true" get parsed as a boolean
     obj = StepMethod.parseArgument("true");
     assertTrue("java.lang.Boolean".equals(obj.getClass().getName()));
 
     // ensure that "false" get parsed as a boolean
     obj = StepMethod.parseArgument("false");
     assertTrue("java.lang.Boolean".equals(obj.getClass().getName()));
 
     // ensure that parsing a boolean is case insensitive
     obj = StepMethod.parseArgument("tRuE");
     assertTrue("java.lang.Boolean".equals(obj.getClass().getName()));
 
     // ensure that parsing a boolean is case insensitive
     obj = StepMethod.parseArgument("fAlSe");
     assertTrue("java.lang.Boolean".equals(obj.getClass().getName()));
   }
 
 }
