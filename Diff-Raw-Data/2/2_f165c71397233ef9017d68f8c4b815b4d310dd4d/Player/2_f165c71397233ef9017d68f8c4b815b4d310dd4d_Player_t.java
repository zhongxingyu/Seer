 package com.turbonips.troglodytes.entities;
 
 import java.awt.Point;
 import java.awt.Rectangle;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 
 public class Player extends Entity {
 	private Point position;
 	private Point slidingPosition;
 	private final Rectangle slidingBox;
 	private Image playerImage;
 	private int speed;
 	
 	public Player(String spritePath, Point position, int speed) throws SlickException {
 		playerImage = new Image(spritePath);
		slidingBox = new Rectangle(speed*-15, speed*-10, speed*15, speed*10);
 		setPosition(position);
 		setSlidingPosition(new Point(0,0));
 		setSpeed(speed);
 	}
 
 	@Override
 	public void render(GameContainer container, StateBasedGame game, Graphics g) {
 		playerImage.draw(container.getWidth()/2 + slidingPosition.x, container.getHeight()/2 + slidingPosition.y);
 	}
 	
 	public void drawLight(GameContainer container, Graphics g, int radius, int darkness) {
 		g.setColor(new Color(0,0,0,darkness));
 		g.fillOval(container.getWidth()/2 + slidingPosition.x + playerImage.getWidth()/2 - radius, container.getHeight()/2 + slidingPosition.y + playerImage.getHeight()/2 - radius, radius*2, radius*2);
 	}
 
 	@Override
 	public void update(GameContainer container, StateBasedGame game, int delta) {
 		Input input = container.getInput();
 		if (input.isKeyDown(Input.KEY_UP)) {
 			position.y -= speed;
 			if (slidingPosition.y > slidingBox.y) {
 				slidingPosition.y -= speed;
 			}
 		} else if (input.isKeyDown(Input.KEY_DOWN)) {
 			position.y += speed;
 			if (slidingPosition.y < slidingBox.height) {
 				slidingPosition.y += speed;
 			}
 		}
 		
 		if (input.isKeyDown(Input.KEY_LEFT)) {
 			position.x -= speed;
 			if (slidingPosition.x > slidingBox.x) {
 				slidingPosition.x -= speed;
 			}
 		} else if (input.isKeyDown(Input.KEY_RIGHT)) {
 			position.x += speed;
 			if (slidingPosition.x < slidingBox.width) {
 				slidingPosition.x += speed;
 			}
 		}
 		
 	}
 
 	public Point getPosition() {
 		return position;
 	}
 
 	public void setPosition(Point position) {
 		this.position = position;
 	}
 
 	public Point getSlidingPosition() {
 		return slidingPosition;
 	}
 
 	public void setSlidingPosition(Point slidingPosition) {
 		this.slidingPosition = slidingPosition;
 	}
 
 	public int getSpeed() {
 		return speed;
 	}
 
 	public void setSpeed(int speed) {
 		this.speed = speed;
 	}
 
 	public Rectangle getSlidingBox() {
 		return slidingBox;
 	}
 
 }
