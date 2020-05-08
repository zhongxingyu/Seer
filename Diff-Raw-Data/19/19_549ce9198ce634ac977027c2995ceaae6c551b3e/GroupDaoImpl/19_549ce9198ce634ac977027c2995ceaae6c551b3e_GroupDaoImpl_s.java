 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.abada.cleia.dao.impl;
 
 import com.abada.cleia.dao.GroupDao;
 import com.abada.cleia.entity.user.Group;
 import com.abada.cleia.entity.user.User;
 import com.abada.jbpm.task.spring.TaskService;
 import com.abada.springframework.orm.jpa.support.JpaDaoUtils;
 import com.abada.springframework.web.servlet.command.extjs.gridpanel.GridRequest;
 import java.util.ArrayList;
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  *
  * @author katsu
  */
 public class GroupDaoImpl extends JpaDaoUtils implements GroupDao {
 
     private static final Log logger = LogFactory.getLog(GroupDaoImpl.class);
     @Autowired
     private TaskService taskService;
     @PersistenceContext(unitName = "cleiaPU")
     private EntityManager entityManager;
 
 //    @Transactional(value = "cleia-txm", readOnly = true)
 //    public Patient getPatientById(long patientId) {
 //        Patient result=entityManager.find(Patient.class, patientId);
 //        return result;
 //    }
     /**
      * returns all users in a group
      *
      * @param groupname
      * @return
      */
     @Transactional(value = "cleia-txm", readOnly = true)
     public List<String> getMemberGroup(String groupname) {
         List<String> listmember = new ArrayList<String>();
         List<Group> listgroup = entityManager.createQuery("select g from Group g where g.value=?").setParameter(1, groupname).getResultList();
         if (listgroup != null && !listgroup.isEmpty()) {
             Group group = listgroup.get(0);
             for (User u : group.getUsers()) {
                 listmember.add(u.getUsername());
             }
         }
         return listmember;
     }
 
     /**
      * Returns all groups
      *
      * @return
      */
     @Transactional(value = "cleia-txm", readOnly = true)
     public List<Group> getAllGroups() {
         List<Group> listgroups = entityManager.createQuery("SELECT g FROM Group g").getResultList();
         return listgroups;
     }
 
     /**
      * Returns one group by id
      *
      * @param idgroup
      * @return
      */
     @Transactional(value = "cleia-txm", readOnly = true)
     public Group getGroupById(Long idgroup) {
 
         Group group = entityManager.find(Group.class, idgroup);
         if (group != null) {
             group.getUsers().size();
             return group;
         }
 
         return null;
 
     }
 
     /**
      * Insert a group
      *
      * @param name
      */
     @Transactional(value = "cleia-txm")
     public void postGroup(Group group) throws Exception {
         List<Group> lgroup = entityManager.createQuery("select g from Group g where g.value=?").setParameter(1, group.getValue()).getResultList();
         if (lgroup.isEmpty() && lgroup != null) {
             List<User> luser = entityManager.createQuery("select u from User u where u.username=?").setParameter(1, group.getValue()).getResultList();
             if (luser.isEmpty() && luser != null) {
                 try {
                     org.jbpm.task.Group grouptask = new org.jbpm.task.Group();
                     grouptask.setId(group.getValue());
                     taskService.getTaskSession().addGroup(grouptask);
                     this.addUsers(group, group.getUsers(), true);
                     entityManager.persist(group);
                 } catch (Exception e) {
                     throw new Exception("Error. Ha ocurrido un error al insertar el servicio " + group.getValue(), e);
                 }
             } else {
                 throw new Exception("Error. Ya existe un usuario con el username " + group.getValue());
             }
         } else {
             throw new Exception("Error. El servicio " + group.getValue() + " ya existe.");
         }
     }
 
     /**
      * Modify a group by id
      *
      * @param idgroup
      * @param name
      * @return
      */
     @Transactional(value = "cleia-txm")
     public void putGroup(Long idgroup, Group newgroup) throws Exception {
         Group group = entityManager.find(Group.class, idgroup);
         if (group != null) {
             List<Group> lgroup = entityManager.createQuery("select g from Group g where g.value=?").setParameter(1, newgroup.getValue()).getResultList();
 
             if ((lgroup.isEmpty() && lgroup != null) || newgroup.getValue().equals(group.getValue())) {
 
                 List<User> luser = entityManager.createQuery("select u from User u where u.username=?").setParameter(1, newgroup.getValue()).getResultList();
 
                 if (luser.isEmpty() && luser != null) {
                     try {
                         if (!group.getValue().equals(newgroup.getValue())) {
                             org.jbpm.task.Group grouptask = new org.jbpm.task.Group();
                             grouptask.setId(newgroup.getValue());
                             taskService.getTaskSession().addGroup(grouptask);
                         }
                         this.addUsers(group, newgroup.getUsers(), false);
                         group.setValue(newgroup.getValue());
                     } catch (Exception e) {
 
                         throw new Exception("Error. Ha ocurrido un error al modificar el servicio " + newgroup.getValue(), e);
                     }
                 }
             } else {
                 throw new Exception("Error. El servicio " + newgroup.getValue() + " ya existe.");
             }
         } else {
             throw new Exception("Error. El servicio no existe.");
         }
     }
 
     /**
      * Delete a group by id
      *
      * @param idgroup
      * @return
      */
     @Transactional(value = "cleia-txm")
     public void deleteGroup(Long idgroup) throws Exception {
         Group group = (Group) entityManager.find(Group.class, idgroup);
 
         if (group != null) {
             try {
 
                 for (User u : group.getUsers()) {
                     u.getGroups().remove(group);
                 }
                 group.getUsers().clear();
                 entityManager.remove(group);
             } catch (Exception e) {
                 throw new Exception("Error. Ha ocurrido un error al borrar el servicio " + group.getValue(), e);
             }
         } else {
             throw new Exception("Error. No se puede borrar el servicio. Compruebe que exista.");
         }
     }
 
     /**
      * Search a list of groups by params
      *
      * @param filters
      * @return
      */
     @Transactional(value = "cleia-txm", readOnly = true)
     public List<Group> getAll(GridRequest filters) {
 
         List<Group> lgroup = this.find(entityManager, "select g from Group g" + filters.getQL("g", true), filters.getParamsValues(), filters.getStart(), filters.getLimit());
 
         for (Group g : lgroup) {
             g.getUsers().size();
         }
 
         return lgroup;
     }
 
     /**
      * Returns a list of all users from a group
      *
      * @param idgroup
      * @return
      * @throws Exception
      */
     @Transactional(value = "cleia-txm", readOnly = true)
    public List<User> getUsersByIdGroup(Long idgroup) throws Exception {
 
         Group group = new Group();
         group = entityManager.find(Group.class, idgroup);
 
         /*
          * Si el grupo existe le fuerzo a que traiga su lista de User
          */
         if (group == null) {
             throw new Exception("Error. El servicio no existe");
         } else {
             for (User user : group.getUsers()) {
                 user.getRoles().size();
                 user.getGroups().size();
 
             }
         }
 
         return (List<User>) group.getUsers();
     }
 
     /**
      * Obtiene el tamaño de {@link Group}
      *
      * @param filters
      * @return Long
      */
     @Transactional(value = "cleia-txm", readOnly = true)
     public Long loadSizeAll(GridRequest filters) {
         List<Long> result = this.find(entityManager, "select count(*) from Group g" + filters.getQL("g", true), filters.getParamsValues());
         return result.get(0);
     }
 
     /**
      * add users a group
      *
      * @param group
      * @param luser
      * @param newgroup
      * @throws Exception
      */
     @Transactional(value = "cleia-txm")
     public void addUsers(Group group, List<User> luser, boolean newgroup) throws Exception {
 
         if (luser != null) {
             if (!newgroup) {
                 for (User u : group.getUsers()) {
                     u.getGroups().remove(group);
                 }
                 group.getUsers().clear();
 
                 entityManager.flush();
             }
             for (User u : luser) {
                 User user = entityManager.find(User.class, u.getId());
                 if (user != null) {
                     group.addUser(user);
                 } else {
                     throw new Exception("Error. Uno de los usuarios no existe");
                 }
             }
         } else {
             throw new NullPointerException("Error. Lista de usuarios inexistente");
         }
     }
 }
