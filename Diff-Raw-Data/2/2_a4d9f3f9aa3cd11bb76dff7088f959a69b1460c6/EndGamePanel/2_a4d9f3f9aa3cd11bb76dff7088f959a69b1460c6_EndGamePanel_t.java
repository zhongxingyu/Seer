 package gui;
 
 import game.GameState;
 import highscoreData.highScoreWriter;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Graphics;
 import java.awt.Image;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 
 /**
  * The
  * <code>EndGamePanel</code> class displays game over screen.
  *
  * @author Haisin Yip
  *
  */
 public class EndGamePanel extends JPanel {
 
     // private properties
     private GameState gamestate;
     private JTable StatisticsTable;
     private JScrollPane scrollPane;
     private Image img;
 
     // initialize size, layout and informative display
     /**
      * Creates EndGamePanel using given parameters. It initializes size, layout
      * and informative display.
      *
      * @param img image for EndGamePanel
      * @param gs current game state
      * @param singleP boolean value representing single player mode or 2 player
      * mode.
      */
     public EndGamePanel(Image img, GameState gs, boolean singleP) {
         this.gamestate = gs;
 
         this.img = img;
         Dimension size = new Dimension(img.getWidth(null), img.getHeight(null));
         setPreferredSize(size);
         setMinimumSize(size);
         setMaximumSize(size);
         setSize(size);
         setLayout(null);
 
         makeComponents(getWidth(), getHeight(), singleP);
         makeLayout();
     }
 
     // construct the main components and informative content 
     private void makeComponents(int w, int h, boolean singleP) {
         // informative content will be displayed at end game
         String player, highscore, asteroidsDestroyed, aliensDestroyed, killDeathRatio, level, bombs, shootingAccuracy;
 
         //when single player, display player's information
         if (singleP) {
             player = "p1";
             highscore = String.valueOf(gamestate.getCurrentScore());
             asteroidsDestroyed = String.valueOf(gamestate.getP1asteroidDestroyed());
             aliensDestroyed = String.valueOf(gamestate.getP1alienDestroyed());;
             killDeathRatio = "";
             level = String.valueOf(gamestate.getLevel());
             bombs = String.valueOf(gamestate.getP1BombUsed());
             shootingAccuracy = "";
             //Total Shot used
             System.out.println(gamestate.getP1shootCounter());
         } //two player
         else {
             //player 1, player 2 scores
             int highscoreP1 = gamestate.getPlayer1Score();
             String levelP1 = String.valueOf(gamestate.getPlayer1Level());
 
             int highscoreP2 = gamestate.getPlayer2Score();
             String levelP2 = String.valueOf(gamestate.getPlayer2Level());
             
             //display winner's score
             if (highscoreP1 >= highscoreP2) {
                 player = "p1";
                 highscore = String.valueOf(highscoreP1);
                 bombs = String.valueOf(gamestate.getP1BombUsed());
                 aliensDestroyed = String.valueOf(gamestate.getP1alienDestroyed());
                 asteroidsDestroyed = String.valueOf(gamestate.getP1asteroidDestroyed());
                 level = levelP1;
             } else {
                 player = "p2";
                 highscore = String.valueOf(highscoreP2);
                 bombs = String.valueOf(gamestate.getP2BombUsed());
                 aliensDestroyed = String.valueOf(gamestate.getP2alienDestroyed());
                 asteroidsDestroyed = String.valueOf(gamestate.getP2asteroidDestroyed());
                 level = levelP2;
             }
         }
 
         //fill in table info
         String[] columnData = {"", ""};
         String[][] rowData = {{"Player name", player}, {"Highscore", highscore}, {"Asteroids Destroyed", asteroidsDestroyed}, {"Aliens Destroyed", aliensDestroyed}, {"Kill-Death ratio", "killDeathRatio"}, {"Last level", level}, {"Bombs used", bombs}, {"Shooting Accuracy", "shootingAccuracy"}};
         StatisticsTable = new JTable(rowData, columnData);
         StatisticsTable.setPreferredScrollableViewportSize(new Dimension(w / 2, h / 6));
         StatisticsTable.setFillsViewportHeight(true);
 
        String[] scoreData = {player + " ", highscore + " ", asteroidsDestroyed + " ", "aliensdestroyed ", "Kill-Deathratio ", level + " ", bombs + " ", "shootingaccuracy"};
         highScoreWriter writer = new highScoreWriter(scoreData, "./src/highscoreData/scoreInfo.txt");
         writer.writeToFile();
     }
 
     // set the layout with a scrollable table
     private void makeLayout() {
         setLayout(new FlowLayout());
         add(StatisticsTable);
         add(new JScrollPane(StatisticsTable));
     }
 
     // set endgame background image
     /**
      * Sets endgame background image.
      *
      * @param g image for background
      */
     @Override
     public void paintComponent(Graphics g) {
         g.drawImage(img, 0, 0, getWidth(), getHeight(), null);
     }
 }
