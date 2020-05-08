 /*
  * Copyright 2011 qorron
  * Contact: https://github.com/qorron
  * 
  * This file is part of 666up!
  * 
  * 666up! is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * 666up! is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with 666up!.  If not, see <http://www.gnu.org/licenses/>.
  */
 
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
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.provider.MediaStore.Images;
 import android.text.ClipboardManager;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * @author quattro
  * 
  *         This activity handles the send intent for images.
  * 
  */
 public class UploadActivity extends Activity {
 
	// gui parts
 	private ImageView mThumbnailView;
 	private TextView mGreeting;
 	private TextView mMimeTypeTextView;
 	private TextView mFilePathTextView;
 	private TextView mUploadDateTextView;
 	private TextView mImageURLTextView;
 	private ProgressBar mProgress;
 	private EditText mCommentEditText;
 	private Button mCopyButton;
 	private Button mShareButton;
 	private Button mConfirmButton;
 
 	// raw data
 	private String mImageURL;
 	private String mMimeType;
 	private String mFilePath;
 	private String mComment;
 	private String mUploadDate;
 	private byte[] mThumbnail;
 	private Long mUploadRowId;
 
 	// helpers, internals, ..
 	private UploadsDbAdapter mDbHelper;
 	private boolean mIntentOk;
 	private Intent mIntent;
 	private Exception ex;
 
 	public enum Error {
 		FILE_NOT_FOUND, HOST_NOT_FOUND, NETWORK, BAD_URL, BAD_INTENT
 	}
 
 	/**
 	 * Gets called when the activity is (re)created.
 	 * 
 	 * @see android.app.Activity#onCreate(android.os.Bundle)
 	 * 
 	 * @param savedInstanceState
 	 *            The previously saved instance data containing: url, file path, mime-type, row id
 	 *            and image comment
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// open database
 		mDbHelper = new UploadsDbAdapter(this);
 		mDbHelper.open();
 
 		mComment = ""; // initialize, so there is no null in the db
 
 		if (savedInstanceState != null) {
 			mImageURL = savedInstanceState.getString("mImageURL");
 			mFilePath = savedInstanceState.getString("mFilePath");
 			mMimeType = savedInstanceState.getString("mMimeType");
 			mUploadRowId = savedInstanceState.getLong("mUploadId");
 			mComment = savedInstanceState.getString("mComment");
 			mUploadDate = savedInstanceState.getString("mUploadDate");
 			mThumbnail = savedInstanceState.getByteArray("mThumbnail");
 
 		}
 		mIntent = getIntent();
 
 		if (mUploadRowId == null) {
 			// if there was no row ID in the saved instance state, look if there is one in the
 			// intent
 			Log.d("mUploadRowId", "no mUploadRowId, tring to get it from the intent");
 			Bundle extras = mIntent.getExtras();
			mUploadRowId = extras != null && extras.containsKey(UploadsDbAdapter.KEY_ROWID) ? extras
					.getLong(UploadsDbAdapter.KEY_ROWID) : null;
 		}
 
 		setContentView(R.layout.upload_edit);
 
 		mThumbnailView = (ImageView) findViewById(R.id.largeThumbnailView);
 		mGreeting = (TextView) findViewById(R.id.hello);
 		mMimeTypeTextView = (TextView) findViewById(R.id.mimeType);
 		mFilePathTextView = (TextView) findViewById(R.id.filePath);
 		mUploadDateTextView = (TextView) findViewById(R.id.uploadDate);
 		mImageURLTextView = (TextView) findViewById(R.id.imageURL);
 		mProgress = (ProgressBar) findViewById(R.id.progressBarUpload);
 		mCommentEditText = (EditText) findViewById(R.id.editComment);
 		mCopyButton = (Button) findViewById(R.id.buttonCopy);
 		mShareButton = (Button) findViewById(R.id.buttonShare);
 		mConfirmButton = (Button) findViewById(R.id.buttonSave);

 		mGreeting.setText(getString(R.string.uploadAt) + " " + getString(R.string.imageHoster));
 
 		guiEmpty(); // disable buttons for now

 		setupGuiElements(); // set onClickers and stuff
 
 		if (mUploadRowId != null
 				&& (mThumbnail == null || mFilePath == null || mImageURL == null || mComment == null)) {
 			// we have a row id, but at least one 'nut null' field is null, so go get it from the db
			Log.d("mUploadRowId", "mUploadRowId is here, fetching fields from the db: "
					+ mUploadRowId);
 			fetchFromDb();
 			guiDone();
 			populateFields();
 		} else if (mImageURL == null) {
 			// since there is no previously stored url, we have to upload the
 			// file
 			Log.d("mUploadRowId", "no mUploadRowId and no mImageURL -> uplaod");
 			decodeIntent();
 			if (mIntentOk) {
 				mMimeTypeTextView.setText(mMimeType);
 				mFilePathTextView.setText(mFilePath);
 				new ImageUploadTask().execute(mFilePath);
 			}
 			guiProcessing();
 			generateThumbnail();
 		} else {
 			// we already have a url, so we just update the gui and make it look
 			// like expected.
 			Log.d("mUploadRowId", "recovered from saved instance nothing to do");
 			populateFields();
 			guiDone();
 		}
 
 	}
 
 	/**
 	 * Saves what we have in the db. if the entry already exists, just the comments will be updated.
 	 * otherwise an entry (with thumbnail) will be generated and stored.
 	 */
 	private void saveState() {
 		if (mImageURL != null) {
 			if (null == mUploadRowId) {
 				// has never been saved to the db
 				Log.d("database", "stored new upload");
 				generateThumbnail(); // does nothing if the thumbnail already exists
 				mUploadRowId = mDbHelper.createUpload(mImageURL, mFilePath, mMimeType, mThumbnail,
 						mComment);
 			} else {
 				// has been saved, just update the comment
 				mDbHelper.updateCommentOnUpload(mUploadRowId, mComment);
 				Log.d("database", "update comment");
 			}
 		}
 	}
 
 	/**
 	 * creates or updates the entry in the database and saves the state information
 	 * 
 	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
 	 */
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		saveState();
 
 		outState.putString("mImageURL", mImageURL);
 		outState.putString("mMimeType", mMimeType);
 		outState.putString("mFilePath", mFilePath);
		if (mUploadRowId != null) {
			outState.putLong("mUploadId", mUploadRowId);
		}
 		outState.putString("mComment", mComment);
 		outState.putByteArray("mThumbnail", mThumbnail);
 		outState.putString("mUploadDate", mUploadDate);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onPause()
 	 */
 	@Override
 	protected void onPause() {
 		super.onPause();
 		saveState();
 		if (mDbHelper != null) {
 			mDbHelper.close();
 			mDbHelper = null;
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onResume()
 	 */
 	@Override
 	protected void onResume() {
 		super.onResume();
 		if (mDbHelper == null) {
 			mDbHelper = new UploadsDbAdapter(this);
 			mDbHelper.open();
 		}
 		// resetGUI();
 	}
 
 	/**
 	 * Displays the image URL and enables the copy/share buttons
 	 * 
 	 * @param url
 	 */
 	protected void showURL(URL url) {
 		mImageURLTextView.setText(url != null ? url.toString() : "nothing!");
 
 		if (url != null) {
 			mImageURL = url.toString();
 
 			mCopyButton.setEnabled(true);
 			mShareButton.setEnabled(true);
 
 		}
 	}
 
 	/**
 	 * Displays an error dialogue with the abilits to send a log on some errors
 	 * 
 	 * @param ex
 	 */
 	protected void errorDialogue(Exception ex, Error error) {
 		this.ex = ex;
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
 		case BAD_INTENT:
 			b.setTitle(R.string.errorTitleBadIntent);
 			b.setMessage(R.string.errorMessageBadIntent);
 			break;
 		default:
 			b.setTitle(R.string.errorTitle);
 			b.setMessage(R.string.errorMessage);
 			break;
 		}
 
 		if (ex != null) { // exception -> send error report
 			b.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
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
 	 * Constructs an ACTION_SEND intent to email a stacktrace
 	 * 
 	 */
 	private void sendError() {
 		StringWriter sw = new StringWriter();
 		ex.printStackTrace(new PrintWriter(sw));
 		String stacktrace = sw.toString();
 
 		// create an email intent to send to yourself
 		final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
 		emailIntent.setType("plain/text");
 		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
 				new String[] { "android@qless.org" });
 		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.app_name)
 				+ " " + getString(R.string.errorSubject));
 		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, stacktrace);
 
 		// start the email activity - note you need to start it
 		// with a chooser
 		startActivity(Intent.createChooser(emailIntent, getString(R.string.errorSendAction)));
 
 	}
 
 	/**
 	 * get the fields from the db.
 	 */
 	private void fetchFromDb() {
 		if (mUploadRowId != null) {
 			Cursor upload = mDbHelper.fetchUpload(mUploadRowId);
 			startManagingCursor(upload);
 			mComment = upload.getString(upload.getColumnIndexOrThrow(UploadsDbAdapter.KEY_COMMENT));
 			mImageURL = upload.getString(upload.getColumnIndexOrThrow(UploadsDbAdapter.KEY_URL));
 			mFilePath = upload.getString(upload
 					.getColumnIndexOrThrow(UploadsDbAdapter.KEY_FILENAME));
 			mMimeType = upload.getString(upload
 					.getColumnIndexOrThrow(UploadsDbAdapter.KEY_MIMETYPE));
 			mUploadDate = upload.getString(upload
 					.getColumnIndexOrThrow(UploadsDbAdapter.KEY_UPLOAD_DATE));
 
 			mThumbnail = upload.getBlob(upload.getColumnIndex(UploadsDbAdapter.KEY_THUMBNAIL));
 
 		}
 	}
 
 	/**
 	 * displays the values we have
 	 */
 	private void populateFields() {
 		mMimeTypeTextView.setText(mMimeType);
 		mFilePathTextView.setText(mFilePath);
 		mImageURLTextView.setText(mImageURL);
 		mCommentEditText.setText(mComment);
 		mUploadDateTextView.setText(mUploadDate);
 
 		if (mThumbnail != null) {
 			mThumbnailView.setImageBitmap(BitmapFactory.decodeByteArray(mThumbnail, 0,
 					mThumbnail.length));
 		} else {
 			mThumbnailView.setImageResource(android.R.drawable.ic_menu_gallery);
 		}
 
 	}
 
 	private void generateThumbnail() {
 		if (mFilePath != null && mThumbnail == null) {
 			try {
 				mThumbnail = ImageProcessor.thumbnail(mFilePath, 100);
 			} catch (Exception e) {
 				// no file, no thumbnail.
 			}
 		}
 	}
 
 	/**
 	 * bring GUI an an empty state. i.e. disable buttons as the serve no purpose unless the upload
 	 * is done or the data has been fetched from the db
 	 */
 	private void guiEmpty() {
 		mProgress.setVisibility(ProgressBar.INVISIBLE);
 		mCopyButton.setEnabled(false);
 		mShareButton.setEnabled(false);
 		mConfirmButton.setEnabled(false);
 		mGreeting.setVisibility(View.VISIBLE);
 	}
 
 	/**
 	 * bring GUI an an processing state. i.e. disable buttons and show the progress indicator
 	 * 
 	 */
 	private void guiProcessing() {
 		mProgress.setVisibility(ProgressBar.VISIBLE);
 		mCopyButton.setEnabled(false);
 		mShareButton.setEnabled(false);
 		mConfirmButton.setEnabled(false);
 		mGreeting.setVisibility(View.VISIBLE);
 	}
 
 	/**
 	 * bring GUI an an done state. i.e. enable buttons and hide the progress indicator
 	 * 
 	 */
 	private void guiDone() {
 		mProgress.setVisibility(ProgressBar.INVISIBLE);
 		mCopyButton.setEnabled(true);
 		mShareButton.setEnabled(true);
 		mConfirmButton.setEnabled(true);
 		mGreeting.setVisibility(View.GONE);
 	}
 
 	/**
 	 * sets up onClicks for buttons. must be called once on activity creation
 	 */
 	private void setupGuiElements() {
		if (mCommentEditText == null) {
 			Log.d("NULL", "mCommentEditText == null @setupGuiElements(");
 		}
 		mCommentEditText.addTextChangedListener(new TextWatcher() {
 
 			public void afterTextChanged(Editable s) {
 				mComment = s.toString();
 			}
 
 			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
 				// nothing to do
 			}
 
 			public void onTextChanged(CharSequence s, int start, int before, int count) {
 				// nothing to do
 			}
 		});
 
 		// Button to copy the url to the clipboard
 		mCopyButton.setOnClickListener(new Button.OnClickListener() {
 			public void onClick(View v) {
 				ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
 				clipboard.setText(mImageURL);
 				Context context = getApplicationContext();
 				CharSequence text = getString(R.string.copyToast);
 				int duration = Toast.LENGTH_SHORT;
 				Toast toast = Toast.makeText(context, text, duration);
 				toast.show();
 			}
 		});
 
 		// Button to share the image url
 		mShareButton.setOnClickListener(new Button.OnClickListener() {
 			public void onClick(View v) {
 				Intent i = new Intent(android.content.Intent.ACTION_SEND);
 				i.setType("text/plain");
 				i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
 				i.putExtra(Intent.EXTRA_TEXT, mImageURL);
 				startActivity(Intent.createChooser(i, getString(R.string.share_title)));
 
 			}
 
 		});
 		mConfirmButton.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View view) {
 				setResult(RESULT_OK);
 				finish();
 			}
 
 		});
 
 	}
 
 	/**
 	 * for upload intents: handles intent parsing and sets member variables: mMimeType, mFilePath
 	 * accordingly
 	 */
 	private void decodeIntent() {
 		if (Intent.ACTION_SEND.equals(mIntent.getAction())) {
 			Bundle extras = mIntent.getExtras();
 			if (extras.containsKey(Intent.EXTRA_STREAM)) {
 				Uri uri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
 				String scheme = uri.getScheme();
 				mMimeType = null;
 				mFilePath = null;
 				if (scheme.equals("content")) {
 					mMimeType = mIntent.getType();
 					ContentResolver contentResolver = getContentResolver();
 					Cursor cursor = contentResolver.query(uri, null, null, null, null);
 					cursor.moveToFirst();
 					mFilePath = cursor.getString(cursor.getColumnIndexOrThrow(Images.Media.DATA));
 					mIntentOk = true;
 				} else if (scheme.equals("file")) {
 					mMimeType = mIntent.getType();
 					mFilePath = uri.getPath();
 					mIntentOk = true;
 				} else {
 					Log.d("BAD_INTENT", "no content scheme, is: " + scheme);
 					errorDialogue(null, Error.BAD_INTENT);
 				}
 			} else {
 				Log.d("BAD_INTENT", "no EXTRA_STREAM");
 				errorDialogue(null, Error.BAD_INTENT);
 			}
 		} else {
 			Log.d("BAD_INTENT", "no ACTION_SEND");
 			errorDialogue(null, Error.BAD_INTENT);
 		}
 
 	}
 
 	/**
 	 * handles the resize and upload process in a background thread
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
 				mImageURLTextView.setText(result != null ? result.toString() : "nothing!");
 
 				if (result != null) {
 					mImageURL = result.toString();
 					populateFields();
 					guiDone();
 				}
 			}
 		}
 	}
 }
