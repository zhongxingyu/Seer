 package com.xmedic.troll;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.xmedic.troll.components.CountDown;
 import com.xmedic.troll.components.ScrollableImageView;
 import com.xmedic.troll.dialogs.FailDialog;
 import com.xmedic.troll.dialogs.SuccessDialog;
 import com.xmedic.troll.service.TrollService;
 import com.xmedic.troll.service.db.TrollServiceSqlLite;
 import com.xmedic.troll.service.model.City;
 import com.xmedic.troll.service.model.Level;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.Point;
 import android.graphics.Typeface;
 import android.opengl.Visibility;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.SystemClock;
 import android.util.Log;
 import android.view.Display;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 public class TrucknrollAndroidActivity extends Activity {
 
 	private Button button1;
 	private Button button2;
 	private Button button3;
 	private Button button4;
 	private ScrollableImageView mapView;
 	private TextView goalView;
 	private TextView timeLeftView;
 	
 	private Level level;
 	private TrollService service;
 	
 	private View.OnClickListener citySelectedListener;
 	private CountDown counter;
 	
 	private SuccessDialog successDialog;
 	private FailDialog failDialog;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
         setContentView(R.layout.main);
         service = new TrollServiceSqlLite(getBaseContext());
         
         String levelId = getIntent().getExtras().getString(HomeScreenActiity.LEVEL_ID);
         level = service.getLevel(levelId);
         
         loadComponents();
         initGraphics();
         
         moveToCity(service.getCity(level.getStartCityId()));
         
         City goal = service.getCity(level.getGoalCityId());
         goalView.setText("Goal: "  + goal.getName());
         mapView.setGoalCity(goal);
 
         counter = new CountDown(10000,1000, timeLeftView);
         counter.start();
         counter.setOnFinishListener(new CountDown.OnCounterFinishListener() {	
 			public void finished() {
 				failDialog.show();
 			}
 		});
         
        successDialog =  new SuccessDialog(this, levelId);
         
         Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/fixed.ttf");
         button1.setTypeface(tf);
         button2.setTypeface(tf);
         button3.setTypeface(tf);
         button4.setTypeface(tf);
         
         goalView.setTypeface(tf);
     	timeLeftView.setTypeface(tf);
     }
 
 	private void moveToCity(City city) {
 		
 		hideButtons();		
 	
 		int index = 0;
 		mapView.setCenter(city, this);
 		mapView.setNearest(null);
 		
 		if(city.getId().equals(level.getGoalCityId())) {
 			counter.cancel();
 			timeLeftView.setTextColor(Color.GREEN);
 			successDialog.show();
 			return;
 		}
 		
 		List<City> nearestCities = service.getNearbyCities(city.getId(), level.getGoalCityId());
 		for(City nearestCity : nearestCities) {
 			setChoice(nearestCity, index);	
 			index++;
 		}
 		mapView.setNearest(nearestCities);
 	}
 
 	private void hideButtons() {
 		button1.setVisibility(View.INVISIBLE);
 		button2.setVisibility(View.INVISIBLE);
 		button3.setVisibility(View.INVISIBLE);
 		button4.setVisibility(View.INVISIBLE);
 	}
 
 	private void setChoice(City city, int index) {
 		Button buttonToUse = null;
 		if(index == 0) {
 			buttonToUse =  button3;
 		} else  if(index == 1) {
 			buttonToUse = button4;
 		} else   if(index == 2) {
 			buttonToUse = button1;
 		} else  if(index == 3) {
 			buttonToUse = button2;
 		} 
 		
 		if(buttonToUse != null) {
 			buttonToUse.setText(city.getName());
 			buttonToUse.setTag(city.getId());
 			buttonToUse.setVisibility(View.VISIBLE);
 		}
 	}
 	
 	private void citySelected(String cityId) {
 		moveToCity(service.getCity(cityId));
 		
 	}
 
 	private void loadComponents() {
 	
 		citySelectedListener = new OnClickListener() {
 			
 			public void onClick(View v) {
 				citySelected(v.getTag().toString());
 			}
 		};
 		
        button1 = (Button)findViewById(R.id.button1);
        button2 = (Button)findViewById(R.id.button2);
        button3 = (Button)findViewById(R.id.button3);
        button4 = (Button)findViewById(R.id.button4);
        button1.setOnClickListener(citySelectedListener);
        button2.setOnClickListener(citySelectedListener);
        button3.setOnClickListener(citySelectedListener);
        button4.setOnClickListener(citySelectedListener);
        
        goalView = (TextView)findViewById(R.id.targetcity);
        
        mapView = (ScrollableImageView)findViewById(R.id.map);
        
        timeLeftView = (TextView)findViewById(R.id.timeleftlabel);
        
        successDialog =  new SuccessDialog(this, level.getId()); 
        failDialog =  new FailDialog(this,level.getId());
 	}
 
 	private void initGraphics() {
         Display d = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
         mapView.setScreenSize(d.getWidth(), d.getHeight());
 	}
 }
