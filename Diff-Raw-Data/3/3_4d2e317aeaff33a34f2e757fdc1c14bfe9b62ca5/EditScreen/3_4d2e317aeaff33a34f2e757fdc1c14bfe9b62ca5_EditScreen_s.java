 package edu.berkeley.cs160.smartnature;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Color;
 import android.graphics.Rect;
 import android.os.Bundle;
 import android.os.Handler;
 import android.preference.PreferenceManager;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.animation.Animation;
 import android.view.animation.ScaleAnimation;
 import android.widget.Button;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 import android.widget.ZoomControls;
 
 public class EditScreen extends Activity implements View.OnTouchListener, View.OnClickListener, ColorPickerDialog.OnColorChangedListener {	
 	final int ZOOM_DURATION = 3000;
 	Garden mockGarden;
 	ZoomControls zoom;
 	EditView editView;
 	Handler mHandler = new Handler();
 	boolean showLabels = true, showFullScreen, dragPlot = false, rotateMode = false, zoomAutoHidden;
 	int zoomLevel;
 	SeekBar sb_rotation;
 	Plot newPlot, oldPlot;
 	Button dragButton, rotateButton, saveButton;
 	TextView mode_drag, mode_rotate;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		showFullScreen = getSharedPreferences("global", Context.MODE_PRIVATE).getBoolean("garden_fullscreen", false); 
 		if (showFullScreen)
 			setTheme(android.R.style.Theme_Light_NoTitleBar_Fullscreen);
 		super.onCreate(savedInstanceState);
 		Bundle extras = getIntent().getExtras();
 
 		mockGarden = StartScreen.gardens.get(extras.getInt("id"));
 		setTitle(extras.getString("name") + " (Edit mode)"); 
 
 		if(extras.containsKey("type")){
 			if(extras.getString("type").equalsIgnoreCase("ellipse")) {
 				Rect bounds = new Rect(140, 120, 210, 190);
 				newPlot = new Plot(extras.getString("name"), bounds, 0, Plot.OVAL);
 				newPlot.getShape().getPaint().setStrokeWidth(7);
 			}
 			else if(extras.getString("type").equalsIgnoreCase("rectangle")) {
 				Rect bounds = new Rect(40, 60, 90, 200);
 				newPlot = new Plot(extras.getString("name"), bounds, 0, Plot.RECT);
 			}
 			else {
 				Rect bounds = new Rect(270, 120, 270 + 90, 120 + 100);
 				float[] pts = { 0, 0, 50, 10, 90, 100 };
 				newPlot = new Plot(extras.getString("name"), bounds, 0, pts);
 			}
 			mockGarden.addPlot(newPlot);
 		}
 		else {
 			newPlot = mockGarden.getPlots().get(extras.getInt("plot_name"));
 			if(newPlot.getType() == 0 || newPlot.getType() == 1) {
 				oldPlot = new Plot(newPlot.getName(), newPlot.getShape().copyBounds(), newPlot.getAngle(), newPlot.getType());
 				oldPlot.setColor(newPlot.getShape().getPaint().getColor());
 			}
 			else {
 				oldPlot = new Plot(newPlot.getName(), newPlot.getShape().copyBounds(), newPlot.getAngle(), newPlot.getPoint());
 				oldPlot.setColor(newPlot.getShape().getPaint().getColor());
 			}
 		}
 
 
 		setContentView(R.layout.edit_plot);
 		editView = (EditView) findViewById(R.id.edit_view);
 		zoom = (ZoomControls) findViewById(R.id.edit_zoom_controls);
 		zoomAutoHidden = getSharedPreferences("global", Context.MODE_PRIVATE).getBoolean("zoom_autohide", false);
 		if (zoomAutoHidden)
 			zoom.setVisibility(View.GONE);
 		zoom.setOnZoomInClickListener(zoomIn);
 		zoom.setOnZoomOutClickListener(zoomOut);
 
 		boolean hintsOn = getSharedPreferences("global", Context.MODE_PRIVATE).getBoolean("show_hints", true);
 		if (hintsOn) {
 			((TextView)findViewById(R.id.edit_hint)).setText(R.string.hint_editscreen);
 			((TextView)findViewById(R.id.edit_hint)).setVisibility(View.VISIBLE);
 		}
 
 		sb_rotation = (SeekBar) findViewById(R.id.sb_rotation);
 		sb_rotation.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 			public void onStopTrackingTouch(SeekBar seekBar) {
 			}
 
 			public void onStartTrackingTouch(SeekBar seekBar) {
 			}
 
 			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
 				newPlot.setAngle(progress);
 				editView.invalidate();
 			}
 		});
 		
 		dragButton = (Button) findViewById(R.id.dragButton);
 		dragButton.setOnClickListener(drag);
 		rotateButton = (Button) findViewById(R.id.rotateButton);
 		rotateButton.setOnClickListener(rotate);
 		saveButton = (Button) findViewById(R.id.saveButton);
 		saveButton.setOnClickListener(save);
 		mode_drag = (TextView) findViewById(R.id.mode_drag);
 		mode_rotate = (TextView) findViewById(R.id.mode_rotate);
 		
 		editView.invalidate();
 	}
 
 	public void onBackPressed() {
 		Bundle extras = getIntent().getExtras();
 		if(extras.containsKey("type")) 
 			mockGarden.getPlots().remove(mockGarden.getPlots().size() - 1);
 		else {
 			newPlot.getShape().setBounds(oldPlot.getShape().getBounds());
 			newPlot.getShape().getPaint().setStrokeWidth(3);
 			newPlot.setColor(newPlot.getColor());
 			newPlot.getShape().getPaint().setColor(oldPlot.getColor());
 			newPlot.setAngle(oldPlot.getAngle());
 		}
 		mockGarden.refreshBounds();
 		finish();
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
 		case R.id.m_dragmode:
 			if(!dragPlot) {
 				dragPlot = true;
 				mode_drag.setText("Drag Mode is ON");
 			}
 			else {
 				dragPlot = false;
 				mockGarden.refreshBounds();
 				mode_drag.setText("Drag Mode is OFF");
 			}
 			break;
 
 		case R.id.m_rotatemode:
 			if(!rotateMode) {
 				sb_rotation.setVisibility(View.VISIBLE);
 				rotateMode = true;
 				mode_rotate.setText("Rotate Mode is OFF");
 			}
 			else
 			{
 				sb_rotation.setVisibility(View.INVISIBLE);
 				rotateMode = false;
 				mode_rotate.setText("Rotate Mode is ON");
 			}
 			break;
 
 		case R.id.m_change_color:
 			int color = PreferenceManager.getDefaultSharedPreferences(EditScreen.this).getInt("color",Color.WHITE);
 			new ColorPickerDialog(EditScreen.this, EditScreen.this, color).show();
 			break;
 
 		case R.id.m_save:
 			newPlot.getShape().getPaint().setStrokeWidth(3);
 			mockGarden.refreshBounds();
 			finish();
 		}
 
 
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public boolean onTouch(View view, MotionEvent event) {
 		System.err.println("touched");
 		return false;
 	}
 
 	@Override
 	public void onClick(View view) {
 		System.err.println("clicked");
 	}
 	
 	View.OnClickListener drag = new View.OnClickListener() {
 		@Override
 		public void onClick(View view) {
 			if(!dragPlot) {
 				dragPlot = true;
 				mode_drag.setText("Drag Mode is ON");
 			}
 			else {
 				dragPlot = false;
 				mockGarden.refreshBounds();
 				mode_drag.setText("Drag Mode is OFF");
 			}
 		}
 	};
 	
 	View.OnClickListener rotate = new View.OnClickListener() {
 		@Override
 		public void onClick(View view) {
 			if(!rotateMode) {
 				sb_rotation.setVisibility(View.VISIBLE);
 				rotateMode = true;
 				mode_rotate.setText("Rotate Mode is ON");
 			}
 			else
 			{
 				sb_rotation.setVisibility(View.INVISIBLE);
 				rotateMode = false;
 				mode_rotate.setText("Rotate Mode is OFF");
 			}
 		}
 	};
 	
 	View.OnClickListener save = new View.OnClickListener() {
 		@Override
 		public void onClick(View view) {
 			newPlot.getShape().getPaint().setStrokeWidth(3);
 			mockGarden.refreshBounds();
 			finish();
 		}
 	};
 
 	View.OnClickListener zoomIn = new View.OnClickListener() {
 		@Override
 		public void onClick(View view) {
 			handleZoom();
 			ScaleAnimation anim = new ScaleAnimation(1, 1.5f, 1, 1.5f, editView.getWidth() / 2.0f, editView.getHeight() / 2.0f);
 			anim.setDuration(400);
 			anim.setAnimationListener(new Animation.AnimationListener() {
 				@Override
 				public void onAnimationStart(Animation anim) { }
 				@Override
 				public void onAnimationRepeat(Animation anim) { }
 				@Override
 				public void onAnimationEnd(Animation anim) { zoomLevel++; }
 			});
 			editView.startAnimation(anim);
 		}
 	};
 
 	View.OnClickListener zoomOut = new View.OnClickListener() {
 		@Override
 		public void onClick(View view) {
 			handleZoom();
 			ScaleAnimation anim = new ScaleAnimation(1, 1/1.5f, 1, 1/1.5f, editView.getWidth() / 2.0f, editView.getHeight() / 2.0f); 
 			anim.setDuration(400);
 			anim.setAnimationListener(new Animation.AnimationListener() {
 				@Override
 				public void onAnimationStart(Animation anim) { }				
 				@Override
 				public void onAnimationRepeat(Animation anim) { }
 				@Override
 				public void onAnimationEnd(Animation anim) { zoomLevel--; }
 			});
 			editView.startAnimation(anim);
 		}
 	};
 
 	public void handleZoom() {
 		mHandler.removeCallbacks(autoHide);
 		if (!zoom.isShown())
 			zoom.show(); //zoom.setVisibility(View.VISIBLE);
 		mHandler.postDelayed(autoHide, ZOOM_DURATION);
 	}
 
 	public boolean getDragPlot() {
 		return dragPlot;
 	}
 
 	Runnable autoHide = new Runnable() {
 		@Override
 		public void run() {
 			if (zoom.isShown()) {
 				mHandler.removeCallbacks(autoHide);
 				zoom.hide(); //zoom.setVisibility(View.GONE);
 			}
 		}
 	};
 
 	@Override
 	public void colorChanged(int color) {
 		PreferenceManager.getDefaultSharedPreferences(this).edit().putInt("color", color).commit();
 		newPlot.getShape().getPaint().setColor(color);
 		editView.invalidate();
 	}
 
 }
