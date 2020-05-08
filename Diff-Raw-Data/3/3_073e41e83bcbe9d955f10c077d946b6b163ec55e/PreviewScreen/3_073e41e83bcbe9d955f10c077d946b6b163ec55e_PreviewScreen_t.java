 package edu.chl.asciicam.activity;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 
 /**
  * 
  * This activity sets the taken picture as the background.
  * You can choose what to with you taken picture, 
  * save, convert or delete picture and take a new one.
  * @author Kryckans
  *
  */
 public class PreviewScreen extends Activity {
 
 	Button back_btn, save_pic_btn, convert_btn;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_preview_screen);
         back_btn = (Button) findViewById(R.id.back);
         save_pic_btn = (Button) findViewById(R.id.save);
         convert_btn = (Button) findViewById(R.id.convert);
     
         back_btn.setOnClickListener(new View.OnClickListener() {
     		
         	/*Using the Intent class to go back to the phones camera */
     		public void onClick(View v) {
    			finish();
     		}
     	});
         
 /**        save_pic_btn.setOnClickListener(new View.OnClickListener() {
     		
     		public void onClick(View v) {
     			Intent l = new Intent(PreviewScreen.this, .class);
     			startActivity(l);
     		}
     	});     ///////Dont know if need this intent
     */  
     
     
         convert_btn.setOnClickListener(new View.OnClickListener() {
     		// if click here, the picture will be converted
     		public void onClick(View v) {
     		}
     	});    //TODO convert picture to ascii
         
         
     }
 
 
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_preview_screen, menu);
         return true;
     }
 }
