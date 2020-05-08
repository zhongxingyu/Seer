 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.geom.Point2D;
 import java.awt.image.BufferStrategy;
 import java.util.ArrayList;
 
 public class Game extends JFrame implements KeyListener, MouseListener {
 
 
     private Ship ship = null;
     private Field sea = null;
 
     private Enemy enemy = null;
 
     static ArrayList<Bullet> Bullets = new ArrayList<Bullet>();
 
     private boolean up_pressed = false;
     private boolean down_pressed = false;
     private boolean left_pressed = false;
     private boolean right_pressed = false;
     private boolean fire_pressed = false;
     private boolean a_pressed = false;
     private boolean d_pressed = false;
 
 
     Game() {
         super("Sea Battle");
         setSize(1224, 768);
         setLocation(300, 120);
         setVisible(true);
         setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
         createBufferStrategy(2);
 
         this.setFocusable(true);
         this.addKeyListener(this);
         this.addMouseListener(this);
 
         sea = new Field(800, 500);
         ship = new Ship(new Vec(500, 350), Ship.PLAYER1);
         enemy = new Enemy(sea.spawnPoint1);
     }
 
     public void update() {
         handle_events();
         ship.update();
         enemy.update();
         moveBullets();
 
         enemy.addShip(sea.spawnPoint1);
     }
 
     public void moveBullets() {
         for (int j = 0; j < Bullets.size(); j++) {
             Bullets.get(j).move();
             if (Bullets.get(j).getPos().OutOfBounds(sea.borderX, sea.borderY, sea.sizeX + sea.borderX, sea.sizeY + sea.borderY))
                 Bullets.remove(j);
         }
     }
 
     private void resolveCollisions() {
         for (int i = 0; i < enemy.Ships.size(); i++)
             if (ship.Collision(enemy.Ships.get(i)))
                 ship.collision_detected = true;
         ship.moveback();
     }
 
 
     private void render_statistics(Graphics2D g) {
 
         g.setColor(Color.BLACK);
         g.drawRect(900, 100, 250, 500);
         //g.drawString(String.valueOf(enemy.Ships.get(0).is_destroyed), 910, 150);
         g.drawString("Your ship position", 980, 150);
         //g.drawString(enemy.Ships.get(0).getPosition().toString(), 910, 180);
         g.drawString(ship.getPosition().toString(), 1000, 180);
         g.drawString(ship.ship_turret.fpos.toString(), 1000, 210);
         // g.drawString(getMousePosition().toString(),310,240);
 
 
         // g.drawString(Bullets.get(0).getPos().toString(), 950, 180);
 
     }
 
     public void display() {
         BufferStrategy bf = this.getBufferStrategy();
         Graphics2D g = null;
 
         try {
             g = (Graphics2D) bf.getDrawGraphics();
             g.clearRect(0, 0, 1224, 768);
             sea.render(g);
             ship.render(g);
             enemy.render(g);
             render_statistics(g);
 
             for (int j = 0; j < Bullets.size(); j++) {
                 Bullets.get(j).render(g);
             }
 
         } finally {
             if (g != null)
                 g.dispose();
         }
         bf.show();
         Toolkit.getDefaultToolkit().sync();
 
     }
 
     private void handle_events() {
         if (!ship.is_destroyed && !ship.collision_detected) {
             if (up_pressed)
                 ship.moveStraight();
             if (left_pressed)
                 ship.turnLeft();
             if (right_pressed)
                 ship.turnRight();
             if (fire_pressed)
                 ship.ship_turret.fire();
             if (a_pressed)
                 ship.ship_turret.turnLeft();
             if (d_pressed)
                 ship.ship_turret.turnRight();
         }
 
     }
 
 
     @Override
     public void keyTyped(KeyEvent e) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void keyPressed(KeyEvent e) {
         switch (e.getKeyCode()) {
             case KeyEvent.VK_UP:
                 up_pressed = true;
                 break;
             case KeyEvent.VK_LEFT:
                 left_pressed = true;
                 break;
             case KeyEvent.VK_RIGHT:
                 right_pressed = true;
                 break;
             case KeyEvent.VK_F:
                 fire_pressed = true;
                 break;
             case KeyEvent.VK_A:
                 a_pressed = true;
                 break;
             case KeyEvent.VK_D:
                 d_pressed = true;
                 break;
             case KeyEvent.VK_ESCAPE:
                 System.exit(0);
 
         }
     }
 
     @Override
     public void keyReleased(KeyEvent e) {
         switch (e.getKeyCode()) {
             case KeyEvent.VK_UP:
                 up_pressed = false;
                 break;
             case KeyEvent.VK_LEFT:
                 left_pressed = false;
                 break;
             case KeyEvent.VK_RIGHT:
                 right_pressed = false;
                 break;
             case KeyEvent.VK_F:
                 fire_pressed = false;
                 break;
             case KeyEvent.VK_A:
                 a_pressed = false;
                 break;
             case KeyEvent.VK_D:
                 d_pressed = false;
                 break;
         }
 
     }
 
     @Override
     public void mouseClicked(MouseEvent e) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void mousePressed(MouseEvent e) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void mouseReleased(MouseEvent e) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void mouseEntered(MouseEvent e) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void mouseExited(MouseEvent e) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 }
 
 
 
 
