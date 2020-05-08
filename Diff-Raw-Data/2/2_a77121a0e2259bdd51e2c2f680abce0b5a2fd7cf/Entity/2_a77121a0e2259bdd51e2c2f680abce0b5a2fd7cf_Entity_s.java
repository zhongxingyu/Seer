 public abstract class Entity {
 	private double xPosition; // in m
 	private double yPosition; // in m
 	private double xSpeed; // in m/s
 	private double ySpeed; // in m/s
 	private double weight; // in g
 
 	public double getxPosition() {
 		return xPosition;
 	}
 
 	public void setxPosition(double xPosition) {
 		this.xPosition = xPosition;
 	}
 
 	public double getyPosition() {
 		return yPosition;
 	}
 
 	public void setyPosition(double yPosition) {
 		this.yPosition = yPosition;
 	}
 
 	public double getxSpeed() {
 		return xSpeed;
 	}
 
 	public void setxSpeed(double xSpeed) {
 		this.xSpeed = xSpeed;
 	}
 
 	public double getySpeed() {
 		return ySpeed;
 	}
 
 	public void setySpeed(double ySpeed) {
 		this.ySpeed = ySpeed;
 	}
 
 	public double getFallBegin() {
 		return fallBegin;
 	}
 
 	public void setFallBegin(double fallBegin) {
 		this.fallBegin = fallBegin;
 	}
 
 	private boolean falling;
 	private double fallBegin;
 
 	public boolean isFalling() {
 		return falling;
 	}
 
 	public void setFalling(boolean falling) {
 		if (falling) {
 			fallBegin = System.currentTimeMillis();
 		}
 		this.falling = falling;
 	}
 
 	public Entity(double xPosition, double yPosition) {
 		setxPosition(xPosition);
 		setyPosition(yPosition);
 	}
 
 	public void update(double timeElapsed) {
 		applyGravity(timeElapsed);
 		updatePosition(timeElapsed);
 	}
 
 	protected void applyGravity(double timeElapsed) {
 		if (isFalling()) {
 			double fallTime = fallBegin - System.currentTimeMillis();
			ySpeed = fallTime * 0.981;
 		} else {
 			ySpeed = 0;
 		}
 	}
 
 	protected void updatePosition(double timeElapsed) {
 
 	}
 
 	public void toss(double angle, double speed) {
 		setFalling(true);
 		setxSpeed(speed * Math.cos(angle));
 		setySpeed(speed * Math.sin(angle));
 	}
 }
