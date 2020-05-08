 /*
  * A re-implementation of the classic C=64 game 'Thrust'.
  *
  * @author "Joe Kiniry (kiniry@acm.org)"
  * @module "COMP 20050, COMP 30050"
  * @creation_date "March 2007"
  * @last_updated_date "April 2008"
  * @keywords "C=64", "Thrust", "game"
  */
 
 package thrust;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.io.File;
 
 import javax.swing.JFrame;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 
 import thrust.audio.SoundEffect;
 
 /**
  * Simulating all of the entities in the game to realize the game.
  * @author Joe Kiniry (kiniry@acm.org)
  * @version 23 April 2008
  */
 public final class Main {
   /**
    * This class cannot be constructed.
    */
   private Main() {
     assert false; //@ assert false;
   }
   
   /**
    * The main screen frame.
    */
   private static JFrame mainFrame;
   private static JTextArea console;
   private static JScrollPane scroll;
   
   /**
    * Run the game.
    * @param the_args The command-line arguments are ignored.
    */
   public static void main(final String[] the_args) {
     //assert false; //@ assert false;
     // display the title screen
     mainFrame = new JFrame("Thrust");
     mainFrame.setSize(500, 500);
     console = new JTextArea();
     scroll = new JScrollPane(console);
     mainFrame.add(scroll);
     console.setText("Welcome to Thrust");
     console.setFocusable(false);
     console.setEditable(false);
     mainFrame.setVisible(true);
     mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     // play title music
     SoundEffect music = new SoundEffect();
     music.make(new File("media/music.mp3"));
     music.start();
     // wait for keyboard input
    MainKeys input  = new MainKeys();
    input.keyPressed(key);
     // repeat the following until the player asks to quit
     //   show the high score display
     //   wait for input to start the game
     //   create game map and initialize location of all entities
     //   repeat the following until the player is out of lives or asks to quit:
     //      record the current time T
     //      perform a step in the simulation
     //      render all entities
     //      process the next keyboard input
     //      record the current time T'
     //      wait for (1/30th of a second - (T-T'))
     //   remove the game interface
     //   if the player has a new high score
     //     ask them to input their initials
     //     save the new high score
   }
  private class MainKeys implements KeyListener {
 
     private int my_key = 0;
     public void keyPressed(KeyEvent key) {
         my_key = key.getKeyCode();
     }
 
     public void keyReleased(KeyEvent arg0) {
       // TODO Auto-generated method stub
       
     }
 
     public void keyTyped(KeyEvent arg0) {
       // TODO Auto-generated method stub
       
     }
     public int lastKeyPressed(){
       int temp = my_key;
       my_key = -1;
       return temp;
     }
     public MainKeys() {
       
     }
   }
 }
