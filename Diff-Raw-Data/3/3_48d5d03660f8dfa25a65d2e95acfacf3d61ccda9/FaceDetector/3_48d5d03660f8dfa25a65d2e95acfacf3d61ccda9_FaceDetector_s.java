 package com.hipsterrific.FaceDetector;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Rect;
 import android.util.Log;
 import android.view.ViewDebug.CapturedViewProperty;
 import android.widget.ImageView;
 
 import com.school.cameraapp.R;
 
 import com.googlecode.javacpp.Loader;
 import com.googlecode.javacv.*;
 import com.googlecode.javacv.cpp.*;
 import static com.googlecode.javacv.cpp.opencv_core.*;
 import static com.googlecode.javacv.cpp.opencv_imgproc.*;
 import static com.googlecode.javacv.cpp.opencv_calib3d.*;
 import static com.googlecode.javacv.cpp.opencv_objdetect.*;
 import com.googlecode.javacv.cpp.opencv_highgui;
 import com.googlecode.javacv.cpp.opencv_objdetect;
 
 
 public class FaceDetector {
 	public enum ClassifierType { FACE }
 	protected IplImage image;
 	protected HashMap<ClassifierType, String> classifierPaths;
 	protected CvHaarClassifierCascade faceClassifier;
 
 	public Bitmap photo;
 	public List<Face> faces;
 
 	public FaceDetector(String path, HashMap<ClassifierType, String> classifierPaths) throws Exception {
 		this(BitmapFactory.decodeFile(path), classifierPaths);
 	}
 	
 	public FaceDetector(Bitmap photo, HashMap<ClassifierType, String> classifierPaths) throws Exception {
 		Loader.load(opencv_objdetect.class);
 		
 		this.photo = photo;
 		this.image = IplImageHelper.scaleDownIplImage(IplImageHelper.createIplImageFromBitmap(photo, 1));
 		this.classifierPaths = classifierPaths;
 		this.faces = new ArrayList<Face>();
 		
 		if (!classifierPaths.containsKey(ClassifierType.FACE)) {
 			throw new Exception("Face classifier path is required.");
 		}
 		String path = classifierPaths.get(ClassifierType.FACE);
 		
 		this.faceClassifier = new CvHaarClassifierCascade(cvLoad(path));
 		if (this.faceClassifier == null) {
 			throw new Exception("Failed to load face classifier.");
 		}
 	}
 	
 	public boolean detectFaces() throws Exception {		
 		CvMemStorage storage = CvMemStorage.create();
 		
 		// this is where the magic happens
 		CvSeq cvSeq = cvHaarDetectObjects(this.image, this.faceClassifier, storage, 1.2, 3, CV_HAAR_DO_CANNY_PRUNING);
 		
 		this.faces.clear();
 		int total = cvSeq.total();
 		Log.i("Faces", "Found " + total + " faces!");
 		for (int i = 0; i < total; i++){
 			CvRect cvRect = new CvRect(cvGetSeqElem(cvSeq, i));
 			
 			Rect rect = new Rect(cvRect.x(), cvRect.y(), cvRect.x() + cvRect.width(), cvRect.y() + cvRect.height());
 			rect = IplImageHelper.scaleUpRect(rect);
 			
 			Log.i("Faces", "Face " + i + " found in rect " + rect.flattenToString());
 			
 			Face face = new Face(this, rect);
 			this.faces.add(face);
 			face.detectFeatures();
 		}
 		
 		return true;
 	}
 	
 	public Bitmap processPhoto() {
 		Bitmap processedPhoto = photo.copy(null, true);
 		for (Face face : faces) {
 			face.render(processedPhoto);
 		}
 		return processedPhoto;
 	}
 }
