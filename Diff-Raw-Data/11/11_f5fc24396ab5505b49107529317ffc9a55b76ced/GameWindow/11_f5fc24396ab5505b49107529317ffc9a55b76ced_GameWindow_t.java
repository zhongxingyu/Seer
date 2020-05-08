 package edu.wctc.java.demo.tictactoe.ui;
 
 import edu.wctc.java.demo.tictactoe.domain.GameEngine;
 import edu.wctc.java.demo.tictactoe.domain.Tile;
 import java.awt.Color;
 import java.awt.event.ActionListener;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JOptionPane;
 
 /**
  * This class is the GUI representing the game window. It uses a custome
  * JButton, the Tile class, to represent the Xs and 0s. It also has three
  * collaborators: the GameEngine, to which it delegates for move processing,
  * the Rail, which holds winning combinations of Tile objects, and the 
  * Tile, which represents a square space on the board.
  * <P>
  * Notice how this program follows the MVC (Model-View-Controller) design
  * pattern and the Single Responsibility Principle. The "View" is this
  * UI class. It's job is to process input and display output views. The
  * "Controller" is the GameEngine. It's job is to process moves. The Rail and
  * Tile objects are "Model" objects. Models store data and state. The 
  * Controller gets data from the Model and then provides result data to the
  * View for display. Notice that only the View is responsible for GUI-related
  * input/output. None of the other classes even know that there is a view.
  * This means that it would be possible touse the GameEngine, Rail and Tile
  * classes in a different application, a web app or a console app, for
  * example, without changing the code in the controller or model classes.
  * Only a new View would be needed.
  * 
  * @author   Jim Lombardo, Lead Java Instructor, jlombardo@wctc.edu
  * @version  1.08
  */
 public class GameWindow extends javax.swing.JFrame implements ActionListener {
     private static final String EMPTY_TILE = "";
     private static final String COMP_WIN_MSG = "Computer Wins, Game Over!";
     private static final String YOU_WIN_MSG = "You Won, Game Over!";
     private static final String DRAW_MSG = "This game is a draw. No winner!";
     private static final String NEW_GAME_MSG = " Want to play a new game?";
     private static final String ICON = "/images/question-icon.png";
     private GameEngine game;
     
     /**
      * Creates new form GameWindow
      */
     public GameWindow() {
         initComponents();
        game = new GameEngine();
         startNewGame();
     }
     
     private void startNewGame() {
         Tile[] tiles = {
             (Tile)r1c1,(Tile)r1c2,(Tile)r1c3,
             (Tile)r2c1,(Tile)r2c2,(Tile)r2c3,
             (Tile)r3c1,(Tile)r3c2,(Tile)r3c3
         };  
         for (Tile tile : tiles) {
             tile.setText("");
         }
        game.initNewGame(tiles);
     }
     
     private void updateStats() {
         getCompWins().setText(""+ game.getoWins());
         getYouWins().setText(""+ game.getxWins());
         getDrawsTotal().setText(""+ game.getDraws());
     }    
  
    private void askStartNewGame(final String playerMsg) {
         updateStats();
         ImageIcon icon = createImageIcon(ICON);
         int result = JOptionPane.showConfirmDialog(getStatusMsg(), 
                 playerMsg + NEW_GAME_MSG, "Game Over", 
                 JOptionPane.OK_CANCEL_OPTION, 
                 JOptionPane.QUESTION_MESSAGE, icon);
         if (result == JOptionPane.OK_OPTION) {
             for(JButton tile : game.getTiles()) {
                 tile.setBackground(Color.WHITE);
                 tile.setText("");
             }
             startNewGame();
         } else {
             System.exit(0);
         }
     }
    
    /**
      * Creates an ImageIcon if the path is valid.
      * @param String - resource path
      * @param String - description of the file
      */
     private ImageIcon createImageIcon(String path) {
         java.net.URL imgURL = getClass().getResource(path);
         if (imgURL != null) {
             return new ImageIcon(imgURL);
         } else {
             System.err.println("Couldn't find file: " + path);
             return null;
         }
     }
     
    /*
     * A helper method that delegates to the GameEngine to actually process the
     * moves. However, the helper class responds to the moves and updates the
     * UI with relevant information.
     */
     private void processMove(final Tile tile) {
         if (tile.getText().equals(EMPTY_TILE)) {
             statusMsg.setText("Good move!");
             tile.setText("X");
             game.incrementTilesPlayed();
             
             if (game.checkForDraw()) {
                 statusMsg.setText(DRAW_MSG);
                 askStartNewGame(DRAW_MSG);
                 return;
             }
 
             if (game.checkForWin()) {
                 if (game.getWinningPlayer().equals("X")) {
                     statusMsg.setText(YOU_WIN_MSG);
                 } else {
                     statusMsg.setText(COMP_WIN_MSG);
                 }
                 askStartNewGame(YOU_WIN_MSG);
                 return;
             }
 
             Tile tile0 = game.selectComputerMove();
             while (tile0.getText().length() > 0) {
                 tile0 = game.selectComputerMove();
             }
             tile0.setText("0");
             game.incrementTilesPlayed();
             
             if (game.checkForWin()) {
                 if (game.getWinningPlayer().equals("X")) {
                     statusMsg.setText(YOU_WIN_MSG);
                 } else {
                     statusMsg.setText(COMP_WIN_MSG);
                 }
                 askStartNewGame(COMP_WIN_MSG);
                 return;
             }
             
         } else {
             statusMsg.setText("Sorry, that tile is taken!");
         }
     }
     
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jPanel1 = new javax.swing.JPanel();
         jLabel1 = new javax.swing.JLabel();
         compWins = new javax.swing.JLabel();
         jLabel3 = new javax.swing.JLabel();
         youWins = new javax.swing.JLabel();
         jLabel5 = new javax.swing.JLabel();
         drawsTotal = new javax.swing.JLabel();
         r1c1 = new Tile();
         r1c2 = new Tile();
         r1c3 = new Tile();
         r2c1 = new Tile();
         r2c2 = new Tile();
         r2c3 = new Tile();
         r3c1 = new Tile();
         r3c2 = new Tile();
         r3c3 = new Tile();
         statusMsg = new javax.swing.JLabel();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setTitle("TicTacToe in Java v1.0.8");
         setResizable(false);
 
         jPanel1.setBackground(new java.awt.Color(0, 0, 0));
 
         jLabel1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
         jLabel1.setForeground(new java.awt.Color(255, 255, 255));
         jLabel1.setText("Computer:");
 
         compWins.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
         compWins.setForeground(new java.awt.Color(255, 255, 255));
         compWins.setText("0");
 
         jLabel3.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
         jLabel3.setForeground(new java.awt.Color(255, 255, 255));
         jLabel3.setText("You:");
 
         youWins.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
         youWins.setForeground(new java.awt.Color(255, 255, 255));
         youWins.setText("0");
 
         jLabel5.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
         jLabel5.setForeground(new java.awt.Color(255, 255, 255));
         jLabel5.setText("Draws:");
 
         drawsTotal.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
         drawsTotal.setForeground(new java.awt.Color(255, 255, 255));
         drawsTotal.setText("0");
 
         javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jLabel1)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(compWins)
                 .addGap(18, 18, 18)
                 .addComponent(jLabel3)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(youWins)
                 .addGap(18, 18, 18)
                 .addComponent(jLabel5)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(drawsTotal)
                 .addContainerGap(46, Short.MAX_VALUE))
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                 .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addComponent(compWins, javax.swing.GroupLayout.DEFAULT_SIZE, 26, Short.MAX_VALUE)
                 .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addComponent(youWins, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addComponent(drawsTotal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         r1c1.setFont(new java.awt.Font("Tahoma", 1, 48)); // NOI18N
         r1c1.setName("r1c1");
         r1c1.addActionListener(this);
 
         r1c2.setFont(new java.awt.Font("Tahoma", 1, 48)); // NOI18N
         r1c2.setName("r1c2");
         r1c2.addActionListener(this);
 
         r1c3.setFont(new java.awt.Font("Tahoma", 1, 48)); // NOI18N
         r1c3.setName("r1c3");
         r1c3.addActionListener(this);
 
         r2c1.setFont(new java.awt.Font("Tahoma", 1, 48)); // NOI18N
         r2c1.setName("r2c1");
         r2c1.addActionListener(this);
 
         r2c2.setFont(new java.awt.Font("Tahoma", 1, 48)); // NOI18N
         r2c2.setName("r2c2");
         r2c2.addActionListener(this);
 
         r2c3.setFont(new java.awt.Font("Tahoma", 1, 48)); // NOI18N
         r2c3.setName("r2c3");
         r2c3.addActionListener(this);
 
         r3c1.setFont(new java.awt.Font("Tahoma", 1, 48)); // NOI18N
         r3c1.setName("r3c1");
         r3c1.addActionListener(this);
 
         r3c2.setFont(new java.awt.Font("Tahoma", 1, 48)); // NOI18N
         r3c2.setName("r3c2");
         r3c2.addActionListener(this);
 
         r3c3.setFont(new java.awt.Font("Tahoma", 1, 48)); // NOI18N
         r3c3.setName("r3c3");
         r3c3.addActionListener(this);
 
         statusMsg.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
         statusMsg.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         statusMsg.setText("Click a button to start new game");
         statusMsg.setBorder(javax.swing.BorderFactory.createEtchedBorder());
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addGroup(layout.createSequentialGroup()
                 .addGap(33, 33, 33)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(r3c1, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(r3c2, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(r3c3, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(r2c1, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(r2c2, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(r2c3, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(r1c1, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(r1c2, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(r1c3, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap(26, Short.MAX_VALUE))
             .addComponent(statusMsg, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(r1c1, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(r1c2, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(r1c3, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(r2c1, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(r2c2, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(r2c3, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(r3c1, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(r3c2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(r3c3, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addComponent(statusMsg)
                 .addGap(4, 4, 4))
         );
 
         java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
         setBounds((screenSize.width-300)/2, (screenSize.height-321)/2, 300, 321);
     }
 
     // Code for dispatching events from components to event handlers.
 
     public void actionPerformed(java.awt.event.ActionEvent evt) {
         if (evt.getSource() == r1c1) {
             GameWindow.this.r1c1ActionPerformed(evt);
         }
         else if (evt.getSource() == r1c2) {
             GameWindow.this.r1c2ActionPerformed(evt);
         }
         else if (evt.getSource() == r1c3) {
             GameWindow.this.r1c3ActionPerformed(evt);
         }
         else if (evt.getSource() == r2c1) {
             GameWindow.this.r2c1ActionPerformed(evt);
         }
         else if (evt.getSource() == r2c2) {
             GameWindow.this.r2c2ActionPerformed(evt);
         }
         else if (evt.getSource() == r2c3) {
             GameWindow.this.r2c3ActionPerformed(evt);
         }
         else if (evt.getSource() == r3c1) {
             GameWindow.this.r3c1ActionPerformed(evt);
         }
         else if (evt.getSource() == r3c2) {
             GameWindow.this.r3c2ActionPerformed(evt);
         }
         else if (evt.getSource() == r3c3) {
             GameWindow.this.r3c3ActionPerformed(evt);
         }
     }// </editor-fold>//GEN-END:initComponents
 
     private void r1c1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_r1c1ActionPerformed
         // TODO add your handling code here:
         processMove((Tile)r1c1);
     }//GEN-LAST:event_r1c1ActionPerformed
 
     private void r1c2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_r1c2ActionPerformed
         // TODO add your handling code here:
         processMove((Tile)r1c2);
     }//GEN-LAST:event_r1c2ActionPerformed
 
     private void r1c3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_r1c3ActionPerformed
         // TODO add your handling code here:
         processMove((Tile)r1c3);
     }//GEN-LAST:event_r1c3ActionPerformed
 
     private void r2c1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_r2c1ActionPerformed
         // TODO add your handling code here:
         processMove((Tile)r2c1);
     }//GEN-LAST:event_r2c1ActionPerformed
 
     private void r2c2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_r2c2ActionPerformed
         // TODO add your handling code here:
         processMove((Tile)r2c2);
     }//GEN-LAST:event_r2c2ActionPerformed
 
     private void r2c3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_r2c3ActionPerformed
         // TODO add your handling code here:
         processMove((Tile)r2c3);
     }//GEN-LAST:event_r2c3ActionPerformed
 
     private void r3c1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_r3c1ActionPerformed
         // TODO add your handling code here:
         processMove((Tile)r3c1);
     }//GEN-LAST:event_r3c1ActionPerformed
 
     private void r3c2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_r3c2ActionPerformed
         // TODO add your handling code here:
         processMove((Tile)r3c2);
     }//GEN-LAST:event_r3c2ActionPerformed
 
     private void r3c3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_r3c3ActionPerformed
         // TODO add your handling code here:
         processMove((Tile)r3c3);
     }//GEN-LAST:event_r3c3ActionPerformed
 
     public javax.swing.JLabel getCompWins() {
         return compWins;
     }
     
     public javax.swing.JLabel getDrawsTotal() {
         return drawsTotal;
     }
     
     public javax.swing.JLabel getStatusMsg() {
         return statusMsg;
     }
     
     public javax.swing.JLabel getYouWins() {
         return youWins;
     }
     
     
     
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JLabel compWins;
     private javax.swing.JLabel drawsTotal;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JButton r1c1;
     private javax.swing.JButton r1c2;
     private javax.swing.JButton r1c3;
     private javax.swing.JButton r2c1;
     private javax.swing.JButton r2c2;
     private javax.swing.JButton r2c3;
     private javax.swing.JButton r3c1;
     private javax.swing.JButton r3c2;
     private javax.swing.JButton r3c3;
     private javax.swing.JLabel statusMsg;
     private javax.swing.JLabel youWins;
     // End of variables declaration//GEN-END:variables
 }
