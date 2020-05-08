 package com.mili.xiaominglui.app.vello.ui;
 
 import it.gmariotti.cardslib.library.internal.Card;
 import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
 import it.gmariotti.cardslib.library.view.CardListView;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.TimeZone;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.DialogInterface;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnLongClickListener;
 import android.view.ViewGroup;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.ActionMode;
 import com.actionbarsherlock.view.ActionMode.Callback;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.atermenji.android.iconictextview.IconicTextView;
 import com.atermenji.android.iconictextview.icon.FontAwesomeIcon;
 import com.mili.xiaominglui.app.vello.R;
 import com.mili.xiaominglui.app.vello.config.VelloConfig;
 import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbDictCard;
 import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbWordCard;
 import com.mili.xiaominglui.app.vello.data.provider.util.ProviderCriteria;
 
 public class ReviewViewFragment extends SherlockFragment implements
 		LoaderManager.LoaderCallbacks<Cursor>, OnLongClickListener, Callback,
 		DialogInterface.OnClickListener {
 	private static final String TAG = ReviewViewFragment.class.getSimpleName();
 
 	private CardListView mCardList;
 	private ArrayList<Card> mCards;
 	private CardArrayAdapter mCardArrayAdapter;
 
 	private onStatusChangedListener mListener;
 
 	private String mCurFilter = "";
 	private boolean mIsSearching = false;
 
 	public static Fragment newInstance() {
 		Fragment f = new ReviewViewFragment();
 		return f;
 	}
 
 	public interface onStatusChangedListener {
 		public void onModeChanged(int modeColor);
 
 		public void syncOnAllRecalled();
 
 		public void onWordRecalled();
 	}
 
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 		try {
 			mListener = (onStatusChangedListener) activity;
 		} catch (ClassCastException e) {
 			throw new ClassCastException(activity.toString()
 					+ " must implement onStatusChangedListener");
 		}
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setRetainInstance(true);
 		setHasOptionsMenu(true);
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		mCards = new ArrayList<Card>();
 		return inflater.inflate(R.layout.fragment_review, container, false);
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		getLoaderManager().initLoader(0, null, this);
 	}
 
 	@Override
 	public void onDestroyView() {
 		super.onDestroyView();
 	}
 
 	@SuppressLint("SimpleDateFormat")
 	@Override
 	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
 		// show all open WordCards whose due time bigger than mobile local now
 		// time && syncInNext mark not set
 		ProviderCriteria criteria = new ProviderCriteria();
 		if (TextUtils.isEmpty(mCurFilter)) {
 			// Review Mode
 			mIsSearching = false;
 			mListener.onModeChanged(VelloConfig.REVIEW_MODE_ACTION_BAR_COLOR);
 			criteria.addSortOrder(DbWordCard.Columns.DUE, true);
 			Calendar rightNow = Calendar.getInstance();
 
 			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
 			long rightNowUnixTime = rightNow.getTimeInMillis();
 			long rightNowUnixTimeGMT = rightNowUnixTime - TimeZone.getDefault().getRawOffset();
 			String now = format.format(new Date(rightNowUnixTimeGMT));
 			criteria.addEq(DbWordCard.Columns.MARKDELETED, "false");
 			criteria.addLt(DbWordCard.Columns.DUE, now, true);
 			criteria.addNe(DbWordCard.Columns.CLOSED, "true");
 			return new CursorLoader(getActivity(), DbWordCard.CONTENT_URI,
 					DbWordCard.PROJECTION, criteria.getWhereClause(),
 					criteria.getWhereParams(), criteria.getOrderClause());
 		} else {
 			// Dictionary Mode
 			mIsSearching = true;
 			mListener.onModeChanged(VelloConfig.DICTIONARY_MODE_ACTION_BAR_COLOR);
 			criteria.addLike(DbDictCard.Columns.KEYWORD, mCurFilter + "%");
 			return new CursorLoader(getActivity(), DbDictCard.CONTENT_URI,
 					DbDictCard.PROJECTION, criteria.getWhereClause(),
 					criteria.getWhereParams(), criteria.getOrderClause());
 		}
 	}
 
 	@Override
 	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
 		mCards.clear();
 		mCardList = (CardListView) getActivity().findViewById(R.id.card_list);
 		if (mIsSearching) {
 			// in Dictionary Mode
 			if (data == null)
 				return;
 			for (data.moveToFirst(); !data.isAfterLast(); data.moveToNext()) {
 				DictCard dc = new DictCard(getActivity()
 						.getApplicationContext(), data);
 				dc.setSwipeable(false);
 
 				mCardArrayAdapter.add(dc);
 			}
 		} else {
 			// in Review Mode
 			for (data.moveToFirst(); !data.isAfterLast(); data.moveToNext()) {
 				ReviewCard rc = new ReviewCard(getActivity().getApplicationContext(), data);
 				rc.setId(rc.trelloCard.id);
 				rc.init();
 				mCards.add(rc);
 				mCardArrayAdapter = new CardArrayAdapter(getActivity(), mCards);
 				// Enable undo controller!
 				mCardArrayAdapter.setEnableUndo(true);
 
 				if (mCardList != null) {
 					mCardList.setAdapter(mCardArrayAdapter);
 				}
 			}
 		}
 
 		View empty = getActivity().findViewById(R.id.emptyView);
		empty.setOnTouchListener(new ViewGroup.OnTouchListener() {
 
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				switch (event.getAction()) {
 				case MotionEvent.ACTION_DOWN:
					return true;
				case MotionEvent.ACTION_MOVE:
					return true;
 				case MotionEvent.ACTION_UP:
 					mListener.syncOnAllRecalled();
 					return true;
 				default:
 					return false;
 				}
 			}
 		});
 		IconicTextView iconicTextView = (IconicTextView) getActivity().findViewById(R.id.iconic_all_recalled);
 		iconicTextView.setIcon(FontAwesomeIcon.OK);
 		iconicTextView.setTextColor(Color.GRAY);
 		mCardList.setEmptyView(empty);
 	}
 
 	@Override
 	public void onLoaderReset(Loader<Cursor> loader) {
 		if (VelloConfig.DEBUG_SWITCH) {
 			Log.d(TAG, "onLoaderReset");
 		}
 	}
 
 	@SuppressLint("SimpleDateFormat")
 	private void asyncMarkDeleteWordRemotely(final Integer[] wordIds) {
 		final AsyncTask<Integer, Void, Void> deleteTask = new AsyncTask<Integer, Void, Void>() {
 			@Override
 			protected Void doInBackground(Integer... ids) {
 				SimpleDateFormat format = new SimpleDateFormat(
 						"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
 				for (final int id : ids) {
 					Calendar rightNow = Calendar.getInstance();
 					long rightNowUnixTime = rightNow.getTimeInMillis();
 					Date rightNowDate = new Date(rightNowUnixTime);
 					String stringRightNow = format.format(rightNowDate);
 					ContentValues cv = new ContentValues();
 					cv.put(DbWordCard.Columns.MARKDELETED.getName(), "true");
 					cv.put(DbWordCard.Columns.DATE_LAST_OPERATION.getName(),
 							stringRightNow);
 					Uri uri = ContentUris.withAppendedId(
 							DbWordCard.CONTENT_URI, id);
 					getActivity().getContentResolver().update(uri, cv, null,
 							null);
 				}
 				return null;
 			}
 		};
 		deleteTask.execute(wordIds);
 	}
 
 	void onQueryTextChange(String newText) {
 		mCurFilter = !TextUtils.isEmpty(newText) ? newText : null;
 		getLoaderManager().restartLoader(0, null, this);
 	}
 
 	/**
 	 * Create a compatible helper that will manipulate the action bar if
 	 * available.
 	 */
 	private ActionBarHelper createActionBarHelper() {
 		return new ActionBarHelper();
 	}
 
 	private class ActionBarHelper {
 		private final ActionBar mActionBar;
 
 		private ActionBarHelper() {
 			mActionBar = ((SherlockFragmentActivity) getActivity())
 					.getSupportActionBar();
 		}
 
 		public void init() {
 			mActionBar.setDisplayHomeAsUpEnabled(false);
 			mActionBar.setHomeButtonEnabled(false);
 		}
 
 	}
 
 	@Override
 	public void onClick(DialogInterface arg0, int arg1) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void onDestroyActionMode(ActionMode mode) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public boolean onLongClick(View arg0) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 }
