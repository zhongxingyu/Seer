 
 package com.carit.flashman;
 
 import java.util.ArrayList;
 
 import android.app.AlertDialog;
 import android.content.ComponentName;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.database.Cursor;
 import android.graphics.drawable.Drawable;
 import android.location.Location;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.provider.Contacts.People.Phones;
 import android.telephony.gsm.SmsManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.ProgressBar;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.amap.mapapi.core.GeoPoint;
 import com.amap.mapapi.map.MapActivity;
 import com.amap.mapapi.map.MapController;
 import com.amap.mapapi.map.MapView;
 import com.carit.flashman.FlashManService.ServiceCallBack;
 import com.carit.flashman.amap.BusLineSearch;
 import com.carit.flashman.amap.FavoritePointsActivity;
 import com.carit.flashman.amap.LongPressOverlay;
 import com.carit.flashman.amap.MyLocationOverlayProxy;
 import com.carit.flashman.amap.SMSLocationOverlay;
 import com.carit.flashman.provider.CityTable;
 import com.carit.flashman.util.Common;
 
 public class MainActivity extends MapActivity implements OnClickListener, ServiceCallBack {
 
     private MapView mMapView;
 
     private MapController mMapController;
 
     private GeoPoint mSMSPoint;
 
     private MyLocationOverlayProxy mLocationOverlay;
 
     // private LocationManagerProxy locationManager = null;
     // private MyLocationManager mMyLocationManager;
     private static final String TAG = "MainActivity";
 
     private ProgressBar mGetting_location;
 
     private FlashManService mMyService;
     
     public static final int INTENT_SMS = 0x01;
 
     private static final int LOCATION_CHANGE = 0x01;
 
     private static final int FIRST_LOCATION = 0x02;
 
     private static final int SEND_SMS = 0x03;
     
     private static final int BUSLINE_SEARCH = 0x04;
 
     private Spinner mProvincespinner;
 
     private Spinner mCityspinner;
 
     // private String mProvince;
 
     private String mCity;
     
     private String mToNumber;
 
     private int[] mArrayIds = {
             R.array.AnHuiSheng, R.array.FuJianSheng, R.array.GanSuSheng, R.array.GangAo,
             R.array.GuangDongSheng, R.array.GuangXiSheng, R.array.GuiZhouSheng,
             R.array.HaiNanSheng, R.array.HeBeiSheng, R.array.HeNanSheng, R.array.HeiLongJiang,
             R.array.HuBeiSheng, R.array.HuNanSheng, R.array.JiLinSheng, R.array.JiangSuSheng,
             R.array.JiangXiSheng, R.array.LiaoNingSheng, R.array.NeiMengGu, R.array.NingXia,
             R.array.QingHaiSheng, R.array.ShanDongSheng, R.array.ShanXiSheng, R.array.ShaanXiSheng,
             R.array.SiChuanSheng, R.array.XiZang, R.array.XinJiang, R.array.YunNanSheng,
             R.array.ZheJiangSheng
     };
 
     private Drawable mPassPinDrawable;
     
     private SMSLocationOverlay mSMSLocationOverlay;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main_layout);
         // init mapView
         mMapView = (MapView) findViewById(R.id.vmapView);
         mMapView.setVectorMap(true);
         mMapView.setBuiltInZoomControls(true);
         mMapController = mMapView.getController();
         mMapController.setZoom(12);
 
         //http://maps.google.com/maps?q=32.615152,110.79561&iwoc=A&hl=zh-CN
         // add MyLocationOverlay
         mLocationOverlay = new MyLocationOverlayProxy(this, mMapView);
         mLocationOverlay.enableCompass();
         mMapView.getOverlays().add(mLocationOverlay);
         // 实现初次定位使定位结果居中显示
         mLocationOverlay.runOnFirstFix(new Runnable() {
             public void run() {
                 
                 handler.sendMessage(Message.obtain(handler, FIRST_LOCATION));
                 mMapController.animateTo(mLocationOverlay.getMyLocation());
             }
         });
 
         // register OnClickListener
         findViewById(R.id.ImageButton_RouteAlert).setOnClickListener(this);
         findViewById(R.id.ImageButtonMyloc).setOnClickListener(this);
         findViewById(R.id.ImageButtonHotkey).setOnClickListener(this);
         findViewById(R.id.ImageButton_Fav).setOnClickListener(this);
 
         // show mGetting_location
         mGetting_location = (ProgressBar) findViewById(R.id.progress_loc);
         Toast.makeText(getBaseContext(), R.string.getting_location,
                  Toast.LENGTH_SHORT).show();
            
 
         // start FlashManService
         Intent i = new Intent();
         i.setClass(this, FlashManService.class);
         startService(i);
         bindService(i, mServiceConnection, BIND_AUTO_CREATE);
         getInitCity();
 
         // add LongPressOverlay
         mPassPinDrawable = getResources().getDrawable(R.drawable.pin_purple);
         LongPressOverlay mLongPressOverlay = new LongPressOverlay(this, mMapView, mMapController,
                 mPassPinDrawable);
         mMapView.getOverlays().add(mLongPressOverlay);
     }
 
     @Override
     protected void onPause() {
         // disableMyLocation();
         this.mLocationOverlay.disableMyLocation();
         super.onPause();
     }
 
     @Override
     protected void onResume() {
         this.mLocationOverlay.enableMyLocation();
 
         super.onResume();
         // mMyLocationManager.enableMyLocation();
     }
 
     @Override
     protected void onDestroy() {
         // mMyLocationManager.destoryLocationManager();
         mMyService.destoryLocationManager(mLocationOverlay);
         unbindService(mServiceConnection);
         super.onDestroy();
     }
 
     private Handler handler = new Handler() {
         public void handleMessage(Message msg) {
             switch (msg.what) {
                 case FIRST_LOCATION:
                     mGetting_location.setVisibility(View.GONE);
                     break;
                 case LOCATION_CHANGE:
                     if (mGetting_location.isShown())
                         mGetting_location.setVisibility(View.GONE);
 
                     break;
                 case SEND_SMS:
                     Bundle bundle = msg.getData();
                     String to = bundle.getString("name") != null ? bundle.getString("name")
                             : bundle.getString("number");
                     
                     AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                             .setMessage(String.format(getString(R.string.sure_send_position), to))
                             .setPositiveButton(android.R.string.ok,
                                     new DialogInterface.OnClickListener() {
                                         public void onClick(DialogInterface dialog, int whichButton) {
                                             sendSms(mToNumber,"#navi#|"+mSMSPoint.getLatitudeE6()/1E6+","+mSMSPoint.getLongitudeE6()/1E6+"|");
                                         }
                                     })
                             .setNegativeButton(android.R.string.cancel,
                                     new DialogInterface.OnClickListener() {
                                         public void onClick(DialogInterface dialog, int whichButton) {
                                             dialog.dismiss();
                                         }
                                     });
                     builder.show();
                     break;
             }
             // Toast.makeText(getBaseContext(), msg.obj.toString(),
             // Toast.LENGTH_SHORT).show();
             // mMapController.animateTo(new GeoPoint(msg.arg1,
             // msg.arg2));
             // Log.e(TAG, msg.obj.toString());
         }
     };
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
 
         return true;
     }
 
     @Override
     public boolean onMenuItemSelected(int featureId, MenuItem item) {
         // TODO Auto-generated method stub
         return super.onMenuItemSelected(featureId, item);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);
         switch (item.getItemId())// 得到被点击的item的itemId
         {
             case R.id.menu_settings:// 这里的Id就是布局文件中定义的Id，在用R.id.XXX的方法获取出来
                 break;
             case R.id.menu_offline_map:
                 break;
         }
         return true;
     }
 
     @Override
     public void onOptionsMenuClosed(Menu menu) {
         // TODO Auto-generated method stub
         super.onOptionsMenuClosed(menu);
     }
 
     @Override
     public void onClick(View v) {
         switch (v.getId()) {
             case R.id.ImageButton_RouteAlert:
                 Intent intent = new Intent(this, BusLineSearch.class);
                 startActivityForResult(intent,BUSLINE_SEARCH);
                 break;
             case R.id.ImageButtonMyloc:
                 mMapController.animateTo(mLocationOverlay.getMyLocation());
                 break;
             case R.id.ImageButtonHotkey:
 
                 break;
             case R.id.ImageButton_Fav:
                 Intent fav = new Intent(this, FavoritePointsActivity.class);
                 startActivity(fav);
                 break;
         }
 
     }
 
     private ServiceConnection mServiceConnection = new ServiceConnection() {
         // 当我bindService时，让TextView显示MyService里getSystemTime()方法的返回值
         public void onServiceConnected(ComponentName name, IBinder service) {
             // TODO Auto-generated method stub
             mMyService = ((FlashManService.MyBinder) service).getService();
             mMyService.setCallBack(MainActivity.this);
             mMyService.requestLocationUpdates(mLocationOverlay);
         }
 
         public void onServiceDisconnected(ComponentName name) {
             mMyService.setCallBack(null);
 
         }
     };
 
     @Override
     public void onLocationChange(Location location) {
         if (location != null) {
             Double geoLat = location.getLatitude();
             Double geoLng = location.getLongitude();
             String str = ("定位成功:(" + geoLng + "," + geoLat + ") provider =" + location
                     .getProvider());
             Message msg = new Message();
             msg.obj = str;
             msg.arg1 = (int) (location.getLatitude() * 1E6);
             msg.arg2 = (int) (location.getLongitude() * 1E6);
             if (handler != null) {
                 handler.sendMessage(msg);
             }
         }
 
     }
 
     /*
      * Thread t = new Thread(new Runnable() { public void run() { for (int
      * i=344;i< mOffline.getOfflineCityList().size();i++) { City cityobj =
      * mOffline.getOfflineCityList().get(i); try { List<Address> address =
      * coder.getFromLocationName(cityobj.getCity(), 3); if (address != null &&
      * address.size() > 0) { Address addres = address.get(0); String addressName
      * = cityobj.getCity()+":"+addres.getLatitude() + "," +
      * addres.getLongitude(); handler.obtainMessage(3,
      * addressName).sendToTarget(); saveLocation(cityobj, addres); } } catch
      * (AMapException e) { e.printStackTrace();
      * handler.sendMessage(Message.obtain(handler, Constants.ERROR)); } } } });
      * private void saveLocation(City city,Address addres) { ContentValues
      * values = new ContentValues(); values.put(CityTable.CITY, city.getCity());
      * values.put(CityTable.LAT, addres.getLatitude());
      * values.put(CityTable.LNG, addres.getLongitude());
      * values.put(CityTable.CODE, city.getCode()); values.put(CityTable.PINYIN,
      * city.getPinyin()); try{ Uri uri = getBaseContext().getContentResolver()
      * .insert(CityTable.CONTENT_URI, values); }catch (SQLException e) {
      * e.printStackTrace(); } }
      */
     private void getInitCity() {
         SharedPreferences info = getSharedPreferences("Info", 0);
         String cityCode = info.getString("CityCode", "");
         if (cityCode.equals("")) {
             setInitCity();
         } else {
             Cursor result = getBaseContext().getContentResolver().query(CityTable.CONTENT_URI,
                     new String[] {
                             "lat", "lng"
                     }, "code like \"%" + cityCode + "%\"", null, null);
             if (result.moveToFirst()) {
                 GeoPoint center = new GeoPoint(result.getDouble(result.getColumnIndex("lat")),
                         result.getDouble(result.getColumnIndex("lng")), true);
 
                 // result.getDouble(result.getColumnIndex("lat"));
                 mMapController.setCenter(center);
             }
         }
 
     }
 
     private void setInitCity() {
         View view = View.inflate(MainActivity.this, R.layout.city_choice, null);
         mProvincespinner = (Spinner) view.findViewById(R.id.province);
         // 将可选内容与ArrayAdapter连接起来
         ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(MainActivity.this,
                 R.array.provinces, android.R.layout.simple_spinner_item);
         // 设置下拉列表的风格
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 
         // 将adapter 添加到spinner中
         mProvincespinner.setAdapter(adapter);
 
         // 添加事件Spinner事件监听
         mProvincespinner.setOnItemSelectedListener(new SpinnerSelectedListener());
         ;
         mCityspinner = (Spinner) view.findViewById(R.id.city);
         adapter = ArrayAdapter.createFromResource(MainActivity.this, R.array.AnHuiSheng,
                 android.R.layout.simple_spinner_item);
         mCityspinner.setAdapter(adapter);
         // 设置下拉列表的风格
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 
         // 添加事件Spinner事件监听
         mCityspinner.setOnItemSelectedListener(new SpinnerSelectedListener());
         AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                 .setTitle(R.string.set_default_city).setView(view)
                 .setIcon(android.R.drawable.ic_menu_today)
                 .setPositiveButton(R.string.setup, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int whichButton) {
                         setResult(RESULT_OK);
                         Common.init(getBaseContext(), mCity);
                         getInitCity();
                     }
                 })
                 .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int whichButton) {
                         finish();
                     }
                 });
         builder.show();
     }
 
     // 使用数组形式操作
     class SpinnerSelectedListener implements OnItemSelectedListener {
 
         @SuppressWarnings("unchecked")
         public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
             if (mProvincespinner != null && mProvincespinner.equals(arg0)) {
                 mCityspinner.setAdapter(ArrayAdapter.createFromResource(MainActivity.this,
                         mArrayIds[arg2], android.R.layout.simple_spinner_item));
                 ((ArrayAdapter<CharSequence>) (mCityspinner.getAdapter()))
                         .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                 // if (arg1 != null) {
                 // mProvince = ((TextView) arg1).getText().toString();
                 // }
             } else if (mCityspinner != null && mCityspinner.equals(arg0)) {
                 // mCity = arg1.toString();
                 if (arg1 != null) {
                     mCity = ((TextView) arg1).getText().toString();
                     // Toast.makeText(getBaseContext(), mCity,
                     // Toast.LENGTH_LONG).show();
                 }
             }
         }
 
         public void onNothingSelected(AdapterView<?> arg0) {
         }
     }
 
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
 
         switch (requestCode) {
             case 0:
                 if (data == null) {
                     return;
                 }
                 Uri uri = data.getData();
                 Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                 cursor.moveToFirst();
 
                 String number = cursor.getString(cursor.getColumnIndexOrThrow(Phones.NUMBER));
                 String name = cursor.getString(cursor.getColumnIndexOrThrow(Phones.NAME));
                Log.d(TAG, "number = " + number + " name = " + name);
                 Message msg = new Message();
                 Bundle bundle = new Bundle();
                 bundle.putString("number", number);
                 bundle.putString("name", name);
                 msg.setData(bundle);
                 msg.what = SEND_SMS;
                 mToNumber = number;
                 handler.sendMessage(msg);
                 // mContactText.setText(number);
                 // mContactText.setSelection(number.length());
                 break;
             case BUSLINE_SEARCH:
                 mMapController.animateTo(new GeoPoint((int)(data.getDoubleExtra("lat", 0)*1E6), (int)(data.getDoubleExtra("lng", 0)*1E6)));
                 break;
             default:
                 break;
         }
     }
 
     public static void sendSms(String destPhone, String message) {
        Log.d(TAG, "Phone:" + destPhone + "Message:" + message);
         SmsManager smsManager = SmsManager.getDefault();
         if (message.length() > 70) {
             ArrayList<String> msgs = smsManager.divideMessage(message);
             for (String msg : msgs) {
                 smsManager.sendTextMessage(destPhone, null, msg, null, null);
             }
         } else {
             smsManager.sendTextMessage(destPhone, null, message, null, null);
         }
     }
     
     public void addSMSItem(GeoPoint point , String title){
         if(mSMSLocationOverlay==null){
             mSMSLocationOverlay = new SMSLocationOverlay(mMapView, mMapController,
                     mPassPinDrawable);
             mSMSLocationOverlay.addSMSItem(point, title);
             mMapView.getOverlays().add(mSMSLocationOverlay);
             
         }else{
             mSMSLocationOverlay.addSMSItem(point, title);
         }
     }
 
     public GeoPoint getSMSPoint() {
         return mSMSPoint;
     }
 
     public void setSMSPoint(GeoPoint mSMSPoint) {
         this.mSMSPoint = mSMSPoint;
     }
 
     @Override
     public void onConfigurationChanged(Configuration arg0) {
         Log.e(TAG, "onConfigurationChanged");
         super.onConfigurationChanged(arg0);
         if (arg0.orientation==Configuration.ORIENTATION_LANDSCAPE) {
             // Nothing need to be done here
              
          } else {
             // Nothing need to be done here
          }
     }
     
    
 
 }
