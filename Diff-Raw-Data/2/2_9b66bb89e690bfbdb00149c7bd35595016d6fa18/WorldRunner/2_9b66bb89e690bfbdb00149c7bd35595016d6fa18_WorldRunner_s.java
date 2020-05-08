 //1 = 1 foot 5280 feet = 1 mile
 // average walk speed = 4mph
 // beginning run speed = 9 mph
 
 import java.awt.AWTException;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Point;
 import java.awt.Robot;
 import java.awt.Toolkit;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.image.BufferedImage;
 
 import javax.media.opengl.GLCapabilities;
 import javax.media.opengl.GLProfile;
 import javax.swing.JFrame;
 
 import com.jogamp.opengl.util.FPSAnimator;
 /**
  * This program makes the shape rotate change and explode + many other things
  * 
  * @author Daniel Kendix (dkendix@gmail.com)
  */
 public class WorldRunner{
 	private JFrame frame ;
 	private World canvas;
 	private FPSAnimator animator;
 	private BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB); //Blank Image to make cursor disappear
 
 	// Create a new blank cursor.
 	private Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor"); //Blank cursor stuff
 	private int screenX =0; //Top Left of screen coordinates
 	private int screenY = 0;
 	private int sizeX = Toolkit.getDefaultToolkit().getScreenSize().width/4*3; //Screen Size
 	private int sizeY =Toolkit.getDefaultToolkit().getScreenSize().height/4*3;
 	private int rX = screenX+sizeX/2;
 	private int rY = screenY+sizeY/2;
 
 	/**
 	 *  Constructor for everything
 	 */
 	Robot robo; //Robots can set the mouse position
 	public WorldRunner(){
 				
 		frame= new JFrame("Hello World!");
 		try {
 			robo = new Robot();
 
 		} catch (AWTException e1) {
 			System.out.println("ROBOT CREATING FAILED");
 		}
 		frame.getContentPane().setCursor(blankCursor); //Sets the blank cursor
 		GLCapabilities capabilities=new GLCapabilities(GLProfile.getDefault());
 		//capabilities.setDoubleBuffered(true);
 		canvas=new  World (capabilities); //creates a world
 		animator = new FPSAnimator(canvas, 60);
 		animator.start();
 
 		frame.addMouseListener(new MouseListener() {
 			public void mousePressed(MouseEvent e) {
 
 			}
 
 			public void mouseReleased(MouseEvent e) {
 
 			}
 
 			public void mouseEntered(MouseEvent e) {
 
 			}
 
 			public void mouseExited(MouseEvent e) {
 
 			}
 
 			public void mouseClicked(MouseEvent e) {
 
 			}
 		});
 		frame.addMouseMotionListener(new MouseMotionListener() {
 			public void mouseMoved(MouseEvent e)
 			{
 
 				canvas.p.setViewRotx((double)(canvas.p.getViewRotx())+(double)(e.getXOnScreen()-rX)/10);
 				canvas.p.setViewRoty((double)(canvas.p.getViewRoty())-(double)(e.getYOnScreen()-rY)/10);
 				robo.mouseMove(rX, rY); //Resets mouse to window center after moving
 
 			}
 			public void mouseDragged(MouseEvent e)
 			{
 
 			}
 		});
 		frame.addMouseWheelListener(new MouseWheelListener() {
 
 			@Override
 			public void mouseWheelMoved(MouseWheelEvent e) {
 
 			}
 		});
 		frame.addKeyListener(new KeyListener() {
 			@Override
 			public void keyTyped(KeyEvent e) {
 
 			}
 
 			@Override
 			public void keyReleased(KeyEvent e) {
 
 				char c = e.getKeyChar();
 				switch (c) {
 				case 'w':
 				case 's':
 					canvas.p.incrz = 0;
 					break;
 				case 'a':
 				case 'd':
 					canvas.p.incrx = 0;
 					break;
 				}
 			}
 
 			@Override
 			public void keyPressed(KeyEvent e) {
 				int move = 1;
 				if(e.getKeyChar()=='f')
 				{
 					if(canvas.p.fly == 0)
 						canvas.p.fly = 1;
 					else
 						canvas.p.fly = 0;
 				}
 				if(e.getKeyChar()=='w')
 				{
 					canvas.p.incrz = move;
 				}
 				if(e.getKeyChar()=='s')
 				{
 					canvas.p.incrz = -move;
 				}
 				if(e.getKeyChar()=='a')
 				{
 					canvas.p.incrx = -move;
 				}
 				if(e.getKeyChar()=='d')
 				{
 					canvas.p.incrx = move;
 				}
 				if(e.getKeyCode()==e.VK_SPACE)
 				{
 					if(canvas.p.jump == 0)
 					{
 						canvas.p.vel[1] = 10;
 						canvas.p.jump = 1;
 					}
 				}
 				if(e.getKeyCode()==e.VK_ESCAPE) {
 					xseton();
 					System.exit(0);
 				}
 			}
 		});
 		setUpFrame();
 	}
 
 	public static void xseton() {
 		try{
			Runtime.getRuntime().exec("xset r off");
 		} catch(Exception e) {}
 	}
 	
 	/**
 	 * Main function which will create a new AWT Frame and add a JOGL Canvas to
 	 * it
 	 * 
 	 * @param args Runtime args
 	 */
 	public static void main(String[] args) {
 		// HACK to fix autorepeat on x11; other workarounds are unreliable
 		// Note the "xset r on" in exit routines
 		try{
 			Runtime.getRuntime().exec("xset r off");
 		} catch(Exception e) {}
 		new WorldRunner();
 
 	}
 
 	public void setUpFrame()
 	{
 		frame.setSize(sizeX, sizeY);
 		frame.setBackground(Color.white);
 
 		frame.addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				xseton();
 				System.exit(0);
 			}
 		});
 
 		frame.setVisible(true);
 		frame.setLocation(screenX, screenY);
 	}
 }
