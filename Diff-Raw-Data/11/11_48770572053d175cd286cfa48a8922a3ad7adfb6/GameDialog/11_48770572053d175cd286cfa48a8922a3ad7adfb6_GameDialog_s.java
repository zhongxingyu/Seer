 package game;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.image.BufferStrategy;
 import java.util.ArrayList;
 import javax.swing.*;
 import obstacles.Obstacle;
 
 
 public class GameDialog implements KeyListener{
 	
 	public static final int FRAME_WIDTH = 600;
 	public static final int FRAME_HEIGHT = 600;
 	private static final int GROW_EVERY_NUM_LOOPS = 20;
 	
 	private int gameTime=0;
 	private int score;
 	private int loopCounter = 0;
 	
 	private SnakeModel snakeModel;
 	private SnakeBoundary snakeBoundary;
 	private SnakeSpriteManager snake;
 	private Wall wall;
 	
 	private boolean isUp, isDown, isLeft, isRight, isShrink = false;
 	
 	JFrame frame;
 	
 	// just a variable to check if the obstacle is
 	//inside the snake, change this when you get ur collision methods working
 	// true if an obstacle is captured, false otherwise
 	private boolean hasCaughtInside = false;
 	
 	public void initGame(){
 		snakeBoundary = new SnakeBoundary();
 		snakeModel = new SnakeModel();
 		snake = new SnakeSpriteManager();
 		wall = new Wall(10, snakeBoundary);
 		frame = new JFrame();
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		//frame.setUndecorated(true);
 		frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
 		frame.setIgnoreRepaint(true);
 		frame.setVisible(true);
 		frame.createBufferStrategy(2);
 		frame.requestFocus();
 		frame.addKeyListener(this);
 	}
 	
 	public static void main (String [ ] args) throws InterruptedException, GameOverException {
 		
 		GameDialog game = new GameDialog();
 		
 		game.initGame();
 		
 		try{
 			game.gameLoop();
 		}catch(GameOverException e){
 			
 		}
 		
 	}
 	
 	public void gameLoop() throws InterruptedException, GameOverException{
 		BufferStrategy bf = frame.getBufferStrategy();
 		Graphics g = null;
 		while(true){
 			try{
 				if(isUp) snake.moveDown();
 				if(isDown) snake.moveUp();
 				if(isRight) snake.moveRight();
 				if(isLeft) snake.moveLeft();
 				
 				g = bf.getDrawGraphics();
 				g.setColor(Color.gray);
 				g.fillRect(0, 0, frame.getWidth(), frame.getHeight());
 				for(Obstacle o: wall.getObstacles()) {
 					o.update();
 				}
 				// repaint
 				for(Obstacle o: wall.getObstacles()) {
 					o.draw(g);
 				}
 				//snake.grow();
 				snake.spin();
 				snake.draw(g);
 				
 				// autogrow snake
 				if (loopCounter % GROW_EVERY_NUM_LOOPS == 0)
 				{
 					snakeBoundary.grow();
 					snake.grow();
 					loopCounter = 1;			
 				}
 				else
 				{
 					loopCounter++;
 				}
 				gameTime++;
 				bf.show();
 				Thread.sleep(20);
 				g.dispose();
 			}finally{
 				if(g!=null) g.dispose();
 			}
 		}
 	}
 	
 	@Override
 	public void keyPressed(KeyEvent e) {
 		int code = e.getKeyCode();
 		if(code == KeyEvent.VK_UP){
 			isUp = true;
 		}
 		if(code == KeyEvent.VK_DOWN){
 			isDown = true;
 		}
 		if(code == KeyEvent.VK_LEFT){
 			isLeft = true;
 		}
 		if(code == KeyEvent.VK_RIGHT){
 			isRight = true;
 		}
 		if(code == KeyEvent.VK_A){
 			snake.shrink();
 			snakeBoundary.shrink();
 		}
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 		int code = e.getKeyCode();
 		if(code == KeyEvent.VK_UP){
 			isUp = false;
 		}
 		if(code == KeyEvent.VK_DOWN){
 			isDown = false;
 		}
 		if(code == KeyEvent.VK_LEFT){
 			isLeft = false;
 		}
 		if(code == KeyEvent.VK_RIGHT){
 			isRight = false;
 		}
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 }
