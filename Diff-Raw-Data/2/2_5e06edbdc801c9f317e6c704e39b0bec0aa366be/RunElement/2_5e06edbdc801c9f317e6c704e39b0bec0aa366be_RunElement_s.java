 package com.yarakyo.cadebuildorders;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.graphics.LightingColorFilter;
 import android.graphics.Rect;
 import android.graphics.drawable.Drawable;
 import android.os.Handler;
 import android.os.Message;
 import android.util.TypedValue;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 public class RunElement extends RunBuild {
 
 	Action action;
 	TextView textViewRunActionDescription;
 	TextView textViewRunActionTime;
 	ProgressBar progressBar;
 	Context context;
 	int totalTime;
 	boolean expired;
 	LinearLayout elementContainerLayout;
 	LinearLayout ElementLeftLayout;
 	LinearLayout ElementRightLayout;
 	ImageView runElementImage;
 
 	// Getters
 	public TextView getTextViewRunActionDescription()
 	{
 		return this.textViewRunActionDescription;
 	}
 	
 	public TextView getTextViewRunActionTime()
 	{
 		return this.textViewRunActionTime;
 	}
 	
 	public ProgressBar getProgressbar() {
 		return this.progressBar;
 	}
 
 	public LinearLayout getElementcontainerLayout() {
 		return this.elementContainerLayout;
 	}
 
 	public LinearLayout getElementLeftLayout() {
 		return this.ElementLeftLayout;
 	}
 
 	public LinearLayout getElementRightLayout() {
 		return this.ElementRightLayout;
 	}
 	
 	public ImageView getImageView()
 	{
 		return this.runElementImage;
 	}
 	
 	public Action getAction(){
 		return this.action;
 	}
 
 	// Setters
 	public void setProgressBar(ProgressBar progressBar) {
 		this.progressBar = progressBar;
 	}
 
 	public void setProgressBarTime(int currentTime) {
 		this.progressBar.setProgress(currentTime);
 	}
 
 	public void setContainerLayout(LinearLayout elementContainerLayout,
 			LinearLayout ElementLeftLayout, LinearLayout ElementRightLayout) {
 		this.ElementLeftLayout = ElementLeftLayout;
 		this.ElementRightLayout = ElementRightLayout;
 		this.elementContainerLayout = elementContainerLayout;
 	}
 	
 	public void setImageView(ImageView runElementImage) {
 		this.runElementImage = runElementImage;
 	}
 
 	// Methods
 
 	private void changeProgressBarColourToRed() {
 		Drawable drawable = this.progressBar.getProgressDrawable();
 		drawable.setColorFilter(new LightingColorFilter(0xFFff0000, 0xFFff0000));
 	}
 
 	public boolean testTimeExpired(int currentTime) {
 		if (currentTime > this.totalTime) {
 			changeProgressBarColourToRed();
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public boolean testForRemovalTime(int currentTime) {
 		if (currentTime > this.totalTime + 5) {
 			return true;
 		} else {
 
 			return false;
 		}
 
 	}
 
 	RunElement(Action action, Context context) {
 		this.context = context;
 		this.action = action;
 
 		// Initialise all UI Variables	
 		textViewRunActionDescription = new TextView(context);
 		textViewRunActionDescription.setText(action.getActionDescription());
 		textViewRunActionDescription.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20f);
 		textViewRunActionTime = new TextView(context);
		textViewRunActionTime.setText(action.getActionID()+ "m " + action.getSeconds() + "s.");
 		textViewRunActionTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20f);
 		
 		
 		progressBar = new ProgressBar(context, null,
 				android.R.attr.progressBarStyleHorizontal);
 		progressBar.setIndeterminate(false);
 		progressBar.setVisibility(ProgressBar.VISIBLE);
 		progressBar.setProgress(0);
 		int totalTime = 0;
 		totalTime += action.getMinutes() * 60;
 		totalTime += action.getSeconds();
 		this.totalTime = totalTime;
 		progressBar.setMax(totalTime);
 
 	}
 
 	public void resetProgressBar() {
 		int savedTime = progressBar.getMax();
 		progressBar = new ProgressBar(context, null,
 				android.R.attr.progressBarStyleHorizontal);
 		progressBar.setIndeterminate(false);
 		progressBar.setVisibility(ProgressBar.VISIBLE);
 		progressBar.setProgress(0);
 		progressBar.setMax(savedTime);
 		LayoutParams layout = new LayoutParams(LayoutParams.MATCH_PARENT,
 				LayoutParams.WRAP_CONTENT);
 		progressBar.setLayoutParams(layout);
 		this.expired = false;
 	}
 
 
 
 }
