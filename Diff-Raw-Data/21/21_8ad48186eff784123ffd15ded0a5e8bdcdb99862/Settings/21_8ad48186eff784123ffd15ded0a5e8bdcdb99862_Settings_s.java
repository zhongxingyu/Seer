 package excel.sheet;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.ss.usermodel.Workbook;
 import org.joda.time.LocalDate;
 
 import enums.CFSplit;
 import enums.ActivityMultiplier;
 import enums.Gender;
 import enums.UnitSystem;
 import excel.CellBuilder;
 import excel.CellStyles;
 import excel.Excel;
 import excel.sheet.cell.Formulas;
 
 /**
  * Returns a Settings sheet.
  * This holds all teh daily information, as well as constants
  * 
  * @author Mikkel Laursen
  *
  */
 public class Settings {
 	
 	public static final int PIXELS_8 = 28*80;
 	public static final int PIXELS_6 = 28*60;
 	public static final short DOW, DATE, EWMA5, EWMA7, WEIGHT, SMOOTHED, FORECAST, RESIDUAL, LOST_WEEK;
 	public static final short TREND, SLOPE, CFSPLIT, CHANGE, BMR, TDEE, BMR_, CALORIES, PMULT, PROTEIN;
 	
 	public static final short CONSTANTS, BIRTHDAY, HEIGHT, ACTIVITY, UNITS, GENDER, ALPHA;
 	public static final short ACTIVITIES, SPLITS, FAT, CARBS, ACTIVITY_VAL;
 	
 	public static final short CONSTANTS_ROW_NAMES = 1;
 	public static final short CONSTANTS_ROW = 2;
 	public static final short SEDENTARY = 2;
 	public static final short LIGHTLY_ACTIVE = 3;
 	public static final short MODERATELY_ACTIVE = 4;
 	public static final short VERY_ACTIVE = 5;
 	public static final short EXTREMELY_ACTIVE = 6;
 	public static final short SPLITS_START = 8;
 	
 	public static final List<Short> NON_NUMBER_FIELDS = new ArrayList<Short>();
 	public static final List<Short> NUMBER = new ArrayList<Short>();
 	public static final List<Short> NUMBER1 = new ArrayList<Short>();
 	public static final List<Short> NUMBER2 = new ArrayList<Short>();
 	public static final List<Short> NUMBER3 = new ArrayList<Short>();
 	public static final List<Short> NUMBER4 = new ArrayList<Short>();
 	public static final List<Short> TITLES_RIGHT_BORDER = new ArrayList<Short>();
 	public static final List<Short> TITLES = new ArrayList<Short>();
 	static {
 		short i = 0;
 		DOW = i++;
 		DATE = i++;
 		WEIGHT = i++;
 		EWMA5 = i++;
 		EWMA7 = i++;
 		SMOOTHED = i++;
 		FORECAST = i++;
 		RESIDUAL = i++;
 		LOST_WEEK = i++;
 		TREND = i++;
 		SLOPE = i++;
 		CFSPLIT = i++;
 		CHANGE = i++;
 		BMR = i++;
 		TDEE = i++;
 		BMR_ = i++;
 		CALORIES = i++;
 		PMULT = i++;
 		PROTEIN = i++;
 		i++;
 		CONSTANTS = i;
 		BIRTHDAY = i++;
 		HEIGHT = i++;
 		ACTIVITY = i++;
 		UNITS = i++;
 		GENDER = i++;
 		ALPHA = i++;
 		i++;
 		ACTIVITIES = i;
 		SPLITS = i++;
 		ACTIVITY_VAL = i;
 		CARBS = i++;
 		FAT = i++;
 		
 		TITLES_RIGHT_BORDER.add(WEIGHT);
 		TITLES_RIGHT_BORDER.add(EWMA7);
 		TITLES_RIGHT_BORDER.add(RESIDUAL);
 		TITLES_RIGHT_BORDER.add(LOST_WEEK);
 		TITLES_RIGHT_BORDER.add(SLOPE);
 		TITLES_RIGHT_BORDER.add(CFSPLIT);
 		TITLES_RIGHT_BORDER.add(CHANGE);
 		TITLES_RIGHT_BORDER.add(TDEE);
 		TITLES_RIGHT_BORDER.add(BMR_);
 		TITLES_RIGHT_BORDER.add(CALORIES);
 		TITLES_RIGHT_BORDER.add(PMULT);
 		TITLES_RIGHT_BORDER.add(PROTEIN);
 		
 		TITLES.addAll(TITLES_RIGHT_BORDER);
 		TITLES.add(DOW);
 		TITLES.add(DATE);
 		TITLES.add(EWMA5);
 		TITLES.add(SMOOTHED);
 		TITLES.add(FORECAST);
 		TITLES.add(BMR);
 		TITLES.add(TREND);
 		TITLES.add(CONSTANTS);
 		TITLES.add(ACTIVITIES);
 		
 		NUMBER.add(CHANGE);
 		NUMBER2.add(HEIGHT);
 		NUMBER1.add(WEIGHT);
 		NUMBER2.add(EWMA5);
 		NUMBER2.add(EWMA7);
 		NUMBER2.add(TREND);
 		NUMBER2.add(SLOPE);
 		NUMBER2.add(BMR);
 		NUMBER2.add(TDEE);
 		NUMBER2.add(CALORIES);
 		NUMBER2.add(PMULT);
 		NUMBER2.add(PROTEIN);
 		NUMBER2.add(CARBS);
 		NUMBER2.add(FAT);
 		NUMBER3.add(ACTIVITY_VAL);
 		NUMBER4.add(SMOOTHED);
 		NUMBER4.add(FORECAST);
 		NUMBER4.add(RESIDUAL);
 	}
 	
 	public static final int DEFAULT_NUM_ROWS = 12*7; // 12 weeks
 	public static final double DEFAULT_HEIGHT = 71.0;
 	public static final int DEFAULT_AGE = 22;
 	
 	public static String REF_SETTINGS = "Settings!";
 	
 	private Workbook wb;
 	public Settings(Workbook wb) {
 		this.wb = wb;
 	}
 
 	
 	public Sheet createSettingsSheet() {
 		Sheet s = this.wb.createSheet("Settings");
 		Row titles = s.createRow(0);
 		createCell(titles, DOW, "DOW");
 		createCell(titles, DATE, "Date");
 		createCell(titles, WEIGHT, "Weight");
 		createCell(titles, EWMA5, "EWMA5");
 		createCell(titles, EWMA7, "EWMA7");
 		createCell(titles, SMOOTHED, "Smoothed");
 		createCell(titles, FORECAST, "Forecast");
 		createCell(titles, RESIDUAL, "Residual");
 		createCell(titles, LOST_WEEK, "Lost/wk");
 		createCell(titles, TREND, "Trend");
 		createCell(titles, SLOPE, "Slope");
 		createCell(titles, CFSPLIT, "C/F Split");
 		createCell(titles, CHANGE, "Change");
 		createCell(titles, BMR, "BMR");
 		createCell(titles, TDEE, "TDEE");
 		createCell(titles, BMR_, "BMR?");
 		createCell(titles, CALORIES, "Calories");
 		createCell(titles, PMULT, "P*");
 		createCell(titles, PROTEIN, "Protein");
 		createCell(titles, CONSTANTS, "Assumed Constants");
 		createCell(titles, ACTIVITIES, "Activity Names");
 		
 		
 		LocalDate d = new LocalDate();
 		for(int i = Excel.DATA_START; i <= DEFAULT_NUM_ROWS; i++) {
 			Row r = s.createRow(i);
 			createFormulaCell(r, DOW, Formulas.dowFormula(r.getRowNum()));
 			createCell(r, DATE, d);
 			d = d.plusDays(1);
 			createCell(r, WEIGHT, "");
 			createCell(r, EWMA5, "");
 			createCell(r, EWMA7, "");
 			if(i > 5) {
 				createFormulaCell(r, SMOOTHED, Formulas.smoothed(i));
 				createFormulaCell(r, FORECAST, Formulas.forecast(i));
 				createFormulaCell(r, RESIDUAL, Formulas.residual(i));
 			}
 			else {
 				createCell(r, SMOOTHED, "");
 				createCell(r, FORECAST, "");
 				createCell(r, RESIDUAL, "");
 			}
 			
 			if(i > 12) {
 				createFormulaCell(r, LOST_WEEK, Formulas.lostPerWeek(i));
 			}
 			else {
 				createCell(r, LOST_WEEK, "");
 			}
 			
 			createFormulaCell(r, TREND, Formulas.trend(i));
 			if(i > Excel.DATA_START) {
 				createFormulaCell(r, SLOPE, Formulas.slope(i));
 			}
 			else {
 				createCell(r, SLOPE, "");
 			}
 			
 			createCell(r, CFSPLIT, ActivityMultiplier.SEDENTARY.getActivityMultiplierName());
 			createCell(r, CHANGE, 0);
 			createFormulaCell(r, BMR, Formulas.bmr(i));
 			createFormulaCell(r, TDEE, Formulas.tdee(i));
 			createCell(r, BMR_, "Y");
 			createFormulaCell(r, CALORIES, Formulas.calcCals(i));
 			createCell(r, PMULT, 1);
 			createFormulaCell(r, PROTEIN, Formulas.calcProtein(i));
 		
 		}
 		
 		Row cNames = s.getRow(CONSTANTS_ROW_NAMES);
 		Row cVals = s.getRow(CONSTANTS_ROW);
 		createCell(cNames, BIRTHDAY, "Birthday");
 		createCell(cNames, HEIGHT, "Height");
 		createCell(cNames, ACTIVITY, "Activity Multiplier");
 		createCell(cNames, UNITS, "Units");
 		createCell(cNames, GENDER, "Gender");
 		createCell(cNames, ALPHA, "alpha");
 		createCell(cVals, BIRTHDAY, "1/1/1990");
 		createCell(cVals, HEIGHT, 0);
 		createCell(cVals, ACTIVITY, ActivityMultiplier.SEDENTARY.getActivityMultiplierName());
 		createCell(cVals, UNITS, UnitSystem.IMPERIAL);
 		createCell(cVals, GENDER, Gender.MALE.getGenderName());
 		createCell(cVals, ALPHA, 0.3);
 		
 		Excel.createDropDown(s, new String[]{"Y", "N" }, BMR_);
 		Excel.createDropDown(s, ActivityMultiplier.ACTIVITY_MULTIPLIER, CFSPLIT);
 		Excel.createDropDown(s, ActivityMultiplier.ACTIVITY_MULTIPLIER, CONSTANTS_ROW, ACTIVITY);
 		Excel.createDropDown(s, UnitSystem.UNIT_SYSTEM, CONSTANTS_ROW, UNITS);
 		Excel.createDropDown(s, Gender.GENDER, CONSTANTS_ROW, GENDER);
 		
 		for(ActivityMultiplier a : ActivityMultiplier.values()) {
 			Row r = s.getRow(a.asRowNum());
 			createCell(r, ACTIVITIES, a.getActivityMultiplierName());
 			createCell(r, ACTIVITY_VAL, a.getActivityMultiplier());
 		}
 		
 		Row splitTable = s.getRow(SPLITS_START);
 		createCell(splitTable, SPLITS, "Split");
 		createCell(splitTable, CARBS, "Carbs");
 		createCell(splitTable, FAT, "Fat");
 		
 		for(CFSplit cfs : CFSplit.values()) {
 			Row r = s.getRow(cfs.asRowNum());
 			createCell(r, SPLITS, cfs.getSplitName());
 			createCell(r, CARBS, cfs.getCarb());
 			createCell(r, FAT, cfs.getFat());
 		}
 		
		CellStyles.applyConditionalFormat(s, CellStyles.BOLD);
 		Excel.autosizeCols(s);
 		s.setColumnWidth(BMR, PIXELS_8);
 		s.setColumnWidth(TDEE, PIXELS_8);
 		s.setColumnWidth(CALORIES, PIXELS_8);
 		s.setColumnWidth(TREND, PIXELS_6);
 		s.setColumnWidth(SLOPE, PIXELS_6);
 		s.setColumnWidth(BIRTHDAY, PIXELS_8);
 		s.setColumnWidth(HEIGHT, PIXELS_8);
 		return s;
 	}
 	
 	
 	public Cell createFormulaCell(Row r, short n, String f) {
 		return createCell(CellBuilder.createFormulaCell(r, n, f), n, r.getRowNum());
 	}
 	
 	public Cell createCell(Row r, short n, Object v) {
 		int rn = r.getRowNum();
 		Cell c = CellBuilder.createCell(r, n, v);
 		if(rn >= Excel.DATA_START) {
 			if(n == DATE) {
 				c = CellBuilder.createPrevPlus1Cell(r, n);
 				if(rn == Excel.DATA_START) {
 					c = r.createCell(n); 
 					c.setCellValue(((LocalDate) v).toDate());
 				}
 			}
 			else if(n == EWMA5 || n == EWMA7) {
 				int x = n == EWMA5 ? 5 : 7;
 				if(rn > x)
 					c = CellBuilder.createFormulaCell(r, n, Formulas.ewmaFormula(rn, x));
 			}
 			else if(n == CHANGE && ((rn-1) % 7 != 0)) {
 				c = CellBuilder.createPrevCell(r, n);
 			}
 		}
 		return createCell(c, n, rn);
 	}
 	public Cell createCell(Cell c, short n, int rn) {
 		Set<Short> styles = addStyles(n, rn);
 		CellStyles.applyStyles(styles, c);
 		return c;
 	}
 	
 	public Set<Short> addStyles(short n, int rn) {
 		Set<Short> styles = new HashSet<Short>();
 		if(NUMBER.contains(n))
 			styles.add(CellStyles.NUMBER);
 		
 		if(NUMBER1.contains(n))
 			styles.add(CellStyles.NUMBER_1);
 		
 		if(NUMBER2.contains(n))
 			styles.add(CellStyles.NUMBER_2);
 		
 		if(NUMBER3.contains(n) && rn < 8)
 			styles.add(CellStyles.NUMBER_3);
 		
 		if(NUMBER4.contains(n))
 			styles.add(CellStyles.NUMBER_4);
 		
 		if(TITLES.contains(n) && rn == Excel.DATA_START - 1) {
 			styles.add(CellStyles.BOLD);
 			styles.add(CellStyles.BORDER_BOTTOM);
 		}
 		
 		if(TITLES_RIGHT_BORDER.contains(n)) {
 			styles.add(CellStyles.BORDER_RIGHT);
 		}
 		
 		if(rn % 7 == 0 && n <= PROTEIN)
 			styles.add(CellStyles.BORDER_BOTTOM);
 		
 		if(n == EWMA5 || n == EWMA7) {
 			int e = n == EWMA5 ? 5 : 7;
 			if(rn >= Excel.DATA_START && (rn <= 5 || (e == 7 && rn <= e))) 
 				styles.add(CellStyles.GRAY_FILL);
 			if(e == 5 && rn > 5 && rn < 8)
 				styles.add(CellStyles.BORDER_RIGHT_THIN);
 			
 			if(e == 7)
 				styles.add(CellStyles.BORDER_RIGHT);
 			
 			if(rn == e && e == 5)
 				styles.add(CellStyles.BORDER_BOTTOM_THIN);
 		}
 		else if(n == SMOOTHED || n == FORECAST || n == RESIDUAL) {
 			if(rn <= 5 && rn >= Excel.DATA_START)
 				styles.add(CellStyles.GRAY_FILL);
 			
 			if(rn == 5)
 				styles.add(CellStyles.BORDER_BOTTOM_THIN);
 			
 			if(n == RESIDUAL)
 				styles.add(CellStyles.BORDER_RIGHT);
 		}
 		else if(n == LOST_WEEK) {
 			styles.add(CellStyles.BORDER_RIGHT);
 			if(rn <= 12 && rn >= Excel.DATA_START)
 				styles.add(CellStyles.GRAY_FILL);
 		}
 		else if(n == DATE) {
 			styles.add(CellStyles.DATE);
 		}
 		else if(n >= BIRTHDAY && n <= ALPHA) {
 			if(rn == CONSTANTS_ROW_NAMES) {
 				styles.add(CellStyles.BORDER_TOP);
 				styles.add(CellStyles.BORDER_BOTTOM_THIN);
 			}
 			else {
 				styles.add(CellStyles.BORDER_TOP_THIN);
 				styles.add(CellStyles.BORDER_BOTTOM);
 			}
 			
 			if(n == ALPHA) {
 				styles.add(CellStyles.BORDER_RIGHT);
 			}
 			else {
 				if(n == BIRTHDAY) {
 					styles.add(CellStyles.BORDER_LEFT);
 				}
 				styles.add(CellStyles.BORDER_RIGHT_THIN);
 			}		
 		}
 		else if(n >= ACTIVITIES && n <= FAT) {
 			if(rn <= EXTREMELY_ACTIVE) {
 				if(rn < Excel.DATA_START) {
 					styles.add(CellStyles.BORDER_ALL);
 				}
 				else {
 					if(rn == Excel.DATA_START) {
 						styles.add(CellStyles.BORDER_TOP);
 					}
 					
 					if(n == ACTIVITIES) {
 						styles.add(CellStyles.BORDER_LEFT);
 						styles.add(CellStyles.BORDER_RIGHT_THIN);
 					}
 					else {
 						styles.add(CellStyles.BORDER_LEFT_THIN);
 						styles.add(CellStyles.BORDER_RIGHT);
 					}
 					
 					if(rn < EXTREMELY_ACTIVE-1) {
 						styles.add(CellStyles.BORDER_BOTTOM_THIN);
 					}
 					else {
 						styles.add(CellStyles.BORDER_BOTTOM);
 					}
 				}
 			}
 			else {
 				if(rn == SPLITS_START) {
 					styles.add(CellStyles.BORDER_ALL);
 					styles.add(CellStyles.BOLD);
 				}
 				else {
 					if(n == SPLITS) {
 						styles.add(CellStyles.BORDER_LEFT);
 						styles.add(CellStyles.BORDER_RIGHT_THIN);
 					}
 					else {
 						styles.add(CellStyles.BORDER_LEFT_THIN);
 						if(n == FAT)
 							styles.add(CellStyles.BORDER_RIGHT);
 					}
 					if(rn < CFSplit.values()[CFSplit.values().length -1 ].asRowNum()) {
 						styles.add(CellStyles.BORDER_BOTTOM_THIN);
 					}
 					else {
 						styles.add(CellStyles.BORDER_BOTTOM);
 					}
 				}
 			}
 			
 		}
 		return styles;
 	}
 	
 	public static String getCol(short r) {
 		return "$" + Excel.rowToLetter(r);
 	}
 	
 	public static String getConst(short c) {
 		return "$" + Excel.rowToLetter(c) + "$" + (CONSTANTS_ROW + 1);
 	}
 	
 	
 	
 	
 	
 	
 	
 
 	/*
 	public static User extractSettings(Workbook wb, int row) {
 		Sheet s = wb.getSheet("Settings");
 		
 		Row constantsRow = s.getRow(Excel.DATA_START);
 		row = row / 6 + 1;
 		
 		/*
 		Row updatingRow = s.getRow(row);
 		double weight = updatingRow.getCell(WEIGHT).getNumericCellValue();
 		double height = constantsRow.getCell(HEIGHT).getNumericCellValue();
 		double age = constantsRow.getCell(AGE).getNumericCellValue();
 		UnitSystem units = UnitSystem.parseRichTextString(constantsRow.getCell(UNIT_SYSTEM).getRichStringCellValue());
 		ActivityMultiplier activity = ActivityMultiplier.parseRichTextString(constantsRow.getCell(ACTIVITY_MULTIPLIER).getRichStringCellValue());
 		CalorieSplit calSplit = CalorieSplit.parseRichTextString(updatingRow.getCell(CALORIE_SPLIT).getRichStringCellValue());
 		DayType type = DayType.parseRichTextString(updatingRow.getCell(DAY_TYPE).getRichStringCellValue());
 		CarbFatSplit carbFatSplit = CarbFatSplit.parseRichTextString(updatingRow.getCell(CARB_FAT_SPLIT).getRichStringCellValue());
 		Formula f = Formula.parseRichTextString(constantsRow.getCell(FORMULA).getRichStringCellValue());
 		Sex sex = Sex.parseRichTextString(constantsRow.getCell(SEX).getRichStringCellValue());
 		*/
 	//	return null; //new User(weight, height, (int) age, units, type, carbFatSplit, activity, calSplit, f, sex);
 	//}
 }
