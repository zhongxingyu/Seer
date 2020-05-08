 package com.atasoft.flangeassist;
 
 import android.content.*;
 import android.os.*;
 import android.preference.*;
 import android.support.v4.app.*;
 import android.view.*;
 import android.view.View.*;
 import android.widget.*;
 
 public class PaychequeFragment extends Fragment implements OnClickListener
 {
 	public enum DayType {
 		FIVE_WEEK, 
 		FIVE_END, 
 		FOUR_WEEK, 
 		FOUR_FRI, 
 		FOUR_END;
 	}
 	
 	double wageRates[] = new double[12];
 	Boolean oldDayToggle;
     View thisFrag;
 	Spinner sunSpin;
 	Spinner monSpin;
 	Spinner tueSpin;
 	Spinner wedSpin;
 	Spinner thuSpin;
 	Spinner friSpin;
 	Spinner satSpin;
 	
 	Spinner mealSpin;
 	Spinner loaSpin;
 	Spinner wageSpin;
 	
 	CheckBox taxVal;
 	CheckBox eiVal;
 	CheckBox cppVal;
 	CheckBox duesVal;
 	
 	CheckBox monHol;
 	CheckBox tueHol;
 	CheckBox wedHol;
 	CheckBox thuHol;
 	CheckBox friHol;
 	
 	SharedPreferences prefs;
 	Context context;
 	Boolean customDay;
 	
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
 							 Bundle savedInstanceState) {
 
         View v = inflater.inflate(R.layout.paycalc, container, false);
         thisFrag = v;
 		context = getActivity().getApplicationContext();
 
         Button bClr = (Button) v.findViewById(R.id.clr_but);
         Button bTens = (Button) v.findViewById(R.id.tens_but);
 		Button bTwelves = (Button) v.findViewById(R.id.twelves_but);
 		Button bFour = (Button) v.findViewById(R.id.four_but);
 		Button bNight = (Button) v.findViewById(R.id.night_but);
 		Button bTravel = (Button) v.findViewById(R.id.travel_but);
 		Button bDayTravel = (Button) v.findViewById(R.id.travelday_but);
 		taxVal = (CheckBox) v.findViewById(R.id.tax_val);
 		cppVal = (CheckBox) v.findViewById(R.id.cpp_val);
 		eiVal = (CheckBox) v.findViewById(R.id.ei_val);
 		duesVal = (CheckBox) v.findViewById(R.id.dues_val);
 		taxVal.setChecked(true);
 		cppVal.setChecked(true);
 		eiVal.setChecked(true);
 		duesVal.setChecked(true);
 		
 		monHol = (CheckBox) v.findViewById(R.id.hol_mon);
 		tueHol = (CheckBox) v.findViewById(R.id.hol_tue);
 		wedHol = (CheckBox) v.findViewById(R.id.hol_wed);
 		thuHol = (CheckBox) v.findViewById(R.id.hol_thu);
 		friHol = (CheckBox) v.findViewById(R.id.hol_fri);
 		
 		bClr.setOnClickListener(this);
 		bTens.setOnClickListener(this);
 		bTwelves.setOnClickListener(this);
 		bFour.setOnClickListener(this);
 		bNight.setOnClickListener(this);
 		bTravel.setOnClickListener(this);
 		bDayTravel.setOnClickListener(this);
 		taxVal.setOnClickListener(this);
 		cppVal.setOnClickListener(this);
 		eiVal.setOnClickListener(this);
 		duesVal.setOnClickListener(this);
 		monHol.setOnClickListener(this);
 		tueHol.setOnClickListener(this);
 		wedHol.setOnClickListener(this);
 		thuHol.setOnClickListener(this);
 		friHol.setOnClickListener(this);
 	
 		prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
 		
 		setupSpinners();
 		
         return v;
     }
 	
 	@Override
 	public void onResume() {
 		//Toast.makeText(context,"Thanks for using application!'!",Toast.LENGTH_LONG).show();
 		Boolean custDayCheck = prefs.getBoolean("custom_daycheck", false);
 		if(customDay != custDayCheck) updateDaySpinners(custDayCheck);
 		pushBootan();
 		super.onResume();
 		return;
 	}
 	
     @Override
     public void onClick(View v) {
         switch (v.getId()) {
 			case R.id.clr_but:
 				preSets(0);
 				break;
 			case R.id.tens_but:
 			    preSets(1);
 				break;
 			case R.id.twelves_but:
 			    preSets(2);
 				break;
 			default:  // Refreshes calc when other buttons called
 			    pushBootan();
 				break;
         }
     }
 
 	private void setupSpinners() {
         sunSpin = (Spinner) thisFrag.findViewById(R.id.sunSpin);
 		monSpin = (Spinner) thisFrag.findViewById(R.id.monSpin);
 		tueSpin = (Spinner) thisFrag.findViewById(R.id.tueSpin);
 		wedSpin = (Spinner) thisFrag.findViewById(R.id.wedSpin);
 		thuSpin = (Spinner) thisFrag.findViewById(R.id.thuSpin);
 		friSpin = (Spinner) thisFrag.findViewById(R.id.friSpin);
 		satSpin = (Spinner) thisFrag.findViewById(R.id.satSpin);
 
 		mealSpin = (Spinner) thisFrag.findViewById(R.id.meals_spin);
 		loaSpin = (Spinner) thisFrag.findViewById(R.id.loa_spin);
 		wageSpin = (Spinner) thisFrag.findViewById(R.id.wageSpin);
 
 		updateDaySpinners(prefs.getBoolean("custom_daycheck",false));
 
         ArrayAdapter<String> weekCount = new ArrayAdapter<String>(getActivity().getApplicationContext(), android.R.layout.simple_spinner_item, 
 																  new String[]{"0","1","2","3","4","5","6","7"});
         loaSpin.setAdapter(weekCount);
         mealSpin.setAdapter(weekCount);
 
 		String wageArr[] = getResources().getStringArray(R.array.wage_rates);
 		String wageStrings[] = new String[wageArr.length];
 		for(int i=0; i<wageArr.length; i++) {
 			String wageSplit[] = wageArr[i].split(",");
 			wageStrings[i] = wageSplit[0];
 			wageRates[i] = Double.parseDouble(wageSplit[1]);
 	    }
 
 		ArrayAdapter<String> wageAdapt = new ArrayAdapter<String>(getActivity().getApplicationContext(), 
 																  android.R.layout.simple_spinner_item, wageStrings);
 		wageSpin.setAdapter(wageAdapt);
 		wageSpin.setSelection(5);
 
 		sunSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
 				public void onItemSelected(AdapterView<?> parent, View view,
 										   int pos, long id) {
 					pushBootan();
 				}
 				public void onNothingSelected(AdapterView<?> parent) {
 				}
 			});
 		monSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
 				public void onItemSelected(AdapterView<?> parent, View view,
 										   int pos, long id) {
 					pushBootan();
 				}
 				public void onNothingSelected(AdapterView<?> parent) {
 				}
 			});
 		tueSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
 				public void onItemSelected(AdapterView<?> parent, View view,
 										   int pos, long id) {
 					pushBootan();
 				}
 				public void onNothingSelected(AdapterView<?> parent) {
 				}
 			});
 		wedSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
 				public void onItemSelected(AdapterView<?> parent, View view,
 										   int pos, long id) {
 					pushBootan();
 				}
 				public void onNothingSelected(AdapterView<?> parent) {
 				}
 			});
 		thuSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
 				public void onItemSelected(AdapterView<?> parent, View view,
 										   int pos, long id) {
 					pushBootan();
 				}
 				public void onNothingSelected(AdapterView<?> parent) {
 				}
 			});
 		friSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
 				public void onItemSelected(AdapterView<?> parent, View view,
 										   int pos, long id) {
 					pushBootan();
 				}
 				public void onNothingSelected(AdapterView<?> parent) {
 				}
 			});
 		satSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
 				public void onItemSelected(AdapterView<?> parent, View view,
 										   int pos, long id) {
 					pushBootan();
 				}
 				public void onNothingSelected(AdapterView<?> parent) {
 				}
 			});
 		loaSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
 				public void onItemSelected(AdapterView<?> parent, View view,
 										   int pos, long id) {
 					pushBootan();
 				}
 				public void onNothingSelected(AdapterView<?> parent) {
 				}
 			});
 		mealSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
 				public void onItemSelected(AdapterView<?> parent, View view,
 										   int pos, long id) {
 					pushBootan();
 				}
 				public void onNothingSelected(AdapterView<?> parent) {
 				}
 			});
 		wageSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
 				public void onItemSelected(AdapterView<?> parent, View view,
 										   int pos, long id) {
 					pushBootan();
 				}
 				public void onNothingSelected(AdapterView<?> parent) {
 				}
 			});
 		
 		pushBootan();
 	}
 	
 	private void updateDaySpinners(Boolean custDayCheck){
 		String[] workHrs;
 		
 		customDay = custDayCheck;
 		if(customDay && verifyCustDays()) {
 			workHrs = new String[] {"0","8","10","12","13","A","B","C"};
 		} else {
 		    workHrs = new String[] {"0","8","10","12","13"};
         	customDay = false;  //for pushbootan usage if invalid
 		}
 
 		ArrayAdapter<String> weekAd = new ArrayAdapter<String>(getActivity().getApplicationContext(),
 															   android.R.layout.simple_spinner_item, workHrs);
 		
 		monSpin.setAdapter(weekAd);
         tueSpin.setAdapter(weekAd);
         wedSpin.setAdapter(weekAd);
         thuSpin.setAdapter(weekAd);
         friSpin.setAdapter(weekAd);
         satSpin.setAdapter(weekAd);
         sunSpin.setAdapter(weekAd);
 	}
 	
 	private Boolean verifyCustDays() {
 		String[] dayKeys = {"custom_dayA", "custom_dayB", "custom_dayC"};
 		String[] dayNames = {" Day A", " Day B", " Day C"};
 		String[] dayBad = {"", "", ""};
 		int errCount = 0;
 		String toastStr = "The custom schedule";
 		for(int i = 0; i < dayKeys.length; i++) {   
 			if(!verifyDay(prefs.getString(dayKeys[i], ""))){
 				dayBad[errCount] = dayNames[i];
 				errCount++;
 			}
 		}
 		switch (errCount) {
 		    case 0:
 		        return true;
 			case 1:
 			    toastStr = toastStr + dayBad[0] +
 				    " was ";
 				break;
 			case 2:
 			    toastStr = toastStr + "s" + dayBad[0] + " and" +
 				    dayBad[1] + " were ";
 				break;
 			case 3:
 			    toastStr = toastStr + "s" + dayBad[0] + "," +
 				    dayBad[1] + ", and" + 
 					dayBad[2] + " were ";
 				break;
 		}
 		
 		toastStr = toastStr + "not entered properly. Please format as (1x,1.5x,2x) ex. \"8.5,2,1.5\"";
 		Toast.makeText(context, toastStr, Toast.LENGTH_LONG).show();
 		return false;
 	}
 	
 	private Boolean verifyDay(String testStr) {
 		String[] splitStr = testStr.split(",");
 		double[] strParse = new double[3];
 		
 		if(splitStr.length != 3) return false;
 		for(int i = 0; i < 3; i++) {
 			try{
 				strParse[i] = Double.parseDouble(splitStr[i]);
 			}
 			catch(NumberFormatException e) {
 				return false;
 			}		
 			if(strParse[i] < 0) return false;
 		}
 
 		if(strParse[0] + strParse[1] + strParse[2] > 24) return false;		
 		return true;
 	}
 
 	private void pushBootan() {
 		TextView sTimeText = (TextView) thisFrag.findViewById(R.id.sing_val);
 		TextView hTimeText = (TextView) thisFrag.findViewById(R.id.half_val);
 		TextView dTimeText = (TextView) thisFrag.findViewById(R.id.doub_val);
 		TextView grossVal = (TextView) thisFrag.findViewById(R.id.gross_val);
 		TextView exemptVal = (TextView) thisFrag.findViewById(R.id.exempt_val);
 		TextView dedVal = (TextView) thisFrag.findViewById(R.id.deduct_val);
 		TextView netVal = (TextView) thisFrag.findViewById(R.id.net_val);
 		ToggleButton fourToggle = (ToggleButton) thisFrag.findViewById(R.id.four_but);
 		ToggleButton nightToggle = (ToggleButton) thisFrag.findViewById(R.id.night_but);
 		ToggleButton travelToggle = (ToggleButton) thisFrag.findViewById(R.id.travel_but);
 		ToggleButton dayTravelToggle = (ToggleButton) thisFrag.findViewById(R.id.travelday_but);
 
 		double splitArr[] = new double[3];
 		boolean fourTens = fourToggle.isChecked();
 		double loaRate = Double.parseDouble(getString(R.string.loa_rate));
 		double mealRate = Double.parseDouble(getString(R.string.meal_rate));
 		double vacationPay = Double.parseDouble(getString(R.string.vacation_pay));
 		double travelRate = Double.parseDouble(getString(R.string.travel_rate));
 		int timeSum[] = {0,0,0};
 
 		int loaCount = Integer.parseInt(loaSpin.getSelectedItem().toString());
 		int mealCount = Integer.parseInt(mealSpin.getSelectedItem().toString());
 		double wageRate;
 		boolean[] weekHolidays = {true, monHol.isChecked(), tueHol.isChecked(), wedHol.isChecked(),thuHol.isChecked(),friHol.isChecked(), true};  //sat and sun count as holidays
 		double addTax = checkValidAddTax(prefs.getString("custom_addtax", "0"));
		double weekTravel = checkValidAddTax(prefs.getString("custom_weektravel" , "216"));
		double dayTravel = checkValidAddTax(prefs.getString("custom_daytravel", "20"));
 		addTax = checkValidAddTax(prefs.getString("custom_addtax", "0"));
 		
 		if(wageSpin.getSelectedItem().toString().contains("Custom")) {
 		    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
 			String wageKey = "custom_wage";
 
 			// use a default value using new Date()
 			wageRate = Double.parseDouble(prefs.getString(wageKey, "20"));	
 		} else {
 			wageRate = wageRates[wageSpin.getSelectedItemPosition()];
 		}
		int dayCount = 0;
 		Spinner[] spinArr = {sunSpin, monSpin, tueSpin, wedSpin, thuSpin, friSpin, satSpin};
 			for (int i = 0; i < spinArr.length; i++) {
 				String itemStr = (spinArr[i].getSelectedItem().toString());
 				if(itemStr.contains("A") || itemStr.contains("B") || itemStr.contains("C")){
 					splitArr = getCustomDayPrefs(itemStr);
 				} else {
 					DayType dayTypeSet;
 					Boolean weekEnd = weekHolidays[i];
 					if(fourTens) {
 						if(i == 5) { //fourtens friday
 							dayTypeSet = DayType.FOUR_FRI;
 						} else {
 							dayTypeSet = DayType.FOUR_WEEK;
 						}
 					} else {
 						dayTypeSet = DayType.FIVE_WEEK;
 					}
 					if(weekEnd) dayTypeSet = DayType.FIVE_END;  //works for fourtens too
 					splitArr = hrsSplit(Double.parseDouble(spinArr[i].getSelectedItem().toString()), dayTypeSet);
 				}
				if(splitArr[0] + splitArr[1] + splitArr[2] > 0) dayCount++;
				
 				timeSum[0] += splitArr[0];
 				timeSum[1] += splitArr[1];
 				timeSum[2] += splitArr[2];
 			}				
 		double grossPay = wageRate * (timeSum[0] + (1.5 * timeSum[1]) + (2 * timeSum[2]));
 
 		if(nightToggle.isChecked()) {grossPay = grossPay + (timeSum[0] + timeSum[1] + timeSum[2]) * 3;}
 
 		double grossVac = grossPay * (vacationPay + 1);
 		double exempt = loaCount * loaRate + mealCount * mealRate;
 		if(travelToggle.isChecked()) {
 			if(!prefs.getBoolean("taxable_weektravel", false)){
 				exempt += weekTravel;
 			} else {
				grossVac += weekTravel;
 			}
 		}
 		
 		if(dayTravelToggle.isChecked()) {
 			if(!prefs.getBoolean("taxable_daytravel", true)){
				exempt += dayTravel * dayCount;
 			} else {
				grossVac += dayTravel * dayCount;
 			}
 			
 		}
 		
 		double[] deductions = taxCalc(grossVac, grossPay);  //returns [fed, ab, dues, cpp, ei]
 		double deductionsSum = 0;
 		deductions[0] += addTax;
 		if(taxVal.isChecked()) deductionsSum = deductions[0] + deductions[1];
 		if(duesVal.isChecked()) deductionsSum += deductions[2];
 		if(cppVal.isChecked()) deductionsSum += deductions[3];
 		if(eiVal.isChecked()) deductionsSum += deductions[4];
 
 		
 		double netPay = grossVac - deductionsSum + exempt;
 
 		grossVal.setText("Gross: " + String.format("%.2f", grossVac) + "$");
 		exemptVal.setText("Tax Exempt: " + String.format("%.2f", exempt) + "$");
 		eiVal.setText("CPP: " + String.format("%.2f", deductions[3]) + "$");
 		cppVal.setText("EI: " + String.format("%.2f", deductions[4]) + "$");
 		duesVal.setText("Dues: " + String.format("%.2f", deductions[2]) + "$");
 		dedVal.setText("Deductions: " + String.format("%.2f", deductionsSum) + "$");
 		netVal.setText("Takehome: " + String.format("%.2f", netPay) + "$");
 		sTimeText.setText("1.0x: " + Integer.toString(timeSum[0]));
 		hTimeText.setText("1.5x: " + Integer.toString(timeSum[1]));
 		dTimeText.setText("2.0x: " + Integer.toString(timeSum[2]));
 		
 		if(addTax == 0) {
 			taxVal.setText("Income Tax: " + String.format("%.2f", deductions[0] + deductions[1]) + "$");
 		} else {
 			taxVal.setText("Income Tax: " + String.format("%.2f", deductions[0] + deductions[1] - addTax) + "$ + " +
 				String.format("%.2f", addTax) + "$");
 		}
 	}
 	
 	private double checkValidAddTax(String prefString) {
 		double retVal;
 		try {
 			retVal = Double.parseDouble(prefString);
 			}
 		catch (NumberFormatException e) {
 			return 0;
 		}
 		if(retVal > 100000 || retVal < 0) return 0;
 		
 		return retVal;
 	}
 	
 	private void preSets(int index){
 		if(index == 0) {
         	sunSpin.setSelection(0, false);
         	monSpin.setSelection(0, false);
         	tueSpin.setSelection(0, false);
         	wedSpin.setSelection(0, false);
         	thuSpin.setSelection(0, false);
         	friSpin.setSelection(0, false);
         	satSpin.setSelection(0, false);
         	mealSpin.setSelection(0, false);
         	loaSpin.setSelection(0, false);
         	return;
         } else {
 			index += 1;
         	sunSpin.setSelection(index, false);
         	monSpin.setSelection(index, false);
         	tueSpin.setSelection(index, false);
         	wedSpin.setSelection(index, false);
         	thuSpin.setSelection(index, false);
         	friSpin.setSelection(index, false);
         	satSpin.setSelection(index, false);
         	mealSpin.setSelection(0, false);
         	loaSpin.setSelection(0, false);        	
         }
         if(index == 3) {
         	mealSpin.setSelection(7, false);
         }
         return;
     }
 	
 	private double[] getCustomDayPrefs(String itemStr) {
 		String[] splitPref = new String[3];
 		double[] retDoub = new double[3];
 		
 		if(itemStr.contains("A")) splitPref = prefs.getString("custom_dayA", "0,0,0").split(",");
 		if(itemStr.contains("B")) splitPref = prefs.getString("custom_dayB", "0,0,0").split(",");
 		if(itemStr.contains("C")) splitPref = prefs.getString("custom_dayC", "0,0,0").split(",");
 		
 		for(int i = 0; i < splitPref.length; i++) {
 			retDoub[i] = Double.parseDouble(splitPref[i]);
 			
 		}
 		return retDoub;
 	}
 
 	private double[] hrsSplit(double hrs, DayType day) { 
 	//day 0 = 5-8s, 1 = 4-10s mon-thu, 2 = 4-10s fri, 3 = weekend
 		double sTime = 0;
 		double hTime = 0;
 		double dTime = 0;
 		switch(day) {
 			case FIVE_WEEK:
 				if (hrs > 10) {
 					dTime = hrs - 10;
 				}
 				if (hrs > 8) {
 					hTime = hrs - dTime - 8;
 				}
 				sTime = hrs - dTime - hTime;
 				break;
 			case FOUR_WEEK:
 				if(hrs > 10) {
 					dTime = hrs - 10;
 				}
 				sTime = hrs - dTime;
 				break;
 			case FOUR_FRI:
 				if(hrs > 10) {
 					dTime = hrs - 10;
 				}
 				hTime = hrs - dTime;
 				break;
 			default:
 				dTime = hrs;
 				break;
 		}
 		return new double[]{sTime, hTime, dTime};	
 	}
 
 	private double[] taxCalc(double gross, double grossNoVac){
 		double anGross = gross * 52;
 		double bracket[] = {0,0,0,0};
 		double diff[] = {0,0,0};
 		double rate[] = {0, 0, 0, 0};
 		double fedConst[] = {0,0,0,0};
 		double fedTax = 0;
 		double abTax = 0;
 
 		String brackStr[] = getResources().getStringArray(R.array.tax_brackets);
 		String rateStr[] = getResources().getStringArray(R.array.tax_rates);
 		String fedCStr[] = getResources().getStringArray(R.array.fed_const);
 		double fedTaxCred = Double.parseDouble(getString(R.string.fed_taxcred));
 		double abTaxCred = Double.parseDouble(getString(R.string.ab_taxcred));
 		double duesRate = Double.parseDouble(getString(R.string.dues_rate));
 		double cppRate = Double.parseDouble(getString(R.string.cpp_rate));
 		double eiRate = Double.parseDouble(getString(R.string.ei_rate));
 
 		for(int i=0; i<4; i++){
 			bracket[i] = Double.parseDouble(brackStr[i]);
 			rate[i] = Double.parseDouble(rateStr[i]);
 			fedConst[i] = Double.parseDouble(fedCStr[i]);
 
 			if(i < 3){
 				diff[i] = bracket[i + 1] - bracket [i];
 			}
 		}
 
 		if(anGross < bracket[1]) {
 			fedTax = anGross * rate[0] - fedConst[0] - fedTaxCred;
 		}
 		if(anGross < bracket[2]) {
 			fedTax = anGross * rate[1] - fedConst[1] - fedTaxCred;
 		}
 		if(anGross < bracket[3]) {
 			fedTax = anGross * rate[2] - fedConst[2] - fedTaxCred;	
 		}
 		if(anGross >= bracket[3]) {
 			fedTax = anGross * rate[3] - fedConst[3] - fedTaxCred;
 		}
 
 		abTax = (anGross * 0.1) - abTaxCred;
 		double dues = grossNoVac * duesRate;
 		double cppRet = (anGross - 3500) / 52 * cppRate;
 		if (cppRet < 0) cppRet = 0;
 		
 		if(abTax < 0){abTax = 0;}
 		if(fedTax < 0){fedTax = 0;}
 		
 		return new double[]{fedTax / 52, 
 			abTax / 52, 
 			dues, 
 			cppRet, 
 			gross * eiRate};
 	}
 }
