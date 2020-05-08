 package org.rsbot.gui;
 
 import org.rsbot.Configuration;
 
import javax.imageio.ImageIO;
 import javax.swing.*;
 import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
 import java.util.Timer;
 import java.util.TimerTask;
 
 /**
  * @author Paris
  * @author Timer
  * @author Nader
  */
 public class Splash extends JDialog {
 	private static final long serialVersionUID = 1L;
 
 	private int display = 5000;
 
 	public Splash(final JFrame owner) {
 		super(owner);
 
 		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
 		setUndecorated(true);
 		setTitle(Configuration.NAME);
 
		final File file = new File(Configuration.Paths.Resources.SPLASH);
 
 		try {
			final BufferedImage img = ImageIO.read(file);
			setSize(img.getWidth(), img.getHeight());
			final JLabel label = new JLabel(new ImageIcon(img));
 			label.setOpaque(true);
 			label.setBackground(Color.BLACK);
 			add(label);
 		} catch (final Exception ignored) {
 			dispose();
 			return;
 		}
 	}
 
 	public void display() {
 		setLocationRelativeTo(getOwner());
 		setVisible(true);
 		final Timer timer = new Timer();
 		timer.schedule(new TimerTask() {
 			@Override
 			public void run() {
 				dispose();
 			}
 		}, display);
 	}
 }
