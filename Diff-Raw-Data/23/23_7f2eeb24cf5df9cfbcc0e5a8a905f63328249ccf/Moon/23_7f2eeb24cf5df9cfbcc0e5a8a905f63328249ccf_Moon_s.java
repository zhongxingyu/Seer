 package timforce.ghosts;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Point;
 import android.graphics.RectF;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.util.Log;
 import android.view.MotionEvent;
 
 
 public class Moon {
 	private final static String TAG="Moon";
 	private static MediaPlayer mPlayer = new MediaPlayer();
 	private static final int moonWidth = 50;
 	private static final int moonHeight = 50;
 	private static int moonX = 300;
 	private static int moonY = 5;
 	private static final int startMoonShadowOffsetX= 5;	
 	private static final int startMoonShadowOffsetY = -5;
 	private static int moonShadowOffsetX = startMoonShadowOffsetX;
 	private static int moonShadowOffsetY = startMoonShadowOffsetY;
 	private static final Paint moonPaint       = new Paint();
 	private static final Paint moonShadowPaint = new Paint();
 	private static final RectF moonRectF       = new RectF();
 	private static final RectF moonShadowRectF = new RectF();
 	private static final int moonColor = Color.argb(255, 255, 255, 100);
 	private static final int rightPadding = moonWidth + 5;
 	private static int bgColor = 0;
 	private static int moonPopCounter = 0;
 	private static boolean isPopped = false;
 	private static Context context = null;
 	
 	
 	public Moon(int screenWidth, int bgColor, Context context) {
 		// this.moonX = screenWidth - moonWidth - 50;  TODO: remove
 		this.bgColor = bgColor;
 		moonPaint.setColor(moonColor);
 		moonPaint.setAntiAlias(true);
 		moonShadowPaint.setColor(bgColor);
 		moonShadowPaint.setAntiAlias(true);
 		moonPopCounter = 0;
 		this.isPopped = false;
 		this.context = context;
 	}
 	
 	
 	public void draw(Canvas canvas) {
 		int canvasWidth = canvas.getWidth();
 		moonX = canvasWidth - rightPadding;
 		
 		if(moonPopCounter == 0) {
 			// draw moon, not popped yet
 			// draw moon
 			moonRectF.set(moonX, moonY, (moonX + moonWidth), (moonY + moonHeight));
 			canvas.drawArc(moonRectF, 0f, 360f, true, moonPaint);
 		
 			// draw moon shadow
 			moonShadowRectF.set(moonX + moonShadowOffsetX, moonY + moonShadowOffsetY, (moonX + moonWidth + moonShadowOffsetX), (moonY + moonHeight + moonShadowOffsetY));
 			canvas.drawArc(moonShadowRectF, 0, 360f, true, moonShadowPaint);		
 		} else if(moonPopCounter < 30) {
 			// moon is being popped
 			moonRectF.set(moonX - (moonPopCounter*20), moonY, (moonX + moonWidth + (moonPopCounter*20)), (moonY + moonHeight + (moonPopCounter*20)));
 			canvas.drawArc(moonRectF, 0f, 360f, true, moonPaint);			
 		} else {
 			// do not draw if popped
 		}
 
 	}
 	
 	
 	public void update() {
 		if(!getIsPopped()) {
 			if(moonPopCounter > 0) {
 				moonPopCounter++;
 			}
 			
 			if(moonPopCounter == 30) {
 				setIsPopped(true);
 				playSoundWhenPop();
 			}
 		}
 		
 	}
 	
 	
 	
 	// sound for smaller objects, such as background eyes
 	//
 	// Sound from sound bible: http://soundbible.com/1151-Grenade.html
 	//   License: Attribution 3.0
 	//   Recorded by Mike Koenig
 	//
 	// Sound from sound bible: http://soundbible.com/1986-Bomb-Exploding.html
 	//   License: Attribution 3.0
 	//   Recorded by Sound Explorer
 	private void playSoundWhenPop() {
 		if(mPlayer != null) {
 			mPlayer.release();
 		}
 		
 		// mPlayer = MediaPlayer.create(context, R.raw.gren_sound);
		mPlayer = MediaPlayer.create(context, R.raw.bmb_expl);
 		((GameEngine) context).setVolumeControlStream(AudioManager.STREAM_MUSIC);
 		// mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
 		
 		try {
			// mPlayer.reset();   mPlayer.prepare();
 			mPlayer.start();
			// mPlayer.release();
 		} catch(Exception e) {
 			Log.e(TAG, "ERROR tring to play moon pop - message=" + e.getMessage());
 		}
 
 	}
  		
 	
 	
 	public void handleClick(MotionEvent e) {
 		Log.d(TAG, "Moon - handling click ...");
 		if( (moonX < e.getX()) && ((moonX + moonWidth) > e.getX()) ) {
 			if( (moonY < e.getY()) && ((moonY + moonHeight) > e.getY()) ) {
 				moonPopCounter = 1;
 			}
 		}
 		
 	}
 
 	
 	public boolean getIsPopped() { return this.isPopped; }
 	
 	public void setIsPopped(boolean isPopped) { this.isPopped = isPopped; }
 
 }
 
