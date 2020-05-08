 import java.awt.Graphics2D;
 
 import java.awt.image.BufferedImage;
 import javax.imageio.ImageIO;
 import java.io.File;
 import java.io.IOException;
 import java.util.Random;
 
 
 /**
  * The Planet class is constructed with the x and y coordinates, as well as a radius.
  */
 public class Planet implements Position //Drawable
 {
	//Top left corner coordinates.
     public int x;
     public int y;
    
    //Center coordinates.
    private int xCenter,yCenter;
    
     public int radius;
     public int ships;
     private BufferedImage image;
    
     //Number of planet images we have.
     public static int imagesNum=18;
     //The planet images.
     public static BufferedImage[] images;
     
     
     //Randomness.
     private Random random;
     
     //Reads all the planet images from their image files.
     static
     {
         images=new BufferedImage [imagesNum];
         for(int i=1;i<=imagesNum;i++)
         {
             try
             {
                 images[i-1] = ImageIO.read(new File("resources/images/planets/planet"+i+".png"));
             }
             catch (IOException e)
             {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         }
     }
     
     BufferedImage resize(BufferedImage originalImage, int r)
     {
     	BufferedImage scaledBI = new BufferedImage(r*2, r*2, BufferedImage.TYPE_INT_ARGB);
     	Graphics2D g = scaledBI.createGraphics();
     	g.drawImage(originalImage, 0, 0, r*2, r*2, null); 
     	g.dispose();
     	return scaledBI;
     }
     
     public Planet(int x, int y, int radius)
     {
 	 this.x = x;
 	 this.y = y;
	 xCenter=x+radius;
	 yCenter=y+radius;
 	 this.radius = radius;
 	 
 	 int i;
 	 random=new Random();
 	 i=random.nextInt(imagesNum);
 	 image=resize(images[i],radius);
     }
     
     public Planet(int x, int y, int radius, int i)
     {
 	 this.x = x;
 	 this.y = y;
	 xCenter=x+radius;
	 yCenter=y+radius;
 	 this.radius = radius;
 	 image=resize(images[i],radius);
     }
 
     /**
      * @return the `x` co-ordinate of the planet.
      */
     public int getX()
     {
         return x;
     }
 
     
     /**
      * @return the `y` co-ordinate of the planet.
      */
     public int getY()
     {
         return y;
     }
     
    public int getXCenter()
    {
    	return xCenter; 
    }
    public int getYCenter()
    {
    	return yCenter;
    }
    
     public double distanceFrom(Position p) {
         
         int dx = this.getX() - p.getX();
         int dy = this.getY() - p.getY();
         
         return Math.sqrt( (dx * dx) + (dy * dy) );
     }
     
     /**
      * @return the radius of the planet.
      */
     public int getRadius()
     {
         return radius;
     }
 
 
     /**
      * @return the number of ships present on the planet.
      */
     public int getShips()
     {
         return ships;
     }
     
     public BufferedImage getImage()
     {
      return image;
     }
 
     /**
      * @return draw the planet.
      */
     /*public void draw(Graphics2D g2d)
     {
         g2d.drawImage(this.bufferedImage, this.getX(), this.getY(), null);
     }*/
     
 
    /* public void setImage(String path) {
         try {
             File f = new File(path);
             this.bufferedImage = ImageIO.read(f);
         } catch (IOException e) {
         // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }*/
     
     /**
      * @return a boolean stating whether or not the given coordinate is 'inside' the
      * planet. Can be used for testing mouse-click-selection.
      */
     public boolean isCoordinateInside(int x, int y)
     {
         //Is the coordinate's distance from the center coordinate less than (or equal) the radius?
        return (xCenter-x)*(xCenter-x)+(yCenter-y)*(yCenter-y)<=radius*radius;
     }
 	
 }
