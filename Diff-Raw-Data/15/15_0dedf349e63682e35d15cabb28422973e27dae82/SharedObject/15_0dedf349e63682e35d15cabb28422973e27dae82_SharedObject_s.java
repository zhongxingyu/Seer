 import java.io.*;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.ReentrantLock;
 import java.util.logging.Logger;
 import java.util.logging.Level;
 
 public class SharedObject implements Serializable, SharedObject_itf {
 	
 	private int id;
 	public Object obj;		
 	private State lockState;
 	private Client client;
 	private static Logger logger;
         private ReentrantLock lock;
         private Condition available;
         private boolean busy;
 	public enum State{	
 		NL,
 		RLT,
 		WLT,
 		RLC,
 		WLC,
 		RLT_WLC;
 	}
 
 	public SharedObject(int id,Object object,Client c){
 		this.id = id;
 		this.obj = object;
 		this.lockState = State.NL;
 		this.client = c;
                 this.busy = false;
                 this.lock = new ReentrantLock();
                 this.available = lock.newCondition();
 		logger = Logger.getLogger("SharedObject");
 		logger.setLevel(null);
 
 	}
 
 	// invoked by the user program on the client node
 	public void lock_read() {
             boolean update = false;
             lock.lock();
 		switch(this.lockState){
 			case RLC :
 				this.lockState=State.RLT;
 			break;
 			case WLC:
 				this.lockState=State.RLT_WLC;
 			break;
 			default:
                                 update = true;
 				this.lockState=State.RLT;
 			break;					
 		}
                 lock.unlock(); 
                 if(update){
                         this.obj = client.lock_read(this.id);
                 logger.log(Level.INFO,"I can read with "+lockState+".");
                 }
 	}
 
 	// invoked by the user program on the client node
 	public void lock_write() {
                 boolean update = false;
 	        lock.lock();
                 switch(this.lockState){
 		        case WLC:
                         this.lockState=State.WLT;
 	    	        break;
 		        default: 
 			this.lockState=State.WLT;
                         update = true;
 		        break;
 	        }
                 lock.unlock();
                 if(update){
                         this.obj = client.lock_write(this.id);
                         logger.log(Level.INFO,"I can write with "+lockState+".");
                 }
         } 
 
 	// invoked by the user program on the client node
 	public synchronized void unlock(){
 		switch(this.lockState){
 			case RLT:
 			lockState = State.RLC;
 			break;
 			case WLT:
 			lockState = State.WLC;
 			case RLT_WLC:
 			lockState = State.WLC;	
                         break;
                         default:
                             logger.log(Level.WARNING,"Unlock with : "+lockState+".");
                         break;
 		}
                this.available.signal();		
 	}
 
 	// callback invoked remotely by the server
 	public synchronized Object reduce_lock() {
 		this.lock.lock();
                 switch(this.lockState){
                         case WLT:
 		        while(this.lockState==State.WLT){
 			        try{		   
 				        this.available.await();
 		        	 }catch(InterruptedException i){}
 		        }
                         this.lockState = State.RLC;	
 			break;
 			case RLT_WLC:
 			this.lockState=State.RLT;
 			break;
 			case WLC:
 			this.lockState=State.RLC;
 			break;
 			default: 
                             logger.log(Level.WARNING,"reduce : Lock incoherent :"+lockState+".");
                         break;
 		}
                 logger.log(Level.INFO,"I was <b>reduced</b> to "+this.lockState+".");
 		this.lock.unlock();
 		return obj;
 	}
         public synchronized Object invalidate_writer(){
             this.lock.lock();
                 switch(this.lockState){
                     case WLT:
                         while(this.lockState==State.WLT){
 			        try{		   
 			                this.available.await();
 		                }catch(InterruptedException i){}
 		        }
                     break;
                     case RLT_WLC:
                         while(this.lockState==State.RLT_WLC){
  			  try{
                                 this.available.await();
 		              }catch(InterruptedException i){}
                         }
                     break;
                     case WLC:
                        //none
                     break;
                     default:
                         logger.log(Level.WARNING,"inv_writer: Lock incoherent :"+lockState+".");
                     break;
                 }
                 this.lockState = State.NL;
                 this.lock.unlock();
                 return obj;
         }
 
         public synchronized void invalidate_reader(){
                 this.lock.lock();
                 switch(this.lockState){
                     case RLT:
                         while(this.lockState==State.RLT){
 			        try{		   
 			                this.available.await();
 		                }catch(InterruptedException i){}
 		        }
                     break;
                     case RLC:
                        //none
                     break;
                     default:
                         logger.log(Level.WARNING,"inv_reader: Lock incoherent :"+lockState+".");
                     break;
                 }
                 this.lockState = State.NL;
                 this.lock.unlock();
         }
         
 	public int getID(){
 		return id;
 	}
 }
