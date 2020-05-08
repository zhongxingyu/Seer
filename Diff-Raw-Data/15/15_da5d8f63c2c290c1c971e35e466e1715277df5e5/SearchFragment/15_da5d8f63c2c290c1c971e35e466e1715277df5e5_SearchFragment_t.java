 package com.mates120.myword.ui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.mates120.myword.AvailableDictionaries;
 import com.mates120.myword.R;
 import com.mates120.myword.Word;
 
 import android.support.v4.app.Fragment;
 import android.content.Context;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnFocusChangeListener;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.view.inputmethod.EditorInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.webkit.WebView;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 
 
 public class SearchFragment extends Fragment {
 	
 	private AvailableDictionaries availableDictionaries;
 	
 	private EditText editText;
 	private String editContent;
 	private static final String editContentKey = "edit_content";
 	
 	private Button clearButton;
 	private WebView resultWebView;
 	private String mime = "text/html";
 	private String encoding = "utf-8";
 	private ListView hintsList;
 	private LinearLayout searchLayout;
 	private LinearLayout webHorizLayout;
 	private boolean hintsShown = true;
	private boolean webShown = false;
 	private HtmlPageComposer htmlPageComposer;
 	
 	private String text;
 	private List<String> hints;
 	private static final String hintsContentKey = "hints_content";
 	private ArrayAdapter<String> hintsAdapter;
 	
 	private String webViewContent;
 	private static final String webViewContentKey = "web_view_content";
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		availableDictionaries = AvailableDictionaries.getInstance(this.getActivity());
 		htmlPageComposer = new HtmlPageComposer(this.getActivity());
 		hintsAdapter = new ArrayAdapter<String>(getActivity(), 
 				android.R.layout.simple_list_item_1);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState)
 	{
 		View view = inflater.inflate(R.layout.fragment_search_list, container, false);
 		searchLayout = (LinearLayout)view.findViewById(R.id.Search);
 		webHorizLayout = (LinearLayout)view.findViewById(R.id.webViewLinearLayout);
 		editText = (EditText) view.findViewById(R.id.editTextSearch);
 		clearButton = (Button) view.findViewById(R.id.clearTextButton);
 		clearButton.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View v) {
 				editText.getText().clear();
 			}
 		});
 		resultWebView = new WebView(getActivity());		
 		resultWebView.setLayoutParams(new ViewGroup.LayoutParams(
 				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
 		hintsList = new ListView(getActivity());		
 		hintsList.setOnItemClickListener(new HintSelectListener());
 		editText.setOnEditorActionListener(new EditorActionListener());
 		editText.setOnFocusChangeListener(new SearchLineFocusChangeListener());
 		editText.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				showHintsList();
 			}
 		});
 		editText.addTextChangedListener(new EditTextWatcher());
 		restoreWebViewState(savedInstanceState);
 		restoreHintsState(savedInstanceState);
 		tryIfLandspace();
 		return view;
 	}
 	
 	class SearchLineFocusChangeListener implements OnFocusChangeListener{
 		@Override
 		public void onFocusChange(View arg0, boolean hasFocus)
 		{
 			hideKeyboard();
//			if(hasFocus && !webShown)
//				showHintsList();
 		}
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState)
 	{
 		outState.putString(webViewContentKey, webViewContent);
 		outState.putString(editContentKey, editText.getText().toString());
 		outState.putStringArrayList(hintsContentKey, (ArrayList<String>) hints);
 	};
 
 	private void restoreWebViewState(Bundle savedInstanceState)
 	{
 		if (savedInstanceState == null)
 			return;
 		webViewContent = savedInstanceState.getString(webViewContentKey);		
 		if (webViewContent == null)
 			return;		
 		loadWebViewContent();
 		tryIfLandspace();
 	}
 
 	private void restoreHintsState(Bundle savedInstanceState)
 	{
 		if (savedInstanceState == null)
 			return;
 		editContent = savedInstanceState.getString(editContentKey);
 		hints = savedInstanceState.getStringArrayList(hintsContentKey);
 		if (hints == null)
 			return;
 		updateHintsAdapterFromHintsList();
 	}
 	
 	private void showHintsList()
 	{
 		searchLayout.removeView(resultWebView);
 		if(hintsList.getParent() == null)
 			searchLayout.addView(hintsList);
 		hintsShown = true;
 	}
 	
 	private void showResultsView()
 	{
 		if (hintsShown == false)
 			return;
 		if(resultWebView.getParent() != null)
 			((LinearLayout)resultWebView.getParent()).removeView(resultWebView);
 		if(webHorizLayout != null){
 			webHorizLayout.addView(resultWebView);
 			hintsShown = true;
 		}else{
 			searchLayout.removeView(hintsList);
 			searchLayout.addView(resultWebView);
 			hintsShown = false;
 		}
		webShown = true;
 	}
 	
 	private void findAndShowWordDefenition(CharSequence word)
 	{
         List<Word> words = null;
         words = availableDictionaries.getWord(word.toString());
         webViewContent = (words.isEmpty())?
         			generateWordNotFoundHtml():convertValuesToHtml(words);
         loadWebViewContent();
         showResultsView();
 	}
 	
 	private void loadWebViewContent()
 	{
 		resultWebView.loadDataWithBaseURL("file:///android_asset/", webViewContent, mime, encoding, null);
 	}
 
 	private String generateWordNotFoundHtml()
 	{
 		return htmlPageComposer.makeNotFoundPage();
 	}
 	
 	private String convertValuesToHtml(List<Word> words)
 	{
 		return htmlPageComposer.makePage(words);
 	}
 	
 	class EditorActionListener implements OnEditorActionListener{
 
 		@Override
 		public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
 		{	    	
 	        boolean handled = false;
 	        if (actionId != EditorInfo.IME_ACTION_SEARCH)
 	        	return handled;	        
         	hideKeyboard();
             findAndShowWordDefenition(editText.getText());
             handled = true;
 	        return handled;
 	    }		
 	}
 	
 	private void updateHintsAdapterFromHintsList()
 	{
 		hintsAdapter.clear();
 		hintsAdapter.addAll(hints);
 		hintsList.setAdapter(hintsAdapter);
 	}
 	
 	private void hideKeyboard()
 	{
 		InputMethodManager inputMM = (InputMethodManager) 
     			getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
         inputMM.hideSoftInputFromWindow(
         		editText.getApplicationWindowToken(), 
         			InputMethodManager.HIDE_NOT_ALWAYS);
 	}
 	
 	class EditTextWatcher implements TextWatcher
 	{
 		@Override
 		public void afterTextChanged(Editable s)
 		{
 			text = editText.getText().toString();
 			if (text.length() != 0)
 			{
 				if (text.equals(editContent)) //hints were updated from bundle
 				{
 					editContent = null;
 					return;
 				}
 				GetHintsAsyncTask hintsAsTask = new GetHintsAsyncTask();
 				hintsAsTask.execute();
 				showHintsList();
 			}
 		}
 
 		@Override
 		public void beforeTextChanged(CharSequence s, int start, int count,
 				int after) {
 		}
 
 		@Override
 		public void onTextChanged(CharSequence s, int start, int before,
 				int count){		
 		}		
 	}
 	
 	class GetHintsAsyncTask extends AsyncTask<Void, 
 	Integer, Void>{
 
 		@Override
 		protected void onPostExecute(Void result) {
 			updateHintsAdapterFromHintsList();
 			cancel(true);
 			super.onPostExecute(result);
 		}
 
 		@Override
 		protected Void doInBackground(Void... params) {
 			hints = availableDictionaries.getHints(text);
 			return null;
 		}		
 	}
 
 	class HintSelectListener implements OnItemClickListener{
 
 		@Override
 		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 				long arg3) {
 			hideKeyboard();
 			TextView textItem = (TextView) arg1;
 			findAndShowWordDefenition(textItem.getText());			
 		}
 	}
 	
 	private void tryIfLandspace(){
		if (webHorizLayout == null){
			if(webShown)
				showResultsView();
 			return;
		}
 		if(hintsList.getParent() != null)
 			((LinearLayout)hintsList.getParent()).removeView(hintsList);
 		if(resultWebView.getParent() != null)
 			((LinearLayout)resultWebView.getParent()).removeView(resultWebView);
 		if(hintsList != null)
 			searchLayout.addView(hintsList);
 		if(resultWebView != null)
 			webHorizLayout.addView(resultWebView);
 		hintsShown = true;
 	}
 
 	public boolean shouldCloseOnBack() {
 		if (hintsShown)
 			return false;
 		showHintsList();
 		return true;
 	}
 }
