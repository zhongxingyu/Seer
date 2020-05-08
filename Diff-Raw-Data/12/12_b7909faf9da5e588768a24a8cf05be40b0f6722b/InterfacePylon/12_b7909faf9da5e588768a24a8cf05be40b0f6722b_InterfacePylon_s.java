 /**
  * @(#)InterfacePylon.java
  *
  *
  * @author 
  * @version 1.00 2012/7/10
  */
 
 
 import java.net.URL;
 import java.awt.Graphics2D;
 import java.awt.image.ImageObserver;
 import java.awt.Color;
 
 public class InterfacePylon extends UIElement{
 
 	Pylon pylon;
 
     public InterfacePylon(double nx, double ny, String img, URL imagecontext){
     	super(nx,ny,img,imagecontext);
     }
     
     public InterfacePylon(Pylon P) {
     	double cornerX = 650, cornerY = 643;
     	
     	pylon = P;
     	
     	//Grab the pylon GUI image
     	String image = P.interfaceImg;
     	URL spritecontext = Global.codeContext;
     	
     	//Set the graphic coordinates
     	double nx = P.screenX + cornerX, ny = P.screenY + cornerY;
     	
     	//initialize other attributes
     	mouseOpacity = 0;
     	
     	layer = 25;
     	
     	x = nx; y = ny;
     	
     	move(nx,ny);
     	
     	if(image != null && spritecontext != null)
     	{
     		sprite = new Sprite(image, spritecontext, false);
 	    	context = spritecontext;
 	    	
 	    	//Init();
 	    	Global.state.newObjBuffer.add(this);
     	}
     }
     
     public void Draw(Graphics2D g, ImageObserver I){
     	//Compute the color stuff
    	int cr=-32,cg=-32,cb=-32,ca = -128;
     	
     	double percent=0;
     	
     	if(pylon!=null){
     		percent = pylon.realHealth/(pylon.baseHealth + (pylon.equipped!=null? pylon.equipped.baseHealth : 0));
     		if(percent != 0) ca = 0;
     		
     		int colorIncrement = 48;
     		
     		cb = 0;
     		if(percent > 0.5) {
     			cr = (int) ((1-percent)*2*colorIncrement);
     			cg = (int) (colorIncrement);
     		}
     		else {
     			cr = (int) (colorIncrement);
     			cg = (int) (percent*2*colorIncrement);
     		}
     		
     	}
     	
     	//Call sprite.addColors()
     	sprite.addColors(cr,cg,cb,ca);
     	
     	
     	//Draw the sprite
     	sprite.Draw(g,I);
     	
     	//Draw the pylon pointer
     	if(pylon!=null && percent > 0){
     		double dx,dy;
     		dx = x+sprite.frameX/2;
     		dy = y+sprite.frameY/2;
     		
     		//This will be the center of whereever the thing is drawn.
     		
     		int radius = 4;
     		
     		g.setColor(Color.yellow);
     		g.drawOval((int) (dx-radius), (int) (dy-radius), radius*2, radius*2);
     		if(pylon.type.compareTo("Weapon")==0){
     			double fx = dx+radius*2*Math.cos(Math.toRadians(pylon.currAngle));
     			double fy = dy+radius*2*Math.sin(Math.toRadians(pylon.currAngle));
     			g.drawLine((int) dx, (int) dy, (int) fx, (int) fy);
     		}
     		
     	}
     	
     }
     
 }
