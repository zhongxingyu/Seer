 package model.MARK_I;
 
 import model.MARK_I.connectTypes.RegionToRegionConnect;
 import model.MARK_I.connectTypes.RegionToRegionRectangleConnect;
 import model.MARK_I.connectTypes.SensorCellsToRegionConnect;
 import model.MARK_I.connectTypes.SensorCellsToRegionRectangleConnect;
 
 import model.MARK_I.ColumnPosition;
 import model.MARK_I.Neocortex;
 import model.MARK_I.Region;
 import model.MARK_I.SpatialPooler;
 
 import model.util.JsonFileInputOutput;
 import com.google.gson.Gson;
 import model.theory.MemoryClassifier;
 import model.theory.Memory;
 import java.util.Set;
 import model.theory.Idea;
 import java.io.IOException;
 import model.Retine;
 import model.NervousSystem;
 import model.LGN;
 
 /**
  * @author Quinn Liu (quinnliu@vt.edu)
  * @version Dec 28, 2013
  */
 public class HowToUseMARK_I extends junit.framework.TestCase {
     private NervousSystem nervousSystem;
 
     private Gson gson;
     private MemoryClassifier memoryClassifier_Digits;
 
     public void setUp() throws IOException {
 	this.nervousSystem = this.constructConnectedNervousSystem();
 
 	this.gson = new Gson();
 	this.memoryClassifier_Digits = this
 		.trainMemoryClassifierWithNervousSystem();
     }
 
     private NervousSystem constructConnectedNervousSystem() {
 	Neocortex unconnectedNeocortex = new Neocortex(new Region("V1", 4, 4,
 		4, 50, 3), new RegionToRegionRectangleConnect());
 
 	LGN unconnectedLGN = new LGN(
 		new Region("LGN", 8, 8, 1, 50, 3));
 
 	Retine unconnectedRetina = new Retine(65, 65);
 
 	NervousSystem nervousSystem = new NervousSystem(unconnectedNeocortex,
 		unconnectedLGN, unconnectedRetina);
 
 	// connect Retine to LGN
 	Retine retine = nervousSystem.getPNS().getSNS().getRetine();
 	LGN LGN = nervousSystem.getCNS().getBrain()
 		.getThalamus().getLGN();
 	SensorCellsToRegionConnect sensorCellsToRegionConnect = new SensorCellsToRegionRectangleConnect();
 	sensorCellsToRegionConnect.connect(retine.getVisionCells(),
 		LGN.getRegion(), 0, 0);
 
 	// connect LGN to V1 Region of Neocortex
 	Neocortex neocortex = nervousSystem.getCNS().getBrain().getCerebrum()
 		.getCerebralCortex().getNeocortex();
 	RegionToRegionConnect regionToRegionConnect = new RegionToRegionRectangleConnect();
 	regionToRegionConnect.connect(LGN.getRegion(),
 		neocortex.getCurrentRegion(), 0, 0);
 
 	return nervousSystem;
     }
 
     private MemoryClassifier trainMemoryClassifierWithNervousSystem()
 	    throws IOException {
 	Retine retina = nervousSystem.getPNS().getSNS().getRetine();
 
 	Region LGNRegion = nervousSystem.getCNS().getBrain().getThalamus()
 		.getLGN().getRegion();
 
 	// Region V1 = nervousSystem.getCNS().getBrain().getCerebrum()
 	// .getCerebralCortex().getNeocortex().getCurrentRegion();
 
 	// -------------train NervousSystem update Memory----------------
 	retina.seeBMPImage("2.bmp");
 
 	SpatialPooler spatialPooler = new SpatialPooler(LGNRegion);
 	spatialPooler.setLearningState(true);
 	spatialPooler.performSpatialPoolingOnRegion();
 	Set<ColumnPosition> LGNNeuronActivity = spatialPooler
 		.getActiveColumnPositions();
 
 	assertEquals(11, LGNNeuronActivity.size());
 
 	// save LGNRegion to be viewed
 	// String regionObject = this.gson1.toJson(LGNRegion);
 	// JsonFileInputOutput.saveObjectToTextFile(regionObject,
 	// "./train/model/MARK_I/Region_LGN.txt");
 
 	Idea twoIdea = new Idea("two", LGNRegion);
 	twoIdea.unionColumnPositions(LGNNeuronActivity);
 
 	Memory digitsMemory = new Memory();
 	digitsMemory.addNewIdea(twoIdea);
 
 	// TODO: train LGNStructure on many more different images of 2's
 
 	MemoryClassifier memoryClassifier_digits = new MemoryClassifier(
 		digitsMemory);
 
 	// save MemoryClassifier object as a JSON file
 	String memoryClassifierObject = this.gson
 		.toJson(memoryClassifier_digits);
 	JsonFileInputOutput.saveObjectToTextFile(memoryClassifierObject,
 		"./train/model/MARK_I/MemoryClassifier_Digits.txt");
 
 	return memoryClassifier_digits;
     }
 
    /**
     * Use the build NervousSystem instance and use the retine to view a .bmp
     * image.
     * @throws IOException
     */
     public void test_MemoryClassifierOnNewImages() throws IOException {
 	String memoryClassifierAsString = JsonFileInputOutput
 		.openObjectInTextFile("./train/model/MARK_I/MemoryClassifier_Digits.txt");
 	MemoryClassifier mc = this.gson.fromJson(memoryClassifierAsString,
 		MemoryClassifier.class);
 	// System.out.println(mc.toString());
 
	Retine retine = nervousSystem.getPNS().getSNS().getRetine();
 
 	Region LGNStructure = nervousSystem.getCNS().getBrain().getThalamus()
 		.getLGN().getRegion();
 
	// retine.seeBMPImage("new2.bmp");
 	// digitsSVM.updateIdeas(spatialPooler.performSpatialPoolingOnRegion());
 	// digitsSVM.toString();
     }
 }
