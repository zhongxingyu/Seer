 package ru.shutoff.caralarm;
 
 import android.app.AlarmManager;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v7.app.ActionBar;
 import android.support.v7.app.ActionBarActivity;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewConfiguration;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 import org.joda.time.DateTimeZone;
 
 import java.lang.reflect.Field;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public class MainActivity extends ActionBarActivity {
 
     ImageView imgCar;
     TextView tvAddress;
     TextView tvLast;
     TextView tvVoltage;
     TextView tvReserve;
     TextView tvBalance;
     TextView tvTemperature;
     TextView tvError;
     View vTemperature;
     View vError;
     ImageView imgRefresh;
     ProgressBar prgUpdate;
 
     ImageView ivMotor;
     ImageView ivRele;
     ImageView ivBlock;
     ImageView ivValet;
 
     static final int REQUEST_ALARM = 4000;
     static final int CAR_SETUP = 4001;
     static final int UPDATE_INTERVAL = 30 * 1000;
 
     PendingIntent pi;
 
     BroadcastReceiver br;
     AlarmManager alarmMgr;
 
     SharedPreferences preferences;
     CarDrawable drawable;
 
     String car_id;
     Cars.Car[] cars;
 
     boolean active;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
 
         super.onCreate(savedInstanceState);
         try {
             ViewConfiguration config = ViewConfiguration.get(this);
             Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
             if (menuKeyField != null) {
                 menuKeyField.setAccessible(true);
                 menuKeyField.setBoolean(config, false);
             }
         } catch (Exception ex) {
             // Ignore
         }
 
         setContentView(R.layout.main);
         preferences = PreferenceManager.getDefaultSharedPreferences(this);
 
         if (savedInstanceState != null) {
             car_id = savedInstanceState.getString(Names.ID);
         } else {
             car_id = getIntent().getStringExtra(Names.ID);
             if (car_id == null)
                 car_id = preferences.getString(Names.LAST, "");
             car_id = Preferences.getCar(preferences, car_id);
         }
 
         imgCar = (ImageView) findViewById(R.id.car);
         tvAddress = (TextView) findViewById(R.id.address);
         tvLast = (TextView) findViewById(R.id.last);
         tvVoltage = (TextView) findViewById(R.id.voltage);
         tvReserve = (TextView) findViewById(R.id.reserve);
         tvBalance = (TextView) findViewById(R.id.balance);
         tvTemperature = (TextView) findViewById(R.id.temperature);
         vTemperature = findViewById(R.id.temperature_block);
 
         ivMotor = (ImageView) findViewById(R.id.motor);
         ivRele = (ImageView) findViewById(R.id.rele);
         ivBlock = (ImageView) findViewById(R.id.block);
         ivValet = (ImageView) findViewById(R.id.valet);
 
         final Context context = this;
 
         ivMotor.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 if (preferences.getBoolean(Names.INPUT3 + car_id, false)) {
                     Actions.motorOff(context, car_id);
                 } else {
                     Actions.motorOn(context, car_id);
                 }
             }
         });
         ivRele.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Actions.rele1(context, car_id);
             }
         });
         ivBlock.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Actions.blockMotor(context, car_id);
             }
         });
         ivValet.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 if (Preferences.getValet(preferences, car_id)) {
                     Actions.valetOff(context, car_id);
                 } else {
                     Actions.valetOn(context, car_id);
                 }
             }
         });
 
         tvError = (TextView) findViewById(R.id.error_text);
         vError = findViewById(R.id.error);
         vError.setVisibility(View.GONE);
 
         imgRefresh = (ImageView) findViewById(R.id.refresh);
         imgRefresh.setVisibility(View.GONE);
         prgUpdate = (ProgressBar) findViewById(R.id.update);
 
         View time = findViewById(R.id.time);
         time.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 startUpdate();
             }
         });
 
         drawable = new CarDrawable(this);
         imgCar.setImageDrawable(drawable.getDrawable());
 
         removeNotifications();
         br = new BroadcastReceiver() {
             @Override
             public void onReceive(Context context, Intent intent) {
                 if (intent == null)
                     return;
                 if (!car_id.equals(intent.getStringExtra(Names.ID)))
                     return;
                 if (intent.getAction().equals(FetchService.ACTION_UPDATE)) {
                     vError.setVisibility(View.GONE);
                     imgRefresh.setVisibility(View.VISIBLE);
                     prgUpdate.setVisibility(View.GONE);
                     update();
                     stopTimer();
                     startTimer(false);
                 }
                 if (intent.getAction().equals(FetchService.ACTION_NOUPDATE)) {
                     vError.setVisibility(View.GONE);
                     imgRefresh.setVisibility(View.VISIBLE);
                     prgUpdate.setVisibility(View.GONE);
                 }
                 if (intent.getAction().equals(FetchService.ACTION_ERROR)) {
                     String error_text = intent.getStringExtra(Names.ERROR);
                     if (error_text == null)
                         error_text = getString(R.string.data_error);
                     tvError.setText(error_text);
                     vError.setVisibility(View.VISIBLE);
                     imgRefresh.setVisibility(View.VISIBLE);
                     prgUpdate.setVisibility(View.GONE);
                 }
                 if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                     DateTimeZone tz = DateTimeZone.getDefault();
                     DateTimeZone.setDefault(tz);
                     update();
                 }
             }
         };
         IntentFilter intFilter = new IntentFilter(FetchService.ACTION_UPDATE);
         intFilter.addAction(FetchService.ACTION_NOUPDATE);
         intFilter.addAction(FetchService.ACTION_ERROR);
         intFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
         registerReceiver(br, intFilter);
 
         alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
         pi = createPendingResult(REQUEST_ALARM, new Intent(), 0);
 
         tvAddress.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(getBaseContext(), MapView.class);
                 intent.putExtra(Names.ID, car_id);
                 startActivity(intent);
             }
         });
 
         active = false;
 
         String phone = preferences.getString(Names.CAR_PHONE + car_id, "");
         String key = preferences.getString(Names.CAR_KEY + car_id, "");
         if ((phone.length() == 0) || (key.length() == 0)) {
             Intent intent = new Intent(this, CarPreferences.class);
             intent.putExtra(Names.ID, car_id);
             startActivityForResult(intent, CAR_SETUP);
         }
     }
 
     String getId() {
         return car_id;
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         unregisterReceiver(br);
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         outState.putString(Names.ID, car_id);
     }
 
     @Override
     protected void onStart() {
         super.onStart();
         active = true;
         startTimer(true);
         setActionBar();
         update();
     }
 
     void setActionBar() {
         ActionBar actionBar = getSupportActionBar();
         cars = Cars.getCars(this);
         if (cars.length > 1) {
             actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
             actionBar.setListNavigationCallbacks(new CarsAdapter(), new ActionBar.OnNavigationListener() {
                 @Override
                 public boolean onNavigationItemSelected(int i, long l) {
                     if (cars[i].id.equals(car_id))
                         return true;
                     car_id = cars[i].id;
                     SharedPreferences.Editor ed = preferences.edit();
                     ed.putString(Names.LAST, car_id);
                     ed.commit();
                     update();
                     startUpdate();
                     return true;
                 }
             });
             actionBar.setDisplayShowHomeEnabled(false);
             actionBar.setDisplayUseLogoEnabled(false);
             for (int i = 0; i < cars.length; i++) {
                 if (cars[i].id.equals(car_id)) {
                     actionBar.setSelectedNavigationItem(i);
                     break;
                 }
             }
             setTitle("");
         } else {
             actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
             actionBar.setDisplayShowHomeEnabled(true);
             actionBar.setDisplayUseLogoEnabled(false);
             setTitle(getString(R.string.app_name));
         }
     }
 
     @Override
     protected void onStop() {
         super.onStop();
         active = false;
         stopTimer();
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (requestCode == REQUEST_ALARM) {
             startUpdate();
             return;
         }
         if (requestCode == CAR_SETUP) {
             String key = preferences.getString(Names.CAR_KEY + car_id, "");
             if (key.length() == 0)
                 finish();
         }
     }
 
     void startTimer(boolean now) {
         if (!active)
             return;
         alarmMgr.setInexactRepeating(AlarmManager.RTC,
                 System.currentTimeMillis() + (now ? 0 : UPDATE_INTERVAL), UPDATE_INTERVAL, pi);
     }
 
     void stopTimer() {
         alarmMgr.cancel(pi);
     }
 
     @Override
     protected void onNewIntent(Intent intent) {
         removeNotifications();
         String id = intent.getStringExtra(Names.ID);
         if (id != null) {
             id = Preferences.getCar(preferences, id);
             if (!id.equals(car_id)) {
                 car_id = id;
                 setActionBar();
             }
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main, menu);
         return super.onCreateOptionsMenu(menu);
     }
 
     private void removeNotifications() {
         NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
         int id = preferences.getInt(Names.IDS, 0);
         for (int i = 1; i <= id; i++) {
             try {
                 manager.cancel(i);
             } catch (NumberFormatException e) {
                 // ignore
             }
         }
         SharedPreferences.Editor ed = preferences.edit();
         ed.putInt(Names.IDS, 0);
         ed.commit();
     }
 
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.preferences: {
                 Intent intent = new Intent(this, Preferences.class);
                 startActivity(intent);
                 break;
             }
             case R.id.actions: {
                 Intent intent = new Intent(this, Actions.class);
                 intent.putExtra(Names.ID, car_id);
                 startActivity(intent);
                 break;
             }
             case R.id.map: {
                 Intent intent = new Intent(this, MapView.class);
                 intent.putExtra(Names.ID, car_id);
                 startActivity(intent);
                 break;
             }
             case R.id.tracks: {
                 Intent intent = new Intent(this, TracksActivity.class);
                 intent.putExtra(Names.ID, car_id);
                 startActivity(intent);
                 break;
             }
             case R.id.events: {
                 Intent intent = new Intent(this, EventsActivity.class);
                 intent.putExtra(Names.ID, car_id);
                 startActivity(intent);
                 break;
             }
         }
         return false;
     }
 
     void update() {
         long last = preferences.getLong(Names.EVENT_TIME + car_id, 0);
         if (last != 0) {
             Date d = new Date(last);
             SimpleDateFormat sf = new SimpleDateFormat();
             tvLast.setText(sf.format(d));
         } else {
             tvLast.setText(getString(R.string.unknown));
         }
         tvVoltage.setText(preferences.getString(Names.VOLTAGE_MAIN + car_id, "?") + " V");
         tvReserve.setText(preferences.getString(Names.VOLTAGE_RESERVED + car_id, "?") + " V");
         tvBalance.setText(preferences.getString(Names.BALANCE + car_id, "?"));
         String temperature = Preferences.getTemperature(preferences, car_id);
         if (temperature == null) {
             vTemperature.setVisibility(View.GONE);
         } else {
             tvTemperature.setText(temperature);
             vTemperature.setVisibility(View.VISIBLE);
         }
 
         drawable.update(preferences, car_id);
         tvAddress.setText(
                 preferences.getString(Names.LONGITUDE + car_id, "") + " " +
                         preferences.getString(Names.LONGITUDE + car_id, "") + "\n" +
                         Address.getAddress(this, car_id));
 
         if (preferences.getBoolean(Names.CAR_AUTOSTART + car_id, false)) {
             ivMotor.setVisibility(View.VISIBLE);
             if (preferences.getBoolean(Names.INPUT3 + car_id, false)) {
                 ivMotor.setImageResource(R.drawable.motor_off);
             } else {
                 ivMotor.setImageResource(R.drawable.motor_on);
             }
         } else {
             ivMotor.setVisibility(View.GONE);
         }
         if (preferences.getBoolean(Names.CAR_RELE1 + car_id, false)) {
             ivRele.setVisibility(View.VISIBLE);
         } else {
             ivRele.setVisibility(View.GONE);
         }
         if (Preferences.getValet(preferences, car_id)) {
             ivValet.setImageResource(R.drawable.valet_btn_off);
         } else {
             ivValet.setImageResource(R.drawable.valet_btn_on);
         }
         if (preferences.getBoolean(Names.INPUT3 + car_id, false)) {
             ivBlock.setVisibility(View.VISIBLE);
         } else {
             ivBlock.setVisibility(View.GONE);
         }
     }
 
     void startUpdate() {
         Intent intent = new Intent(this, FetchService.class);
         intent.putExtra(Names.ID, car_id);
         startService(intent);
         vError.setVisibility(View.GONE);
         imgRefresh.setVisibility(View.GONE);
         prgUpdate.setVisibility(View.VISIBLE);
     }
 
     class CarsAdapter extends BaseAdapter {
 
         @Override
         public int getCount() {
             return cars.length;
         }
 
         @Override
         public Object getItem(int position) {
             return cars[position];
         }
 
         @Override
         public long getItemId(int position) {
             return position;
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             View v = convertView;
             if (v == null) {
                 LayoutInflater inflater = (LayoutInflater) getBaseContext()
                         .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.car_list_item, null);
             }
             TextView tv = (TextView) v.findViewById(R.id.name);
             tv.setText(cars[position].name);
             return v;
         }
 
         @Override
         public View getDropDownView(int position, View convertView, ViewGroup parent) {
             View v = convertView;
             if (v == null) {
                 LayoutInflater inflater = (LayoutInflater) getBaseContext()
                         .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                 v = inflater.inflate(R.layout.car_list_dropdown_item, null);
             }
             TextView tv = (TextView) v.findViewById(R.id.name);
             tv.setText(cars[position].name);
             return v;
         }
     }
 
 }
