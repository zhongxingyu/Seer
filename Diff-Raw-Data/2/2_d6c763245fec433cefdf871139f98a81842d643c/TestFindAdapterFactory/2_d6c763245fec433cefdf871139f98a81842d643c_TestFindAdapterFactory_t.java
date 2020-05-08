 /*******************************************************************************
  * Copyright (c) 2006, 2007 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.tests.unit;
 
 import java.io.IOException;
 
 import junit.framework.AssertionFailedError;
 
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.compare.diff.metamodel.DiffFactory;
 import org.eclipse.emf.compare.diff.metamodel.DiffModel;
 import org.eclipse.emf.compare.tests.util.EMFCompareTestCase;
 import org.eclipse.emf.compare.util.AdapterUtils;
 import org.eclipse.emf.compare.util.ModelUtils;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 
 /**
  * Test the finding of the EMF adapterFactory.
  * 
  * @author Cedric Brun <a href="mailto:cedric.brun@obeo.fr">cedric.brun@obeo.fr</a>
  */
 public class TestFindAdapterFactory extends EMFCompareTestCase {
 	/**
 	 * Check the util finding the adapter factory.
 	 * 
 	 * @throws AssertionFailedError
 	 * 			Thrown if the adapterFactory hasn't been found.
 	 */
 	public void testFindAdapterFactory() throws AssertionFailedError {
		final String uri = "http://www.eclipse.org/emf/compare/diff/1.1"; //$NON-NLS-1$
 		final AdapterFactory factory = AdapterUtils.findAdapterFactory(uri);
 		assertNotNull(factory);
 	}
 	
 	/**
 	 * Find the adapter factory from an loaded file.
 	 * 
 	 * @throws AssertionFailedError
 	 * 			Thrown if the adapterFactory hasn't been found.
 	 * @throws IOException
 	 * 			If the file does not exist.
 	 */
 	public void testFindAdapterFactoryFromFile() throws AssertionFailedError, IOException {
 		final EObject model = ModelUtils.load(pluginFile("/data/result.diff"), new ResourceSetImpl()); //$NON-NLS-1$
 		final AdapterFactory factory = AdapterUtils.findAdapterFactory(model);
 		assertNotNull(factory);
 	}
 	
 	
 	/**
 	 * Find the adapter factory from an eobject.
 	 * 
 	 * @throws AssertionFailedError
 	 * 			Thrown if the adapterFactory hasn't been found.
 	 */
 	public void testFindAdapterFactoryFromEObject() throws AssertionFailedError {
 		final DiffModel model = DiffFactory.eINSTANCE.createDiffModel();
 		final AdapterFactory factory = AdapterUtils.findAdapterFactory(model);
 		assertNotNull(factory);
 	}
 }
