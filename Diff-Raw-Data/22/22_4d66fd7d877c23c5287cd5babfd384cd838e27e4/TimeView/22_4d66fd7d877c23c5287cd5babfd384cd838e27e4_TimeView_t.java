 package ch.cern.atlas.apvs.client.ui;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.moxieapps.gwt.highcharts.client.Series;
 import org.moxieapps.gwt.highcharts.client.plotOptions.LinePlotOptions;
 import org.moxieapps.gwt.highcharts.client.plotOptions.Marker;
 
 import ch.cern.atlas.apvs.client.ClientFactory;
 import ch.cern.atlas.apvs.client.NamedEventBus;
 import ch.cern.atlas.apvs.client.event.PtuSettingsChangedEvent;
 import ch.cern.atlas.apvs.client.event.SelectPtuEvent;
 import ch.cern.atlas.apvs.client.settings.PtuSettings;
 import ch.cern.atlas.apvs.domain.Measurement;
 import ch.cern.atlas.apvs.eventbus.shared.RequestEvent;
 import ch.cern.atlas.apvs.ptu.shared.MeasurementChangedEvent;
 
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.web.bindery.event.shared.EventBus;
 import com.google.web.bindery.event.shared.HandlerRegistration;
 
 public class TimeView extends AbstractTimeView {
 
 	private HandlerRegistration measurementHandler;
 
 	private String ptuId = null;
 	private PtuSettings settings;
 	private String measurementName = null;
 	private EventBus cmdBus;
 	private String options;
 
 	public TimeView(final ClientFactory clientFactory, Arguments args) {
 		this.clientFactory = clientFactory;
 
 		cmdBus = NamedEventBus.get(args.getArg(0));
 		options = args.getArg(1);
 		measurementName = args.getArg(2);
 
 		// FIXME handle height
 
 		this.title = !options.contains("NoTitle");
 		this.export = !options.contains("NoExport");
 
 		SelectPtuEvent.subscribe(cmdBus, new SelectPtuEvent.Handler() {
 
 			@Override
 			public void onPtuSelected(final SelectPtuEvent event) {
 				ptuId = event.getPtuId();
 				updateChart();
 			}
 		});
 
 		SelectMeasurementEvent.subscribe(cmdBus,
 				new SelectMeasurementEvent.Handler() {
 
 					@Override
 					public void onSelection(SelectMeasurementEvent event) {
 						measurementName = event.getName();
 						updateChart();
 					}
 				});
 
 		PtuSettingsChangedEvent.subscribe(clientFactory.getRemoteEventBus(),
 				new PtuSettingsChangedEvent.Handler() {
 
 					@Override
 					public void onPtuSettingsChanged(
 							PtuSettingsChangedEvent event) {
 						settings = event.getPtuSettings();
 						updateChart();
 					}
 				});
 
 		RequestEvent.register(cmdBus, new RequestEvent.Handler() {
 
 			@Override
 			public void onRequestEvent(RequestEvent event) {
 				System.err.println("Request " + event.getRequestedClassName());
 				if (event.getRequestedClassName().equals(
 						ColorMapChangedEvent.class.getName())) {
 					cmdBus.fireEvent(new ColorMapChangedEvent(getColors()));
 				}
 			}
 		});
 	}
 
 	private void updateChart() {
 		unsubscribe();
 		removeChart();
 
 		if (measurementName.equals("")) {
 			return;
 		}
 
 		if (ptuId == null) {
 			// Subscribe to all PTUs
 			final long t0 = System.currentTimeMillis();
 			clientFactory
 					.getPtuService()
 					.getMeasurements(
 							measurementName,
 							new AsyncCallback<Map<String, List<Measurement<Double>>>>() {
 
 								@Override
 								public void onSuccess(
 										Map<String, List<Measurement<Double>>> measurements) {
 									if (measurements == null) {
 										return;
 									}
 
 									System.err.println("Measurement Map retrieval of "
 											+ measurementName
 											+ " took "
 											+ (System.currentTimeMillis() - t0)
 											+ " ms for "
 											+ measurements.size()
 											+ " elements");
 
 									String unit = "";
 
 									if (measurements.keySet().size() > 0) {
 										List<Measurement<Double>> m = measurements
 												.get(measurements.keySet()
 														.iterator().next());
 										if ((m != null) && (m.size() > 0)) {
 											unit = m.get(0).getUnit();
 										}
 									}
 
 									createChart(measurementName, unit);
 
 									int k = 0;
 									for (Iterator<String> i = measurements
 											.keySet().iterator(); i.hasNext();) {
 										String ptuId = i.next();
 
 										if ((settings == null)
 												|| settings.isEnabled(ptuId)) {
 
 											Series series = chart
													.createSeries().setName(
															getName(ptuId));
 											pointsById.put(ptuId, 0);
 											seriesById.put(ptuId, series);
 											colorsById.put(ptuId, color[k]);
 
 											addHistory(ptuId, series,
 													measurements.get(ptuId));
 
 											chart.addSeries(series, true, false);
 											k++;
 										}
 									}
 
 									add(chart);
 
 									cmdBus.fireEvent(new ColorMapChangedEvent(
 											getColors()));
 
 									subscribe(null, measurementName);
 								}
 
 								@Override
 								public void onFailure(Throwable caught) {
 									System.err
 											.println("Cannot retrieve Measurements "
 													+ caught);
 								}
 							});
 		} else {
 			// subscribe to single PTU
 			if ((settings == null) || settings.isEnabled(ptuId)) {
 
 				final long t0 = System.currentTimeMillis();
 				clientFactory.getPtuService().getMeasurements(ptuId,
 						measurementName,
 						new AsyncCallback<List<Measurement<Double>>>() {
 
 							@Override
 							public void onSuccess(
 									List<Measurement<Double>> measurements) {
 								if (measurements == null) {
 									System.err.println("Cannot find "
 											+ measurementName);
 									return;
 								}
 
 								System.err.println("Measurement retrieval of "
 										+ measurementName + " of " + ptuId
 										+ " took "
 										+ (System.currentTimeMillis() - t0)
 										+ " ms for " + measurements.size()
 										+ " elements");
 
 								String unit = measurements.size() > 0 ? measurements
 										.get(0).getUnit() : "";
 
 								createChart(measurementName + " (" + ptuId
 										+ ")", unit);
 
 								Series series = chart.createSeries().setName(
										getName(ptuId));
 								pointsById.put(ptuId, 0);
 								seriesById.put(ptuId, series);
 								colorsById.put(ptuId, color[0]);
 
 								addHistory(ptuId, series, measurements);
 
 								chart.addSeries(series, true, false);
 
 								add(chart);
 
 								cmdBus.fireEvent(new ColorMapChangedEvent(
 										getColors()));
 
 								subscribe(ptuId, measurementName);
 							}
 
 							@Override
 							public void onFailure(Throwable caught) {
 								System.err
 										.println("Cannot retrieve Measurements "
 												+ caught);
 							}
 						});
 			}
 		}
 	}
 
	private String getName(String ptuId) {
		return (settings != null && settings.getName(ptuId) != null ? settings
				.getName(ptuId) + " - " : "")
				+ "" + ptuId;
	}

 	private void addHistory(String ptuId, Series series,
 			List<Measurement<Double>> measurements) {
 		if (measurements == null)
 			return;
 
 		Number[][] data = new Number[measurements.size()][2];
 
 		for (int i = 0; i < data.length; i++) {
 			Measurement<Double> m = measurements.get(i);
 
 			data[i][0] = m.getDate().getTime();
 			data[i][1] = m.getValue();
 		}
 
 		series.setPoints(data, false);
 		pointsById.put(ptuId, data.length);
 	}
 
 	private void unsubscribe() {
 		if (measurementHandler != null) {
 			measurementHandler.removeHandler();
 			measurementHandler = null;
 		}
 	}
 
 	private void subscribe(final String ptuId, final String name) {
 		unsubscribe();
 
 		measurementHandler = MeasurementChangedEvent.register(
 				clientFactory.getRemoteEventBus(),
 				new MeasurementChangedEvent.Handler() {
 
 					@Override
 					public void onMeasurementChanged(
 							MeasurementChangedEvent event) {
 						Measurement<Double> m = event.getMeasurement();
 
 						if ((ptuId != null) && (!m.getPtuId().equals(ptuId))) {
 							return;
 						}
 
 						if (m.getName().equals(name)) {
 							System.err.println("New meas " + m);
 							Series series = seriesById.get(m.getPtuId());
 							if (series != null) {
 								Integer numberOfPoints = pointsById.get(ptuId);
 								if (numberOfPoints == null)
 									numberOfPoints = 0;
 								boolean shift = numberOfPoints >= pointLimit;
 								if (!shift) {
 									pointsById.put(ptuId, numberOfPoints + 1);
 								}
 								chart.setLinePlotOptions(new LinePlotOptions()
 										.setMarker(new Marker()
 												.setEnabled(!shift)));
 
 								series.addPoint(m.getDate().getTime(),
 										m.getValue(), true, shift, true);
 							}
 						}
 					}
 				});
 	}
 }
