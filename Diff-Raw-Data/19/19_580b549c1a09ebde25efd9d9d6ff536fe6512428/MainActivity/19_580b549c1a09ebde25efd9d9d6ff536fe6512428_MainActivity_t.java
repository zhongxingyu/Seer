 package byu.drautb.ldshymnbook;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		// Load the hymns into the list
 		ListView listView = (ListView)findViewById(R.id.lv_hymn_list);
 
 		ArrayList<Hymn> hymns = loadHymns();
 		
 		// Sort hymns according to number
 		Collections.sort(hymns, new Comparator<Hymn>() {
 	        @Override public int compare(Hymn h1, Hymn h2) {
 	            return h1.number() - h2.number();
 	        }
 		});
 
 		
 		// First paramenter - Context
 		// Second parameter - Layout for the row
 		// Third parameter - ID of the TextView to which the data is written
 		// Forth - the Array of data
		final ArrayAdapter<Hymn> adapter = new ArrayAdapter<Hymn>(this, android.R.layout.simple_list_item_1, android.R.id.text1, hymns);
		
		// Add listener to handle changes in the search text
		final EditText etSearch = (EditText)findViewById(R.id.et_search);
		etSearch.addTextChangedListener(new TextWatcher() {
		    public void onTextChanged(CharSequence s, int start, int before, int count) {}
		    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
		    public void afterTextChanged(Editable s) {
		        adapter.getFilter().filter(etSearch.getText().toString());
		    }
		});
		
 		// Assign adapter to ListView
 		listView.setAdapter(adapter); 
 		
 		// Set up an event for item clicks
 		listView.setOnItemClickListener(new OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 
                 String item = ((TextView)view).getText().toString();
                 item = item.replace(" - ", " ");
                 item = item.concat(".pdf");
                 
                 // Show a brief popup of the select hymn name
                 Toast.makeText(getApplicationContext(), item, Toast.LENGTH_SHORT).show();
                 
                 // Get a publicly accesible (in terms of apps on the device) handle to the pdf file
                 File file = getPublicFile(item);
 
                 Intent intent  = new Intent(Intent.ACTION_VIEW);
                 intent.setDataAndType(Uri.fromFile(file), "application/pdf");
                 intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                 startActivity(intent);
             }
         });
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	@SuppressWarnings("unused")
 	public File getPublicFile(String filename) {
 		File file = new File(getExternalFilesDir(null), filename);
 		
 		// If file is null, it means it doesn't already exist in local public storage,
 		// so we copy it over there.
 	    if (!file.exists()) {
 	        try {
 	            InputStream is = getResources().getAssets().open("pdfs/" + filename);
 	            OutputStream os = new FileOutputStream(file);
 	            byte[] data = new byte[is.available()];
 	            is.read(data);
 	            os.write(data);
 	            is.close();
 	            os.close();
 	        } catch (IOException e) {
 	            Log.w("ExternalStorage", "Error writing " + file, e);
 	        }
 	    }
 
         return file;
 	}
 	
 	public ArrayList<Hymn> loadHymns() {
 		// Container for hymn objects
 		ArrayList<Hymn> hymns = new ArrayList<Hymn>();
 			
 		// All the filenames in the pdf directory
 		String[] pdfs = null;
 		try {
 			pdfs = getAssets().list("pdfs");
 		} catch (Exception e) {}
 		
 		if (pdfs == null)
 			return hymns;
 		
 		for (String pdf : pdfs) {
 			int number = Integer.parseInt(pdf.substring(0, pdf.indexOf(' ')));
 			String name = pdf.substring(pdf.indexOf(' ') + 1, pdf.indexOf(".pdf"));
 			
 			hymns.add(new Hymn(number, name, pdf));				   
 		}
 		
 		return hymns;
 	}
 	
 
 }
