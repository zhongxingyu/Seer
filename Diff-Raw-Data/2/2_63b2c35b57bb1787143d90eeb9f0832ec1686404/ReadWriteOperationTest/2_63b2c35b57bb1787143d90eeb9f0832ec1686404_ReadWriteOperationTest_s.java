 package org.dawnsci.persistence.test.operations;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 import java.lang.reflect.ParameterizedType;
 
 import org.dawb.common.services.ServiceManager;
 import org.dawnsci.persistence.internal.PersistJsonOperationHelper;
 import org.eclipse.dawnsci.analysis.api.dataset.Slice;
 import org.eclipse.dawnsci.analysis.api.metadata.OriginMetadata;
 import org.eclipse.dawnsci.analysis.api.processing.IOperation;
 import org.eclipse.dawnsci.analysis.api.processing.IOperationService;
 import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
 import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
 import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
 import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
 import org.eclipse.dawnsci.analysis.dataset.roi.SectorROI;
 import org.eclipse.dawnsci.hdf5.HierarchicalDataFactory;
 import org.eclipse.dawnsci.hdf5.IHierarchicalDataFile;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import uk.ac.diamond.scisoft.analysis.fitting.functions.Gaussian;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.Lorentzian;
 import uk.ac.diamond.scisoft.analysis.metadata.OriginMetadataImpl;
 import uk.ac.diamond.scisoft.analysis.processing.OperationServiceImpl;
 
 public class ReadWriteOperationTest {
 	
 	
 	static IOperationService service;
 	
 	@BeforeClass
 	public static void before() throws Exception {
 
 		ServiceManager.setService(IOperationService.class, new OperationServiceImpl());
 		service = (IOperationService)ServiceManager.getService(IOperationService.class);
 		service.createOperations(service.getClass().getClassLoader(), "org.dawnsci.persistence.test.operations");
		
 	}
 	
 	@Test
 	public void testWriteReadOperations() {
 		
 		try {
 			
 			IOperation op2 = service.create("org.dawnsci.persistence.test.operations.JunkTestOperation");
 			Class modelType = (Class)((ParameterizedType)op2.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
 			IOperationModel model2  = (IOperationModel) modelType.newInstance();
 			op2.setModel(model2);
 			((JunkTestOperationModel)model2).setxDim(50);
 			
 			PersistJsonOperationHelper util = new PersistJsonOperationHelper();
 			String modelJson = util.getModelJson(model2);
 			
 			final File tmp = File.createTempFile("Test", ".nxs");
 			tmp.deleteOnExit();
 			tmp.createNewFile();
 			IHierarchicalDataFile file = HierarchicalDataFactory.getWriter(tmp.getAbsolutePath());
 			
 			util.writeOperations(file, new IOperation[]{op2});
 			
 			IOperation[] readOperations = util.readOperations(file);
 			
 			assertEquals(((JunkTestOperationModel)(readOperations[0].getModel())).getxDim(), 50);
 
 			
 		} catch (Exception e) {
 			fail(e.getMessage());
 		}
 	
 	}
 	
 	@Test
 	public void testWriteReadOperationRoiFuncData() {
 		
 		try {
 			
 			IOperation op2 = service.create("org.dawnsci.persistence.test.operations.JunkTestOperationROI");
 			Class modelType = (Class)((ParameterizedType)op2.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
 			JunkTestModelROI model2  = (JunkTestModelROI) modelType.newInstance();
 			
 			model2.setRoi(new SectorROI());
 			model2.setxDim(50);
 			model2.setBar(new Gaussian());
 			model2.setFoo(DatasetFactory.createRange(10, Dataset.INT32));
 			model2.setData(DatasetFactory.createRange(5, Dataset.INT32));
 			model2.setFunc(new Lorentzian());
 			model2.setRoi2(new RectangularROI());
 			op2.setModel(model2);
 			
 			PersistJsonOperationHelper util = new PersistJsonOperationHelper();
 			String modelJson = util.getModelJson(model2);
 			
 			final File tmp = File.createTempFile("Test", ".nxs");
 			tmp.deleteOnExit();
 			tmp.createNewFile();
 			IHierarchicalDataFile file = HierarchicalDataFactory.getWriter(tmp.getAbsolutePath());
 			
 			util.writeOperations(file, new IOperation[]{op2});
 			
 			IOperation[] readOperations = util.readOperations(file);
 			JunkTestModelROI mo = (JunkTestModelROI)readOperations[0].getModel();
 			assertEquals(mo.getxDim(), 50);
 			assertTrue(mo.getRoi() != null);
 			assertTrue(mo.getBar() != null);
 			assertTrue(mo.getFoo() != null);
 			assertTrue(mo.getData() != null);
 			assertTrue(mo.getFunc() != null);
 			assertTrue(mo.getRoi2() != null);
 			
 			
 
 			
 		} catch (Exception e) {
 			fail(e.getMessage());
 		}
 	
 	}
 	
 	@Test
 	public void testWriteOrigin() {
 		
 		try {
 			
 			Slice[] slices = Slice.convertFromString("0:10:2,2:20,:,:");
 			int[] dataDims = new int[]{2,3};
 			String path = "pathvalue";
 			String dsname = "dsname";
 			
 			
 			OriginMetadata om = new OriginMetadataImpl(null, slices, dataDims, path, dsname);
 			
 			PersistJsonOperationHelper util = new PersistJsonOperationHelper();
 			
 			final File tmp = File.createTempFile("Test", ".nxs");
 			tmp.deleteOnExit();
 			tmp.createNewFile();
 			IHierarchicalDataFile file = HierarchicalDataFactory.getWriter(tmp.getAbsolutePath());
 			
 			util.writeOriginalDataInformation(file, om);
 			
 			OriginMetadata outOm = util.readOriginalDataInformation(file);
 			outOm.toString();
 			
 		} catch (Exception e) {
 			fail(e.getMessage());
 		}
 	
 	}
 
 }
