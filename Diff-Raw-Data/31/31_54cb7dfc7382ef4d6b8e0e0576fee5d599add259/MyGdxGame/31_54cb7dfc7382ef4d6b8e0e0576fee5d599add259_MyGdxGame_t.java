 package com.me.mygdxgame;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.Texture.TextureFilter;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Intersector;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.utils.Array;
 import com.badlogic.gdx.utils.TimeUtils;
 
 public class MyGdxGame implements ApplicationListener 
 {
 
 	private SpriteBatch batch;
 	private Character bill;
 	public float deltaTime;
 	private FoxNews Fox;
 	private Liberals liberal;
 	private RectangleEx Enemy;// way better
 	private ArrayList <Liberals> DirtyDemocrats = new ArrayList<Liberals> (); // good comments
 	Random rand = new Random();
 	@Override
 	
 	public void create() 
 	{
 		batch = new SpriteBatch();
 		bill = new Character();
 		Fox = new FoxNews();
 		liberal = new Liberals();
 		Enemy =  new RectangleEx();
		for(int i = 0; i<100; i++)
 		{
 			Liberals l = new Liberals ();
			int x = rand.nextInt(1000);
 			int y = rand.nextInt(800);
 			l.setPosition(new Vector2(x,y));
 			DirtyDemocrats.add(l);
 		}
 		
 		
 	}
 	
 	public boolean recCollision(RectangleEx a, RectangleEx b)//collision detection
 	{
 		
 		boolean b1 = a.getMaxX() > b.x;
 		boolean b2 = a.x < b.getMaxX();
 		boolean b3 = a.getMaxY() > b.y;
 		boolean b4 = a.y < b.getMaxY();
 		
 		return b1 && b2 && b3 && b4;
 	}
 
 	@Override
 	public void dispose() 
 	{
 		batch.dispose();
 	}
 	
 	public void update()
 	{
 		deltaTime = Gdx.graphics.getDeltaTime();
 		
 		/////////////////////////\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
 		Vector2 dir = new Vector2(0,0);
 		//do game logic here
 		if(Gdx.input.isKeyPressed(Keys.A))
 		{
 			dir.add(-1, 0);
 		}
 		boolean isWPressed = Gdx.input.isKeyPressed(Keys.W);
 		if(isWPressed==true)
 		{
 			dir.add(0, 1);
 		}
 		boolean isSPressed = Gdx.input.isKeyPressed(Keys.S);
 		if(isSPressed==true)
 		{
 			dir.add(0, -1);
 		}
 		boolean isDPressed = Gdx.input.isKeyPressed(Keys.D);
 		if(isDPressed==true)
 		{
 			dir.add(1, 0);
 		}
 		
 		Vector2 tedV = dir.nor().mul(bill.speed);
 		Vector2 finalPos = bill.position.cpy().add(tedV.mul(deltaTime));
 		bill.setPosition(finalPos);
 		
 		
 		for(int i = 0; i<DirtyDemocrats.size(); i++)
 		{
 			Liberals l = DirtyDemocrats.get(i);
 			if(recCollision(l.rect, bill.rect))
 			{
 				DirtyDemocrats.remove(i);
 				i--;
 			}
 		}
 		
		
 	}
	
///////////////////////////////////\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
 	
 	@Override
 	public void render() 
 	{		
 		update();
 		Gdx.gl.glClearColor(1, 1, 1, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		
 		batch.begin();
 		
 			batch.draw(bill.image, bill.position.x, bill.position.y);
 			
 			batch.draw(Fox.image, Fox.x, Fox.y);
 			
 			
 
 			for(int i = 0; i<DirtyDemocrats.size(); i++)
 			{
 				Liberals l = DirtyDemocrats.get(i);
 				Liberals liberal = DirtyDemocrats.get(i);
 				batch.draw(liberal.image, l.position.x, l.position.y);
 			}
 			
 		batch.end();
 	}
 	
 	
 	
 	
 	@Override
 	public void resize(int width, int height) 
 	{
 	}
 
 	@Override
 	public void pause() 
 	{
 	}
 
 	@Override
 	public void resume() 
 	{
 	}
 }
