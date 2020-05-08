 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package be.kdg.groepi.dao;
 
 import be.kdg.groepi.model.Trip;
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 import org.springframework.dao.DataAccessException;
 import org.springframework.stereotype.Repository;
 
 /**
  * @author: Ben Oeyen
  * @date: 7-mrt-2013
  */
 @Repository
 public class TripDaoImpl implements TripDao {
 
     protected EntityManager entityManager;
 
     public EntityManager getEntityManager() {
         return entityManager;
     }
 
     @PersistenceContext
     public void setEntityManager(EntityManager entityManager) {
         this.entityManager = entityManager;
     }
 
     @Override
     public void createTrip(Trip user) throws DataAccessException {
         getEntityManager().persist(user);
     }
 
     @Override
     public void deleteTrip(Trip user) throws DataAccessException {
         getEntityManager().remove(user);
     }
 
     @Override
     public void updateTrip(Trip user) throws DataAccessException {
        getEntityManager().merge(user);
     }
 
     @Override
     public Trip getTripById(Long id) throws DataAccessException {
         return getEntityManager().find(Trip.class, id);
     }
 
     @Override
     public List<Trip> getAllTrips() throws DataAccessException {
         Query query = getEntityManager().createQuery("from Trip t");
         List<Trip> result = query.getResultList();
         return result;
     }
 
     @Override
     public List<Trip> getTripsByOrganiserId(long id) {
         Query query = getEntityManager().createQuery("from Trip t where t.fOrganiser.fId = :id");
         query.setParameter("id", id);
         List<Trip> result = query.getResultList();
         return result;
     }
 
     @Override
     public List<Trip> getPublicTrips() {
         Query query = getEntityManager().createQuery("from Trip t Where t.fAvailable= 1");
         List<Trip> result = query.getResultList();
         return result;
     }
 }
