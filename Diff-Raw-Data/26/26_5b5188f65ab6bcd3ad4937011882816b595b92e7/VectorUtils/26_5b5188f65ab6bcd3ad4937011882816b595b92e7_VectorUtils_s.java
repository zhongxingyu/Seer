 package fr.aumgn.bukkitutils.geom;
 
 public class VectorUtils {
 
    public static void assertEquals(Vector expected, Vector result, double delta) {
         org.junit.Assert.assertEquals(
                 expected.getX(), result.getX(), delta);
         org.junit.Assert.assertEquals(
                 expected.getY(), result.getY(), delta);
         org.junit.Assert.assertEquals(
                 expected.getZ(), result.getZ(), delta);
     }
 
    public static void assertEquals(Vector2D expected, Vector2D result, double delta) {
         org.junit.Assert.assertEquals(
                 expected.getX(), result.getX(), delta);
         org.junit.Assert.assertEquals(
                 expected.getZ(), result.getZ(), delta);
     }
 }
