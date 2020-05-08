 package graphics;
 
 import javax.swing.*;
 
 import java.awt.*;
 import java.awt.image.BufferStrategy;
 
 import core.engine.Land;
 import core.engine.Input;
 import core.entities.GameEntity;
 
 @SuppressWarnings("serial")
 public class Renderer extends Canvas{
     private JFrame frame;
     private BufferStrategy buffer;
     private Graphics g;
     private Graphics2D g2D;
 
     public final int width;
     public final int height;
     //constructor
     public Renderer(Input inp){
 	width = 800;
 	height = 600;
 	frame = new JFrame("OOTanks");
 	//get content of the frame, determine size
 	JPanel panel = (JPanel) frame.getContentPane();
 	panel.setPreferredSize(new Dimension(width,height));
 	panel.setLayout(null);
 
 	//set up canvas and boundaries, add panel to 'this' canvas
 	setBounds(0,0,width,height);
 	panel.add(this);
 
 	//we will paint manually, so
 	setIgnoreRepaint(true);
 	frame.setIgnoreRepaint(true);
 
 	frame.pack();
 	frame.setResizable(false);
 	frame.setVisible(true);
 
 	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 	//creating buffering strategy
 	createBufferStrategy(2);
 	buffer = getBufferStrategy();
 
 	//handle input init. Sadly, we need to you this out of our canvas
 	setKeyListener(inp);
     }
 
     //function to draw tank at (x,y) and rotate it by an angle in degrees
     /**
      * Draws a tank
      * @param x X centre coordinate of the tank
      * @param y Y centre coordinate of the tank
      * @param angle
      */
     public void drawTank(int x,int y,double angle, int id){
 	  	int w = 60;
 	  	int h = 30;
 		g2D.translate(x,y);
 	  	g2D.rotate(angle);
 	
 	 	g2D.setColor(Color.darkGray);
 	  	g2D.drawRect(-w/2, -h/2, w, h);
 	  	g2D.fillRect(-w/2, -h/2, w, h);
 		g2D.setColor(Color.BLUE);
 		g2D.fillArc(-20,-10,20,20,0,360);
 	  	g2D.setColor(Color.RED);
 	  	g2D.drawRect(-w/2+30, -h/2+12, 21, 6);
 	  	g2D.fillRect(-w/2+30, -h/2+12, 21, 6);
 		g2D.rotate(-angle);
 		g2D.translate(-x,-y);
 
     }
     /**
      * Draws a projectile
      * @param x X centre coordinate of the Projectile
      * @param y Y centre coordinate of the Projectile
      * @param angle Angle of the Projectile
      */
     public void drawShell(int x, int y, double angle, int id){
 		int d = 8;
 		g2D.translate(x,y);
 		g2D.rotate(angle);
 		
 		g2D.setColor(Color.BLACK);
 		g2D.fillArc(-d/2, -d/2, d, d, 0, 360);	
 		g2D.rotate(-angle);
 		g2D.translate(-x,-y);
     }
     /**
      * Initialises the renderer, loads resources.
      */
     public void init(){
 	//load resources here
     }
     
     /**
      * Renders the game each frame
      * @param map map that the objects will be taken from
      */
     public void draw(double x, double y, double width, double height, double angle, int id){
     	if (id  < 10)
     		drawTank((int)x,(int)y,angle,id);
     	else if (id < 20)
     		drawShell((int)x,(int)y,angle,id);
     }
     public void update(Land map){
 	//reset the graphics
 	g = null;
 	g2D = null;
 
 	// get ready to draw
 	g = buffer.getDrawGraphics();
 
 	//creating a java 2D graphic object
 	g2D = (Graphics2D) g;
 
 	//fill background to green (Green for no reason)
 	g2D.setColor(new Color(0,150,0));
 	g2D.fillRect(0,0,width,height);
 
 	//drawing will be done here
 
 	for (GameEntity e : map.gameEntities){
 		draw(e.getX(),e.getY(),e.getWidth(),e.getHeight(),e.getAngle(), e.getId());
 	}
 	// Garbage {
 	/* graphic.setColor(Color.blue);
 
 	int thickness = 4;
 	//not the most wise thing to do
 	for (int i = 0; i <= thickness; i++)
 	    graphic.draw3DRect(600 - i, 510 - i, 80 + 2 * i, 30 + 2 * i, true); //use tabbing, especially when not using {}
 
 	//same as above
 	for (int i = 0; i < thickness; i++)
 	    graphic.draw3DRect(600 - i, 550 - i, 80 + 2 * i, 30 + 2 * i, false); //use tabbing, especially when not using {}
 
 	int height = 200;
 	int width = 120;
 
        	graphic.setColor(Color.red);
       	graphic.drawRect(10, 10, height, width);
 
       	graphic.setColor(Color.gray);
       	graphic.fillRect(10, 10, height, width);
 
       	graphic.setColor(Color.red);
      	graphic.drawOval(250, 250, height, width);
 
      	graphic.setColor(Color.getYellow);
      	graphic.fillOval(250, 250, height, width);
        */ 
 	// }
 	//end of drawing

 	//syncs everything to smooth java frames
 	Toolkit.getDefaultToolkit().sync();
 	if(!buffer.contentsLost()){
 	    buffer.show();
 	} else {
 	    System.out.println("Data Lost in buffer");
 	}
     }
     /**
      * Releases used resources
      */
     public void release(){
 
     }
     /**
      * Function for input handler and to sync Canvas 
      * @param inp input handler
      */
     public void setKeyListener(Input inp) {
 	addKeyListener(inp);
        	requestFocus();
     }
 };
