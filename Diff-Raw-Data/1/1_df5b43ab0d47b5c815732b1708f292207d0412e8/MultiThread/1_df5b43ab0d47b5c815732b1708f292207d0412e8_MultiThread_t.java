 /**
  * 
  */
 package multithread;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 /**
  * @author chenqian
  *
  */
 public class MultiThread {
 
 	final int[] lock = new int[1];
 	int ThreadNum = 2;
 	boolean[] threadStatus 	=  null;
 	Task[] tasks 	=  null;
 	
 	public void run() {
 		lock[0] = 0;
 		threadStatus = new boolean[ThreadNum];
 		final int totalNum = tasks.length;
 		for(int id = 0; id < ThreadNum; id ++){
 			threadStatus[id] = false;
 			final int tid  = id;
 			new Thread(new Runnable() {
 				@Override
 				public void run() {
 					// TODO Auto-generated method stub
 					int curid, threadId = tid;
 					while(true){
 						synchronized (lock) {
 							curid = lock[0];
 							lock[0]++;	
 						}
 						if(curid >= totalNum)break;
 						tasks[curid].run();
 					}
 					threadStatus[threadId] = true;
 				}
 			}).start();
 		}
 		while(true){
 			boolean found = false;
 			try {
 				Thread.sleep(100);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			for(int i = 0; i < ThreadNum; i++){
 				if(threadStatus[i] == false){
 					found = true;
 				}
 			}
 			if(!found)break;
 		}
 	}
 	
 	public void setThreadNum(int num) {
 		this.ThreadNum = num;
 	}
 	/**
 	 * 
 	 */
 	public MultiThread() {
 		// TODO Auto-generated constructor stub
 	}
 	
 	public MultiThread(int num) {
 		// TODO Auto-generated constructor stub
 		this.ThreadNum = num;
 	}
 
 	public MultiThread(Task[] tasks, int num) {
 		// TODO Auto-generated constructor stub
 		this.tasks = tasks;
		this.ThreadNum = num;
 	}
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
