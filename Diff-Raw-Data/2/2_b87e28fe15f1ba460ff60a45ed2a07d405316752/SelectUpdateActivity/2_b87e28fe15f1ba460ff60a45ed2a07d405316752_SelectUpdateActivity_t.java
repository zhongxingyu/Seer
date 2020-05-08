 package com.villainrom.otaupdater.activity;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 import com.villainrom.otaupdater.R;
 import com.villainrom.otaupdater.utility.Update;
 import com.villainrom.otaupdater.utility.UpdateManager;
 
 public class SelectUpdateActivity extends Activity {
 	private UpdateManager updateManager;
 	
 	private Set<String> alreadyAppliedUpdates;
 	
 	private List<Update> updateList;
 	
 	private Update chosenUpdate;
 	
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.select_update);
 
         updateManager = new UpdateManager(this);
         
 		alreadyAppliedUpdates = new HashSet<String>(updateManager.getAppliedUpdates());
 
 		/* Turn this Parcelable[] WTF to Update list. */
 		updateList = new ArrayList<Update>();
 		for (Parcelable p : getIntent().getParcelableArrayExtra("update")) {
 			updateList.add((Update) p);
 		}
 		
 		/* Clean the update list for already applied updates. */
 		for (Update candidateUpdate : new ArrayList<Update>(updateList)) {
 			if (alreadyAppliedUpdates.contains(candidateUpdate.name)) {
 				updateList.remove(candidateUpdate);
         	}
         }
 
 		bindUI();
     }
     
     private void bindUI() {
         final Spinner update = (Spinner) findViewById(R.id.Update);
         final Button apply = (Button) findViewById(R.id.Apply);
         final TextView description = (TextView) findViewById(R.id.Description);
 
         update.setOnItemSelectedListener(new OnItemSelectedListener() {
 			@Override
 			public void onItemSelected(AdapterView<?> arg0,
 					View arg1, int item, long arg3) {
 				chosenUpdate = updateList.get(item);
 
 				String text = "";
 				
 				/* validate preconditions */
 				boolean applicable = true;
 				for (String updateName : chosenUpdate.dependencyUpdateNames) {
 					if (! alreadyAppliedUpdates.contains(updateName)) {
 						applicable = false;
 						text += "Update " + updateName + " must be applied first.\n";
 					}
 				}
 
 				apply.setEnabled(applicable);
 				
 				description.setText(text + chosenUpdate.description);
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {
 			}
         });
        update.setAdapter(new ArrayAdapter<Update>(this, android.R.layout.simple_spinner_item, updateList));
         
         apply.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent("com.villainrom.otaupdater.APPLY");
 				intent.putExtra("update", chosenUpdate);
 				sendBroadcast(intent);
 			}
         });
     }
 }
