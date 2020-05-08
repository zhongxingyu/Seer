 package com.cooltofu;
 
 import java.util.Calendar;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.SystemClock;
 import android.text.InputType;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.GestureDetector;
 import android.view.GestureDetector.SimpleOnGestureListener;
 import android.view.LayoutInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 
 import com.cooltofu.db.TimerDbAdapter;
 import com.google.android.apps.analytics.GoogleAnalyticsTracker;
 
 public class TaskTimerActivity extends Activity implements OnClickListener {
 	final int TIME_ID_PREFIX = 10000;
 	final int TASK_LABEL_ID_PREFIX = 20000;
 	final int START_STOP_ID_PREFIX = 30000;
 	
 	
 	//boolean isTimeEdited = false; // indicates if the user has changed the time
 	String editedHour;
 	String editedMinute;
 	String editedSecond;
 	
 	LayoutParams startStopBtnLayoutParams;
 	LayoutParams mtlParams;
 	LayoutParams itlParams;
 	LayoutParams timeTextParams;
 	LayoutParams taskLabelParams;
 	LayoutParams llParams;
 
 	TableLayout mainTl;
 	TableLayout innerTl;
 	TableRow tr;
 	LinearLayout ll;
 
 	
 	int viewIdCounter = 0;
 	Timer timer = new Timer();
 	final Handler handler = new Handler();
 
 	private TimerDbAdapter db;
 	private Cursor cursor;
 	
 	public static final String PREFS_NAME = "MyPrefsFile";
 	public GoogleAnalyticsTracker tracker;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		
 		Eula.show(this);
 		
 		
 		
 		tracker = GoogleAnalyticsTracker.getInstance();
 		tracker.startNewSession("UA-27584987-1", 10, this);
 		
 		setContentView(R.layout.main);
 		
 		startStopBtnLayoutParams = ((Button) findViewById(R.id.button1)).getLayoutParams();
 		timeTextParams = ((TextView) findViewById(R.id.timeText)).getLayoutParams();
 
 		taskLabelParams = ((TextView) findViewById(R.id.taskLabel)).getLayoutParams();
 		llParams = ((LinearLayout) findViewById(R.id.linearLayout)).getLayoutParams();
 		mtlParams = ((TableLayout) findViewById(R.id.tableLayout)).getLayoutParams();
 		itlParams = ((TableLayout) findViewById(R.id.tableLayout2)).getLayoutParams();
 		ll = (LinearLayout) findViewById(R.id.linearLayout);
 		
 		// get timers from db
 		db = new TimerDbAdapter(this);
 		db.open();
 		
 		
 		cursor = db.fetchAllTimers();
 		startManagingCursor(cursor);
 		
 		if (cursor != null) {
 			
 			
 			cursor.moveToFirst();
 			while (cursor.isAfterLast() == false) {
 				// KEY_ROWID, KEY_LABEL, KEY_SECONDS, KEY_IS_ON
 				int id = cursor.getInt(0);
 				String label = cursor.getString(1);
 				int seconds = cursor.getInt(2);
 				long timestamp = cursor.getLong(3); // milliseconds
 				boolean isOn = (cursor.getInt(4) == 1) ? true : false;
 				
 				
 				// add timestamp value to seconds if needed
 				// calculate the seconds to add since the activity was destroyed
 				if (isOn && timestamp > 0) {
 					long now = Calendar.getInstance().getTimeInMillis();
 					long elapsed = now - timestamp;  // milliseconds
 					seconds += ((int) (Math.round(elapsed/1000 + .5))); // add .5 to lessen the lost milliseconds
 					// TODO: keep timer counter in millis for better accuracy
 				}
 				
 				createTaskTimer(id, label, seconds, isOn);
 				
 				cursor.moveToNext();
 			}
 			tracker.trackEvent("RestoreFromDb", "onCreate", "count", cursor.getCount());
 		}
 		
 
 		
 		Button newTimerBtn = (Button) findViewById(R.id.newTimerBtn);
 		
 		newTimerBtn.setOnTouchListener(new View.OnTouchListener() {
 			
 			public boolean onTouch(View v,MotionEvent evt) {
 				
 				switch(evt.getAction()) {
 					case MotionEvent.ACTION_DOWN:
 						v.setBackgroundColor(Color.WHITE);
 						((Button) v).setTextColor(Color.DKGRAY);
 						break;
 					
 					case MotionEvent.ACTION_UP:
 						v.setBackgroundColor(Color.BLACK);
 						((Button) v).setTextColor(Color.LTGRAY);
 						break;
 					
 					case MotionEvent.ACTION_CANCEL:
 						v.setBackgroundColor(Color.BLACK);
 						((Button) v).setTextColor(Color.LTGRAY);
 						break;
 				}
 				
 				return false;
 			}
 		});
 
 		
 		
 
 		newTimerBtn.setOnClickListener(new View.OnClickListener() {
 			
 			
 			public void onClick(View v) {
 				
 				
 				AlertDialog.Builder alert = new AlertDialog.Builder(TaskTimerActivity.this);
 				alert.setTitle("Add New Timer");
 				alert.setMessage("Enter Timer Label");
 
 				final EditText input = new EditText(TaskTimerActivity.this);
 				input.setSingleLine(); // one line tall
 				input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
 				alert.setView(input);
 				alert.setPositiveButton("Ok",
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog,
 									int whichButton) {
 								
 								String label = input.getText().toString();
 								// create db entry for new timer
 								long timerId = db.createTimer(label, 0, 0, false);
 								
 								if (timerId == -1) {
 									// db error
 									// TODO: handle error
 									
 								}
 								
 									
 								createTaskTimer((int) timerId, label, 0, false);
 							}
 						});
 
 				alert.setNegativeButton("Cancel",
 						new DialogInterface.OnClickListener() {
 
 							public void onClick(DialogInterface dialog,
 									int which) {
 								// TODO Auto-generated method stub
 								return;
 							}
 						});
 				alert.show();
 				
 				tracker.trackEvent("NewTimerBtn", "setOnClickListener", "", -1);
 			}
 		});
 
 		
 		
 		//-------------------------------
 		// More button actions
 		final Button moreBtn = (Button) findViewById(R.id.moreBtn);
 		moreBtn.setOnTouchListener(new View.OnTouchListener() {
 				
 			public boolean onTouch(View v,MotionEvent evt) {
 				
 				switch(evt.getAction()) {
 					case MotionEvent.ACTION_DOWN:
 						v.setBackgroundColor(Color.WHITE);
 						moreBtn.setTextColor(Color.DKGRAY);
 						break;
 					case MotionEvent.ACTION_UP:
 						v.setBackgroundColor(Color.BLACK);
 						moreBtn.setTextColor(Color.LTGRAY);
 						break;
 				}
 				
 				return false;
 			}
 		}); // moreBtn.setOnTouchListener()
 		
 		moreBtn.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				Intent i = new Intent();
 				i.setClass(TaskTimerActivity.this, MoreScreen.class);
 				startActivity(i);
 				
 			}
 		
 		});
 		
 		
 		// handle swipe gestures
 		gestureDetector = new GestureDetector(new MyGestureDetector());
         gestureListener = new View.OnTouchListener() {
             public boolean onTouch(View v, MotionEvent event) {
                 return gestureDetector.onTouchEvent(event);
             }
         };
         
         RelativeLayout sv = (RelativeLayout) findViewById(R.id.relativeLayout);
         sv.setOnClickListener(TaskTimerActivity.this);
         sv.setOnTouchListener(gestureListener);
 	}//onCreate
 
 	
 	private void createTaskTimer(int timerId, String label, final int seconds, boolean isOn) {
 		innerTl = new TableLayout(TaskTimerActivity.this);
 		innerTl.setLayoutParams(itlParams);
 
 		final TextView taskLabel = new TextView(innerTl.getContext());
 		taskLabel.setText(label);
 		taskLabel.setLayoutParams(taskLabelParams);
 		taskLabel.setTextSize(18);
 		taskLabel.setId(TASK_LABEL_ID_PREFIX + timerId);
 		taskLabel.setTextColor(Color.LTGRAY);
 
 		final TextView timeText = new TextView(TaskTimerActivity.this);
 		timeText.setText(formatTimeTextDisplay(seconds));
 		timeText.setLayoutParams(timeTextParams);
 		timeText.setTextSize(28);
 		timeText.setId(TIME_ID_PREFIX + timerId);
 		timeText.setTextColor(Color.LTGRAY);
 		
 		TableRow _tr2 = new TableRow(TaskTimerActivity.this);
 		_tr2.addView(timeText);
 		innerTl.addView(_tr2);
 
 		TableRow _tr3 = new TableRow(TaskTimerActivity.this);
 		_tr3.addView(taskLabel);
 		innerTl.addView(_tr3);
 
 		// add horizontal line
 		ViewGroup.LayoutParams p = new ViewGroup.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 1);
 
 		View hrView = new View(TaskTimerActivity.this);
 		hrView.setLayoutParams(p);
 		hrView.setBackgroundColor(Color.GRAY);
 		hrView.getBackground().setAlpha(120);
 		innerTl.addView(hrView);
 		
 		
 		mainTl = new TableLayout(TaskTimerActivity.this);
 		mainTl.setLayoutParams(mtlParams);
 		mainTl.setId(timerId); // set the layout id for reference
 									 // later
 		mainTl.setPadding(0, 10, 0, 0);
 		
 		
         
 
 		//ViewGroup.LayoutParams btnParams = new ViewGroup.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 55);
 		final float scale = getResources().getDisplayMetrics().density;
 		int pixels = (int) (65 * scale + 0.5f);
 
 		final ToggleButton startStopBtn = new ToggleButton(TaskTimerActivity.this);
 		startStopBtn.setText("OFF");
 		startStopBtn.setTextSize(12);
 		startStopBtn.setHeight(pixels);
 		startStopBtn.setWidth(pixels);
 		startStopBtn.setId(START_STOP_ID_PREFIX + timerId);
 		startStopBtn.setBackgroundColor(Color.BLACK);
 		startStopBtn.setTextColor(Color.LTGRAY);
 		startStopBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_toggle_off));
 		
 		
 		startStopBtn.setOnClickListener(new View.OnClickListener() {
 
 			TimerTask timerTask = null;
 			int counter = seconds;
 
 			
 			public void onClick(View v) {
 
 				if (startStopBtn.isChecked()) {
 					
 		        	startStopBtn.setText("ON");
 		        	startStopBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_toggle_on));
 		        
 					
 					timerTask = new TimerTask() {
 						
 						public void run() {
 							
 							handler.post(new Runnable() {
 
 								public void run() {
 									counter = convertToSeconds(timeText.getText().toString());
 									timeText.setText(formatTimeTextDisplay(counter));
 									counter++;
 									timeText.setText(formatTimeTextDisplay(counter));
 									//isTimeEdited = false;
 									
 									/*
 									 * NOTE: is code below is used, the edited time reverts back to
 									 * original time before the edit
 									if (isTimeEdited) {
 										counter = convertToSeconds(timeText.getText().toString());
 										//timeText.setText(formatTimeTextDisplay(counter));
 										isTimeEdited = false;
 									}
 									
 									if (isTimeEdited == false) {
 										counter++; // seconds
 										timeText.setText(formatTimeTextDisplay(counter));
 									}*/
 								}
 							});
 							
 						}
 					};
 
 					timer.scheduleAtFixedRate(timerTask, 1000, 1000);
 
 				} else {
 					startStopBtn.setText("OFF");
 					startStopBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_toggle_off));
 					handler.removeCallbacks(timerTask);
 
 					if (timerTask != null) {
 						timerTask.cancel();
 					}
 				}
 			}
 		});
 
 		// start the timer if isOn == true
 		if (isOn) {
 			startStopBtn.performClick();
 		}
 		
 		TableRow _tr = new TableRow(mainTl.getContext());
 		_tr.addView(startStopBtn);
 		_tr.addView(innerTl);
 		
 
 		TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(
 				TableLayout.LayoutParams.FILL_PARENT,
 				TableLayout.LayoutParams.FILL_PARENT);
 		tableRowParams.setMargins(0, 2, 0, 2);
 		_tr.setLayoutParams(tableRowParams);
 
 		mainTl.addView(_tr);
 
 		// add timer and task label to inner table
 		ll.addView(mainTl);
 
 		
 		innerTl.setOnTouchListener(new View.OnTouchListener() {
 			
 			public boolean onTouch(View v,MotionEvent evt) {
 				int action = evt.getAction() & MotionEvent.ACTION_MASK;
 				
 				switch(action) {
 						
 					case MotionEvent.ACTION_DOWN:
 						v.setBackgroundColor(Color.WHITE);
 						taskLabel.setTextColor(Color.DKGRAY);
 						timeText.setTextColor(Color.DKGRAY);
 						break;
 					
 					case MotionEvent.ACTION_UP:
 						v.setBackgroundColor(Color.BLACK);
 						taskLabel.setTextColor(Color.LTGRAY);
 						timeText.setTextColor(Color.LTGRAY);
 						break;
 						
 					case MotionEvent.ACTION_CANCEL:
 						v.setBackgroundColor(Color.BLACK);
 						taskLabel.setTextColor(Color.LTGRAY);
 						timeText.setTextColor(Color.LTGRAY);
 						break;
 				}
 				
 				
 				return false;
 			}
 		});
 
 		
 		innerTl.setOnLongClickListener(new View.OnLongClickListener() {
 			public boolean onLongClick(View v) {
 				openContextMenu(v);
 				return true;
 			}
 		});
 		
 		
 		// set long press event
 		registerForContextMenu(mainTl);
 		
 	}
 	
 	
 	// long press context menu
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		menu.setHeaderTitle("Select Option");
 		menu.add(0, (v.getId()), 0, "Edit Time");
 		
 		menu.add(0, (v.getId()), 1, "Edit Label");
 		menu.add(0, v.getId(), 2, "Delete Timer");
 	}
 
 	public boolean onContextItemSelected(final MenuItem item) {
 
 		String menuItemTitle = (String) item.getTitle();
 		
 		
 		if (menuItemTitle == "Delete Timer") {
 			final TextView textView = (TextView) findViewById(TASK_LABEL_ID_PREFIX + item.getItemId());
 			
 			AlertDialog.Builder alert = new AlertDialog.Builder(TaskTimerActivity.this);
 			alert.setTitle("Delete " + textView.getText().toString() + "?");
 		 	
 			
 			alert.setPositiveButton("Ok",
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int whichButton) {
 							// get the innerTl view
 							TableLayout tv = (TableLayout) findViewById(item.getItemId());
 							ll.removeView(tv);
 							
 							db.deleteTimer(item.getItemId());
 						}
 					});
 
 			alert.setNegativeButton("Cancel",
 					new DialogInterface.OnClickListener() {
 
 						public void onClick(DialogInterface dialog,int which) {
 							// TODO Auto-generated method stub
 							return;
 						}
 					});
 			alert.show();
 			
 			
 		} else if (menuItemTitle == "Edit Label") {
 			final TextView textView = (TextView) findViewById(TASK_LABEL_ID_PREFIX + item.getItemId());
 			
 			AlertDialog.Builder alert = new AlertDialog.Builder(TaskTimerActivity.this);
 			alert.setTitle("Edit Label");
 
 			final EditText input = new EditText(TaskTimerActivity.this);
 			input.setSingleLine(); // one line tall
 			input.setText(textView.getText().toString());
 			input.setSelection(input.getText().length());
 			alert.setView(input);
 			
 			alert.setPositiveButton("Ok",
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int whichButton) {
 							textView.setText(input.getText().toString());
 						}
 					});
 
 			alert.setNegativeButton("Cancel",
 					new DialogInterface.OnClickListener() {
 
 						public void onClick(DialogInterface dialog,int which) {
 							// TODO Auto-generated method stub
 							return;
 						}
 					});
 			alert.show();
 		} else if (menuItemTitle == "Edit Time") {
 			
 			final TextView timeText = (TextView) findViewById(TIME_ID_PREFIX + item.getItemId());
 			final TextView taskLabel = (TextView) findViewById(TASK_LABEL_ID_PREFIX + item.getItemId());
 			final ToggleButton startStopBtn = (ToggleButton) findViewById(START_STOP_ID_PREFIX + item.getItemId());
 			final boolean isOn = startStopBtn.isChecked();
 			
 			// if timer is running, stop until finished editing time
 			if (isOn)
 				startStopBtn.performClick();
 			
 			String[] timeArray = timeText.getText().toString().split(":");
 			
 			
 			final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			final View editTimeView = inflater.inflate(R.layout.edit_time, null);
 			
 			final TextView hourText = (TextView) editTimeView.findViewById(R.id.editHourText);
 			final String hour = timeArray[0];
 			hourText.setText(hour);
 			
 			TextView minuteText = (TextView) editTimeView.findViewById(R.id.editMinuteText);
 			String minute = timeArray[1];
 			minuteText.setText(minute);
 			
 			TextView secondText = (TextView) editTimeView.findViewById(R.id.editSecondText);
 			String second = timeArray[2];
 			secondText.setText(second);
 			
 			
 			final Dialog dialog = new Dialog(this);
 			dialog.setTitle("Edit Time for " + taskLabel.getText().toString());
 			dialog.setContentView(editTimeView);
 			dialog.show();
 			
			final int repeatSpeed = 100; // how fast to repeat the action for increment/decrement time
 			
 			final Runnable onPressedIncrementHour = new Runnable() {
 				public void run() {
 					
 					final TextView hText = (TextView) editTimeView.findViewById(R.id.editHourText);
 					int h = Integer.parseInt(hText.getText().toString());
 					//editedHour = formatDoubleDigit(++h);
 					hText.setText(formatDoubleDigit(incrementHour(h)));
 					handler.postAtTime(this, SystemClock.uptimeMillis()+repeatSpeed);
 				}
 			};
 			final Runnable onPressedDecrementHour = new Runnable() {
 				public void run() {
 					
 					final TextView hText = (TextView) editTimeView.findViewById(R.id.editHourText);
 					int h = Integer.parseInt(hText.getText().toString());
 					//editedHour = formatDoubleDigit(++h);
 					hText.setText(formatDoubleDigit(decrementHour(h)));
 					handler.postAtTime(this, SystemClock.uptimeMillis()+repeatSpeed);
 				}
 			};
 			
 			
 			
 			final Runnable onPressedIncrementMinute = new Runnable() {
 				public void run() {
 					
 					final TextView mText = (TextView) editTimeView.findViewById(R.id.editMinuteText);
 					int m = Integer.parseInt(mText.getText().toString());
 					//editedMinute = formatDoubleDigit(++m);
 					mText.setText(formatDoubleDigit(incrementMinuteSecond(m)));
 					handler.postAtTime(this, SystemClock.uptimeMillis()+repeatSpeed);
 				}
 			};
 			final Runnable onPressedDecrementMinute = new Runnable() {
 				public void run() {
 					
 					final TextView mText = (TextView) editTimeView.findViewById(R.id.editMinuteText);
 					int m = Integer.parseInt(mText.getText().toString());
 					//editedMinute = formatDoubleDigit(++m);
 					mText.setText(formatDoubleDigit(decrementMinuteSecond(m)));
 					handler.postAtTime(this, SystemClock.uptimeMillis()+repeatSpeed);
 				}
 			};
 			
 			
 			final Runnable onPressedIncrementSecond = new Runnable() {
 				public void run() {
 					final TextView sText = (TextView) editTimeView.findViewById(R.id.editSecondText);
 					int s = Integer.parseInt(sText.getText().toString());
 					//editedSecond = formatDoubleDigit(++s);
 					sText.setText(formatDoubleDigit(incrementMinuteSecond(s)));
 					handler.postAtTime(this, SystemClock.uptimeMillis()+repeatSpeed);
 				}
 			};
 			final Runnable onPressedDecrementSecond = new Runnable() {
 				public void run() {
 					final TextView sText = (TextView) editTimeView.findViewById(R.id.editSecondText);
 					int s = Integer.parseInt(sText.getText().toString());
 					//editedSecond = formatDoubleDigit(++s);
 					sText.setText(formatDoubleDigit(decrementMinuteSecond(s)));
 					handler.postAtTime(this, SystemClock.uptimeMillis()+repeatSpeed);
 				}
 			};
 			//------------------------------------------------------------------------
 			// set the click function for the increment buttons
 			Button hourUpBtn = (Button) editTimeView.findViewById(R.id.editHourUpBtn);
 			hourUpBtn.setOnClickListener(new View.OnClickListener() {
 				public void onClick(View v) {
 					
 					final TextView hText = (TextView) editTimeView.findViewById(R.id.editHourText);
 					int h = Integer.parseInt(hText.getText().toString());
 					//editedHour = formatDoubleDigit(++h);
 					hText.setText(formatDoubleDigit(incrementHour(h)));
 				}
 			});
 			
 			hourUpBtn.setOnTouchListener(new View.OnTouchListener() {
 				
 				public final boolean onTouch(View v, MotionEvent event) {
 					
 					int action = event.getAction();
 					
 					switch(action) {
 						case MotionEvent.ACTION_DOWN:
 							handler.removeCallbacks(onPressedIncrementHour);
 							handler.postAtTime(onPressedIncrementHour, SystemClock.uptimeMillis()+repeatSpeed);
 							break;
 						case MotionEvent.ACTION_UP:
 							handler.removeCallbacks(onPressedIncrementHour);
 							break;
 					}
 					return false;
 				}
 				
 			});
 			
 			
 			
 			Button minuteUpBtn = (Button) editTimeView.findViewById(R.id.editMinuteUpBtn);
 			minuteUpBtn.setOnClickListener(new View.OnClickListener() {
 				public void onClick(View v) {
 					final TextView mText = (TextView) editTimeView.findViewById(R.id.editMinuteText);
 					int m = Integer.parseInt(mText.getText().toString());
 					//editedMinute = formatDoubleDigit(++m);
 					mText.setText(formatDoubleDigit(incrementMinuteSecond(m)));
 				}
 			});
 			minuteUpBtn.setOnTouchListener(new View.OnTouchListener() {
 				
 				public final boolean onTouch(View v, MotionEvent event) {
 					
 					int action = event.getAction();
 					
 					switch(action) {
 						case MotionEvent.ACTION_DOWN:
 							handler.removeCallbacks(onPressedIncrementMinute);
 							handler.postAtTime(onPressedIncrementMinute, SystemClock.uptimeMillis()+repeatSpeed);
 							break;
 						case MotionEvent.ACTION_UP:
 							handler.removeCallbacks(onPressedIncrementMinute);
 							break;
 					}
 					return false;
 				}
 				
 			});
 
 			
 			Button secondUpBtn = (Button) editTimeView.findViewById(R.id.editSecondUpBtn);
 			secondUpBtn.setOnClickListener(new View.OnClickListener() {
 				public void onClick(View v) {
 					final TextView sText = (TextView) editTimeView.findViewById(R.id.editSecondText);
 					int s = Integer.parseInt(sText.getText().toString());
 					//editedSecond = formatDoubleDigit(++s);
 					sText.setText(formatDoubleDigit(incrementMinuteSecond(s)));
 				}
 			});
 			secondUpBtn.setOnTouchListener(new View.OnTouchListener() {
 				
 				public final boolean onTouch(View v, MotionEvent event) {
 					
 					int action = event.getAction();
 					
 					switch(action) {
 						case MotionEvent.ACTION_DOWN:
 							handler.removeCallbacks(onPressedIncrementSecond);
 							handler.postAtTime(onPressedIncrementSecond, SystemClock.uptimeMillis()+repeatSpeed);
 							break;
 						case MotionEvent.ACTION_UP:
 							handler.removeCallbacks(onPressedIncrementSecond);
 							break;
 					}
 					return false;
 				}
 				
 			});
 			
 			//------------------------------------------------------------------------
 			// set the click function for the decrement buttons
 			Button hourDownBtn = (Button) editTimeView.findViewById(R.id.editHourDownBtn);
 			hourDownBtn.setOnClickListener(new View.OnClickListener() {
 				public void onClick(View v) {
 					final TextView hText = (TextView) editTimeView.findViewById(R.id.editHourText);
 					int h = Integer.parseInt(hText.getText().toString());
 					//editedHour = formatDoubleDigit(++h);
 					hText.setText(formatDoubleDigit(decrementHour(h)));
 				}
 			});
 			hourDownBtn.setOnTouchListener(new View.OnTouchListener() {
 				
 				public final boolean onTouch(View v, MotionEvent event) {
 					
 					int action = event.getAction();
 					
 					switch(action) {
 						case MotionEvent.ACTION_DOWN:
 							handler.removeCallbacks(onPressedDecrementHour);
 							handler.postAtTime(onPressedDecrementHour, SystemClock.uptimeMillis()+repeatSpeed);
 							break;
 						case MotionEvent.ACTION_UP:
 							handler.removeCallbacks(onPressedDecrementHour);
 							break;
 					}
 					return false;
 				}
 				
 			});
 
 			Button minuteDownBtn = (Button) editTimeView.findViewById(R.id.editMinuteDownBtn);
 			minuteDownBtn.setOnClickListener(new View.OnClickListener() {
 				public void onClick(View v) {
 					final TextView mText = (TextView) editTimeView.findViewById(R.id.editMinuteText);
 					int m = Integer.parseInt(mText.getText().toString());
 					//editedMinute = formatDoubleDigit(++m);
 					mText.setText(formatDoubleDigit(decrementMinuteSecond(m)));
 				}
 			});
 			minuteDownBtn.setOnTouchListener(new View.OnTouchListener() {
 				
 				public final boolean onTouch(View v, MotionEvent event) {
 					
 					int action = event.getAction();
 					
 					switch(action) {
 						case MotionEvent.ACTION_DOWN:
 							handler.removeCallbacks(onPressedDecrementMinute);
 							handler.postAtTime(onPressedDecrementMinute, SystemClock.uptimeMillis()+repeatSpeed);
 							break;
 						case MotionEvent.ACTION_UP:
 							handler.removeCallbacks(onPressedDecrementMinute);
 							break;
 					}
 					return false;
 				}
 			});
 
 			Button secondDownBtn = (Button) editTimeView.findViewById(R.id.editSecondDownBtn);
 			secondDownBtn.setOnClickListener(new View.OnClickListener() {
 				public void onClick(View v) {
 					final TextView sText = (TextView) editTimeView.findViewById(R.id.editSecondText);
 					int s = Integer.parseInt(sText.getText().toString());
 					//editedSecond = formatDoubleDigit(++s);
 					sText.setText(formatDoubleDigit(decrementMinuteSecond(s)));
 				}
 			});
 			secondDownBtn.setOnTouchListener(new View.OnTouchListener() {
 				
 				public final boolean onTouch(View v, MotionEvent event) {
 					
 					int action = event.getAction();
 					
 					switch(action) {
 						case MotionEvent.ACTION_DOWN:
 							handler.removeCallbacks(onPressedDecrementSecond);
 							handler.postAtTime(onPressedDecrementSecond, SystemClock.uptimeMillis()+repeatSpeed);
 							break;
 						case MotionEvent.ACTION_UP:
 							handler.removeCallbacks(onPressedDecrementSecond);
 							break;
 					}
 					return false;
 				}
 				
 			});
 			
 			
 			Button editTimeOkBtn = (Button) editTimeView.findViewById(R.id.editTimeOkBtn);
 			editTimeOkBtn.setOnClickListener(new View.OnClickListener() {
 				public void onClick(View v) {
 					
 					
 					final TextView hText = (TextView) editTimeView.findViewById(R.id.editHourText);
 					int h = Integer.parseInt(hText.getText().toString());
 					
 					final TextView mText = (TextView) editTimeView.findViewById(R.id.editMinuteText);
 					int m = Integer.parseInt(mText.getText().toString());
 					
 					final TextView sText = (TextView) editTimeView.findViewById(R.id.editSecondText);
 					int s = Integer.parseInt(sText.getText().toString());
 					
 					timeText.setText(formatDoubleDigit(h) + ":" + formatDoubleDigit(m) + ":" + formatDoubleDigit(s));
 					//isTimeEdited = true;
 					
 					// restart the timer
 					if (isOn)
 						startStopBtn.performClick();
 					
 					dialog.dismiss();
 				}
 			});
 			
 			Button editTimeCancelBtn = (Button) editTimeView.findViewById(R.id.editTimeCancelBtn);
 			editTimeCancelBtn.setOnClickListener(new View.OnClickListener() {
 				public void onClick(View v) {
 					//isTimeEdited = false;
 					
 					// restart the timer
 					if (isOn)
 						startStopBtn.performClick();
 					
 					dialog.dismiss();
 				}
 			});
 			
 		}
 		
 		tracker.trackEvent("ContextMenu", "onContextItemSelected", menuItemTitle, -1);
 		return true;
 	}
 
 	protected void onSaveInstanceState(Bundle outState) {
 		
 	}
 	
 	@Override
     protected void onStart() {
         super.onStart();
         // The activity is about to become visible.
         
     }
     @Override
     protected void onResume() {
     	
         super.onResume();
         // The activity has become visible (it is now "resumed").
         
     }
     @Override
     protected void onPause() {
         super.onPause();
         // Another activity is taking focus (this activity is about to be "paused").
         saveTimers();
       
     }
     @Override
     protected void onStop() {
         super.onStop();
         // The activity is no longer visible (it is now "stopped")
         
     }
     
     @Override
     protected void onDestroy() {
         super.onDestroy();
         
         saveTimers();
         
         // The activity is about to be destroyed.
         timer.cancel();
         
         if (cursor != null)
         	cursor.close();
         
         
         if (db != null)
         	db.close();
         
         // stop google analytics tracker
         tracker.stopSession();
         
     }
     
     private void saveTimers() {
     	
     	
         cursor = db.fetchAllTimers();
         startManagingCursor(cursor);
         
         if (cursor != null) {
 			cursor.moveToFirst();
 			while (cursor.isAfterLast() == false) {
 				// KEY_ROWID, KEY_LABEL, KEY_SECONDS, KEY_IS_ON
 				int id = cursor.getInt(0);
 				
 				TableLayout tv = (TableLayout) findViewById(id);
 	        	
 	        	if (tv == null) continue; // none found; continue to next iteration
 	        
 	        	
 	        	
 	        	// table layout found, which means a timer also exists; save the time value
 	        	TextView timeValue = (TextView) findViewById(10000+id);
 	        	int seconds = convertToSeconds(timeValue.getText().toString());
 	        	
 	        	
 	        	// save the timer label
 	        	TextView labelValue = (TextView) findViewById(20000+id);
 	        	String label = labelValue.getText().toString();
 	        	
 	        	
 	        	// save the state of the timer; running or not
 	        	ToggleButton btn = (ToggleButton) findViewById(30000+id);
 	        	boolean isOn = btn.isChecked();
 	        	
 	        	
 	        	// save the timestamp; used for timers that are active when activity is destroyed
 	        	Calendar cal = Calendar.getInstance();
 	        	long timestamp = cal.getTimeInMillis();
 	        	
 	        	
 	        	db.updateTimer(id, label, seconds, timestamp, isOn);
 	        	
 				cursor.moveToNext();
 			}
 		}
         
         
         
     }
     
     private int convertToSeconds(String t) {
     	// t variable is in the format of: 00:00:00
     	// 								hh:mm:ss
     	int sec = 0;
     	
     	String[] timeStr = t.split(":");
     	if (timeStr[0] != "00") {
     		// convert hours into seconds
     		int h = Integer.parseInt(timeStr[0]) * 60 * 60;
     		sec += h;
     	}
     	
     	if (timeStr[1] != "00") {
     		// convert minutes into seconds
     		int m = Integer.parseInt(timeStr[1]) * 60;
     		sec+= m;
     	}
     	
     	if (timeStr[2] != "00") {
     		// convert minutes into seconds
     		int s = Integer.parseInt(timeStr[2]);
     		sec+= s;
     	}
     	
     	 
     	return sec;
     }
     
     private String formatDoubleDigit(int num) {
     	if (num < 10)
     		return "0"+num;
     	
     	return num+"";
     }
     
     private String formatTimeTextDisplay(int seconds) {
     	int hour = seconds / 3600;
 		int rem = seconds % 3600;
 		int min = rem / 60;
 		int sec = rem % 60;
 		
 		return String.format("%02d:%02d:%02d", hour, min, sec);
     }
     
     private int incrementHour(int num) {
     	
     	return ++num;
     }
     
     private int incrementMinuteSecond(int num) {
     	if (num + 1 > 59)
     		return 0;
     	
     	return ++num;
     }
     
     private int decrementHour(int num) {
     	if (num - 1 < 0)
     		return 0;
     	
     	return --num;
     }
     
     private int decrementMinuteSecond(int num) {
     	if (num - 1 < 0)
     		return 59;
     	
     	return --num;
     }
     
     
     private static final int SWIPE_MIN_DISTANCE = 120;
     private static final int SWIPE_MAX_OFF_PATH = 250;
     private static final int SWIPE_THRESHOLD_VELOCITY = 200;
     private GestureDetector gestureDetector;
     View.OnTouchListener gestureListener;
     
 	class MyGestureDetector extends SimpleOnGestureListener {
         @Override
         public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
             try {
                 if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                     return false;
                 // right to left swipe
                 if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                     //Toast.makeText(TaskTimerActivity.this, "Left Swipe", Toast.LENGTH_SHORT).show();
                 	Intent i = new Intent();
     				i.setClass(TaskTimerActivity.this, MoreScreen.class);
     				startActivity(i);
                 }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                     //Toast.makeText(TaskTimerActivity.this, "Right Swipe", Toast.LENGTH_SHORT).show();
                 	// do nothing
                 }
             } catch (Exception e) {
                 // nothing
             }
             return false;
         }
         
 
     }
 
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 		//Toast t = Toast.makeText(v.getContext(), "gesture on Click", Toast.LENGTH_SHORT);
 		//t.show();
 		
 	}
 }
