 package com.project.latviankeyboard.extrarow;
 
 
 import android.app.Service;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.RectF;
 import android.graphics.Typeface;
 import android.graphics.Paint.Align;
 import android.graphics.drawable.BitmapDrawable;
 import android.inputmethodservice.Keyboard;
 import android.inputmethodservice.Keyboard.Key;
 import android.inputmethodservice.Keyboard.Row;
 import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
 import android.os.SystemClock;
 import android.os.Vibrator;
 import android.preference.PreferenceManager;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.Display;
 import android.view.Gravity;
 import android.view.MotionEvent;
 import android.view.MotionEvent.PointerCoords;
 import android.view.Surface;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.WindowManager;
 import android.widget.PopupWindow;
 
 
 public class MyKeyboardView extends View implements OnTouchListener{
 
 	Context ctx;
 	Keyboard keyboard;
 	Key[] keys;
 
 	OnKeyboardActionListener keyboardActionListener;
 
 	Vibrator vibrator;
 
 	private static final int NOT_A_KEY = -1;
 
 
 	boolean kbdDrawPending;
 	boolean kbdChanged;
 	Bitmap kbdBuffer;
 	Canvas kbdCanv;
 
 
 
 	PopupWindow popupWin; //used to show secondary keys when 'noSecondaryKeys' is false
 	FloatyView popupContent;
 	
 	
 	PopupWindow previewWin; //used to show button preview when 'noSecondaryKeys' is true
 	Preview previewContent;
 	
 
 	boolean touchingKey = false;
 	int touchingKeyIndex = NOT_A_KEY;
 	long timeTouchedKey = 0;
 
 	Display display;
 	int displayWidth;
 
 	
 	
 	
 	
 	int buttonHeight;
 	int backspaceCounter = 0;
 	int settingsChar = 44;
 	int toSettingsDelay = 1500;
 	
 
 	
 	////////
 	boolean isHapticOn = true;
 	int kbdHeightpercentVert = 40;
 	int kbdHeightpercentHoriz = 70;
 	int waitTime = 200;
 	int backgroundColor;
 	int btnBackgroundColor;
 	int btnBackgroundHoverColor;
 	int btnBorderColor;
 	int btnTextColor;
 	int textSize;
 	int btnPadding;
 	int btnRoundedness;
 	Typeface typeface;
 	int textShadow;
 	int btnShadow;
 
 	boolean showHints;
 	int previewDelay;
 	int previewTextSize;
 	int previewTextColor;
 	int previewBgColor;
 	int previewsYShift;
 	
 	
 	///////////////
 	Paint darkeningPaint;
 	Paint mainTextPaint;
 	Paint btnBgPaint;
 	Paint btnBorderPaint;
 	Paint popCharPaint;
 	
 	Paint previewTextPaint;
 	Paint previewBgPaint;
 	
 	
 	
 	
 	Key keyLongClicked;
 	Runnable keyLongClick = new Runnable(){	//	
 		@Override
 		public void run() {
 			if(MyKeyboardView.this.keyLongClicked.repeatable){
 				vibrate();
				
				if(MyKeyboardView.this.keys[MyKeyboardView.this.touchingKeyIndex].codes[0] == -5){
					MyKeyboardView.this.backspaceCounter++;
				}
				
 				keyboardActionListener.onKey(keyLongClicked.codes[0], null);
 				MyKeyboardView.this.postDelayed(MyKeyboardView.this.keyLongClick, waitTime);
 			}else if(!MyKeyboardView.this.popupWin.isShowing() && keyLongClicked.popupCharacters != null){
 				
 				MyKeyboardView.this.popupContent.setKeys(keyLongClicked.popupCharacters, keyLongClicked.popupResId);
 				
 				int posX = keyLongClicked.x - MyKeyboardView.this.popupWin.getWidth()/2 + keyLongClicked.width/2;
 				int posY = MyKeyboardView.this.getHeight() - keyLongClicked.y/* + MyKeyboardView.this.popupWin.getHeight()*/ + MyKeyboardView.this.popupContent.padding;
 				
 				if(posX <= 0)
 					posX = 5;
 				if(posX + MyKeyboardView.this.popupWin.getWidth() >= MyKeyboardView.this.displayWidth)
 					posX = MyKeyboardView.this.displayWidth - MyKeyboardView.this.popupWin.getWidth() - 5;
 				
 				if(posY + MyKeyboardView.this.popupWin.getHeight() > MyKeyboardView.this.getHeight() + buttonHeight + 3*MyKeyboardView.this.popupContent.padding)
 					posY = MyKeyboardView.this.getHeight() - buttonHeight + MyKeyboardView.this.popupContent.padding;
 				
 				MyKeyboardView.this.popupWin.showAtLocation(MyKeyboardView.this, Gravity.BOTTOM|Gravity.LEFT, posX, posY);
 				MyKeyboardView.this.invalidate();
 				
 				
 				MyKeyboardView.this.popupContent.posOnScreenX = posX;
 				MyKeyboardView.this.popupContent.posOnScreenY = display.getHeight() - posY - MyKeyboardView.this.popupWin.getHeight();
 				
 				
 				
 				int[] loc = new int[2];
 				MyKeyboardView.this.getLocationOnScreen(loc);
 				PointerCoords pc = new PointerCoords();
 				pc.x = keyLongClicked.x + keyLongClicked.width/2;
 				pc.y = keyLongClicked.y + keyLongClicked.height/2 + loc[1];
 				//MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_MOVE, 1, new int[]{0}, new PointerCoords[]{pc}, 0, 1.0f, 1.0f, 0, 0, 4098, 0);
 				Log.d("!","spaam");
 				MotionEvent me = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_MOVE, keyLongClicked.x + keyLongClicked.width/2, keyLongClicked.y + keyLongClicked.height/2 + loc[1], 0);
 				//MyKeyboardView.this.onTouch(MyKeyboardView.this, me);
 				handlePopUpWindowTouch(me);
 				me.recycle();
 				
 			}
 		}
 	};
 	
 	
 	
 	
 	
 	Runnable keyPreview = new Runnable(){	//
 		
 		@Override
 		public void run() {
 			showPreview();
 			
 		}
 	};
 	
 	
 	
 	Runnable toSettings = new Runnable(){	//
 		@Override
 		public void run() {
 			Log.d("!","yoooooooooooo");
 			keyboardActionListener.onKey(167, null);
 		}
 	};
 	
 	
 	
 
 
 	public MyKeyboardView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		//Log.d("!","-----------------------constructor");
 		this.ctx = context;
 		this.setOnTouchListener(this);
 		vibrator = (Vibrator) ctx.getSystemService(Service.VIBRATOR_SERVICE);
 
 		android.view.LayoutInflater inflater = (android.view.LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		
 		WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
 		this.display = wm.getDefaultDisplay();
 		
 		this.displayWidth = display.getWidth();
 		
 		//init popup window and its content
 		popupContent = new FloatyView(context, this, display.getWidth()/10);
 		this.popupWin = new PopupWindow(context);
 		this.popupWin.setContentView(popupContent);
 		this.popupWin.setBackgroundDrawable(new BitmapDrawable(getResources()));
 		this.popupWin.setTouchable(true);
 
 		//
 		
 		this.previewContent = new Preview(context, this);
 		this.previewWin = new PopupWindow(context);
 		this.previewWin.setContentView(this.previewContent);
 		this.previewWin.setBackgroundDrawable(new BitmapDrawable(getResources()));
 		this.previewWin.setTouchable(true);
 		
 	}
 
 /*
 	public void setDefaultValues(){
 		//this is done to make certain that if invalid values are passed, some settings are set
 		this.isHapticOn = true;
 		this.buttonHeight = 70;
 		this.waitTime = 200;
 		this.backgroundColor = Color.argb(255,0,255,0);
 		this.btnBackgroundColor = Color.argb(255,255,255,0);
 		this.btnBackgroundHoverColor = Color.argb(255,200,200,0);
 		this.btnBorderColor = Color.argb(255, 29, 105, 143);
 		this.btnTextColor = Color.argb(255,0,0,255);
 		this.textSize = 25;
 		this.btnPadding = 5;
 		this.btnRoundedness = 8;
 	}
 */	
 	
 	
 	/**
 	 * this method assumes that all passed parameters are correct
 	 * @param isHapticOn
 	 * @param buttonHeight
 	 * @param waitTime
 	 * @param backgroundColor
 	 * @param btnBackgroundColor
 	 * @param btnBackgroundHoverColor
 	 * @param btnBorderColor
 	 * @param btnTextColor
 	 * @param textSize
 	 * @param btnPadding
 	 * @param btnRoundedness
 	 *//*
 	public void setValues(boolean isHapticOn, int kbdHeightpercentVert, int kbdHeightpercentHoriz, int waitTime, int backgroundColor, int btnBackgroundColor, int btnBackgroundHoverColor, int btnBorderColor, int btnTextColor, int textSize, int btnPadding, int btnRoundedness, String typeface){
 		this.isHapticOn = isHapticOn; //vibration
 		this.kbdHeightpercentVert = kbdHeightpercentVert;
 		this.kbdHeightpercentHoriz = kbdHeightpercentHoriz;
 		this.waitTime = waitTime; //wait time for the pop up window and time for backspace
 		this.backgroundColor = backgroundColor; //main background where the buttons are
 		this.btnBackgroundColor = btnBackgroundColor; //button background color
 		this.btnBackgroundHoverColor = btnBackgroundHoverColor; //color when holding button
 		this.btnBorderColor = btnBorderColor; //when holding a button the border color
 		this.btnTextColor = btnTextColor; //button text color
 		this.textSize = textSize; //letter size
 		this.btnPadding = btnPadding; //space between buttons
 		this.btnRoundedness = btnRoundedness; //roundness of buttons
 		
 		this.buttonHeight = 70;
 		
 		this.typeface = Typeface.createFromAsset( ctx.getAssets(), typeface);
 		
 		this.initPaints();
 	}
 	*/
 	
 	
 	
 	
 	private void setValues() {
 		SharedPreferences prefs  = PreferenceManager.getDefaultSharedPreferences(ctx);
 
 		isHapticOn = prefs.getBoolean("erIsHapticOn", true);
 		this.kbdHeightpercentVert = prefs.getInt("erVerticalHeight", 50);
 		this.kbdHeightpercentHoriz = prefs.getInt("erHorizontalHeight", 60);
 		
 		String textFont = prefs.getString("erTextFont", "fonts/Xolonium-Regular.otf");
 		this.typeface = Typeface.createFromAsset( ctx.getAssets(), textFont);
 		
 		this.waitTime = prefs.getInt("erWaitTime", 200);
 		this.backgroundColor = prefs.getInt("erBackgroundColor", Color.argb(255, 30, 30, 30));
 		this.btnBackgroundColor = prefs.getInt("erBtnBackground", Color.argb(255,60,60,60));
 		this.btnBackgroundHoverColor = prefs.getInt("erBtnHoverColor", Color.argb(255,80,80,80));
 		this.btnBorderColor = prefs.getInt("erBtnBorderColor", Color.argb(255,51,181,229));
 		this.btnTextColor = prefs.getInt("erBtnTextColor", Color.argb(255,255,255,255));
 		this.textSize = prefs.getInt("erTextSize", 25);
 		this.btnPadding = prefs.getInt("erBtnPadding", 1);
 		this.btnRoundedness = prefs.getInt("erBtnRoundness", 8);
 		this.textShadow = prefs.getInt("erTextShadow", Color.argb(135, 0, 0, 0));
 		this.btnShadow = prefs.getInt("erBtnShadow", Color.argb(170, 0, 0, 0));
 		
 		
 		
 		//preview
 		this.showHints = prefs.getBoolean("erHints", true); 
 		this.previewTextSize = prefs.getInt("erHintTextSize", 80); //""
 		this.previewDelay = prefs.getInt("erHintDelay", 0);
 		this.previewTextColor = prefs.getInt("erHintTextColor", Color.argb(255, 255, 255, 255));
 		this.previewBgColor = prefs.getInt("erHintBackground", Color.GRAY);
 		this.previewsYShift = prefs.getInt("erHintElevation", 20);
 		
		this.previewContent.inited = false;
		
 		this.initPaints();
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 
 	@Override
 	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
 		kbdDrawPending = true;
 		WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
 		Display display = wm.getDefaultDisplay();
 
 		this.displayWidth = display.getWidth();		
 		
 		if (keyboard == null) {
 			setMeasuredDimension(display.getWidth(), 100);
 		} else {
 			Key k = this.keys[this.keys.length-1];
 			setMeasuredDimension(display.getWidth(), k.y+k.height);
 		}
 
 		
 	}
 
 
 
 
 
 	@Override
 	protected void onDetachedFromWindow() {
 		super.onDetachedFromWindow();
 		if(this.popupWin.isShowing())
 			this.popupWin.dismiss();
 	}
 
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	private void initPaints(){
 		//Typeface tf = Typeface.createFromAsset( ctx.getAssets(), "fonts/GrandHotel-Regular.otf");
 		//Typeface tf = Typeface.createFromAsset( ctx.getAssets(), "fonts/Xolonium-Regular.otf");
 		//Typeface tf = Typeface.createFromAsset( ctx.getAssets(), "fonts/Roboto-Regular.ttf");
 		//Typeface tf = Typeface.createFromAsset( ctx.getAssets(), "fonts/Pecita.otf");
 		//Typeface tf = Typeface.createFromAsset( ctx.getAssets(), "fonts/d-puntillas-D-to-tiptoe.ttf");
 		//Typeface tf = Typeface.createFromAsset( ctx.getAssets(), "fonts/Megrim.ttf");
 		//Typeface tf = Typeface.createFromAsset( ctx.getAssets(), "fonts/Jura-Medium.ttf");
 		
 		this.darkeningPaint = new Paint();
 		this.darkeningPaint.setColor(Color.argb(170,0,0,0));
 		
 		this.mainTextPaint = new Paint();
 		this.mainTextPaint.setColor(this.btnTextColor);
 		this.mainTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
 		this.mainTextPaint.setTextSize(this.textSize);
 		//this.mainTextPaint.setTypeface(Typeface.DEFAULT);
 		this.mainTextPaint.setTypeface(this.typeface);
 		
 		this.btnBgPaint = new Paint();
 		
 		this.btnBorderPaint = new Paint();
 		this.btnBorderPaint.setColor(this.btnBorderColor);
 		this.btnBorderPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
 		
 		this.popCharPaint = new Paint();
 		this.popCharPaint.setColor(this.btnTextColor);
 		this.popCharPaint.setAlpha(100);
 		this.popCharPaint.setTextSize(this.textSize/1.5f);
 		//this.popCharPaint.setTypeface(Typeface.DEFAULT);
 		this.popCharPaint.setTypeface(this.typeface);
 		this.popCharPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
 		this.popCharPaint.setTextAlign(Align.LEFT);
 		
 		 
 		this.previewTextPaint = new Paint();
 		this.previewTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
 		this.previewTextPaint.setColor(this.previewTextColor);
 		this.previewTextPaint.setTextSize(previewTextSize);
 		this.previewTextPaint.setTextAlign(Align.CENTER);
 		this.previewTextPaint.setTypeface(this.typeface);
 		
 		
 		this.previewBgPaint = new Paint();
 		this.previewBgPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
 		this.previewBgPaint.setColor(this.previewBgColor);
 		
 		
 	}
 	
 
 	@Override
 	public void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
 		Log.d("!", "onDraw");
 		if (kbdDrawPending || kbdBuffer == null || kbdChanged) {
 			onBufferDraw();
 		}
 		canvas.drawBitmap(kbdBuffer, 0, 0, null);
 
 		if(this.touchingKey && !this.popupWin.isShowing()){
 			drawButton(canvas, keys[this.touchingKeyIndex], true);
 		}
 		if(this.popupWin.isShowing()/* && noSecondaryKeys == false*/){
             canvas.drawRect(0, 0, getWidth(), getHeight(), darkeningPaint);
 		}
 	}
 
 	
 
 	private void onBufferDraw() {
 		Log.d("!","redrawing canvas");
 
 		if (kbdBuffer == null || kbdChanged) {
 			if (kbdBuffer == null || kbdChanged && (kbdBuffer.getWidth() != getWidth() || kbdBuffer.getHeight() != getHeight())) {
 				//make certain the bitmap is at least 1x1
 				final int width = Math.max(1, getWidth());
 				final int height = Math.max(1, getHeight());
 				kbdBuffer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
 				kbdCanv = new Canvas(kbdBuffer);
 			}
 			invalidateAllKeys();
 			kbdChanged = false;
 		}
 
 		kbdChanged = false;
 		final Canvas canvas = kbdCanv;
 
 		if (keyboard == null) return;
 
 		canvas.drawColor(backgroundColor);
 
 		for (int i = 0; i < keys.length; i++) {
 			final Key key = keys[i];
 			drawButton(canvas, key, false);
 		}
 
 		kbdDrawPending = false;
 	}
 
 
 
 	public void drawButton(Canvas canv, Key k, boolean border){
 		
 		if(border){
 			btnBgPaint.setColor(btnBackgroundHoverColor);
 		}else{
 			btnBgPaint.setColor(btnBackgroundColor);
 		}
 		btnBgPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
 
 		String label = k.label == null? null : adjustCase(k.label).toString();
 
 		if(border){
 			float borderPadding = btnPadding/2.5f;
 			if(borderPadding <= 1)
 				borderPadding = -1;
 			canv.drawRoundRect(new RectF(k.x + borderPadding, k.y + borderPadding, k.x + k.width - borderPadding, k.y + k.height - borderPadding), btnRoundedness+2, btnRoundedness+2, btnBorderPaint);
 		}
 		
 		if(!border && this.btnPadding >1)
 			btnBgPaint.setShadowLayer(this.btnPadding, 0, 0, this.btnShadow);
 		
 		canv.drawRoundRect(new RectF(k.x + btnPadding, k.y + btnPadding, k.x + k.width - btnPadding, k.y + k.height - btnPadding), btnRoundedness, btnRoundedness, btnBgPaint);
 		btnBgPaint.setShadowLayer(0, 0, 0, 0);
 
 		
 		//draw popup character hint if noSecondaryKeys==false
 		if(!showHints){
 			if(k.popupCharacters != null && k.popupCharacters.length() > 1){
 				canv.drawText("...", k.x + btnPadding + btnRoundedness/3 +3 , k.y + popCharPaint.getTextSize() + btnPadding/2, popCharPaint);
 			}else if(k.popupCharacters != null && k.popupCharacters.length() == 1){
 				canv.drawText(k.popupCharacters.toString(), k.x + btnPadding + btnRoundedness/3 + 3, k.y + popCharPaint.getTextSize() + btnPadding + btnRoundedness/3 +3, popCharPaint);
 			}
 		}
 		
 		
 		if (label != null) { //draw label
 			mainTextPaint.setShadowLayer(7, 0, 0, this.textShadow);
 			canv.drawText(label,(k.width) / 2 + k.x - (mainTextPaint.measureText(label)/2), (k.height) / 2 + (mainTextPaint.getTextSize() - mainTextPaint.descent()/2) / 2 + k.y, mainTextPaint);
 			mainTextPaint.setShadowLayer(0, 0, 0, 0);
 		} else if (k.icon != null) { //draw icon
 			/*
         	final int drawableX = (key.width - padding.left - padding.right 
                             - key.icon.getIntrinsicWidth()) / 2 + padding.left;
             final int drawableY = (key.height - padding.top - padding.bottom 
                     - key.icon.getIntrinsicHeight()) / 2 + padding.top;
             canvas.translate(drawableX, drawableY);
             key.icon.setBounds(0, 0, 
                     key.icon.getIntrinsicWidth(), key.icon.getIntrinsicHeight());
             key.icon.draw(canvas);
             canvas.translate(-drawableX, -drawableY);
 			 */
 		}
 		
 	}
 
 
 
 	private CharSequence adjustCase(CharSequence label) {
 		if (keyboard.isShifted() && label != null && label.length() < 3 && Character.isLowerCase(label.charAt(0))) {
 			label = label.toString().toUpperCase();
 		}
 		return label;
 	}
 	
 	
 
 	public void closing() {
 		if (this.popupWin.isShowing()) {
 		    this.popupWin.dismiss();
 		}
 		kbdBuffer = null;
 		kbdCanv = null;
 	}
 	
 	
 
 	public void invalidateAllKeys() {
 		kbdDrawPending = true;
 		invalidate();
 	}
 
 
 
 	private int getKeysIndex(int x, int y) {
 		int primaryIndex = NOT_A_KEY;
 		
 		for(int i=0; i<this.keys.length; i++){
 			if (this.keys[i].isInside(x,y)) {
 				primaryIndex = i;
 				break;
 			}
 		}
 		
 		/*
 		int[] nearestKeyIndices = keyboard.getNearestKeys(x, y);
 		Log.d("!", "--------------------------------------------");
 		for (int i = 0; i < nearestKeyIndices.length; i++) {
 			final Key key = keys[nearestKeyIndices[i]];
 			Log.d("!", "---- key.label: "+key.label);
 			if (key.isInside(x,y)) {
 				primaryIndex = nearestKeyIndices[i];
 				break;
 			}
 		}
 		*/
 		return primaryIndex;
 	}
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 	@Override
 	public boolean onTouch(View v, MotionEvent me) {
 		//Log.d("!","touch x"+me.getX(0)+" y:"+me.getY(0)+" view_id: "+v.getId());
 		
 		/*Log.d("!","metastate:"+me.getMetaState()+
 				"\tx precision:"+me.getXPrecision()+
 				"\tedge flags: "+me.getEdgeFlags()+
 				"\tsource"+me.getSource()+
 				"\tflags:"+me.getFlags());
 		*/
 		
 		if(this.popupWin.isShowing()/* && noSecondaryKeys == false*/){
 			//this.popupContent.dispatchTouchEvent(me);
 			handlePopUpWindowTouch(me);
 			return true;
 		}
 		
 		//Log.d("!","handlepopupwindowtouch x"+me.getX(0)+" y:"+me.getY(0)+" rawX:"+me.getRawX()+" rawY:"+me.getRawY());
 		
 
 		if (me.getActionMasked() == MotionEvent.ACTION_DOWN){
 			int index = getKeysIndex(Math.round(me.getX(0)), Math.round(me.getY(0)));
 			if(index != NOT_A_KEY){
 				vibrate();
 
 				this.touchingKey = true;
 				this.touchingKeyIndex = index;
 				this.timeTouchedKey = me.getEventTime();
 				
 				
 				if(!showHints || this.keys[index].repeatable == true){
 					this.keyLongClicked = this.keys[index];
 					this.postDelayed(this.keyLongClick, waitTime);
 				}
 
 				if(showHints && this.previewDelay > 0){
 					this.postDelayed(this.keyPreview, this.previewDelay);
 				}else if(showHints && this.previewDelay == 0){
 					this.showPreview();
 				}
 				
 				
 				if(showHints && this.keys[this.touchingKeyIndex].codes[0] == this.settingsChar){
 					this.postDelayed(this.toSettings, this.toSettingsDelay);
 				}
 				
 				
 				this.invalidate();
 			}
 			return true;
 		}
 
 		if (me.getActionMasked() == MotionEvent.ACTION_MOVE){
 			//Log.d("!","ACTION_MOVE");
 			int index = getKeysIndex(Math.round(me.getX(0)), Math.round(me.getY(0)));
 			if(index != NOT_A_KEY){
 				if(index != this.touchingKeyIndex && /*(*/!this.popupWin.isShowing()/* || this.noSecondaryKeys )*/ ){
 					vibrate();
 					
 					if(this.keys[this.touchingKeyIndex].codes[0] == -5){//if moving away form backspace button ... reset its backspaceCounter
 						this.backspaceCounter = 0;
 					}
 					
 					if(showHints && this.keys[this.touchingKeyIndex].codes[0] == this.settingsChar){ //if moving away form comma button (this.settingsChar) AND it is hint mode ... deal with Runnable that was posted to handler 
 						this.removeCallbacks(this.toSettings);
 					}
 					
 					
 					if(showHints){
 						this.previewWin.dismiss();
 					}
 					
 					this.removeCallbacks(this.keyLongClick);
 					this.removeCallbacks(this.keyPreview);
 					
 					
 					this.touchingKeyIndex = index;
 					this.timeTouchedKey = me.getEventTime();
 					
 					
 					if(showHints && this.keys[this.touchingKeyIndex].codes[0] == this.settingsChar){
 						this.postDelayed(this.toSettings, this.toSettingsDelay);
 					}
 					
 					
 					
 					
 					if(!showHints || this.keys[index].repeatable == true){
 						this.keyLongClicked = this.keys[index];
 						this.postDelayed(this.keyLongClick, waitTime);
 					}
 
 					if(showHints && this.previewDelay > 0){
 						this.postDelayed(this.keyPreview, this.previewDelay);
 					}else if(showHints && this.previewDelay == 0){
 						this.showPreview();
 					}
 					
 					this.invalidate();
 				}
 				/*if(index != this.touchingKeyIndex && !this.popupWin.isShowing()){
 					vibrate();
 					
 					this.touchingKeyIndex = index;
 					this.timeTouchedKey = me.getEventTime();
 					
 					this.removeCallbacks(this.keyLongClick);
 					this.keyLongClicked = this.keys[index];
 					this.postDelayed(this.keyLongClick, 200);
 					
 					this.invalidate();
 				}*/ //else if(me.getEventTime() > (this.timeTouchedKey + this.waitTime) && !this.popupWin.isShowing()){
 					
 
 					/*
 					Key k = keys[this.touchingKeyIndex];
 					if(k.popupCharacters != null){ //TODO show popup if there is one
 						vibrate();
 						
 						int[] loc = new int[2];
 						MyKeyboardView.this.getLocationOnScreen(loc);
 						
 						this.popupContent.setKeys(k.popupCharacters, k.popupResId);
 						
 						
 						int posX = k.x - this.popupWin.getWidth()/2 + k.width/2;
 						int posY = k.y - this.popupWin.getHeight()-5;
 						if(posX <= 0)
 							posX = 5;
 						if(posX + this.popupWin.getWidth() >= this.displayWidth)
 							posX = this.displayWidth - this.popupWin.getWidth() - 5;
 						if(posY + this.buttonHeight < 0)
 							posY = - this.buttonHeight;
 						
 						Log.d("!","key.x:"+k.x+" key.y:"+k.y+"\tpopupHeight:"+this.popupWin.getHeight()+" popupWidth:"+this.popupWin.getWidth()+"\tpopupX:"+posX+" popupY:"+posY);
 						
 						this.popupWin.showAtLocation(this, Gravity.NO_GRAVITY, posX, posY);
 						//this.popupWin.showAtLocation(this, Gravity.NO_GRAVITY, k.x, k.y - this.popupWin.getHeight()-5);
 						//Log.d("!","showing popup ... "+this.popupWin.isShowing());
 						this.invalidate();
 					}*/
 					/*else if(k.repeatable){
 						vibrate();
 						this.timeTouchedKey = me.getEventTime();
 						keyboardActionListener.onKey(keys[this.touchingKeyIndex].codes[0], null);
 					}*/
 				//}//else if end
 			}
 			return true;
 		}//ACTION_MOVE
 
 
 		if (me.getActionMasked() == MotionEvent.ACTION_UP){
 			
 			if(showHints){
 				this.previewWin.dismiss();
 			}
 			
 			//if(!noSecondaryKeys){
 				this.removeCallbacks(this.keyLongClick);
 			//}else if(noSecondaryKeys && this.previewDelay > 0){
 				this.removeCallbacks(this.keyPreview);
 			//}
 				
 				
 			/*
 			int index = getKeysIndex(Math.round(me.getX(0)), Math.round(me.getY(0)));
 			if(index != NOT_A_KEY){
 				keyboardActionListener.onKey(keys[index].codes[0], null);
 			}
 			*/
 			if(this.keys[this.touchingKeyIndex].codes[0] == -5){//if moving away form backspace button ... reset its backspaceCounter
 				this.backspaceCounter = 0;
 			}
 			
			
 			if(showHints && this.keys[this.touchingKeyIndex].codes[0] == this.settingsChar){ //if moving away form comma button (this.settingsChar) AND it is hint mode ... deal with Runnable that was posted to handler 
 				this.removeCallbacks(this.toSettings);
 			}
 			
				
			if(this.touchingKeyIndex != NOT_A_KEY){
				keyboardActionListener.onKey(keys[this.touchingKeyIndex].codes[0], null);
			}	
			
			
 			this.touchingKey = false;
 			this.touchingKeyIndex = NOT_A_KEY;
 			this.timeTouchedKey = 0;
 
 			if (this.popupWin.isShowing())
 				this.popupWin.dismiss();
 			
 			this.invalidate();
 			return true;
 		}
 
 
 		return true;
 	}
 
 
 
 
 
 
 
 
 
 
 
 
 
 	protected void handlePopUpWindowTouch(MotionEvent me){
 		//int[] loc = new int[2];
 		//this.popupContent.getLocationOnScreen(loc);
 		//Log.d("!", "handlePopUpWindowTouch block x:"+loc[0]+" y:"+loc[1]);
 		
 		//Log.d("!","handlepopupwindowtouch x"+me.getX(0)+" y:"+me.getY(0)+" rawX:"+me.getRawX()+" rawY:"+me.getRawY());
 		
 		if (me.getActionMasked() == MotionEvent.ACTION_MOVE){
 			//Log.d("!","ACTION_MOVE");
 			
 			//int dX = (int) me.getRawX() - loc[0] - this.popupContent.padding;
 			//int dY = (int) me.getRawY() - loc[1] - buttonHeight - this.popupContent.padding;
 			
 			int dX = (int) me.getRawX() - this.popupContent.posOnScreenX - this.popupContent.padding;
 			int dY = (int) me.getRawY() - this.popupContent.posOnScreenY - buttonHeight - this.popupContent.padding;
 
 			//Log.d("!","!!!!!!!!DELTA dX: "+dX+" dY:"+dY);
 			if(dX < 0 || dX > this.popupWin.getWidth() - 3*this.popupContent.padding || dY < 0 || dY > this.popupWin.getHeight()){
 				int index = -1;
 				Log.d("!","!!!!!!!!index: "+index);
 
 				if(this.popupContent.selKeyIdx != index){
 					this.popupContent.selKeyIdx = index;
 					this.popupContent.invalidate();
 				}
 			}else{
 				int row = dY / buttonHeight;
 				int col = dX / this.popupContent.keyWidth;
 				int index = this.popupContent.keysInCol * row + col;
 				Log.d("!","!!!!!!!!index: "+index);
 				if(this.popupContent.selKeyIdx != index){
 					vibrate();
 					this.popupContent.selKeyIdx = index;
 					this.popupContent.invalidate();
 				}
 			}
 			
 			
 			//Log.d("!","key........ width:"+this.keyboard.getKeys().get(12).width+" height:"+this.keyboard.getKeys().get(12).height);
 			
 		}//ACTION_MOVE
 		
 		
 		if (me.getActionMasked() == MotionEvent.ACTION_UP){
 			//Log.d("!","ACTION_UP");
 			
 			MyKeyboardView.this.touchingKey = false;
 			MyKeyboardView.this.touchingKeyIndex = NOT_A_KEY;
 			MyKeyboardView.this.timeTouchedKey = 0;
 
 			if (MyKeyboardView.this.popupWin.isShowing())
 				MyKeyboardView.this.popupWin.dismiss();
 			
 			if(this.popupContent.selKeyIdx != -1 && this.popupContent.selKeyIdx < this.popupContent.keys.length){
 				keyboardActionListener.onKey(this.popupContent.keys[this.popupContent.selKeyIdx].label.charAt(0), null);
 				this.popupContent.selKeyIdx = -1;
 			}
 			
 			MyKeyboardView.this.invalidate();
 		}//ACTION_UP
 		
 	}
 	
 	
 	
 	
 	
 	
 
 
 
 
 
 
 
 
 
 
 
 
 	public void setKeyboard(Keyboard keyboard) {
 
 		setValues();
 		
 		this.keyboard = keyboard;
 		this.keys = keyboard.getKeys().toArray(new Key[keyboard.getKeys().size()]);
 		
 		
 		
 		//count rows //////////////TODO improve
 		int rows = 0;
 		for(int i=0; i<this.keys.length; i++){
 			if(i==0){
 				rows++;
 				continue;
 			}
 			
 			if(this.keys[i-1].y < this.keys[i].y){
 				rows++;
 				continue;
 			}
 				
 		}
 		
 		//calculate button height
 		if(display.getRotation() == Surface.ROTATION_0 || display.getRotation() == Surface.ROTATION_180){
 			this.buttonHeight = Math.round((int) ( ((float)display.getHeight()) * ((float)this.kbdHeightpercentVert) / 100) / rows);
 		}else{
 			this.buttonHeight = Math.round((int) ( ((float)display.getHeight()) * ((float)this.kbdHeightpercentHoriz) / 100) / rows);
 		}
 		
 		
 		//update key height ... THIS ASSUMES THAT ALL ROWS HAVE THE SAME HEIGHT
 		int curHeight = this.keys[0].height;
 		for(Key k : this.keys){
 			k.height = buttonHeight;
 			k.y = (k.y/curHeight) * buttonHeight;
 		}
 		
 		
 		//manage fail that is INT type of width and pos of keys ....WARNING this fix only manages tiny rounding up
 		int ind = 0;
 		while(ind < this.keys.length){
 			Key curK = this.keys[ind];
 			
 			//if not last key in row - continue
 			if(ind != this.keys.length - 1 && curK.y == this.keys[ind+1].y){
 				ind++;
 				continue;
 			}
 			
 			int overlap = curK.x + curK.width - this.displayWidth;
 			
 			//continue if no fixing is required
 			if(overlap <= 0){
 				ind++;
 				continue;
 			}
 			
 			for(int i=0; i<=overlap; i++){
 				//if overlap was too big and that leads to 'fixing' keys in previous row - don't do that
 				if(ind-(overlap-i) < 0 || this.keys[ind-(overlap-i)].y != curK.y)
 					continue;
 				
 				this.keys[ind-(overlap-i)].x -= i;
 				if(overlap-i != 0)
 					this.keys[ind-(overlap-i)].width -= 1;
 			}
 			
 			ind++;
 		}
 		
 		//init onmeassuer
 		requestLayout();
 		
 		//redraw buffer
 		kbdChanged = true;
 		invalidateAllKeys();
 	}
 	
 	
 	public Keyboard getKeyboard() {
 		return keyboard;
 	}
 
 
 
 
 
 
 
 
 
 	public boolean setShifted(boolean shifted) {
     	if (this.keyboard != null) {
             if (this.keyboard.setShifted(shifted)) {
                 // The whole keyboard probably needs to be redrawn
                 invalidateAllKeys();
                 return true;
             }
             invalidateAllKeys();
         }
         return false;
 	}
 
 	public boolean isShifted() {
 		
     	if (this.keyboard != null) {
             return this.keyboard.isShifted();
         }
 
 		return false;
 	}
 
 
 
 
 
 
 
 	
 	
 	
 	
 
 	
 	
 	/**
 	 * 
 	 */
 	public void showPreview(){
 		if(touchingKeyIndex == NOT_A_KEY)
 			return;
 		
 		Key k = MyKeyboardView.this.keys[touchingKeyIndex];
 		MyKeyboardView.this.previewContent.setPopupLebel(k.label);
 		
 		int posX = k.x - MyKeyboardView.this.previewWin.getWidth()/2 + k.width/2;
 		int posY = MyKeyboardView.this.getHeight() - k.y /*+ MyKeyboardView.this.previewContent.padding*/ + this.previewsYShift;
 		
 		if(posX <= 0)
 			posX = 5;
 		if(posX + MyKeyboardView.this.previewWin.getWidth() >= MyKeyboardView.this.displayWidth)
 			posX = MyKeyboardView.this.displayWidth - MyKeyboardView.this.previewWin.getWidth() - 5;
 
 		MyKeyboardView.this.previewWin.showAtLocation(MyKeyboardView.this, Gravity.BOTTOM|Gravity.LEFT, posX, posY);
 		//MyKeyboardView.this.invalidate();
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 
 
 	/*
     public void setPreviewEnabled(boolean previewEnabled) {
         isPreviewShown = previewEnabled;
     }
     public boolean isPreviewEnabled() {
         return isPreviewShown;
     }
 	 */
 
 
 
 	/////////////////
 	public void setOnKeyboardActionListener(OnKeyboardActionListener listener) {
 		keyboardActionListener = listener;
 	}
 	protected OnKeyboardActionListener getOnKeyboardActionListener() {
 		return keyboardActionListener;
 	}
 
 	
 	private void vibrate(){
 		if(isHapticOn)
 			vibrator.vibrate(25);
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	protected class FloatyView extends View{
 		Context ctx;
 		MyKeyboardView kbdView;
 		Key[] keys;
 		int keyWidth;
 		int keysInCol;
 		int padding = 3;
 		int selKeyIdx = -1;
 		
 		int posOnScreenX;
 		int posOnScreenY;
 		
 		
 		public FloatyView(Context context, MyKeyboardView kbdView, int keyWidth) {
 			super(context);
 			this.ctx = context;
 			this.kbdView = kbdView;
 			this.keyWidth = keyWidth;
 			
 			this.requestLayout();
 		}
 		
 		
 		
 		@Override
 		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
 			if(keys == null){
 				setMeasuredDimension(0, 0);
 			}else{
 				setMeasuredDimension((keysInCol)*keyWidth + 2*padding, ((int) Math.ceil((float)keys.length/keysInCol))*buttonHeight + 2*padding);
 			}
 		}
 
 
 
 		@Override
 		protected void onDraw(Canvas canvas) {
 			super.onDraw(canvas);
 			//Log.d("!", "popupkeyboard draw");
 			Paint bgPaint = new Paint();
 			bgPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
 			bgPaint.setColor(backgroundColor);
 			//bgPaint.setShadowLayer(15, 0, 0, new Color().argb(220, 0, 0, 0));
 			canvas.drawRoundRect(new RectF(0, 0, this.getWidth(), this.getHeight()), btnRoundedness + 3, btnRoundedness + 3, bgPaint);
 			//canvas.drawRoundRect(new RectF(0, 0, this.getWidth(), this.getHeight()), 6, 6, bgPaint);
 			//((keys.length-1)/keysInCol+1)*keyHeight + 2*padding
 			for(int i=0; i<keys.length; i++){
 				if(i == selKeyIdx)
 					kbdView.drawButton(canvas, keys[i], true);
 				else
 					kbdView.drawButton(canvas, keys[i], false);
 			}
 		}
 		
 		
 		
 		public void setKeys(CharSequence keysChars, int kbdRes){
 			//Log.d("!","btn key chars: "+keysChars);
 			this.keys = new Key[keysChars.length()];
 			
 			this.keysInCol = keysChars.length();
 			if(keysInCol > 7){
 				this.keysInCol = (int)  Math.ceil((float)keysChars.length()/2);
 			}
 			
 			Row r = new Row(new Keyboard(ctx, kbdRes));
 			for(int i=0; i<keys.length; i++){
 				keys[i] = new Key(r);
 				keys[i].label = keysChars.subSequence(i, i+1);
 				//Log.d("!","char: "+keysChars.subSequence(i, i+1));
 				//keys[i].label = ""+keysChars.charAt(i);
 				keys[i].x = this.keyWidth * (i%(this.keysInCol)) + this.padding;
 				keys[i].y = buttonHeight * (i/this.keysInCol) + this.padding;
 				keys[i].width = this.keyWidth;
 				keys[i].height = buttonHeight;
 				
 				//Log.d("!","...spam... this.keyHeight: "+this.keyHeight+" keys[i].height: "+keys[i].height);
 			}
 			
 			kbdView.popupWin.setWidth((keysInCol)*keyWidth + 2*padding);
 			kbdView.popupWin.setHeight(  ((int) Math.ceil((float)keys.length/keysInCol))*buttonHeight + 2*padding);
 			
 			this.requestLayout();
 		}
 		
 		
 		
 		
 		
 	}//protected class end
 
 
 	
 	
 	
 	
 	
 	
 	protected class Preview extends View{
 
 		MyKeyboardView kbdView;
 		String label;
 		//Paint bgPaint;
 		//Paint textPaint;
 		int padding;
 		boolean wasLastPreviewAChar;
		boolean inited = false;
 		
 		int width, height;
 		
 		public Preview(Context context, MyKeyboardView kbdView) {
 			super(context);
 			
 			this.kbdView = kbdView;
 			/*
 			textPaint = new Paint();
 			textPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
 			textPaint.setColor(Color.WHITE);
 			textPaint.setTextSize(70);
 			textPaint.setTextAlign(Align.CENTER);
 			textPaint.setTypeface(this.)
 			 */
 			
 			padding = 5;
 			
 		}
 		
 		
 		
 		
 		@Override
 		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
 			//super.onMeasure(widthMeasureSpec, heightMeasureSpec);
 			
 			if(label == null){
 				this.setMeasuredDimension(0, 0);
 			}else{
 				this.setMeasuredDimension( this.width, this.height);
 			}
 		}
 
 
 
 		
 		@Override
 		protected void onDraw(Canvas canvas) {
 			//super.onDraw(canvas);
 			canvas.drawRoundRect(new RectF(0, 0, this.getWidth(), this.getHeight()), btnRoundedness + 3, btnRoundedness + 3, previewBgPaint);
 			canvas.drawText(label, this.getWidth()/2, this.getHeight() - 2*previewTextPaint.descent(), previewTextPaint);
 		}
 
 
 
 
 		public void setPopupLebel(CharSequence label){
 			//Log.d("!","setPopupLebel");
 			
 			
 			this.label = kbdView.adjustCase(label).toString();
 			/*
 			if(kbdView.isShifted()){
 				this.label = label.toUpperCase();
 			}else{
 				this.label = label;
 			}
 			*/
 			
 			boolean isLableChar;
 			
 			
 			
 			if(label.length() == 1)
 				isLableChar = true;
 			else
 				isLableChar = false;
 			
 			
			if(wasLastPreviewAChar && isLableChar && inited){
 				return;
 			}else{
				inited = true;
 				if(label.length() == 1){
 					this.width = (int) previewTextPaint.measureText("W") + 2*padding;
 					wasLastPreviewAChar = true;
 				}else{
 					this.width = (int) (previewTextPaint.measureText(this.label)*1.1f + 2*padding);
 					wasLastPreviewAChar = false;
 				}
 				this.height = (int) (previewTextPaint.getTextSize()*1.4f + 2*padding);
 				
 				kbdView.previewWin.setWidth(this.width);
 				kbdView.previewWin.setHeight(this.height);
 				
 				this.requestLayout();
 			}
 			
 			
 			
 		}
 		
 		
 		//public void init(){
 		//	this.requestLayout();
 		//}
 		
 	}//protected class end
 
 
 
 
 
 	
 	
 }//class end
