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
 
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.util.Random;
 
 import maximusvladimir.dagen.*;
 
 import com.jpii.navalbattle.pavo.grid.Entity;
 import com.jpii.navalbattle.pavo.grid.Tile;
 import com.jpii.navalbattle.pavo.io.PavoImage;
 import com.jpii.navalbattle.renderer.Helper;
 import com.jpii.navalbattle.renderer.RenderConstants;
 
 /**
  * The main file dealing with Chunks, which the world depends on for visuals.
  */
 public class Chunk extends Renderable{
 	private static final long serialVersionUID = 1L;
 	int x,z;
 	boolean generated = false;
 	public Tile<Entity> Tile00, Tile10, Tile01,Tile11;
 	//byte Overlay00;
 	public byte Overlay00,Overlay10, Overlay01, Overlay11;
 	public short water00 = 0,water01 = 0,water10 = 0,water11 = 0;
 	static Perlin p = new Perlin(Game.Settings.rand.nextLong(),0,0);
 	static Rand rand = new Rand();
 	World w;
 	BufferedImage terrain;
 	/**
 	 * Creates a new instance of Chunk.
 	 * @param w The active world to apply the chunk to.
 	 */
 	public Chunk(World w) {
 		this.w = w;
 		Tile00 = Tile10 = Tile01 = Tile11 = null;
 		Overlay00 = Overlay10 = Overlay01 = Overlay11 = 0;
 	}
 	/**
 	 * Sets the x-location for the chunk.
 	 * @param x the value
 	 */
 	public void setX(int x) {
 		this.x = x;
 	}
 	/**
 	 * Sets the z-location for the chunk.
 	 * @param z the value 
 	 */
 	public void setZ(int z) {
 		this.z = z;
 	}
 	/**
 	 * Gets the x-location for the chunk.
 	 * @return
 	 */
 	public int getX() {
 		return x;
 	}
 	/**
 	 * Gets the z-location for the chunk.
 	 * @return
 	 */
 	public int getZ() {
 		return z;
 	}
 	/**
 	 * Sets both location values for the chunk.
 	 * @param x The x location value.
 	 * @param z The z location value.
 	 */
 	public void setLoc(int x, int z) {
 		this.x = x;
 		this.z = z;
 	}
 	/**
 	 * Renders the terrain for the chunk.
 	 */
 	public void render() {
 		Random rp = new Random(Game.Settings.seed+(x&z)+x-z+(z|x));
 		rand = new Rand(rp.nextLong());
 		terrain = new BufferedImage(34,34,BufferedImage.TYPE_USHORT_555_RGB);
 		Graphics2D g = (Graphics2D)terrain.getGraphics();
 		for (int lsx = 0; lsx < 100/3; lsx++) {
 			for (int lsz = 0; lsz < 100/3; lsz++) {
 				float frsh = ProceduralLayeredMapGenerator.getPoint(lsx+(100.0f/3.0f*x), lsz+(100.0f/3.0f*z));
 				float lsy = frsh;
 				if (lsy >= 0.4) {
 					if (lsx < 16.6666666666666666 && lsz < 16.666666666666666)
 						water00 += 1;
 					else if (lsx >= 16.666666666666 && lsz < 16.666666666666666)
 						water10 += 1;
 					else if (lsx < 16.666666666666 && lsz >= 16.666666666666666)
 						water01 += 1;
 					else if (lsx >= 16.666666666666 && lsz >= 16.666666666666666)
 						water11 += 1;
 				}
 				if (lsy < 0.4) {
 					int rgs = Helper.colorSnap((int)(lsy*102));
 					int mod = (int)((lsy * 12) / 0.4f);
 					if (mod <= 0) {
 						g.setColor(new Color(Helper.colorSnap(63+rand.nextInt(15)),Helper.colorSnap(60+rand.nextInt(15)),
 								Helper.colorSnap(rand.nextInt(85, 110)+rgs)));
 					}
 					else {
 						g.setColor(new Color(Helper.colorSnap(63+rand.nextInt(-9,7)+mod),Helper.colorSnap(60+rand.nextInt(-9,7))+mod,
 							Helper.colorSnap(rand.nextInt(90, 100)+rgs+mod)));
 					}
 					if (lsy > 0.38 && rand.nextInt(1,15) == 2) {
 						int h = rand.nextInt(200,210);
 						g.setColor(new Color(143,141,h));
 					}
 				}
 				else if (lsy < 0.55) {
 					Color base1 = PavoHelper.Lerp(RenderConstants.GEN_SAND_COLOR,new Color(52,79,13),((lsy-0.42)/0.15));
 					if (lsy < 0.42) {
 							base1 = PavoHelper.Lerp(new Color(199,189,122),RenderConstants.GEN_SAND_COLOR,((lsy-0.40)/0.02));
 					}
 					base1 = Helper.randomise(base1, 8, rand, false);
 					g.setColor(base1);
 				}
 				else{
 					Color pick = new Color(100,92,40);
 					Color base1 = PavoHelper.Lerp(new Color(52,79,13),pick,((lsy-0.55)/0.45));
 					base1 = Helper.randomise(base1, 8, rand, false);
 					g.setColor(base1);
 				}
 				if (lsy >= 0.395f && lsy <= 0.405f) {
 					int rgs = Helper.colorSnap((int)(lsy*102));
 					int mod = (int)((lsy * 12) / 0.4f);
 					Color ocean = (new Color(Helper.colorSnap(63+rand.nextInt(-9,7)+mod),Helper.colorSnap(60+rand.nextInt(-9,7))+mod,
 							Helper.colorSnap(rand.nextInt(90, 100)+rgs+mod)));
 					float interpolate = (lsy - 0.395f) / 0.01f;
 					g.setColor(PavoHelper.Lerp(ocean, new Color(199,189,122), interpolate));
 				}
 				g.drawLine(lsx,lsz,lsx,lsz);
 			}
 		}
 		//g.shear(-0.45f,0);
 		for (int xc = 100/3; xc > 0; xc--) {
 			for (int zc = 100/3; zc > 0; zc--) {
 				float frsh = ProceduralLayeredMapGenerator.getPoint((xc+(100.0f/3.0f*x))*4.0f, (zc+(100.0f/3.0f*z))*4.0f);
 				float lasy = ProceduralLayeredMapGenerator.getPoint(xc+(100.0f/3.0f*x), zc+(100.0f/3.0f*z));
 				if (lasy > 0.4f && frsh > 0.2f && Game.Settings.rand.nextInt(32) == 2) {
 					g.setColor(new Color(120+Game.Settings.rand.nextInt(-25,25),80+Game.Settings.rand.nextInt(-25,25),
 							Game.Settings.rand.nextInt(0,25)));
 					g.drawLine(xc,zc-1,xc,zc+3);
 					Color leaf = PavoHelper.generateNewLeafColor();
 					g.setColor(leaf);
 					
 					g.drawLine(xc,zc-1,xc,zc-1);
 					g.setColor(PavoHelper.generateLeafMod(leaf));
 					g.drawLine(xc-1,zc-1,xc+1,zc-1);
 					g.drawLine(xc,zc-2,xc,zc-2);
 				}
 			}
 		}
 		//g.shear(0.45f,0);
 		w.getEntityManager().AQms03KampOQ9103nmJMs((getZ()*2), (getX()*2), water00);
 		w.getEntityManager().AQms03KampOQ9103nmJMs((getZ()*2)+1, (getX()*2), water01);
 		w.getEntityManager().AQms03KampOQ9103nmJMs((getZ()*2), (getX()*2)+1, water10);
 		w.getEntityManager().AQms03KampOQ9103nmJMs((getZ()*2)+1, (getX()*2)+1, water11);
 		writeBuffer();
 		generated = true;
 	}
 	/**
 	 * Writes a value to the actual buffer.
 	 */
 	public void writeBuffer() {
 		buffer = new PavoImage(100,100,BufferedImage.TYPE_3BYTE_BGR);
 		Graphics2D g = PavoHelper.createGraphics(buffer);
 		g.drawImage(terrain, 0, 0,103,103, null);
 		
 //		g.drawImage(w.getEntityManager().getImage(Tile00), 0, 0, null);
 //		g.drawImage(w.getEntityManager().getImage(Tile10), 50, 0, null);
 //		g.drawImage(w.getEntityManager().getImage(Tile01), 0, 50, null);
 //		g.drawImage(w.getEntityManager().getImage(Tile11), 50, 50, null);
 		if (Overlay00 != 0) {
 			g.setColor(PavoHelper.changeAlpha(PavoHelper.convertByteToColor(Overlay00), 60));
 			g.fillRect(0,0,50,50);
 		}
 		if (Overlay10 != 0) {
 			g.setColor(PavoHelper.changeAlpha(PavoHelper.convertByteToColor(Overlay10), 60));
 			g.fillRect(50,0,50,50);
 		}
 		if (Overlay01 != 0) {
 			g.setColor(PavoHelper.changeAlpha(PavoHelper.convertByteToColor(Overlay01), 60));
 			g.fillRect(0,50,50,50);
 		}
 		if (Overlay11 != 0) {
 			g.setColor(PavoHelper.changeAlpha(PavoHelper.convertByteToColor(Overlay11), 60));
 			g.fillRect(50,50,50,50);
 		}
 		if (Tile00 != null && Tile00.getEntity() != null && Tile00.getEntity().getTeamColor() != -1) {
 //<<<<<<< HEAD
 //			Area as = new Area();
 //			Tile00.getEntity().onTeamColorBeingDrawn(as);
 //=======
 //			//Area as = new Area();
 //			//Tile00.getEntity().onTeamColorBeingDrawn(as);
 //>>>>>>> Team colors.
 			
			g.setColor(PavoHelper.changeAlpha(PavoHelper.convertByteToColor(Tile00.getEntity().getTeamColor()),50));
 			g.fillRect(2,2,47,47);
 			
 			
 		}
 		if (Tile10 != null && Tile10.getEntity() != null && Tile10.getEntity().getTeamColor() != -1) {
 			//Area as = new Area();
 			//Tile10.getEntity().onTeamColorBeingDrawn(as);
			g.setColor(PavoHelper.changeAlpha(PavoHelper.convertByteToColor(Tile10.getEntity().getTeamColor()),50));
 			g.fillRect(52,2,47,47);
 			
 			
 		}
 		if (Tile01 != null && Tile01.getEntity() != null && Tile01.getEntity().getTeamColor() != -1) {
 			//Area as = new Area();
 			//Tile01.getEntity().onTeamColorBeingDrawn(as);
			g.setColor(PavoHelper.changeAlpha(PavoHelper.convertByteToColor(Tile01.getEntity().getTeamColor()),50));
 			g.fillRect(2,52,47,47);
 			
 			
 		}
 		if (Tile11 != null && Tile11.getEntity() != null && Tile11.getEntity().getTeamColor() != -1) {
 			//Area as = new Area();
 			//Tile11.getEntity().onTeamColorBeingDrawn(as);
			g.setColor(PavoHelper.changeAlpha(PavoHelper.convertByteToColor(Tile11.getEntity().getTeamColor()),50));
 			g.fillRect(52,52,47,47);
 			
 			
 		}
 		//g.shear(-0.45f,0);
 		//g.scale(1,1.333333333333333f);
 		g.drawImage(w.getEntityManager().getImage(Tile00), 0, 0, null);
 		g.drawImage(w.getEntityManager().getImage(Tile10), 50, 0, null);
 		g.drawImage(w.getEntityManager().getImage(Tile01), 0, 50, null);
 		g.drawImage(w.getEntityManager().getImage(Tile11), 50, 50, null);
 		//g.shear(0.45f,0);
 		nesa = false;
 		g.dispose();
 		w.chunkrender = true;
 	}
 
 	boolean nesa = false;
 	/**
 	 * Does the buffer need to be rewritten?
 	 * @return
 	 */
 	public boolean needsBufferWrite() {
 		return nesa;
 	}
 	/**
 	 * Redraws the buffer on next cycle.
 	 */
 	public void reDrawBuffer() {
 		nesa = true;
 	}
 	/**
 	 * Has the chunk been generated yet?
 	 * @return
 	 */
 	public boolean isGenerated() {
 		return generated;
 	}
 }
