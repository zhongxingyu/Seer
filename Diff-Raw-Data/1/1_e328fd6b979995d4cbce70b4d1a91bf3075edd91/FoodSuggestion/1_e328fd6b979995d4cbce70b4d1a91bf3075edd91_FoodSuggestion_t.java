 package com.innutrac.poly.innutrac;
 
 import java.util.ArrayList;
 
 // guidelines for food recommendation engine
 // When queuing from database:
 // -Must be able to recommend food with similar nutrition value
 // 	(Not sure if possible with current database) should have similar qualities to the food the user has inputed so far
 // When filtering results:
 // -Food recommended should not have nutrition values that exceed the users set limits
 // -Food recommended should be the best choice possible to meet users diet needs
 
 // Food recommendation engine should address deficiencies in user's diet
 //    -For example, if users diet is lacking in nutrient in a particular area for days at a 
 //     time this deficiency will accumulate. Engine should recommend food to help user get back
 //     on track for a healthy diet.
 //	  -Limit the amount the foods recommended (maybe like 4?) as to not overwhelm the user	  
 // EXTRA: Food Engine Could help user put together a diet plan according to nutrient goals or calorie limit
 //        set by user.
 //exclude sugar, chole, sat_fat, sodium
 
 
 public class FoodSuggestion {
 	DailyPlan todayDailyPlan; // class with aggregated data of the food user has
 								// eaten today
 	DailyPlan tdp;
 	ArrayList<DailyPlan> totalDailyPlan; // database with all food user has ever
 											// eaten
 	String lowestNutrient;
 	double lowestValue;
 
 	double maxCalorieValue;
 	double highestRangeValue;
 
 	public FoodSuggestion(DailyPlan todayDailyPlan,
 			ArrayList<DailyPlan> totalDailyPlan) {
 		this.todayDailyPlan = todayDailyPlan;
 
 		this.tdp = todayDailyPlan;
 
 		this.totalDailyPlan = totalDailyPlan;
 		lowestNutrient = "";
 		maxCalorieValue = 0;
 		highestRangeValue = 0;
 
 		findLow();
 	}
 
 	public void findLow() {
 		double carbDef = (tdp.getTotalCarbohydrate() - tdp
 				.getCurrentCarbohydrate()) / tdp.getTotalCarbohydrate();
 		double cholDef = (tdp.getTotalCholesterol() - tdp
 				.getCurrentCholesterol()) / tdp.getTotalCholesterol();
 		double unSatFatDef = (tdp.getTotalUnsatFats() - tdp
 				.getCurrentUnsatFats()) / tdp.getTotalUnsatFats();
 		double fibDef = (tdp.getTotalFiber() - tdp.getCurrentFiber())
 				/ tdp.getTotalFiber();
 		double protDef = (tdp.getTotalProtein() - tdp.getCurrentProtein())
 				/ tdp.getTotalProtein();
 
 		double tmpLowest = 100;
 
 		if (carbDef < tmpLowest) {
 			tmpLowest = carbDef;
 
 			this.lowestNutrient = "carbs";
 			this.lowestValue = tdp.getTotalCarbohydrate()
 					- tdp.getCurrentCarbohydrate();
 		}
 		if (cholDef < tmpLowest) {
 			tmpLowest = cholDef;
 
 			this.lowestNutrient = "cholesterol";
 			this.lowestValue = tdp.getTotalCholesterol()
 					- tdp.getCurrentCholesterol();
 		}
 		if (unSatFatDef < tmpLowest) {
 			tmpLowest = unSatFatDef;
 
 			this.lowestNutrient = "fat_unsaturated";
 			this.lowestValue = tdp.getTotalUnsatFats()
 					- tdp.getCurrentUnsatFats();
 		}
 		if (fibDef < tmpLowest) {
 			tmpLowest = fibDef;
 
 			this.lowestNutrient = "fiber";
 			this.lowestValue = tdp.getTotalFiber() - tdp.getCurrentFiber();
 		}
 		if (protDef < tmpLowest) {
 			tmpLowest = protDef;
 
 			this.lowestNutrient = "protein";
 			this.lowestValue = tdp.getTotalProtein() - tdp.getCurrentProtein();
 		}
		this.lowestValue = Math.abs(this.lowestValue);
 	}
 
 	public String getLowestString() {
 		return this.lowestNutrient;
 	}
 
 	public double getLowestValue() {
 		return this.lowestValue;
 	}
 
 	public void init() {
 		findlowestNutrient();
 		setHighRangeValue();
 		setMaxCalorieValue();
 	}
 
 	public String getLowestNutrient() {
 		return lowestNutrient;
 	}
 
 	public double getMaxCalorieValue() {
 		return maxCalorieValue;
 	}
 
 	public double getHighRangeValue() {
 		return highestRangeValue;
 	}
 
 	private void findlowestNutrient() {
 		double lowestNutrient = 100000;
 
 		// if(totalDailyPlan.size() == 0)
 		// temp = todayDailyPlan;
 		for (int i = 0; i < totalDailyPlan.size(); i++) {
 			DailyPlan temp = totalDailyPlan.get(i);
 
 			if (temp.getCurrentSatFats() < lowestNutrient) {
 				lowestNutrient = temp.getCurrentSatFats();
 				this.lowestNutrient = "satFats";
 			}
 
 			else if (temp.getCurrentCarbohydrate() < lowestNutrient) {
 				lowestNutrient = temp.getCurrentCarbohydrate();
 				this.lowestNutrient = "carbohydrate";
 			}
 
 			else if (temp.getCurrentProtein() < lowestNutrient) {
 				lowestNutrient = temp.getCurrentProtein();
 				this.lowestNutrient = "protein";
 			}
 
 			else if (temp.getCurrentSodium() < lowestNutrient) {
 				lowestNutrient = temp.getCurrentSodium();
 				this.lowestNutrient = "sodium";
 			}
 
 			else if (temp.getCurrentCholesterol() < lowestNutrient) {
 				lowestNutrient = temp.getCurrentCholesterol();
 				this.lowestNutrient = "cholesterol";
 			}
 
 			else if (temp.getCurrentUnsatFats() < lowestNutrient) {
 				lowestNutrient = temp.getCurrentUnsatFats();
 				this.lowestNutrient = "unsatFats";
 			}
 
 			else if (temp.getCurrentSugar() < lowestNutrient) {
 				lowestNutrient = temp.getCurrentSugar();
 				this.lowestNutrient = "sugar";
 			}
 
 			else if (temp.getCurrentFiber() < lowestNutrient) {
 				lowestNutrient = temp.getCurrentFiber();
 				this.lowestNutrient = "fiber";
 			}
 		}
 	}
 
 	private void setHighRangeValue() {
 
 		if (lowestNutrient.equals("satFats"))
 			highestRangeValue = todayDailyPlan.getTotalSatFats()
 					- todayDailyPlan.getCurrentSatFats();
 		if (lowestNutrient.equals("carbohydrate"))
 			highestRangeValue = todayDailyPlan.getTotalCarbohydrate()
 					- todayDailyPlan.getCurrentCarbohydrate();
 		if (lowestNutrient.equals("cholesterol"))
 			highestRangeValue = todayDailyPlan.getTotalCholesterol()
 					- todayDailyPlan.getCurrentCholesterol();
 		if (lowestNutrient.equals("protein"))
 			highestRangeValue = todayDailyPlan.getTotalProtein()
 					- todayDailyPlan.getCurrentProtein();
 		if (lowestNutrient.equals("sodium"))
 			highestRangeValue = todayDailyPlan.getTotalCholesterol()
 					- todayDailyPlan.getCurrentCholesterol();
 		if (lowestNutrient.equals("unsatFats"))
 			highestRangeValue = todayDailyPlan.getTotalUnsatFats()
 					- todayDailyPlan.getCurrentUnsatFats();
 		if (lowestNutrient.equals("sugar"))
 			highestRangeValue = todayDailyPlan.getTotalSugar()
 					- todayDailyPlan.getCurrentSugar();
 		if (lowestNutrient.equals("fiber"))
 			highestRangeValue = todayDailyPlan.getTotalFiber()
 					- todayDailyPlan.getCurrentFiber();
 	}
 
 	private void setMaxCalorieValue() {
 		maxCalorieValue = todayDailyPlan.getTotalCalories()
 				- todayDailyPlan.getCurrentCalories();
 	}
 }
