 package mmb.foss.aueb.icong;
 
 //fml 
 import mmb.foss.aueb.icong.boxes.BlurBox;
 import mmb.foss.aueb.icong.boxes.Box;
 import mmb.foss.aueb.icong.boxes.BoxTypes;
 import mmb.foss.aueb.icong.boxes.CameraBox;
 import mmb.foss.aueb.icong.boxes.ChanMergeBox;
 import mmb.foss.aueb.icong.boxes.ChanSplitBox;
 import mmb.foss.aueb.icong.boxes.ClipBox;
 import mmb.foss.aueb.icong.boxes.EdgesBox;
 import mmb.foss.aueb.icong.boxes.ForkBox;
 import mmb.foss.aueb.icong.boxes.HSV2RGBBox;
 import mmb.foss.aueb.icong.boxes.ImageFileBox;
 import mmb.foss.aueb.icong.boxes.InvertBox;
 import mmb.foss.aueb.icong.boxes.MixBox;
 import mmb.foss.aueb.icong.boxes.RGB2HSVBox;
 import mmb.foss.aueb.icong.boxes.SavedState;
 import mmb.foss.aueb.icong.boxes.ScreenBox;
 import mmb.foss.aueb.icong.boxes.SliderBox;
 import mmb.foss.aueb.icong.boxes.ValueEntryBox;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.Button;
 
 public class MainActivity extends Activity {
 	Button btnGo;
 	DrawableAreaView canvas;
 	BoxTypes[] boxes = BoxTypes.values();
 	static int width, height;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		setContentView(R.layout.activity_main);
 		DisplayMetrics dm = new DisplayMetrics();
 		getWindowManager().getDefaultDisplay().getMetrics(dm);
 		width = dm.widthPixels;
 		height = dm.heightPixels;
 		canvas = (DrawableAreaView) findViewById(R.id.canvas);
 		btnGo = (Button) findViewById(R.id.run_button);
 		btnGo.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				Object output = null;
 				for (int y = 0; y < SavedState.getLines().size(); y++) {
 					for (int i = 0; i < SavedState.getLines().size(); i++) {
 						BoxButtonPair[] line = SavedState.getLine(i);
 						if (line[0].getBox().getNoOfOutpus() > 0) {
 							if (line[1].getBox().getNoOfInputs() > 0) {
 								int buttonOutput = line[0].getButton()
 										- line[0].getBox().getNoOfInputs() + 1;
 								int buttonInput = line[1].getButton() + 1;
 								switch (buttonOutput) {
 								// TODO output =
 								// line[0].getBox().getOutput(buttonOutput-1);
 								case 1:
 									output = line[0].getBox().getOutput(0);
 									break;
 								case 2:
 									output = line[0].getBox().getOutput(1);
 									break;
 								case 3:
 									output = line[0].getBox().getOutput(2);
 									break;
 								}
 								switch (buttonInput) {
 								// TODO
 								// line[1].getBox().setInput(output,buttonInput-1);
 								case 1:
 									line[1].getBox().setInput(output, 0);
 									break;
 								case 2:
 									line[1].getBox().setInput(output, 1);
 									break;
 								case 3:
 									line[1].getBox().setInput(output, 2);
 									break;
 								}
 
 								line[0].getBox().function();
 								line[0].setBox(line[0].getBox());
 								line[1].getBox().function();
 								line[1].setBox(line[1].getBox());
 
 								// TODO remove current line,and add another,with
 								// different settings
 
 							}
 						}
 					}
 				}
 
 			}
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.clear_screen:
 			SavedState.clearBoxes();
 			SavedState.clearLines();
			canvas.hideTrash();
 			canvas.invalidate();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	public void onAddButtonClick(View view) {
 		Intent intent = new Intent(this, SelectionActivity.class);
 		startActivityForResult(intent, 0);
 	}
 
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (resultCode != 0) {
 			int boxIndex = Integer.parseInt(data.getData().toString());
 			canvas.addBox(getBox(boxIndex));
 		}
 	}
 
 	private Box getBox(int index) {
 		Box box = null;
 		Context ctx = getBaseContext();
 		switch (boxes[index]) {
 		case ImageFile:
 			box = new ImageFileBox(ctx);
 			break;
 		case Camera:
 			box = new CameraBox(ctx);
 			break;
 		case Screen:
 			box = new ScreenBox(ctx);
 			break;
 		case ChanMerge:
 			box = new ChanMergeBox(ctx);
 			break;
 		case ChanSplit:
 			box = new ChanSplitBox(ctx);
 			break;
 		case Clip:
 			box = new ClipBox(ctx);
 			break;
 		case Edges:
 			box = new EdgesBox(ctx);
 			break;
 		case Fork:
 			box = new ForkBox(ctx);
 			break;
 		case HSV2RGB:
 			box = new HSV2RGBBox(ctx);
 			break;
 		case Invert:
 			box = new InvertBox(ctx);
 			break;
 		case Mix:
 			box = new MixBox(ctx);
 			break;
 		case RGB2HSV:
 			box = new RGB2HSVBox(ctx);
 			break;
 		case Slider:
 			box = new SliderBox(ctx);
 			break;
 		case ValueEntry:
 			box = new ValueEntryBox(ctx);
 			break;
 		case Blur:
 			box = new BlurBox(ctx);
 			break;
 		default:
 			Log.v(">MainActivity", "Something wrong in getBox");
 			break;
 		}
 		return box;
 	}
 
 }
