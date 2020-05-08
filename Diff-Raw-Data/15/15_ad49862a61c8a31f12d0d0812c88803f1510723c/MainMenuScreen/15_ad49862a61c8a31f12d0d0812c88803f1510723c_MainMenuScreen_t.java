 package com.teamBasics.CollegeTD;
 
 import java.util.List;
 
 import com.teamBasics.framework.Game;
 import com.teamBasics.framework.Graphics;
 import com.teamBasics.framework.Screen;
 import com.teamBasics.framework.Input.TouchEvent;
 
 public class MainMenuScreen extends Screen {
 	public MainMenuScreen(Game game) {
 		super(game);
 	}
 
 	@Override
 	public void update(float deltaTime) {
 		Graphics g = game.getGraphics();
 		List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
 
 		int len = touchEvents.size();
 		for (int i = 0; i < len; i++) {
 			TouchEvent event = touchEvents.get(i);
 			if (event.type == TouchEvent.TOUCH_UP) {
 
				if (inBounds(event, 495, 60, 210, 25)) {
 					game.setScreen(new GameScreen(game));
 				}
 
 			}
 		}
 	}
 
 	// Check if keypress is within certain boundaries
 	private boolean inBounds(TouchEvent event, int x, int y, int width,
 			int height) {
 		if (event.x > x && event.x < x + width - 1 && event.y > y
 				&& event.y < y + height - 1)
 			return true;
 		else
 			return false;
 	}
 
 	@Override
 	public void paint(float deltaTime) {
 		Graphics g = game.getGraphics();
 		g.drawImage(Assets.menu, 0, 0);
 	}
 
 	@Override
 	public void pause() {
 	}
 
 	@Override
 	public void resume() {
 
 	}
 
 	@Override
 	public void dispose() {
 
 	}
 
 	@Override
 	public void backButton() {
 		android.os.Process.killProcess(android.os.Process.myPid());
 
 	}
 }
