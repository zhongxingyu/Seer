 package quality;
 
 import java.util.ArrayList;
 
import preprocessors.PreprocessorManager;

 import weka.core.Instances;
 import classification.ClassificationManager;
 import classification.Classifier;
 import datasource.DAO;
 import filters.FilterManager;
 import general.Email;
 
 /**
  * A class used for running the quality reporter and printing the summary
  * report.
  * 
  * @author Amr Sharaf
  * 
  */
 public class QualityReporterRunner {
 
 	// List of used preprocessors.
 	private String[] preprocessorsList;
 	// List of used filters.
 	private String[] filtersList;
 	// Classifier type.
 	private String classifierType;
 	// Username
 	private String username;
 	// Training set percentage
 	private int trainingSetPercentage;
 	// Training Set.
 	private Email[] trainingSet;
 	// Testing Set.
 	private Email[] testingSet;
 	// Index for the list of preprocessors in the command line arguments.
 	private static final int PREPROCESSORS_ID = 0;
 	// Index for the list of preprocessors in the command line arguments.
 	private static final int FILTERS_ID = 1;
 	// Index for the classifier type in the command line arguments.
 	private static final int CLASSIFIER_ID = 2;
 	// Index for the classifier type in the command line arguments.
 	private static final int USERNAME_ID = 3;
 	// Index for the training percentage in the command line arguments.
 	private static final int PERCENTAGE_ID = 4;
 	// Delimiter for separating the preprocessors and filters names.
 	private static final String DELIMITER = ",";
 
 	/**
 	 * Initializes the quality reporter runner from given array of arguments.
 	 * 
 	 * @param args
 	 *            initialization arguments.
 	 */
 	public void Init(String[] args) {
 		preprocessorsList = readPreprocessorsList(args);
 		filtersList = readFiltersList(args);
 		classifierType = readClassifierType(args);
 		username = readUsername(args);
 		trainingSetPercentage = readTrainingSet(args);
 	}
 
 	/**
 	 * Runs a QualityReporter instance and generate the summary report.
 	 * 
 	 * @throws Exception
 	 */
 	private void printSummaryReport() throws Exception {
 		ClassificationManager classifierManager = ClassificationManager
 				.getInstance(filtersList, preprocessorsList);
 		// Prepare evaluation data.
 		prepareDataset();
 		Classifier classifier = classifierManager.trainUserFromFileSystem(
 				username, classifierType, trainingSetPercentage);
 		FilterManager filterMgr = classifierManager.getFilterManager(username);
 		Instances dataset = filterMgr.getDataset(trainingSet);
 		QualityReporter reporter = new WekaQualityReporter(dataset);
 		// Evaluate classifier.
 		reporter.evaluateModel(classifier, filterMgr.getDataset(testingSet));
 		// Printing summary report.
 		System.out.println(reporter.toSummaryString());
 		// Printing class details report.
 		System.out.println(reporter.toClassDetailsString(""));
 	}
 
 	/**
 	 * Prepares the training and testing set.
 	 */
 	private void prepareDataset() {
 		ClassificationManager classifierManager = ClassificationManager
 				.getInstance(filtersList, preprocessorsList);
 		String path = classifierManager.getGoldenDataPath(username);
 		DAO dao = DAO.getInstance("FileSystems:" + path);
 		ArrayList<String> labels = dao.getClasses();
 		ArrayList<Email> testing = new ArrayList<Email>();
 		ArrayList<Email> training = new ArrayList<Email>();
 		for (int i = 0; i < labels.size(); i++) {
 			Email[] emails = dao.getClassifiedEmails(labels.get(i), 2000);
 			double trainingSetRatio = trainingSetPercentage / 100.0;
 			int testSetStartIndex = (int) Math.ceil(trainingSetRatio
 					* emails.length);
 			for (int j = 0; j < testSetStartIndex; j++)
 				training.add(emails[j]);
 			for (int j = testSetStartIndex; j < emails.length; j++)
 				testing.add(emails[j]);
 		}
 		testingSet = new Email[testing.size()];
 		testing.toArray(testingSet);
 		trainingSet = new Email[training.size()];
 		training.toArray(trainingSet);
		
		PreprocessorManager pm = new PreprocessorManager(preprocessorsList);
		for (Email e: testingSet)
			pm.apply(e);
		for(Email e : trainingSet)
			pm.apply(e);
 	}
 
 	/**
 	 * Returns the array of preprocessors names from the arguments array.
 	 * @param args
 	 * @return
 	 */
 	private String[] readPreprocessorsList(String[] args) {
 		String preprocessors = args[PREPROCESSORS_ID];
 		return preprocessors.split(DELIMITER);
 	}
 
 	/**
 	 * Returns the array of filter names from the arguments array.
 	 * @param args
 	 * @return
 	 */
 	private String[] readFiltersList(String[] args) {
 		String preprocessors = args[FILTERS_ID];
 		return preprocessors.split(DELIMITER);
 	}
 
 	/**
 	 * Returns the classifier type from the array of arguments.
 	 * @param args
 	 * @return
 	 */
 	private String readClassifierType(String[] args) {
 		return args[CLASSIFIER_ID];
 	}
 
 	/**
 	 * Returns the username from the array of arguments.
 	 * @param args
 	 * @return
 	 */
 	private String readUsername(String[] args) {
 		return args[USERNAME_ID];
 	}
 
 	private int readTrainingSet(String[] args) {
 		int percentage = Integer.parseInt(args[PERCENTAGE_ID]);
 		if (percentage < 0 || percentage > 100)
 			throw new IllegalArgumentException();
 		return percentage;
 	}
 
 	public static void main(String[] args) throws Exception {
 		args = new String[5];
 		// List of preprocessors.
		args[0] = "preprocessors.Lowercase,preprocessors.NumberNormalization,preprocessors.UrlNormalization" +
 				",preprocessors.WordsCleaner,preprocessors.StopWordsRemoval,preprocessors.EnglishStemmer";
 		// List of filters.
		args[1] = "filters.DateFilterCreator,filters.SenderFilterCreator,filters.WordFrequencyFilterCreator,filters.LabelFilterCreator";
 		// Classifier name.
		args[2] = "naiveBayes";
 		// Username.
 		args[3] = "lokay_m";
 		// Training Percentage.
 		args[4] = "60";
 		QualityReporterRunner repoter = new QualityReporterRunner();
 		// Initialize quality reporter runner.
 		repoter.Init(args);
 		// Print the summary report.
 		repoter.printSummaryReport();
 	}
 }
