 package gsensor.bena.bma220;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 
 import android.content.SharedPreferences;
 import android.provider.Settings;
 import android.util.Log;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 public class AutoRun extends BroadcastReceiver {
     String mFilePath = "";
     int m_progress;
     public SharedPreferences sharedpreferences;
 
     private String getCurrentDirPath(Context paramContext) {
         return paramContext.getFilesDir().getParent();
     }
 
     public boolean check_g_sensor_exists() {
         File localFile = new File("/sys/devices/platform/s3c2440-i2c.2/i2c-2/2-000a/", "offset_x");
         return localFile.exists();
     }
 
     public boolean write_file_value(String paramString1, String paramString2, int paramInt) {
         File localFile = new File(paramString1, paramString2);
         if (localFile == null) return false;
         if (!localFile.exists()) return false;
         String str1 = Integer.toString(paramInt);
         try {
             byte[] arrayOfByte = str1.getBytes();
             FileOutputStream localFileOutputStream = new FileOutputStream(localFile);
             localFileOutputStream.write(arrayOfByte);
             localFileOutputStream.close();
             return true;
         } catch (IOException localIOException) {
             String str2 = "Error writing " + localFile;
             int k = Log.w("sensor offset ", str2, localIOException);
         }
         return false;
     }
 
     public void onReceive(Context paramContext, Intent paramIntent) {
 
         Log.d("AdjustSensorAutoRun", "start");
         if (check_g_sensor_exists()) {
             Log.d("AdjustSensorAutoRun", "bma220 exists!");
             this.mFilePath = getCurrentDirPath(paramContext);
            sharedpreferences = paramContext.getSharedPreferences("offset_pref", Context.MODE_PRIVATE);
             this.m_progress = sharedpreferences.getInt("offset_x",0);
             if (write_file_value("/sys/devices/platform/s3c2440-i2c.2/i2c-2/2-000a/", "offset_x", this.m_progress)) {
                 Log.i("AdjustSensor", " write offset_x = " + this.m_progress + " success!\n");
             }
             this.m_progress = sharedpreferences.getInt("offset_y",0);
             if (write_file_value("/sys/devices/platform/s3c2440-i2c.2/i2c-2/2-000a/", "offset_y", this.m_progress)) {
                 Log.i("AdjustSensor", " write offset_y = " + this.m_progress + " success!\n");
             }
             this.m_progress = sharedpreferences.getInt("offset_z",0);
             if (write_file_value("/sys/devices/platform/s3c2440-i2c.2/i2c-2/2-000a/", "offset_y", this.m_progress)) {
                 Log.i("AdjustSensor", " write offset_y = " + this.m_progress + " success!\n");
             }
             return;
         }
         Log.d("AdjustSensorAutoRun", "bma220 not exists!");
     }
 }
