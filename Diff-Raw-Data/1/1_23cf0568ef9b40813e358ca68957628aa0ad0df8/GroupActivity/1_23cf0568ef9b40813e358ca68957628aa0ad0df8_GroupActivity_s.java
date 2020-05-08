 package nl.q42.hue2.activities;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import nl.q42.hue2.PHUtilitiesImpl;
 import nl.q42.hue2.R;
 import nl.q42.hue2.Util;
 import nl.q42.hue2.dialogs.ErrorDialog;
 import nl.q42.hue2.dialogs.GroupLightDialog;
 import nl.q42.hue2.dialogs.GroupRemoveDialog;
 import nl.q42.hue2.views.ColorButton;
 import nl.q42.hue2.views.HueSlider;
 import nl.q42.hue2.views.SatBriSlider;
 import nl.q42.hue2.views.TempSlider;
 import nl.q42.javahueapi.HueService;
 import nl.q42.javahueapi.models.Group;
 import nl.q42.javahueapi.models.Light;
 import nl.q42.javahueapi.models.State;
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class GroupActivity extends Activity {
 	private final static int PREVIEW_INTERVAL = 500;
 	
 	private Group group;
 	private String id;
 	private HashMap<String, Light> lights;
 	private HueService service;
 	
 	private EditText nameView;
 	private Button lightsButton;
 	private HueSlider hueSlider;
 	private SatBriSlider satBriSlider;
 	private TempSlider tempSlider;
 	private ColorButton presetColorView;
 	
 	private String colorMode;
 	private boolean defaultColor = true;
 	
 	private Timer colorPreviewTimer = new Timer();
 	private boolean previewNeeded = false; // Set to true when color slider is moved
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		ActionBar ab = getActionBar();
 		ab.setDisplayHomeAsUpEnabled(true);
 		ab.setDisplayShowTitleEnabled(false);
 		
 		// Group details
 		group = (Group) getIntent().getSerializableExtra("group");
 		id = getIntent().getStringExtra("id");
 		lights = (HashMap<String, Light>) getIntent().getSerializableExtra("lights");
 		service = (HueService) getIntent().getSerializableExtra("service");
 		
 		// UI setup
 		setContentView(R.layout.activity_group);
 		
 		((TextView) findViewById(R.id.group_header)).setText(id == null ? R.string.group_new : R.string.group_group);
 		
 		nameView = (EditText) findViewById(R.id.group_name);
 		lightsButton = (Button) findViewById(R.id.group_lights);
 		hueSlider = (HueSlider) findViewById(R.id.group_color_hue);
 		satBriSlider = (SatBriSlider) findViewById(R.id.group_color_sat_bri);
 		tempSlider = (TempSlider) findViewById(R.id.group_color_temp);
 		
 		hueSlider.setSatBriSlider(satBriSlider);
 		tempSlider.setSliders(hueSlider, satBriSlider);
 		
 		presetColorView = (ColorButton) findViewById(R.id.group_preset_color);
 		
 		// Set listeners for color slider interaction to record last used color mode (hue/sat or temperature)
 		// and to send preview requests
 		tempSlider.setOnTouchListener(getColorModeListener("ct"));
 		hueSlider.setOnTouchListener(getColorModeListener("xy"));
 		satBriSlider.setOnTouchListener(getColorModeListener("xy"));
 		
 		// Save preset button
 		findViewById(R.id.group_save_preset).setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				saveGroup(true);
 				finish();
 			}
 		});
 		
 		// Create group button
 		findViewById(R.id.group_create).setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if (group.lights.size() == 0) {
 					ErrorDialog.show(getFragmentManager(), R.string.dialog_no_lights_title, R.string.dialog_no_lights);
 				} else {				
 					saveGroup(false);
 					finish();
 				}
 			}
 		});
 		
 		// Add lights button event handler
 		setLights(new ArrayList<String>(group.lights));
 		lightsButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				GroupLightDialog.newInstance(lights, group, service).show(getFragmentManager(), "dialog_lights");
 			}
 		});
 		
 		// Special actions for editing 'all' pseudo group
 		if (id.equals("0")) {
 			findViewById(R.id.group_details).setVisibility(View.GONE);
 			((TextView) findViewById(R.id.group_color_header)).setText(getString(R.string.group_all_lights));
 		}
 		
 		colorMode = "xy";
 		
 		if (savedInstanceState == null) {
 			nameView.setText(group.name);
 			
 			// Fill in color if all lights in group have the same color
 			State first = null;
 			Light firstLight = null;
 			boolean same = true;
 			
 			for (String id : group.lights) {
 				State cur = lights.get(id).state;
 				
 				if (first == null) {
 					first = cur;
 					firstLight = lights.get(id);
 				} else if (cur.on != first.on || !cur.colormode.equals(first.colormode) || cur.bri != first.bri) {
 					same = false;
 				} else {
 					if (cur.colormode.equals("ct") && cur.ct != first.ct) same = false;
 					else if (cur.colormode.equals("hs") && (cur.hue != first.hue || cur.sat != first.sat)) same = false;
 					else if (cur.colormode.equals("xy") && (cur.xy[0] != first.xy[0] || cur.xy[1] != first.xy[1])) same = false;
 				}
 			}
 			
 			if (same && first != null) {
 				if (first.colormode.equals("ct")) {
 					tempSlider.setTemp(first.ct);
 					colorMode = "ct";
 				} else {
 					float hsv[] = new float[3];
 					Color.colorToHSV(Util.getRGBColor(firstLight), hsv);
 					hueSlider.setHue(hsv[0]);
 					satBriSlider.setSaturation(hsv[1]);
 					satBriSlider.setBrightness(first.bri / 255.0f);
 					colorMode = "xy";
 				}
 				
 				defaultColor = false;
 			}
 		}
 		
 		updatePresetPreview();
 	}
 	
 	@Override
 	public void onBackPressed() {
 		saveGroup(false);
 		super.onBackPressed();
 	}
 	
 	private boolean hasColorChanged() {
 		return hueSlider.hasUserSet() || satBriSlider.hasUserSet() || tempSlider.hasUserSet();
 	}
 	
 	private void restoreLights() {
 		Intent result = new Intent();
 		result.putExtra("id", id);
 		result.putExtra("lights", new ArrayList<String>(group.lights));
 		result.putExtra("colorChanged", hasColorChanged());
 		
 		setResult(RESULT_CANCELED, result);
 	}
 	
 	@Override
 	public void onPause() {
 		super.onPause();
 		colorPreviewTimer.cancel();
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		
 		// Start timer that sends requests for color previews
 		colorPreviewTimer = new Timer();
 		colorPreviewTimer.scheduleAtFixedRate(new TimerTask() {
 			@Override
 			public void run() {
 				if (previewNeeded) {
 					previewNeeded = false;
 					
 					try {
 						float[] xy = PHUtilitiesImpl.calculateXY(satBriSlider.getResultColor(), null);
 						int bri = (int) (satBriSlider.getBrightness() * 255.0f);
 						int ct = (int) tempSlider.getTemp();
 						
 						if (colorMode.equals("ct")) {
 							service.setGroupCT(id, ct, bri);
 						} else {
 							service.setGroupXY(id, xy, bri);
 						}
 					} catch (Exception e) {
 						// Don't report exceptions since previewing is a non-essential feature
 					}
 				}
 			}
 		}, 0, PREVIEW_INTERVAL);
 	}
 	
 	private OnTouchListener getColorModeListener(final String mode) {
 		return new OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {				
 				colorMode = mode;
 				previewNeeded = true;
 				defaultColor = false;
 				updatePresetPreview();
 				return false;
 			}
 		};
 	}
 	
 	private void updatePresetPreview() {
 		if (colorMode.equals("ct")) {
 			presetColorView.setColor(Util.temperatureToColor(1000000 / (int) tempSlider.getTemp()));
 		} else {
 			float[] xy = PHUtilitiesImpl.calculateXY(satBriSlider.getResultColor(), null);
 			presetColorView.setColor(PHUtilitiesImpl.colorFromXY(xy, null));
 		}
 	}
 	
 	private String getLightsList() {
 		String lightsStr = "";
 		for (int i = 0; i < group.lights.size(); i++) {
 			lightsStr += lights.get(group.lights.get(i)).name;
 			if (i < group.lights.size() - 1) lightsStr += ", ";
 		}
 		return lightsStr;
 	}
 	
 	// Called by GroupRemoveDialog after confirmation
 	public void removeGroup() {
 		Intent result = new Intent();
 		result.putExtra("id", id);
 		result.putExtra("remove", true);
 		
 		setResult(RESULT_OK, result);
 		finish();
 	}
 	
 	// Called by GroupLightDialog after confirmation	
 	public void setLights(final ArrayList<String> newLights) {
 		// Update group with regards to color previews
 		updateLightColors(new ArrayList<String>(group.lights), newLights);
 		
 		group.lights = newLights;
 		
 		if (group.lights.size() == 0) {
 			lightsButton.setText(getString(R.string.group_no_lights));
 			lightsButton.setTextColor(Color.rgb(50, 187, 226));
 		} else {
 			lightsButton.setText(getLightsList());
 			lightsButton.setTextColor(Color.WHITE);
 		}
 	}
 	
 	private void updateLightColors(final ArrayList<String> oldLights, final ArrayList<String> newLights) {
 		new AsyncTask<Void, Void, Void>() {
 			@Override
 			protected Void doInBackground(Void... params) {
 				try {
 					service.setGroupLights(id, new ArrayList<String>(group.lights));
 					
 					// Restore lights removed from group
 					for (String id : oldLights) {
 						if (!newLights.contains(id)) {
 							service.setLightColor(id, lights.get(id).state);
 						}
 					}
 					
 					// Preview color on new lights in group
 					if (!defaultColor) {
 						for (String id : newLights) {
 							if (!oldLights.contains(id)) {
 								float[] xy = PHUtilitiesImpl.calculateXY(satBriSlider.getResultColor(), null);
 								int bri = (int) (satBriSlider.getBrightness() * 255.0f);
 								int ct = (int) tempSlider.getTemp();
 								
 								if (colorMode.equals("ct")) {
 									service.setLightCT(id, ct, bri, true);
 								} else {
 									service.setLightXY(id, xy, bri, true);
 								}
 							}
 						}
 					}
 				} catch (Exception e) {
 					// Previewing is non-essential, so ignore
 				}
 				
 				return null;
 			}
 		}.execute();
 	}
 	
 	private void saveGroup(boolean addPreset) {
 		ArrayList<String> groupLights = new ArrayList<String>();
 		groupLights.addAll(group.lights);
 		
 		float[] xy = PHUtilitiesImpl.calculateXY(satBriSlider.getResultColor(), null);
 		int bri = (int) (satBriSlider.getBrightness() * 255.0f);
 		int ct = (int) tempSlider.getTemp();
 		
 		Intent result = new Intent();
 		result.putExtra("id", id);
 		result.putExtra("name", nameView.getText().toString().trim());
 		result.putExtra("lights", groupLights);
 		result.putExtra("mode", colorMode);
 		result.putExtra("xy", xy);
 		result.putExtra("ct", ct);
 		result.putExtra("bri", bri);
 		
 		if (addPreset) result.putExtra("addPreset", true);
 		
 		// If the color sliders registered touch events, we know the color has been changed (easier than conversion and checking)
 		result.putExtra("colorChanged", hasColorChanged());
 		
 		setResult(RESULT_OK, result);
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.group, menu);
 	    
 	    // Pseudo group with all lights (or a new group) cannot be removed
 	    menu.findItem(R.id.menu_delete_group).setVisible(!id.equals("0"));
 	    
 	    return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == R.id.menu_delete_group) {
 			GroupRemoveDialog.newInstance().show(getFragmentManager(), "dialog_remove_group");
 			return true;
 		} else if (item.getItemId() == R.id.menu_undo) {
 			restoreLights();
 			finish();			
 			return true;
 		} else if (item.getItemId() == android.R.id.home) {
 			saveGroup(false);
 			finish();
 			
 			return true;
 		} else {
 			return super.onOptionsItemSelected(item);
 		}
 	}
 }
