 /*
  * This is the overlap between the user experience and the innner guts of the
  * battleship program. The Board has a thorough understanding of its role, as
  * does the Ship.
  *
  * Game will represent an experimental, text-based interaction between the
  * user and the board.
  */
 package gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
import java.util.List;
 
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.border.LineBorder;
 
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
 import logic.BSCoordinate;
 import logic.BSSquare;
 import ships.ship.Battleship;
 import ships.ship.Carrier;
 import ships.ship.Cruiser;
 import ships.ship.Destroyer;
 import ships.Orientation;
 import ships.ship.SteelSubmarine;
 import ships.ship.Submarine;
 import boards.Board;
 import boards.CBoard;
 import boards.UBoard;
 import bots.BSAI;
 import bots.CBoardAIRecursive;
 import debug.BSIO;
 import debug.Debug;
 
 /**
  *
  * @author joelgreenberg
  */
 public class GameGUI extends JFrame implements ActionListener {
 
     //Establish global constants.
    private static List<String> shipnames = ImmutableList.of("Carrier", "Battleship", "Cruiser", "Destroyer", "Submarine", "SteelSubmarine");
     public static final int board_x = 15;
     public static final int board_y = 20;
     //Establish the "standard" variables for this game.
     private UBoard userboard;
     private CBoard compboard;
     private BoardPainter userpaint, comppaint;
     private BSAI compboard_AI;
     //GUI - specific stuff
     private JTextField xcoord, ycoord, statusfield;
     private JPanel playingBoardPanel;
     private JComboBox combo_ship, combo_orientation;
     private JPanel controlpanel;
     private BSButton button_shoot, button_placeship, button_quitgame, button_DES, button_random, button_newgame, button_placeexplosive;
     private javax.swing.JTextArea across, down;
     private ArrayList<String> ships_to_be_placed;
 //    private BoardPainter3D threedimensionalprinter;
 
     public GameGUI() {
 
         //First, let's take care of GUI functions. These will be functions that do
         //NOT change as a result of
         setSize(900, 500);
         setLocation(100, 100);
         setDefaultCloseOperation(EXIT_ON_CLOSE);
 
 
         //Initialize things that are used in the GUI, too.
         controlpanel = new JPanel();
         button_shoot = new BSButton("Shoot");
         button_placeship = new BSButton("Place Ship");
         button_placeexplosive = new BSButton("Place Explosive");
         button_quitgame = new BSButton("Quit Game");
         button_DES = new BSButton("Double-Edged Sword");
         across = new JTextArea("Across:");
         down = new JTextArea("Down:");
         button_random = new BSButton("Random Coords");
         button_newgame = new BSButton("New Game");
         across.setEditable(false);
         down.setEditable(false);
         ships_to_be_placed = new ArrayList<String>();
        for (int i = 0; i < getShipnames().length; i++) {
            ships_to_be_placed.add(getShipnames()[i]);
         }
 
         //Establish the content pane. Name it 'content'
         //content.setSize(new Dimension(200,200)); This line did not fix the problem of size.
         Container content = getContentPane();
         content.setLayout(new BorderLayout());
 
         statusfield = new JTextField("Player's move to make.", 30);
         statusfield.setEditable(false);
         statusfield.setBorder(new LineBorder(Color.yellow));
 
         content.add(statusfield, BorderLayout.NORTH);
 
         playingBoardPanel = new JPanel();
         playingBoardPanel.setLayout(new FlowLayout());
 
         //Draw a canvas in the content pane.
 
         controlpanel.setSize(150, 350);
         //      controlpanel.setBorder(new LineBorder(Color.orange, 5));
         controlpanel.setLayout(new FlowLayout());
 
         combo_ship = new JComboBox();
         combo_ship.addActionListener(this);
 
         combo_orientation = new JComboBox();
         combo_orientation.addItem("Horizontal");
         combo_orientation.addItem("Vertical");
 
         JPanel coordinatepanel = new JPanel();
         coordinatepanel.setBorder(new LineBorder(Color.blue, 2));
         coordinatepanel.setLayout(new GridLayout(2, 2));
         xcoord = new JTextField("x", 3);
         xcoord.setEditable(true);
         xcoord.addActionListener(this);
         ycoord = new JTextField("y", 3);
         ycoord.setEditable(true);
         ycoord.addActionListener(this);
 
         JPanel shipplacementpanel = new JPanel();
         shipplacementpanel.setLayout(new GridLayout(2, 1));
         shipplacementpanel.add(combo_orientation);
         shipplacementpanel.add(combo_ship);
         controlpanel.add(shipplacementpanel, BorderLayout.SOUTH);
 
         {
             button_shoot.addActionListener(this);
             button_placeship.addActionListener(this);
             button_quitgame.addActionListener(this);
             button_DES.addActionListener(this);
             button_random.addActionListener(this);
             button_newgame.addActionListener(this);
             button_placeexplosive.addActionListener(this);
             combo_ship.addActionListener(this);
 
         }
         {
             coordinatepanel.add(across);
             coordinatepanel.add(xcoord);
             coordinatepanel.add(down);
             coordinatepanel.add(ycoord);
 
             controlpanel.add(coordinatepanel, BorderLayout.SOUTH);
         }
 
         {
             JPanel shootingpanel = new JPanel();
             shootingpanel.setLayout(new GridLayout(2, 2));
             shootingpanel.add(button_random);
             shootingpanel.add(button_placeship);
             shootingpanel.add(button_shoot);
             shootingpanel.add(button_placeexplosive);
 
             controlpanel.add(shootingpanel, BorderLayout.SOUTH);
         }
 
         controlpanel.add(button_DES);
 
         {
             JPanel buttonpanel = new JPanel();
             buttonpanel.setLayout(new GridLayout(2, 1));
             buttonpanel.add(button_newgame);
             buttonpanel.add(button_quitgame);
             controlpanel.add(buttonpanel, BorderLayout.SOUTH);
         }
 
         button_DES.setEnabled(false);
         button_placeship.setEnabled(false);
         button_random.setEnabled(false);
         button_shoot.setEnabled(false);
         button_placeexplosive.setEnabled(false);
 
         content.add(controlpanel, BorderLayout.SOUTH);
         userboard = new UBoard(board_x, board_y);
         compboard = new CBoard(board_x, board_y);
         userpaint = new BoardPainter(userboard, this);
         comppaint = new BoardPainter(compboard, this);
         compboard_AI = new CBoardAIRecursive(compboard, userboard);
 
         playingBoardPanel.add(userpaint, BorderLayout.WEST);
         playingBoardPanel.add(comppaint, BorderLayout.EAST);
         content.add(playingBoardPanel, BorderLayout.CENTER);
         print("You must click \"New Game\" to begin the game, or \"Quit Game\" to exit.");
         setVisible(true);
     }
 
    public static String[] getShipnames()
    {
        return Iterables.toArray(shipnames, String.class);
    }

     public synchronized void newgame() {
         combo_ship.setEditable(false);
         combo_orientation.setEditable(false);
         button_placeship.setEnabled(true);
         button_random.setEnabled(true);
         button_shoot.setEnabled(false);
         button_placeexplosive.setEnabled(true);
         button_DES.setEnabled(false);
         //Clear the computer board and prepare it for re-deployment.
         compboard.initialize();
         compboard_AI.initialize();
         placeComputerShips();
 
         //Clear the user board and prepare it for re-deployment.
         userboard.initialize();
         ships_to_be_placed.clear();
        for (int i = 0; i < getShipnames().length; i++) {
            ships_to_be_placed.add(getShipnames()[i]);
         }
 
         combo_ship.removeAllItems();
         combo_ship.addActionListener(this);
 
         for (int i = 0; i < ships_to_be_placed.size(); i++) {
             combo_ship.addItem(ships_to_be_placed.get(i));
         }
 
         Debug.println("UserPaint: " + userpaint.getLocation() + "; CompPaint " + comppaint.getLocation());
 
         comppaint.repaint();
         userpaint.repaint();
 
         Board[] boards = {compboard, userboard};
 
 //        threedimensionalprinter = new BoardPainter3D(boards);
         print("Player, begin by setting ships. You have " + ships_to_be_placed.size() + " to place--select ships from the drop-down menu.");
 
 
 
     }
 
 //    public void start() {
 //        computer_placeships();
 //        while (this.ships_to_be_placed.size() != 0) {
 //        }
 //
 //        beginGame();
 //    }
 //    private void beginGame() {
 //        setVisible(true);
 //        userboard.beginGame();
 //        compboard.beginGame();
 //        Debug.println("Game begun.");
 //    }
     
     private void placeComputerShips() {
         compboard_AI.placeships();
     }
 
     /*
      * Mouse clicking stuff below -- do not touch (yet)
      */
     public void mouse_inputter(MouseEvent e, String brd) {
 
         if (button_placeship.isEnabled() || button_placeexplosive.isEnabled()) //we're attempting to place ships.
         {
             if (brd.equals("UBoard") && button_placeship.isEnabled()) {
                 placeShip();
             } else if (brd.equals("UBoard") && !button_placeship.isEnabled()) {
                 print("No more ships to place here...put the explosive on your opponent's board!");
             } else {
                 if (button_placeexplosive.isEnabled()) {
                     print("Click below to set an explosive on your opponent at this location!");
                 } else {
                     print("Put the ship on your board, NOT your opponent's!");
                 }
             }
 
         } else if (button_shoot.isEnabled()) {
             if (brd.equals("CBoard")) {
                 usershot();
             } else {
                 print("No friendly fire--you wish to attack your enemy!");
             }
         }
     }
 
     public void actionPerformed(ActionEvent e) {
         if (e.getActionCommand().equals("Quit Game")) {
             quitGame();
         }
 
         if (e.getActionCommand().equals("Place Ship")) {
             placeShip();
         }
 
         if (e.getActionCommand().equals("Shoot")) {
             usershot();
         }
 
         if (e.getActionCommand().equals("Random Coords")) {
             xcoord.setText(Integer.valueOf(BSIO.getRandomInt(compboard.x_dim)).toString());
             ycoord.setText(Integer.valueOf(BSIO.getRandomInt(compboard.y_dim)).toString());
             combo_orientation.setSelectedIndex(BSIO.getRandomInt(2));
         }
 
         if (e.getActionCommand().equals("Double-Edged Sword")) {
             doubleEdgedSword();
         }
 
         if (e.getActionCommand().equals("New Game")) {
             Debug.println("New Game!");
             newgame();
 
         }
 
 
 
 
         if (e.getActionCommand().equals("Place Explosive")) {
             placeexplosive();
         }
 
     }
 
     private void placeexplosive() {
         if (userboard.setExplosive(getCoords()[0], getCoords()[1])) {
             print("Don't tell your opponent--but the explosive is placed at " + userboard.getExplosive() + ".");
             button_placeexplosive.setEnabled(false);
             if (ships_to_be_placed.size() == 0) {
                 startgame();
             }
 
         } else {
             print("Invalid placement of explosive!");
         }
 
     }
 
     private void quitGame() {
         System.exit(0);
     }
 
     private void placeShip() {
         print("Placing ship . . . ");
         //We will attempt to place a ship at the coordinates as described.
 
         String ship = (String) combo_ship.getSelectedItem();
         Debug.println("From the combo_ship: the ship is " + ship + " and the index is " + combo_ship.getSelectedIndex() + " of " + combo_ship.getItemCount());
         int x = getCoords()[0];
         int y = getCoords()[1];
         String orientation = (String) combo_orientation.getSelectedItem();
         boolean shipplaced = false;
 
         if (ships_to_be_placed.contains(ship) && x != -1 && y != -1)//it is still necessary to place at least one ship of this type.
         {
             Debug.println("ships_to_be_placed is ok.");
             if (ship.equals("Battleship")) {
                 shipplaced = userboard.placeship(new Battleship(userboard, x, y, Orientation.getOrientationFromString(orientation)));
             } else if (ship.equals("Cruiser")) {
                 shipplaced = userboard.placeship(new Cruiser(userboard, x, y, Orientation.getOrientationFromString(orientation)));
             } else if (ship.equals("Destroyer")) {
                 shipplaced = userboard.placeship(new Destroyer(userboard, x, y, Orientation.getOrientationFromString(orientation)));
             } else if (ship.equals("Carrier")) {
                 shipplaced = userboard.placeship(new Carrier(userboard, x, y, Orientation.getOrientationFromString(orientation)));
             } else if (ship.equals("SteelSubmarine")) {
                 shipplaced = userboard.placeship(new SteelSubmarine(userboard, x, y, Orientation.getOrientationFromString(orientation)));
             } else/*if (ship.equals("Submarine"))*/ {
                 shipplaced = userboard.placeship(new Submarine(userboard, x, y, Orientation.getOrientationFromString(orientation)));
             }
 
             if (shipplaced) {
                 combo_ship.removeItem(ship);
                 userpaint.repaint();
                 ships_to_be_placed.remove(ship);
                 print("A ship was placed: " + ship + " at " + x + "," + y + " with a " + orientation + " orientation. There are " + ships_to_be_placed.size() + " ships left to place.");
 
                 if (ships_to_be_placed.size() == 0) { // This phase of the game has ended. We now play battleship!
                     if (!button_placeexplosive.isEnabled()) {
                         startgame();
 
                     } else {
                         button_placeship.setEnabled(false);
 
                     }
 
                 }
             } else //there was no need to place that ship.
             {
                 print("There was an error placing your ship. Please try again.");
             }
 
         } else {
             print("There was an error placing your ship--probably with the coordinates. Please try again.");
 
         }
 
     }
 
     private void startgame() {
         print("All ships are placed--let's play Battleship! ");
         combo_ship.setEditable(false);
         combo_orientation.setEditable(false);
         button_placeship.setEnabled(false);
         button_shoot.setEnabled(true);
         button_DES.setEnabled(true);
 
         if (userboard.sink(compboard.getExplosive())) {
             append("The computer's explosive sunk the user's ship! ");
         } else {
             userboard.shoot(compboard.getExplosive());
             append("The computer's explosive did nothing.");
         }
 
         if (compboard.sink(userboard.getExplosive())) {
             append("The user's explosive sunk the computer's ship!");
         } else {
             compboard.shoot(userboard.getExplosive());
             append("The user's explosive did nothing.");
 
         }
 
         comppaint.repaint();
         userpaint.repaint();
     }
 
     private void usershot() {
         int x = getCoords()[0];
         int y = getCoords()[1];
         //Shooting commands here...
         int user_shot_status = compboard.shoot(x, y);
         if (user_shot_status == Board.B_ALREADYHIT) { //The computer will NOT go again after this.
             print("The coordinate " + xcoord.getText() + "," + ycoord.getText() + " has already been hit. Try again!");
         } else //The computer WILL go again after this.
         {
             comppaint.repaint();
             switch (user_shot_status) {
                 case Board.B_MISS:
                     print("User shoots. The coordinate " + xcoord.getText() + "," + ycoord.getText() + " was a miss.");
                     break;
 
                 case Board.B_HIT:
                     print("User shoots. The coordinate " + xcoord.getText() + "," + ycoord.getText() + " was a hit.");
                     break;
 
                 case Board.B_HIT_SUNK:
                     print("User shoots. The coordinate " + xcoord.getText() + "," + ycoord.getText() + " was a hit, and it sunk the ship.");
                     break;
 
                 case Board.B_GAMELOST:
                     print("User shoots. The coordinate " + xcoord.getText() + "," + ycoord.getText() + " was a hit--and the game is over.");
                     button_shoot.setEnabled(false);
                     button_DES.setEnabled(false);
 
                     break;
             }
             if (!gameOver()) { //The game is not over--the opponent shoots now.
                 opponentshot();
             } else //The game is over
             {
                 button_shoot.setEnabled(false);
                 button_DES.setEnabled(false);
             }
 
         }
     }
 
     private void opponentshot() {
         BSCoordinate current = compboard_AI.nextshot();
 
 //        int compshot_x = BSIO.getRandomInt(board_x);
 //        int compshot_y = BSIO.getRandomInt(board_y);
         Debug.println("Trying to attack ship at: " + current);
         int comp_shot_status = userboard.shoot(current);
         while (comp_shot_status != Board.B_MISS && comp_shot_status != Board.B_HIT && comp_shot_status != Board.B_GAMELOST && comp_shot_status != Board.B_HIT_SUNK) {
 //            compshot_x = BSIO.getRandomInt(board_x);
 //            compshot_y = BSIO.getRandomInt(board_y);
             current = compboard_AI.nextshot();
             comp_shot_status = userboard.shoot(current);
         }
 
         int compshot_x = current.x();
         int compshot_y = current.y();
 
         switch (comp_shot_status) {
             case Board.B_MISS:
                 append("Comp shoots. The coordinate " + compshot_x + "," + compshot_y + " was a miss.");
                 break;
 
             case Board.B_HIT:
                 compboard_AI.setHit(current);
                 append("Comp shoots. The coordinate " + compshot_x + "," + compshot_y + " was a hit.");
                 break;
 
             case Board.B_HIT_SUNK:
                 compboard_AI.setHit(current);
                 compboard_AI.setSunk(current);
                 append("Comp shoots. The coordinate " + compshot_x + "," + compshot_y + " was a hit, and it sunk the ship.");
                 break;
 
             case Board.B_GAMELOST:
                 append("Comp shoots. The coordinate " + compshot_x + "," + compshot_y + " was a hit--and the game is over.");
                 button_shoot.setEnabled(false);
                 button_DES.setEnabled(false);
                 break;
         }
 
         userpaint.repaint();
     }
 
     private void doubleEdgedSword() {
 
         //We will randomly bomb one ship from each board.
         int compcount = -1, usercount = -1;
 
         //We will randomly select ships from each board to shoot.
         for (int i = 0; i <
                 compboard.getBoardShips().size(); i++) {
             if (!compboard.getBoardShips().get(i).isSunk()) {
                 compcount++;
             }
 
         }
         for (int i = 0; i <
                 userboard.getBoardShips().size(); i++) {
             if (!userboard.getBoardShips().get(i).isSunk()) {
                 usercount++;
             }
 
         }
         System.out.println("Double-edged sword on ships " + compcount + "," + usercount);
 
         if (compcount > 0 && usercount > 0) {
             int compship = BSIO.getRandomInt(compcount);
             int usership = BSIO.getRandomInt(usercount);
 
             //Now we will randomly select squares on each ship to shoot.
             int compcount_sq = -1, usercount_sq = -1;
 
             for (int i = 0; i <
                     compboard.getBoardShips().get(compship).getLength(); i++) {
                 if (compboard.getBoardShips().get(compship).getSquares().get(i).status() != BSSquare.Status.HIT && compboard.getBoardShips().get(compship).getSquares().get(i).status() != BSSquare.Status.SUNK) {
                     compcount_sq++;
                 }
 
             }
             for (int i = 0; i <
                     userboard.getBoardShips().get(usership).getLength(); i++) {
                 if (userboard.getBoardShips().get(usership).getSquares().get(i).status() != BSSquare.Status.HIT && userboard.getBoardShips().get(usership).getSquares().get(i).status() != BSSquare.Status.SUNK) {
                     usercount_sq++;
                 }
 
             }
             Debug.println("Random for DES: " + compcount_sq + " " + usercount_sq);
 
             if (compcount_sq > 0 && usercount_sq > 0) {
                 int compshot = BSIO.getRandomInt(compcount_sq);
                 int usershot = BSIO.getRandomInt(usercount_sq);
 
                 //Now we have our squares selected. We need to find the coordinates.
                 int compcounter = -1, usercounter = -1;
                 for (int i = 0; i <
                         compboard.getBoardShips().get(compship).getLength(); i++) {
                     if (compboard.getBoardShips().get(compship).getSquares().get(i).status() != BSSquare.Status.HIT && compboard.getBoardShips().get(compship).getSquares().get(i).status() != BSSquare.Status.SUNK) {
                         compcounter++;
                         if (compcounter == compshot) {
                             compboard.shoot(compboard.getBoardShips().get(compship).getSquares().get(i).x(), compboard.getBoardShips().get(compship).getSquares().get(i).y());
                         }
 
                     }
                 }
                 for (int i = 0; i <
                         userboard.getBoardShips().get(usership).getLength(); i++) {
                     if (userboard.getBoardShips().get(usership).getSquares().get(i).status() != BSSquare.Status.HIT && userboard.getBoardShips().get(usership).getSquares().get(i).status() != BSSquare.Status.SUNK) {
                         usercounter++;
                         if (usercounter == usershot) {
                             userboard.shoot(new BSCoordinate(userboard.getBoardShips().get(usership).getSquares().get(i).x(), userboard.getBoardShips().get(usership).getSquares().get(i).y()));
                             compboard_AI.setHit(new BSCoordinate(userboard.getBoardShips().get(usership).getSquares().get(i).x(), userboard.getBoardShips().get(usership).getSquares().get(i).y()));
 
                         }
 
                     }
                 }
             }
             userpaint.repaint();
             comppaint.repaint();
             print("The double-edged sword function just randomly hit one ship on each board.");
             button_DES.setEnabled(false);
         }
 
     }
 
     private boolean gameOver() {
         return userboard.hasLost() || compboard.hasLost();
     }
 
     public JPanel getPlayingBoardPanel() {
         return playingBoardPanel;
     }
 
     public Board user() {
         return userboard;
     }
 
     public Board comp() {
         return compboard;
     }
 
     private void print(String s) {
         Debug.println(s);
         statusfield.setText(s);
     }
 
     private void append(String s) {
         Debug.println("Appending: " + s);
         String n = statusfield.getText();
         statusfield.setText(n + " " + s);
     }
 
     public void printCoords(int x, int y) {
         xcoord.setText(Integer.valueOf(x).toString());
         ycoord.setText(Integer.valueOf(y).toString());
 
     }
 
     public void printCoords(String x, String y) {
         xcoord.setText(x);
         ycoord.setText(y);
 
     }
 
     private int[] getCoords() {
         int x, y;
         try {
             x = Integer.valueOf(xcoord.getText());
             y = Integer.valueOf(ycoord.getText());
         } catch (Exception e) {
             x = -1;
             y = -1;
         }
         return new int[] {x, y};
     }
 }
