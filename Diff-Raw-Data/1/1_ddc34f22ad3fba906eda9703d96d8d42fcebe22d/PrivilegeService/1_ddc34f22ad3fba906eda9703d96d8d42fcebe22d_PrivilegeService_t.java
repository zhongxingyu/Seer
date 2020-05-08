 package cz.muni.fi.pv243.automoto.service;
 
 import cz.muni.fi.pv243.automoto.model.Privilege;
 
 import java.util.List;
 
 /**
  * @author Andrej Kuročenko <andrej@kurochenko.net>
  */
 
 public interface PrivilegeService {
 
     void create(Privilege privilege);
     void update(Privilege privilege);
     void delete(Privilege privilege);
     Privilege find(Long id);
     List<Privilege> findAll();
     int count();
 }
