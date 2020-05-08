 package com.nauka;
 
 import org.anddev.andengine.entity.IEntity;
 import org.anddev.andengine.entity.modifier.MoveYModifier;
 import org.anddev.andengine.entity.sprite.Sprite;
 import org.anddev.andengine.opengl.texture.region.TextureRegion;
 
 import android.hardware.SensorEvent;
 
 public class Samolot 
 {
 	MainActivity act;
 	 float x,y;
 	 static int w,h;
 	 static int typ = 0;
 	 static Sprite tex;
 	 Sprite[] bullets;
 	 boolean[] isattached;
 	 
 	 float speed;
 	 static int ilosc_karabinow;
 	 static Sprite pocisk;
 	 static TextureRegion pocisktex;
 	 boolean czymoznastrzelac = true;
 	Samolot( int typ)
 	{
 		act = MainActivity.getSharedInstance();
 		w = act.WIDTH; h = act.HEIGHT;
 		
 		bullets = new Sprite[8];
 		isattached = new boolean[8];
 		
 		tex = new Sprite(0, 0, new lb("s"+Integer.toString(typ), 512, 512).region);
 		tex.setScaleCenter(0, 0);
 		tex.setScale(w / tex.getWidth() / 3);
 		tex.setPosition(w * 0.6f - tex.getWidthScaled(), h  * 0.9f - tex.getHeightScaled());
 		pocisktex = new lb("Pocisk", 8, 8).region;
 		
 		
 		if( typ == 1) speed = 1.5f;
 		if( typ == 2) speed = 1.25f;
 		if( typ == 3) speed = 1f;
 		
 		for(int i = 0 ; i < 8 ; i++)
 		{
 			bullets[i] = new Sprite( tex.getX() , tex.getY(),pocisktex);
 			bullets[i].setScaleCenter(0, 0);
 			bullets[i].setScale(w/bullets[i].getWidth()/29.5f);
 			isattached[i] = false;
 		}
 		
 	}
 	
 	public void move(SensorEvent event)
 	{
 		if( tex.getX() < w - tex.getWidthScaled() ) //KOLIZJA PRAWEJ STRONY
 			tex.setPosition(tex.getX() - event.values[0]/speed, tex.getY());
 		else
 		{
 			tex.setPosition(w - tex.getWidthScaled(), tex.getY());	
 			tex.setPosition(tex.getX() - event.values[0]/speed, tex.getY());
 		}
 		
 		if( tex.getX() > 0 ) //KOLIZJA LEWEJ STRONY
 			tex.setPosition(tex.getX() - event.values[0]/speed, tex.getY());
 		else
 		{
 			tex.setPosition(0, tex.getY());	
 			tex.setPosition(tex.getX() - event.values[0]/speed, tex.getY());
 		}
 		
 		if( tex.getY() > 0 ) //KOLIZJA GRY
 			tex.setPosition(tex.getX(), tex.getY() + event.values[1]/speed);
 		else
 		{
 			tex.setPosition(tex.getX(), 0);	
 			tex.setPosition(tex.getX(), tex.getY() + event.values[1]/speed);
 		}
 		
 		if( tex.getY() < h - tex.getHeightScaled() ) // KOLIZJA DOU
 			tex.setPosition(tex.getX(), tex.getY() + event.values[1]/speed);
 		else
 		{
 			tex.setPosition(tex.getX(), h - tex.getHeightScaled() );	
 			tex.setPosition(tex.getX(), tex.getY() + event.values[1]/speed);
 		}
 	}
 	
 	public void shot(float ilosc) 
 	{
 		if (czymoznastrzelac == false) return;
 		{
 		for(int i = 0 ; i < 8 ; i++){
 		float y = 0 ;
 		y -= bullets[i].getHeightScaled() *2;
 		bullets[i].setVisible(true);
 		bullets[i].setPosition( tex.getX() + i*(ilosc+bullets[i].getWidthScaled()), tex.getY());
 		if(isattached[i] == false){ act.game.attachChild(bullets[i]);isattached[i] = true;}
 		czymoznastrzelac = false;
 		bullets[i].registerEntityModifier(new MoveYModifier(0.5f,tex.getY(),y)
 		{
 			@Override
 			protected void onModifierFinished(IEntity pItem)
 			{
 				super.onModifierFinished(pItem);
 				usun();
 			}
 		});
 		
 		}
 		}
 	}
 	
 	void usun()
 	{
 		for(int i = 0 ; i<8;i++)
 			bullets[i].setVisible(false);
 		
 		czymoznastrzelac = true;
 	}
 	
 	
 }
