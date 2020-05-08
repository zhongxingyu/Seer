 package vs.piratenpartei.ch.app.forum;
 
 import java.io.IOException;
 import java.util.List;
 
 import vs.piratenpartei.ch.app.R;
 import vs.piratenpartei.ch.app.ThreadActivity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.util.Log;
 import android.view.View;
 import android.widget.AbsListView;
 import android.widget.ListView;
 
 public class ForumFragment extends ListFragment 
 {
 	private static final int ITEMS_PER_PAGE = 25;
 	private static final String TAG = "vs.piratenpartei.ch.app.forum.ForumFragment";
 	private BoardListAdapter _arrayAdapter;
 	private boolean _isFullPage = false;
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState)
 	{
 		super.onActivityCreated(savedInstanceState);
 		Log.d(TAG, "onActivityCreated()");
 		this.getListView().setOnScrollListener(new AbsListView.OnScrollListener() 
 		{			
 			private static final String TAG_EXT = ".onScrollListener";
 			@Override
 			public void onScrollStateChanged(AbsListView view, int scrollState) 
 			{
 				Log.d(TAG + TAG_EXT, "onScrollStateChanged(" + view.toString() + ", " + scrollState + ")");
 			}
 
 			@Override
 			public void onScroll(AbsListView view, int firstVisibleItem,
 					int visibleItemCount, int totalItemCount) 
 			{
 				Log.d(TAG + TAG_EXT, "onScroll(" + view.toString() + ", " + firstVisibleItem + ", " + visibleItemCount + ", " + totalItemCount + ")");
 				if(_isFullPage)
 				{
 					if(firstVisibleItem >= (totalItemCount - visibleItemCount))
 					{
 						getActivity().setProgressBarIndeterminateVisibility(true);
 						new BoardLoaderTask(totalItemCount / ITEMS_PER_PAGE).execute();
 					}
 				}
 			}
 		});
 	}
 
 	@Override
 	public void onViewCreated(View view, Bundle savedInstanceState)
 	{
 		super.onViewCreated(view, savedInstanceState);
 		Log.d(TAG, "onViewCreated()");
 		getActivity().setProgressBarIndeterminateVisibility(true);
 		new BoardLoaderTask().execute();
 	}
 
 	@Override
 	public void onListItemClick(ListView pListView, View pView, int pPosition, long pId)
 	{
 		Log.d(TAG, "onListItemClick(" + pListView.toString() + ", " + pPosition + ", " + pId + ")");
 		ThreadItem selectedItem = _arrayAdapter.getData().get(pPosition);
 		String title = selectedItem.getTitle();
 		String topicLink = selectedItem.getTopicLink();
 		Intent intent = new Intent(getActivity(), ThreadActivity.class);
 		Bundle params = new Bundle();
 		params.putString("title", title);
 		params.putString("topicUrl", topicLink);
 		intent.putExtras(params);
 		startActivity(intent);
 	}
 
 	private class BoardLoaderTask extends AsyncTask<Void, Void, Void>
 	{
 		private static final String TAG_EXT = ".BoardLoaderTask";

 		private int _offset;
 		private List<ThreadItem> _newThreads;
 
 		public BoardLoaderTask()
 		{
 			super();
 			Log.d(TAG + TAG_EXT, "new BoardLoaderTask()");
 			this._offset = 0;
 		}
 
 		public BoardLoaderTask(int pPage)
 		{
 			super();
 			Log.d(TAG + TAG_EXT, "new BoardLoaderTask(" + pPage + ")");
 			this._offset = pPage * ITEMS_PER_PAGE;
 		}
 
 		@Override
 		protected Void doInBackground(Void... params) 
 		{
 			Log.d(TAG + TAG_EXT, "doInBackground()");
 			try {
				_newThreads = ThreadItem.getBoard(174, this._offset);
				_isFullPage = (_newThreads.size() == ITEMS_PER_PAGE);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Void result)
 		{
 			Log.d(TAG + TAG_EXT, "onPostExecute()");
			if(getView() != null)
 			{
 				Log.d(TAG + TAG_EXT, "Loaded Items: " + _newThreads.size());
 				if(_arrayAdapter == null)
 				{
 					_arrayAdapter = new BoardListAdapter(getActivity(), R.layout.forum_list_item, _newThreads);
 					setListAdapter(_arrayAdapter);
 					setListShown(true);
 				}
 				else
 				{
 					_arrayAdapter.addAll(_newThreads);
 					_arrayAdapter.notifyDataSetChanged();
 				}
 			}
 			getActivity().setProgressBarIndeterminateVisibility(false);
 		}
 
 	}
 }
