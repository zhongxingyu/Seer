 package Shooter;
 
 import java.util.ArrayList;
 
 import org.lwjgl.input.Keyboard;
 
 public class KeyboardController implements iController{
 
 	ArrayList<iControllable> controllables;
 	
 	public KeyboardController()
 	{
 		controllables = new ArrayList<iControllable>();
 	}
 	
 	@Override
 	public void HandleInput() {
 		while (Keyboard.next())
 		{
 			InputEvent i = new InputEvent();
 			i.KeyCode = Keyboard.getEventKey();
 			i.keyState = Keyboard.getEventKeyState();
 			for (iControllable j : controllables)
 			{
 				j.HandleInput(i);
 			}
 		}
 	}
 
 	@Override
 	public void AttachControllable(iControllable c) {
 		// TODO Auto-generated method stub
 		controllables.add(c);
 	}
 
 	@Override
 	public void RemoveControllable(iControllable c) {
 		// TODO Auto-generated method stub
 		controllables.remove(c);
 	}
 
 }
