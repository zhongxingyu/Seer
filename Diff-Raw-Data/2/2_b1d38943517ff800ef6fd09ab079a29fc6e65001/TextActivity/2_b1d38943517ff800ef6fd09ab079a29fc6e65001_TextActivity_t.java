 package com.example.ad_creation;
 
 import java.io.ByteArrayOutputStream;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Matrix;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.TextView.BufferType;
 
 import com.commonsware.cwac.colormixer.ColorMixer;
 import com.commonsware.cwac.colormixer.ColorMixerDialog;
 import com.example.custom_views.ProgressView;
 import com.example.maple_android.AdCreationManager;
 import com.example.maple_android.MapleApplication;
 import com.example.maple_android.R;
 import com.example.maple_android.Utility;
 import com.larvalabs.svgandroid.SVG;
 import com.larvalabs.svgandroid.SVGParser;
 
 public class TextActivity extends FunnelActivity implements
 		FontPickerDialog.FontPickerDialogListener {
 	private MapleApplication mApp;
 	private AdCreationManager mAdCreationManager;
 
 	private ImageView mAdView;
 
 	// text option
 	private boolean mShowOptions;
 	private float mTextXPos;
 	private float mTextYPos;
 	private EditText mTextEntryField;
 	private TextView mPhotoText;
 	private int mTextColor;
 	private ProgressView mProgressBar;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_text);
 
 		// Init app
 		mApp = (MapleApplication) this.getApplication();
 		mAdCreationManager = mApp.getAdCreationManager();
 
 		ImageButton help = (ImageButton) findViewById(R.id.helpButton);
 		SVG svg = SVGParser.getSVGFromResource(getResources(), R.raw.question);
 		help.setImageDrawable(svg.createPictureDrawable());
 		help.setBackgroundColor(Color.BLACK);
 
 		mProgressBar = (ProgressView) findViewById(R.id.progressBar);
 
 		// set photo
		mAdView = (ImageView) this.findViewById(R.id.ad);
 		// initialize photo for clicking
 		mAdView.setOnTouchListener(new View.OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				placeText(v, event);
 				/*
 				 * This callback function requires us to return a boolean.
 				 * Return true just to appease it.
 				 */
 				return true;
 			}
 		});
 		
 		mAdCreationManager.setup(this);
 
 		// start off not showing edit options
 		mShowOptions = false;
 
 		// get TextView to overlap on photo
 		mPhotoText = (TextView) findViewById(R.id.photoText);
 		updateTextSize(); // initialize size to default
 
 		// set up text color
 		mTextColor = mPhotoText.getTextColors().getDefaultColor();
 		((TextView) findViewById(R.id.changeColor)).setTextColor(mTextColor);
 
 		// set up font size listener
 		((EditText) findViewById(R.id.fontSize))
 				.addTextChangedListener(new TextWatcher() {
 					@Override
 					public void onTextChanged(CharSequence s, int start,
 							int before, int count) {
 						updateTextSize();
 
 					}
 
 					@Override
 					public void beforeTextChanged(CharSequence s, int start,
 							int count, int after) {
 					}
 
 					@Override
 					public void afterTextChanged(Editable s) {
 					}
 				});
 
 		// set up text listener
 		mTextEntryField = (EditText) this.findViewById(R.id.textEntry);
 		mTextEntryField.addTextChangedListener(new TextWatcher() {
 
 			@Override
 			public void afterTextChanged(Editable e) {
 				mPhotoText.setText(e.toString(), BufferType.SPANNABLE);
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence arg0, int arg1,
 					int arg2, int arg3) {
 			}
 
 			@Override
 			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
 					int arg3) {
 			}
 
 		});
 
 	}
 
 	/**
 	 * Grabs the user entered value from the font size entry text box and
 	 * updates the text size with it. Changes are instant, and error checking is
 	 * done for null values. The XML declaration specifies that only digits can
 	 * be entered.
 	 */
 	private void updateTextSize() {
 		// grab entry as string
 		String textSize = ((EditText) findViewById(R.id.fontSize)).getText()
 				.toString();
 
 		// try to convert to int
 		Integer size = null;
 		try {
 			size = Integer.valueOf(textSize);
 		} catch (NumberFormatException e) {
 		}
 
 		// update size if possible
 		if (size != null)
 			mPhotoText.setTextSize((float) size);
 	}
 
 	/**
 	 * Called when a click is registered on the picture. This function saves
 	 * that click location for future reference, and moves the currently written
 	 * text to the new location.
 	 * 
 	 * @param v
 	 *            The view that was clicked on
 	 * @param event
 	 *            The mouse click
 	 */
 	private void placeText(View v, MotionEvent event) {
 		// update options after click
 		if (!mShowOptions)
 			toggleOptions();
 
 		// save click location
 		mTextXPos = event.getX();
 		mTextYPos = event.getY();
 
 		// place textView
 		mPhotoText.setX(mTextXPos + v.getX());
 		mPhotoText.setY(mTextYPos + v.getY() - mPhotoText.getBaseline());
 
 		// set focus to text edit
 		mTextEntryField.setFocusable(true);
 		mTextEntryField.requestFocus();
 	}
 
 	/**
 	 * This function toggles the visibility of the text editing options. They
 	 * are initializing set to be hidden until the user clicks on the picture.
 	 * Then they are shown, and the initial instruction are hidden. Calling this
 	 * again will reverse the process. The visibility status is stored in
 	 * mShowOptions.
 	 */
 	private void toggleOptions() {
 		// switch setting
 		mShowOptions = !mShowOptions;
 
 		// get int value for visibility setting
 		int visibility;
 		if (mShowOptions) {
 			visibility = View.VISIBLE;
 			findViewById(R.id.textInstructions).setVisibility(View.GONE);
 		} else {
 			visibility = View.INVISIBLE;
 			findViewById(R.id.textInstructions).setVisibility(View.VISIBLE);
 		}
 
 		// update View visibilities
 		findViewById(R.id.changeColor).setVisibility(visibility);
 		findViewById(R.id.changeFont).setVisibility(visibility);
 		findViewById(R.id.fontSize).setVisibility(visibility);
 		findViewById(R.id.fontSizeLabel).setVisibility(visibility);
 		mTextEntryField.setVisibility(visibility);
 		mPhotoText.setVisibility(visibility);
 	}
 
 	/**
 	 * start a dialog that shows all availabe fonts and / allows the user to
 	 * pick which one they want to use
 	 * 
 	 * @param view
 	 *            The view that was clicked on
 	 */
 	public void changeFont(View view) {
 		FontPickerDialog dlg = new FontPickerDialog();
 		dlg.show(getFragmentManager(), "font_picker");
 	}
 
 	/**
 	 * Call back method for the font picker dialog This is called when a font is
 	 * selected.
 	 * 
 	 * @param dialog
 	 *            The dialog instance. Contains the selected font.
 	 */
 	@Override
 	public void onFontSelected(FontPickerDialog dialog) {
 		// get font file path and typeface style from the dialog
 		String fontPath = dialog.getSelectedFont();
 		Typeface tface = Typeface.createFromFile(fontPath);
 
 		// update text on ad
 		mPhotoText.setTypeface(tface);
 
 		// update font button to show what font we're using
 		Button fontButton = (Button) findViewById(R.id.changeFont);
 		fontButton.setTypeface(tface);
 
 	}
 
 	/**
 	 * Called when the change color button is clicked. This launches a color
 	 * picker dialog that allows the user to change which color is being used.
 	 * 
 	 * @param view
 	 *            The button that was clicked on
 	 */
 	public void changeColor(View view) {
 
 		// Create a new color dialog with a listener
 		// that updates the current color after the user
 		// has made a selection. This does nothing on cancel.
 		new ColorMixerDialog(this, mTextColor,
 				new ColorMixer.OnColorChangedListener() {
 
 					// called when the user selects a new color
 					@Override
 					public void onColorChange(int color) {
 						// update the stored color
 						mTextColor = color;
 						// change the text color
 						mPhotoText.setTextColor(color);
 						// update the color of the button's text
 						Button b = (Button) findViewById(R.id.changeColor);
 						b.setTextColor(color);
 
 					}
 
 				}).show();
 	}
 
 	/**
 	 * This function is used to generate a bitmap from the TextView that holds
 	 * the currently created text
 	 * 
 	 * @param v
 	 *            The TextView containing the text we want to write
 	 * @return The text converted to a bitmap
 	 */
 	private Bitmap loadBitmapFromView(View v) {
 		Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(),
 				Bitmap.Config.ARGB_8888);
 		Canvas c = new Canvas(b);
 		v.layout(0, 0, v.getWidth(), v.getHeight());
 		v.draw(c);
 		return b;
 	}
 
 	/**
 	 * Save ad and continue to the next stage in the funnel
 	 * 
 	 * @param view
 	 */
 	public void nextStage(View view) {
 		// if text has been added, merge it with the ad
 		Bitmap bmOverlay = mAdCreationManager.getCurrentBitmap();
 		if (!mPhotoText.getText().equals("")) {
 			// get text bitmap
 			Bitmap textBitmap = loadBitmapFromView(mPhotoText);
 			Bitmap currBitmap = mAdCreationManager.getCurrentBitmap();
 
 			// combine two bitmaps
 			bmOverlay = Bitmap.createBitmap(currBitmap.getWidth(),
 					currBitmap.getHeight(), currBitmap.getConfig());
 			Canvas canvas = new Canvas(bmOverlay);
 			canvas.drawBitmap(currBitmap, new Matrix(), null);
 			canvas.drawBitmap(textBitmap, mTextXPos,
 					mTextYPos - mPhotoText.getHeight(), null);
 		}
 		mAdCreationManager.nextStage(this, bmOverlay);
 	}
 
 	/**
 	 * Return to the previous stage without saving any changes
 	 * 
 	 * @param view
 	 */
 	public void prevStage(View view) {
 		mAdCreationManager.previousStage(this);
 	}
 
 	public void getHelp(View v) {
 		String message = "Add humorous, sincere, or sophic text to your ad, and decide where to put it.";
 		String title = "Step " + mAdCreationManager.getReadableCurrentStage()
 				+ " of " + mAdCreationManager.getNumStages();
 		Utility.createHelpDialog(this, message, title);
 	}
 }
