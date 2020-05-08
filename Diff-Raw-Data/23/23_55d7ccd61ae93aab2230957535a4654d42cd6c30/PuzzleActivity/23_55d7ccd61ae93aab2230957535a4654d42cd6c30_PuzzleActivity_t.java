 package is.ru.app.puzzle;
 
 import is.ru.app.db.PuzzleAdapter;
 import android.app.Activity;
 import android.database.Cursor;
 import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
 
 
 public class PuzzleActivity extends Activity{
 	
 	BoardDrawableView mBoardDrawableView;
 	private PuzzleAdapter mPuzzlesAdapter = new PuzzleAdapter( this );
 	private int puzzleId = 1;
	private Button m_button_prev;
	private Button m_button_next;
	private LinearLayout layout;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		Cursor cursor = mPuzzlesAdapter.queryPuzzle(true);
 		while(cursor.moveToNext()){
 			puzzleId = cursor.getInt(1);
 		}
 		cursor.close();
		layout = (LinearLayout) findViewById(R.id.puzzle_layout);
 		mBoardDrawableView = new BoardDrawableView(this, puzzleId);
 	    setContentView(mBoardDrawableView);
 	    
	    mBoardDrawableView = (BoardDrawableView) findViewById( R.id.boardview );
	    
 	    if ( savedInstanceState != null ) {
             String state = savedInstanceState.getString( "stateSAVED" );
             mBoardDrawableView.resetShapes(state);
         }
 	    
	    //   m_button_prev = (Button) findViewById( R.id.buttonPrevious );
	  //  m_button_next = (Button) findViewById( R.id.buttonNext );
	    
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
     
     @Override
     public void onSaveInstanceState( Bundle savedInstanceState ) {
         super.onSaveInstanceState(savedInstanceState);
         savedInstanceState.putString( "stateSAVED",mBoardDrawableView.toString());
     }
     @Override
     public void onContentChanged() {
     	// TODO Auto-generated method stub
     	//super.onContentChanged();
     }
 	}
