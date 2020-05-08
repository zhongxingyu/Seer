 package com.timgroup.tucker.info.status;
 
 import static java.lang.String.format;
 import static java.util.Collections.unmodifiableList;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.timgroup.tucker.info.Component;
 import com.timgroup.tucker.info.component.VersionComponent;
 
 public class AsyncStatusPageGenerator implements ApplicationReportGenerator {
 
     private final StatusPageGenerator statusPageGenerator;
     private final List<AsyncComponent> components;
 
     public AsyncStatusPageGenerator(String applicationId, VersionComponent versionComponent, List<AsyncComponent> components) {
         this.components = unmodifiableList(new ArrayList<AsyncComponent>(components));
         this.statusPageGenerator = new StatusPageGenerator(applicationId, versionComponent);
         for (AsyncComponent component: components) {
             statusPageGenerator.addComponent(component);
         }
     }
 
     @Override
     public Component getVersionComponent() {
         return statusPageGenerator.getVersionComponent();
     }
 
     @Override
     public StatusPage getApplicationReport() {
         return statusPageGenerator.getApplicationReport();
     }
     
     public void start() {
         for (AsyncComponent asyncComponent: components) {
             asyncComponent.start();
         }
     }
     
     public void stop() throws InterruptedException {
         List<String> failedToStop = new ArrayList<String>();
        InterruptedException lastThrown = null;
         for (AsyncComponent asyncComponent: components) {
             try {
                 asyncComponent.stop();
             } catch (InterruptedException e) {
                 failedToStop.add(asyncComponent.getId());
                lastThrown = e;
             }
         }
         if (!failedToStop.isEmpty()) {
            String message = format("Failed to stop components: %s. Last failure: %s", failedToStop, lastThrown.getMessage());
             InterruptedException consolidatedException = new InterruptedException(message);
            consolidatedException.initCause(lastThrown);
             throw consolidatedException;
         }
     }
 
 }
