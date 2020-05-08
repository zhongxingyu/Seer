 package com.yhy.cameratest;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import org.opencv.android.LoaderCallbackInterface;
 import org.opencv.android.OpenCVLoader;
 import org.opencv.android.Utils;
 import org.opencv.core.Core;
 import org.opencv.core.Mat;
 import org.opencv.core.MatOfDMatch;
 import org.opencv.core.MatOfKeyPoint;
 import org.opencv.core.Point;
 import org.opencv.core.Rect;
 import org.opencv.core.Scalar;
 import org.opencv.features2d.DMatch;
 import org.opencv.features2d.DescriptorExtractor;
 import org.opencv.features2d.DescriptorMatcher;
 import org.opencv.features2d.FeatureDetector;
 import org.opencv.features2d.Features2d;
 import org.opencv.features2d.KeyPoint;
 import org.opencv.highgui.Highgui;
 import org.opencv.imgproc.Imgproc;
 
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ContentResolver;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.Config;
 import android.graphics.BitmapFactory;
 import android.util.Base64;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity implements OnClickListener {
 	private static final String TAG = "CAMERA::Activity";
 	private static final boolean D = false;
 	private static final int BOUNDARY = 35;
 	
 	private static final int PICK_FROM_CAMERA = 0;
 	private static final int PICK_FROM_ALBUM = 1;
 	private static final int CROP_FROM_CAMERA = 2;
 	
 	private Mat mSceneDescriptors = null;
 	
 	private ImageView mSample = null;
 	private ImageView mTarget = null;
 	
 	private Uri mImageCaptureUri;
 	private ImageView mPhotoImageView;
 	private Button mButton;
 	private ProgressBar mProgress;
 	private AsyncTask<Void, Integer, Void> mTask;
 	//private Bitmap bmpSample = null;
 	//private Bitmap bmpCrop = null;
 	
 	//private Uri mImageUri = null;
 	
    	private static int [] mResources = new int[]{
    			R.drawable.converted_resized_target1,
    			R.drawable.converted_resized_target2,
    			R.drawable.converted_resized_target3,
    			R.drawable.converted_resized_target4,
    			R.drawable.converted_resized_target5,
    			R.drawable.converted_resized_target6
    	};
 	
    	private int nMaxMatchNdx = 0;
    	private int nMaxMatchRate = 0;
    	
     private org.opencv.android.BaseLoaderCallback mLoaderCallback = new org.opencv.android.BaseLoaderCallback(this) {
         @Override
         public void onManagerConnected(int status) {
             switch (status) {
                 case LoaderCallbackInterface.SUCCESS:
                 {
                     Log.i(TAG, "OpenCV loaded successfully");
                     //mOpenCvCameraView.enableView();
                     
                     mButton.setEnabled(true);
             	    
                     /*
             		Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
             	    File photo;
             	    try
             	    {
             	        photo = createTemporaryFile("picture", ".jpg");
             	        photo.delete();
             	    }
             	    catch(Exception e)
             	    {
             	        Log.v(TAG, "Can't create file to take picture!");
             	        //Toast.makeText(MainActivity.this, "Please check SD card! Image shot is impossible!", 1000).show();
             	        return;
             	    }
             	    
             	    mImageUri = Uri.fromFile(photo);
             	    intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
             	    //start camera intent
             	    startActivityForResult(intent, 1004);
             		*/
             	    
                 } break;
                 default:
                 {
                     super.onManagerConnected(status);
                 } break;
             }
         }
     };
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		setContentView(R.layout.activity_main);
         
 		mButton = (Button)findViewById(R.id.start);
         mSample = (ImageView)findViewById(R.id.sample);
 		mTarget = (ImageView)findViewById(R.id.crop);
 		mProgress = (ProgressBar)findViewById(R.id.progress);
 		
 		mButton.setOnClickListener(this);
 		
 		if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback))
 	    {
 			Log.e(TAG, "Cannot connect to OpenCV Manager");
 	    }
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// TODO Auto-generated method stub
 		super.onActivityResult(requestCode, resultCode, data);
 		
 		
 		if(resultCode != RESULT_OK){
 			return;
 	    }
 
 	    switch(requestCode){
 	      	case CROP_FROM_CAMERA:{
 	      		// ũ   ̹ Ѱ ޽ϴ.
 	      		// ̹信 ̹ شٰų ΰ ۾ Ŀ
 		        // ӽ  մϴ.
 		        final Bundle extras = data.getExtras();
 		  
 		        if(extras != null)
 		        {
 		          Bitmap photo = extras.getParcelable("data");
 		          mPhotoImageView.setImageBitmap(photo);
 		        }
 		  
 		        // ӽ  
 		        File f = new File(mImageCaptureUri.getPath());
 		        if(f.exists())
 		        {
 		          f.delete();
 		        }
 		  
 		        break;
 	      	}
 	  
 	      	case PICK_FROM_ALBUM:{
 		        //  ó ī޶ Ƿ ϴ  break մϴ.
 		        //  ڵ忡  ո  Ͻñ ٶϴ.
 		        
 		        mImageCaptureUri = data.getData();
 	      	}
 	      
 	      	case PICK_FROM_CAMERA:{
 		        // ̹    ̹ ũ⸦ մϴ.
 		        // Ŀ ̹ ũ ø̼ ȣϰ ˴ϴ.
 	      		
 	      		/*
 		        Intent intent = new Intent("com.android.camera.action.CROP");
 		        intent.setDataAndType(mImageCaptureUri, "image/*");
 		  
 		        intent.putExtra("outputX", 90);
 		        intent.putExtra("outputY", 90);
 		        intent.putExtra("aspectX", 1);
 		        intent.putExtra("aspectY", 1);
 		        intent.putExtra("scale", true);
 		        intent.putExtra("return-data", true);
 		        startActivityForResult(intent, CROP_FROM_CAMERA);
 		  		*/
 	      		
 	      		
 	      	// AsyncTask Ȱ  ϴ. Ź Ӱ 
 	      	    mTask = new AsyncTask<Void, Integer, Void>()
 	      	    {
 	      	    	// ۾ ҽ ϱ  ÷
 	      	    	private boolean isCanceled = false;
 	      	      
 	      	    	// ۾ ϱ  ȣǴ ޼
 	      	    	@Override
 	      	    	protected void onPreExecute(){
 	      	    		publishProgress(0);
 	      	    		isCanceled = false;
 	      	    	}
 	      	      
 	      	    	// ׶忡 ۾
 	      	    	@Override
 	      	    	protected Void doInBackground(Void... params){
 	      	    		// 0.1ʸ 100ܰ  α׷ٸ 1 ŵϴ.
 	      	    		/*
 	      	    		for(int i = 1 ; i <= 100 && ! isCanceled ; i++){
 	      	    			try{
 	      	    				publishProgress(i);
 	      	    				Thread.sleep(10);
 	      	    			}catch(InterruptedException e){
 	      	    				e.printStackTrace();
 	      	    			}
 	      	    		}
 	      	    		*/
 	      	    		
 	      	    		mImgFromCamera = scaleAndTrun(MainActivity.this.grabImage());
 	      	     	    
 	      	     	    Mat src = new Mat();
 	      	         	Mat target = new Mat();
 	      	         	
 	      	         	BitmapFactory.Options options = new BitmapFactory.Options();
 	      	         	options.inPreferredConfig = Config.ARGB_8888;
 	      	         	Utils.bitmapToMat(mImgFromCamera, src);
 	      	         	
 	      	         	mSceneDescriptors = null;
 	      	         	
 	      	         	for(int i = 0 ;i < 6 ; i++){
 		      	  	       	Bitmap input2 = scaleAndTrun(BitmapFactory.decodeResource(getResources(), mResources[i]));
 		      	  	       	Utils.bitmapToMat(input2, target);
 		      	  	       	
 		      	  	       	Mat matScene = getSecenDescriptor(src);
 		      	  	       	Mat matTrain = getTrainDescriptor(target,i);
 		      	  	       	int nMatchRate = match(matScene,matTrain);
 		      	  	       	
 		      	  	       	if(nMaxMatchRate < nMatchRate){
 		      	  	       		nMaxMatchRate = nMatchRate;
 		      	  	       		nMaxMatchNdx = i;
 		      	  	       	}
 		      	  	       	
 		      	  	       	publishProgress(15*(i+1));
 	      	         	}
 	      	    		
 	      	    		
 	      	    		return null;
 	      	    	}
 
 	      	    	// publishProgress() ޼带  ȣ˴ϴ.  ǥϴµ Դϴ.
 	      	    	@Override
 	      	    	protected void onProgressUpdate(Integer... progress){
 	      	    		if(progress[0] == 0){
 	      	    			mProgress.setVisibility(View.VISIBLE);
 	      	    		}
 	      	    		mProgress.setProgress(progress[0]);
 	      	    	}
 	      	      
 	      	    	// ۾ Ϸ Ŀ ȣǴ ޼ҵ
 	      	    	@Override
 	      	    	protected void onPostExecute(Void result){
 	      	    		//Toast.makeText(MainActivity.this, "Ϸ", Toast.LENGTH_SHORT).show();
 	      	    		//mButton.setText("start");
 	      	    		publishProgress(100);
 	      	    		
 	      	    		TextView tv = (TextView)findViewById(R.id.txt_desc);
 	      	    		tv.setText("Max Matching Rate : "+nMaxMatchRate+"%");
 	      	     		mSample.setImageBitmap(mImgFromCamera);
 	      	     		mTarget.setImageResource(mResources[nMaxMatchNdx]);
 	      	     		
 	      	     		mProgress.setVisibility(View.GONE);
 	      	    	}
 	      	      
 	      	    	// ܺο  Ҷ ȣǴ ޼ҵ
 	      	    	@Override
 	      	    	protected void onCancelled(){
 	      	    		isCanceled = true;
 	      	    		publishProgress(0);
 	      	    		Toast.makeText(MainActivity.this, "ҵ", Toast.LENGTH_SHORT).show();
 	      	    	}
 	      	    };
 	      	    
 	      	    // ۾ 
 	      	    mTask.execute();
 	      		
 	      		
 	      		
 		        break;
 	      	}
 	    }
 	}
 	
 	private Bitmap mImgFromCamera;
 	
 	private void detectImage(){
 		mImgFromCamera = scaleAndTrun(this.grabImage());
    	    
    	    Mat src = new Mat();
        	Mat target = new Mat();
        	
        	BitmapFactory.Options options = new BitmapFactory.Options();
        	options.inPreferredConfig = Config.ARGB_8888;
        	Utils.bitmapToMat(mImgFromCamera, src);
        	
        	mSceneDescriptors = null;
        	
        	for(int i = 0 ;i < 6 ; i++){
 	       	Bitmap input2 = scaleAndTrun(BitmapFactory.decodeResource(getResources(), mResources[i]));
 	       	Utils.bitmapToMat(input2, target);
 	       	
 	       	Mat matScene = getSecenDescriptor(src);
 	       	Mat matTrain = getTrainDescriptor(target,i);
 	       	int nMatchRate = match(matScene,matTrain);
 	       	
 	       	if(nMaxMatchRate < nMatchRate){
 	       		nMaxMatchRate = nMatchRate;
 	       		nMaxMatchNdx = i;
 	       	}
        	}
        	
 	}
 	
 	/*
 	private File createTemporaryFile(String part, String ext) throws Exception
 	{
 	    File tempDir= Environment.getExternalStorageDirectory();
 	    tempDir=new File(tempDir.getAbsolutePath()+"/.temp/");
 	    if(!tempDir.exists())
 	    {
 	        tempDir.mkdir();
 	    }
 	    return File.createTempFile(part, ext, tempDir);
 	}
 	*/
 	
 	public Bitmap grabImage()
 	{
 	    this.getContentResolver().notifyChange(mImageCaptureUri, null);
 	    ContentResolver cr = this.getContentResolver();
 	    Bitmap bitmap;
 	    try
 	    {
 	        bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, mImageCaptureUri);
 	        //imageView.setImageBitmap(bitmap);
 	        return bitmap;
 	    }
 	    catch (Exception e)
 	    {
 	        Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show();
 	        Log.d(TAG, "Failed to load", e);
 	    }
 	    
 	    return null;
 	}
 	
 	private Mat getSecenDescriptor(Mat srcMat){
 		if(mSceneDescriptors == null){
 			FeatureDetector detector = FeatureDetector.create(FeatureDetector.FAST);
 	        DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.BRIEF);
 			
 	        Mat srcImage = new Mat();
 	        Mat quSrcImage = new Mat();
 	        srcMat.copyTo(quSrcImage);
 	        
 	        Imgproc.cvtColor(quSrcImage, srcImage, Imgproc.COLOR_RGBA2RGB,3);
 	        MatOfKeyPoint vectorSrc = new MatOfKeyPoint();
 	        detector.detect(srcImage, vectorSrc );
 	        
 	        Mat sceneDescriptors = new Mat();
 	        extractor.compute( srcImage, vectorSrc, sceneDescriptors );
 	        
 	        mSceneDescriptors = sceneDescriptors;
         }
 		
 		return mSceneDescriptors;
 	}
 	
 	private Mat getTrainDescriptor(Mat targetMat,int i){
 		//Load From File.
 		try{
 			String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
 			File file = new File(extStorageDirectory, "mat_"+i+".txt");
 			if(file.exists() && file.length() > 0){
 				
 				FileInputStream fIn = new FileInputStream(file);
 		        InputStreamReader isr = new InputStreamReader(fIn);
 		        
 		        char[] inputBuffer = new char[(int) file.length()];
 		        isr.read(inputBuffer);
 		        isr.close();
 		        
 		        String data = new String(inputBuffer);
 		        
 		        String base64="";
 		        int type,cols,rows;
 		        
 		        String [] raw = data.split("\t");
 		        
 		        if(raw.length == 4){
 		        	rows = Integer.parseInt(raw[0]);
 		        	cols = Integer.parseInt(raw[1]);
 		        	type = Integer.parseInt(raw[2]);
 		        	base64 = raw[3];
 		        	
 			        byte [] buff  = Base64.decode(base64, Base64.DEFAULT);
 			        Mat trainDescriptors = new Mat(rows,cols,type);
 			        trainDescriptors.put(0, 0, buff);
 			        
 			        return trainDescriptors;
 		        }
 			}
 	    }catch(IOException e){
 	    	
 	    }
 		
 		FeatureDetector detector = FeatureDetector.create(FeatureDetector.FAST);
         DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.BRIEF);
         
     	//Target - Train-------------------------------------------------------
         Mat targetImage = new Mat();
         Mat quTargetImage = new Mat();
         targetMat.copyTo(quTargetImage);
         
         Imgproc.cvtColor(quTargetImage, targetImage, Imgproc.COLOR_RGBA2RGB,3);
         MatOfKeyPoint vectorTarget = new MatOfKeyPoint();
         detector.detect(targetImage, vectorTarget );
         
         Mat trainDescriptors = new Mat();
         extractor.compute( targetImage, vectorTarget, trainDescriptors );
         
 
         int count = (int) (trainDescriptors.total() * trainDescriptors.channels());
 	    byte[] buff = new byte[count];
 	    trainDescriptors.get(0, 0, buff);
 	    String base64 = Base64.encodeToString(buff, Base64.DEFAULT);
 	    
 	    try{
 			String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
 			OutputStream outStream = null;
 			File file = new File(extStorageDirectory, "mat_"+i+".txt");
 		    outStream = new FileOutputStream(file);
 		    OutputStreamWriter osw = new OutputStreamWriter(outStream); 
 		    
 		    int type = trainDescriptors.type();
 		    int cols = trainDescriptors.cols();
 		    int rows = trainDescriptors.rows();
 		    
 		    String data = ""+rows+"\t"+cols+"\t"+type+"\t"+base64;
 		    osw.write(data);
 		    
 		    osw.flush();
 		    osw.close();		    
 	    }catch(IOException e){
 	    	
 	    }
 
         return trainDescriptors;
 	}
 	
 	private int match(Mat matScene, Mat matTrain){
 		MatOfDMatch matches = new MatOfDMatch();
         DescriptorMatcher matcherHamming = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMINGLUT);
         List<Mat> listMat = new ArrayList<Mat>();
         listMat.add(matTrain);
         matcherHamming.add(listMat);
         matcherHamming.train();
                 
         matcherHamming.match(matScene, matches); 
         
         double max_dist = 0;
 		double min_dist = 1000000;
 		List<DMatch> good_matches = new ArrayList<DMatch>();
 		
     	List<DMatch> in_matches = matches.toList();
 		int rowCount = in_matches.size();
 		
 		for (int i = 0; i < rowCount; i++) {
 			double dist = in_matches.get(i).distance;
 			if (dist < min_dist)
 				min_dist = dist;
 			if (dist > max_dist)
 				max_dist = dist;
 		}
 		
 		for (int i = 0; i < rowCount; i++) {
 			if (in_matches.get(i).distance <= BOUNDARY) {
 				good_matches.add(in_matches.get(i));
 			}
 		}        
         
 		int nMatchRate = (int) Math.round((1.0  * good_matches.size() / rowCount) * 100.0);
 		
 		return nMatchRate;
 	}
 	
     private Bitmap scaleAndTrun(Bitmap bm) {
 		int MAX_DIM = 250;
 		int w, h;
 		if (bm.getWidth() >= bm.getHeight()) {
 			w = MAX_DIM;
 			h = bm.getHeight() * MAX_DIM / bm.getWidth();
 		} else {
 			h = MAX_DIM;
 			w = bm.getWidth() * MAX_DIM / bm.getHeight();
 		}
 		bm = Bitmap.createScaledBitmap(bm, w, h, false);
 		Bitmap img_bit = bm.copy(Bitmap.Config.ARGB_8888, false);
 		return img_bit;
 	}
 	
     
     /**
      * ī޶󿡼 ̹ 
      */
     private void doTakePhotoAction()
     {
       /*
        *  غ
        * http://2009.hfoss.org/Tutorial:Camera_and_Gallery_Demo
        * http://stackoverflow.com/questions/1050297/how-to-get-the-url-of-the-captured-image
        * http://www.damonkohler.com/2009/02/android-recipes.html
        * http://www.firstclown.us/tag/android/
        */
 
       Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
       
       // ӽ÷   θ 
       String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
       mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), url));
       
       intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
       // Ư⿡  ϴ  ־  ּó մϴ.
       //intent.putExtra("return-data", true);
       startActivityForResult(intent, PICK_FROM_CAMERA);
     }
     
     /**
      * ٹ ̹ 
      */
     private void doTakeAlbumAction()
     {
       // ٹ ȣ
       Intent intent = new Intent(Intent.ACTION_PICK);
       intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
       startActivityForResult(intent, PICK_FROM_ALBUM);
     }
     
     @Override
     public void onClick(View v)
     {
     	DialogInterface.OnClickListener cameraListener = new DialogInterface.OnClickListener(){
     		@Override
     		public void onClick(DialogInterface dialog, int which){
     			doTakePhotoAction();
     		}	
     	};
       
     	DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener(){
     		@Override
     		public void onClick(DialogInterface dialog, int which){
     			doTakeAlbumAction();
     		}
     	};
       
     	DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener(){
     		@Override
     		public void onClick(DialogInterface dialog, int which){
     			dialog.dismiss();
     		}
     	};
       
     	new AlertDialog.Builder(this)
         	.setTitle("Choose Image")
         	.setPositiveButton("TakePhoto", cameraListener)
         	.setNeutralButton("Album", albumListener)
         	.setNegativeButton("Cancel", cancelListener)
         	.show();
     }
     
 	/*
     private int surf(Mat srcMat, Mat targetMat, 
     		ImageView srcImageView, ImageView targetImageView){
     	
     	long lStartTime  = 0 ;
     	
     	//FeatureDetector detector = FeatureDetector.create(FeatureDetector.SIFT);
         //DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.SIFT);
     	
     	//FeatureDetector surfDetector = FeatureDetector.create(FeatureDetector.SURF);
     	//DescriptorExtractor surfDescriptor =DescriptorExtractor.create(DescriptorExtractor.SURF);
     	
     	FeatureDetector detector = FeatureDetector.create(FeatureDetector.FAST);
         DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.BRIEF);
         
         //FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB);
         //DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
     	
         if(D){
        		long lEndTime = new Date().getTime();
        		Log.v(TAG,"After create instance : "+ (lEndTime - lStartTime));
        		lStartTime  = new Date().getTime();
        	}
         
     	//Target - Train-------------------------------------------------------
         Mat targetImage = new Mat();
         Mat quTargetImage = new Mat();
         targetMat.copyTo(quTargetImage);
         
         //Imgproc.cvtColor(quTargetImage, targetImage, Imgproc.COLOR_RGBA2GRAY);
         Imgproc.cvtColor(quTargetImage, targetImage, Imgproc.COLOR_RGBA2RGB,3);
         MatOfKeyPoint vectorTarget = new MatOfKeyPoint();
         detector.detect(targetImage, vectorTarget );
         
         if(D){
        		long lEndTime = new Date().getTime();
        		Log.v(TAG,"After target compute : "+ (lEndTime - lStartTime));
        		lStartTime  = new Date().getTime();
        	}
         
         Mat trainDescriptors = new Mat();
         extractor.compute( targetImage, vectorTarget, trainDescriptors );
         
         if(D){
 	        int count = (int) (trainDescriptors.total() * trainDescriptors.channels());
 		    byte[] buff = new byte[count];
 		    trainDescriptors.get(0, 0, buff);
 		    Log.v(TAG,"Base64="+Base64.encodeToString(buff, Base64.DEFAULT));
         }
 	    
         if(D){
        		long lEndTime = new Date().getTime();
        		Log.v(TAG,"After target compute : "+ (lEndTime - lStartTime));
        		lStartTime  = new Date().getTime();
        	}
         
         //Src - Scene-------------------------------------------------------
         if(mSceneDescriptors == null){
 	        Mat srcImage = new Mat();
 	        Mat quSrcImage = new Mat();
 	        srcMat.copyTo(quSrcImage);
 	        
 	        //Imgproc.cvtColor(quSrcImage, srcImage, Imgproc.COLOR_RGBA2GRAY);
 	        Imgproc.cvtColor(quSrcImage, srcImage, Imgproc.COLOR_RGBA2RGB,3);
 	        MatOfKeyPoint vectorSrc = new MatOfKeyPoint();
 	        detector.detect(srcImage, vectorSrc );
 	        
 	        if(D){
 	       		long lEndTime = new Date().getTime();
 	       		Log.v(TAG,"After source detect : "+ (lEndTime - lStartTime));
 	       		lStartTime  = new Date().getTime();
 	       	}
 	        
 	        Mat sceneDescriptors = new Mat();
 	        extractor.compute( srcImage, vectorSrc, sceneDescriptors );
 	        
 	        if(D){
 	       		long lEndTime = new Date().getTime();
 	       		Log.v(TAG,"After source compute : "+ (lEndTime - lStartTime));
 	       		lStartTime  = new Date().getTime();
 	       	}
 	        
 	        mSceneDescriptors = sceneDescriptors;
         }
         //Match----------------------------------------------------------------
         MatOfDMatch matches = new MatOfDMatch();
         
         DescriptorMatcher matcherHamming = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMINGLUT);
         List<Mat> listMat = new ArrayList<Mat>();
         listMat.add(trainDescriptors);
         matcherHamming.add(listMat);
         matcherHamming.train();
                 
         matcherHamming.match(mSceneDescriptors, matches); 
         
         if(D){
        		long lEndTime = new Date().getTime();
        		Log.v(TAG,"After match : "+ (lEndTime - lStartTime));
        		lStartTime  = new Date().getTime();
        	}
 
         Mat matchedImage=new Mat();
         Features2d.drawMatches(quSrcImage, vectorSrc, quTargetImage, vectorTarget, matches, matchedImage);
         
         double max_dist = 0;
 		double min_dist = 1000000;
 		List<DMatch> good_matches = new ArrayList<DMatch>();
 		
     	List<DMatch> in_matches = matches.toList();
 		int rowCount = in_matches.size();
 		
 		for (int i = 0; i < rowCount; i++) {
 			double dist = in_matches.get(i).distance;
 			//System.out.println("distance dist==" + dist);
 			//if(D) Log.v(TAG,"distance dist==" + dist);
 			if (dist < min_dist)
 				min_dist = dist;
 			if (dist > max_dist)
 				max_dist = dist;
 		}
 		
 		if(D) Log.v(TAG,"rowCount===" + rowCount + " max_dist= " + max_dist + " min_dist=" + min_dist);
 		
 		for (int i = 0; i < rowCount; i++) {
 			if (in_matches.get(i).distance <= BOUNDARY) {
 				good_matches.add(in_matches.get(i));
 			}
 		}        
         
 		int nMatchRate = (int) Math.round((1.0  * good_matches.size() / rowCount) * 100.0);
 		
 		if(D) Log.v(TAG,"Feature Count :"+ rowCount + ", Good Matched Count : " + good_matches.size());
 		if(D) Log.v(TAG,"Matching Rate : " + (1.0  * good_matches.size() / rowCount) * 100.0);
 		
 		if(D) {
 			TextView tv = (TextView)findViewById(R.id.txt_desc);
 			tv.setText("Matching Rate : "+nMatchRate+"%");
 		}
 		
 
         MatOfDMatch matGood = new MatOfDMatch();
         matGood.fromList(good_matches);
         Features2d.drawMatches(quSrcImage, vectorSrc, quTargetImage, vectorTarget, matGood, matchedImage);
 
 		
         if(D){
        		long lEndTime = new Date().getTime();
        		Log.v(TAG,"After image process : "+ (lEndTime - lStartTime));
        		lStartTime  = new Date().getTime();
        	}
 
         
         //Target Show----------------------------------------------------------
         Mat outTargetImg = new Mat();
         Imgproc.cvtColor(quTargetImage, quTargetImage, Imgproc.COLOR_RGBA2BGR,3);
         Features2d.drawKeypoints(quTargetImage, vectorTarget, outTargetImg);
         Imgproc.cvtColor(outTargetImg, outTargetImg, Imgproc.COLOR_BGR2RGBA,4);
         
         Bitmap resultTarget = Bitmap.createBitmap( outTargetImg.cols(), outTargetImg.rows(), Bitmap.Config.ARGB_8888);
         Utils.matToBitmap(outTargetImg,resultTarget);
         targetImageView.setImageBitmap(resultTarget);
         
         //Src Show-------------------------------------------------------------
         Mat outSrcImg = new Mat();
         //Imgproc.cvtColor(matchedImage, quSrcImage, Imgproc.COLOR_RGBA2BGR,3);
         Imgproc.cvtColor(quSrcImage, quSrcImage, Imgproc.COLOR_RGBA2BGR);
         Features2d.drawKeypoints(quSrcImage, vectorSrc, outSrcImg);
         Imgproc.cvtColor(outSrcImg, outSrcImg, Imgproc.COLOR_BGR2RGBA,4);
         
         Bitmap resultSrc = Bitmap.createBitmap( outSrcImg.cols(), outSrcImg.rows(), Bitmap.Config.ARGB_8888);
         Utils.matToBitmap(outSrcImg,resultSrc);
         srcImageView.setImageBitmap(resultSrc);
         
         if(D){
         	//Result Show-------------------------------------------------------------
 	        Mat outMatchImg = new Mat();
 	        Imgproc.cvtColor(matchedImage, quSrcImage, Imgproc.COLOR_RGBA2BGR,3);
 	        Features2d.drawKeypoints(quSrcImage, vectorSrc, outMatchImg);
 	        Imgproc.cvtColor(outMatchImg, outMatchImg, Imgproc.COLOR_BGR2RGBA,4);
 	        
 	        Bitmap resultMatch = Bitmap.createBitmap( outMatchImg.cols(), outMatchImg.rows(), Bitmap.Config.ARGB_8888);
 	        Utils.matToBitmap(outMatchImg,resultMatch);
 	        
 	        ImageView resultImageView = (ImageView)findViewById(R.id.match);
 	        resultImageView.setImageBitmap(resultMatch);
         }
 
         
         if(D){
        		long lEndTime = new Date().getTime();
        		Log.v(TAG,"Before end process : "+ (lEndTime - lStartTime));
        		lStartTime  = new Date().getTime();
        	}
         
         return nMatchRate;
     	
     }
     */
     
 	/*
 	private boolean templateMatch(Mat src, Mat target, ImageView im){
 		Mat src_img_display = new Mat();
     	src.copyTo(src_img_display);
     	
     	if(D) Log.v(TAG,"src.cols="+src.cols());
     	if(D) Log.v(TAG,"src.rows="+src.rows());    	
     	if(D) Log.v(TAG,"target.cols="+target.cols());
     	if(D) Log.v(TAG,"target.rows="+target.rows());
     	
     	Mat result = new Mat();
     	
     	Imgproc.matchTemplate(src, target, result, 3);
     	
     	Core.normalize(result, result,0,100,Core.NORM_MINMAX,-1, new Mat());
     	Point matchLoc;
     	double maxVal;
     	Core.MinMaxLocResult mmres = Core.minMaxLoc(result);
     	
     	matchLoc = mmres.maxLoc;
     	maxVal = mmres.maxVal;
     	
     	if(maxVal < 90.0 || maxVal > 100.0){
     		return false;
     	}
     	
     	if(D) Log.v(TAG,"maxVal="+maxVal);
     	if(D) Log.v(TAG,"matchLoc="+matchLoc);
     	
     	Core.rectangle(src_img_display, matchLoc, new Point(matchLoc.x + target.cols(), matchLoc.y + target.rows()), new Scalar(255,0,0,255),3,8,0);
     	//Core.Rectangle(result, matchLoc, new Point(matchLoc.x + target.cols(), matchLoc.y + target.rows()), new Scalar(0,0,0,0),2,8,0 );
     	//Core.rectangle(src_img_display, new Point(3,3), new Point(src_img_display.cols()-3,src_img_display.rows()-3), new Scalar(255,0,0,255),3,8,0);
     	
     	if(D) Log.v(TAG,"result.cols="+result.cols());
     	if(D) Log.v(TAG,"result.rows="+result.rows());
     	
     	if(D) Log.v(TAG,"src.cols="+src_img_display.cols());
     	if(D) Log.v(TAG,"src.rows="+src_img_display.rows());
     	
     	Bitmap result1 = Bitmap.createBitmap(src_img_display.cols(),src_img_display.rows(),Config.ARGB_8888);
     	//Imgproc.cvtColor(src_img_display, src_img_display, Imgproc.COLOR_GRAY2RGBA,4);
     	Utils.matToBitmap(src_img_display,result1);
     	im.setImageBitmap(result1);
     	
     	return true;
 	}
 	*/
 }
