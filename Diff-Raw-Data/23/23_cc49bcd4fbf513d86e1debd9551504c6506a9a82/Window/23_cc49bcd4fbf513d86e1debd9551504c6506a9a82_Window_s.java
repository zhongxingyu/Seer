 package game.ui.window;
 
 import static org.lwjgl.opengl.GL11.glColor4f;
 import game.Main;
 import game.ui.Shop;
 import game.util.Util;
 
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.util.Dimension;
 import org.lwjgl.util.Point;
 import org.newdawn.slick.opengl.Texture;
 
 // TODO give priorities to window by last opened and draw them on top, give
 // "focus" to windows and only close the most recent opened with esc
 /**
  * Abstract type for windows and statically manages all other windows
  */
 public abstract class Window
 {
 
 	private Point position = new Point(100, 100);
 	private Dimension size = new Dimension(174, 256);
 	private Texture texture;
 	private boolean active = false, pressed = false;
 	private static Shop shop;
 
 	private static Window[] windows = { new Inventory(), new Skills(), new Equipment(), new Stats()};
 	
 	// TODO? create a stack for active windows, add on open and remove on close.
 	// Pop on esc pressed.
 	
 	public static boolean isShopOpen()
 	{
 		return shop != null;
 	}
 	
 	public Window(Point pos, Dimension s)
 	{
 		position = pos;
 		size = s;
 		texture = Util.getTexture("UI/window/" + getClass().getSimpleName().toLowerCase() + ".png");
 	}
 
 	public static void openShop(int id)
 	{
 		shop = new Shop(id);
 		for(Window w : windows)
 		{
 			w.close();
 		}
 	}
 	public static void closeShop()
 	{
 		shop = null;
 	}
 	
 	public static void renderAll()
 	{
 		if(shop != null)
 			shop.render();
 		for (Window w : windows)
 		{
 			if (w.isActive())
 				w.render();
 		}
 	}
 	
 	public void render()
 	{
 		glColor4f(1, 1, 1, .6f);
 		Util.render(texture, getPosition().getX(), getPosition().getY(), size.getWidth(), size.getHeight(), 174, 256);
 		glColor4f(1, 1, 1, 1);
 	}
 
 	public static void keyboardInput()
 	{
 		if (Keyboard.getEventKeyState())
 		{ // key released
 			for (Window w : windows)
 			{
				if (Keyboard.getEventKey() == w.getKey())
 				{ // each window has a key assigned to open it
 					w.toggleActive(); // open/close window
 					// if for some reason the window goes out of the screen. (this might happen because mouse events and the game run on different threads)
 					if (w.getX() > Main.DIM.getWidth() || w.getX() + w.getWidth() < 0
 							|| w.getY() > Main.DIM.getHeight() || w.getY() + w.getHeight() < 0) 
 						w.setPosition(100, 100);
 				} else if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) // close all
 				{ 
 					w.close();
 					closeShop();
 				}
 			}
 		}
 	}
 
 	public static void mouseInput()
 	{
 		if(shop != null )
 		{
 			shop.mouseInput();
 			return;
 		}
 		for (Window w : windows)
 		{
 			if (Mouse.getX() >= w.getX() && Mouse.getX() <= w.getX() + w.getWidth()
 					&& Main.DIM.getHeight() - Mouse.getY() + 1 >= w.getY()
 					&& Main.DIM.getHeight() - Mouse.getY() + 1 <= w.getY() + w.getHeight())
 			{
 				if (w.isActive())
 				{
 					w.mouse(); // pass the event to the window
 					break; // dont allow 2 windows to respond to the same event
 				}
 			}
 		}
 	}
 
 	abstract int getKey();
 	
 	// TODO make a textureManager to have all textures
 
 	public void setPosition(int x, int y)
 	{
 		position.setLocation(x, y);
 	}
 
 	public Point getPosition()
 	{
 		return position;
 	}
 
 	public boolean isActive()
 	{
 		return active; // active means open
 	}
 
 	protected void close()
 	{
 		active = false;
 	}
 
 	public void toggleActive()
 	{
 		active = !active;
 	}
 
 	abstract protected void mouse();
 
 	public boolean isPressed()
 	{
 		return pressed;
 	}
 
 	public void setPressed(boolean pressed)
 	{
 		this.pressed = pressed;
 	}
 
 	public int getX()
 	{
 		return position.getX();
 	}
 
 	public void setX(int x)
 	{
 		position.setX(x);
 	}
 
 	public int getY()
 	{
 		return position.getY();
 	}
 
 	public void setY(int y)
 	{
 		position.setY(y);
 	}
 
 	public int getWidth()
 	{
 		return size.getWidth();
 	}
 
 	public void setWidth(int width)
 	{
 		size.setWidth(width);
 	}
 
 	public int getHeight()
 	{
 		return size.getHeight();
 	}
 
 	public void setHeight(int height)
 	{
 		size.setHeight(height);
 	}
 	
 	public Texture getTexture()
 	{
 		return texture;
 	}
 	
 	public Dimension getSize()
 	{
 		return new Dimension(size);
 	}
 }
