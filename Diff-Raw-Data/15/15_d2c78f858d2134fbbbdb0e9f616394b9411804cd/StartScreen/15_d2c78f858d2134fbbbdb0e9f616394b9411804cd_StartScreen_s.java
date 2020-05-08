 package src;
 
 import java.util.Vector;
 
 import engine.helper.MathHelper;
 import engine.physics.AdditionalPhysicInformation;
 import engine.physics.PhysicsEngine;
 import engine.physics.Vector2D;
 import engine.physics.objects.EquilateralPolygon;
 import engine.physics.objects.Square;
 import engine.physics.objects.EquilateralTriangle;
 import engine.render.Color;
 import engine.render.Control2D;
 import engine.render.Dimension;
 import engine.render.EquilateralTriangle2D;
 import engine.render.Point2D;
 import engine.render.RenderEngine;
 import engine.render.Screen;
 import engine.render.controls.Label2D;
 
 public class StartScreen extends Screen {
 	
 	private Point2D square1_start = new Point2D(399, RenderEngine.getScreenDimension().height);
 	private Point2D square2_start = new Point2D(300, RenderEngine.getScreenDimension().height);
 	//private Point2D triangle1_start = new Point2D(250, RenderEngine.getScreenDimension().height-150f);
	private AdditionalPhysicInformation polygon1_start = new AdditionalPhysicInformation(new Point2D(200, RenderEngine.getScreenDimension().height-50f), 100f);
 //	private Point2D square1_start = new Point2D(350, 150);
 	
 	private Square square1 = new Square(100.0f, Color.getFromRGB(0, 0, 255));
 	private Square square2 = new Square(70.0f, Color.getFromRGB(255, 0, 0));
 	//private EquilateralTriangle triangle1 = new EquilateralTriangle(80.0f,  Color.getFromRGB(255, 0, 255));
	private EquilateralPolygon polygon1 = new EquilateralPolygon(40, 60f, 0, Color.getFromRGB(0, 100, 140), true);
 	
 	
 	private FontDefault font = new FontDefault(20.0f, Color.getFromRGB(255, 0, 0));
 	private Label2D fps = new Label2D(new Point2D(0, RenderEngine.getScreenDimension().height), "fps; ", font);
 	
 	private long cTime = 0;
 	
 	@Override
 	public void addControls2D(Vector<Control2D> controls) {
 		controls.add(fps);
 	}
 	
 	@Override
 	public void init() {
 		PhysicsEngine.removeAllPhysicalObjects();
 		Dimension screen = RenderEngine.getScreenDimension();
 		PhysicsEngine.setClippingPane(new Point2D(screen.width/2, screen.height/2), screen.height);
 		
 		PhysicsEngine.addEarthGravitation(3f);
 		
 		PhysicsEngine.addPhysicalObject(square1, square1_start);
 		PhysicsEngine.addPhysicalObject(square2, square2_start);
 		//PhysicsEngine.addPhysicalObject(triangle1, triangle1_start);
 		PhysicsEngine.addPhysicalObject(polygon1, polygon1_start);
 		
 		
 		Square sq2 = new Square(100, Color.getFromRGB(50, 250, 50), false);
 		AdditionalPhysicInformation infoSq2 = new AdditionalPhysicInformation(new Point2D(300, 200), 20);
 		PhysicsEngine.addPhysicalObject(sq2, infoSq2);
 	}
 	
 	@Override
 	public void doLogic(int deltaTime) {
 		fps.setText("fps: "+RenderDisplay.instance().getFPS());
 		
 		if(deltaTime > 0) {
 			cTime += 1;
 		}
 		if(cTime == 1000) {
 			//RenderDisplay.instance().setScreen(null);
 			//PhysicsEngine.addPhysicalObject(new Square(100, Color.getFromRGB(50, 250, 50), false), new Point2D(300, 200));
 		}
 		PhysicsEngine.doLogic(deltaTime);
 	}
 	
 	@Override
 	public void postdraw2D(int deltaTime) {
 		PhysicsEngine.draw(deltaTime);
 	}
 	
 	@Override
 	public void onKeyDown(int key, String keyname) {
 		if(keyname == null) {
 			return;
 		}
 		if(keyname.equals("ESCAPE")) {
 			RenderDisplay.instance().setScreen(null);
 		} else if(keyname.equals("SPACE")) {
 			PhysicsEngine.getAdditionalPhysicInformation(square1).setForceVector(new Vector2D(0, 0));
 			PhysicsEngine.getAdditionalPhysicInformation(square1).setPosition(square1_start);
 		}
 	}
 }
