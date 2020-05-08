 /**
    GetOut  
    Copyright (C) 2012 Sylvain "Viish" Berfini
 
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
 
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.viish.apps.android.games.getout;
 
 import android.content.Context;
 import android.os.Handler;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.View.OnTouchListener;
 
 public class GameView extends SurfaceView implements SurfaceHolder.Callback,
 		OnTouchListener
 {
 	long				lastUpdate	= 0;
 	long				sleepTime	= 0;
 
 	SurfaceHolder		surfaceHolder;
 	Context				context;
 	GameEngine			gEngine;
 
 	private PaintThread	thread;
 
	public void pause()
	{
		thread.state = PaintThread.PAUSED;
	}
	
 	void initView()
 	{
 		SurfaceHolder holder = getHolder();
 		holder.addCallback(this);
 
 		gEngine = new GameEngine();
 		gEngine.init(context.getResources());
 
 		thread = new PaintThread(holder, context, new Handler(), gEngine);
 		setFocusable(true);
 
 		setOnTouchListener(this);
 	}
 
 	public GameView(Context contextS)
 	{
 		super(contextS);
 		context = contextS;
 		initView();
 	}
 
 	public GameView(Context contextS, AttributeSet attrs, int defStyle)
 	{
 		super(contextS, attrs, defStyle);
 		context = contextS;
 		initView();
 	}
 
 	public GameView(Context contextS, AttributeSet attrs)
 	{
 		super(contextS, attrs);
 		context = contextS;
 		initView();
 	}
 
 	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3)
 	{}
 
 	public void surfaceDestroyed(SurfaceHolder arg0)
 	{
 		boolean retry = true;
 		thread.state = PaintThread.PAUSED;
 		while (retry)
 		{
 			try
 			{
 				thread.join();
 				retry = false;
 			}
 			catch (InterruptedException e)
 			{}
 		}
 
 	}
 
 	public void surfaceCreated(SurfaceHolder arg0)
 	{
 		if (thread.state == PaintThread.PAUSED)
 		{
 			thread = new PaintThread(getHolder(), context, new Handler(),
 					gEngine);
 			thread.start();
 		}
 		else
 		{
 			thread.start();
 		}
 	}
 
 	public boolean onTouch(View v, MotionEvent event)
 	{
 		if (event.getAction() == MotionEvent.ACTION_DOWN) {
 			gEngine.move(event.getX(), event.getY());
 			return true;
 		}
 		return false;
 	}
 }
