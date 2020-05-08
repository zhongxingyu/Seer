 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.HashSet;
 import java.util.Random;
 import java.util.Set;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.Timer;
 
 /**
  * Runs game operations and handles graphics.  Blocks that have already reached
  * the bottom are
  * stored in a matrix representing the game-play grid.  Blocks that are still
  * in motion are stored by a single Tetra, their positions represented as part
  * of a hypothetical grid. Note: The Tetra that is currently in motion is
  * referred to in comments as the "current Tetra", while the Blocks not in
  * motion are referred to as the "pile".
  * @author Zach Cotter
  */
 public class World extends JPanel {
 
     //Game and Graphics Constants
     public static final int GRID_HEIGHT = 20;
     public static final int GRID_WIDTH = 10;
     public static final int BLOCK_SIZE = 30;
     public static final int PANEL_WIDTH = BLOCK_SIZE * GRID_WIDTH;
     public static final int PANEL_HEIGHT = BLOCK_SIZE * GRID_HEIGHT;
     private static final int MAX_CHARS_ON_HIGH_SCORE_LINE = 23; //includes score
     private static final String PAUSE_TEXT = "     Pause";
     private static final String NEW_GAME_TEXT = "  New Game";
     private static final String CONTINUE_TEXT = "   Continue";
     private static final int TIMER_INITIAL_DELAY = 600;
     private static final int DEFAULT_COMPONENT_SEPARATOR = 10;
     private static final float SCORE_TEXT_FONT_SIZE = 20;
     private static final int SCORE_TEXT_X_LOCATION = 10;
     private static final int SCORE_TEXT_Y_LOCATION = 15;
     //Velocity increases by 20 milliseconds per 1000 points, see below
     private static final int TIMER_DELAY_DECREMENT_PER_THOUSAND_VALUE = 20;
     private static final Color BACKGROUND_COLOR = Color.WHITE;
     private static final Color FOREGROUND_COLOR = Color.BLACK;
     private static final int POINTS_PER_BLOCK = 10;
     private static final int POINTS_PER_TOTAL_CLEAR = 500;
     private static final int POINTS_PER_CLEAR = 100;
 
     //Game and Graphics fields.
     private Tetra current;
     private Timer timer;
     private Block[][] grid;
     private boolean gameOver;
     private boolean gameInProgress;
     private boolean paused;
     private int score;
     private int lastThousandForScore;
     private HighScore highScoreTable;
     private HighScoreFrame highScoreFrame;
 
     /**
      * Constructs a new world and waits for user interaction.
      */
     public World() {
         gameInProgress = false;
         paused = false;
        highScoreTable = new HighScore();
         setUpGUI();
     }
 
     /**
      * Accessor for whether the game is in progress.
      * Note: the distinction between "not in progress",
      * and "paused" occurs if the game has not started yet.
      * @return true if in progress, false otherwise.
      */
     public boolean isGameInProgress() {
         return gameInProgress;
     }
 
     /**
      * Accessor for whether the game is paused.
      * @return true if paused, otherwise false
      */
     public boolean isPaused() {
         return paused;
     }
 
     /**
      * Starts a new game.
      */
     public void startGame() {
         gameInProgress = true;
         paused = false;
         TetrisFrame.gameButton.setText(PAUSE_TEXT);
         current = generateTetra();
 
         gameOver = false;
         score = 0;
         lastThousandForScore = 0;
         grid = new Block[GRID_WIDTH][GRID_HEIGHT];
         for (int x = 0; x < GRID_WIDTH; x++) {
             for (int y = 0; y < GRID_HEIGHT; y++) {
                 grid[x][y] = null;
             }
         }
         timer = new Timer(TIMER_INITIAL_DELAY,
                           new TimerActionListener());
         timer.start();
         paintComponent(this.getGraphics());
     }
 
     /**
      * Sets up graphics components.
      */
     private void setUpGUI() {
         this.setSize(PANEL_WIDTH,
                      PANEL_HEIGHT);
         this.setLayout(new BorderLayout());
         this.setPreferredSize(this.getSize());
         this.setFocusable(true);
         this.setBackground(BACKGROUND_COLOR);
         this.setDoubleBuffered(true);
         this.setVisible(true);
     }
 
     /**
      * Pauses the game on next paint
      */
     public void pause() {
         paused = true;
         TetrisFrame.gameButton.setText(CONTINUE_TEXT);
         timer.stop();
     }
 
     /**
      * Continues the game on next paint
      */
     public void unpause() {
         paused = false;
         TetrisFrame.gameButton.setText(PAUSE_TEXT);
         timer.start();
     }
 
     /**
      * Paints the score on the given Graphics
      * @param g Graphics the score will be painted on.
      */
     private void paintScore(Graphics g) {
         g.setFont(g.getFont().deriveFont(SCORE_TEXT_FONT_SIZE));
         g.setColor(Color.GREEN);
         g.drawString("" + score,
                      SCORE_TEXT_X_LOCATION,
                      SCORE_TEXT_Y_LOCATION);
         int thousands = score / 1000;
         if (thousands > lastThousandForScore) {
             lastThousandForScore = thousands;
             timer.setDelay(
                     timer.getDelay() - TIMER_DELAY_DECREMENT_PER_THOUSAND_VALUE);
         }
     }
 
     /**
      * Handles Timer events by passing them to the appropriate method.
      */
     public class TimerActionListener implements ActionListener {
 
         /**
          * Calls World.throwStepAction() when the Timer throws an ActionEvent
          * @param e the ActionEvent thrown by the Timer.
          */
         @Override
         public void actionPerformed(ActionEvent e) {
             throwStepAction();
         }
     }
 
     /**
      * Paints the entire panel.
      * @param g Graphics to be painted on.
      */
     @Override
     protected void paintComponent(Graphics g) {
         if (gameOver) {
             g.setFont(g.getFont().deriveFont((float) 80));
             g.setColor(FOREGROUND_COLOR);
             g.drawString(" GAME ",
                          DEFAULT_COMPONENT_SEPARATOR,
                          100);
             g.drawString(" OVER ",
                          DEFAULT_COMPONENT_SEPARATOR,
                          200);
             TetrisFrame.gameButton.setText(NEW_GAME_TEXT);
         }
         if (gameInProgress) {
             current.paint(g);
             paintEmpties(g);
             paintPile(g);
         }
     }
 
     /**
      * Paints spaces not occupied by Blocks in the pile
      * @param g Graphics to paint on.
      */
     private void paintEmpties(Graphics g) {
         g.setColor(BACKGROUND_COLOR);
         for (int x = 0; x < GRID_WIDTH; x++) {
             for (int y = 0; y < GRID_HEIGHT; y++) {
                 if (grid[x][y] == null) {
                     g.fillRect(x * BLOCK_SIZE,
                                y * BLOCK_SIZE,
                                BLOCK_SIZE,
                                BLOCK_SIZE);
                 }
             }
         }
     }
 
     /**
      * Paints spaces occupied by Blocks in the pile.
      * @param g Graphics to paint on.
      */
     private void paintPile(Graphics g) {
         for (int x = 0; x < GRID_WIDTH; x++) {
             for (int y = 0; y < GRID_HEIGHT; y++) {
                 if (grid[x][y] != null) {
                     grid[x][y].paint(g);
                 }
             }
         }
         paintScore(g);
     }
 
     /**
      * Paints spaces occupied by the Blocks of the provided Tetra the background
      * color, then paints spaces occupied by currently falling Tetra appropriately.
      * Intended to minimize flickering from excessive painting, so it should
      * be called if nothing changes except the position of the current tetra.
      * @param oldTetra Tetra containing Blocks who's spaces will be erased.
      */
     private void paintCurrent(Tetra oldTetra) {
         HashSet<Block> allBlocks = new HashSet<Block>();
         allBlocks.addAll(current.getTetra());
         allBlocks.addAll(oldTetra.getTetra());
         if (current.getTetra().size() == allBlocks.size()) {
             return;
         }
         for (Block b : allBlocks) {
             boolean occupied = setContains(current.getTetra(),
                                            b);
             if (occupied) {
                 b.paint(this.getGraphics());
             }
             else {
                 b.paintAsEmpty(this.getGraphics());
             }
         }
     }
 
     /**
      * Adds the current Tetra to the pile, then processes the pile.
      */
     private void addCurrentToPile() {
         for (Block b : current.getTetra()) {
             grid[b.getX()][b.getY()] = b;
             score += POINTS_PER_BLOCK;
         }
         dumpFullRows();
         checkGridEmptyForScore();
         paintComponent(this.getGraphics());
     }
 
     /**
      * Determines if there are no blocks in the grid for scoring purposes.
      */
     private void checkGridEmptyForScore() {
         boolean emptyGrid = true;
         for (int y = 0; y < GRID_HEIGHT; y++) {
             for (int x = 0; x < GRID_WIDTH; x++) {
                 if (grid[x][y] != null) {
                     emptyGrid = false;
                     break;
                 }
             }
             if (!emptyGrid) {
                 break;
             }
         }
         if (emptyGrid) {
             score += POINTS_PER_TOTAL_CLEAR;
         }
     }
 
     /**
      * Erases rows of the grid that are full and moves rows above down as needed.
      */
     private void dumpFullRows() {
         for (int y = 0; y < GRID_HEIGHT; y++) {
             boolean rowFull = true;
             for (int x = 0; x < GRID_WIDTH; x++) {
                 if (grid[x][y] == null) {
                     rowFull = false;
                     break;
                 }
             }
             if (rowFull) {
                 deleteRow(y);
                 score += POINTS_PER_CLEAR;
             }
         }
     }
 
     /**
      * Shifts all rows above the given row down one.
      * @param row int representing highest row that is not moved down.
      */
     private void shiftAllAboveDownOne(int row) {
         for (int y = row; y >= 0; y--) {
             for (int x = 0; x < GRID_WIDTH; x++) {
                 if (grid[x][y] != null) {
                     grid[x][y].moveDown();
                     grid[x][y + 1] = grid[x][y];
                     grid[x][y] = null;
                 }
             }
         }
     }
 
     /**
      * Deletes the given row and moves all rows above it down by one.
      * @param row int representing row to delete.
      */
     private void deleteRow(int row) {
         for (int x = 0; x < GRID_WIDTH; x++) {
             grid[x][row] = null;
         }
         shiftAllAboveDownOne(row);
     }
 
     /**
      * Determines if the blocks in motion have collided with
      * stationary blocks.
      * @return Whether or not a collision is taking place.
      */
     private boolean checkIfCurrentIntersectsPile() {
         boolean overlap = false;
         for (int x = 0; x < GRID_WIDTH; x++) {
             for (int y = 0; y < GRID_HEIGHT; y++) {
                 if (grid[x][y] != null) {
                     for (Block b : current.getTetra()) {
                         if (b.equals(grid[x][y])) {
                             overlap = true;
                             break;
                         }
                     }
                 }
                 if (overlap == true) {
                     break;
                 }
             }
             if (overlap == true) {
                 break;
             }
         }
         return overlap;
     }
 
     /**
      * Determines if blocks are present in the top most row of the game-play
      * grid, and if so responds appropriately.
      */
     private void checkGameOver() {
         for (int x = 0; x < GRID_WIDTH; x++) {
             if (grid[x][0] != null) {
                 gameOver = true;
                 checkHighScorer();
                 pause();
                 gameInProgress = false;
                 TetrisFrame.gameButton.setText(NEW_GAME_TEXT);
             }
         }
     }
 
     /**
      * Abstracts singular horizontal and vertical movements of the Tetra
      * currently in motion. Parameter indicates lateral offset of target
      * location. A movement is made if and only if all Blocks of the Tetra
      * currently in motion can complete the motion in the same direction.
      * @param direction int representing lateral offset of target location
      * (ie -1==left, 0==done, 1==right)
      * @return whether or not movement was successful
      */
     private boolean attemptToMoveCurrent(int direction) {
         boolean possible = true;
         boolean putInPile = false;
         /*
          * For each block in the tetra, the new x and y positions are
          * determined by separately.
          */
         for (Block b : current.getTetra()) {
             int currentX = b.getX();
             int currentY = b.getY();
             int targetX = currentX + direction;
             int targetY = -1;
             if (direction == 0) {
                 targetY = currentY + 1;
             }
             else {
                 targetY = currentY;
             }
             if (TetrisFrame.inbounds(targetX,
                                      targetY)) {
                 boolean filled = (grid[targetX][targetY] != null);
                 if (filled) {
                     possible = false;
                     if (direction == 0) {
                         putInPile = true;
                     }
                 }
 
             }
             else {
                 possible = false;
             }
             if (!((targetY >= 0) && (targetY < 20))) {
                 putInPile = true;
             }
         }
         if (possible) {
             if (direction == 0) {
                 current.moveDown();
             }
             if (direction == 1) {
                 current.moveRight();
             }
             if (direction == -1) {
                 current.moveLeft();
             }
             return true;
         }
         else if (putInPile) {
             addCurrentToPile();
             current = generateTetra();
             paintCurrent(current);
             return false;
         }
         else {
             return false;
         }
     }
 
     /**
      * Recursively moves the Tetra currently in motion down until it can
      * no longer.
      * @return always returns false.
      */
     private boolean attemptToMoveCurrentToMaximumDownwardPosition() {
         boolean keepGoing = true;
         while (keepGoing) {
             keepGoing = attemptToMoveCurrentDown();
         }
         return keepGoing;
     }
 
     /**
      * Rotates the tetra currently in motion if possible.
      * @param clockwise whether or not the rotation is clockwise
      * @return whether or not a rotation could be made.
      */
     private boolean attemptToRotateCurrent(boolean clockwise) {
         if (clockwise) {
             current.rotateClockwise();
             boolean overlap = checkIfCurrentIntersectsPile();
             if (overlap) {
                 current.rotateCounterClockwise();
             }
         }
         else {
             current.rotateCounterClockwise();
             boolean overlap = checkIfCurrentIntersectsPile();
             if (overlap) {
                 current.rotateClockwise();
             }
         }
         return true;
     }
 
     /**
      * Handles actions based on the provided identifier and paints
      * appropriately.
      * @param identifier String representing requested action.
      */
     private void throwAction(String identifier) {
         if (!paused) {
             boolean actionSuccess = false;
             Tetra oldTetra = current.cloneMethod();
             if (identifier.equals("step")) {
                 actionSuccess = attemptToMoveCurrentDown();
             }
             if (identifier.equals("left")) {
                 actionSuccess = attemptToMoveCurrentLeft();
             }
             if (identifier.equals("right")) {
                 actionSuccess = attemptToMoveCurrentRight();
             }
             if (identifier.equals("down")) {
                 actionSuccess = attemptToMoveCurrentToMaximumDownwardPosition();
             }
             if (identifier.equals("cwr")) {
                 actionSuccess = attemptToRotateCurrent(true);
             }
             if (identifier.equals("ccwr")) {
                 actionSuccess = attemptToRotateCurrent(false);
             }
             if (actionSuccess) {
                 paintCurrent(oldTetra);
             }
             checkGameOver();
             if (gameOver) {
                 paintComponent(this.getGraphics());
             }
         }
     }
 
     /**
      * Determines if given Set of Blocks contains a Block equivalent to the
      * given target Block.  Equivalency is not based on default hash
      * equivalency, but on the custom comparator defined in the Block class.
      * @param set Set to be searched
      * @param target Search element.
      * @return True if the Block was found, otherwise false
      */
     private static boolean setContains(Set<Block> set,
                                        Block target) {
         for (Block b : set) {
             if (b.equals(target)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Pseudorandomly generates a new Tetra from one of the predefined options.
      * @see java.util.Random
      * @return a Tetra
      */
     private Tetra generateTetra() {
         Random generator = new Random();
         int random = generator.nextInt(Tetra.NUMBER_OF_TETRAS);
         return new Tetra(Tetra.TETRA_IDENTIFIERS[random]);
     }
 
     /**
      * Convenience function that handles singular vertical movement of the
      * current Tetra.
      * @return whether or not the movement was successful.
      */
     private boolean attemptToMoveCurrentDown() {
         return attemptToMoveCurrent(0);
     }
 
     /**
      * Convenience function that handles singular leftward movement of the
      * current Tetra.
      * @return whether or not the movement was successful
      */
     private boolean attemptToMoveCurrentLeft() {
         return attemptToMoveCurrent(-1);
     }
 
     /**
      * Convenience function that handles singular rightward movement of the
      * current Tetra.
      * @return whether or not the movement was successful
      */
     private boolean attemptToMoveCurrentRight() {
         return attemptToMoveCurrent(1);
     }
 
     /**
      * Allows an external function to indicate to throwAction() that a
      * step action should occur without knowledge of the appropriate identifier.
      */
     public void throwStepAction() {
         throwAction("step");
     }
 
     /**
      * Allows an external function to indicate to throwAction() that a
      * leftward action should occur without knowledge of the appropriate
      * identifier.
      */
     public void throwLeftwardAction() {
         throwAction("left");
     }
 
     /**
      * Allows an external function to indicate to throwAction() that a
      * rightward action should occur without knowledge of the appropriate
      * identifier.
      */
     public void throwRightwardAction() {
         throwAction("right");
     }
 
     /**
      * Allows an external function to indicate to throwAction() that a
      * downward action should occur without knowledge of the appropriate
      * identifier.
      */
     public void throwDownwardAction() {
         throwAction("down");
     }
 
     /**
      * Allows an external function to indicate to throwAction() that a
      * clockwise rotation action should occur without knowledge of the
      * appropriate identifier.
      */
     public void throwClockwiseRotationAction() {
         throwAction("cwr");
     }
 
     /**
      * Allows an external function to indicate to throwAction() that a
      * counter clockwise rotation action should occur without knowledge of the
      * appropriate identifier.
      */
     public void throwCounterClockwiseRotationAction() {
         throwAction("ccwr");
     }
 
     /**
      * Dialog for new high scorer. Forces user to input name and passes
      * appropriate data to the HighScore.
      */
     private class HighScoreFrame extends JFrame {
 
         private JTextField nameField;
         private JButton okButton;
         private JLabel message;
 
         /**
          * Constructs a new dialog.
          */
         public HighScoreFrame() {
             gameOver = false;
             pause();
             this.setSize(300,
                          200);
             message = new JLabel("New High Score! Enter Your Name:");
             nameField = new JTextField();
             okButton = new JButton("OK");
             okButton.addActionListener(new OkButtonActionListener());
             this.setLayout(new GridLayout(3,
                                           1));
             this.add(message);
             this.add(nameField);
             this.add(okButton);
             this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
         }
 
         /**
          * Called when the done button is pressed. Determines if a valid
          * entry is present. If not, returns early. If entry is valid,
          * passes appropriate data to the HighScore.
          */
         private void done() {
             if (nameField.getText().equals("")) {
                 new JOptionPane("Enter a valid name.").createDialog(this,
                                                                     "Invalid Entry").setVisible(
                         true);
                 return;
             }
             String scoreString = score + "";
             int maxNameSize =
                 MAX_CHARS_ON_HIGH_SCORE_LINE - 2 - scoreString.length();
             if (nameField.getText().length() > maxNameSize) {
                 new JOptionPane("Enter a name less than " + maxNameSize
                                 + " characters.").createDialog(this,
                                                                "Invalid Entry").setVisible(
                         true);
                 return;
             }
             highScoreTable.addListingIfEligible(nameField.getText(),
                                                 score);
             highScoreTable.save();
             this.dispose();
             this.setVisible(false);
 
         }
 
         /**
          * Listens for ActionEvents created when the user presses ok, and
          * responds appropriately
          */
         private class OkButtonActionListener implements ActionListener {
 
             /**
              * Responds to ActionEvent. Will only be called when ok button is
              * pressed.
              * @param e ActionEvent created by button press
              */
             @Override
             public void actionPerformed(ActionEvent e) {
                 done();
             }
         }
     }
 
     /**
      * Determines if the current score qualifies as a high score. If so,
      * it opens a HighScoreFrame dialog.
      */
     private void checkHighScorer() {
         if (score < highScoreTable.getMinimumToQualify()) {
             return;
         }
         if (highScoreFrame == null || highScoreFrame.isVisible() == false) {
             highScoreFrame = new HighScoreFrame();
             highScoreFrame.setVisible(true);
         }
     }
 }
