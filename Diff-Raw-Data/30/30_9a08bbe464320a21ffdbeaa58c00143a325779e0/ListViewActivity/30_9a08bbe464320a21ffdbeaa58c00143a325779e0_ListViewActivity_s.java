 /*
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 */
 
 package se.ltu.dicnix;
 
 import se.ltu.dicnix.R; 
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 
 /**
 * Lists registered measurement locations for the user to choose from 
 *
 * @author Viktor Stärn
 */
 public class ListViewActivity extends ListActivity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         /**
          * Storing string resources into Array
          */
         String[] locations = getResources().getStringArray(R.array.registered_measurement_locations);
  
         /**
          * Binding resources Array to ListAdapter
          */
         this.setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, R.id.list_item, locations));
  
         ListView lv = getListView();
         
         /**
          * Listening to single list item on click
          */
         lv.setOnItemClickListener(new OnItemClickListener() {
           public void onItemClick(AdapterView<?> parent, View view,
               int position, long id) {
  
               /**
                * Selected item
                */
               String location = ((TextView) view).getText().toString();
               long clicked_item_id = id;
               
               /**
                * Launching new Activity on selecting single List Item
                */
               Intent i = new Intent(getApplicationContext(), LocationDetailsActivity.class);
               /**
                * Sending data to new activity
                */
               i.putExtra("location", location);
               i.putExtra("clicked_item_id", clicked_item_id);
               startActivity(i);
  
           }
         });
         
     }
 
 }
