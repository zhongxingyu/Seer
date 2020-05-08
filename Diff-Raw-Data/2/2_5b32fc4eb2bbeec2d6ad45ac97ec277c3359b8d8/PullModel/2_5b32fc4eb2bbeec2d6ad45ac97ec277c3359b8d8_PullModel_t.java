 package edu.uci.lighthouse.core.controller;
 
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.log4j.Logger;
 import org.eclipse.jface.preference.IPreferenceStore;
 
 import edu.uci.lighthouse.core.Activator;
 import edu.uci.lighthouse.model.LighthouseAuthor;
 import edu.uci.lighthouse.model.LighthouseClass;
 import edu.uci.lighthouse.model.LighthouseEntity;
 import edu.uci.lighthouse.model.LighthouseEvent;
 import edu.uci.lighthouse.model.LighthouseModel;
 import edu.uci.lighthouse.model.LighthouseModelManager;
 import edu.uci.lighthouse.model.LighthouseModelUtil;
 import edu.uci.lighthouse.model.LighthouseRelationship;
 import edu.uci.lighthouse.model.jpa.JPAException;
 import edu.uci.lighthouse.model.jpa.LHEventDAO;
 
 public class PullModel {
 
 	private LighthouseModel model;
 	
 	/**
 	 * Timestamp of the last event received from the database.
 	 */
 	private Date timestamp = new Date(0);
 	
 	private final String PREFSTORE_KEY = Activator.PLUGIN_ID + "lastDBAccess";
 	
 	private static PullModel instance;
 
 	private static Logger logger = Logger.getLogger(PullModel.class);
 
 	private PullModel(LighthouseModel lighthouseModel) {
 		this.model = lighthouseModel;
 		loadTimestamp();
 	}
 
 	public static PullModel getInstance() {
 		if (instance == null){
 			instance = new PullModel(LighthouseModel.getInstance());
 		}
 		return instance;
 	}
 	
 	/**
 	 * Timeout procedure will get all new events (timestamp > lastDBaccessTime)
 	 * @param lastDBaccessTime Last time that we accessed the database
 	 * @return 
 	 * @throws JPAException 
 	 * */
 	public List<LighthouseEvent>  getNewEventsFromDB(LighthouseAuthor author) throws JPAException {
 		Map<String, Object> parameters = new HashMap<String, Object>();
 		parameters.put("timestamp", timestamp);
 		parameters.put("author", author);
 		List<LighthouseEvent> listEvents = new LHEventDAO().executeNamedQuery("LighthouseEvent.findByTimestamp", parameters);
 		updateTimestamp(listEvents);
 		return listEvents;
 	}
 
 	private void updateTimestamp(List<LighthouseEvent> events) {
 		if (!events.isEmpty()) {
 			for (LighthouseEvent event : events) {
				timestamp = event.getTimestamp();
 				Date committedTime = event.getCommittedTime();
 				Date time = committedTime.after(timestamp) ? committedTime : timestamp;
 				timestamp = (time.after(timestamp)) ? time : timestamp;
 			}
 			saveTimestamp();
 		}
 	}
 	
 	private void loadTimestamp(){
 		IPreferenceStore prefStore = Activator.getDefault()
 		.getPreferenceStore();
 		timestamp = new Date(prefStore.getLong(PREFSTORE_KEY));
 	}
 	
 	private void saveTimestamp(){
 		IPreferenceStore prefStore = Activator.getDefault()
 		.getPreferenceStore();
 		prefStore.setValue(PREFSTORE_KEY, timestamp.getTime());
 	}
 
 	public Collection<LighthouseEvent> executeQueryCheckout(WorkingCopy workingCopy) throws JPAException {
 		logger.info("GET IN: PullModel.executeQueryCheckout()");
 		LighthouseModelManager modelManager = new LighthouseModelManager(model);
 		LinkedHashSet<LighthouseEvent> eventsToFire = new LinkedHashSet<LighthouseEvent>();
 		for (Map.Entry<String, Date> entryWorkingCopy : workingCopy.entrySet()) {
 			String fqnClazz = entryWorkingCopy.getKey();
 			Date revisionTimestamp = entryWorkingCopy.getValue();
 
 			HashMap<LighthouseClass, Collection<LighthouseEntity>> map = modelManager.selectEntitiesInsideClass(fqnClazz);
 
 			for ( Entry<LighthouseClass, Collection<LighthouseEntity>> entryEntitesInside : map.entrySet()) {
 				Collection<LighthouseEntity> listEntitiesInside = entryEntitesInside.getValue();
 				listEntitiesInside.add(entryEntitesInside.getKey());
 				List<LighthouseEvent> listEvents = new LHEventDAO().executeQueryCheckOut(listEntitiesInside, revisionTimestamp);
 
 				logger.debug("fqnClazz: " + fqnClazz + "  revisionTimestamp: " + revisionTimestamp + " listEntitiesInside: " + listEntitiesInside.size() + " listEvents: " + listEvents.size());
 				for (LighthouseEvent event : listEvents) {
 					Object artifact = event.getArtifact();
 					if (event.isCommitted() && 	
 							(	(revisionTimestamp.after(event.getCommittedTime())
 									|| revisionTimestamp.equals(event.getCommittedTime()))
 							) ) {
 						if (event.getType()==LighthouseEvent.TYPE.ADD) {
 							if (!LighthouseModelUtil.wasEventRemoved(listEvents,event,null)) {
 								modelManager.addArtifact(artifact);
 								eventsToFire.add(event);
 							}
 						}
 					} else {
 						if (artifact instanceof LighthouseRelationship) {
 							if (!LighthouseModelUtil.isValidRelationship((LighthouseRelationship) artifact, listEntitiesInside)) {
 								continue; // do NOT add relationship
 							}
 						}
 						modelManager.addEvent(event);
 						eventsToFire.add(event);
 					}			
 				}
 
 			}
 		}
 		logger.info("GET OUT: PullModel.executeQueryCheckout()");
 		return eventsToFire;
 	}
 
 }
