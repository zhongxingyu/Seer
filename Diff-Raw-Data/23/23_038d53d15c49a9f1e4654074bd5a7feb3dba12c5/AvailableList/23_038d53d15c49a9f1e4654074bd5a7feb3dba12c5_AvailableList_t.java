 package cmov.pa;
 
 import java.util.ArrayList;
 
 import com.google.android.c2dm.C2DMessaging;
 
 import cmov.pa.CMOVPA2Activity.MyListAdapter;
 import cmov.pa.database.DatabaseAdapter;
 import cmov.pa.utils.HouseInfo;
 import android.app.Activity;
 import android.app.ListActivity;
 import android.app.NotificationManager;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class AvailableList extends Activity{
 	MyListAdapter new_adapter;
 	MyListAdapter updated_adapter;
 	Api api;
 	
 	
 	 @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.list_changed);
         
         api = (Api)getApplicationContext();  
         
         new_adapter = new MyListAdapter();
         updated_adapter = new MyListAdapter();
         
         ((ListView)findViewById(R.id.listView1)).setAdapter(new_adapter);
         ((ListView)findViewById(R.id.listView2)).setAdapter(updated_adapter);
         
 		
         updated_adapter.setList(api.available_updated_list);
         new_adapter.setList(api.available_new_list);		
         
         
         if(new_adapter.getCount() == 0){
 			((RelativeLayout)findViewById(R.id.list_notification_new_container)).setVisibility(View.GONE);
 		}
 		
 		if(updated_adapter.getCount() == 0){
 			((RelativeLayout)findViewById(R.id.list_notification_updated_container)).setVisibility(View.GONE);
 		}
         
 
 		((ListView)findViewById(R.id.listView1)).setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				
 				ArrayList<Integer> tmp = new_adapter.getListIds();
 				
 				Intent intent = new Intent(getApplicationContext(),ShowRealEstate.class);
 				intent.putExtra("index", arg2);
 				intent.putExtra("mode", api.MODE_AVAILABLE_NEW);
 				intent.putIntegerArrayListExtra("ids_list", tmp);
 		        startActivity(intent);
 				
 			}
 		});
 		 
 		 
 	     ((ListView)findViewById(R.id.listView2)).setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				
 				ArrayList<Integer> tmp = updated_adapter.getListIds();
 				
 				Intent intent = new Intent(getApplicationContext(),ShowRealEstate.class);
 				intent.putExtra("index", arg2);
 				intent.putExtra("mode", api.MODE_AVAILABLE_UPDATE);
 				intent.putIntegerArrayListExtra("ids_list", tmp);
 	
 		        startActivity(intent);
 				
 			}
 		});
 		
 		
         
     }
	 
	 
	 
	 @Override
		public void onBackPressed() {
			api.available_new_list = new ArrayList<HouseInfo>();
			api.available_updated_list = new ArrayList<HouseInfo>();
			
			super.onBackPressed();
		}
 	
 	
 	
 	 public class MyListAdapter extends BaseAdapter {
 
 	    	ArrayList<HouseInfo> list;
 	    	
 	    	
 	    	public MyListAdapter(){
 	    		list = new ArrayList<HouseInfo>();
 	    		
 	    	}
 	    	
 	    	public void setList(ArrayList<HouseInfo> l){
 	    		list = l;
 	    	}
 	    	
 	    	
 	    	public ArrayList<Integer> getListIds(){
 	    		ArrayList<Integer> l = new ArrayList<Integer>();
 
 	    		for(HouseInfo h:list)
 	    			l.add(h.getId());
 
 	    		
 	    		return l;
 	    	}
 	    	
 			@Override
 			public int getCount() {
 				return list.size();
 			}
 
 			@Override
 			public Object getItem(int arg0) {
 				return list.get(arg0);
 			}
 
 			@Override
 			public long getItemId(int arg0) {
 				return 0;
 			}
 
 			@Override
 			public View getView(int position, View convertView, ViewGroup parent) {    
 				
 	            	 LayoutInflater infalInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 	        		 convertView = infalInflater.inflate(R.layout.list_child, null);
 	        		 
 	        		 HouseInfo house = list.get(position);
 	        		 
 	        		 //System.out.println(house.getAddress());
 	        		 
 	        		 ((TextView) convertView.findViewById(R.id.list_child_address)).setText(house.getAddress());
 					 
 					
 					if(!house.isFor_sale())
 						((RelativeLayout)convertView.findViewById(R.id.list_child_layout)).setBackgroundResource(R.drawable.background_sold);
 					else
 						((RelativeLayout)convertView.findViewById(R.id.list_child_layout)).setBackgroundResource(R.drawable.background_selling);
 					 
 					 
 					 if(house.getKind().equalsIgnoreCase("Apartment"))
 						 ((ImageView)convertView.findViewById(R.id.list_child_image)).setImageResource(R.drawable.flat_icon);
 					 else if(house.getKind().equalsIgnoreCase("Home"))
 						 ((ImageView)convertView.findViewById(R.id.list_child_image)).setImageResource(R.drawable.house_icon);
 					 else
 						 ((ImageView)convertView.findViewById(R.id.list_child_image)).setImageResource(R.drawable.castle_icon);
 		
 					 
 					
 					 ((TextView) convertView.findViewById(R.id.list_child_bedrooms)).setText("T"+house.getBedrooms());
 					 ((TextView) convertView.findViewById(R.id.list_child_city)).setText(house.getCity());
 					 
 				return convertView;
 			}
 		}
 }
