 package org.levi.engine.db;
 
 import org.levi.engine.persistence.hibernate.process.hobj.DeploymentBean;
 import org.levi.engine.persistence.hibernate.process.hobj.ProcessInstanceBean;
 import org.levi.engine.persistence.hibernate.process.hobj.TaskBean;
 import org.levi.engine.persistence.hibernate.user.hobj.GroupBean;
 import org.levi.engine.persistence.hibernate.user.hobj.UserBean;
 
 import java.util.List;
 
 /**
  * This is the interface to classes which handles the back end database management of Levi Engine
  */
 
 public interface DBManager {
 
     /**
      * This method saves a UserBean to the database; if the UserBean already exists, it updates the attributes
      *
      * @param user The user
      */
     void saveUser(UserBean user);
 
     void saveGroup(GroupBean group);
 
     UserBean getUser(String userId);
 
     GroupBean getGroup(String groupId);
 
     void addUserToGroup(String userId, String groupId);
 
     void deleteUser(String userId);        //?? Only ids or the UserBean object?
 
     void deleteGroup(String groupId);
 
     void removeUserFromGroup(String userId, String groupId); //?? UserBean& Group objects or simple the ids?
 
     void saveTask(TaskBean task);
 
     void deleteTask(String taskId);
 
     void updateTask(TaskBean task);
 
     void saveProcess(DeploymentBean process);
 
     void deleteProcess(String processId);
 
     void updateProcess(ProcessInstanceBean process);
 
     void saveProcessInstance(ProcessInstanceBean process);
 
     void deleteProcessInstance(String processId);
 
     void updateProcessInstance(ProcessInstanceBean process);
 
     List<TaskBean> getUserTaskList(String userId);
 
     List<TaskBean> getGroupTaskList(String groupId);
 
     List<ProcessInstanceBean> getRunningProcessesInstancesList();
 
     List<DeploymentBean> getDeployedProcessList();
 
     UserBean getAssigneeForTask(String taskId);
 
     List<TaskBean> getActiveTasks();
 
     List<TaskBean> getUnassignedTasks();
 
     List<TaskBean> getActiveTasks(String processId);
 
    // given the taskId, the id of the process instance that task belongs to should be retrieved
    // TaskBean has a processInstanceId attribute
    // Can this be retrieved from the TaskBean table?
    String getProcessInstanceId(String taskId);

 
 }
