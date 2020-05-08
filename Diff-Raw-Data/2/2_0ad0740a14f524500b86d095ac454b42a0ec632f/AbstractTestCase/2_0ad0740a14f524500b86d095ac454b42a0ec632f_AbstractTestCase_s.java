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
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 import java.util.zip.ZipOutputStream;
 
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
 import org.eclipse.rmf.pror.presentation.headline.HeadlinePackage;
 import org.eclipse.rmf.pror.reqif10.configuration.ConfigurationPackage;
 import org.eclipse.rmf.reqif10.ReqIF;
 import org.eclipse.rmf.reqif10.ReqIF10Package;
 import org.eclipse.rmf.reqif10.datatypes.DatatypesPackage;
 import org.eclipse.rmf.reqif10.xhtml.XhtmlPackage;
 import org.eclipse.rmf.serialization.ReqIFResourceFactoryImpl;
 import org.eclipse.rmf.serialization.ReqIFResourceImpl;
 import org.eclipse.rmf.serialization.ReqIFResourceSetImpl;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 
 @SuppressWarnings("nls")
 public abstract class AbstractTestCase {
 	private static final String WORKING_DIRECTORY = "work";
 	static Map<String, Object> backupRegistry = null;
 
 	static final DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
 	static final DateFormat timeFormat = new SimpleDateFormat("HHmm");
 
 	@BeforeClass
 	public static void setupOnce() throws Exception {
 		// globally register packages (global registry required since the generated EMF code
 		// doesn't handle local registries during handling of xsd any types)
 		// backup the registry
 		backupRegistry = new HashMap<String, Object>();
 		backupRegistry.putAll(EPackage.Registry.INSTANCE);
 		System.out.println("BeforeClass: Initial package registry: " + EPackage.Registry.INSTANCE.keySet());
 		EPackage.Registry.INSTANCE.clear();
 		EPackage.Registry.INSTANCE.put(ReqIF10Package.eNS_URI, ReqIF10Package.eINSTANCE);
 		EPackage.Registry.INSTANCE.put(XhtmlPackage.eNS_URI, XhtmlPackage.eINSTANCE);
 		EPackage.Registry.INSTANCE.put(DatatypesPackage.eNS_URI, DatatypesPackage.eINSTANCE);
 		EPackage.Registry.INSTANCE.put(XMLNamespacePackage.eNS_URI, XMLNamespacePackage.eINSTANCE);
 		EPackage.Registry.INSTANCE.put(ConfigurationPackage.eNS_URI, ConfigurationPackage.eINSTANCE);
 		EPackage.Registry.INSTANCE.put(HeadlinePackage.eNS_URI, HeadlinePackage.eINSTANCE);
 
 		// TODO: me might be able to live without the last package
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
 
 	protected static String getWorkingDirectoryFileName() {
 		return WORKING_DIRECTORY;
 	}
 
 	protected static String getWorkingFileName(String fileName) {
 		return WORKING_DIRECTORY + IPath.SEPARATOR + fileName;
 	}
 
 	protected void validateAgainstSchema(String filename) throws Exception {
 		File schemaFolder = new File("schema");
 
 		if (schemaFolder.exists() && schemaFolder.isDirectory()) {
 
 			StreamSource[] schemaDocuments = new StreamSource[] { new StreamSource("schema/reqif.xsd") };
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
 
 	/**
 	 * Creates the file name of reference test data.
 	 * 
 	 * The name pattern as defined by the ReqIF Implementor Forum.
 	 * #TestCaseID#_E0000_S10_Reference_#yyyyMMdd#_#HHmm#
 	 * #NameOfHumanCreator#.<reqif/reqifz>
 	 * 
 	 *
 	 * @param testCaseId
 	 * @return
 	 */
 	protected static String getReferenceDataFileName(String testCaseId, boolean isArchive) {
 		return getFileName(testCaseId, 0, 10, "Reference", isArchive);
 	}
 
 	/**
 	 * Creates the file name of reference test data.
 	 * 
 	 * The name pattern as defined by the ReqIF Implementor Forum.
 	 * #TestCaseID#_E0001_S21_Reference_#yyyyMMdd#_#HHmm#
 	 * #NameOfHumanCreator#.<reqif/reqifz>
 	 * 
 	 *
 	 * @param testCaseId
 	 * @return
 	 */
 	protected static String getFirstExportFileName(String testCaseId, boolean isArchive) {
 		return getFileName(testCaseId, 1, 21, "EclipseRMF", isArchive);
 	}
 
 	/**
 	 * Creates the file name according to the ReqIF Implementor Forum naming conventions.
 	 * 
 	 * The name pattern as defined by the ReqIF Implementor Forum.
 	 * #TestCaseID#_E#NumberOfExports#_S#TestStep#_#Tool#_#yyyyMMdd#_#HHmm#_#NameOfHumanCreator#.#reqif/reqifz#
 	 * 
 	 *
 	 * @param testCaseId
 	 * @return
 	 */
 	private static String getFileName(String testCaseId, int numberOfExports, int testStep, String tool, boolean isArchive) {
 		Date now = new Date();
 		String dateString = dateFormat.format(now);
 		String timeString = timeFormat.format(now);
 		String creatorName = System.getProperty("user.name");
 		if (null == creatorName || "".equals(creatorName)) {
 			creatorName = "RMFUser";
 		}
 		StringBuffer stringBuffer = new StringBuffer();
 		stringBuffer.append(testCaseId);
 		stringBuffer.append("_");
 		stringBuffer.append("E");
		stringBuffer.append(String.format("%03d", numberOfExports));
 		stringBuffer.append("_");
 		stringBuffer.append("S");
 		stringBuffer.append(String.format("%02d", testStep));
 		stringBuffer.append("_");
 		stringBuffer.append(tool);
 		stringBuffer.append("_");
 		stringBuffer.append(dateString);
 		stringBuffer.append("_");
 		stringBuffer.append(timeString);
 		stringBuffer.append("_");
 		stringBuffer.append(creatorName);
 		stringBuffer.append(".");
 		if (isArchive) {
 			stringBuffer.append("reqifz");
 		} else {
 			stringBuffer.append("reqif");
 		}
 		return stringBuffer.toString();
 	}
 
 	public static List<ReqIF> loadReqIFFromZip(String zipSourceFileName) throws IOException {
 		ZipFile zipSourceFile = new ZipFile(zipSourceFileName);
 		List<ReqIF> reqIFs = new ArrayList<ReqIF>();
 		Enumeration<? extends ZipEntry> zipFileEntries = zipSourceFile.entries();
 		ReqIFResourceSetImpl resourceSet = getReqIFResourceSet();
 
 		while (zipFileEntries.hasMoreElements()) {
 			ZipEntry entry = zipFileEntries.nextElement();
 
 			if (entry.isDirectory() || !entry.getName().endsWith(".reqif")) {
 				continue;
 			}
 			InputStream zipEntryInputStream;
 			zipEntryInputStream = zipSourceFile.getInputStream(entry);
 
 			Resource resource = new ReqIFResourceImpl();
 			resourceSet.getResources().add(resource);
 
 			resource.load(zipEntryInputStream, null);
 			List<EObject> rootObjects = resource.getContents();
 			if (0 < rootObjects.size()) {
 				reqIFs.add((ReqIF) rootObjects.get(0));
 			}
 
 		}
 		return reqIFs;
 	}
 
 	public static void saveReqIFsToZip(List<ReqIF> reqIFs, String zipFileName) throws IOException {
 		ReqIFResourceSetImpl resourceSet = getReqIFResourceSet();
 		for (ReqIF reqIF : reqIFs) {
 			XMLResource resource = new ReqIFResourceImpl();
 			resource.getContents().add(reqIF);
 			resourceSet.getResources().add(resource);
 		}
 
 		int lastDotIndex = zipFileName.lastIndexOf(".");
 		String entryName = zipFileName;
 		if (0 < lastDotIndex) {
 			entryName = zipFileName.substring(0, lastDotIndex);
 		}
 		int lastSlashIndex = entryName.lastIndexOf("/");
 		if (0 < lastSlashIndex) {
 			entryName = entryName.substring(lastSlashIndex + 1);
 		}
 
 		FileOutputStream fileOutputStream = new FileOutputStream(zipFileName);
 		ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(fileOutputStream));
 
 		for (int i = 0; i < resourceSet.getResources().size(); i++) {
 			Resource resource = resourceSet.getResources().get(i);
 			ZipEntry zipEntry = new ZipEntry(entryName + "_" + i + ".reqif");
 
 			zipOutputStream.putNextEntry(zipEntry);
 			resource.save(zipOutputStream, null);
 		}
 
 		zipOutputStream.close();
 	}
 
 }
