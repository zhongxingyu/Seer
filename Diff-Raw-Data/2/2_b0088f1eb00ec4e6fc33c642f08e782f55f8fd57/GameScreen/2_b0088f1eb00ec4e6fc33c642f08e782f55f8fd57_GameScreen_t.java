 package io.github.ldears.ld26.core;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.utils.Array;
 import io.github.ldears.ld26.events.InputEventHandler;
 import io.github.ldears.ld26.map.*;
 import io.github.ldears.ld26.map.Container;
 import io.github.ldears.ld26.models.Action;
 import io.github.ldears.ld26.models.GameModel;
 import io.github.ldears.ld26.render.Renderer;
 import io.github.ldears.ld26.render.TexturedWalls;
 import io.github.ldears.ld26.sound.SoundManager;
 import io.github.ldears.ld26.sound.Sounds;
 
 import java.awt.*;
 import java.util.*;
 import java.util.List;
 
 import static io.github.ldears.ld26.map.TileType.*;
 
 /**
  * @author dector
  */
 public class GameScreen implements Screen, InputProcessor {
 
 	private GameModel model;
 
 	private Renderer renderer;
 
 	public GameScreen() {
 		model = new GameModel();
 
 		// WARNING: to use wall textures, room must be proportional to texture size (64x64)
 
 		// TODO mockup
 		{
 			TileType[][] tileTypeMap = {
 //					{ WALL_LEFT, 	CEIL, 	CEIL, 		CEIL, 	CEIL, 	WALL_MC, 			CEIL, 	CEIL, 	CEIL, 	CEIL, 	WALL_MC, 	 CEIL, 		CEIL, 	WALL_RIGHT },
 //					{ WALL_LEFT, 	EMPTY, 	EMPTY, 		EMPTY, 	EMPTY, 	WALL_MIDDLE, 		EMPTY,	EMPTY, 	EMPTY, 	EMPTY, 	WALL_MIDDLE, EMPTY, 	EMPTY, 	WALL_RIGHT },
 //					{ WALL_LEFT, 	EMPTY, 	EMPTY, 		EMPTY, 	EMPTY, 	WALL_MIDDLE, 		EMPTY,  EMPTY, 	EMPTY, 	EMPTY, 	WALL_MIDDLE, EMPTY, 	EMPTY, 	WALL_RIGHT },
 //					{ WALL_LEFT, 	EMPTY, 	DOOR_TOP, 	EMPTY, 	EMPTY, 	WALL_MIDDLE, 		EMPTY,  EMPTY, 	EMPTY, 	EMPTY, 	WALL_MIDDLE, EMPTY, 	EMPTY, 	WALL_RIGHT },
 //					{ WALL_LEFT, 	EMPTY, 	DOOR_BOTTOM,EMPTY, 	EMPTY, 	WALL_MIDDLE, 		EMPTY,  EMPTY, 	EMPTY, 	EMPTY, 	WALL_MIDDLE, EMPTY, 	EMPTY, 	WALL_RIGHT },
 //					{ WALL_LEFT, 	CEIL, 	CEIL, 		CEIL, 	CEIL, 	WALL_MC,	 		CEIL, 	CEIL, 	CEIL, 	CEIL, 	WALL_MC, 	 CEIL, 		CEIL, 	WALL_RIGHT },
 //					{ WALL_LEFT, 	EMPTY, 	DOOR_TOP, 	EMPTY, 	EMPTY, 	INNER_DOOR_TOP, 	EMPTY,  EMPTY, 	EMPTY, 	EMPTY, 	WALL_MIDDLE, EMPTY, 	EMPTY, 	WALL_RIGHT },
 //					{ WALL_LEFT, 	EMPTY, 	DOOR_BOTTOM,EMPTY, 	EMPTY, 	INNER_DOOR_BOTTOM, 	EMPTY,	EMPTY, 	EMPTY, 	EMPTY, 	WALL_MIDDLE, EMPTY, 	EMPTY, 	WALL_RIGHT },
 //					{ WALL_CLB, 	CEIL, 	CEIL, 		CEIL, 	CEIL, 	CEIL,				CEIL, 	CEIL, 	CEIL, 	CEIL, 	CEIL, 		 CEIL,		CEIL, 	WALL_CRB }
 					{ WALL_LEFT, 	CEIL, 	CEIL, 		CEIL, 		CEIL, 	CEIL, 		CEIL, 	CEIL, 	CEIL, 	CEIL, 	CEIL, 	CEIL, 	CEIL, 	WALL_MC, 	 CEIL, 		CEIL, 	CEIL, 	CEIL, 	CEIL, 	CEIL,  CEIL,	CEIL, 	CEIL,  CEIL,	CEIL, 	WALL_RIGHT },
 					{ WALL_LEFT, 	EMPTY, 	EMPTY, 		EMPTY, 		EMPTY, 	EMPTY, 		EMPTY, 	BC_TL, 	BC_TR,  EMPTY, 	EMPTY, 	EMPTY, 	EMPTY, 	WALL_MD, 	 EMPTY, 	EMPTY, 	EMPTY, 	EMPTY, 	EMPTY, 	EMPTY, EMPTY,	EMPTY, 	EMPTY, EMPTY,	EMPTY, 	WALL_RIGHT },
 					{ WALL_LEFT, 	EMPTY,  COMP_TL, 	COMP_TR, 	EMPTY, 	EMPTY,		EMPTY, 	BC_ML, 	BC_MR, 	EMPTY,  DOOR_T,	EMPTY, 	EMPTY, 	IN_DOOR_T, 	 EMPTY, 	EMPTY, 	EMPTY, 	EMPTY, 	EMPTY, 	TV_T, EMPTY,	EMPTY, 	EMPTY, EMPTY,	EMPTY, 	WALL_RIGHT },
 					{ WALL_LEFT, 	EMPTY,  COMP_BL, 	COMP_BR, 	EMPTY, 	BOX,		EMPTY, 	BC_BL, 	BC_BR, 	EMPTY,  DOOR_B,	EMPTY, 	EMPTY, 	IN_DOOR_B, 	 EMPTY, 	SO_L, 	SO_R, 	EMPTY, 	EMPTY, 	TV_B, EMPTY,	EMPTY, 	EMPTY, EMPTY,	EMPTY, 	WALL_RIGHT },
 					{ WALL_LEFT, 	CEIL, 	CEIL, 		CEIL, 		CEIL, 	CEIL, 	WALL_MC, 	CEIL, 	CEIL, 	CEIL, 	CEIL, 	CEIL, 	CEIL, 	CEIL, 	CEIL, 		CEIL, 	CEIL, 	WALL_MC, 	CEIL, 	CEIL,  CEIL,	CEIL, 	CEIL,  CEIL,	CEIL, 	WALL_RIGHT },
 					{ WALL_LEFT, 	EMPTY, 	EMPTY, 		EMPTY, 		EMPTY, 	EMPTY, 	WALL_MD, 	EMPTY, 	EMPTY, 	EMPTY, 	CLK_T, 	EMPTY, 	EMPTY,  EMPTY,	EMPTY, 		EMPTY,	EMPTY, 	WALL_MD, 	EMPTY, 	EMPTY, EMPTY,	EMPTY, 	EMPTY, EMPTY,	EMPTY, 	WALL_RIGHT },
 					{ WALL_LEFT, 	EMPTY, 	EMPTY, 		GC_T, 	WDB_T, 		EMPTY, 	IN_DOOR_T, 	EMPTY, 	DOOR_T, 	EMPTY, 	CLK_B, 	EMPTY, 	EMPTY, 	DOOR_T, 	EMPTY,	PHONE, 	EMPTY, 	IN_DOOR_T, 	EMPTY, 	TO_T,  EMPTY,	EMPTY, 	EMPTY,  EMPTY,	EMPTY, 	WALL_RIGHT },
 					{ WALL_LEFT, 	BED_L, 	BED_R, 		GC_B, 	WDB_B, 		EMPTY, 	IN_DOOR_B, 	EMPTY, 	DOOR_B, 	EMPTY, 	EMPTY, 	EMPTY, 	EMPTY, 	DOOR_B, 	EMPTY,	MIN_T, 	EMPTY, 	IN_DOOR_B, 	EMPTY, 	TO_B,  BATH_L,	BATH_R, 	EMPTY,  EMPTY,	EMPTY, 	WALL_RIGHT },
 					{ WALL_LEFT, 	CEIL, 	CEIL, 		CEIL, 		CEIL, 	CEIL, 		CEIL, 	CEIL, 	CEIL, 	CEIL, 	CEIL, 	CEIL, 	WALL_MC, 	CEIL, 	 CEIL, 	CEIL, 	CEIL, 	CEIL, 	CEIL, 	CEIL,  CEIL,	CEIL, 	CEIL,  CEIL,	CEIL, 	WALL_RIGHT },
 					{ WALL_LEFT, 	EMPTY, 	EMPTY, 		EMPTY, 		EMPTY, 	EMPTY, 		EMPTY, 	EMPTY, 	EMPTY, 	EMPTY, 	EMPTY, 	EMPTY, 	WALL_MD, 	EMPTY, 	 EMPTY,	EMPTY, 	EMPTY, 	EMPTY, 	EMPTY, 	EMPTY,  EMPTY,	EMPTY, 	EMPTY,  EMPTY,	EMPTY, 	WALL_RIGHT },
 					{ WALL_LEFT, 	EMPTY, 	EMPTY, 		CR_TL,		CR_TML,	CR_TMR,		CR_TR, 	EMPTY, 	EMPTY, 	EMPTY, 	EMPTY, 	EMPTY, 	WALL_MD, 	DOOR_T,	 EMPTY,	EMPTY, 	MW_OV, 	EMPTY, 	CKR_T, 	EMPTY,  FRI_T,	EMPTY, 	EMPTY,  EMPTY,	EMPTY, 	WALL_RIGHT },
 					{ WALL_LEFT, 	EMPTY, 	EMPTY, 		CR_BL,		CR_BML,	CR_BMR,		CR_BR, 	EMPTY, 	EMPTY, 	EMPTY, 	EMPTY, 	EMPTY, 	WALL_MD, 	DOOR_B,	 EMPTY,	BUCKT, 	MIN_T, 	EMPTY, 	CKR_B, 	EMPTY,  FRI_B,	EMPTY, 	EMPTY,  EMPTY,	EMPTY, 	WALL_RIGHT },
 					{ WALL_CLB, 	CEIL, 	CEIL, 		CEIL, 		CEIL, 	CEIL, 		CEIL, 	CEIL, 	CEIL, 	CEIL, 	CEIL, 	CEIL, 	CEIL, 		CEIL, 	 CEIL, 	CEIL, 	CEIL, 	CEIL, 	CEIL, 	CEIL,  CEIL,	CEIL, 	CEIL,  CEIL,	CEIL, 	WALL_CRB },
 			};
 
 			// Магнітофон - mute sound. анімований. з нотками (коли грає).
 			// лампочки, світло
 
 			int width = tileTypeMap[0].length;
 			int height = tileTypeMap.length;
 
 			int[][] containtersPosition = {
 					{1, 9}, {2, 9}, {3, 9}, {4, 9}, {5, 9}, {6, 9}, {7, 9}, {8, 9}, {9, 9}, {11, 9}, {12, 9}, {13, 9}, {14, 9}, {15, 9}, {16, 9}, {17, 9}, {18, 9}, {19, 9}, {20, 9}, {21, 9}, {22, 9}, {23, 9}, {24, 9},
 					{1, 5}, {2, 5}, {3, 6}, {4, 5}, {5, 5}, {6, 5}, {7, 5}, {9, 5}, {10, 5}, {11, 5}, {12, 5}, {14, 5}, {15, 5}, {16, 5}, {17, 5}, {18, 5}, {19, 5}, {20, 5}, {21, 5}, {22, 5}, {23, 5}, {24, 5},
 					{13, 1}, {14, 1}, {15, 1}, {16, 1}, {17, 1}, {18, 1}, {19, 1}, {20, 1}, {21, 1}, {22, 1}, {23, 1}, {24, 1}
 			};
 
 			int[][] notTransperentContainers = {
 					{ 5, 9 },
 					{ 3, 6 }, { 19, 5 }, { 20, 5 }, { 21, 5 },
 					{15, 2}
 			};
 
 			int[][] notVisibleContainers = {
 					{ 7, 9 }, { 8, 9 },
 					{ 4, 5 }, {15, 5}, { 20, 5 }, { 21, 5 },
 					{15, 1}, {16, 1}, {20, 1}
 			};
 
 			Tile[][] tileMap = new Tile[width][height];
 			for (int x = 0; x < width; x++) {
 				for (int y = 0; y < height; y++) {
 					tileMap[x][y] = new Tile();
 					tileMap[x][y].type = tileTypeMap[height - y - 1][x];
 
 					/*if (tileMap[x][y].type == EMPTY && (y == 1|| y == 5 || y == 9)) {
 						tileMap[x][y].setContent(new Container(x, y, "", 5, 1, 1, true));
 					}*/
 				}
 			}
 
 			for (int i = 0; i < containtersPosition.length; i++) {
 				int x = containtersPosition[i][0];
 				int y = containtersPosition[i][1];
 
 				boolean foundT = false;
 				for (int j = 0; j < notTransperentContainers.length && !foundT; j++) {
 					if (notTransperentContainers[j][0] == x && notTransperentContainers[j][1] == y) {
 						foundT = true;
 					}
 				}
 
 				boolean foundV = false;
 				for (int j = 0; j < notVisibleContainers.length && !foundV; j++) {
 					if (notVisibleContainers[j][0] == x && notVisibleContainers[j][1] == y) {
 						foundV = true;
 					}
 				}
 
 				Container c = new Container(x, y, "", 5, 1, 1, ! foundT);
 				c.setVisible(! foundV);
 
 				tileMap[x][y].setContent(c);
 				tileMap[x][y].addContainer();
 			}
 
 			{
 				Door door1 = new Door(13, 1, "door1");
 				Door door2 = new Door(13, 5, "door2");
 				door1.setPairedDoor(door2);
 				door2.setPairedDoor(door1);
 
 				tileMap[13][1].setContent(door1);
 				tileMap[13][5].setContent(door2);
 				
 				door1 = new Door(10, 9, "door1");
 				door2 = new Door(8, 5, "door2");
 				door1.setPairedDoor(door2);
 				door2.setPairedDoor(door1);
 
 				tileMap[10][9].setContent(door1);
 				tileMap[8][5].setContent(door2);
 			}
 
 			{
 				tileMap[15][5].setContent(new Phone(15, 5, "phone", ObjectType.PHONE));
 			}
 
 			{
 				createItemAt(tileMap, ItemType.BOTTLE, 2, 5);
 				createItemAt(tileMap, ItemType.APPLE, 15, 9);
 				createItemAt(tileMap, ItemType.VANTUZ, 4, 9);
 				createItemAt(tileMap, ItemType.KNIFE, 18, 5);
 				createItemAt(tileMap, ItemType.RASTA, 22, 9);
 				createItemAt(tileMap, ItemType.GUITAR, 17, 9);
 				createItemAt(tileMap, ItemType.BOOK, 20, 1);
 				createItemAt(tileMap, ItemType.CAT, 23, 5);
 			}
 
 			TexturedWalls walls = new TexturedWalls(1);
 
 			{
 				walls.walls[0] = new TexturedWalls.Wall(TexturedWalls.WallTexture.ROSES, 1, 1, 24, 13);
 			}
 
 			{
 				Map<ItemType, List<Point>> map = new HashMap<ItemType, List<Point>>();
 
 				List<Point> bottleWin = new ArrayList<Point>();
 				bottleWin.add(new Point(20, 1));
 				map.put(ItemType.BOTTLE, bottleWin);
 
 				List<Point> w2 = new ArrayList<Point>();
 				w2.add(new Point(15, 1));
 				map.put(ItemType.APPLE, w2);
 
 				List<Point> w3 = new ArrayList<Point>();
 				w3.add(new Point(16, 1));
 				map.put(ItemType.KNIFE, w3);
 
 				List<Point> w4 = new ArrayList<Point>();
 				w4.add(new Point(18, 5));
 				map.put(ItemType.VANTUZ, w4);
 
 				List<Point> w5 = new ArrayList<Point>();
 				w5.add(new Point(4, 5));
 				map.put(ItemType.RASTA, w5);
 
 				List<Point> w6 = new ArrayList<Point>();
 				w6.add(new Point(6, 9));
 				w6.add(new Point(7, 9));
 				w6.add(new Point(8, 9));
 				map.put(ItemType.BOOK, w6);
 
 				List<Point> w7 = new ArrayList<Point>();
 				w7.add(new Point(1, 5));
 				w7.add(new Point(2, 5));
				w7.add(new Point(3, 6));
 				map.put(ItemType.GUITAR, w7);
 
 				model.setWinConditions(map);
 			}
 
 			model.setPlayerSpawn(7, 5);
 			model.init(tileMap);
 			renderer = new Renderer(model, walls);
 
 			SoundManager.instance.setMuted(false);
 		}
 
 	}
 
 	private void createItemAt(Tile[][] tilemap, ItemType type, int x, int y) {
 		Item item = new Item(x, y, type.name());
 		item.itemType = type;
 
 		((Container) (tilemap[x][y].getContent())).add(item);
 	}
 
 	@Override
 	public void render(float dt) {
 		if (won) {
 			renderer.renderWon();
 			return;
 		}
 		model.update(dt);
 		renderer.render(dt);
 	}
 
 	@Override
 	public void resize(int i, int i2) {}
 
 	@Override
 	public void show() {
 	}
 
 	@Override
 	public void hide() {
 	}
 
 	@Override
 	public void pause() {
 	}
 
 	@Override
 	public void resume() {
 	}
 
 	@Override
 	public void dispose() {
 	}
 
 	// Input
 
 	private boolean won;
 
 	@Override
 	public boolean keyDown(int key) {
 		switch (key) {
 			case Input.Keys.ESCAPE:
 				Gdx.app.exit();
 				break;
 			case Input.Keys.LEFT:
 				if (won) break;
 				model.handleEvent(InputEventHandler.InputEvent.LEFT_DOWN);
 				break;
 			case Input.Keys.RIGHT:
 				if (won) break;
 				model.handleEvent(InputEventHandler.InputEvent.RIGHT_DOWN);
 				break;
 			case Input.Keys.X:
 				if (won) break;
 				Action a = model.getAvailableAction();
 				if (a != null) {
 					switch (a) {
 						case DROP_ITEM:
 							SoundManager.instance.play(Sounds.DROP);
 							break;
 						case GET_ITEM:
 							SoundManager.instance.play(Sounds.GET);
 							break;
 						case USE_DOOR:
 							SoundManager.instance.play(Sounds.DOOR);
 							break;
 						case CALL_PHONE:
 							break;
 					}
 				}
 
 				model.handleEvent(InputEventHandler.InputEvent.X);
 				break;
 			case Input.Keys.Z:
 				if (won) break;
 				model.handleEvent(InputEventHandler.InputEvent.Z);
 				break;
 			case Input.Keys.MINUS:
 				SoundManager.instance.decMusicVolume();
 				break;
 			case Input.Keys.PLUS:
 			case Input.Keys.EQUALS:
 				SoundManager.instance.incMusicVolume();
 				break;
 			case Input.Keys.M:
 				SoundManager.instance.toggleMuted();
 				break;
 		}
 
 		renderer.updateText();
 
 		return true;
 	}
 
 	@Override
 	public boolean keyUp(int key) {
 		switch (key) {
 			case Input.Keys.LEFT:
 				if (won) break;
 				model.handleEvent(InputEventHandler.InputEvent.LEFT_UP);
 				break;
 			case Input.Keys.RIGHT:
 				if (won) break;
 				model.handleEvent(InputEventHandler.InputEvent.RIGHT_UP);
 				break;
 			case Input.Keys.X:
 				if (won) break;
 				Action a = model.getAvailableAction();
 				if (a != null && a == Action.CALL_PHONE) {
 					if (model.isWinConditionsOk()) {
 						SoundManager.instance.play(Sounds.DIAL);
 						won = true;
 					} else {
 						SoundManager.instance.play(Sounds.NO);
 					}
 				}
 				break;
 		}
 
 		renderer.updateText();
 
 		return true;
 	}
 
 	@Override
 	public boolean keyTyped(char c) {
 		return false;
 	}
 
 	@Override
 	public boolean touchDown(int i, int i2, int i3, int i4) {
 		return false;
 	}
 
 	@Override
 	public boolean touchUp(int i, int i2, int i3, int i4) {
 		return false;
 	}
 
 	@Override
 	public boolean touchDragged(int i, int i2, int i3) {
 		return false;
 	}
 
 	@Override
 	public boolean mouseMoved(int i, int i2) {
 		return false;
 	}
 
 	@Override
 	public boolean scrolled(int i) {
 		return false;
 	}
 }
