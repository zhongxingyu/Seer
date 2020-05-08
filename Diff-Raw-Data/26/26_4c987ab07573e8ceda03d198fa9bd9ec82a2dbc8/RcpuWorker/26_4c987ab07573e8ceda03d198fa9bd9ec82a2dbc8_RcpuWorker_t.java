 package rcpu;
 
 import java.util.Queue;
 import java.util.LinkedList;
 
 // A core can be thought of as a node
class RcpuWorker extends Thread implements Comparable<RcpuWorker> {
 	private int id;				   			// id
 	private Queue<WorkParcel> work = new LinkedList<WorkParcel>();
 
 	public RcpuWorker(int id) {
 		this.id = id;
 	}
 
 	public void run() {
 	}
 
	public int compareTo(RcpuWorker other) {
		if (this.workSize() == other.workSize()) {
			return 0;
		}
		else {
			return this.workSize() < other.workSize()? -1 : 1;
		}
	}

 	public int addWork(WorkParcel work) {
 		this.work.offer(work);
 		return this.workSize();
 	}
 
 	public int workSize() {
 		return work.size();
 	}
 
 }
