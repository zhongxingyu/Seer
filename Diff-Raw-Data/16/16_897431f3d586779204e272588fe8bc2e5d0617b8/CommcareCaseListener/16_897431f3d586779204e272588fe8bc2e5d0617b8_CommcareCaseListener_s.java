 package org.motechproject.care.reporting.listener;
 
 import org.motechproject.care.reporting.domain.dimension.ChildCase;
 import org.motechproject.care.reporting.domain.dimension.MotherCase;
 import org.motechproject.care.reporting.factory.CaseFactory;
 import org.motechproject.care.reporting.processors.ChildCaseProcessor;
 import org.motechproject.care.reporting.processors.CloseCaseProcessor;
 import org.motechproject.care.reporting.processors.MotherCaseProcessor;
 import org.motechproject.commcare.events.CaseEvent;
 import org.motechproject.commcare.events.constants.EventSubjects;
 import org.motechproject.event.MotechEvent;
 import org.motechproject.event.listener.annotations.MotechListener;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 @Component
 public class CommcareCaseListener {
 
     private static final Logger logger = LoggerFactory.getLogger("commcare-reporting-mapper");
 
     private ChildCaseProcessor childCaseProcessor;
     private CloseCaseProcessor closeCaseProcessor;
     private MotherCaseProcessor motherCaseProcessor;
 
     @Autowired
     public CommcareCaseListener(MotherCaseProcessor motherCaseProcessor, ChildCaseProcessor childCaseProcessor, CloseCaseProcessor closeCaseProcessor) {
         this.motherCaseProcessor = motherCaseProcessor;
         this.childCaseProcessor = childCaseProcessor;
         this.closeCaseProcessor = closeCaseProcessor;
     }
 
     @MotechListener(subjects = EventSubjects.CASE_EVENT)
     public void handleEvent(MotechEvent event) {
         CaseEvent caseEvent = new CaseEvent(event);
 
         String caseId = caseEvent.getCaseId();
         String action = caseEvent.getAction();
         String caseName = caseEvent.getCaseName();
 
        logger.info(String.format("Received case. id: %s, case name: %s; action: %s;", caseId, action, caseName));
 
         if ("CLOSE".equals(action)) {
             closeCaseProcessor.process(caseEvent);
         } else {
             Class<?> caseTypeClass = CaseFactory.getCase(caseEvent.getCaseType());
             if (caseTypeClass.equals(MotherCase.class)) {
                 motherCaseProcessor.process(caseEvent);
             } else if (caseTypeClass.equals(ChildCase.class)) {
                 childCaseProcessor.process(caseEvent);
             }
         }
     }
 }
