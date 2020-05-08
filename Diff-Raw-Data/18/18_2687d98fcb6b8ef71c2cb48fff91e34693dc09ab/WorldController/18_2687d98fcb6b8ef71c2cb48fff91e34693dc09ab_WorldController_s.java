 package pruebas.Controllers;
 
 import java.util.ArrayList;
 
 import pruebas.Entities.Cell;
 import pruebas.Entities.Unit;
 import pruebas.Entities.helpers.AttackUnitAction;
 import pruebas.Entities.helpers.DefendUnitAction;
 import pruebas.Entities.helpers.MoveUnitAction;
 import pruebas.Entities.helpers.NoneUnitAction;
 import pruebas.Entities.helpers.PlaceUnitAction;
 import pruebas.Entities.helpers.UnitAction;
 import pruebas.Networking.ServerDriver;
 import pruebas.Renders.GameEngine;
 import pruebas.Renders.WorldRender;
 import pruebas.Renders.helpers.CellHelper;
 
 import com.badlogic.gdx.utils.JsonValue;
 
 public class WorldController {
 
 	private WorldRender render;
 
 	public Cell[][] cellGrid;
 	private final float deltaX = 122.0F;
 	private final float gridX = 70.0F;
 	private final float gridY = 20.0F;
 
 	public int player;
 	private String gameId;
 	private boolean firstTurn;
 	public int enemiesCount;
 	public int allysCount;
 	public boolean gameEnded = false;
 
 	public WorldController(JsonValue data, int turn) {
 		this.player = data.getInt("player");
 		this.gameId = data.getString("game_id");
 		init();
 
 		render = new WorldRender(this);
 
 		readData(data);
 		if (firstTurn) {
 			render.initFirstTurn();
 		} else {
 			if (turn == 2) { // First playable turn, only FirstTurn actions
 								// (PlaceActions), so nothing to show
 				render.initNormalTurn();
 			} else {
 				render.initTurnAnimations();
 			}
 		}
 	}
 
 	private void readData(JsonValue values) {
 		if (values.get("data") != null
 				&& values.get("data").asString().equals("none")) {
 			firstTurn = true;
 		} else {
 			enemiesCount = 0;
 			allysCount = 0;
 			readPlayerData(values.get("data1"), 1);
 			readPlayerData(values.get("data2"), 2);
 		}
 	}
 
 	private void readPlayerData(JsonValue values, int playerNum) {
 		JsonValue child;
 		JsonValue temp;
 		String action;
 		int x, y;
 		String strValues = values.asString();
 		if (strValues.equals("ended")) {
 
 		} else {
 			values = ServerDriver.parseJson(strValues);
 			boolean isEnemy = player != playerNum;
 
 			for (int i = 0; i < values.size; i++) {
 				child = values.get(i);
 
 				temp = child.get("cell");
 				x = temp.getInt("x");
 				y = temp.getInt("y");
 
 				Unit unit = new Unit(child.getString("unit_name"), isEnemy, child.getInt("unit_hp"));
 				UnitAction unitA;
 				action = child.getString("action");
 
 				if (action.equals("place")) {
 					unitA = new PlaceUnitAction();
 					((PlaceUnitAction) unitA).unitName = child.getString("unit_name");
 				} else if (action.equals("attack")) {
 					unitA = new AttackUnitAction(unit.getRange() == 1);
 					JsonValue cells = child.get("target");
 					int cellX = cells.getInt("x");
 					int cellY = cells.getInt("y");
 
 					((AttackUnitAction) unitA).target = cellGrid[cellX][cellY];
 				} else if (action.equals("move")) {
 					unitA = new MoveUnitAction();
 					JsonValue cells = child.get("target");
 					JsonValue pair = null;
 					for (int c = 0; c < cells.size; c++) {
 						pair = cells.get(c);
 						int cellX = pair.getInt("x");
 						int cellY = pair.getInt("y");
 
 						((MoveUnitAction) unitA).moves.add(cellGrid[cellX][cellY]);
 					}
				} else if (action.equals("defense")) {
 					unitA = new DefendUnitAction();
 				} else {
 					unitA = new NoneUnitAction();
 				}
 
 				cellGrid[x][y].setUnit(unit, playerNum);
 				cellGrid[x][y].setAction(unitA, playerNum);
 
 				if (playerNum == player)
 					allysCount++;
 				else
 					enemiesCount++;
 			}
 		}
 	}
 
 	private void createMap() {
 		/*
 		 * x x x o x x x
 		 */
 		int[][] oddNeighbours = { { 0, -1 }, { -1, 1 }, { 1, 0 }, { 1, 1 },
 				{ 0, 1 }, { -1, 0 } };
 
 		/*
 		 * x x x o x x x
 		 */
 		int[][] evenNeighbours = { { -1, -1 }, { 0, -1 }, { 1, 0 }, { 0, 1 },
 				{ 1, -1 }, { -1, 0 } };
 
 		float yoffset = 0f;
 		float dx = deltaX;// (float) ((3f / 4f) * hexaWidht);
 		float dy = CellHelper.CELL_HEIGHT + 1;// (float) ((Math.sqrt(3f) / 2f) *
 												// hexaWidht);
 
 		ArrayList<int[]> temp = new ArrayList<int[]>();
 		for (int h = 0; h < 6; h++) {
 			for (int v = 0; v < 9; v++) {
 				Cell c = new Cell();
 				c.setVisible(true);
 				temp.clear();
 
 				if (v % 2 == 0) {
 					yoffset = dy / 2;
 
 					for (int i = 0; i < 6; i++) {
 						if (inMap(v + oddNeighbours[i][0], h
 								+ oddNeighbours[i][1])) {
 							temp.add(new int[] { v + oddNeighbours[i][0],
 									h + oddNeighbours[i][1] });
 						}
 					}
 				} else {
 					yoffset = 0;
 
 					for (int i = 0; i < 6; i++) {
 						if (inMap(v + evenNeighbours[i][0], h
 								+ evenNeighbours[i][1])) {
 							temp.add(new int[] { v + evenNeighbours[i][0],
 									h + evenNeighbours[i][1] });
 						}
 					}
 				}
 				c.neigbours = new int[temp.size()][2];
 				for (int i = 0; i < temp.size(); i++) {
 					c.neigbours[i] = temp.get(i);
 				}
 				c.setPosition(gridX + v * dx, gridY + yoffset + (h * dy));
 				c.setGrisPosition(v, h);
 
 				cellGrid[v][h] = c;
 			}
 
 		}
 	}
 
 	protected void init() {
 		this.cellGrid = new Cell[9][6];
 
 		createMap();
 	}
 
 	public void addUnit(Unit unit, int x, int y) {
 		Cell cell = cellAt(x, y);
 		cell.placeUnit(unit, player);
 	}
 
 	public Cell cellAt(float x, float y) {
 		int cellX = 0, cellY = 0;
 		Cell cell = this.cellGrid[cellX][cellY];
 
 		while (cell.getX() < x && ++cellX < 9) {
 			cell = this.cellGrid[cellX][cellY];
 		}
 		if (!inMap(--cellX, cellY))
 			return null;
 
 		cell = this.cellGrid[cellX][cellY];
 		while (cell.getY() < y && ++cellY < 6) {
 			cell = this.cellGrid[cellX][cellY];
 		}
 		if (!inMap(cellX, --cellY))
 			return null;
 
 		return this.cellGrid[cellX][cellY];
 	}
 
 	public boolean inMap(int x, int y) {
 		return (x >= 0) && (x < 9) && (y >= 0) && (y < 6);
 	}
 
 	public void tap(float x, float y) {
 		Cell cell = cellAt(x, y);
 		if (cell != null)
 			cell.setState(Cell.State.MOVE_TARGET);
 	}
 
 	public boolean placeUnit(float x, float y, Unit unit) {
 		Cell cell = cellAt(x, y);
 		if (cell != null && cell.getState() == Cell.State.ABLE_TO_PLACE && cell.getUnit(player) == null) {
 			cell.placeUnit(unit, player);
 			return true;
 		}
 		return false;
 	}
 
 	public void update(float paramFloat) {
 	}
 
 	// -------------Para poder poner una unidad para probar
 	public void setCellState(float x, float y, Cell.State state) {
 		Cell cell = cellAt(x, y);
 		if (cell != null) {
 			cell.setState(state);
 		}
 	}
 
 	public Cell cellAtByGrid(int x, int y) {
 		return cellGrid[x][y];
 	}
 
 	public void setCellStateByGridPos(int x, int y, Cell.State state) {
 		cellGrid[x][y].setState(state);
 	}
 
 	public void assignFirstTurnAvailablePlaces() {
 		if (player == 1) {
 			cellGrid[0][5].setState(Cell.State.ABLE_TO_PLACE);
 			cellGrid[0][4].setState(Cell.State.ABLE_TO_PLACE);
 			cellGrid[0][3].setState(Cell.State.ABLE_TO_PLACE);
 			cellGrid[0][2].setState(Cell.State.ABLE_TO_PLACE);
 			cellGrid[0][1].setState(Cell.State.ABLE_TO_PLACE);
 			cellGrid[0][0].setState(Cell.State.ABLE_TO_PLACE);
 			cellGrid[1][5].setState(Cell.State.ABLE_TO_PLACE);
 			cellGrid[1][4].setState(Cell.State.ABLE_TO_PLACE);
 			cellGrid[1][3].setState(Cell.State.ABLE_TO_PLACE);
 			cellGrid[1][3].setState(Cell.State.ABLE_TO_PLACE);
 			cellGrid[1][2].setState(Cell.State.ABLE_TO_PLACE);
 			cellGrid[1][1].setState(Cell.State.ABLE_TO_PLACE);
 			cellGrid[2][1].setState(Cell.State.ABLE_TO_PLACE);
 			cellGrid[2][2].setState(Cell.State.ABLE_TO_PLACE);
 			cellGrid[2][3].setState(Cell.State.ABLE_TO_PLACE);
 			cellGrid[2][4].setState(Cell.State.ABLE_TO_PLACE);
 		} else {
 			cellGrid[6][4].setState(Cell.State.ABLE_TO_PLACE);
 			cellGrid[6][3].setState(Cell.State.ABLE_TO_PLACE);
 			cellGrid[6][2].setState(Cell.State.ABLE_TO_PLACE);
 			cellGrid[6][1].setState(Cell.State.ABLE_TO_PLACE);
 			cellGrid[7][5].setState(Cell.State.ABLE_TO_PLACE);
 			cellGrid[7][4].setState(Cell.State.ABLE_TO_PLACE);
 			cellGrid[7][3].setState(Cell.State.ABLE_TO_PLACE);
 			cellGrid[7][2].setState(Cell.State.ABLE_TO_PLACE);
 			cellGrid[7][1].setState(Cell.State.ABLE_TO_PLACE);
 			cellGrid[8][5].setState(Cell.State.ABLE_TO_PLACE);
 			cellGrid[8][4].setState(Cell.State.ABLE_TO_PLACE);
 			cellGrid[8][3].setState(Cell.State.ABLE_TO_PLACE);
 			cellGrid[8][2].setState(Cell.State.ABLE_TO_PLACE);
 			cellGrid[8][1].setState(Cell.State.ABLE_TO_PLACE);
 			cellGrid[8][0].setState(Cell.State.ABLE_TO_PLACE);
 		}
 	}
 
 	public void deleteAllUnits() {
 		for (int h = 0; h < 6; h++) {
 			for (int v = 0; v < 9; v++) {
 				cellGrid[v][h].removeUnit(player);
 			}
 		}
 	}
 
 	public void removeAllDeadUnits() {
 		for (int h = 0; h < 6; h++) {
 			for (int v = 0; v < 9; v++) {
 				cellGrid[v][h].removeDeadUnits();
 			}
 		}
 	}
 
 	public void sendTurn() {
 		String data = null;
 		String result = null;
 		if (gameEnded) {
 			if (enemiesCount == 0 && allysCount > 0)
 				result = "victory";
 			else if (enemiesCount > 0 && allysCount == 0)
 				result = "defeat";
 			else if (enemiesCount == 0 && allysCount == 0)
 				result = "draw";
 		} else {
 			StringBuilder builder = new StringBuilder();
 			builder.append("{");
 
 			Cell cell;
 			for (int h = 0; h < 6; h++) {
 				for (int v = 0; v < 9; v++) {
 					cell = cellGrid[v][h];
 					cell.addDataToJson(builder, player);
 				}
 			}
 			// Delete the last comma
 			builder.deleteCharAt(builder.length() - 1);
 
 			builder.append("}");
 			data = builder.toString();
 		}
 
 		ServerDriver.sendGameTurn(GameController.getInstancia().getUser().getId(),
 				gameId, player, data, result);
 
 	}
 
 	public WorldRender getRender() {
 		return render;
 	}
 
 	public void initNormalTurn() {
 		render.initNormalTurn();
 	}
 
 	public void leaveGame() {
 		GameEngine.getInstance().openMenuGames();
 	}
 }
