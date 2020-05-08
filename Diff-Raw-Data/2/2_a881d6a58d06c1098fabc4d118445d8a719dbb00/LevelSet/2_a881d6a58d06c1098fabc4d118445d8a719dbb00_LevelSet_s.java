 package world;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.LinkedList;
 
 import jig.engine.util.Vector2D;
 
 /* Rolf Redford
  * 
  * LevelSet stores all levels read from file.
  * It contains linked list of LevelMap.
  * 
  * Accessor is by getThisLevel, get size by getNumLevels
  */
 public class LevelSet {
 
 	private LinkedList<LevelMap> levels; // Levels
 	private int rscNum = 0;
 
 	public LevelSet(String file) {
 		levels = readLevelSet(file);
 	}
 
 	// Get level.
 	public LevelMap getThisLevel(int levelnum) {
 		if (levels.size() > levelnum)
 			return levels.get(levelnum);
 		return null;
 	}
 
 	// Get number of levels read from file.
 	public int getNumLevels() {
 		return levels.size();
 	}
 
 	// Read file passed to this and create levelmaps and make linkedlist.
 	private LinkedList<LevelMap> readLevelSet(String filename) {
 		LinkedList<LevelMap> res = new LinkedList<LevelMap>();
 
 		//System.out.println(filename);
 
 		try {
 			InputStreamReader R = new InputStreamReader(LevelMap.class
 					.getResourceAsStream(filename));
 			//System.out.println(R);
 			BufferedReader bufRead = new BufferedReader(R);
 			//System.out.println(s);
 			if (bufRead == null)
 				return null;
 			String thisLine = bufRead.readLine();
 			int levelnum = java.lang.Integer.parseInt(thisLine);
 
 			// Level loading.
 			for (int l = 0; l < levelnum; l++) {
 				// Read inital "Level"
 				thisLine = bufRead.readLine();
 				//System.out.println(thisLine.compareTo("Level") + "###");
 				if (thisLine.compareTo("Level") != 0) {
 					bufRead.close();
 					res = null;
 					System.out
 							.println("Error: Level file not formatted properly!");
 					return null;
 				}
 
 				// Create new levelmap and read title.
 				LevelMap thisMap = new LevelMap();
 				thisLine = bufRead.readLine();
 				//System.out.println(thisLine);
 				if (thisLine == null) {
 					bufRead.close();
 					res = null;
 					System.out.println("Error: Level is missing title");
 					return null;
 				}
 				thisMap.LevelTitle = thisLine;
 
 				// Read level type
 				thisLine = bufRead.readLine();
 				//System.out.println(thisLine);
 				if (thisLine == null) {
 					bufRead.close();
 					res = null;
 					System.out.println("Error: Level is missing type");
 					return null;
 				}
 				thisMap.LevelType = java.lang.Integer.parseInt(thisLine);
 				
 				// get the mirror flag
 				thisLine = bufRead.readLine();
 				//System.out.println(thisLine);
 				if (thisLine == null) {
 					bufRead.close();
 					res = null;
 					System.out.println("Error: Level is missing mirror");
 					return null;
 				}
 				if (thisLine.compareTo("true") == 0) thisMap.mirror = true;
 				else thisMap.mirror = false;
 				
 				// get the scaling factor
 				thisLine = bufRead.readLine();
 				if (thisLine == null) {
 					bufRead.close();
 					res = null;
 					System.out.println("Error: Level is missing scale");
 					return null;
 				}
 				thisMap.scale = java.lang.Double.parseDouble(thisLine);
 				
 				// get the offset position
 				thisLine = bufRead.readLine();
 				//System.out.println(thisLine);
 				if (thisLine == null) {
 					bufRead.close();
 					res = null;
 					System.out.println("Error: Level is missing offset");
 					return null;
 				}
 				String[] os = thisLine.split(" ");
 				if (os.length < 2) {
 					bufRead.close();
 					res = null;
 					System.out
 							.println("Error: Offset missing one number");
 					return null;
 				}
 				thisMap.offset = new Vector2D(java.lang.Integer
 						.parseInt(os[0]), java.lang.Integer.parseInt(os[1]));
 
 				// Load four spawn spots.
 				for (int y = 0; y < 4; y++) {
 					thisLine = bufRead.readLine();
 					//System.out.println(thisLine);
 					if (thisLine == null) {
 						bufRead.close();
 						res = null;
 						System.out.println("Error: Level is misformatted!");
 						return null;
 					}
 					String[] splitLine = thisLine.split(" ");
 					if (splitLine.length < 2) {
 						bufRead.close();
 						res = null;
 						System.out
 								.println("Error: Player spawn spot is missing in least one number!");
 						return null;
 					}
 					Vector2D newspawn = new Vector2D(java.lang.Integer
 							.parseInt(splitLine[0]), java.lang.Integer.parseInt(splitLine[1]));
 					thisMap.playerInitSpots.add(newspawn);
 					//System.out.println(newspawn);
 				}
 
 				// Load objects, if any.
 				thisLine = bufRead.readLine();
 				//System.out.println(thisLine);
 				while (thisLine != null && thisLine.compareTo("EndLevel") != 0) {
 					String[] splitLine = thisLine.split(" ");
 					String rsc;
 					int x, y, width, height;
 					double rot, mass, fric, rest;
 					if (splitLine.length >= 9) {
 						rsc = splitLine[0];
 						x = java.lang.Integer.parseInt(splitLine[1]);
 						y = java.lang.Integer.parseInt(splitLine[2]);
 						width = java.lang.Integer.parseInt(splitLine[3]);
 						height = java.lang.Integer.parseInt(splitLine[4]);
 						if (thisMap.mirror) y = -(y + height); // flip upside down inkspape coords :P
 						rot = Math.toRadians(java.lang.Integer.parseInt(splitLine[5]));
 						mass = java.lang.Double.parseDouble(splitLine[6]);
 						if(mass < 0) {
 							mass = Double.MAX_VALUE;
 						}
 						fric = java.lang.Double.parseDouble(splitLine[7]);
 						rest = java.lang.Double.parseDouble(splitLine[8]);
 						
 						// create custom resource
 						/*BufferedImage b[] = new BufferedImage[1];
 						b[0] = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
 						Graphics g = b[0].getGraphics();
 						g.setColor(Color.darkGray);
 						g.fillRect(0, 0, width, height);
 						g.dispose();
 						rsc = rsc.concat(String.valueOf(rscNum++));
 						ResourceFactory.getFactory().putFrames(rsc, b);
 						
 						*/
						world.TileMaker.generateTexture(width, height, x, y, rot);
 						rsc = "static" + width + "x" + height;
 					} else if (splitLine.length == 7) {
 						rsc = splitLine[0];
 						x = java.lang.Integer.parseInt(splitLine[1]);
 						y = java.lang.Integer.parseInt(splitLine[2]);
 						rot = Math.toRadians(java.lang.Double.parseDouble(splitLine[3]));
 						mass = java.lang.Double.parseDouble(splitLine[4]);
 						if(mass < 0) {
 							mass = Double.MAX_VALUE;
 						}
 						fric = java.lang.Double.parseDouble(splitLine[5]);
 						rest = java.lang.Double.parseDouble(splitLine[6]);
 					} else {
 						bufRead.close();
 						res = null;
 						System.out.println("Error: Object parameters is missing in least one number!");
 						return null;
 					}					
 					
 					GameObject go = new GameObject(rsc);
 					go.set(mass, fric, rest, rot);
 					go.setPosition(new Vector2D(x,y));
 					thisMap.Objects.add(go);
 					if (thisMap.mirror && splitLine.length >= 9) {
 						GameObject goM = new GameObject(rsc);
 						goM.set(mass, fric, rest, Math.PI-rot);
 						goM.setPosition(new Vector2D( -go.getPosition().getX()-go.getWidth(), go.getPosition().getY() ) );
 						thisMap.Objects.add(goM);
 					}
 					//System.out.println(go);
 					thisLine = bufRead.readLine();
 					//System.out.println(thisLine);
 				}
 				
 				res.add(thisMap);
 			}
 			bufRead.close();
 			R.close();
 		} catch (IOException e) {
 			System.out.println(e.getMessage());
 			return null;
 		}
 
 		return res;
 	}
 }
