 package select2.debug;
 
 public class Volatile{
 	
 	protected static class TestVolatile{
 		private final long[] threadIds;
 		private volatile State state0;
 		private volatile State state1;
 		private volatile State state;
 		
 		public TestVolatile(Thread[] threads){	
 			threadIds = new long[2];
 			for (int i = 0; i<2;i++){
 				threadIds[i] = threads[i].getId();
 			}
 			state0 = State.IDLE;
 			state1 = State.IDLE;
 			state = State.OK;
 		}			
 		
 		public void test(){
 			assert state == State.OK;
 			
 			// get the internal thread number
 			int i =  getInternalThreadId();
 			
 			// activate
 			switch(i){
 				case 0: state0 = State.ACTIVE; break;
 				case 1: state1 = State.ACTIVE; break;
 			}
 			try{Thread.sleep(1+i);}catch(Throwable ignore){};
 			
 			// guard
 			switch(i){
 				case 0: 
					if (state1 != State.IDLE){ state0 = State.WAIT; }; 
 					break;
 				case 1: 
					if (state0 != State.IDLE){ state1 = State.WAIT; }; 
 					break;
 			}
 			try{Thread.sleep(1+i);}catch(Throwable ignore){};
 			
			// assert: one thread must not be ACTIVE
 			if( state0 == State.ACTIVE && state1 == State.ACTIVE ){
 				state = State.ERROR;
 			}
 			assert state0 != State.ACTIVE || state1 != State.ACTIVE;
 			
 			// deactivate
 			switch(i){
 				case 0: state0 = State.IDLE; break;
 				case 1: state1 = State.IDLE; break;
 			}			
 			try{Thread.sleep(1+i);}catch(Throwable ignore){};
 		}
 		
 		protected int getInternalThreadId(){
 			if (threadIds[0] == Thread.currentThread().getId()){ return 0; }
 			else{ return 1; }
 		}			
 		
 	}
 	
 	protected enum State{
 		IDLE, ACTIVE, WAIT, OK, ERROR
 	}
 	
 	protected static class TestVolatileThread extends Thread{
 		private TestVolatile test;
 		
 		public void setTest(TestVolatile test){ this.test = test; }
 		
 		public void run(){
 			int step = 0;
 			while(true){
 				step++;
 				test.test();
 				System.out.println("THREAD-" + Thread.currentThread().getId() + ": " + step);
 			}
 		}
 	}
 	
 	
 	public static void main(String[] args){
 		TestVolatileThread[] threads = new TestVolatileThread[2];
 		threads[0] = new TestVolatileThread();
 		threads[1] = new TestVolatileThread();
 		
 		TestVolatile test = new TestVolatile(threads);
 		
 		threads[0].setTest(test);
 		threads[1].setTest(test);
 		
 		threads[0].start();
 		threads[1].start();
 	}
 }
