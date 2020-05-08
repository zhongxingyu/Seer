 package com.heavyplayer.tooltip;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.LinearLayout;
 
 import java.util.Random;
 
 public class ButtonsActivity extends Activity {
     public static final String LOG_TAG = ButtonsActivity.class.getSimpleName();
 
     String mText = "A simple test tooltip";
    Menu mMenu;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_buttons);
 
         // Add a few buttons
         LinearLayout container = (LinearLayout)findViewById(R.id.container);
         for(int i = 0; i < 5; i++) {
             LinearLayout row = new LinearLayout(this);
             row.setOrientation(LinearLayout.VERTICAL);
             container.addView(row);
 
             LinearLayout.LayoutParams rowParams = (LinearLayout.LayoutParams)row.getLayoutParams();
             rowParams.width = 0;
             rowParams.weight = 1;
 
             for(int j = 0; j < 5; j++) {
                 Button button = (Button)getLayoutInflater().inflate(R.layout.button, row, false);
                 row.addView(button);
 
                 LinearLayout.LayoutParams buttonParams = (LinearLayout.LayoutParams)button.getLayoutParams();
                 buttonParams.height = 0;
                 buttonParams.weight = 1;
             }
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.test, menu);
        mMenu = menu;
         return true;
     }
 
     @SuppressLint("NewApi")
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if(item.getItemId() == R.id.random) {
             Random random = new Random();
             Tooltip tooltip = new Tooltip(this);
             tooltip.setColor(Color.WHITE);
             tooltip.setText(mText);
             tooltip.setTextColor(Color.BLACK);
             tooltip.setTarget(random.nextInt(getWindow().getDecorView().getWidth()), random.nextInt(getWindow().getDecorView().getHeight()));
             tooltip.show();
         }
         else {
             Tooltip tooltip = new Tooltip(this);
             tooltip.setColor(Color.RED);
             tooltip.setText(mText);
             tooltip.setTextColor(Color.WHITE);
            tooltip.setTarget(mMenu, item.getItemId());
             tooltip.show();
         }
 
         return true;
     }
 
     public void showTooltip(View view) {
         Tooltip tooltip = new Tooltip(this);
         tooltip.setColor(Color.BLUE);
         tooltip.setText(mText);
         tooltip.setTextColor(Color.WHITE);
         tooltip.setTarget(view);
         tooltip.show();
     }
 }
