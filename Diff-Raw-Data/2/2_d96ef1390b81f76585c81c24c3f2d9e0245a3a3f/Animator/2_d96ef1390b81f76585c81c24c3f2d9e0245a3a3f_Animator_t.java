 // Animator.java
 // Jovan Ristic, March 2009
 
package twosnakes;

 import java.io.*;
 import java.awt.*;
 import java.awt.image.*;
 
 public class Animator
 {
 	// -------------- variables --------------
 	// length of the entire animation, length of the segments
 	private int duration, period;
 	// whether animation is looping
 	private boolean looping;
 	// current frame, how many frames there are
 	private int frame, frames;
 	// height and width of a single frame of the image
 	private int height, width;
 	// time when animation started and how far along it is
 	private long startTime, nowTime, timePassed;
 	// the current frame of animation and the entire image strip
 	private BufferedImage currentFrame, fullImage;
 	// used to transribe one image to the other
 	private Graphics2D g2d;
 	//enum that holds the current state of animation
 	public enum Animation
 	{
 		RUNNING_LEFT, RUNNING_RIGHT, ATTACKING, JUMPING, SPINNING, IDLE
 	}
 	public Animation anim;
 	
 	// -------------- constructors -----------
 	public Animator()
 	{
 		anim = Animation.IDLE;
 	}
 	
 	// -------------- methods ----------------
 	//starts a new animation
 	public void startAnimation(BufferedImage image, int duration, int frames, boolean looping, Animation anim)
 	{
 		startTime = System.nanoTime();
 		this.duration = duration;
 		this.frames = frames;
 		this.looping = looping;
 		this.anim = anim;
 		period = (int)(duration/frames);
 		fullImage = image;
 		height = fullImage.getHeight();
 		width = (fullImage.getWidth() - (frames - 1))/frames;
 		currentFrame = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
 	}
 	
 	public void stopAnimation() // stop the current animation
 	{
 		anim = Animation.IDLE;
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
 }
