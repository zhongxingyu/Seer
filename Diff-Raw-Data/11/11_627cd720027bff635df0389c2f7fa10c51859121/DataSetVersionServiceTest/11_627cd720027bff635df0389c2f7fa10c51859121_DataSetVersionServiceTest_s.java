 package pals.service;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.NoResultException;
 
 import org.apache.commons.io.FileUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 import pals.BaseTest;
 import pals.Configuration;
 import pals.analysis.AnalysisException;
 import pals.analysis.NetcdfUtil;
 import pals.dao.DAO;
 import pals.entity.Analysis;
 import pals.entity.AnalysisType;
 import pals.entity.Country;
 import pals.entity.DataSet;
 import pals.entity.DataSetVersion;
 import pals.entity.Experiment;
 import pals.entity.TestEntityFactory;
 import pals.entity.User;
 import pals.entity.VegetationType;
 import pals.utils.PalsFileUtils;
 import ucar.nc2.NetcdfFile;
 
 public class DataSetVersionServiceTest extends BaseTest
 {
 	DataSetVersion entity;
 	
     @Autowired
     DAO dao;
     
     @Autowired
     DataSetVersionService service;
     
     @Autowired
     ExperimentService experimentService;
     
     @Autowired
     AnalysisServiceInterface analysisService;
     
     @Autowired UserServiceInterface userService;
     
     @Autowired DataSetService dataSetService;
     
 	NetcdfFile ncFile;
 	List<AnalysisType> analysisList;
 	List<Object> analysisRuns;
 	String path;
 	String executablePath;
 	File testFile;
 	String filename ="testdata/flux1.nc";
 	Analysis analysisO;
 	AnalysisType analysisType;
 	
     public void setUp() throws IOException
     {    	
     	entity = TestEntityFactory.dataSetVersion();
     	dao.persist(entity.getDataSet().getCountry());
     	dao.persist(entity.getDataSet().getVegType());
     	dao.persist(entity.getDataSet().getOwner());
     	dao.persist(entity.getDataSet());
     	entity.setDataSetId(entity.getDataSet().getId());
     	entity.setOwner(entity.getDataSet().getOwner());
     	dao.persist(entity);
     }
     
 	public void tearDown() throws IOException
 	{
 		dao.deleteAll("Analysis");
 		dao.deleteAll("DataSetVersion");
 		dao.deleteAll("DataSet");
 		dao.deleteAll("Country");
 		dao.deleteAll("VegetationType");
 		dao.deleteAll("User");
 		dao.deleteAll("AnalysisType");
 	}
 	
 	@Test
 	public void testDelete() throws IOException
 	{
 	    setUp();
 	    
 	    // set a new path to app data in case we interact with the existing data
 	    Configuration.getInstance().PATH_TO_APP_DATA = "testdata/dsvServiceTest";
 	    
 	    // create dummy files to make sure they are deleted
 	    File metFile = new File(entity.retrieveMetFilePath());
 	    System.out.println(metFile.getAbsolutePath());
 	    metFile.createNewFile();
 	    File fluxFile = new File(entity.retrieveOutputFilePath());
 	    fluxFile.createNewFile();
 	    File uploadedFile = new File(entity.uploadedFilePath());
 	    uploadedFile.createNewFile();
 	    File qcFile = new File(entity.retrieveQCPlotsFilePath());
 	    qcFile.createNewFile();
 	    
 	    // create analysis to be deleted
 		String cwd = System.getProperty("user.dir");
 		executablePath = "R -f " + cwd + "\\" + "WebContent" +
 		"\\" + "r" + "\\" + "ObsAnnualCycleNEE.R";
 		File testFileSrc = new File(filename);
 		path = entity.retrieveOutputFilePath();
 		testFile = new File(path);
 		FileUtils.copyFile(testFileSrc, testFile);
 		analysisType = TestEntityFactory.analysisType();
 		analysisType.setExecutablePath(executablePath);
 		analysisType.setId(1);
 		analysisType.setType(AnalysisType.DATA_SET_VERSION_ANALYSIS_TYPE);
 		analysisType.setVariableName("NEE");
 		analysisO = TestEntityFactory.analysis();
 		analysisO.setAnalysable(entity);
 		analysisO.setAnalysisType(analysisType);
 		analysisO.setId(66); 
 		ncFile = NetcdfUtil.parse(path);
 		analysisList = TestEntityFactory.analysisList();
 		for( AnalysisType analysis : analysisList )
 		{
 			analysis.setExecutablePath(executablePath);
 			analysis.setType(AnalysisType.DATA_SET_VERSION_ANALYSIS_TYPE);
 			analysis.setVariableName("NEE");
 			dao.persist(analysis);
 		}
     	analysisService.pollPreparedAnalysable();
     	analysisRuns = dao.getAll("Analysis");
 	    
 	    Integer id = entity.getId();
 	    service.delete(id);
 	    try
 	    {
 	        dao.get(DataSetVersion.class.getName(), "id", id);
 	        Assert.fail();
 	    }
 	    catch( NoResultException e )
 	    {
 	    	// what we want
 	    	Assert.assertFalse(metFile.exists());
 	    	Assert.assertFalse(fluxFile.exists());
 	    	Assert.assertFalse(uploadedFile.exists());
 	    	Assert.assertFalse(qcFile.exists());
 	    }
 	    
     	@SuppressWarnings("unchecked")
 		List<Analysis> results = dao.getAll(Analysis.class.getName());
     	Assert.assertEquals(results.size(),0);
 	    
 	    tearDown();
 	}
 	
 	@Test
 	public void testGetPublicDataSetVersions()
 	{
     	entity = TestEntityFactory.dataSetVersion();
     	dao.persist(entity.getDataSet().getCountry());
     	dao.persist(entity.getDataSet().getVegType());
     	dao.persist(entity.getDataSet().getOwner());
     	dao.persist(entity.getDataSet());
     	Experiment experiment = TestEntityFactory.experiment();
     	experiment.setOwner(entity.getDataSet().getOwner());
     	dao.persist(experiment);
     	entity.getDataSet().setExperiment(experiment);
     	dao.update(entity.getDataSet());
     	entity.setDataSetId(entity.getDataSet().getId());
     	entity.setOwner(entity.getDataSet().getOwner());
     	dao.persist(entity);
     	entity.getDataSet().setLatestVersion(entity);
     	dao.update(entity.getDataSet());
     	
     	List<DataSetVersion> dataSetVersions = service.getPublicDataSetVersions(experiment);
     	Assert.assertEquals(dataSetVersions.size(), 1);
     	Assert.assertEquals(dataSetVersions.get(0).getId(),entity.getId());
     		
     	entity.getDataSet().setLatestVersion(null);
     	dao.update(entity.getDataSet());
 		dao.deleteAll("DataSetVersion");
 		experimentService.removeCurrentExperimentFromExperimentables(experiment);
 		experimentService.remove(experiment);
 		dao.deleteAll("DataSet");
 		dao.deleteAll("Country");
 		dao.deleteAll("VegetationType");
 		dao.deleteAll("User");
 	}
 	
 	@Test
 	public void testGetPublicDataSetVersionsNoLatestVersion()
 	{
     	entity = TestEntityFactory.dataSetVersion();
     	dao.persist(entity.getDataSet().getCountry());
     	dao.persist(entity.getDataSet().getVegType());
     	dao.persist(entity.getDataSet().getOwner());
     	dao.persist(entity.getDataSet());
     	Experiment experiment = TestEntityFactory.experiment();
     	experiment.setOwner(entity.getDataSet().getOwner());
     	dao.persist(experiment);
     	entity.getDataSet().setExperiment(experiment);
     	dao.update(entity.getDataSet());
     	entity.setDataSetId(entity.getDataSet().getId());
     	entity.setOwner(entity.getDataSet().getOwner());
     	dao.persist(entity);
     	
     	List<DataSetVersion> dataSetVersions = service.getPublicDataSetVersions(experiment);
     	Assert.assertEquals(dataSetVersions.size(), 0);
     		
 		dao.deleteAll("DataSetVersion");
 		experimentService.removeCurrentExperimentFromExperimentables(experiment);
 		experimentService.remove(experiment);
 		dao.deleteAll("DataSet");
 		dao.deleteAll("Country");
 		dao.deleteAll("VegetationType");
 		dao.deleteAll("User");
 	}
 	
 	@Test
 	public void testGetPublicDataSetVersionsWrongExperiment()
 	{
     	entity = TestEntityFactory.dataSetVersion();
     	dao.persist(entity.getDataSet().getCountry());
     	dao.persist(entity.getDataSet().getVegType());
     	dao.persist(entity.getDataSet().getOwner());
     	dao.persist(entity.getDataSet());
     	Experiment experiment = TestEntityFactory.experiment();
     	experiment.setOwner(entity.getDataSet().getOwner());
     	dao.persist(experiment);
     	entity.getDataSet().setExperiment(experiment);
     	dao.update(entity.getDataSet());
     	entity.setDataSetId(entity.getDataSet().getId());
     	entity.setOwner(entity.getDataSet().getOwner());
     	dao.persist(entity);
     	
     	Experiment dummyExperiment = new Experiment();
     	dummyExperiment.setId(-100);
     	List<DataSetVersion> dataSetVersions = service.getPublicDataSetVersions(dummyExperiment);
     	Assert.assertEquals(dataSetVersions.size(), 0);
     		
 		dao.deleteAll("DataSetVersion");
 		experimentService.removeCurrentExperimentFromExperimentables(experiment);
 		experimentService.remove(experiment);
 		dao.deleteAll("DataSet");
 		dao.deleteAll("Country");
 		dao.deleteAll("VegetationType");
 		dao.deleteAll("User");
 	}
 	
 	@Test
 	public void testGetPublicDataSetVersionsNullExperiment()
 	{
     	entity = TestEntityFactory.dataSetVersion();
     	dao.persist(entity.getDataSet().getCountry());
     	dao.persist(entity.getDataSet().getVegType());
     	dao.persist(entity.getDataSet().getOwner());
     	dao.persist(entity.getDataSet());
     	Experiment experiment = TestEntityFactory.experiment();
     	experiment.setOwner(entity.getDataSet().getOwner());
     	dao.persist(experiment);
     	entity.getDataSet().setExperiment(experiment);
     	dao.update(entity.getDataSet());
     	entity.setDataSetId(entity.getDataSet().getId());
     	entity.setOwner(entity.getDataSet().getOwner());
     	dao.persist(entity);
     	entity.getDataSet().setLatestVersion(entity);
     	dao.update(entity.getDataSet());
     	
     	List<DataSetVersion> dataSetVersions = service.getPublicDataSetVersions(null);
     	Assert.assertEquals(dataSetVersions.size(), 0);
     		
     	entity.getDataSet().setLatestVersion(null);
     	dao.update(entity.getDataSet());
 		dao.deleteAll("DataSetVersion");
 		experimentService.removeCurrentExperimentFromExperimentables(experiment);
 		experimentService.remove(experiment);
 		dao.deleteAll("DataSet");
 		dao.deleteAll("Country");
 		dao.deleteAll("VegetationType");
 		dao.deleteAll("User");
 	}
 	
 	@Test
 	public void testGetPublicDataSetVersionsNullExperimentShouldLoad()
 	{
     	entity = TestEntityFactory.dataSetVersion();
     	dao.persist(entity.getDataSet().getCountry());
     	dao.persist(entity.getDataSet().getVegType());
     	dao.persist(entity.getDataSet().getOwner());
     	dao.persist(entity.getDataSet());
     	Experiment experiment = TestEntityFactory.experiment();
     	experiment.setOwner(entity.getDataSet().getOwner());
     	dao.persist(experiment);
     	//entity.getDataSet().setExperiment(experiment);
     	//dao.update(entity.getDataSet());
     	entity.setDataSetId(entity.getDataSet().getId());
     	entity.setOwner(entity.getDataSet().getOwner());
     	dao.persist(entity);
     	entity.getDataSet().setLatestVersion(entity);
     	dao.update(entity.getDataSet());
     	
     	List<DataSetVersion> dataSetVersions = service.getPublicDataSetVersions(null);
     	Assert.assertEquals(dataSetVersions.size(), 1);
     	Assert.assertEquals(dataSetVersions.get(0).getId(),entity.getId());
     		
     	entity.getDataSet().setLatestVersion(null);
     	dao.update(entity.getDataSet());
 		dao.deleteAll("DataSetVersion");
 		experimentService.removeCurrentExperimentFromExperimentables(experiment);
 		experimentService.remove(experiment);
 		dao.deleteAll("DataSet");
 		dao.deleteAll("Country");
 		dao.deleteAll("VegetationType");
 		dao.deleteAll("User");
 	}
 	
 	@Test
 	public void testGetPublicDataSetVersionsSharedExperiment()
 	{
     	entity = TestEntityFactory.dataSetVersion();
     	dao.persist(entity.getDataSet().getCountry());
     	dao.persist(entity.getDataSet().getVegType());
     	dao.persist(entity.getDataSet().getOwner());
     	dao.persist(entity.getDataSet());
     	Experiment experiment = TestEntityFactory.experiment();
     	experiment.setOwner(entity.getDataSet().getOwner());
     	experiment.setShareWithAll(true);
     	dao.persist(experiment);
     	entity.getDataSet().setExperiment(experiment);
     	dao.update(entity.getDataSet());
     	entity.setDataSetId(entity.getDataSet().getId());
     	entity.setOwner(entity.getDataSet().getOwner());
     	dao.persist(entity);
     	entity.getDataSet().setLatestVersion(entity);
     	dao.update(entity.getDataSet());
     	
     	List<DataSetVersion> dataSetVersions = service.getPublicDataSetVersions(null);
     	Assert.assertEquals(dataSetVersions.size(), 1);
     	Assert.assertEquals(dataSetVersions.get(0).getId(),entity.getId());
     		
     	entity.getDataSet().setLatestVersion(null);
     	dao.update(entity.getDataSet());
 		dao.deleteAll("DataSetVersion");
 		experimentService.removeCurrentExperimentFromExperimentables(experiment);
 		experimentService.remove(experiment);
 		dao.deleteAll("DataSet");
 		dao.deleteAll("Country");
 		dao.deleteAll("VegetationType");
 		dao.deleteAll("User");
 	}
 	
 	@Test
 	public void testGetPublicDataSetVersionsPagination()
 	{
 		List<DataSetVersion> dataSetVersions = new ArrayList<DataSetVersion>();
 		User user = null;
 		Experiment experiment = null;
 		Country country = null;
 		VegetationType vegType = null;
 		
 		for( int i=0; i<10; ++i)
 		{
 	    	entity = TestEntityFactory.dataSetVersion();
 	    	if( i ==0 )
 	    	{
 	    	    dao.persist(entity.getDataSet().getCountry());
 	    	    country = entity.getDataSet().getCountry();
 	    	    dao.persist(entity.getDataSet().getVegType());
 	    	    vegType = entity.getDataSet().getVegType();
 	    	}
 	    	else
 	    	{
 	    		entity.getDataSet().setCountry(country);
 	    		entity.getDataSet().setVegType(vegType);
 	    	}
 	    	if( i == 0 )
 	    	{
 	    	    dao.persist(entity.getDataSet().getOwner());
 	    	    user = entity.getDataSet().getOwner();
 	    	}
 	    	else
 	    	{
 	    		entity.getDataSet().setOwner(user);
 	    	}
 	    	dao.persist(entity.getDataSet());
 	    	if( i == 0 )
 	    	{
 	    	    experiment = TestEntityFactory.experiment();
 	    	    experiment.setOwner(entity.getDataSet().getOwner());
 	    	    dao.persist(experiment);
 	    	}
 	    	entity.getDataSet().setExperiment(experiment);
 	    	dao.update(entity.getDataSet());
 	    	entity.setDataSetId(entity.getDataSet().getId());
 	    	entity.setOwner(entity.getDataSet().getOwner());
 	    	entity.setIsPublic(true);
 	    	dao.persist(entity);
 	    	entity.getDataSet().setLatestVersion(entity);
 	    	dao.update(entity.getDataSet());
 	    	dataSetVersions.add(entity);
 		}
     	
     	List<DataSetVersion> results = service.getPublicDataSetVersions(experiment,2,5,null,true);
     	Assert.assertEquals(results.size(), 2);
     		
     	for( DataSetVersion dsv : dataSetVersions )
     	{
     	    dsv.getDataSet().setLatestVersion(null);
     	    dao.update(dsv.getDataSet());
     	}
 		dao.deleteAll("DataSetVersion");
 		experimentService.removeCurrentExperimentFromExperimentables(experiment);
 		experimentService.remove(experiment);
 		dao.deleteAll("DataSet");
 		dao.deleteAll("Country");
 		dao.deleteAll("VegetationType");
 		dao.deleteAll("User");
 	}
 	
 	@Test
 	public void testGetPublicDataSetVersionsCount()
 	{
 		List<DataSetVersion> dataSetVersions = new ArrayList<DataSetVersion>();
 		User user = null;
 		Experiment experiment = null;
 		Country country = null;
 		VegetationType vegType = null;
 		
 		for( int i=0; i<10; ++i)
 		{
 	    	entity = TestEntityFactory.dataSetVersion();
 	    	if( i ==0 )
 	    	{
 	    	    dao.persist(entity.getDataSet().getCountry());
 	    	    country = entity.getDataSet().getCountry();
 	    	    dao.persist(entity.getDataSet().getVegType());
 	    	    vegType = entity.getDataSet().getVegType();
 	    	}
 	    	else
 	    	{
 	    		entity.getDataSet().setCountry(country);
 	    		entity.getDataSet().setVegType(vegType);
 	    	}
 	    	if( i == 0 )
 	    	{
 	    	    dao.persist(entity.getDataSet().getOwner());
 	    	    user = entity.getDataSet().getOwner();
 	    	}
 	    	else
 	    	{
 	    		entity.getDataSet().setOwner(user);
 	    	}
 	    	dao.persist(entity.getDataSet());
 	    	if( i == 0 )
 	    	{
 	    	    experiment = TestEntityFactory.experiment();
 	    	    experiment.setOwner(entity.getDataSet().getOwner());
 	    	    dao.persist(experiment);
 	    	}
 	    	entity.getDataSet().setExperiment(experiment);
 	    	dao.update(entity.getDataSet());
 	    	entity.setDataSetId(entity.getDataSet().getId());
 	    	entity.setOwner(entity.getDataSet().getOwner());
 	    	entity.setIsPublic(true);
 	    	dao.persist(entity);
 	    	entity.getDataSet().setLatestVersion(entity);
 	    	dao.update(entity.getDataSet());
 	    	dataSetVersions.add(entity);
 		}
     	
     	long count = service.getPublicDataSetVersionsCount(experiment);
     	Assert.assertEquals(count, 10);
     		
     	for( DataSetVersion dsv : dataSetVersions )
     	{
     	    dsv.getDataSet().setLatestVersion(null);
     	    dao.update(dsv.getDataSet());
     	}
 		dao.deleteAll("DataSetVersion");
 		experimentService.removeCurrentExperimentFromExperimentables(experiment);
 		experimentService.remove(experiment);
 		dao.deleteAll("DataSet");
 		dao.deleteAll("Country");
 		dao.deleteAll("VegetationType");
 		dao.deleteAll("User");
 	}
 	
 	@Test
 	public void testGetPublicDataSetVersionsCountNullExperiment()
 	{
 		List<DataSetVersion> dataSetVersions = new ArrayList<DataSetVersion>();
 		User user = null;
 		Country country = null;
 		VegetationType vegType = null;
 		
 		for( int i=0; i<10; ++i)
 		{
 	    	entity = TestEntityFactory.dataSetVersion();
 	    	if( i ==0 )
 	    	{
 	    	    dao.persist(entity.getDataSet().getCountry());
 	    	    country = entity.getDataSet().getCountry();
 	    	    dao.persist(entity.getDataSet().getVegType());
 	    	    vegType = entity.getDataSet().getVegType();
 	    	}
 	    	else
 	    	{
 	    		entity.getDataSet().setCountry(country);
 	    		entity.getDataSet().setVegType(vegType);
 	    	}
 	    	if( i == 0 )
 	    	{
 	    	    dao.persist(entity.getDataSet().getOwner());
 	    	    user = entity.getDataSet().getOwner();
 	    	}
 	    	else
 	    	{
 	    		entity.getDataSet().setOwner(user);
 	    	}
 	    	dao.persist(entity.getDataSet());
 	    	entity.getDataSet().setExperiment(null);
 	    	dao.update(entity.getDataSet());
 	    	entity.setDataSetId(entity.getDataSet().getId());
 	    	entity.setOwner(entity.getDataSet().getOwner());
 	    	entity.setIsPublic(true);
 	    	dao.persist(entity);
 	    	entity.getDataSet().setLatestVersion(entity);
 	    	dao.update(entity.getDataSet());
 	    	dataSetVersions.add(entity);
 		}
     	
     	long count = service.getPublicDataSetVersionsCount(null);
     	Assert.assertEquals(count, 10);
     		
     	for( DataSetVersion dsv : dataSetVersions )
     	{
     	    dsv.getDataSet().setLatestVersion(null);
     	    dao.update(dsv.getDataSet());
     	}
 		dao.deleteAll("DataSetVersion");
 		dao.deleteAll("DataSet");
 		dao.deleteAll("Country");
 		dao.deleteAll("VegetationType");
 		dao.deleteAll("User");
 	}
 	
 	@Test
 	public void testCopy() throws IOException
 	{
 		setUp();
 		
 	    // set a new path to app data in case we interact with the existing data
 	    Configuration.getInstance().PATH_TO_APP_DATA = "testdata/dsvServiceTest";
 		
 	    // create dummy files to make sure they are copied
 	    File metFile = new File(entity.retrieveMetFilePath());
 	    metFile.createNewFile();
 	    File fluxFile = new File(entity.retrieveOutputFilePath());
 	    fluxFile.createNewFile();
 	    File uploadedFile = new File(entity.uploadedFilePath());
 	    uploadedFile.createNewFile();
 	    File qcFile = new File(entity.retrieveQCPlotsFilePath());
 	    qcFile.createNewFile();
 	    File benchFile = new File(entity.retrieveBenchFilePath());
 	    benchFile.createNewFile();
 	    
 	    DataSetVersion copy = service.copy(entity,entity.getDataSet());
 	    Assert.assertNotNull(copy.getId());
 	    Assert.assertNotSame(copy.getId(), entity.getId());
 	    Assert.assertEquals(copy.getDataSet().getId(),entity.getDataSet().getId());
 	    Assert.assertEquals(copy.getDataSetId(),entity.getDataSetId());
 	    Assert.assertEquals(copy.getDescription(),entity.getDescription());
 	    Assert.assertEquals(copy.getDisplayName(),entity.getDisplayName());
 	    Assert.assertEquals(copy.getMetadata(),entity.getMetadata());
 	    Assert.assertEquals(copy.getName(),entity.getName());
 	    Assert.assertEquals(copy.getOriginalFileName(),entity.getOriginalFileName());
 	    Assert.assertEquals(copy.getShortDescription(),entity.getShortDescription());
 	    Assert.assertTrue(copy.getStatus() == Analysis.STATUS_NEW);
 	    Assert.assertEquals(copy.getEndDate().getTime(),entity.getEndDate().getTime());
 	    Assert.assertEquals(copy.getIsPublic(),entity.getIsPublic());
 	    Assert.assertEquals(copy.getOwner().getUsername(),entity.getOwner().getUsername());
 	    Assert.assertEquals(copy.getStartDate().getTime(),entity.getStartDate().getTime());
 	    Assert.assertEquals(copy.getUploadDate().getTime(),entity.getUploadDate().getTime());
 	    
 	    checkDifferentFile(copy.uploadedFilePath(),entity.uploadedFilePath());
 	    checkDifferentFile(copy.retrieveMetFilePath(),entity.retrieveMetFilePath());
 	    checkDifferentFile(copy.retrieveOutputFilePath(),entity.retrieveOutputFilePath());
 	    checkDifferentFile(copy.retrieveQCPlotsFilePath(),entity.retrieveQCPlotsFilePath());
 	    checkDifferentFile(copy.retrieveBenchFilePath(),entity.retrieveBenchFilePath());
 		
 	    service.delete(copy.getId());
 		tearDown();
 		metFile.delete();
 		fluxFile.delete();
 		uploadedFile.delete();
 		qcFile.delete();
 		benchFile.delete();
 	}
 	
 	@Test
 	public void testCopyWithNulls() throws IOException
 	{
 		setUp();
 		
 	    // set a new path to app data in case we interact with the existing data
 	    Configuration.getInstance().PATH_TO_APP_DATA = "testdata/dsvServiceTest";
 		
 	    // create dummy files to make sure they are copied
 	    File metFile = new File(entity.retrieveMetFilePath());
 //	    System.out.println(metFile.getAbsolutePath());
 	    metFile.createNewFile();
 	    File fluxFile = new File(entity.retrieveOutputFilePath());
 	    fluxFile.createNewFile();
 	    File uploadedFile = new File(entity.uploadedFilePath());
 	    uploadedFile.createNewFile();
 	    File qcFile = new File(entity.retrieveQCPlotsFilePath());
 	    qcFile.createNewFile();
 	    
 	    entity.setEndDate(null);
 	    entity.setStartDate(null);
 	    entity.setUploadDate(null);
 	    
 	    DataSetVersion copy = service.copy(entity,entity.getDataSet());
 	    Assert.assertNotNull(copy.getId());
 	    Assert.assertEquals(copy.getDataSet().getId(),entity.getDataSet().getId());
 	    Assert.assertEquals(copy.getDataSetId(),entity.getDataSetId());
 	    Assert.assertEquals(copy.getDescription(),entity.getDescription());
 	    Assert.assertEquals(copy.getDisplayName(),entity.getDisplayName());
 	    Assert.assertEquals(copy.getMetadata(),entity.getMetadata());
 	    Assert.assertEquals(copy.getName(),entity.getName());
 	    Assert.assertEquals(copy.getOriginalFileName(),entity.getOriginalFileName());
 	    Assert.assertEquals(copy.getShortDescription(),entity.getShortDescription());
 	    Assert.assertTrue(copy.getStatus() == Analysis.STATUS_NEW);
 	    Assert.assertNull(copy.getEndDate());
 	    Assert.assertEquals(copy.getIsPublic(),entity.getIsPublic());
 	    Assert.assertEquals(copy.getOwner().getUsername(),entity.getOwner().getUsername());
 	    Assert.assertNull(copy.getStartDate());
 	    Assert.assertNull(copy.getUploadDate());
 	    
 	    checkDifferentFile(copy.uploadedFilePath(),entity.uploadedFilePath());
 	    checkDifferentFile(copy.retrieveMetFilePath(),entity.retrieveMetFilePath());
 	    checkDifferentFile(copy.retrieveOutputFilePath(),entity.retrieveOutputFilePath());
 	    checkDifferentFile(copy.retrieveQCPlotsFilePath(),entity.retrieveQCPlotsFilePath());
 		
 		tearDown();
 		metFile.delete();
 		fluxFile.delete();
 		uploadedFile.delete();
 		qcFile.delete();
 	}
 	
 	public void checkDifferentFile(String differentFile, String original)
 	{
 	    Assert.assertFalse(differentFile.equals(original));
 	    File file = new File(differentFile);
 	    Assert.assertTrue(file.exists());
 	}
 	
 //	@Test
 //	public void testLoadFile() throws IOException, InvalidRangeException
 //	{
 //		String filename = "testdata/ds3.264_met.nc";
 //		NetcdfFile ncFile = NetcdfFile.open(filename);
 //		Map<String,Variable> ncVarMapUC = NetcdfUtil.extractVariableMapUpperCase(ncFile);
 //	    for( String key : ncVarMapUC.keySet() )
 //	    {
 //	    	System.out.println(key);
 //	    }
 //	    Array rainArray = ncVarMapUC.get("RAINF").read();
 //	    Array timeArray = ncVarMapUC.get("TIME").read();
 //	    ncFile.close();
 //	}
 	
 	@Test
 	public void testFixTimeCSV() throws IOException, ParseException
 	{
 		String filename = "testdata/ds3.264_orig.csv";
 		List<String> csv = service.fixTimeCSV(filename);
 	    Assert.assertEquals(csv.get(0),"1997-01-01 00:00,0,0,-9999,-9999,4.3,0,53.5,0,3.49,0,0,0,-9999.00,-9999,985,0,-9999.00,-9999,-50,0,-9999.00,-9999,6,0,-48,0,-0.591,0,-9999.00,-9999\n");
 	    Assert.assertEquals(csv.get(1),"1997-01-01 00:30,0,3,-9999,-9999,-0.8,2,71.93,2,2.689,2,0,1,-9999.00,-9999,978.2,2,-9999.00,-9999,-17.9,2,-9999.00,-9999,2.51,2,-11.46,2,-0.042,1,-9999.00,-9999\n");
 	}
 	
 	@Test
 	public void testFixTimeCSVWithDateStart() throws IOException, ParseException
 	{
 		String filename = "testdata/ds3.264_orig.csv";
 		String date = "1997,01,01";
 		List<String> csv = service.fixTimeCSV(filename,date);
 		System.out.println(csv.get(0));
 		System.out.println(csv.get(1));
 	    Assert.assertEquals(csv.get(0),"1997-01-01 00:00,0,0,-9999,-9999,4.3,0,53.5,0,3.49,0,0,0,-9999.00,-9999,985,0,-9999.00,-9999,-50,0,-9999.00,-9999,6,0,-48,0,-0.591,0,-9999.00,-9999\n");
 	    Assert.assertEquals(csv.get(1),"1997-01-01 00:30,0,3,-9999,-9999,-0.8,2,71.93,2,2.689,2,0,1,-9999.00,-9999,978.2,2,-9999.00,-9999,-17.9,2,-9999.00,-9999,2.51,2,-11.46,2,-0.042,1,-9999.00,-9999\n");
 	}
 	
 	@Test
 	public void testFixTimeCSVWithDate() throws IOException, ParseException
 	{
 		String filename = "testdata/ds3.264_orig.csv";
 		String date = "1997,01,10";
 		List<String> csv = service.fixTimeCSV(filename,date);
 		System.out.println(csv.get(0));
 		System.out.println(csv.get(1));
 	    Assert.assertEquals(csv.get(0),"1997-01-10 00:00,0,0,-9999,-9999,-1.6,0,98.8,0,0.34,0,0,0,-9999.00,-9999,968,0,-9999.00,-9999,-9,0,-9999.00,-9999,2.43,2,-7.64,2,0.01,1,-9999.00,-9999\n");
 	    Assert.assertEquals(csv.get(1),"1997-01-10 00:30,0,0,-9999,-9999,-1.9,0,99.6,0,0.96,0,0,0,-9999.00,-9999,968,0,-9999.00,-9999,-8,0,-9999.00,-9999,2.43,2,-7.64,2,0.01,1,-9999.00,-9999\n");
 	}
 	
 	@Test
 	public void testParseDate()
 	{
 		String date = "1997,01,10";
 		String dateString = service.parseDate(date);
 		Assert.assertEquals(dateString,"1997-01-10 00:00");
 	}
 	
 	@Test
 	public void testParseDateZeroPad()
 	{
 		String date = "1997,1,1";
 		String dateString = service.parseDate(date);
 		Assert.assertEquals(dateString,"1997-01-01 00:00");
 	}
 	
 	@Test
 	public void testFirstDate() throws IOException, ParseException
 	{
 		String filename = "testdata/ds3.264_orig.csv";
 		String date = service.getFirstDate(filename);
 		Assert.assertEquals(date,"01/01/1997");
 	}
 	
 	@Test
 	public void testLastDate() throws IOException, ParseException
 	{
 		String filename = "testdata/ds3.264_orig.csv";
 		String date = service.getLastDate(filename);
 		Assert.assertEquals(date,"12/31/1997");
 	}
 	
 	@Test
 	public void testGetDataSetVersions() throws IOException
 	{
 		setUp();
 		List<DataSetVersion> results = service.getDataSetVersions();
 		Assert.assertEquals(results.size(),1);
 		tearDown();
 	}
 	
 	@Test
 	public void testGetPublicDataSetVersionsColumnNameAsc()
 	{
 		List<DataSetVersion> dataSetVersions = new ArrayList<DataSetVersion>();
 		User user = null;
 		Experiment experiment = null;
 		Country country = null;
 		VegetationType vegType = null;
 		
 		for( int i=0; i<10; ++i)
 		{
 	    	entity = TestEntityFactory.dataSetVersion();
 	    	if( i ==0 )
 	    	{
 	    	    dao.persist(entity.getDataSet().getCountry());
 	    	    country = entity.getDataSet().getCountry();
 	    	    dao.persist(entity.getDataSet().getVegType());
 	    	    vegType = entity.getDataSet().getVegType();
 	    	}
 	    	else
 	    	{
 	    		entity.getDataSet().setCountry(country);
 	    		entity.getDataSet().setVegType(vegType);
 	    	}
 	    	if( i == 0 )
 	    	{
 	    	    dao.persist(entity.getDataSet().getOwner());
 	    	    user = entity.getDataSet().getOwner();
 	    	}
 	    	else
 	    	{
 	    		entity.getDataSet().setOwner(user);
 	    	}
 	    	entity.getDataSet().setName(new Integer(i).toString());
 	    	dao.persist(entity.getDataSet());
 	    	if( i == 0 )
 	    	{
 	    	    experiment = TestEntityFactory.experiment();
 	    	    experiment.setOwner(entity.getDataSet().getOwner());
 	    	    dao.persist(experiment);
 	    	}
 	    	entity.getDataSet().setExperiment(experiment);
 	    	dao.update(entity.getDataSet());
 	    	entity.setDataSetId(entity.getDataSet().getId());
 	    	entity.setOwner(entity.getDataSet().getOwner());
 	    	entity.setIsPublic(true);
 	    	dao.persist(entity);
 	    	entity.getDataSet().setLatestVersion(entity);
 	    	dao.update(entity.getDataSet());
 	    	dataSetVersions.add(entity);
 		}
     	
     	List<DataSetVersion> results = service.getPublicDataSetVersions(experiment,10,0,
     	    DataSetVersionService.COLUMN_NAME,true);
     	Assert.assertEquals(results.size(), 10);
     	
     	Integer last = null;
     	for( DataSetVersion dsv : results )
     	{
     		Integer nameNum = new Integer(dsv.getDataSet().getName());
 //    		System.out.println(nameNum);
     		if( last != null )
     		{
     			Assert.assertTrue(nameNum.compareTo(last) > 0);
     		}
     		last = nameNum;
     	}
     		
     	for( DataSetVersion dsv : dataSetVersions )
     	{
     	    dsv.getDataSet().setLatestVersion(null);
     	    dao.update(dsv.getDataSet());
     	}
 		dao.deleteAll("DataSetVersion");
 		experimentService.removeCurrentExperimentFromExperimentables(experiment);
 		experimentService.remove(experiment);
 		dao.deleteAll("DataSet");
 		dao.deleteAll("Country");
 		dao.deleteAll("VegetationType");
 		dao.deleteAll("User");
 	}
 	
 	@Test
 	public void testGetPublicDataSetVersionsColumnNameDesc()
 	{
 		List<DataSetVersion> dataSetVersions = new ArrayList<DataSetVersion>();
 		User user = null;
 		Experiment experiment = null;
 		Country country = null;
 		VegetationType vegType = null;
 		
 		for( int i=0; i<10; ++i)
 		{
 	    	entity = TestEntityFactory.dataSetVersion();
 	    	if( i ==0 )
 	    	{
 	    	    dao.persist(entity.getDataSet().getCountry());
 	    	    country = entity.getDataSet().getCountry();
 	    	    dao.persist(entity.getDataSet().getVegType());
 	    	    vegType = entity.getDataSet().getVegType();
 	    	}
 	    	else
 	    	{
 	    		entity.getDataSet().setCountry(country);
 	    		entity.getDataSet().setVegType(vegType);
 	    	}
 	    	if( i == 0 )
 	    	{
 	    	    dao.persist(entity.getDataSet().getOwner());
 	    	    user = entity.getDataSet().getOwner();
 	    	}
 	    	else
 	    	{
 	    		entity.getDataSet().setOwner(user);
 	    	}
 	    	entity.getDataSet().setName(new Integer(9-i).toString());
 	    	dao.persist(entity.getDataSet());
 	    	if( i == 0 )
 	    	{
 	    	    experiment = TestEntityFactory.experiment();
 	    	    experiment.setOwner(entity.getDataSet().getOwner());
 	    	    dao.persist(experiment);
 	    	}
 	    	entity.getDataSet().setExperiment(experiment);
 	    	dao.update(entity.getDataSet());
 	    	entity.setDataSetId(entity.getDataSet().getId());
 	    	entity.setOwner(entity.getDataSet().getOwner());
 	    	entity.setIsPublic(true);
 	    	dao.persist(entity);
 	    	entity.getDataSet().setLatestVersion(entity);
 	    	dao.update(entity.getDataSet());
 	    	dataSetVersions.add(entity);
 		}
     	
     	List<DataSetVersion> results = service.getPublicDataSetVersions(experiment,10,0,
     	    DataSetVersionService.COLUMN_NAME,false);
     	Assert.assertEquals(results.size(), 10);
     	
     	Integer last = null;
     	for( DataSetVersion dsv : results )
     	{
     		Integer nameNum = new Integer(dsv.getDataSet().getName());
     		if( last != null )
     		{
     			Assert.assertTrue(nameNum.compareTo(last) < 0);
     		}
     		last = nameNum;
     	}
     		
     	for( DataSetVersion dsv : dataSetVersions )
     	{
     	    dsv.getDataSet().setLatestVersion(null);
     	    dao.update(dsv.getDataSet());
     	}
 		dao.deleteAll("DataSetVersion");
 		experimentService.removeCurrentExperimentFromExperimentables(experiment);
 		experimentService.remove(experiment);
 		dao.deleteAll("DataSet");
 		dao.deleteAll("Country");
 		dao.deleteAll("VegetationType");
 		dao.deleteAll("User");
 	}
 	
 	@Test
 	public void testGetPublicDataSetVersionsColumnCountryAsc()
 	{
 		List<DataSetVersion> dataSetVersions = new ArrayList<DataSetVersion>();
 		User user = null;
 		Experiment experiment = null;
 		Country country = null;
 		VegetationType vegType = null;
 		
 		for( int i=0; i<10; ++i)
 		{
 	    	entity = TestEntityFactory.dataSetVersion();
 	    	if( i ==0 )
 	    	{
 	    	    dao.persist(entity.getDataSet().getVegType());
 	    	    vegType = entity.getDataSet().getVegType();
 	    	}
 	    	else
 	    	{
 	    		entity.getDataSet().setVegType(vegType);
 	    	}
 	    	if( i == 0 )
 	    	{
 	    	    dao.persist(entity.getDataSet().getOwner());
 	    	    user = entity.getDataSet().getOwner();
 	    	}
 	    	else
 	    	{
 	    		entity.getDataSet().setOwner(user);
 	    	}
 	    	entity.getDataSet().getCountry().setName(new Integer(i).toString());
 	    	entity.getDataSet().setName(new Integer(9-i).toString());
 	    	dao.persist(entity.getDataSet().getCountry());
 	    	dao.persist(entity.getDataSet());
 	    	if( i == 0 )
 	    	{
 	    	    experiment = TestEntityFactory.experiment();
 	    	    experiment.setOwner(entity.getDataSet().getOwner());
 	    	    dao.persist(experiment);
 	    	}
 	    	entity.getDataSet().setExperiment(experiment);
 	    	dao.update(entity.getDataSet());
 	    	entity.setDataSetId(entity.getDataSet().getId());
 	    	entity.setOwner(entity.getDataSet().getOwner());
 	    	entity.setIsPublic(true);
 	    	dao.persist(entity);
 	    	entity.getDataSet().setLatestVersion(entity);
 	    	dao.update(entity.getDataSet());
 	    	dataSetVersions.add(entity);
 		}
     	
     	List<DataSetVersion> results = service.getPublicDataSetVersions(experiment,10,0,
     	    DataSetVersionService.COLUMN_COUNTRY,true);
     	Assert.assertEquals(results.size(), 10);
     	
     	Integer last = null;
     	for( DataSetVersion dsv : results )
     	{
     		Integer nameNum = new Integer(dsv.getDataSet().getCountry().getName());
     		if( last != null )
     		{
     			Assert.assertTrue(nameNum.compareTo(last) > 0);
     		}
     		last = nameNum;
     	}
     		
     	for( DataSetVersion dsv : dataSetVersions )
     	{
     	    dsv.getDataSet().setLatestVersion(null);
     	    dao.update(dsv.getDataSet());
     	}
 		dao.deleteAll("DataSetVersion");
 		experimentService.removeCurrentExperimentFromExperimentables(experiment);
 		experimentService.remove(experiment);
 		dao.deleteAll("DataSet");
 		dao.deleteAll("Country");
 		dao.deleteAll("VegetationType");
 		dao.deleteAll("User");
 	}
 	
 	@Test
 	public void testGetPublicDataSetVersionsColumnVegTypeAsc()
 	{
 		List<DataSetVersion> dataSetVersions = new ArrayList<DataSetVersion>();
 		User user = null;
 		Experiment experiment = null;
 		Country country = null;
 		
 		for( int i=0; i<10; ++i)
 		{
 	    	entity = TestEntityFactory.dataSetVersion();
 	    	if( i ==0 )
 	    	{
 	    	    dao.persist(entity.getDataSet().getCountry());
 	    	    country = entity.getDataSet().getCountry();
 	    	}
 	    	else
 	    	{
 	    		entity.getDataSet().setCountry(country);
 	    	}
 	    	if( i == 0 )
 	    	{
 	    	    dao.persist(entity.getDataSet().getOwner());
 	    	    user = entity.getDataSet().getOwner();
 	    	}
 	    	else
 	    	{
 	    		entity.getDataSet().setOwner(user);
 	    	}
 	    	entity.getDataSet().setName(new Integer(9-i).toString());
 	    	entity.getDataSet().getVegType().setVegetationType(new Integer(i).toString());
 	    	dao.persist(entity.getDataSet().getVegType());
 	    	dao.persist(entity.getDataSet());
 	    	if( i == 0 )
 	    	{
 	    	    experiment = TestEntityFactory.experiment();
 	    	    experiment.setOwner(entity.getDataSet().getOwner());
 	    	    dao.persist(experiment);
 	    	}
 	    	entity.getDataSet().setExperiment(experiment);
 	    	dao.update(entity.getDataSet());
 	    	entity.setDataSetId(entity.getDataSet().getId());
 	    	entity.setOwner(entity.getDataSet().getOwner());
 	    	entity.setIsPublic(true);
 	    	dao.persist(entity);
 	    	entity.getDataSet().setLatestVersion(entity);
 	    	dao.update(entity.getDataSet());
 	    	dataSetVersions.add(entity);
 		}
     	
     	List<DataSetVersion> results = service.getPublicDataSetVersions(experiment,10,0,
     	    DataSetVersionService.COLUMN_VEG_TYPE,true);
     	Assert.assertEquals(results.size(), 10);
     	
     	Integer last = null;
     	for( DataSetVersion dsv : results )
     	{
     		Integer nameNum = new Integer(dsv.getDataSet().getVegType().getVegetationType());
     		if( last != null )
     		{
     			Assert.assertTrue(nameNum.compareTo(last) > 0);
     		}
     		last = nameNum;
     	}
     		
     	for( DataSetVersion dsv : dataSetVersions )
     	{
     	    dsv.getDataSet().setLatestVersion(null);
     	    dao.update(dsv.getDataSet());
     	}
 		dao.deleteAll("DataSetVersion");
 		experimentService.removeCurrentExperimentFromExperimentables(experiment);
 		experimentService.remove(experiment);
 		dao.deleteAll("DataSet");
 		dao.deleteAll("Country");
 		dao.deleteAll("VegetationType");
 		dao.deleteAll("User");
 	}
 	
 	@Test
 	public void testGetPublicDataSetVersionsColumnCreatedByAsc()
 	{
 		List<DataSetVersion> dataSetVersions = new ArrayList<DataSetVersion>();
 		Experiment experiment = null;
 		Country country = null;
 		VegetationType vegType = null;
 		
 		for( int i=0; i<10; ++i)
 		{
 	    	entity = TestEntityFactory.dataSetVersion();
 	    	if( i ==0 )
 	    	{
 	    	    dao.persist(entity.getDataSet().getCountry());
 	    	    country = entity.getDataSet().getCountry();
 	    	    dao.persist(entity.getDataSet().getVegType());
 	    	    vegType = entity.getDataSet().getVegType();
 	    	}
 	    	else
 	    	{
 	    		entity.getDataSet().setCountry(country);
 	    		entity.getDataSet().setVegType(vegType);
 	    	}
 	    	entity.getDataSet().getOwner().setFullName(new Integer(i).toString());
 	    	entity.getDataSet().getOwner().setUsername(new Integer(i).toString());
 	    	dao.persist(entity.getDataSet().getOwner());
 	    	entity.getDataSet().setName(new Integer(9-i).toString());
 	    	dao.persist(entity.getDataSet());
 	    	if( i == 0 )
 	    	{
 	    	    experiment = TestEntityFactory.experiment();
 	    	    experiment.setOwner(entity.getDataSet().getOwner());
 	    	    dao.persist(experiment);
 	    	}
 	    	entity.getDataSet().setExperiment(experiment);
 	    	dao.update(entity.getDataSet());
 	    	entity.setDataSetId(entity.getDataSet().getId());
 	    	entity.setOwner(entity.getDataSet().getOwner());
 	    	entity.setIsPublic(true);
 	    	dao.persist(entity);
 	    	entity.getDataSet().setLatestVersion(entity);
 	    	dao.update(entity.getDataSet());
 	    	dataSetVersions.add(entity);
 		}
     	
     	List<DataSetVersion> results = service.getPublicDataSetVersions(experiment,10,0,
     	    DataSetVersionService.COLUMN_CREATED_BY,true);
     	Assert.assertEquals(results.size(), 10);
     	
     	Integer last = null;
     	for( DataSetVersion dsv : results )
     	{
     		Integer nameNum = new Integer(dsv.getDataSet().getOwner().getFullName());
     		if( last != null )
     		{
     			Assert.assertTrue(nameNum.compareTo(last) > 0);
     		}
     		last = nameNum;
     	}
     		
     	for( DataSetVersion dsv : dataSetVersions )
     	{
     	    dsv.getDataSet().setLatestVersion(null);
     	    dao.update(dsv.getDataSet());
     	}
 		dao.deleteAll("DataSetVersion");
 		experimentService.removeCurrentExperimentFromExperimentables(experiment);
 		experimentService.remove(experiment);
 		dao.deleteAll("DataSet");
 		dao.deleteAll("Country");
 		dao.deleteAll("VegetationType");
 		dao.deleteAll("User");
 	}
 	
 	@Test
 	public void testDataAvailableForColumns() throws IOException
 	{
 		String filename = "testdata/ds3.264_orig.csv";
 		boolean[] availableColumns = service.dataAvailableForColumns(filename);
 		boolean[] expected = {false,false,true,true,false,false,true,true,true,true,true,true,true,true,false,false,true,true,false,false,true,true,false,false,true,true,true,true,true,true,false,false};
 	    
 		for(int i=0; i < availableColumns.length; ++i )
 		{
 			Assert.assertEquals(availableColumns[i],expected[i]);
 		}
 	}
 	
 //	@Test
 //	public void testEmpiricalBenchmarks() throws IOException, CSV2NCDFConversionException, AnalysisException, InterruptedException
 //	{
 //		List<String> benchmarks = new ArrayList<String>();
 //	    benchmarks.add("testdata/benchmarks/bugacConversion1.0.2.csv");
 //	    benchmarks.add("testdata/benchmarks/degeroConversion1.0.2.csv");
 //	    benchmarks.add("testdata/benchmarks/espirraConversion1.0.2.csv");
 //		
 //	    boolean saved = false;
 //	    User user = null;
 //	    DataSetVersion dsvLeftOut = null;
 //	    
 //	    for( String benchmark : benchmarks )
 //	    {
 //	    	DataSetVersion dsv = TestEntityFactory.dataSetVersion();
 //	    	if( dsvLeftOut == null ) dsvLeftOut = dsv;
 //	    	dao.persist(dsv.getDataSet().getCountry());
 //	    	dsv.getDataSet().getVegType().setVegetationType(benchmark);
 //	    	dao.persist(dsv.getDataSet().getVegType());
 //	    	if(!saved)
 //	    	{
 //	    	    dao.persist(dsv.getDataSet().getOwner());
 //	    	    saved = true;
 //	    	    user = dsv.getDataSet().getOwner();
 //	    	}
 //	    	else
 //	    	{
 //	    		dsv.getDataSet().setOwner(user);
 //	    	}
 //	    	dao.persist(dsv.getDataSet());
 //	    	dsv.setDataSetId(dsv.getDataSet().getId());
 //	    	dsv.setOwner(dsv.getDataSet().getOwner());
 //	    	dao.persist(dsv);
 //	    	File storeUploadedFile = new File(dsv.uploadedFilePath());
 //	    	File uploadedCSVFile = new File(benchmark);
 //	    	FileUtils.copyFile(uploadedCSVFile,storeUploadedFile);
 //	        userService.convertDataSetCSV2NCDF(storeUploadedFile,
 //			    PalsFileUtils.getDataSetVersionFluxFile(dsv),
 //			    PalsFileUtils.getDataSetVersionMetFile(dsv),dsv);
 //	        dsv.getDataSet().setLatestVersion(dsv);
 //	        dao.update(dsv.getDataSet());
 //	    }
 //	    
 //	    Thread thread = service.empiricalBenchmarks(dsvLeftOut, user);
 //	    thread.join();
 //	    
 //		dao.deleteAll("Analysis");
 //		List<DataSet> dataSets = dao.getAll(DataSet.class.getName());
 //		for( DataSet dataSet : dataSets )
 //		{
 //			dataSet.setLatestVersion(null);
 //			dao.update(dataSet);
 //		}
 //		dao.deleteAll("DataSetVersion");
 //		dataSets = dao.getAll(DataSet.class.getName());
 //		for( DataSet dataSet : dataSets )
 //		{
 //			dataSetService.delete(dataSet);
 //		}
 //		dao.deleteAll("Country");
 //		dao.deleteAll("VegetationType");
 //		dao.deleteAll("User");
 //		dao.deleteAll("AnalysisType");
 //	}
 	
 	@Test
 	public void testGetAllDataSetVersions()
 	{
     	entity = TestEntityFactory.dataSetVersion();
     	dao.persist(entity.getDataSet().getCountry());
     	dao.persist(entity.getDataSet().getVegType());
     	dao.persist(entity.getDataSet().getOwner());
     	dao.persist(entity.getDataSet());
     	Experiment experiment = TestEntityFactory.experiment();
     	experiment.setOwner(entity.getDataSet().getOwner());
     	dao.persist(experiment);
     	entity.getDataSet().setExperiment(experiment);
     	dao.update(entity.getDataSet());
     	entity.setDataSetId(entity.getDataSet().getId());
     	entity.setOwner(entity.getDataSet().getOwner());
     	dao.persist(entity);
     	entity.getDataSet().setLatestVersion(entity);
     	dao.update(entity.getDataSet());
     	
     	List<DataSetVersion> dataSetVersions = service.getAllDataSetVersions(experiment);
     	Assert.assertEquals(dataSetVersions.size(), 1);
     	Assert.assertEquals(dataSetVersions.get(0).getId(),entity.getId());
     		
     	entity.getDataSet().setLatestVersion(null);
     	dao.update(entity.getDataSet());
 		dao.deleteAll("DataSetVersion");
 		experimentService.removeCurrentExperimentFromExperimentables(experiment);
 		experimentService.remove(experiment);
 		dao.deleteAll("DataSet");
 		dao.deleteAll("Country");
 		dao.deleteAll("VegetationType");
 		dao.deleteAll("User");
 	}
 	
 //	@Test
 //	public void testGenerateBenchmarks() throws IOException, CSV2NCDFConversionException, AnalysisException, InterruptedException
 //	{
 //		List<String> benchmarks = new ArrayList<String>();
 //	    benchmarks.add("testdata/benchmarks/bugacConversion1.0.2.csv");
 //	    benchmarks.add("testdata/benchmarks/degeroConversion1.0.2.csv");
 //	    benchmarks.add("testdata/benchmarks/espirraConversion1.0.2.csv");
 //		
 //	    boolean saved = false;
 //	    User user = null;
 //	    DataSetVersion dsvLeftOut = null;
 //	    
 //	    for( String benchmark : benchmarks )
 //	    {
 //	    	DataSetVersion dsv = TestEntityFactory.dataSetVersion();
 //	    	if( dsvLeftOut == null ) dsvLeftOut = dsv;
 //	    	dao.persist(dsv.getDataSet().getCountry());
 //	    	dsv.getDataSet().getVegType().setVegetationType(benchmark);
 //	    	dao.persist(dsv.getDataSet().getVegType());
 //	    	if(!saved)
 //	    	{
 //	    	    dao.persist(dsv.getDataSet().getOwner());
 //	    	    saved = true;
 //	    	    user = dsv.getDataSet().getOwner();
 //	    	}
 //	    	else
 //	    	{
 //	    		dsv.getDataSet().setOwner(user);
 //	    	}
 //	    	dao.persist(dsv.getDataSet());
 //	    	dsv.setDataSetId(dsv.getDataSet().getId());
 //	    	dsv.setOwner(dsv.getDataSet().getOwner());
 //	    	dao.persist(dsv);
 //	    	File storeUploadedFile = new File(dsv.uploadedFilePath());
 //	    	File uploadedCSVFile = new File(benchmark);
 //	    	FileUtils.copyFile(uploadedCSVFile,storeUploadedFile);
 //	        userService.convertDataSetCSV2NCDF(storeUploadedFile,
 //			    PalsFileUtils.getDataSetVersionFluxFile(dsv),
 //			    PalsFileUtils.getDataSetVersionMetFile(dsv),dsv);
 //	        dsv.getDataSet().setLatestVersion(dsv);
 //	        dao.update(dsv.getDataSet());
 //	    }
 //	    
 //	    service.generateBenchmarks(user);
 //	    
 //		dao.deleteAll("Analysis");
 //		List<DataSet> dataSets = dao.getAll(DataSet.class.getName());
 //		for( DataSet dataSet : dataSets )
 //		{
 //			dataSet.setLatestVersion(null);
 //			dao.update(dataSet);
 //		}
 //		dao.deleteAll("DataSetVersion");
 //		dataSets = dao.getAll(DataSet.class.getName());
 //		for( DataSet dataSet : dataSets )
 //		{
 //			dataSetService.delete(dataSet);
 //		}
 //		dao.deleteAll("Country");
 //		dao.deleteAll("VegetationType");
 //		dao.deleteAll("User");
 //		dao.deleteAll("AnalysisType");
 //	}
 	
 }
