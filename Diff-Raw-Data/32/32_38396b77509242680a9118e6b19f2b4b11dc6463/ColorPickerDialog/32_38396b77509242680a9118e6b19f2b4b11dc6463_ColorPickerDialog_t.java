 /*
  * Copyright (C) 2007 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  * Adapted from the Android API samples
  */
 
 package ac.robinson.mediatablet.view;
 
 import ac.robinson.mediatablet.R;
 import android.annotation.SuppressLint;
 import android.app.Dialog;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.LightingColorFilter;
 import android.graphics.Paint;
 import android.graphics.RectF;
 import android.graphics.Shader;
 import android.graphics.SweepGradient;
 import android.os.Bundle;
 import android.view.Gravity;
 import android.view.MotionEvent;
 import android.view.SoundEffectConstants;
 import android.view.View;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 
 public class ColorPickerDialog extends Dialog {
 
 	public interface OnColorChangedListener {
 		void colorChanged(int color);
 	}
 
 	private OnColorChangedListener mListener;
 	private int mInitialColor;
 
 	private static class ColorPickerView extends View {
 		private Paint mPaint;
 		private Paint mCenterPaint;
 		private final int[] mColors;
 		private OnColorChangedListener mListener;
 		private final Bitmap mHomesteadIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_homestead);
 		private final Bitmap mHomesteadSelectedIcon = BitmapFactory.decodeResource(getResources(),
 				R.drawable.ic_homestead_selected);
 		private int mHomesteadHalfIconWidth, mHomesteadHalfIconHeight;
 		private RectF drawRect;
 
 		ColorPickerView(Context c, OnColorChangedListener l, int color) {
 			super(c);
 			mListener = l;
 			mColors = new int[] { 0xFFFFFFFF, 0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF, 0xFF00FF00, 0xFFFFFF00,
 					0xFFFFFFFF };
 			Shader s = new SweepGradient(0, 0, mColors, null);
 
 			mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 			mPaint.setShader(s);
 			mPaint.setStyle(Paint.Style.STROKE);
 			mPaint.setStrokeWidth(80);
 
 			mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 			mCenterPaint.setColor(color);
 			mCenterPaint.setStrokeWidth(5);
 
 			mHomesteadHalfIconWidth = mHomesteadIcon.getWidth() / 2;
 			mHomesteadHalfIconHeight = mHomesteadIcon.getHeight() / 2;
 
 			drawRect = new RectF();
 		}
 
 		private boolean mTrackingCenter;
 		private boolean mHighlightCenter;
 
 		// no way to reuse LightingColorFilter here
 		@SuppressLint("DrawAllocation")
 		@Override
 		protected void onDraw(Canvas canvas) {
 			float r = CENTER_X - mPaint.getStrokeWidth() * 0.5f;
 			drawRect.left = -r;
 			drawRect.top = -r;
 			drawRect.right = r;
 			drawRect.bottom = r;
 
 			canvas.translate(CENTER_X, CENTER_X);
 			canvas.drawOval(drawRect, mPaint);
 			// canvas.drawCircle(0, 0, CENTER_RADIUS, mCenterPaint);
 
 			if (mTrackingCenter) {
 				// int c = mCenterPaint.getColor();
 				// mCenterPaint.setStyle(Paint.Style.STROKE);
 				//
 				if (mHighlightCenter) {
 					mCenterPaint.setAlpha(0xFF);
 				} else {
 					mCenterPaint.setAlpha(0x80);
 				}
 				// canvas.drawCircle(0, 0,
 				// CENTER_RADIUS + mCenterPaint.getStrokeWidth(),
 				// mCenterPaint);
 				//
 				// mCenterPaint.setStyle(Paint.Style.FILL);
 				// mCenterPaint.setColor(c);
 
 				canvas.drawBitmap(mHomesteadSelectedIcon, -mHomesteadHalfIconWidth, -mHomesteadHalfIconHeight,
 						mCenterPaint);
 			}
 
 			mCenterPaint.setColorFilter(new LightingColorFilter(mCenterPaint.getColor(), 1));
 			canvas.drawBitmap(mHomesteadIcon, -mHomesteadHalfIconWidth, -mHomesteadHalfIconHeight, mCenterPaint);
 			mCenterPaint.setColorFilter(null);
 		}
 
 		@Override
 		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
 			setMeasuredDimension(CENTER_X * 2, CENTER_Y * 2);
 		}
 
 		private static final int CENTER_X = 200;
 		private static final int CENTER_Y = 200;
 		private static final int CENTER_RADIUS = 50;
 
 		// private int floatToByte(float x) {
 		// int n = Math.round(x);
 		// return n;
 		// }
 		// private int pinToByte(int n) {
 		// if (n < 0) {
 		// n = 0;
 		// } else if (n > 255) {
 		// n = 255;
 		// }
 		// return n;
 		// }
 
 		private int ave(int s, int d, float p) {
 			return s + Math.round(p * (d - s));
 		}
 
 		private int interpColor(int colors[], float unit) {
 			if (unit <= 0) {
 				return colors[0];
 			}
 			if (unit >= 1) {
 				return colors[colors.length - 1];
 			}
 
 			float p = unit * (colors.length - 1);
 			int i = (int) p;
 			p -= i;
 
 			// now p is just the fractional part [0...1) and i is the index
 			int c0 = colors[i];
 			int c1 = colors[i + 1];
 			int a = ave(Color.alpha(c0), Color.alpha(c1), p);
 			int r = ave(Color.red(c0), Color.red(c1), p);
 			int g = ave(Color.green(c0), Color.green(c1), p);
 			int b = ave(Color.blue(c0), Color.blue(c1), p);
 
 			return Color.argb(a, r, g, b);
 		}
 
 		// private int rotateColor(int color, float rad) {
 		// float deg = rad * 180 / 3.1415927f;
 		// int r = Color.red(color);
 		// int g = Color.green(color);
 		// int b = Color.blue(color);
 		//
 		// ColorMatrix cm = new ColorMatrix();
 		// ColorMatrix tmp = new ColorMatrix();
 		//
 		// cm.setRGB2YUV();
 		// tmp.setRotate(0, deg);
 		// cm.postConcat(tmp);
 		// tmp.setYUV2RGB();
 		// cm.postConcat(tmp);
 		//
 		// final float[] a = cm.getArray();
 		//
 		// int ir = floatToByte(a[0] * r + a[1] * g + a[2] * b);
 		// int ig = floatToByte(a[5] * r + a[6] * g + a[7] * b);
 		// int ib = floatToByte(a[10] * r + a[11] * g + a[12] * b);
 		//
 		// return Color.argb(Color.alpha(color), pinToByte(ir),
 		// pinToByte(ig), pinToByte(ib));
 		// }
 
 		private static final float PI = 3.1415926f;
 
 		@Override
 		public boolean onTouchEvent(MotionEvent event) {
 			float x = event.getX() - CENTER_X;
 			float y = event.getY() - CENTER_Y;
 			boolean inCenter = Math.sqrt(x * x + y * y) <= CENTER_RADIUS;
 
 			switch (event.getAction()) {
 				case MotionEvent.ACTION_DOWN:
 					mTrackingCenter = inCenter;
 					if (inCenter) {
 						mHighlightCenter = true;
 						invalidate();
 						break;
 					}
 				case MotionEvent.ACTION_MOVE:
 					if (mTrackingCenter) {
 						if (mHighlightCenter != inCenter) {
 							mHighlightCenter = inCenter;
 							invalidate();
 						}
 					} else {
 						float angle = (float) Math.atan2(y, x);
 						// need to turn angle [-PI ... PI] into unit [0....1]
 						float unit = angle / (2 * PI);
 						if (unit < 0) {
 							unit += 1;
 						}
 						mCenterPaint.setColor(interpColor(mColors, unit));
 						invalidate();
 					}
 					break;
 				case MotionEvent.ACTION_UP:
 					playSoundEffect(SoundEffectConstants.CLICK); // play the default button click (respects prefs)
 					if (mTrackingCenter) {
 						if (inCenter) {
 							mListener.colorChanged(mCenterPaint.getColor());
 						}
 						mTrackingCenter = false; // so we draw w/o halo
 						invalidate();
 					}
 					break;
 			}
 			return true;
 		}
 	}
 
 	public ColorPickerDialog(Context context, OnColorChangedListener listener, int initialColor) {
 		super(context);
 
 		mListener = listener;
 		mInitialColor = initialColor;
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		OnColorChangedListener l = new OnColorChangedListener() {
 			public void colorChanged(int color) {
 				mListener.colorChanged(color);
 				dismiss();
 			}
 		};
 
 		LinearLayout mainPanel = new LinearLayout(getContext());
 		mainPanel.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
 		mainPanel.setOrientation(LinearLayout.VERTICAL);
 		mainPanel.setPadding(0, 20, 0, 20);
 		mainPanel.setGravity(Gravity.CENTER);
 		mainPanel.addView(new ColorPickerView(getContext(), l, mInitialColor));
 
 		// Button cancelButton = new Button(getContext());
		// cancelButton.setText(getContext().getString(R.string.button_cancel));
 		// cancelButton.setOnClickListener(new View.OnClickListener() {
 		// @Override
 		// public void onClick(View v) {
 		// dismiss();
 		// }
 		// });
 		// LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
 		// layoutParams.setMargins(0, 40, 0, 0);
 		// mainPanel.addView(cancelButton, layoutParams);
 
 		setContentView(mainPanel);
 		setTitle(getContext().getString(R.string.title_change_homestead_colour));
 	}
 }
