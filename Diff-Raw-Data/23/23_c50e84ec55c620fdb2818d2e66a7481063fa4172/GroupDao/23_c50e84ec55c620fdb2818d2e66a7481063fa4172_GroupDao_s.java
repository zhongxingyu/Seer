 package de.enwida.web.dao.implementation;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.datasource.DriverManagerDataSource;
 
 import de.enwida.web.dao.interfaces.BaseDao;
 import de.enwida.web.dao.interfaces.IGroupDao;
 import de.enwida.web.model.Group;
import de.enwida.web.model.User;
 
 public class GroupDao extends BaseDao<Group> implements IGroupDao {
     
     @Autowired
     private DriverManagerDataSource datasource;
 
 }
