 package edu.northwestern.bioinformatics.studycalendar.service;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
 import edu.northwestern.bioinformatics.studycalendar.domain.Source;
 import edu.northwestern.bioinformatics.studycalendar.xml.writers.ActivitySourceXmlSerializer;
 import org.springframework.beans.factory.annotation.Required;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 public class ImportActivitiesService {
     private SourceDao sourceDao;
     private ActivitySourceXmlSerializer xmlSerializer;
 
     public void loadAndSave(InputStream sourcesXml) throws Exception {
         Collection<Source> sources = readData(sourcesXml);
         List<Source> validSources = replaceCollidingSources(sources);
         
         save(validSources);
     }
 
     protected Collection<Source> readData(InputStream dataFile) throws Exception{
         return xmlSerializer.readCollectionDocument(dataFile);
     }
 
     protected List<Source> replaceCollidingSources(Collection<Source> sources) throws Exception {
         List<Source> validSources = new ArrayList<Source>();
         List<Source> existingSources = sourceDao.getAll();
 
         for (Source source : sources ) {
 
             if (existingSources.contains(source)) {
 
                 Source existingSource = existingSources.get(existingSources.indexOf(source));
 
                 for (Activity activity : source.getActivities()) {
                     activity.setSource(existingSource);
                 }
 
                for (Activity activity : source.getActivities()) {
                    if (!existingSource.getActivities().contains(activity)) {
                        existingSource.getActivities().add(activity);
                    }
                }
                
                 source = existingSource;
             }
 
             validSources.add(source);
         }
 
         return validSources;
     }
 
     protected void save(List<Source> sources) {
         for (Source source : sources)  {
             sourceDao.save(source);
         }
     }
 
     //// Field Setters
 
     @Required
     public void setSourceDao(SourceDao sourceDao) {
         this.sourceDao = sourceDao;
     }
 
     @Required
     public void setXmlSerializer(ActivitySourceXmlSerializer serializer) {
         xmlSerializer = serializer;
     }
 }
