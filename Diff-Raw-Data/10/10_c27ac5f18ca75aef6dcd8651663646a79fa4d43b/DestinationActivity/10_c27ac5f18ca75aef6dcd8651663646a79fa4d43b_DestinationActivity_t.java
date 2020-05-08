 package org.routy;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 import java.util.UUID;
 
 import junit.framework.Assert;
 
 import org.routy.fragment.OneButtonDialog;
 import org.routy.model.AppProperties;
 import org.routy.model.GooglePlace;
 import org.routy.model.GooglePlacesQuery;
 import org.routy.model.Route;
 import org.routy.model.RouteOptimizePreference;
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
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.Switch;
 import android.widget.TextView;
 
 public class DestinationActivity extends FragmentActivity {
 
 
 	private final String TAG = "DestinationActivity";
 	private final String SAVED_DESTS_JSON_KEY = "saved_destination_json";
 
 	private FragmentActivity mContext;
 	private Address origin;
 	private LinearLayout destLayout;
 	private Button addDestButton;
 	private Switch preferenceSwitch;
 
 	// shared prefs for destination persistence
 	private SharedPreferences destinationActivityPrefs;
 
 	private SoundPool sounds;
 	private int bad;
 	private int click;
 	private AudioManager audioManager;
 	float volume;
 	RouteOptimizePreference routeOptimized;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
 		volume = (float) audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
 
 		sounds = new SoundPool(3, AudioManager.STREAM_MUSIC, 0); 
 
 		bad = sounds.load(this, R.raw.routybad, 1);
 		click = sounds.load(this, R.raw.routyclick, 1);
 
 		setContentView(R.layout.activity_destination);
 
 		mContext = this;
 
 		addDestButton = (Button) findViewById(R.id.button_destination_add_new);
 
 		// Get the layout containing the list of destination
 		destLayout = (LinearLayout) findViewById(R.id.LinearLayout_destinations);
 
 		routeOptimized = RouteOptimizePreference.PREFER_DISTANCE;
 		preferenceSwitch = (Switch) findViewById(R.id.toggleDistDur);
 		preferenceSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 				onToggleClicked(isChecked);
 			}
 		});
 
 
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
 		String storedAddressesJson = destinationActivityPrefs.getString(SAVED_DESTS_JSON_KEY, null);
 
 		if (storedAddressesJson != null && storedAddressesJson.length() > 0) {
 			List<Address> restoredAddresses = Util.jsonToAddressList(storedAddressesJson);
 
 			for (int i = 0; i < restoredAddresses.size(); i++) {
 				Address address = restoredAddresses.get(i);
 				Bundle addressExtras = address.getExtras();
 
 				if (addressExtras != null) {
 					int status = addressExtras.getInt("valid_status");
 
 					DestinationRowView newRow = null;
 					if (status == DestinationRowView.VALID) {
 						// Put the new row in the list
 						newRow = addDestinationRow(address.getFeatureName());
 						newRow.setAddress(address);
 						newRow.setValid();
 
 						Log.v(TAG, "restored: " + newRow.getAddress().getFeatureName() + " [status=" + newRow.getStatus() + "]");
 
 					} else if (status == DestinationRowView.INVALID || status == DestinationRowView.NOT_VALIDATED) {
 						String addressString = addressExtras.getString("address_string");
 
 						newRow = addDestinationRow(addressString);
 
 						if (status == DestinationRowView.INVALID) {
 							newRow.setInvalid();
 						} else {
 							newRow.clearValidationStatus();
 						}
 
 						Log.v(TAG, "restored: " + newRow.getAddressString() + " [status=" + newRow.getStatus() + "]");
 					}
 
 				}
 			}
 		} else {
 			addDestinationRow();
 		}
 
 		// XXX temp "Test defaults"
 		Button buttonTestDefaults = (Button) findViewById(R.id.button_test_defaults);
 		buttonTestDefaults.setText("Test Default Destinations");
 		buttonTestDefaults.setOnClickListener(listenerTestDefaults);
 
 
 		// TODO: for testing purposes. Remove before prod.
 		showNoobDialog();
 		// First-time user dialog cookie
 		boolean noobCookie = destinationActivityPrefs.getBoolean("noob_cookie", false);
 		if (!noobCookie){
 			showNoobDialog();
 			userAintANoob();
 		}
 
 	}
 
 
 	/**
 	 * Displays an {@link AlertDialog} with one button that dismisses the dialog. Dialog displays helpful first-time info.
 	 * 
 	 * @param message
 	 */
 	private void showNoobDialog() {
 		OneButtonDialog dialog = new OneButtonDialog(getResources().getString(R.string.destination_noob_title), getResources().getString(R.string.destination_noob_instructions)) {
 			@Override
 			public void onButtonClicked(DialogInterface dialog, int which) {
 				dialog.dismiss();
 			}
 		};
 		dialog.show(mContext.getSupportFragmentManager(), TAG);
 	}
 
 	/**
 	 *  If the user sees the first-time instruction dialog, they won't see it again next time.
 	 */
 	private void userAintANoob() {
 		SharedPreferences.Editor ed = destinationActivityPrefs.edit();
 		ed.putBoolean("noob_cookie", true);
 		ed.commit();	
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
 											showAddButton();
 										} else {
 											Log.v(TAG, "place validated...hiding add button");
 											hideAddButton();
 										}
 									} else {
 										row.setInvalid();
 									}
 								}
 
 								@Override
 								public void onNoSelection() {
 									// Doing nothing leaves it NOT_VALIDATED
 								}
 							}.execute(new GooglePlacesQuery(row.getAddressString(), origin.getLatitude(), origin.getLongitude()));
 						}
 					}
 				}
 			};
 
 			destLayout.addView(v, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
 			v.focusOnAddressField();
 
 			if (destLayout.getChildCount() == AppProperties.NUM_MAX_DESTINATIONS) {
 				addDestButton.setVisibility(View.INVISIBLE);
 			}
 
 			return v;
 		} else {
 			volume = (float) audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
 			volume = volume / audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
 			sounds.play(bad, 1, 1, 1, 0, 1);
 
 			showErrorDialog("Routy is all maxed out at " + AppProperties.NUM_MAX_DESTINATIONS + " destinations for now.");
 
 			return null;
 		}
 	}
 
 
 	/**
 	 * Removes the EditText and "remove" button row ({@link DestinationRowView}} with the given {@link UUID} from the screen.
 	 * @param id
 	 */
 	void removeDestinationRow(UUID id) {
 		volume = (float) audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
 		volume = volume / audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
 		sounds.play(click, volume, volume, 1, 0, 1);
 
 		int idx = 0;
 		if (destLayout.getChildCount() > 1) {
 			idx = getRowIndexById(id);
 
 			if (idx >= 0) {
 				Log.v(TAG, "Remove DestinationAddView id=" + id);
 				destLayout.removeViewAt(idx);
 			} else {
 				Log.e(TAG, "attempt to remove a row that does not exist -- id=" + id);
 			}
 		} else if (destLayout.getChildCount() == 1) {
 			idx = getRowIndexById(id);		// Should be 0 all the time
 
 			((DestinationRowView) destLayout.getChildAt(idx)).reset();
 		}
 
 		// set focus on the row idx one less than idx
 		((DestinationRowView) destLayout.getChildAt(Math.max(0, idx - 1))).focusOnAddressField();
 
 		if (destLayout.getChildCount() < AppProperties.NUM_MAX_DESTINATIONS) {
 			addDestButton.setVisibility(View.VISIBLE);
 		}
 	}
 
 
 	/**
 	 * Called when "Route It!" is clicked.  Does any final validation and preparations before calculating 
 	 * the best route and passing route data to the results activity.
 	 * 
 	 * @param v
 	 */
 	public void acceptDestinations(final View v) {
 		Log.v(TAG, "Validate destinations and calculate route if they're good.");
 
 		volume = (float) audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
 		volume = volume / audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
 		sounds.play(click, volume, volume, 1, 0, 1);
 
 		// Go through the list until you find one that is INVALID or NOT_VALIDATED
 		List<Address> validAddresses = new ArrayList<Address>();
 		boolean hasDestinations = false;
 		boolean hasError = false;
 		DestinationRowView row = null;
 		for (int i = 0; i < destLayout.getChildCount(); i++) {
 			row = (DestinationRowView) destLayout.getChildAt(i);
 
 			if (row.getAddressString() != null && row.getAddressString().length() > 0) {
 				if (row.getStatus() == DestinationRowView.INVALID || row.getStatus() == DestinationRowView.NOT_VALIDATED) {
					hasDestinations = true;
 					hasError = true;
					Log.v(TAG, "row id=" + row.getUUID() + " has valid status=" + row.getStatus());
 					break;
 				} else {
 					hasDestinations = true;
 					validAddresses.add(row.getAddress());
 				}
 			}
 		}
 
 		if (hasDestinations) {
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
 
 							acceptDestinations(v);
 						} else {
 							r.setInvalid();
 
 							// TODO Show an error message: couldn't match the query string to a place or address
 						}
 					}
 
 					@Override
 					public void onNoSelection() {
 						// Doing nothing leaves it NOT_VALIDATED
 					}
 				}.execute(new GooglePlacesQuery(row.getAddressString(), origin.getLatitude(), origin.getLongitude()));
 			} else {
 				Log.v(TAG, "All destinations have been validated.");
 
 				// If everything is valid, move on to the Results screen
 				new CalculateRouteTask(this) {
 
 					@Override
 					public void onRouteCalculated(Route route) {
 						// Call ResultsActivity activity
 						Intent resultsIntent = new Intent(getBaseContext(), ResultsActivity.class);
 						resultsIntent.putExtra("addresses", route.getAddresses());
 						resultsIntent.putExtra("distance", route.getTotalDistance());
 						resultsIntent.putExtra("optimize_for", routeOptimized);
 						startActivity(resultsIntent);
 					}
 				}.execute(new RouteRequest(origin, validAddresses, false, 
 						routeOptimized/* ? RouteOptimizePreference.PREFER_DISTANCE : RouteOptimizePreference.PREFER_DURATION*/));
 			}
 		} else {
 			// No destinations entered
 			volume = (float) audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
 			volume = volume / audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
 			sounds.play(bad, volume, volume, 1, 0, 1);
 			showErrorDialog("Please enter at least one destination to continue.");
 		}
 
 	}
 
 
 	public void changeOrigin(View v) {
 		finish();
 	}
 
 
 	public void onAddDestinationClicked(View v) {
 		Log.v(TAG, "new destination row requested by user");
 		volume = (float) audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
 		volume = volume / audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
 		sounds.play(click, volume, volume, 1, 0, 1);
 
 		// If the last row is not empty, add a new row
 		DestinationRowView lastRow = (DestinationRowView) destLayout.getChildAt(destLayout.getChildCount() - 1);
 		if (lastRow.getAddressString() != null && lastRow.getAddressString().length() > 0) {
 			if (lastRow.needsValidation()) {
 				// Validate the last row if it has not been validated.  Otherwise, it puts the new row up first, and then validates due to focusChanged.
 				Log.v(TAG, "validating last row before adding new one");
 				final DestinationRowView r = lastRow;
 
 				// disable the onFocusLost listener just once so it doesn't try to validate twice here
 				r.disableOnFocusLostCallback(true);
 
 				// do the validation
 				new GooglePlacesQueryTask(mContext) {
 
 					@Override
 					public void onResult(GooglePlace place) {
 						if (place != null && place.getAddress() != null) {
 							r.setAddress(place.getAddress());
 							r.setValid();
 
 							Log.v(TAG, "adding a new destination row");
 							addDestinationRow();
 						} else {
 							r.setInvalid();
 						}
 
 						// If the list is full, hide the add button
 						if (destLayout.getChildCount() == AppProperties.NUM_MAX_DESTINATIONS) {
 							addDestButton.setVisibility(View.INVISIBLE);
 						}
 					}
 
 					@Override
 					public void onNoSelection() {
 						// Doing nothing leaves it NOT_VALIDATED
 					}
 				}.execute(new GooglePlacesQuery(lastRow.getAddressString(), origin.getLatitude(), origin.getLongitude()));
 			} else {
 				Log.v(TAG, "adding a new destination row");
 				addDestinationRow();
 			}
 		}
 
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
 
 		Log.v(TAG, "building the JSON string from all the destination addresses");
 		List<Address> addressesToSave = new ArrayList<Address>();
 		for (int i = 0; i < destLayout.getChildCount(); i++) {
 			DestinationRowView destView = (DestinationRowView) destLayout.getChildAt(i);
 
 			if (destView.getAddressString() != null && destView.getAddressString().length() > 0) {
 				Address address = null;
 				if (destView.getStatus() == DestinationRowView.VALID) {
 					// Add the address from this row to the list
 					address = destView.getAddress();
 
 					// It's valid so we'll just keep track of that
 					Bundle extras = address.getExtras();
 					extras.putInt("valid_status", destView.getStatus());
 				} else if (destView.getStatus() == DestinationRowView.INVALID || destView.getStatus() == DestinationRowView.NOT_VALIDATED) {
 					// Manually create an Address object with just the EditText value and the status because it won't be there
 					address = new Address(Locale.getDefault());
 
 					// Since it'll need to be validated when we come back, we need to save what was in the EditText with the status
 					Bundle extras = address.getExtras();
 					extras.putString("address_string", destView.getAddressString());
 					extras.putInt("valid_status", destView.getStatus());
 				} else {
 					// bah!
 					throw new IllegalStateException("Destination row " + i + " had an invalid status value of: " + destView.getStatus());
 				}
 
 				Assert.assertNotNull(address);
 				addressesToSave.add(address);
 			}
 
 		}
 
 		String json = Util.addressListToJSON(addressesToSave);
 		Log.v(TAG, "saved destinations json: " + json);
 
 		// Put the storedAddresses into shared prefs via a set of strings
 		Log.v(TAG, "Saving destinations in shared prefs");
 		SharedPreferences.Editor ed = destinationActivityPrefs.edit();
 		ed.putString(SAVED_DESTS_JSON_KEY, json);
 		//		ed.putStringSet("saved_destination_strings", storedAddresses);
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
 
 
 	public void onToggleClicked(boolean on) {
 		Log.v(TAG, "route optimize preference changed!");
 		if (on) {
 			routeOptimized = RouteOptimizePreference.PREFER_DURATION;
 		} 
 		else {
 			routeOptimized = RouteOptimizePreference.PREFER_DISTANCE;
 		}
 	}
 
 
 	public void showAddButton() {
 		addDestButton.setVisibility(View.VISIBLE);
 	}
 
 	public void hideAddButton() {
 		addDestButton.setVisibility(View.INVISIBLE);
 	}
 
 
 	// XXX temp
 	/**
 	 * Loads the 3 test destinations we've been using.
 	 */
 	View.OnClickListener listenerTestDefaults = new View.OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			volume = (float) audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
 			volume = volume / audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
 			sounds.play(click, volume, volume, 1, 0, 1);
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
