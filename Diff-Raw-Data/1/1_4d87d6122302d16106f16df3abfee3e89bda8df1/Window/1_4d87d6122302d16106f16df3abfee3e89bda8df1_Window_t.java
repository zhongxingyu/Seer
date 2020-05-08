 package bomberman.client.gui;
 
 import bomberman.client.controller.Game;
 import java.awt.BorderLayout;
 import javax.swing.JFrame;
 
 public class Window extends JFrame {
 
     private static Window instance;
     private Settings settings;
     private Board board;
 
     private Window() {
         super();
 
         this.setTitle("Bomberman");
         this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         this.setLocationRelativeTo(null);
         //this.setSize(400, 200);
         this.setResizable(false);
         this.setLayout(new BorderLayout());
 
         this.showSettings();
 
         this.setVisible(true);
     }
 
     /**
      * Creates a unique instance of Window (Singleton)
      *
      * @return Instance of Window
      */
     public static synchronized Window getInstance() {
         if (instance == null) {
             instance = new Window();
         }
         return instance;
     }
 
     
     /**
      * Affiche le formulaire de config
      */
     public void showSettings() {
         if(this.board != null)
             this.remove(this.board);
         if(this.settings == null)
             this.settings = new Settings();
         this.add(this.settings, BorderLayout.CENTER);
         this.setVisible(true);
         this.repaint();
         this.pack();
     }
 
     /**
      * Affiche le plateau de jeu
      */
     public void showBoard() {
         if(this.settings != null)
             this.remove(this.settings);
         this.board = new Board();
         this.addKeyListener(Game.getInstance());
         Game.getInstance().setBoard(this.board);
         this.add(this.board, BorderLayout.CENTER);
         this.setVisible(true);
         this.repaint();
         this.pack();
        this.requestFocus();
     }
 
 }
