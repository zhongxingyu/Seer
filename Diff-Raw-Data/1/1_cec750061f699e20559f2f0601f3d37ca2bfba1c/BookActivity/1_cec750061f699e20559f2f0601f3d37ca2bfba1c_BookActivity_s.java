 package org.csie.mpp.buku;
 
 import org.csie.mpp.buku.db.BookEntry;
 import org.csie.mpp.buku.db.DBHelper;
 import org.csie.mpp.buku.helper.BookUpdater;
 import org.csie.mpp.buku.helper.BookUpdater.OnUpdateFinishedListener;
 
 import com.flurry.android.FlurryAgent;
 import com.markupartist.android.widget.ActionBar;
 import com.markupartist.android.widget.ActionBar.AbstractAction;
 import com.markupartist.android.widget.ActionBar.Action;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.os.Bundle;
 import android.text.method.ScrollingMovementMethod;
 import android.util.Log;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.RatingBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class BookActivity extends Activity implements OnUpdateFinishedListener {
 	public static final int REQUEST_CODE = 1437;
 	public static final String CHECK_DUPLICATE = "duplicate";
 	
 	private DBHelper db;
 	private BookEntry entry;
 	private ActionBar actionBar;
 	private Action actionAdd, actionDelete;
 	private BookUpdater updater;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.book);
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
         
         db = new DBHelper(this);
 
         Intent intent = getIntent();
         String isbn = intent.getStringExtra(App.ISBN);
         if(isbn.length()!=10 && isbn.length()!=13){
         	showError(OnUpdateFinishedListener.BOOK_NOT_FOUND);
         	return;
         }
         entry = BookEntry.get(db.getReadableDatabase(), isbn);
         
         actionBar = ((ActionBar)findViewById(R.id.actionbar));
         
         boolean updateAll = false;
         
         if(entry != null) {
         	if(intent.getBooleanExtra(CHECK_DUPLICATE, false))
         		Toast.makeText(this, R.string.book_already_exists, 3000).show();
         	
 			actionDelete = new AbstractAction(R.drawable.ic_delete) {
 				@Override
 				public void performAction(View view) {
 					Intent data = new Intent();
 					data.putExtra(App.ISBN, entry.isbn);
 					setResult(RESULT_FIRST_USER, data);
 					finish();
 				}
 			};
         	updateView();
         	actionBar.addAction(actionDelete);
         }
         else {
         	entry = new BookEntry();
         	entry.isbn = isbn;
         	updateAll = true;
         	
 			actionAdd = new AbstractAction(R.drawable.ic_bookshelf) {
 				@Override
 				public void performAction(View view) {
 					if(entry.insert(db.getWritableDatabase()) == false)
 						Log.e(App.TAG, "Insert failed \"" + entry.isbn + "\".");
 					
 					Intent data = new Intent();
 					data.putExtra(App.ISBN, entry.isbn);
 					setResult(RESULT_OK, data);
 
 					Toast.makeText(BookActivity.this, getString(R.string.added), App.TOAST_TIME).show();
 					actionBar.removeAction(this);
 				}
 			};
         }	
         
         updater = new BookUpdater(entry);
         updater.setOnUpdateFinishedListener(this);
 
         //TODO(ianchou): change the flow here, and try to solve the speed problem  
        	if(updateAll)
        		updater.updateEntry();
         else
         	updater.updateInfo();
     }
     
     @Override
     public void onStart() {
     	super.onStart();
 
 		FlurryAgent.onStartSession(this, App.FLURRY_APP_KEY);
     }
     
     @Override
     public void onStop() {
     	super.onStop();
     	
     	FlurryAgent.onEndSession(this);
     }
     
     @Override
     public void onDestroy() {
     	super.onDestroy();
     	
     	db.close();
     }
 
     /* --- OnUpdateFinishedListener	(start) --- */
 	@Override
 	public void OnUpdateFinished(int status) {
 		if(status == OnUpdateFinishedListener.OK_ENTRY) {
 			updateView();
 			actionBar.addAction(actionAdd);
 			updater.updateInfo();
 		} else if (status == OnUpdateFinishedListener.OK_INFO) {
 			System.err.println(entry.info.reviews.size());
 			updateView();
 		}
 	}
 
 	@Override
 	public void OnUpdateFailed(int status) {		
 		showError(status);
 	}
 	/* --- OnUpdateFinishedListener	(end) --- */
     
     private void updateView() {
     	if(entry.cover!=null)
     		((ImageView)findViewById(R.id.image)).setImageBitmap(entry.cover);
     	else
     		((ImageView)findViewById(R.id.image)).setImageResource(R.drawable.book);
     	
         ((TextView)findViewById(R.id.title)).setText(entry.title);
         ((TextView)findViewById(R.id.author)).setText(entry.author);
         
         ((RatingBar)findViewById(R.id.rating)).setRating(entry.info.rating);
         if(entry.info.description!=null){
         	StringBuilder shortContent = new StringBuilder(); 
         	shortContent.append(entry.info.description.substring(0, Math.min(200, entry.info.description.length())));
         	if(entry.info.description.length()>200){
         		shortContent.append("...");
         	((TextView)findViewById(R.id.description)).setText(shortContent);
         }
         ((TextView)findViewById(R.id.description)).setMovementMethod(new ScrollingMovementMethod());
         
         if(entry.info.reviews!=null){
         	LinearLayout list = (LinearLayout)findViewById(R.id.reviews);
         	for (int i=0; i<entry.info.reviews.size(); i++) {
         		StringBuilder shortContent = new StringBuilder();
         		shortContent.append(entry.info.reviews.get(i).substring(0, Math.min(100, entry.info.reviews.get(i).length())));
         		if(entry.info.reviews.get(i).length()>100)
         			shortContent.append("...");
         		System.err.println(shortContent);
         		View view = getLayoutInflater().inflate(R.layout.list_item_review, null);
         		((TextView)view.findViewById(R.id.list_review)).setText(shortContent);
         		list.addView(view);        	  
         	}
         }
     }        	
     
     private void showError(int status) {
     	if(status == OnUpdateFinishedListener.BOOK_NOT_FOUND) {
     		FlurryAgent.logEvent(App.FlurryEvent.BOOK_NOT_FOUND.toString());
     		((TextView)findViewById(R.id.title)).setText(R.string.book_not_found);
     	}else{
     		Toast.makeText(this, R.string.unexpected_error, App.TOAST_TIME).show();
     	}
     }
 }
