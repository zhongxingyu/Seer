 package battleship.view;
 
 import battleship.data.UserFile;
 import battleship.objects.User;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.List;
 
 /**
  * highscore Panel
  *
  * @author Hinrich Kaestner, Tom Ohme
  */
 public class Highscore extends JFrame implements ActionListener {
 
 	private static final long serialVersionUID = 1L;
 	private Application app;
 	
 	JLabel showUsername = new JLabel("Username");
 	JLabel showGames = new JLabel("Games");
 	JLabel showVictories = new JLabel("Victories");
 	JLabel showDefeats = new JLabel("Defeats");
 	JLabel showDifference = new JLabel("Difference");
 	JButton back = new JButton("BACK");
 	JPanel highscorePanel = new JPanel();
 
     /**
      * set the highscore Panel with the titel
      *
      * @param app
      */
 	public Highscore(Application app) {
 		this.app = app;
 		
 		setTitle("Highscore Battleship");
 		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 		int top = (screenSize.height - 400) / 2;
 		int left = (screenSize.width - 600) / 2;
 		setSize(600, 400);
 		setLocation(left, top);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setResizable(false);
 		
 		showUsername.setFont(new Font("Arial", Font.BOLD, 14));
 		showGames.setFont(new Font("Arial", Font.BOLD, 14));
 		showVictories.setFont(new Font("Arial", Font.BOLD, 14));
 		showDefeats.setFont(new Font("Arial", Font.BOLD, 14));
 		showDifference.setFont(new Font("Arial", Font.BOLD, 14));
 		
 		highscorePanel.setLayout(null);
 		
 		showUsername.setBounds(50, 30, 110, 25);
 		showGames.setBounds(160, 30, 110, 25);
 		showVictories.setBounds(255, 30, 110, 25);
 		showDefeats.setBounds(365, 30, 110, 25);
 		showDifference.setBounds(475, 30, 110, 25);
 		back.setBounds(50, 325, 110, 25);
 		
 		showUsername.setForeground(Color.ORANGE);
 		showGames.setForeground(Color.ORANGE);
 		showVictories.setForeground(Color.ORANGE);
 		showDefeats.setForeground(Color.ORANGE);
 		showDifference.setForeground(Color.ORANGE);
 		back.setBackground(Color.BLACK);
 		back.setForeground(Color.WHITE);
 		back.setFocusable(false);
 		
 		highscorePanel.add(showUsername);
 		highscorePanel.add(showGames);
 		highscorePanel.add(showVictories);
 		highscorePanel.add(showDefeats);
 		highscorePanel.add(showDifference);
 		highscorePanel.add(back);
 		
 		showUser();
 		
 		highscorePanel.setBackground(Color.BLACK);
 		
 		getContentPane().add(highscorePanel);
 		
 		actionHighscore();
 	}
 
     /**
      * add ActionListener on back and print
      */
 	public void actionHighscore() {
 		back.addActionListener(this);
 	}
 
     /**
      * back Button it goes to the menu
      *
      * @param e
      */
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource() == back) {
 			app.menu();
 		}
 	}
 
     /**
      * show the best 7 username sort by there difference between victories and defeats
      * and there number of games, victories, defeats and the difference between victories and defeats
      */
 	public void showUser() {
 		List<User> userList = new UserFile().readAll();
 		for (int i=0; i<userList.size()-1; i++) {
 			compare(userList.get(i), userList.get(i+1));
 		}
 		int bestUser = 0;
 		int y = 60;
 		for (User user : userList) {
 			if (bestUser < 8 && user.getGamesCount() != 0) {
 		    	JLabel username = new JLabel(user.getName());
                 username.setBounds(50, y, 110, 25);
 				username.setForeground(Color.WHITE);
 				highscorePanel.add(username);
 				String strGames = String.valueOf(user.getGamesCount());
 				JLabel games = new JLabel(strGames);
 				games.setBounds(160, y, 110, 25);
 				games.setForeground(Color.WHITE);
 				highscorePanel.add(games);
 				String strVictories = String.valueOf(user.getVictories());
 				JLabel victories = new JLabel(strVictories);
 				victories.setBounds(255, y, 110, 25);
 				victories.setForeground(Color.WHITE);
 				highscorePanel.add(victories);
 				String strDefeats = String.valueOf(user.getDefeats());
 				JLabel defeats = new JLabel(strDefeats);
 				defeats.setBounds(365, y, 110, 25);
 				defeats.setForeground(Color.WHITE);
 				highscorePanel.add(defeats);
 				String strDifference = String.valueOf(user.getScore());
 				JLabel difference = new JLabel(strDifference);
 				difference.setBounds(475, y, 110, 25);
 				difference.setForeground(Color.WHITE);
 				highscorePanel.add(difference);
 				y=y+30;
 			}
			bestUser++;
            if (user == null) {
                JLabel empty = new JLabel("no entries existing");
                empty.setFont(new Font("Arial", Font.BOLD, 16));
                empty.setBounds(220, 175, 200, 25);
                empty.setForeground(Color.WHITE);
                highscorePanel.add(empty);
            }
 		}
 	}
 
     /**
      * sort the user by the difference between victories and defeats
      *
      * @param inUserLeft
      * @param inUserRight
      * @return
      */
 	private int compare(User inUserLeft, User inUserRight) {
 		int difference = inUserLeft.getScore() - inUserRight.getScore();
 		if (difference == 0) {
 			if (inUserLeft.getScore() > inUserRight.getScore()) {
 				return 1;
 			} else if (inUserLeft.getScore() == inUserRight.getScore()) {
 				return 0;
 			} else {
 				return -1;
 			}
 		} else {
 			return difference;
 		}
 	}
 	
 }
