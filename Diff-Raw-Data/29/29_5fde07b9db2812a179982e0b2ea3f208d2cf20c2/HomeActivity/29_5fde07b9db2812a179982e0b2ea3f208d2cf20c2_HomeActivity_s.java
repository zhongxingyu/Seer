 package com.tldr;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import android.app.ActionBar;
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.app.FragmentTransaction;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Point;
 import android.os.Bundle;
 import android.speech.RecognizerIntent;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.Display;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.Window;
 import android.widget.Toast;
 
 import com.tldr.exlap.ConnectionHelper;
 import com.tldr.tools.ToolBox;
 
 public class HomeActivity extends FragmentActivity implements
 		ActionBar.OnNavigationListener {
 	private final static int SPEECH_REQUEST_CODE = 123;
 	private int currentMenu=R.id.menu_map;
 	private FragmentCommunicator currentFragment;
 	private ConnectionHelper connectionHelper;
 
 	Menu menu;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
 		setContentView(R.layout.activity_home);
 
 		// Set up the action bar.
 		final ActionBar actionBar = getActionBar();
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setIcon(R.drawable.logo);
		actionBar.setTitle("TL;DR");

 		FragmentManager fm = getFragmentManager();
 		FragmentTransaction transaction;
 		Fragment fragment = new MapFragment();
 		currentFragment=(FragmentCommunicator) fragment;
 		transaction = fm.beginTransaction();
 		transaction.replace(R.id.homeActivity, fragment);
 		transaction.commit();
 		
 		
 		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.home, menu);
 		this.menu = menu;
 		menu.findItem(R.id.menu_map).setIcon(R.drawable.map_pressed);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		Fragment fragment;
 		FragmentManager fm = getFragmentManager();
 		FragmentTransaction transaction;
 		Log.e("com.tldr.homeActivity", "menuitem selected: " + item.toString());
 		switch (item.getItemId()) {
 		case R.id.menu_map:
 			item.setIcon(R.drawable.map_pressed);
 			menu.findItem(R.id.menu_community).setIcon(R.drawable.community);
 			menu.findItem(R.id.menu_tasks).setIcon(R.drawable.tasks);
 			fragment = new MapFragment();
 			fragment.setArguments(new Bundle());
 			currentFragment=(FragmentCommunicator) fragment;
 			transaction = fm.beginTransaction();
 			transaction.replace(R.id.homeActivity, fragment);
 			transaction.commit();
 			currentMenu=item.getItemId();
 			break;
 		case R.id.menu_tasks:
 			item.setIcon(R.drawable.tasks_pressed);
 			menu.findItem(R.id.menu_community).setIcon(R.drawable.community);
 			menu.findItem(R.id.menu_map).setIcon(R.drawable.map);
 			fragment = new TasksFragment();
 			fragment.setArguments(new Bundle());
 			transaction = fm.beginTransaction();
 			transaction.replace(R.id.homeActivity, fragment);
 			transaction.commit();
 			currentMenu=item.getItemId();
 			break;
 		case R.id.menu_community:
 			item.setIcon(R.drawable.community_pressed);
 			menu.findItem(R.id.menu_tasks).setIcon(R.drawable.tasks);
 			menu.findItem(R.id.menu_map).setIcon(R.drawable.map);
 			fragment = new CommunityFragment();
 			fragment.setArguments(new Bundle());
 			transaction = fm.beginTransaction();
 			transaction.replace(R.id.homeActivity, fragment);
 			transaction.commit();
 			currentMenu=item.getItemId();
 			break;
 		case R.id.menuShowCompass:
 		case R.id.menuOnlyShowAcceptedTasks:
 		case R.id.menuShowPlayer:
 			if (item.isChecked())
 				item.setChecked(false);
 			else
 				item.setChecked(true);
 			break;
 		case R.id.action_speech_to_text:
 			sendRecognizeIntent();
 			break;
 		case R.id.menuConnectExlap:
 			if (connectionHelper != null){
 				try {
 					connectionHelper.performDiscovery();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 	        }else{
 	        	connectionHelper = new ConnectionHelper();
 	            try {
 	                connectionHelper.performDiscovery();
 	                Log.e("tldr-exlap", "after discover");
 	            } catch (IOException e) {
 	                e.printStackTrace();
 	            }
 	        }
 			break;
 		case R.id.menuSubscribeAll:			
 	        connectionHelper.subscribe("CurrentGear");
 	        Log.e("tldr-exlap", "after subscribe");
 	        Toast.makeText(this, "after subscribe", Toast.LENGTH_SHORT).show();
 			break;
 
 		default:
 			break;
 		}
 		
 		
 		return true;
 	}
 
 	private void switchView(int fragmentId) {
 		Fragment fragment;
 		FragmentManager fm = getFragmentManager();
 		FragmentTransaction transaction;
 		switch (fragmentId) {
 		case R.id.menu_map:
 			fragment = new MapFragment();
 			fragment.setArguments(new Bundle());
 			currentFragment=(FragmentCommunicator) fragment;
 			transaction = fm.beginTransaction();
 			transaction.replace(R.id.homeActivity, fragment);
 			transaction.commit();
 			break;
 		case R.id.menu_tasks:
 			fragment = new TasksFragment();
 			fragment.setArguments(new Bundle());
 			transaction = fm.beginTransaction();
 			transaction.replace(R.id.homeActivity, fragment);
 			transaction.commit();
 			break;
 		case R.id.menu_community:
 			fragment = new CommunityFragment();
 			fragment.setArguments(new Bundle());
 			transaction = fm.beginTransaction();
 			transaction.replace(R.id.homeActivity, fragment);
 			transaction.commit();
 			break;
 
 		default:
 			break;
 		}
 		currentMenu=fragmentId;
 	}
 
 	@Override
 	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	private void sendRecognizeIntent() {
 		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
 		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
 				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
 		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
 		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say your request!");
 		intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 100);
 		startActivityForResult(intent, SPEECH_REQUEST_CODE);
 	}
 	
 	protected void onNewIntent(Intent intent) {
 	    super.onNewIntent(intent);
 		ToolBox.showAlert(this, "GCM",
 				intent.getStringExtra("message"), "Dismiss",
 		new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 			}
 		});		
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// TODO Auto-generated method stub
 		if (requestCode == SPEECH_REQUEST_CODE) {
 			String resultNotification = "";
 			if (resultCode == RESULT_OK) {
 
 				ArrayList<String> matches = data
 						.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
 
 				if (matches.size() == 0) {
 					resultNotification = "Heard nothing";
 				} else {
 					for(String match:matches){
 						resultNotification +=match;
 					}
 
 				}
 				resultNotification = resultNotification.toLowerCase();
 				boolean success=false;
 				if (resultNotification.contains("show"))
 				{
 					if(resultNotification.contains("tasklist") || (resultNotification
 								.contains("task") && resultNotification
 								.contains("list"))|| resultNotification.contains("tasks")) {
 						switchView(R.id.menu_tasks);
 						success=true;
 					}
 					else if(resultNotification.contains("map")){
 						switchView(R.id.menu_map);
 						success=true;
 					}
 					else if(resultNotification.contains("community")){
 						switchView(R.id.menu_community);
 						success=true;
 					}
 				}
 				if(!success)
 					 if(currentMenu==R.id.menu_map)
 						 currentFragment.receiveMessage(FragmentCommunicator.SPEECH_REQUEST_MESSAGE, resultNotification);
 //					ToolBox.showAlert(this, "Speech Result", resultNotification,
 //						"Dismiss", null);
 			}
 		}
 	}
 
 }
