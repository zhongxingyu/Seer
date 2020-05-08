 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: oscka063
  * Date: 2012-09-28
  * Time: 16:23
  * To change this template use File | Settings | File Templates.
  */
 public class PyramidFrame extends JFrame{
 
     public boolean ready;
     private final int SQUARE_SIZE = 64;
     private Board myBoard;
     private Objectives myObjectives;
     JPanel boardPanel = new JPanel();
     JPanel gamePanel = new JPanel();
     JPanel insSquarePanel = new JPanel();
     BoardViewer myBoardViewer;
     InsSquareViewer myInsSquareViewer;
     ObjectiveViewer myObjViewer;
     private JButton[] myArrowViewers = new JButton[12];
     public Player[] players = new Player[2];
     public GameInformation gameInfo;
     public int playerNumber;
     boolean[] isPressed = new boolean[256];
 
     //MouseAdapter myMA = new MouseAdapterMod();
 
     private int aViewerCount = 0;
 
 
     public PyramidFrame(String title, Board pyramidBoard) throws HeadlessException  {
         super(title);
         this.myBoard = pyramidBoard;
         this.myObjectives = new Objectives(pyramidBoard.size);
         this.myBoardViewer = new BoardViewer(pyramidBoard, players, myObjectives);
         this.myObjViewer = new ObjectiveViewer(myObjectives);
         setSize(1380, 709);
         setResizable(false);
 
         KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
         manager.addKeyEventDispatcher(new MyDispatcher());
 
         myInsSquareViewer = new InsSquareViewer(myBoardViewer.getInsertImage(myBoard.insertionSquare.getImage()));
         insObjUpdate();
         initLayout();
         initPlayers();
         createMenus();
         gameInfo = new GameInformation(myBoard.myIntTypeBoard, myBoard.myIntRotBoard, myObjectives.myObjBoard, myBoard.myInsSquareType, myBoard.myInsSquareRot, myObjectives.insertionObjective, makePositions(players[0].getXPosition(), players[0].getYPosition(), players[1].getXPosition(), players[1].getYPosition()));
     }
 
     public int[] makePositions(int player1X, int player1Y, int  player2X, int player2Y) {
         int[] tempPos = new int[4];
         tempPos[0] = player1X;
         tempPos[1] = player1Y;
         tempPos[2] = player2X;
         tempPos[3] = player2Y;
         return tempPos;
     }
 
     //Initializes the players
     public void initPlayers () {
         players[0] = new Player(0, 0, myBoard.size, myBoard);
         players[1] = new Player(myBoard.size - 1, myBoard.size - 1, myBoard.size, myBoard);
     }
 
     private void initLayout() {
         //Adds the arrow buttons
         for (int i = 0; i < myArrowViewers.length; i++) {
 
             ImageIcon arrowIcon;
              final int j = i;
              final char dir;
              if (i < 3) {
                  dir = 'S';
                  arrowIcon = new ImageIcon("resources/arrow3.png");
              } else if (i < 6) {
                  dir = 'E';
                  arrowIcon = new ImageIcon("resources/arrow2.png");
              } else if (i < 9) {
                  dir = 'W';
                  arrowIcon = new ImageIcon("resources/arrow4.png");
              } else if (i < 12) {
                  dir = 'N';
                  arrowIcon = new ImageIcon("resources/arrow1.png");
              } else {
                  dir = 'F';
                  arrowIcon = new ImageIcon("resources/arrow1.png");
              }
 
             myArrowViewers[i] = new JButton(arrowIcon);
             myArrowViewers[i].setMargin(new Insets (0, 0, 0, 0));
             myArrowViewers[i].setBorder(null);
             myArrowViewers[i].setBackground(Color.DARK_GRAY);
             myArrowViewers[i].setMaximumSize(new Dimension(64, 64));
 
              myArrowViewers[i].addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e)
                 {
                     //((j % 3) * 2)  + 1 refers to the movable rows or columns
                     myBoard.insertSquare(((j % 3) * 2)  + 1, dir);
                     myObjectives.insertSquare(((j % 3) * 2) + 1, dir);
                     myInsSquareViewer.repaint();
                     myInsSquareViewer.updateImage(myBoardViewer.getInsertImage(myBoard.insertionSquare.getImage()));
                     insObjUpdate();
                     myBoardViewer.repaint();
                 }
             });
         }
 
         GridLayout mainLayout = new GridLayout(1,2);
 
         GridBagLayout gbl= new GridBagLayout();
         setLayout(mainLayout);
         boardPanel.setLayout(gbl);
         boardPanel.setBackground(Color.DARK_GRAY);
         add(boardPanel);
         //myBoardViewer.addMouseListener(myMA);
         gamePanel.setLayout(new GridLayout(2, 2));
         add(gamePanel);
 
         //Initializes the insSquarePanel
         insSquarePanel.setLayout(new GridLayout(1, 2));
         insSquarePanel.add(myInsSquareViewer);
         JButton rotationButton = new JButton("Rotate");
         insSquarePanel.add(rotationButton);
 
         rotationButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 myBoard.insertionSquare.rotateSquare();
                 myInsSquareViewer.updateImage(myBoardViewer.getInsertImage(myBoard.insertionSquare.getImage()));
                 insObjUpdate();
                 myInsSquareViewer.repaint();
             }
         });
 
         gamePanel.add(insSquarePanel);
         JButton readyButton = new JButton("Done");
         readyButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 ready = false;
             }
         });
         gamePanel.add(readyButton);
         GridBagConstraints c = new GridBagConstraints();
 
 
 
         //Sets the top row
         c.fill =GridBagConstraints.BOTH;
         c.gridx = 0;
         c.gridy = 0;
         c.weightx = 0.1;
         c.weighty = 0.1;
         c.anchor = GridBagConstraints.PAGE_START;
         boardPanel.add(new EmptySquare(), c);
 
 
         for (int i = 1; i < 8; i++) {
             if((i % 2) != 0) {
                 c.fill =GridBagConstraints.BOTH;
                 c.gridx = i;
                 c.gridy = 0;
                 boardPanel.add(new EmptySquare(), c);
             }
             else {
                 c.fill =GridBagConstraints.BOTH;
                 c.gridx = i;
                 c.gridy = 0;
                 boardPanel.add(myArrowViewers[aViewerCount], c);
                 aViewerCount++;
             }
         }
         c.fill =GridBagConstraints.BOTH;
         c.gridx = 8;
         c.gridy = 0;
         boardPanel.add(new EmptySquare(), c);
 
         //Sets the left column
         c.weightx = 0.2;
 
         for (int i = 1; i < 8; i++) {
             if((i % 2) != 0) {
                 c.fill =GridBagConstraints.BOTH;
                 c.gridx = 0;
                 c.gridy = i;
                 boardPanel.add(new EmptySquare(), c);
             }
             else {
                 c.fill =GridBagConstraints.BOTH;
                 c.gridx = 0;
                 c.gridy = i;
                 c.anchor = GridBagConstraints.CENTER;
                 boardPanel.add(myArrowViewers[aViewerCount], c);
                 aViewerCount++;
             }
         }
         c.fill =GridBagConstraints.BOTH;
         c.gridx = 0;
         c.gridy = 8;
         boardPanel.add(new EmptySquare(), c);
 
         //Sets the board in the center
         c.fill =GridBagConstraints.BOTH;
         c.gridx = 1;
         c.gridy = 1;
         c.weightx = 0.2;
         c.weighty = 0.2;
         c.gridheight = 7;
         c.gridwidth = 7;
         c.anchor = GridBagConstraints.CENTER;
         boardPanel.add(myBoardViewer, c);
 
 
         //Sets the column to the right of the board
 
         c.weightx = 0.1;
         c.weighty = 0.1;
         c.gridheight = 1;
         c.gridwidth = 1;
         for (int i = 1; i < 8; i++) {
             if((i % 2) != 0) {
                 c.fill =GridBagConstraints.BOTH;
                 c.gridx = 8;
                 c.gridy = i;
                 boardPanel.add(new EmptySquare(), c);
             }
             else {
                 c.fill =GridBagConstraints.BOTH;
                 c.gridx = 8;
                 c.gridy = i;
                 boardPanel.add(myArrowViewers[aViewerCount], c);
                 aViewerCount++;
             }
         }
 
         //Sets the bottom row
         c.weightx = 0.1;
         c.weighty = 0.1;
 
         for (int i = 1; i < 8; i++) {
             if((i % 2) != 0) {
                 c.fill =GridBagConstraints.BOTH;
                 c.gridx = i;
                 c.gridy = 8;
                 boardPanel.add(new EmptySquare(), c);
             }
             else {
                 c.fill =GridBagConstraints.BOTH;
                 c.gridx = i;
                 c.gridy = 8;
                 boardPanel.add(myArrowViewers[aViewerCount], c);
                 aViewerCount++;
             }
         }
         c.fill =GridBagConstraints.BOTH;
         c.gridx = 8;
         c.gridy = 8;
         boardPanel.add(new EmptySquare(), c);
 
 
     }
     private void insObjUpdate() {
         if (myObjectives.insertionObjective > 0) {
             myInsSquareViewer.updateObjective(myBoardViewer.getInsObjImage(myObjectives.insertionObjective));
         }
     }
 
 
     public void implementGameInfo() {
         int[] tempPos = gameInfo.getPositions();
         players[0].setPosition(tempPos[0], tempPos[1]);
         players[1].setPosition(tempPos[2], tempPos[3]);
         myBoard.myIntRotBoard = gameInfo.getRotBoard();
         myBoard.myIntTypeBoard = gameInfo.getTypeBoard();
         myBoard.myInsSquareRot = gameInfo.getInsRot();
         myBoard.myInsSquareType = gameInfo.getInsType();
         myObjectives.myObjBoard = gameInfo.getObjBoard();
         myObjectives.insertionObjective = gameInfo.getInsObj();
         myBoard.integerToInsSquare();
         myInsSquareViewer.updateImage(myBoardViewer.getInsertImage(myBoard.insertionSquare.getImage()));
         insObjUpdate();
 
     }
     public void exportGameInfo() {
         gameInfo.setInsSquare(myBoard.myInsSquareType, myBoard.myInsSquareRot);
         gameInfo.setInsObj(myObjectives.insertionObjective);
         gameInfo.setPositions(makePositions(players[0].getXPosition(), players[0].getYPosition(), players[1].getXPosition(), players[1].getYPosition()));
 
     }
 
     //Player movement
  /*   class MouseAdapterMod extends MouseAdapter {
         public void mousePressed(MouseEvent e) {
             if (ready) {
                 players[playerNumber].setPosition((e.getX() * myBoard.size) / (myBoard.size * SQUARE_SIZE), (e.getY() * myBoard.size) / (myBoard.size * SQUARE_SIZE));
                 updateArea();
             }
         }
     }  */
 
     private class MyDispatcher implements KeyEventDispatcher {
         @Override
         public boolean dispatchKeyEvent(KeyEvent ke) {
             if (ke.getID() == KeyEvent.KEY_PRESSED) {
                System.out.println("bajs");
                 if (ready) {
                     int i = ke.getKeyCode();
                    System.out.println(i);
                     if (!isPressed[i]) {
                         isPressed[i] = true;
                         System.out.println(i);
                         makeMove(i);
                     }
                 }
                 updateArea();
             } else if (ke.getID() == KeyEvent.KEY_RELEASED) {
                 int i = ke.getKeyCode();
                 isPressed[i] = false;
             }
             return false;
         }
     }
 
 
     public void makeMove(int keyCode) {
         switch(keyCode) {
             case 37 : players[playerNumber].moveWest();
                 break;
             case 38 : players[playerNumber].moveNorth();
                 break;
             case 39 : players[playerNumber].moveEast();
                 break;
             case 40 : players[playerNumber].moveSouth();
                 break;
         }
     }
 
 
 
 
     //Creates the menus
     public void createMenus() {
         final JMenu file = new JMenu("File");
         JMenuItem fileExit = new JMenuItem("Exit");
         file.add(fileExit);
         final JMenuBar bar = new JMenuBar();
         bar.add(file);
         setJMenuBar(bar);
         fileExit.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent ae) {
                 int reply = JOptionPane.showConfirmDialog(null, "Really exit the pyramid?", "Quit", JOptionPane.YES_NO_OPTION);
                 if (reply == JOptionPane.YES_OPTION) {
                     System.exit(0);
                 }
 
             }
         });
     }
     public void updateArea() {
         myBoardViewer.repaint();
         myInsSquareViewer.repaint();
         myObjViewer.repaint();
     }
 
 
 
 
 }
