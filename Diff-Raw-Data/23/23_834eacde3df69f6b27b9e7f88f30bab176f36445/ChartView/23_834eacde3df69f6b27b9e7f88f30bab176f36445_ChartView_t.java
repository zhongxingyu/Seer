 package com.vasilakos.LeleDroid;
 
 import org.achartengine.ChartFactory;
 import org.achartengine.GraphicalView;
 import org.achartengine.model.CategorySeries;
 import org.achartengine.renderer.DefaultRenderer;
 import org.achartengine.renderer.SimpleSeriesRenderer;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.Window;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class ChartView extends Activity {
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);  
 
 		setContentView(R.layout.chart);
 		Integer id = -1;
 		
 		Intent intent = getIntent();
 		Bundle extras = intent.getExtras();
 		if (extras != null) {
 			id = extras.getInt("id", -1);
 		}
 
 		if (id <= 0) {
 			finish();
 		} else {
 			Str str = Str.getStrFromId(id);
 			makeChart(str.getName(), str.getAdeia(), str.getRestDays(), str.getPastDays());
 		}         
 	}
 
 	protected void makeChart(String name, Integer adeia, Integer ipiretisimoYpoloipo,
 			Integer perasan) {
 
 		if (adeia >= ipiretisimoYpoloipo) {
 			adeia = ipiretisimoYpoloipo;
 			ipiretisimoYpoloipo = 0;
 		} else {
 			ipiretisimoYpoloipo -= adeia;
 		}
 
 		if (perasan < 0) {
 			perasan = 0;
 		}
 
 		if (ipiretisimoYpoloipo < 0) {
 			ipiretisimoYpoloipo = 0;
 			adeia = 0;
 		}
 		if (adeia < 0) {
 			adeia = 0;
 		}
 		
 		if (ipiretisimoYpoloipo == 0 && adeia == 0 && perasan == 0) {
 			perasan = 1;
 		}
 
 		String[] labels = { adeia.toString(), ipiretisimoYpoloipo.toString(),
 				perasan.toString() };
 		GraphicalView mChartView;
 		LinearLayout layout = (LinearLayout) findViewById(R.id.pieChartActivity);
 		TextView nameTv = (TextView) findViewById(R.id.ChartTvName);
 		nameTv.setText(name);
 
 		double[] values = new double[] { adeia, ipiretisimoYpoloipo, perasan };
 		int[] colors = new int[] { Color.BLUE, Color.RED, Color.GREEN };
 		DefaultRenderer renderer = buildCategoryRenderer(colors);
 		mChartView = ChartFactory.getPieChartView(this,
 				buildCategoryDataset("Γράφημα", labels, values), renderer);
 
 		layout.addView(mChartView);
 	}
 
 	protected DefaultRenderer buildCategoryRenderer(int[] colors) {
 		DefaultRenderer renderer = new DefaultRenderer();
 		for (int color : colors) {
 			SimpleSeriesRenderer r = new SimpleSeriesRenderer();
 			r.setColor(color);
 			renderer.addSeriesRenderer(r);
 		}
 		renderer.setLabelsTextSize(10);
 		renderer.setShowLegend(false);
		renderer.setPanEnabled(false);
		renderer.setZoomEnabled(false);
 		return renderer;
 	}
 
 	protected CategorySeries buildCategoryDataset(String title,
 			String[] labels, double[] values) {
 		CategorySeries series = new CategorySeries(title);
 		series.add(labels[0], values[0]);
 		series.add(labels[1], values[1]);
 		series.add(labels[2], values[2]);
 		return series;
 	}
 }
