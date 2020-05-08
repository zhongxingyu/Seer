 package com.cmu.journalmap.activities;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.cmu.journalmap.models.Place;
 import com.cmu.journalmap.models.TextRecord;
 import com.cmu.journalmap.storage.Places;
 import com.cmu.journalmap.utilities.AudioUtility;
 import com.cmu.journalmap.utilities.NfcUtility;
 import com.cmu.journalmap.utilities.PictureUtility;
 import com.cmu.journalmap.utilities.PropertiesUtility;
 import com.google.android.maps.GeoPoint;
 
 import android.app.Activity;
 import android.content.Intent;
 
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.media.MediaPlayer;
 import android.media.MediaRecorder;
 import android.net.Uri;
 import android.nfc.NdefMessage;
 import android.nfc.NdefRecord;
 import android.nfc.NfcAdapter;
 import android.os.Bundle;
 
 import android.os.Parcelable;
 
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 
 public class SavePlace extends Activity {
 	private final static String TAG = "SavePlace";
 	private Button savePlace = null;
 	private EditText commentBlock = null;
 
 	private Button recordAudio = null;
 	private Button playAudio = null;
 	private String audioLoc = "";
 
 	private Button recordVideo = null;
 	private String videoLoc = "";
 	private Uri imageUri = null;
 	private Place newPlace = null;
 	private ImageView placePic = null;
 
 	private MediaRecorder mRecorder = null;
 	private MediaPlayer mPlayer = null;
 
 	private int origin = -1;
 
 	private int[] currentCoordinates = { Integer.MAX_VALUE, Integer.MAX_VALUE };
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_addplace);
 
 		connectViewElements();
 
 		// Get intent, action and MIME type
 		Intent intent = getIntent();
 		String action = intent.getAction();
 		String type = intent.getType();
 
 		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
 			origin = 0; // NFC
 			getNfcTagInfo(intent);
 
 		} else {
 			if (Intent.ACTION_SEND.equals(action) && type != null) {
 				origin = 1; // Photo
 				getPhotoInfo(type, intent);
 			}
 			// String photoPath = getIntent().getStringExtra("placePhotoPath");
 			if (placePic != null) {
 				Bitmap ThumbImage = PictureUtility.decodeSampledBitmapFromPath(
 						getRealPathFromURI(imageUri), 400, 400);
 				placePic.setImageBitmap(ThumbImage);
 			}
 		}
 
 		savePlace.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				switch (origin) {
 				case 0: // nfc
 					newPlace.setAudioLocation(audioLoc);
 					newPlace.setNote(commentBlock.getText().toString());
 					newPlace.setVideoLocation(videoLoc);
 
 					PropertiesUtility.writePlaceToFile(v.getContext(), newPlace);
 					Intent intent = new Intent(v.getContext(),
 							ActivityMap.class);
 					Places.getItems().add(newPlace);
 					startActivity(intent);
 					finish();
 					break;
 				case 1: // photo
 					String picPath = getRealPathFromURI(imageUri);
 					int[] tempCoords = PictureUtility
 							.isCoordinatesValid(PictureUtility
 									.getCoordsFromPhoto(picPath)) ? PictureUtility
 							.getCoordsFromPhoto(picPath) : currentCoordinates;
 
 					if (PictureUtility.isCoordinatesValid(tempCoords)) {
 						newPlace = new Place(new GeoPoint(tempCoords[0],
 								tempCoords[1]), "", "");
 						newPlace.setAudioLocation(audioLoc);
 						newPlace.setNote(commentBlock.getText().toString());
 						newPlace.setVideoLocation(videoLoc);
 						newPlace.setPhotoLocation(getRealPathFromURI(imageUri));
 						Log.e(TAG, newPlace.toString());
 						PropertiesUtility.writePlaceToFile(v.getContext(),
 								newPlace);
 						Intent intent3 = new Intent(v.getContext(),
 								ActivityMap.class);
 						Places.getItems().add(newPlace);
 						startActivity(intent3);
 						finish();
 					} else {
 						Intent intent2 = new Intent(v.getContext(),
 								MainActivity.class);
 						startActivityForResult(intent2, 1);
 					}
 					break;
 
 				default:
 					System.err.println("Unknown source type");
 					break;
 
 				}
 			}
 		});
 
 		recordAudio.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				Button recBt = (Button) v;
 				if (recBt.getText().equals(
 						v.getResources().getString(R.string.rec_start_button))) {
 					recBt.setText(v.getResources().getString(
 							R.string.rec_stop_button));
 					mRecorder = AudioUtility.getMediaRecorder();
 					audioLoc = AudioUtility.startRecording(mRecorder);
 				} else {
 					recBt.setText(v.getResources().getString(
 							R.string.rec_start_button));
 					AudioUtility.stopRecording(mRecorder);
 				}
 			}
 		});
 
 		playAudio.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				Button playBt = (Button) v;
 				if (playBt.getText().equals(
 						v.getResources().getString(R.string.rec_play_button))) {
 					playBt.setText(v.getResources().getString(
 							R.string.rec_stop_button));
 					mPlayer = AudioUtility.startPlaying(audioLoc);
 				} else {
 					playBt.setText(v.getResources().getString(
 							R.string.rec_play_button));
 					AudioUtility.stopPlaying(mPlayer);
 				}
 
 			}
 		});
 
 		recordVideo.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 
 			}
 		});
 
 	}
 
 	void handleSendImage(Intent intent) {
 		imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
 		if (imageUri != null) {
 			// Update UI to reflect image being shared
 			Log.i("Main", "Image is being shared");
 
 			// check for EXIF data
 			try {
 				String picPath = getRealPathFromURI(imageUri);
 				System.out.printf("Path is:", picPath);
 				int[] tempCoords = PictureUtility.getCoordsFromPhoto(picPath);
 				if ((tempCoords[0] == 0) || (tempCoords[1] == 0)) {
 					// EXIF Coords were NOT found
 					Log.i("PictureUtlity", "EXIF Coords were NOT found");
 
 				} else {
 					// EXIF Coords were found
 					Log.i("PictureUtlity", "EXIF Coords were found!");
 				}
 			} catch (Exception e) {
 				Log.i("IMAGE Getter", "Could not get image");
 			}
 		}
 	}
 
 	// Source:
 	// http://stackoverflow.com/questions/3401579/get-filename-and-path-from-uri-from-mediastore
 	public String getRealPathFromURI(Uri contentUri) {
 		String[] proj = { MediaStore.Images.Media.DATA };
 		Cursor cursor = managedQuery(contentUri, proj, null, null, null);
 		int column_index = cursor
 				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
 		cursor.moveToFirst();
 		return cursor.getString(column_index);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == 1) {
 			if (resultCode == RESULT_OK) {
 
 				this.currentCoordinates[0] = data.getIntExtra("lag",
 						Integer.MAX_VALUE);
 				this.currentCoordinates[1] = data.getIntExtra("lon",
 						Integer.MAX_VALUE);
 			}
 			if (resultCode == RESULT_CANCELED) {
 				return;
 			}
 		}
 	}
 
 	public void connectViewElements() {
 		savePlace = (Button) findViewById(R.id.bSaveButton);
 		commentBlock = (EditText) findViewById(R.id.etComments);
 		recordAudio = (Button) findViewById(R.id.bRecButton);
 		playAudio = (Button) findViewById(R.id.bPlayButton);
 		recordVideo = (Button) findViewById(R.id.bVideoButton);
 		placePic = (ImageView) findViewById(R.id.ivPlacePic);
 	}
 
 	public void getPhotoInfo(String thisType, Intent thisIntent) {
 		if (thisType.startsWith("image/")) {
 			handleSendImage(thisIntent); // Handle single image being sent
 		}
 	}
 
 	public void getNfcTagInfo(Intent thisIntent) {
 		Parcelable[] rawMsgs = thisIntent
 				.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
 		if (rawMsgs != null) {
 			NdefMessage[] msgs;
 			NdefRecord[] recs = null;
 			msgs = new NdefMessage[rawMsgs.length];
 			// recs = new NdefRecord[rawMsgs.length];
 			for (int i = 0; i < rawMsgs.length; i++) {
 				msgs[i] = (NdefMessage) rawMsgs[i];
 				recs = msgs[i].getRecords();
 			}
 
 			List<TextRecord> records = new ArrayList<TextRecord>();
 			records.add(TextRecord.parse(recs[0]));
 			final int size = records.size();
 			for (int i = 0; i < size; i++) {
 				TextRecord record = records.get(i);
 				String nfctag = record.getText();
 				System.out.println(nfctag);
 				// content.addView(record.getView(this, inflater, content, i));
 				// inflater.inflate(R.layout.tag_divider, content, true);
 				double[] tempCoords = NfcUtility.tagToCoords(nfctag);
 				newPlace = new Place(new GeoPoint(
 						(int) tempCoords[0] * 1000000,
 						(int) tempCoords[1] * 1000000), "", "");
 			}
 		}
 	}
 
 	public void onResume() {
 		super.onResume();
 		//
 		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
 			Intent intent = getIntent();
 			Parcelable[] rawMsgs = intent
 					.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
 			if (rawMsgs != null) {
 				NdefMessage[] msgs;
 				msgs = new NdefMessage[rawMsgs.length];
 				for (int i = 0; i < rawMsgs.length; i++) {
 					msgs[i] = (NdefMessage) rawMsgs[i];
 				}
 			}
 		}
 		// process the msgs array
 	}
 }
