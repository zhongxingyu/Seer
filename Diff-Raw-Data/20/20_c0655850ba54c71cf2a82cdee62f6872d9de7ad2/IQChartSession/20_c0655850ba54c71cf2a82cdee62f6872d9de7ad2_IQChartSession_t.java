 /**
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 
 package org.openmrs.module.iqchartimport.iq;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.module.iqchartimport.iq.code.ChangeCode;
 import org.openmrs.module.iqchartimport.iq.code.ExitCode;
 import org.openmrs.module.iqchartimport.iq.code.HIVStatusPartCode;
 import org.openmrs.module.iqchartimport.iq.code.HospReasonCode;
 import org.openmrs.module.iqchartimport.iq.code.MaritalCode;
 import org.openmrs.module.iqchartimport.iq.code.ModeCode;
 import org.openmrs.module.iqchartimport.iq.code.SexCode;
 import org.openmrs.module.iqchartimport.iq.code.StatusCode;
 import org.openmrs.module.iqchartimport.iq.code.TBScreenCode;
 import org.openmrs.module.iqchartimport.iq.code.WHOStageCode;
 import org.openmrs.module.iqchartimport.iq.code.TransferCode;
 import org.openmrs.module.iqchartimport.iq.model.Hospitalization;
 import org.openmrs.module.iqchartimport.iq.model.PhyAdherence;
 import org.openmrs.module.iqchartimport.iq.model.Pregnancy;
 import org.openmrs.module.iqchartimport.iq.model.Regimen;
 import org.openmrs.module.iqchartimport.iq.model.TBMedication;
 import org.openmrs.module.iqchartimport.iq.model.TBTreatment;
 import org.openmrs.module.iqchartimport.iq.obs.BaseIQObs;
 import org.openmrs.module.iqchartimport.iq.obs.CD4Obs;
 import org.openmrs.module.iqchartimport.iq.obs.HeightObs;
 import org.openmrs.module.iqchartimport.iq.obs.TBScreenObs;
 import org.openmrs.module.iqchartimport.iq.obs.WHOStageObs;
 import org.openmrs.module.iqchartimport.iq.obs.WeightObs;
 
 import com.healthmarketscience.jackcess.Cursor;
 import com.healthmarketscience.jackcess.Database;
 import com.healthmarketscience.jackcess.Table;
 
 /**
  * Provides access to data in an IQChart database
  */
 public class IQChartSession {
 
 	protected static final Log log = LogFactory.getLog(IQChartSession.class);
 	
 	/**
 	 * Table names
 	 */
 	private static final String TABLE_PATIENT = "mst_ptn";
 	private static final String TABLE_CD4 = "dtl_CD4";
 	private static final String TABLE_HEIGHT = "dtl_height";
 	private static final String TABLE_WEIGHT = "dtl_weight";
 	private static final String TABLE_TBSCREEN = "dtl_TBScreen";
 	private static final String TABLE_TBTREATMENT = "dtl_TBTreat";
 	private static final String TABLE_TBMEDICATIONS = "dtl_meds";
 	private static final String TABLE_PREGNANCY = "dtl_pregnancy";
 	private static final String TABLE_HOSPITALIZATION = "dtl_hosp";
 	private static final String TABLE_PHYADHERENCE = "dtl_phyadh";
 	private static final String TABLE_REGIMEN = "dtl_regimen";
 	private static final String TABLE_WHOSTAGE = "dtl_who";
 	private static final String TABLE_STANDARD_REGIMEN = "lst_stdregimen";
 
 	/**
 	 * Key names
 	 */
 	private static final String PATIENT_KEY = "TRACNetID";
 	
 	private Database database;
 	private Map<Integer, IQPatient> patientCache = new HashMap<Integer, IQPatient>();
 	
 	/**
 	 * Opens the given file as an IQChart database
 	 * @param path the MDB file path to open
 	 * @throws IOException if unable to open file
 	 */
 	public IQChartSession(IQChartDatabase database) throws IOException {
 		this.database = Database.open(new File(database.getPath()));
 	}
 	
 	/**
 	 * Gets the number of patients
 	 * @return the number or -1 if error occurred
 	 */
 	public int getNumPatients() {
 		try {
 			Table patTable = database.getTable(TABLE_PATIENT);
 			return patTable.getRowCount();
 		} catch (IOException e) {
 			return -1;
 		}
 	}
 	
 	/**
 	 * Loads all patients from the database
 	 * @return the patients or null if error occurred
 	 */
 	public List<IQPatient> getPatients() {
 		List<IQPatient> patients = new ArrayList<IQPatient>();
 		try {
 			Table table = database.getTable(TABLE_PATIENT);
 			for (Map<String, Object> row : table) {
 				IQPatient patient = patientFromRow(row);
 				patients.add(patient);
 			}
 			return patients;
 			
 		} catch (IOException e) {		
 		}
 		return null;
 	}
 	
 	/**
 	 * Gets a patient by TracNet ID 
 	 * @param tracnetID the TracNet ID
 	 * @return the patient or null
 	 */
 	public IQPatient getPatient(int tracnetID) {
 		// Check cache first
 		if (patientCache.containsKey(tracnetID))
 			return patientCache.get(tracnetID);
 		
 		try {
 			Table table = database.getTable(TABLE_PATIENT);
 			Map<String, Object> row = Cursor.findRow(table, Collections.singletonMap("TRACNetID", (Object)new Integer(tracnetID)));
 			if (row != null)
 				return patientFromRow(row);
 			
 		} catch (IOException e) {
 		}
 		return null;
 	}
 	
 	/**
 	 * Gets all the obs for the given patient
 	 * @param patient the patient
 	 * @return the obs
 	 */
 	public List<BaseIQObs> getPatientObs(IQPatient patient) {
 		List<BaseIQObs> allObs = new ArrayList<BaseIQObs>();
 		allObs.addAll(getPatientCD4Obs(patient));
 		allObs.addAll(getPatientHeightObs(patient));
 		allObs.addAll(getPatientWeightObs(patient));
 		allObs.addAll(getPatientTBScreenObs(patient));
 		allObs.addAll(getPatientWHOStageObs(patient));
 		
 		Collections.sort(allObs);
 		return allObs;
 	}
 	
 	/**
 	 * Gets all the CD4 obs for the given patient
 	 * @param patient the patient
 	 * @return the obs
 	 */
 	public List<CD4Obs> getPatientCD4Obs(IQPatient patient) {
 		List<CD4Obs> obslist = new ArrayList<CD4Obs>();
 		try {
 			Table table = database.getTable(TABLE_CD4);
 			
 			for (Map<String, Object> row : table) {
 				if ((Integer)row.get(PATIENT_KEY) == patient.getTracnetID()) {
 					CD4Obs obs = new CD4Obs((Date)row.get("date"), (Short)row.get("CD4count"));
 					obs.setTestType((String)row.get("TestType"));
 					obslist.add(obs);
 				}
 			}
 			return obslist;
 			
 		} catch (IOException e) {		
 		}
 		return null;
 	}
 	
 	/**
 	 * Gets all the height obs for the given patient
 	 * @param patient the patient
 	 * @return the obs
 	 */
 	public List<HeightObs> getPatientHeightObs(IQPatient patient) {
 		List<HeightObs> obslist = new ArrayList<HeightObs>();
 		try {
 			Table table = database.getTable(TABLE_HEIGHT);
 			
 			for (Map<String, Object> row : table) {
 				if ((Integer)row.get(PATIENT_KEY) == patient.getTracnetID()) {
 					obslist.add(new HeightObs((Date)row.get("date"), (Short)row.get("Height")));
 				}
 			}
 			return obslist;
 			
 		} catch (IOException e) {		
 		}
 		return null;
 	}
 	
 	/**
 	 * Gets all the weight obs for the given patient
 	 * @param patient the patient
 	 * @return the obs
 	 */
 	public List<WeightObs> getPatientWeightObs(IQPatient patient) {
 		List<WeightObs> obslist = new ArrayList<WeightObs>();
 		try {
 			Table table = database.getTable(TABLE_WEIGHT);
 			
 			for (Map<String, Object> row : table) {
 				if ((Integer)row.get(PATIENT_KEY) == patient.getTracnetID()) {
 					obslist.add(new WeightObs((Date)row.get("date"), (Short)row.get("weight")));
 				}
 			}
 			return obslist;
 			
 		} catch (IOException e) {		
 		}
 		return null;
 	}
 	
 	/**
 	 * Gets all the TB screening obs for the given patient
 	 * @param patient the patient
 	 * @return the obs
 	 */
 	public List<TBScreenObs> getPatientTBScreenObs(IQPatient patient) {
 		List<TBScreenObs> obslist = new ArrayList<TBScreenObs>();
 		try {
 			Table table = database.getTable(TABLE_TBSCREEN);
 			
 			for (Map<String, Object> row : table) {
 				if ((Integer)row.get(PATIENT_KEY) == patient.getTracnetID()) {
 					obslist.add(new TBScreenObs((Date)row.get("date"), TBScreenCode.fromByte((Byte)row.get("TBScreen"))));
 				}
 			}
 			return obslist;
 			
 		} catch (IOException e) {		
 		}
 		return null;
 	}
 	
 	/**
 	 * Gets all the WHO stage obs for the given patient
 	 * @param patient the patient
 	 * @return the obs
 	 */
 	public List<WHOStageObs> getPatientWHOStageObs(IQPatient patient) {
 		List<WHOStageObs> obslist = new ArrayList<WHOStageObs>();
 		try {
 			Table table = database.getTable(TABLE_WHOSTAGE);
 			
 			for (Map<String, Object> row : table) {
 				if ((Integer)row.get(PATIENT_KEY) == patient.getTracnetID()) {
 					obslist.add(new WHOStageObs((Date)row.get("date"), WHOStageCode.fromString((String)row.get("WHO"))));
 				}
 			}
 			return obslist;
 			
 		} catch (IOException e) {		
 		}
 		return null;
 	}
 	
 	/**
 	 * Gets the pregnancies for the given patient
 	 * @param patient the patient
 	 * @return the pregnancies
 	 */
 	public List<Pregnancy> getPatientPregnancies(IQPatient patient) {
 		List<Pregnancy> pregnancies = new ArrayList<Pregnancy>();
 		try {
 			Table table = database.getTable(TABLE_PREGNANCY);
 			
 			for (Map<String, Object> row : table) {
 				if ((Integer)row.get(PATIENT_KEY) == patient.getTracnetID()) {
 					pregnancies.add(new Pregnancy((Date)row.get("DateStart"), (Date)row.get("EstDelivery"), (Date)row.get("DateEnd")));
 				}
 			}
 			return pregnancies;
 			
 		} catch (IOException e) {		
 		}
 		return null;
 	}
 	
 	/**
 	 * Gets the pregnancies for the given patient
 	 * @param patient the patient
 	 * @return the pregnancies
 	 */
 	public List<Hospitalization> getPatientHospitalizations(IQPatient patient) {
 		List<Hospitalization> hospitalizations = new ArrayList<Hospitalization>();
 		try {
 			Table table = database.getTable(TABLE_HOSPITALIZATION);
 			
 			for (Map<String, Object> row : table) {
 				if ((Integer)row.get(PATIENT_KEY) == patient.getTracnetID()) {
 					String facility = (String)row.get("facility");
 					Date dateAdm = (Date)row.get("dateAdm");
 					Date dateDisch = (Date)row.get("dateDisch");
 					HospReasonCode hospReasonCode = HospReasonCode.fromByte((Byte)row.get("reason"));
 					hospitalizations.add(new Hospitalization(facility, dateAdm, dateDisch, hospReasonCode));
 				}
 			}
 			return hospitalizations;
 			
 		} catch (IOException e) {		
 		}
 		return null;
 	}
 	
 	/**
 	 * Gets the ARV regimens for the given patient
 	 * @param patient the patient
 	 * @return the regimens
 	 */
 	public List<Regimen> getPatientRegimens(IQPatient patient) {
 		List<Regimen> regimens = new ArrayList<Regimen>();
 		try {
 			Table table = database.getTable(TABLE_REGIMEN);
 			
 			for (Map<String, Object> row : table) {
 				if ((Integer)row.get(PATIENT_KEY) == patient.getTracnetID()) {
 					String regimen = (String)row.get("Regimen");
 					Date startDate = (Date)row.get("startdate");
 					Date endDate = (Date)row.get("enddate");
 					ChangeCode changeCode = ChangeCode.fromByte((Byte)row.get("changecode"));
 					String otherDetails = (String)row.get("OtherDetails");
 					regimens.add(new Regimen(regimen, startDate, endDate, changeCode, otherDetails));
 				}
 			}
 			return regimens;
 			
 		} catch (IOException e) {		
 		}
 		return null;
 	}
 	
 	/**
 	 * Gets the TB treatments for the given patient
 	 * @param patient the patient
 	 * @return the TB treatments
 	 */
 	public List<TBTreatment> getPatientTBTreatments(IQPatient patient) {
 		List<TBTreatment> tbTreatments = new ArrayList<TBTreatment>();
 		try {
 			Table table = database.getTable(TABLE_TBTREATMENT);
 			
 			for (Map<String, Object> row : table) {
 				if ((Integer)row.get(PATIENT_KEY) == patient.getTracnetID()) {
 					tbTreatments.add(new TBTreatment((Date)row.get("startdate"), (Date)row.get("enddate")));
 				}
 			}
 			return tbTreatments;
 			
 		} catch (IOException e) {		
 		}
 		return null;
 	}
 	
 	/**
 	 * Gets the TB medications for the given patient
 	 * @param patient the patient
 	 * @return the TB medications
 	 */
 	public List<TBMedication> getPatientTBMedications(IQPatient patient) {
 		List<TBMedication> tbMedications = new ArrayList<TBMedication>();
 		try {
 			Table table = database.getTable(TABLE_TBMEDICATIONS);
 			
 			for (Map<String, Object> row : table) {
 				if ((Integer)row.get(PATIENT_KEY) == patient.getTracnetID()) {
 					tbMedications.add(new TBMedication((String)row.get("drug"), (Date)row.get("startdate"), (Date)row.get("enddate")));
 				}
 			}
 			return tbMedications;
 			
 		} catch (IOException e) {		
 		}
 		return null;
 	}
 	
 	/**
 	 * Gets the physical adherence records for the given patient
 	 * @param patient the patient
 	 * @return the physical adherence records
 	 */
 	public List<PhyAdherence> getPatientPhyAdherence(IQPatient patient) {
 		List<PhyAdherence> phyAdherences = new ArrayList<PhyAdherence>();
 		try {
 			Table table = database.getTable(TABLE_PHYADHERENCE);
 			
 			for (Map<String, Object> row : table) {
 				if ((Integer)row.get(PATIENT_KEY) == patient.getTracnetID()) {
					phyAdherences.add(new PhyAdherence((Date)row.get("date"), (Short)row.get("DoseMiss7"), (Short)row.get("pillsremain"), (Boolean)row.get("visitrespected")));
 				}
 			}
 			return phyAdherences;
 			
 		} catch (IOException e) {		
 		}
 		return null;
 	}
 	
 	/**
 	 * Gets the standard regimen names
 	 * @return the regimen names
 	 */
 	public Set<String> getStdRegimens(boolean all) {
 		Set<String> stdRegimens = new TreeSet<String>();
 		try {
 			String tableName = all ? TABLE_REGIMEN : TABLE_STANDARD_REGIMEN;
 			Table table = database.getTable(tableName);
 			
 			for (Map<String, Object> row : table)
 				stdRegimens.add(((String)row.get("Regimen")).trim());
 			
 			return stdRegimens;
 			
 		} catch (IOException e) {		
 		}
 		return null;
 	}
 	
 	/**
 	 * Gets the standard TB drug names
 	 * @return the TB drug names
 	 */
 	public Set<String> getStdTBDrugs() {
 		Set<String> stdDrugs = new TreeSet<String>();
 		try {
 			Table table = database.getTable(TABLE_TBMEDICATIONS);
 			
 			for (Map<String, Object> row : table)
 				stdDrugs.add(((String)row.get("drug")).trim());
 			
 			return stdDrugs;
 			
 		} catch (IOException e) {		
 		}
 		return null;
 	}
 	
 	/**
 	 * Gets a patient from a row
 	 * @param row the row
 	 * @return the patient
 	 */
 	protected IQPatient patientFromRow(Map<String, Object> row) {
 		int tracnetID = (Integer)row.get("TRACNetID");				
 		IQPatient patient = new IQPatient(tracnetID);
 		
 		patient.setLastName((String)row.get("lastName"));
 		patient.setFirstName((String)row.get("firstName"));
 		patient.setHospitalID((String)row.get("hospitalID"));
 		patient.setEnrollDate((Date)row.get("enrollDate"));
 		patient.setSexCode(SexCode.fromByte((Byte)row.get("sexCode")));
 		patient.setDob((Date)row.get("DOB"));
 		patient.setDobEstimated((Boolean)row.get("dobEstimated"));
 		patient.setCellule((String)row.get("cellule"));
 		patient.setSector((String)row.get("sector"));
 		patient.setDistrict((String)row.get("district"));
 		patient.setMaritalStatusCode(MaritalCode.fromByte((Byte)row.get("maritalStatusCode")));
 		patient.setHIVStatusPartCode(HIVStatusPartCode.fromByte((Byte)row.get("HIVStatusPartCode")));
 		patient.setModeCode(ModeCode.fromByte((Byte)row.get("ModeCode")));
 		patient.setModeAdmissionOther((String)row.get("ModeAdmissionOther"));
 		patient.setTransferCode(TransferCode.fromByte((Byte)row.get("NewTransfer")));
 		patient.setARVStartDate((Date)row.get("ARVStartDate"));
 		patient.setStatusCode(StatusCode.fromByte((Byte)row.get("StatusCode")));
 		patient.setExitDate((Date)row.get("ExitDate"));
 		patient.setExitCode(ExitCode.fromByte((Byte)row.get("ExitCode")));
 		patient.setExitReasonOther((String)row.get("ExitReasonOther"));
 		
 		// Store in cache
 		patientCache.put(tracnetID, patient);
 		
 		return patient;
 	}
 	
 	/**
 	 * Closes the database
 	 * @throws IOException
 	 */
 	public void close() throws IOException {
 		database.close();
 	}
 }
