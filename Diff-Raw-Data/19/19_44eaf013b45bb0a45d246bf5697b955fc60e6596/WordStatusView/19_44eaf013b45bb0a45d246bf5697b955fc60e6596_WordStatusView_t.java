 package com.mntnorv.wrdl_holo.views;
 
import com.mntnorv.wrdl_holo.dict.WordChecker;
 
 import android.content.Context;
 import android.util.AttributeSet;
 import android.widget.TextView;
 
 public class WordStatusView extends TextView {
 	
 	private int goodColor;
 	private int guessedColor;
 	private int badColor;
 	private int emptyColor;
 
 	public WordStatusView(Context context) {
 		super(context);
 		initWordStatusView();
 	}
 	
 	public WordStatusView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		initWordStatusView();
 	}
 	
 	private void initWordStatusView() {
 		goodColor = 0xFFA2C139;
 		badColor = 0xFFFF5B4B;
 		guessedColor = 0xFFC8C8C8;
 		emptyColor = 0x22000000;
 	}
 	
	public void setStatus (WordChecker.Result status) {
 		if (status.isGood()) {
 			if (!status.isGuessed()) {
 				this.setBackgroundColor(goodColor);
 			} else {
 				this.setBackgroundColor(guessedColor);
 			}
 			
 			this.setText(Integer.toString(status.getScore()));
 		} else if (status.isBad()) {
 			this.setBackgroundColor(badColor);
 			this.setText("X");
 		} else {
 			this.setBackgroundColor(emptyColor);
 			this.setText("");
 		}
 	}
 }
