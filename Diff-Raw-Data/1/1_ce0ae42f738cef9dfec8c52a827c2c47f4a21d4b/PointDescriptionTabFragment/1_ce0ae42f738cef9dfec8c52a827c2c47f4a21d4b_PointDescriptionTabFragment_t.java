 package com.slobodastudio.discussions.ui.fragments;
 
 import com.slobodastudio.discussions.ApplicationConstants;
 import com.slobodastudio.discussions.R;
 import com.slobodastudio.discussions.data.model.Description;
 import com.slobodastudio.discussions.data.model.Point;
 import com.slobodastudio.discussions.data.model.SelectedPoint;
 import com.slobodastudio.discussions.data.provider.DiscussionsContract.Descriptions;
 import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
 import com.slobodastudio.discussions.ui.ExtraKey;
 import com.slobodastudio.discussions.ui.IntentAction;
 import com.slobodastudio.discussions.ui.activities.BaseActivity;
 
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.RelativeLayout;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockFragment;
 
 public class PointDescriptionTabFragment extends SherlockFragment {
 
 	public static final int INVALID_POINT_ID = Integer.MIN_VALUE;
 	private static final boolean DEBUG = true && ApplicationConstants.DEV_MODE;
 	private static final String TAG = PointDescriptionTabFragment.class.getSimpleName();
 	private Cursor mDescriptionCursor;
 	private EditText mDescriptionEditText;
 	private int mDescriptionId;
 	private int mDiscussionId;
 	private boolean mIsEmpty;
 	private EditText mNameEditText;
 	private int mOrderNum;
 	private int mPersonId;
 	private Cursor mPointCursor;
 	private final PointCursorLoader mPointCursorLoader;
 	private int mPointId;
 	private CheckBox mSharedToPublicCheckBox;
 	private Spinner mSideCodeSpinner;
 	private int mTopicId;
 
 	public PointDescriptionTabFragment() {
 
 		// initialize default values
 		mPointId = INVALID_POINT_ID;
 		mIsEmpty = false;
 		mDescriptionId = Integer.MIN_VALUE;
 		mPointCursorLoader = new PointCursorLoader();
 	}
 
 	/** Converts an intent into a {@link Bundle} suitable for use as fragment arguments. */
 	public static Bundle intentToFragmentArguments(final Intent intent) {
 
 		Bundle arguments = new Bundle();
 		if (intent == null) {
 			return arguments;
 		}
 		final String action = intent.getAction();
 		if (action != null) {
 			arguments.putString(ExtraKey.ACTION, action);
 		}
 		final Bundle extras = intent.getExtras();
 		if (extras != null) {
 			arguments.putAll(intent.getExtras());
 		}
 		return arguments;
 	}
 
 	private static FragmentState getCurrentState(final Bundle fragmentArguments) {
 
 		if (fragmentArguments == null) {
 			return FragmentState.EMPTY;
 		}
 		if (!fragmentArguments.containsKey(ExtraKey.ACTION)) {
 			throw new IllegalArgumentException("Fragment arguments doesnt contain action string");
 		}
 		String action = fragmentArguments.getString(ExtraKey.ACTION);
 		if (Intent.ACTION_EDIT.equals(action)) {
 			return FragmentState.EDIT;
 		} else if (Intent.ACTION_VIEW.equals(action)) {
 			return FragmentState.VIEW;
 		} else if (IntentAction.NEW.equals(action)) {
 			return FragmentState.NEW;
 		} else {
 			throw new IllegalArgumentException("Unknown action: " + action);
 		}
 	}
 
 	public String getCurrentPointName() {
 
 		return mNameEditText.getText().toString();
 	}
 
 	public int getPointId() {
 
 		return mPointId;
 	}
 
 	public boolean isEmpty() {
 
 		return mIsEmpty;
 	}
 
 	public void onActionDelete() {
 
 		String action = getArguments().getString(ExtraKey.ACTION);
 		if (!Intent.ACTION_EDIT.equals(action)) {
 			throw new IllegalArgumentException("[onActionDelete] was called with incorrect action: " + action);
 		}
 		if (mPointId == INVALID_POINT_ID) {
 			throw new IllegalArgumentException("[onActionDelete] was called with incorrect point id: "
 					+ mPointId);
 		}
 		((BaseActivity) getActivity()).getServiceHelper().deletePoint(getSelectedPoint());
 	}
 
 	public void onActionSave() {
 
 		onActionSave(mTopicId);
 	}
 
 	public void onActionSave(final int topicId) {
 
 		// description is first because notify server by point change
 		if (mDescriptionId != Integer.MIN_VALUE) {
 			// update description
 			Description description = new Description(mDescriptionId, mDescriptionEditText.getText()
 					.toString(), null, mPointId);
 			((BaseActivity) getActivity()).getServiceHelper().updateDescription(description.toBundle());
 		}
 		Point point = new Point();
 		point.setPersonId(mPersonId);
 		point.setName(mNameEditText.getText().toString());
 		point.setSharedToPublic(mSharedToPublicCheckBox.isChecked());
 		point.setSideCode(getSelectedSideCodeId());
 		point.setTopicId(topicId);
 		if (mPointId != INVALID_POINT_ID) {
 			// update point
 			point.setId(mPointId);
 			point.setOrderNumber(mOrderNum);
 			((BaseActivity) getActivity()).getServiceHelper().updatePoint(point.toBundle(),
 					getSelectedPoint());
 		} else {
 			// new point
 			point.setId(INVALID_POINT_ID);
 			Bundle values = point.toBundle();
 			// with new description
 			if (mDescriptionId != Integer.MIN_VALUE) {
 				throw new IllegalStateException("Cant be new point without new description");
 			}
 			// new description
 			Description description = new Description(mDescriptionId, mDescriptionEditText.getText()
 					.toString(), null, mPointId);
 			values.putAll(description.toBundle());
 			((BaseActivity) getActivity()).getServiceHelper().insertPointAndDescription(values,
 					getSelectedPoint());
 		}
 	}
 
 	@Override
 	public void onActivityCreated(final Bundle savedInstanceState) {
 
 		super.onActivityCreated(savedInstanceState);
 		String action = getArguments().getString(ExtraKey.ACTION);
 		if (Intent.ACTION_EDIT.equals(action)) {
 			onActionEdit(savedInstanceState);
 		} else if (Intent.ACTION_VIEW.equals(action)) {
 			onActionView(savedInstanceState);
 		} else if (IntentAction.NEW.equals(action)) {
 			onActionNew(savedInstanceState);
 		} else {
 			throw new IllegalArgumentException("Unknown action: " + action);
 		}
 	}
 
 	@Override
 	public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
 			final Bundle savedInstanceState) {
 
 		if (isEmpty() || (container == null) || (getArguments() == null)) {
 			if (DEBUG) {
 				Log.d(TAG, "[onCreateView] show empty fragment");
 			}
 			TextView text = (TextView) inflater.inflate(R.layout.fragment_empty, null);
 			text.setText(getActivity().getString(R.string.text_select_point));
 			return text;
 		}
 		// setup layout
 		RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.tab_fragment_point_description,
 				container, false);
 		mNameEditText = (EditText) layout.findViewById(R.id.et_point_name);
 		mDescriptionEditText = (EditText) layout.findViewById(R.id.et_point_description);
 		mSideCodeSpinner = (Spinner) layout.findViewById(R.id.spinner_point_agreement_code);
 		mSharedToPublicCheckBox = (CheckBox) layout.findViewById(R.id.chb_share_to_public);
 		if (getArguments() == null) {
 			// at this point we are expected to show point details
 			throw new IllegalArgumentException("Fragment was called without arguments");
 		}
 		if (DEBUG) {
 			Log.d(TAG, "[onCreateView] arguments: " + getArguments().toString());
 		}
 		return layout;
 	}
 
 	@Override
 	public void onSaveInstanceState(final Bundle outState) {
 
 		// TODO: save comment edit text on rotation
 		super.onSaveInstanceState(outState);
 		if (!isEmpty()) {
 			outState.putBoolean(ExtraKey.SHARED_TO_PUBLIC, mSharedToPublicCheckBox.isChecked());
 			if (mNameEditText.getText() != null) {
 				outState.putString(ExtraKey.POINT_NAME, mNameEditText.getText().toString());
 			}
 			outState.putInt(ExtraKey.AGREEMENT_CODE, getSelectedSideCodeId());
 			outState.putInt(ExtraKey.PERSON_ID, mPersonId);
 			outState.putInt(ExtraKey.TOPIC_ID, mTopicId);
 			outState.putInt(ExtraKey.POINT_ID, mPointId);
 			outState.putInt(ExtraKey.DESCRIPTION_ID, mDescriptionId);
 			if (mDescriptionEditText.getText() != null) {
 				outState.putString(ExtraKey.DESCRIPTION_TEXT, mDescriptionEditText.getText().toString());
 			}
 		}
 	}
 
 	public void setEmpty(final boolean empty) {
 
 		mIsEmpty = empty;
 	}
 
 	private SelectedPoint getSelectedPoint() {
 
 		SelectedPoint selectedPoint = new SelectedPoint();
 		selectedPoint.setDiscussionId(mDiscussionId);
 		selectedPoint.setPersonId(mPersonId);
 		selectedPoint.setPointId(mPointId);
 		selectedPoint.setTopicId(mTopicId);
 		return selectedPoint;
 	}
 
 	private int getSelectedSideCodeId() {
 
 		switch ((int) mSideCodeSpinner.getSelectedItemId()) {
 			case Points.SideCode.CONS:
 				return Points.SideCode.CONS;
 			case Points.SideCode.NEUTRAL:
 				return Points.SideCode.NEUTRAL;
 			case Points.SideCode.PROS:
 				return Points.SideCode.PROS;
 			default:
 				throw new IllegalArgumentException("Unknown side code: "
 						+ (int) mSideCodeSpinner.getSelectedItemId());
 		}
 	}
 
 	private void onActionEdit(final Bundle savedInstanceState) {
 
 		if (!getArguments().containsKey(ExtraKey.DISCUSSION_ID)) {
 			throw new IllegalStateException("intent was without discussion id");
 		}
 		if (!getArguments().containsKey(ExtraKey.POINT_ID)) {
 			throw new IllegalStateException("Fragment was called without point id in arguments");
 		}
 		mDiscussionId = getArguments().getInt(ExtraKey.DISCUSSION_ID, Integer.MIN_VALUE);
 		if (savedInstanceState == null) {
 			int initialPointId = getArguments().getInt(ExtraKey.POINT_ID, Integer.MIN_VALUE);
 			Bundle args = new Bundle();
 			args.putInt(ExtraKey.POINT_ID, initialPointId);
 			getLoaderManager().initLoader(PointCursorLoader.POINT_ID, args, mPointCursorLoader);
 		} else {
 			populateFromSavedInstanceState(savedInstanceState);
 		}
 		setViewsEnabled(true);
 	}
 
 	private void onActionNew(final Bundle savedInstanceState) {
 
 		if (!getArguments().containsKey(ExtraKey.PERSON_ID)) {
 			throw new IllegalStateException("intent was without person id");
 		}
 		if (!getArguments().containsKey(ExtraKey.TOPIC_ID)) {
 			throw new IllegalStateException("intent was without topic id");
 		}
 		if (!getArguments().containsKey(ExtraKey.DISCUSSION_ID)) {
 			throw new IllegalStateException("intent was without discussion id");
 		}
 		if (savedInstanceState == null) {
 			// leave empty fields to create new point
 			mPersonId = getArguments().getInt(ExtraKey.PERSON_ID, Integer.MIN_VALUE);
 			mTopicId = getArguments().getInt(ExtraKey.TOPIC_ID, Integer.MIN_VALUE);
 			mDiscussionId = getArguments().getInt(ExtraKey.DISCUSSION_ID, Integer.MIN_VALUE);
 			mPointId = INVALID_POINT_ID;
 		} else {
 			populateFromSavedInstanceState(savedInstanceState);
 		}
 		setViewsEnabled(true);
 	}
 
 	private void onActionView(final Bundle savedInstanceState) {
 
 		if (!getArguments().containsKey(ExtraKey.POINT_ID)) {
 			throw new IllegalStateException("Fragment was called without point id in arguments");
 		}
 		if (savedInstanceState == null) {
 			int initialPointId = getArguments().getInt(ExtraKey.POINT_ID, Integer.MIN_VALUE);
 			Bundle args = new Bundle();
 			args.putInt(ExtraKey.POINT_ID, initialPointId);
 			getLoaderManager().initLoader(PointCursorLoader.POINT_ID, args, mPointCursorLoader);
 		} else {
 			populateFromSavedInstanceState(savedInstanceState);
 		}
 		setViewsEnabled(false);
 	}
 
 	private void populateFromSavedInstanceState(final Bundle savedInstanceState) {
 
 		if (!savedInstanceState.containsKey(ExtraKey.POINT_ID)) {
 			throw new IllegalStateException("SavedInstanceState doesnt contain point id");
 		}
 		if (!savedInstanceState.containsKey(ExtraKey.PERSON_ID)) {
 			throw new IllegalStateException("SavedInstanceState doesnt contain person id");
 		}
 		if (!savedInstanceState.containsKey(ExtraKey.TOPIC_ID)) {
 			throw new IllegalStateException("SavedInstanceState doesnt contain topic id");
 		}
 		if (!savedInstanceState.containsKey(ExtraKey.POINT_NAME)) {
 			throw new IllegalStateException("SavedInstanceState doesnt contain point name");
 		}
 		if (!savedInstanceState.containsKey(ExtraKey.DESCRIPTION_TEXT)) {
 			throw new IllegalStateException("SavedInstanceState doesnt contain description text");
 		}
 		if (!savedInstanceState.containsKey(ExtraKey.AGREEMENT_CODE)) {
 			throw new IllegalStateException("SavedInstanceState doesnt contain agreement code");
 		}
 		if (!savedInstanceState.containsKey(ExtraKey.SHARED_TO_PUBLIC)) {
 			throw new IllegalStateException("SavedInstanceState doesnt contain shared to public");
 		}
 		mPointId = savedInstanceState.getInt(ExtraKey.POINT_ID, Integer.MIN_VALUE);
 		mPersonId = savedInstanceState.getInt(ExtraKey.PERSON_ID, Integer.MIN_VALUE);
 		mTopicId = savedInstanceState.getInt(ExtraKey.TOPIC_ID, Integer.MIN_VALUE);
 		mNameEditText.setText(savedInstanceState.getString(ExtraKey.POINT_NAME));
 		mDescriptionEditText.setText(savedInstanceState.getString(ExtraKey.DESCRIPTION_TEXT));
 		mSideCodeSpinner.setSelection(savedInstanceState.getInt(ExtraKey.AGREEMENT_CODE, Integer.MIN_VALUE));
 		mSharedToPublicCheckBox.setChecked(savedInstanceState.getBoolean(ExtraKey.SHARED_TO_PUBLIC));
 	}
 
 	private void setViewsEnabled(final boolean enabled) {
 
 		if (!enabled) {
 			mDescriptionEditText.setEnabled(false);
 			mNameEditText.setEnabled(false);
 			mDescriptionEditText.setEnabled(false);
 			mSideCodeSpinner.setEnabled(false);
 			mSharedToPublicCheckBox.setEnabled(false);
 		}
 	}
 
 	private enum FragmentState {
 		EDIT, EMPTY, NEW, VIEW
 	}
 
 	private class PointCursorLoader implements LoaderManager.LoaderCallbacks<Cursor> {
 
 		private static final int DESCRIPTION_ID = 1;
 		private static final int POINT_ID = 0;
 
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
 				case POINT_ID: {
 					String where = Points.Columns.ID + "=?";
 					String[] args = new String[] { String.valueOf(myPointId) };
 					return new CursorLoader(getActivity(), Points.CONTENT_URI, null, where, args, null);
 				}
 				case DESCRIPTION_ID: {
 					String where = Descriptions.Columns.POINT_ID + "=?";
 					String[] args = new String[] { String.valueOf(myPointId) };
 					return new CursorLoader(getActivity(), Descriptions.CONTENT_URI, null, where, args, null);
 				}
 				default:
 					throw new IllegalArgumentException("Unknown loader id: " + loaderId);
 			}
 		}
 
 		@Override
 		public void onLoaderReset(final Loader<Cursor> loader) {
 
 			switch (loader.getId()) {
 				case POINT_ID:
 					mPointCursor = null;
 					break;
 				case DESCRIPTION_ID:
 					mDescriptionCursor = null;
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
 				case POINT_ID: {
 					if (data.moveToFirst()) {
 						mPointCursor = data;
 						Point value = new Point(mPointCursor);
 						mPointId = value.getId();
 						mPersonId = value.getPersonId();
 						mTopicId = value.getTopicId();
 						mNameEditText.setText(value.getName());
 						mSideCodeSpinner.setSelection(value.getSideCode(), true);
 						mSharedToPublicCheckBox.setChecked(value.isSharedToPublic());
 						mOrderNum = value.getOrderNumber();
 						getSherlockActivity().getSupportActionBar().setTitle(value.getName());
 						Bundle args = new Bundle();
 						args.putInt(ExtraKey.POINT_ID, mPointId);
 						getLoaderManager().initLoader(DESCRIPTION_ID, args, this);
						getLoaderManager().destroyLoader(POINT_ID);
 					} else {
 						Log.w(TAG, "[onLoadFinished] Cant move to first item. LOADER_POINT_ID count was: "
 								+ data.getCount());
 					}
 					break;
 				}
 				case DESCRIPTION_ID:
 					if (data.getCount() == 1) {
 						mDescriptionCursor = data;
 						Description description = new Description(mDescriptionCursor);
 						mDescriptionEditText.setText(description.getText());
 						mDescriptionId = description.getId();
 					} else {
 						Log.w(TAG, "[onLoadFinished] LOADER_DESCRIPTION_ID count was: " + data.getCount());
 					}
 					break;
 				default:
 					throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
 			}
 		}
 	}
 }
