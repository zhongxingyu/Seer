 import javax.swing.JApplet;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Color;
 import java.awt.Point;
 import java.awt.Dimension;
 import java.awt.Rectangle;
 import java.awt.geom.Point2D;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import javax.swing.event.MouseInputAdapter;
 import javax.swing.JComponent;
 import java.util.Arrays;
 
 
 /**
  * An interactive Applet that displays the Mandelbrot set. When the mouse is
  * clicked, a rectangle can be drawn. A second click causes the Applet to
  * zoom in such that it now displays the area inside the rectangle at full size.
  * If the SHIFT key was pressed during the first click, the Applet instead zooms
  * out and displays the part of the complex plane that previously filled the
  * whole applet inside the rectangle.
  *
 * @author Michael Borgwardt <brazzy@gmail.com>
  */
 public class MandelApplet extends JApplet implements Runnable{
 
   /** Holds the colors */
   private int[] palette;
 
   /** Number of completed lines after which the screen is updated. */
   private int stepping = 5;
 
   /** Image used for drawing pixels. */
   private BufferedImage img;
 
 
 
   // Applet status
 
   /** Indicates whether the Applet is alive or in the process of shutting down. */
   private volatile boolean live;
 
   /** Indicates whether the Applet is stopped or running. */
   private volatile boolean stop;
 
   /** Indicates whether the Image is complete or unfinished */
   private volatile boolean done;
 
 
 
   // Data for one complete image
 
   /** Dimension of the Applet in pixels. */
   private Dimension appletDim;
 
   /** coordinates within the complex plane of the left upper Applet corner. */
   private Point2D.Double leftUpper;
 
   /** Width of the applet in the complex plane. */
   private double size;
 
   /** Width of an applet pixel in the complex plane. */
   private double step;
 
   /** Maximum number of iterations (pixel is considered part of the Mandelbrot
    *  set if this is exceeded). */
   private int threshold;
 
 
 
   // Data for the treatment of one pixel
 
   /** x number of the pixel being currently processed. */
   private int pixelX;
 
   /** y number of the pixel being currently processed. */
   private int pixelY;
 
   /** x coordinate within the complex plane of the pixel being processed. */
   private double complexX;
 
   /** y coordinate within the complex plane of the pixel being processed. */
   private double complexY;
 
 
   /**
    * Initializes the important variables in the beginning and whenever
    * the user has zoomed in or out.
    *
    * @param leftUpper new value for {@link #leftUpper}
    * @param size new value for {@link #size}
    */
   private void initVars(Point2D.Double leftUpper, double size){
     this.leftUpper=leftUpper;
     this.size=size;
     complexX = this.leftUpper.x;
     complexY = this.leftUpper.y;
     step = this.size/appletDim.width;
 
     // A rule-of-thumb approximation to get a sufficient iteration threshold
     // relative to the resolution.
     threshold = (int)Math.ceil(20+100*(2-(Math.log(step*500)/Math.log(5))));
     pixelX=0;
     pixelY=0;
   }
 
   /**
    * Initializes the Applet.
    */
   public void init(){
     stop = true;
     live = true;
     done = false;
     appletDim = new Dimension(getBounds().width, getBounds().height);
     int colorNum = 256;
     String param = getParameter("colors");
     if(param!=null){
       colorNum = Integer.parseInt(param);
     }
     img = new BufferedImage(appletDim.width, appletDim.height, BufferedImage.TYPE_INT_RGB);
 
     // Initializes the variables in a way such that a rectangle in the complex
     // plane with its upper left corner at (-2,1.5) and a width and height of 3
     // fits completely into the applet, no matter what the Applet's aspect ratio
     // is, and the entire Apfelmnnchen can be seen.
     if(appletDim.width > appletDim.height){
       double diff = (((double)appletDim.width-appletDim.height)*3)/(2*appletDim.height);
       initVars(new Point2D.Double((double)(-diff-2.0),(double)1.5),(double)appletDim.width*3/appletDim.height);
     } else {
       double diff = (((double)appletDim.height-appletDim.width)*3)/(2*appletDim.width);
       initVars(new Point2D.Double((double)-2.0,(double)(diff+1.5)),(double)3.0);
     }
 
     // Create a nice-looking palette by having the red, blue and green
     // part follow sine, cosine and inverted cosine curves.
     palette = new int[colorNum];
     int red,blue,green;
     for(int i=0;i<colorNum;i++){
       red  =(int)Math.floor((Math.sin(Math.PI*2/colorNum*i)+1)*128*0.999);
       blue =(int)Math.floor((Math.cos(Math.PI*2/colorNum*i)+1)*128*0.999);
       green=(int)Math.floor((1-Math.cos(Math.PI*2/colorNum*i))*128*0.999);
       palette[i] = new Color(red,green,blue).getRGB();
     }
 
     // prepare user interaction
     GlassPane pane = new GlassPane(this);
     setGlassPane(pane);
     MouseListener ml = new MouseListener(this);
     addMouseListener(ml);
 
     (new Thread(this)).start();
   }
 
   /**
    * Starts or unpauses the Applet.
    */
   public void start(){
     GlassPane pane = (GlassPane)getGlassPane();
     pane.setOpaque(true);
     pane.setVisible(true);
     stop = false;
     synchronized(this){
       notify(); // ends the wait() in the main loop.
     }
   }
 
   /**
    * Stops (pauses) the Applet.
    */
   public void stop(){
     stop = true; // Causes the main loop to wait.
   }
 
   /**
    * Causes the Applet to die.
    */
   public void destroy(){
     live = false;
   }
 
   /**
    * Paints the Applet.
    */
   public void paint(Graphics g){
     g.drawImage(img, 0, 0, this);
   }
 
   /** Resizes the applet. */
   public void resize(Dimension d){
     if(appletDim != null && !d.equals(appletDim)){ // avoid initial resize that happens before init()
       synchronized(img){
         stop = false;         //  \
         live = true;          //  } Cause computation to restart
         done = false;         //  /
         appletDim = new Dimension(d.width, d.height);
         img = new BufferedImage(appletDim.width, appletDim.height, BufferedImage.TYPE_INT_RGB);
         double newSize = size*d.width/appletDim.width;
         initVars(leftUpper, newSize);
       }
     }
     super.resize(d.width, d.height);
   }
 
   /** Resizes the applet. */
   public void resize(int width, int height){
     resize(new Dimension(width, height));
   }
 
   /**
    * Main loop of the Applet.
    */
   public void run(){
 
     // The actual main loop, ends when destroy() was called.
     // Note that each run of the loop computes only one pixel,
     // and after each run, there is an opportunity to zoom.
     while(live){
 
       // Spinlock to test whether computation is wanted and needed.
       while(stop || done){
         synchronized(this){
           try{
             wait(10000);
           } catch(InterruptedException ex){;}
         }
       }
 
       // Necessary to prevent a zoom request and the actual computation
       // from interfering with each other.
       synchronized(img){
         boolean in = false;
         boolean out = false;
         int iterations = 0;
         double iterationX = 0;
         double iterationY = 0;
 
         // The actual computation to decide whether the current pixel is
         // inside or outside the Mandelbrot set.
         do{
           iterations++;
           double xSquared = iterationX*iterationX;
           double ySquared = iterationY*iterationY;
           iterationY = 2*iterationX*iterationY+complexY;
           iterationX = xSquared - ySquared + complexX;
           in = iterations>threshold; // pixel is considered part of the M set.
           out = xSquared+ySquared > 4; // pixel is not part of the M set.
         } while (!in && !out); // iterate until decision is made
 
         // draw pixel
         if(in){
           img.setRGB(pixelX,pixelY,0);
         } else {
           img.setRGB(pixelX,pixelY, palette[iterations%palette.length]);
         }
 
         complexY -= step; // increase y coordinate
         if(++pixelY<appletDim.height){
           continue; // advance to vertically next pixel
         }
 
         // If this is reached, the current x column is finished.
         pixelY=0;
         complexY = leftUpper.y; // reset y coordinate;
         complexX += step; // increase x coordinate
         if(++pixelX<appletDim.width){
           if(pixelX%stepping==0){
             getGlassPane().repaint();
           }
           continue; // advance to next x column.
         }
 
         // If this is reached, the image is complete and computation can be
         // paused until a zoom request happens.
         getGlassPane().repaint();
         stop = true;
         done = true;
       }
     }
   }
 
   /**
    * Enables zooming in and out with the mouse.
    */
   private class MouseListener extends MouseInputAdapter implements MouseMotionListener{
 
     /** Reference to the enclosing applet, for synchronization and drawing the zoom */
     private JApplet applet;
 
     /** Indicates whether a zoom rectangle is being drawn. */
     private boolean zooming = false;
 
     /** Indicates whether the SHIFT key was pressed and thus a zoom-out
      * should be performed. */
     private boolean zoomout = false;
 
     /** Coordinates of the upper left corner of the first mouse click. */
     private Point firstClick = new Point();
 
     MouseListener(JApplet applet){
       this.applet = applet;
     }
 
     /** Mouse click prepares and performs a zoom. */
     public void mouseClicked(MouseEvent e){
       if(!zooming){ // prepare zoom
         zooming = true;
         firstClick.x = e.getX();
         firstClick.y = e.getY();
         zoomout = e.isShiftDown();
         addMouseMotionListener(this);
       } else { // perform zoom
         zooming = false;
         removeMouseMotionListener(this);
         ((GlassPane)getGlassPane()).box = new Rectangle();
 
         double clickDiffX = Math.abs(firstClick.x-e.getX());
         double clickDiffY = Math.abs(firstClick.y-e.getY());
 
         // Compute the width of the rectangle, considering that it should have
         // the same aspect ratio as the applet, thus we need to constrain the
         // rectangle.
         int zoomRectWidth = (int)Math.min(clickDiffX, clickDiffY*appletDim.width/appletDim.height);
 
         synchronized(img){ // Prevents interference with the main loop.
           stop = false;         //  \
           live = true;          //  } Cause computation to restart
           done = false;         //  /
           double newSize;
           Point2D.Double newLeftUpper;
           if(!zoomout){ // zoom in
             // New (after the zoom) value of {@link #size}.
             newSize = size * zoomRectWidth / appletDim.width;
             newLeftUpper = new Point2D.Double(
               leftUpper.x+(size*firstClick.x/appletDim.width),
               leftUpper.y-(size*firstClick.y/appletDim.width)
             );
           } else { // zoom out
             newSize = size / zoomRectWidth * appletDim.width;
             newLeftUpper = new Point2D.Double(
               leftUpper.x-(newSize*firstClick.x/appletDim.width),
               leftUpper.y+(newSize*firstClick.y/appletDim.width)
             );
           }
           initVars(newLeftUpper, newSize);
         }
         synchronized(applet){
           applet.notify();
         }
       }
     }
 
     /** Mouse movement shapes zoom rectangle. */
     public void mouseMoved (MouseEvent e){
       if(zooming){ // only draw while zoom is being prepared.
         Graphics g = getGraphics();
         g.drawImage(img, 0, 0, applet); // redraw image to remove previous rectangle
         g.setColor(Color.white);
         double clickDiffX = Math.abs(firstClick.x-e.getX());
         double clickDiffY = Math.abs(firstClick.y-e.getY());
 
         // Compute the width of the rectangle, considering that it should have
         // the same aspect ratio as the applet, thus we need to constrain the
         // rectangle.
         int zoomRectWidth = (int)Math.min(clickDiffX, clickDiffY*appletDim.width/appletDim.height);
 
         // draw rectangle
         GlassPane pane = (GlassPane)getGlassPane();
         pane.box.x = firstClick.x;
         pane.box.y = firstClick.y;
         pane.box.width = zoomRectWidth;
         pane.box.height = appletDim.height*zoomRectWidth/appletDim.width;
         pane.repaint();
       }
     }
 
     /** Mouse movement shapes zoom rectangle. */
     public void mouseDragged(MouseEvent e){
         mouseMoved(e);
     }
 
     /** Display help text. */
     public void mouseEntered(MouseEvent e){
       showStatus("Click, drag & click to zoom in, press SHIFT to zoom out.");
     }
   }
 
   private class GlassPane extends JComponent{
     Rectangle box = new Rectangle();
     private JApplet applet;
     GlassPane(JApplet applet){
       this.applet = applet;
     }
     public void paint(Graphics g){
       applet.paint(g);
       g.setColor(Color.white);
       g.drawRect(box.x, box.y, box.width, box.height);
     }
   }
 
 }
