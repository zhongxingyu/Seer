 /**
  * <copyright> Copyright (c) 2008-2009 Jonas Helming, Maximilian Koegel. All rights reserved. This program and the
  * accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this
  * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html </copyright>
  */
 package org.eclipse.emf.emfstore.performance.test;
 
 import static org.junit.Assert.fail;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.emfstore.bowling.BowlingPackage;
 import org.eclipse.emf.emfstore.client.ESLocalProject;
 import org.eclipse.emf.emfstore.client.test.common.cases.ESTestWithLoggedInUser;
 import org.eclipse.emf.emfstore.client.test.common.util.ProjectUtil;
 import org.eclipse.emf.emfstore.client.util.ESVoidCallable;
 import org.eclipse.emf.emfstore.client.util.RunESCommand;
 import org.eclipse.emf.emfstore.internal.client.model.ESWorkspaceProviderImpl;
 import org.eclipse.emf.emfstore.internal.client.model.ProjectSpace;
 import org.eclipse.emf.emfstore.internal.common.model.util.ModelUtil;
 import org.eclipse.emf.emfstore.internal.modelmutator.api.ModelMutator;
 import org.eclipse.emf.emfstore.internal.modelmutator.api.ModelMutatorConfiguration;
 import org.eclipse.emf.emfstore.internal.modelmutator.api.ModelMutatorUtil;
 import org.eclipse.emf.emfstore.server.exceptions.ESException;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 /**
  * This TestCase tests all methods in the main {@link org.unicase.emfstore.EmfStore} interface.
  * 
  * @author Dmitry Litvinov
  */
 public class PerformanceTest extends ESTestWithLoggedInUser {
 	
 
 	@BeforeClass
 	public static void beforeClass() {
 		startEMFStore();
 	}
 
 	@AfterClass
 	public static void afterClass() {
 		stopEMFStore();
 	}
 
 	private final static String MODEL_KEY = "http://org/eclipse/example/bowling";
 	private static final int NUM_ITERATIONS = 10;
 	private final long seed = 1234567800;
 
 	private static MemoryMeter memoryMeter;
 
 	private long lastSeed = seed + 1;
 
 	private static final double ACCEPTED_VARIANCE = 1.5;
 	private double memAfterThreshold;
 	double[] times;
 	long[] memBefore, memDuring, memAfter;
 
 	/**
 	 * Start server and gain session id.
 	 * 
 	 * @throws ESException in case of failure
 	 * @throws IOException
 	 */
 	@Override
 	public void before() {
 		super.before();
		generateModels(getProjectSpace(), 100000);
 		System.out.println("MODEL SIZE: " + getLocalProject().getAllModelElements().size());
 		initMeasurments();
 	}
 
 	@Override
 	public void after() {
 		super.after();
 		memoryMeter.finish();
 	}
 
 	/**
 	 * Opens projects of different sizes, shares them with the server and then deletes them. r
 	 * 
 	 * @see org.unicase.emfstore.EmfStore#createProject(org.eclipse.emf.emfstore.server.model.SessionId, String, String,
 	 *      org.eclipse.emf.emfstore.server.model.versioning.LogMessage, Project)
 	 * @see org.unicase.emfstore.EmfStore#getProjectList(org.eclipse.emf.emfstore.server.model.SessionId)
 	 * @throws ESException in case of failure.
 	 * @throws IOException
 	 */
 	@Test
 	public void testShareProject() throws ESException, IOException {
 
 		initMeasurments();
 
 		for (int i = 0; i < NUM_ITERATIONS; i++) {
 			memoryMeter.startMeasurements();
 			memBefore[i] = usedMemory();
 			long time = System.currentTimeMillis();
 			
 			ProjectUtil.share(getUsersession(), getLocalProject());
 			
 			times[i] = (System.currentTimeMillis() - time) / 1000.0;
 
 			memAfter[i] = usedMemory();
 			memDuring[i] = memoryMeter.stopMeasurements();
 			ModelUtil.logInfo("share project - iteration #" + (i + 1) + ": time=" + times[i] + ", memory used before: "
 				+ memBefore[i] / 1024 / 1024 + "MB, during: " + memDuring[i] / 1024 / 1024 + "MB, after: "
 				+ memAfter[i] / 1024 / 1024 + "MB");
 
 			// if (i > 0 && memAfter[i] > memAfterThreshold * ACCEPTED_VARIANCE) {
 			// fail();
 			// }
 			memAfterThreshold = memAfter[i];
 
 			// Configuration.getClientBehavior().flushCommandStack();
 			System.out.println(usedMemory() / 1024 / 1024 + " MB");
 
 		} // for loop with iterations
 
 		ModelUtil.logInfo("times=" + Arrays.toString(times));
 	}
 
 	/**
 	 * Measures average time, spent for the checkout operation. Opens projects of different sizes, shares them with the
 	 * server, checkouts and then deletes them.
 	 * 
 	 * @see org.unicase.emfstore.EmfStore#createProject(org.eclipse.emf.emfstore.server.model.SessionId, String, String,
 	 *      org.eclipse.emf.emfstore.server.model.versioning.LogMessage, Project)
 	 * @see org.unicase.emfstore.EmfStore#getProjectList(org.eclipse.emf.emfstore.server.model.SessionId)
 	 * @throws ESException in case of failure.
 	 */
 	@Test
 	public void testCheckoutProject() throws ESException {
 
 		ProjectUtil.share(getUsersession(), getLocalProject());
 		
 		long memAfterThreshold = 0;
 		for (int i = 0; i < NUM_ITERATIONS; i++) {
 			memoryMeter.startMeasurements();
 			memBefore[i] = usedMemory();
 			long time = System.currentTimeMillis();
 
 			ProjectUtil.checkout(getLocalProject());
 			times[i] = (System.currentTimeMillis() - time) / 1000.0;
 			memAfter[i] = usedMemory();
 			memDuring[i] = memoryMeter.stopMeasurements();
 			ModelUtil.logInfo("checkout project " + getProjectSpace().getProjectName() + " iteration #" + (i + 1)
 				+ ": time=" + times[i] + ", memory used before: " + memBefore[i] / 1024 / 1024 + "MB, during: "
 				+ memDuring[i] / 1024 / 1024 + "MB, after: " + memAfter[i] / 1024 / 1024 + "MB");
 
 			if (i > 0 && memAfter[i] > memAfterThreshold * 1.2) {
 				fail("Memory consumption too high.");
 			}
 			memAfterThreshold = memAfter[i];
 		}
 
 		ModelUtil.logInfo("times=" + Arrays.toString(times));
 	}
 
 	/**
 	 * Measures average time, spent for the commit and update operations. Opens projects of different sizes, shares them
 	 * with the server and checks it out as two different projects. Then the test generates changes in one of the
 	 * projects, using the ModelMutator, commits them to the server, and updates the second project. The test performs
 	 * model change, commit and update NUM_ITERATIONS times and calculates times for commit and update operations
 	 * 
 	 * @see org.unicase.emfstore.EmfStore#createProject(org.eclipse.emf.emfstore.server.model.SessionId, String, String,
 	 *      org.eclipse.emf.emfstore.server.model.versioning.LogMessage, Project)
 	 * @see org.unicase.emfstore.EmfStore#getProjectList(org.eclipse.emf.emfstore.server.model.SessionId)
 	 * @throws ESException in case of failure.
 	 */
 	@Test
 	public void testCommitAndUpdateProject() throws ESException {
 
 		getLocalProject().shareProject(nullMonitor());
 	
 		final ESLocalProject checkout = ProjectUtil.checkout(getLocalProject());
 		
 		double[] modelChangeTimes = new double[NUM_ITERATIONS];
 		double[] commitTimes = new double[NUM_ITERATIONS];
 		double[] updateTimes = new double[NUM_ITERATIONS];
 		long[] memBeforeMut = new long[NUM_ITERATIONS];
 		long[] memDuringMut = new long[NUM_ITERATIONS];
 		long[] memAfterMut = new long[NUM_ITERATIONS];
 		long[] memDuringCommit = new long[NUM_ITERATIONS];
 		long[] memAfterCommit = new long[NUM_ITERATIONS];
 		long[] memDuringUpdate = new long[NUM_ITERATIONS];
 		long[] memAfterUpdate = new long[NUM_ITERATIONS];
 
 		for (int i = 0; i < NUM_ITERATIONS; i++) {
 			memoryMeter.startMeasurements();
 			memBeforeMut[i] = usedMemory();
 			long time = System.currentTimeMillis();
 			// TODO: Nr of changes
 			changeModel(getProjectSpace(), 100);
 			modelChangeTimes[i] = (System.currentTimeMillis() - time) / 1000.0;
 
 			memDuringMut[i] = memoryMeter.stopMeasurements();
 			memAfterMut[i] = usedMemory();
 			ModelUtil.logInfo("change model-  iteration #" + (i + 1) + ": time=" + modelChangeTimes[i]
 				+ " memory used before:" + memBeforeMut[i] / 1024 / 1024 + "MB, during: " + memDuringMut[i] / 1024
 				/ 1024 + "MB, after: " + memAfterMut[i] / 1024 / 1024 + "MB");
 
 			System.out.println("VERSION BEFORE commit:" + getLocalProject().getBaseVersion().getIdentifier());
 			time = System.currentTimeMillis();
 			memoryMeter.startMeasurements();
 			time = System.currentTimeMillis();
 
 			getLocalProject().commit(null, null, null);
 			commitTimes[i] = (System.currentTimeMillis() - time) / 1000.0;
 
 			memDuringCommit[i] = memoryMeter.stopMeasurements();
 			memAfterCommit[i] = usedMemory();
 			ModelUtil.logInfo("commit project - iteration #" + (i + 1) + ": time=" + commitTimes[i]
 				+ ", memory used before: " + memAfterMut[i] / 1024 / 1024 + "MB, during: " + memDuringCommit[i] / 1024
 				/ 1024 + "MB, after: " + memAfterCommit[i] / 1024 / 1024 + "MB");
 			if (i > 0 && memAfter[i] > memAfterThreshold * ACCEPTED_VARIANCE) {
 				fail();
 			}
 			memAfterThreshold = memAfter[i];
 
 			memoryMeter.startMeasurements();
 			time = System.currentTimeMillis();
 
 			checkout.update(new NullProgressMonitor());
 			updateTimes[i] = (System.currentTimeMillis() - time) / 1000.0;
 			// TODO: re-enable clean memory task
 			// CleanMemoryTask task = new CleanMemoryTask(ESWorkspaceProviderImpl.getInstance().getCurrentWorkspace()
 			// .getResourceSet());
 			// task.run();
 			memDuringUpdate[i] = memoryMeter.stopMeasurements();
 			memAfterUpdate[i] = usedMemory();
 			ModelUtil.logInfo("update project - iteration #" + (i + 1) + ": time=" + updateTimes[i]
 				+ ", memory used before: " + memAfterCommit[i] / 1024 / 1024 + "MB, during: " + memDuringUpdate[i]
 					/ 1024 / 1024 + "MB, after: " + memAfterUpdate[i] / 1024 / 1024 + "MB");
 
 			if (i > 0 && memAfter[i] > memAfterThreshold * ACCEPTED_VARIANCE) {
 				fail();
 			}
 			memAfterThreshold = memAfter[i];
 
 		}
 
 		ModelUtil.logInfo("Mutate model - average=" + Calculate.average(modelChangeTimes) + ", min="
 			+ Calculate.min(modelChangeTimes) + ", max=" + Calculate.max(modelChangeTimes) + ", mean="
 			+ Calculate.mean(modelChangeTimes));
 
 	}
 
 	private void initMeasurments() {
 		memoryMeter = new MemoryMeter();
 		memoryMeter.start();
 		times = new double[NUM_ITERATIONS];
 		memBefore = new long[NUM_ITERATIONS];
 		memDuring = new long[NUM_ITERATIONS];
 		memAfter = new long[NUM_ITERATIONS];
 	}
 
 	public static long usedMemory() {
 		Runtime.getRuntime().gc();
 		return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
 	}
 	
 	private static IProgressMonitor nullMonitor() {
 		return new NullProgressMonitor();
 	}
 	
 	public void generateModels(final ProjectSpace projectSpace, int numberOfModleElements) {
 		lastSeed = lastSeed == seed ? seed + 1 : seed;
 		final ModelMutatorConfiguration mmc = new ModelMutatorConfiguration(ModelMutatorUtil.getEPackage(MODEL_KEY),
 			projectSpace.getProject(), lastSeed);
 		mmc.setMaxDeleteCount(1);
 		mmc.setUseEcoreUtilDelete(false);
 		mmc.setMinObjectsCount(numberOfModleElements);
 		mmc.setEditingDomain(ESWorkspaceProviderImpl.getInstance().getInternalWorkspace().getEditingDomain());
 		Collection<EStructuralFeature> features = new ArrayList<EStructuralFeature>();
 		features.add(org.eclipse.emf.emfstore.internal.common.model.ModelPackage.eINSTANCE.getProject_CutElements());
 		mmc.seteStructuralFeaturesToIgnore(features);
 		
 		RunESCommand.run(new ESVoidCallable() {
 			@Override
 			public void run() {
 				ModelMutator.generateModel(mmc);
 			}
 		});
 		
 		System.out.println("Number of changes: " + projectSpace.getOperations().size());
 	}
 
 	public void changeModel(final ProjectSpace prjSpace, final int nrOfChanges) {
 		lastSeed = lastSeed == seed ? seed + 1 : seed;
 		final ModelMutatorConfiguration mmc = new ModelMutatorConfiguration(ModelMutatorUtil.getEPackage(MODEL_KEY),
 			prjSpace.getProject(), lastSeed);
 		mmc.setMaxDeleteCount(1);
 		mmc.setUseEcoreUtilDelete(false);
 		mmc.setMinObjectsCount(1);
 		mmc.setEditingDomain(ESWorkspaceProviderImpl.getInstance().getInternalWorkspace().getEditingDomain());
 		Collection<EStructuralFeature> features = new ArrayList<EStructuralFeature>();
 		features.add(org.eclipse.emf.emfstore.internal.common.model.ModelPackage.eINSTANCE.getProject_CutElements());
 		mmc.seteStructuralFeaturesToIgnore(features);
 		List<EPackage> packages = new ArrayList<EPackage>();
 		packages.add(BowlingPackage.eINSTANCE);
 		mmc.setModelPackages(packages);
 		
 		long time = System.currentTimeMillis();
 		RunESCommand.run(new ESVoidCallable() {			
 			@Override
 			public void run() {
 				mmc.setMinObjectsCount(nrOfChanges);
 				ModelMutator.changeModel(mmc);
 			}
 		});
 		System.out.println("Changed model: " + (System.currentTimeMillis() - time) / 1000.0 + "sec");
 		System.out.println("Number of changes: " + prjSpace.getOperations().size());
 	}
 }
