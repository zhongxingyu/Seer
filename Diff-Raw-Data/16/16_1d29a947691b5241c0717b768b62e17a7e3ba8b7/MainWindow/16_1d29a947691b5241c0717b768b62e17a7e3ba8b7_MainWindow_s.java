 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package GUI;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 import javax.swing.JFrame;
 import javax.swing.JLayeredPane;
 import playback.Player;
 
 /**
  *
  * @author Niels Visser
  */
 public class MainWindow extends JFrame {
 	public MainWindow(Player p) {
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		GridPanel gf = new GridPanel(p);
 		ButtonPanel bp = new ButtonPanel(p, gf);
 		this.fullScreen();
 		gf.setBounds(0, 0, this.getWidth(), this.getHeight());
 		bp.setBounds(0, 0, this.getWidth(), this.getHeight());
		this.getLayeredPane().add(gf, new Integer(5));
		this.getLayeredPane().add(bp, new Integer(10));
 	}
 
 	private void fullScreen() {
 		this.setResizable(false);
 		if (!this.isDisplayable()) {
 			// Can only do this when the frame is not visible
 			this.setUndecorated(true);
 		}
 		GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
 		GraphicsDevice gd = devices[devices.length-1];
 		try {
 			if (gd.isFullScreenSupported()) {
 				gd.setFullScreenWindow(this);
 			} else {
 				// Can't run fullscreen, need to bodge around it (setSize to screen size, etc)
 			}
 			this.setVisible(true);
 			// Your business logic here
 		}
 		finally {
 		}
 
 	}
 
 	public void setParticlePanel(ParticlePanel pp) {
 		pp.setBounds(0, 0, getWidth(), getHeight());
		pp.setPreferredSize(this.getSize());
 		pp.init();
		this.getLayeredPane().add(pp, new Integer(0));
 	}
 }
