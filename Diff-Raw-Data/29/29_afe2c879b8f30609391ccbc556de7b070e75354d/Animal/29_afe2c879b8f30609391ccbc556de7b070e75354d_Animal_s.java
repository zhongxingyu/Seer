 package backend.environment;
 
 
 import java.util.List;
 
 import math.Vec;
 
 public abstract class Animal extends Element implements Cloneable {
 	public @Property Vec velocity;	//this stores a vector that is at most a unit vector which is multiplied by the maxSpeed 
 	private Vec oldVelocity = null;
 	public @Property double size;
 	public @Property double maxSpeed;
 	public @Property double sightRadius;
 	
 	public Animal(Vec position, Vec velocity)	{
 		this.position = new Vec(position);
 		this.velocity = new Vec(velocity);
 	}
 	
 	public Animal(Animal other)	{
 		this.position = new Vec(other.position);
 		this.velocity = new Vec(other.velocity);
 		this.size = other.size;
 		this.maxSpeed = other.maxSpeed;
 		this.sightRadius = other.sightRadius;
 	}
 	
 	public Animal()	{
 		position = new Vec(0,0);
 		velocity = new Vec(0,0);
 		size = 5;
 		maxSpeed = 0.2;
 		sightRadius = 100;
 	}
 	
 	public Animal(double x, double y, double xvel, double yvel, double size)	{
 		this();
 		position = new Vec(x,y);
 		setVelocity(new Vec(xvel,yvel));
 		this.size = size;
 	}
 	
 	public double getSize() 	{	return size;		}
 	public double getMaxSpeed()	{	return maxSpeed;	}
 	public double getRadius()	{	return sightRadius;	}
 	public Vec getVelocity() 	{	return (oldVelocity == null ? velocity : oldVelocity).mult(getMaxSpeed());	}
 	public void setVelocity(Vec v)	{
 		velocity = new Vec(v).mult(1.0/getMaxSpeed()).truncate(1);
 	}
 	
 	//public Object clone()		{	return new Animal(this);	}
 	
 	public void update()	{
 		if(isAlive())	{
 			position = position.plus(velocity);
 			oldVelocity = new Vec(velocity);
 		}
 	}
 }
