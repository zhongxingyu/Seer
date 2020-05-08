 package net.tailriver.agoraguide;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.DialogInterface.OnMultiChoiceClickListener;
 import android.content.Intent;
import android.graphics.Color;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.FragmentManager;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnKeyListener;
 import android.view.WindowManager;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 
 public class SearchActivity extends AgoraActivity implements OnItemClickListener {
 	public enum SearchType {
 		Keyword, Schedule, Area, Favorite;
 	}
 
 	private DialogFragment dialog;
 	private ListView resultView;
 	private SearchHelper helper;
 	private SearchType type;
 
 	@Override
 	public void onPreInitialize() {
 		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
 		setContentView(R.layout.search);
 
 		resultView = (ListView) findViewById(R.id.searchResult);
 		resultView.setOnItemClickListener(this);
 		resultView.setEmptyView(findViewById(R.id.searchNotFound));
		resultView.setBackgroundColor(Color.argb(180, 255, 255, 255));
 
 		type = (SearchType) getIntent().getSerializableExtra(IntentExtra.SEARCH_TYPE);
 		if (type != SearchType.Keyword) {
 			findViewById(R.id.searchKeyword).setVisibility(View.GONE);
 		}
 		if (type != SearchType.Schedule) {
 			findViewById(R.id.searchSchedule).setVisibility(View.GONE);
 		}
 	}
 
 	@Override
 	public void onPostInitialize(Bundle savedInstanceState) {
 		switch (type) {
 		case Keyword:
 			setTitle(R.string.searchKeyword);
 			helper = new KeywordSearchHelper(this);
 			dialog = new CategoryDialogFrag();
 			resultView.setAdapter(new SearchResultAdapter(this));
 			break;
 
 		case Schedule:
 			setTitle(R.string.searchSchedule);
 			helper = new ScheduleSearchHelper(this);
 			resultView.setAdapter(new TimeFrameAdapter(this));
 			break;
 
 		case Area:
 			setTitle(R.string.searchArea);
 			helper = new AreaSearchHelper(this);
 			resultView.setAdapter(new SearchResultAdapter(this));
 			break;
 
 		case Favorite:
 			setTitle(R.string.favorite);
 			helper = new FavoriteSearchHelper(this);
 			dialog = new FavoriteClearDialogFrag();
 			resultView.setAdapter(new SearchResultAdapter(this));
 			break;
 		}
 		if (savedInstanceState != null) {
 			helper.onRestoreInstanceState(savedInstanceState);
 		}
 		helper.search();
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		helper.onSaveInstanceState(outState);
 	}
 
 	public void onItemClick(AdapterView<?> parent, View view, int position,
 			long id) {
 		Object item = parent.getItemAtPosition(position);
 		EntrySummary summary = item instanceof EntrySummary ?
 				(EntrySummary) item : ((TimeFrame) item).getSummary();
 		Intent intent = new Intent(getApplicationContext(), ProgramActivity.class);
 		intent.putExtra(IntentExtra.ENTRY_ID, summary.getId());
 		startActivityForResult(intent, 0);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (type == SearchType.Keyword) {
 			resultView.requestFocusFromTouch();	// it is need when DPAD operation and back			
 		}
 
 		if (type == SearchType.Favorite && resultView.getCount() != Favorite.values().size()) {
 			helper.search();
 		}
 
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.search, menu);
 		switch (type) {
 		case Keyword:
 			menu.findItem(R.id.searchCategoryFiltering).setVisible(true);
 			return true;
 		case Favorite:
 			menu.findItem(R.id.favoriteClear).setVisible(true);
 			menu.findItem(R.id.favoriteClear).setEnabled( resultView.getCount() != 0 );
 			return true;
 		default:
 			return false;
 		}
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (dialog != null) {
 			FragmentManager manager = getSupportFragmentManager();
 			dialog.show(manager, "dialog");
 			return true;
 		}
 		return super.onOptionsItemSelected(item);			
 	}
 
 	public final SearchHelper getHelper() {
 		return helper;
 	}
 
 	public final ListView getResultView() {
 		return resultView;
 	}
 }
 
 interface SearchHelper {
 	public void onRestoreInstanceState(Bundle savedInstance);
 	public void onSaveInstanceState(Bundle outState);
 	public void search();
 }
 
 final class KeywordSearchHelper
 implements SearchHelper, OnKeyListener, OnScrollListener, TextWatcher
 {
 	private ListView resultView;
 	private EditText searchText;
 	boolean[] categoryChecked;
 
 	public KeywordSearchHelper(SearchActivity activity) {
 		resultView = activity.getResultView();
 		resultView.setOnScrollListener(this);
 		searchText = (EditText) activity.findViewById(R.id.searchKeyword);
 		searchText.addTextChangedListener(this);
 		searchText.setOnKeyListener(this);
 		categoryChecked = new boolean[Category.values().size()];
 		Arrays.fill(categoryChecked, true);
 	}
 
 	public void onRestoreInstanceState(Bundle savedInstanceState) {
 		searchText.setText(savedInstanceState.getString("keyword"));
 		categoryChecked = savedInstanceState.getBooleanArray("categoryChecked");
 	}
 
 	public void onSaveInstanceState(Bundle outState) {
 		outState.putString("keyword", searchText.getText().toString());
 		outState.putBooleanArray("categoryChecked", categoryChecked);
 	}
 
 	public void search() {
 		String s = searchText.getText().toString();
 		Collection<Category> categoryFilter = new HashSet<Category>();
 		List<Category> list = Category.values();
 		for (int i = 0, max = list.size(); i < max; i++) {
 			if (categoryChecked[i]) {
 				categoryFilter.add(list.get(i));
 			}
 		}
 		SearchResultAdapter adapter = (SearchResultAdapter) resultView.getAdapter();
 		adapter.filter(categoryFilter, s.toString());
 	}
 
 	public void beforeTextChanged(CharSequence s, int start, int count,
 			int after) {
 	}
 
 	public void onTextChanged(CharSequence s, int start, int before,
 			int count) {
 	}
 
 	public void afterTextChanged(Editable s) {
 		search();
 	}
 
 	public void onScrollStateChanged(AbsListView view, int scrollState) {
 		closeSoftKeyboard(searchText);
 	}
 
 	public void onScroll(AbsListView view, int firstVisibleItem,
 			int visibleItemCount, int totalItemCount) {
 	}
 
 	public boolean onKey(View v, int keyCode, KeyEvent event) {
 		if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
 			closeSoftKeyboard(v);
 			return true;
 		}
 		return false;
 	}
 
 	private void closeSoftKeyboard(View v) {
 		Context context = v.getContext();
 		InputMethodManager imm =
 				(InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
 		if (imm != null && v != null) {
 			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
 		}
 	}
 }
 
 final class ScheduleSearchHelper implements SearchHelper, OnItemSelectedListener {
 	private ListView resultView;
 	private Spinner daySpinner;
 	private Spinner timeSpinner;
 
 	public ScheduleSearchHelper(SearchActivity activity) {
 		resultView = activity.getResultView();
 
 		ArrayAdapter<CharSequence> dayAdapter  = ArrayAdapter.createFromResource(activity,
 				R.array.days_locale, android.R.layout.simple_spinner_item);
 		ArrayAdapter<CharSequence> timeAdapter = ArrayAdapter.createFromResource(activity,
 				R.array.searchScheduleTimes, android.R.layout.simple_spinner_item);
 
 		dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 
 		daySpinner  = (Spinner) activity.findViewById(R.id.searchDay);
 		timeSpinner = (Spinner) activity.findViewById(R.id.searchTime);
 		daySpinner.setAdapter(dayAdapter);
 		timeSpinner.setAdapter(timeAdapter);
 		daySpinner.setOnItemSelectedListener(this);
 		timeSpinner.setOnItemSelectedListener(this);
 	}
 
 	public void onRestoreInstanceState(Bundle savedInstanceState) {
 		daySpinner.setSelection(savedInstanceState.getInt("day"));
 		timeSpinner.setSelection(savedInstanceState.getInt("time"));
 	}
 
 	public void onSaveInstanceState(Bundle outState) {
 		outState.putInt("day",  daySpinner.getSelectedItemPosition());
 		outState.putInt("time", timeSpinner.getSelectedItemPosition());
 	}
 
 	public void search() {
 		Day day = Day.values().get(daySpinner.getSelectedItemPosition());
 		int time = Integer.parseInt(((String) timeSpinner.getSelectedItem()).replace(":", ""));
 		int viewPosition = - Collections.binarySearch(
 				TimeFrame.values(), TimeFrame.makePivot(day, time)) - 1;
 		resultView.setSelection(viewPosition);
 	}
 
 	public void onItemSelected(AdapterView<?> parent, View view,
 			int position, long id) {
 		search();
 	}
 
 	public void onNothingSelected(AdapterView<?> parent) {
 	}
 }
 
 final class AreaSearchHelper implements SearchHelper {
 	private Area area;
 	private ListView resultView;
 
 	public AreaSearchHelper(SearchActivity activity) {
 		area = Area.get(activity.getIntent().getStringExtra(IntentExtra.AREA_ID));
 		resultView = activity.getResultView();
 	}
 
 	public void onRestoreInstanceState(Bundle savedInstance) {
 	}
 
 	public void onSaveInstanceState(Bundle outState) {
 	}
 
 	public void search() {
 		SearchResultAdapter adapter = (SearchResultAdapter) resultView.getAdapter();
 		adapter.filter(Collections.singleton(area));
 	}
 }
 
 final class FavoriteSearchHelper implements SearchHelper {
 	private ListView resultView;
 
 	public FavoriteSearchHelper(SearchActivity activity) {
 		resultView = activity.getResultView();
 	}
 
 	public void onRestoreInstanceState(Bundle savedInstance) {
 	}
 
 	public void onSaveInstanceState(Bundle outState) {
 	}
 
 	public void search() {
 		SearchResultAdapter adapter = (SearchResultAdapter) resultView.getAdapter();
 		adapter.filter(Favorite.values());
 	}
 }
 
 final class CategoryDialogFrag extends DialogFragment
 implements OnClickListener, OnMultiChoiceClickListener {
 	private KeywordSearchHelper helper;
 
 	@Override
 	public Dialog onCreateDialog(Bundle savedInstanceState) {
 		List<Category> list = Category.values();
 		Category[] category = list.toArray(new Category[list.size()]);
 		helper = (KeywordSearchHelper) ((SearchActivity) getActivity()).getHelper();
 		String[] categoryName = new String[category.length];
 		for (int i = 0; i < category.length; i++) {
 			String allday = Hint.get("$", category[i].isAllday() ? "allday" : "not_allday");
 			categoryName[i] = allday + " " + category[i].toString();
 		}
 		AlertDialog ad = new AlertDialog.Builder(getActivity())
 		.setTitle(R.string.searchCategoryFiltering)
 		.setIcon(android.R.drawable.ic_search_category_default)
 		.setMultiChoiceItems(categoryName, helper.categoryChecked, this)
 		.setPositiveButton(android.R.string.ok, this)
 		.setNeutralButton(R.string.selectAll, this)
 		.create();
 		ad.show();
 		return ad;
 	}
 
 	public void onClick(DialogInterface dialog, int which,
 			boolean isChecked) {
 		helper.categoryChecked[which] = isChecked;
 	}
 
 	public void onClick(DialogInterface dialog, int which) {
 		if (which == AlertDialog.BUTTON_NEUTRAL) {
 			Arrays.fill(helper.categoryChecked, true);
 		}
 		onDismiss(dialog);
 		helper.search();
 	}
 }
 
 final class FavoriteClearDialogFrag extends DialogFragment
 implements OnClickListener {
 	private FavoriteSearchHelper helper;
 
 	@Override
 	public Dialog onCreateDialog(Bundle savedInstanceState) {
 		helper = (FavoriteSearchHelper) ((SearchActivity) getActivity()).getHelper();
 		AlertDialog ad = new AlertDialog.Builder(getActivity())
 		.setTitle(R.string.favoriteClear)
 		.setIcon(android.R.drawable.ic_menu_delete)
 		.setPositiveButton(android.R.string.ok, this)
 		.setNegativeButton(android.R.string.cancel, null)
 		.create();
 		ad.show();
 		return ad;
 	}
 
 	public void onClick(DialogInterface dialog, int which) {
 		Favorite.clear();
 		onDismiss(dialog);
 		helper.search();
 		getActivity().supportInvalidateOptionsMenu();
 	}
 }
