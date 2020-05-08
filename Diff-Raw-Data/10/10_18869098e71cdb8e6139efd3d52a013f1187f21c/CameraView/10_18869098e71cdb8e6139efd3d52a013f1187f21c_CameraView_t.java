 package rja.android.camera;
 
 import java.io.IOException;
 import java.util.List;
 
 import android.content.Context;
 import android.hardware.Camera;
 import android.hardware.Camera.Parameters;
 import android.hardware.Camera.Size;
 import android.util.AttributeSet;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.ViewGroup;
 
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
 
 	private Camera camera;
 	private DisplayMetrics metrics;
 	private AROverlay overlay;
 
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
 
 	public void setMetrics(DisplayMetrics metrics) {
 		this.metrics = metrics;
 	}
 
 	public void setOverlay(AROverlay overlay) {
 		this.overlay = overlay;
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
 
 		Log.d(LOG_CAT, "Density is " + metrics.density);
 
 		Size selectedSize = selectSize(sizes, 
 									   Math.round(width*metrics.density), 
 									   Math.round(height*metrics.density));
 
 		ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) this.getLayoutParams();
		int leftMargin = layoutParams.leftMargin + Math.round(((width*metrics.density)-selectedSize.width)
															  /(2*metrics.density));
		int topMargin = layoutParams.topMargin + Math.round(((height*metrics.density)-selectedSize.height)
															/(2*metrics.density));
		int rightMargin = layoutParams.rightMargin + (Math.round(width*metrics.density)-selectedSize.width)-leftMargin;
		int bottomMargin = layoutParams.bottomMargin + (Math.round(height*metrics.density)-selectedSize.height)-topMargin;
 		
 		layoutParams.setMargins(leftMargin, topMargin,rightMargin,bottomMargin);
 		this.setLayoutParams(layoutParams);
 
 		//Do we need to create a new layoutParams object 
 		//or can we reuse the one created before?
 		ViewGroup.MarginLayoutParams overlayLayoutParams = 
 			(ViewGroup.MarginLayoutParams) overlay.getLayoutParams();
 		overlayLayoutParams.setMargins(leftMargin, topMargin,rightMargin,bottomMargin);
 		overlay.setLayoutParams(overlayLayoutParams);
  
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
 
 		Log.d(LOG_CAT, "Target size  " + width + "x" + height);
 		
 		Size selectedSize = null;
 
 		for(Size size: sizes) {
 			Log.d(LOG_CAT, "Trying  " + size.width + "x" + size.height);
 			if ((size.width <= width) && (size.height <= height)) {
 
 				if ((selectedSize == null)
 					|| ((selectedSize.width < size.width)
 						&& (selectedSize.height < size.height))) {
 					selectedSize = size;
 					Log.d(LOG_CAT, "Selected value: " + selectedSize.width + "x" + selectedSize.height);
 				}
 
 			}
 		}
 
 		if ((selectedSize == null) && (sizes != null) && (sizes.size()>0)) {
 			selectedSize = sizes.get(0);
 		}
 
 		Log.d(LOG_CAT, "Final value: " + selectedSize.width + "x" + selectedSize.height);
 
 		return selectedSize;
 	}
 }
