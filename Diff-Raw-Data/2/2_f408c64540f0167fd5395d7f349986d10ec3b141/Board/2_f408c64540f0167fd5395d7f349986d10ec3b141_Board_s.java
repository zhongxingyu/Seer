 import java.awt.Color;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 
 import java.lang.Math;
 
 import java.util.ArrayList;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.Timer;
 
 
 public class Board extends JPanel implements ActionListener {
 
     private Timer timer;
     private Player player;
     private ArrayList buildings;
     private boolean ingame;
     private String endgameMessage = "";
     private int B_WIDTH;
     private int B_HEIGHT;
     private static Board instance;
     private double points = 0;
     private double speed;
     private boolean doPaint;
 
     public Board() {
         addKeyListener(new TAdapter());
         setFocusable(true);
         setBackground(Color.DARK_GRAY);
         setDoubleBuffered(true);
         ingame = true;
 
         setSize(Collision.width, Collision.height);
 
         player = new Player();
 
         initBuildings();
 
         timer = new Timer(5, this);
         timer.start();
         this.speed = 1;
         doPaint = true;
         Board.instance = this;
 		
     }
 
     public static Board getInstance() {
         return Board.instance;
     }
 
     public void addNotify() {
         super.addNotify();
         B_WIDTH = getWidth();
         B_HEIGHT = getHeight();   
     }
 
     public void setEndgameMessage(String message) {
         endgameMessage = message;
     }
 
     public double getSpeed() {
         return speed;
     }
 
     public void setSpeed(double speed) {
 	   if(speed < 0) return;
 	   this.speed = speed;
     }
 
     public void initBuildings() {
         this.buildings = new ArrayList();
         this.buildings.add(new Building(500, 500, 0, 300));
         this.buildings.add(new Building(500, 500, 600, 400));
     }
 
     public void addBuilding() {
         int buildingWidth = getRandom(300,1000);
         buildingWidth += 50 - (buildingWidth % 50);
         Building b = new Building(buildingWidth, 500, getWidth()+getRandom(50,250), getRandom(290,420));
         b.spawnObstacles();
 		b.spawnBirds();
         this.buildings.add(b);
     }
 
 
     public void paint(Graphics g) {
         super.paint(g);
 		
 		if(!isFocusOwner()) {
             requestFocus();
         }   
         Font small = new Font("VT323-Regular", Font.PLAIN, 22);
         FontMetrics metr = this.getFontMetrics(small);
 
         g.setColor(Color.white);
         g.setFont(small);
         if (ingame) {
 
             Graphics2D g2d = (Graphics2D)g;
             
             g2d.drawImage(player.getImage(), (int)player.getX(), (int)player.getY(), this);
 
             g2d.setColor(Color.WHITE);
 
             for(int i = 0; i<buildings.size();++i) {
                 Building b = (Building) buildings.get(i);
                 b.paintComplete(g2d);
             }
 
             //g2d.drawString("Position: " + (int)player.getX() + " - " + (int)player.getY() + " - DY: " + (int)player.getDY() + " - DX: " + (int)player.getDX() + " - Speed: " + speed, 5, 15);
             g2d.drawString("Points: " + (int)points, getWidth()-150, 30);
             
         } else {
             String msg = "Game Over - You scored " + (int)points + " Points - " + endgameMessage;
             
             g.drawString(msg, (B_WIDTH - metr.stringWidth(msg)) / 2, B_HEIGHT / 2);
             doPaint = false;
         }
         Toolkit.getDefaultToolkit().sync();
         g.dispose();        
     }
 
 
     public void actionPerformed(ActionEvent e) {
 		player.move();
         for (int i = 0; i < buildings.size(); ++i) {
             Building b = (Building) buildings.get(i);
             b.move();
         }
 
         Building b = (Building) buildings.get(buildings.size()-1);
         // Add new building if last one completely visible
         if(b.getX() + b.getWidth() < getWidth()) {
             addBuilding();
         }
 
         points += 0.1 * speed;
 
         // Increase speed
         if(speed < 4)
             setSpeed(getSpeed()+0.0010);
 
         checkCollisions();
         if(doPaint)
             repaint();  
     }
 
     public void checkCollisions() {
 
         Rectangle r3 = player.getBounds();
 
         for (int i = 0; i < buildings.size(); ++i) {
             Building b = (Building) buildings.get(i);
             if(player.intersects(b.getItem())) {
                if(player.getX() < b.getX() && (player.getY() + player.getHeight() - 10) > b.getY())
                 {
                     endgameMessage = "You hit a building.";
                     stopGame();
                 }
                 else
                 {
                     player.setDY(0.0);
 		            player.setMidair(false);
                     player.setY(b.getY() - player.getHeight() + 1);
                 }
 	       }
             // Check obstacles
             ArrayList obstacles = b.getObstacles();
             for(int j = 0; j < obstacles.size(); ++j) {
                 Obstacle o = (Obstacle) obstacles.get(j);
                 if(o.isActive()) {
                     if(player.intersects(o.getItem()) && player.getX() + 10 >= o.getX()) {
                         o.deactivate();
                         o.attachWings();
                         setSpeed(getSpeed()-1);
                     }
                 }
             }
 
 			// Birds !!!!!!!!
 			if(player.intersects(b.getHitbox())) {
 				ArrayList bi = b.getBirds();
 				for(int j=0;j < bi.size();++j) {
 					Bird bir = (Bird) bi.get(j);
 					
 					bir.setProperties(Bird.FLYING);
 				}
 			}
         }
     }
 
     public void stopGame() {
         timer.stop();
         ingame = false;
     }
 
     public Player getPlayer() {
         return player;
     }
 
     public static int getRandom(int from, int to) {
         int diff = to - from;
         double random = Math.random();
         return from + (int)(diff * random);
     }
 
 
     private class TAdapter extends KeyAdapter {
 
         public void keyReleased(KeyEvent e) {
             player.keyReleased(e);
             if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                 stopGame();
                 Collision.getInstance().restartGame();
             }
         }
 
         public void keyPressed(KeyEvent e) {
             player.keyPressed(e);
         }
     }
 }
