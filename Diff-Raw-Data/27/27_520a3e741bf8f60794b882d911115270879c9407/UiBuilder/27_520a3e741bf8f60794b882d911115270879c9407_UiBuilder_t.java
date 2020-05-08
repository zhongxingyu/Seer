 
 package com.fanoyong.aboutphone.ui;
 
 import java.util.List;
 
 import android.annotation.TargetApi;
 import android.app.ActivityManager;
 import android.app.ActivityManager.MemoryInfo;
 import android.content.Context;
 import android.graphics.Color;
 import android.hardware.Sensor;
 import android.hardware.SensorManager;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Build;
 import android.telephony.TelephonyManager;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.TextView;
 
 import com.fanoyong.aboutphone.R;
 public class UiBuilder {
 
     private static final String TAG = "AP|UiBuilder|";
 
     private static View createLine(String name, String content, Context mContext) {
         Log.v(TAG + "createLine|", "start");
         LinearLayout child = new LinearLayout(mContext);
         // child.setGravity(Gravity.CENTER);
 
         LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                 LayoutParams.WRAP_CONTENT);
         int bottom = (int) mContext.getResources().getDimension(R.dimen.margin_box_all);
         int top = (int) mContext.getResources().getDimension(R.dimen.margin_box_all);
         int right = (int) mContext.getResources().getDimension(R.dimen.margin_box_all);
         int left = (int) mContext.getResources().getDimension(R.dimen.margin_box_all);
         params.setMargins(left, top, right, bottom);
 
         TextView tv1 = new TextView(mContext);
         tv1.setText(name);
         tv1.setTextAppearance(mContext, R.style.textView_Cap);
         tv1.setTextColor(Color.CYAN);
         tv1.setWidth((int) mContext.getResources().getDimension(R.dimen.name_width));
         // tv1.setGravity(Gravity.CENTER);
         TextView tv2 = new TextView(mContext);
         tv2.setText(content);
         tv2.setTextAppearance(mContext, R.style.textView);
         if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
             tv2.setTextIsSelectable(true);
         }
         // tv2.setGravity(Gravity.CENTER);
         child.addView(tv1, params);
         child.addView(tv2, params);
         Log.v(TAG + "createLine|", "end");
         return child;
     }
 
     @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
     private static View createHeader(String string, Context mContext) {
         Log.v(TAG + "createHeader|", "start");
         LinearLayout child = new LinearLayout(mContext);
         child.setGravity(Gravity.CENTER);
         LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
         int bottom = (int) mContext.getResources().getDimension(R.dimen.margin_box_top);
         int top = (int) mContext.getResources().getDimension(R.dimen.margin_box_top);
         int right = (int) mContext.getResources().getDimension(R.dimen.margin_box_all);
         int left = (int) mContext.getResources().getDimension(R.dimen.margin_box_all);
         params.setMargins(left, top, right, bottom);
 
         TextView tv1 = new TextView(mContext);
         tv1.setText(string);
         tv1.setTextAppearance(mContext, R.style.textView_Bigger);
         if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
             tv1.setAllCaps(true);
         }
         child.addView(tv1, params);
         Log.v(TAG + "createHeader|", "start");
         return child;
     }
 
     public static ViewGroup infoTelephony(Context mContext) {
         Log.v(TAG + "infoTelephony|", "start");
         ViewGroup mPage = new LinearLayout(mContext);
         View child = null;
         mPage.addView(createHeader(mContext.getString(R.string.telephony), mContext));
         TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
 
         String deviceswver = tm.getDeviceSoftwareVersion();
         if (deviceswver != null) {
             child = createLine(mContext.getString(R.string.device_sw_version), deviceswver, mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         String deviceid = tm.getDeviceId();
         if (deviceid != null) {
             child = createLine(mContext.getString(R.string.device_id), deviceid, mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         int dataactivity = tm.getDataActivity();
         String temp = null;
         switch (dataactivity) {
             case TelephonyManager.DATA_ACTIVITY_DORMANT:
                 temp = mContext.getString(R.string.dormant);
                 break;
             case TelephonyManager.DATA_ACTIVITY_IN:
                 temp = mContext.getString(R.string.in);
                 break;
             case TelephonyManager.DATA_ACTIVITY_INOUT:
                 temp = mContext.getString(R.string.in_out);
                 break;
             case TelephonyManager.DATA_ACTIVITY_NONE:
                 temp = mContext.getString(R.string.none);
                 break;
             case TelephonyManager.DATA_ACTIVITY_OUT:
                 temp = mContext.getString(R.string.out);
                 break;
             default:
         }
         if (temp != null) {
             child = createLine(mContext.getString(R.string.data_activity), temp, mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         int datastate = tm.getDataState();
         temp = null;
         switch (datastate) {
             case TelephonyManager.DATA_CONNECTED:
                 temp = mContext.getString(R.string.connected);
                 break;
             case TelephonyManager.DATA_CONNECTING:
                 temp = mContext.getString(R.string.connecting);
                 break;
             case TelephonyManager.DATA_DISCONNECTED:
                 temp = mContext.getString(R.string.disconnected);
                 break;
             case TelephonyManager.DATA_SUSPENDED:
                 temp = mContext.getString(R.string.suspended);
                 break;
             default:
         }
         if (temp != null) {
             child = createLine(mContext.getString(R.string.data_state), temp, mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         int phonetype = tm.getPhoneType();
         temp = null;
         switch (phonetype) {
             case TelephonyManager.PHONE_TYPE_CDMA:
                 temp = mContext.getString(R.string.cdma);
                 break;
             case TelephonyManager.PHONE_TYPE_GSM:
                 temp = mContext.getString(R.string.gsm);
                 break;
             case TelephonyManager.PHONE_TYPE_SIP:
                 temp = mContext.getString(R.string.sip);
                 break;
             case TelephonyManager.PHONE_TYPE_NONE:
                 temp = mContext.getString(R.string.none);
                 break;
             default:
         }
         if (temp != null) {
             child = createLine(mContext.getString(R.string.phone_type), temp, mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         int networktype = tm.getNetworkType();
         temp = null;
         switch (networktype) {
             case TelephonyManager.NETWORK_TYPE_1xRTT:
                 temp = mContext.getString(R.string._1xrtt);
                 break;
             case TelephonyManager.NETWORK_TYPE_CDMA:
                 temp = mContext.getString(R.string.cdma);
                 break;
             case TelephonyManager.NETWORK_TYPE_EDGE:
                 temp = mContext.getString(R.string.edge);
                 break;
             case TelephonyManager.NETWORK_TYPE_EHRPD:
                 temp = mContext.getString(R.string.ehrpd);
                 break;
             case TelephonyManager.NETWORK_TYPE_EVDO_0:
                 temp = mContext.getString(R.string.evdo_0);
                 break;
             case TelephonyManager.NETWORK_TYPE_EVDO_A:
                 temp = mContext.getString(R.string.evdo_a);
                 break;
             case TelephonyManager.NETWORK_TYPE_EVDO_B:
                 temp = mContext.getString(R.string.evdo_b);
                 break;
             case TelephonyManager.NETWORK_TYPE_GPRS:
                 temp = mContext.getString(R.string.gprs);
                 break;
             case TelephonyManager.NETWORK_TYPE_HSDPA:
                 temp = mContext.getString(R.string.hsdpa);
                 break;
             case TelephonyManager.NETWORK_TYPE_HSPA:
                 temp = mContext.getString(R.string.hspa);
                 break;
             case TelephonyManager.NETWORK_TYPE_HSPAP:
                 temp = mContext.getString(R.string.hspap);
                 break;
             case TelephonyManager.NETWORK_TYPE_HSUPA:
                 temp = mContext.getString(R.string.hsupa);
                 break;
             case TelephonyManager.NETWORK_TYPE_IDEN:
                 temp = mContext.getString(R.string.iden);
                 break;
             case TelephonyManager.NETWORK_TYPE_LTE:
                 temp = mContext.getString(R.string.lte);
                 break;
             case TelephonyManager.NETWORK_TYPE_UMTS:
                 temp = mContext.getString(R.string.umts);
                 break;
             case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                 temp = mContext.getString(R.string.unknown);
                 break;
 
             default:
         }
         if (temp != null) {
             child = createLine(mContext.getString(R.string.network_type), temp, mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         String netop = tm.getNetworkOperator();
         if (netop != null) {
             child = createLine(mContext.getString(R.string.network_operator), netop, mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         String netopname = tm.getNetworkOperatorName();
         if (netopname != null) {
             child = createLine(mContext.getString(R.string.network_operator_name), netopname, mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
 
         String netcountryiso = tm.getNetworkCountryIso();
         if (netcountryiso != null) {
             child = createLine(mContext.getString(R.string.network_country_iso), netcountryiso, mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         String subscriberid = tm.getSubscriberId();
         if (subscriberid != null) {
             child = createLine(mContext.getString(R.string.subscriber_id), subscriberid, mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
 
         String simcountryiso = tm.getSimCountryIso();
         if (simcountryiso != null) {
             child = createLine(mContext.getString(R.string.sim_country_iso), simcountryiso, mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         int simstate = tm.getSimState();
         temp = null;
         switch (simstate) {
             case TelephonyManager.SIM_STATE_ABSENT:
                 temp = mContext.getString(R.string.state_absent);
                 break;
             case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                 temp = mContext.getString(R.string.network_locked);
                 break;
             case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                 temp = mContext.getString(R.string.pin_required);
                 break;
             case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                 temp = mContext.getString(R.string.puk_required);
                 break;
             case TelephonyManager.SIM_STATE_READY:
                 temp = mContext.getString(R.string.ready);
                 break;
             case TelephonyManager.SIM_STATE_UNKNOWN:
                 temp = mContext.getString(R.string.unknown);
                 break;
 
             default:
         }
         if (temp != null) {
             child = createLine(mContext.getString(R.string.sim_state), temp, mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         String simserial = tm.getSimSerialNumber();
         if (simserial != null) {
             child = createLine(mContext.getString(R.string.sim_serial_number), simserial, mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         String simop = tm.getSimOperator();
         if (simop != null) {
             child = createLine(mContext.getString(R.string.sim_operator), simop, mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         String simopname = tm.getSimOperatorName();
         if (simopname != null) {
             child = createLine(mContext.getString(R.string.sim_operator_name), simopname, mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         String vmalphatag = tm.getVoiceMailAlphaTag();
         if (vmalphatag != null) {
             child = createLine(mContext.getString(R.string.voicemail_alpha_tag), vmalphatag, mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         String vmnumber = tm.getVoiceMailNumber();
         if (vmnumber != null) {
             child = createLine(mContext.getString(R.string.voicemail_number), vmnumber, mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         Log.v(TAG + "infoTelephony|", "end");
         return mPage;
     }
 
     public static ViewGroup infoConnectivity(Context mContext) {
         Log.v(TAG + "infoConnectivity|", "start");
         ViewGroup mPage = new LinearLayout(mContext);
         View child = null;
         mPage.addView(createHeader(mContext.getString(R.string.connectivity), mContext));
         ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo[] networkInfo = cm.getAllNetworkInfo();
         StringBuilder sb = new StringBuilder();
         for (NetworkInfo network : networkInfo) {
             sb.setLength(0);
             String connectivitydata = null;
             connectivitydata = network.getSubtypeName();
             if (connectivitydata != null) {
                 sb.append(mContext.getString(R.string.subtype) + ": " + connectivitydata + ", ");
             } else {
                 sb.append(mContext.getString(R.string.subtype) + ": " + mContext.getString(R.string.unknown) + ", ");
             }
 
             connectivitydata = network.getDetailedState().toString();
             if (connectivitydata != null) {
                 sb.append(mContext.getString(R.string.state) + ": " + connectivitydata + ", ");
             } else {
                 sb.append(mContext.getString(R.string.state) + ": " + mContext.getString(R.string.unknown) + ", ");
             }
 
             connectivitydata = network.getReason();
             if (connectivitydata != null) {
                 sb.append(mContext.getString(R.string.reason) + ": " + connectivitydata + ", ");
             } else {
                 sb.append(mContext.getString(R.string.reason) + ": " + mContext.getString(R.string.unknown) + ", ");
             }
 
             connectivitydata = network.getExtraInfo();
             if (connectivitydata != null) {
                 sb.append(mContext.getString(R.string.extrainfo) + ": " + connectivitydata);
             } else {
                 sb.append(mContext.getString(R.string.extrainfo) + ": " + mContext.getString(R.string.unknown));
             }
 
             child = createLine(network.getTypeName(), sb.toString(), mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         Log.v(TAG + "infoConnectivity|", "end");
         return mPage;
     }
 
     public static ViewGroup infoSensors(Context mContext) {
         Log.v(TAG + "infoSensors|", "start");
         ViewGroup mPage = new LinearLayout(mContext);
         View child = null;
         mPage.addView(createHeader(mContext.getString(R.string.sensors), mContext));
         SensorManager sm = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
         List<Sensor> sensors = sm.getSensorList(Sensor.TYPE_ALL);
         StringBuilder sb = new StringBuilder();
         for (Sensor sensor : sensors) {
             sb.setLength(0);
             sb.append(mContext.getString(R.string.type) + ": " + sensor.getType() + ", ");
             sb.append(mContext.getString(R.string.version) + ": " + sensor.getVersion() + ", ");
             sb.append(mContext.getString(R.string.vendor) + ": " + sensor.getVendor() + ", ");
             sb.append(mContext.getString(R.string.power) + ": " + sensor.getPower() + ", ");
             sb.append(mContext.getString(R.string.mindelay) + ": " + sensor.getMinDelay() + ", ");
             sb.append(mContext.getString(R.string.maxrange) + ": " + sensor.getMaximumRange() + ", ");
             sb.append(mContext.getString(R.string.resolution) + ": " + sensor.getResolution());
             child = createLine(sensor.getName(), sb.toString(), mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         Log.v(TAG + "infoSensors|", "end");
         return mPage;
     }
 
     public static ViewGroup infoDisplay(Context mContext) {
         Log.v(TAG + "infoDisplay|", "start");
         ViewGroup mPage = new LinearLayout(mContext);
         View child = null;
         mPage.addView(createHeader(mContext.getString(R.string.display), mContext));
         int width = mContext.getResources().getDisplayMetrics().widthPixels;
         if (width > 0) {
             child = createLine(mContext.getString(R.string.width), String.valueOf(width), mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         int height = mContext.getResources().getDisplayMetrics().heightPixels;
         if (height > 0) {
             child = createLine(mContext.getString(R.string.height), String.valueOf(height), mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         float density = mContext.getResources().getDisplayMetrics().density;
         if (density > 0) {
             child = createLine(mContext.getString(R.string.density), String.valueOf(density), mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         float densitydpi = mContext.getResources().getDisplayMetrics().densityDpi;
         if (densitydpi > 0) {
             child = createLine(mContext.getString(R.string.density_dpi), String.valueOf(densitydpi), mContext);
             if (child != null) {
                 mPage.addView(child);
             }
             if (densitydpi >= DisplayMetrics.DENSITY_XXHIGH) {
                 child = createLine(mContext.getString(R.string.density_dpi), mContext.getString(R.string.xxhdpi), mContext);
                 if (child != null) {
                     mPage.addView(child);
                 }
             } else if (densitydpi >= DisplayMetrics.DENSITY_XHIGH) {
                 child = createLine(mContext.getString(R.string.density_dpi), mContext.getString(R.string.xhdpi), mContext);
                 if (child != null) {
                     mPage.addView(child);
                 }
             } else if (densitydpi >= DisplayMetrics.DENSITY_HIGH) {
                 child = createLine(mContext.getString(R.string.density_dpi), mContext.getString(R.string.hdpi), mContext);
                 if (child != null) {
                     mPage.addView(child);
                 }
             } else if (densitydpi >= DisplayMetrics.DENSITY_MEDIUM) {
                 child = createLine(mContext.getString(R.string.density_dpi), mContext.getString(R.string.mdpi), mContext);
                 if (child != null) {
                     mPage.addView(child);
                 }
             } else if (densitydpi >= DisplayMetrics.DENSITY_LOW) {
                 child = createLine(mContext.getString(R.string.density_dpi), mContext.getString(R.string.ldpi), mContext);
                 if (child != null) {
                     mPage.addView(child);
                 }
             } else {
                 child = createLine(mContext.getString(R.string.density_dpi), mContext.getString(R.string.unknown), mContext);
                 if (child != null) {
                     mPage.addView(child);
                 }
             }
         }
         float scaledDensity = mContext.getResources().getDisplayMetrics().scaledDensity;
         if (scaledDensity > 0) {
             child = createLine(mContext.getString(R.string.scaled_density), String.valueOf(scaledDensity), mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         float xdpi = mContext.getResources().getDisplayMetrics().xdpi;
         if (xdpi > 0) {
             child = createLine(mContext.getString(R.string.xdpi), String.valueOf(xdpi), mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         float ydpi = mContext.getResources().getDisplayMetrics().ydpi;
         if (ydpi > 0) {
             child = createLine(mContext.getString(R.string.ydpi), String.valueOf(ydpi), mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         Log.v(TAG + "infoDisplay|", "end");
         return mPage;
     }
 
     @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
     public static ViewGroup infoKernel(Context mContext) {
         Log.v(TAG + "infoKernel|", "start");
         ViewGroup mPage = new LinearLayout(mContext);
         View child = null;
         mPage.addView(createHeader(mContext.getString(R.string.kernel), mContext));
         String bootloader = android.os.Build.BOOTLOADER;
         if (bootloader != null) {
             child = createLine(mContext.getString(R.string.bootloader), bootloader, mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         String display = android.os.Build.DISPLAY;
         if (display != null) {
             child = createLine(mContext.getString(R.string.display_version), display, mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         String fingerprint = android.os.Build.FINGERPRINT;
         if (fingerprint != null) {
             child = createLine(mContext.getString(R.string.fingerprint), fingerprint, mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
 
         String host = android.os.Build.HOST;
         if (host != null) {
             child = createLine(mContext.getString(R.string.host), host, mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         String id = android.os.Build.ID;
         if (id != null) {
             child = createLine(mContext.getString(R.string.id), id, mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
 
         String serial = android.os.Build.SERIAL;
         if (serial != null) {
             child = createLine(mContext.getString(R.string.serial), serial, mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         String tags = android.os.Build.TAGS;
         if (tags != null) {
             child = createLine(mContext.getString(R.string.tags), tags, mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         long time = android.os.Build.TIME;
         if (time > 0) {
             child = createLine(mContext.getString(R.string.time), String.valueOf(time), mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         String type = android.os.Build.TYPE;
         if (type != null) {
             child = createLine(mContext.getString(R.string.type), type, mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         String unknown = android.os.Build.UNKNOWN;
         if (unknown != null) {
             child = createLine(mContext.getString(R.string.unknown), unknown, mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         String user = android.os.Build.USER;
         if (user != null) {
             child = createLine(mContext.getString(R.string.user), user, mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
             String radioversion = android.os.Build.getRadioVersion();
             if (radioversion != null) {
                 child = createLine(mContext.getString(R.string.radioversion), radioversion, mContext);
                 if (child != null) {
                     mPage.addView(child);
                 }
             }
         }
         String codename = android.os.Build.VERSION.CODENAME;
         if (codename != null) {
             child = createLine(mContext.getString(R.string.codename), codename, mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         String incremental = android.os.Build.VERSION.INCREMENTAL;
         if (incremental != null) {
             child = createLine(mContext.getString(R.string.incremental), incremental, mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         String release = android.os.Build.VERSION.RELEASE;
         if (release != null) {
             child = createLine(mContext.getString(R.string.release), release, mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         int sdkInt = android.os.Build.VERSION.SDK_INT;
         if (sdkInt > 0) {
             child = createLine(mContext.getString(R.string.sdk_int), String.valueOf(sdkInt), mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         Log.v(TAG + "infoKernel|", "end");
         return mPage;
     }
 
     @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
     public static ViewGroup infoCPU(Context mContext) {
         Log.v(TAG + "infoCPU|", "start");
         ViewGroup mPage = new LinearLayout(mContext);
         View child = null;
 
         mPage.addView(createHeader(mContext.getString(R.string.cpu_hardware), mContext));
         String cpu_abi = android.os.Build.CPU_ABI;
         if (cpu_abi != null) {
             child = createLine(mContext.getString(R.string.cpu_abi), cpu_abi, mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         String cpu_abi2 = android.os.Build.CPU_ABI2;
         if (cpu_abi2 != null) {
             child = createLine(mContext.getString(R.string.cpu_abi2), cpu_abi2, mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         String board = android.os.Build.BOARD;
         if (board != null) {
             child = createLine(mContext.getString(R.string.board), board, mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         String hardware = android.os.Build.HARDWARE;
         if (hardware != null) {
             child = createLine(mContext.getString(R.string.hardware), hardware, mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         String manufacturer = android.os.Build.MANUFACTURER;
         if (manufacturer != null) {
             child = createLine(mContext.getString(R.string.manufacturer), manufacturer, mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         String model = android.os.Build.MODEL;
         if (model != null) {
             child = createLine(mContext.getString(R.string.model_name), model, mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         String device = android.os.Build.DEVICE;
         if (device != null) {
             child = createLine(mContext.getString(R.string.device_name), device, mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         String product = android.os.Build.PRODUCT;
         if (product != null) {
             child = createLine(mContext.getString(R.string.product_name), product, mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         String brand = android.os.Build.BRAND;
         if (brand != null) {
             child = createLine(mContext.getString(R.string.brand), brand, mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
 
         MemoryInfo mi = new MemoryInfo();
         ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
         activityManager.getMemoryInfo(mi);
 
         // ((ViewGroup)
         // mPage).addView(createHeader(mContext.getString(R.string.memory),
         // mContext));
         int MB = 1024 * 1024;
         if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
             long totalMem = mi.totalMem;
 
             if (totalMem > 0) {
                 child = createLine(mContext.getString(R.string.total_memory), String.valueOf(totalMem / MB) + " MB", mContext);
                 if (child != null) {
                     mPage.addView(child);
                 }
             }
         }
         long availMem = mi.availMem;
         if (availMem > 0) {
             child = createLine(mContext.getString(R.string.available_memory), String.valueOf(availMem / MB) + " MB", mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         long threshold = mi.threshold;
         if (threshold > 0) {
             child = createLine(mContext.getString(R.string.low_memory_threshold), String.valueOf(threshold / MB) + " MB", mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         Log.v(TAG + "infoCPU|", "end");
         return mPage;
     }
 
     public static ViewGroup showGPS(Context mContext) {
         Log.v(TAG + "showGPS|", "start");
         ViewGroup mPage = new LinearLayout(mContext);
         View child = null;
         mPage.addView(createHeader(mContext.getString(R.string.sensors), mContext));
         SensorManager sm = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
         List<Sensor> sensors = sm.getSensorList(Sensor.TYPE_ALL);
         StringBuilder sb = new StringBuilder();
         for (Sensor sensor : sensors) {
             sb.setLength(0);
             sb.append(mContext.getString(R.string.type) + ": " + sensor.getType() + ", ");
             sb.append(mContext.getString(R.string.version) + ": " + sensor.getVersion() + ", ");
             sb.append(mContext.getString(R.string.vendor) + ": " + sensor.getVendor() + ", ");
             sb.append(mContext.getString(R.string.power) + ": " + sensor.getPower() + ", ");
             sb.append(mContext.getString(R.string.mindelay) + ": " + sensor.getMinDelay() + ", ");
             sb.append(mContext.getString(R.string.maxrange) + ": " + sensor.getMaximumRange() + ", ");
             sb.append(mContext.getString(R.string.resolution) + ": " + sensor.getResolution());
             child = createLine(sensor.getName(), sb.toString(), mContext);
             if (child != null) {
                 mPage.addView(child);
             }
         }
         Log.v(TAG + "showGPS|", "end");
         return mPage;
     }
 
     public static ViewGroup showEmpty(Context mContext) {
         Log.v(TAG + "showEmpty|", "start");
         ViewGroup mPage = new LinearLayout(mContext);
         // View child = null;
         mPage.addView(createHeader(mContext.getString(R.string.welcome), mContext));
         // child = createLine(sensor.getName(), sb.toString(), mContext);
         // if (child != null) {
         // mPage.addView(child);
         // }
         Log.v(TAG + "showEmpty|", "end");
         return mPage;
     }
 }
