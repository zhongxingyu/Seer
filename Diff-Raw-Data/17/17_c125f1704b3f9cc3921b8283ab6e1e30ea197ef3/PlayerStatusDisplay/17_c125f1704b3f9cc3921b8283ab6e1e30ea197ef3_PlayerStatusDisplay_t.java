 package com.rachum.amir.skyhiking.android;
 
 import java.util.Arrays;
 import java.util.List;
 
 import com.rachum.amir.skyhiking.Move;
 import com.rachum.amir.skyhiking.android.R;
 import com.rachum.amir.skyhiking.players.Player;
 
 import android.content.Context;
 import android.graphics.Color;
import android.graphics.Typeface;
 import android.text.TextUtils;
 import android.widget.ImageView;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 public class PlayerStatusDisplay extends TableRow {
 	private final TextView playerName;
 	private final TextView playerScore;
 	private final TextView playerCards;
 	private final ImageView statusImage;
 	private final Player player;
 	List<TextView> tviews;
 	Context context;
 	
 	public PlayerStatusDisplay(Context context, Player player) {
 		super(context);
 		this.context = context;
 		this.player = player;
 		playerName = new TextView(context);
 		playerName.setEllipsize(TextUtils.TruncateAt.END);
 		playerName.setSingleLine();
 		playerName.setLayoutParams(
 				new TableRow.LayoutParams(
 						LayoutParams.FILL_PARENT, 
 						LayoutParams.WRAP_CONTENT, 
 						0f
 				)
 		);
 		playerScore = new TextView(context);
 		playerCards = new TextView(context);
 		tviews = Arrays.asList(playerName, playerScore, playerCards);
 		this.setPadding(dpToPx(0, Dimension.WIDTH), 
 					    dpToPx(14, Dimension.HEIGHT), 
 					    dpToPx(0, Dimension.WIDTH), 
 					    dpToPx(0, Dimension.HEIGHT));
 		for (TextView tview : tviews) {
 			tview.setPadding(dpToPx(0, Dimension.WIDTH), 
 							 dpToPx(0, Dimension.HEIGHT), 
 							 dpToPx(14, Dimension.WIDTH), 
 						     dpToPx(0, Dimension.HEIGHT));
 			tview.setTextSize(18);
 			tview.setTextColor(Color.BLACK);
 		}
 		statusImage = new ImageView(context);
 		addView(playerName);
 		addView(playerScore);
 		addView(playerCards);
 		addView(statusImage);
 		setPlaying(true);
 		unsetStatus();
 		updatePlayerInfo();
 	}
 	
 	enum Dimension {
 		HEIGHT, WIDTH;
 	}
 	
 	private int dpToPx(int dp, Dimension dimension) { 
 		// Get the screen's density scale 
 		final float scale = getResources().getDisplayMetrics().density; 
 		// Convert the dp to pixels, based on density scale
 		int px = (int) (dp * scale + 0.5f); 
 		if (dimension != null) {
 			int ref, dim;
 			switch (dimension) {
 			case HEIGHT:
 				dim = getResources().getDisplayMetrics().heightPixels; 
 				ref = 800;
 				break;
 			case WIDTH:
 				dim = getResources().getDisplayMetrics().widthPixels; 
 				ref = 480;
 				break;
 			default:
 				dim = 1;
 				ref = 1;
 			}
 			px = (int) (px * ((float) dim / ref));
 		}
 		
 		return px;
 	}
 	
 	public void setMove(Move move) {
 		statusImage.setVisibility(VISIBLE);
 		switch (move) {
 		case LEAVE:
 			statusImage.setImageResource(R.drawable.leave);
 			break;
 		case STAY:
 			statusImage.setImageResource(R.drawable.stay);
 			break;
 		}
 		statusImage.getLayoutParams().height = dpToPx(20, null);
 		statusImage.getLayoutParams().width = dpToPx(20, null);
 	}
 	
 	public void setPlaying(boolean playing) {
 		for (TextView tview : tviews) {
 			if (playing) {
 				tview.setTextColor(Color.BLACK);
 			} else {
 				tview.setTextColor(Color.GRAY);
 			}
 		}
 	}
 	
 	public void setWon() {
 		playerName.setTextAppearance(getContext(), R.style.BoldText);
 		playerName.setShadowLayer(dpToPx(2, null), dpToPx(0, null), 
 								  dpToPx(0, null), Color.BLACK);
 		playerName.setTextColor(Color.GREEN);
 	}
 	
 	public void setPilot() {
 		statusImage.setVisibility(VISIBLE);
 		statusImage.setImageResource(R.drawable.pilot); //TODO: change to pilot image
 		statusImage.getLayoutParams().height = dpToPx(20, null);
 		statusImage.getLayoutParams().width = dpToPx(20, null);
		for (TextView tview : tviews) {
			tview.setTypeface(null, Typeface.BOLD);
		}
 	}
 	
 	public void unsetStatus() {
 		statusImage.setVisibility(INVISIBLE);
 		statusImage.getLayoutParams().height = dpToPx(20, null);
 		statusImage.getLayoutParams().width = dpToPx(20, null);
		for (TextView tview : tviews) {
			tview.setTypeface(null, Typeface.NORMAL);
		}
 	}
 	
 	public void updatePlayerInfo() {
 		playerName.setText(player.getName());
 		playerScore.setText(String.format("%2d Pts", player.getScore()));
 		playerCards.setText(String.format("%2d Cards", player.getHand().getCards().size()));
 	}
 }
