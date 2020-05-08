 package mines;
 
 import gridgame.GridBoard;
 import gridgame.GridGame;
 import gridgame.GridStatus;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.InputEvent;
 import java.util.List;
 import java.util.Arrays;
 import javax.swing.Action;
 import javax.swing.AbstractAction;
 import javax.swing.ImageIcon;
 import javax.swing.JOptionPane;
 import javax.swing.KeyStroke;
 import javax.swing.Timer;
 
 public class MinesGame extends GridGame
 {
     protected MinesBoard gridBoard;
     protected GridStatus gridStatus;
 
     private static final int kMaxGameNumber = 5000;
 
     protected int moves = 0;
     protected int flagsPlaced = 0;
     protected int secondsElapsed = 0;
     
     public GridBoard getBoardToView()
     {
         // TODO: Probably supposed to be a copy.
         return this.gridBoard;
     }
     
     public GridStatus getStatusToView()
     {
         // TODO: Probably supposed to be a copy.
         return this.gridStatus;
     }
     
     public void init()
     {
         this.setGame((new java.util.Random()).nextInt(this.kMaxGameNumber));
         this.gridBoard = new MinesBoard();
         this.gridBoard.setParent(this);
         this.gridBoard.resetBoard();
 
         this.gridStatus = new MinesStatus();
         this.startTimer();
     }
     
     public void makeMove(int row, int col)
     {
         this.gridBoard.clickTile(row, col);
         this.updateStatusBar();
         setChanged();
         notifyObservers();
     }
 
     public void handleRightClick(int row, int col)
     {
         this.gridBoard.handleRightClick(row, col);
         this.updateStatusBar();
         setChanged();
         notifyObservers();
     }
     
     public void restart()
     {
         int size = Integer.parseInt(this.myPrefs.get("Board Size"));
         int difficulty = Integer.parseInt(this.myPrefs.get("Difficulty"));
         this.gridBoard.setSize(size);
         this.gridBoard.setDifficulty(difficulty);
 
         this.gridBoard.resetBoard();
         this.secondsElapsed = 0;
         this.updateStatusBar();
         setChanged();
         notifyObservers(this.getGame());
     }
 
     private void selectGame()
     {
         String gameNumber = (String)JOptionPane.showInputDialog(null, "Enter desired game number (1 - 5000):", "Select Game", JOptionPane.QUESTION_MESSAGE, null, null, "");
        if (!gameNumber.equals(""))
         {
             int parsedGameNumber = Integer.parseInt(gameNumber);
             if (parsedGameNumber > 0 && parsedGameNumber <= this.kMaxGameNumber)
             {
                 this.setGame(parsedGameNumber);
                 this.restart();
             }
         }
     }
     
     protected void startTimer()
     {
         // Every 1 second.
         Timer timer = new Timer(1000, new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 MinesGame.this.secondsElapsed++;
                 MinesGame.this.updateStatusBar();
             }
         });
         
         this.updateStatusBar();
         timer.start();
     }
     
     protected void updateStatusBar()
     {
        // I don't really want to do printf in Java.
        String optionalSpace = " ";
        if (this.flagsPlaced > 9)
        {
           optionalSpace = "";
        }
        
        this.gridStatus.setLabelText("Moves: " + this.moves + "   "
                                   + "Flags:  " + optionalSpace + this.flagsPlaced + "/" + this.gridBoard.numBombs + " "
                                   + this.secondsElapsed / 60 + ":" + String.format("%02d", this.secondsElapsed % 60));
     }
     
     public List<Action> getMenuActions()
     {
         return Arrays.asList(new Action[]{
             new RestartAction("Restart", KeyStroke.getKeyStroke('R', InputEvent.ALT_MASK)),
             new NewGameAction("New Game", KeyStroke.getKeyStroke('N', InputEvent.ALT_MASK)),
             new SelectGameAction("Select Game", KeyStroke.getKeyStroke('G', InputEvent.ALT_MASK)),
             new ScoresAction("Scores", KeyStroke.getKeyStroke('S', InputEvent.ALT_MASK)),
             new CheatAction("Cheat", KeyStroke.getKeyStroke('C', InputEvent.ALT_MASK)),
             new QuitAction("Quit", KeyStroke.getKeyStroke('Q', InputEvent.ALT_MASK)),
         });
     }
 
     class RestartAction extends AbstractAction
     {
         public RestartAction(String label, KeyStroke accelKey)
         {
             super(label);
             putValue(ACCELERATOR_KEY, accelKey);
         }
 
         public void actionPerformed(ActionEvent e)
         {
             MinesGame.this.restart();
         }
     }
 
     class NewGameAction extends AbstractAction
     {
         public NewGameAction(String label, KeyStroke accelKey)
         {
             super(label);
             putValue(ACCELERATOR_KEY, accelKey);
         }
 
         public void actionPerformed(ActionEvent e)
         {
             MinesGame.this.incrementGame();
             MinesGame.this.restart();
         }
     }
 
     class SelectGameAction extends AbstractAction
     {
         public SelectGameAction(String label, KeyStroke accelKey)
         {
             super(label);
             putValue(ACCELERATOR_KEY, accelKey);
         }
 
         public void actionPerformed(ActionEvent e)
         {
             MinesGame.this.selectGame();
             MinesGame.this.setChanged();
             MinesGame.this.notifyObservers();
         }
     }
 
     class ScoresAction extends AbstractAction
     {
         public ScoresAction(String label, KeyStroke accelKey)
         {
             super(label);
             putValue(ACCELERATOR_KEY, accelKey);
         }
 
         public void actionPerformed(ActionEvent e)
         {
             MinesGame.this.showHighScores();
             MinesGame.this.setChanged();
             MinesGame.this.notifyObservers();
         }
     }
 
     class CheatAction extends AbstractAction
     {
         public CheatAction(String label, KeyStroke accelKey)
         {
             super(label);
             putValue(ACCELERATOR_KEY, accelKey);
         }
 
         public void actionPerformed(ActionEvent e)
         {
             MinesGame.this.gridBoard.cheat();
             MinesGame.this.setChanged();
             MinesGame.this.notifyObservers();
         }
     }
 
     class QuitAction extends AbstractAction
     {
         public QuitAction(String label, KeyStroke accelKey)
         {
             super(label);
             putValue(ACCELERATOR_KEY, accelKey);
         }
 
         public void actionPerformed(ActionEvent e)
         {
             System.exit(0);
         }
     }
 }
 
