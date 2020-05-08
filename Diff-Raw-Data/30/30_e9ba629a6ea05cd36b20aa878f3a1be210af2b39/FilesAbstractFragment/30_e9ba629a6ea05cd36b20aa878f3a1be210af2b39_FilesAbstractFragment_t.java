 package com.quanturium.androcloud2.fragments;
 
 import android.app.ActionBar.OnNavigationListener;
 import android.app.LoaderManager.LoaderCallbacks;
 import android.content.Loader;
 import android.database.Cursor;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.util.SparseBooleanArray;
 import android.view.ActionMode;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MenuItem.OnActionExpandListener;
 import android.view.View;
 import android.widget.AbsListView.MultiChoiceModeListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.SearchView;
 import android.widget.SearchView.OnQueryTextListener;
 import android.widget.SpinnerAdapter;
 import android.widget.Toast;
 
 import com.cloudapp.api.model.CloudAppItem;
 import com.cloudapp.api.model.CloudAppItem.Type;
 import com.commonsware.cwac.loaderex.SQLiteCursorLoader;
 import com.quanturium.androcloud2.Constants;
 import com.quanturium.androcloud2.FragmentInitParams;
 import com.quanturium.androcloud2.MyApplication;
 import com.quanturium.androcloud2.R;
 import com.quanturium.androcloud2.adapters.FilesAdapter2;
 import com.quanturium.androcloud2.databases.FilesDatabase;
 import com.quanturium.androcloud2.listeners.FilesTaskListener;
 import com.quanturium.androcloud2.requests.FilesTask;
 import com.quanturium.androcloud2.requests.FilesTaskAnswer;
 import com.quanturium.androcloud2.requests.FilesTaskQuery;
 import com.quanturium.androcloud2.tools.Prefs;
 
 public abstract class FilesAbstractFragment extends AbstractListFragment implements FilesTaskListener, OnItemClickListener, MultiChoiceModeListener, OnQueryTextListener, OnNavigationListener, LoaderCallbacks<Cursor>
 {
 	private FilesAdapter2		adapter							= null;
 	private SpinnerAdapter		dropdownAdapter;
 	protected FilesDatabase		database;
 
 	protected MenuItem			menuItemProgress;
 	protected MenuItem			menuItemRefresh;
 	private boolean				currentlyLoading				= false;
 
 	protected String			filterText						= "";
 	protected CloudAppItem.Type	filterType						= null;
 
 	private int					stateDropdownPosition			= -1;
 
 	protected abstract boolean willDisplayTrash();
 
 	public abstract void onItemClick(int itemId);
 
 	@Override
 	public abstract boolean onActionItemClicked(ActionMode mode, MenuItem item);
 
 	@Override
 	public abstract boolean onCreateActionMode(ActionMode mode, Menu menu);
 
 	@Override
 	protected abstract FragmentInitParams init();
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState)
 	{
 		super.onActivityCreated(savedInstanceState);
 
 		int position = 0;
 
 		if (savedInstanceState != null) // when there is a rotation or configuration change
 		{
 			position = savedInstanceState.getInt(Constants.DROPDOWN_INDEX_KEY);
 		}
 		if (getArguments() != null)
 		{
 			if (getArguments().containsKey(Constants.DROPDOWN_INDEX_KEY))
 				position = getArguments().getInt(Constants.DROPDOWN_INDEX_KEY);
 		}
 		if (stateDropdownPosition != -1)
 		{
 			position = stateDropdownPosition;
 		}
 
 		configureDropDown(position);
 
 		database = FilesDatabase.getInstance(getActivity());
 		adapter = null;
 		configureListview(getListView());
 		setListAdapter(adapter);
 
 		getLoaderManager().initLoader(0, null, this);
 
 		setHasOptionsMenu(true);
 
 		if (savedInstanceState != null) // when there is a rotation or configuration change
 		{
 			FilesTask runningTask = ((MyApplication) getActivity().getApplication()).getFilesTask(); // we get back the task when there is a rotation
 
 			if (runningTask != null)
 			{
 				runningTask.setCallback(this);
 
 				if (runningTask.getStatus() != AsyncTask.Status.FINISHED) // either PENDING or RUNNING
 				{
 					currentlyLoading = true;
 					showProgressIcon(true);
 				}
 			}
 
 		}
 	}
 
 	@Override
 	public void onDestroyView()
 	{
 		SparseBooleanArray checkedPositions = getListView().getCheckedItemPositions();
 		if (checkedPositions != null)
 		{
 			for (int i = 0; i < checkedPositions.size(); i++)
 			{
 				if (checkedPositions.valueAt(i))
 				{
 					getListView().setItemChecked(checkedPositions.keyAt(i), false);
 				}
 			}
 		}
 
 		super.onDestroyView();
 	}
 
 	@Override
 	public void onDetach()
 	{
 		this.mCallbacks = null;
 		super.onDetach();
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState)
 	{
 		outState.putInt(Constants.DROPDOWN_INDEX_KEY, stateDropdownPosition);
 		super.onSaveInstanceState(outState);
 	}
 
 	private void configureDropDown(int position)
 	{
 		this.dropdownAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.filesDropDownFilter, R.layout.row_filesdropdown);
 		getActivity().getActionBar().setListNavigationCallbacks(this.dropdownAdapter, this);
 
 		CloudAppItem.Type filterType = null;
 
 		switch (position)
 		{
 			case 0:
 				filterType = null;
 				break;
 			case 1:
 				filterType = Type.IMAGE;
 				break;
 			case 2:
 				filterType = Type.BOOKMARK;
 				break;
 			case 3:
 				filterType = Type.TEXT;
 				break;
 			case 4:
 				filterType = Type.ARCHIVE;
 				break;
 			case 5:
 				filterType = Type.AUDIO;
 				break;
 			case 6:
 				filterType = Type.VIDEO;
 				break;
 			case 7:
 				filterType = Type.UNKNOWN;
 				break;
 		}
 
 		this.filterType = filterType;
 		getActivity().getActionBar().setSelectedNavigationItem(position);
 	}
 
 	private void configureListview(ListView listView)
 	{
 		listView.setOnItemClickListener(this);
 		listView.setTextFilterEnabled(true);
 		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
 		listView.setMultiChoiceModeListener(this);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		switch (item.getItemId())
 		{
 
 			case R.id.menuItemRefresh:
 
 				loadFiles(0);
 
 				break;
 		}
 
 		return false;
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
 	{
 		inflater.inflate(R.menu.fragment_files, menu);
 
 		this.menuItemProgress = menu.findItem(R.id.menuItemProgress);
 		this.menuItemRefresh = menu.findItem(R.id.menuItemRefresh);
 
 		if (!currentlyLoading)
 			showProgressIcon(false);
 
 		MenuItem menuItemSearch = menu.findItem(R.id.menu_search);
 		SearchView searchView = (SearchView) menuItemSearch.getActionView();
 		searchView.setIconifiedByDefault(true);
 		searchView.setOnQueryTextListener(this);
 		searchView.setSubmitButtonEnabled(false);
 		searchView.setQueryHint("Filter files");
 		searchView.setFocusable(false);
 		searchView.setFocusableInTouchMode(false);
 
 		menuItemSearch.setOnActionExpandListener(new OnActionExpandListener()
 		{
 
 			@Override
 			public boolean onMenuItemActionExpand(MenuItem item)
 			{
 				return true;
 			}
 
 			@Override
 			public boolean onMenuItemActionCollapse(MenuItem item)
 			{
 				SearchView searchView = (SearchView) item.getActionView();
 				searchView.setQuery("", false);
 				return true;
 			}
 		});
 
 		super.onCreateOptionsMenu(menu, inflater);
 	}
 
 	public void showProgressIcon(boolean show)
 	{
 		if (this.menuItemProgress != null && this.menuItemRefresh != null)
 		{
 			if (show)
 			{
 				this.menuItemProgress.setVisible(true);
 				this.menuItemRefresh.setVisible(false);
 			}
 			else
 			{
 				this.menuItemRefresh.setVisible(true);
 				this.menuItemProgress.setVisible(false);
 			}
 		}
 	}
 
 	/**
 	 * @param reload
 	 *            : When reload=true, all items in the listview are removed ; when reload=false, only the new items will be added
 	 * @param page
 	 *            : if 0, items will be added on top, else on the bottom
 	 */
 	private void loadFiles(final int page)
 	{
 		if (!currentlyLoading)
 		{
 			currentlyLoading = true;
 			showProgressIcon(true);
 
 			if (page == 0)
 			{
 				((FilesAdapter2) getListAdapter()).setLastItemStatus(FilesAdapter2.Status.DISABLED);
 				((FilesAdapter2) getListAdapter()).notifyDataSetChanged();
 			}
 			else
 			{
 				((FilesAdapter2) getListAdapter()).setLastItemStatus(FilesAdapter2.Status.LOADING);
 				((FilesAdapter2) getListAdapter()).notifyDataSetChanged();
 			}
 
 			FilesTask filesTask = new FilesTask(this);
 			FilesTaskQuery query = new FilesTaskQuery(Prefs.getPreferences(getActivity()).getString(Prefs.EMAIL, ""), Prefs.getPreferences(getActivity()).getString(Prefs.PASSWORD, ""), database, page, Integer.valueOf(Prefs.getPreferences(getActivity()).getString(Prefs.FILES_PER_REQUEST, "20")), willDisplayTrash());
 			filesTask.execute(query);
 			((MyApplication) getActivity().getApplication()).setFilesTask(filesTask);
 		}
 		else
 		{
 			Toast.makeText(getActivity(), "Wait ...", Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
 	{
 		if (getListAdapter().getItemViewType(arg2) == FilesAdapter2.TYPE_LOAD_MORE)
 		{
 			int page = (int) (database.getCount() / Integer.valueOf(Prefs.getPreferences(getActivity()).getString(Prefs.FILES_PER_REQUEST, "20"))) + 1; // TODO fixe le pb de (70/20)+1
 			loadFiles(page);
 		}
 		else
 		{
 			Cursor cursor = (Cursor) getListAdapter().getItem(arg2);
 			int id = cursor.getInt(cursor.getColumnIndex(FilesDatabase.COL_ID));
 
 			if (id >= 0) // ok
 				onItemClick(id);
 		}
 	}
 
 	@Override
 	public void onTaskFinished(FilesTaskAnswer answer)
 	{
 		Log.i(TAG, "onTaskFinished");
 
 		if (answer.resultCode == FilesTaskAnswer.RESULT_OK)
 		{
 			((FilesAdapter2) getListAdapter()).setLastItemStatus(FilesAdapter2.Status.NORMAL);
 			// ((FilesAdapter2) getListAdapter()).changeCursor(database.fetchFilesFiltered(((FilesAdapter2) getListAdapter()).getTypeFilter(), null));
 			getLoaderManager().restartLoader(0, null, this);
 		}
 		else
 		{
 			Toast.makeText(getActivity(), "Request failed. Check your connection and your credentials", Toast.LENGTH_LONG).show();
 		}
 
 		currentlyLoading = false;
 		showProgressIcon(false);
 		((MyApplication) getActivity().getApplication()).setFilesTask(null);
 	}
 
 	@Override
 	public void onTaskCanceled()
 	{
 		currentlyLoading = false;
 		showProgressIcon(false);
 		Log.e(TAG, "Task canceled");
 		((MyApplication) getActivity().getApplication()).setFilesTask(null);
 	}
 
 	@Override
 	public void onDestroyActionMode(ActionMode mode)
 	{
 
 	}
 
 	@Override
 	public boolean onPrepareActionMode(ActionMode mode, Menu menu)
 	{
 		return false;
 	}
 
 	@Override
 	public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked)
 	{
 		if (position == getListAdapter().getCount() - 1 && checked) // if it is the last item = if it is "load more"
 			getListView().setItemChecked(position, false);
 
 		int checkedCount = getListView().getCheckedItemCount();
 
 		mode.setSubtitle("" + checkedCount + " item(s) selected");
 	}
 
 	@Override
 	public boolean onQueryTextChange(String newText)
 	{
 		if (!filterText.equals(newText))
 		{
 			this.filterText = newText;
 			getLoaderManager().restartLoader(0, null, this);
 		}
 
 		return true;
 	}
 
 	@Override
 	public boolean onQueryTextSubmit(String query)
 	{
 		return false;
 	}
 
 	@Override
 	public boolean onNavigationItemSelected(int itemPosition, long itemId)
 	{
 		stateDropdownPosition = itemPosition;
 		CloudAppItem.Type filterType = null;
 
 		switch (itemPosition)
 		{
 			case 0:
 				filterType = null;
 				break;
 			case 1:
 				filterType = Type.IMAGE;
 				break;
 			case 2:
 				filterType = Type.BOOKMARK;
 				break;
 			case 3:
 				filterType = Type.TEXT;
 				break;
 			case 4:
 				filterType = Type.ARCHIVE;
 				break;
 			case 5:
 				filterType = Type.AUDIO;
 				break;
 			case 6:
 				filterType = Type.VIDEO;
 				break;
 			case 7:
 				filterType = Type.UNKNOWN;
 				break;
 		}
 
 		this.filterType = filterType;
 		getLoaderManager().restartLoader(0, null, this);
 		return true;
 	}
 
 	@Override
 	public Loader<Cursor> onCreateLoader(int id, Bundle args)
 	{
 		String query = FilesDatabase.getQuery(filterType, filterText, willDisplayTrash());
 		return new SQLiteCursorLoader(getActivity(), database, query, null);
 	}
 
 	@Override
 	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1)
 	{
 		if (adapter == null)
 		{
 			adapter = new FilesAdapter2(getActivity(), null, 0);
 			setListAdapter(adapter);
 		}
 		else
 		{
 			adapter.swapCursor(arg1);
 		}
 	}
 
 	@Override
 	public void onLoaderReset(Loader<Cursor> arg0)
 	{
 		if (adapter != null)
 			adapter.swapCursor(null);
 	}
 }
