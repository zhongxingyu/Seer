 package cz.fi.muni.pa165.calorycounter.backend.dao.impl;
 
 import cz.fi.muni.pa165.calorycounter.backend.dao.CaloriesDao;
 import cz.fi.muni.pa165.calorycounter.backend.model.Calories;
 import cz.fi.muni.pa165.calorycounter.backend.model.Activity;
 import cz.fi.muni.pa165.calorycounter.serviceapi.dto.WeightCategory;
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceContext;
 import javax.persistence.TypedQuery;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Repository;
 
 /**
  * JPA/Hibernate DAO implementation - for operations on the persistence layer on
  * Calories entities.
  *
  * @author Jan Kucera (Greld)
  */
 @Repository
 public class CaloriesDaoImplJPA implements CaloriesDao {
 
     final static Logger log = LoggerFactory.getLogger(CaloriesDaoImplJPA.class);
     // injected from Spring
     @PersistenceContext
     private EntityManager em;
 
     public CaloriesDaoImplJPA() {
     }
 
     // this is only for legacy compatibility with some old tests
     public CaloriesDaoImplJPA(EntityManager em) {
         this.em = em;
     }
 
     @Override
     public Calories getByActivityWeightCat(Activity activity, WeightCategory weightCat) {
         if (activity == null || activity.getId() == null) {
             throw new IllegalArgumentException("Invalid activity: null or id is null");
         }
         if (weightCat == null) {
             throw new IllegalArgumentException("Invalid weightCat: null");
         }
         TypedQuery<Calories> query;
         try {
             query = em.createQuery("SELECT tbl FROM Calories tbl "
                     + "WHERE tbl.activity = :activity and tbl.weightCat = :weightCat", Calories.class);
             query.setParameter("activity", activity);
             query.setParameter("weightCat", weightCat);
         } catch (NoResultException nrex) {
             throw new IllegalArgumentException("Invalid activity or weightCat: Calories nonexistent", nrex);
         }
         return query.getSingleResult();
     }
 
     @Override
     public Long create(Calories calories) {
         if (validate(calories)) {
             throw new IllegalArgumentException("Invalid user: null or null username of user");
         }
         System.out.println("Creating " + calories.toString());
         log.debug("Creating " + calories.toString());
         Calories createdCalories = em.merge(calories);     // nechceme mu vratit manazovanu entitu, t.j. aby mohol robit zmeny mimo
         // vyhradenych CRUD operacii - to nechceme
         Long id = createdCalories.getId();
         log.debug("Created " + calories.toString() + ". Assigned ID: " + id);
         return id;
     }
 
     @Override
     public Calories get(Long id) {
         if (id == null) {
             throw new IllegalArgumentException("Invalid id: null");
         } else if (em.createQuery("SELECT tbl.id FROM Calories tbl WHERE tbl.id = "
                 + ":givenId", Long.class).setParameter("givenId", id).getResultList().size() < 1) {
             throw new IllegalArgumentException("Invalid id: nonexistent");
         }
         return em.createQuery("SELECT tbl FROM Calories tbl "
                 + "WHERE tbl.id = :givenId", Calories.class).setParameter("givenId", id).getSingleResult();
         // nechceme vracat manazovanu entitu (return em.find(AuthUser.class, id)), treba vyuzivat CRUD metody
     }
 
     @Override
     public void update(Calories calories) {
         if (validate(calories) || calories.getId() == null) {
             throw new IllegalArgumentException("Invalid calories: null or with no id.");
         } else if (em.createQuery("SELECT tbl.id FROM Calories tbl WHERE tbl.id = "
                 + ":givenId", Long.class).setParameter("givenId", calories.getId()).getResultList().size() < 1) {
             throw new IllegalArgumentException("Invalid calories: nonexistent");
         }
         em.merge(calories);
     }
 
     @Override
     public void remove(Long id) {
         if (id == null) {
             throw new IllegalArgumentException("Invalid calories: null or with no id.");
         }
         Calories foundCalories = em.find(Calories.class, id);
         if (foundCalories == null) {
             log.error("Calories is not in DB");
         }
         em.remove(foundCalories); // em.find je nutne, remove zmaze iba manazovanu entitu
         // je potrebne pri inverznej zavislosti osetrit pre-removal
     }
 
     private boolean validate(Calories calories) {
         return (calories == null || calories.getWeightCat() == null || calories.getActivity() == null);
     }
 
     @Override
     public List<Calories> getAll() {
        return em.createQuery("SELECT tbl FROM Calories tbl ORDER BY tbl.activity.name, tbl.weightCat", Calories.class).getResultList();
     }
 
     @Override
     public List<Calories> getByActivity(Activity activity) {
         if (activity == null || activity.getId() == null) {
             throw new IllegalArgumentException("Invalid activity: null or id is null");
         }
         TypedQuery<Calories> query;
         try {
             query = em.createQuery("SELECT tbl FROM Calories tbl "
                     + "WHERE tbl.activity = :activity", Calories.class);
             query.setParameter("activity", activity);
         } catch (NoResultException nrex) {
             throw new IllegalArgumentException("Invalid activity: Calories nonexistent");
         }
         return query.getResultList();
     }
 
     @Override
     public List<Calories> getByWeightCategory(WeightCategory weightCategory) {
         if (weightCategory == null) {
             throw new IllegalArgumentException("Invalid weight category: null");
         }
         TypedQuery<Calories> query;
         try {
             query = em.createQuery("SELECT tbl FROM Calories tbl "
                     + "WHERE tbl.weightCat = :weightCategory", Calories.class);
             query.setParameter("weightCategory", weightCategory);
         } catch (NoResultException nrex) {
             throw new IllegalArgumentException("Invalid activity: Calories nonexistent", nrex);
         }
         return query.getResultList();
     }
 }
