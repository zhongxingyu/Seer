 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Rectangle;
 import java.util.LinkedList;
 
 import javax.swing.ImageIcon;
 
 import Object.EntityDestroyable;
 import Object.EntityMapObject;
 import Object.EntityMovable;
 
 
 public class HealthPack extends GameObject implements EntityMapObject{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 7056820136332853615L;
 	
 	Controller c;
 	DungeonCrawlerGame game;
 	Player p;
 	private double x,y;
 	private Image pic = new ImageIcon("Health Pack-32x32.png").getImage();
 	private Image picLeben = new ImageIcon("Health Pack-32x32.png").getImage();
 	private Image picMana = new ImageIcon("Mana Pack-32x32.png").getImage();
<<<<<<< HEAD
	private Image picMoney = new ImageIcon("Money Pack-32x32.png").getImage();
 	//private boolean mapObject = true;
=======
 	private Image picMoney = new ImageIcon("Coin.gif").getImage();
 	private boolean mapObject = true;
>>>>>>> e21056ef083373619e7802fa841aa86c89cb6050
 	private boolean hited;
 	private int leben=0;
 	private int mana=0;
 	private int geld=0;
 	LinkedList<EntityMapObject> eMO = new LinkedList<EntityMapObject>();
 	public HealthPack(double x, double y, DungeonCrawlerGame game, Player p, int leben, int mana, int geld){
 		
 		
 		this.x=x;
 		this.y=y;
 		this.game = game;
 //		this.c = c;
 		this.p = p;
 		//this.p = p;
 		hited = false;
 		this.setBounds((int)x, (int)y, 32, 32);
 		log("HealthPack X ="+(int)x+" Y ="+(int)y);
 		this.leben = leben;
 		this.mana = mana;
 		this.geld = geld;
 		if (leben>0) {
 			pic = picLeben;
 		}
 		else if (mana>0) {
 			pic = picMana;
 		}
 		else if (geld>0) {
 			pic = picMoney;
 		}
 		
 	}
 
 	public void setLeben(int leben) {
 		this.leben = leben;		
 	}
 	public int getLeben() {
 		return leben;		
 	}
 	
 	public void setMana(int mana) {
 		this.mana = mana;		
 	}
 	public int getMana() {
 		return mana;		
 	}
 	
 	public void setGeld(int geld) {
 		this.geld = geld;		
 	}
 	public int getGeld() {
 		return geld;		
 	}
 	
 	
 	@Override
 	public void update() {
 		// TODO Auto-generated method stub
 		this.setBounds((int)x, (int)y, 25, 25);
 	//log(this.toString());
 ////			if( this.intersects(game.p1.playerRect) ){
 ////				log("COLLSION HEALTHPACK!!!!!!!");
 //			}
 //			
 //				
 //					
 //	}
 //			
 		}
 		
 	
 
 	@Override
 	public void draw(Graphics g) {
 		// TODO Auto-generated method stub
 		if(this !=null)
 			g.drawImage(pic ,(int) x,(int) y, null);
 	}
 
 	@Override
 	public double getX() {
 		// TODO Auto-generated method stub
 		return x;
 	}
 
 	@Override
 	public double getY() {
 		// TODO Auto-generated method stub
 		return y;
 	}
 
 	@Override
 	public void setX(double x) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void setY(double y) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	
 
 
 
 	@Override
 	void explode(boolean explode) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	@Override
 	public boolean isHited() {
 		// TODO Auto-generated method stub
 		return hited;
 	}
 
 
 	@Override
 	public void hit() {
 		// TODO Auto-generated method stub
 		boolean hited = true;
 	}
 	private void log(String s){
 		System.out.println(s);
 	}
 
 
 //	@Override
 //	public boolean isNPC() {
 //		// TODO Auto-generated method stub
 //		return false;
 //	}
 
 
 //	@Override
 //	public boolean isMapObject() {
 //		// TODO Auto-generated method stub
 //		return true;
 //	}
 }
