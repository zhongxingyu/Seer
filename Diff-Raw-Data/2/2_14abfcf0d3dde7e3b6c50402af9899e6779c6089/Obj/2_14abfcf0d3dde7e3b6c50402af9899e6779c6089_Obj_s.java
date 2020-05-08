 /**
  * This is a physical object that is displayed on the game screen.
  *
  * Physically represented by a polygon, and graphically represented by a sprite.
  *
  * Provides functionality for collision detection with other polygons
  *
  * Note: Keep the AffineTransform for the Obj and the Sprite separate.
  * 
  * The Obj uses the transform() method to transform it's vertices.
  *     When a transformation is valid for whatever it's been used for, transform() is called
  *     to transform the vertices. This is a must for collision detection. Game Errors WILL occur 
  *     if collision detection is performed before the transformation.
  *
  * The sprite has no such feature, and cannot save transformations. 
  *     It depends on the AffineTransform object to keep track of all transformations, 
  *     and the deletion of the AffineTransform or inconsistent change of reference for the 
  *     AffineTransform will result in the incorrect rendering of the sprite.
  */
 
 import java.io.*;
 import java.awt.*;
 import java.awt.image.*;
 import javax.imageio.*;
 import java.net.*;
 import java.awt.geom.*;
 import java.util.*;
 
 public class Obj implements Comparable{
 	
 	public Sprite sprite;
 	public Color color = Color.white;
 	
 	ArrayList<HitCircle> hitCircles = new ArrayList(0);
 	
 	public URL context;
 	
 	public double x; //World coordinates. Sprite is drawn centered on these coordinates
 	public double y;
 	public double angle;//Angle
 	public double dx=1;//draw width (For scaling)
 	public double dy=1;//draw height (For scaling)
 	
 	public double layer=3; //Drawing layer
 	
 	public boolean density = true;//Set to false if this object cannot collide
 	public boolean mouseOpacity = true; //Set to false if this object can never be clicked or dragged
 	public boolean passThroughTag; //Objects with the same passThroughTag will never register hits with eachother
 	
 	public Obj(){
 		
 	}
 	
     public Obj(String image) {
     	if(image != null) {
 	    	sprite = new Sprite(image, true);
 	    	Init();
     	}
     }
     
     public Obj(String image, URL spritecontext) {
     	if(image != null && spritecontext != null)
     	{
     		sprite = new Sprite(image, spritecontext, true);
 	    	context = spritecontext;
 	    	Init();
     	}
     }
     
     public void Init(){
     	Global.state.activeObjects.add(this);
     	if(CameraCanSee()){
     		Global.view.addDrawObject(this);
     	}
     }
     
     public void setColor(Color C){
     	color = C;
     }
     
     public void setSprite(Sprite s){
     	sprite = s;
     }
     
     public HitCircle contains(PointS P){
     	double px = P.getX(), py = P.getY();
     	px -= x; py-= y;
     	
     	for(HitCircle O:hitCircles){
    		double d2 = px*px+py*py;
     		if(d2 <= O.r2) return O;
     	}
     	
     	return null;
     }
     
     public HitCircle checkCollision(Obj O){
     	if(!O.density || !this.density) return null;
     	if(O.passThroughTag == this.passThroughTag) return null;
     	
     	for(HitCircle A:hitCircles){
     		for(HitCircle B:O.hitCircles){
     			double d = (O.x-x+B.rx-A.rx)*(O.x-x+B.rx-A.rx)+(O.y-y+B.ry-A.ry)*(O.y-y+B.ry-A.ry);
     			d = Math.sqrt(d);
     			if(A.radius+B.radius>=d) return A;
     		}
     	}
     	return null;
     }
 	
 	public void move(double nx, double ny){
 		double px = nx+x, py = ny+y;
 		translate(px,py);
 	}
 	
 	public void translate(double nx, double ny){
 		x=nx;
 		y=ny;
 	}
 	
 	public void translate(Point2D PointS){
 		double nx = PointS.getX();
 		double ny = PointS.getY();
 		x=nx;
 		y=ny;
 	}
 	
 	public void rotate(double theta){
 		angle += theta;
 		
 		while(angle >= 360) angle-=360;
 		while(angle < 0) angle+=360;
 		
 		for(HitCircle O:hitCircles){
 			double r,a;
 			r = Math.sqrt(O.rx*O.rx+O.ry*O.ry);
 			a = Math.atan2(O.ry,O.rx);
 			a += theta*Math.PI/180;
 			O.rx = r*Math.cos(a);
 			O.ry = r*Math.sin(a);
 		}
 	}
 	
 	public void setAngle(double theta){
 		double delta = theta-angle;
 		rotate(delta);
 	}
 	
 	public void flip(){
 		double cx=0, cy=0;
 		
 	}
     
     public void transform(){
     	AffineTransform transform = new AffineTransform();
     	
     	double rx, ry;
     	rx = (x - Global.player.cx) * Global.player.zoom + Global.view.sizex/2;
     	ry = (Global.player.cy - y) * Global.player.zoom * Global.xyRatio + Global.view.sizey/2;
     	rx -= (sprite.frameX/2) * Global.player.zoom;
     	ry -= (sprite.frameY/2) * Global.player.zoom;
     	
     	transform.translate(rx, ry);
     	
     	transform.scale(dx * Global.player.zoom, dy * Global.player.zoom);
     	
     	sprite.setTransform(transform);
     }
     
     public void Draw(Graphics2D G, ImageObserver loc){
     	
     	//Once testing sprite/polygon coherence is complete, there needs to be a section added
     	//Where the transform is modified to make the drawn coordinates relative to the player.
     	
     	transform(); //Applies the object's transformations to the sprite
     	sprite.Draw(G,loc); //Draws the object's sprite
     }
 	
     public int compareTo(Obj O){
     	//I couldn't get my mergesort to work right? I don't know.
     	//This is a workaround for that.
     	double l = layer;
     	double ol = O.layer;
     	if(ol > l) return -1;
     	if(ol < l) return 1;
     	return 0;
     }
     
     public int compareTo(Object O){
     	return O.toString().compareTo(this.toString());
     }
     
     public boolean CameraCanSee(){
     	if(Global.player == null) return false;
     	
     	double rx, ry;
     	rx = Math.abs(x - Global.player.cx);
     	ry = Math.abs(y - Global.player.cy);
     	
     	if(sprite != null){
     		rx -= sprite.frameX/2;
     		ry -= sprite.frameY/2;
     		if(rx < 0) rx = 0;
     		if(ry < 0) ry = 0;
     	}
     	
     	if(rx * Global.player.zoom > Global.view.sizex/2) return false;
     	if(ry * Global.player.zoom * Global.xyRatio > Global.view.sizey/2) return false;
     	return true;
     }
     
     public void Step(){ //This is what will be called by the gamestate every tick
     }
 }
