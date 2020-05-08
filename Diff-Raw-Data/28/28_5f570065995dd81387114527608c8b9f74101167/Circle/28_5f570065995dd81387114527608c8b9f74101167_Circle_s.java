 package nehsics;
 import nehsics.math.*;
 import static nehsics.math.Util.*;
 import java.awt.geom.Ellipse2D;
 
 public class Circle extends Body {
 	
 	/**
 	 * @param r radius of the circle in meters
 	 * @param mass mass of the circle in kg
 	 */
 	public Circle(double r, double mass) {
 		super(v(0), v(0), mass, new Ellipse2D.Double(0,0,r*2,r*2));
 		setRadius(r);
 	}
 
 	public boolean intersects(Circle c) {
 		if(!alreadyHit.contains(c) && distance(position, c.getPosition()) <= radius+c.radius) {
 			alreadyHit.add(c);
 			c.alreadyHit.add(this);
 			return true;
 		}
 		return false;
 	}
 
 	public void hit(Body b) {
 		double m1 = b.getMass();
 		Vector2d p1 = b.getPosition();
 		Vector2d v1 = b.getVelocity();
 		double m2 = getMass();
 		Vector2d p2 = getPosition();
 		Vector2d v2 = getVelocity();
//		if(!testDirection(p1,v1,p2,v2))
//			return;
 		double theta = 0;
 		try {
 			theta = Math.atan((p1.getY()-p2.getY())/((p1.getX()-p2.getX())));
 		} catch(ArithmeticException e) {
 			theta = Math.PI/2.0;
 		}
 		theta += Math.PI;
 		theta *= -1;
 		Vector2d cv1 = new Vector2d(cos(theta)*v1.getX() - sin(theta)*v1.getY(), sin(theta)*v1.getX() + cos(theta)*v1.getY());
 		Vector2d cv2 = new Vector2d(cos(theta)*v2.getX() - sin(theta)*v2.getY(), sin(theta)*v2.getX() + cos(theta)*v2.getY());
 		double p = m1*cv1.getX() + m2*cv2.getX();
 		cv1 = v((m1*cv1.getX() + (2*cv2.getX()-cv1.getX())*m2)/(m1+m2), cv1.getY());
 		cv2 = v(((p-m1*cv1.getX())/m2), cv2.getY());
 		b.setVelocity(new Vector2d(cos(-theta)*cv1.getX() - sin(-theta)*cv1.getY(), sin(-theta)*cv1.getX() + cos(-theta)*cv1.getY()));
 		setVelocity(new Vector2d(cos(-theta)*cv2.getX() - sin(-theta)*cv2.getY(), sin(-theta)*cv2.getX() + cos(-theta)*cv2.getY()));
 	}
 	public boolean testDirection(Vector2d p1, Vector2d v1, Vector2d p2, Vector2d v2)
 	{
 		double distance = distance(p1,p2); 
		Vector2d p1f = add(p1,v1);
		Vector2d p2f = add(p2,v2); 
 		double distance2 = distance(p1f,p2f); 
		return distance2<=distance;
 	}
 }
