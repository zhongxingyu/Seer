 package ru.shutoff.caralarm;
 
 import android.app.Activity;
 import android.appwidget.AppWidgetManager;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.BaseAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class ConfigWidget extends Activity {
 
     SharedPreferences preferences;
     String car_id;
     int widgetID;
     Intent resultValue;
 
     static final int CAR_CONFIG = 1000;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         Intent intent = getIntent();
         Bundle extras = intent.getExtras();
         if (extras != null) {
             widgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                     AppWidgetManager.INVALID_APPWIDGET_ID);
         }
         if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID) {
             finish();
             return;
         }
 
         // формируем intent ответа
         resultValue = new Intent();
         resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
 
         // отрицательный ответ
         setResult(RESULT_CANCELED, resultValue);
 
         final Cars.Car[] cars = Cars.getCars(this);
         preferences = PreferenceManager.getDefaultSharedPreferences(this);
         if (cars.length == 1) {
             car_id = Preferences.getCar(preferences, "");
             String api_key = preferences.getString(Names.CAR_KEY + car_id, "");
             String phone = preferences.getString(Names.CAR_PHONE + car_id, "");
             if ((api_key.length() > 0) && (phone.length() > 0)) {
                 saveWidget();
                 finish();
                 return;
             }
             Intent carIntent = new Intent(this, CarPreferences.class);
             carIntent.putExtra(Names.ID, car_id);
             startActivityForResult(carIntent, CAR_CONFIG);
             return;
         }
 
         setContentView(R.layout.config_widget);
         ListView lv = (ListView) findViewById(R.id.list);
         lv.setAdapter(new BaseAdapter() {
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
                 TextView tvName = (TextView) v.findViewById(R.id.name);
                 tvName.setText(cars[position].name);
                 return v;
             }
         });
         lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 car_id = cars[position].id;
                 saveWidget();
                 finish();
             }
         });
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (resultCode != RESULT_OK)
             return;
         switch (requestCode) {
             case CAR_CONFIG:
                 if (preferences.getString(Names.CAR_KEY + car_id, "").length() > 0)
                     saveWidget();
                 finish();
                 break;
         }
     }
 
     void saveWidget() {
         SharedPreferences.Editor ed = preferences.edit();
         ed.putString(Names.WIDGET + widgetID, car_id);
         ed.commit();
         setResult(RESULT_OK, resultValue);
     }
 }
