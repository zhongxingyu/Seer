 package particlecollisionstest;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.geom.Ellipse2D;
 import java.util.*;
 import javax.swing.JPanel;
 import javax.swing.Timer;
 
 public class CanvasPanel extends JPanel {
 	private boolean debugEnergy = true; 
 	private float width;
 	private float height;
 	private Timer animator;
 	private ArrayList <Particle> particles;
 	
 	public CanvasPanel (int width, int height, int fps) {
 		this.width = (float) width;
 		this.height = (float) height;
 		this.setPreferredSize (new Dimension (width, height));
 		particles = new ArrayList <> ();
 		animator = new Timer (1000/fps, new ActionListener () {
 			@Override
 			public void actionPerformed (ActionEvent e) {
 				for (Particle p : particles) {
 					updateParticle (p);
 				}
 				repaint();
 			}
 		});
 	}
 	
 	public void startAnimations () {
 		animator.start ();
 	}
 	
 	public void stopAnimations () {
 		animator.stop ();
 	}
 	
 	public void addParticle (Particle p) {
 		particles.add (p);
 	}
 	
 	private float CalculateSystemEnergy () {
 		float ke = 0;
 		for (Particle tmp : particles) {
 			float m = tmp.getR () * tmp.getR ();
 			ke += 0.5 * m * tmp.getVelocity ().magnitude * tmp.getVelocity ().magnitude;
 		}
 		return ke;
 	}
 	
 	private void logEnergyDebug (String msg) {
 		if (debugEnergy) {
 			System.out.println ("[Energy]" + msg + " = " + CalculateSystemEnergy ());
 		}
 	}
 	
 	private void updateParticle (Particle p) {
 		float newX = p.getNewX ();
 		float newY = p.getNewY ();
 		
 		//Boundries
 		if (newX - p.getR () < 0 || newX + p.getR () > width) {
 			p.setVelocity (-p.getVelocity ().dx, p.getVelocity ().dy);
 		}
 		if (newY - p.getR () < 0 || newY + p.getR () > height) {
 			p.setVelocity (p.getVelocity ().dx, -p.getVelocity ().dy);
 		}
 		
 		/*newX = p.getNewX ();
 		newY = p.getNewY ();
 		
 		while (newX - p.getR () < 0 || newX + p.getR () > width || newY - p.getR () < 0 || newY + p.getR () > height) {
 			newX = p.getNewX ();
 			newY = p.getNewY ();
 			p.setPosition (newX, newY);
 			updated = true;
 		}*/
 		
 		//Collision culling here
 		//...
 		
 		//Collision
 		for (Particle o : particles) {
 			if (p != o) {
 				if (p.collidesWith (o)) {
 					System.out.println ("==================================");
 					//System.out.println ("Collision!");
 					logEnergyDebug ("Initial");
 					
 					p.setPosition (p.getX () - p.getVelocity ().dx, p.getY () - p.getVelocity ().dy);
 					o.setPosition (o.getX () - o.getVelocity ().dx, o.getY () - o.getVelocity ().dy);
 					float pMass = p.getR () * p.getR (); 
 					float oMass = o.getR () * o.getR ();
 					float phi;
 					
 					//Get angle shift needed for collision to be 1D
 					if (p.getX () - o.getX () == 0) {
 						phi = (float) Math.PI / 2;
 					}
 					else {
 						phi = (float) Math.atan ((p.getY () - o.getY ()) / (p.getX () - o.getX ()));
 					}
 					System.out.println ("phi = " + phi);
 					
 					//Transform velocities to rotated coordinate system
 					Vector2D pVelRefInit = new Vector2D (
 						p.getVelocity ().magnitude * (float) Math.cos (p.getVelocity ().angle - phi),
 						p.getVelocity ().magnitude * (float) Math.sin (p.getVelocity ().angle - phi));
 					
 					Vector2D oVelRefInit = new Vector2D (
 						o.getVelocity ().magnitude * (float) Math.cos (o.getVelocity ().angle - phi),
 						o.getVelocity ().magnitude * (float) Math.sin (o.getVelocity ().angle - phi));
 					
 					if (debugEnergy) {
 						p.setVelocity (pVelRefInit);
 						o.setVelocity (oVelRefInit);
 					}
 					logEnergyDebug ("After transform");
 					
 					//Calculate new velocities based on 1D collision
 					Vector2D pVelRefFinal = new Vector2D (
 						(pVelRefInit.dx * (pMass - oMass) + 2 * oMass * oVelRefInit.dx) / (pMass + oMass),
 						pVelRefInit.dy);
 					
 					Vector2D oVelRefFinal = new Vector2D (
 						(oVelRefInit.dx * (oMass - pMass) + 2 * pMass * pVelRefInit.dx) / (pMass + oMass),
 						oVelRefInit.dy);
 					
 					if (debugEnergy) {
 						p.setVelocity (pVelRefFinal);
 						o.setVelocity (oVelRefFinal);
 					}
 					logEnergyDebug ("After 1D collision calculation");
 					
 					//Transform velocities back to original system
 					Vector2D pVelFinal = new Vector2D (
						(float) (pVelRefFinal.dx * Math.cos (phi) + pVelRefFinal.dy * Math.sin (phi)),
 						(float) (pVelRefFinal.dx * Math.sin (phi) + pVelRefFinal.dy * Math.cos (phi)));
 					
 					Vector2D oVelFinal = new Vector2D (
						(float) (oVelRefFinal.dx * Math.cos (phi) + oVelRefFinal.dy * Math.sin (phi)),
 						(float) (oVelRefFinal.dx * Math.sin (phi) + oVelRefFinal.dy * Math.cos (phi)));
 					
 					p.setVelocity (pVelFinal);
 					o.setVelocity (oVelFinal);
 					
 					p.setPosition (p.getNewX(), p.getNewY ());
 					o.setPosition (o.getNewX(), o.getNewY ());
 					
 					logEnergyDebug ("After Collision");
 					System.out.println ("==================================");
 					
 					if (p.collidesWith (o)) {
 						System.out.println ("oh no");
 					}
 				}
 			}
 		}
 		
 		//Done
 		p.setPosition (p.getNewX (), p.getNewY ());
 	}
 	
 	@Override
 	public void paintComponent (Graphics g) {
 		super.paintComponent (g);
 		Graphics2D g2 = (Graphics2D) g;
 		
 		if (particles.size () > 0) {
 			for (int i = 0; i < particles.size (); i++) {
 				Particle p = particles.get (i);
 				g2.draw (new Ellipse2D.Float (p.getX () - p.getR (), p.getY () - p.getR (), p.getR () * 2, p.getR () * 2));
 			}
 		}
 	}
 }
 
