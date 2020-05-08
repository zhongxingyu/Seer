 package kr.hs.sshs.AndroidPTS.ui;
 
 import static com.googlecode.javacv.cpp.opencv_highgui.cvSaveImage;
 
 import java.io.File;
 import java.io.IOException;
 
 import kr.hs.sshs.AndroidPTS.logic.CPU;
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.Message;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.example.androidpts.R;
 import com.googlecode.javacv.FrameGrabber;
 import com.googlecode.javacv.FrameGrabber.Exception;
 import com.googlecode.javacv.cpp.opencv_core.IplImage;
 
 public class MainActivity extends Activity implements View.OnClickListener {
 	
 	CPU ARMv7;
 	FrameGrabber grabber;
 
 	TextView tvCPU;
 	ImageView iv;
 	ImageView movieplay;
 	Button btnProcess;
 	Button btnBypass;
 	Button btnGetVideo;
 	EditText etJumpFrame;
 	
 	IplImage result;
 	IplImage movieFrame;
 	
 	MyHandler mh;
 	
 	static double progress;
 
 	public TextView gettvCPU() {
 		return this.tvCPU;
 	}
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		Log.d("PASS", "Program started");
 
 		mh = new MyHandler();
 		
 		iv = (ImageView) findViewById(R.id.imageView2);
 		movieplay = (ImageView) findViewById(R.id.imageView1);
 		tvCPU = (TextView) findViewById(R.id.textView_CPUState);
 		btnBypass = (Button) findViewById(R.id.button_Bypass);
 		btnProcess = (Button) findViewById(R.id.button_Process);
 		btnGetVideo = (Button) findViewById(R.id.button_getVideo);
 		btnBypass.setOnClickListener(this);
 		btnProcess.setOnClickListener(this);
 		btnGetVideo.setOnClickListener(this);
 		etJumpFrame = (EditText) findViewById(R.id.editText_JumpFrame);
 
 		
 	}
 
 	public void onClick(View v) {
 		// IplImage read = cvCreateImage(new CvSize(640, 480), IPL_DEPTH_8U, 4);
 		
 		try {
 			switch (v.getId()) {
 			case R.id.button_getVideo:
 				Intent intent = new Intent(
                         Intent.ACTION_GET_CONTENT,      // 또는 ACTION_PICK
                         android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                 intent.setType("video/*");              // 모든 이미지
                 startActivityForResult(intent, 0);
                 
                 break;
                 
                 
 			case R.id.button_Process:
 				tvCPU.setText("Busy");
 				Log.d("PASS", "Processing...");
 				
 				new Thread() {
 					@Override
 					public void run() {
 						try {
 							while (CPU.framecount <= CPU.framelength) {
 								result = ARMv7.process();
 								movieFrame = ARMv7.getTmpl();
 								mh.sendMessage(mh.obtainMessage(101));	// When not done
 								
 								if(CPU.foundBall)
 									break;
 							}
 							
 							ARMv7.release();//why 주석처리했었음?
 							
 							mh.sendMessage(mh.obtainMessage(100));	// When done
 						} catch (java.lang.Exception e) {
 							e.printStackTrace();
							//Log.e("STOP", e.getMessage());
 						}
 					}
 				}.start();
 				break;
 
 			case R.id.button_Bypass:		
 				int jump = Integer.valueOf(etJumpFrame.getText().toString());
 				ARMv7.jumpFrames(jump);
 				cvSaveImage("/mnt/sdcard/result.jpg", ARMv7.process(true));
 				File pic2 = new File("/mnt/sdcard", "result.jpg");		
 				iv.setImageBitmap(BitmapFactory.decodeFile(pic2.getAbsolutePath()));
 				
 				Log.d("PASS", "Bypassed " + jump + " frames");
 				tvCPU.setText("吏�굹移�(" + CPU.framecount + "踰덉㎏ �꾨젅��");
 				break;
 			}
 		} catch (java.lang.Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			Log.e("STOP", e.getMessage());
 		}
 
 		// IplImage read = cvLoadImage("/mnt/sdcard/437.jpg");
 		// IplImage gray = cvCreateImage(cvSize(read.width(), read.height()),
 		// IPL_DEPTH_8U, 1);
 		// cvCvtColor(read, gray, CV_RGB2GRAY);
 		/*
 		IplImage readB = cvCreateImage(cvGetSize(read), IPL_DEPTH_8U, 1);
 		IplImage readG = cvCreateImage(cvGetSize(read), IPL_DEPTH_8U, 1);
 		IplImage readR = cvCreateImage(cvGetSize(read), IPL_DEPTH_8U, 1);
 		cvSplit(read, readR, readG, readB, null);
 		IplImage readA = cvCreateImage(cvGetSize(read), IPL_DEPTH_8U, 1); // alpha
 		cvSet(readA, new CvScalar(256), null);
 
 		IplImage readBGRA = cvCreateImage(cvGetSize(read), IPL_DEPTH_8U, 4);
 		cvMerge(readB, readG, readR, readA, readBGRA);
 
 		Bitmap temp = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888);
 		temp.copyPixelsFromBuffer(readBGRA.getByteBuffer());
 		iv.setImageBitmap(temp);
 
 		Log.w("FFmpeg", "Reached end FROM BUTTON");
 		*/
 	}
 	
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		if (requestCode == 0)
 			if (resultCode == Activity.RESULT_OK) {
 				Uri selectedVideo = data.getData();
 	      // TODO Do something with the select image URI
 				CPU.PATH=getRealPathFromURI(selectedVideo);
 			}
 		tvCPU.setText(CPU.PATH);
 		try {
 			ARMv7 = new CPU(mh);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			Log.e("STOP", e.getMessage());
 		}
 	}
 	
 	public String getRealPathFromURI(Uri contentUri){
 		String[] proj = {MediaStore.Video.Media.DATA };
 		Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
 		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
 		cursor.moveToFirst();
 		return cursor.getString(column_index);
 	}
 
 	public void showState(String state) {
 		tvCPU.setText("吏꾪뻾 以�" + CPU.framecount + " frame, " + Math.round((float)CPU.framecount*100.0/CPU.framelength) + "%) - " + state);
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 	
 	public class MyHandler extends Handler {
 		public static final int TEST = 0;
 		
 		@Override
 		public void handleMessage(Message msg) {
 			File pic2 = null;
 			File pic3 = null;
 			switch(msg.what) {
 			
 			case CPU.STATE_DETECT_VALUE_CHANGE:
 				showState("DVC");
 				Log.d("PASS", "DVC.....");
 				break;
 				
 			case CPU.STATE_BLOB_LABELING:
 				showState("BL");
 				Log.d("PASS", "BL.....");
 				break;
 				
 			case CPU.STATE_BLOB_FILTERING:
 				showState("BF");
 				Log.d("PASS", "BF.....");
 				break;
 				
 			case CPU.STATE_CANDIDATE_PROCESSING:
 				showState("CP");
 				Log.d("PASS", "CP.....");
 				break;
 				
 			case CPU.STATE_BLOB_STAMPING:
 				showState("BS");
 				Log.d("PASS", "BS.....");
 				break;
 				
 			case CPU.STATE_FOUND_BALL:
 				showState("BALLFOUND");
 				Log.d("PASS", "BALL FOUND");
 				break;
 			
 			case 101:				
 				cvSaveImage("/mnt/sdcard/result.jpg", result);
 				pic2 = new File("/mnt/sdcard", "result.jpg");		
 				iv.setImageBitmap(BitmapFactory.decodeFile(pic2.getAbsolutePath()));
 				cvSaveImage("/mnt/sdcard/movieFrame.jpg", movieFrame);
 				pic3 = new File("/mnt/sdcard", "movieFrame.jpg");		
 				movieplay.setImageBitmap(BitmapFactory.decodeFile(pic3.getAbsolutePath()));
 				break;
 				
 			case 100:				
 				cvSaveImage("/mnt/sdcard/result.jpg", result);
 				pic2 = new File("/mnt/sdcard", "result.jpg");		
 				iv.setImageBitmap(BitmapFactory.decodeFile(pic2.getAbsolutePath()));
 				cvSaveImage("/mnt/sdcard/movieFrame.jpg", movieFrame);
 				pic3 = new File("/mnt/sdcard", "movieFrame.jpg");		
 				movieplay.setImageBitmap(BitmapFactory.decodeFile(pic3.getAbsolutePath()));
 				
 				tvCPU.setText("�꾨즺! �꾨줈洹몃옩��醫낅즺�⑸땲��");
 				Log.d("PASS", "Done! saved result image");
 				break;
 			}
 		}
 	}
 }
