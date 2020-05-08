 import java.util.ArrayList;
 import java.util.List;
 
 public class Cell {
 
 	private Labyrinth labyrinth;
 	private boolean northWall;
 	private boolean eastWall;
 	private int x;
 	private int y;
 	private int gold;
 	private List<Player> players = new ArrayList<Player>();
 
 	public Cell(Labyrinth labyrinth, boolean northWall, boolean eastWall, int gold, int x, int y) {
 		this.labyrinth = labyrinth;
 		this.northWall = northWall;
 		this.eastWall = eastWall;
 		this.gold = gold;
 		this.x = x;
 		this.y = y;
 	}
 
 	/* returns true iff there's a wall in the north
 	 */
 	public boolean hasNorthWall() {
 		return northWall;
 	}
 
 	/* returns true iff there's a wall in the east
 	 */
 	public boolean hasEastWall() {
 		return eastWall;
 	}
 
 	/* returns the current amount of gold >= 0 on this cell
 	 */
 	public int getGold() {
 		return gold;
 	}
 
 	/* returns the x coordinate of the cell 0<=x, from west
 	 * to east
 	 */
 	public int getX() {
 		return x;
 	}
 
 	/* returns the y coordinate of the cell 0<=y, from north
 	 * to south
 	 */
 	public int getY() {
 		return y;
 	}
 	
 	public synchronized List<Player> getPlayers() {
 		return new ArrayList<Player>(players);
 	}
 	
 	public synchronized void addPlayer(Player player) {
 		players.add(player);
 	}
 	
 	public synchronized void removePlayer(Player player) {
 		players.remove(player);
 	}
 
 	public Labyrinth getLabyrinth() {
 		return labyrinth;
 	}
 	
 	public List<Cell> adjacentCells(boolean respectWalls) {
 		List<Cell> cells = new ArrayList<Cell>();
 		
 		if (!respectWalls || !northWall) {
 			if (y + 1 < labyrinth.getHeight()) {
 				cells.add(labyrinth.getCell(x, y+1));
 			}
 		}
 		
 		if (!respectWalls || !eastWall) {
 			if (x + 1 < labyrinth.getWidth()) {
 				cells.add(labyrinth.getCell(x+1, y));
 			}
 		}
 		
 		if (y - 1 >= 0) {
 			Cell newCell = labyrinth.getCell(x, y-1);
			if (!respectWalls || !newCell.hasNorthWall()) {
 				cells.add(newCell);
 			}
 		}
 		
 		if (x - 1 >= 0) {
 			Cell newCell = labyrinth.getCell(x-1, y);
			if (!respectWalls || !newCell.hasEastWall()) {
 				cells.add(newCell);
 			}
 		}
 		
 		return cells;
 	}
 
 	public boolean isExit() {
 		return (x+1 == labyrinth.getWidth() && !eastWall)
 			|| (y+1 == labyrinth.getHeight() && !northWall);
 	}
 }
