 package org.polyforms.repository.util;
 
 import java.util.Collection;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 /**
  * Test case for field utility class.
  * 
  * @author kuisong
  * @since 1.0
  */
 public class GenericsUtilsTest {
     @Test(expected = UnsupportedOperationException.class)
     public void cannotInstance() {
         new GenericsUtils();
     }
 
     @Test
     public void resolveTypeArguments() {
         final Class<?>[] typeArguments = GenericsUtils.resolveTypeArguments(TestActualGenericBean.class);
         Assert.assertEquals(2, typeArguments.length);
         Assert.assertEquals(String.class, typeArguments[0]);
         Assert.assertEquals(Integer.class, typeArguments[1]);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void resolveTypeArgumentsFromNull() {
         GenericsUtils.resolveTypeArguments(null);
     }
 
     @Test
     public void resolveTypeArgumentsFromInterface() {
         Assert.assertEquals(String.class, GenericsUtils.resolveTypeArguments(TestActualGenericInterface.class)[0]);
     }
 
     @Test
     public void resolveTypeArgumentsFromClass() {
         Assert.assertEquals(String.class, GenericsUtils.resolveTypeArguments(TestActualGenericClass.class)[0]);
     }
 
     @Test
     public void resolveTypeArgumentsFromSuperClass() {
         Assert.assertEquals(String.class, GenericsUtils.resolveTypeArguments(TestSubGenericBean.class)[0]);
     }
 
     @Test
     public void resolveTypeArgumentsFromNonParameterized() {
         Assert.assertNull(GenericsUtils.resolveTypeArguments(TestBean.class));
     }
 
     @Test
     public void resolveTypeArgumentsFromBase() {
         Assert.assertNull(GenericsUtils.resolveTypeArguments(TestGenericBean.class));
     }
 
    private static abstract class TestBean {
     }
 
     private static interface TestGenericInterface<T, T2> {
     }
 
     private static interface TestActualGenericInterface extends TestGenericInterface<String, Integer> {
     }
 
     private static abstract class TestActualGenericClass implements TestActualGenericInterface, Collection<Object> {
     }
 
     private static class TestGenericBean<T, T2> {
     }
 
     private static abstract class TestActualGenericBean extends TestGenericBean<String, Integer> {
     }
 
     private static abstract class TestSubGenericBean extends TestActualGenericBean {
     }
 }
