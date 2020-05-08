 package main;
 
 import static org.lwjgl.input.Keyboard.*;
 import graphics.Graphics;
 
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 
 import audio.Audio;
 
 public class Input {
 	
	public static float relX = 0, relY = 0;
 	public static float absX = 0, absY = 0;
 	
 	static final float mouseSensitivity = 100;
 	
 	public static KeyInfo[] keyboardInfo = new KeyInfo[KEYBOARD_SIZE];
 	
 	public static void init() {
 		//Mouse.setGrabbed(true);
 		enableRepeatEvents(true);
 		for(int i = 0; i < keyboardInfo.length; i++) {
 			if(Keyboard.getKeyName(i) != null) {
 				keyboardInfo[i] = KeyInfo.getKeyInfo(); System.out.println(Keyboard.getKeyName(i));
 			}
 		}
 	}
 	
 	public static void mouseUpdate() {
		relX = (Mouse.getDX())/mouseSensitivity;
		relY = (Mouse.getDY())/mouseSensitivity;
 		absX = Mouse.getX()/Graphics.WIDTH;
 		absY = Mouse.getY()/Graphics.HEIGHT;
 		
 		//Graphics.camPos.x = (float) (Graphics.camDist * Math.cos(x) * Math.sin(y) + Main.playerEntity.pos.x);
 		//Graphics.camPos.y = (float) (Graphics.camDist * Math.cos(y) + Main.playerEntity.pos.y);
 		//Graphics.camPos.z = (float) (Graphics.camDist * Math.sin(x) * Math.sin(y) + Main.playerEntity.pos.z);
 
 		//Main.playerEntity.rot.y = (float) -(x*180f/Math.PI+90f);
 		
 		if(Mouse.isButtonDown(0)) {
 			Audio.playAtPlayer("pootis.wav");
 			//Main.entities.add(new EntityProjectile(new Vector3f(Main.playerEntity.pos.x, Main.playerEntity.pos.y, Main.playerEntity.pos.z)));
 		}
 		if(Mouse.isButtonDown(1)) {
 		//	Graphics.takeScreenShot();
 		}
 	}
 	
 	public static void keyboardUpdate() {
 		poll();
 		while(next()) {
 			int keyID = getEventKey();
 			keyboardInfo[keyID].nanosPressed = (int) getEventNanoseconds();
 			keyboardInfo[keyID].state = getEventKeyState();
 		}
 		/*
 		if(keyboardInfo[KEY_W].state) {
 			Main.playerEntity.pos.x -= (float) (Math.cos(x) * Math.sin(y))*speed;
 			Main.playerEntity.pos.y -= (float) (Math.cos(y))*speed;
 			Main.playerEntity.pos.z -= (float) (Math.sin(x) * Math.sin(y))*speed;
 		}
 		if(keyboardInfo[KEY_S].state) {
 			Main.playerEntity.pos.x += (float) (Math.cos(x) * Math.sin(y))*speed;
 			Main.playerEntity.pos.y += (float) (Math.cos(y))*speed;
 			Main.playerEntity.pos.z += (float) (Math.sin(x) * Math.sin(y))*speed;
 		}
 		if(keyboardInfo[KEY_Q].state) {
 			Graphics.camUp.x-=.1f;
 			Graphics.camUp.z-=.1f;
 		}
 		if(keyboardInfo[KEY_E].state) {
 			Graphics.camUp.x+=.1f;
 			Graphics.camUp.z+=.1f;
 		}
 		if(keyboardInfo[KEY_C].state && speed < 10) {
 			speed+=.01f;
 		}
 		if(keyboardInfo[KEY_Z].state && speed > .02f) {
 			speed-=.01f;
 		}
 		if(keyboardInfo[KEY_Z].state && keyboardInfo[KEY_C].state) {
 			speed = 5;
 		}
 		if(keyboardInfo[KEY_1].state) {
 			Graphics.camDist-=speed;
 		}
 		if(keyboardInfo[KEY_3].state) {
 			Graphics.camDist+=speed;
 		}
 		if(keyboardInfo[KEY_LEFT].state) {
 			Graphics.FoV-=speed;
 		}
 		if(keyboardInfo[KEY_RIGHT].state) {
 			Graphics.FoV+=speed;
 		}
 		if(keyboardInfo[KEY_ESCAPE].state) {
 			Main.RUNNING = false;
 		}
 		if(keyboardInfo[KEY_F1].state) {
 			Graphics.toggleFullScreen();
 			Mouse.setCursorPosition(Graphics.WIDTH/2, Graphics.HEIGHT/2);
 			x = .01f; y = 3.14f;
 			Mouse.setGrabbed(true);
 		}
 		if(keyboardInfo[KEY_BACK].state) {
 			Main.loadLevel();
 		}
 		if(keyboardInfo[KEY_RETURN].state) {
 			Main.playerEntity.health = (Main.playerEntity.health >= 0) ? Main.playerEntity.health -.001f*Input.speed : 1;
 		}
 		*/
 	}
 
 	public static class KeyInfo {
 		
 		public int nanosPressed;
 		public boolean state;
 		
 		public KeyInfo() {
 			
 		}
 		
 		public static KeyInfo getKeyInfo() {
 			return new KeyInfo();
 		}
 		
 	}
 }
