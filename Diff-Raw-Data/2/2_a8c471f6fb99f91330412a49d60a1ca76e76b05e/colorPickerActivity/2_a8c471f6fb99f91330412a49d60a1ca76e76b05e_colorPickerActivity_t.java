 //////////////////////////////////////////////////////////////////////////////
 // Copyright 2012 Matthew Egeler
 // 									       
 // Licensed under the Apache License, Version 2.0 (the "License");	       
 // you may not use this file except in compliance with the License.	       
 // You may obtain a copy of the License at				      
 // 									       
 //     http://www.apache.org/licenses/LICENSE-2.0			       
 // 									       
 // Unless required by applicable law or agreed to in writing, software      
 // distributed under the License is distributed on an "AS IS" BASIS,	       
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 // See the License for the specific language governing permissions and      
 // limitations under the License.					      
 //////////////////////////////////////////////////////////////////////////////
 
 package com.wentam.defcol.colorpicker;
 
 import com.wentam.defcol.R;
 
 import android.app.Activity;
 import android.os.Bundle;
 
 import android.widget.GridView;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.TextView;
 import android.view.MotionEvent;
 
 import android.view.LayoutInflater;
 import android.view.View;
 
 import android.content.Intent;
 import android.content.Context;
 
 import android.util.Log;
 import android.util.DisplayMetrics;
 
 import android.opengl.GLSurfaceView;
 
 import android.graphics.PixelFormat;
 import android.graphics.Color;
 import android.graphics.drawable.ColorDrawable;
 
 import android.content.res.Resources;
 import android.text.ClipboardManager;
 
 import android.view.ActionProvider;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.SubMenu;
 
 import android.widget.Toast;
 
 import java.lang.String;
 
 public class colorPickerActivity extends Activity
 {
     private String startingColor;
 
     private int color_id;
 
     private h_svGL h_sv;
 
     String color;
 
     private float[] hsvcolor;
     private int intcolor;
 
     private boolean colorSet = false;
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         menu.add("Copy color")
             .setIcon(R.drawable.icon_copy)
             .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
 
 	SubMenu subMenu1 = menu.addSubMenu("Mode");
         subMenu1.add("H|SV");
         subMenu1.add("More modes to come!");
 
         MenuItem subMenu1Item = subMenu1.getItem();
        // subMenu1Item.setIcon(R.drawable.list);
         subMenu1Item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
 
         return true;
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
 	// setTheme(com.actionbarsherlock.R.style.Theme_Sherlock);
         super.onCreate(savedInstanceState);
 
 	// get dpi 
 	DisplayMetrics metrics = new DisplayMetrics();
 	getWindowManager().getDefaultDisplay().getMetrics(metrics);
 	int dpi = metrics.densityDpi;
 
         setContentView(R.layout.colorpicker);
 	h_sv = (h_svGL) findViewById(R.id.h_sv);
 	h_sv.setDpi(dpi);
 
 	h_sv.getHolder().setFormat(PixelFormat.TRANSPARENT);       
 	
 	getActionBar().setDisplayHomeAsUpEnabled(true);
 		
 
 	// set color
        	Bundle extras = getIntent().getExtras();
 	
 	float hsv[] = new float[3];
 	int colorint = Integer.parseInt(extras.getString("startingColor"));
 
 	if (savedInstanceState != null && savedInstanceState.containsKey("colorInt")) {
 	    colorint = savedInstanceState.getInt("colorInt");
 	    intcolor = colorint;
 	    colorSet = true;
 	}
 
 	color_id = Integer.parseInt(extras.getString("color_id"));
 
 	getActionBar().setBackgroundDrawable(new ColorDrawable(colorint));
 	getActionBar().setTitle("#"+((Integer.toHexString(colorint)).substring(2)));
 	color = "#"+((Integer.toHexString(colorint)).substring(2));
 
 	int red = Color.red(colorint);
 	int green = Color.green(colorint);
 	int blue = Color.blue(colorint);
 
 	if (savedInstanceState != null && savedInstanceState.containsKey("color")) {
 	    hsv = savedInstanceState.getFloatArray("color");
 	    hsvcolor = hsv;
 	    colorSet = true;
 	} else {
 	    Color.RGBToHSV(red, green, blue, hsv);
 	}
 
 	Intent resultIntent = new Intent();
 	resultIntent.putExtra("hue",String.valueOf(hsv[0]));
 	resultIntent.putExtra("saturation",String.valueOf(hsv[1]));
 	resultIntent.putExtra("value",String.valueOf(hsv[2]));
 	resultIntent.putExtra("color_id",String.valueOf(color_id));
 	setResult(Activity.RESULT_OK, resultIntent);
 
 	h_sv.setColor(hsv);
 
 	h_sv.setOnColorChangeListener(new colorChangeListener(){
 		public void onColorChange(float[] _hsv) {
 		    colorSet = true;
 		    hsvcolor = _hsv;
 		    intcolor = Color.HSVToColor(_hsv);
 
 		    Intent resultIntent = new Intent();
 		    
 		    // _hsv[1] = reverseFloatWithRange(_hsv[1],0f, 1f);
 		    // _hsv[2] = reverseFloatWithRange(_hsv[2],0f, 1f);
 
 		    float tmp = new Float(_hsv[2]);
 		    _hsv[2] = new Float(_hsv[1]);
 		    _hsv[1] = tmp;
 
 		    resultIntent.putExtra("hue",String.valueOf(_hsv[0]));
 		    resultIntent.putExtra("saturation",String.valueOf(_hsv[1]));
 		    resultIntent.putExtra("value",String.valueOf(_hsv[2]));
 		    resultIntent.putExtra("color_id",String.valueOf(color_id));
 		    setResult(Activity.RESULT_OK, resultIntent);
 
          
 		    getActionBar().setBackgroundDrawable(new ColorDrawable(Color.HSVToColor(_hsv)));
 		    getActionBar().setTitle("#"+((Integer.toHexString(Color.HSVToColor(_hsv))).substring(2)));		   
 		    color = "#"+((Integer.toHexString(Color.HSVToColor(_hsv))).substring(2));
 		}
 	    });
      	
     }
 
     protected void onSaveInstanceState (Bundle state) {
 	if (colorSet) {
 	    state.putFloatArray("color",hsvcolor);
 	    state.putInt("colorInt",intcolor);
 	}
     }
 
     private float reverseFloatWithRange (float f, float min, float max) {
 	float middle;
 
 	if (min == 0f) {
 	    middle = max/2;
 	} else {
 	    middle = max/min; // (max-min)/2?
 	}
 	
 	if (f > middle) {
 	    f = f-((f-middle)*2);
 	} else if (f < middle) {
 	    f = f+((middle-f)*2);
 	}
 
 	return f;
     }
 
     @Override
     protected void onPause() {
 	super.onPause();
 	h_sv.onPause();
     }
 
     @Override
     protected void onResume() {
 	super.onResume();
 	h_sv.onResume();
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
 	if (item.getItemId() == android.R.id.home) {
 	    finish();
             return true;
 	} else if (item.toString() == "Copy color") {
 	    ClipboardManager clipboard =  (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
 	    clipboard.setText(color);
 	    Toast t = Toast.makeText(this, "Color copied to clipboard",
 			   Toast.LENGTH_SHORT);
 
 	    t.show();
 	    return true;
 	} else {
 	    return super.onOptionsItemSelected(item);
 	}
     }
 }
