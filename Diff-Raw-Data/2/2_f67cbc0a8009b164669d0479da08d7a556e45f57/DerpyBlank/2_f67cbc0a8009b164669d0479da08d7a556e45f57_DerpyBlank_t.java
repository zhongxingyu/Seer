 package DerpyAI;
 
 import java.awt.Point;
 
 public class DerpyBlank extends DerpyPiece{
 
 	public DerpyBlank(Point p) {
		super(true,"X");
 		currentLocation = p;
 		xMoveConstraint = 8;
 		yMoveConstraint = 8;
 	}
 	
 }
