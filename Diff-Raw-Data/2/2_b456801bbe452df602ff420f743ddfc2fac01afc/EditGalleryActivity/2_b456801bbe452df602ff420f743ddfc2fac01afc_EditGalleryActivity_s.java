 /*
  * GalDroid - a webgallery frontend for android
  * Copyright (C) 2011  Raptor 2101 [raptor2101@gmx.de]
  *		
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.  
  */
 
 package de.raptor2101.GalDroid.Activities;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.Spinner;
 import android.widget.TextView;
 import de.raptor2101.GalDroid.R;
 import de.raptor2101.GalDroid.Config.GalDroidPreference;
 import de.raptor2101.GalDroid.Config.GalleryConfig;
 import de.raptor2101.GalDroid.WebGallery.Tasks.GalleryVerifyTask;
 
 public class EditGalleryActivity extends Activity implements OnClickListener {
 	private ProgressDialog mProgressDialog; 
 	private ArrayAdapter<CharSequence> mAdapter;
 	private GalleryConfig mConfig;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    setContentView(R.layout.create_gallery_activity);
 
 	    Spinner spinner = (Spinner) findViewById(R.id.spinnerGalleryType);
 	    mAdapter = ArrayAdapter.createFromResource(
 	            this, R.array.gallery_types, android.R.layout.simple_spinner_item);
 	    mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 	    spinner.setAdapter(mAdapter);
 	    
 	    Button button = (Button) findViewById(R.id.buttonCreate);
 	    button.setOnClickListener(this);
 	    Bundle extras = getIntent().getExtras();
 	    if(extras != null)
 	    {
		    String configName = extras.getString("configName");
 		    if(configName != null) {
 		    	mConfig =  GalDroidPreference.getSetupByName(configName);
 		    	button.setText("Edit");
 		    	
 				TextView galleryName = (TextView) findViewById(R.id.editGallery);
 				TextView serverName = (TextView) findViewById(R.id.editServer);
 				
 				
 				galleryName.setText(configName);
 				serverName.setText(mConfig.RootLink);
 				String typeName = mConfig.TypeName;
 				for (int i = 0; i < mAdapter.getCount(); i++) {
 					String galleryType = mAdapter.getItem(i).toString();
 					if(galleryType.equals(typeName)) {
 						spinner.setSelection(i);
 					}
 				}
 		    }
 		    else {
 		    	button.setText(R.string.button_create);
 		    }
 	    }
 	    else {
 	    	button.setText(R.string.button_create);
 	    }
 	    mProgressDialog = new ProgressDialog(this);
 		mProgressDialog.setTitle(R.string.progress_title_verify);
 		mProgressDialog.setCancelable(false);
 	}
 
 	public void onClick(View v) {
 		
 		Spinner spinner = (Spinner) findViewById(R.id.spinnerGalleryType);
 		String name = ((TextView) findViewById(R.id.editGallery)).getText().toString();
 		String galleryType = (String)spinner.getSelectedItem();
 		String server = ((TextView) findViewById(R.id.editServer)).getText().toString().toLowerCase();
 		String username = ((TextView) findViewById(R.id.editUsername)).getText().toString();
 		String password = ((TextView) findViewById(R.id.editPassword)).getText().toString();
 		
 		
 		if(!(server.startsWith("http://") || server.startsWith("https://"))){
 			server="http://"+server;
 		}
 		
 		
 		mProgressDialog.show();
 		
 		int id = mConfig != null ? mConfig.Id : -1;
 		
 		GalleryConfig config = new GalleryConfig(id, name, galleryType, server, "");
 		GalleryVerifyTask verifyTask = new GalleryVerifyTask(config, username, password, this);
 		verifyTask.execute();
 	}
 
 	public void onGalleryVerified(Boolean result) {
 		mProgressDialog.dismiss();
 		if(result == true){
 			this.finish();
 		}
 	}
 }
 
