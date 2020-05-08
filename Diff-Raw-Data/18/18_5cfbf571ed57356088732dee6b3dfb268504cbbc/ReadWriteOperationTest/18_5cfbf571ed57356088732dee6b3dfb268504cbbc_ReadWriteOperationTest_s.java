 package org.dawnsci.persistence.test.operations;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.lang.reflect.ParameterizedType;
 
 import org.dawb.common.services.ServiceManager;
 import org.dawnsci.persistence.internal.PersistJsonOperationHelper;
 import org.dawnsci.persistence.internal.PersistenceServiceImpl;
 import org.eclipse.dawnsci.analysis.api.dataset.Slice;
 import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
 import org.eclipse.dawnsci.analysis.api.metadata.OriginMetadata;
 import org.eclipse.dawnsci.analysis.api.persistence.IPersistenceService;
 import org.eclipse.dawnsci.analysis.api.persistence.IPersistentFile;
 import org.eclipse.dawnsci.analysis.api.processing.IOperation;
 import org.eclipse.dawnsci.analysis.api.processing.IOperationService;
 import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
 import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
 import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
 import org.eclipse.dawnsci.analysis.dataset.metadata.OriginMetadataImpl;
 import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
 import org.eclipse.dawnsci.analysis.dataset.roi.SectorROI;
 import org.eclipse.dawnsci.hdf5.HierarchicalDataFactory;
 import org.eclipse.dawnsci.hdf5.IHierarchicalDataFile;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import uk.ac.diamond.scisoft.analysis.fitting.functions.FunctionFactory;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.Gaussian;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.Lorentzian;
 import uk.ac.diamond.scisoft.analysis.processing.OperationServiceImpl;
 import uk.ac.diamond.scisoft.analysis.processing.operations.FunctionModel;
 
 public class ReadWriteOperationTest {
 	
 	
 	private static IOperationService   service;
 	private static IPersistenceService pservice;
 	
 	@BeforeClass
 	public static void before() throws Exception {
 
 		ServiceManager.setService(IOperationService.class, new OperationServiceImpl());
 		service = (IOperationService)ServiceManager.getService(IOperationService.class);
 		service.createOperations(service.getClass().getClassLoader(), "org.dawnsci.persistence.test.operations");
 		service.createOperations(service.getClass().getClassLoader(), "uk.ac.diamond.scisoft.analysis.processing.operations");
 		PersistJsonOperationHelper.setOperationService(service);
 		
 		pservice = new PersistenceServiceImpl();
 	}
 	
 	@Test
 	public void testFunctionSimple() throws Exception {
 		
 		final IOperation functionOp = service.findFirst("function");
 		
 		// y(x) = a_0 x^n + a_1 x^(n-1) + a_2 x^(n-2) + ... + a_(n-1) x + a_n
 		final IFunction poly = FunctionFactory.getFunction("Polynomial", 3/*x^2*/, 5.3/*x*/, 9.4/*m*/);
 		functionOp.setModel(new FunctionModel(poly));
 
 		final File tmp = File.createTempFile("Test", ".nxs");
 		tmp.deleteOnExit();
 		tmp.createNewFile();
 		
 		IPersistentFile file = pservice.createPersistentFile(tmp.getAbsolutePath());
 		try {
 			file.setOperations(functionOp);
 			
 		} finally {
 			file.close();
 		}
 		
 		file = pservice.createPersistentFile(tmp.getAbsolutePath());
 		try {
 
 			IOperation[] readOperations = file.getOperations();
 
 			final FunctionModel model = (FunctionModel)readOperations[0].getModel();
             if (!poly.equals(model.getFunction())) {
             	throw new Exception("Cannot get function from serialized file!");
             }
             
 		} finally {
 			file.close();
 		}
 	}
 	
 	@Test
 	public void testWriteReadOperations() throws Exception {
 		
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
 		
 		// TODO Must be closed in a try{} finally{} ?
 		IHierarchicalDataFile file = HierarchicalDataFactory.getWriter(tmp.getAbsolutePath());
 
 		util.writeOperations(file, new IOperation[]{op2});
 
 		IOperation[] readOperations = util.readOperations(file);
 
 		assertEquals(((JunkTestOperationModel)(readOperations[0].getModel())).getxDim(), 50);
 
 	}
 	
 	@Test
 	public void testWriteReadOperationRoiFuncData() throws Exception {
 		
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
 
 
 	}
 	
 	@Test
 	public void testWriteOrigin() throws Exception {
 
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
 		
 	}
 
 }
