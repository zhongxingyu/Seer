 package com.example.game.Game;
 
import com.example.septica_multiplayer_bluetooth.GameActivity;

import android.app.Activity;
 import android.content.Context;
 import android.graphics.Canvas;
import android.os.Handler;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.widget.Toast;
 
 public class GameThread extends Thread
 {
 	/* Holder and Context */
 	private SurfaceHolder mHolder;
 	private Context mContext;
 
 	/* Running and pausing boolean */
 	private boolean mRunning;
 	private boolean mPaused;
 
 	/* Width and height of the game */
 	private int mHeight, mWidth;
 
 	/* The two players */
 	private Player player1;
 	private Player player2;
 
 	/* The table where the cards are */
 	private Table table;
 
 	// Bot Engine Thread
 	private BotPlay botPlay;
 
 	// Which player was first
 	private int whichPlayerWasFirst;
 
 	public GameThread(Context context, SurfaceHolder holder)
 	{
 		mContext = context;
 		mHolder = holder;
 		
 		// Initialize the images this must be done first !!!!!!!!!!!! 
 		Images.init(mContext);
 
 		// initialise botPlay
 		botPlay = new BotPlay();
 
 		// Shuffle the cards
 		DeckVector.init(context);
 		DeckVector.shuffle();
 
 		// instantiate players
 		player1 = new Player(1);
 		player2 = new Player(2);
 
 		// player1's turn
 		player1.setTurn(true);
 		whichPlayerWasFirst = 1;
 
 		// player2 not turn
 		player2.setTurn(false);
 
 		// table dimension
 		table = new Table(2);
 		Log.d("Septica", "Cards are now created!");
 	}
 
 	public void setWH(int width, int height)
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
 
 		// Start botPlay
 		botPlay.start();
 
 		while (mRunning)
 		{
 			try
 			{
 				if (!mPaused)
 				{
 					Canvas canvas = mHolder.lockCanvas();
 					synchronized (mHolder)
 					{
 						canvas.drawBitmap(Images.getBackgroundImage(), 0, 0, null);
 						player1.drawCards(canvas);
 						player2.drawCards(canvas);
 						table.drawCards(canvas);
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
 
 		// join the botPlay thread at the end of the game
 		try
 		{
 			botPlay.join();
 		}
 		catch (InterruptedException e)
 		{
 			e.printStackTrace();
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
 
 		if (player1.isTurn())
 		{
 			int result = player1.checkTouch(event);
 			if (result != -1)
 			{
 				if (table.addToTable(player1.getCard(result)))
 				{
 					player1.removeCard(result);
 					player1.setTurn(false);
 					player2.setTurn(true);
 				}
 				else
 				{
 					Toast.makeText(mContext, "Illegal move", Toast.LENGTH_SHORT).show();
 				}
 			}
 		}
 		return true;
 	}
 
 	public void finishHand(int whichPlayerWantsToFinishHand, boolean player1WantsToSwipe)
 	{
 		Log.d("Septica", whichPlayerWantsToFinishHand + "," + whichPlayerWasFirst);
 		if ( player1WantsToSwipe && (player1.isTurn() == false)){
 			//player1 can not finish the hand
 		}
 		else {
 			if (whichPlayerWantsToFinishHand == whichPlayerWasFirst)
 			{
 				player1.setTurn(false);
 				player2.setTurn(false);
 				HandWinner winner = table.checkHandWinner(whichPlayerWasFirst);
 				whichPlayerWasFirst = winner.getHandWinner();
 
 				// boolean for knowing if the game is finished
 				boolean gameDone = false;
 
 				// firstly we have to deal the cards if it is possible
 				if (DeckVector.isEmpty() && player1.getCards().size() == 0)
 				{
 					gameDone = true;
 				}
 				else
 				{
 					dealCards(winner.getHandWinner());
 				}
 
 				// set the player that won to start the hand
 				// just for 2 players
 				if (winner.getHandWinner() == 1)
 				{
 					player1.setNumberOfPoints(player1.getNumberOfPoints() + winner.getNumberOfPointsWon());
 				}
 				else
 				{
 					player2.setNumberOfPoints(player2.getNumberOfPoints() + winner.getNumberOfPointsWon());
 				}
 
 				if (gameDone)
 				{
					((Activity) mContext).runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							Toast.makeText(mContext, "Finish", Toast.LENGTH_SHORT).show();
						}
					});
 				}
 				else
 				{
 					if (winner.getHandWinner() == 1)
 					{
 						player1.setTurn(true);
 					}
 					else
 					{
 						player2.setTurn(true);
 					}
 				}
 			}
 		}
 	}
 
 	// just for 2 players
 	private void dealCards(int whichPlayerHasWon)
 	{
 		if (whichPlayerHasWon == 1)
 		{
 			while (!DeckVector.isEmpty() && player2.getCards().size() <= 3)
 			{
 				player1.addCard(DeckVector.pop());
 				player2.addCard(DeckVector.pop());
 			}
 		}
 		else
 		{
 			while (!DeckVector.isEmpty() && player1.getCards().size() <= 3)
 			{
 				player2.addCard(DeckVector.pop());
 				player1.addCard(DeckVector.pop());
 			}
 		}
 	}
 
 	private int pseudoRandom()
 	{
 		return (int) (Math.random() * (player2.getCards().size()));
 	}
 
 	private class BotPlay extends Thread
 	{
 
 		@Override
 		public void run()
 		{
 			while (mRunning)
 			{
 
 				try
 				{
 					Thread.sleep(1000);
 				}
 				catch (InterruptedException e)
 				{
 					e.printStackTrace();
 				}
 
 				if (player2.isTurn())
 				{
 					// choose a random card from player's 2 hand
 					int whatCard = pseudoRandom();
 					Log.d("Septica", whatCard + "");
 					
 					if (player2.getCards().size() != 0){
 						if (table.addToTable(player2.getCard(whatCard)))
 						{
 							player2.removeCard(whatCard);
 							// change player 2 turn
 							player2.setTurn(false);
 							player1.setTurn(true);
 						}
 						else
 						{
 							if (whatCard == 0)
 							{
 								player1.setTurn(false);
 								player2.setTurn(false);
 								finishHand(2, false);
 							}
 						}
 					}
 					else {
 						player1.setTurn(false);
 						player2.setTurn(false);
 						finishHand(2, false);
 					}
 				}
 			}
 		}
 	}
 
 }
