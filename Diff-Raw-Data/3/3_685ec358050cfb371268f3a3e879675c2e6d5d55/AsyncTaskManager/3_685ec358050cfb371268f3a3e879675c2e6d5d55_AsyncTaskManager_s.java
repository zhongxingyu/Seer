 package fm.audiobox.sync.util;
 
 import java.util.*;
 
 import fm.audiobox.sync.interfaces.ThreadListener;
 
 
 public class AsyncTaskManager {
 
     private int MAX = 4;
     private int current_thread_index = -1;
     private int started_thread = 0;
     private int max_threads = MAX;
     private boolean _autoStart = true;
     private boolean _stopped = false;
     private boolean _completed = true;
     
     protected ThreadGroup threadGroup = null;
     private ThreadListener threadListener = new ThreadListener(){
 		public boolean onStart(AsyncTask result) {return true;}
 		public void onProgress(AsyncTask result, long total, long completed, long remaining, Object item) {}
 		public void onComplete(AsyncTask result, Object item) {}
 		public void onStop(AsyncTask task) {}
     };
 
 	private List<AsyncTask> _threads = null;
 
 
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
         if ( this.threadGroup == null ) this.threadGroup = new ThreadGroup( this.generateThreadGroupName(item) );
 
         item.setManager( this );
         this._threads.add( item );
         if ( this._autoStart )
             this._start();
     }
 
 
     public void setAutoStart(boolean autoStart){
         this._autoStart = autoStart;
     }
 
     public void start(){
         if ( !this._autoStart ){
             this._autoStart = true;
             for(int i=0;i<this.max_threads;i++)
                 this._start();
         }
     }
 
     private synchronized void _start(){
     	/** Managements: 
     	 * Stopped: return if thread manager was stopped
     	 * No Threads found: return if no thread added
     	 * Completed: fire 'onStart' event if thread manager was completed ( or at first call )
     	 * Stack is full: return if Thread Stack is full
     	 */
     	
     	/* Return if ThreadManager was stopped */
     	if ( this._stopped ) return;
     	
     	/* No Threads found */
     	if ( this._threads == null ) return;
     	
     	/* Thread Stack is full */
         if ( this.started_thread == this.max_threads ) return;
         
         /* Check if there are any thread */
         if ( (this.current_thread_index+1) >= this._threads.size() ) return;
         
         /* Fire 'onStart' event if ThreadManager was completed or is at first call */
         if ( this._completed )
         	this.getThreadListener().onStart(null);
         
         
         
         // Default: set false */
         this._completed = false;
         
         /**
          * Instantiate a new Thread and increment current index
          */
         this.current_thread_index++;
 
         AsyncTask item = this._threads.get(this.current_thread_index);
         Thread thread = this.createThread(item);
         thread.setName( this.generateThreadName(thread,item) );
 
         thread.start();
         
         /**
          * Fire 'onProgress' event when thread is started
          */
         int _size = this._threads.size();
         this.getThreadListener().onProgress(item, _size, this.current_thread_index ,_size - this.current_thread_index, null);
         
         this.started_thread++;
 
     }
     
     
     /**
      * Stop all threads
      */
     public void stop(){
     	
     	this._stopped=true;
     	if ( this._threads != null )
 	    	/** Remove all remaining element from threads list */
 	    	while( this._threads.size() > 0 )
 	    		this._threads.remove( this._threads.size()-1 ).stop();
     	
 		this.threadGroup = null;
 		this.current_thread_index = -1;
 	    this.started_thread = 0;
 	    
 	    this.getThreadListener().onStop(null);
 	    
 		this._stopped = false;
     }
     
 
     public synchronized void onComplete(AsyncTask item) {
         this.started_thread--;
         
        if ( (this.current_thread_index+1) >= this._threads.size() ){
         	this.getThreadListener().onComplete(null, null);
         	return;
         }
         
         this._start();
     }
 
 
     public  void onStart(AsyncTask item) {
 
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
 
 }
