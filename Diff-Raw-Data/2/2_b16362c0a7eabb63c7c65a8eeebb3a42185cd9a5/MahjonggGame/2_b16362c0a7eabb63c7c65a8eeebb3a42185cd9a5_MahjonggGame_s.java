 package mahjongg;
 
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
 
 public class MahjonggGame extends GridGame
 {
     protected MahjonggBoard gridBoard;
     protected GridStatus gridStatus;
     private int secondsElapsed = 0;
     
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
         this.setGame(1);
         this.gridBoard = new MahjonggBoard();
         this.gridBoard.setParent(this);
         this.gridBoard.resetBoard();
 
         this.gridStatus = new MahjonggStatus();
         this.startTimer();
     }
     
     public void makeMove(int row, int col)
     {
         this.gridBoard.clickTile(row, col);
         this.updateStatusBar();
         setChanged();
         notifyObservers();
     }
     
     public void restart()
     {
         this.gridBoard.resetBoard();
         this.secondsElapsed = 0;
         this.updateStatusBar();
         setChanged();
         notifyObservers(this.getGame());
     }
     
     protected void startTimer()
     {
         // Every 1 second.
         Timer timer = new Timer(1000, new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 MahjonggGame.this.secondsElapsed++;
                 MahjonggGame.this.updateStatusBar();
             }
         });
         
         this.updateStatusBar();
         timer.start();
     }
     
     protected void updateStatusBar()
     {
        this.gridStatus.setLabelText("Tiles Left: " + this.gridBoard.tileCount + "  "
                                   + "Time: " + this.secondsElapsed / 60 + ":" + String.format("%02d", this.secondsElapsed % 60));
     }
     
     public List<Action> getMenuActions()
     {
         return Arrays.asList(new Action[]{
             new RestartAction("Restart", KeyStroke.getKeyStroke('R', InputEvent.ALT_MASK)),
             new NewGameAction("New Game", KeyStroke.getKeyStroke('N', InputEvent.ALT_MASK)),
             new HintAction("Hint", KeyStroke.getKeyStroke('H', InputEvent.ALT_MASK)),
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
             MahjonggGame.this.restart();
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
             MahjonggGame.this.incrementGame();
             MahjonggGame.this.restart();
         }
     }
 
     class HintAction extends AbstractAction
     {
         public HintAction(String label, KeyStroke accelKey)
         {
             super(label);
             putValue(ACCELERATOR_KEY, accelKey);
         }
 
         public void actionPerformed(ActionEvent e)
         {
             MahjonggTile hintTile = MahjonggGame.this.gridBoard.findOpenPair();
            if (hintTile == null)
             {
                 JOptionPane.showMessageDialog(null, "No moves available.");
             }
             else
             {
                 JOptionPane.showMessageDialog(null, "Hint: " + hintTile.toString());
             }
             
             MahjonggGame.this.setChanged();
             MahjonggGame.this.notifyObservers();
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
             MahjonggGame.this.gridBoard.cheat();
             MahjonggGame.this.updateStatusBar();
             MahjonggGame.this.setChanged();
             MahjonggGame.this.notifyObservers();
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
 
