 package com.example.locus;
 
 import java.io.IOException;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 
 import com.example.locus.core.CoreFacade;
 import com.example.locus.entity.Sex;
 import com.example.locus.entity.User;
 
 public class MainActivity extends Activity {
 
 	private CoreFacade core;
 	ImageView iv;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		CoreFacade.getInstance().setContext(this.getApplicationContext());
 
 		
 		Intent intent = getIntent();
 
 		if(intent.getExtras() == null){
 			// SLEEP 2 SECONDS HERE ...
 			iv = (ImageView)findViewById(R.id.mainImageView);
 			iv.setScaleType(ScaleType.FIT_XY);
 			iv.setImageResource(R.drawable.main);
 
 			Handler handler = new Handler(); 
 			handler.postDelayed(new Runnable() { 
 				public void run() { 
 
 					User user = CoreFacade.getInstance().getCurrentUser();
 					if(user == null){
 						Intent intent = new Intent(getApplicationContext(), MyProfile.class);
 						startActivity(intent);
 
 					}
 					else{
 						Intent listUser = new Intent(getApplicationContext(), Demo.class);
 						listUser.putExtra("userName", user.getName());
 						listUser.putExtra("latitude", ""+user.getLatitude());
 						listUser.putExtra("longitude", ""+user.getLongtitude());
 						listUser.putExtra("userName", user.getName());
 						String ipAdd;
 						try {
 							ipAdd = IPAddress.getIPAddress(true);
 
 							listUser.putExtra("IP", ipAdd);
 							if(user.getSex() == Sex.Female)
 								listUser.putExtra("sex", "Female");
 							else
 								listUser.putExtra("sex", "Male");
 							listUser.putExtra("interests", user.getInterests());
 						}
 						catch (IOException e) {
 							e.printStackTrace();
 						}
 						startActivity(listUser);
 					}
 				} 
 			}, 2000); 
 		}
 		else{
 			User user = CoreFacade.getInstance().getCurrentUser();
 			if(user == null){
 				Intent intent1 = new Intent(this, MyProfile.class);
 				startActivity(intent1);
 
 			}
 			else{
 				Intent listUser = new Intent(this, Demo.class);
 				listUser.putExtra("userName", user.getName());
 				listUser.putExtra("latitude", ""+user.getLatitude());
 				listUser.putExtra("longitude", ""+user.getLongtitude());
 				listUser.putExtra("userName", user.getName());
 				String ipAdd;
 				try {
 					ipAdd = IPAddress.getIPAddress(true);
 
 					listUser.putExtra("IP", ipAdd);
 					if(user.getSex() == Sex.Female)
 						listUser.putExtra("sex", "Female");
 					else
 						listUser.putExtra("sex", "Male");
 					listUser.putExtra("interests", user.getInterests());
 				}
 				catch (IOException e) {
 					e.printStackTrace();
 				}
 				startActivity(listUser);
 			}
 		}
 
 	}
 
 
 
 }
