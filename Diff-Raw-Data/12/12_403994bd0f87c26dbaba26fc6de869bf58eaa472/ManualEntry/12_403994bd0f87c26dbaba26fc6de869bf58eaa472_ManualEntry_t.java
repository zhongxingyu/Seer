 package com.babyswipes;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 public class ManualEntry extends BaseActivity {
 
    private BabySwipesDB mDataBase;

	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_manual_entry);
         getActionBar().setDisplayHomeAsUpEnabled(true);
         
        mDataBase = new BabySwipesDB(getBaseContext());
        
         ImageView diaperImg = (ImageView) findViewById(R.id.image_man_diaper);
         diaperImg.setClickable(true);
         diaperImg.setOnClickListener(new OnClickListener() {
                     @Override
                     public void onClick(View v) {
                    	recordData("Diaper Change");
                     }
         });
         
         ImageView feedingImg = (ImageView) findViewById(R.id.image_man_feeding);
         feedingImg.setClickable(true);
         feedingImg.setOnClickListener(new OnClickListener() {
                     @Override
                     public void onClick(View v) {
                     	recordData("Feeding");
                     }
         });
         
         ImageView medicineImg = (ImageView) findViewById(R.id.image_man_medicine);
         medicineImg.setClickable(true);
         medicineImg.setOnClickListener(new OnClickListener() {
                     @Override
                     public void onClick(View v) {
                     	recordData("Medicine");
                     }
         });
         
         ImageView napImg = (ImageView) findViewById(R.id.image_man_nap);
         napImg.setClickable(true);
         napImg.setOnClickListener(new OnClickListener() {
                     @Override
                     public void onClick(View v) {
                     	recordData("Nap");
                     }
         });
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_manual_entry, menu);
         return true;
     }
     
     private void recordData(String activity){
    	mDataBase.addTagType(activity);
     	Intent i = new Intent(this, TagActivity.class);
     	i.putExtra("tag", activity);
     	startActivity(i);
     }
 
 }
