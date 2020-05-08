 package ca.idrc.tagin.cloud.tag;
 
 /**
  * Komodo Lab: Tagin! Project: 3D Tag Cloud
  * Google Summer of Code 2011
  * @authors Reza Shiftehfar, Sara Khosravinasr and Jorge Silva
  */
 
 import java.util.Map;
 
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.net.Uri;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 public class TagCloudView extends RelativeLayout {
 	
 	private final float TOUCH_SCALE_FACTOR = .8f;
 	private final float TRACKBALL_SCALE_FACTOR = 10;
 	private final int TEXT_SIZE_MIN = 4;
 	private final int TEXT_SIZE_MAX = 34;
 	private final int SCROLL_SPEED = 1;
 	
 	private float mScrollSpeed;
 	private float mCenterX, mCenterY;
 	private float mRadius;
 	private int mShiftLeft;
 	
 	private Context mContext;
 	private TagCloud mTagCloud;
 	
 	public TagCloudView(Context context, int width, int height, Map<String,Tag> tags) {
 
 		super(context);
 		setFocusableInTouchMode(true);
 		mContext = context;
 		
 		mScrollSpeed = SCROLL_SPEED;
        
 		//set the center of the sphere on center of our screen:
 		mCenterX = width / 2;
 		mCenterY = height / 2;
 		mRadius = Math.min(mCenterX * 0.95f , mCenterY * 0.95f); //use 95% of screen
 		//since we set tag margins from left of screen, we shift the whole tags to left so that
 		//it looks more realistic and symmetric relative to center of screen in X direction
 		mShiftLeft = (int) (Math.min(mCenterX * 0.15f , mCenterY * 0.15f));
 		
 		// initialize the TagCloud from a list of tags
 		mTagCloud = new TagCloud(tags, (int) mRadius, TEXT_SIZE_MIN, TEXT_SIZE_MAX);
 		int color1 = Color.argb(1, 240, 196, 51);
 		int color2 = Color.argb(1, 255, 0, 0);
 		mTagCloud.setTagColor1(color1); // higher color
 		mTagCloud.setTagColor2(color2); // lower color
 		mTagCloud.setRadius((int) mRadius);
 		mTagCloud.create(); // to put each Tag at its correct initial location
 
 
     	// Update the transparency/scale of tags
     	mTagCloud.setAngleX(0);
     	mTagCloud.setAngleY(0);
     	mTagCloud.update();
 		
 		// Now Draw the 3D objects
     	for (Tag tag : mTagCloud.getTags().values()) {
     		initializeTag(tag);
     	}
 	}
 	
 	private void initializeTag(Tag tag) {
 		TextView textView = new TextView(mContext);
 		tag.setTextView(textView);
 		textView.setText(tag.getText());
 		
 		RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(
 				LayoutParams.WRAP_CONTENT,  
 				LayoutParams.WRAP_CONTENT);
 		param.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
 		param.addRule(RelativeLayout.ALIGN_PARENT_TOP);
 		param.setMargins((int) (mCenterX - mShiftLeft + tag.getX2D()), 
 						 (int) (mCenterY + tag.getY2D()), 0, 0);
 		
 		tag.getTextView().setLayoutParams(param);
 		tag.getTextView().setSingleLine(true);
 		tag.getTextView().setTextColor(tag.getColor());
 		tag.getTextView().setTextSize((int)(tag.getTextSize() * tag.getScale()));
 		tag.getTextView().setOnClickListener(onTagClickListener(tag.getUrl()));	
 		addView(tag.getTextView());
 	}
 	
 	private void updateView(Tag tag) {
 		TextView view = tag.getTextView();
 		((RelativeLayout.LayoutParams) view.getLayoutParams())
 			.setMargins((int) (mCenterX - mShiftLeft + tag.getX2D()),
 						(int) (mCenterY + tag.getY2D()), 0, 0);
 		view.setTextSize((int) (tag.getTextSize() * tag.getScale()));
 		view.setTextColor(tag.getColor());
 		view.bringToFront();
 	}
 	
 	public void addTag(Tag tag) {
		initializeTag(tag);
		mTagCloud.add(tag);
		updateView(tag);
 	}
 	
 	public void setTagRGBT(Tag tag) {
 		mTagCloud.setTagRGBT(tag);
 	}
 	
 	@Override
 	public boolean onTrackballEvent(MotionEvent e) {
     	updateAngles(e.getY(), -e.getX(), TRACKBALL_SCALE_FACTOR);
 		return true;
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent e) {
 		float x = e.getX();
 		float y = e.getY();
 		
 		switch (e.getAction()) {
 		case MotionEvent.ACTION_MOVE:	
 			// Rotate elements depending on how far the selection point is from center of cloud
 			float dx = x - mCenterX;
 			float dy = y - mCenterY;
 	    	updateAngles(dy / mRadius, -dx / mRadius, TOUCH_SCALE_FACTOR);
 			
 			break;
 		/*case MotionEvent.ACTION_UP:  //now it is clicked!!!!		
 			dx = x - centerX;
 			dy = y - centerY;			
 			break;*/
 		}
 		
 		return true;
 	}
 	
 	private void updateAngles(float x, float y, float scaleFactor) {
 		float angleX = x * mScrollSpeed * scaleFactor;
 		float angleY = y * mScrollSpeed * scaleFactor;
 		
 		mTagCloud.setAngleX(angleX);
 		mTagCloud.setAngleY(angleY);
     	mTagCloud.update();
     	
     	for (Tag tag : mTagCloud.getTags().values()) {
     		updateView(tag);
     	}
 	}
 	
 	private String makeUrl(String url) {
 		if 	((url.substring(0,7).equalsIgnoreCase("http://")) 	|| 
 			 (url.substring(0,8).equalsIgnoreCase("https://")))
 			return url;
 		else
 			return "http://" + url;
 	}
 	
 	//for handling the click on the tags
 	//onclick open the tag url in a new window. Back button will bring you back to TagCloud
 	private View.OnClickListener onTagClickListener(final String url) {
 		return new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				//we now have url from main code
 				Uri uri = Uri.parse(makeUrl(url));
 				//just open a new intent and set the content to search for the url
 				mContext.startActivity(new Intent(Intent.ACTION_VIEW, uri));				
 			}
 		};
 	}
 }
