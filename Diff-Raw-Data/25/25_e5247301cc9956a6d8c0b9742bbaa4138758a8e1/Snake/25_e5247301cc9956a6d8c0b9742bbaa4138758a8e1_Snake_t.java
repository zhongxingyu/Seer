 import java.util.Random;
 import java.awt.Graphics;
 
 import core.*;
 public class Snake {
 	private static final int MAX_LENGTH = 1600;
 	
 	// Player info & metadata
 	public int score;
 	public int idx;
 	public int lives;
 	
 	// The physical snake
 	private int[] x;
 	private int[] y;
 	private int length;
 	private int ptr;
 	private int dx, dy;
 	private int addFoodLength;
 	
 	private int startX;
 	private int startY;
 	private int startLength;
 	private int startDX;
 	private int startDY;
 	private int startLives;
 	
 	public Snake(int startX, int startY, int lives, int idx) {
 		this.x = new int[MAX_LENGTH];
 		this.y = new int[MAX_LENGTH];
 		this.startX = startX;
 		this.startY = startY;
 		this.startLives = lives;
 		this.idx = idx;
 		
 		init();
 	}
 	
 	private void init() {
 		this.x[0] = this.startX;
 		this.y[0] = this.startY;
 		this.length = 1;
 		this.ptr = length - 1;
 		this.score = 0;
 	}
 	
 	public void Update() {
 		switch (SnakeMain.controller.getKeyPressed(this.idx)) {
 			case DirectionalController.UP:
 			System.out.println("UP");
 			dx = 0;
 			dy = -1;
 			break;
 			case DirectionalController.LEFT:
 			System.out.println("LEFT");
 			dx = -1;
 			dy = 0;
 			break;
 			case DirectionalController.DOWN:
 			System.out.println("DOWN");
 			dx = 0;
 			dy = 1;
 			break;
 			case DirectionalController.RIGHT:
 			System.out.println("RIGHT");
 			dx = 1;
 			dy = 0;
 			break;
 		}
 		System.out.println(this.length + ": " + this.x[this.ptr] + ", " + this.y[this.ptr]);
 		// System.out.println(this.x[this.ptr] + ", " + this.y[this.ptr]);
 		this.moveSnake();
 	}
 	
 	private void moveSnake() {
 		int oldPtr = this.ptr;
		int oldX = this.x[oldPtr];
		int oldY = this.y[oldPtr];
 		// System.out.println(oldPtr);
 		this.ptr = (this.ptr + 1) % this.length;
 		// System.out.println(this.ptr);
 		this.x[this.ptr] = (this.x[oldPtr] + dx + Map.width) % Map.width;
 		this.y[this.ptr] = (this.y[oldPtr] + dy + Map.height) % Map.height;
 		// System.out.println(this.ptr + " " + this.x[this.ptr] + " " + this.y[this.ptr]);
		Map.MoveFromTo(oldX, oldY, this.x[this.ptr], this.y[this.ptr], this);
 	}
 	
 	private void eat() {
 		int newLength = this.length + this.addFoodLength;
 		int[] tmpX = new int[newLength];
 		int[] tmpY = new int[newLength];
 		
 		for (int i = this.addFoodLength; i < newLength; i++) {
 			tmpX[i] = this.x[(this.ptr + i - this.addFoodLength) % this.length];
 			tmpY[i] = this.y[(this.ptr + i - this.addFoodLength) % this.length];
 		}
 		
 		for (int i = this.addFoodLength; i < newLength; i++) {
 			this.x[i] = tmpX[i];
 			this.y[i] = tmpY[i];
 		}
 		
 		int dx = this.x[newLength - 1] - this.x[newLength];
 		int dy = this.y[newLength - 1] - this.y[newLength];
 		
 		this.x[0] = this.x[newLength] + dx;
 		this.y[0] = this.y[newLength] + dy;
 		
 		for (int i = 1; i < this.addFoodLength; i++) {
 			this.x[i] = this.x[i - 1] + dx;
 			this.y[i] = this.y[i - 1] + dx;
 		}
 		
 		this.length = newLength;
 		this.ptr = this.addFoodLength;
 	}
 	
 	public void Collide(byte item) {
 		System.out.println(item);
 		switch (item) {
 			case Map.FOOD:
 			this.eat();
 			break;
 		}
 	}
 	
 	public void Die() {
 		this.lives--;
 		this.init();
 	}
 	public void render(Renderer renderer, int frameCount){
 		for(int i = 0; i < x.length; i++){
 							renderer.drawElement(x[i], y[i], frameCount, (byte)(10 + idx));
 						}
 	}
 }
