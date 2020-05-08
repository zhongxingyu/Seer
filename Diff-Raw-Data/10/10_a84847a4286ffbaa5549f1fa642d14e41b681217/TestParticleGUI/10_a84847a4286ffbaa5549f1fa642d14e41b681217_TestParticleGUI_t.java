 package testParticleSystem;
 
 import java.awt.Color;
 import java.awt.Graphics;
 
 import javax.swing.JFrame;
 
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.awt.event.MouseAdapter;
 
 public class TestParticleGUI extends JFrame {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private PMgnt pm;
 	private int radius;
 	private Events event_handler;
 	
 	public TestParticleGUI(PMgnt pm, int radius){
 		addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				event_handler.openPharmacyPanel();
 			}
 		});
 		this.pm = pm;
 		event_handler = new Events(pm);
 		getContentPane().addMouseMotionListener(new MouseMotionAdapter() {
 			@Override
 			public void mouseDragged(MouseEvent arg0) {
 				event_handler.move(arg0.getX(), arg0.getY());
 				repaint();
 			}
 		});
 		
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.radius = radius;
 	}
 	
 	public void paint(Graphics g) {
         super.paint(g);
         g.setColor(Color.green);
         for(Particle p:pm.particlesystem){        
         	g.fillOval(p.getLocation(0), p.getLocation(1), radius, radius);
         }
     }
 	
 	public void setParticleMgnt(PMgnt pm){
 		this.pm = pm;
 	}
 
 }
