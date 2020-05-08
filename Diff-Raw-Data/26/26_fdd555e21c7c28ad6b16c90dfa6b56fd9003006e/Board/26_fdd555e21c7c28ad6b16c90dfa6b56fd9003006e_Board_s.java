 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.Shape;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.geom.Rectangle2D;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.Random;
 
 import javax.swing.JPanel;
 import javax.swing.text.html.HTMLDocument.Iterator;
 
 
 public class Board extends JPanel implements Runnable, MouseListener, MouseMotionListener{
 	
 	private Thread animator;
 	private int dragX;
 	private int dragY;
 	private boolean canDrag = false;
 	private Building buildingSelection;
 	private LinkedList<Building> buildings = new LinkedList<Building>();
 	private LinkedList<Path> paths = new LinkedList<Path>();
 	private LinkedList<Visitor> people = new LinkedList<Visitor>();
 	private Legenda legenda;
 	private int destinationX;
 	private int destinationY;
 	private boolean moved;
 	private Interface iface;
 	private Map bitmap = new Map();
 	
 	//Constructor
 	public Board(Interface iface)
 	{
 		this.iface = iface;
 		initSimulator();
 		setDoubleBuffered(true);
 		addMouseListener(this);
 		addMouseMotionListener(this);
 		legenda = new Legenda();
 	}
 	
 	
 	//Initialize method
 	public void initSimulator()
 	{
 		addRoad(48,48);
 		addEHBO(100, 100);
 		addEHBO(50, 100);
 		addSnackBar(200, 300);
 		addSnackBar(400, 300);
 		addStage(300, 200);
 		addStage(300, 80);
 		for(int i = 0; i <= 100; i+=4)
 		{
 			for(int t = 0; t <=100; t+=4)
 			{
 		    addVisitor(t, i);
 			}
 			}
 	    for(Person person: people)
 	    {
 			Random random = new Random();
 			if(random.nextInt()%2 == 0)
 			{
 				person.setAppearance(2);
 			}
 			else
 				person.setAppearance(3);
 	    }
 		animator = new Thread(this, "1");
 		animator.start();
 	}
 	
 	
 	// List adds
 	public void addRoad(int x, int y)
 	{
 		paths.add(new Road(x, y));
 	}
 	
 	public void addVisitor(int x, int y)
 	{
 		people.add(new Visitor(x, y));
 	}
 	
 	public void addEHBO(int x, int y)
 	{
 		buildings.add(new EHBO(x, y));
 	}
 	
 	public void addSnackBar(int x, int y)
 	{
 		buildings.add(new Snackbar(x, y));
 	}
 	
 	public void addStage(int x, int y)
 	{
 		buildings.add(new StagePicture(x, y));
 	}
 	
 	
 	//List removes
 	public void removeRoad(int x, int y)
 	{
 		paths.remove(new Road(x, y));
 	}
 	
 	public void removeVisitor(int x, int y)
 	{
 		people.remove(new Visitor(x, y));
 	}
 	
 	public void removeEHBO(int x, int y)
 	{
 		buildings.remove(new EHBO(x, y));
 	}
 	
 	public void removeSnackBar(int x, int y)
 	{
 		buildings.remove(new Snackbar(x, y));
 	}
 	
 	public void removeStage(int x, int y)
 	{
 		buildings.remove(new StagePicture(x, y));
 	}
 	
 	
 	//Paint method
 	public void paint(Graphics g)
 	{
 		super.paint(g);
 		Graphics2D g2 = (Graphics2D) g;
 		checkSelection();
 		g2.setColor(Color.green);
 		g2.fillRect(0, 0, 720, 480);
 		g2.setColor(Color.black);
 		drawRaster(g2);
 		for(Person person: people)
 		{
 			drawPerson(person, g2);
 		}
 		for(Building building: buildings)
 		{
 			drawBuilding(building, g2);
 		}
 		g2.dispose();
 	}
 	
 	
 	//Draw methods
 	public void drawRaster(Graphics2D g2)
 	{
 		int x = 0;
 	    int y = 0;
 	    for(int i = 0; i < 20; i++)
 	    {
 	    	 y = i;
 	    	 Shape s = new Rectangle(x * 24, y * 24, 24, 24);
 	    	 g2.draw(s);
 	     
 	    for(int t = 0; t < 30; t++)
 	    {
 	    	 x = t;
 	    	 Shape z = new Rectangle(x * 24, y * 24, 24, 24);
 	    	 g2.draw(z);
 	    }
 	    }
 	}
 	
 	public void drawPath(Path path, Graphics2D g2)
 	{
 		g2.drawImage(path.getImageIcon().getImage(), path.getX(), path.getY(), this);
 	}
 	
 	public void drawBuilding(Building building, Graphics2D g2)
 	{
 		g2.drawImage(building.getImageIcon().getImage(), building.getX(), building.getY(), this);
 	}
 	
 	public void drawPerson(Person person, Graphics2D g2)
 	{
 		if(person.getAppearance() == 2)
 		{
 			g2.setColor(Color.blue);
 		}
 		else
 			g2.setColor(Color.pink);
 		
 		g2.fillRect(person.getX(), person.getY(), 4, 4);
 		g2.setColor(null);
 	}
 	
 	
 	//Run method
 	public void run() {		
 		while(true)
 		{
 		for(Visitor visitor: people)
 		{
 			movePerson(visitor);
 		}
 		repaint();
 		
 		try {
 			Thread.sleep(100);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		}
 	}
 	
 	//Object checks
 	public void checkSelection()
 	{
 		if(legenda.RightClicked())
 		{
 		if(legenda.getSelection() == "Selection: EHBO")
 		{
 			addEHBO(0, 0);
 		}
 		else if(legenda.getSelection() == "Selection: SnackBar")
 		{
 			addSnackBar(0, 0);
 		}
 		else if(legenda.getSelection() == "Selection: Stage")
 		{
 			addStage(0, 0);
 		}
 		}		
 	}
 	
 	public void checkDragBuilding(Building building, int x, int y)
 	{
 	       if (x >= building.getX() && x <= (building.getX() + building.getWidth())
 	                && y >= building.getY() && y <= (building.getY() + building.getHeight())) {
 	            canDrag = true;
 	            dragX = x - building.getX();  
 	            dragY = y - building.getY(); 
 	            buildingSelection = building;
 	        }
 	}
 	
 	public void checkDragPath(Path path, int x, int y)
 	{
 	       if (x >= path.getX() && x <= (path.getX() + path.getWidth())
 	                && y >= path.getY() && y <= (path.getY() + path.getHeight())) {
 	            canDrag = true;
 	            dragX = x - path.getX();  
 	            dragY = y - path.getY(); 
 	        }
 	}
 	
 	
 	public boolean checkPeople(Visitor visitor)
 	{
 		boolean collision = false;
 		for(Person p: people)
 		{
 			if(p.getX() == visitor.getX() && p.getY() == visitor.getY())
 			{
 				collision = true;
 				break;
 			}
 		}
 		return collision;
 	}
 	
 	public void checkOccupationBuilding(Building building)
 	{
 
 	}
 	
 	
 	
 	//Movement methods
 	public void moveDragBuilding(Building building, int x, int y)
 	{
 		building.setX(x - dragX);
 		building.setY(y - dragY);
 	}
 	
 	public void movePerson(Visitor visitor)
 	{
 		Point p = searchNearestDestination(visitor.getDestination(), visitor);
 		Point space = getAvailableSpace((int)(p.getX()), (int)(p.getY()));
 		int x = (int) (space.getX()/4 * 4);
 		int y = (int) (space.getY()/4 * 4);
 
 // 		Visitor otherPersonLeft = new Visitor(visitor.getX()-4, visitor.getY());
 // // 		Visitor otherPersonRight = new Visitor(visitor.getX()+4, visitor.getY());
 // 		Visitor otherPersonUp = new Visitor(visitor.getX(), visitor.getY()-4);
 // 		Visitor otherPersonDown = new Visitor(visitor.getX(), visitor.getY()+4);
 // 		Visitor otherPersonCornerUpLeft = new Visitor(visitor.getX()-4, visitor.getY()-4);
 // 		Visitor otherPersonCornerUpRight = new Visitor(visitor.getX()+4, visitor.getY()-4);
 // 		Visitor otherPersonCornerDownRight = new Visitor(visitor.getX()+4, visitor.getY()+4);
 // 		Visitor otherPersonCornerDownLeft = new Visitor(visitor.getX()-4, visitor.getY()+4);
 	
 		boolean moved = false;
 		if(visitor.getStatus() != "DestinationReached")
 		
 //		Visitor otherPersonLeft = new Visitor(visitor.getX()-4, visitor.getY());
 //		Visitor otherPersonRight = new Visitor(visitor.getX()+4, visitor.getY());
 //		Visitor otherPersonUp = new Visitor(visitor.getX(), visitor.getY()-4);
 //		Visitor otherPersonDown = new Visitor(visitor.getX(), visitor.getY()+4);
 //		boolean moved = false;
 		if (visitor.getStatus() != "DestinationReached")
 		{
 			if (x < visitor.getX())
 			{
 				bitmap.claim((visitor.getX()/4)-1, visitor.getY()/4);
 				bitmap.free(visitor.getX()/4, visitor.getY()/4);
 				visitor.act("LEFT", 4);
 			}
 			else if (x > visitor.getX())
 			{
 				bitmap.claim(visitor.getX()/4+1, visitor.getY()/4);
 				bitmap.free(visitor.getX()/4, visitor.getY()/4);
 				visitor.act("RIGHT", 4);
 			}
 		
 			if (y < visitor.getY())
 			{
 				bitmap.claim(visitor.getX()/4, visitor.getY()/4-1);
 				bitmap.free(visitor.getX()/4, visitor.getY()/4);
 				visitor.act("UP", 4);
 			}
 			else if (y>visitor.getY())
 			{
 				bitmap.claim(visitor.getX()/4, visitor.getY()/4+1);
 				bitmap.free(visitor.getX()/4, visitor.getY()/4);
 				visitor.act("DOWN", 4);
 			}
			else if(x < visitor.getX()&& y < visitor.getY() && (!checkPeople(otherPersonCornerUpLeft)))
			{
				visitor.act("UP", 4);
				visitor.act("LEFT", 4);
			}
			else if(x > visitor.getX()&& y < visitor.getY() && (!checkPeople(otherPersonCornerUpRight)))
			{
				visitor.act("UP", 4);
				visitor.act("RIGHT", 4);
			}
			else if(x > visitor.getX()&& y > visitor.getY() && (!checkPeople(otherPersonCornerDownRight)))
			{
				visitor.act("DOWN", 4);
				visitor.act("RIGHT", 4);
			}
			else if(x < visitor.getX()&& y > visitor.getY() && (!checkPeople(otherPersonCornerDownLeft)))
			{
				visitor.act("DOWN", 4);
				visitor.act("LEFT", 4);
			}
			if(visitor.getX() == x && visitor.getY() == y && moved)
			{
				visitor.setStatus("DestinationReached");
			}
	}
 		
 		
 //		if(visitor.getStatus() != "DestinationReached")
 //		{
 //			if(x < visitor.getX() && (!checkPeople(otherPersonLeft)))
 //			{
 //				visitor.act("LEFT", 4);
 //				moved = true;
 //			}
 //			else if(x+24 > visitor.getX() && (!checkPeople(otherPersonRight)))
 //			{
 //				visitor.act("RIGHT", 4);
 //				moved = true;
 //			}
 //			if(y < visitor.getY() && (!checkPeople(otherPersonUp)))
 //			{
 //				visitor.act("UP", 4);
 //				moved = true;
 //			}
 //			else if(y+24 > visitor.getY() && (!checkPeople(otherPersonDown)))
 //			{
 //				visitor.act("DOWN", 4);
 //				moved = true;
 //			}
 //			if(visitor.getX() == x && visitor.getY() == y && moved)
 //			{
 //				visitor.setStatus("DestinationReached");
 //			}
 //			if(!moved)
 //			{
 //				visitor.increaseTimesTried();
 //			}
 //			else
 //			{
 //				visitor.resetTimesTried();
 //			}
 //		}
 	}
 	
 	//Methods for behavior
 	public void destinationChange(Visitor visitor)
 	{
 		Random random = new Random();
 		if(random.nextInt()%2 == 0)
 		{
 			visitor.setDestination("EHBO");
 		}
 		else
 		{
 			visitor.setDestination("SnackBar");
 		}
 	}
 	
 	public void changeDestinationPeople()
 	{
 		for(Visitor visitor: people)
 		{
 			destinationChange(visitor);
 			visitor.setStatus(null);
 		}
 	}
 	
 	public Point searchNearestDestination(String destination, Visitor visitor)
 	{
 		int x = 1000;
 		int y = 1000;
 		int difference;
 		int difference2;
 		int visitorXY = visitor.getX()+visitor.getY();
 		for(Building building: buildings)
 		{
 			int buildingXY = building.getX()+building.getY();
 			//Calculate differences
 			if(visitorXY > buildingXY)
 			{
 				difference = visitorXY - buildingXY;
 			}
 			else
 			{
 				difference = buildingXY - visitorXY;
 			}
 			if(visitorXY > x+y)
 			{
 				difference2 = visitorXY - x+y;
 			}
 			else
 			{
 				difference2 = x+y - visitorXY;
 			}
 			//Destination chooser
 			if(destination == "EHBO" && building instanceof EHBO)
 			{
 				if(difference < difference2)
 				{
 					x = building.getX();
 					y = building.getY();
 				}
 			}
 			if(destination == "SnackBar" && building instanceof Snackbar)
 			{
 				if(difference < difference2)
 				{
 					x = building.getX();
 					y = building.getY();
 				}
 			}
 		}
 		return new Point(x,y);
 	}
 	
 	public Point getAvailableSpace(int x, int y)
 	{
 		Point space = new Point();
 		boolean breaker = false;
 		for(int i = x; i <= x+24; i+=4)
 		{
 			for(int t = y; t <= y+24; t+=4)
 			{
 				if(!checkPeople(new Visitor(i, t)))
 				{
 				space = new Point(i, t);
 				breaker = true;
 				break;
 				}
 			}
 			if(breaker)
 			{
 				break;
 			}
 		}
 		return space;
 	}
 
 	
 	//mouseDrag method
 	public void mouseDragged(MouseEvent e) {
 		if (canDrag) {
 		for (Building building : buildings)
 		{
 			if (buildingSelection == building)
 			{
 				moveDragBuilding(building, e.getX(), e.getY());
 				break;
 			}
 		}
 		}
 		}
 	
 	
 	//mousePress method
 	public void mousePressed(MouseEvent e) {
 	       for(Building building: buildings)
 	       {
 			checkDragBuilding(building, e.getX(), e.getY());
 	       }
 		}
 	
 	
 	//mouseRelease method
 	public void mouseReleased(MouseEvent e) {
 		canDrag = false;
 		for(Building building: buildings)
 		{
 			building.setX((building.getX()/24)*24);
 			building.setY((building.getY()/24)*24);
 		}
 		repaint();
 	}
 
 	
 	//mouseClick method
 	public void mouseClicked(MouseEvent e) 
 	{
 		if(e.getButton() == MouseEvent.BUTTON3)
 		{
 			for(Building building: buildings)
 			{
 				if(building.getX() == e.getX()/24*24 && building.getY() == e.getY()/24*24)
 				{
 					destinationX = building.getX();
 					destinationY = building.getY();
 					break;
 				}
 			}
 		}
 		if(e.getButton() == MouseEvent.BUTTON1)	
 		{
 			changeDestinationPeople();
 		}
 	}
 	
 	 //Not used
 	public void mouseMoved(MouseEvent e) {}
 	public void mouseEntered(MouseEvent e) {}
 	public void mouseExited(MouseEvent e) {}
 	
 
 	
 	
 
 
 
 }
