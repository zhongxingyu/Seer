 package org.ggj2013;
 
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.Paint.Align;
 import android.graphics.Paint.Style;
 import android.graphics.Path;
 import android.graphics.Rect;
 import android.graphics.Typeface;
 import android.util.Log;
 
 public class Game {
 	private long timeDiff = 2000000; // 2ms
 	private long lastUpdate = -1;
 	private Room currentRoom;
 	private int currentLevel;
 	public final FullscreenActivity activity;
 	public SoundManager soundManager;
 
 	public Game(FullscreenActivity activity) {
 		this.activity = activity;
 		soundManager = new SoundManager(activity.getApplicationContext());
 		soundManager.loadSoundPack(new SoundPackStandard());
 		restart();
 		activity.game = this;
 	}
 
 	public void restart() {
 		currentRoom = null;
 		currentLevel = 0;
 		soundManager.stopAll();
 		onNextLevel();
 	}
 
 	public void onPause() {
 		soundManager.autoPause();
 	}
 
 	public void onResume() {
 		soundManager.autoResume();
 	}
 
 	public void onNextLevel() {
 		currentLevel++;
 
 		if (currentLevel == 10) {
 			currentRoom = null;
 			Log.e("GAME", "WON!");
 		} else {
 			currentRoom = new Room(this);
 		}
 	}
 
 	public void onUpdate() {
 		long now = System.nanoTime();
 
 		if (lastUpdate > 0) {
 			timeDiff = now - lastUpdate;
 		}
 
 		lastUpdate = now;
 
 		if (currentRoom != null) {
 			float timeElapsed = timeDiff / 1000000000f;
 			// Log.d("DELTA", Float.toString(timeElapsed));
 			currentRoom.onUpdate(timeElapsed);
 		}
 	}
 
 	public void onRender(Canvas c) {
 		c.setMatrix(new Matrix());
 
 		int w = c.getWidth();
 		int h = c.getHeight();
 		float textsize = 64 * w / 1000;
 
 		int centerX = c.getClipBounds().centerX();
 		int centerY = c.getClipBounds().centerY();
 
 		Paint bg = new Paint();
 		bg.setColor(Color.RED);
 		bg.setTextSize(textsize * 2);
 		bg.setStyle(Style.FILL);
 		bg.setAntiAlias(true);
 		bg.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
 
 		Paint ar = new Paint();
 		ar.setColor(Color.WHITE);
 		ar.setAntiAlias(true);
 
 		Paint cp = new Paint();
 		cp.setColor(Color.BLACK);
 		cp.setTextSize(textsize);
 		cp.setStyle(Style.FILL);
 		cp.setAntiAlias(true);
 		cp.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
 
 		Paint fg = new Paint();
 		fg.setColor(Color.WHITE);
 		fg.setTextSize(textsize);
 		fg.setStyle(Style.FILL);
 		fg.setAntiAlias(true);
 		fg.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
 		fg.setTextAlign(Align.CENTER);
 
 		c.save();
 
 		// bg
 		c.drawRect(c.getClipBounds(), bg);
 
 		// debug
 		c.drawText(String.format("x: %.2f / y: %.2f",
 				currentRoom.player.position.getX(),
 				currentRoom.player.position.getY()), centerX, 80, fg);
 
 		// settings
 		int left = w - (h / 10);
 		int top = h - (h / 10);
 		int right = w - 10;
 		int bottom = h - 10;
 		activity.settingsBounds = new Rect(left, top, right, bottom);
 		c.drawRect(activity.settingsBounds, fg);
 		c.drawText("S", left + textsize / 2, top + textsize * 2, bg);
 
 		left = w - (h / 10) - activity.settingsBounds.width() - 10;
 		right = w - 10 - activity.settingsBounds.width() - 10;
 		activity.resetBounds = new Rect(left, top, right, bottom);
 		c.drawRect(activity.resetBounds, fg);
 		c.drawText("R", left + textsize / 2, top + textsize * 2, bg);
 
 		// compass
 		c.translate(centerX, centerY);
		c.rotate(-currentRoom.player.orientation - activity.orientationOffset);
 		c.drawCircle(0, 0, (w / 2) - 20, cp);
 		c.drawCircle(0, 0, (w / 2) - 30, bg);
 		c.drawText("N", 0, -(w / 2) + 20, cp);
 
 		// direction
 		Path arrow = new Path();
 		arrow.moveTo(0, 10);
 		arrow.lineTo(-20, 30);
 		arrow.lineTo(0, -30);
 		arrow.lineTo(20, 30);
 		arrow.close();
 
 		c.restore();
 		c.translate(centerX, centerY);
		c.rotate(90f
				- (float) Math.toDegrees(currentRoom.player
						.relativeOrientationFor(currentRoom.damsel).getAlpha())
				- activity.orientationOffset);
 		c.scale(5, 5);
 		c.drawPath(arrow, ar);
 
 		if (currentRoom != null) {
 			currentRoom.onRender(c);
 		}
 	}
 }
