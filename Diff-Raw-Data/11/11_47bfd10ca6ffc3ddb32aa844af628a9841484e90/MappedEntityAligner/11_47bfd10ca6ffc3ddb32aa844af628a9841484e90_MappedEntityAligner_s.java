 package uk.ac.ebi.mdk.tool;
 
 import com.google.common.collect.ArrayListMultimap;
 import com.google.common.collect.ListMultimap;
 import org.apache.log4j.Logger;
 import uk.ac.ebi.mdk.domain.entity.Entity;
 import uk.ac.ebi.mdk.tool.match.EntityMatcher;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 /**
  * Entity aligner flattens all metrics (per matcher) into a single map which
  * is then searched. This is ideal for a 'match any' comparison.
  *
  * @author John May
  */
 public class MappedEntityAligner<E extends Entity> extends AbstractEntityAligner<E> {
 
     private static final Logger LOGGER = Logger.getLogger(MappedEntityAligner.class);
 
     private List<MetricMap> maps = new ArrayList<MetricMap>();
 
     public MappedEntityAligner() {
         super();
     }
 
     public MappedEntityAligner(Collection<E> references) {
         super(references);
     }
 
     public void build() {
     }
 
     public MetricMap getMetricMap(int i) {
         if (i >= maps.size() || maps.get(i) == null || maps.get(i).isEmpty()) {
             // build metric map
             MetricMap map = new MetricMap(references.size());
             for (E entity : references) {
                 map.put(entity, matchers.get(i));
             }
             maps.add(i, map);
         }
         return maps.get(i);
     }
 
     @Override
     public List<E> getMatches(E entity) {
 
         for (int i = 0; i < matchers.size(); i++) {
             // don't need to cache the query
            Object metric = matchers.get(0).calculatedMetric(entity);
             MetricMap map = getMetricMap(i);
 
             if (metric instanceof Collection) {
                 for (Object submetric : (Collection) metric) {
                     if (map.containsKey(submetric)) {
                         return map.get(submetric);
                     }
                 }
             }
 
         }
 
         return new ArrayList<E>();
     }
 
     private class MetricMap {
 
         private ListMultimap<Object, E> map;
 
         public MetricMap(int size) {
             map = ArrayListMultimap.create(size, 2);
         }
 
         public void put(E entity, EntityMatcher m) {
             Object metric = m.calculatedMetric(entity);
             if (metric instanceof Collection) {
                 for (Object submetric : (Collection) metric) {
                     map.put(submetric, entity);
                 }
             } else {
                 map.put(metric, entity);
             }
         }
 
         public boolean containsKey(Object o) {
             return map.containsKey(o);
         }
 
         public List<E> get(Object o) {
             return map.get(o);
         }
 
         public boolean isEmpty() {
             return map.isEmpty();
         }
 
     }
 
 }
