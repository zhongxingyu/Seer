 package it.albx79.diaspora.activities;
 
 import it.albx79.diaspora.R;
 import it.albx79.diaspora.models.system.SystemModel;
import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
public class ClustersManager extends Activity {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
        Button b = (Button)findViewById(R.id.openSystemBtn);
        b.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				SystemModel model = new SystemModel();
 				model.name.set("My First System");
 				model.tech.set(1);
 				model.res.set(2);
 				model.env.set(3);
 				
 				Intent intent = new Intent(getApplicationContext(), SystemViewer.class);
				// TODO turn model into a bundle and pass it to the intent.
 				startActivity(intent);
 			}
 		});
     }
 }
