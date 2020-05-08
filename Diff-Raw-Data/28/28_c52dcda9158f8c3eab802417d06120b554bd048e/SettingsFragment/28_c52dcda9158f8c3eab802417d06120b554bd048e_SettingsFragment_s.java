 package com.mates120.myword.ui;
 
 import java.util.List;
 
 import com.mates120.myword.AvailableDictionaries;
 import com.mates120.myword.Dictionary;
 import com.mates120.myword.R;
 
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.CheckedTextView;
 import android.widget.ListView;
 
 public class SettingsFragment extends ListFragment {
 	
 	private AvailableDictionaries availableDictionaries;
 	private List<Dictionary> dicts;
 	private DictionaryArrayAdapter mAdapter;
 

 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		availableDictionaries = AvailableDictionaries.getInstance(this.getActivity());
 		dicts = availableDictionaries.getList();
 		mAdapter = new DictionaryArrayAdapter(getActivity(),
 				android.R.id.list, dicts);
 		setListAdapter(mAdapter);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View view = inflater.inflate(R.layout.fragment_settings, container, false);
 		return view;
 	}
 	
 	@Override
     public void onListItemClick(ListView l, View view, int position, long id) {
 		CheckedTextView textView = (CheckedTextView) view;
 		if (textView.isChecked())
 		{
 			availableDictionaries.setDictionaryActive(textView.getText().toString(), false);
 			Log.i("SET DICTIONARY", "SET FALSE");
 		}
 		else
 		{
 			availableDictionaries.setDictionaryActive(textView.getText().toString(), true);
 			Log.i("SET DICTIONARY", "SET TRUE");
 		}
 		textView.setChecked(!textView.isChecked());
 	}
 }
