 package net.acuttone.reddimg;
 
 import java.util.List;
 
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 public class LinkRenderer {
 
 	private static final int TITLE_SIDE_MARGIN = 5;
 
 	private Paint paint;
 	private Bitmap upvoteBmp;
 	private Bitmap downvoteBmp;
 	
 	public LinkRenderer() {
 		paint = new Paint();
 		paint.setColor(Color.WHITE);		
 		paint.setAntiAlias(true);
 		upvoteBmp = BitmapFactory.decodeResource(RedditApplication.instance().getResources(), R.drawable.upvote);
 		downvoteBmp = BitmapFactory.decodeResource(RedditApplication.instance().getResources(), R.drawable.downvote);	}
 	
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
 		
 		int textSize = Integer.parseInt(sp.getString(PrefsActivity.TITLE_SIZE_KEY, "14"));
 		paint.setTextSize(textSize);
 		int width = RedditApplication.instance().getScreenW() - 2 * TITLE_SIDE_MARGIN;
 		List<String> lines = TextWrapper.getWrappedLines(sb.toString(), width, paint);		
		int imgYpos = textSize + (lines.size()-1) * textSize + textSize / 2;
 		Bitmap currentImg = null;
 		try {
 			currentImg = Bitmap.createBitmap(image.getWidth(), image.getHeight() + imgYpos, Bitmap.Config.ARGB_8888);
 		} catch(Exception e) {
 			Log.e(RedditApplication.APP_NAME, e.toString());
 			return null;
 		}
 		Canvas canvas = new Canvas(currentImg);
 		TextWrapper.drawTextLines(canvas, lines, TITLE_SIDE_MARGIN, textSize, paint);
 		canvas.drawBitmap(image, 0, imgYpos, null);
 		if(RedditClient.UPVOTE.equals(link.getVoteStatus())) {
 			canvas.drawBitmap(upvoteBmp, image.getWidth() - upvoteBmp.getWidth(), imgYpos, paint);
 		} else if(RedditClient.DOWNVOTE.equals(link.getVoteStatus())) {
 			canvas.drawBitmap(downvoteBmp, image.getWidth() - downvoteBmp.getWidth(), imgYpos, paint);
 		}
 		return currentImg;
 	}
 
 }
