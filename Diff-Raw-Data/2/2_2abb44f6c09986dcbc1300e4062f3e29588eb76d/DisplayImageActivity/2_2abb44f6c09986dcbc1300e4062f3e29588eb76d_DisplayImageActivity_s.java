 package com.hipsterrific.app;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.NavUtils;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.Toast;
 
 import com.hipsterrific.R;
 import com.hipsterrific.FaceDetector.BitmapHelper;
 import com.hipsterrific.FaceDetector.BoundingBoxOverlaySpec;
 import com.hipsterrific.FaceDetector.Face;
 import com.hipsterrific.FaceDetector.FaceDetector;
 import com.hipsterrific.FaceDetector.FaceDetector.ClassifierType;
 import com.hipsterrific.FaceDetector.GlassesOverlaySpec;
 import com.hipsterrific.FaceDetector.OverlaySpec;
 import com.hipsterrific.app.OverlayPickerDialog.OnOverlaySelectedListener;
 
 public class DisplayImageActivity extends FragmentActivity {
 	
 	HashMap<Button, Face> buttonFaceMap;  
 	ArrayList<OverlaySpec> overlaySpecs;
 	protected ProgressDialog progressDialog;
 	private ImageView imageView;
 	LayoutInflater controlInflater = null;
 	private RelativeLayout layout;
 
 	private File unprocessedImageFile;
 	@SuppressWarnings("unused")
 	private File processedImageFile;
 
 	private int orientation;
 
 	FaceDetector faceDetector;
 	ArrayList<File> classifierFiles; 	
 	HashMap<FaceDetector.ClassifierType, String> classifierPaths;
 	
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		
 		// Load image
 		Uri imageUri = this.getIntent().getData();
 		String unprocessedImagePath = FileHelper.getRealPathFromURI(this, imageUri);
 		this.unprocessedImageFile = new File(unprocessedImagePath);
 		Bitmap unprocessedImage = BitmapHelper.sampledFromPath(unprocessedImagePath, 700, 700);
 		this.addImageToGallery(unprocessedImagePath);
 
 		// Load orientation
 		if (this.getIntent().hasExtra("orientation")) {
 			this.orientation = this.getIntent().getExtras().getInt("orientation");
 		} else {
 			this.orientation = 0;
 		}
 		Log.d("Orientation", this.orientation + "");
 		
 		// Setup layout
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		
 		this.setContentView(R.layout.activity_display_image);
 		this.layout = (RelativeLayout)this.findViewById(R.id.display_layout);
 		
 		// Setup views
 		this.imageView = (ImageView)this.findViewById(R.id.result);
 		this.imageView.setAdjustViewBounds(true);
 		this.imageView.setImageBitmap(unprocessedImage);
 		
 		this.controlInflater = LayoutInflater.from(this.getBaseContext());
 	    View viewControl = this.controlInflater.inflate(R.layout.controldisplay, null);
 	    LayoutParams layoutParamsControl = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
 	    this.addContentView(viewControl, layoutParamsControl);
 	    
 	    this.buttonFaceMap = new HashMap<Button, Face>();
 	    
 	    this.progressDialog = new ProgressDialog(this);
 	    this.progressDialog.setMessage(getString(R.string.detection_string));
 	    this.progressDialog.setIndeterminate(true);
 	    
 	    
 	    try {
 			this.classifierFiles = new ArrayList<File>();
 			this.classifierPaths = new HashMap<FaceDetector.ClassifierType, String>();
 			this.populateClassifierPaths();
 			
 			this.overlaySpecs = new ArrayList<OverlaySpec>();
 			this.populateOverlaySpecs();
 			
 	    	this.detectFaces();
 	    } catch (Exception e) {
 	    	e.printStackTrace();
 	    }
 	}
 	
 	/*
 	 * Adds a pic to the gallery by starting the Media Scanner intent
 	 */
 	private void addImageToGallery(String path) {
 	    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
 	    
 	    File file = new File(path);
 	    Uri contentUri = Uri.fromFile(file);
 	    mediaScanIntent.setData(contentUri);
 	    
 	    this.sendBroadcast(mediaScanIntent);
 	}
 	
 	private void detectFaces(){	
 		if (this.faceDetector != null) {
 			this.faceDetector.cancel(true);
 		}
 		
 		try {
 			File manipulatedImageFile = File.createTempFile("manipulatedphoto", null, getBaseContext().getFilesDir());
 			this.faceDetector = new FaceDetector(this.unprocessedImageFile, manipulatedImageFile, this.orientation, this.classifierPaths) {
 				@Override
 				public void onPreExecute() {
 					Log.d("AsyncTask", "PreExecute");
 					
 				    progressDialog.show();
 				}
 				@Override
 				  public void onPostExecute(Boolean result) {
 					Log.d("AsyncTask", "PostExecute");
 					if (result) {
 						progressDialog.dismiss();
 						addFaceButtons();
 					} 
 					else {
 						Log.d("AsyncTask", "Something went wrong!");
 					}
 				  }
 			};
 
 			this.faceDetector.execute();
 		} catch (Exception e) {
 			e.printStackTrace();
 			return;
 		}
 	}
 	
 	private void processImage() {
 		try {
 			this.processedImageFile = File.createTempFile("processedphoto", null, this.getBaseContext().getFilesDir());
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return;
 		}
 
 		Bitmap processedImage = this.faceDetector.processPhoto();
 		BitmapHelper.saveToFile(processedImage, this.processedImageFile);
 
 		this.imageView.setImageBitmap(processedImage);
 	}
 	
 	protected void addFaceButtons() {
 		if (!this.buttonFaceMap.isEmpty()) {
 			return;
 		}
 		
 		int imageWidth = this.imageView.getDrawable().getIntrinsicWidth();
 //		int imageHeight = imageView.getDrawable().getIntrinsicHeight();
 		int imageViewWidth = this.imageView.getWidth();
 //	    int imageViewHeight = imageView.getHeight();
 	    double scale = (double)imageViewWidth/ imageWidth;
 		
 		int i = 0;
 		for(Face face : this.faceDetector.faces){
 			Log.d("faceButtons","adding button "+ i);
 			
 			Button button = new Button(this);	
 
 			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
 		            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 			layoutParams.leftMargin = (int)(face.rect.left * scale);
 			layoutParams.topMargin = (int)(face.rect.top * scale);
 			layoutParams.width = (int)(face.rect.width() * scale);
 			layoutParams.height = (int)(face.rect.height() *scale);
 			button.setLayoutParams(layoutParams);
 			
 			button.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View view) {
 					Button button = (Button)view; 
 					final Face face = buttonFaceMap.get(button);
 					
 					FragmentManager fragmentManager = getSupportFragmentManager();
                     final OverlayPickerDialog pickerDialog = new OverlayPickerDialog(overlaySpecs, false);
                     pickerDialog.setOnOverlaySelectedListener(new OnOverlaySelectedListener() {
 						@Override
 						public void onOverlaySelected(OverlaySpec overlaySpec) {
 							pickerDialog.dismiss();
 							
 							face.overlays.add(overlaySpec);
 
 							processImage();
 						}
 					});
                     pickerDialog.show(fragmentManager, "overlay_picker");
 				}
 			});
 			
 			button.setOnLongClickListener(new OnLongClickListener() {
 				@Override
 				public boolean onLongClick(View v) {
 					Button button = (Button)v;
 					final Face face = buttonFaceMap.get(button);
 					if(!face.overlays.isEmpty()){
 					FragmentManager fragmentManager = getSupportFragmentManager();
 					final OverlayPickerDialog pickerDialog = new OverlayPickerDialog((ArrayList<OverlaySpec>)face.overlays, true);
 					pickerDialog.setOnOverlaySelectedListener(new OnOverlaySelectedListener() {
 						
 						@Override
 						public void onOverlaySelected(OverlaySpec overlaySpec) {
 							pickerDialog.dismiss();
 							face.overlays.remove(overlaySpec);
 							processImage();
 						}
 					});
 					pickerDialog.show(fragmentManager, "remove_overlay");
 					}else{
						Toast.makeText(getApplicationContext(), "No overlays selected!", Toast.LENGTH_SHORT);
 					}
 					return true;
 				}
 			});
 			this.layout.addView(button);
 			this.buttonFaceMap.put(button, face);
 			i++;
 		}
 	}
 	
 	private void deleteClassifierFiles() {
 		if (this.classifierFiles == null) {
 			Log.i("DisplayImage", "No classifier files");
 			return;
 		}
 
 		for (File file : this.classifierFiles) {
 			file.delete();
 		}
 	}
 	
 	@Override
 	public void onDestroy(){
 		if (this.faceDetector != null) {
 			this.faceDetector.cancel(true);
 		}
 		this.deleteClassifierFiles();
 
 		super.onDestroy();
 	}
 	
 	@Override
 	public void onBackPressed() {
 		NavUtils.navigateUpFromSameTask(this);
 	}
 	
 	private void populateOverlaySpecs() throws IOException {
 		File bitmapFile = FileHelper.getTempFileForResource(this.getApplicationContext(), R.raw.overlay_glasses);
 		GlassesOverlaySpec glassesOverlaySpec = new GlassesOverlaySpec(bitmapFile);
 		this.overlaySpecs.add(glassesOverlaySpec);
 			
 		BoundingBoxOverlaySpec boxOverlaySpec = new BoundingBoxOverlaySpec();
 		this.overlaySpecs.add(boxOverlaySpec);
 	}
 
 	private void populateClassifierPaths() {
 		HashMap<ClassifierType, Integer> classifierResources = new HashMap<ClassifierType, Integer>();
 		
 		classifierResources.put(ClassifierType.FACE,  R.raw.classifier_face);
 		classifierResources.put(ClassifierType.EYES,  R.raw.classifier_eyes);
 		classifierResources.put(ClassifierType.NOSE,  R.raw.classifier_nose);
 		classifierResources.put(ClassifierType.MOUTH, R.raw.classifier_mouth);
 		
 		File classifierFile = null;
 		for (HashMap.Entry<ClassifierType, Integer> entry : classifierResources.entrySet()) {
 		    ClassifierType classifierType = entry.getKey();
 		    int resourceId = entry.getValue();
 		    
 			try {
 				classifierFile = FileHelper.getTempFileForResource(this.getApplicationContext(), resourceId);
 				this.classifierFiles.add(classifierFile);
 				this.classifierPaths.put(classifierType, classifierFile.getAbsolutePath());
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public void exportImage(View view) {
 		File accessibleImageFile = FileHelper.getOutputMediaFile(this.getString(R.string.image_directory));
 		if (accessibleImageFile == null) {
 			Log.d("Displayer", "Error creating media file, check storage permissions");
 			return;
 		}
 		
 		File imageFile = this.processedImageFile != null ? this.processedImageFile : this.unprocessedImageFile;
 
 		BitmapHelper.saveToFile(BitmapHelper.fromFile(imageFile), accessibleImageFile);		
 		this.addImageToGallery(accessibleImageFile.getPath());
 		
 		Toast.makeText(this, "New Image saved: " + accessibleImageFile.getPath(), Toast.LENGTH_LONG).show();
 		
 		Intent shareIntent = new Intent();
 		shareIntent.setAction(Intent.ACTION_SEND);
 		shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(accessibleImageFile));
 		shareIntent.setType("image/jpeg");
 		startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.send_to)));
 	}
 
 }
