 package utils;
 
 
 import models.ActivityRecognitionType;
 import models.HMMTypes;
 import models.Posture;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 public class Utils {
 
     public static final String TRAIN_DIRECTORY = "train/";
     public static final String HMM_DIRECTORY = "learned_HMMs/";
     public static final String DATA_DIRECTORY = "data/";
     public static final String ACTIVITY_FILE = "activity/activity_recognition.txt";
     public static final String ROOM_MODEL_DIR = "room_model/";
     public static final String ROOM_MODEL_FILE = ROOM_MODEL_DIR + "room_model.txt";
     public static final String ROOM_CONFIG_FILE = ROOM_MODEL_DIR + "room_config_info.txt";
 
 
     public static final String POSTURE_PREFIX = "posture_";
     public static final String SKELETON_PREFIX = "skeleton_";
     public static final String READY_PREFIX = "ready_d_";
     public static final String TXT_SUFFIX = ".txt";
     public static final String SINGLE_SUFFIX = "_single";
     public static final String TMP = "~";
 
     public static final String GENERAL_HMM_NAME = "general_hmm";
 
     public static final String OBJECT_DETECTION_FOLDER = "object_detection/";
 
 
     public static final int MAX_OBSERVATION_SIZE = 10;
     public static final int MAX_KINECT_NO = 4;
 
     public static final boolean USE_ROOM_MODEL = true;
 
     public static final HMMTypes HMM_TYPE = HMMTypes.SpecialisedHMM;
    public static final ActivityRecognitionType ACTIVITY_RECOGNITION_TYPE = ActivityRecognitionType.ONLINE_RECOGNITION;
 
 
     /**
      * Make sorted list of the posture names of all the files in a directory.
      *
      * @param directoryFiles files in directory
      * @return sorted list of the names of all the files in a directory
      */
     public static List<String> getFileNamesFromDirectory(File[] directoryFiles) {
         List<String> filesInDirectory;
         filesInDirectory = new LinkedList<String>();
 
         for (File subFile : directoryFiles) {
             if (subFile.isFile() && !subFile.getPath().endsWith(TMP) &&
                     subFile.getName().contains(POSTURE_PREFIX)) {
                 filesInDirectory.add(subFile.getPath());
             }
         }
 
         Collections.sort(filesInDirectory, new FileNameComparator());
         return filesInDirectory;
     }
 
     /**
      * Obtain a list of all the training file names sequences from the training
      * directory.
      *
      * @return list of all the training file names sequences
      */
     private static List<List<String>> getTrainingFileNames() {
 
         List<List<String>> fileNames = new LinkedList<List<String>>();
         List<String> filesInDirectory;
 
         File[] files = new File(TRAIN_DIRECTORY).listFiles(), directoryFiles;
 
         if (null == files)
             return fileNames;
 
         for (File file : files) {
             if (file.isDirectory()) {
 
                 directoryFiles = file.listFiles();
                 filesInDirectory = getFileNamesFromDirectory(directoryFiles);
                 fileNames.add(filesInDirectory);
 
             }
 
         }
 
         return fileNames;
     }
 
     /**
      * Get a list of posture objects from a list of posture file
      * names containing posture information.
      *
      * @param filesInSequence file names containing posture information
      * @return list of posture objects
      * @throws FileNotFoundException
      */
     public static List<Posture> getPosturesFromFileSequence(List<String> filesInSequence) throws FileNotFoundException {
 
         List<Posture> sequencePostures;
         sequencePostures = new ArrayList<Posture>();
         Posture posture;
 
         for (String fileName : filesInSequence) {
 
             posture = new Posture(fileName);
             if (posture.getGeneralPosture() > 0)
                 sequencePostures.add(posture);
         }
         return sequencePostures;
     }
 
 
     /**
      * Read all the training posture files from the training directory
      * and create a list of posture object sequences from all of them.
      *
      * @return list of sequences of posture objects
      * @throws java.io.FileNotFoundException invalid file names
      */
     public static List<List<Posture>> getTrainPostures() throws FileNotFoundException {
         List<List<String>> fileNames = getTrainingFileNames();
 
         List<List<Posture>> postures = new ArrayList<List<Posture>>();
         List<Posture> sequencePostures;
         for (List<String> filesInSequence : fileNames) {
             sequencePostures = getPosturesFromFileSequence(filesInSequence);
             postures.add(sequencePostures);
 
         }
 
         return postures;
     }
 
 
     public static String getSkeletonFile(String postureFileName) {
         String s = new String(postureFileName);
 
         return s.replace(POSTURE_PREFIX, SKELETON_PREFIX);
     }
 
     public static String getPostureFile(String readyFilename) {
         String s = new String(readyFilename);
 
         return s.replace(READY_PREFIX, POSTURE_PREFIX);
     }
 
     public static String addSkeletonDeviceIndex(String skeletonFileName) {
 
         String s = new String(skeletonFileName);
 
         return s.replace(SKELETON_PREFIX, SKELETON_PREFIX + "0_");
     }
 
     public static String getDepthFileName(int i) {
         return ROOM_MODEL_DIR + "depth_" + i + "_0.txt";
     }
 
     public static String getImageFileName(int i) {
         return ROOM_MODEL_DIR + "image_" + i + "_0.bmp";
     }
 }
