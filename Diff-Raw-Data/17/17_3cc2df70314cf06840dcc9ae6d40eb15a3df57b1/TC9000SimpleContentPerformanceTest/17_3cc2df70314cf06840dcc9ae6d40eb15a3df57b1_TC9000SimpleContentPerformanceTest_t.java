 /**
  * Copyright (c) 2012 itemis AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Mark Brrkens - initial API and implementation
  * 
  */
 package org.eclipse.rmf.reqif10.tests.domain;
 
 import java.io.File;
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.util.Date;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.xmi.XMLResource;
 import org.eclipse.rmf.reqif10.ReqIF;
 import org.eclipse.rmf.reqif10.tests.TC9000ModelBuilder;
 import org.eclipse.rmf.serialization.ReqIFResourceFactoryImpl;
 import org.eclipse.rmf.serialization.ReqIFResourceSetImpl;
 import org.junit.Test;
 
 public class TC9000SimpleContentPerformanceTest {
 	
 	private static final String WORKING_DIRECTORY = "work";
 	
 	@Test
 	public void testSpecObject() throws Exception {
 		
		for (int i=1; i<10000 ; i=i*2) {
 			String fileName = "TC9000_SimpleContent_PerformanceTests_SpecObjects_" + i + ".reqif";
 			ReqIF reqif = new TC9000ModelBuilder(new Date(), "ID_" + fileName, fileName, i).getReqIF(); 
 		
 			saveReqifFile(reqif, fileName);
 			doTest(fileName);
 		}
 
 	}
 	
 	private void doTest(String filename) throws Exception {
 
 		System.out.println("Loading '" + filename + "'...");
 		long totaltime = 0;
 
 		for (int i = 0; i < 3; i++) {
 			long start = System.currentTimeMillis();
 			loadReqifFile(filename);
 			long end = System.currentTimeMillis();
 			totaltime += end - start;
 		}
 
 		double size = (new File(WORKING_DIRECTORY + IPath.SEPARATOR + filename).length()) / 1024;
 		double avgtime = totaltime / 3;
 		double timeperkb = new BigDecimal(avgtime / size).setScale(3,
 				BigDecimal.ROUND_UP).doubleValue();
 		
 		System.out.println("Avg. time taken for '" + filename + "' with size "
 				+ size + "KB: " + avgtime/1000 + "s");
 		System.out.println("Time per KB: " + timeperkb + "ms");
 	}
 	
 	
 	public static void saveReqifFile(ReqIF reqif, String fileName) throws IOException {
 		URI emfURI = createEMFURI(fileName);
 		ReqIFResourceFactoryImpl resourceFactory = new ReqIFResourceFactoryImpl();
 		ReqIFResourceSetImpl resourceSet = new ReqIFResourceSetImpl(); 
 		Resource resource = resourceFactory.createResource(emfURI); 
 		resourceSet.getResources().add(resource);
 		resource.getContents().add(reqif);
 		resource.save(null);
 		resourceSet = null;
 	}
 	
 	public static ReqIF loadReqifFile(String fileName) throws IOException {
 		
 		URI emfURI = createEMFURI(fileName);
 		ReqIFResourceFactoryImpl resourceFactory = new ReqIFResourceFactoryImpl();
 		XMLResource resource = (XMLResource) resourceFactory.createResource(emfURI);
 
 		resource.load(null);
 		
 		ReqIFResourceSetImpl resourceSet = new ReqIFResourceSetImpl(); 
 		resourceSet.getResources().add(resource);
 
         EList<EObject> rootObjects = resource.getContents();
         System.out.println(rootObjects);
         if (rootObjects.isEmpty()) {
         	return null;
         } else {
         	return (ReqIF)rootObjects.get(0);
         }
 	}
 	
 	private static URI createEMFURI(String fileName) {
 		return URI.createURI(WORKING_DIRECTORY + IPath.SEPARATOR + fileName, true);
 	}
 
 }
