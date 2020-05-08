 package menu;
 
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.util.Stack;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 /**
  * The root frame of the game. Presents all the menus, and can show the game.
  *
  * @author muruphenr , antunomate , richarhayd
  *
  */
 public class MainFrame extends JFrame {
 	Stack<JPanel> frameStack;
 	public final Thread musicThread = new Thread(
             new Runnable() {
                 public void run() {
                     try {
                    	new AudioPlayer("timer1.wav", true);//TODO to be replaced with menuMusic.wav when it's written
                     } catch (Exception e) {
                         e.printStackTrace();
                     }
                 }
             });
 
 	public MainFrame() {
 		musicThread.start();
 		frameStack = new Stack<JPanel>();
 		frameStack.add(new MainMenuPanel(this));
 		this.add(frameStack.peek());
 		this.setResizable(false);
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
 		GraphicsDevice[] devices = ge.getScreenDevices();
 		for (int i = 0; i < 1; i++) {
 			if (devices[i].isFullScreenSupported()) {
 				this.setUndecorated(true);
 				devices[i].setFullScreenWindow(this);
 				//this.setSize(this.getToolkit().getScreenSize());
 				this.validate();
 				//this.setVisible(true);
 			}
 		}
 
 		this.addWindowListener(new WindowListener() {
 
 			@Override
 			public void windowClosed(WindowEvent e) {
 				musicThread.stop();
 				System.out.println("closinbg");
 
 			}
 
 			@Override
 			public void windowOpened(WindowEvent e) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void windowClosing(WindowEvent e) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void windowIconified(WindowEvent e) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void windowDeiconified(WindowEvent e) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void windowActivated(WindowEvent e) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void windowDeactivated(WindowEvent e) {
 				// TODO Auto-generated method stub
 
 			}
 		});
 	}
 
 	public static void main(String args[]) {
 		MainFrame f = new MainFrame();
 
 	}
 
 	/**
 	 * Add another JPanel. The current JPanel is stored.
 	 *
 	 * @param comp
 	 */
 	public void addMenu(JPanel comp) {
 		if (frameStack.size() > 0)
 			this.remove(frameStack.peek());
 		frameStack.add(comp);
 		this.add(comp);
 		comp.requestFocus();
 		this.validate();
 		this.repaint();
 	}
 
 	/**
 	 * Return to previous JPanel.
 	 */
 	public void back() {
 		if (frameStack.size() == 1)
 			return;
 
 		this.remove(frameStack.pop());
 		this.add(frameStack.peek());
 		this.validate();
 		this.repaint();
 	}
 }
