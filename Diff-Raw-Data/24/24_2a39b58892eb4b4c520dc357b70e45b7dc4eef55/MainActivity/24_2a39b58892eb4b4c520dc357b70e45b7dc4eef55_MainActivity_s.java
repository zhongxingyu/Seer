 package is.ru.app.puzzle;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 
 import org.xmlpull.v1.XmlPullParserException;
 
 import is.ru.app.db.DBHelper;
 import is.ru.app.db.PuzzleAdapter;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.view.Menu;
 import android.view.View;
 import android.widget.SimpleCursorAdapter;
 
 public class MainActivity extends Activity {
 	
 	private static final String puzzleFile = "challenge_classic40.xml";
     private PuzzleAdapter mPuzzlesAdapter = new PuzzleAdapter( this );
    
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		Cursor cursor = mPuzzlesAdapter.queryPuzzles();
 		if(cursor.getCount() == 0){
 			setUpData(cursor);
 		}else{
 
 		}
 		cursor.close();
 	}
 
 	/* 
 	 your implementation of onCreate() should define the user interface and 
 	 possibly instantiate some class-scope variables.
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	/*
 	 * Sets up data for the first time
 	 */
 	public void setUpData(Cursor cursor){
 		List<Puzzle> pList = readInPuzzle(puzzleFile);
 		for(Puzzle p : pList){
			mPuzzlesAdapter.insertPuzzle( Integer.parseInt(p.id), p.setup,Integer.parseInt(p.level), false);
 		}
 	}
 	
 	 public List<Puzzle> readInPuzzle(String puzzleFile){
 		PuzzleXmlParser xmlParser = new PuzzleXmlParser();
 		try {
 			InputStream in = getBaseContext().getAssets().open(puzzleFile);
 			xmlParser.parse(in);
 			return xmlParser.getPuzzles();
 		} catch (IOException e ) {
 			e.printStackTrace();
 		} catch ( XmlPullParserException xmlEx) {
 			xmlEx.printStackTrace();
 		}
 		return null;
 	 }
 	
 	public void buttonPlay(View view) {
 		//open puzzle from here
 		 Intent intent = new Intent(this, PuzzleActivity.class);
 		 startActivity(intent);
 	}
 	
 	public void buttonPuzzleList(View view) {
 		//open puzzle from here
 	    Intent intent = new Intent(this, ListPuzzlesActivity.class);
 	    startActivity(intent);
 	}
 
 	@Override
 	protected void onPause() {
 		// TODO Auto-generated method stub
 		super.onPause();
 	}
 
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 	}
 
 	@Override
 	protected void onStop() {
 		// TODO Auto-generated method stub
 		super.onStop();
 	}
 }
