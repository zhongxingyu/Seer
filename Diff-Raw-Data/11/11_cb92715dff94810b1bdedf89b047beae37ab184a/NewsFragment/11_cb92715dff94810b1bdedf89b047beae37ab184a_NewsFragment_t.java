 package duyhung.news;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import duyhung.news.adapter.NewsAdapter;
 import duyhung.news.dao.RssReader;
 import duyhung.news.model.NewsItem;
 import duyhung.news.model.Variables;
 
 public class NewsFragment extends Fragment {
 
 	private ListView newsListView;
 	private ProgressDialog progressDialog;
 
 	private List<NewsItem> newsList;
 	private String linkRss;
 
 	public static final String ARG_POSITION = "ARG_POSITION";
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		newsList = new ArrayList<NewsItem>();
 		View v = inflater.inflate(R.layout.fragment_news, container, false);
 		newsListView = (ListView) v.findViewById(R.id.newsListView);
 		newsListView.setAdapter(new NewsAdapter(getActivity(), newsList));				
 		newsListView.setOnItemClickListener(onNewsItemClickListener);
		
		if(getArguments().getInt(ARG_POSITION) == 0){
			retrieveNews();
		}
 		return v;
 	}
 
 	public void retrieveNews() {
 		if(isAdded()) {
 			int position = getArguments().getInt(ARG_POSITION);
 			linkRss = getResources().getStringArray(R.array.vne_rss)[position];
 
 			if (Variables.SAVED_NEWS_LIST.containsKey(linkRss)) {
 				newsList = Variables.SAVED_NEWS_LIST.get(linkRss);
 				newsListView.setAdapter(new NewsAdapter(getActivity(), newsList));
 			} else {
 				progressDialog = new ProgressDialog(getActivity());
 				progressDialog.setMessage("Loading news ... ");
 				progressDialog.show();
 
 				new RetrieveNewsListTask().execute();
 			}
 		}
 	}
 
 	protected OnItemClickListener onNewsItemClickListener = new AdapterView.OnItemClickListener() {
 
 		@Override
 		public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
 			String newsLink = ((NewsItem) adapterView.getAdapter().getItem(position)).getLink();
 			Intent newsDetailsIntent = new Intent(getActivity(), NewsReadActivity.class);
 			newsDetailsIntent.putExtra(Variables.LINK, newsLink);
 			startActivity(newsDetailsIntent);
 		}
 
 	};
 
 	private class RetrieveNewsListTask extends AsyncTask<Void, Void, Void> {
 
 		@Override
 		protected Void doInBackground(Void... params) {
 			newsList = new RssReader().getNewsList(linkRss);
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Void result) {
 			if (isAdded()) {
 				super.onPostExecute(result);
 				newsListView.setAdapter(new NewsAdapter(getActivity(), newsList));
 				Variables.SAVED_NEWS_LIST.put(linkRss, newsList);
 				if (progressDialog != null) {
 					progressDialog.dismiss();
 				}
 			}
 		}
 
 	}
 
 }
