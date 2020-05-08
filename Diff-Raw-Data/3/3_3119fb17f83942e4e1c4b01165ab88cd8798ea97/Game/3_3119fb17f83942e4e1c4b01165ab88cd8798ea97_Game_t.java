 package com.example.puzzleprizes;
 
 import java.util.StringTokenizer;
 
 import com.example.puzzleprizes.ServerInterface;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Gravity;
 import android.widget.Toast;
 /* the actual game of Sudoku with letters substituted for the numbers.
  * Changes by Keith Gudger 2013, include:
  * a) pass merchant string from the list of coupon suppliers.
  * This comes from the web server.  
  * b) AlphaSub is the number to letter substitution.  This comes from the server.
  * c) ifFinished() reports to PuzzleView that the game is finished so it can
  * show the user the coupon.
  * d) added server code.  It is not a background task, it will hang the app
  * until finished.  Needs to be fixed.
  */
 public class Game extends Activity {
 	private static final String TAG = "Sudoku";
 	
 	public static final String KEY_DIFFICULTY = "com.example.puzzleprizes.difficulty";
 	public static final int DIFFICULTY_EASY = 0;
 	public static final int DIFFICULTY_MEDIUM =1;
 	public static final int DIFFICULTY_HARD = 2;
 	public static final int PUZZLE_SIZE = 81 ;
 	/* difficulty is passed in, will select puzzle from server string.
 	 * if server string does not include data for this difficulty level,
 	 * a default game supplied.  Currently "DIFFICULTY_HARD" is used to 
 	 * size the puzzle data arrays.  
 	 */
 	public static final String KEY_BUSINESS = "com.example.puzzleprizes.business";
 	/* The String business will contain the string needed for the server.
 	 * getPuzzleData(business,server_url) returns the puzzle data.
 	 */
 	private int puzzle[] = new int[9 * 9];
 	private String business ;
 	private final String[] alphaSub = new String[DIFFICULTY_HARD+1] ;
 	// alphaSub is the letter substitution
 	private int diff ;
 	private String puz[] = new String[DIFFICULTY_HARD+1];
 	private String highlights[] = new String[DIFFICULTY_HARD+1] ;
 	private boolean bHighLights[][] = new boolean[DIFFICULTY_HARD+1][PUZZLE_SIZE] ;
 	/* highlights is the string defining which squares are highlighted.
 	 * bHighLights is the array of booleans for PuzzleView.
 	 */
 	private PuzzleView puzzleView;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Log.d(TAG, "onCreate");
 		
 		diff = getIntent().getIntExtra(KEY_DIFFICULTY, DIFFICULTY_EASY);
 		business = getIntent().getStringExtra(KEY_BUSINESS);
 		// this next part will wait indefinitely for the data from the server.
 		getServerData(ServerInterface.getPuzzleData(business, getString(R.string.server_url)));
 		
 		String mystring = getResources().getString(R.string.game_title);
 		setTitle(mystring + " Key " + getAlphaSub());
 		
 		puzzle = getPuzzle(diff);
		calculateUsedTiles();
 		puzzleView = new PuzzleView(this);
 		setContentView(puzzleView);
 		puzzleView.requestFocus();
 	}
 	
 	protected void showKeypadOrError(int x, int y) {
 		int tiles[] = getUsedTiles(x, y);
 		if (tiles.length == 9) {
 			Toast toast = Toast.makeText(this, R.string.no_moves_label, Toast.LENGTH_SHORT);
 			toast.setGravity(Gravity.CENTER, 0, 0);
 			toast.show();
 		} else {
 			Log.d(TAG, "showKeypad: used=" + toPuzzleString(tiles));
 			Dialog v = new Keypad(this, tiles, puzzleView);
 			v.show();
 		}
 	}
 
 	protected boolean setTileIfValid(int x, int y, int value) {
 		int tiles[] = getUsedTiles(x, y);
 		if (value != 0) {
 			for (int tile : tiles) {
 				if (tile == value)
 					return false;
 			}
 		}
 		setTile(x, y, value);
 		calculateUsedTiles();
 		return true;
 	}
 
 	private final int used[][][] = new int[9][9][];
 
 	protected int[] getUsedTiles(int x, int y) {
 		return used[x][y];
 	}
 /* Tells PuzzleView that the game is finished, it then shows coupon.
  * @return boolean if it's finished, true.	
  */
 	protected boolean isFinished() {
 		for (int x : puzzle )
 				if ( x == 0 )
 					return false ;
 		return true ;
 	}
 
 	private void calculateUsedTiles() {
 		for (int x = 0; x < 9; x++) {
 			for (int y = 0; y < 9; y++) {
 				used[x][y] = calculateUsedTiles(x, y);
 				// Log.d(TAG, "used[" + x + "][" + y + "] = "
 				// + toPuzzleString(used[x][y]));
 			}
 		}
 	}
 
 	private int[] calculateUsedTiles(int x, int y) {
 		int c[] = new int[9];
 		// horizontal
 		for (int i = 0; i < 9; i++) {
 			if (i == y)
 				continue;
 			int t = getTile(x, i);
 			if (t != 0)
 				c[t - 1] = t;
 		}
 		// vertical
 		for (int i = 0; i < 9; i++) {
 			if (i == x)
 				continue;
 			int t = getTile(i, y);
 			if (t != 0)
 				c[t - 1] = t;
 		}
 		// same cell block
 		int startx = (x / 3) * 3;
 		int starty = (y / 3) * 3;
 		for (int i = startx; i < startx + 3; i++) {
 			for (int j = starty; j < starty + 3; j++) {
 				if (i == x && j == y)
 					continue;
 				int t = getTile(i, j);
 				if (t != 0)
 					c[t - 1] = t;
 			}
 		}
 		// compress
 		int nused = 0;
 		for (int t : c) {
 			if (t != 0)
 				nused++;
 		}
 		int c1[] = new int[nused];
 		nused = 0;
 		for (int t : c) {
 			if (t != 0)
 				c1[nused++] = t;
 		}
 		return c1;
 	}
 
 //	private final String easyPuzzle = "360000000004230800000004200" + "070460003820000014500013020" + "001900000007048300000000045" ;
 //	private final String easyPuzzle = "075100304009805000000907008" + "204300600090000080507061009" + "050602040000509800008013720" ;
 //	private final String easyPuzzle = "075126394149835276326947518" + "214398657693704182587261439" + "751682943432579861968413720" ;
 //	private final String easyPuzzle = "875126394149835276326947518" + "214398657693754182587261439" + "751682943432579861968413720" ;
 //	private final String mediumPuzzle = "650000070000506000014000005" + "007009000002314700000700800" + "500000630000201000030000097" ;
 //	private final String mediumPuzzle = "075100304009805000000907008" + "204300600090000080507061009" + "050602040000509800008013720" ;
 	private final String hardPuzzle = "009000000080605020501078000" + "000000700706040102004000000" + "000720903090301080000000600" ;
 	private final String highlightnull = "000000000000000000000000000000000000000000000000000000000000000000000000000000000" ;
 
 	/* getPuzzle 
 	 * @return an integer array from the puzzle string based on
 	 * @param diff the difficulty level.
 	 */
 	private int[] getPuzzle(int diff) {
 		// TODO: Continue last game
 		return fromPuzzleString(puz[diff]);
 	}
 	/* get HighLights
 	 * @return the boolean array
 	 */
 	public boolean[] getHighlights()
 	{
 		return bHighLights[diff];
 	}
 
 	static private String toPuzzleString(int[] puz) {
 		StringBuilder buf = new StringBuilder();
 		for (int element : puz) {
 			buf.append(element);
 		}
 		return buf.toString();
 	}
 
 	static protected int[] fromPuzzleString(String string) {
 		int[] puzl = new int[string.length()];
 		for (int i = 0; i < puzl.length; i++) {
 			puzl[i] = string.charAt(i) - '0';
 		}
 		return puzl;
 	}
 
 	private int getTile(int x, int y) {
 		return puzzle[y * 9 + x];
 	}
 
 	private void setTile(int x, int y, int value) {
 		puzzle[y * 9 + x] = value;
 	}
 // @return alphabetic character associated with position x.	
 	protected String getAlphaSub(int x) {
 		return alphaSub[diff].substring(x,x+1);
 }
 /* @return entire alphaSub string. */
 	protected String getAlphaSub() {
 		return alphaSub[diff] ;
 	}
 
 	protected String getTileString(int x, int y) {
 		int v = getTile(x, y);
 		if (v == 0)
 			return "";
 		else
 //			return String.valueOf(v);
 //			return alphaSub.substring(v-1,v);
 			return getAlphaSub(v-1);
 	}
 	
 	/* getServerData uses data from ServerInterface to 
 	 * @return void with
 	 * @param result is passed from ServerInterface data
 	 */
 	protected void getServerData(String result) {
         // this is used to hold the string array, after tokenizing
         String[] responseList;
 
         // we'll use a string tokenizer, with ";" (semicolon) as the delimiter
         StringTokenizer tk1 = new StringTokenizer(result, ";");
 
         // now we know how long the string array is
         responseList = new String[tk1.countTokens()];
         int j = 0 ;
         while (tk1.hasMoreTokens()) {
         	responseList[j++] = tk1.nextToken();
         } // responseList[j] has whole puzzle info
           // id, alphaSub, puzzle data, highlight data
         if ( (j > diff+1) && (responseList[diff].length() > 0) ) { // make sure it exists
             // we'll use a string tokenizer, with "," (comma) as the delimiter
         	StringTokenizer tk2 = new StringTokenizer(responseList[diff], ",");
         	String puzzList[] ;
         	puzzList = new String[tk2.countTokens()];
         	int k = 0 ;
         	while (tk2.hasMoreTokens()) {
         		puzzList[k++] = tk2.nextToken();
         	}
         	if ( k >= 3 ) { // make sure we have everything
         		alphaSub[diff] = (puzzList[1].length() > 0) ? puzzList[1] : "ABCDEFGHJ" ;
         		puz[diff] = (puzzList[2].length() > 0) ? puzzList[2] : hardPuzzle ;
         		highlights[diff] = (puzzList[3].length() > 0) ? puzzList[3] : highlightnull ; 
             } else {
             	alphaSub[diff] = "ABCDEFGHJ" ;
             	puz[diff] = hardPuzzle;
             	highlights[diff] = highlightnull ;
             }
         } else {
         	alphaSub[diff] = "ABCDEFGHJ" ;
         	puz[diff] = hardPuzzle ;
         	highlights[diff] = highlightnull ;
         }
         // change highlights string into a boolean array for PuzzleView
         for ( int m = 0 ; m < PUZZLE_SIZE ; m++ ) {
         	bHighLights[diff][m] = ((highlights[diff]).charAt(m) == '1') ? true : false ; 
         }
 		
 		
 	}
 }
