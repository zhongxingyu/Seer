 package com.fusioncharts;
 
 public enum ChartType {
 	
 	//add extra values to each enum if desired
 	COLUMN2D (SingleOrMulti.SINGLE,ChartOrWidgetOrMapsOrPower.CHARTS, "Column2D"),
 	COLUMN3D (SingleOrMulti.SINGLE,ChartOrWidgetOrMapsOrPower.CHARTS,  "Column3D"),
 	PIE3D (SingleOrMulti.SINGLE,ChartOrWidgetOrMapsOrPower.CHARTS,  "Pie3D"),
 	PIE2D (SingleOrMulti.SINGLE,ChartOrWidgetOrMapsOrPower.CHARTS,  "Pie2D"),
 	LINE (SingleOrMulti.SINGLE,ChartOrWidgetOrMapsOrPower.CHARTS,  "Line"),
 	BAR2D (SingleOrMulti.SINGLE,ChartOrWidgetOrMapsOrPower.CHARTS,  "Bar2D"),
 	MSCOLUMN2D (SingleOrMulti.MULTI,ChartOrWidgetOrMapsOrPower.CHARTS,  "MSColumn2D"),
 	MSCOLUMN3D (SingleOrMulti.MULTI,ChartOrWidgetOrMapsOrPower.CHARTS,  "MSColumn3D"),
 	MSLINE (SingleOrMulti.MULTI,ChartOrWidgetOrMapsOrPower.CHARTS,  "MSLine"),
 	MSBAR2D (SingleOrMulti.MULTI,ChartOrWidgetOrMapsOrPower.CHARTS,  "MSBar2D"),
 	MSBAR3D (SingleOrMulti.MULTI,ChartOrWidgetOrMapsOrPower.CHARTS,  "MSBar3D"),
 	MSCOMBI2D(SingleOrMulti.MULTI,ChartOrWidgetOrMapsOrPower.CHARTS,  "MSCombi2D"),
 	MSCOMBI3D(SingleOrMulti.MULTI,ChartOrWidgetOrMapsOrPower.CHARTS,  "MSCombi3D"), 
 	STACKEDCOLUMN2D (SingleOrMulti.MULTI,ChartOrWidgetOrMapsOrPower.CHARTS,  "StackedColumn2D"),
 	STACKEDCOLUMN3D (SingleOrMulti.MULTI,ChartOrWidgetOrMapsOrPower.CHARTS,  "StackedColumn3D"),
 	MSCOLUMNLINE3D(SingleOrMulti.MULTI,ChartOrWidgetOrMapsOrPower.CHARTS,  "MSColumnLine3D"),
 	MSCOLUMN3DLINEDY(SingleOrMulti.MULTI,ChartOrWidgetOrMapsOrPower.CHARTS,  "MSColumn3DLineDY"),
 	BUBBLE(SingleOrMulti.MULTI,ChartOrWidgetOrMapsOrPower.CHARTS,  "Bubble"),
 	VBULLET (SingleOrMulti.SINGLE,ChartOrWidgetOrMapsOrPower.WIDGETS,  "VBullet"),
 	HBULLET (SingleOrMulti.SINGLE,ChartOrWidgetOrMapsOrPower.WIDGETS,  "HBullet"),
 	ANGULARGAUGE (SingleOrMulti.SINGLE,ChartOrWidgetOrMapsOrPower.WIDGETS,  "AngularGauge");
 	
 	
 	enum SingleOrMulti {
 		SINGLE,
 		MULTI;
 	}
 	
 	public enum ChartOrWidgetOrMapsOrPower {
 		CHARTS,
 		WIDGETS,
 		MAPS,
 		POWER;
 	}
 	
 	private final SingleOrMulti singleOrMulti;
 	private final ChartOrWidgetOrMapsOrPower chartOrWidgetOrMapsOrPower;
 	private String string;
 	
 	ChartType(SingleOrMulti singleOrMulti,ChartOrWidgetOrMapsOrPower chartOrWidgetOrMapsOrPower , String string)
 	{
 		this.singleOrMulti = singleOrMulti;
 		this.chartOrWidgetOrMapsOrPower = chartOrWidgetOrMapsOrPower;
 		this.string = string;
 	}
 	
 	public boolean isSingleSeries()
 	{
 		if(this.singleOrMulti == SingleOrMulti.SINGLE)
 			return true;
 		return false;
 	}
 	
 	public ChartOrWidgetOrMapsOrPower getChartLibrary()
 	{
 		return chartOrWidgetOrMapsOrPower;
 	}
 	
 	public String toString()
 	{
 		return this.string;
 	}
 	
 }
