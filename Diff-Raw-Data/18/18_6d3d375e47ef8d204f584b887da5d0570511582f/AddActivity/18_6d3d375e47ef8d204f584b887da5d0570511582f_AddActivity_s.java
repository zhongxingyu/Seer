 package cz.vutbr.fit.tam.paint3d;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 public class AddActivity extends Activity implements SensorEventListener {
     public static final String TAG = "3Dpaint|AddActivity";
     private SensorManager mSensorManager;
     private Painting painting;
     private EditText name;
     private ProgressDialog pd;
     private Boolean isSaving = false;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.add);
         mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
         name = (EditText) findViewById(R.id.name);
 
         pd = new ProgressDialog(this);
         pd.setMessage(getString(R.string.painting_save));
         pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
     }
 
     public void startPaint(View v) {
         final ToggleButton button = (ToggleButton) v;
         if (name.getText().toString().trim().isEmpty()) {
             Toast.makeText(AddActivity.this, AddActivity.this.getText(R.string.fill_painting_name), Toast.LENGTH_LONG).show();
             button.setChecked(false);
             return;
         }
         
         if (button.isChecked()) {
             mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_GAME);
             painting = new Painting(AddActivity.this);
         } else {
             mSensorManager.unregisterListener(this);
             if (!isSaving) {
                 isSaving = true;
                 painting.name = name.getText().toString();
                 DateFormat dateFormat = new SimpleDateFormat("d. M. y H:mm:ss");
                 Calendar cal = Calendar.getInstance();
                 painting.created = dateFormat.format(cal.getTime());
                 new PaintingSaveAsyncTask().execute();
             }
         }
     }
 
     @Override
     public void onSensorChanged(SensorEvent event) {
         if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
             Log.v(TAG, event.timestamp + ": " + event.values[0] + ", " + event.values[1] + ", " + event.values[2]);
             painting.paintingPointSet.add(new PaintingPoint(event.values[0], event.values[1], event.values[2]));
         }
     }
 
     @Override
     public void onAccuracyChanged(Sensor sensor, int accuracy) {
     }
 
     @Override
     protected void onStop() {
         mSensorManager.unregisterListener(this);
         super.onStop();
     }
 
     private class PaintingSaveAsyncTask extends AsyncTask<Void, Integer, Boolean> {
 
         @Override
         protected void onPreExecute() {
             pd.setMax(painting.paintingPointSet.size());
             pd.show();
         }
 
         @Override
         protected Boolean doInBackground(Void... arg0) {
             //Prumer
             PaintingPoint avg = new PaintingPoint(0, 0, 0);
             for (PaintingPoint p : painting.paintingPointSet) {
                 avg.add(p);
             }
             avg.x /= painting.paintingPointSet.size();
             avg.y /= painting.paintingPointSet.size();
             avg.z /= painting.paintingPointSet.size();
            Log.d(TAG, "avg = " + avg.x + ", " + avg.y + ", " + avg.z);
             
             //Odecteni prumeru a integrace
             PaintingPoint v = new PaintingPoint(0, 0, 0);	//Rychlost
             PaintingPoint x = new PaintingPoint(0, 0, 0);	//Poloha
             for (int i = 0; i < painting.paintingPointSet.size(); i++) {	//TODO Aktualozvat prubeh progress dialogu
                 PaintingPoint a = painting.paintingPointSet.get(i);
                 
                 a.sub(avg);	//Odecteme prumer
                 v.add(a);	//Zintegrujeme zrychleni na rychlost
                 x.add(v);	//Zintegrujeme rychlost na polohu
                Log.v(TAG, i + ": " + a.x + ", " + a.y + ", " + a.z + " -> " + v.x + ", " + v.y + ", " + v.z + " -> " + x.x + ", " + x.y + ", " + x.z);
                 
                painting.paintingPointSet.set(i, x);
                 publishProgress(i + 1);
             }
             
             try {
                 painting.save();
             } catch (Exception e) {
                 Log.w(TAG, e);
                 return false;
             }
             
             return true;
         }
         
         @Override
         protected void onProgressUpdate(Integer... progress) {
             pd.setProgress(progress[0]);
         }
         
         @Override
         protected void onPostExecute(Boolean result) {
             pd.dismiss();
             Intent intent = new Intent(AddActivity.this, DetailActivity.class);
             intent.putExtra("paintingId", painting.paintingId);
             startActivity(intent);
         }
     }
 }
