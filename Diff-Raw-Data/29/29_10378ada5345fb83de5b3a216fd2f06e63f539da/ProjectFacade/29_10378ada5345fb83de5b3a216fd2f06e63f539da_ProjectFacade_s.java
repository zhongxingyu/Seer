 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.scrumble.server.sessionbeans;
 
 import com.scrumble.server.entities.Member1;
 import com.scrumble.server.entities.Project;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import javax.ejb.EJB;
 import javax.ejb.LocalBean;
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.TypedQuery;
 
 /**
  *
  * @author cyril
  */
 @Stateless
 @LocalBean
 public class ProjectFacade extends AbstractFacade<Project> implements ProjectFacadeLocal {
     @PersistenceContext(unitName = "com.scrumble.server_scrumble-server-ejb_ejb_1.0PU")
     private EntityManager em;
     
     @EJB 
     private Member1FacadeLocal memberLocal;
     
     @Override
     protected EntityManager getEntityManager() {
         return em;
     }
 
     public ProjectFacade() {
         super(Project.class);
     }
     
     public List<Project> quickSearch(String pattern){
         List<Project> results;
         
         //run named queries
         TypedQuery<Project> queryExact = getEntityManager().createNamedQuery("Project.quickSearchExact", Project.class);
         TypedQuery<Project> querySimple = getEntityManager().createNamedQuery("Project.quickSearchSimple", Project.class);
         
         //concatenate results
         results = queryExact.setParameter("pattern", pattern).getResultList();
         results.addAll(querySimple.setParameter("pattern", "%"+pattern+"%").getResultList());
         
         //supprimer les doublons
         Set set = new HashSet();
         set.addAll(results);
         
         return new ArrayList<Project>(set);
     }
     
     public List<Member1> findAllProjectMembers(Integer idProject){
         Project project = em.find(Project.class, idProject);  
         return new ArrayList<Member1>(project.getMember1Collection());
     }
     
     public List<Member1> findAllNotProjectMembers(Integer idProject){
         Project project = em.find(Project.class, idProject);
         //Member1Facade memberFacade = new Member1Facade();
         List<Member1> members = memberLocal.findAll();
         members.removeAll(project.getMember1Collection());
         return members;
     }
     
     public void addMemberToProject(Integer idProject, Integer idMember){
         Project project = em.find(Project.class, idProject); 
         Member1 member1 = em.find(Member1.class, idMember);
         if (!project.getMember1Collection().contains(member1)) {
             project.getMember1Collection().add(member1);
         }
     }
     
     public void removeMemberFromProject(Integer idProject, Integer idMember){
         Project project = em.find(Project.class, idProject); 
         Member1 member1 = em.find(Member1.class, idMember);
         if (project.getMember1Collection().contains(member1)) {
             project.getMember1Collection().remove(member1);
         }
     }
     
    public List<Project> findProjectByUser (Integer idMember) {
        Member1 member1 = em.find(Member1.class, idMember);
         return new ArrayList<Project>(member1.getProjectCollection());
     }
 }
