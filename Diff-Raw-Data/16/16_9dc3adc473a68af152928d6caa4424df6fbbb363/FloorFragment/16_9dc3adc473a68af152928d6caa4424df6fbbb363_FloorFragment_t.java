 package com.example.housecontrol;
 
 import java.util.ArrayList;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 
 /**
  * A dummy fragment representing a section of the app, but that simply
  * displays dummy text.
  */
 public class FloorFragment extends Fragment {
 	/**
 	 * The fragment argument representing the section number for this
 	 * fragment.
 	 */
 	public static final String ARG_SECTION_NUMBER = "section_number";
 	public static final String kFloorKey = "floor_key";
 	
 	private Floor mFloor;
 
 	public FloorFragment() {
 		// TODO Auto-generated constructor stub
 		
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		
 		this.mFloor = (Floor)getArguments().getSerializable(kFloorKey);
 		View view = inflater.inflate(R.layout.fragment_floor, null);
 		
 		ListView listview = (ListView)view.findViewById(R.id.listview);
 		
 		String[] values = this.getStringsFromFloor();
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item, values);
 		
 		
 		listview.setAdapter(adapter);
 		
 		listview.setOnItemClickListener(new OnItemClickListener() {
 
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				
 				Toast.makeText(getActivity(), arg2+" "+arg3, Toast.LENGTH_LONG).show();
 			}
 		});
 		
 		registerForContextMenu(listview);
 		
 		return view;
 	}
 	
 	private String[] getStringsFromFloor()
 	{
 		ArrayList<String> array = new ArrayList<String>();
 		String floorString;
 		int roomNr;
 		
 		for (int i=0; i< this.mFloor.getBedroomNb(); ++i) {
 			floorString = getString(R.string.bedroom_name);
 			roomNr = i+1;
 			array.add(floorString+" "+roomNr);
 		}
 		
 		for (int i=0; i< this.mFloor.getKitchenNb(); ++i) {
 			floorString = getString(R.string.kitchen_name);
 			roomNr = i+1;
 			array.add(floorString+" "+roomNr);
 		}
 		
 		for (int i=0; i< this.mFloor.getLivingroomNb(); ++i) {
 			floorString = getString(R.string.livingroom_name);
 			roomNr = i+1;
 			array.add(floorString+" "+roomNr);
 		}
 		
 		for (int i=0; i< this.mFloor.getWCNb(); ++i) {
 			floorString = getString(R.string.wc_name);
 			roomNr = i+1;
 			array.add(floorString+" "+roomNr);
 		}
 		
 		String[] ret = new String[array.size()];
 		ret = array.toArray(ret);
 		return ret;
 	}
 
 	 @Override
      public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
          super.onCreateContextMenu(menu, v, menuInfo);
          
          menu.add(Menu.NONE, R.id.oppEd, Menu.NONE, R.string.oppEd);
          menu.add(Menu.NONE, R.id.oppCom, Menu.NONE, R.string.oppCom);
      }
 
      @Override
      public boolean onContextItemSelected(MenuItem item) {
     	 Intent roomIntent = null;
     	 switch (item.getItemId()) {
             case R.id.oppCom:  
             	 roomIntent = new Intent();
            	 roomIntent.setClass(getActivity(), EquipmentCommActivity.class);  	 
                 break;
             case R.id.oppEd:
            	 //roomIntent = new Intent();
             	 //roomIntent = new Intent(getApplicationContext(), EditRoomActivity.class);
                 break;
             default:
            	 return super.onContextItemSelected(item);
          }
     	 
     	 if(roomIntent != null)
     		 startActivity(roomIntent);
     	 
          return super.onContextItemSelected(item);
      }
 }
