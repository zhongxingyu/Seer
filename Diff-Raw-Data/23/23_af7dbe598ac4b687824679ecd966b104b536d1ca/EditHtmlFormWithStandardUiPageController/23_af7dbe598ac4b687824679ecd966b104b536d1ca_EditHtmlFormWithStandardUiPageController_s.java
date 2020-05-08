 package org.openmrs.module.emr.page.controller.htmlform;
 
 import org.openmrs.Encounter;
 import org.openmrs.Patient;
 import org.openmrs.module.htmlformentry.HtmlForm;
 import org.openmrs.module.htmlformentry.HtmlFormEntryService;
 import org.openmrs.ui.framework.UiUtils;
 import org.openmrs.ui.framework.annotation.SpringBean;
 import org.openmrs.ui.framework.page.PageModel;
 import org.springframework.web.bind.annotation.RequestParam;
 
 /**
  *
  */
 public class EditHtmlFormWithStandardUiPageController {
 
     public void get(@RequestParam("encounterId") Encounter encounter,
                     @RequestParam("patientId") Patient patient, // explicitly require this instead of inferring from encounter because this sets up the global context
                     @RequestParam(value = "returnUrl", required = false) String returnUrl,
                     @RequestParam(value = "breadcrumbOverride", required = false) String breadcrumbOverride,
                     @SpringBean("htmlFormEntryService") HtmlFormEntryService htmlFormEntryService,
                     UiUtils ui,
                     PageModel pageModel) {
 
        if (returnUrl == null) {
            returnUrl = ui.pageLink("*", "home");
         }
 
         if (!encounter.getPatient().equals(patient)) {
             throw new IllegalArgumentException("encounter.patient != patient");
         }
 
         if (encounter.getForm() == null) {
             throw new IllegalArgumentException("encounter.form is null");
         }
         HtmlForm htmlForm = htmlFormEntryService.getHtmlFormByForm(encounter.getForm());
         if (htmlForm == null) {
             throw new IllegalArgumentException("encounter.form is not an HTML Form: " + encounter.getForm());
         }
 
         pageModel.addAttribute("encounter", encounter);
         pageModel.addAttribute("patient", patient);
         pageModel.addAttribute("htmlForm", htmlForm);
         pageModel.addAttribute("returnUrl", returnUrl);
         pageModel.addAttribute("breadcrumbOverride", breadcrumbOverride);
     }
 
 }
