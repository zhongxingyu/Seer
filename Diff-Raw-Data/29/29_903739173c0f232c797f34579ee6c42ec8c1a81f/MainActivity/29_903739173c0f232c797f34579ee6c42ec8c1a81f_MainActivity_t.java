 package com.example.rest;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.TimePickerDialog;
 import android.app.TimePickerDialog.OnTimeSetListener;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TimePicker;
 import android.widget.Toast;
 
 public class MainActivity extends Activity implements OnClickListener {
 
 	protected String LOG_TAG = "MyLog";
 
 	private EditText[] timer = new EditText[6];
 	
 	private Button installTimer;
 	
 	private AlarmManagerBroadcastReceiver alarm;
 	
 	private SharedPreferences sPref;
 	
 	private int count = 0;
 	private static int START_TIME = 0;
 	private static int END_TIME = 2;
 	private static int PERIODIC_TIME = 4;
 	private int check = 0;
 	private int setHour = 0;
 	private int setMinute = 0;
 	private int periodicHour = 0;
 	private int periodicMinute = 0;
 	
 	private String SAVED_TEXT = "saved check";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		installTimer = (Button) findViewById(R.id.set);
 
 		
 		timer[0] = (EditText) findViewById(R.id.startHours);
 		timer[1] = (EditText) findViewById(R.id.startMinute);
 		timer[2] = (EditText) findViewById(R.id.endHours);
 		timer[3] = (EditText) findViewById(R.id.endMinute);
 		timer[4] = (EditText) findViewById(R.id.periodicHours);
 		timer[5] = (EditText) findViewById(R.id.periodicMinute);
 
 		installTimer.setOnClickListener(this);
 		
 		for(int i=0; i<=5; i++) {
 			timer[i].setOnClickListener(this);
 		}
 		
 		loadCheck();
 		
 		for (int i=0;i<=5;i++) {
 			Log.d(LOG_TAG, "i ="+i);
 			timer[i].setOnTouchListener(new OnTouchListener(){
 				@SuppressWarnings("deprecation")
 				@Override
 				public boolean onTouch(View arg0, MotionEvent arg1) {
 					// TODO Auto-generated method stub
 					
 					switch(arg0.getId()){
 						case R.id.startHours:
 							showDialog(START_TIME);
 							count=START_TIME;
 							break;
 						case R.id.startMinute:
 							showDialog(START_TIME);
 							count = START_TIME;
 							break;
 						case R.id.endHours:
 							showDialog(END_TIME);
 							count = END_TIME;
 							break;
 						case R.id.endMinute:
 							showDialog(END_TIME);
 							count = END_TIME;
 							break;
 						case R.id.periodicHours:
 							showDialog(PERIODIC_TIME);
 							count = PERIODIC_TIME;
 							break;
 						case R.id.periodicMinute:
 							showDialog(PERIODIC_TIME);
 							count = PERIODIC_TIME;
 							break;
 					}
 					
 					return false;
 				}
 			});
 		}
 		
 		alarm = new AlarmManagerBroadcastReceiver();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 	
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch(item.getItemId()) {
 			case R.id.menu_help:
 			Intent Help = new Intent(MainActivity.this, Help.class);
 			startActivity(Help);
 		}
 	
 		return super.onOptionsItemSelected(item);
 		
 	}
 
 	@SuppressWarnings("deprecation")
 	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 		switch(v.getId()) {
 			case R.id.set:
 				if (check == 0) {
 					String[] stringTimer = new String[6];
 					int[] intTimer = new int[6];
 					boolean chekerTime = true;
 					for (int i=0;i<=5;i++) {
 						stringTimer[i] = timer[i].getText().toString();
 						if (stringTimer[i].length() == 0) {
 							Toast.makeText(this, "все поля должны быть заполнены", Toast.LENGTH_SHORT).show();
 							chekerTime = false;
 							break;
 						} else {
 							intTimer[i] = Integer.valueOf(stringTimer[i]);
 							if (i == 5 && intTimer[4] == 0 && intTimer[5] == 0) {
 								Toast.makeText(this, "должны быть время между отдыхом", Toast.LENGTH_LONG).show();
 								showDialog(PERIODIC_TIME);
 								chekerTime = false;
 								break;
 							}
 						}
 					}
 					if (!chekerTime) {
 						return;
 					}
 					installTimer.setText("Остановить таймер");
					installTimer.setBackgroundResource(R.drawable.button_red_selector);
 					Context context = this.getApplicationContext();
 				    alarm.SetAlarm(context, intTimer[0], intTimer[1], intTimer[2], intTimer[3], intTimer[4], intTimer[5]);
 				    check =1;
 				    saveCheck();
 				} else {
 					Context context = this.getApplicationContext();
 				    alarm.CancelAlarm(context);
 				    installTimer.setText("Установить таймер");
				    installTimer.setBackgroundResource(R.drawable.button_blue_selector);
 				    check = 0;
 				    saveCheck();
 				}
 			    break;
 		}
 	}
 	
 	@SuppressWarnings("deprecation")
 	protected Dialog onCreateDialog(int id) {
 		if (id == START_TIME || id == END_TIME) {
 			TimePickerDialog tpd = new TimePickerDialog(this, myCallBack, setHour, setMinute, true);
 			return tpd;
 		} else if (id == PERIODIC_TIME) {
 			TimePickerDialog tpd = new TimePickerDialog(this, myCallBack, periodicHour, periodicMinute, true);
 			return tpd;
 		}
 		return super.onCreateDialog(id);
 	}
 	
 	 OnTimeSetListener myCallBack = new OnTimeSetListener() {
 		    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 		      timer[count].setText(hourOfDay+"");
 		      timer[count+1].setText(minute+"");
 		    }
 	 };
 	
 	 void saveCheck() {
 		    sPref = getPreferences(MODE_PRIVATE);
 		    Editor ed = sPref.edit();
 		    String[] stringTimer = new String[6]; 
 		    for (int i=0;i<=5;i++) {
 		    	stringTimer[i] = timer[i].getText().toString();
 		    }
 		    String savedText = String.valueOf(check);
 		    for (int i=0;i<=5;i++) {
 		    	savedText += ",";
 		    	savedText += stringTimer[i];
 		    }
 		    ed.putString(SAVED_TEXT, savedText);
 		    ed.commit();
 		  }
 		  
 		  void loadCheck() {
 		    sPref = getPreferences(MODE_PRIVATE);
 		    String savedText = sPref.getString(SAVED_TEXT, "");
 		    if (savedText.length() == 0) {
 		    	check = 0;
 		    } else {
 		    String[] avaliable = savedText.split(",");
 		    check = Integer.valueOf(avaliable[0]);
 		    if (check == 1) {
 		    	for (int i=1; i<=6; i++) {
 		    		timer[i-1].setText(avaliable[i]);
 		    	}
 		    	installTimer.setText("Остановить таймер");
		    	installTimer.setBackgroundResource(R.drawable.button_red_selector);
 		    }
 		    }
 		    
 		  }
 		    public void errorTime(View v, EditText set) {
 		    	Toast.makeText(this, "не правильное время", Toast.LENGTH_SHORT).show();
 		    	set.setText("");
 		    }
 }
