 package org.decomposer.nlp.extraction;
 
 import org.decomposer.math.vector.MapVector;
 import org.decomposer.math.vector.VectorFactory;
 import org.decomposer.math.vector.array.ImmutableSparseMapVector;
 import org.decomposer.math.vector.hashmap.HashMapVectorFactory;
 import org.decomposer.nlp.extraction.FeatureDictionary.Feature;
 
 /**
 * The most basic implementation of the {@link org.decomposer.nlp.extraction.FeatureExtractor FeatureExractor} interface, which 
  * tokenizes input text by splitting on a given raw {@link java.lang.String String}, then uses a supplied 
  * {@link org.decomposer.nlp.extraction.FeatureDictionary FeatureDictionary} to look up the feature's index, and then weights 
  * the resultant {@link org.decomposer.math.vector.MapVector MapVector} by tf-idf weighting (where the idf is calculated based 
  * on a supplied {@link org.decomposer.nlp.extraction.Idf Idf} implementation.
  * @author jmannix
  */
 public class DelimitedDictionaryFeatureExtractor implements FeatureExtractor
 {
   protected final FeatureDictionary _featureDictionary;
   protected final String _delimiter;
   protected final Idf _idf;
   protected final VectorFactory _vectorFactory = new HashMapVectorFactory();
   
   /**
    * Uses the default delimiter of " ", and <code>Math.log(numFeatures + 1 / (count + 1))</code> as idf 
    * @param featureDictionary
    */
   public DelimitedDictionaryFeatureExtractor(FeatureDictionary featureDictionary)
   {
     this(featureDictionary, " ");
   }
   
   /**
    * Uses the <code>Math.log(numFeatures + 1 / (count + 1))</code> as idf 
    * @param featureDictionary
    * @param delimiter
    */
   public DelimitedDictionaryFeatureExtractor(final FeatureDictionary featureDictionary,
                                              String delimiter)
   {
     this(featureDictionary, delimiter, new Idf() 
     {
       public double idf(int count) 
       {
         return Math.log((featureDictionary.getNumFeatures() + 1) / (count + 1));
       }
     });
   }
   
   /**
    * 
    * @param featureDictionary
    * @param delimiter
    * @param idf
    */
   public DelimitedDictionaryFeatureExtractor(FeatureDictionary featureDictionary,
                                              String delimiter,
                                              Idf idf)
   {
     _featureDictionary = featureDictionary;
     _delimiter = delimiter;
     _idf = idf;
   }
     
   /**
    * 
    */
   public MapVector extract(String inputText) 
   {
     MapVector vector = _vectorFactory.zeroVector();
     String[] tokens = inputText.split(_delimiter);
     for(String token : tokens)
     {
       Feature feature = _featureDictionary.getFeature(token);
       if(feature != null) vector.add(feature.id, _idf.idf(feature.count));
     }
     return new ImmutableSparseMapVector(vector);
   }
 
 }
