 package org.atlasapi.remotesite.btfeatured;
 
 import javax.annotation.Nonnull;
 
 import nu.xom.Element;
 
import com.metabroadcast.common.scheduling.UpdateProgress;

 import org.atlasapi.media.entity.Container;
 import org.atlasapi.media.entity.Content;
 
 import com.google.common.base.Optional;
 
 /**
  * Interface defining the behaviour required to process a product element.
  * @author andrewtoone
  *
  */
 public interface BtFeaturedContentProcessor {
     
     /**
      * Process an element, optionally returning an id of a further element to be processed
      * @param element
      * @return
      */
     public Optional<Content> process(Element element, @Nonnull Optional<Container> parent);
     
     public UpdateProgress getResult();
 
 }
