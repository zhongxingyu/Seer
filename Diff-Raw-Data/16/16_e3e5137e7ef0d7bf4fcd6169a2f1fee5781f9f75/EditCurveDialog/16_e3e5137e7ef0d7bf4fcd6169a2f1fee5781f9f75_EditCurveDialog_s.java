 package uk.org.smithfamily.mslogger.dialog;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import uk.org.smithfamily.mslogger.ApplicationSettings;
 import uk.org.smithfamily.mslogger.R;
 import uk.org.smithfamily.mslogger.chart.ChartFactory;
 import uk.org.smithfamily.mslogger.chart.GraphicalView;
 import uk.org.smithfamily.mslogger.chart.chart.PointStyle;
 import uk.org.smithfamily.mslogger.chart.model.TimeSeries;
 import uk.org.smithfamily.mslogger.chart.model.XYMultipleSeriesDataset;
 import uk.org.smithfamily.mslogger.chart.renderer.XYMultipleSeriesRenderer;
 import uk.org.smithfamily.mslogger.chart.renderer.XYSeriesRenderer;
 import uk.org.smithfamily.mslogger.ecuDef.Constant;
 import uk.org.smithfamily.mslogger.ecuDef.CurveEditor;
 import uk.org.smithfamily.mslogger.ecuDef.Megasquirt;
 import android.app.Dialog;
 import android.content.Context;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.text.InputType;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TableRow.LayoutParams;
 import android.widget.TextView;
 
 /**
  *
  */
 public class EditCurveDialog extends Dialog implements android.view.View.OnClickListener
 {
     private CurveEditor curve;
     private GraphicalView mChartView;
     private Megasquirt ecu;
     
     // The curve dataset
     private XYMultipleSeriesDataset dataset;
     
     /**
      * 
      * @param context
      * @param curve
      */
     public EditCurveDialog(Context context, CurveEditor curve)
     {
         super(context);
 
         this.curve = curve;
         this.ecu = ApplicationSettings.INSTANCE.getEcuDefinition();
         
         this.dataset = new XYMultipleSeriesDataset();
     }
     
     /**
      * @param savedInstanceState
      */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         
         setContentView(R.layout.editcurve);
         
         setTitle("Edit " + curve.getLabel());
         
         createTable();
         drawCurve();
         
         Button buttonBurn = (Button) findViewById(R.id.burn);
         buttonBurn.setOnClickListener(this);
         
         Button buttonCancel = (Button) findViewById(R.id.cancel);
         buttonCancel.setOnClickListener(this);
         
         // Hide keyboard
         getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
     }
     
     /**
      * 
      */
     private void createTable()
     {
         TableLayout table = (TableLayout) findViewById(R.id.table);
         
         // All columns should have the same width
         table.setStretchAllColumns(true);
         
         LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
         
         final int tableNbX = 2;
         final int tableNbY = curve.getxBins().length;
         
         // Column headers
         TableRow tableRow = new TableRow(getContext());
         tableRow.setLayoutParams(lp);
         
         Constant xBinsConstant = ecu.getConstantByName(curve.getxBinsName());
         Constant yBinsConstant = ecu.getConstantByName(curve.getyBinsName());
         
         for (int x = 1; x <= tableNbX; x++)
         {
             TextView columnHeader = new TextView(getContext());
 
             String label = "";
             
             if (x == 1)
             {
                 label = curve.getxLabel();
                 
                 // Add units, if they exists
                 if (!xBinsConstant.getUnits().equals("")) 
                 {
                     label += " (" + xBinsConstant.getUnits() + ")";
                 }
             }
             else
             {
                 label = curve.getyLabel();
                 
                 // Add units, if they exists
                 if (!yBinsConstant.getUnits().equals(""))
                 {
                     label += " (" + yBinsConstant.getUnits() + ")";
                 }
             }
             
             columnHeader.setText(label);
             columnHeader.setLayoutParams(lp);
             columnHeader.setTypeface(null, Typeface.BOLD);
             
             tableRow.addView(columnHeader);
         }
         
         // Add first row with the X and Y labels
         table.addView(tableRow,lp);
         
         // Add all the X and Y values in two columns
         for (int y = 1; y <= tableNbY; y++)
         {
             tableRow = new TableRow(getContext());
             tableRow.setLayoutParams(lp);
             
             for (int x = 1; x <= tableNbX; x++)
             {               
                 EditText cell = new EditText(getContext());
                 
                 double value;
                 
                 // x = 1 is the X column
                 // x = 2 is the Y column
                 if (x == 1)
                 {
                     value = curve.getxBins()[y - 1];
                 }
                 else
                 {
                     value = curve.getyBins()[y - 1];
                 }
                 
                 cell.setText(String.valueOf(value));
                 cell.setId(getCellId(x,y));
                 cell.setLayoutParams(lp);
                 cell.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                 cell.setSingleLine(true);
                 cell.setPadding(10, 10, 10, 10);
                 
                 // Add EditText to row
                 tableRow.addView(cell);
             }
             
             table.addView(tableRow,lp);
         }
     }
     
     /**
      * Generate on the fly ID based on the cell position
      * 
      * @param x
      * @param y
      * @return
      */
     private int getCellId(int x, int y)
     {
         return x + (y * 1000);
     }
     
     /**
      * Draw the actual curve
      */
     private void drawCurve()
     {
         int colors[] = {Color.rgb(255, 0, 0)};
         
         double[] xAxis = curve.getxAxis();
         double[] yAxis = curve.getyAxis();
         
         double minXaxis = xAxis[0];
         double maxXaxis = xAxis[1];
         
         double minYaxis = yAxis[0];
         double maxYaxis = yAxis[1];
         
         XYMultipleSeriesRenderer renderer = buildRenderer(1, colors);
         setChartSettings(renderer, "", "", "", minXaxis, maxXaxis, minYaxis, maxYaxis, Color.GRAY, Color.LTGRAY);
         
         renderer.setXTitle(curve.getxLabel());
         renderer.setYTitle(curve.getyLabel(), 0);
         
         renderer.setPanLimits(new double[] { minXaxis, maxXaxis, minYaxis, maxYaxis });
         renderer.setShowLabels(true);
         renderer.setClickEnabled(false);
         renderer.setShowGrid(true);
         renderer.setZoomEnabled(true);
         renderer.setShowLegend(false);
         
         // Make sure the curve points are filled
         int length = renderer.getSeriesRendererCount();
         for (int i = 0; i < length; i++)
         {
           ((XYSeriesRenderer) renderer.getSeriesRendererAt(i)).setFillPoints(true);
         }
         
         LinearLayout layout = (LinearLayout) findViewById(R.id.curve);
 
         if (mChartView == null)
         {
             updateDataset();
             mChartView = ChartFactory.getLineChartView(getContext(), dataset, renderer);     
             
             layout.addView(mChartView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
         }
         else
         {
             mChartView.repaint();
         }
     }
     
     /**
      * Function called to update the chart dataset with values that are in the table
      */
     private void updateDataset()
     {
         boolean isUpdate = false;
         
         // If it's not the first time we're building the dataset, remove the old one first
         if (dataset.getSeries().length > 0)
         {
             dataset.removeSeries(0);
             
             isUpdate = true;
         }
         
         List<double[]> x = new ArrayList<double[]>();
         List<double[]> values = new ArrayList<double[]>();
         
         double[] xBins = new double[curve.getxBins().length];
         double[] yBins = new double[curve.getyBins().length];
         
         // Build the arrays of values from the EditTexts
         for (int i = 0; i < curve.getxBins().length; i++)
         {
             EditText edit = (EditText) findViewById(getCellId(1,i + 1));
             xBins[i] = Double.valueOf(edit.getText().toString());
             
             edit = (EditText) findViewById(getCellId(2,i + 1));
             yBins[i] = Double.valueOf(edit.getText().toString());
         }
         
         x.add(xBins);
         values.add(yBins);
         
         // Create the new dataset
         buildDateDataset(new String[]{""}, x, values);
         
         // If it's not the first time we're doing this, trigger a refresh of the curve
         if (isUpdate)
         {
             mChartView.refreshDrawableState();
             mChartView.repaint();
         }
     }
 
     /**
      * Builds an XY multiple time dataset using the provided values.
      * 
      * @param titles the series titles
      * @param xValues the values for the X axis
      * @param yValues the values for the Y axis
      * @return the XY multiple time dataset
      */
     protected void buildDateDataset(String[] titles, List<double[]> xValues, List<double[]> yValues)
     {
         TimeSeries series = new TimeSeries(titles[0]);
         double[] xV = xValues.get(0);
         double[] yV = yValues.get(0);
         int seriesLength = xV.length;
         for (int k = 0; k < seriesLength; k++)
         {
             series.add(xV[k], yV[k]);
         }
         
         dataset.addSeries(series);
     }
     
     /**
      * Builds an XY multiple series renderer.
      * 
      * @param nbLines Number of lines
      * @param colors Array of colors for each point
      * @return The XY multiple series renderers
      */
     protected XYMultipleSeriesRenderer buildRenderer(int nbLines, int[] colors)
     {
         XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
         setRenderer(renderer, nbLines, colors);
         return renderer;
     }
 
     /**
      * 
      * @param renderer
      * @param nbLines
      * @param colors
      */
     protected void setRenderer(XYMultipleSeriesRenderer renderer, int nbLines, int[] colors)
     {
         renderer.setAxisTitleTextSize(16);
         renderer.setChartTitleTextSize(20);
         renderer.setLabelsTextSize(15);
         renderer.setLegendTextSize(15);
         renderer.setPointSize(5f);
         renderer.setMargins(new int[] { 20, 30, 15, 20 });
 
         for (int i = 0; i < nbLines; i++)
         {
             XYSeriesRenderer r = new XYSeriesRenderer();
             r.setColor(colors[i]);
             r.setPointStyle(PointStyle.CIRCLE);
             renderer.addSeriesRenderer(r);
         }
     }
 
     /**
      * Sets a few of the series renderer settings.
      * 
      * @param renderer the renderer to set the properties to
      * @param title the chart title
      * @param xTitle the title for the X axis
      * @param yTitle the title for the Y axis
      * @param xMin the minimum value on the X axis
      * @param xMax the maximum value on the X axis
      * @param yMin the minimum value on the Y axis
      * @param yMax the maximum value on the Y axis
      * @param axesColor the axes color
      * @param labelsColor the labels color
      */
     protected void setChartSettings(XYMultipleSeriesRenderer renderer, String title, String xTitle, String yTitle, double xMin, double xMax, double yMin, double yMax, int axesColor, int labelsColor)
     {
         renderer.setChartTitle(title);
         renderer.setXTitle(xTitle);
         renderer.setYTitle(yTitle);
         renderer.setXAxisMin(xMin);
         renderer.setXAxisMax(xMax);
         renderer.setYAxisMin(yMin);
         renderer.setYAxisMax(yMax);
         renderer.setAxesColor(axesColor);
         renderer.setLabelsColor(labelsColor);
     }
     
     /**
     * Triggered when the two bottoms button are clicked ("Burn" and "Cancel") 
      * 
      * @param v The view that was clicked on
      */
     @Override
     public void onClick(View v)
     {
         int which = v.getId();
         
         if (which == R.id.burn)
         {
             
         }
         else if (which == R.id.cancel)
         {
             cancel();
         }
     }
 }
