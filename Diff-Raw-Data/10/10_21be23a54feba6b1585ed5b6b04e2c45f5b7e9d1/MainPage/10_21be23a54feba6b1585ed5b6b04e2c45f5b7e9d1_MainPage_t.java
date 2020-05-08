 package com.UI;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import com.state.AI;
 import com.state.SingletonStatus;
 
 public class MainPage extends JPanel{
 	/**
 	 * Main page that displays board, score and status of the game
 	 */
 	private static final long serialVersionUID = -386551712892064216L;
 	
 	//Labels for status of turn and scores of players
 	private static JLabel turnStatus = new JLabel("Player 1's Turn");
 	private static JLabel player1Score= new JLabel("Player 1: 0");
 	private static JLabel player2Score = new JLabel("Player 2: 0");
 	
 	//Panel that will take the score
 	private JPanel score = new JPanel();
 	//Board that will be in main page
 	private static Board board;
 	private static int currentNumOfBoxes;
 	private static boolean currentPlayer2;
 	private static FinishGame game;
 	
 
 	public MainPage(int numOfBoxes, boolean player2){
 		reset();
 		currentNumOfBoxes = numOfBoxes;
 		currentPlayer2 = player2;
 		board = new Board(numOfBoxes);
 		AI instance = AI.getInstance();
 		instance.setEnabled(!player2);
 		if(!player2){
 			instance.setBoard(board);
 		}
 		
 		this.setLayout(new BorderLayout());
 		this.add(board, BorderLayout.CENTER);
 		this.add(turnStatus, BorderLayout.NORTH);
 		score.add(player1Score, BorderLayout.LINE_START);
 		score.add(player2Score, BorderLayout.LINE_END);
 		this.add(score, BorderLayout.SOUTH);
 		
 		JPanel panel = new JPanel();
 		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
 		panel.setBackground(Color.GRAY);
 		this.add(panel, BorderLayout.EAST);
 		Dimension d = new Dimension(200,40);
 		JButton reset = new JButton("Reset");
 		reset.setSize(d);
 		reset.setMinimumSize(d);
 		reset.setMaximumSize(d);
 		reset.setPreferredSize(d);
 		reset.addActionListener(new ButtonListener());
 		panel.add(reset);
 		JButton goHome = new JButton("Go To Home");
 		goHome.setSize(d);
 		goHome.setMinimumSize(d);
 		goHome.setMaximumSize(d);
 		goHome.setPreferredSize(d);
 		goHome.addActionListener(new ButtonListener());
 		panel.add(goHome);
 		
 		
 	}
 	
 	class ButtonListener implements ActionListener{
 
 		public void actionPerformed(ActionEvent e) {
 			System.out.println("HERE");
 			System.out.println(e.getActionCommand());
 			if (e.getActionCommand().equals ("Reset")){
 				Runner.getGame(currentNumOfBoxes, currentPlayer2);
 				reset();
 				System.out.println(e.getActionCommand());
 			}
 			if(e.getActionCommand().equals("Go To Home")){
 				Runner.enableWindow();
 				Runner.getHomePage();
 			}
 		}
 	}
 	
 	
 	public static void reset(){
 		SingletonStatus.getInstance().reset();
 		turnStatus.setText("Player 1's Turn");
 		player1Score.setText("Player 1: 0");
 		player2Score.setText("Player 2: 0");
 	}
 	
 	public static void setTurn(){
 		if(board.checkComplete()){
 			turnStatus.setText("Game Complete");
 			game = FinishGame.getInstance();
			game.setVisible(true);
 			Runner.disableWindow();
 			return;
 		}
 		if(SingletonStatus.getInstance().getTurn()){
 			turnStatus.setText("Player 1's turn");
 		} else{
 			turnStatus.setText("Player 2's turn");
 		}
 	}
 	
 	public static void closeFinish(){
 		game.dispose();
 	}
 	public static void refreshScore(){
 		player1Score.setText("Player 1: " + SingletonStatus.getInstance().getPlayer1Score());
 		player2Score.setText("Player 2: " + SingletonStatus.getInstance().getPlayer2Score());
 	}
 	public void setPlayer1Score(int score){
 		player1Score.setText("Player 1: " + score);
 	}
 	
 	public void setPlayer2Score(int score){
 		player2Score.setText("Player 2: " + score);
 	}
 	
 	
 }
 
