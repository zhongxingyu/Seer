 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.abada.cleia.dao.impl;
 
 /*
  * #%L
  * Cleia
  * %%
  * Copyright (C) 2013 Abada Servicios Desarrollo
  * (investigacion@abadasoft.com)
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as
  * published by the Free Software Foundation, either version 3 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/gpl-3.0.html>.
  * #L%
  */
 import com.abada.cleia.dao.IdDao;
 import com.abada.cleia.dao.UserDao;
 import com.abada.cleia.entity.user.Group;
 import com.abada.cleia.entity.user.Id;
 import com.abada.cleia.entity.user.Patient;
 import com.abada.cleia.entity.user.Role;
 import com.abada.cleia.entity.user.User;
 import com.abada.springframework.orm.jpa.support.JpaDaoUtils;
 import com.abada.springframework.web.servlet.command.extjs.gridpanel.GridRequest;
 import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
 import java.util.ArrayList;
 import java.util.List;
 import javax.annotation.Resource;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  *
  * @author david
  */
 public class UserDaoImpl extends JpaDaoUtils implements UserDao {
 
     private static final String DEFAUL_ROLE = "ROLE_USER";
     private static final Log logger = LogFactory.getLog(UserDaoImpl.class);
     @PersistenceContext(unitName = "cleiaPU")
     private EntityManager entityManager;
     @Autowired
     private ShaPasswordEncoder sha1PasswordEncoder;
     @Resource(name = "idDao")
     private IdDao idDao;
 
     /**
      * Returns all actors that an actor has in their groups
      *
      * @param username
      * @return
      */
     @Transactional(value = "cleia-txm", readOnly = true)
     public List<String> getUserGroup(String username) {
         List<String> listuser = new ArrayList<String>();
         List<User> users = entityManager.createQuery("select u from User u where u.username=?").setParameter(1, username).getResultList();
         if (users != null && !users.isEmpty()) {
             for (Group group : users.get(0).getGroups()) {
                 if (group != null) {
                     for (User u : group.getUsers()) {
                         if (!listuser.contains(u.getUsername())) {
                             listuser.add(u.getUsername());
                         }
                     }
                 }
             }
         }
         return listuser;
     }
 
     /**
      * Returns all users
      *
      * @return
      */
     @Transactional(value = "cleia-txm", readOnly = true)
     public List<User> getAllUsers() {
 
         List<User> lusers = entityManager.createQuery("SELECT u FROM User u").getResultList();
 
         /*
          * Fuerzo a que cada usuario traiga sus lista de Role y Group
          */
         if (!lusers.isEmpty()) {
             for (User u : lusers) {
                 u.getGroups().size();
                 u.getRoles().size();
                 u.getIds().size();
                 /*
                  * if (u instanceof Patient) { Patient p = (Patient) u;
                  * p.getMedicals().size(); p.getProcessInstances().size(); }
                  */
 
             }
         }
         return lusers;
     }
 
     /**
      * Obtiene el tamaño de {@link User}
      *
      * @param filters
      * @return Long
      */
     @Transactional(value = "cleia-txm", readOnly = true)
     public Long loadSizeAll(GridRequest filters) {
         List<Long> result = this.find(entityManager, "select count(*) from User u" + filters.getQL("u", true), filters.getParamsValues());
         return result.get(0);
     }
 
     /**
      * Returns one user by id
      *
      * @param iduser
      * @return
      */
     @Transactional(value = "cleia-txm", readOnly = true)
     public User getUserById(Long iduser) {
 
         User user = entityManager.find(User.class, iduser);
 
         /*
          * Si el usuario no es null le fuerzo a que traiga su lista de Role y
          * Group
          */
         if (user != null) {
             user.getGroups().size();
             user.getRoles().size();
             user.getIds().size();
             /*
              * if (user instanceof Patient) { Patient p = (Patient) user;
              * p.getMedicals().size(); p.getProcessInstances(); }
              */
             return user;
         }
 
         return null;
 
     }
 
     /*
      * find user given id
      *
      * @param asList @return @throws Exception
      */
     @Transactional(value = "cleia-txm", readOnly = true)
     public List<User> findUsersrepeatable(List<Id> asList, Boolean repeatable) throws Exception {
 
         List<User> u = new ArrayList<User>();
         if (asList != null && !asList.isEmpty()) {
             for (int i = 0; i < asList.size(); i++) {
                 if (!asList.get(i).getType().isRepeatable()) {
                     for (int j = 0; j < asList.size(); j++) {
                         if (i != j && asList.get(i).getType().getValue().equals(asList.get(j).getType().getValue())) {
                             throw new Exception("Error. El identificador " + asList.get(i).getType().getValue() + " no se puede repetir");
                         }
                     }
                 }
             }
             int append = 0;
             StringBuilder query = new StringBuilder();
             query.append("SELECT u FROM User u join u.ids idss WHERE idss.id in (select distinct pid.id from Id pid where ");
             for (Id pid : asList) {
                 if (pid.getValue() != null && !pid.getValue().equals("") && pid.getType() != null && pid.getType().getValue() != null) {
                     append++;
                     if (append != 1) {
                         query.append(" or ");
                     }
                     query.append("pid.value='").append(pid.getValue()).append("'");
                     if (repeatable != null) {
                         query.append(" and pid.type.repeatable=").append(repeatable);
                     }
 
                     query.append(" and pid.type.value='").append(pid.getType().getValue()).append("'");
                 } else {
                     throw new Exception("Error. Ha ocurrido un error en uno de los identificadores");
                 }
             }
             if (append != 0) {
                 query.append(")");
                 u = entityManager.createQuery(query.toString()).getResultList();
                 for (User user : u) {
                     user.getGroups().size();
                     user.getRoles().size();
                     user.getIds().size();
                 }
 
             }
         }
         return u;
     }
 
     /**
      * Insert a user
      *
      * @param enabled
      * @param nonexpired
      * @param nonexpiredCredentials
      * @param nonlocked
      * @param password
      * @param username
      * @throws Exception
      */
     @Transactional(value = "cleia-txm")
     public void postUser(User user) throws Exception {
 
         /*
          * if (user.getGroups() == null || user.getGroups().isEmpty()) { throw
          * new Exception("Error. El usuario debe pertenecer a un servicio"); }
          * else
          */
         if (user.getRoles() == null || user.getRoles().isEmpty()) {
             Role r = new Role();
             r.setAuthority(DEFAUL_ROLE);
             user.addRole(r);
         }
         List<User> luser = entityManager.createQuery("select u from User u where u.username=?").setParameter(1, user.getUsername()).getResultList();
         List<User> luserid = findUsersrepeatable(user.getIds(), Boolean.FALSE);
         if (luserid != null && luserid.isEmpty()) {
             if (luser != null && luser.isEmpty()) {
 
                 try {
                     this.addGroupsAndRoles(user, user.getGroups(), user.getRoles(), true);
                     this.addIds(user, user.getIds(), true);
                     user.setPassword(sha1PasswordEncoder.encodePassword(user.getPassword(), null));
 
                     entityManager.persist(user);
                 } catch (Exception e) {
 
                     throw new Exception("Error. Ha ocurrido un error al insertar el usuario " + user.getUsername(), e);
                 }
 
             } else {
                 throw new Exception("Error. El usuario " + user.getUsername() + " ya existe.");
             }
         } else {
             throw new Exception("Error. El usuario " + user.getUsername() + " ya existe con esos identificadores");
         }
     }
 
     /**
      * Modify a user by id
      *
      * @param iduser
      * @param enabled
      * @param nonexpired
      * @param nonexpiredCredentials
      * @param nonlocked
      * @param password
      * @param username
      * @throws Exception
      */
     @Transactional(value = "cleia-txm")
     public void putUser(Long iduser, User newuser) throws Exception {
         /*
          * if (newuser.getGroups() == null || newuser.getGroups().isEmpty()) {
          * throw new Exception("Error. El usuario debe pertenecer a un
          * servicio"); } else
          */ if (newuser.getRoles() == null || newuser.getRoles().isEmpty()) {
             Role r = new Role();
             r.setAuthority(DEFAUL_ROLE);
             newuser.addRole(r);
         }
         User user = entityManager.find(User.class, iduser);
         List<User> repeatedIds = findUsersrepeatable(newuser.getIds(), Boolean.FALSE);
         
         List<User> luserAux = new ArrayList<User>();
 
        //Remove updated user from repeatedIds list
         for (int i = 0; i < repeatedIds.size(); i++) {
             if (repeatedIds.get(i).getId() == iduser) {
               
                 luserAux.add(repeatedIds.get(i));
             }
 
         }
 
         if (luserAux != null && !luserAux.isEmpty()) {
             for (User u : luserAux) {
                 repeatedIds.remove(u);
             }
         }
 
         if (repeatedIds != null && repeatedIds.isEmpty()) {
             if (user != null) {
                 List<User> luser = entityManager.createQuery("select u from User u where u.username=?").setParameter(1, newuser.getUsername()).getResultList();
                 if (luser != null && luser.isEmpty() || newuser.getUsername().equals(user.getUsername())) {
 
                     try {
                         this.addGroupsAndRoles(user, newuser.getGroups(), newuser.getRoles(), false);
                         this.addIds(user, newuser.getIds(), false);
                         this.updateUser(user, newuser);
                     } catch (Exception e) {
 
                         throw new Exception("Error. Ha ocurrido un error al modificar el usuario " + newuser.getUsername(), e);
                     }
 
                 } else {
                     throw new Exception("Error. El usuario " + newuser.getUsername() + " ya existe");
                 }
             } else {
                 throw new Exception("Error. El usuario no existe");
             }
         } else {
             throw new Exception("Error. Ya existe un usuario con esos identificadores");
         }
     }
 
     /**
      * Search a list of users by params
      *
      * @param filters
      * @return
      */
     @Transactional(value = "cleia-txm", readOnly = true)
     public List<User> getAll(GridRequest filters) {
 
         List<User> luser = this.find(entityManager, "select u from User u" + filters.getQL("u", true), filters.getParamsValues(), filters.getStart(), filters.getLimit());
         /*
          * Fuerzo a que cada usuario traiga sus lista de Role y Group
          */
         if (luser != null && !luser.isEmpty()) {
             for (User u : luser) {
                 u.getGroups().size();
                 u.getRoles().size();
                 u.getIds().size();
                 /*
                  * if (u instanceof Patient) { Patient p = (Patient) u;
                  * p.getProcessInstances().size(); p.getMedicals().size(); }
                  */
 
             }
         }
         return luser;
     }
 
     /**
      * Search a list of users by params
      *
      * @param filters
      * @return
      */
     @Transactional(value = "cleia-txm", readOnly = true)
     public User getUserByUsername(GridRequest filters, String username) {
         User u = (User) entityManager.createQuery("select u from User u where u.username = :username").setParameter("username", username).getSingleResult();
         u.getGroups().size();
         u.getRoles().size();
         u.getIds().size();
         /*
          * if (u instanceof Patient) { Patient p = (Patient) u;
          * p.getMedicals().size(); p.getProcessInstances().size(); }
          */
 
         return u;
     }
 
     /**
      * Returns a list of all groups from a user
      *
      * @param iduser
      * @return
      * @throws Exception
      */
     @Transactional(value = "cleia-txm", readOnly = true)
     public List<Group> getGroupsByIdUser(Long iduser) throws Exception {
         User user = new User();
         user = entityManager.find(User.class, iduser);
 
         /*
          * Si el usuario existe le fuerzo a que traiga la lista de Group
          */
         if (user == null) {
             throw new Exception("Error. El usuario no existe");
         } else {
             for (Group g : user.getGroups()) {
                 g.getUsers().size();
             }
         }
 
         return (List<Group>) user.getGroups();
     }
 
     /**
      * Returns a list of all roles from a user
      *
      * @param iduser
      * @return
      * @throws Exception
      */
     @Transactional(value = "cleia-txm", readOnly = true)
     public List<Role> getRolesByIdUser(Long iduser) throws Exception {
 
         User user = new User();
         user = entityManager.find(User.class, iduser);
         /*
          * Si el usuario existe le fuerzo a que traiga su lista de Role
          */
         if (user == null) {
             throw new Exception("Error. El usuario no existe");
         } else {
             user.getRoles().size();
         }
 
         return (List<Role>) user.getRoles();
     }
 
     /**
      * Returns a list of all roles from a user
      *
      * @param iduser
      * @return
      * @throws Exception
      */
     @Transactional(value = "cleia-txm", readOnly = true)
     public List<Id> getIdsByIdUser(Long iduser) throws Exception {
 
         User user = new User();
         user = entityManager.find(User.class, iduser);
         /*
          * Si el usuario existe le fuerzo a que traiga su lista de Role
          */
         if (user == null) {
             throw new Exception("Error. El usuario no existe");
         } else {
             user.getIds().size();
         }
 
         return (List<Id>) user.getIds();
     }
 
     /**
      * Modifies the relationship between a user and a role
      *
      * @param iduser
      * @param idrole
      * @return
      */
     @Transactional(value = "cleia-txm")
     public void putUserRole(Long iduser, Integer idrole) throws Exception {
         User user = (User) entityManager.find(User.class, iduser);
 
         if (user != null) {
             Role role = (Role) entityManager.find(Role.class, idrole);
             if (role != null) {
                 List<Role> lrole = user.getRoles();
                 List<User> luser = role.getUsers();
                 if (lrole.contains(role) || luser.contains(user)) {
                     throw new Exception("Error. El usuario " + user.getUsername() + " ya tiene asignado el rol " + role.getAuthority());
                 } else {
                     lrole.add(role);
                     luser.add(user);
                 }
             } else {
                 throw new Exception("Error. El rol no existe");
             }
         } else {
             throw new Exception("Error. El usuario no existe");
         }
     }
 
     /**
      * Removes the relationship between a user and a role
      *
      * @param iduser
      * @param idrole
      * @return
      */
     @Transactional(value = "cleia-txm")
     public void deleteUserRole(Long iduser, Integer idrole) throws Exception {
         User user = (User) entityManager.find(User.class, iduser);
 
         if (user != null) {
             Role role = (Role) entityManager.find(Role.class, idrole);
             if (role != null) {
                 List<Role> lrole = user.getRoles();
                 List<User> luser = role.getUsers();
                 if (!lrole.contains(role) || !luser.contains(user)) {
                     throw new Exception("Error. El usuario " + user.getUsername() + " no tiene asignado el rol " + role.getAuthority());
                 } else {
                     lrole.remove(role);
                     luser.remove(user);
                 }
             } else {
                 throw new Exception("Error. El rol no existe");
             }
         } else {
             throw new Exception("Error. El usuario no existe.");
         }
     }
 
     /**
      * setting user
      *
      * @param user
      * @param newuser
      */
     @Transactional(value = "cleia-txm")
     public void updateUser(User user, User newuser) {
         user.setEnabled(newuser.isEnabled());
         user.setAccountNonExpired(newuser.isAccountNonExpired());
         user.setCredentialsNonExpired(newuser.isCredentialsNonExpired());
         user.setAccountNonLocked(newuser.isAccountNonLocked());
         user.setPassword(sha1PasswordEncoder.encodePassword(newuser.getPassword(), null));
         user.setUsername(newuser.getUsername());
     }
 
     /**
      * Enable a user by id
      *
      * @param iduser
      * @return
      */
     @Transactional(value = "cleia-txm")
     public void enableDisableUser(Long iduser, boolean enable) throws Exception {
 
         User user = entityManager.find(User.class, iduser);
         String habilitar = "";
         if (user != null) {
             if ((!user.isEnabled() && enable) || (user.isEnabled() && !enable)) {
                 try {
                     user.setEnabled(enable);
                 } catch (Exception e) {
                     if (enable) {
                         habilitar = "habilitar";
                     } else {
                         habilitar = "deshabilitar";
                     }
                     throw new Exception("Error. Ha ocurrido un error al " + habilitar + " al usuario " + user.getUsername());
                 }
             } else {
                 if (!enable) {
                     throw new Exception("Error. El usuario " + user.getUsername() + " ya esta deshabilitado");
                 } else {
                     throw new Exception("Error. El usuario " + user.getUsername() + " ya esta habilitado");
                 }
             }
         } else {
             throw new Exception("Error. El usuario no existe");
         }
 
     }
 
     @Transactional(value = "cleia-txm")
     public void addPatient2User(String username, Patient p) throws Exception {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
 
 
     }
 
     @Transactional(value = "cleia-txm")
     public void addGroupsAndRoles(User user, List<Group> lgroup, List<Role> lrole, boolean newUser) throws Exception {
 
         if (lgroup != null && lrole != null) {
             List<Group> lgroupaux = new ArrayList<Group>(lgroup);
             List<Role> lroleaux = new ArrayList<Role>(lrole);
 
             //Eliminamos todos los grupos y roles de un usuario
             if (!newUser) {
                 for (Group g : user.getGroups()) {
                     g.getUsers().remove(user);
                 }
                 for (Role r : user.getRoles()) {
                     r.getUsers().remove(user);
                 }
 
                 user.getGroups().clear();
                 user.getRoles().clear();
 
 
                 entityManager.flush();
             }
             if (lgroup != null) {
                 user.getGroups().clear();
                 for (Group g : lgroupaux) {
                     Group group = entityManager.find(Group.class, g.getValue());
                     if (group != null) {
                         user.addGroup(group);
                     } else {
                         throw new Exception("Error. Uno de los servicios no existe");
                     }
                 }
             }
             if (lrole != null) {
                 user.getRoles().clear();
                 for (Role r : lroleaux) {
                     Role role = entityManager.find(Role.class, r.getAuthority());
                     if (role != null) {
                         user.addRole(role);
                     } else {
                         throw new Exception("Error. Uno de los roles no existe");
                     }
                 }
             }
 
 
         } else {
             throw new NullPointerException("Error. Lista de servicios y roles inexistente");
         }
     }
 
     /**
      * Returns a list of users who are not assigned a patient
      *
      * @return
      */
     @Transactional(value = "cleia-txm", readOnly = true)
     public List<User> getUserWithoutAssignedPatient() {
 
         List<User> luser = entityManager.createQuery("SELECT u FROM User u WHERE u.id not in (select distinct p.id from Patient p)").getResultList();
 
 
         return luser;
     }
 
     @Transactional(value = "cleia-txm")
     private void addIds(User user, List<Id> ids, boolean newUser) throws Exception {
         if (ids != null) {
 
             if (newUser) {
                 user.setIds(null);
                 for (Id id : ids) {
                     idDao.postId(id);
                     user.addId(id);
                 }
             } else {
                 List<Id> oids = user.getIds();
                 user.setIds(null);
                 //check for remove ids
                 for (Id id : oids) {
                     boolean remove = true;
                     for (Id idn : ids) {
                         if (id.getId() == idn.getId()) {
                             remove = false;
                             user.addId(id);
                         }
 
                     }
                     if (remove) {
                         idDao.deleteId(id.getId());
                     }
 
                 }
                 // Add new ids
                 for (Id id : ids) {
 
                     if (id.getId() == 0) {
 
                         idDao.postId(id);
                         user.addId(id);
 
                     }
 
                 }
 
 
             }
 
         }
 
     }
 
     @Transactional(value = "cleia-txm", readOnly = true)
     public List<User> getUsernotPatient(GridRequest filters) {
 
         List<User> lUser = this.find(entityManager, "SELECT u FROM User u WHERE u.id not in (select distinct p.id from Patient p)" + filters.getQL("u", false), filters.getParamsValues(), filters.getStart(), filters.getLimit());
         if (lUser != null && !lUser.isEmpty()) {
             for (User u : lUser) {
                 u.getGroups().size();
                 u.getRoles().size();
                 u.getIds().size();
             }
         }
         return lUser;
 
     }
 
     /**
      * Obtiene el tamaño de {@link User}
      *
      * @param filters
      * @return Long
      */
     @Transactional(value = "cleia-txm", readOnly = true)
     public Long getUsernotPatientsize(GridRequest filters) {
         List<Long> result = this.find(entityManager, "SELECT count(*) FROM User u WHERE u.id not in (select distinct p.id from Patient p)" + filters.getQL("u", false), filters.getParamsValues());
         Long a = result.get(0);
         return result.get(0);
     }
 }
