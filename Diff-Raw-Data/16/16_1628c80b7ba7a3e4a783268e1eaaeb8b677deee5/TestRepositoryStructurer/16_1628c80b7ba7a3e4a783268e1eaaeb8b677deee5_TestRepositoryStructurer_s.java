 /*******************************************************************************
  * Copyright (c) 2010, 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.mylyn.docs.intent.collab.test.structurer;
 
 import com.google.common.collect.Sets;
 
 import java.util.Collection;
 
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.ReadOnlyException;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryStructurer;
 import org.eclipse.mylyn.docs.intent.collab.test.model.TestPackage.AbstractTestClass;
 import org.eclipse.mylyn.docs.intent.collab.test.model.TestPackage.TestClass1;
 import org.eclipse.mylyn.docs.intent.collab.test.model.TestPackage.TestIndex;
 import org.eclipse.mylyn.docs.intent.collab.test.model.TestPackage.TestIndexEntry;
 import org.eclipse.mylyn.docs.intent.collab.test.settings.TestCollabSettings;
 
 /**
  * Structures the TestRepository.
  * <p>
  * The followed splitting strategy is the following :
  * <ul>
  * <li>All the SampleClass1 classes should be placed in the TEST_TESTCLASS1_FOLDER within a specific resource</li>
  * <li>All the SampleClass2 classes should be placed in the TEST_TESTCLASS2_FOLDER within a specific resource</li>
  * </ul>
  * </p>
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public class TestRepositoryStructurer implements RepositoryStructurer {
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryStructurer#structure(org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter)
 	 */
 	public Collection<String> structure(RepositoryAdapter repositoryAdapter) throws ReadOnlyException {
		Collection<String> modifiedResources = Sets.newLinkedHashSet();
 		// We first get the Test Index
 		Resource indexResource = repositoryAdapter.getResource(TestCollabSettings.TEST_INDEX);
 		TestIndex testIndex = (TestIndex)indexResource.getContents().get(0);
 
 		// For each entry of the index
 		for (TestIndexEntry entry : testIndex.getEntries()) {
 			// We structure (i.e place in the correct resource) the reference element
			modifiedResources.addAll(placeCorrectly(repositoryAdapter, entry));
 		}
		return modifiedResources;
 	}
 
 	/**
 	 * Place the referenced element of the given indexEntry in the proper resource.
 	 * 
 	 * @param repositoryAdapter
 	 *            the RepositoryAdapter to use for restructuring the repository
 	 * @param indexEntry
 	 *            the indexEntry to consider
 	 * @return
 	 */
	private Collection<? extends String> placeCorrectly(RepositoryAdapter repositoryAdapter,
			TestIndexEntry indexEntry) {
		Collection<String> modifiedResources = Sets.newLinkedHashSet();
 		// We get the referenceElement
 		AbstractTestClass testClass = indexEntry.getReferencedElement();
 
 		// If this testClass not properly stored
 		if (!isProperlyStored(testClass)) {
 			// We create a new resource following the defined splitting strategy
 			String resourcePath = null;
 			if (testClass instanceof TestClass1) {
 				resourcePath = TestCollabSettings.TEST_TESTCLASS1_FOLDER;
 			} else {
 				resourcePath = TestCollabSettings.TEST_TESTCLASS2_FOLDER;
 			}
 			resourcePath += testClass.getName();
 			Resource resource;
 			try {
 				resource = repositoryAdapter.getOrCreateResource(resourcePath);
 				resource.getContents().add(testClass);
				modifiedResources.add(resourcePath);
 			} catch (ReadOnlyException e) {
 				// As we are in a testCase, we supposed the context has been openned as a save context
 			}
 
 		}
		return modifiedResources;
 	}
 
 	/**
 	 * Indicates if the given testClass is properly stored (i.e in stored in its own resource).
 	 * 
 	 * @param testClass
 	 *            the testClass to inspect
 	 * @return true if the given testClass is properly stored, false otherwise
 	 */
 	private boolean isProperlyStored(AbstractTestClass testClass) {
 		// We simply test if the associated resource is null
 		return testClass.eResource() != null;
 	}
 
 }
