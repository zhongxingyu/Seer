 package isr;
 
 import java.util.HashMap;
 import java.util.HashSet;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.util.Log;
 
 import backend.geom.Vector2i;
 
 public class Room
 {
 	// Drawing layers
 	public static final int LAYER_FLOOR = 0;
 	public static final int LAYER_OBJECTS = 1;
 	public static final int LAYER_CHARACTERS = 2;
 	public static final int LAYER_CEILING = 3;
 	public static final int LAYER_COUNT = 4;
 	
 	// Sprites
 	private static Image backgrounds[];
 	
 	/** Positions a Character can occuppy **/
 	private Slot[] slots;
 	/** Count of free slots (for performance) **/
 	private int freeSlots;
 	
 	/** Type of the room (corridor, engine, jail...) **/
 	private RoomType type;
 	
 	/** Position of the top-left-corner of the room's bounding rectangle **/
 	private Vector2i pos = new Vector2i();
 	/**Last report*/
 	private String report;
 	
 	public static void loadContent() throws SlickException
 	{
 		backgrounds = new Image[RoomType.values().length];
 		for(RoomType t : RoomType.values())
 			backgrounds[t.ordinal()] = new Image(Game.ASSETS_DIR + t.spriteName);
 	}
 	
 	public Room(RoomType type)
 	{
 		this.type = type;
 		report = "";
 		if(type.size() > 0)
 		{
 			pos.set(type.x, type.y);
 			slots = new Slot[type.size()];
 			freeSlots = slots.length;
			Log.debug("Created " + type.name + " with " + freeSlots + " slots");
 			for(int i = 0; i < slots.length; i++)
 			{
 				slots[i] = new Slot();
 				slots[i].pos = new Vector2i(type.charactersPositions[i]);
 			}
 		}
 	}
 	
 	public void init()
 	{
 		// TODO Load the room layout from its type
 	}
 	
 	public void render(GameContainer gc, StateBasedGame game, Graphics gfx, int layer)
 	{
 		gfx.drawImage(backgrounds[type.ordinal()], 
 				type.x, 
 				type.y);
 	}
 
 	public RoomType getType()
 	{
 		return type;
 	}
 	
 	public String getReport()
 	{
 		return type.description+report;
 	}
 	
 	public void setReport(String s)
 	{
 		report = s;
 	}
 	
 	/**
 	 * check if the point is in the room
 	 * @param bx : global x in pixels
 	 * @param by : global y in pixels
 	 * @return
 	 */
 	public boolean contains(int bx, int by)
 	{
 		int spriteX = bx - pos.x;
 		int spriteY = by - pos.y;
 		// We use the floor as a collision mask because it covers all the surface of the room
 		Image img = backgrounds[type.ordinal()];
 		// If in the image
 		if(spriteX >= 0 && spriteY >= 0 && spriteX < img.getWidth() && spriteY < img.getHeight())
 		{
 			// If the pixel is not (roughly) transparent
 			return img.getColor(spriteX, spriteY).a > 0.01f;
 		}
 		return false;
 	}
 	
 	public boolean isFull()
 	{
 		return freeSlots == 0 || slots == null || slots.length == 0;
 	}
 	
 	/**
 	 * Get next available position if available. Returns null if there is no place.
 	 * @param absolute : if true, the returned position will be relative to the whole ship.
 	 * @return
 	 */
 	public Vector2i getNextAvailablePosition(boolean absolute)
 	{
 		for(Slot s : slots)
 		{
 			if(s.characterRef == null)
 			{
 				if(absolute)
 				{
 					return new Vector2i(
 						s.pos.x + type.x,
 						s.pos.y + type.y);
 				}
 				else
 					return new Vector2i(s.pos);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Adds a character to the room by finding a free slot.
 	 * Returns the position of the character if there is room for him, 
 	 * or null if not.
 	 * Warning: this method doesn't guaranties that a character will not 
 	 * be in two rooms at the same time.
 	 * @param c
 	 * @param absolutePosition : if true, the returned position will be relative to the whole ship.
 	 * @return
 	 */
 	public Vector2i addCharacter(Character c, boolean absolutePosition)
 	{
		String roomName = getType().name;
 		if(isFull())
 		{
 			System.out.println(
 				"INFO: the room is full (" + roomName + ")");
 			return null;
 		}
 		if(isCharacterIn(c))
 		{
 			System.out.println(
 				"ERROR: cannot add a character to a room " +
 				"where it is already present (" + roomName + ")");
 			return null;
 		}
 		for(Slot s : slots)
 		{
 			if(s.characterRef == null)
 			{
 				s.characterRef = c;
 				freeSlots--;
				Vector2i pos;
 				if(absolutePosition)
 				{
					pos = new Vector2i(
							s.pos.x + getType().x,
							s.pos.y + getType().y);
 				}
 				else
					pos = new Vector2i(s.pos);
				Log.debug(c.getName() + " added to " + roomName);
				return pos;
 			}
 		}
 		return null;
 	}
 	
 	public void removeCharacter(Character c)
 	{
 		for(Slot s : slots)
 		{
 			if(s.characterRef == c)
 			{
 				s.characterRef = null;
 				freeSlots++;
 				return;
 			}
 		}
 		Log.warn("tried to remove a character from a room, but it was not here.");
 	}
 	
 	public boolean isCharacterIn(Character c)
 	{
 		for(Slot s : slots)
 		{
 			if(s.characterRef == c)
 				return true;
 		}
 		return false;
 	}
 	
 	public int getLoyalCount()
 	{
 		int count = 0;
 		for(Slot s : slots)
 		{
 			if(s.characterRef != null)
 				count += s.characterRef.isLoyal() ? 1 : 0;
 		}
 		return count;
 	}
 	
 	public int getCharacterCount()
 	{
 		return slots.length - freeSlots;
 	}
 	
 	/**
 	 * This is a position a character can occupy in the room.
 	 * @author Marc
 	 *
 	 */
 	private class Slot
 	{
 		public Vector2i pos;
 		public Character characterRef;
 	}
 
 }
 
 
 
 
 
