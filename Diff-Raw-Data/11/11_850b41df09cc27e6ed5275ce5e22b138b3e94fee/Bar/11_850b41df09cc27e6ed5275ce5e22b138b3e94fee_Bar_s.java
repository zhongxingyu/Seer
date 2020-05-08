 package altuso.pong.enitities;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Rectangle;
 
 import altuso.pong.listener.HotKeyListener;
 import altuso.pong.main.Game;
 
 public class Bar extends Entity{
 
 	private int speed;
 	private int width, height;
 	private Rectangle boundingBox;
 	private boolean controlEnabled = false;
 	private boolean aiControledEnabled = false;
 	private int score = 0;
 	private Ball ball;
 	
 	public Bar(int x, int y, int width, int height) {
 		this.width = width;
 		this.height = height;
 		setPosition(x, y);
 		speed = 7;
 		boundingBox = new Rectangle(width, height);
 	}
 
 	@Override
 	public void update(double delta) {
 
 		boundingBox.setBounds(getX(), getY(), width, height);
 		if(controlEnabled) {
 			if(HotKeyListener.downIsPressed && HotKeyListener.upIsPressed) {
 
 			} else {
 				if(HotKeyListener.upIsPressed) {
 					yPos -= speed * delta;
 				} 
 				if(HotKeyListener.downIsPressed) {
 					yPos += speed * delta;
 				} 
 			}
 		}
 		if(aiControledEnabled) {
 			if(ball.getX() >= Game.width / 3) {
				if(yPos <= ball.getY()) {
 					yPos += speed * delta;
 				} 
				if(yPos >= ball.getY()) {
 					yPos -= speed * delta;
 				}
 			} else if(ball.getX() <= Game.width / 2) {
				if(yPos <= Game.height / 2) {
 					yPos += speed * delta;
 				} 
				if(yPos >= Game.height / 2) {
 					yPos -= speed * delta;
 				}
 			}
 			
 		}
 		if(yPos <= 0) {
 			yPos = 0;
 		} else if(yPos + height >= Game.height) {
 			yPos = Game.height - height;
 		}
 
 
 
 	}
 
 	@Override
 	public void render(Graphics g) {
 
 		g.setColor(Color.GREEN);
 		g.fillRect(getX(), getY(), width, height);
 	}
 
 	public Rectangle getBoundingBox() {
 		return boundingBox;
 	}
 
 	public void setControlEnabled(boolean controlEnabled) {
 		this.controlEnabled = controlEnabled;
 	}
 
 	public int getScore() {
 		return score;
 	}
 
 	public void addScore() {
 		score++;
 	}
 
 	public void setAiControledEnabled(boolean aiControledEnabled) {
 		this.aiControledEnabled = aiControledEnabled;
 	}
 	
 	public void setBall(Ball ball) {
 		this.ball = ball;
 	}
 
 
 }
