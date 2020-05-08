 package ch.cern.atlas.apvs.client.ui;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.moxieapps.gwt.highcharts.client.Axis;
 import org.moxieapps.gwt.highcharts.client.AxisTitle;
 import org.moxieapps.gwt.highcharts.client.Chart;
 import org.moxieapps.gwt.highcharts.client.ChartTitle;
 import org.moxieapps.gwt.highcharts.client.Color;
 import org.moxieapps.gwt.highcharts.client.Credits;
 import org.moxieapps.gwt.highcharts.client.Exporting;
 import org.moxieapps.gwt.highcharts.client.Legend;
 import org.moxieapps.gwt.highcharts.client.Point;
 import org.moxieapps.gwt.highcharts.client.Series;
 import org.moxieapps.gwt.highcharts.client.Series.Type;
 import org.moxieapps.gwt.highcharts.client.Style;
 import org.moxieapps.gwt.highcharts.client.ToolTip;
 import org.moxieapps.gwt.highcharts.client.ToolTipData;
 import org.moxieapps.gwt.highcharts.client.ToolTipFormatter;
 import org.moxieapps.gwt.highcharts.client.YAxis;
 import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsData;
 import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsFormatter;
 import org.moxieapps.gwt.highcharts.client.labels.DataLabels;
 import org.moxieapps.gwt.highcharts.client.labels.XAxisLabels;
 import org.moxieapps.gwt.highcharts.client.plotOptions.BarPlotOptions;
 import org.moxieapps.gwt.highcharts.client.plotOptions.LinePlotOptions;
 import org.moxieapps.gwt.highcharts.client.plotOptions.Marker;
 import org.moxieapps.gwt.highcharts.client.plotOptions.SeriesPlotOptions;
 
 import ch.cern.atlas.apvs.client.widget.GlassPanel;
 import ch.cern.atlas.apvs.ptu.shared.PtuClientConstants;
 
 import com.google.gwt.event.logical.shared.ResizeEvent;
 import com.google.gwt.event.logical.shared.ResizeHandler;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Widget;
 
 public class AbstractTimeView extends GlassPanel {
 
 	private static final int POINT_LIMIT = 2000; // 5 seconds basis, 12
 													// pnts/min, 720/hour
 	private static final String[] color = { "#AA4643", "#89A54E", "#80699B",
 			"#3D96AE", "#DB843D", "#92A8CD", "#A47D7C", "#B5CA92", "#4572A7" };
 	private Map<String, Integer> pointsById;
 	private Map<String, Series> seriesById;
 	private Map<String, String> colorsById;
 	private Map<String, Series> limitSeriesById;
 
 	protected Chart chart;
 	protected Integer height = null;
 	protected boolean export = true;
 	protected boolean title = true;
 
 	public AbstractTimeView() {
 		super();
 		pointsById = new HashMap<String, Integer>();
 		seriesById = new HashMap<String, Series>();
 		colorsById = new HashMap<String, String>();
 		limitSeriesById = new HashMap<String, Series>();
 
		// Fix for #289
 		Window.addResizeHandler(new ResizeHandler() {
 
 			@Override
 			public void onResize(ResizeEvent event) {
 				if ((chart != null) && (chart.isVisible())) {
 					// table, with 100% width, will be the same as old chart
 					Widget parent = chart.getParent();
 					if (parent != null) {
 						// div, corrected width
 						parent = parent.getParent();
 					}
 					if (parent != null) {
 						int width = parent.getOffsetWidth();
 						if (width > 0) {
 //							Window.alert("Setting width "+width+" from "+parent.getElement().getId());
 							chart.setSize(width, chart.getOffsetHeight(), false);
 						}
 					}
 				}
 			}
 		});
 	}
 
 	public Map<String, String> getColors() {
 		return colorsById;
 	}
 
 	protected void removeChart() {
 		if (chart != null) {
 			remove(chart);
 			chart = null;
 			pointsById.clear();
 			seriesById.clear();
 			colorsById.clear();
 			limitSeriesById.clear();
 		}
 	}
 
 	protected void createChart(String name) {
 		removeChart();
 
 		chart = new Chart()
 				// same as above
 				// FIXME String.format not supported
 				.setColors(
 						// String.format("%s, ", (Object[])color))
 						"#AA4643", "#89A54E", "#80699B", "#3D96AE", "#DB843D",
 						"#92A8CD", "#A47D7C", "#B5CA92", "#4572A7")
 				.setZoomType(Chart.ZoomType.X)
 				.setSizeToMatchContainer()
 				.setWidth100()
 				.setHeight100()
 				.setChartTitle(
 						title ? new ChartTitle().setText(name).setStyle(
 								new Style().setFontSize("12px")) : null)
 				.setMarginRight(10)
 				.setExporting(new Exporting().setEnabled(export))
 				.setBarPlotOptions(
 						new BarPlotOptions().setDataLabels(new DataLabels()
 								.setEnabled(true)))
 				.setLinePlotOptions(
 						new LinePlotOptions().setMarker(
 								new Marker().setEnabled(false))
 								.setShadow(false))
 				.setAnimation(false)
 				.setLegend(new Legend().setEnabled(false))
 				.setCredits(new Credits().setEnabled(false))
 				.setToolTip(
 						new ToolTip().setCrosshairs(true, true).setFormatter(
 								new ToolTipFormatter() {
 									@Override
 									public String format(ToolTipData toolTipData) {
 										return "<b>"
 												+ toolTipData.getSeriesName()
 												+ "</b><br/>"
 												+ getDateTime(toolTipData
 														.getXAsLong())
 												+ "<br/>"
 												+ NumberFormat
 														.getFormat("0.00")
 														.format(toolTipData
 																.getYAsDouble());
 									}
 								}));
 
 		if (height != null) {
 			chart.setHeight(height);
 		}
 
 		chart.getXAxis().setType(Axis.Type.DATE_TIME).setLabels(
 		// Fix one hour offset in time labels...
 				new XAxisLabels().setFormatter(new AxisLabelsFormatter() {
 
 					@Override
 					public String format(AxisLabelsData axisLabelsData) {
 						return getDateTime(axisLabelsData.getValueAsLong());
 					}
 				}));
 		chart.getXAxis().setAxisTitle(new AxisTitle().setText("Time"));
 
 		YAxis yAxis = chart.getYAxis();
 		yAxis.setAllowDecimals(true);
 		yAxis.setAxisTitle(new AxisTitle().setText(""));
 	}
 
 	protected void addSeries(String ptuId, String name, boolean showLimits) {
 
 		Series series = chart.createSeries().setName(name);
 		series.setType(Type.LINE);
 		series.setPlotOptions(new SeriesPlotOptions().setAnimation(false));
 		pointsById.put(ptuId, 0);
 		seriesById.put(ptuId, series);
 		colorsById.put(ptuId, color[chart.getSeries().length]);
 
 		if (showLimits) {
 			Series limitSeries = chart.createSeries();
 			limitSeries.setType(Type.AREA_RANGE);
 			limitSeries.setPlotOptions(new SeriesPlotOptions()
 					.setAnimation(false)
 					.setColor(new Color(161, 231, 231, 0.2)) // #3482d4
 					.setEnableMouseTracking(false));
 			limitSeriesById.put(ptuId, limitSeries);
 
 			chart.addSeries(limitSeries, false, false);
 		}
 		chart.addSeries(series, true, false);
 	}
 
 	protected void setData(String ptuId, Number[][] data, Number[][] limits) {
 		Series series = seriesById.get(ptuId);
 		series.setPoints(data != null ? data : new Number[0][2], false);
 		pointsById.put(ptuId, data != null ? data.length : 0);
 
 		Series limitSeries = limitSeriesById.get(ptuId);
 		if (limitSeries != null) {
 			limitSeries.setPoints(limits != null ? limits : new Number[0][3],
 					false);
 		}
 	}
 
 	protected void addPoint(String ptuId, long time, Number value,
 			Number lowLimit, Number highLimit) {
 		Series series = seriesById.get(ptuId);
 		if (series == null) {
 			return;
 		}
 		Integer numberOfPoints = pointsById.get(ptuId);
 		if (numberOfPoints == null) {
 			numberOfPoints = 0;
 		}
 		boolean shift = numberOfPoints >= POINT_LIMIT;
 		if (!shift) {
 			pointsById.put(ptuId, numberOfPoints + 1);
 		}
 		chart.setLinePlotOptions(new LinePlotOptions().setMarker(new Marker()
 				.setEnabled(!shift)));
 
 		Series limitSeries = limitSeriesById.get(ptuId);
 		if (limitSeries != null) {
 			limitSeries.addPoint(new Point(time, lowLimit, highLimit), false,
 					shift, false);
 		}
 		series.addPoint(time, value, true, shift, false);
 	}
 
 	private static final DateTimeFormat ddMMM = DateTimeFormat
 			.getFormat("dd MMM");
 	private static final DateTimeFormat ddMMMyyyy = DateTimeFormat
 			.getFormat("dd MMM yyyy");
 
 	private static final long DAY = 24 * 60 * 60 * 1000L;
 
 	@SuppressWarnings("deprecation")
 	private String getDateTime(long time) {
 		Date today = new Date();
 		long now = today.getTime();
 		long nextMinute = now + (60 * 1000);
 		long yesterday = now - DAY;
 		long tomorrow = now + DAY;
 		Date date = new Date(time);
 
 		String prefix = "";
 		String postfix = "";
 		String newline = "<br/>";
 		if (time > nextMinute) {
 			prefix = "<b>";
 			postfix = "</b>";
 		} else if (time < yesterday) {
 			prefix = "<i>";
 			postfix = "</i>";
 		}
 
 		String dateTime = PtuClientConstants.timeFormat.format(date);
 		if ((time < yesterday) || (time > tomorrow)) {
 			if (date.getYear() == today.getYear()) {
 				dateTime += newline + ddMMM.format(date);
 			} else {
 				dateTime += newline + ddMMMyyyy.format(date);
 			}
 		}
 
 		return prefix + dateTime + postfix;
 	}
 
 	protected void setUnit(String ptuId, String unit) {
 		Series series = seriesById.get(ptuId);
 		if (series == null) {
 			return;
 		}
 
 		if (chart == null) {
 			return;
 		}
 
 		// fix #96 to put unicode in place of &deg; and &micro;
 		unit = unit.replaceAll("\\&deg\\;", "\u00B0");
 		unit = unit.replaceAll("\\&micro\\;", "\u00B5");
 
 		chart.getYAxis().setAxisTitle(new AxisTitle().setText(unit));
 	}
 
 	public void redraw() {
 		if (chart != null) {
 			chart.redraw();
 		}
 	}
 
 }
