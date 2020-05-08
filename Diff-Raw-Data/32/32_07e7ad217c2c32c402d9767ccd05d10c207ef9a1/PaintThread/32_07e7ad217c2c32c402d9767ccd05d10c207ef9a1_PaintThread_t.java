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
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.os.Handler;
 import android.view.SurfaceHolder;
 
 public class PaintThread extends Thread
 {
 	private SurfaceHolder	mSurfaceHolder;
 	GameEngine				gEngine;
 
 	private long			sleepTime;
 	private long			delay	= 10;
 
 	int						state	= 1;
 	public final static int	RUNNING	= 1;
 	public final static int	PAUSED	= 2;
 
 	public PaintThread(SurfaceHolder surfaceHolder, Context context,
 			Handler handler, GameEngine gEngineS)
 	{
 
 		mSurfaceHolder = surfaceHolder;
 
 		gEngine = gEngineS;
 		state = RUNNING;
 	}
 
 	public void run()
 	{
 		while (state == RUNNING)
 		{
 			long beforeTime = System.nanoTime();
 			gEngine.update();
 
 			Canvas c = null;
 			try
 			{
 				c = mSurfaceHolder.lockCanvas(null);
 				synchronized (mSurfaceHolder)
 				{
 					gEngine.draw(c);
 				}
 			}
 			finally
 			{
 				if (c != null)
 				{
 					mSurfaceHolder.unlockCanvasAndPost(c);
 				}
 			}
 
 			this.sleepTime = delay
 					- ((System.nanoTime() - beforeTime) / 1000000L);
 
 			try
 			{
 				if (sleepTime > 0)
 				{
 					Thread.sleep(sleepTime);
 				}
 			}
 			catch (InterruptedException ex)
 			{
 				Logger.getLogger(PaintThread.class.getName()).log(Level.SEVERE,
 						null, ex);
 			}
 		}
 
 	}
 }
