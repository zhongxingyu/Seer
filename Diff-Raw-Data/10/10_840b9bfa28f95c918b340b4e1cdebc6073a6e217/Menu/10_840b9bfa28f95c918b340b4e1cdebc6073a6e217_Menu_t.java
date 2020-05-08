 package menusystem2;
 
 import java.awt.Color;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.util.ArrayList;
 import java.awt.Graphics;
 
 import main.ButtonChecker;
 
 public class Menu
 {
 	public static long maxID = 0;
 	public long ID = -1;			//id of the menu (use negative numbers for specialized buttons)
 	
 	public static int indentation = 0;		//used in the printMenus method
 	
 	public boolean placeholder = false;		//true = should not be considered a Menu to be viewed
 	public boolean unsized = false;			//true = pixel dimensions not yet set
 	public boolean unpositioned = false;	//true = xPos & yPos are not yet set
 	
 	public ArrayList<Menu> menus = new ArrayList<Menu>();
 	
 	public double x = 0, y = 0;
 	public int width = 0, height = 0;			//dimensions of menu in pixels
 	public int xPadding = 10, yPadding = 10;	//extra pixels between each row and column
 	
 	public int rows = 1, cols = 1;				//number of rows and columns in menu
 	public int xPos = 0, yPos = 0, xSize = 1, ySize = 1;	//dimension in "button units"
 	
 	public Color bgcolor = new Color(255, 0, 255);	//background color of menu
 	
 	public boolean pressed1 = false, pressed2 = false, pressed3 = false;	//true if mouse is in menu and button is pressed 
 	public boolean hover = false;
 	public boolean isDragging = false;		//will be true if a drag was started in this menu
 	
 	//a placeholder menu
 	public Menu()
 	{
 		placeholder = true;
 		unsized = true;
 		unpositioned = true;
 	}
 	
 	//a menu without pixel dimensions so they can be set by the upper menu (bool required so as not to conflict with other constructor)
 	public Menu(int xPos, int yPos, int xSize, int ySize, boolean bool)
 	{
 		assignID();
 		unsized = true;
 		this.xPos = xPos;
 		this.yPos = yPos;
 		this.xSize = xSize;
 		this.ySize = ySize;
 		setColor(ColorGen.randomColor());
 	}
 	
 	//a menu without a position (to be set when it is added to another menu)
 	public Menu(int xSize, int ySize, boolean bool)
 	{
 		this(0, 0, xSize, ySize, bool);
 		unpositioned = true;
 	}
 	
 	//a  menu that fills the Rectangle(x, y, width, height) (in pixels)
 	public Menu(int x, int y, int width, int height)
 	{
 		assignID();
 		this.x = x;
 		this.y = y;
 		this.width = width;
 		this.height = height;
 		setColor(ColorGen.randomColor());
 	}
 	
 	private void assignID()
 	{
 		ID = maxID;
 		maxID++;
 		ButtonChecker.addID();
 	}
 	
 	public void setPadding(int xPadding, int yPadding)
 	{
 		this.xPadding = xPadding;
 		this.yPadding = yPadding;
 	}
 	
 	public void setPos(int xPos, int yPos)
 	{
 		this.xPos = xPos;
 		this.yPos = yPos;
 		this.unpositioned = false;
 	}
 	
 	public void setColsRows(int cols, int rows)
 	{
 		this.rows = rows;
 		this.cols = cols;
 	}
 	
 	//set the background color of the menu
 	public void setColor(Color color)
 	{
 		bgcolor = new Color(color.getRed(), color.getGreen(), color.getBlue());
 	}
 	
 	//returns false if the menu couldn't be added
 	public boolean addMenu(Menu menu)
 	{
 		if(menu.unpositioned)
 		{
 			System.err.println("Menu wasn't positioned!");
 			return false;
 		}
 		if(!menuIsInBounds(menu))
 		{
 			System.err.println("Menu was out of bounds!");
 			return false;
 		}
 		if(menuCollides(menu))
 		{
 			System.err.println("Menu collided with another menu!");
 			return false;
 		}
 		
 		if(menu.unsized)
 			sizeMenu(menu);
 		
 		menu.sizeSubMenus();
 		
 		addPadding(menu);
 		
 		menus.add(menu);
 		
 		return true;
 	}
 	
 	//add an unpositioned menu
 	public boolean addMenu(Menu menu, int xPos, int yPos)
 	{
 		menu.setPos(xPos, yPos);
 		return addMenu(menu);
 	}
 	
 	//adds an ArrayList of Menus 
 	public void addMenus(ArrayList<Menu> menus)
 	{
 		boolean unpositioned = menus.get(0).unpositioned;
 		
 		if(!unpositioned)
 		{
 			for(int i = 0; i < menus.size(); i++)
 				addMenu(menus.get(i));
 		}
 		else
 		{
 			if(this instanceof Scroller)			//increase (or decrease) number of rows if its a Scroller
 			{
 				rows = (int)Math.ceil( 1.0 * menus.size() / (cols / menus.get(0).xSize) );
 			}
 			
 			int i = 0;
 			for(int y = 0; y < rows; y+=menus.get(0).ySize)
 			{
 				for(int x = 0; x < cols; x+=menus.get(0).xSize)
 				{
 					addMenu(menus.get(i), x, y);
 					i++;
 					
 					if(i == menus.size())
 						return;
 				}
 			}
 		}
 	}
 	
 	public boolean menuIsInBounds(Menu menu)
 	{
 		return !(menu.xSize > menu.xPos+this.cols || menu.ySize > menu.yPos+this.rows);
 	}
 	
 	public boolean menuCollides(Menu menu)
 	{
 		for(int i = 0; i < menus.size(); i++)
 			if(menus.get(i).collides(menu))
 				return true;
 		
 		return false;
 	}
 	
 	public boolean collides(Menu menu)
 	{
 		if(menu.xPos >= xPos+xSize-1 || menu.yPos >= yPos+ySize-1 || menu.xPos+menu.xSize <= xPos || menu.yPos+menu.ySize <= yPos)
 			return false;
 		return true;
 	}
 	
 	public void sizeMenu(Menu menu)
 	{
 		menu.x = this.x + (int)((this.width - xPadding)  * (1.0 * menu.xPos  / this.cols));
 		menu.y = this.y + (int)((this.height - yPadding) * (1.0 * menu.yPos  / this.rows));
 		menu.width =      (int)((this.width - xPadding)  * (1.0 * menu.xSize / this.cols));
 		menu.height =     (int)((this.height - yPadding) * (1.0 * menu.ySize / this.rows));
 		menu.unsized = false;
 	}
 	
 	public void sizeSubMenus()
 	{
 		for(int i = 0; i < menus.size(); i++)
 		{
 			if(menus.get(i).menus.size() > 0)
 				menus.get(i).sizeSubMenus();
 			else
 			{
 				sizeMenu(menus.get(i));
 				
 				menus.get(i).x += xPadding/2.0 + 1.0;			//note getting some sort of truncation error here
 				menus.get(i).y += yPadding/2.0 + 1.0;			//the x is off by 1 or -1 sometimes
 			}
 				
 		}
 	}
 	
 	public void addPadding(Menu menu)
 	{
 		menu.x += xPadding;
 		menu.y += yPadding;
 		menu.width -= xPadding;
 		menu.height -= yPadding;
 	}
 	
 	public void fillMenu(int minX, int minY, int maxX, int maxY, int xSize, int ySize)
 	{
 		int num = 1;		//for the button labels
 		
 		if(minX+maxX > cols)
 			maxX = cols-minX;
 		if(minY+maxY > rows)
 			maxY = rows-minY;
 		
 		for(int row = minY; row < minY+maxY; row+=ySize)
 		{
 			for(int col = minX; col < minX+maxX; col+=xSize)
 			{
 				if(!isOccupied(row, col))
 				{
 					addMenu(new Button(col, row, xSize, ySize, true, "Box "+num));
 					num++;
 				}
 			}
 		}
 	}
 	
 	public void fillMenu()
 	{
 		fillMenu(0, 0, cols, rows, 1, 1);
 	}
 	
 	public boolean isOccupied(int row, int col)
 	{
 		if(menus.size() == 0)
 			return false;
 		
 		for(int i = 0; i < menus.size(); i++)
 		{
 			Menu menu = menus.get(i);
 			if((col >= menu.xPos && col < menu.xPos+menu.xSize) && (row >= menu.yPos && row < menu.yPos+menu.ySize))
 				return true;
 		}
 		return false;
 	}
 	
 	public boolean contains(Point p)
 	{
 		return new Rectangle((int)x, (int)y, width, height).contains(p);
 	}
 	
 	//returns an arraylist of all submenus, including this one
 	public ArrayList<Menu> getAllMenus()
 	{
 		ArrayList<Menu> allMenus = new ArrayList<Menu>();
 		if(menus.size() == 0)
 		{
 			allMenus.add(this);
 		}
 		else
 		{
 			for(int i = 0; i < menus.size(); i++)
 			{
 				ArrayList<Menu> allSubMenus = menus.get(i).getAllMenus();
 				for(int j = 0; j < allSubMenus.size(); j++)
 					allMenus.add(allSubMenus.get(j));
 			}
 		}
 		return allMenus;
 	}
 	
 	//return most deeply nested menu at point
 	public Menu getSubMenu(Point p)
 	{
 		for(int i = 0; i < menus.size(); i++)
 		{
 			Menu m = menus.get(i);
 			if(m.contains(p))
 				return m.getSubMenu(p);
 		}
 		return this;	//will get call only when menus' size is 0
 	}
 	
 	//return 1st level menu at that point
 	public Menu getMenu(Point p)
 	{
 		for(int i = 0; i < menus.size(); i++)
 		{
 			Menu m = menus.get(i);
 			if(m.contains(p))
 				return m;
 		}
 		return new Menu();	//returns menu with boolean placeholder set to true
 	}
 	
 	//return most deeply nested Scroller at point
 	public Menu getScrollerSubMenu(Point p)
 	{
 		ArrayList<Menu> submenus = new ArrayList<Menu>();
 		
 		Menu firstMenu = this.getMenu(p);
 		if(!firstMenu.placeholder)
 		{
 			submenus.add(firstMenu);
 			
 			while(true)
 			{
 				Menu nextMenu = submenus.get(0).getMenu(p);
 				if(nextMenu.placeholder)
 					break;
 				submenus.add(0, nextMenu);		//add the next submenu to the front of the list
 			}
 		}
 		
 		//if the menus position is closer to 0, it is more deeply nested
 		
 		Menu nestedScroller = this;		//if there aren't any Scroller submenus, use this menu
 		for(int i = 0; i < submenus.size(); i++)
 		{
 			if(submenus.get(i) instanceof Scroller)
 			{
 				nestedScroller = submenus.get(i);
 				break;
 			}
 		}
 		
 		return nestedScroller;
 	}
 	
 	public void startingDrag(Point p)
 	{
 		getScrollerSubMenu(p).setDrag(true);
 	}
 	
 	public void endingDrag(Point p)
 	{
 		getScrollerSubMenu(p).setDrag(false);
 	}
 	
 	public void setDrag(boolean isDragging)
 	{
 		this.isDragging = isDragging;
 	}
 	
 	//called when menu is left clicked or un-left clicked
 	public void clicked1(boolean beingPressed)
 	{
 		pressed1 = beingPressed;
 		ButtonChecker.pressed1(this);
 	}
 	
 	//called when there is a possibility that the menu was left clicked or un-left clicked
 	public void press1(Point p, boolean beingPressed)
 	{
 		if(contains(p))
 		{
 			if(beingPressed != pressed1)
 				clicked1(beingPressed);
 			for(int i = 0; i < menus.size(); i++)
 				menus.get(i).press1(p, beingPressed);
 		}
 		else
 		{
 			for(int i = 0; i < menus.size(); i++)
 				menus.get(i).press1(p, false);
 			pressed1 = false;
 		}
 	}
 	
 	//called when menu is middle clicked or un-middle clicked
 	public void clicked2(boolean beingPressed)
 	{
 		pressed2 = beingPressed;
 		ButtonChecker.pressed2(this);
 	}
 	
 	//called when there is a possibility that the menu was middle clicked or un-middle clicked
 	public void press2(Point p, boolean beingPressed)
 	{
 		if(contains(p))
 		{
 			if(beingPressed != pressed2)
 				clicked2(beingPressed);
 			for(int i = 0; i < menus.size(); i++)
 				menus.get(i).press2(p, beingPressed);
 		}
 		else
 		{
 			for(int i = 0; i < menus.size(); i++)
 				menus.get(i).press2(p, false);
 			pressed2 = false;
 		}
 	}
 	
 	//called when menu is middle right or un-right clicked
 	public void clicked3(boolean beingPressed)
 	{
 		pressed3 = beingPressed;
 		ButtonChecker.pressed3(this);
 	}
 	
 	//called when there is a possibility that the menu was right clicked or un-right clicked
 	public void press3(Point p, boolean beingPressed)
 	{
 		if(contains(p))
 		{
 			if(beingPressed != pressed3)
 				clicked3(beingPressed);
 			for(int i = 0; i < menus.size(); i++)
 				menus.get(i).press3(p, beingPressed);
 		}
 		else
 		{
 			for(int i = 0; i < menus.size(); i++)
 				menus.get(i).press3(p, false);
 			pressed3 = false;
 		}
 	}
 	
 	//returns true if this menu actually scrolled something
 	public boolean scroll(Point p, boolean scrollingUp)
 	{
 		if(contains(p))
 			for(int i = 0; i < menus.size(); i++)
 				menus.get(i).scroll(p, scrollingUp);
 		return false;
 	}
 	
 	public void move(double deltaX, double deltaY)
 	{
 		y += deltaY;
 		sizeSubMenus();
 	}
 	
 	public void printMenus()
 	{
 		System.out.println(this);
 		
 		boolean indent = menus.size() > 0;
 		if(indent)
 			indentation++;
 		
 		for(int i = 0; i < menus.size(); i++)
 		{
 			if(indent)
 				for(int j = 0; j < indentation; j++)
 					System.out.print("| ");
 			menus.get(i).printMenus();
 		}
 		
 		if(indent)
 			indentation--;
 	}
 	
 	public void tick()
 	{
 		for(int i = 0; i < menus.size(); i++)
 			menus.get(i).tick();
 	}
 	
 	public void render(Graphics g)
 	{
 		g.setColor(bgcolor);
 		g.fillRect((int)x, (int)y, width, height);
 		
 		for(int i = 0; i < menus.size(); i++)
 			menus.get(i).render(g);
 	}
 	
 	public boolean equals(Object obj)
 	{
 		if(obj instanceof Menu)
 			return ((Menu)obj).ID == this.ID;
 		return false;
 	}
 	
 	public String toString()
 	{
 		return "Menu("+rows+", "+cols+") ID: "+ID;
 	}
 	
 	public String getStringPos()
 	{
 		return xPos+" "+yPos+" "+xSize+" "+ySize;
 	}
 }
