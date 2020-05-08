 package model.MARK_I.vision;
 
 import java.util.Set;
 
 import model.MARK_I.ColumnPosition;
 
 import java.io.IOException;
 import model.MARK_I.connectTypes.SensorCellsToRegionConnect;
 import model.MARK_I.connectTypes.SensorCellsToRegionRectangleConnect;
 import model.Retina;
 import model.MARK_I.Region;
 import model.MARK_I.SpatialPooler;
 
 /**
  * -------------------------------Purpose---------------------------------------
  * To show the spatial pooling learning algorithm produces similar results even
  * after adding noise to the data.
  *
  * ------------------------------Experiment-------------------------------------
  * Run the spatial pooling algorithm on a Region of 2 different bitmap images.
  * The 2 images are both of the same circle but one of the images has some
  * random noise added on.
  *
  * ------------------------------Conclusion-------------------------------------
  * The spatial pooling algoithm does simple local computations on the image to
  * remove noise very efficiently up to a specific threshold that can vary
  * between locations in the input image.
  *
  * @author Quinn Liu (quinnliu@vt.edu)
  * @version Feb 2, 2014
  */
 public class NoiseInvarianceExperiment extends junit.framework.TestCase {
     private Retina retina;
     private Region region;
     private SpatialPooler spatialPooler;
 
     public void setUp() {
	// images in folder images/model/2.bmp and
	// images/model/2_with_noise.bmp are 66x66 pixels
 	this.retina = new Retina(66, 66);
 
 	this.region = new Region("Region", 8, 8, 1, 20, 3);
 
 	SensorCellsToRegionConnect retinaToLGN = new SensorCellsToRegionRectangleConnect();
 	retinaToLGN.connect(this.retina.getVisionCells(), this.region, 2, 2);
 
 	this.spatialPooler = new SpatialPooler(this.region);
 	this.spatialPooler.setLearningState(true);
     }
 
     public void test_runNoiseInvarianceExperiment() throws IOException {
 	this.retina.seeBMPImage("2.bmp");
 
 	this.spatialPooler.performSpatialPoolingOnRegion();
 	Set<ColumnPosition> columnActivityAfterSeeingImage2 = this.spatialPooler
 		.getActiveColumnPositions();
 
	this.retina.seeBMPImage("2_with_noise.bmp");
 	this.spatialPooler.performSpatialPoolingOnRegion();
 	Set<ColumnPosition> columnActivityAfterSeeingImage2_with_noise = this.spatialPooler
 		.getActiveColumnPositions();
 
 	// make sure that all column positions in the set
 	// columnActivityAfterSeeingImage2 and columnActivityAfterSeeingImage2_with_noise
 	// are the same
     }
 }
