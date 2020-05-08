 package ch.cern.atlas.apvs.client.ui;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.cern.atlas.apvs.client.ClientFactory;
 import ch.cern.atlas.apvs.client.domain.HistoryMap;
 import ch.cern.atlas.apvs.client.domain.Intervention;
 import ch.cern.atlas.apvs.client.domain.InterventionMap;
 import ch.cern.atlas.apvs.client.event.ConnectionStatusChangedRemoteEvent;
 import ch.cern.atlas.apvs.client.event.HistoryMapChangedEvent;
 import ch.cern.atlas.apvs.client.event.InterventionMapChangedRemoteEvent;
 import ch.cern.atlas.apvs.client.event.PtuSettingsChangedRemoteEvent;
 import ch.cern.atlas.apvs.client.event.SelectPtuEvent;
 import ch.cern.atlas.apvs.client.settings.PtuSettings;
 import ch.cern.atlas.apvs.client.widget.UpdateScheduler;
 import ch.cern.atlas.apvs.domain.History;
 import ch.cern.atlas.apvs.domain.Measurement;
 import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;
 import ch.cern.atlas.apvs.eventbus.shared.RequestEvent;
 import ch.cern.atlas.apvs.ptu.shared.MeasurementChangedEvent;
 
 import com.google.gwt.dom.client.Element;
 import com.google.web.bindery.event.shared.EventBus;
 import com.google.web.bindery.event.shared.HandlerRegistration;
 
 public class TimeView extends AbstractTimeView implements Module {
 
 	@SuppressWarnings("unused")
 	private Logger log = LoggerFactory.getLogger(getClass().getName());
 
 	private HandlerRegistration measurementHandler;
 
 	private String ptuId = null;
 	private PtuSettings settings;
 	private InterventionMap interventions;
 	private HistoryMap historyMap;
 
 	private String measurementName = null;
 	private EventBus cmdBus;
 	private String options;
 
 	private ClientFactory clientFactory;
 
 	private UpdateScheduler scheduler = new UpdateScheduler(this);
 
 	public TimeView() {
 	}
 
 	@Override
 	public boolean configure(Element element, ClientFactory clientFactory,
 			Arguments args) {
 
 		this.clientFactory = clientFactory;
 
 		RemoteEventBus eventBus = clientFactory.getRemoteEventBus();
 
 		height = args.getArgInt(0);
 		cmdBus = clientFactory.getEventBus(args.getArg(1));
 		options = args.getArg(2);
 		measurementName = args.getArg(3);
 
 		this.title = !options.contains("NoTitle");
 		this.export = !options.contains("NoExport");
 
 		ConnectionStatusChangedRemoteEvent.subscribe(
 				clientFactory.getRemoteEventBus(),
 				new ConnectionStatusChangedRemoteEvent.Handler() {
 
 					@Override
 					public void onConnectionStatusChanged(
 							ConnectionStatusChangedRemoteEvent event) {
 						switch (event.getConnection()) {
 						case database:
 							showGlass(!event.isOk());
 							break;
 						default:
 							break;
 						}
 					}
 				});
 
 		PtuSettingsChangedRemoteEvent.subscribe(eventBus,
 				new PtuSettingsChangedRemoteEvent.Handler() {
 
 					@Override
 					public void onPtuSettingsChanged(
 							PtuSettingsChangedRemoteEvent event) {
 						settings = event.getPtuSettings();
 						scheduler.update();
 					}
 				});
 
 		InterventionMapChangedRemoteEvent.subscribe(eventBus,
 				new InterventionMapChangedRemoteEvent.Handler() {
 
 					@Override
 					public void onInterventionMapChanged(
 							InterventionMapChangedRemoteEvent event) {
 						interventions = event.getInterventionMap();
 						scheduler.update();
 					}
 				});
 
 		HistoryMapChangedEvent.subscribe(eventBus,
 				new HistoryMapChangedEvent.Handler() {
 
 					@Override
 					public void onHistoryMapChanged(HistoryMapChangedEvent event) {
 						historyMap = event.getHistoryMap();
 						scheduler.update();
 					}
 				});
 
 		if (cmdBus != null) {
 			SelectPtuEvent.subscribe(cmdBus, new SelectPtuEvent.Handler() {
 
 				@Override
 				public void onPtuSelected(final SelectPtuEvent event) {
 					ptuId = event.getPtuId();
 					scheduler.update();
 				}
 			});
 
 			SelectMeasurementEvent.subscribe(cmdBus,
 					new SelectMeasurementEvent.Handler() {
 
 						@Override
 						public void onSelection(SelectMeasurementEvent event) {
 							measurementName = event.getName();
 							scheduler.update();
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
 
 	@Override
 	public boolean update() {
 		unregister();
 		removeChart();
 
 		if (measurementName.equals("")) {
 			return false;
 		}
 
 		if (historyMap == null) {
 			return false;
 		}
 
 		if (interventions == null) {
 			return false;
 		}
 
 		if (ptuId == null) {
 			createChart(measurementName);
 			add(chart);
 
 			for (String ptuId : interventions.getPtuIds()) {
 				if ((settings == null) || settings.isEnabled(ptuId)) {
 
 					if (chart != null) {
 						addSeries(ptuId, getName(ptuId));
 						addHistory(historyMap.get(ptuId, measurementName));
 						chart.setAnimation(false);
 					}
 				}
 
 			}
 
 			register();
 
 			if (cmdBus != null) {
 				ColorMapChangedEvent.fire(cmdBus, getColors());
 			}
 		} else {
 			if ((settings == null) || settings.isEnabled(ptuId)) {
 
 				createChart(measurementName + " (" + ptuId + ")");
 				add(chart);
 
 				addSeries(ptuId, getName(ptuId));
 				addHistory(historyMap.get(ptuId, measurementName));
 
 				cmdBus.fireEvent(new ColorMapChangedEvent(getColors()));
 
 				register();
 			}
 		}
 
 		return false;
 	}
 
 	private String getName(String ptuId) {
 		if (interventions == null) {
 			return ptuId;
 		}
 		Intervention intervention = interventions.get(ptuId);
 		return ((intervention != null) && !intervention.getName().equals("") ? intervention
 				.getName() + " - "
 				: "")
 				+ "" + ptuId;
 	}
 
 	private void addHistory(History history) {
 		if (history == null)
 			return;
 
 		addData(history.getPtuId(), history.getData());
 
 		setUnit(history.getPtuId(), history.getUnit());
 	}
 
 	private void unregister() {
 		if (measurementHandler != null) {
 			measurementHandler.removeHandler();
 			measurementHandler = null;
 		}
 	}
 
 	private void register() {
 		unregister();
 
 		measurementHandler = MeasurementChangedEvent.register(
 				clientFactory.getRemoteEventBus(),
 				new MeasurementChangedEvent.Handler() {
 
 					@Override
 					public void onMeasurementChanged(
 							MeasurementChangedEvent event) {
 						Measurement m = event.getMeasurement();
						if (m.getName().equals(measurementName)) {
 
							addPoint(m.getPtuId(), m.getDate().getTime(),
									m.getValue());
 
							setUnit(m.getPtuId(), m.getUnit());
						}
 					}
 				});
 	}
 }
