 package menusystem2;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Point;
 
 public class Scroller extends Menu
 {
 	public int pos = 0;	//the currently viewed row
 	public int visibleRows = rows;	//the number of rows visible at any one time
 	
 	public static final int minScrollSize = 10;		//minimum width of the scroll bar
 	public int scrollSize = 0;						//width of the scroll bar
 	
 	public Scroller()
 	{
 		super();
 	}
 	
 	public Scroller(int x, int y, int width, int height)
 	{
 		super(x, y, width, height);
 		
 		setScrollSize(xPadding);
 		if(scrollSize < minScrollSize)
 			scrollSize = minScrollSize;
 	}
 	
 	public Scroller(int xPos, int yPos, int xSize, int ySize, boolean bool)
 	{
 		super(xPos, yPos, xSize, ySize, bool);
 		
 		setScrollSize(xPadding);
 		if(scrollSize < minScrollSize)
 			scrollSize = minScrollSize;
 	}
 	
 	public Scroller(int xSize, int ySize, boolean bool)
 	{
 		super(xSize, ySize, bool);
 	}
 	
 	public void setVisibleRows(int visibleRows)
 	{
 		this.visibleRows = visibleRows;
 	}
 	
 	public void setScrollSize(int scrollSize)
 	{
 		this.scrollSize = scrollSize;
 	}
 	
 	public void sizeMenu(Menu menu)
 	{
 		menu.x = this.x + (int)(1.0 * (this.width - xPadding - scrollSize) * menu.xPos  / this.cols);
 		menu.y = this.y + (int)(1.0 * (this.height - yPadding)             * menu.yPos  / this.visibleRows);
 		menu.width =      (int)(1.0 * (this.width - xPadding - scrollSize) * menu.xSize / this.cols);
 		menu.height =     (int)(1.0 * (this.height - yPadding)             * menu.ySize / this.visibleRows);
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
 		pos += direction;
 		for(int i = 0; i < menus.size(); i++)
 		{
 			double deltaY = 1.0 * -direction * (this.height - yPadding) * menus.get(i).ySize / this.visibleRows;
			menus.get(i).y += deltaY;
 		}
 	}
 	
 	public void render(Graphics g)
 	{
 		//draw the background
 		g.setColor(bgcolor);
 		g.fillRect((int)x, (int)y, width, height);
 		
 		//draw the submenus
 		for(int i = 0; i < menus.size(); i++)
 		{
 			int yPos = menus.get(i).yPos;
 			
 			if(yPos < pos+visibleRows && yPos >= pos)
 				menus.get(i).render(g);
 		}
 		
 		//draw the scrollbar
 		g.setColor(ColorGen.changeColor(bgcolor, 50));
 		g.fillRect((int)x+width-scrollSize, (int)y, scrollSize, height);
 		g.setColor(ColorGen.changeColor(bgcolor, -20));
 		g.fillRect((int)x+width-scrollSize, (int)y+(int)(1.0*height*pos/rows), scrollSize, (int)(1.0*height*visibleRows/rows));
 		
 		//draw black edging on the scrollbar
 		g.setColor(Color.black);
 		g.drawLine((int)x+width-scrollSize, (int)y, (int)x+width-scrollSize, (int)y+height);
 		g.drawRect((int)x+width-scrollSize, (int)y+(int)(1.0*height*pos/rows), scrollSize, (int)(1.0*height*visibleRows/rows));
 	}
 	
 	public String toString()
 	{
 		return "Scroller("+rows+", "+cols+")";
 	}
 }
