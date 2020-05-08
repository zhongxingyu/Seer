 package nl.q42.hue2.activities;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import nl.q42.hue.dialogs.ErrorDialog;
 import nl.q42.hue.dialogs.ErrorDialog.ErrorDialogCallback;
 import nl.q42.hue2.PHUtilitiesImpl;
 import nl.q42.hue2.R;
 import nl.q42.hue2.Util;
 import nl.q42.hue2.models.Bridge;
 import nl.q42.hue2.views.FeedbackSwitch;
 import nl.q42.javahueapi.HueService;
 import nl.q42.javahueapi.models.Light;
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.AsyncTask;
 import android.os.Bundle;
import android.util.Log;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 public class LightsActivity extends Activity {
 	// It takes extremely long for the server to update its data, so this interval is reasonable
 	private final static long REFRESH_INTERVAL = 5000;
 	
 	private Bridge bridge;
 	private HueService service;
 	
 	private HashMap<String, Light> lights = new HashMap<String, Light>();
 	private HashMap<String, View> lightViews = new HashMap<String, View>();
 	
 	private LinearLayout resultContainer;
 	private LinearLayout resultList;
 	private ImageButton refreshButton;
 	private ProgressBar loadingSpinner;
 	
 	private Timer refreshTimer = new Timer();
 	
 	// TODO: Fix state preservation/switch bug
 	// Reproduce: Turn a light on, rotate the screen and notice that it's off again
 	// Looks like setChecked is still intervening
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_lights);
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 		
 		// Set up loading UI elements		
 		ActionBar ab = getActionBar();
 		ab.setCustomView(R.layout.loader);
 		ab.setDisplayShowCustomEnabled(true);
 		
 		RelativeLayout loadingLayout = (RelativeLayout) ab.getCustomView();
 		
 		loadingSpinner = (ProgressBar) loadingLayout.findViewById(R.id.loader_spinner);
 		refreshButton = (ImageButton) loadingLayout.findViewById(R.id.loader_refresh);
 		
 		resultContainer = (LinearLayout) findViewById(R.id.lights_result_container);
 		resultList = (LinearLayout) findViewById(R.id.lights_list);
 		
 		setEventHandlers();
 		
 		// Set up from bridge info
 		bridge = (Bridge) getIntent().getSerializableExtra("bridge");
 		service = new HueService(bridge.getIp(), Util.getDeviceIdentifier(this));
 		
 		// Save bridge to reconnect later
 		Util.setLastBridge(this, bridge);
 		
 		setTitle(bridge.getName());
 		
 		// Loading lights
 		if (savedInstanceState == null) {
 			refreshState(true);
 		} else {
 			lights = (HashMap<String, Light>) savedInstanceState.getSerializable("lights");
 			populateList();
 		}
 	}
 	
 	@Override
 	protected void onSaveInstanceState(Bundle state) {
 		super.onSaveInstanceState(state);
 		
 		state.putSerializable("lights", lights);
 	}
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 		refreshTimer.cancel();
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		startRefreshTimer();
 	}
 	
 	private void startRefreshTimer() {
 		refreshTimer = new Timer();
 		refreshTimer.scheduleAtFixedRate(new TimerTask() {
 			@Override
 			public void run() {
 				resultContainer.post(new Runnable() {
 					@Override
 					public void run() {
 						refreshState(false);
 					}
 				});
 			}
 		}, REFRESH_INTERVAL, REFRESH_INTERVAL);
 	}
 	
 	private void setEventHandlers() {
 		refreshButton.setOnClickListener(new OnClickListener() {			
 			@Override
 			public void onClick(View v) {
 				refreshState(true);
 			}
 		});
 		
 		// All lights pseudo group
 		final FeedbackSwitch switchAll = (FeedbackSwitch) findViewById(R.id.lights_all_switch);
 		switchAll.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			@Override
 			public void onCheckedChanged(CompoundButton view, final boolean checked) {
 				new AsyncTask<Void, Void, Boolean>() {
 					@Override
 					protected void onPreExecute() {
 						switchAll.setEnabled(false);
 					}
 					
 					@Override
 					protected Boolean doInBackground(Void... params) {
 						try {
 							service.turnAllOn(checked);
 							return true;
 						} catch (Exception e) {
 							return false;
 						}
 					}
 					
 					@Override
 					protected void onPostExecute(Boolean result) {
 						switchAll.setEnabled(true);
 						
 						if (result) {
 							ViewGroup lightViews = (ViewGroup) findViewById(R.id.lights_list);
 							
 							for (int i = 0; i < lightViews.getChildCount(); i++) {
 								((FeedbackSwitch) lightViews.getChildAt(i).findViewById(R.id.lights_light_switch)).setCheckedCode(checked);
 							}
 						} else {
 							// Revert switch
 							switchAll.setCheckedCode(!checked);
 							
 							ErrorDialog.showNetworkError(getFragmentManager());
 						}
 					}
 				}.execute();
 			}
 		});
 	}
 	
 	/**
 	 * Enable/disable all switches (use while executing actions or refreshing state)
 	 */
 	private Timer indicatorTimer = new Timer();
 	private void setActivityIndicator(boolean enabled, boolean forced) {		
 		if (enabled) {
 			// Tasks shorter than 300 ms don't warrant a visual loading indicator
 			indicatorTimer = new Timer();
 			indicatorTimer.schedule(new TimerTask() {
 				@Override
 				public void run() {
 					refreshButton.post(new Runnable() {
 						@Override
 						public void run() {
 							refreshButton.setVisibility(View.GONE);
 							loadingSpinner.setVisibility(View.VISIBLE);
 						}
 					});
 				}
 			}, forced ? 0 : 300);
 		} else {
 			indicatorTimer.cancel();
 			
 			refreshButton.setVisibility(View.VISIBLE);
 			loadingSpinner.setVisibility(View.GONE);
 		}
 	}
 	
 	/**
 	 * Reflect local lights state in UI
 	 */
 	private void refreshViews() {
 		for (String key : lightViews.keySet()) {
 			View view = lightViews.get(key);
 			Light light = lights.get(key);
 			
 			((TextView) view.findViewById(R.id.lights_light_name)).setText(light.name);
 			
 			// Set background of light icon to light color
 			final View colorView = view.findViewById(R.id.lights_light_color);
 			colorView.setBackgroundColor(getRGBColor(light));
 			
 			// Set switch
 			((FeedbackSwitch) view.findViewById(R.id.lights_light_switch)).setCheckedCode(light.state.on);
 		}
 	}
 	
 	/**
 	 * Download fresh copy of light state from bridge
 	 */
 	private void refreshState(final boolean flush) {		
 		new AsyncTask<Void, Void, Boolean>() {
 			@Override
 			protected void onPreExecute() {
 				// Empty state
 				if (flush) {
 					lights.clear();
 					lightViews.clear();
 					resultList.removeAllViews();
 					
 					resultContainer.setVisibility(View.INVISIBLE);
 					
 					setActivityIndicator(true, true);
 				}
 			}
 			
 			@Override
 			protected Boolean doInBackground(Void... params) {
 				try {
 					// getLights() returns no state info
 					lights = new HashMap<String, Light>(service.getFullConfig().lights);
 					return true;
 				} catch (Exception e) {
 					return false;
 				}
 			}
 
 			@Override
 			protected void onPostExecute(Boolean success) {
 				if (success) {
 					if (flush) {
 						populateList();
 						resultContainer.setVisibility(View.VISIBLE);
 					} else {
 						refreshViews();
 					}
 				} else if (flush) {
 					// Being able to retrieve the light list is critical, so if this fails we go back to the bridge selection activity
 					ErrorDialog.show(getFragmentManager(), R.string.dialog_connection_title, R.string.dialog_network_error, new ErrorDialogCallback() {
 						@Override
 						public void onClose() {
 							Util.setLastBridge(LightsActivity.this, null);
 							
 							Intent searchIntent = new Intent(LightsActivity.this, LinkActivity.class);
 							searchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 							startActivity(searchIntent);
 						}
 					});
 				}
 				
 				setActivityIndicator(false, true);
 			}
 		}.execute();
 	}
 	
 	private void populateList() {
 		ViewGroup container = (ViewGroup) findViewById(R.id.lights_list);
 		View lastView = null;
 		
 		// Sort lights by id
 		ArrayList<String> lightIds = new ArrayList<String>();
 		for (String id : lights.keySet()) {
 			lightIds.add(id);
 		}
 		Collections.sort(lightIds);
 		
 		for (final String id : lightIds) {
 			Light light = lights.get(id);
 			
 			// Create view
 			lastView = addLightView(container, id, light);
 			
 			// Associate view with light
 			lightViews.put(id, lastView);
 		}
 		
 		if (lastView != null) {
 			lastView.findViewById(R.id.lights_light_divider).setVisibility(View.INVISIBLE);
 		}
 		
 		// Populate UI with state
 		refreshViews();
 	}
 	
 	private int getRGBColor(Light light) {
 		if (!light.state.on) {
 			return Color.BLACK;
 		}
 		
 		// Convert HSV color to RGB
 		if (light.state.colormode.equals("hs")) {
 			float[] components = new float[] {
 				(float) light.state.hue / 65535.0f * 360.0f,
 				(float) light.state.sat / 255.0f,
 				1.0f // Ignore brightness for more clear color view, hue is most important anyway
 			};
 			
 			return Color.HSVToColor(components);
 		} else if (light.state.colormode.equals("xy")) {
 			float[] points = new float[] { (float) light.state.xy[0], (float) light.state.xy[1] };
 			return PHUtilitiesImpl.colorFromXY(points, light.modelid);
 		} else if (light.state.colormode.equals("ct")) {
 			return temperatureToColor(1000000 / light.state.ct);
 		} else {
 			return Color.WHITE;
 		}
 	}
 	
 	// Adapted from: http://www.tannerhelland.com/4435/convert-temperature-rgb-algorithm-code/
 	private int temperatureToColor(long tmpKelvin) {
 		double tmpCalc;
 		int r, g, b;
 		
 		// Temperature must fall between 1000 and 40000 degrees
 		tmpKelvin = Math.min(40000, Math.max(tmpKelvin, 1000));
 		
 		// All calculations require tmpKelvin / 100, so only do the conversion once
 		tmpKelvin /= 100;
 		
 		// Calculate each color in turn
 		
 		// First: red
 		if (tmpKelvin <= 66) {
 			r = 255;
 		} else {
 			// Note: the R-squared value for this approximation is .988
 			tmpCalc = tmpKelvin - 60;
 			tmpCalc = 329.698727446 * Math.pow(tmpCalc, -0.1332047592);
 			r = (int) tmpCalc;
 			r = Math.min(255, Math.max(r, 0));
 		}
 		
 		// Second: green
 		if (tmpKelvin <= 66) {
 			// Note: the R-squared value for this approximation is .996
 			tmpCalc = tmpKelvin;
 			tmpCalc = 99.4708025861 * Math.log(tmpCalc) - 161.1195681661;
 			g = (int) tmpCalc;
 			g = Math.min(255, Math.max(g, 0));
 		} else {
 			// Note: the R-squared value for this approximation is .987
 			tmpCalc = tmpKelvin - 60;
 			tmpCalc = 288.1221695283 * Math.pow(tmpCalc, -0.0755148492);
 			g = (int) tmpCalc;
 			g = Math.min(255, Math.max(g, 0));
 		}
 		
 		// Third: blue
 		if (tmpKelvin >= 66) {
 			b = 255;
 		} else if (tmpKelvin <= 19) {
 			b = 0;
 		} else {
 			// Note: the R-squared value for this approximation is .998
 			tmpCalc = tmpKelvin - 10;
 			tmpCalc = 138.5177312231 * Math.log(tmpCalc) - 305.0447927307;
 			b = (int) tmpCalc;
 			b = Math.min(255, Math.max(b, 0));
 		}
 		
 		return Color.rgb(r, g, b);
 	}
 	
 	private View addLightView(ViewGroup container, final String id, final Light light) {
 		View view = getLayoutInflater().inflate(R.layout.lights_light, container, false);
 		
 		// Set switch event handler
 		final FeedbackSwitch switchView = (FeedbackSwitch) view.findViewById(R.id.lights_light_switch);
 		switchView.setCheckedCode(light.state.on);
 		switchView.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			@Override
 			public void onCheckedChanged(CompoundButton view, final boolean checked) {				
 				new AsyncTask<Void, Void, Boolean>() {
 					@Override
 					protected void onPreExecute() {
 						setActivityIndicator(true, false);
 						switchView.setEnabled(false);
 					}
 					
 					@Override
 					protected Boolean doInBackground(Void... params) {
 						try {
 							service.turnLightOn(id, checked);
 							return true;
 						} catch (Exception e) {
 							return false;
 						}
 					}
 					
 					@Override
 					protected void onPostExecute(Boolean result) {
 						setActivityIndicator(false, false);
 						switchView.setEnabled(true);
 						
 						// Toggle successful
 						// TODO: Refresh color state when turned on
 						if (result) {
 							lights.get(id).state.on = checked;
 						} else {
 							ErrorDialog.showNetworkError(getFragmentManager());
 						}
 						
 						refreshViews();
 					}
 				}.execute();
 			}
 		});
 		
 		container.addView(view);
 		
 		return view;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == android.R.id.home) {
 			Util.setLastBridge(this, null);
 			
 			Intent searchIntent = new Intent(this, LinkActivity.class);
 			searchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(searchIntent);
 			return true;
 		} else {
 			return super.onOptionsItemSelected(item);
 		}
 	}
 }
