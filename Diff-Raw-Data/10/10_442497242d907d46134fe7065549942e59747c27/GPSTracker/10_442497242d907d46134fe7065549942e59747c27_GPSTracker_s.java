 package ru.entropysw.sporttracker;
 
 import android.*;
 import android.R;
 import android.app.AlertDialog;
 import android.app.Service;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.location.LocationProvider;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.provider.Settings;
 import android.util.Log;
 import android.widget.Toast;
 
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Mikhail_Levanov
  * Date: 29.05.13
  * Time: 11:01
  *
  * Класс для отслеживания местоположения через GPS
  */
 public class GPSTracker extends Service implements LocationListener {
     private static Context mContext;
     private static GPSTracker instance;
 
     private Location location;
     private double latitude; // широта
     private double longitude; // долгота
 
     // Минимальное расстояние, через которое происходит обновление локации в метрах
     private static final long MIN_DISTANCE_CHANGE_FOR_UPDATE = 1;
     // Минимальное время обновления локации
     private static final long MIN_TIME_FOR_UPDATE = 10000;
 
     protected LocationManager locationManager;
 
     private static final Map<Integer, String> providerStatuses;
     static {
         Map<Integer, String> tMap = new HashMap<Integer, String>();
         tMap.put((Integer)LocationProvider.AVAILABLE, "Доступен");
         tMap.put((Integer)LocationProvider.OUT_OF_SERVICE, "Недоступен");
         tMap.put((Integer)LocationProvider.TEMPORARILY_UNAVAILABLE, "Временно недоступен");
         providerStatuses = Collections.unmodifiableMap(tMap);
     }
 
     private boolean doMonitoring = false;
 
     /**
      * Singleton
      */
     private GPSTracker() {
     }
 
     /**
      * Singleton
      *
      * @return
      */
     public static GPSTracker get() {
         if(instance == null) {
             instance = new GPSTracker();
         }
 
         return instance;
     }
 
     /**
      * Устанавливает контекст
      *
      * @param context
      */
     public void setContext(Context context) {
         mContext = context;
     }
 
     /**
      * Возвращает текущую локацию
      *
      * @return
      */
     public Location getLocation() {
         try {
             if(canGetLocation()) {
                 // пробуем получить локацию через сеть
                 if(isNetworkEnabled()) {
                     Location location = tryToGetLocationVia(LocationManager.NETWORK_PROVIDER);
                 }
 
                 // если ничего не вышло и можно использовать GPS, делаем это
                 if(location == null && isGPSEnabled()) {
                     Location location = tryToGetLocationVia(LocationManager.GPS_PROVIDER);
                 }
             } else {
                 Log.d("ProvidersDisabled", "All providers disabled");
                 Toast.makeText(mContext, "Все провайдеры отключены ", Toast.LENGTH_LONG).show();
                 // TODO: Обработчик ошибки нужен
             }
         } catch(Exception e) {
             e.printStackTrace();
         }
         return location;
     }
 
     /**
      * Пытается определить локацию посредством провайдера
      *
      * @param provider
      * @return
      */
     private Location tryToGetLocationVia(String provider) {
         getLocationManager().requestLocationUpdates(
                 provider,
                 MIN_TIME_FOR_UPDATE,
                 MIN_DISTANCE_CHANGE_FOR_UPDATE,
                 this
         );
 
         Log.d(provider, "Trying to locate via "+provider);
         if(getLocationManager() != null) {
             location = getLocationManager().getLastKnownLocation(provider);
             if(location != null) {
                 latitude = location.getLatitude();
                 longitude = location.getLongitude();
             } else {
                 Log.d(provider+"Failed", "Failed to get locaiton via provider "+provider);
                 Toast.makeText(mContext, "Невозможно определить локацию с помощью "+provider, Toast.LENGTH_LONG).show();
                 // TODO: Нужен эксепшн, видимо
             }
         }
 
         return location;
     }
 
     /**
      * Возвращает широту
      *
      * @return
      */
     public double getLatitude() {
         if(location != null) {
             latitude = location.getLatitude();
         }
 
         return latitude;
     }
 
     /**
      * Возвращает долготу
      *
      * @return
      */
     public double getLongitude() {
         if(location != null) {
             longitude = location.getLongitude();
         }
 
         return longitude;
     }
 
     /**
      * Возвращает locationManager
      *
      * @return
      */
     private LocationManager getLocationManager() {
         if(locationManager == null) {
             locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
         }
 
         return locationManager;
     }
 
     /**
      * Доступно ли обнаружение по сети
      *
      * @return
      */
     public boolean isNetworkEnabled() {
         return getLocationManager().isProviderEnabled(LocationManager.NETWORK_PROVIDER);
     }
 
     /**
      * Доступно ли обнаружение по GPS
      *
      * @return
      */
     public boolean isGPSEnabled() {
         return getLocationManager().isProviderEnabled(LocationManager.GPS_PROVIDER);
     }
 
     /**
      * Возвращает возможность получения локации
      *
      * @return
      */
     public boolean canGetLocation() {
         return isNetworkEnabled() || isGPSEnabled();
     }
 
     /**
      * Показывает оповещение о невлюченном GPS
      *
      */
     public void showSettingsAlert() {
         AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
 
         alertDialog.setTitle("GPS недоступен");
         alertDialog.setMessage("GPS недоступен. Вы можете пройти в меню настроек и включить GPS.");
 
         alertDialog.setPositiveButton("Настройки", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialogInterface, int i) {
                 Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                 mContext.startActivity(intent);
             }
         });
 
         alertDialog.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialogInterface, int i) {
                 dialogInterface.cancel();
             }
         });
 
         alertDialog.show();
     }
 
     /**
      * Отображает сообщение-тост с текущей локацией
      */
     public void showLocationNotificator() {
        Toast.makeText(mContext, "Ваше местоположение - \nШирота: " + latitude + "\nДолгота: " + longitude, Toast.LENGTH_LONG).show();
     }
 
     public void setDoMonitoring(boolean b) {
         doMonitoring = b;
     }
 
     @Override
     public void onLocationChanged(Location location) {
         if(doMonitoring) {
             this.location = location;
         }
     }
 
     @Override
     public void onStatusChanged(String s, int i, Bundle bundle) {
         Toast.makeText(mContext, "Провайдер "+s+" изменил статуст на "+ providerStatuses.get((Integer)i) +"!", Toast.LENGTH_LONG).show();
     }
 
     @Override
     public void onProviderEnabled(String s) {
         Toast.makeText(mContext, "Провайдер "+s+" включен!", Toast.LENGTH_LONG).show();
     }
 
     @Override
     public void onProviderDisabled(String s) {
         Toast.makeText(mContext, "Провайдер "+s+" выключен!", Toast.LENGTH_LONG).show();
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         return null;
     }
 }
