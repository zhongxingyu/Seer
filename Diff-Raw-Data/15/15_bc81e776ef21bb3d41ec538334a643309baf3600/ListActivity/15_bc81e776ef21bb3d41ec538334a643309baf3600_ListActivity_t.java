 package edu.berkeley.cs160.theccertservice.splist;
 
 import android.app.Activity;
 import android.os.Bundle;
import android.widget.ListView;
 
 public class ListActivity extends Activity {
    private ListView myList;
//    private MyAdapter myAdapter;
    
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.list);
		
//        myList = (ListView) findViewById(R.id.MyList);
//        myList.setItemsCanFocus(true);
//        myAdapter = new MyAdapter();
//        myList.setAdapter(myAdapter);
 
 	}
	
	
 }
