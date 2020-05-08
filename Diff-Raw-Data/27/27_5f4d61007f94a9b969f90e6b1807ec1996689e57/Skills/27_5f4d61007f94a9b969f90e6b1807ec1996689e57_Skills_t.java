 package game.ui.window;
 
 import game.util.Util;
 
 import java.awt.Color;
 import java.awt.Font;
 
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.util.Dimension;
 import org.lwjgl.util.Point;
 
 public class Skills extends Window{
 	
 	public Skills ()
 	{
		super(new Point(350, 450), new Dimension(174, 256));
 	}
 	
 	public void render()
 	{
 		super.render();
 		Util.useFont("Courier New", Font.BOLD, 14, Color.white);
 	}
 	
 	
 	@Override
 	protected void mouse() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	int getKey()
 	{
 		return Keyboard.KEY_K;
 	}
 
 }
