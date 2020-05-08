 /*
 Copyright 2013 Cadeli
 Contact the authors at cadeli.drummachine@yahoo.com
 See updates at http://github.com/cadeli/CdlUI
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
    http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
  */
 
 package com.cadeli.ui;
 
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.view.MotionEvent;
 
 import com.cadeli.ui.interfaces.OnLongPressCdlListener;
 import com.cadeli.ui.interfaces.OnScrollCdlListener;
 import com.cadeli.ui.interfaces.OnTapUpCdlListener;
 
 public class CdlBaseButton {
 	private static final String TAG = "CdlBaseButton";
 
 	public static final int DISPLAYMODE_COMPACT = 0;
 	public static final int DISPLAYMODE_EXPANDED = 1;
 	public static final int DISPLAYMODE_LIST = 2;
 
 	private boolean visible = true;
 	private boolean hilight = false;
 
 	protected Rect rect = new Rect(100, 10, 200, 200);
 	protected static Rect bounds = new Rect();
 	protected static RectF rectf = new RectF();
 
 	protected int w = 60;
 	protected int h = 24;
 	protected int grid_width = 1;
 	protected int grid_height = 1;
 
 	protected int backgroundColor;
 	protected String label = "Default_Label";
 	private String subLabel;
 	protected int displayMode = DISPLAYMODE_COMPACT;
 	protected int padding;
 	private float round = 0.1f;
 
 	private boolean flashCapable;
 	private boolean flashing;
 
 	protected String textUp;
 	protected String textDown;
 
 	private OnTapUpCdlListener onTapUpCdlListener;
 	private OnLongPressCdlListener onLongPressCdlListener;
 	private OnScrollCdlListener scrollCdlListener;
 
 	protected float round_h;
 	protected float round_w;
 
 	private boolean isBorder=true;
 	private boolean isEnabled=true;
 
 	/**
 	 * 
 	 * @param x
 	 * @param y
 	 * @param w
 	 * @param h
 	 */
 	public void setSize(int x, int y, int w, int h) {
 		rect.set(x, y, x + w, y + h);
 		this.w = w;
 		this.h = h;
 		computeRound(w, h);
 	}
 
 	/**
 	 * 
 	 * @param w
 	 * @param h
 	 */
 	public void setSize(int w, int h) {
 		this.w = w;
 		this.h = h;
 		computeRound(w, h);
 	}
 
 	/**
 	 * 
 	 * @param gw
 	 * @param gh
 	 */
 	public void setGridSize(int gw, int gh) {
 		grid_width = gw;
 		grid_height = gh;
 	}
 
 	private void computeRound(int w2, int h2) {
 		if (w2 < h2) {
 			round_w = round * w2;
 			round_h = round * w2;
 		} else {
 			round_w = round * h2;
 			round_h = round * h2;
 		}
 	}
 
 	/**
 	 * 
 	 * @param left
 	 * @param top
 	 */
 	public void setPosition(int left, int top) {
 		rect.top = top;
 		rect.left = left;
 		rect.bottom = rect.top + h;
 		rect.right = rect.left + w;
 	}
 
 	public void draw(Canvas canvas) {
 		if (isVisible()) {
 			rectf.set(rect.left + padding, rect.top + padding, rect.right - padding, rect.bottom - padding);
 			if (flashing) {
 				canvas.drawRoundRect(rectf, round_w, round_h, CdlPalette.getFlashPaint());
 			} else {
 				canvas.drawRoundRect(rectf, round_w, round_h, CdlPalette.getPaint(backgroundColor,getLeft(),getTop(),w,h));
 			}
 			if (isBorder) {
 				canvas.drawRoundRect(rectf, round_w, round_h, CdlPalette.getBorderPaint());
 			}
 		}
 	}
 
 	protected void drawLabel(Canvas canvas) {
 		if (getLabel().contains(" ") && getSubLabel() == null) {
 			drawCenterTextUp(canvas, textUp, CdlPalette.getTxtPaint(w-2*padding, h-2*padding));
 			drawCenterTextDn(canvas, textDown, CdlPalette.getTxtPaint(w-2*padding, h-2*padding));
 		} else {
 			drawCenterText(canvas, getLabel(), CdlPalette.getTxtPaint(w-2*padding, h-2*padding));
 			if (getSubLabel() != null) {
 				drawBottomText(canvas, getSubLabel(), CdlPalette.getTxtPaint((w-2*padding) / 3, (h-2*padding) / 3));
 			}
 		}
 	}
 
 	protected void drawCenterText(Canvas canvas, String text, Paint paint) {
 		if (text == null)
 			return;
 		text = schrinkText(paint, bounds, text);
 		paint.getTextBounds(text, 0, text.length(), bounds);
 		int x = rect.left + rect.width() / 2 - bounds.centerX();
 		int y = rect.top + rect.height() / 2 - bounds.centerY();
 		canvas.drawText(text, x, y, paint);
 	}
 
 	protected void drawCenterTextDn(Canvas canvas, String text, Paint paint) {
 		if (text == null)
 			return;
 		text = schrinkText(paint, bounds, text);
 		paint.getTextBounds(text, 0, text.length(), bounds);
 		rectf.set(getLeft(), getTop() + getHeight() / 2, getRight(), getBottom());
 		drawCenterTextInrectCase(canvas, text, paint);
 	}
 
 	protected void drawCenterTextUp(Canvas canvas, String text, Paint paint) {
 		if (text == null)
 			return;
 		text = schrinkText(paint, bounds, text);
 		paint.getTextBounds(text, 0, text.length(), bounds);
 		rectf.set(getLeft(), getTop(), getRight(), getTop() + getHeight() / 2);
 		drawCenterTextInrectCase(canvas, text, paint);
 	}
 
 	public static void drawCenterTextInrectCase(Canvas canvas, String text, Paint paint) {
 		if (text == null)
 			return;
 		paint.getTextBounds(text, 0, text.length(), bounds);
 		int x = (int) (rectf.left + rectf.width() / 2 - bounds.centerX());
 		int y = (int) (rectf.top + rectf.height() / 2 - bounds.centerY());
 		// XmlUtil.myLog(TAG," x="+x + " y="+y);
 		canvas.drawText(text, x, y, paint);
 	}
 
 	protected void drawTopText(Canvas canvas, String text, Paint forePaint, Paint backPaint) {
 		if (text == null)
 			return;
 		text = schrinkText(forePaint, bounds, text);
 		forePaint.getTextBounds(text, 0, text.length(), bounds);
 		rectf.top = rect.top - rect.width() / 4 - bounds.height();
 		rectf.bottom = rect.top - 4;
 		rectf.left = rect.left;
 		rectf.right = rect.right;
 		canvas.drawRoundRect(rectf, 5.0f, 5.0f, backPaint);
 
 		// paint.getTextBounds(text, 0, text.length(), bounds);
 		int x = (int) (rectf.left + rectf.width() / 2 - bounds.centerX());
 		int y = (int) (rectf.top + rectf.height() / 2 - bounds.centerY());
 		canvas.drawText(text, x, y, forePaint);
 		return;
 	}
 
 	protected void drawBottomText(Canvas canvas, String text, Paint paint) {
 		if (text == null)
 			return;
 		text = schrinkText(paint, bounds, text);
 		paint.getTextBounds(text, 0, text.length(), bounds);
 		rectf.top = rect.top + rect.height() / 2;
 		rectf.bottom = rect.bottom;
 		rectf.left = rect.left;
 		rectf.right = rect.right;
 
 		int x = (int) (rectf.left + rectf.width() / 2 - bounds.centerX());
 		int y = (int) (rectf.top + rectf.height() / 2 - bounds.centerY());
 		canvas.drawText(text, x, y, paint);
 		return;
 	}
 
 	protected void drawTopTextIn(Canvas canvas, String text, Paint paint) {
 		if (text == null)
 			return;
 		text = schrinkText(paint, bounds, text);
 		paint.getTextBounds(text, 0, text.length(), bounds);
 		int x = rect.left + rect.width() / 2 - bounds.centerX();
 		int y = rect.top + bounds.height();
 		canvas.drawText(text, x, y, paint);
 		return;
 	}
 
 	private String schrinkText(Paint paint, Rect bounds2, String text) {
 		paint.getTextBounds(text, 0, text.length(), bounds2);
 		// CdlUtils.cdlLog(TAG, "schrinkText b="+ bounds2.width() + " getW="+ getWidth());
 		if (bounds2.width() < getWidth() - 2 * padding) {
 			return text;
 		}
 		while (text.length() > 2 && bounds2.width() > getWidth() - 2 * padding) {
 			text = text.substring(0, text.length() - 1);
 			paint.getTextBounds(text, 0, text.length(), bounds2);
 			CdlUtils.cdlLog(TAG, "schrinkText " + getLabel() + "b=" + bounds2.width() + " getW=" + getWidth() + " " + text);
 		}
 		return text;
 	}
 
 	public boolean isXYInControl(float eventX, float eventY) {
 		if (isVisible()) {
 			if (eventY > rect.top && eventY < rect.bottom) {
 				if (eventX > rect.left && eventX < rect.right) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	public void singleTapUp(MotionEvent e) {
 		if (onTapUpCdlListener != null) {
 			onTapUpCdlListener.tapUp(this, e);
 		}
		CdlUtils.cdlLog(TAG, "singleTapUp: " + label);
 	}
 
 	public void longPress(MotionEvent e) {
 		if (onLongPressCdlListener != null) {
 			onLongPressCdlListener.longPress(this, e);
 		}
 		CdlUtils.cdlLog(TAG, "longpress on cdl: " + label);
 	}
 
 	public void scroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
 		if (scrollCdlListener != null) {
 			scrollCdlListener.scroll(this, e1, e2, distanceX, distanceY);
 		}
 		CdlUtils.cdlLog(TAG, "scroll on cdl: " + label);
 	}
 
 	public void setLabel(String s) {
 		CdlUtils.cdlLog(TAG, "setLabel=" + s);
 		this.label = s;
 		textUp = s;
 		textDown = s;
 		if (s.contains(" ")) {
 			int idx = s.indexOf(" ");
 			int end = s.length();
 			textUp = s.substring(0, idx);
 			textDown = s.substring(idx + 1, end);
 			CdlUtils.cdlLog(TAG, "textDn=" + textDown);
 		}
 	}
 
 	public Rect getRect() {
 		return rect;
 	}
 
 	public String getLabel() {
 		return label;
 	}
 
 	public void setSubLabel(String subLabel) {
 		this.subLabel = subLabel;
 	}
 
 	public String getSubLabel() {
 		return subLabel;
 	}
 
 	public boolean isVisible() {
 		return visible;
 	}
 
 	public void setVisible(boolean visible) {
 		this.visible = visible;
 	}
 
 	public boolean isHilight() {
 		return hilight;
 	}
 
 	public void setHilight(boolean hilight) {
 		this.hilight = hilight;
 	}
 
 	public int getWidth() {
 		return (int) (rect.right - rect.left);
 	}
 
 	public int getHeight() {
 		return (int) (rect.bottom - rect.top);
 	}
 
 	public int getLeft() {
 		return (int) rect.left;
 	}
 
 	public int getTop() {
 		return (int) rect.top;
 	}
 
 	public int getBottom() {
 		return (int) rect.bottom;
 	}
 
 	public int getRight() {
 		return (int) rect.right;
 	}
 
 	public void setPadding(int padding) {
 		this.padding = padding;
 	}
 
 	public void setBackgroundColor(int backgroundColor) {
 		this.backgroundColor = backgroundColor;
 	}
 
 	public boolean isFlashCapable() {
 		return flashCapable;
 	}
 
 	public void setFlashCapable(boolean flashCapable) {
 		this.flashCapable = flashCapable;
 	}
 
 	public void setFlashing(boolean b) {
 		this.flashing = b;
 	}
 
 	public OnTapUpCdlListener getOnTapUpCdlListener() {
 		return onTapUpCdlListener;
 	}
 
 	public void setOnTapUpCdlListener(OnTapUpCdlListener onTapUpCdlListener) {
 		this.onTapUpCdlListener = onTapUpCdlListener;
 	}
 
 	public OnLongPressCdlListener getOnLongPressCdlListener() {
 		return onLongPressCdlListener;
 	}
 
 	public void setOnLongPressCdlListener(OnLongPressCdlListener onLongPressCdlListener) {
 		this.onLongPressCdlListener = onLongPressCdlListener;
 	}
 
 	public OnScrollCdlListener getScrollCdlListener() {
 		return scrollCdlListener;
 	}
 
 	public void setScrollCdlListener(OnScrollCdlListener scrollCdlListener) {
 		this.scrollCdlListener = scrollCdlListener;
 	}
 
 	public void setRound(float round) {
 		this.round = round;
 	}
 
 	public void setDisplayMode(int displayMode) {
 		this.displayMode = displayMode;
 	}
 
 	public int getGrid_width() {
 		return grid_width;
 	}
 
 	public int getGrid_height() {
 		return grid_height;
 	}
 
 	public boolean isBorder() {
 		return isBorder;
 	}
 
 	public void setBorder(boolean isBorder) {
 		this.isBorder = isBorder;
 	}
 
 	public void setEnabled(boolean b) {
 		isEnabled=b;
 	}
 
 	public boolean isEnabled() {
 		return isEnabled;
 	}
 
 }
