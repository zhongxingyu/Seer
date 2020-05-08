 package com.example.myauto;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import com.example.myauto.database.DBHelper;
 import com.example.myauto.database.DBManager;
 import com.example.myauto.filter.CarMarkFiltPage;
 import com.example.myauto.filter.CarModelFiltPage;
 import com.example.myauto.filter.Filter;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.opengl.Visibility;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.RelativeLayout;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.TableLayout.LayoutParams;
 
 public class SearchPageActivity extends MasterPageActivity {
 	private static final String DIALOG_ENGINE_TITLE_EN = "Engine";
 	private static final String DIALOG_DAYS_TITLE_EN = "Days";
 	private static final String DIALOG_CUSTOMS_PASSED_TITLE_EN = "Customs Passed";
 	private static final String DIALOG_DRIVEWHEELS_TITLE_EN = "Drive Wheels";
 	private static final String DIALOG_DOORS_TITLE_EN = "Doors";
 	private static final String DIALOG_LOCATION_TITLE_EN = "Location";
 	private static final String DIALOG_FUELTYPE_TITLE_EN = "Fuel Type";
 	private static final String DIALOG_CATEGORIES_TITLE_EN = "Categories";
 	private static final String DIALOG_WHEEL_TITLE_EN = "Right Wheel";
 	private static final int DIALOG_CUSTOMS_AND_WHEEL_BTN_YES_ID = 1;
 	private static final int DIALOG_CUSTOMS_AND_WHEEL_BTN_NO_ID = 2;
 	private static final int STARTING_YEAR = 1960;
 	private static final int MARK_FILTER = 1001;
 	private static final int MODEL_FILTER = 1002;
 	private static final int NUMBER_OF_FILTER_BUTTONS = 29;
 	private RelativeLayout carMark, carModel, carPrice, carYear, carCategory,
 			carLocation, carTransmission, carFuel, carWheel, carDays, carDoors,
 			carDriveWheels, carCustomsPassed, carEngine, carAbs,
 			carCentralLock, carEsd, carBoardComp, carLeatherInt, carElWindows,
 			carAirbags, carParkingControl, carAlumDisks, carHatch,
 			carChairWarming, carNavigSystem;
 	private Button searchSubmit, carMarkBtn, carModelBtn, carPriceBtn,
 			carYearBtn, carCategoryBtn, carLocationBtn, carTransmissionBtn,
 			carFuelBtn, carWheelBtn, carDaysBtn, carDoorsBtn,
 			carDriveWheelsBtn, carCustomsPassedBtn, carEngineBtn, carAbsBtn,
 			carCentralLockBtn, carEsdBtn, carBoardCompBtn, carLeatherIntBtn,
 			carElWindowsBtn, carAirbagsBtn, carParkingControlBtn,
 			carAlumDisksBtn, carHatchBtn, carChairWarmingBtn,
 			carNavigSystemBtn;
 	private String[] filteredData;
 	private Context ctx;
 	private Activity a;
 	private String[] manufacturer;
 	private Resources res;
 	private SharedPreferences prefs;
 	private static final int LANG_EN = 1;
 	private static final int LANG_GE = 2;
 	private static final int LANG_RU = 3;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_search_page);
 
 		ctx = this;
 		a = this;
 
 		filteredData = new String[NUMBER_OF_FILTER_BUTTONS];
 		getFilterLayoutViews();
 		getButtonViews();
 
 		prefs = getSharedPreferences(
 				getResources().getString(R.string.shared_prefs), 0);
 	}
 
 	private void setButtonClickListeners() {
 		searchSubmit.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Filter f = new Filter(getApplicationContext(), filteredData, a);
 				f.filterAndDownload();
 			}
 		});
 
 		carMarkBtn.setOnClickListener(new cancelBtnListenerMark(giveIndex(0, 1,
 				2)));
 
 		carModelBtn.setOnClickListener(new cancelBtnListener(giveIndex(1, 100,
 				1), findViewById(R.id.search_carModel)));
 
 		carYearBtn.setOnClickListener(new cancelBtnListener(giveIndex(2, 3, 2),
 				findViewById(R.id.search_carYear)));
 
 		carPriceBtn.setOnClickListener(new cancelBtnListener(
 				giveIndex(4, 5, 2), findViewById(R.id.search_carPrice)));
 
 		carTransmissionBtn.setOnClickListener(new cancelBtnListener(giveIndex(
 				6, 100, 1), findViewById(R.id.search_carTransmission)));
 
 		carFuelBtn.setOnClickListener(new cancelBtnListener(
 				giveIndex(7, 100, 1), findViewById(R.id.search_carFuel)));
 
 		carCustomsPassedBtn.setOnClickListener(new cancelBtnListener(giveIndex(
 				8, 100, 1), findViewById(R.id.search_carCustomsPassed)));
 
 		carWheelBtn.setOnClickListener(new cancelBtnListener(giveIndex(9, 100,
 				1), findViewById(R.id.search_carWheel)));
 
 		carCategoryBtn.setOnClickListener(new cancelBtnListener(giveIndex(10,
 				100, 1), findViewById(R.id.search_carCategory)));
 
 		carLocationBtn.setOnClickListener(new cancelBtnListener(giveIndex(11,
 				100, 1), findViewById(R.id.search_carLocation)));
 
 		carDoorsBtn.setOnClickListener(new cancelBtnListener(giveIndex(12, 100,
 				1), findViewById(R.id.search_carDoors)));
 
 		carDriveWheelsBtn.setOnClickListener(new cancelBtnListener(giveIndex(
 				13, 100, 1), findViewById(R.id.search_carDriveWheels)));
 
 		carDaysBtn.setOnClickListener(new cancelBtnListener(giveIndex(14, 100,
 				1), findViewById(R.id.search_carDays)));
 
 		carEngineBtn.setOnClickListener(new cancelBtnListener(giveIndex(15, 16,
 				2), findViewById(R.id.search_carEngine)));
 
 		carAbsBtn.setOnClickListener(new cancelBtnListener(
 				giveIndex(17, 100, 1), findViewById(R.id.search_carAbs)));
 
 		carCentralLockBtn.setOnClickListener(new cancelBtnListener(giveIndex(
 				18, 100, 1), findViewById(R.id.search_carCentralLock)));
 
 		carEsdBtn.setOnClickListener(new cancelBtnListener(
 				giveIndex(19, 100, 1), findViewById(R.id.search_carEsd)));
 
 		carBoardCompBtn.setOnClickListener(new cancelBtnListener(giveIndex(20,
 				100, 1), findViewById(R.id.search_carBoardComp)));
 
 		carLeatherIntBtn.setOnClickListener(new cancelBtnListener(giveIndex(21,
 				100, 1), findViewById(R.id.search_carLeatherInt)));
 
 		carElWindowsBtn.setOnClickListener(new cancelBtnListener(giveIndex(22,
 				100, 1), findViewById(R.id.search_carElWindows)));
 
 		carAirbagsBtn.setOnClickListener(new cancelBtnListener(giveIndex(23,
 				100, 1), findViewById(R.id.search_carAirbags)));
 
 		carParkingControlBtn.setOnClickListener(new cancelBtnListener(
 				giveIndex(24, 100, 1),
 				findViewById(R.id.search_carParkingControl)));
 
 		carAlumDisksBtn.setOnClickListener(new cancelBtnListener(giveIndex(25,
 				100, 1), findViewById(R.id.search_carAlumDisks)));
 
 		carHatchBtn.setOnClickListener(new cancelBtnListener(giveIndex(26, 100,
 				1), findViewById(R.id.search_carHatch)));
 
 		carChairWarmingBtn.setOnClickListener(new cancelBtnListener(giveIndex(
 				27, 100, 1), findViewById(R.id.search_carChairWarming)));
 
 		carNavigSystemBtn.setOnClickListener(new cancelBtnListener(giveIndex(
 				28, 100, 1), findViewById(R.id.search_carNavigSystem)));
 
 	}
 
 	/*
 	 * Cacel Button Listeneristvis gadasacemad filtris shesabamis index-is
 	 * array-s vagenerireb.
 	 */
 	private int[] giveIndex(int a1, int a2, int d) {
 		int[] index = new int[d];
 		if (d == 2) {
 			index[0] = a1;
 			index[1] = a2;
 		} else if (d == 1) {
 			index[0] = a1;
 		}
 		return index;
 	}
 
 	/**
 	 * бѓ•бѓђбѓ§бѓ”бѓњбѓ”бѓ‘ бѓ¦бѓ�бѓљбѓђбѓ™бѓ”бѓ‘бѓ�бѓЎ
 	 * бѓљбѓ�бѓЎбѓ”бѓњбѓ”бѓ бѓ”бѓ‘бѓЎ бѓ“бѓђбѓ­бѓ”бѓ бѓђбѓ–бѓ”
 	 */
 	private void setFilterClickListeners() {
 		res = getResources();
 
 		carMark.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent markFilter = new Intent(SearchPageActivity.this,
 						CarMarkFiltPage.class);
 				startActivityForResult(markFilter, MARK_FILTER);
 			}
 		});
 
 		carModel.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent modelFilter = new Intent(SearchPageActivity.this,
 						CarModelFiltPage.class);
 				modelFilter.putExtra("Manufacturer", manufacturer);
 				startActivityForResult(modelFilter, MODEL_FILTER);
 			}
 		});
 
 		carPrice.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				carPriceDialog();
 			}
 		});
 
 		carYear.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				carYearDialog();
 			}
 		});
 
 		carTransmission.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				carTransmissionDialog();
 			}
 		});
 
 		carFuel.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				carFuelTypesDialog();
 			}
 
 		});
 
 		carCategory.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				carCategoriesDialog();
 			}
 		});
 
 		carCustomsPassed.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				carCustomsPassedDialog();
 			}
 		});
 
 		carWheel.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				carWheelDialog();
 			}
 		});
 
 		carLocation.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				carLocationDialog();
 			}
 		});
 
 		carDoors.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				carDoorTypesDialog();
 			}
 		});
 
 		carDriveWheels.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				carDriveWheelsDialog();
 			}
 		});
 
 		carDays.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				carDaysDialog();
 			}
 		});
 
 		carEngine.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				carEngineDialog();
 			}
 		});
 
 		carAbs.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				filteredData[17] = "1";
 				setSelectedValue(findViewById(R.id.search_carAbs),
 						res.getString(R.string.search_selectedValue_yes));
 				carAbsBtn.setVisibility(View.VISIBLE);
 			}
 		});
 
 		carCentralLock.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				filteredData[18] = "1";
 				setSelectedValue(findViewById(R.id.search_carCentralLock),
 						res.getString(R.string.search_selectedValue_yes));
 				carCentralLockBtn.setVisibility(View.VISIBLE);
 			}
 		});
 
 		carEsd.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				filteredData[19] = "1";
 				setSelectedValue(findViewById(R.id.search_carEsd),
 						res.getString(R.string.search_selectedValue_yes));
 				carEsdBtn.setVisibility(View.VISIBLE);
 			}
 		});
 
 		carBoardComp.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				filteredData[20] = "1";
 				setSelectedValue(findViewById(R.id.search_carBoardComp),
 						res.getString(R.string.search_selectedValue_yes));
 				carBoardCompBtn.setVisibility(View.VISIBLE);
 			}
 		});
 
 		carLeatherInt.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				filteredData[21] = "1";
 				setSelectedValue(findViewById(R.id.search_carLeatherInt),
 						res.getString(R.string.search_selectedValue_yes));
 				carLeatherIntBtn.setVisibility(View.VISIBLE);
 			}
 		});
 
 		carElWindows.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				filteredData[22] = "1";
 				setSelectedValue(findViewById(R.id.search_carElWindows),
 						res.getString(R.string.search_selectedValue_yes));
 				carElWindowsBtn.setVisibility(View.VISIBLE);
 			}
 		});
 
 		carAirbags.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				filteredData[23] = "1";
 				setSelectedValue(findViewById(R.id.search_carAirbags),
 						res.getString(R.string.search_selectedValue_yes));
 				carAirbagsBtn.setVisibility(View.VISIBLE);
 			}
 		});
 
 		carParkingControl.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				filteredData[24] = "1";
 				setSelectedValue(findViewById(R.id.search_carParkingControl),
 						res.getString(R.string.search_selectedValue_yes));
 				carParkingControlBtn.setVisibility(View.VISIBLE);
 			}
 		});
 
 		carAlumDisks.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				filteredData[25] = "1";
 				setSelectedValue(findViewById(R.id.search_carAlumDisks),
 						res.getString(R.string.search_selectedValue_yes));
 				carAlumDisksBtn.setVisibility(View.VISIBLE);
 			}
 		});
 
 		carHatch.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				filteredData[26] = "1";
 				setSelectedValue(findViewById(R.id.search_carHatch),
 						res.getString(R.string.search_selectedValue_yes));
 				carHatchBtn.setVisibility(View.VISIBLE);
 			}
 		});
 
 		carChairWarming.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				filteredData[27] = "1";
 				setSelectedValue(findViewById(R.id.search_carChairWarming),
 						res.getString(R.string.search_selectedValue_yes));
 				carChairWarmingBtn.setVisibility(View.VISIBLE);
 			}
 		});
 
 		carNavigSystem.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				filteredData[28] = "1";
 				setSelectedValue(findViewById(R.id.search_carNavigSystem),
 						res.getString(R.string.search_selectedValue_yes));
 				carNavigSystemBtn.setVisibility(View.VISIBLE);
 			}
 		});
 
 	}
 
 	/**
 	 * Damatebuli gancxadebis drois filtri
 	 */
 	private void carDaysDialog() {
 		final Dialog dialog = new Dialog(ctx);
 		dialog.setContentView(R.layout.dialog_car_generic_for_radiobuttons);
 		dialog.setTitle(DIALOG_DAYS_TITLE_EN);
 		
 		fillDaysDialog(dialog);
 		
 		Button cancel = (Button) dialog
 				.findViewById(R.id.dialog_generic_btn_cancel);
 		Button ok = (Button) dialog
 				.findViewById(R.id.dialog_generic_btn_ok);
 
 		ok.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				RadioGroup group = (RadioGroup) dialog
 						.findViewById(R.id.dialog_generic_rdgroup);
 				int id = group.getCheckedRadioButtonId();
 				RadioButton days = (RadioButton) dialog.findViewById(id);
 				String daysID = "" + days.getId();
 				filteredData[14] = daysID;
 				setSelectedValue(findViewById(R.id.search_carDays),
 						(String) days.getText());
 				dialog.dismiss();
 				carDaysBtn.setVisibility(View.VISIBLE);
 			}
 		});
 
 		cancel.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				dialog.dismiss();
 			}
 		});
 
 		dialog.show();
 	}
 	
 	// vavseb Days filtrs monacemebit
 	private void fillDaysDialog(Dialog dialog){
 		ArrayList<String[]> list = DBManager.getDataListFromTable(DBHelper.DAYS_TABLE);
 		RadioGroup group = (RadioGroup) dialog
 				.findViewById(R.id.dialog_generic_rdgroup);
 		String[] days;
 		for (int i = 0; i < list.size(); i++) {
 			days = list.get(i);
 			RadioButton rdbtn = new RadioButton(this);
 			rdbtn.setId(Integer.parseInt(days[0]));
            if(i == 0)
                rdbtn.setChecked(true);
 			int langID = prefs.getInt("Lang", LANG_EN);
 			switch (langID) {
 			case LANG_EN:
 				rdbtn.setText(days[LANG_EN]);
 				break;
 			case LANG_GE:
 				rdbtn.setText(days[LANG_GE]);
 				break;
 			case LANG_RU:
 				rdbtn.setText(days[LANG_RU]);
 				break;
 			default:
 				break;
 			}
 			rdbtn.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
 					LayoutParams.WRAP_CONTENT));
 			group.addView(rdbtn);
 		}
 	}
 
 	/**
 	 * Dzravis Moculobis Filtris Implementacia
 	 */
 	private void carEngineDialog() {
 		final Dialog dialog = new Dialog(ctx);
 		dialog.setContentView(R.layout.dialog_car_generic_for_spinners);
 		dialog.setTitle(DIALOG_ENGINE_TITLE_EN);
 
 		fillEngineSpinners(dialog);
 
 		Button cancel = (Button) dialog
 				.findViewById(R.id.dialog_generic_spinners_btn_cancel);
 		Button ok = (Button) dialog
 				.findViewById(R.id.dialog_generic_spinners_btn_ok);
 
 		ok.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Spinner spinner1 = (Spinner) dialog
 						.findViewById(R.id.dialog_generic_spinners_from);
 				Spinner spinner2 = (Spinner) dialog
 						.findViewById(R.id.dialog_generic_spinners_to);
 				String from = ""
 						+ (int) (Double.parseDouble((String) spinner1
 								.getSelectedItem()) * 1000);
 				String to = ""
 						+ (int) (Double.parseDouble((String) spinner2
 								.getSelectedItem()) * 1000);
 				filteredData[15] = from;
 				filteredData[16] = to;
 				setSelectedValue(findViewById(R.id.search_carEngine), from
 						+ "-" + to);
 				dialog.dismiss();
 				carEngineBtn.setVisibility(View.VISIBLE);
 			}
 		});
 
 		cancel.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				dialog.dismiss();
 			}
 		});
 
 		dialog.show();
 	}
 
 	/*
 	 * Vavseb Dzravis Moculobis Spinnerebs monacemebit
 	 */
 	private void fillEngineSpinners(Dialog dialog) {
 		Spinner from = (Spinner) dialog
 				.findViewById(R.id.dialog_generic_spinners_from);
 		Spinner to = (Spinner) dialog
 				.findViewById(R.id.dialog_generic_spinners_to);
 
 		ArrayList<String> listFrom = new ArrayList<String>();
 		ArrayList<String> listTo = new ArrayList<String>();
 
 		listFrom.add("0.05");
 		listTo.add("0.05");
 
 		for (double i = 0.1; i < 6.1; i += 0.1) {
 			listFrom.add(("" + i).substring(0, 3));
 			listTo.add(("" + i).substring(0, 3));
 		}
 
 		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(ctx,
 				android.R.layout.simple_spinner_dropdown_item, listFrom);
 		dataAdapter
 				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		from.setAdapter(dataAdapter);
 		dataAdapter = new ArrayAdapter<String>(ctx,
 				android.R.layout.simple_spinner_dropdown_item, listTo);
 		dataAdapter
 				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		to.setAdapter(dataAdapter);
 	}
 
 	/**
 	 * Customs Passed (ganbajeba) Filtris Dialogis implementacia
 	 */
 	private void carCustomsPassedDialog() {
 		final Dialog dialog = new Dialog(ctx);
 		dialog.setContentView(R.layout.dialog_car_customs_and_wheel);
 		dialog.setTitle(DIALOG_CUSTOMS_PASSED_TITLE_EN);
 
 		setCustomsAndWheelID(dialog);
 
 		Button cancel = (Button) dialog
 				.findViewById(R.id.dialog_customs_and_wheel_btn_cancel);
 		Button ok = (Button) dialog
 				.findViewById(R.id.dialog_customs_and_wheel_btn_ok);
 
 		ok.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				RadioGroup group = (RadioGroup) dialog
 						.findViewById(R.id.dialog_customs_and_wheel_rdgroup);
 				int id = group.getCheckedRadioButtonId();
 				RadioButton customs = (RadioButton) dialog.findViewById(id);
 				String customsID = "" + customs.getId();
 				filteredData[8] = customsID;
 				setSelectedValue(findViewById(R.id.search_carCustomsPassed),
 						(String) customs.getText());
 				dialog.dismiss();
 				carCustomsPassedBtn.setVisibility(View.VISIBLE);
 			}
 		});
 
 		cancel.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				dialog.dismiss();
 			}
 		});
 
 		dialog.show();
 	}
 
 	/**
 	 * DriveWheels -is implementacia .
 	 */
 	private void carDriveWheelsDialog() {
 		final Dialog dialog = new Dialog(ctx);
 		dialog.setContentView(R.layout.dialog_car_drivewheels_and_doors);
 		dialog.setTitle(DIALOG_DRIVEWHEELS_TITLE_EN);
 
 		fillDriveAndDoorsDialog(dialog, DBHelper.DRIVE_TYPES_TABLE);
 
 		Button cancel = (Button) dialog
 				.findViewById(R.id.dialog_drive_and_doors_btn_cancel);
 		Button ok = (Button) dialog
 				.findViewById(R.id.dialog_drive_and_doors_btn_ok);
 
 		ok.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				RadioGroup group = (RadioGroup) dialog
 						.findViewById(R.id.dialog_drive_and_doors_rdgroup);
 				int id = group.getCheckedRadioButtonId();
 				RadioButton drive = (RadioButton) dialog.findViewById(id);
 				String driveID = "" + drive.getId();
 				filteredData[13] = driveID;
 				setSelectedValue(findViewById(R.id.search_carDriveWheels),
 						(String) drive.getText());
 				dialog.dismiss();
 				carDriveWheelsBtn.setVisibility(View.VISIBLE);
 			}
 		});
 
 		cancel.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				dialog.dismiss();
 			}
 		});
 
 		dialog.show();
 	}
 
 	/**
 	 * Doors Filtris implementacia tavisi gilakebit.
 	 */
 	private void carDoorTypesDialog() {
 		final Dialog dialog = new Dialog(ctx);
 		dialog.setContentView(R.layout.dialog_car_drivewheels_and_doors);
 		dialog.setTitle(DIALOG_DOORS_TITLE_EN);
 
 		fillDriveAndDoorsDialog(dialog, DBHelper.DOOR_TYPES_TABLE);
 
 		Button cancel = (Button) dialog
 				.findViewById(R.id.dialog_drive_and_doors_btn_cancel);
 		Button ok = (Button) dialog
 				.findViewById(R.id.dialog_drive_and_doors_btn_ok);
 
 		ok.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				RadioGroup group = (RadioGroup) dialog
 						.findViewById(R.id.dialog_drive_and_doors_rdgroup);
 				int id = group.getCheckedRadioButtonId();
 				RadioButton door = (RadioButton) dialog.findViewById(id);
 				String doorID = "" + door.getId();
 				filteredData[12] = doorID;
 				setSelectedValue(findViewById(R.id.search_carDoors),
 						(String) door.getText());
 				dialog.dismiss();
 				carDoorsBtn.setVisibility(View.VISIBLE);
 			}
 		});
 
 		cancel.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				dialog.dismiss();
 			}
 		});
 
 		dialog.show();
 	}
 
 	/*
 	 * Door -is Dialog-s bazis monacemebit vavseb.
 	 */
 	private void fillDriveAndDoorsDialog(Dialog dialog, String tableName) {
 		ArrayList<String[]> list = DBManager.getDataListFromTable(tableName);
 		RadioGroup group = (RadioGroup) dialog
 				.findViewById(R.id.dialog_drive_and_doors_rdgroup);
 		String[] door;
 		for (int i = 0; i < list.size(); i++) {
 			door = list.get(i);
 			RadioButton rdbtn = new RadioButton(this);
 			rdbtn.setId(Integer.parseInt(door[0]));
            if(i == 0)
                rdbtn.setChecked(true);
 			int langID = prefs.getInt("Lang", LANG_EN);
 			switch (langID) {
 			case LANG_EN:
 				rdbtn.setText(door[LANG_EN]);
 				break;
 			case LANG_GE:
 				rdbtn.setText(door[LANG_GE]);
 				break;
 			case LANG_RU:
 				rdbtn.setText(door[LANG_RU]);
 				break;
 			default:
 				break;
 			}
 			rdbtn.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
 					LayoutParams.WRAP_CONTENT));
 			group.addView(rdbtn);
 		}
 	}
 
 	/**
 	 * Location Filtris implementacia
 	 */
 	private void carLocationDialog() {
 		final Dialog dialog = new Dialog(ctx);
 		dialog.setContentView(R.layout.dialog_car_location);
 		dialog.setTitle(DIALOG_LOCATION_TITLE_EN);
 
 		fillLocationDialog(dialog);
 
 		Button cancel = (Button) dialog
 				.findViewById(R.id.dialog_location_btn_cancel);
 		Button ok = (Button) dialog.findViewById(R.id.dialog_location_btn_ok);
 
 		ok.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				RadioGroup group = (RadioGroup) dialog
 						.findViewById(R.id.dialog_location_rdgroup);
 				int id = group.getCheckedRadioButtonId();
 				RadioButton location = (RadioButton) dialog.findViewById(id);
 				String locationID = "" + location.getId();
 				filteredData[11] = locationID;
 				setSelectedValue(findViewById(R.id.search_carLocation),
 						(String) location.getText());
 				dialog.dismiss();
 				carLocationBtn.setVisibility(View.VISIBLE);
 			}
 		});
 
 		cancel.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				dialog.dismiss();
 			}
 		});
 
 		dialog.show();
 	}
 
 	/*
 	 * Location-is Dialog fanjaras vavseb monacemebit
 	 */
 	private void fillLocationDialog(Dialog dialog) {
 		ArrayList<String[]> list = DBManager.getLocations();
 		RadioGroup group = (RadioGroup) dialog
 				.findViewById(R.id.dialog_location_rdgroup);
 		String[] location;
 		for (int i = 0; i < list.size(); i++) {
 			location = list.get(i);
 			RadioButton rdbtn = new RadioButton(this);
 			rdbtn.setId(Integer.parseInt(location[0]));
            if(i == 0)
                rdbtn.setChecked(true);
 			int langID = prefs.getInt("Lang", LANG_EN);
 			switch (langID) {
 			case LANG_EN:
 				rdbtn.setText(location[LANG_EN + 1]);
 				break;
 			case LANG_GE:
 				rdbtn.setText(location[LANG_GE + 1]);
 				break;
 			case LANG_RU:
 				rdbtn.setText(location[LANG_RU + 1]);
 				break;
 			default:
 				break;
 			}
 			rdbtn.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
 					LayoutParams.WRAP_CONTENT));
 			group.addView(rdbtn);
 		}
 	}
 
 	/**
 	 * Wheel Filtris Dialogis implementacia
 	 */
 	private void carWheelDialog() {
 		final Dialog dialog = new Dialog(ctx);
 		dialog.setContentView(R.layout.dialog_car_customs_and_wheel);
 		dialog.setTitle(DIALOG_WHEEL_TITLE_EN);
 
 		setCustomsAndWheelID(dialog);
 
 		Button cancel = (Button) dialog
 				.findViewById(R.id.dialog_customs_and_wheel_btn_cancel);
 		Button ok = (Button) dialog
 				.findViewById(R.id.dialog_customs_and_wheel_btn_ok);
 
 		ok.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				RadioGroup group = (RadioGroup) dialog
 						.findViewById(R.id.dialog_customs_and_wheel_rdgroup);
 				int id = group.getCheckedRadioButtonId();
 				RadioButton wheel = (RadioButton) dialog.findViewById(id);
 				String wheelID = "" + wheel.getId();
 				filteredData[9] = wheelID;
 				setSelectedValue(findViewById(R.id.search_carWheel),
 						(String) wheel.getText());
 				dialog.dismiss();
 				carWheelBtn.setVisibility(View.VISIBLE);
 			}
 		});
 
 		cancel.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				dialog.dismiss();
 			}
 		});
 
 		dialog.show();
 	}
 
 	/*
 	 * Customs Passed da Wheel Filtrebiss monacemebs vadeb shesabamis ID-s
 	 */
 	private void setCustomsAndWheelID(Dialog dialog) {
 		RadioButton yes = (RadioButton) dialog
 				.findViewById(R.id.dialog_customs_and_wheel_yes);
 		yes.setId(DIALOG_CUSTOMS_AND_WHEEL_BTN_YES_ID);
        yes.setChecked(true);
 		RadioButton no = (RadioButton) dialog
 				.findViewById(R.id.dialog_customs_and_wheel_no);
 		no.setId(DIALOG_CUSTOMS_AND_WHEEL_BTN_NO_ID);
 	}
 
 	/**
 	 * Vqmni Fuel Type -is Dialog-s, vavseb monacemebit da vamateb gilakebs
 	 */
 	private void carFuelTypesDialog() {
 		final Dialog dialog = new Dialog(ctx);
 		dialog.setContentView(R.layout.dialog_car_fuel_type);
 		dialog.setTitle(DIALOG_FUELTYPE_TITLE_EN);
 
 		fillFuelTypeDialog(dialog);
 
 		Button cancel = (Button) dialog
 				.findViewById(R.id.dialog_fuel_btn_cancel);
 		Button ok = (Button) dialog.findViewById(R.id.dialog_fuel_btn_ok);
 
 		ok.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				RadioGroup group = (RadioGroup) dialog
 						.findViewById(R.id.dialog_fuel_rdgroup);
 				int id = group.getCheckedRadioButtonId();
 				RadioButton fuel = (RadioButton) dialog.findViewById(id);
 				String fuelID = "" + fuel.getId();
 				filteredData[7] = fuelID;
 				setSelectedValue(findViewById(R.id.search_carFuel),
 						(String) fuel.getText());
 				dialog.dismiss();
 				carFuelBtn.setVisibility(View.VISIBLE);
 			}
 		});
 
 		cancel.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				dialog.dismiss();
 			}
 		});
 
 		dialog.show();
 	}
 
 	/*
 	 * Fuel Type -is Dialog fanjaras vavseb bazashi shenaxuli monacemebit
 	 */
 	private void fillFuelTypeDialog(Dialog dialog) {
 		ArrayList<String[]> fuelTypes = DBManager
 				.getDataListFromTable(DBHelper.FUEL_TABLE);
 
 		RadioGroup group = (RadioGroup) dialog
 				.findViewById(R.id.dialog_fuel_rdgroup);
 		String[] fuelType;
 		for (int i = 0; i < fuelTypes.size(); i++) {
 			fuelType = fuelTypes.get(i);
 			RadioButton rdbtn = new RadioButton(this);
 			rdbtn.setId(Integer.parseInt(fuelType[0]));
            if(i == 0)
                rdbtn.setChecked(true);
 			int langID = prefs.getInt("Lang", LANG_EN);
 			switch (langID) {
 			case LANG_EN:
 				rdbtn.setText(fuelType[LANG_EN]);
 				break;
 			case LANG_GE:
 				rdbtn.setText(fuelType[LANG_GE]);
 				break;
 			case LANG_RU:
 				rdbtn.setText(fuelType[LANG_RU]);
 				break;
 			default:
 				break;
 			}
 			rdbtn.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
 					LayoutParams.WRAP_CONTENT));
 			group.addView(rdbtn);
 		}
 	}
 
 	/**
 	 * Categoriis filtri, Vavseb Monacemebit da vuketeb implementacias
 	 */
 	private void carCategoriesDialog() {
 		final Dialog dialog = new Dialog(ctx);
 		dialog.setContentView(R.layout.dialog_car_categories);
 		dialog.setTitle(DIALOG_CATEGORIES_TITLE_EN);
 
 		fillCategoriesDialog(dialog);
 
 		Button cancel = (Button) dialog
 				.findViewById(R.id.dialog_category_btn_cancel);
 		Button ok = (Button) dialog.findViewById(R.id.dialog_category_btn_ok);
 
 		ok.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				RadioGroup group = (RadioGroup) dialog
 						.findViewById(R.id.dialog_category_rdgroup);
 				int id = group.getCheckedRadioButtonId();
 				RadioButton cat = (RadioButton) dialog.findViewById(id);
 				String catID = "" + cat.getId();
 				filteredData[10] = catID;
 				setSelectedValue(findViewById(R.id.search_carCategory),
 						(String) cat.getText());
 				dialog.dismiss();
 				carCategoryBtn.setVisibility(View.VISIBLE);
 			}
 		});
 
 		cancel.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				dialog.dismiss();
 			}
 		});
 
 		dialog.show();
 	}
 
 	/*
 	 * Categoriis Dialog fanjaras vavseb bazashi shenaxuli monacemebit
 	 */
 	private void fillCategoriesDialog(Dialog dialog) {
 		ArrayList<String[]> categories = DBManager
 				.getDataListFromTable(DBHelper.CATEGORIES_TABLE);
 
 		RadioGroup group = (RadioGroup) dialog
 				.findViewById(R.id.dialog_category_rdgroup);
 
 		String[] cat;
 		for (int i = 0; i < categories.size(); i++) {
 			cat = categories.get(i);
 			RadioButton rdbtn = new RadioButton(this);
 			rdbtn.setId(Integer.parseInt(cat[0]));
            if(i == 0)
                rdbtn.setChecked(true);
 			int langID = prefs.getInt("Lang", LANG_EN);
 			switch (langID) {
 			case LANG_EN:
 				rdbtn.setText(cat[LANG_EN]);
 				break;
 			case LANG_GE:
 				rdbtn.setText(cat[LANG_GE]);
 				break;
 			case LANG_RU:
 				rdbtn.setText(cat[LANG_RU]);
 				break;
 			default:
 				break;
 			}
 			rdbtn.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
 					LayoutParams.WRAP_CONTENT));
 			group.addView(rdbtn);
 		}
 	}
 
 	/**
 	 * Vqmni Transmissiis Dialog-s, vavseb monacemebit da vamateb gilakebs . . .
 	 */
 	private void carTransmissionDialog() {
 		final Dialog dialog = new Dialog(ctx);
 		dialog.setContentView(R.layout.dialog_car_transmission);
 		dialog.setTitle("Transmission");
 
 		fillTransmissionDialog(dialog);
 
 		Button cancel = (Button) dialog
 				.findViewById(R.id.dialog_trans_btn_cancel);
 		Button ok = (Button) dialog.findViewById(R.id.dialog_trans_btn_ok);
 
 		ok.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				RadioGroup group = (RadioGroup) dialog
 						.findViewById(R.id.dialog_trans_rdgroup);
 				int id = group.getCheckedRadioButtonId();
 				RadioButton trans = (RadioButton) dialog.findViewById(id);
 				String transmission = "" + trans.getId();
 				filteredData[6] = transmission;
 				setSelectedValue(findViewById(R.id.search_carTransmission),
 						(String) trans.getText());
 				dialog.dismiss();
 				carTransmissionBtn.setVisibility(View.VISIBLE);
 			}
 		});
 
 		cancel.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				dialog.dismiss();
 			}
 		});
 
 		dialog.show();
 	}
 
 	/*
 	 * Transmisiis Dialog fanjaras vavseb bazashi shenaxuli monacemebit
 	 */
 	private void fillTransmissionDialog(Dialog dialog) {
 		ArrayList<String[]> gears = DBManager.getGearTypes();
 
 		RadioGroup group = (RadioGroup) dialog
 				.findViewById(R.id.dialog_trans_rdgroup);
 		String[] gearTypes;
 		for (int i = 0; i < gears.size(); i++) {
 			gearTypes = gears.get(i);
 			RadioButton rdbtn = new RadioButton(this);
 			rdbtn.setId(Integer.parseInt(gearTypes[0]));
            if(i == 0)
                rdbtn.setChecked(true);
 			int langID = prefs.getInt("Lang", LANG_EN);
 			switch (langID) {
 			case LANG_EN:
 				rdbtn.setText(gearTypes[LANG_EN]);
 				break;
 			case LANG_GE:
 				rdbtn.setText(gearTypes[LANG_GE]);
 				break;
 			case LANG_RU:
 				rdbtn.setText(gearTypes[LANG_RU]);
 				break;
 			default:
 				break;
 			}
 			rdbtn.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
 					LayoutParams.WRAP_CONTENT));
 			group.addView(rdbtn);
 		}
 	}
 
 	/**
 	 * бѓ•бѓҐбѓ›бѓњбѓ� бѓ¬бѓљбѓ”бѓ‘бѓ�бѓЎ бѓ¤бѓ�бѓљбѓўбѓ бѓ�бѓЎ
 	 * бѓ“бѓ�бѓђбѓљбѓќбѓ’бѓЎ. бѓ¬бѓљбѓ”бѓ‘бѓ�бѓЎ бѓђбѓ бѓ©бѓ”бѓ•бѓђ
 	 * бѓ®бѓ“бѓ”бѓ‘бѓђ бѓЎбѓћбѓ�бѓњбѓ”бѓ бѓ”бѓ‘бѓ�бѓ—.
 	 */
 	private void carYearDialog() {
 		final Dialog dialog = new Dialog(ctx);
 		dialog.setContentView(R.layout.dialog_car_generic_for_spinners);
 		dialog.setTitle("Car Year");
 
 		fillTheSpinners(dialog);
 
 		Button cancel = (Button) dialog
 				.findViewById(R.id.dialog_generic_spinners_btn_cancel);
 		Button ok = (Button) dialog
 				.findViewById(R.id.dialog_generic_spinners_btn_ok);
 
 		ok.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Spinner spinner1 = (Spinner) dialog
 						.findViewById(R.id.dialog_generic_spinners_from);
 				Spinner spinner2 = (Spinner) dialog
 						.findViewById(R.id.dialog_generic_spinners_to);
 				String from = (String) spinner1.getSelectedItem();
 				String to = (String) spinner2.getSelectedItem();
 				filteredData[2] = from;
 				filteredData[3] = to;
 				setSelectedValue(findViewById(R.id.search_carYear), from + "-"
 						+ to);
 				dialog.dismiss();
 				carYearBtn.setVisibility(View.VISIBLE);
 			}
 		});
 
 		cancel.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				dialog.dismiss();
 			}
 		});
 
 		dialog.show();
 	}
 
 	/**
 	 * бѓ•бѓђбѓ•бѓЎбѓ”бѓ‘ бѓЎбѓћбѓ�бѓњбѓ”бѓ бѓ”бѓ‘бѓЎ бѓЎбѓђбѓ­бѓ�бѓ бѓќ
 	 * бѓ›бѓќбѓњбѓђбѓЄбѓ”бѓ›бѓ”бѓ‘бѓ�бѓ—
 	 * 
 	 * @param dialog
 	 */
 	private void fillTheSpinners(Dialog dialog) {
 		Spinner from = (Spinner) dialog
 				.findViewById(R.id.dialog_generic_spinners_from);
 		Spinner to = (Spinner) dialog
 				.findViewById(R.id.dialog_generic_spinners_to);
 
 		ArrayList<String> listFrom = new ArrayList<String>();
 		ArrayList<String> listTo = new ArrayList<String>();
 		Calendar c = Calendar.getInstance();
 		int currentYear = c.get(Calendar.YEAR);
 
 		listFrom.add("Any");
 		listTo.add("Any");
 
 		for (int i = currentYear; i >= STARTING_YEAR; i--) {
 			listFrom.add("" + i);
 			listTo.add("" + i);
 		}
 		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(ctx,
 				android.R.layout.simple_spinner_dropdown_item, listFrom);
 		dataAdapter
 				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		from.setAdapter(dataAdapter);
 		dataAdapter = new ArrayAdapter<String>(ctx,
 				android.R.layout.simple_spinner_dropdown_item, listTo);
 		dataAdapter
 				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		to.setAdapter(dataAdapter);
 	}
 
 	/**
 	 * бѓ¤бѓђбѓЎбѓ�бѓЎ бѓ¤бѓ�бѓљбѓўбѓ бѓ�бѓЎ бѓ“бѓ�бѓђбѓљбѓќбѓ’бѓ� . . .
 	 * бѓ�бѓњбѓ�бѓЄбѓ�бѓђбѓљбѓ�бѓ–бѓђбѓЄбѓ�бѓђ бѓ“бѓђ
 	 * бѓ¦бѓ�бѓљбѓђбѓ™бѓ”бѓ‘бѓ�бѓЎ бѓ�бѓ›бѓћбѓљбѓ”бѓ›бѓ”бѓњбѓўбѓђбѓЄбѓ�бѓђ.
 	 */
 	private void carPriceDialog() {
 		final Dialog dialog = new Dialog(ctx);
 		dialog.setContentView(R.layout.dialog_car_price);
 		dialog.setTitle("Car Price ($)");
 
 		Button cancel = (Button) dialog
 				.findViewById(R.id.dialog_price_btn_cancel);
 		Button ok = (Button) dialog.findViewById(R.id.dialog_price_btn_ok);
 
 		ok.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				EditText editFrom = (EditText) dialog
 						.findViewById(R.id.dialog_price_from);
 				EditText editTo = (EditText) dialog
 						.findViewById(R.id.dialog_price_to);
 				String from = editFrom.getText().toString();
 				String to = editTo.getText().toString();
 				filteredData[4] = from;
 				filteredData[5] = to;
 				setSelectedValue(findViewById(R.id.search_carPrice), from + "-"
 						+ to);
 				dialog.dismiss();
 				carPriceBtn.setVisibility(View.VISIBLE);
 			}
 		});
 
 		cancel.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				dialog.dismiss();
 			}
 		});
 
 		dialog.show();
 	}
 
 	/**
 	 * бѓ›бѓђбѓ бѓ™бѓ�бѓЎ бѓ“бѓђ бѓ›бѓќбѓ“бѓ”бѓљбѓ�бѓЎ бѓ¤бѓ�бѓљбѓўбѓ бѓ�бѓЎ
 	 * бѓЁбѓ”бѓ“бѓ”бѓ’бѓ�бѓЎ бѓ›бѓ�бѓ¦бѓ”бѓ‘бѓђ.
 	 * 
 	 * @param requestCode
 	 * @param resultCode
 	 * @param data
 	 */
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		switch (requestCode) {
 		case (MARK_FILTER):
 			if (resultCode == Activity.RESULT_OK) {
 				String[] mark = (String[]) data
 						.getSerializableExtra("Manufacturer");
 				manufacturer = mark;
 				filteredData[0] = mark[0];
 				carMarkBtn.setVisibility(View.VISIBLE);
 				((RelativeLayout) findViewById(R.id.search_carModel))
 						.setVisibility(View.VISIBLE);
 
 				((LinearLayout) ((RelativeLayout) findViewById(R.id.search_carMark))
 						.findViewById(R.id.search_selector)).findViewById(
 						R.id.search_selectedValue).setVisibility(View.VISIBLE);
 				((TextView) ((LinearLayout) ((RelativeLayout) findViewById(R.id.search_carMark))
 						.findViewById(R.id.search_selector))
 						.findViewById(R.id.search_selectedValue))
 						.setText(mark[1]);
 			}
 			break;
 		case (MODEL_FILTER):
 			if (resultCode == Activity.RESULT_OK) {
 				String[] model = (String[]) data.getSerializableExtra("Model");
 				filteredData[1] = "2" + model[0];
 				carModelBtn.setVisibility(View.VISIBLE);
 				((LinearLayout) ((RelativeLayout) findViewById(R.id.search_carModel))
 						.findViewById(R.id.search_selector)).findViewById(
 						R.id.search_selectedValue).setVisibility(View.VISIBLE);
 				((TextView) ((LinearLayout) ((RelativeLayout) findViewById(R.id.search_carModel))
 						.findViewById(R.id.search_selector))
 						.findViewById(R.id.search_selectedValue))
 						.setText(model[1]);
 			}
 			break;
 		}
 	}
 
 	/**
 	 * бѓ•бѓ�бѓ¦бѓ”бѓ‘ бѓ¦бѓ�бѓљбѓђбѓ™бѓ”бѓ‘бѓ�бѓЎ View-бѓ”бѓ‘бѓЎ бѓ“бѓђ
 	 * бѓђбѓ¦бѓ¬бѓ”бѓ бѓ�бѓљ бѓ¦бѓ�бѓљбѓђбѓ™бѓ”бѓ‘бѓЎ бѓ•бѓЈбѓ™бѓ”бѓ—бѓ”бѓ‘
 	 * бѓ�бѓњбѓ�бѓЄбѓ�бѓђбѓљбѓ�бѓ–бѓђбѓЄбѓ�бѓђбѓЎ, бѓЁбѓ”бѓ›бѓ“бѓ”бѓ’
 	 * бѓ•бѓђбѓ‘бѓђбѓ• бѓљбѓ�бѓЎбѓ”бѓњбѓ”бѓ бѓ”бѓ‘бѓЎ.
 	 */
 	private void getButtonViews() {
 		searchSubmit = (Button) findViewById(R.id.search_submit_btn);
 		carMarkBtn = (Button) findViewById(R.id.search_carMark_btn);
 		carModelBtn = (Button) findViewById(R.id.search_carModel_btn);
 		carPriceBtn = (Button) findViewById(R.id.search_carPrice_btn);
 		carYearBtn = (Button) findViewById(R.id.search_carYear_btn);
 		carCategoryBtn = (Button) findViewById(R.id.search_carCategory_btn);
 		carLocationBtn = (Button) findViewById(R.id.search_carLocation_btn);
 		carFuelBtn = (Button) findViewById(R.id.search_carFuel_btn);
 		carCustomsPassedBtn = (Button) findViewById(R.id.search_carCustomsPassed_btn);
 		carWheelBtn = (Button) findViewById(R.id.search_carWheel_btn);
 		carDaysBtn = (Button) findViewById(R.id.search_carDays_btn);
 		carTransmissionBtn = (Button) findViewById(R.id.search_carTransmission_btn);
 		carDriveWheelsBtn = (Button) findViewById(R.id.search_carDriveWheels_btn);
 		carDoorsBtn = (Button) findViewById(R.id.search_carDoors_btn);
 		carEngineBtn = (Button) findViewById(R.id.search_carEngine_btn);
 		carAbsBtn = (Button) findViewById(R.id.search_carAbs_btn);
 		carCentralLockBtn = (Button) findViewById(R.id.search_carCentralLock_btn);
 		carEsdBtn = (Button) findViewById(R.id.search_carEsd_btn);
 		carBoardCompBtn = (Button) findViewById(R.id.search_carBoardComp_btn);
 		carLeatherIntBtn = (Button) findViewById(R.id.search_carLeatherInt_btn);
 		carElWindowsBtn = (Button) findViewById(R.id.search_carElWindows_btn);
 		carAirbagsBtn = (Button) findViewById(R.id.search_carAirbags_btn);
 		carParkingControlBtn = (Button) findViewById(R.id.search_carParkingControl_btn);
 		carAlumDisksBtn = (Button) findViewById(R.id.search_carAlumDisks_btn);
 		carHatchBtn = (Button) findViewById(R.id.search_carHatch_btn);
 		carChairWarmingBtn = (Button) findViewById(R.id.search_carChairWarming_btn);
 		carNavigSystemBtn = (Button) findViewById(R.id.search_carNavigSystem_btn);
 
 		setButtonClickListeners();
 	}
 
 	private void getFilterLayoutViews() {
 		carMark = (RelativeLayout) findViewById(R.id.search_carMark);
 		carModel = (RelativeLayout) findViewById(R.id.search_carModel);
 		carPrice = (RelativeLayout) findViewById(R.id.search_carPrice);
 		carYear = (RelativeLayout) findViewById(R.id.search_carYear);
 		carCategory = (RelativeLayout) findViewById(R.id.search_carCategory);
 		carLocation = (RelativeLayout) findViewById(R.id.search_carLocation);
 		carFuel = (RelativeLayout) findViewById(R.id.search_carFuel);
 		carCustomsPassed = (RelativeLayout) findViewById(R.id.search_carCustomsPassed);
 		carWheel = (RelativeLayout) findViewById(R.id.search_carWheel);
 		carDays = (RelativeLayout) findViewById(R.id.search_carDays);
 		carTransmission = (RelativeLayout) findViewById(R.id.search_carTransmission);
 		carDriveWheels = (RelativeLayout) findViewById(R.id.search_carDriveWheels);
 		carDoors = (RelativeLayout) findViewById(R.id.search_carDoors);
 		carEngine = (RelativeLayout) findViewById(R.id.search_carEngine);
 		carAbs = (RelativeLayout) findViewById(R.id.search_carAbs);
 		carCentralLock = (RelativeLayout) findViewById(R.id.search_carCentralLock);
 		carEsd = (RelativeLayout) findViewById(R.id.search_carEsd);
 		carBoardComp = (RelativeLayout) findViewById(R.id.search_carBoardComp);
 		carLeatherInt = (RelativeLayout) findViewById(R.id.search_carLeatherInt);
 		carElWindows = (RelativeLayout) findViewById(R.id.search_carElWindows);
 		carAirbags = (RelativeLayout) findViewById(R.id.search_carAirbags);
 		carParkingControl = (RelativeLayout) findViewById(R.id.search_carParkingControl);
 		carAlumDisks = (RelativeLayout) findViewById(R.id.search_carAlumDisks);
 		carHatch = (RelativeLayout) findViewById(R.id.search_carHatch);
 		carChairWarming = (RelativeLayout) findViewById(R.id.search_carChairWarming);
 		carNavigSystem = (RelativeLayout) findViewById(R.id.search_carNavigSystem);
 
 		setFilterClickListeners();
 	}
 
 	/**
 	 * am metodit filtrebshi archeuli monacemi icereba, filtris dasaxelebis
 	 * qvevit.
 	 * 
 	 * @param parent
 	 * @param txt
 	 */
 	private void setSelectedValue(View parent, String txt) {
 		parent.findViewById(R.id.search_selector)
 				.findViewById(R.id.search_selectedValue)
 				.setVisibility(View.VISIBLE);
 		((TextView) parent.findViewById(R.id.search_selector).findViewById(
 				R.id.search_selectedValue)).setText(txt);
 	}
 
 	/**
 	 * Filtris Cancel Button-ebs yvelas erti onClickListener eqneba. garda Mark
 	 * -isa.
 	 * 
 	 * @author Jay
 	 * 
 	 */
 	private class cancelBtnListener implements OnClickListener {
 		private int[] index;
 		View parent;
 
 		public cancelBtnListener(int[] index, View parent) {
 			this.index = index;
 			this.parent = parent;
 		}
 
 		@Override
 		public void onClick(View v) {
 			for (int i = 0; i < index.length; i++) {
 				filteredData[index[i]] = null;
 			}
 			v.setVisibility(View.INVISIBLE);
 			parent.findViewById(R.id.search_selector)
 					.findViewById(R.id.search_selectedValue)
 					.setVisibility(View.GONE);
 			((TextView) parent.findViewById(R.id.search_selector).findViewById(
 					R.id.search_selectedValue)).setText("");
 		}
 	}
 
 	/**
 	 * Mark Filtris Cancel Button-is Listener
 	 * 
 	 * @author Jay
 	 * 
 	 */
 	private class cancelBtnListenerMark implements OnClickListener {
 		private int[] index;
 
 		public cancelBtnListenerMark(int[] index) {
 			this.index = index;
 		}
 
 		@Override
 		public void onClick(View v) {
 			for (int i = 0; i < index.length; i++) {
 				filteredData[index[i]] = null;
 			}
 			v.setVisibility(View.INVISIBLE);
 			((RelativeLayout) findViewById(R.id.search_carModel))
 					.setVisibility(View.GONE);
 			carModelBtn.setVisibility(View.INVISIBLE);
 			((RelativeLayout) findViewById(R.id.search_carModel))
 					.findViewById(R.id.search_selector)
 					.findViewById(R.id.search_selectedValue)
 					.setVisibility(View.GONE);
 			((TextView) (((RelativeLayout) findViewById(R.id.search_carModel))
 					.findViewById(R.id.search_selector)
 					.findViewById(R.id.search_selectedValue))).setText("");
 			((LinearLayout) ((RelativeLayout) findViewById(R.id.search_carMark))
 					.findViewById(R.id.search_selector)).findViewById(
 					R.id.search_selectedValue).setVisibility(View.GONE);
 			((TextView) ((LinearLayout) ((RelativeLayout) findViewById(R.id.search_carMark))
 					.findViewById(R.id.search_selector))
 					.findViewById(R.id.search_selectedValue)).setText("");
 		}
 	}
 
 }
