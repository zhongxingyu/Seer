 package lh.koneke.games.lwjglplatformer;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import lh.koneke.thomas.framework.Rectangle;
 import lh.koneke.thomas.framework.SoundManager;
 import lh.koneke.thomas.framework.Vector2f;
 import lh.koneke.thomas.graphics.AnimationFrame;
 
 
 /*animation file
  * -name;
  * x,y time( sound);x,y time( sound);...
  * 
  * example
  * -walking
  * 32,0 250 footstep;
  * 64,0 250;
  */
 
 public class AnimationManager {
 	Map<String, List<AnimationFrame>> animations = new HashMap<String, List<AnimationFrame>>();
 	String currentAnimation;
 	private List<AnimationFrame> currentFrames;
 	private int currentFrame;
 	int timer; //ms passed
 	Vector2f frameSize = new Vector2f(32,32);
 	public static SoundManager sm;
 	
 	public void startAnimation(String animation) {
 		currentAnimation = animation;
 		setCurrentFrames(animations.get(animation));
 		setCurrentFrame(-1);
 		advance();
 		timer = 0;
 	}
 	
 	public String getAnimation() {
 		return currentAnimation;
 	}
 	
 	public void Update(int dt) {
 		if (getCurrentFrames().get(getCurrentFrame()).getDisplayTime() == -1) return;
 		timer += dt;
 		while (timer > getCurrentFrames().get(getCurrentFrame()).getDisplayTime()) {
 			timer -= getCurrentFrames().get(getCurrentFrame()).getDisplayTime();
 			advance();
 		}
 	}
 	
 	void advance() {
 		setCurrentFrame((getCurrentFrame() + 1) % getCurrentFrames().size());
 		
 		String sound = currentFrames.get(getCurrentFrame()).getSound();
 		if(sound != null) {
 			sm.play(sound);
 		}
 	}
 	
 	public void addFrameToAnimation(String animation, Vector2f framePosition ,int displayTime) {
 		if(!animations.containsKey(animation)) {
 			animations.put(animation, new ArrayList<AnimationFrame>());
 		}
 		animations.get(animation).add(new AnimationFrame(framePosition, displayTime, null));
 		if(animation == currentAnimation) { startAnimation(currentAnimation); }
 	}
 	
 	public void load(String path) {
 		BufferedReader br;
 		try {
 			br = new BufferedReader(new FileReader(path));
 			String s, sound = null, readAnimation = null;
 			int x,y,time;
 			
 			while((s = br.readLine()) != null) {
 				if(s.substring(0,1).equals(":")) {
 					readAnimation = s.substring(1, s.length());
 					animations.put(readAnimation, new ArrayList<AnimationFrame>());
 				}
 				else {
 					String[] entries = s.split(" ");
 					x = Integer.parseInt(entries[0].split(",")[0]);
 					y = Integer.parseInt(entries[0].split(",")[1]);
 					
 					if(entries[1].equals("-")) {
 						time = -1;
 					} else {
 						time = Integer.parseInt(entries[1]);
 					}
 					
 					if(entries.length > 2 ) {
 						sound = entries[2];
 					} else { sound = null; }
 					
					animations.get(readAnimation).add(new AnimationFrame(new Vector2f(x,y), time, sound));
 					System.out.println("Added "+x+","+y+" (s "+sound+") to "+readAnimation);
 				}
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public List<AnimationFrame> getCurrentFrames() {
 		return currentFrames;
 	}
 
 	public void setCurrentFrames(List<AnimationFrame> currentFrames) {
 		this.currentFrames = currentFrames;
 	}
 
 	public int getCurrentFrame() {
 		return currentFrame;
 	}
 	
 	public Rectangle getFrame() {
 		return new Rectangle(currentFrames.get(currentFrame).getFrame(), frameSize);
 	}
 
 	public void setCurrentFrame(int currentFrame) {
 		this.currentFrame = currentFrame;
 	}
 	
 	/*
 	public float hate() {
 		return 387.44f; //million miles;
 	}
 	*/
 }
