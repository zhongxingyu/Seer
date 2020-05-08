 package io.github.ldears.ld26.map;
 
 import io.github.ldears.ld26.models.Action;
 
 import java.awt.Point;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * @author dector
  */
 public class Container extends GameObject {
 
 	int volume;
 	int tileWidth;
 	int tileHeight;
 	boolean transparent;
 
 	// Void if null
 	// Sprite sprite;
 
 	// Held objects
 	private List<Item> items;
 
 	public Container(int x, int y, String name, int volume, int tileWidth,
 			int tileHeight, boolean transparent) {
 		super(x, y, name, ObjectType.CONTAINER);
 		this.volume = volume;
 		this.tileWidth = tileWidth;
 		this.tileHeight = tileHeight;
 		this.items = new LinkedList<Item>();
 		this.transparent = transparent;
 	}
 
 	public Iterable<Item> getContents() {
 		return items;
 	}
 
 	public boolean add(Item i) {
		if (items.size() < volume) {
 			items.add(i);
 			return true;
 		} else
 			return false;
 	}
 
 	public Item remove(Item i) {
 		if (items.remove(i))
 			return i;
 		else
 			return null;
 	}
 
 	public Item poke() {
 		if (items.size() > 0)
 			return items.remove(0);
 		else
 			return null;
 	}
 
 	@Override
 	public boolean contains(Point p) {
 		if (p.x >= tiledX && p.x < tiledX + tileWidth)
 			if (p.y >= tiledY && p.y < tiledY + tileHeight)
 				return true;
 		return false;
 	}
 
 	@Override
 	public Action getAction(boolean hasItem) {
 		if (hasItem && isFull())
 			return Action.NONE;
 		else if (!hasItem && !isEmpty())
 			return Action.GET_ITEM;
 		else if (hasItem && !isFull())
 			return Action.DROP_ITEM;
 		else
 			// (!hasItem && isEmpty())
 			return Action.NONE;
 	}
 
 	public boolean isFull() {
 		return items.size() == volume;
 	}
 
 	public boolean isEmpty() {
 		return items.size() == 0;
 	}
 
 	public boolean isTransparent() {
 		return transparent;
 	}
 }
