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
 
 import org.apache.commons.lang3.StringUtils;
 import org.junit.Assert;
 import org.junit.Test;
 import org.openmrs.EncounterType;
 import org.openmrs.Form;
 import org.openmrs.GlobalProperty;
 import org.openmrs.LocationAttributeType;
 import org.openmrs.PatientIdentifierType;
 import org.openmrs.PersonAttributeType;
 import org.openmrs.Program;
 import org.openmrs.VisitAttributeType;
 import org.openmrs.VisitType;
 import org.openmrs.api.context.Context;
 import org.openmrs.customdatatype.SerializingCustomDatatype;
 import org.openmrs.patient.IdentifierValidator;
 import org.openmrs.patient.UnallowedIdentifierException;
 import org.openmrs.test.BaseModuleContextSensitiveTest;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import static org.hamcrest.Matchers.*;
 
 /**
  * Tests for {@link CoreMetadataInstaller}
  */
 public class CoreMetadataInstallerTest extends BaseModuleContextSensitiveTest {
 
 	@Autowired
 	private CoreMetadataInstaller installer;
 
 	/**
 	 * @see CoreMetadataInstaller#encounterType(String, String, String)
 	 */
 	@Test
 	public void encounterType() throws Exception {
 		// Check creating new
 		installer.encounterType("Test Encounter", "Testing", "obj1-uuid");
 
 		EncounterType created = Context.getEncounterService().getEncounterTypeByUuid("obj1-uuid");
 		Assert.assertThat(created.getName(), is("Test Encounter"));
 		Assert.assertThat(created.getDescription(), is("Testing"));
 
 		Context.flushSession();
 		Context.clearSession();
 
 		// Check updating existing
 		installer.encounterType("New name", "New desc", "obj1-uuid");
 
 		EncounterType updated = Context.getEncounterService().getEncounterTypeByUuid("obj1-uuid");
 		Assert.assertThat(updated.getName(), is("New name"));
 		Assert.assertThat(updated.getDescription(), is("New desc"));
 	}
 
 	/**
 	 * @see CoreMetadataInstaller#form(String, String, String, String, String)
 	 */
 	@Test
 	public void form() throws Exception {
 		// Check creating new
 		installer.encounterType("Test Encounter", "Testing", "enc-type1-uuid");
 		installer.form("Test Form #1", "Testing", "enc-type1-uuid", "1.0", "form-uuid");
 
 		Form created = Context.getFormService().getFormByUuid("form-uuid");
 		Assert.assertThat(created.getName(), is("Test Form #1"));
 		Assert.assertThat(created.getDescription(), is("Testing"));
 		Assert.assertThat(created.getEncounterType(), is(Context.getEncounterService().getEncounterTypeByUuid("enc-type1-uuid")));
 		Assert.assertThat(created.getVersion(), is("1.0"));
 
 		Context.flushSession();
 		Context.clearSession();
 
 		// Check updating existing
 		installer.encounterType("Other Encounter", "Testing", "enc-type2-uuid");
 		installer.form("New name", "New desc", "enc-type2-uuid", "2.0", "form-uuid");
 
 		Form updated = Context.getFormService().getFormByUuid("form-uuid");
 		Assert.assertThat(updated.getName(), is("New name"));
 		Assert.assertThat(updated.getDescription(), is("New desc"));
 		Assert.assertThat(updated.getEncounterType(), is(Context.getEncounterService().getEncounterTypeByUuid("enc-type2-uuid")));
 		Assert.assertThat(updated.getVersion(), is("2.0"));
 	}
 
 	/**
 	 * @see CoreMetadataInstaller#globalProperty(String, String, Class, String, Object, String)
 	 */
 	@Test
 	public void globalProperty() throws Exception {
 		// Check creating new
 		installer.globalProperty("test.property", "Testing", null, null, "Value", "gp1-uuid");
 
 		GlobalProperty created = Context.getAdministrationService().getGlobalPropertyObject("test.property");
 		Assert.assertThat(created.getDescription(), is("Testing"));
 		Assert.assertThat(created.getDatatypeClassname(), is(nullValue()));
 		Assert.assertThat(created.getValue(), is((Object) "Value"));
 
 		Context.flushSession();
 		Context.clearSession();
 
 		// Check updating existing
 		installer.globalProperty("test.property", "New desc", null, null, "New value", "gp1-uuid");
 
 		GlobalProperty updated = Context.getAdministrationService().getGlobalPropertyObject("test.property");
 		Assert.assertThat(updated.getDescription(), is("New desc"));
 		Assert.assertThat(updated.getDatatypeClassname(), is(nullValue()));
 		Assert.assertThat(updated.getValue(), is((Object) "New value"));
 
 		Context.flushSession();
 		Context.clearSession();
 
 		// Check updating existing by property name
 		installer.globalProperty("test.property", "Diff desc", null, null, "Diff value", "gp2-uuid");
 
 		updated = Context.getAdministrationService().getGlobalPropertyObject("test.property");
 		Assert.assertThat(updated.getDescription(), is("Diff desc"));
 		Assert.assertThat(updated.getDatatypeClassname(), is(nullValue()));
 		Assert.assertThat(updated.getValue(), is((Object) "Diff value"));
 		Assert.assertThat(updated.getUuid(), is("gp2-uuid"));
 
 		// Check with custom data type and null value
 		GlobalProperty custom = installer.globalProperty("test.property2", "Testing", TestingDatatype.class, "config", null, "gp3-uuid");
 		Assert.assertThat(custom.getDatatypeClassname(), is(TestingDatatype.class.getName()));
 		Assert.assertThat(custom.getValue(), is(nullValue()));
 
 		// Check with custom data type and non-null value
 		EncounterType encType = installer.encounterType("Test Encounter", "Testing", "enc-type-uuid");
 		custom = installer.globalProperty("test.property2", "Testing", TestingDatatype.class, "config", encType, "gp3-uuid");
 		Assert.assertThat(custom.getDatatypeClassname(), is(TestingDatatype.class.getName()));
 		Assert.assertThat(custom.getValue(), is((Object) encType));
 	}
 
 	/**
 	 * @see CoreMetadataInstaller#locationAttributeType(String, String, Class, String, int, int, String)
 	 */
 	@Test
 	public void locationAttributeType() throws Exception {
 		// Check creating new
 		installer.locationAttributeType("Test Type", "Testing", TestingDatatype.class, "config1", 0, 1, "obj1-uuid");
 
 		LocationAttributeType type = Context.getLocationService().getLocationAttributeTypeByUuid("obj1-uuid");
 		Assert.assertThat(type.getName(), is("Test Type"));
 		Assert.assertThat(type.getDescription(), is("Testing"));
 		Assert.assertThat(type.getDatatypeClassname(), is(TestingDatatype.class.getName()));
 		Assert.assertThat(type.getDatatypeConfig(), is("config1"));
 		Assert.assertThat(type.getMinOccurs(), is(0));
 		Assert.assertThat(type.getMaxOccurs(), is(1));
 
 		Context.flushSession();
 		Context.clearSession();
 
 		// Check updating existing
 		installer.locationAttributeType("New name", "New desc", TestingDatatype.class, "config2", 1, 2, "obj1-uuid");
 
 		type = Context.getLocationService().getLocationAttributeTypeByUuid("obj1-uuid");
 		Assert.assertThat(type.getName(), is("New name"));
 		Assert.assertThat(type.getDescription(), is("New desc"));
 		Assert.assertThat(type.getDatatypeClassname(), is(TestingDatatype.class.getName()));
 		Assert.assertThat(type.getDatatypeConfig(), is("config2"));
 		Assert.assertThat(type.getMinOccurs(), is(1));
 		Assert.assertThat(type.getMaxOccurs(), is(2));
 	}
 
 	/**
 	 * @see CoreMetadataInstaller#patientIdentifierType(String, String, String, String, Class, org.openmrs.PatientIdentifierType.LocationBehavior, boolean, String)
 	 */
 	@Test
 	public void patientIdentifierType() throws Exception {
 		// Check creating new
 		installer.patientIdentifierType("Test Type", "Testing", "\\d+", "Format desc", TestingIdentifierValidator.class,
 				PatientIdentifierType.LocationBehavior.NOT_USED, false, "obj1-uuid");
 
 		PatientIdentifierType type = Context.getPatientService().getPatientIdentifierTypeByUuid("obj1-uuid");
 		Assert.assertThat(type.getName(), is("Test Type"));
 		Assert.assertThat(type.getDescription(), is("Testing"));
 		Assert.assertThat(type.getFormat(), is("\\d+"));
 		Assert.assertThat(type.getFormatDescription(), is("Format desc"));
 		Assert.assertThat(type.getValidator(), is(TestingIdentifierValidator.class.getName()));
 		Assert.assertThat(type.getLocationBehavior(), is(PatientIdentifierType.LocationBehavior.NOT_USED));
 		Assert.assertThat(type.getRequired(), is(false));
 
 		Context.flushSession();
 		Context.clearSession();
 
 		// Check updating existing
		installer.patientIdentifierType("New name", "New desc", "\\d*", "New format desc", TestingIdentifierValidator.class,
 				PatientIdentifierType.LocationBehavior.REQUIRED, true, "obj1-uuid");
 
 		type = Context.getPatientService().getPatientIdentifierTypeByUuid("obj1-uuid");
 		Assert.assertThat(type.getName(), is("New name"));
 		Assert.assertThat(type.getDescription(), is("New desc"));
 		Assert.assertThat(type.getFormat(), is("\\d*"));
 		Assert.assertThat(type.getFormatDescription(), is("New format desc"));
		Assert.assertThat(type.getValidator(), is(TestingIdentifierValidator.class.getName()));
 		Assert.assertThat(type.getLocationBehavior(), is(PatientIdentifierType.LocationBehavior.REQUIRED));
 		Assert.assertThat(type.getRequired(), is(true));
 	}
 
 	/**
 	 * @see CoreMetadataInstaller#personAttributeType(String, String, Class, Integer, boolean, double, String)
 	 */
 	@Test
 	public void personAttributeType() throws Exception {
 		// Check creating new
 		installer.personAttributeType("Test Type", "Testing", String.class, null, false, 1.0, "obj1-uuid");
 
 		PersonAttributeType type = Context.getPersonService().getPersonAttributeTypeByUuid("obj1-uuid");
 		Assert.assertThat(type.getName(), is("Test Type"));
 		Assert.assertThat(type.getDescription(), is("Testing"));
 		Assert.assertThat(type.getFormat(), is(String.class.getName()));
 		Assert.assertThat(type.isSearchable(), is(false));
 		Assert.assertThat(type.getSortWeight(), is(1.0));
 
 		Context.flushSession();
 		Context.clearSession();
 
 		// Check updating existing
 		installer.personAttributeType("New name", "New desc", String.class, null, true, 2.0, "obj1-uuid");
 
 		type = Context.getPersonService().getPersonAttributeTypeByUuid("obj1-uuid");
 		Assert.assertThat(type.getName(), is("New name"));
 		Assert.assertThat(type.getDescription(), is("New desc"));
 		Assert.assertThat(type.getFormat(), is(String.class.getName()));
 		Assert.assertThat(type.isSearchable(), is(true));
 		Assert.assertThat(type.getSortWeight(), is(2.0));
 	}
 
 	/**
 	 * @see CoreMetadataInstaller#program(String, String, String, String)
 	 */
 	@Test
 	public void program() throws Exception {
 		// Existing concepts in test data
 		final String HIV_PROGRAM_UUID = "0a9afe04-088b-44ca-9291-0a8c3b5c96fa";
 		final String MALARIA_PROGRAM_UUID = "f923524a-b90c-4870-a948-4125638606fd";
 
 		// Check creating new
 		installer.program("Test Program", "Testing", HIV_PROGRAM_UUID, "obj1-uuid");
 
 		Program created = Context.getProgramWorkflowService().getProgramByUuid("obj1-uuid");
 		Assert.assertThat(created.getName(), is("Test Program"));
 		Assert.assertThat(created.getDescription(), is("Testing"));
 		Assert.assertThat(created.getConcept(), is(Context.getConceptService().getConceptByUuid(HIV_PROGRAM_UUID)));
 
 		Context.flushSession();
 		Context.clearSession();
 
 		// Check updating existing
 		installer.program("New name", "New desc", MALARIA_PROGRAM_UUID, "obj1-uuid");
 
 		Program updated = Context.getProgramWorkflowService().getProgramByUuid("obj1-uuid");
 		Assert.assertThat(updated.getName(), is("New name"));
 		Assert.assertThat(updated.getDescription(), is("New desc"));
 		Assert.assertThat(updated.getConcept(), is(Context.getConceptService().getConceptByUuid(MALARIA_PROGRAM_UUID)));
 
 		Context.flushSession();
 		Context.clearSession();
 
 		// Check update existing when name conflicts
 		installer.program("New name", "Diff desc", MALARIA_PROGRAM_UUID, "obj2-uuid");
 		updated = Context.getProgramWorkflowService().getProgramByUuid("obj2-uuid");
 		Assert.assertThat(updated.getName(), is("New name"));
 		Assert.assertThat(updated.getDescription(), is("Diff desc"));
 
 		Program old = Context.getProgramWorkflowService().getProgramByUuid("obj1-uuid");
 		Assert.assertThat(old, is(nullValue()));
 	}
 
 	/**
 	 * @see CoreMetadataInstaller#visitType(String, String, String)
 	 */
 	@Test
 	public void visitType() throws Exception {
 		// Check creating new
 		installer.visitType("Test Visit", "Testing", "obj1-uuid");
 
 		VisitType type = Context.getVisitService().getVisitTypeByUuid("obj1-uuid");
 		Assert.assertThat(type.getName(), is("Test Visit"));
 		Assert.assertThat(type.getDescription(), is("Testing"));
 
 		Context.flushSession();
 		Context.clearSession();
 
 		// Check updating existing
 		installer.visitType("New name", "New desc", "obj1-uuid");
 
 		type = Context.getVisitService().getVisitTypeByUuid("obj1-uuid");
 		Assert.assertThat(type.getName(), is("New name"));
 		Assert.assertThat(type.getDescription(), is("New desc"));
 	}
 
 	/**
 	 * @see CoreMetadataInstaller#visitAttributeType(String, String, Class, String, int, int, String)
 	 */
 	@Test
 	public void visitAttributeType() throws Exception {
 		// Check creating new
 		installer.visitAttributeType("Test Type", "Testing", TestingDatatype.class, "config1", 0, 1, "obj1-uuid");
 
 		VisitAttributeType type = Context.getVisitService().getVisitAttributeTypeByUuid("obj1-uuid");
 		Assert.assertThat(type.getName(), is("Test Type"));
 		Assert.assertThat(type.getDescription(), is("Testing"));
 		Assert.assertThat(type.getDatatypeClassname(), is(TestingDatatype.class.getName()));
 		Assert.assertThat(type.getDatatypeConfig(), is("config1"));
 		Assert.assertThat(type.getMinOccurs(), is(0));
 		Assert.assertThat(type.getMaxOccurs(), is(1));
 
 		Context.flushSession();
 		Context.clearSession();
 
 		// Check updating existing
 		installer.visitAttributeType("New name", "New desc", TestingDatatype.class, "config2", 1, 2, "obj1-uuid");
 
 		type = Context.getVisitService().getVisitAttributeTypeByUuid("obj1-uuid");
 		Assert.assertThat(type.getName(), is("New name"));
 		Assert.assertThat(type.getDescription(), is("New desc"));
 		Assert.assertThat(type.getDatatypeClassname(), is(TestingDatatype.class.getName()));
 		Assert.assertThat(type.getDatatypeConfig(), is("config2"));
 		Assert.assertThat(type.getMinOccurs(), is(1));
 		Assert.assertThat(type.getMaxOccurs(), is(2));
 	}
 
 	/**
 	 * Custom data type class for testing based on encounter types
 	 */
 	public static class TestingDatatype extends SerializingCustomDatatype<EncounterType> {
 
 		@Override
 		public String serialize(EncounterType typedValue) {
 			return typedValue != null ? String.valueOf(typedValue.getId()) : "";
 		}
 
 		@Override
 		public EncounterType deserialize(String serializedValue) {
 			return StringUtils.isNotEmpty(serializedValue) ? Context.getEncounterService().getEncounterType(Integer.valueOf(serializedValue)) : null;
 		}
 	}
 
 	/**
 	 * Custom identifier validator for testing
 	 */
 	public static class TestingIdentifierValidator implements IdentifierValidator {
 
 		@Override
 		public String getName() {
 			return "Test validator";
 		}
 
 		@Override
 		public boolean isValid(String identifier) throws UnallowedIdentifierException {
 			return true;
 		}
 
 		@Override
 		public String getValidIdentifier(String undecoratedIdentifier) throws UnallowedIdentifierException {
 			return null;
 		}
 
 		@Override
 		public String getAllowedCharacters() {
 			return null;
 		}
 	}
 }
