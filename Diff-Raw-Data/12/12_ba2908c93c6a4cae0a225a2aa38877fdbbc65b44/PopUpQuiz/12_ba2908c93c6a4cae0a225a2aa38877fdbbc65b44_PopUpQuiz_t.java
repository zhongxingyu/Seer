 
 /**
  * Pop-Up-Quiz
  * This class will create the application frame.
  * @author quincy
  */
 import java.util.ArrayList;
 
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JInternalFrame;
 import javax.swing.JDesktopPane;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyListener;
 import java.awt.event.KeyEvent;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Toolkit;
 import java.awt.Dimension;
 
 import javax.swing.JOptionPane;
 
 /**
  * The main graphical class of the game.
  * @author quincy
  */
 public class PopUpQuiz extends JFrame {
 
 	/**
 	 * The default constructor. 
 	 * After calling the base constructor, it sets up listeners for
 	 * key events. 
 	 * It waits for the end of the game.
 	 * It creates a thread that updates the  HUD object.
 	 */
 	public PopUpQuiz() {
 		super();
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setLayout(null);
		setUndecorated(true);
		setResizable(false);
 
 		Container c = getContentPane();
 
 		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 		final HUD h = new HUD(screenSize);
 		c.add(h);
 		h.setLocation(0, (int) screenSize.getHeight() - HUD.taskbarHeight);
 
 		final BackgroundGame bg = new BackgroundGame(new Dimension(
 				(int) screenSize.getWidth(),
 				(int) screenSize.getHeight() - h.getTaskbarHeight()));
 		c.add(bg);
 		bg.addKeyListener(bg);
 
 		h.getStartButton().addActionListener(
 				new ActionListener() {
 
 					@Override
 					public void actionPerformed(ActionEvent arg0) {
 						bg.startGame();
 					}
 				});
 
 
 		// Loop that updates the HUD
 		(new Thread(new Runnable() {
 
 			public void run() {
 				while (true) {
 					if (bg.isStarted() && !bg.isPaused() && !bg.isOver()) {
 						h.setCpuUsage((int) bg.getCpuUsage());
 						h.setTime(System.nanoTime() - bg.getTimeGameStarted());
 						if (h.getStartButton().isEnabled()) {
 							h.getStartButton().setEnabled(false);
 						}
 					}
 					if (bg.getCpuUsage() >= 100 && bg.isOver() != true) {
 						bg.endGame();
 
 						//System.out.println("Did it");
 						h.removeAll();
 						h.validate();
 						h.repaint();
 						break;
 					}
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 					}
 
 				}
 			}
 		})).start();
 
 
 
 		validate();
 
 
 
 
 
 	}
 }
