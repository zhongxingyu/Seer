 package org.levi.engine.persistence.hibernate;
 
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.hibernate.criterion.Restrictions;
 import org.hibernate.exception.ConstraintViolationException;
 import org.levi.engine.persistence.hibernate.process.hobj.TaskBean;
 import org.levi.engine.persistence.hibernate.user.hobj.GroupBean;
 import org.levi.engine.persistence.hibernate.user.hobj.UserBean;
 
 import java.util.List;
 
 
 /**
  * Created by IntelliJ IDEA.
  * UserBean: eranda
  * Date: May 10, 2011
  * Time: 8:31:12 AM
  * To change this template use File | Settings | File Templates.
  */
 public class HibernateDao {
 
     Session session;
 
     public HibernateDao() {
         session = SessionFactoryUtil.getSession();
     }
 
     public Session getSession() {
         return session;
     }
 
     public void save(HObject hobj) {
         try {
             Transaction tx = session.beginTransaction();
             session.save(hobj);
             tx.commit();
             session.flush();
         } catch (ConstraintViolationException e) {
             System.out.println("[error] Constrained violated");
         }
     }
 
     public Object getObject(Class hibCls, String id) {
         session.close();
         session = SessionFactoryUtil.getSession();
         Object obj = session.get(hibCls, id);
         return obj;
     }
 
     public Object remove(Class hibCls, String id) {
         Transaction tx = session.beginTransaction();
         Object obj = session.get(hibCls, id);
         session.delete(obj);
         tx.commit();
         session.flush();
         return obj;
     }
 
     public void update(HObject hobj) {
         Transaction tx = session.beginTransaction();
         session.saveOrUpdate(hobj);
         tx.commit();
         session.flush();
     }
 
     public List<UserBean> getUserObjects() {
         List objects = session.createCriteria(UserBean.class).list();
         return objects;
     }
 
     public List<GroupBean> getGroupObjects() {
         List objects = session.createCriteria(GroupBean.class).list();
         return objects;
     }
 
     public List<TaskBean> getAssignedTasks(String userId) {
         Criteria criteria = session.createCriteria(TaskBean.class);
         criteria.add(Restrictions.eq("active", true));
         return criteria.list();
     }
 
     public List<TaskBean> getUnassignedTasks(String groupId) {
         Criteria criteria = session.createCriteria(TaskBean.class);
        criteria.add(Restrictions.eq("potentialGroup.groupId", groupId));
        criteria.add(Restrictions.eq("assigned", false));
         return criteria.list();
     }
 
    public TaskBean getTask(String taskId, String processInstanceId) {
         Criteria criteria = session.createCriteria(TaskBean.class);
         criteria.add(Restrictions.eq("taskId", taskId));
        criteria.add(Restrictions.eq("processeInstance.processId", processInstanceId));
        if (criteria.list().size() > 0)
            return (TaskBean) criteria.list().get(0);
        else
            return null;
     }
 
     public void close() {
         session.close();
     }
 }
