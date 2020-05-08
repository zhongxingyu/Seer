 package com.mendroid.sky;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 import com.mendroid.connection.ImageCallbackListener;
 import com.mendroid.connection.ImageServerConnector;
 import com.mendroid.connection.ImageUploader;
 import com.mendroid.connection.ImageUploaderCallbackListener;
 import com.mendroid.connection.JSONRequestCallbackListener;
 import com.mendroid.connection.JSONServerConnector;
 import com.mendroid.connection.ServerConnector;
 import com.mendroid.connection.SimpleRequestCallbackListener;
 import com.mendroid.connection.SimpleServerConnector;
 import com.mendroid.sky.R;
 import com.mendroid.structures.DishStruct;
 import com.mendroid.structures.ExtraDishInfo;
 import com.mendroid.structures.FoodAttribute;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.preference.PreferenceManager;
 import android.provider.MediaStore;
 import android.provider.Settings.Secure;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class DishDetail extends Activity implements
 		JSONRequestCallbackListener<ExtraDishInfo>, ImageCallbackListener,
 		ImageUploaderCallbackListener, SimpleRequestCallbackListener {
 
 	private DishStruct mDish;
 	private TextView titleView;
 	private TextView descView;
 	private boolean advanced;
 	private boolean debug;
 
 	private ImageView dishImage;
 	private TableRow comCapRow;
 	private TextView comText;
 	private TableRow dbgRow;
 	private TextView dishDbgText;
 
 	private String host;
 	private String uri;
 	private long myID;
 	private String comments;
 	private ProgressDialog pgDia;
 
 	private ServerConnector currentRequest;
 	private Uri imageUri;
 	private String alDiaTit;
 	private String alDiaTxt;
 	private Bitmap imgToUpload;
 
 	public final static String CACHE_DIR = "imgcache";
 	private static final int CAMERA_REQ_CODE = 8848;
 	private static final int ALERT_DIA_ID = 5782;
	private static final String IMGURI_KEY = "IMAGEURI";
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.dishdet);
 
 		titleView = (TextView) findViewById(R.id.titleText);
 		descView = (TextView) findViewById(R.id.descText);
 		comments = "";
 
 		currentRequest = null;
 		pgDia = null;
		
		if (savedInstanceState != null) {
			imageUri = (Uri) savedInstanceState.getParcelable(IMGURI_KEY);
			Log.d("Mendroid", "Image URI restored");
		} else {
			imageUri = null;
		}
		
 
 		advancedView();
 
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 
 		mDish = (DishStruct) getIntent().getExtras().getSerializable("DISH");
 
 		if (mDish == null) {
 			titleView.setText("NO DATA");
 			return;
 		}
 
 		titleView.setText(mDish.getName());
 
 		if (mDish.getDescription().length() == 0) {
 			descView.setText("Keine Beschreibung");
 		} else {
 			descView.setText(mDish.getDescription());
 		}
 
 		descView.append("\n" + String.format("%.2f", mDish.getPrice()) + ""
 				+ "\n");
 
 		final FoodAttribute[] fas = mDish.getAttributes();
 		if (fas != null && fas.length > 0) {
 			for (FoodAttribute tmpAt : fas) {
 				descView.append("\n" + tmpAt.getDesc());
 			}
 		}
 		dbgOut("onStart finished");
 
 		if (advanced && !loadCache())
 			requestExtra();
 
 	}
 
 	private void advancedView() {
 		SharedPreferences preferences = PreferenceManager
 				.getDefaultSharedPreferences(this);
 		advanced = preferences.getBoolean(
 				getString(R.string.KEY_ADVANCED_CHKBX), false);
 		debug = preferences
 				.getBoolean(getString(R.string.KEY_DBG_CHECK), false);
 
 		host = preferences.getString(getString(R.string.KEY_DEF_HOST),
 				getString(R.string.default_host));
 
 		uri = "http://" + host + "/mendroidbackend/request/";
 
 		myID = getSystemID();
 
 		dishImage = (ImageView) findViewById(R.id.dishImage);
 		comCapRow = (TableRow) findViewById(R.id.comCapRow);
 		comText = (TextView) findViewById(R.id.comText);
 		dbgRow = (TableRow) findViewById(R.id.debugRow);
 		dishDbgText = (TextView) findViewById(R.id.dishDbgText);
 
 		dishImage.setVisibility(View.GONE);
 		comCapRow.setVisibility(View.GONE);
 		comText.setVisibility(View.GONE);
 
 		if (!debug) {
 			dbgRow.setVisibility(View.GONE);
 		}
 
 		dbgOut("HOST: " + host);
 
 	}
 
 	private void requestExtra() {
 		dbgOut("Requesting Additional Info");
 
 		JSONServerConnector<ExtraDishInfo> reqCon = new JSONServerConnector<ExtraDishInfo>(
 				uri, String.valueOf(myID), 10000, ExtraDishInfo.class, this);
 		String[] request = new String[2];
 		request[0] = "REQ_EXTRA_INFO";
 		request[1] = getDishID();
 		reqCon.request(request);
 		currentRequest = reqCon;
 		dbgOut("Awaiting response");
 
 	}
 
 	private void dbgOut(String msg) {
 		if (debug) {
 			dishDbgText.append(msg + "\n");
 		}
 	}
 
 	@Override
 	public void onJSONRequestCallback(ExtraDishInfo result, int errorCode) {
 		currentRequest = null;
 		if (errorCode == ServerConnector.ERR_SUCCESS) {
 			dbgOut("Got Extras.");
 			dbgOut("Exists: " + String.valueOf(result.dishExists()));
 			dbgOut("Comments: " + String.valueOf(result.hasComment()));
 			dbgOut("Image: " + String.valueOf(result.hasImage()));
 
 			comCapRow.setVisibility(View.VISIBLE);
 			comText.setVisibility(View.VISIBLE);
 
 			if (result.hasComment()) {
 				comText.setText(result.getComments());
 				comments = result.getComments();
 			}
 
 			if (result.hasImage()) {
 				requestImage();
 			} else {
 				dishImage.setVisibility(View.VISIBLE);
 			}
 
 		} else {
 			dbgOut("JSONReq Error:" + String.valueOf(errorCode));
 			if (errorCode == ServerConnector.ERR_ABORTED) {
 				return;
 			}
 			Toast.makeText(
 					this,
 					"Konnte Kommentare nicht laden. EC: "
 							+ String.valueOf(errorCode), Toast.LENGTH_SHORT)
 					.show();
 		}
 
 	}
 
 	private void requestImage() {
 		dbgOut("Requesting image");
 		ImageServerConnector reqCon = new ImageServerConnector(uri,
 				String.valueOf(myID), 10000, this);
 		String[] request = new String[1];
 		request[0] = getDishID();
 		reqCon.request(request);
 		currentRequest = reqCon;
 		dbgOut("Awaiting response");
 
 	}
 
 	private long getSystemID() {
 		String andID = Secure.getString(this.getContentResolver(),
 				Secure.ANDROID_ID);
 		if (andID == null || andID == "") {
 			andID = "EMULATOR";
 		}
 		final String md5Str = md5(andID);
 		BigInteger id = new BigInteger(md5Str, 16);
 		return id.longValue();
 	}
 
 	private static String md5(String s) {
 		try {
 			// Create MD5 Hash
 			MessageDigest digest = java.security.MessageDigest
 					.getInstance("MD5");
 			digest.update(s.getBytes());
 			byte messageDigest[] = digest.digest();
 
 			// Create Hex String
 			StringBuffer hexString = new StringBuffer();
 			for (int i = 0; i < messageDigest.length; i++)
 				hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
 			return hexString.toString();
 
 		} catch (NoSuchAlgorithmException e) {
 			e.printStackTrace();
 		}
 		return "";
 	}
 
 	@Override
 	public void onImageCallback(Bitmap result, int errorCode) {
 		currentRequest = null;
 		if (errorCode == ServerConnector.ERR_SUCCESS) {
 			dbgOut("Got Image.");
 			dishImage.setImageBitmap(result);
 			dishImage.setVisibility(View.VISIBLE);
 			saveCache(result, comments);
 
 		} else {
 			dbgOut("IMAGEReq Error:" + String.valueOf(errorCode));
 			if (errorCode == ServerConnector.ERR_ABORTED) {
 				return;
 			}
 			Toast.makeText(
 					this,
 					"Konnte Bild nicht laden. EC: " + String.valueOf(errorCode),
 					Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	private boolean loadCache() {
 		Log.v("Mendroid", "Loading dish cache");
 		String imgfilename = getDir("imagecache", Context.MODE_PRIVATE)
 				.getPath();
 		String comfilename = imgfilename + getDishID() + ".txt";
 		imgfilename += "/" + getDishID() + ".jpg";
 		File img = new File(imgfilename);
 		File com = new File(comfilename);
 
 		if (!(img.exists() && com.exists())) {
 			Log.v("Mendroid", "No dish cache found.");
 		} else {
 			Bitmap bmp = BitmapFactory.decodeFile(img.getAbsolutePath());
 			if (bmp != null) {
 				dishImage.setImageBitmap(bmp);
 				dishImage.setVisibility(View.VISIBLE);
 
 				try {
 					BufferedReader br = new BufferedReader(new FileReader(com));
 					comments = "";
 					String line;
 					while ((line = br.readLine()) != null) {
 						comments += line + "\n";
 					}
 					comments = comments.trim();
 				} catch (Exception e) {
 					Log.w("Mendroid",
 							"Reading Comfile failed: " + e.getMessage());
 					return false;
 				}
 				comCapRow.setVisibility(View.VISIBLE);
 				comText.setVisibility(View.VISIBLE);
 
 				if (comments.length() < 3) {
 					comText.setText(getString(R.string.CAP_NOCOM));
 				} else {
 					comText.setText(comments);
 				}
 
 				Log.v("Mendroid", "Got dish cache");
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 		if (currentRequest != null) {
 			if (currentRequest.isBusy()) {
 				Log.v("Mendroid", "Aborting request");
 				currentRequest.abort();
 			}
 		}
 	}
 
 	public String getDishID() {
 		final String dishName = mDish.getName();
 		if (dishName == null)
 			return "";
 		String tmp = dishName.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
 		return (tmp.length() > 13) ? tmp.substring(0, 13) : tmp;
 	}
 
 	private void saveCache(Bitmap bmp, String comments) {
 		Log.v("Mendroid", "Saving dish Cache");
 		String imgfilename = getDir("imagecache", Context.MODE_PRIVATE)
 				.getPath();
 		String comfilename = imgfilename + getDishID() + ".txt";
 		imgfilename += "/" + getDishID() + ".jpg";
 
 		Log.v("Mendroid", "File: " + imgfilename);
 
 		File comFile = new File(comfilename);
 		FileOutputStream fos = null;
 		ByteArrayOutputStream out = null;
 		try {
 			// Image
 			fos = new FileOutputStream(imgfilename);
 			out = new ByteArrayOutputStream();
 			bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
 			out.writeTo(fos);
 			out.close();
 			// Comments
 			BufferedWriter output = new BufferedWriter(new FileWriter(comFile));
 			output.write(comments);
 			output.close();
 
 		} catch (IOException e) {
 			Log.w("Mendroid",
 					"IO Exception while Saving dish Cache: " + e.getMessage());
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		if (advanced) {
 			Log.v("Mendroid", "Inflating Dish menu");
 			MenuInflater inflater = getMenuInflater();
 			inflater.inflate(R.menu.dishmen, menu);
 		}
 		return true;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (!advanced)
 			return super.onOptionsItemSelected(item);
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.it_refresh_dish:
 			requestExtra();
 			return true;
 		case R.id.it_write_com:
 			writeComment();
 			return true;
 		case R.id.it_up_img:
 			startCamera();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	private void startCamera() {
 		Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
 		File photo = new File(Environment.getExternalStorageDirectory(),
 				"upload.jpg");
 		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
 		imageUri = Uri.fromFile(photo);
 		startActivityForResult(intent, CAMERA_REQ_CODE);
 	}
 
 	private void writeComment() {
 		showAlertDialog("Epic Fail", "TODO: Implementieren..");
 	}
 
 	private void requestUploadSession() {
 		SimpleServerConnector sessionReq = new SimpleServerConnector(uri,
 				String.valueOf(myID), 10000, this);
 		String[] request = new String[2];
 		request[0] = "REQ_UPLOAD_SESSION";
 		request[1] = getDishID();
 		Log.v("Mendroid", "Requesting upload session");
 		dbgOut("Requesting Upload Session");
 		sessionReq.request(request);
 	}
 
 	private void uploadImage(String sessionCode) {
 		ImageUploader iu = new ImageUploader(host, this);
 		dbgOut("Starte upload");
 		iu.startUpload(imgToUpload, sessionCode);
 	}
 
 	@Override
 	public void onSimpleRequestCallback(String result, int errorCode) {
 		Log.v("Mendroid", "Simple Request callback: " + result);
 	
 		if (errorCode == ServerConnector.ERR_SUCCESS) {
 			if (result.equals("REJECTED")) {
 				pgDia.dismiss();
 				showAlertDialog("Upload request",
 						"Upload abgelehnt. (Keine Berechtigung)");
 			} else if (result.equals("DISH NOT FOUND")) {
 				pgDia.dismiss();
 				showAlertDialog("Upload request",
 						"Upload abgelehnt. (Konnte nicht zugeordnet werden)");
 			} else {
 				dbgOut("Sessionkey erhalten.");
 				pgDia.setMessage("Lade hoch...");
 				Log.v("Mendroid", "Upload permission granted: " + result);
 				uploadImage(result);
 			}
 		} else {
 			pgDia.dismiss();
 			showAlertDialog("Callback",
 					"Upload Request Error " + String.valueOf(errorCode));
 		}
 
 	}
 
 	private void showAlertDialog(String title, String text) {
 		alDiaTit = title;
 		alDiaTxt = text;
 		showDialog(ALERT_DIA_ID);
 	}
 
 	protected Dialog onCreateDialog(int id) {
 		AlertDialog.Builder builder;
 		switch (id) {
 		case ALERT_DIA_ID:
 			builder = new AlertDialog.Builder(this);
 			builder.setTitle(alDiaTit);
 			builder.setMessage(alDiaTxt);
 			builder.setIcon(android.R.drawable.ic_dialog_alert);
 			builder.setPositiveButton(getString(R.string.MSG_OK), null);
 			return builder.create();
 		default:
 			return null;
 		}
 	}
 
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == CAMERA_REQ_CODE) {
 			if (resultCode == RESULT_OK) {
 				dbgOut("Camera Okay");
 				Log.v("Mendroid", "Camera Okay");
 				Uri selectedImage = imageUri;
 				getContentResolver().notifyChange(selectedImage, null);
 				ContentResolver cr = getContentResolver();
 				Bitmap bitmap = null;
 				try {
 					bitmap = android.provider.MediaStore.Images.Media
 							.getBitmap(cr, selectedImage);
 
 				} catch (Exception e) {
 					dbgOut("Failed to load");
 					Log.e("Mendroid", e.toString());
 				}
 
 				if (bitmap != null) {
 					dbgOut("BMP okay");
 					imgToUpload = bitmap;
 					pgDia = ProgressDialog.show(this, "Uploading",
 							"Uploadsession anfordern..", true);
 					requestUploadSession();
 				} else {
 					dbgOut("BMP null");
 				}
 
 			} else {
 				dbgOut("Camera failed");
 			}
 		}
 	}
 
 	@Override
 	public void onImageUploadCallback(int errorCode) {
 		Log.v("Mendroid", "Uploader Callback: " + String.valueOf(errorCode));
 		pgDia.dismiss();
 		if (errorCode == ImageUploader.ERR_SUCCESS) {
 			Toast.makeText(this, "Upload erfolgreich.", Toast.LENGTH_SHORT)
 					.show();
 			requestExtra();
 		} else {
 			showAlertDialog("Uploader",
 					"Upload Error " + String.valueOf(errorCode));
 		}
 
	} 
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(IMGURI_KEY, imageUri);
 	}
 }
