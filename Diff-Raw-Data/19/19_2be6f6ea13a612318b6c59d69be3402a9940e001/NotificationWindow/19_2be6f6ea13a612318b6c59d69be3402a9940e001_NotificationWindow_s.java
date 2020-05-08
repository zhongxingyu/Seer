 package org.kompiro.nortification.ui;
 
 import java.awt.Color;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.net.URL;
 
 import javax.swing.JWindow;
 import javax.swing.Timer;
 
 import org.pushingpixels.trident.Timeline;
 
 public class NotificationWindow extends JWindow{
 	
 	private static final long serialVersionUID = 1L;
 	private static final int DEFAULT_DURATION = 3000;
 	private NotificationPanel contentPane;
 	private int duration = DEFAULT_DURATION;
 	
 	public NotificationWindow() {
 		contentPane = new NotificationPanel();
         contentPane.setOpaque(false);
         setContentPane(contentPane);
 	}
 	
 	public void setIconURL(final URL iconURL){
 		contentPane.setIconURL(iconURL);
 	}
 	
 	public void setTitle(final String title){
 		contentPane.setTitle(title);
 	}
 	
 	public void setMessage(final String message) {
 		contentPane.setMessage(message);
 	}
 	
 	public void setColor(final Color color){
 		contentPane.setColor(color);
 	}
 	
 	public void setDuration(int duration){
 		this.duration = duration;
 	}
 	
 	public void notifyUI(){
 		this.pack();
 		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
 		GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
 		Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
 		int x = (int)rect.getMaxX() - this.getWidth() - 50;
 		int y = (int)rect.getMaxY();
 		this.setLocation(x, y);
 		this.setVisible(true);
 
 		Timeline moveTimeline = new Timeline(this);
 		moveTimeline.addPropertyToInterpolate("location", new Point(x, y), new Point(x,y - this.getHeight() - 50));
 		Timer hideTimer = new Timer((int) (duration + moveTimeline.getDuration()), new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent event) {
 				NotificationWindow.this.setVisible(false);
 			}
 		});
 		moveTimeline.play();
 		hideTimer.start();
 	}
 	
 }
