 package gui.graphical;
 
 // Default Libraries
 import java.io.IOException;
 import java.util.WeakHashMap;
 
 // Graphical Libraries (AWT)
 import java.awt.*;
 import java.awt.TexturePaint;
 import java.awt.image.BufferedImage;
 
 // Graphical Libraries (Swing)
 import javax.swing.*;
 import javax.swing.border.*;
 import javax.imageio.ImageIO;
 import javax.swing.SwingUtilities;
 
 // Libraries
 import arena.Map;
 import parameters.*;
 import arena.Robot;
 import players.Player;
 import scenario.Scenario;
 import stackable.item.Item;
 
 // Import links
 import static parameters.Game.*;
 
 /**
  * <b>Graphical - Panel</b><br>
  * Creates the main panel to exhibit 
  * the map.
  * @see Graphical
  * @see arena.Map
  * @see arena.World
  * 
  * @author Renato Cordeiro Ferreira
  * @author Vinicius Silva
  */
 class Panel extends JPanel 
 {
     // Map made with cells
     private Cell[][] cell = new Cell[MAP_SIZE][MAP_SIZE];
     
     // Local variables
     private Map map;
     private Insets insets;
     private WeakHashMap<Robot,JRobot> robots = new WeakHashMap<Robot,JRobot>();
     
     /**
      * Create a Panel with dimensions width x height,
      * containing MAP_SIZE² hexagons (built from a map).
      * @see Cell
      * @see Graphical
      *
      * @param R      radius
      * @param width  Desired width of the screen
      * @param height Desired height of the screen
      * @param y0     Desired vertical shift
      * @param map    Map over which the panel will
      *               create the GUI hexagons
      */
     Panel(int R, int width, int height, int y0, Map map) 
     {
         this.map = map;
            
         // Preferences
         this.insets = this.getInsets();
         this.setLayout(null);
         this.setBackground(Color.black);
         this.setPreferredSize(new Dimension(width, height));
         
         // Put images in the screen
         int Dx = (int) ( 2*R * Math.sin(Math.PI/3) ); 
         int Dy = 3 * R/2 +1;
        
         int Δ  = 0;
         for (int i = 0; i < MAP_SIZE; i++)
             for (int j = 0; j < MAP_SIZE; j++) 
             {
                 cell[i][j] = new Cell(
                     Δ + R + i*Dx, R + j*Dy + y0, R, map.map[j][i]
                 ); 
                 Δ = (Δ == 0) ? Dx/2 : 0;
             }
     }
     
     /**
      * Paint hexagons on the screen.<br>
      * At each step, repaint the cells
      * as they need changes.
      * @param g Graphic object with all context 
      *          needed to render the image
      */
     protected void paintComponent(Graphics g) 
     { 
         super.paintComponent(g);
         Graphics2D g2d = (Graphics2D) g;
         
        for (java.util.Map.Entry<Robot, JRobot> entry : robots.entrySet())
            entry.getValue().finalize();
        
         // First, draw all the background
         for (int i = 0; i < MAP_SIZE; i++) 
             for (int j = 0; j < MAP_SIZE; j++)
                 cell[i][j].draw(g); 
                 
         // After, draw items and scenarios
         for (int i = 0; i < MAP_SIZE; i++) 
             for (int j = 0; j < MAP_SIZE; j++)
             {
                 Cell hex = cell[j][i];
                 int x = hex.x, y = hex.y;
                 
                 // Print items
                 if(hex.terrain.getItem() != null)
                 {
                     Images item = Images.valueOf(
                         hex.terrain.getItem().name()
                     );
                     g2d.drawImage(
                         item.img(), x-item.dx(), y-item.dy(), null
                     );
                 }
                 
                 // Print scenarios
                 Scenario s = hex.terrain.getScenario();
                 if(s != null)
                 {
                
                     Images scen = Images.valueOf(s.name(), s.getTeam());
                    
                     g2d.drawImage(
                         scen.img(), x-scen.dx(), y-scen.dy(), null
                     );
                
                     /* TODO: Find a way to throw away all the unused
                      * robots */
                     if(s instanceof Robot)
                     {
                         Robot r = (Robot) s;
                         if(!this.robots.containsKey(r)) 
                             this.robots.put(r, new JRobot(r));
                         
                         JRobot jr = robots.get(r);
                         jr.update(x-scen.dx(), y-scen.dy());
                        jr.add();
                     }
                 }
                 //System.out.println("chegou");
                 validate();
                 JLabel l = new JLabel("Oi", JLabel.CENTER);
                 l.setText("lalalalal");
                 validate();
             }
             
     }
     
     /**
      * <b>JRobot - Robot with more than images</b><br>
      * Print a robot exhibiting a status bar and other
      * useful info for the player.
      */
     private class JRobot 
     {
         private Robot robot;
         private Dimension size = new Dimension(30,5); // Bar size
         
         private int maxHP;
         private int maxPower;
         
         // Progress Bars
         private JProgressBar hp;
         private JProgressBar power;
         
         /**
          * Default Constructor.
          * @param robot Robot to be stored
          */
         JRobot(Robot robot)
         {
             // Stores parameters in attributes
             this.robot = robot;
 
             // Creates and sets HP bar
             this.maxHP = robot.getMaxHP       ();
             this.hp = new JProgressBar        ();
             this.hp.setMaximum                (this.maxHP    );
             this.hp.setPreferredSize          (this.size     );
             this.hp.setForeground             (Color.BLUE    );
             
             // Creates and sets Power bar
             this.maxPower = robot.getMaxPower ();
             this.power = new JProgressBar     ();
             this.power.setMaximum             (this.maxPower );
             this.power.setPreferredSize       (this.size     );
             this.hp.setForeground             (Color.GREEN   );
             
         }
         
         /**
          * Update infos exhibited by a robot.
          * @param x0 Initial horizontal position to paint 
          *           info bars
          * @param y0 Initial vertical position to paint 
          *           info bars
          */
         void update(int x0, int y0)
         {
             int hp    = this.robot.getHP    ();
             int power = this.robot.getPower ();
             
             // Update Color Scheme
             this.updateColorScheme(this.hp, this.maxHP);
             
             // Configure and paint hp bar
             this.hp.setBounds    (x0, y0-15, size.width, size.height);
             this.hp.setValue     (hp);
             
             // Configure and paint power bar
             this.power.setBounds (x0, y0-10, size.width, size.height);
             this.power.setValue  (power);
         }
         
         /**
          * Updates the color of a given bar.<br>
          * If greater than 2/3, paint in GREEN. If lower
          * than 1/3, paint RED. Otherwise, use YELLOW.
          * @param pb  JProgressBar
          * @param max Maximum value to be used as parameter
          *            to the tresholds of each color region
          */
         private void updateColorScheme(JProgressBar pb, int max)
         {
             double per = max * pb.getPercentComplete();
             Color c = Color.YELLOW;
             if(per > 2.0/3 * max) c = Color.GREEN;
             if(per < 1.0/3 * max) c = Color.RED;
             pb.setForeground(c);
         }
        
        protected void add()
        {
            // Add bars in the Panel
            Panel.super.add(this.hp);
            Panel.super.add(this.power);

        }
        
        protected void finalize()
        {
            Panel.super.remove(this.hp);
            Panel.super.remove(this.power);
        }
     }
 }
