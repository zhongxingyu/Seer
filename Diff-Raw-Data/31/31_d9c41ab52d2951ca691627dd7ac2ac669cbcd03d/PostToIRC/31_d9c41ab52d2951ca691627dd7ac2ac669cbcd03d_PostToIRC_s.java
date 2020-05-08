 package epeli.ircshare;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpVersion;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.InputStreamBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.CoreProtocolPNames;
 
 import android.app.Activity;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.res.AssetFileDescriptor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.ClipboardManager;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class PostToIRC extends Activity {
 	public static final String PREFS_NAME = "IRCSharePrefs";
 	
 	Bundle extras;
 	Intent intent;
 	String action;
 	
 	Button send;
 	TextView status;
 	EditText channel;
 	EditText nick;
 	EditText caption;
 	Spinner network;
 
 
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		
 		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 		
 		
 		ImageView image = (ImageView) findViewById(R.id.imageview);
 		
 		network = (Spinner) findViewById(R.id.network);
 		channel = (EditText) findViewById(R.id.channel);
 		nick = (EditText) findViewById(R.id.nick);
 		caption = (EditText) findViewById(R.id.caption);
 		
 		status = (TextView) findViewById(R.id.status);
 		send = (Button) findViewById(R.id.send);
 		
 		channel.setText(settings.getString("channel", "#"));
 		nick.setText(settings.getString("nick", ""));
 		
 		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
 		            this, R.array.networks, android.R.layout.simple_spinner_item);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		network.setAdapter(adapter);		
 		
 
 		intent = getIntent();
 		extras = intent.getExtras();
 		action = intent.getAction();
 		
 		if (isCalledByGallery()){
 			setupSmallImg(image);
 		}
 		else {
 			notify("Not called by gallery. Nothing to upload.");
 			setResult(RESULT_OK);
 			finish();
 		}
 		
 		
 		send.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				if (!isCalledByGallery()){
 					PostToIRC.this.notify("Not called by gallery. Nothing to upload.");
 				}				
 				
 				
 				if (channel.getText().length() <= 1) {
 					PostToIRC.this.notify("Set channel");
 					return;
 				}
 				if (nick.getText().length() == 0) {
 					PostToIRC.this.notify("Set nick name");
 					return;
 				}
 				
 				new UploadImageTask().execute();
 
 				
 			}
 		});
 		
 
 	}
 	
 	protected void onPause () {
 		super.onPause();
 		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
 		Editor editor = settings.edit();
 		editor.putString("channel", channel.getText().toString());
 		editor.putString("nick", nick.getText().toString());
 		editor.commit();
		notify("saved");
 	}
 	
 	private boolean isCalledByGallery(){
 		return Intent.ACTION_SEND.equals(action) && extras.containsKey(Intent.EXTRA_STREAM);
 	}
 	
 	private void notify(CharSequence text) {
 		Context context = getApplicationContext();
 		Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
 		toast.show();		
 		status.setText(text);
 	}
 	
 	private InputStream getImgInputStream() throws FileNotFoundException {
 		// Get resource path from intent callee
 		Uri uri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
 		
 		// Query gallery for camera picture via
 		// Android ContentResolver interface
 		ContentResolver cr = getContentResolver();
 		return cr.openInputStream(uri);
 		
 	}
 	
 	private Uri getImgUri() {
 		return (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
 	}
 	
 	private AssetFileDescriptor getImgFile() throws FileNotFoundException {
 		ContentResolver cr = getContentResolver();
 		return cr.openAssetFileDescriptor(getImgUri(), "r");
 	}
 	
 	private String getImgMimeType() {
 		ContentResolver cr = getContentResolver();
 		return cr.getType(getImgUri());
 	}
 	
 	private void setupSmallImg(ImageView img) {
 		
 		Bitmap bitmap;
 		try {
 			bitmap = BitmapFactory.decodeStream(getImgInputStream());
			int scale = bitmap.getHeight() / bitmap.getWidth();
			img.setImageBitmap( Bitmap.createScaledBitmap(bitmap, 400, 400 / scale, true) );
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 	}
 	
 	private class UploadImageTask extends AsyncTask<Void, Void, Void> {
 		
 		String res;
 		
 		private void postNew(){
 			
 			HttpClient httpclient = new DefaultHttpClient();
 			httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
 			HttpPost httppost = new HttpPost("http://kamino.kortex.jyu.fi:1337/upload");
 			try {
 			  MultipartEntity entity = new MultipartEntity();
 			  entity.addPart("nick", new StringBody(nick.getText().toString()));
 			  entity.addPart("channel", new StringBody(channel.getText().toString()));
 			  entity.addPart("caption", new StringBody(caption.getText().toString()));
 			  entity.addPart("network", new StringBody(network.getSelectedItem().toString()));
 			  
 			  
 			  entity.addPart("picdata", new InputStreamBody(getImgInputStream(), getImgMimeType(), "pic.jpg"));
 			  
 			  httppost.setEntity(entity);
 			  ResponseHandler<String> responseHandler = new BasicResponseHandler();
 			  res = httpclient.execute(httppost, responseHandler);
 			  
 			} catch (ClientProtocolException e) {
 			} catch (IOException e) {
 			}
 			     
 			
 		}
 	
 
 		@Override
 		protected Void doInBackground(Void... params) {
 			postNew();
 			return null;
 		}
 		
 
 		public void onPreExecute() {
 			PostToIRC.this.notify("Sending image...");
 		}
 		
 		public void onPostExecute(Void n) {
			ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 

			clipboard.setText(res);
			PostToIRC.this.notify("Image sent! Copied url to clipboard: " + res);
 		}
 	
 
 	
 	}
 	
 }
