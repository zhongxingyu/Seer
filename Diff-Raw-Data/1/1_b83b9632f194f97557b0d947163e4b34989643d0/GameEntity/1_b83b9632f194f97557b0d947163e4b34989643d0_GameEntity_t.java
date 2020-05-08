 import java.awt.*;
 import javax.swing.*;
 
 public abstract class GameEntity{
 
    // Private happy
    // No reason for gets otherwise
    double x;
    double y;
    Image img;
    double x_vel = 0;
    double y_vel = 0;
    int speed = 0;
    double accel = 2;
    double fric = 0.9;
    Rectangle bounds;
    public  int health;
    String image;
    
    public GameEntity(int x,int y) {
      this.x = x;
      this.y = y;    
    }
    
    public abstract void update(Space stage);   
    public abstract void draw(Graphics2D g);
    public abstract String getImg();
 
    public void setImage() {
   
      // No need to init the same image over and over.. 
      // I know speed isn't a serious issue - for the moment at least (Android?)
      // but Ideally we should generate code to do this once and wrap it all
      // in a static class so we don't keep initializing with every single GameEntity 
      img = new ImageIcon(this.getClass().getResource(getImg())).getImage();  
      bounds = new Rectangle(getX(),getY(),getWidth(),getHeight());
    }
 
    public double getX_vel(){
       return x_vel; 
    }
    public double getY_vel(){
       return y_vel;
    }
    public double getx(){
       return x;
    }
    public int getX(){
       return (int)x;
    }
    public double gety(){
       return y;
    }
    public int getY(){
       return (int)y;
    }
    public Image getImage() {
 	  return img;
    }   
    public int getWidth() {
       return img.getWidth(null);
    }
    public int getHeight() {
       return img.getHeight(null);
    }
    public int getHealth() {
       return health;
    }
    public void setX(int x) {
      this.x = x;
    }
    public void setY(int y) {
      this.y = y; 
    }
    public void setHealth(int health) {
      this.health = health;
    }
 
 }
