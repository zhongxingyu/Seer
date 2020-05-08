 package asteroids.model;
 
 import java.util.Random;
 
 import be.kuleuven.cs.som.annotate.Basic;
 import be.kuleuven.cs.som.annotate.Immutable;
 
 public class Asteroid extends ObjectInSpace {
 	
 	public Asteroid(){
 		this(0,0,0,0,1);
 	}
 	
 	public Asteroid(double x, double y, double velocityX, double velocityY,
 			double radius){
 		
 		super(x,y,velocityX,velocityY,radius, 1);
 		setMass(calculateMass(radius));
 	}
 	
 	public Asteroid(double x, double y, double velocityX, double velocityY,
 			double radius, World world){
 		super(x,y,velocityX,velocityY,radius, 1);
 		setMass(calculateMass(radius));
 		setWorld(world);
 	}
 	
 	private final static double DENSITY = 7.8*Math.pow(10, 12);
 	
 	
 	/**
 	 * Terminate this asteroid.
 	 */
 	public  void Die(){
		if(this.getRadius() >=30) {
 			Random random = new Random();
 			Velocity newVelocity = Velocity.createVelocityInRandomDirection(1.5*Velocity.norm(Velocity.createVelocity(this.getVelocityX(), this.getVelocityY())), random);
 			double randomDouble = random.nextDouble();
 			double newRadius = this.getRadius();
 			Asteroid asteroid1 = new Asteroid(this.getX()+newRadius*Math.cos(Math.PI*2*randomDouble), this.getY()+newRadius*Math.sin(Math.PI*2*randomDouble), newVelocity.getXCoordinate(), newVelocity.getYCoordinate(), newRadius, this.getWorld());
 			Asteroid asteroid2 = new Asteroid(this.getX()-newRadius*Math.cos(Math.PI*2*randomDouble), this.getY()-newRadius*Math.sin(Math.PI*2*randomDouble), newVelocity.getXCoordinate(), newVelocity.getYCoordinate(), newRadius, this.getWorld());
 			this.getWorld().AddObjectInSpace(asteroid1);
 			this.getWorld().AddObjectInSpace(asteroid2);
 		}
 		this.terminate();
 	}
 	
 	/**
 	 * Returns the density in km/km.
 	 */
 	@Basic
 	@Immutable
 	public double getDensity() {
 		return DENSITY;
 	}
 	
 	public double calculateMass (double radius){
 		
 		return 4*Math.PI*Math.pow(radius, 3)*getDensity()/3;
 	}
 	
 	
 }
