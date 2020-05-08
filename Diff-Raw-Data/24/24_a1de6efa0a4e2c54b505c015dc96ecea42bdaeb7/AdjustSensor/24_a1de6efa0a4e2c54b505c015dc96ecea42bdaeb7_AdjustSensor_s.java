 package gsensor.bena.bma220;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.app.AlertDialog;
 //import android.app.AlertDialog.Builder;
 import android.content.Context;
 import android.content.DialogInterface;
 //import android.content.DialogInterface.OnClickListener;
 //import android.content.Intent;
 import android.content.SharedPreferences;
 //import android.content.SharedPreferences.Editor;
 //import android.os.Bundle;
 import android.provider.Settings;
 //import android.provider.Settings.System;
 import android.util.Log;
 import android.view.View;
 //import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.SeekBar;
 //import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 import android.widget.Toast;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.List;
 //import java.io.InputStream;
 //import java.io.OutputStream;
 
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 
 public class AdjustSensor extends Activity
         implements SeekBar.OnSeekBarChangeListener, SensorEventListener {
 
     public int mAccelerometer;
     int mAutoRunValue = 0;
     Context mContex;
    String mFilePath = "";
     TextView mProgressText;
     SeekBar mSeekBar_x;
     SeekBar mSeekBar_y;
     SeekBar mSeekBar_z;
     TextView mSeek_X_Text;
     TextView mSeek_Y_Text;
     TextView mSeek_Z_Text;
     int m_progress;
     Button reset_button;
     Button save_button;
     Button cancel_button;
     View.OnClickListener mResetListener;
     View.OnClickListener mSaveListener;
     View.OnClickListener mCancelListener;
 
     SensorManager mSensorManager;
     Sensor mAccelerometerSensor;
     Sensor mMagneticFieldSensor;
     TextView mXValueText;
     TextView mYValueText;
     TextView mZValueText;
 
     public AdjustSensor() {
         // Кнопка сохранения
         this.mSaveListener = new View.OnClickListener() {
             public void onClick(View paramView) {
                 AdjustSensor localAdjustSensor = AdjustSensor.this;
                 AlertDialog.Builder Dialog_alert = new AlertDialog.Builder(localAdjustSensor);
                 Dialog_alert.setTitle(R.string.save_dialog_title)
                         .setMessage(R.string.save_dialog_message)
                         .setCancelable(false)
                         .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int id) {
                                 if (AdjustSensor.this.save_offset_config()) {
                                     Toast.makeText(AdjustSensor.this, R.string.save_config_success, 0).show();
                                     return;
                                 }
                                 Toast.makeText(AdjustSensor.this, R.string.save_config_failed, 0).show();
                             }
                         }).setNegativeButton("Cancel",
                         new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int id) {
                                 Toast.makeText(AdjustSensor.this, R.string.notify_message_cancel, 0).show();
                             }
                         });
                 Dialog_alert.show();
             }
         };
         // Кнопка завершения
         this.mCancelListener = new View.OnClickListener() {
             public void onClick(View paramView) {
                 AdjustSensor.this.finish();
             }
         };
         // Кнопка сброса
         this.mResetListener = new View.OnClickListener() {
             public void onClick(View paramView) {
                 AdjustSensor.this.mSeekBar_x.setProgress(8);
                 AdjustSensor.this.mSeekBar_y.setProgress(8);
                 AdjustSensor.this.mSeekBar_z.setProgress(8);
                 AdjustSensor.this.mSeek_X_Text.setText("x = 0");
                 AdjustSensor.this.mSeek_Y_Text.setText("y = 0");
                 AdjustSensor.this.mSeek_Z_Text.setText("z = 0");
                 if (!AdjustSensor.this.write_file_value("/sys/devices/platform/s3c2440-i2c.2/i2c-2/2-000a/", "offset_x", 0)) {
                     Log.e("AdjustSensor", "offset_x write 0 reset Error!\n");
                     return;
                 }
                 Log.i("AdjustSensor", "offset_x write 0 reset success!\n");
                 if (!AdjustSensor.this.write_file_value("/sys/devices/platform/s3c2440-i2c.2/i2c-2/2-000a/", "offset_y", 0)) {
                     Log.e("AdjustSensor", "offset_y write 0 reset Error!\n");
                     return;
                 }
                 Log.i("AdjustSensor", "offset_y write 0 reset success!\n");
                 if (!AdjustSensor.this.write_file_value("/sys/devices/platform/s3c2440-i2c.2/i2c-2/2-000a/", "offset_z", 0)) {
                     Log.e("AdjustSensor", "offset_z write 0 reset success!\n");
                     return;
                 }
                 Log.i("AdjustSensor", "offset_z write 0 reset success!\n");
             }
         };
 
     }
 
    private String getCurrentDirPath() {
         return getFilesDir().getParent();
     }

     public boolean check_gsensor_exists() {
         File localFile = new File("/sys/devices/platform/s3c2440-i2c.2/i2c-2/2-000a/", "offset_x");
         return localFile.exists();
     }
 
     public void onCreate(Bundle paramBundle) {
         super.onCreate(paramBundle);
         setContentView(R.layout.main);
         Log.i("AdjustSensor", "bena AjdustPanel start!");
         this.mAccelerometer = Settings.System.getInt(getContentResolver(), "accelerometer_rotation", 0);
         Log.i("AdjustSensor","mAccelerometer = " + String.valueOf(this.mAccelerometer));
         if (this.mAccelerometer == 0) {
             Settings.System.putInt(getContentResolver(), "accelerometer_rotation", 1);
             this.mAccelerometer = Settings.System.getInt(getContentResolver(), "accelerometer_rotation", 0);
             Log.i("AdjustSensor", String.valueOf("set mAccelerometer = " + this.mAccelerometer));
         }
        this.mFilePath = getCurrentDirPath();
         Bundle localBundle = getIntent().getExtras();
         if (localBundle != null) {
             this.mAutoRunValue = localBundle.getInt("AutoRunTT");
         }
         if (this.mAutoRunValue == 1234) {
             finish();
             return;
         }
         this.mContex = getApplicationContext();
         this.mSeekBar_x = (SeekBar) findViewById(R.id.seek_x);
         this.mSeekBar_y = (SeekBar) findViewById(R.id.seek_y);
         this.mSeekBar_z = (SeekBar) findViewById(R.id.seek_z);
         this.mSeekBar_x.setOnSeekBarChangeListener(this);
         this.mSeekBar_y.setOnSeekBarChangeListener(this);
         this.mSeekBar_z.setOnSeekBarChangeListener(this);
         this.mSeek_X_Text = (TextView) findViewById(R.id.seek_x_tip);
         this.mSeek_Y_Text = (TextView) findViewById(R.id.seek_y_tip);
         this.mSeek_Z_Text = (TextView) findViewById(R.id.seek_z_tip);
 
         this.mXValueText = (TextView) findViewById(R.id.sensor_x_text);
         this.mYValueText = (TextView) findViewById(R.id.sensor_y_text);
         this.mZValueText = (TextView) findViewById(R.id.sensor_z_text);
 
         this.mProgressText = (TextView) findViewById(R.id.progress);
         // save Button
         this.save_button = (Button) findViewById(R.id.button_save);
         Button localButton2 = this.save_button;
         View.OnClickListener localOnClickListener1 = this.mSaveListener;
         localButton2.setOnClickListener(localOnClickListener1);
         //
         this.cancel_button = (Button) findViewById(R.id.button_cancel);
         Button localButton4 = this.cancel_button;
         View.OnClickListener localOnClickListener2 = this.mCancelListener;
         localButton4.setOnClickListener(localOnClickListener2);
         //
         this.reset_button = (Button) findViewById(R.id.button_reset);
         Button localButton6 = this.reset_button;
         View.OnClickListener localOnClickListener3 = this.mResetListener;
         localButton6.setOnClickListener(localOnClickListener3);
 
         this.mSeek_X_Text.setText("x = 0");
         this.mSeek_Y_Text.setText("y = 0");
         this.mSeek_Z_Text.setText("z = 0");
         int max_progress = 16;//Math.abs(-1) * 2;
         int def_progress = 8;
         this.mSeekBar_x.setMax(max_progress);
         this.mSeekBar_x.setProgress(def_progress);
         this.mSeekBar_x.setSecondaryProgress(0);
         this.mSeekBar_y.setMax(max_progress);
         this.mSeekBar_y.setProgress(def_progress);
         this.mSeekBar_y.setSecondaryProgress(0);
         this.mSeekBar_z.setMax(max_progress);
         this.mSeekBar_z.setProgress(def_progress);
         this.mSeekBar_z.setSecondaryProgress(0);
         if (!check_gsensor_exists()) {
             this.mSeekBar_x.setEnabled(false);
             this.mSeekBar_y.setEnabled(false);
             this.mSeekBar_z.setEnabled(false);
             this.reset_button.setEnabled(false);
             this.save_button.setEnabled(false);
             this.cancel_button.setEnabled(false);
             this.mSeek_X_Text.setText("x = 0");
             this.mSeek_Y_Text.setText("y = 0");
             this.mSeek_Z_Text.setText("z = 0");
             return;
         }
         int read_offset_x = read_file_value("/sys/devices/platform/s3c2440-i2c.2/i2c-2/2-000a/", "offset_x");
         Log.i("AdjustSensor", "read offset_x value = " + read_offset_x);
         if (read_offset_x != 55537) {
             this.mSeekBar_x.setEnabled(true);
             this.mSeekBar_x.setProgress(read_offset_x + def_progress);
             this.mSeek_X_Text.setText("x = " + read_offset_x);
         }
         int read_offset_y = read_file_value("/sys/devices/platform/s3c2440-i2c.2/i2c-2/2-000a/", "offset_y");
         Log.i("AdjustSensor", "read offset_y value = " + read_offset_y);
         if (read_offset_y != 55537) {
             this.mSeekBar_y.setEnabled(true);
             this.mSeekBar_y.setProgress(read_offset_y + def_progress);
             this.mSeek_Y_Text.setText("y = " + read_offset_y);
         }
         int read_offset_z = read_file_value("/sys/devices/platform/s3c2440-i2c.2/i2c-2/2-000a/", "offset_z");
         Log.i("AdjustSensor", "read offset_z value = " + read_offset_z);
         if (read_offset_z != 55537) {
             this.mSeekBar_z.setEnabled(true);
             this.mSeekBar_z.setProgress(read_offset_z + def_progress);
             this.mSeek_Z_Text.setText("z = " + read_offset_z);
         }
         mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
         List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
         if(sensors.size() > 0)
         {
             for (Sensor sensor : sensors) {
                 switch(sensor.getType())
                 {
                 case Sensor.TYPE_ACCELEROMETER:
                     if(mAccelerometerSensor == null) mAccelerometerSensor = sensor;
                     break;
                case Sensor.TYPE_GRAVITY:
                     if(mMagneticFieldSensor == null) mMagneticFieldSensor = sensor;
                    break;
                 default:
                     break;
                 }
         }
         }
         mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
         mSensorManager.registerListener(this, mMagneticFieldSensor, SensorManager.SENSOR_DELAY_NORMAL);
         mXValueText.setText("default = ");
         mYValueText.setText("default = ");
         mZValueText.setText("default = ");
     }
 
 
     public void onSensorChanged(SensorEvent event) {
         float [] values = event.values;
         switch(event.sensor.getType())
         {
         case Sensor.TYPE_ACCELEROMETER:
             {
                 mXValueText.setText("default = " + String.format("%1.3f", event.values[SensorManager.DATA_X]));
 				mYValueText.setText("default = " + String.format("%1.3f", event.values[SensorManager.DATA_Y]));
 				mZValueText.setText("default = " + String.format("%1.3f", event.values[SensorManager.DATA_Z]));
             }
             break;
         }
     }
 
     public void onAccuracyChanged(Sensor sensor, int accuracy) {
     }
 
     public void onProgressChanged(SeekBar paramSeekBar, int paramInt, boolean paramBoolean) {
         this.m_progress = paramSeekBar.getProgress() + -8;
         if (paramSeekBar == this.mSeekBar_x) {
             this.mSeek_X_Text.setText("x = " + this.m_progress);
             return;
         }
         if (paramSeekBar == this.mSeekBar_y) {
             this.mSeek_Y_Text.setText("y = " + this.m_progress);
             return;
         }
         if (paramSeekBar == this.mSeekBar_z) {
             this.mSeek_Z_Text.setText("z = " + this.m_progress);
         }
     }
 
     public void onStartTrackingTouch(SeekBar paramSeekBar) {
         this.m_progress = paramSeekBar.getProgress() + -8;
         if (paramSeekBar == this.mSeekBar_x) {
             this.mSeek_X_Text.setText("x = " + this.m_progress);
             return;
         }
         if (paramSeekBar == this.mSeekBar_y) {
             this.mSeek_Y_Text.setText("y = " + this.m_progress);
             return;
         }
         if (paramSeekBar == this.mSeekBar_z) {
             this.mSeek_Z_Text.setText("z = " + this.m_progress);
         }
     }
 
     protected void onStop() {
         Log.i("AdjustSensor", "onStop");
         this.mAccelerometer = 1;
         super.onStop();
     }
 
     public void onStopTrackingTouch(SeekBar paramSeekBar) {
         this.m_progress = paramSeekBar.getProgress() + -8;
         if (paramSeekBar == this.mSeekBar_x) {
             this.mSeek_X_Text.setText("x = " + this.m_progress);
             if (write_file_value("/sys/devices/platform/s3c2440-i2c.2/i2c-2/2-000a/", "offset_x", this.m_progress)) {
                 Log.i("AdjustSensor", " write offset_x = " + this.m_progress + " success!\n");
             }
             return;
         }
         if (paramSeekBar == this.mSeekBar_y) {
             this.mSeek_Y_Text.setText("y = " + this.m_progress);
             if (write_file_value("/sys/devices/platform/s3c2440-i2c.2/i2c-2/2-000a/", "offset_y", this.m_progress)) {
                 Log.i("AdjustSensor", " write offset_y = " + this.m_progress + " success!\n");
             }
             return;
         }
         if (paramSeekBar == this.mSeekBar_z) {
             this.mSeek_Z_Text.setText("z = " + this.m_progress);
             if (write_file_value("/sys/devices/platform/s3c2440-i2c.2/i2c-2/2-000a/", "offset_z", this.m_progress)) {
                 Log.i("AdjustSensor", " write offset_z = " + this.m_progress + " success!\n");
             }
         }
     }
 
     public int read_file_value(String paramString1, String paramString2) {
         File localFile = new File(paramString1, paramString2);
 //        if (localFile == null) return 55537;
         if (!localFile.exists()) return 55537;
         try {
             FileInputStream localFileInputStream = new FileInputStream(localFile);
             byte[] arrayOfByte = new byte[localFileInputStream.available()];
             int k = localFileInputStream.read(arrayOfByte);
             localFileInputStream.close();
             return Integer.parseInt(new String(arrayOfByte).trim());
         } catch (IOException localIOException) {
             String str = "Error read " + localFile;
             Log.w("sensor offset ", str, localIOException);
         }
         return 55537;
     }
 
     public boolean save_offset_config() {
         SharedPreferences.Editor localEditor1 = getSharedPreferences("offset_pref", 0).edit();
         this.m_progress = this.mSeekBar_x.getProgress() + -8;
         localEditor1.putInt("offset_x", this.m_progress);
         this.m_progress = this.mSeekBar_y.getProgress() + -8;
         localEditor1.putInt("offset_y", this.m_progress);
         this.m_progress = this.mSeekBar_z.getProgress() + -8;
         localEditor1.putInt("offset_z", this.m_progress);
         localEditor1.commit();
         return true;
     }
 
     public boolean write_file_value(String paramString1, String paramString2, int paramInt) {
         File localFile = new File(paramString1, paramString2);
 //        if (localFile == null) return false;
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
             Log.w("sensor offset ", str2, localIOException);
         }
         return false;
     }
 }
