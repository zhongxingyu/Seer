 package model.MARK_I;
 
 import model.MARK_I.connectTypes.SensorCellsToRegionConnect;
 import model.MARK_I.connectTypes.SensorCellsToRegionRectangleConnect;
 
 import model.MARK_I.ColumnPosition;
 import model.MARK_I.Region;
 import model.MARK_I.SpatialPooler;
 import model.MARK_I.VisionCell;
 
 import model.util.JsonFileInputOutput;
 import model.theory.Memory;
 import model.theory.Idea;
 import model.util.RegionConsoleViewer;
 import com.google.gson.Gson;
 import java.util.Set;
 import model.theory.MemoryClassifier;
 import java.io.IOException;
 import mnist.tools.MnistManager;
 import model.Retine;
 
 /**
  * @author Quinn Liu (quinnliu@vt.edu)
  * @version July 5, 2013
  */
 public class TrainAndTestWalnutOnMNIST extends junit.framework.TestCase {
     private Retine retina;
     private Region region_LGN;
     private SpatialPooler spatialPooler;
 
     private Idea zero;
     private Idea one;
     private Idea two;
     private Idea three;
     private Idea four;
     private Idea five;
     private Idea six;
     private Idea seven;
     private Idea eight;
     private Idea nine;
 
     private int[] occurranceOfTrainingDigits;
     private int[] occurranceOfTestingDigits;
 
     private MemoryClassifier memoryClassifier_Digits;
 
     public void setUp() throws IOException {
 	// Walnut = a partial human brain model
 	this.constructWalnut();
 
 	// 10 means how many elements are in this array with a starting index at
 	// 0 - 9
 	this.occurranceOfTrainingDigits = new int[10];
 	this.occurranceOfTestingDigits = new int[10];
     }
 
     private void constructWalnut() {
 	// all images in MNIST are 28 x 28 pixels
 	VisionCell[][] visionCells = new VisionCell[27][27];
 	for (int x = 0; x < visionCells.length; x++) {
 	    for (int y = 0; y < visionCells[0].length; y++) {
 		visionCells[x][y] = new VisionCell();
 	    }
 	}
 	this.retina = new Retine(visionCells);
 
 	// TODO: size of region can be 4, 7, 14
 	// TODO: percentMinimumOverlapScore = 1 to 99
 	// TODO: desiredLocalActivity = 1 to (depends on region_LGN size)
 	this.region_LGN = new Region("LGNRegion", 7, 7, 1, 50, 1);
 
 	// TODO: try different overlaping Synapses from 0 to (depends on
 	// region_LGN size)
 	SensorCellsToRegionConnect retinaToLGN = new SensorCellsToRegionRectangleConnect();
 	retinaToLGN
 		.connect(this.retina.getVisionCells(), this.region_LGN, 0, 0);
 
 	this.spatialPooler = new SpatialPooler(this.region_LGN);
 	this.spatialPooler.setLearningState(true);
 
 	this.zero = new Idea("zero", this.region_LGN);
 	this.one = new Idea("one", this.region_LGN);
 	this.two = new Idea("two", this.region_LGN);
 	this.three = new Idea("three", this.region_LGN);
 	this.four = new Idea("four", this.region_LGN);
 	this.five = new Idea("five", this.region_LGN);
 	this.six = new Idea("six", this.region_LGN);
 	this.seven = new Idea("seven", this.region_LGN);
 	this.eight = new Idea("eight", this.region_LGN);
 	this.nine = new Idea("nine", this.region_LGN);
     }
 
     public void test_trainWalnut() throws IOException {
 	// ----------------------------Training-------------------------------
 	MnistManager m = new MnistManager(
 		"./images/digits/MNIST/t10k-images.idx3-ubyte",
 		"./images/digits/MNIST/t10k-labels.idx1-ubyte");
 
 	// after 200 images a VisionCell can become unconnected to a Synapse if
 	// it wasn't active for any of the 200 images and Synapse
 	// decreasePermanence
 	// = 0.005f
 	int numberOfImagesToTrain = 400;
 	for (int i = 1; i < (numberOfImagesToTrain + 1); i++) {
 	    m.setCurrent(i); // index of the image that we are interested in
 	    int[][] image = m.readImage();
 	    int imageLabel = m.readLabel();
 	    this.updateOccuranceOfTrainingDigits(imageLabel);
 
 	    this.retina.see2DIntArray(image); // VisionCells states updated
 	    this.spatialPooler.performSpatialPoolingOnRegion();
 	    Set<ColumnPosition> columnActivity = this.spatialPooler
 		    .getActiveColumnPositions();
 
 	    // update each Idea with the frequency of each ColumnPosition
 	    // appearing from the output of the SpatialPooler
 	    this.updateActiveFrequenciesOfColumns(imageLabel, columnActivity);
 	}
 
 	// TODO: subsample size from 4 to (2% of Region Columns size)
 	this.updateIdeasWithSetOfColumnPositions(5);
 	// X = new value
 	// 1 = 8, 2 = 11, 3 = 11, 4 = 13, 5 = 14, 6 = 13, 7 = 8, 8 = 13, 9 = 11,
 	// 10 = 13, 11 = 13, 12 = 5, 13 = 11, 20 = 11
 
 	Memory memory_Digits = new Memory();
 	memory_Digits.addNewIdea(this.zero);
 	memory_Digits.addNewIdea(this.one);
 	memory_Digits.addNewIdea(this.two);
 	memory_Digits.addNewIdea(this.three);
 	memory_Digits.addNewIdea(this.four);
 	memory_Digits.addNewIdea(this.five);
 	memory_Digits.addNewIdea(this.six);
 	memory_Digits.addNewIdea(this.seven);
 	memory_Digits.addNewIdea(this.eight);
 	memory_Digits.addNewIdea(this.nine);
 
 	// add Memory_Digits to MemoryClassifier_Digits
 	this.memoryClassifier_Digits = new MemoryClassifier(memory_Digits);
 
 	// save MemoryClassifier into a human readable .txt file
 	Gson gson = new Gson();
 	String memoryClassifierObject = gson
 		.toJson(this.memoryClassifier_Digits);
 	JsonFileInputOutput.saveObjectToTextFile(memoryClassifierObject,
		"./experiments/model/MARK_I/MemoryClassifier_MNIST_Digits.txt");
 
 	// ---------------------------Testing----------------------------------
 	MnistManager mm = new MnistManager(
 		"./images/digits/MNIST/train-images.idx3-ubyte",
 		"./images/digits/MNIST/train-labels.idx1-ubyte");
 
 	this.spatialPooler.setLearningState(false);
 	int numberOfImagesToTest = 1000;
 
 	int correctClassifications = 0;
 	for (int i = 1; i < (numberOfImagesToTest + 1); i++) {
 	    mm.setCurrent(i); // index of the image that we are interested in
 	    int[][] image = mm.readImage();
 	    int imageLabel = mm.readLabel();
 	    this.updateOccuranceOfTestingDigits(imageLabel);
 
 	    this.retina.see2DIntArray(image); // VisionCells states updated
 	    this.spatialPooler.performSpatialPoolingOnRegion();
 
 	    Set<ColumnPosition> columnActivity = this.spatialPooler
 		    .getActiveColumnPositions();
 	    this.memoryClassifier_Digits.updateIdeas(columnActivity);
 
 	    String digitIdeaName = this.memoryClassifier_Digits
 		    .getCurrentIdeaNameClassification();
 	    int digitClassification = this
 		    .convertStringDigitToInt(digitIdeaName);
 	    if (imageLabel == digitClassification) {
 		correctClassifications++;
 	    }
 
 	    // System.out.println("imageLabel: " + imageLabel);
 	    // System.out.println(this.memoryClassifier_Digits
 	    // .getCurrentIdeaNameClassification());
 	}
 
 	// TODO: assert equals with output stream
 //	System.out.println("total images tested: " + numberOfImagesToTest);
 //	System.out.println("# of correct classifications: "
 //		+ correctClassifications);
 //	int incorrectClassifications = numberOfImagesToTest
 //		- correctClassifications;
 //	System.out.println("# of incorrect classifications: "
 //		+ incorrectClassifications);
     }
 
     private int convertStringDigitToInt(String number) {
 	number.toLowerCase();
 	switch (number) {
 	case "zero":
 	    return 0;
 	case "one":
 	    return 1;
 	case "two":
 	    return 2;
 	case "three":
 	    return 3;
 	case "four":
 	    return 4;
 	case "five":
 	    return 5;
 	case "six":
 	    return 6;
 	case "seven":
 	    return 7;
 	case "eight":
 	    return 8;
 	case "nine":
 	    return 9;
 	}
 	throw new IllegalArgumentException(
 		"number in method convertStringDigitToInt must be a string"
 			+ "representation of a integer between 0 and 9");
     }
 
     private void updateOccuranceOfTrainingDigits(int imageLabel) {
 	// imageLabel is always an integer between 0 - 9
 	switch (imageLabel) {
 	case 0:
 	    this.occurranceOfTrainingDigits[0]++;
 	    break;
 	case 1:
 	    this.occurranceOfTrainingDigits[1]++;
 	    break;
 	case 2:
 	    this.occurranceOfTrainingDigits[2]++;
 	    break;
 	case 3:
 	    this.occurranceOfTrainingDigits[3]++;
 	    break;
 	case 4:
 	    this.occurranceOfTrainingDigits[4]++;
 	    break;
 	case 5:
 	    this.occurranceOfTrainingDigits[5]++;
 	    break;
 	case 6:
 	    this.occurranceOfTrainingDigits[6]++;
 	    break;
 	case 7:
 	    this.occurranceOfTrainingDigits[7]++;
 	    break;
 	case 8:
 	    this.occurranceOfTrainingDigits[8]++;
 	    break;
 	case 9:
 	    this.occurranceOfTrainingDigits[9]++;
 	    break;
 	}
     }
 
     private void updateOccuranceOfTestingDigits(int imageLabel) {
 	// imageLabel is always an integer between 0 - 9
 	switch (imageLabel) {
 	case 0:
 	    this.occurranceOfTestingDigits[0]++;
 	    break;
 	case 1:
 	    this.occurranceOfTestingDigits[1]++;
 	    break;
 	case 2:
 	    this.occurranceOfTestingDigits[2]++;
 	    break;
 	case 3:
 	    this.occurranceOfTestingDigits[3]++;
 	    break;
 	case 4:
 	    this.occurranceOfTestingDigits[4]++;
 	    break;
 	case 5:
 	    this.occurranceOfTestingDigits[5]++;
 	    break;
 	case 6:
 	    this.occurranceOfTestingDigits[6]++;
 	    break;
 	case 7:
 	    this.occurranceOfTestingDigits[7]++;
 	    break;
 	case 8:
 	    this.occurranceOfTestingDigits[8]++;
 	    break;
 	case 9:
 	    this.occurranceOfTestingDigits[9]++;
 	    break;
 	}
     }
 
     private void updateActiveFrequenciesOfColumns(int imageLabel,
 	    Set<ColumnPosition> columnActivity) {
 	switch (imageLabel) {
 	case 0:
 	    this.zero.updateActiveFrequencies(columnActivity);
 	    break;
 	case 1:
 	    this.one.updateActiveFrequencies(columnActivity);
 	    break;
 	case 2:
 	    this.two.updateActiveFrequencies(columnActivity);
 	    break;
 	case 3:
 	    this.three.updateActiveFrequencies(columnActivity);
 	    break;
 	case 4:
 	    this.four.updateActiveFrequencies(columnActivity);
 	    break;
 	case 5:
 	    this.five.updateActiveFrequencies(columnActivity);
 	    break;
 	case 6:
 	    this.six.updateActiveFrequencies(columnActivity);
 	    break;
 	case 7:
 	    this.seven.updateActiveFrequencies(columnActivity);
 	    break;
 	case 8:
 	    this.eight.updateActiveFrequencies(columnActivity);
 	    break;
 	case 9:
 	    this.nine.updateActiveFrequencies(columnActivity);
 	    break;
 	}
     }
 
     private void updateIdeasWithSetOfColumnPositions(int X) {
 	Set<ColumnPosition> zero_mostActiveColumnPositions = this.zero
 		.getTopXActiveColumnPositions(X);
 	this.zero.unionColumnPositions(zero_mostActiveColumnPositions);
 
 	Set<ColumnPosition> one_mostActiveColumnPositions = this.one
 		.getTopXActiveColumnPositions(X);
 	this.one.unionColumnPositions(one_mostActiveColumnPositions);
 
 	Set<ColumnPosition> two_mostActiveColumnPositions = this.two
 		.getTopXActiveColumnPositions(X);
 	this.two.unionColumnPositions(two_mostActiveColumnPositions);
 
 	Set<ColumnPosition> three_mostActiveColumnPositions = this.three
 		.getTopXActiveColumnPositions(X);
 	this.three.unionColumnPositions(three_mostActiveColumnPositions);
 
 	Set<ColumnPosition> four_mostActiveColumnPositions = this.four
 		.getTopXActiveColumnPositions(X);
 	this.four.unionColumnPositions(four_mostActiveColumnPositions);
 
 	Set<ColumnPosition> five_mostActiveColumnPositions = this.five
 		.getTopXActiveColumnPositions(X);
 	this.five.unionColumnPositions(five_mostActiveColumnPositions);
 
 	Set<ColumnPosition> six_mostActiveColumnPositions = this.six
 		.getTopXActiveColumnPositions(X);
 	this.six.unionColumnPositions(six_mostActiveColumnPositions);
 
 	Set<ColumnPosition> seven_mostActiveColumnPositions = this.seven
 		.getTopXActiveColumnPositions(X);
 	this.seven.unionColumnPositions(seven_mostActiveColumnPositions);
 
 	Set<ColumnPosition> eight_mostActiveColumnPositions = this.eight
 		.getTopXActiveColumnPositions(X);
 	this.eight.unionColumnPositions(eight_mostActiveColumnPositions);
 
 	Set<ColumnPosition> nine_mostActiveColumnPositions = this.nine
 		.getTopXActiveColumnPositions(X);
 	this.nine.unionColumnPositions(nine_mostActiveColumnPositions);
     }
 }
 
 // for (int j = 0; j < 10; j++) {
 // System.out.print("\nnumber of " + j + " images = "
 // + this.occurranceOfDigits[j]);
 // }
 
 // if (i == (numberOfImagesToTrain)) {
 // System.out.println("\nSynapse permanences after image #: " + i);
 // RegionConsoleViewer.printDoubleIntArray(RegionConsoleViewer
 // .getSynapsePermanencesIntArray(this.spatialPooler
 // .getRegion()));
 // }
