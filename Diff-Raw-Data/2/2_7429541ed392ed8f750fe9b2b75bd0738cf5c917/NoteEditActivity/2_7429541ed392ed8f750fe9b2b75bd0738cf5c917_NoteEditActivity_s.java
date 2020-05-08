 package com.xue.yynote.activity;
 
 import com.xue.yynote.view.NoteEditView;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.widget.Toast;
 import android.graphics.Bitmap;
 import android.graphics.Matrix;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import android.content.ContentResolver;
 import android.text.style.ImageSpan;
 import android.text.SpannableString;
 import android.text.Spannable;
 import android.text.Editable;
 
 public class NoteEditActivity extends Activity {
 	public static final String TAG = "NoteEditActivity";
 	private NoteEditView mNoteEditView;
 	private static final int PHOTO_SUCCESS = 1;
 	private static final int CAMERA_SUCCESS = 2;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		this.mNoteEditView = new NoteEditView(this);
 		this.setContentView(mNoteEditView);
 		Bundle bundle = getIntent().getExtras();
 		int mNoteId = bundle.getInt("ID");
 		if (mNoteId >= 0) {
 			this.mNoteEditView.setNoteEditModel(mNoteId);
 		} else if (mNoteId == -1) {
 			this.mNoteEditView.createNoteEditModel();
 		} else if (mNoteId == -2) {
 			String mNoteContent = bundle.getString("CONTENT");
 			Uri imageUri = bundle.getParcelable(Intent.EXTRA_STREAM);
 			Log.i(TAG, "" + imageUri);
 			Bitmap mNoteBitmap = null;
 			if (imageUri != null) {
 				try {
 					mNoteBitmap = BitmapFactory.decodeStream(this
 							.getContentResolver().openInputStream(imageUri));
 				} catch (FileNotFoundException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			if (mNoteContent != null) {
 				this.mNoteEditView.setContentText(mNoteContent);
 			}
 			if (mNoteBitmap != null) {
 				this.mNoteEditView.createNoteEditModel(); // 创建model
 				showBitmapImg(mNoteBitmap);
 			}
 		} else if (mNoteId == -3) {    //多个图片
 			ArrayList<Uri> imageUris = bundle
 					.getParcelableArrayList(Intent.EXTRA_STREAM);
 			String mNoteContent = bundle.getString("CONTENT");
 			Bitmap mNoteBitmap = null;
 
 			if (mNoteContent != null) {
 				this.mNoteEditView.setContentText(mNoteContent);
 			}
 			Editable edit_text = mNoteEditView.mContent.getEditableText();
 			this.mNoteEditView.createNoteEditModel(); // 创建model
 			for (Uri imageUri : imageUris) {
 				try {
 					mNoteBitmap = BitmapFactory.decodeStream(this
 							.getContentResolver().openInputStream(imageUri));
 				} catch (FileNotFoundException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				if (mNoteBitmap != null) {
 					showBitmapImg(mNoteBitmap);
 					edit_text.append("\n");
 				}
 			}
 
 		}
 	}
 
 	protected void onActivityResult(int requestCode, int resultCode,
 			Intent intent) {
 		ContentResolver resolver = getContentResolver();
 		if (resultCode == RESULT_OK) {
 			Bitmap originalBitmap = null;
 			if (requestCode == PHOTO_SUCCESS && intent != null) {
 				Uri originalUri = intent.getData();
 				try {
 					originalBitmap = BitmapFactory.decodeStream(resolver
 							.openInputStream(originalUri));
 
 				} catch (FileNotFoundException e) {
 					e.printStackTrace();
 				}
 			} else if (requestCode == CAMERA_SUCCESS && intent != null) {
 				Bundle extras = intent.getExtras();
 				originalBitmap = (Bitmap) extras.get("data");
 
 			}
 			if (originalBitmap != null) {
 				showBitmapImg(originalBitmap);
 			} else {
 				Toast.makeText(NoteEditActivity.this, "获取图片失败",
 						Toast.LENGTH_SHORT).show();
 			}
 		}
 	}
 
 	public void showBitmapImg(Bitmap bitmap) {
		bitmap = resizeImage(bitmap, 300, 300);
 		String filePath = this.savePicture(bitmap);
 		// 根据Bitmap对象创建ImageSpan对象
 		ImageSpan imageSpan = new ImageSpan(NoteEditActivity.this, bitmap);
 		// 创建一个SpannableString对象，以便插入用ImageSpan对象封装的图像
 		SpannableString spannableString = new SpannableString("[0x64"
 				+ filePath + "]");
 		// 用ImageSpan对象替换face
 		spannableString.setSpan(imageSpan, 0, spannableString.length(),
 				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
 		// 将选择的图片追加到EditText中光标所在位置
 		int index = mNoteEditView.mContent.getSelectionStart(); // 获取光标所在位置
 		Editable edit_text = mNoteEditView.mContent.getEditableText();
 		if (index < 0 || index >= edit_text.length()) {
 			edit_text.append(spannableString);
 		} else {
 			edit_text.insert(index, spannableString);
 		}
 		this.mNoteEditView.findImages();
 	}
 
 	private String savePicture(Bitmap bitmap) {
 		// TODO Auto-generated method stub
 		File root = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
 		String name = "/pic_" + System.currentTimeMillis() + ".jpeg";
 		try {
 			File f = new File(root.getAbsolutePath() + name);
 			f.createNewFile();
 			FileOutputStream outStream = new FileOutputStream(f.getPath());
 			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
 
 			outStream.flush();
 			outStream.close();
 			return name;
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	private Bitmap resizeImage(Bitmap originalBitmap, int newWidth,
 			int newHeight) {
 		int width = originalBitmap.getWidth();
 		int height = originalBitmap.getHeight();
 
 		float scaleWidth = 1;
 		float scaleHeight = (float) newHeight / height;
 		if(width <= height){
 			if(width <= newWidth){
 				scaleWidth = 1;
 			}
 			else{
 				scaleWidth = (float) newWidth / width;
 			}
 			scaleHeight = scaleWidth;
 		}
 		else{
 			if(height <= newHeight){
 				scaleHeight = 1;
 			}
 			else{
 				scaleHeight = (float) newHeight / height;
 			}
 			scaleWidth = scaleHeight;
 		}
 		// 创建操作图片用的matrix对象 Matrix
 		Matrix matrix = new Matrix();
 		// 缩放图片动作
 		matrix.postScale(scaleWidth, scaleHeight);
 
 		// 创建新的图片Bitmap
 		Bitmap resizedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, width,
 				height, matrix, true);
 		return resizedBitmap;
 	}
 
 	public void onBackPressed() {
 		
 		if (this.mNoteEditView.getOriginalLen() != this.mNoteEditView
 				.getContentLength()) {
 			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
 			dialog.setMessage("是否保存修改")
 					.setTitle("提示")
 					.setPositiveButton("是",
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int which) {
 									mNoteEditView.finishEdit();
 									
 									Bundle bundle = new Bundle();
 									bundle.putInt("NOTE_ID", mNoteEditView.getModelId());
 							
 									NoteEditActivity.this.setResult(
 											Activity.RESULT_OK,
 											NoteEditActivity.this.getIntent()
 													.putExtras(bundle));
 									NoteEditActivity.this.finish();
 								}
 							})
 					.setNegativeButton("否",
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int which) {
 									// NoteEditActivity.super.onBackPressed();
 									Bundle bundle = new Bundle();
 									bundle.putInt("NOTE_ID", mNoteEditView.getModelId());
 							
 									NoteEditActivity.this.setResult(
 											Activity.RESULT_OK,
 											NoteEditActivity.this.getIntent()
 													.putExtras(bundle));
 									NoteEditActivity.this.finish();
 								}
 							});
 			dialog.create().show();
 			// super.onBackPressed();
 		} else {
 			super.onBackPressed();
 		}
 	}
 }
