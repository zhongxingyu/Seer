 package source.code;
 
 import java.util.ArrayList;
 
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class BrowseAttending extends Activity {
 
     ArrayList<Lunch> lunches;
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.attending_page);
 
         Global state = (Global) getApplication();
         lunches = state.getLunchesAttending();
         ListView lv = (ListView) findViewById(R.id.listEntries);
         lv.setTextFilterEnabled(true);
         lv.setAdapter(new LunchItemAdapter<Lunch>(this, R.layout.attending_item, lunches));
 
         
 
   	  lv.setOnItemClickListener(new OnItemClickListener() {
   	    public void onItemClick(AdapterView<?> parent, View view,
   	        int position, long id) {
   	    	Global state = (Global)getApplication();
   	        Intent eventDetails = new Intent(parent.getContext(), InviteDetails.class);
   	        eventDetails.putExtra("activity","attending");
   	        Lunch clickedLunch = state.getLunchAttending(position);
   	        state.setCurrentClickedLunch(clickedLunch);
   	        startActivity(eventDetails);
  	        finish();
   	      
   	    }
   	  });
   	}
   	
   	public void onButtonClicked(View v) {
           // Do something when the button is clicked
           //Toast.makeText(BrowseAttending.this, "" + b.getText(), Toast.LENGTH_SHORT).show();
           
   		  switch(v.getId()) {
   		  case R.id.create:
               Intent create = new Intent(this, CreateNewLunch.class);
               startActivity(create);
               break;
   		  case R.id.lunchinvitestab:
   		    Intent lunchinvites = new Intent(this, BrowseInvites.class);
             //intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
             startActivity(lunchinvites); 
   		  case R.id.lunchesattendingtab:
   			  
             break;
           default:
               throw new RuntimeException("Button ID unknown");
   		  }
 
       }
 
   	
   	public void makeLunches()
   	{
   		lunches.add(new Lunch("Attending"));
   		lunches.add(new Lunch("Attending"));
   		lunches.add(new Lunch("Attending"));
   		lunches.add(new Lunch("Attending"));
   		lunches.add(new Lunch("Attending"));
   		lunches.add(new Lunch("Attending"));
   		lunches.add(new Lunch("Attending"));
   		lunches.add(new Lunch("Attending"));
   		lunches.add(new Lunch("Attending"));
   		lunches.add(new Lunch("Attending"));
   		lunches.add(new Lunch("Attending"));
   		lunches.add(new Lunch("Attending"));
   		lunches.add(new Lunch("Attending"));
   		lunches.add(new Lunch("Attending"));
   		lunches.add(new Lunch("Attending"));
 
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
   	        TextView details = (TextView) v.findViewById(R.id.details);
   	        TextView confirmed = (TextView) v.findViewById(R.id.confirmed);
 		  	
 			title.setText(lunch.getTitle());
 			details.setText(lunch.getTime() + "      " + lunch.getDate());
 			
 			if (lunch.isConfirmed())
 			{
 				confirmed.setText("confirmed");
 			}
 			return v;
 
   	    }
   	}
 }
