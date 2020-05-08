 /*******************************************************************************
  * Copyright (c) 2013 Ericsson Research Canada
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * This class implements JUnit Test Cases for comparing 2 review items
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  ******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.ui.tests.feature;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4ECommentType;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EParticipant;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4ETextContent;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4ETextPosition;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EUserRole;
 import org.eclipse.mylyn.reviews.r4e.core.model.RModelFactory;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIAnomalyBasic;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIFileContext;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIModelController;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIParticipant;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIReviewBasic;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIReviewGroup;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIReviewItem;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.navigator.ReviewNavigatorActionGroup;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.R4EUIConstants;
 import org.eclipse.mylyn.reviews.r4e.ui.tests.R4ETestSetup;
 import org.eclipse.mylyn.reviews.r4e.ui.tests.proxy.R4EUITestMain;
 import org.eclipse.mylyn.reviews.r4e.ui.tests.utils.R4EAssert;
 import org.eclipse.mylyn.reviews.r4e.ui.tests.utils.TestConstants;
 import org.eclipse.mylyn.reviews.r4e.ui.tests.utils.TestUtils;
 import org.junit.After;
 import org.junit.Before;
 
 @SuppressWarnings("restriction")
 public class CompareReviewItemsTests extends TestCase {
 
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 
 	private static final String JAVA_FILE12_EXT_MOD_PATH = "testFiles" + File.separator + "extModCommitFile12.java";
 
 	private static final String REVIEW_ITEM_COMMIT_1 = "Commit 1: second Jav...";
 
 	private static final String REVIEW_ITEM_COMMIT_2 = "Commit 2: second Jav...";
 
 	// ------------------------------------------------------------------------
 	// Member variables
 	// ------------------------------------------------------------------------
 
 	private R4EUITestMain fProxy = null;
 
 	private R4EUIReviewGroup fGroup = null;
 
 	private String fGroupName = null;
 
 	private R4EUIReviewBasic fReview = null;
 
 	private R4EUIReviewItem fItem = null;
 
 	private R4EUIReviewItem fItem2 = null;
 
 	private int fAnomalyFileIndex;
 
 	private R4EUIParticipant fParticipant = null;
 
 	private R4EUIAnomalyBasic fCompareEditorAnomaly = null;
 
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Method suite - Sets up the global test environment, if not already done at the suite level.
 	 * 
 	 * @return Test
 	 */
 	public static Test suite() {
 		TestSuite suite = new TestSuite();
 		suite.addTestSuite(CompareReviewItemsTests.class);
 		return new R4ETestSetup(suite);
 	}
 
 	/**
 	 * Method setUp - Sets up the fixture, for example, open a network connection. This method is called before a test
 	 * is executed.
 	 * 
 	 * @throws java.lang.Exception
 	 */
 	@Override
 	@Before
 	public void setUp() throws Exception {
 		fProxy = R4EUITestMain.getInstance();
 		createReviewGroup();
 		if (((ReviewNavigatorActionGroup) R4EUIModelController.getNavigatorView().getActionSet()).isHideDeltasFilterSet()) {
 			fProxy.getCommandProxy().toggleHideDeltasFilter();
 		}
 		createReview();
 		createReviewItems();
 		createParticipants();
 		createCompareEditorAnomalies();
 	}
 
 	/**
 	 * Method tearDown
 	 * 
 	 * @throws java.lang.Exception
 	 */
 	@Override
 	@After
 	public void tearDown() throws Exception {
 		fProxy = null;
 	}
 
 	/**
 	 * Method testInformalReviews
 	 * 
 	 * @throws CoreException
 	 */
 	public void testCompareReviewItems() throws CoreException, IOException, URISyntaxException {
 		TestUtils.waitForJobs();
 		commitNewReviewItemVersion();
 		compareReviewItemsVersions();
 	}
 
 	/**
 	 * Method createReviewGroup
 	 */
 	private void createReviewGroup() {
 
 		// Assert object
 		R4EAssert r4eAssert = new R4EAssert("createReviewGroups");
 
 		fGroup = null;
 
 		//Create Review Group
 		r4eAssert.setTest("Create Review Group");
 		for (R4EUIReviewGroup group : R4EUIModelController.getRootElement().getGroups()) {
 			if (group.getReviewGroup().getName().equals(TestConstants.REVIEW_GROUP_TEST_NAME)) {
 				fGroup = group;
 				fGroupName = group.getName();
 				break;
 			}
 		}
 		if (null == fGroup) {
 			fGroup = fProxy.getReviewGroupProxy().createReviewGroup(
 					TestUtils.FSharedFolder + File.separator + TestConstants.REVIEW_GROUP_TEST_NAME,
 					TestConstants.REVIEW_GROUP_TEST_NAME, TestConstants.REVIEW_GROUP_TEST_DESCRIPTION,
 					TestConstants.REVIEW_GROUP_TEST_ENTRY_CRITERIA, TestConstants.REVIEW_GROUP_TEST_AVAILABLE_PROJECTS,
 					TestConstants.REVIEW_GROUP_TEST_AVAILABLE_COMPONENTS, new String[0]);
 			r4eAssert.assertNotNull(fGroup);
 			r4eAssert.assertEquals(TestConstants.REVIEW_GROUP_TEST_NAME, fGroup.getReviewGroup().getName());
 			r4eAssert.assertEquals(new Path(TestUtils.FSharedFolder).toPortableString() + "/"
 					+ TestConstants.REVIEW_GROUP_TEST_NAME, fGroup.getReviewGroup().getFolder());
 			r4eAssert.assertEquals(TestConstants.REVIEW_GROUP_TEST_DESCRIPTION, fGroup.getReviewGroup()
 					.getDescription());
 			r4eAssert.assertEquals(TestConstants.REVIEW_GROUP_TEST_ENTRY_CRITERIA, fGroup.getReviewGroup()
 					.getDefaultEntryCriteria());
 			for (int i = 0; i < TestConstants.REVIEW_GROUP_TEST_AVAILABLE_PROJECTS.length; i++) {
 				r4eAssert.assertEquals(TestConstants.REVIEW_GROUP_TEST_AVAILABLE_PROJECTS[i], fGroup.getReviewGroup()
 						.getAvailableProjects()
 						.get(i));
 			}
 			for (int i = 0; i < TestConstants.REVIEW_GROUP_TEST_AVAILABLE_COMPONENTS.length; i++) {
 				r4eAssert.assertEquals(TestConstants.REVIEW_GROUP_TEST_AVAILABLE_COMPONENTS[i], fGroup.getReviewGroup()
 						.getAvailableComponents()
 						.get(i));
 			}
 			fGroupName = fGroup.getName();
 		}
 	}
 
 	/**
 	 * Method createReviews
 	 */
 	private void createReview() {
 
 		// Assert object
 		R4EAssert r4eAssert = new R4EAssert("createReviews");
 
 		//Update Review Group handle
 		for (IR4EUIModelElement elem : R4EUIModelController.getRootElement().getChildren()) {
 			if (fGroupName.equals(elem.getName())) {
 				fGroup = (R4EUIReviewGroup) elem;
 			}
 		}
 
 		r4eAssert.setTest("Open Review Group");
 		if (!fGroup.isOpen()) {
 			fProxy.getCommandProxy().openElement(fGroup);
 		}
 		r4eAssert.assertTrue(fGroup.isOpen());
 
 		r4eAssert.setTest("Create Review");
 		fReview = fProxy.getReviewProxy().createReview(fGroup, TestConstants.REVIEW_TEST_TYPE_INFORMAL,
				TestConstants.REVIEW_TEST_NAME_INF, TestConstants.REVIEW_TEST_DESCRIPTION,
 				TestConstants.REVIEW_TEST_DUE_DATE, TestConstants.REVIEW_TEST_PROJECT,
 				TestConstants.REVIEW_TEST_COMPONENTS, TestConstants.REVIEW_TEST_ENTRY_CRITERIA,
 				TestConstants.REVIEW_TEST_OBJECTIVES, TestConstants.REVIEW_TEST_REFERENCE_MATERIALS);
 		r4eAssert.assertNotNull(fReview);
 		r4eAssert.assertNotNull(fReview.getParticipantContainer());
 		r4eAssert.assertNotNull(fReview.getAnomalyContainer());
 		r4eAssert.assertEquals(TestConstants.REVIEW_TEST_TYPE_INFORMAL, fReview.getReview().getType());
		r4eAssert.assertEquals(TestConstants.REVIEW_TEST_NAME_INF, fReview.getReview().getName());
 		r4eAssert.assertEquals(TestConstants.REVIEW_TEST_DESCRIPTION, fReview.getReview().getExtraNotes());
 		r4eAssert.assertEquals(TestConstants.REVIEW_TEST_DUE_DATE, fReview.getReview().getDueDate());
 		r4eAssert.assertEquals(TestConstants.REVIEW_TEST_PROJECT, fReview.getReview().getProject());
 		for (int i = 0; i < TestConstants.REVIEW_TEST_COMPONENTS.length; i++) {
 			r4eAssert.assertEquals(TestConstants.REVIEW_TEST_COMPONENTS[i], fReview.getReview().getComponents().get(i));
 		}
 		r4eAssert.assertEquals(TestConstants.REVIEW_TEST_ENTRY_CRITERIA, fReview.getReview().getEntryCriteria());
 		r4eAssert.assertEquals(TestConstants.REVIEW_TEST_OBJECTIVES, fReview.getReview().getObjectives());
 		r4eAssert.assertEquals(TestConstants.REVIEW_TEST_REFERENCE_MATERIALS, fReview.getReview()
 				.getReferenceMaterial());
 		r4eAssert.assertTrue(fReview.isOpen());
 	}
 
 	/**
 	 * Method createReviewItems
 	 */
 	private void createReviewItems() throws CoreException {
 
 		// Assert object
 		R4EAssert r4eAssert = new R4EAssert("createReviewItems");
 
 		r4eAssert.setTest("Create Commit Item");
 		fItem = fProxy.getItemProxy().createCommitItem(TestUtils.FJavaIProject, 0);
 		//close and re-open, so the validation takes de-serialized information
 		String itemName = fItem.getName();
 		fProxy.getCommandProxy().closeElement(fReview);
 		fProxy.getCommandProxy().openElement(fReview);
 		for (IR4EUIModelElement elem : fReview.getChildren()) {
 			if (elem.getName().equals(itemName)) {
 				fItem = (R4EUIReviewItem) elem;
 			}
 		}
 
 		//Now validate
 		r4eAssert.assertNotNull(fItem);
 		r4eAssert.assertEquals(R4EUIModelController.getReviewer(), fItem.getItem().getAddedById());
 		r4eAssert.assertEquals("The.committer@some.com", fItem.getItem().getAuthorRep());
		r4eAssert.assertEquals("second Java Commit", fItem.getItem().getDescription());
 		r4eAssert.assertEquals(4, fItem.getChildren().length);
 		for (int i = 0; i < fItem.getChildren().length; i++) {
 			if (((R4EUIFileContext) fItem.getChildren()[i]).getName().equals(TestUtils.JAVA_FILE1_PROJ_NAME)) {
 				fAnomalyFileIndex = i; //Used later to add anomalies
 				r4eAssert.assertEquals(TestUtils.JAVA_FILE1_PROJ_NAME, fItem.getItem()
 						.getFileContextList()
 						.get(i)
 						.getBase()
 						.getName());
 				r4eAssert.assertEquals(TestUtils.JAVA_FILE1_PROJ_NAME, fItem.getItem()
 						.getFileContextList()
 						.get(i)
 						.getTarget()
 						.getName());
 				r4eAssert.assertEquals(606, ((R4ETextPosition) fItem.getItem()
 						.getFileContextList()
 						.get(i)
 						.getDeltas()
 						.get(0)
 						.getTarget()
 						.getLocation()).getStartPosition());
 				r4eAssert.assertEquals(25, ((R4ETextPosition) fItem.getItem()
 						.getFileContextList()
 						.get(i)
 						.getDeltas()
 						.get(0)
 						.getTarget()
 						.getLocation()).getLength());
 				r4eAssert.assertEquals(665, ((R4ETextPosition) fItem.getItem()
 						.getFileContextList()
 						.get(i)
 						.getDeltas()
 						.get(1)
 						.getTarget()
 						.getLocation()).getStartPosition());
 				r4eAssert.assertEquals(63, ((R4ETextPosition) fItem.getItem()
 						.getFileContextList()
 						.get(i)
 						.getDeltas()
 						.get(1)
 						.getTarget()
 						.getLocation()).getLength());
 				r4eAssert.assertEquals(733, ((R4ETextPosition) fItem.getItem()
 						.getFileContextList()
 						.get(i)
 						.getDeltas()
 						.get(2)
 						.getTarget()
 						.getLocation()).getStartPosition());
 				r4eAssert.assertEquals(61, ((R4ETextPosition) fItem.getItem()
 						.getFileContextList()
 						.get(i)
 						.getDeltas()
 						.get(2)
 						.getTarget()
 						.getLocation()).getLength());
 				r4eAssert.assertTrue(fProxy.getCommandProxy().verifyAnnotations(
 						((R4EUIFileContext) fItem.getChildren()[i]).getContentsContainerElement().getChildren(), true,
 						R4EUIConstants.DELTA_ANNOTATION_ID));
 			} else if (((R4EUIFileContext) fItem.getChildren()[i]).getName().equals(TestUtils.JAVA_FILE4_PROJ_NAME)) {
 				r4eAssert.assertNull(fItem.getItem().getFileContextList().get(i).getBase());
 				r4eAssert.assertEquals(TestUtils.JAVA_FILE4_PROJ_NAME, fItem.getItem()
 						.getFileContextList()
 						.get(i)
 						.getTarget()
 						.getName());
 			} else if (((R4EUIFileContext) fItem.getChildren()[i]).getName().equals(TestUtils.JAVA_FILE3_PROJ_NAME)) {
 				r4eAssert.assertNull(fItem.getItem().getFileContextList().get(i).getBase());
 				r4eAssert.assertEquals(TestUtils.JAVA_FILE3_PROJ_NAME, fItem.getItem()
 						.getFileContextList()
 						.get(i)
 						.getTarget()
 						.getName());
 			} else if (((R4EUIFileContext) fItem.getChildren()[i]).getName().equals(TestUtils.JAVA_FILE2_PROJ_NAME)) {
 				r4eAssert.assertEquals(TestUtils.JAVA_FILE2_PROJ_NAME, fItem.getItem()
 						.getFileContextList()
 						.get(i)
 						.getBase()
 						.getName());
 				r4eAssert.assertNull(fItem.getItem().getFileContextList().get(i).getTarget());
 			}
 		}
 	}
 
 	/**
 	 * Method createParticipants
 	 */
 	private void createParticipants() {
 
 		// Assert object
 		R4EAssert r4eAssert = new R4EAssert("createParticipants");
 
 		r4eAssert.setTest("Create Participant");
 
 		List<R4EParticipant> participants = new ArrayList<R4EParticipant>(1);
 		R4EParticipant participant = RModelFactory.eINSTANCE.createR4EParticipant();
 		participant.setId(TestConstants.PARTICIPANT_TEST_ID);
 		participant.setEmail(TestConstants.PARTICIPANT_TEST_EMAIL);
 		participants.add(participant);
 		fParticipant = fProxy.getParticipantProxy().createParticipant(fReview.getParticipantContainer(), participants);
 		r4eAssert.assertNotNull(fParticipant);
 		r4eAssert.assertEquals(TestConstants.PARTICIPANT_TEST_ID, fParticipant.getParticipant().getId());
 		r4eAssert.assertEquals(TestConstants.PARTICIPANT_TEST_EMAIL, fParticipant.getParticipant().getEmail());
 		r4eAssert.assertEquals(R4EUserRole.REVIEWER, fParticipant.getParticipant().getRoles().get(0));
 	}
 
 	/**
 	 * Method createCompareEditorAnomalies
 	 */
 	private void createCompareEditorAnomalies() {
 
 		// Assert object
 		R4EAssert r4eAssert = new R4EAssert("createCompareEditorAnomalies");
 
 		r4eAssert.setTest("Create Compare Editor Anomaly");
 		fCompareEditorAnomaly = fProxy.getAnomalyProxy().createCompareEditorAnomaly(
 				fItem.getFileContexts().get(fAnomalyFileIndex), 20, 50,
 				TestConstants.COMPARE_EDITOR_ANOMALY_TEST_TITLE, TestConstants.COMPARE_EDITOR_ANOMALY_TEST_DESCRIPTION,
 				TestConstants.ANOMALY_TEST_CLASS_ERRONEOUS, TestConstants.ANOMALY_TEST_RANK_MINOR,
 				TestConstants.ANOMALY_TEST_DUE_DATE, TestConstants.PARTICIPANT_ASSIGN_TO, null);
 		r4eAssert.assertNotNull(fCompareEditorAnomaly);
 		r4eAssert.assertEquals(TestConstants.COMPARE_EDITOR_ANOMALY_TEST_TITLE, fCompareEditorAnomaly.getAnomaly()
 				.getTitle());
 		r4eAssert.assertEquals(TestConstants.COMPARE_EDITOR_ANOMALY_TEST_DESCRIPTION,
 				fCompareEditorAnomaly.getAnomaly().getDescription());
 		r4eAssert.assertEquals(TestConstants.ANOMALY_TEST_CLASS_ERRONEOUS,
 				((R4ECommentType) fCompareEditorAnomaly.getAnomaly().getType()).getType());
 		r4eAssert.assertEquals(TestConstants.ANOMALY_TEST_RANK_MINOR, fCompareEditorAnomaly.getAnomaly().getRank());
 		r4eAssert.assertEquals(TestConstants.ANOMALY_TEST_DUE_DATE, fCompareEditorAnomaly.getAnomaly().getDueDate());
 		r4eAssert.assertEquals(TestConstants.PARTICIPANT_ASSIGN_TO, fCompareEditorAnomaly.getAnomaly()
 				.getAssignedTo()
 				.get(0));
 		r4eAssert.assertEquals(20, ((R4ETextPosition) ((R4ETextContent) fCompareEditorAnomaly.getAnomaly()
 				.getLocations()
 				.get(0)).getLocation()).getStartPosition());
 		r4eAssert.assertEquals(50, ((R4ETextPosition) ((R4ETextContent) fCompareEditorAnomaly.getAnomaly()
 				.getLocations()
 				.get(0)).getLocation()).getLength());
 		r4eAssert.assertTrue(fProxy.getCommandProxy().verifyAnnotation(fCompareEditorAnomaly, true,
 				R4EUIConstants.ANOMALY_OPEN_ANNOTATION_ID));
 	}
 
 	/**
 	 * Method commitNewReviewItemVersion
 	 */
 	private void commitNewReviewItemVersion() throws CoreException, IOException, URISyntaxException {
 
 		// Assert object
 		R4EAssert r4eAssert = new R4EAssert("commitNewReviewItemVersion");
 
 		r4eAssert.setTest("Commit New Review Item Version");
 		TestUtils.FJavaFile1 = TestUtils.changeContentOfFile(TestUtils.FJavaFile1, JAVA_FILE12_EXT_MOD_PATH);
 		TestUtils.commitFiles(TestUtils.FJavaIProject, TestUtils.FJavaRepository, "second Java Commit", true);
 
 		fItem2 = fProxy.getItemProxy().createCommitItem(TestUtils.FJavaIProject, 0);
 		//close and re-open, so the validation takes de-serialized information
 		fProxy.getCommandProxy().closeElement(fReview);
 		fProxy.getCommandProxy().openElement(fReview);
 
 		for (IR4EUIModelElement elem : fReview.getChildren()) {
 			if (elem.getName().equals(REVIEW_ITEM_COMMIT_1)) {
 				fItem = (R4EUIReviewItem) elem;
 			}
 			if (elem.getName().equals(REVIEW_ITEM_COMMIT_2)) {
 				fItem2 = (R4EUIReviewItem) elem;
 			}
 		}
 
 		//Now validate
 		r4eAssert.assertNotNull(fItem);
 		r4eAssert.assertEquals(R4EUIModelController.getReviewer(), fItem2.getItem().getAddedById());
 		r4eAssert.assertEquals("The.committer@some.com", fItem2.getItem().getAuthorRep());
 		r4eAssert.assertEquals("second Java Commit", fItem2.getItem().getDescription());
 		r4eAssert.assertEquals(2, fItem2.getChildren().length);
 		for (int i = 0; i < fItem2.getChildren().length; i++) {
 			if (((R4EUIFileContext) fItem2.getChildren()[i]).getName().equals(TestUtils.JAVA_FILE1_PROJ_NAME)) {
 				r4eAssert.assertEquals(TestUtils.JAVA_FILE1_PROJ_NAME, fItem2.getItem()
 						.getFileContextList()
 						.get(i)
 						.getBase()
 						.getName());
 				r4eAssert.assertEquals(TestUtils.JAVA_FILE1_PROJ_NAME, fItem2.getItem()
 						.getFileContextList()
 						.get(i)
 						.getTarget()
 						.getName());
 				r4eAssert.assertEquals(733, ((R4ETextPosition) fItem2.getItem()
 						.getFileContextList()
 						.get(i)
 						.getDeltas()
 						.get(0)
 						.getTarget()
 						.getLocation()).getStartPosition());
 				r4eAssert.assertEquals(31, ((R4ETextPosition) fItem2.getItem()
 						.getFileContextList()
 						.get(i)
 						.getDeltas()
 						.get(0)
 						.getTarget()
 						.getLocation()).getLength());
 			}
 		}
 
 	}
 
 	/**
 	 * Method compareReviewItemsVersions
 	 */
 	private void compareReviewItemsVersions() {
 		// Assert object
 		R4EAssert r4eAssert = new R4EAssert("compareReviewItemsVersions");
 
 		r4eAssert.setTest("Compare Review Items Versions");
 		fCompareEditorAnomaly = (R4EUIAnomalyBasic) fItem.getFileContexts()
 				.get(fAnomalyFileIndex)
 				.getAnomalyContainerElement()
 				.getChildren()[0];
 		fProxy.getItemProxy().openCompareReviewItems(fItem, fItem2, fCompareEditorAnomaly,
 				R4EUIConstants.ANOMALY_OPEN_ANNOTATION_ID);
 	}
 }
