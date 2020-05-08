 //Hovedklasse for alt som puttes p brettet,
 //skal bli abstrakt
 
 package entity;
 
 import java.util.Random;
 
 import level.BasicLevel;
 
 import Networking.NetworkObject;
 import Networking.NetworkObjectType;
 import baseGame.Rendering.Renderer;
 import blueMaggot.Game;
 
 public abstract class Entity implements NetworkObject {
 
 	private int id;
 	private boolean isOnlineClient;
 	private NetworkObjectType type;
 
 	public Entity(double x, double y, double xr, double yr, BasicLevel level) {
 		this.level = level;
 		setLocation(x, y);
 		this.xr = xr;
 		this.yr = yr;
 		initNetworkValues();
 	}
 	
 	@Override
 	public int getId() {
 		return id;
 	}
 
 
 	@Override
 	public abstract void initNetworkValues();
 
 	@Override
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 
 	@Override
 	public void setIsOnlineGameClient(boolean isClient){
 		this.isOnlineClient = isClient;
 	}
 	
 
 	@Override
 	public boolean IsOnlineGameClient() {
 
 		return isOnlineClient;
 	}
 
 	@Override
 	public void setNetworkObjectType(NetworkObjectType type) {
 		this.type = type;
 	}
 
 	@Override
 	public NetworkObjectType getNetworkObjectType() {
 		return type;
 
 	}
 
 	protected final Random rand = new Random();
 	protected double x, y;
 	protected double xr, yr;
 	protected BasicLevel level;
 	protected double angle = 0;
 	protected double dx = 0, dy = 0;
 	protected double frictionConstant = 0.05;
 	protected double dt;
 	public boolean removed = false;
 	public double damageTaken = 1;
 
 
 	@Override
 	public void handleMessage(String[] message){
 		
 		//x
 		double x = Double.parseDouble(message[4]);
 		//y
 		double y = Double.parseDouble(message[5]);
 		//dx
 		
 		setLocation(x , y);
 		setSpeed(dx, dy);
 	}
 
 	@Override
 
 	public void setLocation(double x, double y) {
 		this.x = x;
 		this.y = y;
 	}
 
 	public void setLocation(FloatingPoint point) {
 		this.x = point.getX();
 		this.y = point.getY();
 	}
 
 	public double[] getLocation() {
 		double[] location = { x, y };
 		return location;
 	}
 
 	public double getXr() {
 		return xr;
 	}
 
 	public double getYr() {
 		return yr;
 	}
 
 	@Override
 	public void setSpeed(double dx, double dy) {
 		this.dx = dx;
 		this.dy = dy;
 	}
 
 	public double getAngle() {
 		return this.angle;
 	}
 
 	public void accelerate(double ddx, double ddy) {
 		this.dx += ddx * dt;
 		this.dy += ddy * dt;
 	}
 
 	public void gravitate() {
 		accelerate(0, 0.1);
 	};
 
 	@Override
 	public void move(double dt) {
 		this.setLocation((this.x + this.dx * dt), (this.y + this.dy * dt));
 	}
 
 	@Override
 	public String getObject() {
 
 		
 		if(type !=null && !isOnlineClient)
 		return "'" + to5DigitString(getId())+ "'" +type + "'"  + removed + "'"+ to5DigitString(x) + "'" + to5DigitString(y);
 		else
 		return "";
 	}
 
 	protected String to5DigitString(double x) {
 		String part1 = String.format("%.0f", x);
		String part2 = String.format("%." + (5 - part1.length()) + "f" , x - (int) x).substring(2);
		return part1 +"."+ part2;
 	}
 	
 	@Override
 	public boolean isRemoved() {
 		return removed;
 	}
 
 	public boolean intersectsEntity(Entity ent) {
 		double[] p = ent.getLocation();
 		double xLeft = p[0] - ent.xr;
 		double xRight = p[0] + ent.xr;
 		double yTop = p[1] - ent.yr;
 		double yBot = p[1] + ent.yr;
 		if (!(x + xr < xLeft || y + yr < yTop || x - xr > xRight || y - yr > yBot)) {
 			return true;
 		}
 		return false;
 	}
 
 	public void takeDamage(double amount) {
 	}
 
 	public void tick(double dt) {
 		this.dt = dt;
 		move(dt);
 		gravitate();
 		if (x > Game.WIDTH + 100 || x < -100 || y > Game.HEIGHT + 100 || y < -1000)
 			remove();
 	}
 
 	public abstract void render(Renderer renderer);
 
 	@Override
 	public void remove() {
 		this.removed = true;
 	}
 }
