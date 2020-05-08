 
 package com.fanoyong.phoneinfo.ui;
 
 import java.util.List;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.app.ActivityManager;
 import android.app.ActivityManager.MemoryInfo;
 import android.content.Context;
 import android.graphics.Color;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Build;
 import android.os.Bundle;
 import android.telephony.TelephonyManager;
 import android.util.DisplayMetrics;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.TextView;
 
 import com.fanoyong.phoneinfo.R;
 
 public class InfoActivity extends Activity implements SensorEventListener {
     private LinearLayout page;
     SensorManager mSensorManager;
     Sensor mTemperature;
     float temperature;
     Sensor mPressure;
     float pressure;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
         mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
         if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
             mTemperature = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
         } else {
             mTemperature = mSensorManager.getDefaultSensor(Sensor.TYPE_TEMPERATURE);
         }
         mSensorManager.registerListener(this, mPressure, SensorManager.SENSOR_DELAY_FASTEST);
         mSensorManager.registerListener(this, mTemperature, SensorManager.SENSOR_DELAY_FASTEST);
         setContentView(R.layout.activity_info);
         LinearLayout infoContainer = (LinearLayout) this.findViewById(R.id.InfoContainer);
         this.page = new LinearLayout(this);
         getInfo();
         infoContainer.removeAllViews();
         infoContainer.addView(this.page);
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         mSensorManager.unregisterListener(this);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.activity_info_actionbar, menu);
         return true;
     }
 
     public void refreshContents(MenuItem item) {
         switch (item.getItemId()) {
             case R.menu.activity_info_actionbar:
                 LinearLayout infoContainer = (LinearLayout) this.findViewById(R.id.InfoContainer);
                 infoContainer.invalidate();
                 getInfo();
                 infoContainer.removeAllViews();
                 infoContainer.addView(this.page);
                 break;
             default:
                 break;
         }
 
     }
 
     private void getInfo() {
         this.page.removeAllViews();
         this.page.setOrientation(LinearLayout.VERTICAL);
 
         infoCPU();
         infoKernel();
         infoMemory();
         infoDisplay();
         infoSensors();
         infoConnectivity();
         infoTelephony();
     }
 
     @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
     private void infoMemory() {
         MemoryInfo mi = new MemoryInfo();
         ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
         activityManager.getMemoryInfo(mi);
 
         View child = null;
         this.page.addView(createHeader(getString(R.string.memory)));
         int MB = 1024 * 1024;
         if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
             long totalMem = mi.totalMem;
 
             if (totalMem > 0) {
                 child = createLine(getString(R.string.total_memory), String.valueOf(totalMem / MB)
                         + " MB");
                 if (child != null) {
                     this.page.addView(child);
                 }
             }
         }
         long availMem = mi.availMem;
         if (availMem > 0) {
             child = createLine(getString(R.string.available_memory), String.valueOf(availMem / MB)
                     + " MB");
             if (child != null) {
                 this.page.addView(child);
             }
         }
         long threshold = mi.threshold;
         if (threshold > 0) {
             child = createLine(getString(R.string.low_memory_threshold),
                     String.valueOf(threshold / MB) + " MB");
             if (child != null) {
                 this.page.addView(child);
             }
         }
     }
 
     private void infoTelephony() {
         View child = null;
         this.page.addView(createHeader(getString(R.string.telephoney)));
         TelephonyManager tm = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
 
         String deviceswver = tm.getDeviceSoftwareVersion();
         if (deviceswver != null) {
             child = createLine(getString(R.string.device_sw_version), deviceswver);
             if (child != null) {
                 this.page.addView(child);
             }
         }
         String deviceid = tm.getDeviceId();
         if (deviceid != null) {
             child = createLine(getString(R.string.device_id), deviceid);
             if (child != null) {
                 this.page.addView(child);
             }
         }
         int dataactivity = tm.getDataActivity();
         String temp = null;
         switch (dataactivity) {
             case TelephonyManager.DATA_ACTIVITY_DORMANT:
                 temp = getString(R.string.dormant);
                 break;
             case TelephonyManager.DATA_ACTIVITY_IN:
                 temp = getString(R.string.in);
                 break;
             case TelephonyManager.DATA_ACTIVITY_INOUT:
                 temp = getString(R.string.in_out);
                 break;
             case TelephonyManager.DATA_ACTIVITY_NONE:
                 temp = getString(R.string.none);
                 break;
             case TelephonyManager.DATA_ACTIVITY_OUT:
                 temp = getString(R.string.out);
                 break;
             default:
         }
         if (temp != null) {
             child = createLine(getString(R.string.data_activity), temp);
             if (child != null) {
                 this.page.addView(child);
             }
         }
         int datastate = tm.getDataState();
         temp = null;
         switch (datastate) {
             case TelephonyManager.DATA_CONNECTED:
                 temp = getString(R.string.connected);
                 break;
             case TelephonyManager.DATA_CONNECTING:
                 temp = getString(R.string.connecting);
                 break;
             case TelephonyManager.DATA_DISCONNECTED:
                 temp = getString(R.string.disconnected);
                 break;
             case TelephonyManager.DATA_SUSPENDED:
                 temp = getString(R.string.suspended);
                 break;
             default:
         }
         if (temp != null) {
             child = createLine(getString(R.string.data_state), temp);
             if (child != null) {
                 this.page.addView(child);
             }
         }
         int phonetype = tm.getPhoneType();
         temp = null;
         switch (phonetype) {
             case TelephonyManager.PHONE_TYPE_CDMA:
                 temp = getString(R.string.cdma);
                 break;
             case TelephonyManager.PHONE_TYPE_GSM:
                 temp = getString(R.string.gsm);
                 break;
             case TelephonyManager.PHONE_TYPE_SIP:
                 temp = getString(R.string.sip);
                 break;
             case TelephonyManager.PHONE_TYPE_NONE:
                 temp = getString(R.string.none);
                 break;
             default:
         }
         if (temp != null) {
             child = createLine(getString(R.string.phone_type), temp);
             if (child != null) {
                 this.page.addView(child);
             }
         }
         int networktype = tm.getNetworkType();
         temp = null;
         switch (networktype) {
             case TelephonyManager.NETWORK_TYPE_1xRTT:
                 temp = getString(R.string._1xrtt);
                 break;
             case TelephonyManager.NETWORK_TYPE_CDMA:
                 temp = getString(R.string.cdma);
                 break;
             case TelephonyManager.NETWORK_TYPE_EDGE:
                 temp = getString(R.string.edge);
                 break;
             case TelephonyManager.NETWORK_TYPE_EHRPD:
                 temp = getString(R.string.ehrpd);
                 break;
             case TelephonyManager.NETWORK_TYPE_EVDO_0:
                 temp = getString(R.string.evdo_0);
                 break;
             case TelephonyManager.NETWORK_TYPE_EVDO_A:
                 temp = getString(R.string.evdo_a);
                 break;
             case TelephonyManager.NETWORK_TYPE_EVDO_B:
                 temp = getString(R.string.evdo_b);
                 break;
             case TelephonyManager.NETWORK_TYPE_GPRS:
                 temp = getString(R.string.gprs);
                 break;
             case TelephonyManager.NETWORK_TYPE_HSDPA:
                 temp = getString(R.string.hsdpa);
                 break;
             case TelephonyManager.NETWORK_TYPE_HSPA:
                 temp = getString(R.string.hspa);
                 break;
             case TelephonyManager.NETWORK_TYPE_HSPAP:
                 temp = getString(R.string.hspap);
                 break;
             case TelephonyManager.NETWORK_TYPE_HSUPA:
                 temp = getString(R.string.hsupa);
                 break;
             case TelephonyManager.NETWORK_TYPE_IDEN:
                 temp = getString(R.string.iden);
                 break;
             case TelephonyManager.NETWORK_TYPE_LTE:
                 temp = getString(R.string.lte);
                 break;
             case TelephonyManager.NETWORK_TYPE_UMTS:
                 temp = getString(R.string.umts);
                 break;
             case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                 temp = getString(R.string.unknown);
                 break;
 
             default:
         }
         if (temp != null) {
             child = createLine(getString(R.string.network_type), temp);
             if (child != null) {
                 this.page.addView(child);
             }
         }
         String netop = tm.getNetworkOperator();
         if (netop != null) {
             child = createLine(getString(R.string.network_operator), netop);
             if (child != null) {
                 this.page.addView(child);
             }
         }
         String netopname = tm.getNetworkOperatorName();
         if (netopname != null) {
             child = createLine(getString(R.string.network_operator_name), netopname);
             if (child != null) {
                 this.page.addView(child);
             }
         }
 
         String netcountryiso = tm.getNetworkCountryIso();
         if (netcountryiso != null) {
             child = createLine(getString(R.string.network_country_iso), netcountryiso);
             if (child != null) {
                 this.page.addView(child);
             }
         }
         String subscriberid = tm.getSubscriberId();
         if (subscriberid != null) {
             child = createLine(getString(R.string.subscriber_id), subscriberid);
             if (child != null) {
                 this.page.addView(child);
             }
         }
 
         String simcountryiso = tm.getSimCountryIso();
         if (simcountryiso != null) {
             child = createLine(getString(R.string.sim_country_iso), simcountryiso);
             if (child != null) {
                 page.addView(child);
             }
         }
         int simstate = tm.getSimState();
         temp = null;
         switch (simstate) {
             case TelephonyManager.SIM_STATE_ABSENT:
                 temp = getString(R.string.state_absent);
                 break;
             case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                 temp = getString(R.string.network_locked);
                 break;
             case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                 temp = getString(R.string.pin_required);
                 break;
             case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                 temp = getString(R.string.puk_required);
                 break;
             case TelephonyManager.SIM_STATE_READY:
                 temp = getString(R.string.ready);
                 break;
             case TelephonyManager.SIM_STATE_UNKNOWN:
                 temp = getString(R.string.unknown);
                 break;
 
             default:
         }
         if (temp != null) {
             child = createLine(getString(R.string.sim_state), temp);
             if (child != null) {
                 page.addView(child);
             }
         }
         String simserial = tm.getSimSerialNumber();
         if (simserial != null) {
             child = createLine(getString(R.string.sim_serial_number), simserial);
             if (child != null) {
                 page.addView(child);
             }
         }
         String simop = tm.getSimOperator();
         if (simop != null) {
             child = createLine(getString(R.string.sim_operator), simop);
             if (child != null) {
                 page.addView(child);
             }
         }
         String simopname = tm.getSimOperatorName();
         if (simopname != null) {
             child = createLine(getString(R.string.sim_operator_name), simopname);
             if (child != null) {
                 page.addView(child);
             }
         }
         String vmalphatag = tm.getVoiceMailAlphaTag();
         if (vmalphatag != null) {
             child = createLine(getString(R.string.voicemail_alpha_tag), vmalphatag);
             if (child != null) {
                 page.addView(child);
             }
         }
         String vmnumber = tm.getVoiceMailNumber();
         if (vmnumber != null) {
             child = createLine(getString(R.string.voicemail_number), vmnumber);
             if (child != null) {
                 page.addView(child);
             }
         }
     }
 
     private void infoConnectivity() {
         View child = null;
         page.addView(createHeader(getString(R.string.connectivity)));
         ConnectivityManager cm = (ConnectivityManager) this.getSystemService(CONNECTIVITY_SERVICE);
         NetworkInfo[] networkInfo = cm.getAllNetworkInfo();
         StringBuilder sb = new StringBuilder();
         for (NetworkInfo network : networkInfo) {
             sb.setLength(0);
             String connectivitydata = null;
             connectivitydata = network.getSubtypeName();
             if (connectivitydata != null) {
                 sb.append(getString(R.string.subtype) + ": " + connectivitydata + ", ");
             } else {
                 sb.append(getString(R.string.subtype) + ": " + getString(R.string.unknown) + ", ");
             }
 
             connectivitydata = network.getDetailedState().toString();
             if (connectivitydata != null) {
                 sb.append(getString(R.string.state) + ": " + connectivitydata + ", ");
             } else {
                 sb.append(getString(R.string.state) + ": " + getString(R.string.unknown) + ", ");
             }
 
             connectivitydata = network.getReason();
             if (connectivitydata != null) {
                 sb.append(getString(R.string.reason) + ": " + connectivitydata + ", ");
             } else {
                 sb.append(getString(R.string.reason) + ": " + getString(R.string.unknown) + ", ");
             }
 
             connectivitydata = network.getExtraInfo();
             if (connectivitydata != null) {
                 sb.append(getString(R.string.extrainfo) + ": " + connectivitydata);
             } else {
                 sb.append(getString(R.string.extrainfo) + ": " + getString(R.string.unknown));
             }
 
             child = createLine(network.getTypeName(), sb.toString());
             if (child != null) {
                 page.addView(child);
             }
         }
     }
 
     private void infoSensors() {
         View child = null;
         page.addView(createHeader(getString(R.string.sensors)));
         List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
         StringBuilder sb = new StringBuilder();
         for (Sensor sensor : sensors) {
             sb.setLength(0);
             sb.append(getString(R.string.type) + ": " + sensor.getType() + ", ");
             sb.append(getString(R.string.version) + ": " + sensor.getVersion() + ", ");
             sb.append(getString(R.string.vendor) + ": " + sensor.getVendor() + ", ");
             sb.append(getString(R.string.power) + ": " + sensor.getPower() + ", ");
             sb.append(getString(R.string.mindelay) + ": " + sensor.getMinDelay() + ", ");
             sb.append(getString(R.string.maxrange) + ": " + sensor.getMaximumRange() + ", ");
             sb.append(getString(R.string.resolution) + ": " + sensor.getResolution());
             child = createLine(sensor.getName(), sb.toString());
             if (child != null) {
                 page.addView(child);
             }
         }
         child = createLine("Temperature: ", String.valueOf(temperature));
         if (child != null) {
             page.addView(child);
         }
         child = createLine("Pressure: ", String.valueOf(pressure));
         if (child != null) {
             page.addView(child);
         }
 
     }
 
     private void infoDisplay() {
         View child = null;
         page.addView(createHeader(getString(R.string.display)));
         int width = this.getResources().getDisplayMetrics().widthPixels;
         if (width > 0) {
             child = createLine(getString(R.string.width), String.valueOf(width));
             if (child != null) {
                 page.addView(child);
             }
         }
         int height = this.getResources().getDisplayMetrics().heightPixels;
         if (height > 0) {
             child = createLine(getString(R.string.height), String.valueOf(height));
             if (child != null) {
                 page.addView(child);
             }
         }
         float density = this.getResources().getDisplayMetrics().density;
         if (density > 0) {
             child = createLine(getString(R.string.density), String.valueOf(density));
             if (child != null) {
                 page.addView(child);
             }
         }
         float densitydpi = this.getResources().getDisplayMetrics().densityDpi;
         if (densitydpi > 0) {
             child = createLine(getString(R.string.density_dpi), String.valueOf(densitydpi));
             if (child != null) {
                 page.addView(child);
             }
             if (densitydpi >= DisplayMetrics.DENSITY_XXHIGH) {
                 child = createLine(getString(R.string.density_dpi), getString(R.string.xxhdpi));
                 if (child != null) {
                     page.addView(child);
                 }
             } else if (densitydpi >= DisplayMetrics.DENSITY_XHIGH) {
                 child = createLine(getString(R.string.density_dpi), getString(R.string.xhdpi));
                 if (child != null) {
                     page.addView(child);
                 }
             } else if (densitydpi >= DisplayMetrics.DENSITY_HIGH) {
                 child = createLine(getString(R.string.density_dpi), getString(R.string.hdpi));
                 if (child != null) {
                     page.addView(child);
                 }
             } else if (densitydpi >= DisplayMetrics.DENSITY_MEDIUM) {
                 child = createLine(getString(R.string.density_dpi), getString(R.string.mdpi));
                 if (child != null) {
                     page.addView(child);
                 }
             } else if (densitydpi >= DisplayMetrics.DENSITY_LOW) {
                 child = createLine(getString(R.string.density_dpi), getString(R.string.ldpi));
                 if (child != null) {
                     page.addView(child);
                 }
             } else {
                 child = createLine(getString(R.string.density_dpi), getString(R.string.unknown));
                 if (child != null) {
                     page.addView(child);
                 }
             }
         }
         float scaledDensity = this.getResources().getDisplayMetrics().scaledDensity;
         if (scaledDensity > 0) {
             child = createLine(getString(R.string.scaled_density), String.valueOf(scaledDensity));
             if (child != null) {
                 page.addView(child);
             }
         }
         float xdpi = this.getResources().getDisplayMetrics().xdpi;
         if (xdpi > 0) {
             child = createLine(getString(R.string.xdpi), String.valueOf(xdpi));
             if (child != null) {
                 page.addView(child);
             }
         }
         float ydpi = this.getResources().getDisplayMetrics().ydpi;
         if (ydpi > 0) {
             child = createLine(getString(R.string.ydpi), String.valueOf(ydpi));
             if (child != null) {
                 page.addView(child);
             }
         }
     }
 
     @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
     private void infoKernel() {
         View child = null;
         page.addView(createHeader(getString(R.string.kernel)));
         String bootloader = android.os.Build.BOOTLOADER;
         if (bootloader != null) {
             child = createLine(getString(R.string.bootloader), bootloader);
             if (child != null) {
                 page.addView(child);
             }
         }
         String display = android.os.Build.DISPLAY;
         if (display != null) {
             child = createLine(getString(R.string.display_version), display);
             if (child != null) {
                 page.addView(child);
             }
         }
         String fingerprint = android.os.Build.FINGERPRINT;
         if (fingerprint != null) {
             child = createLine(getString(R.string.fingerprint), fingerprint);
             if (child != null) {
                 page.addView(child);
             }
         }
 
         String host = android.os.Build.HOST;
         if (host != null) {
             child = createLine(getString(R.string.host), host);
             if (child != null) {
                 page.addView(child);
             }
         }
         String id = android.os.Build.ID;
         if (id != null) {
             child = createLine(getString(R.string.id), id);
             if (child != null) {
                 page.addView(child);
             }
         }
 
         String serial = android.os.Build.SERIAL;
         if (serial != null) {
             child = createLine(getString(R.string.serial), serial);
             if (child != null) {
                 page.addView(child);
             }
         }
         String tags = android.os.Build.TAGS;
         if (tags != null) {
             child = createLine(getString(R.string.tags), tags);
             if (child != null) {
                 page.addView(child);
             }
         }
         long time = android.os.Build.TIME;
         if (time > 0) {
             child = createLine(getString(R.string.time), String.valueOf(time));
             if (child != null) {
                 page.addView(child);
             }
         }
         String type = android.os.Build.TYPE;
         if (type != null) {
             child = createLine(getString(R.string.type), type);
             if (child != null) {
                 page.addView(child);
             }
         }
         String unknown = android.os.Build.UNKNOWN;
         if (unknown != null) {
             child = createLine(getString(R.string.unknown), unknown);
             if (child != null) {
                 page.addView(child);
             }
         }
         String user = android.os.Build.USER;
         if (user != null) {
             child = createLine(getString(R.string.user), user);
             if (child != null) {
                 page.addView(child);
             }
         }
         if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
             String radioversion = android.os.Build.getRadioVersion();
             if (radioversion != null) {
                 child = createLine(getString(R.string.radioversion), radioversion);
                 if (child != null) {
                     page.addView(child);
                 }
             }
         }
         String codename = android.os.Build.VERSION.CODENAME;
         if (codename != null) {
             child = createLine(getString(R.string.codename), codename);
             if (child != null) {
                 page.addView(child);
             }
         }
         String incremental = android.os.Build.VERSION.INCREMENTAL;
         if (incremental != null) {
             child = createLine(getString(R.string.incremental), incremental);
             if (child != null) {
                 page.addView(child);
             }
         }
         String release = android.os.Build.VERSION.RELEASE;
         if (release != null) {
             child = createLine(getString(R.string.release), release);
             if (child != null) {
                 page.addView(child);
             }
         }
         int sdkInt = android.os.Build.VERSION.SDK_INT;
         if (sdkInt > 0) {
             child = createLine(getString(R.string.sdk_int), String.valueOf(sdkInt));
             if (child != null) {
                 page.addView(child);
             }
         }
     }
 
     private void infoCPU() {
         View child = null;
 
         page.addView(createHeader(getString(R.string.cpu_hardware)));
         String cpu_abi = android.os.Build.CPU_ABI;
         if (cpu_abi != null) {
             child = createLine(getString(R.string.cpu_abi), cpu_abi);
             if (child != null) {
                 page.addView(child);
             }
         }
         String cpu_abi2 = android.os.Build.CPU_ABI2;
         if (cpu_abi2 != null) {
             child = createLine(getString(R.string.cpu_abi2), cpu_abi2);
             if (child != null) {
                 page.addView(child);
             }
         }
         String board = android.os.Build.BOARD;
         if (board != null) {
             child = createLine(getString(R.string.board), board);
             if (child != null) {
                 page.addView(child);
             }
         }
         String hardware = android.os.Build.HARDWARE;
         if (hardware != null) {
             child = createLine(getString(R.string.hardware), hardware);
             if (child != null) {
                 page.addView(child);
             }
         }
         String manufacturer = android.os.Build.MANUFACTURER;
         if (manufacturer != null) {
             child = createLine(getString(R.string.manufacturer), manufacturer);
             if (child != null) {
                 page.addView(child);
             }
         }
         String model = android.os.Build.MODEL;
         if (model != null) {
             child = createLine(getString(R.string.model_name), model);
             if (child != null) {
                 page.addView(child);
             }
         }
         String device = android.os.Build.DEVICE;
         if (device != null) {
             child = createLine(getString(R.string.device_name), device);
             if (child != null) {
                 page.addView(child);
             }
         }
         String product = android.os.Build.PRODUCT;
         if (product != null) {
             child = createLine(getString(R.string.product_name), product);
             if (child != null) {
                 page.addView(child);
             }
         }
         String brand = android.os.Build.BRAND;
         if (brand != null) {
             child = createLine(getString(R.string.brand), brand);
             if (child != null) {
                 page.addView(child);
             }
         }
     }
 
     @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
     private View createHeader(String string) {
         LinearLayout child = new LinearLayout(this);
         child.setGravity(Gravity.CENTER);
 
         LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                 LayoutParams.WRAP_CONTENT);
         int bottom = (int) this.getResources().getDimension(R.dimen.margin_box_top);
         int top = (int) this.getResources().getDimension(R.dimen.margin_box_top);
         int right = (int) this.getResources().getDimension(R.dimen.margin_box_all);
         int left = (int) this.getResources().getDimension(R.dimen.margin_box_all);
         params.setMargins(left, top, right, bottom);
 
         TextView tv1 = new TextView(this);
         tv1.setText(string);
         tv1.setTextAppearance(this, R.style.textView_Bigger);
         if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
             tv1.setAllCaps(true);
         }
         child.addView(tv1, params);
         return child;
 
     }
 
     @TargetApi(Build.VERSION_CODES.HONEYCOMB)
     private View createLine(String name, String content) {
         LinearLayout child = new LinearLayout(this);
         // child.setGravity(Gravity.CENTER);
 
         LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                 LayoutParams.WRAP_CONTENT);
         int bottom = (int) this.getResources().getDimension(R.dimen.margin_box_all);
         int top = (int) this.getResources().getDimension(R.dimen.margin_box_all);
         int right = (int) this.getResources().getDimension(R.dimen.margin_box_all);
         int left = (int) this.getResources().getDimension(R.dimen.margin_box_all);
         params.setMargins(left, top, right, bottom);
 
         TextView tv1 = new TextView(this);
         tv1.setText(name);
         tv1.setTextAppearance(this, R.style.textView_Cap);
         tv1.setTextColor(Color.CYAN);
         tv1.setWidth((int) this.getResources().getDimension(R.dimen.name_width));
         // tv1.setGravity(Gravity.CENTER);
         TextView tv2 = new TextView(this);
         tv2.setText(content);
         tv2.setTextAppearance(this, R.style.textView);
         if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
             tv2.setTextIsSelectable(true);
         }
         // tv2.setGravity(Gravity.CENTER);
         child.addView(tv1, params);
         child.addView(tv2, params);
         return child;
     }
 
     @Override
     public void onAccuracyChanged(Sensor sensor, int accuracy) {
     }
 
     @Override
     public void onSensorChanged(SensorEvent event) {
         if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE
                 || event.sensor.getType() == Sensor.TYPE_TEMPERATURE) {
             this.temperature = event.values[0];
         }
         if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
             this.pressure = event.values[0];
         }
 
     }
 }
