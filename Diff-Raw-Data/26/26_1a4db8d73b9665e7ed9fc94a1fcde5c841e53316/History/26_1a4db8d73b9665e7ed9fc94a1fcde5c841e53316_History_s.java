 package com.example.tompomodoros;
 
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.achartengine.ChartFactory;
 import org.achartengine.chart.BarChart.Type;
 import org.achartengine.model.CategorySeries;
 import org.achartengine.model.XYMultipleSeriesDataset;
 import org.achartengine.renderer.SimpleSeriesRenderer;
 import org.achartengine.renderer.XYMultipleSeriesRenderer;
 import org.achartengine.renderer.XYSeriesRenderer;
 
 import android.app.Activity;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Color;
 import android.graphics.Paint.Align;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 
 public class History extends Activity {
	private static Map map = new TreeMap<String, Object>();
 	private static final String TAG = "tomxue";
 	private static SQLiteDatabase db;
 	private final String DBNAME = "tompomo12.db";
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
         
         // 򿪻򴴽tompomodoros.dbݿ
      	db = openOrCreateDatabase(DBNAME, Context.MODE_PRIVATE, null);
 //     	db.execSQL("DROP TABLE IF EXISTS mytable");	// ? to be fixed...
      	// mytable
 //     	db.execSQL("CREATE TABLE mytable (_id INTEGER PRIMARY KEY AUTOINCREMENT, mydate VARCHAR, mydata SMALLINT)");
      	db.execSQL("CREATE TABLE if not exists mytable (_id INTEGER PRIMARY KEY AUTOINCREMENT, mydate VARCHAR, mydata SMALLINT)");
 		XYMultipleSeriesRenderer renderer = getBarDemoRenderer();
 		setChartSettings(renderer);
 		// (1) during it map was filled, by Tom Xue
 		XYMultipleSeriesDataset getBarDataset2 = getBarDataset(this);
 
 		int count = 1;
 		// ȽҪֶX̶ȡжݣҪٸ̶ȣXʾʱ䣬Ҳʾͼ
 		// (2) then map is further rendered, by Tom Xue
 		for (Object key_tmp : map.keySet()) {
 			renderer.addXTextLabel(count, key_tmp.toString());
 			count++;  
 		}
 
 		View chart = ChartFactory.getBarChartView(this, getBarDataset2,
 				renderer, Type.DEFAULT);
 		setContentView(chart);
 		
 		db.close();
 		// delete db, comment it out in final code
 //		deleteDatabase(DBNAME);  
 	}
 
 	private static XYMultipleSeriesDataset getBarDataset(Context cxt) {
 		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
 		CategorySeries series = new CategorySeries("31");
 
 //		map.put(mydate_key, 0.0);	// to make the db not empty
 
 		Cursor c = db.rawQuery("SELECT _id, mydate, mydata FROM mytable", new String[]{});  
         while (c.moveToNext()) {
             int _id = c.getInt(c.getColumnIndex("_id"));  
             String mydate = c.getString(c.getColumnIndex("mydate"));  
             int mydata = c.getInt(c.getColumnIndex("mydata"));  
             map.put(mydate, mydata);
            series.add(mydate, mydata);
             Log.v(TAG, "while loop times");
             System.out.println(_id);
             System.out.println(mydate);
             System.out.println(mydata);
             System.out.println("---------------------");
         }  
        c.close();  		
 		
 		dataset.addSeries(series.toXYSeries());
 		return dataset;
 	}
 
 	private static XYMultipleSeriesRenderer getBarDemoRenderer() {
 		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
 		renderer.setAxisTitleTextSize(16);
 		renderer.setChartTitleTextSize(16);
 		renderer.setLabelsTextSize(16);
 		renderer.setLegendTextSize(16);
 		renderer.setMargins(new int[] { 20, 30, 15, 0 });
 		XYSeriesRenderer r = new XYSeriesRenderer();
 		r.setColor(Color.BLUE);
 		renderer.addSeriesRenderer(r);
 		return renderer;
 	}
 
 	private static void setChartSettings(XYMultipleSeriesRenderer renderer) {
 		renderer.setChartTitle("31");
 		renderer.setXTitle("");
 		renderer.setYTitle("");
 		renderer.setYAxisMin(0);
 		renderer.setYAxisMax(31);
 		renderer.setXAxisMin(0.5);
 		renderer.setXAxisMax(7.5);
 		renderer.setShowLegend(false);
 		renderer.setShowLabels(true);
 		renderer.setShowGrid(true);
 		renderer.setXLabels(1);
 		// renderer.setDisplayChartValues(true);
 		renderer.setBackgroundColor(Color.WHITE);
 		// ҳ߿հ׵ɫ
 		renderer.setMarginsColor(Color.GRAY);
 		// Ƿʾ,,ĬΪ true
 		renderer.setShowAxes(true);
 		// x,yʾ
 		renderer.setXLabelsAlign(Align.CENTER);
 		renderer.setYLabelsAlign(Align.RIGHT);
 		// ,ɫ
 		renderer.setAxesColor(Color.RED);
 		// ʾ
 		renderer.setShowGrid(true);
 		// x,yϵĿ̶ȵɫ
 		renderer.setLabelsColor(Color.BLACK);
 		// Ƿʾ,,ĬΪ true
 		renderer.setShowAxes(true);
 		// ͼ֮ľ
 		renderer.setBarSpacing(2.5);
 		int length = renderer.getSeriesRendererCount();
 
 		for (int i = 0; i < length; i++) {
 			SimpleSeriesRenderer ssr = renderer.getSeriesRendererAt(i);
 			// ֪ߵľô,ĬAlign.CENTER,Ƕϵʾ
 			// ͻұ
 			ssr.setChartValuesTextAlign(Align.RIGHT);
 			ssr.setChartValuesTextSize(16);
 			ssr.setDisplayChartValues(true);
 		}
 	}
 
 }
