 package entities;
 
 public abstract class AbstractMovableEntity extends AbstractEntity implements MoveableEntity {
 
 	protected double dx, dy, dh;
 	protected double heading;
 	
 	protected boolean printHeading = true;
 	protected boolean printRadar = false;
 	protected boolean printPieSlices = false;
 	protected boolean printWallSensors = false;
 	
 	public AbstractMovableEntity(double x, double y, double width, double height) {
 		this(x, y, width, height, 0);
 	}
 	
 	public AbstractMovableEntity(double x, double y, double width, double height, double heading) {
 		super(x, y, width, height);
 		this.dx = 5;
 		this.dy = 5;
 		this.dh = 5;
 		this.heading = heading;
 	}
 
 	public void update(int delta) {
 
 	}
 	
 	public void toggleHeading() {
 		printHeading = !printHeading;
 		System.out.println("Heading: " + heading);
 	}
 
 	public void toggleRadar() {
 		printRadar = !printRadar;
 	}
 	
 	public void togglePieSlices() {
 		printPieSlices = !printPieSlices;
 		printRadar = printPieSlices;
 	}
 	
 	public void turnOnWallSensors() {
 		printWallSensors = true;
 	}
 	
 	public boolean isWallSensorActivated() {
 		return printWallSensors;
 	}
 	
 	public void turnOffWallSensors() {
 		printWallSensors = false;
 		resetWallSensors();
 	}
 	
 	public void resetWallSensors() {
 		return;
 	}
 
 	public double getDX() {
 		return dx;
 	}
 
 	public double getDY() {
 		return dy;
 	}
 	
 	public double getHeading() {
 		return heading;
 	}
 
 	public void setDX(double dx) {
 		this.dx = dx;
 	}
 
 	public void setDY(double dy) {
 		this.dy = dy;
 	}
 	
 	public void setHeading(double heading) {
 		this.heading = heading;
 	}
 	
 	public void increaseHeading() {
 		this.heading += dh;
 		this.heading %= 360;
 		if (printHeading)
 			System.out.println("Heading: " + heading);
 	}
 	
 	public void decreaseHeading() {
 		this.heading -= dh;
 		while (this.heading < 0)
 			this.heading += 360;
 		
 		if (printHeading)
 			System.out.println("Heading: " + heading);
 	}
 	
 	public void moveUp(double minX, double maxX, double minY, double maxY) {
 		this.x += Math.cos(Math.toRadians(heading)) * dx;
 		this.y -= Math.sin(Math.toRadians(heading)) * dy;
 		
 		if (this.x < minX)
 			this.x = maxX;
 		else if (this.x > maxX)
 			this.x = minX;
 		
 		if (this.y < minY)
 			this.y = maxY;
 		else if (this.y > maxY)
 			this.y = minY;
 		
 		System.out.println("(x,y): (" + x + ", " + y + ")");
 		turnOffWallSensors();
 	}
 	
 	public void moveDown(double minX, double maxX, double minY, double maxY) {
 		this.x -= Math.cos(Math.toRadians(heading)) * dx;
 		this.y += Math.sin(Math.toRadians(heading)) * dy;
		
		if (this.x < minX)
			this.x = maxX;
		else if (this.x > maxX)
			this.x = minX;
		
		if (this.y < minY)
			this.y = maxY;
		else if (this.y > maxY)
			this.y = minY;
		
 		System.out.println("(x,y): (" + x + ", " + y + ")");
 		
 		turnOffWallSensors();
 	}
 }
