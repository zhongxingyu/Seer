 package bu.edu.cs673.edukid.settings.category;
 
 import java.io.IOException;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.media.MediaRecorder;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.Toast;
 import bu.edu.cs673.edukid.EDUkid;
 import bu.edu.cs673.edukid.R;
 import bu.edu.cs673.edukid.db.Database;
 import bu.edu.cs673.edukid.db.ImageUtils;
 import bu.edu.cs673.edukid.db.model.Word;
 import bu.edu.cs673.edukid.db.model.category.CategoryType;
 
 public class AddWordView extends Activity implements OnClickListener {
 
 	private CategoryType categoryType;
 
 	private int itemIndex;
 
 	private static final int TAKE_PICTURE = 1888;
 
 	private boolean mStartReco = true;
 
 	private String word = "";
 
 	private ImageView wordImage;
 
 	private ImageView micImage;
 
 	private Boolean picture = false;
 
 	private Database database = Database.getInstance(this);
 
 	public MediaRecorder recorder = new MediaRecorder();
 	
 	private String mFileName = "";
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.add_word);
 		wordImage = (ImageView) findViewById(R.id.imageView1);
 		micImage = (ImageView) findViewById(R.id.imageButton2);
 
 		Intent intent = getIntent();
 		Bundle extras = intent.getExtras();
 
 		categoryType = (CategoryType) extras
 				.getSerializable(EDUkid.CATEGORY_TYPE);
 		itemIndex = extras.getInt(EDUkid.ITEM_INDEX);
 
 		Button createSaveButton = (Button) findViewById(R.id.button1);
 		createSaveButton.setOnClickListener(this);
 		ImageButton createUploadPhotoButton = (ImageButton) findViewById(R.id.imageButton1);
 		createUploadPhotoButton.setOnClickListener(this);
 		micImage.setOnClickListener(this);
 		// TextView balh = (TextView) findViewById(R.id.textView233);
 	}
 
 	// TODO: just temporary for now. Need to get input from the user and use
 	// that instead of hardcoding.
 	/*
 	 * public void onAddWordClick(View view) { // TODO: do this based on user
 	 * entry // TODO: hard coding these for now to add a new word. Word word =
 	 * new Word(); word.setWord("TestWord"); word.setWordSound("Coming soon");
 	 * word.setWordImage(ImageUtils.drawableToByteArray(getResources()
 	 * .getDrawable(R.drawable.boy)));
 	 * 
 	 * categoryType.addItemWord(itemIndex, word);
 	 * 
 	 * // TODO: refresh list }
 	 */
 
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == TAKE_PICTURE && resultCode == RESULT_OK) {
 			Bitmap photo = (Bitmap) data.getExtras().get("data");
 			picture = true;
 			if (photo != null) {
 
 				wordImage.setImageBitmap(photo);
				wordImage.setMaxHeight(400);
				wordImage.setMaxWidth(400);
				wordImage.setAdjustViewBounds(false);
 			}
 		}
 	}
 
 	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 		switch (v.getId()) {
 		case R.id.button1:
 			word = ((EditText) findViewById(R.id.editText1)).getText()
 					.toString();
 			// long result = DATABASE_ERROR;
 			Word w = new Word();
 			if(word.equalsIgnoreCase("") || picture == false){
 				Toast.makeText(this, "cannot save Word!",
 						Toast.LENGTH_LONG).show();
 			}
 			else{
 			w.setWord(word);
 			if (mFileName.equalsIgnoreCase(""))
 				w.setWordSound(word);
 			else
 				w.setWordSound(mFileName);
 
 			w.setWordImage(ImageUtils.drawableToByteArray(wordImage
 					.getDrawable()));
 			categoryType.addItemWord(itemIndex, w);
 
 			Toast.makeText(this, "Word saved successfully!", Toast.LENGTH_LONG)
 					.show();
 			}
 			break;
 
 		case R.id.imageButton1:
 			startCamera();
 			break;
 
 		case R.id.imageButton2:
 			onRecord(mStartReco);
 			mStartReco = !mStartReco;
 			break;
 		}
 	}
 
 	private void onRecord(boolean start) {
 		// TODO Auto-generated method stub
 		if (start) {
 			startRecording();
 
 		} else {
 			stopRecording();
 		}
 
 	}
 
 	private void stopRecording() {
 		// TODO Auto-generated method stub
 		micImage.setBackgroundResource(R.drawable.mikebutton);
 		recorder.stop();
 		recorder.release();
 		recorder = null;
 	}
 
 	private void startRecording() {
 		// TODO Auto-generated method stub
 		micImage.setBackgroundResource(R.drawable.recordmikebutton);
 		recorder = new MediaRecorder();
 		mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
 		mFileName += "/audiorecordtest.3gp";
 		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
 		recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
 		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
 		recorder.setOutputFile(mFileName);
 		try {
 			recorder.prepare();
 		} catch (IllegalStateException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		recorder.start();
 	}
 
 	private void startCamera() {
 		// TODO Auto-generated method stub
 		picture = true;
 		Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
 		startActivityForResult(intent, TAKE_PICTURE);
 	}
 }
