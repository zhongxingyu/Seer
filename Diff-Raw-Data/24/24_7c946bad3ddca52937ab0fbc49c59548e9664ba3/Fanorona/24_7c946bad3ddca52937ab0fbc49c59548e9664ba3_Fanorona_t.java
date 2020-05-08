 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.Label;
 import java.awt.Toolkit;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JToggleButton;
 import javax.swing.Timer;
 
 import java.awt.Button;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 
 import javax.swing.JLabel;
 import java.awt.Color;
 import java.util.ArrayList;
 
 public class Fanorona extends JFrame {
 //Data members
 	private static final long serialVersionUID = 3335293785778663915L;
 	private JPanel contentPane;
 	
 	private Timer countdown;
 	private Timer gameLoop;
 	
 	private Label timeLabel;
 	private JLabel p1Score;
 	private JLabel p2Score;
 	
 	private Button hintsButton;
 	private Button skipbutton;
 	private Button btnMainMenu;
 	private Button btnPause;
 	
 	protected JToggleButton sacrificeButton;
 	
 	protected Board board;
 	private PauseMenu pause;
 	private JFrame swap;
 	private Color maroon = new Color(80,0, 30);
 	
 	//private int timeRemaining = 25;//TODO make this value correspond to the timer value input by user in new game menu
 	private boolean timeOut;
 	private int curP1Score;
 	private int curP2Score;
 	private int timerSet;
 	private int maxTurns;
 	private static int playerNumber;
 	private static int gameType;
 	private static int numRows;
 	private static int numCols;
 	private static int timerLength;
 	private static int timeRemaining;
 	private static String color;
 	
 	private AI CPU1;
 	private AI CPU2;
 
 /**Constructor
 * @param playerNum Player Number
 * @param type Game type, PvP, PvC, CvC
 * @param rows Number of Rows 
 * @param cols Number of Columns
 * @param timer Time in ms */
 	public Fanorona(int playerNum, int type, int rows, int cols, int timer) {
 	/*****************************
 	PLAYERNUMBER
 		- -1;
 		-  1: 
 		-  2: 
 		
 	GAMETYPE
		- 0: you don't get to decide! (client)
		-  1: P v P
		-  2: P v C
		-  3: C v C
 	
 	NUMROWS & NUMCOLS
 		- -1: you don't get to decide! (client)
 		
 	TIMER
 		- -1: no timer (local/server) OR you don't get to decide! (client)
 
 <<<<<<< HEAD
 	*****************************/
		playerNumber = playerNum;
		
 		gameType = type;
 		if (gameType == 2)
 			CPU1 = new AI(this, -1);
 		else if (gameType == 3) {
 			CPU1 = new AI(this, 1);
 			CPU2 = new AI(this, 2);
 		}
 		numRows = rows;
 		numCols = cols;
 		maxTurns = 10*numRows;
		
 	/*************/
 		timeRemaining = timer / 1000;
 		timerSet = timeRemaining;
 	/*************/
 		board = new Board(numRows, numCols, playerNumber, this);
 		pause = new PauseMenu(this);
 		swap = new JFrame();
 		timeOut = false;
 	/*************/
 		setResizable(false);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		int width = 800;
 	    int height = 150 + numRows*50;
 	    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
 	    int x = (screen.width - width) / 2;
 	    int y = (screen.height - height) / 2;
 	    setBounds(x, y, width, height);
 		setTitle("Fanorona");
 		
 		contentPane = new JPanel();
 		contentPane.setLayout(new BorderLayout(0, 0));
 		setContentPane(contentPane);
 		contentPane.add(board);
 		
 		btnMainMenu = new Button("Main Menu");
 		btnMainMenu.setBounds(311, 23, 112, 29);
 		btnMainMenu.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				Welcome_Menu win = new Welcome_Menu();
 				win.setVisible(true);
 				dispose();
 			}
 		});
 		board.setLayout(null);
 		board.add(btnMainMenu);
 		
 		p1Score = new JLabel();
 		p1Score.setBackground(Color.white);
 		p1Score.setForeground(maroon);
 		p1Score.setBounds(50, 23, 80, 29);
 		p1Score.setOpaque(true);
 		board.add(p1Score);
 		
 		p2Score = new JLabel();
 		p2Score.setBackground(maroon);
 		p2Score.setForeground(Color.white);
 		p2Score.setBounds(686, 23, 80, 29);
 		p2Score.setOpaque(true);
 		board.add(p2Score);
 		
 		if (timerSet > 0) {
 			timeLabel = new Label("Time Remaining: " + String.valueOf(timeRemaining));
 			timeLabel.setBackground(Color.LIGHT_GRAY);
 			timeLabel.setBounds(428, 29, 130, 17);
 			board.add(timeLabel);
 		}
 		
 		btnPause = new Button("Pause");
 		btnPause.setBounds(563, 23, 80, 29);
 		btnPause.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				//stop clock and pause game!
 				pause.makeVisible();
 			}
 		});
 		board.add(btnPause);
 		
 		hintsButton = new Button("Hints");
 		hintsButton.setBounds(10, 60, 112, 29);
 		hintsButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (board.getBoardState().hasCaptureMoves()){
 					ArrayList<Pair> captureMovables = board.getCaptureMovable();
 					int maxPair = captureMovables.size() - 1;
 					int randomPiece = (int) Math.floor((Math.random()*maxPair)+0);
 					Pair[] listMoves = board.getBoardState().checkCaptureDestinations(captureMovables.get(randomPiece));
 					maxPair = listMoves.length;
 					boolean foundDestination = false;
 					int randomDestination = -1;
 					while(!foundDestination) {
 						randomDestination = (int) Math.floor((Math.random()*maxPair)+0);
 						if (listMoves[randomDestination] != null)
 							foundDestination = true;
 					}
 					if (randomDestination != -1) {
 						board.move(captureMovables.get(randomPiece), listMoves[randomDestination], "capture");
 						board.activateRemovables(captureMovables.get(randomPiece), listMoves[randomDestination]);
 						int advanceOrWithdrawl = (int) Math.floor(Math.random() + .5);
 						if (!(board.getBoardState().getRemovables().isEmpty())) {
 							if (!(board.getBoardState().getRemovables().get(advanceOrWithdrawl).isEmpty())) {
 								board.removeRemovables(advanceOrWithdrawl);
 							}
 							else {
 								if (advanceOrWithdrawl == 1)
 									advanceOrWithdrawl = 0;
 								else advanceOrWithdrawl = 1;
 								board.removeRemovables(advanceOrWithdrawl);
 							}
 						}
 						board.select(listMoves[randomDestination]);
 					}
 				}
 				hintsButton.setVisible(false);
 			}
 		});
 		board.add(hintsButton);
 		
 		
 		skipbutton = new Button("Skip Turn");
 		skipbutton.setBounds(157, 23, 112, 29);
 		skipbutton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (timerSet > 0) {
 					timeLabel.setText("Next Turn!");
 					timeRemaining = timerSet + 1;
 				}
 				board.nextTurn();
 			}
 		});
 		skipbutton.setVisible(false);
 		board.add(skipbutton);
 		
 		sacrificeButton = new JToggleButton("Sacrifice");
 		sacrificeButton.setBounds(157,23,112,29);
 		sacrificeButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				JToggleButton sacBtn = (JToggleButton)e.getSource();
 				if(sacBtn.isSelected()){
 					board.setMakingSacrifice(true);
 				}
 				else {
 					board.setMakingSacrifice(false);
 				}
 			}
 		});
 		sacrificeButton.setVisible(true);
 		board.add(sacrificeButton);
 		
 		gameLoop = new Timer(100, new GameLoopListener(this));
 		gameLoop.start();
 
 		swap.setVisible(false);
 		
 		countdown = new Timer(1000, new CountdownTimerListener());
 		
 		board.nextTurn();
 		//JOptionPane.showMessageDialog(new JFrame(), "The Game Will Begin Now!");
 		if (timerSet > 0)
 			countdown.start();
 		
 		if (gameType == 3)
 			CPU1.makeMove();
 	}
 
 /**Internal Classes*/	
 	//TODO reset timer when nextTurn() is called in board
 	/*TODO add game condition: 
 	 * 	if( (unable to make a move) && (board.getVisited().isEmpty) ){
 	 * 		currentPlayer LOSES!
 	 * 	}
 	 */
 	
 	//creates the handler for updating the timer!
 	class CountdownTimerListener implements ActionListener {
         public void actionPerformed(ActionEvent e) {
         	if ((!pause.isVisible()) && (!swap.isVisible())) {
         		if (--timeRemaining > 0) 
 	                timeLabel.setText("Time Remaining: " + String.valueOf(timeRemaining));
 	            else {
 	                timeLabel.setText("Time's up!");
 	                if (board.getVisited().size() > 0) {
 	                	countdown.stop();
 	                	timeRemaining = timerSet + 1;//TODO make this value correspond to the timer value input by user in new game menu
 	                	board.nextTurn();
 	                	countdown.start();
 	                } else {
 	                	dispose();
 	                	timeOut = true;
 	                	countdown.stop();
 	                	EndMenu end = new EndMenu(Fanorona.this, timeOut);
 	                	end.setVisible(true);
 	                	swap.dispose();
 	                }
 	            }
         	}
         }
     }
 	//creates the handler for updating the timer!
 	class GameLoopListener implements ActionListener {
 		private Fanorona fan;
         public GameLoopListener(Fanorona f) {
         	fan = f;
         }
 
 		public void actionPerformed(ActionEvent e) {
 //        	final int maxTurn = 10*numRows;
         	fan.board.updateScores();
     		curP1Score = fan.board.getP1Score();
     		curP2Score = fan.board.getP2Score();
     		board.updateRemoveAvailable();
     		
     		EventQueue.invokeLater(new Runnable() {
     			public void run() {
     				try {
     					p2Score.setText(" Maroon: " + Integer.toString(curP2Score)); 
     					p1Score.setText("   White: " + Integer.toString(curP1Score));
     				} catch (Exception e) {
     					e.printStackTrace();
     				}
     			}
     		});
         	
         	//Any other Looping actions we need done here
         	if ((fan.board.getP1Score() == 0) || (fan.board.getP2Score() == 0)) {
         		EndMenu end = new EndMenu(Fanorona.this, timeOut);
         		end.setVisible(true);
         		fan.gameLoop.stop();
         		fan.dispose();
         	}
         	if (fan.board.getTurnCount() == maxTurns) {
         		EndMenu end = new EndMenu(Fanorona.this, timeOut);
         		end.setVisible(true);
         		fan.gameLoop.stop();
         		fan.dispose();
         	}
 
         	//Enable player to end turn early with skipbutton if a capture has been made
         	if (board.getCapturedThisTurn() > 0) {
         		skipbutton.setVisible(true);
         	}
         	else{
         		skipbutton.setVisible(false);
         	}
         	
         	//If a piece is selected, show destinations to move to...if there aren't any remove choices that need to be made
         	if(board.getSelected() != null) {
 	        	sacrificeButton.setVisible(false);
         		if( !(board.isRemoveAvailable()) ) {
         			if(board.getBoardState().hasCaptureDestinations(board.getSelected())) {
 	        			board.highlightCaptureDestinations(board.getSelected());
 	        		}
 	        		else if(board.getBoardState().hasDestinations(board.getSelected())) {
 	        			board.highlightDestinations(board.getSelected());
 	        		}
 	        		else {
 	        			board.nextTurn();
 	        		}
         		}
         	}
         	
         	//Else show pieces that can be moved
         	else {
         		sacrificeButton.setVisible(true);
         		//If capture moves are available, show pieces that can make capture moves
         		if(board.getBoardState().hasCaptureMoves()){
         			board.highlightCaptureMovable();
         		}
         		//Else show pieces that can make paika moves, if any
         		else {
         			board.highlightPaikaMovable();
         		}
 			}	
         }
     }
 	
 /**Get-Methods*/
 	public int getNumRows() {
 		return numRows;
 	}
 	public int getNumCols() {
 		return numCols;
 	}
 	public int getP1Score() {
 		return curP1Score;
 	}
 	public int getP2Score() {
 		return curP2Score;
 	}
 	public String getConfig() {
 		String conf;
 		conf = numRows + " " + numCols + " " + color + " " + timerLength;
  		return conf;
 	}
 	public int getMaxTurns() {
 		return maxTurns;
 	}
 	public final Timer getTimer() {
 		return countdown;
 	}
 	public int getTimeSet() {
 		return timerSet;
 	}
 	public int getGameType() {
 		return gameType;
 	}
 	public AI getAI1() {
 		return CPU1;
 	}
 	public AI getAI2() {
 		return CPU2;
 	}
 	public int getPlayerNum() {
 		return playerNumber;
 	}
 	public Button getHintsButton() {
 		return hintsButton;
 	}
 /**Update-Methods*/
 	public void setConfig(int row, int col, String color2, int time) {
 		numRows = row;
 		numCols = col;
 		color = color2;
 		timerLength = time;		
 	}
 	public void setTimeRemaining(int time) {
 		timeRemaining = time;
 	}
 }
