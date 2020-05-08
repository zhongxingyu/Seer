 package GUIComponents;
 
 import Background.Background;
 import Controls.Controls;
 import Game.Game;
 import Game.Network.GameClient;
 import Game.Network.GameServer;
 import Game.Network.GameState;
 import Game.PlayerDeathException;
 import Path.StraightPath;
 import Projectile.ComplexProjectile;
 import Projectile.Projectile;
 import Spawn.Spawn;
 import Unit.*;
 import Unit.Player;
 import Weapon.LaserWeapon;
 import Weapon.ProtonWeapon;
 import java.awt.Graphics;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.RenderingHints;
 import java.awt.Shape;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.Rectangle2D;
 import java.util.ArrayList;
 import javax.swing.ImageIcon;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.Timer;
 
 /**
  *
  * @author Jere
  */
 public class GamePanel extends JPanel {
 
     /*
      *  The panel needs to have somewhere to get data from, and logically this is
      * the game that is currently taking place. Perhaps have this as a constructor
      * parameter, or have it passed in using another method.
      */
     Game gameLogic;
     Color bgColor = Color.BLACK;
     Player one;
     Spawn sp;
     LaserWeapon laser;
     ProtonWeapon proton;
     int player1_x = 500;
     int player1_y = 500;
     Controls a;
     Boolean mouse; //if mouse is being used or not
     int width;
     int height;
     Timer timer;
     ArrayList<Unit> spawns;
     int counter;
     Background background;
     int damage;
     int speed;
     Shape shape;
     Color color;
     boolean running;
     boolean switchPanel = false;
     boolean playerDeath = false;
     boolean paused = false;
     GameState currentState;
     GameServer gameServer;
     GameClient gameClient;
     boolean networked = false;
 
     public GamePanel(int width, int height) {
         this.width = width;
         this.height = height;
     }
 
     public GamePanel(int width, int height, int port, int maxConnections) {
         this(width, height);
         gameServer = new GameServer(port, maxConnections);
         gameClient = null;
         networked = true;
     }
 
     public GamePanel(int width, int height, String host, int port) {
         this(width, height);
         gameClient = new GameClient(host, port);
         gameClient.connectToServer();
         gameServer = null;
         networked = true;
     }
 
     /**
      * Draws all objects that need to be drawn.
      * @param g
      */
     @Override
     protected void paintComponent(Graphics g) {
         counter++;
         counter = counter % 400;
         Graphics2D g2 = (Graphics2D) g;
         g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                 RenderingHints.VALUE_ANTIALIAS_ON);
         g2.setColor(Color.blue);
 //        g2.draw(new Line2D.Double(30, 30, 500, 500));
 //        try{
 //        shootGame.getProjectileArray().get(shootGame.getProjectileArray().size()-1).draw(g2);
 //        }catch(Exception e){
 //        }
         render(g2);
     }
 
     /**
      * Tries to get this panel to regain focus.
      */
     public void regainFocus() {
         this.setFocusable(true);
         this.requestFocusInWindow();
     }
 
     // Initialization
     public void initialize() {
         sp = new Spawn();
         spawns = new ArrayList();
         laser = new LaserWeapon();
         proton = new ProtonWeapon();
         gameLogic = new Game();
         background = new Background(40);
         one = new Player(300, 200, laser, 200, player1_x, player1_y, Color.WHITE);
         a = new Controls();
         setBackground(bgColor);
         gameLogic.pruneArrays(this.getSize());
         // height = this.getSize().height;
         // width = this.getSize().width;
         one.setLocation(new Point(player1_x, player1_y));
         mouse = a.isMouse();
         gameLogic.addPlayer(one);
 //        /*
 //         * TODO: Implement automatic calling of spawn classes
 //         * Below code generates 5 enemies at random position.
 //         */
 //        int type = 1;
 //        assert (type >= 0 && type <= 2);
 //        spawns = sp.spawnN(5, type);
 //        for (int i = 0; i < spawns.size(); i++) {
 //            shootGame.addUnitToArray(spawns.get(i));
 //        }
         setFocusable(true);
         addMouseListener(a);
         addKeyListener(a);
         addMouseMotionListener(a);
         hideMouse();
         running = true;
         timer = new Timer(20, new ActionListener() { //60 fps
 
             public void actionPerformed(ActionEvent e) {
                 checkUserMovement();
                 if (gameServer != null && gameServer.getNumConnections() != 0) {
                     currentState = gameServer.processGameStates();
                     if (currentState != null) {
                         gameLogic.setGameState(currentState);
                     }
                     logic();
                     gameServer.broadcastGameState(gameLogic.getGameState(playerDeath, paused, running));
                     repaint();
                 } else if (gameClient != null && gameClient.isConnected()) {
                     currentState = gameClient.getCurrentServerState();
                     if (currentState != null) {
                         gameLogic.setGameState(currentState);
                     }
                     logic();
                     repaint();
                     gameClient.sendGameState(currentState);
                 } else {
                     logic();
                     repaint();
                 }
             }
         });
         timer.start();
     }
 
     // Logic methods
     private void logic() {
         // FIXME would be nice to put this somewhere else, but don't know where.
 
         if (running) {
             mouse = a.isMouse();
             gameLogic.pruneArrays(new Dimension(this.getSize()));
             gameLogic.moveEnemies();
             gameLogic.moveProjectiles();
             try {
                 gameLogic.doNaiveCollisionDetection();
             } catch (PlayerDeathException ex) {
                 playerDeath = true;
                 setRun(false);
                 JOptionPane.showMessageDialog(this, "You have become one with the void.");
             }
             background.tick();
         }
     }
 
     // Rendering methods
     private void render(Graphics2D g2) {
 
         if (playerDeath) {
             super.paintComponent(g2);
             g2.setColor(Color.BLACK);
             g2.fill(new Rectangle2D.Double(0, 0, 800, 800));
             g2.setColor(Color.WHITE);
             g2.drawString("GAME OVER!", 350, 275);
         } else {
             if (running) {
                 super.paintComponent(g2);
 //        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-aliasing
 //                RenderingHints.VALUE_ANTIALIAS_ON);
 //        for (Projectile p : shootGame.getProjectileArray()) {
 //            p.draw(g2);
 ////            System.out.println("x= " + p.getX());
 ////            System.out.println("y= " + p.getY());
 //            p.doMove();
 //
 //        }
                 background.draw(g2);
                 drawHealthBar(g2);
                 drawShips(g2);
                 drawProjectiles(g2);
             }
         }
 
     }
 
     private void drawShips(Graphics2D g2) {
         one.setLocation(new Point(player1_x, player1_y));
         ArrayList<Enemy> enemies = gameLogic.getEnemyArray();
         ArrayList<Player> players = gameLogic.getPlayerArray();
         for (Player player : players) {
             player.draw(g2);
             g2.setColor(Color.red);
             g2.draw(gameLogic.getCenteredBox(player.getLocation()));
             g2.setColor(Color.black);
         }
         for (Enemy enemy : enemies) {
             enemy.draw(g2);
             g2.setColor(Color.red);
             g2.draw(gameLogic.getCenteredBox(enemy.getLocation()));
             g2.setColor(Color.black);
         }
 
 //        System.out.println(counter);
         if (counter == 100) { // will spawn a wave as soon as the game starts
                     /*
              * TODO: Implement automatic calling of spawn classes
              * Below code generates 5 enemies at random position.
              */
 
             spawns = sp.spawnRandom(6);
             for (int i = 0; i < spawns.size(); i++) {
                 // FIXME this needs to be done better, although you shouldn't be spawning players in these spawns.
                 gameLogic.addEnemy((Enemy) spawns.get(i));
             }
             //shootGame.getUnitArray().get(shootGame.getUnitArrayLength() - 1).draw(g2);
         }
     }
 
     private void drawProjectiles(Graphics2D g2) {
 
         if (counter == 120 || counter == 200) {
             for (int i = 1; i < gameLogic.getEnemyArray().size() - 1; i++) {
                 shape = new Ellipse2D.Double(gameLogic.getEnemyArray().get(i).getX(), gameLogic.getEnemyArray().get(i).getY() - 15, 5, 5);
                 gameLogic.addProjectileToArray(new ComplexProjectile(gameLogic.getEnemyArray().get(i).getX(),
                         gameLogic.getEnemyArray().get(i).getY() - 15,
                         100, speed, true, shape, color, new StraightPath(StraightPath.Direction.UP),
                         gameLogic.getEnemyArray().get(i).getWeapon().getTexture()));
             }
 
         }
         g2.setColor(Color.BLUE);
         ArrayList<Projectile> projectiles = gameLogic.getProjectileArray();
         for (Projectile projectile : projectiles) {
             projectile.draw(g2);
         }
 
 //        try{
 //        shootGame.getProjectileArray().get(shootGame.getProjectileArray().size()-1).draw(g2);
 //        }catch(Exception e){
 //
 //        }
 //        for (Projectile p : shootGame.getProjectileArray()) {
 //            p.draw(g2);
 //            p.doMove();
 //
 //        }
     }
 
     /**
      * used to control the movement
      */
     private void checkUserMovement() {
         if (!paused && running) {
             if (mouse) {
                 player1_x = a.getMouseX();
                 player1_y = a.getMouseY();
             } else {
                 if (a.isUp() && player1_y > 0) {
                     player1_y -= 5;
                 }
                if (a.isDown() && player1_y < height) {
                     player1_y += 5;
                 }
                 if (a.isLeft() && player1_x > 0) {
                     player1_x -= 5;
                 }
                if (a.isRight() && player1_x < width) {
                     player1_x += 5;
                 }
             }
             if (a.isSpace()) {
                 //testPro = new BasicProjectile(one.getX(), one.getY());
                 //shape = new Ellipse2D.Double(one.getX(), one.getY(), 5, 5);
                 shape = new Rectangle2D.Double(one.getX(), one.getY(), 5, 5);
                 // shape = new Line2D.Double(one.getX(), one.getY(), one.getX(), one.getY()+10);
                 gameLogic.addProjectileToArray(new ComplexProjectile(one.getX(), one.getY() - 15,
                         100, speed, false, shape, color, new StraightPath(StraightPath.Direction.DOWN),
                         one.getWeapon().getTexture()));
             }
             if (a.isEsc()) {
                 switchPanel = true;
                 a.setEscFalse();
             }
             if (a.isPause()) {
                 setRun(false);
                 paused = true;
             }
         } else {
             if (!a.isPause()) {
                 paused = false;
                 setRun(true);
             }
         }
     }
 
     private void hideMouse() {
         ImageIcon invisi = new ImageIcon(new byte[0]);
         Cursor invisible = getToolkit().createCustomCursor(
                 invisi.getImage(), new Point(0, 0), "Hiding");
         this.setCursor(invisible);
     }
 
     private void drawHealthBar(Graphics2D g2) {
         Rectangle2D healthBar = new Rectangle2D.Double(35, 525, one.getHealth(), 20);
         g2.setColor(Color.GREEN);
         g2.fill(healthBar);
     }
 
     /**
      * Method to change the variable run that controls rendering.
      * @param run
      */
     public void setRun(boolean run) {
 //        if (run) {
 //            if (!timer.isRunning()) {
 //                timer.start();
 //            }
 //        } else {
 //            if (timer.isRunning()) {
 //                timer.stop();
 //            }
 //        }
         this.running = run;
     }
 
     /**
      * Checks if the switchpanel variable is true. this indicates that the user
      * has pressed escape and so the frame should switch to the menu. Also stops the
      * game from running.
      * @return
      */
     public boolean getPanelSwitchRequest() {
         boolean req = switchPanel;
         if (req == true) {
             setRun(false);
             switchPanel = false;
             return true;
         }
         return false;
     }
 }
