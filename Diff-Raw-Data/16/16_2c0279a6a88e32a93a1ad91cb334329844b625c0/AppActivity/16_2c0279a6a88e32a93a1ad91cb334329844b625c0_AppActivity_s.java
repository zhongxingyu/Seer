 package de.codenauts.hockeyapp;
 
import de.codenauts.hockeyapp.util.ActivityHelper;
import de.codenauts.hockeyapp.util.UIUtils;
 import net.hockeyapp.android.UpdateActivity;
 import android.app.ActionBar;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.MenuItem;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class AppActivity extends UpdateActivity {
   final ActivityHelper activityHelper = ActivityHelper.createInstance(this);
 
   private ImageLoader imageLoader;
 
   public void onCreate(Bundle savedInstanceState) {
     this.imageLoader = new ImageLoader(getApplicationContext());
 
     super.onCreate(savedInstanceState);
 
     setTitle(null);
     activityHelper.setupActionBar(null, Color.BLACK);
     
     if (UIUtils.isHoneycomb()) {
       ActionBar actionBar = getActionBar();
       actionBar.setDisplayHomeAsUpEnabled(true);
       actionBar.setDisplayShowTitleEnabled(false);
     }
   }
 
   protected void configureView() {
     super.configureView();
     
     ImageView iconView = (ImageView)findViewById(R.id.icon_view);
     try {
       String identifier = getIntent().getStringExtra("identifier");
       String url = "https://rink.hockeyapp.net/api/2/apps/" + identifier + "?format=png";
       imageLoader.displayImage(url, this, iconView);
     }
     catch (Exception e) {
       iconView.setImageBitmap(null);
     }
 
     TextView titleLabel = (TextView)findViewById(R.id.name_label);
     titleLabel.setText(getIntent().getStringExtra("title"));
     
     ImageButton downloadButton = (ImageButton)findViewById(R.id.update_button);
     downloadButton.setImageDrawable(getResources().getDrawable(R.drawable.button_download));
   }
   
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
       switch (item.getItemId()) {
           case android.R.id.home:
               Intent intent = new Intent(this, MainActivity.class);
               intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
               startActivity(intent);
               return true;
           default:
               return super.onOptionsItemSelected(item);
       }
   }
   
   public int getCurrentVersionCode() {
     return -1;
   }
   
   public int getLayout() {
     return R.layout.app_view;
   }
 }
