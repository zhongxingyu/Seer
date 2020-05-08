 package mapobjects;
 
 import imageloader.ImageLoader;
 
 import java.awt.Color;
 import java.awt.image.BufferedImage;
 
 public abstract class MoveableObject extends MapObject {
 	public Direction direction;
 	protected int speed = 5;
 	
 	public MoveableObject(int x,int y,boolean v,boolean d,boolean c,String p,ImageLoader gr){
 		super(x,y,v,d,c,p,gr);
 	}
 	
 	public abstract void move(BufferedImage cm);
 	
 	public boolean hasObjectCollision(int x, int y,BufferedImage cm){
 		BufferedImage collTest = cm.getSubimage(x, y, 50, 50);
 		for(int i=0; i<collTest.getWidth(); i++){
 			for(int j=0; j<collTest.getHeight(); j++){
 				Color test = new Color(collTest.getRGB(i, j));
 				if(test.equals(Color.black)){return true;}
 			}
 		}
 		return false;
 	}
 	
 	public boolean hasEnemyCollision(int x, int y){
 		return false;
 	}
 	
 	public int getSpeed(){return speed;}
 }
 
 
