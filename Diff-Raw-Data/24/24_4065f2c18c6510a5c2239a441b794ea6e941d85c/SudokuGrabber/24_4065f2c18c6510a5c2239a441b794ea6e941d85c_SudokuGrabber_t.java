 package com.sudoku;
 
 import java.util.Random;
 
 import com.sudoku.db.SudokuDatabase;
 import com.sudoku.game.CellCollection;
 import com.sudoku.game.SudokuGame;
 import com.sudoku.gui.SudokuPlayActivity;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.Toast;
 
 public class SudokuGrabber extends Activity{
 	private static final String TAG = "Sudoku";
 	public static final String KEY_DIFFICULTY = "com.sudoku.difficulty";
 	public static final String KEY_PUZZLE = "com.sudoku.puzzle";
 	public static final int DIFFICULTY_EASY= 0;
 	public static final int DIFFICULTY_MEDIUM = 1;
 	public static final int DIFFICULTY_HARD = 2;
 	
 	private SudokuDatabase mDatabase;
     private SudokuGame mGame;
 	private int puzzle[] = null;
 	private int oPuzzle[] =null;
 	private int solution[] = null;
 	private SudokuSolver sv = new SudokuSolver();
 	private PuzzleView puzzleView;
 	private boolean valid[] = new boolean[81];
 	private boolean hints[] = new boolean[81];
 	private long sudokuId = 0;
 	
 	
 	private int getTile(int x, int y){
 		return puzzle[y*9 + x];
 	}
 	
 	
 	protected String getTileString(int x, int y){
 		int v = getTile(x,y);
 		if(v==0){
 			return "";
 		}else{
 			return String.valueOf(v);
 		}
 		
 	}
 	
 	protected boolean setTile(int x, int y, int value){
 		puzzle[y*9+x] = value;
 		validateALlTiles();
 		return isValid(x,y);
 	}
 	protected boolean isValid(int x, int y){
 		return valid[y*9+x];
 	}
 	
 	private boolean isValidPuzzle(int[] puzzle){
 		for(int x=0; x<9; x++){
 			for(int y=0; y<9; y++){
 				if(!validateTile(x,y)){
 					return false;
 				}
 			}
 		}
 		return true;
 	}
 	
 	private void validateALlTiles(){
 		for(int x = 0; x<9; x++){
 			for(int y=0; y<9;y++){
 				valid[y*9+x] = validateTile(x,y);
 			}
 		}
 	}
 	
 	private boolean validateTile(int x, int y){
 		int value = getTile(x,y);
 		if(value==0){
 			return true;
 		}
 		for(int i = 0; i<9; i++){
 			if(i == y){
 				continue;
 			}else{
 				int t = getTile(x,i);
 				if(t ==value){
 					return false;
 				}
 			}
 		}
 		for(int i = 0; i<9; i++){
 			if(i == x){
 				continue;
 			}else{
 				int t = getTile(i,y);
 				if(t==value){
 					return false;
 				}
 			}
 		}
 		
 		int startx = (x/3) *3;
 		int starty = (y/3) *3;
 		for(int i= startx; i<startx+3;i++){
 			for(int j= starty; j<starty+3; j++){
 				if(i == x && j == y){
 					continue;
 				}else{
 					int t = getTile(i,j);
 					if(t== value){
 						return false;
 					}
 				}
 			}
 			
 		}
 		return true;
 	}
 	
 	public void showKeypad(int selX, int selY) {
 		Dialog v = new Keypad(this,puzzleView);
 		v.show();		
 	}
 	@Override 
 	protected void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		Log.d(TAG,"OnCreate");
 		mDatabase = new SudokuDatabase(getApplicationContext());
 		oPuzzle = getIntent().getIntArrayExtra(KEY_PUZZLE);
		if(oPuzzle ==null){
			Toast toast = Toast.makeText(this, R.string.no_puzzle_detected, Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			puzzle = new int[81];
			for(int i=0; i<81;i++){
				puzzle[i] = 0;
			}
		}else{
			if(puzzle == null){
				puzzle = oPuzzle.clone();
			}
 		}
		
 		validateALlTiles();
 		puzzleView = new PuzzleView(this);
 		setContentView(puzzleView);
 		puzzleView.requestFocus();
 
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu){
 		super.onCreateOptionsMenu(menu);
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.sudoku_grabber, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item){
 		switch(item.getItemId()){
 		case R.id.show_solution:
 			if(isValidPuzzle(puzzle)){
 				sv = new SudokuSolver();
 				sv.setPuzzle(puzzle);
 				if(sv.solve()){
 					solution = sv.getSolution();
 					setHints();
 					puzzle =solution;
 					puzzleView = new PuzzleView(this);
 					setContentView(puzzleView);
 					puzzleView.requestFocus();
 				}
 			}else{
 				Toast toast = Toast.makeText(this, R.string.puzzle_invalid_label, Toast.LENGTH_SHORT);
 				toast.setGravity(Gravity.CENTER, 0, 0);
 				toast.show();
 			}
 			break;
 		case R.id.get_hint:
 			if(isValidPuzzle(puzzle)){
 				if(!isSolved(puzzle)){
 					getRandomHint();
 					puzzleView = new PuzzleView(this);
 					setContentView(puzzleView);
 					puzzleView.requestFocus();
 				} else{
 					Toast toast = Toast.makeText(this, R.string.puzzle_solved_label, Toast.LENGTH_SHORT);
 					toast.setGravity(Gravity.CENTER, 0, 0);
 					toast.show();
 				}
 			}else{
 				Toast toast = Toast.makeText(this, R.string.puzzle_invalid_label, Toast.LENGTH_SHORT);
 				toast.setGravity(Gravity.CENTER, 0, 0);
 				toast.show();
 			}
 			
 			break;
 		case R.id.save_puzzle:
 			savePuzzle();
 			break;
 		case R.id.clean_solution:
 			clean();
 			puzzle = oPuzzle.clone();
 			puzzleView = new PuzzleView(this);
 			setContentView(puzzleView);
 			puzzleView.requestFocus();
 			break;
 		case R.id.play_this_puzzle:
 			if(sudokuId==0){
 				if(!savePuzzle()||sudokuId==0){
 					break;
 				}
 				
 			}
 			Intent sudokuAct = new Intent(this,SudokuPlayActivity.class);
 			sudokuAct.putExtra(SudokuPlayActivity.EXTRA_SUDOKU_ID, sudokuId);
 			startActivity(sudokuAct);
 			break;
 		default:
 			break;
 		}
 		return true;
 	}
 	
 	@Override
 	protected void onSaveInstanceState(Bundle outState){
 		outState.putIntArray("puzzle", puzzle);
 		outState.putBooleanArray("hints", hints);
 		outState.putBooleanArray("valid", valid);
 		super.onSaveInstanceState(outState);
 	}
 	
 	@Override
 	public void onRestoreInstanceState(Bundle savedInstanceState){
 		puzzle = savedInstanceState.getIntArray("puzzle");
 		hints = savedInstanceState.getBooleanArray("hints");
 		valid = savedInstanceState.getBooleanArray("valid");
 		super.onRestoreInstanceState(savedInstanceState);
 	}
 	
 	private void getRandomHint(){
 		sv.setPuzzle(puzzle);
 		if(sv.solve()){
 			solution = sv.getSolution();
 			Random random = new Random();
 			while(true){
 				int x = random.nextInt(81);
 				if(puzzle[x]==0){
 					puzzle[x] = solution[x];
 					hints[x] = true;
 					break;
 				}
 			}
 		}
 
 	}
 	private boolean isSolved(int[] p) {
 		for(int i=0; i<81; i++){
 			if(p[i]==0){
 				return false;
 			}
 		}
 		return true;
 	}
 
 
 	private boolean savePuzzle(){
 		if(isValidPuzzle(puzzle)){
 			if(!isSolved(puzzle)){
 				CellCollection cc = CellCollection.fromString(puzzleToString());
 				mGame = new SudokuGame();
 				mGame.setCreated(System.currentTimeMillis());
 				mGame.setCells(cc);
 				sudokuId = mDatabase.insertSudoku(4, mGame);
 				Toast toast = Toast.makeText(this, R.string.puzzle_saved_label, Toast.LENGTH_SHORT);
 				toast.setGravity(Gravity.CENTER, 0, 0);
 				toast.show();
 				return true;
 			}else{
 				Toast toast = Toast.makeText(this, R.string.puzzle_solved_label, Toast.LENGTH_SHORT);
 				toast.setGravity(Gravity.CENTER, 0, 0);
 				toast.show();
 			}
 		}else{
 			Toast toast = Toast.makeText(this, R.string.puzzle_invalid_label, Toast.LENGTH_SHORT);
 			toast.setGravity(Gravity.CENTER, 0, 0);
 			toast.show();
 		}
 		return false;
 
 	}
 
 	private String puzzleToString(){
 		String puzzleS = new String();
 		for(int i=0; i<81; i++){
 			puzzleS = puzzleS.concat(String.valueOf(puzzle[i]));
 		}
 		return puzzleS;
 	}
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		mDatabase.close();
 	}
 	
 	private void clean(){
 		for(int i=0; i<81; i++){
 			hints[i] = false;
 		}
 		for(int i=0; i<81; i++){
 			valid[i] = true;
 		}
 	}
 	
 	protected boolean isHint(int x, int y){
 		return hints[y*9+x];
 	}
 	
 	private void setHints(){
 		for(int i=0; i<81; i++){
 			if(puzzle[i]==0){
 				hints[i] = true;
 			}else{
 				hints[i] = false;
 			}
 		}
 	}
 }
