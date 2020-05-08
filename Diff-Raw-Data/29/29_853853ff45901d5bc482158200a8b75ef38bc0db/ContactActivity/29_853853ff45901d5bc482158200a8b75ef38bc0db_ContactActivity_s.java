 package com.example.payback;
 
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class ContactActivity extends TitleActivity 
 {
 	  private ListView contactlistview;	  
 	  private ArrayList<String> friendList;
 	  
 	  static Activity activityInstance;	//these are variables
 	  static PageKillReceiver pkr;		//used for PageKillReceiver.java
 	  static IntentFilter filter;
 	  
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		modifyTitle("Contact List",R.layout.activity_contact);
 		
 		activityInstance = this;
 		pkr = new PageKillReceiver(); pkr.setActivityInstance(activityInstance);
 		filter = new IntentFilter();
 		filter.addAction("com.Payback.Logout_Intent");
 		registerReceiver(pkr, filter);
 
 		contactlistview = (ListView) findViewById(R.id.listofselected);
 		friendList = buildFriendList();
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.activity_contact_iteminlist, friendList);
 		contactlistview.setAdapter(adapter);
 		registerForContextMenu(contactlistview);
 
 	}
 
 	private ArrayList<String> buildFriendList() {
 	    ArrayList<String> list = new ArrayList<String>();
 	    ArrayList<Friend> userfriends = user.getFriends();
 	    for(int i =0; i < userfriends.size(); i++){
 	    	list.add(userfriends.get(i).toString());
 	    }
 
 	    return list;
 	  }
 
 	
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
 		if (view.getId() == R.id.listofselected) {
 			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
 			menu.setHeaderTitle(friendList.get(info.position));
 			String[] menuItems = getResources().getStringArray(R.array.ContactsMenu);
 			for (int i = 0; i<menuItems.length; i++) {
 				menu.add(Menu.NONE, i, i, menuItems[i]);
 			}
 			Toast.makeText(getApplicationContext(),"menu up", Toast.LENGTH_SHORT).show();
 		}
 		
 	}
 	@Override
 	public boolean onContextItemSelected(MenuItem item){
 		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
 		int menuItemIndex = item.getItemId();
 		
 		Friend user = new Friend(null,null,null);
 		final String toDelete = user.extractEmail(friendList.get(info.position));
 
 		switch (menuItemIndex)
		{
 			case 0:
 				LayoutInflater inflater = this.getLayoutInflater();
 				AlertDialog.Builder builder = new AlertDialog.Builder(this);
 				
 				Toast.makeText(getApplicationContext(),"Opened Show Contact", Toast.LENGTH_SHORT).show();
 				//TextView one = (TextView)findViewById(R.id.emailConfirmView);
 				//one.setText("hh");
 				builder.setTitle("User Information")
 				       .setView(inflater.inflate(R.layout.dialog_user_info, null))
 				       .setPositiveButton(R.string.back, new DialogInterface.OnClickListener() {
 				           public void onClick(DialogInterface dialog, int id) {
 				        	   Toast.makeText(getApplicationContext(),"Back", Toast.LENGTH_SHORT).show();				        	   
 				        	   dialog.dismiss();
 				           }
 				       })
 				       .setNegativeButton(R.string.DeleteContact, new DialogInterface.OnClickListener() {
 				           public void onClick(DialogInterface dialog, int id) {
 				        	   deleteContact(toDelete);
 				        	   Toast.makeText(getApplicationContext(),"ToDelete", Toast.LENGTH_SHORT).show();
 				        	   dialog.cancel();
 				           }
 				       });
 				Dialog dialog = builder.create();
 				dialog.show();
				return false;
			case 1:
 				AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
 		 	    builder2.setTitle("Confirm Delete?")
 				       .setPositiveButton(R.string.Confirm, new DialogInterface.OnClickListener() {
 				    	   public void onClick(DialogInterface dialog, int id) {
 				        	   dialog.dismiss();	     
 					        	try {
 									confirmDelete(toDelete);
 								} catch (InterruptedException e) {
 									e.printStackTrace();
 								} catch (JSONException e) {
 									e.printStackTrace();
 								}		        	   
 				           }
 				       })
 				       .setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
 				           public void onClick(DialogInterface dialog, int id) {
 				        	   dialog.cancel();
 				           }
 				       });
 				Dialog dialog2 = builder2.create();
 				dialog2.show();
 				return false;
			case 2:
 				return false;
 		}
 		return true;
 	}
 	
 	public void showCreateContact(View view)
     {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		
 		final EditText emailinput = new EditText(this);
 		
  	    builder.setTitle("Add new Contact")
 		       .setView(emailinput)
 		       .setPositiveButton(R.string.Add, new DialogInterface.OnClickListener() {
 		           public void onClick(DialogInterface dialog, int id) {
 		        	   //confirmContact(emailinput.getText().toString());	
 		        	    try {
 		        		   sendContact(emailinput.getText().toString());
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 							
 						} catch (JSONException e) {
 							e.printStackTrace();
 						}
 						
 		        	   dialog.dismiss();	        	   
 		           }
 		       })
 		       .setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
 		           public void onClick(DialogInterface dialog, int id) {
 		        	   dialog.cancel();
 		           }
 		       });
 		Dialog dialog = builder.create();
 		dialog.show();
     }
 	
 	public void sendContact(String email) throws InterruptedException, JSONException{	 
 		final String EMAIL_PATTERN = 
 				"^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$";
 		final Pattern pattern = Pattern.compile(EMAIL_PATTERN);
 		final Matcher matcher;
 		matcher = pattern.matcher(email);
 		
 		if(!matcher.matches() && email == user.getEmail()){
 			Toast.makeText(getApplicationContext(), "Email: \""+email+"\" is not a valid email address!", Toast.LENGTH_SHORT).show();
 		}else{
 			if(AccessNet.AddFriend(email, user.getEmail(), user.getPassword())){		
 				if(AccessNet.AddNotif(user.getEmail(), user.getPassword(), user.getEmail() + " Added you as a Friend", email)){
 					Toast.makeText(getApplicationContext(),"Notif sent", Toast.LENGTH_SHORT).show();
 				}
 				else
 					Toast.makeText(getApplicationContext(),"No Notif Made", Toast.LENGTH_SHORT).show();
 				
 				//update list
 				JSONObject friends = AccessNet.lookupFriends(user.getEmail(),user.getPassword());
 				user.setFriends(user.parseFriends(friends));
 				
 				refresh();
 				
 				Toast.makeText(getApplicationContext(),email + " Added as a Friend!", Toast.LENGTH_SHORT).show();
 			}else{
 				Toast.makeText(getApplicationContext(),"Error adding friend", Toast.LENGTH_SHORT).show();
 			}
 		}
 	}
 	
 	public void deleteContact(final String email){
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
  	    builder.setTitle("Confirm Delete?")
 		       .setPositiveButton(R.string.Confirm, new DialogInterface.OnClickListener() {
 		    	   public void onClick(DialogInterface dialog, int id) {
 		        	   dialog.dismiss();	     
 
 	        		   try {
 							confirmDelete(email);
 						} 
 	        		    catch (JSONException e) {
 							e.printStackTrace();
 						}
 	        		    catch (InterruptedException e) {
 							e.printStackTrace();
 						}		        	   
 		           }
 		       })
 		       .setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
 		           public void onClick(DialogInterface dialog, int id) {
 		        	   dialog.cancel();
 		           }
 		       });
 		Dialog dialog = builder.create();
 		dialog.show();
 	}
 	
 	public void confirmDelete(String email) throws InterruptedException, JSONException{
 		if(AccessNet.DeleteFriend(email, user.getEmail(), user.getPassword())){
 			Toast.makeText(getApplicationContext(),email + " Deleted from friends!", Toast.LENGTH_SHORT).show();	
 			
 			JSONObject friends = AccessNet.lookupFriends(user.getEmail(),user.getPassword());
 			user.setFriends(user.parseFriends(friends));
 			
 			refresh();
 		}
 		else
 			Toast.makeText(getApplicationContext(),"Error deleting friend", Toast.LENGTH_SHORT).show();
 	}
 	
 	public void showMainMenu(View view)
     {
     	Intent intent = new Intent(this, MainActivity.class);
         startActivity(intent);
         this.finish();
     }
 }
