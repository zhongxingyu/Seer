 package com.doobs.java2d.test;
 
 import com.doobs.java2d.Game2D;
 import com.doobs.java2d.GameLoop;
 import com.doobs.java2d.gfx.Screen;
 import com.doobs.java2d.input.InputHandler;
 
 public class TestGame extends GameLoop{
	private static final int WIDTH = 320, HEIGHT = 240;
	private static final int SCALE = 2;
	private static final String TITLE = "TEST";
 	private static final boolean VSYNC = true;
 	
 	private Game2D game;
 	
 	private MoveCube cube;
 	
 	public TestGame() {
 		game = new Game2D(WIDTH, HEIGHT, SCALE, TITLE, this);
 		Bitmaps.setBitmapLoader(game.getBitmapLoader());
 		Bitmaps.init();
 		cube = new MoveCube();
 		game.setVSync(VSYNC);
 		game.start();
 	}
 	
 	public void tick(InputHandler input) {
 		cube.tick(input);
 	}
 
 	public void render(Screen screen) {
 		screen.fill(0xFFFF0000);
 		cube.render(screen);
 	}
 	
 	public void printFPS() {
 		System.out.println("FPS: " + game.getFps());
 	}
 	
 	public static void main(String[] args) {
 		new TestGame();
 	}
 }
