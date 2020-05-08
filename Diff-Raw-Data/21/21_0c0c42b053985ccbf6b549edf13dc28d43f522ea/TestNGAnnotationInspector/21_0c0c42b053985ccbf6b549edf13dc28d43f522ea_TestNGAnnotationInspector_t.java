 package com.maxheapsize.quant.testng;
 
 import com.maxheapsize.quant.ClassTesterException;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.AfterGroups;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.AfterSuite;
 import org.testng.annotations.AfterTest;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeGroups;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.BeforeSuite;
 import org.testng.annotations.BeforeTest;
 import org.testng.annotations.Test;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class TestNGAnnotationInspector {
 
  private Map<Class, String> testNGAnnotations = new HashMap<Class, String>();
 
   public TestNGAnnotationInspector() {
     super();
     storeTestAnnotation(BeforeSuite.class);
     storeTestAnnotation(AfterSuite.class);
     storeTestAnnotation(BeforeTest.class);
     storeTestAnnotation(AfterTest.class);
     storeTestAnnotation(BeforeGroups.class);
     storeTestAnnotation(AfterGroups.class);
     storeTestAnnotation(BeforeClass.class);
     storeTestAnnotation(AfterClass.class);
     storeTestAnnotation(BeforeMethod.class);
     storeTestAnnotation(AfterMethod.class);
     storeTestAnnotation(Test.class);
   }
 
   final boolean isTestAnnotation(Annotation annotation) {
     Class annotationType = annotation.annotationType();
    return testNGAnnotations.containsKey(annotationType);
   }
 
   private void storeTestAnnotation(Class testAnnotation) {
    testNGAnnotations.put(testAnnotation, testAnnotation.getName());
   }
 
   public Map<Class, String> getTestAnnotations() {
    return testNGAnnotations;
   }
 
   public boolean hasTestAnnotation(Method method) {
     Annotation[] annotations = method.getAnnotations();
     for (Annotation annotation : annotations) {
       if (isTestAnnotation(annotation)) {
         return true;
       }
     }
     return false;
   }
 
   public List<Method> getNonTestAnnotatedPublicVoidMethod(Iterable<Method> methodIterable) {
     List<Method> result = new ArrayList<Method>();
     for (Method method : methodIterable) {
       boolean addMethod = true;
       if (hasTestAnnotation(method)) {
         addMethod = false;
       }
       if (addMethod) {
         result.add(method);
       }
     }
     return result;
   }
 
   public String[] getTestGroupsFromAnnotation(Annotation annotation) {
 
     String[] testAnnotationGroups = {};
     for (Class testNGAnnotationClass : getTestAnnotations().keySet()) {
       if (annotation.annotationType().equals(testNGAnnotationClass)) {
         testAnnotationGroups = getTestGroups(annotation, testNGAnnotationClass);
       }
     }
     return testAnnotationGroups;
   }
 
   private String[] getTestGroups(Annotation annotation, Class annotationTypeKlass) {
 
    Method groupsMethod;
     String[] testAnnotationGroups;
     try {
       groupsMethod = annotationTypeKlass.getMethod("groups", null);
 
       testAnnotationGroups = (String[]) groupsMethod.invoke(annotation);
     }
     catch (NoSuchMethodException e) {
       throw new ClassTesterException("Could not get groups of annotation " + annotation.annotationType().getName());
     }
     catch (IllegalAccessException e) {
       throw new ClassTesterException("Could not get groups of annotation " + annotation.annotationType().getName());
     }
     catch (InvocationTargetException e) {
       throw new ClassTesterException("Could not get groups of annotation " + annotation.annotationType().getName());
     }
     return testAnnotationGroups;
   }
 }
