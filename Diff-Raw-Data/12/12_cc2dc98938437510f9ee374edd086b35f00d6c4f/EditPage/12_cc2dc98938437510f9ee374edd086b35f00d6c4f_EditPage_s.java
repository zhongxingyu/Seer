 package tw.edu.ntu.fortour;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Calendar;
 
 import com.dropbox.client2.android.AndroidAuthSession;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.app.TimePickerDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.location.LocationManager;
 import android.media.MediaRecorder;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.preference.PreferenceManager;
 import android.provider.MediaStore;
 import android.provider.Settings;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.GridView;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.TimePicker;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class EditPage extends Activity {
 	private String TAG = "EditPage";
 	private ImageView imageViewOPImage;
 	private EditText editTextOPStory, editTextOPLocation, editTextOPDate, editTextOPTime;
 	private ImageButton buttonOPOK, buttonOPSticker, buttonOPHelp, buttonOPRecord, buttonOPLocation;
 	private Bitmap bm;
 	private Uri bmUriPath;
 	private ImageUtil imgUtil;
 	private String mFileName, mMediaFileName, locName;
 	private String ftStoryTimeDate, ftStoryTimeTime;
 	private MediaRecorder mMediaRecorder;
 	private ProgressDialog mProgressDlg;
 	private LocationManager mLocationManager;
 	private long ftStoryTime;
 	private double locLatitude, locLongitute;
 	
 	private boolean hasRecord = false;
 	private String ftID = null;
 	private int mMoodIndex = 0;
 	private boolean updateMode = false;
 	private Calendar mCalendar = Calendar.getInstance();
 
 	private final int DATE_DIALOG = 1;      
     private final int TIME_DIALOG = 2;
 
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);  
         setContentView( R.layout.one_photo );
         
         findviews();
         
         locLatitude = -1;
         locLongitute = -1;
         
         mLocationManager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
         
         imgUtil = new ImageUtil();
         
 		Bundle extras = getIntent().getExtras();
         if( extras != null ) {
             mFileName = extras.getString( "FILE" );
             ftID = extras.getString("_ID");

            if( mFileName == null ) {
            	Toast.makeText( EditPage.this, getString( R.string.stringUnableToProcessDataNow ), Toast.LENGTH_LONG ).show();
            	finish();
            }
         }
         
         if( ftID != null ) {
         	updateMode = true;
         	
         	mLocationManager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
         	
         	Cursor c = ForTour.mDbHelper.ftStoryFetchByID( ftID );
             
             mFileName = c.getString( 0 );
             
             editTextOPStory.setText( c.getString( 1 ) );
             
             locName = c.getString( 2 );
             editTextOPLocation.setText( locName );
             
             hasRecord = ( c.getInt( 3 ) == 0 ) ? false : true; 
             
             ftStoryTime = c.getLong( 4 );
             mCalendar = Util.setCalendarInMSec( ftStoryTime );
             
             locLatitude   = c.getDouble( 5 );
             locLongitute  = c.getDouble( 6 );
             mMoodIndex = c.getInt( 7 );
             
             c.close();
             
             buttonOPSticker.setImageResource( ImageUtil.imageMoodFiles[ mMoodIndex ] );
         }
         
         mMediaFileName = mFileName.replace( ForTour.EXT_PHOTO, ForTour.EXT_RECORD );
         
         bmUriPath = Uri.fromFile( new File( Environment.getExternalStorageDirectory(),
 									   		ForTour.DIR_WORK + "/" + mFileName ) );
 
         /* NOTE: All the buttons MUST AFTER ALL INITIAL READY, due to UPDATE mode */
         setButtonListener();
         setDateTimePicker();
         
         try {
 			bm = MediaStore.Images.Media.getBitmap( this.getContentResolver(), bmUriPath );
 		} catch (FileNotFoundException e) {
 			Toast.makeText( EditPage.this, "File Not Found: " + e.getLocalizedMessage(), Toast.LENGTH_LONG ).show();
 		} catch (IOException e) {
 			Toast.makeText( EditPage.this, "IO Exception: " + e.getLocalizedMessage(), Toast.LENGTH_LONG ).show();
 		}
 
         imageViewOPImage.setImageBitmap( imgUtil.imageBorderMerge( getResources().getDrawable( R.drawable.photo_frame ), bm ) );
     }
 	
 	private void findviews(){
 		imageViewOPImage  	= (ImageView)   findViewById( R.id.imageViewOPImage );
         buttonOPOK    		= (ImageButton) findViewById( R.id.buttonOPOK );
         buttonOPRecord		= (ImageButton) findViewById( R.id.buttonOPRecord );
         buttonOPLocation	= (ImageButton) findViewById( R.id.buttonOPLocation );
         buttonOPSticker		= (ImageButton) findViewById( R.id.emotion_sticker );
         buttonOPHelp 		= (ImageButton) findViewById( R.id.ques);
         editTextOPStory		= (EditText) findViewById( R.id.editTextOPStory );
         editTextOPLocation	= (EditText) findViewById( R.id.editTextOPLocation );
         
 	}
 	
 	private void setButtonListener(){
 		buttonOPOK.setOnClickListener( new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if( !updateMode ) {
 					AlertDialog.Builder builder = new AlertDialog.Builder( EditPage.this );
 					builder.setTitle( R.string.stringSaveAndExit );
 					builder.setMessage( R.string.stringDoYouWantToSaveIt );
 					builder.setIcon( android.R.drawable.ic_dialog_info );
 
 					builder.setPositiveButton( android.R.string.yes, new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface arg0, int arg1) {
 							long rst = ForTour.mDbHelper.ftStoryAdd(	mFileName,
 																		editTextOPStory.getText().toString(),
 																		editTextOPLocation.getText().toString(),
 																		( ( hasRecord != false ) ? 1 : 0 ),
 																		locLatitude,
 																		locLongitute,
 																		mMoodIndex,
 																		Util.datetimeStringToMSec( ftStoryTimeDate, ftStoryTimeTime )
 																	);
 							
 							if( rst == -1 ) Toast.makeText( EditPage.this, getString( R.string.stringSaveStoryFail ), Toast.LENGTH_LONG ).show();
 							else {
 								try {
 									FileOutputStream thumbFile = new FileOutputStream(
 																	new File( Environment.getExternalStorageDirectory(),
 																			   ForTour.DIR_WORK + "/" + ForTour.DIR_THUMB + "/" + mFileName
 																	)
 																 );
 									Bitmap.createScaledBitmap( bm, imgUtil.THUMB_SIZE, imgUtil.THUMB_SIZE, true ).compress( Bitmap.CompressFormat.PNG, 90, thumbFile );
 									
 									if ( SetPreference.mApi != null ) checkSyncDropbox();
 								}
 								catch( FileNotFoundException e ) { }
 								
 								Toast.makeText( EditPage.this, getString( R.string.stringSaveStorySuccess ), Toast.LENGTH_LONG ).show();
 								finish();
 							}
 						}
 					} );
 					builder.setNegativeButton( android.R.string.no, null );
 					
 					builder.show();
 				}
 				else {
 					AlertDialog.Builder builder = new AlertDialog.Builder( EditPage.this );
 					builder.setTitle( R.string.stringUpdateAndExit );
 					builder.setMessage( R.string.stringDoYouWantToSaveIt );
 					
 					builder.setPositiveButton( android.R.string.yes, new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							boolean rst = ForTour.mDbHelper.ftStoryUpdByID(	ftID,
 																			mFileName,
 																			editTextOPStory.getText().toString(),
 																			editTextOPLocation.getText().toString(),
 																			( ( hasRecord != false ) ? 1 : 0 ),
 																			locLatitude,
 																			locLongitute,
 																			mMoodIndex,
 																			Util.datetimeStringToMSec( ftStoryTimeDate, ftStoryTimeTime )
 																			);
 
 							if( !rst ) Toast.makeText( EditPage.this, getString( R.string.stringUpdateStoryFail ), Toast.LENGTH_LONG ).show();
 							else {
 								Toast.makeText( EditPage.this, getString( R.string.stringUpdateStorySuccess ), Toast.LENGTH_LONG ).show();
 								
 								setResult( Activity.RESULT_OK );
 								
 								finish();
 								overridePendingTransition( android.R.anim.fade_in, android.R.anim.fade_out );
 							}
 						}
 					} );
 					builder.setNegativeButton( android.R.string.no, null );
 					
 					builder.show();
 				}
 			}
 		} );
 		
 		buttonOPRecord.setOnTouchListener( new OnTouchListener() {
 			File mMediaFileTemp, mMediaFile;
 			
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				switch( event.getAction() ) {
 					case MotionEvent.ACTION_DOWN:
 						mProgressDlg = ProgressDialog.show( EditPage.this,
 															getString( R.string.stringRecording ),
 															getString( R.string.stringReleaseButtonToStop ) );
 						mProgressDlg.setIcon( android.R.drawable.ic_btn_speak_now );
 						
 						try {
 							mMediaFile = new File( Environment.getExternalStorageDirectory(), ForTour.DIR_WORK + "/" + mMediaFileName );
 							mMediaFileTemp = new File( Environment.getExternalStorageDirectory(), ForTour.DIR_WORK + "/" + ForTour.DIR_TEMP + "/" + mMediaFileName );
 							
 							mMediaRecorder = new MediaRecorder();
 							mMediaRecorder.setAudioSource( MediaRecorder.AudioSource.MIC );
 							mMediaRecorder.setOutputFormat( MediaRecorder.OutputFormat.THREE_GPP );
 							mMediaRecorder.setAudioEncoder( MediaRecorder.AudioEncoder.AMR_NB );
 							mMediaRecorder.setOutputFile( mMediaFileTemp.getAbsolutePath() );
 							
 							mMediaRecorder.prepare();
 							mMediaRecorder.start();
 						}
 						catch( IllegalStateException e ) { }
 						catch( IOException e ) { }
 						break;
 					case MotionEvent.ACTION_UP:
 						mProgressDlg.dismiss();
 						
 						if( mMediaFileTemp != null ) {
 							mMediaRecorder.stop();
 							mMediaRecorder.release();
 							
 							AlertDialog.Builder builder = new AlertDialog.Builder( EditPage.this );
 							
 							android.content.DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
 								@Override
 								public void onClick(DialogInterface dialog, int which) {
 									if( mMediaFileTemp.renameTo( mMediaFile ) ) {
 										hasRecord = true;
 										Util.deleteFile( mMediaFileTemp );
 										Toast.makeText( EditPage.this, "Save media success.", Toast.LENGTH_LONG ).show();
 									}
 									else {
 										Toast.makeText( EditPage.this, "Save media fail", Toast.LENGTH_LONG ).show();
 									}
 								}
 							};
 							
 							android.content.DialogInterface.OnClickListener negtiveListener = new DialogInterface.OnClickListener() {
 								@Override
 								public void onClick(DialogInterface dialog, int which) {
 									Util.deleteFile( mMediaFileTemp );
 									Toast.makeText( EditPage.this, "Discard save", Toast.LENGTH_LONG ).show();
 								}
 							};
 							
 							if( !updateMode || ( updateMode && !hasRecord ) ) {
 								builder.setTitle( getString( R.string.stringSave ) + " " + getString( R.string.stringStoryMedia ) );
 								builder.setMessage( getString( R.string.stringNote ) + ": " + getString( R.string.stringHoldDownButtonToRecord ) );
 								
 								builder.setPositiveButton( android.R.string.yes, positiveListener );
 								builder.setNegativeButton( android.R.string.no, negtiveListener );
 								
 								builder.show();
 							}
 							else if( updateMode && hasRecord ) {
 								builder.setTitle( getString( R.string.stringReplace ) + " " + getString( R.string.stringStoryMedia ) );
 								builder.setMessage( getString( R.string.stringDoYouWantToReplaceIt ) + "\n\n" + 
 													getString( R.string.stringNote ) + ": " + getString( R.string.stringHoldDownButtonToRecord ) );
 								
 								builder.setPositiveButton( android.R.string.yes, positiveListener );
 								builder.setNegativeButton( android.R.string.no, negtiveListener );
 								
 								builder.show();
 							}
 						}
 						else {
 							Toast.makeText( EditPage.this, "Save media fail", Toast.LENGTH_LONG ).show();
 						}
 						break;
 					default:
 						break;
 				}
 				
 				return true;
 			}
 		} );
 		
 		buttonOPLocation.setOnClickListener( new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				if( mLocationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ||
 					mLocationManager.isProviderEnabled( LocationManager.NETWORK_PROVIDER ) ) {
 					
 					Intent intent = new Intent();
 					
 					if( updateMode && locLatitude != -1 && locLongitute != -1 ) {
 						Bundle b = new Bundle();
 						
 						b.putString( LocationMap.KEY_LATITUDE, String.valueOf( (int) ( locLatitude * 1E6 ) ) );
 						b.putString( LocationMap.KEY_LONGITUDE, String.valueOf( (int) ( locLongitute * 1E6 ) ) );
 						b.putString( LocationMap.KEY_LOCNAME, locName );
 						b.putString( LocationMap.KEY_UPDMODE, "" );
 						
 						intent.putExtras( b );
 					}
 					intent.setClass( EditPage.this, LocationMap.class );
 					startActivityForResult( intent, ForTour.LOCATION_MAP_PICK );
 				}
 				else {
 					AlertDialog.Builder builder = new AlertDialog.Builder( EditPage.this );
 					builder.setTitle( R.string.stringEnableLocationServices );
 					builder.setMessage( R.string.stringDoYouWantToEnableIt );
 					builder.setPositiveButton( android.R.string.yes, new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface arg0, int arg1) {
 							startActivity( new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS ) );
 						}
 					} );
 					builder.setNegativeButton( android.R.string.no, new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface arg0, int arg1) {
 							Toast.makeText( EditPage.this, getString( R.string.stringServiceCanNotBeUsedNow ), Toast.LENGTH_LONG ).show();
 						}
 					} );
 					builder.show();
 				}
 			}
 		} );
 		
 		buttonOPSticker.setOnClickListener( new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				popdialogue();
 			}
 		} );
 		
 		buttonOPHelp.setOnClickListener(new Button.OnClickListener(){
         	public void onClick(View arg0){
             	Intent intent = new Intent();
             	intent.setClass( EditPage.this, EditPageInfo.class );
             	startActivity( intent );
             	overridePendingTransition( android.R.anim.fade_in, android.R.anim.fade_out );
         	}
         });
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		switch( requestCode ) {
 			case ForTour.LOCATION_MAP_PICK:
 				if( resultCode == Activity.RESULT_OK ) {
 					Bundle extras = data.getExtras();
 			        if( extras != null ) {
 			        	String locLati = extras.getString( LocationMap.KEY_LATITUDE );
 			        	String locLong = extras.getString( LocationMap.KEY_LONGITUDE );
 			        	String locName = extras.getString( LocationMap.KEY_LOCNAME );
 
 			        	if( locName != null ) {
 			        		editTextOPLocation.setText( locName );
 			        	}
 			        	
 			        	try {
 				        	if( locLati != null ) locLatitude = Double.parseDouble( locLati ) / 1E6;
 				        	if( locLong != null ) locLongitute = Double.parseDouble( locLong ) / 1E6;
 			        	}
 			        	catch( NumberFormatException nfe ) {}
 			        }
 			        
 			        if( locLatitude == -1 || locLongitute == -1 ) {
 			        	locLatitude = -1;
 			        	locLongitute = -1;
 			        }
 				}
 				break;
 
 			case ForTour.PASS_ONE_PHOTO:
 				if( resultCode == Activity.RESULT_OK ) {
 					Bundle extras = data.getExtras();
 			        if( extras != null ) {
 			            mFileName = extras.getString( "FILE" );
 			        }
 				}
 				break;
 			default:
 				break;
 		}
 	}
 	
 	private void discardStory() {
 		Util.deleteFile( new File( Environment.getExternalStorageDirectory(),
 				   			    ForTour.DIR_WORK + "/" + ForTour.DIR_THUMB + "/" + mFileName ) );
 		Util.deleteFile( new File( Environment.getExternalStorageDirectory(),
    			    				ForTour.DIR_WORK + "/" + mFileName ) );
 		Util.deleteFile( new File( Environment.getExternalStorageDirectory(),
 								ForTour.DIR_WORK + "/" + mMediaFileName ) );
 	}
 	
 	@Override
 	public void onBackPressed() {
 		AlertDialog.Builder builder = new AlertDialog.Builder( EditPage.this );
 		builder.setIcon( android.R.drawable.ic_dialog_info );
 		builder.setTitle( R.string.stringDiscardAndExit );
 		builder.setMessage( R.string.stringDoYouWantToDiscardIt );
 		builder.setNegativeButton( android.R.string.no, null );
 		builder.setPositiveButton( android.R.string.yes, new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface arg0, int arg1) {
 				if( !updateMode ) discardStory();
 				finish();
 				overridePendingTransition( android.R.anim.fade_in, android.R.anim.fade_out );
 			}
 		} );
 		builder.show();
 	}
 	
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		
 		ImageUtil.freeBitmap( bm );
 		
 		try {
 			imgUtil.finalize();
 		}
 		catch( Throwable e ) { }
 	}
 	
 	private void setDateTimePicker(){
     	View.OnClickListener dateBtnListener =   
                 new BtnOnClickListener(DATE_DIALOG);  
     	View.OnClickListener timeBtnListener =   
                 new BtnOnClickListener(TIME_DIALOG);      
     	editTextOPDate =(EditText) findViewById(R.id.editTextOPDate);
     	editTextOPTime =(EditText) findViewById(R.id.editTextOPTime);
     	
 		ftStoryTimeDate = Util.sdfDate.format( mCalendar.getTime() );
 		ftStoryTimeTime = Util.sdfTime.format( mCalendar.getTime() );
     	
     	editTextOPDate.setText( ftStoryTimeDate );
     	editTextOPTime.setText( ftStoryTimeTime );
     	
     	editTextOPDate.setOnClickListener(dateBtnListener);
     	editTextOPTime.setOnClickListener(timeBtnListener);
     }
     
     protected Dialog onCreateDialog(int id) {  
         Dialog dialog = null;
         
         switch(id) {  
             case DATE_DIALOG:  
                 DatePickerDialog.OnDateSetListener dateListener =   
                     new DatePickerDialog.OnDateSetListener() {  
                         public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {  
                             //Calendar starts from month 0, so add 1 to month
                         	ftStoryTimeDate = String.format( "%d/%02d/%02d", year, month+1, dayOfMonth);
                             editTextOPDate.setText( ftStoryTimeDate );
                         }  
                     };  
                 dialog = new DatePickerDialog(	this,  
 						                        dateListener,  
 						                        mCalendar.get(Calendar.YEAR),  
 						                        mCalendar.get(Calendar.MONTH),  
 						                        mCalendar.get(Calendar.DAY_OF_MONTH) );  
                 break;  
             case TIME_DIALOG:  
                 TimePickerDialog.OnTimeSetListener timeListener =   
                     new TimePickerDialog.OnTimeSetListener() {  
                           
                         public void onTimeSet(TimePicker timerPicker, int hourOfDay, int minute) {
                         	ftStoryTimeTime = String.format( "%02d:%02d", hourOfDay, minute);
                             editTextOPTime.setText( ftStoryTimeTime );
                         }  
                     };  
                     dialog = new TimePickerDialog(	this,
                     								timeListener,  
                     								mCalendar.get(Calendar.HOUR_OF_DAY),  
                     								mCalendar.get(Calendar.MINUTE),  
 						                            false );
                 break;  
             default:  
                 break;  
         }  
         return dialog;  
     }  
 
     private class BtnOnClickListener implements View.OnClickListener {  
           
         private int dialogId = 0;   //'0'->no dialog
   
         public BtnOnClickListener(int dialogId) {  
             this.dialogId = dialogId;  
         }  
         public void onClick(View view) {  
             showDialog(dialogId);  
         }  
           
     }
     
     private GridView gridView; 
 	  
 	ProgressDialog mydialog;
 
 	private void popdialogue(){
 		final AlertDialog builder = new AlertDialog.Builder( EditPage.this ).create();
 		
 	    LayoutInflater inflater = LayoutInflater.from(this);  
 	    View selectView = inflater.inflate(R.layout.picture_dialog,(ViewGroup) findViewById(R.id.layout_root));
 	    gridView = (GridView) selectView.findViewById(R.id.gridview);  
 	    
 	    PictureAdapter adapter= new PictureAdapter( ImageUtil.imageMoodTitles , ImageUtil.imageMoodFiles, this);	       
 	    gridView.setAdapter(adapter);  
 	    gridView.setOnItemClickListener( new OnItemClickListener(){
 	    	@Override
 	    	public void onItemClick(AdapterView<?> parent, View v, int position, long id){
 	    		mMoodIndex = position;
 	    		
 				ImageButton mMoodButton = (ImageButton) findViewById( R.id.emotion_sticker );
 				mMoodButton.setImageResource( ImageUtil.imageMoodFiles[ position ] );
 	    		
 	    		if( builder != null ) builder.dismiss();
 	    	}
 	    });
 	    
 	    builder.setCancelable(false);
 	    builder.setTitle( R.string.stringHowAboutYourMood );  
 	    builder.setView(selectView);
 	    builder.setCancelable(true);
 	    builder.show(); 
 	}
 	
 	private void checkSyncDropbox(){
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		// then you use
 		Boolean linked = prefs.getBoolean(SetPreference.SYNC_DROPBOX, false);
 		if (linked) {
 			AndroidAuthSession session = SetPreference.mApi.getSession();
 
 			// The next part must be inserted in the onResume() method of the
 			// activity from which session.startAuthentication() was called, so
 			// that Dropbox authentication completes properly.
 			if (session.authenticationSuccessful()) {
 				try {
 					// Mandatory call to complete the auth
 					session.finishAuthentication();
                 
 					SetPreference.uploadDB(mFileName, EditPage.this);
 				} catch (IllegalStateException e) {
 					showToast("Couldn't authenticate with Dropbox:" + e.getLocalizedMessage());
 					Log.i(TAG, "Error authenticating", e);
 				}
 			}
 		}
 	}
 	
 	private void showToast(String msg) {
         Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
         error.show();
     }
 }
