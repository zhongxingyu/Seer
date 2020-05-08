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
 
 import com.jpii.navalbattle.data.Constants;
 import com.jpii.navalbattle.pavo.grid.Entity;
 import com.jpii.navalbattle.pavo.grid.Tile;
 import com.jpii.navalbattle.pavo.io.PavoImage;
 import com.jpii.navalbattle.renderer.Helper;
 import com.jpii.navalbattle.renderer.RenderConstants;
 
 /**
  * The main file dealing with Chunks, which the world depends on for visuals.
  * @author MKirkby
  *
  */
 public class Chunk extends Renderable{
 	int x,z;
 	boolean generated = false;
 	public Tile<Entity> Tile00, Tile10, Tile01,Tile11;
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
 	public void render() {
 		//if (!ready)
 			//return;
 		//ready = false;
 		Random rp = new Random(Game.Settings.seed+(x&z)+x-z+(z|x));
 		rand = new Rand(rp.nextLong());
 		terrain = new BufferedImage(34,34,BufferedImage.TYPE_USHORT_555_RGB);
 		Graphics g = terrain.getGraphics();
 		int water00 = 0,water01 = 0,water10 = 0,water11 = 0;
 		for (int lsx = 0; lsx < 100/3; lsx++) {
 			for (int lsz = 0; lsz < 100/3; lsz++) {
 				float frsh = ProceduralLayeredMapGenerator.getPoint(lsx+(100.0f/3.0f*x), lsz+(100.0f/3.0f*z));
 				byte slip = ProceduralLayeredMapGenerator.getValidHouse((int)(lsx+(100.0f/3.0f*x)), (int)(lsz+(100.0f/3.0f*z)));
 				float lsy = frsh;
 				int nawo = rand.nextInt(-5, 8);
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
 					g.setColor(new Color(63+rand.nextInt(-7,7),60+rand.nextInt(-7,7),rand.nextInt(90, 100)+rgs));
 					if (lsy > 0.38 && rand.nextInt(1,15) == 2) {
 						int h = rand.nextInt(200,210);
 						int rg2s = Helper.colorSnap((int)(lsy*102));
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
 					//Color start = Helper.adjust(Helper.randomise(RenderConstants.GEN_SAND_COLOR,7
 	                        ///*RenderConstants.GEN_COLOR_DIFF*/, rand, false), ((lsy-0.4)/0.1), 50);
 				}
 				else{
 					Color base1 = PavoHelper.Lerp(new Color(52,79,13),new Color(100,92,40),((lsy-0.55)/0.45));
 					base1 = Helper.randomise(base1, 8, rand, false);
 					g.setColor(base1);
 					/*if (lsy == 1) {
 						Color base = new Color(52,79,13);
 						double actmountain = (lsy - 0.95)/0.05;
 						base = PavoHelper.Lerp(base, new Color(164,133,70), actmountain);
 						base = Helper.randomise(base, 8, rand, false);
 						g.setColor(base);
 					}*/
 					//System.out.println(frsh);
 					//g.setColor(Helper.adjust(Helper.randomise(new Color(40,61,4),
 	                  //      RenderConstants.GEN_COLOR_DIFF, rand, false), ((lsy-0.6)/0.3), 40));
 				}
 				g.drawLine(lsx,lsz,lsx,lsz);
 				if (slip > 0) {
 					g.setColor(Color.red);
 					g.drawRect(lsx-(slip/2),lsz-(slip/2),slip,slip);
 				}
 				//g.fillRect(lsx*3,lsz*3,4,4);
 			}
 		}
 		
 		for (int xc = 100/3; xc > 0; xc--) {
 			for (int zc = 100/3; zc > 0; zc--) {
 				float frsh = ProceduralLayeredMapGenerator.getPoint((xc+(100.0f/3.0f*x))*4.0f, (zc+(100.0f/3.0f*z))*4.0f);
 				float lasy = ProceduralLayeredMapGenerator.getPoint(xc+(100.0f/3.0f*x), zc+(100.0f/3.0f*z));
 				if (lasy > 0.4f && frsh > 0.6f && Game.Settings.rand.nextInt(32) == 2) {
 					g.setColor(new Color(150,100,15));
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
 		w.getEntityManager().AQms03KampOQ9103nmJMs((getZ()*2), (getX()*2), water00);
 		w.getEntityManager().AQms03KampOQ9103nmJMs((getZ()*2)+1, (getX()*2), water01);
 		w.getEntityManager().AQms03KampOQ9103nmJMs((getZ()*2), (getX()*2)+1, water10);
 		w.getEntityManager().AQms03KampOQ9103nmJMs((getZ()*2)+1, (getX()*2)+1, water11);
 		writeBuffer();
 		//ready = true;
 		generated = true;
 	}
 	/**
 	 * Writes a value to the actual buffer.
 	 */
 	public void writeBuffer() {
 		buffer = new PavoImage(100,100,BufferedImage.TYPE_3BYTE_BGR);
 		Graphics2D g = PavoHelper.createGraphics(buffer);
 		g.drawImage(terrain, 0, 0,103,103, null);
 		/*if (Tile00 != null || Tile10 != null || Tile11 != null || Tile01 != null) {
 			//g.drawOval(0,0,33,33);
 		}*/
 		
 		/*if (Tile00!=null){
 			Entity parent = Tile00.getEntity();
 			g.setColor(parent.getManager().getTeamColor(parent.teamId));
 			if(Tile00.getId().getMutexId()==65537)
 				g.fillRect(45, 15, 5, 20);
 			if(Tile00.getId().getMutexId()==65538)
 				g.fillRect(0, 15, 35, 20);
 		}
 		if (Tile01!=null){
 			Entity parent = Tile01.getEntity();
 			g.setColor(parent.getManager().getTeamColor(parent.teamId));
 			if(Tile01.getId().getMutexId()==65537)
 				g.fillRect(45, 65, 5, 20);
 			if(Tile01.getId().getMutexId()==65538)
 				g.fillRect(0, 65, 35, 20);
 		}
 		if (Tile10!=null){
 			Entity parent = Tile10.getEntity();
 			g.setColor(parent.getManager().getTeamColor(parent.teamId));
 			if(Tile10.getId().getMutexId()==65537)
 				g.fillRect(95, 15, 5, 20);
 			if(Tile10.getId().getMutexId()==65538)
 				g.fillRect(50, 15, 35, 20);
 		}
 		if (Tile11!=null ){
 			Entity parent = Tile11.getEntity();
 			g.setColor(parent.getManager().getTeamColor(parent.teamId));
 			if(Tile11.getId().getMutexId()==65537)
 				g.fillRect(95, 65, 5, 20);
 			if(Tile11.getId().getMutexId()==65538)
 				g.fillRect(50, 65, 35, 20);
 		}*/
 		
 		g.drawImage(w.getEntityManager().getImage(Tile00), 0, 0, null);
 		g.drawImage(w.getEntityManager().getImage(Tile10), 50, 0, null);
 		g.drawImage(w.getEntityManager().getImage(Tile01), 0, 50, null);
 		g.drawImage(w.getEntityManager().getImage(Tile11), 50, 50, null);
 		//if (poychingmode) {
 		//	g.setColor(new Color(0,0,255,127));
 		//	g.fillRect(0,0,103,103);
 		//}
 		nesa = false;
 		g.dispose();
 		
 	}
 	
 	//boolean poychingmode = false;
 	//public void poyching() {
 	//	poychingmode = true;
 	//}
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
