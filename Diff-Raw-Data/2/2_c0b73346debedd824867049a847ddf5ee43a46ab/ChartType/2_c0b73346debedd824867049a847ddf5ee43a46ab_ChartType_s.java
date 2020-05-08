 package com.fusioncharts;
 
 // TODO Change main configuration file to XML and place these properties in there.
 public enum ChartType {
 
 	// add extra values to each enum if desired
 	COLUMN2D(SingleOrMulti.SINGLE, ChartLibrary.CHARTS, "Column2D"), 
 	COLUMN3D(SingleOrMulti.SINGLE, ChartLibrary.CHARTS, "Column3D"), 
 	PIE3D(SingleOrMulti.SINGLE,ChartLibrary.CHARTS, "Pie3D"), 
 	PIE2D(SingleOrMulti.SINGLE, ChartLibrary.CHARTS,"Pie2D"), 
 	LINE(SingleOrMulti.SINGLE, ChartLibrary.CHARTS, "Line"), 
 	BAR2D(SingleOrMulti.SINGLE, ChartLibrary.CHARTS, "Bar2D"), 
 	MSCOLUMN2D(SingleOrMulti.MULTI,ChartLibrary.CHARTS, "MSColumn2D"), 
 	MSCOLUMN3D(SingleOrMulti.MULTI,ChartLibrary.CHARTS, "MSColumn3D"), 
 	MSLINE(SingleOrMulti.MULTI,ChartLibrary.CHARTS, "MSLine"), 
 	MSBAR2D(SingleOrMulti.MULTI,ChartLibrary.CHARTS, "MSBar2D"), 
 	MSBAR3D(SingleOrMulti.MULTI,ChartLibrary.CHARTS, "MSBar3D"), 
 	MSCOMBI2D(SingleOrMulti.MULTI,ChartLibrary.CHARTS, "MSCombi2D"), 
 	MSCOMBI3D(SingleOrMulti.MULTI,ChartLibrary.CHARTS, "MSCombi3D"), 
 	STACKEDCOLUMN2D(SingleOrMulti.MULTI,ChartLibrary.CHARTS, "StackedColumn2D"), 
 	STACKEDCOLUMN3D(SingleOrMulti.MULTI,ChartLibrary.CHARTS, "StackedColumn3D"), 
 	MSCOLUMNLINE3D(SingleOrMulti.MULTI,ChartLibrary.CHARTS, "MSColumnLine3D"), 
 	MSCOLUMN3DLINEDY(SingleOrMulti.MULTI,ChartLibrary.CHARTS, "MSColumn3DLineDY"), 
 	BUBBLE(SingleOrMulti.MULTI,ChartLibrary.CHARTS, "Bubble"), 
 	MARIMEKKO(SingleOrMulti.MULTI,ChartLibrary.CHARTS, "Marimekko"), 
 	SCROLLCOLUMN2D(SingleOrMulti.MULTI,ChartLibrary.CHARTS,"ScrollColumn2D"), 
	SCROLLSTACKEDCOLUMN2D(SingleOrMulti.MULTI,ChartLibrary.CHARTS,"ScrollLine2D"), 
 	SCROLLAREA2D(SingleOrMulti.MULTI,ChartLibrary.CHARTS,"ScrollArea2D"), 
 	SCROLLSTAKEDCOLUMN2D(SingleOrMulti.MULTI,ChartLibrary.CHARTS,"ScrollStackedColumn2D"), 
 	SCROLLCOMBI2D(SingleOrMulti.MULTI,ChartLibrary.CHARTS,"ScrollCombi2D"), 
 	SCROLLCOMBIDY2D(SingleOrMulti.MULTI,ChartLibrary.CHARTS,"ScrollCombiDY2D"), 
 	ZOOMLINE(SingleOrMulti.MULTI,ChartLibrary.CHARTS, "ZoomLine"), 
 	VBULLET(SingleOrMulti.SINGLE,ChartLibrary.WIDGETS, "VBullet"), 
 	HBULLET(SingleOrMulti.SINGLE,ChartLibrary.WIDGETS, "HBullet"), 
 	ANGULARGAUGE(SingleOrMulti.SINGLE,ChartLibrary.WIDGETS, "AngularGauge"), 
 	FUNNEL(SingleOrMulti.SINGLE,	ChartLibrary.CHARTS, "Funnel"), 
 	PYRAMID(SingleOrMulti.SINGLE,ChartLibrary.CHARTS, "Pyramid"),
 	FCMAP(SingleOrMulti.SINGLE,ChartLibrary.MAPS, "FCMap");
 
 	enum SingleOrMulti {
 		SINGLE, MULTI;
 	}
 
 	public enum ChartLibrary {
 		CHARTS, WIDGETS, MAPS, POWER;
 	}
 
 	private final SingleOrMulti singleOrMulti;
 	private final ChartLibrary chartOrWidgetOrMapsOrPower;
 	private String string;
 
 	ChartType(SingleOrMulti singleOrMulti, ChartLibrary chartLibrary, String string) {
 		this.singleOrMulti = singleOrMulti;
 		this.chartOrWidgetOrMapsOrPower = chartLibrary;
 		this.string = string;
 	}
 
 	public boolean isSingleSeries() {
 		if (this.singleOrMulti == SingleOrMulti.SINGLE)
 			return true;
 		return false;
 	}
 
 	public ChartLibrary getChartLibrary() {
 		return chartOrWidgetOrMapsOrPower;
 	}
 
 	public String toString() {
 		return this.string;
 	}
 
 }
