 package ch.cern.atlas.apvs.client.ui;
 
 import java.util.Iterator;
 
 import org.moxieapps.gwt.highcharts.client.Series;
 import org.moxieapps.gwt.highcharts.client.plotOptions.LinePlotOptions;
 import org.moxieapps.gwt.highcharts.client.plotOptions.Marker;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.cern.atlas.apvs.client.ClientFactory;
 import ch.cern.atlas.apvs.client.event.InterventionMapChangedEvent;
 import ch.cern.atlas.apvs.client.event.PtuSettingsChangedEvent;
 import ch.cern.atlas.apvs.client.event.SelectPtuEvent;
 import ch.cern.atlas.apvs.client.settings.InterventionMap;
 import ch.cern.atlas.apvs.client.settings.PtuSettings;
 import ch.cern.atlas.apvs.domain.History;
 import ch.cern.atlas.apvs.domain.Measurement;
 import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;
 import ch.cern.atlas.apvs.eventbus.shared.RequestEvent;
 import ch.cern.atlas.apvs.ptu.shared.MeasurementChangedEvent;
 
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.web.bindery.event.shared.EventBus;
 import com.google.web.bindery.event.shared.HandlerRegistration;
 
 public class TimeView extends AbstractTimeView implements Module {
 
 	private Logger log = LoggerFactory.getLogger(getClass().getName());
 
 	private HandlerRegistration measurementHandler;
 
 	private String ptuId = null;
 	private PtuSettings settings;
 	private InterventionMap interventions;
 	private String measurementName = null;
 	private EventBus cmdBus;
 	private String options;
 
 	public TimeView() {
 	}
 
 	@Override
 	public boolean configure(Element element, ClientFactory clientFactory,
 			Arguments args) {
 
 		this.clientFactory = clientFactory;
 
 		RemoteEventBus eventBus = clientFactory.getRemoteEventBus();
 
 		height = Integer.parseInt(args.getArg(0));
 		cmdBus = clientFactory.getEventBus(args.getArg(1));
 		options = args.getArg(2);
 		measurementName = args.getArg(3);
 
 		this.title = !options.contains("NoTitle");
 		this.export = !options.contains("NoExport");
 
 		PtuSettingsChangedEvent.subscribe(eventBus,
 				new PtuSettingsChangedEvent.Handler() {
 
 					@Override
 					public void onPtuSettingsChanged(
 							PtuSettingsChangedEvent event) {
 						settings = event.getPtuSettings();
 						updateChart();
 					}
 				});
 
 		InterventionMapChangedEvent.subscribe(eventBus,
 				new InterventionMapChangedEvent.Handler() {
 
 					@Override
 					public void onInterventionMapChanged(
 							InterventionMapChangedEvent event) {
 						interventions = event.getInterventionMap();
 						updateChart();
 					}
 				});
 
 		if (cmdBus != null) {
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
 
 			RequestEvent.register(cmdBus, new RequestEvent.Handler() {
 
 				@Override
 				public void onRequestEvent(RequestEvent event) {
 					if (event.getRequestedClassName().equals(
 							ColorMapChangedEvent.class.getName())) {
 						cmdBus.fireEvent(new ColorMapChangedEvent(getColors()));
 					}
 				}
 			});
 		}
 
 		return true;
 	}
 
 	private void updateChart() {
 		deregister();
 		removeChart();
 
 		if (measurementName.equals("")) {
 			return;
 		}
 
 		if (ptuId == null) {
 			if (interventions == null) {
 				return;
 			}
 
 			createChart(measurementName);
 			add(chart);
 
 			// Subscribe to all PTUs
 			int k = 0;
 			for (Iterator<String> i = interventions.getPtuIds().iterator(); i
 					.hasNext(); k++) {
 
 				final String ptuId = i.next();
 				final int z = k;
 				clientFactory.getPtuService().getHistory(ptuId,
 						measurementName, new AsyncCallback<History>() {
 
 							@Override
 							public void onSuccess(History history) {
 								if (history == null) {
 									return;
 								}
 
 								if ((settings == null)
 										|| settings.isEnabled(ptuId)) {
 
 									if (chart != null) {
 									Series series = chart.createSeries()
 											.setName(getName(ptuId));
									chart.setAnimation(false);
 									pointsById.put(ptuId, 0);
 									seriesById.put(ptuId, series);
 									colorsById.put(ptuId, color[z]);
 
 									addHistory(ptuId, series, history);
 
 									chart.addSeries(series, true, false);
 									}
 								}
 
 								if (cmdBus != null) {
 									ColorMapChangedEvent.fire(cmdBus,
 											getColors());
 								}
 
 								register(ptuId, measurementName);
 							}
 
 							@Override
 							public void onFailure(Throwable caught) {
 								log.warn("Cannot retrieve Measurements ",
 										caught);
 							}
 						});
 			}
 		} else {
 			// subscribe to single PTU
 			log.info("***** " + ptuId);
 			if ((settings == null) || settings.isEnabled(ptuId)) {
 
 				final long t0 = System.currentTimeMillis();
 				clientFactory.getPtuService().getHistory(ptuId,
 						measurementName, new AsyncCallback<History>() {
 
 							@Override
 							public void onSuccess(History history) {
 								if (history == null) {
 									log.warn("Cannot find history for "
 											+ measurementName);
 								}
 
 								log.info("Measurement retrieval of "
 										+ measurementName + " of " + ptuId
 										+ " took "
 										+ (System.currentTimeMillis() - t0)
 										+ " ms");
 
 								createChart(measurementName + " (" + ptuId
 										+ ")");
 
 								Series series = chart.createSeries().setName(
 										getName(ptuId));
 								pointsById.put(ptuId, 0);
 								seriesById.put(ptuId, series);
 								colorsById.put(ptuId, color[0]);
 
 								addHistory(ptuId, series, history);
 
 								chart.addSeries(series, true, false);
 
 								add(chart);
 
 								cmdBus.fireEvent(new ColorMapChangedEvent(
 										getColors()));
 
 								register(ptuId, measurementName);
 							}
 
 							@Override
 							public void onFailure(Throwable caught) {
 								log.warn("Cannot retrieve Measurements ",
 										caught);
 							}
 						});
 			}
 		}
 	}
 
 	private String getName(String ptuId) {
 		return (interventions != null && interventions.get(ptuId) != null
 				&& !interventions.get(ptuId).getName().equals("") ? interventions
 				.get(ptuId).getName() + " - "
 				: "")
 				+ "" + ptuId;
 	}
 
 	private void addHistory(String ptuId, Series series, History history) {
 		if (history == null)
 			return;
 
 		Number[][] data = history.getData();
 		series.setPoints(data != null ? data : new Number[0][2], false);
 		pointsById.put(ptuId, data != null ? data.length : 0);
 
 		setUnit(history.getUnit());
 	}
 
 	private void deregister() {
 		if (measurementHandler != null) {
 			measurementHandler.removeHandler();
 			measurementHandler = null;
 		}
 	}
 
 	private void register(final String ptuId, final String name) {
 		deregister();
 
 		measurementHandler = MeasurementChangedEvent.register(
 				clientFactory.getRemoteEventBus(),
 				new MeasurementChangedEvent.Handler() {
 
 					@Override
 					public void onMeasurementChanged(
 							MeasurementChangedEvent event) {
 						Measurement m = event.getMeasurement();
 
 						if ((ptuId != null) && (!m.getPtuId().equals(ptuId))) {
 							return;
 						}
 
 						if (m.getName().equals(name)) {
 							log.info("New meas " + m);
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
 
 								setUnit(m.getUnit());
 
 								series.addPoint(m.getDate().getTime(),
 										m.getValue(), true, shift, true);
 							}
 						}
 					}
 				});
 	}
 }
