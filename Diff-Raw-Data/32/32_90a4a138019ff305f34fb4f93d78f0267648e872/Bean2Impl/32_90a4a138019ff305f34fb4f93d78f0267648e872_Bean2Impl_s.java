 package org.levi.engine.utils;
 
 import org.levi.engine.Deployment;
 import org.levi.engine.EngineData;
 import org.levi.engine.persistence.hibernate.process.hobj.DeploymentBean;
 import org.levi.engine.persistence.hibernate.process.hobj.EngineDataBean;
 import org.levi.engine.persistence.hibernate.process.hobj.ProcessInstanceBean;
 import org.levi.engine.runtime.ProcessInstance;
 
 import java.util.ArrayList;
 
 /**
  * Created by IntelliJ IDEA.
  * User: eranda
  * Date: May 31, 2011
  * Time: 4:47:17 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Bean2Impl {
 
     public EngineData engineData(EngineDataBean bean){
         EngineData engine = new EngineData();
         engine.setDeploymentPIds(new ArrayList<String>(bean.getDeployedProcesses().keySet()));
         engine.setPausedProcessIds(new ArrayList<String>(bean.getPausedProcesses().keySet()));
         engine.setStoppedProcessIds(new ArrayList<String>(bean.getStoppedProcesses().keySet()));
         return engine;
    }

    public Deployment deployment(DeploymentBean bean){
        Deployment deployment = new Deployment(bean.getDefinitionsId(),bean.getProcessDefinitionPath(),bean.getDiagramPath(),bean.getExtractPath(),bean.getDeploymentTime());
        return deployment;
    }

    
    
 }
