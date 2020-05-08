 package com.livestrong.myplate.back.models;
 
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import com.j256.ormlite.dao.RuntimeExceptionDao;
 import com.j256.ormlite.field.DatabaseField;
 import com.j256.ormlite.table.DatabaseTable;
 import com.livestrong.myplate.back.DataHelper;
 
 @DatabaseTable(tableName = "Diary_Food")
 public class FoodDiaryEntry extends DiaryEntry implements LiveStrongDisplayableListItem {
 	private static final long serialVersionUID = 6739814399295690139L;
 
 	public enum TimeOfDay {BREAKFAST, LUNCH, DINNER, SNACKS};
 	private enum TimeOfDayDetailed {SNACKS, BREAKFAST, MORNING_SNACK, LUNCH, AFTERNOON_SNACK, DINNER, EVENING_SNACK, MIDNIGHT_SNACK, PRE_WORKOUT, POST_WORKOUT, UNCLASSIFIED, H0, H1, H2, H3, H4, H5, H6, H7, H8, H9, H10, H11, H12, H13, H14, H15, H16, H17, H18, H19, H20, H21, H22, H23};
 	
 	public static final String ITEM_ID_FIELD_NAME = "itemId";
 	public static final String TIME_OF_DAY_FIELD_NAME = "timeOfDay";
 
 	public final static LinkedHashMap<String, Integer> servingsPickerValues = new LinkedHashMap<String, Integer>() {
 		private static final long serialVersionUID = -3381491247661807314L;
 		{
			for (int i=1; i<=10; i++) {
 	            put(i+"", i);
 			}
         };
 	};
 
 	public final static LinkedHashMap<String, Double> servingsFractionPickerValues = new LinkedHashMap<String, Double>() {
 		private static final long serialVersionUID = -8293383266276700640L;
 		{
 			put("-", 0.0);
 			put("1/4", (double) 1/4);
 			put("1/3", (double) 1/3);
 			put("1/2", (double) 1/2);
 			put("2/3", (double) 2/3);
 			put("3/4", (double) 3/4);
         };
 	};
 
 	@DatabaseField(columnName = ITEM_ID_FIELD_NAME)
 	private int itemId;
 	
 	@DatabaseField
 	private Integer mealId = null;
 	
 	@DatabaseField(columnName = TIME_OF_DAY_FIELD_NAME)
 	private TimeOfDay timeOfDay;
 	
 	@DatabaseField
 	private double servings;
 
 	@DatabaseField
 	private String label;
 	
 	@SuppressWarnings("unused")
 	private int calories; // Only used when sending manually entered entries back to the API
 
 	@DatabaseField(foreign = true, foreignAutoRefresh = true)
 	private Food food;
 
 	public FoodDiaryEntry() {}
 	
 	public FoodDiaryEntry(Food food, Integer mealId, TimeOfDay timeOfDay, double servings, Date datestamp) {
 		super(datestamp, food);
 		this.mealId = mealId;
 		this.food = food;
 		this.itemId = food.getFoodId();
 		this.timeOfDay = timeOfDay;
 		this.servings = servings;
 	}
 	
 	public String getTitle() {
 		if (label != null) {
 			return this.label;
 		}
 		return getFood().getTitle();
 	}
 
 	public String getDescription() {
 		int fractionIndex = getServingsFractionIndex();
 		String fraction = "";
 		if (fractionIndex > 0) {
 			fraction = " " + servingsFractionPickerValues.keySet().toArray(new String[servingsFractionPickerValues.size()])[fractionIndex];
 		}
 		return (int) Math.floor(getServings()) + fraction + " serving" + (getServings() != 1 ? "s" : "") + ", " + getCals() + " calories";
 	}
 
 	public Food getFood() {
 		if (this.food == null) {
 			if (this.itemId == Food.CUSTOM_FOOD_ID){ // Fetch generic calorie Food Item
 				RuntimeExceptionDao<Food, Integer> foodDao = DataHelper.getDatabaseHelper().getFoodDao();
 				try {
 					List<Food> foods = foodDao.queryBuilder().where()
 								.eq(Food.FOOD_ID_FIELD_NAME, this.itemId).and()
 								.eq(Food.CUSTOM_FIELD_NAME, false)
 								.query();
 					
 					if (foods.size() > 0){
 						this.food = foods.get(0);
 					}
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			} else { // Fetch food by ID
 				this.food = DataHelper.getFood(this.itemId, null);
 			}
 		}
 		return this.food;
 	}
 	
 	public void setTimeOfDay(String string) {
 		try {
 			if (isInteger(string)) {
 				this.timeOfDay = getSimplifiedTimeOfDay(TimeOfDayDetailed.valueOf("H" + string));
 			} else {
 				String s = string.toUpperCase().replace(" ", "_").replace("-", "_").trim();
 				if (s.length() == 0) {
 					this.timeOfDay = TimeOfDay.SNACKS;
 				} else {
 					this.timeOfDay = getSimplifiedTimeOfDay(TimeOfDayDetailed.valueOf(s));
 				}
 			}
 		} catch (Exception e) {
 			this.timeOfDay = TimeOfDay.SNACKS;
 		}
 	}
 
 	private static boolean isInteger(String string) {
 		try {
 			Integer.parseInt(string);
 			return true;
 		} catch (Exception e) {
 			return false;
 		}
 	}
 
 	private static TimeOfDay getSimplifiedTimeOfDay(TimeOfDayDetailed timeOfDay) {
 		if (timeOfDay.equals(TimeOfDayDetailed.BREAKFAST) || timeOfDay.equals(TimeOfDayDetailed.LUNCH) || timeOfDay.equals(TimeOfDayDetailed.DINNER)) {
 			return TimeOfDay.valueOf(timeOfDay.toString());
 		}
 		else if (timeOfDay.equals(TimeOfDayDetailed.H5) || timeOfDay.equals(TimeOfDayDetailed.H6) || timeOfDay.equals(TimeOfDayDetailed.H7) || timeOfDay.equals(TimeOfDayDetailed.H8) || timeOfDay.equals(TimeOfDayDetailed.H9) || timeOfDay.equals(TimeOfDayDetailed.H10)) {
 			return TimeOfDay.BREAKFAST;
 		}
 		else if (timeOfDay.equals(TimeOfDayDetailed.H11) || timeOfDay.equals(TimeOfDayDetailed.H12) || timeOfDay.equals(TimeOfDayDetailed.H13) || timeOfDay.equals(TimeOfDayDetailed.H14) || timeOfDay.equals(TimeOfDayDetailed.H15)) {
 			return TimeOfDay.LUNCH;
 		}
 		else if (timeOfDay.equals(TimeOfDayDetailed.H16) || timeOfDay.equals(TimeOfDayDetailed.H17) || timeOfDay.equals(TimeOfDayDetailed.H18) || timeOfDay.equals(TimeOfDayDetailed.H19) || timeOfDay.equals(TimeOfDayDetailed.H20) || timeOfDay.equals(TimeOfDayDetailed.H21) || timeOfDay.equals(TimeOfDayDetailed.H22) || timeOfDay.equals(TimeOfDayDetailed.H23)) {
 			return TimeOfDay.DINNER;
 		}
 		// Default to snacks
 		return TimeOfDay.SNACKS;
 	}
 
 	public void setServings(double servings) {
 		this.servings = servings;
 		wasModified();
 	}
 	
 	public int getCals() {
 		return (int) Math.round(getFood().getCals() * this.servings);
 	}
 
 	public int getFat() {
 		return (int) Math.round(getFood().getFat() * getServings());
 	}
 
 	public int getCarbs() {
 		return (int) Math.round(getFood().getCarbs() * getServings());
 	}
 
 	public int getProtein() {
 		return (int) Math.round(getFood().getProtein() * getServings());
 	}
 
 	public int getItemId() {
 		return this.itemId;
 	}
 
 	public int getMealId() {
 		return this.mealId;
 	}
 
 	public TimeOfDay getTimeOfDay() {
 		return this.timeOfDay;
 	}
 
 	public double getServings() {
 		if (getFood().isManual() && !getFood().isCustom() ) {
 			return 1.0f;
 		}
 		return this.servings;
 	}
 
 	public int getServingsWholeIndex() {
 		int index = 0;
 		for (int pickerValue : servingsPickerValues.values()) {
 			if (pickerValue == Math.floor(getServings())) {
 				return index;
 			}
 			index++;
 		}
 		return 0;
 	}
 
 	public int getServingsFractionIndex() {
 		double servingsFraction = getServings() - Math.floor(getServings());
 
 		// To know what fraction to select, we will choose the one that has the smallest difference (distance) with the (double) value from the DB
 		int index = 0;
 		Map<Integer, Double> distances = new HashMap<Integer, Double>();
 		for (double pickerFractionValue : servingsFractionPickerValues.values()) {
 			distances.put(index, Math.abs(servingsFraction - pickerFractionValue));
 			index++;
 		}
 
 		// Find the index (key) of the smallest distance
 		Entry<Integer, Double> min = null;
 		for (Entry<Integer, Double> entry : distances.entrySet()) {
 		    if (min == null || min.getValue() > entry.getValue()) {
 		        min = entry;
 		    }
 		}
 		
 		return min.getKey();
 	}
 	
 	// For serialization (to API)
 	
 	public String getSerializableTimeOfDay() {
 		return this.timeOfDay.toString().toLowerCase();
 	}
 
 	public String getSerializableServings() {
 		if (getFood().isManual()) {
 			return getCals() + "";
 		}
 		return this.servings + "";
 	}
 
 	public Integer getSerializableCalories() {
 		if (getFood().isManual()) {
 			return 1;
 		}
 		return null;
 	}
 	
 	public Food getSerializableFood() {
 		// Don't serialize this field, when it's sent to the API
 		return null;
 	}
 	
 	public String getSerializableLabel() {
 		if (getFood().isManual()) {
 			return getFood().getTitle();
 		}
 		return null;
 	}
 
 	@Override
 	public String getSmallImage() {
 		return this.food.getSmallImage();
 	}
 }
