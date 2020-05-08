 package edu.brown.cs32.goingrogue.game;
 
 import static java.lang.Math.toDegrees;
 
 import java.awt.geom.Point2D;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.List;
 import java.util.PriorityQueue;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import edu.brown.cs32.goingrogue.constants.Constants;
 import edu.brown.cs32.goingrogue.constants.KeyCodes;
 import edu.brown.cs32.goingrogue.gameobjects.actions.Action;
 import edu.brown.cs32.goingrogue.gameobjects.actions.ActionAnimation;
 import edu.brown.cs32.goingrogue.gameobjects.actions.ActionType;
 import edu.brown.cs32.goingrogue.gameobjects.creatures.Attribute;
 import edu.brown.cs32.goingrogue.gameobjects.creatures.Creature;
 import edu.brown.cs32.goingrogue.gameobjects.creatures.Player;
 import edu.brown.cs32.goingrogue.gameobjects.items.Item;
 import edu.brown.cs32.goingrogue.gameobjects.items.Potion;
 import edu.brown.cs32.goingrogue.graphics.Animation;
 import edu.brown.cs32.goingrogue.graphics.GraphicsLoader;
 import edu.brown.cs32.goingrogue.graphics.GraphicsPaths;
 import edu.brown.cs32.goingrogue.map.RogueMap;
 import edu.brown.cs32.goingrogue.map.Space;
 import edu.brown.cs32.goingrogue.map.Tile;
 import edu.brown.cs32.goingrogue.map.Wall;
 import edu.brown.cs32.goingrogue.util.IndexPair;
 import edu.brown.cs32.goingrogue.util.Text;
 import edu.brown.cs32.goingrogue.util.Util;
 import edu.brown.cs32.jcadler.GameLogic.GameLogic;
 
 /** Handles the updating and rendering of a game
  * 
  * @author Dominic Adams
  * @version 1.0 4/13
  */
 public class GamePlayState extends BasicGameState{
 
 	GameContainer gc;
 
 	ConcurrentHashMap<Integer, Integer> keysPressed;
 	ConcurrentHashMap<Integer, Boolean> keysPressedOnce;
 	PriorityQueue<Integer> keyEventQueue;
 	
 	
 	GameLogic game;
 	Player player;
 	RogueMap map;
 	AnimationCache cache;
 	
 	int hudSize;
 	String deathMessage;
 	
 	private int id; //Used for StateBasedGame
 
 	
 	public void setID(int id){
 		this.id = id;
 	}
 
 	int timeCount; //Used for tracking game updates
 	int lastRenderTime; //Used to track time passage since the last render
 
 	double gameToScreenFactor=40.; //Conversion from game to screen coordinates
 
 	final int animationDurationBuffer=20; //Adds 20 ms to the duration of an animation
 
 	public GamePlayState(GameContainer gc, int id) {
 		this.gc=gc;
 		this.id = id;
 	}
 
 	public void init(){
 		//g=null; //Set up by init() method
 		timeCount=0;
 
 		keysPressed=new ConcurrentHashMap<Integer, Integer>();
 		keysPressed.put(KeyCodes.Q, -1);
 		keysPressed.put(KeyCodes.W, -1);
 		keysPressed.put(KeyCodes.E, -1);
 		keysPressed.put(KeyCodes.A, -1);
 		keysPressed.put(KeyCodes.S, -1);
 		keysPressed.put(KeyCodes.D, -1);
 		keysPressed.put(KeyCodes.LEFT, -1);
 		keysPressed.put(KeyCodes.RIGHT, -1);
 		keysPressed.put(KeyCodes.UP, -1);
 		keysPressed.put(KeyCodes.DOWN, -1);
 		keysPressed.put(KeyCodes.SPACE, -1);
 		keysPressed.put(KeyCodes.ESC, -1);
 		
 		keysPressedOnce=new ConcurrentHashMap<>();
 		keysPressedOnce.put(KeyCodes.E, false);
 		keysPressedOnce.put(KeyCodes.R, false);
 		
 		keyEventQueue=new PriorityQueue<Integer>(10, new Comparator<Integer>(){
 			public int compare(Integer i1, Integer i2) {
 				return keysPressed.get(i1)-keysPressed.get(i2);
 			}
 		});
 		
 		hudSize=0;
 		deathMessage=DeathMessage.getRandomMessage();
 
 		//Initializes gameplay
 		try {
 			game = new GameLogic();
 			player=game.getPlayer();
 			map=game.getMap();
 			cache=new AnimationCache();
 		} catch(IOException e) {
 			//Should not happen
 			e.printStackTrace();
 		}
 	}
 	
 	public GameLogic getGameLogic() {
 		return game;
 	}
 	
 	public void setGameLogic(GameLogic g) {
 		game=g;
 		player=game.getPlayer();
 		map=game.getMap();
 	}
 
 	public Player getPlayer() {
 		return player;
 	}
 	
 	public int[] getScreenCenter() {
 		return new int[]{gc.getWidth()/2, (gc.getHeight()-hudSize)/2};
 	}
 	
 	//Converts a point on the game map to a point on the screen
 	int[] gameToScreen(Point2D point, Point2D center) {
 
 		int[] screenCenter=getScreenCenter();
 		
 		//Offsets from the center of the screen
 		double xOffset=(point.getX()-center.getX());
 		double yOffset=(point.getY()-center.getY());
 
 		//Screen coords
 		int x=(int)(screenCenter[0]+xOffset*gameToScreenFactor);
 		int y=(int)(screenCenter[1]+yOffset*gameToScreenFactor);
 
 		return new int[]{x, y};
 	}
 
 	//Converts a point on the screen to a point on the game map
 	Point2D screenToGame(int[] point, Point2D center) {
 
 		//The screen center
 		int[] screenCenter=getScreenCenter();
 		
 		int screenXCenter=screenCenter[0];
 		int screenYCenter=screenCenter[1];
 
 		//Game coords
 		double x=(point[0]-screenXCenter)/gameToScreenFactor + center.getX();
 		double y=(point[1]-screenYCenter)/gameToScreenFactor + center.getY();
 
 		return new Point2D.Double(x, y);
 	}
 
 	//Returns the coords needed to draw an image so that it appears centered around the specified coords
 	int[] centerImage(int[] coords, Image i) {
 		return new int[]{coords[0]-i.getWidth()/2, coords[1]-i.getHeight()/2};
 	}
 
 	//Locates the an image's center of rotation at its center
 	void setRotationCenter(Image i) {
 		i.setCenterOfRotation(i.getWidth()/2, i.getHeight()/2);
 	}
 
 	/** Updates the game one time
 	 * 
 	 * @param delta The amount of time since the last game update
 	 */
 	public void update(int delta) {
 		Player p = getPlayer();
 		
 		for(int key: keyEventQueue) {
 			if(key==KeyCodes.W || key==KeyCodes.UP) p.moveUp();
 			if(key==KeyCodes.S || key==KeyCodes.DOWN) p.moveDown();
 			if(key==KeyCodes.A || key==KeyCodes.LEFT) p.moveLeft();
 			if(key==KeyCodes.D || key==KeyCodes.RIGHT) p.moveRight();
 			if(key==KeyCodes.SPACE) p.attack();
 			if(keysPressedOnce.get(KeyCodes.E)){
 				p.pickUp();
 				keysPressedOnce.put(KeyCodes.E, false);
 			}
			if(key==KeyCodes.R) {
 				p.quaff();
 				keysPressedOnce.put(KeyCodes.R, false);
 			}
 			if(key==KeyCodes.ESC) System.exit(0);
 			
 		}
 		
 		timeCount+=delta;
 		try {
 			game.update(delta);
 		} catch(CloneNotSupportedException e) {
 			
 		} catch(IOException e) {
         	
         }
 	}
 
 	/** Draws the game
 	 * 
 	 * @param g The graphics component used to draw the game
 	 */
 	public void render(Graphics g) {
 		
 		g.setBackground(Color.black);
 		
 		if(player.isDead()) {
 			
 			org.newdawn.slick.Font f=g.getFont();
 			int width=f.getWidth(deathMessage);
 			g.setColor(Color.red);
 			g.drawString(deathMessage,
 						gc.getWidth()/2-width/2,
 						gc.getHeight()/2);
 			
 			return;
 		}
 		
 		Point2D center=player.getPosition();
 		
 		Point2D upperLeft=screenToGame(new int[]{0,0}, center);
 		Point2D lowerRight=screenToGame(new int[]{gc.getWidth(),gc.getHeight()-hudSize}, center);
 		
 		//Draws the map
 		List<Space> spaces=map.getData((int)(upperLeft.getX()-2), (int)(upperLeft.getY()-2), (int)(lowerRight.getX()+2), (int)(lowerRight.getY()+2));
 		for(Space s: spaces) drawWall(s, center, g);
 		for(Space s: spaces) drawInnerSpace(s, center, g);
 		
 		
 		GraphicsLoader.setFilterType(Image.FILTER_NEAREST);
 		
 		//Updates all animations in the cache
 		int renderDelta=timeCount-lastRenderTime;
 		lastRenderTime=timeCount;
 		cache.update(renderDelta);
 		
 		//Draws and animates entities
 		List<Creature> gameCreatures=game.getCreatures(upperLeft.getX()-5, upperLeft.getY()-5, lowerRight.getX()+5, lowerRight.getY()+5);
 		for(Creature c: gameCreatures) drawCreature(c, center);
 		
 		//Draws the HUD
 		drawHUD(g);
 	}
 	
 	//Draws the inside of a space
 	void drawInnerSpace(Space s, Point2D center, Graphics g) {
 		
 		GraphicsLoader.setFilterType(Image.FILTER_NEAREST);
 		
 		//Draws the floor tiles
 		
 		int[] upperLeft=Util.snapPoint(s.upperLeft());
 		Tile[][] floor=s.getFloor();
 		
 		for(int i=0; i<floor.length; i++)
 		for(int j=0; j<floor[i].length; j++) {
 			Tile t=floor[i][j];
 			int x=(int)upperLeft[0]+i;
 			int y=(int)upperLeft[1]+j;
 			Point2D gameCoords=new Point2D.Double(x, y);
 			
 			Image tileImage=null;
 			try {
 				int[] screenCoords=gameToScreen(gameCoords, center);
 				tileImage=GraphicsLoader.loadImageAt(t.path);
 				tileImage.draw(screenCoords[0], screenCoords[1], (int)(1*gameToScreenFactor), (int)(1*gameToScreenFactor));
 			} catch(SlickException e) {
 				System.out.println("Could not create image for tile "+t+" at location ("+x+", "+y+")...");
 				e.printStackTrace();
 			}
 		}
 	}	
 	
 	//Draws the wall around a space
 	public void drawWall(Space s, Point2D center, Graphics g) {
 		
 		GraphicsLoader.setFilterType(Image.FILTER_NEAREST);
 		
 		Wall w=s.getWallType();
 		int[] upperLeft=Util.snapPoint(s.upperLeft());
 		
 		//{N, S, E, W, NE, NW, SE, SW}
 		String[] wallPaths=new String[8];
 		if(w==Wall.NONE) {
 			for(int i=0; i<wallPaths.length; i++) {
 				wallPaths[i]=GraphicsPaths.EMPTY.path;
 			}
 		} else if(w==Wall.DEFAULT) {
 			wallPaths[0]=GraphicsPaths.WALL_N.path;
 			wallPaths[1]=GraphicsPaths.WALL_S.path;
 			wallPaths[2]=GraphicsPaths.WALL_E.path;
 			wallPaths[3]=GraphicsPaths.WALL_W.path;
 			wallPaths[4]=GraphicsPaths.WALL_NE.path;
 			wallPaths[5]=GraphicsPaths.WALL_NW.path;
 			wallPaths[6]=GraphicsPaths.WALL_SE.path;
 			wallPaths[7]=GraphicsPaths.WALL_SW.path;
 		}
 		//Top edge
 		int y1=upperLeft[1]-1;
 		
 		try {
 			Image wallN=GraphicsLoader.loadImageAt(wallPaths[0]);
 			for(int i=0; i<s.width(); i++) {
 				int x=upperLeft[0]+i;
 				
 				Point2D gameCoords=new Point2D.Double(x, y1);
 				int[] screenCoords=gameToScreen(gameCoords, center);
 				wallN.draw(screenCoords[0], screenCoords[1], (int)(1*gameToScreenFactor), (int)(1*gameToScreenFactor));
 			}
 		} catch(SlickException e) {
 			System.out.println("Could not create image for north wall...");
 			e.printStackTrace();
 		}
 		
 		//Bottom edge
 		int y2=upperLeft[1]+s.height();
 		
 		try {
 			Image wallS=GraphicsLoader.loadImageAt(wallPaths[1]);
 			for(int i=0; i<s.width(); i++) {
 				int x=upperLeft[0]+i;
 
 				Point2D gameCoords=new Point2D.Double(x, y2);
 				int[] screenCoords=gameToScreen(gameCoords, center);
 				wallS.draw(screenCoords[0], screenCoords[1], (int)(1*gameToScreenFactor), (int)(1*gameToScreenFactor));
 			}
 		} catch(SlickException e) {
 			System.out.println("Could not create image for south wall...");
 			e.printStackTrace();
 		}
 		
 		//Right edge
 		int x2=upperLeft[0]+s.width();
 		
 		try {
 			Image wallE=GraphicsLoader.loadImageAt(wallPaths[2]);
 			for(int i=0; i<s.height(); i++) {
 				int y=upperLeft[1]+i;
 
 				Point2D gameCoords=new Point2D.Double(x2, y);
 				int[] screenCoords=gameToScreen(gameCoords, center);
 				wallE.draw(screenCoords[0], screenCoords[1], (int)(1*gameToScreenFactor), (int)(1*gameToScreenFactor));
 
 			}
 		} catch(SlickException e) {
 			System.out.println("Could not create image for west wall...");
 			e.printStackTrace();
 		}
 		
 		//Left edge
 		int x1=upperLeft[0]-1;
 		
 		try {
 			Image wallW=GraphicsLoader.loadImageAt(wallPaths[3]);
 			for(int i=0; i<s.height(); i++) {
 				int y=upperLeft[1]+i;
 
 				Point2D gameCoords=new Point2D.Double(x1, y);
 				int[] screenCoords=gameToScreen(gameCoords, center);
 				wallW.draw(screenCoords[0], screenCoords[1], (int)(1*gameToScreenFactor), (int)(1*gameToScreenFactor));
 			}
 		} catch(SlickException e) {
 			System.out.println("Could not create image for east wall...");
 			e.printStackTrace();
 		}
 		
 		//Upper right tile
 		try {
 			Image wallNE=GraphicsLoader.loadImageAt(wallPaths[4]);
 			int x=upperLeft[0]+s.width();
 			int y=upperLeft[1]-1;
 			
 			Point2D gameCoords=new Point2D.Double(x, y);
 			int[] screenCoords=gameToScreen(gameCoords, center);
 			wallNE.draw(screenCoords[0], screenCoords[1], (int)(1*gameToScreenFactor), (int)(1*gameToScreenFactor));
 
 		} catch(SlickException e) {
 			System.out.println("Could not create image for NE wall...");
 			e.printStackTrace();
 		}
 		
 		//Upper left tile
 		try {
 			int x=upperLeft[0]-1;
 			int y=upperLeft[1]-1;
 			
 			Image wallNW=GraphicsLoader.loadImageAt(wallPaths[5]);
 			Point2D gameCoords=new Point2D.Double(x, y);
 			int[] screenCoords=gameToScreen(gameCoords, center);
 			wallNW.draw(screenCoords[0], screenCoords[1], (int)(1*gameToScreenFactor), (int)(1*gameToScreenFactor));
 
 		} catch(SlickException e) {
 			System.out.println("Could not create image for NW wall...");
 			e.printStackTrace();
 		}
 
 		//Lower right tile
 		try {
 			int x=upperLeft[0]+s.width();
 			int y=upperLeft[1]+s.height();
 			
 			Image wallSE=GraphicsLoader.loadImageAt(wallPaths[6]);
 			Point2D gameCoords=new Point2D.Double(x, y);
 			int[] screenCoords=gameToScreen(gameCoords, center);
 			wallSE.draw(screenCoords[0], screenCoords[1], (int)(1*gameToScreenFactor), (int)(1*gameToScreenFactor));
 		} catch(SlickException e) {
 			System.out.println("Could not create image for SE wall...");
 			e.printStackTrace();
 		}
 
 		//Lower left tile
 		try {
 			int x=upperLeft[0]-1;
 			int y=upperLeft[1]+s.height();
 			
 			Image wallSW=GraphicsLoader.loadImageAt(wallPaths[7]);
 			Point2D gameCoords=new Point2D.Double(x, y);
 			int[] screenCoords=gameToScreen(gameCoords, center);
 			wallSW.draw(screenCoords[0], screenCoords[1], (int)(1*gameToScreenFactor), (int)(1*gameToScreenFactor));
 		} catch(SlickException e) {
 			System.out.println("Could not create image for SW wall...");
 			e.printStackTrace();
 		}
 	}
 	
 	public void drawCreature(Creature c, Point2D center) {
 		Action actionToAnimate=null;
 		List<Action> actions=c.getActions();
 		
 		int actionNum=0;
 		
 		for(Action a: actions) {				
 			
 			actionNum++;
 
 		//TEST
 			if(!c.containsAttribute(Attribute.PLAYER)) {
 			}
 			
 			if(actionToAnimate==null ||
 				a.type().getPriority()>actionToAnimate.type().getPriority()) {
 				
 					actionToAnimate=a;
 			}
 		}
 		
 		//No action
 		if(actionToAnimate==null || actionToAnimate.getActionAnimations().size()==0) {
 			try {
 				
 			//TEST
 				if(!c.containsAttribute(Attribute.PLAYER)) {
 					//System.out.println("Static image");
 				}
 				
 				Image[] images=null;
 				
 				//Finishes the previous animation
 				if(cache.get(c)!=null) {
 					List<Animation> anims=cache.get(c);
 					List<Image> imageList=new ArrayList<>();
 					for(int j=0; j<anims.size(); j++)
 						imageList.add(anims.get(j).getCurrentFrame());
 					images=imageList.toArray(new Image[0]);
 					
 				}
 				
 				//Otherwise, draws the creature's static sprite
 				else {
 					images=new Image[1];
 					images[0]=GraphicsLoader.loadImage(c.getSpritePath());
 				}
 				
 				for(Image image: images)
 					drawImage(image, center, new ImageData(c.getPosition().x,
 						c.getPosition().y,
 						c.getSize().getWidth(),
 						c.getSize().getHeight(),
 						c.getDirection(),
 						c.shouldRotate(),
 						c.shouldFlip() && c.isLeft()));
 
 				
 			} catch(SlickException e) {
 				//Should not happen
 				e.printStackTrace();
 			}
 		
 		} else {
 			
 			
 			Image[] images=null;
 			boolean shouldFlip=false;
 			boolean shouldRotate=false;
 			
 			List<ActionAnimation> actionAnimations=actionToAnimate.getActionAnimations();
 			
 			
 			//Attack action
 			if(actionToAnimate.type()==ActionType.ATTACK) {
 				
 				shouldFlip=false;
 				shouldRotate=true;
 				
 				Animation creatureAnim=null;
 				Animation weaponAnim=null;
 				
 				//Checks the animation cache for memory equality of the current attack action
 				if(actionToAnimate==cache.getAction(c)) {
 					//Gets the animation in the cache
 					List<Animation> list=cache.get(c);
 					creatureAnim=list.get(0);
 					weaponAnim=list.get(1);
 				} else {
 					//Creates a new animation and adds it to the cache
 					
 					creatureAnim=GraphicsLoader.loadAttack(actionAnimations.get(0).getSpritePath());
 					
 					try {
 						if(actionAnimations.get(1)==null) weaponAnim=GraphicsLoader.makeAnimation(GraphicsPaths.EMPTY.path);
 					} catch(SlickException e) {
 						//Should not happen
 						e.printStackTrace();
 					}
 					weaponAnim=GraphicsLoader.load(actionAnimations.get(1).getSpritePath());
 					
 					setDuration(creatureAnim, actionToAnimate.getTimer());
 					setDuration(weaponAnim, actionToAnimate.getTimer());
 					
 					List<Animation> animList=new ArrayList<>();
 					animList.add(creatureAnim);
 					animList.add(weaponAnim);
 					cache.add(c, actionToAnimate, animList);
 				}
 				
 				Image creatureImage=creatureAnim.getCurrentFrame();
 				Image weaponImage=weaponAnim.getCurrentFrame();
 				
 				images=new Image[]{creatureImage, weaponImage};
 				
 			
 			//Other type of action (no weapon)
 			} else {
 				
 				shouldFlip=c.shouldFlip() && c.isLeft();
 				shouldRotate=c.shouldRotate();
 				
 				Animation anim=null;
 				
 				//Checks the animation cache for type equality of the current attack action
 				if(cache.getAction(c)!=null && cache.getAction(c).type()==actionToAnimate.type()) {
 					//Gets the animation in the cache
 					anim=cache.get(c).get(0);
 				} else {
 					//Creates a new animation and adds it to the cache
 					if(actionToAnimate.type()==ActionType.MOVE) anim=GraphicsLoader.loadMove(actionAnimations.get(0).getSpritePath());
 					else if(actionToAnimate.type()==ActionType.PICKUP) anim=GraphicsLoader.load(actionAnimations.get(0).getSpritePath());
 					setDuration(anim, actionToAnimate.getTimer());
 					
 					List<Animation> animList=new ArrayList<>();
 					animList.add(anim);
 					cache.add(c, actionToAnimate, animList);
 				}
 				
 				images=new Image[]{anim.getCurrentFrame()};
 			}
 			
 			//Scales, centers, rotates and draws the current images
 			//Updates all animations
 			for(int index=0; index<images.length; index++) {
 				
 				//Patch -- avoids a null pointer exception
 				if(images[index]==null) try {
 					images[index]=GraphicsLoader.loadImageAt(GraphicsPaths.EMPTY.path);
 				} catch(SlickException e) {
 					
 				}
 				
 				drawImage(images[index], center, actionAnimations.get(index), shouldFlip, shouldRotate);
 			}
 		}
 	}
 	
 	//A packaging class for the data required to draw an image
 	static class ImageData {
 		
 		double x, y, width, height, rot;
 		boolean shouldRotate, shouldFlip;
 		
 		public ImageData(double x0, double y0, double w, double h, double r, boolean rotate, boolean flip) {
 			x=x0;
 			y=y0;
 			width=w;
 			height=h;
 			rot=r;
 			shouldRotate=rotate;
 			shouldFlip=flip;
 		}
 	}
 	
 	//Draws an image to the screen given the game coordinate center, x and y coordinates, a width and height, and an angle
 	void drawImage(Image i, Point2D center, ImageData data) {
 		
 		//Transforms the coordinates from game to screen
 		double[] gameCoords=new double[]{data.x, data.y};
 		int[] screenCoords=gameToScreen(new Point2D.Double(gameCoords[0], gameCoords[1]), center);
 		
 		//Resizes the image
 		Image toDraw=i.getScaledCopy((int)(gameToScreenFactor*data.width),
 									(int)(gameToScreenFactor*data.height));
 		
 		//If shouldFlip is true, do not rotate the image - just flip it horizontally
 		if(data.shouldFlip) {
 			System.out.println("Flip!");
 			toDraw=toDraw.getFlippedCopy(true, false);
 		}
 		
 		//If shouldRotate is true, rotate the image according to the angle
 		if(data.shouldRotate) {
 			//Centers the image
 			screenCoords=centerImage(screenCoords, toDraw);
 			
 			//Rotates the image
 			setRotationCenter(toDraw);
 			toDraw.setRotation((float)toDegrees(data.rot));
 		}
 				
 		//Draws the image
 		toDraw.draw(screenCoords[0], screenCoords[1]);
 	}
 	
 	//Draws an image to the screen given the game coordinate center and the image's ActionAnimation
 	void drawImage(Image i, Point2D center, ActionAnimation actionAnim, boolean flip, boolean rotate) {
 		
 		drawImage(i, center, new ImageData(actionAnim.getPos().x,
 											actionAnim.getPos().y,
 											actionAnim.getSize().getWidth(),
 											actionAnim.getSize().getHeight(),
 											actionAnim.getAngle(),
 											rotate,
 											flip));
 	}
 	
 	void setDuration(Animation a, int dur) {
 		if(dur==0) a.setImageDuration((int)(1000./Constants.DEFAULT_IMAGE_RATE));
 		else a.setDuration(dur+animationDurationBuffer);
 	}
 	
 	//Draw's the player's HUD
 	void drawHUD(Graphics g) {
 		
 		int lineSize=20; //The spacing between each line
 		
 		int horzDisplacement=10; //The displacement from the horizontal slot the text is in
 		int vertDisplacement=15; //The displacement from the screen's bottom
 		
 		//Bars for separating elements
 		int bar=10;
 		
 		int invSpace=gc.getWidth()-bar-200; //The width for displaying inventory
 		int xpSpace=gc.getWidth()-bar-invSpace; //The width for displaying xp
 		
 		int invHorzSlots=3;
 		int invVertSlots=2;
 		
 		int xpHorzSlots=2;
 		int xpVertSlots=2;
 		
 		Text[][] titles=new Text[invHorzSlots][invVertSlots];
 		Text[][] items=new Text[invHorzSlots][invVertSlots];
 		
 		titles[0][0]=new Text("Weapon", Color.white);
 		titles[1][0]=new Text("Armour", Color.white);
 		titles[2][0]=new Text("Shield", Color.white);
 		titles[0][1]=new Text("Helmet", Color.white);
 		titles[1][1]=new Text("Boots", Color.white);
 		titles[2][1]=new Text("Potions", Color.white);
 		
 		Item weapon=player.getInventory().getWeapon();
 		Item armour=player.getInventory().getArmour();
 		Item shield=player.getInventory().getShield();
 		Item helmet=player.getInventory().getHelmet();
 		Item boots=player.getInventory().getBoots();
 		
 		int attackPotions=0;
 		int defensePotions=0;
 		int healthPotions=0;
 		for(int i=0; i<player.getInventory().getNumPotions(); i++) {
 			Potion p=(Potion) player.getInventory().getPotion(i);
 			if(p.containsAttribute(Attribute.ATTACK_POTION)) {
 				attackPotions++;
 			} else if(p.containsAttribute(Attribute.DEFENSE_POTION)) {
 				defensePotions++;
 			} else if(p.containsAttribute(Attribute.HEALTH_POTION)) {
 				healthPotions++;
 			}
 		}
 		
 		items[0][0]= Text.getText(weapon);
 		items[1][0]= Text.getText(armour);
 		items[2][0]= Text.getText(shield);
 		items[0][1]= Text.getText(helmet);
 		items[1][1]= Text.getText(boots);
 		IndexPair potionSlot=new IndexPair(2, 1);
 		
 		int ribbonRectHeight=5;
 		int boundingRectHeight=vertDisplacement;
 		for(int i=0; i<Math.max(invVertSlots, xpVertSlots); i++) {
 			boundingRectHeight+=2*lineSize+bar;
 		}
 		hudSize=boundingRectHeight+ribbonRectHeight;
 		
 		g.setColor(Color.black);
 		g.fill(new Rectangle(0, gc.getHeight()-boundingRectHeight, gc.getWidth(), boundingRectHeight));
 		
 		g.setColor(Color.red);
 		g.fill(new Rectangle(0,
 							gc.getHeight()-boundingRectHeight-ribbonRectHeight,
 							gc.getWidth()*player.getHealth()/player.getMaxHealth(),
 							ribbonRectHeight));
 		
 		for(int i=0; i<invHorzSlots; i++)
 		for(int j=0; j<invVertSlots; j++) {
 			
 			int horzSlot=(i*invSpace)/invHorzSlots;
 			int vertSlot=gc.getHeight()-bar;
 			for(int vertIndex=j; vertIndex<invVertSlots-1; vertIndex++) {
 				vertSlot-=2*lineSize+bar;
 			}
 			
 			//Displays the item title
 			g.setColor(titles[i][j].getColor()) ;
 			g.drawString(titles[i][j].getText(),
 					horzSlot,
 					vertSlot-lineSize*2);
 			
 			//Handles potions
 			if(potionSlot.x==i && potionSlot.y==j) {
 				
 				int slotSize=invSpace/invHorzSlots;
 				
 				g.setColor(Attribute.ATTACK_POTION.color);
 				g.drawString(""+attackPotions,
 							horzSlot,
 							vertSlot-lineSize*1);
 				
 				g.setColor(Attribute.DEFENSE_POTION.color);
 				g.drawString(""+defensePotions,
 							horzSlot+(int)(slotSize/3.),
 							vertSlot-lineSize*1);
 				
 				g.setColor(Attribute.HEALTH_POTION.color);
 				g.drawString(""+healthPotions,
 							horzSlot+(int)(2*slotSize/3.),
 							vertSlot-lineSize*1);
 				continue;
 			}
 			
 			//Displays the item
 			g.setColor(items[i][j].getColor());
 			g.drawString(items[i][j].getText(),
 					horzSlot,
 					vertSlot-lineSize*1);
 		}
 		
 		g.setColor(Color.white);
 		
 		int vertSlot=gc.getHeight()-vertDisplacement;
 		
 		g.drawString("XP", 
 					gc.getWidth()-xpSpace+horzDisplacement,
 					gc.getHeight()-vertDisplacement-lineSize*2);
 		g.drawString(""+player.getXP()+"/"+player.getNextLevelXP(),
 				gc.getWidth()-xpSpace+horzDisplacement,
 				vertSlot-lineSize*1);
 		
		g.drawString("Level",
 				gc.getWidth()-xpSpace/2+horzDisplacement,
 				vertSlot-lineSize*2);
		g.drawString(""+player.getLevel(),
 				gc.getWidth()-xpSpace/2+horzDisplacement,
 				vertSlot-lineSize*1);
 		
 		vertSlot-=2*lineSize+bar;
 		
 		g.drawString("Floor",
  				gc.getWidth()-xpSpace+horzDisplacement,
  				vertSlot-lineSize*2);
 		g.drawString(""+game.getFloor(),
  				gc.getWidth()-xpSpace+horzDisplacement,
  				vertSlot-lineSize);
 	}
 	
 	
 	
 
 	@Override
 	public void init(GameContainer gc, StateBasedGame gm)
 			throws SlickException {
 		this.gc = gc;
 		init();
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame gm, Graphics g)
 			throws SlickException {
 		render(g);
 
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame gm, int delta)
 			throws SlickException {
 		update(delta);
 
 	}
 
 	@Override
 	public int getID() {
 		return id;
 	}
 
 	@Override
 	public void mouseMoved(int oldx, int oldy, int newx, int newy) {
 	}
 
 	@Override
 	public void mousePressed(int button, int x, int y) {
 	}
 
 	@Override
 	public void mouseReleased(int button, int x, int y) {
 	}
 
 	@Override
 	public void mouseClicked(int button, int x, int y, int clickCount) {
 
 	}
 
 	@Override
 	public void mouseDragged(int oldx, int oldy, int newx, int newy) {
 	}
 
 	@Override
 	public void keyPressed(int key, char c) {
 		keysPressed.put(key, timeCount);
 		keysPressedOnce.put(key, true);
 		keyEventQueue.add(key);
 	}
 
 	@Override
 	public void keyReleased(int key, char c) {
 		keysPressed.put(key, -1);
 		keysPressedOnce.put(key, false);
 		keyEventQueue.remove(key);
 	}
 }
