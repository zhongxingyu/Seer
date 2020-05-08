 package pruebas.Controllers;
 
 import java.util.ArrayList;
 
 import pruebas.Entities.Cell;
 import pruebas.Entities.Unit;
 import pruebas.Entities.helpers.AttackUnitAction;
 import pruebas.Entities.helpers.DefendUnitAction;
 import pruebas.Entities.helpers.MoveUnitAction;
 import pruebas.Entities.helpers.PlaceUnitAction;
 import pruebas.Entities.helpers.UnitAction;
 import pruebas.Networking.ServerDriver;
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
 
 	public WorldController(int player, String data) {
 		// TODO: Data reader
 		this.player = player;
 		init();
 
 		render = new WorldRender(this);
 
 		readData(data);
 		if (firstTurn) {
 			render.initFirstTurn();
 		} else {
 			render.initNormalTurn();
 		}
 	}
 
 	private void readData(String strData) {
 		JsonValue values = ServerDriver.ProcessResponce(strData);
 		if (values.isString() && values.toString().equals("none")) {
 			firstTurn = true;
 		} else {
 			JsonValue data = values.get("data");
 			JsonValue child;
 			JsonValue temp;
 			String action;
 			int x, y;
 			for (int i = 0; i < data.size; i++) {
 				child = data.get(i);
 
 				temp = child.get("cell");
 				x = temp.getInt("x");
 				y = temp.getInt("y");
 
 				UnitAction unitA;
 				action = child.getString("action");
 				if (action.equals("place")) {
 					unitA = new PlaceUnitAction();
 
 				} else if (action.equals("attack")) {
 					unitA = new AttackUnitAction();
 				} else if (action.equals("move")) {
 					unitA = new MoveUnitAction();
 				} else {
 					unitA = new DefendUnitAction();
 				}
 
 				unitA.origin = cellGrid[x][y];
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
 		this.cellGrid = ((Cell[][]) java.lang.reflect.Array.newInstance(
 				Cell.class, new int[] { 9, 6 }));
 
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
 
 	public void placeUnit(float x, float y, Unit unit) {
 		Cell cell = cellAt(x, y);
 		if (cell != null && cell.getState() == Cell.State.ABLE_TO_PLACE) {
 			cell.placeUnit(unit, player);
 		}
 	}
 
 	public void update(float paramFloat) {
 	}
 
 	// -------------Para poder poner una unidad para probar
 	public void setCellEnable(float x, float y) {
 		Cell cell = cellAt(x, y);
 		if (cell != null) {
 			cell.setState(Cell.State.ABLE_TO_PLACE);
 		}
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
 
 	public void sendTurn() {
 		StringBuilder builder = new StringBuilder();
 
 		builder.append("{");
 
 		Cell cell;
 		UnitAction action;
 		for (int h = 0; h < 6; h++) {
 			for (int v = 0; v < 9; v++) {
 				cell = cellGrid[v][h];
 				action = cell.getAction(player);
 				if (action != null) {
 					String data = cell.getAction(player).getData();
 					System.out.println(data);
 					builder.append(data);
 					builder.append(",");
 				}
 			}
 		}
 		// Delete the last comma
 		builder.deleteCharAt(builder.length() - 1);
 
 		builder.append("}");
 
 		System.out.println(builder.toString());
		ServerDriver.gameTurn(GameController.getInstancia().getUser().getId(),
				gameId, player, builder.toString());
 	}
	
 	public WorldRender getRender() {
 		return render;
 	}
 }
