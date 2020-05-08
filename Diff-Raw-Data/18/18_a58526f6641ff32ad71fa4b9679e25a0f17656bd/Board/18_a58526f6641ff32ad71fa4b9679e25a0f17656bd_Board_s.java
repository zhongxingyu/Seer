 package jsk.sudoku.model;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.IdentityHashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.Callable;
 
 import jsk.sudoku.BoardType;
 
 import org.apache.log4j.Logger;
 
 public class Board implements CellListener {
 	private static final Logger log = Logger.getLogger(Board.class);
 	
 	private final Cell[][] cells;
 	public final CellBlock[] rows;
 	public final CellBlock[] columns;
 	public final CellBlock[] sections;
 	
 	private final Map<Cell, Coordinate> cheatSheet;
 
 	public final BoardType type;
 	
 	private volatile int solved;
 	
 	private static final class Coordinate {
 		final int x, y, section;
 		Coordinate(int x, int y, int section) {
 			this.x = x; this.y = y; this.section = section;
 		}
 	}
 	
 	/**
 	 * Creates a deep copy of an existing board
 	 * @param board
 	 */
 	public Board(Board board) {
 		this(board.type, board.cells.clone());
 		for (int i = 0; i < type.size; ++i) {
 			for (int j = 0; j < type.size; ++j) {
 				cells[i][j] = new Cell(cells[i][j]);
 			}
 		}
 		fillBlocks(type);
 		setupListeners();
		solved = board.solved;
 	}
 	
 	/**
 	 * Constructs a new empty board
 	 * @param baseDimension
 	 */
 	public Board(BoardType type) {
 		this(type, newCellArray(type.size));
 		fillBlocks(type);
 		setupListeners();
 	}
 	
 	private Board(BoardType type, Cell[][] cells) {
 		this.type = type;
 		this.cells = cells;
 		columns = newBlockArray(type.size);
 		rows = newBlockArray(type.size);
 		sections = newBlockArray(type.size);
 		cheatSheet = new IdentityHashMap<Cell, Board.Coordinate>(type.size * type.size);
 	}
 
 	private void fillBlocks(BoardType type) {
 		int width = type.baseDimension;
 		for (int x = 0; x < type.size; ++x) {
 			for (int y = 0; y < type.size; ++y) {
 				int sectionIdx = (y/width) + (width * (x/width));
 				CellBlock
 					row = rows[x],
 					column = columns[y],
 					section = sections[sectionIdx];
 				Cell cell = cells[x][y];
 				row.add(cell);
 				column.add(cell);
 				section.add(cell);
 				cheatSheet.put(cell, new Coordinate(x, y, sectionIdx));
 			}
 		}
 	}
 	private void setupListeners() {
 		for (CellBlock row : rows)
 			for (Cell cell : row)
 				cell.addListener(row);
 		
 		for (CellBlock column : columns)
 			for (Cell cell : column)
 				cell.addListener(column);
 		
 		for (CellBlock section : sections) {
 			for (Cell cell : section) {
 				cell.addListener(section);
 				cell.addListener(this);
 			}
 		}
 	}
 	
 	private static Cell[][] newCellArray(int size) {
 		Cell[][] cells = new Cell[size][size];
 		for (int i = 0; i < size; ++i) {
 			for (int j = 0; j < size; ++j) {
 				cells[i][j] = new Cell(size);
 			}
 		}
 		return cells;
 	}
 	
 	private static CellBlock[] newBlockArray(int size) {
 		int i = size;
 		CellBlock[] result;
 		for (result = new CellBlock[size]; i > 0; result[--i] = new CellBlock(size));
 		return result;
 	}
 	
 	public boolean isSolved() {
 		return solved >= type.size * type.size;
 	}
 	
 	public boolean equals(Object o) {
 		if (o instanceof Board) {
 			Board other = (Board) o;
 			if (other.type != type)
 				return false;
 			
 			int size = type.size;
 			for (int i = 0; i < size; ++i)
 				for (int j = 0; j < size; ++j)
 					if (!cells[i][j].getPossibilities().equals(other.cells[i][j].getPossibilities()))
 						return false;
 			
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	public int hashCode() {
 		int hash = type.ordinal();
 		int size = type.size;
 		hash ^= size;
 		for (int i = 0; i < size; ++i)
 			for (int j = 0; j < size; ++j)
 				hash += (7*i + 2*j) * cells[i][j].getPossibilities().hashCode();
 		
 		return hash;
 	}
 	
 	public void cellChanged(Cell changedCell) {
 		Coordinate location = cheatSheet.get(changedCell);
 		CellBlock section = sections[location.section];
 		for (int value = 0; value < type.size; ++value) {
 			if (section.isSolved(value))
 				continue;
 			
 			Collection<CellBlock> sharedBlocks = new HashSet<CellBlock>(3);
 			for (Cell cell : section.cells) {
 				if (cell.couldBe(value)) {
 					Coordinate loc = cheatSheet.get(cell);
 					Collection<CellBlock> blocks = Arrays.asList(rows[loc.x], columns[loc.y], sections[loc.section]);
 					if (sharedBlocks.isEmpty())
 						sharedBlocks.addAll(blocks);
 					else
 						sharedBlocks.retainAll(blocks);
 				}
 			}
 			
 			sharedBlocks.remove(this);
 			if (!sharedBlocks.isEmpty()) {
 				for (CellBlock block : sharedBlocks) {
 					for (Cell cell : block) {
 						if (!section.cells.contains(cell)) {
 							cell.eliminate(value);
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	public void save(OutputStream out) throws IOException {
 		out.write(type.ordinal());
 		int max = type.size;
 		for (int i = 0; i < max; i++) {
 			for (int j = 0; j < max; j++) {
 				int value = cells[i][j].getValue();
 				if (value != -1) {
 					out.write(i);
 					out.write(j);
 					out.write(value);
 				}
 			}
 		}
 	}
 	
 	public static Board load(InputStream in) throws IOException {
 		Board board = new Board(BoardType.values()[in.read()]);
 		
 		int row;
 		while ((row = in.read()) != -1) {
 			int column = in.read(), value = in.read();
 			board.cells[row][column].solve(value);
 		}
 		
 		return board;
 	}
 
 	@Override
 	public void solved(Cell cell) {
 		++solved;
 		log.info("Solved " + solved + " / " + type.size * type.size);
 	}
 	
 	class GuessAndCheck implements Callable<Set<Board>> {
 		public Set<Board> call() {
 			Set<Board> branches = new HashSet<Board>();
 			
 			for (int x = 0, length = type.size; x < length; ++x) {
 				for (int y = 0; y < length; ++y) {
 					Cell cell = cells[x][y];
 					if (cell.isSolved())
 						continue;
 					
 					for (int value : cell.getPossibilities()) {
 						Board branch = new Board(Board.this);
 						try {
 							// Guess
 							branch.cells[x][y].solve(value);
 							branches.add(branch);
 						} catch (Exception e) {
 							// Check
 						}
 					}
 				}
 			}
 			
 			return branches;
 		}
 	}
 	
 }
