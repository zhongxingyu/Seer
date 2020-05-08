 package edu.nus.tp.engine.saver;
 
 import static edu.nus.tp.engine.utils.Constants.NEGATIVE;
 import static edu.nus.tp.engine.utils.Constants.NEUTRAL;
 import static edu.nus.tp.engine.utils.Constants.PASSWORD;
 import static edu.nus.tp.engine.utils.Constants.POSITIVE;
 import static edu.nus.tp.engine.utils.Constants.REDIS_HOST;
 import static edu.nus.tp.engine.utils.Constants.REDIS_PORT;
 import static edu.nus.tp.engine.utils.Constants.TERM_COUNT_BY_CATEGORY;
 import static edu.nus.tp.engine.utils.Constants.TWEET_COUNT_BY_CATEGORY;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import redis.clients.jedis.Jedis;
 import redis.clients.jedis.Tuple;
 import edu.nus.tp.engine.utils.Category;
 
 public class RedisPersistence implements Persistence {
 
 	/*private ConcurrentHashMap <String, AtomicLong> positiveTermMap = new ConcurrentHashMap <String, AtomicLong>();
 	private ConcurrentHashMap <String, AtomicLong> negativeTermMap = new ConcurrentHashMap <String, AtomicLong>();
 	private ConcurrentHashMap <String, AtomicLong> neutralTermMap = new ConcurrentHashMap <String, AtomicLong>();*/
 
 	private Jedis jedis=null;
 
 	public RedisPersistence() {
 
		jedis=new Jedis(REDIS_HOST, REDIS_PORT);
		jedis.auth(PASSWORD);
		//jedis=new Jedis("127.0.0.1");
 		jedis.connect();
 	    System.out.println("Connected");
 	}
 
 	@Override
 	public void saveTermsAndClassification(Collection<String> eachParsedTweet,
 			Category category) {
 
 		//for calculation of priors
 		incrementTweetCountFor(category);
 
 		for (String eachTerm : eachParsedTweet) {
 
 			incrementTermCountFor(category);
 
 			switch (category) {
 				case POSITIVE:
 					//TODO need to check if this is expensive
 					/*positiveTermMap.putIfAbsent(eachTerm, new AtomicLong(0)); //init value set at 1 for Laplace smoothing
 					positiveTermMap.get(eachTerm).incrementAndGet();*/
 					addPositiveTerm(eachTerm);
 					break;
 
 				case NEGATIVE:
 					/*negativeTermMap.putIfAbsent(eachTerm, new AtomicLong(0));
 					negativeTermMap.get(eachTerm).incrementAndGet();*/
 					addNegativeTerm(eachTerm);
 					break;
 
 				case NEUTRAL:
 					/*neutralTermMap.putIfAbsent(eachTerm, new AtomicLong(0));
 					neutralTermMap.get(eachTerm).incrementAndGet();*/
 					addNeutralTerm(eachTerm);
 					break;
 
 				default:
 					break;
 				}
 
 		}
 
 		System.out.println("Done");
 
 	}
 
 
 	public long getUniqueTermsInVocabulary(){
 
 		Set<String> uniqueTermsInVocabulary=new HashSet<String>();
 		uniqueTermsInVocabulary.addAll(getTermsInClass(POSITIVE).keySet());
 		uniqueTermsInVocabulary.addAll(getTermsInClass(NEGATIVE).keySet());
 		uniqueTermsInVocabulary.addAll(getTermsInClass(NEUTRAL).keySet());
 
 		return uniqueTermsInVocabulary.size();
 
 	}
 
 
 
 	/**
 	 * Gets prior for a category
 	 * @param category
 	 * @return
 	 */
 	public double getPriorClassificationForCategory(Category category){
 
 		long totalInstances=0,thisCategoryCount=0;
 
 		Set<Tuple> tweetCountByCategory = jedis.zrangeWithScores(TWEET_COUNT_BY_CATEGORY, 0, -1);
 
 		for (Tuple eachCategory : tweetCountByCategory) {
 			totalInstances+=eachCategory.getScore();
 		}
 
 		Double thisCategoryCountDbl=jedis.zscore(TWEET_COUNT_BY_CATEGORY, category.toString());
 
 		thisCategoryCount=thisCategoryCountDbl==null?1:thisCategoryCountDbl.longValue();
 
 		return (double)thisCategoryCount/totalInstances;
 
 	}
 
 
 	public long getTotalUniquePositiveTerms(){
 		return getTermsInClass(POSITIVE).keySet().size();
 	}
 
 	public long getTotalUniqueNegativeTerms(){
 		return getTermsInClass(NEGATIVE).keySet().size();
 	}
 
 	public long getTotalUniqueNeutralTerms(){
 		return getTermsInClass(NEUTRAL).keySet().size();
 	}
 
 
 	public long getTermFrequencyInCategory(String term, Category category){
 
 		switch (category) {
 
 		case POSITIVE: return getTermFrequencyInClass(POSITIVE,term);
 
 		case NEGATIVE:  return getTermFrequencyInClass(NEGATIVE,term);
 
 		case NEUTRAL:  return getTermFrequencyInClass(NEUTRAL,term);
 
 		default: return 0;
 		}
 
 	}
 
 	private long getTermFrequencyInClass(String className, String term) {
 		String termFrequencyString=jedis.hget(className, term);
		return termFrequencyString==null?1:Long.parseLong(termFrequencyString);
 		//positiveTermMap.get(term)==null?1:positiveTermMap.get(term).longValue();
 	}
 
 	public long getTermCountByCategory(Category category) {
 		Double thisCategoryCountDbl=jedis.zscore(TERM_COUNT_BY_CATEGORY, category.toString());
 		return thisCategoryCountDbl==null?1:thisCategoryCountDbl.longValue();
 
 	}
 
 
 	private void incrementTermCountFor(Category category) {
 		jedis.zincrby(TERM_COUNT_BY_CATEGORY, 1, category.toString());
 	}
 
 
 	private void incrementTweetCountFor(Category category) {
 		jedis.zincrby(TWEET_COUNT_BY_CATEGORY, 1, category.toString());
 
 	}
 
 	private void addPositiveTerm(String term) {
 		addTermToClass(POSITIVE, term);
 	}
 
 	private void addNegativeTerm(String term) {
 		addTermToClass(NEGATIVE, term);
 	}
 
 	private void addNeutralTerm(String term) {
 		addTermToClass(NEUTRAL, term);
 	}
 
 	private void addTermToClass(String key, String term) {
 		jedis.hincrBy(key, term,1);
 	}
 
 	private Map<String,String> getTermsInClass(String className) {
 		return jedis.hgetAll(className);
 	}
 
 	//No lifecycle method here 
 	//FIXME Didn't disconnect any connections here 
 
 }
