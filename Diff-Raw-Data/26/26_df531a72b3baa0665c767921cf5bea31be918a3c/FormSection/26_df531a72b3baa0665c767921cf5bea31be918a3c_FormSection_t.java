 package com.example.ui.components.form;
 
 import org.apache.tapestry5.ComponentAction;
 import org.apache.tapestry5.ComponentResources;
 import org.apache.tapestry5.EventConstants;
 import org.apache.tapestry5.Field;
 import org.apache.tapestry5.PersistenceConstants;
 import org.apache.tapestry5.TrackableComponentEventCallback;
 import org.apache.tapestry5.ValidationException;
 import org.apache.tapestry5.ValidationTracker;
 import org.apache.tapestry5.ValidationTrackerImpl;
 import org.apache.tapestry5.ValidationTrackerWrapper;
 import org.apache.tapestry5.annotations.CleanupRender;
 import org.apache.tapestry5.annotations.Environmental;
 import org.apache.tapestry5.annotations.Persist;
 import org.apache.tapestry5.annotations.SetupRender;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.apache.tapestry5.ioc.util.ExceptionUtils;
 import org.apache.tapestry5.services.Environment;
 import org.apache.tapestry5.services.FormSupport;
 
 /**
  * @author Ivan Khalopik
  */
 public class FormSection {
     private static final ComponentAction<FormSection> SETUP_TRACKER = new SetupTracker();
     private static final ComponentAction<FormSection> CLEANUP_TRACKER = new CleanupTracker();
     private static final ComponentAction<FormSection> PROCESS_SUBMISSION = new ProcessSubmission();
 
     @Persist(PersistenceConstants.FLASH)
     private ValidationTracker tracker;
 
    private ValidationTracker wrapper;

     @Inject
     private ComponentResources resources;
 
     @Environmental
     private FormSupport formSupport;
 
     @Environmental
     private TrackableComponentEventCallback eventCallback;
 
     @Inject
     private Environment environment;
 
     @SetupRender
     void storeSetupAction() {
         formSupport.storeAndExecute(this, SETUP_TRACKER);
     }
 
     @CleanupRender
     void storeCleanupAction() {
         formSupport.storeAndExecute(this, CLEANUP_TRACKER);
         formSupport.store(this, PROCESS_SUBMISSION);
     }
 
     private void processSubmission() {
         // defer validation to be executed after all field values are populated
         formSupport.defer(new Runnable() {
             public void run() {
                environment.push(ValidationTracker.class, wrapper);
                 validateSection();
                environment.pop(ValidationTracker.class);
             }
         });
     }
 
     private void setupValidationTracker() {
         // get or create inner validation tracker
         // it will save validation state for fields within this form section
         final ValidationTracker innerTracker = tracker != null ? tracker : new ValidationTrackerImpl();
 
         // get original validation tracker
        final ValidationTracker outerTracker = environment.pop(ValidationTracker.class);
         // add error recording hook to original validation tracker
         // that will save inner validation tracker in session flash attribute
         final ValidationTracker outerTrackerWrapper = new ValidationTrackerWrapper(outerTracker) {
             private boolean saved = false;
 
             private void save() {
                 if (!saved) {
                     tracker = innerTracker;
                     saved = true;
                 }
             }
 
             @Override
             public void recordError(final Field field, final String errorMessage) {
                 super.recordError(field, errorMessage);
 
                 save();
             }
 
             @Override
             public void recordError(final String errorMessage) {
                 super.recordError(errorMessage);
 
                 save();
             }
         };
        // replace original validation tracker with its hooked version
        environment.push(ValidationTracker.class, outerTrackerWrapper);
 
         // create composite validation tracker that will record errors and input values
         // in both inner and original validation trackers
        wrapper = new ValidationTrackerWrapper(innerTracker) {
             @Override
             public void recordError(final Field field, final String errorMessage) {
                 super.recordError(field, errorMessage);
                 outerTrackerWrapper.recordError(field, errorMessage);
             }
 
             @Override
             public void recordError(final String errorMessage) {
                 super.recordError(errorMessage);
                 outerTrackerWrapper.recordError(errorMessage);
             }
 
             @Override
             public void recordInput(final Field field, final String input) {
                 super.recordInput(field, input);
                 outerTrackerWrapper.recordInput(field, input);
             }
         };
 
         // push composite tracker to environment
         // to be accessible by all inner components
         environment.push(ValidationTracker.class, wrapper);
     }
 
     private void cleanupValidationTracker() {
         // pop composite tracker from environment
         environment.pop(ValidationTracker.class);
     }
 
     private void validateSection() {
         try {
             // trigger validate event
             // it will record error if validation exception occurs
             resources.triggerEvent(EventConstants.VALIDATE, null, eventCallback);
         } catch (RuntimeException ex) {
             final ValidationException ve = ExceptionUtils.findCause(ex, ValidationException.class);
             if (ve != null) {
                wrapper.recordError(ve.getMessage());
                 return;
             }
             throw ex;
         }
     }
 
     static class SetupTracker implements ComponentAction<FormSection> {
         public void execute(final FormSection component) {
             component.setupValidationTracker();
         }
 
         @Override
         public String toString() {
             return "FormSection.SetupTracker";
         }
     }
 
     static class CleanupTracker implements ComponentAction<FormSection> {
         public void execute(final FormSection component) {
             component.cleanupValidationTracker();
         }
 
         @Override
         public String toString() {
             return "FormSection.CleanupTracker";
         }
     }
 
     static class ProcessSubmission implements ComponentAction<FormSection> {
         public void execute(final FormSection component) {
             component.processSubmission();
         }
 
         @Override
         public String toString() {
             return "FormSection.ProcessSubmission";
         }
     }
 }
