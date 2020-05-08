 package ru.gafi.game;
 
 import ru.gafi.common.Point;
 import ru.gafi.game.actions.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * User: Michael
  * Date: 20.05.13
  * Time: 20:45
  */
 public class TableController {
 	private List<ITableListener> listeners = new ArrayList<>();
 	private boolean[][] burnTable;
 	private TableModel tableModel;
 	private ActionHistory history;
 	private PathFinder pathFinder;
 	private Figure[] figures = Figure.values();
 	private RNG random;
 
 	public TableController() {
 		pathFinder = new PathFinder();
 		random = new RNG();
 	}
 
 	private int columnCount() {
 		return tableModel.columnCount();
 	}
 
 	private int rowCount() {
 		return tableModel.rowCount();
 	}
 
 	public void tryMove(Point from, Point to) {
 		if (isValidMove(from, to)) {
 			PathFinder.FindPathResult findResult = findPath(from, to);
 			if (findResult.pathFinded) {
 				startRecord();
 				doAction(new ActionMove(findResult.path));
 				burnCompleteFigures();
 				boolean gameEnded = false;
 				if (!usefulStep()) {
 					addRandomFigures();
 					if (isLose()) {
 						lose();
 						gameEnded = true;
 					}
 				}
 				if (!gameEnded && isWin()) {
 					win();
 					gameEnded = true;
 				}
 				if (!gameEnded && getCountOfBusyCells() == 0) {
 					addRandomFigures();
 				}
 				stopRecord();
 			} else {
 				fireOnMoveFailure();
 			}
 		} else {
 			fireOnMoveFailure();
 		}
 	}
 
 	private void startRecord() {
 		fireOnStepBegin();
 		history.startRecord(random.getSeed());
 	}
 
 	private void stopRecord() {
 		history.stopRecord(random.getSeed());
 		fireOnStepFinish();
 	}
 
 	private boolean isValidMove(Point from, Point to) {
 		return tableModel.getCell(from.x, from.y).figure != null && tableModel.getCell(to.x, to.y).figure == null;
 	}
 
 	public void addRandomFigures() {
 		addRandomFigures(calculateCountAddingFigures());
 		burnCompleteFigures();
 	}
 
 	private int calculateCountAddingFigures() {
 		int countOfFreeCells = getCountOfFreeCells();
 		int allCells = rowCount() * columnCount();
 		float perc = countOfFreeCells / (float) allCells;
 		int count;
 		if (perc > 0.5) {
 			count = Math.min(countOfFreeCells - allCells / 2, 5);
 		} else {
 			count = 2;
 		}
 		return count;
 	}
 
 	private void fireOnStepBegin() {
 			for (ITableListener listener : listeners) {
 				listener.onStepBegin();
 			}
 		}
 
 	private void fireOnStepFinish() {
 			for (ITableListener listener : listeners) {
 				listener.onStepFinish();
 			}
 		}
 
 	private void fireOnMoveFailure() {
 		for (ITableListener listener : listeners) {
 			listener.onMoveFailure();
 		}
 	}
 
 	public void moveFigure(Point[] path) {
 		Point from = path[0];
 		Point to = path[path.length - 1];
 		TableCell fromCell = tableModel.getCell(from.x, from.y);
 		TableCell toCell = tableModel.getCell(to.x, to.y);
 		toCell.figure = fromCell.figure;
 		fromCell.figure = null;
 
 		fireOnMoveFigure(new ITableListener.MoveFigureResult(path));
 	}
 
 	private void fireOnMoveFigure(ITableListener.MoveFigureResult result) {
 		for (ITableListener listener : listeners) {
 			listener.onMoveFigure(result);
 		}
 	}
 
 	public void StartGame() {
 		fireOnStartGame();
 	}
 
 	private void fireOnStartGame() {
 		for (ITableListener listener : listeners) {
 			listener.onStartGame();
 		}
 	}
 
 	public void addRandomFigures(int count) {
 		int availableCells = getCountOfFreeCells();
 		int realCount = Math.min(availableCells, count);
 		for (int i = 0; i < realCount; i++) {
 			addRandomFigure();
 		}
 	}
 
 	private void addRandomFigure() {
 		int x = randomRange(0, columnCount());
 		int y = randomRange(0, rowCount());
 		int figureIndex = randomRange(0, figures.length);
 		Point point = findNearestFreeCell(x, y);
 		Figure figure = figures[figureIndex];
 		doAction(new ActionAddFigure(point, figure));
 	}
 
 	private int randomRange(int from, int to) {
 		return random.range(from, to);
 	}
 
 	private void addFigure(Point point, Figure figure) {
 		tableModel.getCell(point.x, point.y).figure = figure;
 		fireOnAddFigure(point, figure);
 	}
 
 	private void fireOnAddFigure(Point point, Figure figure) {
 		for (ITableListener listener : listeners) {
 			listener.onAddFigure(point, figure);
 		}
 	}
 
 	private Point findNearestFreeCell(int x, int y) {
 		while (tableModel.getCell(x, y).figure != null) {
 			if (x == columnCount() - 1) {
 				x = 0;
 				if (y == rowCount() - 1) {
 					y = 0;
 				} else {
 					y++;
 				}
 			} else {
 				x++;
 			}
 		}
 		return new Point(x, y);
 	}
 
 	private int getCountOfFreeCells() {
 		int countOfClosedCells = getCountOfBusyCells();
 		int allCellsCount = columnCount() * rowCount();
 		int availableCells = allCellsCount - countOfClosedCells;
 		return availableCells;
 	}
 
 	private int getCountOfBusyCells() {
 		int count = 0;
 		for (int i = 0; i < columnCount(); i++) {
 			for (int j = 0; j < rowCount(); j++) {
 				if (tableModel.getCell(i, j).figure != null) {
 					count++;
 				}
 			}
 		}
 		return count;
 	}
 
 	public boolean usefulStep() {
 		for (int i = 0; i < columnCount(); i++) {
 			for (int j = 0; j < rowCount(); j++) {
 				if (burnTable[i][j]) return true;
 			}
 		}
 		return false;
 	}
 
 	public void burnCompleteFigures() {
 		clearBurnTable();
 		fillBurnTable();
 		burnTable();
 	}
 
 	private void burnTable() {
 		for (int i = 0; i < columnCount(); i++) {
 			for (int j = 0; j < rowCount(); j++) {
 				if (burnTable[i][j]) {
 					Point point = new Point(i, j);
 					doAction(new ActionRemoveFigure(point, tableModel.getCell(i, j).figure));
 					if (!tableModel.getCell(i, j).opened) {
 						doAction(new ActionOpenCell(point));
 					}
 				}
 			}
 		}
 	}
 
 	private void fillBurnTable() {
 		for (int i = 0; i < columnCount(); i++) {
 			for (int j = 0; j < rowCount(); j++) {
 				Figure figure = tableModel.getCell(i, j).figure;
 				if (figure != null) {
 					checkForBurn(i, j);
 				}
 			}
 		}
 	}
 
 	private void checkForBurn(int x, int y) {
 		Figure figure = tableModel.getCell(x, y).figure;
 		if (figure == null) return;
 		for (int i = 0; i < figure.columnCount(); i++) {
 			int sx = x - i;
 			if (sx < 0 || sx + figure.columnCount() > columnCount()) continue;
 			for (int j = 0; j < figure.rowCount(); j++) {
 				if (figure.mask[i][j] == 0) continue;
 				int sy = y - j;
 				if (sy < 0 || sy + figure.rowCount() > rowCount()) continue;
 				if (figureCollected(figure, sx, sy)) {
 					markForBurn(figure, sx, sy);
 				}
 			}
 		}
 	}
 
 	private void clearBurnTable() {
 		for (int i = 0; i < columnCount(); i++) {
 			for (int j = 0; j < rowCount(); j++) {
 				burnTable[i][j] = false;
 			}
 		}
 	}
 
 	private void markForBurn(Figure figure, int x, int y) {
 		for (int i = 0; i < figure.columnCount(); i++) {
 			for (int j = 0; j < figure.rowCount(); j++) {
 				if (figure.mask[i][j] > 0) burnTable[x + i][y + j] = true;
 			}
 		}
 	}
 
 	private boolean figureCollected(Figure figure, int x, int y) {
 		for (int i = 0; i < figure.columnCount(); i++) {
 			for (int j = 0; j < figure.rowCount(); j++) {
 				if (figure.mask[i][j] > 0 && tableModel.getCell(x + i, y + j).figure != figure) return false;
 			}
 		}
 		return true;
 	}
 
 	private void setCellOpened(Point p, boolean opened) {
 		TableCell tableCell = tableModel.getCell(p.x, p.y);
 		if (tableCell.opened != opened) {
 			tableCell.opened = opened;
 			fireOnCellOpenedChanged(new ITableListener.CellOpenChangedResult(p, opened));
 		}
 	}
 
 	private void fireOnCellOpenedChanged(ITableListener.CellOpenChangedResult result) {
 		for (ITableListener listener : listeners) {
 			listener.onCellOpenedChanged(result);
 		}
 	}
 
 	public void removeFigure(Point p) {
 		Figure figure = tableModel.getCell(p.x, p.y).figure;
 		tableModel.getCell(p.x, p.y).figure = null;
 
 		fireOnRemoveFigure(new ITableListener.RemoveFigureResult(p, figure));
 	}
 
 	private void fireOnRemoveFigure(ITableListener.RemoveFigureResult result) {
 		for (ITableListener listener : listeners) {
 			listener.onRemoveFigure(result);
 		}
 	}
 
 	private void setSeed(long seed) {
 		random.setSeed(seed);
 	}
 
 	public void debugPreWin() {
 		for (int i = 0; i < columnCount(); i++) {
 			for (int j = 0; j < rowCount(); j++) {
 				Point point = new Point(i, j);
 
 				TableCell tableCell = tableModel.getCell(i, j);
 				if (tableCell.figure != null) {
 					removeFigure(point);
 				}
 				if (!tableCell.opened) {
 					setCellOpened(point, true);
 				}
 			}
 		}
 
 		addFigure(new Point(0, 0), figures[0]);
 		addFigure(new Point(1, 0), figures[0]);
 		addFigure(new Point(2, 0), figures[0]);
 		addFigure(new Point(0, 1), figures[0]);
 		setCellOpened(new Point(1, 1), false);
 	}
 
 	public void debugAddFigure(Point point, Figure figure) {
 		if (getCountOfFreeCells() == 0) return;
 		Point nearestFreeCell = findNearestFreeCell(point.x, point.y);
 		addFigure(nearestFreeCell, figure);
 	}
 
 	public void debugRemoveFigure(Point point) {
 		removeFigure(point);
 	}
 
 	public void debugMove(Point from, Point to) {
 		moveFigure(new Point[]{from, to});
 	}
 
 	public void debugTryMove(Point from, Point to) {
 		PathFinder.FindPathResult findResult = findPath(from, to);
 		if (findResult.pathFinded) {
 			moveFigure(findResult.path);
 		}
 	}
 
 	private void win() {
 		clearHistory();
 		fireOnWin();
 	}
 
 	private void fireOnWin() {
 		for (ITableListener listener : listeners) {
 			listener.onWin();
 		}
 	}
 
 	private void lose() {
 		clearHistory();
 		fireOnLose();
 	}
 
 	private void fireOnLose() {
 		for (ITableListener listener : listeners) {
 			listener.onLose();
 		}
 	}
 
 	private boolean isWin() {
 		for (int i = 0; i < columnCount(); i++) {
 			for (int j = 0; j < rowCount(); j++) {
 				TableCell tableCell = tableModel.getCell(i, j);
 				if (!tableCell.opened) return false;
 			}
 		}
 		return true;
 	}
 
 	private boolean isLose() {
 		for (int i = 0; i < columnCount(); i++) {
 			for (int j = 0; j < rowCount(); j++) {
 				TableCell tableCell = tableModel.getCell(i, j);
 				if (tableCell.figure == null) return false;
 			}
 		}
 		return true;
 	}
 
 	private PathFinder.FindPathResult findPath(Point from, Point to) {
 		return pathFinder.find(tableModel, from, to);
 	}
 
 	public void addListener(ITableListener _tableListener) {
 		listeners.add(_tableListener);
 	}
 
 	public void SetTable(TableModel tableModel) {
 		this.tableModel = tableModel;
 		burnTable = new boolean[columnCount()][rowCount()];
 	}
 
 	public void setHistory(ActionHistory history) {
 		this.history = history;
 		setSeed(history.currentSeed);
 	}
 
 	public void makeFirstMove() {
 		addRandomFigures(rowCount() * columnCount() / 2);
 		burnCompleteFigures();
 	}
 
 	public void clearTable() {
 		fireOnClearTable();
 		clearHistory();
 		for (int i = 0; i < columnCount(); i++) {
 			for (int j = 0; j < rowCount(); j++) {
 				TableCell tableCell = tableModel.getCell(i, j);
 				tableCell.figure = null;
 				tableCell.opened = false;
 			}
 		}
 
 	}
 
 	private void fireOnClearTable() {
 		for (ITableListener listener : listeners) {
 			listener.onClearTable();
 		}
 	}
 
 	private void doAction(GameAction action) {
 		execute(action);
 		history.push(action);
 	}
 
 	private void execute(GameAction action) {
 		switch (action.type) {
 			case AddFigure:
 				ActionAddFigure actionAddFigure = (ActionAddFigure) action;
 				addFigure(actionAddFigure.point, actionAddFigure.figure);
 				break;
 			case RemoveFigure:
 				ActionRemoveFigure actionRemoveFigure = (ActionRemoveFigure) action;
 				removeFigure(actionRemoveFigure.point);
 				break;
 			case MoveFigure:
 				ActionMove actionMove = (ActionMove) action;
 				moveFigure(actionMove.path);
 				break;
 			case OpenCell:
 				ActionOpenCell actionOpenCell = (ActionOpenCell) action;
 				setCellOpened(actionOpenCell.point, true);
 				break;
 			case StepBegin:
 				fireOnStepBegin();
 				ActionStepBegin actionStepBegin = (ActionStepBegin) action;
 				List<GameAction> actions = actionStepBegin.actions;
 				for (int i = 0; i < actions.size(); i++) {
 					execute(actions.get(i));
 				}
 				setSeed(actionStepBegin.endSeed);
 				fireOnStepFinish();
 				break;
 		}
 	}
 
 	private void cancel(GameAction action) {
 		switch (action.type) {
 			case AddFigure:
 				ActionAddFigure actionAddFigure = (ActionAddFigure) action;
 				removeFigure(actionAddFigure.point);
 				break;
 			case RemoveFigure:
 				ActionRemoveFigure actionRemoveFigure = (ActionRemoveFigure) action;
 				addFigure(actionRemoveFigure.point, actionRemoveFigure.figure);
 				break;
 			case MoveFigure: {
 				ActionMove actionMove = (ActionMove) action;
 				Point[] path = actionMove.path;
 				Point[] reversed = new Point[path.length];
 				System.arraycopy(path, 0, reversed, 0, path.length);
 				for (int i = 0; i < reversed.length / 2; i++) {
 					Point temp = reversed[i];
 					reversed[i] = reversed[reversed.length - i - 1];
 					reversed[reversed.length - i - 1] = temp;
 				}
 				moveFigure(reversed);
 			}
 			break;
 			case OpenCell:
 				ActionOpenCell actionOpenCell = (ActionOpenCell) action;
 				setCellOpened(actionOpenCell.point, false);
 				break;
 			case StepBegin: {
 				fireOnStepBegin();
 				ActionStepBegin actionStepBegin = (ActionStepBegin) action;
 				List<GameAction> actions = actionStepBegin.actions;
 				for (int i = actions.size() - 1; i >= 0; i--) {
 					cancel(actions.get(i));
 				}
 				setSeed(actionStepBegin.beginSeed);
 				fireOnStepFinish();
 			}
 			break;
 		}
 	}
 
 	public void undo() {
 		if (!history.isUndoEmpty()) {
 			cancel(history.popUndo());
 		}
 	}
 
 	public void redo() {
 		if (!history.isRedoEmpty()) {
 			execute(history.popRedo());
 		}
 	}
 
 	private void clearHistory() {
 		history.clear();
 	}
 }
