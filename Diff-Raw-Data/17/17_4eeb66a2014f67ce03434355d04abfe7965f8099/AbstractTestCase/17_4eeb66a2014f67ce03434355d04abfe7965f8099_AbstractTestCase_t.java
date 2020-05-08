 /**
  * Copyright (c) 2013 itemis AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *   itemis AG - initial API and implementation
  */
 package org.eclipse.rmf.tests.serialization.util;
 
 import static org.junit.Assert.assertSame;
 
 import java.io.BufferedInputStream;
 import java.io.InputStream;
 import java.util.Map;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.URIConverter;
 import org.eclipse.emf.ecore.resource.impl.ResourceFactoryImpl;
 import org.eclipse.emf.ecore.xmi.XMLResource;
 import org.eclipse.rmf.serialization.XMLPersistenceMappingResourceSetImpl;
 import org.junit.Before;
 
 @SuppressWarnings("nls")
 public abstract class AbstractTestCase {
 
 	@Before
 	public void setUp() throws Exception {
 
 	}
 
 	protected EObject loadInputFile(String inputFileName, ResourceFactoryImpl resourceFactory, Map<?, ?> options) throws Exception {
 		URI emfURI = URI.createURI("resources/input/" + inputFileName, true);
 		XMLResource resource = (XMLResource) resourceFactory.createResource(emfURI);
 		resource.load(options);
 
 		ResourceSet resourceSet = new XMLPersistenceMappingResourceSetImpl();
 		resourceSet.getResources().add(resource);
 
 		assertSame(0, resource.getErrors().size());
 
 		return resource.getContents().get(0);
 	}
 
 	protected String loadFileAsString(InputStream inputStream) throws Exception {
 		inputStream = new BufferedInputStream(inputStream);
 		try {
 			byte[] buffer = new byte[1024];
 			int bufferLength;
 			StringBuilder content = new StringBuilder();
 			while ((bufferLength = inputStream.read(buffer)) > -1) {
 				content.append(new String(buffer, 0, bufferLength));
 			}
 			return content.toString();
 		} finally {
 			inputStream.close();
 		}
 	}
 
 	protected String loadWorkingFileAsString(String fileName) throws Exception {
		URI emfURI = URI.createURI("working-dir/" + fileName, true);
 		return loadFileAsString(URIConverter.INSTANCE.createInputStream(emfURI));
 	}
 
 	protected String loadInputFileAsString(String fileName) throws Exception {
 		URI emfURI = URI.createURI("resources/input" + fileName, true);
 		return loadFileAsString(URIConverter.INSTANCE.createInputStream(emfURI));
 	}
 
 	protected void saveWorkingFile(String fileName, EObject modelRoot, ResourceFactoryImpl resourceFactory, Map<?, ?> options) throws Exception {
		URI emfURI = URI.createURI("working-dir/" + fileName, true);
 		XMLResource resource = (XMLResource) resourceFactory.createResource(emfURI);
 		resource.getContents().add(modelRoot);
 		resource.save(options);
 
 		assertSame(0, resource.getErrors().size());
 	}
 
 }
