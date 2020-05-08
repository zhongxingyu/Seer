 package com.uncc.gameday.activities;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import retrofit.RetrofitError;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 import com.uncc.gameday.R;
 import com.uncc.gameday.registration.Attendee;
 import com.uncc.gameday.registration.RegistrationClient;
 
 public class Search extends MenuActivity {
 	
 	List<Attendee> rsvpList;
 	boolean listFetched = false;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_search_rsvp);
 		new fetchAttendeesThread(this).start();
 	}
 	
 	private class fetchAttendeesThread extends Thread {
 		Context c;
 		public fetchAttendeesThread(Context c) {
 			this.c = c;
 		}
 		
 		public void run() {
 			try {
 				RegistrationClient client = new RegistrationClient(this.c);
 				rsvpList = client.listAttendees();
 				listFetched = true;
 			} catch (RetrofitError e) {
 				Toast.makeText(c, R.string.internet_down_error, Toast.LENGTH_SHORT).show();
 				Log.e("Search", e.getLocalizedMessage());
 			}
 			
 			//sorts RSVPList alphabetically by last name
         	Collections.sort(rsvpList, new Comparator<Attendee>() {
         		@Override
         		public int compare(Attendee a1, Attendee a2) {
         			String compareName = a1.getLastName();
         			String thisName = a2.getLastName();
         			return compareName.compareTo(thisName);
         		}			
         	});
 
         	//function to display RSVPList onto listView
 			runOnUiThread(new Runnable() {
 				@Override
 				public void run() {
					ListView listView = (ListView)findViewById(R.id.RSVPListView);
 				    ArrayAdapter<Attendee> adapter =
 				            new ArrayAdapter<Attendee>(c,android.R.layout.simple_list_item_1, rsvpList);
 				    listView.setAdapter(adapter);	
 				}
 			});		
 	}	
 }
 }
