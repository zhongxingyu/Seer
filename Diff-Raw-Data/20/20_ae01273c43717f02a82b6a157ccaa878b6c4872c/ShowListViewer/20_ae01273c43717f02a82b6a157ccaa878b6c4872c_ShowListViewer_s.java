 package com.NativeSample;
 
 import android.app.Activity;
 import android.app.ListActivity;
 import android.os.Bundle;
 import android.widget.ArrayAdapter;
 
public class ShowListViewer extends Activity {
 
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.listviewer);
	    // TODO Auto-generated method stub
	}

	public class TutListActivity extends ListActivity {
	    private String strings[]={"Crack the Shell", "Digital Fortress", "Labyrinth"};
 		@Override
 	    public void onCreate(Bundle savedInstanceState) {
 	        super.onCreate(savedInstanceState);
		    setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, strings));
 	    }
 	}
}
