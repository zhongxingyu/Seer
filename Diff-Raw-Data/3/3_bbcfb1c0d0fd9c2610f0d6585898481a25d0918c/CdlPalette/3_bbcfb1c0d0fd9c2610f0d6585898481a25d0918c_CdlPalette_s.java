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
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 
 public class CdlPalette {
 	private static final String TAG = "CdlPalette";
 	public static final int COLORSCHEME1 = 10;
 	public static final int COLORSCHEME2 = 20;
 	public static final int COLORSCHEME3 = 30;
 	public static final int COLORSCHEME4 = 40;
 	public static final int COLORSCHEME5 = 50;
 
 	static List<Paint> colorList = new ArrayList<Paint>();
 	static Paint txtPaint;
 	static Paint txtInversePaint;
 	static Paint borderPaint;
 	static Paint hilightPaint;
 	static Paint hilightPaintLarge;
 	static Paint blackPaint;
 	static Paint blackPaintLarge;
 	static int txtPaintColor = Color.WHITE;
 	static int txtPaintInverseColor = Color.BLACK;
 	private static Paint flashPaint;
 	private static int hilightColor = Color.GREEN;
 	private static float borderSize = 2;
 	private static int defaultAlpha = 128;
 	private static float defaulStrokeWidth = 12;
 	private static boolean isGradient = false;
 
 	// protected static final int ACTIVETEXT_COLOR = 0xFFFFFFFF;
 	// private static final int INACTIVETEXT_COLOR = 0xFF808080;
 
 	public static void addColor(int color) {
 		Paint paint = new Paint();
 		paint.setColor(color);
 		paint.setAntiAlias(true);
 		paint.setDither(true);
 		paint.setAlpha(192);
 		colorList.add(paint);
 		CdlUtils.cdlLog(TAG,"AddColor:"+ Integer.toHexString(paint.getColor()));
 	}
 	
 	
 	public static Paint getPaint(int i) {
 		return  getPaint( i, 0, 0, 100, 100);
 	}
 
 
 	public static Paint getPaint(int i, int x, int y, int w, int h) {
 		int size = colorList.size();
 		if (i >= 0 && size > 0) {
 			Paint p = (Paint) colorList.get(i % size);
 			if (isGradient) { // TODO avoid new
 				// p.setShader(new LinearGradient(x, y, x, y+h,
 				// p.getColor(),
 				// Color.parseColor("#FF000000"), Shader.TileMode.REPEAT));
 				// Shader s = p.getShader();
 				// LinearGradient lg = s.getLocalMatrix(localM);
 				CdlUtils.cdlLog(TAG, "new Grdient");
 			}
 			return p;
 		}
 		return (Paint) colorList.get(0);
 	}
 
 	public static void createDefaultColors() {
 		setColorScheme(COLORSCHEME3);
 	}
 
 	public static void setColorScheme(int colorscheme) {
 		CdlUtils.cdlLog(TAG, "setColorScreme :" + colorscheme);
 		colorList.clear();
 		switch (colorscheme) {
 		case COLORSCHEME1:
 			addColor(0x006BC2);
 			addColor(0x39CC00);
 			addColor(0xC6E400);
 			addColor(0xF3DF00);
 			addColor(0xF3AF00);
 		break;
 		case  COLORSCHEME2:
 			addColor(0x737791);
 			addColor(0xDDC71A);
 			addColor(0xD32643);
 			addColor(0x0A3560);
 		break;
 		case  COLORSCHEME3:
 			addColor(Color.rgb(137, 247, 142));
 			addColor(Color.rgb(190, 151, 233));
 			addColor(Color.rgb(252, 248, 132));
 			addColor(Color.rgb(255, 0, 0));
 			addColor(Color.rgb(255, 191, 223));			
 		break;
 		case  COLORSCHEME4:
 			addColor(Color.rgb(255, 64, 64));
 			addColor(Color.rgb(106, 72, 215));
 			addColor(Color.rgb(255, 222, 64));
 			addColor(Color.rgb(57, 230, 57));
 		break;
 		case  COLORSCHEME5:
 			addColor(0xE19595);//Color.rgb(225, 149, 149));//0xE19595
 			addColor(0x87E4EF);//Color.rgb(135, 228, 239));//0x87E4EF
 			addColor(Color.rgb(149, 255, 149));
 			addColor(Color.rgb(228, 239, 135));
 		break;
 		default:
 			addColor(Color.rgb(255, 115, 115));
 			addColor(Color.rgb(135, 110, 215));
 			addColor(Color.rgb(255, 231, 115));
 			addColor(Color.rgb(103, 230, 103));
 		}
 	}
 	
 	
 	public static Paint getTxtPaint(int size) {		
 		return getTxtPaint(size, size);
 	}
 
 
 	public static Paint getTxtPaint(int w, int h) {
 		if (txtPaint == null) {
 			txtPaint = new Paint();
 			txtPaint.setAntiAlias(true);
 			txtPaint.setDither(true);
 		}
 		if (w < h) {
 			txtPaint.setTextSize((int)(float)(w / 2.5f));
 		} else {
 			txtPaint.setTextSize((int)(float)(h / 2.5f));
 		}
 		txtPaint.setColor(txtPaintColor);
 		return txtPaint;
 	}
 
 	public static Paint getTxtInversePaint(int size) {		
 		return getTxtInversePaint(size, size);
 	}
 
 
 	public static Paint getTxtInversePaint(int w, int h) {
 		if (txtInversePaint == null) {
 			txtInversePaint = new Paint();
 			txtInversePaint.setAntiAlias(true);
 			txtInversePaint.setDither(true);
 		}
 		if (w < h) {
 			txtInversePaint.setTextSize((int)(float)(w / 2.5f));
 		} else {
 			txtInversePaint.setTextSize((int)(float)(h / 2.5f));
 		}
 		txtInversePaint.setColor(txtPaintInverseColor);
 		return txtInversePaint;
 	}
 
 	public static Paint getBorderPaint() {
 		if (borderPaint == null) {
 			borderPaint = new Paint();
 			borderPaint.setAntiAlias(true);
 			borderPaint.setDither(true);
 			borderPaint.setAlpha(defaultAlpha);
 			borderPaint.setStyle(Style.STROKE);
 			borderPaint.setStrokeWidth(borderSize);
 		}
 
 		borderPaint.setColor(txtPaintColor);
 		return borderPaint;
 	}
 
 	public static Paint getFlashPaint() {
 		if (flashPaint == null) {
 			flashPaint = new Paint();
 			flashPaint.setColor(Color.GRAY);
 			flashPaint.setAlpha(170);
 			flashPaint.setAntiAlias(true);
 			flashPaint.setDither(true);
 		}
 		return flashPaint;
 	}
 
 	public static Paint getHilightPaint() {
 		if (hilightPaint == null) {
 			hilightPaint = new Paint();
 			hilightPaint.setColor(hilightColor);
 			hilightPaint.setAntiAlias(true);
 			hilightPaint.setDither(true);
 			hilightPaint.setAlpha(defaultAlpha);
 		}
 		return hilightPaint;
 	}
 
 	public static Paint getBlackPaint() {
 		if (blackPaint == null) {
 			blackPaint = new Paint();
 			blackPaint.setColor(Color.BLACK);
 			blackPaint.setAntiAlias(true);
 			blackPaint.setDither(true);
 		}
 		return blackPaint;
 	}
 
 	public static Paint getHilightPaintLarge() {
 		if (hilightPaintLarge == null) {
 			hilightPaintLarge = new Paint();
 			hilightPaintLarge.setStyle(Style.STROKE);
 			hilightPaintLarge.setStrokeWidth(defaulStrokeWidth);
 			hilightPaintLarge.setColor(hilightColor);
 			hilightPaintLarge.setAntiAlias(true);
 			hilightPaintLarge.setDither(true);
 			hilightPaintLarge.setAlpha(128);
 		}
 		return hilightPaintLarge;
 	}
 
 	public static Paint getBlackPaintLarge() {
 		if (blackPaintLarge == null) {
 			blackPaintLarge = new Paint();
 			blackPaintLarge.setStyle(Style.STROKE);
 			blackPaintLarge.setStrokeWidth(defaulStrokeWidth);
 			blackPaintLarge.setColor(Color.DKGRAY);
 			blackPaintLarge.setAntiAlias(true);
 			blackPaintLarge.setDither(true);
 			blackPaintLarge.setAlpha(128);
 		}
 		return blackPaintLarge;
 	}
 
 	public static void setHilightColor(int hilightColor) {
 		CdlPalette.hilightColor = hilightColor;
 	}
 
 	public static int getLastColorIndex() {
 		return colorList.size() - 1;
 	}
 
 }
