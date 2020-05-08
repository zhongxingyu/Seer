 /* ========================================================================= *
  * Boarder                                                                   *
  * http://boarder.mikuz.org/                                                 *
  * ========================================================================= *
  * Copyright (C) 2013 Boarder                                                *
  *                                                                           *
  * Licensed under the Apache License, Version 2.0 (the "License");           *
  * you may not use this file except in compliance with the License.          *
  * You may obtain a copy of the License at                                   *
  *                                                                           *
  *     http://www.apache.org/licenses/LICENSE-2.0                            *
  *                                                                           *
  * Unless required by applicable law or agreed to in writing, software       *
  * distributed under the License is distributed on an "AS IS" BASIS,         *
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *
  * See the License for the specific language governing permissions and       *
  * limitations under the License.                                            *
  * ========================================================================= */
 
 package fi.mikuz.boarder.gui;
 
 import android.app.Dialog;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.RectF;
 import android.graphics.drawable.BitmapDrawable;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewTreeObserver;
 import android.view.ViewTreeObserver.OnGlobalLayoutListener;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.thoughtworks.xstream.XStream;
 
 import fi.mikuz.boarder.R;
 import fi.mikuz.boarder.app.BoarderActivity;
 import fi.mikuz.boarder.component.soundboard.GraphicalSound;
 import fi.mikuz.boarder.component.soundboard.GraphicalSoundboard;
 import fi.mikuz.boarder.util.XStreamUtil;
 import fi.mikuz.boarder.util.editor.SoundNameDrawing;
 
 public class ColorChanger extends BoarderActivity implements OnSeekBarChangeListener, ColorPickerDialog.OnColorChangedListener {
 	private String TAG = "ColorChanger";
 	
 	private String mParent;
 	private GraphicalSound mSound;
 	private GraphicalSoundboard mGsb;
 	
 	private boolean mChangingSoundColor = true;
 	
 	private TextView mAlphaValueText, mRedValueText, mGreenValueText, mBlueValueText;
 	private SeekBar alphaBar, redBar, greenBar, blueBar;
 	private View mPreview;
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
 		
 		Bundle extras = getIntent().getExtras();
 		mParent = extras.getString("parentKey");
 		
 		if (mParent.equals("changeBackgroundColor")) mChangingSoundColor = false;
 		
 		XStream xstream = XStreamUtil.graphicalBoardXStream();
 		if (mChangingSoundColor) mSound = (GraphicalSound) xstream.fromXML(extras.getString(XStreamUtil.SOUND_KEY));
 		mGsb = (GraphicalSoundboard) xstream.fromXML(extras.getString(XStreamUtil.SOUNDBOARD_KEY));
 		
 		LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
 		View layout = inflater.inflate(R.layout.color_changer, (ViewGroup) findViewById(R.id.root));
 		mPreview = layout.findViewById(R.id.preview);
 		mAlphaValueText = (TextView) layout.findViewById(R.id.alphaValueText);
 		mRedValueText = (TextView) layout.findViewById(R.id.redValueText);
 		mGreenValueText = (TextView) layout.findViewById(R.id.greenValueText);
 		mBlueValueText = (TextView) layout.findViewById(R.id.blueValueText);
 		alphaBar = (SeekBar) layout.findViewById(R.id.alphaBar);
 		redBar = (SeekBar) layout.findViewById(R.id.redBar);
 		greenBar = (SeekBar) layout.findViewById(R.id.greenBar);
 		blueBar = (SeekBar) layout.findViewById(R.id.blueBar);
 		
 		alphaBar.setOnSeekBarChangeListener(this);
 		redBar.setOnSeekBarChangeListener(this);
 		greenBar.setOnSeekBarChangeListener(this);
 		blueBar.setOnSeekBarChangeListener(this);
 		
 		int initialColor = 0;
 		if (mParent.equals("changeNameColor")) {
 			initialColor = Integer.valueOf(mSound.getNameTextColor());
 		} else if (mParent.equals("changeinnerPaintColor")) {
 			initialColor = Integer.valueOf(mSound.getNameFrameInnerColor());
 		} else if (mParent.equals("changeBorderPaintColor")) {
 			initialColor = Integer.valueOf(mSound.getNameFrameBorderColor());
 		} else if (mParent.equals("changeBackgroundColor")) {
 			initialColor = Integer.valueOf(mGsb.getBackgroundColor());
 		}
 		alphaBar.setProgress(Color.alpha(initialColor));
 		redBar.setProgress(Color.red(initialColor));
 		greenBar.setProgress(Color.green(initialColor));
 		blueBar.setProgress(Color.blue(initialColor));
 		
 		setContentView(layout);
 		
         ViewTreeObserver vto = mPreview.getViewTreeObserver();
         vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
             @Override
             public void onGlobalLayout() {
             	if (mChangingSoundColor) setNamePreviewPosition();
                 drawPreview();
             }
         });
 	}
 	
 	@Override
     public void colorChanged(int color) {
         redBar.setProgress(Color.red(color));
         greenBar.setProgress(Color.green(color));
         blueBar.setProgress(Color.blue(color));
     }
 	
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.color_changer_bottom, menu);
 	    
 	    if (!mChangingSoundColor) {
 	    	menu.setGroupVisible(R.id.copy, false);
 	    }
 	    return true;
     }
 	
 	@Override
     public boolean onOptionsItemSelected(MenuItem item) {
 		
         switch(item.getItemId()) {
         
         	case R.id.menu_pick_color:
         		if (mParent.equals("changeNameColor")) {
         			Dialog colorDialog = new ColorPickerDialog(this, this, Integer.valueOf(mSound.getNameTextColor()));
         			colorDialog.show();	
         		} else if (mParent.equals("changeinnerPaintColor")) {
         			Dialog colorDialog = new ColorPickerDialog(this, this, Integer.valueOf(mSound.getNameFrameInnerColor()));
         			colorDialog.show();	
         		} else if (mParent.equals("changeBorderPaintColor")) {
         			Dialog colorDialog = new ColorPickerDialog(this, this, Integer.valueOf(mSound.getNameFrameBorderColor()));
         			colorDialog.show();	
         		} else if (mParent.equals("changeBackgroundColor")) {
         			Dialog colorDialog = new ColorPickerDialog(this, this, Integer.valueOf(mGsb.getBackgroundColor()));
         			colorDialog.show();	
         		}
         		return true;
         		
         	case R.id.menu_save_color:
         		Bundle bundle = new Bundle();
         		bundle.putBoolean("copyKey", false);
     			bundle.putInt("colorKey", Color.argb(alphaBar.getProgress(), redBar.getProgress(), 
     					greenBar.getProgress(), blueBar.getProgress()));
     			
     			Intent intent = new Intent();
 				intent.putExtras(bundle);
 				
 				setResult(RESULT_OK, intent);
 				finish();
         		return true;
         		
         	case R.id.menu_cancel_color:
         		finish();
         		return true;
         		
         	case R.id.menu_copy_color:
         		Toast.makeText(getApplicationContext(), "Select a sound to copy", Toast.LENGTH_LONG).show();
         		Bundle copyBundle = new Bundle();
     			copyBundle.putBoolean("copyKey", true);
     			
     			Intent copyIntent = new Intent();
 				copyIntent.putExtras(copyBundle);
 				
 				setResult(RESULT_OK, copyIntent);
         		finish();
         		return true;
         
 	        default:
 	            return super.onOptionsItemSelected(item);
         }
     }
 	
 	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
 		update();
 	}
 
 	@Override
 	public void onStartTrackingTouch(SeekBar seekBar) {}
 
 	@Override
 	public void onStopTrackingTouch(SeekBar seekBar) {}
 	
 	private void update() {
 		String alphaValue = Float.toString(alphaBar.getProgress());
 		mAlphaValueText.setText(alphaValue.substring(0, alphaValue.indexOf('.')));
 		String redValue = Float.toString(redBar.getProgress());
 		mRedValueText.setText(redValue.substring(0, redValue.indexOf('.')));
 		String greenValue = Float.toString(greenBar.getProgress());
 		mGreenValueText.setText(greenValue.substring(0, greenValue.indexOf('.')));
 		String blueValue = Float.toString(blueBar.getProgress());
 		mBlueValueText.setText(blueValue.substring(0, blueValue.indexOf('.')));
 		
 		if (mParent.equals("changeNameColor")) {
 			mSound.setNameTextColor(alphaBar.getProgress(), redBar.getProgress(), greenBar.getProgress(), blueBar.getProgress());
 		} else if (mParent.equals("changeinnerPaintColor")) {
 			mSound.setNameFrameInnerColor(alphaBar.getProgress(), redBar.getProgress(), greenBar.getProgress(), blueBar.getProgress());
 		} else if (mParent.equals("changeBorderPaintColor")) {
 			mSound.setNameFrameBorderColor(alphaBar.getProgress(), redBar.getProgress(), greenBar.getProgress(), blueBar.getProgress());
 		} else if (mParent.equals("changeBackgroundColor")) {
 			mGsb.setBackgroundColor(alphaBar.getProgress(), redBar.getProgress(), greenBar.getProgress(), blueBar.getProgress());
 		}
 		
 		drawPreview();
 	}
 	
 	private void setNamePreviewPosition() {
 		SoundNameDrawing soundNameDrawing = new SoundNameDrawing(mSound);
 		RectF nameFrameRect = soundNameDrawing.getNameFrameRect();
 		
 		float padding = 10;
 		
 		if (nameFrameRect.width()+padding > (float) mPreview.getWidth()) {
 			float xScale = (float) mPreview.getWidth()/nameFrameRect.width();
 			mSound.setNameSize(mSound.getNameSize()*xScale-padding*xScale);
 			soundNameDrawing = new SoundNameDrawing(mSound);
 			nameFrameRect = soundNameDrawing.getNameFrameRect();
 		}
 		
 		if (nameFrameRect.height()+padding > (float) mPreview.getHeight()) {
 			float yScale = (float) mPreview.getHeight()/nameFrameRect.height();
 			mSound.setNameSize(mSound.getNameSize()*yScale-padding*yScale);
 			soundNameDrawing = new SoundNameDrawing(mSound);
 			nameFrameRect = soundNameDrawing.getNameFrameRect();
 		}
 		
 		mSound.setNameFrameX((float) mPreview.getWidth()/2-nameFrameRect.width()/2);
 		mSound.setNameFrameY(0);
 	}
 	
 	private void drawPreview() {
 		if (mPreview.getWidth() == 0 || mPreview.getHeight() == 0) {
 			Log.d(TAG, "Waiting for layout to initialize");
 		} else {
 			Bitmap bitmap = Bitmap.createBitmap(mPreview.getWidth(), mPreview.getHeight(), Bitmap.Config.ARGB_8888);
 			Canvas canvas = new Canvas(bitmap);
 			new PreviewDrawer(this).onDraw(canvas);
 			mPreview.setBackgroundDrawable(new BitmapDrawable(getResources(), bitmap));
 		}
 	}
 	
 	public class PreviewDrawer extends SurfaceView {
 		
 		public PreviewDrawer(Context context) {
 			super(context);
 		}
 
 		@Override
 		public void onDraw(Canvas canvas) {
 			
 			canvas.drawColor(mGsb.getBackgroundColor());
 			
 			if (mChangingSoundColor) {
 				float NAME_DRAWING_SCALE = SoundNameDrawing.NAME_DRAWING_SCALE;
 				
 				canvas.scale(1/NAME_DRAWING_SCALE, 1/NAME_DRAWING_SCALE);
 				SoundNameDrawing soundNameDrawing = new SoundNameDrawing(mSound);
 				
 				Paint nameTextPaint = soundNameDrawing.getBigCanvasNameTextPaint();
 				Paint borderPaint = soundNameDrawing.getBorderPaint();
 				Paint innerPaint = soundNameDrawing.getInnerPaint();
 				
 				RectF bigCanvasNameFrameRect = soundNameDrawing.getBigCanvasNameFrameRect();
 				
 			    canvas.drawRoundRect(bigCanvasNameFrameRect, 2*NAME_DRAWING_SCALE, 2*NAME_DRAWING_SCALE, innerPaint);
 				canvas.drawRoundRect(bigCanvasNameFrameRect, 2*NAME_DRAWING_SCALE, 2*NAME_DRAWING_SCALE, borderPaint);
 			    
 				int i = 0;
 			    for (String row : mSound.getName().split("\n")) {
 		    		canvas.drawText(row, (mSound.getNameFrameX()+2)*NAME_DRAWING_SCALE, 
 		    				mSound.getNameFrameY()*NAME_DRAWING_SCALE+(i+1)*mSound.getNameSize()*NAME_DRAWING_SCALE, nameTextPaint);
 		    		i++;
 			    }
 			    canvas.scale(NAME_DRAWING_SCALE, NAME_DRAWING_SCALE);
 			}
 		}
 	}
 
 }
