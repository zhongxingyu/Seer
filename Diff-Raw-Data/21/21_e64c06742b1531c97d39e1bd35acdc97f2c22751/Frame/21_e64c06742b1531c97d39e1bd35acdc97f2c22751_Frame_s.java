 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 
 import javax.swing.JFrame;
 import javax.swing.WindowConstants;
 
 
 /**
  * The frame for the application, it has a paint loop and guarentees a specified
  * fps
  *
  * @author matthewmercer.
  *         Created Apr 18, 2012.
  */
 public class Frame extends JFrame implements Runnable{
 
 	/**
 	 *  The elements for use throughout the game
 	 */
	enum element { 	/**  The elemnt Fire  */ FIRE, 
 					/**  The elemnt Water  */ WATER, 
 					/**  The elemnt Light  */ LIGHT, 
 					/**  The elemnt Earth  */ EARTH, 
 					/**  The elemnt Air  */ AIR }
 	private int fps;
 	private Thread thr;
 	private int frameSkips;
 	private int sleepSkips;
 	private int requestedFPS;
 	
 	
 	/**
 	 * creates the frame, setting it to be the size of the screen and 
 	 * setting the requested fps
 	 *
 	 * @param fps FPS to run the paint loop
 	 */
 	public Frame(int fps){
 		this.requestedFPS = fps;
 		this.thr = new Thread(this);
 		this.setSize(java.awt.Toolkit.getDefaultToolkit().getScreenSize());
 		this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
 		this.setVisible(true);
 		this.toBack();
 		this.thr.start();
 		
 	}
 	
 	@Override
 	public void paint(Graphics arg){
 		Graphics2D g = (Graphics2D) arg;
 		g.setColor(g.getBackground());
 		g.fillRect(0,0,this.getWidth(),this.getHeight());
 		g.setColor(Color.BLACK);
 		for(int i = 0; i < 100; i++){
 			g.drawLine(0, i * (this.getHeight()/100), i * (this.getWidth()/100), this.getHeight());
 			g.drawLine(0, (100-i) * (this.getHeight()/100), i * (this.getWidth()/100), 0);
 			g.drawLine(this.getWidth(), i * (this.getHeight()/100), (100-i)* (this.getWidth()/100), this.getHeight());
 			g.drawLine(this.getWidth(), (100-i) * (this.getHeight()/100), (100-i) * (this.getWidth()/100), 0);
 		}
 	}
 	
	@Override
 	public void run(){
 		long time;
 		long timeDiff;
 		
 		while(true){
 			time = System.currentTimeMillis();
 			repaint();
 			timeDiff = System.currentTimeMillis() - time;
 			if(timeDiff < (1000 / this.requestedFPS)){
 				try{Thread.sleep((1000/this.requestedFPS) - timeDiff);}
 				catch(InterruptedException e){
 					e.printStackTrace();
 				}
 			}
 			
 			
 			
 			else{
 				if(this.sleepSkips < 10){
 					this.sleepSkips++;
 				}
 				else{
 					this.frameSkips++;
 					this.sleepSkips = 0;
 					try{Thread.sleep(10);}
 					catch(InterruptedException e){
 						e.printStackTrace();
 					}
 				}
 			}
 			time = System.currentTimeMillis() - time;
 			System.out.println(time);
 			if(time != 0)
 				this.fps = (int) (1000 / time);
 		}
 	}
 	
 
 	/**
 	 * Returns the value of the field called 'fps'.
 	 * @return Returns the fps.
 	 */
 	public int getFPS() {
 		return this.fps;
 	}
 	
 	/**
 	 * Returns the value of the field called 'requestedFPS'.
 	 * @return Returns the fps.
 	 */
 	public int getRequestedFPS() {
 		return this.requestedFPS;
 	}
 
 	/**
 	 * Returns the value of the field called 'frameSkips'.
 	 * @return Returns the frameSkips.
 	 */
 	public int getFrameSkips() {
 		return this.frameSkips;
 	}
 
 	/**
 	 * Returns the value of the field called 'sleepSkips'.
 	 * @return Returns the sleepSkips.
 	 */
 	public int getSleepSkips() {
 		return this.sleepSkips;
 	}
 	
 	
 }
