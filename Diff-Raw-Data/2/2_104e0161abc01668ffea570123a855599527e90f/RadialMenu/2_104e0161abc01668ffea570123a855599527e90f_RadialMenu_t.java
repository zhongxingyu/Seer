 package hci.menu;
 
 import java.awt.*;
 import java.io.*;
 
 import hci.menu.icon.*;
 import hci.util.Point;
 
 public class RadialMenu {
 	
 	private static final int SIZE = 50, BORDER = 2;
 	
 	/**
 	 *  State of menu
 	 *  0 = middle
 	 *  1...n = segment from 12 o'clock clockwise
 	 */
 	private int state = 0;
 	private boolean visible = false;
 	private Point position;
 	private double alphaMod = 1;
 	private IconManager iconman;
 	private int containerWidth, containerHeight;
 	
 	public RadialMenu(int containerWidth, int containerHeight) throws IOException, FileNotFoundException
 	{
 		// load icons
 		Icon[] icons = {
 				new CompleteIcon(),
 				new EditIcon(),
 				new DeleteIcon(),
 				new MenuIcon()
 		};
 		
 		this.containerWidth = containerWidth;
 		this.containerHeight = containerHeight;
 		
		iconman = new IconManager(icons,50,50);
 	}
 	
 	public void show(Point p)
 	{
 		
 		state = 0;
 		visible = true;
 		alphaMod = 1;
 		
 		if (p.getX() - SIZE*2 - BORDER < 0)
 		{
 			p.setX(SIZE*2 + BORDER);
 		}
 		else if (p.getX() + SIZE*2 + BORDER > containerWidth)
 		{
 			p.setX(containerWidth - SIZE*2 - BORDER);
 		}
 		
 		if (p.getY() - SIZE*2 - BORDER < 0)
 		{
 			p.setY(SIZE*2 + BORDER);
 		}
 		else if (p.getY() + SIZE*2 + BORDER > containerHeight)
 		{
 			p.setY(containerHeight - SIZE*2 - BORDER);
 		}
 		
 		position = p;
 		
 	}
 	
 	public void hide()
 	{
 		visible = false;
 	}
 	
 	public void draw(Graphics g)
 	{
 		
 		if (!visible)
 			return;
 		
 		// draw background
 		Graphics2D g2d = (Graphics2D)g;
 		Composite c = g2d.getComposite();
 		int type = AlphaComposite.SRC_OVER;
 		g2d.setComposite(AlphaComposite.getInstance(type, (0.4f * (float)alphaMod)));
 		
 		g2d.setColor(Color.WHITE);
 		g2d.fillOval(position.getX() - SIZE*2 - BORDER, position.getY() - SIZE*2 - BORDER, 4*SIZE + 2*BORDER, 4*SIZE + 2*BORDER);
 		
 		// glow
 		g.setColor(Color.YELLOW);
 		switch (state)
 		{
 		case 0:
 			g.fillOval(position.getX() - (int)(SIZE/2) - BORDER, position.getY() - (int)(SIZE/2) - BORDER, SIZE + 2*BORDER, SIZE + 2*BORDER);
 			break;
 		case 1:
 			g.fillOval(position.getX() - (int)(SIZE/2) - BORDER, position.getY() - SIZE - 20 - (int)(SIZE/2) - BORDER, SIZE + 2*BORDER, SIZE + 2*BORDER);
 			break;
 		case 3:
 			g.fillOval(position.getX() - (int)(SIZE/2) + SIZE + 20 - BORDER, position.getY() - (int)(SIZE/2) - BORDER, SIZE + 2*BORDER, SIZE + 2*BORDER);
 			break;
 		case 5:
 			g.fillOval(position.getX() - (int)(SIZE/2) - BORDER, position.getY() + SIZE + 20 - (int)(SIZE/2) - BORDER, SIZE + 2*BORDER, SIZE + 2*BORDER);
 			break;
 		case 7:
 			g.fillOval(position.getX() - (int)(SIZE/2) - SIZE - 20 - BORDER, position.getY() - (int)(SIZE/2) - BORDER, SIZE + 2*BORDER, SIZE + 2*BORDER);
 			break;
 		}
 		
 		g.setColor(Color.BLUE);
 		
 		// 0
 		g.fillOval(position.getX() - (int)(SIZE/2), position.getY() - (int)(SIZE/2), SIZE, SIZE);
 		
 		// 1EditIcon
 		g.fillOval(position.getX() - (int)(SIZE/2), position.getY() - SIZE - 20 - (int)(SIZE/2), SIZE, SIZE);
 		
 		// 3
 		g.fillOval(position.getX() - (int)(SIZE/2) - SIZE - 20, position.getY() - (int)(SIZE/2), SIZE, SIZE);
 		
 		// 5
 		g.fillOval(position.getX() - (int)(SIZE/2), position.getY() + SIZE + 20 - (int)(SIZE/2), SIZE, SIZE);
 		
 		// 7
 		g.fillOval(position.getX() - (int)(SIZE/2) + SIZE + 20, position.getY() - (int)(SIZE/2), SIZE, SIZE);
 		
 		// icons
 		g2d.setComposite(AlphaComposite.getInstance(type, (0.8f * (float)alphaMod)));
 		
 		// 1
 		g.drawImage(iconman.getIconByName("EditIcon").getImage(), position.getX() - (int)(SIZE/2) + 10, position.getY() - SIZE - 10 - (int)(SIZE/2), null);
 		
 		// 3
 		g.drawImage(iconman.getIconByName("CompleteIcon").getImage(), position.getX() - (int)(SIZE/2) + SIZE + 30, position.getY() - (int)(SIZE/2) + 10, null);
 		
 		// 7
 		g.drawImage(iconman.getIconByName("DeleteIcon").getImage(), position.getX() - (int)(SIZE/2) - SIZE - 10, position.getY() - (int)(SIZE/2) + 10, null);
 		
 		// 5
 		g.drawImage(iconman.getIconByName("MenuIcon").getImage(), position.getX() - (int)(SIZE/2) + 10, position.getY() + SIZE + 30 - (int)(SIZE/2), null);
 		
 		
 		g2d.setComposite(c);
 		
 	}
 	
 	public boolean showing()
 	{
 		return visible;
 	}
 	
 	public int close()
 	{
 		
 		// accept current menu item
 		hide();
 		
 		return state;
 		
 	}
 	
 	public Point getPosition()
 	{
 		return position;
 	}
 	
 	public boolean updateMousePosition(Point p) 
 	{
 		
 		int oldState = state;
 		
 		double angle = Math.toDegrees(Math.atan2(p.getY() - position.getY(), p.getX() - position.getX()));
 		double dist = Math.sqrt(Math.pow(p.getX() - position.getX(),2) + Math.pow(p.getY() - position.getY(),2));
 		
 		if (dist > SIZE)
 		{
 		
 			if (angle > -45 && angle < 45)
 			{
 				state = 3;
 			}
 			else if (angle > 135 || angle < -135)
 			{
 				state = 7;
 			}
 			else if (angle < -45 && angle > -135)
 			{
 				state = 1;
 			}
 			else if (angle > 45 && angle < 135)
 			{
 				state = 5;
 			}
 			else
 			{
 				state = 0;
 			}
 		
 		}
 		else
 		{
 			state = 0;
 		}
 		
 		if (dist < SIZE*3)
 		{
 			alphaMod = 1;
 		}
 		else if (dist > SIZE*3 && dist < SIZE*5)
 		{
 			
 			// alpha mod
 			alphaMod = 1 - ((dist - SIZE*3) / (2*SIZE));
 			return true;
 			
 		}
 		else if (dist > SIZE*5)
 		{
 			state = 0;
 			alphaMod = 0;
 			return true;
 		}
 		
 		if (oldState == state)
 		{
 			return false;
 		}
 		return true;
 		
 	}
 	
 }
