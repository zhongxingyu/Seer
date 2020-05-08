 package RegionBuilder;
 
 import java.awt.*;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.awt.image.BufferedImage;
 import java.awt.image.DataBufferByte;
 import java.awt.image.Raster;
 import java.nio.ShortBuffer;
 
 import javax.swing.JFrame;
 import javax.swing.WindowConstants;
 
 import org.OpenNI.License;
// import org.OpenNI.OutArg;
 import org.OpenNI.Point3D;
 import org.OpenNI.Context;
 import org.OpenNI.DepthGenerator;
 import org.OpenNI.DepthMetaData;
 import org.OpenNI.GeneralException;
 
 import ActiveSpace.Coord;
 
 public class KinectViewer extends Component 
 			implements MouseListener, WindowListener {
 
 	private int click_pos;					// pixel index of last click
 	private Coord lastClick;				// real world coordinates
 	private int cursorX, cursorY;			// screen coordinates of cursor
 	
     private Context context;				// session
     private DepthGenerator depthGen;		// depth generator
     private int width, height;				// sensor resolution
 
     private byte[] imgbytes;				// per-pixel brightness map
     private BufferedImage bimg;				// constructed display image
     private float histogram[];				// brightness correction map
     
     private int debugLevel;					// how much debug output we want
     
     private static final int MAX_Z = 10000;	// maximum distance to consider
     private static final int C_HEIGHT = 10;	// cursor height
     private static final int C_WIDTH  = 10;	// cursor width
     
 	private static final long serialVersionUID = 1L;
     
     public KinectViewer()
     {
         try {
         	// create a context (hard-coded to eliminate dependence on cfg file)
         	context = new Context();
         	License license = new License("PrimeSense", "0KOIk2JeIBYClPWVnMoRKn5cdY4=");
         	context.addLicense(license);
         	
         	// create and start a depth generator
             depthGen = DepthGenerator.create(context);
             DepthMetaData depthMD = depthGen.getMetaData();
 			context.startGeneratingAll();
             width = depthMD.getFullXRes();
             height = depthMD.getFullYRes();   
         } catch (GeneralException e) {
             e.printStackTrace();
             System.exit(1);
         }
         
         histogram = new float[MAX_Z];	// brightness correction histogram
         
         // allocate the image data capture and display buffers
         imgbytes = new byte[width*height];
         bimg = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
         DataBufferByte dataBuffer = new DataBufferByte(imgbytes, width*height);
         Raster raster = Raster.createPackedRaster(dataBuffer, width, height, 8, null);
         bimg.setData(raster);
         
         // create a new frame for the viewer
         JFrame f = new JFrame("OpenNI Depth Viewer");
         f.addMouseListener(this);			// handle mouse clicks
 		f.addWindowListener(this);			// ignore window events
 		f.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
         f.add( this );
         f.pack();
         f.setVisible(true);
         
         click_pos = -1;
         cursorX = -1;
         cursorY = -1;
     }
 	
     /**
      * return our preferred window size
      */
     public Dimension getPreferredSize() {
         return new Dimension(width, height);
     }
     
     /**
      * read a new depth frame and generate a corresponding display
      *      (and associate a depth with any recent mouse click)
      */
 	public void update() {
 		try {
 			// get a new frame of depth data
             DepthMetaData depthMD = depthGen.getMetaData();
             context.waitAnyUpdateAll();
             ShortBuffer depth = depthMD.getData().createShortBuffer();
  
             // calculate a brightness correction map
             calcHist(depth);
  
             // process the depth data into per-pixel brightnesses
             depth.rewind();
             while(depth.remaining() > 0) {
                 int pos = depth.position();		// row-by-column pixel index
                 short dist = depth.get();		// sensor reported distance
                 imgbytes[pos] = (byte)histogram[dist];
                 
                 // if this was the click location, record its coordinates
                 if (click_pos >= 0 && click_pos == pos) {
                 	cursorY = (pos / width);
                 	cursorX = (pos % width);
                 	Point3D p = new Point3D(cursorX, cursorY, dist);
                 	Point3D w = depthGen.convertProjectiveToRealWorld(p);
                 	lastClick = new Coord(w.getX(), w.getY(), w.getZ());
                 	click_pos = -1;
                 	
                 	if (debugLevel > 0) {
                 		System.out.println("Set cursor <" + cursorX + "," + cursorY + 
                 				"> = " + lastClick);
                 	}
                 }
             }
         } catch (GeneralException e) {
             e.printStackTrace();
         }
 	}
 	
 
 	/**
 	 * update the displayed image from our internal copy
 	 */
 	public void paint(Graphics g) {
 		
 		// place the cursor (if we have one)
 		if (cursorX >= 0 && cursorY >= 0) {
 			final byte BRIGHT = (byte) 255;
 			final byte DARK = (byte) 0;
 			final byte LOW = (byte) BRIGHT/3;
 			
 			for( int row = cursorY - C_HEIGHT; row <= cursorY + C_HEIGHT; row++) {
 				if (row >= 0 && row < height) {
 					int index = cursorX + (row * width);
 					imgbytes[index] = (imgbytes[index] > LOW) ? BRIGHT : DARK;
 				}
 			}
 			for( int col = cursorX - C_WIDTH; col <= cursorX + C_WIDTH; col++) {
 				if (col >= 0 && col < width) {
 					int index = col + (cursorY * width);
 					imgbytes[index] = (imgbytes[index] > LOW) ? BRIGHT : DARK;
 				}
 			}
 		}
 		
 		// turn the internal map into an image and display it
         DataBufferByte dataBuffer = new DataBufferByte(imgbytes, width*height);
         Raster raster = Raster.createPackedRaster(dataBuffer, width, height, 8, null);
         bimg.setData(raster);
         g.drawImage(bimg, 0, 0, null);
     }
 	
 	/**
 	 * calculate a brightness correction matrix
 	 * 		where full-bright is the closest report
 	 * 		and full-dark is the farthest report
 	 * 
 	 * @param depth ... ShortBuffer full of per-pixel depth reports
 	 */
 	 private void calcHist(ShortBuffer depth) {
 	        // figure out the relative frequency of each depth
 	        for (int i = 0; i < histogram.length; ++i)
 	            histogram[i] = 0;
 	        
 	        int points = 0;
 	        depth.rewind();
 	        while(depth.remaining() > 0) {
 	            short depthVal = depth.get();
 	            if (depthVal != 0) {
 	                histogram[depthVal]++;
 	                points++;
 	            }
 	        }
 	        
 	        // convert per-distance counts to cumulative (that and closer)
 	        for (int i = 1; i < histogram.length; i++) {
 	            histogram[i] += histogram[i-1];
 	        }
 
 	        // normalize counts to an 8-bit brightness scale
 	        if (points > 0) {
 	            for (int i = 1; i < histogram.length; i++) {
 	                histogram[i] = (int)(256 * (1.0f - (histogram[i] / (float)points)));
 	            }
 	        }
 	    }
 	
 	/**
 	 * if there has been a recent click in the viewer, return and clear coordinates
 	 * @return
 	 */
 	public Coord lastClick() {
 		Coord c = lastClick;
 		lastClick = null;
 		return c;
 	}
 	
 	/*
 	 * on-mouse click, note its pixel position
 	 */
 	public void mouseClicked(MouseEvent e) {
 		int x = e.getX();
 		int y = e.getY();
 		click_pos = x + (width * y);
 	}
 	
 	public void debug( int level ) {
 		debugLevel = level;
 	}
 	
 	// all the listener events we ignore
 	public void mousePressed(MouseEvent e) {}
 	public void mouseReleased(MouseEvent e) {}
 	public void mouseEntered(MouseEvent e) {}
 	public void mouseExited(MouseEvent e) {}
 	public void windowClosing(WindowEvent e) {}
 	public void windowActivated(WindowEvent arg0) {}
 	public void windowClosed(WindowEvent arg0) {}
 	public void windowDeactivated(WindowEvent arg0) {}
 	public void windowDeiconified(WindowEvent arg0) {}
 	public void windowIconified(WindowEvent arg0) {}
 	public void windowOpened(WindowEvent arg0) {}
 }
