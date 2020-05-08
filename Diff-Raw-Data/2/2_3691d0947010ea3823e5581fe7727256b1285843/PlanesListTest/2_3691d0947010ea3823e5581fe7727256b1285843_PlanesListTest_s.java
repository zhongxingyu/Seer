 package lab4;
 
 import lab3.Plane;
 import lab3.TransportPlane;
 import org.junit.Test;
 
 import java.util.Arrays;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 public class PlanesListTest {
 
     @Test
     public void testAdd() {
         PlanesList planes = new PlanesList();
         Plane plane = new TransportPlane("omg", 1, 2, 3, 4, 5);
         planes.add(plane);
         assertEquals(planes.size(), 1);
         assertTrue(planes.get(0) == plane);
     }
 
     @Test
     public void testClear() {
         Plane plane = new TransportPlane("omg", 1, 2, 3, 4, 5);
         PlanesList planes = new PlanesList(Arrays.asList(plane, plane, plane, plane));
         assertFalse(planes.isEmpty());
         planes.clear();
         assertTrue(planes.isEmpty());
     }
 
     @Test
     public void testContains() {
         Plane plane = new TransportPlane("omg", 1, 2, 3, 4, 5);
         PlanesList planes = new PlanesList();
         assertFalse(planes.contains(plane));
         planes.add(plane);
         assertTrue(planes.contains(plane));
     }
 
     @Test(expected = IndexOutOfBoundsException.class)
     public void testDefaultConstruct() {
         PlanesList planes = new PlanesList();
         assertTrue(planes.isEmpty());
         assertEquals(planes.size(), 0);
         planes.get(0);
     }
 
     @Test
     public void testSingleElementConstruct() {
         Plane plane = new TransportPlane("omg", 1, 2, 3, 4, 5);
         PlanesList planes = new PlanesList(plane);
         assertEquals(planes.size(), 1);
         assertFalse(planes.isEmpty());
        planes.remove();
         assertTrue(planes.isEmpty());
     }
 
     @Test
     public void testViaCollectionConstruct() {
         Plane plane = new TransportPlane("omg", 1, 2, 3, 4, 5);
         PlanesList planes = new PlanesList(Arrays.asList(plane, plane, plane, plane));
         assertEquals(planes.size(), 4);
         assertFalse(planes.isEmpty());
     }
 
     @Test
     public void testGet() {
         Plane plane = new TransportPlane("omg", 1, 2, 3, 4, 5);
         Plane plane2 = new TransportPlane("omg", 1, 2, 3, 4, 3);
         PlanesList planes = new PlanesList(Arrays.asList(plane2, plane, plane2, plane2));
         assertEquals(planes.get(1), plane);
     }
 
     @Test
     public void testIndexOf() {
         Plane plane = new TransportPlane("omg", 1, 2, 3, 4, 5);
         Plane plane2 = new TransportPlane("omg", 1, 2, 3, 4, 3);
         PlanesList planes = new PlanesList(Arrays.asList(plane2, plane, plane, plane2));
         assertEquals(planes.indexOf(plane), 1);
     }
 
     @Test
     public void testLastIndexOf() {
         Plane plane = new TransportPlane("omg", 1, 2, 3, 4, 5);
         Plane plane2 = new TransportPlane("omg", 1, 2, 3, 4, 3);
         PlanesList planes = new PlanesList(Arrays.asList(plane2, plane, plane, plane2));
         assertEquals(planes.lastIndexOf(plane), 2);
     }
 
     @Test
     public void testRemove() {
         Plane plane = new TransportPlane("omg", 1, 2, 3, 4, 5);
         Plane plane2 = new TransportPlane("omg", 1, 2, 3, 4, 3);
         PlanesList planes = new PlanesList(Arrays.asList(plane2, plane, plane, plane2));
         planes.remove(plane);
         assertEquals(planes.size(), 3);
     }
 
     @Test
     public void testSet() {
         Plane plane = new TransportPlane("omg", 1, 2, 3, 4, 5);
         Plane plane2 = new TransportPlane("omg", 1, 2, 3, 4, 3);
         PlanesList planes = new PlanesList(Arrays.asList(plane2, plane, plane, plane2));
         planes.set(2, plane2);
         assertEquals(planes.get(2), plane2);
         assertEquals(planes.size(), 4);
     }
 
     @Test
     public void testSize() {
         Plane plane = new TransportPlane("omg", 1, 2, 3, 4, 5);
         Plane plane2 = new TransportPlane("omg", 1, 2, 3, 4, 3);
         PlanesList planes = new PlanesList(Arrays.asList(plane2, plane, plane, plane2));
         planes.add(plane);
         assertEquals(planes.size(), 5);
         planes.remove(plane);
         assertEquals(planes.size(), 4);
     }
 
     @Test
     public void testToArray() {
         Plane plane1 = new TransportPlane("omg", 1, 2, 3, 4, 5);
         Plane plane2 = new TransportPlane("omg", 1, 2, 3, 4, 3);
         PlanesList planes = new PlanesList(Arrays.asList(plane2, plane1, plane1, plane2));
         int i = 0;
         for (Plane plane : planes.toArray(new Plane[planes.size()])) {
             assertEquals(plane, planes.get(i));
             ++i;
         }
     }
 }
