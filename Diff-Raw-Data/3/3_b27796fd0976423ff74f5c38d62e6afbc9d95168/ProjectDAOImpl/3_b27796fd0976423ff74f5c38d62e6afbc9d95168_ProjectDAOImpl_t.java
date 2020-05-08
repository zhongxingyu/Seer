 package models;
 
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 import javax.persistence.*;
 import java.util.List;
 
 @Repository("projectDAO")
 @Transactional
 public class ProjectDAOImpl implements ProjectDAO {
     @PersistenceContext
     private EntityManager entityManager;
 
     @Override
     public Project save(Project project) {
         Project merge = entityManager.merge(project);
         entityManager.flush();
         project.setId(merge.getId());
         return project;
     }
 
     @Override
     public Project fetch(long id) {
         return entityManager.find(Project.class, id);
     }
 
     @Override
     public List<Project> fetchAllCurrent() {
         return entityManager.createQuery("From Project where status = 'CURRENT'").getResultList();
     }
 
     public void deleteAll() {
        Query deleteDonation = entityManager.createQuery("Delete From Donation");
         Query deleteQuery = entityManager.createQuery("Delete From Project");
        deleteDonation.executeUpdate();
         deleteQuery.executeUpdate();
     }
 
     @Override
     public Donation saveDonationToProject(Donation donation) {
         Donation donation1 = entityManager.merge(donation);
         entityManager.flush();
         return donation1;
     }
 }
