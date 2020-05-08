 package view;
 
 import java.awt.Font;
 import java.util.Vector;
 
 import model.exception.LoseException;
 import model.exception.VictoryException;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.SpriteSheet;
 import org.newdawn.slick.TrueTypeFont;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.tiled.TiledMap;
 
 import view.Tile.FIELD;
 import controller.MainController;
 import controller.TurnController;
 import controller.ViewController;
 
 /**
  * @author Adrien Aurel
  */
 
 public class GamePlayState extends BasicGameState {
 
 	private enum STATES {
 		START_GAME, NEW_UNIT, START_TURN, PLAY_TURN, END_TURN, SELECTING_UNIT, PAUSE_GAME, GAME_OVER
 	}
 
 	private TiledMap grassMap;
 	private int stateID = -1;
 	private STATES currentState = null;
 	private MainController main;
 	private Tile currentSelected = null;
 	private Vector<Tile> tiles;
 	private Image poison = null;
 	private Image cripple = null;
 	private Image Target = null;
 	private Image AtkTarget = null;
 	private int unitNb = 0;
 	private String[][] taba;
 	private String[][] tabb;
 	private Vector<Tile> highLight = new Vector<Tile>();
 	private Vector<Tile> atkHighLight = new Vector<Tile>();
 	private Vector<Tile> poisoned = new Vector<Tile>();
 	private Vector<Tile> crippled = new Vector<Tile>();
 	private SpriteSheet archerS;
 	private Animation archerA;
 	private SpriteSheet archerR;
 	private Animation archerAR;
 	private SpriteSheet archerMonteS;
 	private Animation archerMonteA;
 	private SpriteSheet archerMonteR;
 	private Animation archerMonteAR;
 	private SpriteSheet berserkerS;
 	private Animation berserkerA;
 	private SpriteSheet berserkerR;
 	private Animation berserkerAR;
 	private SpriteSheet bretteurS;
 	private Animation bretteurA;
 	private SpriteSheet bretteurR;
 	private Animation bretteurAR;
 	private SpriteSheet cavalierS;
 	private Animation cavalierA;
 	private SpriteSheet cavalierR;
 	private Animation cavalierAR;
 	private SpriteSheet chevalierS;
 	private Animation chevalierA;
 	private SpriteSheet chevalierR;
 	private Animation chevalierAR;
 	private SpriteSheet eclaireurS;
 	private Animation eclaireurA;
 	private SpriteSheet eclaireurR;
 	private Animation eclaireurAR;
 	private SpriteSheet fantassinS;
 	private Animation fantassinA;
 	private SpriteSheet fantassinR;
 	private Animation fantassinAR;
 	private SpriteSheet lancierS;
 	private Animation lancierA;
 	private SpriteSheet lancierR;
 	private Animation lancierAR;
 	private SpriteSheet rodeurS;
 	private Animation rodeurA;
 	private SpriteSheet rodeurR;
 	private Animation rodeurAR;
 	private SpriteSheet tankS;
 	private Animation tankA;
 	private SpriteSheet tankR;
 	private Animation tankAR;
 	private SpriteSheet pegasusS;
 	private Animation pegasusA;
 	private SpriteSheet pegasusR;
 	private Animation pegasusAR;
 	private SpriteSheet dragonS;
 	private Animation dragonA;
 	private SpriteSheet dragonR;
 	private Animation dragonAR;
 	private int popupX = -1;
 	private int popupY = -1;
 	private Image popupGold;
 	private Image popupSilver;
 	private Image calque;
 	private Tile unitOnIt;
 	private Font awtFont;
 	private TrueTypeFont font;
 	private TrueTypeFont message;
 
 	public GamePlayState(int stateID) {
 		this.stateID = stateID;
 		currentState = STATES.START_GAME;
 		tiles = new Vector<Tile>();
 		main = MainController.getInstance();
 	}
 
 	/**
 	 * Auto generate some units for player B in auto mode.
 	 */
 	private void autoGenerateBUnits() {
 		main.addUnitToB(main.getPlayerB().getBoss(), getTile(36, 18));
 		main.addUnitToB("Eclaireur", getTile(23, 15));
 		main.addUnitToB("Fantassin", getTile(23, 16));
 		main.addUnitToB("Archer", getTile(24, 17));
 		main.addUnitToB("ArcherMonte", getTile(22, 15));
 		main.addUnitToB("Cavalier", getTile(23, 18));
 	}
 
 	/**
 	 * End the turn and send the players by network.
 	 */
 	private void endTurn() {
 		currentSelected = null;
 		currentState = STATES.PAUSE_GAME;
 		main.endNewTurn();
 
 		if (main.isAuto()) {
 			sleep();
 			currentState = STATES.START_TURN;
 		} else {
 
 			main.sendEnd();
 			main.endTurn();
 			if (main.getPlayerA().getTurn()) {
 				currentState = STATES.START_TURN;
 			}
 		}
 	}
 
 	@Override
 	public int getID() {
 		return stateID;
 	}
 
 	/**
 	 * Returns the tile of a certain position.
 	 * 
 	 * @param x
 	 *            x parameter of the position
 	 * @param y
 	 *            y parameter of the position
 	 * @return the tile which have this position
 	 */
 	private Tile getTile(int x, int y) {
 		for (Tile t : tiles) {
 			if (t.x == x && t.y == y)
 				return t;
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the tile the user clicked on.
 	 * 
 	 * @param gc
 	 *            GameContainer of slick
 	 * @return the tile the user clicked on
 	 */
 	private Tile getTileClicked(GameContainer gc) {
 		Input input = gc.getInput();
 		if (input.isMousePressed(0)) {
 
 			int mouseX = input.getMouseX();
 			int mouseY = input.getMouseY();
 			int x = 0;
 			int y = 0;
 
 			x = mouseX / grassMap.getTileWidth();
 			y = mouseY / grassMap.getTileHeight();
 
 			for (Tile t : tiles) {
 				if (t.x == x && t.y == y)
 					return t;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the tile the mouse is on.
 	 * 
 	 * @param gc
 	 *            GameContainer of slick
 	 */
 	private void getTileMouseOn(GameContainer gc) {
 		Input input = gc.getInput();
 
 		int mouseOnX = input.getMouseX();
 		int mouseOnY = input.getMouseY();
 		int x = 0;
 		int y = 0;
 
 		x = mouseOnX / grassMap.getTileWidth();
 		y = mouseOnY / grassMap.getTileHeight();
 
 		popupX = -1;
 		popupY = -1;
 
 		for (Tile t : tiles) {
 			if (t.x == x && t.y == y) {
 				if (main.getUnit(t) != null) {
 					popupX = t.x * 32;
 					popupY = t.y * 32 - 32;
 					unitOnIt = t;
 				}
 			}
 
 		}
 
 	}
 
 	@Override
 	public void init(GameContainer gc, StateBasedGame sbg)
 			throws SlickException {
 		Target = new Image("res/sprites/path.png");
 		grassMap = new TiledMap("res/drasus.tmx");
 		AtkTarget = new Image("res/sprites/atk.png");
 		poison = new Image("res/sprites/poison.png");
 		cripple = new Image("res/sprites/cripple.png");
 		popupGold = new Image("res/sprites/popupGold.png");
 		popupSilver = new Image("res/sprites/popupSilver.png");
 		calque = new Image("res/sprites/calqueblanc.png");
 		archerS = new SpriteSheet("res/sprites/ArcherS.png", 32, 32);
 		archerA = new Animation(archerS, 0, 0, 1, 0, false, 500, true);
 		archerR = new SpriteSheet("res/sprites/ArcherR.png", 32, 32);
 		archerAR = new Animation(archerR, 0, 0, 1, 0, false, 500, true);
 		archerMonteS = new SpriteSheet("res/sprites/ArchermonteS.png", 32, 32);
 		archerMonteA = new Animation(archerMonteS, 0, 0, 1, 0, false, 500, true);
 		archerMonteR = new SpriteSheet("res/sprites/ArchermonteR.png", 32, 32);
 		archerMonteAR = new Animation(archerMonteR, 0, 0, 1, 0, false, 500,
 				true);
 		berserkerS = new SpriteSheet("res/sprites/BerserkS.png", 32, 32);
 		berserkerA = new Animation(berserkerS, 0, 0, 1, 0, false, 500, true);
 		berserkerR = new SpriteSheet("res/sprites/BerserkR.png", 32, 32);
 		berserkerAR = new Animation(berserkerR, 0, 0, 1, 0, false, 500, true);
 		bretteurS = new SpriteSheet("res/sprites/BretteurS.png", 32, 32);
 		bretteurA = new Animation(bretteurS, 0, 0, 1, 0, false, 500, true);
 		bretteurR = new SpriteSheet("res/sprites/BretteurR.png", 32, 32);
 		bretteurAR = new Animation(bretteurR, 0, 0, 1, 0, false, 500, true);
 		cavalierS = new SpriteSheet("res/sprites/CavalierS.png", 32, 32);
 		cavalierA = new Animation(cavalierS, 0, 0, 1, 0, false, 500, true);
 		cavalierR = new SpriteSheet("res/sprites/CavalierR.png", 32, 32);
 		cavalierAR = new Animation(cavalierR, 0, 0, 1, 0, false, 500, true);
 		chevalierS = new SpriteSheet("res/sprites/ChevalierS.png", 32, 32);
 		chevalierA = new Animation(chevalierS, 0, 0, 1, 0, false, 500, true);
 		chevalierR = new SpriteSheet("res/sprites/ChevalierR.png", 32, 32);
 		chevalierAR = new Animation(chevalierR, 0, 0, 1, 0, false, 500, true);
 		eclaireurS = new SpriteSheet("res/sprites/EclaireurS.png", 32, 32);
 		eclaireurA = new Animation(eclaireurS, 0, 0, 1, 0, false, 500, true);
 		eclaireurR = new SpriteSheet("res/sprites/EclaireurR.png", 32, 32);
 		eclaireurAR = new Animation(eclaireurR, 0, 0, 1, 0, false, 500, true);
 		fantassinS = new SpriteSheet("res/sprites/FantassinS.png", 32, 32);
 		fantassinA = new Animation(fantassinS, 0, 0, 1, 0, false, 500, true);
 		fantassinR = new SpriteSheet("res/sprites/FantassinR.png", 32, 32);
 		fantassinAR = new Animation(fantassinR, 0, 0, 1, 0, false, 500, true);
 		lancierS = new SpriteSheet("res/sprites/LancierS.png", 32, 32);
 		lancierA = new Animation(lancierS, 0, 0, 1, 0, false, 500, true);
 		lancierR = new SpriteSheet("res/sprites/LancierR.png", 32, 32);
 		lancierAR = new Animation(lancierR, 0, 0, 1, 0, false, 500, true);
 		rodeurS = new SpriteSheet("res/sprites/RodeurS.png", 32, 32);
 		rodeurA = new Animation(rodeurS, 0, 0, 1, 0, false, 500, true);
 		rodeurR = new SpriteSheet("res/sprites/RodeurR.png", 32, 32);
 		rodeurAR = new Animation(rodeurR, 0, 0, 1, 0, false, 500, true);
 		tankS = new SpriteSheet("res/sprites/TankS.png", 32, 32);
 		tankA = new Animation(tankS, 0, 0, 1, 0, false, 500, true);
 		tankR = new SpriteSheet("res/sprites/TankR.png", 32, 32);
 		tankAR = new Animation(tankR, 0, 0, 1, 0, false, 500, true);
 		pegasusS = new SpriteSheet("res/sprites/PegasusS.png", 32, 32);
 		pegasusA = new Animation(pegasusS, 0, 0, 1, 0, false, 500, true);
 		pegasusR = new SpriteSheet("res/sprites/PegasusR.png", 32, 32);
 		pegasusAR = new Animation(pegasusR, 0, 0, 1, 0, false, 500, true);
 		dragonS = new SpriteSheet("res/sprites/DragonnierS.png", 32, 32);
 		dragonA = new Animation(dragonS, 0, 0, 1, 0, false, 500, true);
 		dragonR = new SpriteSheet("res/sprites/DragonnierR.png", 32, 32);
 		dragonAR = new Animation(dragonR, 0, 0, 1, 0, false, 500, true);
 		awtFont = new Font("Times New Roman", Font.BOLD, 24);
 		font = new TrueTypeFont(awtFont, false);
 		message = new TrueTypeFont(awtFont, false);
 		/* Init Vector tiles */
 
 		for (int xAxis = 0; xAxis < grassMap.getWidth(); xAxis++) {
 			for (int yAxis = 0; yAxis < grassMap.getHeight(); yAxis++) {
 				int tileID = grassMap.getTileId(xAxis, yAxis, 0);
 				boolean block = false;
 
 				String value = grassMap.getTileProperty(tileID, "blocked",
 						"false");
 				if ("true".equals(value)) {
 					block = true;
 				}
 				value = grassMap.getTileProperty(tileID, "field", "default");
 				switch (value) {
 				case "default":
 					tiles.addElement(new Tile(xAxis, yAxis, block));
 					break;
 				case "grass":
 					tiles.addElement(new Tile(xAxis, yAxis, block, FIELD.GRASS));
 					break;
 				case "forest":
 					tiles.addElement(new Tile(xAxis, yAxis, block, FIELD.FOREST));
 					break;
 				case "mountain":
 					tiles.addElement(new Tile(xAxis, yAxis, block,
 							FIELD.MOUNTAIN));
 					break;
 				case "fort":
 					tiles.addElement(new Tile(xAxis, yAxis, block, FIELD.FORT));
 					break;
 				case "bridge":
 					tiles.addElement(new Tile(xAxis, yAxis, block, FIELD.BRIDGE));
 					break;
 				default:
 					break;
 				}
 			}
 		}
 	}
 
 	/**
 	 * Start a new Turn.
 	 * 
 	 * @param sbg
 	 *            the game
 	 */
 	private void initTurn(StateBasedGame sbg) {
 		try {
 			main.initNewTurn();
 		} catch (VictoryException e) {
 			sbg.enterState(ViewController.VICTORYSTATE);
 		}
 	}
 
 	/**
 	 * Create a new Unit by clicking on tiles.
 	 * 
 	 * @param gc
 	 *            GameContainer of slick
 	 * @param sbg
 	 *            The game
 	 * @param delta
 	 *            The framerate
 	 */
 	private void newUnit(GameContainer gc, StateBasedGame sbg, int delta) {
 		if (main.isAuto()) {
 			main.addUnit("Eclaireur", getTile(18, 3));
 			main.addUnit("Fantassin", getTile(18, 15));
 			main.addUnit("Chevalier", getTile(17, 15));
 			main.addUnit("Archer", getTile(18, 7));
 			main.addUnit("ArcherMonte", getTile(18, 17));
 			currentState = STATES.START_TURN;
 
 		} else {
 			Tile tile = getTileClicked(gc);
 
 			if (tile != null) // si clic
 				if (tile.isBlocked() == false
 						&& main.isFreeTileset(tile)
 						&& ((main.playLeft() && tile.x < grassMap.getWidth() / 2) || (!main
 								.playLeft() && tile.x >= grassMap.getWidth() / 2))) {
 					main.addUnit(main.getPlayerAUnitsNames()[unitNb], tile);
 					System.out.print("Ajout :");
 					System.out.println(main.getPlayerAUnitsNames()[unitNb]);
 					unitNb++;
 				} else
 					System.out.println("Impossible de placer une unit ici");
 
 			// si il reste des units  placer
 			currentState = STATES.NEW_UNIT;
 
 			// sinon
 			if (unitNb == main.getPlayerAUnitsNames().length) {
 				currentState = STATES.START_TURN;
 				main.sendPlayer();
 				main.recPlayer();
 				if (main.getPlayerA().getTurn() == false) {
 					currentState = STATES.PAUSE_GAME;
 				}
 			}
 		}
 	}
 
 	/**
 	 * Start the turn and check the actions of player.
 	 * 
 	 * @param gc
 	 *            GameContainer of slick
 	 * @param sbg
 	 *            The game
 	 * @param delta
 	 *            The framerate
 	 */
 	private void playTurn(GameContainer gc, StateBasedGame sbg, int delta) {
 		if (main.isAuto()) {
 			switch (TurnController.numberTurn) {
 			case 1:
 				highLight = main.canMove(tiles, getTile(18, 17));
 				break;
 			case 2:
 				main.move(getTile(21, 17), getTile(18, 17), highLight);
 				break;
 			case 3:
 				highLight = main.canMove(tiles, getTile(18, 3));
 				break;
 			case 4:
 				main.move(getTile(21, 3), getTile(18, 3), highLight);
 				break;
 			case 5:
 				try {
 					main.attack(getTile(21, 17), getTile(24, 17));
 				} catch (VictoryException e) {
 					e.printStackTrace();
 				}
 				break;
 			case 6:
 				try {
 					main.attack(getTile(21, 17), getTile(24, 17));
 				} catch (VictoryException e) {
 					e.printStackTrace();
 				}
 				break;
 			default:
 				sleep();
 				sbg.enterState(ViewController.VICTORYSTATE);
 				break;
 			}
 			currentState = STATES.END_TURN;
 		} else {
 			getTileMouseOn(gc);
 			Tile tile = getTileClicked(gc);
 			if (tile != null) {
 				if (main.isPlayerAUnit(tile)) {
 					currentState = STATES.SELECTING_UNIT;
 					currentSelected = tile;
 					if ((!main.getTurn().hasMove(main.getUnit(tile)) && !main
 							.getTurn().hasAttack(main.getUnit(tile)))) {
 						highLight = main.canMove(tiles, currentSelected);
 						atkHighLight = main
 								.atkHighLight(tiles, currentSelected);
 					}
 
 					else if ((main.getUnit(tile).hasMat()
 							&& main.getTurn().hasAttack(main.getUnit(tile)) && !main
 							.getTurn().hasMoveAfterAttack(main.getUnit(tile)))) {
 						highLight = main.canMove(tiles, currentSelected);
 					}
 
 					else if (main.getTurn().hasMove(main.getUnit(tile))
 							&& !main.getTurn().hasAttack(main.getUnit(tile))) {
 						atkHighLight = main
 								.atkHighLight(tiles, currentSelected);
 					}
 				}
 			} else if (gc.getInput().isKeyPressed(Input.KEY_SPACE)) {
				if (currentState !=  STATES.PAUSE_GAME)
						currentState = STATES.END_TURN;
 			}
 		}
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics arg2)
 			throws SlickException {
 		grassMap.render(0, 0);
 		calque.draw(32, 673);
 		if (highLight != null && highLight.isEmpty() == false) {
 			for (Tile t : highLight) {
 				Target.draw(t.x * 32, t.y * 32);
 			}
 		}
 
 		if (atkHighLight.isEmpty() == false) {
 			for (Tile t : atkHighLight) {
 				AtkTarget.draw(t.x * 32, t.y * 32);
 			}
 		}
 
 		taba = main.playerAToStringArray();
 		for (int i = 0; i < taba.length; i++) {
 			switch (taba[i][0]) {
 			case "Archer":
 				archerA.draw(Integer.parseInt(taba[i][1]) * 32,
 						Integer.parseInt(taba[i][2]) * 32);
 				break;
 			case "ArcherMonte":
 				archerMonteA.draw(Integer.parseInt(taba[i][1]) * 32,
 						Integer.parseInt(taba[i][2]) * 32);
 				break;
 			case "Berserker":
 				berserkerA.draw(Integer.parseInt(taba[i][1]) * 32,
 						Integer.parseInt(taba[i][2]) * 32);
 				break;
 			case "Bretteur":
 				bretteurA.draw(Integer.parseInt(taba[i][1]) * 32,
 						Integer.parseInt(taba[i][2]) * 32);
 				break;
 			case "Cavalier":
 				cavalierA.draw(Integer.parseInt(taba[i][1]) * 32,
 						Integer.parseInt(taba[i][2]) * 32);
 				break;
 			case "Chevalier":
 				chevalierA.draw(Integer.parseInt(taba[i][1]) * 32,
 						Integer.parseInt(taba[i][2]) * 32);
 				break;
 			case "Eclaireur":
 				eclaireurA.draw(Integer.parseInt(taba[i][1]) * 32,
 						Integer.parseInt(taba[i][2]) * 32);
 				break;
 			case "Fantassin":
 				fantassinA.draw(Integer.parseInt(taba[i][1]) * 32,
 						Integer.parseInt(taba[i][2]) * 32);
 				break;
 			case "Lancier":
 				lancierA.draw(Integer.parseInt(taba[i][1]) * 32,
 						Integer.parseInt(taba[i][2]) * 32);
 				break;
 			case "Rodeur":
 				rodeurA.draw(Integer.parseInt(taba[i][1]) * 32,
 						Integer.parseInt(taba[i][2]) * 32);
 				break;
 			case "Tank":
 				tankA.draw(Integer.parseInt(taba[i][1]) * 32,
 						Integer.parseInt(taba[i][2]) * 32);
 				break;
 			case "Dragon":
 				dragonA.draw(Integer.parseInt(taba[i][1]) * 32,
 						Integer.parseInt(taba[i][2]) * 32);
 				break;
 			case "Pegasus":
 				pegasusA.draw(Integer.parseInt(taba[i][1]) * 32,
 						Integer.parseInt(taba[i][2]) * 32);
 				break;
 			}
 
 		}
 
 		tabb = main.playerBToStringArray();
 		for (int i = 0; i < tabb.length; i++) {
 			switch (tabb[i][0]) {
 			case "Archer":
 				archerAR.draw(Integer.parseInt(tabb[i][1]) * 32,
 						Integer.parseInt(tabb[i][2]) * 32);
 				break;
 			case "ArcherMonte":
 				archerMonteAR.draw(Integer.parseInt(tabb[i][1]) * 32,
 						Integer.parseInt(tabb[i][2]) * 32);
 				break;
 			case "Berserker":
 				berserkerAR.draw(Integer.parseInt(tabb[i][1]) * 32,
 						Integer.parseInt(tabb[i][2]) * 32);
 				break;
 			case "Bretteur":
 				bretteurAR.draw(Integer.parseInt(tabb[i][1]) * 32,
 						Integer.parseInt(tabb[i][2]) * 32);
 				break;
 			case "Cavalier":
 				cavalierAR.draw(Integer.parseInt(tabb[i][1]) * 32,
 						Integer.parseInt(tabb[i][2]) * 32);
 				break;
 			case "Chevalier":
 				chevalierAR.draw(Integer.parseInt(tabb[i][1]) * 32,
 						Integer.parseInt(tabb[i][2]) * 32);
 				break;
 			case "Eclaireur":
 				if (!main.isCamo()) {
 					eclaireurAR.draw(Integer.parseInt(tabb[i][1]) * 32,
 							Integer.parseInt(tabb[i][2]) * 32);
 				}
 				break;
 			case "Fantassin":
 				fantassinAR.draw(Integer.parseInt(tabb[i][1]) * 32,
 						Integer.parseInt(tabb[i][2]) * 32);
 				break;
 			case "Lancier":
 				lancierAR.draw(Integer.parseInt(tabb[i][1]) * 32,
 						Integer.parseInt(tabb[i][2]) * 32);
 				break;
 			case "Rodeur":
 				rodeurAR.draw(Integer.parseInt(tabb[i][1]) * 32,
 						Integer.parseInt(tabb[i][2]) * 32);
 				break;
 			case "Tank":
 				tankAR.draw(Integer.parseInt(tabb[i][1]) * 32,
 						Integer.parseInt(tabb[i][2]) * 32);
 				break;
 			case "Dragon":
 				dragonAR.draw(Integer.parseInt(tabb[i][1]) * 32,
 						Integer.parseInt(tabb[i][2]) * 32);
 				break;
 			case "Pegasus":
 				pegasusAR.draw(Integer.parseInt(tabb[i][1]) * 32,
 						Integer.parseInt(tabb[i][2]) * 32);
 				break;
 			}
 		}
 
 		if (popupX > -1 && popupY > -1) {
 			if (main.getUnit(unitOnIt) != null) {
 				// If the unit is not an Eclaireur in stealth, show Life Points
 				if (!main.isCamo()
 						|| !main.isPlayerBUnit(unitOnIt)
 						|| !main.getUnit(unitOnIt).getName()
 								.equals("Eclaireur")) {
 					if (main.getUnit(unitOnIt).isPowActivate()) {
 						popupGold.draw(popupX, popupY);
 						if (main.getUnit(unitOnIt).getHp() > 99) {
 							font.drawString(popupX + 19, popupY + 7, String
 									.valueOf(main.getUnit(unitOnIt).getHp()),
 									Color.black);
 						} else if (main.getUnit(unitOnIt).getHp() > 9) {
 							font.drawString(popupX + 25, popupY + 7, String
 									.valueOf(main.getUnit(unitOnIt).getHp()),
 									Color.black);
 						} else {
 							font.drawString(popupX + 31, popupY + 7, String
 									.valueOf(main.getUnit(unitOnIt).getHp()),
 									Color.black);
 						}
 					} else {
 						popupSilver.draw(popupX, popupY);
 						if (main.getUnit(unitOnIt).getHp() > 99) {
 							font.drawString(popupX + 19, popupY + 7, String
 									.valueOf(main.getUnit(unitOnIt).getHp()),
 									Color.black);
 						} else if (main.getUnit(unitOnIt).getHp() > 9) {
 							font.drawString(popupX + 25, popupY + 7, String
 									.valueOf(main.getUnit(unitOnIt).getHp()),
 									Color.black);
 						} else {
 							font.drawString(popupX + 31, popupY + 7, String
 									.valueOf(main.getUnit(unitOnIt).getHp()),
 									Color.black);
 						}
 					}
 				}
 			}
 		}
 
 		if (main.getLastMessage() != null) {
 			message.drawString(35, 675, main.getLastMessage(), Color.black);
 		}
 
 		if (currentState != STATES.START_GAME
 				&& currentState != STATES.NEW_UNIT
 				&& currentState != STATES.START_TURN) {
 			poisoned = main.isPoisoned(tiles);
 			for (Tile t : poisoned) {
 				poison.draw(t.x * 32, t.y * 32);
 			}
 			crippled = main.isCrippled(tiles);
 			for (Tile t : crippled) {
 				cripple.draw(t.x * 32 + 20, t.y * 32 + 20);
 			}
 		}
 
 	}
 
 	/**
 	 * Check what a player want to do with the unit he selected before.
 	 * 
 	 * @param gc
 	 *            GameContainer of slick
 	 * @param sbg
 	 *            The game
 	 * @param delta
 	 *            The framerate
 	 */
 	private void selectingUnit(GameContainer gc, StateBasedGame sbg, int delta) {
 		getTileMouseOn(gc);
 		Tile tile = getTileClicked(gc);
 		if (tile != null) {
 			// on clique sur une unit de B
 			if (main.isPlayerBUnit(tile)) {
 				try {
 					main.setLastMessage(main.attack(currentSelected, tile));
 					main.sendLastMessage();
 				} catch (VictoryException e) {
 					System.out.println("J'ai gagn !");
 					// TODO: prvenir l'autre de sa dfaite !
 					sbg.enterState(ViewController.VICTORYSTATE);
 				}
 				main.sendBoth();
 				// Si tout c'est bien pass on rinitialise l'tat
 				currentState = STATES.PLAY_TURN;
 			}
 
 			else {
 				if (highLight != null)
 					main.move(tile, currentSelected, highLight);
 				main.sendBoth();
 				currentState = STATES.PLAY_TURN;
 			}
 			System.out.println(atkHighLight.size());
 			if (highLight != null)
 				highLight.clear();
 			atkHighLight.clear();
 		}
 	}
 
 	/**
 	 * Slow the game in auto mode to permit the user to see the game.
 	 */
 	private void sleep() {
 		try {
 			Thread.sleep(2000L);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Start the game by trying to connect to a server or use Auto mode.
 	 */
 	private void startGame() {
 		if (main.isAuto())
 			autoGenerateBUnits();
 		else {
 			main.connexion();
 			try {
 				Thread.sleep(500);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			while (main.isConnected() != true){
 			main.connexion();
 			}
 			main.matchedWithPlayer();
 		}
 
 		if (main.playLeft())
 			main.addUnit(main.getPlayerA().getBoss(), getTile(3, 2));
 		else
 			main.addUnit(main.getPlayerA().getBoss(), getTile(36, 18));
 
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sbg, int delta)
 			throws SlickException {
 		switch (currentState) {
 		case START_GAME:
 			startGame();
 			currentState = STATES.NEW_UNIT;
 			break;
 		case NEW_UNIT:
 			newUnit(gc, sbg, delta);
 			break;
 		case START_TURN:
 			initTurn(sbg);
 			currentState = STATES.PLAY_TURN;
 		case PLAY_TURN:
 			playTurn(gc, sbg, delta);
 		case SELECTING_UNIT:
 			selectingUnit(gc, sbg, delta);
 			break;
 		case END_TURN:
 			endTurn();
 			break;
 		case PAUSE_GAME:
 			main.recMsg();
 			getTileMouseOn(gc);
 			if (!main.isAuto()) {
 				try {
 					main.recPlayers();
 				} catch (LoseException e) {
 					sbg.enterState(ViewController.LOSESTATE);
 				}
 				if (main.isTurn())
 					currentState = STATES.START_TURN;
 			}
 			break;
 		case GAME_OVER:
 			sbg.enterState(ViewController.MAINMENUSTATE);
 			break;
 		}
 
 	}
 
 }
