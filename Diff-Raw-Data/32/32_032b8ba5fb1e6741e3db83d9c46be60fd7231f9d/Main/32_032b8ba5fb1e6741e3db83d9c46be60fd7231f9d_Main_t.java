 //Main.java
 //holds main method, deals with ticks and rendering
 
 /*
  * Problem Mapper program
  * 
  * Controls:
  * left click - create a problem
  */
 
 import java.applet.*;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Toolkit;
 import javax.swing.*;
 
 public class Main extends Applet implements Runnable
 {
 	private static final long serialVersionUID = 8864158495101925325L;		//because stupid warnings
 	
 	public static int pixelSize = 1;										//change the scale the pixels are multiplied by when drawn to
 	
 	public static int computerSpeed = 10;		//higher number for slower computers
 	public static int tickTime = 5;
 	public static boolean isRunning = false;
 	
 	public static String windowName = "Problem Mapper";
 
 	public static boolean debugMode = true;
 	
 	public static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 	public static int screenWidth = (int)screenSize.getWidth();
 	public static int screenHeight = (int)screenSize.getHeight();
 	public static Dimension realSize;															//size of whole window
 	public static Dimension size = new Dimension(screenWidth*2/3,screenHeight*2/3);				//drawable area
 	public static Dimension pixel = new Dimension(size.width/pixelSize, size.height/pixelSize);	//"pixels" in drawable area
 	
 //	public static int camX = 0;
 	public static int camSpeed = 10;
 	
 	public static Point mse = new Point(0, 0);
 
 	public static boolean isMouseLeft = false;
 	public static boolean isMouseMiddle = false;
 	public static boolean isMouseRight = false;
 
 	private Image screen;
 	public static JFrame frame;
 	public static VisualProblemLine line;
	public static boolean drawDot = false;
 	
 	public Main()
 	{
 		setPreferredSize(size);
 	}
 
 	public static void restart()
 	{
 		Main main = new Main();
 		main.start();
 	}
 
 	public void start()
 	{
 		line = new VisualProblemLine(pixel.width, pixel.height);
 		
 		addKeyListener(new Listening());
 		addMouseListener(new Listening());
 		addMouseMotionListener(new Listening());
 		addMouseWheelListener(new Listening());
 
 		//start the main loop
 		isRunning = true;
 		new Thread(this).start();
 		requestFocus();
 	}
 
 	public void stop()
 	{
 		isRunning = false;
 	}
 
 	public static void moveCamRight()
 	{
 		if(line.width < pixel.width)
 		{
 			line.camX = 0;
 		}
 		else
 		{
 			if(line.camX+pixel.width+Main.camSpeed <= line.width)
 				line.camX += Main.camSpeed;
 			else
 				line.camX = line.width-pixel.width;
 		}
 	}
 	
 	public static void moveCamLeft()
 	{
 		if(line.width < pixel.width)
 		{
 			line.camX = 0;
 		}
 		else
 		{
 			if(line.camX-Main.camSpeed >= 0)
 				line.camX -= Main.camSpeed;
 			else
 				line.camX = 0;
 		}
 	}
 
 	public void tick()
 	{
 //		if(frame.getWidth() != realSize.width || frame.getHeight() != realSize.height)
 //			frame.pack();
 		
 		
 		line.tick();
 	}
 
 	public void render()
 	{
 		Graphics g = screen.getGraphics();
 		
 		//fill the background
 		g.setColor(new Color(230, 230, 230));
 		g.fillRect(0, 0, Main.pixel.width, Main.pixel.height);
 		
 		line.render(g);
 		
 		
		if(drawDot)
 		{
 			if(isMouseLeft)
 				g.setColor(new Color(100, 100, 100, 255/2));
 			else
 				g.setColor(new Color(180, 180, 180, 255/2));
 			
 			int circleSize = 20;
 			g.fillOval(mse.x-circleSize/2, pixel.height/2-circleSize/2, circleSize, circleSize);
 		}
 		
 		
 		g = getGraphics();
 
 		g.drawImage(screen, 0, 0, size.width, size.height, 0, 0, pixel.width, pixel.height, null);
 		g.dispose();		//throw it away to avoid lag from too many graphics objects
 	}
 
 	public void run()
 	{
 		screen = createVolatileImage(pixel.width, pixel.height);	//actually use the graphics card (less lag)
 		
 		// render();
 		// JOptionPane.showMessageDialog(null, "Controls:\n\nWho knows!?");
 		
 		while(isRunning)
 		{
 			tick();			//do math and any calculations
 			render();		//draw the objects
 
 			try
 			{
 				Thread.sleep(tickTime*(int)computerSpeed);
 			}catch(Exception e){ }
 		}
 	}
 
 	public static void main(String[] args) {
 		Main main = new Main();
 
 		frame = new JFrame();
 		frame.add(main);
 		frame.pack();
 
 		realSize = new Dimension(frame.getWidth(), frame.getHeight());
 
 		frame.setTitle(windowName);
 		frame.setResizable(false);
 		frame.setLocationRelativeTo(null);		//null makes it go to the center
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setVisible(true);
 		
 		main.start();
 	}
 }
