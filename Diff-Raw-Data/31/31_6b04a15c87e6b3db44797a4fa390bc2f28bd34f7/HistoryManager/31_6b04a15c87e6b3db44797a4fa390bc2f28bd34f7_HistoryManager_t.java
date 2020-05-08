 package ch.cern.atlas.apvs.client.manager;
 
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.cern.atlas.apvs.client.domain.HistoryMap;
 import ch.cern.atlas.apvs.client.event.HistoryMapChangedEvent;
 import ch.cern.atlas.apvs.client.event.InterventionMapChangedRemoteEvent;
 import ch.cern.atlas.apvs.client.service.PtuServiceAsync;
 import ch.cern.atlas.apvs.domain.History;
 import ch.cern.atlas.apvs.domain.Measurement;
 import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;
 import ch.cern.atlas.apvs.ptu.shared.MeasurementChangedEvent;
 
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.web.bindery.event.shared.HandlerRegistration;
 
 public class HistoryManager {
 
 	private Logger log = LoggerFactory.getLogger(getClass().getName());
 
 	private static HistoryManager instance;
 	private HistoryMap historyMap;
 	private List<String> ptuIds;
 	private HandlerRegistration measurementRegistration;
 	private RemoteEventBus eventBus;
 
 	private HistoryManager(RemoteEventBus eventBus) {
 		this.eventBus = eventBus;
 
 		// subscribe
 		InterventionMapChangedRemoteEvent.subscribe(eventBus,
 				new InterventionMapChangedRemoteEvent.Handler() {
 
 					@Override
 					public void onInterventionMapChanged(
 							InterventionMapChangedRemoteEvent event) {
 						// FIXME #298
 						System.err
 								.println("FIXME (3x?)... Received............."
 										+ event.getEventBusUUID() + " "
 										+ event.getSourceUUID());
 						List<String> newPtuIds = event.getInterventionMap()
 								.getPtuIds();
 						if (!newPtuIds.equals(ptuIds)) {
 							ptuIds = newPtuIds;
 							update();
 						}
 					}
 				});
 	}
 
 	private void update() {
 		if (measurementRegistration != null) {
 			measurementRegistration.removeHandler();
 			measurementRegistration = null;
 		}
 
 		historyMap = new HistoryMap();
 		PtuServiceAsync.Util.getInstance().getHistoryMap(ptuIds,
 				new AsyncCallback<HistoryMap>() {
 
 					@Override
 					public void onSuccess(HistoryMap result) {
 						historyMap = result;
 
 						// subscribe to further measurements
 						measurementRegistration = MeasurementChangedEvent
 								.register(eventBus,
 										new MeasurementChangedEvent.Handler() {
 
 											@Override
 											public void onMeasurementChanged(
 													MeasurementChangedEvent event) {
 												// Add entry to history
 												Measurement measurement = event
 														.getMeasurement();
 												History history = historyMap.get(
 														measurement.getPtuId(),
 														measurement.getName());
 												if (history == null) {
													// NOTE #314, do not create a new History here... just ignore this PTU
//													history = new History(
//															measurement
//																	.getPtuId(),
//															measurement
//																	.getName(),
//															measurement
//																	.getSamplingRate(),
//															new Number[0][],
//															measurement
//																	.getUnit());
//													historyMap.put(history);
												} else {
													history.addEntry(measurement
															.getDate().getTime(),
															measurement.getValue());
 												}
 											}
 										});
 
 						HistoryMapChangedEvent.fire(eventBus, historyMap);
 					}
 
 					@Override
 					public void onFailure(Throwable caught) {
 						log.warn("Cannot get histories " + caught);
 					}
 				});
 	}
 
 	public static HistoryManager getInstance(RemoteEventBus eventBus) {
 		if (instance == null) {
 			instance = new HistoryManager(eventBus);
 		}
 		return instance;
 	}
 
 }
