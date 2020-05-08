 /*
  * Copyright (C) 2007 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.android.gl2jni;
 
 import android.app.Activity;
 import android.graphics.Point;
 import android.hardware.Camera.Size;
 import android.media.AudioManager;
 import android.media.SoundPool;
 import android.media.SoundPool.OnLoadCompleteListener;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Display;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Toast;
 
 public class GL2JNIActivity extends Activity implements View.OnTouchListener {
 
 	private GL2JNIView mView;
 	private Point size = new Point();
 	private GL2JNILib lib = new GL2JNILib();
 
 	@Override
 	protected void onCreate(Bundle icicle) {
 		super.onCreate(icicle);
 		mView = new GL2JNIView(getApplication());
 		mView.setOnTouchListener(this);
 		Display display = getWindowManager().getDefaultDisplay();
 		display.getSize(size);
 		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
 		lib.setContext(this);
 		lib.setSoundPool(new SoundPool(10, AudioManager.STREAM_MUSIC, 0));
 		lib.getSoundPool().setOnLoadCompleteListener(new OnLoadCompleteListener() {
 			@Override
 			public void onLoadComplete(SoundPool soundPool, int sampleId,
 					int status) {
 				lib.setLoaded(true);
 			}
 		});
 		lib.setSoundIDA(lib.getSoundPool().load(this, R.raw.a, 1));
 		lib.setSoundIDB(lib.getSoundPool().load(this, R.raw.b, 1));
 		lib.setSoundIDG(lib.getSoundPool().load(this, R.raw.g, 1));
 		lib.setSoundIDCSharp(lib.getSoundPool().load(this, R.raw.cs, 1));
 		lib.setSoundIDC(lib.getSoundPool().load(this, R.raw.cs, 1));
 		lib.setSoundIDD(lib.getSoundPool().load(this, R.raw.d, 1));
 		lib.setSoundIDE(lib.getSoundPool().load(this, R.raw.e, 1));
 		lib.setSoundIDFSharp(lib.getSoundPool().load(this, R.raw.fs, 1));
 		lib.setAudioManager((AudioManager) getSystemService(AUDIO_SERVICE));
 		setContentView(mView);
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		mView.onPause();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		mView.onResume();
 	}
 
 	@Override
 	public boolean onTouch(View v, MotionEvent event) {
 		// TODO Auto-generated method stub
 		Float[] normilized = null;
 		switch(event.getAction() & MotionEvent.ACTION_MASK){
 		case MotionEvent.ACTION_DOWN:
 			normilized = ConvertToGL(event.getX(), event.getY());
 			lib.touch(normilized[0], normilized[1]);
 			break;
 		case MotionEvent.ACTION_POINTER_DOWN:
 //			Log.d("Test",Integer.toString(event.getActionIndex()));
 			normilized = ConvertToGL(event.getX(event.getActionIndex()), event.getY(event.getActionIndex()));
 			lib.touch(normilized[0], normilized[1]);
 			break;
 		case MotionEvent.ACTION_POINTER_UP:
 //			Log.d("Test",Integer.toString(event.getActionIndex()));
 			normilized = ConvertToGL(event.getX(event.getActionIndex()), event.getY(event.getActionIndex()));
 			lib.touchUp(normilized[0], normilized[1]);
 			break;
		case MotionEvent.ACTION_UP:
			//TODO: fix
			//lib.touchUpAll();
			break;
 		}
 		
 //		Toast toast = Toast.makeText(this.getApplicationContext(), "Touched "
 //				+ normilized[0] + "," + normilized[1], Toast.LENGTH_SHORT);
 //		toast.show();
 		return true;
 	}
 
 	// Default
 	public Float[] ConvertToGL(Float x, Float y) {
 		return ConvertToGL(x, y, 1.0f, 1.0f, -1.0f, -1.0f);
 	}
 
 	public Float[] ConvertToGL(Float x, Float y, Float maxX, Float maxY,
 			Float minX, Float minY) {
 		// Float helfX = size.x/2.0f;
 		// Float halfY = size.y/2.0f;
 		// Using absolute in case it is negative
 		Float xSizeGL = Math.abs(maxX) + Math.abs(minX);
 		Float ySizeGL = Math.abs(maxY) + Math.abs(minY);
 		Float halfXGL = xSizeGL / 2;
 		Float halfYGL = ySizeGL / 2;
 		// Float centerXGL = maxX-halfXGL;
 		// Float centerYGL = maxY-halfYGL;
 		// Negative 1 to flip due to origin being top left
 		Float normalizedX = (x * (xSizeGL / size.x) - maxX);
 		Float normalizedY = -1 * (y * (xSizeGL / size.y) - maxY);
 		Float[] values = { normalizedX, normalizedY };
 		return values;
 	}
 
 //	public void playA() {
 //		lib.setAudioManager((AudioManager) getSystemService(AUDIO_SERVICE));
 //		float actualVolume = (float) lib.getAudioManager().getStreamVolume(AudioManager.STREAM_MUSIC);
 //		float maxVolume = (float) lib.getAudioManager()
 //				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
 //		float volume = actualVolume / maxVolume;
 //		// Is the sound loaded already?
 //		if (lib.isLoaded()) {
 //			lib.getSoundPool().play(lib.getSoundID(), volume, volume, 1, 0, 1f);
 //			Log.e("Test", "Played sound");
 //		}
 //	}
 }
