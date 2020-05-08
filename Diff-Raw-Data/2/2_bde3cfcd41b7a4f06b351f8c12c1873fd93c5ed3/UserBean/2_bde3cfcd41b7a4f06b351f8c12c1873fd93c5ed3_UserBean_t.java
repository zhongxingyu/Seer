 package org.levi.engine.persistence.hibernate.user.hobj;
 
 import org.levi.engine.persistence.hibernate.HObject;
 import org.levi.engine.persistence.hibernate.process.hobj.DeploymentBean;
 import org.levi.engine.persistence.hibernate.process.hobj.ProcessInstanceBean;
 import org.levi.engine.persistence.hibernate.process.hobj.TaskBean;
 import org.levi.engine.runtime.ProcessInstance;
 
 import javax.persistence.*;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * UserBean: eranda
  * Date: May 11, 2011
  * Time: 7:52:29 AM
  * To change this template use File | Settings | File Templates.
  */
 
 @Entity
 public class UserBean extends HObject implements Serializable {
     private String userId;
     private String password;
     private String firstName;
     private String lastName;
     private String userEmail;
     private List<GroupBean> userGroups = new ArrayList<GroupBean>();
     private List<TaskBean> assigned = new ArrayList<TaskBean>();
     private List<TaskBean> owned = new ArrayList<TaskBean>();
     private List<ProcessInstanceBean> started = new ArrayList<ProcessInstanceBean>();
     private List<DeploymentBean> deployed = new ArrayList<DeploymentBean>();
     private List<DeploymentBean> undeployed = new ArrayList<DeploymentBean>();
 
     /*
         Bean Identifier-UserId
      */
     @Id
     public String getUserId() {
         return this.userId;
     }
 
     public void setUserId(String userId) {
         this.userId = userId;
     }
 
     public String getPassword() {
         return this.password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public String getFirstName() {
         return this.firstName;
     }
 
     public void setFirstName(String firstName) {
         this.firstName = firstName;
     }
 
     public String getLastName() {
         return this.lastName;
     }
 
     public void setLastName(String lastName) {
         this.lastName = lastName;
     }
 
     public String getUserEmail() {
         return userEmail;
     }
 
     public void setUserEmail(String userEmail) {
         this.userEmail = userEmail;
     }
 
     /*
         @ManyToMany - One user can be categorized  under many groups
         fetch = FetchType.LAZY - fetching 'UserBean' must not fetch its owned 'GroupBean's
         CascadeType.ALL - Delete a UserBean must cause delete its data in all other tables
      */
     @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
     @JoinTable(name = "user_group", joinColumns = {@JoinColumn(name = "userId")}, inverseJoinColumns = {@JoinColumn(name = "groupId")})
     public List<GroupBean> getUserGroups() {
         return this.userGroups;
     }
 
     public void setUserGroups(List<GroupBean> groups) {
         this.userGroups = groups;
     }
 
     /*
         @OneToMany - One user can assign many tasks
         mappedBy = "assignee" - attribute mapped in TaskBean 
         fetch = FetchType.LAZY - fetching 'UserBean' must not fetch its owned 'TaskBean's
         CascadeType.ALL - Delete a UserBean must cause delete its data in all other tables  which he involve 
      */
    @OneToMany(targetEntity = TaskBean.class, mappedBy = "assignee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
     public List<TaskBean> getAssigned() {
         return assigned;
     }
 
     public void setAssigned(List<TaskBean> assigned) {
         this.assigned = assigned;
     }
 
     /*
         @OneToMany - One user can own many tasks
         mappedBy = "owner" - attribute mapped in TaskBean
         fetch = FetchType.LAZY - fetching 'UserBean' must not fetch its owned 'TaskBean's
         CascadeType.ALL - Delete a UserBean must cause delete its data in all other tables  which he involve
      */
     @OneToMany(targetEntity = TaskBean.class, mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
     public List<TaskBean> getOwned() {
         return owned;
     }
 
     public void setOwned(List<TaskBean> owned) {
         this.owned = owned;
     }
 
     /*
         @OneToMany - One user can start many processes
         mappedBy = "startUser" - attribute mapped in ProcessInstanceBean
         fetch = FetchType.LAZY - fetching 'UserBean' must not fetch its owned 'ProcessInstanceBean's
         CascadeType.ALL - Delete a UserBean must cause delete its data in all other tables  which he involve
      */
     @OneToMany(targetEntity = ProcessInstanceBean.class, mappedBy = "startUser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
     public List<ProcessInstanceBean> getStarted() {
         return started;
     }
 
     public void setStarted(List<ProcessInstanceBean> started) {
         this.started = started;
     }
 
     /*
         @OneToMany - One user can deploy many processes
         mappedBy = "startUser" - attribute mapped in DeploymentBean
         fetch = FetchType.LAZY - fetching 'UserBean' must not fetch its owned 'DeploymentBean's
         CascadeType.ALL - Delete a UserBean must cause delete its data in all other tables  which he involve
      */
     @OneToMany(targetEntity = DeploymentBean.class, mappedBy = "deployedUser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
     public List<DeploymentBean> getDeployed() {
         return deployed;
     }
 
     public void setDeployed(List<DeploymentBean> deployed) {
         this.deployed = deployed;
     }
 
     /*
         @OneToMany - One user can undeploy many processes
         mappedBy = "startUser" - attribute mapped in DeploymentBean
         fetch = FetchType.LAZY - fetching 'UserBean' must not fetch its owned 'DeploymentBean's
         CascadeType.ALL - Delete a UserBean must cause delete its data in all other tables  which he involve
      */
     @OneToMany(targetEntity = DeploymentBean.class, mappedBy = "undeployedUser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
     public List<DeploymentBean> getUndeployed() {
         return undeployed;
     }
 
     public void setUndeployed(List<DeploymentBean> undeployed) {
         this.undeployed = undeployed;
     }
 
     public void addStartedProcessInstances(ProcessInstanceBean processInstanceBean) {
         if (started == null) {
             started = new ArrayList<ProcessInstanceBean>();
         }
         started.add(processInstanceBean);
     }
 
     public void removeFromAssignedList(TaskBean taskBean) {
         if (assigned == null) {
             return;
         }
         assigned.remove(taskBean);
     }
 }
