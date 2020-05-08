 package com.vloxlands.scene;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.util.vector.Vector2f;
 
import com.vloxlands.game.Game;
 import com.vloxlands.ui.IClickableGui;
 import com.vloxlands.ui.IGuiElement;
 
 public abstract class Scene
 {
 	public List<IGuiElement> content = new ArrayList<>();
 	private boolean wasButton0Down;
 	private boolean wasButton1Down;
 	private boolean wasButton2Down;
 	
 	public abstract void init();
 	
 	public void update()
 	{
 		for (IGuiElement i : content)
 			if (i instanceof IClickableGui)
 			{
 				((IClickableGui) i).onTick();
 			}
 		handleInput();
 		for (IGuiElement g : content)
 			if (g.isVisible()) g.render();
 	}
 	
 	public void handleInput()
 	{
		
		if (Mouse.isButtonDown(1))
		{
			Mouse.setGrabbed(true);
			if (Mouse.isButtonDown(1) && !wasButton1Down)
			{
				Mouse.setCursorPosition(Display.getWidth() / 2, Display.getHeight() / 2);
			}
			Game.currentGame.rotateCamera();
		}
		else Mouse.setGrabbed(false);
		
 		int x = Mouse.getX();
 		int y = Mouse.getY();
 		int flag = 0;
 		
 		flag += Mouse.isButtonDown(0) ? 1 : 0;
 		flag += wasButton0Down ? 2 : 0;
 		flag += Mouse.isButtonDown(1) ? 4 : 0;
 		flag += wasButton1Down ? 8 : 0;
 		flag += Mouse.isButtonDown(2) ? 16 : 0;
 		flag += wasButton2Down ? 32 : 0;
 		
 		wasButton0Down = Mouse.isButtonDown(0);
 		wasButton1Down = Mouse.isButtonDown(1);
 		wasButton2Down = Mouse.isButtonDown(2);
 		
 		if (!handleMouseGUI(x, y, flag)) handleMouseWorld(x, y, flag);
 		
 	}
 	
 	protected boolean handleMouseGUI(int posX, int posY, int flag)
 	{
 		IClickableGui iG = getObjectUnderCursor();
 		if (iG != null)
 		{
 			iG.handleMouse(posX - (int) iG.getPos().x, posY - (int) iG.getPos().y, flag);
 			return true;
 		}
 		return false;
 	}
 	
 	public IClickableGui getObjectUnderCursor()
 	{
 		for (IGuiElement i : content)
 			if (i instanceof IClickableGui)
 			{
 				IClickableGui iG = (IClickableGui) i;
 				Vector2f pos = iG.getPos();
 				Vector2f size = iG.getSize();
 				
 				int x = Mouse.getX();
 				int y = Display.getHeight() - Mouse.getY();
 				
 				float startA, startB, endA, endB;
 				
 				startA = pos.x;
 				startB = pos.y;
 				endA = pos.x + size.x;
 				endB = pos.y + size.y;
 				
 				if (startA < x && endA >= x && startB < y && endB >= y) return iG;
 			}
 		return null;
 	}
 	
 	protected void handleMouseWorld(int x, int y, int flag)
 	{}
 	
 	protected void lockScene()
 	{
 		for (IGuiElement i : content)
 			if (i instanceof IClickableGui)
 			{
 				((IClickableGui) i).setEnabled(false);
 			}
 	}
 }
