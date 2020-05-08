 package org.kevoree.monitoring.strategies.adaptation;
 
 import org.kevoree.*;
 import org.kevoree.api.service.core.handler.KevoreeModelHandlerService;
 import org.kevoree.api.service.core.handler.KevoreeModelUpdateException;
 import org.kevoree.api.service.core.handler.UUIDModel;
 import org.kevoree.cloner.ModelCloner;
 import org.kevoree.microsandbox.api.communication.MonitoringReporterFactory;
 import org.kevoree.monitoring.sla.FaultyComponent;
 
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: inti
  * Date: 7/3/13
  * Time: 9:20 AM
  * To change this template use File | Settings | File Templates.
  */
 public class KillThemAll extends BasicAdaptation {
 
     public KillThemAll(KevoreeModelHandlerService service) {
         super(service);
     }
 
     @Override
     public boolean adapt(String nodeName, List<FaultyComponent> faultyComponents) {
         ModelCloner cloner = new ModelCloner();
         UUIDModel uuidModel = modelService.getLastUUIDModel();
         try {
             ContainerRoot clonedModel = cloner.clone(uuidModel.getModel());
             ContainerNode node = clonedModel.findNodesByID(nodeName);
             for (FaultyComponent c : faultyComponents) {
                 MonitoringReporterFactory.reporter().adaptation(getActionName(), c.getComponentPath());
 
                 ComponentInstance cc = clonedModel.findByPath(c.getComponentPath(), ComponentInstance.class);
                 node.removeComponents(cc);
                 for (Port p : cc.getProvided()) {
                     for (MBinding b : p.getBindings()) {
                         clonedModel.removeMBindings(b);
                     }
                 }
                 for (Port p : cc.getRequired()) {
                     for (MBinding b : p.getBindings()) {
                         clonedModel.removeMBindings(b);
                     }
                 }
             }
             modelService.atomicCompareAndSwapModel(uuidModel, clonedModel);
             return true;
         } catch (KevoreeModelUpdateException e) {
             return false;
         }
 
     }
 
     private String getActionName() {
         return "remove";
     }
 }
