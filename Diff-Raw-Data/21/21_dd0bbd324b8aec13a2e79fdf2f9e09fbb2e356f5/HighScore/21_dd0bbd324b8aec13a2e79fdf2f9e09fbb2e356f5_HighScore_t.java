 import javax.swing.*;
 import java.awt.*;
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.StringTokenizer;
 
 /**
  * User: FeliciousX
  * Date: 5/23/13
  * Time: 11:02 PM
  *
  * HighScore class manages the I/O of high scores as well as displaying scores
  * to the user.
  */
 public class HighScore {
    private static final String HIGHSCORE_FILENAME = "High_Scores.txt";
     private static final int MAX_PLAYERS = 10;
 
     private ArrayList<Player> playerArray;
 
     public HighScore() {
         playerArray = new ArrayList<Player>();
         getHighScorers();
     }
 
     public int getLowestHighScore() {
         if (playerArray.size() >= MAX_PLAYERS) {
             return playerArray.get(playerArray.size() - 1).getScore();
         }
         else {
             return 0;
         }
     }
 
     public void displayScores() {
         JDialog scoreDialog = new JDialog();
         scoreDialog.setTitle("***** High Scores! *****");
         scoreDialog.setPreferredSize(new Dimension(MainFrame.getMaxWidth(), MainFrame.getMaxHeight()));
         scoreDialog.setResizable(false);
         scoreDialog.setLayout(new GridLayout(0, 3));
         JLabel lblNum = new JLabel("Position");
         JLabel lblName = new JLabel("Name");
         JLabel lblScores = new JLabel("Scores");
         JLabel lblPlayerName;
         JLabel lblPlayerScore;
         lblNum.setHorizontalTextPosition(JLabel.RIGHT);
         lblNum.setHorizontalAlignment(JLabel.RIGHT);
         lblName.setHorizontalTextPosition(JLabel.CENTER);
         lblName.setHorizontalAlignment(JLabel.CENTER);
         lblScores.setHorizontalTextPosition(JLabel.CENTER);
         lblScores.setHorizontalAlignment(JLabel.CENTER);
         lblNum.setFont(new Font("Serif", Font.BOLD, 27));
         lblName.setFont(new Font("Serif", Font.BOLD, 27));
         lblScores.setFont(new Font("Serif", Font.BOLD, 27));
         scoreDialog.add(lblNum);
         scoreDialog.add(lblName);
         scoreDialog.add(lblScores);
 
         int i = 1;
         for (Player p : playerArray) {
             lblPlayerName = new JLabel(p.getName());
             lblPlayerScore = new JLabel("" + p.getScore());
 
             lblPlayerName.setHorizontalTextPosition(JLabel.RIGHT);
             lblPlayerScore.setHorizontalTextPosition(JLabel.LEFT);
             lblPlayerName.setHorizontalAlignment(JLabel.CENTER);
             lblPlayerScore.setHorizontalAlignment(JLabel.CENTER);
             scoreDialog.add(new JLabel("" + i++ + ".", JLabel.RIGHT));
             scoreDialog.add(lblPlayerName);
             scoreDialog.add(lblPlayerScore);
         }
 
         scoreDialog.pack();
 
         final int x = (Assign2.getScreenSize().width - scoreDialog.getWidth()) / 2;
         final int y = (Assign2.getScreenSize().height - scoreDialog.getHeight()) / 2;
 
         scoreDialog.setLocation(x, y);
         scoreDialog.setVisible(true);
     }
     public int getNumberOfPlayers() {
         return this.playerArray.size();
     }
 
     public static int getMaxPlayers() {
         return MAX_PLAYERS;
     }
 
     private void getHighScorers() {
 
         BufferedReader input = null;
         String line;
 
         try {
             input = new BufferedReader(new FileReader(HighScore.HIGHSCORE_FILENAME));
                int i = 0;
             while (input.ready()) {
                 line = input.readLine().trim();
                 String [] token = line.split(",");
                 playerArray.add(new Player(token[0], Integer.parseInt(token[1])));
             }
         }
         catch (FileNotFoundException e) {
             JOptionPane.showMessageDialog(null, "You are our first player!", "Welcome!", JOptionPane.INFORMATION_MESSAGE);
         }
         catch (IOException e) {
             JOptionPane.showMessageDialog(null, "Failed to read from file (" + HIGHSCORE_FILENAME + ").", "ERROR!", JOptionPane.ERROR_MESSAGE);
             System.out.println(e.toString());
         }
         finally {
             try {
                 if (input != null) {
                     input.close();
                 }
             }
             catch (IOException e) {
                 JOptionPane.showMessageDialog(null, "Failed to close connection to file buffer!", "ERROR!", JOptionPane.ERROR_MESSAGE);
                 System.out.println(e.toString());
             }
         }
     }
 
     public void update(Player player) {
         if (playerArray.size() >= MAX_PLAYERS)
             playerArray.remove(playerArray.size() - 1);
 
         playerArray.add(player);
         Collections.sort(playerArray);
 
 
         this.writeToFile();
     }
 
     private void writeToFile() {
         PrintWriter output = null;
 
         try {
             output = new PrintWriter(new FileWriter(HIGHSCORE_FILENAME), true);
 
             for (Player p : playerArray) {
                 output.println(p.toString());
             }
         }
         catch (FileNotFoundException e) {
             JOptionPane.showMessageDialog(null, "New file created! (" + HIGHSCORE_FILENAME + ")", "Info", JOptionPane.INFORMATION_MESSAGE);
         }
         catch (IOException e) {
             JOptionPane.showMessageDialog(null, "Failed to write to file (" + HIGHSCORE_FILENAME + ").", "ERROR!", JOptionPane.ERROR_MESSAGE);
             System.out.println(e.toString());
         }
         finally {
             if (output != null) {
                 output.close();
             }
         }
     }
 }
