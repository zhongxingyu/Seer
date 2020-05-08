 package com.wmbest.etherguide;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.RelativeLayout;
 import android.widget.RelativeLayout.LayoutParams;
 import android.widget.Spinner;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import com.adwhirl.AdWhirlLayout;
 
 public class EtherGuide extends Activity
 {
 	public static ImageView image;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
 		RelativeLayout layout = (RelativeLayout) findViewById(R.id.layout_main);
 		image = (ImageView) findViewById(R.id.image);
 
 		Spinner itemSpinner = (Spinner)findViewById(R.id.type);
 		ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this, R.array.guidelist, R.layout.guide_list_layout);
 		typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		itemSpinner.setAdapter(typeAdapter);
 		itemSpinner.setSelection(1);
 
 		itemSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
 			@Override
 			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
 				switch (position) {
 					case 0: EtherGuide.this.image.setImageResource(R.drawable.etherneta); break;
 					case 1: EtherGuide.this.image.setImageResource(R.drawable.ethernetb); break;
 					default: break;
 				}
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> parentView) {
 				// your code here
 			}
 		});
 
 		AdWhirlLayout adWhirlLayout = new AdWhirlLayout(this, "724a225f9e4041f3b2a8a8da2ab9d620");
 		RelativeLayout.LayoutParams adWhirlLayoutParams =
 			new RelativeLayout.LayoutParams(
 				LayoutParams.FILL_PARENT,
 				LayoutParams.WRAP_CONTENT);
 		adWhirlLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
 		layout.addView(adWhirlLayout, adWhirlLayoutParams);
 		layout.invalidate();
     }
 }
