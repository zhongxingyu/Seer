 package com.fastbiz.core.bootstrap;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import com.fastbiz.core.bootstrap.BootstrapConfiguration.ServiceInformation;
 import com.fastbiz.core.bootstrap.service.BootstrapService;
 import com.fastbiz.core.bootstrap.service.BootstrapServiceEvent;
 import com.fastbiz.core.bootstrap.service.BootstrapServiceException;
 import com.fastbiz.core.bootstrap.service.BootstrapServiceWrapper;
 import com.fastbiz.core.bootstrap.service.ServiceEventListener;
 import com.fastbiz.core.bootstrap.service.ServiceEventListenerAdapter;
 import com.fastbiz.core.bootstrap.service.BootstrapServiceEvent.Level;
 
 public class BootstrapController{
 
     private static final Logger    LOG      = LoggerFactory.getLogger(BootstrapController.class);
 
     private volatile boolean       stop     = false;
 
     private List<BootstrapService> services = new ArrayList<BootstrapService>();
 
     public BootstrapController() {}
 
     private void logBrand(){
         StringBuffer buffer = new StringBuffer();
         buffer.append(System.getProperty("line.separator"));
         buffer.append("Product Code:" + Brand.getProductCode());
         buffer.append(System.getProperty("line.separator"));
         buffer.append("Version:" + Brand.getVersion());
         buffer.append(System.getProperty("line.separator"));
         buffer.append("Company:" + Brand.getCompany());
         LOG.info(buffer.toString());
     }
 
     public void init(){
         logBrand();
         setShutdownHook();
         BootstrapConfiguration bc = new BootstrapConfiguration(EnvironmentConfigration.getBootstrapConfigFilePath());
         List<ServiceInformation> services = bc.getBootstrapServices();
         for (ServiceInformation service : services) {
             Class<? extends BootstrapService> serviceClass = service.getServiceClass();
             BootstrapService instance;
             BootstrapService wrapper;
             try {
                 instance = serviceClass.newInstance();
                 wrapper = new BootstrapServiceWrapper(instance);
                 wrapper.addServiceEventListener(new BootstrapListener());
                 List<Class<? extends ServiceEventListener>> listeners = service.getServiceEventListenerClasses();
                 for (Class<? extends ServiceEventListener> c : listeners) {
                     try {
                         if (c == null) {
                             continue;
                         }
                         ServiceEventListener ls = c.newInstance();
                         wrapper.addServiceEventListener(ls);
                         String format = "Created LifecycleListener instance of type {} for bootstrap service {}";
                         LOG.info(format, c.getName(), service.getServiceClass());
                     } catch (Throwable ex) {
                         String format = "Cannot create LifecycleListener instance of type %s for bootstrap service %s";
                         String msg = String.format(format, c.getName(), service.getServiceClass());
                         throw new BootstrapServiceException(serviceClass.getName(), ex, msg);
                     }
                 }
                 this.services.add(wrapper);
             } catch (Throwable ex) {
                 String format = "Error when instantiating bootstrap service %s";
                 throw new BootstrapServiceException(serviceClass.getName(), ex, format, service.getServiceClass());
             }
             wrapper.init(Application.getApplication());
         }
     }
 
     public void start(){
         for (BootstrapService service : services) {
             service.start(Application.getApplication());
         }
         for (BootstrapService service : services) {
             Application.getApplication().addBootstrapService(service);
         }
     }
 
     public void shutdown(){
         this.stop = true;
         Collections.reverse(services);
         for (BootstrapService service : services) {
             try {
                 LOG.info("Shutdowning bootstrap service {}", service);
                 service.stop(Application.getApplication());
             } catch (Throwable e) {
                 LOG.error("Failed shutdown service {} ", e);
             }
         }
     }
 
     private void setShutdownHook(){
         Thread hook = new Thread(){
 
             public void run(){
                 setName(Brand.getProductCode() + "_Shutdown_Hook");
                 LOG.info("Start shutdown hook......");
                 shutdown();
                 try {
                     Thread.sleep(1000);
                 } catch (Exception e) {
                     LOG.error("", e);
                 }
             }
         };
         hook.setDaemon(true);
         Runtime.getRuntime().addShutdownHook(hook);
     }
 
     public void join() throws InterruptedException{
         synchronized (this) {
             while (!stop) {
                 this.wait();
             }
         }
     }
 
     class BootstrapListener extends ServiceEventListenerAdapter{
 
         private final Logger LOG = LoggerFactory.getLogger(BootstrapListener.class);
 
         private boolean      stopOnError;
 
         public BootstrapListener(boolean stopOnError) {
             this.stopOnError = stopOnError;
         }
 
         public BootstrapListener() {
             this(true);
         }
 
         protected void onInit(BootstrapServiceEvent event){
             if (event.getLevel() == Level.error) {
                 Object data = event.getData();
                 BootstrapService service = (BootstrapService) event.getSource();
                 if (data != null) {
                     if (data instanceof Throwable) {
                         String message = String.format("Failed initializing bootstrap service %s", service.getClass());
                         LOG.error(message, (Throwable) data);
                     } else {
                         String fmt = "Failed initializing bootstrap service {} with message {}";
                         LOG.error(fmt, service.getClass(), data);
                     }
                 }
                 if (stopOnError) {
                     shutdown();
                 }
             }
         }
 
         protected void onStart(BootstrapServiceEvent event){
             if (event.getLevel() == Level.error) {
                 Object data = event.getData();
                 BootstrapService service = (BootstrapService) event.getSource();
                 if (data != null) {
                     if (data instanceof Throwable) {
                         String message = String.format("Failed starting bootstrap service %s ", service.getClass());
                         LOG.error(message, (Throwable) data);
                     } else {
                         String fmt = "Failed starting bootstrap service {} with message {}";
                         LOG.error(fmt, service.getClass(), data);
                     }
                 }
                 if (stopOnError) {
                     shutdown();
                 }
             } else {
                 Application.getApplication().addBootstrapService((BootstrapService) event.getSource());
             }
         }
 
         protected void onStop(BootstrapServiceEvent event){
             Application.getApplication().removeBootstrapService((BootstrapService) event.getSource());
             if (event.getLevel() == Level.error) {
                 Object data = event.getData();
                 BootstrapService service = (BootstrapService) event.getSource();
                 if (data != null) {
                     if (data instanceof Throwable) {
                         String message = String.format("Failed stopping bootstrap service %s ", service.getClass());
                         LOG.error(message, (Throwable) data);
                     } else {
                         String fmt = "Failed stopping bootstrap service {} with message {}";
                         LOG.error(fmt, service.getClass(), data);
                     }
                 }
             }
         }
     }
 }
