 package me.gods.raintimer;
 
 import java.util.Calendar;
 import java.util.Locale;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnMultiChoiceClickListener;
 import android.content.SharedPreferences;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.util.SparseBooleanArray;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.RadioButton;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class TimerFragment extends Fragment {
     private TextView favoriteEvents;
     private RadioButton[] radios = new RadioButton[6];
     private int[] radioIds = new int[6];
     private FavoriteRadioListener frl = new FavoriteRadioListener();
     private Spinner eventSpinner;
     private ArrayAdapter<String> adapter;
     private static TextView timerText;
     private static Button switcherButton;
 
     private SharedPreferences settings;
     private String[] events;
     private int eventLength;
     private String[] candidateFavorite;
     private boolean[] candidateChecked;
     private JSONArray favoriteArray;
 
     private enum State {
         reset,
         running,
         pause,
         stop
     };
 
     private State state;
     private static long startTime;
     private static long offsetTime;
     private TimerThread timerThread;
     private String currentEvent;
     private SQLiteDatabase db;
 
     final static Handler handler = new Handler(){
         public void handleMessage(Message msg){
             switch (msg.what) {
             case 1:
                 long minus = System.currentTimeMillis() - startTime;
                 timerText.setText(millisToTime(offsetTime + minus));
 
                 break;
             default:
                 break;
             }
 
             super.handleMessage(msg);
         }
     };
 
     @Override
     public void onCreate (Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         this.state = State.reset;
     }
 
     @Override
     public void onResume () {
         super.onResume();
     }
 
     @Override
     public void onPause () {
         super.onPause();
 
         db.close();
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View v = inflater.inflate(R.layout.fragment_timer, container, false);
 
         db = getActivity().openOrCreateDatabase("raintimer.db", Context.MODE_PRIVATE, null);
         db.execSQL("CREATE TABLE IF NOT EXISTS history (_id INTEGER PRIMARY KEY AUTOINCREMENT, event_name VARCHAR, total_time INT, commit_date VARCHAR)");
 
         settings = this.getActivity().getPreferences(Activity.MODE_PRIVATE);
         String eventList = settings.getString(PreferenceFragment.EVENT_LIST, "[]");
 
         JSONArray eventArray = null;
         try {
             eventArray = new JSONArray(eventList);
         } catch (JSONException e) {
             e.printStackTrace();
         }
 
         eventLength = eventArray.length();
         events = new String[eventLength + 1];
 
         events[0] = getString(R.string.default_event);
 
         for (int i = 1; i < eventLength + 1; i++) {
             try {
                 events[i] = eventArray.getString(i - 1);
             } catch (JSONException e) {
                 e.printStackTrace();
             }
         }
 
         String favoriteList = settings.getString(PreferenceFragment.FAVORITE_EVENT_LIST, "[]");
         favoriteArray = null;
         try {
             favoriteArray = new JSONArray(favoriteList);
         } catch (JSONException e) {
             e.printStackTrace();
         }
 
         candidateFavorite = new String[eventLength];
         candidateChecked = new boolean[eventLength];
 
         for (int i = 0; i < eventLength; i++) {
             try {
                 candidateFavorite[i] = eventArray.getString(i);
             } catch (JSONException e) {
                 e.printStackTrace();
             }
         }
 
         for (int i = 0; i < favoriteArray.length(); i++) {
             int j;
             for (j = 0; j < candidateFavorite.length; j++) {
                 try {
                     if (favoriteArray.getString(i).equals(candidateFavorite[j])) {
                         candidateChecked[j] = true;
                         break;
                     }
                 } catch (JSONException e) {
                     e.printStackTrace();
                 }
             }
         }
 
         radioIds[0] = R.id.radio_0;
         radioIds[1] = R.id.radio_1;
         radioIds[2] = R.id.radio_2;
         radioIds[3] = R.id.radio_3;
         radioIds[4] = R.id.radio_4;
         radioIds[5] = R.id.radio_5;
 
         for (int i = 0; i < 6; i++) {
             radios[i] = (RadioButton)v.findViewById(radioIds[i]);
             radios[i].setOnClickListener(frl);
         }
 
         updateFavoriteRadios();
 
         favoriteEvents = (TextView)v.findViewById(R.id.favorite_events);
         favoriteEvents.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 AlertDialog dialog = new AlertDialog.Builder(getActivity())
                 .setIcon(android.R.drawable.btn_star_big_on)
                 .setTitle("Choose Favorite")
                 .setMultiChoiceItems(candidateFavorite, candidateChecked, new OnMultiChoiceClickListener() {
 
                     @Override
                     public void onClick(DialogInterface arg0, int which, boolean isChecked) {
                         int count = 0;
                         for (int i = 0; i < eventLength; i++) {
                             if (candidateChecked[i]) {
                                 Log.i("caca", "checked" + count);
                                 count ++;
                             }
                             if (count > 6) {
                                 Toast.makeText(getActivity(), "No more than 6:)", Toast.LENGTH_SHORT).show();
                                 ((AlertDialog) arg0).getButton(arg0.BUTTON_POSITIVE).setEnabled(false);
                             } else {
                                 ((AlertDialog) arg0).getButton(arg0.BUTTON_POSITIVE).setEnabled(true);
                             }
                         }
                     }
 
                 })
                 .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface arg0, int arg1) {
                         SparseBooleanArray Checked = ((AlertDialog) arg0).getListView().getCheckedItemPositions();
 
                         favoriteArray = new JSONArray();
                        for (int i = 0; i < eventLength - 1; i++) {
                             if (Checked.get(i)) {
                                 favoriteArray.put(candidateFavorite[i]);
                             }
                         }
 
                         settings.edit().putString(PreferenceFragment.FAVORITE_EVENT_LIST, favoriteArray.toString()).commit();
 
                         updateFavoriteRadios();
                     }
                 })
                 .setNegativeButton("Cancel",  null).create();
                 dialog.show(); 
             }
         });
 
         eventSpinner = (Spinner)v.findViewById(R.id.event_spinner);
         adapter = new ArrayAdapter<String>(this.getActivity(), R.layout.spinner_text, events);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         eventSpinner.setAdapter(adapter);
 
         eventSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
 
             @Override
             public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                 currentEvent = events[arg2];
 
                 if (arg2 == 0) {
                     for (int i = 0; i < 6; i++) {
                         radios[i].setChecked(false);
                     }
                 }
             }
 
             @Override
             public void onNothingSelected(AdapterView<?> arg0) {
             }
         });
 
         timerText = (TextView)v.findViewById(R.id.timer_text);
         timerText.setOnClickListener(new View.OnClickListener() {
 
             @Override
             public void onClick(View arg0) {
                 if (state == State.running) {
                     state = State.pause;
                     try {
                         timerThread.join();
                     } catch (InterruptedException e) {
                         e.printStackTrace();
                     }
                     offsetTime += System.currentTimeMillis() - startTime;
                     startTime = System.currentTimeMillis();
                 } else if (state == State.pause) {
                     state = State.running;
                     startTime = System.currentTimeMillis();
                     timerThread = new TimerThread();
                     timerThread.start();
                 }
             }
         });
 
         switcherButton = (Button)v.findViewById(R.id.timer_switcher);
         switcherButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 switch(state) {
                     case reset:
                         state = State.running;
                         startTime = System.currentTimeMillis();
                         timerThread = new TimerThread();
                         timerThread.start();
 
                         break;
                     case running:
                     case pause:
                         state = State.stop;
                         try {
                             timerThread.join();
                         } catch (InterruptedException e) {
                             e.printStackTrace();
                         }
 
                         offsetTime += System.currentTimeMillis() - startTime;
                         startTime = System.currentTimeMillis();
 
                         if (currentEvent != null && currentEvent != getString(R.string.default_event)) {
                             new Builder(getActivity())
                                     .setMessage("Save this?")
                                     .setTitle("Info")
                                     .setPositiveButton("Save", new DialogInterface.OnClickListener() {
 
                                         @Override
                                         public void onClick(DialogInterface arg0, int arg1) {
                                             String sql = "insert into history values(null, ?, ?, ?)";
                                             Calendar now = Calendar.getInstance();
                                             String dateString = String.format("%02d-%02d-%04d", now.get(Calendar.MONTH) + 1, now.get(Calendar.DATE), now.get(Calendar.YEAR));
                                             Object[] bindArgs = new Object[] {currentEvent, offsetTime, dateString};
                                             db.execSQL(sql, bindArgs);
                                         }
                                     })
                                     .setNegativeButton("Discard", null).show();
                         }
 
                         break;
                     case stop:
                         state = State.reset;
                         timerText.setText("0'00\"00");
                         offsetTime = 0;
 
                         break;
                     default:
                         break;
                 }
 
                 updateButton();
             }
         });
 
         updateButton();
 
         return v;
     }
 
     public static String millisToTime(long millis) {
         long millisecond = (millis / 10) % 100;
         long second = (millis / 1000) % 60;
         long minute = (millis / 1000 / 60) % 60;
 
         return String.format(Locale.getDefault(), "%d'%02d\"%02d", minute, second, millisecond);
     }
 
     public void updateFavoriteRadios() {
         int i;
         int length = favoriteArray.length();
 
         for (i = 0; i < length; i++) {
             try {
                 radios[i].setText(favoriteArray.getString(i));
                 radios[i].setVisibility(View.VISIBLE);
             } catch (Exception e) {
                 Log.e("RADIO", "Cannot set text");
             }
         }
 
         for (;i < 6; i++) {
             radios[i].setVisibility(View.GONE);
         }
     }
 
     public void updateButton() {
         switch(state) {
             case reset:
                 switcherButton.setText("Start");
                 break;
             case running:
                 switcherButton.setText("Stop");
                 break;
             case stop:
                 switcherButton.setText("Reset");
                 break;
             default:
                 break;
         }
     }
 
     public class TimerThread extends Thread {
         @Override
         public void run(){
             while(state == TimerFragment.State.running){
                 try{
                     Thread.sleep(10);
                     Message message = new Message();
                     message.what = 1;
                     handler.sendMessage(message);
                 }catch (Exception e) {
 
                 }
             }
         }
     }
 
     public class FavoriteRadioListener implements View.OnClickListener {
 
         @Override
         public void onClick(View v) {
             for (int i = 0; i < 6; i++) {
                 if (radioIds[i] == v.getId()) {
                     radios[i].setChecked(true);
                     currentEvent = radios[i].getText().toString();
 
                     for (int j = 0; j < eventLength + 1; j++) {
                         if (currentEvent.equals(events[j])) {
                             eventSpinner.setSelection(j);
                         }
                     }
                     Log.i("Caca", currentEvent);;
                 }else {
                     radios[i].setChecked(false);
                 }
             }
         }
         
     }
 }
