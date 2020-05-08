 package cc.omusic.FingerprintDemoVer2;
 
 import java.io.File;
 import java.util.ArrayList;
 import cc.omusic.FingerprintDecodeJin.FingerprintWraper;
 import android.net.Uri;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckedTextView;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class MainActivity extends Activity {
 	private ProgressBar  autoProgressBar;
 	private Button testButton;
 	private Button playButton;
 	private TextView infoText;
 	private ListView musicList;
 	
 	private ArrayList<String> recordFiles;
 	private ArrayAdapter<String> adapter;
 	
 	
 	
 	private SDRecord SDRecorder;
 	private File RecordMusicDir;
 	private File SelectedFile;
 	
 	private String TAG = "main";
 	private long fingerprint_time=0;
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		autoProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
 		autoProgressBar.setIndeterminate(false);
 		playButton = (Button) findViewById( R.id.play_button);
 		testButton = (Button) findViewById( R.id.test_button);
 		infoText = (TextView) findViewById( R.id.info_text);
 		musicList = (ListView) findViewById( R.id.music_List);
 		
 		SelectedFile = null;
 		SDRecorder = new SDRecord();
 		RecordMusicDir = SDRecorder.createSDDir( "omusic" );
 		//list all  media (.amr) files
 		getRecordFiles();
 		// use a ArrarAdapter to contain a ListView
 		adapter = new ArrayAdapter<String>(this, R.layout.my_simple_list_item, recordFiles );
 		musicList.setAdapter(adapter);
 		
 		testButton.setOnClickListener( new testButtonListener() );
 		playButton.setOnClickListener( new playButtonListener() );
 		musicList.setOnItemClickListener(new musicListClickListener());
 	
 	}
 
 	public class playButtonListener  implements OnClickListener{
 		@Override
 		public void onClick(View v) {
 			// TODO Auto-generated method stub
 			Log.d(TAG,"play a music");
 			try 
 			{
 				//start audio record thread to write data to sd file.
 				//new Thread( new AutoTestThread()).start();
 				playMusicFile( SelectedFile );
 
 			}catch( Exception e ){
 				e.printStackTrace();
 			}
 		
 		}
 	}
 	
 
 	public boolean playMusicFile( File musicfile ){
 		if( musicfile == null )
 			return false;
 		if( !musicfile.exists())
 			return false;
 		Intent intent = new Intent();
 		intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK);
 		intent.setAction( android.content.Intent.ACTION_VIEW);
 		String type = SDRecorder.getMIMEType( musicfile );
 		intent.setDataAndType(Uri.fromFile( musicfile ), type);
 		startActivity( intent );	
 		return true;
 	}
 	
 	
 	public class testButtonListener implements OnClickListener{
 		@Override
 		public void onClick(View v) {
 			// TODO Auto-generated method stub
 			Log.d(TAG,"start test");
 			if( SelectedFile == null ){
 				infoText.setText( "please select a corect music file ");
 			}
 			else{
 				//read_music( SelectedFile );
 				calculateFP( SelectedFile );
 					
 			}	
 		}
 		
 		
 	}
 	
 	
 	
 	public void calculateFP(  File music_file){
 		Log.d(TAG,"ready to generate fingerprint");
 		fingerprint_time = System.currentTimeMillis();
 		FingerprintWraper fp = new FingerprintWraper();
 
 		String music_file_path = music_file.getAbsolutePath();
 		infoText.setText( "calculate music file's fingerprint:" + music_file_path);
 		Log.d(TAG, "calculate music file's fingerprint:" + music_file_path);
 		byte []fp_byte = new byte[424];
 		fp_byte = fp.generate( music_file_path);	
 		fingerprint_time = System.currentTimeMillis() - fingerprint_time;
 		Log.i(TAG,"fingerprint generate time: " + fingerprint_time + "ms" );
 		
 		if( fp_byte == null ){
 			infoText.setText( "fingerprint is null ");
 		}else{
 			/*
 			infoText.setText("file name: "+  music_file.getName()
 							+ " \nfingerprint : \n"
 							+ "cost time:"+ fingerprint_time + "ms \n"
 							+ "fingerprint: \n" + byteArrayToHexString( fp_byte ) );
 			*/
 			infoText.setText("file name: "+  music_file.getName()
 					+ " \nfingerprint : \n"
 					+ "cost time:"+ fingerprint_time + "ms \n"
 					+ "file name: " + music_file.getName() + "\n"
 					+ "fp_length: " + byteArray4ToInt(fp_byte, 2)+ "\n"
 					+ "fp_avg_fit: " + byteArray2ToShort(fp_byte,6 )+ "\n"
 					+ "fp_avg_dom: " + byteArray2ToShort(fp_byte,8 ) );
 			Log.d(TAG, "file name: " + music_file.getName());
 			Log.d(TAG,"fp_str: " + byteArrayToHexString(fp_byte));
 			
 			Log.d(TAG, "fp_version: " + byteArray2ToShort(fp_byte,0 ) );
			Log.d(TAG, "fp_length: " + byteArray4ToInt(fp_byte, 2));
 			Log.d(TAG, "fp_avg_fit: " + byteArray2ToShort(fp_byte,6 ));
 			Log.d(TAG, "fp_avg_dom: " + byteArray2ToShort(fp_byte,8 ) );
 			
 			byte[] fp_fit = new byte[348];
 			for( int i=10; i<358; i++ ){
 				fp_fit[i-10] = fp_byte[i];
 			}
 			Log.d(TAG, "fp_fit:" + byteArrayToHexString( fp_fit));
 			
 			byte[] fp_dom = new byte[66];
 			for( int i =358; i<424; i++){
 				fp_dom[i-358] = fp_byte[i];
 			}
 			Log.d(TAG, "fp_dom:" + byteArrayToHexString( fp_dom));
 		}	
 	}
 	
 
 	private  int byteArray4ToInt(byte[] byteValue, int index){   
         if(byteValue.length   <   4)   
            return   0;   
         int   intValue =   0;
         intValue   =   byteValue[index+ 3]; 
         intValue   =   (intValue   <<  8)   +   byteValue[index+2];   
         intValue   =   (intValue   <<  8)   +   byteValue[index+1]; 
         intValue   =   (intValue   <<  8)   +   byteValue[index+0]; 
         return   intValue;   
 	}   
 	
 	
	private  int byteArray2ToShort(byte[] byteValue, int index){   
         if(byteValue.length   <  2)   
                 return   0;   
        short   shortValue   =   0;    
        shortValue   =   byteValue[index+ 1];   
        shortValue   =   (short) ( (shortValue << 8) + byteValue[index+0] );   
 
        return   shortValue;   
 	} 
 	
 	
	
 	//select a music file from list
 	class musicListClickListener implements OnItemClickListener{
 		@Override
 		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 				long arg3) {
 			// TODO Auto-generated method stub
 			
 			playButton.setEnabled(true);
 			testButton.setEnabled(true);
 			
 			SelectedFile = new File( RecordMusicDir.getAbsolutePath()
 									+ File.separator 
 									+ ( (CheckedTextView) arg1).getText());
 			infoText.setText(( (CheckedTextView) arg1).getText() );
 			//Log.d(TAG,"select: " + ( (CheckedTextView) arg1).getText());
 			Log.d(TAG,"select: " + SelectedFile.toString());
 		}
 		
 	}
 	
 	//play a music that selected
 
 	
 	
 	public String byteArrayToHexString( byte[] array)
 	{
 		StringBuilder sb =new StringBuilder();
 		for( byte b: array ){
 			sb.append(String.format("%02x", b & 0xff));
 		}
 		return sb.toString();
 	}
 	
 	
 	
 
 	private void getRecordFiles( ){
 		recordFiles = new ArrayList<String>();
 		 if( SDRecorder.checkSD() ){
 			 File files[] = RecordMusicDir.listFiles();
 			 if(files != null ){
 				 //Log.d("tusion"," get files!");
 				 for( int i=0; i<files.length; i++ ){
 					 if( files[i].getName().indexOf(".") >= 0 )
 					 {
 						 //read all .amr files
 						 String file_str = files[i].getName().substring(
 								 	files[i].getName().indexOf("."));
 						 //if( file_str.toLowerCase().equals(".wav"))
 							 recordFiles.add( files[i].getName());
 					 }
 				 }
 			 } 
 		 }
 	 }
     
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 }
