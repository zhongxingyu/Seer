 package com.bibounde.vprotovis;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.bibounde.vprotovis.chart.bar.BarChart;
 import com.bibounde.vprotovis.chart.bar.BarTooltipFormatter;
 import com.bibounde.vprotovis.chart.bar.DefaultBarTooltipFormatter;
 import com.bibounde.vprotovis.chart.bar.Serie;
 import com.bibounde.vprotovis.common.AxisLabelFormatter;
 import com.bibounde.vprotovis.common.DefaultAxisLabelFormatter;
 import com.bibounde.vprotovis.common.Padding;
 import com.bibounde.vprotovis.common.Range;
 import com.bibounde.vprotovis.gwt.client.bar.VBarChartComponent;
 import com.vaadin.terminal.PaintException;
 import com.vaadin.terminal.PaintTarget;
 import com.vaadin.ui.ClientWidget;
 
 /**
  * Component used to display multi-series bar chart. A grouped chart allows
  * accurate comparison of individual values thanks to an aligned baseline: a
  * position, rather than length, judgment is used
  * 
  * @author bibounde
  * 
  */
 @ClientWidget(VBarChartComponent.class)
 public class BarChartComponent extends AbstractChartComponent {
 
     private AxisLabelFormatter yAxisLabelFormatter = new DefaultAxisLabelFormatter();
     private BarTooltipFormatter tooltipFormatter = new DefaultBarTooltipFormatter();
 
     /**
      * Initializes a newly created BarChartComponent
      */
     public BarChartComponent() {
         super(new BarChart());
         this.setId("v-protovis-barchart-" + this.hashCode());
     }
     
     private BarChart getBarChart() {
         return (BarChart) this.chart;
     }
 
     /**
      * Appends serie values
      * @param name name of the serie
      * @param values values
      * @return serie index
      */
     public int addSerie(String name, double[] values) {
         return this.getBarChart().addSerie(name, values);
     }
 
     /**
      * Clears all serie values
      */
     public void clearSeries() {
         this.getBarChart().getSeries().clear();
     }
 
     /**
      * Sets the names of group
      * @param groupNames new names of group
      */
     public void setGroupNames(String[] groupNames) {
         this.getBarChart().setGroupNames(groupNames);
     }
 
     /**
      * Sets space (unit : pixels) between groups
      * @param groupInset space (unit : pixels) between groups
      */
     public void setGroupInset(double groupInset) {
         this.getBarChart().setGroupInset(groupInset);
     }
 
     /**
      * Sets space (unit : pixels) between bar in each group
      * @param barInset space (unit : pixels) between bar in each group
      */
     public void setBarInset(double barInset) {
         this.getBarChart().setBarInset(barInset);
     }
 
     /**
      * Sets visibility of x axis
      * @param visible x axis visibility
      */
    public void setXAxisVisible(boolean visible) {
         this.getBarChart().setXAxisEnabled(visible);
         this.getBarChart().setXAxisLabelEnabled(false);
     }
 
     /**
      * Sets visibility of x axis labels
      * @param visible x axis label visibility
      */
     public void setXAxisLabelVisible(boolean visible) {
         this.getBarChart().setXAxisEnabled(this.getBarChart().isXAxisEnabled() || visible);
         this.getBarChart().setXAxisLabelEnabled(visible);
     }
 
     /**
      * Sets visibility of y axis
      * @param visible y axis visibility
      */
     public void setYAxisVisible(boolean visible) {
         this.getBarChart().setYAxisEnabled(visible);
         this.getBarChart().setYAxisLabelEnabled(false);
     }
 
     /**
      * Sets visibility of y axis labels
      * @param visible y axis label visibility
      */
     public void setYAxisLabelVisible(boolean visible) {
         this.getBarChart().setYAxisEnabled(this.getBarChart().isYAxisEnabled() || visible);
         this.getBarChart().setYAxisLabelEnabled(visible);
     }
 
     /**
      * Sets y axis label step (used to display ticks on y axis)
      * @param step y axis label step
      */
     public void setYAxisLabelStep(double step) {
         this.getBarChart().setYAxisLabelStep(step);
     }
 
     /**
      * Sets visibility of horizontal lines
      * @param visible horizontal line visibility
      */
     public void setYAxisGridVisible(boolean visible) {
         this.getBarChart().setYAxisEnabled(this.getBarChart().isYAxisEnabled() || visible);
         this.getBarChart().setYAxisGridEnabled(visible);
     }
 
     /**
      * Sets y axis label formatter
      * @param yAxisLabelFormatter y axis label formatter
      */
     public void setYAxisLabelFormatter(AxisLabelFormatter yAxisLabelFormatter) {
         if (yAxisLabelFormatter == null) {
             this.yAxisLabelFormatter = new DefaultAxisLabelFormatter();
         } else {
             this.yAxisLabelFormatter = yAxisLabelFormatter;
         }
     }
 
     /**
      * Sets tooltip formatter
      * @param tooltipFormatter tooltip formatter
      */
     public void setTooltipFormatter(BarTooltipFormatter tooltipFormatter) {
         this.tooltipFormatter = tooltipFormatter;
         chart.setTooltipEnabled(tooltipFormatter != null);
     }
 
     @Override
     public void paintContent(PaintTarget target) throws PaintException {
         super.paintContent(target);
 
         int groupCount = 0;
         double minValue = 0d, maxValue = 0d;
         for (Serie serie : this.getBarChart().getSeries()) {
             for (int i = 0; i < serie.getValues().length; i++) {
                 minValue = minValue < serie.getValues()[i] ? minValue : serie.getValues()[i];
                 maxValue = maxValue < serie.getValues()[i] ? serie.getValues()[i] : maxValue;
             }
             groupCount = groupCount < serie.getValues().length ? serie.getValues().length : groupCount;
         }
         Padding padding = new Padding(10d, 10d, 10d, minValue < 0 ? 10d : 0d);
 
         this.paintChartValues(target, groupCount);
         this.paintChartOptions(target, groupCount, minValue, maxValue, padding);
     }
 
     private void paintChartValues(PaintTarget target, int groupCount) throws PaintException {
         target.addVariable(this, VBarChartComponent.UIDL_DATA_SERIES_COUNT, this.getBarChart().getSeries().size());
         target.addVariable(this, VBarChartComponent.UIDL_DATA_GROUPS_COUNT, groupCount);
 
         String[] groupNames = this.getGroupNames(groupCount);
         for (int i = 0; i < groupNames.length; i++) {
             target.addVariable(this, VBarChartComponent.UIDL_DATA_GROUP_NAME + i, groupNames[i]);
         }
         target.addVariable(this, VBarChartComponent.UIDL_DATA_GROUPS_NAMES, groupNames);
 
         // Convert data in protovis mode
         Map<Integer, List<String>> dataMap = new HashMap<Integer, List<String>>();
         Map<Integer, List<String>> tooltipMap = new HashMap<Integer, List<String>>();
 
         String[] serieNames = new String[this.getBarChart().getSeries().size()];
 
         int serieIndex = 0;
         for (Serie serie : this.getBarChart().getSeries()) {
             for (int i = 0; i < serie.getValues().length; i++) {
                 // Data
                 List<String> values = null;
                 if (!dataMap.containsKey(i)) {
                     values = new ArrayList<String>();
                     dataMap.put(i, values);
                 } else {
                     values = dataMap.get(i);
                 }
                 values.add(String.valueOf(serie.getValues()[i]));
 
                 // Tooltips
                 if (this.getBarChart().isTooltipEnabled()) {
                     List<String> tooltips = null;
                     if (!tooltipMap.containsKey(i)) {
                         tooltips = new ArrayList<String>();
                         tooltipMap.put(i, tooltips);
                     } else {
                         tooltips = tooltipMap.get(i);
                     }
                     tooltips.add(this.tooltipFormatter.getTooltipHTML(serie.getName(), serie.getValues()[i], groupNames[i]));
                 }
             }
 
             target.addVariable(this, VBarChartComponent.UIDL_DATA_SERIE_NAME + serieIndex, serie.getName());
             serieNames[serieIndex] = serie.getName();
             serieIndex++;
         }
 
         // Serie names
         target.addVariable(this, VBarChartComponent.UIDL_DATA_SERIES_NAMES, serieNames);
 
         // Data
         int index = 0;
         for (List<String> values : dataMap.values()) {
             target.addVariable(this, VBarChartComponent.UIDL_DATA_GROUP_VALUES + index, values.toArray(new String[values.size()]));
             index++;
         }
 
         // Tooltips
         if (this.getBarChart().isTooltipEnabled()) {
             index = 0;
             for (List<String> values : tooltipMap.values()) {
                 target.addVariable(this, VBarChartComponent.UIDL_DATA_GROUP_TOOLTIP_VALUES + index, values.toArray(new String[values.size()]));
                 index++;
             }
         }
     }
 
     private void paintChartOptions(PaintTarget target, int groupCount, double minValue, double maxValue, Padding padding) throws PaintException {
 
         double bottom = this.getAutoBottom(minValue);
         target.addVariable(this, VBarChartComponent.UIDL_OPTIONS_BOTTOM, bottom);
         target.addVariable(this, VBarChartComponent.UIDL_OPTIONS_LEFT, this.getBarChart().getMarginLeft());
 
         double groupWidth = this.getAutoGroupWidth(groupCount, padding);
         target.addVariable(this, VBarChartComponent.UIDL_OPTIONS_GROUP_WIDTH, groupWidth);
         target.addVariable(this, VBarChartComponent.UIDL_OPTIONS_GROUP_INSET, this.getBarChart().getGroupInset());
         target.addVariable(this, VBarChartComponent.UIDL_OPTIONS_BAR_HEIGHT, this.getAutoBarHeight(minValue, maxValue, bottom, padding));
         target.addVariable(this, VBarChartComponent.UIDL_OPTIONS_BAR_WIDTH, this.getAutoBarWidth(groupWidth));
         target.addVariable(this, VBarChartComponent.UIDL_OPTIONS_BAR_INSET, this.getBarChart().getBarInset());
 
         target.addVariable(this, VBarChartComponent.UIDL_OPTIONS_PADDING_LEFT, padding.getLeft());
         target.addVariable(this, VBarChartComponent.UIDL_OPTIONS_PADDING_BOTTOM, padding.getBottom());
         target.addVariable(this, VBarChartComponent.UIDL_OPTIONS_PADDING_RIGHT, padding.getRight());
         target.addVariable(this, VBarChartComponent.UIDL_OPTIONS_PADDING_TOP, padding.getTop());
 
         target.addVariable(this, VBarChartComponent.UIDL_OPTIONS_X_AXIS_ENABLED, this.getBarChart().isXAxisEnabled());
         target.addVariable(this, VBarChartComponent.UIDL_OPTIONS_X_AXIS_LABEL_ENABLED, this.getBarChart().isXAxisLabelEnabled());
 
         target.addVariable(this, VBarChartComponent.UIDL_OPTIONS_Y_AXIS_ENABLED, this.getBarChart().isYAxisEnabled());
         target.addVariable(this, VBarChartComponent.UIDL_OPTIONS_Y_AXIS_LABEL_ENABLED, this.getBarChart().isYAxisLabelEnabled());
 
         // Add y axis values and their formatted text
         Range rangeY = Range.getAutoRange(minValue, maxValue, this.getBarChart().getYAxisLabelStep());
         Double[] rangeYValues = rangeY.getRangeArray();
         String[] rangeYSValues = new String[rangeYValues.length];
         for (int i = 0; i < rangeYValues.length; i++) {
             rangeYSValues[i] = this.yAxisLabelFormatter.format(rangeYValues[i]);
         }
         target.addVariable(this, VBarChartComponent.UIDL_OPTIONS_Y_AXIS_LABEL_RANGE_D_VALUES, rangeY.getRangeArrayAsString());
         target.addVariable(this, VBarChartComponent.UIDL_OPTIONS_Y_AXIS_LABEL_RANGE_S_VALUES, rangeYSValues);
         target.addVariable(this, VBarChartComponent.UIDL_OPTIONS_Y_AXIS_GRID_ENABLED, this.getBarChart().isYAxisGridEnabled());
     }
 
     protected String[] getGroupNames(int groupCount) {
 
         String[] ret = new String[groupCount];
 
         if (this.getBarChart().getGroupNames() == null) {
             for (int i = 0; i < groupCount; i++) {
                 ret[i] = "";
             }
         } else {
             int namesCount = this.getBarChart().getGroupNames().length;
             for (int i = 0; i < groupCount; i++) {
                 if (i < namesCount) {
                     ret[i] = this.getBarChart().getGroupNames()[i];
                 } else {
                     ret[i] = "";
                 }
             }
         }
 
         return ret;
     }
 
     protected double getAutoBottom(double minValue) {
         if (minValue < 0) {
             // Axis is in the center of chart
             return (this.getBarChart().getHeight() - this.getBarChart().getMarginBottom() - this.getBarChart().getMarginTop()) / 2 + this.getBarChart().getMarginBottom();
         } else {
             return 0d + this.getBarChart().getMarginBottom();
         }
     }
 
     protected double getAutoGroupWidth(int groupCount, Padding padding) {
         double availableWidth = this.getBarChart().getWidth() - this.getBarChart().getMarginLeft() - this.getBarChart().getMarginRight() - padding.getRight() - padding.getLeft()
                 - this.getBarChart().getLegendAreaWidth() - this.chart.getLegendInsetLeft();
 
         return (availableWidth - ((groupCount - 1) * this.getBarChart().getGroupInset())) / groupCount;
     }
 
     protected double getAutoBarHeight(double minValue, double maxValue, double bottom, Padding padding) {
         double availableHeight = this.getBarChart().getHeight() - bottom - this.getBarChart().getMarginTop() - padding.getTop() - padding.getBottom();
 
         double minAbs = Math.abs(minValue);
         double max = minAbs > maxValue ? minAbs : maxValue;
 
         return availableHeight / max;
     }
 
     protected double getAutoBarWidth(double groupWidth) {
         return (groupWidth - ((this.getBarChart().getSeries().size() - 1) * this.getBarChart().getBarInset())) / this.getBarChart().getSeries().size();
     }
 }
