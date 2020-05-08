 package com.dylan.orzeye;
 
 import java.io.IOException;
 
 import com.dylan.orzeye.dictionary.DictionaryTool;
 import com.dylan.orzeye.dictionary.YoudaoJsonParser;
 import com.dylan.orzeye.dictionary.YoudaoTranslater;
 import com.dylan.orzeye.image.ImageProcessTool;
 import com.dylan.orzeye.ocr.OCRTool;
 
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.ImageFormat;
 import android.graphics.PixelFormat;
 import android.hardware.Camera;
 import android.hardware.Camera.PreviewCallback;
 import android.util.DisplayMetrics;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class CameraActivity extends Activity implements SurfaceHolder.Callback,
 		PreviewCallback {
 	private boolean isPreview = false;
 	private SurfaceView mPreviewSV = null;
 	private SurfaceHolder mSurfaceHolder = null;
 	private Camera mCamera = null;
 	private TextView recognizedView;
 	private TextView translatedView;
 	private ImageButton triggerButton;
 	private ImageButton dicWebSearchButton;
 	private DisplayMetrics dm;
 
 	private final String[] recognizedText = { new String("") };
 	private OCRTool mOCRTool;
 	private DictionaryTool mDictionaryTool;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.activity_camera);
 
 		mPreviewSV = (SurfaceView) findViewById(R.id.surfaceView);
 		mPreviewSV.setZOrderOnTop(false);
 		mSurfaceHolder = mPreviewSV.getHolder();
 		mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
 		mSurfaceHolder.addCallback(this);
 
 		triggerButton = (ImageButton) findViewById(R.id.triggerButton);
 		triggerButton.setOnClickListener(new TriggerButtonOnClickListener());
 
 		dicWebSearchButton = (ImageButton) findViewById(R.id.web_search_btn);
 		dicWebSearchButton
 				.setOnClickListener(new WebSearchButtonOnClickListener());
 
 		recognizedView = (TextView) findViewById(R.id.RecognizedView);
 		translatedView = (TextView) findViewById(R.id.TranslatedView);
 		dm = new DisplayMetrics();
 		getWindowManager().getDefaultDisplay().getMetrics(dm);
 
 		mOCRTool = new OCRTool();
 		mDictionaryTool = new DictionaryTool();
 	}
 
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 		initCamera();
 	}
 
 	public void surfaceCreated(SurfaceHolder holder) {
 		mCamera = Camera.open();
 		try {
 			mCamera.setPreviewDisplay(mSurfaceHolder);
 		} catch (IOException e) {
 			if (null != mCamera) {
 				mCamera.release();
 				mCamera = null;
 			}
 			e.printStackTrace();
 		}
 	}
 
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		if (null != mCamera) {
 			mCamera.setOneShotPreviewCallback(null);
 
 			mCamera.stopPreview();
 			isPreview = false;
 			mCamera.release();
 			mCamera = null;
 		}
 	}
 
 	private void initCamera() {
 		if (isPreview) {
 			mCamera.stopPreview();
 		}
 		if (null != mCamera) {
 			Camera.Parameters mCameraParam = mCamera.getParameters();
 			mCameraParam.setPictureFormat(ImageFormat.JPEG);
 			mCameraParam.setPreviewSize(mCameraParam.getSupportedPreviewSizes()
 					.get(1).width,
 					mCameraParam.getSupportedPreviewSizes().get(1).height);
 			mCamera.setDisplayOrientation(90);
 			mCameraParam
 					.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
 			mCamera.setParameters(mCameraParam);
 			mCamera.startPreview();
 			isPreview = true;
 		}
 	}
 
 	public DisplayMetrics getDisplayMetrics() {
 		return dm;
 	}
 
 	@Override
 	public void onPreviewFrame(final byte[] data, final Camera camera) {
 		if (null != data) {
 			enableTriggerButton(false);
 
			final Handler handler = showProgressDialog("识别中...", "请稍候...");
 
 			Thread thread = new Thread(new Runnable() {
 
 				@Override
 				public void run() {
 					String translatedText;
 					Bitmap ocrBitmap = ImageProcessTool.getOCRBitmapFromCamera(
 							data, camera, dm);
 					if (mOCRTool.isOCREngineReasy()
 							&& mDictionaryTool.isDictionaryReady()) {
 						recognizedText[0] = mOCRTool.OCRStart(ocrBitmap);
 						translatedText = mDictionaryTool
 								.lookUpDictionary(recognizedText[0]
 										.toLowerCase());
 					} else {
 						translatedText = "OCR Data or Dictionary Data can not be found!";
 					}
 					Message msg = new Message();
 					Bundle bundle = new Bundle();
 					bundle.putString("translatedtext", translatedText);
 					msg.setData(bundle);
 					handler.sendMessage(msg);
 				}
 			});
 			thread.start();
 
 			enableTriggerButton(true);
 
 		}
 	}
 
 	private void enableTriggerButton(boolean enable) {
 		triggerButton
 				.setOnClickListener(enable ? new TriggerButtonOnClickListener()
 						: null);
 		triggerButton.setEnabled(enable);
 	}
 
 	long waitTime = 2000;
 	long touchTime = 0;
 
 	@Override
 	public void onBackPressed() {
 		long currentTime = System.currentTimeMillis();
 		if ((currentTime - touchTime) >= waitTime) {
 			Toast.makeText(this, "Press again to quit.", Toast.LENGTH_SHORT)
 					.show();
 			touchTime = currentTime;
 		} else {
 			finish();
 		}
 	}
 
 	private Handler showProgressDialog(String title, String msg) {
 		final ProgressDialog dialog = new ProgressDialog(this);
 		dialog.setTitle(title);
 		dialog.setMessage(msg);
 		dialog.show();
 
 		final Handler handler = new Handler() {
 			public void handleMessage(android.os.Message msg) {
 				dialog.cancel();
 				if(msg.what != 1) {
 					recognizedView.setText(recognizedText[0]);
 					translatedView.setText(msg.getData().getString("translatedtext"));
 				}
 				
 			}
 		};
 		return handler;
 	}
 
 	class TriggerButtonOnClickListener implements OnClickListener {
 
 		@Override
 		public void onClick(View v) {
 			if (null != mCamera && isPreview) {
 				mCamera.setOneShotPreviewCallback(CameraActivity.this);
 			}
 		}
 	}
 
 	class WebSearchButtonOnClickListener implements OnClickListener {
 
 		@Override
 		public void onClick(View v) {
 			if (recognizedText[0].isEmpty()) {
 				Toast.makeText(CameraActivity.this, "Text is empty.",
 						Toast.LENGTH_SHORT).show();
 			} else {
 
				final Handler handler = showProgressDialog("查询中...", "请稍候...");
 
 				Thread thread = new Thread(new Runnable() {
 
 					@Override
 					public void run() {
 						YoudaoJsonParser youdaoJsonParser = YoudaoTranslater
 								.translate(recognizedText[0]);
 						if(youdaoJsonParser != null) {
 							Intent intent = new Intent();
 							intent.setClass(CameraActivity.this, WebTranslationActivity.class);
 							intent.putExtra("word", recognizedText[0]);
 							intent.putExtra("phonetic", youdaoJsonParser.getPhonetic());
 							intent.putExtra("basicTanslation", youdaoJsonParser.getBasicTanslation());
 							intent.putExtra("webTanslation", youdaoJsonParser.getWebTanslation());
 							startActivity(intent);
 						}
 						
 						handler.sendEmptyMessage(1);
 					}
 				});
 				thread.start();
 
 			}
 		}
 	}
 }
