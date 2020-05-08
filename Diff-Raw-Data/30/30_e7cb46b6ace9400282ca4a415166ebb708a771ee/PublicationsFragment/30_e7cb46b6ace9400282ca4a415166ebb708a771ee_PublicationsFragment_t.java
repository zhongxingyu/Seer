 package free.solnRss.fragment;
 
 import android.app.ActionBar;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.content.Loader;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.preference.PreferenceManager;
 import android.text.Html;
 import android.text.Spanned;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.LayoutInflater;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import free.solnRss.R;
 import free.solnRss.activity.ReaderActivity;
 import free.solnRss.activity.SolnRss;
 import free.solnRss.adapter.PublicationAdapter;
 import free.solnRss.fragment.listener.PublicationsFragmentListener;
 import free.solnRss.notification.NewPublicationsNotification;
 import free.solnRss.repository.PublicationContentRepository;
 import free.solnRss.repository.PublicationRepository;
 import free.solnRss.repository.PublicationTable;
 import free.solnRss.repository.SyndicationTable;
 
 public class PublicationsFragment extends AbstractFragment implements
 		PublicationsFragmentListener {
 
 	private PublicationRepository publicationRepository;
 	private PublicationContentRepository publicationContentRepository;
 	private Integer selectedSyndicationID;
 	private String dateNewPublicationsFound; // Z date in format 
 	private Integer nextSelectedSyndicationID; // selected by context menu
 	private Integer selectedCategoryID;
 	
 	@Override protected void displayEmptyMessage() {
 		LinearLayout l = (LinearLayout) getActivity().findViewById(emptyLayoutId);
 		l.setVisibility(View.VISIBLE);
 		
 		getActivity().findViewById(R.id.displayAllButton).setVisibility(View.VISIBLE);
 		
 		if (selectedSyndicationID != null) {
 			// Selected by syndication are empty
 			writeEmptyMessage(syndicationName(), R.string.empty_publications_with_syndication,
 					R.string.empty_publications_with_syndication_with_filter, getFilterText());
 
 		}
 		else if (selectedCategoryID != null) {
 			// Selected by category is empty
 			writeEmptyMessage(categoryName(), R.string.empty_publications_with_category,
 					R.string.empty_publications_with_category_with_filter, getFilterText());
 		}
 		
 		else if (dateNewPublicationsFound != null) {
 			// Selected by last date found
 		}
 		
 		else {
 			// All publication
 			writeEmptyMessage(null, R.string.empty_publications,
 					R.string.empty_publications_with_filter, getFilterText());
 			getActivity().findViewById(R.id.displayAllButton).setVisibility(View.GONE);
 		}
 		
 		// If already read publications are hidden display button for display them
 		displayAlreadyReadPublicationsButton();
 	}
 	
 	private void writeEmptyMessage(String name, int idMsgWithoutFilter,
 			int idMsgWithFilter, String filter) {
 		if (TextUtils.isEmpty(filter)) {
 			if (name == null)
 				displayEmptyView(getResources().getString(idMsgWithoutFilter));
 			else
 				displayEmptyView(getResources().getString(idMsgWithoutFilter, name));
 		} else {
 			if (name == null)
 				displayEmptyView(getResources().getString(idMsgWithFilter, filter));
 			else
 				displayEmptyView(getResources().getString(idMsgWithFilter, name,	filter));
 		}
 	}
 	
 	private void displayEmptyView(String msg) {
 		TextView tv = (TextView)getActivity().findViewById(R.id.emptyPublicationsMessage);
 		tv.setText(msg);
 	}
 	
 	private void displayAlreadyReadPublicationsButton() {
 		
 		if (displayAlreadyReadPublications()) {
 			getActivity().findViewById(R.id.displayAlreadyReadButton)
 					.setVisibility(View.INVISIBLE);
 
 		} else {
 			getActivity().findViewById(R.id.displayAlreadyReadButton)
 					.setVisibility(View.VISIBLE);
 		}
 	}
 
 	private String categoryName() {
 		return categoryName(selectedCategoryID);
 	}
 
 	private String syndicationName() {
 		return syndicationName(selectedSyndicationID);
 	}
 	
 	private String dataNewPublicationsFoundLabel() {
 		return getResources().getString(R.string.last_publications);
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup vg, Bundle save) {
 		View fragment = inflater.inflate(R.layout.fragment_publications, vg,
 				false);
 		listContainer = fragment.findViewById(R.id.listContainer);
 		progressContainer = fragment.findViewById(R.id.progressContainer);
 		emptyLayoutId = R.id.emptyPublicationsLayout;
 		listShown = true;
 		
 		return fragment;
 	}
 	
 	@Override
 	public void onViewCreated(View view, Bundle save) {
 		
 	}
 	
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		
 		publicationRepository = new PublicationRepository(getActivity());
 		publicationContentRepository = new PublicationContentRepository(getActivity());
 		restoreOrFirstDisplay(savedInstanceState);
 		
 		registerForContextMenu(getListView());
 		
 		getListView().setTextFilterEnabled(true);
 		
 		((SolnRss)getActivity()).setPublicationsFragmentListener(this);
 		
 		setListShown(false);
 		
 		setHasOptionsMenu(true);
 		
 		// getListView().setOnScrollListener(this);
 		// progressItemView = getActivity().getLayoutInflater().inflate(R.layout.progress_item, null);
 		// testSearch();
 	}
 	
	public void testSearch() {
		// PublicationFinderBusinessImpl finder = new PublicationFinderBusinessImpl(getActivity());
		// finder.searchNewPublications();
 		NewPublicationsNotification notify = new NewPublicationsNotification(getActivity());
 		//DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRENCH);
		notify.notificationForNewPublications(25, "2014-01-23 15:00:00");//sdf.format(new Date()));
	}
 	
 	
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 	}
 
 	@Override
 	public void onListItemClick(final ListView l, final View v, final int position, final long id) {
 		
 		Cursor cursor = ((PublicationAdapter) l.getAdapter()).getCursor();
 		
 		if (position == cursor.getCount()) {
 			return;
 		}
 		
 		String[] publicationContent = publicationContentRepository.retrievePublicationContent(
 				cursor.getInt(cursor.getColumnIndex(PublicationTable.COLUMN_SYNDICATION_ID)), 
 				cursor.getInt(0));
 		
 		String title = cursor.getString(1);         //getPublicationTitle(cursor);
 		String link = publicationContent[0];        //getPublicationUrl(cursor);
 		String description = publicationContent[1]; //hasPublicationContentToDisplay(cursor);
 		
 		clickOnPublicationItem(cursor, l, v, position, id);
 		
 		if (description != null && description.trim().length() > 0) {
 			if (isPreferenceToDisplayOnAppReader()) {
 				displayOnApplicationReader(description, link, title);
 			} else {
 				displayOnSystemBrowser(link);
 			}
 		} else {
 			displayOnSystemBrowser(link);
 		}
 	}
 	
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,	ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		
 		nextSelectedSyndicationID = null;
 		
 		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
 		Cursor c = ((PublicationAdapter) getListAdapter()).getCursor();
 		c.moveToPosition(info.position);
 		
 		menu.setHeaderTitle(c.getString(c
 				.getColumnIndex(SyndicationTable.COLUMN_NAME)));
 
 		nextSelectedSyndicationID = c.getInt(c
 				.getColumnIndex(PublicationTable.COLUMN_SYNDICATION_ID));
 
 		MenuInflater inflater = getActivity().getMenuInflater();
 		inflater.inflate(R.menu.publications_context, menu);
 		
 		if (selectedSyndicationID != null) {
 			menu.getItem(0).setTitle(getResources().getString(R.string.display_all_publication));
 		}
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_see_only:
 			if(selectedSyndicationID == null){
 				reLoadPublicationsBySyndication(nextSelectedSyndicationID);
 				this.moveListViewToTop();
 				
 			} else {
 				reloadPublications();
 				this.moveListViewToTop();
 			}
 			break;
 
 		case R.id.menu_mark_read:
 			confirmMarkSyndicationPublicationsAsRead(nextSelectedSyndicationID);
 			break;
 			
 		default:
 			break;
 		}
 		return super.onContextItemSelected(item);
 	}
 	
 	@Override public void onPause() {
 		Log.e(PublicationsFragment.class.getName(), "ON PAUSE - SAVE INSTANCE STATE IN PUBLICATION FRAGMENT");
 		super.onPause();
 		SharedPreferences.Editor editor = getActivity().getPreferences(0).edit();
 		if (selectedSyndicationID != null) {
 			editor.putInt("selectedSyndicationID", selectedSyndicationID);
 		} else {
 			editor.putInt("selectedSyndicationID", -1);
 		}
 
 		if (selectedCategoryID != null) {
 			editor.putInt("selectedCategoryID", selectedCategoryID);
 		} else {
 			editor.putInt("selectedCategoryID", -1);
 		}
 		
 		if (dateNewPublicationsFound != null) {
 			editor.putString("dateNewPublicationsFound", dateNewPublicationsFound);
 		} else {
 			editor.putString("dateNewPublicationsFound", null);
 		}
 
 		if (!TextUtils.isEmpty(getFilterText())) {
 			editor.putString("filterText", getFilterText());
 		} else {
 			editor.putString("filterText", null);
 		}
 		savePositionOnScreen(editor);
 		editor.commit();
 	}
 	
	/*public void loadLastPublicationRecorded() {		
 		NewPublicationsNotification.NotifyEvent event = 
 				NewPublicationsNotification.NotifyEvent.detachFrom(getActivity().getIntent());
 
 		if (event != null
 				&& event.compareTo(NewPublicationsNotification.NotifyEvent.RESTART_ACTIVITY) == 0) {
 			// Reload list by date
 			String lastUpdateDate = getActivity().getIntent().getStringExtra("newPublicationsRecordDate");
 			
 			if (lastUpdateDate != null) {
 				Bundle bundle = new Bundle();
 				bundle.putString("lastUpdateDate", lastUpdateDate);
 				getLoaderManager().initLoader(0, bundle, this);
 				updateActionBarTitle();
 			}
 		}
	}*/
 	
 	// Bundle saveInstanceState = null;
 	
 	private void restoreOrFirstDisplay(Bundle save) {
 		
 		// this.saveInstanceState = save;
 		
 		SharedPreferences prefs = getActivity().getPreferences(0);
 		
 		NewPublicationsNotification.NotifyEvent event = 
 				NewPublicationsNotification.NotifyEvent.detachFrom(getActivity().getIntent());
 
 		if (event != null
 				&& event.compareTo(NewPublicationsNotification.NotifyEvent.RESTART_ACTIVITY) == 0) {
 			dateNewPublicationsFound = getActivity().getIntent().getStringExtra("dateNewPublicationsFound");
 			
 		} else {
 
 			if (prefs.getInt("selectedSyndicationID", -1) != -1) {
 				selectedSyndicationID = prefs.getInt("selectedSyndicationID", -1);
 			}
 			
 			if (prefs.getInt("selectedCategoryID", -1) != -1) {
 				selectedCategoryID = prefs.getInt("selectedCategoryID", -1);
 			}
 			
 			if (prefs.getString("dateNewPublicationsFound", null) != null) {
 				dateNewPublicationsFound = prefs.getString("dateNewPublicationsFound", null);
 			}
 		}
 		
 		setFilterText(prefs.getString("filterText", null));
 		
 		if (selectedSyndicationID != null) {
 			loadPublicationsBySyndication();
 		} else if (selectedCategoryID != null) {
 			loadPublicationsByCategory();
 		} else if (dateNewPublicationsFound != null) {
 			loadPublicationsByLastFound(dateNewPublicationsFound);
 		} else {
 			loadPublications();
 		}
 	}
 	
 	private void savePositionOnScreen(SharedPreferences.Editor editor) {
 		
 		int index = getListView().getFirstVisiblePosition();
 		editor.putInt("publicationsListViewIndex", index);
 
 		View v = getListView().getChildAt(0);
 		int position = (v == null) ? 0 : v.getTop();
 		editor.putInt("publicationsListViewPosition", position);
 	}
 	
 	@Override
 	protected void setListPositionOnScreen() {
 		
 		SharedPreferences prefs = getActivity().getPreferences(0);
 		int index = prefs.getInt("publicationsListViewIndex", -1);
 		int position = prefs.getInt("publicationsListViewPosition", -1);
 		int cursorCount = prefs.getInt("cursorCount", -1);
 
 		SharedPreferences.Editor editor = prefs.edit();
 		
 		if (cursorCount != -1) {
 			// Get differece between old and new cursor
 			int newCursorCount = 
 				((PublicationAdapter) getListAdapter()).getCursor().getCount() - cursorCount;
 			if (newCursorCount > 0) {
 				index = index + newCursorCount;
 			}
 			
 			getListView().setSelectionFromTop(index, position); 
 			editor.putInt("cursorCount", -1);
 			editor.putInt("publicationsListViewIndex", -1);
 			editor.putInt("publicationsListViewPosition", -1);
 			editor.commit();
 		}
 		else if (index != -1) {
 			// Set list view at position
 			getListView().setSelectionFromTop(index, position);
 			// Reset position save
 			editor.putInt("publicationsListViewIndex", -1);
 			editor.putInt("publicationsListViewPosition", -1);
			editor.commit();;
 		}
 	}
 	
 	@Override
 	protected void initAdapter() {				
 		final String[] from = { 
 				PublicationTable.COLUMN_TITLE,  
 				PublicationTable.COLUMN_TITLE 
 			};
 		final int[] to = { 
 				android.R.id.text1, 
 				android.R.id.text2 
 			};
 		
 		simpleCursorAdapter = new PublicationAdapter(getActivity(),	R.layout.publications, null, from, to, 0);
 		setListAdapter((PublicationAdapter)simpleCursorAdapter);		
 	}
 	
 	@Override
 	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
 		
 		getActivity().setProgressBarIndeterminateVisibility(true);
 		
 		if (bundle != null) {
 			if (bundle.getInt("selectedSyndicationID") != 0) {
 				selectedSyndicationID = bundle.getInt("selectedSyndicationID");				
 			} else if (bundle.getInt("selectedCategoryID") != 0) {
 				selectedCategoryID = bundle.getInt("selectedCategoryID");
 			}
 			else if (bundle.getString("dateNewPublicationsFound") != null) {
 				dateNewPublicationsFound = bundle.getString("dateNewPublicationsFound");
 			}
 		}
 		
 		return publicationRepository.loadPublications(getFilterText(),
 				selectedSyndicationID, selectedCategoryID, dateNewPublicationsFound,
 				displayAlreadyReadPublications());
 	}
 	
 	private boolean displayAlreadyReadPublications() {
 		return PreferenceManager.getDefaultSharedPreferences(getActivity())
 					.getBoolean("pref_display_unread", true);
 	}
 	
 	@Override
 	protected void queryTheTextChange() {
 		Bundle bundle = new Bundle();
 		
 		if (selectedSyndicationID != null) {
 			bundle.putInt("selectedSyndicationID", selectedSyndicationID);
 		}
 		
 		if (selectedCategoryID != null) {
 			bundle.putInt("selectedCategoryID", selectedCategoryID);
 		}
 		
 		if (dateNewPublicationsFound != null) {
 			bundle.putString("dateNewPublicationsFound", dateNewPublicationsFound);
 		}
 		
 		getLoaderManager().restartLoader(0, bundle, this);
 	}
 	
 	/*
 	 * Click on a publication item. It's mark this publication as read and add
 	 * one click to the syndication
 	 */
 	private void clickOnPublicationItem(final Cursor cursor, ListView l, View v,	int position, long id) {
 		
 		new Runnable() {
 			public void run() {
 				try {
 					publicationRepository.markOnePublicationAsReadByUser(
 							cursor.getInt(0), 
 							cursor.getInt(cursor.getColumnIndex(PublicationTable.COLUMN_SYNDICATION_ID)),
 							cursor.getInt(cursor.getColumnIndex(SyndicationTable.COLUMN_NUMBER_CLICK)));
 					((PublicationAdapter)getListAdapter()).notifyDataSetChanged();
 					refreshPublications();
 					((SolnRss) getActivity()).refreshSyndications();
 					
 				} catch (Exception e) {
 					Toast.makeText(getActivity(),
 							"Error unable to set this publication as read",
 							Toast.LENGTH_LONG).show();
 					e.printStackTrace();
 				}
 			}
 		}.run();
 	}
 	
 	public void moveListViewToTop() {
 		getListView().setSelection(0);
 	}
 	
 	public void loadPublications() {
 		getLoaderManager().initLoader(0, null, this);
 	}
 
 	public void reloadPublications() {
 		this.selectedSyndicationID = null;
 		this.selectedCategoryID = null;
 		this.dateNewPublicationsFound = null;
 		if (isAdded()) {
 			getLoaderManager().restartLoader(0, null, this);
 			updateActionBarTitle();
 		}
 	}
 
 	@Override
 	public void reloadPublicationsWithAlreadyRead() {
 		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
 		editor.putBoolean("pref_display_unread", true);
 		editor.commit();
 	}
 	
 	public void refreshPublications() {
 		if (this.selectedSyndicationID != null) {
 			reLoadPublicationsBySyndication(this.selectedSyndicationID);
 		} else if (this.selectedCategoryID != null) {
 			reLoadPublicationsByCategory(this.selectedCategoryID);
 		} else if (this.dateNewPublicationsFound != null) {
 			reLoadPublicationsByLastFound(dateNewPublicationsFound);
 		} 
 		else {
 			reloadPublications();
 		}
 	}
 
 	private void loadPublicationsByCategory() {
 		Bundle bundle = new Bundle();
 		bundle.putInt("selectedCategoryID",selectedCategoryID);
 		getLoaderManager().initLoader(0, bundle, this);
 		updateActionBarTitle();
 	}
 	
 	public void reLoadPublicationsByCategory(Integer categoryID) {
 		this.selectedSyndicationID = null;
 		this.dateNewPublicationsFound = null;
 		this.selectedCategoryID = categoryID;
 		Bundle bundle = new Bundle();
 		bundle.putInt("selectedCategoryID",this.selectedCategoryID);
 		if(isAdded()){
 			getLoaderManager().restartLoader(0, bundle, this);
 			updateActionBarTitle();
 		}
 	}
 
 	private void loadPublicationsBySyndication() {
 		Bundle bundle = new Bundle();
 		bundle.putInt("selectedSyndicationID", selectedSyndicationID);
 		getLoaderManager().initLoader(0, bundle, this);
 		updateActionBarTitle();
 	}
 	
 	public void reLoadPublicationsBySyndication(Integer syndicationID) {
 		this.selectedSyndicationID = syndicationID;
 		this.selectedCategoryID = null;
 		this.dateNewPublicationsFound = null;
 		Bundle bundle = new Bundle();
 		bundle.putInt("selectedSyndicationID", this.selectedSyndicationID);
 		if (isAdded()) {
 			getLoaderManager().restartLoader(0, bundle, this);
 			updateActionBarTitle();
 		}
 	}
 	
 	@Override
 	public void reLoadPublicationsAfterCatgoryDeleted(Integer deletedCategoryId) {
 		if (selectedCategoryID != null
 				&& selectedCategoryID.compareTo(deletedCategoryId) == 0) {
 			selectedCategoryID = null;
 			reloadPublications();
 		}
 	}
 
 	@Override
 	public void reLoadPublicationsAfterSyndicationDeleted(Integer deletedSyndicationId) {
 		if (selectedSyndicationID != null
 				&& selectedSyndicationID.compareTo(deletedSyndicationId) == 0) {
 			selectedSyndicationID = null;
 		}
 		refreshPublications();
 	}
 	
 	@Override
 	public void reLoadPublicationsByLastFound(String dateNewPublicationsFound) {
 		
 		this.selectedSyndicationID = null;
 		this.selectedCategoryID = null;
 		
 		SharedPreferences sharedPreferences = 
 				PreferenceManager.getDefaultSharedPreferences(getActivity());
 		
 		SharedPreferences.Editor editor = sharedPreferences.edit();
 		editor.putInt("newPublicationsRecorded", 0);
 		editor.putString("newPublicationsRecordDate", null);
 		editor.commit();
 
 		Bundle bundle = new Bundle();
 		bundle.putString("dateNewPublicationsFound", dateNewPublicationsFound);
 
 		if(isAdded()){
 			getLoaderManager().restartLoader(0, bundle, this);
 			updateActionBarTitle();
 		}
 	}
 
 	public void loadPublicationsByLastFound(String dateNewPublicationsFound) {
 		
 		this.selectedSyndicationID = null;
 		this.selectedCategoryID = null;
 		
 		SharedPreferences sharedPreferences = 
 				PreferenceManager.getDefaultSharedPreferences(getActivity());
 		
 		SharedPreferences.Editor editor = sharedPreferences.edit();
 		editor.putInt("newPublicationsRecorded", 0);
 		editor.putString("newPublicationsRecordDate", null);
 		editor.commit();
 
 		Bundle bundle = new Bundle();
 		bundle.putString("dateNewPublicationsFound", dateNewPublicationsFound);
 
 		if(isAdded()){
 			getLoaderManager().initLoader(0, bundle, this);
 			updateActionBarTitle();
 		}
 	}
 	
 
 	@Override
 	public void reLoadPublicationsWithLastFound() {
 		
 		SharedPreferences preferences =  getActivity().getPreferences(0);
 		SharedPreferences.Editor editor = preferences.edit();
 
 		int index = getListView().getFirstVisiblePosition();
 		
 		View v = getListView().getChildAt(0);
 		int position = (v == null) ? 0 : v.getTop();
 		
 		editor.putInt("cursorCount", ((PublicationAdapter)getListAdapter()).getCursor().getCount());
 		editor.putInt("publicationsListViewIndex", index);
 		editor.putInt("publicationsListViewPosition", position);
 		editor.commit();
 		
 		refreshPublications();
 	}
 	
 	private void updateActionBarTitle() {
 		String title = null;
 		ActionBar bar = getActivity().getActionBar();
 		
 		if (this.selectedSyndicationID != null) {
 			title = syndicationName();
 			
 		} else if (this.selectedCategoryID != null) {
 			title = categoryName();
 		} 
 		else if (this.dateNewPublicationsFound!= null) {
 			title = dataNewPublicationsFoundLabel();
 		} 
 		
 		if (TextUtils.isEmpty(title)) {
 			bar.setTitle(titleToHtml(getActivity().getTitle().toString()));
 		} else {
 			bar.setTitle(titleToHtml(title));
 		}
 	}
 	
 	private Spanned titleToHtml(String s) {
 		return Html.fromHtml("<b><u>" + s + "</u><b>");
 	}
 	
 	private boolean isPreferenceToDisplayOnAppReader() {
 		return PreferenceManager.getDefaultSharedPreferences(getActivity())
 				.getBoolean("pref_display_publication", true);
 	}
 
 	private void displayOnApplicationReader(String text, String link,String title) {
 		Intent i = new Intent(getActivity(), ReaderActivity.class);
 		
 		i.addFlags(
 				Intent.FLAG_ACTIVITY_CLEAR_TOP | 
 				 Intent.FLAG_ACTIVITY_SINGLE_TOP | 
 			    Intent.FLAG_ACTIVITY_NEW_TASK);
 		
 		i.putExtra("read", text);
 		i.putExtra("link", link);
 		i.putExtra("title", title);
 		startActivity(i);
 	}
 
 	private void displayOnSystemBrowser(String url) {
 		// Start a browser
 		Intent openUrlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
 		try {
 			startActivity(openUrlIntent);
 		} catch (Exception e) {
 			Toast.makeText(getActivity(), getActivity().getResources().getString(
 				R.string.open_browser_bad_url), Toast.LENGTH_LONG).show();
 		}
 	}
 	
 	/**
 	 * After click "mark as read" on context menu, All publication of the selected syndication are marked as read
 	 * @param syndicationId
 	 */
 	private void confirmMarkSyndicationPublicationsAsRead(final Integer syndicationId) {
 		
 		final Resources r = getResources();
 		OnClickListener listener = new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				markSyndicationPublicationsAsRead(syndicationId);
 			}
 		};
 
 		String name = syndicationName(syndicationId);
 		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());	
 		builder.setMessage(r.getString(R.string.confirm_mark_as_read, name)).setTitle(name)
 				.setNegativeButton(r.getString(android.R.string.cancel), null)
 				.setPositiveButton(r.getString(android.R.string.ok), listener)
 				.create().show();
 	}
 	
 	@Override
 	public void markAsRead() {
 				
 		final Resources r = getResources();
 		OnClickListener listener = new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				
 				if (selectedSyndicationID != null) {
 					markSyndicationPublicationsAsRead(selectedSyndicationID);
 					
 				} else if (selectedCategoryID != null) {
 					markCategoryPublicationsAsRead(selectedCategoryID);
 				}
 				else if (dateNewPublicationsFound != null) {
 					marklastPublicationFoundAsRead(dateNewPublicationsFound);
 				}
 				else {
 					markAllPublicationsAsRead();
 				}
 			}
 		};
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 		
 		String who = r.getString(R.string.all);
 		CharSequence title = getActivity().getTitle();
 		if (selectedSyndicationID != null) {
 			who = syndicationName();
 			title = who;
 		} else if (selectedCategoryID != null) {
 			who = categoryName();
 			title = who;
 		}
 		else if (dateNewPublicationsFound != null) {
 			who = dataNewPublicationsFoundLabel();
 			title = who;
 		}
 				
 		builder.setMessage(r.getString(R.string.confirm_mark_as_read, who)).setTitle(title)
 				.setNegativeButton(r.getString(android.R.string.cancel), null)
 				.setPositiveButton(r.getString(android.R.string.ok), listener)
 				.create().show();
 	}
 
 	private void markAllPublicationsAsRead() {
 		publicationRepository.markAllPublicationsAsRead();
 	}
 
 	@Override
 	public void markSyndicationPublicationsAsRead(Integer syndicationId) {
 		publicationRepository.markSyndicationPublicationsAsRead(syndicationId);
 	}
 
 	@Override
 	public void deletePublications(Integer syndicationID) {
 		try {
 			publicationRepository.deletePublications(syndicationID);
 		} catch (Exception e) {
 			Toast.makeText(getActivity(), "Sorry an error occured when trying to delete publications.",
 					Toast.LENGTH_LONG).show();
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	public void markCategoryPublicationsAsRead(Integer categoryId) {
 		publicationRepository.markCategoryPublicationsAsRead(categoryId);
 	}
 	
 	public void marklastPublicationFoundAsRead(String dateNewPublicationsFound) {
 		publicationRepository.marklastPublicationFoundAsRead(dateNewPublicationsFound);
 	}
 
 	@Override
 	public void removeTooOLdPublications() {
 		try {
 			publicationRepository.removeTooOLdPublications();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public Parcelable getListViewInstanceState() {
 		return getListView().onSaveInstanceState();
 	}
 
 }
