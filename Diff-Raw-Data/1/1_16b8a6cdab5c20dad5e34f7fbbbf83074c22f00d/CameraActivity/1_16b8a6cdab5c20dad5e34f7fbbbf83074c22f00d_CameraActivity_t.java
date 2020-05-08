 package com.example.lightdetector;
 
 import java.nio.Buffer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import org.opencv.android.BaseLoaderCallback;
 import org.opencv.android.CameraBridgeViewBase;
 import org.opencv.android.LoaderCallbackInterface;
 import org.opencv.android.OpenCVLoader;
 import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
 import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
 import org.opencv.core.Core;
 import org.opencv.core.Mat;
 import org.opencv.core.MatOfPoint;
 import org.opencv.core.Point;
 import org.opencv.core.Scalar;
 import org.opencv.imgproc.Imgproc;
 
 import com.example.digitallighterserver.R;
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Window;
 import android.view.WindowManager;
 
 public class CameraActivity extends Activity implements CvCameraViewListener2, PointCollectorListener {
 
 	PointCollector collector;
 
 	static int tilesX = 4;
 	static int tilesY = 4;
 	BlockingQueue<HashMap<String, ArrayList<Point>>> buffer = new LinkedBlockingQueue<HashMap<String, ArrayList<Point>>>();
 
 	int fpsCounter;
 
 	// COLORS
 	ArrayList<String> screenColors = new ArrayList<String>();
 
 	private CameraBridgeViewBase mOpenCvCameraView;
 	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
 		@Override
 		public void onManagerConnected(int status) {
 			switch (status) {
 			case LoaderCallbackInterface.SUCCESS: {
 				Log.i("Yo", "OpenCV loaded successfully");
 
 				mOpenCvCameraView.enableView();
 				// mOpenCvCameraView.setOnTouchListener(ColorBlobDetectionActivity.this);
 			}
 				break;
 			default: {
 				super.onManagerConnected(status);
 			}
 				break;
 			}
 		}
 	};
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		if (mOpenCvCameraView != null)
 			mOpenCvCameraView.disableView();
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
 	}
 
 	public void onDestroy() {
 		super.onDestroy();
 		if (mOpenCvCameraView != null)
 			mOpenCvCameraView.disableView();
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 
 		setContentView(R.layout.color_blob_detection_surface_view);
 
 		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.color_blob_detection_activity_surface_view);
 		mOpenCvCameraView.setCvCameraViewListener(this);
 
 	}
 
 	@Override
 	public void onCameraViewStarted(int width, int height) {
 		// TODO Auto-generated method stub
 		collector = new PointCollector(tilesX, tilesY, this);
 
 		screenColors.add(ColorManager.KEY_BLUE);
 		screenColors.add(ColorManager.KEY_GREEN);
 		screenColors.add(ColorManager.KEY_RED);
 	}
 
 	@Override
 	public void onCameraViewStopped() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
 		collector.collect(inputFrame.rgba(), screenColors);
 
 		Mat image = drawTilesGrid(inputFrame.rgba(), tilesY, tilesY);
 		if (buffer.size() > 0) {
 			for (String colorItem : buffer.peek().keySet()) {
 				for (Point tile : buffer.peek().get(colorItem)) {
 					// System.out.println("Blob: " + colorItem + " " + tile.x + " " + tile.y);
 					image = drawTile(image, (int) tile.x, (int) tile.y, ColorManager.getCvColor(colorItem));
 				}
 			}
 			buffer.remove();
 		}
 		return image;
 	}
 
 	@Override
 	public void onPointCollectorUpdate(HashMap<String, ArrayList<Point>> update) {
 		if (buffer.size() > 20) {
 			buffer.clear();
 		}
 		buffer.add(update);
 	}
 
 	public static Mat drawTilesGrid(Mat input, int tilesX, int tilesY) {
 		Mat output = new Mat();
 		input.copyTo(output);
 
 		int unit = output.width() / tilesX;
 		for (int i = 0; i < tilesX; ++i)
 			Core.line(output, new Point(i * unit, 0), new Point(i * unit, output.height()),
 					ColorManager.getCvColor(ColorManager.KEY_RED));
 
 		unit = output.height() / tilesY;
 		for (int i = 0; i < tilesY; ++i)
 			Core.line(output, new Point(0, i * unit), new Point(output.width(), i * unit),
 					ColorManager.getCvColor(ColorManager.KEY_RED));
 
 		return output;
 	}
 
 	private static Mat drawTile(Mat input, int x, int y, Scalar color) {
 		Mat output = new Mat(input.height(), input.width(), input.type(), new Scalar(0, 0, 0));
 		input.copyTo(output);
 
 		int unitX = output.width() / tilesX;
 		int unitY = output.height() / tilesY;
 		Core.rectangle(output, new Point(unitX * x, unitY * y), new Point(unitX * (x + 1), unitY * (y + 1)),
 				color, 5);
 
 		// Core.addWeighted(input, 1.0, output, 0.5, 0, output);
 		return output;
 	}
 }
