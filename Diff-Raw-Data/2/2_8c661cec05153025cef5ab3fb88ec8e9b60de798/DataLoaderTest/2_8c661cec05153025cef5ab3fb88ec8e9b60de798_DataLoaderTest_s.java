 package spamdetection.util;
 
 import java.io.File;
 import java.io.PrintStream;
 
 import junit.framework.Assert;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import spamdetection.detection.ProgramArguments;
 import weka.core.Instances;
 
 public class DataLoaderTest {
 
 	private static final String CLASSIFICATION_TECHNIQUE = "J48";
 	private static final String TRAINING_SET_DIRECTORY = "training_set";
 	private static final String TEST_SET_DIRECTORY = "test_set";
 	private static final String HAM_TRAINING_DIRECTORY = TRAINING_SET_DIRECTORY + File.separator + "ham";
 	private static final String SPAM_TRAINING_DIRECTORY = TRAINING_SET_DIRECTORY + File.separator + "spam";
 	private static final String SPAM_TRAINING_FILE_1 = SPAM_TRAINING_DIRECTORY + File.separator + "file_1";
 	private static final String SPAM_TRAINING_FILE_2 = SPAM_TRAINING_DIRECTORY + File.separator + "file_2";
 	private static final String HAM_TRAINING_FILE_1 = HAM_TRAINING_DIRECTORY + File.separator + "file_1";
 	private static final String HAM_TRAINING_FILE_2 = HAM_TRAINING_DIRECTORY + File.separator + "file_2";
 	private static final String HAM_TRAINING_FILE_3 = HAM_TRAINING_DIRECTORY + File.separator + "file_3";
 	private static final String HAM_TEST_DIRECTORY = TEST_SET_DIRECTORY + File.separator + "ham";
 	private static final String SPAM_TEST_DIRECTORY = TEST_SET_DIRECTORY + File.separator + "spam";
 	private static final String SPAM_TEST_FILE_1 = SPAM_TEST_DIRECTORY + File.separator + "file_1";
 	private static final String SPAM_TEST_FILE_2 = SPAM_TEST_DIRECTORY + File.separator + "file_2";
 	private static final String HAM_TEST_FILE_1 = HAM_TEST_DIRECTORY + File.separator + "file_1";
 	private static final String HAM_TEST_FILE_2 = HAM_TEST_DIRECTORY + File.separator + "file_2";
 	private static final String HAM_TEST_FILE_3 = HAM_TEST_DIRECTORY + File.separator + "file_3";
 	
 	private DataLoader loader;
 	private File trainingSet;
 	private File testSet;
 	
 	@Before
 	public void setUp() throws Exception {
 		loader = new DataLoader();
 		
 		trainingSet = new File(TRAINING_SET_DIRECTORY);
 		testSet = new File(TEST_SET_DIRECTORY);
 
 		trainingSet.mkdir();
 		testSet.mkdir();
 		
 		File hamTrainingSet = new File(HAM_TRAINING_DIRECTORY);
 		File hamTestSet = new File(HAM_TEST_DIRECTORY);
 		File spamTrainingSet = new File(SPAM_TRAINING_DIRECTORY);
 		File spamTestSet = new File(SPAM_TEST_DIRECTORY);
 		
 		hamTrainingSet.mkdir();
 		hamTestSet.mkdir();
 		spamTrainingSet.mkdir();
 		spamTestSet.mkdir();
 
 		File spamTrainingFile1 = new File(SPAM_TRAINING_FILE_1);
 		File spamTrainingFile2 = new File(SPAM_TRAINING_FILE_2);
 		File spamTestFile1 = new File(SPAM_TEST_FILE_1);
 		File spamTestFile2 = new File(SPAM_TEST_FILE_2);
 		File hamTrainingFile1 = new File(HAM_TRAINING_FILE_1);
 		File hamTrainingFile2 = new File(HAM_TRAINING_FILE_2);
 		File hamTrainingFile3 = new File(HAM_TRAINING_FILE_3);
 		File hamTestFile1 = new File(HAM_TEST_FILE_1);
 		File hamTestFile2 = new File(HAM_TEST_FILE_2);
 		File hamTestFile3 = new File(HAM_TEST_FILE_3);
 		
 		spamTrainingFile1.createNewFile();
 		spamTrainingFile2.createNewFile();
 		spamTestFile1.createNewFile();
 		spamTestFile2.createNewFile();
 		hamTrainingFile1.createNewFile();
 		hamTrainingFile2.createNewFile();
 		hamTrainingFile3.createNewFile();
 		hamTestFile1.createNewFile();
 		hamTestFile2.createNewFile();
 		hamTestFile3.createNewFile();
 		
 		PrintStream printStreamSpamTrainingFile1 = new PrintStream(spamTrainingFile1);
 		PrintStream printStreamSpamTrainingFile2 = new PrintStream(spamTrainingFile2);
 		PrintStream printStreamSpamTestFile1 = new PrintStream(spamTestFile1);
 		PrintStream printStreamSpamTestFile2 = new PrintStream(spamTestFile2);
 		PrintStream printStreamHamTrainingFile1 = new PrintStream(hamTrainingFile1);
 		PrintStream printStreamHamTrainingFile2 = new PrintStream(hamTrainingFile2);
 		PrintStream printStreamHamTrainingFile3 = new PrintStream(hamTrainingFile3);
 		PrintStream printStreamHamTestFile1 = new PrintStream(hamTestFile1);
 		PrintStream printStreamHamTestFile2 = new PrintStream(hamTestFile2);
 		PrintStream printStreamHamTestFile3 = new PrintStream(hamTestFile3);
 		
 		printStreamSpamTrainingFile1.printf("word1 word1 word1 word1\n");
 		printStreamSpamTrainingFile2.printf("word1 word1 word1 word1\n");
 		printStreamSpamTestFile1.printf("word2 word2 word3 word4\n");
 		printStreamSpamTestFile2.printf("word2 word2 word3 word4\n");
 		printStreamHamTrainingFile1.printf("word1 word1 word1 word1\n");
 		printStreamHamTrainingFile2.printf("word1 word1 word1 word1\n");
 		printStreamHamTrainingFile3.printf("word1 word1 word1 word1\n");
 		printStreamHamTestFile1.printf("word2 word2 word3 word4\n");
 		printStreamHamTestFile2.printf("word2 word2 word3 word4\n");
 		printStreamHamTestFile3.printf("word2 word2 word3 word4\n");
 		
 		printStreamSpamTrainingFile1.close();
 		printStreamSpamTrainingFile2.close();
 		printStreamSpamTestFile1.close();
 		printStreamSpamTestFile2.close();
 		printStreamHamTrainingFile1.close();
 		printStreamHamTrainingFile2.close();
 		printStreamHamTrainingFile3.close();
 		printStreamHamTestFile1.close();
 		printStreamHamTestFile2.close();
 		printStreamHamTestFile3.close();
 	}
 
	//@After
 	public void tearDown() throws Exception {
 		new File(SPAM_TRAINING_FILE_1).delete();
 		new File(SPAM_TRAINING_FILE_2).delete();
 		new File(SPAM_TEST_FILE_1).delete();
 		new File(SPAM_TEST_FILE_2).delete();
 		new File(HAM_TRAINING_FILE_1).delete();
 		new File(HAM_TRAINING_FILE_2).delete();
 		new File(HAM_TRAINING_FILE_3).delete();
 		new File(HAM_TEST_FILE_1).delete();
 		new File(HAM_TEST_FILE_2).delete();
 		new File(HAM_TEST_FILE_3).delete();
 		
 		new File(HAM_TRAINING_DIRECTORY).delete();
 		new File(HAM_TEST_DIRECTORY).delete();
 		new File(SPAM_TRAINING_DIRECTORY).delete();
 		new File(SPAM_TEST_DIRECTORY).delete();
 		
 		new File(TRAINING_SET_DIRECTORY).delete();
 		new File(TEST_SET_DIRECTORY).delete();
 	}
 	
 	@Test
 	public void testGetTrainingData() throws Exception {
 		ProgramArguments arguments = new ProgramArguments(CLASSIFICATION_TECHNIQUE, trainingSet, testSet);
 		Instances instances = loader.getTrainingData(arguments);
 		
 		int countSpam = 0;
 		int countHam = 0;
 		for (double value : instances.attributeToDoubleArray(0)) {
 			if (value == 0) {
 				countSpam++;
 			} else {
 				countHam++;
 			}
 		}
 		
 		Assert.assertEquals(2, countSpam);
 		Assert.assertEquals(3, countHam);
 	}
 	
 	@Test
 	public void testGetTestData() throws Exception {
 		ProgramArguments arguments = new ProgramArguments(CLASSIFICATION_TECHNIQUE, trainingSet, testSet);
 		Instances instances = loader.getTestData(arguments);
 		
 		int countSpam = 0;
 		int countHam = 0;
 		for (double value : instances.attributeToDoubleArray(0)) {
 			if (value == 0) {
 				countSpam++;
 			} else {
 				countHam++;
 			}
 		}
 		
 		Assert.assertEquals(2, countSpam);
 		Assert.assertEquals(3, countHam);
 	}
 }
