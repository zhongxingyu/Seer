 package com.slobodastudio.discussions.ui.fragments;
 
 import com.slobodastudio.discussions.ApplicationConstants;
 import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.data.model.Comment;
 import com.slobodastudio.discussions.data.model.SelectedPoint;
 import com.slobodastudio.discussions.data.provider.DiscussionsContract.Comments;
 import com.slobodastudio.discussions.data.provider.DiscussionsContract.Persons;
 import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
 import com.slobodastudio.discussions.ui.ExtraKey;
 import com.slobodastudio.discussions.ui.activities.BaseActivity;
 import com.slobodastudio.discussions.utils.TextViewUtils;
 
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.BaseColumns;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.support.v4.widget.SimpleCursorAdapter;
 import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.LayoutInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockFragment;
 
 public class PointCommentsTabFragment extends SherlockFragment implements OnClickListener,
 		OnItemClickListener {
 
 	private static final boolean DEBUG = true && ApplicationConstants.DEV_MODE;
 	private static final String TAG = PointCommentsTabFragment.class.getSimpleName();
 	private TextView mPointNameTextView;
 	private EditText mCommentEditText;
 	private ListView mCommentsList;
 	private SimpleCursorAdapter mCommentsAdapter;
 	private final PointCursorLoader mPointCursorLoader;
 	private int mLoggedInPersonId;
 	private SelectedPoint mSelectedPoint;
 
 	public PointCommentsTabFragment() {
 
 		// initialize default values
 		mPointCursorLoader = new PointCursorLoader();
 	}
 
 	@Override
 	public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
 			final Bundle savedInstanceState) {
 
 		mCommentsList = (ListView) inflater.inflate(R.layout.tab_fragment_point_comments, container, false);
 		mCommentsList.setOnItemClickListener(this);
 		registerForContextMenu(mCommentsList);
 		addCommentsHeader(inflater);
 		addCommentsFooter(inflater);
 		initFromArguments();
 		return mCommentsList;
 	}
 
 	@Override
 	public void onActivityCreated(final Bundle savedInstanceState) {
 
 		super.onActivityCreated(savedInstanceState);
 		populateSavedInstanceState(savedInstanceState);
 		setUpCommentsAdapter();
 		initCommentsLoader();
 	}
 
 	@Override
 	public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
 
 		super.onCreateContextMenu(menu, v, menuInfo);
 		if (mCommentsAdapter.getCount() > 0) {
 			AdapterContextMenuInfo info = castAdapterContextMenuInfo(menuInfo);
 			Cursor cursor = (Cursor) mCommentsAdapter.getItem(info.position - 1);
 			if (cursor == null) {
 				// For some reason the requested item isn't available, do nothing
 				return;
 			}
 			int textIndex = cursor.getColumnIndexOrThrow(Comments.Columns.TEXT);
 			int personIdIndex = cursor.getColumnIndexOrThrow(Comments.Columns.PERSON_ID);
 			int personId = cursor.getInt(personIdIndex);
 			if (personId == mLoggedInPersonId) {
 				menu.setHeaderTitle(cursor.getString(textIndex)); // if your table name is name
 				android.view.MenuInflater inflater = getActivity().getMenuInflater();
 				inflater.inflate(R.menu.context_comments, menu);
 			}
 		}
 	}
 
 	@Override
 	public boolean onContextItemSelected(final MenuItem item) {
 
 		switch (item.getItemId()) {
 			case R.id.menu_delete:
 				onActionDeleteComment(item);
 				return true;
 			default:
 				return super.onContextItemSelected(item);
 		}
 	}
 
 	@Override
 	public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
 
 		Cursor cursor = (Cursor) mCommentsAdapter.getItem(position - 1);
 		int commentIdIndex = cursor.getColumnIndexOrThrow(Comments.Columns.ID);
 		int commentId = cursor.getInt(commentIdIndex);
 		Uri commentUri = Comments.buildTableUri(commentId);
 		Intent commentIntent = new Intent(Intent.ACTION_VIEW, commentUri);
 		startActivity(commentIntent);
 	}
 
 	@Override
 	public void onClick(final View v) {
 
 		if (v.getId() == R.id.btn_add_comment) {
 			String comment = TextViewUtils.toString(mCommentEditText);
 			if (!TextUtils.isEmpty(comment)) {
 				mCommentEditText.setText("");
 				insertComment(comment);
 			}
 		}
 	}
 
 	@Override
 	public void onSaveInstanceState(final Bundle outState) {
 
 		super.onSaveInstanceState(outState);
 		outState.putString(ExtraKey.COMMENT_TEXT, TextViewUtils.toString(mCommentEditText));
 	}
 
 	private void addCommentsHeader(final LayoutInflater layoutInflater) {
 
 		View headerView = layoutInflater.inflate(R.layout.list_header_point_name, null, false);
 		mPointNameTextView = (TextView) headerView.findViewById(R.id.list_header_point_name);
 		mCommentsList.addHeaderView(headerView, null, false);
 	}
 
 	private void addCommentsFooter(final LayoutInflater layoutInflater) {
 
 		View containerLayout = layoutInflater.inflate(R.layout.layout_comments_footer, null, false);
 		mCommentsList.addFooterView(containerLayout, null, false);
 		mCommentEditText = (EditText) containerLayout.findViewById(R.id.et_point_comment);
 		containerLayout.findViewById(R.id.btn_add_comment).setOnClickListener(this);
 	}
 
 	private void initCommentsLoader() {
 
 		Bundle args = new Bundle();
 		args.putInt(ExtraKey.POINT_ID, mSelectedPoint.getPointId());
 		getLoaderManager().initLoader(PointCursorLoader.COMMENTS_ID, args, mPointCursorLoader);
 		getLoaderManager().initLoader(PointCursorLoader.POINT_NAME_ID, args, mPointCursorLoader);
 	}
 
 	private void initFromArguments() {
 
 		Bundle arguments = getArguments();
 		if (arguments == null) {
 			throw new NullPointerException("You are trying to instantiate fragment without arguments");
 		}
 		if (!arguments.containsKey(ExtraKey.SELECTED_POINT)) {
 			throw new IllegalStateException("fragment was called without selected point extra");
 		}
 		if (!arguments.containsKey(ExtraKey.ORIGIN_PERSON_ID)) {
 			throw new IllegalStateException("fragment was called without logged in person id extra");
 		}
 		mSelectedPoint = arguments.getParcelable(ExtraKey.SELECTED_POINT);
 		mLoggedInPersonId = arguments.getInt(ExtraKey.ORIGIN_PERSON_ID, Integer.MIN_VALUE);
 	}
 
 	private void populateSavedInstanceState(final Bundle savedInstanceState) {
 
 		if (savedInstanceState != null) {
 			String comment = savedInstanceState.getString(ExtraKey.COMMENT_TEXT);
 			mCommentEditText.setText(comment);
 		}
 	}
 
 	private void insertComment(final String comment) {
 
 		Bundle commentValues = new Bundle();
 		commentValues.putString(Comments.Columns.TEXT, comment);
 		commentValues.putInt(Comments.Columns.POINT_ID, mSelectedPoint.getPointId());
 		commentValues.putInt(Comments.Columns.PERSON_ID, mLoggedInPersonId);
 		((BaseActivity) getActivity()).getServiceHelper().insertComment(commentValues, mSelectedPoint);
 	}
 
 	private void onActionDeleteComment(final MenuItem item) {
 
 		AdapterContextMenuInfo info = castAdapterContextMenuInfo(item.getMenuInfo());
 		Cursor cursor = (Cursor) mCommentsAdapter.getItem(info.position - 1);
 		if (cursor == null) {
 			// For some reason the requested item isn't available, do nothing
 			return;
 		}
 		int columnIndex = cursor.getColumnIndexOrThrow(Comments.Columns.ID);
 		int commentId = cursor.getInt(columnIndex);
 		((BaseActivity) getActivity()).getServiceHelper().deleteComment(commentId, mSelectedPoint);
		
 	}
	
	
 	private void setUpCommentsAdapter() {
 
 		mCommentsAdapter = new SimpleCursorAdapter(getActivity(), R.layout.list_item_comments, null,
 				new String[] { Persons.Columns.NAME, Comments.Columns.TEXT, Persons.Columns.COLOR },
 				new int[] { R.id.text_comment_person_name, R.id.text_comment, R.id.image_person_color }, 0);
 		mCommentsAdapter.setViewBinder(new ViewBinder() {
 
 			@Override
 			public boolean setViewValue(final View view, final Cursor cursor, final int columnIndex) {
 
 				switch (view.getId()) {
 					case R.id.image_person_color:
 						ImageView colorView = (ImageView) view;
 						colorView.setBackgroundColor(cursor.getInt(columnIndex));
 						return true;
 					case R.id.text_comment:
 						TextView itemText = (TextView) view;
 						itemText.setText(cursor.getString(columnIndex));
 						return true;
 					case R.id.text_comment_person_name:
 						TextView itemName = (TextView) view;
 						itemName.setText(cursor.getString(columnIndex));
 						return true;
 					default:
 						return false;
 				}
 			}
 		});
 		mCommentsList.setAdapter(mCommentsAdapter);
 	}
 
 	/** Converts an intent into a {@link Bundle} suitable for use as fragment arguments. */
 	public static Bundle intentToFragmentArguments(final Intent intent) {
 
 		Bundle arguments = new Bundle();
 		if (intent == null) {
 			return arguments;
 		}
 		if (!intent.hasExtra(ExtraKey.DISCUSSION_ID)) {
 			throw new IllegalStateException("intent was without discussion id");
 		}
 		if (!intent.hasExtra(ExtraKey.POINT_ID)) {
 			throw new IllegalStateException("intent was without point id");
 		}
 		if (!intent.hasExtra(ExtraKey.PERSON_ID)) {
 			throw new IllegalStateException("intent was without person id");
 		}
 		if (!intent.hasExtra(ExtraKey.TOPIC_ID)) {
 			throw new IllegalStateException("intent was without topic id");
 		}
 		int discussionId = intent.getIntExtra(ExtraKey.DISCUSSION_ID, Integer.MIN_VALUE);
 		int personId = intent.getIntExtra(ExtraKey.PERSON_ID, Integer.MIN_VALUE);
 		int topicId = intent.getIntExtra(ExtraKey.TOPIC_ID, Integer.MIN_VALUE);
 		int pointId = intent.getIntExtra(ExtraKey.POINT_ID, Integer.MIN_VALUE);
 		SelectedPoint point = new SelectedPoint();
 		point.setDiscussionId(discussionId);
 		point.setPersonId(personId);
 		point.setTopicId(topicId);
 		point.setPointId(pointId);
 		arguments.putParcelable(ExtraKey.SELECTED_POINT, point);
 		int loggedInPersonId;
 		if (intent.hasExtra(ExtraKey.ORIGIN_PERSON_ID)) {
 			loggedInPersonId = intent.getIntExtra(ExtraKey.ORIGIN_PERSON_ID, Integer.MIN_VALUE);
 		} else {
 			loggedInPersonId = personId;
 		}
 		arguments.putInt(ExtraKey.ORIGIN_PERSON_ID, loggedInPersonId);
 		return arguments;
 	}
 
 	private static AdapterContextMenuInfo castAdapterContextMenuInfo(final ContextMenuInfo contextMenuInfo) {
 
 		try {
 			// Casts the incoming data object into the type for AdapterView objects.
 			return (AdapterContextMenuInfo) contextMenuInfo;
 		} catch (ClassCastException e) {
 			// If the menu object can't be cast, logs an error.
 			throw new RuntimeException("bad menuInfo: " + contextMenuInfo, e);
 		}
 	}
 
 	private class PointCursorLoader implements LoaderManager.LoaderCallbacks<Cursor> {
 
 		private static final int COMMENTS_ID = 0x00;
 		private static final int POINT_NAME_ID = 0x01;
 
 		@Override
 		public Loader<Cursor> onCreateLoader(final int loaderId, final Bundle arguments) {
 
 			if (!arguments.containsKey(ExtraKey.POINT_ID)) {
 				throw new IllegalArgumentException("Loader was called without point id");
 			}
 			int myPointId = arguments.getInt(ExtraKey.POINT_ID, Integer.MIN_VALUE);
 			if (DEBUG) {
 				Log.d(TAG, "[onCreateLoader] point id: " + myPointId);
 			}
 			switch (loaderId) {
 				case COMMENTS_ID: {
 					String where = Comments.Columns.POINT_ID + "=?";
 					String[] args = new String[] { String.valueOf(myPointId) };
 					return new CursorLoader(getActivity(), Comments.CONTENT_URI, null, where, args, null);
 				}
 				case POINT_NAME_ID: {
 					String where = Points.Columns.ID + "=?";
 					String[] args = new String[] { String.valueOf(myPointId) };
 					String[] projection = new String[] { BaseColumns._ID, Points.Columns.NAME };
 					return new CursorLoader(getActivity(), Points.CONTENT_URI, projection, where, args, null);
 				}
 				default:
 					throw new IllegalArgumentException("Unknown loader id: " + loaderId);
 			}
 		}
 
 		@Override
 		public void onLoaderReset(final Loader<Cursor> loader) {
 
 			switch (loader.getId()) {
 				case COMMENTS_ID:
 					mCommentsAdapter.swapCursor(null);
 					break;
 				case POINT_NAME_ID:
 					mPointNameTextView.setText("");
 					break;
 				default:
 					throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
 			}
 		}
 
 		@Override
 		public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
 
 			if (DEBUG) {
 				Log.d(TAG, "[onLoadFinished] cursor count: " + data.getCount() + ", id: " + loader.getId());
 			}
 			switch (loader.getId()) {
 				case COMMENTS_ID:
 					mCommentsAdapter.swapCursor(data);
 					break;
 				case POINT_NAME_ID:
 					if (data.moveToFirst()) {
 						int nameColumnIndex = data.getColumnIndexOrThrow(Points.Columns.NAME);
 						String name = data.getString(nameColumnIndex);
 						mPointNameTextView.setText(name);
 						getSherlockActivity().getSupportActionBar().setTitle(name);
 					}
 					break;
 				default:
 					throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
 			}
 		}
 	}
 }
