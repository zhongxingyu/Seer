 package mountain;
 
 public class Side{
 	private Point p1, p2, m;
 	
 	public Side(Point p1, Point p2, Point m){
 		this.p1 = p1;
 		this.p2 = p2;
 		this.m = m;
 	}
 	
 	public Point getP1(){
 		return p1;
 	}
 	public Point getP2(){
 		return p2;
 	}
 	public Point getM(){
 		return m;
 	}
 	public boolean equalPoints(Point p1, Point p2){
 		return 
			p1.getX() == this.p1.getX() &&
			p1.getY() == this.p1.getY() &&
			p2.getX() == this.p2.getX() &&
			p2.getY() == this.p2.getY();
 	}
 }
