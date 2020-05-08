 package Case;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.vecmath.*;
 
 import java.awt.*;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.image.BufferedImage;
 
 import javax.imageio.ImageIO;
 import javax.media.Buffer;
 import javax.media.CaptureDeviceInfo;
 import javax.media.CaptureDeviceManager;
 import javax.media.Manager;
 import javax.media.MediaLocator;
 import javax.media.Player;
 import javax.media.control.FormatControl;
 import javax.media.control.FrameGrabbingControl;
 import javax.media.format.VideoFormat;
 import javax.media.j3d.*;
 import javax.media.util.BufferToImage;
 
 import com.sun.j3d.utils.picking.PickCanvas;
 import com.sun.j3d.utils.picking.PickTool;
 import com.sun.j3d.utils.universe.*;
 import com.sun.j3d.utils.geometry.*;
 import com.sun.j3d.utils.image.TextureLoader;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Enumeration;
 import java.util.Vector;
 
 import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageDecoder;
 
 
 public class Case extends JFrame implements KeyListener {
 	public static void main(String[] s) {
 		
 		
 		// Getting save directory
 		String saveDir;
 		if(s.length > 0)
 		{
 			saveDir = s[0];
 		}
 		else
 		{
 			saveDir =
 				JOptionPane.showInputDialog(null, "Please enter directory where " +
 						"the images is/will be saved\n\n" +
 						"Also possible to specifiy as argument 1 when " +
 						"running this program.",
 						 ""); 
 		}
 		
 		new Case(saveDir);
 	}
 	
 	public Case(String saveDir)
 	{
 		// JFrame
 		setLayout(new BorderLayout());
 		
 		// Settings
 		this.saveDirectory = saveDir;
 		System.out.println("Using " + this.saveDirectory + " as directory.");
 		
 		// Images
 		getImages();
 		
 		// Webcam
 		images_used = new ArrayList<Integer>();
 		images_lastadded = new ArrayList<Integer>();
 		images_nevershown = new ArrayList<Integer>();
 		
 		
 		Vector devices = (Vector) CaptureDeviceManager.getDeviceList(null).clone();
 		Enumeration enumeration = devices.elements();
 		System.out.println("- Available cameras -");
 		ArrayList<String> names = new ArrayList<String>();
 		while (enumeration.hasMoreElements())
 		{
 			CaptureDeviceInfo cdi = (CaptureDeviceInfo) enumeration.nextElement();
 			String name = cdi.getName();
 			if (name.startsWith("vfw:"))
 			{
 				names.add(name);
 				System.out.println(name);
 			}
 		}
 		
 		//String str1 = "vfw:Logitech USB Video Camera:0";
 		//String str2 = "vfw:Microsoft WDM Image Capture (Win32):0";
 		if(names.size() == 0) {
 			JOptionPane.showMessageDialog(null, "Ingen kamera funnet. " +
 					"Du bør koble til et kamera for å kjøre programmet optimalt.",
 					"Feil",
 					 JOptionPane.ERROR_MESSAGE); 
 			cameraFound = false;
 		}
 		else
 		{
 			cameraFound = true;
 			if (names.size() > 1)
 			{
 	
 				JOptionPane.showMessageDialog(null, 
 						"Fant mer enn 1 kamera. " +
 						"Velger da:\n" +
 						names.get(0),
 						"Advarsel",
 						 JOptionPane.WARNING_MESSAGE);
 			}
 		}
 		
 		if(cameraFound)
 		{
 			String str2 = names.get(0);
 			di = CaptureDeviceManager.getDevice(str2);
 			ml = di.getLocator();
 			
 			try {
 				player = Manager.createRealizedPlayer(ml);
 				formatControl = (FormatControl)player.getControl(
 	            "javax.media.control.FormatControl");
 				
 				/*
 				Format[] formats = formatControl.getSupportedFormats();
 				for (int i=0; i<formats.length; i++)
 					System.out.println(formats[i].toString());
 				*/
 				
 				player.start();
 			}
 			catch(javax.media.NoPlayerException e) 
 			{
 				 JOptionPane.showMessageDialog(null, "Klarer ikke å starte"+
 						 " kamera. Sjekk at det er koblet til.", 
 						 "IOException", 
 						 JOptionPane.ERROR_MESSAGE); 
 				 System.exit(0);
 			}
 			catch (Exception e)
 			{
 				e.printStackTrace();
 				System.exit(0);
 			}
 			
 			if ((comp = player.getVisualComponent()) != null) {
 				add(comp, BorderLayout.EAST);
 			}
 		}
 		
 		
 		// Create canvas
 		GraphicsConfiguration gc = SimpleUniverse.getPreferredConfiguration();
 		Canvas3D cv = new Canvas3D(gc);
 		cv.addKeyListener(this);
 		add(cv, BorderLayout.CENTER);
 		BranchGroup bg = createSceneGraph();
 		bg.compile();
 		pc = new PickCanvas(cv, bg);
 	    pc.setMode(PickTool.GEOMETRY);
 		SimpleUniverse su = new SimpleUniverse(cv);
 		su.getViewingPlatform().setNominalViewingTransform();
 		
 		//Skjermbevegelse
 		/*OrbitBehavior orbit = new OrbitBehavior(cv);
 	    orbit.setSchedulingBounds(new BoundingSphere());
 	    su.getViewingPlatform().setViewPlatformBehavior(orbit);
 		*/
 	    su.addBranchGraph(bg);
 	    
 	    // JFrame stuff
 		setTitle("Caseoppgave - Datagrafikk ved UiS - Hallvard, Gunnstein og Stefan");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		//setUndecorated(true);
 		pack();
 		setSize(800, 800);
 		
 		setVisible(true);
 	}
 	
 	// Settings
 	String            saveDirectory;
 	
 	// Other stuff
 	PickCanvas        pc;
 	
 	TransformGroup[]  shapeMove;
 	Shape3D[]         shapes;
 	Appearance[]      appearance;
 	Material          material;
 	BoundingSphere    bounds;
 	CaseBehavior[]    behave;
 	CamBehavior	      camBehave;
 	
 	// Images
 	public ArrayList<String>   images;
 	public ArrayList<Integer>  images_used;
 	public ArrayList<Integer>  images_lastadded; // Last added, intergers refering to images
 	public int lastadded_max = 20; // How many images is considered "lastadded"
 	public int randomImageNum_maxTries = 100;
 	
 	public int lastImg = 0;
 	
 	public ArrayList<Integer> images_nevershown; // New images that are never shown before
 	
 	// Webcam
 	public static Player player;
 	public Buffer buf;
 	public Image img;
 	public BufferToImage btoi;
 	public CaptureDeviceInfo di;
 	public MediaLocator ml;
 	public FormatControl formatControl;
 	protected Component comp;
 	
 	public boolean cameraFound;
 	public String noCamImage;
 	
 
 	private BranchGroup createSceneGraph() {
 		int n = 5;
 		
 		/* root */
 		BranchGroup root = new BranchGroup();
 		bounds = new BoundingSphere();
 		
 		/* testTransform */
 		Transform3D tr = new Transform3D();
 		tr.setTranslation(new Vector3f(0.1f, 0.1f, 0.1f));
 		TransformGroup testTransform = new TransformGroup(tr);
 		testTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
 		
 		
 		//Spin
 		root.addChild(testTransform);
 		Alpha alpha = new Alpha(0, 8000);
 		RotationInterpolator rotator = new RotationInterpolator(alpha, testTransform);
 		rotator.setSchedulingBounds(bounds);
 		testTransform.addChild(rotator);
 		
 		/* background and lights */
 		Background background = new Background(1.0f, 1.0f, 1.0f);
 		//Background background = new Background(0f, 0f, 0f);
 		background.setApplicationBounds(bounds);
 		root.addChild(background);
 		AmbientLight light = new AmbientLight(true, new Color3f(Color.white));
 		light.setInfluencingBounds(bounds);
 		root.addChild(light);
 		PointLight ptlight = new PointLight(new Color3f(Color.WHITE),
 				new Point3f(3f, 3f, 3f), new Point3f(1f, 0f, 0f));
 		ptlight.setInfluencingBounds(bounds);
 		root.addChild(ptlight);
 		
 		/* Material */
 		material = new Material();
 		
 		// temp
 	    material.setAmbientColor(new Color3f(0f,0f,0f));
 	    material.setDiffuseColor(new Color3f(0.15f,0.15f,0.25f));
 	   
 	    
 		
 		/* Making shapes from 0 to n */
 		
 		// Make arrays
 		shapeMove   = new TransformGroup[n];
 		shapes      = new Shape3D[n];
 		appearance  = new Appearance[n];
 		behave      = new CaseBehavior[n];
 		
 		// Make shapes
 		for (int i = 0; i < n; i++) {
 			makeShapes(i);
 			testTransform.addChild(shapeMove[i]);
 			root.addChild(behave[i]);
 		}
 		
 		// Webcam box
 		Shape3D webcamBox = new Shape3D();
 		webcamBox = makeCamShape();
 		camBehave = new CamBehavior(webcamBox);
 		camBehave.setSchedulingBounds(bounds);
 		root.addChild(camBehave);
 		testTransform.addChild(webcamBox);
 		
 		/*
 		SharedGroup sg = new SharedGroup();
 		// object
 		for (int i = 0; i < n; i++) {
 			Transform3D tr1 = new Transform3D();
 			tr1.setRotation(new AxisAngle4d(0, 1, 0, 2 * Math.PI
 					* ((double) i / n)));
 			TransformGroup tgNew = new TransformGroup(tr1);
 			Link link = new Link();
 			link.setSharedGroup(sg);
 			tgNew.addChild(link);
 			tg.addChild(tgNew);
 
 		}*/
 		
 		/*
 		Shape3D torus1 = new Torus(0.1, 0.7);
 		Appearance ap = new Appearance();
 		ap.setMaterial(new Material());
 		torus1.setAppearance(ap);
 		tg.addChild(torus1);
 
 		Shape3D torus2 = new Torus(0.1, 0.4);
 		ap = new Appearance();
 		ap.setMaterial(new Material());
 		ap.setTransparencyAttributes(new TransparencyAttributes(
 				TransparencyAttributes.BLENDED, 0.0f));
 		torus2.setAppearance(ap);
 		Transform3D tr2 = new Transform3D();
 		tr2.setRotation(new AxisAngle4d(1, 0, 0, Math.PI / 2));
 		tr2.setTranslation(new Vector3d(0.8, 0, 0));
 
 		TransformGroup tg2 = new TransformGroup(tr2);
 		tg2.addChild(torus2);
 		*/
 
 		// SharedGroup
 		/*
 		sg.addChild(tg2);
 		Alpha alpha = new Alpha(0, 8000);
 		RotationInterpolator rotator = new RotationInterpolator(alpha, spin);
 		rotator.setSchedulingBounds(bounds);
 		spin.addChild(rotator);
 		*/
 		
 		return root;
 	}
 	
 	public void makeShapes (int i)
 	{
 		// Oppretter shape
 		shapes[i] = makeShape();
 				
 		// Oppretter shapeMove
 		shapeMove[i] = new TransformGroup();
 		shapeMove[i].addChild(shapes[i]);
 		
 		// Oppretter RotPosScaleIntepolator
 		shapeMove[i].setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
 		shapeMove[i].addChild(makeRotPosTingen(shapeMove[i]));
 		
 		// Oppretter Behavior
 		behave[i] = new CaseBehavior(shapes[i], shapeMove[i]);
 		behave[i].setSchedulingBounds(bounds);
 		
 	}
 	
 	public Shape3D makeShape ()
 	{
 		int shapeType = (int)(Math.random()*2);
 		Appearance ap = createAppearance(shapeType);
 		/*
 		return new Box(
 				(float) (0.05f * Math.random()),
 				(float) (0.05f * Math.random()),
 				(float) (0.05f * Math.random()),
 					Primitive.ENABLE_GEOMETRY_PICKING |
 					Primitive.ENABLE_APPEARANCE_MODIFY |
 					Primitive.GENERATE_NORMALS |
 					Primitive.GENERATE_TEXTURE_COORDS,ap);
 					   Sphere shape = new Sphere(0.7f, Primitive.GENERATE_TEXTURE_COORDS, 50, ap);
 					*/
 		//PickTool.setCapabilities(shapes[i], PickTool.INTERSECT_TEST);
 		
 		Shape3D shape = new Shape3D(getGeometry(shapeType), ap);
 		shape.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
 		shape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
 		shape.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
 		shape.setAppearance(ap);
 		return shape;
 	}
 	
 	public Shape3D makeCamShape ()
 	{
 		int shapeType = 0;
 		Appearance ap = createCamAppearance();
 		/*
 		return new Box(
 				(float) (0.05f * Math.random()),
 				(float) (0.05f * Math.random()),
 				(float) (0.05f * Math.random()),
 					Primitive.ENABLE_GEOMETRY_PICKING |
 					Primitive.ENABLE_APPEARANCE_MODIFY |
 					Primitive.GENERATE_NORMALS |
 					Primitive.GENERATE_TEXTURE_COORDS,ap);
 					   Sphere shape = new Sphere(0.7f, Primitive.GENERATE_TEXTURE_COORDS, 50, ap);
 					*/
 		//PickTool.setCapabilities(shapes[i], PickTool.INTERSECT_TEST);
 		
 		Shape3D shape = new Shape3D(getGeometry(shapeType), ap);
 		shape.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
 		shape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
 		shape.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
 		shape.setAppearance(ap);
 		return shape;
 	}
 	public RotPosScalePathInterpolator makeRotPosTingen(TransformGroup shapeMove)
 	{
 
 		Alpha alpha = new Alpha(-1, 8000);
 		Transform3D axisOfRotPos = new Transform3D();
 		float[] knots = { 0.0f, 0.3f, 0.5f, 0.7f, 1.0f };
 		Quat4f[] quats = new Quat4f[5];
 		Point3f[] positions = new Point3f[5];
 		
 		// Sizes:
 		float[] scales = {0.4f, 0.4f, 2.0f, 0.4f, 0.4f};
 		
 		AxisAngle4f axis = new AxisAngle4f(1.0f, 0.0f, 0.0f, 0.0f);
 		axisOfRotPos.set(axis);
 		
 		quats[0] = new Quat4f(0.0f, 0.0f, 0.0f, 0.0f);
 		quats[1] = new Quat4f(0.0f, 0.0f, 0.0f, 0.0f);
 		quats[2] = new Quat4f(0.0f, 0.0f, 0.0f, 0.0f);
 		quats[3] = new Quat4f(0.0f, 0.0f, 0.0f, 0.0f);
 		quats[4] = new Quat4f(0.0f, 0.0f, 0.0f, 0.0f);
 		
 		float avstand_ytre  = 0.5f*5;
 		float avstand_indre = 0.2f*5;
 		double theta = Math.random()* 2 * Math.PI;
 		positions[0] = new Point3f(
 				(float) (-avstand_ytre * Math.cos(theta)),
 				(float) (-avstand_ytre * Math.sin(theta)),
 				-1.0f);
 		positions[1] = new Point3f(
 				(float) (-avstand_indre * Math.cos(theta)),
 				(float) (-avstand_indre * Math.sin(theta)),
 				-1.0f);
 		positions[2] = new Point3f(
 				0.0f,
 				0.0f, 
 				-1.0f);
 		positions[3] = new Point3f(
 				(float) (avstand_indre * Math.cos(theta)),
 				(float) (avstand_indre * Math.sin(theta)),
 				-1.0f);
 		positions[4] = new Point3f(
 				(float) (avstand_ytre * Math.cos(theta)),
 				(float) (avstand_ytre * Math.sin(theta)),
 				-1.0f);
 		RotPosScalePathInterpolator rotPosScalePath = new RotPosScalePathInterpolator(alpha,
 				shapeMove, axisOfRotPos, knots, quats, positions, scales);
 		rotPosScalePath.setSchedulingBounds(bounds);
 		
 		return rotPosScalePath;
 	}
 	
 	public GeometryArray getGeometry(int shapeType)
 	{
 
 		GeometryInfo gi;
 		Point3d[] pts;
 		int[] indices;
 		
 		double scale;
 
 		if (shapeType == 0) {
 
 			double l = Math.random() * 0.4;
 			double w = Math.random() * 0.4;
 			// w = 0.04;
 			double h = Math.random() * 0.4;
 			// h=0.01;
 			gi = new GeometryInfo(GeometryInfo.QUAD_ARRAY);
 			pts = new Point3d[8];
 			pts[0] = new Point3d(0.5f, 0.5f, 0.5f);
 			pts[1] = new Point3d(0.5f, -0.5f, 0.5f);
 			pts[2] = new Point3d(-0.5f, -0.5f, 0.5f);
 			pts[3] = new Point3d(-0.5f, 0.5f, 0.5f);
 			pts[4] = new Point3d(0.5f, 0.5f, -0.5f);
 			pts[5] = new Point3d(0.5f, -0.5f, -0.5f);
 			pts[6] = new Point3d(-0.5f, -0.5f, -0.5f);
 			pts[7] = new Point3d(-0.5f, 0.5f, -0.5f);
 			
 			scale = 0.5;
 			
 			gi.setCoordinates(pts);
 			indices = new int[24];
 			indices[0] = 0;
 			indices[1] = 3;
 			indices[2] = 2;
 			indices[3] = 1;
 			indices[4] = 0;
 			indices[5] = 1;
 			indices[6] = 5;
 			indices[7] = 4;
 			indices[8] = 4;
 			indices[9] = 5;
 			indices[10] = 6;
 			indices[11] = 7;
 			indices[12] = 2;
 			indices[13] = 3;
 			indices[14] = 7;
 			indices[15] = 6;
 			indices[16] = 0;
 			indices[17] = 4;
 			indices[18] = 7;
 			indices[19] = 3;
 			indices[20] = 1;
 			indices[21] = 2;
 			indices[22] = 6;
 			indices[23] = 5;
 		}
 		//else if (shapeType == 1)
 		else
 		{
 			gi = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
 			double phi = 0.5 * (Math.sqrt(5) + 1);
 			pts = new Point3d[20];
 			pts[0] = new Point3d(1, 1, 1);
 			pts[1] = new Point3d(0, 1 / phi, phi);
 			pts[2] = new Point3d(phi, 0, 1 / phi);
 			pts[3] = new Point3d(1 / phi, phi, 0);
 			pts[4] = new Point3d(-1, 1, 1);
 			pts[5] = new Point3d(0, -1 / phi, phi);
 			pts[6] = new Point3d(1, -1, 1);
 			pts[7] = new Point3d(phi, 0, -1 / phi);
 			pts[8] = new Point3d(1, 1, -1);
 			pts[9] = new Point3d(-1 / phi, phi, 0);
 			pts[10] = new Point3d(-phi, 0, 1 / phi);
 			pts[11] = new Point3d(-1, -1, 1);
 			pts[12] = new Point3d(1 / phi, -phi, 0);
 			pts[13] = new Point3d(1, -1, -1);
 			pts[14] = new Point3d(0, 1 / phi, -phi);
 			pts[15] = new Point3d(-1, 1, -1);
 			pts[16] = new Point3d(-1 / phi, -phi, 0);
 			pts[17] = new Point3d(-phi, 0, -1 / phi);
 			pts[18] = new Point3d(0, -1 / phi, -phi);
 			pts[19] = new Point3d(-1, -1, -1);
 			
 			scale = 0.2;
 			
 			int i = 0;
 			indices = new int[60];
 			indices[i] = 0;			i++;
 			indices[i] = 1;			i++;
 			indices[i] = 5;			i++;
 			indices[i] = 6;			i++;
 			indices[i] = 2;			i++;
 			
 			indices[i] = 0;			i++;
 			indices[i] = 2;			i++;
 			indices[i] = 7;			i++;
 			indices[i] = 8;			i++;
 			indices[i] = 3;			i++;
 			// 10
 			indices[i] = 0;			i++;
 			indices[i] = 3;			i++;
 			indices[i] = 9;			i++;
 			indices[i] = 4;			i++;
 			indices[i] = 1;			i++;
 			
 			indices[i] = 1;			i++;
 			indices[i] = 4;			i++;
 			indices[i] = 10;		i++;
 			indices[i] = 11;		i++;
 			indices[i] = 5;			i++;
 			// 20
 			indices[i] = 2;			i++;
 			indices[i] = 6;			i++;
 			indices[i] = 12;		i++;
 			indices[i] = 13;		i++;
 			indices[i] = 7;			i++;
 			
 			indices[i] = 3;			i++;
 			indices[i] = 8;			i++;
 			indices[i] = 14;		i++;
 			indices[i] = 15;		i++;
 			indices[i] = 9;			i++;
 			// 30
 			indices[i] = 5;			i++;
 			indices[i] = 11;		i++;
 			indices[i] = 16;		i++;
 			indices[i] = 12;		i++;
 			indices[i] = 6;			i++;
 			
 			indices[i] = 7;			i++;
 			indices[i] = 13;		i++;
 			indices[i] = 18;		i++;
 			indices[i] = 14;		i++;
 			indices[i] = 8;			i++;
 			// 40
 			indices[i] = 9;			i++;
 			indices[i] = 15;		i++;
 			indices[i] = 17;		i++;
 			indices[i] = 10;		i++;
 			indices[i] = 4;			i++;
 			
 			indices[i] = 19;		i++;
 			indices[i] = 16;		i++;
 			indices[i] = 11;		i++;
 			indices[i] = 10;		i++;
 			indices[i] = 17;		i++;
 			// 50
 			indices[i] = 19;		i++;
 			indices[i] = 17;		i++;
 			indices[i] = 15;		i++;
 			indices[i] = 14;		i++;
 			indices[i] = 18;		i++;
 			
 			indices[i] = 19;		i++;
 			indices[i] = 18;		i++;
 			indices[i] = 13;		i++;
 			indices[i] = 12;		i++;
 			indices[i] = 16;		i++;
 			// 60
 			int[] stripCounts = { 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5 };
 			gi.setStripCounts(stripCounts);
 		}
 		
 		// Fix scale
 		double a, b, c;
 		for (int i = 0; i < pts.length; i++) {
 			a = pts[i].getX() * scale;
 			b = pts[i].getY() * scale;
 			c = pts[i].getZ() * scale;
 			pts[i] = new Point3d(a, b, c);
 		}
 		
 		gi.setCoordinates(pts);
 		gi.setCoordinateIndices(indices);
 		NormalGenerator ng = new NormalGenerator();
 		ng.generateNormals(gi);
 		return gi.getGeometryArray();
 	}
 	
 	public Appearance createAppearance(int shapeType) {
 		Appearance appear = new Appearance();
 		/*
 		URL filename;
 		if(Math.random() > 0.5)
 			filename = getClass().getClassLoader().getResource(
 			"images/earth.jpg");
 		else
 			filename = getClass().getClassLoader().getResource(
 				"images/stone.jpg");*/
 		
 		TextureLoader loader = new TextureLoader(getRandomImage(), this);
 		ImageComponent2D image = loader.getImage();
 
 		
 		boolean cube = (image.getWidth() == image.getHeight());
 
 	    TexCoordGeneration tcg = new TexCoordGeneration(TexCoordGeneration.OBJECT_LINEAR, 
 		TexCoordGeneration.TEXTURE_COORDINATE_3);
 		tcg.setPlaneR(new Vector4f(2, 0, 0, 0));
 		tcg.setPlaneS(new Vector4f(0, 2, 0, 0));
 		tcg.setPlaneT(new Vector4f(0, 0, 2, 0));
 		appear.setTexCoordGeneration(tcg);
 		appear.setCapability(Appearance.ALLOW_TEXGEN_WRITE);
 		
 		boolean strekk = false;
 		if(cube)
 		{
 			TextureCubeMap texture = new TextureCubeMap(Texture.BASE_LEVEL, Texture.RGBA,
 					 image.getWidth());
 			texture.setEnable(true);
 			texture.setMagFilter(Texture.BASE_LEVEL_LINEAR);
 			texture.setMinFilter(Texture.BASE_LEVEL_LINEAR);
 		    appear.setTexture(texture);
 		    
 			// definerer bilde for hver av sidene for Dodecahedron
 			if(shapeType == 1)
 			{
 				 		    texture.setImage(0, TextureCubeMap.NEGATIVE_X, image);
 						    texture.setImage(0, TextureCubeMap.NEGATIVE_Y, image);
 						    texture.setImage(0, TextureCubeMap.NEGATIVE_Z, image);
 						    texture.setImage(0, TextureCubeMap.POSITIVE_X, image);
 						    texture.setImage(0, TextureCubeMap.POSITIVE_Y, image);
 						    texture.setImage(0, TextureCubeMap.POSITIVE_Z, image);
 			}			  
 			// definerer bilde for hver av sidene for firkant
 			else if(cube && shapeType == 0)
 			{
 						    texture.setImage(0, TextureCubeMap.NEGATIVE_X, image);
 						    texture.setImage(0, TextureCubeMap.NEGATIVE_Y, image);
 						    texture.setImage(0, TextureCubeMap.NEGATIVE_Z, image);
 						    texture.setImage(0, TextureCubeMap.POSITIVE_X, image);
 						    texture.setImage(0, TextureCubeMap.POSITIVE_Y, image);
 						    texture.setImage(0, TextureCubeMap.POSITIVE_Z, image);   
 			}
 			else
 			{
 				strekk = true;
 			}
 		}
 		// strekker bilde 
 		else
 		{
 			strekk = true;
 		}
 		
 		if(strekk)
 		{
 			Texture2D texture = new Texture2D(Texture.BASE_LEVEL, Texture.RGBA,
 			image.getWidth(), image.getHeight());
 			texture.setImage(0, image);
 			tcg.setGenMode(TexCoordGeneration.OBJECT_LINEAR);
 			appear.setMaterial(material);
 			appear.setTexCoordGeneration(tcg);
 			appear.setTransparencyAttributes(new TransparencyAttributes(
 					TransparencyAttributes.BLENDED, 0.0f));
 
 		    appear.setTexture(texture);
 		}
 		return appear;
 	}
 	
 	public class CaseBehavior extends Behavior 
 	{
 		Shape3D shape;
 		TransformGroup shapeMove;
 		
 		public CaseBehavior (Shape3D shape, TransformGroup shapeMove)
 		{
 			this.shape = shape;
 			this.shapeMove = shapeMove;
 		}
 
 		@Override
 		public void initialize()
 		{
 			// Time for testing purpose
 			wakeupOn(new WakeupOnElapsedTime((int)(Math.random()*5000) + 1000));
 		}
 
 		@Override
 		public void processStimulus(Enumeration arg0)
 		{
 			int shapeType = (int)(Math.random()*2);
 			shape.setGeometry(getGeometry(shapeType));
 			shape.setAppearance(createAppearance(shapeType));
 			
 			// TODO: remove old child
 			//shapeMove.addChild(makeRotPosTingen(shapeMove));
 			
 			// Time for testing purpose
 			wakeupOn(new WakeupOnElapsedTime((int)(Math.random()*5000) + 1000));
 		}
 		
 	}
 	
 	public class CamBehavior extends Behavior 
 	{
 		Shape3D shape;
 		
 		public CamBehavior (Shape3D shape)
 		{
 			this.shape = shape;
 		}
 
 		@Override
 		public void initialize()
 		{
 			// Time for testing purpose
 			wakeupOn(new WakeupOnElapsedTime((int)(1000/30)));
 		}
 
 		@Override
 		public void processStimulus(Enumeration arg0)
 		{
 			shape.setAppearance(createCamAppearance());
 			
 			// Time for testing purpose
 			wakeupOn(new WakeupOnElapsedTime((int)(1000/30)));
 		}
 		
 	}
 	
 	public Appearance createCamAppearance() {
 		Appearance appear = new Appearance();
 		/*
 		URL filename;
 		if(Math.random() > 0.5)
 			filename = getClass().getClassLoader().getResource(
 			"images/earth.jpg");
 		else
 			filename = getClass().getClassLoader().getResource(
 				"images/stone.jpg");*/
 		
 		TextureLoader loader = new TextureLoader(getCamImage(), this);
 		ImageComponent2D image = loader.getImage();
 		
 
 	    TexCoordGeneration tcg = new TexCoordGeneration(TexCoordGeneration.OBJECT_LINEAR, 
 		TexCoordGeneration.TEXTURE_COORDINATE_3);
 		tcg.setPlaneR(new Vector4f(2, 0, 0, 0));
 		tcg.setPlaneS(new Vector4f(0, 2, 0, 0));
 		tcg.setPlaneT(new Vector4f(0, 0, 2, 0));
 		appear.setTexCoordGeneration(tcg);
 		appear.setCapability(Appearance.ALLOW_TEXGEN_WRITE);
 		
 		Texture2D texture = new Texture2D(Texture.BASE_LEVEL, Texture.RGBA,
 		image.getWidth(), image.getHeight());
 		texture.setImage(0, image);
 		tcg.setGenMode(TexCoordGeneration.OBJECT_LINEAR);
 		appear.setMaterial(material);
 		appear.setTexCoordGeneration(tcg);
 		appear.setTransparencyAttributes(new TransparencyAttributes(
 				TransparencyAttributes.BLENDED, 0.0f));
 
 	    appear.setTexture(texture); 
 	    
 		return appear;
 	}
 	
 	protected void getImages() {
 		File directory = new File(this.saveDirectory);
 
 		//BufferedImage img = null;
 		
 		images = new ArrayList<String>();
 		boolean noCamFound = false;
 		if( directory.exists() && directory.isDirectory())
 		{
 			//File[] files = directory.listFiles();
 			String[] files = directory.list();
 		
 			for(int i=0; i < files.length; i++)
 			{
 				if(files[i].endsWith("jpg"))
 				{
 					if(files[i].equals("feilmedkamera.jpg"))
 					{
 						noCamImage = this.saveDirectory + File.separator + files[i];
 						noCamFound = true;
 					}
 					else
 					{
 						images.add(this.saveDirectory + File.separator + files[i]);
 					}
 				}
 			}
 		}
 		
 		if(!noCamFound)
 		{
 			System.out.println("feilmedkamera.jpg not found in image folder. Using a random image as feilmedkamera.jpg");
 			noCamImage = images.get((int)(Math.random()*images.size()));
 		}
 		
 		System.out.println("Total image count = " + images.size());
 	}
 	
 	public Image getImage(int imagenum)
 	{
 		if(imagenum == -1)
 		{
 			return null;
 		}
 		
 		String path = images.get(imagenum);
 		//System.out.println("Path - getImage: " + path);
 		try {
 			return ImageIO.read(new File(path));
 		} catch (IOException e) {
 			System.out.println("Path til ikke funnet: " + path);
 			return null;
 		}
 	}
 	
 	public Image getNoCamImage()
 	{
 		System.out.println("Path - getNoCamImage: " + noCamImage);
 		try {
 			return ImageIO.read(new File(noCamImage));
 		} catch (IOException e) {
 			System.out.println("Path til ikke funnet: " + noCamImage);
 			return null;
 		}
 	}
 	
 	public Image getRandomImage()
 	{
 		return getImage((int)(Math.random()*images.size()));
 	}
 	
 	public void captureImage()
 	{
 		String savepath = this.saveDirectory + "\\cam"
 		+ this.getDateFormatNow("yyyyMMdd_HHmmss-S") + ".jpg";
 		System.out.println("Capturing current image to " +savepath);
 		
 		// Grab a frame
 		FrameGrabbingControl fgc = (FrameGrabbingControl) player
 				.getControl("javax.media.control.FrameGrabbingControl");
 		buf = fgc.grabFrame();
 		
 		// Convert it to an image
 		btoi = new BufferToImage((VideoFormat) buf.getFormat());
 		img = btoi.createImage(buf);
 		
 		if(img == null)
 		{
 			JOptionPane.showMessageDialog(null, "Feil med kamera. Fikk null img");
 		}
 		else
 		{
 			
 			// save image
 			saveJPG(img.getScaledInstance(265, 265, Image.SCALE_SMOOTH), savepath);
 			
 			// show the image
 			//imgpanel.setImage(img);
 			
 			//images.add(img);
 			images.add(savepath);
 			
 			if(images_lastadded.size() >= lastadded_max)
 			{
 				// Remove last
 				images_lastadded.remove(images_lastadded.size()-1);
 			}
 	
 			images_lastadded.add(0, images.size()-1);
 			images_nevershown .add(0, images.size()-1);
 		}
 	}
 	
 	public String getDateFormatNow(String dateFormat)
 	{
 		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
 		return sdf.format(Calendar.getInstance().getTime());
 	}
 	
 	public static void saveJPG(Image img, String s) {
 		BufferedImage bi = new BufferedImage(
 				img.getWidth(null), 
 				img.getHeight(null), 
 				BufferedImage.TYPE_INT_RGB);
 		Graphics2D g2 = bi.createGraphics();
 		g2.drawImage(img, null, null);
 
 		FileOutputStream out = null;
 		try {
 			out = new FileOutputStream(s);
 		} catch (java.io.FileNotFoundException io) {
 			System.out.println("File Not Found");
 		}
 		
 		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
 		JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(bi);
 		param.setQuality(0.5f, false);
 		encoder.setJPEGEncodeParam(param);
 
 		try {
 			encoder.encode(bi);
 			out.close();
 		} catch (java.io.IOException io) {
 			System.out.println("IOException");
 		}
 	}
 	
 	public Image getCamImage ()
 	{
 		if(!cameraFound)
 		{
 			return getNoCamImage();
 		}
 		
 		// Grab a frame
 		FrameGrabbingControl fgc = (FrameGrabbingControl) player
 				.getControl("javax.media.control.FrameGrabbingControl");
 		buf = fgc.grabFrame();
 		
 		// Convert it to an image
 		btoi = new BufferToImage((VideoFormat) buf.getFormat());
 		img = btoi.createImage(buf);
 		
 		if(img == null)
 		{
 			System.out.println("Feil med henting av bilde fra kamera. img == null");
 			return getNoCamImage();
 		}
 		else
 			return img.getScaledInstance(256, 256, Image.SCALE_FAST);
 	}
 	
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 		if(e.getKeyCode() == 67) // C 
 		{
 			if(cameraFound)
 				this.captureImage();
 			else
 				JOptionPane.showMessageDialog(null, "Ingen kamera koblet til. Kan ikke hente bilde.");
 		}
 		
 		else if(e.getKeyCode() == 27) // Escape
 		{
 			System.out.println("Escape pressed, exiting");
 			System.exit(0);
 		}
 		
 		else {
 			displayInfo(e, "KEY TYPED: ");
 		}
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 		
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 		
 	}
 	
 	private void displayInfo(KeyEvent e, String keyStatus) {
 		// Method copied from http://java.sun.com/docs/books/tutorial/uiswing/events/keylistener.html
 
 		//You should only rely on the key char if the event
 		//is a key typed event.
 		int id = e.getID();
 		String keyString;
 		if (id == KeyEvent.KEY_TYPED) {
 			char c = e.getKeyChar();
 			keyString = "key character = '" + c + "'";
 		} else {
 			int keyCode = e.getKeyCode();
 			keyString = "key code = " + keyCode + " ("
 					+ KeyEvent.getKeyText(keyCode) + ")";
 		}
 
 		int modifiersEx = e.getModifiersEx();
 		String modString = "extended modifiers = " + modifiersEx;
 		String tmpString = KeyEvent.getModifiersExText(modifiersEx);
 		if (tmpString.length() > 0) {
 			modString += " (" + tmpString + ")";
 		} else {
 			modString += " (no extended modifiers)";
 		}
 
 		String actionString = "action key? ";
 		if (e.isActionKey()) {
 			actionString += "YES";
 		} else {
 			actionString += "NO";
 		}
 
 		String locationString = "key location: ";
 		int location = e.getKeyLocation();
 		if (location == KeyEvent.KEY_LOCATION_STANDARD) {
 			locationString += "standard";
 		} else if (location == KeyEvent.KEY_LOCATION_LEFT) {
 			locationString += "left";
 		} else if (location == KeyEvent.KEY_LOCATION_RIGHT) {
 			locationString += "right";
 		} else if (location == KeyEvent.KEY_LOCATION_NUMPAD) {
 			locationString += "numpad";
 		} else { // (location == KeyEvent.KEY_LOCATION_UNKNOWN)
 			locationString += "unknown";
 		}
 
 		// Added:
 		System.out.println("Keypress: " + keyString);
 	}
 }
