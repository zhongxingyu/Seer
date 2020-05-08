 package com.banished.core.items;
 
 import java.util.*;
 
 //import processing.core.PApplet;
 import com.banished.Banished;
 import com.banished.core.Coordinate;
 import com.banished.core.Location;
 import com.banished.core.entities.Player;
 import com.banished.graphics.Graphics;
 import com.banished.graphics.Image;
 import com.banished.input.Mouse.MouseButton;
 
 // stores data about what items a player has
 // in the inventory. it is basically a grid
 // of items
 public class Inventory implements IInventory
 {
 	static final int 
 		WIDTH = 5, 
 		HEIGHT = 7;
 	// #pixels of the border image for each item
 	static final int BORDER_WIDTH = 2;
 	// #pixels between each image border
 	static final int BORDER_SPACING = 1;
 	// #pixels between items
 	static final int SPACING = 2 * BORDER_WIDTH + BORDER_SPACING;
 	
 	Image itemBorder, equippedBorder;
 	
 	boolean shown;
 	
 	// row-major order
 	Item[][] items;
 	
 	int pxItemsTop, pxItemsLeft;
 	int pxInvTop, pxInvLeft;
 	int pxItemsWidth, pxItemsHeight;
 	int pxInvWidth, pxInvHeight;
 	int pxTextOffsetX, pxTextOffsetY;
 	
 	public Inventory()
 	{
 		this.items = new Item[HEIGHT][];
 		for (int y = 0; y < HEIGHT; y++)
 			this.items[y] = new Item[WIDTH];
 		
 		itemBorder = Image.fromFile("gui/item border.png");
 		equippedBorder = Image.fromFile("gui/item equipped border.png");
 		
 		pxItemsWidth = (Item.IMAGE_WIDTH + SPACING) * WIDTH;
 		pxItemsHeight = (Item.IMAGE_HEIGHT + SPACING) * HEIGHT;
 		pxItemsLeft = Banished.width() - pxItemsWidth;
 		pxItemsTop = Banished.height() - pxItemsHeight;
 		pxInvWidth = pxItemsWidth + BORDER_WIDTH;
 		pxInvHeight = pxItemsHeight + BORDER_WIDTH;
 		pxInvTop = pxItemsTop - BORDER_WIDTH;
 		pxInvLeft = pxItemsLeft - BORDER_WIDTH;
 		pxTextOffsetX = Item.IMAGE_WIDTH * 7 / 8;
 		pxTextOffsetY = Item.IMAGE_HEIGHT * 7 / 8;
 		
 		shown = false;
 	}
 
 	public boolean isShown()
 	{
 		return shown;
 	}
 	public void setShown(boolean shown)
 	{
 //		System.out.println("shown <- " + shown);
 		this.shown = shown;
 	}
 	
 	public void setLocation(Location loc)
 	{
 		this.pxInvLeft = (int)loc.getX();
 		this.pxInvTop = (int)loc.getY();
 		
 		this.pxItemsLeft = this.pxInvLeft + BORDER_WIDTH;
 		this.pxItemsTop = this.pxInvTop + BORDER_WIDTH;
 	}
 	
 	public void set(int x, int y, Item item)
 	{
 		this.items[y][x] = item;
 	}
 	public boolean add(Item item)
 	{
 		for (int y = 0; y < HEIGHT; y++)
 		{
 			for (int x = 0; x < WIDTH; x++)
 			{
 				if (this.items[y][x] == null)
 				{
 					this.items[y][x] = item;
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 	// draws an inventory background,
 	// draws the item border behind every item space,
 	// and draws any items that are in the inventory
 	public void render()
 	{
 		int areaWidth = Item.IMAGE_WIDTH + SPACING,
 			areaHeight = Item.IMAGE_HEIGHT + SPACING;
 		
 		Graphics.Applet.pushMatrix();
 		Graphics.Applet.fill(0, 0);
 		Graphics.Applet.stroke(0, 0);
 		Graphics.Applet.strokeWeight(1f);
 		Graphics.Applet.rect(pxInvLeft, pxInvTop, pxInvWidth, pxInvHeight);
 		for (int y = 0; y < HEIGHT; y++)
 		{
 			for (int x = 0; x < WIDTH; x++)
 			{
 				Graphics.DrawImage(itemBorder, new Location(
 					pxInvLeft + areaWidth * x,
 					pxInvTop + areaHeight * y));
 				
 				Item item = items[y][x];
 				if (item == null) continue;
 				
 				if (Player.get().isWearing(item))
 					Graphics.DrawImage(equippedBorder, new Location(pxInvLeft + areaWidth * x,
 							pxInvTop + areaHeight * y));
 				
 				item.renderAt(
 					pxItemsLeft + areaWidth * x, 
 					pxItemsTop + areaHeight * y,
 					pxTextOffsetX, pxTextOffsetY);
 			}
 		}
 		Graphics.Applet.popMatrix();
 	}
 
 	public List<Object> getItemMarkers()
 	{
 		List<Object> markers = new ArrayList<Object>();
 		for (int y = 0; y < HEIGHT; y++)
 			for (int x = 0; x < WIDTH; x++)
 				markers.add(new Coordinate(x, y));
 		return markers;
 	}
 	public List<Object> getItemMarkers(int itemType)
 	{
 		return getItemMarkers();
 	}
 
 	public Location getLocationFromMarker(Object marker)
 	{
 		// (marker->loc) * (image size + spacing) + inv-top-left
 		return ((Coordinate)marker).toLocation().mult(Item.IMAGE_WIDTH + SPACING).add(new Location(pxInvLeft, pxInvTop));
 	}
 	
 	public Item pickup(Object marker, MouseButton button)
 	{
 		int y = ((Coordinate)marker).getY(), x = ((Coordinate)marker).getX();
 		
 		if (button == MouseButton.Left)
 		{
 			// pick up whole stack
 			Item stack = this.items[y][x];
 			this.items[y][x] = null;
 			return stack;
 		}
 		else if (button == MouseButton.Right)
 		{
 			Item stack = this.items[y][x];
 			if (stack == null) return null;
 			
 			stack.use();
 			if (stack.isConsumable())
 			{
 				if (stack.getCount() == 1)
 					this.items[y][x] = null;
 				else stack.setCount(stack.getCount() - 1);
 			}
 			return null;
 		}
 		else // middle button
 		{
 			// do nothing
 			return null;
 		}
 	}
 
 	public Item accept(Item item, Object marker)
 	{
 		int y = ((Coordinate)marker).getY(), x = ((Coordinate)marker).getX();
 		Item there = this.items[y][x];
 		
 		if (there == null)
 		{
 			// no item there; put the given item in the slot
 			this.items[y][x] = item;
 			return null;
 		}
 		else if (there.getType() == item.getType())
 		{
 			// same kind of item; fill up the stack if we can
 			if (there.getCount() < there.getMaxStackSize())
 			{
 				// max number of items to move
 				int diff = there.getMaxStackSize() - there.getCount();
 				
 				if (item.getCount() <= diff)
 				{
 					// move entire stack to inventory
 					there.setCount(there.getCount() + item.getCount());
 					return null;
 				}
 				else
 				{
 					// move only part of stack to inventory
 					there.setCount(there.getMaxStackSize());
 					item.setCount(item.getCount() - diff);
 					return item;
 				}
 			}
 			// no more room in the stack!
 			else return item;
 		}
 		else
 		{
 			// swap stacks
 			this.items[y][x] = item;
 			return there;
 		}
 	}
 }
