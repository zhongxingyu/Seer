 package org.omships.omships;
 
 import android.os.Bundle;
 import android.content.Intent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.support.v4.app.Fragment;
 
 public abstract class FeedFragment extends Fragment {
 	
 	abstract Feed[] getFeeds(); 
 	
 	class NewsItemClicked implements OnItemClickListener{
 		@Override
 		public void onItemClick(AdapterView<?> adaptor, View view,
 				int postion, long id) {
 			RSSItem item = (RSSItem) adaptor.getItemAtPosition(postion);
 			Intent intent = new Intent(getActivity().getApplicationContext(),NewsItemView.class);
			intent.putExtra("item",item);
 			startActivity(intent);
 		}
 	}//end class NewsItemClicked
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle SavedInstanceState){
 		return inflater.inflate(R.layout.feedlayout, container, false);
 	}
 	
 	@Override
 	public void onStart() {
 		super.onStart();
 		Settings.loadConfig(getResources());
 		ListView rssList = (ListView) getView().findViewById(R.id.newslist);
 		new FetchItems(getActivity(),rssList).execute(getFeeds());
 		rssList.setOnItemClickListener(new NewsItemClicked());
 	}
 
 }
