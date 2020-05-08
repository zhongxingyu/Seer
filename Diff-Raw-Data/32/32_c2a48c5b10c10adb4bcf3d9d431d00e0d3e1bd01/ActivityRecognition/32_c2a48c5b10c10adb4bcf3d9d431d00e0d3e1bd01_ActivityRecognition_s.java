 package app;
 
 
 import hmm.HMM;
 import hmm.HMMCalculus;
 import hmm.HMMOperations;
 import hmm.HMMOperationsImpl;
 import models.*;
 import org.apache.commons.lang3.ArrayUtils;
 import utils.FileNameComparator;
 import utils.Pair;
 import utils.Utils;
 
 import java.io.*;
 import java.net.URISyntaxException;
 import java.nio.file.*;
 import java.util.ArrayList;
 import java.util.EnumMap;
 import java.util.List;
 import java.util.Map;
 
 import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
 import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
 import static utils.Utils.*;
 
 
 public class ActivityRecognition {
 
 
     /* every activity has a list of observations */
    public Map<Activity, List<Integer>> activityObservationsMap = initActivityObservationsMap();
 
     /* reference to the object recognition module*/
     private ObjectRecognition objectRecognition;
 
     /* tha last position of the user on the grid*/
     private Pair<Integer, Integer> lastPosition = null;
 
     public ActivityRecognition() throws FileNotFoundException {
 
         objectRecognition = new ObjectRecognition(Utils.ROOM_MODEL_FILE);
     }
 
     public static void main(String args[]) throws IOException, URISyntaxException {
 
         new ActivityRecognition().waitForNewFiles();
 
     }
 
     /**
      * Waits for new posture files to be added in the directory DATA_DIRECTORY.
      * <p/>
      * When a new posture file is created, it processes it by predicting
      * the corresponding activity, printing and logging it into a file.
      *
      * @throws IOException
      */
     public void waitForNewFiles() throws IOException {
 
         WatchService watcher = FileSystems.getDefault().newWatchService();
         Path dir = Paths.get(DATA_DIRECTORY);
 
         dir.register(watcher, ENTRY_CREATE);
 
         for (; ; ) {
 
             // wait for key to be signaled
             WatchKey key;
             try {
                 key = watcher.take();
             } catch (InterruptedException x) {
                 return;
             }
 
             for (WatchEvent<?> event : key.pollEvents()) {
                 WatchEvent.Kind<?> kind = event.kind();
 
 
                 if (kind == OVERFLOW) {
                     continue;
                 }
 
 
                 WatchEvent<Path> ev = (WatchEvent<Path>) event;
                 Path filename = ev.context();
 
                 /* Verify that the new file is a text file.*/
                 try {
 
                     Path child = dir.resolve(filename);
                     if (!Files.probeContentType(child).equals("text/plain")) {
                         System.err.format("New file '%s'" +
                                 " is not a plain text file.%n", filename);
                         continue;
                     }
 
 
                 } catch (IOException x) {
                     System.err.println(x);
                     continue;
                 }
 
 
                 /* Verify file name */
                 if (!filename.toString().startsWith("posture_") || !filename.toString().endsWith(".txt")) {
                     continue;
                 }
 
 
                 try {
                     processNewFile(DATA_DIRECTORY + filename.toString());
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
 
             }
 
             /* reset the key*/
             boolean valid = key.reset();
             if (!valid) {
                 break;
             }
         }
     }
 
     /**
      * Processes a newly created file to obtain the predicted activity, which is
      * displayed and logged into the file ACTIVITY_FILE.
      * <p/>
      * When a new posture file is created, it's information is used to create
      * an observation which is added to the observations lists of all the activities.
      * For each activity, the sequence is fed to it's corresponding HMM and the
      * predictions are aggregated. The best prediction is then chosen.
      *
      * @param filename name of newly created file
      * @throws IOException
      */
     private void processNewFile(String filename) throws IOException {
 
         /*read posture information from file*/
         Posture posture = new Posture(filename);
         /* keep the predictions made by each activity HMM to later choose the best one*/
         Map<Activity, Prediction> predictions = new EnumMap<Activity, Prediction>(Activity.class);
         Prediction prediction;
         Map.Entry<Activity, Prediction> bestPrediction;
         String predictedActivity;
         int preds[], predictedActivityIndex;
         int frameNumber = FileNameComparator.getFileNumber(filename);
 
         for (Activity activity : Activity.values()) {
             /* make a prediction using an activity's HMM*/
             prediction = predictActivity(activity, posture, filename);
             predictions.put(activity, prediction);
         }
 
         /* select the best prediction */
         bestPrediction = chooseBestPrediction(predictions);
 
         preds = bestPrediction.getValue().getPredictions();
 
         /* check to see if no activity has been detected */
         if (preds[preds.length - 1] == 1) {
             predictedActivity = bestPrediction.getKey().getName();
             predictedActivityIndex = bestPrediction.getKey().getIndex();
         } else {
             predictedActivity = "no activity";
             predictedActivityIndex = 0;
         }
 
 
         System.out.println("Activity from frame " + frameNumber + ": " + predictedActivity);
 
         /* log activity prediction to file */
         appendActivityToFile(frameNumber, predictedActivityIndex, bestPrediction.getValue().getProbability());
 
 
     }
 
     private void appendActivityToFile(int frameNumber, int predictedActivityIndex, double probability) throws IOException {
 
         PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(Utils.ACTIVITY_FILE, true)));
 
         out.println(frameNumber + "," + predictedActivityIndex + "," + probability + ",");
         out.close();
     }
 
     /**
      * Chooses from a mapping of activities and predictions the best prediction,
      * taking into account the prediction probability.
      *
      * @param predictions mapping of activities and predictions
      * @return the best prediction
      */
     private Map.Entry<Activity, Prediction> chooseBestPrediction(Map<Activity, Prediction> predictions) {
 
         Map.Entry<Activity, Prediction> prediction = null;
         int result1, result2, lastIndex;
         double probability1, probability2;
 
 
         for (Map.Entry<Activity, Prediction> entry : predictions.entrySet()) {
 
             if (prediction == null) {
                 prediction = entry;
             } else {
                 lastIndex = prediction.getValue().getObservations().length - 1;
 
                 result1 = prediction.getValue().getPredictions()[lastIndex];
                 probability1 = prediction.getValue().getProbability();
                 result2 = entry.getValue().getPredictions()[lastIndex];
                 probability2 = entry.getValue().getProbability();
 
                 /*a result 1 means that an activity has been detected,
                 * thus we prefer a result that predicts an activity*/
                 if (result2 == 1 && result1 == 0)
                     prediction = entry;
 
                 /* if both predict an activity, or don't predict anything then
                 * we take the probability into consideration */
                 if (result1 == result2) {
                     if (probability2 > probability1)
                         prediction = entry;
                 }
 
             }
 
         }
 
         return prediction;
 
     }
 
     /**
      * Makes a prediction using a the HMM of an activity.
      * <p/>
      * The prediction takes into account not only the last observation,
      * but also the sequence of observations made before that.
      *
      * @param activity activity
      * @param posture  posture information
      * @param postureFileName posture file name
      * @return prediction
      * @throws FileNotFoundException
      */
     private Prediction predictActivity(Activity activity, Posture posture, String postureFileName) throws FileNotFoundException {
         Prediction prediction;
         HMM hmm;
         HMMOperations hmmOperations = new HMMOperationsImpl();
         List<String> posturesOfInterest = Utils.activityMap.get(activity);
         String skeletonFileName = getSkeletonFile(postureFileName);
         Pair<Integer, Pair<Integer, Integer>> result;
 
         /* load HMM for the current activity */
         hmm = new HMMCalculus(Utils.HMM_DIRECTORY + activity.getName() + ".txt");
 
         /* obtain list of past observations */
        List<Integer> observations = activityObservationsMap.get(activity);
 
         /* transform posture information into observation index*/
         int observation = posture.computeObservationIndex(posturesOfInterest);
 
         /* if the posture is misclassified then no activity is detected*/
         if (observation < 0) {
 
             int size = observations.size() + 1;
             int obs[] = new int[size], pred[] = new int[size];
 
             return new Prediction(obs, pred, 0.0);
         }
 
         /* combine the posture information with the object interaction and position information*/
         if (USE_OBJECT_RECOGNITION) {
             result = addObjectRecognitionObservation(observation,
                     skeletonFileName, objectRecognition, lastPosition);
             observation = result.getFirst();
             lastPosition = result.getSecond();
         }
 
         /* add new observation to list*/
         activityObservationsMap.remove(activity);
         observations.add(observation);
         activityObservationsMap.put(activity, observations);
 
 
 
 
         /* predict */
         prediction = hmmOperations.predict(hmm,
                ArrayUtils.toPrimitive(observations.toArray(new Integer[0])));
 
 
         return prediction;
     }
 
 
 
 
 
    private Map<Activity, List<Integer>> initActivityObservationsMap() {
 
        Map<Activity, List<Integer>> map = new EnumMap<Activity, List<Integer>>(Activity.class);
 
         for (Activity activity : Activity.values()) {
 
            map.put(activity, new ArrayList<Integer>());
 
         }
 
         return map;
     }
 
 
 }
