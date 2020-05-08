 package nl.digitalica.skydivekompasroos;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 
 /**
  * @author robbert
  * 
  */
 public class CalculateActivity extends KompasroosBaseActivity {
 
 	// min & max weight in kg
 	final static int WEIGHT_MIN = 50;
 	final static int WEIGHT_MAX = 140;
 	final static int WEIGHT_DEFAULT = 100;
 
 	// max for total jumps
 	final static int TOTALJUMPS_MAX = 1100;
 	final static int TOTALJUMPS_LASTGROUP = 1000;
 	final static int TOTALJUMPS_DEFAULT = 100;
 
 	// max for total jumps
 	final static int JUMPS_LAST_12_MONTHS_MAX = 125;
 	final static int JUMPS_LAST_12_MONTHS_LASTGROUP = 100;
 	final static int JUMPS_LAST_12_MONTHS_DEFAULT = 25;
 
 	// wingload table
 	final static int WINGLOAD_FIRST_COL = 130;
 	final static int WINGLOAD_STEP = 20;
 
 	// dialog ID's
 	final static int SAVE_DIALOG_ID = 1;
 	final static int RESET_DIALOG_ID = 2;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_calculate);
 		prefs = getSharedPreferences(KOMPASROOSPREFS, Context.MODE_PRIVATE);
 
 		// if compile date over 1 year ago, show warning text
 		TextView tvWarning = (TextView) findViewById(R.id.textViewWarning);
 		String warning = "";
 		try {
 			long time = getCompileDateTime();
 			Calendar cal = Calendar.getInstance();
 			long now = cal.getTime().getTime();
 			long maxDiff = 1000 * 60 * 60 * 24 * 365; // 1 year
 			// maxDiff = 1000 * 60 * 5; // 5 mins (for testing)
 			if (now - time > maxDiff)
 				warning = getString(R.string.calculationOvertimeWarning);
 		} catch (Exception e) {
 			warning = getString(R.string.calculationOvertimeUnknownWarning);
 		}
 		tvWarning.setText(warning);
 
 		// initialize seek bars and calculated texts
 		initSeekBars();
 		calculate();
 
 		// set click listener for canopy list button
 		Button canopyListButton = (Button) findViewById(R.id.buttonShowCanopyList);
 		canopyListButton.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				Intent i = new Intent(getBaseContext(),
 						CanopyListActivity.class);
 				startActivity(i);
 			}
 		});
 
 		// Just for testing the canopy list
 		// canopyListButton.performClick();
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		switch (id) {
 		case SAVE_DIALOG_ID:
 			return saveDialog();
 		case RESET_DIALOG_ID:
 			return resetDialog();
 
 		}
 		return null;
 	}
 
 	private Dialog saveDialog() {
 		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		View layout = inflater.inflate(R.layout.save_dialog,
 				(ViewGroup) findViewById(R.id.root));
 		Button asFriend = (Button) layout.findViewById(R.id.buttonFriend);
 		Button asOwn = (Button) layout.findViewById(R.id.buttonOwn);
 		// TODO: add onclick handlers to buttons
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setView(layout);
 		builder.setNegativeButton(android.R.string.cancel,
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int whichButton) {
 						// We forcefully dismiss and remove the Dialog, so it
 						// cannot be used again (no cached info)
 						CalculateActivity.this.removeDialog(SAVE_DIALOG_ID);
 					}
 				});
 		asFriend.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				prefs = getSharedPreferences(KOMPASROOSPREFS,
 						Context.MODE_PRIVATE);
 				Editor e = prefs.edit();
 				e.putInt(SETTING_FRIEND_TOTAL_JUMPS, currentTotalJumps);
 				e.putInt(SETTING_FRIEND_LAST_12_MONTHS,
 						currentJumpsLast12Months);
 				e.putInt(SETTING_FRIEND_WEIGHT, currentWeight);
 				e.commit();
 				CalculateActivity.this.removeDialog(SAVE_DIALOG_ID);
 			}
 		});
 		asOwn.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				prefs = getSharedPreferences(KOMPASROOSPREFS,
 						Context.MODE_PRIVATE);
 				Editor e = prefs.edit();
 				e.putInt(SETTING_OWN_TOTAL_JUMPS, currentTotalJumps);
 				e.putInt(SETTING_OWN_LAST_12_MONTHS, currentJumpsLast12Months);
 				e.putInt(SETTING_OWN_WEIGHT, currentWeight);
 				e.commit();
 				CalculateActivity.this.removeDialog(SAVE_DIALOG_ID);
 			}
 		});
 		return builder.create();
 	}
 
 	private Dialog resetDialog() {
 		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		View layout = inflater.inflate(R.layout.reset_dialog,
 				(ViewGroup) findViewById(R.id.root));
 		Button asBeginner = (Button) layout.findViewById(R.id.buttonBeginner);
 		Button asIntermediate = (Button) layout
 				.findViewById(R.id.buttonIntermediate);
 		Button asSkyGod = (Button) layout.findViewById(R.id.buttonSkyGod);
 		Button asFriend = (Button) layout.findViewById(R.id.buttonFriend);
 		Button asOwn = (Button) layout.findViewById(R.id.buttonOwn);
 		// TODO: add onclick handlers to buttons
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setView(layout);
 		builder.setNegativeButton(android.R.string.cancel,
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int whichButton) {
 						// We forcefully dismiss and remove the Dialog, so it
 						// cannot be used again (no cached info)
 						CalculateActivity.this.removeDialog(RESET_DIALOG_ID);
 					}
 				});
 		asBeginner.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				setSeekBars(WEIGHT_DEFAULT - WEIGHT_MIN, 5, 5);
 				CalculateActivity.this.removeDialog(RESET_DIALOG_ID);
 			}
 		});
 		asIntermediate.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				setSeekBars(WEIGHT_DEFAULT - WEIGHT_MIN, TOTALJUMPS_DEFAULT,
 						JUMPS_LAST_12_MONTHS_DEFAULT);
 				CalculateActivity.this.removeDialog(RESET_DIALOG_ID);
 			}
 		});
 		asSkyGod.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				setSeekBars(WEIGHT_DEFAULT - WEIGHT_MIN, 1200, 200);
 				CalculateActivity.this.removeDialog(RESET_DIALOG_ID);
 			}
 
 		});
 		asFriend.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				prefs = getSharedPreferences(KOMPASROOSPREFS,
 						Context.MODE_PRIVATE);
 				int weight = prefs
 						.getInt(SETTING_FRIEND_WEIGHT, WEIGHT_DEFAULT);
 				int totalJumps = prefs.getInt(SETTING_FRIEND_TOTAL_JUMPS,
 						TOTALJUMPS_DEFAULT);
 				int jumpsLastMonth = prefs.getInt(
 						SETTING_FRIEND_LAST_12_MONTHS,
 						JUMPS_LAST_12_MONTHS_DEFAULT);
 				setSeekBars(weight - WEIGHT_MIN, totalJumps, jumpsLastMonth);
 				CalculateActivity.this.removeDialog(RESET_DIALOG_ID);
 			}
 		});
 		asOwn.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				prefs = getSharedPreferences(KOMPASROOSPREFS,
 						Context.MODE_PRIVATE);
 				int weight = prefs.getInt(SETTING_OWN_WEIGHT, WEIGHT_DEFAULT);
 				int totalJumps = prefs.getInt(SETTING_OWN_TOTAL_JUMPS,
 						TOTALJUMPS_DEFAULT);
 				int jumpsLastMonth = prefs.getInt(SETTING_OWN_LAST_12_MONTHS,
 						JUMPS_LAST_12_MONTHS_DEFAULT);
 				setSeekBars(weight - WEIGHT_MIN, totalJumps, jumpsLastMonth);
 				CalculateActivity.this.removeDialog(RESET_DIALOG_ID);
 			}
 		});
 		return builder.create();
 	}
 
 	private void setSeekBars(int weight, int totalJumps, int jumpsLast12Months) {
 		((SeekBar) findViewById(R.id.seekBarWeight)).setProgress(weight);
 		((SeekBar) findViewById(R.id.seekBarTotalJumps))
 				.setProgress(totalJumps);
 		((SeekBar) findViewById(R.id.seekBarJumpsLast12Months))
 				.setProgress(jumpsLast12Months);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_calculate, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		// case R.id.menu_resetbeginner:
 		//
 		// return true;
 		// case R.id.menu_resetnormal:
 		// return true;
 		// case R.id.menu_resetexpert:
 		// return true;
 		case R.id.menu_reset:
 			showDialog(RESET_DIALOG_ID);
 			return true;
 		case R.id.menu_save:
 			showDialog(SAVE_DIALOG_ID);
 			return true;
 		case R.id.menu_about:
 			startActivity(new Intent(this, AboutActivity.class));
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	private void initSeekBars() {
 		// weight seek bar
 		SeekBar sbWeight = (SeekBar) findViewById(R.id.seekBarWeight);
 		int weightInKg = prefs.getInt(SETTING_WEIGHT, WEIGHT_DEFAULT);
 		sbWeight.setMax(WEIGHT_MAX - WEIGHT_MIN);
 		sbWeight.setOnSeekBarChangeListener(seekBarChangeListenerWeight);
 		setPlusMinButtonListeners(sbWeight, R.id.buttonWeightMin,
 				R.id.buttonWeightPlus);
 		sbWeight.setProgress(weightInKg - WEIGHT_MIN);
 		setWeightSettingText(weightInKg); // TODO: not needed?
 
 		// total jumps seek bar
 		SeekBar sbTotalJumps = (SeekBar) findViewById(R.id.seekBarTotalJumps);
 		int totalJumps = prefs.getInt(SETTING_TOTAL_JUMPS, TOTALJUMPS_DEFAULT);
 		sbTotalJumps.setMax(TOTALJUMPS_MAX);
 		sbTotalJumps
 				.setOnSeekBarChangeListener(seekBarChangeListenerTotalJumps);
 		setPlusMinButtonListeners(sbWeight, R.id.buttonTotalJumpsMin,
 				R.id.buttonTotalJumpsPlus);
 		sbTotalJumps.setProgress(totalJumps);
 		setTotalJumpsSettingText(totalJumps); // TODO: not needed?
 
 		// jumps last 12 months seek bar
 		SeekBar sbJumpsLast12Months = (SeekBar) findViewById(R.id.seekBarJumpsLast12Months);
 		int jumpsLast12Months = prefs.getInt(SETTING_JUMPS_LAST_12_MONTHS,
 				JUMPS_LAST_12_MONTHS_DEFAULT);
 		sbJumpsLast12Months.setMax(JUMPS_LAST_12_MONTHS_MAX);
 		sbJumpsLast12Months
 				.setOnSeekBarChangeListener(seekBarChangeListenerJumpsLast12Months);
 		setPlusMinButtonListeners(sbWeight, R.id.buttonJumpLast12MonthsMin,
 				R.id.buttonJumpLast12MonthsPlus);
 		sbJumpsLast12Months.setProgress(jumpsLast12Months);
 		setJumpsLast12MonthsSettingText(jumpsLast12Months); // TODO: not needed?
 
 	}
 
 	private void setPlusMinButtonListeners(SeekBar sb, int minButtonId,
 			int plusButtonId) {
 		Button minButton = (Button) findViewById(minButtonId);
 		Button plusButton = (Button) findViewById(plusButtonId);
 		minButton.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				// find matching seekbar
 				ViewGroup hList = (ViewGroup) v.getParent();
 				if (hList.getChildCount() != 3)
 					Log.e(LOG_TAG,
 							"Incorrect number of children in seekbargroup");
 				SeekBar sb = (SeekBar) hList.getChildAt(1);
 				int progress = sb.getProgress();
 				if (progress > 0)
 					sb.setProgress(progress - 1);
 			}
 		});
 		plusButton.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				// find matching seekbar
 				ViewGroup hList = (ViewGroup) v.getParent();
 				if (hList.getChildCount() != 3)
 					Log.e(LOG_TAG,
 							"Incorrect number of children in seekbargroup");
 				SeekBar sb = (SeekBar) hList.getChildAt(1);
 				int progress = sb.getProgress();
 				if (progress < sb.getMax())
 					sb.setProgress(progress + 1);
 			}
 		});
 
 	}
 
 	private OnSeekBarChangeListener seekBarChangeListenerWeight = new OnSeekBarChangeListener() {
 
 		public void onProgressChanged(SeekBar seekBar, int progress,
 				boolean fromUser) {
 			int weightInKg = progress + WEIGHT_MIN;
 			savePreference(SETTING_WEIGHT, weightInKg);
 			currentWeight = weightInKg;
 			setWeightSettingText(weightInKg);
 			calculate();
 		}
 
 		public void onStartTrackingTouch(SeekBar seekBar) {
 			// TODO Auto-generated method stub
 
 		}
 
 		public void onStopTrackingTouch(SeekBar seekBar) {
 			// TODO Auto-generated method stub
 
 		}
 	};
 
 	private OnSeekBarChangeListener seekBarChangeListenerTotalJumps = new OnSeekBarChangeListener() {
 
 		public void onProgressChanged(SeekBar seekBar, int progress,
 				boolean fromUser) {
 			savePreference(SETTING_TOTAL_JUMPS, progress);
 			currentTotalJumps = progress;
 			setTotalJumpsSettingText(progress);
 			// check to see if jumps in last 12 months in not higher
 			SeekBar sbJumpsLast12Months = (SeekBar) findViewById(R.id.seekBarJumpsLast12Months);
 			int jumpsLast12Months = sbJumpsLast12Months.getProgress();
 			if (jumpsLast12Months > progress)
 				sbJumpsLast12Months.setProgress(progress);
 			calculate();
 		}
 
 		public void onStartTrackingTouch(SeekBar seekBar) {
 			// TODO Auto-generated method stub
 
 		}
 
 		public void onStopTrackingTouch(SeekBar seekBar) {
 			// TODO Auto-generated method stub
 
 		}
 	};
 
 	private OnSeekBarChangeListener seekBarChangeListenerJumpsLast12Months = new OnSeekBarChangeListener() {
 
 		public void onProgressChanged(SeekBar seekBar, int progress,
 				boolean fromUser) {
 			savePreference(SETTING_JUMPS_LAST_12_MONTHS, progress);
 			currentJumpsLast12Months = progress;
 			setJumpsLast12MonthsSettingText(progress);
 			// check to see if jumps in last 12 months in not higher
 			SeekBar sbTotalJumps = (SeekBar) findViewById(R.id.seekBarTotalJumps);
 			int totalJumps = sbTotalJumps.getProgress();
 			if (totalJumps < progress)
 				sbTotalJumps.setProgress(progress);
 			calculate();
 		}
 
 		public void onStartTrackingTouch(SeekBar seekBar) {
 			// TODO Auto-generated method stub
 
 		}
 
 		public void onStopTrackingTouch(SeekBar seekBar) {
 			// TODO Auto-generated method stub
 
 		}
 	};
 
 	/***
 	 * Saves an (integer) preference to the global prefs var.
 	 * 
 	 * @param preferenceName
 	 *            the name of the preference
 	 * @param value
 	 *            the (new) value of the preference
 	 */
 	private void savePreference(String preferenceName, int value) {
 		Editor e = prefs.edit();
 		e.putInt(preferenceName, value);
 		e.commit();
 	}
 
 	private void setWeightSettingText(int weightInKg) {
 		int weightInLbs = Calculation.kgToLbs(weightInKg);
 		TextView tvWeight = (TextView) findViewById(R.id.textViewWeightLabel);
 		String weightLabel = getString(R.string.calculationWeightLabel);
 		String weightFormat = getString(R.string.calculationWeightSetting);
 		tvWeight.setText(weightLabel
 				+ String.format(weightFormat, weightInKg, weightInLbs));
 
 		// fill the wingload table
 		int column = 0;
 		TextView area1 = (TextView) findViewById(R.id.textViewArea1);
 		TextView wingLoad1 = (TextView) findViewById(R.id.textViewWingLoad1);
 		fillWingLoadTableColumn(column++, weightInLbs, area1, wingLoad1);
 		TextView area2 = (TextView) findViewById(R.id.textViewArea2);
 		TextView wingLoad2 = (TextView) findViewById(R.id.textViewWingLoad2);
 		fillWingLoadTableColumn(column++, weightInLbs, area2, wingLoad2);
 		TextView area3 = (TextView) findViewById(R.id.textViewArea3);
 		TextView wingLoad3 = (TextView) findViewById(R.id.textViewWingLoad3);
 		fillWingLoadTableColumn(column++, weightInLbs, area3, wingLoad3);
 		TextView area4 = (TextView) findViewById(R.id.textViewArea4);
 		TextView wingLoad4 = (TextView) findViewById(R.id.textViewWingLoad4);
 		fillWingLoadTableColumn(column++, weightInLbs, area4, wingLoad4);
 		TextView area5 = (TextView) findViewById(R.id.textViewArea5);
 		TextView wingLoad5 = (TextView) findViewById(R.id.textViewWingLoad5);
 		fillWingLoadTableColumn(column++, weightInLbs, area5, wingLoad5);
 		TextView area6 = (TextView) findViewById(R.id.textViewArea6);
 		TextView wingLoad6 = (TextView) findViewById(R.id.textViewWingLoad6);
 		fillWingLoadTableColumn(column++, weightInLbs, area6, wingLoad6);
 
 	}
 
 	private void fillWingLoadTableColumn(int column, int weightInLbs,
 			TextView tvArea, TextView tvWingLoad) {
 		int area = WINGLOAD_FIRST_COL + column * WINGLOAD_STEP;
 		double wingload = (double) weightInLbs / (double) area;
 		tvArea.setText(String.format(" %d ", area));
 		tvWingLoad.setText(String.format(" %.2f ", wingload));
 	}
 
 	private void setTotalJumpsSettingText(int totalJumps) {
 		TextView tvTotalJumps = (TextView) findViewById(R.id.textViewTotalJumpsLabel);
 		String totalJumpsLabel = getString(R.string.calculationTotalJumpsLabel);
 		String totalJumpsFormat = getString(R.string.calculationTotalJumpsSetting);
 		String orMoreText = "";
 		if (totalJumps > TOTALJUMPS_LASTGROUP)
 			orMoreText = getString(R.string.ormore);
 		tvTotalJumps.setText(totalJumpsLabel
 				+ String.format(totalJumpsFormat, totalJumps, orMoreText));
 	}
 
 	private void setJumpsLast12MonthsSettingText(int jumps) {
 		TextView tvJumpsLast12Months = (TextView) findViewById(R.id.textViewJumpsLast12MonthsLabel);
 		String jumpsLast12MonthsLabel = getString(R.string.calculationJumpsLast12MonthsLabel);
 		String jumpsLast12MonthsFormat = getString(R.string.calculationJumpsLast12MonthsSetting);
 		String orMoreText = "";
 		if (jumps > JUMPS_LAST_12_MONTHS_LASTGROUP)
 			orMoreText = getString(R.string.ormore);
 		tvJumpsLast12Months.setText(jumpsLast12MonthsLabel
 				+ String.format(jumpsLast12MonthsFormat, jumps, orMoreText));
 	}
 
 	/**
 	 * Calculate the current category, based on weight, total jumps and jumps
 	 * last year
 	 */
 	private void calculate() {
 		// get weight and set text
 		SeekBar sbWeight = (SeekBar) findViewById(R.id.seekBarWeight);
 		int weightInKg = sbWeight.getProgress() + WEIGHT_MIN;
 
 		SeekBar sbTotalJumps = (SeekBar) findViewById(R.id.seekBarTotalJumps);
 		int totalJumps = sbTotalJumps.getProgress();
 
 		SeekBar sbJumpsLast12Months = (SeekBar) findViewById(R.id.seekBarJumpsLast12Months);
 		int jumpsLast12Months = sbJumpsLast12Months.getProgress();
 
 		int jumperCategory = Calculation.jumperCategory(totalJumps,
 				jumpsLast12Months);
 
 		// now decide on minArea and maxWingload
 		int minArea = Calculation.minArea(jumperCategory, weightInKg);
 
 		// only update screen if there actually is a change, so seek bars
 		// respond quickly
 		if (KompasroosBaseActivity.currentMaxCategory != jumperCategory
 				|| KompasroosBaseActivity.currentMinArea != minArea) {
 			TextView tvJumperCategory = (TextView) findViewById(R.id.textViewJumperCategory);
 			String jumperCatFormat = getString(R.string.categorySetting);
 			tvJumperCategory.setText(String.format(jumperCatFormat,
 					jumperCategory));
 
 			TextView tvJumperCategoryDescription = (TextView) findViewById(R.id.textViewJumperCategoryDescription);
 			String jumperCatDescriptionFormat = getString(R.string.categorySettingDescription);
 			String jumperCategories[] = getResources().getStringArray(
 					R.array.jumperCategories);
 			tvJumperCategoryDescription.setText(String.format(
 					jumperCatDescriptionFormat,
 					jumperCategories[jumperCategory]));
 
 			TextView tvCanopyCategory = (TextView) findViewById(R.id.textViewCanopyCategoryText);
 			String canopyCatFormat;
 			switch (jumperCategory) {
 			case 1:
 				canopyCatFormat = getString(R.string.calculationCanopyCategoryOne);
 				break;
 			case 6:
 				canopyCatFormat = getString(R.string.calculationCanopyCategoryAll);
 				break;
 			default:
 				canopyCatFormat = String.format(
 						getString(R.string.calculationCanopyCategoryMultiple),
 						jumperCategory);
 				break;
 			}
 			tvCanopyCategory.setText(canopyCatFormat);
 
 			TextView tvCanopyMinArea = (TextView) findViewById(R.id.textViewCanopyMinAreaText);
 			int minAreaBasedOnCategory = Calculation
 					.minAreaBasedOnCategory(jumperCategory);
 			String minAreaText;
 			switch (minAreaBasedOnCategory) {
 			case 0:
 				minAreaText = getString(R.string.calculationCanopyMinAreaAny);
 				break;
 			default:
 				minAreaText = String.format(
 						getString(R.string.calculationCanopyMinArea),
 						minAreaBasedOnCategory);
 				break;
 			}
 			tvCanopyMinArea.setText(minAreaText);
 
 			TextView tvCanopyMaxWingLoad = (TextView) findViewById(R.id.textViewCanopyMaxWingLoadText);
 			double maxWingLoad = Calculation
 					.maxWingLoadBasedOnCategory(jumperCategory);
 			String maxWingLoadText;
 			if (maxWingLoad > 10)
 				maxWingLoadText = getString(R.string.calculationCanopyMaxWingLoadAny);
 			else
 				maxWingLoadText = String.format(
 						getString(R.string.calculationCanopyMaxWingLoad),
 						maxWingLoad);
 			tvCanopyMaxWingLoad.setText(maxWingLoadText);
 
 			TextView tvCanopyAdvise = (TextView) findViewById(R.id.textViewCanopyAdvise);
			String canopyAdviseFormat = getString(R.string.canopyAdvise);
 			tvCanopyAdvise.setText(String.format(canopyAdviseFormat,
 					jumperCategory, minArea));
 
 			// save globally, to pass on in buttonClick
 			KompasroosBaseActivity.currentMaxCategory = jumperCategory;
 			KompasroosBaseActivity.currentMinArea = minArea;
 		}
 	}
 }
