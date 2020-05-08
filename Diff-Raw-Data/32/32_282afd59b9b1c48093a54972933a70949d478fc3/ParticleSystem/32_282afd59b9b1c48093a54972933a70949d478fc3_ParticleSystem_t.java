 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
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
 import java.util.List;
 
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
 	
 	private static final long serialVersionUID = -8271945508104460591L;
 	private int x=0,y=0;
 	private List<Particle> particles = new ArrayList<Particle>();
 	private boolean drukMuis = false; 
 	private int particleCount;
 //	private javax.swing.JDialog error;
 	private FPScounter fpsCounter;
 	
 
 	
 	/* Constructor */
 	public ParticleSystemPanel()
 	{
 		fpsCounter = new FPScounter();
 		setBackground(Color.black);
 		setPreferredSize( new Dimension(900,700));
 		
 		Timer timer = new Timer(1000/30, this);
 		timer.start();
 		addMouseListener(this);
 		addMouseMotionListener(this);
 		gameDo();
 	}
 
 	private void gameDo() {
 
 		new Timer(1000/200, new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				if(drukMuis) {					 
 				particles.add(new Particle(x,y));
 				particles.add(new Particle(x,y));
 				particleCount+=2;
 				}
 				fpsCounter.tick();
 			}
 		}).start();
 
 		
 		
 	}
 
 	public void actionPerformed(ActionEvent arg0)
 	{
 		
 		for( Iterator<Particle> itr = particles.iterator(); itr.hasNext(); )
 		{
 			Particle k = itr.next();
 
			if(particleCount > 748)
 			{
 				itr.remove();
 				particleCount-=1;
 			}
 			else
 			{
 				k.update();
 			}
 		}
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
 	
 	public void paintComponent(Graphics g)
 	{
 		super.paintComponent(g);
 		g.setColor(Color.green);
 		g.drawString(fpsCounter.tellFPS(), 1, 10);
 		g.drawString("Het aantal paricles is: "+particleCount,1, 30);
 		for(Particle k : particles)
 		{
 			k.draw(g);
 		}
 	}
 	
 
 	@Override
 	public void mouseClicked(MouseEvent arg0) {
 //		if(arg0.getButton() == 1)JOptionPane.showMessageDialog(error, "Het aantal paricles is: "+particleCount ,"Hoeveel particles zijn er?", JOptionPane.QUESTION_MESSAGE);;
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent arg0) {
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
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
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent arg0) {
 		
 	}
 	
 
     
 }
 
 
