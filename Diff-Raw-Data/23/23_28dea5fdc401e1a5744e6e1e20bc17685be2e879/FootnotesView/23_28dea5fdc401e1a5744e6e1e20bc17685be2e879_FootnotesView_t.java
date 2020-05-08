 package gov.cdc;
 
 import gov.cdc.R;
 import android.os.Bundle;
 import android.app.ListActivity;
import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.Window;
 import android.widget.ArrayAdapter;
import android.widget.Button;
 import android.widget.ListView;
 import java.util.ArrayList;
 
 public class FootnotesView extends ListActivity {
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.activity_footnotes_view);
		
		final Button doneButton = (Button)findViewById(R.id.doneButton);
        doneButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                //startActivity(new Intent(MyActivity.this,ActivityEins.class));
            	Log.d("TEST","TEST");
            	finish();
            }
        });
		
		
		
 		ArrayList<String> footnotes = MainView.controller.currentNode.getFootnotes();
 		String [] footnotes_array = footnotes.toArray(new String[footnotes.size()]);
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.footnote_item, footnotes_array);
 		setListAdapter(adapter);
 		// setContentView(R.layout.activity_footnotes_view);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_footnotes_view, menu);
 		return true;
 	}
 
 }
