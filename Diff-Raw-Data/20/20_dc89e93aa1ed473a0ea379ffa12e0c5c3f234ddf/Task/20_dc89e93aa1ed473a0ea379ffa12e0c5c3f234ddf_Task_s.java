 package com.hstefan.threadingexample.task;
 
 import java.util.Observable;
 
 import android.os.AsyncTask;
 
 public abstract class Task<ParamT, ResultT> extends Observable {
	private AsyncTask<ParamT, Void, ResultT> m_task;
 	private ParamT m_params[];
 	
 	public abstract ResultT onRun(ParamT... params);
 	
 	private class InternalTask extends AsyncTask<ParamT, Void, ResultT> {
 		private ParamT m_params[];
 		private Task<ParamT, ResultT> m_task;
 		
 		public InternalTask(Task<ParamT, ResultT> task, ParamT...params) {
 			m_params = params;
 		}
 		
 		@Override
 		protected ResultT doInBackground(ParamT... params) {
 			return m_task.onRun(m_params);
 		}
 		
 		@Override
 		protected void onPostExecute(ResultT result) {
 			m_task.notifyObservers(result);
 		}
 	}
 	
 	public Task<ParamT, ResultT> setParams(ParamT... params) {
 		m_params = params;
 		return this;
 	}
 	
 	public void run() {
		m_task = new InternalTask(this, m_params);
		m_task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, m_params);
 	}
 }
