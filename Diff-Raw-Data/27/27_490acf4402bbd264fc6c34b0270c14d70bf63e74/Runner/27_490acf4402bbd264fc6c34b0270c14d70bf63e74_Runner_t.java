 package genlab.core.exec;
 
 import genlab.core.commons.ProgramException;
 import genlab.core.model.exec.ComputationState;
 import genlab.core.model.exec.IAlgoExecution;
 import genlab.core.model.exec.IComputationProgress;
 import genlab.core.model.exec.IComputationProgressSimpleListener;
 import genlab.core.usermachineinteraction.ListOfMessages;
 import genlab.core.usermachineinteraction.ListsOfMessages;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Takes many tasks, and executes them.
  * 
  * TODO use the map of threads to monitor and kill
  * TODO timeouts
  * 
  * @author Samuel Thiriot
  *
  */
 public class Runner extends Thread implements IComputationProgressSimpleListener {
 	
 	final static int MAX_THREADS = 8;
 	
 	final static int START_TASKS_SIZE = 500;
 	
 	
 	/**
 	 * Acts as the locker for: all, roots, done, ready, running, notReady
 	 */
 	final Set<IAlgoExecution> all = new HashSet<IAlgoExecution>(START_TASKS_SIZE);
 
 	final Set<IAlgoExecution> roots = new HashSet<IAlgoExecution>();;
 
 	final Set<IAlgoExecution> done = new HashSet<IAlgoExecution>(START_TASKS_SIZE);
 	final Set<IAlgoExecution> ready = new HashSet<IAlgoExecution>(START_TASKS_SIZE);
 	final Set<IAlgoExecution> running = new HashSet<IAlgoExecution>(START_TASKS_SIZE);
 	final Set<IAlgoExecution> notReady = new HashSet<IAlgoExecution>(START_TASKS_SIZE);	
 	
 	final Set<ITasksDynamicProducer> tasksProducers = new HashSet<ITasksDynamicProducer>();
 	
 	final Map<IAlgoExecution, Thread> exec2thread = new HashMap<IAlgoExecution, Thread>(START_TASKS_SIZE);
 	
 	/**
 	 * The number of threads used currently. Based on the number of thread displayed by tasks, 
 	 * not an objective measure.
 	 */
 	private int usedThreads = 0;
 	
 		
 	/**
 	 * If true, all tasks should be canceled as soon as possible.
 	 */
 	boolean cancel = false;
 	
 	private ListOfMessages messagesRun = null;
 	
 	private final Object lockerMainLoop = new Object();
 	
 	public Runner() {
 		
 		this.messagesRun =  ListsOfMessages.getGenlabMessages();
 		
 		setName("glRunner");
 		setPriority(MAX_PRIORITY);
 		setDaemon(true);
 		
 	}
 	
 	public void addTasks(Collection<IAlgoExecution> allTasks) {
 		for (IAlgoExecution e: allTasks)
 			addTask(e);	
 		
 	}
 
 	
 	public void addTask(IAlgoExecution exec) {
 		
 		synchronized (all) {
 			
 			if (all.contains(exec))
 				return;
 			
 			all.add(exec);
 		
 			switch (exec.getProgress().getComputationState()) {
 			case READY:
 				ready.add(exec);
 				break;
 			case CREATED:
 			case WAITING_DEPENDENCY:
 				notReady.add(exec);
 				break;
 			case FINISHED_CANCEL:
 			case FINISHED_FAILURE:
 			case FINISHED_OK:
 				// adding a task at this state is... weird.
 				done.add(exec);
 				break;
 			case STARTED:
 				// weird too
 				running.add(exec);
 				break;
 			default: 
 				throw new ProgramException("state of this task unknown: "+exec.getProgress().getComputationState());
 			}
 		}
 		
 		exec.getProgress().addListener(this);
 		
 		// wake up the thread an make him think about the idea of working.
 		wakeUp();
 		
 		//ExecutionHooks.singleton.notifyParentTaskAdded(exec);
 		
 	}
 	
 	protected void wakeUp() {
 		
 		messagesRun.traceTech("attempting to wake up the runner: \"Knock knock, Neo !\"", getClass());
 
 		synchronized (lockerMainLoop) {
 			lockerMainLoop.notifyAll();
 		}
 		
 	}
 
 	protected Collection<IAlgoExecution> detectRoots() {
 		
 		LinkedList<IAlgoExecution> res = new LinkedList<IAlgoExecution>();
 		
 		synchronized (all) {
 			for (IAlgoExecution e: all) {
 				if (e.getPrerequires().isEmpty() && e.getAlgoInstance().getContainer()==null)
 					res.add(e);
 			}				
 		}
 		
 		messagesRun.traceTech("found root tasks: "+res, getClass());
 		
 		return res;
 		
 	}
 	
 	protected void registerListeners() {
 		synchronized (all) {
 			for (IAlgoExecution e: all) {
 				e.getProgress().addListener(this);
 			}	
 		}
 	}
 	
 	/**
 	 * Called at the begining of the start method
 	 */
 	protected void initRun() {
 
 		// prepare our task
 		/*
 		task = new ContainerTask(this.getName());
 		synchronized (all) {
 			for (IAlgoExecution e: all) {
 				task.addTask(e);
 			}
 		}
 		*/
 
 		
 		// init progress
 		// TODO remove
 		//progress.setComputationState(ComputationState.STARTED);
 		//progress.setProgressTotal(1);
 		
 		// register listener
 		registerListeners();
 		
 		// init internal structures 
 		synchronized (all) {
 				
 			// at init: 
 			// - nothing is done
 			done.clear();
 			
 			// - roots are ready
 			ready.clear();
 			ready.addAll(detectRoots());
 			
 			// - nothing is running
 			running.clear();
 			
 			// - all others are not ready
 			notReady.clear();
 			notReady.addAll(all);
 			notReady.removeAll(ready);
 		
 		}
 		
 	}
 	
 	protected void printState() {
 		
 		StringBuffer sb = new StringBuffer();
 		
 		synchronized (all) {
 			sb
 				.append(done.size()).append(" done, ")
 				.append(exec2thread.size()).append(" running, ")
 				.append(ready.size()).append(" ready, ")
 				.append(notReady.size()).append(" waiting.");
 			
 		}
 		
 		messagesRun.traceTech(sb.toString(), getClass());
 		
 	}
 	
 	protected boolean proposeDynamicProducersToWork() {
 		
 		synchronized (tasksProducers) {
 			
 			if (tasksProducers.isEmpty())
 				return false;
 			
 			messagesRun.debugTech("proposing to tasks producers to submit novel tasks...", getClass());
 			
 			ITasksDynamicProducer producer = tasksProducers.iterator().next();
 			
 			messagesRun.debugTech("proposing to task producer "+producer+" to submit novel tasks...", getClass());
 				
 			IAlgoExecution t = producer.provideMoreTasks();
 					
 			if (t == null) {
 				tasksProducers.remove(producer);
 				messagesRun.debugTech("task producer "+producer+" has no more tasks", getClass());
 				return false;
 			} else {
 				addTask(t);
 				return true;
 			}
 			
 		}
 	}
 	
 	protected boolean attemptToDoSomething() {
 		
 		//messages.traceTech("attempting to do something", getClass());
 		
 		//printState();
 		
 		// are there enough resources ?
 		if (usedThreads >= MAX_THREADS) {
 			//messages.debugTech("all threads used, wait...", getClass());
 			return false;	// threads limit reached, do nothing.
 		}
 		
 		// is there something ready ?
 		IAlgoExecution e = null;
 		Thread t = null;
 		synchronized (all) {
 			
 			if (ready.isEmpty()) {
 				
 				//messages.traceTech("nothing ready, wait...", getClass());
 				if (running.isEmpty()) {
 					// TODO wait, nothing is gonna happen there ?
 					
 				}
 				
 				
 				return false; // nothing ready for run, leave.
 			}
 			
 			e = ready.iterator().next();
 			ready.remove(e);
 			running.add(e);
 		
 			if (!e.isCostless()) {
 				t = new Thread(e);
 				t.setName("gl_task_"+System.currentTimeMillis());
 				t.setDaemon(false);
 				t.setPriority(NORM_PRIORITY);
 				exec2thread.put(e, t);
 			}
 			usedThreads += e.getThreadsUsed();
 				
 		}
 		
 		
 		final ListOfMessages messages = e.getExecution().getListOfMessages();
 		
 		messagesRun.debugUser("now using "+usedThreads+" threads over "+MAX_THREADS, getClass());
 		
 		// actually run something
 	
 		if (t==null) {
 			
 			messages.debugUser("running task "+e+" (no thread, it is costless)", getClass());
 			e.run();
 		} else {
 			messages.debugUser("starting a thread for task "+e+": "+t, getClass());
 			final IAlgoExecution e2 = e;
 			
 			// add an exception handler to the thread;
 			t.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
 				
 				@Override
 				public void uncaughtException(Thread arg0, Throwable arg1) {
 					
 					arg1.printStackTrace();
 					messages.errorUser(
 							"thie algorithm \""+e2.getAlgoInstance().getName()+"\" ended with an error: "+arg1.getMessage(), 
 							getClass(), 
 							arg1
 							);
 					messages.warnTech(
 							"an algorithm "+e2.getAlgoInstance().getAlgo()+" raised an error that was not properly catched at its level: "+e2+", "+arg1.getClass(), 
 							getClass(),
 							arg1
 							);
 					
 					
 					e2.getProgress().setComputationState(ComputationState.FINISHED_FAILURE);
 
 				}
 			});
 			
 			// also add a watchdog
 			/* TODO restore the watchdog
 			(new WatchdogTimer(
 					e2.getTimeout(), 
 					e2.getProgress(), 
 					e2.getExecution().getListOfMessages())
 			).start();
 			*/
 			
 			t.start();
 		}
 		
 		printState();
 
 		
 		return true;
 		
 	}
 	
 	private boolean doingSomething = false;
 	
 	
 	public void attemptToDoThings() {
 		
 		if (doingSomething) { // avoids simultaneous execs
 			messagesRun.debugTech("already doing something !", getClass());
 			return;
 		}
 		
 		try {
 			
 			
 			doingSomething = true;
 			
 			while (true) {
 
 					//messages.debugTech("trying to do things.", getClass());
 				while (attemptToDoSomething()) {}
 				//messages.debugTech("nothing to do yet.", getClass());
 		
 				// when nothing more can be done, we may propose novel tasks :-) 
 				//System.err.println("threads used: "+usedThreads);
 				boolean proposeDynamicProcudersToWork;
 				synchronized (all) {
 					proposeDynamicProcudersToWork = ready.isEmpty() && usedThreads < MAX_THREADS;
 					// well, we could create new tasks !
 				}
 				if (proposeDynamicProcudersToWork && !proposeDynamicProducersToWork())
 						break;
 				
 			}
 			
 			
 		} finally {
 			doingSomething = false;
 		}
 	}
 	
 	public void run() {
 		
 		boolean displayMessage = false;
 		
 		initRun();
 	
 	
 		while (!cancel) {
 	
 			boolean nothingToDo;
 			synchronized (all) {
 				nothingToDo = running.isEmpty() && ready.isEmpty() && notReady.isEmpty(); 
 			}
 			
 			if (nothingToDo) {
 				
 				// really, there is nothing to do.
 				if (displayMessage) {
 					messagesRun.infoUser("all tasks done. ", getClass());
 					// we may display something there is relevant
 					displayMessage = false;
 				}		
 			
 			
 			} else {
 				displayMessage = true;
 				
 				attemptToDoThings();
 				
 
 			}
 			

			
			try {
				messagesRun.traceTech("nothing to do, sleeping.", getClass());
				synchronized (lockerMainLoop) {
					lockerMainLoop.wait();	
				}
				messagesRun.traceTech("wake up !", getClass());
			} catch (InterruptedException e) {
				messagesRun.traceTech("Runner thread interrupted o_O. This is not supposed to happen.", getClass());
				e.printStackTrace();
			}
			
			
 		}
 		
 	}
 	
 	public void cancelTasks() {
 		cancel = true;
 		
 		synchronized (all) {
 			for (IAlgoExecution e: all) {
 				switch (e.getProgress().getComputationState()) {
 				case CREATED:
 				case READY:
 				case STARTED:
 				case WAITING_DEPENDENCY:
 					e.getProgress().setComputationState(ComputationState.FINISHED_CANCEL);	
 				}
 				
 			}
 		}
 	}
 
 	@Override
 	public void computationStateChanged(IComputationProgress progress) {
 		
 		//messages.traceTech("computation state changed: "+progress.getAlgoExecution()+": "+progress.getComputationState(), getClass());
 		
 		IAlgoExecution e = progress.getAlgoExecution();
 
 		boolean wakeUp = false;
 		
 		final ListOfMessages messages = e.getExecution().getListOfMessages();
 		
 		synchronized (all) {
 	
 			// check event
 			if (!all.contains(e)) {
 				all.add(e);
 			}
 				//throw new ProgramException("oops, I receive events which are not really of interest to me, like: "+progress.getAlgoExecution().getAlgoInstance());
 			
 			switch (progress.getComputationState()) {
 			
 			// a task was running, and ended
 			case FINISHED_FAILURE:
 				cancelTasks();
 				
 			case FINISHED_OK:
 			case FINISHED_CANCEL:
 				messages.traceTech("task finished: "+e+" ("+progress.getDurationMs()+" ms)", getClass());
 				if (running.contains(e)) {
 					running.remove(e);
 					done.add(e);
 					exec2thread.remove(e);
 					usedThreads -= e.getThreadsUsed();
 				}
 				wakeUp = true;
 				break;
 				
 			// a task was waiting for dependancy, and received all inputs
 			case READY:
 				messages.traceTech("task is now ready: "+e, getClass());
 				notReady.remove(e);
 				ready.add(e);
 				wakeUp = true;
 				break;
 				
 			case WAITING_DEPENDENCY:
 			case STARTED:
 			case CREATED:
 				// ignore;
 				break;
 				
 			default:
 				throw new ProgramException("unknown state: "+progress.getComputationState());
 			}
 		
 		}
 		
 		if (wakeUp)
 			wakeUp();
 
 	}
 
 	public void cancel() {
 		cancelTasks();
 		
 	}
 	
 	public void kill() {
 		// TODO
 		cancelTasks();
 	}
 	
 	public void registerTasksDynamicProducer(ITasksDynamicProducer producer) {
 		synchronized (tasksProducers) {
 			messagesRun.debugTech("registered a novel tasks producer: "+producer, getClass());
 			tasksProducers.add(producer);
 		}
 		wakeUp();
 	}
 }
