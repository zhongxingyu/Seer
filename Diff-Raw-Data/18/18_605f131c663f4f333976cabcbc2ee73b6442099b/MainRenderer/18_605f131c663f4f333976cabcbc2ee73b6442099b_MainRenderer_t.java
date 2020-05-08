 package com.example.spaceshipgame.renderer;
 
 import android.annotation.TargetApi;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Point;
 import android.graphics.Rect;
 import android.os.Build;
 import android.view.Display;
 import android.view.WindowManager;
 
 import com.example.spaceshipgame.R;
 import com.example.spaceshipgame.model.*;
 
 @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
 public class MainRenderer {
 	public static final Point	MARGIN	= new Point(100, 50);
 	private Point				screenSize;
 	private Point				mapCenter;
 	private Bitmap				background;
 
 	ElementRendererFactory		elementRendererFactory;
 	Context						context;
 
 	public MainRenderer(Context context_) {
 		context = context_;
 		elementRendererFactory = new ElementRendererFactory(context_);
 
 		screenSize = new Point();
 
 		WindowManager wm = (WindowManager) context
 				.getSystemService(Context.WINDOW_SERVICE);
 		Display display = wm.getDefaultDisplay();
 		display.getSize(screenSize);
 
 		mapCenter = new Point(screenSize.x / 2, screenSize.y / 2);
		createBackground();
 	}
 
 	public void render(Canvas canvas, GameState gameState) {
 		updateMapCenter(gameState);
 		renderMap(canvas, gameState);
 		for (Player player : gameState.players) {
 			for (Element element : player.elements) {
 				ElementRenderer elementRenderer = elementRendererFactory
 						.getElementRenderer(element);
 				elementRenderer.render(canvas, element, mapCenter, screenSize);
 			}
 		}
 	}
 
 	private void createBackground() {
 		Bitmap bmp = BitmapFactory.decodeResource(context.getResources(),
 				R.drawable.background);
 
		Bitmap bmOverlay = Bitmap.createBitmap(screenSize.x * 2,
				screenSize.y * 2, Bitmap.Config.ARGB_8888);
		Rect src = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
 		Canvas canvas = new Canvas(bmOverlay);
		canvas.drawBitmap(bmp, src, new Rect(screenSize.x*0, screenSize.y*0, screenSize.x*1, screenSize.y*1), null);
		canvas.drawBitmap(bmp, src, new Rect(screenSize.x*0, screenSize.y*1, screenSize.x*1, screenSize.y*2), null);
		canvas.drawBitmap(bmp, src, new Rect(screenSize.x*1, screenSize.y*0, screenSize.x*2, screenSize.y*1), null);
		canvas.drawBitmap(bmp, src, new Rect(screenSize.x*1, screenSize.y*1, screenSize.x*2, screenSize.y*2), null);
 		background = bmOverlay;
 	}
 
 	private void updateMapCenter(GameState gameState) {
 		Spaceship currentPlayerSpaceship = gameState.currentInstancePlayer.spaceship;
 		int x = (int) currentPlayerSpaceship.getPosition().getX();
 		int y = (int) currentPlayerSpaceship.getPosition().getY();
 
 		if (mapCenter.x + screenSize.x / 2 - MARGIN.x < x) {
 			mapCenter.x = x - screenSize.x / 2 + MARGIN.x;
 		} else if (mapCenter.x - screenSize.x / 2 + MARGIN.x > x) {
 			mapCenter.x = x + screenSize.x / 2 - MARGIN.x;
 		}
 
 		if (mapCenter.y + screenSize.y / 2 - MARGIN.y < y) {
 			mapCenter.y = y - screenSize.y / 2 + MARGIN.y;
 		} else if (mapCenter.y - screenSize.y / 2 + MARGIN.y > y) {
 			mapCenter.y = y + screenSize.y / 2 - MARGIN.y;
 		}
 	}
 
 	private void renderMap(Canvas canvas, GameState gameState) {
 		Point mapSize = gameState.map.size;
 		int left, top;
 
 		if (mapCenter.x - screenSize.x / 2 < 0)
 			left = 0;
 		else if (mapCenter.x + screenSize.x / 2 > mapSize.x)
 			left = mapSize.x - screenSize.x;
 		else
 			left = mapCenter.x - screenSize.x / 2;
 
 		if (mapCenter.y - screenSize.y / 2 < 0)
 			top = 0;
 		else if (mapCenter.y + screenSize.y / 2 > mapSize.y)
 			top = mapSize.y - screenSize.y;
 		else
 			top = mapCenter.y - screenSize.y / 2;
 
 		left %= background.getWidth() / 2;
 		top %= background.getHeight() / 2;
 
 		Rect src = new Rect(left, top, left + background.getWidth() / 2, top
 				+ background.getHeight() / 2);
 		Rect dst = new Rect(0, 0, screenSize.x, screenSize.y);
 		canvas.drawBitmap(background, src, dst, null);
 	}
 }
