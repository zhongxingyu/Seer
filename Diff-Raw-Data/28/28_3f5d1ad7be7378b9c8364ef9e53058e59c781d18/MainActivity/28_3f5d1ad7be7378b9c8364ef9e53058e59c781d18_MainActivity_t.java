 package au.edu.adelaide.physics.opticsstatusboard;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
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
 import android.widget.ListView;
 import android.widget.TimePicker;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 	private ArrayList<Person> people;
 	private ArrayAdapter<Person> peopleAdapter;
 	private boolean networking, canNotify, statusChanged, canVibrate, newVersion;
 	private ListView peopleList;
 	private ImageButton refreshButton, inButton, outButton, confButton, lunchButton, sickButton, vacButton, setMessageButton;
 	private Button setBackMessageButton;
 	private Person user;
 	private String userInput, webAddress, updateFileURL;
 	private int signOutHour, signOutMinute, statusButtonCurrent;
 	private MenuItem versionButton;
 	private BroadcastReceiver bReceiver;
 
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         refreshUserData();
         
         networking = false;
         statusChanged = true;
         newVersion = false;
         statusButtonCurrent = 0;
        
        updateFileURL = "https://dl.dropbox.com/u/11481054/OpticsStatusBoardApp/OpticsStatusBoard.apk";
            
         peopleList = (ListView) findViewById(R.id.peopleList);
         
         new OnAlarmReceiver();
         
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
 			public void onClick(View v) {
 				refreshList();
 			}
 		});
         
         setMessageButton = (ImageButton) findViewById(R.id.setMessage);
         setMessageButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(final View v) {
 				showMessageDialog(0);
 			}
 		});
         
         setBackMessageButton = (Button) findViewById(R.id.setBackMessage);
         setBackMessageButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				showMessageDialog(1);
 			}
 		});
         
         inButton = (ImageButton) findViewById(R.id.in_button);
         inButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				setStatus(1);
 			}
 		});
         
         outButton = (ImageButton) findViewById(R.id.out_button);
         outButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				setStatus(0);
 			}
 		});
         
         confButton = (ImageButton) findViewById(R.id.conf_button);
         confButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				setStatus(0);
 			}
 		});
         
         lunchButton = (ImageButton) findViewById(R.id.lunch_button);
         lunchButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				setStatus(0);
 			}
 		});
         
         sickButton = (ImageButton) findViewById(R.id.sick_button);
         sickButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				setStatus(0);
 			}
 		});
         
         vacButton = (ImageButton) findViewById(R.id.vac_button);
         vacButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				setStatus(0);
 			}
 		});
         
         requestUpdateCheck();
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
     public void requestUpdateCheck() {
     	Intent updateCheckIntent = new Intent(this, BackgroundManager.class);
     	updateCheckIntent.putExtra("requestVersionCheck", true);
     	startService(updateCheckIntent);
     	disableRefreshButton();
     	setNetworking(true);
     }
     public void requestPeopleRefresh() {
     	Intent refreshIntent = new Intent(this, BackgroundManager.class);
     	startService(refreshIntent);
     	disableRefreshButton();
     	setNetworking(true);
     }
     
     public void notifyNewVersion(boolean newVersion) {
     	this.newVersion = newVersion;
     	
     	if (newVersion) {
     		showToast("New version available, download link available in menu");
     		versionButton.setTitle("Download New Version");
     	} else {
     		showToast("Currently up to date :)");
     	}
     }
     
     public void notifyUserUpdate(Person user) {
     	this.user = user;
     	
 //    	System.out.println("User received is "+user.getName());
     	
     	setStatusButton(user.getStatus());
     }
     
     public void setSignOutTime(int hour, int min) {
     	signOutHour = hour;
     	signOutMinute = min;
     	setUserData();
     	
     	if (user != null) {
     		setSignOutAlarm();
     	}
     }
     
     public void setSignOutAlarm() {
     	AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
     	Intent alarmIntent = new Intent(this, OnAlarmReceiver.class);
     	alarmIntent.putExtra("status", getUser().getStatus());
     	alarmIntent.putExtra("canNotify", canNotify);
     	alarmIntent.putExtra("canVibrate", canVibrate);
     	PendingIntent alarmPending = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
     	
     	Calendar cal = Calendar.getInstance();
     	cal.set(Calendar.HOUR_OF_DAY, signOutHour);
     	cal.set(Calendar.MINUTE, signOutMinute);
     	cal.set(Calendar.SECOND, 0);
     	long trigger = cal.getTimeInMillis();
     	
     	if (trigger <= System.currentTimeMillis()) {
     		trigger += AlarmManager.INTERVAL_DAY;
     	}
 //    	System.out.println("Trigger time in ms: "+trigger);
     	
     	alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, trigger, AlarmManager.INTERVAL_DAY, alarmPending);
     }
     
     public int[] getSignOutTime() {
     	int[] out = {signOutHour, signOutMinute};
     	return out;
     }
     
     public void refreshUserData() {
     	//Get the saved preferences
         SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
         //Retrieve the username and password
         webAddress = settings.getString("webAddress", "http://www.physics.adelaide.edu.au/cgi-bin/usignin/usignin.cgi");
         canNotify = settings.getBoolean("notificationEnabled", false);
         canVibrate = settings.getBoolean("canVibrate", true);
         signOutHour = settings.getInt("signOutHour", 18);
         signOutMinute = settings.getInt("signOutMin", 0);
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
     
     public void setStatusButton(int status) {
     	if (statusButtonCurrent != status) {
     		statusButtonCurrent = status;
     		
     		findViewById(R.id.in_button).setVisibility(View.GONE);
     		findViewById(R.id.out_button).setVisibility(View.GONE);
     		findViewById(R.id.conf_button).setVisibility(View.GONE);
     		findViewById(R.id.lunch_button).setVisibility(View.GONE);
     		findViewById(R.id.sick_button).setVisibility(View.GONE);
     		findViewById(R.id.vac_button).setVisibility(View.GONE);
 
     		switch (status) {
     		case 0:
     			findViewById(R.id.in_button).setVisibility(View.VISIBLE);
     			break;
     		case 1:
     			findViewById(R.id.out_button).setVisibility(View.VISIBLE);
     			break;
     		case 2:
     			findViewById(R.id.conf_button).setVisibility(View.VISIBLE);
     			break;
     		case 3:
     			findViewById(R.id.lunch_button).setVisibility(View.VISIBLE);
     			break;
     		case 4:
     			findViewById(R.id.sick_button).setVisibility(View.VISIBLE);
     			break;
     		case 5:
     			findViewById(R.id.vac_button).setVisibility(View.VISIBLE);
     			break;
     		default:
     			// TODO What should it do in this case?
     		}
     	}
     }
     
     public void disableRefreshButton() {
 		findViewById(R.id.refresh_button).setVisibility(View.GONE);
 		findViewById(R.id.progress).setVisibility(View.VISIBLE);
     }
     
     public void enableRefreshButton() {
 		findViewById(R.id.refresh_button).setVisibility(View.VISIBLE);
 		findViewById(R.id.progress).setVisibility(View.GONE);
     }
 
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         versionButton = menu.findItem(R.id.versionButton);
         return true;
     }
     
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
     
     protected void onStop() {
     	super.onStop();
     	
     	setUserData();
     }
     
     public void setNetworking(boolean networking) {
     	this.networking = networking;
     }
     public boolean isNetworking() {
     	return networking;
     }
     private Person getUser() {
     	return user;
     }
 }
 
 
 
 
 
 
 
 
 
