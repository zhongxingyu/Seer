 package ru.deeper4k.space.dao;
 
 import org.springframework.stereotype.Repository;
 import ru.deeper4k.space.entity.Story;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import java.util.List;
 
 /**
  * Stories DAO  JPA implementation
  *
  * @author Albert Gazizov
  */
 @Repository
 public class StoryJpaDao implements StoryDao {
 
     /** Entity manager */
     @PersistenceContext
     private EntityManager _em;
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<Story> findAll() {
         return  _em.createQuery("Select distinct story from Story story").getResultList();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Story findById(Long aId) {
         return _em.find(Story.class, aId);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Story create(Story aStory) {
         _em.persist(aStory);
         return aStory;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void update(Story aStory) {
         _em.merge(aStory);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void remove(Long aId) {
        Story story = findById(aId);
        _em.remove(story);
     }
 }
