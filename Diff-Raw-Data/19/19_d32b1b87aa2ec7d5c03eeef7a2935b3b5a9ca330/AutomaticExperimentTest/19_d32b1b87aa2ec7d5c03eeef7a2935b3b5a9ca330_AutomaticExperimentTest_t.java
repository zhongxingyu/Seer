 package org.eclipse.stem.tests.automaticexperiment;
 
 /*******************************************************************************
  * Copyright (c) 2008 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import junit.textui.TestRunner;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.stem.analysis.automaticexperiment.ErrorAnalysisAlgorithm;
 import org.eclipse.stem.analysis.automaticexperiment.ErrorAnalysisAlgorithmFactory;
 import org.eclipse.stem.core.STEMXMIResourceFactoryImpl;
 import org.eclipse.stem.core.common.tests.IdentifiableTest;
 
 import automaticexperiment.AutomaticExperiment;
 
 public class AutomaticExperimentTest 
 	extends IdentifiableTest {
 	
 	private final static String AUTOMATIC_EXPERIMENT_FILE = "D:/Development/Workspaces/STEM-runtime/IsrSIRflu/experiments/sample.automaticexperiment"; //$NON-NLS-1$
 
 	public static void main(String[] args) {
 		TestRunner.run(AutomaticExperimentTest.class);
 	}
 
 	public AutomaticExperimentTest(String name) {
 		super(name);
 	}
 
 	/**
 	 * Returns the fixture for this Experiment test case.
 	 * <!-- begin-user-doc
 	 * --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected AutomaticExperiment getFixture() {
 		return (AutomaticExperiment)fixture;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see junit.framework.TestCase#setUp()
 	 * @generated NOT
 	 */
 	@Override
 	protected void setUp() throws Exception {
 		setFixture(createFixture());
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see junit.framework.TestCase#tearDown()
 	 * @generated
 	 */
 	@Override
 	protected void tearDown() throws Exception {
 		setFixture(null);
 	}
 
 	/**
 	 * Tests the '{@link org.eclipse.stem.core.experiment.Experiment#updateScenario() <em>Update Scenario</em>}'
 	 * operation. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see org.eclipse.stem.core.experiment.Experiment#updateScenario()
 	 * @generated NOT
 	 */
 	public void testAutomaticExperiment() 
 	{
 		AutomaticExperiment automaticExperiment = getFixture();
 		assertNotNull(automaticExperiment);
 		String algorithmName = automaticExperiment.getErrorAnalysisAlgorithm();
 		ErrorAnalysisAlgorithm algorithm = 
 			ErrorAnalysisAlgorithmFactory.INSTANCE.createErrorAnalysisAlgorithm(algorithmName);
 		assertNotNull(algorithm);
		algorithm.init(automaticExperiment, algorithm);
 		algorithm.execute();
 	}
 
 	/**
 	 * @return an {@link AutomaticExperiment} for testing.
 	 */
 	public static AutomaticExperiment createFixture() {
 		return loadAutomaticExperiment(AUTOMATIC_EXPERIMENT_FILE);
 	}
 	
 	static AutomaticExperiment loadAutomaticExperiment(final String filename) {
 		final ResourceSet resourceSet = new ResourceSetImpl();
 		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", //$NON-NLS-1$
 				STEMXMIResourceFactoryImpl.INSTANCE);
 		resourceSet.getResourceFactoryRegistry().getProtocolToFactoryMap().put("platform", //$NON-NLS-1$
 				STEMXMIResourceFactoryImpl.INSTANCE);
 		final Resource resource = resourceSet.getResource(URI.createFileURI(filename), true);
 		try 
 		{			
 			resource.load(null);
 		} 
 		catch (FileNotFoundException e) 
 		{
 			e.printStackTrace();
 		} 
 		catch (IOException e) 
 		{
 			e.printStackTrace();
 		}
 		AutomaticExperiment toReturn = (AutomaticExperiment)resource.getContents().get(0);
 		return toReturn;
 	}
 }
