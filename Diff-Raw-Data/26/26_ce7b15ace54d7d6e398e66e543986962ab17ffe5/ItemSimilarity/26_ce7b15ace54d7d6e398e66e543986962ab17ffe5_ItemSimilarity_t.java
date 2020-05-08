 package org.semtag.core.sim;
 
 import com.typesafe.config.Config;
 import org.semtag.core.dao.DaoException;
 import org.semtag.core.dao.DaoFilter;
 import org.semtag.core.dao.TagAppDao;
 import org.semtag.core.model.Item;
 import org.semtag.core.model.TagApp;
 import org.semtag.core.model.TagAppGroup;
 import org.semtag.core.model.concept.Concept;
 import org.wikapidia.conf.Configuration;
 import org.wikapidia.conf.ConfigurationException;
 import org.wikapidia.conf.Configurator;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * @author Ari Weiland
  */
 public class ItemSimilarity implements Similar<Item> {
 
     private final TagAppDao helperDao;
     private final ConceptSimilarity sim;
 
     public ItemSimilarity(TagAppDao helperDao, ConceptSimilarity sim) {
         this.helperDao = helperDao;
         this.sim = sim;
     }
 
     public TagAppDao getHelperDao() {
         return helperDao;
     }
 
     public ConceptSimilarity getSim() {
         return sim;
     }
 
     @Override
     public double similarity(Item x, Item y) throws DaoException {
         TagAppGroup groupX = helperDao.getGroup(new DaoFilter().setItemId(x.getItemId()));
         TagAppGroup groupY = helperDao.getGroup(new DaoFilter().setItemId(y.getItemId()));
 
         // map concept IDs to concepts
         Map<Integer, Concept> concepts = new HashMap<Integer, Concept>();
         // maps X's concept IDs to amount they are tagged
         Map<Integer, Integer> mapX = new HashMap<Integer, Integer>();
         // maps Y's concept IDs to amount they are tagged
         Map<Integer, Integer> mapY = new HashMap<Integer, Integer>();
         for (TagApp t : groupX.getTagApps()) {
             int cId = t.getConceptId();
             concepts.put(cId, t.getConcept());
             int count = 1;
             if (mapX.containsKey(cId)) {
                 count = mapX.get(cId) + 1;
             }
             mapX.put(cId, count);
         }
         for (TagApp t : groupY.getTagApps()) {
             int cId = t.getConceptId();
             concepts.put(cId, t.getConcept());
             int count;
             if (mapY.containsKey(cId)) {
                 count = mapY.get(cId) + 1;
             } else {
                 count = 1;
             }
             mapY.put(cId, count);
         }
         int dim = concepts.size();
 
         // convert to vector form
         // concept vector space
         Integer[] vectorSpace = concepts.keySet().toArray(new Integer[dim]);
         // alpha vector representation of mapX values in above vector space
         int[] aX = new int[dim];
         // alpha vector representation of mapY values in above vector space
         int[] aY = new int[dim];
         for (int i=0; i<dim; i++) {
             int cId = vectorSpace[i];
             int count = 0;
             if (mapX.containsKey(cId)) {
                 count = mapX.get(cId);
             }
             aX[i] = count;
             count = 0;
             if (mapY.containsKey(cId)) {
                 count = mapY.get(cId);
             }
             aY[i] = count;
             i++;
         }
 
         // build similarity matrix and calculate beta vectors
         double[][] matrix = new double[dim][dim];
         double[] bX = new double[dim];
         double[] bY = new double[dim];
         for (int i=0; i<dim; i++) {
             for (int j=0; j<dim; j++) {
                 // For efficiency, only cosimilarity in the upper triangle of the matrix is calculated
                 if (i > j) {
                     matrix[i][j] = matrix[j][i];
                 } else {
                     matrix[i][j] = sim.similarity(concepts.get(vectorSpace[i]), concepts.get(vectorSpace[j]));
                 }
                 bX[i] += matrix[i][j] * aX[j];
                 bY[i] += matrix[i][j] * aY[j];
             }
         }
 
         // calculate and return cosine similarity between beta vectors
         double xDotX = 0.0;
         double yDotY = 0.0;
         double xDotY = 0.0;
         for (int i=0; i<dim; i++) {
             xDotX += bX[i] * bX[i];
             yDotY += bY[i] * bY[i];
             xDotY += bX[i] * bY[i];
         }
         return xDotY / Math.sqrt(xDotX * yDotY);
     }
 
     @Override
     public SimilarResultList mostSimilar(Item obj, int maxResults) throws DaoException {
         TagAppGroup group = helperDao.getGroup(new DaoFilter().setItemId(obj.getItemId()));
         Set<Integer> conceptIds = new HashSet<Integer>();
         for (TagApp t : group) {
             conceptIds.add(t.getConceptId());
             SimilarResultList conceptList = sim.mostSimilar(t.getConcept(), maxResults);
             for (SimilarResult result : conceptList) {
                 conceptIds.add(result.getIntId());
             }
         }
         Iterable<TagApp> iterable = helperDao.get(new DaoFilter().setConceptIds(conceptIds));
         SimilarResultList list = new SimilarResultList(maxResults);
         for (TagApp t : iterable) {
             Item item = t.getItem();
             list.add(new SimilarResult(item.getItemId(), similarity(obj, item)));
         }
         list.lock();
         return list;
     }
 
     @Override
     public double[][] cosimilarity(Item[] objs) throws DaoException {
         return new double[0][];
     }
 
     public static class Provider extends org.wikapidia.conf.Provider<ItemSimilarity> {
         public Provider(Configurator configurator, Configuration config) throws ConfigurationException {
             super(configurator, config);
         }
 
         @Override
         public Class getType() {
             return ItemSimilarity.class;
         }
 
         @Override
         public String getPath() {
             return "sem-tag.similar.item";
         }
 
         @Override
         public ItemSimilarity get(String name, Config config) throws ConfigurationException {
             if (!config.getString("type").equals("item")) {
                 return null;
             }
             return new ItemSimilarity(
                     getConfigurator().get(TagAppDao.class, config.getString("tagAppDao")),
                    getConfigurator().get(ConceptSimilarity.class, config.getString("conceptSim"))
             );
         }
     }
 }
