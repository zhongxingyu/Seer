 package backend.environment;
 
 import java.util.ArrayList;
 
 import math.Vec;
 
 public class Prey extends Element implements Cloneable {
 	public @Property Vec position;
 	public @Property Vec velocity;
 	public @Property double size;
 	
 	public Prey(Vec position, Vec velocity, double size)	{
 		this.position = new Vec(position);
 		this.velocity = new Vec(velocity);
 		this.size = size;
 	}
 	
 	public Prey()
 	{
 		position = new Vec(0,0);
 		velocity = new Vec(0,0);
		size = 10;
 	}
 	
 	public Prey(double x, double y, double xvel, double yvel, double size)
 	{
 		position = new Vec(x,y);
 		velocity = new Vec(0,0);
 		this.size = size;
 	}
 	public double getSize() {
 		return size;
 	}
 
 	public Vec getPosition() {
 		return position;
 	}
 
 	public Vec getVelocity() {
 		return velocity;
 	}
 
 	public void update(ArrayList<Element> influences) {
 		for(Element e : influences)	{
 			Vec dir = e.getPosition().minus(getPosition());
 			
 		}
 	}
 
 	public RenderObject getROb() {
 		return null;
 	}
 
 	public Object clone()	{
 		return new Prey(position, velocity, size);
 	}
 	
 }
