 package com.gentics.cr;
 
 import java.net.URISyntaxException;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.junit.AfterClass;
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.gentics.cr.exceptions.CRException;
 
 /**
  * This test checks the functionality of building of navigation objects in the.
  * 
  * {@link OptimisticNavigationRequestProcessor} Request Processor.
  * 
  * @author l.osang@gentics.com
  */
 
 public class CRRequestProcessorNavigationTest extends RequestProcessorTest {
 
 	/** The Constant LOGGER. */
 	private final static Logger LOGGER = Logger.getLogger(CRRequestProcessorNavigationTest.class);
 
 	/** The Constant attributes. */
 	private static final String[] attributes = { "name", "folder_id" };
 
 	/** The Constant FOLDER_TYPE. */
 	private static final String FOLDER_TYPE = CRResolvableBean.DEFAULT_DIR_TYPE;
 
 	/** The request processor. */
 	private static CRRequestProcessor requestProcessor;
 
 	/** The test handler. */
 	private static HSQLTestHandler testHandler;
 
 	/** The navigation request processor. */
 	private static OptimisticNavigationRequestProcessor navigationRequestProcessor;
 
 	/** The expected collection size. */
 	private static Integer expectedCollectionSize = 0;
 
 	/** The root folder content id. */
 	private static String rootFolderContentId;
 
 	/** The original navigation object. */
 	private static Collection<CRResolvableBean> originalNavigationObject;
 
 	/**
 	 * Setup the request processors and the hsql database handler
 	 * 
 	 * @throws CRException
 	 *             the cR exception
 	 * @throws URISyntaxException
 	 *             the uRI syntax exception
 	 */
 	@BeforeClass
 	public static void setUp() throws CRException, URISyntaxException {
		CRConfigUtil config = HSQLTestConfigFactory.getDefaultHSQLConfiguration(CRRequestProcessorTest.class.getName());
 
 		requestProcessor = new CRRequestProcessor(config.getRequestProcessorConfig(1));
 		navigationRequestProcessor = new OptimisticNavigationRequestProcessor(config.getRequestProcessorConfig(1));
 
 		testHandler = new HSQLTestHandler(config.getRequestProcessorConfig(1));
 
 		// a folder structure
 		createTestNavigationData(3, 3);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.gentics.cr.RequestProcessorTest#getRequestProcessor()
 	 */
 	@Override
 	protected RequestProcessor getRequestProcessor() {
 		return requestProcessor;
 	}
 
 	/**
 	 * Tear down and clean up.
 	 * 
 	 * @throws CRException
 	 *             the cR exception
 	 */
 	@AfterClass
 	public static void tearDown() throws CRException {
 		requestProcessor.finalize();
 		navigationRequestProcessor.finalize();
 		testHandler.cleanUp();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.gentics.cr.RequestProcessorTest#getExpectedCollectionSize()
 	 */
 	@Override
 	protected int getExpectedCollectionSize() {
 		return expectedCollectionSize;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.gentics.cr.RequestProcessorTest#getRequest()
 	 */
 	@Override
 	protected CRRequest getRequest() {
 		CRRequest req = new CRRequest();
 		req.setRequestFilter("object.obj_type == " + FOLDER_TYPE);
 		return req;
 	}
 
 	/**
 	 * Test optimistic navigation building.
 	 * 
 	 * @throws CRException
 	 *             the cR exception
 	 */
 	@Test
 	public void testOptimisticNavigationBuilding() throws CRException {
 
 		// check that the original navigation object is not empty
 		Assert.assertFalse(CollectionUtils.isEmpty(originalNavigationObject));
 
 		// do a request with the navigation request and the optimistic request processor
 		Collection<CRResolvableBean> result = navigationRequestProcessor.getNavigation(getNavigationRequest());
 
 		if (LOGGER.isDebugEnabled()) {
 			LOGGER.debug("size of constructed object: " + result.size());
 
 			for (CRResolvableBean crResolvableBean : result) {
 				LOGGER.debug(toStringWithChildren(crResolvableBean, 0));
 			}
 		}
 
 		Assert.assertTrue(compareResolvableChildren(originalNavigationObject, result));
 	}
 
 	/**
 	 * Test normal navigation building.
 	 * 
 	 * @throws CRException
 	 *             the cR exception
 	 */
 	@Test
 	public void testNavigationBuilding() throws CRException {
 
 		// check that the original navigation object is not empty
 		Assert.assertFalse(CollectionUtils.isEmpty(originalNavigationObject));
 
 		// do a request with the navigation request and the normal request processor
 		Collection<CRResolvableBean> result = requestProcessor.getNavigation(getNavigationRequest());
 
 		if (LOGGER.isDebugEnabled()) {
 			LOGGER.debug("size of constructed object: " + result.size());
 			for (CRResolvableBean crResolvableBean : result) {
 				LOGGER.debug(toStringWithChildren(crResolvableBean, 0));
 			}
 		}
 
 		Assert.assertTrue(compareResolvableChildren(originalNavigationObject, result));
 	}
 
 	/**
 	 * Gets the prepared Request for a Navigation Object building for any
 	 * request processor.
 	 * 
 	 * @return the prepared Navigation Request
 	 */
 	private CRRequest getNavigationRequest() {
 
 		CRRequest req = new CRRequest();
 
 		req.setRequestFilter("object.obj_type == " + FOLDER_TYPE + " AND object.folder_id == " + rootFolderContentId);
 		req.setChildFilter("object.obj_type == " + FOLDER_TYPE);
 		req.setAttributeArray(attributes);
 
 		return req;
 	}
 
 	/**
 	 * Creates the test navigation data.
 	 * 
 	 * @param vertical
 	 *            specifies how many nodes should be appended to every node
 	 * @param depth
 	 *            specifies how deep the tree should be
 	 * @throws CRException
 	 *             the cR exception
 	 */
 	private static void createTestNavigationData(Integer vertical, Integer depth) throws CRException {
 		// check if all necessary object are present
 		Assert.assertNotNull(requestProcessor);
 		Assert.assertNotNull(testHandler);
 		Assert.assertNotNull(vertical);
 		Assert.assertNotNull(depth);
 
 		// the root folder
 		CRResolvableBean rootFolder = createTestFolder(null, "root.html");
 
 		// save the root element contentid
 		rootFolderContentId = rootFolder.getContentid();
 
 		// create tree
 		recursiveTreeCreation(rootFolder, vertical, depth);
 
 		// save the original navigation object
 		originalNavigationObject = rootFolder.getChildRepository();
 
 		if (LOGGER.isDebugEnabled()) {
 			LOGGER.debug("tree created with " + vertical + " vertical child nodes and depth of " + depth);
 			LOGGER.debug("Inserted folders amount: " + expectedCollectionSize);
 			LOGGER.debug(toStringWithChildren(rootFolder, 0));
 		}
 
 	}
 
 	/**
 	 * recursive method to create a tree.
 	 * 
 	 * @param active
 	 *            is the active element, initially the root element
 	 * @param vertical
 	 *            specifies how many nodes should be appended to every node
 	 * @param depth
 	 *            specifies how deep the tree should be
 	 * @throws CRException
 	 *             the cR exception
 	 *             {@link CRRequestProcessorTest#createTestNavigationData(Integer, Integer)}
 	 */
 	private static void recursiveTreeCreation(CRResolvableBean active, int vertical, int depth) throws CRException {
 
 		// recursive break condition
 		if (depth <= 0) {
 			return;
 		}
 
 		String newName;
 
 		// create vertical nodes for the given active node as mother
 		for (int i = 0; i < vertical; i++) {
 			// the new test folder name
 			newName = "node_vertical_" + i + "_depth_" + depth + ".html";
 
 			// create node and start the same method with depth -1 and new node
 			// with parent tree node
 			recursiveTreeCreation(createTestFolder(active, newName), vertical, depth - 1);
 		}
 	}
 
 	/**
 	 * creates and persists a folder with specified parent folder and name.
 	 * 
 	 * @param parentFolder
 	 *            the mother folder
 	 * @param name
 	 *            the name for the new folder, should not be null
 	 * @return the created and persisted folder
 	 * @throws CRException
 	 *             the cR exception
 	 */
 	private static CRResolvableBean createTestFolder(CRResolvableBean parentFolder, String name) throws CRException {
 
 		if (StringUtils.isEmpty(name)) {
 			throw new IllegalArgumentException("no name specified for bean creation");
 		}
 
 		CRResolvableBean folder = new CRResolvableBean();
 		folder.setObj_type(FOLDER_TYPE);
 		folder.set("name", name);
 
 		if (parentFolder != null) {
 			// append the parent folder
 			folder.set("folder_id", parentFolder.getContentid());
 		}
 
 		// persist the Bean
 		folder = testHandler.createBean(folder, attributes);
 
 		if (parentFolder != null) {
 			parentFolder.getChildRepository().add(folder);
 		}
 
 		// increase size
 		expectedCollectionSize++;
 
 		if (LOGGER.isDebugEnabled()) {
 			LOGGER.debug("created folder bean " + folder.getContentid() + " with name: " + folder.get("name"));
 		}
 
 		return folder;
 	}
 
 	/**
 	 * Compares two collections of CRResolvableBeans against contentid and
 	 * childrepositories.
 	 * 
 	 * ATTENTION: not very fast! Should only be used for testing!
 	 * 
 	 * @param origTree
 	 * @param fetchedTree
 	 * @return true if the two collections are equal
 	 */
 	private static boolean compareResolvableChildren(final Collection<CRResolvableBean> origTree,
 			Collection<CRResolvableBean> fetchedTree) {
 		Boolean result = true;
 
 		Set<CRResolvableBean> matchesFoundOrig = new HashSet<CRResolvableBean>();
 		Set<CRResolvableBean> matchesFoundFetched = new HashSet<CRResolvableBean>();
 
 		for (CRResolvableBean crResolvableBean : origTree) {
 
 			for (CRResolvableBean crResolvableBeanFetched : fetchedTree) {
 
 				// check if we got a match and that match
 				if (crResolvableBean.getContentid().equals(crResolvableBeanFetched.getContentid())
 						&& !matchesFoundOrig.contains(crResolvableBean)) {
 
 					result = compareResolvableChildren(crResolvableBean.getChildRepository(),
 							crResolvableBeanFetched.getChildRepository());
 
 					if (result == false) {
 						break;
 					}
 
 					matchesFoundFetched.add(crResolvableBeanFetched);
 					// store which matches we already found
 					matchesFoundOrig.add(crResolvableBean);
 
 				}
 			}
 
 			if (result == false) {
 				break;
 			}
 		}
 
 		// return false if we didn't find all original entries
 		if (matchesFoundOrig.size() != origTree.size()) {
 			result = false;
 		}
 
 		// return false if we didn't got all fetched entries in the original
 		// tree
 		if (matchesFoundFetched.size() != fetchedTree.size()) {
 			result = false;
 		}
 
 		return result;
 	}
 
 	/**
 	 * Prints a tree view of the CRResolvable, convenient for navigation
 	 * objects.
 	 * 
 	 * @param resolvable
 	 *            the resolvable
 	 * @param depth
 	 *            the depth
 	 * @return String tree with children
 	 */
 	private static String toStringWithChildren(CRResolvableBean resolvable, int depth) {
 
 		StringBuilder sb = new StringBuilder();
 		sb.append(getTabs(depth));
 		sb.append("[" + resolvable.getContentid() + "]" + "\n");
 		sb.append(getTabs(depth));
 
 		if (resolvable.getChildRepository() != null && resolvable.getChildRepository().size() > 0) {
 			sb.append("children: { ");
 
 			for (CRResolvableBean child : resolvable.getChildRepository()) {
 				sb.append("\n");
 				sb.append(getTabs(depth));
 				sb.append(toStringWithChildren(child, depth + 1));
 			}
 			sb.append(getTabs(depth));
 			sb.append("}\n");
 		}
 
 		return sb.toString();
 	}
 
 	/**
 	 * Gets tab amount for output.
 	 * 
 	 * @param amount
 	 *            the amount
 	 * @return the tabs
 	 */
 	private static String getTabs(int amount) {
 
 		StringBuilder sb = new StringBuilder();
 		for (int i = 0; i < amount; i++) {
 			sb.append("	");
 		}
 		return sb.toString();
 	}
 }
