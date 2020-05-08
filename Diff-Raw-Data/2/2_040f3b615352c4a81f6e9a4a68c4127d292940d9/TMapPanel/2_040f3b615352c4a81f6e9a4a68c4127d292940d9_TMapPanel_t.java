 import javax.swing.JPanel;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 
 /** A panel to display our MBTA map */
 public class TMapPanel extends JPanel implements Runnable {
 	/** Default constructor */
 	public TMapPanel() {
 
 		// Set our preferred size
 		setPreferredSize( new Dimension(400, 400) );
 
 		// Get the focus
 		setFocusable( true );
 		requestFocusInWindow();
 
 		// Start our main loop
 		new Thread(this).start();
 	}
 
 	@Override
 	/** Our main loop */
 	public void run() {
 		// Set our thread priority
 		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
 		while( true ) {
 
 			// Make sure we're valid before trying to display ourself
 			if( isValid() ) {
 				render();
 				display();
 			}
 
 			// Go to sleep for a bit
 			try {
 				Thread.sleep(5);
 			} catch (InterruptedException ex) { }
 
 			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
 		}
 	}
 	
 	/** Render our buffer image */
 	private void render() {
 		// First set everything up...
 		
 		// If we don't have a buffer image, create one
 		if( bufferImage == null ) {
 			bufferImage= createImage( getSize().width, getSize().height );
 			
 			// If we couldn't create a buffer image, let the world know.
 			if( bufferImage == null ) {
 				System.out.println( "Failed to create buffer Image..." );
 				return;
 			}
 			
 			// Otherwise associate it with our graphics object.
 			imageGraphics= (Graphics2D)bufferImage.getGraphics();
 			imageGraphics.setBackground( Color.black );
 			imageGraphics.setClip(0, 0, getSize().width, getSize().height);
 		}
 		
 		// Do actual rendering here...
 		
 		// Fill the screen with our background color
 		imageGraphics.setColor( imageGraphics.getBackground() );
 		imageGraphics.fillRect(0, 0, getSize().width, getSize().height);
 	}
 	
 	/** Draw our buffer image on the screen */
 	private void display() {
 		// A reference to our screen graphics object
 		Graphics screenGraphics;
 		
 		// Try to set up the reference...
 		try {
 			// Get the reference
 			screenGraphics= this.getGraphics();
 			
 			// And use it to draw our buffer image
 			if ((screenGraphics != null) && (bufferImage != null))
 				screenGraphics.drawImage(bufferImage, 0, 0, null);
 			
 			// Clean up
 			screenGraphics.dispose();
 		} catch(Exception e) {
 			System.out.println("Exception in display()! "+ e);
 		}
 	}

 	/** Our buffered image */
 	private Image bufferImage;
 	/** A reference to our buffer image's graphics object */
 	private Graphics2D imageGraphics;
 }
