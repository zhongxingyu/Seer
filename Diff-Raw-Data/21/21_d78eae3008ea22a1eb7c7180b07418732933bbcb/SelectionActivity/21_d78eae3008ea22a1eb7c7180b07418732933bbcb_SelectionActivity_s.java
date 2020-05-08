 package mmb.foss.aueb.icong;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ImageView;
 import android.widget.ListView;
 
 /**
  * This activity will introduce the boxes to the user.
  * 
  * @author periklis
  * 
  */
 public class SelectionActivity extends Activity {
 
 	private ImageView imagefileBox, cameraBox, screenBox, chanSplitBox, mixBox,
 			chanMergeBox, forkBox, valueEntryBox, sliderBox, blurBox, edgesBox,
 			invertBox, clipBox, rgb2hsvBox, hsv2rgbBox;
 	// fixed size of entries, no need for ArrayList or other dynamic data set
 	private ImageView[] listImages = new ImageView[15];
 	private String[] imageNames = { "ImageFile", "Camera", "Screen",
 			"Channel Split", "Mix", "Channel Merge", "Fork", "Value Entry",
 			"Slider", "Blur", "Edges", "Invert", "Clip", "RGB to HSV",
 			"HSV to RGB" };
 	private String[] imageDescriptions = { "Gets/sets input/output to file",
 			"Gets input from the Camera", "Outputs the result to the screen",
 			"Splits the color channel", "Mixes", "Merges the color channel",
 			"Forks", "Inputs usr defined values", "Inputs user defined values",
 			"Blurs", "Edges", "Inverts the image", "Clips", "", "" };
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.selection_layout);
 		imagefileBox = new ImageView(getBaseContext());
 		imagefileBox.setImageResource(R.drawable.image_file);
 		listImages[0] = imagefileBox;
 		cameraBox = new ImageView(getBaseContext());
 		cameraBox.setImageResource(R.drawable.camera);
 		listImages[1] = cameraBox;
 		screenBox = new ImageView(getBaseContext());
 		screenBox.setImageResource(R.drawable.screen);
 		listImages[2] = screenBox;
 		chanSplitBox = new ImageView(getBaseContext());
 		chanSplitBox.setImageResource(R.drawable.chan_split);
 		listImages[3] = chanSplitBox;
 		mixBox = new ImageView(getBaseContext());
 		mixBox.setImageResource(R.drawable.mix);
 		listImages[4] = mixBox;
 		chanMergeBox = new ImageView(getBaseContext());
 		chanMergeBox.setImageResource(R.drawable.chan_merge);
 		listImages[5] = chanMergeBox;
 		forkBox = new ImageView(getBaseContext());
 		forkBox.setImageResource(R.drawable.fork);
 		listImages[6] = forkBox;
 		valueEntryBox = new ImageView(getBaseContext());
		valueEntryBox.setImageResource(R.drawable.value_entry);
 		listImages[7] = valueEntryBox;
 		sliderBox = new ImageView(getBaseContext());
 		sliderBox.setImageResource(R.drawable.slider);
 		listImages[8] = sliderBox;
 		blurBox = new ImageView(getBaseContext());
 		blurBox.setImageResource(R.drawable.blur);
 		listImages[9] = blurBox;
 		edgesBox = new ImageView(getBaseContext());
 		edgesBox.setImageResource(R.drawable.edges);
 		listImages[10] = edgesBox;
 		invertBox = new ImageView(getBaseContext());
 		invertBox.setImageResource(R.drawable.invert);
 		listImages[11] = invertBox;
 		clipBox = new ImageView(getBaseContext());
 		clipBox.setImageResource(R.drawable.clip);
 		listImages[12] = clipBox;
 		rgb2hsvBox = new ImageView(getBaseContext());
 		rgb2hsvBox.setImageResource(R.drawable.rgb2hsv);
 		listImages[13] = rgb2hsvBox;
 		hsv2rgbBox = new ImageView(getBaseContext());
 		hsv2rgbBox.setImageResource(R.drawable.hsv2rgb);
 		listImages[14] = hsv2rgbBox;
 
 		ListView listView = (ListView) findViewById(R.id.selectionlist);
 		SelectionAdapter adapter = new SelectionAdapter(this, listImages,
 				imageNames, imageDescriptions);
 		listView.setAdapter(adapter);
 
 		listView.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				// TODO Auto-generated method stub
 				Intent selectedBox = new Intent();
 				Log.e("yaw", "" + Uri.parse("" + arg2));
 				selectedBox.setData(Uri.parse("" + arg2));
 				setResult(RESULT_OK, selectedBox);
 				finish();
 			}
 
 		});
 	}
 
 }
