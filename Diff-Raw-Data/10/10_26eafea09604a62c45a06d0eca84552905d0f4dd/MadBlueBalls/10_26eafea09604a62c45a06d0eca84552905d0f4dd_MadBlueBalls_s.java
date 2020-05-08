 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Random;
 
 import javax.imageio.ImageIO;
 import javax.swing.JFrame;
 
 
 public class MadBlueBalls extends JFrame {
 	VectorObject ball;
 	int diameter;
 	Image dbImage;
 	Graphics dbGraphics;
 	Vector velocity;
 	Vector gravity;
 	Vector friction;
 	Vector targetLoc;
 	Vector targetCenter;
 	BufferedImage ballImage;
 	BufferedImage Background;
 	BufferedImage grass;
 	BufferedImage target;
 	ParticleSystem explosion;
 	ParticleSystem deadBall;
 	Random rand;
 	boolean hasBounced;
 	boolean hitTarget;
 	
 	public MadBlueBalls() throws IOException
 	{
 		// Set up the ball
 		ball = new VectorObject(200,450, 650, 1.75);;
 		diameter = 30;
 		ballImage = ImageIO.read(new File("MadBlueBall.png"));
 		
 		// Set up the background
 		Background = ImageIO.read(new File("Background.png"));
 		grass = ImageIO.read(new File("grass.png"));
 		
 		// Set up the target
 		target = ImageIO.read(new File("target.png"));
 		targetLoc = new Vector(950,500);
 		targetCenter = new Vector(targetLoc.getX()+8,targetLoc.getY()+23);
 		hitTarget = false;
 		
 		gravity = new Vector(0, 9.8);
 		rand = new Random();
 		hasBounced = false;
 		
 		setSize(1200,700);
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		setVisible(true);
 		addMouseMotionListener(new MML());
 		addMouseListener(new ML());
 	}
 	
 	public void update()
 	{
 		// Only apply gravity if ball has been shot
 		if (!ball.getVelocity().isZero())
 		{
 			ball.applyForce(gravity);
 		}
 		
 		// Update the ball's location
 		ball.updateLocation();
 		
 		// Get the location
 		Vector location = ball.getLocation();
 		int x = (int) location.getX();
 		int y = (int) location.getY();
 		
 		// Detect a hit target
 		Vector distFromCent = Vector.sub(targetCenter, location);
 		if (distFromCent.magnitude()<30)
 			hitTarget = true;
 		
 		
 		// Bounce the ball if it hits the ground
 		if (y>=600)
 		{
 			ball.setLocation(new Vector(x,599));
 			ball.bounceY();
 			ball.multVelocity(0.75);
 			if (!hasBounced && !hitTarget) // Explosion
 			{
 				explosion = new ParticleSystem(x,y-10,50);
 				explosion.create(); // Should put this directly in the particlesystem constructor
 				deadBall = new ParticleSystem(x,y-10,10);
 				deadBall.create();
 				hasBounced = true;
 			}
			else if (!hitTarget) // Explosion already happened
			{
				explosion.update();
				deadBall.update();
			}
 		}
 	}
 	
 	public void paint(Graphics g)
 	{
 		dbImage = createImage(1200,700);
 		dbGraphics = dbImage.getGraphics();
 		paintComponent(dbGraphics);
 		g.drawImage(dbImage, 0, 0, this);
 	}
 	
 	public void paintComponent(Graphics g)
 	{
 		// Update the ball's location
 		update();
 		
 		// Get ball's location
 		Vector loc = ball.getLocation();
 		int x = (int) loc.getX();
 		int y = (int) loc.getY();
 		
 		// Set background
 		g.drawImage(Background, 0, 0, this);
 		
 		// Draw grass
 		for (int i=0; i<=1200; i=i+100)
 			g.drawImage(grass, i, 600, this);
 		
 		// Draw the "slingshot"
 		g.setColor(new Color(139,69,19,255));
 		g.fillRect(198, 455, 20, 170);
 		
 		/*
 		 * PUT THIS AS THE LAST THING DRAWN!
 		 */
 		// Draw the target
 		
 		int tX = (int) targetLoc.getX();
 		int tY = (int) targetLoc.getY();
 		g.drawImage(target, tX, tY, this);
 		
 		// Explosion
 		if(hasBounced)
 		{
 			ArrayList<Particle> particles = explosion.getParticles();
 			for (Particle P: particles)
 			{
 				Vector expLoc = P.getLocation();
 				int pX = (int) expLoc.getX();
 				int pY = (int) expLoc.getY();
 				g.setColor(new Color(139,69,19,255));
 				g.fillOval(pX, pY, 10, 10);
 				g.setColor(Color.BLACK);
 				g.drawOval(pX, pY, 10, 10);
 			}
 			ArrayList<Particle> deadBallParts = deadBall.getParticles();
 			for (Particle P: deadBallParts)
 			{
 				Vector expLoc = P.getLocation();
 				int pX = (int) expLoc.getX();
 				int pY = (int) expLoc.getY();
 				g.setColor(Color.BLUE);
 				g.fillOval(pX, pY, 10, 10);
 				g.setColor(Color.BLACK);
 				g.drawOval(pX, pY, 10, 10);
 			}
 		}
 		else
 		{
 			//Draw the ball
 			g.drawImage(ballImage, x-x/diameter, y-y/diameter, this);
 		}
 		
 		
 		
 		
 		// Draw the ball
 		
 		// Rotation!
 	/*	int w = ballImage.getWidth();
 		int h = ballImage.getHeight();
 		BufferedImage ball2 = new BufferedImage(w, h, ballImage.getType());
 		Graphics2D gb = ball2.createGraphics();
 		gb.rotate(Math.toRadians(10), w/2, h/2);
 		gb.drawImage(ballImage, 0, 0, this);
 		ballImage = ball2;*/
 		
 
 		
 		
 		//ballImage.createGraphics().rotate(Math.PI/2);
 		
 		repaint();
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	public static void main(String[] args) throws IOException {
 		new MadBlueBalls();
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	private class ML implements MouseListener
 	{
 
 		@Override
 		public void mouseClicked(MouseEvent arg0) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public void mouseEntered(MouseEvent arg0) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public void mouseExited(MouseEvent arg0) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public void mousePressed(MouseEvent arg0) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent e) {
 			// TODO Auto-generated method stub
 			velocity.div(10);
 			velocity.limit(10);
 			ball.setVelocity(velocity);
 		}
 		
 	}
 
 	private class MML implements MouseMotionListener
 	{
 		Vector oldLoc = ball.getLocation();
 		@Override
 		public void mouseDragged(MouseEvent mE) {
 			int x = mE.getX();
 			int y = mE.getY();
 			Vector newLoc = new Vector(x,y);
 			Vector diff = Vector.sub(newLoc, oldLoc);
 			diff.limit(100);
 			newLoc = oldLoc.get();
 			newLoc.add(diff);
 			ball.setLocation(newLoc);
 			diff.mult(-1);
 			velocity = diff;
 		}
 
 		@Override
 		public void mouseMoved(MouseEvent arg0) {
 			// TODO Auto-generated method stub
 			
 		}
 		
 	}
 }
