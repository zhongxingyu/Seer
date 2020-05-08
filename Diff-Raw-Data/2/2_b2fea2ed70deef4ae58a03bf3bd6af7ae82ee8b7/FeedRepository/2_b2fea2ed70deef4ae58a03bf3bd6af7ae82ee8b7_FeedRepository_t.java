 package at.nullpointer.trayrss.persistence;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.inject.Inject;
 
 import lombok.Setter;
 
 import org.springframework.core.convert.ConversionService;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 
 import at.nullpointer.trayrss.domain.Feed;
 import at.nullpointer.trayrss.persistence.dao.FeedEntityRepository;
 import at.nullpointer.trayrss.persistence.dao.NewsEntityRepository;
 import at.nullpointer.trayrss.persistence.model.FeedEntity;
 
 /**
  * Provides functionality to access or store feed data
  * 
  * @author Thomas Pummer
  * @since 1.4
  */
 @Repository
 public class FeedRepository {
 
     /**
      * FeedEntityRepository
      */
     @Inject
     @Setter
     private FeedEntityRepository feedEntityRepository;
 
     /**
      * NewsEntityRepository
      */
     @Inject
     @Setter
     private NewsEntityRepository newsEntityRepository;
 
     /**
      * ConversionService
      */
     @Inject
     @Setter
     private ConversionService conversionService;
 
 
     /**
      * Retrieves the feed
      * 
     * @param feedUrl
      * @return feed
      */
     @Transactional
     public Feed retrieveFeed( final String feedUrl ) {
 
         final FeedEntity findOne = feedEntityRepository.findByUrl( feedUrl );
 
         final Feed result = conversionService.convert( findOne, Feed.class );
 
         return result;
     }
 
 
     /**
      * retrieves all feeds
      * 
      * @return Collection containing all feeds
      */
     @Transactional
     public Collection<Feed> retrieveFeeds() {
 
         final List<FeedEntity> findAll = feedEntityRepository.findAll();
         final List<Feed> result = new ArrayList<Feed>();
 
         for ( FeedEntity feed : findAll ) {
             result.add( conversionService.convert( feed, Feed.class ) );
         }
         return result;
     }
 
 
     /**
      * Removes a feed from persistence
      * 
      * @param feedUrl
      */
     @Transactional
     public void delete( final String feedUrl ) {
 
         FeedEntity feedToDelete = feedEntityRepository.findByUrl( feedUrl );
 
         feedEntityRepository.delete( feedToDelete );
 
     }
 
 
     /**
      * saves or updates the feed in persistence
      * 
      * @param feed
      * 
      */
     @Transactional
     public void saveOrUpdate( final Feed feed ) {
 
         FeedEntity feedEntityToPersist = feedEntityRepository.findByUrl( feed.getUrl() );
         if ( feedEntityToPersist == null ) {
             feedEntityToPersist = new FeedEntity();
         }
         feedEntityToPersist.setIntervall( feed.getIntervall() );
         feedEntityToPersist.setLastAction( feed.getLastAction() );
         feedEntityToPersist.setMonitored( feed.getMonitored() );
         feedEntityToPersist.setName( feed.getName() );
         feedEntityToPersist.setUrl( feed.getUrl() );
         feedEntityRepository.saveAndFlush( feedEntityToPersist );
 
     }
 }
