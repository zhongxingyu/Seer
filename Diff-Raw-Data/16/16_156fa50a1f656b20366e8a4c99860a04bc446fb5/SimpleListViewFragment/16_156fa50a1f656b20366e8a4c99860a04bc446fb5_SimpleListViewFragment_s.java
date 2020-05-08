 package com.siteduzero.android.lists.simple;
 
 import com.siteduzero.android.R;
 
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 
 public class SimpleListViewFragment extends ListFragment {
	public static final String TAG = "ListViewFragment";
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.fragment_listview, container, false);
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 
		String[] items = getResources().getStringArray(R.array.list_examples);

		ArrayAdapter<String> aa = new ArrayAdapter<String>(getActivity(),
 				android.R.layout.simple_list_item_1, items);
 		setListAdapter(aa);
 	}
 }
