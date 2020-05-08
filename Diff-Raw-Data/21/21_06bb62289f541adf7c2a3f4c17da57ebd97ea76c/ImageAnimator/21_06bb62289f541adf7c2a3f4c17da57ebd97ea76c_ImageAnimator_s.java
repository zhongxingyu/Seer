 package javachallenge.graphics.util;
 
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: LGM
  * Date: 2/27/13
  * Time: 6:21 PM
  */
 public class ImageAnimator extends Thread
 {
 	protected ArrayList<AnimatedImage> images=new ArrayList<AnimatedImage>();
 	protected int sleepTime;
 	protected boolean pause=false;
 
 	public boolean isPause()
 	{
 		return pause;
 	}
 
 	public void setPause(boolean pause)
 	{
 		this.pause = pause;
 	}
 
 	public ImageAnimator(int sleepTime)
 	{
 		this.sleepTime = sleepTime;
 	}
 	public void add(AnimatedImage image)
 	{
 		synchronized (images)
 		{
 			images.add(image);
 		}
 	}
 	public void garbageCollector()
 	{
		ArrayList<AnimatedImage> temp=new ArrayList<AnimatedImage>();
 		synchronized (images)
 		{
			for (AnimatedImage image:images)
				if (image.isDestroyed()==false)
					temp.add(image);
			images.clear();
			images=temp;
 		}
 	}
 	public void run()
 	{
 		while (true)
 		{
 			if (pause==false)
 			{
 				synchronized (images)
 				{
 					for (AnimatedImage image:images)
 						if (image.isDestroyed()==false)
 							image.next();
 				}
 				garbageCollector();
 			}
 			try
 			{
 				sleep(sleepTime);
 			} catch (InterruptedException e)
 			{
 				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
 			}
 		}
 	}
 }
