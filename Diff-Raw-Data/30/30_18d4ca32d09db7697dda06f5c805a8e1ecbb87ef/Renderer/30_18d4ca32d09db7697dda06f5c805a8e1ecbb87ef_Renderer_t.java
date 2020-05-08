 package MainPackage;
 
 
 import java.awt.*;
 import java.awt.geom.*;
 import java.io.*;
 import java.util.ArrayList;
 
 
 
 // @author Michael Kieburtz and Davis Freeman
 
 public class Renderer {
     File fontFile;
     private Font fpsFont;
     private Point2D.Double cameraPos= new Point2D.Double(0, 0);
     private Point cameraSize;
     GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
     
     public Renderer(int cameraWidth, int cameraHeight) {
         
         fontFile = new File("resources/BlackHoleBB_ital.ttf");
         
         try {
             fpsFont = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(36f);
         } catch (FontFormatException ex) {
             System.err.println("Bad font");
             ex.printStackTrace();
         } catch (IOException ex) {
             System.err.println("Bad file");
             ex.printStackTrace();
         }
         
         cameraSize = new Point(cameraWidth, cameraWidth);
     }
 
     public void drawScreen(Graphics g, Player player, double xRot, double yRot, double fps,
             ArrayList<Dust> dust, Point2D.Double cameraPos, Point cameraSize) {
         Graphics2D g2d = (Graphics2D) g; // turns it into 2d graphics
         
         g.setColor(Color.BLACK);
        g2d.fillRect(0,0,1000,1000);
        
         //g2d.drawImage(player.getImage(4), null, 0, 0);
         g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
         g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
         g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
         for (int i = 0; i < dust.size(); i++)
         {
             if (insideCameraView(dust.get(i).getLocation()))
             {
                 dust.get(i).draw(g2d, cameraPos);
             }
         }
         
         g2d.drawImage(player.getImage(5),null,0,0);
        g2d.setFont(fpsFont.deriveFont(32f));
         g2d.setColor(Color.CYAN);
        
        
         g2d.drawString(String.valueOf(fps), 155, 33);
        
        g2d.setFont(fpsFont.deriveFont(60f));
        
        g2d.drawString("FPS:",10,50);
         
         
         AffineTransform origXform = g2d.getTransform();
         AffineTransform newXform = (AffineTransform) (origXform.clone());
         
         
         g2d.setPaint(new TexturePaint(player.getImage(), new Rectangle2D.Float(0, 0, player.getImage().getWidth(), player.getImage().getHeight())));
         newXform.setToIdentity();
         
         newXform.rotate(Math.toRadians(player.getAngle()), xRot, yRot);
         
         g2d.setTransform(newXform);
         
         g2d.drawImage(player.getImage(), (int)(player.getLocation().x - cameraPos.x), (int)(player.getLocation().y - cameraPos.y), null);
     }
     
     private boolean insideCameraView(Point2D.Double point)
     {
         // use nested if statements because the conditionals are so long.
         
         if (point.x > cameraPos.x && point.x < cameraPos.x + cameraSize.x)
         {
             if (point.y > cameraPos.y && point.y < cameraPos.y + cameraSize.y)
             {
                 return true;
             }
         }
         
         return false;
     }
    
 }
