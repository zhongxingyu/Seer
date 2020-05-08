 package com.bar.foo.accelkeyboard;
 
 import java.util.List;
 
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.inputmethodservice.InputMethodService;
 import android.inputmethodservice.Keyboard;
 import android.inputmethodservice.Keyboard.Key;
 import android.inputmethodservice.KeyboardView;
 import android.os.AsyncTask;
 import android.text.InputType;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
 import android.view.inputmethod.InputConnection;
 
 /**
  * Informatics 143
  * AccelKeyboard, a touch-free keyboard for Android
  * Melvin Chien and Danny Ngo
  * January 22, 2013
 * Modified code based off of SoftKeyboard sample from Android SDK samples
  * 
  * 
  * Example of writing an input method for a soft keyboard. This code is focused on simplicity over completeness, so it
  * should in no way be considered to be a complete soft keyboard implementation. Its purpose is to provide a basic
  * example for how you would get started writing an input method, to be fleshed out as appropriate.
  */
 public class AccelKeyboard extends InputMethodService implements KeyboardView.OnKeyboardActionListener {
 
 	private KeyboardView keyboardView;
 
 	private StringBuilder mComposing = new StringBuilder();
 
 	private LatinKeyboard qwertyKeyboard;
 
 	private String mWordSeparators;
 
 	SensorManager sm;
 	SensorEventListener accelListener;
 	SensorEventListener lightListener;
 	private int curIndex;
 	private List<Key> keys;
 	private Key curKey;
 	private Key prevKey;
 	private boolean enterLock;
 	private String accelX;
 
 	/**
 	 * Main initialization of the input method component. Be sure to call to super class.
 	 */
 	@Override
 	public void onCreate() {
 		super.onCreate();
 
 		mWordSeparators = getResources().getString(R.string.word_separators);
 
 		sm = (SensorManager) getSystemService(SENSOR_SERVICE);
 		enterLock = false;
 		accelListener = new SensorEventListener() {
 			public void onSensorChanged(SensorEvent event) {
 				new accelComp().execute(event);
 				stopSelf();
 			}
 
 			@Override
 			public void onAccuracyChanged(Sensor sensor, int accuracy) {
 			}
 		};
 		lightListener = new SensorEventListener() {
 			public void onSensorChanged(SensorEvent event) {
 				new lightComp().execute(event);
 				stopSelf();
 			}
 
 			@Override
 			public void onAccuracyChanged(Sensor sensor, int accuracy) {
 			}
 		};
 	}
 
 	/**
 	 * This is the point where you can do all of your UI initialization. It is called after creation and any
 	 * configuration change.
 	 */
 	@Override
 	public void onInitializeInterface() {
 		qwertyKeyboard = new LatinKeyboard(this, R.xml.qwerty);
 		curIndex = 0;
 		keys = qwertyKeyboard.getKeys();
 		// curKey = keys.get(curIndex);
 		Log.i("keys", Integer.toString(keys.size()));
 	}
 
 	// this function makes it so that the app doesn't crash when the keyboard UI is being updated
 	private class accelComp extends AsyncTask<SensorEvent, Void, String> {
 
 		protected void onPostExecute(String result) {
 			float ansZ = Float.parseFloat(result);
 			float ansX = Float.parseFloat(accelX);
 			int nextIndex = curIndex; // check next index
 			prevKey = keys.get(curIndex); // keep track of previous key
 
 			// tilting right
 			if (ansZ < -2.5) {
 				nextIndex++;
 				if (nextIndex != 11 && nextIndex != 22 && nextIndex != 33 && nextIndex != 44) {
 					curIndex = nextIndex;
 				}
 			}
 			// tilting left
 			else if (ansZ > 2.5) {
 				nextIndex--;
 				if (nextIndex != -1 && nextIndex != 10 && nextIndex != 21 && nextIndex != 32) {
 					curIndex = nextIndex;
 				}
 			}
 			// tilting up
 			else if (ansX < -2.5) {
 				nextIndex -= 11;
 				if (nextIndex >= 0) {
 					curIndex = nextIndex;
 				}
 			}
 			// tilting down
 			else if (ansX > 2.5) {
 				nextIndex += 11;
 				if (nextIndex <= 43) {
 					curIndex = nextIndex;
 				}
 			}
 
 			prevKey.pressed = false;
 			curKey = keys.get(curIndex);
 			curKey.pressed = true;
 			handleShift(); // Ugly call to force the keyboard to update
 			handleShift();
 		}
 
 		protected String doInBackground(SensorEvent... event) {
 			Log.i("key", Integer.toString(curIndex));
 			curKey = keys.get(curIndex);
 			String result = Float.toString(event[0].values[0]);
 			accelX = Float.toString(event[0].values[1]);
 			Log.i("accelX", accelX);
 			Log.i("accelY", result);
 			return result;
 		}
 	}
 
 	private class lightComp extends AsyncTask<SensorEvent, Void, String> {
 
 		protected void onPostExecute(String result) {
 			float ans = Float.parseFloat(result);
 			if (ans < 8) { // if the light sensor reading reaches below the specified threshold
 				if (!enterLock) { // need this lock so that it doesn't keep entering the key
 					onKey(curKey.codes[0], null);
 					enterLock = true;
 				}
 			} else {
 				enterLock = false;
 			}
 			Log.i("light", Float.toString(ans));
 		}
 
 		@Override
 		protected String doInBackground(SensorEvent... event) {
 			Log.i("enterlock", String.valueOf(enterLock));
 			String result = Float.toString(event[0].values[0]);
 			return result;
 		}
 	}
 
 	/**
 	 * Called by the framework when your view for creating input needs to be generated. This will be called the first
 	 * time your input method is displayed, and every time it needs to be re-created such as due to a configuration
 	 * change.
 	 */
 	@Override
 	public View onCreateInputView() {
 		keyboardView = (KeyboardView) getLayoutInflater().inflate(R.layout.input, null);
 		keyboardView.setOnKeyboardActionListener(this);
 		keyboardView.setKeyboard(qwertyKeyboard);
 		return keyboardView;
 	}
 
 	/**
 	 * This is the main point where we do our initialization of the input method to begin operating on an application.
 	 * At this point we have been bound to the client, and are now receiving all of the detailed information about the
 	 * target of our edits.
 	 */
 	@Override
 	public void onStartInput(EditorInfo attribute, boolean restarting) {
 		super.onStartInput(attribute, restarting);
 
 		// Reset our state. We want to do this even if restarting, because
 		// the underlying state of the text editor could have changed in any
 		// way.
 		mComposing.setLength(0);
 		updateShiftKeyState(attribute);
 
 		// Update the label on the enter key, depending on what the application
 		// says it will do.
 		qwertyKeyboard.setImeOptions(getResources(), attribute.imeOptions);
 
 		sm.registerListener(accelListener, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
 				SensorManager.SENSOR_DELAY_NORMAL);
 		sm.registerListener(lightListener, sm.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL);
 
 	}
 
 	/**
 	 * This is called when the user is done editing a field. We can use this to reset our state.
 	 */
 	@Override
 	public void onFinishInput() {
 		super.onFinishInput();
 
 		// Clear current composing text and candidates.
 		mComposing.setLength(0);
 
 		// We only hide the candidates window when finishing input on
 		// a particular editor, to avoid popping the underlying application
 		// up and down if the user is entering text into the bottom of
 		// its window.
 		setCandidatesViewShown(false);
 
 		if (keyboardView != null) {
 			keyboardView.closing();
 		}
 
 		sm.unregisterListener(accelListener);
 		sm.unregisterListener(lightListener);
 	}
 
 	@Override
 	public void onStartInputView(EditorInfo attribute, boolean restarting) {
 		super.onStartInputView(attribute, restarting);
 		// Apply the selected keyboard to the input view.
 		keyboardView.setKeyboard(qwertyKeyboard);
 		keyboardView.closing();
 	}
 
 	/**
 	 * Deal with the editor reporting movement of its cursor.
 	 */
 	@Override
 	public void onUpdateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd, int candidatesStart,
 			int candidatesEnd) {
 		super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd);
 
 		// If the current selection in the text view changes, we should
 		// clear whatever candidate text we have.
 		if (mComposing.length() > 0 && (newSelStart != candidatesEnd || newSelEnd != candidatesEnd)) {
 			mComposing.setLength(0);
 			InputConnection ic = getCurrentInputConnection();
 			if (ic != null) {
 				ic.finishComposingText();
 			}
 		}
 	}
 
 	/**
 	 * Use this to monitor key events being delivered to the application. We get first crack at them, and can either
 	 * resume them or let them continue to the app.
 	 */
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		switch (keyCode) {
 		case KeyEvent.KEYCODE_BACK:
 			// The InputMethodService already takes care of the back key for us, to dismiss the input method if it is
 			// shown. However, our keyboard could be showing a pop-up window that back should dismiss, so we first allow
 			// it to do that.
 			if (event.getRepeatCount() == 0 && keyboardView != null) {
 				if (keyboardView.handleBack()) {
 					return true;
 				}
 			}
 			break;
 
 		case KeyEvent.KEYCODE_DEL:
 			// Special handling of the delete key: if we currently are
 			// composing text for the user, we want to modify that instead
 			// of let the application to the delete itself.
 			if (mComposing.length() > 0) {
 				onKey(Keyboard.KEYCODE_DELETE, null);
 				return true;
 			}
 			break;
 
 		case KeyEvent.KEYCODE_ENTER:
 			// Let the underlying text editor always handle these.
 			return false;
 		}
 
 		return super.onKeyDown(keyCode, event);
 	}
 
 	/**
 	 * Use this to monitor key events being delivered to the application. We get first crack at them, and can either
 	 * resume them or let them continue to the app.
 	 */
 	@Override
 	public boolean onKeyUp(int keyCode, KeyEvent event) {
 		return super.onKeyUp(keyCode, event);
 	}
 
 	/**
 	 * Helper function to commit any text being composed in to the editor.
 	 */
 	private void commitTyped(InputConnection inputConnection) {
 		if (mComposing.length() > 0) {
 			inputConnection.commitText(mComposing, mComposing.length());
 			mComposing.setLength(0);
 		}
 	}
 
 	/**
 	 * Helper to update the shift state of our keyboard based on the initial editor state.
 	 */
 	private void updateShiftKeyState(EditorInfo attr) {
 		if (attr != null && keyboardView != null && qwertyKeyboard == keyboardView.getKeyboard()) {
 			int caps = 0;
 			EditorInfo ei = getCurrentInputEditorInfo();
 			if (ei != null && ei.inputType != InputType.TYPE_NULL) {
 				caps = getCurrentInputConnection().getCursorCapsMode(attr.inputType);
 			}
 			keyboardView.setShifted(caps != 0);
 		}
 	}
 
 	/**
 	 * Helper to send a key down / key up pair to the current editor.
 	 */
 	private void keyDownUp(int keyEventCode) {
 		getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
 		getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
 	}
 
 	/**
 	 * Helper to send a character to the editor as raw key events.
 	 */
 	private void sendKey(int keyCode) {
 		switch (keyCode) {
 		case '\n':
 			keyDownUp(KeyEvent.KEYCODE_ENTER);
 			break;
 		default:
 			if (keyCode >= '0' && keyCode <= '9') {
 				keyDownUp(keyCode - '0' + KeyEvent.KEYCODE_0);
 			} else {
 				getCurrentInputConnection().commitText(String.valueOf((char) keyCode), 1);
 			}
 			break;
 		}
 	}
 
 	// Implementation of KeyboardViewListener
 
 	@Override
 	public void onKey(int primaryCode, int[] keyCodes) {
 		if (isWordSeparator(primaryCode)) {
 			// Handle separator
 			if (mComposing.length() > 0) {
 				commitTyped(getCurrentInputConnection());
 			}
 			sendKey(primaryCode);
 			updateShiftKeyState(getCurrentInputEditorInfo());
 		} else if (primaryCode == Keyboard.KEYCODE_DELETE) {
 			handleBackspace();
 		} else if (primaryCode == Keyboard.KEYCODE_SHIFT) {
 			handleShift();
 		} else if (primaryCode == Keyboard.KEYCODE_CANCEL) {
 			handleClose();
 			return;
 
 		} else if (primaryCode == Keyboard.KEYCODE_MODE_CHANGE && keyboardView != null) {
 			Keyboard current = keyboardView.getKeyboard();
 			keyboardView.setKeyboard(current);
 		} else {
 			handleCharacter(primaryCode, keyCodes);
 		}
 	}
 
 	@Override
 	public void onText(CharSequence text) {
 		InputConnection ic = getCurrentInputConnection();
 		if (ic == null)
 			return;
 		ic.beginBatchEdit();
 		if (mComposing.length() > 0) {
 			commitTyped(ic);
 		}
 		ic.commitText(text, 0);
 		ic.endBatchEdit();
 		updateShiftKeyState(getCurrentInputEditorInfo());
 	}
 
 	private void handleBackspace() {
 		final int length = mComposing.length();
 		if (length > 1) {
 			mComposing.delete(length - 1, length);
 			getCurrentInputConnection().setComposingText(mComposing, 1);
 		} else if (length > 0) {
 			mComposing.setLength(0);
 			getCurrentInputConnection().commitText("", 0);
 		} else {
 			keyDownUp(KeyEvent.KEYCODE_DEL);
 		}
 		updateShiftKeyState(getCurrentInputEditorInfo());
 	}
 
 	private void handleShift() {
 		if (keyboardView == null) {
 			return;
 		}
 
 		Keyboard currentKeyboard = keyboardView.getKeyboard();
 		if (qwertyKeyboard == currentKeyboard) {
 			keyboardView.setShifted(!keyboardView.isShifted());
 		}
 	}
 
 	private void handleCharacter(int primaryCode, int[] keyCodes) {
 		if (isInputViewShown()) {
 			if (keyboardView.isShifted()) {
 				primaryCode = Character.toUpperCase(primaryCode);
 			}
 		}
 		getCurrentInputConnection().commitText(String.valueOf((char) primaryCode), 1);
 
 	}
 
 	private void handleClose() {
 		commitTyped(getCurrentInputConnection());
 		requestHideSelf(0);
 		keyboardView.closing();
 	}
 
 	private String getWordSeparators() {
 		return mWordSeparators;
 	}
 
 	public boolean isWordSeparator(int code) {
 		String separators = getWordSeparators();
 		return separators.contains(String.valueOf((char) code));
 	}
 
 	@Override
 	public void swipeRight() {
 	}
 
 	@Override
 	public void swipeLeft() {
 		handleBackspace();
 	}
 
 	@Override
 	public void swipeDown() {
 		handleClose();
 	}
 
 	@Override
 	public void swipeUp() {
 	}
 
 	@Override
 	public void onPress(int primaryCode) {
 	}
 
 	@Override
 	public void onRelease(int primaryCode) {
 	}
 }
