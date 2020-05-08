 package com.example.payback;
 
 
 import java.util.ArrayList;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 
 public class MainActivity extends TitleActivity
 {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	 {
 		super.onCreate(savedInstanceState);
 		modifyTitle("Main Menu",R.layout.activity_main);
 		
 		Intent broadcastIntent = new Intent();
     	broadcastIntent.setAction("com.Payback.StayLoggedIn_Intent");
     	sendBroadcast(broadcastIntent);
 	}
 	/*
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) 
 	{
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	*/
     public void CreateTrsn(View view)
    {
    	user.setnoneselected();
    	
 		int transCostInt = 0;
     	String transCommentString = "";
     	ArrayList<Friend> transselected = new ArrayList<Friend>();
  	    int translenderamountInt = 0;
  	    ArrayList<Integer> lendsharelist = new ArrayList<Integer>();
  	    boolean button1Selected = true;
  	    boolean button2Selected = false;
     	
     	Intent intent = new Intent(this, Transaction1Activity.class);
  	    Bundle Bundle = new Bundle();
         
         Bundle.putInt("Transaction1transCost", transCostInt);
         Bundle.putString("Transaction1transComment", transCommentString);
         Bundle.putParcelableArrayList("Transaction2selected", transselected);
         Bundle.putInt("Transaction3lenderamount", translenderamountInt);
         Bundle.putIntegerArrayList("Transaction3borroweramountlist", lendsharelist);
         Bundle.putBoolean("Transaction3button1Selected", button1Selected);
         Bundle.putBoolean("Transaction3button2Selected", button2Selected);
     
         intent.putExtras(Bundle);
         startActivity(intent);
     }
     
     public void ResolveTrsn(View view)
     {
     	Intent intent = new Intent(this, ResolveTransactionActivity.class);
         startActivity(intent);
     }
     
     public void Contact(View view)
     {
     	Intent intent = new Intent(this, ContactActivity.class);
         startActivity(intent);
     }
     
     public void ManageGroup(View view)
     {
     	Intent intent = new Intent(this, ManageGroupActivity.class);
         startActivity(intent);
     }
     
     public void Notification(View view)
     {
     	Intent intent = new Intent(this, NotificationActivity.class);
         startActivity(intent);
     }
     
     public void Statistic(View view)
     {
     	Intent intent = new Intent(this, StatisticActivity.class);
         startActivity(intent);
     }
     
     public void MainLogout(View view)
     {
     	Intent broadcastIntent = new Intent();
     	broadcastIntent.setAction("com.Payback.Logout_Intent");
     	sendBroadcast(broadcastIntent);
 
         this.finish(); //activity is done and should be closed
     }
     
     public void Setting(View view)
     {
     	Intent intent = new Intent(this, SettingsActivity.class);
         startActivity(intent);
     }
 
 }
