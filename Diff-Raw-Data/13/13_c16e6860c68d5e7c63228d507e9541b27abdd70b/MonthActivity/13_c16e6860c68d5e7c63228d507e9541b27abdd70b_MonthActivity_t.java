 package bb.sxytm.freedum;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Locale;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.GridView;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.parse.FindCallback;
 import com.parse.Parse;
 import com.parse.ParseException;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 import com.parse.ParseRelation;
 import com.parse.ParseUser;
 import com.parse.SaveCallback;
 
 public class MonthActivity extends Activity {
 	
 	public static String EXTRA_MESSAGE = "bb.sxytm.freedum.MESSAGE";
 	public static final String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
 	public static final int DIALOG = 0;
 	private int selectedFriend = 0;
 	
     public GregorianCalendar month, itemmonth;// calendar instances.
 
     public CalendarAdapter adapter;// adapter instance
     public Handler handler;// for grabbing some event values for showing the dot
                                                     // marker.
     public ArrayList<String> items; // container to store calendar items which
                                                                     // needs showing the event marker
     ArrayList<String> event;
     LinearLayout rLayout;
     ArrayList<String> date;
     ArrayList<String> desc;
     
     Calendar c=Calendar.getInstance();
 	int cYear = c.get(Calendar.YEAR);
 	int cMonth = c.get(Calendar.MONTH);
 	int cDay = c.get(Calendar.DAY_OF_MONTH);
 	String compare = null;
 	
     public void onCreate(Bundle savedInstanceState) {
             super.onCreate(savedInstanceState);
     		Parse.initialize(this, "GsPuRscxaR95D6ELavshA2X9zsGfIMnqHegQgeo5", "KRzPHVrLf0mCL8pQfkNFmQfhefdKuN9OBnkO5cP7"); 
             setContentView(R.layout.activity_month);
             Locale.setDefault(Locale.US);
 
     		final ParseUser currentUser = ParseUser.getCurrentUser();
     		Log.d("MonthActivity", "Current user: " + currentUser.getUsername());
     		
     		// Get person to compare to
     		Intent compareIntent = getIntent();
     		String compareMessage = compareIntent.getStringExtra(MonthActivity.EXTRA_MESSAGE);
     		compare = compareMessage;
     		Log.d("MonthActivity", "Compare user: " + compare);
     		
     		// Create new event for new user
     		if(compareMessage=="*NEW_USER*") {
     			ActionBar ab = getActionBar();
     			ab.setTitle("Month + " + compare);
 				Log.d("MonthActivity", "New User");
 				Calendar c=Calendar.getInstance();
 				int year = c.get(Calendar.YEAR);
 				int month = c.get(Calendar.MONTH);
 				int day = c.get(Calendar.DAY_OF_MONTH);
 				int hour = c.get(Calendar.HOUR_OF_DAY);
 		  	  	int min = c.get(Calendar.MINUTE);
 			  	String time = hour + ":" + min;
 			  	String startMonth = months[month] + "_" + String.valueOf(year);
 				String startDay = months[month] + "_" +String.valueOf(day) + "_" +  String.valueOf(year);
 
 				// Put all event information in a JSON array
 				JSONObject event = new JSONObject();
 				Log.d("NAME", "Signed Up");
 				try {
 					event.put("name","Signed Up");
 				Log.d("FROM DATE", startDay);
 				event.put("fromDate",startDay);
 				Log.d("TO DATE", startDay);
 				event.put("toDate",startDay);
 				Log.d("TO TIME", time);
 				event.put("toTime",time);
 				Log.d("FROM TIME", time);
 				event.put("fromTime", time);
 
 				} catch (JSONException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 				
 				// Add username to front of startMonth and startDay
 				startMonth = currentUser.getUsername() + "_" + startMonth;
 				startDay = currentUser.getUsername() + "_" + startDay;
 				
 				// Save
 				ParseObject sMonth = new ParseObject(startMonth);
 				ParseObject sDay = new ParseObject(startDay);
 				sMonth.put(startMonth, startDay);
 				sMonth.saveInBackground();
 				sDay.put(startDay, event);
 				sDay.put("time", time);
 				try {
 					sDay.save();
 				} catch (ParseException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
     		}
     		if(compareMessage!=null) showToast("Comparing with " + compareMessage);
     		
     		// Set layouts
             rLayout = (LinearLayout) findViewById(R.id.text);
             month = (GregorianCalendar) GregorianCalendar.getInstance();
             itemmonth = (GregorianCalendar) month.clone();
 
             items = new ArrayList<String>();
 
             adapter = new CalendarAdapter(this, month, compareMessage);
 
             GridView gridview = (GridView) findViewById(R.id.gridview);
             gridview.setAdapter(adapter);
 
             handler = new Handler();
             handler.post(calendarUpdater);
 
             TextView title = (TextView) findViewById(R.id.title);
             title.setText(android.text.format.DateFormat.format("MMMM yyyy", month));
 
             RelativeLayout previous = (RelativeLayout) findViewById(R.id.previous);
             
             // On previous click
             previous.setOnClickListener(new OnClickListener() {
 
                     @Override
                     public void onClick(View v) {
                             setPreviousMonth();
                             refreshCalendar();
                     }
             });
 
             // On next click
             RelativeLayout next = (RelativeLayout) findViewById(R.id.next);
             next.setOnClickListener(new OnClickListener() {
 
                     @Override
                     public void onClick(View v) {
                             setNextMonth();
                             refreshCalendar();
 
                     }
             });
 
             // On item click in calendar
             gridview.setOnItemClickListener(new OnItemClickListener() {
 					public void onItemClick(AdapterView<?> parent, View v,
                                     int position, long id) {
                             // removing the previous view if added
                             if (((LinearLayout) rLayout).getChildCount() > 0) {
                                     ((LinearLayout) rLayout).removeAllViews();
                             }
                             desc = new ArrayList<String>();
                             date = new ArrayList<String>();
                             ((CalendarAdapter) parent.getAdapter()).setSelected(v);
                             String selectedGridDate = CalendarAdapter.dayString
                                             .get(position);
                             Log.d("dayString", selectedGridDate);
                             String[] separatedTime = selectedGridDate.split("-");
                             String gridvalueString = separatedTime[2].replaceFirst("^0*",
                                             "");// taking last part of date. ie; 2 from 2012-12-02.
                             int gridvalue = Integer.parseInt(gridvalueString);
                             // navigate to next or previous month on clicking offdays.
                             if ((gridvalue > 10) && (position < 8)) {
                                     setPreviousMonth();
                                     refreshCalendar();
                             } else if ((gridvalue < 7) && (position > 28)) {
                                     setNextMonth();
                                     refreshCalendar();
                             }
                             ((CalendarAdapter) parent.getAdapter()).setSelected(v);
 
                                                         
                             for (int i = 0; i < CalendarUtility.startDates.size(); i++) {
                                     if (CalendarUtility.startDates.get(i).equals(selectedGridDate)) {
                                             desc.add(CalendarUtility.nameOfEvent.get(i));
                                     }
                             }
                             
                             // Get data from Parse and add to layout
                             String[] substring = selectedGridDate.split("-");
                             cMonth = Integer.parseInt(substring[1])-1;
                             cDay = Integer.parseInt(substring[2]);
                             cYear = Integer.parseInt(substring[0]);
                             final String currentDay = currentUser.getUsername() + "_" + months[cMonth] + "_" + cDay + "_" + cYear;
                             Log.d("currentDay",currentDay);
                             ParseQuery<ParseObject> query = ParseQuery.getQuery(currentDay);
                             query = query.orderByAscending("time");
                             query.findInBackground(new FindCallback<ParseObject>() {
                               public void done(List<ParseObject> objects, ParseException e) {
                                 if (e == null) {
                                 	desc = new ArrayList<String>();
                                 	if(objects == null || objects.size() == 0) return;
                                 	Log.d("objects.size", objects.size()+"");
                                 	String event = "";
                                 	String fTime = "";
                                 	String tTime = "";
                                   for(int i = 0; i < objects.size(); i ++) {
                                 	  Log.d("i", i+"");
                                 	  try {
 										event = objects.get(i).getJSONObject(currentDay).getString("name");
 										fTime = objects.get(i).getJSONObject(currentDay).getString("fromTime");
 										tTime = objects.get(i).getJSONObject(currentDay).getString("toTime");
 									} catch (JSONException e1) {
 										// TODO Auto-generated catch block
 										e1.printStackTrace();
 									}
                                 	  Log.d("added", event);
                                 	  //desc.add(events);
                                 	
                                	  Log.d("desc.size: user", desc.size()+"");
                                 	  Log.d("Setting rowText 1", i+"");
                                 	  TextView eventTextView = new TextView(MonthActivity.this);
 
                                 	  // set some properties of eventTextView
                                 	  eventTextView.setText("You: " + event);
                                 	  eventTextView.setTextColor(Color.BLACK);
                                 	  eventTextView.setBackgroundColor(0xFF00FF00);
                                 	  eventTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
 
                                 	  // add the textview to the linearlayout	
                                 	  rLayout.addView(eventTextView);
                                 	  
                                 	  // set some properties for timeTextView
                                 	  TextView timeTextView = new TextView(MonthActivity.this);
                                 	  timeTextView.setText("\t\t" + fTime + " - " + tTime);
                                 	  timeTextView.setTextColor(Color.BLACK);
                                 	  timeTextView.setBackgroundColor(0xFF00FF00);//Color.parseColor("#FF00DD"));
                                 	  timeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                                 	  rLayout.addView(timeTextView);
                                   }
                                 } else {
                                   // Failed
                                 }
                               }
                             });
 
                             // Get data from Parse for compare and add to layout
                             if(compare != null && compare != "*NEW_USER*") {
                             	final String compareCurrentDay = compare + "_" + months[cMonth] + "_" + cDay + "_" + cYear;
                                 Log.d("compareCurrentDay",compareCurrentDay);
                                ParseQuery<ParseObject> queryC = ParseQuery.getQuery(compareCurrentDay);
                                 queryC = queryC.orderByAscending("time");
                                 queryC.findInBackground(new FindCallback<ParseObject>() {
                                   public void done(List<ParseObject> objects, ParseException e) {
                                     if (e == null) {
                                     	desc = new ArrayList<String>();
                                     	if(objects == null || objects.size() == 0) return;
                                     	Log.d("compare objects.size", objects.size()+"");
                                     	String event = "";
                                     	String fTime = "";
                                     	String tTime = "";
                                       for(int i = 0; i < objects.size(); i ++) {
                                     	  Log.d("i", i+"");
                                     	  try {
     										event = objects.get(i).getJSONObject(compareCurrentDay).getString("name");
     										fTime = objects.get(i).getJSONObject(compareCurrentDay).getString("fromTime");
     										tTime = objects.get(i).getJSONObject(compareCurrentDay).getString("toTime");
     									} catch (JSONException e1) {
     										// TODO Auto-generated catch block
     										e1.printStackTrace();
     									} catch(NullPointerException e2) {
     										e2.printStackTrace();
     									}
                                     	  Log.d("added", event);
                                     	  //desc.add(events);
                                     	
                                    	  Log.d("desc.size: compare", desc.size()+"");
                                     	  Log.d("Setting rowText 1", i+"");
                                    	  Log.d("MonthCompare", "Name: " + event);
                                     	  TextView eventTextView = new TextView(MonthActivity.this);
 
                                     	  // set some properties of eventTextView
                                     	  eventTextView.setText(compare + ": " + event);
                                     	  eventTextView.setTextColor(Color.BLACK);
                                     	  eventTextView.setBackgroundColor(Color.parseColor("#FF00DD"));
                                     	  eventTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
 
                                     	  // add the textview to the linearlayout	
                                     	  rLayout.addView(eventTextView);
                                     	  
                                     	  // set some properties for timeTextView
                                     	  TextView timeTextView = new TextView(MonthActivity.this);
                                     	  timeTextView.setText("\t\t" + fTime + " - " + tTime);
                                     	  timeTextView.setTextColor(Color.BLACK);
                                     	  timeTextView.setBackgroundColor(Color.parseColor("#FF00DD"));
                                     	  timeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                                     	  rLayout.addView(timeTextView);
                                       }
                                     } else {
                                       // Failed
                                     }
                                   }
                                 });
                             }
                             
                             // Get data from calendar and add to layout
                            Log.d("desc.size: calendar", desc.size()+"");
                             if (desc.size() > 0) {
                                     for (int i = 0; i < desc.size(); i++) {
                                     		Log.d("Setting rowText 2", i+"");
                                             TextView rowTextView = new TextView(MonthActivity.this);
 
                                             // set some properties of rowTextView or something
                                             rowTextView.setText("Event:" + desc.get(i));
                                             rowTextView.setTextColor(Color.BLACK);
                                             rowTextView.setBackgroundColor(Color.LTGRAY);
 
                                             // add the textview to the linearlayout
                                             rLayout.addView(rowTextView);
 
                                     }
 
                             }
 
                             desc = null;
 
                     }
 
             });
             
     }
 
     // Move to next month
     protected void setNextMonth() {
             if (month.get(GregorianCalendar.MONTH) == month
                             .getActualMaximum(GregorianCalendar.MONTH)) {
                     month.set((month.get(GregorianCalendar.YEAR) + 1),
                                     month.getActualMinimum(GregorianCalendar.MONTH), 1);
             } else {
                     month.set(GregorianCalendar.MONTH,
                                     month.get(GregorianCalendar.MONTH) + 1);
             }
 
     }
 
     // Move to previous month
     protected void setPreviousMonth() {
             if (month.get(GregorianCalendar.MONTH) == month
                             .getActualMinimum(GregorianCalendar.MONTH)) {
                     month.set((month.get(GregorianCalendar.YEAR) - 1),
                                     month.getActualMaximum(GregorianCalendar.MONTH), 1);
             } else {
                     month.set(GregorianCalendar.MONTH,
                                     month.get(GregorianCalendar.MONTH) - 1);
             }
 
     }
 
     protected void showToast(String string) {
             Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
 
     }
 
     public void refreshCalendar() {
             TextView title = (TextView) findViewById(R.id.title);
 
             adapter.refreshDays();
             adapter.notifyDataSetChanged();
             handler.post(calendarUpdater); // generate some calendar items
 
             title.setText(android.text.format.DateFormat.format("MMMM yyyy", month));
     }
 
     public Runnable calendarUpdater = new Runnable() {
 
             @Override
             public void run() {
                     items.clear();
 
                     // Print dates of the current week
                     DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                     String itemvalue;
                     event = CalendarUtility.readCalendarEvent(MonthActivity.this);
                     Log.d("=====Event====", event.toString());
                     Log.d("=====Date ARRAY====", CalendarUtility.startDates.toString());
 
                     for (int i = 0; i < CalendarUtility.startDates.size(); i++) {
                             itemvalue = df.format(itemmonth.getTime());
                             itemmonth.add(GregorianCalendar.DATE, 1);
                             items.add(CalendarUtility.startDates.get(i).toString());
                     }
                     adapter.setItems(items);
                     adapter.notifyDataSetChanged();
             }
     };
 	
 /*	int year, month, day;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_month);
 		Parse.initialize(this, "GsPuRscxaR95D6ELavshA2X9zsGfIMnqHegQgeo5", "KRzPHVrLf0mCL8pQfkNFmQfhefdKuN9OBnkO5cP7"); 
 
 		// Get current month
 		Calendar c=Calendar.getInstance();
         year = c.get(Calendar.YEAR);
         month = c.get(Calendar.MONTH);
         day = c.get(Calendar.DAY_OF_MONTH);    
         
         // Sends user to new event activity if they select a day
         CalendarView calendarView=(CalendarView) findViewById(R.id.calendar);
         calendarView.setOnDateChangeListener(new OnDateChangeListener() {
             @Override
             public void onSelectedDayChange(CalendarView view, int year, int month,
                     int dayOfMonth) {
             	Toast.makeText(getApplicationContext(), dayOfMonth+" "+month+" "+year, 0).show();
             	sendMessage(dayOfMonth, month, year);
             }
         });
         
 		// Get all days with events for current month and put into a List
         	// TO DO
 */
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.month, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
     	case R.id.monthNewEvent:
     		Log.d("Action Bar", "Sending to new");
     		sendMessage(cMonth, cDay, cYear);
     		return true;
     	case R.id.compareFriend:
     		Log.d("Action Bar", "Sending to compare");
     		compareFriend();
     		return true;
     	case R.id.monthListView:
     		Log.d("Action Bar", "Sending to list");
     		listView();
     		return true;
     	case R.id.newFriend:
     		Log.d("Action Bar", "Sending to add");
     		addFriend();
     		return true;
     	case R.id.Logout:
     		Log.d("Action Bar", "Sending to logout");
     		// If logout clicked, then logout :p
     		logout();
     		return true;
     	default:
     		return super.onOptionsItemSelected(item);
         }
 	 }
         
 	
     // Logout user if this is selected
     public void logout() {
     	Intent intent = new Intent(this, LoginActivity.class);
     	ParseUser.logOut();
     	ParseUser currentUser = ParseUser.getCurrentUser(); // this is now null
     	if(currentUser == null) {
     		startActivity(intent);
     	}
     	else {
 			showToast("SOMETHING MESSED UP! WE CAN'T LOG YOU OUT!! ");
     	}
     	finish();
     }
     
     // Compare a friend by selecting a friend then relaunching activity
 	public void compareFriend() {
     	Log.d("Compare Friends", "Starting creation");
     	final ParseUser currentUser =  ParseUser.getCurrentUser();
     	String foundUser = "";
 		final Intent intent = new Intent(this, MonthActivity.class);
     	if(currentUser != null) {
     		ParseRelation relation = currentUser.getRelation("Friends");
     		ParseQuery query = relation.getQuery();
     		query.whereEqualTo("username", null);
     		try {
 				List<ParseObject> friends = query.find();
 				List<String> tFriends = new ArrayList<String>();
 				int i = 0;
 				if(!friends.isEmpty()) {
 					Log.d("Compare Friends", "size of friends: " + friends.size());
 					for(ParseObject f : friends) {
 						tFriends.add(f.getString("username").toString());
 						i++;
 						Log.d("Compare Friends", "Adding: " + f.getString("username").toString());
 					}
 				}
 				else tFriends.add("NO FRIENDS FOUND");
 				final CharSequence[] sFriends = tFriends.toArray(new CharSequence[tFriends.size()]);
 				for(CharSequence s : sFriends)	Log.d("Compare Friends", "sFriends: " + s);
 				AlertDialog.Builder builder = new AlertDialog.Builder(this);
 				builder.setTitle(R.string.select_friend);
 				builder.setSingleChoiceItems(sFriends, 0, new DialogInterface.OnClickListener() {
 						
 				    	   @Override
 							public void onClick(DialogInterface dialog, int which) {
 								// TODO Auto-generated method stub
 				    		   selectedFriend = which;
 							}
 				       })
 				       .setPositiveButton(R.string.compare_button, new DialogInterface.OnClickListener() {
 						
 				    	   @Override
 				    	   public void onClick(DialogInterface dialog, int which) {
 				    		   // TODO Auto-generated method stub
 				    	    	String message = sFriends[selectedFriend].toString();
 				    	    	intent.putExtra(EXTRA_MESSAGE, message);
 				    	    	startActivity(intent);
 				    	    	finish();
 				    	   }
 				       })
 				       .setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
 						
 				    	   @Override
 				    	   public void onClick(DialogInterface dialog, int which) {
 				    		   // TODO Auto-generated method stub
 				    	   }
 				       });
 				AlertDialog dialog = builder.create();
 				dialog.show();
 			} catch (ParseException e) {
 				// TODO Auto-generated catch block
 				showToast("Error in finding your friends\nPlease try again later");
 				e.printStackTrace();
 			}
     	}
     }
     
     // Add a new friend to the users friend list
     public void addFriend() {
    // 	showDialog(DIALOG);
     	Log.d("Add Friend Progress", "Starting creeation");
     	// User an EditText view to get user input
     	final EditText input = new EditText(this);
     	input.setId(R.id.userSearch);
     	final AlertDialog d = new AlertDialog.Builder(MonthActivity.this)
     	.setView(input)
     	.setIcon(R.drawable.search)
         .setTitle(R.string.new_friend_button)
         .setMessage("Who do you want to add")
         .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
         .setNegativeButton(android.R.string.cancel, null)
         .setNeutralButton(R.string.new_friend_search, null)
         .create();
     	
     	Log.d("Add Friend Progress", "Finished creation");
     	d.setOnShowListener(new DialogInterface.OnShowListener() {
 
     		@Override
     		public void onShow(DialogInterface dialog) {
     			Log.d("Add Friend Progress", "onShow");
     			Button save = d.getButton(AlertDialog.BUTTON_POSITIVE);
     			save.setOnClickListener(new View.OnClickListener() {
 
     				@Override
     				public void onClick(View view) {
     					String value = input.getText().toString();
     					Log.d("Dialog Action", "SAVING");
     					//Dismiss once everything is OK
     					final String username = input.getText().toString();
 
 						Toast.makeText(getApplicationContext(), "Checking . . .", Toast.LENGTH_SHORT).show();
 						final ParseQuery<ParseUser> query = ParseUser.getQuery();
 						query.whereEqualTo("username", username);
 						List<ParseUser> users = new ArrayList<ParseUser>();
 						try {
 							users = query.find();
 						} catch (ParseException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 							Toast.makeText(getApplicationContext(), "User not found",Toast.LENGTH_LONG).show();
 						}
 						for(ParseUser u : users) {
 							Log.d("User", u.getString("username"));
 						}
 						final ParseUser currentUser = ParseUser.getCurrentUser();
 		                if (currentUser != null) {
 		                    {
 		                        final ParseObject friend = new ParseObject("Friends");
 		                        friend.put("username", username);
 
 		                        friend.saveInBackground(new SaveCallback() {
 		                            @Override
 		                            public void done(ParseException e) {
 		                                // TODO Auto-generated method stub
 		                                ParseRelation relation = currentUser.getRelation("Friends");
 		                                ParseQuery query = relation.getQuery();
 		                   /*             query.whereEqualTo("username", username);
 		        						
 		        						List<ParseUser> user = new ArrayList<ParseUser>();
 		                                try {
 											user = query.find();
 										} catch (ParseException e1) {
 											// TODO Auto-generated catch block
 
 											Toast.makeText(getApplicationContext(), "Saving . . .", Toast.LENGTH_SHORT).show();
 											e1.printStackTrace();
 						*/					relation.add(friend);
 											currentUser.saveInBackground();
 											Toast.makeText(getApplicationContext(), username + " is now your friend", Toast.LENGTH_LONG).show();
 											d.dismiss();
 						//				}
 		                //                Toast.makeText(getApplicationContext(), username + " is already a friend",Toast.LENGTH_LONG).show();
 		                            }
 		                        });
 		                    }
 		                }
     				}
     			});
     			
     			Button cancel = d.getButton(AlertDialog.BUTTON_NEGATIVE);
     			cancel.setOnClickListener(new View.OnClickListener() {
 					
 					@Override
 					public void onClick(View view) {
 						Log.d("Dialog Action", "CANCELING");
 						d.dismiss();
 					}
 				});
     			
     			Button search = d.getButton(AlertDialog.BUTTON_NEUTRAL);
     			search.setOnClickListener(new View.OnClickListener() {
 					
 					@Override
 					public void onClick(View v) {
 						Log.d("Dialog Action", "SEARCHING");
 						final String username = input.getText().toString();
 						Toast.makeText(getApplicationContext(), "Searching . . .",Toast.LENGTH_LONG).show();
 						final ParseQuery<ParseUser> query = ParseUser.getQuery();
 						query.whereEqualTo("username", username);
 						
 						List<ParseUser> users = new ArrayList<ParseUser>();
 						try {
 							users = query.find();
 						} catch (ParseException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 							Toast.makeText(getApplicationContext(), "User not found",Toast.LENGTH_LONG).show();
 						}
 						for(ParseUser u : users) {
 							Log.d("User", u.getString("username"));
 						}
 						Toast.makeText(getApplicationContext(), "User " + users.get(0).getString("username") + " found",Toast.LENGTH_LONG).show();
 					}
 				});	
     		}
     	});
     	d.show();
     }
     
     // Send user to create a new activity if button pressed
     public void newEvent() {
 		Intent intent = new Intent(this, NewEventActivity.class);
 		startActivity(intent);
     }
     
     // Send user to list view if button pressed
     public void listView() {
     	Intent intent = new Intent(this, ListActivity.class);
     	startActivity(intent);
     	finish();
     }
 
 /*
     // Go to next month when right arrow is clicked
     public void nextMonth(View v) {
     	if(month>11) {
     		month = 1;
     		year++;
     	}
     	else month++;
     	Log.d("month",month + "");
     	
     }
     
     // Go to previous month when left arrow is clicked
     public void previousMonth(View v) {
     	if(month <= 1) {
     		month = 12;
     		year--;
     	}
     	else month--;
     	Log.d("month",month + "");
     	
     }
 */
     
     // Called when the user clicks on a day
     public void sendMessage(int sMonth, int sDay, int sYear) {
     	// Do something in response to button
     	Intent intent = new Intent(this, NewEventActivity.class);
     	String message = sDay + " " + sMonth + " " + sYear;
     	Log.d("Message", message);
     	intent.putExtra(EXTRA_MESSAGE, message);
     	startActivity(intent);
     }
 
 }
