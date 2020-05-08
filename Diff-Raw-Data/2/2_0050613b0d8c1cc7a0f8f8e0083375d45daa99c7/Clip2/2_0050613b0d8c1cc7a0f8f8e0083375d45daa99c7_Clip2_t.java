 package select2;
 
 import java.util.Random;
 
 /**
  * A clipboard object threads can share objects through.
  */
 public class Clip2{
	private final Select2 select2;
 	private volatile boolean setable;
 	
 	private volatile Object value;
 	
 	/**
 	 * The current implementation does not deal with open systems: one must explicitely give the threads willing to use the clipboard.
 	 */
 	public Clip2(Thread[] threads){
 		select2 = new Select2(threads);
 		setable = true;
 	}
 		
 	/**
 	 * Pops the object from the clipboard if any. 
 	 * After popping the clipboard is cleaned, waits for another object, one must push before another pop.
 	 * Returns the clipboard object if pop was successful, otherwise null.
 	 */
 	public Object pop(){
 		if (!setable){
 			if ( select2.execute(pop) ){
 				return pop.getValue();
 			}
 			else{ return null; }
 		}
 		else{ return null; }
 	}
 	
 	/**
 	 * Pushes an object to the clipboard, if it is waiting for it, otherwise does nothing. 
 	 * After pushing, the clipboard gets full, one must pop before another push.  
 	 * Returns true if push was successful, otherwise false.
 	 */
 	public boolean push(Object newVal){
 		if (setable){
 			if (newVal == null){
 				throw new NullPointerException();
 			}
 			
 			push.setValue(newVal);
 			
 			return select2.execute(push);
 		}
 		else{ return false; }
 	}
 	
 	protected Pop pop = new Pop();
 	
 	/**
 	 * A closure that implements pop.
 	 */
 	protected class Pop implements Closure{
 		private Object _value;
 
 		/**
 		 * Returns the value popped.
 		 */
 		public Object getValue(){
 			return _value;
 		}
 		
 		public boolean execute(){
 			if (!setable){
 				_value = value;
 				value = null;
 				setable = true;
 				return true;
 			}
 			else{
 				return false;
 			}
 		}
 	}
 	
 	protected Push push = new Push();
 	
 	/**
 	 * A closure that implements push.
 	 */
 	protected class Push implements Closure{
 		private Object _value;
 		
 		/**
 		 * Sets the value to push.
 		 */
 		public void setValue(Object value){
 			_value = value;
 		}
 		
 		public boolean execute(){
 			if (setable){
 				value = _value;
 				setable = false;
 				return true;
 			}
 			else{
 				return false;
 			}
 		}
 	}	
 
 
 	/**
 	 * Runs a simple test (or call it demo if you like).
 	 */
 	public static void main(String[] args){
 		Clip2Thread[] threads = new Clip2Thread[2];
 		
 		int rounds = args.length == 0 ? 10 : Integer.parseInt( args[0] );
 		threads[0] = new Clip2Thread(rounds, true);
 		threads[1] = new Clip2Thread(rounds, false);
 		
 		Clip2 clip2 = new Clip2(threads);
 		
 		for (int i = 0; i<2 ;i++){
 			threads[i].setClip2(clip2);
 			threads[i].start();
 		}
 	}
 	
 	/**
 	 * A simple class for testing purposes.
 	 */
 	private static class Clip2Thread extends Thread{
 		private boolean push;
 		private Clip2 clip2;
 		private Random random;
 		private int rounds;
 		
 		public Clip2Thread(int rounds, boolean push){
 			this.rounds = rounds;
 			this.push = push;
 			if (push){
 				random = new Random();
 			}
 		}
 		public void setClip2(Clip2 clip2){
 			this.clip2 = clip2;
 		}
 		public void run(){
 			long id = Thread.currentThread().getId();
 			int val;
 			for (int i = 0; i< rounds; i++){
 				if (push){
 					val = 1 + random.nextInt(100);
 					boolean pushed = clip2.push(val + "");
 					if (pushed){
 						System.out.println( "DEBUG: THREAD-" + id + ":\tPUSHED\t" + val );
 					}
 					else{
 						System.out.println( "DEBUG: THREAD-" + id + ":\tNOT_PUSHED\t" + val );
 					}
 					Thread.yield();
 				}
 				else{
 					Object popped = clip2.pop();
 					if (popped != null){
 						System.out.println( "DEBUG: THREAD-" + id + ":\tPOPPED\t" + popped);
 					}
 					else{
 						System.out.println( "DEBUG: THREAD-" + id + ":\tNOT_POPPED");
 					}
 					Thread.yield();
 				}
 			}
 		}	
 	}
 }
