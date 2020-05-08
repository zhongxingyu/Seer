 /*
  * Menu for the game, contains options to start new game, use custom levels, 
  * options etc...
  */
 package golf;
 
 import java.awt.GridLayout;
 import java.awt.Toolkit;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 
 /**
  * @author Paul
  */
 public class Menu extends JFrame
 {
     private final int WIDTH = 200;
     private final int HEIGHT = 300;
     
     /**
      * Constructor for the menu, initialises the whole game
      */
     public Menu()
     {
         //Call JFrame with the title
         super("Golf");
         //Set size and position in middle of screen
         setSize(WIDTH, HEIGHT);
         setLocation((int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth()/2 - WIDTH/2), (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight()/2 - HEIGHT/2));
         //Make the Menu exit on close
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         //Disable the maximise button
         setResizable(false);
         //Make the menu visible
         setVisible(true);
        //Set layout
         setLayout(new GridLayout(6, 0, 5, 5));
        //Add buttons
         add(new JButton("New Game"));
         add(new JButton("Load"));
         add(new JButton("Custom Levels"));
         add(new JButton("Options"));
         add(new JButton("Credits"));
         add(new JButton("Exit"));
         
     }
     
     /**
      * Main method, creates a new menu for the game
      */
     public static void main(String[] args)
     {
         new Menu();
     }
     
 }
