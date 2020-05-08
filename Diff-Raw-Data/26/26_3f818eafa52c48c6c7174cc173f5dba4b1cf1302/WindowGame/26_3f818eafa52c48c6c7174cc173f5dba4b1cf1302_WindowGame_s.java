 package RockContorter;
 
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 public class WindowGame implements ActionListener, KeyListener {
 
 
     JPanel titlePanel, scorePanel, buttonPanel;
     JTextArea gameTextAre;
     JButton leftButton, rightButton, upButton, downButton;
     public static Game game;
     private Timer clock;
 
     public WindowGame() {
         Board board = new Board();
         game = new Game(board);
     }
 
     public JPanel createContentPane (){
 
         JPanel totalGUI = new JPanel();
         totalGUI.setLayout(null);
         totalGUI.addKeyListener(this);
 
         titlePanel = new JPanel();
         titlePanel.setLayout(null);
         titlePanel.setLocation(10, 0);
         titlePanel.setSize(450, 450);
         titlePanel.addKeyListener(this);
         totalGUI.add(titlePanel);
 
 
         gameTextAre = new JTextArea();
         gameTextAre.setText(CommandLineGame.present(game));
         gameTextAre.setLocation(0, 0);
         gameTextAre.setSize(700, 700);
         gameTextAre.setFont(new Font("Monospaced", Font.PLAIN, 12));
         gameTextAre.setForeground(Color.black);
         gameTextAre.addKeyListener(this);
         titlePanel.add(gameTextAre);
 
 
         scorePanel = new JPanel();
         scorePanel.setLayout(null);
         scorePanel.setLocation(10, 40);
         scorePanel.setSize(260, 30);
         scorePanel.addKeyListener(this);
         totalGUI.add(scorePanel);
 
 
 
         buttonPanel = new JPanel();
         buttonPanel.setLayout(null);
         buttonPanel.setLocation(0, 350);
         buttonPanel.setSize(260, 160);
         buttonPanel.addKeyListener(this);
         totalGUI.add(buttonPanel);
 
 //
 //        leftButton = new JButton("Left");
 //        leftButton.setLocation(30,80);
 //        leftButton.setSize(70, 70);
 //        leftButton.addActionListener(this);
 //        leftButton.addKeyListener(this);
 //        buttonPanel.add(leftButton);
 //
 //        downButton = new JButton("Down");
 //        downButton.setLocation(110, 80);
 //        downButton.setSize(70, 70);
 //        downButton.addActionListener(this);
 //        downButton.addKeyListener(this);
 //        buttonPanel.add(downButton);
 //
 //        rightButton = new JButton("Right");
 //        rightButton.setLocation(190, 80);
 //        rightButton.setSize(70, 70);
 //        rightButton.addActionListener(this);
 //        rightButton.addKeyListener(this);
 //        buttonPanel.add(rightButton);
 //
 //        upButton = new JButton("Up");
 //        upButton.setLocation(110, 0);
 //        upButton.setSize(70, 70);
 //        upButton.addActionListener(this);
 //        upButton.addKeyListener(this);
 //        buttonPanel.add(upButton);
 
         totalGUI.setOpaque(true);
         return totalGUI;
     }
 
     public void actionPerformed(ActionEvent e) {
         if((e.getSource() == leftButton)){
             game.Move(Game.Move.MOVELeft);
         }else if(e.getSource() == rightButton){
             game.Move(Game.Move.MOVERight);
         }else if(e.getSource() == upButton){
             game.Move(Game.Move.MOVEUp);
         } else if(e.getSource() == downButton) {
             game.Move(Game.Move.MoveDown);
         } else if (e.getSource() == clock ) {
             WindowGame.game.Update();
             updateUI();
             return;
         }
         WindowGame.game.Update();
         updateUI();
     }
 
     private static void createAndShowGUI() {
         JFrame.setDefaultLookAndFeelDecorated(true);
         JFrame frame = new JFrame("[=] JButton Scores! [=]");
 
         WindowGame demo = new WindowGame();
         frame.setContentPane(demo.createContentPane());
 
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setSize(500, 700);
         frame.setVisible(true);
         frame.addKeyListener(demo);
 
         demo.clock = new Timer(100, demo);
         demo.clock.start();
     }
 
     private void updateUI() {
         if(gameTextAre!=null)
             gameTextAre.setText(CommandLineGame.present(game));
     }
 
     public void play(){
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 createAndShowGUI();
             }
         });
     }
 
     @Override
     public void keyTyped(KeyEvent e) {
     }
 
     @Override
     public void keyPressed(KeyEvent e) {
 
     }
 
     @Override
     public void keyReleased(KeyEvent e) {
         switch(e.getKeyCode()) {
             case KeyEvent.VK_UP:
             case KeyEvent.VK_W:
                 game.Move(Game.Move.MOVEUp);
                 break;
             case KeyEvent.VK_DOWN:
             case KeyEvent.VK_S:
                 game.Move(Game.Move.MoveDown);
                 break;
             case KeyEvent.VK_RIGHT:
             case KeyEvent.VK_D:
                 game.Move(Game.Move.MOVERight);
                 break;
             case KeyEvent.VK_LEFT:
             case KeyEvent.VK_A:
                 game.Move(Game.Move.MOVELeft);
                 break;
             case KeyEvent.VK_E:
                game.PositionRock();
                 game.RockPlace();
                 break;
             case KeyEvent.VK_R:
                 game.ThrowCounter = 0;
                 game.PositionRock();
                 game.ThrewRock = true;
                 game.RockInFront();
                 break;
             case KeyEvent.VK_F:
                 game.Leap();
                 break;
             case KeyEvent.VK_G:
                 game.ChopWall();
                 break;
             case KeyEvent. VK_T:
                 game.RockShell();
                 break;
             case KeyEvent. VK_Y:
                 game.MadeWave = true;
                 break;
         }
         updateUI();
     }
 }
 
 
 
 
