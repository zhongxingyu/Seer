 package mobi.monaca.framework.nativeui.component;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Locale;
 
 import mobi.monaca.framework.bootloader.LocalFileBootloader;
 import mobi.monaca.framework.nativeui.UIContext;
 import mobi.monaca.framework.nativeui.UIUtil;
 import mobi.monaca.framework.psedo.R;
 import mobi.monaca.framework.util.MyLog;
 
 import org.apache.commons.io.FileUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.graphics.drawable.AnimationDrawable;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.ColorDrawable;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.TextView;
 
 public class SpinnerDialog extends Dialog {
 
 	private static final String DebuggerTAG = "Monaca";
 	private static final String TAG = SpinnerDialog.class.getSimpleName();
 	
 	public class SpinnerDialogException extends Exception{
 		public SpinnerDialogException(String message) {
 			super(message);
 		}
 	}
 
 	private TextView mTitleView;
 
 	public SpinnerDialog(UIContext context, JSONArray args) throws SpinnerDialogException {
 		super(context.getApplicationContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
 		
 		getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		
 
 		String spinnerImagePath = null;
 		try {
 			spinnerImagePath = args.getString(0);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			throw new SpinnerDialogException(e.getMessage());
 		}
 
 		int numFrames = args.optInt(1, 1);
 		if (numFrames < 1) {
 			throw new SpinnerDialogException("Spinner frames must be greater than 0. You provided: " + numFrames);
 		}
 
 		int interval = args.optInt(2, 100);
 		if(interval < 50){
 			throw new SpinnerDialogException("Spinner interval needs to be greater than 50 milliseconds. You provided: " + interval);
 		}
 		
 		String spinnerBackgroundColor = args.optString(3, "#000000");
 		if(spinnerBackgroundColor.equalsIgnoreCase("null")){
 			spinnerBackgroundColor = "#000000";
 		}
 		
 		float backgroundOpacity = (float) args.optDouble(4, 0.3f);
 		if(backgroundOpacity < 0.0 || backgroundOpacity > 1.0){
 			throw new SpinnerDialogException("Spinner backgroundOpacity value must be in the range 0.0-1.0, you provided: " + backgroundOpacity);
 		}
 		
 		try{
 			ColorDrawable windowDrawable = new ColorDrawable( Color.parseColor(spinnerBackgroundColor));
 			getWindow().setBackgroundDrawable(windowDrawable);
 			windowDrawable.setAlpha(UIUtil.buildOpacity(backgroundOpacity));
 		}catch (IllegalArgumentException e){
 			throw new SpinnerDialogException("Spinner backgroundColor is invalid. You provided: " + spinnerBackgroundColor);
 		}
 		
 		Bitmap fullSpinner = null;
 		if (spinnerImagePath == null || spinnerImagePath.equalsIgnoreCase("null")) {
 			fullSpinner = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.spinner);
 			numFrames = 12;
 		} else {
 			if(!spinnerImagePath.toLowerCase(Locale.ENGLISH).endsWith("png")){
 				throw new SpinnerDialogException("Spinner image is not a PNG format: " + spinnerImagePath);
 			}
 			
 			String fullImagePath = context.resolve(spinnerImagePath);
 			if (fullImagePath.startsWith("file:///android_asset/")) {
 				try {
 					fullSpinner = BitmapFactory.decodeStream(LocalFileBootloader.openAsset(context, fullImagePath));
 				} catch (IOException e) {
 					throw new SpinnerDialogException("Spinner image not found: " + fullImagePath);
 				}
 			} else {
 				File imageFile = new File(fullImagePath);
 				if(imageFile.exists()){
 					fullSpinner = BitmapFactory.decodeFile(fullImagePath);
 				}else{
 					throw new SpinnerDialogException("Spinner image not found: " + fullImagePath);
 				}
 				
 			}
 		}
 
		AnimationDrawable animationDrawable = new AnimationDrawable();
 
 		float frameHeight = (float) fullSpinner.getHeight() / numFrames;
 		int y = 0;
 		for (int i = 0; i < numFrames; i++) {
 			y = Math.round(frameHeight * i);
 			if(y + frameHeight > fullSpinner.getHeight()){
 				frameHeight = fullSpinner.getHeight() - y;
 			}
 			Bitmap frameBitmap = Bitmap.createBitmap(fullSpinner, 0, y, fullSpinner.getWidth(), Math.round(frameHeight));
 			animationDrawable.addFrame(new BitmapDrawable(getContext().getResources(), frameBitmap), interval);
 		}
 
 		
 		LinearLayout spinnerContent = new LinearLayout(context);
 		spinnerContent.setOrientation(LinearLayout.VERTICAL);
 		spinnerContent.setBackgroundColor(Color.TRANSPARENT);
 		
		LinearLayout.LayoutParams imageViewParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 		imageViewParams.gravity = Gravity.CENTER;
 
 		ImageView imageView = new ImageView(context);
 		imageView.setImageDrawable(animationDrawable);
 		imageView.setScaleType(ScaleType.CENTER_INSIDE);
 		spinnerContent.addView(imageView, imageViewParams);
 		
 		String title = args.optString(5);
 		if (title != null && !TextUtils.isEmpty(title) && !title.equals("null")) {
 			mTitleView = new TextView(getContext());
 			mTitleView.setText(title);
 			
 			float titleFontScale = (float) args.optDouble(7, 1.0f);
 			int defaultFontSize = context.getFontSizeFromDip(Component.SPINNER_TEXT_DIP);
 			float titleFontSize = titleFontScale * defaultFontSize;
 			mTitleView.setTextSize(titleFontSize);
 
 			String titleColor = args.optString(6, "#000000");
 			if(titleColor.equalsIgnoreCase("null")){
 				titleColor = "#000000";
 			}
 			
 			try{
 				mTitleView.setTextColor(Color.parseColor(titleColor));
 			}catch (IllegalArgumentException e){
 				throw new SpinnerDialogException("Spinner titleColor is invalid. You provided: " + titleColor);
 			}
 			
 			LinearLayout.LayoutParams titleParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 			titleParams.gravity = Gravity.CENTER;
 			titleParams.topMargin = (int) (titleFontSize * 1.5);
 			spinnerContent.addView(mTitleView, titleParams);
 		}
 
 		FrameLayout.LayoutParams spinnerContentParams = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 		spinnerContentParams.gravity = Gravity.CENTER;
 		setContentView(spinnerContent, spinnerContentParams);
 
		animationDrawable.setOneShot(false);
		animationDrawable.start();
 	}
 	
 	public void updateTitleText(String title){
 		MyLog.v(TAG, "updating title to:" + title);
 		mTitleView.setText(title);
 	}
 
 }
