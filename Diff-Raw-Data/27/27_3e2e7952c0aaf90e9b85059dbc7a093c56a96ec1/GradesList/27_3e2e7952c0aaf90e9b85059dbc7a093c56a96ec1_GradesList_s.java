 package de.nware.app.hsDroid.ui;
 
 import java.util.Date;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Looper;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.provider.BaseColumns;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 import de.nware.app.hsDroid.R;
 import de.nware.app.hsDroid.provider.onlineService2Data.ExamInfos;
 import de.nware.app.hsDroid.provider.onlineService2Data.ExamsCol;
 import de.nware.app.hsDroid.provider.onlineService2Data.ExamsUpdateCol;
 
 /**
  * {@link ListActivity} zum anzeigen der Prüfungen
  * 
  * @author Oliver Eichner
  * 
  */
 public class GradesList extends nActivity {
 
 	private static final String TAG = "GradesListActivity";
 	// private ExamAdapter m_examAdapter;
 	private ListView lv;
 	// private ArrayList<Exam> examsTest;
 	// private ExamInfo currentEInfo;
 	// private GradeParserThread mGradeParserThread = null;
 	// private ExamInfoParserThread mExamInfoParserThread = null;
 	private Cursor cursor = null;
 	private Cursor examinfoCursor = null;
 
 	private ExamDBAdapter mExamAdapter;
 
 	private ProgressDialog mProgressDialog = null;
 
 	private SharedPreferences mPreferences;
 
 	private boolean autoUpdate;
 	private boolean forceAutoUpdate = false;
 	private static final byte DIALOG_PROGRESS = 1;
 
 	private final int HANDLER_MSG_REFRESH = 1;
 	private final int HANDLER_MSG_LOADING = 2;
 	private final int HANDLER_MSG_INFO_GET = 3;
 	private final int HANDLER_MSG_INFO_READY = 4;
 
 	private static final String SORT_ALL = "";
 	private static final String SORT_ALL_FAILED = "allfail";
 	private static final String SORT_ACTUAL = "act";
 	private static final String SORT_ACTUAL_FAILED = "actfail";
 	private static String ACTUAL_SORT = SORT_ALL;
 
 	// private boolean customTitleSupported;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.grade_list_view);
 		customTitle(getText(R.string.grades_view).toString());
 
 		// einstellungne holen
 		mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
 		String dbUser = mPreferences.getString("dbUser", "0");
 		String user = mPreferences.getString("UserSave", "");
 		Log.d(TAG, "dbUser:" + dbUser + " user:" + user);
 		if (!dbUser.equals(user)) {
 			clearDB();
 			forceAutoUpdate = true;
 		}
 		ACTUAL_SORT = getDefaultListSort();
 		Log.d(TAG, "create resolver");
 		final ContentResolver resolver = getContentResolver();
 
 		// lv = getListView();
 		lv = (ListView) findViewById(R.id.gradesListView);
 
 		cursor = resolver.query(ExamsCol.CONTENT_URI, null, null, null, null);
 		startManagingCursor(cursor);
 
 		final String[] from = new String[] { ExamsCol.EXAMNAME, ExamsCol.EXAMNR, ExamsCol.ATTEMPTS, ExamsCol.GRADE };
 		final int[] to = new int[] { R.id.examName, R.id.examNr, R.id.examGrade, R.id.examAttempts, R.id.examGrade };
 		mExamAdapter = new ExamDBAdapter(GradesList.this, R.layout.grade_row_item, cursor, from, to);
 		lv.setAdapter(mExamAdapter);
 
 		autoUpdate = mPreferences.getBoolean("autoUpdatePref", false);
 		if (mExamAdapter.getCount() == 0 || autoUpdate || forceAutoUpdate) {
 			updateGrades();
 			if (forceAutoUpdate) {
 				forceAutoUpdate = false;
 			}
 		}
 
 		this.mExamAdapter.getFilter().filter(getDefaultListSort());
 
 		lv.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				// url muss vorhanden sein
 				// if
 				// (!GradesList.this.m_examAdapter.getItem(position).getInfoLink().equals(""))
 				// {
 				Log.d(TAG, "itemid: " + mExamAdapter.getItemId(position));
 				long itemID = mExamAdapter.getItemId(position);
 				String selection = BaseColumns._ID + " LIKE ?";
 
 				Cursor cur = getContentResolver().query(ExamsCol.CONTENT_URI, null, selection,
 						new String[] { String.valueOf(itemID) }, null);
 				startManagingCursor(cur);
 				final String out;
 				if (cur.moveToFirst()) {
 					out = cur.getString(cur.getColumnIndexOrThrow(ExamsCol.LINKID)).toString();
 					Log.d(TAG, "out: [" + out + "]");
 					final String name = cur.getString(cur.getColumnIndexOrThrow(ExamsCol.EXAMNAME)).toString();
 					final String nr = cur.getString(cur.getColumnIndexOrThrow(ExamsCol.EXAMNR)).toString();
 					final String semester = cur.getString(cur.getColumnIndexOrThrow(ExamsCol.SEMESTER)).toString();
 					final String grade = cur.getString(cur.getColumnIndexOrThrow(ExamsCol.GRADE)).toString();
 					if (!out.equals("0") && !grade.equals("0,0")) { // FIXME
 
 						Log.d(TAG, "show examInfo");
 						// showDialog(DIALOG_PROGRESS);
 						showTitleProgress();
 						showToast("Lade Notenverteilung für " + name + ".");
 						// mProgressHandle.sendMessage(mProgressHandle.obtainMessage(HANDLER_MSG_INFO_GET));
 						setRequestedOrientation(2);
 						Thread t = new Thread() {
 							public void run() {
 								try {
 									Looper.prepare();
 
 									// ContentProvider öffnen
 									final ContentResolver resolver = getContentResolver();
 									// Cursor setzen
 
 									examinfoCursor = resolver.query(ExamInfos.CONTENT_URI, null, null,
 											new String[] { out }, null);
 									startManagingCursor(examinfoCursor);
 									examinfoCursor.moveToFirst();
 
 									// Dem Handler bescheid sagen, dass die
 									// Daten
 									// nun
 									// verfügbar sind
 									Message oMessage = mProgressHandle.obtainMessage();
 									Bundle oBundle = new Bundle();
 
 									oBundle.putString("Name", name);
 									oBundle.putString("Nr", nr);
 									oBundle.putString("Semester", semester);
 
 									oMessage.setData(oBundle);
 									oMessage.what = HANDLER_MSG_INFO_READY;
 									mProgressHandle.sendMessage(oMessage);
 
 								} catch (Exception e) {
 									dismissDialog(DIALOG_PROGRESS);
 									createDialog(GradesList.this.getString(R.string.error), e.getMessage());
 									e.printStackTrace();
 								}
 								Looper.loop();
 							}
 						};
 						t.start();
 					} else {
 						showToast("Keine Notenverteilung für " + name + " verfügbar.");
 					}
 				}
 
 			}
 		});
 
 	}
 
 	// helper, get rid of....
 	// hashmap??
 	private String getDefaultListOrder() {
 		String prefOrder = mPreferences.getString("defaultOrderPref", "DESC");
 		return prefOrder;
 	}
 
 	private String getDefaultListSort() {
 
 		String prefView = mPreferences.getString("defaultViewPref", "1");
 		if (prefView.equals("")) {
 			prefView = "1";
 		}
 		switch (Integer.valueOf(prefView)) {
 		case 0:
 			return SORT_ALL;
 		case 1:
 			return SORT_ACTUAL;
 		case 2:
 			return SORT_ACTUAL_FAILED;
 		case 3:
 			return SORT_ALL_FAILED;
 
 		default:
 			return prefView;
 		}
 	}
 
 	/**
 	 * ProgresDialog Handler
 	 */
 	final Handler mProgressHandle = new Handler() {
 
 		@Override
 		public void handleMessage(Message msg) {
 			Log.d("handler msg.what:", String.valueOf(msg.what));
 
 			switch (msg.what) {
 
 			case HANDLER_MSG_REFRESH:
 				// ListView wieder neu laden
 				refreshList();
 				// Bildschirm Orientierung wieder dem User überlassen
 				setRequestedOrientation(-1);
 				// dismissDialog(DIALOG_PROGRESS);
 				hideTitleProgress();
 				break;
 
 			case HANDLER_MSG_LOADING:
 				// mProgressDialog.setMessage(GradesList.this.getString(R.string.progress_loading));
 				break;
 
 			case HANDLER_MSG_INFO_GET:
 				// mProgressDialog.setMessage(GradesList.this.getString(R.string.progress_loading));
 				break;
 			case HANDLER_MSG_INFO_READY:
 				if (examinfoCursor == null) {
 					Log.d(TAG, "cursor null");
 				}
 
 				new ExamInfoDialog(GradesList.this, msg.getData().getString("Name"), msg.getData().getString("Nr"), msg
 						.getData().getString("Semester"), examinfoCursor);
 				// dismissDialog(DIALOG_PROGRESS);
 				hideTitleProgress();
 				// schließe progress und zeige infodialog
 				break;
 
 			default:
 				// Log.d("onCreate should not happen",
 				// String.valueOf(mGradeParserThread.getStatus()));
 				// dismissDialog(DIALOG_PROGRESS);
 				hideTitleProgress();
 				// Get rid of the sending thread
 				// mGradeParserThread.stopThread();
 				// mGradeParserThread = null;
 				break;
 			}
 		}
 	};
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		switch (id) {
 		case DIALOG_PROGRESS:
 			mProgressDialog = new ProgressDialog(this);
 			// mProgressDialog.setMessage(this.getString(R.string.progress_connect));
 			mProgressDialog.setIndeterminate(true);
 			mProgressDialog.setCancelable(false);
 			return mProgressDialog;
 
 		default:
 			return null;
 		}
 	}
 
 	@Override
 	protected void onRestoreInstanceState(Bundle outState) {
 		super.onRestoreInstanceState(outState);
 
 		// examsTest = (ArrayList<Exam>) outState.get("exams_list");
 		// this.m_examAdapter.getFilter().filter(GradesList.ACTUAL_SORT);
 		// FIXME nicht default , alte sortierung.!!
 
 		System.out.println("test onRestoreInstanceState");
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 
 		// outState.putParcelableArrayList("exams_list", examsTest);
 		System.out.println("test onSaveInstanceState");
 
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		System.out.println("test onPause");
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		ACTUAL_SORT = getDefaultListSort();
 	}
 
 	@Override
 	public void onWindowFocusChanged(boolean hasFocus) {
 		if (hasFocus) {
 			this.mExamAdapter.getFilter().filter(ACTUAL_SORT);
 		}
 	}
 
 	/**
 	 * Optionsmenü
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.grade_menu, menu);
 		return true;
 	}
 
 	/**
 	 * Optionsmenü Callback
 	 */
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_about:
 			new AboutDialog(this);
 			return true;
 		case R.id.view_menu_refresh:
 
 			updateGrades();
 
 			return true;
 		case R.id.view_menu_preferences:
 			Intent settingsActivity = new Intent(getBaseContext(), Preferences.class);
 			startActivity(settingsActivity);
 			mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
 			ACTUAL_SORT = getDefaultListSort();
 			return true;
 		case R.id.view_submenu_examViewAll:
 			if (item.isChecked())
 				item.setChecked(false);
 			else
 				item.setChecked(true);
 
 			ACTUAL_SORT = SORT_ALL;
 			mExamAdapter.getFilter().filter(ACTUAL_SORT);
 			return true;
 		case R.id.view_submenu_examViewOnlyLast:
 			if (item.isChecked())
 				item.setChecked(false);
 			else
 				item.setChecked(true);
 			ACTUAL_SORT = SORT_ACTUAL;
 			mExamAdapter.getFilter().filter(ACTUAL_SORT);
 			return true;
 		case R.id.view_submenu_examViewOnlyLastFailed:
 			if (item.isChecked())
 				item.setChecked(false);
 			else
 				item.setChecked(true);
 
 			ACTUAL_SORT = SORT_ACTUAL_FAILED;
 			mExamAdapter.getFilter().filter(ACTUAL_SORT);
 			return true;
 		case R.id.view_submenu_examViewAllFailed:
 			if (item.isChecked())
 				item.setChecked(false);
 			else
 				item.setChecked(true);
 
 			ACTUAL_SORT = SORT_ALL_FAILED;
 			mExamAdapter.getFilter().filter(ACTUAL_SORT);
 			return true;
 		default:
 			Log.d("GradeView menu:", "default");
 			return super.onOptionsItemSelected(item);
 		}
 
 	}
 
 	private void clearDB() {
 		Thread clrDB = new Thread() {
 			public void run() {
 				try {
 					Looper.prepare();
 					final ContentResolver resolver = getContentResolver();
 					resolver.delete(ExamsCol.CONTENT_URI, null, null);
 
 				} catch (Exception e) {
 					e.printStackTrace();
 
 				}
 			}
 		};
 		clrDB.start();
 
 	}
 
 	private void updateGrades() {
 		// Thread, update grades progress
 		// showDialog(DIALOG_PROGRESS);
 		showTitleProgress();
 		showToast("Aktualisiere Notenblatt...");
 		mProgressHandle.sendMessage(mProgressHandle.obtainMessage(HANDLER_MSG_LOADING));
 		setRequestedOrientation(2);
 		Thread t = new Thread() {
 			public void run() {
 				try {
 					Looper.prepare();
 
 					// ContentProvider öffnen
 					final ContentResolver resolver = getContentResolver();
 					// Cursor setzen
 					final Cursor cursor = resolver.query(ExamsUpdateCol.CONTENT_URI, null, null, null, null);
 					startManagingCursor(cursor);
 
 					cursor.close();
 					// Dem Handler bescheid sagen, dass die Daten nun
 					// verfügbar sind
 					mProgressHandle.sendMessage(mProgressHandle.obtainMessage(HANDLER_MSG_REFRESH));
 
 				} catch (Exception e) {
 					dismissDialog(DIALOG_PROGRESS);
 					createDialog(GradesList.this.getString(R.string.error), e.getMessage());
 					e.printStackTrace();
 				}
 				Looper.loop();
 			}
 		};
 		t.start();
 
 	}
 
 	private void refreshList() {
 		this.lv.invalidateViews();
 
 	}
 
 	private void createDialog(String title, String text) {
 		AlertDialog ad = new AlertDialog.Builder(this).setPositiveButton(this.getString(R.string.error_ok), null)
 				.setTitle(title).setMessage(text).create();
 		ad.show();
 	}
 
 	/**
 	 * Gibt das aktuelle Semester zurück
 	 * 
 	 * @return ein {@link String} ("WiSe XX/XX" oder "SoSe XX")
 	 */
	private String getLastExamSem() {
 		String semString = "";
 		Date dt = new Date();
 		int year = dt.getYear() - 100;
 		int month = dt.getMonth() + 1;
		// if (month > 1 && month < 7) { // zwischen jan und jul ws anzeigen
		if (month > 10 && month < 3) {
			semString = "WiSe " + (year - 1) + "/" + year;
		} else {// ansonsten ss
			// wenn januar is, dann jahr-1
			if (month == 1) {
				year--;
 			}
 			semString = "SoSe " + year;
 		}
 		return semString;
 	}
 
 	/**
 	 * 
 	 * @author Oliver Eichner
 	 * 
 	 */
 	public class ExamDBAdapter extends SimpleCursorAdapter {
 
 		private Context context;
 		private int layout;
 
 		public ExamDBAdapter(Context context, int layout, Cursor cursor, String[] from, int[] to) {
 			super(context, layout, cursor, from, to);
 			this.context = context;
 			this.layout = layout;
 		}
 
 		@Override
 		public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
 			// Log.d(TAG, "exAdapter newView");
 			Cursor c = getCursor();
 
 			final LayoutInflater inflater = LayoutInflater.from(context);
 			View v = inflater.inflate(layout, viewGroup, false);
 
 			int nameCol = c.getColumnIndex(ExamsCol.EXAMNAME);
 			String name = c.getString(nameCol);
 			int rnCol = c.getColumnIndex(ExamsCol.EXAMNR);
 			String nr = c.getString(rnCol);
 			int attCol = c.getColumnIndex(ExamsCol.ATTEMPTS);
 			int att = c.getInt(attCol);
 			int gradeCol = c.getColumnIndex(ExamsCol.GRADE);
 			String grade = c.getString(gradeCol);
 			int semCol = c.getColumnIndex(ExamsCol.SEMESTER);
 			String sem = c.getString(semCol);
 			int passedCol = c.getColumnIndex(ExamsCol.PASSED);
 			int passed = c.getInt(passedCol);
 
 			// Log.d(TAG, "name: " + name);
 			// Log.d(TAG, "nr: " + nr);
 			// Log.d(TAG, "att: " + att);
 			// Log.d(TAG, "grade:" + grade);
 			// Log.d(TAG, "sem:" + sem);
 			// Log.d(TAG, "passed:" + passed);
 
 			/**
 			 * Next set the name of the entry.
 			 */
 			TextView exName = (TextView) v.findViewById(R.id.examName);
 
 			TextView exNr = (TextView) v.findViewById(R.id.examNr);
 
 			TextView exAtt = (TextView) v.findViewById(R.id.examAttempts);
 			TextView exGrade = (TextView) v.findViewById(R.id.examGrade);
 			TextView exSemester = (TextView) v.findViewById(R.id.examSemester);
 
 			if (exName != null) {
 				exName.setText(name);
 				if (isActualExam(sem) && mPreferences.getBoolean("highlightActualExamsPref", false)) {
 					exName.setShadowLayer(3, 0, 0, Color.GREEN);
 				} else {
 					exName.setShadowLayer(0, 0, 0, 0);
 				}
 			}
 			if (exNr != null) {
 				exNr.setText(nr);
 			}
 			if (exSemester != null) {
 				exSemester.setText(getApplicationContext().getString(R.string.grades_view_semester) + sem);
 			}
 			if (exAtt != null && att != 0) {
 				exAtt.setText(getApplicationContext().getString(R.string.grades_view_attempt) + att);
 			}
 			if (exGrade != null) {
 				if (passed == 0) {
 					// FIXME wenn möglich.. farben gedöns is ziemlich tricky
 					// wegen "recycler" von ListActivity
 					if (att > 1) {
 						exGrade.setTextColor(Color.BLACK);
 						exGrade.setBackgroundColor(Color.RED);
 						exGrade.setCompoundDrawablePadding(2);
 
 					} else {
 						exGrade.setTextColor(Color.RED);
 						exGrade.setBackgroundColor(Color.TRANSPARENT);
 					}
 				} else {
 					exGrade.setTextColor(Color.rgb(0x87, 0xeb, 0x0c));
 
 					exGrade.setBackgroundColor(Color.TRANSPARENT);
 				}
 				if (grade != "") {
 					exGrade.setText(grade);
 				} else {
 					if (passed == 0)
 						exGrade.setText("NB");
 					else
 						exGrade.setText("BE");
 				}
 			}
 
 			return v;
 		}
 
 		@Override
 		public void bindView(View v, Context context, Cursor c) {
 			// Log.d(TAG, "exAdapter bindView");
 			int nameCol = c.getColumnIndex(ExamsCol.EXAMNAME);
 			String name = c.getString(nameCol);
 			int rnCol = c.getColumnIndex(ExamsCol.EXAMNR);
 			String nr = c.getString(rnCol);
 			int attCol = c.getColumnIndex(ExamsCol.ATTEMPTS);
 			int att = c.getInt(attCol);
 			int gradeCol = c.getColumnIndex(ExamsCol.GRADE);
 			String grade = c.getString(gradeCol);
 			int semCol = c.getColumnIndex(ExamsCol.SEMESTER);
 			String sem = c.getString(semCol);
 			int passedCol = c.getColumnIndex(ExamsCol.PASSED);
 			int passed = c.getInt(passedCol);
 
 			/**
 			 * Next set the name of the entry.
 			 */
 			TextView exName = (TextView) v.findViewById(R.id.examName);
 			// if (exName != null) {
 			// exName.setText(name);
 			// }
 
 			TextView exNr = (TextView) v.findViewById(R.id.examNr);
 			// if (exNr != null) {
 			// exNr.setText(exNr);
 			// }
 			TextView exAtt = (TextView) v.findViewById(R.id.examAttempts);
 			TextView exGrade = (TextView) v.findViewById(R.id.examGrade);
 			TextView exSemester = (TextView) v.findViewById(R.id.examSemester);
 
 			if (exName != null) {
 				exName.setText(name);
 				if (isActualExam(sem) && mPreferences.getBoolean("highlightActualExamsPref", false)) {
 					exName.setShadowLayer(3, 0, 0, Color.GREEN);
 				} else {
 					exName.setShadowLayer(0, 0, 0, 0);
 				}
 			}
 			if (exNr != null) {
 				exNr.setText(nr);
 			}
 			if (exSemester != null) {
 				exSemester.setText(getApplicationContext().getString(R.string.grades_view_semester) + sem);
 			}
 			if (exAtt != null && att != 0) {
 				exAtt.setText(getApplicationContext().getString(R.string.grades_view_attempt) + att);
 			}
 			if (exGrade != null) {
 				if (passed == 0) { // wenn nicht bestanden
 					// FIXME wenn möglich.. farben gedöns is ziemlich tricky
 					// wegen "recycler" von ListActivity
 					if (att > 1) {
 						exGrade.setTextColor(Color.BLACK);
 						exGrade.setBackgroundColor(Color.RED);
 						exGrade.setCompoundDrawablePadding(2);
 
 					} else {
 						exGrade.setTextColor(Color.RED);
 						exGrade.setBackgroundColor(Color.TRANSPARENT);
 					}
 				} else { // wenn bestanden
 					exGrade.setTextColor(Color.rgb(0x87, 0xeb, 0x0c));
 					// exGrade.setTextColor(Color.GREEN);
 					exGrade.setBackgroundColor(Color.TRANSPARENT);
 				}
 
 				if (grade.length() != 0) {
 					// Log.d(TAG, "grade[" + grade + "]");
 					exGrade.setText(grade);
 				} else {
 					if (passed == 0) { // Wenn nicht bestanden
 						// Log.d(TAG, "grade NB [" + grade + "]");
 						exGrade.setText("NB");
 					} else { // Wenn bestanden
 						// Log.d(TAG, "grade BE [" + grade + "]");
 						exGrade.setText("BE");
 
 					}
 				}
 			}
 
 		}
 
 		public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
 			// Log.d(TAG, "runQueryOnBackgroundThread");
 			// Log.d(TAG, "rqbg constraint: " + constraint);
 
 			String sortOrder = BaseColumns._ID + " " + getDefaultListOrder();
 
 			if (getFilterQueryProvider() != null) {
 				return getFilterQueryProvider().runQuery(constraint);
 			}
 			// Log.d(TAG, "no FilterQueryProvider");
 			if (constraint.equals(SORT_ALL)) {
 				return getContentResolver().query(ExamsCol.CONTENT_URI, null, null, null, sortOrder);
 			} else if (constraint.equals(SORT_ALL_FAILED)) {
 
 				StringBuilder buffer = null;
 				String[] args = null;
 
 				buffer = new StringBuilder();
 				buffer.append("UPPER(");
 				buffer.append(ExamsCol.PASSED);
 				buffer.append(") LIKE ?");
 				args = new String[] { "0" };
 				// Log.d(TAG, "buffer: " + buffer.toString());
 				// Log.d(TAG, "args: " + args[0]);
 				return getContentResolver().query(ExamsCol.CONTENT_URI, null,
 						buffer == null ? null : buffer.toString(), args, sortOrder);
 			} else if (constraint.equals(SORT_ACTUAL)) {
 
 				StringBuilder buffer = null;
 				String[] args = null;
 
 				buffer = new StringBuilder();
 				buffer.append("UPPER(");
 				buffer.append(ExamsCol.SEMESTER);
 				buffer.append(") LIKE ?");
 
				args = new String[] { getLastExamSem() };
 				// Log.d(TAG, "buffer: " + buffer.toString());
 				// Log.d(TAG, "args: " + args[0]);
 				return getContentResolver().query(ExamsCol.CONTENT_URI, null,
 						buffer == null ? null : buffer.toString(), args, sortOrder);
 			} else if (constraint.equals(SORT_ACTUAL_FAILED)) {
 
 				StringBuilder buffer = null;
 				String[] args = null;
 
 				buffer = new StringBuilder();
 				buffer.append("UPPER(");
 				buffer.append(ExamsCol.SEMESTER);
 				buffer.append(") LIKE ? AND UPPER(");
 				buffer.append(ExamsCol.PASSED);
 				buffer.append(") LIKE ?");
				args = new String[] { getLastExamSem(), "0" };
 				// Log.d(TAG, "buffer: " + buffer.toString());
 				// Log.d(TAG, "args: " + args[0]);
 				return getContentResolver().query(ExamsCol.CONTENT_URI, null,
 						buffer == null ? null : buffer.toString(), args, sortOrder);
 			} else {
 				return null;
 			}
 		}
 	}
 
 	// /**
 	// *
 	// * @param nExam
 	// * @return
 	// */
 	// private boolean isActualExam(Exam nExam) {
 	// return nExam.getSemester().equals(getLastExamSem());
 	// }
 
 	/**
 	 * 
 	 * @param nExam
 	 * @return
 	 */
 	private boolean isActualExam(String sem) {
		return sem.equals(getLastExamSem());
 	}
 
 }
