 package de.enwida.web.dao.implementation;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.datasource.DriverManagerDataSource;
 
 import de.enwida.web.dao.interfaces.BaseDao;
 import de.enwida.web.model.Group;
 
 public class RoleDao extends BaseDao<Group> implements IRoleDao {
     
     @Autowired
     private DriverManagerDataSource datasource;
 
 }
