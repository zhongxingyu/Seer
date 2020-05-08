 package org.personalfinance;
 
 
 import java.sql.Date;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import org.achartengine.ChartFactory;
 import org.achartengine.GraphicalView;
 import org.achartengine.chart.BarChart.Type;
 import org.achartengine.model.CategorySeries;
 import org.achartengine.model.XYMultipleSeriesDataset;
 import org.achartengine.renderer.SimpleSeriesRenderer;
 import org.achartengine.renderer.XYMultipleSeriesRenderer;
 import org.achartengine.renderer.XYMultipleSeriesRenderer.Orientation;
 import org.personalfinance.database.TransactionDAO;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.graphics.Color;
 import android.graphics.Paint.Align;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.RelativeLayout;
 
 
 
 
 public class BarTrial extends Activity {
 
 	List<Transaction> outcomeTransactions = null;
 	Calendar cal = new GregorianCalendar();
 	int currentYear;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_bar_trial);
 		//transaccionesDePrueba();
 		TransactionDAO DAO = new TransactionDAO(getApplicationContext());
 		DAO.open();
 		outcomeTransactions = DAO.getAllTransactions(true);
 		DAO.close();
 		currentYear = cal.get(Calendar.YEAR);
 		final GraphicalView gv =createIntent();
 
 		RelativeLayout rl=(RelativeLayout)findViewById(R.id.graph);
 		rl.addView(gv);
 
 	}
 	
 	//Si el dispositivo se rota, se lanza la activity principal y esta se finaliza
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 
 		if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
 			Intent intent = new Intent(this, MainActivity.class);
 			startActivity(intent);
 			this.finish();
 		}
 
 	}
 
 
 	public void transaccionesDePrueba(){
 		Transaction trans1 = new Transaction(1, 1, 50, Date.valueOf("2013-05-05"), "Ropa", 0, 1, 1, true);
 		Transaction trans2 = new Transaction(2, 1, (float) 25.5, Date.valueOf("2013-06-05"), "Ropa", 0, 1, 1, true);
 		Transaction trans3 = new Transaction(3, 1, 40, Date.valueOf("2013-05-05"), "Ropa", 0, 1, 1, true);
 		Transaction trans4 = new Transaction(4, 1, 10, Date.valueOf("2013-01-05"), "Ropa", 0, 1, 1, true);
 		Transaction trans5 = new Transaction(5, 1, 150, Date.valueOf("2013-03-05"), "Ropa", 0, 1, 1, true);
 		Transaction trans6 = new Transaction(6, 1, 150, Date.valueOf("2012-03-05"), "Ropa", 0, 1, 1, true);
 		TransactionDAO DAO = new TransactionDAO(getApplicationContext());
 		DAO.open();
 		DAO.deleteAllTransactions();
 		DAO.saveTransactionOutcome(trans1);
 		DAO.saveTransactionOutcome(trans2);
 		DAO.saveTransactionOutcome(trans3);
 		DAO.saveTransactionOutcome(trans4);
 		DAO.saveTransactionOutcome(trans5);
 		DAO.saveTransactionOutcome(trans6);
 		DAO.close();
 	}
 	
 	
 	public GraphicalView createIntent() {
 		String[] titles = new String[] { ""};
 		List<double[]> values = new ArrayList<double[]>();
 
 		values.add(new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
 		double maxAmountOfMoney = 0;
 		
 		
 		for(Transaction trans: outcomeTransactions){	//Cargamos los valores de las transacciones para despues cargarlos en la gráfica, siempre y cuando sean del año actual
 			cal = Calendar.getInstance();
 			cal.setTime(trans.getFecha());
 			int transYear = cal.get(Calendar.YEAR);
 			if(transYear == currentYear) {
 				double[] newValue = values.get(0);
 				int month = cal.get(Calendar.MONTH);
 				newValue[month] += trans.getCantidadDinero();
 				if(newValue[month] > maxAmountOfMoney)
 					maxAmountOfMoney = newValue[month];
 				values.set(0, newValue );
 			}
 		}
 
 		int[] colors = new int[] { Color.parseColor("#FFA000")};  //Color de las barras 
 		XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
 		renderer.setOrientation(Orientation.HORIZONTAL);
 		setChartSettings(renderer, getString(R.string.user_expenses), ""/*leyenda eje x*/, ""/*leyenda eje y*/, 0.5,
 				12.5, 0, maxAmountOfMoney+10, Color.parseColor("#DEDEDE"), Color.parseColor("#DEDEDE"));
 		renderer.setXLabels(1);
 		renderer.setYLabels(10);
 		renderer.addXTextLabel(1, getString(R.string.jan));
 		renderer.addXTextLabel(2, "Feb");
 		renderer.addXTextLabel(3, "Mar");
 		renderer.addXTextLabel(4, getString(R.string.apr));
 		renderer.addXTextLabel(5, "May");
 		renderer.addXTextLabel(6, "Jun");
 		renderer.addXTextLabel(7, "Jul");
 		renderer.addXTextLabel(8, getString(R.string.aug));
 		renderer.addXTextLabel(9, "Sep");
 		renderer.addXTextLabel(10, "Oct");
 		renderer.addXTextLabel(11, "Nov");
 		renderer.addXTextLabel(12, getString(R.string.dec));
 		int length = renderer.getSeriesRendererCount();
 		/*for (int i = 0; i < length; i++) {
 			SimpleSeriesRenderer seriesRenderer = renderer.getSeriesRendererAt(i);
 			seriesRenderer.setDisplayChartValues(true);
 		} En el caso de querer mostrar valores encima de las barras descomentar estas lineas*/
 
 		renderer.setShowLegend(false);
 		renderer.setShowGrid(true);
 		
 		final GraphicalView grfv = ChartFactory.getBarChartView(BarTrial.this, buildBarDataset(titles, values), renderer,Type.DEFAULT);
 
 
 		return grfv;
 	}
 
 	protected XYMultipleSeriesRenderer buildBarRenderer(int[] colors) {
 		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
 		renderer.setAxisTitleTextSize(12);		//Tamaño del nombre de los ejes
 		renderer.setChartTitleTextSize(14);		//Tamaño del titulo de la grafica
 		renderer.setLabelsTextSize(10);			//Tamaño texto en los ejes
 		renderer.setLegendTextSize(10);			//Tamaño de la leyenda
 		renderer.setBarSpacing(1);			//Indica la separacion entre las barras
 
 		renderer.setMarginsColor(Color.parseColor("#303030"));		//Color de lo de alrededor de la grafica
 		renderer.setXLabelsColor(Color.parseColor("#DEDEDE"));
 		renderer.setYLabelsColor(0,Color.parseColor("#DEDEDE"));
 		
 		renderer.setGridColor(Color.parseColor("#555555"));
 
 		renderer.setApplyBackgroundColor(true);
 		renderer.setBackgroundColor(Color.parseColor("#303030"));	//Color de fondo en la grafica
 
 		int length = colors.length;
 		for (int i = 0; i < length; i++) {
 			SimpleSeriesRenderer r = new SimpleSeriesRenderer();
 			r.setColor(colors[i]);
 			//r.setChartvalueAngle(-90);
 			r.setDisplayChartValues(false);
 			//r.setChartValuesSpacing(1);			//El espacio que habra entre el valor y la barra
 			renderer.addSeriesRenderer(r);
 		}
 		return renderer;
 	}
 
 	protected XYMultipleSeriesDataset buildBarDataset(String[] titles, List<double[]> values) {
 		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
 		int length = titles.length;
 		for (int i = 0; i < length; i++) {
 			CategorySeries series = new CategorySeries(titles[i]);
 			double[] v = values.get(i);
 			int seriesLength = v.length;
 			for (int k = 0; k < seriesLength; k++) {
 				series.add(v[k]);
 			}
 			dataset.addSeries(series.toXYSeries());
 		}
 		return dataset;
 	}
 
 	protected void setChartSettings(XYMultipleSeriesRenderer renderer, String title, String xTitle,
 			String yTitle, double xMin, double xMax, double yMin, double yMax, int axesColor,
 			int labelsColor) {
 		renderer.setChartTitle(title);
 		renderer.setYLabelsAlign(Align.RIGHT);
 		renderer.setXTitle(xTitle);
 		renderer.setYTitle(yTitle);
 		renderer.setXAxisMin(xMin);
 		renderer.setXAxisMax(xMax);
 		renderer.setYAxisMin(yMin);
 		renderer.setYAxisMax(yMax);
 		renderer.setMargins(new int[] { 20, 40, 0, 15 });  //Arriba, Izq, Abajo, Der
 		renderer.setAxesColor(axesColor);
 		renderer.setLabelsColor(labelsColor);
 	}
 
 }
