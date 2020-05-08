 import java.util.*;
 import java.util.concurrent.*;
 
 public class Ball {
 	public Vektor position;
 	public Vektor speed;
 	public Vektor acceleration;
 	public double physicalRadius;
 	public double visibleRadius;
 	public double mass = 1;
 	public Color color; // TODO: should be in another class
 	private List<Joint> joints;
 
 	public Ball(Vektor position, double radius) {
 		this.position = position;
 		this.physicalRadius = radius;
		this.visibleRadius = radius-4;
 		speed = new Vektor();
 		acceleration = new Vektor();
 		color = Color.random();
 		joints = new ArrayList<Joint>();
 	}
 
 	/* position and movement below here */
 	
 	public void accelerate(Vektor v) {
 		acceleration = acceleration.add(v);
 	}
 
 	public boolean isHit(Vektor v) {
 		double distance = v.sub(position).getLength();
 		return (distance <= physicalRadius);
 	}
 
 	public Vektor distanceTo(Vektor v) {
 		return position.sub(v);
 	}
 
 	/* joint stuff below here */
 
 	public void addJoint(Joint joint) {
 		joints.add(joint);
 	}
 
 	public void removeJoint(Joint j) {
 		joints.remove(j);
 	}
 
 	public boolean isJointWith(Ball b) {
 		for (Joint j : joints) {
 			if (j.opposite(this) == b)
 				return true;
 		}
 		return false;
 	}
 
 	public List<Joint> jointsWith(Ball b) {
 		List<Joint> withB = new CopyOnWriteArrayList<Joint>();
 		for (Joint j : joints) {
 			if (j.a == b || j.b == b)
 				withB.add(j);
 		}
 
 		return withB;
 	}
 
 	public void resetJoints() {
 		joints = new ArrayList<Joint>();
 	}
 
 	public List<Ball> jointBalls() {
 		List<Ball> list = new CopyOnWriteArrayList<Ball>();
 		for (Joint j : this.joints) {
 			list.add(j.opposite(this));
 		}
 		return list;
 	}
 
 	public boolean equals(Ball b) {
 		return this.color.equals(b.color);
 	}
 	
 	/* getter/setter below here */
 	
 	public double getX() {
 		return position.getX();
 	}
 
 	public double getY() {
 		return position.getY();
 	}
 
 	public void setX(double x) {
 		position.setX(x);
 	}
 
 	public void setY(double y) {
 		position.setY(y);
 	}
 
 	public List<Joint> getJoints() {
 		return joints;
 	}
 
 }
