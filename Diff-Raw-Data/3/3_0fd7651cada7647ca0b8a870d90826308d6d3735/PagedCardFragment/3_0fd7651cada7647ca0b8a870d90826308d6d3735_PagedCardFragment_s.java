 package com.campus.prime.ui;
 
 import java.util.List;
 
 
 import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
 import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.content.Loader;
 import android.view.View;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 
 import com.afollestad.cardsui.CardBase;
 import com.campus.prime.R;
 import com.campus.prime.core.MessagePage;
 import com.campus.prime.core.PageBase;
 import com.campus.prime.utils.CommonLog;
 import com.campus.prime.utils.LogFactory;
 
 public abstract class PagedCardFragment<E extends CardBase<E>> extends CardListFragment<E>{
 	protected CommonLog log = LogFactory.createLog();
 	
 	/**
 	 * Entity Page
 	 */
 	protected PageBase currentPage = new MessagePage();
 	
 	/**
 	 *  sign for prompt the direction refresh
 	 *  true  down
 	 *  false up
 	 */
 	protected boolean sign = false;
 	
 	private PullToRefreshAttacher mPullToRefreshAttacher;
 	
 	protected PageBase getCurrentPage(){
 		return currentPage;
 	}
 	
 	protected void setCurrentPage(PageBase page){
 		this.currentPage = page;
 	}
 	
 	
 	
 	@Override
 	public void onViewCreated(View view, Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onViewCreated(view, savedInstanceState);
 		
 		
 		mPullToRefreshAttacher = ((BaseActivity)this.getActivity()).getPullToRefreshAttacher();	
 		PullToRefreshLayout ptrLayout = (PullToRefreshLayout)view.findViewById(R.id.ptr_layout);
 		ptrLayout.setPullToRefreshAttacher(mPullToRefreshAttacher,new PullToRefreshAttacher.OnRefreshListener() {
 			
 			@Override
 			public void onRefreshStarted(View view) {
 				// TODO Auto-generated method stub
 				getActivity().setProgressBarIndeterminateVisibility(true);
 				new GetDataTask().execute();
 			}
 		});
 		listView.setOnScrollListener(new OnScrollListener() {
 			@Override
 			public void onScrollStateChanged(AbsListView view, int scrollState) {
 				// TODO Auto-generated method stub
 				if(view.getLastVisiblePosition() == view.getCount() - 1 && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE){
 					sign = true;
 					getActivity().setProgressBarIndeterminateVisibility(true);
 					new GetDataTask().execute();
 					log.i("refresh");
 				}
 			}
 			
 			@Override
 			public void onScroll(AbsListView view, int firstVisibleItem,
 					int visibleItemCount, int totalItemCount) {
 				// TODO Auto-generated method stub
 			}
 		});
 	}
 	
 
 	@Override
 	public Loader<List<E>> onCreateLoader(int arg0, Bundle arg1) {
 		// TODO Auto-generated method stub
 		// intialize MessageService
 		getActivity().setProgressBarIndeterminate(true);
 		return new AsyncLoader<List<E>>(getActivity()) {
 
 			@Override
 			protected List<E> loadData() {
 				// TODO Auto-generated method stub
 				return load();
 			}
 		};
 	}
 	
 
 	@Override
 	public void onLoadFinished(Loader<List<E>> arg0, List<E> arg1) {
 		// TODO Auto-generated method stub
 		super.onLoadFinished(arg0, arg1);
 		if(!isUsable())
 			return;
 		this.items = arg1;
 		if(items != null)
 			getListAdapter().set(items);
 	}
 	
 	public class GetDataTask extends AsyncTask<Void,Void,List<E>>{
 
 		@Override
 		protected List<E> doInBackground(Void... arg0) {
 			// TODO Auto-generated method stub
 			if(!sign){
 				return first();
 			}
 			else{
 				return next();
 			}
 		}
 			
 		@Override
 		protected void onPostExecute(List<E> result) {
 			// TODO Auto-generated method stub
 			if(!isUsable())
 				return;
 			if(!sign){
 				items = result;
 				mPullToRefreshAttacher.setRefreshComplete();
 			}
 			else{
 				if(result != null){
 					items.addAll(result);
 				}
 				sign = false;
 			}
			getListAdapter().set(items);
 			getActivity().setProgressBarIndeterminateVisibility(false);
 			super.onPostExecute(result);
 		}
 	}
 	
 	/**
 	 * async load data
 	 * @return
 	 */
 	protected abstract List<E> load();
 	/**
 	 * get first page
 	 */
 	protected List<E> first(){
 		return load();
 	}
 	
 	/**
 	 * get next page
 	 */
 	protected abstract List<E> next();
 	
 	/**
 	 * has next page
 	 */
 	protected boolean hasNext(){
 			return (currentPage == null || currentPage.getNext() == null) ? false : true;
 	}
 	
 }
