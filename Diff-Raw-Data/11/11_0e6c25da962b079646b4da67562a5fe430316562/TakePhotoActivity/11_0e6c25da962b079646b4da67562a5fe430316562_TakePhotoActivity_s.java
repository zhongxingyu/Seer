 package com.webb.androidmosaic;
 
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.res.AssetManager;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 import com.webb.androidmosaic.generation.AnalyzedImage;
 import com.webb.androidmosaic.generation.Configuration;
 import com.webb.androidmosaic.generation.NewStateListener;
 import com.webb.androidmosaic.generation.generator.Generator;
 import com.webb.androidmosaic.generation.generator.GeneratorFactory;
 import com.webb.androidmosaic.util.MosaicUtil;
 
 public class TakePhotoActivity extends Activity {
 	
 	private static final int CAMERA_REQUEST = 1888;
 	private ImageView imageView;
 	private Button makeMosaicButton;
 	private Bitmap image;
 	private Generator generator;
 	@SuppressWarnings("unused")
 	private Configuration cfg;
 	private List<AnalyzedImage> solution; 
 	private String MosaicGeneratorTag = "Mosaic Generation";
 	private int count = 0;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.imageview);
 		this.imageView = (ImageView) this.findViewById(R.id.imageView1);
 		Button photoButton = (Button) this.findViewById(R.id.button1);
 		photoButton.setOnClickListener(new View.OnClickListener() {
 			
 			public void onClick(View v) {
 				Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
 				startActivityForResult(cameraIntent, CAMERA_REQUEST);				
 			}
 		});
 		makeMosaicButton = (Button) this.findViewById(R.id.makeMosaic);
 		makeMosaicButton.setEnabled(false);
 		
 	}
 	
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		Bitmap photo = null;
 		if(requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
 			photo = (Bitmap) data.getExtras().get("data");
 			imageView.setImageBitmap(photo);
 			image = photo;
 		}
 		
 		makeMosaicButton.setEnabled(true);
 		makeMosaicButton.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				Log.i(MosaicGeneratorTag, "Now creating mosaic");
 				createPhotoMosaic(image);
 			}
 		});
 	}
 	
 	private  void createPhotoMosaic(Bitmap image) {
 		AndroidMosaicApp app = (AndroidMosaicApp) getApplicationContext();
 		generator = GeneratorFactory.getGenerator(app.getCfg());
 		generator.setTargetImage(image);
 		NewStateListener generatorStateListener = new NewStateListener() {
 			
 			public void handle(List<AnalyzedImage> state, float currentFitness) {
 				Log.d(MosaicGeneratorTag, "got state");
 				if(count >= 100) {
 					generator.pause();
 					solution = generator.getSolutionTiles();
 					if(saveMosaic(generator.getNumTilesPerRowInSolution(), generator.getNumTilesPerColumnInSolution(), generator.getWidthOfTileInPixels())) {
 						Log.d(MosaicGeneratorTag, "Image saved");
 					}
 				} else {
 					count++;
 				}
 				// TODO code for animating mosaic change
 			}
 		};
 		generator.registerNewStateListener(generatorStateListener);
 		Log.i(MosaicGeneratorTag, "Listener attached");
 		generator.start();
 		if(generator.getSolutionTiles() != null) {
 			solution = generator.getSolutionTiles();
 		}
 		//Code could follow here to add image to screen
 		
 		
 	}
 	
 	private boolean saveMosaic(int numOfRows, int numOfColums, int widthOfTile) {
 		Bitmap mosaicBitmap = Bitmap.createBitmap(numOfRows*widthOfTile, numOfColums*widthOfTile, Bitmap.Config.ARGB_8888);
 		Canvas mosaic = new Canvas(mosaicBitmap);
 		
 		boolean success = false;
 		
 		FileInputStream file = null;
 		int x = 0;
 		int y = 0;
 		int columnsCurrentlyWritten = 0;
 		for(AnalyzedImage analyzedImage: solution) {
 			String fileName = ((AndroidMosaicApp) getApplicationContext()).getBitmapDirectory() + "/" + analyzedImage.getFile();
 			File directory = getDir(((AndroidMosaicApp) getApplicationContext()).getBitmapDirectory(), MODE_WORLD_READABLE);
 			try {
 				file = new FileInputStream(directory+ "/" + analyzedImage.getFile());
 			} catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			Bitmap image = BitmapFactory.decodeStream(file);
 			if(image != null) {
 				Bitmap croppedImage = MosaicUtil.cropBitMapToSquare(image);
 				image = Bitmap.createScaledBitmap(croppedImage, widthOfTile, widthOfTile, false);
 				if(columnsCurrentlyWritten == numOfColums) {
					y += 15;
 					x = 0;
 				} else {
 					mosaic.drawBitmap(image, x, y, null);
 				}
 			}
 		}
 		
 		try {
 			File sdDir = Environment.getExternalStorageDirectory();
 			mosaicBitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(sdDir+"/image.jpg"));
 			sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"+ Environment.getExternalStorageDirectory())));
 			success = true;
 		} catch(IOException e) {
 			Log.e(MosaicGeneratorTag, "Unable to save the mosaic");
 			e.printStackTrace();
 		}
 		return success;
 	}
 
 }
