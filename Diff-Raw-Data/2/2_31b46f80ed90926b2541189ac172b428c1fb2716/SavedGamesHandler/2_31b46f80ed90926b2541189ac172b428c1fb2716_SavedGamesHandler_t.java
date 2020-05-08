 package ch.unibe.scg.team3.localDatabase;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import ch.unibe.scg.team3.board.Board;
 import ch.unibe.scg.team3.board.RawBoardBuilder;
 import ch.unibe.scg.team3.game.SavedGame;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.preference.PreferenceManager;
 
 public class SavedGamesHandler extends DataHandler {
 
 	public SavedGamesHandler(Context context) {
 		super(context);
 	}
 
 	public void saveGame(String name, String board, int words, String time,
 			int score, boolean isPersonal, int guesses) {
 
 		SharedPreferences preferences = PreferenceManager
 				.getDefaultSharedPreferences(context);
 
 		String wordlist = preferences.getString("choose_wordlist", null);
 
 		int timesPlayed = 1;
 
 		String date = (new Date().toString());
 
 		String sql = "INSERT INTO Games VALUES(NULL, ?, '" + board + "', "
 				+ words + ", '" + time + "', '" + date + "', '" + wordlist
 				+ "', " + score + ", '" + isPersonal + "', " + timesPlayed
 				+ ", " + guesses + ")";
 		SQLiteDatabase db = helper.getReadableDatabase();
 		db.execSQL(sql, new String[] { name });
 		db.close();
 
 	}
 
 	public ArrayList<SavedGame> getSavedGames() {
 
 		ArrayList<SavedGame> list = new ArrayList<SavedGame>();
 		SQLiteDatabase db = helper.getReadableDatabase();
 
 		Cursor c = db.rawQuery("SELECT * FROM Games", null);
 
 		if (c != null && c.getCount() != 0) {
 
 			while(c.moveToNext()) {
 				SavedGame game = new SavedGame();
 				game.setId(c.getInt(0));
 				game.setName(c.getString(1));
 				
 				RawBoardBuilder builder = new RawBoardBuilder(c.getString(2));
 				Board board = builder.getBoard();
 				
 				game.setBoard(board);
 				game.setFoundWords(c.getInt(3));
 				game.setTime(c.getString(4));
 				game.setDate(c.getString(5));
 				game.setWordlistId(c.getInt(6));
 				game.setScore(c.getInt(7));
 				game.setPrivate(Boolean.parseBoolean(c.getString(8)));
 				game.setTimesPlayed(c.getInt(9));
 				game.setGuesses(c.getInt(10));
 				list.add(game);
 			}
 			c.close();
 			db.close();
 
 			return list;
 		} else {
 			c.close();
 			db.close();
 			return list;
 		}
 
 	}
	
	
 
 }
