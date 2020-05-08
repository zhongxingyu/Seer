 /*******************************************************************************
  * Copyright (c) 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.mylyn.docs.intent.client.ui.test.unit.demo;
 
 import java.math.BigInteger;
 
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.emf.common.util.WrappedException;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.mylyn.docs.intent.client.ui.ide.builder.ToggleNatureAction;
 import org.eclipse.mylyn.docs.intent.client.ui.test.util.AbstractIntentUITest;
 import org.eclipse.mylyn.docs.intent.client.ui.test.util.WorkspaceUtils;
 import org.eclipse.mylyn.docs.intent.collab.common.IntentRepositoryManager;
 import org.eclipse.mylyn.docs.intent.collab.common.location.IntentLocations;
 import org.eclipse.mylyn.docs.intent.collab.repository.RepositoryConnectionException;
 import org.eclipse.mylyn.docs.intent.core.compiler.TraceabilityIndex;
 import org.eclipse.mylyn.docs.intent.core.compiler.TraceabilityIndexEntry;
 
 /**
  * Tests the Intent demo, part 1: navigation behavior.
  * 
  * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
  */
 public abstract class AbstractDemoTest extends AbstractIntentUITest {
 
 	protected static final String TEST_COMPILER_NO_ERROR_MSG = "The compiler failed to detect errors";
 
 	protected static final String TEST_COMPILER_INVALID_ERROR_MSG = "The compiler detected invalid errors";
 
 	protected static final String TEST_COMPILER_NO_INFO_MSG = "The compiler failed to detect infos";
 
 	protected static final String TEST_COMPILER_INVALID_INFO_MSG = "The compiler detected invalid infos";
 
 	protected static final String TEST_SYNCHRONIZER_NO_WARNING_MSG = "The synchronizer failed to detect errors";
 
 	protected static final String TEST_SYNCHRONIZER_INVALID_WARNING_MSG = "The synchronizer failed to detect errors";
 
 	private static final String DEMO_ZIP_LOCATION = "data/unit/demo/demo.zip";
 
 	private static final String BUNDLE_NAME = "org.eclipse.mylyn.docs.intent.client.ui.test";
 
 	private static final String INTENT_PROJECT_NAME = "org.eclipse.emf.compare.idoc";
 
 	private static final int TIME_TO_WAIT = 300;
 
 	private static final int RECENT_COMPILATION_DELAY = 60000;
 
 	private static final long TIME_OUT_DELAY = 10000;
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.client.ui.test.util.AbstractIntentUITest#setUp()
 	 */
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 
 		// Step 1 : import the demo projects
 		WorkspaceUtils.unzipAllProjects(BUNDLE_NAME, DEMO_ZIP_LOCATION, new NullProgressMonitor());
 
 		intentProject = ResourcesPlugin.getWorkspace().getRoot().getProject(INTENT_PROJECT_NAME);
 
 		// workaround hudson issue
 		if (waitForRepositoryAvailability()) {
 			ToggleNatureAction.toggleNature(intentProject);
 			waitForAllOperationsInUIThread();
 			ToggleNatureAction.toggleNature(intentProject);
 			waitForAllOperationsInUIThread();
 			assertFalse(waitForRepositoryAvailability());
 		}
 
 		// Step 2 : setting the intent repository
 		// and wait its complete initialization
 		setUpRepository(intentProject);
 		boolean repositoryInitialized = false;
 		long startTime = System.currentTimeMillis();
 		boolean timeOutDetected = false;
 		while (!repositoryInitialized && !timeOutDetected) {
 			try {
 				Resource resource = repositoryAdapter
 						.getResource(IntentLocations.TRACEABILITY_INFOS_INDEX_PATH);
 				// We ensure that the compiler did its work less that one minute ago
 				repositoryInitialized = resource != null
 						&& !resource.getContents().isEmpty()
 						&& isRecentTraceabilityIndex((TraceabilityIndex)resource.getContents().iterator()
 								.next());
 				timeOutDetected = System.currentTimeMillis() - startTime > TIME_OUT_DELAY;
 				Thread.sleep(TIME_TO_WAIT);
 			} catch (WrappedException e) {
 				// Try again
 			}
 		}
 
 		assertFalse("The Intent clients have not been launched although the project has been imported",
 				timeOutDetected);
 		registerRepositoryListener();
 	}
 
 	private boolean waitForRepositoryAvailability() throws RepositoryConnectionException, CoreException,
 			InterruptedException {
 		boolean timeOutDetected = false;
 		long startTime = System.currentTimeMillis();
		// while the project does not have the correct nature or is unaccessible, the repository is null
 		while (IntentRepositoryManager.INSTANCE.getRepository(INTENT_PROJECT_NAME) == null
 				&& !timeOutDetected) {
 			timeOutDetected = System.currentTimeMillis() - startTime > TIME_OUT_DELAY;
 			Thread.sleep(TIME_TO_WAIT);
 		}
 		return timeOutDetected;
 	}
 
 	/**
 	 * Indicates if the given traceability index is recent or is old.
 	 * 
 	 * @param traceabilityIndex
 	 *            the traceability index to test
 	 * @return true if the given traceability index has been compiled less than a minute ago, false otherwise
 	 */
 	private boolean isRecentTraceabilityIndex(TraceabilityIndex traceabilityIndex) {
 		if (traceabilityIndex.getEntries().size() > 0) {
 			final TraceabilityIndexEntry entry = traceabilityIndex.getEntries().iterator().next();
 			BigInteger compilationTime = entry.getCompilationTime();
 			return compilationTime.doubleValue() > (System.currentTimeMillis() - RECENT_COMPILATION_DELAY);
 		}
 		return false;
 	}
 
 }
