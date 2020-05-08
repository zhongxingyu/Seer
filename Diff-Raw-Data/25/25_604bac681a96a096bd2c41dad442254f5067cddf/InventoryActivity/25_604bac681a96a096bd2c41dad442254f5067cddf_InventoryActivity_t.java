 package org.atxhackerspace.inventory;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 
 import com.google.zxing.integration.android.IntentIntegrator;
 import com.google.zxing.integration.android.IntentResult;
 
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 
 public class InventoryActivity extends Activity {
 	IntentIntegrator integrator;
	
	EditText username, password, item_code, item_name, item_description;
 	ImageView image_to_upload;
 	
 	public static final int SHRINK_MAX = 1024;
 	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 42;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_inventory);
 
 		integrator = new IntentIntegrator(this);
 
		username = (EditText) findViewById(R.id.wiki_username);
		password = (EditText) findViewById(R.id.wiki_password);
 		item_code = (EditText) findViewById(R.id.item_code);
 		image_to_upload = (ImageView) findViewById(R.id.image_to_upload);
		item_name = (EditText) findViewById(R.id.item_name);
		item_description = (EditText) findViewById(R.id.item_description);
		
 		setImagePreview();
 
 		final Button scan = (Button) findViewById(R.id.scan);
 		scan.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				integrator.initiateScan();
 			}
 		});
 
 		final Button take_picture = (Button) findViewById(R.id.take_picture);
 		take_picture.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 				Log.d("Onclick", Uri.fromFile(getTempFile()).toString());
 				intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getTempFile()));
 				startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
 			}
 		});
 		
 		final Button submit = (Button) findViewById(R.id.submit);
 		submit.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
				try {
					WikiWhack.create(username.getText().toString(), password.getText().toString(),
							item_code.getText().toString(), getPreviewImage(),
							item_name.getText().toString(), item_description.getText().toString());
				} catch (FileNotFoundException e) {} // No image? Just don't do anything
 			}
 		});
 	}
 
 	public void setImagePreview() {
 		try {
 			Bitmap bm = getPreviewImage();
 			image_to_upload.setImageBitmap(bm);
 		}
 		// It's fine if there's no file.
 		catch (FileNotFoundException e) {
 			Log.d("setImagePreview", "File Not Found");
 		}
 	}
 	
 	public Bitmap getPreviewImage() throws FileNotFoundException {
 		FileInputStream fis = new FileInputStream(getTempFile());
 		Bitmap imageBitmap = BitmapFactory.decodeStream(fis);
 
 		Float width  = Float.valueOf(imageBitmap.getWidth());
 		Float height = Float.valueOf(imageBitmap.getHeight());
 		
 		Integer new_width, new_height;
 		if (width > height) {
 			Float ratio = height / width;
 			new_width = SHRINK_MAX;
 			new_height = (int)(ratio * SHRINK_MAX);
 		} else {
 			Float ratio = width / height;
 			new_width = (int)(ratio * SHRINK_MAX);
 			new_height = SHRINK_MAX;
 		}
 		Log.d("getPreviewImage", String.format("Shrinking from %d, %d to %d, %d", width.intValue(), height.intValue(), new_width, new_height));
 		imageBitmap = Bitmap.createScaledBitmap(imageBitmap, new_width, new_height, false);
 		FileOutputStream fos = new FileOutputStream(getTempFile(true));
 		imageBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
 		return imageBitmap;
 	}
 	
 	private File getTempFile(Boolean shrunk) {
 		File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
 		if (!path.exists()) path.mkdir();
 		if (shrunk) {
 			return new File(path, "last-inventory-pic-small.jpg");
 		} else {
 			return new File(path, "last-inventory-pic.jpg");
 		}
 	}
 	
 	private File getTempFile() {
 		return getTempFile(false);
 	}
 
 	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
 		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
 			if (resultCode == RESULT_OK) {
 				setImagePreview();
 			}
 		} else {
 			IntentResult scanResult = IntentIntegrator.parseActivityResult(
 					requestCode, resultCode, intent);
 			if (scanResult != null) {
 				item_code.setText(scanResult.getContents());
 			}
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_inventory, menu);
 		return true;
 	}
 
 }
