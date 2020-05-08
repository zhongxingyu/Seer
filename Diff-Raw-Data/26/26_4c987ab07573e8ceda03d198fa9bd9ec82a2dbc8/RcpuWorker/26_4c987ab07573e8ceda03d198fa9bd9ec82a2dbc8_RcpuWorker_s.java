 package rcpu;
 
 import java.util.Queue;
 import java.util.LinkedList;
 
 // A core can be thought of as a node
class RcpuWorker extends Thread {
 	private int id;				   			// id
 	private Queue<WorkParcel> work = new LinkedList<WorkParcel>();
 
 	public RcpuWorker(int id) {
 		this.id = id;
 	}
 
 	public void run() {
 	}
 
 	public int addWork(WorkParcel work) {
 		this.work.offer(work);
 		return this.workSize();
 	}
 
 	public int workSize() {
 		return work.size();
 	}
 
 }
