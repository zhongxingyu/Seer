 package org.semtag.core.sim;
 
 import com.typesafe.config.Config;
import org.semtag.core.dao.ConceptDao;
 import org.semtag.core.dao.DaoException;
 import org.semtag.core.dao.DaoFilter;
 import org.semtag.core.dao.TagAppDao;
 import org.semtag.core.model.TagApp;
 import org.semtag.core.model.concept.Concept;
 import org.wikapidia.conf.Configuration;
 import org.wikapidia.conf.ConfigurationException;
 import org.wikapidia.conf.Configurator;
 
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * @author Ari Weiland
  */
 public class TagAppSimilarity implements Similar<TagApp> {
 
     private final TagAppDao helperDao;
    private final ConceptDao conceptDao;
     private final ConceptSimilarity sim;
 
    public TagAppSimilarity(TagAppDao helperDao, ConceptDao conceptDao, ConceptSimilarity sim) {
         this.helperDao = helperDao;
        this.conceptDao = conceptDao;
         this.sim = sim;
     }
 
     public TagAppDao getHelperDao() {
         return helperDao;
     }
 
     public ConceptSimilarity getSim() {
         return sim;
     }
 
     @Override
     public double similarity(TagApp x, TagApp y) throws DaoException {
        x.setConcept(conceptDao);
        y.setConcept(conceptDao);
         if (    x.getConcept() != null && y.getConcept() != null &&
                 x.getConcept().getMetric().equals(y.getConcept().getMetric())) {
             return sim.similarity(x.getConcept(), y.getConcept());
         } else if (x.getConceptId() == y.getConceptId() && x.getConceptId() > -1) {
             return 1.0;
         }
         return 0.0;
     }
 
     @Override
     public SimilarResultList mostSimilar(TagApp obj, int maxResults) throws DaoException {
        obj.setConcept(conceptDao);
         SimilarResultList concepts = sim.mostSimilar(obj.getConcept(), maxResults);
         Set<Integer> conceptIds = new HashSet<Integer>();
         for (SimilarResult result : concepts) {
             conceptIds.add(result.getIntId());
         }
         Iterable<TagApp> iterable = helperDao.get(new DaoFilter().setConceptIds(conceptIds));
         SimilarResultList list = new SimilarResultList(concepts.getMaxSize());
         for (TagApp t : iterable) {
             list.add(new SimilarResult(t.getTagAppId(), concepts.getValue(t.getTagAppId())));
         }
         list.lock();
         return list;
     }
 
     @Override
     public double[][] cosimilarity(TagApp[] objs) throws DaoException {
         Concept[] concepts = new Concept[objs.length];
         for (int i=0; i<objs.length; i++) {
            objs[i].setConcept(conceptDao);
             concepts[i] = objs[i].getConcept();
         }
         return sim.cosimilarity(concepts);
     }
 
     public static class Provider extends org.wikapidia.conf.Provider<TagAppSimilarity> {
         public Provider(Configurator configurator, Configuration config) throws ConfigurationException {
             super(configurator, config);
         }
 
         @Override
         public Class getType() {
             return TagAppSimilarity.class;
         }
 
         @Override
         public String getPath() {
             return "sem-tag.similar.tagApp";
         }
 
         @Override
         public TagAppSimilarity get(String name, Config config) throws ConfigurationException {
             if (!config.getString("type").equals("tagApp")) {
                 return null;
             }
             return new TagAppSimilarity(
                     getConfigurator().get(TagAppDao.class, config.getString("tagAppDao")),
                    getConfigurator().get(ConceptDao.class, config.getString("conceptDao")),
                    getConfigurator().get(ConceptSimilarity.class, config.getString("conceptSim"))
             );
         }
     }
 }
