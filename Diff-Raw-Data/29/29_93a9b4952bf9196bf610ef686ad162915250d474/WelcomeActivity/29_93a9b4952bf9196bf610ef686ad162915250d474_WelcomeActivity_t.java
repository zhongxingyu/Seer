 package com.tejus.shavedoggery.ui;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.StringTokenizer;
 import java.util.concurrent.ExecutionException;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.tejus.shavedoggery.R;
 import com.tejus.shavedoggery.core.Definitions;
 import com.tejus.shavedoggery.core.ShaveService;
 import com.tejus.shavedoggery.util.Logger;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.graphics.Typeface;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.IBinder;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class WelcomeActivity extends ListActivity {
 
     String sdcardDir = Environment.getExternalStorageDirectory().toString();
     ArrayList<String> rootListing = new ArrayList<String>();
     String mCurrentDirectory;
     ArrayList<String> mFiles = new ArrayList<String>();
     HashMap<String, String> mFileLengthMap = new HashMap<String, String>();
 
     Button backButton, homeButton, refreshButton;
     Context mContext;
 
     ServiceConnection mConnection;
     ShaveService mShaveService;
     BroadcastReceiver mShaveReceiver = new ServiceIntentReceiver();
     Handler handler = new Handler();
 
     @Override
     public void onCreate( Bundle args ) {
         super.onCreate( args );
         initShaveServiceStuff();
         mContext = this;
        Definitions.OUR_USERNAME = "notashavedog";
         setContentView( R.layout.welcome_layout );
         initReceiver();
 
         refreshButton = ( Button ) findViewById( R.id.refresh );
 
         refreshButton.setOnClickListener( new View.OnClickListener() {
             @Override
             public void onClick( View v ) {
                 startActivity( ( new Intent().setClass( mContext, WelcomeActivity.class ).setFlags( Intent.FLAG_ACTIVITY_NEW_TASK ).putExtra( "directory_path",
                         mCurrentDirectory ) ) );
             }
         } );
 
         handler.postDelayed( new Runnable() {
             @Override
             public void run() {
                 sendBootup();
             }
         }, 2000 );
         Bundle bundle = getIntent().getExtras();
         if ( bundle != null ) {
             mCurrentDirectory = ( String ) bundle.get( "directory_path" );
             Logger.debug( "WelcomeActivity.onCreate: mCurrentDirectory = " + mCurrentDirectory );
         } else {
             mCurrentDirectory = "";
         }
         try {
             mFiles = new SdCardLister( mCurrentDirectory ).execute().get();
             Logger.info( "oncreate: mFiles = " + mFiles.toString() );
             MySimpleArrayAdapter adapter = new MySimpleArrayAdapter( this, mFiles );
             setListAdapter( adapter );
 
         } catch ( InterruptedException e ) {
             e.printStackTrace();
         } catch ( ExecutionException e ) {
             e.printStackTrace();
         }
 
     }
 
     @Override
     public boolean onCreateOptionsMenu( Menu menu ) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate( R.menu.shave_menu, menu );
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected( MenuItem item ) {
         switch ( item.getItemId() ) {
             case R.id.test_api:
                 this.testApi();
                 return true;
 
             default:
                 return super.onOptionsItemSelected( item );
         }
     }
 
     private class ServiceIntentReceiver extends BroadcastReceiver {
 
         @Override
         public void onReceive( Context context, Intent intent ) {
             String action = intent.getAction();
             Logger.info( "WelcomeActivity.ServiceIntentReceiver: action received = " + action );
             if ( action.equals( Definitions.INTENT_RECIPIENT_NOT_FOUND ) ) {
                 showRecipientNotFoundToast( intent );
             }
         }
 
         private void showRecipientNotFoundToast( Intent intent ) {
             String unknownRecipient = ( intent.getStringExtra( "unknown_recipient" ) != null ) ? intent.getStringExtra( "unknown_recipient" ) : null;
             Toast.makeText( mContext, getResources().getString( R.string.unknown_recipient ) + " " + unknownRecipient, Toast.LENGTH_LONG ).show();
 
         }
 
     }
 
     private void testApi() {
 
         JSONObject data = new JSONObject();
         try {
             data.put( "username", Definitions.OUR_USERNAME );
             data.put( "packet_type", "bootup" );
             mShaveService.sendMessage( data );
         } catch ( JSONException e ) {
             e.printStackTrace();
         }
 
     }
 
     void initShaveServiceStuff() {
         mConnection = new ServiceConnection() {
             @Override
             public void onServiceDisconnected( ComponentName className ) {
                 mShaveService = null;
             }
 
             @Override
             public void onServiceConnected( ComponentName name, IBinder service ) {
                 mShaveService = ( ( ShaveService.ShaveBinder ) service ).getService();
             }
         };
 
         doBindService();
         startService( new Intent().setClass( this, ShaveService.class ) );
     }
 
     void doBindService() {
         bindService( new Intent( this, ShaveService.class ), mConnection, Context.BIND_AUTO_CREATE );
     }
 
     private void initReceiver() {
         IntentFilter filter = new IntentFilter();
         filter.addAction( Definitions.INTENT_RECIPIENT_NOT_FOUND );
         registerReceiver( mShaveReceiver, filter );
     }
 
     private void sendBootup() {
         JSONObject data = new JSONObject();
         try {
             data.put( "username", Definitions.OUR_USERNAME );
             data.put( "packet_type", "bootup" );
             mShaveService.sendMessage( data );
         } catch ( JSONException e ) {
             e.printStackTrace();
         }
     }
 
     public class MySimpleArrayAdapter extends ArrayAdapter<String> {
         private final Activity context;
         private final ArrayList<String> fileList;
 
         public MySimpleArrayAdapter( Activity context, ArrayList<String> fileList ) {
             super( context, R.layout.file_row, fileList );
             this.context = context;
             this.fileList = fileList;
         }
 
         @Override
         public View getView( int position, View convertView, ViewGroup parent ) {
             String name = null;
 
             LayoutInflater inflater = context.getLayoutInflater();
             View rowView = inflater.inflate( R.layout.file_row, null, true );
             TextView itemName = ( TextView ) rowView.findViewById( R.id.item_name );
             TextView itemSize = ( TextView ) rowView.findViewById( R.id.item_size );
             if ( fileList.size() > 0 ) {
                 if ( fileList.get( position ).startsWith( "#" ) ) {
                     Logger.info( "WelcomeActivity.getView(): setting bold for position = " + position );
                     itemName.setTypeface( null, Typeface.BOLD );
                     itemSize.setVisibility( View.GONE );
                     name = getFileNameTrivial( fileList.get( position ) );
                 } else {
                     Log.d( "XXXX", "setting italics for position = " + position );
                     itemName.setTypeface( null, Typeface.ITALIC );
                     if ( mFileLengthMap.get( mFiles.get( position ) ) != null ) {
                         itemSize.setText( saneFileSizeRepresentation( mFileLengthMap.get( mFiles.get( position ) ) ) );
                     }
                     name = getFileNameTrivial( fileList.get( position ) );
                 }
                 itemName.setText( name );
             }
             return rowView;
         }
     }
 
     @Override
     protected void onListItemClick( ListView l, View v, int position, long id ) {
 
         Logger.debug( "WelcomeActivity.onListItemClick: item clicked = " + mFiles.get( position ) );
         String filePath = mFiles.get( position ); // if it's a file, fire up the
         if ( isNotADirectory( filePath ) ) {
             Logger.info( " gonna  sendRequestAlert" );
             sendRequestAlert( filePath.replace( "#", "" ) );
         } else {
             startActivity( ( new Intent().setClass( this, WelcomeActivity.class ).setFlags( Intent.FLAG_ACTIVITY_NEW_TASK ).putExtra( "directory_path",
                     filePath.replace( "#", "" ) ) ) );
         }
 
     }
 
     private void sendRequestAlert( String filePath ) {
         final EditText input = new EditText( this );
         final String finalFilePath = filePath;
         new AlertDialog.Builder( mContext ).setIcon( R.drawable.iconshave )
                 .setTitle( getFileNameTrivial( filePath ) )
                 .setMessage( R.string.send_question )
                 .setView( input )
                 .setPositiveButton( R.string.send, new DialogInterface.OnClickListener() {
 
                     @Override
                     public void onClick( DialogInterface arg0, int arg1 ) {
                         String recipient = input.getText().toString().toLowerCase();
                         JSONObject data = new JSONObject();
                         try {
                             data.put( "packet_type", "file_push_req" );
                             data.put( "file_size", getFileSize( finalFilePath ) );
                             data.put( "file_name", finalFilePath );
                             data.put( "to", recipient );
                             data.put( "username", Definitions.OUR_USERNAME );
 
                             mShaveService.sendMessage( data );
                         } catch ( JSONException e ) {
                             e.printStackTrace();
                         }
                     }
 
                 } )
                 .create()
                 .show();
     }
 
     private class SdCardLister extends AsyncTask<Void, Void, ArrayList<String>> {
         String directoryName;
 
         public SdCardLister( String directoryName ) {
             this.directoryName = directoryName;
         }
 
         @Override
         protected ArrayList<String> doInBackground( Void... params ) {
             ArrayList<String> files = new ArrayList<String>();
             File file[] = null;
             if ( directoryName.length() > 0 ) {
                 Logger.info( "SdCardLister: searching directory - " + directoryName );
                 file = new File( directoryName ).listFiles();
             } else {
                 Logger.info( "SdCardLister: searching directory - " + sdcardDir );
                 file = new File( sdcardDir ).listFiles();
             }
             // if the folder isn't empty:
             if ( file != null ) {
                 for ( File iFile : file ) {
                     if ( iFile.isDirectory() ) {
                         files.add( "#" + iFile.getAbsolutePath() );
                     } else {
                         files.add( iFile.getAbsolutePath() );
                         mFileLengthMap.put( iFile.getAbsolutePath(), String.valueOf( iFile.length() ) );
                         Logger.info( "mFileLengthMap here = " + mFileLengthMap.toString() );
                     }
                 }
             } else {
                 // the folder's empty
                 files.add( "" );
             }
             return files;
         }
 
     }
 
     private String getFileNameTrivial( String filePath ) {
         return filePath.substring( filePath.lastIndexOf( "/" ) + 1 );
     }
 
     String saneFileSizeRepresentation( String fileSize ) {
         Log.d( "XXXX", "saneFileSizeRepresentation called for - " + fileSize );
         fileSize = fileSize.replace( "]", "" ).replace( "[", "" );
         if ( fileSize.length() < 4 ) {
             return fileSize + " B";
         } else if ( fileSize.length() < 7 ) {
             return String.valueOf( ( Long.parseLong( fileSize ) / 1000 ) ) + " KB";
         } else if ( fileSize.length() < 10 ) {
             Log.d( "XXXX", "for MB: " + ( Long.parseLong( fileSize ) / 1000000 ) );
             return String.valueOf( ( Long.parseLong( fileSize ) / ( long ) 1000000 ) ) + " MB";
         } else if ( fileSize.length() < 13 ) {
             return String.valueOf( ( Long.parseLong( fileSize ) / 1000000000 ) ) + " GB";
         } else {
             return null;
         }
 
     }
 
     private boolean isNotADirectory( String filePath ) {
         return !( filePath.trim().startsWith( "#" ) );
     }
 
     private long getFileSize( String filePath ) {
         File tempFile = new File( filePath );
         if ( tempFile.exists() ) {
             return tempFile.length();
         } else {
             return -1;
         }
 
     }
 
 }
