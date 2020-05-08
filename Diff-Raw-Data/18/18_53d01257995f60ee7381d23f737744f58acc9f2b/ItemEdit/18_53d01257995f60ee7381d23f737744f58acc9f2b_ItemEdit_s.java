 package moshe.apps.shoppinglist;
 import android.app.Activity;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TimePicker;
 
 
 public class ItemEdit extends Activity {
 	private Long mRowId;
 	private EditText mBodyText;
 	private TimePicker mQuantityPicker;
     @Override
     protected void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
         setContentView(R.layout.item_edit);
         setTitle(R.string.item_title);
         
         Button confirmButton = (Button) findViewById(R.id.add);
         
         
         mBodyText = (EditText) findViewById(R.id.body);
         mQuantityPicker =(TimePicker) findViewById(R.id.quantity);
         mQuantityPicker.setCurrentHour(1);
         
         mRowId   = (savedInstanceState == null) ? null :
             (Long) savedInstanceState.getSerializable(DBAdapter.KEY_ROWID);
 		if (mRowId == null) {
 			Bundle extras = getIntent().getExtras();
			mRowId = extras != null ? extras.getLong(DBAdapter.KEY_ROWID)
 									: null;
 		}
         
 		
 		
 		if(mRowId != null){
 			DBAdapter db=new DBAdapter(this);
 			db.open();
 			Cursor c =db.getItem( mRowId ) ;
 			mBodyText.setText(c.getString(
                     c.getColumnIndexOrThrow(DBAdapter.KEY_TITLE)));
 			
 			mQuantityPicker.setCurrentHour(c.getInt(
                     c.getColumnIndexOrThrow(DBAdapter.KEY_QUANTITY)));
 		    db.close();
 		}
         
         confirmButton.setOnClickListener(new View.OnClickListener() {
         	 
             public void onClick(View view) {
             	            
             	
             	
             	
             	DBAdapter db = new DBAdapter(view.getContext());
             	db.open(); 
             	if (mRowId == null) {
             	    db.insertNewItem(             	    	
             	    	mBodyText.getText().toString(),//title
            	    	1);
             	
             	}
             	else{
             		
             		int h=mQuantityPicker.getCurrentHour();
             		db.updateItem(mRowId,
             				mBodyText.getText().toString(),
             				h) ;
             	
             	}    	
             	db.close();  
        	
                 setResult(RESULT_OK);
                 finish();
             }
 
         });     
               
         
     }//end of onCreate
     
 
     @Override
     protected void onPause() {
         super.onPause();
        
         
     }
     
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
 
     }
 }
