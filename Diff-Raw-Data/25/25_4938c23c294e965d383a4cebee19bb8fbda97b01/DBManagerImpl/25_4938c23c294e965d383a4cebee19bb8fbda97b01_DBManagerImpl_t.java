 package org.levi.engine.impl.db;
 
 import org.levi.engine.db.DBManager;
 import org.levi.engine.persistence.hibernate.HibernateDao;
 import org.levi.engine.persistence.hibernate.process.hobj.DeploymentBean;
 import org.levi.engine.persistence.hibernate.process.hobj.ProcessInstanceBean;
 import org.levi.engine.persistence.hibernate.process.hobj.TaskBean;
 import org.levi.engine.persistence.hibernate.user.hobj.GroupBean;
 import org.levi.engine.persistence.hibernate.user.hobj.UserBean;
 
 import java.util.List;
 
 public class DBManagerImpl implements DBManager {
 
     HibernateDao dao;

    public DBManagerImpl() {
       dao = new HibernateDao();
     }

     /**
      * This method saves a UserBean to the database; if the UserBean already exists, it updates the attributes
      *
      * @param user The user
      */
 
 
     public void saveUser(UserBean user) {
         dao.save(user);
     }
 
     public void saveGroup(GroupBean group) {
         dao.save(group);
     }
 
     public UserBean getUser(String userId) {
         return (UserBean)dao.getObject(UserBean.class,userId);
     }
 
     public GroupBean getGroup(String groupId) {
         return (GroupBean)dao.getObject(GroupBean.class,groupId);
     }
 
     public void addUserToGroup(String userId, String groupId) {
         UserBean user = (UserBean)dao.getObject(UserBean.class,userId);
         GroupBean group =  (GroupBean)dao.getObject(GroupBean.class,groupId);
         group.getMembers().add(user);
     }
 
     public void deleteUser(String userId) {
         dao.remove(UserBean.class, userId);
     }
 
     public void deleteGroup(String groupId) {
         dao.remove(GroupBean.class, groupId);
     }
 
     public void removeUserFromGroup(String userId, String groupId) {
         GroupBean group =  (GroupBean)dao.getObject(GroupBean.class,groupId);
         UserBean user = (UserBean)dao.getObject(UserBean.class,userId);
         group.getMembers().remove(user);
     }
 
     public void saveTask(TaskBean task) {
         dao.save(task);
     }
 
     public void deleteTask(String taskId) {
         dao.remove(TaskBean.class, taskId);
     }
 
     public void updateTask(TaskBean task) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public void saveProcess(DeploymentBean deployedProcess) {
         dao.save(deployedProcess);
     }
 
     public void deleteProcess(String processId) {
         dao.remove(DeploymentBean.class, processId);
     }
 
     public void updateProcess(ProcessInstanceBean process) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public void saveProcessInstance(ProcessInstanceBean process) {
         dao.save(process);
     }
 
     public void updateProcess(DeploymentBean process) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public void deleteProcessInstance(String processId) {
         dao.remove(ProcessInstanceBean.class, processId);
     }
 
     public void updateProcessInstance(ProcessInstanceBean process) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public List<TaskBean> getUserTaskList(String userId) {
         UserBean user = (UserBean)dao.getObject(UserBean.class,userId);
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public List<TaskBean> getGroupTaskList(String groupId) {
         GroupBean group =  (GroupBean)dao.getObject(GroupBean.class,groupId);
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public List<ProcessInstanceBean> getRunningProcessesInstancesList() {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public List<DeploymentBean> getDeployedProcessList() {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public UserBean getAssigneeForTask(String taskId) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public List<TaskBean> getActiveTasks() {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public List<TaskBean> getUnassignedTasks() {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public List<TaskBean> getActiveTasks(String processId) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }

    // given the taskId, the id of the process instance that task belongs to should be retrieved
    // TaskBean has a processInstanceId attribute
    // Can this be retrieved from the TaskBean table?
    public String getProcessInstanceId(String taskId) {
        return null;
    }
 }
