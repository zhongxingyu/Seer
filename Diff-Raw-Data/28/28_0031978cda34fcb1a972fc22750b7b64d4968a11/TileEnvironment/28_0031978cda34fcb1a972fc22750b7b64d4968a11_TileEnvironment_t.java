 package projectubernahme.environments;
 
 import java.awt.Graphics2D;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Point2D;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import projectubernahme.gfx.ConvertedGraphics;
 import projectubernahme.gfx.TileCrosswalkHorizontal;
 import projectubernahme.gfx.TileCrosswalkVertical;
 import projectubernahme.gfx.TileDefault;
 import projectubernahme.gfx.TileLawn;
 import projectubernahme.gfx.TileSidewalk;
 import projectubernahme.gfx.TileStreet;
 import projectubernahme.lifeforms.Human;
 import projectubernahme.lifeforms.Lifeform;
 import projectubernahme.lifeforms.Tree;
 import projectubernahme.lifeforms.Zombie;
 import projectubernahme.simulator.MainSimulator;
 
 /** an environment made up from tiles
   The tile environment is parsed from a text file filled with characters in a rectangular way. Every character stands for something on the map, these keys can be found in the guide file in the same folder this class is in. */
 public class TileEnvironment implements Environment {
 
 	private ConvertedGraphics[] cgs;
 
 	/** the loaded tiles */
 	private char[][] tiles;
 	private BufferedImage bg;
 
 	private static final ConvertedGraphics tileDefault = new TileDefault();
 
 	/** width of the tiles in meters of the game coordinates */
 	private double tileWidthInReal = 1.0;
 
 	private Point2D origin = new Point2D.Double(0.0, 0.0);
 
 	/** a copy of the previous transform to compare to the new one
 	  This saves a new rendering if the transform stayed all the same, making the game more fluent when not zooming or panning around. */
 	private AffineTransform previousTransform;
 
 	public TileEnvironment (String mapname) {
 
 		cgs = new ConvertedGraphics[256];
 
 		/* load the map from the text file */
 		ClassLoader cl = getClass().getClassLoader();
 
 		try {
 			InputStream fis = cl.getResourceAsStream("projectubernahme/environments/tilemap_"+mapname+".txt");
 
 			byte[] text = new byte[fis.available()];
 
 			fis.read(text);
 
 			/* count rows and columns */
 			int columns = 0, rows = 0;
 			boolean erste = true;
 
 			for (int i = 0; i < text.length; i++) {
 				if (erste)
 					if (text[i] != '\n' && text[i] != '\r')
 						columns++;
 
 				if (text[i] == '\n' || text[i] == '\r') {
 					rows++;
 					erste = false;
 				}
 			}
 
 			/* creates the needed array for the tiles */
 			tiles = new char[columns][rows];
 
 			/* loads the tiles into the array */
 			for (int i = 0; i < rows; i++) {
 				for (int j = 0; j < columns; j++) {
 					tiles[j][i] = (char)text[i*(columns+1)+j];
 
 					/* load the tile class if it is not in the map already */
 					if (cgs[tiles[j][i]] == null) {
 						loadTile(tiles[j][i]);
 					}
 				}
 			}
 
 			fis.close();
 		} catch (IOException e1) {
 			System.out.println("Error while reading file");
 			System.exit(1);
 		}
 	}
 
 	private void loadTile(char c) {
 		switch (c) {
 		case 'S': cgs[c] = new TileSidewalk(); break;
 		case 'l':
 		case 'L': cgs[c] = new TileLawn(); break;
 		case 's': cgs[c] = new TileStreet(); break;
 		case 'C': cgs[c] = new TileCrosswalkHorizontal(); break;
 		case 'c': cgs[c] = new TileCrosswalkVertical(); break;
 		}
 
 	}
 
 	public boolean isFreeBetween(double x1, double y1, double z1, double x2, double y2, double z2) {
 		/* return false if path would lead out of the map */
 		if (x2 < 0 || y2 < 0 || x2 > tiles.length*tileWidthInReal || y2 > tiles[0].length*tileWidthInReal)
 			return false;
 		
 		return true;
 	}
 
 	public BufferedImage getBackground() {
 		return bg;
 	}
 
 	/** returns the background and renders it new, if needed */
 	public BufferedImage getBackground (int width, int height, AffineTransform transform) {
 		/* do not repaint if the transform has not changed at all */
 		if (transform.equals(previousTransform)) {
 		}
 		else {
 			double scaling = 1.05;
 			ConvertedGraphics cg;
 			
 			boolean[][] visible = new boolean[tiles.length][tiles[0].length];
 
 			double twiceScreenRadius = Math.hypot(width, height);
 			
 			/* iterate through the tiles ... */
 			for (int i = 0; i < tiles[0].length; i += 1) {
 				for (int j = 0; j < tiles.length; j += 1) {
 					/* create a new transform for each of the tiles */
 					AffineTransform tileTransform = new AffineTransform();
 					tileTransform.translate(j*tileWidthInReal, i*tileWidthInReal);
 
 					/* also add the (physical) player's transform to generate the picture that he wants to have */
 					tileTransform.preConcatenate(transform);
 
 					/* draw only the visible items onto the background */					
 					Point2D target = tileTransform.transform(origin, null);
 					
 					visible[j][i] = target.distance(width/2, height/2) < twiceScreenRadius;
 				}
 			}
 			
 
 			/* create a new background image and pull the graphics to draw on it */
 			bg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
 			Graphics2D g = (Graphics2D) bg.getGraphics();
 
 			/* iterate through the tiles ... */
 			for (int i = 0; i < tiles[0].length; i += 1) {
 				for (int j = 0; j < tiles.length; j += 1) {
 					/* create a new transform for each of the tiles */
 					AffineTransform tileTransform = new AffineTransform();
 					tileTransform.translate(j*tileWidthInReal, i*tileWidthInReal);
 					//tileTransform.scale(1.0/tileWidth*tileWidthInReal, 1.0/tileWidth*tileWidthInReal);
 
 					/* also add the (physical) player's transform to generate the picture that he wants to have */
 					tileTransform.preConcatenate(transform);
 
 					/* draw only the visible items onto the background */
 					if (visible[j][i] || (i+1 < tiles[0].length && j+1 < tiles.length && (visible[j+1][i] || visible[j][i+1] || visible[j+1][i+1]))) {
 
 						if (cgs[tiles[j][i]] != null) {
 							cg = cgs[tiles[j][i]];
 						}
 						else {
 							cg = tileDefault;
 						}
 
 						/* figure out which tile image is the right one */
 						tileTransform.scale(tileWidthInReal/cg.getOrigWidth()*scaling, tileWidthInReal/cg.getOrigWidth()*scaling);
 						tileTransform.translate(-cg.getOrigX(), -cg.getOrigY());
 
 						g.setTransform(tileTransform);
 						cg.paint(g);
 					}
 				}
 			}
 			/* clone the current player's transform to compare it in the next iteration of this method */
 			previousTransform = (AffineTransform) transform.clone();
 		}
 		return bg;
 	}
 
 	/** iterate through the tiles and spawn some lifeforms where they belong */
 	public void initializeNPCs(CopyOnWriteArrayList<Lifeform> list, MainSimulator sim) {
 		for (int i = 0; i < tiles[0].length; i++) {
 			for (int j = 0; j < tiles.length; j++) {
				if (Math.random() > 0.6) {
 					switch (tiles[j][i]) {
					case 'S': list.add(Math.random() < 0.9 ? new Human(sim, (j+0.5)*tileWidthInReal, (i+0.5)*tileWidthInReal) : new Zombie(sim, (j+0.5)*tileWidthInReal, (i+0.5)*tileWidthInReal)); break;
 					case 'L':
 						int treeOffset = 2;
 						if (i >= treeOffset && j >= treeOffset && i+treeOffset < tiles[0].length && j+treeOffset < tiles.length) {
 							if (tiles[j-treeOffset][i] == 'L' && tiles[j+treeOffset][i] == 'L' && tiles[j][i-treeOffset] == 'L' && tiles[j][i+treeOffset] == 'L' && Math.random() > 0.9) {
 								list.add(new Tree(sim, (j+0.5)*tileWidthInReal, (i+0.5)*tileWidthInReal));
 							}
 						}
 						break;
 					}
 				}
 			}
 		}
 	}
 
 	public Point2D getRandomPointOnMap() {
 		return new Point2D.Double(Math.random()*tiles.length*tileWidthInReal, Math.random()*tiles[0].length*tileWidthInReal);
 	}
 }
 
