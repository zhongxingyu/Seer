 package no.dusken.momus.service;
 
 import no.dusken.momus.model.Source;
 import no.dusken.momus.model.SourceTag;
 import no.dusken.momus.service.repository.SourceRepository;
 import no.dusken.momus.service.repository.SourceTagRepository;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.List;
 
 
 @Service
 public class SourceService {
 
     @Autowired
     private SourceRepository sourceRepository;
 
     @Autowired
     private SourceTagRepository tagRepository;
 
 
     public SourceRepository getSourceRepository() {
         return sourceRepository;
     }
 
     public SourceTagRepository getTagRepository() {
         return tagRepository;
     }
 
     /**
      * Will save any new tags as well
      */
     public Source save(Source source) {
         tagRepository.save(source.getTags());
         source = sourceRepository.save(source);
         return source;
     }
 
     /**
     * Will remove the tag from all sources where it has been used
      */
     public void deleteTag(SourceTag tag) {
         List<Source> sources = sourceRepository.findByTag(tag.getTag());
 
         for (Source source : sources) {
             source.getTags().remove(tag);
         }
         sourceRepository.save(sources);
 
         tagRepository.delete(tag);
     }
 
     /**
      * Basically adds the new tag to every source and removes the old one
      */
     public SourceTag updateTag(SourceTag oldTag, SourceTag newTag) {
         List<Source> sources = sourceRepository.findByTag(oldTag.getTag());
 
         SourceTag saved = tagRepository.save(newTag);
 
         for (Source source : sources) {
             source.getTags().remove(oldTag);
             source.getTags().add(newTag);
         }
         sourceRepository.save(sources);
 
         tagRepository.delete(oldTag);
         return saved;
     }
 }
