 package com.intuitive.yummy.activities;
 
 import com.intuitive.yummy.R;
 import com.intuitive.yummy.R.id;
 import com.intuitive.yummy.R.layout;
 import com.intuitive.yummy.R.menu;
 import com.intuitive.yummy.models.Vendor;
 import com.intuitive.yummy.models.Vendor.VendorStatus;
 import com.intuitive.yummy.webservices.IntentExtraKeys;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class VendorActivity extends Activity {
 	private Vendor vendor;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_vendor);
         
         //Get the data that is being passed by the parent activity, in this case, vendor
         Intent intent = getIntent();
         vendor = (Vendor) intent.getParcelableExtra("Vendor");
         
         String name = vendor.getName();
         String description = vendor.getDescription();
        
        int[][] hours = { {800, 1700}, {800, 1700}, {800, 1600}, {830, 1630}, {900, 1800}, {0, 0}, {0, 0} };
 
         //String address;
         String status;
         if (vendor.getStatus() == VendorStatus.OPEN)
         	status = "Open";
         else
         	status = "Closed";
         String hourOfOperation = "";
     	for (int i = 0; i < 7; i++)
     	{
    		//if (vendor.getHours() == null)	// for demo
    		if(hours == null)					//   purposes	
     			hourOfOperation = "Not Available";
     		else
     		{
	    		//int temp1 = vendor.getHours()[i][0];		
    			int temp1 = hours[i][0];					// for demo
 	    		int openHour = (int)Math.floor(temp1/100);
 	    		int openMinute = temp1%100;
	    		//int temp2 = vendor.getHours()[i][1];		
	    		int temp2 = hours[i][1];					// for demo
 	    		int closeHour = (int)Math.floor(temp2/100);
 	    		int closeMinute = temp2%100;
 	    		        		
 	    		if (openHour == 0 && openMinute == 0 && closeHour == 0 && closeMinute == 0)
 	    			hourOfOperation += "Closed";
 	    		else
 	    		{
 	    			String time = Integer.toString(openHour) + ":";
 	    			if (openMinute == 0)
 	    				time += "00";
 	    			else
 	    				time += Integer.toString(openMinute);
 	    			time += " - ";
 	    			time += Integer.toString(closeHour) + ":";
 	    			if (closeMinute == 0)
 	    				time += "00";
 	    			else
 	    				time += Integer.toString(closeMinute);
 	    			hourOfOperation += time;
 	    		}
 	    		if (i == 0)
 	    		{
 	    	        TextView mondayHour = (TextView) findViewById(R.id.mondayHour);
 	    	        mondayHour.setText(hourOfOperation);
 	    		}
 	    		else if (i == 1)
 	    		{
 	    			TextView tuesdayHour = (TextView) findViewById(R.id.tuesdayHour);
 	    	        tuesdayHour.setText(hourOfOperation);
 	    		}
 	    		else if (i == 2)
 	    		{
 	    			TextView wednesdayHour = (TextView) findViewById(R.id.wednesdayHour);
 	    	        wednesdayHour.setText(hourOfOperation);
 	    		}
 	    		else if (i == 3)
 	    		{
 	    			TextView thursdayHour = (TextView) findViewById(R.id.thursdayHour);
 	    	        thursdayHour.setText(hourOfOperation);
 	    		}
 	    		else if (i == 4)
 	    		{
 	    			TextView fridayHour = (TextView) findViewById(R.id.fridayHour);
 	    	        fridayHour.setText(hourOfOperation);
 	    		}
 	    		else if (i == 5)
 	    		{
 	    			TextView saturdayHour = (TextView) findViewById(R.id.saturdayHour);
 	    	        saturdayHour.setText(hourOfOperation);
 	    		}
 	    		else if (i == 6)
 	    		{
 	    			TextView sundayHour = (TextView) findViewById(R.id.sundayHour);
 	    	        sundayHour.setText(hourOfOperation);
 	    		}
 	    		hourOfOperation = "";
 	    	}
         }
         
         TextView vendorName = (TextView) findViewById(R.id.vendorName);
         vendorName.setText(name);
         
         TextView vendorDescription = (TextView) findViewById(R.id.vendorDescription);
         vendorDescription.setText(description);
         
         ImageView vendorPicture = (ImageView) findViewById(R.id.vendorPicture);
         if (vendor.getPictureUrl() != null) {
         	vendorPicture.setImageBitmap(BitmapFactory.decodeFile(vendor.getPictureUrl()));
         }
         
         TextView vendorStatus = (TextView) findViewById(R.id.currentStatus);
         vendorStatus.setText(status);
         
         Button menuButton = (Button) findViewById(R.id.button_menu);
 
         // we'll catch empty menus on the MenuActivity screen
         menuButton.setEnabled(true);
         
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_vendor, menu);
         return true;
     }
     
     public void viewMenu(View v){
     	Intent intent = new Intent(this, MenuActivity.class);
     	intent.putExtra(IntentExtraKeys.VENDOR, (Parcelable) vendor);
    	startActivity(intent);
    }
     public void viewReviews(View v){
     	Intent intent = new Intent(this, ReadReviewActivity.class);
     	intent.putExtra("vendorId", vendor.getId());
     	intent.putExtra("VendorName", vendor.getName());
     	intent.putExtra("Activity", "Vendor");
     	startActivity(intent);
    }
 }
