 package mapobjects;
 
 import imageloader.Animation;
 import imageloader.ImageLoader;
 
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 
 import main.GameConstants;
 import map.Map;
 
 /*
   abstract class for all MapObjects     	
 */
 
 public abstract class MapObject {
 	protected int posX;
 	protected int posY;
 	protected int rotation = 0;
 	
 	protected boolean visible;
 	protected boolean destroyable;
 	protected boolean destroyed = false;
 	protected boolean collision;
 	protected String imageUrl;
 	protected Animation animation;
 	
 	
 	protected Map map;
 	
 	public MapObject(int x,int y,int r,boolean v,boolean d,boolean c,String img){
 		posX=x;
 		posY=y;
 		rotation=r;
 		visible=v;
 		destroyable=d;
 		collision=c;
 		imageUrl = GameConstants.MAP_GRAPHICS_DIR+img;
 		
 	}
 	
 	public MapObject(int x,int y,boolean v,boolean d,boolean c,String img,ImageLoader gr){
 		posX=x;
 		posY=y;
 		visible=v;
 		destroyable=d;
 		collision=c;
 		animation = new Animation(img,gr);
 
 	}
 	
 
 	//all MapObjects have to implement
 	public abstract void update();
 	public abstract void draw(Graphics2D g2d,ImageLoader gr,Graphics2D b);
 
 	// Getter und Setter
 	public String getImageUrl(){
 		return imageUrl;
 	}
 	
 	public int getPosX(){
 		return posX;
 	}
 	
 	public void setPosX(int x){
 		posX = x;
 	}
 	
 	public int getPosY(){
 		return posY;
 	}
 	
 	public void setPosY(int y){
 		posY = y;
 	}
 	
 	public boolean isDestroyable(){
 		return destroyable;
 	}
 	
 	public void setDestroyable(boolean b){
 		destroyable = b;
 	}
 	
 	public boolean isDestroyed() {
 		return destroyed;
 	}
 
 	public void setDestroyed(boolean destroyed) {
 		this.destroyed = destroyed;
 	}
 
 	public boolean isVisible(){
 		return visible;
 	}
 	
 	public void setVisible(boolean b){
 		visible = b;
 	}
 	
 	public boolean collides(){
 		return collision;
 	}
 	
 	public void setCollision(boolean b){
 		collision = b;
 	}
 		
 	public Map getMap() {
 		return map;
 	}
 
 	public void setMap(Map map) {
 		this.map = map;
 	}
 
 	public BufferedImage rotate(BufferedImage original,int degc){
 		if(degc!=0){
 			BufferedImage rotated = new BufferedImage(50,50,BufferedImage.TYPE_INT_ARGB);
 		for(int i=0;i<original.getWidth();i++){
 			for(int j=0;j<original.getHeight();j++){
 				int cx = (i*(int)(Math.cos(Math.toRadians((double)degc*90.00)))
 						 -j*(int)Math.sin(Math.toRadians((double)degc*90.00)));
 				int cy = (i*(int)Math.sin(Math.toRadians((double)degc*90.00))
 						  +j*(int)Math.cos(Math.toRadians((double)degc*90.00)));
				if(degc==1){if(cx<=0){cx+=49;}}
				else if(degc==3){if(cy<=0){cy+=49;}}
				else{
 				if(cx<=0){cx+=49;}
				if(cy<=0){cy+=49;}
				}
 				rotated.setRGB(
						(int)Math.floor(cx),
						(int)Math.floor(cy),
 						original.getRGB(i, j));
 			}
 			
 		}
 		return rotated;
 		}else return original;
 	}
 }
