 /**
  * Copyright (c) 2012 itemis AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Mark Broerkens - initial API and implementation
  * 
  */
 package org.eclipse.rmf.reqif10.tests.util;
 
import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.xml.transform.Source;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.validation.Schema;
 import javax.xml.validation.SchemaFactory;
 import javax.xml.validation.Validator;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.xmi.XMLResource;
 import org.eclipse.emf.ecore.xml.namespace.XMLNamespacePackage;
 import org.eclipse.emf.ecore.xml.type.XMLTypePackage;
 import org.eclipse.rmf.reqif10.ReqIF;
 import org.eclipse.rmf.reqif10.ReqIF10Package;
 import org.eclipse.rmf.reqif10.datatypes.DatatypesPackage;
 import org.eclipse.rmf.reqif10.xhtml.XhtmlPackage;
 import org.eclipse.rmf.serialization.ReqIFResourceFactoryImpl;
 import org.eclipse.rmf.serialization.ReqIFResourceSetImpl;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 @SuppressWarnings("nls")
 public class AbstractTestCase {
 	private static final String WORKING_DIRECTORY = "work";
 	static Map<String, Object> backupRegistry = null;
 
 	@BeforeClass
 	public static void setupOnce() throws Exception {
 		// globally register packages (global registry required since the generated EMF code
 		// doesn't handle local registries during handling of xsd any types)
 		// backup the registry
 		backupRegistry = new HashMap<String, Object>();
 		backupRegistry.putAll(EPackage.Registry.INSTANCE);
 		System.out.println("BeforeClass: Initial package registry: " + EPackage.Registry.INSTANCE.keySet());
 		EPackage.Registry.INSTANCE.clear();
 		// put doesn't overwrite existing entries. We have to remove them before setting a new value
 		EPackage.Registry.INSTANCE.remove(ReqIF10Package.eNS_URI);
 		EPackage.Registry.INSTANCE.put(ReqIF10Package.eNS_URI, ReqIF10Package.eINSTANCE);
 		EPackage.Registry.INSTANCE.remove(XhtmlPackage.eNS_URI);
 		EPackage.Registry.INSTANCE.put(XhtmlPackage.eNS_URI, XhtmlPackage.eINSTANCE);
 		EPackage.Registry.INSTANCE.remove(DatatypesPackage.eNS_URI);
 		EPackage.Registry.INSTANCE.put(DatatypesPackage.eNS_URI, DatatypesPackage.eINSTANCE);
 		EPackage.Registry.INSTANCE.remove(XMLNamespacePackage.eNS_URI);
 		EPackage.Registry.INSTANCE.put(XMLNamespacePackage.eNS_URI, XMLNamespacePackage.eINSTANCE);
 		// TODO: me might be able to live without the last package
 		EPackage.Registry.INSTANCE.remove(XMLTypePackage.eNS_URI);
 		EPackage.Registry.INSTANCE.put(XMLTypePackage.eNS_URI, XMLTypePackage.eINSTANCE);
 		System.out.println("BeforeClass: reset to: " + EPackage.Registry.INSTANCE.keySet());
 	}
 
 	@AfterClass
 	public static void tearDownOnce() throws Exception {
 		if (null != backupRegistry) {
 			EPackage.Registry.INSTANCE.clear();
 			EPackage.Registry.INSTANCE.putAll(backupRegistry);
 		}
 		System.out.println("AfterClass: reset to: " + EPackage.Registry.INSTANCE.keySet());
 	}
 
 	@Test
 	public void testSchemaCompliance() throws Exception {
 		validateAgainstSchema(getReqIFFileName());
 	}
 
 	protected static String getReqIFFileName() {
 		return getWorkingDirectoryFileName() + IPath.SEPARATOR + "default.reqif";
 	}
 
 	protected static String getWorkingDirectoryFileName() {
 		return WORKING_DIRECTORY;
 
 	}
 
 	protected void validateAgainstSchema(String filename) throws Exception {
		File schemaFolder = new File("schema");
 
		if (schemaFolder.exists() && schemaFolder.isDirectory()) {
 
			StreamSource[] schemaDocuments = new StreamSource[] { new StreamSource("schema/reqif.xsd") };
			// StreamSource[] schemaDocuments = new StreamSource[]{new
			// StreamSource("http://www.omg.org/spec/ReqIF/20110401/ReqIF.xsd")};
			Source instanceDocument = new StreamSource(filename);

			SchemaFactory sf = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
			Schema s = sf.newSchema(schemaDocuments);
			Validator v = s.newValidator();
			v.validate(instanceDocument);
		} else {
			System.err.println("Could not find schema folder. Schema validation is turned off!!! ");
		}
 	}
 
 	protected static void saveReqIFFile(ReqIF reqif, String fileName) throws IOException {
 		ReqIFResourceSetImpl resourceSet = getReqIFResourceSet();
 
 		URI emfURI = createEMFURI(fileName);
 		Resource resource = resourceSet.createResource(emfURI);
 
 		resource.getContents().add(reqif);
 		resource.save(null);
 	}
 
 	protected static ReqIF loadReqIFFile(String fileName) throws IOException {
 		ReqIFResourceSetImpl resourceSet = getReqIFResourceSet();
 
 		URI emfURI = createEMFURI(fileName);
 		XMLResource resource = (XMLResource) resourceSet.createResource(emfURI);
 
 		resource.load(null);
 
 		EList<EObject> rootObjects = resource.getContents();
 
 		if (rootObjects.isEmpty()) {
 			return null;
 		} else {
 			return (ReqIF) rootObjects.get(0);
 		}
 	}
 
 	private static ReqIFResourceSetImpl getReqIFResourceSet() {
 		ReqIFResourceSetImpl resourceSet = new ReqIFResourceSetImpl();
 
 		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("reqif", new ReqIFResourceFactoryImpl());
 		return resourceSet;
 	}
 
 	private static URI createEMFURI(String fileName) {
 		return URI.createURI(fileName, true);
 	}
 
 }
