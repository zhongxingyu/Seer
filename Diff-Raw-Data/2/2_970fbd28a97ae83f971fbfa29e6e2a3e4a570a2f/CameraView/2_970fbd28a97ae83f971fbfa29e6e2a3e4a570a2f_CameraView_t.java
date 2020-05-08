 package dev.poutyface.cherrypic;
 
 import android.content.Context;
 import android.graphics.PixelFormat;
 import android.hardware.Camera;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import java.io.BufferedOutputStream;
 import java.net.Socket;
 import android.widget.Toast;
 
 
 public class CameraView extends SurfaceView implements SurfaceHolder.Callback {
 	private SurfaceHolder holder;
 	private Camera camera;
 	private static final int WIDTH = 480;
 	private static final int HEIGHT = 320;
 	private static final int PORT = 4321;
 	private static final String ADDRESS = "210.132.175.118";
 	private Context mContext;
 	
 	public CameraView(Context context){
 		super(context);
 		mContext = context;
 		holder = getHolder();
 		holder.addCallback(this);
 		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 	}
 	
 	public void surfaceCreated(SurfaceHolder holder){
 		
 		try{
 			camera = Camera.open();
 			camera.setPreviewDisplay(holder);
 		} catch (Exception e){
 			e.printStackTrace();
 		}		
 	}
 	
 	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h){
 		Camera.Parameters params = camera.getParameters();
 		params.setPictureSize(WIDTH, HEIGHT);
 		params.setPreviewFormat(PixelFormat.JPEG);
 		camera.startPreview();
 		
 	}
 	
 	public void surfaceDestroyed(SurfaceHolder holder){
 		camera.setPreviewCallback(null);
 		camera.stopPreview();
 		camera.release();
 		camera = null;
 	}
 	
 	public boolean onTouchEvent(MotionEvent event){
 		if(event.getAction() == MotionEvent.ACTION_DOWN){
 			takePicture();
 			camera.startPreview();
 		}
 		return true;
 	}
 	
 	public void takePicture(){
 		camera.takePicture(null, null,  new Camera.PictureCallback() {
 			public void onPictureTaken(byte[] data, Camera camera) {
 				sendData(getContext(), data);
 			}
 		});
 	}
 	
 	private void sendData(Context context, byte[] data){
 		Socket socket;
 		BufferedOutputStream out;
 		Log.d("MyApp", "Send Data:--------------------------");
 
 		try{
 			Log.d("MyApp", "Connecting...");
 			socket = new Socket(ADDRESS, PORT);
 			out = new BufferedOutputStream(socket.getOutputStream());
 			Log.d("MyApp", "Sending data...");
 			out.write(data);
 			Toast.makeText(mContext, "SUCCESS: Send Your Pic!", Toast.LENGTH_SHORT).show();
 			out.close();
 			socket.close();
 		} catch (Exception e){
 			e.printStackTrace();
 			Toast.makeText(mContext, "ERROR: Send Your Pic!", Toast.LENGTH_SHORT).show();
 			Log.e("MyApp", "Send Error:--------------------------");
 		}
		Log.d("MyApp", "End Send Data ----------------------------");
 	}
 }
