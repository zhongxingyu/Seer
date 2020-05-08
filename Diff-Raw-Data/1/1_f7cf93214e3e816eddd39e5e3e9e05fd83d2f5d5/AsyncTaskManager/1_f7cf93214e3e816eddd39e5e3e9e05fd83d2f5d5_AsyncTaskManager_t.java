 
 /***************************************************************************
  *   Copyright (C) 2010 iCoreTech research labs                            *
  *   Contributed code from:                                                *
  *   - Valerio Chiodino - keytwo at keytwo dot net                         *
  *   - Fabio Tunno      - fat at fatshotty dot net                         *
  *                                                                         *
  *   This program is free software: you can redistribute it and/or modify  *
  *   it under the terms of the GNU General Public License as published by  *
  *   the Free Software Foundation, either version 3 of the License, or     *
  *   (at your option) any later version.                                   *
  *                                                                         *
  *   This program is distributed in the hope that it will be useful,       *
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
  *   GNU General Public License for more details.                          *
  *                                                                         *
  *   You should have received a copy of the GNU General Public License     *
  *   along with this program. If not, see http://www.gnu.org/licenses/     *
  *                                                                         *
  ***************************************************************************/
 
 package fm.audiobox.sync.util;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import fm.audiobox.sync.interfaces.ThreadListener;
 
 /**
  * 
  * 
  * @author Fabio Tunno
  * @version 1.0.0
  */
 public class AsyncTaskManager implements Observer{
 
   private int MAX = 4;
   private int current_thread_index = -1;
   //  private int started_thread = 0;
   private int max_threads = MAX;
   private boolean _autoStart = true;
   private boolean _stopped = false;
   private boolean _completed = true;
   private boolean isRunning = false;
 
   protected ThreadGroup threadGroup = null;
   private ThreadListener threadListener = new ThreadListener(){
     public boolean onStart(AsyncTask result) {return true;}
     public void onProgress(AsyncTask result, long total, long completed, long remaining, Object item) {}
     public void onComplete(AsyncTask result, Object item) {}
     public void onStop(AsyncTask task) {}
   };
 
   private List<AsyncTask> _threads;
   private List<AsyncTask> _runningthreads;
 
 
   public AsyncTaskManager(){
     this.max_threads = MAX;
   }
 
   public AsyncTaskManager(boolean autoStart){
     super();
     this._autoStart = autoStart;
   }
 
   public AsyncTaskManager(int maxThread){
     this.max_threads = maxThread;
   }
 
   public AsyncTaskManager(int maxThread, boolean autoStart){
     this.max_threads = maxThread;
     this._autoStart = autoStart;
   }
 
 
 
   public ThreadListener getThreadListener() {
     return threadListener;
   }
 
   public void setThreadListener(ThreadListener threadListener) {
     this.threadListener = threadListener;
   }
 
 
   public void newThread(AsyncTask item){
 
     if ( this._threads == null ) this._threads = new ArrayList<AsyncTask>();
     if ( this._runningthreads == null ) this._runningthreads = new ArrayList<AsyncTask>();
 
     if ( this.threadGroup == null ) this.threadGroup = new ThreadGroup( this.generateThreadGroupName(item) );
 
     //    item.setManager( this );
 
     if ( this._autoStart && this._runningthreads.size() <= this.max_threads ){
       this._start( item );
     }else
       this._threads.add( item );
   }
 
 
   //  public void setAutoStart(boolean autoStart){
   //    this._autoStart = autoStart;
   //  }
 
   public void start(){
     if ( !this._autoStart && !this.isRunning){
       //      this._autoStart = true;
       if( this._threads.size() > 0  )
         for(int i=0;  (i < this.max_threads && i < this._threads.size() ); i++)      
           this._start( this._threads.remove( 0 ) );
     }
   }
 
   private synchronized void _start( AsyncTask _task ){
     /** Managements: 
      * Stopped: return if thread manager was stopped
      * No Threads found: return if no thread added
      * Completed: fire 'onStart' event if thread manager was completed ( or at first call )
      * Stack is full: return if Thread Stack is full
      */
 
     /* Return if ThreadManager was stopped */
     if ( this._stopped ) return;
 
     /* No Threads found */
     //    if ( this._runningthreads == null ) return;
 
     //    /* Thread Stack is full */
     //    if ( this.started_thread == this.max_threads ) return;
     //
     //    /* Check if there are any thread */
     //    if ( (this.current_thread_index+1) >= this._threads.size() ) return;
 
     this._runningthreads.add(_task);
 
     this.isRunning = true;
 
     /* Fire 'onStart' event if ThreadManager was completed or is at first call */
     if ( this._completed )
       this.getThreadListener().onStart(null);
 
 
 
     // Default: set false */
     this._completed = false;
 
     /**
      * Instantiate a new Thread and increment current index
      */
     this.current_thread_index++;
 
     //    AsyncTask item = this._threads.get(this.current_thread_index);
     Thread thread = this.createThread( _task );
     thread.setName( this.generateThreadName( thread, _task ) );
 
     _task.addObserver( this );
     
     thread.start();
 
     /**
      * Fire 'onProgress' event when thread is started
      */
     int _size = this._runningthreads.size();
     this.getThreadListener().onProgress( _task , _size, this.current_thread_index ,_size - this.current_thread_index, null);
 
     //    this.started_thread++;
 
   }
 
 
   /**
    * Stop all threads
    */
   public void stop(){
 
     this._stopped = true;
     if ( this._threads != null )
       /** Remove all remaining element from threads list */
       while( this._threads.size() > 0 )
         this._threads.remove( this._threads.size()-1 ).stop();
 
     this.threadGroup = null;
     //    this.current_thread_index = -1;
     //    this.started_thread = 0;
 
     this.getThreadListener().onStop(null);
 
     this._stopped = false;
     this.isRunning = false;
   }
 
 
   public synchronized void onComplete( Object item) {
     //    this.started_thread--;
     //
     //    //if ( (this.current_thread_index+1) >= this._threads.size() ){
     //    if ( this.started_thread == 0 ){
     //      this.getThreadListener().onComplete(null, null);
     this.isRunning = false;
    this.getThreadListener().onComplete(null, null);
     //      return;
     //    }
     //
     //    this._start();
   }
 
 
   public  void onStart( Object item) {
 
   }
 
   protected Thread createThread( AsyncTask item ) {
     return new Thread(this.getThreadGroup(), item);
   }
 
   protected String generateThreadName(Thread thread, AsyncTask task){
     return task.getClass().getSimpleName() + "-" + thread.getId();
   }
   protected String generateThreadGroupName(AsyncTask task){
     return task.getClass().getSimpleName();
   }
 
   protected ThreadGroup getThreadGroup(){
     return this.threadGroup;
   }
 
   public boolean isRunning(){
     return this.isRunning;
   }
 
   @Override
   public synchronized void update(Observable arg0, Object arg1) {
     this._runningthreads.remove(arg0);    
     if( this._threads.size() > 0 && this._runningthreads.size() <= this.max_threads){
       try {
         AsyncTask _task = this._threads.remove(0);
         this._start( _task );
       } catch ( Throwable t ){
         // That's strange!!
         System.out.println("[AsyncTaskManager] An error occurs while starting a new thread" + t.getMessage() );
       }
     } else if( this._threads.size() == 0 )
       this.onComplete( arg0 );
   }
 
 }
