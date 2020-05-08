 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.*;
 import java.awt.font.*;
 import java.awt.image.BufferedImage;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 import javax.swing.*;
 import javax.swing.JTextField;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.text.DefaultCaret;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.UIManager;
 import javax.swing.BorderFactory;
 
 import java.util.concurrent.locks.*;
 import java.util.ArrayList;
 import java.util.Date;
 
 /**
  * A class that handles all IO with the user
  *
  * @author Alex Stelea
  * @author Geoving Gerard II
  * @author Kennon Bittick
  * @author Tyler MacGrogan
  * @version 11 | 21 | 2013
  */
 public class Renderer {
 
     private JFrame frame;
     private boolean waiting;
     private String[] states; // keeps track of the state of the GUI
     private ReentrantLock lock;
     protected static final int WIDTH = 9;
     protected static final int HEIGHT = 5;
     protected static final int TILE_SIZE = 100;
     private JButton[] buttons = new JButton[WIDTH + HEIGHT];
     private Timer timer;
     private boolean isSelectedButtonCreated = false;
     private long timeWhenTimerSet;
     private long pauseTime;
     private int num;
     private boolean paused;
 
     /**
      * Renderer handles all graphics related actions for game.
      */
     public Renderer() {
         frame = new JFrame("M.U.L.E.");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setResizable(false);
         frame.setMinimumSize(new Dimension(950, 770));
         frame.setPreferredSize(new Dimension(950, 770));
         frame.setVisible(true);
         waiting = true;
         lock = new ReentrantLock();
         frame.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
         timer = createTimer(50000);
         paused = false;
     }
 
     /**
      * Draw the introduction screen
      *
      * @return an array of the states selected by the user
      **/
     public String[] drawIntroScreen() {
         
         // declare initial variables
         String action = "";
         states = new String[1];
         states[0] = "new";
 
         ImagePanel panel = new ImagePanel("/media/startscreen.png");
         panel.setPreferredSize(new Dimension(950, 700));
         panel.setLayout(null);
 
         ImagePanel menuPanel = new ImagePanel("/media/bss.png");
         menuPanel.setPreferredSize(new Dimension(950, 50));
         menuPanel.setLayout(null);
 
         ArrayList<JPanel> panels = new ArrayList<JPanel>();
         panels.add(panel);
         panels.add(menuPanel);
 
         changePanel(frame, panels);
 
         // add buttons
         final JButton quitButton = addButtonToPanel(menuPanel, 11, 5, 171, 40, 0, "quit");
 		addHoverIcon(quitButton, "/media/hoverbuttons/quithover.png");
         final JButton loadButton = addButtonToPanel(menuPanel, 590, 5, 171, 40, 0, "load");
 		addHoverIcon(loadButton, "/media/hoverbuttons/loadhover.png");
         JButton newButton = addButtonToPanel(menuPanel, 771, 5, 171, 40, 0, "new");
 		addHoverIcon(newButton, "/media/hoverbuttons/newhover.png");
 
         blockForInput();
         exitSafely();
         return states;
     }
 
     // States[0] - Action to perform: {"back", "okay"}
     // States[1] - Difficulty: {"1", "2", "3"}
     // States[2] - Number of Players: {"1", "2", "3", "4", "5"}
     /**
      * Draw the setup screen
      *
      * @param numPlayers how many players to draw
      * @param difficulty the current difficulty to draw
      *
      * @return an arary of the states selected by the player
      **/
     public String[] drawSetupScreen(int numPlayers, int difficulty) {
 
         // declare initial variables
         String action = "";
         states = new String[3];
         states[0] = "okay";
         states[1] = "1";
         states[2] = "1";
 
         String difficultyValue = getDifficultyValueString(difficulty);
 
         ImagePanel panel = new ImagePanel("/media/gamesetup.png");
         panel.setPreferredSize(new Dimension(950, 525));
         panel.setLayout(null);
 
         JPanel playerPanel = new JPanel();
         ImagePanel playerBox1 = new ImagePanel("/media/p00.png");
         playerBox1.setPreferredSize(new Dimension(160, 175));
         playerPanel.setPreferredSize(new Dimension(950, 175));
         playerPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
         playerPanel.add(playerBox1);
         
         for (int i = 1; i < numPlayers+1; i++) {
             ImagePanel playerBox = new ImagePanel("/media/p" +i+"0.png");
             playerBox.setPreferredSize(new Dimension(158, 175));
             playerPanel.add(playerBox);
         }
         for (int i = numPlayers+1; i < 6; i++) {
             ImagePanel playerBox = new ImagePanel("/media/p" +i+".png");
             playerBox.setPreferredSize(new Dimension(158, 175));
             playerPanel.add(playerBox);
         }
 
         ImagePanel menuPanel = new ImagePanel("/media/bp0.png");
         menuPanel.setPreferredSize(new Dimension(950, 50));
         menuPanel.setLayout(null);
 
         ArrayList<JPanel> panels = new ArrayList<JPanel>();
         panels.add(panel);
         panels.add(playerPanel);
         panels.add(menuPanel);
         changePanel(frame, panels);
 
         // add buttons
         JButton backButton = addButtonToPanel(menuPanel, 11, 5, 171, 40, 0, "back");
 		//addHoverIcon(backButton, "/media/hoverbuttons/backhover.png");
         JButton okayButton = addButtonToPanel(menuPanel, 771, 5, 171, 40, 0, "okay");
 		//addHoverIcon(okayButton, "/media/hoverbuttons/okayhover.png");
         JButton easyButton = addButtonToPanel(panel, 160, 164, 77, 40, 1, "1");
         JButton mediumButton = addButtonToPanel(panel, 407, 164, 137, 38, 1, "2");
         JButton hardButton = addButtonToPanel(panel, 715, 164, 78, 38, 1, "3");
         JButton onePlayer = addButtonToPanel(panel, 185, 404, 24, 40, 2, "1");
 		//addHoverIcon(onePlayer, "/media/tileselect.png");
         JButton twoPlayer = addButtonToPanel(panel, 325, 404, 24, 40, 2, "2");
 		//addHoverIcon(twoPlayer, "/media/tileselect.png");
         JButton threePlayer = addButtonToPanel(panel, 465, 404, 24, 40, 2, "3");
 		//addHoverIcon(threePlayer, "/media/tileselect.png");
         JButton fourPlayer = addButtonToPanel(panel, 605, 404, 24, 40, 2, "4");
 		//addHoverIcon(fourPlayer, "/media/tileselect.png");
         JButton fivePlayer = addButtonToPanel(panel, 745, 404, 24, 40, 2, "5");
 		//addHoverIcon(fivePlayer, "/media/tileselect.png");
 
         blockForSetupScreen(panel, playerBox1, 170, 150, 280, numPlayers, difficultyValue, playerPanel);
         exitSafely();
         return states;
     }
 
     // States[0] - Action to perform: {"new", "load", "quit"}
     // States[1] - Map Number: {"1", "2", "3", "4", "5"}
     /**
      * Draw the amp selection screen to the player
      *
      * @param numPlayers the number of players to draw
      * @param difficulty the difficulty to draw
      *
      * @return an array of the states selected by the player
      **/
     public String[] drawMapScreen(int numPlayers, int difficulty) {
 
         // declare initial variables
         String action = "";
         states = new String[2];
         states[0] = "okay";
         states[1] = "1";
 
         this.num = numPlayers;
         String difficultyValue = getDifficultyValueString(difficulty);
 
         ImagePanel panel = new ImagePanel("/media/mapselection.png");
         panel.setPreferredSize(new Dimension(950, 525));
         panel.setLayout(null); 
 
         JPanel playerPanel = new JPanel();
         ImagePanel playerBox1 = new ImagePanel("/media/p00.png");
         playerBox1.setPreferredSize(new Dimension(160, 175));
         playerPanel.setPreferredSize(new Dimension(950, 175));
         playerPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
         playerPanel.add(playerBox1);
         for (int i = 1; i < numPlayers+1; i++) {
             ImagePanel playerBox = new ImagePanel("/media/p" +i+"0.png");
             playerBox.setPreferredSize(new Dimension(158, 175));
             playerPanel.add(playerBox);
         }
         for (int i = numPlayers+1; i < 6; i++) {
             ImagePanel playerBox = new ImagePanel("/media/p" +i+".png");
             playerBox.setPreferredSize(new Dimension(158, 175));
             playerPanel.add(playerBox);
         }
 
         ImagePanel menuPanel = new ImagePanel("/media/bp0.png");
         menuPanel.setPreferredSize(new Dimension(950, 50));
         menuPanel.setLayout(null);
 
         ArrayList<JPanel> panels = new ArrayList<JPanel>();
         panels.add(panel);
         panels.add(playerPanel);
         panels.add(menuPanel);
         changePanel(frame, panels);
 
         // add buttons
         JButton backButton = addButtonToPanel(menuPanel, 11, 5, 170, 40, 0, "back");
 		//addHoverIcon(backButton, "/media/hoverbuttons/backhover.png");
         JButton okayButton = addButtonToPanel(menuPanel, 771, 5, 170, 40, 0, "okay");
 		//addHoverIcon(okayButton, "/media/hoverbuttons/okayhover.png");
         JButton map1Button = addButtonToPanel(panel, 110, 162, 224, 126, 1, "1");
         JButton map2Button = addButtonToPanel(panel, 365, 162, 224, 126, 1, "2");
         JButton map3Button = addButtonToPanel(panel, 617, 162, 224, 126, 1, "3");
         JButton map4Button = addButtonToPanel(panel, 235, 317, 224, 126, 1, "4");
         JButton map5Button = addButtonToPanel(panel, 490, 317, 224, 126, 1, "5");
  
         blockForMapScreen(panel, playerBox1, difficultyValue);
         exitSafely();
         return states;
     }
 
     // States[0] - Action to perform: {"new", "load", "quit"}
     // States[1] - Race: {"human", "elephant", "squirrel", "frog", "cat"}
     // States[2] - Player Name
     // States[3] - Color: {"red", "blue", "pink", "green", "orange"}
     /**
      * Draw the character selection screen 
      *
      * @param players the current players that have been created
      * @param difficulty the current difficulty to draw
      * @param map the map that was selected
      * @param size the number of players we have
      *
      * @return an array of the states selected by the player
      **/
     public String[] drawCharacterScreen(ArrayList<Player> players, int difficulty, Map map, int size) {
 
         // declare initial variables
         String action = "";
         states = new String[4];
         states[0] = "okay";
         states[1] = "human";
         states[2] = "default";
         states[3] = "red";
 
         String difficultyValue = getDifficultyValueString(difficulty);
 
         ImagePanel panel = new ImagePanel("/media/playerselection.png");
         panel.setPreferredSize(new Dimension(950, 525));
         panel.setLayout(null);
 
         JPanel playerPanel = new JPanel();
         ImagePanel playerBox1 = new ImagePanel("/media/p00.png");
         playerBox1.setPreferredSize(new Dimension(160, 175));
         playerPanel.setPreferredSize(new Dimension(950, 175));
         playerPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
         playerPanel.add(playerBox1);
         for (int i = 1; i < num+1; i++) {
             ImagePanel playerBox = new ImagePanel("/media/p" +i+"0.png");
             playerBox.setPreferredSize(new Dimension(158, 175));
             playerPanel.add(playerBox);
         }
         for (int i = num+1; i < 6; i++) {
             ImagePanel playerBox = new ImagePanel("/media/p" +i+".png");
             playerBox.setPreferredSize(new Dimension(158, 175));
             playerPanel.add(playerBox);
         }
         for (int i = 0; i < players.size(); i++) {
             drawPlayerCharacter(players.get(i), i, playerPanel);
         }
 
         ImagePanel menuPanel = new ImagePanel("/media/bp0.png");
         menuPanel.setPreferredSize(new Dimension(950, 50));
         menuPanel.setLayout(null);
 
 
         ArrayList<JPanel> panels = new ArrayList<JPanel>();
         panels.add(panel);
         panels.add(playerPanel);
         panels.add(menuPanel);
         changePanel(frame, panels);
 
         // add buttons
         JButton backButton = addButtonToPanel(menuPanel, 11, 5, 170, 40, 0, "back");
 		//addHoverIcon(backButton, "/media/hoverbuttons/backhover.png");
         JButton okayButton = addButtonToPanel(menuPanel, 771, 5, 170, 40, 0, "okay");
 		//addHoverIcon(okayButton, "/media/hoverbuttons/okayhover.png");
         JButton humanButton = addButtonToPanel(panel, 75, 78, 133, 115, 1, "human");
         JButton elephantButton = addButtonToPanel(panel, 232, 78, 133, 115, 1, "elephant");
         JButton squirrelButton = addButtonToPanel(panel, 413, 78, 123, 115, 1, "squirrel");
         JButton frogButton = addButtonToPanel(panel, 593, 78, 98, 115, 1, "frog");
         JButton catButton = addButtonToPanel(panel, 763, 78, 98, 115, 1, "cat");
         JButton redButton = addButtonToPanel(panel, 92, 250, 130, 200, 3, "red");
         JButton blueButton = addButtonToPanel(panel, 260, 250, 130, 200, 3, "blue");
         JButton pinkButton = addButtonToPanel(panel, 427, 250, 130, 200, 3, "pink");
         JButton greenButton = addButtonToPanel(panel, 587, 250, 130, 200, 3, "green");
         JButton orangeButton = addButtonToPanel(panel, 750, 250, 130, 200, 3, "orange");
 
         JTextField nameBox = addTextToPanel(menuPanel, 420, 6, 225, 38); //480
 
         blockForInputCharacter(panel, playerBox1, difficultyValue, map, playerPanel, players);
         exitSafely();
         states[2] = nameBox.getText();
         return states;
     }
 
     /**
      * draw the town screen
      *
      * @param players the current players
      * @param currPlayer the index of the currently active player
      * @param store the store, used to get its quantities
      * @param numPlayers the number of players playing
      * @param round the round number
      *
      * @return an array of the states selected by the player
      **/
     public String[] drawTownScreen(ArrayList<Player> players, int currPlayer, Store store, int numPlayers, int round) {
         states = new String[2];
     
         ImagePanel panel = new ImagePanel("/media/town.png");
         panel.setPreferredSize(new Dimension(950, 525));
         panel.setLayout(null);
 
         JPanel playerPanel = new JPanel();
         playerPanel.setPreferredSize(new Dimension(950, 175));
         playerPanel.setLayout(null);
 
         drawGameStatus(players, playerPanel, currPlayer, store, numPlayers, round);
 
         ImagePanel menuPanel = new ImagePanel("/media/bp1.png");
         menuPanel.setPreferredSize(new Dimension(950, 50));
         menuPanel.setLayout(null);
 
         ArrayList<JPanel> panels = new ArrayList<JPanel>();
         panels.add(panel);
         panels.add(playerPanel);
         panels.add(menuPanel);
         changePanel(frame, panels);
 
         // buttons
         addButtonToPanel(panel, 60, 60, 200, 400, 0, "assay");
         addButtonToPanel(panel, 260, 60, 250, 400, 0, "store");
         addButtonToPanel(panel, 510, 60, 210, 400, 0, "land office");
         addButtonToPanel(panel, 720, 60, 200, 400, 0, "pub");
 		
         JButton backButton = addButtonToPanel(panel, 81, 456, 100, 61, 0, "back");
 		//addHoverIcon(backButton, "/media/hoverbuttons/backhover2.png");
 
         JButton stopButton = addButtonToPanel(menuPanel, 783, 5, 40, 40, 0, "stop");
 		addHoverIcon(stopButton, "/media/hoverbuttons/stophover.png");
         JButton pauseButton = addButtonToPanel(menuPanel, 839, 5, 40, 40, 0, "pause");
 		addHoverIcon(pauseButton, "/media/hoverbuttons/pausehover.png");
         JButton skipButton = addButtonToPanel(menuPanel, 894, 5, 40, 40, 0, "skip");
 		addHoverIcon(skipButton, "/media/hoverbuttons/skiphover.png");
 
         blockForInputMain(menuPanel);
         exitSafely();
         states[1] = "" + timer.getDelay();
         return states;
     }
 
     // state[0] = {"quit", "switchScreen", "food", "energy", "smithore", "crystite", "foodMule", "energyMule", "smithoreMule", "crystiteMule"}
     // state[1] = quantityFood
     // state[2] = quantityEnergy
     // state[3] = quantitySmithore
     // state[4] = quantityCrystite
     /**
      * draw the store screen
      *
      * @param players the list of players
      * @param currPlayer the index of the current player
      * @param transactionType buying or selling
      * @param quantities the quantities the player is purchasing or selling
      * @param store used to get the store quantities
      * @param numPlayers the total number of players
      * @param round the round number
      * @param text the text to print at the bottom of the screen
      *
      * @return an array of the states selected by the player
      **/
     public String[] drawStoreScreen(ArrayList<Player> players, int currPlayer, String transactionType, String[] quantities, 
         Store store, int numPlayers, int round, String text) {
 
         // initialize the states
         states = new String[5];
         states[0] = "quit";
         states[1] = quantities[0];
         states[2] = quantities[1];
         states[3] = quantities[2];
         states[4] = quantities[3];
 
         ImagePanel panel = new ImagePanel("/media/storecomponents/store" + transactionType + ".png");
         panel.setPreferredSize(new Dimension(950, 525));
         panel.setLayout(null);
 
         JPanel playerPanel = new JPanel();
         playerPanel.setPreferredSize(new Dimension(950, 175));
         playerPanel.setLayout(null);
 
         drawGameStatus(players, playerPanel, currPlayer, store, numPlayers, round);
         ArrayList<JLabel> storeLabels = drawStorePanelStatus(panel, null);
 
         ImagePanel menuPanel = new ImagePanel("/media/bp1.png");
         menuPanel.setPreferredSize(new Dimension(950, 50));
         menuPanel.setLayout(null);
 
         ArrayList<JPanel> panels = new ArrayList<JPanel>();
         panels.add(panel);
         panels.add(playerPanel);
         panels.add(menuPanel);
         changePanel(frame, panels);
 
         // buttons
         JButton backButton = addButtonToPanel(panel, 40, 455, 98, 58, 0, "quit"); 
 		addHoverIcon(backButton, "/media/hoverbuttons/backhover2.png");
         JButton switchButton = addButtonToPanel(panel, 166, 455, 98, 58, 0, "switchScreen"); 
         JButton foodButton = addButtonToPanel(panel, 471, 135, 42, 25, 0, "food"); 
         JButton energyButton = addButtonToPanel(panel, 471, 220, 42, 25, 0, "energy"); 
         JButton oreButton = addButtonToPanel(panel, 471, 305, 42, 25, 0, "smithore"); 
         JButton crystiteButton = addButtonToPanel(panel, 471, 391, 42, 25, 0, "crystite"); 
         JButton foodMuleButton = addButtonToPanel(panel, 693, 161, 42, 25, 0, "foodMule");
         JButton energyMuleButton = addButtonToPanel(panel, 693, 257, 42, 25, 0, "energyMule");
         JButton oreMuleButton = addButtonToPanel(panel, 853, 161, 42, 25, 0, "smithoreMule");
         JButton crystiteMuleButton = addButtonToPanel(panel, 853, 257, 42, 25, 0, "crystiteMule");
 		
 		if(transactionType.equals("sell")) {
 			addHoverIcon(switchButton, "/media/hoverbuttons/buyswitchhover.png");
 			addHoverIcon(foodButton, "/media/hoverbuttons/sellhover.png");
 			addHoverIcon(energyButton, "/media/hoverbuttons/sellhover.png");
 			addHoverIcon(oreButton, "/media/hoverbuttons/sellhover.png");
 			addHoverIcon(crystiteButton, "/media/hoverbuttons/sellhover.png");
 			addHoverIcon(foodMuleButton, "/media/hoverbuttons/sellhover.png");
 			addHoverIcon(energyMuleButton, "/media/hoverbuttons/sellhover.png");
 			addHoverIcon(oreMuleButton, "/media/hoverbuttons/sellhover.png");
 			addHoverIcon(crystiteMuleButton, "/media/hoverbuttons/sellhover.png");
 		}
 		else {
 			addHoverIcon(switchButton, "/media/hoverbuttons/sellswitchhover.png");
 			addHoverIcon(foodButton, "/media/hoverbuttons/buyhover.png");
 			addHoverIcon(energyButton, "/media/hoverbuttons/buyhover.png");
 			addHoverIcon(oreButton, "/media/hoverbuttons/buyhover.png");
 			addHoverIcon(crystiteButton, "/media/hoverbuttons/buyhover.png");
 			addHoverIcon(foodMuleButton, "/media/hoverbuttons/buyhover.png");
 			addHoverIcon(energyMuleButton, "/media/hoverbuttons/buyhover.png");
 			addHoverIcon(oreMuleButton, "/media/hoverbuttons/buyhover.png");
 			addHoverIcon(crystiteMuleButton, "/media/hoverbuttons/buyhover.png");
 		}
 
         JButton plusButton1 = addButtonToPanel(panel, 290, 117, 22, 18, 1, "+");
 		addHoverIcon(plusButton1, "/media/hoverbuttons/plushover.png");
         JButton minusButton1 = addButtonToPanel(panel, 290, 157, 22, 18, 1, "-");
 		addHoverIcon(minusButton1, "/media/hoverbuttons/minushover.png");
         JButton plusButton2 = addButtonToPanel(panel, 290, 202, 22, 18, 2, "+");
 		addHoverIcon(plusButton2, "/media/hoverbuttons/plushover.png");
         JButton minusButton2 = addButtonToPanel(panel, 290, 242, 22, 18, 2, "-");
 		addHoverIcon(minusButton2, "/media/hoverbuttons/minushover.png");
         JButton plusButton3 = addButtonToPanel(panel, 290, 288, 22, 18, 3, "+");
 		addHoverIcon(plusButton3, "/media/hoverbuttons/plushover.png");
         JButton minusButton3 = addButtonToPanel(panel, 290, 328, 22, 18, 3, "-");
 		addHoverIcon(minusButton3, "/media/hoverbuttons/minushover.png");
         JButton plusButton4 = addButtonToPanel(panel, 290, 373, 22, 18, 4, "+");
 		addHoverIcon(plusButton4, "/media/hoverbuttons/plushover.png");
         JButton minusButton4 = addButtonToPanel(panel, 290, 413, 22, 18, 4, "-");
 		addHoverIcon(minusButton4, "/media/hoverbuttons/minushover.png");
 
         JButton stopButton = addButtonToPanel(menuPanel, 783, 5, 40, 40, 0, "stop");
 		addHoverIcon(stopButton, "/media/hoverbuttons/stophover.png");
         JButton pauseButton = addButtonToPanel(menuPanel, 839, 5, 40, 40, 0, "pause");
 		addHoverIcon(pauseButton, "/media/hoverbuttons/pausehover.png");
         JButton skipButton = addButtonToPanel(menuPanel, 894, 5, 40, 40, 0, "skip");
 		addHoverIcon(skipButton, "/media/hoverbuttons/skiphover.png");
 
         drawStatusText(menuPanel, text);
 
         blockForInputStore(menuPanel, panel, storeLabels);
         exitSafely();
         return states;
     }
 
     /**
      * draw the menu screen
      *
      * @param players the list of players
      * @param currPlayer the index of the current player
      * @param store the store instance
      * @param numPlayers the number of players
      * @param round the current round number
      * @param map the current map
      * @param text the text to display at the bottom of the screen
      *
      * @return the array of states selected by the player
      **/
     public String[] drawMenuScreen(ArrayList<Player> players, int currPlayer, Store store, int numPlayers, int round, Map map, String text) {
         states = new String[2];
 
         ImagePanel panel = new ImagePanel("/media/map"+map.getMapNum()+".png");
         panel.setPreferredSize(new Dimension(950, 525));
         panel.setLayout(null);
 
         JPanel playerPanel = new JPanel();
         playerPanel.setPreferredSize(new Dimension(950, 175));
         playerPanel.setLayout(null);
 
         drawPlayerFlags(map, panel);
         drawPlayerMules(map, panel);
         drawTerrain(map, panel);
         drawGameStatus(players, playerPanel, currPlayer, store, numPlayers, round);
         
         ImagePanel menuPanel = new ImagePanel("/media/bp2.png");
         menuPanel.setPreferredSize(new Dimension(950, 50));
         menuPanel.setLayout(null);
 
         ArrayList<JPanel> panels = new ArrayList<JPanel>();
         panels.add(panel);
         panels.add(playerPanel);
         panels.add(menuPanel);
         changePanel(frame, panels);
 
         JButton stopButton = addButtonToPanel(menuPanel, 783, 5, 40, 40, 0, "stop");
 		addHoverIcon(stopButton, "/media/hoverbuttons/stophover.png");
         JButton pauseButton = addButtonToPanel(menuPanel, 839, 5, 40, 40, 0, "pause");
 		addHoverIcon(pauseButton, "/media/hoverbuttons/pausehover.png");
         JButton skipButton = addButtonToPanel(menuPanel, 894, 5, 40, 40, 0, "skip");
 		addHoverIcon(skipButton, "/media/hoverbuttons/skiphover.png");
         drawStatusText(menuPanel, text);
 
         blockForInputMain(menuPanel);
         exitSafely();
         states[1] = "" + timer.getDelay();
         return states;
     }
 
     /**
      * draw the land office screen
      *
      * @param players the list of players
      * @param currPlayer the index of the current player
      * @param store the store instance
      * @param numPlayers the number of players
      * @param round the round number
      *
      * @return The array of states selected by the player
      **/
     public String[] drawLandOfficeScreen(ArrayList<Player> players, int currPlayer, Store store, int numPlayers, int round) {
         states = new String[2];
 
         ImagePanel panel = new ImagePanel("/media/landoffice.png");
         panel.setPreferredSize(new Dimension(950, 525));
         panel.setLayout(null);
 
         JPanel playerPanel = new JPanel();
         playerPanel.setPreferredSize(new Dimension(950, 175));
         playerPanel.setLayout(null);
 
         drawGameStatus(players, playerPanel, currPlayer, store, numPlayers, round);
 
         ImagePanel menuPanel = new ImagePanel("/media/bp1.png");
         menuPanel.setPreferredSize(new Dimension(950, 50));
         menuPanel.setLayout(null);
 
         ArrayList<JPanel> panels = new ArrayList<JPanel>();
         panels.add(panel);
         panels.add(playerPanel);
         panels.add(menuPanel);
         changePanel(frame, panels);
 
         JButton stopButton = addButtonToPanel(menuPanel, 783, 5, 40, 40, 0, "stop");
 		addHoverIcon(stopButton, "/media/hoverbuttons/stophover.png");
         JButton pauseButton = addButtonToPanel(menuPanel, 839, 5, 40, 40, 0, "pause");
 		addHoverIcon(pauseButton, "/media/hoverbuttons/pausehover.png");
         JButton skipButton = addButtonToPanel(menuPanel, 894, 5, 40, 40, 0, "skip");
 		addHoverIcon(skipButton, "/media/hoverbuttons/skiphover.png");
 
         blockForInputMain(menuPanel);
         exitSafely();
         return states;
     }
 
     // State[0] = {"town", "time"}
     // State[1] = time left on timer
     /**
      * Draws the Main Game Screen
      *
      * @param map The current map type.
      * @param players The list of players playing.
      * @param currPlayer The index of the current player.
      * @param store The store instance.
      * @param numPlayers The number of players playing.
      * @param round The current round number.
      * @param text The text to be outputted on the menuPanel.
      *
      * @return The array of states selected by the player.
      */
     public String[] drawMainGameScreen(Map map, ArrayList<Player> players, int currPlayer, Store store, int numPlayers, int round, String text) {
         states = new String[2];
 
         ImagePanel panel = new ImagePanel("/media/map"+map.getMapNum()+".png");
         panel.setPreferredSize(new Dimension(950, 525));
         panel.setLayout(null);
 
         JPanel playerPanel = new JPanel();
         playerPanel.setPreferredSize(new Dimension(950, 175));
         playerPanel.setLayout(null);
 
         drawPlayerFlags(map, panel);
         drawPlayerMules(map, panel);
         drawTerrain(map, panel);
         drawGameStatus(players, playerPanel, currPlayer, store, numPlayers, round);
         
         ImagePanel menuPanel = new ImagePanel("/media/bp1.png");
         menuPanel.setPreferredSize(new Dimension(950, 50));
         menuPanel.setLayout(null);
 
         ArrayList<JPanel> panels = new ArrayList<JPanel>();
         panels.add(panel);
         panels.add(playerPanel);
         panels.add(menuPanel);
         changePanel(frame, panels);
 
         JButton[] buttons = new JButton[Map.WIDTH * Map.HEIGHT];
         for (int i = 0; i < Map.HEIGHT; i++) {
             for (int j = 0; j < Map.WIDTH; j++) {
                 buttons[i * Map.WIDTH + j] = addButtonToPanel(panel, 25 + j * 100, 25 + i * 100, 100, 100, 0, "" + (i * Map.WIDTH + j));
             }
         }
 
         JButton stopButton = addButtonToPanel(menuPanel, 783, 5, 40, 40, 0, "stop");
 		addHoverIcon(stopButton, "/media/hoverbuttons/stophover.png");
         JButton pauseButton = addButtonToPanel(menuPanel, 839, 5, 40, 40, 0, "pause");
 		addHoverIcon(pauseButton, "/media/hoverbuttons/pausehover.png");
         JButton skipButton = addButtonToPanel(menuPanel, 894, 5, 40, 40, 0, "skip");
 		addHoverIcon(skipButton, "/media/hoverbuttons/skiphover.png");
         drawStatusText(menuPanel, text);
 
         blockForInputMain(menuPanel);
         exitSafely();
         states[1] = "" + timer.getDelay();
         return states;
     }
 
     /**
      * Draws the Save Screen
      *
      * @param players The list of players playing.
      * @param currPlayer The index of the current player.
      * @param store The store instance.
      * @param numPlayers The number of players playing.
      * @param round The current round number.
      *
      * @return The array of states selected by the player.
      */
     public String[] drawSaveScreen(ArrayList<Player> players, int currPlayer, Store store, int numPlayers, int round) {
         states = new String[2];
         states[1] = "DefaultSave";
 
         ImagePanel panel = new ImagePanel("/media/savescreen.png");
         panel.setPreferredSize(new Dimension(950, 525));
         panel.setLayout(null);
 
         JPanel playerPanel = new JPanel();
         playerPanel.setPreferredSize(new Dimension(950, 175));
         playerPanel.setLayout(null);
 
         drawGameStatus(players, playerPanel, currPlayer, store, numPlayers, round);
 
         ImagePanel menuPanel = new ImagePanel("/media/bp0.png");
         menuPanel.setPreferredSize(new Dimension(950, 50));
         menuPanel.setLayout(null);
 
         ArrayList<JPanel> panels = new ArrayList<JPanel>();
         panels.add(panel);
         panels.add(playerPanel);
         panels.add(menuPanel);
         changePanel(frame, panels);
 
         JButton backButton = addButtonToPanel(menuPanel, 11, 7, 170, 40, 0, "back");
         JButton okayButton = addButtonToPanel(menuPanel, 770, 7, 170, 40, 0, "save");
 
         JTextField textBox = addTextToPanel(panel, 113, 332, 225, 38);
 
         blockForInput();
         exitSafely();
         states[1] = textBox.getText();
         return states;
     }
 
     /**
      * Draws the Load Screen
      *
      * @return The array of states selected by the player.
      */
     public String[] drawLoadScreen() {
         states = new String[2];
 
         ImagePanel panel = new ImagePanel("/media/loadscreen.png");
         panel.setPreferredSize(new Dimension(950, 700));
         panel.setLayout(null);
 
         JPanel playerPanel = new JPanel();
         playerPanel.setPreferredSize(new Dimension(950, 175));
         playerPanel.setLayout(null);
 
         ImagePanel menuPanel = new ImagePanel("/media/bp0.png");
         menuPanel.setPreferredSize(new Dimension(950, 50));
         menuPanel.setLayout(null);
 
         ArrayList<JPanel> panels = new ArrayList<JPanel>();
         panels.add(panel);
         panels.add(menuPanel);
         changePanel(frame, panels);
 
         JButton backButton = addButtonToPanel(menuPanel, 11, 7, 170, 40, 0, "back");
         JButton okayButton = addButtonToPanel(menuPanel, 770, 7, 170, 40, 0, "okay");
 
         JTextField textBox = addTextToPanel(panel, 110, 350, 225, 38);
 
         blockForInput();
         exitSafely();
         states[1] = textBox.getText();
         return states;
     }
 
     /**
      * Draws the Win Screen
      *
      * @param players The list of players playing.
      * @param currPlayer The index of the current player.
      * @param store The store instance.
      * @param numPlayers The number of players playing.
      * @param round The current round number.
      *
      * @return The array of states selected by the player.
      */
     public String[] drawWinScreen(ArrayList<Player> players, int currPlayer, Store store, int numPlayers, int round) {
         states = new String[2];
 
         ImagePanel panel = new ImagePanel("/media/gameover.png");
         panel.setPreferredSize(new Dimension(950, 525));
         panel.setLayout(null);
 
         JPanel playerPanel = new JPanel();
         playerPanel.setPreferredSize(new Dimension(950, 175));
         playerPanel.setLayout(null);
 
         drawGameStatus(players, playerPanel, currPlayer, store, numPlayers, round);
 
         ImagePanel menuPanel = new ImagePanel("/media/bp3.png");
         menuPanel.setPreferredSize(new Dimension(950, 50));
         menuPanel.setLayout(null);
 
         ArrayList<JPanel> panels = new ArrayList<JPanel>();
         panels.add(panel);
         panels.add(playerPanel);
         panels.add(menuPanel);
         changePanel(frame, panels);
 
         JButton backButton = addButtonToPanel(menuPanel, 11, 7, 170, 40, 0, "back");
         JButton okayButton = addButtonToPanel(menuPanel, 770, 7, 170, 40, 0, "okay");
 
         blockForInput();
         exitSafely();
         return states;
     }
     
 //Helper Methods
 
     /**
      * Changes the various panels.
      *
      * @param frame The frame to display the panels on.
      * @param panels ArrayList of panels to display.
      */
     private void changePanel(JFrame frame, ArrayList<JPanel> panels) {
         frame.getContentPane().removeAll();        
         for (JPanel panel : panels) {
             frame.add(panel);
         }
         frame.pack();
         frame.repaint();
         return;
     }
 
     /**
      * Ensures that player selects only "okay" or "back" to move on to next panel.
      */
     private void blockForInput() {
         // wait for a button to be clicked
         boolean waitingSafe = true; // used to avoid race condition
         while (waitingSafe) {
             try {
                 lock.lock();
                 waitingSafe = waiting;
             }
             finally {
                 lock.unlock();
             }
         }
     }
 
     /**
      * Allows player to select various maps and have indicator arrows drawn underneath them.
      *
      * @param panel The current panel to draw on.
      * @param infoPanel The panel that displays the selected map and game difficulty selected.
      * @param difficultyValue The current game difficulty.
      */
     private void blockForMapScreen(JPanel panel, ImagePanel infoPanel, String difficultyValue){ 
         try { Thread.sleep(100); } catch (Exception e) {}
         JTextField difficultyText = drawDifficulty(infoPanel, difficultyValue, 0, 125, 162, 25);
         JLabel map = addLabelToPanel(infoPanel, 21, 37, 119, 66, "/media/m"+ 1+ ".png");
         JLabel mapArrow = addLabelToPanel(panel, 199, 289, 45, 24, "/media/uparrow.png");
 
         panel.repaint();
         infoPanel.repaint();
 
         String oldState = states[1];
         boolean waitingSafe = true; // used to avoid race condition
 
         while (waitingSafe) {
             if (!oldState.equals(states[1])) {
                 infoPanel.remove(difficultyText);
                 difficultyText = drawDifficulty(infoPanel, difficultyValue, 0, 125, 162, 25);
 
                 if(states[1].equals("1")){
                     infoPanel.remove(map);
                     map = addLabelToPanel(infoPanel, 21, 37, 119, 66, "/media/m"+ 1+ ".png");
 
                     panel.remove(mapArrow);
                     mapArrow = addLabelToPanel(panel, 199, 289, 45, 24, "/media/uparrow.png");
                 }
                 if(states[1].equals("2")){
                     infoPanel.remove(map);
                     map = addLabelToPanel(infoPanel, 21, 37, 119, 66, "/media/m"+ 2+ ".png");
 
                     panel.remove(mapArrow);
                     mapArrow = addLabelToPanel(panel, 453, 289, 45, 24, "/media/uparrow.png");
                 }
                 if(states[1].equals("3")){
                     infoPanel.remove(map);
                     map = addLabelToPanel(infoPanel, 21, 37, 119, 66, "/media/m"+ 3+ ".png");
 
                     panel.remove(mapArrow);
                     mapArrow = addLabelToPanel(panel, 705, 289, 45, 24, "/media/uparrow.png");
                 }
                 if(states[1].equals("4")){
                     infoPanel.remove(map);
                     map = addLabelToPanel(infoPanel, 21, 37, 119, 66, "/media/m"+ 4+ ".png");
 
                     panel.remove(mapArrow);
                     mapArrow = addLabelToPanel(panel, 326, 444, 45, 24, "/media/uparrow.png");
                 }
                 if(states[1].equals("5")){
                     infoPanel.remove(map);
                     map = addLabelToPanel(infoPanel, 21, 37, 119, 66, "/media/m"+ 5+ ".png");
 
                     panel.remove(mapArrow);
                     mapArrow = addLabelToPanel(panel, 580, 444, 45, 24, "/media/uparrow.png");
                 }
                 panel.repaint();
                 infoPanel.repaint();
                 oldState = states[1];
             }
             try {
                 lock.lock();
                 waitingSafe = waiting;
             }
             finally {
                 lock.unlock();
             }
         }
     }
 
     /**
      * Allows player to select various difficulties/number of players 
      * and have indicator arrows drawn underneath them.
      *
      * @param panel The current panel to draw on.
      * @param infoPanel The panel that displays the selected map and game difficulty selected.
      * @param x The initial x position
      * @param y The initial y position
      * @param xMargin The margin between each option on screen.
      * @param numPlayers The number of players playing.
      * @param difficultyValue The current game difficulty.
      * @param playerPanel The panel where player stats are displayed.
      */
     private void blockForSetupScreen(JPanel panel, ImagePanel infoPanel, int x, int y, int xMargin, int numPlayers, String difficultyValue, JPanel playerPanel){
         try { Thread.sleep(100); } catch (Exception e) {}
         JLabel difficultyArrow = addLabelToPanel(panel, (Integer.parseInt(states[1])-1)*xMargin + x, y, 804, 200, "/media/uparrow.png");
         JLabel playerArrow = addLabelToPanel(panel, (Integer.parseInt(states[2])-1)*150 + x, 390, 804, 200, "/media/uparrow.png");
         JTextField difficultyText = drawDifficulty(infoPanel, getDifficultyValueString(Integer.parseInt(states[1])),  0, 125, 162, 25);
 
         panel.repaint();
         infoPanel.repaint();
         playerPanel.repaint();
 
         String oldState = states[1];
         String oldState2 = states[2];
 
         boolean waitingSafe = true; // used to avoid race condition
         while (waitingSafe) {
             if (!oldState.equals(states[1])) {
                 panel.remove(difficultyArrow);
                 difficultyArrow = addLabelToPanel(panel, (Integer.parseInt(states[1])-1)*xMargin + x , y, 804, 200, "/media/uparrow.png");
 
                 infoPanel.remove(difficultyText);
                 difficultyText = drawDifficulty(infoPanel, getDifficultyValueString(Integer.parseInt(states[1])), 0, 125, 162, 25);
                 
                 infoPanel.repaint();
                 panel.repaint();
                 oldState = states[1];
             }
             if (!oldState2.equals(states[2])) {
                 panel.remove(playerArrow);
                 playerArrow = addLabelToPanel(panel, (Integer.parseInt(states[2])-1)*140 + x , 390, 804, 200, "/media/uparrow.png");
 
                 
                 for (int i = 1; i < Integer.parseInt(states[2])+1; i++) {
                     playerPanel.remove(i);
                     JLabel playerBox = addLabelToPanel(playerPanel, i*158 , 0, 158, 175, "/media/p"+i+"0.png");
 
                 }
                 for (int i = Integer.parseInt(states[2])+1; i < 6; i++) {
                     JLabel playerBox = addLabelToPanel(playerPanel, i*158 , 0, 158, 175, "/media/p"+i+".png");
 
                 }
 
                 playerPanel.repaint();
                 panel.repaint();
 
                 oldState2 = states[2];
             }
             try {
                 lock.lock();
                 waitingSafe = waiting;
             }
             finally {
                 lock.unlock();
             }
         }
     }
 
     /**
      * Allows player to select various character types/colors
      * and have indicator arrows drawn underneath them.
      *
      * @param panel The current panel to draw on.
      * @param infoPanel The panel that displays the selected map and game difficulty selected.
      * @param difficultyValue The current game difficulty.
      * @param map The current map type.
      * @param playerPanel The panel where player stats are displayed.
      * @param players ArrayList of players currently playing.
      *
      * @return Image of races and colors already selected on infoPanel.
      */
     private JLabel blockForInputCharacter(JPanel panel, ImagePanel infoPanel, String difficultyValue, Map map, JPanel playerPanel, ArrayList<Player> players) {
         try { Thread.sleep(100); } catch (Exception e) {}
         JLabel charArrow = addLabelToPanel(panel, 117, 210, 45, 24, "/media/uparrow.png");
         JLabel colorArrow = addLabelToPanel(panel, 117, 482, 45, 24, "/media/uparrow.png");
         JLabel colors = addLabelToPanel(panel, 57, 247, 839, 226, "/media/" + states[1] + ".png");
 
         JTextField difficultyText = drawDifficulty(infoPanel, difficultyValue, 0, 125, 162, 25);
         JLabel map1 = addLabelToPanel(infoPanel, 21 , 37, 119, 66, "/media/m"+ map.getMapNum()+ ".png");
         
         JLabel photo = addLabelToPanel(playerPanel, 200 + (players.size()*160), 20, 100, 130, "/media/" + states[1].charAt(0) + states[3].charAt(0) + ".png");
 
         for (int i = 0; i < players.size(); i++)
         {
             Player player = players.get(i);
             addLabelToPanel(playerPanel, 200 + (i * 160), 20, 100, 130, "/media/" + player.getRace().charAt(0) + player.getColor().charAt(0) + ".png");
         }
         panel.repaint();
         playerPanel.repaint();
 
         String oldState = states[1];
         System.out.println(states[2]);
         String oldState2 = states[3];
 
         int currentPlayer = players.size();
 
         boolean waitingSafe = true; // used to avoid race condition
         while (waitingSafe) {
             if (!oldState.equals(states[1])){
                 panel.remove(colors);
                 colors = addLabelToPanel(panel, 57, 247, 839, 226, "/media/" + states[1] + ".png");
 
                 infoPanel.remove(map1);
 
                 map1 = addLabelToPanel(infoPanel, 21, 37, 119, 66, "/media/m"+ map.getMapNum()+ ".png");
 
                 infoPanel.remove(difficultyText);
                 difficultyText = drawDifficulty(infoPanel, difficultyValue, 0, 125, 162, 25);
 
                 if(states[1].equals("human")){
                     panel.remove(charArrow);
                     charArrow = addLabelToPanel(panel, 117, 210, 45, 24, "/media/uparrow.png");
                 }
                 else if(states[1].equals("elephant")){
                     panel.remove(charArrow);
                     charArrow = addLabelToPanel(panel, 275, 210, 45, 24, "/media/uparrow.png");
                 }
                 else if(states[1].equals("squirrel")){
                     panel.remove(charArrow);
                     charArrow = addLabelToPanel(panel, 450, 210, 45, 24, "/media/uparrow.png");
                 }
                 else if(states[1].equals("frog")){
                     panel.remove(charArrow);
                     charArrow = addLabelToPanel(panel, 619, 210, 45, 24, "/media/uparrow.png");
                 }
                 else if(states[1].equals("cat")){
                     panel.remove(charArrow);
                     charArrow = addLabelToPanel(panel, 787, 210, 45, 24, "/media/uparrow.png");
                 }
                 infoPanel.repaint();
                 panel.repaint();
                 oldState = states[1];
             }
             if (!oldState2.equals(states[3])) {
                 playerPanel.remove(photo);
                 photo = addLabelToPanel(playerPanel, 200 + (currentPlayer*160), 20, 100, 130, "/media/" + states[1].charAt(0) + states[3].charAt(0) + ".png");
 
 
                 if(states[3].equals("red")){
                     panel.remove(colorArrow);
                     colorArrow = addLabelToPanel(panel, 117, 482, 45, 24, "/media/uparrow.png");
                 }
                 else if(states[3].equals("blue")){
                     panel.remove(colorArrow);
                     colorArrow = addLabelToPanel(panel, 275, 482, 45, 24, "/media/uparrow.png");
                 }
                 else if(states[3].equals("pink")){
                     panel.remove(colorArrow);
                     colorArrow = addLabelToPanel(panel, 450, 482, 45, 24, "/media/uparrow.png");
                 }
                 else if(states[3].equals("green")){
                     panel.remove(colorArrow);
                     colorArrow = addLabelToPanel(panel, 619, 482, 45, 24, "/media/uparrow.png");
                 }
                 else if(states[3].equals("orange")){
                     panel.remove(colorArrow);
                     colorArrow = addLabelToPanel(panel, 787, 482, 45, 24, "/media/uparrow.png");
                 }
                 panel.repaint();
                 playerPanel.repaint();
                 oldState2 = states[3];
             }
             try {
                 lock.lock();
                 waitingSafe = waiting;
             }
             finally {
                 lock.unlock();
             }
         }
         return colors;
     }
 
     /**
      * Allows player to select various difficulties/number of players 
      * and have indicator arrows drawn underneath them.
      *
      * @param panel The current panel to draw on.
      */
     private void blockForInputMain(JPanel panel) {
         Date date = new Date();
         long currentTime = date.getTime();
         int timerNum = (int)(((currentTime - timeWhenTimerSet) / 1000) / 7);
         int oldTimerNum = timerNum;
         JLabel timerImage = addLabelToPanel(panel, 11, 4, 41, 41, "/media/t" + timerNum + ".png");
         panel.repaint();
         boolean waitingSafe = true;
         
         while (waitingSafe) {
             date = new Date();
             currentTime = date.getTime();
             timerNum = (int)(((currentTime - timeWhenTimerSet) / 1000) / 7);
 
             if (oldTimerNum != timerNum && !paused) {
                 try {
                     panel.remove(timerImage);
                 }
                 catch (NullPointerException e) {
 
                 }
                 timerImage = addLabelToPanel(panel, 11, 4, 41, 41, "/media/t" + timerNum + ".png");
                 panel.repaint();
                 oldTimerNum = timerNum;
             }
 
             try {
                 lock.lock();
                 waitingSafe = waiting;
             }
             finally {
                 lock.unlock();
             }
         }
     }  
 
     /**
      * Allows player to switch between "buy" and "sell" options within the store
      * as well as go back to the Town.
      *
      * @param panel The current panel to draw on.
      * @param storePanel The panel that displays the various store quantities.
      * @param storeLabels The labels drawn throughout the store that indicate how of a 
      *                    particular item the current player is purchasing.
      */
     private void blockForInputStore(JPanel panel, JPanel storePanel, ArrayList<JLabel> storeLabels) {
         Date date = new Date();
         long currentTime = date.getTime();
         int timerNum = (int)(((currentTime - timeWhenTimerSet) / 1000) / 7);
         int oldTimerNum = timerNum;
         JLabel timerImage = addLabelToPanel(panel, 11, 4, 41, 41, "/media/t" + timerNum + ".png");
         panel.repaint();
         boolean waitingSafe = true;
         String[] oldStates = new String[4];
         oldStates[0] = states[1];
         oldStates[1] = states[2];
         oldStates[2] = states[3];
         oldStates[3] = states[4];
         
         while (waitingSafe) {
             date = new Date();
             currentTime = date.getTime();
             timerNum = (int)(((currentTime - timeWhenTimerSet) / 1000) / 7);
 
             if (oldTimerNum != timerNum && !paused) {
                 try {
                     panel.remove(timerImage);
                 }
                 catch (NullPointerException e) {
 
                 }
                 timerImage = addLabelToPanel(panel, 11, 4, 41, 41, "/media/t" + timerNum + ".png");
                 oldTimerNum = timerNum;
                 panel.repaint();
             }
             if (!oldStates[0].equals(states[1]) || !oldStates[1].equals(states[2]) || !oldStates[2].equals(states[3]) || !oldStates[3].equals(states[4])) {
                 storeLabels = drawStorePanelStatus(storePanel, storeLabels);
                 oldStates[0] = states[1];
                 oldStates[1] = states[2];
                 oldStates[2] = states[3];
                 oldStates[3] = states[4];
                 storePanel.repaint();
             }
             try {
                 lock.lock();
                 waitingSafe = waiting;
             }
             finally {
                 lock.unlock();
             }
         }
     }  
 
     /**
      * Exit current panel safely to go to next panel.
      */
     private void exitSafely() {
         try {
             lock.lock();
             waiting = true;
         }
         finally {
             lock.unlock();
         }
     }
 
     /**
      * Draws all buttons used in game to appropriate panel.
      *
      * @param panel Panel to draw on.
      * @param x The initial x position of the button.
      * @param y The initial y position of the button.
      * @param width The width of the button.
      * @param height The height of the button.
      * @param stateNum The current state.
      * @param stateText The string associated with current state.
      */
     private JButton addButtonToPanel(JPanel panel, int x, int y, int width, int height, 
         final int stateNum, final String stateText) {
         final JButton button = new JButton();
         button.setBounds(x, y, width, height);
         panel.add(button);
         button.addMouseListener(new MouseAdapter() {
             public void mouseClicked(MouseEvent e) {
                 if (stateText.equals("+")) {
                     states[stateNum] = "" + (Integer.parseInt(states[stateNum]) + 1);
                     System.out.println(stateNum + " is now: " + states[stateNum]);
                 }
                 else if (stateText.equals("-")) {
                     System.out.println(stateNum + " is now: " + states[stateNum]);
                     states[stateNum] = "" + (Integer.parseInt(states[stateNum]) - 1);
                     if (states[stateNum].equals("-1")) {
                         states[stateNum] = "0";
                     }
                 }
                 else {
                     states[stateNum] = stateText; // set the new state
                     System.out.println(stateNum + " set to: " + stateText);
                 }
 
                 if (stateNum == 0) {
                     try {
                         lock.lock();
                         waiting = false;
                     }
                     finally {
                         lock.unlock();
                     }
                 }
             }
         });
         button.setOpaque(false);
         button.setContentAreaFilled(false);
         button.setBorderPainted(false);
         return button;
     }
 
     /**
      * Creates game timer.
      *
      * @param time Intiial time to count down from.
      */
     private Timer createTimer(int time) {
         ActionListener timerListener = new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 states[0] = "time";
                 try {
                     lock.lock();
                     waiting = false;
                 } 
                 finally {
                     lock.unlock();
                 }
             }
         };
         Timer timer = new Timer(time, timerListener);
         return timer;
     }
 
     /**
      * Pauses game timer.
      */
     public void pauseTimer() {
         if (paused)
             return;
         Date date = new Date();
         pauseTime = date.getTime();    
         timer.stop();
         paused = true;
     }
 
     /**
      * Unpauses game timer.
      */
     public void unpauseTimer() {
         if (!paused)
             return;
         Date date = new Date();
         long timePaused = (date.getTime() - pauseTime);
         pauseTime = 0;
         timeWhenTimerSet += timePaused;
         timer.start();
         paused = false;
     }
     
     /**
      * Stops game timer.
      */
     public void stopTimer() {
         timer.stop();
     }
 
     /**
      * Starts game timer.
      *
      * @param time Intiial time to put on timer.
      */
     public void startTimer(int time) {
         Date date = new Date();
         timeWhenTimerSet = date.getTime();
         timer.setDelay(time);
         timer.start();
     }
 
     /**
      * Restarts game timer.
      *
      * @param time Intiial time to put on timer.
      */
     public void restartTimer(int time) {
         Date date = new Date();
         timeWhenTimerSet = date.getTime();
         timer.setDelay(time);
         timer.restart();
     }
 
     /**
      * Gets how much time has passed since timer last started.
      */
     public int getElapsedTime() {
         Date date = new Date();
         return (int)(date.getTime() - timeWhenTimerSet);
     }
 
     /**
      * Draws JTextField on Save, Load, and Character Selection screen.
      *
      * @param panel Panel to draw on.
      * @param x The initial x position of the JTextField.
      * @param y The initial y position of the JTextField.
      * @param width The width of the JTextField.
      * @param height The height of the JTextField.
      *
      * @return Intial text to display on JTextField.
      */
     private JTextField addTextToPanel(JPanel panel, int x, int y, int width, int height) {
         final JTextField text = new JTextField("Enter Name");
         text.addMouseListener(new MouseAdapter(){
             public void mouseClicked(MouseEvent e){
                 text.setText("");
             }
         });
         text.setBounds(x, y, width, height);
         DefaultCaret c = (DefaultCaret)text.getCaret();
         c.setVisible(true);
         c.setDot(0);
         text.setFont(new Font("Candara", Font.PLAIN, 30));
         text.setHorizontalAlignment(JTextField.LEFT);
         text.setForeground(Color.WHITE);
         text.setBackground(new Color(87, 51, 4));
         text.setOpaque(false);
         text.setCaretColor(Color.WHITE);
         text.setBorder(javax.swing.BorderFactory.createEmptyBorder());
         panel.add(text);
         return text;
     }
 
     /**
      * Draws all JLabels for game.
      *
      * @param panel Panel to draw on.
      * @param x The initial x position of the JLabel.
      * @param y The initial y position of the JLabel.
      * @param width The width of the JLabel.
      * @param height The height of the JLabel.
      * @param image Image name to be drawn on pabel.
      *
      * @return Label to draw on panel.
      */
     private JLabel addLabelToPanel(JPanel panel, int x, int y, int width, int height, String image) {
         BufferedImage img;
         try {
             img = ImageIO.read(getClass().getResourceAsStream(image));
         }
         catch (Exception e) {
             System.out.println("Caught: " + e + " in function addLabelToPanel");
             return null;
         }
 
         ImageIcon icon = new ImageIcon(img);
         JLabel label = new JLabel();
         label.setIcon(icon);
         label.setBounds(x, y, width, height);
         panel.add(label, 0);
         return label;
     }
 
     /**
      * Draws Player and Store Stats on bottom panel throughout game.
      *
      * @param players The list of players playing.
      * @param panel Panel to draw on.
      * @param currPlayer The index of the current player.
      * @param store The store instance.
      * @param numPlayers The number of players playing.
      * @param round The current round number.
      */
     private void drawGameStatus(ArrayList<Player> players, JPanel panel, int currPlayer, Store store, int numPlayers, int round) {
         System.out.println("Size: " + players.size());
         String output;
         for (int i = 0; i < players.size(); i++) {
             drawPlayerStatus(players.get(i), i, panel);
             drawStoreStatus(store, panel);
         }
         if(round < 10){
             output = "0" + round;
         }
         else if(round > 12){
             output = "12";
         }
         else{
             output = "" + round;
         }
         JLabel roundLabel = new JLabel(output);
         roundLabel.setBounds(87, 127, 18, 18);
         panel.add(roundLabel);
 
         if (currPlayer >= 0) {  
             // current player color
             String colorPrefix = players.get(currPlayer).getColor().substring(0, 1);
             BufferedImage colorImg;
             try {
                 colorImg = ImageIO.read(getClass().getResourceAsStream("/media/circ" + colorPrefix + ".png"));
             }
             catch (Exception e) {
                 System.out.println("Caught: " + e + " in function drawGameStatus");
                 return;
             }
 
             JLabel colorLabel = new JLabel();
             ImageIcon colorIcon = new ImageIcon(colorImg);
             colorLabel.setIcon(colorIcon);
             colorLabel.setBounds(122, 128, 18, 18);
             panel.add(colorLabel);
         }
 
         // create boxes for players
         for (int i = 0; i < numPlayers+1; i++) {
             ImagePanel playerBox = new ImagePanel("/media/p" + i + "1.png");
             if (i == 0) {
                 playerBox.setBounds(0, 0, 160, 175);
             }
             else {
                 playerBox.setBounds(160 * i - (i - 1) * 2, 0, 158, 175);
             }
             panel.add(playerBox);
         }
 
         for (int i = numPlayers+1; i < 6; i++) {
             ImagePanel playerBox = new ImagePanel("/media/p" +i+".png");
             playerBox.setBounds(160 * i - (i - 1) * 2, 0, 158, 175);
             panel.add(playerBox);
         }
     }
 
     /**
      * Draws Store Stats within the Store.
      *
      * @param panel Panel to draw on.
      * @param labels Labels to be drawn on panel.
      *
      * @return ArrayList of JLabels to be drawn on panel.
      */
     private ArrayList<JLabel> drawStorePanelStatus(JPanel panel, ArrayList<JLabel> labels){
         int[] quantities = new int[4];
         if (labels != null) {
             for (int i = 0; i < labels.size(); i++) {
                 System.out.println("Removing: " + i);
                 panel.remove(labels.get(i));
             }
         }
         labels = new ArrayList<JLabel>();
         quantities[0] = Integer.parseInt(states[1]);
         quantities[1] = Integer.parseInt(states[2]);
         quantities[2] = Integer.parseInt(states[3]);
         quantities[3] = Integer.parseInt(states[4]);
         System.out.println("Drawing store with quantities: " + quantities[0] + ", " + quantities[1] + ", " + quantities[2] + ", " + quantities[3]);
         String output;
         //food label
         if(quantities[0] < 10){
             output = "0" + quantities[0];
         }
         else{
             output = "" + quantities[0];
         }
         System.out.println("output: " + output);
         JLabel foodLabel = new JLabel("" + output);
         foodLabel.setBounds(293, 135, 100, 20);
         foodLabel.setForeground(Color.WHITE);
         panel.add(foodLabel);
         labels.add(foodLabel);
 
         //food price
         JLabel foodPrice = new JLabel("" + (quantities[0] * 30));
         foodPrice.setBounds(401, 135, 100, 20);
         foodPrice.setForeground(Color.WHITE);
         panel.add(foodPrice);
         labels.add(foodPrice);
 
         //energy label
         if(quantities[1] < 10){
             output = "0" + quantities[1];
         }
         else{
             output = "" + quantities[1];
         }
         JLabel energyLabel = new JLabel("" + output);
         energyLabel.setBounds(293, 221, 100, 20);
         energyLabel.setForeground(Color.WHITE);
         panel.add(energyLabel);
         labels.add(energyLabel);
 
         //energy price
         JLabel energyPrice = new JLabel("" + (quantities[1] * 25));
         energyPrice.setBounds(401, 221, 100, 20);
         energyPrice.setForeground(Color.WHITE);
         panel.add(energyPrice);
         labels.add(energyPrice);
 
         //smithore label
         if(quantities[2] < 10){
             output = "0" + quantities[2];
         }
         else{
             output = "" + quantities[2];
         }
         JLabel smithoreLabel = new JLabel("" + output);
         smithoreLabel.setBounds(293, 307, 100, 20);
         smithoreLabel.setForeground(Color.WHITE);
         panel.add(smithoreLabel);
         labels.add(smithoreLabel);
 
         //smithore price
         JLabel smithorePrice = new JLabel("" + (quantities[2] * 50));
         smithorePrice.setBounds(401, 307, 100, 20);
         smithorePrice.setForeground(Color.WHITE);
         panel.add(smithorePrice);
         labels.add(smithorePrice);
 
         //crystite label
         if(quantities[3] < 10){
             output = "0" + quantities[3];
         }
         else{
             output = "" + quantities[3];
         }
         JLabel crystiteLabel = new JLabel("" + output);
         crystiteLabel.setBounds(293, 393, 100, 20);
         crystiteLabel.setForeground(Color.WHITE);
         panel.add(crystiteLabel);
         labels.add(crystiteLabel);
 
         //crystite price
         JLabel crystitePrice = new JLabel("" + (quantities[3] * 100));
         crystitePrice.setBounds(401, 393, 100, 20);
         crystitePrice.setForeground(Color.WHITE);
         panel.add(crystitePrice);
         labels.add(crystitePrice);
 
         return labels;
     }
 
     /**
      * Draws Store Stats throughout game.
      *
      * @param store The store instance.
      * @param panel Panel to draw on.
      */
     private void drawStoreStatus(Store store, JPanel panel){
         //food label
         JLabel foodLabel = new JLabel("" + store.getFoodQuantity());
         foodLabel.setBounds(45, 53, 100, 20);
         panel.add(foodLabel);
 
         //energy label
         JLabel energyLabel = new JLabel("" + store.getEnergyQuantity());
         energyLabel.setBounds(103, 53, 100, 20);
         panel.add(energyLabel);
 
         //smithore label
         JLabel smithoreLabel = new JLabel("" + store.getSmithoreQuantity());
         smithoreLabel.setBounds(45, 88, 100, 20);
         panel.add(smithoreLabel);
 
         //crystite label
         JLabel crystiteLabel = new JLabel("" + store.getCrystiteQuantity());
         crystiteLabel.setBounds(103, 88, 100, 20);
         panel.add(crystiteLabel);
 
         //mule label
         JLabel muleLabel = new JLabel("" + store.getMulesQuantity());
         muleLabel.setBounds(45, 125, 100, 20);
         panel.add(muleLabel);
     }
 
     /**
      * Draws player character on playerPanel during Character Selection phase.
      *
      * @param player The current player
      * @param number The margin between players.
      * @param playerPanel Panel to draw on.
      */
     private void drawPlayerCharacter(Player player, int number, JPanel playerPanel){
         int xBase = 0;
         int yBase = 30;
 
         // player name label
         JLabel playerLabel = new JLabel(player.getName());
         playerLabel.setBounds((xBase + 158 * (number + 1)) + 30, 30 , 100, 20);
         playerPanel.add(playerLabel);
     }
 
     /**
      * Draws player stats throughout the game.
      *
      * @param player The current player
      * @param number The margin between players.
      * @param playerPanel Panel to draw on.
      */
     private void drawPlayerStatus(Player player, int number, JPanel panel) {
         int xBase = 0;
         int yBase = 30;
 
         // player name label
         JLabel playerLabel = new JLabel(player.getName());
         playerLabel.setBounds((xBase + 158 * (number + 1)) + 30, yBase, 100, 20);
         panel.add(playerLabel);
 
         // food label
         JLabel foodLabel = new JLabel("" + player.getFood());
         foodLabel.setBounds((xBase + 158 * (number + 1)) + 45, yBase + 23, 100, 20);
         panel.add(foodLabel);
 
         // energy label
         JLabel energyLabel = new JLabel("" + player.getEnergy());
         energyLabel.setBounds((xBase + 158 * (number + 1)) + 105, yBase + 23, 100, 20);
         panel.add(energyLabel);
 
         // smithore label
         JLabel smithoreLabel = new JLabel("" + player.getSmithore());
         smithoreLabel.setBounds((xBase + 158 * (number + 1)) + 45, yBase + 58, 100, 20);
         panel.add(smithoreLabel);
 
         // crystite label
         JLabel muleLabel = new JLabel("" + player.getCrystite());
         muleLabel.setBounds((xBase + 158 * (number + 1)) + 107, yBase + 58, 100, 20);
         panel.add(muleLabel);
 
         // money label
         JLabel moneyLabel = new JLabel("" + player.getMoney());
         moneyLabel.setBounds((xBase + 158 * (number + 1) + 45), yBase + 95, 100, 20);
         panel.add(moneyLabel);
 
         // color label
         String colorPrefix = player.getColor().substring(0, 1);
         BufferedImage colorImg;
         try {
             colorImg = ImageIO.read(getClass().getResourceAsStream("/media/circ" + colorPrefix + ".png"));
         }
         catch (Exception e) {
             System.out.println("Caught: " + e + " in function drawPlayerStatus");
             return;
         }
 
         JLabel colorLabel = new JLabel();
         ImageIcon colorIcon = new ImageIcon(colorImg);
         colorLabel.setIcon(colorIcon);
         colorLabel.setBounds((xBase + 158 * (number + 1) + 124), yBase + 98, 18, 18);
         panel.add(colorLabel);
     }
 
     /**
      * Draws all player flags on map.
      *
      * @param map The current map type.
      * @param panel Panel to draw on.
      */
     private void drawPlayerFlags(Map map, JPanel panel) {
         for (int i = 0; i < Map.HEIGHT; i++) {
             for (int j = 0; j < Map.WIDTH; j++) {
                 Player owner = map.getOwnerOfTile(i * Map.WIDTH + j);
                 if (owner != null) {
                     drawPlayerFlag(i, j, owner, panel);
                 }
             }
         }
     }
 
     /**
      * Draws all player Mules on map.
      *
      * @param map The current map type.
      * @param panel Panel to draw on.
      */
     private void drawPlayerMules(Map map, JPanel panel) {
         for (int i = 0; i < Map.HEIGHT; i++) {
             for (int j = 0; j < Map.WIDTH; j++) {
                 Tile tile = map.getTiles()[i * Map.WIDTH + j];
                 if (tile != null && map.getTiles()[i * Map.WIDTH + j].hasMule()) {
                     drawPlayerMule(i, j, tile, panel);
                     System.out.println("Drawing mule at " + i + ", " + j);
                 }
             }
         }
     }
 
     /**
      * Draws M1, M2, M3.
      *
      * @param map The current map type.
      * @param panel Panel to draw on.
      */
     private void drawTerrain(Map map, JPanel panel) {
         for (int i = 0; i < Map.HEIGHT; i++) {
             for (int j = 0; j < Map.WIDTH; j++) {
                 String type = map.getTileType(i * Map.WIDTH + j);
                 if (type.length() == 9 && type.substring(0, 8).equals("mountain"))
                 {
                     drawMountain(i, j, type.substring(8, 9), panel);
                 }
             }
         }
     }
 
     /**
      * Draws current difficulty on Player Panel.
      *
      * @param panel Panel to draw on.
      * @param textString Text to be drawn in JTextField.
      * @param x The initial x position of the JTextField.
      * @param y The initial y position of the JTextField.
      * @param width The width of the JTextField.
      * @param height The height of the JTextField.
      *
      * @return Difficulty string to be displayed on the JTextField.
      */
     private JTextField drawDifficulty(JPanel panel, String textString, int x, int y, int width, int height) {
         JTextField text = new JTextField(textString);
         text.setBounds(x, y, width, height);
         text.setFont(new Font("Candara", Font.PLAIN, 20));
         text.setHorizontalAlignment(JTextField.CENTER);
         text.setForeground(Color.BLACK);
         text.setBackground(new Color(87, 51, 4));
         text.setOpaque(false);
         text.setBorder(javax.swing.BorderFactory.createEmptyBorder());
         panel.add(text);
         panel.repaint();
         return text;
     }
 
     /**
      * Draws various status information to menuPanel.
      *
      * @param panel Panel to draw on.
      * @param textString Text to be drawn on JTextField.
      *
      * @return Various status information.
      */
     public JTextField drawStatusText(JPanel panel, String textString) {
         if(panel == null){
             panel = new ImagePanel("/media/bp1.png");
             panel.setPreferredSize(new Dimension(950, 50));
             panel.setLayout(null);
         }
         if (textString == null) {
             return null;
         }
         System.out.println("Displaying: " + textString);
         JTextField text = new JTextField(textString);
         text.setBounds(60, 6, 600, 38);
         text.setFont(new Font("Candara", Font.PLAIN, 12));
         text.setHorizontalAlignment(JTextField.LEFT);
         text.setForeground(Color.WHITE);
         text.setBackground(new Color(87, 51, 4));
         text.setOpaque(false);
         text.setBorder(javax.swing.BorderFactory.createEmptyBorder());
         text.setCaretColor(Color.WHITE);
         panel.add(text);
         panel.repaint();
         return text;
     }
 
     /**
      * Draws player flags to indicate that property is owned.
      *
      * @param row Row to draw flag on.
      * @param column Column to draw flag on.
      * @param player Current player.
      * @param panel Panel to draw on.
      */
     private void drawPlayerFlag(int row, int column, Player player, JPanel panel) {
         System.out.println("Drawing at location " + row + ", " + column);
         BufferedImage flagImg;
         String colorPrefix = player.getColor().substring(0, 1);
 
         try {
             flagImg = ImageIO.read(getClass().getResourceAsStream("/media/flag" + colorPrefix + ".png"));
         }
         catch (Exception e) {
             System.out.println("Caught: " + e + " in function drawPlayerFlag");
             return;
         }
 
         JLabel flagLabel = new JLabel();
         ImageIcon flagIcon = new ImageIcon(flagImg); 
         flagLabel.setIcon(flagIcon);
         flagLabel.setBounds(25 + column * 100, 25 + row * 100, 100, 100);
         panel.add(flagLabel);
     }
 
     /**
      * Draws player Mule to indicate that property has a Mule.
      *
      * @param row Row to draw Mule on.
      * @param column Column to draw Mule on.
      * @param title Mule type to draw.
      * @param panel Panel to draw on.
      */
     private void drawPlayerMule(int row, int column, Tile tile, JPanel panel) {
         System.out.println("Drawing at location " + row + ", " + column);
         BufferedImage muleImg;
         String mulePrefix = tile.getMuleType().substring(0, 1);
         mulePrefix = mulePrefix.toLowerCase();
 
         try {
         muleImg = ImageIO.read(getClass().getResourceAsStream("/media/mule" + mulePrefix + ".png"));
 
         }
         catch (Exception e) {
             System.out.println("Caught: " + e + " in function drawPlayerMule");
             System.out.println("mulePrefix: " + mulePrefix);
             return;
         }
 
         JLabel muleLabel = new JLabel();
         ImageIcon muleIcon = new ImageIcon(muleImg); 
         muleLabel.setIcon(muleIcon);
         muleLabel.setBounds(25 + column * 100, 25 + row * 100, 100, 100);
         panel.add(muleLabel);
     }
 
     /**
      * Draws mountains on map.
      *
      * @param row Row to draw mountain on.
      * @param column Column to draw mountain on.
      * @param type Type of mountain to draw.
      * @param panel Panel to draw on.
      */
     private void drawMountain(int row, int column, String type, JPanel panel) {
         BufferedImage mountImg;
 
         try {
             mountImg = ImageIO.read(getClass().getResourceAsStream("/media/mount" + type + ".png"));
         }
         catch (Exception e) {
             System.out.println("Caught: " + e + " in function drawMountain");
             return;
         }
 
         JLabel mountLabel = new JLabel();
         ImageIcon mountIcon =  new ImageIcon(mountImg);
         mountLabel.setIcon(mountIcon);
         mountLabel.setBounds(25 + column * 100, 25 + row * 100, 100, 100);
         panel.add(mountLabel);
     }
 
     /**
      * Gets current difficulty number and returns a string.
      *
      * @param num Current difficulty int.
      *
      * @return Current difficulty string.
      */
     private String getDifficultyValueString(int num){
         String returnString = "";
         if (num == 1){
             returnString = "Easy";
         }
         if (num == 2){
             returnString = "Medium";
         }
         if (num == 3){
             returnString = "Hard";
         }
         return returnString;
     }
 	
     /**
      * Draws hover buttons throughout game.
      *
      * @param button Button draw hover image over.
      * @param image Image to draw.
      */
 	private void addHoverIcon(final JButton button, String image) {
 		BufferedImage img;
 		BufferedImage defaultImg;
 		try {
 			img = ImageIO.read(getClass().getResourceAsStream(image));
 			defaultImg = ImageIO.read(getClass().getResourceAsStream("/media/trans.png"));
         }
         catch (Exception e) {
             System.out.println("Caught: " + e + " in function addHoverIcon");
             System.out.println("button: " + image);
             return;
         }
 		final ImageIcon defaultIcon = new ImageIcon(defaultImg);
 		final ImageIcon icon = new ImageIcon(img);
 		button.setIcon(defaultIcon);
 		button.addMouseListener(new MouseAdapter(){
 			public void mouseEntered(MouseEvent e){
 				button.setIcon(icon);
 				button.repaint();
 			}
 			public void mouseExited(MouseEvent e){
 				button.setIcon(defaultIcon);
 				button.repaint();
 			}
 		});
 	}
 }
