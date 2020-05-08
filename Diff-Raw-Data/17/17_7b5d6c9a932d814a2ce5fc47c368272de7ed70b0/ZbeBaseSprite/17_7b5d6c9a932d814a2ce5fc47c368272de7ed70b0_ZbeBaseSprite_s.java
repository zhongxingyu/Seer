 package zbecreate.sprite;
 
 import java.awt.Graphics;
 import java.awt.geom.Point2D;
 import javax.swing.JPanel;
 
 /**
  * @author Dan
  */
 
 //! This class is meant to be derived for the several different
 //! types of sprites in the zoidbergengine. It is most anaglous to the
 //! 'object' class within the program.
 
 public abstract class ZbeBaseSprite extends JPanel{
 
     protected Point2D.Double position;
     protected java.awt.Image myImage;
     protected int tileWidth;
     protected int tileHeight;
     
 
     public ZbeBaseSprite(){
         tileWidth  = 0;
         tileHeight = 0;
         position = new Point2D.Double();
     }
 
     public void loadImage(){
         String location = "";
 
         try{
         location = new java.io.File(".").getCanonicalPath();
         }
         catch(Exception ex){
             ex.printStackTrace();
             System.exit(1);
         }
 
         location += "/src/zbecreate/resources/about.png";
         System.out.println(location);
         myImage = java.awt.Toolkit.getDefaultToolkit().getImage(location);
        tileWidth = 20;
        tileHeight = 20;
         System.out.printf("tileH = %d\nTileW = %d\n", tileHeight, tileWidth);
     }
 
     public java.awt.Image getImage(){
         return this.myImage;
     }
 
     public void deleteImage(){
         this.myImage = null;
     }    
 
     public void setPosition(int x, int y){
         this.position.setLocation(x,y);
     }
 
     public int getXPosition(){
         return (int)position.x;
     }
 
     public int getYPosition(){
         return (int)position.y;
     }
 
     public int getTileWidth(){ return this.tileWidth; }
     public int getTileHeight(){ return this.tileHeight; }
 
     @Override
     public void paintComponents(Graphics g) {
         super.paintComponents(g);
 
         if( myImage != null)
             g.drawImage(myImage, tileWidth, tileHeight, this);
     }
 
     public abstract void exportXML();
     
 }
