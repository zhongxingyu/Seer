 package au.edu.adelaide.physics.opticsstatusboard;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.AlertDialog;
 import android.app.PendingIntent;
 import android.app.TimePickerDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.content.LocalBroadcastManager;
 import android.text.InputType;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TimePicker;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 	private ArrayList<Person> people;
 	private ArrayAdapter<Person> peopleAdapter;
 	private boolean networking, statusChanged, newVersion, reminderEnabled;
 	private ListView peopleList;
 	private ImageButton refreshButton, statusButton, setMessageButton;
 	private Button setBackMessageButton;
 	private Person user;
 	private String userInput, webAddress, updateFileURL;
 	private int signOutHour, signOutMinute, statusButtonCurrent;
 	private MenuItem versionButton;
 	private BroadcastReceiver bReceiver;
 
     public void disableRefreshButton() {
 		findViewById(R.id.refresh_button).setVisibility(View.GONE);
 		findViewById(R.id.progress).setVisibility(View.VISIBLE);
     }
     
     public void enableRefreshButton() {
 		findViewById(R.id.refresh_button).setVisibility(View.VISIBLE);
 		findViewById(R.id.progress).setVisibility(View.GONE);
     }
     public int[] getSignOutTime() {
     	int[] out = {signOutHour, signOutMinute};
     	return out;
     }
     private Person getUser() {
     	return user;
     }
     
     public boolean isNetworking() {
     	return networking;
     }
     
     public void notifyNewVersion(boolean newVersion) {
     	this.newVersion = newVersion;
     	
    	if (versionButton != null) {
    		if (newVersion) {
    			showToast("New version available, download link available in menu");
    			versionButton.setTitle("Download New Version");
    		} else {
    			showToast("Currently up to date :)");
    		}
     	} else {
    		requestUpdateCheck();
     	}
     }
     
     public void notifyUserUpdate(Person user) {
     	this.user = user;
     	setStatusButton(user.getStatus());
     	setNewMessageImage(user.hasMessage());
     }
     
     @Override
 	protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         setContentView(R.layout.activity_main);
         
         refreshUserData();
         
         networking = false;
         statusChanged = true;
         newVersion = false;
         statusButtonCurrent = 0;
            
         peopleList = (ListView) findViewById(R.id.peopleList);
         
         people = new ArrayList<Person>(0);
         
         peopleAdapter = new PeopleArrayAdapter(this, people);
         peopleList.setAdapter(peopleAdapter);
         
         bReceiver = new BroadcastReceiver() {
             @SuppressWarnings("unchecked")
 			@Override
             public void onReceive(Context context, Intent intent) {
                 if (intent.getAction().equals("BackgroundRefresh")) {
     				Bundle data = intent.getExtras();
                     
                     if (data.containsKey("people")) {
                     	people.clear();
                     	people.addAll((Collection<? extends Person>) data.getParcelableArrayList("people"));
                     	peopleAdapter.notifyDataSetChanged();
                     }
                     if (data.containsKey("user")) {
                     	notifyUserUpdate((Person) data.getParcelable("user"));
                     }
                     if (data.containsKey("newVersion")) {
                     	notifyNewVersion(data.getBoolean("newVersion"));
                     }
                     if (data.containsKey("postResult")) {
                     	showToast((String) data.get("postResult"));
                     }
                     
                     setNetworking(false);
     				enableRefreshButton();
                 }
             }
         };
         
         LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
 		IntentFilter intentFilter = new IntentFilter();
 		intentFilter.addAction("BackgroundRefresh");
 		bManager.registerReceiver(bReceiver, intentFilter);
         
         peopleList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				if (!networking) {
 					Context context = parent.getContext();
 					Person clickedPerson = people.get(position);
 
 					PersonDetailDialog detailDialog = new PersonDetailDialog(context);
 					LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 					View personDetailView = inflater.inflate(R.layout.person_detail, null, false);
 					detailDialog.setContentView(personDetailView);
 					detailDialog.setTitle(clickedPerson.getName());
 
 					ListView personDetailList = (ListView) detailDialog.findViewById(R.id.personDetailList);
 					PersonDetailAdapter detailAdapter = new PersonDetailAdapter(context, clickedPerson.getInfoContainer());
 					personDetailList.setAdapter(detailAdapter);
 
 					detailDialog.show();
 				}
 			}
         });
             
         refreshButton = (ImageButton) findViewById(R.id.refresh_button);
         refreshButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				refreshList();
 			}
 		});
         
         setMessageButton = (ImageButton) findViewById(R.id.setMessage);
         setMessageButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(final View v) {
 				showMessageDialog(0);
 			}
 		});
         
         setBackMessageButton = (Button) findViewById(R.id.setBackMessage);
         setBackMessageButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				showMessageDialog(1);
 			}
 		});
         
         statusButton = (ImageButton) findViewById(R.id.statusButton);
         statusButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if (user != null) {
 					if (user.getStatus() == 0) {
 						setStatus(1);
 					} else {
 						setStatus(0);
 					}
 				}
 			}
 		});
         
         requestUpdateCheck();
         
     	if (user != null && reminderEnabled) {
     		setSignOutAlarm();
     	}
     }
     
     @Override
 	public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         versionButton = menu.findItem(R.id.versionButton);
         return true;
     }
     
     @Override
 	public boolean onOptionsItemSelected(MenuItem item) {
     	switch (item.getItemId()) {
     	case R.id.action_settings:
     		setNetworking(false);
     		startActivity(new Intent(this, SettingsActivity.class));
     		return true;
     	case R.id.versionButton:
     		if (!newVersion) {
     			requestUpdateCheck();
     		} else {
     			Intent appDownload = new Intent(Intent.ACTION_VIEW);
     			appDownload.setData(Uri.parse(updateFileURL));
     			startActivity(appDownload);
     		}
     		return true;
     	case R.id.setStatusOption:
     		if (user != null) {
     			AlertDialog.Builder statusDialog = new AlertDialog.Builder(this);
         	
     			statusDialog.setSingleChoiceItems(R.array.statusOptions, user.getStatus(), new DialogInterface.OnClickListener() {
     				@Override
     				public void onClick(DialogInterface dialog, int selectedStatus) {
     					setStatus(selectedStatus);
     					dialog.dismiss();
     				}
     			});
     			
     			statusDialog.setTitle(R.string.statusOption);
     			statusDialog.show();
     		} else {
     			showToast("Not logged in correctly or list hasn't loaded yet");
     		}
         	
         	return true;
     	case R.id.websiteLink:
     		Intent websiteLink = new Intent(Intent.ACTION_VIEW);
 			websiteLink.setData(Uri.parse(webAddress));
 			startActivity(websiteLink);
     		
     		return true;
     	case R.id.setSignOutTime:
     		refreshUserData();
     		
     		TimePickerDialog timePicker = new TimePickerDialog( this, new TimePickerDialog.OnTimeSetListener() {
     			@Override
     			public void onTimeSet(TimePicker view, int hour, int min) {
    					setSignOutTime(hour, min);
     			}
     		}, signOutHour, signOutMinute, false);
     		
     		timePicker.setTitle(R.string.signOutTime);
     		timePicker.show();
     		
     		return true;
     	default:
     		return super.onOptionsItemSelected(item);
     	}
     }
     
     @Override
 	protected void onStop() {
     	super.onStop();
     	
     	setUserData();
     }
     
     public void postUserUpdate() {
     	Intent postIntent = new Intent(this, BackgroundManager.class);
     	Bundle postData = new Bundle();
     	postData.putParcelable("updatedUser", getUser());
     	postIntent.putExtras(postData);
     	startService(postIntent);
 		disableRefreshButton();
 		setNetworking(true);
     }
     
     public void refreshList() {
     	if (!networking) {
     		refreshUserData();
 
     		requestPeopleRefresh();
 
     		if (user != null && statusChanged) {
     			setSignOutAlarm();
     			statusChanged = false;
     		}
     	}
     }
     
     public void refreshUserData() {
     	//Get the saved preferences
         SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
         //Retrieve the username and password
         webAddress = settings.getString("webAddress", "http://www.physics.adelaide.edu.au/cgi-bin/usignin/usignin.cgi");
         reminderEnabled = settings.getBoolean("reminderEnabled", false);
         signOutHour = settings.getInt("signOutHour", 18);
         signOutMinute = settings.getInt("signOutMin", 0);
         updateFileURL = settings.getString("updateFileURL" ,"https://dl.dropbox.com/u/11481054/OpticsStatusBoardApp/OpticsStatusBoard.apk");
     }
     
     public void requestPeopleRefresh() {
     	Intent refreshIntent = new Intent(this, BackgroundManager.class);
     	startService(refreshIntent);
     	disableRefreshButton();
     	setNetworking(true);
     }
     
     public void requestUpdateCheck() {
     	Intent updateCheckIntent = new Intent(this, BackgroundManager.class);
     	updateCheckIntent.putExtra("requestVersionCheck", true);
     	startService(updateCheckIntent);
     	disableRefreshButton();
     	setNetworking(true);
     }
     
     public void setNetworking(boolean networking) {
     	this.networking = networking;
     }
     
     public void setNewMessageImage(boolean newMessage) {
     	if (newMessage)
     		findViewById(R.id.newMessageImage).setVisibility(View.VISIBLE);
     	else
     		findViewById(R.id.newMessageImage).setVisibility(View.INVISIBLE);
     }
 
     public void setSignOutAlarm() {
     	AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
     	
     	Intent reminderIntent = new Intent(getApplicationContext(), BackgroundManager.class);
     	reminderIntent.putExtra("reminderAlarm", false);
 		PendingIntent remPendingIntent = PendingIntent.getService(getApplicationContext(), 0, reminderIntent, PendingIntent.FLAG_UPDATE_CURRENT);
     	
     	Calendar cal = Calendar.getInstance();
     	cal.set(Calendar.HOUR_OF_DAY, signOutHour);
     	cal.set(Calendar.MINUTE, signOutMinute);
     	cal.set(Calendar.SECOND, 0);
     	long trigger = cal.getTimeInMillis();
     	
     	if (trigger <= System.currentTimeMillis()) {
     		trigger += AlarmManager.INTERVAL_DAY;
     	}
     	
     	alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, trigger, AlarmManager.INTERVAL_DAY, remPendingIntent);
     }
     
     public void setSignOutTime(int hour, int min) {
     	signOutHour = hour;
     	signOutMinute = min;
     	setUserData();
     	
     	if (user != null && reminderEnabled) {
     		setSignOutAlarm();
     	}
     }
     
     public void setStatus(int status) {
     	if (user != null && !networking) {
     		refreshUserData();
     		setStatusButton(status);
     		user.setStatus(status);
     		postUserUpdate();
     		statusChanged = true;
     	} else {
 			showToast("Invalid Username");
     	}
     }
     
     public void setStatusButton(int status) {
     	if (statusButtonCurrent != status) {
     		statusButtonCurrent = status;
     		
     		ImageView imageView = (ImageView) findViewById(R.id.statusButton);
     		int imageId = 0;
 
     		switch (status) {
     		case 0:
     			imageId = R.drawable.in;
     			break;
     		case 1:
     			imageId = R.drawable.out;
     			break;
     		case 2:
     			imageId = R.drawable.meeting;
     			break;
     		case 3:
     			imageId = R.drawable.lunch;
     			break;
     		case 4:
     			imageId = R.drawable.sick;
     			break;
     		case 5:
     			imageId = R.drawable.vacation;
     			break;
     		default:
     			// TODO What should it do in this case?
     		}
     		
     		loadBitmap(imageId, imageView, this);
     	}
     }
     
     private void loadBitmap(int resId, ImageView imageView, Context context) {
     	if (BitmapWorkerTask.cancelPotentialWork(resId, imageView)) {
     		final BitmapWorkerTask task = new BitmapWorkerTask(imageView, context);
     		final AsyncDrawable asyncDrawable = new AsyncDrawable(context.getResources(), null, task);
     		imageView.setImageDrawable(asyncDrawable);
     		task.execute(resId);
     	}
     }
     
     public void setUserData() {
     	//Get the preferences
     	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
     	SharedPreferences.Editor editor = settings.edit();
     	
     	//Save any changed data
     	editor.putString("webAddress", webAddress);
     	editor.putInt("signOutHour", signOutHour);
     	editor.putInt("signOutMin", signOutMinute);
     	
     	//Commit the changes
     	editor.commit();
     }
     public void showMessageDialog(final int messageType) {
     	if (!networking && user != null) {
     		refreshUserData();
     		String title;
 
     		switch (messageType) {
     		case 0:
     			userInput = user.getMessage();
     			title = "Enter your message";
     			break;
     		case 1:
     			userInput = user.getBackMessage();
     			title = "Enter when you will be back";
     			break;
     		default:
     			userInput = "";
     			title = "How did you even get here?";
     			break;
     		}
 
     		AlertDialog.Builder builder = new AlertDialog.Builder(this);
     		builder.setTitle(title);
 
     		// Set up the input
     		final EditText input = new EditText(this);
     		input.setText(userInput);
     		// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
     		input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);
     		builder.setView(input);
 
     		// Set up the buttons
     		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() { 
     			@Override
     			public void onClick(DialogInterface dialog, int which) {
     				userInput = input.getText().toString();
 
     				switch (messageType) {
     				case 0:
     					user.setMessage(userInput);
     					if (!networking) {
     						postUserUpdate();
     					}
     					break;
     				case 1:
     					user.setBackMessage(userInput);
     					if (!networking) {
     						postUserUpdate();
     					}
     					break;
     				default:
     					//Shouldn't ever get here...
     					break;
     				}
     			}
     		});
     		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
     			@Override
     			public void onClick(DialogInterface dialog, int which) {
     				dialog.cancel();
     			}
     		});
 
     		builder.show();
     	} else {
 			showToast("Invalid Username");
     	}
     }
     public void showToast(String data) {
     	Toast.makeText(getApplicationContext(), data, Toast.LENGTH_SHORT).show();
     }
 }
 
 
 
 
 
 
 
 
 
