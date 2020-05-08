 package com.gromsoft.onlyroad;
 
 import java.io.IOException;
 
 import android.content.Context;
 import android.hardware.Camera;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 
 public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
 
	private SurfaceHolder mHolder; // Контейнер для превью
 	private Camera mCamera;
 	private final static String TAG = "MyLog";
 
	// Конструктор
 	public CameraPreview(Context context, Camera camera) {
 		super(context);
 		mCamera = camera;
 		mHolder = getHolder();
 		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); // C API 11 (Android 3.0) depricated, ничего не значит
 	}
 
 	public void surfaceCreated(SurfaceHolder holder) {
		// Превью подготовлено, стартуем
 		try {
 			mCamera.setPreviewDisplay(holder);
 			mCamera.startPreview();
 		} catch (IOException e) {
 			Log.d(TAG, "Error setting camera preview: " + e.getMessage());
 		}
 	}
 
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		// empty. Take care of releasing the Camera preview in your activity.
 	}
 
 	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// Всегда перед поворотом, ресайзом и т.п.
 		if (mHolder.getSurface() == null) {
			// нет камеры
 			return;
 		}
		// Остановить перед поворотом
 		try {
 			mCamera.stopPreview();
 		} catch (Exception e) {
 		}
		// Тут всякие обработки
 		try {
 			mCamera.setPreviewDisplay(mHolder);
 			mCamera.startPreview();
 		} catch (Exception e) {
 			Log.d(TAG, "Error starting camera preview: " + e.getMessage());
 		}
 	}
 	
 }
