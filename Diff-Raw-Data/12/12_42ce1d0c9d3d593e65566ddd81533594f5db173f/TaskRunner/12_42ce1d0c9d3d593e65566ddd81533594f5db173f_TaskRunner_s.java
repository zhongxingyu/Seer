 package com.anddevbg.andlib.task;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.annotation.SuppressLint;
 import android.os.AsyncTask;
 import android.os.AsyncTask.Status;
 
 import com.anddevbg.andlib.log.LogWrapper;
 import com.anddevbg.andlib.task.runner.SyncTaskRunnerWorker;
 
 /**
  * 
 * Convenient class to execute sequence of tasks for a result.
  * 
  * @author anddevbg@gmail.com
  *
  */
 public abstract class TaskRunner<T> implements ITaskRunnerWorkerCallback<T> {
 
 	private List<Task<T>> mTasks;
 	private ITaskRunnerWorker<T> mWorker;
 	private ITaskRunnerCallback<T> mCallback;
 	
 	private T mResultObject;
 
 	public TaskRunner() {
 		this(new SyncTaskRunnerWorker<T>(), null);
 	}
 
 	public TaskRunner(ITaskRunnerWorker<T> worker, ITaskRunnerCallback<T> callback) {
 		synchronized (this) {
 			mCallback = callback;
 		}
 		
 		mResultObject = createResultObject();
 		mWorker = worker;
 		mWorker.setCallback(this);
 	}
 
 	public void setTasks(List<Task<T>> tasks) {
 		if (mWorker.getStatus() != Status.PENDING) {
 			throw new RuntimeException("Tasks already has been executed or are in process of executing.");
 		}
 
 		mTasks = new ArrayList<Task<T>>(tasks);
 	}
 
 	@SuppressLint("NewApi")
 	public void start() {
 		LogWrapper.d(this, "---- Start ----");
 		
 		if (mTasks == null || mTasks.isEmpty()) {
 			throw new RuntimeException("Task list is empty.");
 		}
 		
 		mWorker.start(mResultObject, mTasks);
 	}
 
 	public void stop() {
 		LogWrapper.d(this, "---- Stop ----");
 		
 		if (mWorker.getStatus() == AsyncTask.Status.RUNNING) {
 			synchronized (this) {
 				mCallback = null;
 			}
 			mWorker.stop();
 		}
 	}
 
 	public void setCallback(ITaskRunnerCallback<T> callback) {
 		synchronized (this) {
 			mCallback = callback;
 		}
 	}
 	
 	public Status getStatus() {
 		return mWorker.getStatus();
 	}
 
 	@Override
 	public void onWorkerJobStart() {
 		synchronized (this) {
 			if (mCallback != null) {
 				mCallback.onJobStart();
 			}
 		}
 	}
 
 	@Override
 	public void onWorkerTaskComplete(TaskProgress<T> fetchProgress) {
 		synchronized (this) {
 			if (mCallback != null) {
 				mCallback.onProgress(mResultObject, fetchProgress);
 			}
 		}
 	}
 
 	@Override
 	public void onWorkerJobComplete(boolean isCancelled) {
 		synchronized (this) {
 			if (mCallback != null) {
 				if (mResultObject != null) {
 					mCallback.onJobComplete(mResultObject);
 				} else {
 					mCallback.onJobFailed();
 				}
 			}
 		}
 		
 		mResultObject = null;
 	}
 	
 	protected abstract T createResultObject();
 }
