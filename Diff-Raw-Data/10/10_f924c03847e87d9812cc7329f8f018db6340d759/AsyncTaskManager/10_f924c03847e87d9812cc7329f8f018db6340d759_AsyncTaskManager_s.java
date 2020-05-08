 package fm.audiobox.sync.util;
 
 import java.util.*;
 
 
 public class AsyncTaskManager {
 
     private int MAX = 4;
     private int current_thread_index = -1;
     private int started_thread = 0;
     private int max_threads = MAX;
     private boolean _autoStart = true;
 
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
 
     public void newThread(AsyncTask item){
 
         if ( this._threads == null ) this._threads = new ArrayList<AsyncTask>();
 
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
 
         if ( this.started_thread == this.max_threads ) return;
         if ( (this.current_thread_index+1) >= this._threads.size() ) return;
 
         this.current_thread_index++;
 
         AsyncTask item = this._threads.get(this.current_thread_index);
         Thread thread = new Thread( item );
 
         thread.start();
         this.started_thread++;
 
     }
 
     public synchronized void onComplete(AsyncTask item) {
         this.started_thread--;
         this._start();
     }
 
 
     public synchronized void onStart(AsyncTask item) {
 
     }
 
 
 
 }
