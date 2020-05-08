 package org.jfree.chart.editor;
 
 import org.jfree.chart.editor.themes.*;
 import org.jfree.chart.editor.components.*;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.plot.Plot;
 import org.jfree.chart.axis.Axis;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.axis.CategoryAxis;
 
 import java.awt.*;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Dan
  * Date: 17-Dec-2009
  * Time: 16:08:00
  * Implementation of the factory class for chart editor components. Enables users of the library to supply
  * extended implementations of the required components.
  */
 public class ChartEditorComponentFactoryImpl implements ChartEditorComponentFactory {
 
    public ChartEditorComponentFactoryImpl() {
         // do nothing.
     }
 
     public DefaultChartEditor createChartEditor(ExtendedChartTheme theme, JFreeChart chart, boolean immediateUpdate) {
         return new DefaultChartEditor(theme, chart, immediateUpdate);
     }
 
     public DefaultAxisEditor createAxisEditor(AxisTheme theme, JFreeChart chart, Axis axis, boolean immediateUpdate) {
         if (axis != null) {
             // figure out what type of axis we have and instantiate the
             // appropriate panel
             if (axis instanceof NumberAxis) {
                 return new DefaultNumberAxisEditor(theme, chart, immediateUpdate);
             } else if (axis instanceof CategoryAxis) {
                 return new DefaultCategoryAxisEditor(theme, chart, immediateUpdate);
             }
             else {
                 return new DefaultAxisEditor(theme, chart, immediateUpdate);
             }
         }
         else {
             return null;
         }
     }
 
     public DefaultPlotEditor createPlotEditor(PlotTheme theme, JFreeChart chart, Plot plot, boolean immediateUpdate) {
         return new DefaultPlotEditor(theme, chart, plot, immediateUpdate);
     }
 
     public DefaultLegendEditor createLegendEditor(LegendTheme theme, JFreeChart chart, boolean immediateUpdate) {
         return new DefaultLegendEditor(theme, chart, immediateUpdate);
     }
 
     public DefaultChartTitleEditor createChartTitleEditor(ChartTitleTheme theme, JFreeChart chart, boolean immediateUpdate) {
         return new DefaultChartTitleEditor(theme, chart, immediateUpdate);
     }
 
     public ItemLabelFormatPanel createItemLabelFormatPanel(LegendTheme theme, Plot p) {
         return new ItemLabelFormatPanel(theme.getItemFormatString(),
                 p, theme.getItemNumberFormatString(),
                 theme.getItemPercentFormatString(), false);
     }
 
     public ItemLabelFormatPanel createItemLabelFormatPanel(PlotTheme theme, Plot p ) {
         return new ItemLabelFormatPanel(theme.getLabelFormat(), p,
                 theme.getNumberFormatString(), theme.getPercentFormatString(), true);
     }
 
     public BorderPanel buildBorderPanel(String title, boolean visible, BasicStroke stroke, Paint paint) {
         return new BorderPanel(title, visible, stroke, paint);
     }
 
     public BackgroundEditingPanel buildBackgroundEditingPanel(ExtendedChartTheme theme) {
         return new BackgroundEditingPanel(theme);
     }
 
     public BackgroundEditingPanel buildBackgroundEditingPanel(PlotTheme theme) {
         return new BackgroundEditingPanel(theme);
     }
 
     public PaintControl getPaintControl(Paint p) {
         return getPaintControl(p, false);
     }
 
     public PaintControl getPaintControl(Paint p, boolean allowNulls) {
         return new PaintControl(p, allowNulls);
     }
 
     public NumberFormatDisplay getNumberFormatDisplay(String formatString) {
         return new NumberFormatDisplay(formatString);
     }
 
     public StrokeControl getStrokeControl(BasicStroke s) {
         return new StrokeControl(s);
     }
 
     public FontControl getFontControl(Font f) {
         return new FontControl(f);
     }
 }
