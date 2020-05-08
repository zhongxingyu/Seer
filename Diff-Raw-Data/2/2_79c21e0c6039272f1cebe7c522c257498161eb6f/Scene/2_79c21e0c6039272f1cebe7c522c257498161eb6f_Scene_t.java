 package com.vloxlands.scene;
 
 import static org.lwjgl.opengl.GL11.*;
 
 import java.awt.Font;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.util.vector.Vector2f;
 
 import com.vloxlands.settings.CFG;
 import com.vloxlands.ui.ClickableGui;
 import com.vloxlands.ui.Container;
 import com.vloxlands.ui.IGuiElement;
 import com.vloxlands.ui.IGuiEvent;
 import com.vloxlands.ui.ImageButton;
 import com.vloxlands.ui.Label;
 import com.vloxlands.ui.TextButton;
 import com.vloxlands.util.FontAssistant;
 import com.vloxlands.util.NetworkAssistant;
 import com.vloxlands.util.RenderAssistant;
 
 public abstract class Scene
 {
 	public ArrayList<IGuiElement> content = new ArrayList<>();
 	private boolean wasButton0Down;
 	private boolean wasButton1Down;
 	private boolean wasButton2Down;
 	public boolean initialized = false;
 	
 	public abstract void init();
 	
 	boolean uiActive = true, worldActive = true;
 	
 	// -- title -- //
 	boolean titled = false;
 	
 	// -- userZone -- //
 	final int SPEED = 10;
 	
 	Container userZone;
 	
 	int userZoneWidth, defaultUserZoneHeight, userZoneWantedHeight;
 	int selectedZoneButton;
 	
 	protected void setBackground()
 	{
 		Label bg = new Label(0, 0, Display.getWidth(), Display.getHeight(), "");
 		bg.setZIndex(-1);
 		bg.setTexture("/graphics/textures/ui/paper.png");
 		bg.stackTexture = true;
 		bg.texW = 512;
 		bg.texH = 512;
 		content.add(bg);
 	}
 	
 	protected void setUserZone()
 	{
 		if (!CFG.INTERNET) return;
 		
 		NetworkAssistant.pullUserLogo();
 		
 		Label user = new Label(15, 15, 70, 70, "");
 		user.setZIndex(4);
 		user.setTexture("USER_LOGO");
 		content.add(user);
 		Label username = new Label(100, 10, 10, 30, CFG.USERNAME, false);
 		username.setZIndex(4);
 		userZoneWidth = 140 + FontAssistant.getFont(username.font).getWidth(CFG.USERNAME);
 		content.add(username);
 		userZone = new Container(0, 0, (userZoneWidth > TextButton.WIDTH) ? userZoneWidth : TextButton.WIDTH, 100, true);
 		userZone.setZIndex(3);
 		defaultUserZoneHeight = userZoneWantedHeight = 100;
 		selectedZoneButton = -1;
 		content.add(userZone);
 		
 		ImageButton friendList = new ImageButton(95, 53, 32, 32);
 		friendList.setZIndex(4);
 		friendList.setTexture("/graphics/textures/ui/FriendList.png");
 		friendList.setClickEvent(new IGuiEvent()
 		{
 			@Override
 			public void trigger()
 			{
 				final int wanted = Display.getHeight() / 3 * 2;
 				if (userZoneWantedHeight == wanted) userZoneWantedHeight = defaultUserZoneHeight;
 				else
 				{
 					selectedZoneButton = 0;
 					userZoneWantedHeight = wanted;
 				}
 			}
 		});
 		content.add(friendList);
 		
 		friendList = new ImageButton(95 + 32, 53, 32, 32);
 		friendList.setZIndex(4);
 		friendList.setTexture("/graphics/textures/ui/FriendList.png");
 		friendList.setClickEvent(new IGuiEvent()
 		{
 			@Override
 			public void trigger()
 			{
 				final int wanted = Display.getHeight() / 3;
 				if (userZoneWantedHeight == wanted) userZoneWantedHeight = defaultUserZoneHeight;
 				else
 				{
 					selectedZoneButton = 1;
 					userZoneWantedHeight = wanted;
 				}
 			}
 		});
 		content.add(friendList);
 		
 		friendList = new ImageButton(95 + 64, 53, 32, 32);
 		friendList.setZIndex(4);
 		friendList.setTexture("/graphics/textures/ui/FriendList.png");
 		friendList.setClickEvent(new IGuiEvent()
 		{
 			@Override
 			public void trigger()
 			{
 				final int wanted = Display.getHeight() / 3 + 20;
 				if (userZoneWantedHeight == wanted) userZoneWantedHeight = defaultUserZoneHeight;
 				else
 				{
 					selectedZoneButton = 2;
 					userZoneWantedHeight = wanted;
 				}
 			}
 		});
 		content.add(friendList);
 	}
 	
 	protected void setTitle(String title)
 	{
 		Label l = new Label(0, 0, Display.getWidth(), 60, title);
 		l.font = l.font.deriveFont(Font.BOLD, 60f);
 		content.add(l);
 		
 		titled = true;
 	}
 	
 	public void onTickContent()
 	{
 		ArrayList<IGuiElement> sorted = getSortedContent();
 		
 		for (IGuiElement i : sorted)
 			if (i instanceof ClickableGui)
 			{
 				((ClickableGui) i).onTick();
 			}
 	}
 	
 	public void onTick()
 	{
 		onTickContent();
 		
 		if (userZone != null)
 		{
 			float dif = userZoneWantedHeight - userZone.getSize().y;
 			if (Math.abs(dif) >= Math.abs(dif) / SPEED) userZone.setSize(new Vector2f(userZone.getSize().x, userZone.getSize().y + dif / SPEED));
 			else if (Math.abs(dif) != 0) userZone.setSize(new Vector2f(userZone.getSize().x, userZoneWantedHeight));
 		}
 	}
 	
 	public void render()
 	{
 		renderContent();
 		
 		if (userZone != null)
 		{
			RenderAssistant.renderLine(96, 10, 80, false, false);
 			RenderAssistant.renderLine(90, 45, userZoneWidth - 62, true, false);
 			glEnable(GL_BLEND);
 			RenderAssistant.renderRect(84, 38, 19, 26, 787 / 1024f, 409 / 1024f, 19 / 1024f, 26 / 1024f);
 			glDisable(GL_BLEND);
 			if (userZone.getSize().y > defaultUserZoneHeight)
 			{
 				if (selectedZoneButton > -1)
 				{
 					RenderAssistant.renderLine(10, 83, 72, true, false);
 					
 					glEnable(GL_BLEND);
 					if (selectedZoneButton > 0)
 					{
 						RenderAssistant.renderLine(85, 83, 13 + selectedZoneButton * 32, true, false);
 						glEnable(GL_BLEND);
 						RenderAssistant.renderRect(77, 76, 26, 19, 780 / 1024f, 450 / 1024f, 26 / 1024f, 19 / 1024f);
 						glDisable(GL_BLEND);
 					}
 					else RenderAssistant.renderRect(77, 76, 19, 19, 982 / 1024f, 498 / 1024f, 19 / 1024f, 19 / 1024f);
 					RenderAssistant.renderLine(95 + (selectedZoneButton + 1) * 32, 83, userZoneWidth - (selectedZoneButton + 1) * 32 - 68, true, false);
 					glDisable(GL_BLEND);
 				}
 			}
 		}
 		if (titled) RenderAssistant.renderLine((int) ((userZone != null) ? userZone.getPos().x + userZoneWidth + 30 : 0), 83, Display.getWidth(), true, true);
 	}
 	
 	public void renderContent()
 	{
 		ArrayList<IGuiElement> sorted = getSortedContent();
 		for (IGuiElement g : sorted)
 			if (g.isVisible()) g.render();
 	}
 	
 	public void handleMouse()
 	{
 		int x = Mouse.getX();
 		int y = Display.getHeight() - Mouse.getY();
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
 		
 		if ((!uiActive || !handleMouseGUI(x, y, flag)) && worldActive) handleMouseWorld(x, y, flag);
 	}
 	
 	public void handleKeyboard(int key, char chr, boolean down)
 	{
 		for (IGuiElement iG : content)
 		{
 			iG.handleKeyboard(key, chr, down);
 		}
 	}
 	
 	// not abstract so that implementing won't be forced
 	public boolean handleMouseGUI(int posX, int posY, int flag)
 	{
 		ClickableGui iG = getObjectUnderCursor();
 		if (iG != null && iG.isVisible() && iG.isEnabled())
 		{
 			iG.handleMouse(posX - (int) iG.getPos().x, posY - (int) iG.getPos().y, flag);
 			return true;
 		}
 		return false;
 	}
 	
 	private ClickableGui getObjectUnderCursor()
 	{
 		for (IGuiElement i : getSortedContent())
 			if (i instanceof ClickableGui)
 			{
 				ClickableGui iG = (ClickableGui) i;
 				if (iG.isUnderCursor()) return iG;
 			}
 		return null;
 	}
 	
 	private ArrayList<IGuiElement> getSortedContent()
 	{
 		@SuppressWarnings("unchecked")
 		final ArrayList<IGuiElement> sorted = (ArrayList<IGuiElement>) content.clone();
 		if (sorted.size() == 0 || sorted.get(0) == null) return new ArrayList<>();
 		
 		Collections.sort(sorted, new Comparator<IGuiElement>()
 		{
 			
 			@Override
 			public int compare(IGuiElement o1, IGuiElement o2)
 			{
 				return o1.getZIndex() - o2.getZIndex();
 			}
 		});
 		
 		return sorted;
 	}
 	
 	// not abstract so that implementing won't be forced
 	public void handleMouseWorld(int x, int y, int flag)
 	{}
 	
 	protected void lockScene()
 	{
 		for (IGuiElement i : content)
 			if (i instanceof ClickableGui)
 			{
 				((ClickableGui) i).setEnabled(false);
 			}
 	}
 }
