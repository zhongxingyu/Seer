 /**
  *
  */
 package org.jdcp.job;
 
 import java.io.IOException;
 import java.io.PrintStream;
 import java.io.Serializable;
 
 import org.selfip.bkimmel.progress.ProgressMonitor;
 
 /**
  * A <code>ParallelizableJob</code> for diagnostic purposes.
  * @author brad
  */
 public final class DiagnosticJob extends AbstractParallelizableJob implements Serializable {
 
 	/**
 	 * Creates a new <code>DiagnosticJob</code>.
 	 */
 	public DiagnosticJob() {
 		this.worker = new DiagnosticTaskWorker();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jmist.framework.ParallelizableJob#getNextTask()
 	 */
 	public Object getNextTask() {
		return !this.isComplete() ? nextTask++ : null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jmist.framework.ParallelizableJob#isComplete()
 	 */
 	public boolean isComplete() {
		return nextTask >= 10;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jmist.framework.ParallelizableJob#submitTaskResults(java.lang.Object, java.lang.Object, org.jmist.framework.reporting.ProgressMonitor)
 	 */
 	public void submitTaskResults(Object task, Object results,
 			ProgressMonitor monitor) {
 
 		System.out.print("Received results for task: ");
 		System.out.println(task);
 
 		System.out.print("Results: ");
 		System.out.println(results);
 
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jmist.framework.ParallelizableJob#worker()
 	 */
 	public TaskWorker worker() {
 		return this.worker;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.jmist.framework.ParallelizableJob#finish()
 	 */
 	public void finish() throws IOException {
 		PrintStream out = new PrintStream(createFileOutputStream("output.txt"));
 		out.println("Done");
 		out.flush();
 		out.close();
 	}
 
 	/**
 	 * A <code>TaskWorker</code> for a <code>DiagnosticJob</code>.
 	 * @author brad
 	 */
 	private static final class DiagnosticTaskWorker implements TaskWorker {
 
 		/* (non-Javadoc)
 		 * @see org.jmist.framework.TaskWorker#performTask(java.lang.Object, org.jmist.framework.reporting.ProgressMonitor)
 		 */
 		public Object performTask(Object task, ProgressMonitor monitor) {
 			System.out.print("Performing task: ");
 			System.out.println(task);
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			monitor.notifyComplete();
 			return ((Integer) task) + 2;
 		}
 
 		/**
 		 *
 		 */
 		private static final long serialVersionUID = -7475685356387392964L;
 
 		//private static final long serialVersionUID = -5412216755033059283L;
 
 	}
 
 	/**
 	 * The <code>TaskWorker</code> for the job.
 	 */
 	private final TaskWorker worker;
 
 	/**
 	 * The index of the next task to be assigned.
 	 */
 	private int nextTask = 0;
 
 	/**
 	 * Serialization version ID.
 	 */
 	private static final long serialVersionUID = 1122053403154092883L;
 
 }
