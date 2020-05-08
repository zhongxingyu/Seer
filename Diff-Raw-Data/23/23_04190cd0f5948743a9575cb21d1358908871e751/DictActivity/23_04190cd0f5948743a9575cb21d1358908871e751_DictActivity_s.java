 package com.ngapham.englishlearningkit;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.ngapham.englishlearningkit.R;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class DictActivity extends Activity {
 	
 	private AutoCompleteTextView txtSearch;
	private List<Word> listWord;		//Danh sach tu vung lay tu CSDL tu dien
 	private List<String> listWordString;	//Danh sach tu dang string
 	private List<String> listSuggestion;	//Danh sach suggestion cho txtSearch
	private List<String> listSelection;
 	private String selectionWord;
 	private WordDatabase myDbAdapter;
 	private ListView list;
 	private ArrayAdapter<String> adapter;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.search_layout);
 		//txtSelection = (TextView) findViewById(R.id.txtSelection);
 		txtSearch = (AutoCompleteTextView) findViewById(R.id.txtSearch);
 		list = (ListView) findViewById(R.id.listWord);
 		myDbAdapter = new WordDatabase(this);
 		
 		listWordString = new ArrayList<String>();
 		listSuggestion = new ArrayList<String>();
 		listSelection = new ArrayList<String>();
 		
 		listWord = myDbAdapter.getAllWord();
 		for (Word word : listWord)
 		{
 			listWordString.add(word.toString());
 			listSuggestion.add(word.getName());
 		}
		
		bindData();
 		
 		txtSearch.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position,
 					long rowID) {
 				selectionWord = (String) parent.getItemAtPosition(position);
 				Log.i("Selection word ------>", selectionWord);
 				
 				listSelection.add(myDbAdapter.getWord(selectionWord.toLowerCase().trim()).toString());
 				if (listSelection != null) {
 					bindDataSelection();
 				}
 			}
			
 		});
 		
 	}
 	
 	//Thiet lap ArrayAdapter de bind data
 	public void bindData()
 	{
 		adapter = new ArrayAdapter<String> (this, android.R.layout.simple_list_item_1, listWordString);
 		list.setAdapter(adapter);
 		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listSuggestion);
 		txtSearch.setAdapter(adapter);
 	}
 	
 	public void bindDataSelection()
 	{
 		adapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_list_item_1, listSelection);
 		list.setAdapter(adapter);
 	}
 	
 }
