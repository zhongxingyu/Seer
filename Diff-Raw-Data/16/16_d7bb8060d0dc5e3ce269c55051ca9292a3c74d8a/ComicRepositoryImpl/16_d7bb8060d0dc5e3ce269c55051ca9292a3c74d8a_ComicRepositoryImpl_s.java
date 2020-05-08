 package org.lazydog.comic.internal.repository;
 
 import javax.annotation.PostConstruct;
 import javax.ejb.Local;
import javax.ejb.Singleton;
 import javax.interceptor.Interceptors;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import org.lazydog.comic.ComicRepository;
 import org.lazydog.repository.AbstractRepository;
 import org.lazydog.ejbmonitor.interceptor.EJBMonitor;
 
 
 /**
  * Comic repository Enterprise Java Bean.
  * 
  * @author  Ron Rickard
  */
@Singleton(name="ejb/ComicRepository")
 @Local(ComicRepository.class)
 @Interceptors(EJBMonitor.class)
 public class ComicRepositoryImpl extends AbstractRepository implements ComicRepository {
 
     @PersistenceContext
     private EntityManager entityManager;
 
     @PostConstruct
     private void initialize() {
         this.setEntityManager(this.entityManager);
     }
 }
