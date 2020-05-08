 package mmb.foss.aueb.icong.dialogs;
 
 import java.io.IOException;
 
 import android.annotation.TargetApi;
 import android.content.Context;
 import android.hardware.Camera;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 @TargetApi(8)
 public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback 
 {
 	private SurfaceHolder mHolder;
 	private Camera camera;
 	public CameraPreview(Context context, Camera camera) 
 	{
 	super(context);
 	this.camera = camera;
 	camera.setDisplayOrientation(90);
 	mHolder = getHolder();
 	mHolder.addCallback(this);
 	//WTFFFFFF ? ? ? ? ? y android u no recognize ?
 	//mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 	// fu android 
 	}
 	
 	public void surfaceCreated(SurfaceHolder holder) 
 	{
 		try 
 		{
 			camera.setPreviewDisplay(holder);
 			camera.startPreview();
 		}
 		catch(Exception e) 
 		{
 			
 		}
 	}
 	
 	public void surfaceDestroyed(SurfaceHolder holder)
 	{
 		camera.stopPreview();
		camera.release();
 	}
 	
 	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h)
 	{
 		if (mHolder.getSurface() == null)
 		{
 			return;
 		}
 		try 
 		{
 			camera.stopPreview();
 		} 
 		catch (Exception e)
 		{
 			
 		}
 		try 
 		{
 			camera.setPreviewDisplay(mHolder);
 			camera.startPreview();
 		}
 		catch(Exception e)
 		{
 			
 		}
 	}
 	
 }
