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
 
 package com.jpii.navalbattle.pavo;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 
 import com.jpii.navalbattle.game.Location;
 import com.jpii.navalbattle.game.entity.Entity;
 import com.jpii.navalbattle.game.entity.TankTestEntity;
 import com.jpii.navalbattle.util.FileUtils;
 
 public class EntityManager {
 	int[][] tileAccessor;
 	Entity[][] ent;
 	World w;
 	int counter = 0;
 	BufferedImage grid,humanoid,staticTank;
 	/**
 	 * Creates a new entity manager for the desired world.
 	 * @param w The world to create the entity manager.
 	 */
 	public EntityManager(World w) {
 		this.w = w;
 		ent = new Entity[PavoHelper.getGameWidth(w.getWorldSize())*2][PavoHelper.getGameHeight(w.getWorldSize())*2];
 		tileAccessor = new int[PavoHelper.getGameWidth(w.getWorldSize())*2][PavoHelper.getGameHeight(w.getWorldSize())*2];
 		grid = new BufferedImage(50,50,BufferedImage.TYPE_INT_ARGB);
 		Graphics2D g = PavoHelper.createGraphics(grid);
 		g.setColor(new Color(120,120,120,100));
 		g.drawRect(1,1,49,49);
 		humanoid = FileUtils.getImage("drawable-game/Other/humanmob.png");
 		staticTank = FileUtils.getImage("drawable-game/Other/TankBase.png");
 	}
 	public void update(long ticksPassed) {
 		// Every 8/10ths of a second, perform an update on the tank.
 		if (ticksPassed % 800 == 0) {
 			ArrayList<Entity> ents = findEntities("tank-test");
 			for (int e = 0; e < ents.size(); e++) {
 				TankTestEntity tte = (TankTestEntity) ents.get(e);
 				if (tte != null) {
 					int cr = tte.getLocation().getRow();
 					int cc = tte.getLocation().getCol();
 					int ay = Game.Settings.rand.nextInt(-1,2);
 					int ax = Game.Settings.rand.nextInt(-1,2);
 					if (tte.moveTo(cr+ay,cc+ax))
 						getWorld().forceRender();
 				}
 			}
 		}
 	}
 	/**
 	 * Finds the first occuring entity with the given tag.
 	 * @param tag The tag to search for.
 	 * @return The entity. May return a null value if the entity is not found.
 	 */
 	public Entity findEntity(String tag) {
 		for (int x = 0; x < PavoHelper.getGameWidth(w.getWorldSize())*2; x++) {
 			for (int y = 0; y < PavoHelper.getGameHeight(w.getWorldSize())*2; y++) {
 				if (ent[x][y] != null && ent[x][y].getTag() != null && ent[x][y].getTag().equals(tag))
 					return ent[x][y];
 			}
 		}
 		return null;
 	}
 	/**
 	 * Finds all occuring instances of entities with the given tags.
 	 * @param tag The tag to search for.
 	 * @return The list of entities that were found.
 	 */
 	public ArrayList<Entity> findEntities(String tag) {
 		ArrayList<Entity> es = new ArrayList<Entity>();
 		for (int x = 0; x < PavoHelper.getGameWidth(w.getWorldSize())*2; x++) {
 			for (int y = 0; y < PavoHelper.getGameHeight(w.getWorldSize())*2; y++) {
 				if (ent[x][y] != null && ent[x][y].getTag() != null && ent[x][y].getTag().equals(tag))
 					es.add(ent[x][y]);
 			}
 		}
 		return es;
 	}
 	/**
 	 * Gets the entity at the given row and column.
 	 * @param r The row the entity is at.
 	 * @param c The column the entity is at.
 	 * @return The entity. Will return null if the entity is null, or the location is out of bounds.
 	 */
 	public Entity getEntity(int r, int c) {
 		if (c >= PavoHelper.getGameWidth(w.getWorldSize())*2 ||
 				r >= PavoHelper.getGameHeight(w.getWorldSize())*2 || c < 0 || r < 0)
 			return null;
 		return ent[c][r];
 	}
 	/**
 	 * Moves an entity to another location. Be aware that any entity that was in the new location will be overriden.
 	 * You should now use Entity.moveTo(int r, int c). This method will not be removed, but is now declared deprecated.
 	 * @param cr The current row.
 	 * @param cc The current column.
 	 * @param nr The new row.
 	 * @param nc The new column.
 	 * @return A value indicating whether the operation was sucessful or not.
 	 * @deprecated
 	 */
 	public boolean moveEntity(int cr, int cc, int nr, int nc) {
 		if (cr == nr && cc == nc)
 			return true;
 		if (cr >= PavoHelper.getGameWidth(w.getWorldSize())*2 ||
 				cc >= PavoHelper.getGameHeight(w.getWorldSize())*2 || cc < 0 || cr < 0)
 			return false;
 		if (nr >= PavoHelper.getGameWidth(w.getWorldSize())*2 ||
 				nc >= PavoHelper.getGameHeight(w.getWorldSize())*2 || nc < 0 || nr < 0)
 			return false;
 		try {
 			setEntity(nr,nc,ent[cc][cr]);
 			setEntity(cr,cc,new Entity(this,new Location(cr,cc)));
 		}
 		catch (Throwable throwable) {
 			return false;
 		}
 		getAssociatedChunk(nr,nc).reDrawBuffer();
 		getAssociatedChunk(cr,cc).reDrawBuffer();
 		return true;
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
 	/**
 	 * Sets the entity at the desired location.
 	 * @param r The row the entity should be at.
 	 * @param c The column the entity should be at.
 	 * @param e The entity to replace it with.
 	 */
 	public void setEntity(int r, int c, Entity e) {
 		if (c >= PavoHelper.getGameWidth(w.getWorldSize())*2 ||
 				r >= PavoHelper.getGameHeight(w.getWorldSize())*2 || c < 0 || r < 0)
 			return;
 		e.setLocation(new Location(r,c));
 		ent[c][r] = e;
 		int x = c/2;
 		int z = r/2;
 		Chunk chunk = w.getChunk(x, z);
 		int rx = c % 2;
 		int rz = r % 2;
 		if (rx == 0 && rz == 0)
 			chunk.Tile00 = e;
 		else if (rx != 0 && rz == 0)
 			chunk.Tile10 = e;
 		else if (rx == 0 && rz != 0)
 			chunk.Tile01 = e;
 		else if (rx != 0 && rz != 0)
 			chunk.Tile11 = e;
 		chunk.needsBufferWrite();
 	}
 	/**
 	 * Determines whether the selected tile is filled with water.
 	 * @param r The row of the tile.
 	 * @param c The column of the tile.
 	 * @return
 	 */
 	public boolean isTileFilledWithWater(int r, int c) {
 		return tileAccessor[c][r] == 0;
 	}
 	/**
 	 * Gets the amount of land in the given tile.
 	 * @param r The row of the tile.
 	 * @param c The column of the tile.
 	 * @return
 	 */
 	public int getTilePercentLand(int r, int c) {
 		return tileAccessor[c][r] * 100 / 2500;
 	}
 	public EntityReference getTypeById(int id) {
 		return new EntityReference(counter++,1);
 	}
 	/**
 	 * Gets the image for the type of given entity.
 	 * @param ent The entity to get the image for.
 	 * @return
 	 */
 	public BufferedImage getImage(Entity ent) {
 		if (ent == null)
 			return null;
 		BufferedImage ager = null;
 		switch (ent.getId()) {
 		case 0:
 			ager = grid;
 			break;
 		case 1:
 			ager = humanoid;
 			break;
 		case 0x93AF9B:
 			ager = staticTank;
 			break;
 		}
 		BufferedImage s = ent.getCustomImage();
 		if (s != null)
 			ager = s;
 		return ager;
 	}
 	/**
 	 * Don't play with this.
 	 * @param snJMkqmd Don't play with this.
 	 * @param cKQK91nm38910JNFEWo Don't play with this.
 	 * @param traKQ91 Don't play with this.
 	 */
 	public void AQms03KampOQ9103nmJMs(int snJMkqmd, int cKQK91nm38910JNFEWo, int traKQ91) {
 		tileAccessor[cKQK91nm38910JNFEWo][snJMkqmd] = traKQ91;//mjMo1091(cKQK91nm38910JNFEWo, traKQ91);
 	}
 	/*public boolean mjMo1091(int Tj001, int Uim294) {
 		if (Tj001 == Integer.MAX_VALUE || Uim294 == 0) return Boolean.TRUE;
 		if (Tj001 == Integer.MIN_VALUE || Uim294 == 1) return Boolean.FALSE;
 		return Boolean.FALSE;
 	}*/
 	/**
 	 * Get the world instance for the Entity Manager.
 	 * @return
 	 */
 	public World getWorld() {
 		return w;
 	}
 }
