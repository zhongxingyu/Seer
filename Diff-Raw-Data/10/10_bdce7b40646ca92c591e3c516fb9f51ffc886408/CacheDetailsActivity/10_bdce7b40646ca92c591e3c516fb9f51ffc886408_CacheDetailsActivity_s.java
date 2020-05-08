 package fi.jamk.e6379;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.TextView;
 
 public class CacheDetailsActivity extends Activity {
 	private OnClickListener targetButtonListener;
 	public static int index;
 	private Cache cache;
 	
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.cachedetailslayout);
 		Intent callingIntent = getIntent();
 		
 		CacheManager cacheManager = new CacheManager();
 		cache = null;
 		TextView cacheid = (TextView) findViewById(R.id.cacheid_value);
 		TextView cachename = (TextView) findViewById(R.id.cachename_value);
 		TextView cachecreator = (TextView) findViewById( R.id.cachecreator_value );
 		TextView cachetype = (TextView) findViewById(R.id.cachetype_value);
 		ArrayList<Cache> caches = cacheManager.getCaches();
 		index = callingIntent.getIntExtra("CacheID", -1);
 		
 		targetButtonListener = new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent();
 				intent.putExtra("CacheID", index);
 				setResult(RESULT_OK, intent);
 				finish();
 			}
 			
 		};
 		
 		findViewById(R.id.Button01).setOnClickListener( targetButtonListener );
 		
 		cache = caches.get(index);
 		
 		cacheid.setText( cache.getId() );
 		cachename.setText( cache.getName() );
 		cachecreator.setText( cache.getCreator() );
 		cachetype.setText( cache.getType() );
 	}
 	
 	public void openAddNoteActivity(View target) {
 		Intent intent = new Intent(CacheDetailsActivity.this, NoteEditActivity.class);
 		if( cache != null ) {
 			intent.putExtra("cacheID", cache.getId() );
 		}
 		startActivity( intent );
 	}
 	
 	public void openLogActivity( View target ) {
 		Intent intent = new Intent( CacheDetailsActivity.this, LogActivity.class );
 		startActivity( intent );
 	}
 	
 	public void openNotesAndLogsActivity( View target ) {
 		Intent intent = new Intent( CacheDetailsActivity.this, NotesAndLogsActivity.class );
 		startActivity( intent );
 	}
 }
