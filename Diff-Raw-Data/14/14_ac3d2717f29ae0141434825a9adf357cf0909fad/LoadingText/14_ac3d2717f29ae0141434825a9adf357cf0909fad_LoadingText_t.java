 package com.secondhand.controller;
 
 import java.util.Arrays;
 
 import org.anddev.andengine.engine.camera.Camera;
 import org.anddev.andengine.entity.text.ChangeableText;
 
 import com.secondhand.resource.Fonts;
 
 /*
  * Implements a loading animation that looks like this:
  "Loading."
  "Loading.."
  "Loading..."
  */
 public class LoadingText extends ChangeableText {
 
 	// how many seconds to wait before showing the next string in the
 	// "animation"
 	private static final float SECONDS_PER_STRING = 0.3f;
 
 	private String[] loadingStrings;
	private final String loadingString;
 	private int loadingStringsI;
 
 	private float secondsPassedSinceLastUpdate;
 	
 	public static String getPlaceHolderString() {
 		// a length of 20 should be good enough.
 		final char[] exmarks = new char[20];
 		Arrays.fill(exmarks, ' ');
 		return new String(exmarks);
 	}
 
 	public LoadingText(final String loadingString, final Camera camera) {
 		super(0, 0, Fonts.getInstance().menuItemFont,
 				getPlaceHolderString());
 		
 		this.loadingString = loadingString;
 
 		this.setText(getNextString());
 
 		// center the "loading" text both horizontally and vertically.
 		final float posX = camera.getWidth() / 2.0f - this.getWidth() / 2.0f;
 		final float posY = camera.getHeight() / 2.0f - this.getHeight() / 2.0f;
 		this.setPosition(posX, posY);
 	}
 
 	// get next string in the animation
 	private String getNextString() {
 		if (loadingStrings == null) {
 			// create the loading strings.
 
 			loadingStrings = new String[] { loadingString + ".",
 					loadingString + "..", loadingString + "..." };
 
 			// start the animation:
 			loadingStringsI = -1;
 		}
 
 		// the "%" makes it loop.
 		loadingStringsI = (loadingStringsI + 1) % loadingStrings.length;
 		return loadingStrings[loadingStringsI];
 	}
 	
 	@Override
 	protected void onManagedUpdate(final float pSecondsElapsed) {
 
 //		MyDebug.d("seconds passed: " + pSecondsElapsed);
 
 		secondsPassedSinceLastUpdate += pSecondsElapsed;
 
 		
 		if (secondsPassedSinceLastUpdate >= SECONDS_PER_STRING) {
 			secondsPassedSinceLastUpdate = 0;
 			final String next = getNextString();
 			this.setText(next);
 		}
 
 		super.onManagedUpdate(pSecondsElapsed);
 	}
 
 }
