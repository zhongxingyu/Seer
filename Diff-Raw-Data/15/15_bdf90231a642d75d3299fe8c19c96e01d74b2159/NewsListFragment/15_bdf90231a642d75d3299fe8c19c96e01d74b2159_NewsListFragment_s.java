 package edu.sysuedaily.ui;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import android.R.integer;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 
 import com.actionbarsherlock.app.SherlockListFragment;
 
 import edu.sysuedaily.R;
 
 public class NewsListFragment extends SherlockListFragment {
 	
	static NewsListFragment getInstant(int position) {
 		NewsListFragment fragment = new NewsListFragment();
 		Bundle argsBundle = new Bundle();
 		argsBundle.putInt("showId", position);
 		fragment.setArguments(argsBundle);
 		
 		return fragment;
 	}
 	
 	
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
		int pos = getArguments().getInt("showInt");
 		
 		System.out.println(pos);
 		
 		super.onCreate(savedInstanceState);
 	}
 
 
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
		int pos = getArguments().getInt("showInt");
 		
 		ArrayList<Map<String, ?>> arrayList2 = new ArrayList<Map<String,?>>();
 		HashMap<String, Object> item1 = new HashMap<String, Object>();
 		if (pos == 0) {
 			item1.put("TITLE", "新闻条目1");
 			arrayList2.add(item1);
 		}
 		HashMap<String, Object> item2 = new HashMap<String, Object>();
 		if (pos == 1) {
 			item2.put("TITLE", "新闻条目2");
 			arrayList2.add(item2);
 		}
 		SimpleAdapter adapter = new SimpleAdapter(getActivity(), arrayList2, 
 				R.layout.listcontent_activity_news, new String[] {"TITLE"}, new int[] {R.id.textview_newstitle_listcontent_activity_news});
 		
 		setListAdapter(adapter);
 		
 		super.onActivityCreated(savedInstanceState);
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		// TODO Auto-generated method stub
 		super.onSaveInstanceState(outState);
 	}
 
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		
 	}
 
 }
