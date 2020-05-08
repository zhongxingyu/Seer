 package se2.cleaningRobot.simulation;
 
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 
 import java.awt.*;
 import java.awt.image.*;
 import java.lang.*;
 import java.util.ArrayList;
 import java.util.Observable;
 import java.util.Observer;
 
 import se2.cleaningRobot.control.*;
 
 /**
  * @author SE2 Gruppe 8
  */
 public class Scene extends Canvas implements Observer {	
 
 	private ArrayList<CRobot> robotList = new ArrayList<CRobot>();	
  private BufferedImage robot; // represents image of a cleaningRobot
   private ArrayList<Obstacle> obstacleList = new ArrayList<Obstacle>();
 
   public Scene() {
     add(new CRobot(300, 300, 1000, Color.BLUE));
     add(new Obstacle(100, 100, 50, 50, Color.RED));

    robot = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
   }
 	
   @Override
 	public void paint(Graphics g) {
     // provide a better graphics class
     Graphics2D g2 = (Graphics2D) g;
 
     // get draw area of the scene
     Rectangle bounds = g2.getClipBounds();
 
     // set background color
     g2.setColor(Color.WHITE);
     g2.fillRect(0, 0, bounds.width, bounds.height);
 
     // draw scene objects
     drawObstacles(g2);
     drawRobots(g2);
 	}	
 
   @Override
   public void update(Observable o, Object arg) {
     System.out.println("UPDATE Scene");
     repaint();
   }
 
 	public void add(CRobot robot) {
 		robotList.add(robot);
 	}	
 
   public void add(Obstacle obstacle) {
     obstacleList.add(obstacle);
   }
 
   private void drawObstacles(Graphics2D g2) {
     for (int i=0; i < obstacleList.size(); i++) {
       Obstacle ob = obstacleList.get(i);
       g2.setColor(ob.color);
       g2.fillRect(ob.xPos, ob.yPos, ob.width, ob.height);
       //g.drawImage(robot, 500, 500, null);
     }
   }
 
   private void drawRobots(Graphics2D g2) {
 		for (int i=0; i < robotList.size(); i++) {
 			CRobot r = robotList.get(i);
 			g2.setColor(r.color);
			//g.fillArc(r.xPos , r.yPos, 50, 50, 0, 360);
      g2.drawImage(robot, 300, 300, null);
      System.out.println("color: " + robot.getRGB(10,10));
 		}
   }
 }
