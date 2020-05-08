 package manager;
 
 import model.Region;
 
 import javax.ejb.LocalBean;
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.TypedQuery;
 import java.util.List;
 
 @Stateless
 @LocalBean
 
 public class RegionManager
 {
     @PersistenceContext
     EntityManager em;
 
     public List<Region> getRegions()
     {
         TypedQuery<Region> regionQuery = em.createQuery("SELECT r FROM Region r", Region.class);
         return regionQuery.getResultList();
     }
 
     public Region findRegion(String name)
     {
        TypedQuery<Region> regionQuery = sem.createQuery("SELECT r FROM Region r WHERE name = " + name, Region.class);
         return regionQuery.getSingleResult();
     }
 
     public Region getRegion(Integer id)
     {
         return em.find(Region.class, id);
     }
 
     public Region create(Region region)
     {
         em.persist(region);
         return region;
     }
 
     public Region update(Region region)
     {
         em.merge(region);
         return region;
     }
 
     public void delete(Integer id)
     {
         em.remove(getRegion(id));
     }
 }
