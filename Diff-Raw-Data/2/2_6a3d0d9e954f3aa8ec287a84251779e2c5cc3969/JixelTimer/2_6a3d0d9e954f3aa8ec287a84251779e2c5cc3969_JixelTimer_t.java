 package jixel.stage;
 
 public class JixelTimer {
 
 	private double fpsNS;
 	private long timer;
 	private long lastTime;
 	private long now;
 	private double deltaFps;
 	private int frames;
 	
 	private long bootTime;
 	
 	private final String GAME_TITLE;
 	
 	public JixelTimer(String gameTitle){
 		GAME_TITLE = gameTitle;
 	}
 	
	public void startTimer(int fps) {
 		bootTime = System.currentTimeMillis();
 		
 		fpsNS = 1000000000.0 / fps;
 
 		timer = System.currentTimeMillis();
 		lastTime = System.nanoTime();
 
 		deltaFps = 0;
 		frames = 0;
 
 		now = System.nanoTime();
 	}
 
 	public void updateTime() {
 
 		if (System.currentTimeMillis() - timer > 1000) {
 			timer += 1000;
 			JixelGame.getScreen().setTitle(GAME_TITLE + "Fps: " + frames);
 			frames = 0;
 		}
 
 		now = System.nanoTime();
 		deltaFps += (now - lastTime) / fpsNS;
 		lastTime = now;
 	}
 
 	public boolean timeForFrame() {
 		if (deltaFps >= 1) {
 			frames++;
 			deltaFps--;
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	public long getBootTime(){
 		return bootTime;
 	}
 }
