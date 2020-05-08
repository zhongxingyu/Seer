 package nl.q42.huelimitededition.activities;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import nl.q42.huelimitededition.PHUtilitiesImpl;
 import nl.q42.huelimitededition.R;
 import nl.q42.huelimitededition.Util;
 import nl.q42.huelimitededition.views.ColorButton;
 import nl.q42.huelimitededition.views.HueSlider;
 import nl.q42.huelimitededition.views.SatBriSlider;
 import nl.q42.huelimitededition.views.TempSlider;
 import nl.q42.javahueapi.HueService;
 import nl.q42.javahueapi.models.Light;
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.widget.EditText;
 
 public class LightActivity extends Activity {
 	private final static int PREVIEW_INTERVAL = 500;
 	
 	private Light light;
 	private String id;
 	private HueService service;
 	
 	private EditText nameView;
 	private HueSlider hueSlider;
 	private SatBriSlider satBriSlider;
 	private TempSlider tempSlider;
 	private ColorButton presetColorView;
 	
 	private String colorMode;
 	
 	private Timer colorPreviewTimer = new Timer();
 	private boolean previewNeeded = false; // Set to true when color slider is moved
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		ActionBar ab = getActionBar();
 		ab.setDisplayHomeAsUpEnabled(true);
 		ab.setDisplayShowTitleEnabled(false);
 		
 		// Light details
 		light = (Light) getIntent().getSerializableExtra("light");
 		id = getIntent().getStringExtra("id");
 		service = (HueService) getIntent().getSerializableExtra("service");
 		
 		// UI setup
 		setContentView(R.layout.activity_light);
 		setTitle(getString(R.string.light_edit));
 		
 		nameView = (EditText) findViewById(R.id.light_name);
 		hueSlider = (HueSlider) findViewById(R.id.light_color_hue);
 		satBriSlider = (SatBriSlider) findViewById(R.id.light_color_sat_bri);
 		tempSlider = (TempSlider) findViewById(R.id.light_color_temp);
 		
 		satBriSlider.setSliders(hueSlider, tempSlider);
 		hueSlider.setSliders(satBriSlider, tempSlider);
 		tempSlider.setSliders(hueSlider, satBriSlider);
 		
 		presetColorView = (ColorButton) findViewById(R.id.light_preset_color);
 		
 		// Set listeners for color slider interaction to record last used color mode (hue/sat or temperature)
 		// and to send preview requests
 		tempSlider.setOnTouchListener(getColorModeListener("ct"));
 		hueSlider.setOnTouchListener(getColorModeListener("xy"));
 		satBriSlider.setOnTouchListener(getColorModeListener("xy"));
 		
 		// Save preset button
 		findViewById(R.id.light_save_preset).setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				saveLight(true);
 				finish();
 			}
 		});
 		
 		// Fill in current name/color in UI or restore previous
 		if (savedInstanceState == null) {
 			nameView.setText(light.name);
 			
 			if ("ct".equals(light.state.colormode)) {
 				tempSlider.setTemp(light.state.ct);
 				tempSlider.setActive(true);
 				colorMode = "ct";
 			} else {
 				float hsv[] = new float[3];
 				Color.colorToHSV(Util.getRGBColor(light), hsv);
 				hueSlider.setHue(hsv[0]);
 				satBriSlider.setSaturation(hsv[1]);
 				satBriSlider.setBrightness(light.state.bri / 255.0f);
 				hueSlider.setActive(true);
 				satBriSlider.setActive(true);
 				colorMode = "xy";
 			}
 		}
 		
 		updatePresetPreview();
 	}
 	
 	@Override
 	public void onBackPressed() {
 		saveLight(false);
 		super.onBackPressed();
 	}
 	
 	private void saveLight(boolean addPreset) {
 		float[] xy = PHUtilitiesImpl.calculateXY(satBriSlider.getResultColor(), light.modelid);
 		int bri = (int) (satBriSlider.getBrightness() * 255.0f);
 		int ct = (int) tempSlider.getTemp();
 		
 		Intent result = new Intent();
 		result.putExtra("id", id);
 		result.putExtra("name", nameView.getText().toString().trim());
 		result.putExtra("mode", colorMode);
 		result.putExtra("xy", xy);
 		result.putExtra("ct", ct);
 		result.putExtra("bri", bri);
 		
 		if (addPreset) result.putExtra("addPreset", true);
 
 		// If the color sliders registered touch events, we know the color has been changed (easier than conversion and checking)
 		result.putExtra("colorChanged", hasColorChanged());
 		
 		setResult(RESULT_OK, result);
 	}
 	
 	private boolean hasColorChanged() {
 		return hueSlider.hasUserSet() || satBriSlider.hasUserSet() || tempSlider.hasUserSet();
 	}
 	
 	private void restoreLight() {
 		Intent result = new Intent();
 		result.putExtra("id", id);
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
 						float[] xy = PHUtilitiesImpl.calculateXY(satBriSlider.getResultColor(), light.modelid);
 						int bri = (int) (satBriSlider.getBrightness() * 255.0f);
 						int ct = (int) tempSlider.getTemp();
 						
 						if (colorMode.equals("ct")) {
 							service.setLightCT(id, ct, bri, true);
 						} else {
 							service.setLightXY(id, xy, bri, true);
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
 				updatePresetPreview();
 				
 				return false;
 			}
 		};
 	}
 	
 	private void updatePresetPreview() {
		if ("ct".equals(colorMode)) {
 			presetColorView.setColor(Util.temperatureToColor(1000000 / (int) tempSlider.getTemp()));
 		} else {
 			float[] xy = PHUtilitiesImpl.calculateXY(satBriSlider.getResultColor(), light.modelid);
 			presetColorView.setColor(PHUtilitiesImpl.colorFromXY(xy, light.modelid));
 		}
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.light, menu);
 	    return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getItemId() == R.id.menu_undo) {
 			restoreLight();
 			finish();
 			
 			return true;
 		} else if (item.getItemId() == android.R.id.home) {
 			saveLight(false);
 			finish();
 			
 			return true;
 		} else {
 			return super.onOptionsItemSelected(item);
 		}
 	}
 }
