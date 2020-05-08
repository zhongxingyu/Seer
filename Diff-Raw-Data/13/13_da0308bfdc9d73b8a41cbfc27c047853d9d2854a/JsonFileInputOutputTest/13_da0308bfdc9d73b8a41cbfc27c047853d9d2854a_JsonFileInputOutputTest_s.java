 package model.util;
 
 import model.MARK_I.Column;
 import java.util.Set;
 import model.theory.Idea;
 import model.MARK_I.connectTypes.SensorCellsToRegionConnect;
 import model.MARK_I.connectTypes.SensorCellsToRegionRectangleConnect;
 import model.MARK_I.Region;
 import model.MARK_I.SpatialPooler;
 import model.Retina;
 import java.io.IOException;
 import com.google.gson.Gson;
 import model.theory.Memory;
 import model.theory.MemoryClassifier;
 import model.util.JsonFileInputOutput;
 
 /**
  * @author Quinn Liu (quinnliu@vt.edu)
  * @version Feb 1, 2014
  */
 public class JsonFileInputOutputTest extends junit.framework.TestCase {
     private Gson gson;
 
     public void setUp() {
 	this.gson = new Gson();
     }
 
     public void test_saveMemoryClassifierObject() throws IOException {
 	Memory digitsMemory = new Memory();
 
 	digitsMemory.addNewIdea(new Idea("love your enemy", new Region("V1", 2,
 		2, 1, 0.2, 2)));
 	MemoryClassifier memoryClassifier_digits = new MemoryClassifier(
 		digitsMemory);
 
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
 
 	assertEquals("love your enemy",
 		mc.getMemory().getIdea("love your enemy").getName());
     }
 
     public void test_saveRegionObject() throws IOException {
 	Region LGNRegion = new Region("LGNRegion", 8, 8, 1, 50, 3);
 
 	Retina retina = new Retina(66, 66);
 
 	SensorCellsToRegionConnect retinaToLGN = new SensorCellsToRegionRectangleConnect();
 	retinaToLGN.connect(retina.getVisionCells(), LGNRegion, 0, 0);
 
 	// run spatial pooling on a image
 	SpatialPooler spatialPooler = new SpatialPooler(LGNRegion);
 	spatialPooler.setLearningState(true);
 
 	retina.seeBMPImage("2.bmp");
 	Set<Column> LGNNeuronActivity = spatialPooler
 		.performSpatialPoolingOnRegion();
 
 	assertEquals(11, LGNNeuronActivity.size());
 
 	Gson gson2 = new Gson();
 	Region trainedLGNRegion = spatialPooler.getRegion();
 	String regionObject = gson2.toJson(trainedLGNRegion);
 
 	JsonFileInputOutput.saveObjectToTextFile(regionObject,
 		"./tests/model/util/test_saveRegionObject.txt");
     }
 
     public void test_openRegionObject() throws IOException {
 	String regionAsString = JsonFileInputOutput
 		.openObjectInTextFile("./tests/model/util/test_saveRegionObject.txt");
 
 	Gson gson2 = new Gson();
 	Region trainedLGNRegion = gson2.fromJson(regionAsString, Region.class);
	assertEquals("LGNRegion", trainedLGNRegion.getBiologicalName());
     }
 }
