 package br.com.upinterativo.upstopwatch;
 
 import java.io.File;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.GestureDetector.SimpleOnGestureListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class Settings  extends Activity {
 	
 	private Button btPlusRealTime;
 	private Button btLessRealTime;
 	private Button btPlusFakeTime;
 	private Button btLessFakeTime;
 	
 	private CheckBox checkAlarm;
 	
 	private TextView vl_soundSelect;
 	
 	private GestureDetector gd;
 	
 	public static int realMinutes;
 	public static int fakeMinutes;
 	
 	public static boolean tocarAlarme;
 	
 	public static String urlSong;
 	public static String nameSong;
 	
 	
 	public static final String PREFS_NAME = "DataSettings";
 	public static final String REAL_MINUTES = "realMinutes";
 	public static final String FAKE_MINUTES = "fakeMinutes";
 	public static final String TOCAR_ALARME = "tocarAlarme";
 	public static final String URL_SONG = "urlSong";
 	public static final String NAME_SONG = "nameSong";
 	
 	@Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.settings);
         
         /*Dados armazenados*/
         
         SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        realMinutes = settings.getInt(REAL_MINUTES, 0);
        fakeMinutes = settings.getInt(FAKE_MINUTES, 0);
         urlSong = settings.getString(URL_SONG, "");
         nameSong = settings.getString(NAME_SONG, "");
         tocarAlarme = settings.getBoolean(TOCAR_ALARME, false);
         
         /*Evento para tratativa de gestos*/
         
         gd = new GestureDetector(this, simpleGestureDetector);
         
         /*Botoes de interacao com o tempo real*/
         
         btPlusRealTime = (Button)findViewById(R.id.btPlusRealTime);
         btLessRealTime = (Button)findViewById(R.id.btLessRealTime);
         
         /*Botoes de interacao com o tempo falso*/
         
         btPlusFakeTime = (Button)findViewById(R.id.btPlusFakeTime);
         btLessFakeTime = (Button)findViewById(R.id.btLessFakeTime);
         
         /*TextView do som selecionado*/
         
         vl_soundSelect = (TextView)findViewById(R.id.soundSelect);
         
         /*Checkbox para validar se tocara alarme*/
         
         checkAlarm = (CheckBox)findViewById(R.id.chAlarm);
         
         /*Status inicial dos valores de tempo*/        
         
         if (realMinutes > 0) {
         	EditText valueRealTime = (EditText)findViewById(R.id.vl_realTime);
         	if (realMinutes > 9)
         		valueRealTime.setText(realMinutes);
         	else {
         		String value = "0" + realMinutes;
         		valueRealTime.setText(value);
         	}
         }
         
         if (fakeMinutes > 0) {
         	EditText valueFakeTime = (EditText)findViewById(R.id.vl_fakeTime);
         	if (fakeMinutes > 9)
     			valueFakeTime.setText(fakeMinutes);
         	else {
         		String value = "0" + fakeMinutes;
         		valueFakeTime.setText(value);
         	}
         }
         
         /*Status inicial do checkbox para emitir alarme*/
         
         checkAlarm.setChecked(tocarAlarme);
         
         /*Status inicial do texview de som selecionado*/
         
         if (urlSong.isEmpty())
         	vl_soundSelect.setVisibility(View.GONE);
         else
         	vl_soundSelect.setText(nameSong);
         
         /*Definindo cor transparente aos botoes*/
         
         btPlusRealTime.setBackgroundColor(0);
         btLessRealTime.setBackgroundColor(0);
         btPlusFakeTime.setBackgroundColor(0);
         btLessFakeTime.setBackgroundColor(0);
         
         /*Evento de clique para incrementar minutos do tempo real*/
         
         btPlusRealTime.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				EditText valueRealTime = (EditText)findViewById(R.id.vl_realTime);
 				int value = Integer.parseInt(valueRealTime.getText().toString());
 				value++;
 				String newValue = "";
 				
 				if (value <= 99) {
 					if (value < 10) {
 						newValue = "0" + value;
 					}
 					else {
 						newValue = "" + value;
 					}
 					
 					valueRealTime.setText(newValue);
 					
 					SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 					SharedPreferences.Editor editor = settings.edit();
 					editor.putInt(REAL_MINUTES, value);
 					
 					editor.commit();
 				}
 			}
 		});
         
         /*Evento de clique para decrementar minutos do tempo real*/
         
         btLessRealTime.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				EditText valueRealTime = (EditText)findViewById(R.id.vl_realTime);
 				int value = Integer.parseInt(valueRealTime.getText().toString());
 				value--;
 				String newValue = "";
 				
 				if (value >= 0) {
 					if (value < 10) {
 						newValue = "0" + value;
 					}
 					else {
 						newValue = "" + value;
 					}
 					
 					valueRealTime.setText(newValue);
 					
 					SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 					SharedPreferences.Editor editor = settings.edit();
 					editor.putInt(REAL_MINUTES, value);
 					
 					editor.commit();
 				}
 			}
 		});
         
         /*Evento de clique para incrementar minutos do tempo falso*/
         
         btPlusFakeTime.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View arg0) {
 				EditText valueFakeTime = (EditText)findViewById(R.id.vl_fakeTime);
 				int value = Integer.parseInt(valueFakeTime.getText().toString());
 				value++;
 				String newValue = "";
 				
 				if (value <= 99) {
 					if (value < 10) {
 						newValue = "0" + value;
 					}
 					else {
 						newValue = "" + value;
 					}
 					
 					valueFakeTime.setText(newValue);
 					
 					SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 					SharedPreferences.Editor editor = settings.edit();
 					editor.putInt(FAKE_MINUTES, value);
 					
 					editor.commit();
 				}
 			}
 		});
         
         /*Evento de clique para decrementar minutos do tempo real*/
         
         btLessFakeTime.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				EditText valueFakeTime = (EditText)findViewById(R.id.vl_fakeTime);
 				int value = Integer.parseInt(valueFakeTime.getText().toString());
 				value--;
 				String newValue = "";
 				
 				if (value >= 0) {
 					if (value < 10) {
 						newValue = "0" + value;
 					}
 					else {
 						newValue = "" + value;
 					}
 					
 					valueFakeTime.setText(newValue);
 					
 					SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 					SharedPreferences.Editor editor = settings.edit();
 					editor.putInt(FAKE_MINUTES, value);
 					
 					editor.commit();
 				}
 			}
 		});
         
         /*Evento de checkbox para validacao de alarme*/
         
         checkAlarm.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				boolean checked = ((CheckBox) v).isChecked();
 				
 				if (checked) {
 					getSound();
 				}
 				else {
 					vl_soundSelect.setVisibility(View.GONE);
 					
 					SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 					SharedPreferences.Editor editor = settings.edit();
 					editor.putString(NAME_SONG, "");
 					editor.putString(URL_SONG, "");
 					editor.putBoolean(TOCAR_ALARME, false);
 					
 					editor.commit();
 				}
 			}
 		});
     }
 	
 	private void getSound() {
 		Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
 		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
 		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
 		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
 		this.startActivityForResult(intent, 5);
 	}
 	
 	@Override
 	 protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent)
 	 {
 	     if (resultCode == Activity.RESULT_OK && requestCode == 5)
 	     {
 	          Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
 
 	          if (uri != null)
 	          {
 	        	  File file = new File(uri.toString());
 	        	  String title = "Tocar:\n\t" + file.getName();
 	              this.vl_soundSelect.setText(title);
 	              this.vl_soundSelect.setVisibility(View.VISIBLE);
 	             
 	              SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 	              SharedPreferences.Editor editor = settings.edit();
 	              editor.putString(NAME_SONG, file.getName());
 	              editor.putString(URL_SONG, uri.toString());
 	              editor.putBoolean(TOCAR_ALARME, true);
 					
 	              editor.commit();
 	          }
 	          else
 	          {
 	        	  this.vl_soundSelect.setText("");
 	        	  this.checkAlarm.setChecked(false);
 	        	  
 	        	  SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 	        	  SharedPreferences.Editor editor = settings.edit();
 	        	  editor.putString(NAME_SONG, "");
 	        	  editor.putString(URL_SONG, "");
 	        	  editor.putBoolean(TOCAR_ALARME, false);
 				
 	        	  editor.commit();
 	          }
 	      }            
 	  }
 	
 	@Override
     public boolean onTouchEvent(MotionEvent event) {
     	return gd.onTouchEvent(event);
     }
 
     SimpleOnGestureListener simpleGestureDetector = new SimpleOnGestureListener() {
     	
     	/*Swype para troca de tela (Abre a tela principal)*/
 		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
 			float velocityY) {
 			int dist = (int) (e1.getX() - e2.getX());
 			if(velocityX < - 5000 && dist > 200) {
 				SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 				realMinutes = settings.getInt(REAL_MINUTES, 0);
 				fakeMinutes = settings.getInt(FAKE_MINUTES, 0);
 				
 				finish();
 			}
 			
 			return false;
 		}
     };
 }
