 package nl.digitalica.skydivekompasroos;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.UUID;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
import android.util.TypedValue;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 public class CanopyTypeListActivity extends KompasroosBaseActivity {
 
 	public enum SortingEnum {
 		SORTBYNAME, SORTBYMANUFACTURER, SORTBYCATEGORY
 	}
 
 	public enum FilterEnum {
 		COMMONAROUNDMAX, ONLYCOMMON, ALL
 	}
 
 	List<CanopyType> canopyTypeList;
 	LinearLayout canopyTypeTable;
 
 	SortingEnum currentSortingMethod;
 	FilterEnum currentFilterType;
 
 	final static int SORT_DIALOG_ID = 1;
 	final static int FILTER_DIALOG_ID = 2;
 
 	final static int MILLISINDAY = 1000 * 60 * 60 * 24;
 
 	// static, so it can be statically referenced from onClick...
 	static StringBuilder skydiveKompasroosResultAccepted;
 	static StringBuilder skydiveKompasroosResultNeededSizeNotAvailable;
 	static StringBuilder skydiveKompasroosResultNotAccepted;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_canopytypelist);
 
 		canopyTypeTable = (LinearLayout) findViewById(R.id.tablelayout_canopytypelist);
 
 		canopyTypeList = CanopyType
 				.getAllCanopyTypesInList(CanopyTypeListActivity.this);
 
 		// if sorting and filter were save over 1 day ago, clear them
 		int sortingFilterTime = prefs.getInt(SETTING_SORTING_FILTER_DAYNR, 0);
 		int currentTime = (int) (System.currentTimeMillis() / MILLISINDAY);
 		if (currentTime - sortingFilterTime > 7) {
 			Editor e = prefs.edit();
 			e.remove(SETTING_SORTING);
 			e.remove(SETTING_FILTER_TYPE);
 			e.commit();
 		}
 
 		// get the saved sorting Method
 		int sortingMethodOrdinal = prefs.getInt(SETTING_SORTING,
 				SortingEnum.SORTBYNAME.ordinal());
 		this.currentSortingMethod = SortingEnum.values()[sortingMethodOrdinal];
 
 		// get the saved filter cat
 		int filterCatdOrdinal = prefs.getInt(SETTING_FILTER_TYPE,
 				FilterEnum.COMMONAROUNDMAX.ordinal());
 		this.currentFilterType = FilterEnum.values()[filterCatdOrdinal];
 
 		// TODO: store sorting so it is persistent (?)
 		fillCanopyTypeTable(canopyTypeTable, currentSortingMethod,
 				currentFilterType);
 
 		// add on click handler to share button
 		ImageButton shareResultButton = (ImageButton) findViewById(R.id.buttonShareResult);
 		shareResultButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				shareResult();
 			}
 
 		});
 
 		// add on click handler to filter header
 		View filterHeader = findViewById(R.id.tablelayout_filterheader);
 		filterHeader.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				showDialog(FILTER_DIALOG_ID);
 			}
 		});
 
 		// add on click handler to filter button
 		ImageButton filterButton = (ImageButton) findViewById(R.id.buttonEditFilter);
 		filterButton.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				showDialog(FILTER_DIALOG_ID);
 			}
 		});
 
 	}
 
 	/***
 	 * Fills the canopy type table on screen again based on the given sorting
 	 * method
 	 * 
 	 * @param canopyTypeTable
 	 * @param sortingMethod
 	 */
 	private void fillCanopyTypeTable(LinearLayout canopyTypeTable,
 			SortingEnum sortingMethod, FilterEnum filterType) {
 
 		// sort the canopyList on type, name, manufacturer
 		Comparator<CanopyType> canopyComparator = null;
 		switch (sortingMethod) {
 		case SORTBYNAME:
 			canopyComparator = new CanopyType.ComparatorByNameManufacturer();
 			break;
 		case SORTBYCATEGORY:
 			canopyComparator = new CanopyType.ComparatorByCategoryName();
 			break;
 		case SORTBYMANUFACTURER:
 			canopyComparator = new CanopyType.ComparatorByManufacturerCategoryName();
 			break;
 		}
 		Collections.sort(canopyTypeList, canopyComparator);
 
 		this.currentSortingMethod = sortingMethod;
 		savePreference(SETTING_SORTING, sortingMethod.ordinal());
 		this.currentFilterType = filterType;
 		savePreference(SETTING_FILTER_TYPE, filterType.ordinal());
 		savePreference(SETTING_SORTING_FILTER_DAYNR,
 				(int) (System.currentTimeMillis() / MILLISINDAY));
 		canopyTypeTable.removeAllViewsInLayout();
 		skydiveKompasroosResultAccepted = new StringBuilder();
 		skydiveKompasroosResultNeededSizeNotAvailable = new StringBuilder();
 		skydiveKompasroosResultNotAccepted = new StringBuilder();
 
 		UUID previousManufacturerId = UUID.randomUUID();
 		int previousCat = 9999;
 		int shownCount = 0;
 		int allCount = 0;
 		for (CanopyType theCanopyType : canopyTypeList) {
 			allCount++;
 			boolean showThisCanopyType = true;
 			// check cat filter
 			if (filterType == FilterEnum.ONLYCOMMON)
 				if (!theCanopyType.commontype)
 					showThisCanopyType = false;
 			if (filterType == FilterEnum.COMMONAROUNDMAX)
 				if (!theCanopyType.commontype
 						|| theCanopyType.category < currentMaxCategory - 1
 						|| theCanopyType.category > currentMaxCategory + 1)
 					showThisCanopyType = false;
 			// show the canopy type (and maybe headerline) if needed
 			if (showThisCanopyType) {
 				shownCount++;
 				if (sortingMethod == SortingEnum.SORTBYMANUFACTURER
 						&& !previousManufacturerId
 								.equals(theCanopyType.manufacturerId)) {
 					insertCanopyTypeHeaderRow(canopyTypeTable,
 							theCanopyType.manufacturerName);
 					previousManufacturerId = theCanopyType.manufacturerId;
 				}
 				if (sortingMethod == SortingEnum.SORTBYCATEGORY
 						&& previousCat != theCanopyType.category) {
 					insertCanopyTypeHeaderRow(canopyTypeTable, String.format(
 							getString(R.string.canopyListCategoryHeader),
 							theCanopyType.category));
 					previousCat = theCanopyType.category;
 				}
 				insertCanopyTypeRow(canopyTypeTable, theCanopyType,
 						currentMaxCategory, currentWeight);
 			}
 		}
 		String nl = System.getProperty("line.separator");
 
 		TextView tvFilterText = (TextView) findViewById(R.id.textview_filtersettings);
 		StringBuilder filterText = new StringBuilder();
 		if (allCount != shownCount)
 			filterText.append(String.format(getString(R.string.filterRange),
 					shownCount, allCount) + nl);
 		else
 			filterText.append(getResources().getString(R.string.filterNone)
 					+ nl);
 		switch (filterType) {
 		case ALL:
 			filterText.append(String.format(
 					getResources().getString(R.string.filterTypeAll), allCount)
 					+ nl);
 			break;
 		case COMMONAROUNDMAX:
 			filterText.append(String.format(
 					getResources().getString(R.string.filterTypeAround),
 					currentMaxCategory) + nl);
 			break;
 		case ONLYCOMMON:
 			filterText.append(getResources().getString(
 					R.string.filterTypeCommon)
 					+ nl);
 			break;
 		}
 		tvFilterText.setText(filterText.toString());
 	}
 
 	/***
 	 * Return the full string for the results to share. It has one block for the
 	 * acceptable canopies, and one for the not acceptable, as colored
 	 * backgrounds are not an option here...
 	 * 
 	 * @param c
 	 * @return
 	 */
 	static String skydiveKompasroosResult(Context c) {
 		String nl = System.getProperty("line.separator");
 		String nlnl = nl + nl;
 		StringBuilder skydiveKompasroosResult = new StringBuilder();
 		// now fill table with the list
 		String resultHeaderFormat = c
 				.getString(R.string.shareresultheaderformat);
 		String resultheader = String.format(resultHeaderFormat,
 				currentTotalJumps, currentJumpsLast12Months, currentWeight,
 				currentMaxCategory, currentMinArea);
 
 		skydiveKompasroosResult.append(resultheader);
 		skydiveKompasroosResult.append(nl);
 		skydiveKompasroosResult.append(skydiveKompasroosResultAccepted);
 		skydiveKompasroosResult.append(nlnl);
 		if (skydiveKompasroosResultNeededSizeNotAvailable.length() > 0) {
 			skydiveKompasroosResult.append(c
 					.getString(R.string.shareresultneededsizenotavailable));
 			skydiveKompasroosResult.append(nl);
 			skydiveKompasroosResult
 					.append(skydiveKompasroosResultNeededSizeNotAvailable);
 			skydiveKompasroosResult.append(nlnl);
 		}
 		if (skydiveKompasroosResultNotAccepted.length() > 0) {
 			skydiveKompasroosResult.append(c
 					.getString(R.string.shareresultnotaccepted));
 			skydiveKompasroosResult.append(nl);
 			skydiveKompasroosResult.append(skydiveKompasroosResultNotAccepted);
 			skydiveKompasroosResult.append(nlnl);
 		}
 		skydiveKompasroosResult.append(c.getString(R.string.shareresultfooter));
 
 		return skydiveKompasroosResult.toString();
 	}
 
 	private void shareResult() {
 		Intent sendIntent = new Intent();
 		sendIntent.setAction(Intent.ACTION_SEND);
 		sendIntent.putExtra(Intent.EXTRA_TEXT,
 				CanopyTypeListActivity.skydiveKompasroosResult(this));
 		sendIntent.putExtra(Intent.EXTRA_SUBJECT,
 				getString(R.string.shareresultsubject));
 		sendIntent.putExtra(Intent.EXTRA_TITLE,
 				getString(R.string.shareresultsubject));
 		sendIntent.setType("text/plain");
 		startActivity(sendIntent);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_canopylist, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_shareResult:
 			shareResult();
 			return true;
 		case R.id.menu_sort:
 			showDialog(SORT_DIALOG_ID);
 			return true;
 		case R.id.menu_filter:
 			showDialog(FILTER_DIALOG_ID);
 			return true;
 		case R.id.menu_about:
 			startActivity(new Intent(this, AboutActivity.class));
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	/***
 	 * Generic click handle to for clicks on a canopy type row the tag is used
 	 * to decide what canopy details to show.
 	 * 
 	 * @param v
 	 */
 	private void onCanopyTypeRowClick(View v) {
 		String canopyKey = v.getTag().toString();
 		Intent i = new Intent(getBaseContext(), CanopyDetailsActivity.class);
 		// TODO: remove extras as they will be in global vars...
 		i.putExtra(CANOPYIDEXTRA, canopyKey);
 		startActivity(i);
 	}
 
 	private void insertCanopyTypeHeaderRow(LinearLayout canopyTable,
 			String header) {
 		String nl = System.getProperty("line.separator");
 		TextView canopyListHeader = new TextView(CanopyTypeListActivity.this);
 		canopyListHeader.setText(nl + header);
 		canopyListHeader.setTextSize(getResources().getDimension(
 				R.dimen.bodyText));
 
 		// create row, and add row to table
 		TableRow row = new TableRow(this);
 		row.addView(canopyListHeader);
 		canopyTable.addView(row);
 	}
 
 	/***
 	 * Add a canopy row to the canopy table
 	 * 
 	 * @param canopyTypeTable
 	 * @param theCanopyType
 	 * @param maxCategory
 	 */
 	private void insertCanopyTypeRow(LinearLayout canopyTypeTable,
 			CanopyType theCanopyType, int maxCategory, int exitWeightInKg) {
 
 		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
		View canopyTypeListRow = inflater.inflate(
				R.layout.canopy_type_row_layout, null);
 
		// Drawable box = getResources().getDrawable(R.drawable.box);
 		LinearLayout hLayout = (LinearLayout) canopyTypeListRow
 				.findViewById(R.id.linearLayoutCanopyListRow);
 		hLayout.setTag(theCanopyType.id.toString());
 		if (!theCanopyType.isSpecialCatchAllCanopy)
 			hLayout.setOnClickListener(new View.OnClickListener() {
 				public void onClick(View v) {
 					onCanopyTypeRowClick(v);
 				}
 			});
 
 		hLayout.setBackgroundDrawable(backgroundDrawableForAcceptance(theCanopyType
 				.acceptablility(maxCategory, exitWeightInKg)));
 
 		// hLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
 		// LayoutParams.WRAP_CONTENT));
 
 		TextView tvCategory = (TextView) canopyTypeListRow
 				.findViewById(R.id.textViewCanopyListRowCategory);
 		// tvCategory.setBackgroundDrawable(box);
 		tvCategory.setText(Integer.toString(theCanopyType.category));
 
 		TextView tvCanopyName = (TextView) canopyTypeListRow
 				.findViewById(R.id.textViewCanopyListRowName);
 		// tvCanopyName.setBackgroundDrawable(box);
 		tvCanopyName.setText(theCanopyType.name);
 
 		// TODO: add more details like cells and remarks here
 		TextView tvCanopyDetails = (TextView) canopyTypeListRow
 				.findViewById(R.id.textViewCanopyListRowDetails);
 		if (this.currentSortingMethod != SortingEnum.SORTBYMANUFACTURER)
 			tvCanopyDetails.setText(theCanopyType.manufacturerName);
 		else
 			tvCanopyDetails.setText(theCanopyType
 					.alternativeDetailsText(CanopyTypeListActivity.this));
 		// tvCanopyDetails.setBackgroundDrawable(box);
 
 		// if the link won't work, because this is the catch all,
 		// don't show the arrow.
 		if (theCanopyType.isSpecialCatchAllCanopy) {
 			TextView tvArrowRight = (TextView) canopyTypeListRow
 					.findViewById(R.id.textViewArrowRight);
 			tvArrowRight.setText("");
 		}
 
 		// create text view and row
 		TableRow row = new TableRow(this);
 		row.addView(canopyTypeListRow);
 
 		// add row to table
 		canopyTypeTable.addView(row);
 
 		// add row to text for results sharing
 		// TODO: in case of current sorting by manufacturer, show that first.
 		String shareResultLine = theCanopyType.name + " - "
 				+ theCanopyType.manufacturerName
 				+ System.getProperty("line.separator");
 
 		switch (theCanopyType.acceptablility(currentMaxCategory, currentWeight)) {
 		case ACCEPTABLE:
 			skydiveKompasroosResultAccepted.append(shareResultLine);
 			break;
 		case NEEDEDSIZENOTAVAILABLE:
 			skydiveKompasroosResultNeededSizeNotAvailable
 					.append(shareResultLine);
 			break;
 		case CATEGORYTOOHIGH:
 			skydiveKompasroosResultNotAccepted.append(shareResultLine);
 			break;
 		}
 
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		switch (id) {
 		case SORT_DIALOG_ID:
 			return sortDialog();
 
 		case FILTER_DIALOG_ID:
 			return filterDialog();
 
 		}
 		return null;
 	}
 
 	@Override
 	protected void onPrepareDialog(final int id, final Dialog dialog) {
 		switch (id) {
 		case SORT_DIALOG_ID:
 		case FILTER_DIALOG_ID:
 			AlertDialog alert = (AlertDialog) dialog;
 			Button cancel = alert.getButton(AlertDialog.BUTTON_NEGATIVE);
 			if (cancel != null)
				cancel.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources()
 						.getDimension(R.dimen.bodyText));
 			Button ok = alert.getButton(AlertDialog.BUTTON_POSITIVE);
 			if (ok != null)
				ok.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources()
						.getDimension(R.dimen.bodyText));
 
 		}
 	}

 	private Dialog sortDialog() {
 		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		View layout = inflater.inflate(R.layout.sort_dialog,
 				(ViewGroup) findViewById(R.id.root));
 
 		// check the correct radio button in group
 		RadioButton radioToCheck = null;
 		switch (currentSortingMethod) {
 		case SORTBYNAME:
 			radioToCheck = (RadioButton) layout
 					.findViewById(R.id.radioButtonByName);
 			break;
 		case SORTBYCATEGORY:
 			radioToCheck = (RadioButton) layout
 					.findViewById(R.id.radioButtonByCategory);
 			break;
 		case SORTBYMANUFACTURER:
 			radioToCheck = (RadioButton) layout
 					.findViewById(R.id.radioButtonByManufacturer);
 			break;
 
 		}
 		if (radioToCheck != null)
 			radioToCheck.setChecked(true);
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setView(layout);
 		builder.setNegativeButton(android.R.string.cancel,
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int whichButton) {
 						// We forcefully dismiss and remove the Dialog, so it
 						// cannot be used again (no cached info)
 						CanopyTypeListActivity.this
 								.removeDialog(SORT_DIALOG_ID);
 					}
 				});
 
 		RadioGroup rg = (RadioGroup) layout
 				.findViewById(R.id.radioGroupFilterOptions);
 		rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
 
 			public void onCheckedChanged(RadioGroup group, int checkedId) {
 				int checkButtonId = group.getCheckedRadioButtonId();
 				switch (checkButtonId) {
 				case R.id.radioButtonByName:
 					currentSortingMethod = SortingEnum.SORTBYNAME;
 					break;
 				case R.id.radioButtonByCategory:
 					currentSortingMethod = SortingEnum.SORTBYCATEGORY;
 					break;
 				case R.id.radioButtonByManufacturer:
 					currentSortingMethod = SortingEnum.SORTBYMANUFACTURER;
 					break;
 				}
 				fillCanopyTypeTable(canopyTypeTable, currentSortingMethod,
 						currentFilterType);
 				CanopyTypeListActivity.this.removeDialog(SORT_DIALOG_ID);
 			}
 		});
 
 		return builder.create();
 	}
 
 	private Dialog filterDialog() {
 		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		View layout = inflater.inflate(R.layout.filter_dialog,
 				(ViewGroup) findViewById(R.id.root));
 
 		// check the correct radio button in group
 		RadioButton radioToCheck = null;
 		switch (currentFilterType) {
 		case ONLYCOMMON:
 			radioToCheck = (RadioButton) layout
 					.findViewById(R.id.radioButtonCommon);
 			break;
 		case COMMONAROUNDMAX:
 			radioToCheck = (RadioButton) layout
 					.findViewById(R.id.radioButtonCommonAround);
 			break;
 		case ALL:
 			radioToCheck = (RadioButton) layout
 					.findViewById(R.id.radioButtonAll);
 			break;
 
 		}
 		if (radioToCheck != null)
 			radioToCheck.setChecked(true);
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setView(layout);
 		builder.setNegativeButton(android.R.string.cancel,
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int whichButton) {
 						// We forcefully dismiss and remove the Dialog, so it
 						// cannot be used again (no cached info)
 						CanopyTypeListActivity.this
 								.removeDialog(SORT_DIALOG_ID);
 					}
 				});
 
 		RadioGroup rg = (RadioGroup) layout
 				.findViewById(R.id.radioGroupFilterOptions);
 		rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
 
 			public void onCheckedChanged(RadioGroup group, int checkedId) {
 				int checkButtonId = group.getCheckedRadioButtonId();
 				switch (checkButtonId) {
 				case R.id.radioButtonCommon:
 					currentFilterType = FilterEnum.ONLYCOMMON;
 					break;
 				case R.id.radioButtonCommonAround:
 					currentFilterType = FilterEnum.COMMONAROUNDMAX;
 					break;
 				case R.id.radioButtonAll:
 					currentFilterType = FilterEnum.ALL;
 					break;
 				}
 				fillCanopyTypeTable(canopyTypeTable, currentSortingMethod,
 						currentFilterType);
 				CanopyTypeListActivity.this.removeDialog(FILTER_DIALOG_ID);
 			}
 		});
 
 		return builder.create();
 	}
 
 }
