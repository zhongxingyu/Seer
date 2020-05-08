 package com.MobiSeeker.PrescriptionWatcher.activities;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 
 import com.MobiSeeker.PrescriptionWatcher.R;
 import com.MobiSeeker.PrescriptionWatcher.data.Adapter;
 import com.MobiSeeker.PrescriptionWatcher.data.Entry;
 import com.MobiSeeker.PrescriptionWatcher.data.PrescriptionRepository;
 
 import java.util.List;
 
 import roboguice.activity.RoboListActivity;
 import roboguice.inject.InjectView;
 
 public class Prescriptions extends BaseActivity {
 
 	protected @InjectView(R.id.list)
 	ListView list;
 	
     private final static String TAG = "com.MobiSeeker.PrescriptionWatcher.activities.Prescriptions";
 
     protected PrescriptionRepository prescriptionRepository;
 
     protected Adapter adapter;
 
     public static void start(Context context) {
         Intent intent = new Intent(context, Prescriptions.class);
         context.startActivity(intent);
     }
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.prescriptions);
         this.prescriptionRepository = new PrescriptionRepository();
         setCurrentRoboActivity(this);
     }
 
     @Override
     protected void onStart() {
         super.onStart();
 
         loadEntries();

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Entry entry = (Entry)list.getItemAtPosition(position);
                LaunchPrescription(entry);
            }
            });
          }
 
     public void loadEntries()
     {
     	
     	 try {
     	        List<Entry> entries = this.prescriptionRepository.getEntries(this);
     	        this.adapter = new Adapter(this, 0, entries);
     	        this.list.setAdapter(adapter);
     	        }
     	        catch(Exception exception){
     	            Log.e(Prescriptions.TAG, "Prescriptions onStart", exception);
     	        }
     	  
     }
     
     private void LaunchPrescription(Entry entry) {
         Prescription.start(this, entry);
         this.finish();
     }
 
     public void createPrescription(View view) {
         Prescription.start(this);
     }
 
     @Override
     protected void onRestart() {
     	// TODO Auto-generated method stub
     	
     	super.onRestart();
     	setCurrentRoboActivity(this);
     }
 
     @Override
     protected void onResume() {
     	// TODO Auto-generated method stub
     	super.onResume();
     	setCurrentRoboActivity(this);
     	loadEntries();
     }
 }
