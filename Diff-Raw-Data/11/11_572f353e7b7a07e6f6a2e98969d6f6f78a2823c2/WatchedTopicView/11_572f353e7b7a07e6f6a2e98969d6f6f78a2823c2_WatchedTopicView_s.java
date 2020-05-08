 package com.ChewieLouie.Topical.View;
 
 import android.graphics.Color;
 import android.widget.TextView;
 
 public class WatchedTopicView implements ViewWatchedTopicIfc {
 
 	private TextView textView = null;
	private int resultColour = Color.BLACK;
 	private boolean activityStarted = false;
 
 	public WatchedTopicView(TextView textView) {
 		this.textView = textView;
 	}
 
 	@Override
 	public void setText( String text ) {
 		textView.setText( text );
 	}
 
 	@Override
 	public void setTopicResultsHaveChanged() {
		if( activityStarted )
			resultColour = Color.MAGENTA;
 		if( activityStarted == false )
 			textView.setBackgroundColor( resultColour );
 	}
 
 	@Override
 	public void setTopicResultsHaveNotChanged() {
 		resultColour = Color.GREEN;
 		if( activityStarted == false )
 			textView.setBackgroundColor( resultColour );
 	}
 
 	@Override
 	public void activityStarted() {
 		activityStarted = true;
		textView.setBackgroundColor( Color.BLACK );
 	}
 
 	@Override
 	public void activityStopped() {
 		textView.setBackgroundColor( resultColour );
 		activityStarted = false;
 	}
 }
