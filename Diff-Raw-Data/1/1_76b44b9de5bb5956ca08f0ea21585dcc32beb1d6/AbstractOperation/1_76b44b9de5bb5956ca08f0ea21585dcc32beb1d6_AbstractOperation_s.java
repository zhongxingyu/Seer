 package de.uniluebeck.itm.devicedriver.operation;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import de.uniluebeck.itm.devicedriver.State;
 import de.uniluebeck.itm.devicedriver.async.AsyncCallback;
 import de.uniluebeck.itm.devicedriver.event.StateChangedEvent;
 import de.uniluebeck.itm.devicedriver.exception.TimeoutException;
 
 /**
  * An abstract operation.
  * If no other timeout is set the operation will be canceled automatically after the <code>DEFAULT_TIMEOUT</code>.
  * The result of a timed out operation is null also when the operation completed at the same time.
  * 
  * @author Malte Legenhausen
  *
  * @param <T> The return type of the operation.
  */
 public abstract class AbstractOperation<T> implements Operation<T> {
 	
 	/**
 	 * Default timeout is set to 5 minutes.
 	 */
 	public static final long DEFAULT_TIMEOUT = 30000;
 	
 	/**
 	 * Logger for this class.
 	 */
 	private static final Logger logger = LoggerFactory.getLogger(Operation.class);
 	
 	/**
 	 * Sub <code>Operation</code> that is currently running.
 	 */
 	private Operation<?> subOperation;
 	
 	/**
 	 * Listeners for <code>OperationContainer</code> changed.
 	 */
 	private final List<OperationListener<T>> listeners = new ArrayList<OperationListener<T>>();
 	
 	/**
 	 * The timeout after which the application will be canceled.
 	 */
 	private long timeout = DEFAULT_TIMEOUT;
 	
 	/**
 	 * The callback that is called when the operation has finished, canceled or when an exception occured.
 	 */
 	private AsyncCallback<T> callback;
 	
 	/**
 	 * The current state of the <code>Operation</code>.
 	 */
 	private State state = State.WAITING;
 	
 	/**
 	 * <code>Timer</code> that executes the timeout operation.
 	 */
 	private final Timer timer = new Timer(getClass().getName());
 	
 	/**
 	 * Boolean thats stores if the operatio has to be canceled.
 	 */
 	private boolean canceled;
 	
 	/**
 	 * The task that will be executed when the timeout occurs.
 	 */
 	private final TimerTask task;
 	
 	/**
 	 * Constructor.
 	 */
 	public AbstractOperation() {
 		task = new TimerTask() {			
 			@Override
 			public void run() {
 				onTimeout();
 			}
 		};
 	}
 	
 	/**
 	 * Method is called when a timeout occured.
 	 */
 	protected void onTimeout() {
 		setState(State.TIMEDOUT);
 		callback.onFailure(new TimeoutException("Operation timeout " + timeout + "ms reached."));
 	}
 	
 	@Override
 	public void setAsyncCallback(final AsyncCallback<T> callback) {
 		this.callback = callback;
 	}
 	
 	@Override
 	public T call() {
 		setState(State.RUNNING);
 		scheduleTimeout();
 		
 		callback.onExecute();
 		T result = null;
 		try {
 			// Cancel execution if operation was canceled before operation changed to running.
 			if (!canceled) {
 				result = execute(callback);
 			}
 		} catch (Exception e) {
 			setState(State.EXCEPTED);
 			logger.error("Exception during operation execution", e);
 			callback.onFailure(e);
 			return null;
 		} finally {
 			cancelTimeout();
 		}
 		
 		// Do nothing after a timeout happens and execute finished.
 		synchronized (state) {
 			if (state.equals(State.TIMEDOUT)) {
 				logger.warn("Operation finsihed but timeout occured.");
 				return null;
 			}
 		}	
 		
 		if (canceled) {
 			setState(State.CANCELED);
 			callback.onCancel();
 		} else {
 			setState(State.DONE);
 			callback.onSuccess(result);
 		}
 		return result;
 	}
 	
 	private void scheduleTimeout() {
 		logger.debug("Schduling timeout timer (Timout: + " + timeout + "ms");
 		timer.schedule(task, timeout);
 	}
 	
 	private void cancelTimeout() {
 		logger.debug("Canceling timeout timer");
 		timer.cancel();
 	}
 	
 	
 	/**
 	 * Call this method when another <code>Operation</code> has to be executed while this <code>Operation</code>.
 	 * 
 	 * @param <R> The return type of the sub <code>Operation</code>.
 	 * @param operation The sub <code>Operation</code> that has to be executed.
 	 * @return The result of the sub <code>Operation</code>.
 	 */
 	protected <R> R executeSubOperation(Operation<R> operation) throws Exception {
 		subOperation = operation;
 		final R result = operation.execute(callback);
 		subOperation = null;
 		return result;
 	}
 	
 	/**
 	 * Thread safe state change function.
 	 * 
 	 * @param newState The new State of this operation.
 	 */
 	private void setState(final State newState) {
 		synchronized (state) {
 			final State oldState = state;
 			state = newState;
 			fireStateChangedEvent(new StateChangedEvent<T>(this, oldState, newState));
 		}
 	}
 	
 	/**
 	 * Notify all listeners that the state has changed.
 	 * 
 	 * @param oldState The old state.
 	 * @param newState The new state.
 	 */
 	private void fireStateChangedEvent(final StateChangedEvent<T> event) {
 		logger.debug("Operation state changed from " + event.getOldState() + " to " + event.getNewState());
 		for (OperationListener<T> listener : listeners.toArray(new OperationListener[listeners.size()])) {
 			listener.onStateChanged(event);
 		}
 	}
 
 	@Override
 	public State getState() {
 		return state;
 	}
 	
 	/**
 	 * Method will throw an <code>IllegalStateException</code> when 
 	 * trying to change the timeout when the operation is in running state.
 	 */
 	@Override
 	public void setTimeout(long timeout) {
 		if (timeout < 0) {
 			throw new IllegalArgumentException("Negativ timeout is not allowed");
 		}
 		if (state.equals(State.RUNNING)) {
 			throw new IllegalStateException("Timeout can not be set when operation is in running state");
 		}
 		this.timeout = timeout;
 	}
 
 	@Override
 	public long getTimeout() {
 		return timeout;
 	}
 	
 	@Override
 	public void addListener(OperationListener<T> listener) {
 		listeners.add(listener);
 	}
 	
 	@Override
 	public void removeOperationListener(OperationListener<T> listener) {
 		listeners.remove(listener);
 	}
 
 	@Override
 	public void cancel() {
 		if (subOperation != null) {
 			subOperation.cancel();
 		}
 		canceled = true;
 	}
 	
 	@Override
 	public boolean isCanceled() {
 		return canceled;
 	}
 	
 	@Override
 	protected void finalize() {
 		timer.cancel();
 	}
 }
