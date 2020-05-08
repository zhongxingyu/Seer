 package net.acuttone.reddimg;
 
 import java.util.List;
 
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 public class LinkRenderer {
 
 	private Paint textPaint;
 
 	private static final int TITLE_SIDE_MARGIN = 5;
 	private static final int TITLE_TOP = 14;
 	private static final int TEXT_HEIGHT = 16;
 	private static final int TITLE_TO_IMG_MARGIN = 10;
 	
 	public LinkRenderer() {
 		textPaint = new Paint();
 		textPaint.setColor(Color.WHITE);
 		textPaint.setTextSize(14.0f);
 		textPaint.setAntiAlias(true);
 	}
 	
 	public Bitmap render(RedditLink link, Bitmap image) {
 		StringBuilder sb = new StringBuilder();
 		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(RedditApplication.instance());
 		if(sp.getBoolean("showScore", false)) {
 			sb.append("[" + link.getScore() + "] ");
 		}
 		sb.append(link.getTitle());
 		if(sp.getBoolean("showAuthor", false)) {
			sb.append(" | by " + link.getAuthor());
 		}		
 		if(sp.getBoolean("showSubreddit", false)) {
 			sb.append(" in " + link.getSubreddit());
 		}			
 		int width = RedditApplication.instance().getScreenW() - 2 * TITLE_SIDE_MARGIN;
 		List<String> lines = TextWrapper.getWrappedLines(sb.toString(), width, textPaint);		
 		int imgYpos = TITLE_TOP + (lines.size()-1) * TEXT_HEIGHT + TITLE_TO_IMG_MARGIN;
 		Bitmap currentImg = null;
 		try {
 			currentImg = Bitmap.createBitmap(image.getWidth(), image.getHeight() + imgYpos, Bitmap.Config.ARGB_8888);
 		} catch(Exception e) {
 			Log.e(RedditApplication.APP_NAME, e.toString());
 			return null;
 		}
 		Canvas canvas = new Canvas(currentImg);
 		TextWrapper.drawTextLines(canvas, lines, TITLE_SIDE_MARGIN, TITLE_TOP, textPaint);
 		canvas.drawBitmap(image, 0, imgYpos, null);
 		return currentImg;
 	}
 
 }
