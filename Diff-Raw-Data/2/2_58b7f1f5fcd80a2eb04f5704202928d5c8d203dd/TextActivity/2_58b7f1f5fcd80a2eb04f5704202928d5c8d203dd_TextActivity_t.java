 package com.portfolio.activities;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.view.animation.AccelerateInterpolator;
 import android.view.animation.Animation;
 import android.view.animation.TranslateAnimation;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.ViewFlipper;
 
 import com.portfolio.R;
 import com.portfolio.components.menu;
 
 public class TextActivity extends Activity {
 
         private Button buttonMenu;
         ViewFlipper flipper;
         Button buttonItem1;
         Button buttonItem2;
         Button buttonItem3;
         Button buttonItem4;
         Button buttonItem5;
         Button buttonItem6;
         Button buttonItem7;
         
         @Override
         protected void onCreate(Bundle savedInstanceState) {
                 
                 super.onCreate(savedInstanceState);
                 requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
                 setContentView(R.layout.activity_text);
                 Bundle bundle = this.getIntent().getExtras();
 
                 //String text = bundle.getString("text");
                 //TextView textView = (TextView) findViewById(R.id.text);
                 //textView.setText(text);
                Typeface tf = Typeface.createFromAsset(getAssets(),"fonts/CopperGothicStd29AB.otf");
                 TextView tv = (TextView) findViewById(R.id.tittle_app);
                 tv.setTypeface(tf);
         
                 flipper = (ViewFlipper) findViewById(R.id.flipper);
                 final menu menuLayout = (menu) findViewById(R.id.layout_menu);
                 menuLayout.init();
                 buttonMenu = (Button) findViewById(R.id.buttonMenu);
                 buttonMenu.setOnClickListener(new OnClickListener() {
 	                public void onClick(View v) {
 	                        flipper.setInAnimation(inFromRightAnimation());
 	                        flipper.setOutAnimation(outToLeftAnimation());
 	                        flipper.showNext();     
 	                }
                 });
                 //BuFETE
                 buttonItem1 = (Button) findViewById(R.id.itemMenu1);
 		        buttonItem1.setOnClickListener(new OnClickListener() {
 		        @Override
 		            public void onClick(View v) {
 		                        startActivity(new Intent(getApplicationContext(), PhotoTittleTextActivity.class));
 		                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
 		                    }
 		            });
 		        //Socios
 		        buttonItem2 = (Button) findViewById(R.id.itemMenu2);
 		        buttonItem2.setOnClickListener(new OnClickListener() {
 		        @Override
 		            public void onClick(View v) {
 		                        startActivity(new Intent(getApplicationContext(), PhotoTextListTwoRowsActivity.class));
 		                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
 		                    }
 		            });
 		        //Especialistas en: despliega el submenu
 		        buttonItem3 = (Button) findViewById(R.id.itemMenu2);
 		        buttonItem3.setOnClickListener(new OnClickListener() {
 		        @Override
 		            public void onClick(View v) {
 		                        startActivity(new Intent(getApplicationContext(), PhotoTextListTwoRowsActivity.class));
 		                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
 		                    }
 		            });
 		        //Servicios? seguramente despliega otro submenu tambien, consultar!
 		        buttonItem4 = (Button) findViewById(R.id.itemMenu2);
 		        buttonItem4.setOnClickListener(new OnClickListener() {
 		        @Override
 		            public void onClick(View v) {
 		                        startActivity(new Intent(getApplicationContext(), PhotoTextListTwoRowsActivity.class));
 		                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
 		                    }
 		            });
 		        //Testimonios
 		        buttonItem5 = (Button) findViewById(R.id.itemMenu2);
 		        buttonItem5.setOnClickListener(new OnClickListener() {
 		        @Override
 		            public void onClick(View v) {
 		                        startActivity(new Intent(getApplicationContext(), PhotoTextListActivity.class));
 		                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
 		                    }
 		            });
 		        //Redes sociales
 		        buttonItem6 = (Button) findViewById(R.id.itemMenu2);
 		        buttonItem6.setOnClickListener(new OnClickListener() {
 		        @Override
 		            public void onClick(View v) {
 		                        startActivity(new Intent(getApplicationContext(), NetworkActivity.class));
 		                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
 		                    }
 		            });
 		      //Contacto
 		        buttonItem7 = (Button) findViewById(R.id.itemMenu2);
 		        buttonItem7.setOnClickListener(new OnClickListener() {
 		        @Override
 		            public void onClick(View v) {
 		                        startActivity(new Intent(getApplicationContext(), ContactActivity.class));
 		                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
 		                    }
 		            });
 		        
         
 
         }
 
         @Override
         public boolean onCreateOptionsMenu(Menu menu) {
                 // Inflate the menu; this adds items to the action bar if it is present.
                 getMenuInflater().inflate(R.menu.main, menu);
                 return true;
         }
         private Animation inFromRightAnimation() {
         
                 Animation inFromRight = new TranslateAnimation(
                 Animation.RELATIVE_TO_PARENT,  +1.0f, Animation.RELATIVE_TO_PARENT,  0.0f,
                 Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f);
                 inFromRight.setDuration(500);
                 inFromRight.setInterpolator(new AccelerateInterpolator());
                 
                 return inFromRight;
                 
         }
         private Animation outToLeftAnimation() {
                 
                 Animation outtoLeft = new TranslateAnimation(
                 Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,  -1.0f,
                 Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f);
                 outtoLeft.setDuration(500);
                 outtoLeft.setInterpolator(new AccelerateInterpolator());
                 
                 return outtoLeft;
         }
         private Animation inFromLeftAnimation() {
         
                 Animation inFromLeft = new TranslateAnimation(
                 Animation.RELATIVE_TO_PARENT,  -1.0f, Animation.RELATIVE_TO_PARENT,  0.0f,
                 Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f);
                 inFromLeft.setDuration(500);
                 inFromLeft.setInterpolator(new AccelerateInterpolator());
                 
                 return inFromLeft;
         }
                 
         private Animation outToRightAnimation() {
                 
                 Animation outtoRight = new TranslateAnimation(
                 Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,  +1.0f,
                 Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f);
                 
                 outtoRight.setDuration(500);
                 outtoRight.setInterpolator(new AccelerateInterpolator());
                 
                 return outtoRight;
         }
         @Override
         public void onBackPressed() {
             // TODO Auto-generated method stub
             super.onBackPressed();
             overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
         }
         
 
 }
