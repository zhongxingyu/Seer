 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.Rectangle2D;
 
 import javax.swing.JComponent;
 
 
 public class VehicleBus extends JComponent implements Vehicle
 {
	private static final String vehicleName = "Bus";
 	private static final int VEHICLE_WIDTH = 81;
 	private static final int VEHICLE_HEIGHT = 40;
 	
 	private int speed;
 	
 	public VehicleBus(int _speed)
 	{
 		speed = _speed;
 		
 		// Set size
 		setSize(VEHICLE_WIDTH, VEHICLE_HEIGHT);
 	}
 	
 	public String getName()
 	{
 		return vehicleName;
 	}
 
 	public void initVehicle()
 	{
 		setLocation(32*3 - VEHICLE_WIDTH, (32*3 - VEHICLE_HEIGHT) / 2);
 	}
 	
 	public void paintComponent(Graphics g)
 	{
 		Graphics2D g3 = (Graphics2D) g;
 
 		Color body = new Color(0, 100, 220);
 		Color windows = new Color(200, 200, 200);
 		Color door = new Color(0, 120, 220);
 		
 		//body
 		Rectangle2D.Float c1 = new Rectangle2D.Float(0, 0, 80, 30);
 		g3.setColor(body);
 		g3.fill(c1);
 		g3.draw(c1);
 		//tire1
 		Ellipse2D.Float c2 = new Ellipse2D.Float(10, 30, 10, 10);
 		g3.setColor(Color.BLACK);
 		g3.fill(c2);
 		g3.draw(c2);
 		//tire2
 		Ellipse2D.Float c3 = new Ellipse2D.Float(60, 30, 10, 10);
 		g3.setColor(Color.BLACK);
 		g3.fill(c3);
 		g3.draw(c3);
 		//windows
 		Rectangle2D.Float c4 = new Rectangle2D.Float(5, 2, 15, 15);
 		g3.setColor(windows);
 		g3.fill(c4);
 		g3.draw(c4);
 		Rectangle2D.Float c5 = new Rectangle2D.Float(20, 2, 15, 15);
 		g3.fill(c5);
 		g3.draw(c5);
 		Rectangle2D.Float c6 = new Rectangle2D.Float(53, 2, 15, 15);
 		g3.fill(c6);
 		g3.draw(c6);
 		//door
 		Rectangle2D.Float c7 = new Rectangle2D.Float(36, 2, 15, 27);
 		g3.setColor(door);
 		g3.fill(c7);
 		g3.draw(c7);
 		//HeadLights
 		Rectangle2D.Float c8 = new Rectangle2D.Float(0, 16, 2, 7);
 		g3.setColor(Color.RED);
 		g3.fill(c8);
 		g3.draw(c8);
 		Rectangle2D.Float c9 = new Rectangle2D.Float(0, 5, 2, 4);
 		g3.setColor(Color.WHITE);
 		g3.fill(c9);
 		g3.draw(c9);
 		//FrontWindow
 		Rectangle2D.Float c10 = new Rectangle2D.Float(76, 0, 4, 17);
 		g3.setColor(windows);
 		g3.fill(c10);
 		g3.draw(c10);
 		//FrontLight
 		Rectangle2D.Float c11 = new Rectangle2D.Float(76, 25, 4, 5);
 		g3.setColor(Color.WHITE);
 		g3.fill(c11);
 		g3.draw(c11);
 		g3.setColor(Color.BLACK);
 		//BodyLine
 		Rectangle2D.Float c12 = new Rectangle2D.Float(0, 0, 80, 30);
 		g3.draw(c12);
 		//BusName
 		g3.setFont(g.getFont().deriveFont(Font.BOLD, 9f));
 		g3.drawString("750A", 8, 27);
 	}
 	
 	public void move(int direction)
 	{
 		this.setLocation
 		(
 			this.getLocation().x + dir[direction].x*speed,
 			this.getLocation().y + dir[direction].y*speed
 		);
 		
 		// Afterimage is remained, so repaint that part.
 		this.getRootPane().repaint();
 	}
 	
 	public Rectangle getBounds()
 	{
 		return super.getBounds();
 	}
 }
