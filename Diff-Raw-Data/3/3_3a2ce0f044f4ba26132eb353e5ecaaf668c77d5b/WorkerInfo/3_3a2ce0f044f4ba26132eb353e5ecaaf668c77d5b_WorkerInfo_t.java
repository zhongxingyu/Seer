 package de.wfhosting.workmanager;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import de.wfhosting.workmanager.data.WMConfig;
 import de.wfhosting.workmanager.exceptions.WorkCancelException;
 import de.wfhosting.workmanager.exceptions.WorkException;
 import de.wfhosting.workmanager.workers.AbstractWorkProcess;
 
 class WorkerInfo<E, T extends AbstractWorkProcess<E>> implements Serializable {
 	private static final long serialVersionUID = 1L;
 
 	private boolean runnerFinished = false;
 	private final AbstractWorkProcess<E> worker;
 	private final List<WorkException> messages = new ArrayList<WorkException>();
 	private final WorkerStatus status = new WorkerStatus();
 	private transient WorkerRunner<E, T> runner = null;
 
 	public WorkerInfo(final AbstractWorkProcess<E> worker) {
 		this.worker = worker;
 	}
 
 	void addMessage(final WorkException wex) {
 		if (!runnerFinished) {
 			messages.add(wex);
 		}
 	}
 
 	void cancelRunner() {
 		if (runnerFinished) {
 			return;
 		}
 
 		runner.cancel();
 	}
 
 	public List<WorkException> getMessages() {
 		return Collections.unmodifiableList(messages);
 	}
 
 	public WorkerStatus getStatus() {
 		return status;
 	}
 
 	public AbstractWorkProcess<E> getWorker() {
 		return worker;
 	}
 
 	public boolean isRunnerFinished() {
 		return runnerFinished;
 	}
 
 	@SuppressWarnings("deprecation")
 	void killRunner(final WMConfig config) {
 		if (runnerFinished) {
 			return;
 		}
 
 		ThreadGroup group = runner.getThreadGroup();
 		runnerFinished = true;
 		cancelRunner();
 
 		try {
 			runner.join(config.getKillTimeout());
 		} catch (InterruptedException e) {
 			// should not happen
 		}
 
 		group.stop();
 	}
 
 	private void readObject(final ObjectInputStream stream) throws IOException,
 			ClassNotFoundException {
 		stream.defaultReadObject();
 	}
 
 	void setRunner(final WorkerRunner<E, T> runner) {
 		this.runner = runner;
 	}
 
 	void setRunnerFinished() {
 		runnerFinished = true;
 	}
 
 	@Override
 	public String toString() {
 		return getClass().getSimpleName() + "[status=" + status + ", finished="
 				+ runnerFinished + ", messages=" + messages + "]";
 	}
 
 	private void writeObject(final ObjectOutputStream stream) throws IOException {
 		addMessage(new WorkCancelException("Killed."));
		WMConfig config = new WMConfig(1, 1, 100, 300);
		killRunner(config);
 
 		stream.defaultWriteObject();
 	}
 }
