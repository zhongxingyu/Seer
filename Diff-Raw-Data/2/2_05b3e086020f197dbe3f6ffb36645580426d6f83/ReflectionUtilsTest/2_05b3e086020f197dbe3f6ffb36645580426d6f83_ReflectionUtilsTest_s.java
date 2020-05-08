 package com.seitenbau.testing.util;
 
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.reflect.Method;
 import java.util.List;
 import static com.seitenbau.testing.asserts.fest.Assertions.*;
 
 import org.junit.Test;
 
 @Retention(RetentionPolicy.RUNTIME)
 @interface MyAnnotation {}
 class Param {}
 class Param2 extends Param {}
 class Param3 extends Param {}
 class BaseClass {
   
 }
 class Klasse1 extends BaseClass {
   void method_k1_NoAnntation() {}
   @MyAnnotation void method_k1_Anntation() {}
   @MyAnnotation void method_with_Anntation(Param p) {}
 }
 class Klasse2 extends Klasse1 {
   void method_k2_NoAnntation() {}
   @MyAnnotation void method_k2_Anntation() {}
   @MyAnnotation void method_with_Anntation(Param p) {}
 }
 class Klasse3 extends Klasse1 {
   void method_k2_NoAnntation() {}
   @MyAnnotation void method_k2_Anntation() {}
   @MyAnnotation void method_with_Anntation(Param p) {}
   @MyAnnotation void method_with_Anntation(Param3 p) {}
 }
 
 public class ReflectionUtilsTest
 {
   
   @Test
   public void notUsedAnnoation() 
   {
     List<Method> result = ReflectionUtils.findMethodByAnnotation(Klasse1.class, Retention.class, true);
     // verify
     assertThat(result).isEmpty();
   }
 
   @Test
   public void usedAnnotationButBaseClass()
   {
     List<Method> result = ReflectionUtils.findMethodByAnnotation(BaseClass.class, MyAnnotation.class, true);
     // verify
     assertThat(result).isEmpty();
   }
 
   @Test
   public void usedAnnotationButKlasse1()
   {
     List<Method> result = ReflectionUtils.findMethodByAnnotation(Klasse1.class, MyAnnotation.class, true);
     // verify
     assertThat(result).hasSize(2);
    assertThat(result).onProperty("name").containsSequence("method_k1_Anntation", "method_with_Anntation");
   }
 
   @Test
   public void usedAnnotationButKlasse2()
   {
     List<Method> result = ReflectionUtils.findMethodByAnnotation(Klasse2.class, MyAnnotation.class, true);
     // verify
     assertThat(result).hasSize(3);
     assertThat(result).onProperty("name")
         .containsSequence("method_with_Anntation", "method_k2_Anntation", "method_k1_Anntation");
   }
 
   @Test
   public void findMethodsWithDifferentParamTypeInChildThanInParent()
   {
     List<Method> result = ReflectionUtils.findMethodByAnnotation(Klasse3.class, MyAnnotation.class, true);
     // verify
     assertThat(result).hasSize(4);
     assertThat(result).onProperty("name")
         .containsSequence("method_with_Anntation", "method_with_Anntation", "method_k2_Anntation",
             "method_k1_Anntation");
   }
   
   @Test
   public void subclass_none() {
     assertThat(ReflectionUtils.canCast(Object.class, Object.class)).isTrue();
     assertThat(ReflectionUtils.canCast(RuntimeException.class, Object.class)).isTrue();
     assertThat(ReflectionUtils.canCast(Object.class,RuntimeException.class)).isFalse();
   }
 }
