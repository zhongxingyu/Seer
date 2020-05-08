 package gfx;
 
 public class BatAnimation {
 	protected int frames, states;
 	protected float delay;
 	protected BatBitmap[][] sheet;
 	private int currentFrame, currentState;
 	private float fps;
 	private boolean oneShot = false;
 	private boolean finished = false;
 	
 	public static final int ONE_SHOT = 0;
 	public static final int LOOP = 1;
 	
 	public BatAnimation(BatBitmap[][] spriteSheet, int fps, int animType){
 		this.states = spriteSheet[0].length;
 		this.frames = spriteSheet.length;
 		this.sheet = spriteSheet;
 		currentFrame = 0;
 		currentState = 0;
 		delay = 0;
 		setFps(fps);
 		
 		switch(animType) {
 		case 0:
 			oneShot = true;
 			break;
 		case 1:
 			oneShot = false;
 		
 		}
 	}
 	
 	
 	public void setFps(int fps) {
 		this.fps = 1000.0f/fps;
 	}
 	
 	public BatBitmap getGraphics() {
 		return sheet[currentFrame][currentState];
 	}
 	
 	public void stepAnimation(float dt) {
 		delay += dt;
 		while (delay > fps) {
 			delay -= fps;
 			incFrame();
 		}
 	}
 	
 	private void incFrame() {
 		currentFrame++;
 		if(currentFrame >= frames) {
 			if (oneShot) {
 				finished = true;
 				currentFrame--;
 			} 
 			else {
 				currentFrame %= frames;
 			}
 		}
 	}
 	
 	public int getNumStates() {
 		return states;
 	}
 	
 	public void setState(int state) {
 		this.currentState = state;
 		this.currentFrame = 0;
 	}
 	
 	
 }
