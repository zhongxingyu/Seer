 package game.ui.window;
 
 import static org.lwjgl.opengl.GL11.GL_QUADS;
 import static org.lwjgl.opengl.GL11.glBegin;
 import static org.lwjgl.opengl.GL11.glColor4f;
 import static org.lwjgl.opengl.GL11.glEnd;
 import static org.lwjgl.opengl.GL11.glLoadIdentity;
 import static org.lwjgl.opengl.GL11.glTranslatef;
 import static org.lwjgl.opengl.GL11.glVertex2f;
 import game.Main;
 import game.entities.item.EquipItem;
 import game.entities.item.Item;
 import game.entities.item.UsableItem;
 import game.features.Stat;
 import game.structure.MapManager;
 import game.structure.Slot;
 import game.util.Util;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.util.Dimension;
 import org.lwjgl.util.Point;
 import org.newdawn.slick.opengl.Texture;
 
 public class Inventory extends Window
 {
 
 	List<Item> items = MapManager.getMap().getPlayer().getItems();
 	private int click = -1;
 	private long timeOfClick = 0L;
 	private boolean itemGrabbed = false;
 
 	public Inventory()
 	{
		super(new Point(100, 100), new Dimension(174, 256));
 	}
 
 	public void mouse()
 	{
 
 		// TODO NO ACCURATE WAY TO DISTINGUISH BETWEEN MOUSE MOVE AND MOUSE
 		// RELEASE SINCE THERE CAN BE A MOUSE MOVED EVENT WITH 0 DX & 0DY
 		// TRY SAVE MOUSE STATE AND COMPARE CURRENT STATE TO DETERMINE IF MOUSE
 		// MOVED OR RELEASED
 
 		if (Mouse.getEventButtonState())
 		{
 
 
 			setPressed(true);
 
 			int clickIndex = getClickedItem(Mouse.getX(), Main.DIM.getHeight() - Mouse.getY() + 1);
 			if (clickIndex != -1)
 			{
 				
 			}
 		} else if (!Mouse.getEventButtonState())
 		{
 			int dX = Mouse.getDX();
 			int dY = Mouse.getDY();
 			if (!itemGrabbed)
 			{
 				if (Mouse.isButtonDown(0) && isPressed())
 				{
 					int clickIndex = getClickedItem(Mouse.getX(), Main.DIM.getHeight() - Mouse.getY() + 1);
 					Item clickedItem = null;
 					if (clickIndex != -1)
 						clickedItem = items.get(clickIndex);
 					if (clickedItem == null)
 					{
 						if (!((dX > 0 && getX() > Main.DIM.getWidth() * .98)
 								|| (dX < 0 && getX() + getWidth() < Main.DIM.getWidth() * .02)
 								|| (dY > 0 && getY() < Main.DIM.getHeight() * .02) || (dY < 0 && getY() > Main.DIM
 								.getHeight() * .98)))
 						{
 							setPosition(getX() + dX, getY() - dY);
 						}
 					}
 				} else if (dX == 0 && dY == 0)
 				{
 					setPressed(false);
 				}
 			} else if (dX == 0 && dY == 0)
 			{
 				itemGrabbed = false;
 			}
 		}
 
 		if (Mouse.getEventButtonState())
 		{
 			if (timeOfClick + 200L < System.currentTimeMillis())
 			{
 				click = getClickedItem(Mouse.getX(), Main.DIM.getHeight() - Mouse.getY() + 1);
 				timeOfClick = System.currentTimeMillis();
 			} else if (getClickedItem(Mouse.getX(), Main.DIM.getHeight() - Mouse.getY() + 1) == click
 					&& click <= items.size() - 1 && click >= 0)
 			{
 				Item item = items.get(click);
 				if (item instanceof UsableItem)
 				{
 					MapManager.getMap().getPlayer().useItem(item);
 				} else if (item instanceof EquipItem)
 				{
 					EquipItem equip = (EquipItem) item;
 					MapManager.getMap().getPlayer().removeEquip(equip.getType());
 					MapManager.getMap().getPlayer().addEquip(equip);
 					MapManager.getMap().getPlayer().removeItem(item);
 				}
 				click = -1;
 			}
 		}
 	}
 
 	private int getClickedItem(int xPos, int yPos)
 	{
 		for (int i = 0; i < items.size(); i++)
 		{
 			if (xPos > getPosition().getX() + 7 + 32 * (i % 5)
 					&& xPos < getPosition().getX() + 7 + 32 * (i % 5) + Slot.SIZE
 					&& yPos > getPosition().getY() + 30 + 32 * (i / 5)
 					&& yPos < getPosition().getY() + 30 + 32 * (i / 5) + Slot.SIZE)
 				return i;
 		}
 		return -1;
 
 	}
 
 	public void render()
 	{
 		super.render();
 
 		Util.useFont("Courier New", Font.BOLD, 14, Color.white);
 		for (int i = 0; i < items.size(); i++)
 		{
 			
			items.get(i).render(getPosition().getX() + 7 + 32 * (i % 5), getPosition().getY() + 30 + 32 * (i / 5));
 			if (!(items.get(i) instanceof EquipItem))
 			{
 				Util.write(Integer.toString(items.get(i).getQuantity()), getPosition().getX() + 7 + 32 * (i % 5) + 3,
 						getPosition().getY() + 30 + 32 * (i / 5));
 			}
 		}
 		
 		
 
 		Util.write(Integer.toString(MapManager.getMap().getPlayer().getGold()), getPosition().getX() + 37,
 				getPosition().getY() + getSize().getHeight() - 25);
 
 		int mouseHover = getClickedItem(Mouse.getX(), Main.DIM.getHeight() - Mouse.getY() + 1);
 
 		if (mouseHover != -1)
 		{
 			Item item = items.get(mouseHover);
 
 			String lines[];
 			if (item instanceof EquipItem)
 			{
 				EquipItem equip = ((EquipItem) item);
 				ArrayList<String> linesList = new ArrayList<String>();
 				for (int i = 0; i < Stat.values().length; i++)
 				{
 					if (equip.getStat(Stat.values()[i]) != 0)
 						linesList.add(Stat.values()[i].NAME + ": "
 								+ Integer.toString(equip.getStat(Stat.values()[i])));
 				}
 
 				String[] description = Util.tokenizeText(item.getDescription(), 190, 4);
 				
 				lines = new String[linesList.size()+description.length];
 				int i;
 				for (i = 0; i < linesList.size(); i++)
 					lines[i] = linesList.get(i);
 				for(int j=0; j<description.length; j++)
 					lines[i+j] = description[j];
 			} else
 			{
 				lines = Util.tokenizeText(item.getDescription(), 190, 4);
 			}
 
 			Texture tex = Util.getTexture("UI/window/itemDesc.png");
 			
 			glColor4f(1, 1, 1, .5f);
 
 			Util.render(tex, Mouse.getX(), Main.DIM.getHeight() - Mouse.getY() + 1, 200, Util.getFontHeight() * lines.length + 55, tex.getTextureWidth(), tex.getTextureHeight());
 			
 			//render a white square with half transparency
 			glLoadIdentity();
 			glTranslatef(Mouse.getX(), Main.DIM.getHeight() - Mouse.getY() + 1, 0);
 			glBegin(GL_QUADS);
 			glVertex2f(10, 10);
 			glVertex2f(42, 10);
 			glVertex2f(42, 42);
 			glVertex2f(10, 42);
 			glEnd();
 			glLoadIdentity();
 
 			glColor4f(1, 1, 1, 1); //return to full opacity
 
 			item.render(Mouse.getX() + 10, Main.DIM.getHeight() - Mouse.getY() + 1 + 10);
 			
 			Util.write(item.getName(), Mouse.getX() + 42 + 10, Main.DIM.getHeight() - Mouse.getY() + 1 + 10);
 
 			for (int i = 0; i < lines.length; i++)
 			{
 				Util.write(lines[i], Mouse.getX() + 10,
 						Main.DIM.getHeight() - Mouse.getY() + 1 + 50 + i * Util.getFontHeight());
 			}
 		}
 	}
 
 	@Override
 	int getKey()
 	{
 		return Keyboard.KEY_I;
 	}
 
 }
