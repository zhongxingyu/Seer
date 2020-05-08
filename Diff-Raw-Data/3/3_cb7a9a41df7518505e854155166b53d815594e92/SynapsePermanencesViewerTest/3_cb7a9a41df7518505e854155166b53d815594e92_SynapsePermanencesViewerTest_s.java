 package model.util;
 
 import java.util.Set;
 
 import model.MARK_I.ColumnPosition;
 
 import model.MARK_I.connectTypes.SensorCellsToRegionConnect;
 import model.MARK_I.connectTypes.SensorCellsToRegionRectangleConnect;
 
 import model.MARK_I.Region;
 import model.MARK_I.SpatialPooler;
 import com.google.gson.Gson;
 import java.io.IOException;
 import model.LGN;
 import model.Retina;
 
 /**
  * @author Quinn Liu (quinnliu@vt.edu)
  * @version July 29, 2013
  */
 public class SynapsePermanencesViewerTest extends junit.framework.TestCase {
     SynapsePermanencesViewer SPV;
     private Region region;
 
     public void setUp() throws IOException {
 	// not necessary
     }
 
     public void test_saveRegionToBeOpenedInSynapsePermanencesViewer()
 	    throws IOException {
 	this.region = new Region("Region", 8, 8, 1, 50, 10);
 
 	Retina retina = new Retina(66, 66);
 
 	SensorCellsToRegionConnect retinaToLGN = new SensorCellsToRegionRectangleConnect();
 	retinaToLGN.connect(retina.getVisionCells(), this.region, 0, 0);
 	retina.seeBMPImage("2.bmp");
 
 	SpatialPooler spatialPooler = new SpatialPooler(this.region);
 	spatialPooler.setLearningState(true);
 
 	for (int i = 0; i < 100; i++) {
 	    spatialPooler.performSpatialPoolingOnRegion();
 	}
 
 	Set<ColumnPosition> LGNNeuronActivity = spatialPooler
 		.getActiveColumnPositions();
 	assertEquals(11, LGNNeuronActivity.size());
 
 	Gson gson = new Gson();
 	String regionObject = gson.toJson(this.region);
 	JsonFileInputOutput
 		.saveObjectToTextFile(regionObject,
 			"./tests/model/util/test_saveRegionToBeOpenedInSynapsePermanencesViewer.txt");
     }
 
     public void test_openRegionToBeOpenedInSynapsePermanencesViewer()
 	    throws IOException {
 	String regionAsString = JsonFileInputOutput
 		.openObjectInTextFile("./tests/model/util/test_saveRegionToBeOpenedInSynapsePermanencesViewer.txt");
 	Gson gson = new Gson();
 	Region deserializedRegion = gson.fromJson(regionAsString, Region.class);
 
	// uncommenting the following code causes a build error with grade
	// in oracleJDK7 but is fine with openJDK7 & openJDK6

 	assertEquals("Region", deserializedRegion.getBiologicalName());
 	assertEquals(8, deserializedRegion.getXAxisLength());
 	assertEquals(8, deserializedRegion.getYAxisLength());
 	assertEquals(10, deserializedRegion.getDesiredLocalActivity());
 	assertEquals(5, deserializedRegion.getInhibitionRadius());
     }
 }
