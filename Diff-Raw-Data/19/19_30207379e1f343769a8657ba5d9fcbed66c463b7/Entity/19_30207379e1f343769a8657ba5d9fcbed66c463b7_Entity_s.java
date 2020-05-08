 /*
  * Copyright (C) 2012 JPII and contributors
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.jpii.navalbattle.pavo.grid;
 
 import java.io.Serializable;
 
 import com.jpii.navalbattle.game.NavalGame;
 import com.jpii.navalbattle.pavo.*;
 
 public class Entity implements Serializable {
 	
 	private Location location = Location.Unknown;
 	private String tag = "";
 	public long lastUpdate = 0;
 	private int width, height;
 	private EntityManager manager;
 	private GridedEntityTileOrientation id;
 	public int teamId;
 	private byte ORIENTATION_BUFFER_POSITION = GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT;
 	public String imgLocation;
 	private static int ENTITYMASTERRECORDSYSTEMPLEASEDONOTTOUCHTHIS = 0;
 	
 	public Entity(EntityManager em) {
 		manager = em;
 		init();
 	}
 	
 	public byte getCurrentOrientation() {
 		return ORIENTATION_BUFFER_POSITION;
 	}
 	
 	public int getCurrentId() {
 		return id.memCall(ORIENTATION_BUFFER_POSITION)[0];
 	}
 	
 	public String getTag() {
 		return tag;
 	}
 	
 	public void setTag(String tag) {
 		this.tag = tag;
 	}
 	
 	public Entity(EntityManager em,Location loc, GridedEntityTileOrientation id,byte orientation,int teams) {
 		manager = em;
 		location = loc;
 		teamId=teams;
 		ORIENTATION_BUFFER_POSITION = orientation;
 		try {
 			moveTo(loc,true);
 		}
 		catch (Throwable throwable) {
 			
 		}
 		manager.addEntity(this);
 		init();
 		setId(id);
 	}
 	/**
 	 * Initialises the entity. This should never be called. If inheriting <code>Entity></code>, this method should probably be overriden.
 	 */
 	public void init() {
 		setWidth(1);
 		setTag("entity-"+ENTITYMASTERRECORDSYSTEMPLEASEDONOTTOUCHTHIS++);
 		//setHeight(1);
 	}
 	
 	/*
 	 * Actions:
 	 */
 	
 	/**
 	 * Rotates the entity.
 	 * @param akamai The rotation to apply to the entity. (e.g. Location.HALF_CIRCLE).
 	 */
 	public void rotateTo(byte akamai) {
 		if (akamai == GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT) {
 			if (ORIENTATION_BUFFER_POSITION == GridedEntityTileOrientation.ORIENTATION_TOPTOBOTTOM){//akamai) {
 				for (int h = 0; h < getHeight(); h++) {
 					manager.setTile(location.getRow()+h,location.getCol(), null);
 				}
 				ORIENTATION_BUFFER_POSITION = akamai;
 				for (int w = 0; w < getWidth(); w++) {
 					Tile t = new Tile(this,location.getRow(),location.getCol()+w);
 					t.setId(new Id(id.memCall(ORIENTATION_BUFFER_POSITION)[0],w));
 					manager.setTile(location.getRow(),location.getCol()+w, t);
 				}
 				manager.getWorld().forceRender();
 			}
 		}
 		else if (akamai == GridedEntityTileOrientation.ORIENTATION_TOPTOBOTTOM) {
 			if (ORIENTATION_BUFFER_POSITION == GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT) {
 				for (int w = 0; w < getWidth(); w++) {
 					manager.setTile(location.getRow(),location.getCol()+w, null);
 				}
 				ORIENTATION_BUFFER_POSITION = akamai;
 				for (int h = 0; h < getHeight(); h++) {
 					Tile t = new Tile(this,location.getRow(),location.getCol());
 					t.setId(new Id(id.memCall(akamai)[0],h));
 					manager.setTile(location.getRow()+h,location.getCol(), t);
 				}
 				manager.getWorld().forceRender();
 			}
 		}
 	}
 	/**
 	 * Moves the entity to the specified location on the grid.
 	 * @param r The row to move the entity to.
 	 * @param c The column to move the entity to.
 	 */
 	public void moveTo(int r, int c) {
 		moveTo(new Location(r,c));
 	}
 	/**
 	 * Sets the genericied id of the entity. This shouldn't have to be called by the client.
 	 * @param id The identifier to set the entity to.
 	 */
 	public void setId(GridedEntityTileOrientation id) {
 		this.id = id;
 		/*for (int w = 0; w < getWidth(); w++) {
 			for (int h = 0; h < getHeight(); h++) {
 				Tile t = new Tile(this,location.getRow()+h, location.getCol()+w);
 				t.setId(new Id(id,w));
 				manager.setTile(location.getRow()+h, location.getCol()+w, t);
 				//System.out.println("efretgfd");
 			}
 		}*/
 		if (ORIENTATION_BUFFER_POSITION == GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT) {
 			for (int w = 0; w < getWidth(); w++) {
 				Tile t22 = new Tile(this,location.getRow(),location.getCol()+w);
 				int[] vbws = id.memCall(ORIENTATION_BUFFER_POSITION);
 				int vbw = vbws[0];
 				t22.setId(new Id(vbw,w));
 				manager.setTile(location.getRow(),location.getCol()+w, t22);
 			}
 		}
 		else if (ORIENTATION_BUFFER_POSITION == GridedEntityTileOrientation.ORIENTATION_TOPTOBOTTOM) {
 			for (int h = 0; h < getHeight(); h++) {
 				Tile t22 = new Tile(this,location.getRow()+h,location.getCol());
 				t22.setId(new Id(id.memCall(ORIENTATION_BUFFER_POSITION)[0],h));
 				manager.setTile(location.getRow()+h,location.getCol(), t22);
 			}
 		}
 		manager.getWorld().forceRender();
 	}
 	
 	public boolean canDoRotation(byte desiredRotation) {
 		return true;
 	}
 	
 	public boolean canDoMoveTo(Location desiredLocation) {
 		return true;
 	}
 	
 	/**
 	 * Moves the entity to the specified location on the grid.
 	 * @param loc The location to move the entity to.
 	 */
 	public void moveTo(Location loc) {
 		moveTo(loc,true);
 	}
 	/**
 	 * Moves the entity to the specified location on the grid.
 	 * @param r The row to move the entity to.
 	 * @param c The column to move the entity to.
 	 * @param override Should current entities at that location be overidden?
 	 * @return A value indicating if the operation was sucessfull.
 	 */
 	public boolean moveTo(int r, int c, boolean override) {
 		return moveTo(new Location(r,c),override);
 	}
 	/**
 	 * Moves the entity to the specified location on the grid.
 	 * @param loc The location to move the entity to.
 	 * @param override Should current entities at that location be overriden?
 	 * @return A value indicating if the operation was sucessfull.
 	 */
 	public boolean moveTo(Location loc, boolean override) {
 		if (loc == null)
 			return false;
 		if (loc == Location.Unknown) {
 			hideEntity();		
 			return true;
 		}
 		Tile<Entity> t = manager.getTile(loc);
 		if (t != null && ((t.getSuperId() != 0 || t.getEntity() != null) && override))
 			return false;
 		if (getWidth() + loc.getCol() + 1 >= PavoHelper.getGameWidth(manager.getWorld().getWorldSize())*2 ||
 				getHeight() + loc.getRow() + 1 >= PavoHelper.getGameHeight(manager.getWorld().getWorldSize())*2)
 			return false;
 		if (loc.getRow() < 0 || loc.getCol() < 0)
 			return false;
 		if (ORIENTATION_BUFFER_POSITION == GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT) {
 			for (int w = 0; w < getWidth(); w++) {
 				manager.setTile(location.getRow(),location.getCol()+w, null);
 				Tile t22 = new Tile(this,loc.getRow(),loc.getCol()+w);
 				t22.setId(new Id(id.memCall(ORIENTATION_BUFFER_POSITION)[0],w));
 				manager.setTile(loc.getRow(),loc.getCol()+w, t22);
 			}
 			manager.getWorld().forceRender();
 		}
 		else if (ORIENTATION_BUFFER_POSITION == GridedEntityTileOrientation.ORIENTATION_TOPTOBOTTOM) {
 			for (int h = 0; h < getHeight(); h++) {
 				manager.setTile(location.getRow()+h,location.getCol(),null);
 				Tile t22 = new Tile(this,loc.getRow()+h,loc.getCol());
 				t22.setId(new Id(id.memCall(ORIENTATION_BUFFER_POSITION)[0],h));
 				manager.setTile(loc.getRow()+h,loc.getCol(), t22);
 			}
 			manager.getWorld().forceRender();
 		}
 		Location swap2 = getLocation();
 		//System.out.println("wincall");
 		setLocation(loc);
 		manager.getWorld().forceRender();
 		onMove(swap2);
 		return true;
 	}
 	
 	private void hideEntity() {
 		for (int w = 0; w < getWidth(); w++) {
 			for (int h = 0; h < getHeight(); h++) {
 				Tile<Entity> ttmp = (Tile<Entity>)manager.getTile(h+getLocation().getRow(), w+getLocation().getCol());
 				manager.setTile(getLocation().getRow()+h, getLocation().getCol()+w,null);
 				//System.out.println("efretgfd");
 			}
 		}
 	}
 	
 	/**
 	 * Gets rid of the entity.
 	 */
 	public void truncate() {
 		if (getLocation() == null || getLocation() == Location.Unknown)
 			return;
 		int row = getLocation().getRow();
 		int col = getLocation().getCol();		
 		if (getCurrentOrientation() == GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT) {
 			for (int c = 0; c < width; c++) {
 				Tile t=null;
 				manager.setTile(new Location(row,col+c), t);
 			}
 		}
 		if (getCurrentOrientation() == GridedEntityTileOrientation.ORIENTATION_TOPTOBOTTOM) {
 			for (int c = 0; c < width; c++) {
 				Tile t=null;
 				manager.setTile(new Location(row+c,col), t);
 			}
 		}
 	}
 	
 	/**
 	 * Same as <code>truncate()</code>.
 	 */
 	public void dispose() {
 		truncate();
 	}
 	
 	/*
 	 * Attributes:
 	 */
 	
 	private void setLocation(Location loc) {
 		location = loc;
 	}
 	
 	public EntityManager getManager(){
 		return manager;
 	}
 	
 	/**
 	 * Gets the upper left tile of the Entity's location.
 	 * @return The location. Could be "Unknown", if the Entity is not in the Grid.
 	 */
 	public Location getLocation() {
 		return location;
 	}
 	
 	public final void setWidth(int width) {
 		this.width = width;
 	}
 	
 	public final void setHeight(int height) {
 		this.height = height;
 	}
 	
 	public int getWidth() {
 		return width;
 	}
 	
 	public int getHeight() {
 		return width;
 	}
 	
 	/*
 	 * Events:
 	 */
 	
 	/**
 	 * Occurs when the mouse moves over the entity.
 	 * @param x The local x location.
 	 * @param y The local y location.
 	 */
 	public void onMouseMove(int x, int y) {
 	}
 	
 	public void onMove(Location original) {
 		
 	}
 	
 	public void onMouseDown(int x, int y, boolean leftClick) {
 		NavalGame ng = (NavalGame)getManager().getWorld().getGame();
 		ng.getHud().setEntity(this);
 	}
 	
 	public void onHit(Entity attackingEntity) {
 		
 	}
 	
 	public void onAttack(Entity entityBeingAttacked) {
 		
 	}
 	
 	public void onUpdate(long tickTime) {
 		
 	}
 }
