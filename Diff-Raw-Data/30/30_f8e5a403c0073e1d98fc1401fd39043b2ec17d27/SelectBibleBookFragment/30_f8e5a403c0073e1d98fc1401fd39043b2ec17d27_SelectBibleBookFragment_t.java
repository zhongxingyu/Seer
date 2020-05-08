 package com.app.wordservant.ui;
 
 import java.util.concurrent.SynchronousQueue;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 import com.actionbarsherlock.app.SherlockListFragment;
 import com.app.wordservant.R;
 import com.app.wordservant.bible.Bible;
 import com.app.wordservant.util.BibleImporter;
 import com.app.wordservant.util.Constants;
 
 public class SelectBibleBookFragment extends SherlockListFragment{
 	private class WaitForBibleLoad extends AsyncTask<Void, Void, Void>{
 
 		@Override
 		protected Void doInBackground(Void... arg0) {
 			if(!Bible.getInstance().mImportStatus.equals(Constants.IMPORT_STATUS_IMPORTING)){
 				Runnable bibleSetup = new BibleImporter(getResources().openRawResource(R.raw.kjv));
 				ThreadPoolExecutor tpe = new ThreadPoolExecutor(10, 10, 10000, TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>());
 				tpe.execute(bibleSetup);
 			}
 			while(!Bible.getInstance().isImported()){
 				if(this.isCancelled())
 					return null;
 			}
 			return null;
 		}
 
 		protected void onPostExecute(Void bible){
 			assert Bible.getInstance()!=null;
			if(getActivity()==null)
				return;
 			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, Bible.getInstance().getBookNames());
 			SelectBibleBookFragment.this.setListAdapter(adapter);
 			if (SelectBibleBookFragment.this.isVisible())
 				SelectBibleBookFragment.this.setListShown(true);
 		}
 	}
 	private ArrayAdapter<String> mAdapter;
	private AsyncTask<Void, Void, Void> mBibleLoader;
 	public void onActivityCreated(Bundle savedInstanceState){
 		super.onActivityCreated(savedInstanceState);
 		if(!Bible.getInstance().isImported()){
 			this.setListShown(false);
			mBibleLoader = new WaitForBibleLoad().execute();
 		}else{
 			mAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, Bible.getInstance().getBookNames());
 			setListAdapter(mAdapter);
 			setListShown(true);
 		}
 	}
 	public void onViewCreated(View view, Bundle savedInstanceState){
 		super.onViewCreated(view, savedInstanceState);
 		((SelectScriptureFragmentActivity) getActivity()).getSupportActionBar().setTitle("Select Scripture");
 	}
 	@SuppressWarnings("unchecked")
 	public void onListItemClick(ListView list, View v, int position, long id){
 		((SelectScriptureFragmentActivity) getActivity()).updateBook(position, ((ArrayAdapter<String>) list.getAdapter()).getItem(position));
 	}
	
	public void onDestroy(){
		super.onDestroy();
		if(mBibleLoader!=null)
			mBibleLoader.cancel(true);
	}
 }
