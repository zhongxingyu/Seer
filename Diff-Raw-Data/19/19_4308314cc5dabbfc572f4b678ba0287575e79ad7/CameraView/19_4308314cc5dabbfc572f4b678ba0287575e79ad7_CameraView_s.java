 package rja.android.camera;
 
 import android.content.Context;
 import android.hardware.Camera;
 import android.hardware.Camera.Parameters;
 import android.hardware.Camera.Size;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.SurfaceView;
 import android.view.SurfaceHolder;
 
 import java.io.IOException;
 import java.util.List;
 
 /**
  * Describe class CameraView here.
  *
  *
  * Created: Mon Dec 06 17:46:56 2010
  *
  * @author <a href="mailto:rajeshja@D-174758"></a>
  * @version 1.0
  */
 public class CameraView extends SurfaceView implements SurfaceHolder.Callback {
 
 	public Camera camera;
 
 	private static final String LOG_CAT = "CameraView";
 
 	public CameraView(Context context) {
 		super(context);
 		
 		getHolder().addCallback(this);
 		getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 	}
 
 	public CameraView(Context context, AttributeSet  attrs) {
 		super(context, attrs);
 
 		getHolder().addCallback(this);
 		getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 	}
 	
 	public CameraView(Context context, AttributeSet  attrs, int defStyle) {
 		super(context, attrs, defStyle);
 
 		getHolder().addCallback(this);
 		getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 	}
 
 	public void releaseCamera() {
 		if (camera != null) {
 			camera.stopPreview();
 			camera.release();
 			camera = null;
 		}
 	}
 
 	public void reopenCamera() {
 		if (camera == null) {
 			camera = Camera.open();
 		}
 	}
 
 	public void surfaceCreated(SurfaceHolder holder) {
 		if (camera == null) {
 			camera = Camera.open();
 		}
 		try {
 			camera.setPreviewDisplay(getHolder());
 		} catch (IOException e) {
 			releaseCamera();
 		}
 	}
 
 	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
 		Parameters cameraParams = camera.getParameters();
 		List<Size> sizes = cameraParams.getSupportedPreviewSizes();
 
 		Size selectedSize = selectSize(sizes, width, height);
 
 		Log.d(LOG_CAT, "Selected size " + selectedSize.width + "x" + selectedSize.height);
 
 		cameraParams.setPreviewSize(selectedSize.width, selectedSize.height);
 		camera.setParameters(cameraParams);
 		camera.startPreview();
 	}
 
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		if (camera != null) {
 			releaseCamera();
 		}
 	}
 
 	private Size selectSize(List<Size> sizes, int width, int height) {
 		
		Size selectedSize;

		if ((sizes != null) && (sizes.size()>=0)) {
			selectedSize = sizes.get(0);
		} else {
			selectedSize = null;
		}
 
 		for(Size size: sizes) {
 			if ((size.width <= (width - 20)) && (size.height <= (height - 20))) {
 				if ((selectedSize == null)
 					|| ((selectedSize.width < size.width)
 						&& (selectedSize.height < size.height))) {
 					selectedSize = size;
 				}
 			}
 		}
 
 		return selectedSize;
 	}
 }
