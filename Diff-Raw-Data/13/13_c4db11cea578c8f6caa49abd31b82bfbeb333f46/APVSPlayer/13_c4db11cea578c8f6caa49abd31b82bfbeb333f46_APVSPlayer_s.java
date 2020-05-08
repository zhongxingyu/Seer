 package ch.cern.atlas.apvs.player;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.moxieapps.gwt.highcharts.client.ChartSubtitle;
 import org.moxieapps.gwt.highcharts.client.ChartTitle;
 import org.moxieapps.gwt.highcharts.client.Credits;
 import org.moxieapps.gwt.highcharts.client.Exporting;
 import org.moxieapps.gwt.highcharts.client.Global;
 import org.moxieapps.gwt.highcharts.client.Highcharts;
 import org.moxieapps.gwt.highcharts.client.Highcharts.Options;
 import org.moxieapps.gwt.highcharts.client.Legend;
 import org.moxieapps.gwt.highcharts.client.Legend.Align;
 import org.moxieapps.gwt.highcharts.client.Legend.Layout;
 import org.moxieapps.gwt.highcharts.client.Legend.VerticalAlign;
 import org.moxieapps.gwt.highcharts.client.Point;
 import org.moxieapps.gwt.highcharts.client.RangeSelector;
 import org.moxieapps.gwt.highcharts.client.RangeSelector.Button;
 import org.moxieapps.gwt.highcharts.client.RangeSelector.ButtonType;
 import org.moxieapps.gwt.highcharts.client.Series;
 import org.moxieapps.gwt.highcharts.client.Series.Type;
 import org.moxieapps.gwt.highcharts.client.StockChart;
 import org.moxieapps.gwt.highcharts.client.ToolTip;
 import org.moxieapps.gwt.highcharts.client.ToolTipData;
 import org.moxieapps.gwt.highcharts.client.ToolTipFormatter;
 import org.moxieapps.gwt.highcharts.client.events.SeriesLegendItemClickEvent;
 import org.moxieapps.gwt.highcharts.client.events.SeriesLegendItemClickEventHandler;
 import org.moxieapps.gwt.highcharts.client.plotOptions.Marker;
 import org.moxieapps.gwt.highcharts.client.plotOptions.SeriesPlotOptions;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.core.client.JsArray;
 import com.google.gwt.core.client.JsArrayNumber;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.jsonp.client.JsonpRequestBuilder;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.RootPanel;
 
 public class APVSPlayer implements EntryPoint {
 
 	static final Logger logger = Logger.getLogger(APVSPlayer.class.getName());
 
 	private Map<String, Series> seriesByName;
 	
 	@Override
 	public void onModuleLoad() {
 
 		GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
 			@Override
 			public void onUncaughtException(Throwable e) {
 				logger.log(Level.SEVERE, "Uncaught exception", e);
 			}
 		});
 		
 		seriesByName = new HashMap<String, Series>();
 
 		Highcharts.setOptions(new Options().setGlobal(new Global()
 				.setUseUTC(true)));
 
 		final StockChart chart = new StockChart();
 
 		chart.setSizeToMatchContainer();
 		chart.setWidth100();
 		chart.setHeight100();
 		
 		chart.setType(Type.LINE);
 		chart.setOption("chart/zoomType", "x");
 		chart.setAnimation(false);
 
 		chart.setSeriesPlotOptions(new SeriesPlotOptions().setAnimation(false)
 				.setMarker(new Marker().setEnabled(false))
 				.setOption("dataGrouping/approximation", "high")
 				.setOption("dataGrouping/enabled", false)
 				.setSeriesLegendItemClickEventHandler(new SeriesLegendItemClickEventHandler() {
 					
 					@Override
 					public boolean onClick(SeriesLegendItemClickEvent event) {
 						Series series = seriesByName.get("Dose-"+event.getSeriesName());
 						if (event.isVisible()) {
 							series.hide();
 						} else {
 							series.show();
 						}
 						return true;
 					}
 				}));
 		
 		chart.setExporting(new Exporting().setEnabled(true));
 
 		chart.setCredits(new Credits().setText("ATLAS Wireless Safety System")
 				.setHref("http://cern.ch/ATWSS"));
 
 		chart.setRangeSelector(new RangeSelector()
 				.setButtons(
 						new Button().setType(ButtonType.MINUTE).setCount(60)
 								.setText("1h"),
 						new Button().setType(ButtonType.MINUTE).setCount(180)
 								.setText("3h"),
 						new Button().setType(ButtonType.ALL).setText("All"))
 				.setInputEnabled(false).setSelected(2));
 
 		chart.getNavigator().setOption("dataGrouping/approximation", "high");
 		chart.getNavigator().setOption("dataGrouping/smoothed", true);
 		chart.getNavigator().setOption("dataGrouping/enabled", true);
 
 		chart.setTitle(new ChartTitle().setText("ATLAS Dosimeters - $name"),
 				new ChartSubtitle().setText("TBD ")); // +unixToLocalTime($unixStart,
 														// $unixEnd)));
 
 		chart.setLegend(new Legend().setEnabled(true).setY(100)
 				.setLayout(Layout.VERTICAL).setBorderWidth(2)
 				.setAlign(Align.RIGHT).setVerticalAlign(VerticalAlign.TOP));
 
 		chart.setToolTip(new ToolTip().setFormatter(new ToolTipFormatter() {
 			
 			@Override
 			public String format(ToolTipData data) {
 				String s = "<b>" + DateTimeFormat.getFormat("EEEE, d MMMM yyyy HH:mm:ss").format(new Date(data.getXAsLong()))+"</b>";
 
 				for (int i=0; i<data.getPointsLength(); i++) {
 					if (i % 2 > 0) {
 						s += " " + data.getYAsDouble(i) + " \u00b5Sv";
 					} else {
 						s += " <br></br>" + data.getSeriesName(i) + ": " + data.getYAsDouble(i) + " \u00b5Sv/h";
 					}
 				}
 			    return s;
 			}
 		}));
 		
 		chart.getXAxis()
 				.setType(
 						org.moxieapps.gwt.highcharts.client.Axis.Type.DATE_TIME)
 				.setOption("ordinal", false);
 
 		chart.getYAxis(0)
 				.setAxisTitleText("Rate [\u00b5Sv/h]")
 				.setOffset(0)
 				.setLineWidth(2)
 				.setMin(0)
 				.setOption("height", 400);
 
 		chart.getYAxis(1)
 				.setAxisTitleText("Dose [\u00b5Sv]")
 				.setOffset(0)
 				.setLineWidth(2)
 				.setMin(0)
 				.setOption("height", 200)
 				.setOption("top", 500);
 		
 		String url = "http://atlas.web.cern.ch/Atlas/TCOORD/CavCom/plot-data.php";
 //		url = "http://ws.geonames.org/postalCodeLookupJSON?postalcode=M1&country=GB&maxRows=4";
 		JsonpRequestBuilder jsonp = new JsonpRequestBuilder();
 		jsonp.requestObject(url, new AsyncCallback<JsArray<JavaScriptObject>>() {
 			
 			@Override
 			public void onSuccess(JsArray<JavaScriptObject> dataArray) {
 				chart.removeAllSeries();
 				seriesByName.clear();
 				
 				for (int i=0; i<dataArray.length(); i++) {
 					JavaScriptObject data = dataArray.get(i);
 					String name = nativeGetName(data);
 					Series series = chart.createSeries().setName(name);
 					seriesByName.put(name, series);
 					
 					Point[] points = getPoints(data);
 					series.setPoints(points);
 					
 					if (name.startsWith("Dose-")) {
 						series.setYAxis(1);
 						series.setOption("showInLegend", false);
 						series.setOption("tooltip/enabled", false);
 						series.setOption("tooltip/crosshairs", false);
 					}
 
 					chart.addSeries(series, true, false);
 				}
 			}
 			
 			@Override
 			public void onFailure(Throwable caught) {
 				Window.alert(""+caught);
 			}
 		});
 
 		RootPanel.get("chart").add(chart);
 	}
 	
     private static Point[] getPoints(JavaScriptObject nativeSeries) {
         ArrayList<Point> convertedPoints = new ArrayList<Point>();
         JsArray<JsArrayNumber> nativePoints = nativeGetData(nativeSeries);
         for (int i = 0; i < nativePoints.length(); i++) {
             JsArrayNumber nativePoint = nativePoints.get(i);
             Point point = new Point(nativePoint.get(0), nativePoint.get(1));
             convertedPoints.add(point);
         }
         return convertedPoints.toArray(new Point[convertedPoints.size()]);
     }
 
     private static native JsArray<JsArrayNumber> nativeGetData(JavaScriptObject series) /*-{
     	return series.data;
 	}-*/;
 
 	private static native String nativeGetName(JavaScriptObject series) /*-{
     	return series.name;
 	}-*/;
 }
