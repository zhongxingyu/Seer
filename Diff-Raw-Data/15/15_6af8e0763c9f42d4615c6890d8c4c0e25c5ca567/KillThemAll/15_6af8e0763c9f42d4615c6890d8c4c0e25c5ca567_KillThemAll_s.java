 package org.kevoree.monitoring.strategies.adaptation;
 
 import org.kevoree.*;
 import org.kevoree.api.service.core.handler.KevoreeModelHandlerService;
 import org.kevoree.api.service.core.handler.KevoreeModelUpdateException;
 import org.kevoree.api.service.core.handler.UUIDModel;
import org.kevoree.cloner.ModelCloner;
 import org.kevoree.microsandbox.api.communication.MonitoringReporterFactory;
 import org.kevoree.microsandbox.api.event.AdaptationEvent;
 import org.kevoree.monitoring.sla.FaultyComponent;
 
 import java.util.ArrayList;
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
     public List<FaultyComponent> adapt(String nodeName, List<FaultyComponent> faultyComponents) {
         int index = 0;
        ModelCloner cloner = new ModelCloner();
         UUIDModel uuidModel = modelService.getLastUUIDModel();
         try {
             ContainerRoot clonedModel = cloner.clone(uuidModel.getModel());
             ContainerNode node = clonedModel.findNodesByID(nodeName);
             for (FaultyComponent c : faultyComponents) {
                 MonitoringReporterFactory.reporter().trigger(new AdaptationEvent(getActionName(), c.getComponentPath()));/*.adaptation(getActionName(), c.getComponentPath());*/
 
                 ComponentInstance cc = clonedModel.findByPath(c.getComponentPath(), ComponentInstance.class);
                 node.removeComponents(cc);
                 for (Port p : cc.getProvided()) {
                     for (MBinding b : p.getBindings()) {
                         clonedModel.removeMBindings(b);
                         b.getHub().removeBindings(b);
                     }
                 }
                 for (Port p : cc.getRequired()) {
                     for (MBinding b : p.getBindings()) {
                         clonedModel.removeMBindings(b);
                         b.getHub().removeBindings(b);
                     }
                 }
             }
             modelService.atomicCompareAndSwapModel(uuidModel, clonedModel);
             return new ArrayList<FaultyComponent>();
         } catch (KevoreeModelUpdateException e) {
             return faultyComponents;
         }
 
     }
 
     private String getActionName() {
         return "remove";
     }
 }
