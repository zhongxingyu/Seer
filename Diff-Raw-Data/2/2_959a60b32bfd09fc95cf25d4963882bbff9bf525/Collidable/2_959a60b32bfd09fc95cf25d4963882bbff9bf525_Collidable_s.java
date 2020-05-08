 package core;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.font.FontRenderContext;
 
 public abstract class Collidable{
 	protected int x, y, width, height;
 	protected int speedx,speedy;
 	protected int angle=90;
 	protected int dmg;
 	protected Rectangle bounds;
 	protected String shape;
 	protected static Font smallFont = new Font("SansSerif", Font.PLAIN, 12);
 	protected static Font bigFont = new Font("SansSerif", Font.BOLD, 18);
 	protected int health;
 	static FontRenderContext frc;
 	public Collidable(){
 		x=0;
 		y=50;
 		shape="/\\";
 	}
 	public Collidable(String shape){
 		x=0;
 		y=50;
 		this.shape=shape;
 		health=10;
 	}
 	public Collidable(int x, int y, int dmg, String shape, int health){
 		this.x=x;
 		this.y=y;
 		this.dmg=dmg;
 		this.shape=shape;
 		this.health=health;
 	}
 	public int getDmg(){
 		return dmg;
 	}
 	public Rectangle getRect(){
 		return bounds;
 	}
 	public boolean checkCollision(Collidable other){
 		if(bounds==null||other.getRect()==null)return false;
 		return this.getRect().intersects(other.getRect());
 	}
 	public abstract void move();
 	public abstract void collide(Collidable other);
 	public int getX(){return x;}
 	public int getY(){return y;}
 	public void draw(Graphics2D g2){
 		if(smallFont==null){
 			smallFont=g2.getFont();
 			bigFont=smallFont.deriveFont(18.0f);
 		}
 		if(frc==null)frc=g2.getFontRenderContext();
 		g2.setFont(bigFont);
 		if(width==0||height==0||bounds==null){
 			width=(int)bigFont.getStringBounds(shape, frc).getWidth();
			height=(int)bigFont.getStringBounds(shape, frc).getWidth();
 			bounds = new Rectangle(x,y,width,height);
 		}
 		bounds.setLocation(x, y) ;
 		g2.setPaint(Color.WHITE);
 		g2.drawString(shape,x,y);
 		g2.setFont(smallFont);
 	}
 }
