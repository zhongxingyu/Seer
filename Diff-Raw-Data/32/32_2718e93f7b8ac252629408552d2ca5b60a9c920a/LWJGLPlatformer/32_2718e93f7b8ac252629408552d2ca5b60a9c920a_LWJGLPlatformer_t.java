 package lh.koneke.games.lwjglplatformer;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Random;
 
import org.lwjgl.input.Keyboard;
 import org.lwjgl.opengl.GL11;
 
 import lh.koneke.thomas.framework.EntityManager;
 import lh.koneke.thomas.framework.Font;
 import lh.koneke.thomas.framework.Game;
 import lh.koneke.thomas.framework.GameMouse;
 import lh.koneke.thomas.framework.Graphics;
 import lh.koneke.thomas.framework.Quad;
 import lh.koneke.thomas.framework.Rectangle;
 import lh.koneke.thomas.framework.SoundManager;
 import lh.koneke.thomas.framework.Vector2f;
 import lh.koneke.thomas.graphics.Colour;
 import lh.koneke.thomas.graphics.DrawQuadCall;
 import lh.koneke.thomas.graphics.Frame;
 import lh.koneke.thomas.graphics.Texture2d;
 import lh.koneke.thomas.graphics.TextureInformation;
 import lh.koneke.thomas.gui.Button;
 import lh.koneke.thomas.gui.ContextMenu;
 import lh.koneke.thomas.gui.Text;
 
 public class LWJGLPlatformer extends Game {
 	public static void main(String[] args) { LWJGLPlatformer game = new LWJGLPlatformer(); game.start(); }
 	
 	Vector2f screenSize;
 	
 	/* ~~~~~~~~~~ */
 	
 	EntityManager em;
 	SoundManager sm;
 	Vector2f playerTarget; //spot to move to
 	
 	List<Frame> unloadedFrames;
 	Frame levelBackground;
 	Frame tileSheet;
 	Vector2f tileSize;
 	
 	Screen currentScreen;
 	
 	ContextMenu contextMenu;
 	ContextMenu actionMenu;
 	
 	List<String> commands = new ArrayList<String>();
 	Entity selectedEntity;
 	
 	boolean mouseFree = true;
 	
 	int hotswapFrequency = 1000; //time in ms for hotswap updating
 	int hotswapTimer = 0;
 	
 	Font f;
 	
 	String[] console;
 	
 	/* ~~~~~~~~~~ */
 	
 	public void sysInit() {
 		screenSize = new Vector2f(320, 256);
 		Graphics.scale = 3f;
 		setDisplay((int)(screenSize.x * Graphics.scale), (int)(screenSize.y * Graphics.scale));
 		random = new Random();
 	}
 	
 	public void initialize() {
 		sysInit();
 		
 		console = new String[3];
 		for(int i = 0; i < 3; i++){
 			console[i] = "";
 		}
 		console[0] = "Thomas the Game Engine, early alpha. github.com/Koneke";
 		
 		unloadedFrames = new ArrayList<Frame>();
 		
 		contextMenu = new ContextMenu(new Vector2f(0,0), 68);
 		contextMenu.setGraphics(new Colour(0.3f,0.3f,0.3f,1));
 		
 		actionMenu = new ContextMenu(new Vector2f(0,0), 68);
 		actionMenu.setGraphics(new Colour(0.3f,0.3f,0.3f,1));
 		
 		commands.add("Look at");
 		actionMenu.addItem(
 			new Button(new Rectangle(new Vector2f(0, 0), new Vector2f(66, 15)),
 			null
 		));
 	}
 	
 	public void load() {
 		Graphics.setInterpolationMode("none");
 
 		tileSize = new Vector2f(32,32);
 
 		f = new Font();
 		f.load("res/font.thf");
 		f.sheet = new Frame(null, null);
 		f.sheet.texturePath = f.getPath();
 		
 		tileSheet = new Frame(null, new Vector2f(32,32));
 		tileSheet.texturePath = "res/testsheet2.png";
 		
 		levelBackground = new Frame(new Vector2f(0,0), screenSize);
 		levelBackground.texturePath = "res/testbg2.png";
 		
 		unloadedFrames.add(f.sheet);
 		unloadedFrames.add(tileSheet);
 		unloadedFrames.add(levelBackground);
 		
 		currentScreen = new Screen(10, 8, tileSheet, tileSize);
 		currentScreen.load("res/screen.thl");
 		
 		em = new EntityManager(currentScreen);
 		em.load("res/entities.the");
 		
 		sm = new SoundManager();
 		sm.load("res/sounds.ths");
 		AnimationManager.sm = sm;
 		
 		playerTarget = new Vector2f(em.getEntity("player").quad.topleft.add(tileSize.scale(1/2f))); //where the player is moving towards
 		
 		for(Entity e : em.getEntities().values()) {
 			e.graphics = new Frame(null, tileSize);
 			System.out.println("e loading "+e.texturePath);
 			e.graphics.setTexture(
 					new Texture2d(Graphics.loadTexture(e.texturePath),
 					e.texturePath));
 			e.am.load(e.thaPath);
 			e.am.startAnimation("idle");
 		}
 
 		for(Frame f : unloadedFrames) {
 			f.setTexture(new Texture2d(Graphics.loadTexture(f.texturePath), f.texturePath));
 		}
 	}
 	
 	public Vector2f getGridPosition(Vector2f position) {
 		Vector2f v = new Vector2f(position);
 		v.x -= v.x % tileSize.x;
 		v.y -= v.y % tileSize.y;
 		v = v.scale(1f/tileSize.x, 1f/tileSize.y);
 		return v;
 	}
 	
 	public void update() {
 		/* 
 		 * TODO:
 		 * clean menu stuff
 		 */
 		if(GameMouse.right && !GameMouse.prevRight) {
 			sm.play("step");
 			
 			actionMenu.setVisible(false);
 			contextMenu.setVisible(true);
 			
 			//get tile clicked
 			Vector2f v = getGridPosition(GameMouse.getPosition().scale(1f/Graphics.scale));
 			TileSlot tile = currentScreen.getAt(v);
 			contextMenu.tile = tile;
 
 			//clear the menu, and add a button for each entity in the tile
 			contextMenu.clear();
 			int items = tile.entities.size();
 			for(int i = 0; i< items; i++) {
 				Button b = new Button(new Rectangle(
 					new Vector2f(contextMenu.getShape().getPosition().add(new Vector2f(1, 0))), new Vector2f(contextMenu.getShape().w-2,
 						f.characterHeight+2)),
 					null
 				);
 				if (b.getGraphics() == null) {
 					b.setGraphics(new Colour(1,1,1,1));
 				}
 				contextMenu.addItem(b);
 			}
 
 			//shape and place the buttons
 			contextMenu.getShape().setPosition(GameMouse.getPosition().scale(1f/Graphics.scale));
 			float h = 2;
 			for(Button b : contextMenu.getItems()) {
 				b.getShape().x = contextMenu.getShape().x+1;
 				b.getShape().y = contextMenu.getShape().y+h;
 				h += b.getShape().h+2; //margin = 2
 			}
 			
 			contextMenu.getShape().h = h;
 		}
 		
 		if(GameMouse.left && !GameMouse.prevLeft) {
 			sm.play("step");
 			
 			if(contextMenu.getVisible()) {
 				if(!contextMenu.getShape().containsPoint(GameMouse.getPosition().scale(1f/Graphics.scale))) {
 					//clicked somewhere else, close and hide menu
 					contextMenu.setVisible(false);
 					mouseFree = true;
 				} else {
 					//menu has been clicked, lock the mouse so we can navigate it without moving the player
 					mouseFree = false;
 					
 					for(Button b : contextMenu.getItems()) {
 						if(b.getShape().containsPoint(GameMouse.getPosition().scale(1f/Graphics.scale))) {
 							//the button b in the menu was clicked, select that item
 							selectedEntity = contextMenu.tile.entities.get(contextMenu.getItems().indexOf(b));
 							
 							//this menu is done, switch to the next one
 							contextMenu.setVisible(false);
 							actionMenu.getShape().setPosition(contextMenu.getShape().getPosition());
 							actionMenu.getShape().h = 4;
 							actionMenu.setVisible(true);
 							
 							float h = 2;
 							for(Button bb : actionMenu.getItems()) {
 								bb.getShape().x = actionMenu.getShape().x+1;
 								bb.getShape().y = actionMenu.getShape().y+h;
 								h += bb.getShape().h+2; //margin = 2
 							}
 							//place and shape the buttons
 							actionMenu.getShape().h = h;
 							
 							break;
 						}
 					}
 				}
 			} else if(actionMenu.getVisible()) {
 				if(!contextMenu.getShape().containsPoint(GameMouse.getPosition().scale(1f/Graphics.scale))) {
 					actionMenu.setVisible(false);
 					mouseFree = true;
 				} else {
 					mouseFree = false;
 					
 					for(Button b : actionMenu.getItems()) {
 						if(b.getShape().containsPoint(GameMouse.getPosition().scale(1f/Graphics.scale))) {
 							//get command
 							String command = commands.get(actionMenu.getItems().indexOf(b));
 							int c = command == "Look at" ? 1 : 0; //support for 1.7- versions of java, so don't switch on string
 							
 							//handle command
 							switch(c) {
 								case 1:
 									console[0] = console[1];
 									console[1] = console[2];
 									console[2] = selectedEntity.getLook();
 									break;
 								default:
 									console[0] = console[1];
 									console[1] = console[2];
 									console[2] = "Uh, what?";
 							}
 							
 							//action menu is done
 							actionMenu.setVisible(false);
 							break;
 						}
 					}
 				}
 			} else {
 				mouseFree = true;
 			}
 		}
 		
 		
 		if(mouseFree) {
 			if(GameMouse.left) {
 				playerTarget.x = GameMouse.getPosition().scale(1f/Graphics.scale).x;
 			} else {
 				//if the mouse is not free, set our current position as target
 				playerTarget.x = em.getEntity("player").quad.topleft.add(tileSize.scale(1/2f)).x;
 			}
 		}
 		
 		if(Math.abs(em.getEntity("player").quad.topleft.add(tileSize.scale(1/2f)).x - playerTarget.x) > 1) {
 			em.getEntity("player").graphics.setxflip(GameMouse.getPosition().scale(1f/Graphics.scale).x < em.getEntity("player").quad.topleft.add(tileSize.scale(1/2f)).x);
 			
 			Vector2f preMove = getGridPosition(em.getEntity("player").quad.topleft.add(tileSize.scale(1/2f)));
 			em.getEntity("player").quad.move(new Vector2f(((em.getEntity("player").quad.topleft.add(tileSize.scale(1/2f)).x > playerTarget.x) ? -1 : 1)*dt*tileSize.x/250f, 0));
 			Vector2f postMove = getGridPosition(em.getEntity("player").quad.topleft.add(tileSize.scale(1/2f)));
 			
 			if(preMove.x != postMove.x || preMove.y != postMove.y) {
 				em.getEntity("player").logicalPosition = getGridPosition(em.getEntity("player").quad.topleft.add(tileSize.scale(1/2f)));
 				
 				currentScreen.getActiveTiles().remove(em.getEntity("player").currentTileSlot);
 				currentScreen.getAt(preMove).entities.remove(em.getEntity("player"));
 				
 				em.getEntity("player").currentTileSlot = currentScreen.getAt(postMove);
 				
 				currentScreen.getActiveTiles().add(em.getEntity("player").currentTileSlot);
 				currentScreen.getAt(postMove).entities.add(em.getEntity("player"));
 			}
 			
 			if(em.getEntity("player").am.getAnimation() != "walking") {
 				em.getEntity("player").am.startAnimation("walking");
 			}
 		} else {
 			if(em.getEntity("player").am.getAnimation() != "idle") {
 				em.getEntity("player").am.startAnimation("idle");
 			}
 		}
 		
		for(Entity e : em.getEntities().values()) {
			//em.getEntity("player").lifetime += dt;
			e.lifetime += dt;
		}
		
 		hotswapTimer += dt;
 		if (hotswapTimer > hotswapFrequency) {
 			while (hotswapTimer > hotswapFrequency) {
 				hotswapTimer -= hotswapFrequency;
 			}
 			
 			for(TextureInformation ti : Texture2d.information) {
 				ti.checkHotswap();
 			}
 			
 			levelBackground.getTexture().checkHotswap();
 			tileSheet.getTexture().checkHotswap();
 			f.sheet.getTexture().checkHotswap();
 		}
 	}
 	
 	public void draw() {
 		List<DrawQuadCall> drawCommands = new ArrayList<DrawQuadCall>();
 		
 		drawCommands.add(new DrawQuadCall(
 			levelBackground, null, null,
 			new Quad(new Rectangle(new Vector2f(0,0), new Vector2f(512,256))),
 			10, null
 		));
 		
 		/*
 		for(TileSlot t : currentScreen.getActiveTiles()) {
 			drawCommands.add(new DrawQuadCall(
 				new Colour(1,0,0,1), null,
 				null,
 				new Quad(new Rectangle(t.position.scale(tileSize.x, tileSize.y), tileSize)),
 				scale,
 				3, null
 			));
 		}*/
 		//debugging, show active tiles
 		
 		for (int x = 0; x < currentScreen.map.length; x++) {
 			for (int y = 0; y < currentScreen.map[0].length; y++) {
 				for (Tile t : currentScreen.map[x][y].getTiles()) {
 					Vector2f v = t.tile;
 					Rectangle r = ((Frame)currentScreen.tileSheet).getAt(v, tileSize);
 					
 					if(t.xflip) r=r.xflip();
 					if(t.yflip) r=r.yflip();
 					
 					drawCommands.add(new DrawQuadCall(
 						((Frame)currentScreen.tileSheet), null, r,
 						new Quad(new Rectangle(
 							currentScreen.map[x][y].position.scale(
 									currentScreen.tileSize.x,
 									currentScreen.tileSize.y),
 							currentScreen.tileSize)),
 						t.depth, null));
 				}
 			}
 		}
 		
		//em.getEntity("player").am.Update(dt);
		for(Entity e : em.getEntities().values()) {
			//em.getEntity("player").lifetime += dt;
			e.am.Update(dt);
		}
 		
 		/*float bump = 0; float bumpsPerSecond = 4;
 		if(em.getEntity("player").am.getAnimation() == "walking") { bump = 5f; }*/
 		/*
 		 * TODO:
 		 * Reimplement bumping (IMPORTANT)
 		 */
 
 		for(Entity e : em.getEntities().values()) {
 			drawCommands.add(new DrawQuadCall(
 				e.graphics, e.am, e.graphics.getTexCoord(e.graphics.getTexture(), e.am),
 				e.quad, e.depth, null));
 		}
 		
 		if(contextMenu.getVisible()) {
 			drawCommands.add(new DrawQuadCall(
 				contextMenu.getGraphics(), null, null,
 				new Quad(contextMenu.getShape()), -10, null));
 			
 			for(Button b : contextMenu.getItems()) {
 				drawCommands.add(new DrawQuadCall(
 					new Colour(0.5f,0.5f,0.5f,1), null, null,
 					new Quad(b.getShape()), -11, null));
 				drawCommands.addAll(Text.renderString(
 					contextMenu.tile.entities.get(contextMenu.getItems().indexOf(b)).name,
 					f, b.getShape().getPosition().add(new Vector2f(1,1)), -12, null).calls);
 			}
 		}
 		
 		if(actionMenu.getVisible()) {
 			drawCommands.add(new DrawQuadCall(
 				actionMenu.getGraphics(), null, null,
 				new Quad(actionMenu.getShape()), -10, null));
 			
 			for(Button b : actionMenu.getItems()) {
 				drawCommands.add(new DrawQuadCall(
 					new Colour(0.5f,0.5f,0.5f,1), null, null,
 					new Quad(b.getShape()), -11, null));
 				drawCommands.addAll(Text.renderString(
 					commands.get(actionMenu.getItems().indexOf(b)),
 					f, b.getShape().getPosition().add(new Vector2f(1,1)), -12, null).calls);
 			}
 		}
 		
 		/*drawCommands.addAll(Text.renderString(
 				"ABCDEFGHIJKLMNOPQRSTUWXYZabcdefghijklmnopqrstuwxyz...,,,'''???!!!",
				f, new Vector2f(0,0), -10, new Colour(1,0,0,1)).calls);*/
 		//debugging, draw alphabet
 		
 		for(int i = 0;i<3;i++) {
 			drawCommands.addAll(Text.renderString(
 				console[i], f, new Vector2f(2,f.characterHeight*i), -10, new Colour(0,0,0,1)).calls);
 		}
 		
 		/*Sort and run calls*/
 		
 		Collections.sort(drawCommands, new Comparator<DrawQuadCall>() {
 			public int compare(DrawQuadCall a, DrawQuadCall b) {
 				if (a.getDepth() < b.getDepth()) return -1;
 				if (a.getDepth() > b.getDepth()) return 1;
 				return 0; }});
 		Collections.reverse(drawCommands);
 		
 		for(DrawQuadCall dqc : drawCommands) {
 			if(dqc.getColour() != null) {
 				GL11.glColor4f(dqc.getColour().getRed(), dqc.getColour().getGreen(), dqc.getColour().getBlue(), dqc.getColour().getAlpha());
 			}
 			drawQuad(dqc.getGraphics(), dqc.getAm(), dqc.getSource(), dqc.getQuad());
 		}
 	}
 }
