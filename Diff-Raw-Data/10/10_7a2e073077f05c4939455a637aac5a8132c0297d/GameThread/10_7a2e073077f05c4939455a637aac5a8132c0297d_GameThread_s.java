 package com.example.game.Game;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 
 public class GameThread extends Thread
 {
 	/* Holder and Context */
 	private SurfaceHolder mHolder;
 	private Context mContext;
 	
 	/* Running and pausing boolean */	
 	private boolean mRunning;
 	private boolean mPaused;
 	
 	/* Width and height of the game */
 	private int mHeight , mWidth;
 	
 	/* The two players */	
 	private Player player1;
 	private Player player2;
 	
 	/* The table where the cards are */
 	private Table table;
 
 	public GameThread(Context context, SurfaceHolder holder)
 	{
 		mContext = context;
 		mHolder = holder;
 		DeckVector.init(context);
 		DeckVector.shuffle();
 		player1 = new Player(1);
 		player2 = new Player(2);
 		table = new Table(2);
 		Log.d("Septica", "Cards are now created!");
 	}
 	
 	public void setWH(int width , int height)
 	{
 		mHeight = height;
 		mWidth = width;
 		player1.setupCards(mWidth, mHeight);
 		player2.setupCards(mWidth, mHeight);
 		table.setWidthHeight(mWidth, mHeight);
 	}
 
 	public void run()
 	{
 		mRunning = true;
 		mPaused = false;
 		while (mRunning)
 		{
 			try
 			{
 				if (!mPaused)
 				{
 					Canvas canvas = mHolder.lockCanvas();
 					synchronized (mHolder)
 					{
 						canvas.drawColor(Color.BLACK);
 						player1.drawCards(canvas);
 						player2.drawCards(canvas);
 						mHolder.unlockCanvasAndPost(canvas);
 					}
 				}
 				Thread.sleep(20);
 			}
 			catch (Exception e)
 			{
 				break;
 			}
 		}
 		Log.d("Septica", "Game has stopped! ");
 	}
 
 	public void stopRunning()
 	{
 		mRunning = false;
 	}
 
 	public void Pause()
 	{
 		mPaused = true;
 	}
 
 	public void Unpause()
 	{
 		mPaused = false;
 	}
 	
 	public boolean handleTouch(MotionEvent event)
 	{
 		ArrayList<Card> cards = player1.getCards();
 		for (int i = 0;i<cards.size();i++)
 			if (cards.get(i).isTouched(event))
 			{
 				table.addToTable(cards.get(i));
 				break;
 			}
 		return true;
 	}
 
 }
