 package steve4448.livetextbackground.background;
 
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import steve4448.livetextbackground.background.object.ExplosionParticle;
 import steve4448.livetextbackground.background.object.ExplosionParticleGroup;
 import steve4448.livetextbackground.background.object.TextObject;
 import steve4448.livetextbackground.util.PreferenceHelper;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.graphics.Typeface;
 import android.os.Handler;
 import android.service.wallpaper.WallpaperService;
 import android.view.SurfaceHolder;
 
 public class LiveTextBackgroundService extends WallpaperService {
 	@Override
 	public Engine onCreateEngine() {
 		return new LiveTextBackgroundEngine();
 	}
 	
 	private class LiveTextBackgroundEngine extends Engine {
 		public PreferenceHelper pref;
 		private Paint paintBackground;
 		private Paint paintText;
 		private Paint paintRect;
 		private Paint paintRectShadow;
 		private CopyOnWriteArrayList<TextObject> textObj = new CopyOnWriteArrayList<TextObject>();
 		private CopyOnWriteArrayList<ExplosionParticleGroup> textExplObj = new CopyOnWriteArrayList<ExplosionParticleGroup>();
 		private Timer logicTimer;
 		private TimerTask logicTimerTask;
 		private boolean hasShadow;
 		private final Handler paintHandler = new Handler();
 		private final Runnable paintRunnable = new Runnable() {
 			@Override
 			public void run() {
 				draw();
 			}
 		};
 		private boolean visible = false;
 		
 		private LiveTextBackgroundEngine() {
 			pref = new PreferenceHelper(getBaseContext());
 			paintBackground = new Paint();
 			
 			paintRect = new Paint();
 			paintRect.setStyle(Paint.Style.FILL);
 			
 			paintRectShadow = new Paint();
 			paintRectShadow.setStyle(Paint.Style.FILL);
 
 			paintText = new Paint();
 			paintText.setAntiAlias(true);
 			paintText.setTypeface(Typeface.SANS_SERIF);
 			paintText.setStyle(Paint.Style.FILL);
 		}
 		
 		@Override
 		public void onCreate(SurfaceHolder holder) {
 			super.onCreate(holder);
 		}
 		
 		@Override
 		public void onDestroy() {
 			super.onDestroy();
 			setupLogicHandler(false);
 		}
 		
 		@Override
 		public void onSurfaceCreated(SurfaceHolder holder) {
 			super.onSurfaceCreated(holder);
 		}
 		
 		@Override
 		public void onSurfaceDestroyed(SurfaceHolder holder) {
 			super.onSurfaceDestroyed(holder);
 			this.visible = false;
 			setupLogicHandler(false);
 		}
 		
 		@Override
 		public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
 			super.onSurfaceChanged(holder, format, width, height);
 			setupLogicHandler(true);
 		}
 		
 		@Override
 		public void onVisibilityChanged(boolean visible) {
 			this.visible = visible;
 			setupLogicHandler(visible);
 		}
 		
 		public void setupLogicHandler(boolean on) {
 			System.out.println("Setting up logic handler? " + on);
 			if(on) {
 				if(logicTimer != null)
 					setupLogicHandler(false);
 				
 				logicTimer = new Timer();
 				logicTimer.schedule(logicTimerTask = new TimerTask() {
 					@Override
 					public void run() {
 						logic();
 					}
 				}, pref.desiredFPS, pref.desiredFPS);
 				paintHandler.post(paintRunnable);
 			} else {
 				if(logicTimer != null) {
 					if(logicTimerTask != null) {
 						logicTimerTask.cancel();
 						logicTimerTask = null;
 					}
 					logicTimer.cancel();
 					logicTimer.purge();
 					logicTimer = null;
 				}
 				paintHandler.removeCallbacks(paintRunnable);
 			}
 		}
 		
 		private void logic() {
 			// long startTime = System.currentTimeMillis();
 			if((int)(Math.random() * 10) == 1) {
 				String text = pref.availableStrings[(int)(Math.random() * pref.availableStrings.length)];
 				int size = (int)(pref.textSizeMin + (Math.random() * (pref.textSizeMax - pref.textSizeMin)));
 				int col = Color.argb(155 + (int)(Math.random() * 100), (int)(Math.random() * 255), (int)(Math.random() * 255), (int)(Math.random() * 255));
 				Rect bounds = new Rect();
 				paintText.setTextSize(size);
 				paintText.getTextBounds(text, 0, text.length(), bounds);
 				float x = (float)(Math.random() * getDesiredMinimumWidth());
 				TextObject newObj = new TextObject(text, new RectF(x, 0, x + bounds.width(), bounds.height()), size, col);
 				newObj.doCache(paintText, null);
 				textObj.add(newObj);
 				bounds = null;
 			}
 			for(TextObject t : textObj) {
 				// Text Object Logic
 				if(t.velocityX > 0) {
 					t.velocityX -= 0.01;
 					if(t.velocityX < 0)
 						t.velocityX = 0;
 				}
 				if(t.velocityX < 0) {
 					t.velocityX += 0.01;
 					if(t.velocityX > 0)
 						t.velocityX = 0;
 				}
 				t.velocityY += 0.01;
 				t.dimen.offset(t.velocityX, t.velocityY);
 				if(t.dimen.top > getDesiredMinimumHeight() || t.dimen.left > getDesiredMinimumWidth() || t.dimen.right < 0) {
 					t.cachedText.recycle();
 					textObj.remove(t);
 					continue;
 				}
 				if(pref.collisionEnabled) {
 					for(TextObject t2 : textObj) {
 						if(t2 == t)
 							continue;
 						if(t.dimen.intersect(t2.dimen)) {
 							textExplObj.add(new ExplosionParticleGroup(t.dimen.left, t.dimen.top, t.dimen.width(), t.dimen.height(), t.color));
 							textExplObj.add(new ExplosionParticleGroup(t.dimen.left, t.dimen.top, t.dimen.width(), t.dimen.height(), t2.color));
 							t.cachedText.recycle();
 							t2.cachedText.recycle();
 							textObj.remove(t);
 							textObj.remove(t2);
 							break;
 						}
 					}
 				}
 			}
 			for(ExplosionParticleGroup p : textExplObj) {
 				for(int i = 0; i < p.arr.length; i++) {
 					ExplosionParticle p2 = p.arr[i];
 					if(p2.velocityX > 0) {
 						p2.velocityX -= 0.01;
 						if(p2.velocityX < 0)
 							p2.velocityX = 0;
 					}
 					if(p2.velocityX < 0) {
 						p2.velocityX += 0.01;
 						if(p2.velocityX > 0)
 							p2.velocityX = 0;
 					}
 					
 					p2.x += p2.velocityX;
 					p2.y += p2.velocityY += 0.01;
 				}
 				p.alpha -= (int)(Math.random() * 12);
 				if(p.alpha <= 0)
 					textExplObj.remove(p);
 			}
 			paintHandler.removeCallbacks(paintRunnable);
 			if(visible)
 				paintHandler.post(paintRunnable);
 			// System.out.println("Finished logic in " + (System.currentTimeMillis() - startTime) + "ms.");
 		}
 		
 		private void draw() {
 			// long startTime = System.currentTimeMillis();
 			final SurfaceHolder holder = getSurfaceHolder();
 			Canvas canvas = null;
 			try {
 				canvas = holder.lockCanvas();
 				if(canvas != null) {
 					canvas.drawColor(Color.DKGRAY);
 					if(pref.backgroundImageEnabled && pref.backgroundImage != null)
 						canvas.drawBitmap(pref.backgroundImage, 0, 0, paintBackground);
 					for(ExplosionParticleGroup p : textExplObj) {
 						paintRect.setColor(p.color);
 						paintRect.setAlpha(p.alpha);
 						for(int i = 0; i < p.arr.length; i++) {
 							ExplosionParticle p2 = p.arr[i];
 							if(pref.applyShadow) {
 								paintRectShadow.setColor(Color.argb(p.alpha, 0, 0, 0));
 								canvas.drawRect(p2.x + 2, p2.y + 2, p2.x + p2.size + 2, p2.y + p2.size + 2, paintRectShadow);
 							}
 							canvas.drawRect(p2.x, p2.y, p2.x + p2.size, p2.y + p2.size, paintRect);
 						}
 					}
 					for(TextObject t : textObj) {
						paintText.setColor(t.color);
						if(t.cachedText == null || t.cachedText.isRecycled())
							t.doCache(paintText, canvas);
						else if(t.cachedText != null) {
 							canvas.drawBitmap(t.cachedText, t.dimen.left, t.dimen.top, paintText);
 						}
 					}
 					
 					if(!hasShadow && pref.applyShadow) {
 						paintText.setShadowLayer(1, 2, 2, Color.BLACK);
 						hasShadow = true;
 					} else if(hasShadow && !pref.applyShadow) {
 						paintText.setShadowLayer(0, 0, 0, Color.BLACK);
 						hasShadow = false;
 					}
 				}
 			} finally {
 				if(canvas != null)
 					holder.unlockCanvasAndPost(canvas);
 			}
 			// System.out.println("Finished painting in " + (System.currentTimeMillis() - startTime) + "ms."); //Seems to take about 16-20ms on my device (LGP960).
 		}
 	}
 }
