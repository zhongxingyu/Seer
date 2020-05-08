 package Case;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 import javax.swing.SwingWorker;
 import javax.vecmath.*;
 
 import java.awt.*;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.image.BufferedImage;
 
 import javax.imageio.ImageIO;
 import javax.imageio.stream.FileImageInputStream;
 import javax.imageio.stream.FileImageOutputStream;
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
 import com.sun.j3d.utils.picking.PickResult;
 import com.sun.j3d.utils.picking.PickTool;
 import com.sun.j3d.utils.picking.behaviors.PickRotateBehavior;
 import com.sun.j3d.utils.universe.*;
 import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
 import com.sun.j3d.utils.geometry.*;
 import com.sun.j3d.utils.image.TextureLoader;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Enumeration;
 import java.util.Vector;
 /*
 import com.sun.image.codec.jpeg.JPEGCodec;
 import com.sun.image.codec.jpeg.JPEGImageEncoder;
 import com.sun.image.codec.jpeg.JPEGEncodeParam;
  */
 
 public class Case extends JFrame implements KeyListener, MouseListener, MouseMotionListener {
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
 				
 				if ((comp = player.getVisualComponent()) != null) {
 					add(comp, BorderLayout.EAST);
 				}
 			}
 			catch(javax.media.NoPlayerException e) 
 			{
 				 JOptionPane.showMessageDialog(null, "Klarer ikke å starte"+
 						 " kamera. Sjekk at det er koblet til.", 
 						 "IOException", 
 						 JOptionPane.ERROR_MESSAGE);
 				 cameraFound = false;
 			}
 			catch (Exception e)
 			{
 				e.printStackTrace();
 				cameraFound = false;
 			}
 		}
 
 
 		// Create canvas
 		GraphicsConfiguration gc = SimpleUniverse.getPreferredConfiguration();
 		Canvas3D cv = new Canvas3D(gc);
 		cv.addKeyListener(this);
 		cv.addMouseListener(this);
 		cv.addMouseMotionListener(this);
 		add(cv, BorderLayout.CENTER);
 		BranchGroup bg = createSceneGraph(cv);
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
 	float             avstand_ytre  = 0.5f*5;
 	float             avstand_indre = 0.2f*5;
 	float             avstand_buffer = 0.5f;
 
 	// Other stuff
 	PickCanvas        pc;
 
 	TransformGroup[]  shapeMove;
 	Primitive[]         shapes;
 	RotPosScalePathInterpolator[] rotPosScale;
 	Appearance[]      appearance;
 	Material          material;
 	BoundingSphere    bounds;
 	CaseBehavior[]    behave;
 	ArrayList<rotationBehave> behaveRotating;
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
 
 	Primitive webcamBox;
 
 	private BranchGroup createSceneGraph(Canvas3D cv) {
 		int n = 5;
 
 		/* root */
 		BranchGroup root = new BranchGroup();
 		bounds = new BoundingSphere();
 		
 		/* testTransform */
 		Transform3D tr = new Transform3D();
 		tr.setTranslation(new Vector3f(0.1f, 0.1f, 0.1f));
 		TransformGroup testTransform = new TransformGroup(tr);
 		testTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
 
 
 		// rotere enkelte objekter
 		/*testTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
 	    testTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
 		MouseRotate rotator1 = new MouseRotate(testTransform);
 	    BoundingSphere bounds = new BoundingSphere();
 	    rotator1.setSchedulingBounds(bounds);
 	    testTransform.addChild(rotator1);
 		 */
 
 		PickRotateBehavior rotatorObjekt = new PickRotateBehavior(root, cv, bounds, 
 				PickTool.GEOMETRY);
 		root.addChild(rotatorObjekt);
 
 
 
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
 		shapes      = new Primitive[n];
 		rotPosScale = new RotPosScalePathInterpolator[n];
 		appearance  = new Appearance[n];
 		behave      = new CaseBehavior[n];
 		behaveRotating = new ArrayList<rotationBehave>();
 		
 		// Make shapes
 		for (int i = 0; i < n; i++) {
 			makeShapes(i);
 			testTransform.addChild(shapeMove[i]);
 			root.addChild(behave[i]);
 		}
 
 		// Webcam box
 		TransformGroup wbTransform = new TransformGroup();
 		Transform3D webTr = new Transform3D();
 		webTr.setTranslation(new Vector3d(-0.5,0.5,0));
 		wbTransform.setTransform(webTr);
 
 		webcamBox = makeCamShape();
 
 		TransformGroup rotatorCam = new TransformGroup();
 		rotatorCam.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
 		rotatorCam.addChild(webcamBox);
 		wbTransform.addChild(rotatorCam);
 		
 		camBehave = new CamBehavior(webcamBox, rotatorCam);
 		camBehave.setSchedulingBounds(bounds);
		root.addChild(camBehave);
 		
 		testTransform.addChild(wbTransform);
 		
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
 		
 		// Oppretter rotator
 		TransformGroup rotator = new TransformGroup();
 		rotator.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
 		rotator.addChild(shapes[i]);
 		
 		// Oppretter shapeMove
 		shapeMove[i] = new TransformGroup();
 		shapeMove[i].addChild(rotator);
 		shapeMove[i].setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
 
 		// Oppretter RotPosScaleIntepolator
 		rotPosScale[i] = makeRotPosTingen(shapeMove[i]);
 		shapeMove[i].addChild(rotPosScale[i]);
 
 		// Oppretter Behavior
 		behave[i] = new CaseBehavior(shapes[i], shapeMove[i], rotPosScale[i], rotator);
 		behave[i].setSchedulingBounds(bounds);
 
 	}
 
 	public Primitive makeShape ()
 	{
 		int shapeType = (int)(Math.random()*2);
 		Appearance ap = createAppearance(shapeType);
 		Primitive shape = new Box(
 				(float) (0.4),
 				(float) (0.4),
 				(float) (0.4),
 				Primitive.ENABLE_GEOMETRY_PICKING |
 				Primitive.ENABLE_APPEARANCE_MODIFY |
 				Primitive.GENERATE_NORMALS |
 				Primitive.GENERATE_TEXTURE_COORDS,ap);
 
 		//Sphere shape = new Sphere(0.7f, Primitive.GENERATE_TEXTURE_COORDS, 50, ap);
 
 		/*
 		Primitive shape = new Primitive(getGeometry(shapeType), ap);
 		PickTool.setCapabilities(shape, PickTool.INTERSECT_FULL);
 		shape.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
 		shape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
 		shape.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
 		shape.setCapability(Shape3D.ENABLE_PICK_REPORTING);
 
 		shape.setAppearance(ap);
 		 */
 		return shape;
 	}
 
 	public Primitive makeCamShape ()
 	{
 		Appearance ap = createCamAppearance();
 		Primitive shape = new Box(
 				(float) (0.2),
 				(float) (0.2),
 				(float) (0.2),
 					Primitive.ENABLE_GEOMETRY_PICKING |
 					Primitive.ENABLE_APPEARANCE_MODIFY |
 					Primitive.GENERATE_NORMALS |
 					Primitive.GENERATE_TEXTURE_COORDS,ap);
 		shape.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
 		shape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
 		shape.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
 		
 		
 		return shape;
 	}
 
 	public float[] getRotPosKnots() 
 	{
 		float[] knots = { 0.0f, 0.3f, 0.5f, 0.7f, 1.0f };
 		return knots;
 	}
 
 	public Quat4f[] getRotPosQuats() 
 	{
 		Quat4f[] quats = new Quat4f[5];
 		quats[0] = new Quat4f(0.0f, 0.0f, 0.0f, 0.0f);
 		quats[1] = new Quat4f(0.0f, 0.0f, 0.0f, 0.0f);
 		quats[2] = new Quat4f(0.0f, 0.0f, 0.0f, 0.0f);
 		quats[3] = new Quat4f(0.0f, 0.0f, 0.0f, 0.0f);
 		quats[4] = new Quat4f(0.0f, 0.0f, 0.0f, 0.0f);
 
 		return quats;
 	}
 
 	public Point3f[] getRandomPositionsTilRotPos ()
 	{
 		double theta = Math.random()* 2 * Math.PI;
 
 		Point3f[] positions = new Point3f[5];
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
 
 		return positions;
 	}
 
 	public float[] getRotPosScales ()
 	{
 		float[] scales = {0.4f, 0.4f, 2.0f, 0.4f, 0.4f};
 		return scales;
 	}
 
 	public RotPosScalePathInterpolator makeRotPosTingen(TransformGroup shapeMove)
 	{
 
 		Alpha alpha = new Alpha(-1, 8000);
 		Transform3D axisOfRotPos = new Transform3D();
 
 		AxisAngle4f axis = new AxisAngle4f(1.0f, 0.0f, 0.0f, 0.0f);
 		axisOfRotPos.set(axis);
 
 		RotPosScalePathInterpolator rotPosScalePath = new RotPosScalePathInterpolator(alpha,
 				shapeMove, axisOfRotPos, getRotPosKnots(), getRotPosQuats(), getRandomPositionsTilRotPos(), getRotPosScales());
 		rotPosScalePath.setSchedulingBounds(bounds);
 
 		return rotPosScalePath;
 	}
 
 	/*public void getGeometry(int shapeType)
 	{
 		if (shapeType ==0){
 			newBox();
 		}
 		else {
 			newSphere();
 		}
 	}
 	*/
 	public Appearance createAppearance(int shapeType) {
 		Appearance appear = new Appearance();
 		TextureLoader loader = new TextureLoader(getRandomImage(), this);
 		ImageComponent2D image = loader.getImage();
 		if(image ==null){
 			System.out.println("Finner ikke bilde.");
 		}
 		Texture2D texture = new Texture2D(Texture.BASE_LEVEL,Texture.RGBA,
 				image.getWidth(),image.getHeight());
 		texture.setImage(0, image);
 		texture.setEnable(true);
 		texture.setMagFilter(texture.BASE_LEVEL_LINEAR);
 		texture.setMinFilter(texture.BASE_LEVEL_LINEAR);
 		appear.setTexture(texture);
 		return appear;
 	}
 	
 	public class CaseBehavior extends rotationBehave
 	{
 		TransformGroup shapeMove;
 		RotPosScalePathInterpolator rotPos;
 		boolean passed_zero = false;
 
 		public CaseBehavior (Primitive shape, TransformGroup shapeMove, RotPosScalePathInterpolator ting, TransformGroup rotator)
 		{
 			this.shape = shape;
 			this.shapeMove = shapeMove;
 			this.rotPos = ting;
 			this.rotator = rotator;
 		}
 
 		@Override
 		public void initialize()
 		{
 			// Time for testing purpose
 			wakeupOn(new WakeupOnElapsedTime(50));
 		}
 
 		@Override
 		public void processStimulus(Enumeration arg0)
 		{
 			Transform3D grpTransform = new Transform3D();
 			shapeMove.getTransform(grpTransform);
 
 			Vector3f location= new Vector3f();
 			grpTransform .get(location);
 
 			double avstand = Math.sqrt(location.getX()*location.getX() + location.getY()*location.getY());
 			if(avstand > (avstand_ytre-avstand_buffer))
 			{
 				if(passed_zero)
 				{
 					int shapeType = (int)(Math.random()*2);
 
 					// Get random geometry
 					//TODO: Må fikses
 					//shape.setGeometry(getGeometry(shapeType));
 
 					// Get new appearance (new image/texture)
 					shape.setAppearance(createAppearance(shapeType));
 
 					// Set new path
 					rotPos.setPathArrays(getRotPosKnots(), getRotPosQuats(), getRandomPositionsTilRotPos(), getRotPosScales());
 
 					passed_zero = false;
 				}
 			}
 			else if(!passed_zero)
 			{
 				passed_zero = true;
 			}
 
 			// Time for testing purpose
 			wakeupOn(new WakeupOnElapsedTime(50));
 		}
 	}
 	
 	public class CamBehavior extends rotationBehave
 	{
 		public CamBehavior (Primitive shape, TransformGroup rotator)
 		{
 			this.shape = shape;
 			this.rotator = rotator;
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
 	
 	public abstract class rotationBehave extends Behavior
 	{
 		Primitive shape;
 		Point mouseStart;
 		Point mouseLast;
 		TransformGroup rotator;
 		
 		public void rotateStart(Point mouse)
 		{
 			//System.out.println("rotateStart: " + shape.toString());
 			this.mouseStart = this.mouseLast = mouse;
 			behaveRotating.add(this);
 		}
 		
 		public void rotate(Point mouse)
 		{
 			//System.out.println("rotate: " + shape.toString());
 			if(mouseStart != null)
 			{
 				if(!mouseLast.equals(mouse))
 				{
 					mouseLast = mouse;
 					double distanceX = mouse.getX() - mouseStart.getX();
 					double distanceY = mouse.getY() - mouseStart.getY();
 					
 					Transform3D rotY = new Transform3D();
 					rotY.rotY(0.03*distanceX);
 					
 					Transform3D transform = new Transform3D();
 					transform.rotX(0.03*distanceY);
 					transform.mul(rotY);
 					rotator.setTransform(transform);
 					
 					//System.out.println("Distance: " + (mouse.distance(mouseStart)) +
 					//		", distanceX: " + distanceX +
 					//		", distanceY: " + distanceY);
 				}
 			}
 		}
 		
 		public void rotateStop()
 		{
 			//System.out.println("rotateStop: " + shape.toString());
 			behaveRotating.remove(this);
 			mouseStart = null;
 		}
 		
 	}
 	
 	public Appearance createCamAppearance() {
 		Appearance appear = new Appearance();
 		
 		TextureLoader loader = new TextureLoader(getCamImage(), this);
 		ImageComponent2D image = loader.getImage();
 
 
 		
 
 		Texture2D texture = new Texture2D(Texture.BASE_LEVEL, Texture.RGBA,
 				image.getWidth(), image.getHeight());
 		texture.setImage(0, image);
 		texture.setEnable(true);
 		texture.setMagFilter(texture.BASE_LEVEL_LINEAR);
 		texture.setMinFilter(texture.BASE_LEVEL_LINEAR);
 		/*
 		appear.setMaterial(material);
 		appear.setTransparencyAttributes(new TransparencyAttributes(
 				TransparencyAttributes.BLENDED, 0.0f));
 		*/
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
 		//System.out.println("Path - getNoCamImage: " + noCamImage);
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
 		if(!cameraFound)
 		{
 			JOptionPane.showMessageDialog(null, "Ingen kamera koblet til. Kan ikke hente bilde.");
 			return;
 		}
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
 				256, 
 				256, 
 				BufferedImage.TYPE_INT_RGB);
 		Graphics2D g2 = bi.createGraphics();
 		g2.drawImage(img, null, null);
 		try {
 			ImageIO.write(bi,"jpg", new FileImageOutputStream(new File(s)));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		/*
 		FileOutputStream out = null;
 		try {
 			out = new FileOutputStream(s);
 		} catch (java.io.FileNotFoundException io) {
 			System.out.println("File Not Found");
 		}*/
 		/*
 		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
 		JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(bi);
 		param.setQuality(0.5f, false);
 		encoder.setJPEGEncodeParam(param);
 
 		try {
 			encoder.encode(bi);
 			out.close();
 		} catch (java.io.IOException io) {
 			System.out.println("IOException");
 		}*/
 	}
 
 	public BufferedImage getCamImage ()
 	{
 		if(!cameraFound)
 		{
 			return (BufferedImage)getNoCamImage();
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
 			return (BufferedImage)getNoCamImage();
 		}
 		else
 		{
 			BufferedImage bi = new BufferedImage(
 					256, 
 					256, 
 					BufferedImage.TYPE_INT_RGB);
 			Graphics2D g2 = bi.createGraphics();
 			g2.drawImage(img, null, null);
 
 			return bi;
 			//return img.getScaledInstance(256, 256, Image.SCALE_FAST);
 		}
 	}
 
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 		if(e.getKeyCode() == 67) // C 
 		{
 			this.captureImage();
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
 
 	@Override
 	public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {
 		System.out.println("Picking:D");
 		pc.setShapeLocation(mouseEvent);
 		PickResult[] results = pc.pickAll();
 		for (int i = 0; (results != null) && (i < results.length); i++) {
 			Node node = results[i].getObject();
 			if (node instanceof Shape3D) {
 				System.out.println("clicked: " + node.toString());
 				if(node == webcamBox){
 					captureImage();
 				}
 			}
 		}
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseExited(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mousePressed(MouseEvent arg0)
 	{
 		// Most likely one of the moving objects
 
 		pc.setShapeLocation(arg0);
 		PickResult[] results = pc.pickAll();
 		for (int i = 0; (results != null) && (i < results.length); i++) {
 			Node node = results[i].getObject().getParent();
 			if (node instanceof Primitive)
 			{
 				if(node != webcamBox){
 					for (int j = 0; j < shapes.length; j++)
 					{
 						if(node == shapes[j])
 						{
 							// Start rotation in this point
 							behave[j].rotateStart(arg0.getPoint());
 						}
 					}
 				} else
 				{
 					camBehave.rotateStart(arg0.getPoint());
 				}
 			}
 		}
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent arg0) {
 		// Stop all rotation
 		if(behaveRotating.size() > 0)
 		{
 			for (int i = 0; i < behaveRotating.size(); i++) {
 				behaveRotating.get(i).rotateStop();
 			}
 		}
 	}
 
 	@Override
 	public void mouseDragged(MouseEvent e) {
 		if(behaveRotating.size() > 0)
 		{
 			for (int i = 0; i < behaveRotating.size(); i++) {
 				behaveRotating.get(i).rotate(e.getPoint());
 			}
 		}
 		
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent e) {
 	}
 }
