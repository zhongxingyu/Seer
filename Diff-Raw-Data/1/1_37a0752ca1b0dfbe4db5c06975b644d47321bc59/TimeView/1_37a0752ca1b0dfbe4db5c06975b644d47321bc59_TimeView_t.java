 package ch.cern.atlas.apvs.client.ui;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.moxieapps.gwt.highcharts.client.Axis;
 import org.moxieapps.gwt.highcharts.client.AxisTitle;
 import org.moxieapps.gwt.highcharts.client.Chart;
 import org.moxieapps.gwt.highcharts.client.ChartTitle;
 import org.moxieapps.gwt.highcharts.client.Credits;
 import org.moxieapps.gwt.highcharts.client.Exporting;
 import org.moxieapps.gwt.highcharts.client.Legend;
 import org.moxieapps.gwt.highcharts.client.Series;
 import org.moxieapps.gwt.highcharts.client.Style;
 import org.moxieapps.gwt.highcharts.client.ToolTip;
 import org.moxieapps.gwt.highcharts.client.ToolTipData;
 import org.moxieapps.gwt.highcharts.client.ToolTipFormatter;
 import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsData;
 import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsFormatter;
 import org.moxieapps.gwt.highcharts.client.labels.DataLabels;
 import org.moxieapps.gwt.highcharts.client.labels.XAxisLabels;
 import org.moxieapps.gwt.highcharts.client.plotOptions.BarPlotOptions;
 import org.moxieapps.gwt.highcharts.client.plotOptions.LinePlotOptions;
 import org.moxieapps.gwt.highcharts.client.plotOptions.Marker;
 
 import ch.cern.atlas.apvs.client.ClientFactory;
 import ch.cern.atlas.apvs.domain.Measurement;
 import ch.cern.atlas.apvs.ptu.shared.MeasurementChangedEvent;
 
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.web.bindery.event.shared.HandlerRegistration;
 
 public class TimeView extends SimplePanel {
 
 	private static final String[] color = { "#4572A7", "#AA4643", "#89A54E", "#80699B", "#3D96AE", "#DB843D", "#92A8CD", "#A47D7C", "#B5CA92" };
 	
 	private ClientFactory clientFactory;
 
 	private HandlerRegistration handler;
 	private Chart chart;
 	private Map<Integer, Series> seriesByPtuId;
 	private Map<Integer, String> colorsByPtuId;
 
 	private int height;
 	private boolean export;
 
 	public TimeView(final ClientFactory clientFactory, int height, boolean export) {
 		this.clientFactory = clientFactory;
 		this.height = height;
 		this.export = export;
 	}
 
 	public void setMeasurement(final String name) {
 		seriesByPtuId = new HashMap<Integer, Series>();
 		colorsByPtuId = new HashMap<Integer, String>();
 		
 		unsubscribe();
 		removeChart();
 		
 		if (name == null) return;
 
 		final long t0 = System.currentTimeMillis();
 		clientFactory.getPtuService().getMeasurements(name,
 				new AsyncCallback<Map<Integer, List<Measurement<Double>>>>() {
 
 					@Override
 					public void onSuccess(
 							Map<Integer, List<Measurement<Double>>> measurements) {
 						if (measurements == null) {
 							return;
 						}
 
 
 						System.err.println("Measurement Map retrieval took "
 								+ (System.currentTimeMillis() - t0)
 								+ " ms for " + measurements.size());
 
 						String unit = "";
 
 						if (measurements.keySet().size() > 0) {
 							List<Measurement<Double>> m = measurements
 									.get(measurements.keySet().iterator()
 											.next());
 							if ((m != null) && (m.size() > 0)) {
 								unit = m.get(0).getUnit();
 							}
 						}
 
 						createChart(name, unit);
 
 						int k = 0;
 						for (Iterator<Integer> i = measurements.keySet()
 								.iterator(); i.hasNext();) {
 							int ptuId = i.next();
 
 							Series series = chart.createSeries().setName(
 									"" + ptuId);
 							seriesByPtuId.put(ptuId, series);
 							colorsByPtuId.put(ptuId, color[k]);
 
 							addHistory(series, measurements.get(ptuId));
 
 							chart.addSeries(series, true, false);
 							k++;
 						}
 
 						add(chart);
 
 						subscribe(null, name);
 					}
 
 					@Override
 					public void onFailure(Throwable caught) {
 						System.err.println("Cannot retrieve Measurements "
 								+ caught);
 					}
 				});
 	}
 
 	public void setMeasurement(final int ptuId, final String name) {
 		seriesByPtuId = new HashMap<Integer, Series>();
 		colorsByPtuId = new HashMap<Integer, String>();
 		
 		unsubscribe();
 		removeChart();
 		
 		if ((ptuId == 0) || (name == null)) return;
 
 		final long t0 = System.currentTimeMillis();
 		clientFactory.getPtuService().getMeasurements(ptuId, name,
 				new AsyncCallback<List<Measurement<Double>>>() {
 
 					@Override
 					public void onSuccess(List<Measurement<Double>> measurements) {
 						if (measurements == null) {
 							return;
 						}
 
 						System.err.println("Measurement retrieval took "
 								+ (System.currentTimeMillis() - t0)
 								+ " ms for " + measurements.size());
 
 						String unit = measurements.size() > 0 ? measurements
 								.get(0).getUnit() : "";
 
 						createChart(name + " (" + ptuId + ")", unit);
 
 						Series series = chart.createSeries()
 								.setName("" + ptuId);
 						seriesByPtuId.put(ptuId, series);
 						colorsByPtuId.put(ptuId, color[0]);
 
 						addHistory(series, measurements);
 
 						chart.addSeries(series, true, false);
 
 						add(chart);
 
 						subscribe(ptuId, name);
 					}
 
 					@Override
 					public void onFailure(Throwable caught) {
 						System.err.println("Cannot retrieve Measurements "
 								+ caught);
 					}
 				});
 	}
 	
 	public String getColor(int ptuId) {
 		return chart != null ? colorsByPtuId.get(ptuId) : null;
 	}
 	
 	private void removeChart() {
 		if (chart != null) {
 			remove(chart);
 			chart = null;
 		}		
 	}
 
 	private void createChart(String name, String unit) {
 		removeChart();
 		
 		chart = new Chart()
 		// same as above
 				.setColors("#4572A7", "#AA4643", "#89A54E", "#80699B", "#3D96AE", "#DB843D", "#92A8CD", "#A47D7C", "#B5CA92")
 				.setType(Series.Type.LINE)
 				.setZoomType(Chart.ZoomType.X)
 				.setWidth100()
 				.setHeight(height)
 				.setChartTitle(new ChartTitle().setText(name).setStyle(new Style().setFontSize("12px")))
 				.setMarginRight(10)
 				.setExporting(new Exporting().setEnabled(export))
 				.setBarPlotOptions(
 						new BarPlotOptions().setDataLabels(new DataLabels()
 								.setEnabled(true)))
 				.setLinePlotOptions(
 						new LinePlotOptions()
 							.setMarker(new Marker()
 								.setEnabled(false))
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
 												+ DateTimeFormat
 														.getFormat(
 																"yyyy-MM-dd HH:mm:ss")
 														.format(new Date(
 																toolTipData
 																		.getXAsLong()))
 												+ "<br/>"
 												+ NumberFormat
 														.getFormat("0.00")
 														.format(toolTipData
 																.getYAsDouble());
 									}
 								}));
 
 		chart.getXAxis().setType(Axis.Type.DATE_TIME).setLabels(
 		// Fix one hour offset in time labels...
 				new XAxisLabels().setFormatter(new AxisLabelsFormatter() {
 
 					@Override
 					public String format(AxisLabelsData axisLabelsData) {
 						Date date = new Date(axisLabelsData.getValueAsLong());
						@SuppressWarnings("deprecation")
 						String pattern = date.getSeconds() == 0 ? "HH:mm" : "HH:mm:ss";
 						return DateTimeFormat.getFormat(pattern).format(date);
 					}
 				}));
 
 		chart.getYAxis().setAllowDecimals(true)
 				.setAxisTitle(new AxisTitle().setText(unit));
 	}
 
 	private void addHistory(Series series,
 			List<Measurement<Double>> measurements) {
 
 		Number[][] data = new Number[measurements.size()][2];
 		
 		for (int i = 0; i < data.length; i++) {
 			Measurement<Double> m = measurements.get(i);
 
 			data[i][0] = m.getDate().getTime();
 			data[i][1] = m.getValue();
 		}
 		
 		series.setPoints(data, false);
 	}
 	
 	private void unsubscribe() {
 		if (handler != null) {
 			handler.removeHandler();
 			handler = null;
 		}
 	}
 
 	private void subscribe(final Integer ptuId, final String name) {
 		unsubscribe();
 		
 		handler = MeasurementChangedEvent.register(clientFactory.getEventBus(),
 				new MeasurementChangedEvent.Handler() {
 
 					@Override
 					public void onMeasurementChanged(
 							MeasurementChangedEvent event) {
 						Measurement<Double> m = event.getMeasurement();
 
 						if ((ptuId != null) && (m.getPtuId() != ptuId)) {
 							return;
 						}
 
 						if (m.getName().equals(name)) {
 							System.err.println("New meas " + m);
 							Series series = seriesByPtuId.get(m.getPtuId());
 							if (series != null) {
 								series.addPoint(m.getDate().getTime(),
 										m.getValue(), true, true, true);
 							}
 						}
 					}
 				});
 	}
 
 	public void redraw() {
 		if (chart != null) {
 			chart.redraw();
 		}
 	}
 }
