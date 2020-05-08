 /* Copyright 2009 Sean Hodges <seanhodges@bluebottle.com>
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package tesla.app.ui.widget;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import tesla.app.R;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.View;
 
 public class VolumeSlider extends View {
 	
 	public static final int MAX_LEVEL = 7;
 	private static final int EXTRA_MUTE_HEIGHT = 20; // This is a fudge because the mute button is slightly taller than the levels
 
 	private float currentLevel = 0;
 	private float minVolume;
 	private float maxVolume;
 	
 	private OnVolumeLevelChangeListener listener;
 
 	private boolean levelsCalculated = false;
 	private List<LevelDrawable> levelDrawable = null;
 	
 	public interface OnVolumeLevelChangeListener {
 		public void onLevelChanged(VolumeSlider volumeSlider, float level);
 		public void onChangeFinished(VolumeSlider volumeSlider);
 	}
 	
 	private class LevelDrawable {
 		public float geometryPosition;
 		public float level;
 		public int resource;
 	}
 
 	// Need all of these
 	public VolumeSlider(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		setUp(context);
 	}
 	
 	public VolumeSlider(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		setUp(context);
 	}
 	
 	public VolumeSlider(Context context) {
 		super(context);
 		setUp(context);
 	}
 
 	private void setUp(Context context) {
 		if (context == null) context = getContext();
 		if (levelDrawable == null) levelDrawable = new ArrayList<LevelDrawable>();
 	}
 	
 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 		// Calculate the level positions after widget has been drawn
 		if (!levelsCalculated) {
 			calculateLevels(null);
 			levelsCalculated = true;
 			// Set default image
 			changeImage(R.drawable.vol0);
 			refresh();
 		}
 		
 	}
 	
 	public void refresh() {
 		for (LevelDrawable item : levelDrawable) {
 			if (item.level >= currentLevel) {
 				changeImage(item.resource);
 				break;
 			}
 		}
 	}
 	
 	private void calculateLevels(Context context) {
 		if (context == null) context = getContext();
 		float itemLevel = ((maxVolume - minVolume) / (MAX_LEVEL - 1)) + minVolume;
 		float itemHeight = (getHeight() - EXTRA_MUTE_HEIGHT) / MAX_LEVEL;
 		float currentHeight = getHeight();
 		for (int i = 0; i < MAX_LEVEL; i++) {
 			LevelDrawable item = new LevelDrawable();
 			currentHeight -= itemHeight;
 			item.level = itemLevel * i;
 			item.geometryPosition = currentHeight;
 			switch (i) {
 				case 0:
 					// Adjust for extra size of mute button
 					currentHeight -= EXTRA_MUTE_HEIGHT;
 					item.geometryPosition = currentHeight;
 					item.resource = R.drawable.vol0; 
 					break;
 				case 1: item.resource = R.drawable.vol1; break;
 				case 2: item.resource = R.drawable.vol2; break;
 				case 3: item.resource = R.drawable.vol3; break;
 				case 4: item.resource = R.drawable.vol4; break;
 				case 5: item.resource = R.drawable.vol5; break;
 				case 6:
 					// Ensure the last item is 100%
 					item.level = maxVolume;
 					item.resource = R.drawable.vol6; 
 					break;
 			}
 			levelDrawable.add(item);
 		}
 	}
 	
 	private void changeImage(int newImage) {
 		setBackgroundResource(newImage);
 	}
 	
 	public boolean onTouchEvent(MotionEvent event) {
 		// TODO: This event is being called twice
 		boolean handled = false;
 		if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
 			for (LevelDrawable item : levelDrawable) {
 				if (event.getY() > item.geometryPosition) {
 					currentLevel = item.level;
 					changeImage(item.resource);
 					break;
 				}
 			}
 			listener.onLevelChanged(this, currentLevel);
 			handled = true;
 		}
 		else if (event.getAction() == MotionEvent.ACTION_UP) {
 			listener.onChangeFinished(this);
 		}
 		return handled;
 	}
 	
 	public void setOnVolumeLevelChangeListener(OnVolumeLevelChangeListener listener) {
 		this.listener = listener;
 	}
 
 	public float getLevel() {
 		return currentLevel;
 	}
	
	public byte getLevelPercent() {
		byte out = 0;
		if (currentLevel > 0) {
			out = (byte)((currentLevel / maxVolume) * 100);
		}
		return out;
	}
 
 	public void setLevel(float currentLevel) {
 		this.currentLevel = currentLevel;
 	}
 
 	public void setMinVolume(float minVolume) {
 		this.minVolume = minVolume;
 	}
 
 	public float getMinVolume() {
 		return minVolume;
 	}
 
 	public void setMaxVolume(float maxVolume) {
 		this.maxVolume = maxVolume;
 	}
 
 	public float getMaxVolume() {
 		return maxVolume;
 	}
 }
