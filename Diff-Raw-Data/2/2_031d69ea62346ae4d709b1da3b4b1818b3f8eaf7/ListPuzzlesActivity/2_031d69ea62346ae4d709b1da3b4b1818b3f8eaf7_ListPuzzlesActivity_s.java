 package is.ru.app.puzzle;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import android.view.View;
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class ListPuzzlesActivity extends ListActivity{
 	
 	private List<Puzzle> puzzles = new ArrayList<Puzzle>();
 	private static final String puzzleFile = "challenge_classic40.xml";
 	
 	public ListPuzzlesActivity(){
 		
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		readInPuzzle(puzzleFile);
		System.out.println("Create");
 	    ArrayAdapter<Puzzle> adapter = new ArrayAdapter<Puzzle>(this, android.R.layout.simple_list_item_1, puzzles);
 	    setListAdapter(adapter);
 	 }
 	
 	 @Override
 	 public void onListItemClick(ListView l, View v, int position, long id) {
 		 Toast.makeText(getApplicationContext(),"Playing Puzzle " + id, Toast.LENGTH_LONG).show();
 		 Intent intent = new Intent(this, PuzzleActivity.class);
 		 intent.putExtra("id", (int)id);
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
 		
 	    @Override
 	    protected void onRestart() {
 	        super.onRestart();
 	    }
 	
 	 public void readInPuzzle(String puzzleFile){
 		PuzzleXmlParser xmlParser = new PuzzleXmlParser();
 		try {
 			InputStream in = getBaseContext().getAssets().open(puzzleFile);
 			xmlParser.parse(in);
 			puzzles = xmlParser.getPuzzles();
 		} catch (IOException e ) {
 			e.printStackTrace();
 		} catch ( XmlPullParserException xmlEx) {
 			xmlEx.printStackTrace();
 		}
 	}
 
 }
