 package com.glowman.spaceunit.game.core;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.glowman.spaceunit.Assets;
 
 public class FPSViewer {
 	
	private static Boolean _show;
 	private static float _x;
 	private static float _y;
 	private static TextBounds _bounds;
 	private static BitmapFont _font;
 	private static String _fps;
 	
	public static void init(Boolean show) {
		_show = show;
		if (_show == false) { return; }
		
		_font = new BitmapFont(Gdx.files.internal("fonts/newfont.fnt"), Assets.myfont, false);
 		_font.setColor(Color.RED);
		_font.setScale(1f/Assets.pixelDensity);
 		_x = 0;
 		_y = 0;
 	}
 	
 	public static void draw(SpriteBatch spriteBatch) {
		if (_show == false) { return; }
		
 		_fps = "fps: " + Gdx.graphics.getFramesPerSecond();
 		if (_bounds == null) {
 			_bounds = _font.getBounds(_fps);
 		}
		_y = Assets.VIRTUAL_HEIGHT;
 		_font.draw(spriteBatch, _fps, _x, _y);
 	}
 }
