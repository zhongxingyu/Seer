 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cz.muni.fi.pv243.mymaps.service;
 
 import cz.muni.fi.pv243.mymaps.dto.MyMap;
 import cz.muni.fi.pv243.mymaps.dto.User;
 import cz.muni.fi.pv243.mymaps.dto.View;
 import java.util.Collection;
 import java.util.List;
 
 /**
  *
  * @author Kuba
  */
 public interface ViewService {
 
     public void createView(View view);
    
     public void createView(View view, User user);
 
     public void updateView(View view);
 
     public void deleteView(View view);
     
     public View getViewById(Long id);
     
     public List<View> findViewsByName(String name);
 
     public View getViewByMap(MyMap myMap);
 
     public Collection<View> getViewsByUser(User user);
 }
