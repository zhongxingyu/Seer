 package no.ntnu.tdt4240.views;
 
 import no.ntnu.tdt4240.models.Player;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.util.AttributeSet;
 import android.view.View;
 
 public class PlayerView extends View {
 
 	private Paint paint;
 	private Player player;
 	
 	public PlayerView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		init();
 	}
 
 	private void init() {
 		paint = new Paint();
 		paint.setTextSize(22);
 		paint.setColor(Color.BLUE);
 		paint.setStyle(Paint.Style.FILL);
 	}
 
 	public String getText() {
 		if (player == null)
 			return "Player1";
 		return player.getName();
 	}
 
 	public void setPlayer(Player player) {
 		this.player = player;
 		if (player.getPlayerView() == null)
 			player.setPlayerView(this);
 	}
 	
 	public Player getPlayer() {
 		return player;
 	}
 	
 	@Override
 	protected void onDraw(Canvas canvas) {
		canvas.drawText(getText(), 0, 22, paint);
 	}
 
 }
