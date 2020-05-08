 import javax.vecmath.Point3d;
 
 public class CheckBoardShad extends Shader {
 	Color color1, color2;
 	private double checkBoardSize;
 	
 	public CheckBoardShad(Point3d origin, Color color1, Color color2, double checkBoardSize) {
 		super(origin);
 		this.color1 = color1;
 		this.color2 = color2;
 		this.checkBoardSize = checkBoardSize;
 	}
 	
 	public Color shade(Point3d point) {
 		Color result = new Color(0,0,0);
 		double row = Math.floor(origin.z - point.z / checkBoardSize) + 2;
 		double col = Math.floor(point.x - origin.x / checkBoardSize) + 2;
 		if( row < 0 )
 			row *= -1;
 		if( col < 0 )
 			col *= -1;
 		//if both row and column are either even or odd. Make color red
 		if (row % 2 == 0 && col % 2 == 0 || row % 2 == 1 && col % 2 == 1)
 			result = color1;
 		//else either the row or column are odd with the other even. Make color yellow
 		else if(row % 2 == 0 && col % 2 == 1 || row % 2 == 1 && col % 2 == 0) 
 			result = color2;
 		return result;
 	}
 }
