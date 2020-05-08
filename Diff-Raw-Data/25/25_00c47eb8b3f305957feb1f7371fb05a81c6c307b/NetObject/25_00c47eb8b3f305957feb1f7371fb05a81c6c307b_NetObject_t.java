 package net;  
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import jig.engine.util.Vector2D;
 
 public class NetObject {
 	
 	public static final int PLAYER   = 0;
 	public static final int GROUND   = 1;
 	public static final int PLATFORM = 2;
 	public static final int SMALLBOX = 3;
 	
 	private int id;
 	private Vector2D position;
 	private Vector2D velocity;
 	private double rotation;
 	private String ipAddress;
 	private int type;
 
 	public NetObject(int id) {
 		init(id, new Vector2D(0,0));
 	}
 	
 	public NetObject(int id, Vector2D p) {
 		init(id, p);
 	}
 
 	public NetObject(int id, Vector2D p, int type) {
 		init(id,p,type);
 	}
 	
 	public NetObject(int id, Vector2D p, Vector2D v, int type) {
 		init(id,p,type);
 		setVelocity(v);
 	}
 	
 	
 	public void init (int id, Vector2D p, int t) {
 		init(id, p);
 		type = t;	
 	}
 	
 	public void init (int objectId, Vector2D p) {
 		type = PLAYER;
 		id = objectId;
 		rotation = 0;
 		setPosition(p);
 		setVelocity(new Vector2D(0,0));
 		
 	    try {
 	        InetAddress addr = InetAddress.getLocalHost();
 	        ipAddress = addr.getHostAddress();
 	    } catch (UnknownHostException e) {
 	    	ipAddress = "127.0.0.0.1";
 	    }			
 	}
 	
 	public void update(long deltaMs) {
 		Vector2D p = getPosition();
 		Vector2D v = getVelocity();
 		
		setPosition(new Vector2D(p.getX()+v.getX()*deltaMs/1000,
				                 p.getY()+v.getY()*deltaMs/1000));
 	}
 	
 	
 	public int getId() { return id; }
 	
 	public Vector2D getPosition() { return position; }
 	public void setPosition(Vector2D p) { position = p; }
 	
 	public Vector2D getVelocity() { return velocity; }
 	public void setVelocity(Vector2D v) { velocity = v; }
 
 	public double getRotation() { return rotation; }
 	public void setRotation(double r) { rotation = r; }
 	
 	public String getIp() { return ipAddress; }
 	public void setIp(String ip) { ipAddress = ip; }
 	
 	public int getType() { return type; }
 }
