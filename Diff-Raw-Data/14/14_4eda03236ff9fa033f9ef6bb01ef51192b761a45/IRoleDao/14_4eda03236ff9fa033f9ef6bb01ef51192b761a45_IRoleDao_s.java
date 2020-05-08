 package de.enwida.web.dao.interfaces;
 
 import java.util.List;
 
 import de.enwida.web.model.Right;
 import de.enwida.web.model.Role;
 
 public interface IRoleDao extends IDao<Role> {
 
     Role fetchById(long id);
 
     Role addRole(Role role);
 
     void removeRole(Role role);
 
     List<Role> fetchAll();
 
    List<Right> getAllAspects(long roleID);
 
     Role save(Role role);
 
 }
