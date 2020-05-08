 package DerpyAI;
 
 import java.awt.Point;
 
 public class DerpyBlank extends DerpyPiece{
 
 	public DerpyBlank(Point p) {
		super(true,"WX");
 		currentLocation = p;
 		xMoveConstraint = 8;
 		yMoveConstraint = 8;
 	}
 	
 }
