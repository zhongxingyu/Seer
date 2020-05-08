 package nl.ansuz.android.asylum.tasks;
 
 import android.annotation.TargetApi;
 import android.os.AsyncTask;
 import android.os.Build;
 import java.util.concurrent.Executor;
 
 /**
  * Base data Task implementation.
  *
  * @author Wijnand
  */
 public abstract class DataTaskBase<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> implements DataTask {
 
	protected OnCompleteListener<Result> onCompleteListener;
 	protected OnExceptionListener onExceptionListener;
 
 	private RunType runType;
 	private boolean aborted;
 
 	/**
 	 * CONSTRUCTOR
 	 *
 	 * @param type RunType - How this Task should be run.
 	 */
 	public DataTaskBase(RunType type) {
 		super();
 		runType = type;
 		init();
 	}
 
 	/**
 	 * Initializes this class.
 	 */
 	protected void init() {
 		aborted = false;
 	}
 
 	/**
 	 * Hook that is called after data loading has finished successfully.
 	 *
 	 * @param result Result - The resulting object of loading the data.
 	 */
 	protected void onPostSuccess(Result result) {
 		// Do nothing.
 	}
 
 	/**
 	 * Hook that is called when there was an exception while loading the data.
 	 *
 	 * @param exception Exception - The Exception that was thrown while loading data.
 	 */
 	protected void onPostException(Exception exception) {
 		// Do nothing.
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	protected void onPreExecute() {
 		super.onPreExecute();
 		aborted = false;
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public void execute() {
 		execute(null, null);
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	public void executeOnExecutor(Executor executor) {
 		executeOnExecutor(executor, null, null);
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public void abort() {
 		aborted = true;
 		cancel(true);
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public void setOnCompleteListener(OnCompleteListener listener) {
 		onCompleteListener = listener;
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public void setOnExceptionListener(OnExceptionListener listener) {
 		onExceptionListener = listener;
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public RunType getType() {
 		return runType;
 	}
 
 	/**
 	 * @return Whether or not this Task has been aborted.
 	 */
 	public boolean isAborted() {
 		return aborted;
 	}
 }
