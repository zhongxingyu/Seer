 package com.benjaminlanders.taptorun.renderer;
 
 public class Cloud
 {
 	private float x, y;
 	private float speed;
 	
 	public Cloud(float y,float x, float speed)
 	{
 		this.speed = speed;
 		this.y = y;
 		this.x = x;
 	}
 	
 	public Cloud(float y, float speed)
 	{
 		this(y,(float) Math.random(),speed);
 	}
 	
 	public Cloud(float y)
 	{
 		this(y, -(float) Math.random()/8);
 	}
 	
 	public Cloud()
 	{
 		this((float) Math.random()/3 + .66f);
 	}
 	
 	public void update(float delta)
 	{
 		if(getX() + delta * speed < -.5f)
 		{
			x  = 1;//+ 1.5f;
 		}else
 		{
 			x += delta * speed;
 		}
 	}
 	
 	public float getX()
 	{
 		return x;
 	}
 	
 	public float getY()
 	{
 		return y;
 	}
 }
