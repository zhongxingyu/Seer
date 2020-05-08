 package com.doobs.java2d.test;
 
 import java.awt.event.KeyEvent;
 
 import com.doobs.java2d.Game2D;
 import com.doobs.java2d.GameLoop;
 import com.doobs.java2d.gfx.Screen;
 import com.doobs.java2d.input.InputHandler;
 
 public class TestGame extends GameLoop{
 	private static final int WIDTH = 160, HEIGHT = 120;
 	private static final int SCALE = 4;
 	private static final String TITLE = "Test Game";
	private static final boolean PRINT_FPS = true;
 	private static final boolean VSYNC = false;
 	
 	private MoveCube cube;
 	
 	public TestGame() {
 		game = new Game2D(WIDTH, HEIGHT, SCALE, TITLE, this);
 		Bitmaps.init(game.getBitmapLoader());
 		cube = new MoveCube();
		game.setPrintFPS(PRINT_FPS);
 		game.setVSync(VSYNC);
 		game.start();
 	}
 	
 	public void tick(InputHandler input) {
 		if(input.keys[KeyEvent.VK_SPACE])
 			game.stopInput(30);
 		cube.tick(input);
 	}
 
 	public void render(Screen screen) {
 		screen.fill(0xFF0F0000);
 		cube.render(screen);
 	}
 	
 	public void printFPS() {
 		
 	}
 	
 	public static void main(String[] args) {
 		new TestGame();
 	}
 }
