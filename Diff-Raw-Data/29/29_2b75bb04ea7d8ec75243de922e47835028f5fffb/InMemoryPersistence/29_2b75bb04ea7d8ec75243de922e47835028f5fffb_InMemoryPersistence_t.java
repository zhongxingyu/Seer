 package edu.nus.tp.engine.saver;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.EnumMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.atomic.AtomicLong;
 
 import redis.clients.jedis.Transaction;
 
 import edu.nus.tp.engine.utils.Category;
 
 public class InMemoryPersistence implements Persistence {
 
 	private ConcurrentHashMap <String, AtomicLong> positiveTermMap = new ConcurrentHashMap <String, AtomicLong>();
 	private ConcurrentHashMap <String, AtomicLong> negativeTermMap = new ConcurrentHashMap <String, AtomicLong>();
 	private ConcurrentHashMap <String, AtomicLong> neutralTermMap = new ConcurrentHashMap <String, AtomicLong>();
 	
 	private Map<Category,AtomicLong> tweetCountByCategory=Collections.synchronizedMap(new EnumMap<Category, AtomicLong>(Category.class));
 	private Map<Category,AtomicLong> termCountByCategory=Collections.synchronizedMap(new EnumMap<Category, AtomicLong>(Category.class));
 	
 	public InMemoryPersistence() {
 		init();
 	}
 	
 	public void init(){
 		for (Category eachCategory : Category.values()) {
 			tweetCountByCategory.put(eachCategory, new AtomicLong(0));
 			termCountByCategory.put(eachCategory, new AtomicLong(0));
 		}
 	}
 	
 	@Override
 	public void saveTermsAndClassification(Collection<String> eachParsedTweet,
 			Category category) {
 
 		//for calculation of priors
 		tweetCountByCategory.get(category).incrementAndGet();
 		
 		for (String eachTerm : eachParsedTweet) {
 			
 			termCountByCategory.get(category).incrementAndGet();
 			
 			switch (category) {
 				case POSITIVE:
 					//TODO need to check if this is expensive
 					positiveTermMap.putIfAbsent(eachTerm, new AtomicLong(0));
 					positiveTermMap.get(eachTerm).incrementAndGet();
 					break;
 				
 				case NEGATIVE:
 					negativeTermMap.putIfAbsent(eachTerm, new AtomicLong(0));
 					negativeTermMap.get(eachTerm).incrementAndGet();
 					break;
 					
 				case NEUTRAL:
 					neutralTermMap.putIfAbsent(eachTerm, new AtomicLong(0));
 					neutralTermMap.get(eachTerm).incrementAndGet();
 					break;
 					
 				default:
 					break;
 				}
 			
 		}
 		
 		System.out.println("Done");
 	
 	}
 	
 	
 	public long getUniqueTermsInVocabulary(){
 		
 		Set<String> uniqueTermsInVocabulary=new HashSet<String>();
 		uniqueTermsInVocabulary.addAll(positiveTermMap.keySet());
 		uniqueTermsInVocabulary.addAll(negativeTermMap.keySet());
 		uniqueTermsInVocabulary.addAll(neutralTermMap.keySet());
 		
 		return uniqueTermsInVocabulary.size();
 		
 	}
 	
 	
 	
 	/**
 	 * Gets prior for a category
 	 * @param category
 	 * @return
 	 */
 	public double getPriorClassificationForCategory(Category category){
 		
 		long totalInstances=0,thisCategoryCount=0;
 		
 		for (Category eachCategory : Category.values()) {
 			totalInstances+=tweetCountByCategory.get(eachCategory).longValue();
 		}
 		
 		thisCategoryCount=tweetCountByCategory.get(category).longValue();
 		
 		return (double)thisCategoryCount/totalInstances;
 		
 	}
 	
 	
 	public long getTotalUniquePositiveTerms(){
 		return positiveTermMap.size();
 	}
 	
 	public long getTotalUniqueNegativeTerms(){
 		return negativeTermMap.size();
 	}
 	
 	public long getTotalUniqueNeutralTerms(){
 		return neutralTermMap.size();
 	}
 
 	
 	public long getTermFrequencyInCategory(String term, Category category){
 		
 		switch (category) {
 		
 		case POSITIVE: return positiveTermMap.get(term)==null?0:positiveTermMap.get(term).longValue();
 			
 		case NEGATIVE:  return negativeTermMap.get(term)==null?0:negativeTermMap.get(term).longValue();
 			
 		case NEUTRAL:  return neutralTermMap.get(term)==null?0:neutralTermMap.get(term).longValue();
 			
 		default: return 0;
 		}
 		
 	}
 
 	public long getTermCountByCategory(Category category) {
 		return termCountByCategory.get(category).longValue();
 	}
 
 	@Override
 	public double getSentiScoreForWord(String word) {
 		return 0;
 	}
 
 	@Override
 	public Transaction startBatch() {
		throw new RuntimeException("Invalid method");
 	}
 
 	@Override
 	public void endBatch(Transaction txn) {
		throw new RuntimeException("Invalid method");
	}

	@Override
	public void saveTermsAndClassificationBatch(
			Collection<String> eachParsedTweet, Category category,
			Transaction txn) {
		
		throw new RuntimeException("Invalid method");
		
 	}
 }
