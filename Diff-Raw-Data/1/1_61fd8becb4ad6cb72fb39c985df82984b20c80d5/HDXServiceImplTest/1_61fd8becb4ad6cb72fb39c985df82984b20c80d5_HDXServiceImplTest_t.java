 package org.ocha.hdx.service;
 
 import java.io.IOException;
 import java.util.List;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.ocha.hdx.dto.apiv3.DatasetV3DTO;
 import org.ocha.hdx.dto.apiv3.DatasetV3WrapperDTO;
 import org.ocha.hdx.persistence.dao.UserDAO;
 import org.ocha.hdx.persistence.dao.ckan.CKANDatasetDAO;
 import org.ocha.hdx.persistence.dao.ckan.CKANResourceDAO;
 import org.ocha.hdx.persistence.entity.ckan.CKANDataset;
 import org.ocha.hdx.persistence.entity.ckan.CKANResource;
 import org.ocha.hdx.persistence.entity.ckan.CKANResource.WorkflowState;
 import org.ocha.hdx.security.exception.InsufficientCredentialsException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 //FIXME reactivate these integration tests when the dedicated ckan instance is ready
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = { "classpath:/ctx-config-test.xml", "classpath:/ctx-core.xml", "classpath:/ctx-dao.xml", "classpath:/ctx-service.xml", "classpath:/ctx-persistence-test.xml" })
 public class HDXServiceImplTest {
 
 	@Before
 	public void setUp() throws Exception {
 		userDAO.createUser("seustachi", "dummyPwd", "admin", "079f6194-45e1-4534-8ca7-1bd4130ef897");
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		userDAO.deleteUser("seustachi");
 		ckanResourceDAO.deleteAllCKANResourcesRecords();
 		ckanDatasetDAO.deleteAllCKANDatasetsRecords();
 	}
 
 	@Autowired
 	private HDXService hdxService;
 
 	@Autowired
 	private UserDAO userDAO;
 
 	@Autowired
 	private CKANResourceDAO ckanResourceDAO;
 
 	@Autowired
 	private CKANDatasetDAO ckanDatasetDAO;
 
 	@Test
 	public void testGetDatasetsListFromCKAN() throws InsufficientCredentialsException {
 		Assert.assertTrue(hdxService.getDatasetsListFromCKAN("seustachi").size() > 0);
 
 		try {
 			hdxService.getDatasetsListFromCKAN("otherUser");
 			Assert.fail("Should have raised an InsufficientCredentialsException");
 		} catch (final InsufficientCredentialsException e) {
 		}
 	}
 
 	// @Test
 	// public void testGetDatasetContentFromCKANV3() throws Exception {
 	// {
 	// final DatasetV3WrapperDTO dto = hdxService.getDatasetContentFromCKANV3("seustachi", "testforauth");
 	// Assert.assertTrue(dto.isSuccess());
 	// }
 	//
 	// try {
 	// hdxService.getDatasetContentFromCKANV3("otherUser", "testforauth");
 	// Assert.fail("Should have raised an InsufficientCredentialsException");
 	// } catch (final InsufficientCredentialsException e) {
 	// }
 	// }
 
 	@Test
 	public void testGetDatasetDTOFromQueryV3() {
 
 		{
 			final DatasetV3WrapperDTO wrapper = hdxService.getDatasetDTOFromQueryV3("raw-scraperwiki-input", null);
 			Assert.assertTrue(wrapper.isSuccess());
 			final DatasetV3DTO dto = wrapper.getResult();
 			Assert.assertEquals("raw-scraperwiki-input", dto.getName());
 			Assert.assertEquals(1396446415229L, dto.getRevision_timestamp().getTime());
 			Assert.assertEquals("bfef925e-537b-454a-8aea-3147bd28935e", dto.getRevision_id());
 			Assert.assertEquals(0, dto.getTags().size());
 			// Assert.assertEquals("Junk", dto.getTags().get(0).getName());
 			// Assert.assertEquals("toBeCurated", dto.getTags().get(1).getName());
 			Assert.assertEquals(0, dto.getExtras().size());
 			// Assert.assertEquals("hdx_status", dto.getExtras().get(0).getKey());
 			Assert.assertEquals(1, dto.getResources().size());
 
 			Assert.assertEquals("active", dto.getResources().get(0).getState());
 		}
 	}
 
 	@Test
 	@Ignore
 	public void testGetPrivateDatasetDTOFromQueryV3() {
 		{
 			final DatasetV3WrapperDTO dto = hdxService.getDatasetDTOFromQueryV3("testforauth", null);
 			// Cannot access private dataset without API key
 			Assert.assertNull(dto);
 		}
 
 		{
 			final DatasetV3WrapperDTO dto = hdxService.getDatasetDTOFromQueryV3("testforauth", "079f6194-45e1-4534-8ca7-1bd4130ef897");
 			Assert.assertTrue(dto.isSuccess());
 		}
 	}
 
 	@Test
 	public void testCheckForNewCKANDatasets() {
 		Assert.assertEquals(0, ckanDatasetDAO.listCKANDatasets().size());
 		hdxService.checkForNewCKANDatasets();
 		Assert.assertTrue(ckanDatasetDAO.listCKANDatasets().size() > 0);
 		for (final CKANDataset ckandDataset : ckanDatasetDAO.listCKANDatasets()) {
 			Assert.assertEquals(CKANDataset.Status.PENDING, ckandDataset.getStatus());
 			hdxService.flagDatasetAsToBeCurated(ckandDataset.getName(), CKANDataset.Type.SCRAPER_VALIDATING, 0);
 		}
 		for (final CKANDataset ckandDataset : ckanDatasetDAO.listCKANDatasets()) {
 			Assert.assertEquals(CKANDataset.Status.TO_BE_CURATED, ckandDataset.getStatus());
 		}
 	}
 
 	@Test
	@Ignore
 	public void testCheckForNewCKANResources() {
 		// we need to initialize datasets as to be curated to get some data to
 		// work on
 		hdxService.checkForNewCKANDatasets();
 		final List<CKANDataset> ckandDatasets = ckanDatasetDAO.listCKANDatasets();
 		for (final CKANDataset ckandDataset : ckandDatasets) {
 			Assert.assertEquals(CKANDataset.Status.PENDING, ckandDataset.getStatus());
 			if (ckandDataset.getName().startsWith("raw"))
 				hdxService.flagDatasetAsToBeCurated(ckandDataset.getName(), CKANDataset.Type.SCRAPER_VALIDATING, 0);
 		}
 
 		Assert.assertEquals(0, ckanResourceDAO.listCKANResources().size());
 		hdxService.checkForNewCKANResources();
 		Assert.assertTrue(ckanResourceDAO.listCKANResources().size() > 0);
 	}
 
 	@Test
 	@Ignore
 	public void testAddResourceToCKANDataset() throws IOException {
 		// hdxService.checkForNewCKANDatasets();
 		// final String firstDatasetName = ckanDatasetDAO.listCKANDatasets().get(0).getName();
 		// System.out.println("Will now try to add a resource to the dataset : " + firstDatasetName);
 
 		// final File inputFile = new ClassPathResource("samples/report/USA.xlsx").getFile();
 
 		final boolean result = hdxService.addResourceToCKANDataset("sdn",
 				"http://data.ochadata.net:9231/hdx-1.0.0/api/exporter/country/xlsx/SDN/fromYear/1998/toYear/2014/language/EN/SDN_baseline.xlsx", "sdn");
 		Assert.assertTrue(result);
 	}
 
 	@Ignore
 	@Test
 	public void testStandardWorkflow() throws IOException {
 		// we need to initialize datasets as to be curated to get some data to
 		// work on
 		hdxService.checkForNewCKANDatasets();
 		for (final CKANDataset ckandDataset : ckanDatasetDAO.listCKANDatasets()) {
 			Assert.assertEquals(CKANDataset.Status.PENDING, ckandDataset.getStatus());
 			if (!ckandDataset.getName().equals("nope")) {
 				hdxService.flagDatasetAsToBeCurated(ckandDataset.getName(), CKANDataset.Type.DUMMY, 0);
 			}
 		}
 
 		Assert.assertEquals(0, ckanResourceDAO.listCKANResources().size());
 		hdxService.checkForNewCKANResources();
 
 		final List<CKANResource> resources = ckanResourceDAO.listCKANResources();
 		Assert.assertTrue(resources.size() > 0);
 
 		{
 			final CKANResource firstResource = resources.get(0);
 			Assert.assertEquals(WorkflowState.DETECTED_NEW, firstResource.getWorkflowState());
 
 			hdxService.downloadFileForCKANResource(firstResource.getId().getId(), firstResource.getId().getRevision_id());
 
 			final CKANResource firstResourceAfterDownload = ckanResourceDAO.getCKANResource(firstResource.getId().getId(), firstResource.getId().getRevision_id());
 			Assert.assertEquals(WorkflowState.DOWNLOADED, firstResourceAfterDownload.getWorkflowState());
 
 			hdxService.evaluateFileForCKANResource(firstResource.getId().getId(), firstResource.getId().getRevision_id());
 
 			final CKANResource firstResourceAfterEvaluation = ckanResourceDAO.getCKANResource(firstResource.getId().getId(), firstResource.getId().getRevision_id());
 			Assert.assertEquals(WorkflowState.FILE_PRE_VALIDATION_SUCCESS, firstResourceAfterEvaluation.getWorkflowState());
 
 			hdxService.transformAndImportDataFromFileForCKANResource(firstResource.getId().getId(), firstResource.getId().getRevision_id());
 
 			final CKANResource firstResourceAfterImport = ckanResourceDAO.getCKANResource(firstResource.getId().getId(), firstResource.getId().getRevision_id());
 			Assert.assertEquals(WorkflowState.IMPORT_FAIL, firstResourceAfterImport.getWorkflowState());
 		}
 
 		{
 			final CKANResource secondResource = resources.get(1);
 			Assert.assertEquals(WorkflowState.DETECTED_NEW, secondResource.getWorkflowState());
 
 			hdxService.downloadFileForCKANResource(secondResource.getId().getId(), secondResource.getId().getRevision_id());
 
 			final CKANResource secondResourceAfterDownload = ckanResourceDAO.getCKANResource(secondResource.getId().getId(), secondResource.getId().getRevision_id());
 			Assert.assertEquals(WorkflowState.DOWNLOADED, secondResourceAfterDownload.getWorkflowState());
 
 			hdxService.evaluateFileForCKANResource(secondResource.getId().getId(), secondResource.getId().getRevision_id());
 
 			final CKANResource secondResourceAfterEvaluation = ckanResourceDAO.getCKANResource(secondResource.getId().getId(), secondResource.getId().getRevision_id());
 			Assert.assertEquals(WorkflowState.FILE_PRE_VALIDATION_FAIL, secondResourceAfterEvaluation.getWorkflowState());
 
 			hdxService.transformAndImportDataFromFileForCKANResource(secondResource.getId().getId(), secondResource.getId().getRevision_id());
 
 			// we should still be in FILE_PRE_VALIDATION_FAIL, as the workflow
 			// cannot go from FILE_PRE_VALIDATION_FAIL to IMPORT_XXX
 			final CKANResource secondResourceAfterImport = ckanResourceDAO.getCKANResource(secondResource.getId().getId(), secondResource.getId().getRevision_id());
 			Assert.assertEquals(WorkflowState.FILE_PRE_VALIDATION_FAIL, secondResourceAfterImport.getWorkflowState());
 		}
 
 	}
 }
