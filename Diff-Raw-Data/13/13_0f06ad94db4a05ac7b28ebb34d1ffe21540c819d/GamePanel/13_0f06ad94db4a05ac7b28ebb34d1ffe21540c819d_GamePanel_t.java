 package GUIComponents;
 
 import Controls.Controls;
 import Game.Game;
 import Projectile.BasicProjectile;
 import Projectile.Projectile;
 import Spawn.Spawn;
 import Unit.Enemy;
 import Unit.Player;
 import Weapon.BasicWeapon;
 import java.awt.Graphics;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.RenderingHints;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.ImageIcon;
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
     Game shootGame;
     JPanel gPanel;
     Color bgColor = Color.BLACK;
     Player one;
     Spawn sp;
     Enemy staticEnemy;
     BasicWeapon base;
     int player1_x = 500;
     int player1_y = 500;
     Controls a;
     Boolean mouse; //if mouse is being used or not
     int width;
     int height;
     Timer timer;
 
     public GamePanel() {
         initialize();
     }
 
     /**
      * Most important method here. Draws all objects that need to be drawn.
      * @param g
      */
     @Override
     protected void paintComponent(Graphics g) {
         logic();
         render(g);
     }
 
     // Initialization
     private void initialize() {
         timer = new Timer(33, new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 repaint();
             }
         });
         timer.start();
 
         sp = new Spawn();
         base = new BasicWeapon();
         shootGame = new Game();
         one = new Player(200, 200, base, 200, player1_x, player1_y, Color.WHITE);
         staticEnemy = sp.newSpawn();
         a = new Controls();
         gPanel = new JPanel();
 
         setBackground(bgColor);
         shootGame.pruneArrays(this.getSize());
         height = this.getHeight();
         width = this.getWidth();
 
         one.setLocation(new Point(player1_x, player1_y));
         mouse = a.isMouse();
         shootGame.addUnitToArray(one);
         //The way things are spawned needs to be changed since currently adding it to
         //the end of the unit array breaks the get of the players unit
         // TODO: Create automatic call (timer) for new spawns and add to array.
         //shootGame.addUnitToArray(staticEnemy);  // Temporary workaround
 
         setFocusable(true);
         addMouseListener(a);
         addKeyListener(a);
         addMouseMotionListener(a);
 
         hideMouse();
         mouse = a.isMouse();
     }
 
     // Logic methods
     private void logic() {
         movement();
     }
 
     // Rendering methods
     private void render(Graphics g) {
         super.paintComponent(g);
         Graphics2D g2 = (Graphics2D) g;
 
         drawShips(g2);
         drawProjectiles(g2);
 
     }
 
     private void drawShips(Graphics g2) {
         shootGame.removeUnitfromArray(0);
         one.setLocation(new Point(player1_x, player1_y));
         shootGame.addUnitToArray(one);
         shootGame.getUnitArray().get(shootGame.getUnitArrayLength() - 1).draw(g2);
     }
 
     private void drawProjectiles(Graphics2D g2) {
        g2.setColor(Color.WHITE);
         for (Projectile p : shootGame.getProjectileArray()) {
             p.doMove();
             p.draw(g2);
         }
     }
 
     /**
      * used to control the movement
      */
     private void movement() {
         if (mouse) {
             player1_x = a.getMouseX();
             player1_y = a.getMouseY();
         } else {
             if (a.isUp() && player1_y > 0) {
                 player1_y -= 1;
             }
            if (a.isDown()){// && player1_y < height) {
                 player1_y += 1;
             }
             if (a.isLeft() && player1_x > 0) {
                 player1_x -= 1;
             }
            if (a.isRight() ){//&& player1_x < width) {
                 player1_x += 1;
             }
             if (a.isSpace()) {
                 //testPro = new BasicProjectile(one.getX(), one.getY());
                 shootGame.addProjectileToArray(new BasicProjectile(one.getX(), one.getY()));
             }
 
         }
     }
 
     private void hideMouse() {
         ImageIcon invisi = new ImageIcon(new byte[0]);
         Cursor invisible = getToolkit().createCustomCursor(
                 invisi.getImage(), new Point(0, 0), "Hiding");
         this.setCursor(invisible);
     }
 }
