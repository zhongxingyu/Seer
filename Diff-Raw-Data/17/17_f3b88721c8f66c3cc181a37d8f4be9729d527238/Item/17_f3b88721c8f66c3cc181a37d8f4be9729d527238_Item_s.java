 package yuuki.item;
 
 import java.awt.Point;
 
 import yuuki.ui.Displayable;
 import yuuki.world.Locatable;
 
 /**
  * Something that can be held in a character's inventory.
  */
 public class Item implements Locatable, Displayable {
 	
 	/**
 	 * The definition of an Item.
 	 */
 	public static class Definition {
 		
 		/**
 		 * The index of the Action that this Item activates when used.
 		 */
 		public int action;
 		
 		/**
 		 * The description of the item.
 		 */
 		public String description;
 		
 		/**
 		 * Whether the Item may be used outside of battle.
 		 */
 		public boolean external;
 		
 		/**
 		 * The ID of the Item.
 		 */
 		public long id;
 		
 		/**
 		 * The image for the Item.
 		 */
 		public String image;
 		
 		/**
 		 * The name of the Item.
 		 */
 		public String name;
 		
 		/**
 		 * Whether the Item may be used.
 		 */
 		public boolean usable;
 		
 		/**
 		 * The number of times that this Item may be used.
 		 */
 		public int uses;
 		
 		/**
 		 * The Item's value in copper.
 		 */
 		public int value;
 		
 	}
 	
 	/**
 	 * The description of this item.
 	 */
 	private String description;
 	
 	/**
 	 * The unique identifier for this item's type.
 	 */
 	private final long id;
 	
 	/**
 	 * The index of this item's image.
 	 */
 	private final String image;
 	
 	/**
 	 * This item's location on the map. May be null if this Item is not
 	 * currently on a map.
 	 */
 	private Point location = null;
 	
 	/**
 	 * The name of this Item.
 	 */
 	private final String name;
 	
 	/**
	 * The value of this Item.
 	 */
 	private final int value;
 	
 	/**
 	 * Creates a new Item.
 	 * 
 	 * @param id The ID of this item's type.
 	 * @param name The name of this item.
 	 * @param value The value of this item.
 	 * @param image The index of the image for this item.
 	 * @param description The description of this item.
 	 */
 	public Item(long id, String name, int value, String image,
 			String description) {
 		this.name = name;
 		this.value = value;
 		this.id = id;
 		this.image = image;
 		this.description = description;
 	}
 	
 	/**
 	 * Checks whether this Item is the same type as another.
 	 * 
 	 * @param i2 The item to check against.
 	 */
 	public boolean equals(Item i2) {
 		return (this.id == i2.id);
 	}
 	
 	@Override
 	public String getBattleImage() {
 		return null;
 	}
 	
 	/**
 	 * Gets the description of this Item.
 	 */
 	public String getDescription() {
 		return description;
 	}
 	
 	@Override
 	public Displayable getDisplayable() {
 		return this;
 	}
 	
 	@Override
 	public char getDisplayChar() {
 		return ' ';
 	}
 	
 	/**
 	 * Gets the type ID of this Item.
 	 * 
 	 * @return The ID.
 	 */
 	public long getId() {
 		return id;
 	}
 	
 	/**
 	 * Gets the index of the image for this item.
 	 * 
 	 * @return The image index.
 	 */
 	public String getImage() {
 		return image;
 	}
 	
 	@Override
 	public Point getLocation() {
 		return location;
 	}
 	
 	/**
 	 * Gets the name of this item.
 	 * 
 	 * @return The name.
 	 */
 	public String getName() {
 		return name;
 	}
 	
 	@Override
 	public String getOverworldImage() {
 		return image;
 	}
 	
 	/**
 	 * Gets the value of this item.
 	 * 
 	 * @return The value.
 	 */
 	public int getValue() {
 		return value;
 	}
 	
 	/**
 	 * Checks whether this Item may be used outside of battle.
 	 * 
 	 * @return false in the general case; subclasses may override.
 	 */
 	public boolean isExternal() {
 		return false;
 	}
 	
 	/**
 	 * Checks whether this Item may be used.
 	 * 
 	 * @return false in the general case; subclasses may override.
 	 */
 	public boolean isUsable() {
 		return false;
 	}
 	
 	@Override
 	public void setLocation(Point l) {
 		location = l;
 	}
 	
 	@Override
 	public String toString() {
 		return getName();
 	}
 	
 }
