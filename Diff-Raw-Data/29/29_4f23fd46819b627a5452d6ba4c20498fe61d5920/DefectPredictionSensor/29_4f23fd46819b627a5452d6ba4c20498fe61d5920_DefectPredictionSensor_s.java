 package com.revitasinc.sonar.dp.batch;
 
 import java.io.File;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.sonar.api.batch.Sensor;
 import org.sonar.api.batch.SensorContext;
 import org.sonar.api.measures.Measure;
 import org.sonar.api.resources.Project;
 
 import com.revitasinc.sonar.dp.DefectPredictionMetrics;
 import com.revitasinc.sonar.dp.DefectPredictionPlugin;
 import com.revitasinc.sonar.dp.batch.parser.ScmLogParserFactory;
 
 /**
  * Assigns a score to each source file in the project and collects scores for
  * those files most likely to contain more defects.
  * 
  * @author John Amos (jamos@revitasinc.com)
  */
 public class DefectPredictionSensor implements Sensor {
   private static final double TOP_SCORE = 100.0;
 
   private static Logger logger = LoggerFactory.getLogger(DefectPredictionSensor.class);
 
   private static Map<String, Double> scoreMap;
 
   public boolean shouldExecuteOnProject(Project project) {
     // This sensor is executed on all projects
     return true;
   }
 
   public void analyse(Project project, SensorContext sensorContext) {
     scoreMap = null;
     try {
       String command = (String) project.getProperty(DefectPredictionPlugin.COMMAND);
      saveScores(
          project,
          ScmLogParserFactory.getParser(command).parse(new File(getSourcePath(project)), command),
          sensorContext,
          doubleFromProperty(project, DefectPredictionPlugin.AUTHOR_CHANGE_WEIGHT,
              DefectPredictionPlugin.DEFAULT_AUTHOR_CHANGE_WEIGHT),
          doubleFromProperty(project, DefectPredictionPlugin.LINES_CHANGED_WEIGHT,
              DefectPredictionPlugin.DEFAULT_LINES_CHANGED_WEIGHT),
          doubleFromProperty(project, DefectPredictionPlugin.TIME_DECAY_EXPONENT,
              DefectPredictionPlugin.DEFAULT_TIME_DECAY_EXPONENT),
          (String) project.getProperty(DefectPredictionPlugin.FILE_NAME_REGEX),
          (String) project.getProperty(DefectPredictionPlugin.COMMENT_REGEX));
     }
     catch (Exception e) {
       logger.error(e.getClass().getName() + " - " + e.getMessage(), e);
     }
   }
 
   @Override
   public String toString() {
     return "DefectPredictionSensor";
   }
 
   /**
    * Reads the revision data from the scm repository to calculate a score for
    * each file and saves a Measure for each of the top scoring files. Calculates
    * and saves the score for each file using the code from
    * https://github.com/igrigorik/bugspots/blob/master/lib/bugspots/scanner.rb
    * <p>
    * t = 1 - ((Time.now - fix.date).to_f / (Time.now - fixes.last.date)) <br>
    * hotspots[file] += 1/(1+Math.exp((-12*t)+12))
    * 
    * @param project
    * @param map
    * @param sensorContext
    * @param authorChangeWeight
    * @param lineWeight
    * @param timeDecay
    */
   void saveScores(Project project, Map<String, List<RevisionInfo>> map, SensorContext sensorContext,
       double authorChangeWeight, double lineWeight, double timeDecay, String fileNameRegex, String commentRegex) {
     final long startTime = System.currentTimeMillis();
     Set<FileScore> set = new TreeSet<FileScore>();
     double projectDuration = startTime - getEarliestCommit(map);
     for (Entry<String, List<RevisionInfo>> entry : map.entrySet()) {
       if (!isFileIncluded(project, entry.getKey(), fileNameRegex)) {
         continue;
       }
       double score = 0.0;
       String lastAuthor = null;
       for (RevisionInfo revision : entry.getValue()) {
         if (isRevisionIncluded(revision, commentRegex)) {
           double authorChange = (lastAuthor != null && !lastAuthor.equals(revision.getAuthor())) ? authorChangeWeight
               : 1.0;
           double ti = 1.0 - ((startTime - revision.getDate().getTime()) / projectDuration);
           score += authorChange * (lineWeight * revision.getAddedLineCount())
               / (1 + Math.exp((-timeDecay * ti) + timeDecay));
           lastAuthor = revision.getAuthor();
         }
       }
       set.add(new FileScore(entry.getKey(), score));
     }
     set = normalizeScores(set);
     buildScoreMap(set);
     buildSortedMeasure(set, sensorContext);
   }
 
   /**
    * @param project
    * @param fileName
    * @param regex
    * @return true if the supplied fileName is included in the results, false
    *         otherwise
    */
   private boolean isFileIncluded(Project project, String fileName, String regex) {
     boolean result = (StringUtils.isEmpty(regex) || fileName == null || fileName.matches(regex));
     if (result && fileName != null) {
       String[] exclusions = project.getExclusionPatterns();
       if (exclusions != null) {
         for (String exclusion : exclusions) {
           if (fileName.matches(".*" + exclusion.replace("*", ".*"))) {
             result = false;
             break;
           }
         }
       }
     }
     return result;
   }
 
   /**
    * @param revision
    * @param commentRegex
    * @return true if the supplied RevisionInfo should be included in the score
    */
   private boolean isRevisionIncluded(RevisionInfo revision, String commentRegex) {
     return revision.getAuthor() != null
         && (commentRegex == null || revision.getComment() == null || revision.getComment().matches(commentRegex));
   }
 
   /**
    * @param map
    * @return the earliest commit for all files in the supplied revision history
    *         (can be used as the project start date)
    */
   private long getEarliestCommit(Map<String, List<RevisionInfo>> map) {
     long result = System.currentTimeMillis();
     for (List<RevisionInfo> list : map.values()) {
       for (RevisionInfo rev : list) {
         if (rev.getDate().getTime() < result) {
           result = rev.getDate().getTime();
         }
       }
     }
     return result;
   }
 
   /**
    * Normalizes the score for every file in the repository so that the top score
    * is TOP_SCORE.
    * 
    * @param set
    * @return
    */
   private Set<FileScore> normalizeScores(Set<FileScore> set) {
     Set<FileScore> newSet = new TreeSet<FileScore>();
     if (set != null && !set.isEmpty()) {
       double topRawScore = set.iterator().next().getScore();
       for (FileScore score : set) {
         newSet.add(new FileScore(score.getPath(), (score.getScore() / topRawScore) * TOP_SCORE));
       }
     }
     else {
       logger.warn("There were no source files analyzed by " + getClass().getSimpleName()
           + ".  You may need to change the Source Path in Configuration > General Settings > Defect Prediction.");
     }
     return newSet;
   }
 
   /**
    * @param project
    * @param key
    * @param defaultValue
    * @return a double value from the property with the supplied key
    */
   private double doubleFromProperty(Project project, String key, String defaultValue) {
     double result = Double.parseDouble(defaultValue);
     try {
       result = Double.parseDouble((String) project.getProperty(key));
     }
     catch (Exception e) {
       logger.warn("The property \"" + key
           + "\" is not set to a valid number in Configuration > System > General Settings > "
           + DefectPredictionPlugin.PLUGIN_NAME + ".  Using " + defaultValue + " instead.");
     }
     return result;
   }
 
   /**
    * Saves one Measure for each of the top-scoring files.
    * 
    * @param set
    * @param sensorContext
    */
   private void buildSortedMeasure(Set<FileScore> set, SensorContext sensorContext) {
     int i = 0;
     for (FileScore score : set) {
       if (i >= DefectPredictionMetrics.TOP_FILES_METRICS.size()) {
         break;
       }
       String value = String.format("%.2f", score.getScore()) + "#"
           + score.getPath().substring(score.getPath().lastIndexOf('/') + 1);
       logger.debug("Top defects measure value: \"" + value + "\"");
       sensorContext.saveMeasure(new Measure(DefectPredictionMetrics.TOP_FILES_METRICS.get(i), value));
       i++;
     }
   }
 
   /**
    * Builds a Map that can be used by the Decorator to assign a score to each
    * file.
    * 
    * @param set
    */
   private void buildScoreMap(Set<FileScore> set) {
     scoreMap = new ConcurrentHashMap<String, Double>();
     for (FileScore score : set) {
       scoreMap.put(score.getPath(), score.getScore());
     }
   }
 
   /**
    * @return the Map of scores
    */
   public static Map<String, Double> getScoreMap() {
     return scoreMap;
   }
 
   /**
    * @param project
    * @return the path to the source code for the supplied project
    */
   private String getSourcePath(Project project) {
     String result = project.getFileSystem().getBasedir().getPath();
     logger.info("Project basedir = \"" + result + "\"");
     if (result != null) {
       String subfolder = (String) project.getProperty(DefectPredictionPlugin.SOURCE_PATH);
       if (subfolder != null) {
         // absolute path
         if (subfolder.startsWith("/") || subfolder.substring(1).startsWith(":\\")) {
           result = subfolder;
         }
         else {
           result = new File(result, subfolder).getPath();
         }
       }
     }
     return result;
   }
 }
