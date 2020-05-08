 package org.levi.engine.persistence.hibernate.process.hobj;
 
 import org.hibernate.annotations.Cascade;
 import org.hibernate.annotations.CollectionOfElements;
 import org.levi.engine.Deployment;
 import org.levi.engine.persistence.hibernate.HObject;
 import org.levi.engine.runtime.ProcessInstance;
 
 import javax.persistence.*;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Created by IntelliJ IDEA.
  * User: eranda
  * Date: May 27, 2011
  * Time: 12:53:22 AM
  * To change this template use File | Settings | File Templates.
  */
 @Entity
 @Table(name = "engine_data")
 public class EngineDataBean extends HObject {
 
     private String id;
     //private int nDeployments;
     //private List<String> deploymentPIds;
     private Map<String, DeploymentBean> deployedProcesses;
     //private int nDeploymentPIds;
     private Map<String, ProcessInstanceBean> runningProcesses;
     //private int nRunningProcesses;
     private Map<String, ProcessInstanceBean> stoppedProcesses;
     private Map<String, ProcessInstanceBean> pausedProcesses;
     //private List<String> runningProcessIds;
 
     @Id
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     /*
     public int getnDeployments() {
         return nDeployments;
     }
 
     public void setnDeployments(int nDeployments) {
         this.nDeployments = nDeployments;
     }
     */
 
     /*
     @CollectionOfElements
 	@Cascade({org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
 	@JoinTable( name="deploymentpids",joinColumns={ @JoinColumn(name="id")})
     public List<String> getDeploymentPIds() {
         return deploymentPIds;
     }
 
     public void setDeploymentPIds(List<String> deploymentPIds) {
         this.deploymentPIds = deploymentPIds;
     }
      */
 
    @OneToMany
     @MapKey(name = "definitionsId")
     @JoinTable(name = "deployed_processes", joinColumns = {@JoinColumn(name = "id")})
     public Map<String, DeploymentBean> getDeployedProcesses() {
         return deployedProcesses;
     }
 
     public void setDeployedProcesses(Map<String, DeploymentBean> deployedProcesses) {
         this.deployedProcesses = deployedProcesses;
     }
 
     /*
     public int getnDeploymentPIds() {
         return nDeploymentPIds;
     }
 
     public void setnDeploymentPIds(int nDeploymentPIds) {
         this.nDeploymentPIds = nDeploymentPIds;
     }
     */
 
     /*
     public int getnRunningProcesses() {
         return nRunningProcesses;
     }
 
     public void setnRunningProcesses(int nRunningProcesses) {
         this.nRunningProcesses = nRunningProcesses;
     }
     */
 
     @OneToMany(targetEntity = ProcessInstanceBean.class)
     @MapKey(name = "processId")
     @JoinTable(name = "process_instance_data", joinColumns = {@JoinColumn(name = "id")})
     public Map<String, ProcessInstanceBean> getRunningProcesses() {
         return runningProcesses;
     }
 
     public void setRunningProcesses(Map<String, ProcessInstanceBean> runningProcesses) {
         this.runningProcesses = runningProcesses;
     }
 
     @OneToMany(targetEntity = ProcessInstanceBean.class)
     @MapKey(name = "processId")
     @JoinTable(name = "stopped_process_instance_data", joinColumns = {@JoinColumn(name = "id")})
     public Map<String, ProcessInstanceBean> getStoppedProcesses() {
         return stoppedProcesses;
     }
 
     public void setStoppedProcesses(Map<String, ProcessInstanceBean> stoppedProcesses) {
         this.stoppedProcesses = stoppedProcesses;
     }
 
     @OneToMany(targetEntity = ProcessInstanceBean.class)
     @MapKey(name = "processId")
     @JoinTable(name = "paused_process_instance_data", joinColumns = {@JoinColumn(name = "id")})
     public Map<String, ProcessInstanceBean> getPausedProcesses() {
         return pausedProcesses;
     }
 
     public void setPausedProcesses(Map<String, ProcessInstanceBean> pausedProcesses) {
         this.pausedProcesses = pausedProcesses;
     }
 
     /*
     public List<String> getRunningProcessIds() {
         return runningProcessIds;
     }
 
     public void setRunningProcessIds(List<String> runningProcessIds) {
         this.runningProcessIds = runningProcessIds;
     }
     */
 
     public void addDeployment(DeploymentBean deploymentBean) {
         if (deployedProcesses == null) {
             deployedProcesses = new HashMap<String, DeploymentBean>();
         }
         deployedProcesses.put(deploymentBean.getDefinitionsId(), deploymentBean);
     }
 
     public void addProcessInstance(ProcessInstanceBean processInstanceBean) {
         if (runningProcesses == null) {
             runningProcesses = new HashMap<String, ProcessInstanceBean>();
         }
         runningProcesses.put(processInstanceBean.getProcessId(), processInstanceBean);
     }
 }
