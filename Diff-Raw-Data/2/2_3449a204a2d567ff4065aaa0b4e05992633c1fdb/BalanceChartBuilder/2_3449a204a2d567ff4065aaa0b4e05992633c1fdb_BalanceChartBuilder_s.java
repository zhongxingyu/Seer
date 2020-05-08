 package ru.spbau.WhereIsMyMoney.gui;
 
 import java.util.List;
 
 import org.achartengine.ChartFactory;
 import org.achartengine.chart.BarChart;
 import org.achartengine.model.CategorySeries;
 import org.achartengine.model.XYMultipleSeriesDataset;
 import org.achartengine.renderer.SimpleSeriesRenderer;
 import org.achartengine.renderer.XYMultipleSeriesRenderer;
 
 import ru.spbau.WhereIsMyMoney.R;
 import ru.spbau.WhereIsMyMoney.Transaction;
 import android.content.Context;
 import android.content.Intent;
 
 /**
  * User: Alexander Opeykin alexander.opeykin@gmail.com
  * Date: 11/13/12
  * Time: 10:14 PM
  */
 public class BalanceChartBuilder {
     private XYMultipleSeriesRenderer renderer;
     private Context myContext;
 
     public BalanceChartBuilder() {
         renderer = new XYMultipleSeriesRenderer();
     }
 
     public Intent getIntent(Context context, List<Transaction> transactions) {
         setChartSettings(transactions);
         myContext = context;
         readSettings();
         return ChartFactory.getBarChartIntent(context, getDataset(transactions), renderer, BarChart.Type.DEFAULT);
     }
 
     private void readSettings() {
         renderer.setAxisTitleTextSize(16);
 //        renderer.setChartTitleTextSize(20);
         renderer.setLabelsTextSize(15);
         renderer.setLegendTextSize(18);
         renderer.setBarSpacing(1);
         renderer.setMargins(new int[]{20, 30, 15, 5});
         renderer.setPanEnabled(true, false);
         renderer.setZoomEnabled(true, false);
         renderer.setAntialiasing(true);
         SimpleSeriesRenderer r = new SimpleSeriesRenderer();
         r.setColor(myContext.getResources().getColor(R.color.graph_color));
         renderer.addSeriesRenderer(r);
     }
 
     private void setChartSettings(List<Transaction> transactions) {
         //renderer.setChartTitle("Chart demo");
         renderer.setXTitle("transactions");
         renderer.setYTitle("balance");
         renderer.setXAxisMin(0);
         renderer.setXAxisMax(transactions.size() + 1);
         renderer.setYAxisMin(getMinBalance(transactions) * 0.9);
         renderer.setYAxisMax(getMaxBalance(transactions) * 1.1);  // additional 10%
     }
 
     private float getMaxBalance(List<Transaction> transactions) {
         float max = 0;
         for (Transaction t : transactions) {
             float balance = t.getBalance();
             max = max >= balance ? max : balance;
         }
         return max;
     }
 
     private float getMinBalance(List<Transaction> transactions) {
        float min = 0;
         for (Transaction t : transactions) {
             float balance = t.getBalance();
             min = min <= balance ? min : balance;
         }
         return min;
     }
 
     private XYMultipleSeriesDataset getDataset(List<Transaction> transactions) {
         XYMultipleSeriesDataset dataSet = new XYMultipleSeriesDataset();
         CategorySeries series = new CategorySeries("Balance");
 
         for (Transaction t : transactions) {
             series.add(t.getBalance());
         }
 
         dataSet.addSeries(series.toXYSeries());
         return dataSet;
     }
 }
