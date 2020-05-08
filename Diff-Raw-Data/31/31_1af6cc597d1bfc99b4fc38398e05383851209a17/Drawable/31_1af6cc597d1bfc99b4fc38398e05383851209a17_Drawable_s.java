 package Crane;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.NoninvertibleTransformException;
 import java.awt.geom.Point2D;
 
 import javax.swing.JComponent;
 
 public class Drawable extends JComponent {
 
 	private static final long serialVersionUID = 1L;
 	
 	int x, y;
 	double angle;
 	Rectangle rect;
 	Drawable parent = null;
 	AffineTransform ai;
 	Color fillColor;
 	Point old, current;
 
 	public Drawable(int x, int y, double angle, Drawable parent, Color fill){
 		this.x = x;
 		this.y = y;
 		this.angle = angle;
 		this.parent = parent;
 		ai = new AffineTransform();
 		ai.translate(x, y);
 		ai.rotate(angle);
 		this.fillColor = fill;
 		rect = new Rectangle(-25,-25,50,100);
 	}
 	
 	public void paintComponent(Graphics g) {
 		Graphics2D g2 = (Graphics2D) g;
 		g2.setColor(fillColor);
 		
 		AffineTransform aiCurr = g2.getTransform(); // to recover at the end
 		g2.transform(getTransform()); // offset
 		g2.fill(rect);
 		g2.setTransform(aiCurr); // set to
 	}
 	
 	public AffineTransform getTransform(){
 		//AffineTransform aiChild = ai;
 		AffineTransform aiParent;
 		if (parent != null){
 			aiParent = new AffineTransform(parent.getTransform()); // recursive
 			aiParent.concatenate(ai);
 		} else {
 			aiParent = ai;
 		}
 		return aiParent;
 	}
 	
	Point2D getPointInverse(Point p){
 		AffineTransform temp = new AffineTransform(getTransform()); // copy cxr
 		try {
 			temp = temp.createInverse();
 		} catch (NoninvertibleTransformException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		Point2D d = new Point2D.Double();
 		temp.transform(p, d);
 		return d;
 	}
	public boolean isInside(Point p){
 		Point2D d = getPointInverse(p);
		
 		if (d.getX() > -25 && d.getX() < 25 && d.getY() > -25 && d.getY() < 75 ){
 			System.out.print("yay");
 			return true;
 		}
 		else{
			System.out.print("nay");
 			return false;
 		}

 	}
	public void moveArm(Point p) {
 		Point2D d = getPointInverse(p);
 		double angle = Math.atan2(d.getY(), d.getX());
		System.out.println("The angle before is "+ this.angle);
		//this.angle += angle;
		System.out.println("The angle after is "+ this.angle);
		ai.rotate(angle);
 	}
 }
