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
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 
 import com.jpii.navalbattle.game.NavalGame;
 import com.jpii.navalbattle.pavo.Chunk;
 import com.jpii.navalbattle.pavo.Game;
 import com.jpii.navalbattle.pavo.PavoHelper;
 import com.jpii.navalbattle.pavo.World;
 import com.jpii.navalbattle.pavo.gui.MessageBox;
 import com.jpii.navalbattle.pavo.gui.MessageBoxIcon;
 
 public class EntityManager {
 	private byte[][] tileAccessor;
 	//private Tile[][] ent;
 	private World w;
 	private ArrayList<Integer> entityRegister;
 	private ArrayList<Entity> entities;
 	public int battleShipId;
 	int counter = 0;
 	/**
 	 * Creates a new entity manager for the desired world.
 	 * @param w The world to create the entity manager.
 	 */
 	public EntityManager(World w) {
 		this.w = w;
 		entities = new ArrayList<Entity>();
 		//ent = new Tile[PavoHelper.getGameWidth(w.getWorldSize())*2][PavoHelper.getGameHeight(w.getWorldSize())*2];
 		BufferedImage grid = new BufferedImage(50,50,BufferedImage.TYPE_INT_ARGB);
 		Graphics2D g = PavoHelper.createGraphics(grid);
 		g.setColor(Game.Settings.GridColor);
 		g.drawRect(1,1,49,49);
 		try {
 			IndexableImage.populateStore(0, grid);
 		}
 		catch (Exception ex) {}
 		entityRegister = new ArrayList<Integer>();
 		entityRegister.add(0);
 		//System.out.println(Integer.bitCount(IndexableImage.getStoreSize())+"."+Integer.toHexString(IndexableImage.getStoreSize())+"swapspace");
 		tileAccessor = new byte[PavoHelper.getGameWidth(w.getWorldSize())*2][PavoHelper.getGameHeight(w.getWorldSize())*2];
 	}
 	public void update(long ticksPassed) {
 		//System.out.println(Integer.bitCount(IndexableImage.getStoreSize())+"."+Integer.toHexString(IndexableImage.getStoreSize()));
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
 	 * @deprecated
 	 */
 	public void addEntity(Entity e) {
 		if (e == null)
 			return;
 		if (entities.size() >= 25) {
 			MessageBox.show("Warning!","Too many entities added.",
					MessageBoxIcon.Notify, true);
 		}
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
 		//ent[c][r] = t;
 		int x = c/2;
 		int z = r/2;
 		Chunk chunk = w.getChunk(x, z);
 		//chunk.poyching();
 		int rx = c % 2;
 		int rz = r % 2;
 		//System.out.println("cresult="+t.getId().getMutexId());
 		if (rx == 0 && rz == 0)
 			chunk.Tile00 = t;
 		else if (rx != 0 && rz == 0)
 			chunk.Tile10 = t;
 		else if (rx == 0 && rz != 0)
 			chunk.Tile01 = t;
 		else if (rx != 0 && rz != 0)
 			chunk.Tile11 = t;
 		//chunk.
 		//System.out.println("chunk at:" + x + "," + z);
 		chunk.writeBuffer();//needsBufferWrite();
 	}
 	public <T> void setTile(Location loc, Tile<Entity> t) {
 		setTile(loc.getRow(),loc.getCol(),t);
 	}
 	public Tile getTile(Location loc) {
 		return getTile(loc.getRow(),loc.getCol());
 	}
 	public Tile getTile(int r, int c) {
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
 		return tileAccessor[c][r] <= 8;
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
 	public <T> int registerEntity(BufferedImage horizontalImage) {
 		int swap = lastid + 1;
 		for (int w = 0; w < horizontalImage.getWidth() / 50; w++) {
 			BufferedImage ab = PavoHelper.imgUtilFastCrop(horizontalImage, w * 50, 0, 50, 50);
 			IndexableImage.populateStore(new Id(swap,w).getMutexId(), ab);
 		}
 		//System.out.println("registered in system=" + new Id(swap,0).getMutexId());
 		lastid = swap;
 		return lastid;
 	}
 	
 	public final BufferedImage getImage(Tile tile) {
 		if (tile == null)
 			return IndexableImage.getImage(0);
 		return IndexableImage.getImage(tile.getId().getMutexId());
 	}
 	public void gameDoneGenerating() {
 		
 	}
 	/*public void Wj3aI54Fh92Ka3668nf2Oq90oi441nf0JWnf() {
 		
 	}*/
 	/**
 	 * Don't play with this.
 	 * @param snJMkqmd Don't play with this.
 	 * @param cKQK91nm38910JNFEWo Don't play with this.
 	 * @param traKQ91 Don't play with this.
 	 */
 	public void AQms03KampOQ9103nmJMs(int snJMkqmd, int cKQK91nm38910JNFEWo, int traKQ91) {
 		byte b = (byte)(((traKQ91 * 0.4)*100)/108);
 		if (b > 100)
 			b = 100;
 		tileAccessor[cKQK91nm38910JNFEWo][snJMkqmd] = b;//mjMo1091(cKQK91nm38910JNFEWo, traKQ91);
 	}
 	/**
 	 * Get the world instance for the Entity Manager.
 	 * @return
 	 */
 	public World getWorld() {
 		return w;
 	}
 	
 	public Color getTeamColor(int teamId){
 		//System.out.println("Team id is...."+teamId);
 		Color temp = Color.black;
 		if(teamId == 0)
 			temp = Color.darkGray;
 		if(teamId == 1)
 			temp = Color.green;//.darker();
 		if(teamId == 2)
 			temp = Color.red;//.darker();
 		if(teamId == 3)
 			temp = Color.blue;//.darker();
 		return temp;
 	}
 }
