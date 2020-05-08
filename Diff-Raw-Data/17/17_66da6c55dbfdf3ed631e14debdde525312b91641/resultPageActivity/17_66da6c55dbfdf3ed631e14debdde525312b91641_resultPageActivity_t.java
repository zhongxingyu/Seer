 //TEST PAGE
 //TODO:
 //Add result list
 //Design displaying
 //Add clicking
 
 
 package no.hiof.stud.localstories;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class resultPageActivity extends Activity {
 	public final static String EVENT_ID = "1";
 
 		  @Override
 		  protected void onCreate(Bundle savedInstanceState) {
 
 			    Log.i("LocalStories", "1");
 		    super.onCreate(savedInstanceState);
 
 		    setContentView(R.layout.list_fragment);
 
 		    Log.i("LocalStories", "2");
 		    
 		    final ListView listview = (ListView) findViewById(R.id.listView1);
 		    
 		    ArrayList<Event> events = Search.getList();
 		    String[] values = new String[events.size()];
 		    for(int i=0; i<events.size(); i++){
 		    	values[i] = events.get(i).text;
 		    	
 		    }
 
 		    final ArrayList<String> list = new ArrayList<String>();
 		    for (int i = 0; i < values.length; ++i) {
 		      list.add(values[i]);
 		    }
 
 		    Log.i("LocalStories", "3");
 		    StableArrayAdapter adapter = new StableArrayAdapter(this,
 		    		R.layout.item, R.id.label, values, events);
 		    listview.setAdapter(adapter);
 
 		    listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 
 		      @Override
 		      public void onItemClick(AdapterView<?> parent, final View view,
 				int position, long id) {
 				
 				String ide = Search.getList().get((int) id).id+"";
 				Intent intent = new Intent(getApplicationContext(), EventActivity.class);
 				intent.putExtra(EVENT_ID, ide);
 				startActivity(intent);
 		      }
 
 		    });
 		  }
 
 		  private class StableArrayAdapter extends ArrayAdapter<String> {
 			  ArrayList<Event> events;
 
 		    HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();
 
 		    public StableArrayAdapter(Context context, int viewResourceId, int textViewResourceId,
 		        String[] values, ArrayList<Event> evts) {
 		      super(context, viewResourceId, textViewResourceId, values);
 		    	events=evts;
 		      for (int i = 0; i < values.length; ++i) {
 		        mIdMap.put(values[i], i);
 		      }
 		    }
 
 		    @Override
 		    public long getItemId(int position) {
 		      String item = getItem(position);
 		      return mIdMap.get(item);
 		    }
 
 		    @Override
 		    public boolean hasStableIds() {
 		      return true;
 		    }
 		    
 		    //Unsure if/how this works, needs testing
 		    public View getView(int position, View convertView, ViewGroup viewGroup) {
 		        Event event = events.get(position);
 		        TextView tvContact = (TextView) convertView.findViewById(R.id.date);
 		        tvContact.setText(event.day + "/" + event.month+ " " +event.year);
 
 		        
 		        return convertView;
 		    }
 
 		  }
 
 		} 
