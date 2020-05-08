 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.scrumble.server.sessionbeans;
 
 import com.scrumble.server.entities.Userstory;
 import java.util.List;
 import javax.ejb.Local;
 
 /**
  *
  * @author cyril
  */
 @Local
 public interface UserstoryFacadeLocal {
 
     void create(Userstory userstory);
 
     void edit(Userstory userstory);
 
     void remove(Userstory userstory);
 
     Userstory find(Object id);
 
     List<Userstory> findAll();
 
     List<Userstory> findRange(int[] range);
 
     int count();
     
     /** custom methods **/
     
     /**
      * QUICK SEARCH : search members within all Userstory object attributes
      * @param pattern a String to compare with any attributes content
      * @return a list with all matching Userstory object 
      */
     public List<Userstory> quickSearch(String pattern);
     
     /**
      * findAllOrderByImportance : list all Userstory objects ordered by importance
      * @param 
      * @return a list of all Userstory objects ordered by importance
      */
     public List<Userstory> findAllOrderByImportance();

     
     /**
      * 
      * @param id
      * @param position 
      */
     public void updateImportance(Integer id, int position);

 }
