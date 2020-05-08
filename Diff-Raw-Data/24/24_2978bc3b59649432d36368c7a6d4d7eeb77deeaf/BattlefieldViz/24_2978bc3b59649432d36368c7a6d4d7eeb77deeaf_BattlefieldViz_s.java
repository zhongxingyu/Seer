 package edu.cs523.project3.model;
 
 //Original Realtime Rendering Source Code taken from example on Stack Overflow.
 //by Ivo Wetzel and Peter Mortensen
 //Code from http://stackoverflow.com/questions/1963494/java-2d-game-graphics
 
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.GraphicsConfiguration;
 import java.awt.GraphicsEnvironment;
 import java.awt.Point;
 import java.awt.Polygon;
 import java.awt.RenderingHints;
 import java.awt.Toolkit;
 import java.awt.Transparency;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.geom.Line2D;
 import java.awt.image.BufferStrategy;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 
 import javax.swing.JFrame;
 import javax.swing.WindowConstants;
 
 public class BattlefieldViz extends Thread {
     private boolean isRunning = true;
     private Canvas canvas;
     private BufferStrategy strategy;
     private BufferedImage background;
     private Graphics2D backgroundGraphics;
     private Graphics2D graphics;
     private JFrame frame;
     private int steps = 1;
     private boolean runs[];
     private int currentsteps = 0;
     private int width = 500;
     private int height = 500;
     private double scale = 1;
     private int q=0, k=0, i=0;
     //Define Ship
     ArrayList<Ship> ships;
     //Battlefield
     Battlefield b;
     //Ship Log
     ShipLog[][][] log;
     
     private GraphicsConfiguration config =
     		GraphicsEnvironment.getLocalGraphicsEnvironment()
     			.getDefaultScreenDevice()
     			.getDefaultConfiguration();
 
     // create a hardware accelerated image
     public final BufferedImage create(final int width, final int height,
     		final boolean alpha) {
     	return config.createCompatibleImage(width, height, alpha
     			? Transparency.TRANSLUCENT : Transparency.OPAQUE);
     }
 
     // Setup
     /**
      * Pass a battlefield into visualizer for rendering.
      * @param b The battlefield to render.
      * @param run True if the battlefield should be executed before rendering. False if already pre-rendered.
      */
     public BattlefieldViz(Battlefield b, boolean run) {
     	//Load in battlefield info.
     	this.b = b;
     	
     	//IF the run flag is set, run the simulation.
     	if(run==true) this.b.run(true);    	
     	
     	//Set local references for rendering.
     	this.ships = b.getShips();
     	this.log = b.getLog();
     	System.out.println("Beginning Render...");
     	// JFrame
     	frame = new JFrame();
     	frame.addWindowListener(new FrameClose());
     	frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
     	frame.setSize((int)(width * scale), (int)(height * scale));
     	frame.setVisible(true);
 
     	// Canvas
     	canvas = new Canvas(config);
     	canvas.setSize((int)(width * scale), (int)(height * scale));
     	frame.add(canvas, 0);
 
     	// Background & Buffer
     	background = create(width, height, false);
     	canvas.createBufferStrategy(2);
     	do {
     		strategy = canvas.getBufferStrategy();
     	} while (strategy == null);
     	
     	//Begin rendering.
     	start();
     }
 
     private class FrameClose extends WindowAdapter {
     	@Override
     	public void windowClosing(final WindowEvent e) {
     		isRunning = false;
     	}
     }
 
     // Screen and buffer stuff
     private Graphics2D getBuffer() {
     	if (graphics == null) {
     		try {
     			graphics = (Graphics2D) strategy.getDrawGraphics();
     		} catch (IllegalStateException e) {
     			return null;
     		}
     	}
     	return graphics;
     }
 
     private boolean updateScreen() {
     	graphics.dispose();
     	graphics = null;
     	try {
     		strategy.show();
     		Toolkit.getDefaultToolkit().sync();
     		return (!strategy.contentsLost());
 
     	} catch (NullPointerException e) {
     		return true;
 
     	} catch (IllegalStateException e) {
     		return true;
     	}
     }
 
     public void run() {
     	backgroundGraphics = (Graphics2D) background.getGraphics();
     	long fpsWait = (long) (1.0 / 30 * 1000);
     	//long fpsWait = (long) 0.0;
     	main: while (isRunning) {
     		long renderStart = System.nanoTime();
     		//todo: Here is where we update the graphics.
     		if(this.q < b.getRuns()){
 	    		if(this.k==b.getSteps()){
 	    			this.q++;
 	    			this.k=0;
 	    		}else{
 	    			this.k++;
	    			if(this.log[q][k][0].set==false){
	    				this.q++;
	    				this.k=0;
	    			}
 	    		}
 	    	}
    		if(this.q!= b.getRuns())updateGame();
     		else {
     			System.out.println("Finished Rendering.");
     			isRunning = false;
     		}
 
     		// Update Graphics
     		do {
     			Graphics2D bg = getBuffer();
     			if (!isRunning) {
     				break main;
     			}
     			renderGame(backgroundGraphics); // this calls your draw method
     			// thingy
     			if (scale != 1) {
     				bg.drawImage(background, 0, 0, (int)(width * scale), (int)(height * scale), 0, 0, width, height, null);
     			} else {
     				bg.drawImage(background, 0, 0, null);
     			}
     			bg.dispose();
     		} while (!updateScreen());
 
     		// Better do some FPS limiting here
     		long renderTime = (System.nanoTime() - renderStart) / 1000000;
     		try {
     			Thread.sleep(Math.max(0, fpsWait - renderTime));
     		} catch (InterruptedException e) {
     			Thread.interrupted();
     			break;
     		}
     		renderTime = (System.nanoTime() - renderStart) / 1000000;
 
     	}
     	frame.dispose();
     }
 
     public void updateGame() {
     	// update game logic here
     	for(int i=0; i<b.getCount();i++){
     	//Here we apply the log back to the ship
  			this.ships.get(i).setFromLog(this.log[q][k][i]);
    		}
     }
     /**
 	 * Draws a pie shaped outline.
 	 * @param g
 	 * @param x
 	 * @param y
 	 * @param radius
 	 * @param startAngle
 	 * @param endAngle
 	 */
 	public void drawPie(Graphics2D g, int x, int y, int radius, double startAngle, double endAngle){
 	     //System.out.println("H: " + arcLength);
 	     int x1, x2, y1, y2, x3, y3;
 	     x1 = x;
 	     y1 = y;
 
 	     int xa = (int) (Math.cos(startAngle*Math.PI)*radius);
 	     int ya = (int) (Math.sin(startAngle*Math.PI)*radius);
 	     int xb = (int) (Math.cos(endAngle*Math.PI)*radius);
 	     int yb = (int) (Math.sin(endAngle*Math.PI)*radius);
 	     	     
 	     x2 = x + xa;
 	     y2 = y - ya;
 	     x3 = x + xb;
 	     y3 = y - yb;
 	     
 	     //Calculate and draw Firing Arc
 	     g.draw(new Line2D.Double(x1, y1, x2, y2));
 	     g.draw(new Line2D.Double(x1, y1, x3, y3));
 	     double xold, yold, xnew, ynew;
 	     xold = x2;
 	     yold = y2;
 	  // for(double angle=startAngle; angle>=endAngle;angle=angle-(1.0/180)){
 	   for(double angle=startAngle; angle>=endAngle;angle=angle-0.001){
 		    	 //System.out.println(shipArcStart + " " + shipArcEnd + " " + angle);
 	    	 xnew = x + (int) (Math.cos(angle*Math.PI)*radius);
 		     ynew = y - (int) (Math.sin(angle*Math.PI)*radius);
 		     g.draw(new Line2D.Double(xold, yold, xnew, ynew));
 	    	 xold = xnew;
 	    	 yold = ynew;
 	     }
 	     g.draw(new Line2D.Double(xold, yold, x3, y3));
 
 	}
 	/**
 	 * Function drawShipBody - Draws the ship at the given co-ordinates with given facing.
 	 * @param g
 	 * @param x
 	 * @param y
 	 * @param size
 	 * @param facing
 	 * @param label
 	 */
 	public void drawShipBody(Graphics2D g, int x, int y, int size, double facing, String label){
 		double sX1, sX2, sX3, sY1, sY2, sY3;
 		sX1 = (Math.cos(facing*Math.PI)*size*1.5);
 	    sY1 = (Math.sin(facing*Math.PI)*size*1.5);
 	    sX2 = (Math.cos((facing+2.0/3.0)*Math.PI)*size);
 	    sY2 = (Math.sin((facing+2.0/3.0)*Math.PI)*size);
 	    sX3 = (Math.cos((facing-2.0/3.0)*Math.PI)*size);
 	    sY3 = (Math.sin((facing-2.0/3.0)*Math.PI)*size);
 	    int[] xs = new int[] {(int) (x + sX1), (int) (x + sX2), (int) (x + sX3)};
 	    int[] ys = new int[] {(int) (y - sY1), (int) (y - sY2), (int) (y - sY3)};
 	    g.fill(new Polygon(xs, ys, 3));
 	    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 	    g.setColor(Color.red);
 	    Font font = new Font("Courier New", Font.BOLD, size);
 	    g.setFont(font);
 	    g.drawString(label, (int) (x-2), (int) (y+2));
 	}
 	/**
 	 * Function drawShip
 	 * Draw a ship on the given Graphics2D object.
 	 * @param g
 	 * @param s
 	 */
 	public void drawShip(Graphics2D g, Ship s, String label, int boardSize, int maxRange){
 
 	     //Board Declaration
 	     //int size = boardSize/2;
 	     //int maxRange = size - 50;
 	     int shipSize = boardSize/50;
 	     
 	     
 	     if(s.getScore().energy>0){ //Enable arc drawing if ship is alive.
 		     //Loop through sensors and draw the arcs.
 		     //Draw the gun range Arc
 		     if(s.isFiring()) g.setColor(Color.red); else g.setColor(Color.gray);
 		     int gRange = (int)(s.getGun().getRange()*maxRange);
 		     double gStartArc = ((s.getFacing() + s.getGun().getFacing()) + (s.getGun().getWidth()/2));
 		     double gEndArc = ((s.getFacing() + s.getGun().getFacing()) - (s.getGun().getWidth()/2));
 		     drawPie(g, s.getLocation().x, s.getLocation().y, gRange, gStartArc, gEndArc);
 		     
 		     //Draw Sensor Arcs
 	         for(int scount = 0; scount < s.getSensors().size(); scount++){
 			     g.setColor(Color.blue);
 			     Sensor cs = s.getSensors().get(scount);
 			     if(cs.isActive()){
 			    	 //Set sensor color here. Will take care of this later.
 			    	 //TODO 
 			
 			    	 int test = s.getSensors().get(scount).getMode();
 				     if((test & Action.MOVE)==Action.MOVE){
 				    	 if(s.getSensors().get(scount).isTriggered()) g.setColor(Color.red); else g.setColor(Color.blue);
 				     }
 				     if((test & Action.FIRE)==Action.FIRE){
 				    	 if(s.getSensors().get(scount).isTriggered()) g.setColor(Color.red); else g.setColor(Color.green);
 				     }
 				     if((test & Action.LEFT)==Action.LEFT){
 				    	 if(s.getSensors().get(scount).isTriggered()) g.setColor(Color.red); else g.setColor(Color.yellow);
 				     }
 				     if((test & Action.RIGHT)==Action.RIGHT){
 				    	 if(s.getSensors().get(scount).isTriggered()) g.setColor(Color.red); else g.setColor(Color.orange);
 				     }
 				    
 			    	// g.setColor(Color.blue);
 			    	 
 			    	 //draw sensor arc
 			    	 int arcRange = (int)(cs.getArc().getRange()*maxRange);
 				     double startArc = ((s.getFacing() + cs.getArc().getFacing()) + (cs.getArc().getWidth()/2));
 				     double endArc = ((s.getFacing() + cs.getArc().getFacing()) - (cs.getArc().getWidth()/2));
 				     drawPie(g, s.getLocation().x, s.getLocation().y, arcRange, startArc, endArc);
 			     }
 		     }
 	     }
 	     //Render the Ship
 	     if(s.getScore().energy>0) g.setColor(Color.white); else g.setColor(Color.gray); //setting context
 	     if(s.isHit()) g.setColor(Color.red);
 	     drawShipBody(g, s.getLocation().x, s.getLocation().y, shipSize, s.getFacing(), label);
 	}
 
     public void renderGame(Graphics2D g) {
     	g.setColor(Color.BLACK);
     	g.fillRect(0, 0, width, height);
     	for(int i=0;i<this.ships.size(); i++){
     		drawShip(g, this.ships.get(i), (""+(i+1)), b.getSize(), b.getMaxRange());
     	}
 	     
     }
 
     public static void main(final String args[]) {
     	//ArrayList<Ship> s = new ArrayList<Ship>();
     	
     	Ship ship = new Ship();
     	Ship ship2 = new Ship();
     	Battlefield b = new Battlefield();
     	//Ship
     	ship.setFacing(0.5);									//Set ship Facing.
         ship.setLocation(new Point(250,250));
         ship.setDefaultAction(Action.MOVE);
        /* Score sc = ship.getScore();
         sc.energy = 0;
         ship.setScore(sc);
         System.out.println(ship.getScore().energy);*/
         
         Arc marcS = new Arc(0, .25, 1.0);					//sensor arc defined
 	     Sensor sensS = new Sensor(2, marcS, true);			//sensor defined
 	     sensS.setActive(true);
 	     ship2.getSensors().add(sensS);							//add sensor to ship
 	     
         
         ship2.setFacing(1.5);
         ship2.setDefaultAction(Action.RIGHT);
         ship2.setLocation(new Point(300, 200));
         Arc marc = new Arc(0, .25, 1.0);					//sensor arc defined
 	     Sensor sens = new Sensor(2, marc, true);			//sensor defined
 	     sens.setActive(true);
 	     ship.getSensors().add(sens);							//add sensor to ship
 	     
 	     Arc marc2 = new Arc(.5, 1.0, .75);					//sensor arc defined
 	     Sensor sens2 = new Sensor(4, marc2, true);			//sensor defined
 	     sens2.setActive(true);
 	     ship.getSensors().add(sens2);							//add sensor to ship
 	     
 	     Arc marc3 = new Arc(1.5, 1.0, .75);					//sensor arc defined
 	     Sensor sens3 = new Sensor(8, marc3, true);			//sensor defined
 	     sens3.setActive(true);
 	     ship.getSensors().add(sens3);							//add sensor to ship
 	     
 	     Arc marc4 = new Arc(0, .5, .875);					//sensor arc defined
 	     Sensor sens4 = new Sensor(1, marc4, true);			//sensor defined
 	     sens4.setActive(true);
 	     ship.getSensors().add(sens4);							//add sensor to ship
         //Battlefield
 	     b.getShips().add(ship);
 	     b.getShips().add(ship2);
 	     
 	    b.setCount(2);
 	    b.setMode(1);
	    b.setRuns(2);
 	    
     	new BattlefieldViz(b, true);
     }
 }
