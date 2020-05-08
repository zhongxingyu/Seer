 package dinoForts.dinosaurs;
 
 import main.Maths;
 
 public class DinosaurLocomotion {
 
 	private float targetX, targetY;
 	
 	private int mass;
 	
 	// Locomotion
 	private float velocity;
 	private float desiredVelocity;
 	private float maxForce;
 	private float maxVelocity;
 	
 	private float maxAccel;
 	
 	// Rotation
 	private float maxAngularForce;
 	private float desiredAngle;
 	private float angle;
 	
 	private float maxAngleAccel;
 	
 	private DinosaurEntity entity;
 
     private boolean halting;
 
     private static final int HALTING_RADIUS = 3;
 	
 	public DinosaurLocomotion(DinosaurEntity entity){
 		
 	    this.entity = entity;
 	    
 	    this.targetX = 0;
 	    this.targetY = 0;
 		
 		this.mass = 2000;
 		
 		// Movement variables
 		velocity = 0;
 		desiredVelocity = 0;
 		maxForce = 1;
 		maxVelocity = 1;
 		
 		maxAccel = (float)(maxForce / mass);
 		
 		maxAngularForce = 10;
 		desiredAngle = 0;
 		angle = 0;
 		
 		maxAngleAccel = (float)(maxAngularForce / mass);
 		
 		this.halting = false;
 	}
 
 	public float getAngle() {
 		return angle;
 	}
 
 	public void setTargetX(float targetX) {
 	    this.targetX = targetX;
 	    this.halting = false;
     }
 	
 	public void setTargetY(float targetY) {
 	    this.targetY = targetY;
 	    this.halting = false;
     }
 	
 	public float getX() {
 	    return this.entity.getX();
 	}
 	
 	public float getY() {
         return this.entity.getY();
     }
 	
 	public void move(int delta) {
 
 	    calculateDesiredVelocity();
 		calculateDesiredRotation();
 		
 		calculateVelocity(delta);
 		calculateRotation();
 		
 		// Commit movement
 		this.setX((float) (this.getX() + (Math.cos(angle)) * velocity));
 		this.setY((float) (this.getY() + (Math.sin(angle)) * velocity));
 		
 		// Sometimes the dinosaur can overshoot a destination by a bit and will have to make large movements to try and land exactly on spot
 		// So if it is close to the target, just make it stop
 		if(Maths.getDistance(this.getX(), this.getY(), this.targetX, this.targetY) < HALTING_RADIUS )
 		    this.halting = true;
 		
 		// Dinosaur has completely halted
         if(this.velocity == 0 && this.halting) {
             this.halting = false;
             this.targetX = this.getX();
             this.targetY = this.getY();
         }
 		
 	}
 
 	private void setY(float y) {
 	    this.entity.setY(y);
     }
 
     private void setX(float x) {
         this.entity.setX(x);
     }
 
     private void calculateDesiredVelocity() {
         
 		float distanceToTarget = Maths.getDistance(this.getX(), this.getY(), this.targetX, this.targetY);
         double angleDifference = Maths.angleDifference(Math.toDegrees(angle), Math.toDegrees(desiredAngle));
         
         if(!halting && distanceToTarget > 1) {
 		    
             // First case handles cases where movement point is very close to ai so the angle must be spot on so it doesn't loop around the target
             if(distanceToTarget < 120 && angleDifference > 45 + distanceToTarget){
                 this.desiredVelocity = 0;
             }
 		    else if(distanceToTarget > ((velocity * velocity) / (float)(2 * maxAccel)))
 				this.desiredVelocity = maxVelocity;
 			else
 				this.desiredVelocity = 0;
 			
 		}
 		else {
 		    this.desiredVelocity = 0;
 		}
 	}
 
 	private void calculateDesiredRotation() {
 		if(!halting && Maths.getDistance(this.getX(), this.getY(), this.targetX, this.targetY) > 1)
 			this.desiredAngle = Maths.getRads(this.getX(), this.getY(), this.targetX, this.targetY);
 		else
 			this.desiredAngle = this.angle;
 	}
 
	private void calculateVelocity(int delta) {
 		if(this.velocity < this.desiredVelocity){
 			// F = MA
 			// A =  F/M
 			this.velocity += maxAccel;
 		}
 		else if(this.velocity > this.desiredVelocity){
 			this.velocity -= maxAccel;
 		}
 		
 		if(Math.abs(this.velocity) > Math.abs(maxVelocity))
 			this.velocity = maxVelocity;
 	}
 
 	private void calculateRotation() {
 
 		if(Maths.angleDifference(Math.toDegrees(angle), Math.toDegrees(desiredAngle)) < Math.toDegrees(this.maxAngleAccel) + Math.toDegrees(this.maxAngleAccel) * 0.01){
 			this.angle = desiredAngle;
 			return;
 		}
 		
 		if(Maths.isAngleLeft(Math.toDegrees(angle), Math.toDegrees(desiredAngle))){
 			this.angle += maxAngleAccel;
 		}
 		else if(!Maths.isAngleLeft(Math.toDegrees(angle), Math.toDegrees(desiredAngle))){
 			this.angle -= maxAngleAccel;
 		}
 		
 	}
 
     public float getTargetX() {
         return targetX;
     }
 
     public float getTargetY() {
         return targetY;
     }
     
     public void halt () {
         this.halting = true;
     }
 
 }
