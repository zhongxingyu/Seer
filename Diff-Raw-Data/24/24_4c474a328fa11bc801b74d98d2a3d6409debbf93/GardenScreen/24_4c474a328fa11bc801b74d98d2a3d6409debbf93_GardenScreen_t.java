 package edu.berkeley.cs160.smartnature;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Path;
 import android.graphics.drawable.ShapeDrawable;
 import android.graphics.drawable.shapes.*;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.WindowManager;
 import android.view.animation.Animation;
 import android.view.animation.ScaleAnimation;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.ZoomControls;
 
 public class GardenScreen extends Activity implements View.OnTouchListener, View.OnClickListener {
 	
 	final int ZOOM_DURATION = 3000;
 	Garden mockGarden;
 	AlertDialog dialog;
 	ZoomControls zoom;
 	GardenLayout gardenLayout;
 	Handler mHandler = new Handler();
 	boolean showLabels = true;
 	int zoomLevel;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		boolean showFullScreen = getSharedPreferences("global", Context.MODE_PRIVATE).getBoolean("garden_fullscreen", false); 
 		if (showFullScreen)
 			setTheme(android.R.style.Theme_Light_NoTitleBar_Fullscreen);
 		super.onCreate(savedInstanceState);
 		Bundle extras = getIntent().getExtras();
		mockGarden = new Garden(R.drawable.preview, "");
 		if (extras != null && extras.containsKey("name")) {
 			setTitle(extras.getString("name"));
			mockGarden.setName(extras.getString("name"));
 			initMockData();
 		} else
 			showDialog(0);
 		setContentView(R.layout.garden);
 		gardenLayout = (GardenLayout) findViewById(R.id.garden_layout);
 		zoom = (ZoomControls) findViewById(R.id.zoom_controls);
 		zoom.setVisibility(View.GONE);
 		zoom.setOnZoomInClickListener(zoomIn);
 		zoom.setOnZoomOutClickListener(zoomOut);
 		
 		boolean hintsOn = getSharedPreferences("global", Context.MODE_PRIVATE).getBoolean("show_hints", true);
 		if (hintsOn) {
 			((TextView)findViewById(R.id.garden_hint)).setText(R.string.hint_gardenscreen);
 			((TextView)findViewById(R.id.garden_hint)).setVisibility(View.VISIBLE);
 		}
 	}
 	
 	public void initMockData() {
 		ShapeDrawable s1 = new ShapeDrawable(new RectShape());
 		s1.setBounds(40, 60, 90, 200);
 		ShapeDrawable s2 = new ShapeDrawable(new OvalShape());
 		s2.setBounds(140, 120, 210, 190);
 		mockGarden.addPlot(new Plot(s1, "Jerry's Plot"));
 		mockGarden.addPlot(new Plot(s2, "Amy's Plot"));
 		Path p = new Path();
 		p.lineTo(50, 10);
 		p.lineTo(90, 100);
 		p.close();
 		PathShape ps = new PathShape(p, 90, 100);
 		ShapeDrawable s3 = new ShapeDrawable(ps);
 		s3.setBounds(270, 120, 270 + 90, 120 + 100);
 		mockGarden.addPlot(new Plot(s3, "Shared Plot"));
 	}
 
 	@Override
 	public Dialog onCreateDialog(int id) {
 		LayoutInflater factory = LayoutInflater.from(this);
 		final View textEntryView = factory.inflate(R.layout.text_entry_dialog, null);
 		DialogInterface.OnClickListener confirmed = new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int whichButton) {
 				EditText gardenName = (EditText) textEntryView.findViewById(R.id.dialog_text_entry);
 				setTitle(gardenName.getText().toString());
				mockGarden.setName(gardenName.getText().toString());
 			}
 		};
 		DialogInterface.OnClickListener canceled = new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int whichButton) {
 				finish();
 			}
 		};
 		dialog = new AlertDialog.Builder(this)
 			.setTitle(R.string.new_garden_prompt)
 			.setView(textEntryView)
 			.setPositiveButton(R.string.alert_dialog_ok, confirmed)
 			.setNegativeButton(R.string.alert_dialog_cancel, canceled)
 			.create();
 		
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
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.garden_menu, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.m_home:
 				finish();
 				break;
 			case R.id.m_resetzoom:
 				zoomLevel = 0;
 				gardenLayout.reset();
 				break;
 			case R.id.m_share:
 				startActivity(new Intent(this, ShareGarden.class));
 				break;
 			case R.id.m_showlabels:
 				showLabels = !showLabels;
 				item.setTitle(showLabels ? "Hide labels" : "Show labels");
 				gardenLayout.invalidate();				
 				break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public boolean onTouch(View view, MotionEvent event) {
 		handleZoom();
 		return false;
 	}
 	
 	@Override
 	public void onClick(View view) {
 		//handleZoom();
 		System.out.println("clicked");
 	}
 	
 
 	View.OnClickListener zoomIn = new View.OnClickListener() {
 		@Override
 		public void onClick(View view) {
 			handleZoom();
 			ScaleAnimation anim = new ScaleAnimation(1, 1.5f, 1, 1.5f, gardenLayout.getWidth() / 2.0f, gardenLayout.getHeight() / 2.0f);
 			anim.setDuration(400);
 			anim.setAnimationListener(new Animation.AnimationListener() {
 				@Override
 				public void onAnimationStart(Animation anim) { }
 				@Override
 				public void onAnimationRepeat(Animation anim) { }
 				@Override
 				public void onAnimationEnd(Animation anim) { zoomLevel++; }
 			});
 			gardenLayout.startAnimation(anim);
 		}
 	};
 	
 	View.OnClickListener zoomOut = new View.OnClickListener() {
 		@Override
 		public void onClick(View view) {
 			handleZoom();
 			ScaleAnimation anim = new ScaleAnimation(1, 1/1.5f, 1, 1/1.5f, gardenLayout.getWidth() / 2.0f, gardenLayout.getHeight() / 2.0f); 
 			anim.setDuration(400);
 			anim.setAnimationListener(new Animation.AnimationListener() {
 				@Override
 				public void onAnimationStart(Animation anim) { }				
 				@Override
 				public void onAnimationRepeat(Animation anim) { }
 				@Override
 				public void onAnimationEnd(Animation anim) { zoomLevel--; }
 			});
 			gardenLayout.startAnimation(anim);
 		}
 	};
 	
 	public void handleZoom() {
 		mHandler.removeCallbacks(autoHide);
 		if (!zoom.isShown())
 			zoom.show(); //zoom.setVisibility(View.VISIBLE);
 		mHandler.postDelayed(autoHide, ZOOM_DURATION);
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
 }
