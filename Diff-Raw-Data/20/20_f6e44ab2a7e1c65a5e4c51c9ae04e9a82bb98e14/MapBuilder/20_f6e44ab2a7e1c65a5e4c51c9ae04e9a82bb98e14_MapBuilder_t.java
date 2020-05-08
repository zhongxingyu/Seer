 /**
  * 
  * MapBuilder - draws a top down map of a region to PNG files
  * 
  * Copyright (C) 2011 Chris Poole chris@hackernet.co.uk
  * See README for BSD license.
  *
  * 2011.03.04 - Initial revision
  * 
  */
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.awt.image.RenderedImage;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import javax.imageio.ImageIO;
 
 
 public class MapBuilder {
 	RegionEditor re;
 	
	String mapPrefix;
	
 	byte locateBlock;
 	
 	public void setRegionEditor(RegionEditor red) {
 		re = red;
 	}
 	
	public void setMapPrefix(String prefix) {
		mapPrefix = prefix;
	}
	
 	public Color getMapBlockColor(byte b) {
 		Color d = new Color(0, 0, 0); // default colour
 		
 		switch(b) {
 		case  0: return new Color(200, 200, 200);	// Air
 		case  1: return new Color(128, 128, 128);	// Stone
 		case  2: return new Color( 40, 128,  40); 	// Grass
 		case  3: return new Color(128,  64,   0);	// Dirt
 		case  4: return new Color( 40,  40,  40);	// Cobblestone
 		case  5: return new Color(220, 178,  90);	// Wooden Plank
 		case  6: return new Color(220, 178,  90);	// Sapling
 		case  7: return new Color(  0,   0,   0);	// Bedrock
 		case  8: return new Color(  0,   0, 255);	// Water D
 		case  9: return new Color(  0,   0, 255);	// Stationary water D
 		case 10: return new Color(236,  95,   0); 	// Lava D
 		case 11: return new Color(236,  95,   0); 	// Stationary lava D
 		case 12: return new Color(248, 248, 194);	// Sand
 		case 13: return new Color(182, 154, 154);	// Gravel
 		case 14: return d;	// Gold Ore
 		case 15: return d;	// Iron Ore
 		case 16: return new Color( 20,  20,  20);	// Coal Ore
 		case 17: return new Color(220, 178,  90);	// Wood
 		case 18: return new Color( 64, 192,  64);	// Leaves D
 		case 19: return d;	// Sponge
 		case 20: return d;	// Glass
 		case 21: return d;	// Lapis Lazuli Ore
 		case 22: return d;	// Lapis Lazuli Block
 		case 23: return d;	// Dispenser
 		case 24: return new Color(247, 247, 179);	// Sandstone
 		case 25: return d;	// Note Block
 		case 26: return d;	// Bed
 		case 35: return new Color(255, 255, 255);	// Wool
 		case 37: return new Color(255, 255,   0);	// Yellow Flower
 		case 38: return new Color(255,   0,   0);	// Red Rose
 		case 39: return d;	// Brown Mushroom
 		case 40: return new Color(255,   0,   0);	// Red Mushroom
 		case 41: return d;	// Gold Block
 		case 42: return d;	// Iron Block
 		case 43: return new Color(128, 128, 128);	// Double Slab
 		case 44: return new Color(128, 128, 128);	// Slab
 		case 45: return new Color(206,   0,   0);	// Brick Block
 		case 46: return new Color(255,   0,   0);	// TNT
 		case 47: return new Color(220, 178,  90);	// Bookshelf
 		case 48: return d;	// Moss Stone
 		case 49: return new Color( 64,   0,  64);	// Obsidian
 		case 50: return d;	// Torch D
 		case 51: return d;	// Fire
 		case 52: return d;	// Monster Spawner
 		case 53: return new Color(220, 178, 90);	// Wooden Stairs D
 		case 54: return d;	// Chest
 		case 55: return d;	// Redstone Wire
 		case 56: return d;	// Diamond Ore
 		case 57: return d;	// Diamond Block
 		case 58: return d;	// Crafting Table
 		case 59: return new Color(176, 236, 38);	// Crops
 		case 60: return d;	// Farmland
 		case 61: return d;	// Furnace
 		case 62: return d;	// Burning Furnace
 		case 63: return new Color(220, 178, 90);	// Sign Post
 		case 64: return new Color(200, 158, 75);	// Wooden Door
 		case 65: return d;	// Ladder
 		case 66: return new Color(180, 180, 180);	// Rails
 		case 67: return new Color(120, 120, 120);	// Cobblestone Stairs
 		case 68: return new Color(220, 178, 90);	// Wall Sign
 		case 69: return new Color(128, 128, 128);	// Lever
 		case 70: return new Color(128, 128, 128);	// Stone Pressure Plate
 		case 71: return new Color(128, 128, 128);	// Iron Door
 		case 72: return new Color(220, 178, 90);	// Wooden Pressure Plate
 		case 73: return d;	// Redstone Ore
 		case 74: return d;	// Glowing Redstone Ore
 		case 75: return d;	// Redstone Torch ("off" state)
 		case 76: return d;	// Redstone Torch ("on" state)
 		case 77: return new Color(128, 128, 128);	// Stone Button
 		case 78: return new Color(255, 255, 255);	// Snow
 		case 79: return new Color(206, 206, 255);	// Ice
 		case 80: return new Color(255, 255, 255);	// Snow Block
 		case 81: return new Color(  0,  64,   0);	// Cactus
 		case 82: return new Color( 82,  82,  82);	// Clay Block
 		case 83: return new Color(180, 255, 180);	// Sugar Cane I
 		case 84: return new Color(  0,   0,   0);	// Jukebox
 		case 85: return new Color(220, 178,  90);	// Fence
 		case 86: return new Color(255, 242,   0);	// Pumpkin
 		case 87: return new Color(194,  58,   0);	// Netherrack
 		case 88: return d;	// Soul Sand
 		case 89: return new Color(255, 235, 129);	// Glowstone Block
 		case 90: return new Color(128,   0, 128);	// Portal
 		case 91: return new Color(255, 242,   0);	// Jack-O-Lantern
 		case 92: return new Color(255, 174, 255);	// Cake Block
 		case 93: return d;	// Redstone Repeater ("off" state)
 		case 94: return d;	// Redstone Repeater ("on" state)
 		}
 
 		return d;
 	}
 	
 	public void writeHTML(int minX, int maxX, int minZ, int maxZ) {
 		try {
		    BufferedWriter out = new BufferedWriter(new FileWriter(mapPrefix + "map.html"));
 		    
 		    out.write("<html>\r\n<head>\r\n<style>\r\nimg { border: none; padding: 0px; margin: 0px; }\r\ndiv { white-space: nowrap; }\r\n</style>\r\n</head>\r\n<body>\r\n<div>\r\n");
 		    
	    	for (int z = minZ; z <= maxZ; z++) {
 	    		for (int x = minX; x <= maxX; x++) {
		    		out.write("<img src=\"" + mapPrefix + "r." + x + "." + z + ".mcr.png\">\r\n");
 		    	}
 		    	out.write("<br>\r\n");
 		    }
 		    out.write("</div>\r\n</body>\r\n</html>\r\n");
 		    out.close();
 		} catch (IOException e) {
 		}
 	}
 	
 	public void drawMap(byte locateBlock) {
 		int rgnSize = RegionEditor.chunksize * RegionEditor.regionsize;
 
 		// Create a buffered image in which to draw
 		BufferedImage bufferedImage = new BufferedImage(rgnSize, rgnSize, BufferedImage.TYPE_INT_RGB);
 
 		// Create a graphics contents on the buffered image
 		Graphics2D g2d = bufferedImage.createGraphics();
 
 		// Draw graphics
 		g2d.setColor(Color.black);
 		g2d.fillRect(0, 0, rgnSize, rgnSize);
 
 		int x,y,z;
 		byte b; // block type
 
 		int cx = 0, cz = 0;
 		
 		Color highlightColor = new Color(255, 242, 0); // bright yellow
 		
 		// Work through the chunks in a region
 		for (cz = 0; cz < RegionEditor.regionsize; cz++) {
 			for (cx = 0; cx < RegionEditor.regionsize; cx++) {
 				if (re.hasChunk(cx, cz)) {
 					
 					// Work through the blocks in a chunk
 					for (x = cx * RegionEditor.chunksize; x < (cx + 1) * RegionEditor.chunksize; x++) {
 						for (z = cz * RegionEditor.chunksize; z < (cz + 1) * RegionEditor.chunksize; z++) {
 							
 							// Search down the column for the top non-air block, starting from the lowest full light block
 							for (y = re.getHeight(x, z); y >= 0; y--) {
 								b = re.get(x,y,z);
 								if (b != 0) {
 									if (locateBlock == 0) {
 										// normal map mode
 										g2d.setColor(getMapBlockColor(b));
 										g2d.fillRect(x, z, 1, 1);
 									} else {
 										// map highlighter mode - highlights one block type on a darkened map
 										boolean p; // locateBlock present flag
 										
 										g2d.setColor(getMapBlockColor(b).darker().darker()); // use a dim map
 										g2d.fillRect(x, z, 1, 1);
 										
 										p = false; // clear flag for locateBlock
 										
 										// Search for locateBlock
 										for (; (p == false) && (y > 0); y-- ) {
 											if (re.get(x, y, z) == locateBlock)
 												p = true;
 										}
 	
 										if (p) {
 											g2d.setColor(highlightColor);
 											g2d.fillRect(x, z, 1, 1);
 										}
 									}
 
 									y = -1; // exit the column search
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 
 		g2d.dispose();
 
 		// Create an image to save
 		RenderedImage rendImage = bufferedImage;
 
 		// Write generated image to a file
 		try {
 			// Save as PNG
			File file = new File(mapPrefix + re.regionString + ".png");
 			ImageIO.write(rendImage, "png", file);
 		} catch (IOException e) {
 		}
 	}
 
 }
