 /**
  * 
  */
 package org.openmrs.module.patientregistration.controller.workflow;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.junit.Before;
 import org.openmrs.Location;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.patientregistration.util.PatientRegistrationWebUtil;
 import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
 import org.springframework.mock.web.MockHttpServletRequest;
 
import javax.servlet.http.HttpSession;

 /**
  * @author cospih
  *
  */
 public abstract class BasePatientRegistrationControllerTest extends
 		BaseModuleWebContextSensitiveTest {
 
 	
 	protected final Log log = LogFactory.getLog(getClass());
 	
 	protected static final String PATIENT_REGISTRATION_GLOBALPROPERTY_XML = "org/openmrs/module/patientregistration/include/globalproperty.xml";
 	protected static final String PATIENT_REGISTRATION_PATIENTIDENTIFIERTYPE_XML = "org/openmrs/module/patientregistration/include/patientidentifiertype.xml";	
 	protected static final String PATIENT_REGISTRATION_ENCOUNTERTYPE_XML = "org/openmrs/module/patientregistration/include/encountertype.xml";
 	protected static final String PATIENT_REGISTRATION_LOCATIONS_XML = "org/openmrs/module/patientregistration/include/locations.xml";
    protected static final String PATIENT_REGISTRATION_LOCATION_TAG_XML = "org/openmrs/module/patientregistration/include/locationtag.xml";
    protected static final String PATIENT_REGISTRATION_LOCATION_TAG_MAP_XML = "org/openmrs/module/patientregistration/include/locationtagmap.xml";
 	protected static final String PATIENT_REGISTRATION_ADDRESS_HIERARCHY_XML = "org/openmrs/module/patientregistration/include/addresshierarchy.xml";
 	
 	MockHttpServletRequest request = null;
 	HttpSession session = null;
 
 	@Before
 	public void initTestData() throws Exception{
 		executeDataSet(PATIENT_REGISTRATION_GLOBALPROPERTY_XML);
 		executeDataSet(PATIENT_REGISTRATION_PATIENTIDENTIFIERTYPE_XML);
 		executeDataSet(PATIENT_REGISTRATION_ENCOUNTERTYPE_XML);
 		//overwriting the default openmrs test data set(/api/src/test/reources/org/openmrs/include/standardTestDataset.xml)
 		executeDataSet(PATIENT_REGISTRATION_LOCATIONS_XML);
        executeDataSet(PATIENT_REGISTRATION_LOCATION_TAG_XML);
        executeDataSet(PATIENT_REGISTRATION_LOCATION_TAG_MAP_XML);
 		executeDataSet(PATIENT_REGISTRATION_ADDRESS_HIERARCHY_XML);
 		request = new MockHttpServletRequest();
 		session = request.getSession();
 		String task = "patientRegistration";
 		Location location = Context.getLocationService().getLocation("Lacolline");
 		PatientRegistrationWebUtil.setRegistrationLocation(session, location);
 		PatientRegistrationWebUtil.setRegistrationTask(session, task);
 	}
 }
