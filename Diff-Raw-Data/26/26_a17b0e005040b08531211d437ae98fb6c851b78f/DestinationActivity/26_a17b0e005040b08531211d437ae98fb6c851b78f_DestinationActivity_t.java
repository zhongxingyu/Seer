 package org.routy;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.UUID;
 
 import junit.framework.Assert;
 
 import org.routy.fragment.OneButtonDialog;
 import org.routy.model.AppProperties;
 import org.routy.model.GooglePlace;
 import org.routy.model.GooglePlacesQuery;
 import org.routy.model.Route;
 import org.routy.model.RouteRequest;
 import org.routy.task.CalculateRouteTask;
 import org.routy.task.GooglePlacesQueryTask;
 import org.routy.view.DestinationRowView;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.location.Address;
 import android.media.AudioManager;
 import android.media.SoundPool;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class DestinationActivity extends FragmentActivity {
 	
 	
 	private final String TAG = "DestinationActivity";
 	
 	private FragmentActivity mContext;
 	private Address origin;
 	private LinearLayout destLayout;
   // shared prefs for destination persistence
   private SharedPreferences destinationActivityPrefs;
   
   private SoundPool sounds;
   private int bad;
   private int click;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
     sounds = new SoundPool(3, AudioManager.STREAM_MUSIC, 0); 
     
     bad = sounds.load(this, R.raw.routybad, 1);
     click = sounds.load(this, R.raw.routyclick, 1);
 		
 		setContentView(R.layout.activity_destination);
 		
 		mContext = this;
 		
 		// Get the layout containing the list of destination
 		destLayout = (LinearLayout) findViewById(R.id.LinearLayout_destinations);
 		
 		// Get the origin address passed from OriginActivity
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) {
 			origin = (Address) extras.get("origin");
 			Assert.assertNotNull(origin);
 			
 			Log.v(TAG, "Origin address: " + origin.getExtras().getString("formatted_address"));
 			TextView originText = (TextView) findViewById(R.id.textview_destinations_origin);
 			originText.setText(origin.getExtras().getString("formatted_address"));
 
 		}
 		
 		// Initialize shared preferences
 		destinationActivityPrefs = getSharedPreferences("destination_prefs", MODE_PRIVATE);
 		Set<String> storedAddresses = destinationActivityPrefs.getStringSet("saved_destination_strings", new TreeSet<String>());
 		
 		// If there were stored addresses, initialize destination views with them
 		if (storedAddresses.size() != 0){
 			Object[] addresses =  storedAddresses.toArray();
 			for (int i = 0; i < addresses.length; i++){
 				DestinationRowView newRow = addDestinationRow((String)addresses[i]);
 				
 				if (newRow != null && i != addresses.length - 1) {
 					newRow.hideAddButton();
 				}
 			}
 		}
 		// Otherwise initialize the screen with one empty destination
 		else{
 			addDestinationRow();
 		}
 		
 		// XXX temp "Test defaults"
         Button buttonTestDefaults = (Button) findViewById(R.id.button_test_defaults);
         buttonTestDefaults.setText("Test Default Destinations");
         buttonTestDefaults.setOnClickListener(listenerTestDefaults);
 	}
 	
 	
 	/**
 	 * Adds a {@link DestinationRowView} to the Destinations list.
 	 * @param address
 	 * @return			the row that was added, or null if no row was added
 	 */
 	DestinationRowView addDestinationRow() {
 		return addDestinationRow("");
 	}
 	
 	
 	/**
 	 * Adds a {@link DestinationRowView} to the Destinations list.
 	 * @param address
 	 * @return			the row that was added, or null if no row was added
 	 */
 	DestinationRowView addDestinationRow(String address) {
 		if (destLayout.getChildCount() < AppProperties.NUM_MAX_DESTINATIONS) {
 			DestinationRowView v = new DestinationRowView(mContext, address) {
 
 				@Override
 				public void onRemoveClicked(UUID id) {
 					removeDestinationRow(id);
 				}
 
 				// The "+" button was clicked on a destination row
 				@Override
 				public void onAddClicked(UUID id) {
 				  sounds.play(click, 1, 1, 1, 0, 1);
 					// Do validation and then display the additional row
 					final DestinationRowView row = getRowById(id);
 					if (row.getAddressString() != null && row.getAddressString().length() > 0) {
 						
 						// If this row was invalid or hasn't been validated, do validation.
 						if (row.getStatus() == DestinationRowView.INVALID || row.getStatus() == DestinationRowView.NOT_VALIDATED) {
 							new GooglePlacesQueryTask(mContext) {
 								
 								@Override
 								public void onResult(GooglePlace place) {
 									if (place != null && place.getAddress() != null) {
 										row.setAddress(place.getAddress());
 										row.setValid();
 										addDestinationRow();
 									} else {
 									  sounds.play(bad, 1, 1, 1, 0, 1);
 										row.setInvalid();
 										addDestinationRow();		// TODO Show another row if the last one was invalid??
 									}
 								}
 
 								@Override
 								public void onNoSelection() {
 									// The user dismissed the place picker dialog without making a selection (e.g. tapped outside the dialog box)
 									Log.v(TAG, "no selection made");
 									row.clearValidationStatus();
 									row.showAddButton();
 								}
 							}.execute(new GooglePlacesQuery(row.getAddressString(), origin.getLatitude(), origin.getLongitude()));
 						} else {
 							addDestinationRow();
 						}
 					} else {
 						Log.v(TAG, "attempted to add an empty row...ignoring that request");
 						row.reset();
 					}
 				}
 
 				// The user tapped on a different row and this row lost focus
 				@Override
 				public void onFocusLost(final UUID id) {
 					// TODO Do validation and SHOW A LOADING SPINNER while working
 					final DestinationRowView row = getRowById(id);
 					
 					Log.v(TAG, "FOCUS LOST row id=" + row.getUUID() + ": " + row.getAddressString() + " valid status=" + row.getStatus());
 					
 					// The user tapped on a different text box.  Do validation if necessary, but don't add an additional row.
 					if (row.getAddressString() != null && row.getAddressString().length() > 0) {
 						if (row.getStatus() == DestinationRowView.INVALID || row.getStatus() == DestinationRowView.NOT_VALIDATED) {
 							new GooglePlacesQueryTask(mContext) {
 								
 								@Override
 								public void onResult(GooglePlace place) {
 									if (place != null && place.getAddress() != null) {
 										row.setAddress(place.getAddress());
 										row.setValid();
 										
										if ((getRowIndexById(id) == destLayout.getChildCount() - 1) && (destLayout.getChildCount() < AppProperties.NUM_MAX_DESTINATIONS - 1)) {
											Log.v(TAG, "place validated...showing add button");
 											row.showAddButton();
										} else {
											Log.v(TAG, "place validated...hiding add button");
											row.hideAddButton();
 										}
 									} else {
 										row.setInvalid();
 									}
 								}
 
 								@Override
 								public void onNoSelection() {
 									// TODO Auto-generated method stub
 									// Doing nothing leaves it NOT_VALIDATED
 								}
 							}.execute(new GooglePlacesQuery(row.getAddressString(), origin.getLatitude(), origin.getLongitude()));
 						}
 					}
 				}
 			};
 			
			if (destLayout.getChildCount() == AppProperties.NUM_MAX_DESTINATIONS - 1) {
				v.hideAddButton();
			}
			
 			destLayout.addView(v, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
 			return v;
 		} else {
 		  sounds.play(bad, 1, 1, 1, 0, 1);
 			showErrorDialog("Routy is all maxed out at " + AppProperties.NUM_MAX_DESTINATIONS + " destinations for now.");
 			((DestinationRowView) destLayout.getChildAt(destLayout.getChildCount() - 1)).showAddButton();
 			return null;
 		}
 	}
 	
 	
 	/**
 	 * Removes the EditText and "remove" button row ({@link DestinationRowView}} with the given {@link UUID} from the screen.
 	 * @param id
 	 */
 	void removeDestinationRow(UUID id) {
 	  sounds.play(click, 1, 1, 1, 0, 1);
 		if (destLayout.getChildCount() > 1) {
 			int idx = getRowIndexById(id);
 			
 			if (idx >= 0) {
 				Log.v(TAG, "Remove DestinationAddView id=" + id);
 				destLayout.removeViewAt(idx);
 				
 				// If we removed the last row, re-enable the "+" button on the NEW last row
 				if (idx >= destLayout.getChildCount()) {
 					((DestinationRowView) destLayout.getChildAt(destLayout.getChildCount() - 1)).showAddButton();
 				}
 			} else {
 				Log.e(TAG, "attempt to remove a row that does not exist -- id=" + id);
 			}
 		} else if (destLayout.getChildCount() == 1) {
 			int idx = getRowIndexById(id);
 			
 			((DestinationRowView) destLayout.getChildAt(idx)).reset();
 		}
 	}
 	
 	
 	/**
 	 * Adds a {@link DestinationRowView} row to the list if we're not maxed out.  Max number 
 	 * of rows is set in {@link AppProperties}.
 	 * @param v
 	 */
 	/*public void onClickFromDestinationAdd(View v) {
 		Log.v(TAG, "Add another destination.");
 		
 		if (destLayout.getChildCount() < AppProperties.NUM_MAX_DESTINATIONS) {
 			addDestinationRow();
 		} else {
 			showErrorDialog("Routy is all maxed out at " + AppProperties.NUM_MAX_DESTINATIONS + " destinations for now.");
 		}
 	}*/
 	
 	
 	/**
 	 * Called when "Route It!" is clicked.  Does any final validation and preparations before calculating 
 	 * the best route and passing route data to the results activity.
 	 * 
 	 * @param v
 	 */
 	public void acceptDestinations(final View v) {
 		Log.v(TAG, "Validate destinations and calculate route if they're good.");
 		sounds.play(click, 1, 1, 1, 0, 1);
 		
		// XXX This is only printing stuff out for debugging
		/*for (int i = 0; i < destLayout.getChildCount(); i++) {
 			DestinationRowView row = (DestinationRowView) destLayout.getChildAt(i);
 			
 			if (row.getAddressString() != null && row.getAddressString().length() > 0) {
 				Log.v(TAG, "Destination " + i + ": " + (row.getStatus() == DestinationRowView.VALID));
 			}
		}*/
 		
 		// Go through the list until you find one that is INVALID or NOT_VALIDATED
 		List<Address> validAddresses = new ArrayList<Address>();
 		boolean hasError = false;
 		DestinationRowView row = null;
 		for (int i = 0; i < destLayout.getChildCount(); i++) {
 			row = (DestinationRowView) destLayout.getChildAt(i);
 			
 			if (row.getAddressString() != null && row.getAddressString().length() > 0) {
 				if (row.getStatus() == DestinationRowView.INVALID || row.getStatus() == DestinationRowView.NOT_VALIDATED) {
 					hasError = true;
 					Log.v(TAG, "row id=" + row.getId() + " has valid status=" + row.getStatus());
 					break;
 				} else {
 					validAddresses.add(row.getAddress());
 				}
 			}
 		}
 		
 		// If you encountered an "error" take steps to validate it.  When it's done validating, call acceptDestinations again.
 		if (hasError && row != null) {
 			Log.v(TAG, "The destinations list has a row (id=" + row.getUUID() + ") in need of validation.");
 			// Validate "row"
 			final DestinationRowView r = row;
 			new GooglePlacesQueryTask(mContext) {
 				
 				@Override
 				public void onResult(GooglePlace place) {
 					if (place != null && place.getAddress() != null) {
 						r.setAddress(place.getAddress());
 						r.setValid();
 						
						if ((getRowIndexById(r.getUUID()) == destLayout.getChildCount() - 1) && (destLayout.getChildCount() < AppProperties.NUM_MAX_DESTINATIONS - 1)) {
 							r.showAddButton();
 						}
 						
 						acceptDestinations(v);
 					} else {
 						r.setInvalid();
 					}
 				}
 
 				@Override
 				public void onNoSelection() {
 					// TODO Auto-generated method stub
 					// Doing nothing leaves it NOT_VALIDATED
 				}
 			}.execute(new GooglePlacesQuery(row.getAddressString(), origin.getLatitude(), origin.getLongitude()));
 		} else {
 			Log.v(TAG, "All destinations have been validated.");
 			
 			// If everything is valid, move on to the Results screen
 			new CalculateRouteTask() {
 				
 				@Override
 				public void onRouteCalculated(Route route) {
 					// Call ResultsActivity activity
 	    			Intent resultsIntent = new Intent(getBaseContext(), ResultsActivity.class);
 	    			resultsIntent.putExtra("addresses", route.getAddresses());
 	    			resultsIntent.putExtra("distance", route.getTotalDistance());
 	    			startActivity(resultsIntent);
 				}
 			}.execute(new RouteRequest(origin, validAddresses, false));
 		}
 		
 	}
 	
 	
 	public void changeOrigin(View v) {
 		finish();
 	}
 	
 	
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_destination, menu);
         return true;
     }
 	
 	/**
 	 * In the case that a user presses back to change an origin, or any other reason why they leave the destination screen,
 	 * we save their entered destinations to shared prefs so they don't have to re-enter the destinations again
 	 */
 	@Override
 	public void onPause(){
 		super.onPause();
 		
     if(sounds != null) { 
       sounds.release(); 
       sounds = null; 
     } 
 		
 		// http://stackoverflow.com/questions/1463284/hashset-vs-treeset
 		Set<String> storedAddresses = new TreeSet<String>();
 		// iterate through every destinationInputView
 		for (int i = 0; i < destLayout.getChildCount(); i++) {
 			DestinationRowView destView = (DestinationRowView) destLayout.getChildAt(i);
 			// Add the text in the EditText of the destination to storedAddresses
 			if (destView.getAddressString() != null && destView.getAddressString().length() > 0) {
 				storedAddresses.add(destView.getAddressString());
 			}
 		}
 		// Put the storedAddresses into shared prefs via a set of strings
 		Log.v(TAG, "Saving destinations in shared prefs");
 		SharedPreferences.Editor ed = destinationActivityPrefs.edit();
 		ed.putStringSet("saved_destination_strings", storedAddresses);
 		ed.commit();
 	}
 
 	
 	/**
      * Displays an {@link AlertDialog} with one button that dismisses the dialog.  Use this to display error messages 
      * to the user.
      * 
      * @param message
      */
     private void showErrorDialog(String message) {
     	OneButtonDialog dialog = new OneButtonDialog(getResources().getString(R.string.error_message_title), message) {
 			@Override
 			public void onButtonClicked(DialogInterface dialog, int which) {
 				dialog.dismiss();
 			}
 		};
 		dialog.show(mContext.getSupportFragmentManager(), TAG);
     }
     
     
     private int getRowIndexById(UUID id) {
     	for (int i = 0; i < destLayout.getChildCount(); i++) {
 			if (((DestinationRowView) destLayout.getChildAt(i)).getUUID().equals(id)) {
 				return i;
 			}
 		}
     	
     	return -1;
     }
     
     
     private DestinationRowView getRowById(UUID id) {
     	int idx = getRowIndexById(id);
     	return (DestinationRowView) destLayout.getChildAt(idx);
     }
     
     @Override
     protected void onResume() {   
        super.onResume(); 
        
        sounds = new SoundPool(3, AudioManager.STREAM_MUSIC, 0); 
        
        bad = sounds.load(this, R.raw.routybad, 1);
        click = sounds.load(this, R.raw.routyclick, 1);
      }
 
 	// XXX temp
 	/**
 	 * Loads the 3 test destinations we've been using.
 	 */
 	View.OnClickListener listenerTestDefaults = new View.OnClickListener() {
 	    @Override
       public void onClick(View v) {
 	      sounds.play(click, 1, 1, 1, 0, 1);
 	    	if (destLayout.getChildCount() < 3) {
 	    		for (int i = destLayout.getChildCount(); i < 3; i++) {
 	    			addDestinationRow();
 	    		}
 	    	}
 	    	
 	    	DestinationRowView view = (DestinationRowView) destLayout.getChildAt(0);
 	    	((EditText) view.findViewById(R.id.edittext_destination_add)).setText(getResources().getString(R.string.test_destination_1));
 	    	
 	    	view = (DestinationRowView) destLayout.getChildAt(1);
 	    	((EditText) view.findViewById(R.id.edittext_destination_add)).setText(getResources().getString(R.string.test_destination_2));
 	    	
 	    	view = (DestinationRowView) destLayout.getChildAt(2);
 	    	((EditText) view.findViewById(R.id.edittext_destination_add)).setText(getResources().getString(R.string.test_destination_3));
 	    }
 	};
 
 }
