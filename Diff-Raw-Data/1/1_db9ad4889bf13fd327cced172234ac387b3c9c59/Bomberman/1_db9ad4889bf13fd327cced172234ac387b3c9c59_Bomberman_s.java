 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import javax.swing.*;
 import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;
 
 public class Bomberman extends JFrame implements KeyListener {
     
 	//main players
     Board board;
     JLayeredPane layeredPane;
     Player playerMe;
     Player playerOpp;
     int bomb;
     Timer t;
     MyConnection con;
     String walls; 
 	
 	// TIMER
 	Timer timer;
 	JLayeredPane gameTimer = new JLayeredPane();
 	JTextField tf = new JTextField();
 	JLabel timerLabel; 
 	int count;
     
     public Bomberman (MyConnection con, Player p, Player o, String walls) {
         
         this.con = con;
 		this.setSize(600,650);
         setTitle("Bomberman");
         Dimension boardSize = new Dimension(600, 650);
         layeredPane = new JLayeredPane();
         getContentPane().add(layeredPane);
         layeredPane.setPreferredSize(boardSize);
         addKeyListener(this);
         
         playerMe = p;
         playerOpp = o;
 		playerMe.bombCount = 1;
 		playerOpp.bombCount = 1;
 		playerMe.bombLen = 1;
 		playerOpp.bombLen = 1;
         this.walls = walls;
         
         board = new Board(walls);
         layeredPane.add(board, JLayeredPane.DEFAULT_LAYER);
         
 		// Timer
 		timerLabel = new JLabel("Waiting..", SwingConstants.CENTER);
 		timerLabel.setBounds(250,600,100,50);
 		layeredPane.add(timerLabel);
 		
         setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 		pack();
 		setResizable(true);
 		setLocationRelativeTo(null);
     }
     
 	public void startTime() {
 		int count = 5;
 		timerLabel.setText("Time left: " + count);
 			
 		TimeClass tc = new TimeClass(count);
 		timer = new Timer(1000, tc);
 		timer.start();
     }
     	
 	public class TimeClass implements ActionListener {
 		int counter;
 		int flag = 0;
 		
 		public TimeClass(int counter){
 			this.counter = counter;
 		}
 		
 		public void actionPerformed(ActionEvent tc){
 			counter--;
 			if( counter >= 1 ) timerLabel.setText("Time left: " + counter);
 			else{
 				timer.stop();
 				timerLabel.setText("TIME'S UP!");
 			}
 		}
 		
 	}
 
     public void startGame() {
     	this.playerMe.loc = board.addPlayer(this.playerMe, this.playerMe.startPos);
 		this.playerOpp.loc = board.addPlayer(this.playerOpp, this.playerOpp.startPos);
     }
     
 	public void keyPressed(KeyEvent e) {
         int key = e.getKeyCode();
         JPanel panel = (JPanel) board.getComponent(playerMe.loc);
 		panel.remove(playerMe.piece);
 		int old = playerMe.loc;
 		if(!playerMe.dead) {
 			if (key == KeyEvent.VK_LEFT) {
 				int newLoc = playerMe.moveLeft(board, playerMe.loc);
 				int bombCount = playerMe.bombCount;
 				int bombLen = playerMe.bombLen;
 				playerMe = new Player(playerMe.name, 0, newLoc, bombCount, bombLen);
 				con.sendMessage("/playerMoveLeft " + playerMe.name + " " + old);
 			}
 
 			if (key == KeyEvent.VK_RIGHT) {
 				int newLoc = playerMe.moveRight(board, playerMe.loc);
 				int bombCount = playerMe.bombCount;
 				int bombLen = playerMe.bombLen;
 				playerMe = new Player(playerMe.name, 1, newLoc, bombCount, bombLen);
 				con.sendMessage("/playerMoveRight " + playerMe.name + " " + old);
 			}
 
 			if (key == KeyEvent.VK_UP) {
 				int newLoc = playerMe.moveUp(board, playerMe.loc);
 				int bombCount = playerMe.bombCount;
 				int bombLen = playerMe.bombLen;
 				playerMe = new Player(playerMe.name, 2, newLoc, bombCount, bombLen);
 				con.sendMessage("/playerMoveUp " + playerMe.name + " " + old);
 			}
 
 			if (key == KeyEvent.VK_DOWN) {
 				int newLoc = playerMe.moveDown(board, playerMe.loc);
 				int bombCount = playerMe.bombCount;
 				int bombLen = playerMe.bombLen;
 				playerMe = new Player(playerMe.name, 3, newLoc, bombCount, bombLen);
 				con.sendMessage("/playerMoveDown " + playerMe.name + " " + old);
 			}
 			
 			if (key == KeyEvent.VK_SPACE) {
 				System.out.println("bombcount: " + playerMe.bombCount);
 				if(playerMe.bombCount > 0) {
 					System.out.println("pwede");
 					playerMe.bombCount--;
 					con.sendMessage("/playerBomb " + playerMe.name + " " + old);
 				}
 			}
 //			System.err.println(playerMe.loc);
 		}
     }
 	
 	public void updateBoard() {
 		JPanel panel = (JPanel) board.getComponent(playerMe.loc);
 		panel.add(playerMe.piece);
 		validate();
 		repaint();
 		if(panel.getBackground() == Color.red) {
 			JOptionPane.showMessageDialog(this, "GAME OVER! YOU DIED :(");
 			playerMe.piece.setVisible(false);
 		}
 	}
 	
 	public void updateOpponent(int oldLoc, int direction) {
 		JPanel panel = (JPanel) board.getComponent(oldLoc);
 		panel.remove(playerOpp.piece);
 		int newLoc = 0;
 		switch (direction) {
 			case 0:	newLoc = playerOpp.moveLeft(board, oldLoc); break;
 			case 1:	newLoc = playerOpp.moveRight(board, oldLoc); break;
 			case 2:	newLoc = playerOpp.moveUp(board, oldLoc); break;
 			case 3:	newLoc = playerOpp.moveDown(board, oldLoc); break;
 		}
 		panel = (JPanel) board.getComponent(newLoc);
 		int bombCount = playerOpp.bombCount;
 		int bombLen = playerOpp.bombLen;
 		playerOpp = new Player(playerOpp.name, direction, newLoc, bombCount, bombLen);
 		panel.add(playerOpp.piece);
 		validate();
 		repaint();
 	}
 	
 	public void updateBomb(int bombLoc) {
 		updateBoard();
 		JPanel panel = (JPanel) board.getComponent(bombLoc);
 		panel.setBackground(Color.white);
 		panel.add( new JLabel( new ImageIcon("data/bomb.png") ) );
 		validate();
 		repaint();
 	}
 	
     public void fire(int bombLoc, int bombLen) {
         System.out.println("FIRE!");
         JPanel p = (JPanel) board.getComponent(bombLoc);
         p.removeAll();
         p.add(new JLabel(new ImageIcon("data/fire_mid.png")));
 		p.setBackground(Color.red);
 		JPanel side;
 		Color c;
 		final int LEFT = -1, RIGHT = 1, UP = -11, DOWN = 11;
 		final int[] DIRECTION = {-1, 1, -11, 11};
 		final String[] DIRECT = {"left", "right", "up", "down"};
 		bombLen = 2;
 		int fireLoc;
 		boolean condition;
 		for(int j = 0; j < 4; j++) {
 			fireLoc = bombLoc;
 			// for heuristics na OB sa map
 			for (int i = 1; i <= bombLen; i++) {
 				condition = false;
 				
 				switch(DIRECTION[j]) {
 					case -1:	condition = fireLoc % 11 != 0 ? true : false; break; //left
 					case 1:		condition = fireLoc % 11 != 10 ? true : false; break; //right
 					case -11:	condition = fireLoc >= 11 ? true : false; break; //up
 					case 11:	condition = fireLoc <= 109 ? true : false; break; //down
 				}
 				System.out.println(condition);
 				fireLoc += DIRECTION[j]; 
 				if(condition) {
 					System.out.println("direction: " + DIRECT[j]);
 					side = (JPanel) board.getComponent(fireLoc);
 					c = side.getBackground();
 					if (c == Color.gray) break;
 					if (c == Color.black || c == Color.green) {
 						if(c == Color.green) {
 							i = bombLen;
 						}
 						side.removeAll();
 						if(i == bombLen) side.add( new JLabel( new ImageIcon("data/fire_" + DIRECT[j] + ".png") ) );
 						else {
 							// horizontal
 							if(j <= 1) side.add( new JLabel( new ImageIcon("data/fire_horizontal.png") ) );
 							else side.add( new JLabel( new ImageIcon("data/fire_vertical.png") ) );
 						}
 						side.setBackground(Color.red);
 						validate();
 						repaint();
 					}
 					if (fireLoc == playerMe.loc) {
 						playerMe.dead = true;
 					}
 				}
 			}
 		}
 		if(playerMe.dead) {
 			con.sendMessage("/dead "+playerMe.name);
 //			JOptionPane.showMessageDialog(this, "GAME OVER! YOU DIED :(");
 //			playerMe.piece.setVisible(false);
 		}
     }
 	
 	public void removeBomb(int bombLoc) {
 		JPanel panel = (JPanel) board.getComponent(bombLoc);
 		panel.setBackground(Color.black);
 		panel.removeAll();
 		JPanel side;
 		Color c;
 		
 		final int LEFT = -1, RIGHT = 1, UP = -11, DOWN = 11;
 		final int[] DIRECTION = {-1, 1, -11, 11};
 		final String[] DIRECT = {"left", "right", "up", "down"};
 		
 		int j = 0;
 		while(j <= 4) {
 			System.out.println("REMOVE direction: " + DIRECT[j]);
 			int i = 1;
 			while(true) {			
 				side = (JPanel) board.getComponent(bombLoc+(DIRECTION[j]*i));
 				c = side.getBackground();
 				if(c == Color.red) {
 					System.out.print("red");
 					side.setBackground(Color.black);
 					side.removeAll();
 				} else { System.out.println("not red"); break; }
 				validate();
 				repaint();
 				i++;
 				side = null;
 				c = null;
 			}
 			System.out.println("j: " + j);
 			j++;
 		}
 	}
 
     public void keyReleased(KeyEvent e) {
         int key = e.getKeyCode();
 
         if (key == KeyEvent.VK_LEFT) {
 //             playerMe.dx = 0;
         }
 
         if (key == KeyEvent.VK_RIGHT) {
 //             playerMe.dx = 0;
         }
 
         if (key == KeyEvent.VK_UP) {
 //             playerMe.dy = 0;
         }
 
         if (key == KeyEvent.VK_DOWN) {
 //             playerMe.dy = 0;
         }
         
         if (key == KeyEvent.VK_SPACE) {
             
         }
     }
 
     @Override
     public void keyTyped(KeyEvent ke) {
     }
 	
 }
