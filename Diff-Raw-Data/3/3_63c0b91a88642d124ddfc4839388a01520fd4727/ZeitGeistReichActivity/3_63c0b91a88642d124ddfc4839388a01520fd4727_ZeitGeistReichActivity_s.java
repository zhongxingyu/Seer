 package de.golov.zeitgeistreich;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.provider.MediaStore.Images.ImageColumns;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.MultiAutoCompleteTextView;
 import android.widget.Toast;
 
 import java.io.File;
 
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.ContentBody;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 public class ZeitGeistReichActivity extends Activity {
 
 	private class ZeitGeistObject {
 		public File image;
 		public String tags;
 
 		public ZeitGeistObject(File image, String tags) {
 			this.image = image;
 			this.tags = tags;
 		}
 	}
 
 	private class ZeitGeistReichUploaderTask extends AsyncTask<ZeitGeistObject,Void,Void>{
 		private ProgressDialog Dialog = new ProgressDialog(ZeitGeistReichActivity.this);
 		private String Error = null;
 
 		protected void onPreExecute() {
 			Dialog.setMessage("Uploading image...");
 			Dialog.show();
 		}
 
 		protected void onPostExecute(Void unused) {
 			Dialog.dismiss();
 			if (Error != null) {
 				Toast.makeText(ZeitGeistReichActivity.this, Error, Toast.LENGTH_SHORT).show();
 			} else {
 				Toast.makeText(ZeitGeistReichActivity.this, "Image uploaded.", Toast.LENGTH_SHORT).show();
				finish();
 			}
 		}
 
 		protected Void doInBackground(ZeitGeistObject... files) {
 			for (ZeitGeistObject file: files) {
 				try {
 
 					HttpClient httpclient = new DefaultHttpClient();
 
 					HttpPost httppost = new HttpPost("http://zeitgeist.li/new");
 
 					MultipartEntity mpEntity = new MultipartEntity();
 					ContentBody cbFile = new FileBody(file.image);
 					mpEntity.addPart("image_upload", cbFile);
 					StringBody tags = new StringBody(file.tags);
 					mpEntity.addPart("tags", tags);
 					httppost.setEntity(mpEntity);
 					httpclient.execute(httppost);
 				} catch (Exception e) {
 					Error = e.toString();
 				}
 
 			}
 			return null;
 		}
 	}
 
 	private String[] tag_suggests = new String[] {
 			"comic", "screenshot", "animated", "wallpaper", "cat", "girl", "fail",
 			"explosm", "nerd", "facebook", "hardware", "minecraft", "cloth",
 			"motivational", "linux", "star trek", "nsfw", "politics", "apple",
 			"gay", "zensursula", "ani", "my little pony", "lol", "xkcd", "dope",
 			"cute", "game", "skyrim", "meme", "stasi2.0", "emma watson", "girls",
 			"video", "wtf", "piraten", "piratenpartei", "cover", "music",
 			"summerglau", "google+", "dance", "sexy", "star wars", "debian",
 			"android", "dog", "house", "obama", "steve", "emo", "epic", "picard",
 			"remix", "animation", "fdp", "food", "google", "jessor", "awesome",
 			"notebook", "penis", "berlin", "cdu", "felicia_day", "fun", "goth",
 			"iphone", "party", "trailer", "world", "anime", "ccc", "dnb", "dogs",
 			"geek", "lesbian", "microsoft", "youtube", "computer", "education",
 			"guitar", "pepper spray cop", "porn", "schischa", "water", "club-mate",
 			"drogen", "fire", "jesse", "jesse keys", "mac", "notch", "siyb",
 			"summer glau", "tattoo", "vim", "chuck", "fefe", "hacker"
 	};
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		final ImageView imagePreview = (ImageView) findViewById(R.id.ImagePreview);
 		Intent intent = getIntent();
 		Bundle extras = intent.getExtras();
 		Uri mImageUri = null;
 		if (Intent.ACTION_SEND.equals(intent.getAction()) && extras != null) {
 			if (extras.containsKey(Intent.EXTRA_STREAM)) {
 				mImageUri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
 				if (mImageUri != null) {
 					imagePreview.setImageURI(mImageUri);
 				}
 			}
 		}
 		
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_dropdown_item_1line, tag_suggests);
 		final MultiAutoCompleteTextView textView = (MultiAutoCompleteTextView) findViewById(R.id.TagEditView);
 		textView.setAdapter(adapter);
 		textView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
 
 		final Button button = (Button) findViewById(R.id.ZeitgeistSubmit);
 		button.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				submitImage(textView.getText().toString());
 			}
 		});
 	}
 
 	protected void submitImage(String tags) {
 		Intent intent = getIntent();
 		Bundle extras = intent.getExtras();
 		Uri mImageUri = null;
 		File mFilename = null;
 		if (Intent.ACTION_SEND.equals(intent.getAction()) && extras != null) {
 			if (extras.containsKey(Intent.EXTRA_STREAM)) {
 				mImageUri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
 				if (mImageUri != null) {
 					Cursor cursor = getContentResolver().query(mImageUri, null, null, null, null);
 					if (cursor.moveToFirst()) {
 						mFilename = new File( cursor.getString(cursor.getColumnIndexOrThrow(ImageColumns.DATA)));
 					}
 					cursor.close();
 					if (mFilename != null) {
 						ZeitGeistReichUploaderTask task = new ZeitGeistReichUploaderTask();
 						ZeitGeistObject o = new ZeitGeistObject(mFilename, tags);
 						task.execute(o);
 					}
 				}
 			}
 		}
 	}
 }
