 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.EventQueue;
 import java.awt.Font;
 import java.lang.reflect.InvocationTargetException;
 import java.util.Random;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.SwingConstants;
 import javax.swing.SwingUtilities;
 import javax.swing.border.EmptyBorder;
 import pt.ipleiria.estg.dei.stackemup.gridpanel.GridPanel;
 import java.awt.event.*;
 import java.awt.Toolkit;
 import java.awt.BorderLayout;
 import javax.swing.ImageIcon;
 
 /**
  * 
  * Graphical interface class
  * 
  */
 public class Gui extends JFrame implements KeyListener {
 
 	private JPanel contentPane;
 	protected static GridPanel battlefieldGrid;
 	private BattleField bf;
 	private int xGun;
 	private Random ran;
 	protected static boolean shootAllowed = true;
 	protected static boolean gameOver = false;
 	protected static boolean gameStart = false;
 	protected JLabel lblScore;
 	protected JLabel lblGameOver;
 	protected JLabel lblGameStart;
 	protected JLabel lblLevelFinished;
 	protected JLabel lblEarthDestroyed;
 	protected JLabel lblStartLevel;
 	protected JLabel lblIcon;
 	protected String info;
 	protected static Thread novaThread;
 	protected static int levelNumber = 0;
 	protected static boolean levelFinished = false;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					Gui frame = new Gui();
 					frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the Graphical interface
 	 * 
 	 * @throws IllegalElementException
 	 * @throws IllegalPositionException
 	 */
 	public Gui() throws IllegalElementException, IllegalPositionException {
 		this.addKeyListener(this);
 		Color col = new Color(000000);
 		setIconImage(Toolkit.getDefaultToolkit().getImage(
 				Gui.class.getResource("/image/icon.png")));
 		// System.out.println("test");
 		setTitle("Space Invaders - Erasmus Project 2013");
 		bf = new BattleField("es-in.txt");
 
 		ran = new Random(0);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		// setBounds(100, 100,694,691);
 		setBounds(100, 100, 50 * bf.getColumns(), 50 + 50 * bf.getRows());
 		contentPane = new JPanel();
 		info = (" Earth life : " + bf.life + "     Score : " + bf.score + " ");
 		contentPane.setBackground(col);
 		contentPane.setBorder(new EmptyBorder(0, 0, 0, 0));
 		contentPane.setLayout(new BorderLayout(0, 0));
 		Sound.setCurrentMusic(Sound.music);
 		setContentPane(contentPane);
 
 		battlefieldGrid = new GridPanel();
 		battlefieldGrid.setShowGridLines(false);
 		battlefieldGrid.setBackground(col);
 
 		battlefieldGrid.setRows(bf.getRows());
 		battlefieldGrid.setColumns(bf.getColumns());
 		contentPane.add(battlefieldGrid, BorderLayout.CENTER);
 
 		lblScore = new JLabel(info);
 		lblScore.setFont(new Font("Space Invaders", Font.PLAIN, 16));
 		lblScore.setHorizontalAlignment(SwingConstants.LEFT);
 
 		lblScore.setForeground(Color.WHITE);
 		contentPane.add(lblScore, BorderLayout.NORTH);
 
 		lblIcon = new JLabel();
 		lblIcon.setFont(new Font("Space Invaders", Font.PLAIN, 16));
 		lblIcon.setForeground(Color.BLACK);
 		lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
 		lblIcon.setIcon(new ImageIcon(Gui.class
 				.getResource("image/startScreen1.png")));
 		contentPane.add(lblIcon, BorderLayout.CENTER);
 
 		xGun = 0;
 
 		for (int i = 0; i < bf.columns; i++) {
 			if (bf.battlefield[bf.rows - 2][i].toString().equals("G")) {
 				xGun = i;
 				break;
 			}
 		}
 
 		final Runnable iterator = new Runnable() {
 
 			public void run() {
 				try {
 
 					ImageManage im = new ImageManage(bf, battlefieldGrid);
 					ImageManageGun imGun = new ImageManageGun(bf,
 							battlefieldGrid);
 					bf.move();
 					battlefieldGrid.repaint();
 				} catch (IllegalElementException e) {
 					e.printStackTrace();
 				} catch (IllegalPositionException e) {
 					e.printStackTrace();
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		};
 
 		novaThread = new Thread() {
 
 			public void run() {
 				lblScore.setVisible(false);
 				lblStartLevel = new JLabel();
 				lblStartLevel.setHorizontalAlignment(SwingConstants.CENTER);
 				lblStartLevel.setIcon(new ImageIcon(Gui.class.getResource("/image/start3.gif")));
 				contentPane.add(lblStartLevel, BorderLayout.CENTER);
 				repaint();
 				try {
 					sleep(10000);
 				} catch (InterruptedException e1) {
 					e1.printStackTrace();
 				}
 				lblStartLevel.setIcon(new ImageIcon(Gui.class
 						.getResource("/image/startBar.gif")));
 				repaint();
 				while (gameStart == false) {
 					try {
 						sleep(1000);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 
 				}
 				lblStartLevel.setVisible(false);
 				battlefieldGrid.setVisible(true);
 				battlefieldGrid.setGridBackground(Color.BLACK);
 				lblScore.setVisible(true);
 				contentPane.add(battlefieldGrid, BorderLayout.CENTER);
 				int sc = 0;
 				int li = 3;
 				while (gameOver == false) {
 					info = (" Earth life : " + bf.life + "     Score : "
 							+ bf.score + " ");
 					int newli = bf.life;
 					if (newli != li) {
 						if (bf.life <= 0) {
 							gameOver = true;
 
 							// ////////////////
 
 							lblEarthDestroyed = new JLabel();
 							lblEarthDestroyed.setFont(new Font(
 									"Space Invaders", Font.PLAIN, 16));
 							lblEarthDestroyed.setForeground(Color.BLACK);
 							lblEarthDestroyed
 									.setHorizontalAlignment(SwingConstants.CENTER);
							lblEarthDestroyed.setIcon(new ImageIcon(Gui.class
									.getResource("image/earthDestroyed.png")));
 							contentPane.add(lblEarthDestroyed,
 									BorderLayout.CENTER);
 
 							// //////////////
 
 							lblGameOver = new JLabel("Score : " + bf.score);
 							lblGameOver.setFont(new Font("Space Invaders",Font.PLAIN, 25));
 							lblGameOver.setHorizontalAlignment(SwingConstants.CENTER);
 							lblGameOver.setForeground(Color.WHITE);
 							contentPane.add(lblGameOver, BorderLayout.SOUTH);
 							battlefieldGrid.setVisible(false);
 							lblScore.setVisible(false);
 							this.interrupt();
 
 						}
 						lblScore.setText(info);
 						li = newli;
 					}
 
 					if (levelFinished == true) {
 						if(levelNumber!=10){
 							
 							try {
 
 								sleep(2000);
 								bf.newLevel();
 								
 								levelFinished=false;
 							} catch (IllegalElementException e) {
 								// TODO Auto-generated catch block
 								e.printStackTrace();
 							} catch (IllegalPositionException e) {
 								// TODO Auto-generated catch block
 								e.printStackTrace();
 							} catch (InterruptedException e) {
 								// TODO Auto-generated catch block
 								e.printStackTrace();
 							}
 						}
 						else{
 							lblLevelFinished = new JLabel("You saved the earth ! - Score : "+bf.score);
 							lblLevelFinished.setFont(new Font("Space Invaders", Font.PLAIN, 25));
 							lblLevelFinished.setHorizontalAlignment(SwingConstants.CENTER);
 							lblLevelFinished.setForeground(Color.WHITE);
 							contentPane.add(lblLevelFinished, BorderLayout.CENTER);
 							battlefieldGrid.setVisible(false);
 							lblScore.setVisible(false);
 							this.interrupt();
 						}
 					}
 
 					int newsc = bf.score;
 					if (newsc != sc) {
 
 						sc = newsc;
 						lblScore.setText(info);
 					}
 					for (int i = 0; i < bf.columns; i++) {
 						if (bf.battlefield[bf.rows - 2][i].toString().equals(
 								"G")) {
 							xGun = i;
 							break;
 						}
 					}
 
 					int numRand = ran.nextInt(100) + 1;
 					if (numRand < 3) {
 						try {
 							bf.setBattleFieldElement(0, bf.columns - 1,
 									new RedSpacecraft(0, bf.columns - 1));
 						} catch (Exception e) {
 							System.out
 									.println("RedSPaceCraft problem in Gui.java");
 						}
 					}
 
 					if (bf.dead) {
 						shootAllowed = false;
 						try {
 
 							// sleep(650);
 							shootAllowed = true;
 							bf.dead = false;
 							/*
 							 * for(int v=0;v<bf.rows;v++){ for(int
 							 * h=0;h<bf.columns;h++){ if
 							 * (bf.battlefield[v][h].toString().equals("S") ||
 							 * bf.battlefield[v][h].toString().equals("s") ) {
 							 * try { bf.setBattleFieldElement(v, h, new
 							 * Empty(v,h)); } catch (IllegalElementException |
 							 * IllegalPositionException e) {
 							 * System.out.println("Delete shot bug in Gui.java"
 							 * +e); } } } }
 							 */
 
 						} catch (Exception e) {
 							e.printStackTrace();
 						}
 					}
 
 					try {
 						SwingUtilities.invokeAndWait(iterator);
 					} catch (InvocationTargetException e) {
 						e.printStackTrace();
 					} catch (InterruptedException e) {
 						System.out.println("Thread Stoped");
 					}
 					try {
 						ImageManage im = new ImageManage(bf, battlefieldGrid);
 						ImageManageGun imGun = new ImageManageGun(bf,
 								battlefieldGrid);
 						sleep(250);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 
 			}
 		};
 		novaThread.start();
 
 	}
 
 	static boolean left = false;
 	static boolean right = false;
 	static boolean shot = false;
 
 	/**
 	 * Key Detection
 	 */
 	public void keyPressed(KeyEvent e) {
 		if (gameStart == false) {
 			int keyCode = e.getKeyCode();
 			if (keyCode == 32) {
 				gameStart = true;
 			}
 		} else {
 			int keyCode = e.getKeyCode();
 
 			if (keyCode == 37 && left == false && xGun > 0 && gameOver == false) {
 				left = true;
 				try {
 					if (bf.battlefield[bf.rows - 2][xGun - 1].toString()
 							.equals("S")) {
 						bf.gunCounter--;
 						// bf.life--;
 						Sound.explosion.play();
						bf.setBattleFieldElement(bf.rows - 2, xGun, new Empty(
								bf.rows - 2, xGun));
 						bf.dead = true;
						bf.setBattleFieldElement(bf.rows - 2, xGun - 1,
								new Empty(bf.rows - 2, xGun - 1));
						bf.setBattleFieldElement(bf.rows - 2, bf.columns / 2,
								new Gun(bf.rows - 2, bf.columns / 2));
 						xGun = 0;
 
 					} else {
 						// System.out.println("Move" +
 						// bf.battlefield[bf.rows-1][xGun-1].toString());
 						bf.battlefield[bf.rows - 2][xGun].move(bf.rows - 2,
 								xGun - 1);
 						bf.battlefield[bf.rows - 2][xGun - 1] = bf.battlefield[bf.rows - 2][xGun];
 						bf.setBattleFieldElement(bf.rows - 2, xGun, new Empty(
 								bf.rows - 2, xGun));
 
 					}
 
 					xGun--;
 					ImageManageGun imGun = new ImageManageGun(bf,
 							battlefieldGrid);
 					battlefieldGrid.repaint();
 
 				} catch (IllegalElementException | IllegalPositionException
 						| ArrayIndexOutOfBoundsException e1) {
 					System.out
 							.println("ArrayIndexOutOfBoundsException exception in Gui.java");
 				}
 
 			} else if (keyCode == 39 && right == false && xGun < bf.columns - 1
 					&& gameOver == false) {
 				right = true;
 
 				try {
 					if (bf.battlefield[bf.rows - 2][xGun + 1].toString()
 							.equals("S")) {
 						bf.gunCounter--;
 						// bf.life--;
 						Sound.explosion.play();
 						bf.dead = true;
						bf.setBattleFieldElement(bf.rows - 2, xGun, new Empty(
								bf.rows - 2, xGun));
						bf.setBattleFieldElement(bf.rows - 2, xGun + 1,
								new Empty(bf.rows - 2, xGun + 1));
						bf.setBattleFieldElement(bf.rows - 2, bf.columns / 2,
								new Gun(bf.rows - 2, bf.columns / 2));
 						xGun = 0;
 					} else {
 						bf.battlefield[bf.rows - 2][xGun].move(bf.rows - 2,
 								xGun + 1);
 						bf.battlefield[bf.rows - 2][xGun + 1] = bf.battlefield[bf.rows - 2][xGun];
						bf.setBattleFieldElement(bf.rows - 2, xGun, new Empty(
								bf.rows - 2, xGun));
 					}
 					xGun++;
 					ImageManageGun imGun = new ImageManageGun(bf,
 							battlefieldGrid);
 					battlefieldGrid.repaint();
 
 				} catch (IllegalElementException | IllegalPositionException
 						| ArrayIndexOutOfBoundsException e1) {
 					System.out.println(xGun + " = " + e1);
 				}
 
 			} else if (keyCode == 32 && shot == false && shootAllowed
 					&& gameOver == false) {
 				shot = true;
 
 				try {
 					bf.setBattleFieldElement(bf.rows - 3, xGun, new GunShot(
 							bf.rows - 3, xGun));
 					// bf.playSound("shoot.wav");
 					ImageManage im = new ImageManage(bf, battlefieldGrid);
 					battlefieldGrid.repaint();
 				} catch (IllegalElementException | IllegalPositionException e1) {
 					System.out.println("Probel shot inside the Gui.java");
 				}
 			}
 		}
 	}
 
 	/**
 	 * Key Released
 	 */
 	public void keyReleased(KeyEvent e) {
 
 		int keyCode = e.getKeyCode();
 		if (keyCode == 37) {
 			left = false;
 		} else if (keyCode == 39) {
 			right = false;
 		} else if (keyCode == 32) {
 			shot = false;
 		}
 
 	}
 
 	/**
 	 * Key Typed (Not used in this case)
 	 */
 	public void keyTyped(KeyEvent e) {
 
 	}
 
 }
