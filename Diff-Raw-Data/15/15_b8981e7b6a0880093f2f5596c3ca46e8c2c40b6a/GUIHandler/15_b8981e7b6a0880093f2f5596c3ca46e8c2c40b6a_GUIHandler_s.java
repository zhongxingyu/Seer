 package ui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.lwjgl.input.Mouse;
 
 public class GUIHandler
 {
 	private static List<Component> components = new ArrayList<>();
 	
 	public static void addComponent(Component c)
 	{
 		components.add(c);
 	}
 	
 	public static void renderComponents()
 	{
 		for(Component c : components) if(c.visible) c.draw();
 	}
 
 	public static void handleMouse()
 	{
 		int x = Mouse.getX();
		int y = Mouse.getY();
 		for(Component c : components)
 		{
 			if(x > c.x && x <= c.x + c.width && y > c.y && y <= c.y + c.height)
 			{
 				byte b = 0;
				if(Mouse.isButtonDown(1)) b += 1;
				if(Mouse.isButtonDown(2)) b += 2;
				if(Mouse.isButtonDown(3)) b += 4;
 				
 				c.mouseEvent(x - c.x, y - c.y, b);
 			}
 		}
 	}
 }
