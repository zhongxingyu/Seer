 import java.awt.*;
 import java.awt.event.InputEvent;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.Set;
 
 import javax.imageio.ImageIO;
 
 public class Bot {
 
 	public static void main(String[] args) throws AWTException, InterruptedException 
 	{	
 		int i = 0;
 		int x = 0;
 		int y = 0;
 		int color = 0;
 		int WHITE = 240;
 		
 		int HEIGHT_OF_AREA = 250;
		int WIDTH_OF_AREA = 160;
 		int TOP_LEFT_X = 874;
 		int TOP_LEFT_Y = 550;
 		
 		int HR_ZONE = 105;
 		int SAFE_ZONE = 50;
 		
 		boolean once = false;
 		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
 		GraphicsDevice devices[] = ge.getScreenDevices();
 
 	    Robot robot = new Robot(devices[1]); 
 	    robot.mouseMove(1200,800);
 	    Rectangle screenRect = new Rectangle(TOP_LEFT_X,TOP_LEFT_Y,WIDTH_OF_AREA,HEIGHT_OF_AREA);
 	    System.out.println(InputEvent.BUTTON1_MASK);
 	    while(i < 100000)
 	    {
 	    	BufferedImage img = robot.createScreenCapture(screenRect);
 	    	for(x = 0; x < WIDTH_OF_AREA; ++x)
 	    	{
 	    		for(y = 0; y < HEIGHT_OF_AREA; ++y)
 	    		{
 	    			color = img.getRGB(x,y);
 						
 	    			if ((((color)&0xFF) > WHITE) && (((color>>8)&0xFF) > WHITE) && (((color>>16)&0xFF) > WHITE))
 	    			{
 	    				//keep Y centered so that Pooh only moves left to right.
 	    				robot.mouseMove(x+TOP_LEFT_X, TOP_LEFT_Y+150);
 	    				
 	    				i++;
 	    				if(y > HR_ZONE)
 	    				{
 	    					robot.mousePress(InputEvent.BUTTON1_MASK);
 	    					
 	    					if(!once)
 	    					{
 	    						Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
 	    						Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);
 	    						for (Thread th : threadArray)
 	    						{
 	    							System.out.println("Thread " + th.getId() + " sleeping.");
 	    							th.sleep(500);
 	    						}
 	    						once = true;
 	    						robot.mouseMove(TOP_LEFT_X + 74, TOP_LEFT_Y+150);
 	    					}
 	    				}
 	    				if(y < SAFE_ZONE)
 	    				{
 	    					robot.mouseRelease(InputEvent.BUTTON1_MASK);
 	    					once = false;
 	    				}
 	    				
 	    				//Uncomment below for image generation of homerun zone - i.e. where bot tracks ball
 	    				
	    				if(i%100==0)
	    				{
	    					File outputfile = new File("saved" + i/100 + ".png");
	    					try {
	    						ImageIO.write(img, "png", outputfile);
	    					
	    					} catch (IOException e) {
	    						e.printStackTrace();
	    					}
	    				}
 	    			}
 	    		}
 	    	}
 	    }	    	
 	 }
 	
 }
