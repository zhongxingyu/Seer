 package com.example.locus;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import android.app.Activity;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.NotificationCompat;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.example.locus.core.Constants;
 import com.example.locus.core.CoreFacade;
 import com.example.locus.core.IObserver;
 import com.example.locus.entity.Message;
 import com.example.locus.entity.Sex;
 import com.example.locus.entity.User;
 
 public class Demo extends Activity implements IObserver {
 
 	double latitude = 0;
 	double longitude = 0;
 	String username;
 	String ipAdd;
 	String gender;
 	Sex sex;
 	String interests;
 	private ListView listView;
 	private TextView latituteField;
 	private TextView longitudeField;
 	CoreFacade core;
 	User currentUser;
 	Sex gend;
 
 	private int groupId1 = 1;
 	private int editProfileId = Menu.FIRST;
 	byte[] imageInByte;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_list_users);
 		Intent intent = getIntent();
 		core = CoreFacade.getInstance();
 
 		currentUser = CoreFacade.getInstance().getCurrentUser();
 		username = intent.getStringExtra("userName");
 		latitude = Double.parseDouble(intent.getStringExtra("latitude"));
 		longitude = Double.parseDouble(intent.getStringExtra("longitude"));
 		ipAdd = intent.getStringExtra("IP");
 		gender = intent.getStringExtra("sex");
 		imageInByte = intent.getByteArrayExtra("pic");
		if (gender.equals("Male"))
 			sex = Sex.Male;
 		else
 			sex = Sex.Female;
 
 		interests = intent.getStringExtra("interests");
 
 		if (currentUser == null) {
 			currentUser = new User(username, sex, ipAdd, latitude, longitude,
 					interests);
 			currentUser.setPic(imageInByte);
 			core.addObserver(this);
 		} else {
 			currentUser.setLatitude(latitude);
 			currentUser.setLongtitude(longitude);
 			currentUser.setIp(ipAdd);
 			currentUser.setName(username);
 			currentUser.setInterests(interests);
 			currentUser.setSex(sex);
 			currentUser.setPic(imageInByte);
 		}
 
 
 		AsyncTask<User, Integer, Set<User>> registerTask = new RegisterTask();
 		registerTask.execute(currentUser);
 
 		System.out.println("Call CoreFacade's register");
 		latituteField = (TextView) findViewById(R.id.textView1);
 		longitudeField = (TextView) findViewById(R.id.textView2);
 		latituteField.setText(String.valueOf(latitude));
 		longitudeField.setText(String.valueOf(longitude));
 	}
 
 	// ------------------------------------------------------------------------------------------------------------------------
 	/* Request updates at startup */
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		MenuInflater menuInflater = getMenuInflater();
 		menuInflater.inflate(R.layout.menus, menu);
 		return true;
 
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		switch (item.getItemId()) {
 
 		case R.id.editProfile:
 			Intent intent = new Intent(this, MyProfile.class);
 			startActivity(intent);
 			break;
 		case R.id.broadCast:
 			Intent intentBroadCast = new Intent(this, BroadCast.class);
 			startActivity(intentBroadCast);
 			break;
 
 		case R.id.refresh:
 			Intent intentMain = new Intent(this, MainActivity.class);
 			startActivity(intentMain);
 			break;
 
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	public void onReceiveMessage(Message msg) {
 		AsyncTask<Message, Integer, Message> updateUITask = new OnReceiveMessageUpdateUITask();
 		updateUITask.execute(msg);
 	}
 
 	@Override
 	public void onReceiveUserProfile(User user) {
 	}
 
 //	public void onDestroy() {
 //		super.onDestroy();
 //		LogoutTask logoutTask = new LogoutTask();
 //		logoutTask.execute(new Object[1]);
 //	}
 
 	public void onBackButtonPressed(){
 		LogoutTask logoutTask = new LogoutTask();
 		logoutTask.execute(new Object[1]);
 		System.exit(0);
 		
 	}
 		
 	
 	@Override
 	public void onReceiveNearbyUsers(Set<User> users) {
 	}
 
 	private class RegisterTask extends AsyncTask<User, Integer, Set<User>> {
 
 		@Override
 		protected Set<User> doInBackground(User... params) {
 			CoreFacade.getInstance().register(currentUser);
 			return CoreFacade.getInstance().getUsersNearby();
 		}
 
 		@Override
 		protected void onPostExecute(Set<User> result) {
 			if (result != null) {
 				Log.i(Constants.AppUITag,
 						"refreshed user number = " + result.size());
 			}
 
 			List<User> data = new ArrayList<User>();
 			try {
 				data.addAll(result);
 			} catch (NullPointerException e) {
 				Toast.makeText(getBaseContext(), "No users Nearby",
 						Toast.LENGTH_SHORT).show();
 			}
 
 			AdapterList adapter = new AdapterList(Demo.this,
 					R.layout.activity_list_adapter, data);
 
 			listView = (ListView) findViewById(R.id.listView);
 			listView.setAdapter(adapter);
 
 			Intent intent = new Intent(getApplicationContext(), Profile.class);
 
 			// listView.setOnItemClickListener(new
 			// AdapterView.OnItemClickListener() {
 			//
 			// @Override
 			// public void onItemClick(AdapterView<?> adapter, View view,
 			// int position, long id) {
 			// User o = (User) adapter.getItemAtPosition(position);
 			// GetUserProfileTask getUserProfileTask = new GetUserProfileTask();
 			// getUserProfileTask.execute(o);
 			//
 			// SendMessageTask sendMessageTask = new SendMessageTask();
 			//
 			// Message msg = new Message(currentUser, o, "Normal", "lala");
 			// sendMessageTask.execute(msg);
 			// }
 			// });
 			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 
 				@Override
 				public void onItemClick(AdapterView<?> adapter, View view,
 						int position, long id) {
 					// TODO Auto-generated method stub
 					User o = (User) adapter.getItemAtPosition(position);
 					String str_text = o.getName();
 					Toast.makeText(
 							getApplicationContext(),
 							str_text + " \n" + "IP = " + o.getIp() + "\nLat="
 									+ o.getLatitude() + " Lon="
 									+ o.getLongtitude(), Toast.LENGTH_LONG)
 							.show();
 					Intent intent = new Intent(getApplicationContext(),
 							Profile.class);
 
 					intent.putExtra("user", o);
 					intent.putExtra("user2", (User)null);
 
 					startActivity(intent);
 				}
 
 
 			});  
 
 		}
 	}
 
 	private class GetUserProfileTask extends AsyncTask<User, Integer, User> {
 		@Override
 		protected User doInBackground(User... params) {
 			return CoreFacade.getInstance().getUserProfile(params[0]);
 		}
 
 		@Override
 		protected void onPostExecute(User result) {
 			String str_text = result.getName();
 			Toast.makeText(
 					getApplicationContext(),
 					str_text + " \n" + "IP = " + result.getIp() + "\nLat="
 							+ result.getLatitude() + " Lon="
 							+ result.getLongtitude() + " Int = "
 							+ result.getInterests(), Toast.LENGTH_LONG).show();
 		}
 	}
 
 	private class OnReceiveMessageUpdateUITask extends
 	AsyncTask<Message, Integer, Message> {
 		@Override
 		protected Message doInBackground(Message... params) {
 			return params[0];
 		}
 
 		@Override
 		protected void onPostExecute(Message result) {
 			// TODO add new message notification on the list
 			String str_text = result.toString();
 			Toast.makeText(getApplicationContext(), str_text, Toast.LENGTH_LONG)
 			.show();
 			createNotification(result);
 		}
 	}
 
 
 	public void createNotification(Message m){
 		Intent intent2 = new Intent(getApplicationContext(), Chat.class);
 		intent2.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
 		intent2.putExtra("user", m.getSrc());
 		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent2, 0);
 		
 		// Build notification
 		// Actions are just fake
 		Notification noti = new NotificationCompat.Builder(this).setAutoCancel(true)
 		.setContentTitle("New Message from " + m.getSrc().getName())
 		.setContentText(m.getData().toString())
 		.setSmallIcon(R.drawable.locus)
 		.setContentIntent(pIntent)
 		.addAction(R.drawable.msg1, "View", pIntent).build();
 
 
 		NotificationManager notificationManager = 
 				(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 
 		// Hide the notification after its selected
 		noti.flags |= Notification.FLAG_AUTO_CANCEL;
 	
 		//noti.flags |= Notification.DEFAULT_VIBRATE;
 		//noti.flags |= Notification.DEFAULT_SOUND;
 		
 		notificationManager.notify(0, noti); 
 
 
 	}
 
 	private class SendMessageTask extends AsyncTask<Message, Integer, Message> {
 
 		@Override
 		protected Message doInBackground(Message... params) {
 			CoreFacade.getInstance().sendMessage(params[0].getDst(),
 					(String) params[0].getData());
 			return params[0];
 		}
 
 		@Override
 		protected void onPostExecute(Message result) {
 			String str_text = String.format("msg sent.  msg = %s",
 					result.toString());
 			Toast.makeText(getApplicationContext(), str_text, Toast.LENGTH_LONG)
 			.show();
 		}
 	}
 
 
 	private class LogoutTask extends AsyncTask<Object, Integer, Object> {
 		@Override
 		protected Object doInBackground(Object... params) {
 			CoreFacade.getInstance().logout();
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Object result) {
 			Toast.makeText(getApplicationContext(), "Good bye!",
 					Toast.LENGTH_LONG).show();
 		}
 	}
 
 
 }
