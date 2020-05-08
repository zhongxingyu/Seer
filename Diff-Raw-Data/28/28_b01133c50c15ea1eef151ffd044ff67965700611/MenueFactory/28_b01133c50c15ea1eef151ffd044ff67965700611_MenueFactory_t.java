 package tinf11bc.kbs.yavalath.gui;
 
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 
 import tinf11bc.kbs.yavalath.logic.Yavalath;
 import tinf11bc.kbs.yavalath.logic.YavalathException;
 
 public class MenueFactory {
 
 	public MenueFactory() {
 		final JFrame mainMenuFrame = new JFrame("Settings");
 
 		mainMenuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		mainMenuFrame.setSize(300, 300);
 
 		mainMenuFrame.setLocation(100, 100);
 		mainMenuFrame.setLayout(new GridLayout(0, 1, 10, 10));
 
 		JLabel title = new JLabel("            Yavalath");
 		JButton newGame = new JButton("Start new Game");
 		JButton settings = new JButton("Settings");
 		JButton exit = new JButton("Exit");
 
 		mainMenuFrame.add(title);
 		mainMenuFrame.add(new JLabel(""));
 		mainMenuFrame.add(newGame);
 		mainMenuFrame.add(settings);
 		mainMenuFrame.add(exit);
 		mainMenuFrame.add(new JLabel(""));
 
 		mainMenuFrame.pack();
 
 		ActionListener al = new ActionListener() {
 			public void actionPerformed(ActionEvent e) {

				Thread playGroundThread = new Thread() {
					public void run() {
						mainMenuFrame.dispose();
						try {
							Yavalath.startNewGame();
						} catch (YavalathException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				};
				playGroundThread.start();

 			}
 
 		};
 		newGame.addActionListener(al);
 
 		ActionListener al2 = new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				Yavalath.showSettings();
 			}
 
 		};
 		settings.addActionListener(al2);

 		ActionListener al3 = new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.exit(0);
 			}
 
 		};
 		exit.addActionListener(al3);
 
 		mainMenuFrame.setVisible(true);
 	}
 }
