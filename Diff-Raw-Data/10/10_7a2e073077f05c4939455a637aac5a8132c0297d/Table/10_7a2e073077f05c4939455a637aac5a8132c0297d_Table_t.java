 package com.example.game.Game;
 
 import android.graphics.Canvas;
 
 
 public class Table
 {
 	private Card[] mCards;
 	private int mCount;
 	private int mGameWidth , mGameHeight;
 	
 	public Table(int numberofplayers)
 	{
 		mCards = new Card[4 * numberofplayers];
 		mCount = 0;
 	}
 	
 	public void clear()
 	{
 		mCount = 0;
 	}
 	
 	public void setWidthHeight(int width , int height)
 	{
 		mGameWidth = width;
 		mGameHeight = height;
 	}
 	
 	public void addToTable(Card card)
 	{
 		if (mCount == 0)
 		{
 			mCards[mCount] = card;
 			card.setCenterX(mGameWidth / 2);
 			card.setCenterY(mGameHeight / 2);
 			mCount++;
 		}
 		else
 		{
 			mCards[mCount] = card;
 			card.setCenterX(mCards[mCount - 1].getCenterX() + 10);
 			card.setCenterY(mGameHeight / 2);
 			mCount++;
 		}
 	}
 	
 	public void drawCards(Canvas canvas)
 	{
 		for (int i = 0;i<mCount;i++)
			mCards[i].draw(canvas);
 	}
 }
