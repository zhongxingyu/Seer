 package org.libreSubsApplet;
 
 import java.awt.Graphics;
 import java.net.URL;
 
 import javax.swing.ImageIcon;
 import javax.swing.JTextArea;
 
 @SuppressWarnings("serial")
 public class JTextAreaWithBackground extends JTextArea {
 	
	private final ImageIcon background;
 
 	public JTextAreaWithBackground() {
 		setOpaque(false);
 		final URL resource = JTextAreaWithBackground.class.getResource("/applet.png");
		background = new ImageIcon(resource);
 	}
 	
 	@Override
 	public void paintComponent (final Graphics g)
     {
        g.drawImage(background.getImage(), 0, 0, (int)getSize().getWidth(), (int)getSize().getHeight(), this);
         super.paintComponent(g);
     }
 
 }
