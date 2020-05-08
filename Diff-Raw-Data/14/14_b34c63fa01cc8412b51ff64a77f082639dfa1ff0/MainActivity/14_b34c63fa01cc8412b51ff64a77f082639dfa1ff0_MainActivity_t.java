 package com.example.grubber;
 
 import com.example.grubber.R;
 
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MenuItem.OnMenuItemClickListener;
 import android.view.View;
 import android.widget.SearchView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity  {
 	
 	public final Context context = this;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);        
     }
     
     @Override
     public void onResume() {
     	super.onResume();
     	//Refresh the options menu when this activity comes in focus
     	invalidateOptionsMenu();
     }
 
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main, menu);
                       
         //Change profile button to login/register if they are not logged in
         if(SaveSharedPreference.getUserId(MainActivity.this) == 0)
         {
             MenuItem profileItem = menu.findItem(R.id.action_profile);
         	profileItem.setTitle(R.string.login);
             Toast.makeText(this,"Not logged in",Toast.LENGTH_SHORT).show();
         }
         else {
         	MenuItem signout = menu.findItem(R.id.action_signout);
         	signout.setVisible(true);
             signout.setOnMenuItemClickListener(new OnMenuItemClickListener() {
             	public boolean onMenuItemClick(MenuItem item) {            		        	
         			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
         			alertDialogBuilder.setTitle(R.string.logout_msg);
         			alertDialogBuilder.setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
         				public void onClick(DialogInterface dialog,int id) {            					    							
 							int tempUserName = SaveSharedPreference.getUserId(context);    			        		
 			        		dialog.cancel();    			        		
 			        		SaveSharedPreference.setUserId(context, 0);
         					Toast.makeText(context ,tempUserName + " logged out" , Toast.LENGTH_SHORT).show();
         					invalidateOptionsMenu();
 						}    						
 					}).setNegativeButton("No", new DialogInterface.OnClickListener() {
     					public void onClick(DialogInterface dialog,int id) {    						
     						dialog.cancel();    					
     					}}
     				  );            		
             		AlertDialog alertDialog = alertDialogBuilder.create();
             		alertDialog.show();   
             		return true;            		
             	} 	
             });        	
         }        
         return true;
       } 					
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
       switch (item.getItemId()) {
 	      case R.id.menu_search:
 	    	  Intent intent = new Intent(this, Results.class);
 	    	  intent.putExtra("key", "taco");
 	    	  startActivity(intent);
 	    	  break;
 	      case R.id.action_nearby:
 	    	  Intent intent2 = new Intent(this, Results.class);
 	    	  //get long lat 
 	    	  LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE); 
 	    	  Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 	    	  intent2.putExtra("longitude", location.getLongitude());
 	    	  intent2.putExtra("latitude", location.getLatitude());			
 	    	  startActivity(intent2);   
 	    	  break;
 	      case R.id.action_profile:
	    	  if(SaveSharedPreference.getUserId(MainActivity.this) != 0){
	    		  Intent intent3 = new Intent(context, ProfileActivity.class);
	    		  startActivity(intent3);   
	    	  } else {
	    		  Intent intent3 = new Intent(context, LoginActivity.class);
	    		  startActivity(intent3);   
	    	  }
 	          break;        
 	      default:
 	    	  break;
       }
 
       return true;
     } 
 }
