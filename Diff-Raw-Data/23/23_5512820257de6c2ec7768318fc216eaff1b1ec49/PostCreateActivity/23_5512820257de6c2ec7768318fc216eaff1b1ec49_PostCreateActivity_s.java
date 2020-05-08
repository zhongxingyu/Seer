 package com.example.board.view;
 
 import java.io.ByteArrayOutputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import org.apache.http.client.HttpResponseException;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Dialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Base64;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.example.board.R;
 import com.example.board.lib.UrlJsonAsyncTask;
 import com.example.board.model.NetworkInfo;
 
 public class PostCreateActivity extends SherlockActivity {
 
 	ImageView[] targetImage = new ImageView[5];
 	String[] imageData = new String[5];
 
 	private SharedPreferences mPreferences;
 	private String mPostTitle;
 	private String mPostDescription;
 	private String mPostCategory;
 
 	private String category;
 
	// int mImageWidth = 0;
	// int mImageHeight = 0;
	// int newImageWidth = 400;
	// int newImageHeight = 400;
	int imageNum = 1;
 	int[] imageId_arr = new int[50];
 	boolean isChange = false;
 
	// float scaleWidth = 0;
	// float scaleHeight = 0;

 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_post_create);
 
 		category = getIntent().getStringExtra("category");
 
 		Button addImageBtn = (Button) findViewById(R.id.addImageBtn);
 
 		addImageBtn.setOnClickListener(new addImageBtnListener());
 
 		mPreferences = getSharedPreferences("AuthToken", MODE_PRIVATE);
 	}
 
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 
 		Log.d("Present Image[0] Status", String.valueOf(imageId_arr[0]));
 		Log.d("Present Image[1] Status", String.valueOf(imageId_arr[1]));
 		Log.d("Present Image[2] Status", String.valueOf(imageId_arr[2]));
 		Log.d("Present Image[3] Status", String.valueOf(imageId_arr[3]));
 		Log.d("Present Image[4] Status", String.valueOf(imageId_arr[4]));
 	}
 
 	private class addImageBtnListener implements OnClickListener {
 
 		@Override
 		public void onClick(View v) {
 			// TODO Auto-generated method stub
 			// imageNum = 이미지 개수
 			int add_image_flag = 0; // 몇 번째에 사진을 추가 할지 경정
 			int max_flag = 0;
 
 			for (int t = 1; t < 50; t++) {
 				if (imageId_arr[t - 1] == 0) {
 					add_image_flag = t;
 					break;
 				} else if (imageId_arr[t - 1] > 0) {
 					max_flag++;
 				}
 
 			}
 			if (max_flag != 5) {
 				getImageFromGallery(add_image_flag);
 			} else {
 				Toast.makeText(v.getContext(), "사진은 5개까지 추가가 가능합니다.",
 						Toast.LENGTH_LONG).show();
 			}
 		}
 	}
 
 	private class imageListener implements OnClickListener {
 		@Override
 		public void onClick(View v) {
 			// TODO Auto-generated method stub
 			Log.d("Change Image ID", String.valueOf(v.getId()));
 			isChange = true;
 			getImageFromGallery(v.getId());
 		}
 	}
 
 	private class deleteImageListner implements OnLongClickListener {
 
 		@Override
 		public boolean onLongClick(View v) {
 			// @Override
 
 			final Dialog dialog = new Dialog(PostCreateActivity.this);
 			dialog.setContentView(R.layout.task_remove_input);
 			dialog.setTitle("삭제하시겠습니까?");
 
 			final ImageView clickImage = (ImageView) findViewById(v.getId());
 			final int clickImageId = v.getId();
 			Log.d("Long Click Image ID", String.valueOf(v.getId()));
 
 			// 취소 버튼
 			((Button) dialog.findViewById(R.id.todayCancelBtn))
 					.setOnClickListener(new OnClickListener() {
 						@Override
 						public void onClick(View v) {
 							dialog.dismiss();
 						}
 					});
 			// 저장 버튼
 			((Button) dialog.findViewById(R.id.todayInputBtn))
 					.setOnClickListener(new OnClickListener() {
 						@Override
 						public void onClick(View v) {
 							Toast.makeText(getApplicationContext(), "삭제했습니다.",
 									Toast.LENGTH_SHORT).show();
 							clickImage.setImageBitmap(null);
 							clickImage.setImageResource(0);
 							imageId_arr[clickImageId - 1] = -1;
 							Log.d("DeleteImageID", String.valueOf(clickImageId));
 
 							Log.d("Present Image[0] Status",
 									String.valueOf(imageId_arr[0]));
 							Log.d("Present Image[1] Status",
 									String.valueOf(imageId_arr[1]));
 							Log.d("Present Image[2] Status",
 									String.valueOf(imageId_arr[2]));
 							Log.d("Present Image[3] Status",
 									String.valueOf(imageId_arr[3]));
 							Log.d("Present Image[4] Status",
 									String.valueOf(imageId_arr[4]));
 
 							dialog.dismiss();
 						}
 					});
 			dialog.show();
 			return false;
 
 		}
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// TODO Auto-generated method stub
 		super.onActivityResult(requestCode, resultCode, data);
 
 		if (resultCode == RESULT_OK) {
 			Uri targetUri = data.getData();
 			try {
 				Log.d("Add Image ID", String.valueOf(requestCode));
 				if (isChange == true) {
 					ImageView changeImage = (ImageView) findViewById(imageId_arr[requestCode - 1]);
 
 					changeImage.setImageBitmap(decodeUri(targetUri));
 					changeImage.setId(requestCode);
 
 					isChange = false;
 				} else {
 					LinearLayout postCreateView = (LinearLayout) findViewById(R.id.postCreateView);
 					ImageView newImage = new ImageView(getBaseContext());
 					newImage.setLayoutParams(new LayoutParams(
 							LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
 
 					newImage.setImageBitmap(decodeUri(targetUri));
 					newImage.setId(requestCode);
 					Log.d("addImage Request Code", String.valueOf(requestCode));
 					newImage.setOnLongClickListener(new deleteImageListner());
 					newImage.setOnClickListener(new imageListener());
 
 					postCreateView.addView(newImage);

					imageNum++;
 				}
 
 			} catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		} else {
 			imageId_arr[requestCode - 1] = 0;
 			Log.d("RESULT_CANCEL", "CANCEL");
 		}
 	}
 
 	public void getImageFromGallery(int imageId) {
 		Intent intent = new Intent(Intent.ACTION_PICK,
 				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
 		imageId_arr[imageId - 1] = imageId;
 		intent.setType("image/*");
 		startActivityForResult(intent, imageId);
 	}
 
 	public void saveTask(View button) {
 		EditText postTitlelField = (EditText) findViewById(R.id.postTitle);
 		mPostTitle = postTitlelField.getText().toString();
 		EditText postDescriptionField = (EditText) findViewById(R.id.postDescription);
 		mPostDescription = postDescriptionField.getText().toString();
 		// http://www.mkyong.com/android/android-spinner-drop-down-list-example/
 		// ���ǳ� (select item)
 		// Spinner postCategoryField = (Spinner)
 		// findViewById(R.id.postCategory);
 		mPostCategory = category;
 
 		if (mPostTitle.length() == 0) {
 			// input fields are empty
 			Toast.makeText(this, "제목을 써주세요", Toast.LENGTH_LONG).show();
 			return;
 		} else {
 			// everything is ok!
 			CreateTaskTask createTask = new CreateTaskTask(
 					PostCreateActivity.this);
 			createTask
 					.setMessageLoading("새 글을 등록 중입니다(사진이 많으면 오래 걸릴 수도 있습니다)...");
 			createTask.setAuthToken(mPreferences.getString("AuthToken", ""));
 			createTask.execute(NetworkInfo.CREATE_TASK_ENDPOINT_URL);
 		}
 	}
 
 	private class CreateTaskTask extends UrlJsonAsyncTask {
 		public CreateTaskTask(Context context) {
 			super(context);
 		}
 
 		@Override
 		protected JSONObject doInBackground(String... urls) {
 			DefaultHttpClient client = new DefaultHttpClient();
 			HttpPost post = new HttpPost(urls[0]);
 			JSONObject holder = new JSONObject();
 			JSONObject taskObj = new JSONObject();
 			String response = null;
 			JSONObject json = new JSONObject();
 
			int tmp_flag = 0;
 
 			for (int k = 0; k < 50; k++) {
 				if (imageId_arr[k] > 0) {
					targetImage[tmp_flag] = (ImageView) findViewById(imageId_arr[k]);
					tmp_flag++;
 
					if (tmp_flag == 5) {
 						break;
 					}
 				}
 			}
 
 			for (int i = 0; i < imageNum; i++) {
 				ByteArrayOutputStream imageStream = new ByteArrayOutputStream();
 				BitmapDrawable drawable = (BitmapDrawable) targetImage[i]
 						.getDrawable();
 				Bitmap imageBitmap = drawable.getBitmap();
 				imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100,
 						imageStream);
 				byte[] data = imageStream.toByteArray();
 				imageData[i] = new String(Base64.encode(data, 1));
 				try {
 					imageStream.close();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 
 			try {
 				try {
 					json.put("success", false);
 					json.put("info", "Something went wrong. Retry!");
 					taskObj.put("title", mPostTitle);
 					taskObj.put("category", mPostCategory);
 					taskObj.put("description", mPostDescription);
 					taskObj.put("image1", imageData[0]);
 					taskObj.put("image2", imageData[1]);
 					taskObj.put("image3", imageData[2]);
 					taskObj.put("image4", imageData[3]);
 					taskObj.put("image5", imageData[4]);
 					holder.put("post", taskObj);
 					StringEntity se = new StringEntity(holder.toString(),
 							"utf-8");
 					post.setEntity(se);
 					post.setHeader("Accept", "application/json");
 					post.setHeader("Content-Type", "application/json");
 					post.setHeader("Authorization", "Token token="
 							+ mPreferences.getString("AuthToken", ""));
 
 					ResponseHandler<String> responseHandler = new BasicResponseHandler();
 					response = client.execute(post, responseHandler);
 					json = new JSONObject(response);
 
 				} catch (HttpResponseException e) {
 					e.printStackTrace();
 					Log.e("ClientProtocol", "" + e);
 				} catch (IOException e) {
 					e.printStackTrace();
 					Log.e("IO", "" + e);
 				}
 			} catch (JSONException e) {
 				e.printStackTrace();
 				Log.e("JSON", "" + e);
 			}
 
 			return json;
 		}
 
 		@Override
 		protected void onPostExecute(JSONObject json) {
 			try {
 				if (json.getBoolean("success")) {
 					Intent intent = new Intent(getApplicationContext(),
 							HomeActivity.class);
 					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 					startActivity(intent);
 				}
 				Toast.makeText(context, json.getString("info"),
 						Toast.LENGTH_LONG).show();
 			} catch (Exception e) {
 				Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG)
 						.show();
 			} finally {
 				super.onPostExecute(json);
 				finish();
 			}
 		}
 	}
 
 	private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {
 		BitmapFactory.Options o = new BitmapFactory.Options();
 		o.inJustDecodeBounds = true;
 		BitmapFactory.decodeStream(
 				getContentResolver().openInputStream(selectedImage), null, o);
 
 		final int REQUIRED_SIZE = 300;
 
 		int width_tmp = o.outWidth, height_tmp = o.outHeight;
 		int scale = 1;
 		while (!(width_tmp / 2 < REQUIRED_SIZE && height_tmp / 2 < REQUIRED_SIZE)) {
 			width_tmp /= 2;
 			height_tmp /= 2;
 			scale *= 2;
 		}
 
 		BitmapFactory.Options o2 = new BitmapFactory.Options();
 		o2.inSampleSize = scale;
 		return BitmapFactory.decodeStream(
 				getContentResolver().openInputStream(selectedImage), null, o2);
 	}
 
 	// public void addItemsOnSpinner() {
 	// category = (Spinner) findViewById(R.id.postCategory);
 	// List<String> list = new ArrayList<String>();
 	// list.add(getIntent().getStringExtra("category"));
 	//
 	// ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(this,
 	// android.R.layout.simple_spinner_item, list);
 	// categoryAdapter
 	// .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 	// category.setAdapter(categoryAdapter);
 	// }
 }
