 /*
  *
  *  Copyright (C) 2010 GSyC/LibreSoft, Universidad Rey Juan Carlos.
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see http://www.gnu.org/licenses/. 
  *
  *  Author : Raúl Román López <rroman@gsyc.es>
  *  Author : Carlos Cortes <zoeoros@gmail.com>  (QR Code Content)
  *
  */
 
 package com.libresoft.apps.ARviewer.Tagging.Content;
 
 import java.io.File;
 import java.util.List;
 
 import com.libresoft.apps.ARviewer.Constants;
 import com.libresoft.apps.ARviewer.R;
 import com.libresoft.apps.ARviewer.File.FileManager;
 import com.libresoft.apps.ARviewer.Location.ARLocationManager;
 import com.libresoft.apps.ARviewer.Multimedia.AudioRecorder;
 import com.libresoft.apps.ARviewer.Multimedia.AudioRecorder.OnFinishListener;
 import com.libresoft.apps.ARviewer.Utils.GeoNames.AltitudeManager;
 import com.libresoft.sdk.ARviewer.Types.Audio;
 import com.libresoft.sdk.ARviewer.Types.GeoNode;
 import com.libresoft.sdk.ARviewer.Types.Note;
 import com.libresoft.sdk.ARviewer.Types.Photo;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageManager;
 import android.database.Cursor;
 import android.location.Location;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Handler;
 import android.os.Message;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.SubMenu;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class ContentAttacher{
 
 	public static final int ACTIVITY_PICK_GALLERY = 10000;
 	public static final int ACTIVITY_PICK_QRCODE = 10001;
 	public static final int ACTIVITY_PHOTO = 10002;
 	
 	public static final int MENU = 10000;
 	public static final int MENU_NOTE = 10001;
 	public static final int MENU_PHOTO = 10002;
 	public static final int MENU_AUDIO = 10003;
 	public static final int MENU_QRCODE = 10004;
 	
 	public static final int DIALOG_UPLOAD = 10000;
 	public static final int DIALOG_SELECT = 10001;
 	public static final int DIALOG_SURE = 10002;
 	public static final int DIALOG_RECORD_AUDIO = 10003;
 	public static final int DIALOG_PBAR = 10004;
 	
 	public static final String AUDIO_TMP = "audio_tmp_";
 	
 	private Activity mActivity;
 	
 	private GeoNode resource;
 	private Location resource_location;
 	private String path;
 	private AudioRecorder audio_recorder;
 	
 	private OnAttachListener onAttachListener = null;
 	
 	private OnFinishListener finishRecordingListener = new OnFinishListener() {
 		
 		@Override
 		public void onFinish() {
 			path = audio_recorder.getPath();
 			mActivity.showDialog(DIALOG_UPLOAD);
 		}
 	};
 	
 	public ContentAttacher(Activity mActivity){
 		this.mActivity = mActivity;
 	}
 	
 	public void setResourceLocation(Location res_location, boolean request_altitude){
 		this.resource_location = res_location;
 		if(!ARLocationManager.getInstance(mActivity).isLocationServiceAltitude() && request_altitude){
 			Log.i("ContentUpload", "Request altitude");
 			Toast.makeText(mActivity, 
 					"Getting altitude", 
 					Toast.LENGTH_SHORT).show();
 			final Handler altHandler = new Handler(){
 				@Override
 				public void handleMessage(Message msg) {
 					if(mActivity.isFinishing())
 						return;
 					Log.i("ContentUpload", "Altitude received");
 					Toast.makeText(mActivity, 
 							"Altitude received", 
 							Toast.LENGTH_SHORT).show();
 				}
 			};
 			
 			new Thread(){
 				public void run(){
 					resource_location.setAltitude(
 							AltitudeManager.getAbsoluteAltitude(
 									mActivity, 
 									(float) AltitudeManager.getAltitudeFromLatLong(
 											(float)resource_location.getLatitude(), 
 											(float)resource_location.getLongitude()), 
 											true));
 					altHandler.sendEmptyMessage(0);
 				}
 			}.start();
 		}
 	}
 	
 	public void onCreateOptionsMenu(Menu menu){
 		SubMenu sub = menu.addSubMenu(0, MENU, 0, R.string.label_attach_content)
 		.setIcon(R.drawable.media_add);
 		
 		sub.add(0,MENU_NOTE, 0, "Note");
 		sub.add(0,MENU_PHOTO, 0, "Photo");
 		sub.add(0,MENU_AUDIO, 0, "Audio");
 		sub.add(0,MENU_QRCODE, 0, "QRCode");
 	}
 	
 	public boolean onOptionsItemSelected (MenuItem item){
 		
 		switch(item.getItemId()){
 		case MENU:
 			break;
 			
 		case MENU_NOTE:
 			resource = new Note (null, "", "",
 					  resource_location.getLatitude(),
 					  resource_location.getLongitude(),
 					  resource_location.getAltitude(),
 					  null, null, null, 0.0);
 			mActivity.showDialog(DIALOG_UPLOAD);
 			break;
 			
 		case MENU_PHOTO:
 			resource = new Photo(0, 
 					resource_location.getLatitude(),
 					resource_location.getLongitude(), 
 					resource_location.getAltitude(), 
 					0.0, "", "", null, null, null, null, 0.0);
 			
 			mActivity.showDialog(DIALOG_SELECT);
 			break;
 			
 		case MENU_AUDIO:
 			resource = new Audio  (null, 
 					  "", 
 					  "",
 					  null, null,
 					  resource_location.getLatitude(),
 					  resource_location.getLongitude(),
 					  resource_location.getAltitude(),
 					  null, null, null, 0.0);
 			
 			mActivity.showDialog(DIALOG_SELECT);
 			break;
 			
 		case MENU_QRCODE:
 	  		PackageManager pm = mActivity.getPackageManager();
 			List<ApplicationInfo> list = pm.getInstalledApplications(PackageManager.GET_META_DATA);
 			boolean isBarcode = false;
 			for(int K = 0; K< list.size(); K++){
 				if( list.get(K).packageName.equals("com.google.zxing.client.android")){
 					isBarcode = true;
 					break;
 				}
 			}
 
 			if (!isBarcode){
 				Toast.makeText(mActivity.getBaseContext(), 
 						"Please, install Barcode Scanner from Market",
 						Toast.LENGTH_LONG).show();
 				break;
 			}
 
 			Intent intent = new Intent("com.google.zxing.client.android.SCAN");
 			intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
 			mActivity.startActivityForResult(intent, ACTIVITY_PICK_QRCODE);			
 			break;			
 			
 		default:
 			return false;
 		}
 		return true;
 	}
 	
 	public Dialog onCreateDialog(int id){
 		
 		switch(id){
     		
 		case DIALOG_SELECT:
 			return new AlertDialog.Builder(mActivity)	        
 	        .setTitle("Select source")
 	        .setPositiveButton("Create", new DialogInterface.OnClickListener() {
 	            public void onClick(DialogInterface dialog, int whichButton) {
 	            	if(Photo.class.isInstance(resource)){
 	            		FileManager.getInstance();
 	            		Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	            		path = FileManager.PHOTO_DIRECTORY + "data_" + Long.toString(System.currentTimeMillis()) + ".jpg";
	            		if(Build.VERSION.SDK_INT > Constants.ANDROID_ECLAIR_MR1)
	            			path = "/mnt" + path;
	            		Uri outputFile = Uri.fromFile(new File(path));
	            		i.putExtra(MediaStore.EXTRA_OUTPUT, outputFile);
 	        			mActivity.startActivityForResult(i, ACTIVITY_PHOTO);
 	            	}else if(Audio.class.isInstance(resource)){
 	            		FileManager.getInstance();
 	            		audio_recorder = new AudioRecorder(mActivity);
 	            		audio_recorder.doRecording(AUDIO_TMP + Long.toString(System.currentTimeMillis()), DIALOG_RECORD_AUDIO);
 	            	}
 	            }
 	        })
 	        .setNeutralButton("Pick", new DialogInterface.OnClickListener() {
 	            public void onClick(DialogInterface dialog, int whichButton) {
 	            	if(Photo.class.isInstance(resource)){
 	        			Intent i = new Intent(Intent.ACTION_PICK) ;
 	        	    	i.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, 
 	        	    					 MediaStore.Images.Media.CONTENT_TYPE) ;
 	        	    	
 	        	    	mActivity.startActivityForResult(i,ACTIVITY_PICK_GALLERY) ;
 	            	}else if(Audio.class.isInstance(resource)){
 	        			Intent i2;
 	        			if((Build.VERSION.SDK_INT < Constants.ANDROID_ECLAIR) || 
 	        					(Build.VERSION.SDK_INT > Constants.ANDROID_ECLAIR_MR1)){
 	        				i2 = new Intent(Intent.ACTION_PICK) ;
 	        				i2.setDataAndType(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
 	        									MediaStore.Audio.Media.CONTENT_TYPE) ;
 	        			}else{
 	        				i2 = new Intent(mActivity, ContentPick.class) ;
 	        				i2.putExtra("MAIN_PATH", FileManager.AUDIO_DIRECTORY);
 	        				i2.putExtra("MIME_TYPE", "audio/3gpp");
 	        			}
 	        	    	
 	        	    	mActivity.startActivityForResult(i2,ACTIVITY_PICK_GALLERY) ;
 	            	}
 	            }
 	        })
 	        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 	            public void onClick(DialogInterface dialog, int whichButton) {
 	
 	                /* User clicked cancel so do some stuff */
 	            }
 	        })
 	        .create();
 			
 		case DIALOG_RECORD_AUDIO:
 			return audio_recorder.getRecordDialog(DIALOG_RECORD_AUDIO, finishRecordingListener);
 		
 		case DIALOG_UPLOAD:
 			
 			LayoutInflater factory = LayoutInflater.from(mActivity);
     		final View textEntryView = factory.inflate(R.layout.note, null);
     		
     		final EditText title = (EditText) textEntryView.findViewById (R.id.txtTitle);
     		final EditText body = (EditText) textEntryView.findViewById (R.id.txtBody);
             
             if(Note.class.isInstance(resource)) {
             	Note note = (Note) resource;
             	title.setText(note.getTitle());
             	body.setText(note.getBody());
             }
             
 	        return new AlertDialog.Builder(mActivity)	        
 	        .setTitle(R.string.label_create_resource)
 	        .setView(textEntryView)
 	        .setPositiveButton(R.string.label_create, new DialogInterface.OnClickListener() {
 	            public void onClick(DialogInterface dialog, int whichButton) {
 
 	            	GeoNode result = null;
 	            	if(Note.class.isInstance(resource)){	    	            		
 	            		Note note = (Note) resource;
 	            		note.setTitle(title.getText().toString());
 	            		note.setBody(body.getText().toString());
 	            		result = note;
 	            	}else if(Photo.class.isInstance(resource)){
 	            		Photo photo = (Photo) resource;
 	            		photo.setName(title.getText().toString());
 	            		photo.setDescription(body.getText().toString());
 	            		photo.setPhotoPath(path);
 	            		result = photo;
 	            	}else if(Audio.class.isInstance(resource)){
 	            		Audio audio = (Audio) resource;
 	            		audio.setName(title.getText().toString());
 	            		audio.setDescription(body.getText().toString());
 	            		audio.setPath(path);
 	            		
 	            		result = audio;
 	            	}
 	            	if(onAttachListener != null)
 	            		onAttachListener.onReady(result);
 	            }
 	        })
 	        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
 	            public void onClick(DialogInterface dialog, int whichButton) {
 	
 	                /* User clicked cancel so do some stuff */
 	            }
 	        })
 	        .create();
 			
 		}
 		return null;
 		
 	}
 	
 	public boolean onActivityResult(int requestCode, int resultCode, Intent data){
 		if(resultCode != Activity.RESULT_OK)
 			return false;
 		
 		switch(requestCode){
 		
 		case ACTIVITY_PICK_GALLERY:
 			if((Build.VERSION.SDK_INT < Constants.ANDROID_ECLAIR) || 
 					(Build.VERSION.SDK_INT > Constants.ANDROID_ECLAIR_MR1)|| 
 					(!Audio.class.isInstance(resource))){
 				Cursor c = mActivity.managedQuery(data.getData(),null,null,null,null);
 				if( c.moveToFirst() )
 					path = c.getString(1) ;
 			}else{
 				path = data.getStringExtra("PATH");
 			}
 
 			mActivity.showDialog(DIALOG_UPLOAD);
 			break;
 			
 		case ACTIVITY_PHOTO:
     		mActivity.showDialog(DIALOG_UPLOAD);
 			break;
 			
 		case ACTIVITY_PICK_QRCODE:
 			
 			// The info must have the next body:
 			// Titulo (String);Descripcion (String);Latitud (String);Longitud (String)
 			
     		if( resultCode != Activity.RESULT_CANCELED ){
     			String contents = data.getStringExtra("SCAN_RESULT");
 
     			try{
     				
     				if (contents.indexOf("http://")!= -1)
     					contents = (String) contents.subSequence(7, contents.length());
     					
     				String[] info = contents.split(";");
    				
     				resource = new Note (null, info[0], info[1],
     							Double.parseDouble(info[2]),
     							Double.parseDouble(info[3]),
     						  0.0,
     						  null, null, null, 0.0);
     				
     			}catch(Exception e){
     				Toast.makeText(mActivity.getBaseContext(), 
     						"There was a read error with QRcode format", 
     						Toast.LENGTH_LONG).show();
     			}
     		}else
     			Toast.makeText(mActivity.getBaseContext(), 
     					"There was a read error with QRCode", 
     					Toast.LENGTH_LONG).show();
     		
 			
 			mActivity.showDialog(DIALOG_UPLOAD);
 			break;
 			
 		default:
 			return false;
 		}
 		return true;
 	}
 	
 	
 	public void setOnAttachListener(OnAttachListener onAttachListener){
     	this.onAttachListener = onAttachListener;
     }
     
     public void unregisterListeners(){
     	onAttachListener = null;
     }
     
     public interface OnAttachListener {
 		public abstract void onReady(GeoNode node);
 	}
 }
