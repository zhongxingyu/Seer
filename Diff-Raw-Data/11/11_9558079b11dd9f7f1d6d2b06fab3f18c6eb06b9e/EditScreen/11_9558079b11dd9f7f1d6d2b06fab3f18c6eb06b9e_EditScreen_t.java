 package edu.berkeley.cs160.smartnature;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.graphics.Color;
 import android.graphics.Matrix;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.animation.ScaleAnimation;
 import android.view.animation.TranslateAnimation;
 import android.widget.TextView;
 import android.widget.ZoomControls;
 
 public class EditScreen extends Activity implements View.OnClickListener, ColorPickerDialog.OnColorChangedListener {
 	
 	Garden mockGarden;
 	EditView editView;
 	Plot plot, oldPlot;
 	Bundle extras;
 	
 	ZoomControls zoom;
 	
 	boolean footerShown;
 	/** false if activity has been previously started */
 	boolean firstInit = true;
 	/** User-related options */
 	boolean showLabels = true, rotateMode, showFullScreen, zoomAutoHidden;
 	/** describes what zoom button was pressed: 1 for +, -1 for -, and 0 by default */
 	int zoomPressed;
	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		firstInit = savedInstanceState == null;
 		showFullScreen = getSharedPreferences("global", Context.MODE_PRIVATE).getBoolean("garden_fullscreen", false); 
 		if (showFullScreen)
 			setTheme(android.R.style.Theme_Light_NoTitleBar_Fullscreen);
 		super.onCreate(savedInstanceState);
 		
 		extras = getIntent().getExtras();
 		mockGarden = StartScreen.gardens.get(extras.getInt("garden_id"));
 		setTitle(extras.getString("name") + " (Edit mode)"); 
 		
 		if (firstInit && extras.containsKey("type"))
 			createPlot();
 		else
 			loadPlot();
 		
 		if (firstInit) { 
 			oldPlot = firstInit ? new Plot(plot) : mockGarden.getPlot(mockGarden.size() - 1);
 			mockGarden.addPlot(oldPlot);
 		} else
 			oldPlot = mockGarden.getPlot(mockGarden.size() - 1);
 		
 		setContentView(R.layout.edit_plot);
 		plot.getPaint().setStrokeWidth(getResources().getDimension(R.dimen.strokesize_edit));
 		editView = (EditView) findViewById(R.id.edit_view);
 		
 		if (firstInit) {
 			editView.zoomScale = extras.getFloat("zoom_scale");
 			editView.dragMatrix.setValues(extras.getFloatArray("drag_matrix"));
 			editView.bgDragMatrix.setValues(extras.getFloatArray("bgdrag_matrix"));
 			editView.onAnimationEnd();
 		}
 		
 		zoom = (ZoomControls) findViewById(R.id.edit_zoom_controls);
 		zoom.setOnZoomInClickListener(zoomIn);
 		zoom.setOnZoomOutClickListener(zoomOut);
 		zoomAutoHidden = getSharedPreferences("global", Context.MODE_PRIVATE).getBoolean("zoom_autohide", false);
 		if (zoomAutoHidden)
 			zoom.setVisibility(View.GONE);
 		
 		boolean hintsOn = getSharedPreferences("global", Context.MODE_PRIVATE).getBoolean("show_hints", true);
 		if (hintsOn) {
 			TextView hint = (TextView) findViewById(R.id.edit_hint);
 			hint.setText(R.string.hint_editscreen);
 			hint.setVisibility(View.VISIBLE);
 		}
 		
 		findViewById(R.id.rotateButton).setOnClickListener(this);
 		findViewById(R.id.saveButton).setOnClickListener(this);
 		
 		editView.invalidate();
 	}
 	
 	public void createPlot() {
 		RectF gBounds = mockGarden.getRawBounds();
 		int type = extras.getInt("type");
 		String name = extras.getString("name");
 		if (type == Plot.POLY) {
 			Rect bounds = new Rect(270, 120, 270 + 90, 120 + 100);
 			float[] pts = { 0, 0, 50, 10, 90, 100 };
 			plot = new Plot(name, bounds, 0, pts);
 		}
 		else {
 			Rect bounds = new Rect((int)gBounds.left, (int)gBounds.top, (int)gBounds.right, (int)gBounds.bottom);
 			bounds.inset((int)gBounds.width()/3, (int)gBounds.height()/3);
 			plot = new Plot(name, bounds, 0, type);
 		}
 		mockGarden.addPlot(plot);
 	}
 	
 	public void loadPlot() {
 		if (extras.containsKey("type"))
 			plot = mockGarden.getPlot(mockGarden.size() - 2);
 		else
 			plot = mockGarden.getPlot(extras.getInt("plot_id"));
 	}
 	
 	@Override
 	public void onSaveInstanceState(Bundle savedInstanceState) {
 		savedInstanceState.putFloat("zoom_scale", editView.zoomScale);
 		savedInstanceState.putBoolean("portrait_mode", editView.portraitMode);
 		float[] values = new float[9], bgvalues = new float[9];
 		editView.dragMatrix.getValues(values);
 		editView.bgDragMatrix.getValues(bgvalues);
 		savedInstanceState.putFloatArray("drag_matrix", values);
 		savedInstanceState.putFloatArray("bgdrag_matrix", bgvalues);
 		super.onSaveInstanceState(savedInstanceState);
 	}
 	
 	@Override
 	public void onRestoreInstanceState(Bundle savedInstanceState) {
 		super.onRestoreInstanceState(savedInstanceState);
 		editView.zoomScale = savedInstanceState.getFloat("zoom_scale");
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
 		
 		editView.dragMatrix.setValues(values);
 		editView.bgDragMatrix.setValues(bgvalues);
 		editView.onAnimationEnd();
 	}
 	
 	@Override
 	public void onBackPressed() {
 		mockGarden.remove(oldPlot);
 		plot.getPaint().setStrokeWidth(getResources().getDimension(R.dimen.strokesize_default));
 		Intent intent = new Intent();
 		Bundle bundle = new Bundle();
 		bundle.putFloat("zoom_scale", editView.zoomScale);
 		float[] values = new float[9], bgvalues = new float[9];
 		editView.dragMatrix.getValues(values);
 		editView.bgDragMatrix.getValues(bgvalues);
 		bundle.putFloatArray("drag_matrix", values);
 		bundle.putFloatArray("bgdrag_matrix", bgvalues);
 		intent.putExtras(bundle);
 		setResult(RESULT_OK, intent);
 		finish();
 		overridePendingTransition(0, 0);
 	}
 	
 	/** in this method views actually have valid dimensions */
 	@Override
 	public void onWindowFocusChanged(boolean hasFocus) {
 		super.onWindowFocusChanged(hasFocus);
 		if (firstInit && !footerShown) {
 			footerShown = true;
 			TranslateAnimation anim = new TranslateAnimation(0, 0, findViewById(R.id.footer).getHeight(), 0);
 			anim.setDuration(getResources().getInteger(R.integer.footer_duration));
 			findViewById(R.id.footer).startAnimation(anim);
 		}
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.plot_edit_menu, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.m_changecolor:
 			int color = getPreferences(MODE_PRIVATE).getInt("color", Color.WHITE);
 			new ColorPickerDialog(this, this, color).show();
 			break;
 		case R.id.m_resetzoom:
 			editView.zoomScale = 1;
 			mockGarden.refreshBounds();
 			editView.reset();
 			break;
 		case R.id.m_revert:
 			plot.set(oldPlot);
 			plot.getPaint().setStrokeWidth(getResources().getDimension(R.dimen.strokesize_edit));
 			editView.invalidate();
 			break;
 		}
 		
 		return super.onOptionsItemSelected(item);
 	}
 	
 	@Override
 	public void onClick(View view) {
 		switch (view.getId()) {
 			case R.id.rotateButton:
 				rotateMode = !rotateMode;
 				((TextView)findViewById(R.id.mode_rotate)).setText("Rotate Mode is " + (rotateMode ? "ON" : "OFF"));
 				editView.invalidate();
 				break;
 			case R.id.saveButton:
 				onBackPressed();
 				break;
 		}
 	}
 	
 	View.OnClickListener zoomIn = new View.OnClickListener() {
 		@Override
 		public void onClick(View view) {
 			handleZoom();
 			if (zoomPressed == 0) {
 				zoomPressed = 1;
 				float zoomScalar = getResources().getDimension(R.dimen.zoom_scalar);
 				ScaleAnimation anim = new ScaleAnimation(1, zoomScalar, 1, zoomScalar, editView.getWidth()/2f, editView.getHeight()/2f);
 				anim.setDuration(getResources().getInteger(R.integer.zoom_duration));
 				editView.startAnimation(anim);
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
 				ScaleAnimation anim = new ScaleAnimation(1, zoomScalar, 1, zoomScalar, editView.getWidth()/2f, editView.getHeight()/2f); 
 				anim.setDuration(getResources().getInteger(R.integer.zoom_duration));
 				editView.startAnimation(anim);
 			}
 		}
 	};
 	
 	public void handleZoom() {
 		zoom.removeCallbacks(autoHide);
 		if (!zoom.isShown())
 			zoom.show();
 		zoom.postDelayed(autoHide, getResources().getInteger(R.integer.hidezoom_delay));
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
 	
 	@Override
 	public void colorChanged(int color) {
 		getPreferences(MODE_PRIVATE).edit().putInt("color", color).commit();
 		plot.getPaint().setColor(color);
 		editView.invalidate();
 	}
 	
 }
