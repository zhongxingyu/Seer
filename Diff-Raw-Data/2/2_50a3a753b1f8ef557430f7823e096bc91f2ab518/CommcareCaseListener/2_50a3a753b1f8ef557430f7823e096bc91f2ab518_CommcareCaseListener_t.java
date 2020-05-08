 package org.motechproject.care.reporting.listener;
 
 import org.motechproject.care.reporting.enums.CaseType;
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
 
 import static java.lang.String.format;
 
 @Component
 public class CommcareCaseListener {
 
     private static final Logger logger = LoggerFactory.getLogger("commcare-reporting-mapper");
     public static final String CLOSE_ACTION_IDENTIFIER = "CLOSE";
 
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
 
         logger.info(format("Received case. id: %s, case name: %s; action: %s;", caseId, caseName, action));
 
         if (CLOSE_ACTION_IDENTIFIER.equals(action)) {
             processClose(caseEvent);
             return;
         }
 
         processCreateUpdate(caseEvent, caseId);
     }
 
 
 
     private void processCreateUpdate(CaseEvent caseEvent, String caseId) {
         CaseType caseType = CaseType.getType(caseEvent.getCaseType());
 
         if(!caseType.shouldProcess())  {
            logger.info(String.format("Ignoring case type %s with the case id %s", caseType,caseId));
             return;
         }
 
         if (caseType.equals(CaseType.MOTHER)) {
             motherCaseProcessor.process(caseEvent);
             return;
         }
 
         if (caseType.equals(CaseType.CHILD)) {
             childCaseProcessor.process(caseEvent);
             return;
         }
 
         throw new RuntimeException(format("Cannot process case with id %s of type %s", caseId, caseType));
     }
 
     private void processClose(CaseEvent caseEvent) {
         closeCaseProcessor.process(caseEvent);
     }
 }
