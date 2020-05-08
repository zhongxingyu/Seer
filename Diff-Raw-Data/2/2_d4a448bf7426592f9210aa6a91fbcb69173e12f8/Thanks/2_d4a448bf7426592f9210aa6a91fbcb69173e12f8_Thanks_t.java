 /**
  * 
  */
 package alcaldiadebarranquilla.prohibidoparquear;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.UUID;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.HttpMultipartMode;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import alcaldiadebarranquilla.prohibidoparquear.controller.Manager;
 import alcaldiadebarranquilla.prohibidoparquear.util.AppConfig;
 import alcaldiadebarranquilla.prohibidoparquear.util.AppGlobal;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.CompressFormat;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 /**
  * @author Soldier
  * 
  */
 @SuppressLint("HandlerLeak")
 public class Thanks extends Activity {
 
 	private final String TAG = "THANKS";
 	private LinearLayout thanks_container;
 	private LinearLayout thanks_container_error;
 	private RelativeLayout wait_container;
 	private TextView mensaje;
 	private static final int GLOBAL_ERROR = 1;
 	private static final int COMPLETE_UPLOAD = 2;
 	private static final int IMAGE_ERROR = 3;
 	private MultipartEntity entity;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 
 		// requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		setContentView(R.layout.activity_thanks);
 		AppConfig.setProductionEnviroment();
 
 		this.mensaje = (TextView) findViewById(R.id.error);
 
 		this.thanks_container_error = (LinearLayout) findViewById(R.id.thannks_container_error);
 		this.thanks_container_error.setVisibility(View.GONE);
 
 		this.thanks_container = (LinearLayout) findViewById(R.id.thannks_container);
 		this.thanks_container.setVisibility(View.GONE);
 
 		this.createEntity();
 		this.sendData();
 
 	}
 
 	public void reintentar(View view) {
 		this.thanks_container_error.setVisibility(View.GONE);
 		this.wait_container.setVisibility(View.VISIBLE);
 		sendData();
 	}
 
 	private void sendData() {
 		// save the report
 		new SaveInBackground().execute("");
 	}
 
 	public void exit(View view) {
		System.exit(0);
 	}
 
 	public void nuevoReporte(View view) {
 
 		Manager.getInstance().reset();
 		AppGlobal.getInstance().dispatcher.open(Thanks.this, "take", true);
 
 	}
 
 	public HttpResponse postIncident(String url, MultipartEntity entity) {
 
 		HttpResponse response = null;
 		// Create a new HttpClient and Post Header
 		HttpClient httpclient = new DefaultHttpClient();
 		HttpPost httppost = new HttpPost(url);
 
 		Log.i(TAG, "I'm here HttpResponse");
 
 		try {
 			// Add your data
 			httppost.setHeader("Accept", "application/json");
 			httppost.setEntity(entity);
 			// Execute HTTP Post Request
 			response = httpclient.execute(httppost);
 
 			if (response != null) {
 				HttpEntity data = response.getEntity();
 
 				// Borrar
 				Log.i(TAG, response.getStatusLine().getStatusCode() + "");
 				String responseString = EntityUtils.toString(data);
 				Log.i(TAG, responseString);
 
 				int status = response.getStatusLine().getStatusCode();
 
 				if (status == 200) {
 					try {
 						Log.i(TAG, "Try parse JSONObject");
 						JSONObject json_data = new JSONObject(responseString);
 						Log.i(TAG, "Try parse JSONObject finish");
 
 						String status_response = json_data.getString("status");
 						Log.i(TAG, "status_response => " + status_response);
 
 						if (status_response.equals("true")) {
 							// mensaje.setText(getString(R.string.thanks_layout_title_ok));
 							Log.i(TAG, "ERROR in true!");
 							// this.thanks_container.setVisibility(View.VISIBLE);
 							Manager.getInstance().reset();
 						} else {
 							// mensaje.setText(getString(R.string.thanks_layout_title_error));
 							this.mensaje
 									.setText(getString(R.string.thanks_layout_title_ok_error));
 							this.thanks_container_error
 									.setVisibility(View.VISIBLE);
 						}
 
 					} catch (JSONException e) {
 						// TODO Auto-generated catch block
 						Log.i(TAG, "ERROR!");
 						e.printStackTrace();
 					}
 
 				}
 
 			} else {
 				Log.i(TAG, "Response is null");
 			}
 
 		} catch (ClientProtocolException e) {
 			response = null;
 		} catch (IOException e) {
 			response = null;
 		}
 		return response;
 	}
 
 	@SuppressLint("HandlerLeak")
 	private Handler responseHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			getResponse(msg);
 		}
 	};
 
 	protected void getResponse(Message msg) {
 
 		// AppGlobal.getInstance().hideLoading();
 
 		switch (msg.what) {
 		case IMAGE_ERROR:
 
 			mensaje.setText(R.string.thanks_layout_title_error_image);
 			this.thanks_container_error.setVisibility(View.VISIBLE);
 			break;
 
 		case GLOBAL_ERROR:
 
 			new AlertDialog.Builder(this)
 					.setIcon(android.R.drawable.ic_dialog_alert)
 					.setTitle(R.string.dialog_no_internet_title)
 					.setMessage(R.string.dialog_no_internet_content)
 					.setPositiveButton(
 							R.string.dialog_no_internet_btn_reintentar,
 							new DialogInterface.OnClickListener() {
 
 								@Override
 								public void onClick(DialogInterface dialog,
 										int which) {
 									dialog.cancel();
 									sendData();
 								}
 							})
 					.setNegativeButton(
 							R.string.dialog_no_internet_btn_cancelar,
 							new DialogInterface.OnClickListener() {
 
 								@Override
 								public void onClick(DialogInterface dialog,
 										int which) {
 									AppGlobal.getInstance().dispatcher.open(
 											Thanks.this, "take", true);
 								}
 
 							}).show();
 			break;
 
 		case COMPLETE_UPLOAD:
 
 			// mensaje.setText(R.string.thanks_layout_title_ok_content);
 			thanks_container.setVisibility(View.VISIBLE);
 
 			this.wait_container = (RelativeLayout) findViewById(R.id.wait_container);
 			this.wait_container.setVisibility(View.GONE);
 
 			break;
 		}
 	}
 
 	private void createEntity() {
 
 		// Build params list
 		this.entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
 
 		boolean entityDone = true;
 
 		try {
 
 			String addres = Manager.getInstance().getAddress();
 			if (addres == null) {
 				addres = "";
 			}
 
 			entity.addPart("application_token", new StringBody(
 					AppConfig.PUSHER_APP_KEY));
 			entity.addPart("key", new StringBody(AppConfig.PUSHER_KEY));
 			entity.addPart("event[longitude]", new StringBody(Manager
 					.getInstance().getLongitude()));
 			entity.addPart("event[latitude]", new StringBody(Manager
 					.getInstance().getLatitude()));
 			entity.addPart("event[device_model]",
 					new StringBody(Manager.getInstance().PhoneModel + " - "
 							+ Manager.getInstance().AndroidVersion));
 			entity.addPart("event[category_id]", new StringBody(Manager
 					.getInstance().getSelectedCategory() + ""));
 			entity.addPart("event[address_description]", new StringBody(addres));
 			entity.addPart("event[data]", new StringBody(""));
 			entity.addPart("event[description]", new StringBody(""));
 
 			entityDone = true;
 
 		} catch (UnsupportedEncodingException e) {
 			Log.i(TAG, "UnsupportedEncodingException => entityDone = false");
 			entityDone = false;
 		} finally {
 
 			if (entityDone) {
 				// Save the files
 
 				for (Bitmap image : Manager.getInstance().getImages()) {
 
 					String imagePath = saveImage(image);
 
 					Log.i(TAG, "Image saved at: " + imagePath);
 
 					if (imagePath != null) {
 						FileBody body = new FileBody(new File(imagePath),
 								"image/jpeg");
 						String tencodign = body.getTransferEncoding();
 						Log.i(TAG, tencodign);
 						entity.addPart("event[photos_attributes][][image]",
 								body);
 					} else {
 						Log.e(TAG, "Error guardando imagen");
 					}
 
 				}
 
 			} else {
 				responseHandler.sendEmptyMessage(GLOBAL_ERROR);
 			}
 		}
 
 	}
 
 	private String saveImage(Bitmap image) {
 
 		String the_path = Environment.getExternalStorageDirectory()
 				+ File.separator + "prohibidoparquear";
 		File root = new File(the_path);
 		boolean isReady = root.exists();
 
 		if (!isReady) {
 			try {
 				if (Environment.getExternalStorageDirectory().canWrite()) {
 					root.mkdirs();
 					isReady = true;
 				}
 			} catch (Exception e) {
 				isReady = false;
 			}
 
 		} else {
 
 			String uid = UUID.randomUUID().toString();
 			String the_file = the_path + File.separator + uid + ".jpg";
 			OutputStream stream;
 
 			boolean done = false;
 
 			try {
 				stream = new FileOutputStream(the_file);
 				image.compress(CompressFormat.JPEG, 80, stream);
 				stream.flush();
 				stream.close();
 				done = true;
 			} catch (FileNotFoundException e) {
 				done = false;
 			} catch (IOException e) {
 				done = false;
 			} finally {
 				if (done) {
 					return the_file;
 				}
 			}
 
 		}
 
 		return null;
 	}
 
 	private class SaveInBackground extends AsyncTask<String, Integer, Void> {
 
 		@Override
 		protected Void doInBackground(String... params) {
 
 			// Call to post incident
 			if (postIncident(AppConfig.ADD_EVENT_URL, entity) != null) {
 				Message msg = new Message();
 				msg.what = COMPLETE_UPLOAD;
 				responseHandler.sendMessage(msg);
 			} else {
 				responseHandler.sendEmptyMessage(GLOBAL_ERROR);
 			}
 
 			return null;
 		}
 
 	}
 
 }
