 package nl.digitalica.skydivekompasroos;
 
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.UUID;
 
 import nl.digitalica.skydivekompasroos.CanopyBase.AcceptabilityEnum;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences.Editor;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.text.InputType;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.SeekBar;
 import android.widget.TableLayout;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TableRow;
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
 
 	// dialog ID's
 	final static int SAVE_DIALOG_ID = 1;
 	final static int RESET_DIALOG_ID = 2;
 	final static int TOTAL_JUMPS_DIALOG_ID = 3;
 	final static int JUMPS_LAST_12_MONTHS_DIALOG_ID = 4;
 	final static int WEIGHT_DIALOG_ID = 5;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_calculate);
 
 		// if compile date over 1 year ago, show warning text
 		TextView tvWarning = (TextView) findViewById(R.id.textViewWarning);
 		String warning = "";
 		try {
 			long compilationDateTime = getCompileDateTime();
 			Calendar cal = Calendar.getInstance();
 			long now = cal.getTime().getTime();
			long maxDiff = 1000 * 60 * 60 * 24 * 365; // 1 year
 			// maxDiff = 1000 * 60 * 5; // 5 mins (for testing)
 			if (now - compilationDateTime > maxDiff)
 				warning = getString(R.string.calculationOvertimeWarning);
 		} catch (Exception e) {
 			warning = getString(R.string.calculationOvertimeUnknownWarning);
 		}
 		tvWarning.setText(warning);
 
 		// initialize seek bars and calculated texts
 		initSeekBars();
 
 		fillSpecificCanopyTable();
 
 		// set click listener for canopy list button
 		ImageButton canopyListButton = (ImageButton) findViewById(R.id.buttonShowCanopyList);
 		canopyListButton.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				startActivity(new Intent(getBaseContext(),
 						CanopyTypeListActivity.class));
 			}
 		});
 
 		// set click listener for about button
 		ImageButton aboutButton = (ImageButton) findViewById(R.id.buttonAbout);
 		aboutButton.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				startActivity(new Intent(getBaseContext(), AboutActivity.class));
 			}
 		});
 
 		// add click listener for allowed header
 		View filterHeader = findViewById(R.id.tablelayout_filterheader);
 		filterHeader.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				startActivity(new Intent(getBaseContext(),
 						CanopyTypeListActivity.class));
 			}
 		});
 
 		// set click listener for specific canopy add
 		Button addSpecificCanopy = (Button) findViewById(R.id.buttonAddSpecificCanopy);
 		addSpecificCanopy.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				Intent intent = new Intent(getBaseContext(),
 						SpecificCanopyEdit.class);
 				Bundle bundle = new Bundle();
 				bundle.putInt(SPECIFICCANOPYID_KEY, 0);
 				intent.putExtras(bundle);
 				startActivity(intent);
 			}
 		});
 
 		TextView tvTotalJumps = (TextView) findViewById(R.id.textViewTotalJumpsLabel);
 		tvTotalJumps.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				showDialog(TOTAL_JUMPS_DIALOG_ID);
 			}
 
 		});
 
 		TextView tvJumpsLast12Months = (TextView) findViewById(R.id.textViewJumpsLast12MonthsLabel);
 		tvJumpsLast12Months.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				showDialog(JUMPS_LAST_12_MONTHS_DIALOG_ID);
 			}
 
 		});
 
 		TextView tvWeight = (TextView) findViewById(R.id.textViewWeightLabel);
 		tvWeight.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				showDialog(WEIGHT_DIALOG_ID);
 			}
 
 		});
 
 		// Just for testing the canopy list
 		// canopyListButton.performClick();
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		fillSpecificCanopyTable();
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		initSeekBarTextsAndCalculate();
 		updateSpecificCanopyTable();
 	}
 
 	private void fillSpecificCanopyTable() {
 		// TODO Auto-generated method stub
 		TableLayout scTable = (TableLayout) findViewById(R.id.tableSpecificCanopies);
 		scTable.removeAllViews();
 		insertCanopyHeaderRow(scTable);
 		List<SpecificCanopy> scList = SpecificCanopy
 				.getSpecificCanopiesInList(CalculateActivity.this);
 		HashMap<UUID, CanopyType> canopyTypes = CanopyType
 				.getCanopyTypeHash(CalculateActivity.this);
 		for (SpecificCanopy theCanopy : scList) {
 			CanopyType ct = canopyTypes.get(theCanopy.typeId);
 			insertSpecificCanopyRow(scTable, theCanopy, ct.specificName(),
 					ct.category);
 		}
 
 		// enable or disable button if needed
 		Button addSpecificCanopy = (Button) findViewById(R.id.buttonAddSpecificCanopy);
 		if (scList.size() >= SpecificCanopy.MAXSPECIFICCANOPIES)
 			addSpecificCanopy.setEnabled(false);
 		else
 			addSpecificCanopy.setEnabled(true);
 	}
 
 	/**
 	 * Update the already drawn specific canopy table, based on the data in the
 	 * table itself.
 	 */
 	private void updateSpecificCanopyTable() {
 		// TODO Auto-generated method stub
 		TableLayout scTable = (TableLayout) findViewById(R.id.tableSpecificCanopies);
 		for (int i = 1; i < scTable.getChildCount(); i++) {
 			TableRow canopyListRow = (TableRow) scTable.getChildAt(i);
 			TextView tvSize = (TextView) canopyListRow
 					.findViewById(R.id.textViewSpecificSize);
 			TextView tvType = (TextView) canopyListRow
 					.findViewById(R.id.textViewSpecificType);
 			TextView tvRemarks = (TextView) canopyListRow
 					.findViewById(R.id.textViewSpecificRemarks);
 			TextView tvWingLoad = (TextView) canopyListRow
 					.findViewById(R.id.textViewSpecificWingload);
 
 			int tagAll = (Integer) tvWingLoad.getTag();
 			int typeCategory = tagAll % 10;
 			int size = (tagAll - typeCategory) / 10;
 
 			double wingload = Calculation.wingLoad(size, currentWeight);
 			tvWingLoad.setText(String.format("%.2f", wingload));
 
 			AcceptabilityEnum acc = SpecificCanopy.acceptablility(
 					currentMaxCategory, typeCategory, size, currentWeight);
 			// We need different drawables for each column as the widths are
 			// different
 			Drawable backgroundCol1 = backgroundDrawableForAcceptance(acc);
 			Drawable backgroundCol2 = backgroundDrawableForAcceptance(acc);
 			Drawable backgroundCol3 = backgroundDrawableForAcceptance(acc);
 			Drawable backgroundCol4 = backgroundDrawableForAcceptance(acc);
 
 			tvSize.setBackgroundDrawable(backgroundCol1);
 			tvType.setBackgroundDrawable(backgroundCol2);
 			tvRemarks.setBackgroundDrawable(backgroundCol3);
 			tvWingLoad.setBackgroundDrawable(backgroundCol4);
 		}
 	}
 
 	private void insertCanopyHeaderRow(TableLayout scTable) {
 		// TODO Auto-generated method stub
 		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		View canopyListRow = inflater.inflate(
 				R.layout.specific_canopy_row_layout, null);
 
 		TextView tvSize = (TextView) canopyListRow
 				.findViewById(R.id.textViewSpecificSize);
 		TextView tvType = (TextView) canopyListRow
 				.findViewById(R.id.textViewSpecificType);
 		TextView tvRemarks = (TextView) canopyListRow
 				.findViewById(R.id.textViewSpecificRemarks);
 		TextView tvWingload = (TextView) canopyListRow
 				.findViewById(R.id.textViewSpecificWingload);
 
 		tvSize.setText(getString(R.string.specificCanopyHeaderSize));
 		tvType.setText(getString(R.string.specificCanopyHeaderType));
 		tvRemarks.setText(getString(R.string.specificCanopyHeaderRemarks));
 		tvWingload.setText(getString(R.string.specificCanopyHeaderWingLoad));
 
 		scTable.addView(canopyListRow);
 	}
 
 	private void insertSpecificCanopyRow(TableLayout scTable,
 			SpecificCanopy theCanopy, String typeName, int typeCategory) {
 		// TODO Auto-generated method stub
 		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		View canopyListRow = inflater.inflate(
 				R.layout.specific_canopy_row_layout, null);
 
 		TextView size = (TextView) canopyListRow
 				.findViewById(R.id.textViewSpecificSize);
 		TextView type = (TextView) canopyListRow
 				.findViewById(R.id.textViewSpecificType);
 		TextView remarks = (TextView) canopyListRow
 				.findViewById(R.id.textViewSpecificRemarks);
 		TextView wingload = (TextView) canopyListRow
 				.findViewById(R.id.textViewSpecificWingload);
 
 		size.setText(Integer.toString(theCanopy.size));
 		type.setText(typeName);
 		remarks.setText(theCanopy.remarks);
 		wingload.setTag(theCanopy.size * 10 + typeCategory);
 
 		// set click listener for specific canopy edit
 
 		canopyListRow.setTag(theCanopy.id);
 		canopyListRow.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				Intent intent = new Intent(getBaseContext(),
 						SpecificCanopyEdit.class);
 				Bundle bundle = new Bundle();
 				bundle.putInt(SPECIFICCANOPYID_KEY, (Integer) v.getTag());
 				intent.putExtras(bundle);
 				startActivity(intent);
 			}
 		});
 
 		// add it to the table
 		scTable.addView(canopyListRow);
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		switch (id) {
 		case SAVE_DIALOG_ID:
 			return saveDialog();
 		case RESET_DIALOG_ID:
 			return resetDialog();
 		case TOTAL_JUMPS_DIALOG_ID:
 			return totalJumpsDialog();
 		case JUMPS_LAST_12_MONTHS_DIALOG_ID:
 			return jumpsLast12MonthsDialog();
 		case WEIGHT_DIALOG_ID:
 			return weightDialog();
 		}
 		return null;
 	}
 
 	private Dialog totalJumpsDialog() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 
 		builder.setTitle(getString(R.string.totalNumberOfJumpsDialogTitle));
 		builder.setMessage(String.format(
 				getString(R.string.enterTotalNumberOfJumpsFormat),
 				TOTALJUMPS_MAX));
 
 		// Set an EditText view to get user input
 		final EditText input = new EditText(this);
 		input.setInputType(InputType.TYPE_CLASS_NUMBER);
 		builder.setView(input);
 
 		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				String valueText = input.getText().toString();
 				int value = Integer.parseInt(valueText);
 				if (value >= 0 && value <= TOTALJUMPS_MAX) {
 					SeekBar sb = (SeekBar) findViewById(R.id.seekBarTotalJumps);
 					sb.setProgress(value);
 				}
 				// make sure it will be initialized next time...
 				removeDialog(TOTAL_JUMPS_DIALOG_ID);
 			}
 		});
 
 		builder.setNegativeButton("Cancel",
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int whichButton) {
 						// make sure it will be initialized next time...
 						removeDialog(TOTAL_JUMPS_DIALOG_ID);
 					}
 				});
 
 		return builder.create();
 	}
 
 	private Dialog jumpsLast12MonthsDialog() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 
 		builder.setTitle(getString(R.string.numberOfJumpsLast12MonthsDialogTitle));
 		builder.setMessage(String.format(
 				getString(R.string.enterNumberOfJumpsLast12MonthsFormat),
 				JUMPS_LAST_12_MONTHS_MAX));
 
 		// Set an EditText view to get user input
 		final EditText input = new EditText(this);
 		input.setInputType(InputType.TYPE_CLASS_NUMBER);
 		builder.setView(input);
 
 		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				String valueText = input.getText().toString();
 				int value = Integer.parseInt(valueText);
 				if (value >= 0 && value <= JUMPS_LAST_12_MONTHS_MAX) {
 					SeekBar sb = (SeekBar) findViewById(R.id.seekBarJumpsLast12Months);
 					sb.setProgress(value);
 				}
 				// make sure it will be initialized next time...
 				removeDialog(JUMPS_LAST_12_MONTHS_DIALOG_ID);
 			}
 		});
 
 		builder.setNegativeButton("Cancel",
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int whichButton) {
 						// make sure it will be initialized next time...
 						removeDialog(JUMPS_LAST_12_MONTHS_DIALOG_ID);
 					}
 				});
 
 		return builder.create();
 
 	}
 
 	private Dialog weightDialog() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 
 		builder.setTitle(getString(R.string.weightDialogTitle));
 		builder.setMessage(String.format(getString(R.string.enterWeightFormat),
 				WEIGHT_MIN, WEIGHT_MAX));
 
 		// Set an EditText view to get user input
 		final EditText input = new EditText(this);
 		input.setInputType(InputType.TYPE_CLASS_NUMBER);
 		builder.setView(input);
 
 		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				String valueText = input.getText().toString();
 				int value = Integer.parseInt(valueText);
 				if (value >= WEIGHT_MIN && value <= WEIGHT_MAX) {
 					SeekBar sb = (SeekBar) findViewById(R.id.seekBarWeight);
 					sb.setProgress(value - WEIGHT_MIN);
 				}
 				// make sure it will be initialized next time...
 				removeDialog(WEIGHT_DIALOG_ID);
 			}
 		});
 
 		builder.setNegativeButton("Cancel",
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int whichButton) {
 						// make sure it will be initialized next time...
 						removeDialog(WEIGHT_DIALOG_ID);
 					}
 				});
 
 		return builder.create();
 	}
 
 	/***
 	 * The save dialog to allow saving the current settings. They can be saved
 	 * as 'own' or 'friend' settings.
 	 * 
 	 * @return
 	 */
 	private Dialog saveDialog() {
 		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		View layout = inflater.inflate(R.layout.save_dialog,
 				(ViewGroup) findViewById(R.id.root));
 		Button asFriend = (Button) layout.findViewById(R.id.buttonFriend);
 		Button asOwn = (Button) layout.findViewById(R.id.buttonOwn);
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
 
 	/***
 	 * The dialog to reset the settings. Settings can be reset to beginner,
 	 * intermediate or pro or to saved users own or friend settings.
 	 * 
 	 * @return
 	 */
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
 
 	/***
 	 * Set three seekbars. This will automatically trigger calculate calls
 	 * through the OnProgressChanged of the seekbars.
 	 * 
 	 * @param weight
 	 * @param totalJumps
 	 * @param jumpsLast12Months
 	 */
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
 
 	/***
 	 * Initialize the seekbar listeners
 	 */
 	private void initSeekBars() {
 		// weight seek bar
 		SeekBar sbWeight = (SeekBar) findViewById(R.id.seekBarWeight);
 		sbWeight.setMax(WEIGHT_MAX - WEIGHT_MIN);
 		sbWeight.setOnSeekBarChangeListener(seekBarChangeListenerWeight);
 		setPlusMinButtonListeners(sbWeight, R.id.buttonWeightMin,
 				R.id.buttonWeightPlus);
 
 		// total jumps seek bar
 		SeekBar sbTotalJumps = (SeekBar) findViewById(R.id.seekBarTotalJumps);
 		sbTotalJumps.setMax(TOTALJUMPS_MAX);
 		sbTotalJumps
 				.setOnSeekBarChangeListener(seekBarChangeListenerTotalJumps);
 		setPlusMinButtonListeners(sbWeight, R.id.buttonTotalJumpsMin,
 				R.id.buttonTotalJumpsPlus);
 
 		// jumps last 12 months seek bar
 		SeekBar sbJumpsLast12Months = (SeekBar) findViewById(R.id.seekBarJumpsLast12Months);
 		sbJumpsLast12Months.setMax(JUMPS_LAST_12_MONTHS_MAX);
 		sbJumpsLast12Months
 				.setOnSeekBarChangeListener(seekBarChangeListenerJumpsLast12Months);
 		setPlusMinButtonListeners(sbWeight, R.id.buttonJumpLast12MonthsMin,
 				R.id.buttonJumpLast12MonthsPlus);
 	}
 
 	/***
 	 * Initialize the seekbars texts
 	 */
 	private void initSeekBarTextsAndCalculate() {
 		// weight seek bar
 		SeekBar sbWeight = (SeekBar) findViewById(R.id.seekBarWeight);
 		int weightInKg = prefs.getInt(SETTING_WEIGHT, WEIGHT_DEFAULT);
 		sbWeight.setProgress(weightInKg - WEIGHT_MIN);
 
 		// total jumps seek bar
 		SeekBar sbTotalJumps = (SeekBar) findViewById(R.id.seekBarTotalJumps);
 		int totalJumps = prefs.getInt(SETTING_TOTAL_JUMPS, TOTALJUMPS_DEFAULT);
 		sbTotalJumps.setProgress(totalJumps);
 
 		// jumps last 12 months seek bar
 		SeekBar sbJumpsLast12Months = (SeekBar) findViewById(R.id.seekBarJumpsLast12Months);
 		int jumpsLast12Months = prefs.getInt(SETTING_JUMPS_LAST_12_MONTHS,
 				JUMPS_LAST_12_MONTHS_DEFAULT);
 		sbJumpsLast12Months.setProgress(jumpsLast12Months);
 
 		// now calculate to set all texts
 		calculate();
 	}
 
 	/***
 	 * Add click listeners to the plus and min buttons at the left and right of
 	 * a seekbar
 	 */
 	private void setPlusMinButtonListeners(SeekBar sb, int minButtonId,
 			int plusButtonId) {
 		// get the buttons based on the give id's
 		Button minButton = (Button) findViewById(minButtonId);
 		Button plusButton = (Button) findViewById(plusButtonId);
 		// add click listener to the min button
 		minButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
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
 		// add click listener to the plus button
 		plusButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
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
 
 	/***
 	 * The seekbar change listener for the exit weight
 	 */
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
 			// No action required
 		}
 
 		public void onStopTrackingTouch(SeekBar seekBar) {
 			// No action required
 		}
 	};
 
 	/***
 	 * Listener for changes on the total jumps seekbar. Triggers a calculate
 	 * call, that updates texts where needed.
 	 */
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
 
 	/***
 	 * Listener for changes on the jumps in last 12 months seekbar. Triggers a
 	 * calculate call, that updates texts where needed.
 	 */
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
 
 	/**
 	 * Fills the wingloadtable. As the table is allways six positings, its
 	 * starting point depends on the current category and weight.
 	 * 
 	 * @param weightInKg
 	 *            TODO: split in fill (first row) & update (second row & colors)
 	 */
 	private void fillWingloadTable(int weightInKg) {
 
 		final int[] WLTBL = new int[] { 120, 135, 150, 170, 190, 210, 230 };
 
 		// fill the wingload table
 		int column = 0;
 		TextView area1 = (TextView) findViewById(R.id.textViewArea1);
 		TextView wingLoad1 = (TextView) findViewById(R.id.textViewWingLoad1);
 		fillWingLoadTableColumn(WLTBL[column++], weightInKg, area1, wingLoad1);
 		TextView area2 = (TextView) findViewById(R.id.textViewArea2);
 		TextView wingLoad2 = (TextView) findViewById(R.id.textViewWingLoad2);
 		fillWingLoadTableColumn(WLTBL[column++], weightInKg, area2, wingLoad2);
 		TextView area3 = (TextView) findViewById(R.id.textViewArea3);
 		TextView wingLoad3 = (TextView) findViewById(R.id.textViewWingLoad3);
 		fillWingLoadTableColumn(WLTBL[column++], weightInKg, area3, wingLoad3);
 		TextView area4 = (TextView) findViewById(R.id.textViewArea4);
 		TextView wingLoad4 = (TextView) findViewById(R.id.textViewWingLoad4);
 		fillWingLoadTableColumn(WLTBL[column++], weightInKg, area4, wingLoad4);
 		TextView area5 = (TextView) findViewById(R.id.textViewArea5);
 		TextView wingLoad5 = (TextView) findViewById(R.id.textViewWingLoad5);
 		fillWingLoadTableColumn(WLTBL[column++], weightInKg, area5, wingLoad5);
 		TextView area6 = (TextView) findViewById(R.id.textViewArea6);
 		TextView wingLoad6 = (TextView) findViewById(R.id.textViewWingLoad6);
 		fillWingLoadTableColumn(WLTBL[column++], weightInKg, area6, wingLoad6);
 		TextView area7 = (TextView) findViewById(R.id.textViewArea7);
 		TextView wingLoad7 = (TextView) findViewById(R.id.textViewWingLoad7);
 		fillWingLoadTableColumn(WLTBL[column++], weightInKg, area7, wingLoad7);
 	}
 
 	/***
 	 * Fills a single column in the wing load table
 	 * 
 	 * @param area
 	 * @param weightInKg
 	 * @param tvArea
 	 * @param tvWingLoad
 	 */
 	private void fillWingLoadTableColumn(int area, int weightInKg,
 			TextView tvArea, TextView tvWingLoad) {
 		double wingload = Calculation.wingLoad(area, weightInKg);
 
 		Drawable backgroundRed = getResources().getDrawable(
 				R.drawable.canopycategorytoohigh);
 		Drawable backgroundOrange = getResources().getDrawable(
 				R.drawable.canopyneededsizenotavailable);
 		Drawable backgroundGreen = getResources().getDrawable(
 				R.drawable.canopyacceptable);
 
 		int areaAllowedOnCat = Calculation
 				.minAreaBasedOnCategory(currentMaxCategory);
 		boolean areaOrWingloadOutOfRange = area < Calculation.minArea(
 				currentMaxCategory, weightInKg);
 		tvArea.setText(String.format("%d", area));
 		if (area < areaAllowedOnCat)
 			tvArea.setBackgroundDrawable(backgroundRed);
 		else if (areaOrWingloadOutOfRange)
 			tvArea.setBackgroundDrawable(backgroundOrange);
 		else
 			tvArea.setBackgroundDrawable(backgroundGreen);
 
 		double maxWinloadAllowed = Calculation
 				.maxWingLoadBasedOnCategory(currentMaxCategory);
 		tvWingLoad.setText(String.format("%.2f", wingload));
 		if (wingload > maxWinloadAllowed)
 			tvWingLoad.setBackgroundDrawable(backgroundRed);
 		else if (areaOrWingloadOutOfRange)
 			tvWingLoad.setBackgroundDrawable(backgroundOrange);
 		else
 			tvWingLoad.setBackgroundDrawable(backgroundGreen);
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
 
 	private void setWeightSettingText(int weightInKg) {
 		TextView tvWeight = (TextView) findViewById(R.id.textViewWeightLabel);
 		String weightLabel = getString(R.string.calculationWeightLabel);
 		String weightFormat = getString(R.string.calculationWeightSetting);
 		tvWeight.setText(weightLabel
 				+ String.format(weightFormat, weightInKg,
 						Calculation.kgToLbs(weightInKg)));
 		fillWingloadTable(weightInKg);
 	}
 
 	/**
 	 * Calculate the current category, based on weight, total jumps and jumps
 	 * last year.
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
 			canopyCatFormat = String.format(
 					getString(R.string.calculationCanopyCategory),
 					jumperCategory);
 			tvCanopyCategory.setText(canopyCatFormat);
 
 			TextView tvCanopyMinArea = (TextView) findViewById(R.id.textViewCanopyMinAreaText);
 			int minAreaBasedOnCategory = Calculation
 					.minAreaBasedOnCategory(jumperCategory);
 			String minAreaText;
 			// TODO: this string should be taken from canopy class (or method in
 			// calculation)
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
 			String canopyAdviseFormat;
 			// TODO: this string should be taken from a jumper class (or method
 			// in calculation)
 			canopyAdviseFormat = getString(R.string.canopyAdvise);
 			tvCanopyAdvise.setText(String.format(canopyAdviseFormat,
 					jumperCategory, minArea));
 
 			// save globally, to pass on in buttonClick
 			KompasroosBaseActivity.currentMaxCategory = jumperCategory;
 			KompasroosBaseActivity.currentMinArea = minArea;
 
 			fillWingloadTable(weightInKg);
 			updateSpecificCanopyTable();
 		}
 	}
 }
