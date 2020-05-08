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
 
 package org.openmrs.module.odkconnector.serialization.processor;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.GZIPOutputStream;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.math.NumberUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Cohort;
 import org.openmrs.Concept;
 import org.openmrs.api.context.Context;
 import org.openmrs.cohort.CohortSearchHistory;
 import org.openmrs.module.odkconnector.api.ConceptConfiguration;
 import org.openmrs.module.odkconnector.api.service.ConnectorService;
 import org.openmrs.module.odkconnector.api.utils.ConnectorUtils;
 import org.openmrs.module.odkconnector.reporting.metadata.DefinitionProperty;
 import org.openmrs.module.odkconnector.reporting.metadata.ExtendedDefinition;
 import org.openmrs.module.odkconnector.reporting.service.ReportingConnectorService;
 import org.openmrs.module.odkconnector.serialization.Processor;
 import org.openmrs.module.odkconnector.serialization.Serializer;
 import org.openmrs.module.odkconnector.serialization.serializable.SerializedCohort;
 import org.openmrs.module.odkconnector.serialization.serializable.SerializedForm;
 import org.openmrs.module.reporting.cohort.EvaluatedCohort;
 import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
 import org.openmrs.module.reporting.evaluation.EvaluationContext;
 import org.openmrs.reporting.AbstractReportObject;
 import org.openmrs.reporting.PatientSearch;
 import org.openmrs.reporting.PatientSearchReportObject;
 import org.openmrs.util.HandlerUtil;
 import org.openmrs.util.OpenmrsConstants;
 
 public class HttpProcessor implements Processor {
 
 	private static final Log log = LogFactory.getLog(HttpProcessor.class);
 
 	public static final String PROCESS_COHORT = "download.cohort";
 
 	public static final String PROCESS_PATIENTS = "download.patients";
 
 	private String action;
 
 	public HttpProcessor(final String action) {
 		this.action = action;
 	}
 
 	/**
 	 * Get the http processor action
 	 *
 	 * @return the http processor action
 	 */
 	public String getAction() {
 		return action;
 	}
 
 	/**
 	 * Set the http processor action
 	 *
 	 * @param action the http processor action
 	 */
 	public void setAction(final String action) {
 		this.action = action;
 	}
 
 	/**
 	 * Process any stream connection to this module
 	 *
 	 * @param inputStream  the input stream
 	 * @param outputStream the output stream
 	 * @throws Exception when the stream processing failed
 	 */
 	@Override
 	public void process(final InputStream inputStream, final OutputStream outputStream) throws Exception {
 
 		GZIPInputStream gzipInputStream = new GZIPInputStream(new BufferedInputStream(inputStream));
 
 		DataInputStream dataInputStream = new DataInputStream(gzipInputStream);
 		String username = dataInputStream.readUTF();
 		String password = dataInputStream.readUTF();
 		Boolean savedSearch = dataInputStream.readBoolean();
 		Integer cohortId = 0;
 		Integer programId = 0;
 		if (StringUtils.equalsIgnoreCase(getAction(), HttpProcessor.PROCESS_PATIENTS)) {
 			cohortId = dataInputStream.readInt();
 			programId = dataInputStream.readInt();
 		}
 		dataInputStream.close();
 
 		GZIPOutputStream gzipOutputStream = new GZIPOutputStream(new BufferedOutputStream(outputStream));
 		DataOutputStream dataOutputStream = new DataOutputStream(gzipOutputStream);
 		try {
 			Context.openSession();
 			Context.authenticate(username, password);
 
             dataOutputStream.writeInt(HttpURLConnection.HTTP_OK);
 
 			Serializer serializer = HandlerUtil.getPreferredHandler(Serializer.class, List.class);
 			if (StringUtils.equalsIgnoreCase(getAction(), HttpProcessor.PROCESS_PATIENTS)) {
 				ConnectorService connectorService = Context.getService(ConnectorService.class);
 
 				Cohort cohort = new Cohort();
 				if (savedSearch) {
 					CohortSearchHistory history = new CohortSearchHistory();
 					PatientSearchReportObject patientSearchReportObject = (PatientSearchReportObject) Context.getReportObjectService().getReportObject(cohortId);
 					if (patientSearchReportObject != null) {
 						history.addSearchItem(PatientSearch.createSavedSearchReference(cohortId));
 						cohort = history.getPatientSet(0, null);
 					}
 				} else {
 					cohort = Context.getCohortService().getCohort(cohortId);
 				}
 
                 System.out.println("Writing patients information!");
 				serializer.write(dataOutputStream, connectorService.getCohortPatients(cohort));
 
 				// check the concept list
 				Collection<Concept> concepts = null;
 				ConceptConfiguration conceptConfiguration = connectorService.getConceptConfiguration(programId);
 				if (conceptConfiguration != null)
 					concepts = ConnectorUtils.getConcepts(conceptConfiguration.getConfiguredConcepts());
                 System.out.println("Writing observations information!");
 				serializer.write(dataOutputStream, connectorService.getCohortObservations(cohort, concepts));
 
 				// evaluate and get the applicable form for the patients
 				CohortDefinitionService cohortDefinitionService = Context.getService(CohortDefinitionService.class);
 				ReportingConnectorService reportingConnectorService = Context.getService(ReportingConnectorService.class);
 				List<ExtendedDefinition> definitions = reportingConnectorService.getAllExtendedDefinition();
 
 				EvaluationContext context = new EvaluationContext();
 				context.setBaseCohort(cohort);
 
 				List<SerializedForm> serializedForms = new ArrayList<SerializedForm>();
 				for (ExtendedDefinition definition : definitions) {
 					if (definition.containsProperty(ExtendedDefinition.DEFINITION_PROPERTY_FORM)) {
 						EvaluatedCohort evaluatedCohort = cohortDefinitionService.evaluate(definition.getCohortDefinition(), context);
                        Collection intersectedMemberIds = CollectionUtils.intersection(evaluatedCohort.getMemberIds(), cohort.getMemberIds());
 						for (DefinitionProperty definitionProperty : definition.getProperties()) {
 							Integer formId = NumberUtils.toInt(definitionProperty.getPropertyValue());
 							for (Object patientId : intersectedMemberIds)
 								serializedForms.add(new SerializedForm(NumberUtils.toInt(String.valueOf(patientId)), formId));
 						}
 					}
 				}
                 System.out.println("Writing forms information!");
 				serializer.write(dataOutputStream, serializedForms);
 
 			} else {
 				if (savedSearch) {
 					List<SerializedCohort> serializedCohorts = new ArrayList<SerializedCohort>();
 					List<AbstractReportObject> objects = Context.getReportObjectService().getReportObjectsByType(OpenmrsConstants.REPORT_OBJECT_TYPE_PATIENTSEARCH);
 					for (AbstractReportObject object : objects) {
 						SerializedCohort serializedCohort = new SerializedCohort();
 						serializedCohort.setId(object.getReportObjectId());
 						serializedCohort.setName(object.getName());
 						serializedCohorts.add(serializedCohort);
 					}
 					serializer.write(dataOutputStream, serializedCohorts);
 
 				} else {
 					serializer.write(dataOutputStream, Context.getCohortService().getAllCohorts());
 				}
 			}
 
 			dataOutputStream.close();
 		} catch (Exception e) {
 			log.error("Processing stream failed!", e);
 			dataOutputStream.writeInt(HttpURLConnection.HTTP_UNAUTHORIZED);
 			dataOutputStream.close();
 		} finally {
 			Context.closeSession();
 		}
 	}
 
 }
