 /*
  * Copyright [2012] [Robert James Szabo]
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
  */
 
 package com.sababado.app;
 
 import java.io.Serializable;
 import java.util.List;
 
 import android.os.Bundle;
 import android.text.Editable;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.EditText;
 import android.widget.Filter;
 import android.widget.Filterable;
 import android.widget.ListAdapter;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 
 import com.actionbarsherlock.app.SherlockListFragment;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.sababado.sherlock.app.R;
 import com.sababado.content.SearchableList;
 import com.sababado.utils.UtilDevice;
 import com.sababado.widget.FilterableBaseAdapter;
 
 /**
  * <p>
  * Class for a {@link com.actionbarsherlock.app.SherlockListFragment SherlockListFragment} which isn't connected to a local
  * database that is search-able by a {@link android.widget.TextView TextView}
  * </p>
  * <p>
  * Extends from {@link com.actionbarsherlock.app.SherlockListFragment SherlockListFragment} and
  * implements {@link com.sababado.content.SearchableList SearchableList}
  * </p>
  * 
  * <h1>How to use with a {@link android.widget.SimpleAdapter SimpleAdapter}</h1>
  * No special actions are required! This also holds true for any {@link android.widget.Adapter Adapter} of {@link android.widget.Filterable Filterable} type.
  * Just make sure to follow the custom layout rules if using a custom layout.
  * 
  * <h1>How to use with a {@link FilterableBaseAdapter}</h1>
  * <h3>Step 1. <i>Create an adapter that extends {@link FilterableBaseAdapter}.</i></h3>
  * <ul>
  * 	<li>The constructor must call the super constructor with an instance of a {@link SearchableList} object
  * 		and the list data to monitor.</li>
  * 	<li>The default implementation of {@link FilterableBaseAdapter#getItem(int) getItem(int)} will return the appropriate item from the list when it is filtered. So use this!</li>
  * 	<li>{@link FilterableBaseAdapter#performFiltering(List, CharSequence) performFiltering(List, CharSequence)} must be implemented in order to sort the data. Filter the given <code>listData</code> by the <code>constraint</code> and return the results as a list.</li>
  * </ul>
  * <h3>Step 2. <i>Set the custom adapter as the list adapter using {@link com.actionbarsherlock.app.SherlockListFragment#setListAdapter(ListAdapter) setListAdapter(ListAdapter)}</i></h3>
  * 
  * <h1>How To Modify (add in / remove from) List Data</h1>
  * <i>There is no support for these actions yet. It is possible to add/remove objects to the unfiltered list however if these actions are performed on the list while it is filtered
  * 		(or the search box is visible) then the list is unable to be acted upon any further (even when unfiltered again).	</i>
  * 
  * <h1>How to use custom layouts</h1>
  * Override {@link com.actionbarsherlock.app.SherlockListFragment#onCreateView(LayoutInflater, ViewGroup, Bundle) onCreateView(LayoutInflater, ViewGroup, Bundle)} to set the new layout (do not call super!).
  * <ul>
  * 	<li>There must be an {@link android.widget.EditText EditText} view with the id search_edittext</li>
  * 	<li>There must be a {@link android.widget.TextView TextView} view with the id android.R.id.empty</li>
  * </ul>
  * 
  * @author Robert J. Szabo
  * @since 01/20/2013
  * @version 1.0
  */
 public class SearchableSherlockListFragment extends SherlockListFragment implements SearchableList
 {
 	/**
 	 * The {@link android.widget.EditText EditText} that a user will use to search and filter the
 	 * list.
 	 */
 	protected EditText mSearchView;
 	/**
 	 * The display string for when the filtered list shows no results.
 	 */
 	protected String mEmptySearchString;
 	/**
 	 * Flag to show all results if the search filter brings up zero results.
 	 * True to show, false to show empty search string.
 	 */
 	protected boolean mShowAllOnEmpty = false;
 	/**
 	 * The display string for when the list is empty and nothing is being
 	 * filtered.
 	 */
 	protected String mEmptyText;
 	/**
 	 * This will exist only if the adapter is a {@link FilterableBaseAdapter}
 	 */
 	private FilterableBaseAdapter mFilterableAdapter;
 	
 	/**
 	 * ID used for the search menu button.
 	 */
 	public static final int MENU_ID_SEARCH=6245736;
 	
 	/**
 	 * Used only in the case that a custom adapter isn't used.
 	 * This is a "cache" to help loading from state changes.
 	 */
 	private List<?> mListData;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
 	{
 		View view = inflater.inflate(R.layout.searchable_list_fragment, container, false);
 		return view;
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState)
 	{
 		super.onActivityCreated(savedInstanceState);
 		
 		//make sure there is an edit text to work with.
 		try
 		{
 			View searchEditText = getView().findViewById(R.id.search_edittext);
 			mSearchView = (EditText) searchEditText;
 		}
 		catch(Exception e)
 		{
 			throw new RuntimeException("The Searchable ListFragment must have an EditText view with id search_edittext");
 		}
 		mSearchView.addTextChangedListener(this);
 		mSearchView.setOnEditorActionListener(new OnEditorActionListener()
 		{
 			@Override
 			public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
 			{
 				UtilDevice.hideKeyboard(getActivity(), v);
 				return true;
 			}
 		});
 
 		// if the empty search string hasn't been set then set it.
 		if (mEmptySearchString == null)
 			setEmptySearchString(getResources().getString(R.string.searchable_list_search_empty));
 		// if the empty list string hasn't been set then set it
 		if (mEmptyText == null)
 			setEmptyText(getResources().getString(R.string.searchable_list_empty_list));
 
 		// if the search text is visible then filter by it.
 		if (mSearchView.getVisibility() == View.VISIBLE)
 			onTextChanged(mSearchView.getText());
 
 		//check to restore state
 		if (savedInstanceState != null)
 		{
 			// get the state of the search box.
 			mSearchView.setText(savedInstanceState.getString("search_text"));
 			mSearchView.setVisibility(savedInstanceState.getInt("search_visibility", View.GONE));
 		}
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState)
 	{
 		super.onSaveInstanceState(outState);
 		//Save the search view's text and visibility
 		outState.putInt("search_visibility", mSearchView.getVisibility());
 		outState.putString("search_text", mSearchView.getText().toString());
 	}
 
 	@Override
 	public void onStart()
 	{
 		super.onStart();
 		
 		//set that this fragment has an options menu
 		setHasOptionsMenu(true);
 	}
 	
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
 	{
 		super.onCreateOptionsMenu(menu, inflater);
 		inflater.inflate(R.menu.search_menu, menu);
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		if(item.getItemId() == R.id.menu_search)
 		{
 			toggleSearchTextViewVisibility(true);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	@Override
 	public void onHiddenChanged(boolean hidden)
 	{
 		super.onHiddenChanged(hidden);
 		//if hidden, hide the keyboard
 		if(hidden && mSearchView != null)
 		{
 			UtilDevice.hideKeyboard(getActivity(), mSearchView);
 		}
 	}
 	
 	/**
 	 * Do not use this!! The effect of setting this to true will result in the search text not getting
 	 * saved when orientation changes. This method has been overridden to always set false.
 	 */
 	@Override
 	public final void setRetainInstance(boolean retain) {
 		super.setRetainInstance(false);
 	}
 
 	@Override
 	public void onTextChanged(CharSequence s)
 	{
 		// Make sure the adapter exists
 		ListAdapter adapter = getListView().getAdapter();
		if (mSearchView.getVisibility() != View.VISIBLE || adapter == null || s == null)
 			return;
 		//if adapter is filterable type then call ontextchanged
 		if(adapter instanceof Filterable)
 		{
 			Filterable fAdapter = (Filterable)adapter;
 			// get the adapter's filter
 			Filter filter = fAdapter.getFilter();
 			// make sure the filter exists before filering.
 			if (filter != null)
 				filter.filter(s);
 		}
 		
 	}
 	
 	@Override
 	public void setListAdapter(ListAdapter adapter)
 	{
 		if(!(adapter instanceof Filterable))
 			throw new RuntimeException("The adapter "+adapter.getClass()+" is not of Filterable type.");
 		if(adapter instanceof FilterableBaseAdapter)
 			mFilterableAdapter = (FilterableBaseAdapter) adapter;
 		super.setListAdapter(adapter);
 	}
 
 	@Override
 	public void onTextChanged(CharSequence s, int start, int before, int count)
 	{
 		onTextChanged(s);
 	}
 
 	@Override
 	public void afterTextChanged(Editable arg0)
 	{
 	}
 
 	@Override
 	public void beforeTextChanged(CharSequence s, int start, int count, int after)
 	{
 	}
 
 	@Override
 	public <T> void setListData(List<T> listData)
 	{
 		try
 		{
 			if (listData.size() > 0 && listData.get(0) != null)
 			{
 				// make sure data is Serializable
 				if (!(listData.get(0) instanceof Serializable))
 					throw new RuntimeException("Exception in Searchable List: List data must be of a Serializable type.");
 			}
 			if(mFilterableAdapter != null)
 			{
 				mFilterableAdapter.setListData(listData);
 			}
 			else //ex:  simple adapter
 			{
 				mListData = (listData);
 			}
 		}
 		catch(NullPointerException e)
 		{
 		}
 	}
 
 	@Override
 	public List<?> getListData()
 	{
 		if(mFilterableAdapter != null)
 			return  mFilterableAdapter.getListData();
 		else if(mListData != null)
 			return mListData;
 		return null;
 	}
 	
 	@Override
 	public List<?> getFilteredListData()
 	{
 		if(mFilterableAdapter == null)
 			throw new RuntimeException("getFilteredListData() can only be used when using a FilterableBaseAdapter");
 		return  mFilterableAdapter.getFilteredListData();
 	}
 
 	@Override
 	public void setSearchText(String text)
 	{
 		mSearchView.setText(text);
 	}
 
 	@Override
 	public Editable getSearchText()
 	{
 		return mSearchView.getText();
 	}
 
 	@Override
 	public void setEmptySearchString(String text)
 	{
 		if (text == null)
 		{
 			mEmptySearchString = "";
 			return;
 		}
 		mEmptySearchString = text;
 	}
 	
 	@Override
 	public String getEmptySearchString()
 	{
 		return mEmptySearchString;
 	}
 
 	@Override
 	public void setEmptyListText(CharSequence text)
 	{
 		if (text == null)
 		{
 			mEmptyText = "";
 		}
 		else
 		{
 			mEmptyText = text.toString();
 		}
 		try
 		{
 			((TextView)getView().findViewById(android.R.id.empty)).setText(mEmptyText);
 		}
 		catch(Exception e)
 		{
 			throw new RuntimeException("The Searchable ListFragment's content view must contain an empty text view with the android.R.id.empty id");
 		}
 		return;
 	}
 	
 	/**
 	 * This calls {@link SearchableList#setEmptyListText(CharSequence) setEmptyListText(CharSequence)}
 	 */
 	@Override
 	public void setEmptyText(CharSequence text)
 	{
 		setEmptyListText(text);
 	}
 	
 	@Override
 	public String getEmptyListText()
 	{
 		return mEmptyText;
 	}
 
 	@Override
 	public boolean isShowAllOnEmpty()
 	{
 		return mShowAllOnEmpty;
 	}
 
 	@Override
 	public void setShowAllOnEmpty(boolean showAllOnEmpty)
 	{
 		mShowAllOnEmpty = showAllOnEmpty;
 	}
 	
 	@Override
 	public boolean toggleSearchTextViewVisibility(boolean clearText)
 	{
 		int visibility = mSearchView.getVisibility();
 		if (visibility == View.GONE)
 		{
 			if (clearText)
 				mSearchView.setText("");
 			mSearchView.setVisibility(View.VISIBLE);
 			mSearchView.requestFocus();
 			UtilDevice.showKeyboard(getActivity(), mSearchView);
 			return true;
 		}
 		else
 		{
 			UtilDevice.hideKeyboard(getActivity(), mSearchView);
 			mSearchView.setVisibility(View.GONE);
 			if (clearText)
 				mSearchView.setText("");
 			return false;
 		}
 	}
 
 	@Override
 	public void forceFilter()
 	{
 		onTextChanged(mSearchView.getText().toString());
 	}
 	
 	@Override
 	public boolean isInSearchMode() {
 		return mSearchView.getVisibility() == View.VISIBLE;
 	}
 }
