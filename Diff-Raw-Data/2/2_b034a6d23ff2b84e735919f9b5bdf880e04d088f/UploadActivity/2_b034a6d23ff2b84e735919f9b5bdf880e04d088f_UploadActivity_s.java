 package org.qless.up666;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.net.MalformedURLException;
 import java.net.ProtocolException;
 import java.net.URL;
 import java.net.UnknownHostException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.provider.MediaStore.Images;
 import android.text.ClipboardManager;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * @author quattro
  * 
  */
 public class UploadActivity extends Activity {
 
 	private TextView mMimeTypeTextView;
 	private TextView mFilePathTextView;
 	private TextView mImageURLTextView;
 	private ProgressBar mProgress;
 	private TextView mGreeting;
 	private Button mCopyButton;
 	private Button mShareButton;
 
 	private String imageURL;
 
 	private Exception ex;
 	private Error error;
 
 	public enum Error {
 		FILE_NOT_FOUND, HOST_NOT_FOUND, NETWORK, BAD_URL
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onCreate(android.os.Bundle)
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Log.d("ullilog", "start");
 		Intent intent = getIntent();
 		Log.d("ullilog", "got intent");
 		setContentView(R.layout.upload);
 
 		mGreeting = (TextView) findViewById(R.id.hello);
 		mMimeTypeTextView = (TextView) findViewById(R.id.mimeType);
 		mFilePathTextView = (TextView) findViewById(R.id.filePath);
 		mImageURLTextView = (TextView) findViewById(R.id.imageURL);
 		mProgress = (ProgressBar) findViewById(R.id.progressBarUpload);
 		mCopyButton = (Button) findViewById(R.id.buttonCopy);
 		mShareButton = (Button) findViewById(R.id.buttonShare);
 
 		mGreeting.setText(getString(R.string.uploadAt) + " "
 				+ getString(R.string.imageHoster));
 
 		mProgress.setVisibility(ProgressBar.INVISIBLE);
 		mCopyButton.setEnabled(false);
 		mShareButton.setEnabled(false);
 
 		mCopyButton.setOnClickListener(new Button.OnClickListener() {
 			public void onClick(View v) {
 				ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
 				clipboard.setText(imageURL);
 				Context context = getApplicationContext();
 				CharSequence text = getString(R.string.copyToast);
 				int duration = Toast.LENGTH_SHORT;
 				Toast toast = Toast.makeText(context, text, duration);
 				toast.show();
 			}
 		});
 
 		mShareButton.setOnClickListener(new Button.OnClickListener() {
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Intent i = new Intent(android.content.Intent.ACTION_SEND);
 				i.setType("text/plain");
 				i.putExtra(Intent.EXTRA_SUBJECT,
 						getString(R.string.share_subject));
 				i.putExtra(Intent.EXTRA_TEXT, imageURL);
 				startActivity(Intent.createChooser(i,
 						getString(R.string.share_title)));
 
 			}
 
 		});
 
 		Log.d("ullilog", "about to proccess intent");
 
 		if (Intent.ACTION_SEND.equals(intent.getAction())) {
 			Bundle extras = intent.getExtras();
 			if (extras.containsKey(Intent.EXTRA_STREAM)) {
 				Uri uri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
 				String scheme = uri.getScheme();
 				Log.d("ullilog", "content scheme is: " + scheme);
 				boolean ok = false;
 				String mimeType = null;
 				String filePath = null;
 				if (scheme.equals("content")) {
 					mimeType = intent.getType();
 					ContentResolver contentResolver = getContentResolver();
 					Cursor cursor = contentResolver.query(uri, null, null,
 							null, null);
 					cursor.moveToFirst();
 					filePath = cursor.getString(cursor
 							.getColumnIndexOrThrow(Images.Media.DATA));
 					ok = true;
 				} else if (scheme.equals("file")) {
 					mimeType = intent.getType();
					filePath = uri.toString().substring("file://".length());
 					ok = true;
 				} else {
 					Log.d("ullilog", "no content scheme, is: " + scheme);
 					Context context = getApplicationContext();
 					CharSequence text = "no content scheme";
 					int duration = Toast.LENGTH_SHORT;
 					Toast toast = Toast.makeText(context, text, duration);
 					toast.show();
 				}
 				if (ok) {
 					mMimeTypeTextView.setText(mimeType);
 					mFilePathTextView.setText(filePath);
 					new ImageUploadTask().execute(filePath);
 				}
 			} else {
 				Log.d("ullilog", "no EXTRA_STREAM");
 				Context context = getApplicationContext();
 				CharSequence text = "no EXTRA_STREAM";
 				int duration = Toast.LENGTH_SHORT;
 				Toast toast = Toast.makeText(context, text, duration);
 				toast.show();
 			}
 		} else {
 			Log.d("ullilog", "no ACTION_SEND");
 			Context context = getApplicationContext();
 			CharSequence text = "no ACTION_SEND";
 			int duration = Toast.LENGTH_SHORT;
 			Toast toast = Toast.makeText(context, text, duration);
 			toast.show();
 		}
 	}
 
 	/**
 	 * @author quattro
 	 * 
 	 */
 	private class ImageUploadTask extends AsyncTask<String, Integer, URL> {
 
 		private Exception ex;
 		private Error error;
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see android.os.AsyncTask#onPreExecute()
 		 */
 		@Override
 		protected void onPreExecute() {
 			mProgress.setVisibility(ProgressBar.VISIBLE);
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see android.os.AsyncTask#doInBackground(Params[])
 		 */
 		@Override
 		protected URL doInBackground(String... params) {
 			URL url = null;
 			try {
 				url = ImageUploader.upload(params[0]);
 			} catch (FileNotFoundException e) {
 				error = Error.FILE_NOT_FOUND;
 			} catch (UnknownHostException e) {
 				error = Error.HOST_NOT_FOUND;
 			} catch (MalformedURLException e) {
 				error = Error.BAD_URL;
 				ex = e;
 			} catch (ProtocolException e) {
 				ex = e;
 			} catch (IOException e) {
 				error = Error.NETWORK;
 			} catch (Exception e) {
 				ex = e;
 			}
 			return url;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
 		 */
 		@Override
 		protected void onPostExecute(URL result) {
 			mProgress.setVisibility(ProgressBar.INVISIBLE);
 			if (ex != null || error != null) {
 				errorDialogue(ex, error);
 			} else {
 				showURL(result);
 			}
 		}
 
 	}
 
 	/**
 	 * @param url
 	 */
 	protected void showURL(URL url) {
 		mImageURLTextView.setText(url != null ? url.toString() : "nothing!");
 
 		if (url != null) {
 			imageURL = url.toString();
 
 			mCopyButton.setEnabled(true);
 			mShareButton.setEnabled(true);
 		}
 	}
 
 	/**
 	 * @param ex
 	 */
 	protected void errorDialogue(Exception ex, Error error) {
 		this.ex = ex;
 		this.error = error;
 		final AlertDialog.Builder b = new AlertDialog.Builder(this);
 		b.setIcon(android.R.drawable.ic_dialog_alert);
 
 		switch (error) {
 		case HOST_NOT_FOUND:
 			b.setTitle(R.string.errorTitleHostNotFound);
 			b.setMessage(R.string.errorMessageHostNotFound);
 			break;
 		case NETWORK:
 			b.setTitle(R.string.errorTitleNetwork);
 			b.setMessage(R.string.errorMessageNetwork);
 			break;
 		case FILE_NOT_FOUND:
 			b.setTitle(R.string.errorTitleFileNotFound);
 			b.setMessage(R.string.errorMessageFileNotFound);
 			break;
 		case BAD_URL:
 			b.setTitle(R.string.errorTitleBadURL);
 			b.setMessage(R.string.errorMessageBadURL);
 			break;
 		default:
 			b.setTitle(R.string.errorTitle);
 			b.setMessage(R.string.errorMessage);
 			break;
 		}
 
 		if (ex != null) { // exception -> send error report
 			b.setPositiveButton(android.R.string.yes,
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface arg0, int arg1) {
 							sendError();
 						}
 					});
 			b.setNegativeButton(android.R.string.no, null);
 		} else { // just a normal error like network problems
 			// add a neutral button to the alert box and assign a click listener
 			b.setNeutralButton("Ok", null);
 		}
 		b.show();
 
 	}
 
 	/**
 	 * 
 	 */
 	private void sendError() {
 		StringWriter sw = new StringWriter();
 		ex.printStackTrace(new PrintWriter(sw));
 		String stacktrace = sw.toString();
 
 		// create an email intent to send to yourself
 		final Intent emailIntent = new Intent(
 				android.content.Intent.ACTION_SEND);
 		emailIntent.setType("plain/text");
 		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
 				new String[] { "android@qless.org" });
 		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
 				getString(R.string.app_name) + " "
 						+ getString(R.string.errorSubject));
 		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, stacktrace);
 
 		// start the email activity - note you need to start it
 		// with a chooser
 		startActivity(Intent.createChooser(emailIntent,
 				getString(R.string.errorSendAction)));
 
 	}
 }
