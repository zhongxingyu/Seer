 package com.bibounde.vprotovis.gwt.client.pie;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import com.bibounde.vprotovis.gwt.client.Tooltip;
 import com.bibounde.vprotovis.gwt.client.TooltipComposite;
 import com.bibounde.vprotovis.gwt.client.TooltipComposite.ArrowStyle;
 import com.bibounde.vprotovis.gwt.client.TooltipOptions;
 import com.bibounde.vprotovis.gwt.client.UIDLUtil;
 import com.bibounde.vprotovis.gwt.client.UIRectangle;
 import com.bibounde.vprotovis.gwt.client.VAbstractChartComponent;
 import com.google.gwt.event.logical.shared.CloseEvent;
 import com.google.gwt.event.logical.shared.CloseHandler;
 import com.google.gwt.user.client.ui.AbsolutePanel;
 import com.google.gwt.user.client.ui.PopupPanel;
 
 public class VPieChartComponent extends VAbstractChartComponent {
     
     public static final String UIDL_DATA_SERIES_COUNT = "vprotovis.data.series.count";
     public static final String UIDL_DATA_SERIES_SUM = "vprotovis.data.series.sum";
     public static final String UIDL_DATA_SERIES_NAMES = "vprotovis.data.series.names";
     public static final String UIDL_DATA_SERIES_HIGHLIGHTED = "vprotovis.data.series.highlithed";
     public static final String UIDL_DATA_LABEL_VALUES = "vprotovis.data.label.values";
     public static final String UIDL_DATA_SERIE_VALUE = "vprotovis.data.serie.value.";
     public static final String UIDL_DATA_TOOLTIP_VALUES = "vprotovis.data.tooltip.values";
     public static final String UIDL_OPTIONS_BOTTOM = "vprotovis.options.bottom";
     public static final String UIDL_OPTIONS_LEFT = "vprotovis.options.left";
     public static final String UIDL_OPTIONS_RADIUS = "vprotovis.options.radius";
     public static final String UIDL_OPTIONS_HIGHLIGHT_OFFSET = "vprotovis.options.highlight.offset";
     public static final String UIDL_OPTIONS_MARGIN_LEFT = "vprotovis.options.margin.left";
     public static final String UIDL_OPTIONS_MARGIN_RIGHT = "vprotovis.options.margin.right";
     public static final String UIDL_OPTIONS_MARGIN_TOP= "vprotovis.options.margin.top";
     public static final String UIDL_OPTIONS_MARGIN_BOTTOM = "vprotovis.options.margin.bottom";
     public static final String UIDL_OPTIONS_PADDING_LEFT = "vprotovis.options.padding.left";
     public static final String UIDL_OPTIONS_TOOLTIPS_PERMANENT = "vprotovis.options.tooltips.permanent";
     public static final String UIDL_OPTIONS_SINGLE_TOOLTIP_ENABLED = "vprotovis.options.tooltip.enabled.";
     public static final String UIDL_OPTIONS_LABEL_ENABLED = "vprotovis.options.label.enabled";
     public static final String UIDL_OPTIONS_LABEL_COLOR = "vprotovis.options.label.color";
     public static final String UIDL_OPTIONS_LINE_WIDTH = "vprotovis.options.line.width";
     public static final String UIDL_OPTIONS_LINE_COLOR = "vprotovis.options.line.color";
     
     
     /** Set the CSS class name to allow styling. */
     public static final String CLASSNAME = "v-vprotovis-piechart";
     
     private Logger LOGGER = Logger.getLogger(VPieChartComponent.class.getName());
     
     private Map<Integer, Tooltip> tooltipMap = new HashMap<Integer, Tooltip>();
     private AbsolutePanel content;
     
     
     
     /**
      * @see com.bibounde.vprotovis.gwt.client.VAbstractChartComponent#getClassName()
      */
     @Override
     public String getClassName() {
         return CLASSNAME;
     }
 
     public native void render() /*-{
         var vpiechart = this;
 
         var colors = eval(this.@com.bibounde.vprotovis.gwt.client.pie.VPieChartComponent::getColors()());
         var legendColor = $wnd.pv.color("#464646");
 
         var data = eval(this.@com.bibounde.vprotovis.gwt.client.pie.VPieChartComponent::getData()());
         var sum = this.@com.bibounde.vprotovis.gwt.client.pie.VPieChartComponent::getSum()();
         var highlighted = eval(this.@com.bibounde.vprotovis.gwt.client.pie.VPieChartComponent::getHighlighted()());
 
         var chartWidth = this.@com.bibounde.vprotovis.gwt.client.pie.VPieChartComponent::getChartWidth()();
         var chartHeight = this.@com.bibounde.vprotovis.gwt.client.pie.VPieChartComponent::getChartHeight()();
         
         var vis = new $wnd.pv.Panel().canvas(this.@com.bibounde.vprotovis.gwt.client.pie.VPieChartComponent::getDivId()());
         vis.width(chartWidth);
         vis.height(chartHeight);
         
         var marginLeft = this.@com.bibounde.vprotovis.gwt.client.pie.VPieChartComponent::getMarginLeft()();
         var marginRight = this.@com.bibounde.vprotovis.gwt.client.pie.VPieChartComponent::getMarginRight()();
         var marginBottom = this.@com.bibounde.vprotovis.gwt.client.pie.VPieChartComponent::getMarginBottom()();
         var marginTop = this.@com.bibounde.vprotovis.gwt.client.pie.VPieChartComponent::getMarginTop()();
         var legendAreaWidth = this.@com.bibounde.vprotovis.gwt.client.pie.VPieChartComponent::getLegendAreaWidth()();
         var legendInsetLeft = this.@com.bibounde.vprotovis.gwt.client.pie.VPieChartComponent::getLegendInsetLeft()();
         
         var wedgeBottom = this.@com.bibounde.vprotovis.gwt.client.pie.VPieChartComponent::getWedgeBottom()();
         var wedgeLeft = this.@com.bibounde.vprotovis.gwt.client.pie.VPieChartComponent::getWedgeLeft()();
         var wedgeRadius = this.@com.bibounde.vprotovis.gwt.client.pie.VPieChartComponent::getWedgeRadius()();
         var wedgeHighlightOffset = this.@com.bibounde.vprotovis.gwt.client.pie.VPieChartComponent::getWedgeHighlightOffset()();
         
         var wedge = vis.add($wnd.pv.Wedge).data(data);
         wedge.outerRadius(wedgeRadius);
         wedge.angle(function(d){return d / sum * 2 * Math.PI});
         wedge.left(function() {
             return wedgeLeft + Math.cos(this.startAngle() + this.angle() / 2) * ((highlighted[this.index]) ? wedgeHighlightOffset : 0);
         });
         wedge.bottom(function() {
             return wedgeBottom - Math.sin(this.startAngle() + this.angle() / 2) * ((highlighted[this.index]) ? wedgeHighlightOffset : 0);
         });
         wedge.fillStyle(colors.by($wnd.pv.index));
         wedge.strokeStyle(this.@com.bibounde.vprotovis.gwt.client.pie.VPieChartComponent::getLineColor()());
         wedge.lineWidth(this.@com.bibounde.vprotovis.gwt.client.pie.VPieChartComponent::getLineWidth()());
         
         
         //Label management
         if (this.@com.bibounde.vprotovis.gwt.client.pie.VPieChartComponent::isLabelEnabled()()) {
             var labelColor = this.@com.bibounde.vprotovis.gwt.client.pie.VPieChartComponent::getLabelColor()();
             var labelValues = eval(this.@com.bibounde.vprotovis.gwt.client.pie.VPieChartComponent::getLabelValues()());
             var label = wedge.anchor("center").add($wnd.pv.Label);
             label.textAngle(0);
             label.text(function() {
                 return labelValues[this.index];
             });
             label.textStyle(labelColor);
         }
         
         //Tooltip management
         if (this.@com.bibounde.vprotovis.gwt.client.pie.VPieChartComponent::isTooltipEnabled()()) {
         
             //Add anchor in order to obtain coordinates
             var tooltipLeft = new Array();
             var tooltipTop = new Array();
             var coordLabel = wedge.anchor("outer").add($wnd.pv.Label);
             coordLabel.text(function() {
                 tooltipLeft.push(this.left());
                 tooltipTop.push(this.top());
             
                 if (vpiechart.@com.bibounde.vprotovis.gwt.client.pie.VPieChartComponent::isTooltipsPermanent()()) {
                     //Display permanent tooltip
                     vpiechart.@com.bibounde.vprotovis.gwt.client.pie.VPieChartComponent::showTooltip(III)(this.left(), this.top(), this.index);
                 }
                 return "";
             });
             
             if (!this.@com.bibounde.vprotovis.gwt.client.pie.VPieChartComponent::isTooltipsPermanent()()) {
                 //Register dynamic tooltips
                 wedge.event("mouseover", function() {
                     if (vpiechart.@com.bibounde.vprotovis.gwt.client.pie.VPieChartComponent::isTooltipEnabled(I)(this.index)) {
                         var x = tooltipLeft[this.index];
                         var y = tooltipTop[this.index];
                         vpiechart.@com.bibounde.vprotovis.gwt.client.pie.VPieChartComponent::showTooltip(III)(x, y, this.index);
                     }
                 
                     return this;
                 });
                 wedge.event('mouseout', function() {
                     vpiechart.@com.bibounde.vprotovis.gwt.client.pie.VPieChartComponent::hideTooltip(I)(this.index);
                     return this;
                 });
             }
         }
         
         //Legend management
         if (this.@com.bibounde.vprotovis.gwt.client.pie.VPieChartComponent::isLegendEnabled()()) {
             var serieNames = eval(this.@com.bibounde.vprotovis.gwt.client.pie.VPieChartComponent::getSerieNames()());
             
             var legengTop = marginTop + (chartHeight - marginBottom - marginTop - (serieNames.length * 18)) / 2;
             
             //Use bar instead of DOT because msie-shim does not support it
             var legend = vis.add($wnd.pv.Bar).data(serieNames);
             legend.top(function(){
                 return legengTop + (this.index * 18);
             });
             legend.width(11).height(11).left(chartWidth - marginRight - legendAreaWidth + legendInsetLeft);
             legend.fillStyle(colors.by($wnd.pv.index));
             legend.anchor("left").add($wnd.pv.Label).textBaseline("middle").textMargin(16).textStyle(legendColor);
         }
         
         vis.render();
     }-*/;
     
     public void showTooltip(int x, int y, final int tooltipIndex) {
         
         if (this.isTooltipsPermanent()) {
             TooltipComposite composite = new TooltipComposite();
             composite.setText(this.getTooltipValues()[tooltipIndex]);
            
             this.content.add(composite);
             
             TooltipOptions options = this.getTooltipOptions(composite.getOffsetWidth(), composite.getOffsetHeight(), x, y, true);
             composite.setArrowStyle(options.arrowStyle);
             this.content.setWidgetPosition(composite, options.left, options.top);
         } else {
             if (this.tooltipMap.containsKey(tooltipIndex)) {
                 return;
             }
             
             Tooltip tooltip = this.tooltipMap.get(tooltipIndex); 
             
             if (tooltip == null) {
                 tooltip = new Tooltip();
                 tooltip.addCloseHandler(new CloseHandler<PopupPanel>() {
                     
                     public void onClose(CloseEvent<PopupPanel> event) {
                         tooltipMap.remove(tooltipIndex);
                     }
                 });
                 this.tooltipMap.put(tooltipIndex, tooltip);
             }
             tooltip.setText(this.getTooltipValues()[tooltipIndex]);
             
             //Tooltip location calculation
             tooltip.show();
             
             TooltipOptions options = this.getTooltipOptions(tooltip.getOffsetWidth(), tooltip.getOffsetHeight(), x, y, false);
             tooltip.setArrowStyle(options.arrowStyle);
             tooltip.setPopupPosition(options.left, options.top);
         }
     }
     
     public void hideTooltip(int tooltipIndex) {
         if (this.tooltipMap.containsKey(tooltipIndex)) {
             this.tooltipMap.get(tooltipIndex).hide();
         }
     }
     
     
     public String getDivId() {
         return this.currentUIDL.getStringVariable(UIDL_DIV_ID);
     }
     
     public String getData() {
         StringBuilder ret = new StringBuilder("[");
         
         int serieCount = this.currentUIDL.getIntVariable(UIDL_DATA_SERIES_COUNT);
         for (int i = 0; i < serieCount; i++) {
             if (i > 0) {
                 ret.append(", ");
             }
             ret.append(this.currentUIDL.getStringVariable(UIDL_DATA_SERIE_VALUE + i));
         }
         
         ret.append("]");
         return ret.toString();
     }
     
     public String getHighlighted() {
         String[] highlighted = this.currentUIDL.getStringArrayVariable(UIDL_DATA_SERIES_HIGHLIGHTED);
         return UIDLUtil.getJSArray(highlighted, false);
     }
     
     public double getSum() {
         return this.currentUIDL.getDoubleVariable(UIDL_DATA_SERIES_SUM);
     }
     
     public double getWedgeBottom() {
         return this.currentUIDL.getDoubleVariable(UIDL_OPTIONS_BOTTOM);
     }
     
     public double getWedgeLeft() {
         return this.currentUIDL.getDoubleVariable(UIDL_OPTIONS_LEFT);
     }
     
     public double getWedgeHighlightOffset() {
         return this.currentUIDL.getDoubleVariable(UIDL_OPTIONS_HIGHLIGHT_OFFSET);
     }
     
     public double getWedgeRadius() {
         return this.currentUIDL.getDoubleVariable(UIDL_OPTIONS_RADIUS);
     }
     
     
     public boolean isLabelEnabled() {
         return this.currentUIDL.getBooleanVariable(UIDL_OPTIONS_LABEL_ENABLED);
     }
     
     public String getLabelColor() {
         return this.currentUIDL.getStringVariable(UIDL_OPTIONS_LABEL_COLOR);
     }
     
     public String getLabelValues() {
         String[] labelValues = this.currentUIDL.getStringArrayVariable(UIDL_DATA_LABEL_VALUES);
         return UIDLUtil.getJSArray(labelValues, true);
     }
     
     public boolean isTooltipsPermanent() {
         return this.currentUIDL.getBooleanVariable(UIDL_OPTIONS_TOOLTIPS_PERMANENT);
     }
     
     public boolean isTooltipEnabled(int index) {
         return this.currentUIDL.getBooleanVariable(UIDL_OPTIONS_SINGLE_TOOLTIP_ENABLED + index);
     }
     
     public String[] getTooltipValues() {
         return this.currentUIDL.getStringArrayVariable(UIDL_DATA_TOOLTIP_VALUES);
     }
     
     public String getSerieNames() {
         String[] values = this.currentUIDL.getStringArrayVariable(UIDL_DATA_SERIES_NAMES);
         return UIDLUtil.getJSArray(values, true);
     }
     
     public int getLineWidth() {
         return this.currentUIDL.getIntVariable(UIDL_OPTIONS_LINE_WIDTH);
     }
     
     public String getLineColor() {
         return this.currentUIDL.getStringVariable(UIDL_OPTIONS_LINE_COLOR);
     }
     
     private TooltipOptions getTooltipOptions(int tooltipWidth, int tooltipHeight, int x, int y, boolean permanent) {
         
         //Center of the pie (Do not use bottom because 'this' could be not displayed)
         double wedgeLeft = this.getWedgeLeft();
         double wedgeBottom = this.getWedgeBottom();
         double chartHeight = this.getChartHeight();
         
         double centerLeft = -1;
         double centerTop = -1;
         int left = -1;
         int top = -1;
         
         if (permanent) {
             left = x;
             top = y;
             centerLeft = wedgeLeft;
             centerTop = chartHeight - wedgeBottom;
         } else {
             left = this.getElement().getAbsoluteLeft() + x;
             top = this.getElement().getAbsoluteTop() + y;
             centerLeft = this.getElement().getAbsoluteLeft() + wedgeLeft;
             centerTop = this.getElement().getAbsoluteTop() + chartHeight - wedgeBottom;
         }
         
         //TODO: Only tooltip need to know that
         int tooltipArrowOffset = 9;
         int tooltipMarginOffset = 5;
         
         TooltipOptions ret = new TooltipOptions();
         
         double r = this.getWedgeRadius() + this.getWedgeHighlightOffset();
         double h = r / 2;
         
         UIRectangle topLeft = new UIRectangle(centerLeft - r, centerTop - r, r, h);
         if (topLeft.contains(left, top)) {
             ret.arrowStyle = ArrowStyle.BOTTOM_RIGHT;
             ret.left = left - tooltipWidth + tooltipArrowOffset;
             ret.top = top - tooltipHeight - tooltipArrowOffset;
             return ret;
         }
         
         UIRectangle topRight = new UIRectangle(centerLeft, centerTop - r, r, h);
         if (topRight.contains(left, top)) {
             ret.arrowStyle = ArrowStyle.BOTTOM_LEFT;
             ret.left = left - tooltipArrowOffset;
             ret.top = top - tooltipHeight - tooltipArrowOffset;
             return ret;
         }
         
         UIRectangle leftTop = new UIRectangle(centerLeft - r, centerTop - h, r, h);
         if (leftTop.contains(left, top)) {
             ret.arrowStyle = ArrowStyle.MIDDLE_BOTTOM_RIGHT;
             ret.left = left - tooltipWidth - tooltipArrowOffset;
             ret.top = top - tooltipHeight + tooltipMarginOffset;
             
             return ret;
         }
         
         UIRectangle rightTop = new UIRectangle(centerLeft, centerTop - h, r, h);
         if (rightTop.contains(left, top)) {
             ret.arrowStyle = ArrowStyle.MIDDLE_BOTTOM_LEFT;
             ret.left = left;
             ret.top = top - tooltipHeight + tooltipMarginOffset;
         }
         
         UIRectangle leftBottom = new UIRectangle(centerLeft - r, centerTop, r, h);
         if (leftBottom.contains(left, top)) {
             ret.arrowStyle = ArrowStyle.MIDDLE_TOP_RIGHT;
             ret.left = left - tooltipWidth - tooltipArrowOffset;
             ret.top = top - tooltipMarginOffset;
             return ret;
         }
         
         UIRectangle rightBottom = new UIRectangle(centerLeft, centerTop, r, h);
         if (rightBottom.contains(left, top)) {
             ret.arrowStyle = ArrowStyle.MIDDLE_TOP_LEFT;
             ret.left = left;
             ret.top = top - tooltipMarginOffset;
             return ret;
         }
         
         UIRectangle bottomLeft = new UIRectangle(centerLeft - r, centerTop + h, r, h);
         if (bottomLeft.contains(left, top)) {
             ret.arrowStyle = ArrowStyle.TOP_RIGHT;
             ret.left = left - tooltipWidth + tooltipArrowOffset;
             ret.top = top;
             return ret;
         }
         
         UIRectangle bottomRight = new UIRectangle(centerLeft, centerTop + h, r, h);
         if (bottomRight.contains(left, top)) {
             ret.arrowStyle = ArrowStyle.TOP_LEFT;
             ret.left = left - tooltipArrowOffset;
             ret.top = top;
             return ret;
         }
         
         //Default values
         ret.arrowStyle = ArrowStyle.MIDDLE_TOP_LEFT;
         ret.left = left;
         ret.top = top - tooltipMarginOffset;
         
         return ret;
     }
 }
