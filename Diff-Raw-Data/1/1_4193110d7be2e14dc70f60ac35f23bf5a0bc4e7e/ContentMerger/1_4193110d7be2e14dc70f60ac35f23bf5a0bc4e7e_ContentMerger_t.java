 package org.atlasapi.remotesite;
 
 import org.atlasapi.media.entity.Container;
 import org.atlasapi.media.entity.Content;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Identified;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Series;
 
 public class ContentMerger {
     
     public static Item merge(Item current, Item extracted) {
         current = mergeContents(current, extracted);
         current.setParentRef(extracted.getContainer());
         current.setVersions(extracted.getVersions());
         if (current instanceof Episode && extracted instanceof Episode) {
             Episode currentEp = (Episode) current;
             Episode extractedEp = (Episode) extracted;
             currentEp.setEpisodeNumber(extractedEp.getEpisodeNumber());
             currentEp.setSeriesRef(extractedEp.getSeriesRef());
         }        
         return current;
     }
 
     public static Container merge(Container current, Container extracted) {
         current = mergeContents(current, extracted);
         if (current instanceof Series && extracted instanceof Series) {
             ((Series) current).withSeriesNumber(((Series) extracted).getSeriesNumber());
             ((Series) current).setParentRef(((Series) extracted).getParent());
         }
         return current;
     }
 
     private static <C extends Content> C mergeContents(C current, C extracted) {
         current.setActivelyPublished(extracted.isActivelyPublished());
         current.setAliasUrls(extracted.getAliasUrls());
        current.setAliases(extracted.getAliases());
         current.setTitle(extracted.getTitle());
         current.setDescription(extracted.getDescription());
         current.setImage(extracted.getImage());
         current.setYear(extracted.getYear());
         current.setGenres(extracted.getGenres());
         current.setPeople(extracted.people());
         current.setLanguages(extracted.getLanguages());
         current.setCertificates(extracted.getCertificates());
         current.setMediaType(extracted.getMediaType());
         current.setSpecialization(extracted.getSpecialization());
         current.setLastUpdated(extracted.getLastUpdated());
         return current;
     }   
     
     public static Container asContainer(Identified identified) {
         return castTo(identified, Container.class);
     }
 
     public static Item asItem(Identified identified) {
         return castTo(identified, Item.class);
     }
 
     private static <T> T castTo(Identified identified, Class<T> cls) {
         try {
             return cls.cast(identified);
         } catch (ClassCastException e) {
             throw new ClassCastException(String.format("%s: expected %s got %s", 
                 identified.getCanonicalUri(), 
                 cls.getSimpleName(), 
                 identified.getClass().getSimpleName()));
         }
     }
 }
