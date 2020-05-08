 package il.ac.huji.shoppit;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import android.app.Activity;
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.app.FragmentTransaction;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.ImageFormat;
 import android.graphics.Matrix;
 import android.graphics.Rect;
 import android.graphics.YuvImage;
 import android.hardware.Camera;
 import android.hardware.Camera.Parameters;
 import android.hardware.Camera.Size;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Surface;
 import android.view.SurfaceHolder;
 import android.view.SurfaceHolder.Callback;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.Toast;
 
 import com.google.zxing.*;
 import com.google.zxing.common.HybridBinarizer;
 import com.google.zxing.oned.EAN13Reader;
 
 /**
  * @author Elie2
  * This fragment is used to take a picture. It is used by both NewItemActivity and NewShopActivity.
  */
 public class CameraFragment extends Fragment {
 
 	public static final String TAG = "CameraFragment";
 
 	private Camera camera;
 	private SurfaceView surfaceView;
 	private ImageButton photoButton;
 	private Button barcodeButton;
 	private boolean barcodeMode = false,
 			currentlyScanning = false;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
 			Bundle savedInstanceState) {
 
 		barcodeMode = false;
 		currentlyScanning = false;
 		View v = inflater.inflate(R.layout.fragment_camera, parent, false);
 
 		photoButton = (ImageButton) v.findViewById(R.id.camera_photo_button);
 		barcodeButton = (Button) v.findViewById(R.id.barcode_button);
 
 		// hide barcode button when adding shop
 		if (getActivity().getClass() == NewShopActivity.class) {
 			barcodeButton.setVisibility(View.INVISIBLE);
 		}
 
 		if (camera == null) {
 			try {
 				camera = Camera.open();
 				photoButton.setEnabled(true);
 			} catch (Exception e) {
 				Log.e(TAG, "No camera with exception: " + e.getMessage());
 				photoButton.setEnabled(false);
 				Toast.makeText(getActivity(), "No camera detected",
 						Toast.LENGTH_LONG).show();
 			}
 		}
 
 
 		//Barcode scanning is allowed only if the camera is of type NV21.
 		//Don't really know what that is, but that's the only option I've seen when scanning
 		//for a barcode. Seems like it's the most probable option though.
 		Parameters parameters = camera.getParameters();
 		int imageFormat = parameters.getPreviewFormat();
 		if (imageFormat != ImageFormat.NV21)
 			barcodeButton.setVisibility(View.INVISIBLE);
 
 		//Define the focus for the picture taking mode, which is the initial mode
 		parameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
 		camera.setParameters(parameters);
 
 
 
 		//This part will search for a barcode in the image.
 		camera.setPreviewCallback(new Camera.PreviewCallback() {
 
 			@Override
 			public void onPreviewFrame(byte[] data, Camera camera) {
 
 				if (barcodeMode && !currentlyScanning) {
 
 					currentlyScanning = true;
 
 					//Construct the image and rotate it
 					Bitmap bMap;
 					{
 						Parameters params = camera.getParameters();
 						Size size = params.getPictureSize();
 						int width = size.width,
 								height = size.height;
 						YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, width, height, null);
 						ByteArrayOutputStream baos = new ByteArrayOutputStream();
 						yuvimage.compressToJpeg(new Rect(0, 0, width, height), 100, baos);
 						byte[] jdata = baos.toByteArray();
 						bMap = BitmapFactory.decodeByteArray(jdata, 0, jdata.length);
 					}
 
 					//Try to find a barcode
 					String result = decodeBitmap(bMap);
 					if (result != null) {
 						Toast.makeText(getActivity(), result,
 								Toast.LENGTH_LONG).show();
 						camera.setPreviewCallback(null);
 					}
 
 					currentlyScanning = false;
 				}
 			}
 		});
 
 
 		photoButton.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if (camera == null)
 					return;
 
 				camera.takePicture(new Camera.ShutterCallback() {
 
 					@Override
 					public void onShutter() {
 						// nothing to do
 					}
 
 				}, null, new Camera.PictureCallback() {
 
 					@Override
 					public void onPictureTaken(byte[] data, Camera camera) {
 
 						//Crop and rotate the image and get the new data
 						Bitmap image = rotate(crop(data));
 						ByteArrayOutputStream bos = new ByteArrayOutputStream();
 						image.compress(Bitmap.CompressFormat.JPEG, 100, bos);
 						data = bos.toByteArray();
 
 						//if (!barcodeMode)
 						addPhotoToShopAndReturn(data);
 						/*else {
 
 							//TEST CODE
 							File outFile = new File(Environment.getExternalStorageDirectory(), "barcode.jpg");
 							Bitmap finalBitmap = BitmapFactory.decodeByteArray(rotatedData, 0, rotatedData.length);
 							FileOutputStream out = null;
 							try {
 								outFile.createNewFile();
 								out = new FileOutputStream(outFile);
 								finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
 								out.flush();
 							} catch (Exception e) {}
 							try {
 								out.close();
 							} catch (Exception e) {}
 							finalBitmap.recycle();
 
 							Bitmap bMap = BitmapFactory.decodeFile(outFile.getAbsolutePath());
 
 							int w = bMap.getWidth();
 							int h = bMap.getHeight();
 							int [] argb = new int[w * h];
 							bMap.getPixels(argb, 0, w, 0, 0, w, h);
 							byte [] yuv = new byte[w*h*3/2];
 							encodeYUV420SP(yuv, argb, w, h);
 							bMap.recycle();
 
 							LuminanceSource source = new PlanarYUVLuminanceSource(yuv, w, h, 0, 0, w, h, false);
 							BinaryBitmap binBmp = new BinaryBitmap(new HybridBinarizer(source));
 
 							EAN13Reader reader = new EAN13Reader();
 							reader.reset();
 							Result result = null;
 
 							try {
 								result = reader.decode(binBmp);
 								Log.d("BARCODE", result.getText());
 							} catch (NotFoundException e1) {
 								e1.printStackTrace();
 							} catch (FormatException e1) {
 								e1.printStackTrace();
 							}
 							Toast.makeText(getActivity(), result == null ? "No barcode found" :
 								result.toString(), Toast.LENGTH_LONG).show();
 
 							//							outFile.delete();
 
 						}*/
 					}
 
 				});
 
 			}
 		});
 
 		barcodeButton.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 
 				barcodeMode = !barcodeMode;
 				if (barcodeMode) {
 					photoButton.setVisibility(View.INVISIBLE);
 					barcodeButton.setText("Take item picture");
 				}
 				else {
 					photoButton.setVisibility(View.VISIBLE);
 					barcodeButton.setText("Scan barcode");
 				}
 
 			}
 
 		});
 
 		surfaceView = (SurfaceView) v.findViewById(R.id.camera_surface_view);
 		SurfaceHolder holder = surfaceView.getHolder();
 		holder.addCallback(new Callback() {
 
 			public void surfaceCreated(SurfaceHolder holder) {
 				try {
 					if (camera != null) {
 						//						camera.setDisplayOrientation(90);
 						setCameraDisplayOrientation(getActivity(), 0, camera);
 						camera.setPreviewDisplay(holder);
 						camera.startPreview();
 					}
 				} catch (IOException e) {
 					Log.e(TAG, "Error setting up preview", e);
 				}
 			}
 
 			public void surfaceChanged(SurfaceHolder holder, int format,
 					int width, int height) {
 
 				// If your preview can change or rotate, take care of those events here.
 				// Make sure to stop the preview before resizing or reformatting it.
 
 				/*if (surfaceView.getHolder().getSurface() == null) {
 					// preview surface does not exist
 					return;
 				}
 				Log.d(TAG, "Here");
 				// stop preview before making changes
 				try {
 					camera.stopPreview();
 				} catch (Exception e) {
 					// ignore: tried to stop a non-existent preview
 				}
 				// set preview size and make any resize, rotate or
 				// reformatting changes here
 				// start preview with new settings
 				try {
 					setCameraDisplayOrientation(getActivity(), 0, camera);
 					camera.setPreviewDisplay(holder);
 					camera.startPreview();
 
 				} catch (Exception e) {
 					Log.d(TAG, "Error starting camera preview: " + e.getMessage());
 				}*/
 			}
 
 			public void surfaceDestroyed(SurfaceHolder holder) {
 				// nothing here
 			}
 
 		});
 
 		return v;
 	}
 
 
 
 	//We can erase this code if barcode works.
 
 	//Helper functions for getting the image from the camera in the right format
 	byte [] getNV21(int inputWidth, int inputHeight, Bitmap scaled) {
 
 		int [] argb = new int[inputWidth * inputHeight];
 
 		scaled.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight);
 
 		byte [] yuv = new byte[inputWidth*inputHeight*3/2];
 		encodeYUV420SP(yuv, argb, inputWidth, inputHeight);
 
 		scaled.recycle();
 
 		return yuv;
 	}
 
 	void encodeYUV420SP(byte[] yuv420sp, int[] argb, int width, int height) {
 		final int frameSize = width * height;
 
 		int yIndex = 0;
 		int uvIndex = frameSize;
 
 		int a, R, G, B, Y, U, V;
 		int index = 0;
 		for (int j = 0; j < height; j++) {
 			for (int i = 0; i < width; i++) {
 
 				a = (argb[index] & 0xff000000) >> 24; // a is not used obviously
 			R = (argb[index] & 0xff0000) >> 16;
 			G = (argb[index] & 0xff00) >> 8;
 			B = (argb[index] & 0xff) >> 0;
 
 			// well known RGB to YUV algorithm
 			Y = ( (  66 * R + 129 * G +  25 * B + 128) >> 8) +  16;
 			U = ( ( -38 * R -  74 * G + 112 * B + 128) >> 8) + 128;
 			V = ( ( 112 * R -  94 * G -  18 * B + 128) >> 8) + 128;
 
 			// NV21 has a plane of Y and interleaved planes of VU each sampled by a factor of 2
 			//    meaning for every 4 Y pixels there are 1 V and 1 U.  Note the sampling is every other
 			//    pixel AND every other scanline.
 			yuv420sp[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
 			if (j % 2 == 0 && index % 2 == 0) { 
 				yuv420sp[uvIndex++] = (byte)((V<0) ? 0 : ((V > 255) ? 255 : V));
 				yuv420sp[uvIndex++] = (byte)((U<0) ? 0 : ((U > 255) ? 255 : U));
 			}
 
 			index ++;
 			}
 		}
 	}
 
 
 	private Bitmap rotate(Bitmap image) {
 		Matrix matrix = new Matrix();
 		matrix.postRotate(90);
 		return Bitmap.createBitmap(image, 0,
 				0, image.getWidth(), image.getHeight(),
 				matrix, true);
 	}
 
 
 	private Bitmap crop(byte[] data) {
 
 		Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length);
 
 		if (image.getWidth() >= image.getHeight()) {
 			return Bitmap.createBitmap(image, 
 					image.getWidth()/2 - image.getHeight()/2,
 					0,
 					image.getHeight(), 
 					image.getHeight()
 					);
 		}
 		else {
 			return Bitmap.createBitmap(
 					image,
 					0, 
 					image.getHeight()/2 - image.getWidth()/2,
 					image.getWidth(),
 					image.getWidth() 
 					);
 		}
 	}
 
 
 	/**
 	 * Returns the barcode in the image, or null if not found.
 	 * @param image
 	 * @return
 	 */
 	String decodeBitmap(Bitmap image) {
 
 		int w = image.getWidth();
 		int h = image.getHeight();
 		int [] argb = new int[w * h];
 		image.getPixels(argb, 0, w, 0, 0, w, h);
 		byte [] yuv = new byte[w*h*3/2];
 		encodeYUV420SP(yuv, argb, w, h);
 		//bMap.recycle();
 		LuminanceSource source = new PlanarYUVLuminanceSource(yuv, w, h, 0, 0, w, h, false);
 		BinaryBitmap binBmp = new BinaryBitmap(new HybridBinarizer(source));
 
 		EAN13Reader reader = new EAN13Reader();
 		reader.reset();
 		Result result = null;
 
 		try {
 			result = reader.decode(binBmp);
 			Log.d("BARCODE", result.getText());
 			return result.getText();
 		} catch (Exception e) {
 			//e.printStackTrace();
 		}
 
 		return null;
 
 	}
 
 
 	/* we don't actually want this scaling/resizing, since it makes the picture too small,
 	 * but we might want to do some other scaling, so I've left the code in for now.
 	 * 
 	 * ParseQueryAdapter loads ParseFiles into a ParseImageView at whatever size
 	 * they are saved. Since we never need a full-size image in our app, we'll
 	 * save a scaled one right away.
 	 */
 	//	@SuppressWarnings("unused")
 	//	private void saveScaledPhoto(byte[] data) {
 	//		
 	//		// Resize photo from camera byte array
 	//		Bitmap shopImage = BitmapFactory.decodeByteArray(data, 0, data.length);
 	//		Bitmap shopImageScaled = Bitmap.createScaledBitmap(shopImage, 200, 200
 	//				* shopImage.getHeight() / shopImage.getWidth(), false);
 	//
 	//		Matrix matrix = new Matrix();
 	//		matrix.postRotate(90);
 	//		Bitmap rotatedScaledShopImage = Bitmap.createBitmap(shopImageScaled, 0,
 	//				0, shopImageScaled.getWidth(), shopImageScaled.getHeight(),
 	//				matrix, true);
 	//
 	//		ByteArrayOutputStream bos = new ByteArrayOutputStream();
 	//		rotatedScaledShopImage.compress(Bitmap.CompressFormat.JPEG, 100, bos);
 	//
 	//		byte[] scaledData = bos.toByteArray();
 	//		
 	//		addPhotoToShopAndReturn(scaledData);
 	//	}
 
 	//	private void saveScaledPhoto(byte[] data) {
 	//
 	//		Bitmap image = BitmapFactory.decodeByteArray(data, 0, data.length);
 	//
 	//		Matrix matrix = new Matrix();
 	//		matrix.postRotate(90);
 	//		Bitmap rotatedShopImage = Bitmap.createBitmap(image, 0,
 	//				0, image.getWidth(), image.getHeight(),
 	//				matrix, true);
 	//
 	//		ByteArrayOutputStream bos = new ByteArrayOutputStream();
 	//		rotatedShopImage.compress(Bitmap.CompressFormat.JPEG, 100, bos);
 	//
 	//		byte[] scaledData = bos.toByteArray();
 	//
 	//		addPhotoToShopAndReturn(scaledData);
 	//	}
 
 	private void addPhotoToShopAndReturn(byte[] data) {
 
 		if (getActivity().getClass() == NewShopActivity.class) {
 			Log.i(TAG, "NewShopActivity");
 			((NewShopActivity) getActivity()).setCurrentPhotoData(data);
 
 			FragmentManager fm = getActivity().getFragmentManager();
 			fm.popBackStack("NewShopFragment",
 					FragmentManager.POP_BACK_STACK_INCLUSIVE);
 		} else if (getActivity().getClass() == NewItemActivity.class) {
 			Log.i(TAG, "NewItemActivity");
 			((NewItemActivity) getActivity()).setCurrentPhotoData(data);
 
 			Fragment cameraFragment = new NewItemFragment();
 			FragmentTransaction transaction = getActivity().getFragmentManager()
 					.beginTransaction();
 			transaction.replace(R.id.fragmentContainer, cameraFragment);
 			transaction.addToBackStack("NewItemFragment");
 			transaction.commit();
 
 		} else {
 			Log.e(TAG, "error in addPhotoToShopAndReturn");
 		}
 
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 
 		if (camera == null) {
 			try {
 				camera = Camera.open();
 				photoButton.setEnabled(true);
 			} catch (Exception e) {
 				Log.i(TAG, "No camera: " + e.getMessage());
 				photoButton.setEnabled(false);
 				Toast.makeText(getActivity(), "No camera detected",
 						Toast.LENGTH_LONG).show();
 			}
 		}
 	}
 
 	@Override
 	public void onPause() {
 		if (camera != null) {
 			camera.stopPreview();
 			camera.release();
 			camera = null;
 		}
 		super.onPause();
 	}
 
 	public static void setCameraDisplayOrientation(Activity activity, int cameraId, android.hardware.Camera camera) {
 		android.hardware.Camera.CameraInfo info =
 				new android.hardware.Camera.CameraInfo();
 		android.hardware.Camera.getCameraInfo(cameraId, info);
 		int rotation = activity.getWindowManager().getDefaultDisplay()
 				.getRotation();
 		int degrees = 0;
 		switch (rotation) {
 		case Surface.ROTATION_0: degrees = 0; break;
 		case Surface.ROTATION_90: degrees = 90; break;
 		case Surface.ROTATION_180: degrees = 180; break;
 		case Surface.ROTATION_270: degrees = 270; break;
 		}
 
 		int result;
 		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
 			result = (info.orientation + degrees) % 360;
 			result = (360 - result) % 360;  // compensate the mirror
 		} else {  // back-facing
 			result = (info.orientation - degrees + 360) % 360;
 		}
 		camera.setDisplayOrientation(result);
 	}
 
 }
