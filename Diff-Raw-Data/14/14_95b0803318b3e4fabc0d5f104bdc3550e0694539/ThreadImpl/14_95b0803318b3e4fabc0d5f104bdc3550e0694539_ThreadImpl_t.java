 /**
  *  CGateInterface - A library to allow interaction with Clipsal C-Gate.
  *  Copyright (C) 2008,2009  Dave Oxley <dave@daveoxley.co.uk>.
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Affero General Public License as
  *  published by the Free Software Foundation, either version 3 of the
  *  License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Affero General Public License for more details.
  *
  *  You should have received a copy of the GNU Affero General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package com.daveoxley.cbus.threadpool;
 
 import com.daveoxley.cbus.CGateException;
 
 /**
  *
  * @author Dave Oxley <dave@daveoxley.co.uk>
  */
 public class ThreadImpl extends Thread
 {
     private boolean used = false;
 
    private volatile boolean running = false;
 
     private Runnable runnable = null;
 
    private ThreadImplPool pool = null;
 
     @Override
     public synchronized void start()
     {
         if (!running && !used)
         {
             running = true;
             used = true;
             setDaemon(true);
             super.start();
         }
     }
 
     void finish()
     {
         running = false;
         notifyAll();
     }
 
    synchronized boolean isBorrowed()
     {
        return pool != null;
     }
 
     boolean isRunning()
     {
         return running;
     }
 
    synchronized void setPool(ThreadImplPool pool)
     {
         this.pool = pool;
     }
 
     /**
      *
      * @param runnable
      */
     public synchronized void execute(Runnable runnable)
     {
         this.runnable = runnable;
         notifyAll();
     }
 
     @Override
     public void run()
     {
         while (running)
         {
             Runnable _runnable = null;
             synchronized (this)
             {
                 while (running && runnable == null)
                 {
                     try
                     {
                         wait();
                     }
                     catch (InterruptedException ie) {}
                 }
                 _runnable = runnable;
             }
 
             if (_runnable != null)
             {
                 try
                 {
                     _runnable.run();
                 }
                 catch (Exception e)
                 {
                     new CGateException(e);
                 }
 
                 synchronized (this)
                 {
                     try
                     {
                         pool.returnObject(this);
                     }
                     catch (Exception e)
                     {
                         new CGateException(e);
                     }
                     pool = null;
                     runnable = null;
                 }
             }
         }
     }
 }
