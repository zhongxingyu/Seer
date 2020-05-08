 package com.suicune.notasupna;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Locale;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.annotation.SuppressLint;
 import android.app.ActionBar;
 import android.app.ActionBar.OnNavigationListener;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.app.ListFragment;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.content.AsyncTaskLoader;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.Spinner;
 import android.widget.SpinnerAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.suicune.notasupna.database.GradesContract;
 import com.suicune.notasupna.helpers.ConnectLoader;
 import com.suicune.notasupna.helpers.GradesParserLoader;
 
 /**
  * 
  * @author Denis Lapuente
  * 
  */
 @SuppressLint("NewApi")
 public class RecordFragment extends ListFragment {
 
 	private static final int LOADER_COURSE = 1;
 	private static final int LOADER_CONNECTION = 2;
 	private static final int LOADER_PARSER = 3;
 
 	private static final int PROGRESS_SIGN_IN = 1;
 	private static final int PROGRESS_PARSE = 2;
 	private static final int PROGRESS_LOAD = 3;
 
 	private static final int ACTIVITY_PREFERENCES = 1;
 	private static final int ACTIVITY_DETAILS = 2;
 
 	private boolean isLandscape;
 	private boolean isHoneyComb;
 	private boolean isSmallScreen;
 	private boolean isParsing;
 	private boolean isConnecting;
 	private boolean isLoading;
 
 	private int mCursorPosition = 0;
 	private int mCurrentCourse = 0;
 
 	private Record mCurrentRecord = null;
 	private Student mStudent = null;
 	private Subject mCurrentSubject = null;
 
 	private View mRecordView;
 	private View mRecordStatusView;
 	private View mDetailsHintView;
 	private View mDetailsView;
 	private View mRecordHeaderView;
 	private Spinner mRecordSpinnerView;
 	private TextView mRecordStatusMessageView;
 
 	private SharedPreferences prefs;
 
 	private static RecordFragment mRecordFragment;
 	private static DetailsFragment mDetailsFragment;
 
 	public static RecordFragment getInstance() {
 		if (mRecordFragment == null) {
 			mRecordFragment = new RecordFragment();
 		}
 		return mRecordFragment;
 	}
 
 	@Override
 	public void onAttach(Activity activity) {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			isHoneyComb = true;
 		} else {
 			isHoneyComb = false;
 		}
 
 		if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
 			isLandscape = true;
 		} else {
 			isLandscape = false;
 		}
 
 		super.onAttach(activity);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.record_fragment, container, false);
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		checkScreenSize();
 		setViews(getActivity());
 		restoreParameters();
 
 		Bundle extras = getActivity().getIntent().getExtras();
 		if (extras != null) {
 			FragmentActivity activity = (FragmentActivity) getActivity();
 			isParsing = true;
 			activity.getSupportLoaderManager().restartLoader(LOADER_PARSER,
 					extras, new AsyncHelper());
 		}
 
 		if (!isParsing) {
 			loadData();
 		}
 		if (isLandscape) {
 			mDetailsHintView.setVisibility(View.VISIBLE);
 			mDetailsView.setVisibility(View.GONE);
 		}
 		super.onActivityCreated(savedInstanceState);
 	}
 
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
 		if ((mCurrentSubject != null)
 				&& (mCurrentSubject.equals(mCurrentRecord.mSubjectsList
 						.get(position)))) {
 			return;
 		}
 		setCursorPosition(position);
 		showDetails(mCurrentRecord.mSubjectsList.get(position));
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		switch (requestCode) {
 		case ACTIVITY_DETAILS:
 			if (mStudent != null) {
 				showData(false);
 			}
 			break;
 		case ACTIVITY_PREFERENCES:
 			if (resultCode == PreferencesActivity.RESULT_LANGUAGE_CHANGED) {
 				changeLanguage();
 			} else {
 				showData(false);
 			}
 			break;
 		}
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.action_record_refresh:
 			getActivity().getSupportLoaderManager().restartLoader(
 					LOADER_CONNECTION, null, new AsyncHelper());
 			break;
 		case R.id.action_settings:
 			Intent intent = new Intent(getActivity(), PreferencesActivity.class);
 			startActivityForResult(intent, ACTIVITY_PREFERENCES);
 			break;
 		case R.id.action_help:
 			// TODO
 			break;
 		}
 		return true;
 	}
 
 	private void showDetails(Subject subject) {
 		mCurrentSubject = subject;
 		if (isLandscape) {
 			mDetailsFragment = DetailsFragment.newInstance(mCursorPosition);
 			if (mCurrentSubject != null) {
 				mDetailsFragment.setSubject(mCurrentSubject);
 			}
 			
 			FragmentTransaction transaction = getActivity()
 					.getSupportFragmentManager().beginTransaction();
 			transaction.replace(R.id.record_details, mDetailsFragment);
 			transaction.commit();
 			
 			mDetailsHintView.setVisibility(View.GONE);
 			mDetailsView.setVisibility(View.VISIBLE);
 		} else {
 			Intent intent = new Intent(getActivity(), DetailsActivity.class);
 			intent.putExtra(DetailsActivity.EXTRA_SUBJECT, subject);
 			startActivity(intent);
 		}
 	}
 
 	@SuppressLint("NewApi")
 	private void checkScreenSize() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
 			isSmallScreen = getResources().getConfiguration()
 					.isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_LARGE) ? false
 					: true;
 		} else {
 			isSmallScreen = (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) < 3;
 		}
 	}
 
 	private void setViews(Activity activity) {
 		setHasOptionsMenu(true);
 
 		mRecordView = activity.findViewById(R.id.record_view);
 		mRecordStatusView = activity.findViewById(R.id.record_status);
 		mRecordStatusMessageView = (TextView) activity
 				.findViewById(R.id.record_status_message);
 
 		mDetailsView = activity.findViewById(R.id.record_details);
 		mDetailsHintView = activity.findViewById(R.id.record_details_hint);
 		mRecordHeaderView = activity.findViewById(R.id.record_header_block);
 
 		View otherCallsView = activity.findViewById(R.id.record_calls);
 		if (isLandscape && isSmallScreen) {
 			if (otherCallsView != null) {
 				otherCallsView.setVisibility(View.GONE);
 			}
 			if (mRecordHeaderView != null) {
 				mRecordHeaderView.setVisibility(View.GONE);
 			}
 		} else {
 			if (otherCallsView != null) {
 				otherCallsView.setVisibility(View.VISIBLE);
 			}
 			if (mRecordHeaderView != null) {
 				mRecordHeaderView.setVisibility(View.VISIBLE);
 			}
 		}
 		mRecordSpinnerView = (Spinner) activity
 				.findViewById(R.id.record_course_spinner);
 		if (isHoneyComb) {
 			mRecordSpinnerView.setVisibility(View.GONE);
 		} else {
 			mRecordSpinnerView.setVisibility(View.VISIBLE);
 		}
 	}
 
 	private void restoreParameters() {
 		prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
 		mCurrentCourse = prefs
 				.getInt(PreferencesActivity.LAST_COURSE_VIEWED, 0);
 		mCursorPosition = prefs.getInt(PreferencesActivity.LAST_SUBJECT_VIEWED,
 				0);
 	}
 
 	private void setCourseData() {
 		if (isHoneyComb) {
 			ActionBar actionBar = getActivity().getActionBar();
 			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
 			actionBar.setListNavigationCallbacks(setSpinnerAdapter(),
 					setNavigationCallbacks());
 			actionBar.setSelectedNavigationItem(mCurrentCourse);
 		} else {
 			mRecordSpinnerView.setAdapter(setSpinnerAdapter());
 			mRecordSpinnerView.setSelection(mCurrentCourse);
 			mRecordSpinnerView
 					.setOnItemSelectedListener(getOnItemSelectedListener());
 		}
 	}
 
 	private void loadData() {
 		getActivity().getSupportLoaderManager().restartLoader(LOADER_COURSE, null,
 				new CursorLoaderHelper());
 	}
 
 	private void showData(boolean isLoader) {
 		sortList();
 		if (!isLandscape || !isSmallScreen) {
 			showStudentInfo();
 			showCourseInfo();
 		}
 		showRecord();
 		if (isLandscape && !isLoader) {
 			showDetails(mCurrentSubject);
 			mDetailsHintView.setVisibility(View.VISIBLE);
 			mDetailsView.setVisibility(View.GONE);
 		}
 	}
 
 	private void showStudentInfo() {
 		TextView studentNameView = (TextView) getActivity().findViewById(
 				R.id.student_name);
 		TextView studentNifView = (TextView) getActivity().findViewById(
 				R.id.student_nif);
 		TextView studentNiaView = (TextView) getActivity().findViewById(
 				R.id.student_nia);
 
 		studentNameView.setText(mStudent.mStudentFullName);
 		studentNifView.setText(getString(R.string.record_header_nif)
 				+ mStudent.mStudentNif);
 		studentNiaView.setText(getString(R.string.record_header_nia)
 				+ mStudent.mStudentNia);
 	}
 
 	private void showCourseInfo() {
 		TextView courseNameView = (TextView) getActivity().findViewById(
 				R.id.record_course);
 		TextView courseCenterView = (TextView) getActivity().findViewById(
 				R.id.record_center);
 		TextView courseStudiesView = (TextView) getActivity().findViewById(
 				R.id.record_studies);
 
 		courseNameView.setText(mCurrentRecord.mCourseName);
 		courseCenterView.setText(mCurrentRecord.mCourseCenter);
 		courseStudiesView.setText(mCurrentRecord.mCourseStudies);
 	}
 
 	private void showRecord() {
 		String[] from = { GradesContract.SubjectsTable.COL_SU_NAME,
 				GradesContract.SubjectsTable.COL_SU_CREDITS,
 				GradesContract.GradesTable.COL_GR_CODE };
 		int[] to = { R.id.record_item_name, R.id.record_item_credits,
 				R.id.record_item_grade };
 		SimpleAdapter adapter = new SimpleAdapter(getActivity(),
 				prepareSubjectsList(), R.layout.record_list_item, from, to);
 		setListAdapter(adapter);
 	}
 
 	private void sortList() {
 		String sortOrder = PreferencesActivity.getSortOrder(getActivity());
 		if (sortOrder
 				.equalsIgnoreCase(getString(R.string.sort_order_alpha_asc_value))) {
 			mStudent.sortByName(true);
 		} else if (sortOrder
 				.equalsIgnoreCase(getString(R.string.sort_order_alpha_desc_value))) {
 			mStudent.sortByName(false);
 		} else if (sortOrder
 				.equalsIgnoreCase(getString(R.string.sort_order_time_asc_value))) {
 			mStudent.sortByTime(true);
 		} else if (sortOrder
 				.equalsIgnoreCase(getString(R.string.sort_order_time_desc_value))) {
 			mStudent.sortByTime(false);
 		}
 	}
 
 	private void changeLanguage() {
 		if (needsDownload()) {
 			Locale.setDefault(new Locale(
					prefs.getString(PreferencesActivity.PREFERENCE_APP_LANGUAGE, getString(R.string.language_code_spanish))));
 			getActivity().getSupportLoaderManager().restartLoader(
 					LOADER_CONNECTION, null, new AsyncHelper());
 		} else {
 			getActivity().getSupportLoaderManager().restartLoader(LOADER_COURSE,
 					null, new CursorLoaderHelper());
 		}
 
 	}
 
 	private boolean needsDownload() {
 		String language = PreferencesActivity.getRecordLanguage(getActivity());
 		if (language.equalsIgnoreCase(getString(R.string.language_code_basque))) {
 			if (prefs.getInt(PreferencesActivity.DATA_EU, 0) == 0) {
 				return true;
 			}
 		} else if (language
 				.equalsIgnoreCase(getString(R.string.language_code_spanish))) {
 			if (prefs.getInt(PreferencesActivity.DATA_ES, 0) == 0) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private ArrayList<HashMap<String, String>> prepareSubjectsList() {
 		ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
 		ArrayList<String> subjectsList = new ArrayList<String>();
 		for (int i = 0; i < mCurrentRecord.mSubjectsCount; i++) {
 			Subject subject = mCurrentRecord.mSubjectsList.get(i);
 			if (!subjectsList.contains(subject.mSubjectName)) {
 				subjectsList.add(subject.mSubjectName);
 				HashMap<String, String> item = new HashMap<String, String>();
 				item.put(GradesContract.SubjectsTable.COL_SU_NAME,
 						subject.mSubjectName);
 				item.put(GradesContract.SubjectsTable.COL_SU_CREDITS,
 						getString(R.string.subject_credits)
 								+ subject.mSubjectCredits);
 				item.put(GradesContract.GradesTable.COL_GR_CODE,
 						subject.getLastGrade().mGradeLetter);
 				result.add(item);
 			}
 		}
 		return result;
 	}
 
 	private void setCursorPosition(int position) {
 		mCurrentSubject = mCurrentRecord.mSubjectsList.get(position);
 		mCursorPosition = position;
 		SharedPreferences.Editor editor = prefs.edit();
 		editor.putInt(PreferencesActivity.LAST_SUBJECT_VIEWED, position);
 		editor.commit();
 	}
 
 	private void setSpinnerPosition(int position) {
 		mCurrentRecord = mStudent.mRecordList.get(position);
 		mCurrentSubject = null;
 		mCursorPosition = 0;
 		SharedPreferences.Editor editor = prefs.edit();
 		editor.putInt(PreferencesActivity.LAST_SUBJECT_VIEWED, 0);
 		editor.putInt(PreferencesActivity.LAST_COURSE_VIEWED, position);
 		editor.commit();
 	}
 
 	private SpinnerAdapter setSpinnerAdapter() {
 		String[] from = { GradesContract.CoursesTable.COL_CO_NAME };
 
 		int[] to = { android.R.id.text1 };
 
 		SimpleAdapter adapter = new SimpleAdapter(getActivity(),
 				prepareSpinnerData(), android.R.layout.simple_spinner_item,
 				from, to);
 		adapter.setDropDownViewResource(android.R.layout.select_dialog_item);
 		return adapter;
 	}
 
 	private ArrayList<HashMap<String, String>> prepareSpinnerData() {
 		ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
 		for (int i = 0; i < mStudent.mRecordCount; i++) {
 			HashMap<String, String> item = new HashMap<String, String>();
 			item.put(GradesContract.CoursesTable.COL_CO_NAME,
 					mStudent.mRecordList.get(i).mCourseName);
 			result.add(item);
 		}
 		return result;
 	}
 
 	private OnNavigationListener setNavigationCallbacks() {
 		OnNavigationListener listener = new OnNavigationListener() {
 
 			@Override
 			public boolean onNavigationItemSelected(int itemPosition,
 					long itemId) {
 				setSpinnerPosition(itemPosition);
 				showData(false);
 				return true;
 			}
 		};
 		return listener;
 	}
 
 	private OnItemSelectedListener getOnItemSelectedListener() {
 		OnItemSelectedListener listener = new OnItemSelectedListener() {
 
 			@Override
 			public void onItemSelected(AdapterView<?> adapterView, View view,
 					int position, long itemId) {
 				setSpinnerPosition(position);
 				showData(false);
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {
 				return;
 			}
 		};
 		return listener;
 	}
 
 	private void showProgress(final boolean show, int who) {
 		String statusMessage = "";
 		switch (who) {
 		case PROGRESS_LOAD:
 		case PROGRESS_PARSE:
 			statusMessage = getString(R.string.loading_data);
 			break;
 		case PROGRESS_SIGN_IN:
 			statusMessage = getString(R.string.login_progress_signing_in);
 			break;
 		}
 
 		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
 		// for very easy animations. If available, use these APIs to fade-in
 		// the progress spinner.
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
 			int shortAnimTime = getResources().getInteger(
 					android.R.integer.config_shortAnimTime);
 
 			mRecordStatusView.setVisibility(View.VISIBLE);
 			mRecordStatusView.animate().setDuration(shortAnimTime)
 					.alpha(show ? 1 : 0)
 					.setListener(new AnimatorListenerAdapter() {
 						@Override
 						public void onAnimationEnd(Animator animation) {
 							mRecordStatusView.setVisibility(show ? View.VISIBLE
 									: View.GONE);
 						}
 					});
 
 			mRecordView.setVisibility(View.VISIBLE);
 			mRecordView.animate().setDuration(shortAnimTime)
 					.alpha(show ? 0 : 1)
 					.setListener(new AnimatorListenerAdapter() {
 						@Override
 						public void onAnimationEnd(Animator animation) {
 							mRecordView.setVisibility(show ? View.GONE
 									: View.VISIBLE);
 						}
 					});
 		} else {
 			// The ViewPropertyAnimator APIs are not available, so simply show
 			// and hide the relevant UI components.
 			if (show) {
 				mRecordView.setVisibility(View.GONE);
 				mRecordStatusMessageView.setText(statusMessage);
 				mRecordStatusView.setVisibility(View.VISIBLE);
 			} else {
 				mRecordView.setVisibility(View.VISIBLE);
 				mRecordStatusView.setVisibility(View.GONE);
 			}
 		}
 	}
 
 	private boolean hasNewData(int dataLength) {
 		boolean result = false;
 
 		String language = PreferencesActivity.getRecordLanguage(getActivity());
 
 		if (language.equalsIgnoreCase(getString(R.string.language_code_basque))) {
 			result = dataLength > prefs.getInt(PreferencesActivity.DATA_EU, 0);
 			if (result) {
 				prefs.edit().putInt(PreferencesActivity.DATA_EU, dataLength)
 						.commit();
 			}
 		} else {
 			result = dataLength > prefs.getInt(PreferencesActivity.DATA_ES, 0);
 			if (result) {
 				prefs.edit().putInt(PreferencesActivity.DATA_ES, dataLength)
 						.commit();
 			}
 		}
 
 		return result;
 	}
 
 	private class AsyncHelper implements LoaderCallbacks<String> {
 
 		@Override
 		public Loader<String> onCreateLoader(int id, Bundle args) {
 			AsyncTaskLoader<String> loader = null;
 			switch (id) {
 			case LOADER_CONNECTION:
 				showProgress(true, PROGRESS_SIGN_IN);
 				isConnecting = true;
 				loader = new ConnectLoader(getActivity(), args);
 				break;
 			case LOADER_PARSER:
 				showProgress(true, PROGRESS_PARSE);
 				isParsing = true;
 				loader = new GradesParserLoader(getActivity(), args);
 				break;
 			}
 			return loader;
 		}
 
 		@Override
 		public void onLoadFinished(Loader<String> loader, String result) {
 			switch (loader.getId()) {
 			case LOADER_CONNECTION:
 				if (result == null) {
 					Toast.makeText(getActivity(), R.string.error_connection,
 							Toast.LENGTH_LONG).show();
 				} else {
 					try {
 						JSONObject object = new JSONObject(result);
 						int error = object.getInt(GradesParserLoader.nError);
 						switch (error) {
 						case 0:
 							if (hasNewData(result.length())) {
 								Bundle args = new Bundle();
 								args.putString(
 										RecordActivity.EXTRA_DOWNLOADED_DATA,
 										result);
 								getActivity().getSupportLoaderManager()
 										.restartLoader(LOADER_PARSER, args, this);
 							} else {
 								Toast.makeText(getActivity(),
 										R.string.no_new_data, Toast.LENGTH_LONG)
 										.show();
 							}
 							break;
 						default:
 							String errorMsg = object
 									.getString(GradesParserLoader.nErrorMsg);
 							failedLogin(errorMsg, ConnectLoader.ERROR_JSON);
 							break;
 						}
 					} catch (JSONException e) {
 						Log.d(ConnectLoader.logging,
 								"Error in response from server: " + result);
 						failedLogin(result, ConnectLoader.ERROR_NO_JSON);
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 				isConnecting = false;
 				if(!isParsing && !isLoading){
 					showProgress(false, PROGRESS_SIGN_IN);
 				}
 
 				break;
 			case LOADER_PARSER:
 				isParsing = false;
 				loadData();
 				if(!isConnecting && !isLoading){
 					showProgress(false, PROGRESS_PARSE);
 				}
 				break;
 			}
 		}
 
 		@Override
 		public void onLoaderReset(Loader<String> loader) {
 			loader.reset();
 		}
 
 		public void failedLogin(String response, int errorCode) {
 			switch (errorCode) {
 			case ConnectLoader.ERROR_JSON:
 				Toast.makeText(getActivity(), response, Toast.LENGTH_LONG)
 						.show();
 				break;
 			case ConnectLoader.ERROR_NO_JSON:
 				Toast.makeText(getActivity(), R.string.error_connecting,
 						Toast.LENGTH_LONG).show();
 				break;
 			default:
 				break;
 			}
 		}
 	}
 
 	private class CursorLoaderHelper implements LoaderCallbacks<Cursor> {
 
 		@Override
 		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
 			showProgress(true, PROGRESS_LOAD);
 			CursorLoader loader = null;
 			switch (id) {
 			case LOADER_COURSE:
 				isLoading = true;
 				Uri uri = GradesContract.CONTENT_NAME_ALL;
 				String selection = GradesContract.GradesTable.COL_GR_LANGUAGE
 						+ "=?";
 				String[] selectionArgs = { PreferencesActivity
 						.getRecordLanguage(getActivity()) };
 				loader = new CursorLoader(getActivity(), uri, null, selection,
 						selectionArgs,
 						PreferencesActivity.getSortOrder(getActivity()));
 				break;
 			}
 			return loader;
 		}
 
 		@Override
 		public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
 			switch (loader.getId()) {
 			case LOADER_COURSE:
 				isLoading = false;
 				mStudent = new Student(cursor);
 				mCurrentRecord = mStudent.mRecordList.get(mCurrentCourse);
 				setCourseData();
 				showData(true);
 				break;
 			}
 			showProgress(false, PROGRESS_LOAD);
 		}
 
 		@Override
 		public void onLoaderReset(Loader<Cursor> loader) {
 			loader.reset();
 		}
 	}
 }
