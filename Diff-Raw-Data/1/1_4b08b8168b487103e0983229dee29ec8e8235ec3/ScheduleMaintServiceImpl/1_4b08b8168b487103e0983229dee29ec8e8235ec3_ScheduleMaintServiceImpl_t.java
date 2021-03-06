 package org.motechproject.server.omod.sdsched;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * Implementation of {@link ScheduleMaintService}.
  * 
  * @author batkinson
  * 
  */
 public class ScheduleMaintServiceImpl implements ScheduleMaintService {
 
 	private static Log log = LogFactory.getLog(ScheduleMaintServiceImpl.class);
 
 	public static String RESOURCE_NAME = "_DS_SCHEDULER_UTILS_RESOURCE";
 
 	TxSyncManWrapper syncManWrapper;
 	ScheduleAdjuster scheduleAdjuster;
 
 	public void setSyncManWrapper(TxSyncManWrapper txSyncManWrapper) {
 		this.syncManWrapper = txSyncManWrapper;
 	}
 
 	public void setScheduleAdjuster(ScheduleAdjuster scheduleAdjuster) {
 		this.scheduleAdjuster = scheduleAdjuster;
 	}
 
 	/**
 	 * Returns the {@link AffectedPatients} object for the current transaction.
 	 * 
 	 * @return the AffectedPatients object for the current tx, or null if
 	 *         non-existent
 	 */
 	private AffectedPatients getAffectedPatients() {
 		AffectedPatients patients = null;
 		try {
 			patients = (AffectedPatients) syncManWrapper
 					.getResource(RESOURCE_NAME);
 		} catch (ClassCastException e) {
 			log.debug("Cannot cast affected patients", e);
			clearAffectedPatients();
 		}
 		return patients;
 	}
 
 	public AffectedPatients getAffectedPatients(boolean create) {
 		AffectedPatients patients = getAffectedPatients();
 		if (patients == null && create) {
 			patients = new AffectedPatients();
 			syncManWrapper.bindResource(RESOURCE_NAME, patients);
 		}
 		return patients;
 	}
 
 	public void addAffectedPatient(Integer patientId) {
 		getAffectedPatients(true).getAffectedIds().add(patientId);
 	}
 
 	public void removeAffectedPatient(Integer patientId) {
 		AffectedPatients patients = getAffectedPatients();
 		if (patients != null)
 			patients.getAffectedIds().remove(patientId);
 	}
 
 	public void clearAffectedPatients() {
 		syncManWrapper.unbindResource(RESOURCE_NAME);
 	}
 
 	public void requestSynch() {
 		if (!syncManWrapper
 				.containsSynchronization(ScheduleMaintSynchronization.class)) {
 			ScheduleMaintSynchronization schedSync = new ScheduleMaintSynchronization();
 			schedSync.setSchedService(this);
 			syncManWrapper.registerSynchronization(schedSync);
 		}
 	}
 
 	public void updateSchedule(Integer patientId) {
 		scheduleAdjuster.adjustSchedule(patientId);
 	}
 }
