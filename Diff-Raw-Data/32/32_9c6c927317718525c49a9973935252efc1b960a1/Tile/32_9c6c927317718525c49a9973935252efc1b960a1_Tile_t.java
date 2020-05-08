 package general;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
 import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
 import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
 import java.util.HashMap;
 import java.util.Map;
 
 
 public class Tile{
 
     //private TileType type;
     private Rectangle blocks;
     private Image tileImg;
    // private final int arrayNum = 500;
     Map<String,Image> type = new HashMap<String,Image>();
     //coords of 1 tile
     private int x, y;
    
    public static int tileSize = 15;
 
     public Tile(int coordX, int coordY)
     {
         type.put(TileType.HORIZONTAL.toString(),new ImageIcon(getClass().getResource("/general/img/horizontal.png")).getImage());
         type.put(TileType.VERTICAL.toString(),new ImageIcon(getClass().getResource("/general/img/vertical.png")).getImage());
         type.put(TileType.UPPIN.toString(),new ImageIcon(getClass().getResource("/general/img/pintop.png")).getImage());
         type.put(TileType.DOWNPIN.toString(),new ImageIcon(getClass().getResource("/general/img/pinbot.png")).getImage());
         type.put(TileType.LEFTBOTCORNER.toString(),new ImageIcon(getClass().getResource("/general/img/lbcorner.png")).getImage());
         type.put(TileType.LEFTTOPCORNER.toString(),new ImageIcon(getClass().getResource("/general/img/ltcorner.png")).getImage());
         type.put(TileType.RIGHTTOPCORNER.toString(),new ImageIcon(getClass().getResource("/general/img/rtcorner.png")).getImage());
         type.put(TileType.RIGHTBOTCORNER.toString(),new ImageIcon(getClass().getResource("/general/img/rbcorner.png")).getImage());
         this.x = coordX;
         this.y = coordY;
     }
 
     public void draw(Graphics g)
     {
             blocks = new Rectangle(x,y,10,10);
             g.drawImage(this.tileImg, blocks.x, blocks.y,null);
     }
 
     public void setType (TileType newType)
     {
        this.tileImg = type.get(newType.toString());
       this.tileImg = resizeImage(tileImg, tileSize, tileSize);
    }
    
    private Image resizeImage(Image originalImage, int biggerWidth, int biggerHeight) {
        int type = BufferedImage.TYPE_INT_ARGB;


        BufferedImage resizedImage = new BufferedImage(biggerWidth, biggerHeight, type);
        Graphics2D g = resizedImage.createGraphics();

        g.setComposite(AlphaComposite.Src);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.drawImage(originalImage, 0, 0, biggerWidth, biggerHeight, null);
        g.dispose();


        return resizedImage;
     }
     
     public void makeTextTile(String string)
     {
     	 int fontSize = (int) (tileSize*0.75);
     	 //create String object to be converted to image
          //String sampleText = Character.toString(c);
          
          //create the font you wish to use
          Font font = new Font("Sans", Font.PLAIN, fontSize);
          
          //create the FontRenderContext object which helps us to measure the text
          FontRenderContext frc = new FontRenderContext(null, true, true);
           
          //get the height and width of the text
          Rectangle2D bounds = font.getStringBounds(string, frc);
          int w = (int) bounds.getWidth();
          int h = (int) bounds.getHeight();
          
          //create a BufferedImage object
          BufferedImage image = new BufferedImage(tileSize, tileSize,   BufferedImage.TYPE_INT_ARGB);
          
          //calling createGraphics() to get the Graphics2D
          Graphics2D g = image.createGraphics();
          
          //set color and other parameters
          g.setColor(Color.BLACK);
          g.setFont(font);
               
          g.drawString(string, (float) bounds.getX(), (float) -bounds.getY());
          //releasing resources
          g.dispose();
         
          this.tileImg = image;
     }
 }
