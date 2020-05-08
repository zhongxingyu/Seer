 package nitrogene.core;
 
 import nitrogene.objecttree.RectangleObject;
 import nitrogene.world.ArenaMap;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import java.util.Random;
 
 
 public class SLaser extends RectangleObject{
 	
 	private float startX = 0, startY = 0, desX = 0, desY = 0, speed = 0, dx, dy;
 	double mangle = 0;
 	private float mmangle, scalesize, sspeed;
 	private int sdamage;
	private boolean isRotated = false;
 	
 	public SLaser(float startX, float startY, float destinX, float destinY, int accuracy, float speed, int damage, float size, float rotation, Image img, ArenaMap map) throws SlickException{
 		super(startX, startY, img.getWidth(), img.getHeight(), rotation, img, size, map);
 		this.startX =startX;
 		this.startY =startY;
 		this.desX = destinX;
 		this.desY = destinY;
 		this.sdamage = damage;
 		this.sspeed = speed;
 		this.scalesize = size;
 		this.mmangle = 0;
 		if(accuracy!=0){
 		desX += randomize(accuracy);
 		desY += randomize(accuracy);
 		}
 		recalculateVector(desX, desY);
 		recalculateAngle(desX, desY);
 	}
 	
 	/*
 	public SLaser(float startX, float startY, float destinX, float destinY, int accuracy, float speed, int damage, float size, Image im) throws SlickException{
 		super(startX, startY, img.getWidth(), img.getHeight(), img, size, map);
 		theimage = im;
 		this.startX =startX;
 		this.startY =startY;
 		this.desX = destinX;
 		this.desY = destinY;
 		this.sdamage = damage;
 		this.sspeed = speed;
 		this.scalesize = size;
 		this.mmangle = 0;
 		location.setLocation(startX,startY);
 		if(accuracy!=0){
 		desX += randomize(accuracy);
 		desY += randomize(accuracy);
 		}
 		boundbox = new AABB(theimage.getTextureWidth() * size, theimage.getTextureHeight() * size);
 		recalculateVector(desX, desY);
 		recalculateAngle(desX, desY);
 	}
 	*/
 	
 	private void recalculateVector(float desX2, float desY2) {
 		float vec = (float)(Math.atan2(desX2 - startX, desY2 - startY));
 		//set speed
 		speed = sspeed;
 
 		this.dx = (float) Math.sin(vec) * speed;
         this.dy = (float) Math.cos(vec) * speed;
 	}
 	
 	public void recalculateVector(){
 		recalculateVector(desX, desY);
 	}
 	
 	public void recalculateAngle(float desX2, float desY2){
 		double mecX = (desX2 - startX);
 		double mecY = (desY2 - startY);
 		mangle = Math.toDegrees(Math.atan2(mecY,mecX));
 		mmangle = (float) mangle;
 	}
 	
 	public float getAngle(){
 		return mmangle;
 	}
 	
 	@Override
 	public void move(int delta){	
		this.setX(this.getX()+((dx*2f)*(delta/1000f)));
		this.setY(this.getY()+((dy*2f)*(delta/1000f)));
 	}
 	
 	
 	private int randomize(int distance) {
 		Random rand = new Random();
 		int  n = rand.nextInt(distance * 2) + 1;
 		return (int) (n* Zoom.getZoom().scale - distance);
 		}
 	
 	public boolean isRotated(){
 		return isRotated;
 	}
 	public int getDamage(){
 		return sdamage;
 	}
 	public float getSize(){
 		return scalesize;
 	}
 	public void setRotated(boolean rotate){
 		isRotated = rotate;
 	}
 }
