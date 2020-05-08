 package maze_objects;
 import java.util.Random;
 import java.util.Stack;
 import java.util.Vector;
 
 import general_utilities.MazeInput;
 
 public class Maze {
 
 	/**
 	 * @param args
 	 */
 	private final int DEFAULT_ROW_SIZE = 10;
 	private final int DEFAULT_COLUMN_SIZE = 10;
 
 	public static final int OPEN = 1;
 	public static final int CLOSED = 0;
 
 	private int game_state = 0;
 
 	private int rows;
 	private int columns;
 
 	private int exit_state;
 
 	private Hero hero;
 	private Dragon dragon;
 
 	char[][] positions = new char[DEFAULT_ROW_SIZE][DEFAULT_COLUMN_SIZE];
 
 	//Constructors
 	Maze() {
 		rows = DEFAULT_ROW_SIZE;
 		columns = DEFAULT_COLUMN_SIZE;
 		exit_state = CLOSED;
 		//startPredefinedMaze();
 		positions = generateMaze(rows, columns);
 		spawnHero();
 		spawnSword();
 		spawnDragon();
 	}
 
 	public static void main(String[] args) {
 		Maze m1 = new Maze();
 		m1.printMaze();
 		m1.play();
 	}
 
 	//General Methods
 	int getDragonState() {
 		return dragon.getState();
 	}
 
 	int getExitState() {
 		return exit_state;
 	}
 
 	//Game initializers
 	private void spawnHero() {
 		Random random = new Random();
 		int hero_row = 0;
 		int hero_column = 0;
 
 		do {
 			hero_row = random.nextInt(rows);
 			hero_column = random.nextInt(columns);
 		} while (positions[hero_row][hero_column] != MazeSymbol.empty);
 
 		hero = new Hero(hero_row, hero_column);
 		positions[hero.getRow()][hero.getColumn()] = MazeSymbol.hero;
 	}
 
 	private void spawnSword() {
 		Random random = new Random();
 		int sword_row = 0;
 		int sword_column = 0;
 
 		do {
 			sword_row = random.nextInt(rows);
 			sword_column = random.nextInt(columns);
 		} while (positions[sword_row][sword_column] != MazeSymbol.empty);
 
 		positions[sword_row][sword_column] = MazeSymbol.sword;
 	}
 
 	private void spawnDragon() {
 		Random random = new Random();
 		int dragon_row = 0;
 		int dragon_column = 0;
 
 		do {
 			dragon_row = random.nextInt(rows);
 			dragon_column = random.nextInt(columns);
 		} while (positions[dragon_row][dragon_column] != MazeSymbol.empty || nextToHero(dragon_row, dragon_column));
 
 		dragon = new Dragon(dragon_row, dragon_column);
 		positions[dragon_row][dragon_column] = MazeSymbol.dragon;
 	}
 
 	public boolean checkIfWall(int row, int column) {
 		if(positions[row][column] == MazeSymbol.wall)
 			return true;
 		else
 			return false;
 	}
 
 	public boolean checkIfExit(int row, int column) {
 		if(positions[row][column] == MazeSymbol.exit)
 			return true;
 		else
 			return false;
 	}
 
 	public boolean nextToDragon() { //True if the hero is adjacent to the dragon (horizontally or vertically), false if not
 		return(((dragon.getRow() == hero.getRow() + 1 && dragon.getColumn() == hero.getColumn()) ||
 				(dragon.getRow() == hero.getRow() - 1  && dragon.getColumn() == hero.getColumn()) ||
 				(dragon.getColumn() == hero.getColumn() + 1 && dragon.getRow() == hero.getRow()) ||
				(dragon.getColumn() == hero.getColumn() - 1 && dragon.getRow() == hero.getRow()) ||
				(dragon.getColumn() == hero.getColumn() && dragon.getRow() == hero.getRow()))
				&& (dragon.getState() == Dragon.ALIVE));
 	}
 
 	public boolean nextToHero(int row, int column) {
 		return((row == hero.getRow() + 1 && column == hero.getColumn()) ||
 				(row == hero.getRow() - 1  && column == hero.getColumn()) ||
 				(column == hero.getColumn() + 1 && row == hero.getRow()) ||
 				(column == hero.getColumn() - 1 && row == hero.getRow()));
 	}
 
 	public boolean fightDragon() { //True if the hero killed the dragon (was carrying sword), false if the hero died
 		if(hero.getState() == Hero.ARMED) {
 			dragon.setState(Dragon.DEAD);
 			positions[dragon.getRow()][dragon.getColumn()] = MazeSymbol.empty;
 			exit_state = OPEN;
 			return true;
 		}
 		else if(hero.getState() == Hero.IN_GAME) {
 			hero.setState(Hero.DEAD);
 			return false;
 		}
 
 		return false;
 	}
 
 
 	public void startPredefinedMaze(){
 		for (int row = 0; row < DEFAULT_ROW_SIZE; row++) {
 			for (int column = 0; column < DEFAULT_COLUMN_SIZE; column++) {
 				if ( row == 0 || row == DEFAULT_ROW_SIZE-1 || column == 0 || column == DEFAULT_COLUMN_SIZE-1){
 					positions[row][column] = MazeSymbol.wall;
 				}
 				else if((row == 2 || row == 3 || row == 4 || row == 6 || row == 7 || row == 8) &&
 						(column == 2 || column == 3 || column == 5 || column == 7)){
 					positions[row][column] = MazeSymbol.wall;
 				}
 				else {
 					positions[row][column] = MazeSymbol.empty;
 				}
 			}
 		}
 
 		positions[1][2] = MazeSymbol.empty;
 		positions[1][3] = MazeSymbol.empty;
 		positions[1][5] = MazeSymbol.empty;
 		positions[1][7] = MazeSymbol.empty;
 		positions[5][2] = MazeSymbol.empty;
 		positions[5][3] = MazeSymbol.empty;
 		positions[5][5] = MazeSymbol.empty;
 		positions[5][7] = MazeSymbol.wall;
 		positions[8][5] = MazeSymbol.empty;
 		positions[8][7] = MazeSymbol.empty;
 		positions[5][9] = MazeSymbol.exit;
 	}
 	
 	static public int checkWalls(int i, int j, char[][] map){
 		int numberWalls = 0;
 		if( (i-1) >= 0 && map[i-1][j] == MazeSymbol.wall)
 			numberWalls++;
 		if( (i+1) < map.length && map[i+1][j] == MazeSymbol.wall)
 			numberWalls++;
 		if( (j+1) < map[0].length && map[i][j+1] == MazeSymbol.wall)
 			numberWalls++;
 		if( (j-1) >= 0 && map[i][j-1] == MazeSymbol.wall)
 			numberWalls++;
 		return numberWalls;
 	}
 	
 	public char[][] generateMaze(int rows, int cols){	
 		// fill the maze with walls
 		char[][] maze = new char [rows][cols];
 		for (int i = 0; i < rows; i++) {
 			for (int j = 0; j < cols; j++) {
 				maze[i][j] = MazeSymbol.wall;
 			}
 		}
 		
 		// create a CellStack (LIFO) to hold a list of cell locations
 		Stack<Cell> cellStack = new Stack<Cell>();
 		
 		//choose random starting odd cell, call it currentCell
 		Cell currentCell = new Cell();
 		Random r = new Random();
 		currentCell.i = r.nextInt(cols-2)+1; // 1..cols-2, can't be a wall
 		currentCell.j = r.nextInt(rows-2)+1;
 		
 		maze[currentCell.i][currentCell.j] = MazeSymbol.empty;
 		Vector<Cell> nearbyCells = new Vector<Cell>();
 		
 		while(true){
 			// find all 4 neighbors of CurrentCell with all walls intact 
 			nearbyCells.clear();
 			if(currentCell.i-1 > 0 && currentCell.i-1 < rows && maze[currentCell.i-1][currentCell.j] == MazeSymbol.wall ){
 				if (checkWalls(currentCell.i-1, currentCell.j, maze) == 3 ){
 					nearbyCells.add(new Cell(currentCell.i-1, currentCell.j));
 				}
 			}
 			if(currentCell.j-1 > 0 && currentCell.j-1 < cols && maze[currentCell.i][currentCell.j-1] == MazeSymbol.wall ){
 				if (checkWalls(currentCell.i, currentCell.j-1, maze) == 3 ){
 					nearbyCells.add(new Cell(currentCell.i, currentCell.j-1));
 				}
 			}
 			if(currentCell.i+1 > 0 && currentCell.i+1 < rows && maze[currentCell.i+1][currentCell.j] == MazeSymbol.wall ){
 				if (checkWalls(currentCell.i+1, currentCell.j, maze) == 3 ){
 					nearbyCells.add(new Cell(currentCell.i+1, currentCell.j));
 				}
 			}
 			if(currentCell.j+1 > 0 && currentCell.j+1 < cols && maze[currentCell.i][currentCell.j+1] == MazeSymbol.wall ){
 				if (checkWalls(currentCell.i, currentCell.j+1, maze) == 3 ){
 					nearbyCells.add(new Cell(currentCell.i, currentCell.j+1));
 				}
 			}
 			
 			//if one or more found 
 			if (nearbyCells.size() > 0){
 				// choose one at random  
 				int selected = r.nextInt(nearbyCells.size());
 				Cell next = nearbyCells.elementAt(selected);
 				maze[next.i][next.j] = MazeSymbol.empty;
 				// push CurrentCell location on the CellStack
 				cellStack.push(next);
 				// make the new cell CurrentCell 
 				currentCell = next;
 			} else {
 				if (cellStack.empty()){
 					break;
 				}
 				currentCell = cellStack.pop();
 			}
 
 		}
 
 		// generate an exit
 		boolean done = false;
 		Cell exitCell = new Cell();
 		while (!done){
 			int side = r.nextInt(4);
 			switch (side) {
 			case 0: // left side exit
 				exitCell.j = 0;
 				exitCell.i = r.nextInt(rows-2)+1;
 				if(maze[exitCell.i][exitCell.j+1] != MazeSymbol.wall){
 					done = true;
 				}
 				break;
 			case 1: // right side exit
 				exitCell.j = cols-1;
 				exitCell.i = r.nextInt(rows-2)+1;
 				if(maze[exitCell.i][exitCell.j-1] != MazeSymbol.wall){
 					done = true;
 				}
 				break;
 			case 2: // top side exit
 				exitCell.i = 0;
 				exitCell.j = r.nextInt(cols-2)+1;
 				if(maze[exitCell.i+1][exitCell.j] != MazeSymbol.wall){
 					done = true;
 				}
 				break;
 			case 3: // down side exit
 				exitCell.i = rows-1;
 				exitCell.j = r.nextInt(rows-2)+1;
 				if(maze[exitCell.i-1][exitCell.j] != MazeSymbol.wall){
 					done = true;
 				}
 				break;
 
 			default:
 				break;
 			}
 		}
 		maze[exitCell.i][exitCell.j] = MazeSymbol.exit;
 
 		return maze;
 	}
 
 	//Game methods
 	public void printMaze() {
 		for(int i = 0; i < 100; i++)
 			System.out.println();
 		for (int x = 0; x < rows; x++) {
 			for (int y = 0; y < columns; y++) {
 				System.out.print(positions[x][y]);
 				System.out.print(MazeSymbol.space);
 			}
 			System.out.print('\n');
 		}
 	}
 
 	public boolean play() {
 		boolean goOn = true;
 
 		char input;
 
 		while(goOn) {
 
 			if(game_state == 1 && (dragon.getState() == Dragon.ALIVE)) {
 				dragon.moveDragon(this);
 
 				printMaze();
 
 				if (nextToDragon()) {
 					if(fightDragon())
 						System.out.println("WOW! You slayed the dragon! Exit is now opened!\n");
					else {
 						goOn = false;
						break;
					}
 				}
 			}
 			else
 				game_state = 1;
 
 			try {
 				System.out.print("Move your hero (WASD, only first input will be considered): ");
 				input = MazeInput.getChar();
 				if(input == 's')
 					goOn = hero.moveHero(hero.getRow() + 1, hero.getColumn(), this);
 				else if(input == 'w')
 					goOn = hero.moveHero(hero.getRow() - 1, hero.getColumn(), this);
 				else if(input == 'a')
 					goOn = hero.moveHero(hero.getRow(), hero.getColumn() - 1, this);
 				else if(input == 'd')
 					goOn = hero.moveHero(hero.getRow(), hero.getColumn() + 1, this);
 				else if(input == 'z')
 					goOn = false;
 			}
 			catch(Exception e) {
 				System.err.println("Problem reading user input!");
 			}
 
 			printMaze();
 
 			if (nextToDragon()) {
 				if(fightDragon())
 					System.out.println("WOW! You slayed the dragon! Exit is now opened!\n");
 				else 
 					goOn = false;
 			}
 		}
 
 		switch(hero.getState()) {
 		case Hero.EXITED_MAZE:
 			System.out.println("Congratulations, you exited the maze!");
 			break;
 		case Hero.DEAD:
 			System.out.println("Oh no, the dragon killed you!");
 			break;
 		}
 
 		return true;
 	}
 
 
 }
 
 class Cell {
 	public int i;
 	public int j;
 	
 	public Cell(){
 		i = 0;
 		j = 0;
 	}
 	
 	public Cell(int row, int col){
 		i = row;
 		j = col;
 	}
 }
