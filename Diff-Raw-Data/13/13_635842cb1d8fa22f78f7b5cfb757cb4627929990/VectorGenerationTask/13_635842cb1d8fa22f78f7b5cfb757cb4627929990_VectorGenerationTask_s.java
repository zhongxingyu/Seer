 package com.hstefan.threadingexample;
 
 import android.os.AsyncTask;
 import android.widget.Button;
 
 public class VectorGenerationTask extends AsyncTask<VectorGenerationData, Void, Float[][]> {
 	private int m_numElements;
	private Button m_bRun;
 	private OnTaskFinishedListener<Float[]> m_finListener;
 	
 	public VectorGenerationTask setOnFinishListener(OnTaskFinishedListener<Float[]> listener) {
 		this.m_finListener = listener;
 		return this;
 	}
 
 	@Override
 	protected Float[][] doInBackground(VectorGenerationData... data) {
 		m_numElements = data[0].getVectorLength();
		m_bRun = data[0].getButton();
		//m_bRun.setClickable(false);
		//m_bRun.setPressed(false);
 		
 		Float[][] ret = new Float[2][m_numElements];
 		for(int i = 0; i < m_numElements; ++i) {
 			ret[0][i] = (float) i;
 		}
 		
 		ret[1] = ret[0];
 		return ret;
 	}
 	
 	@Override
 	protected void onPostExecute(Float[][] result) {
 		if(m_finListener != null)
 			m_finListener.onTaskFinish(result);
 	}
 	
 	public interface OnTaskFinishedListener<T> {
 		void onTaskFinish(Float[][] result);
 	}
 }
