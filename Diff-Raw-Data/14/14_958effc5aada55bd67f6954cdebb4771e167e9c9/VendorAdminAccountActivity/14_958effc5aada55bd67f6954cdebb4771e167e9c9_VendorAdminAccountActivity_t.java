 package com.intuitive.yummy.activities;
 
 import java.util.ArrayList;
 
 import com.intuitive.yummy.R;
 import com.intuitive.yummy.R.id;
 import com.intuitive.yummy.R.layout;
 import com.intuitive.yummy.R.menu;
 import com.intuitive.yummy.models.MenuItem;
 import com.intuitive.yummy.models.Vendor;
 import com.intuitive.yummy.models.Vendor.VendorStatus;
 
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.BitmapFactory;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class VendorAdminAccountActivity extends Activity {
 	private Vendor vendor = new Vendor(1, "Jack's Pizza", "We sell Pizzas!", "123 Main St", new int[][] {{830,1700}, {830,1700}, {830,1700}, {830,1700}, {830,1700}, {0,0}, {0,0}}, VendorStatus.CLOSED, null, new com.intuitive.yummy.models.Menu(new ArrayList<MenuItem>()));
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_vendor_admin_account);
         Intent intent = getIntent();
         if (intent.getStringExtra("Activity").equals("Edit"))
         	vendor = (Vendor)intent.getSerializableExtra("Vendor");
 		
         ImageView vendorPicture = (ImageView) findViewById(R.id.adminVendorPicture);
         if (vendor.getPictureUrl() != null) {
         	vendorPicture.setImageBitmap(BitmapFactory.decodeFile(vendor.getPictureUrl()));
         }
         TextView vendorName = (TextView)findViewById(R.id.adminVendorName);
 		vendorName.setText(vendor.getName());
 		TextView vendorAddress = (TextView)findViewById(R.id.adminVendorAddress);
 		vendorAddress.setText(vendor.getLocation());
 		TextView vendorStatus = (TextView)findViewById(R.id.adminVendorStatus);
 		if (vendor.getStatus() == VendorStatus.OPEN)
 			vendorStatus.setText("Open");
 		else
 			vendorStatus.setText("Close");
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_vender_admin_account, menu);
 		return true;
 	}
 	
 	public void editGeneralInfo(View v) {
     	Intent intent = new Intent(this, VendorEditActivity.class);
     	intent.putExtra("Vendor", (Parcelable) vendor);
     	intent.putExtra("VendorID", vendor.getID());
     	startActivity(intent);
 	}
 	
 	public void editMenu(View v) {
 		Intent intent = new Intent(this, VendorEditMenuActivity.class);
 		intent.putExtra("VendorID", vendor.getID());
 		startActivity(intent);
 	}
 
 }
