 package org.rsbot.gui;
 
 import org.rsbot.Configuration;
 
 import javax.swing.*;
 import java.awt.*;
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
 
		final Image image = Configuration.getImage(Configuration.Paths.Resources.SPLASH);
 
 		try {
			Icon icon = new ImageIcon(image);
			setSize(icon.getIconWidth(), icon.getIconHeight());
			final JLabel label = new JLabel(icon);
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
