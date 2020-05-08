 package gui;
 
 import java.awt.CardLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import game.GameState;
 import java.awt.Dimension;
 
 /**
  *
  * @author Haisin Yip
  */
 
 public class MenuGUI implements Runnable
 {
     private static final int DEFAULT_WIDTH = 800;
     private static final int DEFAULT_HEIGHT = 600;
     
     public static int WIDTH = DEFAULT_WIDTH;
     public static int HEIGHT = DEFAULT_HEIGHT;
     
    
     //main window, associated elements
     private JFrame frame;
     private CardLayout cardLayout;
     private JPanel card = new JPanel();
     
     // menu buttons
     public JButton singlePbutton = new JButton("SINGLE-PLAYER MODE");
     public JButton twoPbutton = new JButton("TWO-PLAYER MODE");
     public JButton leaderBoardButton = new JButton("LEADERBOARD");
     public JButton tutorialButton = new JButton("TUTORIAL");
     public JButton quitButton = new JButton("QUIT");
     
     //back button for both leaderboard and tutorial
     public JButton backButton = new JButton("BACK");
     
     private ActionListener buttonClick;
     private KeyListener keyboard;
     
     //whether or not we are in single player mode
     private boolean singleP = false;
     
     //Panels for single and 2 player modes
     private SinglePgamePanel onePPanel;
     private TwoPgamePanel twoPPanel;
     /**
      * Starts the GUI Menu.
      * @param AL
      * @param keyb
      */
     public MenuGUI(ActionListener AL, KeyListener keyb)
     {
         // create and initialize frame
         frame = new JFrame("AMBroSIA");
         cardLayout = new CardLayout();
         card.setLayout(cardLayout);
         frame.getContentPane().add(card);
         
         //allow global access to actionlistener,keyboard listener
         buttonClick = AL;
         keyboard = keyb;
         
         //set important parameters
         frame.setVisible(true);
         frame.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
         frame.setLocation(100, 100);
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(800,600));
         
         //set up (but do not yet allow) keyboard input
         frame.addKeyListener(keyboard);
         frame.setFocusable(false);
     }
  
     public void showMenu() {
         //disable keyboard input
         frame.setFocusable(false);
         // create menu page panel, set it up, show it
         JPanel cardMenu = new JPanel(new GridLayout(2, 1));
         cardMenu.add(new MenuPanel());
         // initialize the 5 buttons that shall be in the menu page
         JPanel buttonPanelMenu = new JPanel(new GridLayout(5, 1));
         buttonPanelMenu.add(singlePbutton);
         buttonPanelMenu.add(twoPbutton);
         buttonPanelMenu.add(leaderBoardButton);
         buttonPanelMenu.add(tutorialButton);
         buttonPanelMenu.add(quitButton);
         
         singlePbutton.addActionListener(buttonClick);
         twoPbutton.addActionListener(buttonClick);
         leaderBoardButton.addActionListener(buttonClick);
         tutorialButton.addActionListener(buttonClick);
         quitButton.addActionListener(buttonClick);
         
         cardMenu.add(buttonPanelMenu);
         card.add("Menu", cardMenu);
         cardLayout.show(card, "Menu");
         
         frame.setResizable(true);
     }
     
     public void displaySingleP(GameState gs) {
         //allow keyboard input
         frame.setFocusable(true);
        //create panel, show it
         onePPanel = new SinglePgamePanel(gs);
         JPanel cardGame1P = new JPanel();
         cardGame1P.add(onePPanel);
         card.add("Single-Player Mode", cardGame1P);
         cardLayout.show(card, "Single-Player Mode");
         //remove menu component for efficiency
         card.remove(card.getComponent(0));
         //let other methods know we are in single P mode
         singleP = true;
         
         //do not allow resizing at this point, as it can disrupt gameplay
         frame.setResizable(false);
     }
 
     public void displayTwoP(GameState gs) {
         frame.setFocusable(true);
         // create single player mode game page (two player)
         twoPPanel = new TwoPgamePanel();
         JPanel cardGame2P = new JPanel();
         //cardGame.setLayout(new GridLayout(2,1)); not sure how to set layout for actual gameplay
         cardGame2P.add(twoPPanel);
         card.add("Two-Player Mode", cardGame2P);
     }
        
     public void displayLeaderBoard() {
         // create leaderboard page
         JPanel cardLeaderBoard = new JPanel();
         cardLeaderBoard.setLayout(new GridLayout(5, 1));
         JPanel leaderBoardPanel = new LeaderBoardPanel();
         cardLeaderBoard.add(leaderBoardPanel);
         //initialize a back button
         JPanel buttonPanelLeaderBoard = new JPanel();
         buttonPanelLeaderBoard.add(backButton);
         backButton.addActionListener(buttonClick);
         cardLeaderBoard.add(buttonPanelLeaderBoard);
         card.add("LeaderBoard", cardLeaderBoard);
         cardLayout.show(card, "LeaderBoard");
     }
 
     public void displayTutorial() {
         JPanel cardTutorial = new JPanel();
         cardTutorial.setLayout(new GridLayout(5, 1));
         JPanel tutorialPanel = new TutorialPanel();
         cardTutorial.add(tutorialPanel);
         //initialize a back button
         JPanel buttonPanelTutorial = new JPanel();
         buttonPanelTutorial.add(backButton);
         backButton.addActionListener(buttonClick);
         cardTutorial.add(buttonPanelTutorial);
         card.add("Tutorial", cardTutorial);
         cardLayout.show(card, "Tutorial");
     }
         
     public void goBack() {
         cardLayout.show(card, "Menu");
     }
 
     public void updateDraw()
     {
 //     PLACEHOLDER IF STATEMENT
 //        if (singleP)
 //        {
             onePPanel.updatePanel();
 //        }
 //        else
 //        {
 //            twoPPanel.updatePanel();
 //        }
     }
 
     @Override
     public void run() {
         updateSize();
         updateDraw();
     }
     
     private void updateSize() {
         MenuGUI.WIDTH = frame.getWidth();
         MenuGUI.HEIGHT = frame.getHeight();
     }
 }
