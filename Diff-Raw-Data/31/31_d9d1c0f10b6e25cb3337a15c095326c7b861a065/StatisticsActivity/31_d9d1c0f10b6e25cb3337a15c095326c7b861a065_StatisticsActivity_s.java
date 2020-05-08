 package pack.bi.activities;
 
 import org.achartengine.ChartFactory;
 import org.achartengine.GraphicalView;
 import org.achartengine.model.CategorySeries;
 import org.achartengine.model.SeriesSelection;
 import org.achartengine.renderer.DefaultRenderer;
 import org.achartengine.renderer.SimpleSeriesRenderer;
 
 import pack.bi.R;
 import pack.bi.model.Category;
 import pack.bi.model.Product;
 import pack.bi.model.Sales;
 import pack.bi.storage.DatabaseHandler;
 import pack.bi.utils.BiUtils;
 
 import android.app.Activity;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 
 import java.util.List;
 
 public class StatisticsActivity extends Activity {
     public static final String TYPE = "type";
 
     public static final String CATOGORY_KEY = "selected_category_key";
     public static final String CHART_BY_KEY = "chart_by_key";
 
     public static final int CHARTS_BY_INCOME = 0;
     public static final int CHARTS_BY_UNITS = 1;
     public static final int CHARTS_BY_SALES = 2;
 
     private static int[] COLORS = new int[] { Color.GREEN, Color.BLUE, Color.MAGENTA, Color.CYAN, Color.YELLOW };
 
     private CategorySeries mSeries = new CategorySeries("");
 
     private DefaultRenderer mRenderer = new DefaultRenderer();
 
     private String mDateFormat;
 
     private GraphicalView mChartView;
     private Category selectedCategory;
     private int chartsByType = CHARTS_BY_INCOME;// default value
     List<Product> productsInCategory;
     List<Category> allCategories;
     List<Sales> sales;
     DatabaseHandler dbHandler;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.xy_chart);
         selectedCategory = (Category) getIntent().getExtras().getSerializable(CATOGORY_KEY);
        setTitle(selectedCategory.get_categoryName());
         chartsByType = getIntent().getExtras().getInt(CHART_BY_KEY);
         dbHandler = new DatabaseHandler(this);
         productsInCategory = dbHandler.getProductsByCategoryId(selectedCategory.get_idCategory());
         drawGraph();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         if (mChartView == null) {
             LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
             mChartView = ChartFactory.getPieChartView(this, mSeries, mRenderer);
             mRenderer.setClickEnabled(true);
             mRenderer.setSelectableBuffer(10);
             mChartView.setOnClickListener(new View.OnClickListener() {
                 public void onClick(View v) {
                     SeriesSelection seriesSelection = mChartView.getCurrentSeriesAndPoint();
                     if (seriesSelection == null) {
                        Toast.makeText(StatisticsActivity.this, "No chart element was clicked", Toast.LENGTH_SHORT).show();
                     } else {
                         Toast.makeText(StatisticsActivity.this, String.valueOf(seriesSelection.getValue()), Toast.LENGTH_SHORT).show();
                     }
                 }
             });
             mChartView.setOnLongClickListener(new View.OnLongClickListener() {
                 public boolean onLongClick(View v) {
                     SeriesSelection seriesSelection = mChartView.getCurrentSeriesAndPoint();
                     if (seriesSelection == null) {
                        Toast.makeText(StatisticsActivity.this, "No chart element was long pressed", Toast.LENGTH_SHORT).show();
                         return false; // no chart element was long pressed, so
                                       // let something
                         // else handle the event
                     } else {
                         Toast.makeText(StatisticsActivity.this, String.valueOf(seriesSelection.getValue()), Toast.LENGTH_SHORT).show();
                         return true; // the element was long pressed - the event
                                      // has been
                         // handled
                     }
                 }
             });
             layout.addView(mChartView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
         } else {
             mChartView.repaint();
         }
     }
 
     @Override
     protected void onRestoreInstanceState(Bundle savedState) {
         super.onRestoreInstanceState(savedState);
         mSeries = (CategorySeries) savedState.getSerializable("current_series");
         mRenderer = (DefaultRenderer) savedState.getSerializable("current_renderer");
         mDateFormat = savedState.getString("date_format");
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         outState.putSerializable("current_series", mSeries);
         outState.putSerializable("current_renderer", mRenderer);
         outState.putString("date_format", mDateFormat);
     }
 
     private void drawGraph() {
         mRenderer.setApplyBackgroundColor(true);
         mRenderer.setBackgroundColor(Color.argb(100, 50, 50, 50));
         mRenderer.setChartTitleTextSize(20);
         mRenderer.setLabelsTextSize(15);
         mRenderer.setLegendTextSize(15);
         mRenderer.setMargins(new int[] { 20, 30, 15, 0 });
         mRenderer.setZoomButtonsVisible(true);
         mRenderer.setStartAngle(90);
         switch (chartsByType) {
             case CHARTS_BY_INCOME:
                 // statistici de incasari totale per categorii
                 allCategories = dbHandler.getAllCategories();
                 log("ByIncome", 0);
                 for (Category cat : allCategories) {
                     log(cat.get_categoryName(), 3);
                     int sum = 0;
                     productsInCategory = dbHandler.getProductsByCategoryId(cat.get_idCategory());
                     for (Product p : productsInCategory) {
                         log(p.get_name(), 6);
                         sales = dbHandler.getSalesByProductId(p.get_idProduct());
                         for (Sales sale : sales) {
                             log(String.valueOf(sale.get_amount()), 9);
                             sum += sale.get_amount() * p.get_price();
                         }
                     }
                    mSeries.add(cat.get_categoryName(), sum);
                     sum = 0;
                     SimpleSeriesRenderer renderer = new SimpleSeriesRenderer();
                     renderer.setColor(COLORS[(mSeries.getItemCount() - 1) % COLORS.length]);
                     mRenderer.addSeriesRenderer(renderer);
                 }
                 break;
             case CHARTS_BY_UNITS:
                 log("ByUnits", 0);
                 // statistici de vanzare per numar produse in categorie
                 for (Product p : productsInCategory) {
                     log(p.get_name(), 3);
                     sales = dbHandler.getSalesByProductId(p.get_idProduct());
                     int sum = 0;
                     for (Sales sale : sales) {
                         sum += sale.get_amount();
                         log(sale.get_amount() + " - sum:" + sum, 6);
                     }
                    mSeries.add(p.get_name(), sum);
                     sum = 0;
                     SimpleSeriesRenderer renderer = new SimpleSeriesRenderer();
                     renderer.setColor(COLORS[(mSeries.getItemCount() - 1) % COLORS.length]);
                     mRenderer.addSeriesRenderer(renderer);
                 }
                 break;
             case CHARTS_BY_SALES:
                 log("BySales", 0);
                 // statistici de incasari per produs in categorie
                 for (Product p : productsInCategory) {
                     log(p.get_name() + "->" + p.get_idProduct(), 3);
                     sales = dbHandler.getSalesByProductId(p.get_idProduct());
                     int sum = 0;
                     for (Sales sale : sales) {
                         sum += sale.get_amount() * p.get_price();
                         log(sale.get_amount() + " - sum:" + sum, 6);
                     }
                    mSeries.add(p.get_name(), sum);
                     sum = 0;
                     SimpleSeriesRenderer renderer = new SimpleSeriesRenderer();
                     renderer.setColor(COLORS[(mSeries.getItemCount() - 1) % COLORS.length]);
                     mRenderer.addSeriesRenderer(renderer);
                 }
                 break;
             default:
                 // this shouldn't happen, but just in case finish this activity if it does
                 BiUtils.showAlert("There was an error! Sorry. Please try again!", StatisticsActivity.this);
                 finish();
                 break;
         }
     }
 
     /**
      * Used for debugging, logs the required message
      * @param s
      * @param spaces
      */
     private void log(String s, int spaces) {
         String tab = "";
         for (int i = 0; i < spaces; i++) {
             tab += " ";
         }
         Log.d(StatisticsActivity.class.getSimpleName(), tab + s);
     }
 }
