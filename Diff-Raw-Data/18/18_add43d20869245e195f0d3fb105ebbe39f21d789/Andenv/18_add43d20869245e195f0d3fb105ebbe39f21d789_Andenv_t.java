 package at.drizzd.Andenv;
 
 import java.lang.Thread;
 import java.lang.String;
 import java.lang.Integer;
 
 import android.util.Log;
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.LinearLayout;
 
 import org.achartengine.GraphicalView;
 import org.achartengine.ChartFactory;
 import org.achartengine.model.XYMultipleSeriesDataset;
 import org.achartengine.model.XYSeries;
 import org.achartengine.renderer.XYMultipleSeriesRenderer;
 import org.achartengine.renderer.XYSeriesRenderer;
 
 public class Andenv extends Activity {
     private static String TAG = "Andenv";
     volatile boolean mActive = true;
     Thread mGenerator;
 
     private GraphicalView mChart;
 
     private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
 
     private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
 
     private XYSeries mCurrentSeries;
 
     private XYSeriesRenderer mCurrentRenderer;
 
     private void initChart() {
        mRenderer.setPanEnabled(false, false);
        mRenderer.setZoomEnabled(false, false);
        mRenderer.setClickEnabled(false);
        mRenderer.setShowGridY(false);

         mCurrentSeries = new XYSeries("Sample Data");
         mDataset.addSeries(mCurrentSeries);

         mCurrentRenderer = new XYSeriesRenderer();
         mRenderer.addSeriesRenderer(mCurrentRenderer);
     }
 
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
         initChart();
         mChart = ChartFactory.getLineChartView(this, mDataset, mRenderer);
         layout.addView(mChart);
 
         mGenerator = new Thread(new Runnable() {
             public void run() {
                 int x = 0;
                 int y = 0;
                while (mActive) {
                     runOnUiThread(new PublishUpdate(x, y));
                     x++;
                     y = (y + 1) % 50;
                     sleep(50);
                 }
             }
         });
         mGenerator.start();
     }
 
     class PublishUpdate implements Runnable {
         double mX;
         double mY;
 
         public PublishUpdate(double x, double y) {
             mX = x;
             mY = y;
         }
 
         public void run() {
             mCurrentSeries.add(mX, mY);
            mRenderer.setRange(new double[] {mX-100, mX, 0, 50});
             mChart.repaint();
         }
     };
 
     public void onDestroy() {
         mActive = false;
         try {
             mGenerator.join();
         } catch (Throwable t) {
             Log.wtf(TAG, t);
         }
         super.onDestroy();
     }
 
     private void sleep(int millis) {
         try {
             Thread.sleep(millis);
         } catch (Throwable t) {
             Log.wtf(TAG, t);
         }
     }
 }
