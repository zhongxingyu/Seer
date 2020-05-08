 package be.ugent.ods.osgi.tests.implementations;
 
 import java.io.ByteArrayOutputStream;
 import java.util.ArrayList;
 import java.util.concurrent.Semaphore;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Bitmap.CompressFormat;
 import android.graphics.BitmapFactory;
 import android.media.MediaMetadataRetriever;
 import android.media.MediaPlayer;
 import android.net.Uri;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.widget.ImageView;
 import android.widget.ViewFlipper;
 import be.ugent.ods.osgi.protocolabstraction.ModuleAccessor;
 import be.ugent.ods.osgi.tests.interfaces.AbstractTest;
 import be.ugent.ods.testapplications.service.interfaces.VideoService;
 
 public class VideoTest extends AbstractTest {
 
 	private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;
 	protected Semaphore waitingForResult=new Semaphore(0);
 	private VideoService service;
 	private Uri videoUri = null;
 	
 	private ArrayList<Bitmap> ar = null;
 	int begin = 0;
 	int einde = 0;
 	int width = 0;
 	int height = 0;
 	
 	private ArrayList<ArrayList<Bitmap>> resultmacroblocks;// = new ArrayList<ArrayList<Bitmap>>();
 	private ArrayList<Bitmap> frames = new ArrayList<Bitmap>();
 	
 	private ArrayList<ArrayList<byte[]>> bytesMacroblocks; // = new ArrayList<ArrayList<byte[]>>();
 	private ArrayList<ArrayList<byte[]>> result = null;
 	
 	@SuppressLint("NewApi")
 	@Override
 	public void runActivityForResult(int requestCode, int resultCode,
 			Intent data) {
 		if (requestCode == CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE) {
 			if (resultCode == Activity.RESULT_OK) {
 				videoUri = data.getData();
 				MediaMetadataRetriever retriever = new MediaMetadataRetriever();
 	    		retriever.setDataSource(feedback.getActivity(),videoUri);
     	    	ArrayList<Bitmap> rev = new ArrayList<Bitmap>();
     	    	MediaPlayer mp = MediaPlayer.create(feedback.getActivity(),videoUri);
     	    	int millis = mp.getDuration();
     	    	for(int i=0;i<millis;i+=100){
     	    		boolean eralin = false;
     	    		Bitmap bm = retriever.getFrameAtTime(i*10000);
     	    		if(bm != null && !rev.contains(bm)){
     	    			for(int j=0; j<rev.size();j++){
     	    				if(bm.sameAs(rev.get(j))){
     	    					eralin=true;
     	    				}
     	    			}
     	    			if(!eralin){
     	    				rev.add(bm);
     	    			}
     	    		}
     	    	}
         	    ar = rev;
 	    		for(int i=0;i< rev.size(); i++){
 	    			ar.set(i, rev.get(i));	    			
 	    		}
 	    		
 	    		//resultmacroblocks = new ArrayList<ArrayList<Bitmap>>();
 	    		bytesMacroblocks = new ArrayList<ArrayList<byte[]>>();
 	    		for(int frame = 0; frame<ar.size();frame++){
 	        		boolean done = false;
 	        		//resultmacroblocks.add(new ArrayList<Bitmap>());
 	        		bytesMacroblocks.add(new ArrayList<byte[]>());
 	        		Bitmap source = ar.get(frame);
 	        		width = source.getWidth();
 	        		height = source.getHeight();
 	        		while(!done){
 	                	Bitmap bm = Bitmap.createBitmap(source, begin, einde, 16, 16);
 	                	ByteArrayOutputStream bos = new ByteArrayOutputStream();
 	                	bm.compress(CompressFormat.PNG, 0, bos);
 	                	byte[] n = bos.toByteArray();
 	                	bytesMacroblocks.get(frame).add(n);
 	                	//byte[] result = service.doSomething(bm.getWidth(),bm.getHeight(),n);
 	                	//Bitmap nieuw = BitmapFactory.decodeByteArray(result, 0, result.length);
 	                	//resultmacroblocks.get(frame).add(nieuw);
 	                	begin += 16;
 	                	if(begin>=source.getWidth()){
 	                		begin = 0;
 	                		einde += 16;
 	                		if(einde>=source.getHeight()){
 	                			einde = 0;
 	                			done = true;
 	                		}
 	                	}
 	            		
 	            	}
 	        		Log.d("HELP","lengte van bytesMacroblocks" + bytesMacroblocks.size());
 	        	}
 	    		
 	    		
 			//	test();
 			} else if (resultCode == Activity.RESULT_CANCELED) {
 				Log.d("HELP", "hier2");
 			} else {
 				Log.d("HELP", "hier3");
 			}		
 			waitingForResult.release();
 		}
 
 	}
 
 	@Override
 	public void test() {
 		
 		for(int frame = 0; frame<ar.size();frame++){
     		for(int lengte = 0; lengte < bytesMacroblocks.get(frame).size(); lengte++){
     			Log.d("HELP","iets doorsturen" + lengte);
     			byte[] n = bytesMacroblocks.get(frame).get(lengte);
     			service.doSomething(width,height,n,frame);
     		}
     	}
 		result = service.getResult();		
 	}
 
 	@Override
 	public void preRun(ModuleAccessor accessor) {
 		// create new Intent
 		service = (VideoService) accessor
 				.getModule(VideoService.class);
 		if(videoUri == null){
 			Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
 
 			intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1); // set the video
 																// image quality to
 																// high
 			// start the Video Capture Intent
 			feedback.getActivity().startActivityForResult(intent,
 					CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);
 			//wait
 			try {
 				waitingForResult.acquire();
 			} catch (InterruptedException e) {}
 		}
 	}
 
 	@Override
 	public void postRun() {
 		resultmacroblocks = new ArrayList<ArrayList<Bitmap>>();
 		for(int frame = 0; frame<ar.size();frame++){
     		resultmacroblocks.add(new ArrayList<Bitmap>());
     		for(int lengte = 0; lengte < result.get(frame).size(); lengte++){
     			byte[] r = result.get(frame).get(lengte);
             	Bitmap nieuw = BitmapFactory.decodeByteArray(r, 0, r.length);
             	resultmacroblocks.get(frame).add(nieuw); 
     		}
     		
     	}
 		for(int frame = 0; frame<resultmacroblocks.size();frame++){
     		ArrayList<Bitmap> bmFrame = resultmacroblocks.get(frame);
         	Bitmap bmnieuw = Bitmap.createBitmap(width,height,bmFrame.get(0).getConfig());
     		Canvas canvas = new Canvas(bmnieuw);
     		canvas.drawBitmap(bmFrame.get(0), 0, 0, null);
         	int aantal = 1;
         	int y = 0;
         	for(int i=1;i<(width/16);i++){
         		if(aantal<bmFrame.size()){
         			canvas.drawBitmap(bmFrame.get(aantal),i*16,y,null);
         		}
         		aantal++;
         	}
     		y+= 16;	
         	while(aantal<bmFrame.size()){
         		for(int i=0;i<(width/16);i++){
             		if(aantal<bmFrame.size()){
             			canvas.drawBitmap(bmFrame.get(aantal),i*16,y,null);
             		}
             		aantal++;
             	}
         		y+= 16;	
         	}
         	frames.add(bmnieuw);
     	}
 		
 		Log.d("HELP","het aantal frames is: "+frames.size());
 		
 		feedback.getActivity().runOnUiThread(new Runnable() {
 			
 			@Override
 			public void run() {
 				//videoview needs for some f***** reason a main thread
 				ViewFlipper imageFlipper = new ViewFlipper(feedback.getActivity());
 				for(int i=0;i<frames.size();i++){
 					ImageView im = new ImageView(feedback.getActivity());
 					im.setImageBitmap(frames.get(i));
 					imageFlipper.addView(im);
 				}
 				imageFlipper.setFlipInterval(2500);
 				imageFlipper.startFlipping();
 				feedback.pushTestView(imageFlipper);
 				
 			}
 		});
 
 	}
 
 	@Override
 	public String getName() {
 		// TODO Auto-generated method stub
 		return "video";
 	}
 
 }
