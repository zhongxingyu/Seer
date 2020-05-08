 package com.mntnorv.wrdl_holo;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Menu;
import android.widget.TextView;
 
 import com.mntnorv.wrdl_holo.dict.Dictionary;
 import com.mntnorv.wrdl_holo.dict.LetterGrid;
import com.mntnorv.wrdl_holo.dict.WordChecker;
 import com.mntnorv.wrdl_holo.views.TileGridView;
 
 public class GameActivity extends Activity {
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         setContentView(R.layout.activity_game);
         
         allWords = new ArrayList<String>();
         guessedWords = new ArrayList<String>();
         
         final TileGridView grid = (TileGridView)findViewById(R.id.mainTileGrid);
        final TextView wordField = (TextView)findViewById(R.id.currentWordField);
         
         final String[] letters = {
         		"A", "B", "C", "D",
         		"E", "F", "G", "H",
         		"I", "J", "K", "L",
         		"M", "N", "O", "Qu"};
         
         grid.setLetters(letters);
         grid.setOnWordChangeListener(new TileGridView.OnWordChangeListener() {	
 			@Override
 			public void onWordChange(String word) {
 				WordChecker.Result res = wrdlHoloChecker.checkWord(word);
 				if (res.isGood()) {
 					if (!res.isGuessed()) {
 						word += " OK";
 						guessedWords.add(word);
 					} else {
 						word += " K";
 					}
 				} else if (res.isBad()) {
 					word += " X";
 				}
 				
 				wordField.setText(word.toUpperCase());
 			}
 		});
         
         Dictionary dict = null;
         
 		try {
 			dict = new Dictionary(getAssets().open("dict.hex"));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
         LetterGrid lGrid = new LetterGrid(letters, 4, 4);
         allWords.addAll(lGrid.getWordsInGrid(dict));
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_game, menu);
         return true;
     }
     
     // Word lists
     private ArrayList<String> allWords = null;
     private ArrayList<String> guessedWords = null;
     
     // Word checker implementation
     private WordChecker wrdlHoloChecker = new WordChecker() {
 		@Override
 		public Result checkWord(String pWord) {
 			if (pWord.length() > 0) {
 				if (Collections.binarySearch(allWords, pWord) >= 0) {
 					byte state = Result.GOOD;
 					
 					if (guessedWords.contains(pWord)) {
 						state |= Result.GUESSED;
 					}
 						
 					return new Result (state, this.getWordScore(pWord));
 				} else {
 					return new Result (Result.BAD, 0);
 				}
 			} else {
 				return new Result (Result.EMPTY, 0);
 			}
 		}
 
 		@Override
 		public int getWordScore(String pWord) {
 			return pWord.length()*5;
 		}
     };
 }
