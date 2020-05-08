 package org.motechproject.care.reporting.processors;
 
 import org.motechproject.commcare.domain.CommcareForm;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 @Component
 public class FormProcessor {
 
     private static final Logger logger = LoggerFactory.getLogger("commcare-reporting-mapper");
 
     private MotherFormProcessor motherFormProcessor;
     private ChildFormProcessor childFormProcessor;
 
     private static final String FORM_NAME_ATTRIBUTE = "name";
     private static final String FORM_XMLNS_ATTRIBUTE = "xmlns";
 
     @Autowired
     public FormProcessor(MotherFormProcessor motherFormProcessor, ChildFormProcessor childFormProcessor) {
         this.motherFormProcessor = motherFormProcessor;
         this.childFormProcessor = childFormProcessor;
     }
 
     public void process(CommcareForm commcareForm) {
         String formName = commcareForm.getForm().getAttributes().get(FORM_NAME_ATTRIBUTE);
         String xmlns = commcareForm.getForm().getAttributes().get(FORM_XMLNS_ATTRIBUTE);
        logger.info(String.format("Received form. id: %s, type: %s; xmlns: %s;", commcareForm.getId(), formName, xmlns));
 
         motherFormProcessor.parseMotherForm(commcareForm);
         childFormProcessor.parseChildForms(commcareForm);
     }
 }
