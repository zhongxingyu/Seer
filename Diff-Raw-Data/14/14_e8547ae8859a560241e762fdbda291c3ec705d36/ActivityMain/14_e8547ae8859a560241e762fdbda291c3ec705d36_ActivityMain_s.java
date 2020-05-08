 package com.jeffthefate.buzztimer;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 
 import net.simonvt.widget.NumberPicker;
 import net.simonvt.widget.NumberPicker.Formatter;
 import net.simonvt.widget.NumberPicker.OnValueChangeListener;
 import android.content.ComponentName;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.graphics.Typeface;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.graphics.drawable.TransitionDrawable;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.support.v4.app.FragmentActivity;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 import android.widget.Button;
 import android.widget.CheckedTextView;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 import com.jeffthefate.buzztimer.TimerService.TimerBinder;
 import com.jeffthefate.buzztimer.TimerService.UiCallback;
 
 public class ActivityMain extends FragmentActivity implements UiCallback {
     
     private TextView minText;
     private NumberPicker minPicker;
     private TextView secText;
     private TextView msecText;
     private NumberPicker secPicker;
     private RelativeLayout timeLayout;
     private TextView colonText;
     
     private CheckedTextView loopCheck;
     private Button doneButton;
     
     private ImageView background;
     
     private SharedPreferences sharedPrefs;
     
     private boolean bound;
     
     private int newMin = 1;
     private int newSec = 0;
     
     private UiCallback uiCallback = this;
     
     private int rawIndex = -1;
     private Field[] fields;
     private ArrayList<Integer> fieldsList;
     private String currentBackground = null;
     
     private Drawable backgroundDrawable;
     private Drawable tempDrawable;
     private Drawable[] arrayDrawable = new Drawable[2];
     private TransitionDrawable transitionDrawable;
     private BitmapDrawable oldBitmapDrawable = null;
     
     private Resources res;
 
 	@Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Typeface font = Typeface.createFromAsset(getAssets(),
         		"fonts/digital-7.ttf");
         res = getResources();
         setContentView(R.layout.main);
         fields = R.drawable.class.getFields();
         fieldsList = new ArrayList<Integer>();
         for (Field field : fields) {
             if (field.getName().contains("background")) {
                 try {
                     fieldsList.add(field.getInt(null));
                 } catch (IllegalArgumentException e1) {e1.printStackTrace();
                 } catch (IllegalAccessException e1) {e1.printStackTrace();}
             }
         }
         background = (ImageView) findViewById(R.id.Background);
         minText = (TextView) findViewById(R.id.MinuteText);  
         minText.setTypeface(font);
         minPicker = (NumberPicker) findViewById(R.id.MinutePicker);
         minPicker.setFormatter(new TimerFormatter());
         minPicker.setMaxValue(59);
         minPicker.setMinValue(0);
         minPicker.setOnValueChangedListener(new OnValueChangeListener() {
             @Override
             public void onValueChange(NumberPicker picker, int oldVal,
                     int newVal) {
                 newMin = newVal;
                 setTime(newMin, newSec);
             }
         });
         minPicker.setTypeface(font);
         secText = (TextView) findViewById(R.id.SecondText);
         secText.setTypeface(font);
         secPicker = (NumberPicker) findViewById(R.id.SecondPicker);
         secPicker.setFormatter(new TimerFormatter());
         secPicker.setMaxValue(59);
         secPicker.setMinValue(0);
         secPicker.setOnValueChangedListener(new OnValueChangeListener() {
             @Override
             public void onValueChange(NumberPicker picker, int oldVal,
                     int newVal) {
                 newSec = newVal;
                 if (oldVal == 0 && newVal == 59)
                 	minPicker.setValue(--newMin);
                 else if (oldVal == 59 && newVal == 0)
                 	minPicker.setValue(++newMin);
                 newMin = minPicker.getValue();
                 setTime(newMin, newSec);
             }
         });
         secPicker.setTypeface(font);
         msecText = (TextView) findViewById(R.id.MillisecondText);
         msecText.setTypeface(font);
         timeLayout = (RelativeLayout) findViewById(R.id.TimeLayout);
         timeLayout.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 if (ApplicationEx.getMsecs() < 5000) {
                     ApplicationEx.mToast.setText("Must be at least 5 seconds");
                     ApplicationEx.mToast.show();
                 }
                 else {
                     if (mService != null) {
                         updateTime(ApplicationEx.getMsecs(), false);
                         if (!mService.isTimerRunning()) {
                             mService.setTime(loopCheck.isChecked());
                             mService.startTimer();
                         }
                         else {
                             mService.stopTimer();
                             mService.setTime(loopCheck.isChecked());
                         }
                     }
                 }
             }
         });
         timeLayout.setOnLongClickListener(new OnLongClickListener() {
             @Override
             public boolean onLongClick(View v) {
             	if (mService.isTimerRunning())
             		mService.stopTimer();
                 if (minPicker.getVisibility() == View.GONE &&
                         secPicker.getVisibility() == View.GONE) {
                     colonText.setVisibility(View.INVISIBLE);
                     minPicker.setVisibility(View.VISIBLE);
                     secPicker.setVisibility(View.VISIBLE);
                     loopCheck.setVisibility(View.VISIBLE);
                     doneButton.setVisibility(View.VISIBLE);
                     minText.setVisibility(View.INVISIBLE);
                     secText.setVisibility(View.INVISIBLE);
                     msecText.setVisibility(View.INVISIBLE);
                     newMin = minPicker.getValue();
                     newSec = secPicker.getValue();
                 }
                 return true;
             }
         });
         colonText = (TextView) findViewById(R.id.ColonText);
         colonText.setTypeface(font);
         loopCheck = (CheckedTextView) findViewById(R.id.LoopCheck);
         loopCheck.setVisibility(View.INVISIBLE);
         loopCheck.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				loopCheck.toggle();
 				sharedPrefs.edit().putBoolean(getString(R.string.loop_key),
                         loopCheck.isChecked()).commit();
 			}
         });
         loopCheck.setTypeface(font);
         doneButton = (Button) findViewById(R.id.DoneButton);
         doneButton.setVisibility(View.INVISIBLE);
         doneButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View arg0) {
                 if (newMin < 1 && newSec < 5) {
                     ApplicationEx.mToast.setText("Must be at least 5 seconds");
                     ApplicationEx.mToast.show();
                 }
                 else if (newMin == 60 && newSec > 0) {
                     ApplicationEx.mToast.setText("Must be at most 60 minutes");
                     ApplicationEx.mToast.show();
                 }
                 else {
                 	ApplicationEx.dbHelper.setTime(ApplicationEx.getMsecs());
                     setTime(newMin, newSec);
                     if (mService != null) {
                         if (mService.isTimerRunning())
                             mService.stopTimer();
                         mService.setTime(loopCheck.isChecked());
                     }
                     minText.setVisibility(View.VISIBLE);
                     secText.setVisibility(View.VISIBLE);
                     msecText.setVisibility(View.VISIBLE);
                     minPicker.setVisibility(View.GONE);
                     secPicker.setVisibility(View.GONE);
                     loopCheck.setVisibility(View.INVISIBLE);
                     doneButton.setVisibility(View.INVISIBLE);
                     colonText.setVisibility(View.VISIBLE);
                 }
                 if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO &&
                 		Build.SERIAL.equals("XXXXXXXXXXXXXXXX"))
                 	setBackground(ApplicationEx.dbHelper.getCurrBackground(),
                 			true);
             }
         });
         doneButton.setTypeface(font);
         if (savedInstanceState != null) {
             minText.setText(savedInstanceState.getString("minText"));
             secText.setText(savedInstanceState.getString("secText"));
             msecText.setText(savedInstanceState.getString("msecText"));
             newMin = savedInstanceState.getInt("newMin");
             newSec = savedInstanceState.getInt("newSec");
             colonText.setVisibility(savedInstanceState.getInt("colonTextVis"));
             minPicker.setVisibility(savedInstanceState.getInt("minPickerVis"));
             secPicker.setVisibility(savedInstanceState.getInt("secPickerVis"));
             loopCheck.setVisibility(savedInstanceState.getInt("loopCheckVis"));
             doneButton.setVisibility(
                     savedInstanceState.getInt("doneButtonVis"));
             minText.setVisibility(savedInstanceState.getInt("minTextVis"));
             secText.setVisibility(savedInstanceState.getInt("secTextVis"));
             msecText.setVisibility(savedInstanceState.getInt("msecTextVis"));
         }
         else
             updateTime(ApplicationEx.getMsecs(), true);
         sharedPrefs = PreferenceManager.getDefaultSharedPreferences(
                 ApplicationEx.getApp());
         minPicker.setValue(newMin);
         secPicker.setValue(newSec);
     }
     
     @Override
     public void onSaveInstanceState(Bundle outState) {
         outState.putString("minText", minText.getText().toString());
         outState.putString("secText", secText.getText().toString());
         outState.putString("msecText", msecText.getText().toString());
         outState.putInt("newMin", newMin);
         outState.putInt("newSec", newSec);
         outState.putInt("colonTextVis", colonText.getVisibility());
         outState.putInt("minPickerVis", minPicker.getVisibility());
         outState.putInt("secPickerVis", secPicker.getVisibility());
         outState.putInt("loopCheckVis", loopCheck.getVisibility());
         outState.putInt("doneButtonVis", doneButton.getVisibility());
         outState.putInt("minTextVis", minText.getVisibility());
         outState.putInt("secTextVis", secText.getVisibility());
         outState.putInt("msecTextVis", msecText.getVisibility());
         super.onSaveInstanceState(outState);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
     
     @Override
     public void onResume() {
         super.onResume();
         if (mService == null) {
             Intent timerIntent = new Intent(getApplicationContext(),
                     TimerService.class);
             getApplicationContext().startService(timerIntent);
         }
         bindService(new Intent(getApplicationContext(), TimerService.class),
                 mConnection, 0);
         ApplicationEx.setActive();
         if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO &&
         		Build.SERIAL.equals("XXXXXXXXXXXXXXXX"))
         	setBackground(ApplicationEx.dbHelper.getCurrBackground(), false);
     }
     
     @Override
     public void onPause() {
         if (bound) {
             unbindService(mConnection);
             bound = false;
         }
         ApplicationEx.setInactive();
         super.onPause();
     }
     
     private static TimerService mService;
     
     private ServiceConnection mConnection = new ServiceConnection() {
         @Override
         public void onServiceConnected(ComponentName name, IBinder service) {
             TimerBinder binder = (TimerBinder) service;
             mService = binder.getService();
             mService.setUiCallback(uiCallback);
             bound = true;
         }
         @Override
         public void onServiceDisconnected(ComponentName name) {
             bound = false;
         }
     };
     
     @Override
     public void updateTime(int mSecs, boolean setLocal) {
         int mins = (int)((mSecs-(mSecs%60000))/60000);
         int secs = (int)((mSecs%60000)/1000);
         if (setLocal) {
         	newMin = mins;
         	newSec = secs;
         }
         int millis = (int)((mSecs%1000)/100);
         minText.setText(mins < 10 ? "0" + mins : Integer.toString(mins));
         secText.setText(secs < 10 ? "0" + secs : Integer.toString(secs));
         msecText.setText(Integer.toString(millis));
     }
     
     private void setTime(int minutes, int seconds) {
         ApplicationEx.setMsecs((minutes*60000)+(seconds*1000));
         updateTime((minutes*60000)+(seconds*1000), false);
     }
     
     private class TimerFormatter implements Formatter {
         @Override
         public String format(int value) {
             if (value < 10)
                 return "0" + value;
             else
                 return Integer.toString(value);
         }
     }
     
     public void setBackground(String name, boolean showNew) {
         if (Build.VERSION.SDK_INT <
                 Build.VERSION_CODES.HONEYCOMB)
             new SetBackgroundTask(name, showNew).execute();
         else
             new SetBackgroundTask(name, showNew).executeOnExecutor(
                     AsyncTask.THREAD_POOL_EXECUTOR);
     }
     
     private class SetBackgroundTask extends AsyncTask<Void, Void, Void> {
         private String name;
         private boolean showNew;
         private int resourceId;
         
         private SetBackgroundTask(String name, boolean showNew) {
             this.name = name;
             this.showNew = showNew;
         }
         
         @Override
         protected Void doInBackground(Void... nothing) {
             if (name == null)
                 name = "background1";
             resourceId = res.getIdentifier(name, "drawable", getPackageName());
             if (showNew) {
                 rawIndex = fieldsList.indexOf(resourceId);
                 if (rawIndex < 0)
                     rawIndex = fieldsList.indexOf(R.drawable.background1);
                 rawIndex++;
                 if (rawIndex >= fieldsList.size())
                     rawIndex = 0;
                 int currentId = fieldsList.get(rawIndex);
                 currentBackground = res.getResourceEntryName(currentId);
                 ApplicationEx.dbHelper.setCurrBackground(currentBackground);
                 if (currentId != resourceId) {
                     try {
                         tempDrawable = res.getDrawable(currentId);
                         /*
                         if (backgroundDrawable != null &&
                         		backgroundDrawable instanceof
                         			TransitionDrawable) {
                         	((BitmapDrawable)(
                         			((TransitionDrawable)backgroundDrawable)
                         			.getDrawable(0))).getBitmap().recycle();
                         }
                         if (transitionDrawable != null) {
                         	((BitmapDrawable)(transitionDrawable.getDrawable(0)))
 		            				.getBitmap().recycle();
                         }
                         */
                         backgroundDrawable = background.getDrawable();
                         /*
                         if (oldBitmapDrawable != null)
                         	oldBitmapDrawable.getBitmap().recycle();
                     	*/
                         if (backgroundDrawable != null &&
                         		backgroundDrawable instanceof
                         			TransitionDrawable) {
                            transitionDrawable = (TransitionDrawable) backgroundDrawable;
                             oldBitmapDrawable = (BitmapDrawable)(
                                     transitionDrawable.getDrawable(1));
                             /*
                             ((BitmapDrawable)(transitionDrawable.getDrawable(0)))
                     				.getBitmap().recycle();
             				*/
                         }
                         else if (backgroundDrawable != null &&
                         		backgroundDrawable instanceof BitmapDrawable)
                            oldBitmapDrawable = (BitmapDrawable) backgroundDrawable;
                         if (backgroundDrawable != null) {
                             arrayDrawable[0] = oldBitmapDrawable;
                             arrayDrawable[1] = tempDrawable;
                         }
                     } catch (OutOfMemoryError memErr) {
                     	setBackground(currentBackground, showNew);
                     } catch (Resources.NotFoundException e) {
                         setBackground(currentBackground, showNew);
                     }
                 }
                 else
                     setBackground(currentBackground, showNew);
             }
             ApplicationEx.dbHelper.setCurrBackground(currentBackground);
             return null;
         }
         
         @Override
         protected void onCancelled(Void nothing) {
         }
         
         @Override
         protected void onPostExecute(Void nothing) {
         	background.setVisibility(View.VISIBLE);
             if (showNew) {
                 if (backgroundDrawable != null) {
                     transitionDrawable = new TransitionDrawable(arrayDrawable);
                     transitionDrawable.setCrossFadeEnabled(true);
                     background.setImageDrawable(transitionDrawable);
                     transitionDrawable.startTransition(500);
                 }
                 else
                     background.setImageDrawable(tempDrawable);
             }
             else {
                 if (fieldsList.indexOf(resourceId) >= 0)
                     background.setImageResource(resourceId);
                 else
                     background.setImageResource(R.drawable.background1);
             }
         }
     }
 
 }
