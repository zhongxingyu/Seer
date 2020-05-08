 // Animator.java
 // Jovan Ristic, March 2009
 
 package twosnakes;
 
 import java.io.*;
 import java.awt.*;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.image.*;
 
 import javax.imageio.ImageIO;
 import javax.swing.JFrame;
 
 public class Animator
 {
 	// -------------- variables --------------
 	// length of the entire animation, length of the segments
 	protected int duration, period;
 	// whether animation is looping
 	protected boolean looping;
 	// current frame, how many frames there are
 	protected int frame, frames;
 	// height and width of a single frame of the image
 	private int height, width;
 	// time when animation started and how far along it is
 	private long startTime, nowTime, timePassed;
 	// the current frame of animation and the entire image strip
 	private BufferedImage currentFrame, fullImage;
 	// used to transcribe one image to the other
 	private Graphics2D g2d;
 
 	
 	// -------------- constructors -----------
 	public Animator()
 	{
 		
 	}
 	
 	// -------------- methods ----------------
 	//starts a new animation
 	public void startAnimation(String filename, int duration, int frames, boolean looping)
 	{
 		//Load the image
 		BufferedImage image;
 		try {
 	           image = ImageIO.read(new File(filename));
 	       } catch (IOException e) {
 	    	   image = null;
 	    	   System.out.println("Image failed to load.");
 	       }
 		
 		
 		startTime = System.nanoTime();
 		this.duration = duration;
 		this.frames = frames;
 		this.looping = looping;
 		period = (int)(duration/frames);
 		fullImage = image;
 		height = fullImage.getHeight();
 		width = (fullImage.getWidth() - (frames - 1))/frames;
 		currentFrame = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
 	}
 	
 	public void stopAnimation() // stop the current animation
 	{
 		
 	}
 	
 	public BufferedImage getCurrentFrame() //returns the current frame of animation based on time passed, frames and duration
 	{
 		frame = 0;
 		currentFrame = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
 		g2d = currentFrame.createGraphics();
 		nowTime = System.nanoTime();
 		timePassed = (nowTime - startTime)/1000000; // returns time passed in miliseconds
 		if (timePassed > duration)
 		{
 			if (looping)
 			{
 				startTime = System.nanoTime() - 1;
 				timePassed = (nowTime - startTime)/1000000;
 			}
 			else
 				stopAnimation();
 		}
 		for (int i = 0 ; i < frames ; i++)
 		{
 			if (timePassed >= (i+1)*period)
 				frame++;
 			else
 				break;
 		}
 		int x = -(width+1)*frame;
 		g2d.drawImage(fullImage, x, 0, null);
 		g2d.dispose();
 		return currentFrame;
 	}
 	
 	public static void main(String args[])
 	{
 		JFrame f = new JFrame("Load Image Sample");
 		        
		        f.addWindowListener(new WindowAdapter(){
		                public void windowClosing(WindowEvent e) {
		                    System.exit(0);
		                }
		            });
 		        
 	    Animator animator = new Animator();
 	    animator.startAnimation("spritesheet.png", 700, 70, true);
 	    
 	    f.pack();
 	    f.setVisible(true);
 	    Graphics g = f.getGraphics();
 	    while(true)
 	    {
 	    	g.clearRect(0, 0, 200, 200);
 	    	g.drawImage(animator.getCurrentFrame(), 0, 30, null);
 	    }
 	}
 }
