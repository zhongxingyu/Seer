 package matt.and.jessica;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.badlogic.gdx.Game;
 
 public class TileFlipGame  extends Game {
 	
 	private final int width;
 	private final int height;
 
 	public TileFlipGame(int width, int height){
 		this.width = width;
 		this.height = height;
 	}
 
 	@Override
 	public void create() {
 		List<Puzzle> puzzles = new ArrayList<Puzzle>();
 //		puzzles.add(triforcePuzzle());
 		puzzles.add(squarePuzzle());
 		puzzles.add(xPuzzle());
 		puzzles.add(iPuzzle());
 		puzzles.add(golfPuzzle());
 		puzzles.add(gemPuzzle());
 		puzzles.add(ironPuzzle());
 		puzzles.add(onePuzzle());
 		
 		setScreen(new GameScreen(puzzles,width,height));
 		
 	}
 
 	private Puzzle squarePuzzle() {
 		int size = 4;
 		Puzzle puzzle = new Puzzle();
 		puzzle.clue = "Don't be a ____";
 		puzzle.initialState = new Grid(size,size);
 		puzzle.initialState.turnOnTile(0, 0);
 		puzzle.initialState.turnOnTile(3, 3);
 		puzzle.initialState.turnOnTile(1, 2);
 		puzzle.initialState.turnOnTile(2, 1);
 		puzzle.solvedState = new Grid(size,size);
 		puzzle.solvedState.turnOnTile(1, 1);
 		puzzle.solvedState.turnOnTile(1, 2);
 		puzzle.solvedState.turnOnTile(2, 1);
 		puzzle.solvedState.turnOnTile(2, 2);
 		
 		return puzzle;
 	}
 
 	private Puzzle golfPuzzle() {
 		int size = 4;
 		Puzzle puzzle = new Puzzle();
 		puzzle.clue = "Fore!";
 		puzzle.initialState = new Grid(size,size);
 		puzzle.initialState.turnOnTile(0, 0);
 		puzzle.initialState.turnOnTile(1, 1);
 		puzzle.initialState.turnOnTile(2, 2);
 		puzzle.initialState.turnOnTile(1, 2);
 		puzzle.initialState.turnOnTile(2, 1);
 		puzzle.solvedState = new Grid(size,size);
 		puzzle.solvedState.turnOnTile(1, 0);
 		puzzle.solvedState.turnOnTile(2, 0);
 		puzzle.solvedState.turnOnTile(2, 1);
 		puzzle.solvedState.turnOnTile(2, 2);
 		puzzle.solvedState.turnOnTile(2, 3);
 		return puzzle;
 	}
 	
 	private Puzzle iPuzzle() {
 		int size = 5;
 		Puzzle puzzle = new Puzzle();
 		
 		puzzle.clue = "Me, myself";
 		
 		puzzle.initialState = new Grid(size,size);
 		puzzle.initialState.turnOnTile(0, 4);
 		puzzle.initialState.turnOnTile(1, 4);
 		puzzle.initialState.turnOnTile(2, 3);
 		puzzle.initialState.turnOnTile(3, 1);
 		puzzle.initialState.turnOnTile(4, 0);
 		
 		puzzle.solvedState = new Grid(size,size);
 		puzzle.solvedState.turnOnTile(2, 0);
 		puzzle.solvedState.turnOnTile(2, 1);
 		puzzle.solvedState.turnOnTile(2, 2);
 		puzzle.solvedState.turnOnTile(2, 4);
 		
 		puzzle.outlineSolution = true;
 		return puzzle;
 	}
 	
 	private Puzzle ironPuzzle() {
 		int size = 5;
 		Puzzle puzzle = new Puzzle();
 		
 		puzzle.clue = "Iron";
 		
 		puzzle.initialState = new Grid(size,size);
 		puzzle.initialState.turnOnTile(1, 0);
 		puzzle.initialState.turnOnTile(1, 2);
 		puzzle.initialState.turnOnTile(1, 4);
 		
 		puzzle.initialState.turnOnTile(0, 1);
 		puzzle.initialState.turnOnTile(0, 3);
 		
 		puzzle.initialState.turnOnTile(3, 0);
 		puzzle.initialState.turnOnTile(3, 2);
 		puzzle.initialState.turnOnTile(3, 4);
 		
 		puzzle.initialState.turnOnTile(2, 1);
 		puzzle.initialState.turnOnTile(2, 3);
 		
 		puzzle.initialState.turnOnTile(4, 0);
 		puzzle.initialState.turnOnTile(4, 1);
 		puzzle.initialState.turnOnTile(4, 2);
 		puzzle.initialState.turnOnTile(4, 3);
 		puzzle.initialState.turnOnTile(4, 4);
 		
 		puzzle.solvedState = new Grid(size,size);
 		puzzle.solvedState.turnOnTile(0, 0);
 		puzzle.solvedState.turnOnTile(0, 1);
 		puzzle.solvedState.turnOnTile(0, 2);
 		puzzle.solvedState.turnOnTile(0, 3);
 		puzzle.solvedState.turnOnTile(0, 4);
 		
 		puzzle.solvedState.turnOnTile(1, 2);
 		puzzle.solvedState.turnOnTile(1, 4);
 		
 		puzzle.solvedState.turnOnTile(3, 0);
 		puzzle.solvedState.turnOnTile(3, 1);
 		puzzle.solvedState.turnOnTile(3, 2);
 		puzzle.solvedState.turnOnTile(3, 3);
 		puzzle.solvedState.turnOnTile(3, 4);
 		
 		puzzle.solvedState.turnOnTile(4, 4);
 		puzzle.solvedState.turnOnTile(4, 2);
 		puzzle.solvedState.turnOnTile(4, 0);
 		
 		puzzle.outlineSolution = true;
 		return puzzle;
 	}
 	
 	private Puzzle gemPuzzle(){
 		int size = 5;
 		Puzzle puzzle = new Puzzle();
 		
 		puzzle.clue = "A Gem";
 		
 		puzzle.initialState = new Grid(size,size);
 		for(int x = 0; x < size; x++){
 			puzzle.initialState.turnOnTile(x, 0);
 			puzzle.initialState.turnOnTile(x, size - 1);
 		}
 		for(int y = 0; y < size; y++){
 			puzzle.initialState.turnOnTile(0, y);
 			puzzle.initialState.turnOnTile(size - 1, y);
 		}
 		
 		puzzle.solvedState = new Grid(size,size);
 		for(int y = 0; y < size; y++){
 			puzzle.solvedState.turnOnTile(2, y);
 			if(y > 0 && y < size-1){
 				puzzle.solvedState.turnOnTile(1, y);
 				puzzle.solvedState.turnOnTile(3, y);
 			}
 		}
 		
 		puzzle.outlineSolution = true;
 		return puzzle;
 	}
 	
 	private Puzzle onePuzzle(){
 		int height = 6;
 		int width = 5;
 		Puzzle puzzle = new Puzzle();
 		
		puzzle.clue = "You're Number _!";
 		
 		puzzle.initialState = new Grid(width,height);
 		for(int x = 0; x < width; x++){
 			puzzle.initialState.turnOnTile(x, 0);
 			puzzle.initialState.turnOnTile(x, height - 1);
 		}
 		for(int y = 0; y < height; y++){
 			puzzle.initialState.turnOnTile(0, y);
 			puzzle.initialState.turnOnTile(width - 1, y);
 		}
 		
 		puzzle.solvedState = new Grid(width,height);
 		for(int x = 0; x < width; x++){
 			puzzle.solvedState.turnOnTile(x, 0);
 		}
 		for(int y = 0; y < height; y++){
 			puzzle.solvedState.turnOnTile(2, y);
 		}
 		puzzle.solvedState.turnOnTile(0, 3);
 		puzzle.solvedState.turnOnTile(1, 4);
 		
 		puzzle.outlineSolution = true;
 		return puzzle;
 	}
 	
 	private Puzzle xPuzzle(){
 		int size = 5;
 		Puzzle puzzle = new Puzzle(size, size);
 		puzzle.clue = "Marks the Spot";
 		
 		puzzle.initialState.turnOnTile(2, 2);
 		
 		for(int i = 0; i < size; i++){
 			puzzle.solvedState.turnOnTile(i, i);
 			puzzle.solvedState.turnOnTile(i, size - 1 -i);
 		}
 		
 		return puzzle;
 	}
 	
 	private Puzzle triforcePuzzle() {
 		int width = 9;
 		int height = 5;
 		Puzzle puzzle = new Puzzle();
 		
 		puzzle.clue = "Triforce";
 		
 		puzzle.initialState = new Grid(width,height);
 		puzzle.initialState.turnOnTile(1, 0);
 		puzzle.initialState.turnOnTile(1, 2);
 		puzzle.initialState.turnOnTile(1, 4);
 		
 		puzzle.initialState.turnOnTile(0, 1);
 		puzzle.initialState.turnOnTile(0, 3);
 		
 		puzzle.initialState.turnOnTile(3, 0);
 		puzzle.initialState.turnOnTile(3, 2);
 		puzzle.initialState.turnOnTile(3, 4);
 		
 		puzzle.initialState.turnOnTile(2, 1);
 		puzzle.initialState.turnOnTile(2, 3);
 		
 		puzzle.initialState.turnOnTile(4, 0);
 		puzzle.initialState.turnOnTile(4, 1);
 		puzzle.initialState.turnOnTile(4, 2);
 		puzzle.initialState.turnOnTile(4, 3);
 		puzzle.initialState.turnOnTile(4, 4);
 		
 		puzzle.solvedState = new Grid(width,height);
 		for(int x = 0; x < width; x++){
 			puzzle.solvedState.turnOnTile(x, 0);
 		}
 		puzzle.solvedState.turnOnTile(1, 1);
 		puzzle.solvedState.turnOnTile(3, 1);
 		puzzle.solvedState.turnOnTile(5, 1);
 		puzzle.solvedState.turnOnTile(7, 1);
 		
 		for(int x = 2; x < width-2; x++){
 			puzzle.solvedState.turnOnTile(x, 2);
 		}
 		
 		puzzle.solvedState.turnOnTile(3, 3);
 		puzzle.solvedState.turnOnTile(4, 4);
 		puzzle.solvedState.turnOnTile(5, 3);
 		
 		puzzle.outlineSolution = true;
 		return puzzle;
 	}
 	
 }
