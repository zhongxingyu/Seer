 package com.hyperactivity.android_app.activities;
 
 import java.util.ArrayList;
 import java.util.Locale;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 import com.hyperactivity.android_app.R;
 import com.hyperactivity.android_app.core.Engine;
 import com.hyperactivity.android_app.forum.models.Category;
 import com.hyperactivity.android_app.forum.models.Thread;
 
 public class SearchFragment extends Fragment {
 	
 	ThreadListFragment searchResultList;
 	TextView noResultsText;
 	
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
 		View view = inflater.inflate(R.layout.search_fragment, null);
 		
 		searchResultList = new ThreadListFragment();
 		searchResultList.updateThreadList(new ArrayList<Thread>());
 		getFragmentManager().beginTransaction().replace(R.id.search_thread_list_container, searchResultList).commit();
		
		final EditText editText = (EditText)view.findViewById(R.id.search_text_field);
 		final Button button = (Button)view.findViewById(R.id.search_button);
 		button.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				String text = editText.getText().toString().trim();
 				querySearchString(text);
 			}
 		});
 		
 		return view;
 	}
 	
 	private void querySearchString(String text) {
 		text = text.toLowerCase(Locale.ENGLISH);
 		ArrayList<Thread> result = new ArrayList<Thread>();
 		
 		for (Category category : ((Engine) getActivity().getApplication()).getPublicForum().getCategories()) {
 			if (category.getThreads() == null) continue;
 			for (Thread thread : category.getThreads()) {
 				if (thread.getText().toLowerCase(Locale.ENGLISH).contains(text) || thread.getHeadLine().toLowerCase(Locale.ENGLISH).contains(text)) {
 					result.add(thread);
 				}
 			}
 		}
 		
 		searchResultList.updateThreadList(result);
 	}
	
 }
