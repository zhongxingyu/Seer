 package com.kaist.crescendo.activity;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.Window;
 import android.widget.Button;
 
 import com.kaist.crescendo.R;
 import com.kaist.crescendo.activity.UpdateActivity;
 import com.kaist.crescendo.utils.MyStaticValue;
 
 public class MainActivity extends UpdateActivity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.activity_main);
 		
 		findViewById(R.id.main_plans_list).setOnClickListener(mClickListener);
 		findViewById(R.id.main_friends_list).setOnClickListener(mClickListener);
 		findViewById(R.id.main_manage_settings).setOnClickListener(mClickListener);
 		findViewById(R.id.main_view_status).setOnClickListener(mClickListener);
 		findViewById(R.id.main_manage_widget).setOnClickListener(mClickListener);
 		
 		MyStaticValue.myId = getMyID();
 	} 
 	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
 	
 	/* handling buttons */
 	Button.OnClickListener mClickListener = new View.OnClickListener()
 	{
 	      public void onClick(View v)
 	      {
 	    	  Intent intent = new Intent();
 	           switch (v.getId())
 	           {
 	           		case R.id.main_plans_list:
 	           			intent.setClass(getApplicationContext(), PlanListActivity.class);
 	           			break;
 	           		case R.id.main_friends_list:
 	           			intent.setClass(getApplicationContext(), FriendsListActivity.class);
 	           			break;
 	           		case R.id.main_manage_settings:
 	           			intent.setClass(getApplicationContext(), SettingsActivity.class);
 	           			break;
 	           		case R.id.main_manage_widget:
 	           			intent.setClass(getApplicationContext(), AvataEditorActivity.class);
 	           			break;
 	           		case R.id.main_view_status:
 	           			intent.setClass(getApplicationContext(), StatusActivity.class);
 	           			break;
 	           		default:
 	           			break;
 	           }
 	           startActivity(intent);
 	      }
 	};
 	
 	
 
 }
