 package com.mates120.myword.ui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.mates120.myword.DictionaryManager;
 import com.mates120.myword.R;
 import com.mates120.myword.Value;
 import com.mates120.myword.Word;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnFocusChangeListener;
 import android.view.ViewGroup;
 import android.view.inputmethod.EditorInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 
 /**
  * A fragment representing a list of Items.
  * <p />
  * Large screen devices (such as tablets) are supported by replacing the
  * ListView with a GridView.
  * <p />
  * Activities containing this fragment MUST implement the {@link Callbacks}
  * interface.
  */
 public class SearchFragment extends ListFragment{
 	
 	private DictionaryManager dictionaryManager;
 	private EditText editText;
 	private TextView wordTextView;
 	private View lineView;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		dictionaryManager = new DictionaryManager(this.getActivity());
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View view = inflater.inflate(R.layout.fragment_search_list, container, false);
		
 		editText = (EditText) view.findViewById(R.id.editTextSearch);
 		wordTextView = (TextView) view.findViewById(R.id.resWordTextView);
 		lineView = (View) view.findViewById(R.id.line);
		lineView.setVisibility(View.INVISIBLE);
		wordTextView.setText("");
 		editText.setOnEditorActionListener(new OnEditorActionListener() {
 		    @Override
 		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event){
 		    	ResultArrayAdapter adapter;
 		    	List<Value> values = null;
 		    	Word word = null;
 		        boolean handled = false;
 		        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
 		        	InputMethodManager inputMM = (InputMethodManager) 
 		        			getActivity().getSystemService(
 		        					Context.INPUT_METHOD_SERVICE);
 	                inputMM.hideSoftInputFromWindow(
 	                		editText.getApplicationWindowToken(), 
 	                			InputMethodManager.HIDE_NOT_ALWAYS);
 		            word = dictionaryManager.getWord(editText.getText().toString());
 		            if(word != null){
 		            	wordTextView.setText(word.getSource());
 		            	lineView.setVisibility(View.VISIBLE);
 		            	values = new ArrayList<Value>();
 		            	for(int i = 0; i < word.getValues().size(); i++)
 		            		values.add(word.getValue(i));
 		            	adapter = new ResultArrayAdapter(getActivity(),
 			        			android.R.id.list, values);
 			        	setListAdapter(adapter);
 			        }else{
 			        	wordTextView.setText("No such word in the dictionary.");
 		            }
 		            handled = true;
 		        }
 		        editText.getText().clear();
 		        return handled;
 		    }
 		});
 		editText.setOnFocusChangeListener(new OnFocusChangeListener(){
 
 			@Override
 			public void onFocusChange(View arg0, boolean hasFocus) {
 				InputMethodManager inputMM = (InputMethodManager) 
 						getActivity().getSystemService(
 								Context.INPUT_METHOD_SERVICE);
 				inputMM.hideSoftInputFromWindow(
 							editText.getApplicationWindowToken(), 
                 				InputMethodManager.HIDE_NOT_ALWAYS);
 			}
 			
 		});
 		return view;
	}	
 }
