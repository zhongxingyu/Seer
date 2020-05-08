 package com.sudoku;
 import java.nio.ByteOrder;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.provider.MediaStore.Images;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 
 
 public class SudokuSelector extends Activity implements OnClickListener {
 	private static final int ACTIVITY_SELECT_CAMERA = 0;
 	private static final int ACTIVITY_SELECT_IMAGE = 1;
 	private String mCurrentImagePath = null;
 	private SudokuDetector sd = new SudokuDetector();
 	private int[][] sudoku = new int[9][9];
 	private int byteOrder = -1;
 	Bitmap bitmap = null;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.select_sudoku);
 		if(bitmap!=null){
 			ImageView imageView = (ImageView) findViewById(R.id.sudoku_image);
 			imageView.setImageBitmap(bitmap);
 		}
 		View grabButton = findViewById(R.id.start_grabbing_button);
 		grabButton.setOnClickListener(this);
 		View rotateButton = findViewById(R.id.clockwise_button);
 		rotateButton.setOnClickListener(this);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.sudoku_photo_taken, menu);
 		return true;
 	}
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.camera:
 			Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 			startActivityForResult(cameraIntent, ACTIVITY_SELECT_CAMERA);
 			return true;
 		case R.id.gallery:
 			Intent galleryIntent = new Intent(Intent.ACTION_PICK,
 					Images.Media.INTERNAL_CONTENT_URI);
 			startActivityForResult(galleryIntent, ACTIVITY_SELECT_IMAGE);
 			return true;
 		}
 
 		return super.onMenuItemSelected(featureId, item);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == ACTIVITY_SELECT_CAMERA
 				&& resultCode == Activity.RESULT_OK) {
 			Uri currImageURI = data.getData();
 			String[] proj = { Images.Media.DATA, Images.Media.ORIENTATION };
 			Cursor cursor = managedQuery(currImageURI, proj, null, null,
 					null);
 			int columnIndex = cursor.getColumnIndex(proj[0]);
 			cursor.moveToFirst();
 			mCurrentImagePath = cursor.getString(columnIndex);
 			bitmap = BitmapFactory.decodeFile(mCurrentImagePath);
 			ImageView imageView = (ImageView) findViewById(R.id.sudoku_image);
 			imageView.setImageBitmap(bitmap);
 		}
 		if (requestCode == ACTIVITY_SELECT_IMAGE && resultCode == RESULT_OK) {
 			try {
 				Uri currImageURI = data.getData();
 				String[] proj = { Images.Media.DATA, Images.Media.ORIENTATION };
 				Cursor cursor = managedQuery(currImageURI, proj, null, null,
 						null);
 				int columnIndex = cursor.getColumnIndex(proj[0]);
 				cursor.moveToFirst();
 				mCurrentImagePath = cursor.getString(columnIndex);
 				bitmap = BitmapFactory.decodeFile(mCurrentImagePath);
 				ImageView imageView = (ImageView) findViewById(R.id.sudoku_image);
 				imageView.setImageBitmap(bitmap);
 			} catch (Exception e) {
 			}
 		}
 	}
 	private void getByteOrder(){
 		String order = ByteOrder.nativeOrder().toString();
 		if(order.equals(ByteOrder.BIG_ENDIAN.toString())){
 			byteOrder = SudokuDetector.BIG_ENDIAN;
 		}else if(order.equals(ByteOrder.LITTLE_ENDIAN.toString())){
 			byteOrder = SudokuDetector.LITTLE_ENDIAN;
 		}else{
 			byteOrder = -1;
 		}
 	}
 	
 	public int[] setPuzzle(int sudoku[][]){
 		int puzzle[] = new int[81];
 		for(int i=0;i<9;i++){
 			for(int j=0; j<9; j++){
 				puzzle[i*9+j] = sudoku[i][j];
 			}
 		}
 		return puzzle;
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch(v.getId()){
 		case R.id.start_grabbing_button:
 			if(bitmap!=null){
 				int width = bitmap.getWidth();
 				int height = bitmap.getHeight();
 				int[] pixels = new int[width * height];
 				bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
 				sd.setSourceImage(pixels, width, height);
 				getByteOrder();
 				if(byteOrder != -1){
 					sudoku = sd.detectSudoku(byteOrder);
 					Intent i = new Intent(this,SudokuGrabber.class);
 					i.putExtra(SudokuGrabber.KEY_PUZZLE, setPuzzle(sudoku));
 					startActivity(i);
 		
 				}
 			}
 			break;
 		case R.id.clockwise_button:
 			if(bitmap!=null){
 				bitmap = Utility.rotate(bitmap, 90);
 				ImageView imageView = (ImageView) findViewById(R.id.sudoku_image);
 				imageView.setImageBitmap(bitmap);
 			}
 			break;
 		default:
 			break;
 		}
 	}
 	
 	@Override
 	protected void onSaveInstanceState(Bundle outState){
 		outState.putString("imagePath", mCurrentImagePath);
 		super.onSaveInstanceState(outState);
 	}
 	
 	@Override
 	public void onRestoreInstanceState(Bundle savedInstanceState){
 		mCurrentImagePath = savedInstanceState.getString("imagePath");
 		bitmap = BitmapFactory.decodeFile(mCurrentImagePath);
 		ImageView imageView = (ImageView) findViewById(R.id.sudoku_image);
 		imageView.setImageBitmap(bitmap);
 		super.onRestoreInstanceState(savedInstanceState);
 		
 	}
 }
