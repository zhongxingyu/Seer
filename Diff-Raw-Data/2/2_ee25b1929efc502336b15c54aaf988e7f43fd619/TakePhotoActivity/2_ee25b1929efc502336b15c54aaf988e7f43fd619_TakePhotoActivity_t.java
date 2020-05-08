 package c301.AdventureBook;
 //Creator: Zhao Zhang
 //Done
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.os.Environment;
 
 import com.example.adventurebook.R;
 public class TakePhotoActivity extends Activity{
 	//int REQUEST_CODE = 0;
 	private static final int SELECT_PHOTO = 100;
 	private static final int TAKE_PHOTO = 101;
 	static Uri capturedImageUri=null;
 	String show_path;
 	int select_result = 0;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
		setContentView(R.layout.upload_media);
 		Button uploadFromPhone = (Button)findViewById(R.id.fromPhoneButton);
 		Button uploadFromWebCam = (Button)findViewById(R.id.fromWebCamButton);
 		Button uploadConfirm = (Button)findViewById(R.id.confirmButton);
 		uploadFromPhone.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View v) {
 				//Intent photoPickerIntent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
 				Intent photoPickerIntent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);				
 				//photoPickerIntent.setType("image/*");
 				//photoPickerIntent.setAction(Intent.ACTION_GET_CONTENT);
 				startActivityForResult(photoPickerIntent, SELECT_PHOTO);
 				//startActivityForResult(Intent.createChooser(photoPickerIntent,"Select Picture"), SELECT_PHOTO);				
 				// TODO Auto-generated method stub
 			}
 		});
 		uploadFromWebCam.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View v) {
 				Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
 				File file = new File(Environment.getExternalStorageDirectory(),  ("temp.jpg"));
 				try {
 					file.createNewFile();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				capturedImageUri = Uri.fromFile(file);
 				cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri);
 				startActivityForResult(cameraIntent, TAKE_PHOTO);
 				// TODO Auto-generated method stub
 				
 			}
 		});
 		uploadConfirm.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				if(select_result == 1){
 					saveAndFinish();
 				}
 				
 			}
 
 		});
 		
 			
 	}
 	private void saveAndFinish() {
 		final EditText my_path = (EditText) findViewById(R.id.editpath);
 		my_path.setText(show_path);
 		Intent intent = new Intent();		
 		intent.putExtra("path", show_path);
 		setResult(RESULT_OK, intent);
 		//finish();
 		
 	}
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnIntent) {
 		// TODO Auto-generated method stub
 		super.onActivityResult(requestCode, resultCode, imageReturnIntent);
 		switch(requestCode){			
 				//Bitmap photo = (Bitmap) imageReturnIntent.getExtras().get("imageReturnIntent");
 		
 				//ImageView test = (ImageView) findViewById(R.id.upload_photo_view);
 				//test.setImageBitmap(photo);
 				
 				//try{
 				//	String imageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/name.jpg";
 				//	FileOutputStream out = new FileOutputStream(imageFilePath);
 				//	photo.compress(Bitmap.CompressFormat.JPEG, 90, out);
 				//} catch(Exception e) {
 				//	e.printStackTrace();
 				//}
 				
 		case SELECT_PHOTO:
 			If(resultCode == RESULT_OK);{
 				//Uri selectedImage = imageReturnIntent.getData();
 				//
 				//try {
 				//	InputStream imageStream = getContentResolver().openInputStream(selectedImage);
 				//	Bitmap yourSelectedImage = BitmapFactory.decodeStream(imageStream);
 				//	ImageView test2 = (ImageView) findViewById(R.id.upload_photo_view);
 				//	test2.setImageBitmap(yourSelectedImage);
 				//} catch (FileNotFoundException e) {
 					// TODO Auto-generated catch block
 				//	e.printStackTrace();
 				//}
 	            
 				Uri selectedImage = imageReturnIntent.getData();
 				String[] filePathColumn = {MediaStore.Images.Media.DATA};
 				Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
 				cursor.moveToFirst();
 				int columnIndex =  cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
 			    String filePath = cursor.getString(columnIndex);
 				cursor.close();
 				ImageView test = (ImageView) findViewById(R.id.upload_photo_view);
 			    test.setImageBitmap(BitmapFactory.decodeFile(filePath));
 			    show_path = filePath;
 			    select_result = 1;
 			    break;
 			}
 		case TAKE_PHOTO:
 			If(resultCode == RESULT_OK);{
 				try {
 					ImageView test = (ImageView) findViewById(R.id.upload_photo_view);
 					Bitmap bitmap = MediaStore.Images.Media.getBitmap( getApplicationContext().getContentResolver(),  capturedImageUri);
 					test.setImageBitmap(bitmap);
 					show_path = Environment.getExternalStorageDirectory() +"/temp.jpg" ;
 					select_result = 1;
 					break;
 				} catch (FileNotFoundException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			
 			
 		
 		}
 			
 	}
 	public void onBackPressed() {
 		// TODO Auto-generated method stub
 		finish();
 	}
 	private void If(boolean b) {
 		// TODO Auto-generated method stub
 		
 	}
 }
 
