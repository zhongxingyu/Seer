 package model.util;
 
 import java.util.Set;
 import model.MARK_II.ColumnPosition;
 import model.MARK_II.ConnectTypes.SensorCellsToRegionConnect;
 import model.MARK_II.ConnectTypes.SensorCellsToRegionRectangleConnect;
 import model.Retina;
 import model.MARK_II.VisionCell;
 import model.MARK_II.SpatialPooler;
 import model.MARK_II.ConnectTypes.RegionToRegionConnect;
 import model.MARK_II.ConnectTypes.RegionToRegionRectangleConnect;
 import model.MARK_II.Column;
 import model.MARK_II.Region;
 import java.util.List;
 import java.io.IOException;
 import com.google.gson.Gson;
 import model.theory.Memory;
 import model.theory.MemoryClassifier;
 import model.util.JsonFileInputOutput;
 
 /**
  * @author Quinn Liu (quinnliu@vt.edu)
  * @version MARK II | June 26, 2013
  */
 public class Test_JsonFileInputOutput extends junit.framework.TestCase {
     private Gson gson;
 
     public void setUp() {
 	this.gson = new Gson();
     }
 
     public void test_saveMemoryClassifierObject() throws IOException {
 	Memory digitsMemory = new Memory();
 
 	// TODO: train LGNStructure on many more different images of 2's
 
 	MemoryClassifier memoryClassifier_digits = new MemoryClassifier(
 		digitsMemory);
 
 	// save MemoryClassifier object as a JSON file
 	String memoryClassifierObject = this.gson
 		.toJson(memoryClassifier_digits);
 	JsonFileInputOutput.saveObjectToTextFile(memoryClassifierObject,
 		"./tests/model/util/test_saveMemoryClassifierObject.txt");
     }
 
     public void test_openMemoryClassifierObject() throws IOException {
 	String memoryClassifierAsString = JsonFileInputOutput
 		.openObjectInTextFile("./tests/model/util/test_saveMemoryClassifierObject.txt");
 	MemoryClassifier mc = this.gson.fromJson(memoryClassifierAsString,
 		MemoryClassifier.class);
     }
 
     public void test_saveRegionObject() throws IOException {
 	Region LGNRegion = new Region("LGNRegion", 8, 8, 1, 50, 3);
 
 	// construct Retina
 	VisionCell[][] visionCells = new VisionCell[65][65];
 	for (int x = 0; x < visionCells.length; x++) {
 	    for (int y = 0; y < visionCells[0].length; y++) {
 		visionCells[x][y] = new VisionCell();
 	    }
 	}
 	Retina retina = new Retina(visionCells);
 
 	SensorCellsToRegionConnect retinaToLGN = new SensorCellsToRegionRectangleConnect();
 	retinaToLGN.connect(retina.getVisionCells(), LGNRegion, 0, 0);
 
 	// run spatial pooling on a image
 	SpatialPooler spatialPooler = new SpatialPooler(LGNRegion);
 	spatialPooler.setLearningState(true);
 
 	retina.seeBMPImage("2.bmp");
	Set<ColumnPosition> LGNNeuronActivity = spatialPooler // <= Cannot both
							      // be uncommented
		.performSpatialPoolingOnRegion();

	assertEquals(11, LGNNeuronActivity.size());
 
 	Region trainedLGNRegion = spatialPooler.getRegion();
 	System.out.println(LGNRegion.toString());
 	System.out.println(trainedLGNRegion.toString());
 
 	Gson gson2 = new Gson();
 	String regionObject = gson2.toJson(trainedLGNRegion); // <== Cannot both be
 	// uncommented
 
 	// JsonFileInputOutput.saveObjectToTextFile(regionObject,
 	// "./tests/model/util/test_saveRegionObject.txt");
     }
 
     public void test_openRegionObject() throws IOException {
 	String regionAsString = JsonFileInputOutput
 		.openObjectInTextFile("./tests/model/util/test_saveRegionObject.txt");
 	Gson gson2 = new Gson();
 	//System.out.println(regionAsString);
 	Region LGNRegion = gson2.fromJson(regionAsString, Region.class);
 	//System.out.println(LGNRegion.toString());
     }
 }
