 package it.albx79.diaspora.activities;
 
 import it.albx79.diaspora.R;
import it.albx79.diaspora.controllers.Connector;
 import it.albx79.diaspora.models.system.SystemModel;
 import roboguice.activity.RoboActivity;
 import roboguice.inject.InjectView;
 import android.content.Intent;
 import android.os.Bundle;
 import android.widget.TextView;
 
 public class SystemViewer extends RoboActivity {
 	@InjectView(R.id.systemName) TextView systemName;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.system_view);
 		Intent i = getIntent();
		SystemModel model = new SystemModel();
		Bundle extras = i.getExtras();
		if (extras != null)
			model.readBundle(extras);
 //		Connector.bind(model.name, systemName);
 	}
 
 }
