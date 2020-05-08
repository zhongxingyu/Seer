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
 package org.openmrs.module.rheapocadapter.impl;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.UUID;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import ca.uhn.hl7v2.model.Varies;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Concept;
 import org.openmrs.ConceptAnswer;
 import org.openmrs.ConceptDatatype;
 import org.openmrs.ConceptMap;
 import org.openmrs.ConceptName;
 import org.openmrs.ConceptNumeric;
 import org.openmrs.ConceptSource;
 import org.openmrs.Drug;
 import org.openmrs.Encounter;
 import org.openmrs.EncounterType;
 import org.openmrs.Location;
 import org.openmrs.Obs;
 import org.openmrs.Patient;
 import org.openmrs.PatientIdentifier;
 import org.openmrs.PatientIdentifierType;
 import org.openmrs.Person;
 import org.openmrs.PersonAttribute;
 import org.openmrs.PersonAttributeType;
 import org.openmrs.PersonName;
 import org.openmrs.User;
 import org.openmrs.api.ConceptService;
 import org.openmrs.api.context.Context;
 import org.openmrs.hl7.handler.ProposingConceptException;
 import org.openmrs.module.rheapocadapter.RHEAConstants;
 import org.openmrs.module.rheapocadapter.RHEAHL7Constants;
 import org.openmrs.module.rheapocadapter.service.MessageTransformer;
 import org.openmrs.module.rheapocadapter.service.TransactionService;
 import org.openmrs.util.OpenmrsConstants;
 import org.openmrs.util.OpenmrsUtil;
 import org.springframework.util.StringUtils;
 
 import ca.uhn.hl7v2.HL7Exception;
 import ca.uhn.hl7v2.app.Application;
 import ca.uhn.hl7v2.app.ApplicationException;
 import ca.uhn.hl7v2.model.DataTypeException;
 import ca.uhn.hl7v2.model.Message;
 import ca.uhn.hl7v2.model.Type;
 import ca.uhn.hl7v2.model.v25.segment.NK1;
 import ca.uhn.hl7v2.model.v25.datatype.CE;
 import ca.uhn.hl7v2.model.v25.datatype.CWE;
 import ca.uhn.hl7v2.model.v25.datatype.CX;
 import ca.uhn.hl7v2.model.v25.datatype.DT;
 import ca.uhn.hl7v2.model.v25.datatype.DTM;
 import ca.uhn.hl7v2.model.v25.datatype.FT;
 import ca.uhn.hl7v2.model.v25.datatype.ID;
 import ca.uhn.hl7v2.model.v25.datatype.NM;
 import ca.uhn.hl7v2.model.v25.datatype.ST;
 import ca.uhn.hl7v2.model.v25.datatype.TM;
 import ca.uhn.hl7v2.model.v25.datatype.TS;
 import ca.uhn.hl7v2.model.v25.datatype.XAD;
 import ca.uhn.hl7v2.model.v25.datatype.XCN;
 import ca.uhn.hl7v2.model.v25.group.ORU_R01_OBSERVATION;
 import ca.uhn.hl7v2.model.v25.group.ORU_R01_ORDER_OBSERVATION;
 import ca.uhn.hl7v2.model.v25.group.ORU_R01_PATIENT_RESULT;
 import ca.uhn.hl7v2.model.v25.message.ADT_A05;
 import ca.uhn.hl7v2.model.v25.message.ORU_R01;
 import ca.uhn.hl7v2.model.v25.segment.MSH;
 import ca.uhn.hl7v2.model.v25.segment.OBR;
 import ca.uhn.hl7v2.model.v25.segment.OBX;
 import ca.uhn.hl7v2.model.v25.segment.ORC;
 import ca.uhn.hl7v2.model.v25.segment.PD1;
 import ca.uhn.hl7v2.model.v25.segment.PID;
 import ca.uhn.hl7v2.model.v25.segment.PV1;
 import ca.uhn.hl7v2.parser.EncodingNotSupportedException;
 import ca.uhn.hl7v2.parser.GenericParser;
 import ca.uhn.hl7v2.parser.Parser;
 
 /**
  *
  */
 public class HL7MessageTransformer implements MessageTransformer, Application {
 
 	private Log log = LogFactory.getLog(HL7MessageTransformer.class);
 
 	private TransactionService service = Context
 			.getService(TransactionService.class);
 
 	private static Integer obxCount = 0;
 
 	int orderObsCount = 0;
 	int counter = 0;
 
 	private ORU_R01 r01 = new ORU_R01();
 	List<Encounter> encounterList = new ArrayList<Encounter>();
 
 	public List<Encounter> getEncounterList() {
 		return encounterList;
 	}
 
 	public void setEncounterList(List<Encounter> encounterList) {
 		this.encounterList = encounterList;
 	}
 
 	public boolean canProcess(Message message) {
 		return message != null && "ORU_R01".equals(message.getName());
 	}
 
 	public Message processMessage(Message message) throws ApplicationException {
 
 		if (message instanceof ORU_R01) {
 
 			try {
 				ORU_R01 oru = (ORU_R01) message;
 				return oru;
 			} catch (ClassCastException e) {
 				log.error("Error casting " + message.getClass().getName()
 						+ " to ORU_R01", e);
 				throw new ApplicationException(
 						"Invalid message type for handler");
 			}
 
 		} else if (message instanceof ADT_A05) {
 			try {
 				ADT_A05 adt = (ADT_A05) message;
 				log.info("ADT Message");
 				return adt;
 
 			} catch (ClassCastException e) {
 				log.error("Error casting " + message.getClass().getName()
 						+ " to ADT_A05 : " + e.getMessage());
 				throw new ApplicationException(
 						"Invalid message type for handler");
 			}
 
 		} else {
 			throw new ApplicationException(
 					"Invalid message sent to ORU_R01/ADT_A05 handler");
 		}
 	}
 
 	/**
 	 * Auto generated method comment
 	 * 
 	 * @param adt
 	 * @return
 	 */
 	public Patient patientFromADT_A051(ADT_A05 adt) throws HL7Exception {
 		PID pid = adt.getPID();
 		log.info("PID =" + pid.toString());
 		Patient patient = getPatient(pid);
 		setParentsNames(patient, adt);
 		return patient;
 	}
 
 	public List<Obs> fromORU_R01toObs(ORU_R01 oru) throws HL7Exception {
 
 		// validate message
 		validate(oru);
 		List<Obs> obses = new ArrayList<Obs>();
 		// extract segments for convenient use below
 		MSH msh = getMSH(oru);
 		PID pid = getPID(oru);
 		String messageControlId = msh.getMessageControlID().getValue();
 		if (log.isDebugEnabled())
 			log.debug("Found HL7 message in inbound queue with control id = "
 					+ messageControlId);
 
 		Patient patient = getPatient(pid);
 		if (log.isDebugEnabled())
 			log.debug("Processing HL7 message for patient "
 					+ patient.getPatientId());
 		// create observations
 		if (log.isDebugEnabled())
 			log.debug("Creating observations for message " + messageControlId
 					+ "...");
 		ORU_R01_PATIENT_RESULT patientResult = oru.getPATIENT_RESULT();
 		int numObr = patientResult.getORDER_OBSERVATIONReps();
 		for (int i = 0; i < numObr; i++) {
 			if (log.isDebugEnabled())
 				log.debug("Processing OBR (" + i + " of " + numObr + ")");
 			ORU_R01_ORDER_OBSERVATION orderObs = patientResult
 					.getORDER_OBSERVATION(i);
 			// the parent obr
 			OBR obr = orderObs.getOBR();
 			int numObs = orderObs.getOBSERVATIONReps();
 			for (int j = 0; j < numObs; j++) {
 				if (log.isDebugEnabled())
 					log.debug("Processing OBS (" + j + " of " + numObs + ")");
 
 				OBX obx = orderObs.getOBSERVATION(j).getOBX();
 				PV1 pv1 = oru.getPATIENT_RESULT().getPATIENT().getVISIT()
 						.getPV1();
 				Obs obs;
 				try {
 					obs = parseObs(obx, obr, pid, pv1);
 
 					if (!obs.getValueText().equals(null))
 						obs.setValueText(obs.getValueText() + "/"
 								+ oru.toString());
 					else
 						obs.setValueText(oru.toString());
 
 					obses.add(obs);
 				} catch (ParseException e) {
 					log.error("Unable to parse" + e.getMessage());
 				}
 			}
 		}
 		return (obses.isEmpty()) ? obses : null;
 
 	}
 
 	private void validate(Message message) throws HL7Exception {
 		// TODO: check version, etc.
 	}
 
 	private MSH getMSH(ORU_R01 oru) {
 		return oru.getMSH();
 	}
 
 	private PID getPID(ORU_R01 oru) {
 		return oru.getPATIENT_RESULT().getPATIENT().getPID();
 	}
 
 	private Obs parseObs(OBX obx, OBR obr, PID pid, PV1 pv1)
 			throws HL7Exception, ParseException {
 		if (log.isDebugEnabled())
 			log.debug("parsing observation: " + obx);
 		Encounter encounter = new Encounter();
 		if (pv1 != null) {
 			encounter.setEncounterDatetime(DateFormat.getDateTimeInstance()
 					.parse(pv1.getAdmitDateTime().getTime().getValue()));
 		}
 		Varies[] values = obx.getObservationValue();
 
 		// bail out if no values were found
 		if (values == null || values.length < 1)
 			return null;
 
 		String hl7Datatype = values[0].getName();
 		if (log.isDebugEnabled())
 			log.debug("  datatype = " + hl7Datatype);
 		Concept concept = getConcept(obx.getObservationIdentifier());
 		if (log.isDebugEnabled())
 			log.debug("  concept = " + concept.getConceptId());
 		ConceptName conceptName = getConceptName(obx.getObservationIdentifier());
 		if (log.isDebugEnabled())
 			log.debug("  concept-name = " + conceptName);
 
 		Date datetime = getDatetime(obx);
 		if (log.isDebugEnabled())
 			log.debug("  timestamp = " + datetime);
 		if (datetime == null)
 			datetime = new Date();
 
 		Obs obs = new Obs();
 		Patient patient = getPatient(pid);
 		obs.setPerson(patient);
 		obs.setConcept(concept);
 		obs.setEncounter(encounter);
 		obs.setObsDatetime(datetime);
 		obs.setLocation(null);
 		obs.setCreator(Context.getAuthenticatedUser());
 		obs.setDateCreated(getDatetime(obr));
 		Type obx5 = values[0].getData();
 		if ("NM".equals(hl7Datatype)) {
 			String value = ((NM) obx5).getValue();
 			if (value == null || value.length() == 0) {
 				log.warn("Not creating null valued obs for concept " + concept);
 				return null;
 			} else if (value.equals("0") || value.equals("1")) {
 				obs.setConcept(concept);
 				if (concept.getDatatype().isBoolean()) {
 
 					obs.setValueAsString(value.equals("1") ? "true" : "false");
 
 				} else if (concept.getDatatype().isNumeric())
 					try {
 						obs.setValueNumeric(Double.valueOf(value));
 					} catch (NumberFormatException e) {
 						throw new HL7Exception("numeric (NM) value '" + value
 								+ "' is not numeric for concept #"
 								+ concept.getConceptId() + " ("
 								+ conceptName.getName() + ") ", e);
 					}
 				else if (concept.getDatatype().isCoded()) {
 					Concept answer = value.equals("1") ? Context
 							.getConceptService().getConceptByName("true")
 							: Context.getConceptService().getConceptByName(
 									"false");
 
 					boolean isValidAnswer = false;
 					Collection<ConceptAnswer> conceptAnswers = concept
 							.getAnswers();
 					if (conceptAnswers != null && conceptAnswers.size() > 0) {
 						for (ConceptAnswer conceptAnswer : conceptAnswers) {
 							if (conceptAnswer.getAnswerConcept().equals(answer)) {
 								obs.setValueCoded(answer);
 								isValidAnswer = true;
 								break;
 							}
 						}
 					}
 					// answer the boolean answer concept was't found
 					if (!isValidAnswer)
 						throw new HL7Exception(answer.toString()
 								+ " is not a valid answer for obs with uuid ");
 				} else {
 					// throw this exception to make sure that the handler
 					// doesn't silently ignore bad hl7 message
 					throw new HL7Exception(
 							"Can't set boolean concept answer for concept with id "
 									+ obs.getConcept().getConceptId());
 				}
 			} else {
 				try {
 					obs.setValueNumeric(Double.valueOf(value));
 				} catch (NumberFormatException e) {
 					throw new HL7Exception("numeric (NM) value '" + value
 							+ "' is not numeric for concept #"
 							+ concept.getConceptId() + " ("
 							+ conceptName.getName() + ") in message ", e);
 				}
 			}
 		} else if ("CWE".equals(hl7Datatype)) {
 			log.debug("  CWE observation");
 			CWE value = (CWE) obx5;
 			String valueIdentifier = value.getIdentifier().getValue();
 			log.debug("    value id = " + valueIdentifier);
 			String valueName = value.getText().getValue();
 			log.debug("    value name = " + valueName);
 
 			try {
 				Concept valueConcept = getConcept(value);
 				obs.setValueCoded(valueConcept);
 
 			} catch (NumberFormatException e) {
 				throw new HL7Exception("Invalid concept ID '" + valueIdentifier
 						+ "' for OBX-5 value '" + valueName + "'");
 			}
 
 			if (log.isDebugEnabled())
 				log.debug("  Done with CWE");
 		} else if ("CE".equals(hl7Datatype)) {
 			CE value = (CE) obx5;
 			String valueIdentifier = value.getIdentifier().getValue();
 			String valueName = value.getText().getValue();
 
 			try {
 				obs.setValueCoded(getConcept(value));
 			} catch (NumberFormatException e) {
 				throw new HL7Exception("Invalid concept ID '" + valueIdentifier
 						+ "' for OBX-5 value '" + valueName + "'");
 
 			}
 		} else if ("DT".equals(hl7Datatype)) {
 			DT value = (DT) obx5;
 			Date valueDate = getDate(value.getYear(), value.getMonth(),
 					value.getDay(), 0, 0, 0);
 			if (value == null || valueDate == null) {
 				log.warn("Not creating null valued obs for concept " + concept);
 				return null;
 			}
 			obs.setValueDatetime(valueDate);
 		} else if ("TS".equals(hl7Datatype)) {
 			DTM value = ((TS) obx5).getTime();
 			Date valueDate = getDate(value.getYear(), value.getMonth(),
 					value.getDay(), value.getHour(), value.getMinute(),
 					value.getSecond());
 			if (value == null || valueDate == null) {
 				log.warn("Not creating null valued obs for concept " + concept);
 				return null;
 			}
 			obs.setValueDatetime(valueDate);
 		} else if ("TM".equals(hl7Datatype)) {
 			TM value = (TM) obx5;
 			Date valueTime = getDate(0, 0, 0, value.getHour(),
 					value.getMinute(), value.getSecond());
 			if (value == null || valueTime == null) {
 				log.warn("Not creating null valued obs for concept " + concept);
 				return null;
 			}
 			obs.setValueDatetime(valueTime);
 		} else if ("ST".equals(hl7Datatype)) {
 			ST value = (ST) obx5;
 			if (value == null || value.getValue() == null
 					|| value.getValue().trim().length() == 0) {
 				log.warn("Not creating null valued obs for concept " + concept);
 				return null;
 			}
 			obs.setValueText(value.getValue());
 		} else {
 			// unsupported data type
 			throw new HL7Exception("Unsupported observation datatype '"
 					+ hl7Datatype + "'");
 		}
 		return obs;
 	}
 
 	private ConceptName getConceptName(CE ce) throws HL7Exception {
 		ST altIdentifier = ce.getAlternateIdentifier();
 		ID altCodingSystem = ce.getNameOfAlternateCodingSystem();
 		return getConceptName(altIdentifier, altCodingSystem);
 	}
 
 	private ConceptName getConceptName(CWE cwe) throws HL7Exception {
 		ST altIdentifier = cwe.getAlternateIdentifier();
 		ID altCodingSystem = cwe.getNameOfAlternateCodingSystem();
 		return getConceptName(altIdentifier, altCodingSystem);
 	}
 
 	private ConceptName getConceptName(ST altIdentifier, ID altCodingSystem)
 			throws HL7Exception {
 		if (altIdentifier != null) {
 			String hl7ConceptNameId = altIdentifier.getValue();
 			return getConceptName(hl7ConceptNameId);
 
 		}
 
 		return null;
 	}
 
 	private ConceptName getConceptName(String hl7ConceptNameId)
 			throws HL7Exception {
 		ConceptName specifiedConceptName = null;
 		if (hl7ConceptNameId != null) {
 			// get the exact concept name specified by the id
 			try {
 				Integer conceptNameId = new Integer(hl7ConceptNameId);
 				specifiedConceptName = new ConceptName();
 				specifiedConceptName.setConceptNameId(conceptNameId);
 			} catch (NumberFormatException e) {
 				// if it is not a valid number, more than likely it is a bad hl7
 				// message
 				log.debug("Invalid concept name ID '" + hl7ConceptNameId + "'",
 						e);
 			}
 		}
 		return specifiedConceptName;
 
 	}
 
 	private Date getDate(int year, int month, int day, int hour, int minute,
 			int second) {
 		Calendar cal = Calendar.getInstance();
 		// Calendar.set(MONTH, int) is zero-based, Hl7 is not
 		cal.set(year, month - 1, day, hour, minute, second);
 		return cal.getTime();
 	}
 
 	private Concept getConcept(CE codedElement) throws HL7Exception {
 		String hl7ConceptId = codedElement.getIdentifier().getValue();
 
 		String codingSystem = codedElement.getNameOfCodingSystem().getValue();
 		return getConcept(hl7ConceptId, codingSystem);
 	}
 
 	private Concept getConcept(CWE codedElement) throws HL7Exception {
 		String hl7ConceptId = codedElement.getIdentifier().getValue();
 
 		String codingSystem = codedElement.getNameOfCodingSystem().getValue();
 		return getConcept(hl7ConceptId, codingSystem);
 	}
 
 	protected Concept getConcept(String hl7ConceptId, String codingSystem)
 			throws HL7Exception {
 		Concept concept = Context.getConceptService().getConceptByMapping(
 				hl7ConceptId, codingSystem);
 		if (concept == null) {
 			log.error("Unable to find concept with code: " + hl7ConceptId
 					+ " and mapping: " + codingSystem
 					+ " in hl7 message, a new one created ");
 			concept = new Concept();
 			ConceptMap conceptMap = new ConceptMap();
 			conceptMap.setConcept(concept);
 			conceptMap.setSourceCode(hl7ConceptId);
 			conceptMap.setSource(new ConceptSource());
 			Context.getConceptService().saveConcept(concept);
 			return null;
 		}
 		return concept;
 
 	}
 
 	private Date getDatetime(OBX obx) throws HL7Exception {
 		TS ts = obx.getDateTimeOfTheObservation();
 		return getDatetime(ts);
 	}
 
 	private Date getDatetime(OBR obr) throws HL7Exception {
 		TS ts = obr.getObservationDateTime();
 		return getDatetime(ts);
 
 	}
 
 	private Date getDatetime(TS ts) throws HL7Exception {
 		Date datetime = null;
 		DTM value = ts.getTime();
 
 		if (value.getYear() == 0 || value.getValue() == null)
 			return null;
 
 		try {
 			datetime = getDate(value.getYear(), value.getMonth(),
 					value.getDay(), value.getHour(), value.getMinute(),
 					value.getSecond());
 		} catch (DataTypeException e) {
 
 		}
 		return datetime;
 
 	}
 
 	private Patient getPatient(PID pid) throws HL7Exception {
 		log.info("get ID " + pid.getPatientID().getIDNumber());
 		log.info("get Name " + pid.getPatientName().toString());
 		Patient patient = resolvePatientId(pid);
 		if (patient == null)
 			throw new HL7Exception("Could not resolve patient");
 		return patient;
 	}
 
 	private Patient getPatientORUR01(PID pid) throws HL7Exception {
 		Patient patient;
 		String patId = pid.getPatientIdentifierList(0).getIDNumber().toString();
 		String idType = pid.getPatientIdentifierList(0).getIdentifierTypeCode()
 				.getValue();
 
 		PatientIdentifierType patientIdentifierType = Context
 				.getPatientService().getPatientIdentifierTypeByName(idType);
 		List<PatientIdentifierType> identifierTypeList = new ArrayList<PatientIdentifierType>();
 		identifierTypeList.add(patientIdentifierType);
 
 		List<Patient> patients = Context.getPatientService().getPatients(null,
 				patId, identifierTypeList, false);
 		// I am not checking the identifier type here. Need to come back and add
 		// a check for this
 		if (patients.size() == 1) {
 			patient = patients.get(0);
 		} else {
 			throw new HL7Exception("Could not resolve patient");
 		}
 		return patient;
 	}
 
 	private Message changeStringToMessage(String mess)
 			throws EncodingNotSupportedException, HL7Exception {
 		Parser genericParser = new GenericParser();
 
 		Message message = genericParser.parse(mess);
 		return message;
 	}
 
 	public void processMessage(String mess) throws ApplicationException,
 			EncodingNotSupportedException, HL7Exception {
 
 		processMessage(changeStringToMessage(mess));
 	}
 
 	private Patient resolvePatientId(PID pid) throws HL7Exception {
 		String patId = pid.getPatientID().getIDNumber().getValue();
 		log.info("the Id = " + patId);
 		String idType = pid.getPatientID().getIdentifierTypeCode().getValue();
 		List<PatientIdentifierType> patientIdTypes = Context
 				.getPatientService().getAllPatientIdentifierTypes();
 		if (!patientIdTypes.contains(idType)) {
 			log.error("Type of the Id not in the list for this site " + idType);
 			return null;
 		} else {
 			Patient patient = new Patient();
 			// Populate the PID Segment
 			patient.getPersonName().setFamilyName(
 					pid.getPatientName(0).getFamilyName().getSurname()
 							.getValue());
 			patient.getPersonName().setGivenName(
 					pid.getPatientName(0).getGivenName().getValue());
 			PatientIdentifier patientIdentifier = null;
 			for (CX cx : pid.getPatientIdentifierList()) {
 				patientIdentifier.setIdentifier(cx.getIDNumber().getValue());
 				patientIdentifier.getIdentifierType().setName(
 						cx.getIdentifierTypeCode().getValue());
 				patient.getIdentifiers().add(patientIdentifier);
 
 			}
 			return patient;
 		}
 	}
 
 	private void setParentsNames(Patient patient, ADT_A05 adt)
 			throws HL7Exception {
 		PD1 pd1 = adt.getPD1();
 		pd1.getStudentIndicator().setTable(0);
 		pd1.getHandicap().setTable(0);
 		Set<PersonAttribute> att = new TreeSet<PersonAttribute>();
 		NK1 nk1 = null;
 		for (int i = 0; i < adt.getNK1Reps(); i++) {
 			PersonAttributeType mother = Context.getPersonService()
 					.getPersonAttributeTypeByName(
 							RHEAConstants.MOTHER_NAME_ATTRIBUTE_TYPE);
 			PersonAttributeType father = Context.getPersonService()
 					.getPersonAttributeTypeByName(
 							RHEAConstants.FATHER_NAME_ATTRIBUTE_TYPE);
 			nk1 = adt.getNK1(i);
 			if (nk1.getRelationship().getText().getValue()
 					.equalsIgnoreCase("MTH")) {
 				PersonAttribute mom = new PersonAttribute(mother, nk1
 						.getNKName(0).getFamilyName().getSurname().getValue());
 				att.add(mom);
 			} else if (nk1.getRelationship().getText().getValue()
 					.equalsIgnoreCase("FTH")) {
 
 				PersonAttribute dad = new PersonAttribute(father, nk1
 						.getNKName(0).getFamilyName().getSurname().getValue());
 				att.add(dad);
 			}
 		}
 
 	}
 
 	@Override
 	public String encodingEncounterToMessage(Encounter encounter) {
 		ArrayList<Encounter> encounters = new ArrayList<Encounter>();
 		encounters.add(encounter);
 		String message = "";
 		try {
 			message = getMessage(generateORU_R01Message(encounter.getPatient(),
 					encounters));
 
 			log.info("after parsing " + message);
 		} catch (HL7Exception e) {
 			// TODO Auto-generated catch block
 			log.error("Error generated", e);
 		}
 		return message;
 	}
 
 	// private String getConceptMappingId(Concept concept) {
 	// for (ConceptMap conMap : concept.getConceptMappings()) {
 	// if (conMap.getSource().getName().equalsIgnoreCase("LOINC")) {
 	// return conMap.getSourceCode();
 	// }
 	// }
 	// return null;
 	//
 	// }
 	//
 	// private String getConceptMappingCodingSys(Concept concept) {
 	// for (ConceptMap conMap : concept.getConceptMappings()) {
 	// if (conMap.getSource().getName().equalsIgnoreCase("LOINC")) {
 	// return conMap.getSource().getName();
 	// }
 	// }
 	// return null;
 	//
 	// }
 
 	/**
 	 * @throws HL7Exception
 	 * @throws ApplicationException
 	 * @throws EncodingNotSupportedException
 	 * @see org.openmrs.module.rheapocadapter.service.MessageTransformer#translateMessage(java.lang.String)
 	 */
 	@Override
 	public Message translateMessage(String message) {
 
 		try {
 			return processMessage(changeStringToMessage(message));
 		} catch (EncodingNotSupportedException e) {
 
 			log.error("Error generated" + e.getMessage());
 		} catch (ApplicationException e) {
 
 			log.error("Error generated" + e.getMessage());
 		} catch (HL7Exception e) {
 
 			log.error("Error generated" + e.getMessage());
 		}
 		return null;
 	}
 
 	private PatientIdentifier getPatientIdentifierByIdentifierType(
 			Patient patient, PatientIdentifierType idType) {
 		return ((patient.getPatientIdentifier(idType) != null) && (patient
 				.getPatientIdentifier(idType).getIdentifierType()
 				.equals(idType))) ? (patient.getPatientIdentifier(idType))
 				: null;
 
 	}
 
 	/**
 	 * @throws DataTypeException
 	 * @see org.openmrs.module.rheapocadapter.service.MessageTransformer#generateMessage(org.openmrs.Patient)
 	 */
 	@Override
 	public String generateMessage(Patient patient, String eventType) {
 		try {
 			log.info("Start Creating message");
 			String implementationId = "";
 
 			try {
 				implementationId = (Context.getAdministrationService()
 						.getImplementationId().getImplementationId() != null) ? Context
 						.getAdministrationService().getImplementationId()
 						.getImplementationId()
 						: "rwanda000";
 			} catch (NullPointerException e) {
 				log.error("No Implementation Id  set;");
 				implementationId = "rwanda000";
 			}
 			String fosaid = implementationId.substring(implementationId
 					.indexOf("rwanda") + 6);
 			log.info("fosaid" + fosaid);
 			ADT_A05 adt = new ADT_A05();
 
 			// Populate the MSH Segment
 			MSH mshSegment = adt.getMSH();
 			mshSegment.getFieldSeparator().setValue("|");
 			mshSegment.getEncodingCharacters().setValue("^~\\&");
 			mshSegment
 					.getDateTimeOfMessage()
 					.getTime()
 					.setValue(
 							new SimpleDateFormat("yyyyMMdd").format(new Date()));
 			mshSegment
 					.getSendingApplication()
 					.getNamespaceID()
 					.setValue(fosaid);
 			mshSegment.getSequenceNumber().setValue("123");
 			mshSegment.getMessageType().getMessageCode().setValue("ADT");
 			mshSegment.getMessageType().getMessageStructure().setValue("ADT_A05");
 			if (eventType.equalsIgnoreCase("Update")) {
 				mshSegment.getMessageType().getTriggerEvent().setValue("A31");
 				
 			} else if (eventType.equalsIgnoreCase("Create")) {
 				mshSegment.getMessageType().getTriggerEvent().setValue("A28");
 			}
 
 			mshSegment.getVersionID().getVersionID().setValue("2.5");
 
 			// Populate the PID Segment
 			PID pid = adt.getPID();
 			pid.getPatientName(0).getFamilyName().getSurname()
 					.setValue(patient.getFamilyName());
 			pid.getPatientName(0).getGivenName()
 					.setValue(patient.getGivenName());
 			{
 				PatientIdentifierType nid = Context.getPatientService()
 						.getPatientIdentifierTypeByName("NID");
 				PatientIdentifierType mutuelle = Context.getPatientService()
 						.getPatientIdentifierTypeByName("Mutuelle");
 				PatientIdentifierType rama = Context.getPatientService()
 						.getPatientIdentifierTypeByName("RAMA");
 				PatientIdentifierType primaryCare = Context.getPatientService()
 						.getPatientIdentifierTypeByName("Primary Care ID Type");
 				String id = "";
 				String idType = "";
 				int i = 0;
 				if (getPatientIdentifierByIdentifierType(patient, nid) != null) {
 					log.info("Get NID");
 					id = "";
 					idType = "";
 					idType = RHEAHL7Constants.NID_ID_TYPE;
 					id = getPatientIdentifierByIdentifierType(patient, nid)
 							.getIdentifier();
 					if (id != "" && idType != "") {
 
 						pid.getPatientIdentifierList(i).getIDNumber()
 								.setValue(id);
 						pid.getPatientIdentifierList(i).getIdentifierTypeCode()
 								.setValue(idType);
 						i++;
 					}
 				}
 				if (getPatientIdentifierByIdentifierType(patient, rama) != null) {
 					log.info("Get Rama");
 					id = "";
 					idType = "";
 					idType = RHEAHL7Constants.RAMA_ID_TYPE;
 					id = getPatientIdentifierByIdentifierType(patient, rama)
 							.getIdentifier();
 					if (id != "" && idType != "") {
 
 						pid.getPatientIdentifierList(i).getIDNumber()
 								.setValue(id);
 						pid.getPatientIdentifierList(i).getIdentifierTypeCode()
 								.setValue(idType);
 						i++;
 					}
 				}
 				if (getPatientIdentifierByIdentifierType(patient, mutuelle) != null) {
 					log.info("Get Mutuelle");
 					id = "";
 					idType = "";
 					idType = RHEAHL7Constants.MUTUELLE_ID_TYPE;
 					id = getPatientIdentifierByIdentifierType(patient, mutuelle)
 							.getIdentifier();
 					if (id != "" && idType != "") {
 
 						pid.getPatientIdentifierList(i).getIDNumber()
 								.setValue(id);
 						pid.getPatientIdentifierList(i).getIdentifierTypeCode()
 								.setValue(idType);
 						i++;
 					}
 				}
 				if (getPatientIdentifierByIdentifierType(patient, primaryCare) != null) {
 					log.info("Get OMRS");
 					id = "";
 					idType = "";
 					implementationId = implementationId.toLowerCase();
 					fosaid = implementationId.substring(implementationId
 							.indexOf("rwanda") + 6);
 					idType = RHEAHL7Constants.OMRS_ID_TYPE_PREFIX + fosaid;
 					id = getPatientIdentifierByIdentifierType(patient,
 							primaryCare).getIdentifier();
 					log.info("idType " + idType);
 					if (id != "" && idType != "") {
 
 						pid.getPatientIdentifierList(i).getIDNumber()
 								.setValue(id);
 						pid.getPatientIdentifierList(i).getIdentifierTypeCode()
 								.setValue(idType);
 						i++;
 					}
 				}
 				if (!patient.getPatientIdentifier().getIdentifierType()
 						.equals(nid)
 						&& !patient.getPatientIdentifier().getIdentifierType()
 								.equals(rama)
 						&& !patient.getPatientIdentifier().getIdentifierType()
 								.equals(mutuelle)
 						&& !patient.getPatientIdentifier().getIdentifierType()
 								.equals(primaryCare)) {
 					id = "";
 					idType = "";
 					log.info("Get "
 							+ patient.getPatientIdentifier().getIdentifier());
 					idType = patient.getPatientIdentifier().getIdentifierType()
 							.getName();
 					id = patient.getPatientIdentifier().getIdentifier();
 					if (id != "" && idType != "") {
 
 						pid.getPatientIdentifierList(i).getIDNumber()
 								.setValue(id);
 						pid.getPatientIdentifierList(i).getIdentifierTypeCode()
 								.setValue(idType);
 						i++;
 					}
 
 				}
 
 			}
 			SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
 			Date dob = patient.getBirthdate();
 			Date dod = patient.getDeathDate();
 			String dobStr = "";
 			String dodStr = "";
 			if (dob != null)
 				dobStr = df.format(dob);
 			if (dod != null)
 				dodStr = df.format(dod);
 
 			// Address
			if(patient.getPersonAddress() != null) {
 			XAD add = pid.getPatientAddress(0);
 			add.getCountry().setValue(patient.getPersonAddress().getCountry());
 			add.getStateOrProvince().setValue(
 					patient.getPersonAddress().getStateProvince());
 			add.getCity().setValue(
 					patient.getPersonAddress().getCountyDistrict());
 			add.getCensusTract().setValue(
 					patient.getPersonAddress().getAddress1());
 			add.getCountyParishCode().setValue(
 					patient.getPersonAddress().getCityVillage());
 			add.getOtherGeographicDesignation().setValue(
 					patient.getPersonAddress().getNeighborhoodCell());
			}
 
 			// gender
 			pid.getAdministrativeSex().setValue(patient.getGender());
 
 			// dob
 			pid.getDateTimeOfBirth().getTime().setValue(dobStr);
 
 			// Death
 			// pid.getPatientDeathIndicator().setValue(
 			// patient.getDead().toString());
 			// pid.getPatientDeathDateAndTime().getTime().setValue(dodStr);
 
 			PD1 pd1 = adt.getPD1();
 			pd1.getStudentIndicator().setTable(0);
 			pd1.getHandicap().setTable(0);
 			
 			// set mother and father name
 			int n = 0;
 			NK1 nk1 = adt.getNK1(n);
 			
 			PersonAttributeType mother = Context.getPersonService().getPersonAttributeTypeByName(RHEAConstants.MOTHER_NAME_ATTRIBUTE_TYPE);
 			PersonAttribute mom = patient.getAttribute(mother);
 			if (mom != null) {
 				n++;
 				String mom_str = mom.getValue();
 				nk1.getSetIDNK1().setValue("1");
 				nk1.getRelationship().getIdentifier().setValue("MTH");
 				nk1.getRelationship().getText().setValue("mother");
 				nk1.getRelationship().getNameOfCodingSystem().setValue("REL_RTS");
 				nk1.getNKName(0).getFamilyName().getSurname().setValue(mom_str);
 			}
 			
 			nk1 = adt.getNK1(n);
 			
 			PersonAttributeType father = Context.getPersonService().getPersonAttributeTypeByName(RHEAConstants.FATHER_NAME_ATTRIBUTE_TYPE);
 			PersonAttribute dad = patient.getAttribute(father);
 			if (dad != null) {
 				n++;
 				String dad_str = dad.getValue();
 				nk1.getSetIDNK1().setValue("2");
 				nk1.getRelationship().getIdentifier().setValue("FTH");
 				nk1.getRelationship().getText().setValue("father");
 				nk1.getRelationship().getNameOfCodingSystem().setValue("REL_RTS");
 				nk1.getNKName(0).getFamilyName().getSurname().setValue(dad_str);
 			}
 			
 			PV1 pv1 = adt.getPV1();
 			pv1.getSetIDPV1().setValue("1");
 			pv1.getPatientClass().setValue("U");
 
 			return new GenericParser().encode(adt, "XML");
 		} catch (DataTypeException e) {
 			log.error("Error generated" + e.getMessage());
 			return "";
 		} catch (HL7Exception e) {
 			log.error("Error generated" + e.getMessage());
 			return "";
 		}
 
 	}
 
 	private PV1 getPV1(ORU_R01 oru) {
 		return oru.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1();
 	}
 
 	private ORC getORC(ORU_R01 oru) {
 		return oru.getPATIENT_RESULT().getORDER_OBSERVATION().getORC();
 	}
 
 	private boolean isConceptProposal(String identifier) {
 		return OpenmrsUtil.nullSafeEquals(identifier,
 				OpenmrsConstants.PROPOSED_CONCEPT_IDENTIFIER);
 	}
 
 	private Concept getConcept(CE codedElement, String uid) throws HL7Exception {
 		String hl7ConceptId = codedElement.getIdentifier().getValue();
 		String codingSystem = codedElement.getNameOfCodingSystem().getValue()
 				.toString();
 		Concept concept = Context.getConceptService().getConceptByMapping(
 				hl7ConceptId, codingSystem);
 
 		return concept;
 	}
 
 	private Concept getConcept(CWE codedElement, String uid)
 			throws HL7Exception {
 		String hl7ConceptId = codedElement.getIdentifier().getValue();
 		String codingSystem = codedElement.getNameOfCodingSystem().getValue();
 		return getConcept(hl7ConceptId, codingSystem, uid);
 	}
 
 	protected Concept getConcept(String hl7ConceptId, String codingSystem,
 			String uid) throws HL7Exception {
 		if (codingSystem == null || "99DCT".equals(codingSystem)) {
 			// the concept is local
 			try {
 				Integer conceptId = new Integer(hl7ConceptId);
 				Concept concept = new Concept(conceptId);
 				return concept;
 			} catch (NumberFormatException e) {
 				throw new HL7Exception("Invalid concept ID '" + hl7ConceptId
 						+ "' in hl7 message with uid: " + uid);
 			}
 		} else {
 			// the concept is not local, look it up in our mapping
 			Concept concept = Context.getConceptService().getConceptByMapping(
 					hl7ConceptId, codingSystem);
 			if (concept == null) {
 				log.error("Unable to find concept with code: " + hl7ConceptId
 						+ " and mapping: " + codingSystem
 						+ " in hl7 message with uid: " + uid);
 			}
 			return concept;
 		}
 	}
 
 	private Obs parseObs(Encounter encounter, OBX obx, OBR obr, String uid)
 			throws HL7Exception, ProposingConceptException {
 		if (log.isDebugEnabled())
 			log.debug("parsing observation: " + obx);
 		Varies[] values = obx.getObservationValue();
 
 		// bail out if no values were found
 		if (values == null || values.length < 1)
 			return null;
 
 		String hl7Datatype = values[0].getName();
 		if (log.isDebugEnabled())
 			log.debug("  datatype = " + hl7Datatype);
 		Concept concept = getConcept(obx.getObservationIdentifier(), uid);
 		if (log.isDebugEnabled())
 			log.debug("  concept = " + concept.getConceptId());
 		ConceptName conceptName = getConceptName(obx.getObservationIdentifier());
 		if (log.isDebugEnabled())
 			log.debug("  concept-name = " + conceptName);
 
 		Date datetime = getDatetime(obx);
 		if (log.isDebugEnabled())
 			log.debug("  timestamp = " + datetime);
 		if (datetime == null)
 			datetime = encounter.getEncounterDatetime();
 
 		Obs obs = new Obs();
 		obs.setUuid(UUID.randomUUID().toString());
 		obs.setPerson(encounter.getPatient());
 		obs.setConcept(concept);
 		obs.setEncounter(encounter);
 		obs.setObsDatetime(datetime);
 		obs.setLocation(encounter.getLocation());
 		obs.setCreator(encounter.getCreator());
 		obs.setDateCreated(new Date());
 
 		// set comments if there are any
 		StringBuilder comments = new StringBuilder();
 		ORU_R01_OBSERVATION parent = (ORU_R01_OBSERVATION) obx.getParent();
 		// iterate over all OBX NTEs
 		for (int i = 0; i < parent.getNTEReps(); i++)
 			for (FT obxComment : parent.getNTE(i).getComment()) {
 				if (comments.length() > 0)
 					comments.append(" ");
 				comments = comments.append(obxComment.getValue());
 			}
 		// only set comments if there are any
 		if (StringUtils.hasText(comments.toString()))
 			obs.setComment(comments.toString());
 
 		Type obx5 = values[0].getData();
 		if ("NM".equals(hl7Datatype)) {
 			String value = ((NM) obx5).getValue();
 			if (value == null || value.length() == 0) {
 				log.warn("Not creating null valued obs for concept " + concept);
 				return null;
 			} else if (value.equals("0") || value.equals("1")) {
 				concept = concept.hydrate(concept.getConceptId().toString());
 				obs.setConcept(concept);
 
 				if (concept.getDatatype().isNumeric())
 					try {
 						obs.setValueNumeric(Double.valueOf(value));
 					} catch (NumberFormatException e) {
 						throw new HL7Exception(
 								"numeric (NM) value '" + value
 										+ "' is not numeric for concept #"
 										+ concept.getConceptId() + " ("
 										+ conceptName.getName()
 										+ ") in message " + uid, e);
 					}
 				else {
 					// throw this exception to make sure that the handler
 					// doesn't silently ignore bad hl7 message
 					throw new HL7Exception(
 							"Can't set boolean concept answer for concept with id "
 									+ obs.getConcept().getConceptId());
 				}
 			} else {
 				try {
 					obs.setValueNumeric(Double.valueOf(value));
 				} catch (NumberFormatException e) {
 					throw new HL7Exception("numeric (NM) value '" + value
 							+ "' is not numeric for concept #"
 							+ concept.getConceptId() + " ("
 							+ conceptName.getName() + ") in message " + uid, e);
 				}
 			}
 		} else if ("CWE".equals(hl7Datatype)) {
 			log.debug("  CWE observation");
 			CWE value = (CWE) obx5;
 			String valueIdentifier = value.getIdentifier().getValue();
 			log.debug("    value id = " + valueIdentifier);
 			String valueName = value.getText().getValue();
 			log.debug("    value name = " + valueName);
 			if (isConceptProposal(valueIdentifier)) {
 				if (log.isDebugEnabled())
 					log.debug("Proposing concept");
 				throw new ProposingConceptException(concept, valueName);
 			} else {
 				log.debug("    not proposal");
 				try {
 					Concept valueConcept = getConcept(value, uid);
 					obs.setValueCoded(valueConcept);
 					if ("99RX".equals(value.getNameOfAlternateCodingSystem()
 							.getValue())) {
 						Drug valueDrug = new Drug();
 						valueDrug.setDrugId(new Integer(value
 								.getAlternateIdentifier().getValue()));
 						obs.setValueDrug(valueDrug);
 					} else {
 						ConceptName valueConceptName = getConceptName(value);
 						if (valueConceptName != null) {
 							if (log.isDebugEnabled()) {
 								log.debug("    value concept-name-id = "
 										+ valueConceptName.getConceptNameId());
 								log.debug("    value concept-name = "
 										+ valueConceptName.getName());
 							}
 							obs.setValueCodedName(valueConceptName);
 						}
 					}
 				} catch (NumberFormatException e) {
 					throw new HL7Exception("Invalid concept ID '"
 							+ valueIdentifier + "' for OBX-5 value '"
 							+ valueName + "'");
 				}
 			}
 			if (log.isDebugEnabled())
 				log.debug("  Done with CWE");
 		} else if ("CE".equals(hl7Datatype)) {
 			CE value = (CE) obx5;
 			String valueIdentifier = value.getIdentifier().getValue();
 			String valueName = value.getText().getValue();
 			if (isConceptProposal(valueIdentifier)) {
 				throw new ProposingConceptException(concept, valueName);
 			} else {
 				try {
 					Concept c = getConcept(value, uid);
 					obs.setValueCoded(c);
 					ConceptName name = c.getName();
 					obs.setValueCodedName(name);
 				} catch (NumberFormatException e) {
 					throw new HL7Exception("Invalid concept ID '"
 							+ valueIdentifier + "' for OBX-5 value '"
 							+ valueName + "'");
 				}
 			}
 		} else if ("DT".equals(hl7Datatype)) {
 			DT value = (DT) obx5;
 			Date valueDate = getDate(value.getYear(), value.getMonth(),
 					value.getDay(), 0, 0, 0);
 			if (value == null || valueDate == null) {
 				log.warn("Not creating null valued obs for concept " + concept);
 				return null;
 			}
 			obs.setValueDatetime(valueDate);
 		} else if ("TS".equals(hl7Datatype)) {
 			DTM value = ((TS) obx5).getTime();
 			Date valueDate = getDate(value.getYear(), value.getMonth(),
 					value.getDay(), value.getHour(), value.getMinute(),
 					value.getSecond());
 			if (value == null || valueDate == null) {
 				log.warn("Not creating null valued obs for concept " + concept);
 				return null;
 			}
 			obs.setValueDatetime(valueDate);
 		} else if ("TM".equals(hl7Datatype)) {
 			TM value = (TM) obx5;
 			Date valueTime = getDate(0, 0, 0, value.getHour(),
 					value.getMinute(), value.getSecond());
 			if (value == null || valueTime == null) {
 				log.warn("Not creating null valued obs for concept " + concept);
 				return null;
 			}
 			obs.setValueDatetime(valueTime);
 		} else if ("ST".equals(hl7Datatype)) {
 			ST value = (ST) obx5;
 			if (value == null || value.getValue() == null
 					|| value.getValue().trim().length() == 0) {
 				log.warn("Not creating null valued obs for concept " + concept);
 				return null;
 			}
 			obs.setValueText(value.getValue());
 		} else {
 			// unsupported data type
 			// TODO: support RP (report), SN (structured numeric)
 			// do we need to support BIT just in case it slips thru?
 			throw new HL7Exception("Unsupported observation datatype '"
 					+ hl7Datatype + "'");
 		}
 
 		return obs;
 	}
 
 	public List<Encounter> messageToEncounter(ORU_R01 message)
 			throws HL7Exception {
 		// List<Encounter> encList = new ArrayList<Encounter>();
 		// log.info("Starting creating enc from message");
 		// Encounter enc = null;
 		//
 		// ORU_R01_PATIENT_RESULT result = message.getPATIENT_RESULT();
 		// int orderNo = result.getORDER_OBSERVATIONReps();
 		//
 		// Encounter previousEnc = null;
 		// boolean preFlag = false;
 		// log.info("Before loop");
 		// for (int i = 0; i < orderNo; i++) {
 		// log.info("inside loop " + i);
 		// ORU_R01_ORDER_OBSERVATION order = result.getORDER_OBSERVATION(i);
 		// log.info("order length " + order.getOBSERVATIONReps());
 		// String id = order.getOBR().getFillerOrderNumber()
 		// .getEntityIdentifier().getValue();
 		// log.info("before if");
 		// if (id != null) {
 		// log.info("id!=null");
 		// enc = parseEncounter(i, result, message, previousEnc);
 		// log.info("after getting enc");
 		// encList.add(enc);
 		// previousEnc = enc;
 		// preFlag = true;
 		// } else if (preFlag == false) {
 		// log.info("id==null and preflag==false");
 		// if (previousEnc != null) {
 		// log.info("id==null and preflag==false and previousEnc!=null");
 		// enc = parseEncounter(i, result, message, previousEnc);
 		// previousEnc = enc;
 		// }
 		// } else {
 		// log.info("id==null and preflag==true");
 		// preFlag = false;
 		// }
 		// }
 		// log.info(encList.size() + " Size of the list");
 		// return encList;
 		// return parseEncounter(result, message, null);
 		log.info(">>>>Creating encs");
 		List<Encounter> encs = null;
 		try {
 			RHEA_ORU_R01Handler rHEA_ORU_R01Handler = new RHEA_ORU_R01Handler(
 					null);
 			// String mess =
 			//
 			// "<?xml version=\"1.0\"?> <ORU_R01 xmlns=\"urn:hl7-org:v2xml\"> <MSH> <MSH.1>|</MSH.1> <MSH.2>^~\\&amp;</MSH.2> <MSH.6> <HD.1>Point of Care</HD.1> </MSH.6> <MSH.7> <TS.1>20121005084254</TS.1> </MSH.7> <MSH.9> <MSG.1>ORU</MSG.1> <MSG.2>R01</MSG.2> <MSG.3>ORU_R01</MSG.3> </MSH.9> <MSH.10>a70d7108-5a0c-4818-8360-f73a22f7fd65</MSH.10> <MSH.11> <PT.1>D</PT.1> <PT.2>C</PT.2> </MSH.11> <MSH.12> <VID.1>2.5</VID.1> <VID.2> <CE.1>RWA</CE.1> </VID.2> </MSH.12> <MSH.21> <EI.1>CLSM_V0.83</EI.1> </MSH.21> </MSH> <ORU_R01.PATIENT_RESULT> <ORU_R01.PATIENT> <PID> <PID.3> <CX.1>1199170003455088</CX.1> <CX.5>NID</CX.5> </PID.3> <PID.5> <XPN.1> <FN.1>ALICE</FN.1> </XPN.1> <XPN.2>MUKAMURIGO</XPN.2> </PID.5> <PID.7> <TS.1>19910430</TS.1> </PID.7> </PID> </ORU_R01.PATIENT> <ORU_R01.ORDER_OBSERVATION> <ORC> <ORC.1>RE</ORC.1> <ORC.9> <TS.1>201210050842</TS.1> </ORC.9> <ORC.12> <XCN.1>38</XCN.1> </ORC.12> <ORC.16> <CE.1>Identifier</CE.1> <CE.2>Text</CE.2> <CE.3>Name of Coding System</CE.3> </ORC.16> </ORC> <OBR> <OBR.1>0</OBR.1> <OBR.3> <EI.1>5</EI.1> </OBR.3> <OBR.4> <CE.2>ANC Physical</CE.2> </OBR.4> <OBR.7> <TS.1>201210010000</TS.1> </OBR.7> <OBR.16> <XCN.1>1198080018198077 </XCN.1> <XCN.2> <FN.1>MUSABWA</FN.1> </XCN.2> <XCN.3>JACQUES</XCN.3> <XCN.13>NID</XCN.13> </OBR.16> <OBR.20>363</OBR.20> <OBR.21>OMRS-Ruhunda</OBR.21> </OBR> </ORU_R01.ORDER_OBSERVATION> <ORU_R01.ORDER_OBSERVATION> <OBR> <OBR.1>1</OBR.1> <OBR.18>0</OBR.18> <OBR.29> <EIP.2> <EI.3>5</EI.3> </EIP.2> </OBR.29> </OBR> <ORU_R01.OBSERVATION> <OBX> <OBX.1>0</OBX.1> <OBX.2>NM</OBX.2> <OBX.3> <CE.1>84862-4</CE.1> <CE.2>DIASTOLIC BLOOD PRESSURE</CE.2> <CE.3>LOINC</CE.3> </OBX.3> <OBX.5>68.0</OBX.5> <OBX.6> <CE.1>mmHg</CE.1> <CE.3>ucum</CE.3> </OBX.6> <OBX.14> <TS.1>20121001161045</TS.1> </OBX.14> </OBX> </ORU_R01.OBSERVATION> <ORU_R01.OBSERVATION> <OBX> <OBX.1>1</OBX.1> <OBX.2>NM</OBX.2> <OBX.3> <CE.1>29463-7</CE.1> <CE.2>WEIGHT (KG)</CE.2> <CE.3>LOINC</CE.3> </OBX.3> <OBX.5>50.0</OBX.5> <OBX.6> <CE.1>kg</CE.1> <CE.3>ucum</CE.3> </OBX.6> <OBX.14> <TS.1>20121001161047</TS.1> </OBX.14> </OBX> </ORU_R01.OBSERVATION> <ORU_R01.OBSERVATION> <OBX> <OBX.1>2</OBX.1> <OBX.2>NM</OBX.2> <OBX.3> <CE.1>11885-1</CE.1> <CE.2>NUMBER OF WEEKS PREGNANT</CE.2> <CE.3>LOINC</CE.3> </OBX.3> <OBX.5>10.0</OBX.5> <OBX.6> <CE.1>weeks</CE.1> <CE.3>ucum</CE.3> </OBX.6> <OBX.14> <TS.1>20121001161046</TS.1> </OBX.14> </OBX> </ORU_R01.OBSERVATION> <ORU_R01.OBSERVATION> <OBX> <OBX.1>3</OBX.1> <OBX.2>NM</OBX.2> <OBX.3> <CE.1>8310-5</CE.1> <CE.2>TEMPERATURE (C)</CE.2> <CE.3>LOINC</CE.3> </OBX.3> <OBX.5>37.0</OBX.5> <OBX.6> <CE.1>DEG C</CE.1> <CE.3>ucum</CE.3> </OBX.6> <OBX.14> <TS.1>20121001161046</TS.1> </OBX.14> </OBX> </ORU_R01.OBSERVATION> <ORU_R01.OBSERVATION> <OBX> <OBX.1>4</OBX.1> <OBX.2>NM</OBX.2> <OBX.3> <CE.1>11881-0</CE.1> <CE.2>Length of the uterus (fundal height) in cm</CE.2> <CE.3>LOINC</CE.3> </OBX.3> <OBX.5>8.0</OBX.5> <OBX.6> <CE.1>cm</CE.1> <CE.3>ucum</CE.3> </OBX.6> <OBX.14> <TS.1>20121001161046</TS.1> </OBX.14> </OBX> </ORU_R01.OBSERVATION> <ORU_R01.OBSERVATION> <OBX> <OBX.1>5</OBX.1> <OBX.2>NM</OBX.2> <OBX.3> <CE.1>55283-6</CE.1> <CE.2>Heart rate of fetus</CE.2> <CE.3>LOINC</CE.3> </OBX.3> <OBX.5>120.0</OBX.5> <OBX.6> <CE.1>BPM</CE.1> <CE.3>ucum</CE.3> </OBX.6> <OBX.14> <TS.1>20121001161048</TS.1> </OBX.14> </OBX> </ORU_R01.OBSERVATION> <ORU_R01.OBSERVATION> <OBX> <OBX.1>6</OBX.1> <OBX.3> <CE.1>46040-2</CE.1> <CE.2>WEIGHT CHANGE</CE.2> <CE.3>LOINC</CE.3> </OBX.3> </OBX> </ORU_R01.OBSERVATION> <ORU_R01.OBSERVATION> <OBX> <OBX.1>7</OBX.1> <OBX.2>NM</OBX.2> <OBX.3> <CE.1>8480-6</CE.1> <CE.2>SYSTOLIC BLOOD PRESSURE</CE.2> <CE.3>LOINC</CE.3> </OBX.3> <OBX.5>124.0</OBX.5> <OBX.6> <CE.1>mmHg</CE.1> <CE.3>ucum</CE.3> </OBX.6> <OBX.14> <TS.1>20121001161047</TS.1> </OBX.14> </OBX> </ORU_R01.OBSERVATION> </ORU_R01.ORDER_OBSERVATION> <ORU_R01.ORDER_OBSERVATION> <OBR> <OBR.1>2</OBR.1> <OBR.3> <EI.1>18</EI.1> </OBR.3> <OBR.4> <CE.2>ANC Maternal Treatments and Interventions</CE.2> </OBR.4> <OBR.7> <TS.1>201210010000</TS.1> </OBR.7> <OBR.16> <XCN.1>b6d79622-9cfe-1031-84cf-bd846ceebe61</XCN.1> <XCN.2> <FN.1>MUSABWA</FN.1> </XCN.2> <XCN.3>JACQUES</XCN.3> <XCN.13>EPID</XCN.13> </OBR.16> <OBR.20>363</OBR.20> <OBR.21>OMRS-Ruhunda</OBR.21> </OBR> </ORU_R01.ORDER_OBSERVATION> <ORU_R01.ORDER_OBSERVATION> <OBR> <OBR.1>3</OBR.1> <OBR.18>2</OBR.18> <OBR.29> <EIP.2> <EI.3>18</EI.3> </EIP.2> </OBR.29> </OBR> <ORU_R01.OBSERVATION> <OBX> <OBX.1>0</OBX.1> <OBX.2>CE</OBX.2> <OBX.3> <CE.1>72180-3</CE.1> <CE.2>Was the woman given iron and folic acid?</CE.2> <CE.3>LOINC</CE.3> </OBX.3> <OBX.5> <CE.1>1066</CE.1> <CE.2>NO</CE.2> <CE.3>RWCS</CE.3> </OBX.5> <OBX.14> <TS.1>20121001163939</TS.1> </OBX.14> </OBX> </ORU_R01.OBSERVATION> <ORU_R01.OBSERVATION> <OBX> <OBX.1>1</OBX.1> <OBX.2>CE</OBX.2> <OBX.3> <CE.1>72178-7</CE.1> <CE.2>Given Mosquito Nets</CE.2> <CE.3>LOINC</CE.3> </OBX.3> <OBX.5> <CE.1>1065</CE.1> <CE.2>YES</CE.2> <CE.3>RWCS</CE.3> </OBX.5> <OBX.14> <TS.1>20121001163940</TS.1> </OBX.14> </OBX> </ORU_R01.OBSERVATION> <ORU_R01.OBSERVATION> <OBX> <OBX.1>2</OBX.1> <OBX.2>CE</OBX.2> <OBX.3> <CE.1>72179-5</CE.1> <CE.2>Given Sulfadoxin Pyrimethamine</CE.2> <CE.3>LOINC</CE.3> </OBX.3> <OBX.5> <CE.1>1066</CE.1> <CE.2>NO</CE.2> <CE.3>RWCS</CE.3> </OBX.5> <OBX.14> <TS.1>20121001163940</TS.1> </OBX.14> </OBX> </ORU_R01.OBSERVATION> <ORU_R01.OBSERVATION> <OBX> <OBX.1>3</OBX.1> <OBX.2>CE</OBX.2> <OBX.3> <CE.1>72187-8</CE.1> <CE.2>given tetanus vaccine</CE.2> <CE.3>LOINC</CE.3> </OBX.3> <OBX.5> <CE.1>1065</CE.1> <CE.2>YES</CE.2> <CE.3>RWCS</CE.3> </OBX.5> <OBX.14> <TS.1>20121001163938</TS.1> </OBX.14> </OBX> </ORU_R01.OBSERVATION> <ORU_R01.OBSERVATION> <OBX> <OBX.1>4</OBX.1> <OBX.2>CE</OBX.2> <OBX.3> <CE.1>8406</CE.1> <CE.2>Given Mebendazole</CE.2> <CE.3>RWCS</CE.3> </OBX.3> <OBX.5> <CE.1>1066</CE.1> <CE.2>NO</CE.2> <CE.3>RWCS</CE.3> </OBX.5> <OBX.14> <TS.1>20121001163938</TS.1> </OBX.14> </OBX> </ORU_R01.OBSERVATION> </ORU_R01.ORDER_OBSERVATION> </ORU_R01.PATIENT_RESULT> </ORU_R01>";
 			// // String ms = EncodedMessageComparator.standardize(mess);
 			// Message message =
 			// rHEA_ORU_R01Handler.changeStringToMessage(mess);
 
 			encs = rHEA_ORU_R01Handler
 					.processORU_R01((ORU_R01) rHEA_ORU_R01Handler
 							.processMessage(message));
 			log.info("<<<<<<Encs size >>>>> " + encs.size());
 		} catch (HL7Exception e) {
 
 			log.error(e.getMessage());
 		} catch (ApplicationException e) {
 
 			log.error(e.getMessage());
 		}
 		return encs;
 
 	}
 
 	public synchronized Encounter parseEncounter(int num,
 			ORU_R01_PATIENT_RESULT result, ORU_R01 message,
 			Encounter previousEnc) throws HL7Exception {
 		MSH msh = getMSH(message);
 		PID pid = getPID(message);
 		ORC orc = getORC(message);
 		// List<Encounter> encounters = new ArrayList<Encounter>();
 		log.info(num + " Encounter");
 		ORU_R01_ORDER_OBSERVATION order2 = result.getORDER_OBSERVATION(num);
 
 		ORU_R01_ORDER_OBSERVATION order = null;
 		OBR childObr = null;
 		OBR encOBR = null;
 
 		if (previousEnc == null) {
 			encOBR = order2.getOBR();
 			// the child obr
 			order = result.getORDER_OBSERVATION(num);
 			childObr = order.getOBR();
 		} else {
 			order = result.getORDER_OBSERVATION(num);
 			childObr = order.getOBR();
 		}
 
 		String messageControlId = msh.getMessageControlID().getValue();
 
 		// create the encounter
 		// Patient is retrieved by enterpriseId only
 		Patient patient = getPatientORUR01(pid);
 
 		if (previousEnc == null) {
 			previousEnc = createEncounter(msh, patient, orc, childObr, encOBR);
 		}
 
 		if (childObr.getParentNumber().getFillerAssignedIdentifier()
 				.getUniversalID().getValue() != null) {
 			// boolean flag = false;
 			// create observations
 			ORU_R01_PATIENT_RESULT patientResult = message.getPATIENT_RESULT();
 			log.info(message.getPATIENT_RESULTReps() + "Number of results");
 			int numObr = patientResult.getORDER_OBSERVATIONReps();
 			ORU_R01_ORDER_OBSERVATION orderObs = null;
 			for (int i = 1; i < numObr; i++) {
 				log.info(">>>>>>>>>>>Processing OBR (" + i + " of " + numObr
 						+ ")");
 				orderObs = patientResult.getORDER_OBSERVATION(i);
 
 				// the parent obr
 				OBR obr = orderObs.getOBR();
 				if (obr.getPlacerField1() != null) {
 
 					// if we're not ignoring this obs group, create an
 					// Obs grouper object that the underlying obs objects
 					// will
 					// use
 					Obs obsGrouper = null;
 					if (childObr.getUniversalServiceIdentifier()
 							.getIdentifier().getValue() != null) {
 						Concept obrConcept = getConcept(
 								childObr.getUniversalServiceIdentifier(),
 								messageControlId);
 
 						// create an obs for this obs group too
 						obsGrouper = new Obs();
 						obsGrouper.setConcept(obrConcept);
 						obsGrouper.setPerson(previousEnc.getPatient());
 						obsGrouper.setEncounter(previousEnc);
 						Date datetime = getDatetime(obr);
 						if (datetime == null)
 							datetime = previousEnc.getEncounterDatetime();
 						obsGrouper.setObsDatetime(datetime);
 						obsGrouper.setLocation(previousEnc.getLocation());
 						obsGrouper.setCreator(previousEnc.getCreator());
 						obsGrouper.setDateCreated(new Date());
 						obsGrouper.setUuid(UUID.randomUUID().toString());
 
 						// add this obs as another row in the obs table
 						previousEnc.addObs(obsGrouper);
 					}
 
 					// loop over the obs and create each object, adding it
 					// to
 					// the encounter
 					int numObs = orderObs.getOBSERVATIONReps();
 					HL7Exception errorInHL7Queue = null;
 					for (int j = 0; j < numObs; j++) {
 						if (log.isDebugEnabled())
 							log.debug("Processing OBS (" + j + " of " + numObs
 									+ ")");
 
 						OBX obx = orderObs.getOBSERVATION(j).getOBX();
 						try {
 							log.debug("Parsing observation");
 							Obs obs = parseObs(previousEnc, obx, obr,
 									messageControlId);
 							if (obs != null) {
 
 								// if we're backfilling an encounter, don't
 								// use
 								// the creator/dateCreated from the
 								// encounter
 								if (previousEnc.getEncounterId() != null) {
 									obs.setCreator(Context
 											.getAuthenticatedUser());
 									obs.setDateCreated(new Date());
 								}
 
 								// set the obsGroup on this obs
 								if (obsGrouper != null) {
 									// set the obs to the group. This
 									// assumes
 									// the group is already
 									// on the encounter and that when the
 									// encounter is saved it will
 									// propagate to the children obs
 									obsGrouper.addGroupMember(obs);
 									previousEnc.addObs(obs);
 
 								} else {
 									// set this obs on the encounter object
 									// that
 									// we
 									// will be saving later
 									log.debug("Obs is not null. Adding to encounter object");
 									previousEnc.addObs(obs);
 								}
 
 							} else {
 							}
 
 						} catch (ProposingConceptException proposingException) {
 
 						} catch (HL7Exception e) {
 							errorInHL7Queue = e;
 						}
 
 					}
 
 				}
 
 			}
 		}
 		// encounters.add(previousEnc);
 
 		return previousEnc;
 
 	}
 
 	private Date getEncounterDate(OBR obr) throws HL7Exception {
 		return tsToDate(obr.getObservationDateTime());
 	}
 
 	private Date tsToDate(TS ts) throws HL7Exception {
 		// need to handle timezone
 		String dtm = ts.getTime().getValue();
 		int year = Integer.parseInt(dtm.substring(0, 4));
 		int month = (dtm.length() >= 6 ? Integer.parseInt(dtm.substring(4, 6)) - 1
 				: 0);
 		int day = (dtm.length() >= 8 ? Integer.parseInt(dtm.substring(6, 8))
 				: 1);
 		int hour = (dtm.length() >= 10 ? Integer.parseInt(dtm.substring(8, 10))
 				: 0);
 		int min = (dtm.length() >= 12 ? Integer.parseInt(dtm.substring(10, 12))
 				: 0);
 		int sec = (dtm.length() >= 14 ? Integer.parseInt(dtm.substring(12, 14))
 				: 0);
 		Calendar cal = Calendar.getInstance();
 		cal.set(year, month, day, hour, min, sec);
 		/*
 		 * if (cal.getTimeZone().getRawOffset() != timeZoneOffsetMillis) {
 		 * TimeZone tz = (TimeZone)TimeZone.getDefault().clone();
 		 * tz.setRawOffset(timeZoneOffsetMillis); cal.setTimeZone(tz); }
 		 */
 		return cal.getTime();
 	}
 
 	private Person getProviderORUR01(OBR encObr) throws HL7Exception {
 		XCN hl7Provider = encObr.getOrderingProvider(0);
 
 		Person p = service.getPersonByNID(hl7Provider.getIDNumber().getValue());
 		// Integer providerId = service.getPersonByNID(NID);
 
 		if (p == null) {
 			log.info("ID extracted from the HL7 message does not match with PoC records, a basic patient will be created...");
 			Person providerCandidate = new Person();
 			providerCandidate.setGender("N/A");
 			PersonName name = new PersonName();
 
 			if (hl7Provider.getGivenName().getValue() != null) {
 				name.setGivenName(hl7Provider.getGivenName().getValue());
 			} else {
 				name.setGivenName("BLANK");
 			}
 
 			if (hl7Provider.getFamilyName().getSurname().getValue() != null) {
 				name.setFamilyName(hl7Provider.getFamilyName().getSurname()
 						.getValue());
 			} else {
 				name.setFamilyName("BLANK");
 			}
 
 			SortedSet<PersonName> names = new TreeSet<PersonName>();
 
 			names.add(name);
 			providerCandidate.setNames(names);
 
 			PersonAttributeType NIDAttributeType = Context.getPersonService()
 					.getPersonAttributeTypeByName("NID");
 
 			if (NIDAttributeType == null) {
 				log.info("Creating a PersonAttributeType for NID since it does not exsist");
 				NIDAttributeType = new PersonAttributeType();
 				NIDAttributeType.setName("NID");
 				NIDAttributeType
 						.setDescription("Stores the NID of the Person object");
 				Context.getPersonService().savePersonAttributeType(
 						NIDAttributeType);
 			}
 
 			PersonAttribute NIDAtrribute = new PersonAttribute();
 			NIDAtrribute.setAttributeType(NIDAttributeType);
 			NIDAtrribute.setValue(hl7Provider.getIDNumber().getValue());
 
 			PersonAttributeType roleAttributeType = Context.getPersonService()
 					.getPersonAttributeType("Role");
 
 			// We need to mark this patient as a potential provider.
 			// The ideal way to do this would be to create a user object and set
 			// its Role to Provider.
 			// However, this would result in the creation and storage of an
 			// additional object in the database.
 			// Furthermore, we would be forced to define an username and
 			// password for each of the new users.
 			// Therefore, I am merely creating a person attribute of type Role,
 			// and setting it to "Provider" (for now).
 
 			if (roleAttributeType == null) {
 				log.info("Creating a PersonAttributeType for Role since it does not exsist");
 				roleAttributeType = new PersonAttributeType();
 				roleAttributeType.setName("Role");
 				roleAttributeType
 						.setDescription("Stores the Role of the Person object");
 				Context.getPersonService().savePersonAttributeType(
 						roleAttributeType);
 			}
 
 			PersonAttribute roledAtrribute = new PersonAttribute();
 			roledAtrribute.setAttributeType(roleAttributeType);
 			roledAtrribute.setValue("Provider");
 
 			SortedSet<PersonAttribute> attributes = new TreeSet<PersonAttribute>();
 			attributes.add(NIDAtrribute);
 			attributes.add(roledAtrribute);
 
 			providerCandidate.setAttributes(attributes);
 
 			Person candidate = Context.getPersonService().savePerson(
 					providerCandidate);
 
 			p = candidate;
 		}
 		return p;
 	}
 
 	private Location getLocation(OBR obr) throws HL7Exception {
 		String hl7Location = obr.getFillerField1().getValue();
 		Location location = null;
 
 		List<Location> locationsList = Context.getLocationService()
 				.getAllLocations();
 		Context.getLocationService().getLocation(hl7Location);
 		for (Location l : locationsList) {
 			String des = l.getDescription();
 			String fosaid = null;
 			if (des != null) {
 				fosaid = des.toString();
 			}
 			String elid = null;
 
 			if (fosaid != null) {
 				final Matcher matcher = Pattern.compile(":").matcher(fosaid);
 				if (matcher.find()) {
 					elid = fosaid.substring(matcher.end()).trim();
 					if (elid.equals(hl7Location)) {
 						location = l;
 					}
 				}
 			}
 		}
 
 		return (location == null) ? Context.getLocationService()
 				.getDefaultLocation() : location;
 	}
 
 	private EncounterType getEncounterType(OBR obr) {
 		String admissionType = obr.getUniversalServiceIdentifier().getText()
 				.getValue().toString();
 		EncounterType encounterType = Context.getEncounterService()
 				.getEncounterType(admissionType);
 
 		if (encounterType == null) {
 			log.info("EncounterType does not exsist, creating a new one for :"
 					+ admissionType);
 			EncounterType newEncounterType = new EncounterType();
 			newEncounterType.setName(admissionType);
 			newEncounterType.setDescription("CREATED BY POC Adapter MODULE");
 
 			Context.getEncounterService().saveEncounterType(newEncounterType);
 			log.info("Saved newly created encounter type");
 			return newEncounterType;
 		} else {
 			return encounterType;
 		}
 	}
 
 	private Date getDate(OBR obr) {
 		try {
 			String str_date = obr.getObservationDateTime().getTime().getValue();
 			DateFormat formatter;
 			Date date;
 			formatter = new SimpleDateFormat("yyyyMMddhhmm");
 			date = (Date) formatter.parse(str_date);
 			return date;
 		} catch (ParseException e) {
 			return new Date();
 		}
 
 	}
 
 	private Encounter createEncounter(MSH msh, Patient patient, ORC orc,
 			OBR obr, OBR encObr) throws HL7Exception {
 		// the encounter we will return
 		Encounter encounter = null;
 		encounter = new Encounter();
 
 		Date encounterDate = getEncounterDate(encObr);
 		Person provider = getProviderORUR01(encObr);
 		Location location = getLocation(encObr);
 		EncounterType encounterType = getEncounterType(encObr);
 		User enterer = Context.getAuthenticatedUser();
 		Date date = getDate(encObr);
 
 		encounter.setEncounterDatetime(encounterDate);
 		encounter.setProvider(provider);
 		encounter.setPatient(patient);
 		encounter.setLocation(location);
 		encounter.setEncounterType(encounterType);
 		encounter.setCreator(enterer);
 		encounter.setDateCreated(date);
 
 		return encounter;
 	}
 
 	public ORU_R01 generateORU_R01Message(Patient pat,
 			List<Encounter> encounterList) throws HL7Exception {
 		MSH msh = r01.getMSH();
 		String implementationId = "";
 		String fosaid = "";
 		// Get current date
 		String dateFormat = "yyyyMMddHHmmss";
 		SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
 		String formattedDate = formatter.format(new Date());
 
 		msh.getFieldSeparator().setValue(RHEAHL7Constants.FIELD_SEPARATOR);//
 		msh.getEncodingCharacters().setValue(
 				RHEAHL7Constants.ENCODING_CHARACTERS);//
 		msh.getVersionID().getInternationalizationCode().getIdentifier()
 				.setValue(RHEAHL7Constants.INTERNATIONALIZATION_CODE);//
 		msh.getVersionID().getVersionID().setValue(RHEAHL7Constants.VERSION);//
 		msh.getDateTimeOfMessage().getTime().setValue(formattedDate);//
 		try {
 			implementationId = (Context.getAdministrationService()
 					.getImplementationId().getImplementationId() != null) ? Context
 					.getAdministrationService().getImplementationId()
 					.getImplementationId()
 					: "rwanda000";
 		} catch (NullPointerException e) {
 			log.error("No Implementation Id  set;");
 			implementationId = "rwanda000";
 		}
 		implementationId = implementationId.toLowerCase();
 		fosaid = implementationId
 				.substring(implementationId.indexOf("rwanda") + 6);
 		msh.getSendingFacility().getNamespaceID().setValue(fosaid);//
 		msh.getMessageType().getMessageCode()
 				.setValue(RHEAHL7Constants.MESSAGE_TYPE);//
 		msh.getMessageType().getTriggerEvent()
 				.setValue(RHEAHL7Constants.TRIGGER_EVENT);//
 		msh.getMessageType().getMessageStructure()
 				.setValue(RHEAHL7Constants.MESSAGE_STRUCTURE);//
 		msh.getReceivingFacility().getNamespaceID()
 				.setValue(RHEAHL7Constants.RECEIVING_FACILITY);//
 		msh.getProcessingID().getProcessingID()
 				.setValue(RHEAHL7Constants.PROCESSING_ID);//
 		msh.getProcessingID().getProcessingMode()
 				.setValue(RHEAHL7Constants.PROCESSING_MODE);//
 		msh.getMessageControlID().setValue(UUID.randomUUID().toString());//
 
 		msh.getAcceptAcknowledgmentType().setValue(RHEAHL7Constants.ACK_TYPE);
 		msh.getApplicationAcknowledgmentType().setValue(
 				RHEAHL7Constants.APPLICATION_ACK_TYPE);
 		msh.getMessageProfileIdentifier(0).getEntityIdentifier()
 				.setValue(RHEAHL7Constants.MSG_PROFILE_IDENTIFIER);
 		PID pid = r01.getPATIENT_RESULT().getPATIENT().getPID();
 
 		Patient patient = pat;
 		int i = 0;
 		String id = "";
 		String idType = "";
 		try {
 			{
 				PatientIdentifierType nid = Context.getPatientService()
 						.getPatientIdentifierTypeByName("NID");
 				PatientIdentifierType mutuelle = Context.getPatientService()
 						.getPatientIdentifierTypeByName("Mutuelle");
 				PatientIdentifierType rama = Context.getPatientService()
 						.getPatientIdentifierTypeByName("RAMA");
 				PatientIdentifierType primaryCare = Context.getPatientService()
 						.getPatientIdentifierTypeByName("Primary Care ID Type");
 
 				if (getPatientIdentifierByIdentifierType(patient, nid) != null) {
 					log.info("Get NID");
 					id = "";
 					idType = "";
 					idType = nid.getName();
 					id = getPatientIdentifierByIdentifierType(patient, nid)
 							.getIdentifier();
 					if (id != "" && idType != "") {
 
 						pid.getPatientIdentifierList(i).getIDNumber()
 								.setValue(id);
 						pid.getPatientIdentifierList(i).getIdentifierTypeCode()
 								.setValue(idType);
 						i++;
 					}
 				}
 				if (getPatientIdentifierByIdentifierType(patient, rama) != null) {
 					log.info("Get Rama");
 					id = "";
 					idType = "";
 					idType = rama.getName();
 					id = getPatientIdentifierByIdentifierType(patient, rama)
 							.getIdentifier();
 					if (id != "" && idType != "") {
 
 						pid.getPatientIdentifierList(i).getIDNumber()
 								.setValue(id);
 						pid.getPatientIdentifierList(i).getIdentifierTypeCode()
 								.setValue(idType);
 						i++;
 					}
 				}
 				if (getPatientIdentifierByIdentifierType(patient, mutuelle) != null) {
 					log.info("Get Mutuelle");
 					id = "";
 					idType = "";
 					idType = mutuelle.getName();
 					id = getPatientIdentifierByIdentifierType(patient, mutuelle)
 							.getIdentifier();
 					if (id != "" && idType != "") {
 
 						pid.getPatientIdentifierList(i).getIDNumber()
 								.setValue(id);
 						pid.getPatientIdentifierList(i).getIdentifierTypeCode()
 								.setValue(idType);
 						i++;
 					}
 				}
 				if (getPatientIdentifierByIdentifierType(patient, primaryCare) != null) {
 					log.info("Get OMRS");
 					id = "";
 					idType = "";
 					try {
 						implementationId = (Context.getAdministrationService()
 								.getImplementationId().getImplementationId() != null) ? Context
 								.getAdministrationService()
 								.getImplementationId().getImplementationId()
 								: "rwanda000";
 					} catch (NullPointerException e) {
 						log.error("No Implementation Id  set;");
 						implementationId = "rwanda000";
 					}
 					implementationId = implementationId.toLowerCase();
 					fosaid = implementationId.substring(implementationId
 							.indexOf("rwanda") + 6);
 					idType = "OMRS" + fosaid;
 					id = getPatientIdentifierByIdentifierType(patient,
 							primaryCare).getIdentifier();
 					if (id != "" && idType != "") {
 
 						pid.getPatientIdentifierList(i).getIDNumber()
 								.setValue(id);
 						pid.getPatientIdentifierList(i).getIdentifierTypeCode()
 								.setValue(idType);
 						i++;
 					}
 				}
 				if (!patient.getPatientIdentifier().getIdentifierType()
 						.equals(nid)
 						&& !patient.getPatientIdentifier().getIdentifierType()
 								.equals(rama)
 						&& !patient.getPatientIdentifier().getIdentifierType()
 								.equals(mutuelle)
 						&& !patient.getPatientIdentifier().getIdentifierType()
 								.equals(primaryCare)) {
 					id = "";
 					idType = "";
 					log.info("Get "
 							+ patient.getPatientIdentifier().getIdentifier());
 					idType = patient.getPatientIdentifier().getIdentifierType()
 							.getName();
 					id = patient.getPatientIdentifier().getIdentifier();
 					if (id != "" && idType != "") {
 
 						pid.getPatientIdentifierList(i).getIDNumber()
 								.setValue(id);
 						pid.getPatientIdentifierList(i).getIdentifierTypeCode()
 								.setValue(idType);
 						i++;
 					}
 
 				}
 
 			}
 		} catch (Exception e) {
 			log.info(e.getMessage());
 			idType = patient.getPatientIdentifier().getIdentifierType()
 					.getName();
 			id = patient.getPatientIdentifier().getIdentifier();
 
 			pid.getPatientIdentifierList(i).getIDNumber().setValue(id);
 			pid.getPatientIdentifierList(i).getIdentifierTypeCode()
 					.setValue(idType);
 
 		}
 
 		// Cohort singlePatientCohort = new Cohort();
 		// singlePatientCohort.addMember(pat.getId());
 		//
 		// Map<Integer, PatientIdentifier> patientIdentifierMap = Context
 		// .getPatientSetService().getPatientIdentifiersByType(
 		// singlePatientCohort,
 		// Context.getPatientService()
 		// .getPatientIdentifierTypeByName(
 		// RHEAHL7Constants.IDENTIFIER_TYPE));
 		//
 		// PID pid = r01.getPATIENT_RESULT().getPATIENT().getPID();
 		//
 		// pid.getSetIDPID().setValue(RHEAHL7Constants.IDPID);
 		// pid.getPatientIdentifierList(0)
 		// .getIDNumber()
 		// .setValue(
 		// patientIdentifierMap
 		// .get(patientIdentifierMap.keySet().iterator()
 		// .next()).getIdentifier());
 		//
 		// pid.getPatientIdentifierList(0).getIdentifierTypeCode()
 		// .setValue(RHEAHL7Constants.IDENTIFIER_TYPE);
 		pid.getPatientName(0).getFamilyName().getSurname()
 				.setValue(pat.getFamilyName());
 		pid.getPatientName(0).getGivenName().setValue(pat.getGivenName());
 
 		PV1 pv1 = r01.getPATIENT_RESULT().getPATIENT().getVISIT().getPV1();
 
 		pv1.getPatientClass().setValue(RHEAHL7Constants.PATIENT_CLASS);
 		pv1.getAssignedPatientLocation().getFacility().getNamespaceID()
 				.setValue(encounterList.get(0).getLocation().getName());
 		pv1.getAssignedPatientLocation()
 				.getPointOfCare()
 				.setValue(
 						encounterList.get(0).getLocation().getLocationId()
 								.toString());
 		pv1.getAdmissionType().setValue(
 				encounterList.get(0).getEncounterType().getName());
 
 		pv1.getPatientClass().setValue(RHEAHL7Constants.PATIENT_CLASS);
 		pv1.getAssignedPatientLocation().getFacility().getNamespaceID()
 				.setValue(encounterList.get(0).getLocation().getName());
 		pv1.getAssignedPatientLocation()
 				.getPointOfCare()
 				.setValue(
 						encounterList.get(0).getLocation().getLocationId()
 								.toString());
 		pv1.getAdmissionType().setValue(
 				encounterList.get(0).getEncounterType().getName());
 
 		Person provider = encounterList.get(0).getProvider();
 		TransactionService service = Context
 				.getService(TransactionService.class);
 		// Cohort singleProviderCohort = new Cohort();
 		// singleProviderCohort.addMember(provider.getId());
 		//
 		// Map<Integer, PatientIdentifier> providerIdentifierMap = Context
 		// .getPatientSetService().getPatientIdentifiersByType(
 		// singleProviderCohort,
 		// Context.getPatientService()
 		// .getPatientIdentifierTypeByName("NID"));
 		//
 		// String providerNID = providerIdentifierMap.get(
 		// providerIdentifierMap.keySet().iterator().next())
 		// .getIdentifier();
 		String providerNID = service.getPersonAttributesByPerson(provider,
 				"NID");
 		log.info(providerNID + " Provider NID");
 
 		pv1.getAttendingDoctor(0).getFamilyName().getSurname()
 				.setValue(encounterList.get(0).getProvider().getFamilyName());
 		pv1.getAttendingDoctor(0).getGivenName()
 				.setValue(encounterList.get(0).getProvider().getGivenName());
 		pv1.getAttendingDoctor(0).getIdentifierTypeCode().setValue("NID");
 		pv1.getAttendingDoctor(0).getIDNumber().setValue(providerNID);
 		pv1.getAdmitDateTime()
 				.getTime()
 				.setValue(
 						new SimpleDateFormat("yyyyMMddhhmm")
 								.format(encounterList.get(0).getDateCreated()));
 
 		// populate ORC segments
 
 		try {
 			createORC(r01, encounterList);
 		} catch (Exception e) {
 			log.error("Error generated", e);
 		}
 
 		// populate OBR segments
 
 		try {
 			createOBREnc(r01, encounterList);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			log.error("Error generated", e);
 		}
 
 		// populate OBX segments
 
 		ConceptService cs = Context.getConceptService();
 
 		int counter = 1;
 
 		return r01;
 	}
 
 	public String getMessage(ORU_R01 roi) {
 		Parser parser = new GenericParser();
 		String msg = null;
 		try {
 			msg = parser.encode(roi, "XML");
 		} catch (HL7Exception e) {
 			log.error("Exception parsing constructed message.");
 		}
 		return msg;
 	}
 
 	private static void createORC(ORU_R01 r01, List<Encounter> encounterList)
 			throws Exception {
 		int orderORCCount = 0;
 
 		ORC orc = null;
 
 		orc = r01.getPATIENT_RESULT().getORDER_OBSERVATION(orderORCCount)
 				.getORC();
 		orc.getOrderControl().setValue(RHEAHL7Constants.ORDER_CONTROL);
 		orc.getOrderingProvider(0)
 				.getIDNumber()
 				.setValue(encounterList.get(0).getProvider().getId().toString());
 
 		orc.getOrderControlCodeReason().getIdentifier().setValue("Identifier");
 		orc.getOrderControlCodeReason().getText().setValue("Text");
 		orc.getOrderControlCodeReason().getNameOfCodingSystem()
 				.setValue("Name of Coding System");
 
 		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
 		String dateStr = "";
 		Date d = new Date();
 		dateStr = df.format(d);
 
 		orc.getDateTimeOfTransaction().getTime().setValue(dateStr);
 
 		orderORCCount++;
 
 	}
 
 	private void createOBREnc(ORU_R01 r01, List<Encounter> encounterList)
 			throws Exception {
 
 		for (Encounter e : encounterList) {
 			OBR obr = null;
 
 			obr = r01.getPATIENT_RESULT().getORDER_OBSERVATION(orderObsCount)
 					.getOBR();
 			int reps = r01.getPATIENT_RESULT().getORDER_OBSERVATIONReps();
 
 			Date encDt = e.getEncounterDatetime();
 			SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
 			SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");
 
 			String encDateStr = "";
 			String encDateOnly = "";
 			if (encDt != null) {
 				encDateStr = df.format(encDt);
 				encDateOnly = dayFormat.format(encDt);
 			}
 			obr.getObservationDateTime().getTime().setValue(encDateStr);
 			obr.getSetIDOBR().setValue(String.valueOf(orderObsCount));
 			obr.getUniversalServiceIdentifier().getText()
 					.setValue(e.getEncounterType().getName());
 
 			// Accession number
 			String accessionNumber = String.valueOf(e.getEncounterId());
 
 			obr.getFillerOrderNumber().getEntityIdentifier()
 					.setValue(accessionNumber);
 
 			obr.getFillerField1().setValue(e.getLocation().getId().toString());
 			obr.getFillerField2().setValue(e.getLocation().getName());
 
 			orderObsCount++;
 			createOBRGroup(r01, orderObsCount, e);
 			orderObsCount++;
 		}
 
 	}
 
 	private void createOBRGroup(ORU_R01 r01, int orderObsCount,
 			Encounter encounter) throws HL7Exception {
 
 		Set<Obs> allObs = encounter.getAllObs();
 		Set<Obs> rejectedObs = new HashSet<Obs>();
 		Set<Obs> unrelatedObs = new HashSet<Obs>();
 		Set<Obs> acceptedObs = new HashSet<Obs>();
 
 		Iterator<Obs> it = allObs.iterator();
 
 		while (it.hasNext()) {
 			Obs obs = it.next();
 			if (obs.getObsGroup() != null && !rejectedObs.contains(obs)) {
 				Obs parentObs = obs.getObsGroup();
 				Set<Obs> childObs = parentObs.getGroupMembers();
 
 				acceptedObs.add(parentObs);
 				acceptedObs.addAll(childObs);
 
 				it.remove();
 				rejectedObs.addAll(childObs);
 
 				parentObs.setVoided(true);
 				createOBRGroupSegment(r01, encounter, parentObs, childObs,
 						orderObsCount);
 				orderObsCount++;
 
 			}
 		}
 
 		allObs.removeAll(acceptedObs);
 
 		if (allObs.size() > 0) {
 			createOBRGroupSegment(r01, encounter, null, allObs, orderObsCount);
 		}
 
 	}
 
 	private void createOBRGroupSegment(ORU_R01 r01, Encounter encounter,
 			Obs parentObs, Set<Obs> childObs, int orderObsCount)
 			throws HL7Exception {
 		OBR obr = null;
 
 		obr = r01.getPATIENT_RESULT().getORDER_OBSERVATION(orderObsCount)
 				.getOBR();
 
 		Date encDt = encounter.getEncounterDatetime();
 		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
 		SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");
 
 		String encDateStr = "";
 		String encDateOnly = "";
 		if (encDt != null) {
 			encDateStr = df.format(encDt);
 			encDateOnly = dayFormat.format(encDt);
 		}
 		obr.getSetIDOBR().setValue(String.valueOf(orderObsCount));
 
 		if (parentObs != null) {
 			if (parentObs.isObsGrouping()) {
 
 				Collection<ConceptMap> conceptMappings = parentObs.getConcept()
 						.getConceptMappings();
 				Iterator<ConceptMap> itr = conceptMappings.iterator();
 				boolean hasMapping = false;
 
 				while (itr.hasNext() && hasMapping == false) {
 					ConceptMap map = itr.next();
 					if (map.getSource().getName().toString().equals("RWCS")
 							|| map.getSource().getName().toString()
 									.equals("ICD10")
 							|| map.getSource().getName().toString()
 									.equals("LOINC")) {
 
 						obr.getUniversalServiceIdentifier().getIdentifier()
 								.setValue(map.getSourceCode());
 						obr.getUniversalServiceIdentifier()
 								.getText()
 								.setValue(
 										parentObs.getConcept().getName()
 												.toString());
 						obr.getUniversalServiceIdentifier()
 								.getNameOfCodingSystem()
 								.setValue(map.getSource().getName());
 						hasMapping = true;
 					}
 				}
 			}
 		}
 
 		// Accession number
 		String accessionNumber = String.valueOf(encounter.getEncounterId());
 
 		obr.getParentNumber().getFillerAssignedIdentifier().getUniversalID()
 				.setValue(accessionNumber);
 
 		int x = orderObsCount - 1;
 		obr.getPlacerField1().setValue(Integer.toString(x));
 
 		for (Obs ob : childObs) {
 			boolean successful = createOBXSegment(ob, orderObsCount, counter);
 			if (successful)
 				counter++;
 		}
 
 		orderObsCount = orderObsCount + 1;
 
 	}
 
 	private boolean createOBXSegment(Obs ob, int orderObsCount, int counter)
 			throws HL7Exception, DataTypeException {
 		ConceptService cs = Context.getConceptService();
 
 		OBX obx = r01.getPATIENT_RESULT().getORDER_OBSERVATION(orderObsCount)
 				.getOBSERVATION(counter).getOBX();
 		obx.getSetIDOBX().setValue(counter + "");
 
 		Collection<ConceptMap> conceptMappings = ob.getConcept()
 				.getConceptMappings();
 		Iterator<ConceptMap> itr = conceptMappings.iterator();
 		boolean hasMapping = false;
 
 		while (itr.hasNext() && hasMapping == false) {
 			ConceptMap map = itr.next();
 			if (map.getSource().getName().toString().equals("RWCS")
 					|| map.getSource().getName().toString().equals("ICD10")
 					|| map.getSource().getName().toString().equals("LOINC")) {
 
 				obx.getObservationIdentifier().getIdentifier()
 						.setValue(map.getSourceCode());
 				obx.getObservationIdentifier().getText()
 						.setValue(ob.getConcept().getName().toString());
 				obx.getObservationIdentifier().getNameOfCodingSystem()
 						.setValue(map.getSource().getName());
 				hasMapping = true;
 			}
 		}
 
 		ConceptDatatype datatype = ob.getConcept().getDatatype();
 
 		if (ob.getConcept().isNumeric()) {
 			obx.getValueType().setValue(RHEAHL7Constants.HL7_NUMERIC);
 
 			NM nm = new NM(r01);
 			nm.setValue(ob.getValueNumeric() + "");
 
 			Concept concept = ob.getConcept();
 			if (concept.isNumeric()) {
 				ConceptNumeric conceptNumeric = cs.getConceptNumeric(concept
 						.getId());
 				if (conceptNumeric.getUnits() != null
 						&& !conceptNumeric.getUnits().equals("")) {
 					obx.getUnits().getIdentifier()
 							.setValue(conceptNumeric.getUnits());
 					obx.getUnits().getNameOfCodingSystem()
 							.setValue(RHEAHL7Constants.UNIT_CODING_SYSTEM);
 				}
 			}
 			obx.getObservationValue(0).setData(nm);
 			TS ts = new TS(r01);
 			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
 			ts.getTime().setValue(sdf.format(ob.getDateCreated()));
 			obx.getDateTimeOfTheObservation().getTime()
 					.setValue(sdf.format(ob.getDateCreated()));
 
 		} else if (datatype
 				.equals(cs
 						.getConceptDatatypeByName(RHEAHL7Constants.CONCEPT_DATATYPE_DATETIME))
 				|| datatype
 						.equals(cs
 								.getConceptDatatypeByName(RHEAHL7Constants.CONCEPT_DATATYPE_DATE))) {
 
 			obx.getValueType().setValue(RHEAHL7Constants.HL7_DATETIME);
 			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
 			TS ts = new TS(r01);
 			ts.getTime().setValue(sdf.format(ob.getValueDatetime()));
 			obx.getObservationValue(0).setData(ts);
 
 			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddHHmmss");
 			ts.getTime().setValue(sdf1.format(ob.getValueDatetime()));
 			obx.getDateTimeOfTheObservation().getTime()
 					.setValue(sdf.format(ob.getDateCreated()));
 
 		} else if (datatype
 				.equals(cs
 						.getConceptDatatypeByName(RHEAHL7Constants.CONCEPT_DATATYPE_TEXT))) {
 
 			obx.getValueType().setValue(RHEAHL7Constants.HL7_TEXT);
 			ST st = new ST(r01);
 			st.setValue(ob.getValueText());
 			obx.getObservationValue(0).setData(st);
 
 			TS ts = new TS(r01);
 			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
 			ts.getTime().setValue(sdf.format(ob.getDateCreated()));
 			obx.getDateTimeOfTheObservation().getTime()
 					.setValue(sdf.format(ob.getDateCreated()));
 
 		} else if (datatype
 				.equals(cs
 						.getConceptDatatypeByName(RHEAHL7Constants.CONCEPT_DATATYPE_CODED))) {
 
 			obx.getValueType().setValue(RHEAHL7Constants.HL7_CODED);
 
 			CE ce = new CE(r01);
 			Concept concept = ob.getValueCoded();
 
 			Collection<ConceptMap> conceptValueMappings = concept
 					.getConceptMappings();
 
 			Iterator<ConceptMap> itr2 = conceptValueMappings.iterator();
 			boolean hasValueMapping = false;
 
 			while (itr2.hasNext() && hasValueMapping == false) {
 				ConceptMap map = itr2.next();
 				if (map.getSource().getName().toString().equals("RWCS")
 						|| map.getSource().getName().toString().equals("ICD10")
 						|| map.getSource().getName().toString().equals("LOINC")) {
 
 					ce.getNameOfCodingSystem().setValue(
 							map.getSource().getName());
 					String nameStr = concept.getName().toString();
 					ce.getText().setValue(nameStr);
 					ce.getIdentifier().setValue(map.getSourceCode());
 					hasValueMapping = true;
 				}
 			}
 
 			obx.getObservationValue(0).setData(ce);
 
 			TS ts = new TS(r01);
 			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
 			ts.getTime().setValue(sdf.format(ob.getDateCreated()));
 			obx.getDateTimeOfTheObservation().getTime()
 					.setValue(sdf.format(ob.getDateCreated()));
 		}
 
 		obxCount++;
 		return true;
 	}
 
 	@Override
 	public Object encodingEncounterToMessage(Patient patient,
 			List<Encounter> encounters) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
