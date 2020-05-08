import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.MouseInfo;
 import java.awt.Point;
 import java.awt.PointerInfo;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.Timer;
 
 public class ParticleSystem {
 	
 	public static void main(String s[])
 	{
 		JFrame frame = new JFrame("Staart van Particles");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		JPanel panel = new ParticleSystemPanel();
 		
 		frame.getContentPane().add(panel);
 		frame.pack();
 		frame.setVisible(true);
 	}
 }
 
 
 class ParticleSystemPanel extends JPanel implements ActionListener, MouseListener, MouseMotionListener {
 	
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -8271945508104460591L;
 	private int x=0,y=0;
 	private ArrayList<Particle> particles = new ArrayList<Particle>();
 	private boolean drukMuis = false; 
 	
 	/* Constructor */
 	public ParticleSystemPanel()
 	{
		setBackground(Color.black);
 		setPreferredSize( new Dimension(640,480));
 		for(int idx = 0; idx < 2; idx++)
 		{
 			particles.add(new Particle());
 		}
 		
 		Timer timer = new Timer(1000/30, this);
 		timer.start();
 		addMouseListener(this);
 		addMouseMotionListener(this);
 		
 		new Timer(1000/200, new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				if(drukMuis)particles.add(new Particle(x,y));
 				if(drukMuis)particles.add(new Particle(x,y));
 				}
 		}).start();
 	}
 
 	// Timer, action performed. Update hier objects en/of world
 	// en dan issue een repaint()
 	public void actionPerformed(ActionEvent arg0)
 	{
 		for( Iterator<Particle> itr = particles.iterator(); itr.hasNext(); )
 		{
 			Particle k = itr.next();
 
 			if(k.y < -2000.0)
 			{
 				itr.remove();
 			}
 			else
 			{
 				k.update();
 			}
 		}
 		
 //		if( kogels.size() < 500)
 //		{
 //			kogels.add(new Kogel());
 //		}
 		
 		repaint();
 	}
 
 	 
 	
 	public int muisX()
 	{
 		PointerInfo piepMuis = MouseInfo.getPointerInfo();
 		Point puntMuis = piepMuis.getLocation();
 		int muisX = (int) puntMuis.getX();
 		return muisX;
 	}
 	
 	public int muisY()
 	{
 		PointerInfo piepMuis = MouseInfo.getPointerInfo();
 		Point puntMuis = piepMuis.getLocation();
 		int muisY = (int) puntMuis.getY();
 		return muisY;
 	}
 	
 	// Hier alleen tekenen ! Nooit een repaint() !!
 	public void paintComponent(Graphics g)
 	{
 		super.paintComponent(g);
 		Graphics2D g2 = (Graphics2D)g;
 				
 		// 0-punt verlegging
 		//g2.translate(0,getHeight());
 		//g2.scale(1,-1);
 		
 		// Center screen
 		//g2.translate(getWidth()/2, getHeight()/2);
 		//g2.scale(1, 1);
 		
 		for(Particle k : particles)
 		{
 			k.draw(g);
 		}
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent arg0) {
 //		int x = arg0.getX();
 //		int y = arg0.getY();
 //		kogels.add(new Kogel(x,y));
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		
 		if(e.getButton() == 3)drukMuis = true;
 		 x = e.getX();
 		 y = e.getY();
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		if(e.getButton() == 3)drukMuis = false;
 	}
 
 	@Override
 	public void mouseDragged(MouseEvent arg0) {
 		 x = arg0.getX();
 		 y = arg0.getY();
 //		kogels.add(new Kogel(x,y));
 //		kogels.add(new Kogel(x,y));
 //		kogels.add(new Kogel(x,y));
 //		kogels.add(new Kogel(x,y));
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent arg0) {
 //		System.out.println("Muis is op, X: "+x+" Y: "+y);
 		
 	}
 }
 
 
