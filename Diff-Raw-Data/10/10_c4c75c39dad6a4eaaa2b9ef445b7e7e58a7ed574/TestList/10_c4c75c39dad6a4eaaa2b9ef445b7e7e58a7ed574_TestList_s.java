 package junitview.model;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashSet;
 
 public class TestList {
 	
	HashSet<TestResult> mTestResults;
 	ArrayList<Method> mTestMethods = new ArrayList<Method>();
 	
 	public TestList(Class<?> JUnitTestClass){
 		Method[] methods = JUnitTestClass.getMethods();
 		
 		// find all the methods with @Test Annotation
 		for (Method method: methods){
 			if (isTestMethod(method)) mTestMethods.add(method);
 		}
 		
 		// convert these to TestResults
 		// TODO add error checking. For now, we're assuming they all have Names and descriptions
 		for (Method method: mTestMethods){
 			mTestResults.add(new TestResult(method));
 		}
 	}
 	
 	private boolean isTestMethod(Method m){
 		Annotation[] annotations = m.getAnnotations();
 		for (Annotation annotation: annotations){
 			if (annotation.annotationType().getSimpleName().equals("Test")){
 				return true;
 			}
 		}
 		return false;
 	}
 }
