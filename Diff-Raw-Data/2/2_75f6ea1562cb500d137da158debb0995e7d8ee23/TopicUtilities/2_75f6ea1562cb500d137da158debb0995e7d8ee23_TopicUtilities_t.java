 package org.jboss.pressgang.ccms.model.utils;
 
 import static ch.lambdaj.Lambda.filter;
 import static ch.lambdaj.Lambda.having;
 import static ch.lambdaj.Lambda.on;
 import static ch.lambdaj.collection.LambdaCollections.with;
 import static org.hamcrest.Matchers.equalTo;
 
 import java.util.*;
 
 
 import org.jboss.pressgang.ccms.model.*;
 import org.jboss.pressgang.ccms.model.constants.Constants;
 import org.jboss.pressgang.ccms.model.sort.TagToCategorySortingComparator;
 import org.jboss.pressgang.ccms.utils.common.XMLUtilities;
 import org.jboss.pressgang.ccms.utils.structures.StringToNodeCollection;
 import org.w3c.dom.Document;
 
 import javax.persistence.EntityManager;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Predicate;
 import javax.persistence.criteria.Root;
 
 public class TopicUtilities {
 
     /**
      * Recalculate the min hash signature for a topic.
      * @param topic The topic to generate a signature for
      * @param minHashXORs The list of XOR values to apply to the hash code
      */
     public static void recalculateMinHash(final Topic topic, final List<MinHashXOR> minHashXORs) {
         final Set<MinHash> existingMinHashes = topic.getMinHashes();
 
         final Map<Integer, Integer> minHashes = getMinHashes(topic.getTopicXML(), minHashXORs);
 
         for (final Integer funcId : minHashes.keySet()) {
 
             boolean found = false;
             for (final MinHash minHash : existingMinHashes) {
                if (minHash.getMinHashFuncID().equals(funcId)) {
                     minHash.setMinHash(minHashes.get(funcId));
                     found = true;
                     break;
                 }
             }
 
             if (!found) {
                 final MinHash minHash = new MinHash();
                 minHash.setMinHashFuncID(funcId);
                 minHash.setMinHash(minHashes.get(funcId));
                 topic.addMinHash(minHash);
             }
         }
     }
 
     /**
      * Generate the min hashes
      * @param xml The content to apply the signature to
      * @param minHashXORs The list of XOR values to apply to the hash code
      * @return
      */
     public static Map<Integer, Integer> getMinHashes(final String xml, final List<MinHashXOR> minHashXORs) {
         final Map<Integer, Integer> retValue = new HashMap<Integer, Integer>();
 
         // If the xml is null then all min hashes are also going to be null, so just return an empty list.
         if (xml == null) {
             return retValue;
         }
 
         // the first minhash uses the builtin hashcode only
         final Integer baseMinHash = getMinHash(xml, null);
         if (baseMinHash != null) {
             retValue.put(0, baseMinHash);
         }
 
 
         for (int funcId = 1; funcId < org.jboss.pressgang.ccms.model.constants.Constants.NUM_MIN_HASHES; ++funcId) {
             boolean foundMinHash = false;
             for (final MinHashXOR minHashXOR : minHashXORs) {
                 if (minHashXOR.getMinHashXORFuncId() == funcId) {
                     final Integer minHash = getMinHash(xml, minHashXOR.getMinHashXOR());
                     if (minHash != null) {
                         retValue.put(funcId, minHash);
                     }
                     foundMinHash = true;
                     break;
                 }
             }
             if (!foundMinHash) {
                 throw new IllegalStateException("Did not find a minhash xor int for function " + funcId);
             }
         }
 
         return retValue;
     }
 
     /**
      * Returns the minimum hash of the sentences in an XML file.
      * @param xml The xml to analyse
      * @param xor the number to xor the hash against. Null if the standard hashCode() method should be used alone.
      * @return The minimum hash
      */
     public static Integer getMinHash(final String xml, final Integer xor) {
         if (xml == null) {
             return null;
         }
 
         final int SHINGLE_WORD_COUNT = 5;
 
         String text = null;
 
         try {
             final Document doc = XMLUtilities.convertStringToDocument(xml);
             if (doc != null) {
                 text = doc.getTextContent();
             }
         }
         catch (final Exception ex) {
 
         }
 
         // the xml was invalid, so just strip out xml elements manually
         if (text == null) {
             text = xml.replaceAll("<//?.*?//?>", " ");
         }
 
         // now generate the minhashes
         final String[] words = text.replaceAll("[^A-Za-z0-9]", " ").split("\\s+");
         final List<String> shingles = new ArrayList<String>();
 
         if (words.length < SHINGLE_WORD_COUNT) {
             final StringBuilder shingle = new StringBuilder();
             for (int i = 0; i < words.length; ++i) {
                 if (shingle.length() != 0) {
                     shingle.append(" ");
                 }
                 shingle.append(words[i]);
             }
             final int hash =  shingle.toString().hashCode();
             if (xor != null) {
                 return hash ^ xor;
             } else {
                 return hash;
             }
         } else {
             for (int i = 0; i < words.length - SHINGLE_WORD_COUNT + 1; ++i) {
                 final StringBuilder shingle = new StringBuilder();
                 for (int j = i; j < words.length && j < i + SHINGLE_WORD_COUNT; ++j) {
                     if (shingle.length() != 0) {
                         shingle.append(" ");
                     }
                     shingle.append(words[j]);
                 }
                 shingles.add(shingle.toString());
             }
 
             Integer minHash = null;
             for (final String string : shingles) {
                 final int hash = string.hashCode();
                 final int finalHash = xor != null ? hash ^ xor : hash;
                 if (minHash == null || finalHash < minHash) {
                     minHash = finalHash;
                 }
             }
             return minHash;
         }
     }
 
     public static List<Integer> getMatchingMinHash(final EntityManager entityManager, final Integer topicId, final Float threshold) {
         // get the source topic
         final Topic sourceTopic = entityManager.find(Topic.class, topicId);
         if (sourceTopic.getMinHashes().size() == 0) {
                 /*
                     If the source topic does not have a minhash signature, force the search query to
                     match a non existent topic id so no results are returned.
                  */
             return new ArrayList<Integer>(){{add(-1);}};
         }
 
         final Map<Integer, Integer> minhashes = new HashMap<Integer, Integer>();
         for (final MinHash minHash : sourceTopic.getMinHashes()) {
             minhashes.put(minHash.getMinHashFuncID(), minHash.getMinHash());
         }
 
         return getMatchingMinHash(entityManager, minhashes, threshold);
     }
 
     /**
      * Matching the minhash signature of a document relies on a process known as locality sensitive hashing.
      * A good explaination of this process can be found at http://infolab.stanford.edu/~ullman/mmds/ch3.pdf.
      *
      * To implement this feature, we need to group the minhash signatures into bands. When two topics share
      * the same minhash in a single band, they are considered a candidate pair for further testing.
      *
      * The documentation above suggests hashing the minhash values that fall into a band, and then comparing
      * these hashed band values to find candidates. Our implementation will defer this to the database by
      * finding the number of topics that have matching minhash values in all rows in a band.
      *
      * The number of rows and bands is calculated such that the threshold is approximately Math.pow(1/b, 1/r). This
      * formula means that increasing the threshold results in an increased number of rows and a decreased number
      * of bands. We get a close approximation by running through a bunch of combinations and seeing what fits best.
      *
      * @param entityManager The entity manager to use for queries
      * @param minhashes The minhash signature to match topics to
      * @param threshold How similar we want two documents to be to be considered a match. This will be forced to a value
      *                  between 0.6 and 0.9.
      * @return
      */
     public static List<Integer> getMatchingMinHash(final EntityManager entityManager, final Map<Integer, Integer> minhashes, final Float threshold) {
         try {
 
             if (minhashes.size() == 0) {
                 /*
                     If the source does not have a minhash signature, force the search query to
                     match a non existent topic id so no results are returned.
                  */
                 return new ArrayList<Integer>(){{add(-1);}};
             }
 
             Float fixedThreshold = Constants.MIN_DOCUMENT_SIMILARITY;
             if (threshold > Constants.MAX_DOCUMENT_SIMILARITY) {
                 fixedThreshold = Constants.MAX_DOCUMENT_SIMILARITY;
             } else if (threshold >= Constants.MIN_DOCUMENT_SIMILARITY) {
                 fixedThreshold = threshold;
             }
 
             int lhsRows = 0;
             for (int rows = Constants.LSH_SIXTY_PERCENT_ROWS; rows < Constants.LSH_NINETY_PERCENT_ROWS; ++rows) {
                 final int bands = Constants.NUM_MIN_HASHES / rows;
                 final double thisThreshold = Math.pow(1.0/bands, 1.0/rows);
 
                 if (rows != Constants.LSH_SIXTY_PERCENT_ROWS && thisThreshold > fixedThreshold) {
                     lhsRows = rows - 1;
                     break;
                 }
             }
 
             // The number of full bands that we be used to generate the sql query
             int bands = Constants.NUM_MIN_HASHES / lhsRows;
             // Any remaining topics that don't fall into a complete band
             if(Constants.NUM_MIN_HASHES % lhsRows != 0) {
                 ++bands;
             }
 
             final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
             final CriteriaQuery<Integer> criteriaQuery = criteriaBuilder.createQuery(Integer.class);
             final Root<MinHash> minHashRoot = criteriaQuery.from(MinHash.class);
 
             final Set<Integer> candidates = new HashSet<Integer>();
 
             /*
                 Note to self - It may be possible to perform these band queries as one big query through something like
                 cb.having(cb.equal(cb.toInteger(cb.prod(minHashRoot.<Integer>get("minHashFuncID"), 1.0/lhsRows), band).
                 However, unless lhsRows * bands exactly equals Constants.NUM_MIN_HASHES there needs to be some additional
                 logic for the last band, which will have less than lhsRows rows in it.
              */
 
             for (int band = 0; band < bands; ++band) {
                 final List<Predicate> rowMatches = new ArrayList<Predicate>();
                 for (int row = band * lhsRows; row < (band * lhsRows) + lhsRows && row < Constants.NUM_MIN_HASHES; ++row) {
                     Integer sourceMinHash = null;
                     for (final Integer minHashFuncID : minhashes.keySet()) {
                         if (row == minHashFuncID) {
                             sourceMinHash = minhashes.get(minHashFuncID);
                             break;
                         }
                     }
 
                     rowMatches.add(criteriaBuilder.and(
                             criteriaBuilder.equal(minHashRoot.<Integer>get("minHashFuncID"), row),
                             criteriaBuilder.equal(minHashRoot.<Integer>get("minHash"), sourceMinHash)
                     ));
                 }
 
                 final Predicate minHashOrs = criteriaBuilder.or(rowMatches.toArray(new Predicate[]{}));
 
                 final CriteriaQuery<Integer> query = criteriaQuery
                         .select(minHashRoot.<Topic>get("topic").<Integer>get("topicId"))
                         .distinct(true)
                         .where(minHashOrs)
                         .groupBy(minHashRoot.<Integer>get("topic").<Integer>get("topicId"))
                         .having(criteriaBuilder.equal(criteriaBuilder.count(minHashRoot.<Integer>get("topic").<Integer>get("topicId")), rowMatches.size()));
 
                 candidates.addAll(entityManager.createQuery(query).getResultList());
             }
 
             // at this point candidates should now list topic ids that are a potential match to the source topic.
             final CriteriaQuery<Topic> topicCQ = criteriaBuilder.createQuery(Topic.class);
             final Root<Topic> topicRoot = topicCQ.from(Topic.class);
             final CriteriaBuilder.In<Integer> in = criteriaBuilder.in(topicRoot.<Integer>get("topicId"));
             for (final Integer candidate : candidates) {
                 in.value(candidate);
             }
             final CriteriaQuery<Topic> topicQuery = topicCQ.select(topicRoot).where(in);
             final List<Topic> topics = entityManager.createQuery(topicQuery).getResultList();
 
             // we now have a list of topics that are possible candidates for a match. Now we compare the minhash values
             // to see what the similarity actually is.
             final List<Integer> matchingTopics = new ArrayList<Integer>();
             for (final Topic topic : topics) {
                 int matches = 0;
                 for (final Integer minHashFuncID : minhashes.keySet()) {
                     for (final MinHash otherMinHash : topic.getMinHashes()) {
                         if (minHashFuncID.equals(otherMinHash.getMinHashFuncID())) {
                             if (minhashes.get(minHashFuncID).equals(otherMinHash.getMinHash())) {
                                 ++matches;
                             }
                             break;
                         }
                     }
                 }
 
                 if ((float)matches / Constants.NUM_MIN_HASHES >= fixedThreshold) {
                     matchingTopics.add(topic.getId());
                 }
             }
 
 
 
             return matchingTopics;
 
         } catch (final Exception ex) {
             return null;
         }
     }
 
     /**
      * Validate and Fix a topics relationships to ensure that the topics related topics are still matched by the Related Topics
      * themselves.
      *
      * @param topic The topic to validate and fix the relationships for.
      */
     public static void validateAndFixRelationships(final Topic topic) {
         /* remove relationships to this topic in the parent collection */
         final ArrayList<TopicToTopic> removeList = new ArrayList<TopicToTopic>();
         for (final TopicToTopic topicToTopic : topic.getParentTopicToTopics())
             if (topicToTopic.getRelatedTopic().getTopicId().equals(topic.getTopicId())) removeList.add(topicToTopic);
 
         for (final TopicToTopic topicToTopic : removeList)
             topic.getParentTopicToTopics().remove(topicToTopic);
 
         /* remove relationships to this topic in the child collection */
         final ArrayList<TopicToTopic> removeChildList = new ArrayList<TopicToTopic>();
         for (final TopicToTopic topicToTopic : topic.getChildTopicToTopics())
             if (topicToTopic.getMainTopic().getTopicId().equals(topic.getTopicId())) removeChildList.add(topicToTopic);
 
         for (final TopicToTopic topicToTopic : removeChildList)
             topic.getChildTopicToTopics().remove(topicToTopic);
     }
 
     /**
      * Validate and Fix a topics tags so that mutually exclusive tags are enforced and also remove any tags that may have been
      * duplicated.
      *
      * @param topic The topic to fix the tags for.
      */
     public static void validateAndFixTags(final Topic topic) {
         /*
          * validate the tags that are applied to this topic. generally the gui should enforce these rules, with the exception of
          * the bulk tag apply function
          */
 
         // Create a collection of Categories mapped to TagToCategories, sorted by the Category sorting order
         final TreeMap<Category, ArrayList<TagToCategory>> tagDB = new TreeMap<Category, ArrayList<TagToCategory>>(
                 Collections.reverseOrder());
 
         for (final TopicToTag topicToTag : topic.getTopicToTags()) {
             final Tag tag = topicToTag.getTag();
             for (final TagToCategory tagToCategory : tag.getTagToCategories()) {
                 final Category category = tagToCategory.getCategory();
 
                 if (!tagDB.containsKey(category)) tagDB.put(category, new ArrayList<TagToCategory>());
 
                 tagDB.get(category).add(tagToCategory);
             }
         }
 
         // now remove conflicting tags
         for (final Category category : tagDB.keySet()) {
             /* sort by the tags position in the category */
             Collections.sort(tagDB.get(category), new TagToCategorySortingComparator(false));
 
             /*
              * because of the way we have ordered the tagDB collections, and the ArrayLists it contains, this process will
              * remove those tags that belong to lower priority categories, and lower priority tags in those categories
              */
 
             final ArrayList<TagToCategory> tagToCategories = tagDB.get(category);
 
             // remove tags in the same mutually exclusive categories
             if (category.isMutuallyExclusive() && tagToCategories.size() > 1) {
                 while (tagToCategories.size() > 1) {
                     final TagToCategory tagToCategory = tagToCategories.get(1);
                     /* get the lower priority tag */
                     final Tag removeTag = tagToCategory.getTag();
                     /* remove it from the tagDB collection */
                     tagToCategories.remove(tagToCategory);
 
                     /* and remove it from the tag collection */
                     final ArrayList<TopicToTag> removeTopicToTagList = new ArrayList<TopicToTag>();
                     for (final TopicToTag topicToTag : topic.getTopicToTags()) {
                         if (topicToTag.getTag().equals(removeTag)) removeTopicToTagList.add(topicToTag);
                     }
 
                     for (final TopicToTag removeTopicToTag : removeTopicToTagList) {
                         topic.getTopicToTags().remove(removeTopicToTag);
                     }
                 }
             }
 
             /* remove tags that are explicitly defined as mutually exclusive */
             for (final TagToCategory tagToCategory : tagToCategories) {
                 final Tag tag = tagToCategory.getTag();
                 for (final Tag exclusionTag : tag.getExcludedTags()) {
                     if (filter(having(on(TopicToTag.class).getTag(), equalTo(tagToCategory.getTag())),
                             topic.getTopicToTags()).size() != 0 && // make
                             /*
                              * sure that we have not removed this tag already
                              */
                             filter(having(on(TopicToTag.class).getTag(), equalTo(exclusionTag)),
                                     topic.getTopicToTags()).size() != 0 && // make
                             /*
                              * sure the exclusion tag exists
                              */
                             !exclusionTag.equals(tagToCategory.getTag())) // make
                     /*
                      * sure we are not trying to remove ourselves
                      */ {
                         with(topic.getTopicToTags()).remove(having(on(TopicToTag.class).getTag(), equalTo(exclusionTag)));
                     }
                 }
             }
         }
     }
 
     private TopicUtilities() {
     }
 }
