 package net.bioclipse.brunn.tests.business;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.sql.Timestamp;
 import java.util.List;
 import java.util.Set;
 
 import net.bioclipse.brunn.business.IAuditService;
 import net.bioclipse.brunn.business.annotation.AbstractDAOBasedAnnotationManager;
 import net.bioclipse.brunn.business.annotation.AnnotationManager;
 import net.bioclipse.brunn.business.audit.AbstractDAOBasedAuditManager;
 import net.bioclipse.brunn.business.audit.AuditManager;
 import net.bioclipse.brunn.business.operation.AbstractDAOBasedOperationManager;
 import net.bioclipse.brunn.business.operation.OperationManager;
 import net.bioclipse.brunn.business.origin.AbstractDAOBasedOriginManager;
 import net.bioclipse.brunn.business.origin.OriginManager;
 import net.bioclipse.brunn.business.plate.AbstractDAOBasedPlateManager;
 import net.bioclipse.brunn.business.plate.PlateManager;
 import net.bioclipse.brunn.business.plateLayout.AbstractDAOBasedPlateLayoutManager;
 import net.bioclipse.brunn.business.plateLayout.PlateLayoutManager;
 import net.bioclipse.brunn.genericDAO.IAnnotationDAO;
 import net.bioclipse.brunn.genericDAO.IAnnotationInstanceDAO;
 import net.bioclipse.brunn.genericDAO.IAuditLogDAO;
 import net.bioclipse.brunn.genericDAO.ICellOriginDAO;
 import net.bioclipse.brunn.genericDAO.ICellSampleDAO;
 import net.bioclipse.brunn.genericDAO.IDrugOriginDAO;
 import net.bioclipse.brunn.genericDAO.IDrugSampleDAO;
 import net.bioclipse.brunn.genericDAO.IFolderDAO;
 import net.bioclipse.brunn.genericDAO.IInstrumentDAO;
 import net.bioclipse.brunn.genericDAO.IMasterPlateDAO;
 import net.bioclipse.brunn.genericDAO.IMeasurementDAO;
 import net.bioclipse.brunn.genericDAO.IPlateDAO;
 import net.bioclipse.brunn.genericDAO.IPlateLayoutDAO;
 import net.bioclipse.brunn.genericDAO.IPlateTypeDAO;
 import net.bioclipse.brunn.genericDAO.IResultTypeDAO;
 import net.bioclipse.brunn.genericDAO.ISampleContainerDAO;
 import net.bioclipse.brunn.genericDAO.IUserDAO;
 import net.bioclipse.brunn.genericDAO.IWellDAO;
 import net.bioclipse.brunn.pojos.AbstractAnnotationInstance;
 import net.bioclipse.brunn.pojos.AbstractAuditableObject;
 import net.bioclipse.brunn.pojos.Annotation;
 import net.bioclipse.brunn.pojos.AnnotationType;
 import net.bioclipse.brunn.pojos.AuditLog;
 import net.bioclipse.brunn.pojos.AuditType;
 import net.bioclipse.brunn.pojos.CellOrigin;
 import net.bioclipse.brunn.pojos.DrugOrigin;
 import net.bioclipse.brunn.pojos.FloatAnnotation;
 import net.bioclipse.brunn.pojos.ILISObject;
 import net.bioclipse.brunn.pojos.Instrument;
 import net.bioclipse.brunn.pojos.MasterPlate;
 import net.bioclipse.brunn.pojos.Measurement;
 import net.bioclipse.brunn.pojos.Plate;
 import net.bioclipse.brunn.pojos.PlateLayout;
 import net.bioclipse.brunn.pojos.PlateType;
 import net.bioclipse.brunn.pojos.ResultType;
 import net.bioclipse.brunn.pojos.SampleContainer;
 import net.bioclipse.brunn.pojos.User;
 import net.bioclipse.brunn.pojos.Well;
 import net.bioclipse.brunn.pojos.WorkList;
 import net.bioclipse.brunn.tests.BaseTest;
 import net.bioclipse.brunn.tests.TestConstants;
 
 import org.apache.tools.ant.Project;
 import org.hibernate.SessionFactory;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * Tests that the <code>AuditService</code> gets called when it should 
  * 
  * @author jonathan
  *
  */
 public class AuditInvokedTest extends BaseTest {
 
 	private MockAuditService auditService;
 	
 	
 	public AuditInvokedTest(){
 		super();
 	}
 	
 	@Before
 	public void setUp() throws Exception {
 		super.setUp();
 		auditService = new MockAuditService();
 		
 	}
 	
 	@After
 	public void tearDown() throws Exception {
 		super.tearDown();
 	}
 	
 	private void performAssert(int expected) {
 		assertEquals("The Audit Service was not invoked", expected, auditService.getCallCount());
 	}
 
 /*
  * TEST THE ANNOTATION MANAGER'S METHODS
  */
 	/**
 	 * tests that the <code>AuditService</code> gets called when creating an <code>Annotation</code>
 	 */
 	@Test
 	public void testNewAnnotation() {
 		AbstractDAOBasedAnnotationManager am = new AnnotationManager();
 		
 		am.setAnnotationDAO(new MockAnnotationDAO());
 		am.setAnnotationInstanceDAO(new MockAnnotationInstanceDAO());
 		am.setAuditService(auditService);
 		
 		am.createAnnotation( tester, AnnotationType.FLOAT_ANNOTATION, "annotation", null);
 		
 		performAssert(1);
 	}
 	
 	/**
 	 * tests that the <code>AuditService</code> gets called when deleting 
 	 * an <code>AbstractAnnotationInstance</code>
 	 */
 	@Test
 	public void testDeleteAbstractAnnotationInstance(){
 		
 		
 
 		Annotation annotation = am.getAnnotation(am.createAnnotation( tester, 
 				                                  AnnotationType.FLOAT_ANNOTATION, 
 				                                  "annotation", 
 				                                  null));
 		
 		FloatAnnotation annotationInstance = 
 			(FloatAnnotation)am.getAnnotationInstance(am.annotate(tester, plate, annotation, 23.0));
 		
 		AbstractDAOBasedAnnotationManager am = new AnnotationManager();
 		
 		am.setAnnotationDAO(new MockAnnotationDAO());
 		am.setAnnotationInstanceDAO(new MockAnnotationInstanceDAO());
 		am.setAuditService(auditService);
 		
 		am.delete(tester, annotationInstance);
 		
 		performAssert(1);
 	}
 	
 	/**
 	 * tests that the <code>AuditService</code> gets called when deleting 
 	 * an <code>Annotation</code>
 	 */
 	@Test
 	public void testDeleteAnnotation(){
 		
 		Annotation annotation = am.getAnnotation(am.createAnnotation( tester, 
 				                                  AnnotationType.FLOAT_ANNOTATION, 
 				                                  "annotation", 
 				                                  null));
 		
 		AbstractDAOBasedAnnotationManager am = new AnnotationManager();
 		
 		am.setAnnotationDAO(new MockAnnotationDAO());
 		am.setAnnotationInstanceDAO(new MockAnnotationInstanceDAO());
 		am.setAuditService(auditService);
 
 		
 		am.delete(tester, annotation);
 		performAssert(1);
 	}
 	
 	/**
 	 * tests that the <code>AuditService</code> gets called when editing 
 	 * an <code>AbstractAnnotationInstance</code>
 	 */
 	@Test
 	public void testEditAbstractAnnotationInstance(){
 		
 		Annotation annotation = am.getAnnotation(am.createAnnotation( tester, 
 				                                  AnnotationType.FLOAT_ANNOTATION, 
 				                                  "annotation", 
 				                                  null));
 		
 		FloatAnnotation annotationInstance = 
 			(FloatAnnotation)am.getAnnotationInstance(am.annotate(tester, plate, annotation, 23.0));
 		annotationInstance.setValue(24.3);
 		
 		AbstractDAOBasedAnnotationManager am = new AnnotationManager();
 		
 		am.setAnnotationDAO(new MockAnnotationDAO());
 		am.setAnnotationInstanceDAO(new MockAnnotationInstanceDAO());
 		am.setAuditService(auditService);
 		
 		am.edit(tester, annotationInstance);
 
 		performAssert(1);
 	}
 	
 	/**
 	 * tests that the <code>AuditService</code> gets called when editing 
 	 * an <code>Annotation</code>
 	 */
 	@Test
 	public void testEditAnnotation(){
 		
 		Annotation annotation = am.getAnnotation(am.createAnnotation( tester, 
 				                                  AnnotationType.FLOAT_ANNOTATION, 
 				                                  "annotation", 
 				                                  null));
 		
 		annotation.setName("an annotation");
 
 		AbstractDAOBasedAnnotationManager am = new AnnotationManager();
 		
 		am.setAnnotationDAO(new MockAnnotationDAO());
 		am.setAnnotationInstanceDAO(new MockAnnotationInstanceDAO());
 		am.setAuditService(auditService);
 
 		am.edit(tester, annotation);
 		
 		performAssert(1);
 	}
 /*
  * TEST THE OPERATION MANAGER'S METHODS
  */
 	/**
 	 * tests that the <code>AuditService</code> gets called when creating an <code>User</code>
 	 */
 	@Test
 	public void testNewUser() {
 		AbstractDAOBasedAuditManager am = new AuditManager();
 		
 		IUserDAO userDAO = new MockUserDAO();
 		IAuditLogDAO auditLogDAO = new MockAuditLogDAO();
 		
 		am.setUserDAO(userDAO);
 		am.setAuditLogDAO(auditLogDAO);
 		am.setAuditService(auditService);
 
 		am.createUser(tester, "name", "", false);
 		
 		performAssert(1);
 	}
 	
 	/**
 	 * tests that the <code>AuditService</code> gets called when editing an <code>User</code>
 	 */
 	@Test
 	public void testEditUser() {
 		
 		User user = aum.getUser(aum.createUser(tester, "name", "", false));
 		user.setName("user");
 
 		AbstractDAOBasedAuditManager am = new AuditManager();
 		
 		IUserDAO userDAO = new MockUserDAO();
 		IAuditLogDAO auditLogDAO = new MockAuditLogDAO();
 		am.setAuditService(auditService);
 
 		am.setUserDAO(userDAO);
 		am.setAuditLogDAO(auditLogDAO);
 
 		am.edit(user);
 		
 		performAssert(1);
 	}
 	
 	/**
 	 * tests that the <code>AuditService</code> gets called when deleteing an <code>User</code>
 	 */
 	@Test
 	public void testDeleteUser() {
 		
 
 		User user = aum.getUser(aum.createUser(tester, "name", "", false));
 		
 		AbstractDAOBasedAuditManager am = new AuditManager();
 		
 		IUserDAO userDAO = new MockUserDAO();
 		IAuditLogDAO auditLogDAO = new MockAuditLogDAO();
 		
 		am.setUserDAO(userDAO);
 		am.setAuditLogDAO(auditLogDAO);
 		am.setAuditService(auditService);
 		
 		am.delete(user);
 		
 		performAssert(1);
 	}
 /*
  * TEST THE OPERATION MANAGER'S METHODS
  */
 	/**
 	 * tests that the <code>AuditService</code> gets called when creating an <code>Instrument</code>
 	 */
 	@Test
 	public void testCreateNewInstrument() {
 		AbstractDAOBasedOperationManager om = new OperationManager();
 		
 		IInstrumentDAO instrumentDAO = new MockInstrumentDAO();
 		IMeasurementDAO measurementDAO = new MockMeasurementDAO();
 		IResultTypeDAO resultTypeDAO = new MockResultTypeDAO();
 		
 		om.setInstrumentDAO(instrumentDAO);
 		om.setMeasurementDAO(measurementDAO);
 		om.setResultTypeDAO(resultTypeDAO);
 		om.setAuditService(auditService);
 
 		om.createInstrument(tester, "name");
 		
 		performAssert(1);
 	}
 	
 	/**
 	 * tests that the <code>AuditService</code> gets called when creating an <code>ResultType</code>
 	 */
 	@Test
 	public void testCreateNewResultType() {
 		AbstractDAOBasedOperationManager om = new OperationManager();
 		
 		IInstrumentDAO instrumentDAO = new MockInstrumentDAO();
 		IMeasurementDAO measurementDAO = new MockMeasurementDAO();
 		IResultTypeDAO resultTypeDAO = new MockResultTypeDAO();
 		
 		om.setInstrumentDAO(instrumentDAO);
 		om.setMeasurementDAO(measurementDAO);
 		om.setResultTypeDAO(resultTypeDAO);
 		om.setAuditService(auditService);
 
 		om.createResultType(tester, "name", 1);
 		
 		performAssert(1);
 	}
 	
 	/**
 	 * tests that the <code>AuditService</code> gets called when creating a <code>Measurement</code>
 	 */
 	@Test
 	public void testCreateNewMeasurement() {
 		AbstractDAOBasedOperationManager om = new OperationManager();
 		
 		IInstrumentDAO instrumentDAO           = new MockInstrumentDAO();
 		IMeasurementDAO measurementDAO         = new MockMeasurementDAO();
 		IResultTypeDAO resultTypeDAO           = new MockResultTypeDAO();
 		ISampleContainerDAO sampleContainerDAO = new MockSampleContainerDAO();
		IPlateDAO plateDAO                     = new MockPlateDAO();
 		
 		om.setInstrumentDAO(instrumentDAO);
 		om.setMeasurementDAO(measurementDAO);
 		om.setResultTypeDAO(resultTypeDAO);
 		om.setSampleContainerDAO(sampleContainerDAO);
 		om.setAuditService(auditService);
		om.setPlateDAO(plateDAO);
 
 		om.createMeasurement(tester, "name", workList, instrument, resultType);
 		
 		performAssert(1);
 	}
 		
 	/**
 	 * tests that the <code>AuditService</code> gets called when editing an <code>Instrument</code>
 	 */
 	@Test
 	public void testEditInstrument() {
 		
 		Instrument instrument = om.getInstrument(om.createInstrument(tester, "name"));
 		instrument.setName("instrument");
 		
 		AbstractDAOBasedOperationManager om = new OperationManager();
 		
 		IInstrumentDAO instrumentDAO = new MockInstrumentDAO();
 		IMeasurementDAO measurementDAO = new MockMeasurementDAO();
 		IResultTypeDAO resultTypeDAO = new MockResultTypeDAO();
 		
 		om.setInstrumentDAO(instrumentDAO);
 		om.setMeasurementDAO(measurementDAO);
 		om.setResultTypeDAO(resultTypeDAO);
 		om.setAuditService(auditService);
 		
 		om.edit(tester, instrument);
 		
 		performAssert(1);
 	}
 	
 	/**
 	 * tests that the <code>AuditService</code> gets called when editing a <code>ResultType</code>
 	 */
 	@Test
 	public void testEditResultType() {
 		
 		ResultType resultType = om.getResultType(om.createResultType(tester, "name", 1));
 		resultType.setName("resultType");
 		
 		AbstractDAOBasedOperationManager om = new OperationManager();
 		
 		IInstrumentDAO instrumentDAO = new MockInstrumentDAO();
 		IMeasurementDAO measurementDAO = new MockMeasurementDAO();
 		IResultTypeDAO resultTypeDAO = new MockResultTypeDAO();
 		
 		om.setInstrumentDAO(instrumentDAO);
 		om.setMeasurementDAO(measurementDAO);
 		om.setResultTypeDAO(resultTypeDAO);
 		om.setAuditService(auditService);
 		
 		om.edit(tester, resultType);
 		
 		performAssert(1);
 	}
 	
 	/**
 	 * tests that the <code>AuditService</code> gets called when editing a <code>Measurement</code>
 	 */
 	@Test
 	public void testEditMeasurement() {
 		
 		Measurement measurement = om.getMeasurement(om.createMeasurement(tester, "name", workList, instrument, resultType));
 		
 		measurement.setName("measurement");
 		
 		AbstractDAOBasedOperationManager om = new OperationManager();
 		
 		IInstrumentDAO instrumentDAO = new MockInstrumentDAO();
 		IMeasurementDAO measurementDAO = new MockMeasurementDAO();
 		IResultTypeDAO resultTypeDAO = new MockResultTypeDAO();
 		
 		om.setInstrumentDAO(instrumentDAO);
 		om.setMeasurementDAO(measurementDAO);
 		om.setResultTypeDAO(resultTypeDAO);
 		om.setAuditService(auditService);
 
 		om.edit(tester, measurement);
 		performAssert(1);
 	}
 	
 	/**
 	 * tests that the <code>AuditService</code> gets called when deleteing an <code>Instrument</code>
 	 */
 	@Test
 	public void testDeleteInstrument() {
 		
 		Instrument instrument = om.getInstrument(om.createInstrument(tester, "name"));
 		
 		AbstractDAOBasedOperationManager om = new OperationManager();
 		
 		IInstrumentDAO instrumentDAO = new MockInstrumentDAO();
 		IMeasurementDAO measurementDAO = new MockMeasurementDAO();
 		IResultTypeDAO resultTypeDAO = new MockResultTypeDAO();
 		
 		om.setInstrumentDAO(instrumentDAO);
 		om.setMeasurementDAO(measurementDAO);
 		om.setResultTypeDAO(resultTypeDAO);
 		om.setAuditService(auditService);
 		
 		om.delete(tester, instrument);
 		
 		performAssert(1);
 	}
 	
 	/**
 	 * tests that the <code>AuditService</code> gets called when deleteing a <code>ResultType</code>
 	 */
 	@Test
 	public void testDeleteResultType() {
 		
 		ResultType resultType = om.getResultType(om.createResultType(tester, "name", 1));
 		
 		AbstractDAOBasedOperationManager om = new OperationManager();
 		
 		IInstrumentDAO instrumentDAO = new MockInstrumentDAO();
 		IMeasurementDAO measurementDAO = new MockMeasurementDAO();
 		IResultTypeDAO resultTypeDAO = new MockResultTypeDAO();
 		
 		om.setInstrumentDAO(instrumentDAO);
 		om.setMeasurementDAO(measurementDAO);
 		om.setResultTypeDAO(resultTypeDAO);
 		om.setAuditService(auditService);
 
 		om.delete(tester, resultType);
 		
 		performAssert(1);
 	}
 	
 	/**
 	 * tests that the <code>AuditService</code> gets called when deleteing a <code>Measurement</code>
 	 */
 	@Test
 	public void testDeleteMeasurement() {
 		
 		Measurement measurement = om.getMeasurement(om.createMeasurement(tester, "name", workList, instrument, resultType));
 		
 		AbstractDAOBasedOperationManager om = new OperationManager();
 		
 		IInstrumentDAO instrumentDAO = new MockInstrumentDAO();
 		IMeasurementDAO measurementDAO = new MockMeasurementDAO();
 		IResultTypeDAO resultTypeDAO = new MockResultTypeDAO();
 		
 		om.setInstrumentDAO(instrumentDAO);
 		om.setMeasurementDAO(measurementDAO);
 		om.setResultTypeDAO(resultTypeDAO);
 		om.setAuditService(auditService);
 
 		om.delete(tester, measurement);
 		performAssert(1);
 	}
 	
 	/**
 	 * tests that the <code>AuditService</code> gets called when adding a <code>Result</code>
 	 */
 	@Test
 	public void testAddResult() {
 		
 		Measurement measurement = om.getMeasurement(om.createMeasurement(tester, "name", workList, instrument, resultType));
 		
 		double[] result = {0.2, 0.5, 0.34};
 
 		AbstractDAOBasedOperationManager om = new OperationManager();
 		
 		IInstrumentDAO instrumentDAO = new MockInstrumentDAO();
 		IMeasurementDAO measurementDAO = new MockMeasurementDAO();
 		IResultTypeDAO resultTypeDAO = new MockResultTypeDAO();
 		
 		om.setInstrumentDAO(instrumentDAO);
 		om.setMeasurementDAO(measurementDAO);
 		om.setResultTypeDAO(resultTypeDAO);
 		om.setAuditService(auditService);
 
 		om.addResult(tester, measurement, result);
 		
 		performAssert(1);
 	}
 	
 /*
  * TEST THE ORIGIN MANAGER'S METHODS
  */
 	/**
 	 * tests that the <code>AuditService</code> gets called when creating a <code>DrugOrigin</code>
 	 * @throws IOException 
 	 * @throws FileNotFoundException 
 	 */
 	@Test
 	public void testCreateDrugOrigin() throws FileNotFoundException, IOException{
 		
 		orm.setAuditService(auditService);
 		
 		orm.createDrugOrigin(tester, "name", new FileInputStream(TestConstants.getTestMolFile()), 23.0, compounds);
 		
 		performAssert(2);
 	}
 	
 	/**
 	 * tests that the <code>AuditService</code> gets called when creating a <code>CellOrigin</code>
 	 */
 	@Test
 	public void testCreateCellOrigin(){
 		
 		orm.setAuditService(auditService);
 		
 		orm.createCellOrigin(tester, "name", cellTypes);
 		
 		performAssert(2);
 	}
 	
 	/**
 	 * tests that the <code>AuditService</code> gets called when editing a <code>DrugOrigin</code>
 	 * @throws IOException 
 	 * @throws FileNotFoundException 
 	 */
 	@Test
 	public void testEditDrugOrigin() throws FileNotFoundException, IOException{
 		
 		DrugOrigin drugOrigin = orm.getDrugOrigin(orm.createDrugOrigin(tester, "name", new FileInputStream(TestConstants.getTestMolFile()), 23.0, compounds));
 		drugOrigin.setName("drugOrigin");
 		
 		AbstractDAOBasedOriginManager om = new OriginManager();
 		IDrugOriginDAO drugOriginDAO = new MockDrugOriginDAO();
 		
 		om.setDrugOriginDAO(drugOriginDAO);
 		om.setAuditService(auditService);
 		
 		om.edit(tester, drugOrigin);
 		
 		performAssert(1);
 	}
 	
 	/**
 	 * tests that the <code>AuditService</code> gets called when editing a <code>CellOrigin</code>
 	 */
 	@Test
 	public void testEditCellOrigin(){
 		
 		CellOrigin cellOrigin = orm.getCellOrigin(orm.createCellOrigin(tester, "name", cellTypes));
 		cellOrigin.setName("cellOrigin");
 		
 		AbstractDAOBasedOriginManager om = new OriginManager();
 		ICellOriginDAO cellOriginDAO = new MockCellOriginDAO();
 		
 		om.setCellOriginDAO(cellOriginDAO);
 		om.setAuditService(auditService);
 		om.edit(tester, cellOrigin);
 		
 		performAssert(1);
 	}
 	
 	/**
 	 * tests that the <code>AuditService</code> gets called when deleting a <code>DrugOrigin</code>
 	 * @throws IOException 
 	 * @throws FileNotFoundException 
 	 */
 	@Test
 	public void testDeleteDrugOrigin() throws FileNotFoundException, IOException{
 				
 		DrugOrigin drugOrigin = orm.getDrugOrigin(orm.createDrugOrigin(tester, "name", new FileInputStream(TestConstants.getTestMolFile()), 23.0, compounds));
 		
 		AbstractDAOBasedOriginManager om = new OriginManager();
 		IDrugOriginDAO drugOriginDAO = new MockDrugOriginDAO();
 		
 		om.setDrugOriginDAO(drugOriginDAO);
 		om.setAuditService(auditService);
 		
 		om.delete(tester, drugOrigin);
 		
 		performAssert(1);
 	}
 	
 	/**
 	 * tests that the <code>AuditService</code> gets called when deleting a <code>CellOrigin</code>
 	 */
 	@Test
 	public void testDeleteCellOrigin(){
 				
 		CellOrigin cellOrigin = orm.getCellOrigin(orm.createCellOrigin(tester, "name", cellTypes));
 		
 		AbstractDAOBasedOriginManager om = new OriginManager();
 		ICellOriginDAO cellOriginDAO = new MockCellOriginDAO();
 		
 		om.setCellOriginDAO(cellOriginDAO);
 		om.setAuditService(auditService);
 		
 		om.delete(tester, cellOrigin);
 		
 		performAssert(1);
 	}
 /*
  * TEST THE PLATE MANAGER'S METHODS
  */
 	/**
 	 * tests that the <code>AuditService</code> gets called when creating a <code>Plate</code>
 	 */
 	@Test
 	public void testCreatePlate(){
 						
 		AbstractDAOBasedPlateManager pm = new PlateManager();
 		IPlateDAO plateDAO = new MockPlateDAO();
 		IFolderDAO       folderDAO       = (IFolderDAO)context.getBean("folderDAO");
 		
 		
 		pm.setMasterPlateDAO((IMasterPlateDAO)context.getBean("masterPlateDAO"));
 		pm.setPlateDAO(plateDAO);
 		pm.setAuditService(auditService);
 		pm.setFolderDAO(folderDAO);
 		pm.setCellSampleDAO(           (ICellSampleDAO)context.getBean("cellSampleDAO")      );
 		pm.setSampleContainerDAO( (ISampleContainerDAO)context.getBean("sampleContainerDAO") );
 		pm.setDrugSampleDAO(           (IDrugSampleDAO)context.getBean("drugSampleDAO")      );
 		pm.setPlateDAO(                     (IPlateDAO)context.getBean("plateDAO")           );
 		pm.setUserDAO(                       (IUserDAO)context.getBean("userDAO")            );
 		
 		pm.createPlate(tester, "my plate", "P14t3", folder, masterPlate, cellOrigin, new Timestamp(System.currentTimeMillis()));
 		performAssert(97);
 	}
 	
 	/**
 	 * tests that the <code>AuditService</code> gets called when creating a <code>MasterPlate</code>
 	 */
 	@Test
 	public void testCreateMasterPlate(){
 		AbstractDAOBasedPlateManager pm = new PlateManager();
 		
 		IMasterPlateDAO  masterPlateDAO  = new MockMasterPlateDAO();
 		IFolderDAO       folderDAO       = (IFolderDAO)context.getBean("folderDAO");
 		IUserDAO         userDAO         = (IUserDAO)context.getBean("userDAO");
 		
 		pm.setMasterPlateDAO(masterPlateDAO);
 		pm.setFolderDAO(folderDAO);
 		pm.setUserDAO(userDAO);
 		pm.setAuditService(auditService);
 		
 		pm.createMasterPlate(tester, "masterPlate", plateLayout, masterPlates, 1);
 		
 		performAssert(1);
 	}
 	
 	/**
 	 * tests that the <code>AuditService</code> gets called when creating a <code>WellFunction</code>
 	 */
 	@Test
 	public void testCreateWellFunction(){
 				
 		MasterPlate masterPlate = pm.getMasterPlate(pm.createMasterPlate(tester, "masterPlate", plateLayout, masterPlates, 1));
 		
 		AbstractDAOBasedPlateManager pm = new PlateManager();
 		IMasterPlateDAO  masterPlateDAO = new MockMasterPlateDAO();
 		IWellDAO                wellDAO = new MockWellDAO();
 		
 		pm.setMasterPlateDAO(masterPlateDAO);
 		pm.setAuditService(auditService);
 		pm.setWellDAO(wellDAO);
 		
 		pm.createWellFunction(tester, "test", new Well(tester, "test", 1, 'a', masterPlate), "");
 		
 		performAssert(1);
 	}
 	
 	/**
 	 * tests that the <code>AuditService</code> gets called when creating a <code>PlateFunction</code>
 	 */
 	@Test
 	public void testCreatePlateFunction(){
 		
 		AbstractDAOBasedPlateManager pm = new PlateManager();
 				
 		pm.setMasterPlateDAO((IMasterPlateDAO)context.getBean("masterPlateDAO"));
 		pm.setPlateDAO((IPlateDAO)context.getBean("plateDAO"));
 		pm.setAuditService(auditService);
 		
 		pm.createPlateFunction(tester, "test", masterPlate, "");
 		
 		performAssert(1);
 	}
 	
 	/**
 	 * tests that the <code>AuditService</code> gets called when creating a <code>PlateFunction</code>
 	 */
 	@Test
 	public void testCreatePlateFunctionWithValues(){
 				
 		AbstractDAOBasedPlateManager pm = new PlateManager();
 		
 		pm.setPlateDAO((IPlateDAO)context.getBean("plateDAO"));
 		pm.setMasterPlateDAO((IMasterPlateDAO)context.getBean("masterPlateDAO"));
 		pm.setAuditService(auditService);
 		
 		pm.createPlateFunction(tester, "test", masterPlate, "", 2, 3);
 		
 		performAssert(1);
 	}
 	
 /*
  * TEST THE PLATELAYOUT MANAGER'S METHODS
  */	
 	/**
 	 * tests that the <code>AuditService</code> gets called when creating a <code>PlateType</code>
 	 */
 	@Test
 	public void testCreatePlateType(){
 
 		plm.setAuditService(auditService);
 		
 		plm.createPlateType(tester, 6, 6, "test", plateTypes);
 		performAssert(2);
 	}
 	
 	/**
 	 * tests that the <code>AuditService</code> gets called when creating a <code>PlateLayout</code>
 	 */
 	@Test
 	public void testCreatePlateLayout(){
 
 		plm.setAuditService(auditService);
 				
 		plm.createPlateLayout(tester, "my plateLayout", plateType, plateTypes);
 		performAssert(1);
 	}
 	
 	/**
 	 * tests that the <code>AuditService</code> gets called when creating a <code>WellFunction</code>
 	 */
 	@Test
 	public void testCreateLayoutWellFunction(){
 		
 		plm.setAuditService(auditService);
 		
 		plm.createWellFunction(tester, "test", layoutWell, "");
 		performAssert(1);
 	}
 	
 	/**
 	 * tests that the <code>AuditService</code> gets called when creating a <code>PlateFunction</code>
 	 */
 	@Test
 	public void testCreatePlateLayoutFunction(){
 			
 		PlateLayout plateLayout = plm.getPlateLayout(plm.createPlateLayout(tester, "my plateLayout", plateType, plateLayouts));
 		
 		AbstractDAOBasedPlateLayoutManager plm = new PlateLayoutManager();
 		IPlateLayoutDAO plateLayoutDAO = new MockPlateLayoutDAO();
 		
 		plm.setPlateLayoutDAO(plateLayoutDAO);
 		plm.setAuditService(auditService);
 		
 		plm.createPlateFunction(tester, "test", plateLayout, "");
 		performAssert(1);
 	}
 
 	/**
 	 * tests that the <code>AuditService</code> gets called when creating a <code>PlateFunction</code>
 	 */
 	@Test
 	public void testCreatePlateLayoutFunctionWithValues(){
 			
 		PlateLayout plateLayout = plm.getPlateLayout(plm.createPlateLayout(tester, "my plateLayout", plateType, plateLayouts));
 		
 		AbstractDAOBasedPlateLayoutManager plm = new PlateLayoutManager();
 		IPlateLayoutDAO plateLayoutDAO = new MockPlateLayoutDAO();
 		
 		plm.setPlateLayoutDAO(plateLayoutDAO);
 		plm.setAuditService(auditService);
 		
 		plm.createPlateFunction(tester, "test", plateLayout, "", 3, 4);
 		performAssert(1);
 	}
 
 /*
  * TEST THE SAMPLE MANAGER'S METHODS
  */
 //	/**
 //	 * tests that the <code>AuditService</code> gets called when adding a new <code>CellSample</code>
 //	 * to a <code>SampleContainer</code>
 //	 */
 //	public void testAddNewCellSampleToContainer(){
 //		AbstractDAOBasedSampleManager sm = new SampleManager();
 //		sm.setAuditService(auditService);
 //		sm.setSampleContainerDAO((ISampleContainerDAO)context.getBean("sampleContainerDAO"));
 //		
 //		sm.addNewCellSampleToContainer(tester, "name", cellOrigin, new Timestamp(23), sampleContainer);
 //		performAssert(1);
 //		
 //	}
 //	
 //	/**
 //	 * tests that the <code>AuditService</code> gets called when adding a new <code>DrugSample</code>
 //	 * to a <code>SampleContainer</code>
 //	 */
 //	public void testAddNewDrugSampleToContainer(){
 //		AbstractDAOBasedSampleManager sm = new SampleManager();
 //		sm.setAuditService(auditService);
 //		sm.setSampleContainerDAO((ISampleContainerDAO)context.getBean("sampleContainerDAO"));
 //		
 //		sm.addNewDrugSampleToContainer(tester, "namE", drugOrigin, 2, sampleContainer, null);
 //		performAssert(1);
 //		
 //	}
 			
 	
 /*
  * MOCKIMPLEMENTATIONS
  */
 	
 	/**
 	 * A mockimplementation of the PlateLayoutDAO for testing purposes
 	 * 
 	 * @author jonathan
 	 *
 	 */
 	private class MockPlateLayoutDAO implements IPlateLayoutDAO{
 
 		public void delete(long id) {   
         }
 
 		public List getAll() {
 	        return null;
         }
 
 		public PlateLayout getById(long id) {
 	        return null;
         }
 
 		public void save(PlateLayout plateLayout) {
         }
 
 		public List<PlateLayout> findAll() {
 	        return null;
         }
 
 		public SessionFactory getSessionFactory() {
 	        return null;
         }
 
 		public void update(PlateLayout instance) {
 
         }
 
 		public PlateLayout merge(PlateLayout instance) {
 	        // TODO Auto-generated method stub
 	        return null;
         }
 
 		public List<PlateLayout> findAllNotDeleted() {
 	        // TODO Auto-generated method stub
 	        return null;
         }
 
 		@Override
         public void delete(ILISObject obj) {
 	        // TODO Auto-generated method stub
 	        
         }
 
 		@Override
         public void evict(PlateLayout o) {
 	        // TODO Auto-generated method stub
 	        
         }
 
 		@Override
         public <T2> T2 mergeObject(T2 o) {
 	        // TODO Auto-generated method stub
 	        return null;
         }
 
 
 	}
 	
 	/**
 	 * A mockimplementation of the PlateDAO for testing purposes
 	 * 
 	 * @author jonathan
 	 *
 	 */
 	private class MockPlateDAO implements IPlateDAO{
 
 		public void delete(long id) {
         }
 
 		public List getAll() {
 	        return null;
         }
 
 		public Plate getById(long id) {
 	        return null;
         }
 
 		public void save(Plate plate) {   
         }
 
 		public SessionFactory getSessionFactory() {
 	        return null;
         }
 
 		public List<Plate> findAll() {
 	        return null;
         }
 
 		public List<Plate> findPlatesByExperiment(long experimentId) {
 	        return null;
         }
 
 		public void update(Plate instance) {
 
         }
 
 		public Plate merge(Plate instance) {
			return instance;
 	        
         }
 
 		public List<String> findAllPlateBarcodes() {
 	        // TODO Auto-generated method stub
 	        return null;
         }
 
 		public List<Plate> findByBarcode(String barcode) {
 	        // TODO Auto-generated method stub
 	        return null;
         }
 
 		public List findAllNotDeleted() {
 	        // TODO Auto-generated method stub
 	        return null;
         }
 
 		@Override
         public void delete(ILISObject obj) {
 	        // TODO Auto-generated method stub
 	        
         }
 
 		@Override
         public Object mergeObject(Object o) {
	        return o;
         }
 
 		@Override
         public void evict(Plate o) {
 	        // TODO Auto-generated method stub
 	        
         }
 
 	}
 	
 	
 	/**
 	 * A mockimplementation of the AuditService that only counts how many times  it's been called
 	 * @author  jonathan
 	 */
 	private class MockAuditService implements IAuditService{
 
 		private int callCount;
 		
 		/**
 		 * @return  the callCount
 		 */
 		public int getCallCount(){
 			return callCount;
 		}
 
 		public void audit(User user, AuditType auditType, AbstractAuditableObject auditedObject) {
 			
 			callCount++;
         }
 	}
 	
 	/**
 	 * A mockimplementation of the MasterPlateDAO for testing purposes
 	 * 
 	 * @author jonathan
 	 *
 	 */
 	private class MockMasterPlateDAO implements IMasterPlateDAO {
 
 		public List<MasterPlate> findAll() {
 	        return null;
         }
 
 		public void delete(long id) {
         }
 
 		public MasterPlate getById(long id) {
 	        return null;
         }
 
 		public SessionFactory getSessionFactory() {
 	        return null;
         }
 
 		public void save(MasterPlate instance) {
         }
 
 		public void update(MasterPlate instance) {
 
 		}
 
 		public MasterPlate merge(MasterPlate instance) {
 			return instance;
 	        // TODO Auto-generated method stub
 	        
         }
 
 		public List<MasterPlate> findAllNotDeleted() {
 	        // TODO Auto-generated method stub
 	        return null;
         }
 
 		@Override
         public void delete(ILISObject obj) {
 	        // TODO Auto-generated method stub
 	        
         }
 
 		@Override
         public void evict(MasterPlate o) {
 	        // TODO Auto-generated method stub
 	        
         }
 
 		@Override
         public <T2> T2 mergeObject(T2 o) {
 	        // TODO Auto-generated method stub
 	        return null;
         }
 	}
 	
 	private class MockPlateTypeDAO implements IPlateTypeDAO {
 
 		public List<PlateType> findAll() {
 	        return null;
         }
 
 		public void delete(long id) {
         }
 
 		public PlateType getById(long id) {
 	        return null;
         }
 
 		public SessionFactory getSessionFactory() {
 	        return null;
         }
 
 		public void save(PlateType instance) {
         }
 
 		public void update(PlateType instance) {
 	        
         }
 
 		public PlateType merge(PlateType instance) {
 			return instance;
 	        // TODO Auto-generated method stub
 	        
         }
 
 		public List findAllNotDeleted() {
 	        // TODO Auto-generated method stub
 	        return null;
         }
 
 		@Override
         public void delete(ILISObject obj) {
 	        // TODO Auto-generated method stub
 	        
         }
 
 		@Override
         public Object mergeObject(Object o) {
 	        // TODO Auto-generated method stub
 	        return null;
         }
 
 		@Override
         public void evict(PlateType o) {
 	        // TODO Auto-generated method stub
 	        
         }
 
 	}
 	
 	private class MockSampleContainerDAO implements ISampleContainerDAO {
 
 		public List<SampleContainer> findAll() {
 	        return null;
         }
 
 		public void delete(long id) {
         }
 
 		public SampleContainer getById(long id) {
 	        return null;
         }
 
 		public SessionFactory getSessionFactory() {
 	        return null;
         }
 
 		public void save(SampleContainer instance) {
         }
 
 		public void update(SampleContainer instance) {
 	        
         }
 
 		public SampleContainer merge(SampleContainer instance) {
 			return instance;
 	        // TODO Auto-generated method stub
 	        
         }
 
 		@Override
         public void delete(ILISObject obj) {
 	        // TODO Auto-generated method stub
 	        
         }
 
 		@Override
         public Object mergeObject(Object o) {
 	        // TODO Auto-generated method stub
 	        return null;
         }
 
 		@Override
         public void evict(SampleContainer o) {
 	        // TODO Auto-generated method stub
 	        
         }
 	}
 	
 	private class MockDrugOriginDAO implements IDrugOriginDAO {
 
 		public List<DrugOrigin> findAll() {
 	        return null;
         }
 
 		public void delete(long id) {
         }
 
 		public DrugOrigin getById(long id) {
 	        return null;
         }
 
 		public SessionFactory getSessionFactory() {
 			return null;
         }
 
 		public void save(DrugOrigin instance) {
         }
 
 		public void update(DrugOrigin instance) {
 	        
         }
 
 		public DrugOrigin merge(DrugOrigin instance) {
 			return instance;
 	        // TODO Auto-generated method stub
 	        
         }
 
 		public List<DrugOrigin> findAllNotDeleted() {
 	        // TODO Auto-generated method stub
 	        return null;
         }
 
 		@Override
         public void delete(ILISObject obj) {
 	        // TODO Auto-generated method stub
 	        
         }
 
 		@Override
         public void evict(DrugOrigin o) {
 	        // TODO Auto-generated method stub
 	        
         }
 
 		@Override
         public <T2> T2 mergeObject(T2 o) {
 	        // TODO Auto-generated method stub
 	        return null;
         }
 	}
 	
 	private class MockCellOriginDAO implements ICellOriginDAO {
 
 		public List<CellOrigin> findAll() {
 	        return null;
         }
 
 		public void delete(long id) {
         }
 
 		public CellOrigin getById(long id) {
 	        return null;
         }
 
 		public SessionFactory getSessionFactory() {
 	        return null;
         }
 
 		public void save(CellOrigin instance) {
         }
 
 		public void update(CellOrigin instance) {
 	        
         }
 
 		public CellOrigin merge(CellOrigin instance) {
 			return instance;
 	        // TODO Auto-generated method stub
 	        
         }
 
 		public List<CellOrigin> findAllNotDeleted() {
 	        // TODO Auto-generated method stub
 	        return null;
         }
 
 		@Override
         public void delete(ILISObject obj) {
 	        // TODO Auto-generated method stub
 	        
         }
 
 		@Override
         public Object mergeObject(Object o) {
 	        // TODO Auto-generated method stub
 	        return null;
         }
 
 		@Override
         public void evict(CellOrigin o) {
 	        // TODO Auto-generated method stub
 	        
         }
 
 	}
 		
 	private class MockInstrumentDAO implements IInstrumentDAO {
 
 		public List<Instrument> findAll() {
 	        return null;
         }
 
 		public void delete(long id) {
         }
 
 		public Instrument getById(long id) {
 	        return null;
         }
 
 		public SessionFactory getSessionFactory() {
 	        return null;
         }
 
 		public void save(Instrument instance) {
         }
 
 		public void update(Instrument instance) {
 	        
         }
 
 		public Instrument merge(Instrument instance) {
 			return instance;
 	        // TODO Auto-generated method stub
 	        
         }
 
 		public List<Instrument> findByName(String name) {
 	        // TODO Auto-generated method stub
 	        return null;
         }
 
 		@Override
         public void delete(ILISObject obj) {
 	        // TODO Auto-generated method stub
 	        
         }
 
 		@Override
         public Object mergeObject(Object o) {
 	        // TODO Auto-generated method stub
 	        return null;
         }
 
 		@Override
         public void evict(Instrument o) {
 	        // TODO Auto-generated method stub
 	        
         }
 
 	}
 	
 	private class MockMeasurementDAO implements IMeasurementDAO {
 
 		public List<Measurement> findAll() {
 	        return null;
         }
 
 		public void delete(long id) {
         }
 
 		public Measurement getById(long id) {
 	        return null;
         }
 
 		public SessionFactory getSessionFactory() {
 	        return null;
         }
 
 		public void save(Measurement instance) {
         }
 
 		public void update(Measurement instance) {
 	        
         }
 
 		public Measurement merge(Measurement instance) {
 			return instance;
 	        // TODO Auto-generated method stub
 	        
         }
 
 		@Override
         public void delete(ILISObject obj) {
 	        // TODO Auto-generated method stub
 	        
         }
 
 		@Override
         public void evict(Measurement o) {
 	        // TODO Auto-generated method stub
 	        
         }
 
 		@Override
         public <T2> T2 mergeObject(T2 o) {
 	        // TODO Auto-generated method stub
 	        return null;
         }
 	}
 	
 	private class MockResultTypeDAO implements IResultTypeDAO {
 
 		public List<ResultType> findAll() {
 	        return null;
         }
 
 		public void delete(long id) {
         }
 
 		public ResultType getById(long id) {
 	        return null;
         }
 
 		public SessionFactory getSessionFactory() {
 	        return null;
         }
 
 		public void save(ResultType instance) {
         }
 
 		public void update(ResultType instance) {
 	        
         }
 
 		public ResultType merge(ResultType instance) {
 			return instance;
 	        // TODO Auto-generated method stub
 	        
         }
 
 		public List<ResultType> findByName(String name) {
 	        // TODO Auto-generated method stub
 	        return null;
         }
 
 		@Override
         public void delete(ILISObject obj) {
 	        // TODO Auto-generated method stub
 	        
         }
 
 		@Override
         public Object mergeObject(Object o) {
 	        // TODO Auto-generated method stub
 	        return null;
         }
 
 		@Override
         public void evict(ResultType o) {
 	        // TODO Auto-generated method stub
 	        
         }
 
 	}
 	
 	private class MockUserDAO implements IUserDAO {
 
 		public List<User> findAll() {
 	        return null;
         }
 
 		public List<User> findByName(String name) {
 	        return null;
         }
 
 		public void delete(long id) {
         }
 
 		public User getById(long id) {
 	        return null;
         }
 
 		public SessionFactory getSessionFactory() {
 	        return null;
         }
 
 		public void save(User instance) {
         }
 
 		public void update(User instance) {
 	        
         }
 
 		public User merge(User instance) {
 			return instance;
 	        // TODO Auto-generated method stub
 	        
         }
 
 		@Override
         public void delete(ILISObject obj) {
 	        // TODO Auto-generated method stub
 	        
         }
 
 		@Override
         public void evict(User o) {
 	        // TODO Auto-generated method stub
 	        
         }
 
 		@Override
         public <T2> T2 mergeObject(T2 o) {
 	        // TODO Auto-generated method stub
 	        return null;
         }
 	}
 	
 	private class MockAuditLogDAO implements IAuditLogDAO {
 
 		public List<AuditLog> findAll() {
 	        return null;
         }
 
 		public void delete(long id) {
         }
 
 		public AuditLog getById(long id) {
 	        return null;
         }
 
 		public SessionFactory getSessionFactory() {
 	        return null;
         }
 
 		public void save(AuditLog instance) {
         }
 
 		public void update(AuditLog instance) {
 	        
         }
 
 		public AuditLog merge(AuditLog instance) {
 			return instance;
 	        // TODO Auto-generated method stub
 	        
         }
 
 		@Override
         public void delete(ILISObject obj) {
 	        // TODO Auto-generated method stub
 	        
         }
 
 		@Override
         public void evict(AuditLog o) {
 	        // TODO Auto-generated method stub
 	        
         }
 
 		@Override
         public <T2> T2 mergeObject(T2 o) {
 	        // TODO Auto-generated method stub
 	        return null;
         }
 	}
 	
 	private class MockAnnotationDAO implements IAnnotationDAO {
 
 		public List<Annotation> findAll() {
 	        return null;
         }
 
 		public void delete(long id) {
         }
 
 		public Annotation getById(long id) {
 	        return null;
         }
 
 		public SessionFactory getSessionFactory() {
 	        return null;
         }
 
 		public void save(Annotation instance) {
         }
 
 		public void update(Annotation instance) {
 	        
         }
 
 		public Annotation merge(Annotation instance) {
 			return instance;
 	        // TODO Auto-generated method stub
 	        
         }
 
 		@Override
         public void delete(ILISObject obj) {
 	        // TODO Auto-generated method stub
 	        
         }
 
 		@Override
         public Object mergeObject(Object o) {
 	        // TODO Auto-generated method stub
 	        return null;
         }
 
 		@Override
         public void evict(Annotation o) {
 	        // TODO Auto-generated method stub
 	        
         }
 
 	}
 	
 	private class MockWellDAO implements IWellDAO {
 
 		public List<Well> findAll() {
 	        return null;
         }
 
 		public void delete(long id) {
         }
 
 		public Well getById(long id) {
 	        return null;
         }
 
 		public SessionFactory getSessionFactory() {
 	        return null;
         }
 
 		public Well merge(Well instance) {
 	        return null;
         }
 
 		public void save(Well instance) {
         }
 
 		public void update(Well instance) {
         }
 
 		@Override
         public void delete(ILISObject obj) {
 	        // TODO Auto-generated method stub
 	        
         }
 
 		@Override
         public Object mergeObject(Object o) {
 	        // TODO Auto-generated method stub
 	        return null;
         }
 
 		@Override
         public void evict(Well o) {
 	        // TODO Auto-generated method stub
 	        
         }
 
 	}
 	
 	private class MockAnnotationInstanceDAO implements IAnnotationInstanceDAO {
 
 		public List<AbstractAnnotationInstance> findAll() {
 	        return null;
         }
 
 		public void delete(long id) {
         }
 
 		public AbstractAnnotationInstance getById(long id) {
 	        return null;
         }
 
 		public SessionFactory getSessionFactory() {
 	        return null;
         }
 
 		public void save(AbstractAnnotationInstance instance) {
         }
 
 		public void update(AbstractAnnotationInstance instance) {
 	        
         }
 
 		public AbstractAnnotationInstance merge(AbstractAnnotationInstance instance) {
 			return instance;
 	        // TODO Auto-generated method stub
 	        
         }
 
 		@Override
         public void delete(ILISObject obj) {
 	        // TODO Auto-generated method stub
 	        
         }
 
 		@Override
         public Object mergeObject(Object o) {
 	        // TODO Auto-generated method stub
 	        return null;
         }
 
 		@Override
         public void evict(AbstractAnnotationInstance o) {
 	        // TODO Auto-generated method stub
 	        
         }
 
 	}
 }
