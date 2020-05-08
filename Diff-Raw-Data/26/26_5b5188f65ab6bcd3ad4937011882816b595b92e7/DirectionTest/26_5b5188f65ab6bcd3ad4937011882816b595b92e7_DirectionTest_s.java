 package fr.aumgn.bukkitutils.geom;
 
 import org.junit.Ignore;
 import org.junit.Test;
import static org.junit.Assert.assertEquals;
 
import static fr.aumgn.bukkitutils.geom.VectorUtils.*;
 import fr.aumgn.bukkitutils.geom.direction.HorizontalDirection;
 import fr.aumgn.bukkitutils.geom.direction.LocationDirection;
 
 public class DirectionTest {
 
     @Test
     public void testVector2DToYaw() {
         testVector2DToYaw(  0.0f,  0,  1);
         testVector2DToYaw( 45.0f, -1,  1);
         testVector2DToYaw( 90.0f, -1,  0);
         testVector2DToYaw(135.0f, -1, -1);
         testVector2DToYaw(180.0f,  0, -1);
         testVector2DToYaw(225.0f,  1, -1);
         testVector2DToYaw(270.0f,  1,  0);
         testVector2DToYaw(315.0f,  1,  1);
     }
 
     private void testVector2DToYaw(float yaw, int x, int z) {
         assertEquals(yaw, new Vector2D(x, z).toDirection().getYaw(), 0);
     }
 
     @Test
     public void testVectorToPitch() {
         testVectorToPitch(   0.0f,  1,  0);
 
         testVectorToPitch( 180.0f,  0,  1);
         testVectorToPitch(-180.0f,  0, -1);
 
         testVectorToPitch(  90.0f,  1,  1);
         testVectorToPitch( -90.0f,  1, -1);
     }
 
     private void testVectorToPitch(float pitch, int h, int y) {
         if (h == 0) {
             assertEquals(pitch, new Vector(0, y, 0).toDirection().getPitch(), 0);
         } else {
             assertEquals(pitch, new Vector(h, y, 0).toDirection().getPitch(), 0);
             assertEquals(pitch, new Vector(-h, y, 0).toDirection().getPitch(), 0);
             assertEquals(pitch, new Vector2D(h, h).normalize().to3D(y).toDirection().getPitch(), 0);
             assertEquals(pitch, new Vector2D(-h, h).normalize().to3D(y).toDirection().getPitch(), 0);
         }
     }
 
     @Test
     public void testYawToVector2D() {
         testYawToVector2D( 0,  1,   0.0f);
         testYawToVector2D(-1,  1,  45.0f);
         testYawToVector2D(-1,  0,  90.0f);
         testYawToVector2D(-1, -1, 135.0f);
         testYawToVector2D( 0, -1, 180.0f);
         testYawToVector2D( 1, -1, 225.0f);
         testYawToVector2D( 1,  0, 270.0f);
         testYawToVector2D( 1,  1, 315.0f);
     }
 
     private void testYawToVector2D(int x, int y, float yaw) {
         Vector2D expected = new Vector2D(x, y).normalize();
         Vector2D result = new HorizontalDirection(yaw).getVector2D();
        assertEquals(expected, result, 0.00001);
     }
 
     @Ignore("Not implemented yet.")
     @Test
     public void testPitchToVector() {
         testPitchToVector( 1,  0,    0.0f);
 
         testPitchToVector( 0,  1,  180.0f);
         testPitchToVector( 0, -1, -180.0f);
 
         testPitchToVector( 1,  1,   90.0f);
         testPitchToVector( 1, -1,  -90.0f);
     }
 
     private void testPitchToVector(int h, int y, float pitch) {
         Vector expected = new Vector(h, y, 0);
         LocationDirection dir = new LocationDirection(
                 expected.to2D().toDirection().getYaw(), pitch);
         assertEquals(y, dir.getVectorY(), 0.001);
     }
 }
