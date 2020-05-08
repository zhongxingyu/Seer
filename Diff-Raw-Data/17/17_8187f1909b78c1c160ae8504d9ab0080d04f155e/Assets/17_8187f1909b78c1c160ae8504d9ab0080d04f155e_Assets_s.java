 package no.jts.android.simple.gameapi.example.setup;
 
 import no.jts.android.simple.gameapi.example.R;
 import no.jts.android.simple.gameapi.graphics.Button;
 import no.jts.android.simple.gameapi.graphics.Sprite;
 import no.jts.android.simple.gameapi.graphics.SpriteUtil;
 import no.jts.android.simple.gameapi.setup.Globals;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Typeface;
 
 public class Assets {
 	
 	private static final float GESTURE_THRESHOLD_DIP = 22.0f;
 	
 	public static Paint paint;
 	public static Sprite background;
 	public static Button buttonNewGame;
	
	
 	public static void init(){
 		paint = createPaint();
 		background = SpriteUtil.createSprite(R.drawable.background);
 		buttonNewGame = SpriteUtil.createButton(R.drawable.button_new_game);
 	}
 	
 	public static Paint createPaint(){
 		final float scale = Globals.resources.getDisplayMetrics().density;
 		int gestureThreshold = (int) (GESTURE_THRESHOLD_DIP * scale + 0.5f);
 		Paint paint = new Paint();
 		paint.setColor(Color.BLACK); 
 		paint.setTypeface(Typeface.DEFAULT_BOLD);
 		paint.setTextSize(gestureThreshold);
 		paint.setShadowLayer(1.0f, 1.0f, 1.0f, Color.BLACK);
 		return paint;
 	}
 }
