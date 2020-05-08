 package game;
 
 import game.sprites.*;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 
public class MarsLanderGame extends JComponent implements SpriteContainer {
     private final GameManager gameManager;
     private MarsLander lander;
     private Astronaut astronaut;
     private boolean isJetFiring;
     private static final double JET_ACCELERATION = 0.005;
     private static final double CRASH_VELOCITY = 0.2;
 
     public MarsLanderGame(int width, int height, GameManager manager) {
         this.gameManager = manager;
         setPreferredSize(new Dimension(width, height));
         setBounds(0,0,width, height);
         startGame();
     }
 
     private void startGame() {
         astronaut = null;
         lander = createLanderSprite();
         setInitalParameters();
         lander.start();
         requestFocus();
     }
 
     private void setInitalParameters() {
         isJetFiring = false;
         lander.showLander();
         lander.setPosition(new SpriteVector(100.0, 100.0));
         lander.setVelocity(new SpriteVector(0.095, 0.0));
         lander.setAcceleration(new SpriteVector(0.0, 0.003)); // Gravity
         lander.setCollisionLoss(0.95);
     }
 
     @Override
     public void paint(Graphics g) {
         lander.paint(g, this);
         if (astronaut != null) {
             astronaut.paint(g, this);
         }
     }
 
     public void restart() {
         stopSprites();
         try {
             Thread.sleep(1000);
         } catch (InterruptedException e1) {
             Thread.currentThread().interrupt();
         }
         startGame();
     }
 
     public void keyPressed() {
         if (!isJetFiring && astronaut == null) {
             jetOn();
         }
     }
 
     public void keyReleased() {
         if (isJetFiring) {
             jetOff();
         }
     }
 
     private void jetOn() {
         isJetFiring = true;
         lander.showLanderWithFlame();
         lander.changeAcceleration(new SpriteVector(0, -JET_ACCELERATION));
     }
 
     private void jetOff() {
         isJetFiring = false;
         lander.showLander();
         lander.changeAcceleration(new SpriteVector(0, JET_ACCELERATION));
     }
 
     private MarsLander createLanderSprite() {
         MarsLander sprite = new MarsLander(new SpriteImageManager(), this);
         sprite.setBounds(getBounds());
         return sprite;
     }
 
     private Astronaut createAstronautSprite() {
         SpriteVector position = lander.getPosition();
         position.x += lander.getSpriteSize()/2;
         Astronaut sprite = new Astronaut(new SpriteImageManager(), this);
         sprite.setBounds(getBounds());
         sprite.setPosition(position);
         sprite.setVelocity(new SpriteVector(0.15, 0.0));
         return sprite;
     }
 
     private void stopSprites() {
         lander.requestStop();
         if (astronaut != null) {
             astronaut.requestStop();
         }
     }
 
     // SpriteContainer method
     public void hitBottom(final double velocity) {
         stopGame(velocity < CRASH_VELOCITY);
     }
 
     private void stopGame(boolean didPlayerWin) {
         if (isJetFiring) {
             jetOff();
         }
         if (didPlayerWin) {
             showWin();
             gameManager.gameWon();
         } else {
             showLoss();
             gameManager.gameLost();
         }
         lander.requestStop();
     }
 
     private void showLoss() {
         lander.showCrash();
     }
 
     private void showWin() {
         astronaut = createAstronautSprite();
         astronaut.start();
     }
 
     public static void main(String[] args) {
         JFrame frame = new MarsLanderGameFrame();
         frame.setSize(600,450);
         frame.setVisible(true);
     }
 
 }
 
 class MarsLanderGameFrame extends JFrame implements GameManager {
     private final MarsLanderGame game;
     private final JLabel gamesWon;
     private final JLabel gamesLost;
     private int numberGamesWon = 0;
     private int numberGamesLost = 0;
 
     public MarsLanderGameFrame() {
         setLayout(new FlowLayout());
 
         game = new MarsLanderGame(450, 450, this);
         add(game);
 
         final JPanel controlPanel = new JPanel();
         controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
 
         JButton startOverButton = new JButton("Start Over");
         gamesWon = new JLabel();
         gamesLost = new JLabel();
         controlPanel.add(gamesWon);
         controlPanel.add(gamesLost);
         controlPanel.add(startOverButton);
         updateGameStats();
         getRootPane().setDefaultButton(startOverButton);
 
         add(controlPanel);
 
         startOverButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 game.restart();
             }
         });
 
         addWindowListener(new WindowAdapter() {
             @Override
             public void windowClosing(WindowEvent e) {
                 System.exit(0);
             }
         });
 
         addWindowFocusListener(new WindowAdapter() {
             @Override
             public void windowGainedFocus(WindowEvent e) {
                 game.requestFocusInWindow();
             }
         });
 
         game.addKeyListener(new KeyAdapter() {
             @Override
             public void keyPressed(KeyEvent e) {
                 game.keyPressed();
             }
             @Override
             public void keyReleased(KeyEvent e) {
                 game.keyReleased();
             }
         });
 
     }
 
     public void gameWon() {
         numberGamesWon++;
         updateGameStats();
     }
 
     public void gameLost() {
         numberGamesLost++;
         updateGameStats();
     }
 
     private void updateGameStats() {
         gamesWon.setText("Games Won: " + numberGamesWon);
         gamesLost.setText("Games Lost: " + numberGamesLost);        
     }
 }
