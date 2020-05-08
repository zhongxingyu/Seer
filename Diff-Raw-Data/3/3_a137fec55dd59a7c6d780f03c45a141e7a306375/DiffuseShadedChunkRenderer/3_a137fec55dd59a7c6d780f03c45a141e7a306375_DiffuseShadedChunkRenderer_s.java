 package net.nabaal.majiir.realtimerender.rendering;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 import net.nabaal.majiir.realtimerender.Coordinate;
 import net.nabaal.majiir.realtimerender.image.ChunkRenderer;
 import net.nabaal.majiir.realtimerender.image.ImageProvider;
 
 import org.bukkit.ChunkSnapshot;
 import org.bukkit.Material;
 import org.jscience.mathematics.vector.Float64Vector;
 
 public class DiffuseShadedChunkRenderer implements ChunkRenderer {
 	
 	private static BufferedImage grassColor = null;
 	private static BufferedImage waterColor = null;
 	private static BufferedImage foliageColor = null;
 	private final NormalMap normalMap;
 	private final ImageProvider imageProvider;
 	
 	public DiffuseShadedChunkRenderer(ImageProvider imageProvider, NormalMap normalMap) {		
 		try {
 			grassColor = ImageIO.read(getClass().getResource("/images/grasscolor.png"));
 			waterColor = ImageIO.read(getClass().getResource("/images/watercolor.png"));
 			foliageColor = ImageIO.read(getClass().getResource("/images/foliagecolor.png"));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		this.normalMap = normalMap;
 		this.imageProvider = imageProvider;
 	}
 
 	@Override
 	public void render(ChunkSnapshot chunkSnapshot) {
 		BufferedImage image = ImageProvider.createImage(Coordinate.SIZE_CHUNK);
 		Graphics2D g = (Graphics2D) image.getGraphics();
 		for (int x = 0; x < 16; x++) {
 			for (int z = 0; z < 16; z++) {
 				int ymax = chunkSnapshot.getHighestBlockYAt(x, z);
 				// TODO: Handle cases where we can start at an opaque non-terrain block
 				for (int y = getTerrainHeight(chunkSnapshot, x, z); y <= ymax; y++) {
 					Color color;
 					Material material = Material.getMaterial(chunkSnapshot.getBlockTypeId(x, y, z));
 					color = getMaterialColor(material, chunkSnapshot, x, y, z);
 					g.setColor(color);
 					g.fillRect(x, z, 1, 1);
 					
 					if (TerrainHelper.isTerrain(material)) {
 						double shading = computeDiffuseShading(chunkSnapshot, x, z);
 						if (shading >= 0) {
 							g.setColor(computeShadeColor(shading));
 							g.fillRect(x, z, 1, 1);
 						}
 					}
 				}
 			}
 		}
 		imageProvider.setImage(Coordinate.fromSnapshot(chunkSnapshot), image);
 	}
 	
 	public static Color getMaterialColor(Material material, ChunkSnapshot chunkSnapshot, int x, int y, int z) {
 		
 		Color color = getSimpleMaterialColor(material);
 		
 		if (color == null) {
 		
 			switch (material) {
 			case LEAVES:
 				color = computeShadedColor(setAlpha(new Color(getBiomeFoliageColor(chunkSnapshot, x, y, z)), 96), 0.55);
 				break;
 			case GRASS:
 				color = computeShadedColor(new Color(getBiomeGrassColor(chunkSnapshot, x, z)), 0.9);
 				break;
 			case STATIONARY_WATER:
 				color = computeShadedColor(setAlpha(new Color(getBiomeWaterColor(chunkSnapshot, x, z)), 192), 0.7);
 				if (isUnderWater(chunkSnapshot, x, y, z)) {
 					color = setAlpha(color, 32);
 				}
 				break;
 			case LONG_GRASS:
 				color = setAlpha(computeShadedColor(new Color(getBiomeGrassColor(chunkSnapshot, x, z)), 0.5), 96);
 				break;
 			case WOOL:
 				int colorVal = chunkSnapshot.getBlockData(x, y, z) & 0xF;
 				switch (colorVal) {
 				case 0: color = new Color(0xdcdcdc); break; // white
 				case 1: color = new Color(0xe77e34); break; // orange
 				case 2: color = new Color(0xc050c8); break; // magenta
 				case 3: color = new Color(0x6084c2); break; // light blue
 				case 4: color = new Color(0xbdb520); break; // yellow
 				case 5: color = new Color(0x43b428); break; // lime
 				case 6: color = new Color(0xcf7a95); break; // pink
 				case 7: color = new Color(0x424545); break; // gray
 				case 8: color = new Color(0x9da3a3); break; // light gray
 				case 9: color = new Color(0x2b729d); break; // cyan
 				case 10: color = new Color(0x7335c2); break; // purple
 				case 11: color = new Color(0x2a379b); break; // blue
 				case 12: color = new Color(0x5f3a23); break; // brown
 				case 13: color = new Color(0x3a5a29); break; // green
 				case 14: color = new Color(0x9f2f28); break; // red
 				case 15: color = new Color(0x241819); break; // black
 				}
 			default:
 				//RealtimeRender.getLogger().warning(String.format("RealtimeRender: missing color for material '%s'!", material.toString()));
 				color = new Color(0xFF00FF);
 			}
		
 		}
 		
 		return color;
 	}
 	
 	private static Color getSimpleMaterialColor(Material material) {
 		switch (material) {
 			case AIR:			return new Color(0, 0, 0, 0);
 			case STONE:			return new Color(128, 132, 136);
 			case VINE:			return new Color(0, 0xDD, 0, 32);
 			case WATER_LILY:	return new Color(0, 0xDD, 0, 32);
 			case YELLOW_FLOWER:	return new Color(0xDD, 0xDD, 0, 192);
 			case DIRT:				return new Color(134, 96, 67);
 			case COBBLESTONE: 		return new Color(100, 100, 100);
 			case WOOD:				return new Color(157, 128, 79);
 			case SAPLING:			return new Color(120, 205, 120, 64);
 			case BEDROCK: 			return new Color(84, 84, 84);
 			case STATIONARY_LAVA:
 			case LAVA:				return new Color(255, 108, 16);
 			case SAND:				return new Color(218, 210, 158);
 			case GRAVEL:			return new Color(136, 126, 126);
 			case GOLD_ORE:			return new Color(143, 140, 125);
 			case IRON_ORE:			return new Color(136, 130, 127);
 			case COAL_ORE:			return new Color(115, 115, 115);
 			case LOG: 				return new Color(102,81,51);
 			case LAPIS_ORE:			return new Color(102,112,134,255);
 			case LAPIS_BLOCK:		return new Color(29,71,165,255);
 			case DISPENSER: 		return new Color(107,107,107,255);
 			case SANDSTONE: 		return new Color(218,210,158,255);
 			case NOTE_BLOCK: 		return new Color(100,67,50,255);
 			case GOLD_BLOCK: 		return new Color(255,237,140,255);
 			case IRON_BLOCK: 		return new Color(217,217,217,255);
 			case DOUBLE_STEP: 		return new Color(200,200,200,255);
 			case STEP: 				return new Color(200,200,200,255);
 			case BRICK: 			return new Color(86,35,23,255);   
 			case TNT: 				return new Color(255,0,0,255);
 			case BOOKSHELF:		return new Color(191,169,116,255);
 			case MOSSY_COBBLESTONE: return new Color(127,174,125,255);
 			case OBSIDIAN: return new Color(17,13,26,255);   
 			case TORCH: return new Color(255,225,96,208); 
 			case FIRE: return new Color(224,174,21,255); 
 			case WOOD_STAIRS: return new Color(191,169,116,255);
 			case CHEST: return new Color(191,135,2,255);
 			case REDSTONE_WIRE: return new Color(111,1,1,255);    
 			case DIAMOND_ORE: return new Color(129,140,143,255);
 			case DIAMOND_BLOCK: return new Color(45,166,152,255); 
 			case WORKBENCH: return new Color(169,107,0,255);  
 			case CROPS: return new Color(144,188,39,128); 
 			case SOIL: return new Color(134,96,67,255);  
 			case FURNACE: return new Color(188,188,188,255);
 			case BURNING_FURNACE: return new Color(221,221,221,255); 
 			case RAILS: return new Color(120,120,120,128);
 			case COBBLESTONE_STAIRS: return new Color(120,120,120,128);
 			case STONE_PLATE: return new Color(120,120,120,255);
 			case REDSTONE_ORE: return new Color(143,125,125,255);
 			case GLOWING_REDSTONE_ORE: return new Color(163,145,145,255);
 			case REDSTONE_TORCH_OFF: return new Color(181,140,64,32);  
 			case REDSTONE_TORCH_ON: return new Color(255,0,0,176);    
 			case STONE_BUTTON: return new Color(128,128,128,16); 
 			case SNOW: return new Color(245,245,245,255);
 			case ICE: return new Color(150,192,255,150);
 			case SNOW_BLOCK: return new Color(205,205,205,255);
 			case CACTUS: return new Color(85,107,47,255);  
 			case CLAY: return new Color(144,152,168,255);
 			case SUGAR_CANE_BLOCK: return new Color(193,234,150,255);
 			case JUKEBOX: return new Color(125,66,44,255);  
 			case FENCE: return new Color(88,54,22,200);   
 			case PUMPKIN: return new Color(227,144,29,255); 
 			case NETHERRACK: return new Color(194,115,115,255);
 			case SOUL_SAND: return new Color(121,97,82,255);  
 			case GLOWSTONE: return new Color(255,188,94,255); 
 			case PORTAL: return new Color(60,13,106,127);  
 			case JACK_O_LANTERN: return new Color(227,144,29,255); 
 			case CAKE_BLOCK: return new Color(228,205,206,255);
 			case RED_ROSE: return new Color(111,1,1,255);
 			case SMOOTH_BRICK: return new Color(128, 132, 136);
 			case SMOOTH_STAIRS: return new Color(128, 132, 136);
 			case MELON_BLOCK: return new Color(153,194,110,255);
 			case MELON_STEM: return new Color(102,81,51,64);
 			case PUMPKIN_STEM: return new Color(102,81,51,64);
 			case MYCEL: return new Color(0x7b6e83);
 			case HUGE_MUSHROOM_1: return new Color(111,1,1,255);
 			case HUGE_MUSHROOM_2: return new Color(102,81,51);
 			case DEAD_BUSH: return new Color(157, 128, 79, 70);
 			
 			default: return null;
 		}
 	}
 
 	private static boolean isUnderWater(ChunkSnapshot chunkSnapshot, int x, int y, int z) {
 		return Material.getMaterial(chunkSnapshot.getBlockTypeId(x, y + 1, z)) == Material.STATIONARY_WATER;
 	}
 	
 	public static Color setAlpha(Color color, int a) {
 		return new Color(color.getRed(), color.getGreen(), color.getBlue(), a);
 	}
 	
 	public static Color computeShadeColor(double shading) {
 		if (shading < 0.5) {
 			return new Color(0, 0, 0, (int) Math.floor((1 - (shading * 2)) * 255));
 		} else if (shading < 1.0) {
 			return new Color(255, 255, 255, (int) Math.floor(((shading * 2) - 1) * 255));
 		} else {
 			return new Color(255, 255, 255, 255);
 		}
 	}
 	
 	public static Color computeShadedColor(Color color, double multiplier) {		
 		int r = color.getRed();
 		int g = color.getGreen();
 		int b = color.getBlue();
 		
 		if (multiplier < 1) {
 			r *= multiplier;
 			g *= multiplier;
 			b *= multiplier;	
 		}
 		
 		r *= multiplier;
 		g *= multiplier;
 		b *= multiplier;
 	
 		r = Math.min(r, 255);
 		g = Math.min(g, 255);
 		b = Math.min(b, 255);
 		
 		r = Math.max(r, 0);
 		g = Math.max(g, 0);
 		b = Math.max(b, 0);
 		
 		return new Color(r, g, b, color.getAlpha());
 	}
 	
 	
 	public double computeDiffuseShading(ChunkSnapshot chunkSnapshot, int x, int z) {	
 		Float64Vector n = computeTerrainNormal(chunkSnapshot, x, z);
 		Float64Vector light = Float64Vector.valueOf(-1, -1, -1);
 		if (n == null) {
 			return -1;
 		}
 		double shading = n.times(light).divide((n.norm().times(light.norm()))).doubleValue();
 		return ((shading + 1) * 0.4) + 0.15;
 	}
 	
 	
 	public Float64Vector computeTerrainNormal(ChunkSnapshot chunkSnapshot, int x, int z) {
 		Coordinate chunk = Coordinate.fromSnapshot(chunkSnapshot);
 		return this.normalMap.getNormal(chunk.zoomIn(Coordinate.OFFSET_BLOCK_CHUNK).plus(new Coordinate(x, z, Coordinate.LEVEL_BLOCK))); 
 	}
 	
 
 	public static int getTerrainHeight(ChunkSnapshot chunkSnapshot, int x, int z) {
 		int y = chunkSnapshot.getHighestBlockYAt(x, z);
 		Material material;
 		do {
 			y--;
 			int id = chunkSnapshot.getBlockTypeId(x, y, z);
 			material = Material.getMaterial(id);
 		} while (!TerrainHelper.isTerrain(material) && (y > 0));
 		return y;
 	}
 	
 	public static int getBiomeFoliageColor(ChunkSnapshot chunkSnapshot, int x, int y, int z) {
 		Coordinate coordinate = getBiomeColorIndex(chunkSnapshot, x, z);
 		if ((chunkSnapshot.getBlockData(x, y, z) & 0x3) == 0x2) {
 			return foliageColor.getRGB(255 - coordinate.getX(), 255 - coordinate.getY());
 		} else {
 			return foliageColor.getRGB(coordinate.getX(), coordinate.getY());
 		}
 	}
 	
 	public static int getBiomeGrassColor(ChunkSnapshot chunkSnapshot, int x, int z) {
 		Coordinate coordinate = getBiomeColorIndex(chunkSnapshot, x, z);
 		return grassColor.getRGB(coordinate.getX(), coordinate.getY());
 	}
 	
 	public static int getBiomeWaterColor(ChunkSnapshot chunkSnapshot, int x, int z) {
 		Coordinate coordinate = getBiomeColorIndex(chunkSnapshot, x, z);
 		return waterColor.getRGB(coordinate.getX(), coordinate.getY());
 	}
 	
 	public static Coordinate getBiomeColorIndex(ChunkSnapshot chunkSnapshot, int x, int z) {
 		double rainfall = chunkSnapshot.getRawBiomeRainfall(x, z);
 		double temperature = chunkSnapshot.getRawBiomeTemperature(x, z);
 		
 		rainfall *= temperature;
 		int i = (int)((1.0d - temperature) * 255.0d);
 		int j = (int)((1.0d - rainfall) * 255.0d);
 		
 		// TODO: Refactor without using Coordinate
 		return new Coordinate(i, j, Coordinate.LEVEL_BLOCK);
 	}
 
 }
