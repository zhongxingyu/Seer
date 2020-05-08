 package com.bibounde.vprotovis;
 
 import com.bibounde.vprotovis.chart.pie.DefaultPieLabelFormatter;
 import com.bibounde.vprotovis.chart.pie.DefaultPieTooltipFormatter;
 import com.bibounde.vprotovis.chart.pie.PieChart;
 import com.bibounde.vprotovis.chart.pie.PieLabelFormatter;
 import com.bibounde.vprotovis.chart.pie.PieTooltipFormatter;
 import com.bibounde.vprotovis.chart.pie.Serie;
 import com.bibounde.vprotovis.gwt.client.pie.VPieChartComponent;
 import com.vaadin.terminal.PaintException;
 import com.vaadin.terminal.PaintTarget;
 import com.vaadin.ui.ClientWidget;
 
 /**
  * Pie charts are useful for performing relative comparisons: how do the parts
  * make up the whole?
  * 
  * @author bibounde
  * 
  */
 @ClientWidget(VPieChartComponent.class)
 public class PieChartComponent extends AbstractChartComponent {
 
     private PieLabelFormatter labelFormatter = new DefaultPieLabelFormatter();
     private PieTooltipFormatter tooltipFormatter = new DefaultPieTooltipFormatter();
 
     /**
      * Initializes a newly created PieChartComponent
      */
     public PieChartComponent() {
         super(new PieChart());
         this.setId("v-protovis-piechart-" + this.hashCode());
     }
     
     private PieChart getPieChart() {
         return (PieChart) this.chart;
     }
 
     /**
      * Appends serie value
      * 
      * @param name
      *            name of the serie
      * @param value
      *            value
      * @return serie index
      */
     public int addSerie(String name, double value) {
         return this.getPieChart().addSerie(name, value, false);
     }
 
     /**
      * Appends serie value with highlight policy. Highlighted serie will be
      * shifted
      * 
      * @param name
      *            name of the serie
      * @param value
      *            value
      * @param highlight
      * @return serie index
      */
     public int addSerie(String name, double value, boolean highlight) {
         return this.getPieChart().addSerie(name, value, highlight);
     }
 
     /**
      * Clears all serie values
      */
     public void clearSeries() {
         this.getPieChart().getSeries().clear();
     }
 
     /**
      * Sets highlight offset
      * 
      * @param highlightOffset
      *            new value
      */
     public void setHighlightOffset(double highlightOffset) {
         this.getPieChart().setHighlightOffset(highlightOffset);
     }
 
     /**
      * Sets label visibility
      * 
      * @param visible
      *            label visibility
      */
     public void setLabelVisible(boolean visible) {
         this.getPieChart().setLabelVisible(visible);
     }
 
     /**
      * Sets label color (default black)
      * 
      * @param color
      *            new color to set
      */
     public void setLabelColor(String color) {
         this.getPieChart().setLabelColor(color);
     }
 
     /**
      * Sets the label formatter
      * 
      * @param pieLabelFormatter
      *            label formatter
      */
     public void setLabelFormatter(PieLabelFormatter pieLabelFormatter) {
         if (pieLabelFormatter == null) {
             this.labelFormatter = new DefaultPieLabelFormatter();
         } else {
             this.labelFormatter = pieLabelFormatter;
         }
     }
     
     /**
      * Enables (or disables) the tooltip
      * @param enabled  true to enable the tooltip, otherwise false
      */
     public void setTooltipEnabled(boolean enabled) {
         this.getPieChart().setTooltipEnabled(enabled);
     }
 
     /**
      * Sets the tooltip formatter
      * 
      * @param pieTooltipFormatter
      *            tooltip formatter
      */
     public void setTooltipFormatter(PieTooltipFormatter pieTooltipFormatter) {
         this.setTooltipFormatter(pieTooltipFormatter, false);
     }
 
     /**
      * Sets the tooltip formatter
      * 
      * @param pieTooltipFormatter
      *            tooltip formatter
      * @param permanent
      *            if true, tooltip is always displayed. Otherwise, tooltip
      *            appears on mouseover
      */
     public void setTooltipFormatter(PieTooltipFormatter pieTooltipFormatter, boolean permanent) {
         this.tooltipFormatter = pieTooltipFormatter;
         this.getPieChart().setTooltipEnabled(pieTooltipFormatter != null);
         this.getPieChart().setTooltipPermanent(permanent);
 
     }
 
     /**
      * Sets thickness of the slice.For example, a quick way to separate wedges is
      * to use a white border, at the expense of some accuracy in angle
      * comparison (especially for small wedges)
      * 
      * @param width border width
      */
     public void setLineWidth(int width) {
         this.getPieChart().setLineWidth(width);
     }
     
     /**
      * Sets the line color
      * @see PieChartComponent#setLineWidth(int) 
      * @param color line color
      */
     public void setLineColor(String color) {
         this.getPieChart().setLineColor(color);
     }
 
     @Override
     public void paintContent(PaintTarget target) throws PaintException {
         super.paintContent(target);
 
         this.paintChartValues(target);
         this.paintChartOptions(target);
     }
 
     private void paintChartValues(PaintTarget target) throws PaintException {
 
         target.addVariable(this, VPieChartComponent.UIDL_DATA_SERIES_COUNT, this.getPieChart().getSeries().size());
 
         double sum = 0d;
         int index = 0;
         String[] highlighted = new String[this.getPieChart().getSeries().size()];
         String[] labelValues = new String[this.getPieChart().getSeries().size()];
         String[] tooltips = new String[this.getPieChart().getSeries().size()];
         String[] serieNames = new String[this.getPieChart().getSeries().size()];
 
         for (Serie serie : this.getPieChart().getSeries()) {
             target.addVariable(this, VPieChartComponent.UIDL_DATA_SERIE_VALUE + index, String.valueOf(serie.getValue()));
 
             highlighted[index] = String.valueOf(serie.isHighlight());
             labelValues[index] = this.labelFormatter.isVisible(serie.getValue()) ? this.labelFormatter.format(serie.getValue()) : "";
 
             if (this.getPieChart().isTooltipEnabled()) {
                 tooltips[index] = this.tooltipFormatter.getTooltipHTML(serie.getName(), serie.getValue());
                 target.addVariable(this, VPieChartComponent.UIDL_OPTIONS_SINGLE_TOOLTIP_ENABLED + index, this.tooltipFormatter.isVisible(serie.getName(), serie.getValue()));
             }
 
             serieNames[index] = serie.getName();
 
             sum += serie.getValue();
             index++;
         }
 
         target.addVariable(this, VPieChartComponent.UIDL_DATA_SERIES_HIGHLIGHTED, highlighted);
         target.addVariable(this, VPieChartComponent.UIDL_DATA_LABEL_VALUES, labelValues);
         if (this.getPieChart().isTooltipEnabled()) {
             target.addVariable(this, VPieChartComponent.UIDL_DATA_TOOLTIP_VALUES, tooltips);
         }
         target.addVariable(this, VPieChartComponent.UIDL_DATA_SERIES_NAMES, serieNames);
         target.addVariable(this, VPieChartComponent.UIDL_DATA_SERIES_SUM, sum);
 
     }
 
     private void paintChartOptions(PaintTarget target) throws PaintException {
 
         double radius = this.getAutoRadius();
 
         target.addVariable(this, VPieChartComponent.UIDL_OPTIONS_RADIUS, radius);
         target.addVariable(this, VPieChartComponent.UIDL_OPTIONS_LEFT, this.getAutoLeft());
         target.addVariable(this, VPieChartComponent.UIDL_OPTIONS_BOTTOM, this.getAutoBottom());
         target.addVariable(this, VPieChartComponent.UIDL_OPTIONS_HIGHLIGHT_OFFSET, this.getPieChart().getHighlightOffset());
 
         target.addVariable(this, VPieChartComponent.UIDL_OPTIONS_TOOLTIPS_PERMANENT, this.getPieChart().isTooltipPermanent());
        target.addVariable(this, VPieChartComponent.UIDL_OPTIONS_LABEL_ENABLED, this.getPieChart().isLabelVisible());
         target.addVariable(this, VPieChartComponent.UIDL_OPTIONS_LABEL_COLOR, this.getPieChart().getLabelColor());
         
         target.addVariable(this, VPieChartComponent.UIDL_OPTIONS_LINE_WIDTH, this.getPieChart().getLineWidth());
         target.addVariable(this, VPieChartComponent.UIDL_OPTIONS_LINE_COLOR, this.getPieChart().getLineColor());
     }
 
     private double getAutoRadius() {
         double availableWidth = this.getPieChart().getWidth() - this.getPieChart().getMarginLeft() - this.getPieChart().getMarginRight() - this.getPieChart().getLegendAreaWidth() - this.chart.getLegendInsetLeft();
         double availableHeight = this.getPieChart().getHeight() - this.getPieChart().getMarginTop() - this.getPieChart().getMarginBottom();
 
         return (Math.min(availableWidth, availableHeight) - (this.getPieChart().getHighlightOffset() * 2)) / 2;
     }
 
     private double getAutoLeft() {
         double availableWidth = this.getPieChart().getWidth() - this.getPieChart().getMarginLeft() - this.getPieChart().getMarginRight() - this.getPieChart().getLegendAreaWidth() - this.chart.getLegendInsetLeft() - (2 * this.getPieChart().getHighlightOffset());
         return (availableWidth / 2) + this.getPieChart().getMarginLeft() + this.getPieChart().getHighlightOffset();
     }
 
     private double getAutoBottom() {
         double availableHeight = this.getPieChart().getHeight() - this.getPieChart().getMarginTop() - this.getPieChart().getMarginBottom() - (2 * this.getPieChart().getHighlightOffset());
         return (availableHeight / 2) + this.getPieChart().getMarginBottom() + this.getPieChart().getHighlightOffset();
     }
 }
