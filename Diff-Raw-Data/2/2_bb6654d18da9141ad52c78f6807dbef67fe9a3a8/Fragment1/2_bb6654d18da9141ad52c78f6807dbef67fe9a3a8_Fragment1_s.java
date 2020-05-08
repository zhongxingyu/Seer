 package ua.ck.android.geekhubandroidfeedreader.fragments;
 
 import ua.ck.android.geekhubandroidfeedreader.MainActivity;
 import ua.ck.android.geekhubandroidfeedreader.R;
 import ua.ck.android.geekhubandroidfeedreader.adapters.ArticleAdapterGeekHub;
 import ua.ck.android.geekhubandroidfeedreader.db.Article;
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 
 import com.actionbarsherlock.app.SherlockFragment;
 
 public class Fragment1 extends SherlockFragment {
 	ListView listArticles;
 	ArticleAdapterGeekHub adapter;
 	Article articlesEmpty[];
 	onShowArticle showArticleInterface;
 	
 	public interface onShowArticle {
 	    public void showArticle(Article article);
 	  }
 	
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		Log.i("Fragment1","onResume");
 		if (((MainActivity)getSherlockActivity()).getIsShowingAll()){
 			if (((MainActivity)getSherlockActivity()).articlesAll != null){
 				update(((MainActivity)getSherlockActivity()).articlesAll);
 			}
 		}else{
 			if (((MainActivity)getSherlockActivity()).arts != null){
 				update(((MainActivity)getSherlockActivity()).arts);
 			}
 		}
 		if (((MainActivity)getSherlockActivity()).state == ((MainActivity)getSherlockActivity()).STATE_ARTICLE_ONLY){
 			((MainActivity)getSherlockActivity()).state = ((MainActivity)getSherlockActivity()).STATE_LIST_ONLY;
 			((MainActivity)getSherlockActivity()).invalidateOptionsMenu();
 		}
 	}
 
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 		try {
 			showArticleInterface = (onShowArticle) activity;
 		} catch (ClassCastException e) {
 	    	throw new ClassCastException(activity.toString() + " must implement onShowArticle");
 	    }
 	}
 
 	public void update(Article[] newArticles){
 		adapter.ChangeData(newArticles);
 		listArticles.setAdapter(adapter);
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
 		View v = inflater.inflate(R.layout.fragment_1, null);
		setRetainInstance(true);
 		listArticles = (ListView)v.findViewById(R.id.listArticles);
 		adapter = new ArticleAdapterGeekHub(articlesEmpty, inflater);
 		listArticles.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				showArticleInterface.showArticle(adapter.getItem(position));
 			}
 		});
 		
 		return v;
 	}
 
 }
