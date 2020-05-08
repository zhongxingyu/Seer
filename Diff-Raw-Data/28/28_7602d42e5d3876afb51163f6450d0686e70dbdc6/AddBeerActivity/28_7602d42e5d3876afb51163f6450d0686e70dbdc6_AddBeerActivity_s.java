 package ws.wiklund.beerguide.activities;
 
 import ws.wiklund.beerguide.R;
 import ws.wiklund.beerguide.db.BeerDatabaseHelper;
 import ws.wiklund.guides.activities.BaseActivity;
 import ws.wiklund.guides.util.DownloadBeverageTask;
 import ws.wiklund.guides.util.ViewHelper;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnKeyListener;
 import android.widget.EditText;
 
 public class AddBeerActivity extends BaseActivity {
 	private BeerDatabaseHelper helper;
 	private EditText searchStr;
 	
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.add);
         
         helper = new BeerDatabaseHelper(this);
         searchStr = (EditText)findViewById(R.id.EditNo);
         
         searchStr.setOnKeyListener(new OnKeyListener() {
 			@Override
 			public boolean onKey(View v, int keyCode, KeyEvent event) {
 				if(keyCode == KeyEvent.KEYCODE_ENTER  && event.getAction() == 0) {
 					searchWine(v);
 					return true;
 				}
 				
 				return false;
 			}
 		});
     }
     
 	public void searchWine(View view) {    	
 		search(searchStr.getText().toString());
     }
     
     private void search(String no) {
     	if(ViewHelper.isValidNo(this, helper, no)) {
     		new DownloadBeverageTask(helper, true, this, ModifyBeerActivity.class).execute(no);
     	}
     }
 	
 	public void addBeverageManually(View view) {    	
 		Intent intent = new Intent(this, ModifyBeerActivity.class);
    	startActivityForResult(intent, 0);
     }
 
 }
