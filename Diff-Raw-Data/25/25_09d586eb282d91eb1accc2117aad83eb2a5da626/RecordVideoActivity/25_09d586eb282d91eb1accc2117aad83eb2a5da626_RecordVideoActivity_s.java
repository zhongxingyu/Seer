 package com.sosohan.snapmv;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 
 
 import android.media.ThumbnailUtils;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.provider.MediaStore.Images.Thumbnails;
 import android.app.Activity;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 
 
 public class RecordVideoActivity extends Activity {
 	
 	public static final int REC_MOV = 1;
 	private ArrayList<String> array = new ArrayList<String>();
 		
 	private Button btnVideoRecord;
 	private Button btnCamera;
 	private Button btnPreview;
 	private Button btnDone;	
 	private View.OnClickListener btnClickListener;
 	
 	private ArrayList<ImageView> thumbnailArray = new ArrayList<ImageView>();
 	private int videoIndex = 0;
 	
 	private ImageButton btnDebug_add_mv;
 	private Button btnDebug_make_text_image;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.activity_record_video);       
 		
 		btnVideoRecord = (Button) findViewById(R.id.record_btn);
 		btnCamera = (Button) findViewById(R.id.camera_A_btn);
 		btnPreview = (Button) findViewById(R.id.preview_A_btn);
 		btnDone = (Button) findViewById(R.id.done_btn);
 		
 		btnDebug_add_mv = (ImageButton) findViewById(R.id.imageButton1);
 		btnDebug_make_text_image = (Button) findViewById(R.id.tmp_debug_btn);
 		
 		//thumbnailArray = (ImageView) findViewById(R.id.imageView1);
 		thumbnailArray.add((ImageView) findViewById(R.id.imageView0));
 		thumbnailArray.add((ImageView) findViewById(R.id.imageView1));
 		thumbnailArray.add((ImageView) findViewById(R.id.imageView2));
 		thumbnailArray.add((ImageView) findViewById(R.id.imageView3));
 		thumbnailArray.add((ImageView) findViewById(R.id.imageView4));
 		thumbnailArray.add((ImageView) findViewById(R.id.imageView5));
 		thumbnailArray.add((ImageView) findViewById(R.id.imageView6));
 		thumbnailArray.add((ImageView) findViewById(R.id.imageView7));
 		
 		//final String path = getExternalFilesDir(Environment.DIRECTORY_MOVIES).getPath().toString();
 		
 		btnClickListener = new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				if (v == btnVideoRecord) {
 					if(videoIndex < 8)
 					{
 						final ContentValues values = new ContentValues();
 						//videoFilename = path + "/sosohan"+videoIndex+".mp4";
 						//videoFilename = "/sdcard/DCIM/sosohan"+videoIndex+".mp4";
 						//values.put(MediaStore.Video.Media.DATA, videoFilename);
 						//captureMediaUri = getContentResolver().insert(
 						//		MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
 						final Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
 						//intent.putExtra(MediaStore.EXTRA_OUTPUT, captureMediaUri);
 						intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 3);
 						startActivityForResult(intent, REC_MOV);
 					}
 				}else if(v == btnCamera)
 				{
 					Intent intent = new Intent(RecordVideoActivity.this,
 							CameraActivity.class);
 					startActivity(intent);
 				}else if (v == btnPreview && !array.isEmpty()) {					
 					Intent intent = new Intent(RecordVideoActivity.this, PreviewActivity.class);
 					intent.putStringArrayListExtra("videolist", array);	
 					startActivity(intent);
 				}else if (v == btnDone){
 					Intent intent = new Intent(RecordVideoActivity.this,
 							SelectBgmActivity.class);
 					intent.putStringArrayListExtra("videolist", array);	
 					startActivity(intent);					
 				}else if (v == btnDebug_add_mv )
 				{
 					//array.add("/sdcard/DCIM/sosohan0.mp4");
 					//array.add("/sdcard/DCIM/sosohan1.mp4");	
 //					array.add("/storage/sdcard0/DCIM/100LGDSC/CAM00011.mp4");	
 //					array.add("/storage/sdcard0/DCIM/100LGDSC/CAM00010.mp4");	
 //					array.add("/storage/sdcard0/DCIM/100LGDSC/CAM00009.mp4");
 //					array.add("/sdcard/DCIM/Camera/20130717_120750.3gp");
 //					array.add("/sdcard/DCIM/Camera/20130718_173307.3gp");
 //					array.add("/sdcard/DCIM/Camera/20130717_120236.3gp");
 					array.add("/sdcard/DCIM/Camera/VID_20130719_170343.3gp");
 					array.add("/sdcard/DCIM/Camera/VID_20130719_180532.3gp");
 //					array.add("/sdcard/DCIM/Camera/VID_20130719_180544.3gp");
 //					array.add("/sdcard/DCIM/Camera/VID_20130717_165542.3gp");
 										
 					for(int i=0 ; i < array.size() ; i++)	{
 						Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(array.get(i), Thumbnails.MICRO_KIND);
						Log.e("JWJWJW", "onResume = " + array.get(i));
 						thumbnailArray.get(i).setImageBitmap(thumbnail);			
 					}
 					Intent intent = new Intent(RecordVideoActivity.this, MakeMVActivity.class);
 					intent.putStringArrayListExtra("videolist", array);	
 					startActivity(intent);
 				}
 				else if (v == btnDebug_make_text_image)
 				{
 					String path = Environment.getExternalStorageDirectory().getAbsolutePath();
 					Log.e("hjhjhj", "path : " + path);
 					Bitmap  b = Bitmap.createBitmap(640, 480, Bitmap.Config.RGB_565);
 					View view = (View)findViewById(R.id.imageView0);
 					Paint whitePaint = new Paint();
 					whitePaint.setTextSize(30);
 					whitePaint.setColor(Color.WHITE);
 					try {
 						File f = new File(path+"/notes");
 
 						f.mkdir();
 						File f2 = new File(path + "/notes/"+"test"+".png");
 
 						Canvas c = new Canvas( b );
 						c.drawText("this is SnapMV test", 100, 300, whitePaint);
 						view.draw( c );
 						FileOutputStream fos = new FileOutputStream(f2);
 						if ( fos != null )
 						{
 							b.compress(Bitmap.CompressFormat.PNG, 100, fos ); 
 							fos.close();
 						}
 					} catch ( Exception e ) {
 						Log.e("testSaveView", "Exception: " + e.toString() ); 
 					}
 				} 
 			}
 		};
 		
 		btnVideoRecord.setOnClickListener(btnClickListener);
 		btnCamera.setOnClickListener(btnClickListener);
 		btnPreview.setOnClickListener(btnClickListener);
 		btnDone.setOnClickListener(btnClickListener);
 		
 		btnDebug_add_mv.setOnClickListener(btnClickListener);
 		btnDebug_make_text_image.setOnClickListener(btnClickListener);
 	}
 
 	public void onActivityResult(int requestCode, int resultCode, Intent intent)
 	{
 	    super.onActivityResult(requestCode, resultCode, intent);
 	    if (resultCode == RESULT_OK)
 	    {
 	        if (requestCode == REC_MOV)
 	        {  
 	        	Uri uri = intent.getData();
 	        	String path = getPath(uri);
 	        	//Log.e("JWJWJW", "add = " + videoFilename + ",index = "+videoIndex );
 	        	Log.e("JWJWJW", "add = " + uri.getPath() + "::" + path + ",index = "+videoIndex );
 	        	//array.add(videoFilename);
 	        	array.add(path);
 	        	//Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(videoFilename, Thumbnails.MICRO_KIND);
 	        	Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(path, Thumbnails.MICRO_KIND);
 	            thumbnailArray.get(videoIndex).setImageBitmap(thumbnail);
 	            videoIndex++;
 	        }
 	    }	   
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.video_capture, menu);
 		return true;
 	}
 	
 	private String getPath(Uri uri)
 	{
 		String[] projection = { MediaStore.Images.Media.DATA };
 		Cursor cursor = managedQuery(uri, projection, null, null, null);
 		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
 		cursor.moveToFirst();
 		return cursor.getString(column_index);
 	}
 
 	private String getName(Uri uri)
 	{
 		String[] projection = { MediaStore.Images.ImageColumns.DISPLAY_NAME };
 		Cursor cursor = managedQuery(uri, projection, null, null, null);
 		int column_index = cursor
 				.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DISPLAY_NAME);
 		cursor.moveToFirst();
 		return cursor.getString(column_index);
 	}
 
 	private String getUriId(Uri uri)
 	{
 		String[] projection = { MediaStore.Images.ImageColumns._ID };
 		Cursor cursor = managedQuery(uri, projection, null, null, null);
 		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID);
 		cursor.moveToFirst();
 		return cursor.getString(column_index);
 	}
 
 }
