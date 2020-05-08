 package com.game.pong;
 
 import java.util.Random;
 
 import android.graphics.Canvas;
 import android.graphics.Paint;
 
 import com.game.math.Vector2;
 
 public class Ball {
 
 	Random rn = new Random();
 	
 	//position
 	private Vector2 m_position;
 	
 	//velocity
 	private Vector2 m_velocity;
 	
 	//ball speed
 	private float m_ballSpeed;
 	//radius
 	private float m_radius;
 	
 	//color
 	private int m_color;
 
 
 	public Ball(Vector2 pos, float radius, int color)
 	{
 		m_color = color;
 		setPosition(new Vector2());
 		setVelocity(new Vector2());
 		setPosition(pos);
 		setRadius(radius);
 
 	}
 
 	public void resetBall(Vector2 start)
 	{
 		m_position.x = (start.x /2);
 		m_position.y = (start.y /2);
 
 		int range = 1 - -1;
 		int randomNum =  rn.nextInt(range);
 		if(randomNum == 0) randomNum = -1;
 		setVelocity(0, randomNum * 10);
 	}
 
 	public void setPosition(int x, int y)
 	{
 		m_position.set(x, y);
 	}
 
 	public void setPosition(Vector2 pos)
 	{
 		this.m_position = pos;
 	}
 
 	public Vector2 getPosition()
 	{
 		return m_position;
 	}
 
 	public float getRadius() {
 		return m_radius;
 	}
 
 	public void setRadius(float m_radius) {
 		this.m_radius = m_radius;
 	}
 
 	public void update(double elapsed)
 	{
 		m_position.x += m_velocity.x * m_ballSpeed * elapsed;
 		m_position.y += m_velocity.y * m_ballSpeed * elapsed;
 
 	}
 
 	public Vector2 getVelocity() {
 		return m_velocity;
 	}
 
 	public void inverseX()
 	{
 		m_velocity.x = -m_velocity.x;

 	}
 
 	public void inverseY()
 	{
 		m_velocity.y = -m_velocity.y;
 	}
 
	public void inverseVelocity()
	{
		m_velocity.y = -m_velocity.y;
		m_velocity.x = -m_velocity.x;

		System.out.println(m_ballSpeed);
	}

 	public void setVelocity(Vector2 m_velocity) {
 		this.m_velocity = m_velocity;
 	}
 
 	public void setVelocity(int x, int y) {
 		this.m_velocity.x = x;
 		this.m_velocity.y = y;
 	}
 	public void draw(Canvas canvas, Paint paint)
 	{
 		paint.setColor(m_color);
 		canvas.drawCircle(getPosition().x, getPosition().y, getRadius(), paint);
 	}
 
 	public float getSpeed() {
 		return m_ballSpeed;
 	}
 
 	public void setSpeed(float m_ballSpeed) {
 		this.m_ballSpeed = m_ballSpeed;
 	}
 
 }
