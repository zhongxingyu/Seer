 package org.sortdc.sortdc;
 
 import com.esotericsoftware.yamlbeans.YamlReader;
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.sortdc.sortdc.database.Database;
 import org.sortdc.sortdc.database.DatabaseMongo;
 import org.sortdc.sortdc.database.DatabaseMysql;
 
 public class Config {
 
     private static Config instance;
    private Map<String, Object> parameters;
     private Map<String, Classifier> classifiers = new HashMap<String, Classifier>();
     public static final String LOG = "log";
     public static final String LOG_VERBOSE = "verbose";
     public static final String LOG_FILEPATH = "filepath";
     public static final String CLASSIFIERS_LIST = "classifiers";
     public static final String CLASSIFIERS_DATABASE = "database";
     public static final String CLASSIFIERS_DATABASE_DBMS = "dbms";
     public static final String CLASSIFIERS_DATABASE_HOST = "host";
     public static final String CLASSIFIERS_DATABASE_PORT = "port";
     public static final String CLASSIFIERS_DATABASE_DBNAME = "dbname";
     public static final String CLASSIFIERS_DATABASE_USERNAME = "username";
     public static final String CLASSIFIERS_DATABASE_PASSWORD = "password";
     public static final String CLASSIFIER_LANG = "lang";
     public static final String CLASSIFIER_WORDS = "words";
     public static final String CLASSIFIER_WORDS_MIN_LENGTH = "words_min_length";
     public static final String CLASSIFIER_STEMMING = "stemming";
     public static final String CLASSIFIER_NGRAMSCHARS = "ngrams_chars";
     public static final String CLASSIFIER_NGRAMSWORDS = "ngrams_words";
     public static final String CLASSIFIER_STOPWORDS = "stopwords";
     public static final String CLASSIFIER_STOPWORDS_FILEPATH = "stopwords_file";
 
     private Config() {
     }
 
     /**
      * Creates a unique instance of Config (Singleton)
      *
      * @return Instance of Config
      */
     public static synchronized Config getInstance() {
         if (instance == null) {
             instance = new Config();
         }
         return instance;
     }
 
     /**
      * Loads configuration parameters from a config file
      *
      * @param filePath Path to the config file
      * @throws Exception
      */
     public void loadFile(String filePath) throws Exception {
         YamlReader reader = new YamlReader(new FileReader(filePath));
         Object params = reader.read();
         if (params instanceof Map) {
            this.parameters = (Map<String, Object>) params;
         } else {
             throw new Exception("Invalid config file");
         }
     }
 
     /**
      * Gets a parameter
      *
      * @param parameter Name of the parameter
      * @return Value of the parameter
      */
     public Object get(String parameter) {
         return this.parameters.get(parameter);
     }
 
     /**
      * Sets a parameter's value
      * 
      * @param name Name of the parameter
      * @param value Value of the parameter
      */
     public void set(String name, Object value) {
         this.parameters.put(name, value);
     }
 
     /**
      * Converts a config parameter to a boolean
      *
      * @param value
      * @return
      */
     public boolean paramIsTrue(Object value) {
         if (value instanceof Boolean) {
             return (Boolean) value;
         }
         if (value instanceof String) {
             return ((String) value).toLowerCase().equals("true")
                     || ((String) value).toLowerCase().equals("yes")
                     || ((String) value).equals("1");
         }
         return false;
     }
 
     /**
      * Converts a config parameter to an integer
      *
      * @param value
      * @return
      * @throws Exception
      */
     public Integer paramToInt(Object value) throws Exception {
         if (value instanceof Integer) {
             return (Integer) value;
         }
         if (value instanceof String) {
             return Integer.parseInt((String) value);
         }
         throw new Exception("Invalid number format: " + value);
     }
 
     /**
      * Converts a config parameter to a String
      *
      * @param value
      * @return
      * @throws Exception
      */
     public String paramToString(Object value) throws Exception {
         if (value instanceof String) {
             return (String) value;
         }
         return value.toString();
     }
 
     /**
      * Converts a config parameter to a List
      *
      * @param value
      * @return
      * @throws Exception
      */
     public List paramToList(Object value) throws Exception {
         if (value instanceof List) {
             return (List) value;
         }
         return null;
     }
 
     /**
      * Converts a config parameter to a List
      *
      * @param value
      * @return
      * @throws Exception
      */
     public Map paramToMap(Object value) throws Exception {
         if (value instanceof Map) {
             return (Map) value;
         }
         return null;
     }
 
     /**
      * Applies config for logging system
      *
      * @throws Exception
      */
     public void applyLogConfig() throws Exception {
         Map logConfig = this.paramToMap(this.get(LOG));
         if (logConfig == null) {
             return;
         }
         Log log = Log.getInstance();
 
         if (logConfig.containsKey(LOG_VERBOSE)) {
             log.setVerbose(this.paramIsTrue(logConfig.get(LOG_VERBOSE)));
         }
         if (logConfig.containsKey(LOG_FILEPATH)) {
             log.setFilepath(this.paramToString(logConfig.get(LOG_FILEPATH)));
         }
     }
 
     /**
      * Creates Classifiers' instances and configures it
      *
      * @return Classifiers' instances
      * @throws Exception
      */
     public synchronized Map<String, Classifier> getClassifiers() throws Exception {
         if (!this.classifiers.isEmpty()) {
             return this.classifiers;
         }
 
         Map<String, Map> classifiersConfig;
         try {
             classifiersConfig = this.paramToMap(this.get(Config.CLASSIFIERS_LIST));
         } catch (Exception e) {
             throw new Exception("Invalid classifier config");
         }
 
         for (Map.Entry<String, Map> classifierEntry : classifiersConfig.entrySet()) {
             String classifier_name = classifierEntry.getKey();
             Map classifierConfig = classifierEntry.getValue();
 
             Tokenization tokenization = new Tokenization();
             if (classifierConfig.containsKey(CLASSIFIER_WORDS)) {
                 tokenization.setExtractWords(this.paramIsTrue(classifierConfig.get(CLASSIFIER_WORDS)));
             }
             if (classifierConfig.containsKey(CLASSIFIER_STEMMING)) {
                 if (this.paramIsTrue(classifierConfig.get(CLASSIFIER_STEMMING))) {
                     if (classifierConfig.containsKey(CLASSIFIER_LANG)) {
                         tokenization.enableStemming(this.paramToString(classifierConfig.get(CLASSIFIER_LANG)));
                     } else {
                         throw new Exception("You must define a language for stemming");
                     }
                 } else {
                     tokenization.disableStemming();
                 }
             }
             if (classifierConfig.containsKey(CLASSIFIER_WORDS_MIN_LENGTH)) {
                 tokenization.setWordsMinLength(this.paramToInt(classifierConfig.get(CLASSIFIER_WORDS_MIN_LENGTH)));
             }
             if (classifierConfig.containsKey(CLASSIFIER_NGRAMSCHARS)) {
                 List<Integer> ngrams_chars = this.paramToList(classifierConfig.get(CLASSIFIER_NGRAMSCHARS));
                 if (ngrams_chars == null) {
                     ngrams_chars = Arrays.asList(this.paramToInt(classifierConfig.get(CLASSIFIER_NGRAMSCHARS)));
                 } else {
                     for (int i = ngrams_chars.size() - 1; i >= 0; i--) {
                         ngrams_chars.set(i, this.paramToInt(ngrams_chars.get(i)));
                     }
                 }
                 tokenization.setNgramsChars(ngrams_chars);
             }
             if (classifierConfig.containsKey(CLASSIFIER_NGRAMSWORDS)) {
                 List<Integer> ngrams_words = this.paramToList(classifierConfig.get(CLASSIFIER_NGRAMSWORDS));
                 if (ngrams_words == null) {
                     ngrams_words = Arrays.asList(this.paramToInt(classifierConfig.get(CLASSIFIER_NGRAMSWORDS)));
                 } else {
                     for (int i = ngrams_words.size() - 1; i >= 0; i--) {
                         ngrams_words.set(i, this.paramToInt(ngrams_words.get(i)));
                     }
                 }
                 tokenization.setNgramsWords(ngrams_words);
             }
 
             if (classifierConfig.containsKey(CLASSIFIER_STOPWORDS_FILEPATH)) {
                 String stopwordsFilepath = this.paramToString(classifierConfig.get(CLASSIFIER_STOPWORDS_FILEPATH));
                List<String> stopWords = new ArrayList<String>();
                 InputStream is = new FileInputStream(stopwordsFilepath);
                 InputStreamReader isr = new InputStreamReader(is);
                 BufferedReader br = new BufferedReader(isr);
                 String line;
                 while ((line = br.readLine()) != null) {
                     stopWords.add(line.trim());
                 }
                 br.close();
                 if (stopWords.size() != 0) {
                     classifierConfig.put(CLASSIFIER_STOPWORDS, stopWords);
                 }
             }
             if (classifierConfig.containsKey(CLASSIFIER_STOPWORDS)) {
                 tokenization.setStopWords(this.paramToList(classifierConfig.get(CLASSIFIER_STOPWORDS)));
             }
 
 
             Map databaseConfig = this.paramToMap(classifierConfig.get(CLASSIFIERS_DATABASE));
             if (databaseConfig == null) {
                 throw new Exception("Database config not found");
             }
 
             Database database;
             String dbms = this.paramToString(databaseConfig.get(CLASSIFIERS_DATABASE_DBMS)).toLowerCase();
 
             if (dbms.equals("mongodb")) {
                 database = new DatabaseMongo();
             } else if (dbms.equals("mysql")) {
                 database = new DatabaseMysql();
             } else {
                 throw new Exception("Unsupported DBMS: " + dbms);
             }
 
             if (databaseConfig.containsKey(CLASSIFIERS_DATABASE_HOST)) {
                 database.setHost(this.paramToString(databaseConfig.get(CLASSIFIERS_DATABASE_HOST)));
             }
             if (databaseConfig.containsKey(CLASSIFIERS_DATABASE_PORT)) {
                 database.setPort(this.paramToInt(databaseConfig.get(CLASSIFIERS_DATABASE_PORT)));
             }
             database.setDbName(this.paramToString(databaseConfig.get(CLASSIFIERS_DATABASE_DBNAME)));
             if (databaseConfig.containsKey(CLASSIFIERS_DATABASE_USERNAME) && databaseConfig.containsKey(CLASSIFIERS_DATABASE_PASSWORD)) {
                 database.setUsername(this.paramToString(databaseConfig.get(CLASSIFIERS_DATABASE_USERNAME)));
                 database.setPassword(this.paramToString(databaseConfig.get(CLASSIFIERS_DATABASE_PASSWORD)));
             }
             database.connect();
 
 
             Classifier classifier = new Classifier(tokenization, database);
             this.classifiers.put(classifier_name, classifier);
         }
         return this.classifiers;
     }
 }
