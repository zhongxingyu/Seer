 package com.hack.elements.view;
 
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.tiled.*;
 import com.hack.elements.control.FreeObjectController;
 import com.hack.elements.control.ObjectController;
 import com.hack.elements.model.FreeObject;
 import com.hack.elements.model.impl.Direction;
 
 public class Main extends BasicGame {
 	int mxpos = 0;
 	int mypos = 0;
 	int startingx;
 	int startingy;
 	ObjectController controller;
 	private TiledMap map;
 	int cxpos;
 	int cypos;
 	static int screenw = 512;
 	static int screenh = 512;
 	private boolean[][] blocked;
 	private static final int SIZE = 32;
 	int globalx;
 	int globaly;
 
 	public Main() {
 		super("FUCK!");
 		// TODO Auto-generated constructor stub
 	}
 
 	public static void main(String[] args) throws SlickException {
 		AppGameContainer app = new AppGameContainer(new Main());
 
 		app.setDisplayMode(screenw, screenh, false);
 		app.start();
 
 	}
 
 	@Override
 	public void init(GameContainer container) throws SlickException {
 		startingx = 148;
 		startingy = 9;
 				
 		controller = new ObjectController();
 		controller.getModel().getHero()
 				.setImage(new Image("resources/images/player-U.png"));
 		container.setVSync(true);
 		controller.getModel().getHero().setX(startingx);
 		controller.getModel().getHero().setY(startingy);
 
 		map = new TiledMap("resources/test3.tmx");
 
 		// CORY SO HELP ME GOD IF YOU CHANGE THE STARTING PLACE OF THE PLAYER
 		// CHARACTER WITHOUT MAKING THIS STARTING POSITION CORRECT I WILL
 		// STAB EVERYTHING YOU HOLD DEAR!!!
 		globalx = 248;
 		globaly = 248;
 
 		blocked = new boolean[map.getWidth()][map.getHeight()];
 
 		for (int xAxis = 0; xAxis < map.getWidth(); xAxis++) {
 			for (int yAxis = 0; yAxis < map.getHeight(); yAxis++) {
 				int tileID = map.getTileId(xAxis, yAxis, 0);
 				String value = map.getTileProperty(tileID, "blocked","false");
 				if(tileID == 721)
 				System.out.println(value + "= value..." + tileID + " = tileID..."+ xAxis + "= xAxis..." + yAxis + "= yAxis"  );
 
 				if ("true".equals(value)) {
 					blocked[xAxis][yAxis] = true;
 //					System.out.println(value + "= value..." + tileID + " = tileID..."+ xAxis + "= xAxis..." + yAxis + "= yAxis"  );
 				}
 			}
 		}
 
 	}
 
 	@Override
 	public void render(GameContainer container, Graphics g)
 			throws SlickException {
 		FreeObject hero = controller.getModel().getHero();
 		map.render(0, 0, mxpos, mypos, screenw, screenh);
 		hero.getImage().draw(hero.getX(), hero.getY());
 	}
 
 	public boolean endOfScreen(int xpos, int ypos) {
 		if (xpos % screenw == 0)
 			return true;
 
 		if (ypos % screenh == 0)
 			return true;
 
 		return false;
 	}
 
 	public boolean borderTile(int xpos, int ypos) {
 
 		if ((cxpos == 0 && mxpos == 0)
 				|| cxpos == map.getWidth() * map.getTileWidth())
 			return true;
 		// System.out.println(cypos);
 		// System.out.println(map.getHeight());
 		// System.out.println(map.getTileHeight());
 		if ((cypos == 0 && mypos == 0)
 				|| cypos == map.getHeight() * map.getTileHeight())
 			return true;
 		// System.out.println(cypos);
 		// System.out.println(map.getHeight());
 		// System.out.println(map.getTileHeight());
 
 		return false;
 
 	}
 
 	// public boolean collision(int xpos, int ypos) {
 	// if (map.getTileSetByGID(map.getTileId(xpos, ypos, 1)).equals(
 	// map.getTileSet(1)))
 	// return true;
 	// return false;
 	// }
 
 	@Override
 	public void update(GameContainer container, int delta)
 			throws SlickException {
 		Input input = container.getInput();
 
 		FreeObject hero = controller.getModel().getHero();
 		cxpos = (int) hero.getX();
 		cypos = (int) hero.getY();
 
 		if (input.isKeyDown(Input.KEY_D)) {
 			hero.setImage(new Image("resources/images/player-R.png"));
			if (isBlocked(globalx, globaly))
 				return;
 			else if (borderTile(cxpos, cypos))
 				return;
 			else if (endOfScreen(cxpos, cypos)) {
 				mxpos = (mxpos) + (screenw / 32);
 				cxpos = (cxpos - screenw) + 1;
 				FreeObjectController.moveToPoint(hero, cxpos, cypos);
 
 			}
 
 			else
 				FreeObjectController.move(hero, Direction.EAST, .5f);
 
 		}
 		if (input.isKeyDown(Input.KEY_A)) {
 			hero.setImage(new Image("resources/images/player-L.png"));
 			if (isBlocked(globalx, globaly))
 				return;
			else if (borderTile(cxpos, cypos)) {
 				cxpos = cxpos++;
 				FreeObjectController.moveToPoint(hero, cxpos, cypos);
 			} else if (endOfScreen(cxpos - 1, cypos)) {
 //				System.out.println("end of screen");
 				cxpos = (cxpos + screenw) - 1;
 //				System.out
 //C//				System.out.println("mxpos : " + mxpos);
 				mxpos = (mxpos) - (screenw / 32);
 //				System.out.println("mxpos : " + mxpos);
 
 				FreeObjectController.moveToPoint(hero, cxpos, cypos);
 
 			}
 
 			else
 				FreeObjectController.move(hero, Direction.WEST, .5f);
 		}
 		if (input.isKeyDown(Input.KEY_W)) {
 			hero.setImage(new Image("resources/images/player-U.png"));
 //			System.out.println(cypos);
 //			System.out.println(map.getHeight());
 //			System.out.println(map.getTileHeight());
			if (isBlocked(globalx, globaly - 3)) {
 				return;
 			} else if (borderTile(cxpos, cypos)) {
 				System.out.println(cypos);
 //				System.out.println(map.getHeight());
 //				System.out.println(map.getTileHeight());
 				cypos = cypos + 8;
 				FreeObjectController.moveToPoint(hero, cxpos, cypos);
 
 				return;
 			} else if (endOfScreen(cxpos, cypos)) {
 //				System.out.println("fuck!");
 //				System.out.println(mypos);
 				mypos = (mypos) - (screenh / 32);
 //				System.out.println(mypos);
 //				System.out.println(cypos);
 				cypos = (cypos + screenh) - 1;
 //				System.out.println(cypos);
 				FreeObjectController.moveToPoint(hero, cxpos, cypos);
 
 			}
 
 			else
 				FreeObjectController.move(hero, Direction.NORTH, .5f);
 		}
 		if (input.isKeyDown(Input.KEY_S)) {
 			hero.setImage(new Image("resources/images/player-D.png"));
			if (isBlocked(globalx, globaly + 8))
 				return;
 			else if (borderTile(cxpos, cypos))
 				return;
 			else if (endOfScreen(cxpos, cypos)) {
 				mypos = (mypos) + (screenh / 32);
 				cypos = cypos - screenh + 1;
 				FreeObjectController.moveToPoint(hero, cxpos, cypos);
 				
 				
 
 			}
 
 			else
 				FreeObjectController.move(hero, Direction.SOUTH, .5f);
 		}
 		globalx = cxpos + (mxpos*32);
 		globaly = cypos + (mypos*32);
 		if (input.isKeyDown(Input.KEY_SPACE)) {
 		}
 		if (input.isKeyDown(Input.KEY_ESCAPE)) {
 		}
 
 
 	}
 
 	private boolean isBlocked(int x, int y) {
 		int xBlock = x /SIZE ;
 		int yBlock = y /SIZE ;
 		System.out.println(blocked[xBlock][yBlock] + " "+ xBlock + " " + yBlock + " " + cxpos + " " + cypos + " " + mxpos + " " + mypos);
 		return blocked[xBlock][yBlock];
 		
 	}
 }
