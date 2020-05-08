 /*
  * Copyright 2012 Dmytro Titov
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.github.pickncrop;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.UUID;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.CompressFormat;
 import android.graphics.Matrix;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.Display;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup.LayoutParams;
 import android.view.ViewManager;
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 import android.widget.RelativeLayout;
 import android.widget.Toast;
 import android.widget.ZoomButtonsController;
 import android.widget.ZoomButtonsController.OnZoomListener;
 
 public class MainActivity extends Activity {
 	private ImageView imageView;
 	private ViewManager viewManager;
 	private Matrix matrix;
 	private int size;
 	private final int outputSize = 100;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		Display display = getWindowManager().getDefaultDisplay();
 		int width = display.getWidth();
 		int height = display.getHeight();
 		size = width < height ? width : height;
 		size -= 50;
 
 		imageView = (ImageView) findViewById(R.id.imageViewCrop);
 		imageView.getLayoutParams().width = size;
 		imageView.getLayoutParams().height = size;
 		viewManager = (ViewManager) imageView.getParent();
 
 		if (!getPackageManager().hasSystemFeature(
 				PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH)) {
 			createZoomControls();
 		}
 
 		imageView.setOnTouchListener(new OnTouchListener() {
 			float initX;
 			float initY;
 			float scale;
 			float initDistance;
 			float currentDistance;
 			boolean isMultitouch = false;
 
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				switch (event.getAction() & MotionEvent.ACTION_MASK) {
 				case MotionEvent.ACTION_DOWN:
 					initX = event.getX();
 					initY = event.getY();
 					break;
 				case MotionEvent.ACTION_POINTER_DOWN:
 					isMultitouch = true;
 					initDistance = (float) Math.sqrt(Math.pow(
 							initX - event.getX(1), 2)
 							+ Math.pow(initY - event.getY(1), 2));
 					break;
 				case MotionEvent.ACTION_MOVE:
 					if (isMultitouch) {
 						matrix = imageView.getImageMatrix();
 						currentDistance = (float) Math.sqrt(Math.pow(initX
 								- event.getX(1), 2)
 								+ Math.pow(initY - event.getY(1), 2));
						scale = currentDistance / initDistance;
 						matrix.postScale(scale, scale, 0.5f * size, 0.5f * size);
 						imageView.setImageMatrix(matrix);
 						imageView.invalidate();
 					} else {
 						imageView.scrollBy((int) (initX - event.getX()),
 								(int) (initY - event.getY()));
 						initX = event.getX();
 						initY = event.getY();
 					}
 					break;
 				case MotionEvent.ACTION_UP:
 					isMultitouch = false;
 					break;
 				case MotionEvent.ACTION_POINTER_UP:
 					isMultitouch = false;
 					break;
 				}
 				return true;
 			}
 		});
 	}
 
 	public void createZoomControls() {
 		ZoomButtonsController zoomButtonsController = new ZoomButtonsController(
 				imageView);
 		zoomButtonsController.setVisible(true);
 		zoomButtonsController.setAutoDismissed(false);
 		zoomButtonsController.setOnZoomListener(new OnZoomListener() {
 
 			@Override
 			public void onZoom(boolean zoomIn) {
 				matrix = imageView.getImageMatrix();
 				if (zoomIn) {
 					matrix.postScale(1.05f, 1.05f, 0.5f * size, 0.5f * size);
 					imageView.setImageMatrix(matrix);
 				} else {
 					matrix.postScale(0.95f, 0.95f, 0.5f * size, 0.5f * size);
 					imageView.setImageMatrix(matrix);
 				}
 				imageView.invalidate();
 			}
 
 			@Override
 			public void onVisibilityChanged(boolean visible) {
 			}
 		});
 		RelativeLayout.LayoutParams zoomLayoutParams = new RelativeLayout.LayoutParams(
 				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 		zoomLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
 		zoomLayoutParams.addRule(RelativeLayout.BELOW, R.id.imageViewCrop);
 		viewManager.addView(zoomButtonsController.getContainer(),
 				zoomLayoutParams);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		Toast.makeText(this, R.string.info, 5).show();
 		return super.onMenuItemSelected(featureId, item);
 	}
 
 	public void buttonPickClick(View view) {
 		Intent intent = new Intent(Intent.ACTION_PICK,
 				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
 		startActivityForResult(intent, 0);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 
 		switch (resultCode) {
 		case RESULT_OK:
 			Uri targetUri = data.getData();
 			imageView.setScaleType(ScaleType.CENTER_INSIDE);
 			imageView.scrollTo(0, 0);
 			imageView.setImageURI(targetUri);
 			imageView.setScaleType(ScaleType.MATRIX);
 			break;
 		}
 	}
 
 	public void buttonCropClick(View view) throws IOException {
 		imageView.setDrawingCacheEnabled(true);
 		imageView.buildDrawingCache(true);
 		File imageFile = new File(Environment.getExternalStorageDirectory(),
 				"Pictures/" + UUID.randomUUID().toString() + ".jpg");
 		FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
 		Bitmap.createScaledBitmap(imageView.getDrawingCache(true), outputSize,
 				outputSize, false).compress(CompressFormat.JPEG, 100,
 				fileOutputStream);
 		fileOutputStream.close();
 		imageView.setDrawingCacheEnabled(false);
 	}
 }
