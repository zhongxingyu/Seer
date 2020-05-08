 package com.bibounde.vprotovis;
 
 import com.bibounde.vprotovis.chart.spider.DefaultSpiderTooltipFormatter;
 import com.bibounde.vprotovis.chart.spider.Serie;
 import com.bibounde.vprotovis.chart.spider.SpiderChart;
 import com.bibounde.vprotovis.chart.spider.SpiderTooltipFormatter;
 import com.bibounde.vprotovis.common.AxisLabelFormatter;
 import com.bibounde.vprotovis.common.DefaultAxisLabelFormatter;
 import com.bibounde.vprotovis.common.Range;
 import com.bibounde.vprotovis.gwt.client.spider.VSpiderChartComponent;
 import com.vaadin.terminal.PaintException;
 import com.vaadin.terminal.PaintTarget;
 import com.vaadin.ui.ClientWidget;
 
 /**
  * A spider chart is a graphical method of displaying multivariate data in the
  * form of a two-dimensional chart of three or more quantitative variables
  * represented on axes starting from the same point.
  * 
  * @author bibounde
  * 
  */
 @ClientWidget(VSpiderChartComponent.class)
 public class SpiderChartComponent extends AbstractChartComponent {
 
     private AxisLabelFormatter axisLabelFormatter = new DefaultAxisLabelFormatter();
     private SpiderTooltipFormatter tooltipFormatter = new DefaultSpiderTooltipFormatter();
     /**
      * Initializes a newly created SpiderChartComponent
      */
     public SpiderChartComponent() {
         super(new SpiderChart());
         this.setId("v-protovis-spiderchart-" + this.hashCode());
     }
     
     private SpiderChart getSpiderChart() {
         return (SpiderChart) this.chart;
     }
     
     /**
      * Appends serie values
      * 
      * @param name
      *            name of the serie
      * @param values
      *            values
      * @return serie index
      */
     public int addSerie(String name, double[] values) {
         return this.getSpiderChart().addSerie(name, values);
     }
     
     /**
      * Clears all serie values
      */
     public void clearSeries() {
         this.getSpiderChart().getSeries().clear();
     }
     
     /**
      * Sets axis name values
      * @param axisNames axis name values
      */
     public void setAxisNames(String[] axisNames) {
         this.getSpiderChart().setAxisNames(axisNames);
     }
     
     /**
      * Sets visibility of axis
      * @param visible axis visibility
      */
     public void setAxisVisible(boolean visible) {
         this.getSpiderChart().setAxisEnabled(visible);
     }
     
     /**
      * Sets the axis label step
      * @param step axis label step
      */
     public void setAxisLabelStep(double step) {
         this.getSpiderChart().setAxisLabelStep(step);
     }
     
     /**
      * Sets visibility of axis labels
      * @param visible axis label visibility
      */
     public void setAxisLabelVisible(boolean visible) {
         this.getSpiderChart().setAxisEnabled(this.getSpiderChart().isAxisEnabled() || visible);
         this.getSpiderChart().setAxisLabelEnabled(visible);
     }
     
     /**
      * Sets visibility of axis caption
      * @param visible axis caption visibility
      */
     public void setAxisCaptionVisible(boolean visible) {
         this.getSpiderChart().setAxisEnabled(this.getSpiderChart().isAxisEnabled() || visible);
         this.getSpiderChart().setAxisCaptionEnabled(visible);
     }
     
     /**
      * Sets visibility of grid
      * @param visible grid visibility
      */
     public void setAxisGridVisible(boolean visible) {
         this.getSpiderChart().setAxisEnabled(this.getSpiderChart().isAxisEnabled() || visible);
         this.getSpiderChart().setAxisGridEnabled(visible);
     }
     
     /**
      * Sets axis label formatter
      * @param labelFormatter label formatter
      */
     public void setAxisLabelFormatter(AxisLabelFormatter labelFormatter) {
         if (labelFormatter != null) {
             this.axisLabelFormatter = labelFormatter;
         } else {
             this.axisLabelFormatter = new DefaultAxisLabelFormatter();
         }
     }
    
     /**
      * Sets visibility of tooltips
      * @param enabled visibility of tooltips
      */
     public void setTooltipEnabled(boolean enabled) {
         this.getSpiderChart().setTooltipEnabled(enabled);
     }
     
     /**
      * Sets tooltip formatter
      * @param tooltipFormatter tooltip formatter
      */
     public void setTooltipFormatter(SpiderTooltipFormatter tooltipFormatter) {
         this.tooltipFormatter = tooltipFormatter;
         if (tooltipFormatter == null) {
             this.getSpiderChart().setTooltipEnabled(false);
         }
     } 
     
     /**
      * Sets the line width
      * @param lineWidth line width
      */
     public void setLineWidth(int lineWidth) {
         this.getSpiderChart().setLineWidth(lineWidth);
     }
     
     /**
      * Sets the area mode
      * @param enabled area mode enabled if true
      */
     public void setAreaModeEnabled(boolean enabled) {
         this.getSpiderChart().setAreaMode(enabled);
     }
     
     /**
      * Sets the area opacity
      * @param opacity area opacity (must be in [0, 1])
      */
     public void setAreaOpacity(double opacity) {
         if (opacity < 0 || opacity > 1) {
             throw new IllegalArgumentException("Opacity must be in [0, 1]");
         }
         this.getSpiderChart().setOpacity(opacity);
     }
 
     /**
      * @see com.bibounde.vprotovis.AbstractChartComponent#paintContent(com.vaadin.terminal.PaintTarget)
      */
     @Override
     public void paintContent(PaintTarget target) throws PaintException {
         super.paintContent(target);
         
         this.paintChartValues(target);
         this.paintChartOptions(target);
     }
     
     private void paintChartValues(PaintTarget target) throws PaintException {
         
         target.addVariable(this, VSpiderChartComponent.UIDL_DATA_AXIS_VALUES, this.getSpiderChart().getAxisNames());
         target.addVariable(this, VSpiderChartComponent.UIDL_DATA_AXIS_COUNT, this.getSpiderChart().getAxisNames().length);
         
         target.addVariable(this, VSpiderChartComponent.UIDL_DATA_SERIES_COUNT, this.getSpiderChart().getSeries().size());
         
         double maxValue = 0d;
         String[] serieNames = new String[this.getSpiderChart().getSeries().size()];
         for (int i = 0; i < this.getSpiderChart().getSeries().size(); i++) {
             Serie s = this.getSpiderChart().getSeries().get(i);
             serieNames[i] = s.getName();
             
             String[] sValues = new String[this.getSpiderChart().getAxisNames().length + 1];
             String[] tooltips = new String[this.getSpiderChart().getAxisNames().length];
             for (int j = 0; j < sValues.length; j++) {
                 double val = 0d;
                 if (j < s.getValues().length) {
                     val = s.getValues()[j];
                     maxValue = Math.max(maxValue, val);
                 }
                 sValues[j] = String.valueOf(val);
                 
                 if (this.chart.isTooltipEnabled() && j < tooltips.length) {
                     if (this.getSpiderChart().getAxisNames() != null && j < this.getSpiderChart().getAxisNames().length) {
                         tooltips[j] = this.tooltipFormatter.getTooltipHTML(this.getSpiderChart().getAxisNames()[j], s.getName(), val);
                     } else {
                         tooltips[j] = this.tooltipFormatter.getTooltipHTML("", s.getName(), val);
                     }
                 }
             }
             //Need to add first value for circle completion
             if (s.getValues().length > 0) {
                 sValues[this.getSpiderChart().getAxisNames().length] = String.valueOf(s.getValues()[0]);
             } else {
                 sValues[this.getSpiderChart().getAxisNames().length] = String.valueOf(0d);
             }
             target.addVariable(this, VSpiderChartComponent.UIDL_DATA_SERIE_VALUE + i, sValues);
             target.addVariable(this, VSpiderChartComponent.UIDL_DATA_SERIE_TOOLTIP_VALUES + i, tooltips);
         }
         target.addVariable(this, VSpiderChartComponent.UIDL_DATA_SERIES_NAMES, serieNames);
         target.addVariable(this, VSpiderChartComponent.UIDL_DATA_MAX_VALUE, maxValue);
         
         //Axis label management
         if (this.getSpiderChart().isAxisLabelEnabled()) {
             Range r = Range.getAutoRange(0, maxValue, this.getSpiderChart().getAxisLabelStep());
             
             Double[] rangeValues = r.getRangeArray();
             String[] labels = new String[rangeValues.length];
             String[] range = new String[rangeValues.length];
             for (int i = 0; i < rangeValues.length; i++) {
                 labels[i] = this.axisLabelFormatter.format(rangeValues[i]);
                 range[i] = String.valueOf(rangeValues[i]);
             }
             target.addVariable(this, VSpiderChartComponent.UIDL_DATA_AXIS_LABEL_VALUES, labels);
             target.addVariable(this, VSpiderChartComponent.UIDL_DATA_AXIS_LABEL_RANGE, range);
         }
     }
     
     private void paintChartOptions(PaintTarget target) throws PaintException {
         double availableHeight = this.chart.getHeight() - this.chart.getMarginBottom() - this.chart.getMarginTop();
         double availableWidth = this.chart.getWidth() - this.chart.getMarginLeft() - this.chart.getMarginRight() - this.chart.getLegendAreaWidth();
         
         target.addVariable(this, VSpiderChartComponent.UIDL_OPTIONS_BOTTOM, this.getAutoCenterBottom(availableHeight));
         target.addVariable(this, VSpiderChartComponent.UIDL_OPTIONS_LEFT, this.getAutoCenterLeft(availableWidth));
         target.addVariable(this, VSpiderChartComponent.UIDL_OPTIONS_RULE_WIDTH, this.getAutoRuleWidth(availableWidth, availableHeight));
         
         target.addVariable(this, VSpiderChartComponent.UIDL_OPTIONS_AREA_ENABLED, this.getSpiderChart().isAreaMode());
         target.addVariable(this, VSpiderChartComponent.UIDL_OPTIONS_AREA_OPACITY, this.getSpiderChart().getOpacity());
         target.addVariable(this, VSpiderChartComponent.UIDL_OPTIONS_AXIS_ENABLED, this.getSpiderChart().isAxisEnabled());
         target.addVariable(this, VSpiderChartComponent.UIDL_OPTIONS_AXIS_GRID_ENABLED, this.getSpiderChart().isAxisGridEnabled());
         target.addVariable(this, VSpiderChartComponent.UIDL_OPTIONS_AXIS_LABEL_ENABLED, this.getSpiderChart().isAxisLabelEnabled());
         target.addVariable(this, VSpiderChartComponent.UIDL_OPTIONS_AXIS_CAPTION_ENABLED, this.getSpiderChart().isAxisCaptionEnabled());
         target.addVariable(this, VSpiderChartComponent.UIDL_OPTIONS_AXIS_OFFSET, this.getSpiderChart().getAxisOffset());
         target.addVariable(this, VSpiderChartComponent.UIDL_OPTIONS_AXIS_STEP, this.getSpiderChart().getAxisLabelStep());
         target.addVariable(this, VSpiderChartComponent.UIDL_OPTIONS_LINE_WIDTH, this.getSpiderChart().getLineWidth());
     }
     
     private double getAutoCenterBottom(double availableHeight) {
         
         return (availableHeight / 2) + this.chart.getMarginBottom();
     }
     
     private double getAutoCenterLeft(double availableWidth) {
         
         return (availableWidth / 2) + this.chart.getMarginLeft();
     }
     
     private double getAutoRuleWidth(double availableWidth, double availableHeight) {
         return Math.min(availableHeight, availableWidth) / 2;
         
     }
 }
