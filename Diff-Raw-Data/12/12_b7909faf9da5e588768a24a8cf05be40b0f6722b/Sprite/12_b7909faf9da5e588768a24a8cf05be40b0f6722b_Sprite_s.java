 /**
  * This is a super class that provides basic functionality for displaying sprites.
  */
 
 import java.io.*;
 import java.awt.*;
 import java.awt.geom.*;
 import java.awt.image.*;
 import javax.imageio.*;
 import java.net.*;
 
 public class Sprite {
 	public AffineTransform transform = new AffineTransform();
 	BufferedImage img;
 	BufferedImage source;
 	
 	boolean hasFrames;
 	int frame;
 	int frameX;
 	int frameY;
 	
 	public Sprite(String dir, URL context, boolean hF){
 		
 		hasFrames = hF;
 		
 		try {
 			URL url = new URL(context, dir);
 			source = ImageIO.read(url);
 			
 			frameY = source.getHeight();
 			frameX = frameY;
 			
 			setFrame(1);
 		} catch (IOException e) {
 		}
 		
 	}
 	
 	public Sprite(String dir, boolean hF){
 		
 		hasFrames = hF;
 		
 		try {
 			source = ImageIO.read(new File(dir));
 			
 			frameY = source.getHeight();
 			frameX = frameY;
 			
 			setFrame(1);
 		} catch (IOException e) {
 		}
 		
 	}
 	
 	public int getFrames(){
 		return source.getWidth()/frameX;
 	}
 	
 	public void setFrame(int f){
 		if(!hasFrames) {
			img.flush();
 			frameX = source.getWidth();
 			frameY = source.getHeight();
 			
 			img = source.getSubimage(0,0,frameX,frameY);
 			return;
 		}
 		if(frame == f) return;
 		frame = f;
 		
 		//if(source == null) return;
 		int gx,gy;
 		gx = (frame-1)*frameX;
 		gy = 0;
 		
 		while(gx+frameX > source.getWidth()){
 			frame--;
 			gx = (frame-1)*frameX;
 		}
 		//Garbage Collector Sanity shit
		img.flush();
 		img = null;
 		
 		//Set the current image to the new frame
 		img = source.getSubimage(gx, gy, frameX, frameY);
 		
 	}
 	
 	public void setHasFrame(boolean f){
 		hasFrames = f;
 		setFrame(frame);
 	}
 	
 	public void setTransform(AffineTransform at){
 		transform = at;
 	}
 	
 	public void setImage(BufferedImage image){
 		source = image;
 	}
 	
 	public void Draw (Graphics2D graphic, ImageObserver loc){
 		graphic.drawImage(img, transform, loc);
 	}
 	
 	public void addColors(int r, int g, int b, int a){
 		img.flush();
 		
 		int nf = frame;
 		frame = -1;
 		setFrame(nf);
 		for(int x=0;x<=frameX;x++){
 			for(int y=0;y<=frameY;y++){
 				
 				int cr, cg, cb, ca;
 				int color = img.getRGB(x,y);
 				
 				cr = r + ((color) & 0xFF);
 				cg = g + ((color  >> 8) & 0xFF);
 				cb = b + ((color  >> 16) & 0xFF);
 				ca = a + ((color  >> 24) & 0xFF);
 				
 				cr = Math.max(0,Math.min(255,cr));
 				cg = Math.max(0,Math.min(255,cg));
 				cb = Math.max(0,Math.min(255,cb));
 				ca = Math.max(0,Math.min(255,ca));
 				
 				color = 0;
 				color = (cr) | (cg << 8) | (cb << 16) | (ca << 24);
 				
 				img.setRGB(x,y,color);
 			}
 			
 		}
 	}
 }
