 package com.cadeli.ui;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.os.Handler;
 import android.util.AttributeSet;
 
 public class CdlMessageView extends CdlView {
 	public static int MESSAGETYPE_INFO = 1;
 	public static int MESSAGETYPE_WARNING = 2;
 
 	private Handler handler = new Handler();
 
 	private static final long MESSAGE_DURATION = 500;
 	private static final String TAG = "MessageView";
 
 	protected int timerCountMessage;
 	private String messageString = "";
 	private int messageType;
 	private int progressVal;
 
 	private Runnable runnableMessage = new Runnable() {
 		public void run() {
 			// XmlUtil.myLog(TAG, "timer " + timerCount);
 			timerCountMessage--;
 			if (timerCountMessage <= 0) {
 				messageString = "";
 				CdlUtils.cdlLog(TAG, " display message (end)");
 				invalidate();
 				// invalidate((int) bounds2.left - 8, (int) bounds2.top - 8, (int) bounds2.right + 8, (int) bounds2.bottom + 8);
 			} else {
 				handler.postDelayed(this, MESSAGE_DURATION);
 			}
 		}
 	};
 	private String progressMessage;
 
 	public CdlMessageView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		init2(context);
 	}
 
 	public CdlMessageView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		init2(context);
 	}
 
 	public CdlMessageView(Context context) {
 		super(context);
 		init2(context);
 	}
 
 	private void init2(Context context) {
 	}
 
 	public void displayMessage(String messageString, int messageType) {
 		// CdlUtils.cdlLog(TAG, "displayMessage::" + messageString);
 		this.messageType = messageType;
 		this.messageString = messageString;
 		invalidate();
 		timerCountMessage = 2;
 		runnableMessage.run();
 	}
 
 	public void draw(Canvas canvas) {
 		super.draw(canvas);
 		if (progressVal != 0) {
 			drawProgress(canvas);
 		} else if (messageString.length() > 0) {
 			drawMessage(canvas);
 		}
 	}
 
 	private void drawProgress(Canvas canvas) {
 		String progressValStr = "" + progressVal + " " + "%";
 		Paint txtPaintValue = CdlPalette.getTxtPaint(getWidth() / 6);
 		Paint txtPaintTitle = CdlPalette.getTxtPaint(getWidth() / 6);
 		txtPaintValue.getTextBounds(progressValStr, 0, progressValStr.length(), bounds);
 		int x = (int) (getWidth() / 2 - bounds.centerX());
 		int y = (int) ((getHeight()/2) - bounds.centerY());
 		
 		int round_h = getWidth() / 100;
 		int round_w = getWidth() / 100;
 		int padding = round_h * 3;
 		int dx = (getWidth() / 2 - bounds.centerX());
 		int dy = (int) (getHeight() / 2 - bounds.centerY());
 		int pad = getWidth() / 4;
 		int h_start = bounds.top + dy - padding - bounds.height() / 3;
 		int h_end = bounds.bottom + dy + 2*padding;
 		y+=(h_end-h_start)/4;
 		rectf.set(getLeft() + pad, h_start, getRight() - pad, h_end);
 		urect.set(getLeft() + pad, h_start, getLeft() + pad +  ((getRight() - pad) -(getLeft() + pad))  * progressVal / 100, h_end);
 		canvas.drawRoundRect(rectf, round_w, round_h, CdlPalette.getFlashPaint());
 		canvas.drawRoundRect(urect, round_w, round_h, CdlPalette.getHilightPaint());
 		canvas.drawText(progressValStr, x, y, txtPaintTitle);
 		canvas.drawRoundRect(rectf, round_w, round_h, CdlPalette.getBorderPaint());
 		//h_start =(int) rectf.bottom;
 		rectf.set(getLeft() + pad, h_start, getRight() - pad, h_start + urect.height() / 2);
 		canvas.drawRoundRect(rectf, round_w, round_h, CdlPalette.getPaint(1, x, h_start, (int) rectf.width(), (int) rectf.height()));
 		drawCenterTextInrectCase(canvas, progressMessage, txtPaintValue);
 	}
 
 	private void drawMessage(Canvas canvas) {
 		// CdlUtils.cdlLog(TAG, "message is: >" + messageString + "<");
 		CdlBaseButton.getRectf().set(0, 0, getWidth(), getHeight());
 		if (messageType == CdlMessageView.MESSAGETYPE_WARNING) {
 			drawMessage(canvas, messageString, CdlPalette.getHilightPaint());
 		} else {
 			drawMessage(canvas, messageString, CdlPalette.getTxtPaint(getWidth() / 8, getHeight() / 8));
 		}
 	}
 
 	private void drawMessage(Canvas canvas, String text, Paint paint) {
 		paint.getTextBounds(text, 0, text.length(), bounds);
 		int x = (int) (getWidth() / 2 - bounds.centerX());
 		int y = (int) (getHeight() / 2 - bounds.centerY());
 
 		int round_h = getWidth() / 100;
 		int round_w = getWidth() / 100;
 		int padding = round_h * 3;
 		int dx = (getWidth() / 2 - bounds.centerX());
 		int dy = (int) (getHeight() / 2 - bounds.centerY());
 		rectf.set(bounds.left + dx - padding, bounds.top + dy - padding, bounds.right + dx + padding, bounds.bottom + dy + padding);
 		canvas.drawRoundRect(rectf, round_w, round_h, CdlPalette.getFlashPaint());
 		canvas.drawText(text, x, y, paint);
 		canvas.drawRoundRect(rectf, round_w, round_h, CdlPalette.getBorderPaint());
 	}
 
 
 	public void setProgress(float min, float max, float val, String progressMessage) {
		CdlUtils.cdlLog(TAG, "setProgress =" + val + "="+ progressMessage);
 		this.progressMessage = progressMessage;
 		float interval = (max - min);
 		progressVal = (int) ((val * 100) / interval + min);
 		if (getHandler() == null)
 			return;
 		getHandler().post(new Runnable() {
 			public void run() {
 				invalidate();
 			}
 		});
 	}
 }
