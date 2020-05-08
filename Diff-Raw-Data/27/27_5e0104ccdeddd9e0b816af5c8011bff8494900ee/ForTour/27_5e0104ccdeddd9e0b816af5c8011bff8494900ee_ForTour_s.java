 package tw.edu.ntu.fortour;
 
 import java.io.File;
 
 import com.droid4you.util.cropimage.CropImage;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ActivityNotFoundException;
 import android.content.ContentResolver;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.Toast;
 
 public class ForTour extends Activity {
     private ImageButton add, view, set;
     private Uri mImageCaptureUri, mImageDiaryUri;
     private String mFileName;
     
     protected static DbAdapter mDbHelper;
 
     private static final int MENU_HELP = Menu.FIRST;
 
 	private static final int PICK_FROM_CAMERA    = 0x100001;
 	private static final int CROP_FROM_CAMERA    = 0x100002;
 	private static final int PICK_FROM_FILE      = 0x100003;
	protected static final int LOCATION_MAP_PICK = 0x100004;
	protected static final int EDIT_ONE_PHOTO    = 0x100005;
	protected static final int PASS_ONE_PHOTO    = 0x100005;
 	
 	protected static final String EXT_PHOTO  = ".png";
 	protected static final String EXT_RECORD = ".3gp";
 	
 	protected static final String DIR_WORK   = "ForTour";
 	protected static final String DIR_TEMP   = ".tmp";
 	protected static final String DIR_THUMB  = ".thumbs";
 	
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         mDbHelper = new DbAdapter( this );
         mDbHelper.open();
         
         // Initial default filename
         mFileName = Util.getFileName( EXT_PHOTO );
         
         checkWorkDirs();
         
         findviews();
         setCamera();
         setButtonListener();
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	super.onCreateOptionsMenu(menu);
     	
     	menu.add( 0, MENU_HELP, 0, getString( R.string.stringHelp ) ).setIcon( android.R.drawable.ic_menu_help );
     	
     	return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	super.onOptionsItemSelected(item);
     	
     	switch( item.getItemId() ) {
 		case MENU_HELP:
 			Intent intent = new Intent();
         	intent.setClass( ForTour.this, ForTourInfo.class );
         	startActivity( intent );
         	overridePendingTransition( android.R.anim.fade_in, android.R.anim.fade_out );
 			break;
 		default:
 			break;
 		}
     	
     	return true;
     }
     
     private boolean checkExternalAvaliable() {
     	String state = Environment.getExternalStorageState();
     	
     	if( state.equals( Environment.MEDIA_MOUNTED ) ) return true;
     	else return false;
     }
     
     private boolean checkDirExistOrCreate( boolean isExternal, final String dirPath ) {
     	File dir;
     	
     	if( isExternal ) dir = new File( Environment.getExternalStorageDirectory(), dirPath );
     	else dir = new File( dirPath );
     	
     	if( !dir.exists() ) {
     		if( !dir.mkdir() ) {
     			return false;
     		}
     	}
     	
     	return true;
     }
     
     private void checkWorkDirs() {
     	if( checkExternalAvaliable() ) {
 	    	if( !checkDirExistOrCreate( true, DIR_WORK + "/" ) ||
 	    		!checkDirExistOrCreate( true, DIR_WORK + "/" + DIR_TEMP + "/" ) ||
 	    		!checkDirExistOrCreate( true, DIR_WORK + "/" + DIR_THUMB + "/" ) ) {
 	    		
 	    		Toast.makeText( this, "Working Directory Creation Fail", Toast.LENGTH_LONG ).show();
 	    	}
     	}
     	else {
     		Toast.makeText( this, "External Storage not Avaliable Now", Toast.LENGTH_LONG ).show();
     	}
     }
     
     private void findviews(){
     	add = (ImageButton) findViewById(R.id.button1);
     	view = (ImageButton) findViewById(R.id.button2);
     	set = (ImageButton) findViewById(R.id.button3);
     }
     
     private void setCamera(){
     	final String [] items			= new String [] {"Take from camera", "Select from gallery"};				
 		ArrayAdapter<String> adapter	= new ArrayAdapter<String> (this, android.R.layout.select_dialog_item,items);
 		AlertDialog.Builder builder		= new AlertDialog.Builder(this);
 		
 		builder.setTitle("Select Image");
 		builder.setAdapter( adapter, new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick( DialogInterface dialog, int item ) {
 				// The real filename
 				mFileName = Util.getFileName( EXT_PHOTO );
 				
 				if (item == 0) {
 					//pick from camera
 					Intent intent 	 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);					
 					mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
 									   DIR_WORK + "/" + DIR_TEMP + "/" + mFileName ));
 					intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
 					
 					try {
 						intent.putExtra("return-data", true);					
 						startActivityForResult(intent, PICK_FROM_CAMERA);
 					} catch (ActivityNotFoundException e) {
 						e.printStackTrace();
 					}
 				} else { 
 					//pick from file
 					Intent intent = new Intent();					
 	                intent.setType("image/*");
 	                intent.setAction(Intent.ACTION_GET_CONTENT);	                
 	                startActivityForResult(Intent.createChooser(intent, "Complete Action With"), PICK_FROM_FILE);
 				}
 			}
 		} );
 		
 		final AlertDialog dialog = builder.create();
 		add.setOnClickListener(new View.OnClickListener() {	
 			@Override
 			public void onClick(View v) {
 				dialog.show();
 			}
 		});
     }
     
     private void setButtonListener(){
         view.setOnClickListener(new Button.OnClickListener(){
         	@Override
 			public void onClick(View arg0){
         		Intent intent = new Intent();
         		intent.setClass( ForTour.this, ListPage.class );        		
         		startActivity( intent );
         	}
         });
         set.setOnClickListener(new Button.OnClickListener(){
         	@Override
 			public void onClick(View arg0){
         		Intent intent = new Intent();
         		intent.setClass( ForTour.this, SetPreference.class );
         		startActivity( intent );
         	}
         });
     }
     
     @Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 	    if (resultCode != RESULT_OK) return;
 	   
 	    switch (requestCode) {
 		    case PICK_FROM_CAMERA:
		    	doCrop();    	
 		    	break;
 		    	
 		    case PICK_FROM_FILE:
 		    	mImageCaptureUri = data.getData();
 
 				if( mImageCaptureUri.getScheme().equals( ContentResolver.SCHEME_CONTENT ) ) {
 			    	try {
 			            Cursor cursor = managedQuery( data.getData(), new String[] { MediaStore.Images.Media.DATA }, null, null, null );	     
 			            int column_index = cursor.getColumnIndexOrThrow( MediaStore.Images.Media.DATA );
 			            cursor.moveToFirst();
 			            mImageCaptureUri = Uri.parse( cursor.getString( column_index ) );
 		
				    	doCrop();
 			    	}
 			    	catch( Exception e ) {
 			    		Toast.makeText( ForTour.this, "Exception: " + e.getLocalizedMessage(), Toast.LENGTH_LONG ).show();
 			    	}
 				}
 				else if ( mImageCaptureUri.getScheme().equals( ContentResolver.SCHEME_FILE ) ) {
					doCrop();
 				}
 				else {
 					Toast.makeText( ForTour.this, "Error when retrieve data.", Toast.LENGTH_LONG ).show();
 				}
 		    	break;
 	    
 		    case CROP_FROM_CAMERA:
 		        Bundle extras = data.getExtras();
 		        if (extras != null) {	 					
 					//open the editPage
 					Intent intent1 = new Intent();
 					intent1.setClass(ForTour.this, EditPage.class);
 					Bundle bundle = new Bundle();
 					bundle.putString( "FILE", mFileName );
 					intent1.putExtras(bundle);
 					startActivityForResult(intent1, PASS_ONE_PHOTO);  
 		        }
 
 		        // Delete the template photo
		        Util.deleteFile( new File( mImageCaptureUri.getPath() ) );
 		        break;
 	    }
 	}
     
    private void doCrop() {
 		Intent intent = new Intent(this, CropImage.class);
 		intent.setType("image/*");
 
        	mImageDiaryUri = Uri.fromFile( new File( Environment.getExternalStorageDirectory(),
        											  DIR_WORK + "/" + mFileName ) );
 
        	intent.putExtra("image-path", mImageCaptureUri.getPath());
         intent.putExtra("outputX", ImageUtil.imageInnerWidth );
         intent.putExtra("outputY", ImageUtil.imageInnerHeight );
         intent.putExtra("aspectX", 1);
         intent.putExtra("aspectY", 1);
         intent.putExtra("scale", true);
         intent.putExtra("return-data", false);
         intent.putExtra( MediaStore.EXTRA_OUTPUT, mImageDiaryUri.getPath() );
         intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
             
         Intent i = new Intent(intent);
        startActivityForResult(i, CROP_FROM_CAMERA);
 	}
     
     @Override
     protected void onDestroy() {
     	super.onDestroy();
     	
     	if( mDbHelper != null ) mDbHelper.close();
     }
 }
