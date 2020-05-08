 package com.petrifiednightmares.singularityChess.ui;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapShader;
 import android.graphics.Color;
 import android.graphics.LinearGradient;
 import android.graphics.Paint;
 import android.graphics.RadialGradient;
 import android.graphics.Shader;
 import android.graphics.Typeface;
 import android.media.MediaPlayer;
 
 import com.petrifiednightmares.singularityChess.R;
 import com.petrifiednightmares.singularityChess.utilities.SingularBitmapFactory;
 
 public class SUI
 {
 
 	public static int			WIDTH, HEIGHT, MIN_DIMENSION, UNIT, PADDING, PIECE_SIZE,
 			TOP_PADDING, CIRCLE_RADIUS_DIFFERENCE, TOP_BAR_BOTTOM, BORDER_WIDTH, HEIGHT_CENTER,
 			BOTTOM, BOTTOM_BAR_HEIGHT;
 
 	public static Paint			darkPaint, lightPaint, highlightPaint, highlightPaint2,
 			attackPaint, attackPaint2, piecePaint, labelPaint, flashPaint, flashPaint2,
 			kingThreatenPaint, kingThreatenPaint2, turnNamePaint, topBarPaint, topBarTexturePaint,
 			turnNameWhitePaint, turnNameBlackPaint, borderPaint, borderShadowPaint,
 			boardLightingPaint, gameLightingPaint;
 	public static Paint			whitePaint;
 
 	public static MediaPlayer	pieceSound;
 
 	// determines if a cached bg was found. This means board doesn't have to set
 	// up draws.
 	public static boolean		CACHED_BACKGROUND	= false;
 
 	public static void setup(int width, int height, Resources r, Context c)
 	{
 		setupMeasurements(width, height);
 
 		setupSimplePaints();
 		setupColorPaints();
 
 		setupTexturedPaints(r);
 
 		setupAudio(c);
 	}
 
 	private static void setupMeasurements(int width, int height)
 	{
 		WIDTH = width;
 		HEIGHT = height;
 		MIN_DIMENSION = Math.min(WIDTH, HEIGHT);
 		UNIT = (int) (MIN_DIMENSION / 100.0);
 		PADDING = 5 * UNIT;
 		PIECE_SIZE = 10 * UNIT;
 		TOP_PADDING = 70; // Derived from: 10 * UNIT;
 		TOP_BAR_BOTTOM = 56; // Derived from: 8 * UNIT;
//<<<<<<< HEAD
 		CIRCLE_RADIUS_DIFFERENCE = 11 * UNIT; // 12
 		
 		HEIGHT_CENTER = TOP_PADDING + 6 * CIRCLE_RADIUS_DIFFERENCE + BORDER_WIDTH;
 
 		BOTTOM_BAR_HEIGHT = SUI.UNIT * 10;
 
 		// 60 from shadow, 10 from padding
 		BOTTOM = HEIGHT_CENTER + 6 * CIRCLE_RADIUS_DIFFERENCE + 60 + 10;
 
 		// If the circle is so big it pushes away the bottom bar
 		if (BOTTOM > SUI.HEIGHT - SUI.PADDING - SUI.BOTTOM_BAR_HEIGHT)
 		{
 			HEIGHT_CENTER = (height - SUI.PADDING - SUI.BOTTOM_BAR_HEIGHT - TOP_PADDING - 70) / 2
 					+ TOP_PADDING + BORDER_WIDTH;
 
 			CIRCLE_RADIUS_DIFFERENCE = ((SUI.HEIGHT - SUI.PADDING - SUI.BOTTOM_BAR_HEIGHT - 70) - HEIGHT_CENTER) / 6;
 		}
		
		BORDER_WIDTH = (WIDTH / 2 - PADDING) - 4 * CIRCLE_RADIUS_DIFFERENCE;

//=======
//		BOTTOM_BAR_HEIGHT = SUI.UNIT * 10;
//
//		HEIGHT_CENTER = (height - SUI.PADDING - SUI.BOTTOM_BAR_HEIGHT - TOP_PADDING - 70)/2
//				+ TOP_PADDING;
//		
//		CIRCLE_RADIUS_DIFFERENCE = ((SUI.HEIGHT - SUI.PADDING - SUI.BOTTOM_BAR_HEIGHT 
//				- 70) - HEIGHT_CENTER) / 6;
//		
//		BORDER_WIDTH = CIRCLE_RADIUS_DIFFERENCE/4;
//>>>>>>> 4b5bd6d108600d3c19e26dfcad23c2e14d8aad55
 	}
 
 	private static void setupColorPaints()
 	{
 		whitePaint = new Paint();
 		whitePaint.setColor(Color.WHITE);
 		whitePaint.setAntiAlias(true);
 	}
 
 	public static Paint getHorizontalPaint(int left, int top, int height, int width)
 	{
 		int darkColor = Color.argb(200, 24, 17, 7);
 		int lightColor = Color.argb(100, 62, 43, 18);
 
 		Paint tileHorizontalPaint = new Paint();
 		tileHorizontalPaint.setAntiAlias(true);
 		tileHorizontalPaint.setShader(new LinearGradient(left, top + height / 2f, left + width, top
 				+ height / 2f, new int[] { darkColor, lightColor, lightColor, darkColor,
 				Color.argb(20, 255, 255, 255) }, new float[] { 0f, 0.2f, 0.8f, 1f - 1.5f / width,
 				1f }, Shader.TileMode.MIRROR));
 		tileHorizontalPaint.setAlpha(150);
 
 		return tileHorizontalPaint;
 	}
 
 	public static Paint getVerticalPaint(int left, int top, int height, int width)
 	{
 		int darkColor2 = Color.rgb(19, 14, 6);
 		int lightColor2 = Color.argb(150, 62, 43, 18);
 
 		Paint verticalPaint = new Paint();
 		verticalPaint.setAntiAlias(true);
 		verticalPaint.setShader(new LinearGradient(left + width / 2f, top, left + width / 2f, top
 				+ height,
 				new int[] { darkColor2, lightColor2, lightColor2, darkColor2, Color.WHITE },
 				new float[] { 0f, 0.25f, 0.75f, 1f - 1.5f / height, 1f }, Shader.TileMode.MIRROR));
 		verticalPaint.setAlpha(150);
 
 		return verticalPaint;
 	}
 
 	private static void setupSimplePaints()
 	{
 		borderShadowPaint = new Paint();
 		borderShadowPaint.setColor(Color.TRANSPARENT);
 		borderShadowPaint.setAntiAlias(true);
 		borderShadowPaint.setShadowLayer(40, 20, 20, Color.argb(200, 0, 0, 0));
 
 		highlightPaint = new Paint();
 		highlightPaint.setColor(Color.rgb(36, 109, 218));
 		highlightPaint.setAlpha(150);
 		highlightPaint.setAntiAlias(true);
 
 		highlightPaint2 = new Paint(highlightPaint);
 		highlightPaint2.setColor(Color.rgb(148, 156, 171));
 		highlightPaint2.setAlpha(150);
 		highlightPaint2.setAntiAlias(true);
 
 		flashPaint = new Paint();
 		flashPaint.setColor(Color.rgb(0, 255, 0));
 		flashPaint.setAlpha(200);
 		flashPaint.setAntiAlias(true);
 
 		flashPaint2 = new Paint();
 		flashPaint2.setColor(Color.rgb(101, 154, 101));
 		flashPaint2.setAlpha(200);
 		flashPaint2.setAntiAlias(true);
 
 		attackPaint = new Paint();
 		attackPaint.setColor(Color.rgb(205, 52, 52));
 		attackPaint.setAlpha(150);
 		attackPaint.setAntiAlias(true);
 
 		attackPaint2 = new Paint(attackPaint);
 		attackPaint2.setColor(Color.rgb(143, 112, 112));
 		attackPaint2.setAlpha(150);
 		attackPaint2.setAntiAlias(true);
 
 		kingThreatenPaint = new Paint();
 		kingThreatenPaint.setColor(Color.rgb(255, 127, 0));
 		kingThreatenPaint.setAntiAlias(true);
 
 		kingThreatenPaint2 = new Paint();
 		kingThreatenPaint2.setColor(Color.rgb(255, 127, 0));
 		kingThreatenPaint2.setAntiAlias(true);
 		kingThreatenPaint2.setAlpha(80);
 
 		piecePaint = new Paint();
 		piecePaint.setAntiAlias(true);
 		piecePaint.setFilterBitmap(true);
 		piecePaint.setDither(true);
 
 		labelPaint = new Paint();
 		labelPaint.setColor(Color.rgb(0, 0, 0));
 		labelPaint.setTextSize(15);
 		labelPaint.setTypeface(Typeface.create("courier new", Typeface.NORMAL));
 		labelPaint.setAntiAlias(true);
 
 		turnNamePaint = new Paint();
 		turnNamePaint.setColor(Color.WHITE);
 		turnNamePaint.setShadowLayer(2, 0, 0, Color.BLACK);
 		turnNamePaint.setTextSize(25);
 		turnNamePaint.setAntiAlias(true);
 
 		turnNameBlackPaint = new Paint();
 		turnNameBlackPaint.setColor(Color.BLACK);
 		turnNameBlackPaint.setAntiAlias(true);
 
 		turnNameWhitePaint = new Paint();
 		turnNameWhitePaint.setColor(Color.WHITE);
 		turnNameWhitePaint.setAntiAlias(true);
 	}
 
 	private static void setupTexturedPaints(Resources r)
 	{
 		Bitmap _darkTexture = SingularBitmapFactory.buildBitmap(r, R.drawable.wood_pattern);
 		BitmapShader darkShader = new BitmapShader(_darkTexture, Shader.TileMode.REPEAT,
 				Shader.TileMode.REPEAT);
 
 		darkPaint = new Paint();
 		darkPaint.setShader(darkShader);
 		darkPaint.setAntiAlias(true);
 		darkPaint.setFilterBitmap(true);
 
 		Bitmap _lightTexture = SingularBitmapFactory.buildBitmap(r, R.drawable.retina_wood_1);
 		BitmapShader lightShader = new BitmapShader(_lightTexture, Shader.TileMode.REPEAT,
 				Shader.TileMode.REPEAT);
 
 		Bitmap _topBarTexture = SingularBitmapFactory.buildBitmap(r, R.drawable.wild_oliva);
 		BitmapShader topBarShader = new BitmapShader(_topBarTexture, Shader.TileMode.REPEAT,
 				Shader.TileMode.REPEAT);
 
 		topBarTexturePaint = new Paint();
 		topBarTexturePaint.setShader(topBarShader);
 		topBarTexturePaint.setAntiAlias(true);
 		topBarTexturePaint.setFilterBitmap(true);
 
 		lightPaint = new Paint();
 		lightPaint.setShader(lightShader);
 		lightPaint.setAntiAlias(true);
 		lightPaint.setFilterBitmap(true);
 
 		Bitmap _borderTexture = SingularBitmapFactory.buildBitmap(r, R.drawable.leather);
 		BitmapShader borderShader = new BitmapShader(_borderTexture, Shader.TileMode.REPEAT,
 				Shader.TileMode.REPEAT);
 		borderPaint = new Paint();
 		borderPaint.setShader(borderShader);
 		borderPaint.setAntiAlias(true);
 		borderPaint.setFilterBitmap(true);
 
 		topBarPaint = new Paint();
 		topBarPaint.setAntiAlias(true);
 		topBarPaint.setShader(new LinearGradient(0, 0, 0, TOP_BAR_BOTTOM, Color.WHITE, Color.rgb(
 				50, 50, 50), Shader.TileMode.MIRROR));
 		topBarPaint.setAlpha(150);
 
 		boardLightingPaint = new Paint();
 		boardLightingPaint.setAntiAlias(true);
 		boardLightingPaint.setShader(new RadialGradient(WIDTH / 2f + 6 * UNIT,
 				(float) HEIGHT_CENTER - 4 * UNIT, 5f * CIRCLE_RADIUS_DIFFERENCE, new int[] {
 						Color.rgb(255, 255, 200), Color.argb(255, 20, 20, 0) }, new float[] { 0.3f,
 						0.8f }, Shader.TileMode.MIRROR));
 		boardLightingPaint.setAlpha(80);
 
 		gameLightingPaint = new Paint();
 		gameLightingPaint.setAntiAlias(true);
 		gameLightingPaint.setShader(new RadialGradient(WIDTH / 2f + 6 * UNIT, (float) HEIGHT_CENTER
 				- 4 * UNIT, HEIGHT / 2, new int[] { Color.rgb(255, 255, 200),
 				Color.argb(255, 20, 20, 0) }, new float[] { 0.2f, 0.5f }, Shader.TileMode.MIRROR));
 		gameLightingPaint.setAlpha(40);
 	}
 
 	private static void setupAudio(Context c)
 	{
 		pieceSound = MediaPlayer.create(c, R.raw.piece);
 	}
 }
