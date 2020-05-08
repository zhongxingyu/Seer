 package it.albx79.diaspora.activities;
 
 import it.albx79.diaspora.R;
 import it.albx79.diaspora.models.system.SystemModel;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
public class ClustersManager extends RoboActivity {
	@InjectView(R.id.openSystemBtn) Button openSystem;
	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
        openSystem.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				SystemModel model = new SystemModel();
 				model.name.set("My First System");
 				model.tech.set(1);
 				model.res.set(2);
 				model.env.set(3);
 				
 				Intent intent = new Intent(getApplicationContext(), SystemViewer.class);
				Bundle bundle = model.toBundle();
				intent.putExtras(bundle);
 				startActivity(intent);
 			}
 		});
     }
 }
