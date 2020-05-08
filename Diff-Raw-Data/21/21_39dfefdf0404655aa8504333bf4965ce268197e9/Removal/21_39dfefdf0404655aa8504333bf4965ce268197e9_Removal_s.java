 import java.util.ArrayList;
 import java.util.Random;
 
 
 public class Removal {
 	
 	public Removal(Board b){
 		this.b = b;
		if(b.getDifficulty() == b.difficultyValueEasy()){
			difficulty = EASY;
		} else if (b.getDifficulty() == b.difficultyValueMedium()){
			difficulty = MEDIUM;
		} else {
			difficulty = HARD;
		}
 		checker = new SudokuSolver();
 		removable = new ArrayList<Point>();
 		initialise();
 	}
 	
 	public void remove(){
 		simpleRemove();
 		//checker.isSudokuSolutionUnique(b);
		if (difficulty > 1){
 			//complexRemove();
 		}
 	}
 	
 	private void simpleRemove(){
 		Random r = new Random();
 		int toRemove;
 		int x, y;
 		int z;
 		int count = 0;
 		boolean can;
		if(difficulty <= 1){
 			toRemove = 20;
		} else if (difficulty <= 2){
 			toRemove = 30;
 		} else {
 			toRemove = 80;
 		}
 		while(toRemove > 0){
 			x = r.nextInt(9);
 			y = r.nextInt(9);
 			z = b.getCellValue(x+1, y+1);
 			can = true;
 			if(b.isInitiallyVisibleCell(x+1, y+1) == false){
 				continue;
 			}
 			//TODO the randomness and trial and error nature of this seems inefficient
 			if (count > 1000){
 				break;
 			}
 			for(int i = 0; i < 9; i++){
 				if(i != z){
 					if (!(b.rowHas(x+1, i) || b.columnHas(y+1, i) || b.squareHas(x+1,y))){
 						can = false;
 						count++;
 						break;
 					}
 				}
 			}
 			if(can){
 				b.setCellVisibility(x+1, y+1, false);
 				toRemove--;
 			}
 		}
 	}
 	
 	private void complexRemove(){
		int complexity = difficulty*DIFF_CALIBRATION;
 		int i = 0;
 		int x, y, z;
 		int diff;
 		Random r = new Random();
 		while (i < complexity){
 			x = r.nextInt(9);
 			y = r.nextInt(9);
 			b.setCellVisibility(x+1, y+1, false);
 			//diff = checker.isSudokuSolutionUnique(b);
 			//if(diff < difficulty){
 			if(checker.isSudokuSolutionUnique(b)){
 				i += 1;//diff;
 			} else {
 				b.setCellVisibility(x+1, y+1, false);
 			}
 		}
 	}
 	
 	private void initialise(){
 		int row, col;
 		for(row = 0; row < 9; row++){
 			for(col = 0; col < 9; col++){
 				removable.add(new Point(row, col));
 			}
 		}
 	}
 	
 	private Board b;
 	private int difficulty;
 	SudokuSolver checker;
 	ArrayList<Point> removable;
 	private static final int EASY = 1;
 	private static final int MEDIUM = 2;
 	private static final int HARD = 3;
 	private static final int MISSING = -1;
 	private static final int DIFF_CALIBRATION = 10;
 }
