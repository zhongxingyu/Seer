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
             if (each instanceof ApplicationContextAware) {
                 ApplicationContextAware eachAware = (ApplicationContextAware) each;
                 eachAware.setApplicationContext(appCtx);
            } else if (each instanceof CustomDiagnosticTask) {
                CustomDiagnosticTask custom = (CustomDiagnosticTask) each;
                // try resolving task by a lookup in the application context
                DiagnosticTask task = (DiagnosticTask) appCtx.getBean(custom.getReference());
                custom.setTask(task);
             }
         }
     }
 }
