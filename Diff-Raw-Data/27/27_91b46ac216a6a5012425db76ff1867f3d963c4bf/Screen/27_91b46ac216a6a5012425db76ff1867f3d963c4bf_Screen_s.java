 package com.jfboily.fxgea;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.xml.sax.EntityResolver;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.util.Log;
 
 
 public abstract class Screen
 {
 	// les sprites
 	private ArrayList<Sprite>[] sprites;
 	
 	// states
 	private int curState;
 	private boolean newState;
 	private boolean tempoNewState;
 	
 	// planes
 	public static final int PLANE_0 = 0;
 	public static final int PLANE_1 = 1;
 	public static final int PLANE_2 = 2;
 	public static final int PLANE_3 = 3;
 	public static final int PLANE_4 = 4;
 
 	private Fade fader = null;
 	private int fade;
 	
 	// background
 	private Bitmap bitmapBG = null;
 	
 	
 	@SuppressWarnings("unchecked")
 	public Screen(Game game) 
 	{
 		// objects to track
 		sprites = (ArrayList<Sprite>[]) new ArrayList[5];
 		for(int i = 0 ; i < sprites.length; i++)
 		{
 			sprites[i] = new ArrayList<Sprite>(100);
 		}
 		
 		tempoNewState = true;
 		fade = 255;
 		
 		game.getInput().unregisterAllTouchables();
 	}
 	
 	
 	public void render(Canvas c)
 	{
 		if(bitmapBG != null)
 		{
 			c.drawBitmap(bitmapBG, 0, 0, null);
 		}
 		else
 		{
 			c.drawColor(Color.BLACK);
 		}
 	
		// dessine les sprites, plane 4 --> plane 0
 		
 		for(int i = sprites.length - 1; i >= 0; i--)
 		{
 			for(int j = 0; j < sprites[i].size(); j++)
 			//for(Sprite j : sprites[i])
 			{
 				sprites[i].get(j).draw(c);
 			}
 		}
		
 		// appelle la methode draw definie par la classe utilisateur
 		draw(c);
 	}
 
 
 	public void superUpdate(long currentTime, long deltaTime)
 	{
 		newState = tempoNewState;
 		tempoNewState = false;
 		
 		updateFader(deltaTime);
 		update(curState, newState, currentTime, deltaTime);
 	}
 
 	
 	public Sprite createSprite(String fname, int frameW, int frameH, int plane)
 	{
 		Sprite s = new Sprite(fname, frameW, frameH);
 		addSprite(s, plane);
 		return s;
 	}
 	
 	public Sprite createSprite(String fname, int plane)
 	{
 		Sprite s = new Sprite(fname);
 		addSprite(s, plane);
 		return s;
 	}
 	
 	public Sprite createSprite(String fname, int frameW, int frameH, int plane, Sprite.RefPixel refPixel)
 	{
 		Sprite s = new Sprite(fname, frameW, frameH, refPixel);
 		addSprite(s, plane);
 		return s;
 	}
 	
 	public Sprite createSprite(String fname, int plane, Sprite.RefPixel refPixel)
 	{
 		Sprite s = new Sprite(fname, refPixel);
 		addSprite(s, plane);
 		return s;
 	}
 	
 	private void addSprite(Sprite s, int plane)
 	{
 		if(plane >= 0 && plane < sprites.length)
 		{
 			sprites[plane].add(s);
 		}
 		else
 		{
 			throw new IllegalArgumentException("Invalid sprite plane : "+plane);
 		}		
 	}
 	
 	public void deleteSprite(Sprite sprite)
 	{
 		for(int i = 0; i < sprites.length; i++)
 		{
 			if(sprites[i].contains(sprite))
 			{
 				sprites[i].remove(sprite);
 			}
 		}
 	}
 	
 	public void deleteAllSprites()
 	{
 		for(int i = 0; i < sprites.length; i++)
 		{
 			sprites[i].clear();
 		}
 	}
 	
 	public int getState()
 	{
 		return curState;
 	}
 	
 	public void setState(int state)
 	{
 		if(state == curState)
 			return;
 		
 		curState = state;
 		tempoNewState = true;
 	}
 	
 	
 	// Méthodes abstraites
 	public abstract void update(int state, boolean newState, long currentTime, long deltaTime);
 	
 	public abstract void draw(Canvas canvas);
 	
 	public abstract void dispose();
 	
 	public static enum FadeType
 	{
 		FADE_IN,
 		FADE_OUT,
 	}
 	
 	public void fadeIn(long ms)
 	{
 		if(fader == null)
 		{
 			fader = new Fade(FadeType.FADE_IN, ms);
 			fade = 0;
 		}
 	}
 	
 	public void fadeOut(long ms)
 	{
 		if(fader == null)
 		{
 			fader = new Fade(FadeType.FADE_OUT, ms);
 			fade = 255;
 		}
 	}
 	
 	private class Fade
 	{
 		private FadeType type;
 		private float delta;
 		private int value;
 		private boolean done;
 		
 		public Fade(FadeType type, long ms)
 		{
 			this.type = type;
 			delta = (float)255 / (float)ms;
 			done = false;
 			
 			if(type == FadeType.FADE_IN)
 			{
 				value = 0;
 			}
 			else
 			{
 				value = 255;
 			}
 		}
 		
 		public int run(long deltaTime)
 		{
 			int f = (int)(delta * deltaTime);
 			if(type == FadeType.FADE_IN)
 			{
 				value += f;
 				if(value > 255)
 				{
 					value = 255;
 					done = true;
 				}
 			}
 			else
 			{
 				value -= f;
 				if(value < 0)
 				{
 					value = 0;
 					done = true;
 				}
 			}
 			
 			return value;
 		}
 		
 		public boolean done()
 		{
 			return done;
 		}
 		
 	}
 	
 	private void updateFader(long deltaTime)
 	{
 		if(fader != null)
 		{
 			fade = fader.run(deltaTime);
 			if(fader.done())
 			{
 				fader = null;
 			}
 		}
 	}
 	
 	public int getFadeValue()
 	{
 		return fade;
 	}
 	
 	public boolean isFading()
 	{
 		return fader != null;
 	}
 	
 	public void createStaticBG(String fname)
 	{	
 		try
 		{
 			InputStream is = Game.getGame().getAssets().open(fname);
 			bitmapBG = BitmapFactory.decodeStream(is);
 		}
 		catch(IOException e)
 		{
 			Log.e("Screen:createStaticBG", "Impossible d'ouvrir le fichier "+fname);
 		}
 	}
 	
 	public void setBackground(Bitmap bitmap)
 	{
 		bitmapBG = bitmap;
 	}
 	
 	public void createStaticTiledBG(String tilEDfilename)
 	{
 
 	}
 }
