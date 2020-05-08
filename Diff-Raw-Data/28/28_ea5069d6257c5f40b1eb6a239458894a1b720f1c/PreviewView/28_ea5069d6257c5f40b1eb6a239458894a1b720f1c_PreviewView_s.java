 package test.ThreeDAR.beta;
 
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.PixelFormat;
 import android.hardware.Camera;
 import android.hardware.Camera.Parameters;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 // This class defines the custom SurfaceView for previewing images from the camera 
 public class PreviewView extends SurfaceView implements SurfaceHolder.Callback{
 	
 	private Camera 			_camera;
 	private SurfaceHolder 	_previewHolder;
 
 	public PreviewView(Context context) {
 		super(context);
 		_previewHolder = this.getHolder();
 		_previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); //Not sure what it is...
 		_previewHolder.addCallback(this);
 		
 	}
 	
 	
 	//Call back functions for surfaceHolder:
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
 	    //Reset the parameters of the camera:  
 		Parameters params = _camera.getParameters();
		List<Camera.Size> previewSizes = params.getSupportedPreviewSizes();
		params.setPreviewSize(320, 240);
 		params.setPictureFormat(PixelFormat.JPEG);
 		_camera.setParameters(params);
 		_camera.startPreview();
 		
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 		//Open the camera and set the preview display
 		_camera = Camera.open();
 		try {
 			_camera.setPreviewDisplay(_previewHolder);
 		}
 		catch( Exception e){
 			Log.v("DEBUG", e.toString());
 		}
 		
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		//Close the camera:
 		_camera.stopPreview();
 		_camera.release();
 		
 	}
 
 }
