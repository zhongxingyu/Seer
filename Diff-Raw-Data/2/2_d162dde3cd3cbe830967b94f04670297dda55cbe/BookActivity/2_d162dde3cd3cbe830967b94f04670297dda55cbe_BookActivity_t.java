 package org.csie.mpp.buku;
 
 import org.csie.mpp.buku.db.BookEntry;
 import org.csie.mpp.buku.db.DBHelper;
 import org.csie.mpp.buku.helper.BookUpdater;
 import org.csie.mpp.buku.helper.BookUpdater.OnUpdateFinishedListener;
 
 import com.markupartist.android.widget.ActionBar;
 import com.markupartist.android.widget.ActionBar.AbstractAction;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.os.Bundle;
 import android.text.method.ScrollingMovementMethod;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.RatingBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class BookActivity extends Activity implements OnUpdateFinishedListener {
 	public static final int REQUEST_CODE = 1437;
 	public static final String ISBN = "isbn";
 	public static final String CHECK_DUPLICATE = "duplicate";
 
 	protected ActionBar actionbar;
 	
 	private DBHelper db;
 	private BookEntry entry;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.book);
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 
         /* initialize ActionBar */
         //actionbar = (ActionBar)findViewById(R.id.actionbar);
         
         db = new DBHelper(this);
 
         Intent intent = getIntent();
         String isbn = intent.getStringExtra(ISBN);
         entry = BookEntry.get(db.getReadableDatabase(), isbn);
         
         boolean updateAll = false;
         
         if(entry != null) {
         	if(intent.getBooleanExtra(CHECK_DUPLICATE, false))
         		Toast.makeText(this, R.string.book_already_exists, 3000).show();
         	updateView();
         }
         else {
         	entry = new BookEntry();
         	entry.isbn = isbn;
         	updateAll = true;
         }	
         	
         BookUpdater updater = new BookUpdater(entry);
         updater.setOnUpdateFinishedListener(this);
         
         if(updateAll)
         	updater.updateEntry();
         
         updater.updateInfo();
     }
     
     @Override
     public void onDestroy() {
     	super.onDestroy();
     	
     	db.close();
     }
 
     /* --- OnUpdateFinishedListener	(start) --- */
 	@Override
 	public void OnUpdateFinished() {
 		updateView();
 	}
 
 	@Override
 	public void OnUpdateFailed() {
 		showError();
 	}
 	/* --- OnUpdateFinishedListener	(end) --- */
     
     private void updateView() {
     	if(BookEntry.get(db.getReadableDatabase(), entry.isbn)==null) {
    		((ActionBar)findViewById(R.id.actionbar)).addAction(new AbstractAction(R.drawable.ic_camera) {
     			@Override
     			public void performAction(View view) {
     				entry.insert(db.getWritableDatabase());
 				
     				Intent data = new Intent();
     				data.putExtra(BookActivity.ISBN, entry.isbn);
     				setResult(RESULT_OK, data);
     				finish();
     			}
     		});
     	}
     	if(entry.cover!=null)
     		((ImageView)findViewById(R.id.image)).setImageBitmap(entry.cover);
     	else
     		((ImageView)findViewById(R.id.image)).setImageResource(R.drawable.book);
         ((TextView)findViewById(R.id.title)).setText(entry.title);
         ((TextView)findViewById(R.id.author)).setText(entry.author);
         
         ((RatingBar)findViewById(R.id.rating)).setRating(entry.info.rating);
         ((TextView)findViewById(R.id.description)).setText(entry.info.description);
         ((TextView)findViewById(R.id.description)).setMovementMethod(new ScrollingMovementMethod());
     }
     
     private void showError() {
     	((TextView)findViewById(R.id.title)).setText("Book not found!");
     	Toast.makeText(this, R.string.book_not_found, Toast.LENGTH_LONG).show();
     }
 }
