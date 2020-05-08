 import java.awt.*;
 import java.awt.event.*;
 import java.util.Enumeration;
 import javax.media.j3d.*;
 import javax.vecmath.*;
 import javax.swing.*;
 import com.sun.j3d.utils.picking.PickCanvas;
 import com.sun.j3d.utils.picking.PickResult;
 
 
 public class SwingTest extends JPanel implements MouseListener, MouseMotionListener, KeyListener {
 	private static int sphereCount = 0;
 	private static int rectPrismCount = 0;
 	private static int hexPrismCount = 0;
 	private static int triPrismCount = 0;
 	private static int pyramidCount = 0;
 	
 	private int mouseX, mouseY;
 	private int mouseButton;
 	private float tx, ty;
 	private String curPos;
 	
 	private static BranchGroup    sceneBranchGroup = null;
 	private RotationInterpolator  rotator = null;
 	private static Canvas3D c3d;
 	private static PickCanvas pickCanvas;
 	
 	private String selectedUserData;
 	private Node shapeClicked;
 	private Node shapeToDrag;
 	private TransformGroup tgArray;
 	
 	private Transform3D changeSize;
  	private Transform3D stayInPos;
 
 	
 	Color3f red = new Color3f (Color.RED);
 	Color3f pink = new Color3f (Color.PINK);
 	Color3f orange = new Color3f (Color.ORANGE);
 	Color3f yellow = new Color3f (Color.YELLOW);
 	Color3f green = new Color3f (Color.GREEN);
 	Color3f blue = new Color3f (Color.BLUE);
 	Color3f cyan = new Color3f (Color.CYAN);
 	Color3f purple = new Color3f (Color.MAGENTA);
 	Color3f black = new Color3f (Color.BLACK);
 	Color3f white = new Color3f (Color.WHITE);
 	Color3f gray = new Color3f (Color.GRAY);
 	Color3f lightgray = new Color3f (Color.LIGHT_GRAY);
 
 
 	
 	public SwingTest() {
 		init();
 	}
 	
 
 	
 	 protected void init() {
 		 VirtualUniverse universe = createVirtualUniverse();
 
 		 Locale locale = createLocale( universe );
 		 
 		 ViewPlatform vp = createViewPlatform();
 		 createView( vp );
 	
 		 BranchGroup sceneBranchGroup = createSceneBranchGroup();
 	
 		 Background background = createBackground();
 		 if( background != null )
 			  sceneBranchGroup.addChild( background );
 	
 		  BranchGroup viewBranchGroup = createViewBranchGroup(getViewTransformGroupArray(), vp);
 		  
 		  locale.addBranchGraph(sceneBranchGroup);
 		  addViewBranchGroup(locale, viewBranchGroup);
 	 }
 	
 
 	 
 	 protected void addCanvas3D(Canvas3D c3d)  {
 		 add( "Center", c3d );
 	 }
 	
 
 	 
 	 protected View createView(ViewPlatform vp) {
 	 	View view = new View();
 	
 	 	PhysicalBody pb = new PhysicalBody();
 	 	PhysicalEnvironment pe = new PhysicalEnvironment();
 	 	view.setPhysicalEnvironment( pe );
 	 	view.setPhysicalBody( pb );
 
 	 	view.attachViewPlatform( vp );
 	
 	 	view.setBackClipDistance( 500.0 );
 	 	view.setFrontClipDistance( 0.5 );
 	
 	 	c3d = createCanvas3D( false );
 	 	view.addCanvas3D(c3d);
 		
 	 	addCanvas3D( c3d );
 		c3d.addMouseListener(this);
 		c3d.addMouseMotionListener(this);
 		c3d.addKeyListener(this);
 		
 	 	return view;
 	 }
 	
 
 	 protected Background createBackground() {
 	 	Background background = new Background(white);
 		background.setApplicationBounds(createApplicationBounds());
 		
 		return background;
 	 }
 	
 
 	protected Bounds createApplicationBounds() {
 		return new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);
 	}
 	
 
 	 protected Canvas3D createCanvas3D(boolean offscreen) {
 		 GraphicsConfigTemplate3D gc3D = new GraphicsConfigTemplate3D();
 		 gc3D.setSceneAntialiasing( GraphicsConfigTemplate.PREFERRED );
 	
 		 GraphicsDevice gd[] = 
 				 GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
 	
 		 Canvas3D c3d = new Canvas3D(gd[0].getBestConfiguration(gc3D), offscreen);
 	
 		 return c3d;
 	 }
 	
 
 	 public TransformGroup[] getViewTransformGroupArray() {
 		 TransformGroup[] tgArray = new TransformGroup[1];
 		 tgArray[0] = new TransformGroup();
 		 tgArray[0].setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
 		 setTgArray(tgArray[0]);
 	
 		 Transform3D t3d = new Transform3D();
 		 t3d.setTranslation( new Vector3d( 0.0, 0.0, -10.0 ) );
 		 t3d.invert(); //moving the viewer, not the scene
 		 tgArray[0].setTransform( t3d );
 	
 		 return tgArray;
 	 }
 	
 
 	 
 	 protected void addViewBranchGroup( Locale locale, BranchGroup bg ) {
 		 locale.addBranchGraph( bg );
 	 }
 	
 
 	 protected Locale createLocale( VirtualUniverse u ) {
 		 return new Locale( u );
 	 }
 	
 
 	 protected ViewPlatform createViewPlatform() {
 		 ViewPlatform vp = new ViewPlatform();
 		 vp.setViewAttachPolicy( View.RELATIVE_TO_FIELD_OF_VIEW );
 		 vp.setActivationRadius( 100 );
 	
 		 return vp;
 	 }
 	
 
 	 protected BranchGroup createViewBranchGroup(TransformGroup[] tgArray, ViewPlatform vp) {
 		 BranchGroup vpBranchGroup = new BranchGroup();
 	
 		 if( tgArray != null && tgArray.length > 0 ) {
 			 Group parentGroup = vpBranchGroup;
 			 TransformGroup curTg = null;
 	
 			 for( int n = 0; n < tgArray.length; n++ ) {
 				 curTg = tgArray[n];
 				 parentGroup.addChild( curTg );
 				 parentGroup = curTg;
 			 }
 	
 			 tgArray[tgArray.length-1].addChild( vp );
 		 }
 		 else
 			 vpBranchGroup.addChild( vp );
 	
 		 return vpBranchGroup;
 	 }
 	
 	 
 	 protected VirtualUniverse createVirtualUniverse() {
 		 return new VirtualUniverse();
 	 }
 	
 
 
 	 protected BranchGroup createSceneBranchGroup() {
 		  BranchGroup objRoot = new BranchGroup();
 	
 		  TransformGroup objTrans = new TransformGroup();
 		  objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
 		  objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
 
 		 
 		  BoundingSphere bounds = new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);
 		 
 		  sceneBranchGroup = new BranchGroup();
 	
 		  sceneBranchGroup.setCapability( Group.ALLOW_CHILDREN_EXTEND );
 		  sceneBranchGroup.setCapability( Group.ALLOW_CHILDREN_READ );
 		  sceneBranchGroup.setCapability( Group.ALLOW_CHILDREN_WRITE );
 
 		  //ambient light
 		  AmbientLight aLgt = new AmbientLight( white );
 		  aLgt.setInfluencingBounds( bounds );
 		
 		  //directional light
 		  DirectionalLight lgt1 = new DirectionalLight(white, new Vector3f( -1.0f,-1.0f,-1.0f ) );
 		  lgt1.setInfluencingBounds( bounds );
 		
 		  objRoot.addChild(aLgt);
 		  objRoot.addChild(lgt1);
 		  
 		  //allows picking
 		  pickCanvas = new PickCanvas(c3d, sceneBranchGroup);
 		  pickCanvas.setMode(PickCanvas.GEOMETRY);
 		  
 		  objTrans.addChild( sceneBranchGroup );
 		  objRoot.addChild( objTrans );
 		  
 		  return objRoot;
 	 }
 
 
 	protected BranchGroup createPyramid() {
 		 BranchGroup bg = new BranchGroup();
 		 bg.setCapability( BranchGroup.ALLOW_DETACH ); //makes the shape deletable
 		 
 		 Pyramid pyramid = new Pyramid();
 		 
 		 bg.addChild(pyramid.createRotator());
 		 setShapeClicked(pyramid);
 		 
 		 return bg;
 	 }
 	 
 	 
 	 
 	 protected BranchGroup createHexPrism() {
 		 BranchGroup bg = new BranchGroup();
 		 bg.setCapability( BranchGroup.ALLOW_DETACH ); //makes the shape deletable
 		 
 		 HexagonalPrism hexPrism = new HexagonalPrism();
 		 
 		 bg.addChild(hexPrism.createRotator());
 		 setShapeClicked(hexPrism);
 		  
 		 return bg;
 	 }
 	 
 	 
 
 	 protected BranchGroup createRectPrism() {
 		 BranchGroup bg = new BranchGroup();
 		 bg.setCapability( BranchGroup.ALLOW_DETACH ); //makes the shape deletable
 		 
 		 RectangularPrism rectPrism = new RectangularPrism();
 		 
 		 bg.addChild(rectPrism.createRotator());
 		 setShapeClicked(rectPrism);
 		  
 		 return bg;
 	 }
 	 
 	 
 	 protected BranchGroup createTriPrism() {
 		 BranchGroup bg = new BranchGroup();
 		 bg.setCapability( BranchGroup.ALLOW_DETACH ); //makes the shape deletable
 		 
 		 TriangularPrism triPrism = new TriangularPrism();
 		 
 		 bg.addChild(triPrism.createRotator());
 		 setShapeClicked(triPrism);
 		  
 		 return bg;
 	 }
 	 
 	
 	 protected BranchGroup createSphere() {
 		 BranchGroup bg = new BranchGroup();
 		 bg.setCapability( BranchGroup.ALLOW_DETACH ); //makes the shape deletable
 		 
 		 aSphere sphere = new aSphere();		 
 		 bg.addChild(sphere.createRotator());
 		 
 		 setShapeClicked(sphere);
 
 		 return bg;
 	 }
 	
 	 
 	 //allows dynamic removal of the shape BranchGroups at runtime
 	 protected void removeShape(String name) {
 		      Enumeration e = sceneBranchGroup.getAllChildren();
 		      int index = 0;
 		      
 		      while (e.hasMoreElements() != false) {
 		    	  Object sgObject = ((SceneGraphObject) (e.nextElement()));
 		    	  
 		    	  if (((String)((Group)((Group)(((Group)sgObject).getChild(0))).getChild(0)).getChild(0).getUserData()).equalsIgnoreCase(selectedUserData)) {
 		    		  sceneBranchGroup.removeChild(index);
 		    		  System.out.println("Removed: " + selectedUserData);
 		    	  }
 		    	  else
 		    	  	  index++;
 		      }		      
 	 }
 
 	 
 /***********************************************************************/
 /************************** ACCESSORS/MUTATORS *************************/
 /***********************************************************************/	
 	public static BranchGroup getSceneBranchGroup() {
 		return sceneBranchGroup;
 	}
 
 	public void setSceneBranchGroup(BranchGroup sceneBranchGroup) {
 		this.sceneBranchGroup = sceneBranchGroup;
 	}
 	
 	public static Canvas3D getC3d() {
 		return c3d;
 	}
 
 
 	public void setC3d(Canvas3D c3d) {
 		this.c3d = c3d;
 	}
 	
 	
 	public static int getRectPrismCount() {
 		return rectPrismCount;
 	}
 
 
 	public static void setRectPrismCount(int rectPrismCount2) {
 		rectPrismCount = rectPrismCount2;
 	}
 	
 	
 	public static int getPyramidCount() {
 		return pyramidCount;
 	}
 
 
 	public static void setPyramidCount(int pyramidCount2) {
 		pyramidCount = pyramidCount2;
 	}
 	
 	
 	public static int getTriPrismCount() {
 		return triPrismCount;
 	}
 
 
 	public static void setTriPrismCount(int triPrismCount2) {
 		triPrismCount = triPrismCount2;
 	}
 	
 	
 	public static int getSphereCount() {
 		return sphereCount;
 	}
 
 
 	public static void setSphereCount(int sphereCount2) {
 		sphereCount = sphereCount2;
 	}
 	
 	
 	public static int getHexPrismCount() {
 		return hexPrismCount;
 	}
 
 
 	public static void setHexPrismCount(int hexPrismCount2) {
 		hexPrismCount = hexPrismCount2;
 	}
 	
 	
 	public String getSelectedUserData() {
 		return selectedUserData;
 	}
 
 
 	public void setSelectedUserData(String selectedUserData) {
 		this.selectedUserData = selectedUserData;
 	}
 
 
 	public Node getShapeClicked() {
 		return shapeClicked;
 	}
 
 
 	public void setShapeClicked(Node shapeClicked) {
 		this.shapeClicked = shapeClicked;
 	}
 	
 	
 	public TransformGroup getTgArray() {
 		return tgArray;
 	}
 
 
 	public void setTgArray(TransformGroup tgArray2) {
 		this.tgArray = tgArray2;
 	}
 	
 	
 	/*************** Mouse cursor ****************/
 	public String getCurPos() {
 		return curPos;
 	}
 
 	public void setCurPos(String curPos) {
 		this.curPos = curPos;
 	}
 
 	public int getMouseX() {
 		return mouseX;
 	}
 
 	public void setMouseX(int mouseX) {
 		this.mouseX = mouseX;
 	}
 
 	public int getMouseY() {
 		return mouseY;
 	}
 
 	public void setMouseY(int mouseY) {
 		this.mouseY = mouseY;
 	}
 	
 	
 
 /***********************************************************************/
 /************************* MOUSE/KEY LISTENERS *************************/
 /***********************************************************************/
 	public void mouseClicked(MouseEvent e) { 
 	    PickResult result = pickCanvas.pickClosest();
 	    shapeClicked = result.getNode(PickResult.SHAPE3D);
     	System.out.println("Shape clicked: " + shapeClicked.getUserData());
 	}
 	
 
 	public void mousePressed(MouseEvent e) {
 		mouseX = e.getX();
 		mouseY = e.getY();
 
 		//System.out.println("Mouse pressed at x=" + mouseX + ", y=" + mouseY);
 		mouseButton = e.getButton();
 
 	    pickCanvas.setShapeLocation(e);
 	    PickResult result = pickCanvas.pickClosest();
 	    
 	    //System.out.println(result.getClass());
 	
 	    if (result == null) {
 	       System.out.println("Nothing picked");
 	    } 
 	    
 	    
 	    else {
 	    	shapeToDrag = result.getNode(PickResult.SHAPE3D);
 	    	
 	    	//System.out.println(shapeToDrag.getClass().getName());
 
 	    	String data = (String)result.getNode(PickResult.SHAPE3D).getUserData();
 	    	//System.out.println("Repositioning: " + data);
 	    		
 	    	setSelectedUserData(data);
 	    }
 	    
 	    
 		if (shapeToDrag.getClass().getName().equals("TriangularPrism")) {
 			tx = ((TriangularPrism) shapeToDrag).getTx();
 			ty = ((TriangularPrism) shapeToDrag).getTy();
 	 	}
 	 	
 	 	else if (shapeToDrag.getClass().getName().equals("HexagonalPrism")) {
 			tx = ((HexagonalPrism) shapeToDrag).getTx();
 			ty = ((HexagonalPrism) shapeToDrag).getTy();
 	 	}
 	 	
 	 	else if (shapeToDrag.getClass().getName().equals("Pyramid")) {
 			tx = ((Pyramid) shapeToDrag).getTx();
 			ty = ((Pyramid) shapeToDrag).getTy();
 	 	}
 	 	
 	 	else if (shapeToDrag.getClass().getName().equals("RectangularPrism")) {
 			tx = ((RectangularPrism) shapeToDrag).getTx();
 			ty = ((RectangularPrism) shapeToDrag).getTy();
 	 	}
 	 	
 	 	else if (shapeToDrag.getClass().getName().equals("aSphere")) {
 	 		//System.out.println(shapeToDrag.getClass());
 			tx = ((aSphere) shapeToDrag).getTx();
 			ty = ((aSphere) shapeToDrag).getTy();
 	 	}
 	 	
 	 	//else if (shapeToDrag.getClass().getName().equals("Cylinder")) {
 			//tx = ((Cylinder) shapeToDrag).getTx();
 			//ty = ((Cylinder) shapeToDrag).getTy();
 	 	//}
 	}
 	
	
	public void mouseMoved(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		
		mouseX = x;
		mouseY = y;

		setMouseX(x);
		setMouseY(y);

		setCurPos("(" + mouseX + "," + mouseY + ")");
		
	}
	
 
 	public void mouseDragged(MouseEvent e) {
 		if (shapeToDrag == null)
 			return;
 
 		int x = e.getX();
 		int y = e.getY();
 		
 		if (shapeToDrag.getClass().getName().equals("TriangularPrism")) {
 			tx = ((TriangularPrism) shapeToDrag).getTx();
 			ty = ((TriangularPrism) shapeToDrag).getTy();
 	 	}
 	 	
 	 	else if (shapeToDrag.getClass().getName().equals("HexagonalPrism")) {
 			tx = ((HexagonalPrism) shapeToDrag).getTx();
 			ty = ((HexagonalPrism) shapeToDrag).getTy();
 	 	}
 	 	
 	 	else if (shapeToDrag.getClass().getName().equals("Pyramid")) {
 			tx = ((Pyramid) shapeToDrag).getTx();
 			ty = ((Pyramid) shapeToDrag).getTy();
 	 	}
 	 	
 	 	else if (shapeToDrag.getClass().getName().equals("RectangularPrism")) {
 			tx = ((RectangularPrism) shapeToDrag).getTx();
 			ty = ((RectangularPrism) shapeToDrag).getTy();
 	 	}
 	 	
 	 	else if (shapeToDrag.getClass().getName().equals("aSphere")) {
 	 		//System.out.println("LOOOOOOOL");
 			tx = ((aSphere) shapeToDrag).getTx();
 			ty = ((aSphere) shapeToDrag).getTy();
 	 	}
 	 	
 	 	//else if (shapeToDrag.getClass().getName().equals("Cylinder")) {
 			//tx = ((Cylinder) shapeToDrag).getTx();
 			//ty = ((Cylinder) shapeToDrag).getTy();
 	 	//}
 		
 
 		if (mouseButton == MouseEvent.BUTTON1) {
 			tx += (x - mouseX) * 0.02f;
 			ty -= (y - mouseY) * 0.02f;
 			
 		 	if (shapeToDrag.getClass().getName().equals("TriangularPrism")) {
 				((TriangularPrism) shapeToDrag).setTx(tx);
 				((TriangularPrism) shapeToDrag).setTy(ty);
 		 	}
 		 	
 		 	else if (shapeToDrag.getClass().getName().equals("HexagonalPrism")) {
 				((HexagonalPrism) shapeToDrag).setTx(tx);
 				((HexagonalPrism) shapeToDrag).setTy(ty);
 		 	}
 		 	
 		 	else if (shapeToDrag.getClass().getName().equals("Pyramid")) {
 				((Pyramid) shapeToDrag).setTx(tx);
 				((Pyramid) shapeToDrag).setTy(ty);
 		 	}
 		 	
 		 	else if (shapeToDrag.getClass().getName().equals("RectangularPrism")) {
 				((RectangularPrism) shapeToDrag).setTx(tx);
 				((RectangularPrism) shapeToDrag).setTy(ty);
 		 	}
 		 	
 		 	else if (shapeToDrag.getClass().getName().equals("aSphere")) {
 		 		((aSphere) shapeToDrag).setTx(tx);
 		 		((aSphere) shapeToDrag).setTy(ty);
 		 	}
 		 	
 		 	//else if (shapeToDrag.getClass().getName().equals("Cylinder")) {
 		 		//((Pyramid) shapeToDrag).setTx(tx);
 		 		//((Pyramid) shapeToDrag).setTy(ty);
 		 	//}
 		}
 		
 		
 	 	Transform3D dragShape = new Transform3D();
 	 	dragShape.setTranslation(new Vector3f (tx, ty, 0.0f ));
 	 	
 
 	 	
 	 	if (shapeToDrag.getClass().getName().equals("TriangularPrism")) {
 		 	dragShape.mul(((TriangularPrism) shapeToDrag).getResize());
 		 	((TriangularPrism) shapeToDrag).getTg().setTransform(dragShape);
 	 	}
 	 	
 	 	else if (shapeToDrag.getClass().getName().equals("HexagonalPrism")) {
 		 	dragShape.mul(((HexagonalPrism) shapeToDrag).getResize());
 		 	((HexagonalPrism) shapeToDrag).getTg().setTransform(dragShape);
 		}
 	 	
 	 	else if (shapeToDrag.getClass().getName().equals("Pyramid")) {
 		 	dragShape.mul(((Pyramid) shapeToDrag).getResize());
 		 	((Pyramid) shapeToDrag).getTg().setTransform(dragShape);
 	 	}
 	 	
 	 	else if (shapeToDrag.getClass().getName().equals("RectangularPrism")) {
 		 	dragShape.mul(((RectangularPrism) shapeToDrag).getResize());
 		 	((RectangularPrism) shapeToDrag).getTg().setTransform(dragShape);
 	 	}
 	 	
 	 	else if (shapeToDrag.getClass().getName().equals("aSphere")) {
 	 		dragShape.mul(((aSphere) shapeToDrag).getResize());
 	 		((aSphere) shapeToDrag).getTg().setTransform(dragShape);
 	 	}
 	 	
 	 	//else if (shapeToDrag.getClass().getName().equals("Cylinder")) {
 	 	//	((Cylinder) shapeToDrag).getTg().setTransform(dragShape);
 	 	//}
 		
 
 		mouseX = x;
 		mouseY = y;
 
 		setMouseX(x);
 		setMouseY(y);
 
 		setCurPos("(" + mouseX + "," + mouseY + ")");
 		
 		//System.out.println("(" + mouseX + "," + mouseY + ")");
 	
 	}
 	
 	
 	public void mouseReleased(MouseEvent e) { 
 		
 	 	if (shapeToDrag.getClass().getName().equals("TriangularPrism")) {
 	 		((TriangularPrism) shapeToDrag).setTx(tx);
 	 		((TriangularPrism) shapeToDrag).setTy(ty);
 	 	}
 	 	
 	 	else if (shapeToDrag.getClass().getName().equals("HexagonalPrism")) {
 	 		((HexagonalPrism) shapeToDrag).setTx(tx);
 	 		((HexagonalPrism) shapeToDrag).setTy(ty);
 	 	}
 	 	
 	 	else if (shapeToDrag.getClass().getName().equals("Pyramid")) {
 	 		//System.out.println("That's a pyramid");
 	 		((Pyramid) shapeToDrag).setTx(tx);
 	 		((Pyramid) shapeToDrag).setTy(ty);
 	 	}
 	 	
 	 	else if (shapeToDrag.getClass().getName().equals("RectangularPrism")) {
 	 		((RectangularPrism) shapeToDrag).setTx(tx);
 	 		((RectangularPrism) shapeToDrag).setTy(ty);
 	 	}
 	 	
 	 	else if (shapeToDrag.getClass().getName().equals("aSphere")) {
  			((aSphere) shapeToDrag).setTx(tx);
  			((aSphere) shapeToDrag).setTy(ty);
 	 	}
 	 	
 	 	//else if (shapeToDrag.getClass().getName().equals("Cylinder")) {
  			//((Cylinder) shapeToDrag).setTx(tx);
  			//((Cylinder) shapeToDrag).setTy(ty);
 	 	//}
 	 	
 		shapeToDrag = null;
 	}
 		
 		
 	public void keyPressed(KeyEvent e) {
 		if (e.getKeyCode() == KeyEvent.VK_DELETE)
 			removeShape(getSelectedUserData());
 	}
 
 	
 	/* unused */
 	public void keyReleased(KeyEvent e) { }
 	public void keyTyped(KeyEvent e) { }
 	public void mouseEntered(MouseEvent e) { }
 	public void mouseExited(MouseEvent e) { }
 }
