 /**
  * ModelReaderTest.java - test for the ModelReader class
  */
 package edu.bu.cs.cs480.main;
 
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertSame;
 
 import java.io.FileNotFoundException;
 import java.util.Arrays;
 
 import org.junit.Test;
 
 import edu.bu.cs.cs480.Material;
 import edu.bu.cs.cs480.TestUtils;
 
 /**
  * Test for the ModelReader class.
  * 
  * @author Jeffrey Finkelstein <jeffrey.finkelstein@gmail.com>
  * @since Spring 2011
  */
 public class ModelReaderTest {
 
   /** The directory containing the model files for testing. */
   public static final String TESTDIR = "src/test/resources/edu/bu/cs/cs480/";
   /** The file containing a test model. */
   public static final String TESTFILE = TESTDIR + "model1.dat";
 
   /**
    * Test method for
   * {@link edu.bu.cs.cs480.main.ModelReader#ModelReader(java.lang.String)}.
    */
   @Test
   public void testFromFile() {
     TracerEnvironment e = null;
     try {
       e = new ModelReader(TESTFILE).environment();
     } catch (final FileNotFoundException exception) {
       TestUtils.fail(exception);
     } catch (final FileFormatException exception) {
       TestUtils.fail(exception);
     }
     
     assertNotNull(e);
   }
 
   /**
    * Test method for
    * {@link edu.bu.cs.cs480.main.ModelReader#getObjectWithID(java.util.List, int)}
    * .
    */
   @Test
   public void testGetObjectWithID() {
     final Material m1 = new Material();
     final Material m2 = new Material();
     final Material m3 = new Material();
     m1.setId(1);
     m2.setId(2);
     m3.setId(3);
 
     Material m = ModelReader.getObjectWithID(Arrays.asList(m1, m2, m3), 2);
     assertSame(m, m2);
     m = ModelReader.getObjectWithID(Arrays.asList(m1, m2, m3), 1);
     assertSame(m, m1);
   }
 //
 //  /**
 //   * Test method for
 //   * {@link edu.bu.cs.cs480.main.ModelReader#readBox(java.util.Scanner, java.util.List)}
 //   * .
 //   */
 //  @Test
 //  public void testReadBox() {
 //    final Scanner s = new Scanner(
 //        "ID 6 mat ID 6 1 1 -1 1 0 0 0 1 0 0 0 1 8 8 0.1");
 //    final Material m = new Material();
 //    m.setId(6);
 //    final Box b = ModelReader.readBox(s, Arrays.asList(m));
 //    assertTrue(b.position().equals(new Vector3D(1, 1, -1)));
 //    assertTrue(b.orientation().u().equals(new Vector3D(1, 0, 0)));
 //    assertTrue(b.orientation().v().equals(new Vector3D(0, 1, 0)));
 //    assertTrue(b.orientation().w().equals(new Vector3D(0, 0, 1)));
 //    assertTrue(b.dimensions().equals(new Vector3D(8, 8, 0.1)));
 //  }
 //
 //  /**
 //   * Test method for
 //   * {@link edu.bu.cs.cs480.main.ModelReader#readCamera(java.util.Scanner)}.
 //   */
 //  @Test
 //  public void testReadCamera() {
 //    final Scanner s = new Scanner(
 //        "orthographic  -1 1 1  0 1 1   0 0 1   5 1 20");
 //
 //    Camera camera = null;
 //    try {
 //      camera = ModelReader.readCamera(s);
 //    } catch (final FileFormatException exception) {
 //      TestUtils.fail(exception);
 //    }
 //
 //    assertTrue(camera instanceof OrthographicCamera);
 //    assertTrue(camera.position().equals(new Vector3D(-1, 1, 1)));
 //    assertTrue(camera.direction().equals(new Vector3D(1, 0, 0)));
 //    assertTrue(camera.up().equals(new Vector3D(0, 0, 1)));
 //    assertEquals(1, camera.near(), 0);
 //    assertEquals(20, camera.far(), 0);
 //  }
 //
 //  /**
 //   * Test method for
 //   * {@link edu.bu.cs.cs480.main.ModelReader#readColor(java.util.Scanner)}.
 //   */
 //  @Test
 //  public void testReadColor() {
 //    Scanner s = new Scanner("0.1 0.2 0.3");
 //
 //    final FloatColor color = ModelReader.readColor(s);
 //
 //    assertEquals(0.1, color.red(), 1e-2);
 //    assertEquals(0.2, color.green(), 1e-8);
 //    assertEquals(0.3, color.blue(), 1e-2);
 //  }
 //
 //  /**
 //   * Test method for
 //   * {@link edu.bu.cs.cs480.main.ModelReader#readCSG(java.util.Scanner, java.util.List)}
 //   * .
 //   */
 //  @Test
 //  public void testReadCSG() {
 //    final Scanner s = new Scanner("ID 3 union ID 1 ID 2");
 //
 //    final SurfaceObject o1 = new Sphere();
 //    final SurfaceObject o2 = new Sphere();
 //    o1.setId(1);
 //    o1.setId(2);
 //    ConstructiveSolidGeometry csg = null;
 //    try {
 //      csg = ModelReader.readCSG(s, Arrays.asList(o1, o2));
 //    } catch (final FileFormatException exception) {
 //      TestUtils.fail(exception);
 //    }
 //
 //    assertTrue(csg instanceof Union);
 //  }
 //
 //  /**
 //   * Test method for
 //   * {@link edu.bu.cs.cs480.main.ModelReader#readCylinder(java.util.Scanner, java.util.List)}
 //   * .
 //   */
 //  @Test
 //  public void testReadCylinder() {
 //    final Scanner s = new Scanner("ID 2 mat ID 2    0 -2.5 1 1 0 0 0.3 2");
 //
 //    final Material m = new Material();
 //    m.setId(2);
 //
 //    final Cylinder c = ModelReader.readCylinder(s, Arrays.asList(m));
 //    assertEquals(2, c.id());
 //    assertSame(m, c.material());
 //    assertTrue(c.position().equals(new Vector3D(0, -2.5, 1)));
 //    assertTrue(c.direction().equals(new Vector3D(1, 0, 0)));
 //    assertEquals(0.3, c.radius(), 0);
 //    assertEquals(2, c.length(), 0);
 //  }
 //
 //  /**
 //   * Test method for
 //   * {@link edu.bu.cs.cs480.main.ModelReader#readEllipsoid(java.util.Scanner, java.util.List)}
 //   * .
 //   */
 //  @Test
 //  public void testReadEllipsoid() {
 //    final Scanner s = new Scanner("ID 2 mat ID 2    0 -2.5 1 1 2 3");
 //
 //    final Material m = new Material();
 //    m.setId(2);
 //
 //    final Ellipsoid e = ModelReader.readEllipsoid(s, Arrays.asList(m));
 //    assertEquals(2, e.id());
 //    assertSame(m, e.material());
 //    assertTrue(e.position().equals(new Vector3D(0, -2.5, 1)));
 //    assertTrue(e.radii().equals(new Vector3D(1, 2, 3)));
 //
 //  }
 //
 //  /**
 //   * Test method for
 //   * {@link edu.bu.cs.cs480.main.ModelReader#readIntegerList(java.util.Scanner)}
 //   * .
 //   */
 //  @Test
 //  public void testReadIntegerList() {
 //    final Scanner s = new Scanner("1 2 3 4 5 6");
 //    org.junit.Assert.assertArrayEquals(new Integer[] { 1, 2, 3, 4, 5, 6 },
 //        ModelReader.readIntegerList(s).toArray(new Integer[6]));
 //  }
 //
 //  /**
 //   * Test method for
 //   * {@link edu.bu.cs.cs480.main.ModelReader#readLight(java.util.Scanner)}.
 //   */
 //  @Test
 //  public void testReadLight() {
 //    final Scanner s = new Scanner(
 //        "ID 0 pnt   0 -10 10  0 1 -0.7  1 1 1   1 0 0   10  shadow_on");
 //
 //    Light l = null;
 //    try {
 //      l = ModelReader.readLight(s);
 //    } catch (final FileFormatException exception) {
 //      TestUtils.fail(exception);
 //    }
 //
 //    assertEquals(0, l.id());
 //    assertTrue(l instanceof PointLight);
 //    assertTrue(l.position().equals(new Vector3D(0, -10, 10)));
 //    assertTrue(l.direction().equals(new Vector3D(0, 1, -0.7).normalized()));
 //    assertTrue(l.color().equals(new FloatColor(1, 1, 1)));
 //    assertTrue(l.attenuationCoefficients().equals(new Vector3D(1, 0, 0)));
 //    assertEquals(10, l.attenuationExponent());
 //    assertTrue(l.shadow());
 //  }
 //
 //  /**
 //   * Test method for
 //   * {@link edu.bu.cs.cs480.main.ModelReader#readMaterial(java.util.Scanner)}.
 //   */
 //  @Test
 //  public void testReadMaterial() {
 //    final Scanner s = new Scanner("ID 0   1 1 1   0.1 0.8 0.1 5   0 0 0 ");
 //
 //    final Material m = ModelReader.readMaterial(s);
 //
 //    assertEquals(0, m.id());
 //    assertTrue(m.color().equals(new FloatColor(1, 1, 1)));
 //    assertEquals(0.1, m.ambientReflection(), 0);
 //    assertEquals(0.8, m.diffuseReflection(), 0);
 //    assertEquals(0.1, m.specularReflection(), 0);
 //    assertEquals(5, m.specularExponent(), 0);
 //    assertEquals(0, m.transmission(), 0);
 //    assertEquals(0, m.reflection(), 0);
 //    assertEquals(0, m.refraction(), 0);
 //  }
 //
 //  /**
 //   * Test method for
 //   * {@link edu.bu.cs.cs480.main.ModelReader#readOrientation(java.util.Scanner)}
 //   * .
 //   */
 //  @Test
 //  public void testReadOrientation() {
 //    final Scanner s = new Scanner("1 2 3 4 5 6 7 8 9");
 //    final Orientation o = ModelReader.readOrientation(s);
 //    assertTrue(o.u().equals(new Vector3D(1, 2, 3).normalized()));
 //    assertTrue(o.v().equals(new Vector3D(4, 5, 6).normalized()));
 //    assertTrue(o.w().equals(new Vector3D(7, 8, 9).normalized()));
 //  }
 //
 //  /**
 //   * Test method for
 //   * {@link edu.bu.cs.cs480.main.ModelReader#readResolution(java.util.Scanner)}
 //   * .
 //   */
 //  @Test
 //  public void testReadResolution() {
 //    final Scanner s = new Scanner("0.0185 0.0186");
 //    final Resolution r = ModelReader.readResolution(s);
 //    assertEquals(0.0185, r.xResolution(), 0);
 //    assertEquals(0.0186, r.yResolution(), 0);
 //  }
 //
 //  /**
 //   * Test method for
 //   * {@link edu.bu.cs.cs480.main.ModelReader#readSphere(java.util.Scanner, java.util.List)}
 //   * .
 //   */
 //  @Test
 //  public void testReadSphere() {
 //    final Scanner s = new Scanner("ID 0 mat ID 0    2 2 0.7   0.85");
 //    final Material m = new Material();
 //    m.setId(1);
 //    final Sphere sphere = ModelReader.readSphere(s, Arrays.asList(m));
 //    assertEquals(0, sphere.id());
 //    assertTrue(sphere.position().equals(new Vector3D(2, 2, 0.7)));
 //    assertEquals(0.85, sphere.radius(), 0);
 //  }
 //
 //  /**
 //   * Test method for
 //   * {@link edu.bu.cs.cs480.main.ModelReader#readSurfaceObject(java.util.Scanner, java.util.List, java.util.List)}
 //   * .
 //   */
 //  @Test
 //  public void testReadSurfaceObject() {
 //    final Scanner s = new Scanner("sphere ID 0 mat ID 0     2 2 0.7   0.85");
 //    final Material m = new Material();
 //    m.setId(0);
 //    try {
 //      assertTrue(ModelReader.readSurfaceObject(s, Arrays.asList(m), null) instanceof Sphere);
 //    } catch (final FileFormatException exception) {
 //      TestUtils.fail(exception);
 //    }
 //  }
 //
 //  /**
 //   * Test method for
 //   * {@link edu.bu.cs.cs480.main.ModelReader#readTriple(java.util.Scanner)}.
 //   */
 //  @Test
 //  public void testReadTriple() {
 //    final Scanner s = new Scanner("1 2 3");
 //    assertTrue(ModelReader.readTriple(s).equals(new Vector3D(1, 2, 3)));
 //  }
 //
 //  /**
 //   * Test method for
 //   * {@link edu.bu.cs.cs480.main.ModelReader#readViewport(java.util.Scanner)}.
 //   */
 //  @Test
 //  public void testReadViewport() {
 //    final Scanner s = new Scanner("400 300");
 //    final Viewport v = ModelReader.readViewport(s);
 //    assertEquals(400, v.width());
 //    assertEquals(300, v.height());
 //  }
 
 }
