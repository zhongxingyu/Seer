 package menusystem2;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.util.ArrayList;
 
 import main.Main;
 
 public class Scroller extends Menu
 {
 	public int pos = 0; //the currently viewed row
 	public int visibleRows = rows; //the number of rows visible at any one time
 	
 	public static final int minScrollSize = 10;          //minimum width of the scroll bar
 	public static final int maxScrollSize = 40;         //maximum width of the scroll bar
 	public static final double scrollSizePercent = 0.05; //size of scroll bar in relation to menu size
 	
 	public int initScrollPos = 0;
 	public boolean isDraggingScrollbar = false;
 	
 	public Scroller()
 	{
 		super();
 	}
 	
 	public Scroller(int x, int y, int width, int height)
 	{
 		super(x, y, width, height);
 	}
 	
 	public Scroller(int xPos, int yPos, int xSize, int ySize, boolean bool)
 	{
 		super(xPos, yPos, xSize, ySize, bool);
 	}
 	
 	public Scroller(int xSize, int ySize, boolean bool)
 	{
 		super(xSize, ySize, bool);
 	}
 	
 	public void setVisibleRows(int visibleRows)
 	{
 		this.visibleRows = visibleRows;
 	}
 	
 	public int getScrollbarSize()
 	{
 		int scrollSize = (int)(width*scrollSizePercent);
 		if(scrollSize < minScrollSize)
 			scrollSize = minScrollSize;
 		if(scrollSize > maxScrollSize)
 			scrollSize = maxScrollSize;
 		return scrollSize;
 	}
 	
 	public void sizeMenu(Menu menu)
 	{
 		menu.width = (int)((this.width - 2.0*getBorder() - getScrollbarSize() - xPadding)*menu.xSize/cols) - xPadding;
 		menu.height = (int)((this.height - 2.0*getBorder() - yPadding)*menu.ySize/this.visibleRows) - yPadding;
 		menu.x = this.x + getBorder() + xPadding + 1.0*menu.xPos*(menu.width + xPadding);
 		menu.y = this.y + getBorder() + yPadding + 1.0*menu.yPos*(menu.height + yPadding);
 		menu.unsized = false;
 	}
 	
 	//returns true if this menu actually scrolled something
 	public boolean scroll(Point p, boolean scrollingUp)
 	{
 		if(contains(p))
 		{
 			boolean somethingScrolled = false;
 			for(int i = 0; i < menus.size(); i++)
 			{
 				if(menus.get(i).scroll(p, scrollingUp))
 				{
 					somethingScrolled = true;
 					break;
 				}
 			}
 			if(!somethingScrolled)
 			{
 				scrollMenu(scrollingUp);
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public void scrollMenu(boolean scrollingUp)
 	{
 		if(scrollingUp && pos > 0)
 			changePos(-1);
 		else if(!scrollingUp && pos < rows-visibleRows)
 			changePos(1);
 	}
 	
 	public void changePos(int direction)
 	{
 		if(pos + direction < 0)
 			direction = -pos;
 		else if(pos + direction > rows-visibleRows)
 			direction = rows-visibleRows-pos;
 		
 		pos += direction;
 		for(int i = 0; i < menus.size(); i++)
 		{
 			double deltaY = 1.0 * -direction * (this.height - 2.0*getBorder() - yPadding) * menus.get(i).ySize / this.visibleRows;
			deltaY = (int)deltaY;
 			menus.get(i).move(0, deltaY);
 		}
 	}
 	
 	public void setPos(int pos)
 	{
 		changePos(pos - this.pos);
 	}
 	
 	public void setDrag(boolean isDragging)
 	{
 		super.setDrag(isDragging);
 		
 		if(isDragging)
 		{
 			if(Main.prevmse.x >= (int)x+width-getBorder()-getScrollbarSize() && Main.prevmse.x <= (int)x+width-getBorder())
 			{
 //				if(Main.prevmse.y >= (int)y+(int)(1.0*height*pos/rows) && Main.prevmse.y <= (int)(y + 1.0*height*pos/rows + 1.0*height*visibleRows/rows))
 				if(Main.prevmse.y >= y+getBorder() && Main.prevmse.y <= y + height - getBorder())
 				{
 					isDraggingScrollbar = true;
 					initScrollPos = pos;
 				}
 			}
 		}
 		else
 		{
 			isDraggingScrollbar = false;
 			initScrollPos = 0;
 		}
 	}
 	
 	//adds an ArrayList of Menus 
 	public void addMenus(ArrayList<Menu> menus)
 	{
 		if(menus.get(0).unpositioned)
 		{
 			rows = (int)Math.ceil( 1.0 * menus.size() / (cols / menus.get(0).xSize) );
 			pos = 0;
 		}
 		
 		super.addMenus(menus);
 	}
 	
 	public void tick()
 	{
 		super.tick();
 		
 		if(isDraggingScrollbar)
 		{
 //			double deltaY = Main.mse.y - Main.prevmse.y;
 //			double oneScroll = (int)(1.0*height/rows);
 //			int newPos = (int)(deltaY / oneScroll);
 			
 			double percent = ((double)Main.mse.y - y - (1.0*height*visibleRows/rows/2)) / (1.0*height*(rows-visibleRows-1)/rows);
 			int newPos = (int)(percent * (rows-visibleRows));
 			
 			if(newPos < 0)
 				newPos = 0;
 			else if(newPos > rows-visibleRows)
 				newPos = rows-visibleRows;
 			
 			setPos(newPos);
 		}
 	}
 	
 	public void render(Graphics g)
 	{
 		//draw the background
 		g.setColor(bgcolor);
 		g.fillRect((int)x, (int)y, width, height);
 		
 		//draw the borders
 		renderBorders(g);
 		
 		//draw the scrollbar
 		int scrollSize = getScrollbarSize();
 		g.setColor(ColorGen.changeColor(bgcolor, 50));
 		g.fillRect((int)x+width-getBorder()-scrollSize, (int)y+getBorder(), scrollSize, height-2*getBorder());	//whole bar
 		g.setColor(ColorGen.changeColor(bgcolor, -20));
 		g.fillRect((int)x+width-getBorder()-scrollSize, (int)y+getBorder()+(int)(1.0*(height-2*getBorder())*pos/rows), 
 				scrollSize, (int)(1.0*(height-2*getBorder())*visibleRows/rows));	//current position
 		
 		//draw black edging on the scrollbar
 		g.setColor(Color.black);
 		g.drawRect((int)x+width-getBorder()-scrollSize, (int)y+getBorder(), scrollSize, height-2*getBorder()-1);	//whole bar
 		g.drawRect((int)x+width-getBorder()-scrollSize, (int)y+getBorder()+(int)(1.0*(height-2*getBorder())*pos/rows), 
 				scrollSize, (int)(1.0*(height-2*getBorder())*visibleRows/rows));	//current position
 		
 		//render the tooltip
 		renderTooltip();
 		
 		//draw the submenus
 		for(int i = 0; i < menus.size(); i++)
 		{
 			int yPos = menus.get(i).yPos;
 			
 			if(yPos < pos+visibleRows && yPos >= pos)
 				menus.get(i).render(g);
 		}
 	}
 	
 	public String toString()
 	{
 		return "Scroller("+rows+", "+cols+")";
 	}
 }
