 package warrenfalk.meldfs;
 
 import java.util.concurrent.atomic.AtomicInteger;
 
 
 public class SourceOpRunner implements Runnable {
 	final int index;
 	final SourceFs source;
 	final AtomicInteger sync;
 	final SourceOp operation;
 	
 	public SourceOpRunner(SourceOp operation, int index, SourceFs source, AtomicInteger sync) {
 		this.index = index;
 		this.source = source;
 		this.sync = sync;
 		this.operation = operation;
 	}
 	
 	@Override
 	public void run() {
		operation.run(index, source);
		if (0 == sync.decrementAndGet()) {
			synchronized (sync) {
				sync.notify();
 			}
 		}
 	}
 }
