 
 
 package ui;
 
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.util.Timer;
 import java.util.TimerTask;
 import objects.Gem;
 import objects.Planet;
 import objects.Spaceship;
 import objects.Sun;
 
 public class GameCanvas extends Canvas  implements Runnable{
 
     private Sun sun;
     private Planet[] planets;
     private Gem[] gems;
     private Spaceship sp;
     private Timer timer;
 
     public GameCanvas() { }
     
     public void initialize() {
         sun = new Sun(getHeight() / 2, getWidth() / 2, 0.8, 40, Color.orange);
 
         planets = new Planet[6];
         for (int i = 0; i < 6; i++) {
             int radius = i * 50 + 80;
             double angle = Math.random() * 2 * Math.PI;
             planets[i] = new Planet((int)(radius * Math.sin(angle)), (int)(radius * Math.cos(angle)), 
                     0.4, (int)(Math.random() * 15) + 5, new Color((float)Math.random(), 
                     (float)Math.random(), (float)Math.random()), (int)(Math.random() * 5),
                     (int)Math.pow(-1, (int)(Math.random() * 2)), new Point(sun.getPositionX(), sun.getPositionY()));
         }
         
         gems = new Gem[5];
         for(int j = 0; j < 5; j++) {
             gems[j] = new Gem((int) (Math.random() * 600 + 100), (int) (Math.random() * 600 + 100), 
                     Color.CYAN);
         }
         
        sp = new Spaceship(50, getHeight() - 100, 1, Color.LIGHT_GRAY);
         timer = new Timer();
     }
 
     @Override
     public void paint(Graphics g) {
         g.setColor(Color.black);
         g.fillRect(0, 0, getWidth(), getHeight());
 
         g.setColor(Color.white);
         for (int i = 0; i < 100; i++) {
             int size = (int)(Math.random() * 4);
             g.fillOval((int)(Math.random() * getWidth()), (int)(Math.random() * getHeight()), size, size);
         }
 
         g.setColor(sun.getColor());
         g.fillOval(sun.getPositionX() - sun.getRadius(), sun.getPositionY() - sun.getRadius(),
                 sun.getRadius() * 2, sun.getRadius() * 2);
 
         for (Planet planet : planets) {
             g.setColor(planet.getColor());
             g.fillOval(planet.getPositionX() - planet.getRadius(), planet.getPositionY() - planet.getRadius(),
                     planet.getRadius() * 2, planet.getRadius() * 2);
         }
         
         for (Gem gem : gems) {
             g.setColor(gem.getColor());
             g.fillPolygon(gem.getPolygon());
         }
         
         g.setColor(sp.getColor());
         g.fillPolygon(sp.getPolygon());
         
     }
     
     @Override
     public void run() {
         for (Planet planet : planets) {
             planet.run();
         }
         timer.scheduleAtFixedRate(
                 new TimerTask() {
                     public void run() {
                         repaint();
                     }
                 }, 0, 20);
     }
 
 }
