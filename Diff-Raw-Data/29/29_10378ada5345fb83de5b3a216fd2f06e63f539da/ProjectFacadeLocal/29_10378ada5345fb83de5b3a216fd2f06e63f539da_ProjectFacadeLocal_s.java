 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.scrumble.server.sessionbeans;
 
 import com.scrumble.server.entities.Member1;
 import com.scrumble.server.entities.Project;
 import java.util.List;
 import javax.ejb.Local;
 
 /**
  *
  * @author cyril
  */
 @Local
 public interface ProjectFacadeLocal {
 
     void create(Project project);
 
     void edit(Project project);
 
     void remove(Project project);
 
     Project find(Object id);
 
     List<Project> findAll();
 
     List<Project> findRange(int[] range);
 
     int count();
     
     /** custom methods **/
     
     /**
      * QUICK SEARCH : search members within all Project object attributes
      * @param pattern a String to compare with any attributes content
      * @return a list with all matching Project object 
      */
     public List<Project> quickSearch(String pattern);
     
     /**
      * Get the list of all Member1 objects of a specified Project object
      * @param idProject the id of a specified Project object
      * @return a list of all members of the project 
      */
     public List<Member1> findAllProjectMembers(Integer idProject);
     
     /**
      * Get the list of all Member1 objects that are NOT linked with a specified Project object
      * @param idProject the id of a specified Project object
      * @return a list of members
      */
     public List<Member1> findAllNotProjectMembers(Integer idProject);    
     
     /**
      * Add a new member to a specified Project object
      * @param idProject the id of a specified Project object
      * @param idMember the id of a specified Member object 
      */
     public void addMemberToProject(Integer idProject, Integer idMember);
     
     /**
      * Remove a member from a specified Project object
      * @param idProject the id of a specified Project object
      * @param idMember the id of a specified Member object 
      */
     public void removeMemberFromProject(Integer idProject, Integer idMember);
     
     /**
      * View project if the member is included in
     * @param idMember the id of a specified Member object
      */
    public List<Project> findProjectByUser (Integer idMember);
     
 }
