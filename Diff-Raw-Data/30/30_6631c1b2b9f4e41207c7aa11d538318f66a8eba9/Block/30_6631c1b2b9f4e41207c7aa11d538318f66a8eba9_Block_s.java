 /*******************************************************************************
  * Copyright (c) 2012 MCForge.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  ******************************************************************************/
 package net.mcforge.world;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 
 import net.mcforge.server.Server;
 import net.mcforge.world.blocks.*;
 
 
 public abstract class Block implements Serializable {
 	private static final long serialVersionUID = 1L;
 
 	private static Block[] blocks = new Block[] {
 			new Air(),
 			new Aqua(),
 			new Bedrock(),
 			new Black(),
 			new Blue(),
 			new Bookshelf(),
 			new Brick(),
 			new BrownShroom(),
 			new CoalOre(),
 			new Cobblestone(),
 			new Cyan(),
 			new Dirt(),
 			new DoubleStair(),
 			new Glass(),
 			new GoldBlock(),
 			new GoldOre(),
 			new Grass(),
 			new Gravel(),
 			new Gray(),
 			new Green(),
 			new Indigo(),
 			new IronBlock(),
 			new IronOre(),
 			new Lava(),
 			new Leaves(),
 			new Lime(),
 			new Magenta(),
 			new MossyCobblestone(),
 			new Obsidian(),
 			new Orange(),
 			new Pink(),
 			new Purple(),
 			new Red(),
 			new RedFlower(),
 			new RedShroom(),
 			new Sand(),
 			new Sapling(),
 			new Sponge(),
 			new Stair(),
 			new StationaryLava(),
 			new StationaryWater(),
 			new Stone(),
 			new TNT(),
 			new Unknown(),
 			new Violet(),
 			new Water(),
 			new White(),
 			new Wood(),
 			new WoodenPlank(),
 			new Yellow(),
 			new YellowFlower()
 	};
 	
 	private static ArrayList<Block> custom = new ArrayList<Block>();
 	
 	/**
 	 * The ID of the block
 	 */
 	public byte ID;
 	
 	/**
 	 * The name of the block
 	 */
 	public String name;
 	
 	/**
 	 * This method is called when the block is placed
 	 * @param l 
 	 *          The level the block was placed in
 	 * @param index 
 	 */
 	public void onPlace(Level l, int x, int y, int z, Server server) {
 		
 	}
 	
 	/**
 	 * Get the block ID the client will see
 	 * @return 
 	 *        The ID
 	 */
 	public byte getVisableBlock() {
 		return ID;
 	}
 	
 	/**
 	 * This method is called when the block is delete
 	 * @param l 
 	 *          The level the block was deleted in
 	 * @param index 
 	 */
 	public void onDelete(Level l, int x, int y, int z, Server server) {
 		
 	}
 	
 	/**
 	 * Add a custom block to the block list
 	 * @param b 
 	 *         An empty version of the custom block.
 	 */
 	public static void addBlock(Block b) {
 		if (custom.contains(b))
 			return;
 		custom.add(b);
 	}
 	
 	/**
 	 * Remove a custom block from the block list
 	 * @param b
 	 *         The block to remove
 	 */
 	public static void removeBlock(Block b) {
 		if (!custom.contains(b))
 			return;
 		custom.remove(b);
 	}
 	
 	/**
 	 * Initialize a new block
 	 * @param ID 
 	 *           The ID of the block
 	 * @param name 
 	 *            The name of the block
 	 */
 	public Block(byte ID, String name) {
 		this.ID = ID;
 		this.name = name;
 	}
 	
 	/**
 	 * Get a block by ID
 	 * @param ID 
 	 *          The ID of the block to look for
 	 * @return 
 	 *        An empty version of the block with the basic information in it
 	 */
 	public static Block getBlock(byte ID) {
 		for (Block b : blocks) {
 			if (b.ID == ID)
 				return b;
 		}
 		for (Block b : custom) {
 			if (b.ID == ID)
 				return b;
 		}
 		return new Unknown();
 	}
 	
 	/**
 	 * Get a block by name
 	 * @param name 
 	 *            The name of the block to look for
 	 * @return 
 	 *        An empty version of the block with the basic information in it
 	 */
 	public static Block getBlock(String name) {
 		for (Block b : blocks) {
 			if (b.name.equalsIgnoreCase(name))
 				return b;
 		}
		for (Block b : blocks) {
 			if (b.name.equalsIgnoreCase(name))
 				return b;
 		}
 		return new Unknown();
 	}
 
 }
