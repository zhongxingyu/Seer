 package com.gop.engine;
 
 import org.lwjgl.Sys;
 import org.newdawn.slick.util.Log;
 
 import com.gop.engine.character.Character;
 import com.gop.engine.character.Character.*;
 import com.gop.engine.character.capacities.BasicAttack;
 import com.gop.engine.character.capacities.Range;
 import com.gop.engine.character.capacities.Range.RangeType;
 import com.gop.engine.InputManager.actions;
 import com.gop.graphics.DisplayManager;
 import com.gop.graphics.GameboardRender.viewPoint;
 
 public class Game {
 
 	enum GameStatus {
 		PlacingBeforeBattle, Pending, InCharMenu, MoveSelection, TargetSelection, ExploringMap
 	}
 
 	enum Menus {
 		Actions
 	}
 
 	String[] actionMenuString = { "Move", "Attack", "Comp", "End Turn" };
 	Menus actualMenu;
 	Map map;
 	DisplayManager dm;
 	InputManager im;
 	GameStatus state;
 
 	Player[] players;
 	Player currentPlayer;
 	int indexPlayer;
 	int indexChar;
 	int nbPlayers;
 
 	int charIndex;
 	Character currentChar;
 
 	Cursor cursor;
 
 	boolean quit;
 
 	boolean charPlaced;
 	int deployementcountDown;
 	int playerCountDown;
 
 	private long timerTicksPerSecond = Sys.getTimerResolution();
 	private long lastLoopTime;
 
 	public Game(int width, int height, Map map, int nbPlayers) {
 		this.map = map;
 		dm = new DisplayManager(width, height, map);
 		this.dm.Init();
 		this.im = new InputManager();
 		this.quit = false;
 		this.map.getTile(0, 0).setHighlighted(true);
 		this.state = GameStatus.PlacingBeforeBattle;
 		this.nbPlayers = nbPlayers;
 		this.players = new Player[this.nbPlayers];
 		this.currentPlayer = null;
 		lastLoopTime = getTime();
 		if (nbPlayers == 0) {
 			this.state = GameStatus.ExploringMap;
 		}
 		cursor = new Cursor(map, dm);
 	}
 
 	private void UpdateLogic() {
 
 		long delta = getTime() - lastLoopTime;
 		lastLoopTime = getTime();
 		for (Player p : players) {
 			for (Character c : p.getChars()) {
 				c.Update(delta, map);
 			}
 		}
 		switch (state) {
 
 		case PlacingBeforeBattle:
 			if (charPlaced) {
 				if (deployementcountDown > 0) {
 					deployementcountDown--;
 					indexChar++;
 					if (indexChar < players[indexPlayer].getChars().length) {
 						currentChar = players[indexPlayer].getChars()[indexChar];
 						this.dm.getHUD().SetCurrentChar(currentChar);
 					}
 				}
 				if (deployementcountDown == 0) {
 					if (playerCountDown > 0) {
 						playerCountDown--;
 						if (indexPlayer < players.length - 1) {
 							indexPlayer++;
 							currentPlayer = players[indexPlayer];
 							deployementcountDown = currentPlayer.getChars().length;
 							indexChar = 0;
 							currentChar = players[indexPlayer].getChars()[indexChar];
 							this.dm.getHUD().SetCurrentChar(currentChar);
 							map.LightUpStartZone(indexPlayer + 1);
 						}
 					}
 					if (playerCountDown == 0) {
 						state = GameStatus.Pending;
 						map.CleanLightUpZones();
 					}
 				}
 
 				charPlaced = false;
 			}
 			break;
 
 		case Pending:
 			if (!GetTheCharToPlay()) {
 				HourglassTick();
 			} else {
 				dm.getHUD().getContextMenu().setMenu(actionMenuString);
 				actualMenu = Menus.Actions;
 				state = GameStatus.InCharMenu;
 			}
 			break;
 		case InCharMenu:
 			dm.getHUD().getContextMenu().setShow(true);
 			if (currentChar.hasMoved()) {
 				dm.getHUD().getContextMenu().DisableOption(0);
 			}
 			if (currentChar.hasAttacked()) {
 				dm.getHUD().getContextMenu().DisableOption(1);
 			}
 			break;
 		}
 
 	}
 
 	public void setPlayer(int i, Player player) {
 		players[i] = player;
 	}
 
 	private void initPlacingBeforeBattle() {
 		this.indexPlayer = 0;
 		this.currentPlayer = players[this.indexPlayer];
 		this.indexChar = 0;
 		this.currentChar = currentPlayer.getChars()[this.indexChar];
 		this.dm.getHUD().SetCurrentChar(currentChar);
 		this.playerCountDown = players.length;
 		this.deployementcountDown = currentPlayer.getChars().length;
 		this.charPlaced = false;
 		this.map.LightUpStartZone(indexPlayer + 1);
 
 	}
 
 	public boolean GetTheCharToPlay() {
 		for (Player p : players) {
 			for (Character c : p.getChars()) {
 				if (c.isReadyToPlay()) {
 					cursor.focusOn(c.getCurrentTileX(), c.getCurrentTileY());
 
 					currentPlayer = p;
 					currentChar = c;
 					this.dm.getHUD().SetCurrentChar(currentChar);
 					c.setReadyToPlay(false);
 					UpdateCursor();
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	private void HourglassTick() {
 		for (Player p : players) {
 			for (Character c : p.getChars()) {
 				c.HourglassTick();
 			}
 		}
 	}
 
 	public long getTime() {
 		return (Sys.getTime() * 1000) / timerTicksPerSecond;
 	}
 
 	private boolean gameHasEnded() {
 		boolean checker = true; // passe a false si il ya plusieurs players
 								// encore vivants
 		boolean checker2 = false; // passe a true au bout du 1er survivor
 		for (Player p : players) {
 			if (!p.hasLost()) {
 				if (!checker2) {
 					checker2 = true;
 				} else {
 					checker = false;
 				}
 			}
 		}
 		return checker;
 	}
 
 	public void run() {
 		if (this.nbPlayers != 0) {
 			initPlacingBeforeBattle();
 		}
 		actions act;
 		while (!dm.isRequestClose() && !quit && !gameHasEnded()) {
 
 			act = im.getInputs();
 			UpdateLogic();
 
 			if (!dm.getGameBoard().isBusy()) {
 				if (currentChar != null) {
 					if (!currentChar.IsMoving()) {
 						manageKeyInput(act);
 					}
 				} else {
 					manageKeyInput(act);
 				}
 			} else {
 				act = actions.none;
 			}
 			dm.Render();
 		}
 		dm.Clean();
 		if (gameHasEnded()) {
 			for (Player p : players) {
 				if (!p.hasLost()) {
 					Log.debug("Player " + p.getName() + " has won!!!");
 				}
 			}
 		}
 	}
 
 	private void PlaceChar() {
 		if (map.getTile(cursor.getposX(), cursor.getposY()).getDeploymentZone() == this.indexPlayer + 1) {
 			if (!isTileOccupied(cursor.getposX(), cursor.getposY())) {
 				currentChar.setCurrentTileX(cursor.getposX());
 				currentChar.setCurrentTileY(cursor.getposY());
 				currentChar.setHeight(map.getTile(cursor.getposX(),
 						cursor.getposY()).getHeight());
 				charPlaced = true;
 				currentChar.setPlaced(true);
 				dm.getGameBoard().getCharsToDRaw()
 						.add(players[this.indexPlayer].getChars()[indexChar]);
 			}
 		}
 	}
 
 	private boolean isTileOccupied(int X, int Y) {
 		for (Player p : players) {
 			for (Character c : p.getChars()) {
 				if (c.getCurrentTileX() == X && c.getCurrentTileY() == Y) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	private Character getCharOnTile(int X, int Y) {
 		for (Player p : players) {
 			for (Character c : p.getChars()) {
 				if (c.getCurrentTileX() == X && c.getCurrentTileY() == Y) {
 					return c;
 				}
 			}
 		}
 		return null;
 	}
 
 	private void ProcessMenuEnter() {
 		switch (actualMenu) {
 		case Actions:
 			switch (dm.getHUD().getContextMenu().getIndex()) {
 			case 0:
 				if (currentChar.hasMoved()) {
 
 				} else {
 					LightUpPossibleMovementR(currentChar.getCurrentTileX(),
 							currentChar.getCurrentTileY(),
 							currentChar.getMovement());
 					state = GameStatus.MoveSelection;
 				}
 				dm.getHUD().getContextMenu().setShow(false);
 				break;
 			case 1:
 				LightUpPossibleAttackR(currentChar.getCurrentTileX(),
 						currentChar.getCurrentTileY(), currentChar.getRange());
 				state = GameStatus.TargetSelection;
 				dm.getHUD().getContextMenu().setShow(false);
 				break;
 			case 2:
 				break;
 			case 3:
 				currentChar.TurnIsOver();
 				map.getTile(currentChar.getCurrentTileX(),
 						currentChar.getCurrentTileY()).setHighlighted(false);
 				state = GameStatus.Pending;
				map.getTile(currentChar.getCurrentTileX(),
						currentChar.getCurrentTileY()).setHighlighted(false);
 				dm.getHUD().getContextMenu().setShow(false);
 				break;
 			}
 			break;
 		}
 	}
 
 	public void LightUpPossibleMovementR(int X, int Y, int movement) {
 		if (movement <= 0) {
 			return;
 		} else {
 			int zEcart;
 
 			if (X + 1 < map.getLength()) {
 				if (map.getTile(X + 1, Y).getHeight() > map.getTile(X, Y)
 						.getHeight()) {
 					zEcart = map.getTile(X + 1, Y).getHeight()
 							- map.getTile(X, Y).getHeight();
 				} else {
 					zEcart = map.getTile(X, Y).getHeight()
 							- map.getTile(X + 1, Y).getHeight();
 				}
 				if (zEcart >= 2) {
 					if (!isTileOccupied(X + 1, Y)) {
 						map.getTile(X + 1, Y).setHighlightedGreen(true);
 					}
 					LightUpPossibleMovementR(X + 1, Y, movement - (zEcart + 1));
 				} else if (zEcart < 2) {
 					if (!isTileOccupied(X + 1, Y)) {
 						map.getTile(X + 1, Y).setHighlightedGreen(true);
 					}
 					LightUpPossibleMovementR(X + 1, Y, movement - 1);
 				}
 			}
 			if (X > 0) {
 				if (map.getTile(X - 1, Y).getHeight() > map.getTile(X, Y)
 						.getHeight()) {
 					zEcart = map.getTile(X - 1, Y).getHeight()
 							- map.getTile(X, Y).getHeight();
 				} else {
 					zEcart = map.getTile(X, Y).getHeight()
 							- map.getTile(X - 1, Y).getHeight();
 				}
 				if (zEcart >= 2) {
 					if (!isTileOccupied(X - 1, Y)) {
 						map.getTile(X - 1, Y).setHighlightedGreen(true);
 					}
 					LightUpPossibleMovementR(X - 1, Y, movement - (zEcart + 1));
 				} else if (zEcart < 2) {
 					if (!isTileOccupied(X - 1, Y)) {
 						map.getTile(X - 1, Y).setHighlightedGreen(true);
 					}
 					LightUpPossibleMovementR(X - 1, Y, movement - 1);
 				}
 			}
 			if (Y + 1 < map.getWidth()) {
 				if (map.getTile(X, Y + 1).getHeight() > map.getTile(X, Y)
 						.getHeight()) {
 					zEcart = map.getTile(X, Y + 1).getHeight()
 							- map.getTile(X, Y).getHeight();
 				} else {
 					zEcart = map.getTile(X, Y).getHeight()
 							- map.getTile(X, Y + 1).getHeight();
 				}
 				if (zEcart >= 2) {
 					if (!isTileOccupied(X, Y + 1)) {
 						map.getTile(X, Y + 1).setHighlightedGreen(true);
 					}
 					LightUpPossibleMovementR(X, Y + 1, movement - (zEcart + 1));
 				} else if (zEcart < 2) {
 					if (!isTileOccupied(X, Y + 1)) {
 						map.getTile(X, Y + 1).setHighlightedGreen(true);
 					}
 					LightUpPossibleMovementR(X, Y + 1, movement - 1);
 				}
 			}
 			if (Y > 0) {
 				if (map.getTile(X, Y - 1).getHeight() > map.getTile(X, Y)
 						.getHeight()) {
 					zEcart = map.getTile(X, Y - 1).getHeight()
 							- map.getTile(X, Y).getHeight();
 				} else {
 					zEcart = map.getTile(X, Y).getHeight()
 							- map.getTile(X, Y - 1).getHeight();
 				}
 				if (zEcart >= 2) {
 					if (!isTileOccupied(X, Y - 1)) {
 						map.getTile(X, Y - 1).setHighlightedGreen(true);
 					}
 					LightUpPossibleMovementR(X, Y - 1, movement - (zEcart + 1));
 				} else if (zEcart < 2) {
 					if (!isTileOccupied(X, Y - 1)) {
 						map.getTile(X, Y - 1).setHighlightedGreen(true);
 					}
 					LightUpPossibleMovementR(X, Y - 1, movement - 1);
 				}
 			}
 
 		}
 	}
 
 	public void LightUpPossibleAttackR(int X, int Y, Range range) {
 		if (range.getRange() <= 0) {
 			return;
 		} else {
 			switch (range.getRangeType()) {
 			case Cross:
 				for (int i = 1; i <= range.getRange(); i++) {
					if (X + i < map.getLength()) {
 						map.getTile(X + i, Y).setHighlightedRed(true);
 					}
 					if (X - i >= 0) {
 						map.getTile(X - i, Y).setHighlightedRed(true);
 					}
 
					if (Y + i < map.getWidth()) {
 						map.getTile(X, Y + i).setHighlightedRed(true);
 					}
 					if (Y - i >= 0) {
 						map.getTile(X, Y - i).setHighlightedRed(true);
 					}
 				}
 				break;
 			case Circle:
 
 				break;
 			}
 		}
 	}
 
 	private void Move() {
 		if (map.getTile(cursor.getposX(), cursor.getposY())
 				.isHighlightedGreen()
 				&& !isTileOccupied(cursor.getposX(), cursor.getposY())) {
 			map.getTile(currentChar.getCurrentTileX(),
 					currentChar.getCurrentTileY()).setHighlighted(false);
 			currentChar.setTileToGoX(cursor.getposX());
 			currentChar.setTileToGoY(cursor.getposY());
 			currentChar.setIsMoving(true);
 			currentChar.setHasMoved(true);
 			map.CleanLightUpZones();
 			state = GameStatus.InCharMenu;
 		}
 	}
 
 	private void Attack() {
 		Character target = getCharOnTile(cursor.getposX(), cursor.getposY());
 		BasicAttack.Activate(currentChar, target);
 		currentChar.setHasAttacked(true);
		cursor.focusOn(currentChar.getCurrentTileX(),
				currentChar.getCurrentTileY());
 		UpdateCursor();
 		map.CleanLightUpZones();
 		state = GameStatus.InCharMenu;
 	}
 
 	private void manageKeyInput(actions act) {
 		if (!act.equals(actions.none)) {
 			switch (act) {
 			case VIEW_SOUTH:
 				dm.getGameBoard().RequestView(viewPoint.South);
 				break;
 
 			case VIEW_NORTH:
 				dm.getGameBoard().RequestView(viewPoint.North);
 				break;
 
 			case VIEW_EAST:
 				dm.getGameBoard().RequestView(viewPoint.East);
 				break;
 
 			case VIEW_WEST:
 				dm.getGameBoard().RequestView(viewPoint.West);
 				break;
 
 			case UP:
 				if (state == GameStatus.PlacingBeforeBattle
 						|| state == GameStatus.MoveSelection
 						|| state == GameStatus.TargetSelection
 						|| state == GameStatus.ExploringMap) {
 					cursor.goUp();
 					UpdateCursor();
 				} else if (state == GameStatus.InCharMenu) {
 					dm.getHUD().getContextMenu().Previous();
 				}
 				break;
 
 			case DOWN:
 				if (state == GameStatus.PlacingBeforeBattle
 						|| state == GameStatus.MoveSelection
 						|| state == GameStatus.TargetSelection
 						|| state == GameStatus.ExploringMap) {
 					cursor.goDown();
 					UpdateCursor();
 				} else if (state == GameStatus.InCharMenu) {
 					dm.getHUD().getContextMenu().Next();
 				}
 				break;
 
 			case LEFT:
 				if (state == GameStatus.PlacingBeforeBattle
 						|| state == GameStatus.MoveSelection
 						|| state == GameStatus.TargetSelection
 						|| state == GameStatus.ExploringMap) {
 					cursor.goLeft();
 					UpdateCursor();
 				} else if (state == GameStatus.InCharMenu) {
 					dm.getHUD().getContextMenu().Previous();
 				}
 				break;
 
 			case RIGHT:
 				if (state == GameStatus.PlacingBeforeBattle
 						|| state == GameStatus.MoveSelection
 						|| state == GameStatus.TargetSelection
 						|| state == GameStatus.ExploringMap) {
 					cursor.goRigth();
 					UpdateCursor();
 				} else if (state == GameStatus.InCharMenu) {
 					dm.getHUD().getContextMenu().Next();
 				}
 				break;
 
 			case QUIT:
 				quit = true;
 				break;
 
 			case ENTER:
 				switch (state) {
 				case PlacingBeforeBattle:
 					PlaceChar();
 					break;
 				case InCharMenu:
 					ProcessMenuEnter();
 					break;
 				case MoveSelection:
 					Move();
 					break;
 				case TargetSelection:
 					Attack();
 					break;
 				case ExploringMap:
 					break;
 				case Pending:
 					break;
 				default:
 					break;
 				}
 				break;
 			}
 		}
 
 	}
 
 	public void UpdateCursor() {
 		if (currentChar.getCurrentTileX() > 0
 				&& currentChar.getCurrentTileY() > 0) {
 			map.getTile(currentChar.getCurrentTileX(),
 					currentChar.getCurrentTileY()).setHighlighted(true);
 
 		}
 		dm.getGameBoard().RequestFocusOn(cursor.getposX(), cursor.getposY(),
 				map.getTile(cursor.getposX(), cursor.getposY()).getHeight());
 		dm.getHUD().SetCurrentTarget(
 				getCharOnTile(cursor.getposX(), cursor.getposY()));
 	}
 
 	public static void main(String[] argv) {
		Map map = new Map(10, 10, "lolilol");
 		;
 		/*
 		 * try { BufferedReader in = new BufferedReader(new
 		 * FileReader("./maps/ert.map")); String save = ""; String buff =
 		 * in.readLine(); while (buff != null) { save += buff; buff =
 		 * in.readLine(); } map = new Map(save); in.close(); } catch
 		 * (FileNotFoundException ex) { ex.printStackTrace(); } catch
 		 * (IOException ex) { ex.printStackTrace(); }
 		 */
 		Character c1 = new Character(Race.Human, Gender.Male);
 		c1.setName("bobix1");
 		Character c2 = new Character(Race.Dwarf, Gender.Male);
 		c2.setName("bobix2");
 		Character c3 = new Character(Race.Elve, Gender.Male);
 		c3.setRange(5, RangeType.Cross);
 		c3.setName("bobixou1");
 		Character c4 = new Character(Race.Human, Gender.Male);
 		c4.setName("bobixou2");
 		Character c5 = new Character(Race.Human, Gender.Male);
 		c5.setName("bobixou3");
 		Player p1 = new Player("bobyx", new Character[] { c1, c2 });
 		Player p2 = new Player("bobyxou", new Character[] { c3, c4, c5 });
 
 		c3.setLifePoints(10);
 
 		map.getTile(5, 2).setDeploymentZone(1);
 		map.getTile(5, 3).setDeploymentZone(1);
 		map.getTile(5, 4).setDeploymentZone(1);
 		map.getTile(5, 5).setDeploymentZone(1);
 
 		map.getTile(2, 0).setDeploymentZone(2);
 		map.getTile(2, 1).setDeploymentZone(2);
 		map.getTile(2, 2).setDeploymentZone(2);
 		map.getTile(2, 3).setDeploymentZone(2);
 		/*
 		 * map.getTile(0, 1).setHeight(4); map.getTile(0, 2).setHeight(5);
 		 * map.getTile(2, 0).setHeight(4); map.getTile(2, 1).setHeight(5);
 		 * map.getTile(2, 2).setHeight(6); map.getTile(2, 3).setHeight(7);
 		 * 
 		 * map.getTile(3, 1).setHeight(2); map.getTile(3, 2).setHeight(1);
 		 */
 		Game g = new Game(1024, 768, map, 2);
 		g.setPlayer(0, p1);
 		g.setPlayer(1, p2);
 		g.run();
 
 	}
 }
