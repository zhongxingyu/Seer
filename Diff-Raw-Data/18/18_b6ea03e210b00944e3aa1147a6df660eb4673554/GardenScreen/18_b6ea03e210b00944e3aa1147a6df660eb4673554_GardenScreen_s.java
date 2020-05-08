 package edu.berkeley.cs160.smartnature;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.graphics.Matrix;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.provider.MediaStore.Images;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.animation.ScaleAnimation;
 import android.widget.TextView;
 import android.widget.ZoomControls;
 
 public class GardenScreen extends Activity implements View.OnClickListener, View.OnFocusChangeListener, View.OnTouchListener {
 	
 	final static int VIEW_PLOT = 1, USE_CAMERA = 2;
 	Garden mockGarden;
 	GardenView gardenView;
 	View textEntryView;
 	AlertDialog dialog;
 	ZoomControls zoomControls;
 	
 	/** User-related options */
 	boolean showLabels = true, showFullScreen, zoomAutoHidden;
 	int currentDialog;
 	/** describes what zoom button was pressed: 1 for +, -1 for -, and 0 by default */
 	int zoomPressed;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		showFullScreen = getSharedPreferences("global", MODE_PRIVATE).getBoolean("garden_fullscreen", false); 
 		if (showFullScreen)
 			setTheme(android.R.style.Theme_Light_NoTitleBar_Fullscreen);
 		super.onCreate(savedInstanceState);
 		mockGarden = GardenGnome.gardens.get(getIntent().getIntExtra("garden_id", 0));
 		setTitle(mockGarden.getName());
 		if (savedInstanceState == null) // first init
 			mockGarden.refreshBounds();
 		
 		setContentView(R.layout.garden);
 		gardenView = (GardenView) findViewById(R.id.garden_view);
 		findViewById(R.id.garden_footer).getBackground().setAlpha(getResources().getInteger(R.integer.bar_trans));
 		initButton(R.id.addplot_btn);
 		initButton(R.id.zoomfit_btn);
 		boolean hintsOn = getSharedPreferences("global", Context.MODE_PRIVATE).getBoolean("show_hints", true);
 		if (hintsOn) {
 			((TextView)findViewById(R.id.garden_hint)).setText(R.string.hint_gardenscreen);
 			((TextView)findViewById(R.id.garden_hint)).setVisibility(View.VISIBLE);
 		}
 		zoomControls = (ZoomControls) findViewById(R.id.zoom_controls);
 		zoomAutoHidden = getSharedPreferences("global", MODE_PRIVATE).getBoolean("zoom_autohide", false);
 		if (zoomAutoHidden)
 			zoomControls.setVisibility(View.GONE);
 		zoomControls.setOnZoomInClickListener(zoomIn);
 		zoomControls.setOnZoomOutClickListener(zoomOut);
 	}
 	
 	public void initButton(int id) {
 		View view = findViewById(id);
 		view.setOnClickListener(this);
 		view.setOnFocusChangeListener(this);
 		view.setOnTouchListener(this);
 		view.getBackground().setAlpha(getResources().getInteger(R.integer.btn_trans));
 	}
 	
 	@Override
 	public void onSaveInstanceState(Bundle savedInstanceState) {
 		savedInstanceState.putFloat("zoom_scale", gardenView.zoomScale);
 		savedInstanceState.putBoolean("portrait_mode", gardenView.portraitMode);
 		float[] values = new float[9], bgvalues = new float[9];
 		gardenView.dragMatrix.getValues(values);
 		gardenView.bgDragMatrix.getValues(bgvalues);
 		savedInstanceState.putFloatArray("drag_matrix", values);
 		savedInstanceState.putParcelable("key", imageUri);
 		//("drag_matrix", values);
 		savedInstanceState.putFloatArray("bgdrag_matrix", bgvalues);
 		super.onSaveInstanceState(savedInstanceState);
 	}
 	
 	@Override
 	public void onRestoreInstanceState(Bundle savedInstanceState) {
 		super.onRestoreInstanceState(savedInstanceState);
 		gardenView.zoomScale = savedInstanceState.getFloat("zoom_scale");
 		boolean prevPortraitMode = savedInstanceState.getBoolean("portrait_mode");
 		int orien = getResources().getConfiguration().orientation;
 		imageUri = (Uri) savedInstanceState.getParcelable("key");
 		
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
 	public void onClick(View view) {
 		switch (view.getId()) {
 			case R.id.addplot_btn:
 				startActivityForResult(new Intent(this, AddPlot.class), 0);
 				break;
 			case R.id.zoomfit_btn:
 				gardenView.zoomScale = 1;
 				mockGarden.refreshBounds();
 				gardenView.reset();
 				break;
 		}
 	}
 	
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		if (requestCode == VIEW_PLOT && zoomAutoHidden) // returning from PlotScreen activity
 			zoomControls.hide(); // need to manually hide
 		else if (data != null && data.hasExtra("name")) { // returning from AddPlot activity
 				data.putExtra("garden_id", GardenGnome.gardens.indexOf(mockGarden));
 				data.putExtra("zoom_scale", gardenView.zoomScale);
 				float[] values = new float[9], bgvalues = new float[9];
 				gardenView.dragMatrix.getValues(values);
 				gardenView.bgDragMatrix.getValues(bgvalues);
 				data.putExtra("drag_matrix", values);
 				data.putExtra("bgdrag_matrix", bgvalues);
 				startActivityForResult(data, 0);
 				overridePendingTransition(0, 0);
 		}
 		else if (data != null && data.hasExtra("zoom_scale")) { // returning from EditScreen activity
 			gardenView.zoomScale = data.getFloatExtra("zoom_scale", 1); //extras.getFloat("zoom_scale");
 			gardenView.dragMatrix.setValues(data.getFloatArrayExtra("drag_matrix"));
 			gardenView.bgDragMatrix.setValues(data.getFloatArrayExtra("bgdrag_matrix"));
 			gardenView.onAnimationEnd();
 			if (zoomAutoHidden)
 				zoomControls.setVisibility(View.GONE); // need to manually hide
 		}
 		
 		if (requestCode == USE_CAMERA && resultCode == RESULT_OK) {
			if (data.getData() != null)
 				imageUri = data.getData();
			System.out.println(imageUri.toString());
 			mockGarden.addImage(imageUri);
 		}
 		
 		setTitle(mockGarden.getName());
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.garden_menu, menu);
 		return true;
 	}
 	
 	Uri imageUri;
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Intent intent;
 		
 		switch (item.getItemId()) {
 			case R.id.m_home:
 				finish();
 				break;
 			case R.id.m_gardenoptions:
 				intent = new Intent(this, GardenAttr.class);
 				intent.putExtra("garden_id", GardenGnome.gardens.indexOf(mockGarden));
 				startActivityForResult(intent, 0);
 				break;
 			case R.id.m_sharegarden:
 				intent = new Intent(this, ShareGarden.class);
 				intent.putExtra("garden_id", GardenGnome.gardens.indexOf(mockGarden));
 				startActivity(intent);
 				break;
 			case R.id.m_showlabels:
 				showLabels = !showLabels;
 				item.setTitle(showLabels ? "Hide labels" : "Show labels");
 				gardenView.invalidate();
 				break;
 			case R.id.m_takephoto:
 				intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 				String fileName = "GardenGnome_" + mockGarden.getName() + mockGarden.numImages() + ".jpg";
 				
 				// MY STUPID HTC CAMERA APP IGNORES EXTRA_OUTPUT!!! 
 				ContentValues values = new ContentValues();
 				values.put(Images.Media.TITLE, fileName);
 				//values.put(MediaStore.Images.Media.DESCRIPTION, "Image capture by camera");
 				imageUri = getContentResolver().insert(Images.Media.EXTERNAL_CONTENT_URI, values);
 				intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
 				startActivityForResult(intent, USE_CAMERA);
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
 			zoomControls.removeCallbacks(autoHide);
 			if (!zoomControls.isShown())
 				zoomControls.show();
 			zoomControls.postDelayed(autoHide, getResources().getInteger(R.integer.hidezoom_delay));
 		}
 	}
 	
 	Runnable autoHide = new Runnable() {
 		@Override
 		public void run() {
 			if (zoomControls.isShown()) {
 				zoomControls.removeCallbacks(this);
 				zoomControls.hide();
 			}
 		}
 	};
 	
 	@Override
 	public void onFocusChange(View view, boolean hasFocus) {
 		if (hasFocus)
 			view.getBackground().setAlpha(0xff);
 		else
 			view.getBackground().setAlpha(getResources().getInteger(R.integer.btn_trans));
 	}
 	
 	@Override
 	public boolean onTouch(View view, MotionEvent event) {
 		if (event.getAction() == MotionEvent.ACTION_DOWN)
 			view.getBackground().setAlpha(0xff);
 		else if (event.getAction() == MotionEvent.ACTION_MOVE) {
 			if (!view.isPressed()) {
 				view.getBackground().setAlpha(getResources().getInteger(R.integer.btn_trans));
 				view.invalidate();
 			}
 		}
 		else {
 			view.getBackground().setAlpha(getResources().getInteger(R.integer.btn_trans));
 			//view.invalidate();
 		}
 		return false;
 	}
 	
 }
