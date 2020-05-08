 /*******************************************************************************
  * Copyright (C) 2010-2011 Dmitriy Nesterov
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 
 package byku.traindroid;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 
 public final class MainView extends ListActivity {
 	
 	private final String DATE_KEY = "DateKey";
 	private Boolean _save = false;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         updateTimeTable(false);
        DataFacade.Init();
		
         setContentView(R.layout.textlist);
     }
     
     @Override
     public void onResume() {
         super.onResume();
         
         if (_save)
         {
 			//TODO: [Code] Move save actions to UpdateView or remove save actions from StationView (make equally).
         	updateTimeTable(true);
         	_save = false;
         }
         
         fillView();
     }
     
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main_view_menu, menu);
         return true;
     }
     
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         	case R.id.mainview_menu_update:
         		_save = true;
 
         		Intent updateView = new Intent(MainView.this, UpdateView.class);               
                 startActivity(updateView);
         		// TODO: [Code] Rewrite with startActivityForResult and update TT in some cases.
         		return true;
 
         	case R.id.mainview_menu_stations:
         		Intent stationListView = new Intent(MainView.this, StationListView.class);               
                 startActivity(stationListView);
         		
         		return true;
 
         	case R.id.mainview_menu_regexp:
         		Intent regexpView = new Intent(MainView.this, RegexpView.class);               
                 startActivity(regexpView);
         		
         		return true;
         		
         	case R.id.mainview_menu_clear_all:
         		DataFacade.ClearTimeTable(true);
         		updateTimeTable(true);
         		fillView();
         		
         		return true;
         		
         	case R.id.mainview_menu_clear:
         		DataFacade.ClearTimeTable(false);
         		updateTimeTable(true);
         		fillView();
         		
         		return true;
 
         	default:
         		return false;
         }
     }
     
     @Override
     protected void onListItemClick(ListView listView, View view, int position, long id) 
     {
     	Bundle bundle = new Bundle();
     	bundle.putString(getString(R.string.transfer_date_key), ((TextView)view).getText().toString());    	
         Intent intent = new Intent(MainView.this, DateView.class);
         intent.putExtras(bundle);
         
         startActivity(intent);
     }
 
     /*
      * Saves/loads timetables from storage.
      */
     private void updateTimeTable(boolean save)
     {
 		try 
 		{
 			if (save)
 			{
 				FileOutputStream stream = openFileOutput(getString(R.string.data_file_name), Context.MODE_PRIVATE);
 				DataFacade.Serialize(stream);
 				stream.close();
 			}
 			else
 			{
				// TODO: Add check that file not exists. That check not work.
//				File file = new File(getString(R.string.data_file_name));
 
//				if (file.exists())
//				{
					FileInputStream stream = openFileInput(getString(R.string.data_file_name));
					DataFacade.Deserialize(stream);
					stream.close();
//				}
 			}
 		} 
 		catch (Exception e) 
 		{
 			UIUtils.ShowToast(this, "Ошибка: " + e.getMessage());
 		}
     }
     
     private void fillView()
     {
     	ArrayList<HashMap<String, String>> items = new ArrayList<HashMap<String,String>>();
     	ArrayList<String> existsItems = new ArrayList<String>();
     	   	
     	for (TimeTable timeTable : DataFacade.GetTimeTables()) 
     	{
     		String date = timeTable.getDate();
     		
     		if (!existsItems.contains(date))
     		{
 	            HashMap<String, String> item = new HashMap<String, String>();
 	            item.put(DATE_KEY, date);
 	            items.add(item);
 	            
 	            existsItems.add(date);
     		}
 		}
     	
         SimpleAdapter adapter = new SimpleAdapter(this, items, R.layout.textlistitem, 
         		new String[]{DATE_KEY},
         		new int[]{R.id.textListItem});
         setListAdapter(adapter);
     }
 }
