 package org.tongji.mahoutplatform.recommender.evaluation;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.apache.mahout.cf.taste.common.NoSuchItemException;
 import org.apache.mahout.cf.taste.common.NoSuchUserException;
 import org.apache.mahout.cf.taste.common.TasteException;
 import org.apache.mahout.cf.taste.eval.DataModelBuilder;
 import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
 import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
 import org.apache.mahout.cf.taste.impl.common.FullRunningAverageAndStdDev;
 import org.apache.mahout.cf.taste.impl.common.RunningAverageAndStdDev;
 import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
 import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
 import org.apache.mahout.cf.taste.model.DataModel;
 import org.apache.mahout.cf.taste.model.Preference;
 import org.apache.mahout.cf.taste.model.PreferenceArray;
 import org.apache.mahout.cf.taste.recommender.Recommender;
 import org.apache.mahout.common.RandomUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.tongji.mahoutplatform.recommender.data.KFoldCrossFileDataModel;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Lists;
 
 public abstract class AbstractKFoldCrossRecommenderEvaluator {
   
   private static final Logger log = LoggerFactory.getLogger(AbstractKFoldCrossRecommenderEvaluator.class);
   
   private final Random random;
   private float maxPreference;
   private float minPreference;
   private int kFold;
   
   
   public AbstractKFoldCrossRecommenderEvaluator() {
     random = RandomUtils.getRandom();
     maxPreference = Float.NaN;
     minPreference = Float.NaN;
   }
   
   public final float getMaxPreference() {
     return maxPreference;
   }
   
   public final void setMaxPreference(float maxPreference) {
     this.maxPreference = maxPreference;
   }
   
   public final float getMinPreference() {
     return minPreference;
   }
   
   public final void setMinPreference(float minPreference) {
     this.minPreference = minPreference;
   }
   
   public double evaluate(RecommenderBuilder recommenderBuilder,
           DataModelBuilder dataModelBuilder,
           DataModel dataModel,
           int kFold) throws TasteException{
 	  return this.evaluate(recommenderBuilder, dataModelBuilder, dataModel, kFold, 1.0);
   }
   
   
   public double evaluate(RecommenderBuilder recommenderBuilder,
   		DataModelBuilder dataModelBuilder, DataModel dataModel, int kFold,
   		double evaluationPercentage) throws TasteException {
   	Preconditions.checkNotNull(recommenderBuilder);
   	Preconditions.checkNotNull(dataModel);
   	Preconditions.checkArgument(kFold > 0, "Invalid kFold: " + kFold);
   	Preconditions.checkArgument(evaluationPercentage >= 0.0
   			&& evaluationPercentage <= 1.0,
   			"Invalid evaluationPercentage: " + evaluationPercentage);
   	this.kFold = kFold;
   
   	log.info("Beginning evaluation using {} FoldCross of {}", kFold,
   			dataModel);
   
   	int numPrefs = ((KFoldCrossFileDataModel) dataModel).getNumPrefs();
   	int eachFoldNumPrefs = numPrefs / kFold;
   	ArrayList<Preference> allPrefs = (ArrayList<Preference>) ((KFoldCrossFileDataModel) dataModel)
   			.getAllPrefs();
   
   	int numUsers = dataModel.getNumUsers();
   	List<FastByIDMap<PreferenceArray>> allTrainingPrefs = new ArrayList<FastByIDMap<PreferenceArray>>();
   	for (int i = 0; i < kFold; i++) {
   		allTrainingPrefs.add(new FastByIDMap<PreferenceArray>(
   				1 + (int) (evaluationPercentage * numUsers)));
   	}
   	FastByIDMap<PreferenceArray> testPrefs = null;
   
   	int randomNums[] = generateRandomNums(numPrefs);
   	for (int i = 0; i < kFold; i++) {
   		for (int j = i * eachFoldNumPrefs; j < ((i + 1) * eachFoldNumPrefs); j++) {
   			Preference pref = allPrefs.get(randomNums[j]);
   			long userID = pref.getUserID();
   			long itemID = pref.getItemID();
   			if (allTrainingPrefs.get(i).containsKey(userID)) {
   				PreferenceArray prefArray = allTrainingPrefs.get(i).get(
   						userID);
   				PreferenceArray newPrefArray = new GenericUserPreferenceArray(
   						prefArray.length() + 1);
   				for (int k = 0; k < prefArray.length(); k++) {
   					newPrefArray.setItemID(k, prefArray.getItemID(k));
   					newPrefArray.setValue(k, prefArray.getValue(k));
   				}
   				newPrefArray.setUserID(0, userID);
   				newPrefArray.setItemID(prefArray.length(), itemID);
   				newPrefArray.setValue(prefArray.length(), pref.getValue());
   				allTrainingPrefs.get(i).remove(userID);
   				allTrainingPrefs.get(i).put(userID, newPrefArray);
   			} else {
   				PreferenceArray prefArray = new GenericUserPreferenceArray(
   						1);
   				prefArray.setUserID(0, userID);
   				prefArray.setItemID(0, itemID);
   				prefArray.setValue(0, pref.getValue());
   				allTrainingPrefs.get(i).put(userID, prefArray);
   			}
   		}
   	}
   	
  	for(int i = (kFold * eachFoldNumPrefs); i < numPrefs; i++){
 	    Preference pref = allPrefs.get(randomNums[i]);
 	    long userID = pref.getUserID();
 	    long itemID = pref.getItemID();
 	    FastByIDMap<PreferenceArray> trainingPrefs = allTrainingPrefs.get(kFold - 1);
 	    if(trainingPrefs.containsKey(userID)){
 	      PreferenceArray prefArray = trainingPrefs.get(userID);
 	      PreferenceArray newPrefArray = new GenericUserPreferenceArray(prefArray.length() + 1);
 	      for(int k = 0; k < prefArray.length(); k++){
 	        newPrefArray.setItemID(k, prefArray.getItemID(k));
 	        newPrefArray.setValue(k, prefArray.getValue(k));
 	      }
 	      newPrefArray.setUserID(0, userID);
 	      newPrefArray.setItemID(prefArray.length(), itemID);
 	      newPrefArray.setValue(prefArray.length(), pref.getValue());
 	      trainingPrefs.remove(userID);
 	      trainingPrefs.put(userID, newPrefArray);
 	    }else{
 	      PreferenceArray prefArray = new GenericUserPreferenceArray(1);
 	      prefArray.setUserID(0, userID);
 	      prefArray.setItemID(0, itemID);
 	      prefArray.setValue(0, pref.getValue());
 	      trainingPrefs.put(userID, prefArray);
 	    }
 	  }
   	
   	double result = 0;
     for(int i = 0; i < kFold; i++){
       testPrefs = allTrainingPrefs.get(i);
       FastByIDMap<PreferenceArray> trainingPrefs = new FastByIDMap<PreferenceArray>(
               1 + (int) (evaluationPercentage * numUsers));
       for(int j = 0; j < kFold; j++){
         if(j != i){
           FastByIDMap<PreferenceArray> trainingPrefsPart = allTrainingPrefs.get(j);
           for(Map.Entry<Long, PreferenceArray> entry : trainingPrefsPart.entrySet()){
             Long userID = entry.getKey();
             PreferenceArray prefArray = entry.getValue();
             if(trainingPrefs.containsKey(userID)){
               PreferenceArray originalPrefArray = trainingPrefs.get(userID);
               PreferenceArray newPrefArray = new GenericUserPreferenceArray(originalPrefArray.length() + prefArray.length());
               newPrefArray.setUserID(0, userID);
               for(int k = 0; k < originalPrefArray.length(); k++){
                 newPrefArray.setItemID(k, originalPrefArray.getItemID(k));
                 newPrefArray.setValue(k, originalPrefArray.getValue(k));
               }
               for(int k = originalPrefArray.length(); k < (prefArray.length() + originalPrefArray.length()); k++){
                 newPrefArray.setItemID(k, prefArray.getItemID(k - originalPrefArray.length()));
                 newPrefArray.setValue(k, prefArray.getValue(k - originalPrefArray.length()));
               }
               trainingPrefs.remove(userID);
               trainingPrefs.put(userID, newPrefArray);
             }else{
                trainingPrefs.put(userID, prefArray);
             }
           }
         }
       }
       DataModel trainingModel = dataModelBuilder == null ? new GenericDataModel(trainingPrefs)
             : dataModelBuilder.buildDataModel(trainingPrefs);
       Recommender recommender = recommenderBuilder.buildRecommender(trainingModel);
       result += getEvaluation(testPrefs, recommender);
     }
     log.info("Evaluation result: {}", result / kFold);
     return result / kFold;
   }
   
   /*protected double evaluate(RecommenderBuilder recommenderBuilder,
                          DataModelBuilder dataModelBuilder,
                          DataModel dataModel,
                          double trainingPercentage,
                          double evaluationPercentage) throws TasteException {
     Preconditions.checkNotNull(recommenderBuilder);
     Preconditions.checkNotNull(dataModel);
     Preconditions.checkArgument(trainingPercentage >= 0.0 && trainingPercentage <= 1.0,
       "Invalid trainingPercentage: " + trainingPercentage);
     Preconditions.checkArgument(evaluationPercentage >= 0.0 && evaluationPercentage <= 1.0,
       "Invalid evaluationPercentage: " + evaluationPercentage);
 
     log.info("Beginning evaluation using {} of {}", trainingPercentage, dataModel);
     
     
     int numPrefs = ((ImproveFileDataModel)dataModel).getNumPrefs();
     int eachFoldNumPrefs = numPrefs / kFold;
     ArrayList<Preference> allPrefs = (ArrayList<Preference>)((ImproveFileDataModel)dataModel).getAllPrefs();
     
     int numUsers = dataModel.getNumUsers();
     List<FastByIDMap<PreferenceArray>> allTrainingPrefs = new ArrayList<FastByIDMap<PreferenceArray>>();
     for(int i = 0; i < kFold; i++){
     	allTrainingPrefs.add(new FastByIDMap<PreferenceArray>(1 + (int) (evaluationPercentage * numUsers)));
     }
 	FastByIDMap<PreferenceArray> testPrefs = null;
 	
 	int randomNums[] = generateRandomNums(numPrefs);
 	for(int i = 0; i < kFold; i++){
 		for(int j = i * eachFoldNumPrefs; j < ((i + 1) * eachFoldNumPrefs); j++){
 			Preference pref = allPrefs.get(randomNums[j]);
 			long userID = pref.getUserID();
 			long itemID = pref.getItemID();
 			if(allTrainingPrefs.get(i).containsKey(userID)){
 				PreferenceArray prefArray = allTrainingPrefs.get(i).get(userID);
 				PreferenceArray newPrefArray = new GenericUserPreferenceArray(prefArray.length() + 1);
 				for(int k = 0; k < prefArray.length(); k++){
 					newPrefArray.setItemID(k, prefArray.getItemID(k));
 					newPrefArray.setValue(k, prefArray.getValue(k));
 				}
 				newPrefArray.setUserID(0, userID);
 				newPrefArray.setItemID(prefArray.length(), itemID);
 				newPrefArray.setValue(prefArray.length(), pref.getValue());
 				allTrainingPrefs.get(i).remove(userID);
 				allTrainingPrefs.get(i).put(userID, newPrefArray);
 			}else{
 				PreferenceArray prefArray = new GenericUserPreferenceArray(1);
 				prefArray.setUserID(0, userID);
 				prefArray.setItemID(0, itemID);
 				prefArray.setValue(0, pref.getValue());
 				allTrainingPrefs.get(i).put(userID, prefArray);
 			}
 		}
 	}
 	
 	for(int i = (kFold - 1) * eachFoldNumPrefs; i < numPrefs; i++){
 		Preference pref = allPrefs.get(randomNums[i]);
 		long userID = pref.getUserID();
 		long itemID = pref.getItemID();
 		FastByIDMap<PreferenceArray> trainingPrefs = allTrainingPrefs.get(kFold - 1);
 		if(trainingPrefs.containsKey(userID)){
 			PreferenceArray prefArray = trainingPrefs.get(userID);
 			PreferenceArray newPrefArray = new GenericUserPreferenceArray(prefArray.length() + 1);
 			for(int k = 0; k < prefArray.length(); k++){
 				newPrefArray.setItemID(k, prefArray.getItemID(k));
 				newPrefArray.setValue(k, prefArray.getValue(k));
 			}
 			newPrefArray.setUserID(0, userID);
 			newPrefArray.setItemID(prefArray.length(), itemID);
 			newPrefArray.setValue(prefArray.length(), pref.getValue());
 			trainingPrefs.remove(userID);
 			trainingPrefs.put(userID, newPrefArray);
 		}else{
 			PreferenceArray prefArray = new GenericUserPreferenceArray(1);
 			prefArray.setUserID(0, userID);
 			prefArray.setItemID(0, itemID);
 			prefArray.setValue(0, pref.getValue());
 			trainingPrefs.put(userID, prefArray);
 		}
 	}
 	
 	double result = 0;
 	for(int i = 0; i < kFold; i++){
 		testPrefs = allTrainingPrefs.get(i);
 		FastByIDMap<PreferenceArray> trainingPrefs = new FastByIDMap<PreferenceArray>(
 		        1 + (int) (evaluationPercentage * numUsers));
 		for(int j = 0; j < kFold; j++){
 			if(j != i){
 				FastByIDMap<PreferenceArray> trainingPrefsPart = allTrainingPrefs.get(j);
 				for(Map.Entry<Long, PreferenceArray> entry : trainingPrefsPart.entrySet()){
 					Long userID = entry.getKey();
 					PreferenceArray prefArray = entry.getValue();
 					if(trainingPrefs.containsKey(userID)){
 						PreferenceArray originalPrefArray = trainingPrefs.get(userID);
 						PreferenceArray newPrefArray = new GenericUserPreferenceArray(originalPrefArray.length() + prefArray.length());
 						newPrefArray.setUserID(0, userID);
 						for(int k = 0; k < originalPrefArray.length(); k++){
 							newPrefArray.setItemID(k, originalPrefArray.getItemID(k));
 							newPrefArray.setValue(k, originalPrefArray.getValue(k));
 						}
 						for(int k = originalPrefArray.length(); k < (prefArray.length() + originalPrefArray.length()); k++){
 							newPrefArray.setItemID(k, prefArray.getItemID(k - originalPrefArray.length()));
 							newPrefArray.setValue(k, prefArray.getValue(k - originalPrefArray.length()));
 						}
 						trainingPrefs.remove(userID);
 						trainingPrefs.put(userID, newPrefArray);
 					}else{
 					   trainingPrefs.put(userID, prefArray);
 					}
 				}
 			}
 		}
 		DataModel trainingModel = dataModelBuilder == null ? new GenericDataModel(trainingPrefs)
         	: dataModelBuilder.buildDataModel(trainingPrefs);
     
 		Recommender recommender = recommenderBuilder.buildRecommender(trainingModel);
     
     	result += getEvaluation(testPrefs, recommender);
     	log.info("Evaluation result: {}", result);
 	}
     return result / kFold;
   }*/
   
   private int[] generateRandomNums(int num){
 	  Object[] randomNumsTemp = null;
 	  HashSet<Integer> hashSet = new HashSet<Integer>();
 	  while(hashSet.size() < num){
           hashSet.add(random.nextInt(num));
       }
 	  randomNumsTemp = hashSet.toArray();
 	  int randomNums[] = new int[num];
 	  for(int i = 0; i < num; i++){
 		  randomNums[i] = Integer.parseInt(String.valueOf(randomNumsTemp[i]));
 	  }
 	  return randomNums;
   }
   
   private float capEstimatedPreference(float estimate) {
     if (estimate > maxPreference) {
       return maxPreference;
     }
     if (estimate < minPreference) {
       return minPreference;
     }
     return estimate;
   }
 
   private double getEvaluation(FastByIDMap<PreferenceArray> testPrefs, Recommender recommender)
     throws TasteException {
     reset();
     Collection<Callable<Void>> estimateCallables = Lists.newArrayList();
     AtomicInteger noEstimateCounter = new AtomicInteger();
     for (Map.Entry<Long,PreferenceArray> entry : testPrefs.entrySet()) {
       estimateCallables.add(
           new PreferenceEstimateCallable(recommender, entry.getKey(), entry.getValue(), noEstimateCounter));
     }
     log.info("Beginning evaluation of {} users", estimateCallables.size());
     RunningAverageAndStdDev timing = new FullRunningAverageAndStdDev();
     execute(estimateCallables, noEstimateCounter, timing);
     return computeFinalEvaluation();
   }
   
   protected static void execute(Collection<Callable<Void>> callables,
                                 AtomicInteger noEstimateCounter,
                                 RunningAverageAndStdDev timing) throws TasteException {
 
     Collection<Callable<Void>> wrappedCallables = wrapWithStatsCallables(callables, noEstimateCounter, timing);
     int numProcessors = Runtime.getRuntime().availableProcessors();
     ExecutorService executor = Executors.newFixedThreadPool(numProcessors);
     log.info("Starting timing of {} tasks in {} threads", wrappedCallables.size(), numProcessors);
     try {
       List<Future<Void>> futures = executor.invokeAll(wrappedCallables);
       // Go look for exceptions here, really
       for (Future<Void> future : futures) {
         future.get();
       }
     } catch (InterruptedException ie) {
       throw new TasteException(ie);
     } catch (ExecutionException ee) {
       throw new TasteException(ee.getCause());
     }
     executor.shutdown();
   }
   
   private static Collection<Callable<Void>> wrapWithStatsCallables(Iterable<Callable<Void>> callables,
                                                                    AtomicInteger noEstimateCounter,
                                                                    RunningAverageAndStdDev timing) {
     Collection<Callable<Void>> wrapped = Lists.newArrayList();
     int count = 0;
     for (Callable<Void> callable : callables) {
       boolean logStats = count++ % 1000 == 0; // log every 1000 or so iterations
       wrapped.add(new StatsCallable(callable, logStats, timing, noEstimateCounter));
     }
     return wrapped;
   }
   
   protected abstract void reset();
   
   protected abstract void processOneEstimate(float estimatedPreference, Preference realPref);
   
   protected abstract double computeFinalEvaluation();
 
   public int getkFold() {
 	return kFold;
   }
 	
   public void setkFold(int kFold) {
 	this.kFold = kFold;
   }
 
 
   public final class PreferenceEstimateCallable implements Callable<Void> {
 
     private final Recommender recommender;
     private final long testUserID;
     private final PreferenceArray prefs;
     private final AtomicInteger noEstimateCounter;
 
     public PreferenceEstimateCallable(Recommender recommender,
                                       long testUserID,
                                       PreferenceArray prefs,
                                       AtomicInteger noEstimateCounter) {
       this.recommender = recommender;
       this.testUserID = testUserID;
       this.prefs = prefs;
       this.noEstimateCounter = noEstimateCounter;
     }
 
     public Void call() throws TasteException {
       for (Preference realPref : prefs) {
         float estimatedPreference = Float.NaN;
         try {
           estimatedPreference = recommender.estimatePreference(testUserID, realPref.getItemID());
         } catch (NoSuchUserException nsue) {
           // It's possible that an item exists in the test data but not training data in which case
           // NSEE will be thrown. Just ignore it and move on.
           log.info("User exists in test data but not training data: {}", testUserID);
         } catch (NoSuchItemException nsie) {
           log.info("Item exists in test data but not training data: {}", realPref.getItemID());
         }
         if (Float.isNaN(estimatedPreference)) {
           noEstimateCounter.incrementAndGet();
         } else {
           estimatedPreference = capEstimatedPreference(estimatedPreference);
           processOneEstimate(estimatedPreference, realPref);
         }
       }
       return null;
     }
 
   }
 
 }
