 package com.clarityforandroid.views;
 
 import com.clarityforandroid.R;
 import com.clarityforandroid.models.ProviderModel;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.util.AttributeSet;
 import android.view.View;
 
 /**
  * A view that acts like an info bar at the top of the app's viewport.
  * Displays information about the current user.
  * 
  * @author Jonathan Ballands
  * @version 1.0
  */
 public class CurrentUserView extends View {
 
 	private Paint infoTextPaint;
 	private String infoText;
 	private int infoTextCenterPosition;
 	
 	private Bitmap picture;
 	private Bitmap scaledPicture;
 	private Paint pictureSmoother;
 	
 	private int backgroundColor;
 	
 	private final float SCREEN_DENSITY = this.getResources().getDisplayMetrics().density;
 	
 	/**
 	 * Constructs a new view that displays user info.
 	 * 
 	 * @param context The context of this view.
 	 * @param attrs Any attributes for this view. (For this view, use null. Any argument will be ignored.)
 	 * @param model A ProviderModel that has complete properties pertaining to this view.
 	 */
 	public CurrentUserView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		
 		infoTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 		infoTextPaint.setColor(Color.parseColor("#FFFFFF"));
 		infoTextPaint.setTextSize(26 * this.SCREEN_DENSITY);
 
 		backgroundColor = Color.parseColor("#C4C4C4");
 	}
 	
 	/**
 	 * Initializes this view with a ProviderModel.
 	 * 
 	 * @param model A ProviderModel with data that this view needs.
 	 */
 	public void initializeWithModel(ProviderModel model) {
		infoText = String.format("%s %s", model.firstName(), model.lastName());
 		if (model.photo() != null) {
 			picture = model.photo();
 		}
 		else {
 			picture = BitmapFactory.decodeResource(this.getResources(), R.drawable.no_user_image);
 		}
 		
 		pictureSmoother = new Paint();
 		pictureSmoother.setAntiAlias(true);
 		pictureSmoother.setFilterBitmap(true);
 		pictureSmoother.setDither(true);
 	}
 	
 	/**
 	 * Note: This method is called automatically as Android assesses size requirements for this view.
 	 */
 	@Override
 	protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
         super.onSizeChanged(xNew, yNew, xOld, yOld);
         
         infoTextCenterPosition = (int)((yNew / 2) + ((this.infoTextPaint.getTextSize() / SCREEN_DENSITY) / 2));
         
         scaledPicture = Bitmap.createScaledBitmap(picture, yNew, yNew, true);
     }
 	
 	@Override
 	protected void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 		
 		canvas.drawColor(backgroundColor);
 		canvas.drawBitmap(scaledPicture, 0, 0, pictureSmoother);
 		canvas.drawText(infoText, 200, infoTextCenterPosition, infoTextPaint);
 	}
 	
 }
