 /*******************************************************************************
  * Copyright (c) 2012 Ericsson Research Canada
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * This class implements the main JUnit UI Test suite
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  ******************************************************************************/
 package org.eclipse.mylyn.reviews.r4e.ui.tests;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 import junit.textui.TestRunner;
 
 import org.eclipse.mylyn.reviews.r4e.ui.tests.feature.CloneAnomaliesCommentsTests;
 import org.eclipse.mylyn.reviews.r4e.ui.tests.feature.DuplicateAnomalyTests;
 import org.eclipse.mylyn.reviews.r4e.ui.tests.feature.PostponedAnomaliesTests;
 import org.eclipse.mylyn.reviews.r4e.ui.tests.feature.PreferencesTests;
 import org.eclipse.mylyn.reviews.r4e.ui.tests.feature.ReviewWithStrangeCharTest;
 import org.eclipse.mylyn.reviews.r4e.ui.tests.feature.UpgradeVersionTests;
 import org.eclipse.mylyn.reviews.r4e.ui.tests.feature.UserReviewedTests;
 import org.eclipse.mylyn.reviews.r4e.ui.tests.sanity.SanityBasicTests;
 import org.eclipse.mylyn.reviews.r4e.ui.tests.sanity.SanityInformalTests;
 import org.eclipse.mylyn.reviews.r4e.ui.tests.sanity.SanitySetupTests;
 
 /**
  * <!-- begin-user-doc --> A test suite for the '<em><b>R4E</b></em>' UI component. <!-- end-user-doc -->
  * 
  * @generated
  */
 public class R4EUIAllTests extends TestSuite {
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public static void main(String[] args) {
 		TestRunner.run(suite());
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public static Test suite() {
 
 		//Execute test cases
 		TestSuite suite = new R4EUIAllTests("R4E UI Tests"); //$NON-NLS-1$
 		suite.addTestSuite(SanitySetupTests.class);
 		suite.addTestSuite(SanityBasicTests.class);
 		suite.addTestSuite(SanityInformalTests.class);
 		suite.addTestSuite(PreferencesTests.class);
 		suite.addTestSuite(UserReviewedTests.class);
 		suite.addTestSuite(PostponedAnomaliesTests.class);
 		suite.addTestSuite(CloneAnomaliesCommentsTests.class);
 		suite.addTestSuite(DuplicateAnomalyTests.class);
 		suite.addTestSuite(ReviewWithStrangeCharTest.class);
 		suite.addTestSuite(UpgradeVersionTests.class);
//		suite.addTestSuite(CompareReviewItemsTests.class);
 
 		//TODO Add test cases here
 		//suite.addTestSuite(ReviewGroupTests.class);
 		//suite.addTestSuite(ReviewTests.class);
 		//suite.addTestSuite(ReviewItemTests.class);
 		//suite.addTestSuite(FileContextTests.class);
 		//suite.addTestSuite(ContentsTests.class);
 		//suite.addTestSuite(AnomalyTests.class);
 		//suite.addTestSuite(CommentTests.class);
 		//suite.addTestSuite(ParticipantTests.class);
 		//suite.addTestSuite(RuleAreaTests.class);
 		//suite.addTestSuite(RuleViolationTests.class);
 		//suite.addTestSuite(RuleTests.class);
 		//suite.addTestSuite(PostponedElementsTests.class);
 
 		return new R4ETestSetup(suite);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public R4EUIAllTests(String name) {
 		super(name);
 	}
 
 } //R4eAllTests
