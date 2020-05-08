 package com.zoeetrope.lineupcamera;
 
 import java.io.IOException;
 import java.lang.reflect.Method;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.hardware.Camera;
 import android.hardware.Camera.PictureCallback;
 import android.hardware.Camera.Size;
 import android.os.AsyncTask;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.view.SurfaceHolder.Callback;
 import android.view.SurfaceView;
 import android.view.View;
 import android.widget.FrameLayout;
 
 import com.zoeetrope.lineupcamera.model.Album;
import com.zoeetrope.lineupcamera.model.Image;
 
 public class CameraPreview extends SurfaceView implements Callback,
 		View.OnClickListener {
 
 	private final static String TAG = "CameraPreview";
 
 	private Camera mCamera;
 	private SurfaceHolder mHolder;
 	private CameraOverlay mOverlay;
 	private Album mAlbum;
 
 	public CameraPreview(Context context) {
 		super(context);
 
 		initialize();
 	}
 
 	public CameraPreview(Context context, AttributeSet attributes) {
 		super(context, attributes);
 
 		initialize();
 	}
 
 	public CameraPreview(Context context, AttributeSet attributes, int defStyle) {
 		super(context, attributes, defStyle);
 
 		initialize();
 	}
 
 	private void initialize() {
 		mHolder = getHolder();
 		mHolder.addCallback(this);
 		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 
 		mAlbum = new Album("");
 
 		this.setOnClickListener(this);
 	}
 
 	public void setCamera(Camera camera) {
 		this.mCamera = camera;
 		setupCameraPreview();
 		requestLayout();
 	}
 
 	public void setOverlay(CameraOverlay overlay) {
 		this.mOverlay = overlay;
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 		if (mCamera != null) {
 			try {
 				mCamera.setPreviewDisplay(holder);
 				mCamera.startPreview();
 
 				requestLayout();
 			} catch (IOException e) {
 				Log.d(TAG, "Error setting camera preview: " + e.getMessage());
 			}
 		}
 	}
 
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 		if (mHolder.getSurface() == null) {
 			return;
 		}
 
 		setupCameraPreview();
 	}
 
 	private void setupCameraPreview() {
 		try {
 			mCamera.stopPreview();
 		} catch (Exception e) {
 		}
 
 		try {
 			Camera.Parameters parameters = mCamera.getParameters();
 			List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
 
 			Camera.Size cs = sizes.get(0);
 			parameters.setPictureSize(cs.width, cs.height);
 
 			sizes = parameters.getSupportedPreviewSizes();
 			cs = getOptimalPreviewSize(sizes);
 
 			setDisplayOrientation(mCamera, 0);
 			setLayoutParams(new FrameLayout.LayoutParams(cs.width, cs.height));
 			parameters.setPreviewSize(cs.width, cs.height);
 			mOverlay.setSize(cs.width, cs.height);
 
 			parameters.setJpegQuality(90);
 
 			mCamera.setPreviewDisplay(mHolder);
 			mCamera.setParameters(parameters);
 			mCamera.startPreview();
 
 			new LoadImageTask().execute();
 		} catch (Exception e) {
 			Log.d(TAG, "Error starting camera preview: " + e.getMessage());
 		}
 	}
 
 	private Size getOptimalPreviewSize(List<Size> previewSizes) {
 		Camera.Size optimalSize = null;
 
 		Collections.sort(previewSizes, new Comparator<Camera.Size>() {
 			@Override
 			public int compare(Size lhs, Size rhs) {
 				return rhs.height - lhs.height;
 			}
 		});
 
 		Iterator<Size> iterator = previewSizes.iterator();
 		Log.d(TAG, "my height: " + getHeight());
 		while (iterator.hasNext()) {
 			Camera.Size cs = iterator.next();
 
 			if (cs.height <= getHeight()) {
 				optimalSize = cs;
 				break;
 			}
 		}
 
 		return optimalSize;
 	}
 
 	protected void setDisplayOrientation(Camera camera, int angle) {
 		Method downPolymorphic;
 		try {
 			downPolymorphic = camera.getClass().getMethod(
 					"setDisplayOrientation", new Class[] { int.class });
 			if (downPolymorphic != null)
 				downPolymorphic.invoke(camera, new Object[] { angle });
 		} catch (Exception e1) {
 		}
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder arg0) {
 
 	}
 
 	@Override
 	public void onClick(View v) {
 		if (mCamera != null) {
 			mCamera.takePicture(null, null, null, mPicture);
 		}
 	}
 
 	private PictureCallback mPicture = new PictureCallback() {
 
 		@Override
 		public void onPictureTaken(byte[] data, Camera camera) {
 			mAlbum.saveNewImage(data);
 
 			new LoadImageTask().execute();
 
 			camera.startPreview();
 		}
 
 	};
 
 	public void setAlbumName(String name) {
 		mAlbum.setName(name);
 	}
 
 	private class LoadImageTask extends AsyncTask<Void, Integer, Bitmap> {
 
 		@Override
 		protected Bitmap doInBackground(Void... params) {
			Image latestImage = mAlbum.getLatestImage();

			if (latestImage != null) {
				return latestImage.getResizedBitmap(mOverlay.getWidth(),
						mOverlay.getHeight());
			}

			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Bitmap result) {
			if (result != null) {
				mOverlay.setImage(result);
				requestLayout();
			}
 		}
 
 	};
 
 }
