 package source.code;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class InviteDetails extends Activity {
 
     private Lunch thisLunch;
     private int thisPosition;
     private boolean fromAttending;
     
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    //TODO: fix layout (accept and decline buttons not aligned)
 	    setContentView(R.layout.eventdetails);
 	
 	    Global state = (Global) getApplication();
 	    //Intent data = getIntent();
 	    thisLunch = state.getCurrentClickedLunch();
 	    Intent intent = getIntent();
         String activity = intent.getStringExtra("activity");
         if (activity.equals("attending"))
         {
         	fromAttending = true;
         	Button acceptconfirm = (Button) findViewById(R.id.accept);
         	acceptconfirm.setText("Confirm");
 
         	if (thisLunch.isConfirmed())
         	{
             	acceptconfirm.setVisibility(acceptconfirm.INVISIBLE);
         	}        	
         }
 	    //System.out.println(thisLunch==null);
 	    //TODO: rewrite xml file and this class so that it displays data from thisLunch. Should
 	    //also show list of friends
 	    TextView location = (TextView) findViewById(R.id.location);
 	    location.setText(thisLunch.getTitle());
 	    
 	    TextView date = (TextView) findViewById(R.id.date);
 	    date.setText(thisLunch.getDate());
 	    
 	    TextView time = (TextView) findViewById(R.id.time);
 	    time.setText(thisLunch.getTime());
 	    
 	    ListView attending = (ListView) findViewById(R.id.listfriends);
 	    ArrayList<Friend> friends = thisLunch.getFriends();
 	    attending.setAdapter(new ArrayAdapter<Friend>(this, R.layout.whitelist_item, friends));
 	    attending.setSelector(android.R.color.transparent); 
 
 	    
 	    TextView comments = (TextView) findViewById(R.id.comments);
 	    comments.setText(thisLunch.getComments());
 	    //thisPosition = data.getIntExtra("position", -1);
 	}
 	
 	public void onButtonClicked(View v) {
 	    Global state = (Global)getApplication();
 	    Intent invites = new Intent(this, BrowseInvites.class);
 	    Intent attending = new Intent(this, BrowseAttending.class);
 
 	    switch(v.getId()) {
 	    case R.id.accept:
 	    	if (fromAttending)
 	    	{
 	    		thisLunch.setConfirmed(true);
 		        startActivity(attending);
 	    	}
 	    	else
 	    	{
 	    		state.addLunchAttending(thisLunch);
 	    		state.removeLunchInvite(thisLunch.getTitle());
 	    		startActivity(attending);
 	    		Toast.makeText(getApplicationContext(), "You have accepted lunch at " +thisLunch.getTitle(), Toast.LENGTH_SHORT).show();
 	    	}
 	    	finish();
 	        break;
 	    case R.id.decline:
 	    	if (fromAttending)
 	    	{
 	    		thisLunch.setDeclined(true);
 		        state.removeLunchesAttending(thisLunch.getTitle());
 		        state.addLunchInvite(thisLunch);
 		        startActivity(attending);
 	    		Toast.makeText(getApplicationContext(), "You have declined lunch at " +thisLunch.getTitle(), Toast.LENGTH_SHORT).show();
 
 	    	}
 	    	else
 	    	{
 	    		thisLunch.setDeclined(true);
 	    		startActivity(invites);
 	    		Toast.makeText(getApplicationContext(), "You have declined lunch at " +thisLunch.getTitle(), Toast.LENGTH_SHORT).show();
 	    	}
 	        finish();
 	        break;
 	    default:
 	        throw new RuntimeException("Button ID not understood");
 	    }
 	}
 
 }
