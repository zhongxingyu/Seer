 package edu.berkeley.cs160.smartnature;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.graphics.Matrix;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.WindowManager;
 import android.view.animation.ScaleAnimation;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.ZoomControls;
 
 public class GardenScreen extends Activity implements DialogInterface.OnClickListener {
 	
 	final int NEW_DIALOG = 0, RENAME_DIALOG = 1;
 	Garden mockGarden;
 	GardenView gardenView;
 	View textEntryView;
 	AlertDialog dialog;
 	ZoomControls zoom;
 	
 	
 	/** User-related options */
 	boolean showLabels = true, showFullScreen, zoomAutoHidden;
 	int currentDialog;
 	/** describes what zoom button was pressed: 1 for +, -1 for -, and 0 by default */
 	int zoomPressed;
 	int gardenID;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		showFullScreen = getSharedPreferences("global", MODE_PRIVATE).getBoolean("garden_fullscreen", false); 
 		if (showFullScreen)
 			setTheme(android.R.style.Theme_Light_NoTitleBar_Fullscreen);
 		super.onCreate(savedInstanceState);
 		Bundle extras = getIntent().getExtras();
 		if (extras != null && extras.containsKey("id")) {
 			mockGarden = StartScreen.gardens.get(extras.getInt("id"));
 			gardenID = extras.getInt("id");
 			setTitle(mockGarden.getName());
 		} else {
 			mockGarden = new Garden();
 			showDialog(NEW_DIALOG);
 		}
 		setContentView(R.layout.garden);
 		gardenView = (GardenView) findViewById(R.id.garden_view);
 		
 		zoom = (ZoomControls) findViewById(R.id.zoom_controls);
 		zoomAutoHidden = getSharedPreferences("global", MODE_PRIVATE).getBoolean("zoom_autohide", false);
 		if (zoomAutoHidden)
 			zoom.setVisibility(View.GONE);
 		zoom.setOnZoomInClickListener(zoomIn);
 		zoom.setOnZoomOutClickListener(zoomOut);
 		boolean hintsOn = getSharedPreferences("global", Context.MODE_PRIVATE).getBoolean("show_hints", true);
 		if (hintsOn) {
 			((TextView)findViewById(R.id.garden_hint)).setText(R.string.hint_gardenscreen);
 			((TextView)findViewById(R.id.garden_hint)).setVisibility(View.VISIBLE);
 		}
 	}
 	
 	@Override
 	public void onSaveInstanceState(Bundle savedInstanceState) {
 		savedInstanceState.putFloat("zoom_scale", gardenView.zoomScale);
 		savedInstanceState.putBoolean("portrait_mode", gardenView.portraitMode);
 		float[] values = new float[9], bgvalues = new float[9];
 		gardenView.dragMatrix.getValues(values);
 		gardenView.bgDragMatrix.getValues(bgvalues);
 		savedInstanceState.putFloatArray("drag_matrix", values);
 		savedInstanceState.putFloatArray("bgdrag_matrix", bgvalues);
 		super.onSaveInstanceState(savedInstanceState);
 	}
 	
 	@Override
 	public void onRestoreInstanceState(Bundle savedInstanceState) {
 		super.onRestoreInstanceState(savedInstanceState);
 		gardenView.zoomScale = savedInstanceState.getFloat("zoom_scale");
 		boolean prevPortraitMode = savedInstanceState.getBoolean("portrait_mode");
 		int orien = getResources().getConfiguration().orientation;
 		
 		float[] values = savedInstanceState.getFloatArray("drag_matrix");
 		float[] bgvalues = savedInstanceState.getFloatArray("bgdrag_matrix");
 		
 		if (orien == Configuration.ORIENTATION_PORTRAIT && !prevPortraitMode) {
 			// changed from landscape to portrait
 			float tmp = values[Matrix.MTRANS_X];
 			values[Matrix.MTRANS_X] = -values[Matrix.MTRANS_Y];
 			values[Matrix.MTRANS_Y] = tmp;
 			tmp = bgvalues[Matrix.MTRANS_X];
 			bgvalues[Matrix.MTRANS_X] = -bgvalues[Matrix.MTRANS_Y];
 			bgvalues[Matrix.MTRANS_Y] = tmp;
 		}
 		else if (orien == Configuration.ORIENTATION_LANDSCAPE && prevPortraitMode) {
 			// changed from portrait to landscape
 			float tmp = values[Matrix.MTRANS_X];
 			values[Matrix.MTRANS_X] = values[Matrix.MTRANS_Y];
 			values[Matrix.MTRANS_Y] = -tmp;
 			tmp = bgvalues[Matrix.MTRANS_X];
 			bgvalues[Matrix.MTRANS_X] = bgvalues[Matrix.MTRANS_Y];
 			bgvalues[Matrix.MTRANS_Y] = -tmp;
 		}
 		
 		gardenView.dragMatrix.setValues(values);
		gardenView.bgDragMatrix.setValues(bgvalues);
 		gardenView.onAnimationEnd();	
 	}
 	
 	@Override
 	public Dialog onCreateDialog(int id) {
 		DialogInterface.OnClickListener cancelled = new DialogInterface.OnClickListener() {
 			@Override public void onClick(DialogInterface dialog, int whichButton) { finish(); }
 		};
 		
 		DialogInterface.OnCancelListener exited = new DialogInterface.OnCancelListener() {
 			@Override public void onCancel(DialogInterface dialog) { finish(); }
 		};
 		
 		textEntryView = LayoutInflater.from(this).inflate(R.layout.text_entry_dialog, null);
 		AlertDialog.Builder builder = new AlertDialog.Builder(this).setView(textEntryView);
 		
 		if (id == NEW_DIALOG)
 			builder.setTitle(R.string.new_garden_prompt)
 				.setPositiveButton(R.string.alert_dialog_ok, this)
 				.setNegativeButton(R.string.alert_dialog_cancel, cancelled) // this means cancel was pressed
 				.setOnCancelListener(exited); // this means the back button was pressed
 		else {
 			((EditText) textEntryView.findViewById(R.id.dialog_text_entry)).setText(mockGarden.getName());
 			builder.setTitle(R.string.rename_garden_prompt)
 				.setPositiveButton(R.string.alert_dialog_rename, this)
 				.setNegativeButton(R.string.alert_dialog_cancel, null);	
 		}
 		dialog = builder.create();
 		
 		// automatically show soft keyboard
 		EditText input = (EditText) textEntryView.findViewById(R.id.dialog_text_entry);
 		input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
 			@Override
 			public void onFocusChange(View v, boolean hasFocus) {
 				if (hasFocus)
 					dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
 			}
 		});
 		
 		return dialog;
 	}
 	
 	@Override
 	public void onClick(DialogInterface dialog, int whichButton) {
 		EditText gardenName = (EditText) textEntryView.findViewById(R.id.dialog_text_entry);
 		setTitle(gardenName.getText().toString());
 		mockGarden.setName(gardenName.getText().toString());
 		if (currentDialog == NEW_DIALOG)
 			StartScreen.gardens.add(mockGarden);
 		StartScreen.adapter.notifyDataSetChanged();	
 	}
 	
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		if (data != null) { // AddPlot activity was cancelled
 			Bundle extras = data.getExtras();
 			if (extras.containsKey("name")) { // returning from AddPlot activity
 				extras.putInt("garden_id", StartScreen.gardens.indexOf(mockGarden));
 				extras.putFloat("zoom_scale", gardenView.zoomScale);
 				float[] values = new float[9], bgvalues = new float[9];
 				gardenView.dragMatrix.getValues(values);
 				gardenView.bgDragMatrix.getValues(bgvalues);
 				extras.putFloatArray("drag_matrix", values);
 				extras.putFloatArray("bgdrag_matrix", bgvalues);
 				data.putExtras(extras);
 				startActivityForResult(data, 0);
 				overridePendingTransition(0, 0);
 			}
 			else if (extras.containsKey("zoom_scale")) { // returning from EditScreen activity
 				gardenView.zoomScale = extras.getFloat("zoom_scale");
 				gardenView.dragMatrix.setValues(extras.getFloatArray("drag_matrix"));
 				gardenView.bgDragMatrix.setValues(extras.getFloatArray("bgdrag_matrix"));
 				gardenView.onAnimationEnd();
 				if (zoomAutoHidden)
 					zoom.setVisibility(View.GONE); // Android bug?
 			}
 		}
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.garden_menu, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.m_addplot:
 				startActivityForResult(new Intent(this, AddPlot.class), 0);
 				break;
 			case R.id.m_home:
 				finish();
 				break;
 			case R.id.m_renamegarden:
 				currentDialog = RENAME_DIALOG;
 				showDialog(RENAME_DIALOG);
 				break;
 			case R.id.m_resetzoom:
 				gardenView.zoomScale = 1;
 				mockGarden.refreshBounds();
 				gardenView.reset();
 				break;
 			case R.id.m_sharegarden:
 				startActivity(new Intent(this, ShareGarden.class));
 				break;
 			case R.id.m_showlabels:
 				showLabels = !showLabels;
 				item.setTitle(showLabels ? "Hide labels" : "Show labels");
 				gardenView.invalidate();				
 				break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	View.OnClickListener zoomIn = new View.OnClickListener() {
 		@Override
 		public void onClick(final View view) {
 			handleZoom();
 			if (zoomPressed == 0) {
 				zoomPressed = 1;
 				float zoomScalar = getResources().getDimension(R.dimen.zoom_scalar);
 				ScaleAnimation anim = new ScaleAnimation(1, zoomScalar, 1, zoomScalar, gardenView.getWidth()/2f, gardenView.getHeight()/2f);
 				anim.setDuration(getResources().getInteger(R.integer.zoom_duration));
 				gardenView.startAnimation(anim);
 			}
 		}
 	};
 	
 	View.OnClickListener zoomOut = new View.OnClickListener() {
 		@Override
 		public void onClick(View view) {
 			handleZoom();
 			if (zoomPressed == 0) {
 				zoomPressed = -1;
 				float zoomScalar = 1/getResources().getDimension(R.dimen.zoom_scalar);
 				ScaleAnimation anim = new ScaleAnimation(1, zoomScalar, 1, zoomScalar, gardenView.getWidth()/2f, gardenView.getHeight()/2f); 
 				anim.setDuration(getResources().getInteger(R.integer.zoom_duration));
 				gardenView.startAnimation(anim);
 			}
 		}
 	};
 	
 	public void handleZoom() {
 		if (zoomAutoHidden) {
 			zoom.removeCallbacks(autoHide);
 			if (!zoom.isShown())
 				zoom.show();
 			zoom.postDelayed(autoHide, getResources().getInteger(R.integer.hidezoom_delay));
 		}
 	}
 	
 	Runnable autoHide = new Runnable() {
 		@Override
 		public void run() {
 			if (zoom.isShown()) {
 				zoom.removeCallbacks(this);
 				zoom.hide();
 			}
 		}
 	};
 	
 }
