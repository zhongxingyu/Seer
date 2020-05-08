 /**
  * Copyright (C) 2009, 2010 SC 4ViewSoft SRL
  *  
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *  
  *      http://www.apache.org/licenses/LICENSE-2.0
  *  
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package finalProject.gregMo;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.TimeZone;
 
 import org.achartengine.ChartFactory;
 import org.achartengine.chart.BarChart.Type;
 import org.achartengine.renderer.SimpleSeriesRenderer;
 import org.achartengine.renderer.XYMultipleSeriesRenderer;
 import org.achartengine.renderer.XYMultipleSeriesRenderer.Orientation;
 
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.graphics.Paint.Align;
 import finalProject.gregMo.database.DateTable;
 import finalProject.gregMo.database.FoodTable;
 import finalProject.gregMo.database.NutritionContentProvider;
 import finalProject.gregMo.database.PersonalInformationTable;
 
 /**
  * Measurements bar chart.
  */
 public class MeasurementsBarChart extends AbstractChart {
 
   public String getName() {
     return "Measurements horizontal bar chart";
   }
 
   /**
    * Returns the chart description.
    * 
    * @return the chart description
    */
   public String getDesc() {
     return "The monthly sales for the last 2 years (horizontal bar chart)";
   }
 
   /**
    * Executes the chart.
    * 
    * @param context the context
    * @return the built intent
    */
   public Intent execute(Context context) {
    String[] titles = new String[] { "My Intake", "Recommended Intake" };
 
 	String[] projection = { 
 			DateTable.COLUMN_ID 
 	};
 	
 	int id = -1;
 	
 	GregorianCalendar today = new GregorianCalendar();
 	SimpleDateFormat date_format = new SimpleDateFormat("yyyyMMdd");
 	date_format.setTimeZone(TimeZone.getTimeZone("EST"));
 	
 	String date = date_format.format(today.getTime());
 	
 	Cursor cursor = context.getContentResolver().query(NutritionContentProvider.CONTENT_URI_DATE, projection, DateTable.COLUMN_DATE + " = ?", new String[] { date }, null);
 	if (cursor != null) {
 		cursor.moveToFirst();
 
 		id = cursor.getInt(cursor.getColumnIndexOrThrow(DateTable.COLUMN_ID));
 		
 		cursor.close();
 	}
 
 	projection = new String[] {
 			FoodTable.COLUMN_NAME,
 			FoodTable.COLUMN_FOOD_CALORIES,
 			FoodTable.COLUMN_FOOD_FATG,
 			FoodTable.COLUMN_FOOD_CARBSG,
 			FoodTable.COLUMN_FOOD_PROTEING,
 			FoodTable.COLUMN_FOOD_FIBERG,
 			FoodTable.COLUMN_FOOD_SFATG,
 			FoodTable.COLUMN_FOOD_CHOLESTEROL,
 			FoodTable.COLUMN_FOOD_SODIUM
 	};
 	
 	int calories = 0, 
 	fatGrams = 0, 
 	carbsGrams = 0, 
 	proteinGrams = 0, 
 	fiberGrams = 0, 
 	saturatedFatGrams = 0, 
 	cholesterol = 0, 
 	sodium = 0;
 
 	cursor = context.getContentResolver().query(NutritionContentProvider.CONTENT_URI_FOOD, projection, FoodTable.COLUMN_DAILY_ID + " = ? ", new String[] { ("" + id) }, null);
 	
 	if (cursor != null) {
		if(cursor.moveToFirst()) {
			do {
				calories += Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(FoodTable.COLUMN_FOOD_CALORIES)));
				fatGrams += Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(FoodTable.COLUMN_FOOD_FATG)));
				carbsGrams += Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(FoodTable.COLUMN_FOOD_CARBSG)));
				proteinGrams += Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(FoodTable.COLUMN_FOOD_PROTEING)));
				fiberGrams += Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(FoodTable.COLUMN_FOOD_FIBERG)));
				saturatedFatGrams += Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(FoodTable.COLUMN_FOOD_SFATG)));
				cholesterol += Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(FoodTable.COLUMN_FOOD_CHOLESTEROL)));
				sodium += Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(FoodTable.COLUMN_FOOD_SODIUM)));
			} while (cursor.moveToNext());
		}
 		cursor.close();
 	}
 	
 	projection = new String[] {
 			PersonalInformationTable.COLUMN_MAX_CALORIES,
 			PersonalInformationTable.COLUMN_MAX_FATG,
 			PersonalInformationTable.COLUMN_MAX_CARBSG,
 			PersonalInformationTable.COLUMN_MAX_PROTEING,
 			PersonalInformationTable.COLUMN_MAX_FIBERG,
 			PersonalInformationTable.COLUMN_MAX_SFATG,
 			PersonalInformationTable.COLUMN_MAX_CHOLESTEROL,
 			PersonalInformationTable.COLUMN_MAX_SODIUM
 	};
 
 	int maxCalories = 0, 
 	maxFatGrams = 0, 
 	maxCarbsGrams = 0, 
 	maxProteinGrams = 0, 
 	maxFiberGrams = 0, 
 	maxSaturatedFatGrams = 0, 
 	maxCholesterol = 0, 
 	maxSodium = 0;
 	
 	cursor = context.getContentResolver().query(NutritionContentProvider.CONTENT_URI_PERSONAL, projection, null, null, null);
 	
 	if (cursor != null) {
 		cursor.moveToFirst();
 		
 		maxCalories = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(PersonalInformationTable.COLUMN_MAX_CALORIES)));
 		maxFatGrams = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(PersonalInformationTable.COLUMN_MAX_FATG)));
 		maxCarbsGrams = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(PersonalInformationTable.COLUMN_MAX_CARBSG)));
 		maxProteinGrams = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(PersonalInformationTable.COLUMN_MAX_PROTEING)));
 		maxFiberGrams = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(PersonalInformationTable.COLUMN_MAX_FIBERG)));
 		maxSaturatedFatGrams = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(PersonalInformationTable.COLUMN_MAX_SFATG)));
 		maxCholesterol = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(PersonalInformationTable.COLUMN_MAX_CHOLESTEROL)));
 		maxSodium = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(PersonalInformationTable.COLUMN_MAX_SODIUM)));
 
 		cursor.close();
 	}
 	
     List<double[]> values = new ArrayList<double[]>();
     values.add(new double[]{ calories, fatGrams, carbsGrams, proteinGrams, fiberGrams, saturatedFatGrams, cholesterol, sodium }); 
     values.add(new double[] { maxCalories, maxFatGrams, maxCarbsGrams, maxProteinGrams, maxFiberGrams, maxSaturatedFatGrams, maxCholesterol, maxSodium });
     
     int[] colors = new int[] { Color.CYAN, Color.BLUE };
     
     XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
     renderer.setOrientation(Orientation.VERTICAL);
     
    setChartSettings(renderer, "My Intake vs. Recommended Intake", "Measurements", "Units", 0.5,
         12.5, 0, 3000, Color.GRAY, Color.LTGRAY);
 
     renderer.setXLabels(1);
     renderer.setYLabels(10);
     renderer.addXTextLabel(1, "Calories");
     renderer.addXTextLabel(2, "Grams Fat");
     renderer.addXTextLabel(3, "Carbs (g)");
     renderer.addXTextLabel(4, "Protein (g)");
     renderer.addXTextLabel(5, "Fibre (g)");
     renderer.addXTextLabel(6, "Sat. Fat (g)");
     renderer.addXTextLabel(7, "Cholesterol (mg)");
     renderer.addXTextLabel(8, "Sodium (mg)");
     renderer.setPanEnabled(false, true);
     renderer.setBarSpacing(1.0);
     renderer.setMargins(new int[] { 30, 30, 150, 30 });
     renderer.setFitLegend(true);
     renderer.setXLabelsAlign(Align.RIGHT);
     renderer.setAntialiasing(true);
     
     int length = renderer.getSeriesRendererCount();
     for (int i = 0; i < length; i++) {
       SimpleSeriesRenderer seriesRenderer = renderer.getSeriesRendererAt(i);
       seriesRenderer.setDisplayChartValues(true);
       seriesRenderer.setChartValuesTextAlign(Align.LEFT);
       seriesRenderer.setChartValuesTextSize(12);
     }
     
     return ChartFactory.getBarChartIntent(context, buildBarDataset(titles, values), renderer,
         Type.DEFAULT);
   }
 
 }
