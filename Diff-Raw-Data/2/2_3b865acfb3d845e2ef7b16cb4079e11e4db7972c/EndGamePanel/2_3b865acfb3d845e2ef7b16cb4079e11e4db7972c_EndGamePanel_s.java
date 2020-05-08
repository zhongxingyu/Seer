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
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.TableModel;
 
 /**
  * The
  * <code>EndGamePanel</code> class displays game over screen.
  *
  * @author Haisin Yip
  *
  */
 public class EndGamePanel extends JPanel {
 
     // private properties
     private GameState gameState;
     private JTable StatisticsTable;
     private JScrollPane scrollPane;
     private Image img;
     String player, highscore, asteroidsDestroyed, aliensDestroyed, killDeathRatio, level, bombs, shootingAccuracy;
     
     
     /**
      * Creates EndGamePanel using given parameters. It initializes size, layout
      * and informative display.
      *
      * @param img image for EndGamePanel
      * @param gameState current game state
      * @param singleP boolean value representing single player mode or 2 player
      * mode.
      */
     public EndGamePanel(Image img, GameState gameState, boolean singleP) {
         this.gameState = gameState;
 
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
     private void makeComponents(int width, int height, boolean singleP) {
         // informative content will be displayed at end game
         
 
         //when single player, display player's information
         if (singleP) {
             player = "p1";
             highscore = String.valueOf(gameState.getCurrentScore());  
             asteroidsDestroyed = String.valueOf(gameState.getP1asteroidDestroyed());  
             aliensDestroyed = String.valueOf(gameState.getP1alienDestroyed());
             killDeathRatio = String.valueOf((double)gameState.getP1asteroidDestroyed()/(double)gameState.getP1deaths());
             if(gameState.getP1deaths() == 0 )
             {
                 killDeathRatio = String.valueOf(0);
             }
             level = String.valueOf(gameState.getLevel());
             bombs = String.valueOf(gameState.getP1BombUsed());
             shootingAccuracy = String.valueOf(100*(double)gameState.getP1asteroidDestroyed()/(double)gameState.getP1shootCounter());
             if(gameState.getP1shootCounter() == 0 )
             {
                 shootingAccuracy = String.valueOf(0);
             }
         } //two player
         else {
             //player 1, player 2 scores
             int highscoreP1 = gameState.getPlayer1Score();
             int highscoreP2 = gameState.getPlayer2Score();
             
             //display winner's score
             if (highscoreP1 >= highscoreP2) {
                 player = "p1";
                 highscore = String.valueOf(highscoreP1);
                 asteroidsDestroyed = String.valueOf(gameState.getP1asteroidDestroyed());
                 aliensDestroyed = String.valueOf(gameState.getP1alienDestroyed());
                 killDeathRatio = String.valueOf((double)gameState.getP1asteroidDestroyed()/(double)gameState.getP1deaths());
                 if(gameState.getP1deaths() == 0 )
                 {
                     killDeathRatio = String.valueOf(0);
                 }
                 level = String.valueOf(gameState.getPlayer1Level());
                 bombs = String.valueOf(gameState.getP1BombUsed());
                 shootingAccuracy = String.valueOf(100*(double)gameState.getP1asteroidDestroyed()/(double)gameState.getP1shootCounter());
                 if(gameState.getP1shootCounter() == 0 )
                 {
                     shootingAccuracy = String.valueOf(0);
                 }
             } else {
                 player = "p2";
                 highscore = String.valueOf(highscoreP2);
                 asteroidsDestroyed = String.valueOf(gameState.getP2asteroidDestroyed());
                 aliensDestroyed = String.valueOf(gameState.getP2alienDestroyed());
                 killDeathRatio = String.valueOf((double)gameState.getP2asteroidDestroyed()/(double)gameState.getP1deaths());
                 if(gameState.getP1deaths() == 0 )
                 {
                     killDeathRatio = String.valueOf(0);
                 }
                 level = String.valueOf(gameState.getPlayer2Level());
                 bombs = String.valueOf(gameState.getP2BombUsed());
                 shootingAccuracy = String.valueOf(100.0*(double)gameState.getP2asteroidDestroyed()/(double)gameState.getP1shootCounter());
                 if(gameState.getP1shootCounter() == 0 )
                 {
                     shootingAccuracy = String.valueOf(0);
                 }
             }
         }
         
         String[] columnData = {"", ""};
         String[][] rowData = {{"Player name", player}, {"Highscore", highscore}, {"Asteroids Destroyed", asteroidsDestroyed}, {"Aliens Destroyed", aliensDestroyed}, {"Kill-Death ratio", killDeathRatio}, {"Last level", level}, {"Bombs used", bombs}, {"Shooting Accuracy", shootingAccuracy+"%"}};
         
         StatisticsTable = new JTable(rowData, columnData){
             @Override
             public boolean isCellEditable(int rowData, int columnData){
                 if (rowData == 0 && columnData == 1){
                     return true;
                 }
                 else {
                     return false;
                 }
             }
         };
         
         StatisticsTable.getModel().addTableModelListener(
                 new TableModelListener()
                 {
                     @Override
                     public void tableChanged(TableModelEvent e)
                     {
                         // get cell update
                         int row = e.getFirstRow();
                         int column = e.getColumn();
                         TableModel model = (TableModel)e.getSource();
                         String columnName = model.getColumnName(column);
                         Object data = model.getValueAt(row, column);
                         String customName = data.toString();
                         
                         // if the name is changed to p1 or p2, do not write to file
                         if(!customName.equals("p1") &&  !customName.equals("p2"))
                         {
                             String[] newScoreData = {customName + " ", highscore + " ", asteroidsDestroyed + " ", aliensDestroyed + " ", killDeathRatio + " ", level + " ", bombs + " ", shootingAccuracy};
                             highScoreWriter writer = new highScoreWriter(newScoreData, "./src/highscoreData/scoreInfo.txt");
                             writer.writeToFile();
                         }
                     }
                 });
         
        // write write to highscore text file with default name p1 or p2
         if(!player.equals("p1") && !player.equals("p2")){
             System.out.println(player);
             String[] scoreData = {player + " ", highscore + " ", asteroidsDestroyed + " ", aliensDestroyed + " ", killDeathRatio + " ", level + " ", bombs + " ", shootingAccuracy};
             highScoreWriter writer = new highScoreWriter(scoreData, "./src/highscoreData/scoreInfo.txt");
             writer.writeToFile();
         }
         
         //disable selecting for all cells in table
         StatisticsTable.setRowSelectionAllowed( false );
         StatisticsTable.setColumnSelectionAllowed( false );
         StatisticsTable.setCellSelectionEnabled( false );
         StatisticsTable.setPreferredScrollableViewportSize(new Dimension(width / 2, height / 6));
         StatisticsTable.setFillsViewportHeight(true);
     }
 
     // set the layout with a scrollable table
     private void makeLayout() {
         setLayout(new FlowLayout());
         add(StatisticsTable);
         add(new JScrollPane(StatisticsTable));
     }
 
     
     /**
      * Sets endgame background image.
      *
      * @param graphic image for background
      */
     @Override
     public void paintComponent(Graphics graphic) {
         graphic.drawImage(img, 0, 0, getWidth(), getHeight(), null);
     }
 }
