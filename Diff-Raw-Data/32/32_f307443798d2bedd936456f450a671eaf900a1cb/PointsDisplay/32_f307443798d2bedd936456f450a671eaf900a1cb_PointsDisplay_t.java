 package edu.txstate.hearts.gui;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.JLabel;
 import java.awt.Font;
 import java.awt.Image;
 import java.util.List;
 import javax.swing.JButton;
 import edu.txstate.hearts.controller.Hearts;
 import edu.txstate.hearts.model.Player;
 import edu.txstate.hearts.utils.ReadFiles;
 
 /**
  * This class displays the points of each player at the end of the game
  * @author Maria Poole, I Gede Sutapa
  *
  */
 @SuppressWarnings("serial")
 public class PointsDisplay extends JFrame 
 {
 	private JPanel contentPane;
 	private Hearts heartsController;
 	
 	/**
 	 * Create the frame.
 	 */
 	public PointsDisplay(Hearts heartsController, List<Player> players, String winnerName) 
 	{
 		this.heartsController = heartsController;
 		setTitle("Final Scores");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 327, 333);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(null);
 		
 		JLabel lblPlayerName = new JLabel("Player Name");
 		lblPlayerName.setFont(new Font("Tahoma", Font.PLAIN, 12));
 		lblPlayerName.setBounds(10, 25, 84, 27);
 		contentPane.add(lblPlayerName);
 		
 		JLabel lblPoints = new JLabel("Points");
 		lblPoints.setFont(new Font("Tahoma", Font.PLAIN, 12));
 		lblPoints.setBounds(171, 25, 84, 27);
 		contentPane.add(lblPoints);
 		
		JLabel lblUser = new JLabel(players.get(0).getName());
		lblUser.setBounds(10, 66, 84, 21);
		contentPane.add(lblUser);
 		
 		JLabel label = new JLabel(players.get(1).getName());
 		label.setBounds(10, 91, 84, 21);
 		contentPane.add(label);
 		
 		JLabel label_1 = new JLabel(players.get(2).getName());
 		label_1.setBounds(10, 116, 84, 21);
 		contentPane.add(label_1);
 		
		JLabel lblPlayer = new JLabel(players.get(3).getName());
		lblPlayer.setBounds(10, 141, 84, 21);
		contentPane.add(lblPlayer);
 		
 		JLabel lblPointvalu = new JLabel(players.get(0).getScore()+"");
		lblPointvalu.setBounds(171, 66, 46, 14);
 		contentPane.add(lblPointvalu);
 		
 		JLabel label_2 = new JLabel(players.get(1).getScore()+"");
 		label_2.setBounds(171, 91, 46, 14);
 		contentPane.add(label_2);
 		
 		JLabel label_3 = new JLabel(players.get(2).getScore()+"");
 		label_3.setBounds(171, 116, 46, 14);
 		contentPane.add(label_3);
 		
 		JLabel label_4 = new JLabel(players.get(3).getScore()+"");
 		label_4.setBounds(171, 141, 46, 14);
 		contentPane.add(label_4);
 		
 		JLabel lblTheWinnerIs = new JLabel("The Winner is:");
 		lblTheWinnerIs.setFont(new Font("Tahoma", Font.PLAIN, 13));
 		lblTheWinnerIs.setBounds(10, 195, 99, 27);
 		contentPane.add(lblTheWinnerIs);
 		
 		JLabel lblBla = new JLabel(winnerName);
 		lblBla.setFont(new Font("Tahoma", Font.PLAIN, 13));
 		lblBla.setBounds(166, 195, 99, 27);
 		contentPane.add(lblBla);
 		
 		JButton btnPlayAgain = new JButton("Play Again");
 		btnPlayAgain.putClientProperty("ButtonType", "PointDisplayPlayAgain");
 		btnPlayAgain.addActionListener(this.heartsController);
 		btnPlayAgain.setBounds(32, 246, 113, 38);
 		contentPane.add(btnPlayAgain);
 		
 		JButton btnQuit = new JButton("Quit");
		btnQuit.putClientProperty("ButtonType", "PointDisplayQuit");
 		btnQuit.addActionListener(this.heartsController);
 		btnQuit.setBounds(155, 246, 110, 38);
 		contentPane.add(btnQuit);
 		
 		Image image = ReadFiles.getImage("heart.png");
 		this.setIconImage(image);
 	}
 	
 	
 	/**
 	 * Show UI in center screen
 	 */
 	public void showDialog() 
 	{
 		try 
 		{
 			this.setLocationRelativeTo(null);
 			this.setVisible(true);
 		} 
 		catch (Exception e) 
 		{
 			e.printStackTrace();
 		}
 	}
 }
