 package de.wfhosting.workmanager;
 
 import de.wfhosting.workmanager.WorkerStatus.PHASE;
import de.wfhosting.workmanager.data.JobProgressEvent;
 import de.wfhosting.workmanager.data.WMConfig;
 import de.wfhosting.workmanager.exceptions.WorkCancelException;
 import de.wfhosting.workmanager.exceptions.WorkException;
 import de.wfhosting.workmanager.workers.AbstractWorkProcess;
 
 class WorkerRunner<E, T extends AbstractWorkProcess<E>> extends Thread {
 	private final Workmanager manager;
 	private final WorkerInfo<E, T> info;
 	private final AbstractWorkProcess<E> worker;
 
 	private boolean doCancel = false;
 
 	public WorkerRunner(final Integer id, final Workmanager manager,
 			final WorkerInfo<E, T> info) {
 		super(new ThreadGroup("WorkerRunner#" + id), "WM_Runner#" + id);
 
 		// no worker given?
 		if (manager == null) {
 			throw new NullPointerException();
 		}
 
 		// no manager given?
 		if (info == null) {
 			throw new NullPointerException();
 		}
 
 		// set attributes
 		this.manager = manager;
 		this.info = info;
 		this.worker = info.getWorker();
 
 		// set info
 		info.setRunner(this);
 	}
 
 	void addMessage(final WorkException exc) {
 		info.addMessage(exc);
 	}
 
 	void blockFinished() {
 		info.getStatus().finishedBlock();
 	}
 
 	public void cancel() {
 		doCancel = true;
 	}
 
 	private void checkCancel() throws WorkException {
 		if (doCancel) {
 			throw new WorkCancelException();
 		}
 	}
 
 	void exceptionCaught(final Throwable exc) {
 		if (exc instanceof WorkException) {
 			info.addMessage((WorkException) exc);
 		} else if (exc instanceof ThreadDeath) {
 			// ignore
 		} else {
 			info.addMessage(new WorkException(exc));
 		}
 	}
 
 	@Override
 	public void run() {
 		try {
 			/* wait for free slot */
 			WMConfig config = manager.getConfig();
			while (manager.getRunningJobs().size() >= config.getMaxJobs()) {
 				Thread.sleep(config.getPauseWaitInterval());
 			}
 
 			/* prepare the block */
 			checkCancel();
 			info.getStatus().setPhase(PHASE.BEFORE_START);
 			info.notifyListener();
 			worker.beforeStart();
 
 			/* fetch blockcount */
 			checkCancel();
 			info.getStatus().setPhase(PHASE.BLOCK_COUNT);
 			info.notifyListener();
 			int blockCount = worker.getBlockCount();
 			info.getStatus().setTotalBlocks(blockCount);
 
 			/* start blocks */
 			checkCancel();
 			info.getStatus().setPhase(PHASE.STARTING_BLOCKS);
 			info.notifyListener();
 			startBlocks(blockCount);
 
 			/* wait for blocks to finish */
 			checkCancel();
 			info.getStatus().setPhase(PHASE.RUNNING_BLOCKS);
 			info.notifyListener();
 			waitForBlocks();
 
 			/* cleanup after block */
 			checkCancel();
 			info.getStatus().setPhase(PHASE.AFTER_FINISH);
 			info.notifyListener();
 			worker.afterFinish();
 		} catch (Exception | Error ex) {
 			exceptionCaught(ex);
 		} finally {
 			info.setRunnerFinished();
 			info.getStatus().setPhase(PHASE.FINISHED);
 			info.notifyListener();
 		}
 	}
 
 	private void startBlocks(int blockCount) throws WorkException {
 		for (int i = 0; i < blockCount; i++) {
 			startNewThread(i);
 			checkCancel();
 		}
 	}
 
 	private void startNewThread(final int block) throws WorkException {
 		// create new thread
 		ThreadGroup group = getThreadGroup();
 		WorkerThread<E, T> thread = new WorkerThread<E, T>(group, this, worker,
 				block);
 
 		WMConfig config = manager.getConfig();
 		int maxThreadCount = config.getMaxThreadCount();
 		while (group.activeCount() > maxThreadCount) {
 			try {
 				Thread.sleep(config.getPauseWaitInterval());
 			} catch (InterruptedException iex) {
 				throw new WorkException(iex);
 			}
 		}
 
 		// actually start the work
 		thread.start();
 	}
 
 	@Override
 	public String toString() {
 		return getClass().getSimpleName() + "[" + getName() + ", info=" + info
 				+ "]";
 	}
 
 	private void waitForBlocks() throws WorkException {
 		ThreadGroup group = getThreadGroup();
 		WMConfig config = manager.getConfig();
 
 		while (group.activeCount() > 1) {
 			try {
 				Thread.sleep(config.getPauseWaitInterval());
 			} catch (InterruptedException iex) {
 				throw new WorkException(iex);
 			}
 		}
 	}
 }
