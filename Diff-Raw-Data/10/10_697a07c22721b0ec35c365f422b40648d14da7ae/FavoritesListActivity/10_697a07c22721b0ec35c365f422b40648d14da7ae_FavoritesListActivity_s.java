 package edu.uj.pmd.locationalarm.activities;
 
 import android.app.Activity;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.*;
 import edu.uj.pmd.locationalarm.R;
 import edu.uj.pmd.locationalarm.favorites.DatabaseHandler;
 import edu.uj.pmd.locationalarm.favorites.Destination;
 import edu.uj.pmd.locationalarm.favorites.tasks.DeleteFavoriteDestinationTask;
 import edu.uj.pmd.locationalarm.utilities.AppPreferences;
 
 import java.util.ArrayList;
 
 /**
  * User: piotrplaneta
  * Date: 29.12.2012
  * Time: 17:43
  */
 public class FavoritesListActivity extends Activity {
 
     private ArrayList<Destination> destinations;
     private ArrayAdapter<Destination> adapter;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_favorites_list_view);
 
         ListView listView = (ListView) findViewById(R.id.list);
 
         DatabaseHandler database = new DatabaseHandler(this);
 
         this.destinations = (ArrayList<Destination>) database.getAllFavoriteDestinations();
 
         this.adapter = new ArrayAdapter<Destination>(this, R.layout.favorites_list_view_item, destinations);
         listView.setAdapter(adapter);
 
         listView.setTextFilterEnabled(true);
 
         registerForContextMenu(listView);
 
         listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view,
                                     int position, long id) {
                // When clicked, show a toast with the TextView text
                Toast.makeText(getApplicationContext(),
                        destinations.get(position).getName(), Toast.LENGTH_SHORT).show();
 
                 AppPreferences appPrefs = new AppPreferences(getApplicationContext());
                 appPrefs.setDestinationLongitude((float)destinations.get(position).getLongitude());
                 appPrefs.setDestinationLatitude((float)destinations.get(position).getLatitude());
 
                 Intent returnIntent = new Intent();
                 setResult(RESULT_OK, returnIntent);
                 finish();
             }
         });
 
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
         AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
         menu.setHeaderTitle(this.destinations.get(info.position).getName());
         menu.add(R.string.delete);
     }
 
     @Override
     public boolean onContextItemSelected(MenuItem item) {
         AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
         Destination listItemToDelete = destinations.get(info.position);
 
         new DeleteFavoriteDestinationTask(this).execute(listItemToDelete);
 
         destinations.remove(info.position);
         adapter.notifyDataSetChanged();
 
         return true;
     }
 
 }
