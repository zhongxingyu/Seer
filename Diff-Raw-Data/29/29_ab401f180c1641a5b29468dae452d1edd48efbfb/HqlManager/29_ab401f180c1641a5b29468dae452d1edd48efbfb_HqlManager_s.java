 package org.levi.engine.persistence.hibernate.process.ql;
 
 import org.hibernate.Criteria;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.criterion.Restrictions;
 import org.hibernate.type.Type;
 import org.levi.engine.persistence.hibernate.SessionFactoryUtil;
 import org.levi.engine.persistence.hibernate.process.hobj.TaskBean;
 import org.levi.engine.persistence.hibernate.user.hobj.GroupBean;
 import org.levi.engine.persistence.hibernate.user.hobj.UserBean;
 
 import java.sql.Types;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: eranda
  * Date: Jun 20, 2011
  * Time: 11:07:22 PM
  * To change this template use File | Settings | File Templates.
  */
 public class HqlManager {
 
     Session session;
 
    public HqlManager(){
         session = SessionFactoryUtil.getSession();
     }
 
    public List<UserBean> getUserObjects(){
         refresh();
         List<UserBean> users = session.createQuery("from UserBean").list();
         return users;
 
         //List<UserBean> users = session.createSQLQuery("select * from users").addEntity(UserBean.class).list();
         //return users;
     }
 
    public List<GroupBean> getGroupObjects(){
         refresh();
         List<GroupBean> groups = session.createQuery("from GroupBean").list();
         return groups;
 
         //List<GroupBean> groups = session.createSQLQuery("select * from groups").addEntity(GroupBean.class).list();
         //return groups;
     }
 
    public List<String> getGroupIds(){
         refresh();
         List<String> groupIds = session.createQuery("select groupId from GroupBean").list();
         return groupIds;
     }
 
     public List<TaskBean> getUnassignedTasks(String groupId) {
         refresh();
        List<TaskBean> tasks = session.createQuery("from TaskBean as task left join fetch task.potentialGroup as group where" +
                "group.groupId = "+groupId+" and assigned = false and active = true").list();
        return tasks;
     }
 
    private void refresh(){
         session.close();
         session = SessionFactoryUtil.getSession();
     }
 
     public void close() {
         session.close();
     }
 }
