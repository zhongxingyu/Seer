 package org.kevoree.microsandbox.samples;
 
 import org.kevoree.*;
 import org.kevoree.annotation.ComponentType;
 import org.kevoree.annotation.Start;
 import org.kevoree.annotation.Stop;
 import org.kevoree.api.service.core.handler.UUIDModel;
import org.kevoree.cloner.DefaultModelCloner;
 import org.kevoree.framework.AbstractComponentType;
 import org.kevoree.library.defaultNodeTypes.context.KevoreeDeployManager;
 import org.kevoree.log.Log;
 
 import java.lang.management.ManagementFactory;
 import java.lang.management.ThreadMXBean;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import static org.kevoree.log.Log.info;
 
 /**
  * Created with IntelliJ IDEA.
  * User: duke
  * Date: 30/05/13
  * Time: 14:35
  */
 @ComponentType
 public class SandboxReasoner extends AbstractComponentType implements Runnable {
 
     private ScheduledExecutorService schduler = null;
     private ThreadMXBean tbean = null;
    private DefaultModelCloner cloner = new DefaultModelCloner();
 
     @Start
     public void start() {
         info("Starting Reasoner {}@{}", getName(), getNodeName());
         schduler = Executors.newScheduledThreadPool(1);
         tbean = ManagementFactory.getThreadMXBean();
         schduler.scheduleAtFixedRate(this, 2000, 5000, TimeUnit.MILLISECONDS);
     }
 
     @Stop
     public void stop() {
         info("Stoping Reasoner {}@{}", getName(), getNodeName());
         schduler.shutdownNow();
         schduler = null;
         tbean = null;
     }
 
     @Override
     public void run() {
         ContainerRoot currentModel = getModelService().getLastModel();
         Log.info("Perf stats =====> ");
         try {
             for (String key : KevoreeDeployManager.instance$.getInternalMap().keySet()) {
                 if (key.contains("_tg")) {  //TODO far better ....
                     ThreadGroup tg = (ThreadGroup) KevoreeDeployManager.instance$.getInternalMap().get(key);
                     Thread[] lists = new Thread[tg.activeCount()];
                     tg.enumerate(lists);
                     long globalWallTime = 0l;
                     for (Thread t : lists) {
                         globalWallTime = globalWallTime + tbean.getThreadCpuTime(t.getId());
                     }
                     String threadGroupKevoreeInstancePath = tg.getName().substring(tg.getName().indexOf("/") + 1);
                     Object foundedInstance = currentModel.findByPath(threadGroupKevoreeInstancePath);
                     if (foundedInstance != null) {
                         //Ok founded, check the contract
                         Instance kevInstance = (Instance) foundedInstance;
                         if (kevInstance.getTypeDefinition().getDictionaryType() != null) {
                             org.kevoree.DictionaryAttribute att = kevInstance.getTypeDefinition().getDictionaryType().findAttributesByID("cpu_wall_time");
                             for (DictionaryValue val : kevInstance.getDictionary().getValues()) {
                                 if (val.getAttribute().equals(att)) {
                                     try {
                                         long contractWallTime = Long.parseLong(val.getValue());
                                         if (globalWallTime >= contractWallTime) {
                                             System.out.println("Contract violated , expected " + contractWallTime + " while found : " + globalWallTime);
                                             //simple reaction , remove the component
 
                                             UUIDModel uuidModel = getModelService().getLastUUIDModel();
                                             ContainerRoot clonedModel = cloner.clone(uuidModel.getModel());
                                             ContainerNode node = clonedModel.findNodesByID(getNodeName());
                                             node.removeComponents(clonedModel.findByPath(threadGroupKevoreeInstancePath, ComponentInstance.class));
                                             getModelService().atomicCompareAndSwapModel(uuidModel,clonedModel);
 
                                         } else {
                                             System.out.println("Contract respected "+threadGroupKevoreeInstancePath+" : "+globalWallTime);
                                         }
                                     } catch (Exception e) {
                                         e.printStackTrace();
                                     }
                                 }
                             }
                         }
                     } else {
                         Log.warn("TG not currently found on the platform " + threadGroupKevoreeInstancePath);
                     }
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         Log.info("<----------------- ");
     }
 }
