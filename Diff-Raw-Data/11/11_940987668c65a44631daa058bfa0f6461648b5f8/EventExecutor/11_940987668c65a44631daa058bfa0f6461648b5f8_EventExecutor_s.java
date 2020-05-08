 package com.kg6.schlafdoedel.event;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.List;
 import java.util.Random;
 
 import com.kg6.schlafdoedel.custom.Util;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.FrameLayout;
 
 public class EventExecutor extends Thread {
 	private final int EVENT_ANIMATION_SLEEPTIME = 50;
 	private final int VIEW_MARGIN = 20;
 	
 	private final Activity CONTEXT;
 	private final FrameLayout CONTAINER;
 	private final EventScheduler EVENT_SCHEDULER;
 	private final Event EVENT;
 	
 	private MediaPlayer mediaPlayer;
 	private EventAnimationPanel animationPanel;
 	private boolean enabled;
 	private float currentVolume;
 	
 	public EventExecutor(Activity context, EventScheduler eventScheduler, Event event, FrameLayout container) {
 		CONTEXT = context;
 		CONTAINER = container;
 		EVENT_SCHEDULER = eventScheduler;
 		EVENT = event;
 		
 		this.mediaPlayer = null;
 		this.animationPanel = null;
 		
 		this.enabled = true;
 		this.currentVolume = 0.0f;
 	}
 	
 	private void cleanup() {
 		CONTEXT.runOnUiThread(new Runnable() {
 					
 			@Override
 			public void run() {
 				stopPlayback();
 				
 				try {
 					if(animationPanel != null) {
 						animationPanel.cleanup();
 						
 						CONTAINER.removeView(animationPanel);
 					}
 					
 					for(int i = 0; i < CONTAINER.getChildCount(); i++) {
 						CONTAINER.getChildAt(i).setVisibility(View.VISIBLE);
 					}
 				} catch (Exception e) {
 					Log.e("EventExecutor.java", "Unable to cleanup the event image view", e);
 				}
 			}
 		});
 	}
 	
 	public Event getEvent() {
 		return EVENT;
 	}
 	
 	public void dismiss() {
 		this.enabled = false;
 	}
 	
 	public boolean isDismissed() {
 		return !this.enabled;
 	}
 	
 	public void setVolume(float volume) {
 		try {
 			if(this.mediaPlayer != null) {
 				this.mediaPlayer.setVolume(volume, volume);
 			}
 		} catch (Exception e) {
 			Log.e("EventExecutor.java", "Unable to change the event media volume", e);
 		}
 	}
 	
 	public float getVolume() {
 		return currentVolume;
 	}
 	
 	public void run() {
 		try {
 			prepareViewContainer();
 			
 			List<EventSource> eventSourceList = EVENT.getEventSourceList();
 			
 			for(EventSource source : eventSourceList) {
 				switch(source.getSourceType()) {
 					case Image:
						loadEventBitmap(source);					
 						break;
 					case Music:
 						startPlayback(source);
 						break;
 					default:
 						return;
 				}
 			}
 			
 			if(this.animationPanel != null && this.animationPanel.getEventBitmap() == null) {
 				this.animationPanel.setBackgroundBlinkingAnimationEnabled(true);
 			}
 			
 			while(this.enabled) {
 				if(this.animationPanel != null) {
 					this.animationPanel.postInvalidate();
 				}
 				
 				try {
 					Thread.sleep(EVENT_ANIMATION_SLEEPTIME);
 				} catch (InterruptedException e) {
 					
 				}
 			}
 		} catch (Exception e) {
 			Log.e("EventExecutor.java", "Unable to execute event", e);
 		} finally {
 			cleanup();
 		}
 	}
 	
 	private void prepareViewContainer() {
 		CONTEXT.runOnUiThread(new Runnable() {
 			
 			@Override
 			public void run() {
 				for(int i = 0; i < CONTAINER.getChildCount(); i++) {
 					CONTAINER.getChildAt(i).setVisibility(View.GONE);
 				}
 				
 				if(animationPanel != null) {
 					CONTAINER.removeView(animationPanel);
 				}
 				
 				animationPanel = new EventAnimationPanel(CONTEXT, EVENT.getTitle());
 				
 				CONTAINER.addView(animationPanel, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 			}
 			
 		});
 	}
 	
 	private void loadEventBitmap(EventSource source) {
 		if(this.animationPanel != null) {
 			Bitmap eventBitmap = loadBitmap(source, Util.GetDeviceWidth(CONTEXT) - 2 * VIEW_MARGIN);
 			
 			if(eventBitmap != null) {
 				this.animationPanel.setEventBitmap(eventBitmap);
 			} else {
 				EVENT_SCHEDULER.raiseEventError(EVENT, "Unable to load the image");
 			}
 		}
 	}
 	
 	private void startPlayback(EventSource source) {
 		final String url = source.getUrl();
 		
 		try {
 			this.mediaPlayer = new MediaPlayer();
 			this.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
 			this.mediaPlayer.setDataSource(url);
 			this.mediaPlayer.prepare();
 			this.mediaPlayer.start();
 			 
 			AudioManager am = (AudioManager) CONTEXT.getSystemService(Context.AUDIO_SERVICE);
 			int streamVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
 			int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
 			
 			currentVolume = (float)streamVolume / (float)maxVolume;
 		} catch (Exception e) {
 			Log.e("EventExecutor.java", String.format("Unable to play audio from URL %s", url), e);
 			
 			EVENT_SCHEDULER.raiseEventError(EVENT, "Unable to play audio");
 		}
 	}
 	
 	private void stopPlayback() {
 		try {
 			if(this.mediaPlayer != null) {
 				this.mediaPlayer.stop();
 				this.mediaPlayer.release();
 			}
 		} catch (Exception e) {
 			Log.e("EventExecutor.java", "Unable to stop audio playback", e);
 		}
 	}
 	
 	private Bitmap loadBitmap(EventSource source, float width) {
         Bitmap loadedBitmap = null;
         Bitmap scaledBitmap = null;
         
         InputStream inputStream = null;
         
         try {
         	inputStream = new URL(source.getUrl()).openStream();
         	
         	//Load the bitmap
         	loadedBitmap = BitmapFactory.decodeStream(inputStream);
         	
         	//Scale the bitmap to the output size
         	float ratio = (float)loadedBitmap.getWidth() / (float)loadedBitmap.getHeight();
         	
         	scaledBitmap = Bitmap.createScaledBitmap(loadedBitmap, (int)width, (int)(width / ratio), true);
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
         	if(inputStream != null) {
         		try {
 					inputStream.close();
 				} catch (IOException e) {
 					
 				}
         	}
         	
         	if(loadedBitmap != null) {
         		try {
         			loadedBitmap.recycle();
         			loadedBitmap = null;
         		} catch (Exception e) {
         			
         		}
         	}
         }
         
         return scaledBitmap;
 	}
 	
 	private class EventAnimationPanel extends View {
 		private final float ANIMATION_TEXTSIZE_MAXIMUM = 70f;
 		private final float ANIMATION_TEXTSIZE_MINIMUM = 10f;
 		private final float ANIMATION_TEXTSIZE_INCREMENT = 1f;
 		
 		private final int[] ANIMATION_BACKGROUND_COLORS = new int[] { Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN, Color.MAGENTA };
 		
 		private final Paint TEXT_FOREGROUND_PAINT;
 		private final Paint TEXT_BACKGROUND_PAINT;
 		
 		private final String ANIMATION_TEXT;
 		
 		private final Random RANDOM;
 		
 		private Bitmap eventBitmap;
 		private float animationTextSize;
 		private float animationDirection;
 		
 		private boolean backgroundBlinkingAnimationEnabled;
 
 		public EventAnimationPanel(Context context, String animationText) {
 			super(context);
 			
 			ANIMATION_TEXT = animationText;
 			
 			TEXT_FOREGROUND_PAINT = new Paint();
 			TEXT_FOREGROUND_PAINT.setColor(Color.WHITE);
 			TEXT_FOREGROUND_PAINT.setStyle(Style.FILL);
 			TEXT_FOREGROUND_PAINT.setFlags(Paint.ANTI_ALIAS_FLAG);
 			
 			TEXT_BACKGROUND_PAINT = new Paint();
 			TEXT_BACKGROUND_PAINT.setColor(Color.BLACK);
 			TEXT_BACKGROUND_PAINT.setStyle(Style.STROKE);
 			TEXT_BACKGROUND_PAINT.setStrokeWidth(2);
 			TEXT_BACKGROUND_PAINT.setFlags(Paint.ANTI_ALIAS_FLAG);
 			
 			RANDOM = new Random();
 			
 			this.animationTextSize = ANIMATION_TEXTSIZE_MAXIMUM;
 			this.animationDirection = -1;
 			
 			this.backgroundBlinkingAnimationEnabled = false;
 		}
 		
 		public void cleanup() {
 			try {
 				if(this.eventBitmap != null) {
 					this.eventBitmap.recycle();
 					this.eventBitmap = null;
 				}
 			} catch (Exception e) {
 				Log.e("EventExecutor.java", "Unable to recycle event bitmap", e);
 			}
 		}
 		
 		public void setEventBitmap(Bitmap bitmap) {
 			this.eventBitmap = bitmap;
 		}
 		
 		public Bitmap getEventBitmap() {
 			return this.eventBitmap;
 		}
 		
 		public void setBackgroundBlinkingAnimationEnabled(boolean enabled) {
 			this.backgroundBlinkingAnimationEnabled = enabled;
 		}
 
 		@Override
 		protected void onDraw(Canvas canvas) {
 			super.onDraw(canvas);
 			
 			final int halfWidth = getWidth() / 2;
 			final int halfHeight = getHeight() / 2;
 			
 			if(this.eventBitmap != null) {
 				canvas.drawBitmap(this.eventBitmap, halfWidth - this.eventBitmap.getWidth() / 2, halfHeight - this.eventBitmap.getHeight() / 2, null);
 			} else if (this.backgroundBlinkingAnimationEnabled) {
 				canvas.drawColor(ANIMATION_BACKGROUND_COLORS[RANDOM.nextInt(ANIMATION_BACKGROUND_COLORS.length)]);
 			}
 			
 			//Animate the text size
 			TEXT_FOREGROUND_PAINT.setTextSize(this.animationTextSize);
 			TEXT_BACKGROUND_PAINT.setTextSize(this.animationTextSize);
 			
 			this.animationTextSize += ANIMATION_TEXTSIZE_INCREMENT * this.animationDirection;
 			
 			if(this.animationTextSize <= ANIMATION_TEXTSIZE_MINIMUM || this.animationTextSize >= ANIMATION_TEXTSIZE_MAXIMUM) {
 				this.animationDirection *= -1;
 			}
 			
 			//Draw the text
 			final int x = halfWidth - (int)(TEXT_FOREGROUND_PAINT.measureText(ANIMATION_TEXT) / 2f);
 			final int y = halfHeight;
 			
 			canvas.drawText(ANIMATION_TEXT, x, y, TEXT_BACKGROUND_PAINT);
 			canvas.drawText(ANIMATION_TEXT, x, y, TEXT_FOREGROUND_PAINT);
 		}
 	}
 }
