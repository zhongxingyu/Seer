 package snake;
 
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
 	
 	public Snake(int startX, int startY, int addFoodLength, int lives, int idx) {
 		this.x = new int[MAX_LENGTH];
 		this.y = new int[MAX_LENGTH];
 		this.startX = startX;
 		this.startY = startY;
 		this.startLives = lives;
 		this.idx = idx;
 		this.addFoodLength = addFoodLength;
 		
 		init();
 	}
 	
 	private void init() {
 		this.x[0] = this.startX;
 		this.y[0] = this.startY;
 		this.length = 1;
 		this.ptr = length - 1;
 		this.score = 0;
 		
 		for (int i = 0; i < this.length; i++) {
 			Map.SetSnake(this.x[i], this.y[i]);
 		}
 	}
 	
 	public void Update() {
 		System.out.println("Update!");
 		int dir = SnakeMain.controller.getKeyPressed(this.idx);
 		if (dir == DirectionalController.UP && dy != 1) {
 			dx = 0;
 			dy = -1;
 		} else if (dir == DirectionalController.LEFT && dx != 1) {
 			dx = -1;
 			dy = 0;
 		} else if (dir == DirectionalController.DOWN && dy != -1) {
 			dx = 0;
 			dy = 1;
 		} else if (dir == DirectionalController.RIGHT && dx != -1) {
 			dx = 1;
 			dy = 0;
 		}
 		this.moveSnake();
 	}
 	
 	private void moveSnake() {
 		// Find first "displayed" snake bit at tail
 		int i;
 		for (i = (this.ptr + 1) % this.length; this.x[i] == -1; i = (i + 1) % this.length) {}
 		int oldX = this.x[i];
 		int oldY = this.y[i];
 		
 		int oldPtr = this.ptr;
 		this.ptr = (this.ptr + 1) % this.length;
 		this.x[this.ptr] = (this.x[oldPtr] + dx + Map.width) % Map.width;
 		this.y[this.ptr] = (this.y[oldPtr] + dy + Map.height) % Map.height;
 		
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
 		
 		// Assume that negative values won't fuck shit up.
 		// If it does, try impossibly large values.
 		// The idea here is that these are never rendered
 		// until this part of the area is written over.
 		for (int i = 0; i < this.addFoodLength; i++) {
 			this.x[i] = (this.y[i] = -1);
 		}
 		
 		this.length = newLength;
 		this.ptr = this.addFoodLength;
 	}
 	
 	public void Collide(byte item) {
 		System.out.println("Collided with " + item + "!");
 		switch (item) {
 			case Map.FOOD:
 			this.eat();
 			break;
 		}
 	}
 	
 	public void Die() {
		for (int i = this.ptr; i != this.ptr && this.x[i] != -1; i = (i + this.length - 1) % this.length) {
 			Map.Remove(this.x[i], this.y[i]);
 		}
 		
 		this.lives--;
 		this.init();
 	}
 	
 	public void render(Renderer renderer, int frameCount){
 		for(int i = 0; i < x.length; i++){
 			renderer.drawElement(x[i], y[i], frameCount, (byte)(10 + idx));
 		}
 	}
 }
