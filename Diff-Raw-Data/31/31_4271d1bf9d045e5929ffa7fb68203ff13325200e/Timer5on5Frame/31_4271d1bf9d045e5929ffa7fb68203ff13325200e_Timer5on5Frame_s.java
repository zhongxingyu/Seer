 package gui.timer;
 
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 
 import util.SpeechSynthesizer;
 
 import main.Main;
 
 import de.mikescher.MikesKeyLogger.converter.GermanKeyboardConverter;
 import de.mikescher.MikesKeyLogger.events.GlobalKeyEvent;
 import de.mikescher.MikesKeyLogger.interfaces.GlobalKeyListener;
 import de.mikescher.MikesKeyLogger.logger.GlobalKeyLogger;
 
 public class Timer5on5Frame extends JFrame implements WindowListener, GlobalKeyListener {
 	private static final long serialVersionUID = 1L;
 	private JButton btnBlueBuff;
 	private JLabel lblBluebuffone;
 	private JButton btnRedBuff;
 	private JLabel lbRedBuffOne;
 	private JButton btnWolfsOne;
 	private JLabel lblWolfsOne;
 	private JButton btnGhostsOne;
 	private JLabel lblGhostsOne;
 	private JButton btnGolems;
 	private JLabel lblGolemsOne;
 	private JLabel lblEnemyJungle;
 	private JButton btnBlueBuff_1;
 	private JLabel lblBlueBuff_2;
 	private JButton btnRedBuff_1;
 	private JLabel lblRedBuff_2;
 	private JButton btnWolfs;
 	private JLabel lblWolfs_2;
 	private JButton btnGhosts;
 	private JLabel lblGhosts_2;
 	private JButton btnGolems_1;
 	private JLabel lblGolems_2;
 	private JButton btnDragon;
 	private JLabel lblDragon;
 	private JButton btnNashor;
 	private JLabel lblNashor;
 
 	private GameTimer timerBB_1;
 	private GameTimer timerBB_2;
 	private GameTimer timerRB_1;
 	private GameTimer timerRB_2;
 	private GameTimer timerGo_1;
 	private GameTimer timerGo_2;
 	private GameTimer timerGh_1;
 	private GameTimer timerGh_2;
 	private GameTimer timerW_1;
 	private GameTimer timerW_2;
 	private GameTimer timerD_1;
 	private GameTimer timerN_1;
 	
 	private GlobalKeyLogger gKeyLogger = new GlobalKeyLogger();
 
 	public Timer5on5Frame() {
 		super();
 		
 		initGUI();
 		initTimer();
 		initHotkeys();
 		
 		addWindowListener(this);
 	}
 
 	private void initGUI() {
 		setTitle("5on5-Timer");
 		setSize(new Dimension(256, 470));
 		setResizable(false);
 		getContentPane().setLayout(null);
 		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		setLocationRelativeTo(null);
 
 		btnBlueBuff = new JButton("F1: Blue Buff");
 		btnBlueBuff.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				setTimerBB();
 			}
 		});
 		btnBlueBuff.setBounds(10, 24, 109, 23);
 		getContentPane().add(btnBlueBuff);
 
 		lblBluebuffone = new JLabel("5min");
 		lblBluebuffone.setBounds(182, 28, 46, 14);
 		getContentPane().add(lblBluebuffone);
 
 		btnRedBuff = new JButton("F2: Red Buff");
 		btnRedBuff.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				setTimerRB();
 			}
 		});
 		btnRedBuff.setBounds(10, 51, 109, 23);
 		getContentPane().add(btnRedBuff);
 
 		lbRedBuffOne = new JLabel("5min");
 		lbRedBuffOne.setBounds(182, 55, 46, 14);
 		getContentPane().add(lbRedBuffOne);
 
 		btnWolfsOne = new JButton("F3: Wolfs");
 		btnWolfsOne.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				setTimerW();
 			}
 		});
 		btnWolfsOne.setBounds(10, 78, 109, 23);
 		getContentPane().add(btnWolfsOne);
 
 		lblWolfsOne = new JLabel("60s");
 		lblWolfsOne.setBounds(182, 82, 46, 14);
 		getContentPane().add(lblWolfsOne);
 
 		btnGhostsOne = new JButton("F4: Ghosts");
 		btnGhostsOne.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				setTimerG();
 			}
 		});
 		btnGhostsOne.setBounds(10, 106, 109, 23);
 		getContentPane().add(btnGhostsOne);
 
 		lblGhostsOne = new JLabel("50s");
 		lblGhostsOne.setBounds(182, 110, 46, 14);
 		getContentPane().add(lblGhostsOne);
 
 		btnGolems = new JButton("F5: Golems");
 		btnGolems.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				setTimerGolOne();
 			}
 		});
 		btnGolems.setBounds(10, 133, 109, 23);
 		getContentPane().add(btnGolems);
 
 		lblGolemsOne = new JLabel("60s");
 		lblGolemsOne.setBounds(182, 137, 46, 14);
 		getContentPane().add(lblGolemsOne);
 
 		lblEnemyJungle = new JLabel("Enemy Jungle");
 		lblEnemyJungle.setFont(new Font("Tahoma", Font.PLAIN, 15));
 		lblEnemyJungle.setBounds(76, 181, 90, 19);
 		getContentPane().add(lblEnemyJungle);
 
 		btnBlueBuff_1 = new JButton("F6: Blue Buff");
 		btnBlueBuff_1.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				setTimerBB_2();
 			}
 		});
 		btnBlueBuff_1.setBounds(10, 221, 109, 23);
 		getContentPane().add(btnBlueBuff_1);
 
 		lblBlueBuff_2 = new JLabel("5min");
 		lblBlueBuff_2.setBounds(182, 225, 46, 14);
 		getContentPane().add(lblBlueBuff_2);
 
 		btnRedBuff_1 = new JButton("F7: Red Buff");
 		btnRedBuff_1.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				setTimerRB_2();
 			}
 		});
 		btnRedBuff_1.setBounds(10, 248, 109, 23);
 		getContentPane().add(btnRedBuff_1);
 
 		lblRedBuff_2 = new JLabel("5min");
 		lblRedBuff_2.setBounds(182, 252, 46, 14);
 		getContentPane().add(lblRedBuff_2);
 
 		btnWolfs = new JButton("F8: Wolfs");
 		btnWolfs.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				setTimerW_2();
 			}
 		});
 		btnWolfs.setBounds(10, 275, 109, 23);
 		getContentPane().add(btnWolfs);
 
 		lblWolfs_2 = new JLabel("60s");
 		lblWolfs_2.setBounds(182, 277, 46, 14);
 		getContentPane().add(lblWolfs_2);
 
 		btnGhosts = new JButton("F9: Ghosts");
 		btnGhosts.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				setTimerG_2();
 			}
 		});
 		btnGhosts.setBounds(10, 302, 109, 23);
 		getContentPane().add(btnGhosts);
 
 		lblGhosts_2 = new JLabel("50s");
 		lblGhosts_2.setBounds(182, 306, 46, 14);
 		getContentPane().add(lblGhosts_2);
 
 		btnGolems_1 = new JButton("F10: Golems");
 		btnGolems_1.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				setTimerGo_2();
 			}
 		});
 		btnGolems_1.setBounds(10, 329, 109, 23);
 		getContentPane().add(btnGolems_1);
 
 		lblGolems_2 = new JLabel("60s");
 		lblGolems_2.setBounds(182, 333, 46, 14);
 		getContentPane().add(lblGolems_2);
 
 		btnDragon = new JButton("F11: Dragon");
 		btnDragon.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				setTimerD();
 			}
 		});
 		btnDragon.setBounds(10, 375, 109, 23);
 		getContentPane().add(btnDragon);
 
 		lblDragon = new JLabel("6min");
 		lblDragon.setBounds(182, 379, 46, 14);
 		getContentPane().add(lblDragon);
 
 		btnNashor = new JButton("F12: Nashor");
 		btnNashor.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				setTimerN();
 			}
 		});
 		btnNashor.setBounds(10, 407, 109, 23);
 		getContentPane().add(btnNashor);
 
 		lblNashor = new JLabel("7min");
 		lblNashor.setBounds(182, 411, 46, 14);
 		getContentPane().add(lblNashor);
 	}
 
 	private void initHotkeys() {
		gKeyLogger.addGlobalKeyListener(this);
		gKeyLogger.setConverter(new GermanKeyboardConverter());

		gKeyLogger.startListening();
 	}
 
 	private void initTimer() {
 		timerBB_1 = new GameTimer(60 * 5, lblBluebuffone);
 		timerBB_2 = new GameTimer(60 * 5, lblBlueBuff_2);
 		timerRB_1 = new GameTimer(60 * 5, lbRedBuffOne);
 		timerRB_2 = new GameTimer(60 * 5, lblRedBuff_2);
 		timerGo_1 = new GameTimer(60, lblGolemsOne);
 		timerGo_2 = new GameTimer(60, lblGolems_2);
 		timerGh_1 = new GameTimer(50, lblGhostsOne);
 		timerGh_2 = new GameTimer(50, lblGhosts_2);
 		timerW_1 = new GameTimer(60, lblWolfsOne);
 		timerW_2 = new GameTimer(60, lblWolfs_2);
 		timerN_1 = new GameTimer(60 * 7, lblNashor);
 		timerD_1 = new GameTimer(60 * 6, lblDragon);
 	}
 
 	private void setTimerBB() {
		SpeechSynthesizer.speak("Starting Timer B B");
 		timerBB_1.start();
 	}
 
 	private void setTimerRB() {
 		timerRB_1.start();
 	}
 
 	private void setTimerW() {
 		timerW_1.start();
 	}
 
 	private void setTimerG() {
 		timerGh_1.start();
 	}
 
 	private void setTimerGolOne() {
 		timerGo_1.start();
 	}
 
 	private void setTimerBB_2() {
 		timerBB_2.start();
 	}
 
 	private void setTimerRB_2() {
 		timerRB_2.start();
 	}
 
 	private void setTimerW_2() {
 		timerW_2.start();
 	}
 
 	private void setTimerG_2() {
 		timerGh_2.start();
 	}
 
 	private void setTimerGo_2() {
 		timerGo_2.start();
 	}
 
 	private void setTimerD() {
 		timerD_1.start();
 	}
 
 	private void setTimerN() {
 		timerN_1.start();
 	}
 
 	@Override
 	public void windowActivated(WindowEvent e) {
 		// unused
 	}
 
 	@Override
 	public void windowClosed(WindowEvent e) {
 		// unused
 	}
 
 	@Override
 	public void windowClosing(WindowEvent e) {
		gKeyLogger.removeGlobalKeyListener(this);
		
		gKeyLogger.stopListening();
 	}
 
 	@Override
 	public void windowDeactivated(WindowEvent e) {
 		// unused
 	}
 
 	@Override
 	public void windowDeiconified(WindowEvent e) {
 		// unused
 	}
 
 	@Override
 	public void windowIconified(WindowEvent e) {
 		// unused
 	}
 
 	@Override
 	public void windowOpened(WindowEvent e) {
 		// unused
 	}
 
 	@Override
 	public void keyPressed(GlobalKeyEvent gke) { // Wird bei jedem KeyEvent global aufgerufen
 		if (gke.getConverted().equalsIgnoreCase("[F1]")) {
 			setTimerBB();
 		} else if (gke.getConverted().equalsIgnoreCase("[F2]")) {
 			setTimerRB();
 		} else if (gke.getConverted().equalsIgnoreCase("[F3]")) {
 			setTimerW();
 		} else if (gke.getConverted().equalsIgnoreCase("[F4]")) {
 			setTimerG();
 		} else if (gke.getConverted().equalsIgnoreCase("[F5]")) {
 			setTimerGolOne();
 		} else if (gke.getConverted().equalsIgnoreCase("[F6]")) {
 			setTimerBB_2();
 		} else if (gke.getConverted().equalsIgnoreCase("[F7]")) {
 			setTimerRB_2();
 		} else if (gke.getConverted().equalsIgnoreCase("[F8]")) {
 			setTimerW_2();
 		} else if (gke.getConverted().equalsIgnoreCase("[F9]")) {
 			setTimerG_2();
 		} else if (gke.getConverted().equalsIgnoreCase("[F10]")) {
 			setTimerGo_2();
 		} else if (gke.getConverted().equalsIgnoreCase("[F11]")) {
 			setTimerD();
 		} else if (gke.getConverted().equalsIgnoreCase("[F12]")) {
 			setTimerN();
 		} else if (Main.DEBUG) {
 			System.out.println("Unconsumed Key Event: " + gke.getConverted());
 		}
 	}
 
 }
