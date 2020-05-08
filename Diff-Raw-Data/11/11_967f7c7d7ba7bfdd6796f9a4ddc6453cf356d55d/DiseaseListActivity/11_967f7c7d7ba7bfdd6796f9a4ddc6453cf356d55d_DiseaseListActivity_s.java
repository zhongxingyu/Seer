 package com.example.epidemicapp;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.view.Window;
 
 public class DiseaseListActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
         this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.activity_disease_list);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.disease_list, menu);
 		return true;
 	}
 	
 	public void clickDisease0(View view) {
 		Intent intent = new Intent(this, DiseaseDetailActivity.class);
 		Bundle b = new Bundle();
         b = toBundle(SplashScreenActivity.DB.getDiseaseName(0), 41, R.drawable.two_blue_tongues);
         intent.putExtra("Disease Bundle", b);
 	    startActivity(intent);
 	}
 	
 
 	public void clickDisease1(View view) {
 		Intent intent = new Intent(this, DiseaseDetailActivity.class);
 		Bundle b = new Bundle();
         b = toBundle(SplashScreenActivity.DB.getDiseaseName(1), 58, R.drawable.reggae_armpit);
         intent.putExtra("Disease Bundle", b);
 	    startActivity(intent);
 	}
 
 	public void clickDisease2(View view) {
 		Intent intent = new Intent(this, DiseaseDetailActivity.class);
 		Bundle b = new Bundle();
         b = toBundle(SplashScreenActivity.DB.getDiseaseName(2), 79, R.drawable.meat_tasting_fingertips);
         intent.putExtra("Disease Bundle", b);
 	    startActivity(intent);
 	}
 	
 	public void clickDisease3(View view) {
 		Intent intent = new Intent(this, DiseaseDetailActivity.class);
 		Bundle b = new Bundle();
         b = toBundle(SplashScreenActivity.DB.getDiseaseName(3), 42, R.drawable.white_eyeballs);
         intent.putExtra("Disease Bundle", b);
 	    startActivity(intent);
 	}
 	
 	private Bundle toBundle(String diseaseName, int percentage, int pid) {
 		Bundle b = new Bundle();
 		b.putString("disease name", diseaseName);
 		b.putString("percentage", Integer.toString(percentage));
		b.putString("picture id", Integer.toString(pid));
 		return b;
 	}
 
 }
