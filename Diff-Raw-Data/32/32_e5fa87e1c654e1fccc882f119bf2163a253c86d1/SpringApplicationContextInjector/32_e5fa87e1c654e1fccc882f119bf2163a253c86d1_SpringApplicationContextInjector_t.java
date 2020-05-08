 package com.philemonworks.selfdiagnose.check.vendor;
 
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 
 import com.philemonworks.selfdiagnose.CustomDiagnosticTask;
 import com.philemonworks.selfdiagnose.DiagnosticTask;
 import com.philemonworks.selfdiagnose.SelfDiagnose;
 
 public class SpringApplicationContextInjector {
 
     public static void inject(ApplicationContext appCtx) {
         // Iterate through all registered tasks
         for (DiagnosticTask each : SelfDiagnose.getTasks()) {

	        // First, custom tasks are a special case: we extract its task (this may be null), and then inject the Spring bean from the reference (if available:).
	        // As for a custom task either its task or its reference is set (never both), 'each' will be null if we injected a Spring bean (for which the next step is not needed).

	        if (each instanceof CustomDiagnosticTask) {
		        CustomDiagnosticTask custom = (CustomDiagnosticTask) each;
		        each = custom.getTask();
		        injectTask(custom, appCtx);
	        }

	        // Then, if the task is (not null and) ApplicationContextAware, we inject the Spring context.

             if (each instanceof ApplicationContextAware) {
                 ApplicationContextAware eachAware = (ApplicationContextAware) each;
                 eachAware.setApplicationContext(appCtx);
             }
         }
     }

	/**
	 * If {@code task} is a {@link CustomDiagnosticTask} with a reference, inject the task from the application context.
	 *
	 * @param customTask a {@code CustomDiagnosticTask}
	 */
	private static void injectTask(CustomDiagnosticTask customTask, ApplicationContext context) {

		String beanReference = customTask.getReference();
		if (beanReference != null) {
			customTask.setTask(context.getBean(customTask.getReference(), DiagnosticTask.class));
		}
	}
 }
