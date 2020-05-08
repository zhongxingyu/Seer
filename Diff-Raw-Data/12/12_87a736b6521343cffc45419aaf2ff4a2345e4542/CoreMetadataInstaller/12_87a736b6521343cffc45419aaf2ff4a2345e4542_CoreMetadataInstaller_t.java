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
 
 package org.openmrs.module.kenyacore.metadata.installer;
 
 import org.openmrs.EncounterType;
 import org.openmrs.Form;
 import org.openmrs.GlobalProperty;
 import org.openmrs.LocationAttributeType;
 import org.openmrs.PatientIdentifierType;
 import org.openmrs.PersonAttributeType;
 import org.openmrs.Program;
 import org.openmrs.VisitAttributeType;
 import org.openmrs.VisitType;
 import org.openmrs.api.AdministrationService;
 import org.openmrs.api.EncounterService;
 import org.openmrs.api.FormService;
 import org.openmrs.api.LocationService;
 import org.openmrs.api.PatientService;
 import org.openmrs.api.PersonService;
 import org.openmrs.api.ProgramWorkflowService;
 import org.openmrs.api.VisitService;
 import org.openmrs.customdatatype.CustomDatatype;
 import org.openmrs.module.kenyacore.metadata.MetadataUtils;
 import org.openmrs.patient.IdentifierValidator;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Component;
 import org.springframework.validation.Validator;
 
 /**
  * Component for installing standard types of metadata
  */
 @Component
 public class CoreMetadataInstaller {
 
 	@Autowired
 	@Qualifier("adminService")
 	private AdministrationService administrationService;
 
 	@Autowired
 	@Qualifier("encounterService")
 	private EncounterService encounterService;
 
 	@Autowired
 	@Qualifier("formService")
 	private FormService formService;
 
 	@Autowired
 	@Qualifier("locationService")
 	private LocationService locationService;
 
 	@Autowired
 	@Qualifier("patientService")
 	private PatientService patientService;
 
 	@Autowired
 	@Qualifier("personService")
 	private PersonService personService;
 
 	@Autowired
 	@Qualifier("programWorkflowService")
 	private ProgramWorkflowService programService;
 
 	@Autowired
 	@Qualifier("visitService")
 	private VisitService visitService;
 
 	/**
 	 * Installs an encounter type
 	 * @param name the name
 	 * @param description the description
 	 * @param uuid the UUID
 	 * @return the encounter type
 	 */
 	public EncounterType encounterType(String name, String description, String uuid) {
 		EncounterType obj = findEncounterType(name, uuid);
 		if (obj == null) {
 			obj = new EncounterType();
 		}
 
 		obj.setName(name);
 		obj.setDescription(description);
 		obj.setUuid(uuid);
 
 		return encounterService.saveEncounterType(obj);
 	}
 
 	/**
 	 * Installs a form
 	 * @param name the name
 	 * @param description the description
 	 * @param encTypeUuid the encounter type UUID
 	 * @param uuid the UUID
 	 * @return the form
 	 */
 	public Form form(String name, String description, String encTypeUuid, String version, String uuid) {
 		Form obj = findForm(name, version, uuid);
 		if (obj == null) {
 			obj = new Form();
 		}
 
 		obj.setName(name);
 		obj.setDescription(description);
 		obj.setEncounterType(MetadataUtils.getEncounterType(encTypeUuid));
 		obj.setVersion(version);
 		obj.setUuid(uuid);
 
 		return formService.saveForm(obj);
 	}
 
 	/**
 	 * Installs a global property
 	 * @param property the property
 	 * @param description the description
 	 * @param datatype the custom data type (can be null)
 	 * @param datatypeConfig the data type config (can be null)
 	 * @param value the value (can be null)
 	 * @return the global property
 	 */
 	public <T, H extends CustomDatatype<T>> GlobalProperty globalProperty(String property,
 																		  String description,
 																		  Class<H> datatype,
 																		  String datatypeConfig,
 																		  T value,
 																		  String uuid) {
 		GlobalProperty obj = findGlobalProperty(property, uuid);
 		if (obj == null) {
 			obj = new GlobalProperty();
 		}
 
 		obj.setProperty(property);
 		obj.setDescription(description);
 
 		if (datatype != null) {
 			obj.setDatatypeClassname(datatype.getName());
 			obj.setDatatypeConfig(datatypeConfig);
 
 			if (value != null) {
 				obj.setValue(value);
 			}
 		}
 		else {
 			obj.setPropertyValue(String.valueOf(value));
 		}
 
 		obj.setUuid(uuid);
 
 		return administrationService.saveGlobalProperty(obj);
 	}
 
 	/**
 	 * Installs a location attribute type
 	 * @param name the name
 	 * @param description the description
 	 * @param datatype the datatype class
 	 * @param datatypeConfig the data type config (can be null)
 	 * @param minOccurs the minimum allowed occurrences
 	 * @param maxOccurs the maximum allowed occurrences
 	 * @param uuid the UUID
 	 * @return the program
 	 */
 	public LocationAttributeType locationAttributeType(String name, String description, Class<?> datatype, String datatypeConfig, int minOccurs, int maxOccurs, String uuid) {
 		LocationAttributeType obj = locationService.getLocationAttributeTypeByUuid(uuid);
 		if (obj == null) {
 			obj = new LocationAttributeType();
 		}
 
 		obj.setName(name);
 		obj.setDescription(description);
 		obj.setDatatypeClassname(datatype.getName());
 		obj.setDatatypeConfig(datatypeConfig);
 		obj.setMinOccurs(minOccurs);
 		obj.setMaxOccurs(maxOccurs);
 		obj.setUuid(uuid);
 
 		return locationService.saveLocationAttributeType(obj);
 	}
 
 	/**
 	 * Installs a patient identifier type
 	 * @param name the name
 	 * @param description the description
 	 * @param format the format regex
 	 * @param uuid the UUID
 	 * @return the program
 	 */
 	public PatientIdentifierType patientIdentifierType(String name,
 													   String description,
 													   String format,
 													   String formatDescription,
 													   Class<? extends IdentifierValidator> validator,
 													   PatientIdentifierType.LocationBehavior locationBehavior,
 													   boolean required,
 													   String uuid) {
 
 		PatientIdentifierType obj = findPatientIdentifierType(name, uuid);
 		if (obj == null) {
 			obj = new PatientIdentifierType();
 		}
 
 		obj.setName(name);
 		obj.setDescription(description);
 		obj.setFormat(format);
 		obj.setFormatDescription(formatDescription);
		obj.setValidator(validator != null ? validator.getName() : null);
 		obj.setLocationBehavior(locationBehavior);
 		obj.setRequired(required);
 		obj.setUuid(uuid);
 
 		return patientService.savePatientIdentifierType(obj);
 	}
 
 	/**
 	 * Installs a person attribute type
 	 * @param name the name
 	 * @param description the description
 	 * @param format the format class
 	 * @param foreignKey the foreign key (can be null)
 	 * @param searchable whether attribute is searchable
 	 * @param sortWeight the sort weight
 	 * @param uuid the UUID
 	 * @return the program
 	 */
 	public PersonAttributeType personAttributeType(String name,
 												   String description,
 												   Class<?> format,
 												   Integer foreignKey,
 												   boolean searchable,
 												   double sortWeight,
 												   String uuid) {
 
 		PersonAttributeType obj = findPersonAttributeType(name, uuid);
 		if (obj == null) {
 			obj = new PersonAttributeType();
 		}
 
 		obj.setName(name);
 		obj.setDescription(description);
 		obj.setFormat(format.getName());
 		obj.setForeignKey(foreignKey);
 		obj.setSearchable(searchable);
 		obj.setSortWeight(sortWeight);
 		obj.setUuid(uuid);
 
 		return personService.savePersonAttributeType(obj);
 	}
 
 	/**
 	 * Installs a program
 	 * @param name the name
 	 * @param description the description
 	 * @param concept the concept identifier
 	 * @param uuid the UUID
 	 * @return the program
 	 */
 	public Program program(String name, String description, String concept, String uuid) {
 		Program obj = findProgram(name, uuid);
 		if (obj == null) {
 			obj = new Program();
 		}
 
 		obj.setName(name);
 		obj.setDescription(description);
 		obj.setConcept(MetadataUtils.getConcept(concept));
 		obj.setUuid(uuid);
 
 		return programService.saveProgram(obj);
 	}
 
 	/**
 	 * Installs a visit attribute type
 	 * @param name the name
 	 * @param description the description
 	 * @param datatype the datatype class
 	 * @param datatypeConfig the data type config (can be null)
 	 * @param minOccurs the minimum allowed occurrences
 	 * @param maxOccurs the maximum allowed occurrences
 	 * @param uuid the UUID
 	 * @return the program
 	 */
 	public VisitAttributeType visitAttributeType(String name, String description, Class<?> datatype, String datatypeConfig, int minOccurs, int maxOccurs, String uuid) {
 		VisitAttributeType obj = visitService.getVisitAttributeTypeByUuid(uuid);
 		if (obj == null) {
 			obj = new VisitAttributeType();
 		}
 
 		obj.setName(name);
 		obj.setDescription(description);
 		obj.setDatatypeClassname(datatype.getName());
 		obj.setDatatypeConfig(datatypeConfig);
 		obj.setMinOccurs(minOccurs);
 		obj.setMaxOccurs(maxOccurs);
 		obj.setUuid(uuid);
 
 		return visitService.saveVisitAttributeType(obj);
 	}
 
 	/**
 	 * Installs a visit type
 	 * @param name the name
 	 * @param description the description
 	 * @param uuid the UUID
 	 * @return the visit type
 	 */
 	public VisitType visitType(String name, String description, String uuid) {
 		VisitType obj = visitService.getVisitTypeByUuid(uuid);
 		if (obj == null) {
 			obj = new VisitType();
 		}
 
 		obj.setName(name);
 		obj.setDescription(description);
 		obj.setUuid(uuid);
 
 		return visitService.saveVisitType(obj);
 	}
 
 	/**
 	 * Finds an existing encounter type
 	 * @param name the name
 	 * @param uuid the uuid
 	 * @return the encounter type or null
 	 */
 	protected EncounterType findEncounterType(String name, String uuid) {
 		EncounterType obj = encounterService.getEncounterTypeByUuid(uuid);
 		if (obj != null) {
 			return obj;
 		}
 
 		return encounterService.getEncounterType(name);
 	}
 
 	/**
 	 * Finds an existing form
 	 * @param name the name
 	 * @param version the version
 	 * @param uuid the uuid
 	 * @return the form or null
 	 */
 	protected Form findForm(String name, String version, String uuid) {
 		Form obj = formService.getFormByUuid(uuid);
 		if (obj != null) {
 			return obj;
 		}
 
 		return formService.getForm(name, version);
 	}
 
 	/**
 	 * Finds an existing global property
 	 * @param property the property
 	 * @param uuid the uuid
 	 * @return the global property or null
 	 */
 	protected GlobalProperty findGlobalProperty(String property, String uuid) {
 		GlobalProperty obj = administrationService.getGlobalPropertyByUuid(uuid);
 		if (obj != null) {
 			return obj;
 		}
 
 		return administrationService.getGlobalPropertyObject(property);
 	}
 
 	/**
 	 * Finds an existing patient identifier type
 	 * @param name the name
 	 * @param uuid the uuid
 	 * @return the patient identifier type or null
 	 */
 	protected PatientIdentifierType findPatientIdentifierType(String name, String uuid) {
 		PatientIdentifierType obj = patientService.getPatientIdentifierTypeByUuid(uuid);
 		if (obj != null) {
 			return obj;
 		}
 
 		return patientService.getPatientIdentifierTypeByName(name);
 	}
 
 	/**
 	 * Finds an existing person attribute type
 	 * @param name the name
 	 * @param uuid the uuid
 	 * @return the person attribute type or null
 	 */
 	protected PersonAttributeType findPersonAttributeType(String name, String uuid) {
 		PersonAttributeType obj = personService.getPersonAttributeTypeByUuid(uuid);
 		if (obj != null) {
 			return obj;
 		}
 
 		return personService.getPersonAttributeTypeByName(name);
 	}
 
 	/**
 	 * Finds an existing program
 	 * @param name the name
 	 * @param uuid the uuid
 	 * @return the program or null
 	 */
 	protected Program findProgram(String name, String uuid) {
 		Program obj = programService.getProgramByUuid(uuid);
 		if (obj != null) {
 			return obj;
 		}
 
 		// In 1.9.x getProgramByName incorrectly looks at concept name (TRUNK-3504)
 		for (Program p : programService.getAllPrograms(true)) {
 			if (p.getName().equals(name)) {
 				return p;
 			}
 		}
 		return null;
 	}
 }
