 package triangleeditor;
 
 import java.util.ArrayList;
 
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 
 import processing.core.*;
 import triangleeditor.physics.PhysicsController;
 
 
 public class TriangleEditor extends PApplet {
 	private static final long serialVersionUID = -3824555102005090780L;
 	
 	float				_elapsedFrames;
 	GridModel			_gridModel;
 	PhysicsController	_physicsController;
 	ArrayList<Body>		_circles = new ArrayList<Body>();
 	
 	public void setup() {
 		_elapsedFrames = 0;
 		
 		size(1200, 600, P3D);
 		frameRate(60);
 		background(0);
 		
 		setupGrid();
 		setupPhysicsController();
 	}
 	
 	public void setupGrid() {
 		_gridModel = new GridModel(width, height, 25, this);
 	}
 	
 	public void setupPhysicsController() {
 		_physicsController = new PhysicsController( this );
 		_physicsController.createWorld(width, height, 0, 20, width*2, height*2, width, height, 20);
 //		_physicsController.createHollowBox(width * 0.5f, height*.5f, width, height, 10.0f);
 		
 		_physicsController.m_density = 1;
 		_physicsController.m_restitution = 0.5f;
 		for( int i = 0; i < 500; ++i ) {
 			Body circle = _physicsController.createCircle(random(width), 10, random(2, 8));
 			_circles.add( circle );
 		}
 	}
 	
 	@SuppressWarnings("unused")
 	public void draw() {
 		++_elapsedFrames;
 		background( 255 );
 		noStroke();
 		drawGrid();
 		
 		_physicsController.update();
 		_physicsController.draw();
 		
 		// Reset circles
 		for( Body body : _circles ) {
 			Vec2 pos = _physicsController.worldToScreen( body.getPosition() );
 			if( pos.y > height ) {
 				Vec2 randomPosition = _physicsController.screenToWorld( random(width), 0);
 				body.setXForm( randomPosition, 0);
 				body.setLinearVelocity( new Vec2(0.0f, 0.0f) );
 			}
 		}
 	}
 	
 	
 	/**
 	 * Calls draw on all GridSquares
 	 */
 	public void drawGrid() {
 		for (GridSquare square : _gridModel.get_gridSquareList() ) {
 			square.draw();
 		}
 	}
 	
 	
 	
 	/* (non-Javadoc)
 	 * @see processing.core.PApplet#mouseDragged()
 	 */
 	@Override
 	public void mouseDragged() {
 		super.mouseDragged();
 		handleSquares();
 	}
 
 	/* (non-Javadoc)
 	 * @see processing.core.PApplet#mouseMoved()
 	 */
 	@Override
 	public void mouseMoved() {
 		super.mouseMoved();
 	}
 
 	/* (non-Javadoc)
 	 * @see processing.core.PApplet#mousePressed()
 	 */
 	@Override
 	public void mousePressed() {
 		super.mousePressed();
 	}
 	
 	/* (non-Javadoc)
 	 * @see processing.core.PApplet#mouseReleased()
 	 */
 	@Override
 	public void mouseReleased() {
 		super.mouseReleased();
 		handleSquares();
 	}
 	
 	private void handleSquares(){
 		GridSquare square = _gridModel.getSquareAtPosition( mouseX, mouseY );
 		if( square == null ) return; // No square at location
 				
 		GridTriangle triangle = square.getTriangle( mouseX, mouseY );
 		if(triangle == null)  return; // No triangle at location
 		
 		
 		// Already was active - rotate
 		if(triangle.get_isActive()) {
 			
 		}
 		
 		triangle.set_isActive( true );
 		
 		// If the triangle already has a body, destroy it
 		if(triangle.get_body() != null ) {
 			_physicsController.get_world().destroyBody( triangle.get_body() );
 			triangle.rotate( 90 );
 		}
 		
 		
 		// Create a triangle based on the CCW points of the triangle
 		ArrayList<PVector> trianglePoints = triangle.getPoints( true );
 		_physicsController.m_density = 0;
 		_physicsController.m_friction = 1;
 		_physicsController.m_restitution = 0.8f;
 		
 		Body triangleBody = _physicsController.createPolygon(trianglePoints.get(0).x, trianglePoints.get(0).y, 
				trianglePoints.get(1).x, trianglePoints.get(1).y,
				trianglePoints.get(2).x, trianglePoints.get(2).y);
 		
 		triangle.set_body( triangleBody );
		
		print("Creating triangle:");
		print(triangleBody);
		print("\n");
		
 	}
 
 	//
 	public static void main(String args[]) {
 		PApplet.main(new String[] { "--present", "triangleeditor.TriangleEditor" });
 	}
 }
