 /*******************************************************************************
 * Copyright (c) 2012 MCForge.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
 package net.mcforge.converter;
 
 import java.io.*;
 import java.text.DateFormat;
 import java.util.zip.GZIPOutputStream;
 import java.util.zip.GZIPInputStream;
 
 import net.mcforge.world.Block;
 import net.mcforge.world.Level;
 
 /**
 * This class was taken from the Minecraft Wiki
 * and was slightly modified
 *
 */
 public class DatToGGS {
 	public com.mojang.minecraft.level.Level level = null;
 	private int width;
 	private int height;
 
 	public DatToGGS() {
 	}
 	
 	/**
 	 * Convert a .dat file to a .ggs file
 	 * @param file
 	 *           The file to load and convert
 	 * @return
 	 *        The converted level object
 	 * @throws IOException
 	 *                   An IOException is thrown if there is a problem reading the file
 	 */
 	public static Level convertDAT(String file) throws IOException {
 		String name = new File(file).getName().split("\\.")[0];
 		DatToGGS newlvl = new DatToGGS();
 		newlvl.load(file);
 		Level lvl = new Level((short)newlvl.level.width, (short)newlvl.level.height, (short)newlvl.level.height);
 		int[] cords = new int[3];
 		for (int i = 0; i < newlvl.level.blocks.length; i++) {
 			cords = newlvl.getCoords(i);
 			try {
 				lvl.blocks[lvl.posToInt(cords[0], cords[1], cords[2])] = Block.getBlock(newlvl.level.blocks[i]);
 			} catch (Exception e) {
 				System.out.println(i + "= " + cords[0] + ":" + cords[1] + ":" + cords[2]);
 			}
 		}
 		lvl.name = name;
 		lvl.save();
 		new File(file).delete();
 		return lvl;
 	}
 	
 	public static void convertLevels()
 	{
 		File levelsFolder = new File("levels");
 		File[] levelFiles = levelsFolder.listFiles();
 		for(File f : levelFiles) {
 			if (f.getName().endsWith(".dat")) {
 				try {
 					convertDAT(levelsFolder.getPath() + "/" + f.getName());
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	public void load(String filename) {
 		FileInputStream fis = null;
 		GZIPInputStream gzis = null;
 		ObjectInputStream in = null;
 		DataInputStream inputstream = null;
 		try {
 			fis = new FileInputStream(filename);
 			gzis = new GZIPInputStream(fis);
 			inputstream = new DataInputStream(gzis);
 			if((inputstream.readInt()) != 0x271bb788) {
 				fis.close();
 				gzis.close();
 				inputstream.close();
 				return;
 			}
 			if((inputstream.readByte()) > 2) {
 				System.out.println("Error: Level version > 2, this is unexpected!");
 				fis.close();
 				gzis.close();
 				inputstream.close();
 				return;
 			}
 			in = new ObjectInputStream(gzis);
 			level = (com.mojang.minecraft.level.Level)in.readObject();
 			inputstream.close();
 			in.close();
 			System.out.println("Loading level "+filename+" successful");
 			fis.close();
 			gzis.close();
 		} catch(IOException ex) {
 			ex.printStackTrace();
 		} catch(ClassNotFoundException ex) {
 			ex.printStackTrace();
 		}
 		level.initTransient();
 		width = level.width;
 		height = level.height;
 		}
 
 	// save in file called filename
 	public void save(String filename) {
 		FileOutputStream fos = null;
 		GZIPOutputStream gzos = null;
 		ObjectOutputStream out = null;
 		DataOutputStream outputstream = null;
 		try {
 			fos = new FileOutputStream(filename);
 			gzos = new GZIPOutputStream(fos);
 			outputstream = new DataOutputStream(gzos);
 			outputstream.writeInt(0x271bb788);
 			outputstream.writeByte(2);
 			out = new ObjectOutputStream(gzos);
 			out.writeObject(level);
 			outputstream.close();
 			out.close();
 			System.out.println("Saving level "+filename+" successful");
 		} catch(IOException ex) {
 			ex.printStackTrace();
 		}
 	}
 
 	// prints all there is to know about a level, except for the blocks data
 	public void printInfo() {
 		if (level == null) {
 			return;
 		}
 		System.out.println("Level info:");
 		System.out.println("name: "+level.name);
 		System.out.println("creator: "+level.creator);
 		System.out.println("createTime: "+(DateFormat.getDateTimeInstance(DateFormat.FULL,DateFormat.FULL).format(level.createTime)));
 		System.out.println("width: "+level.width);
 		System.out.println("height: "+level.height);
 		System.out.println("depth: "+level.depth);
 		System.out.println("spawnpoint: ["+level.xSpawn+","+level.ySpawn+","+level.zSpawn+"]");
 		System.out.println("spawn rotation: "+level.rotSpawn);
 	}
 
 	// safe to use method, return value let's you know if anything was changed
 	public boolean setTile(int x, int y, int z, int t) {
 		if (
 			x >=0 && x < level.width &&
 			y >=0 && y < level.depth &&
 			z >=0 && z < level.height &&
 			t >= 0 && t <= 37
 		) {
 			if (t == 8 || t == 10) {
 				level.setTile(x,y,z,t);
 			} else if (t >= 0 && t <= 18) {
 				level.setTileNoUpdate(x,y,z,t);
 			}
 			return true;
 		}
 		return false;
 	}
 
 	// gets you the level coordinates from the blocks array index
 	public int[] getCoords(int index) {
 		int x = index % width;
 		index = (index-x) / width;
 		int z = index % height;
 		int y = (index-z) / height;
 		return new int[] {x, y, z};
 	}
 
 	public void clearBlocks() {
 		for (int i=0; i<level.blocks.length; i++) {
 			level.blocks[i] = 0;
 		}
 	}
 
 	public void floor(int y, int type) {
 		for (int i=0; i<level.width; i++) {
 		for (int j=0; j<level.height; j++) {
 			setTile(i,y,j,type);
 		}
 		}
 	}
 
 	public void wallX(int x1, int x2, int z, int y, int height, int type) {
 		for (int i=x1; i<=x2; i++) {
 		for (int j=y; j<y+height; j++) {
 			if (!setTile(i,j,z,type)) {
 				System.out.println("Warning: a tile got ignored while building a wallX: ["+i+","+j+","+z+"]");
 			}
 		}
 		}
 	}
 
 	public void wallZ(int x, int z1, int z2, int y, int height, int type) {
 		for (int i=z1; i<=z2; i++) {
 		for (int j=y; j<y+height; j++) {
 			if (!setTile(x,j,i,type)) {
 				System.out.println("Warning: a tile got ignored while building a wallZ: ["+x+","+j+","+i+"]");
 			}
 		}
 		}
 	}
 
 	// substitute all of block type 'from' to 'to' :) returning the number of blocks altered
 	public int substitute(byte from, byte to) {
 		int count=0;
 		for (int i=0; i<level.blocks.length; i++) {
 			if (level.blocks[i] == from) {
 				level.blocks[i] = to;
 				count++;
 			}
 		}
 		return count;
 	}
 
 	public void setSize(int x, int y, int z) {
 		level.setData(x, y, z, new byte[x*y*z]);
 	}
 }
 
