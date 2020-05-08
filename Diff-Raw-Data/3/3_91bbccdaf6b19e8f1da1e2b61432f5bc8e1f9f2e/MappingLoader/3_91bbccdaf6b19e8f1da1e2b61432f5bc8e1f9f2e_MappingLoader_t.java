 package net.straininfo2.grs.idloader.db;
 
 import net.straininfo2.grs.idloader.TargetIdExtractor;
 import net.straininfo2.grs.idloader.bioproject.domain.BioProject;
 import net.straininfo2.grs.idloader.bioproject.domain.mappings.Mapping;
 import net.straininfo2.grs.idloader.bioproject.domain.mappings.Provider;
 import net.straininfo2.grs.idloader.bioproject.eutils.MappingHandler;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Map;
 
 public class MappingLoader implements MappingHandler {
 
     @Autowired
     SessionFactory factory;
 
     private Session session;
 
     private long count = 0;
 
     private final static Logger logger = LoggerFactory.getLogger(MappingHandler.class);
 
     private void checkTransaction() {
         count++;
         if (session == null) {
             session = factory.openSession();
             session.beginTransaction();
         }
         else if (count % 10000 == 0) {
             session.getTransaction().commit();
             session.close();
             session = factory.openSession();
            session.beginTransaction();
             count = 0;
         }
     }
 
     @Override
     public void addMapping(long bioProjectId, Mapping mapping, TargetIdExtractor extractor) {
         checkTransaction();
         BioProject project = (BioProject)session.get(BioProject.class, bioProjectId);
         if (project == null) {
             // this should not happen, as it should always be in the XML file, but trust no-one
             logger.error("Bioproject with id {} not found, cannot load mapping.", bioProjectId);
         }
         else {
             logger.info("Saving mapping {} for bioproject {}", mapping.getLinkName(), bioProjectId);
             mapping.computeTargetId(extractor);
             project.getMappings().add(mapping);
             mapping.setBioProject(project);
             session.merge(project);
         }
     }
 
     @Override
     public void handleMappings(long bioProjectId, Collection<Mapping> mappings, Map<Provider, TargetIdExtractor> extractors) {
         checkTransaction();
         BioProject project = (BioProject)session.get(BioProject.class, bioProjectId);
         if (project == null) {
             logger.error("Bioproject with id {} not found, cannot load mapping.", bioProjectId);
         }
         else {
             logger.info("Saving mappings for bioproject {}", bioProjectId);
             for (Mapping mapping : mappings) {
                 mapping.computeTargetId(extractors.get(mapping.getProvider()));
                 mapping.setBioProject(project);
             }
             if (Mapping.differentMapping(project.getMappings(), mappings)) {
                 // only do something if mappings changed
                 project.setMappings(new HashSet<>(mappings));
                 session.merge(project);
             }
         }
     }
 
     @Override
     public void endLoading() {
         try {
             session.getTransaction().commit();
             session.close();
         } catch (NullPointerException e) {
             // ignore, could happen if transaction already committed or no session yet (zero-input)
         }
     }
 
     public SessionFactory getFactory() {
         return factory;
     }
 
     public void setFactory(SessionFactory factory) {
         this.factory = factory;
     }
 }
