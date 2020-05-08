 package com.UI;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.WindowConstants;
 import com.state.SingletonStatus;
 
 public class FinishGame extends JFrame{
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	private static final String WINDOW_TITLE = "Game Finished";
 	
 	private JLabel winnerText;
 	private JLabel player1Score;
 	private JLabel player2Score;
 	
 	
 	private static FinishGame instance;
 	public static FinishGame getInstance(){
 		if(instance == null){
 			instance = new FinishGame();
 		}
 		return instance;
 	}
 	
 	private FinishGame(){
 		super(WINDOW_TITLE);
 		winnerText = new JLabel();
 		winnerText.setText("");
 		//winnerText.setEditable(false);
 		player1Score = new JLabel();
 		player1Score.setText("");
 		//player1Score.setEditable(false);
 		player2Score = new JLabel();
 		player2Score.setText("");
 		//player2Score.setEditable(false);
 		this.getContentPane().add(winnerText);
 		this.getContentPane().add(player1Score);
 		this.getContentPane().add(player2Score);
 		this.setSize(250, 200);
 		JPanel panel = new JPanel();
 		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
 		winnerText.setAlignmentX(Component.CENTER_ALIGNMENT);
 		panel.add(winnerText);
 		panel.add(Box.createRigidArea(new Dimension (0,15)));
 		
 		JLabel finalScore = new JLabel("Final Score:");
 		finalScore.setAlignmentX(Component.CENTER_ALIGNMENT);
 		panel.add(finalScore);
 		panel.add(Box.createRigidArea(new Dimension (0,10)));		
 		
 		player1Score.setAlignmentX(Component.CENTER_ALIGNMENT);
 		panel.add(player1Score);
 		panel.add(Box.createRigidArea(new Dimension (0,10)));
 		
 		player2Score.setAlignmentX(Component.CENTER_ALIGNMENT);
 		panel.add(player2Score);
 		panel.add(Box.createRigidArea(new Dimension (0,15)));
 		
 		JButton goTo = new JButton("Go To Home");
 		goTo.addActionListener(new ButtonListener());
 		goTo.setAlignmentX(Component.CENTER_ALIGNMENT);
 		panel.add(goTo);
 		this.add(panel);
 		winner();
 		this.setVisible(true);
 		this.addWindowListener(new WindowEventHandler());
 		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 	}
 		
 		
 	public void winner(){
 		if(SingletonStatus.getInstance().getPlayer1Score() > SingletonStatus.getInstance().getPlayer2Score()){
 			winnerText.setText("Player 1 wins!");
 			player1Score.setText("Player 1: " + SingletonStatus.getInstance().getPlayer1Score());
 			player2Score.setText("Player 2: " + SingletonStatus.getInstance().getPlayer2Score());
 		}
 		else if (SingletonStatus.getInstance().getPlayer2Score() > SingletonStatus.getInstance().getPlayer1Score()){
 			winnerText.setText("Player 2 wins!");
 			player1Score.setText("Player 1: " + SingletonStatus.getInstance().getPlayer1Score());
 			player2Score.setText("Player 2: " + SingletonStatus.getInstance().getPlayer2Score());
 			
 		}
 		else{
 			winnerText.setText("Game Draw!");
 			player1Score.setText("Player 1: " + SingletonStatus.getInstance().getPlayer1Score());
 			player2Score.setText("Player 2: " + SingletonStatus.getInstance().getPlayer2Score());
 		}
 	}
 	
 	class WindowEventHandler extends WindowAdapter {
 		  public void windowClosing(WindowEvent evt) {
 		    Runner.enableWindow();
 		  }
 		}
 	
 	class ButtonListener implements ActionListener{
 
 		public void actionPerformed(ActionEvent e) {
 			if (e.getActionCommand().equals ("Go To Home")){
 				MainPage.closeFinish();
 				Runner.enableWindow();
 				Runner.getHomePage();				
 			}	
 		}
 	}	
 	
 }
