 package no.ntnu.idi.socialhitchhiking.journey;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.BitSet;
 import java.util.Calendar;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 
 import org.apache.http.client.ClientProtocolException;
 
 
 import no.ntnu.idi.freerider.model.Journey;
 import no.ntnu.idi.freerider.model.Route;
 import no.ntnu.idi.freerider.model.TripPreferences;
 import no.ntnu.idi.freerider.model.Visibility;
 import no.ntnu.idi.freerider.protocol.JourneyRequest;
 import no.ntnu.idi.freerider.protocol.PreferenceRequest;
 import no.ntnu.idi.freerider.protocol.PreferenceResponse;
 import no.ntnu.idi.freerider.protocol.Request;
 import no.ntnu.idi.freerider.protocol.RequestType;
 import no.ntnu.idi.freerider.protocol.Response;
 import no.ntnu.idi.freerider.protocol.ResponseStatus;
 import no.ntnu.idi.socialhitchhiking.R;
 import no.ntnu.idi.socialhitchhiking.client.RequestTask;
 import no.ntnu.idi.socialhitchhiking.utility.DateChooser;
 import no.ntnu.idi.socialhitchhiking.utility.ShareOnFacebook;
 import no.ntnu.idi.socialhitchhiking.utility.TripOptionAdapter;
 import no.ntnu.idi.socialhitchhiking.utility.SocialHitchhikingActivity;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Toast;
 
 /**
  * @author Jose Luis Trigo
  */
 
 public class TripOptions extends SocialHitchhikingActivity {
     private ListView listView1;
     private Calendar dateAndTime;
     private Calendar newTime;
 	private DateChooser dc;
 	private Journey journey;
 	private Route selectedRoute;
 	private TripPreferences tripPreferences;
 	private Integer selectedPrivacy = null;
 	private Visibility privacyPreference;
 	private TripOptionAdapter adapter;
 	private List<TripOption> list_trip_options;
 	private Integer seatValue;
 	private Request req;
 	private PreferenceResponse res;
 	private String[] items = {"Music", "Animals", "Breaks", "Talking", "Smoking"};
 	private boolean[] checkedExtrasFromUserPreferences = {false,false,false,false,false};
     
 	private PropertyChangeListener propLis = new PropertyChangeListener() {
 		@Override
 		public void propertyChange(PropertyChangeEvent event) {
 			if(event.getPropertyName() == DateChooser.DATE_CHANGED){
 //				dateChanged=true;
 				dateAndTime = (Calendar) event.getNewValue();
 				
 				list_trip_options.set(0, new TripOption(R.drawable.trip_icon_calendar, "Date", formatDate(dateAndTime)));
 //				Toast.makeText(getApplicationContext(), dateAndTime.getTime().toString(), Toast.LENGTH_LONG).show();
 				adapter.notifyDataSetChanged();
 				
 			}
 			if(event.getPropertyName() == DateChooser.TIME_CHANGED){
 //				timeChanged=true;
 				newTime = (Calendar) event.getNewValue();
 				dateAndTime.set(Calendar.HOUR_OF_DAY,newTime.get(Calendar.HOUR_OF_DAY));
 				dateAndTime.set(Calendar.MINUTE,newTime.get(Calendar.MINUTE));
 				
 				list_trip_options.set(1, new TripOption(R.drawable.trip_icon_clock, "Time", formatTime(dateAndTime)));
 				adapter.notifyDataSetChanged();
 			}
 		}
 	};
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.trip_options);
         //Get privacy default value from user preferences
         privacyPreference=getApp().getSettings().getFacebookPrivacy();
        
         //Get user default TripPreferences
         tripPreferences= new TripPreferences();
         tripPreferences.setPrefId(0);
         
         req = new PreferenceRequest(RequestType.GET_PREFERENCE, getApp().getUser(), tripPreferences);
 		res = null;
 			try {
 				res = (PreferenceResponse) RequestTask.sendRequest(req,getApp());
 				
 			} catch (ClientProtocolException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			} catch (ExecutionException e) {
 				e.printStackTrace();
 			}
 			
         
         tripPreferences = res.getPreferences();
         seatValue= tripPreferences.getSeatsAvailable();
         
     	String ex = "";
     	for(int i=0 ; i<tripPreferences.getExtras().length() ; i++){
     		if(tripPreferences.getExtras().get(i)){
     			ex=ex+items[i]+" ";
     			checkedExtrasFromUserPreferences[i]=tripPreferences.getExtras().get(i);
     		}
     	}
     	
         selectedRoute=getApp().getSelectedRoute();
         dateAndTime=Calendar.getInstance();
         
         list_trip_options = new ArrayList<TripOption>();
         
         //Initialization of default strings and position of default privacy option
         
         String privacyString="";
         if(privacyPreference==Visibility.FRIENDS){
         	privacyString="Friends";
         	selectedPrivacy=0;
         }
         if(privacyPreference==Visibility.FRIENDS_OF_FRIENDS){
         	privacyString="Friends of Friends";
     		selectedPrivacy=1;
     	}
         if(privacyPreference==Visibility.PUBLIC){
         	privacyString="Public";
     		selectedPrivacy=2;
     	}
        
        //Initialization of default TripOptions string values
         list_trip_options.add(new TripOption(R.drawable.trip_icon_calendar, "Date", formatDate(dateAndTime)));
         list_trip_options.add(new TripOption(R.drawable.trip_icon_clock, "Time",formatTime(dateAndTime)));
         list_trip_options.add(new TripOption(R.drawable.trip_icon_seats, "Seats Available", Integer.toString(tripPreferences.getSeatsAvailable())));
         list_trip_options.add(new TripOption(R.drawable.trip_icon_fb, "Privacy", privacyString));
         list_trip_options.add(new TripOption(R.drawable.trip_icon_plus, "Preferences", ex));
         
         adapter = new TripOptionAdapter(this, R.layout.list_row_trip_options, list_trip_options);       
         listView1 = (ListView)findViewById(R.id.list);
          
         View header = (View)getLayoutInflater().inflate(R.layout.trip_options_header, null);
         listView1.addHeaderView(header);
         
         listView1.setAdapter(adapter);
         
         listView1.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parentView, View childView, int position, long id) {
 				listView1.setItemChecked(position, true);
 				
 				switch (position) {
 				case 1:
 					setDate();
 					break;
 				case 2:
 					setTime();
 					break;
 				case 3:
 					setSeats();
 					break;
 				case 4:
 					setPrivacy();
 					break;
 				case 5:
 					setExtras();
 					break;
 				default:
 					break;
 				}
 			}
 		});
         
         
     }
     void setDate(){
     	dc = new DateChooser(TripOptions.this, propLis);
 		dc.setTitleDate("Set Date of Trip");
 		dc.showDatePicker();
     }
     
     void setTime(){
     	dc = new DateChooser(TripOptions.this, propLis);
 		dc.setTitleTime("Set Time of Trip");
 		dc.showTimePicker();
     }
     void setSeats(){
 
 //    	seatValue = 1;
     	AlertDialog.Builder builder = new AlertDialog.Builder(this);
 	    builder.setTitle("Seats");	
 	    builder.setMessage("Select seats available");
 	    
 	    LayoutInflater inflater = this.getLayoutInflater();
 	    final View textEntryView = inflater.inflate(R.layout.number_picker, null);
 	    final EditText editTextField = (EditText) textEntryView.findViewById(R.id.numberofseats);
 	    Button plus = (Button) textEntryView.findViewById(R.id.bAdd);
 	    Button minus = (Button) textEntryView.findViewById(R.id.bSub);
 	    builder.setView(textEntryView);
 	    editTextField.setText(seatValue.toString());
 		plus.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				seatValue++;
 				editTextField.setText(seatValue.toString());
 			}
 		});
 		minus.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				if(seatValue>1)
 					seatValue--;
 				editTextField.setText(seatValue.toString());
 			}
 		});
 		
 	    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int id) {
                 String editTextFieldValue = editTextField.getText().toString();
 				if(editTextFieldValue.length()==0)
                 	editTextFieldValue = seatValue.toString();
 				else{
 					tripPreferences.setSeatsAvailable(Integer.valueOf(editTextFieldValue));
 					seatValue = Integer.valueOf(editTextFieldValue);
 				}
 				list_trip_options.set(2, new TripOption(R.drawable.trip_icon_seats, "Seats Availabe", editTextFieldValue));
 				adapter.notifyDataSetChanged();
 			}
 	    });
 	    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
 	    	public void onClick(DialogInterface dialog, int id) {
 	    	}
 	    });      
 	    builder.show();
     }
     
     void setPrivacy(){
 
     	AlertDialog.Builder builder = new AlertDialog.Builder(this);
     	builder.setTitle("Set Privacy");
     	builder.setSingleChoiceItems(R.array.privacy_setting, selectedPrivacy, new DialogInterface.OnClickListener() {
     	    public void onClick(DialogInterface dialog, int item) {
     	        selectedPrivacy = item;
     	    }
     	});
     	builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
             	switch (selectedPrivacy) {
 				case 0:
 					privacyPreference=Visibility.FRIENDS;
 					list_trip_options.set(3, new TripOption(R.drawable.trip_icon_fb, "Privacy", "Friends"));
 					adapter.notifyDataSetChanged();
 					break;
 				case 1:
 					privacyPreference=Visibility.FRIENDS_OF_FRIENDS;
 					list_trip_options.set(3, new TripOption(R.drawable.trip_icon_fb, "Privacy", "Friends of Friends"));
 					adapter.notifyDataSetChanged();
 					break;
 				case 2:
 					privacyPreference=Visibility.PUBLIC;
 					list_trip_options.set(3, new TripOption(R.drawable.trip_icon_fb, "Privacy", "Public"));
 					adapter.notifyDataSetChanged();
 					break;
 				default:
 					break;
 				}
             }
           
         });
         builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                  dialog.cancel();
             }
         });
     	AlertDialog alert = builder.create();
     	alert.show();
     }
     
     void setExtras(){
     	
    	final BitSet sExtras = tripPreferences.getExtras();
    	
     	AlertDialog.Builder builder = new AlertDialog.Builder(this);
     	builder.setTitle("Edit preferences");
     	
     	builder.setMultiChoiceItems(R.array.preferences_array, checkedExtrasFromUserPreferences, new DialogInterface.OnMultiChoiceClickListener() {
 			
 			@Override
 			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
 				sExtras.set(which,isChecked);
 			}
     	});
     	builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
             	String ex = "";
             	for(int i=0 ; i<sExtras.length() ; i++){
             		if(sExtras.get(i)){
             			ex=ex+items[i]+" ";
             		}
             	}
             	tripPreferences.setExtras(sExtras);
             	list_trip_options.set(4, new TripOption(R.drawable.trip_icon_plus, "Preferences", ex));
 				adapter.notifyDataSetChanged();
             }
         });
     	 builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int id) {
                   dialog.cancel();
              }
          });
     	AlertDialog alert = builder.create();
     	alert.show();
     }
 
     public void onNextClick(View v){
     	journey = new Journey(-1);
 		journey.setRoute(selectedRoute);
 		journey.setStart(dateAndTime);
 		journey.setVisibility(privacyPreference);
 		journey.setTripPreferences(tripPreferences);
 		getApp().setSelectedJourney(journey);
 		
 		Intent intent = new Intent(TripOptions.this, ShareOnFacebook.class);
 		startActivity(intent);
 		sendJourneyRequest();
 
 	}
     
     private void sendJourneyRequest(){
     	
 		JourneyRequest req = new JourneyRequest(RequestType.CREATE_JOURNEY, getApp().getUser(), journey);
 		
 		Response res = null;
 		try {
 			res = RequestTask.sendRequest(req,getApp());
 			if(res.getStatus() != ResponseStatus.OK){
 //				createAlertDialog(this, false,  "Journey","created","");
 				Toast.makeText(getApplicationContext(), "Trip created", Toast.LENGTH_SHORT).show();
 			}
 			else{
 				if(getApp().getJourneys() != null)
 					getApp().getJourneys().add(journey);
 				Toast.makeText(getApplicationContext(), "Trip created", Toast.LENGTH_SHORT).show();
 
 			}
 		} catch (ClientProtocolException e) {
 			Toast.makeText(getApplicationContext(), "Trip created", Toast.LENGTH_SHORT).show();
 
 		} catch (IOException e) {
 			Toast.makeText(getApplicationContext(), "Trip created", Toast.LENGTH_SHORT).show();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			e.printStackTrace();
 		}
 		
 		Request req2 = new PreferenceRequest(RequestType.CREATE_PREFERENCE,getApp().getUser(),tripPreferences);
 		try {
 			PreferenceResponse res2 = (PreferenceResponse) RequestTask.sendRequest(req2,getApp());
 			if(res2.getStatus() != ResponseStatus.OK){
 			}
 			else{
 //				if(getApp().g != null)
 //					getApp().getJourneys().add(jour);
 			}
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			e.printStackTrace();
 		}
 	}
     
     public String formatDate(Calendar c){
     	String formatedDate = c.get(Calendar.DAY_OF_MONTH)
 				+"/"+(c.get(Calendar.MONTH)+1)+"/"+c.get(Calendar.YEAR);
     	return formatedDate;
     }
     
     public String formatTime(Calendar c){
 		//This formats Calendar.MINUTE so minutes below 10 show a 0 before
     	Integer min = c.get(Calendar.MINUTE);
 		String minutes=min.toString();
 		if(min<10)
 			minutes="0"+minutes;
 		
 		String formatedTime = c.get(Calendar.HOUR_OF_DAY)+":"+minutes;
 		return formatedTime;
     }
 }
