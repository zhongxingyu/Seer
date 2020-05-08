 package particlecollisionstest;
 
 import javax.swing.*;
 
 public class TestFrame {
 	private JFrame frame;
 	private CanvasPanel canvas;
 	
 	public TestFrame () {
 		frame = new JFrame ("Collision");
 		frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
 		frame.setVisible (true);
 		
 		canvas = new CanvasPanel (400, 300, 60);
 		frame.add (canvas);
 		frame.pack ();
 		
                canvas.addParticle (new Particle (90, 150, 1, 0, 10));
		canvas.addParticle (new Particle (20, 150, 2, 0, 20));
		canvas.addParticle (new Particle (200, 150, -2, 0, 40));
 		canvas.addParticle (new Particle (380, 150, 4, 0, 20));
 		canvas.addParticle (new Particle (20, 20, 1, 1, 20));
 		canvas.startAnimations ();
 	}
 }
 
