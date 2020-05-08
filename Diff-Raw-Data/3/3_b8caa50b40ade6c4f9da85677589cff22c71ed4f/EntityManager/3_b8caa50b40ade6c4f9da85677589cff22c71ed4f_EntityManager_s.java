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
 
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.io.Serializable;
 import java.util.ArrayList;
 
 import com.jpii.navalbattle.pavo.Chunk;
 import com.jpii.navalbattle.pavo.Game;
 import com.jpii.navalbattle.pavo.PavoHelper;
 import com.jpii.navalbattle.pavo.World;
 import com.jpii.navalbattle.pavo.io.PavoImage;
 
 public class EntityManager implements Serializable {
 	private static final long serialVersionUID = 1L;
 	private byte[][] tileAccessor;
 	private transient World w;
 	private ArrayList<Integer> entityRegister;
 	private ArrayList<Entity> entities;
 	public GridedEntityTileOrientation battleShipId;
 	public GridedEntityTileOrientation acarrierId;
 	public GridedEntityTileOrientation submarineId;
 	public GridedEntityTileOrientation submarineUId;
 	int counter = 0;
 	/**
 	 * Creates a new entity manager for the desired world.
 	 * @param w The world to create the entity manager.
 	 */
 	public EntityManager(World w) {
 		this.w = w;
 		tileAccessor = new byte[PavoHelper.getGameWidth(w.getWorldSize())*2][PavoHelper.getGameHeight(w.getWorldSize())*2];
 		entities = new ArrayList<Entity>();
 		PavoImage grid = new PavoImage(50,50,BufferedImage.TYPE_INT_ARGB);
 		Graphics2D g = PavoHelper.createGraphics(grid);
 		g.setColor(Game.Settings.GridColor);
 		g.drawRect(1,1,49,49);
 		try {
 			IndexableImage.populateStore(0, grid);
 		}
 		catch (Exception ex) {}
 		g.dispose();
 		entityRegister = new ArrayList<Integer>();
 		entityRegister.add(0);
 	}
 	public boolean isReadyForGen() {
 		if (tileAccessor == null)
 			return false;
 		return true;
 	}
 	public void update(long ticksPassed) {
 
 	}
 	public Entity getEntity(int index) {
 		return entities.get(index);
 	}
 	public void removeEntity(Entity e) {
 		entities.remove(e);
 	}
 	public void removeEntity(int index) {
 		entities.remove(index);
 	}
 	/**
 	 * DO NOT CALL INDIVIDUALLY
 	 * @param e The entity.
 	 */
 	public void addEntity(Entity e) {
 		if (e == null)
 			return;
 		entities.add(e);
 	}
 	public int getTotalEntities() {
 		return entities.size();
 	}
 	/**
 	 * Gets the chunk associated with the given entity location.
 	 * @param r The row of the entity.
 	 * @param c The column of the entity.
 	 * @return The chunk. Will return null if the desired location is out of bounds.
 	 */
 	public Chunk getAssociatedChunk(int r, int c) {
 		if (c >= PavoHelper.getGameWidth(w.getWorldSize())*2 ||
 				r >= PavoHelper.getGameHeight(w.getWorldSize())*2 || c < 0 || r < 0)
 			return null;
 		
 		return w.getChunk(c/2,r/2);
 	}
 	public Chunk getAssociatedChunk(Location loc) {
 		if (loc.getCol() >= PavoHelper.getGameWidth(w.getWorldSize())*2 ||
 				loc.getRow() >= PavoHelper.getGameHeight(w.getWorldSize())*2 || loc.getCol() < 0 || loc.getRow() < 0)
 			return null;
 		
 		return w.getChunk(loc.getCol()/2,loc.getRow()/2);
 	}
 	/**
 	 * Sets the entity at the desired location.
 	 * @param <T>
 	 * @param r The row the entity should be at.
 	 * @param c The column the entity should be at.
 	 * @param e The entity to replace it with.
 	 */
 	public void setTile(int r, int c, Tile<Entity> t) {
 		if (c >= PavoHelper.getGameWidth(w.getWorldSize())*2 ||
 				r >= PavoHelper.getGameHeight(w.getWorldSize())*2 || c < 0 || r < 0)
 			return;
 		int x = c/2;
 		int z = r/2;
 		Chunk chunk = w.getChunk(x, z);
 		int rx = c % 2;
 		int rz = r % 2;
 		if (rx == 0 && rz == 0)
 			chunk.Tile00 = t;
 		else if (rx != 0 && rz == 0)
 			chunk.Tile10 = t;
 		else if (rx == 0 && rz != 0)
 			chunk.Tile01 = t;
 		else if (rx != 0 && rz != 0)
 			chunk.Tile11 = t;
 		chunk.writeBuffer();
 	}
 	public void setTileOverlay(int r, int c, byte color) {
 		if (c >= PavoHelper.getGameWidth(w.getWorldSize())*2 ||
 				r >= PavoHelper.getGameHeight(w.getWorldSize())*2 || c < 0 || r < 0)
 			return;
 		int x = c/2;
 		int z = r/2;
 		Chunk chunk = w.getChunk(x, z);
 		int rx = c % 2;
 		int rz = r % 2;
 		if (rx == 0 && rz == 0)
 			chunk.Overlay00 = color;
 		else if (rx != 0 && rz == 0)
 			chunk.Overlay10 = color;
 		else if (rx == 0 && rz != 0)
 			chunk.Overlay01 = color;
 		else if (rx != 0 && rz != 0)
 			chunk.Overlay11 = color;
 		chunk.writeBuffer();
 	}
 	public Entity findEntity(int r, int c) {
 		if (r >= 0 && c >= 0) {
			Chunk chuck = w.getChunk(c/2,r/2);
 			if (chuck == null)
 				return null;
 			if (chuck.Tile00 != null && chuck.Tile00.parent != null)
 				return chuck.Tile00.parent;
 			else if (chuck.Tile10 != null && chuck.Tile10.parent != null)
 				return chuck.Tile10.parent;
 			else if (chuck.Tile01 != null && chuck.Tile01.parent != null)
 				return chuck.Tile01.parent;
 			else if (chuck.Tile11 != null && chuck.Tile11.parent != null)
 				return chuck.Tile11.parent;
 			else {
 				return null;
 			}
 		}
 		return null;
 	}
 	public <T> void setTile(Location loc, Tile<Entity> t) {
 		setTile(loc.getRow(),loc.getCol(),t);
 	}
 	public Tile<Entity> getTile(Location loc) {
 		return getTile(loc.getRow(),loc.getCol());
 	}
 	public Tile<Entity> getTile(int r, int c) {
 		if (c >= PavoHelper.getGameWidth(w.getWorldSize())*2 ||
 				r >= PavoHelper.getGameHeight(w.getWorldSize())*2 || c < 0 || r < 0)
 			return null;
 		int x = c/2;
 		int z = r/2;
 		Chunk chunk = w.getChunk(x, z);
 		if (chunk == null)
 			return null;
 		int rx = c % 2;
 		int rz = r % 2;
 		if (c == 0)
 			rx = 0;
 		if (c == 1)
 			rx = 1;
 		if (r == 0)
 			rz = 0;
 		if (r == 1)
 			rz = 1;
 		if (rx == 0 && rz == 0)
 			return chunk.Tile00;
 		else if (rx != 0 && rz == 0)
 			return chunk.Tile10;
 		else if (rx == 0 && rz != 0)
 			return chunk.Tile01;
 		else if (rx != 0 && rz != 0)
 			return chunk.Tile11;
 		return null;
 		
 	}
 	/**
 	 * Determines whether the selected tile is filled with water.
 	 * @param r The row of the tile.
 	 * @param c The column of the tile.
 	 * @return
 	 */
 	public boolean isTileFilledWithWater(int r, int c) {
 		if (r < 0 || r >= PavoHelper.getGameHeight(w.getWorldSize())*2 || c < 0 || c >= PavoHelper.getGameWidth(w.getWorldSize())*2)
 			return false;
 		return tileAccessor[c][r] <= Game.Settings.waterThresholdBarrier;
 	}
 	/**
 	 * Gets the amount of land in the given tile.
 	 * @param r The row of the tile.
 	 * @param c The column of the tile.
 	 * @return
 	 */
 	public int getTilePercentLand(int r, int c) {
 		if (r < 0 || c < 0 || r >= PavoHelper.getGameHeight(getWorld().getWorldSize())*2
 				|| c >= PavoHelper.getGameWidth(getWorld().getWorldSize())*2)
 			return 0;
 		return tileAccessor[c][r];
 	}
 	public static int lastid = 0;
 	public <T> int registerEntity(BufferedImage horizontalImage,byte orientation) {
 		int swap = lastid + 1;
 		if (orientation == GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT) {
 			for (int w = 0; w < horizontalImage.getWidth() / 50; w++) {
 				BufferedImage ab = PavoHelper.imgUtilFastCrop(horizontalImage, w * 50, 0, 50, 50);
 				IndexableImage.populateStore(new Id(swap,w).getMutexId(), ab);
 			}
 		}
 		else if (orientation == GridedEntityTileOrientation.ORIENTATION_TOPTOBOTTOM) {
 			for (int h = 0; h < horizontalImage.getHeight() / 50; h++) {
 				BufferedImage ab = PavoHelper.imgUtilFastCrop(horizontalImage,0, h * 50, 50, 50);
 				IndexableImage.populateStore(new Id(swap,h).getMutexId(), ab);
 			}
 		}
 		lastid = swap;
 		return lastid;
 	}
 	/**
 	 * Finds all non-null entities of a particular type.
 	 * Use it like:
 	 * <code>
 	 * BattleShip[] bs = getEntityManager().<BattleShip>findEntities();
 	 * </code>
 	 * @return
 	 */
 	public <T> T[] findEntities() {
 		ArrayList<T> ern = new ArrayList<T>();
 		for (int c = 0; c < getTotalEntities(); c++) {
 			Entity ef = getEntity(c);
 			if (ef != null) {
 				T tcast = null;
 				try {
 					tcast = (T)ef;
 				}
 				catch (Throwable t) {
 					
 				}
 				if (tcast != null)
 					ern.add(tcast);
 			}
 		}
 		return (T[])ern.toArray();
 	}
 	public Entity findEntity(String tag) {
 		for (int c = 0; c < getTotalEntities(); c++) {
 			Entity ef = getEntity(c);
 			if (ef != null && ef.getTag().equals(tag)) {
 				return ef;
 			}
 		}
 		return null;
 	}
 	public Entity[] findEntities(int id) {
 		ArrayList<Entity> ferns = new ArrayList<Entity>();
 		for (int c = 0; c < getTotalEntities(); c++) {
 			Entity ef = getEntity(c);
 			if (ef != null && ef.getCurrentId() == id) {
 				ferns.add(ef);
 			}
 		}
 		return (Entity[])ferns.toArray();
 	}
 	public final BufferedImage getImage(Tile<Entity> tile) {
 		if (tile == null)
 			return IndexableImage.getImage(0);
 		return IndexableImage.getImage(tile.getId().getMutexId());
 	}
 	public void gameDoneGenerating() {
 		
 	}
 	
 	/**
 	 * Don't play with this.
 	 * @param snJMkqmd Don't play with this.
 	 * @param cKQK91nm38910JNFEWo Don't play with this.
 	 * @param traKQ91 Don't play with this.
 	 */
 	public void AQms03KampOQ9103nmJMs(int snJMkqmd, int cKQK91nm38910JNFEWo, int traKQ91) {
 		byte b = (byte)((traKQ91 * 100)/272);
 		if (b > 100)
 			b = 100;
 		if (b < 0)
 			b = 0;
 		if (b == 94)
 			b = 100;
 		if (tileAccessor == null)
 			tileAccessor = new byte[PavoHelper.getGameWidth(w.getWorldSize())*2][PavoHelper.getGameHeight(w.getWorldSize())*2];
 			
 		tileAccessor[cKQK91nm38910JNFEWo][snJMkqmd] = b;
 	}
 	/**
 	 * Get the world instance for the Entity Manager.
 	 * @return
 	 */
 	public World getWorld() {
 		return w;
 	}
 }
