 package cz.fi.muni.pa165.calorycounter.backend.dao.impl;
 
 import cz.fi.muni.pa165.calorycounter.backend.dao.CaloriesDao;
 import cz.fi.muni.pa165.calorycounter.backend.model.Calories;
 import cz.fi.muni.pa165.calorycounter.backend.model.Activity;
 import cz.fi.muni.pa165.calorycounter.backend.model.WeightCategory;
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceContext;
 import javax.persistence.TypedQuery;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * JPA/Hibernate DAO implementation - for operations on the persistence layer on
  * Calories entities.
  *
  * @author Jan Kucera (Greld)
  */
 public class CaloriesDaoImplJPA implements CaloriesDao {
     
     final static Logger log = LoggerFactory.getLogger(CaloriesDaoImplJPA.class);
     
     @PersistenceContext(name = "PU1")
     private EntityManager em;
 
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
             throw new IllegalArgumentException("Invalid activity or weightCat: Calories nonexistent");
         }
         return query.getSingleResult();
     }
 
     @Override
    public Long create(Calories calories) {
         if (validate(calories)) {
             throw new IllegalArgumentException("Invalid user: null or null username of user");
         }
        Calories createdCalories = em.merge(calories);     // nechceme mu vratit manazovanu entitu, t.j. aby mohol robit zmeny mimo
         // vyhradenych CRUD operacii - to nechceme
        return createdCalories.getId();
     }
 
     @Override
     public Calories get(Long id) {
         if (id == null) {
             throw new IllegalArgumentException("Invalid id: null");
         } else if (em.createQuery("SELECT tbl.id FROM Calories tbl WHERE tbl.id = "
                 + ":givenId", Long.class).setParameter("givenId", id).getResultList().size() < 1) {
             throw new IllegalArgumentException("Invalid id: nonexistent");
         }
         return em.createQuery("SELECT tbl FROM Calories tbl"
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
     public void remove(Calories calories) {
         if (validate(calories) || calories.getId() == null) {
             throw new IllegalArgumentException("Invalid calories: null or with no id.");
         }
         Calories foundCalories = em.find(Calories.class, calories.getId());
         if(foundCalories == null) {
             log.error("Calories is not in DB");
         }
         em.remove(calories);                    // em.find je nutne, remove zmaze iba manazovanu entitu
         // je potrebne pri inverznej zavislosti osetrit pre-removal
     }
 
     
     private boolean validate(Calories calories) {
         return (calories == null || calories.getWeightCat() == null || calories.getActivity() == null);
     }
 }
