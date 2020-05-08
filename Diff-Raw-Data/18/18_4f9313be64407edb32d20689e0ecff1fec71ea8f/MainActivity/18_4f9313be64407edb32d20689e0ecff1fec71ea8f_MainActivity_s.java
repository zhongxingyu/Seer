 package ugtug.lab3;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends ListActivity {
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         
 		super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         Resources resources = getResources();
        rumiNumbers = resources.getStringArray(R.array.list);
         
         this.getListView().setTextFilterEnabled(true);
         this.getListView().setOnItemClickListener(new OnItemClickListener() {
         	
 			public void onItemClick(AdapterView<?> parent, View target, int position, long id) {
 
 				CharSequence text = ((TextView) target).getText();
 				Toast t = Toast.makeText(getApplicationContext(), "Displaying " + text, Toast.LENGTH_SHORT);
 				t.show();
 
 				Log.v("MainActivity", "displaying: " + text + ", position: " + position + ", id:= " + id);
 
 				Intent otherIntent = new Intent(MainActivity.this, OtherActivity.class);
 				otherIntent.putExtra("key", text);
 				
 				startActivity(otherIntent);
 			}        	
         });
                 
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, rumiNumbers);
         this.getListView().setAdapter(adapter);
         
         // allow for long-click context menu
         registerForContextMenu(this.getListView());
 
         setContentView(R.layout.main);
     }
     
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		menu.add(0,1,0,"Bio");
 		menu.add(0,2,0,"Works");
 		return true;
     }
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		Toast t = Toast.makeText(getApplicationContext(), "Clicked " + item.getTitle(), Toast.LENGTH_SHORT);
 		t.show();
 
 		Intent displayIntent = new Intent(MainActivity.this, DisplayActivity.class);
 
 		switch(item.getItemId()) {
 			
 			// bio
 			case 1:
 				displayIntent.putExtra("key", "bio");
 				startActivity(displayIntent);
 				break;
 
 			// works
 			case 2:
 				displayIntent.putExtra("key", "works");
 				startActivity(displayIntent);
 				break;
 				
 			default:
 				break;
 		}
 
 	   return true;
 	}
 	
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		Log.v("MainActivity", v.getClass().getName());
 		
 		TextView tv = (TextView) ((ListView) v).getSelectedView();
 		CharSequence text = tv.getText();
 		
 		rumiTextNumber = text.toString();
 
 		menu.setHeaderTitle("Read, Listen, Search");
 		menu.add(200, 200, 200, "Read");
 		menu.add(200, 201, 201, "Listen");
 		menu.add(200, 202, 202, "Search");
 	}
 	
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		
 		Toast t = Toast.makeText(getApplicationContext(), "Clicked " + item.getTitle(), Toast.LENGTH_SHORT);
 		t.show();
 
 		switch(item.getItemId()) {
 			case 200:
 				Intent quatrainReadIntent = new Intent(MainActivity.this, QuatrainActivity.class);
 				quatrainReadIntent.putExtra("key", rumiTextNumber);
 				startActivity(quatrainReadIntent);
 				break;
 				
 			case 201:
 				Intent quatrainListenIntent = new Intent(MainActivity.this, QuatrainActivity.class);
 				quatrainListenIntent.putExtra("key", rumiTextNumber);
 				quatrainListenIntent.putExtra("mode", "listen");
 				startActivity(quatrainListenIntent);
 				break;
 
 			case 202:
 				Intent searchIntent = new Intent(MainActivity.this, SearchActivity.class);
 				searchIntent.putExtra("key", rumiTextNumber);
 				startActivity(searchIntent);
 				break;			
 		}
 		return true;
 	}
 
 }
