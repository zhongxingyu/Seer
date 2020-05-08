 package model.MARK_II;
 
 import model.util.RegionConsoleViewer;
 import com.google.gson.Gson;
 import java.util.Set;
 import model.theory.MemoryClassifier;
 import java.io.IOException;
 import mnist.tools.MnistManager;
 import model.MARK_II.ConnectTypes.SensorCellsToRegionConnect;
 import model.MARK_II.ConnectTypes.SensorCellsToRegionRectangleConnect;
 import model.Retina;
 
/**
 * @author Quinn Liu (quinnliu@vt.edu)
 * @version MARK II | June 29, 2013
 */
 public class TrainWalnutOnMNIST extends junit.framework.TestCase {
     private Retina retina;
     private Region LGNRegion;
     private SpatialPooler spatialPooler;
 
     int[] occurranceOfDigits;
 
     // private MemoryClassifier memoryClassifier;
 
     public void setUp() throws IOException {
 	// Walnut = a partial human brain model
 	this.constructWalnut();
 
 	// 10 means how many elements are in this array with a starting index at
 	// 0 - 9
 	this.occurranceOfDigits = new int[10];
     }
 
     private void constructWalnut() {
 	// all images in MNIST are 28 x 28 pixels
 	VisionCell[][] visionCells = new VisionCell[27][27];
 	for (int x = 0; x < visionCells.length; x++) {
 	    for (int y = 0; y < visionCells[0].length; y++) {
 		visionCells[x][y] = new VisionCell();
 	    }
 	}
 	this.retina = new Retina(visionCells);
 
 	this.LGNRegion = new Region("LGNRegion", 7, 7, 1, 50, 1);
 
 	SensorCellsToRegionConnect retinaToLGN = new SensorCellsToRegionRectangleConnect();
 	retinaToLGN.connect(this.retina.getVisionCells(), this.LGNRegion, 0, 0);
 
 	this.spatialPooler = new SpatialPooler(this.LGNRegion);
     }
 
     public void test_trainWalnut() throws IOException {
 	// after 200 images a pixel can become unconnected to a Synapse if
 	// it wasn't "on" for any of the 200 images
 	int numberOfImagesToTrain = 20;
 	for (int i = 1; i < (numberOfImagesToTrain + 1); i++) {
 	    MnistManager m = new MnistManager(
 		    "./images/MNIST/t10k-images.idx3-ubyte",
 		    "./images/MNIST/t10k-labels.idx1-ubyte");
 	    m.setCurrent(i); // index of the image that we are interested in
 	    int[][] image = m.readImage();
 	    int imageLabel = m.readLabel();
 	    this.updateOccuranceOfDigits(imageLabel);
 
 	    this.retina.see2DIntArray(image); // VisionCells states updated
 
 	    this.spatialPooler.setLearningState(true);
 	    Set<ColumnPosition> LGNNeuronActivity = this.spatialPooler
 		    .performSpatialPoolingOnRegion();
 
 	    if (i == (numberOfImagesToTrain)) {
 		System.out.println("\nimage #: " + i);
 		RegionConsoleViewer.printDoubleIntArray(RegionConsoleViewer
 			.getSynapsePermanencesIntArray(this.spatialPooler
 				.getRegion()));
 	    }
 	}
 
 	for (int j = 0; j < 10; j++) {
 	    System.out.print("\nnumber of " + j + " images = "
 		    + this.occurranceOfDigits[j]);
 	}
     }
 
     private void updateOccuranceOfDigits(int imageLabel) {
 	// imageLabel is always an integer between 0 - 9
 	switch (imageLabel) {
 	case 0:
 	    this.occurranceOfDigits[0]++;
 	    break;
 	case 1:
 	    this.occurranceOfDigits[1]++;
 	    break;
 	case 2:
 	    this.occurranceOfDigits[2]++;
 	    break;
 	case 3:
 	    this.occurranceOfDigits[3]++;
 	    break;
 	case 4:
 	    this.occurranceOfDigits[4]++;
 	    break;
 	case 5:
 	    this.occurranceOfDigits[5]++;
 	    break;
 	case 6:
 	    this.occurranceOfDigits[6]++;
 	    break;
 	case 7:
 	    this.occurranceOfDigits[7]++;
 	    break;
 	case 8:
 	    this.occurranceOfDigits[8]++;
 	    break;
 	case 9:
 	    this.occurranceOfDigits[9]++;
 	    break;
 	}
     }
 
     // private void trainMemoryClassifier() {
     // See which Columns become active most often for the same image type
     // }
     //
     // public void testWalnut() {
     //
     // }
 
     // track the frequency of ColumnPositions for each digit/label
 
     // then add most frequent ColumnPositions to Idea
 }
