 package source.code;
 
 import java.util.ArrayList;
 
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class BrowseInvites extends Activity {
 
     ArrayList<Lunch> lunches;
     Global state;
     ListView lv;
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.invite_page);
         
         state = (Global) getApplication();
         state.makeLunches();
         lunches = state.getLunchInvites();
         lv = (ListView) findViewById(R.id.listEntries);
         lv.setTextFilterEnabled(true);
 
         lv.setAdapter(new LunchItemAdapter<Lunch>(this, R.layout.attending_item, lunches));
 
 
       	lv.setOnItemClickListener(new OnItemClickListener() {
       	    public void onItemClick(AdapterView<?> parent, View view,
       	        int position, long id) {
       	        Global state = (Global)getApplication();
       	        Intent eventDetails = new Intent(parent.getContext(), InviteDetails.class);
       	        eventDetails.putExtra("activity","invites");
 
       	        Lunch clickedLunch = state.getLunchInvite(position);
       	        eventDetails.putExtra("lunch", clickedLunch);
       	        state.setCurrentClickedLunch(clickedLunch);
       	        startActivity(eventDetails);
       	      
       	    }
       	});
   	  
   	}
     
     @Override
     protected void onNewIntent(Intent intent) {
         Toast.makeText(BrowseInvites.this, "" + intent.getClass(), Toast.LENGTH_SHORT).show();
 
         handleIntent(intent);
     }
 
     private void handleIntent(Intent intent) {
         Toast.makeText(BrowseInvites.this, "" + intent.getClass(), Toast.LENGTH_SHORT).show();
 
     	
     }
   	
   	public void onButtonClicked(View v) {
           // Do something when the button is clicked
   		  Button b = (Button) v;
           //Toast.makeText(BrowseInvites.this, "" + b.getText(), Toast.LENGTH_SHORT).show();
           if (b.getText().equals("Lunches I'm Attending"))
           {
         	  Intent intent = new Intent(this, BrowseAttending.class);
               startActivity(intent); 
               //finish();
 
           }
           if (b.getText().equals("Create"))
           {
         	  Intent intent = new Intent(this, CreateNewLunch.class);
               startActivity(intent); 
               //finish();
           }
           if (b.getText().equals("Lunch Invites"))
           {
         	  // do nothing
           }
       }
   	
   	private class LunchItemAdapter<T> extends ArrayAdapter {
 
   	    private ArrayList<Lunch> lunches;
 
 		public LunchItemAdapter(Context context, int textViewResourceId, ArrayList<Lunch> lunches) {
   	        super(context, textViewResourceId, lunches);
   	        this.lunches = lunches;
   	    }
 
   	    @Override
   	    public View getView(int position, View convertView, ViewGroup parent) 
   	    {
   	        View v = convertView;
 		  	if (v == null) {
 		  	            LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		  	            v = vi.inflate(R.layout.attending_item, null);
 		  	  }
   	      
 		  	Lunch lunch = lunches.get(position);
 		  	TextView title = (TextView) v.findViewById(R.id.title);
 		  	TextView date = (TextView) v.findViewById(R.id.date);
   	        TextView time = (TextView) v.findViewById(R.id.time);
   	        TextView confirmed = (TextView) v.findViewById(R.id.confirmed);
 		  	
 			title.setText(lunch.getTitle());
 			date.setText(lunch.getDate());
			
	        String timeString = lunch.getTime();
	            String[] timeblocks = timeString.split(":");
	            if (timeblocks[0].equals("0")) {
	                timeString = "12".concat(timeString.substring(1));
	            }
 			time.setText(lunch.getTime());
 			
 			if (lunch.isDeclined())
 			{
 				confirmed.setText("declined");
 				confirmed.setVisibility(confirmed.VISIBLE);
 				confirmed.setTextColor(Color.rgb(191, 0, 0));
 			}
 			return v;
 
   	    }
   	}
 }
