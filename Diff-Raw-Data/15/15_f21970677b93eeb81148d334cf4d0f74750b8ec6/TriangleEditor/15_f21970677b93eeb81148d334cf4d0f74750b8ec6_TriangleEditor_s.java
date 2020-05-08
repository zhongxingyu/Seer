 package triangleeditor;
 
 import org.jbox2d.dynamics.Body;
 
 import processing.core.*;
 import triangleeditor.physics.PhysicsController;
 
 
 public class TriangleEditor extends PApplet {
 	float				_elapsedFrames;
 	GridModel			_gridModel;
 	PhysicsController	_physicsController;
 	
 	public void setup() {
 		_elapsedFrames = 0;
 		
 		size(1200, 600, P3D);
 		frameRate(60);
 		background(0);
 		setupGrid();
 		setupPhysicsController();
 	}
 	
 	public void setupGrid() {
 		_gridModel = new GridModel(width, height, 50, this);
 	}
 	
 	public void setupPhysicsController() {
 		_physicsController = new PhysicsController( this );
 		_physicsController.createWorld(width, height, 0, 20, width*2, height*2, width, height, 20);
 		
 		_physicsController.m_density = 1;
 		_physicsController.m_restitution = 0.5f;
 //		for( int i = 0; i < 500; ++i ) {
 //			_physicsController.createCircle(random(width), 10, random(5, 20));
 //		}
 	}
 	
 	public void draw() {
 		++_elapsedFrames;
 		background( 255 );
 		noStroke();
 		drawGrid();
 		
 		_physicsController.update();
 		_physicsController.draw();
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
 			triangle.rotate(90);
 		}
 		
 		triangle.set_isActive( true );
 		
 		if(triangle.get_body() == null ) {
 			PVector[] trianglePoints = triangle.getPoints( true );
 			Body triangleBody = _physicsController.createPolygon(trianglePoints[0].x, trianglePoints[0].y, 
 					trianglePoints[1].x, trianglePoints[1].y,
 					trianglePoints[2].x, trianglePoints[2].y);
 			
 			triangle.set_body( triangleBody );
 		}
 	}
 
 	//
 	public static void main(String args[]) {
 		PApplet.main(new String[] { "--present", "triangleeditor.TriangleEditor" });
 	}
 }
