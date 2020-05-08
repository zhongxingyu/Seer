 /*
  * Class PlanarPolygon
  * TCSS 491 Computational Worlds
  * Steve Bradshaw
  */
 
 package tesseract.objects;
 
 import javax.media.j3d.Appearance;
 import javax.media.j3d.Geometry;
 import javax.media.j3d.Group;
 import javax.media.j3d.ImageComponent2D;
 import javax.media.j3d.Node;
 import javax.media.j3d.PolygonAttributes;
 import javax.media.j3d.Shape3D;
 import javax.media.j3d.Texture;
 import javax.media.j3d.Texture2D;
 import javax.media.j3d.Transform3D;
 import javax.media.j3d.TriangleFanArray;
 import javax.vecmath.Matrix3f;
 import javax.vecmath.Point3f;
 import javax.vecmath.TexCoord2f;
 import javax.vecmath.Vector3f;
 
 import com.sun.j3d.utils.geometry.Sphere;
 import com.sun.j3d.utils.image.TextureLoader;
 
 /**
  * This class creates an ellipsoid using the formula 
  * (x/a)^2  +  (y/b)^2  +  (z/c)^2 = 1 using a matrix3f transformation
  * on a basic Sphere.  This class sets 'a' to a constant 1.0 and allows
  * 'b' and 'c'  to alter the ellipsoid's shape along with the radius field 
  * Sphere.  Since this is a sphere, the normals are already calculated.
  * 
  * @author Steve Bradshaw
  * @version 8 Feb 2011
  */
 public class PlanarPolygon extends PhysicalObject {
 	
 	/**
 	 * Default mass.
 	 */
	private static final float DEFAULT_MASS = Float.POSITIVE_INFINITY;
 	
 	/**
 	 * Number of divisions in the sphere.
 	 */
 	private static final int DEFAULT_DIVISIONS = 6;
 	
 	/**
 	 * Create a new Ellipsoid.
 	 * 
 	 * @param position Initial position.
 	 * @param mass Initial mass.
 	 * @param radius the radius of the base sphere.
 	 * @param primflags an int for the base spere primflags.
 	 * @param divisions an in for the shape divisions.
 	 * @param appearance an Appearance object.
 	 * @param b a float for the b portion of the ellipsoid formula.
 	 * @param c a float for the c portion of the ellipsoid formula.
 	 */
 	public PlanarPolygon(final Vector3f position, final float mass,
 			final float radius, final int divisions) {
 		super(position, mass);
 		
 		setShape(createShape(radius, divisions));
 	}
 	
 	/**
 	 * Create a new Ellipsoid.
 	 * 
 	 * @param position Initial position.
 	 * @param radius a float for the size of the base sphere.
 	 */
 	public PlanarPolygon(final Vector3f position, final float radius) {
		this(position, DEFAULT_MASS, radius, DEFAULT_DIVISIONS);
 	}
 	
 	/**
 	 * This creates a default Ellipsoid for the 2 argument constructor.
 	 * @param radius the siz of the ellipsoid
 	 */
 	/*private void createDefaultPlanarPolygon(final float radius) {
 		
 		Sphere sphere = new Sphere(radius, new Sphere().getPrimitiveFlags(),
 				DEFAULT_DIVISIONS);
 		Transform3D tmp = new Transform3D();
 		tmp.set(new Matrix3f(1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.5f));
 		getTransformGroup().setTransform(tmp);
 		getTransformGroup().addChild(sphere);
 	}*/
 	
 	/**
 	 * This method creates a planar polygon shape.
 	 * 
 	 * @param radius a float for the size of the base polygon
 	 * @param divisions an int for the number of divisons
 	 * @param appearance an Appearance object
 	 */
	private Shape3D createShape(final float radius, final int divisions) {
 		TriangleFanArray geometry = new TriangleFanArray(divisions,
 				TriangleFanArray.COORDINATES | TriangleFanArray.TEXTURE_COORDINATE_2, new int[] {divisions});
 		for (int i = 0; i < divisions; i++) {
 			float baseX = (float)Math.cos(2 * Math.PI * i / divisions);
 			float baseZ = -(float)Math.sin(2 * Math.PI * i / divisions);
 			geometry.setCoordinate(i, new Point3f(radius * baseX, 0, radius * baseZ));
 			geometry.setTextureCoordinate(0, i, new TexCoord2f((baseX + 1) / 2, (-baseZ + 1) / 2));
 		}
 		TextureLoader tl = new TextureLoader("lava.jpg", null);
 		ImageComponent2D image = tl.getImage();
 		int width = image.getWidth();
 		int height = image.getHeight();
 		Texture2D texture = new Texture2D(Texture.MULTI_LEVEL_MIPMAP, Texture.RGB, width, height);
 		
 		int imageLevel = 0;
 		texture.setImage(imageLevel, image);
 		while (width > 1 || height > 1) {
 			imageLevel++;
 			if (width > 1) width /= 2;
 			if (height > 1) height /= 2;
 			texture.setImage(imageLevel, tl.getScaledImage(width, height));
 		}
 		texture.setMagFilter(Texture2D.NICEST);
 		texture.setMinFilter(Texture2D.NICEST);
 
 		Appearance appearance = new Appearance();
 		appearance.setTexture(texture);
 		PolygonAttributes polyAttr = new PolygonAttributes(PolygonAttributes.POLYGON_FILL, PolygonAttributes.CULL_NONE, 0);
 		appearance.setPolygonAttributes(polyAttr);		
 		geometry.setCapability(Geometry.ALLOW_INTERSECT);
 		Shape3D polygon =  new Shape3D(geometry, appearance);
		
 		return polygon;
 	}
 	
 	/*private void createShape(final float radius, final int primflags,
 			final Appearance appearance, final int divisions, final float b,
 			final float c) {
 		
 		Sphere sphere = new Sphere(radius, primflags, divisions, appearance);
 		Transform3D tmp = new Transform3D();
 		tmp.set(new Matrix3f(1.0f, 0.0f, 0.0f, 0.0f, b, 0.0f, 0.0f, 0.0f, c));
 		getTransformGroup().setTransform(tmp);
 		getTransformGroup().addChild(sphere);
 	}*/
 }
