 package gui;
 
 import brawllogic.BasePosition;
 import brawllogic.BrawlGame;
 import brawllogic.IllegalMoveException;
 import brawllogic.Player;
import deepdish.Animator;
import deepdish.GameObject;
import deepdish.Painter;
import deepdish.Scene;
import deepdish.Viewport;
 import java.awt.Image;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 
 /**
  * @author Patrick Herrmann
  */
 public class GameFrame extends JFrame {
     
     private Animator animator;
     private Painter painter;
     private Scene scene;
     private BrawlGame brawlGame;
     private GameView gameView;
     private JLabel canvas;
     
     public GameFrame(BrawlGame brawlGame, int width, int height) {
         super("Brawl");
         
         this.brawlGame = brawlGame;
         
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         initializeAnimation(width, height);
     }
     
     private void initializeAnimation(int width, int height) {
         
         gameView = new GameView(brawlGame.getState());
         scene = new Scene();
         
         for (GameObject obj : gameView.getCardViews())
             scene.add(obj);
         
         Viewport viewport = new Viewport(width, height);
         animator = new Animator(scene, 80);
         painter = new GamePainter(scene, viewport, 30);
         canvas = new JLabel(new ImageIcon(viewport.getImage()));
         add(canvas);
         addKeyListener(new KeyHandler());
         pack();
         
         animator.start();
         painter.start();
     }
     
     private void handleKeyType(char key) throws IllegalMoveException {
         BasePosition bp;
             Player s;
             Player p;
 
             switch (key) {
                 // LEFT PLAYER CONTROLS
                 case 'w':
                     bp = BasePosition.HIGH;
                     s = Player.LEFT;
                     p = Player.LEFT;
                     break;
                 case 'e':
                     bp = BasePosition.HIGH;
                     s = Player.RIGHT;
                     p = Player.LEFT;
                     break;
                 case 's':
                     bp = BasePosition.MID;
                     s = Player.LEFT;
                     p = Player.LEFT;
                     break;
                 case 'd':
                     bp = BasePosition.MID;
                     s = Player.RIGHT;
                     p = Player.LEFT;
                     break;
                 case 'x':
                     bp = BasePosition.LOW;
                     s = Player.LEFT;
                     p = Player.LEFT;
                     break;
                 case 'c':
                     bp = BasePosition.LOW;
                     s = Player.RIGHT;
                     p = Player.LEFT;
                     break;
                 case 'z':
                     brawlGame.draw(Player.LEFT);
                     return;
                 // RIGHT PLAYER CONTROLS
                 case 'o':
                     bp = BasePosition.HIGH;
                     s = Player.LEFT;
                     p = Player.RIGHT;
                     break;
                 case 'p':
                     bp = BasePosition.HIGH;
                     s = Player.RIGHT;
                     p = Player.RIGHT;
                     break;
                 case 'l':
                     bp = BasePosition.MID;
                     s = Player.LEFT;
                     p = Player.RIGHT;
                     break;
                 case ';':
                     bp = BasePosition.MID;
                     s = Player.RIGHT;
                     p = Player.RIGHT;
                     break;
                 case '.':
                     bp = BasePosition.LOW;
                     s = Player.LEFT;
                     p = Player.RIGHT;
                     break;
                 case '/':
                     bp = BasePosition.LOW;
                     s = Player.RIGHT;
                     p = Player.RIGHT;
                     break;
                 case ',':
                     brawlGame.draw(Player.RIGHT);
                     return;
                 default:
                     System.out.println("Unrecognized command.");
                     return;
             }
 
             brawlGame.tryMove(p, bp, s);
     }
     
     private class KeyHandler implements KeyListener {
 
         @Override
         public void keyTyped(KeyEvent ke) {
             
             try {
                 handleKeyType(ke.getKeyChar());
                 
                 if (brawlGame.getState().isGameOver()) {
                     Player winner = brawlGame.getState().getWinner();
                     
                     if (winner == null) {
                         System.out.println("The game ends in a tie.");
                     } else {
                         System.out.println(winner + " player wins!");
                     }
                     System.exit(0);
                 }
                 
             } catch (IllegalMoveException e) {
                 System.out.print(brawlGame.getState().getFighter(e.getPlayer()).getName() + " (" + e.getPlayer() + "): ");
                 System.out.println(e.getMessage());
             }
         }
 
         @Override
         public void keyPressed(KeyEvent ke) {
             
         }
 
         @Override
         public void keyReleased(KeyEvent ke) {
             
         }
         
     }
     
     private class GamePainter extends Painter {
         
         public GamePainter(Scene scene, Viewport viewport, int fps) {
             super(scene, viewport, fps);
         }
         
         @Override
         public void sendToScreen(Image buffer) {
             repaint();
         }
     }
 }
