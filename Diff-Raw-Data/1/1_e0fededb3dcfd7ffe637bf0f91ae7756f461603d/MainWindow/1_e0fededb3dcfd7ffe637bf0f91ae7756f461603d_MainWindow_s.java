 package client.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Cursor;
 import java.awt.DisplayMode;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.Window;
 import java.awt.event.ActionEvent;
 import java.awt.image.BufferedImage;
 import javax.swing.AbstractAction;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.KeyStroke;
 
 public class MainWindow extends JFrame {
 
 	private static GraphicsDevice device;
 	private JPanel panel;
 	private static final long serialVersionUID = -4902720969305740099L;
 
 	public MainWindow(JPanel panel) {
 		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
 		device = ge.getDefaultScreenDevice();
 		this.panel = panel;
 		this.setTitle("Picture Browser");
 		this.setLayout(new BorderLayout());
 		this.add(panel, BorderLayout.CENTER);
 		this.setKeyBinding();
 		this.hideCursor();
 	}
 	
 	private void setKeyBinding() {
 		panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE,0),"escapeDown");
 		panel.getActionMap().put("escapeDown",new AbstractAction() {
 		    private static final long serialVersionUID = 1l;
 		    @Override public void actionPerformed(ActionEvent e) {
 		        endFullScreenMode();
 		    }
 		});
 	}
 	
 	public void setFullScreenMode(DisplayMode dm){
 		this.setUndecorated(true);
 		this.setResizable(false);
 		device.setFullScreenWindow(this);
 		if(device.isDisplayChangeSupported() && dm!=null){
 			try{
 				device.setDisplayMode(dm);
 			}
 			catch(Exception ex){}
 		}
 
 	}
 	
 	public void endFullScreenMode(){
 		Window w = device.getFullScreenWindow();
 		if(w != null){
 			w.dispose();
 		}
 		device.setFullScreenWindow(null);
 	}
 	
 	public void hideCursor() {
 		// Transparent 16 x 16 pixel cursor image.
 		BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
 
 		// Create a new blank cursor.
 		Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
 		    cursorImg, new Point(0, 0), "blank cursor");
 
 		// Set the blank cursor to the JFrame.
 		this.getContentPane().setCursor(blankCursor);
 	}
 }
