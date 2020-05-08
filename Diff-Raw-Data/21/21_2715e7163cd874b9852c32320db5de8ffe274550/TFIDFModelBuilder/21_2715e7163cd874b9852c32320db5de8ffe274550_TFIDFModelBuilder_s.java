 package org.grouplens.mooc.cbf;
 
 import com.google.common.collect.Maps;
 
 import it.unimi.dsi.fastutil.longs.LongSet;
 
 import org.grouplens.lenskit.core.Transient;
 import org.grouplens.lenskit.vectors.MutableSparseVector;
 import org.grouplens.lenskit.vectors.SparseVector;
 import org.grouplens.lenskit.vectors.VectorEntry;
 import org.grouplens.mooc.cbf.dao.ItemTagDAO;
 
 import javax.inject.Inject;
 import javax.inject.Provider;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Builder for computing {@linkplain TFIDFModel TF-IDF models} from item tag data.  Each item is
  * represented by a normalized TF-IDF vector.
  *
  * @author Kunal Taneja</a>
  */
 public class TFIDFModelBuilder implements Provider<TFIDFModel> {
     private final ItemTagDAO dao;
 
     /**
      * Construct a model builder.  The {@link Inject} annotation on this constructor tells LensKit
      * that it can be used to build the model builder.
      *
      * @param dao The item-tag DAO.  This is where the builder will get access to items and their
      *            tags.
      *            <p>{@link Transient} means that the provider promises that the DAO is no longer
      *            needed once the object is built (that is, the model will not contain a reference
      *            to the DAO).  This allows LensKit to configure your recommender components
      *            properly.  It's up to you to keep this promise.</p>
      */
     @Inject
     public TFIDFModelBuilder(@Transient ItemTagDAO dao) {
         this.dao = dao;
     }
 
     /**
      * This method is where the model should actually be computed.
      * @return The TF-IDF model (a model of item tag vectors).
      */
     @Override
     public TFIDFModel get() {
         // Build a map of tags to numeric IDs.  This lets you convert tags (which are strings)
         // into long IDs that you can use as keys in a tag vector.
         Map<String, Long> tagIds = buildTagIdMap();
 
         // Create a vector to accumulate document frequencies for the IDF computation
         MutableSparseVector docFreq = MutableSparseVector.create(tagIds.values());
         docFreq.fill(0);
 
         // We now proceed in 2 stages. First, we build a TF vector for each item.
         // While we do this, we also build the DF vector.
         // We will then apply the IDF to each TF vector and normalize it to a unit vector.
 
         // Create a map to store the item TF vectors.
         Map<Long,MutableSparseVector> itemVectors = Maps.newHashMap();
 
         // Create a work vector to accumulate each item's tag vector.
         // This vector will be re-used for each item.
         Collection<Long> tagIDs = tagIds.values();
         MutableSparseVector work = MutableSparseVector.create(tagIDs);        
         // Iterate over the items to compute each item's vector.
         LongSet items = dao.getItemIds();
         for (long item: items) {
             // Reset the work vector for this item's tags.
             work.clear();
             // Now the vector is empty (all keys are 'unset').
             // Populating the work vector with the number of times each tag is applied to this item.
             setWorkVectorWithTagFrequency(tagIds, work, item);
             // Incrementing the document frequency vector once for each unique tag on the item.
             for (Long key : work.keySet()) {
				if(key > 0)
 					docFreq.set(key, docFreq.get(key) + 1);
 			}
             // Save a shrunk copy of the vector (only storing tags that apply to this item) in
             // our map, we'll add IDF and normalize later.
             itemVectors.put(item, work.shrinkDomain());
             // work is ready to be reset and re-used for the next item
         }
 
         // Now we've seen all the items, so we have each item's TF vector and a global vector
         // of document frequencies.
         // Invert and log the document frequency.  We can do this in-place.
         Long numDocs = (long) docFreq.keySet().size();
         for (VectorEntry e: docFreq.fast()) {
             //Updating this document frequency entry to be a log-IDF value
         	double logIdf = Math.log(numDocs/e.getValue());
         	docFreq.set(e, logIdf);
         	
         }
 
         // Now docFreq is a log-IDF vector.
         // So we can use it to apply IDF to each item vector to put it in the final model.
         // Create a map to store the final model data.
         Map<Long,SparseVector> modelData = Maps.newHashMap();
         for (Map.Entry<Long,MutableSparseVector> entry: itemVectors.entrySet()) {
             MutableSparseVector tv = entry.getValue();
             // Converting this vector to a TF-IDF vector
             for (VectorEntry e : tv) {
             	double logIdf = docFreq.get(e.getKey());
             	double tf = e.getValue();
             	tv.set(e, tf*logIdf);
 			}
             
 
             // Normalizing the TF-IDF vector to be a unit vector
             // The method tv.norm() returns the Euclidian length of the vector
             double euc_length = tv.norm();
             for (VectorEntry e : tv) 
 				tv.set(e, e.getValue()/euc_length);
             // Store a frozen (immutable) version of the vector in the model data.
             modelData.put(entry.getKey(), tv.freeze());
         }
         // we technically don't need the IDF vector anymore, so long as we have no new tags
         return new TFIDFModel(tagIds, modelData);
     }
 
 	private void setWorkVectorWithTagFrequency(Map<String, Long> tagIds,
 			MutableSparseVector work, long item) {
 		List<String> tags = new ArrayList<String>(dao.getItemTags(item));
		Collections.sort(tags, new SortIgnoreCase());
 		String previousTag = "";
 		double frequency =0.0;
 		
 		for (String tag : tags) {
			if(tag.equalsIgnoreCase(previousTag)){
 				continue;
 			}
 			frequency = getFrequency(tag, tags);
 			System.out.println(tag + " " + frequency);
 			long key = tagIds.get(tag);
 			work.set(key, frequency);
 			previousTag = tag;
 		}
 	}
 
     private double getFrequency(String tag, List<String> tags) {
 		// TODO Auto-generated method stub
     	int index = tags.indexOf(tag);
     	double frequency=0;
     	for (int i=index;i < tags.size() ;i++){
    		if(tags.get(i).equalsIgnoreCase(tag))
     			frequency++;
     		else
     			break;
     	}
 		return frequency;
 	}
 
 	/**
      * Build a mapping of tags to numeric IDs.
      *
      * @return A mapping from tags to IDs.
      */
     private Map<String,Long> buildTagIdMap() {
         // Get the universe of all tags
         Set<String> tags = dao.getTagVocabulary();
         // Allocate our new tag map
         Map<String,Long> tagIds = Maps.newHashMap();
 
         for (String tag: tags) {
             // Map each tag to a new number.
             tagIds.put(tag, tagIds.size() + 1L);
         }
         return tagIds;
     }
     
    public class SortIgnoreCase implements Comparator<Object> {
        public int compare(Object o1, Object o2) {
            String s1 = (String) o1;
            String s2 = (String) o2;
            return s1.toLowerCase().compareTo(s2.toLowerCase());
        }
    }
 }
