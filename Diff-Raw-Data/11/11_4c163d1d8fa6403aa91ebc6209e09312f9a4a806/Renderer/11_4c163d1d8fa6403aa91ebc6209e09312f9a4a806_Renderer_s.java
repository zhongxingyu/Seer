 package interaction;
 
 import engine.MainLoop;
 import game.Game;
 import game.Projectile;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import util.Vector2D;
 
 public class Renderer {
   
 	
 
   public JPanel panel;
   private Game game;
  
   //This variable has a special function
   //it is only valid during the execution if render()
   //and provides access to the g2d object to the
   //classes draw functions
   private Graphics2D graphics;
   
   private double width, height;
   
   public Renderer(Game g, JFrame frame) {
 	  this.game = g;
 	  this.panel = (JPanel) frame.getContentPane();
 	  width = (double)panel.getWidth();
 	  height = (double)panel.getHeight();
   }
 
   
   public void render() {
 	  draw(panel.getGraphics());
   }
   
   private void draw(Graphics g) {
 
     
     double sizeX = width/(double)game.f.tilesX;
     double sizeY = height/(double)game.f.tilesY;
     
     BufferedImage back = new BufferedImage((int)width,(int)height,BufferedImage.TYPE_BYTE_INDEXED);
     Graphics2D g2d = back.createGraphics();   
     graphics = g2d;
     
     g2d.setColor(new Color(0,0,0));
     g2d.fillRect(0, 0, (int)width, (int)height);
     
     for(int x=0;x<game.f.tilesX;x++) {
     	for(int y=0; y<game.f.tilesY;y++) {
     		switch(game.f.tiles[x][y]) {
     		case 1:
    			g2d.setColor(new Color(255,0,0));
     			g2d.fill(new Rectangle2D.Double(
         				(double)x*sizeX,(double)y*sizeY,sizeX,sizeY));
     			break;
     		case 2:
     			g2d.setColor(new Color(0,0,255));
     			g2d.fill(new Rectangle2D.Double(
     					(double)x*sizeX,(double)y*sizeY,sizeX,sizeY));
     			break;
     		
     		}
 
     	}
     }
     
 
     ArrayList<Vector2D> path = game.u.path;
     g2d.setColor(new Color(0,255,0));
     if(path != null) {
     	if(path.size() > 0) {
     		g2d.drawLine((int)(game.u.pos.x()*width), 
 					 (int)(game.u.pos.y()*height),
 					 (int)(path.get(0).x()*width), 
 					 (int)(path.get(0).y()*height));
     	}
     	for(int i=0;i<path.size()-1;i++) {
     		g2d.drawLine((int)(path.get(i).x()*width), 
     					 (int)(path.get(i).y()*height),
     					 (int)(path.get(i+1).x()*width), 
     					 (int)(path.get(i+1).y()*height));
     	}
     }
     
     g2d.fill(new Ellipse2D.Double((game.u.pos.x()*width)-(sizeX/4),
     							  (game.u.pos.y()*height)-(sizeY/4),
     							  sizeX/2,sizeY/2));
     
     //Projectiles
    for (Projectile proj : game.projectiles) {
		line(proj.pos, proj.pos.add(proj.direction.scalar(0.01)));		
     }
     
     g2d.draw(new Ellipse2D.Double(
     		  game.c.x()*width-(sizeX/2),
 			  game.c.y()*height-(sizeY/2),
 			  sizeX,sizeY));
     
     g2d.drawLine((int)(game.c.x()*width), 
     			 (int)(game.c.y()*height-(sizeY/2)-5), 
     			 (int)(game.c.x()*width), 
     			 (int)(game.c.y()*height+(sizeY/2)+5));
     
     g2d.drawLine((int)(game.c.x()*width-(sizeY/2)-5), 
 			 	 (int)(game.c.y()*height), 
 			 	 (int)(game.c.x()*width+(sizeY/2)+5), 
 			 	 (int)(game.c.y()*height));
     
     
     
     
     String fps = Double.toString(MainLoop.framerate);
     int dot = fps.indexOf(".");
     
     g2d.drawString(fps.substring(0, dot+2) + " fps", 5.0f,15.0f);
     
     Graphics2D g2dpanel = (Graphics2D)g;
     g2dpanel.drawImage(back,null, 0,0);
   }
   
     private void line(Vector2D a, Vector2D b) {
     	graphics.drawLine(
     		 (int)(a.x()*width), (int)(a.y()*height), 
    			 (int)(b.x()*width), (int)(b.y()*height));
     }
 
   public int gt(int x, int y) {
 	  try{
 		  return game.f.tiles[x][y];
 	  }
 	  catch(Exception e)
 	  {
 		  return 1;
 	  }
   }
  
 
 
 }
