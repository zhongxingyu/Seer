 package mmb.foss.aueb.icong.boxes;
 
 import mmb.foss.aueb.icong.R;
 import mmb.foss.aueb.icong.dialogs.CameraPreview;
 import android.app.Dialog;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.hardware.Camera;
 import android.hardware.Camera.PictureCallback;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.FrameLayout;
 
 public class CameraBox extends Box {
 	Bitmap bitmapPicture;
 	PictureCallback myPictureCallback_JPG;
 	private Camera camera;
     private CameraPreview cPreview;
 	public CameraBox(Context context) {
 		// TODO Auto-generated constructor stub
 		super(context, R.drawable.camera);
 		buttonX = new int[1][2];
 		buttonY = new int[1][2];
 		buttonPressed = new boolean[1];
 		buttonX[0][0] = 107;
 		buttonX[0][1] = 127;
 		buttonY[0][0] = 10;
 		buttonY[0][1] = 30;
 		this.setNoOfInputs(0);
 		this.setNoOfOutpus(1);
 		this.setHasDialog(true);
 	}
 
 	@Override
 	public void function() {
 		
 
 	}
 
 	@Override
 	public void showDialog(Context context)
 	{
 		Dialog dialog = new Dialog(context);
 		camera = getCameraInstance();
 		if(camera!=null)
 		{
 			dialog.setContentView(R.layout.camera_layout);
 			dialog.setCancelable(true);
 			dialog.setTitle("Camera,press click");
 			cPreview = new CameraPreview(context, camera);
 	        FrameLayout preview = (FrameLayout) dialog.findViewById(R.id.camera_preview);
 	        preview.addView(cPreview);
 	        myPictureCallback_JPG = new PictureCallback(){
 
 	        	 @Override
 	        	 public void onPictureTaken(byte[] arg0, Camera arg1) {
 	        	  // TODO Auto-generated method stub
 	        	  bitmapPicture
 	        	   = BitmapFactory.decodeByteArray(arg0, 0, arg0.length);
	        	  Log.e("yaw","picture taken"+bitmapPicture.toString());
	        	 setOutput(bitmapPicture, 0);
	        	 if(getOutput(0)==null)
	        	 {
	        		 Log.e("getOutput0","null");
	        	 }
	        	 else
	        	 {
	        		 Log.e("getOutput0",""+getOutput(0));
	        	 }
 	        	 }};
 		}
 		else
 		{
 			dialog.setTitle("Camera not found");
 			dialog.setCancelable(true);
 			dialog.setCanceledOnTouchOutside(true);
 		}
 		Button click = (Button) dialog.findViewById(R.id.button_save);
 		click.setOnClickListener(new View.OnClickListener()
 		{
 			
 			@Override
 			public void onClick(View v)
 			{
 				if(camera!=null)
 				{
 					
 					camera.takePicture(null, null, myPictureCallback_JPG);
 				}
 			}
 		});
 		dialog.show();
 		
 	}
 	public static Camera getCameraInstance(){
         Camera c = null;
         try {
             c = Camera.open();
         }
         catch (Exception e){
         }
         return c;
     }
 
 }
