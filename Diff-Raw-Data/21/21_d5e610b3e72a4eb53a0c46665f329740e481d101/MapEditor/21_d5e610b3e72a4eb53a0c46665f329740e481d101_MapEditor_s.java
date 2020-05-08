 package level;
 
 import enemy.AbstractEnemy;
 import enemy.TestEnemy;
 import gameCharacter.GameCharacter;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import javax.swing.JOptionPane;
 
 import npc.NPC;
 
 import player.Player;
 import utils.Location;
 import app.Main;
 import app.RPGame;
 
 import com.golden.gamedev.Game;
 import com.golden.gamedev.GameLoader;
 import com.golden.gamedev.util.FileUtil;
 import com.google.gson.Gson;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonPrimitive;
 
 
 /**
  * Arrow key	: navigate
  * Space		: switch lower/upper tile
  * A			: next tile
  * D			: prev tile
  * W			: fast next tile
  * S			: fast prev tile
  * Right click	: select tile
  * Click		: put tile
  * Ctrl + S		: save
  */
 
 public class MapEditor extends Game {
 
 	Map 	map;
 	int 	tilenum;
 	int		tilemode;
 	int 	charnum;
 	BufferedImage player, enemy, item, npc;
 	RPGame game = new RPGame(new Main(null), null);
 	
 	// add 
 	JsonObject jLevel = new JsonObject();
 	JsonObject jPlayer = new JsonObject();
 	JsonArray jNPCs = new JsonArray();
 	JsonArray jItems = new JsonArray();
 	JsonArray jEnemies = new JsonArray();
 	JsonObject jInventory = new JsonObject();
 	
 	public void initResources() {
 		map = new Map(bsLoader, bsIO);
 		player = getImage("rsc/player/playerstart.png", false);
 		enemy = getImage("rsc/enemy/question.png", false);
 		item = getImage("rsc/items/question.png",false);
 		npc = getImage("rsc/npc/question.png", false);
 	}
 
 
 	public void update(long elapsedTime) {
 		map.update(elapsedTime);
 
 		// navigate
 		if (keyDown(KeyEvent.VK_LEFT)) {
 			map.move(-0.2*elapsedTime, 0);
 		}
 		if (keyDown(KeyEvent.VK_RIGHT)) {
 			map.move(0.2*elapsedTime, 0);
 		}
 		if (keyDown(KeyEvent.VK_UP)) {
 			map.move(0, -0.2*elapsedTime);
 		}
 		if (keyDown(KeyEvent.VK_DOWN)) {
 			map.move(0, 0.2*elapsedTime);
 		}
 			
 
 		// switch lower/upper tile
 		if (keyPressed(KeyEvent.VK_SPACE)) {
 			if (++tilemode > 2) 
 				tilemode = 0;
 
 			// validate current mode tile count
 			if (tilenum > getChipsetLength()) {
 				tilenum = getChipsetLength();
 			}
 		}
 
 		// next/prev tile
 		if (keyPressed(KeyEvent.VK_A) || keyDown(KeyEvent.VK_D)) {
 			if (++tilenum > getChipsetLength()) {
 				tilenum = getChipsetLength();
 			}
 		}
 		if (keyPressed(KeyEvent.VK_W) || keyDown(KeyEvent.VK_S)) {
 			if (--tilenum < 0) {
 				tilenum = 0;
 			}
 		}
 		
 		// next/prev character
 		if (keyPressed(KeyEvent.VK_N)) {
 			if (++charnum > getChipsetLength()) {
 				charnum = getChipsetLength();
 			}
 		}
 		if (keyPressed(KeyEvent.VK_M)) {
 			if (--charnum < 0) {
 				charnum = 0;
 			}
 		}
 
 
 		Point tileAt = map.getTileAt(getMouseX(), getMouseY());
 		if (tileAt != null) {
 			// put tile
 			if (bsInput.isMouseDown(MouseEvent.BUTTON1)) {
 				if(tilemode == 2) {
 					
 					Location loc = new Location(new int[]{getMouseX(), getMouseY()});
 					switch (charnum) {
 						case 0:
 							//player
 							game.bsLoader = bsLoader;
 							Player player = new Player(new GameCharacter(game, loc,
 									"rsc/config/player_directions.json"), "rsc/config/player_actions.json");
 							jPlayer = player.toJson();
 							
 							
 						case 1:
 							//item
 							JsonObject jItem = new JsonObject();
 							String itemName = JOptionPane.showInputDialog("Item name:");
 							jItem.add("name", new JsonPrimitive(itemName));
 							JsonArray jLoc = new JsonArray();
 							jLoc.add(new JsonPrimitive(getMouseX()));
 							jLoc.add(new JsonPrimitive(getMouseY()));
 							
 							jItem.add("location", jLoc);
 							jItem.add("image", new JsonPrimitive("rsc/items/"+itemName+".png"));
 							jItem.add("quantity", new JsonPrimitive(JOptionPane.showInputDialog("Quantity:")));
 							jItem.add("price", new JsonPrimitive(JOptionPane.showInputDialog("Price:")));
 							jItems.add(jItem);
 							
 						case 2:
 							//enemy
 							JsonObject jEnemy = new JsonObject();
 							JsonArray jELoc = new JsonArray();
 							jELoc.add(new JsonPrimitive(getMouseX()));
 							jELoc.add(new JsonPrimitive(getMouseY()));
 							
 							jEnemy.add("location", jELoc);
 							jEnemy.add("name", new JsonPrimitive("TestEnemy"));
 							jEnemy.add("directions", new JsonPrimitive("rsc/config/enemy_directions.json"));
 							
 							JsonArray jAttack = new JsonArray();
 							jAttack.add(new JsonPrimitive("shooting"));
 							jEnemy.add("attacks", jAttack);
 							jEnemies.add(jEnemy);
 							
 						case 3:
 							//npc
							NPC npc = NPC.createNPC("npcName", new GameCharacter(game, loc,
									"rsc/config/npc_directions.json"));
							jNPCs.add(npc.toJson());
 							
 					}
 					
 					//swing code to take attributes of sprite
 					
 					
 				}
 				else
 					getLayer() [tileAt.x] [tileAt.y] = tilenum;
 			}
 
 
 			// select tile
 			if (rightClick()) {
 				tilenum = getLayer() [tileAt.x] [tileAt.y];
 			}
 		}
 
 
 		// save to file with name map00.lwr and map00.upr
 		if (keyPressed(KeyEvent.VK_S) && keyDown(KeyEvent.VK_CONTROL)) {
 			String[] lowerTile = new String[map.layer1[0].length];
 			String[] upperTile = new String[map.layer1[0].length];
 			for (int i=0;i < map.layer1.length;i++)
 			for (int j=0;j < map.layer1[0].length;j++) {
 				if (lowerTile[j] == null) lowerTile[j] = "";
 				lowerTile[j] += String.valueOf(map.layer1[i][j])+" ";
 
 				if (upperTile[j] == null) upperTile[j] = "";
 				upperTile[j] += String.valueOf(map.layer2[i][j])+" ";
 			}
 			
 			String lwrLevel = JOptionPane.showInputDialog("Lower Layer:");
 			String uprLevel = JOptionPane.showInputDialog("Upper Layer:");
 			
 			FileUtil.fileWrite(lowerTile, bsIO.setFile(uprLevel));
 			FileUtil.fileWrite(upperTile, bsIO.setFile(lwrLevel));
 			
 			String nextLevel = JOptionPane.showInputDialog("Next file name:");
 			jLevel.add("nextLevel", new JsonPrimitive("rsc/savedmaps/"+nextLevel+".json"));
 			
 			jLevel.add("upperFilename", new JsonPrimitive("rsc/level/map00.upr"));
 			jLevel.add("lowerFilename", new JsonPrimitive("rsc/level/map00.lwr"));
 			
 			jLevel.add("player", jPlayer);
 			jLevel.add("enemies", jEnemies);
 			jLevel.add("npcs", jNPCs);
 			
 			jInventory.add("items", jItems);
 			jLevel.add("inventory", jInventory);
 			
 			String file = JOptionPane.showInputDialog("File name:");
 			Gson gson = new Gson();
 			try {
				FileWriter f1 = new FileWriter(file); 
 				System.out.println(gson.toJson(jLevel));
 				f1.write(jLevel.toString());
 				f1.close();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}   		
 			
 		}
 	}
 
 
 //	private int getTileX() {
 //		// convert mouse x coordinate to map tile
 //		return (int) ((map.getX() + getMouseX()) / 32);
 //	}
 //
 //	private int getTileY() {
 //		// convert mouse y coordinate to map tile
 //		return (int) ((map.getY() + getMouseY()) / 32);
 //	}
 //
 	// since lower tile has additional different tile
 	// the length of lower tile is chipsetE + additional tile
 	private int getChipsetLength() {
 		switch (tilemode) {
 			case 0: return map.chipsetE.image.length + map.chipset.length - 2;	// lower mode
 			case 1: return map.chipsetF.image.length - 1;	// upper mode
 			case 2: return 3; // sprite mode
 		}
 		return 0;
 	}
 
 	private BufferedImage getChipsetImage(int num) {
 		if (num == -1) {
 			return null;
 		}
 
 		switch (tilemode) {
 		// lower mode
 		case 0:
 			if (num < map.chipsetE.image.length) {
 				return map.chipsetE.image[num];
 			} else {
 				return map.chipset[num-map.chipsetE.image.length].image[2];
 			}
 
 		// upper mode
 		case 1:
 			return map.chipsetF.image[num];
 			
 		// sprite mode - return chipset
 		case 2:
 			return null;
 		}
 
 		return null;
 	}
 	
 	private BufferedImage getCharacter() {
 		if (charnum == -1) {
 			return null;
 		}
 
 		switch (charnum) {
 
 		case 0:
 			return player;
 			
 		case 1:
 			return item;
 			
 		case 2:
 			return enemy;
 
 		case 3: 
 			return npc;
 		}
 
 		return null;
 	}
 
 
 	// get tiles
 	private int[][] getLayer() {
 		switch (tilemode) {
 			case 0: return map.layer1;	// lower mode
 			case 1: return map.layer2;	// upper mode
 			case 2: return null;  // sprite mode
 		}
 
 		return null;
 	}
 
 
 	public void render(Graphics2D g) {
 		map.render(g);
 
 		// selected tile
 		if (getChipsetImage(tilenum) != null) {
 			g.drawImage(getChipsetImage(tilenum), 600, 40, null);
 		}
 		else
 			g.drawImage(getCharacter(), getMouseX(), getMouseY(), null);
 		
 		if (tilemode == 1 || tilemode == 0) {
 			g.setColor(Color.BLACK);
 			g.drawRect(600, 40, 32, 32);
 		}
 		Point tileAt = map.getTileAt(getMouseX(), getMouseY());
 		if (tileAt != null) {
 			g.setColor(Color.WHITE);
 			int posX = (tileAt.x - map.getTileX()) * 32,
 				posY = (tileAt.y-map.getTileY()) * 32;
 			g.drawRect(posX - map.getOffsetX() + map.getClip().x,
 					   posY - map.getOffsetY() + map.getClip().y,
 					   32, 32);
 		}
 	}
 
 
 	public static void main(String[] args) {
 		GameLoader game = new GameLoader();
 		game.setup(new MapEditor(), new Dimension(640,480), false);
 		game.start();
 	}
 
 }
