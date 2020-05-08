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
 	protected int lvl=1;
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
 	protected static int levelNumber = 1;
 	protected static boolean levelFinished = false;
 	protected static boolean pause = false;
 	protected static boolean gameLoad = true;
 	protected static boolean gameEnd = false;
 	protected ImageManage im;
 	protected ImageManageGun imGun;
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
 		setIconImage(Toolkit.getDefaultToolkit().getImage(Gui.class.getResource("/image/icon.png")));
 		// System.out.println("test");
 		setTitle("Space Invaders - Erasmus Project 2013");
 		bf = new BattleField("es-in.txt");
 
 		ran = new Random(0);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		// setBounds(100, 100,694,691);
 		setBounds(100, 100, 50 * bf.getColumns(), 50 + 50 * bf.getRows());
 		contentPane = new JPanel();
 		info = (" Earth life : " + bf.life + "     Score : " + bf.score + "     Level : " + lvl);
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
 
 					
 					bf.move();
 					im = new ImageManage(bf,battlefieldGrid);
 					imGun = new ImageManageGun(bf,battlefieldGrid);
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
 				pause = true;
 				try {
 					sleep(5000);
 				} catch (InterruptedException e1) {
 					e1.printStackTrace();
 				}
 				gameLoad = false;
 				
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
 				pause = false;
 				lblStartLevel.setVisible(false);
 				battlefieldGrid.setVisible(true);
 				battlefieldGrid.setGridBackground(Color.BLACK);
 				lblScore.setVisible(true);
 				contentPane.add(battlefieldGrid, BorderLayout.CENTER);
 				int sc = 0;
 				int li = 3;
 				while (gameOver == false) {
 					if (pause) {
 						try {
 							sleep(100);
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 						
 					} else {
 						
 					
 					info = (" Earth life : " + BattleField.life + "     Score : " + BattleField.score + "     Level : " + levelNumber);
 					int newli = BattleField.life;
 					if (newli != li) {
 						if (BattleField.life <= 0) {
 							gameOver = true;
 
 							// ////////////////
 
 							lblEarthDestroyed = new JLabel();
 							lblEarthDestroyed.setFont(new Font(
 									"Space Invaders", Font.PLAIN, 16));
 							lblEarthDestroyed.setForeground(Color.BLACK);
 							lblEarthDestroyed
 									.setHorizontalAlignment(SwingConstants.CENTER);
 							lblEarthDestroyed.setIcon(new ImageIcon(Gui.class.getResource("image/earthDestroyed.png")));
 							contentPane.add(lblEarthDestroyed,
 									BorderLayout.CENTER);
 
 							// //////////////
 
 							lblGameOver = new JLabel("Score : " + BattleField.score);
 							lblGameOver.setFont(new Font("Space Invaders",Font.PLAIN, 25));
 							lblGameOver.setHorizontalAlignment(SwingConstants.CENTER);
 							lblGameOver.setForeground(Color.WHITE);
 							contentPane.add(lblGameOver, BorderLayout.SOUTH);
 							battlefieldGrid.setVisible(false);
 							lblScore.setVisible(false);
 							gameEnd = true;
 							this.interrupt();
 							
 						}
 						lblScore.setText(info);
 						li = newli;
 					}
 
 					if (levelFinished == true) {
						if(levelNumber<3){
 							
 							try {
 
 								sleep(2000);
 								levelNumber++;
 								info = (" Earth life : " + bf.life + "     Score : " + bf.score + "     Level : " + levelNumber);
 								bf.newLevel(levelNumber);
 								
 								
 								levelFinished=false;
 							} catch (IllegalElementException e) {	
 								e.printStackTrace();
 							} catch (IllegalPositionException e) {
 								e.printStackTrace();
 							} catch (InterruptedException e) {
 								e.printStackTrace();
 							}
 						}
 						else{
 							lblLevelFinished = new JLabel("You saved the earth ! - Score : "+BattleField.score);
 							lblLevelFinished.setFont(new Font("Space Invaders", Font.PLAIN, 25));
 							lblLevelFinished.setHorizontalAlignment(SwingConstants.CENTER);
 							lblLevelFinished.setForeground(Color.WHITE);
 							contentPane.add(lblLevelFinished, BorderLayout.CENTER);
 							battlefieldGrid.setVisible(false);
 							lblScore.setVisible(false);
 							this.interrupt();
 						}
 					}
 
 					int newsc = BattleField.score;
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
 
 
 					
 					
 					
 					try {
 						SwingUtilities.invokeAndWait(iterator);
 					} catch (InvocationTargetException e) {
 						e.printStackTrace();
 					} catch (InterruptedException e) {
 						System.out.println("Thread Stoped");
 					}
 					
 
 					
 					if (BattleField.dead) {
 						try {
 							BattleField.score-=250;
 							if(BattleField.score<0)
 								BattleField.score=0;
 							
 
 						} catch (Exception e) {
 							e.printStackTrace();
 						}
 					}
 					
 					
 					int numRand = ran.nextInt(100) + 1;
 					if (numRand < 3) {
 						try {
 							bf.setBattleFieldElement(0, bf.columns - 1,new RedSpacecraft(0, bf.columns - 1));
 						} catch (Exception e) {
 							System.out.println("RedSPaceCraft problem in Gui.java");
 						}
 					}
 
 					
 					
 					try {
 						im = new ImageManage(bf, battlefieldGrid);
 						imGun = new ImageManageGun(bf, battlefieldGrid);
 						sleep(250);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
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
 		
 		if (gameLoad == true) {
 			
 		} else {
 			 
 		
 		
 		while (gameStart == false) {
 			int keyCode = e.getKeyCode();
 			switch (keyCode) {
 			case KeyEvent.VK_SPACE:
 				gameStart = true;
 				
 				break;
 			default:
 				gameStart = true;
 				break;
 			}
 //			try {
 //				bf.clearShot();
 //			} catch (IllegalElementException e2) {
 //				e2.printStackTrace();
 //			} catch (IllegalPositionException e2) {
 //				e2.printStackTrace();
 //			}
 		}
 		
 		
 		
 		
 		int keyCode = e.getKeyCode();
 		switch (keyCode) {
 		
 		case KeyEvent.VK_LEFT :
 			
 			if (left == false && xGun > 0 && gameOver == false && BattleField.dead == false && pause==false && !levelFinished) {
 				left = true;
 				try {
 					if (bf.battlefield[bf.rows - 2][xGun - 1].toString().equals("S")) {
 						BattleField.score-=250; 
 						if(BattleField.score<0){
 							BattleField.score=0;
 						}
 						bf.gunCounter--;
 						
 						Sound.explosion.play();
 						BattleField.dead = true;
 						bf.setBattleFieldElement(bf.rows - 2, xGun, new Empty(bf.rows - 2, xGun));
 						bf.setBattleFieldElement(bf.rows - 2, xGun - 1,new GunExplosion(bf.rows - 2, xGun - 1));
 						bf.setBattleFieldElement(bf.rows - 2, bf.columns / 2,new Gun(bf.rows - 2, bf.columns / 2));
 						xGun = 0;
 
 					} else {
 						bf.battlefield[bf.rows - 2][xGun].move(bf.rows - 2, xGun - 1);
 						bf.battlefield[bf.rows - 2][xGun - 1] = bf.battlefield[bf.rows - 2][xGun];
 						bf.setBattleFieldElement(bf.rows - 2, xGun, new Empty(bf.rows - 2, xGun));
 
 					}
 
 					xGun--;
 					imGun = new ImageManageGun(bf,battlefieldGrid);
 					battlefieldGrid.repaint();
 
 				} catch (IllegalElementException | IllegalPositionException	| ArrayIndexOutOfBoundsException e1) {
 					System.out.println("ArrayIndexOutOfBoundsException exception in Gui.java");
 				}
 			
 			}
 			break;
 		case KeyEvent.VK_RIGHT :
 			if ( right == false && xGun < bf.columns - 1 && gameOver == false && BattleField.dead == false && pause==false && !levelFinished) {
 				
 				right = true;
 
 				try {
 					if (bf.battlefield[bf.rows - 2][xGun + 1].toString().equals("S")) {
 						BattleField.score-=250; 
 						if(BattleField.score<0){
 							BattleField.score=0;
 						}
 						bf.gunCounter--;
 						// bf.life--;
 						Sound.explosion.play();
 						BattleField.dead = true;
 						bf.setBattleFieldElement(bf.rows - 2, xGun, new Empty(bf.rows - 2, xGun));
 						bf.setBattleFieldElement(bf.rows - 2, xGun + 1,new GunExplosion(bf.rows - 2, xGun + 1));
 						bf.setBattleFieldElement(bf.rows - 2, bf.columns / 2,new Gun(bf.rows - 2, bf.columns / 2));
 						
 						xGun = 0;
 					} else {
 						bf.battlefield[bf.rows - 2][xGun].move(bf.rows - 2,xGun + 1);
 						bf.battlefield[bf.rows - 2][xGun + 1] = bf.battlefield[bf.rows - 2][xGun];
 						bf.setBattleFieldElement(bf.rows - 2, xGun, new Empty(bf.rows - 2, xGun));
 					}
 					xGun++;
 					imGun = new ImageManageGun(bf,battlefieldGrid);
 					battlefieldGrid.repaint();
 
 				} catch (IllegalElementException | IllegalPositionException | ArrayIndexOutOfBoundsException e1) {
 					System.out.println(xGun + " = " + e1);
 				}
 			
 	
 			
 			}
 	
 			break;
 
 		case KeyEvent.VK_SPACE :
 			
 			if (shot == false && gameOver == false  && BattleField.dead == false && pause==false) {
 				shot = true;
 
 				try {
 					bf.setBattleFieldElement(bf.rows - 3, xGun, new GunShot(bf.rows - 3, xGun));
 					im = new ImageManage(bf, battlefieldGrid);
 					battlefieldGrid.repaint();
 				} catch (IllegalElementException | IllegalPositionException e1) {
 					System.out.println("Probel shot inside the Gui.java");
 				}
 			}
 			
 			break;
 		
 			
 		case KeyEvent.VK_P :
 			if (pause == false) {
 				pause = true;
 				
 			} else {
 				pause = false;
 			}
 			
 			
 			break;
 		
 			
 		case KeyEvent.VK_ENTER :
 			if (gameEnd ==true ) {
 				
 			}
 			break;
 			
 		default:
 			break;
 		}
 		}
 	}
 	
 
 	/**
 	 * Key Released
 	 */
 	public void keyReleased(KeyEvent e) {
 		if (gameLoad == true) {
 			
 		} else {
 			int keyCode = e.getKeyCode();
 			switch (keyCode) {
 			
 			case KeyEvent.VK_LEFT: 
 				left = false;
 				
 				break;
 			case KeyEvent.VK_RIGHT: 
 				right = false;
 				break;
 			case KeyEvent.VK_SPACE: 
 				shot = false;
 				break;
 			default:
 				break;
 			}
 		}
 		
 
 	}
 
 	/**
 	 * Key Typed (Not used in this case)
 	 */
 	public void keyTyped(KeyEvent e) {
 		if (gameLoad == true) {
 			
 		} 
 	}
 
 }
