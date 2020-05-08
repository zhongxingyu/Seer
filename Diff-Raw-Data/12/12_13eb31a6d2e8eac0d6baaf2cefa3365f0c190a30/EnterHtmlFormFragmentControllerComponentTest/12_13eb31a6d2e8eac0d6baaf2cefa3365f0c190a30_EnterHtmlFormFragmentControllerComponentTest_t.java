 /*
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
 
 package org.openmrs.module.emr.fragment.controller.htmlform;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.openmrs.Encounter;
 import org.openmrs.Obs;
 import org.openmrs.Patient;
 import org.openmrs.Provider;
 import org.openmrs.api.EncounterService;
 import org.openmrs.api.FormService;
 import org.openmrs.api.PatientService;
 import org.openmrs.api.PersonService;
 import org.openmrs.api.ProviderService;
 import org.openmrs.module.emr.EmrContext;
import org.openmrs.module.emr.TestUiUtils;
 import org.openmrs.module.htmlformentry.FormEntrySession;
 import org.openmrs.module.htmlformentry.HtmlForm;
 import org.openmrs.module.htmlformentry.HtmlFormEntryService;
 import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
 import org.openmrs.ui.framework.fragment.FragmentConfiguration;
 import org.openmrs.ui.framework.fragment.FragmentModel;
 import org.openmrs.ui.framework.resource.ResourceFactory;
 import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.mock.web.MockHttpServletRequest;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import static org.hamcrest.Matchers.containsString;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.core.IsCollectionContaining.hasItem;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 /**
  *
  */
 public class EnterHtmlFormFragmentControllerComponentTest extends BaseModuleWebContextSensitiveTest {
 
     @Qualifier("personService")
     @Autowired
     PersonService personService;
 
     @Qualifier("patientService")
     @Autowired
     PatientService patientService;
 
     @Qualifier("providerService")
     @Autowired
     ProviderService providerService;
 
     @Qualifier("htmlFormEntryService")
     @Autowired
     HtmlFormEntryService htmlFormEntryService;
 
     @Qualifier("formService")
     @Autowired
     FormService formService;
 
     @Qualifier("encounterService")
     @Autowired
     EncounterService encounterService;
 
     ResourceFactory resourceFactory;
 
    UiUtils ui;

     EmrContext emrContext;
 
     public static final String FORM_DEFINITION = "<htmlform formUuid=\"form-uuid\" formName=\"Form Name\" formVersion=\"1.0\">Weight:<obs id=\"weight\" conceptId=\"5089\"/> <encounterDate/> <encounterLocation/> <encounterProvider/></htmlform>";
     private EnterHtmlFormFragmentController controller;
 
 
     @Before
     public void before() throws Exception {
         resourceFactory = mock(ResourceFactory.class);
         when(resourceFactory.getResourceAsString("emr", "htmlforms/vitals.xml")).thenReturn(FORM_DEFINITION);
 
         emrContext = mock(EmrContext.class);
 
        ui = new TestUiUtils();

         controller = new EnterHtmlFormFragmentController();
     }
 
     @Test
     public void testDefiningAnHtmlFormInUiResource() throws Exception {
         FragmentModel model = new FragmentModel();
         Patient patient = new Patient();
         String resourcePath = "emr:htmlforms/vitals.xml";
 
         FragmentConfiguration config = new FragmentConfiguration();
         config.put("patient", patient);
         config.put("definitionResource", resourcePath);
 
         controller.controller(config, emrContext, htmlFormEntryService, formService, resourceFactory, patient, null, null, null, resourcePath, null, null, null, true, model, null);
 
         FormEntrySession command = (FormEntrySession) model.getAttribute("command");
         String html = command.getHtmlToDisplay();
         assertThat(html, containsString("name=\"w2\""));
         assertThat(html, containsString("onBlur=\"checkNumber("));
     }
 
     @Test
     public void testSubmittingHtmlFormDefinedInUiResource() throws Exception {
         // first, ensure the form is created and persisted, by calling the controller display method
         testDefiningAnHtmlFormInUiResource();
         HtmlForm hf = htmlFormEntryService.getHtmlFormByForm(formService.getFormByUuid("form-uuid"));
 
         // make "Hippocrates" a provider
         Provider provider = new Provider();
         provider.setPerson(personService.getPerson(502));
         providerService.saveProvider(provider);
 
         Patient patient = patientService.getPatient(8);
         assertThat(encounterService.getEncountersByPatient(patient).size(), is(0));
 
         String dateString = "2012-12-17";
         Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
 
         MockHttpServletRequest request = new MockHttpServletRequest();
         request.addParameter("w2", "70"); // weight in kg
         request.addParameter("w3", dateString); // date
         request.addParameter("w5", "2"); // location = Xanadu
         request.addParameter("w7", "502"); // provider = Hippocrates
 
        SimpleObject result = controller.submit(patient, hf, null, null, null, encounterService, resourceFactory, ui, request);
         assertThat((Boolean) result.get("success"), is(Boolean.TRUE));
         assertThat(encounterService.getEncountersByPatient(patient).size(), is(1));
         Encounter created = encounterService.getEncountersByPatient(patient).get(0);
         assertThat(created.getEncounterDatetime(), is(date));
         assertThat(created.getLocation().getId(), is(2));
         assertThat(created.getProvidersByRoles().values().iterator().next(), hasItem(provider));
         assertThat(created.getAllObs().size(), is(1));
         Obs weightObs = created.getAllObs().iterator().next();
         assertThat(weightObs.getConcept().getId(), is(5089));
         assertThat(weightObs.getValueNumeric(), is(Double.valueOf(70d)));
     }
 }
