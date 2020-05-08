 /*******************************************************************************
  * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *******************************************************************************/
 package org.ebayopensource.turmeric.tools.errorlibrary;
 
 import static org.ebayopensource.turmeric.common.v1.types.ErrorCategory.*;
 import static org.ebayopensource.turmeric.common.v1.types.ErrorSeverity.*;
 import static org.hamcrest.Matchers.*;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.logging.Logger;
 
 import javax.tools.JavaCompiler;
 import javax.tools.ToolProvider;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.FilenameUtils;
 import org.ebayopensource.turmeric.junit.asserts.PathAssert;
 import org.ebayopensource.turmeric.junit.utils.MavenTestingUtils;
 import org.ebayopensource.turmeric.common.v1.types.CommonErrorData;
 import org.ebayopensource.turmeric.common.v1.types.ErrorCategory;
 import org.ebayopensource.turmeric.common.v1.types.ErrorSeverity;
 import org.ebayopensource.turmeric.tools.AbstractCodegenTestCase;
 import org.ebayopensource.turmeric.tools.ExpectedLogMessage;
 import org.ebayopensource.turmeric.tools.GeneratedAssert;
 import org.ebayopensource.turmeric.tools.TestResourceUtil;
 import org.ebayopensource.turmeric.tools.codegen.exception.CodeGenFailedException;
 import org.ebayopensource.turmeric.tools.codegen.exception.MissingInputOptionException;
 import org.ebayopensource.turmeric.tools.codegen.util.ClassPathUtil;
 import org.ebayopensource.turmeric.tools.errorlibrary.util.ErrorLibraryUtils;
 import org.junit.Assert;
 import org.junit.Test;
 
 
 public class ErrorLibraryFileGenerationTest extends AbstractCodegenTestCase {
 	private final static String ERRORLIBPROPS = "domain_list.properties";
 
 	private File createTestSpecificPropFile(String errorLibName) throws Exception {
 		// Create empty properties file.
 		File testProp = testingdir.getFile(ERRORLIBPROPS);
 		Assert.assertTrue("Creating empty file: " + testProp, testProp.createNewFile());
 		
 		return testProp;
 	}
 	
 	private File createDomainPropertiesFile(File projRoot, String errorLibName) throws IOException
 	{
 		File testDir = testingdir.getFile("meta-src/META-INF/errorlibrary/" + errorLibName);
 		MavenTestingUtils.ensureDirExists(testDir);
 		File testProp = new File(testDir, ERRORLIBPROPS);
 		if(!testProp.exists()) {
 			Assert.assertTrue("Creating empty file: " + testProp, testProp.createNewFile());
 		}
 		return testProp;
 	}
 	
 	private void storeProps(File propsFile, Properties props) throws IOException {
 		writeProperties(propsFile, props);
 	}
 
 	/**
 	 * Test for generating ErrorConstants.java
 	 */
 	@Test
 	public void testGeneratingErrorConstants() throws Exception {
 		testingdir.ensureEmpty();
 		
 		File rootDir = testingdir.getDir();
 		File destDir = new File(rootDir, "gen-src");
 
 		// @formatter:off
 		String[] inputArgs = { 
 			"-gentype", "genTypeErrorLibAll", 
 			"-pr", rootDir.getAbsolutePath(), 
 			"-domain", "runtime,security", 
 			"-errorlibname","TestErrorLibrary",
 			"-dest", destDir.getAbsolutePath()
 		};
 		// @formatter:on
 
 		File propFile = createDomainPropertiesFile(rootDir, "TestErrorLibrary");
 		Properties props = new Properties();
 		props.setProperty("listOfDomains", "runtime,security");
 		storeProps(propFile, props);
 		
 		copyErrorXmlToProjectRoot("ErrorData_QA.xml", rootDir, "runtime");
 		copyErrorXmlToProjectRoot("ErrorData_QA.xml", rootDir, "security");
 		copyErrorPropertiesToProjectRoot("QAErrors.properties", rootDir, "runtime");
 		copyErrorPropertiesToProjectRoot("QAErrors.properties", rootDir, "security");
 		
 		performDirectCodeGen(inputArgs);
 		
 		GeneratedAssert.assertFileExists(destDir, "org/suhua/errorlibrary/runtime/ErrorConstants.java");
 	}
 
 	/** 
 	 * Testing of generating java files with one entry of error information in ErrorData.xml And error.properties
 	 * contains the error names present in the ErrorData.xml
 	 */
 	@Test
 	public void testGeneratingJavaFilesWithOneErrorEntry() throws Exception {
 		testingdir.ensureEmpty();
 		File rootDir = testingdir.getDir();
 		File destDir = new File(rootDir, "gen-src");
 
 		// @formatter:off
 		String[] inputArgs = { 
 			"-gentype", "genTypeErrorLibAll", 
 			"-pr", rootDir.getAbsolutePath(), 
 			"-domain", "runtime", 
 			"-errorlibname", "TestErrorLibrary",
 			"-dest", destDir.getAbsolutePath()
 		};
 		// @formatter:on
 		
 		String errorPropertiesFilename = "QAErrors.properties";
 
 		File propFile = createDomainPropertiesFile(rootDir, "TestErrorLibrary");
 		copyErrorXmlToProjectRoot("ErrorData_QA.xml", rootDir, "runtime");
 		copyErrorPropertiesToProjectRoot(errorPropertiesFilename, rootDir, "runtime");
 
 		Properties props = new Properties();
 		props.setProperty("listOfDomains", "runtime");
 		storeProps(propFile, props);
 
 		performDirectCodeGen(inputArgs);
 		
 		ExpectedErrors expectedErrors = new ExpectedErrors();
 		expectedErrors.setExpectedErrorDomain("runtime");
 		expectedErrors.setExpectedOrganization("eBay");
 		expectedErrors.setExpectedPackageName("org.suhua.errorlibrary.runtime");
 		expectedErrors.setErrorProperties(errorPropertiesFilename);
 		expectedErrors.add(1000L,"svc_factory_cannot_create_svc",ERROR,SYSTEM,"runtime","System",null);
 		
 		assertErrorConstants(destDir, expectedErrors);
 		assertErrorDataCollection(destDir, expectedErrors);
 	}
 
 	@Test
 	public void testGeneratingJavaFilesWithTwoErrorEntry() throws Exception {
 		testingdir.ensureEmpty();
 		File rootDir = testingdir.getDir();
 		File destDir = new File(rootDir, "gen-src");
 
 		// @formatter:off
 		String[] inputArgs = { 
 			"-gentype", "genTypeErrorLibAll", 
 			"-pr", rootDir.getAbsolutePath(), 
 			"-domain", "security", 
 			"-errorlibname","TestErrorLibrary",
 			"-dest", destDir.getAbsolutePath()
 		};
 		// @formatter:on
 
 		String errorPropertiesFilename = "QAErrors2.properties";
 		
 		copyErrorXmlToProjectRoot("ErrorData2_QA.xml", rootDir, "security");
 		copyErrorPropertiesToProjectRoot(errorPropertiesFilename, rootDir,
 				"security");
 
 		File propFile = createDomainPropertiesFile(rootDir, "TestErrorLibrary");
 		Properties props = new Properties();
 		props.setProperty("listOfDomains", "security");
 		storeProps(propFile, props);
 
 		performDirectCodeGen(inputArgs);
 		
 		ExpectedErrors expectedErrors = new ExpectedErrors();
 		expectedErrors.setExpectedErrorDomain("security");
 		expectedErrors.setExpectedOrganization("eBay");
 		expectedErrors.setExpectedPackageName("org.suhua.errorlibrary.security");
 		expectedErrors.setErrorProperties(errorPropertiesFilename);
 		expectedErrors.add(1002L,"new_error1",ERROR,SYSTEM,"security","System",null);
 		expectedErrors.add(1003L,"new_error2",WARNING,APPLICATION,"security","System",null);
 
 		assertErrorConstants(destDir, expectedErrors);
 		assertErrorDataCollection(destDir, expectedErrors);
 	}
 
 	@Test
 	public void testGeneratingJavaFilesWithUpdatedErrorEntry() throws Exception {
 		testingdir.ensureEmpty();
 		File rootDir = testingdir.getDir();
 		File destDir = new File(rootDir, "gen-src");
 
 		// @formatter:off
 		String[] inputArgs1 = { 
 			"-gentype", "genTypeErrorLibAll", 
 			"-pr", rootDir.getAbsolutePath(), 
 			"-domain", "security", 
 			"-errorlibname","TestErrorLibrary",
 			"-dest", destDir.getAbsolutePath()
 		};
 		// @formatter:on
 
 		copyErrorXmlToProjectRoot("ErrorData2_QA.xml", rootDir, "security");
 		copyErrorPropertiesToProjectRoot("QAErrors2.properties", rootDir,
 				"security");
 
 		File propFile = createDomainPropertiesFile(rootDir, "TestErrorLibrary");
 		Properties props = new Properties();
 		props.setProperty("listOfDomains", "security");
 		storeProps(propFile, props);
 
 		performDirectCodeGen(inputArgs1);
 
 		// @formatter:off
 		String[] inputArgs = { 
 			"-gentype", "genTypeCommandLineAll", 
 			"-pr", rootDir.getAbsolutePath(),
 			"-errorlibname","TestErrorLibrary",
 		};
 		// @formatter:on
 
 		String errorPropertiesFilename = "UpdatedQAErrors.properties";
 		
 	    propFile = createDomainPropertiesFile(rootDir, "TestErrorLibrary");
 	    props = new Properties();
 		props.setProperty("listOfDomains", "security");
 		storeProps(propFile, props);
 	
 		copyErrorXmlToProjectRoot("UpdatedErrorData_QA.xml", rootDir, "security");
 		copyErrorPropertiesToProjectRoot(errorPropertiesFilename, rootDir,
 				"security");
 
 		performDirectCodeGen(inputArgs);
 		
 		ExpectedErrors expectedErrors = new ExpectedErrors();
 		expectedErrors.setExpectedErrorDomain("security");
 		expectedErrors.setExpectedOrganization("eBay");
 		expectedErrors.setExpectedPackageName("org.suhua.errorlibrary.security");
 		expectedErrors.setErrorProperties(errorPropertiesFilename);
 		expectedErrors.add(1002L,"new_error3",WARNING,SYSTEM,"security","newSubdomain",null);
 		expectedErrors.add(1003L,"new_error2",WARNING,APPLICATION,"security","System",null);
 
 		assertErrorConstants(destDir, expectedErrors);
 		assertErrorDataCollection(destDir, expectedErrors);
 	}
 
 	@Test
 	public void testGeneratingJavaFilesWithDeletedErrorEntry() throws Exception {
 		testingdir.ensureEmpty();
 		File rootDir = testingdir.getDir();
 		File destDir = new File(rootDir, "gen-src");
 
 		// @formatter:off
 		String[] inputArgs1 = { 
 			"-gentype", "genTypeErrorLibAll", 
 			"-pr", rootDir.getAbsolutePath(), 
 			"-domain", "security", 
 			"-errorlibname","TestErrorLibrary",
 			"-dest", destDir.getAbsolutePath()
 		};
 		// @formatter:on
 
 		copyErrorXmlToProjectRoot("UpdatedErrorData_QA.xml", rootDir, "security");
 		copyErrorPropertiesToProjectRoot("UpdatedQAErrors.properties", rootDir,
 				"security");
 
 		File propFile = createDomainPropertiesFile(rootDir, "TestErrorLibrary");
 		Properties props = new Properties();
 		props.setProperty("listOfDomains", "security");
 		storeProps(propFile, props);
 
 		performDirectCodeGen(inputArgs1);
 
 		// These two calls to codegen are need as the first call creates java files for "UpdatedErrorData_QA.xml", before the 
 		// next call ErrorData.xml is replaced with "DeletedErrorData_QA.xml" to simulate the error deletion.
 		 
 		// @formatter:off
 		String[] inputArgs = { 
 			"-gentype", "genTypeCommandLineAll", 
 			"-pr", rootDir.getAbsolutePath(), 
 			"-domain", "security", 
 			"-errorlibname","temp",
 			"-dest", destDir.getAbsolutePath()
 		};
 		// @formatter:on
 
 		propFile = createDomainPropertiesFile(rootDir, "TestErrorLibrary");
 		props = new Properties();
 		props.setProperty("listOfDomains", "security");
 		storeProps(propFile, props);
 		
 		String errorPropertiesFilename = "DeletedQAErrors.properties";
 		
 		copyErrorXmlToProjectRoot("DeletedErrorData_QA.xml", rootDir, "security");
 		copyErrorPropertiesToProjectRoot(errorPropertiesFilename, rootDir,"security");
 
 		performDirectCodeGen(inputArgs);
 		
 		ExpectedErrors expectedErrors = new ExpectedErrors();
 		expectedErrors.setExpectedErrorDomain("security");
 		expectedErrors.setExpectedOrganization("eBay");
 		expectedErrors.setExpectedPackageName("org.suhua.errorlibrary.security");
 		expectedErrors.setErrorProperties(errorPropertiesFilename);
 		expectedErrors.add(1003L,"new_error2",WARNING,APPLICATION,"security","System",null);
 
 		assertErrorConstants(destDir, expectedErrors);
 		assertErrorDataCollection(destDir, expectedErrors);
 	}
 
 	@Test
 	public void testGeneratingJavaFilesWithoutOptionalAttribute()
 			throws Exception {
 		testingdir.ensureEmpty();
 		File rootDir = testingdir.getDir();
 		File destDir = new File(rootDir, "gen-src");
 
 		// @formatter:off
 		String[] inputArgs1 = { 
 			"-gentype", "genTypeErrorLibAll", 
 			"-pr", rootDir.getAbsolutePath(), 
 			"-domain", "security", 
 			"-errorlibname","TestErrorLibrary",
 			"-dest", destDir.getAbsolutePath()
 		};
 		// @formatter:on
 
 		String errorPropertiesFilename = "OptionalAttQAErrors.properties";
 		copyErrorXmlToProjectRoot("OptionalAttErrorData_QA.xml", rootDir,
 				"security");
 		copyErrorPropertiesToProjectRoot(errorPropertiesFilename,
 				rootDir, "security");
 
 		File propFile = createDomainPropertiesFile(rootDir, "TestErrorLibrary");
 		Properties props = new Properties();
 		props.setProperty("listOfDomains", "security");
 		storeProps(propFile, props);
 
 		performDirectCodeGen(inputArgs1);
 		
 		ExpectedErrors expectedErrors = new ExpectedErrors();
 		expectedErrors.setExpectedErrorDomain("security");
 		expectedErrors.setExpectedOrganization("eBay");
 		expectedErrors.setExpectedPackageName("org.suhua.errorlibrary.security");
 		expectedErrors.setErrorProperties(errorPropertiesFilename);
 		expectedErrors.add(1003L,"new_error2",WARNING,APPLICATION,"security",null,null);
 
 		assertErrorConstants(destDir, expectedErrors);
 		assertErrorDataCollection(destDir, expectedErrors);
 	}
 
 	@Test
 	public void testInvalidNumberOfEntryBetweenErrorDataAndErrorProperties()
 			throws Exception {
 		testingdir.ensureEmpty();
 		File rootDir = testingdir.getDir();
 		File destDir = new File(rootDir, "gen-src");
 
 		// @formatter:off
 		String[] inputArgs1 = { 
 			"-gentype", "genTypeErrorLibAll", 
 			"-pr", rootDir.getAbsolutePath(), 
 			"-domain", "security", 
 			"-errorlibname","TestErrorLibrary",
 			"-dest", destDir.getAbsolutePath()
 		};
 		// @formatter:on
 
 		copyErrorXmlToProjectRoot("InvalidErrorData_QA.xml", rootDir, "security");
 		copyErrorPropertiesToProjectRoot("InvalidQAErrors.properties", rootDir,
 				"security");
 
 		File propFile = createDomainPropertiesFile(rootDir, "TestErrorLibrary");
 		Properties props = new Properties();
 		props.setProperty("listOfDomains", "security");
 		storeProps(propFile, props);
 
 		try {
 			performDirectCodeGen(inputArgs1);
 			Assert.fail("Expected an Exception");
 		} catch (Exception e) {
 			String expected = "Errors.properties does not have all the errors "
 				+"defined in ErrorData.xml namely [new_error3]";
 			Assert.assertThat(e.getMessage(), containsString(expected));
 		}
 	}
 
 	@Test
 	public void testInvalidNumberOfEntryBetweenErrorDataAndErrorProperties2()
 			throws Exception {
 		testingdir.ensureEmpty();
 		File rootDir = testingdir.getDir();
 		File destDir = new File(rootDir, "gen-src");
 
 		// @formatter:off
 		String[] inputArgs1 = { 
 			"-gentype", "genTypeErrorLibAll", 
 			"-pr", rootDir.getAbsolutePath(), 
 			"-domain", "security", 
 			"-errorlibname","TestErrorLibrary",
 			"-dest", destDir.getAbsolutePath()
 		};
 		// @formatter:on
 
 		copyErrorXmlToProjectRoot("Invalid2ErrorData_QA.xml", rootDir, "security");
 		copyErrorPropertiesToProjectRoot("Invalid2QAErrors.properties", rootDir,
 				"security");
 
 		File propFile = createDomainPropertiesFile(rootDir, "TestErrorLibrary");
 		Properties props = new Properties();
 		props.setProperty("listOfDomains", "security");
 		storeProps(propFile, props);
 		
 		Logger errlogger = Logger.getLogger(ErrorLibraryUtils.class.getPackage().getName());
 		ExpectedLogMessage expectedLog = new ExpectedLogMessage();
 		expectedLog.setExpectedMessage("The Errors.properties file has more "
 				+ "errors defined in addition to those existing in "
 				+ "ErrorData.xml.");
 		
 		errlogger.addHandler(expectedLog);
 		try {
 			performDirectCodeGen(inputArgs1);
 			expectedLog.assertFoundMessage();
 		} finally {
 			errlogger.removeHandler(expectedLog);
 		}
 	}
 	
 	@Test
 	public void testDuplicateEntryInErrorData() throws Exception {
 		testingdir.ensureEmpty();
 		File rootDir = testingdir.getDir();
 		File destDir = new File(rootDir, "gen-src");
 
 		// @formatter:off
 		String[] inputArgs1 = { 
 			"-gentype", "genTypeErrorLibAll", 
 			"-pr", rootDir.getAbsolutePath(), 
 			"-domain", "security", 
 			"-errorlibname","TestErrorLibrary",
 			"-dest", destDir.getAbsolutePath()
 		};
 		// @formatter:on
 
 		copyErrorXmlToProjectRoot("DuplicateErrorData_QA.xml", rootDir, "security");
 		copyErrorPropertiesToProjectRoot("QAErrors2.properties", rootDir,
 				"security");
 
 		File propFile = createDomainPropertiesFile(rootDir, "TestErrorLibrary");
 		Properties props = new Properties();
 		props.setProperty("listOfDomains", "security");
 		storeProps(propFile, props);
 
 		try {
 			performDirectCodeGen(inputArgs1);
 			Assert.fail("Expected an Exception");
 		} catch (Exception e) {
 			String expected = "Duplicates found in ErrorData.xml. "
 					+ "They are [new_error1, new_error2]";
 			Assert.assertThat(e.getMessage(), containsString(expected));
 		}
 	}
 
 	@Test
 	public void testDuplicateEntryInErrorProperties() throws Exception {
 		testingdir.ensureEmpty();
 		File rootDir = testingdir.getDir();
 		File destDir = new File(rootDir, "gen-src");
 
 		// @formatter:off
 		String[] inputArgs1 = { 
 			"-gentype", "genTypeErrorLibAll", 
 			"-pr", rootDir.getAbsolutePath(), 
 			"-domain", "security", 
 			"-errorlibname","TestErrorLibrary",
 			"-dest", destDir.getAbsolutePath()
 		};
 		// @formatter:on
 
 		copyErrorXmlToProjectRoot("ErrorData2_QA.xml", rootDir, "security");
 		copyErrorPropertiesToProjectRoot("DuplicateQAErrors.properties", rootDir,
 				"security");
 
 		File propFile = createDomainPropertiesFile(rootDir, "TestErrorLibrary");
 		Properties props = new Properties();
 		props.setProperty("listOfDomains", "security");
 		storeProps(propFile, props);
 
 		try {
 			performDirectCodeGen(inputArgs1);
 			Assert.fail("Expected an Exception");
 		} catch (Exception e) {
 			String expected = "Duplicates found in Error.properties. "
 					+ "They are [new_error2, new_error1]";
 			Assert.assertThat(e.getMessage(), containsString(expected));
 		}
 	}
 
 	@Test
 	public void testMissingAttributeNameInErrorDataXml() throws Exception {
 		testingdir.ensureEmpty();
 		File rootDir = testingdir.getDir();
 		File destDir = new File(rootDir, "gen-src");
 
 		// @formatter:off
 		String[] inputArgs1 = { 
 			"-gentype", "genTypeErrorLibAll", 
 			"-pr", rootDir.getAbsolutePath(), 
 			"-domain", "security", 
 			"-errorlibname","TestErrorLibrary",
 			"-dest", destDir.getAbsolutePath()
 		};
 		// @formatter:on
 
 		copyErrorXmlToProjectRoot("MissingAttNameErrorData_QA.xml", rootDir,
 				"security");
 		copyErrorPropertiesToProjectRoot("QAErrors2.properties", rootDir,
 				"security");
 
 		File propFile = createDomainPropertiesFile(rootDir, "TestErrorLibrary");
 		Properties props = new Properties();
 		props.setProperty("listOfDomains", "security");
 		storeProps(propFile, props);
 
 		try {
 			performDirectCodeGen(inputArgs1);
 			Assert.fail("Expected an Exception");
 		} catch (Exception e) {
 			String expected = "Metadata Validation failed : XML validation against XSD failed : "
 				+"cvc-complex-type.4: Attribute 'name' must appear on element 'error'";
 			Assert.assertThat(e.getMessage(), containsString(expected));
 		}
 	}
 
 	@Test
 	public void testMissingAttributeIdInErrorDataXml() throws Exception {
 		testingdir.ensureEmpty();
 		File rootDir = testingdir.getDir();
 		File destDir = new File(rootDir, "gen-src");
 
 		// @formatter:off
 		String[] inputArgs1 = { 
 			"-gentype", "genTypeErrorLibAll", 
 			"-pr", rootDir.getAbsolutePath(), 
 			"-domain", "security", 
 			"-errorlibname","TestErrorLibrary",
 			"-dest", destDir.getAbsolutePath()
 		};
 		// @formatter:on
 
 		copyErrorXmlToProjectRoot("MissingAttIdErrorData_QA.xml", rootDir,
 				"security");
 		copyErrorPropertiesToProjectRoot("QAErrors2.properties", rootDir,
 				"security");
 
 		File propFile = createDomainPropertiesFile(rootDir, "TestErrorLibrary");
 		Properties props = new Properties();
 		props.setProperty("listOfDomains", "security");
 		storeProps(propFile, props);
 
 		try {
 			performDirectCodeGen(inputArgs1);
 			Assert.fail("Expected an Exception");
 		} catch (Exception e) {
 			String expected = "Metadata Validation failed : XML validation against XSD failed : "
 				+"cvc-complex-type.4: Attribute 'id' must appear on element 'error'";
 			Assert.assertThat(e.getMessage(), containsString(expected));
 		}
 	}
 
 	@Test
 	public void testMissingAttributeSeverityInErrorDataXml() throws Exception {
 		testingdir.ensureEmpty();
 		File rootDir = testingdir.getDir();
 		File destDir = new File(rootDir, "gen-src");
 
 		// @formatter:off
 		String[] inputArgs1 = { 
 			"-gentype", "genTypeErrorLibAll", 
 			"-pr", rootDir.getAbsolutePath(), 
 			"-domain", "security", 
 			"-errorlibname","TestErrorLibrary",
 			"-dest", destDir.getAbsolutePath()
 		};
 		// @formatter:on
 
 		copyErrorXmlToProjectRoot("MissingAttSeverityErrorData_QA.xml", rootDir,
 				"security");
 		copyErrorPropertiesToProjectRoot("QAErrors2.properties", rootDir,
 				"security");
 
 		File propFile = createDomainPropertiesFile(rootDir, "TestErrorLibrary");
 		Properties props = new Properties();
 		props.setProperty("listOfDomains", "security");
 		storeProps(propFile, props);
 
 		try {
 			performDirectCodeGen(inputArgs1);
 			Assert.fail("Expected an Exception");
 		} catch (Exception e) {
 			String expected = "Metadata Validation failed : XML validation against XSD failed : "
 				+"cvc-complex-type.4: Attribute 'severity' must appear on element 'error'";
 			Assert.assertThat(e.getMessage(), containsString(expected));
 		}
 	}
 
 	@Test
 	public void testMissingAttributeCategoryInErrorDataXml() throws Exception {
 		testingdir.ensureEmpty();
 		File rootDir = testingdir.getDir();
 		File destDir = new File(rootDir, "gen-src");
 
 		// @formatter:off
 		String[] inputArgs1 = { 
 			"-gentype", "genTypeErrorLibAll", 
 			"-pr", rootDir.getAbsolutePath(), 
 			"-domain", "security", 
 			"-errorlibname","TestErrorLibrary",
 			"-dest", destDir.getAbsolutePath()
 		};
 		// @formatter:on
 
 		copyErrorXmlToProjectRoot("MissingAttCategoryErrorData_QA.xml", rootDir,
 				"security");
 		copyErrorPropertiesToProjectRoot("QAErrors2.properties", rootDir,
 				"security");
 
 		File propFile = createDomainPropertiesFile(rootDir, "TestErrorLibrary");
 		Properties props = new Properties();
 		props.setProperty("listOfDomains", "security");
 		storeProps(propFile, props);
 
 		try {
 			performDirectCodeGen(inputArgs1);
 			Assert.fail("Expected an Exception");
 		} catch (Exception e) {
 			String expected = "Metadata Validation failed : XML validation against XSD failed : "
 				+"cvc-complex-type.4: Attribute 'category' must appear on element 'error'";
 			Assert.assertThat(e.getMessage(), containsString(expected));
 		}
 	}
 
 	@Test
 	public void testErrorNameWithSpacesInErrorDataXml() throws Exception {
 		testingdir.ensureEmpty();
 		File rootDir = testingdir.getDir();
 		File destDir = new File(rootDir, "gen-src");
 
 		// @formatter:off
 		String[] inputArgs1 = { 
 			"-gentype", "genTypeErrorLibAll", 
 			"-pr", rootDir.getAbsolutePath(), 
 			"-domain", "security", 
 			"-errorlibname","TestErrorLibrary",
 			"-dest", destDir.getAbsolutePath()
 		};
 		// @formatter:on
 
 		copyErrorXmlToProjectRoot("SpacesInErrorNameErrorData_QA.xml", rootDir,
 				"security");
 		copyErrorPropertiesToProjectRoot("QAErrors2.properties", rootDir,
 				"security");
 
 		File propFile = createDomainPropertiesFile(rootDir, "TestErrorLibrary");
 		Properties props = new Properties();
 		props.setProperty("listOfDomains", "security");
 		storeProps(propFile, props);
 
 		try {
 			performDirectCodeGen(inputArgs1);
 			Assert.fail("Expected an Exception");
 		} catch (Exception e) {
 			String expected = "The error name(s) [new_ error1] have whitespace character and "
 				+"CodeGen cannot proceed. Pls check your ErrorData.xml file and fix it.";
 			Assert.assertThat(e.getMessage(), containsString(expected));
 		}
 	}
 
 	@Test
 	public void testErrorNameWithSpacesInErrorPropertiesXml() throws Exception {
 		testingdir.ensureEmpty();
 		File rootDir = testingdir.getDir();
 		File destDir = new File(rootDir, "gen-src");
 
 		// @formatter:off
 		String[] inputArgs1 = { 
 			"-gentype", "genTypeErrorLibAll", 
 			"-pr", rootDir.getAbsolutePath(), 
 			"-domain", "security", 
 			"-errorlibname","TestErrorLibrary",
 			"-dest", destDir.getAbsolutePath()
 		};
 		// @formatter:on
 
 		copyErrorXmlToProjectRoot("ErrorData2_QA.xml", rootDir, "security");
 		copyErrorPropertiesToProjectRoot(
 				"SpacesInErrorNameQAErrors.properties", rootDir, "security");
 
 		File propFile = createDomainPropertiesFile(rootDir, "TestErrorLibrary");
 		Properties props = new Properties();
 		props.setProperty("listOfDomains", "security");
 		storeProps(propFile, props);
 
 		try {
 			performDirectCodeGen(inputArgs1);
 			Assert.fail("Expected an Exception");
 		} catch (Exception e) {
 			String expected = "Errors.properties does not have all the errors "
 				+"defined in ErrorData.xml namely [new_error2]";
 			Assert.assertThat(e.getMessage(), containsString(expected));
 		}
 	}
 
 	@Test
 	@SuppressWarnings("unchecked")
 	public void testMultipleErrorsInErrorPropertiesErrorData() throws Exception {
 		testingdir.ensureEmpty();
 		File rootDir = testingdir.getDir();
 		File destDir = new File(rootDir, "gen-src");
 
 		// @formatter:off
 		String[] inputArgs1 = { 
 			"-gentype", "genTypeErrorLibAll", 
 			"-pr", rootDir.getAbsolutePath(), 
 			"-domain", "security", 
 			"-errorlibname","TestErrorLibrary",
 			"-dest", destDir.getAbsolutePath()
 		};
 		// @formatter:on
 
 		copyErrorXmlToProjectRoot("MultipleErrorErrorData_QA.xml", rootDir,
 				"security");
 		copyErrorPropertiesToProjectRoot("MultipleErrorQAErrors.properties",
 				rootDir, "security");
 
 		File propFile = createDomainPropertiesFile(rootDir, "TestErrorLibrary");
 		Properties props = new Properties();
 		props.setProperty("listOfDomains", "security");
 		storeProps(propFile, props);
 
 		try {
 			performDirectCodeGen(inputArgs1);
 			Assert.fail("Expected an Exception of type: " + CodeGenFailedException.class.getName());
 		} catch (CodeGenFailedException e) {
 			String expected1 = "Duplicates found in ErrorData.xml. "
 				+"They are [new_error2]";
 			String expected2 = "Duplicates found in Error.properties. "
 				+"They are [new_error1]";
 			Assert.assertThat(e.getMessage(),
 					allOf(containsString(expected1), containsString(expected2)));
 		}
 	}
 
 	@Test
 	public void testJavaFileGenerationForTwoDomains() throws Exception {
 		testingdir.ensureEmpty();
 		File rootDir = testingdir.getDir();
 		File destDir = new File(rootDir, "gen-src");
 
 		// @formatter:off
 		String[] inputArgs1 = { 
 			"-gentype", "genTypeErrorLibAll", 
 			"-pr", rootDir.getAbsolutePath(), 
 			"-domain", "runtime,security", 
 			"-errorlibname","TestErrorLibrary",
 			"-dest", destDir.getAbsolutePath()
 		};
 		// @formatter:on
 
 		copyErrorXmlToProjectRoot("ErrorData_QA.xml", rootDir, "runtime");
 		copyErrorPropertiesToProjectRoot("QAErrors.properties", rootDir, "runtime");
 
 		copyErrorXmlToProjectRoot("ErrorData2_QA.xml", rootDir, "security");
 		copyErrorPropertiesToProjectRoot("QAErrors2.properties", rootDir,
 				"security");
 
 		File propFile = createDomainPropertiesFile(rootDir, "TestErrorLibrary");
 		Properties props = new Properties();
 		props.setProperty("listOfDomains", "runtime,security");
 		storeProps(propFile, props);
 
 		performDirectCodeGen(inputArgs1);
 
 		// Assert Runtime Domain Generation
 		ExpectedErrors expectedRuntimeErrors = new ExpectedErrors();
 		expectedRuntimeErrors.setExpectedErrorDomain("runtime");
 		expectedRuntimeErrors.setExpectedOrganization("eBay");
 		expectedRuntimeErrors.setExpectedPackageName("org.suhua.errorlibrary.runtime");
 		expectedRuntimeErrors.setErrorProperties("QAErrors.properties");
 		expectedRuntimeErrors.add(1000L,"svc_factory_cannot_create_svc",ERROR,SYSTEM,"runtime","System",null);
 
 		assertErrorConstants(destDir, expectedRuntimeErrors);
 		assertErrorDataCollection(destDir, expectedRuntimeErrors);
 
 		// Assert Security Domain Generation
 		ExpectedErrors expectedSecurityErrors = new ExpectedErrors();
 		expectedSecurityErrors.setExpectedErrorDomain("security");
 		expectedSecurityErrors.setExpectedOrganization("eBay");
 		expectedSecurityErrors.setExpectedPackageName("org.suhua.errorlibrary.security");
 		expectedSecurityErrors.setErrorProperties("QAErrors2.properties");
 		expectedSecurityErrors.add(1002L,"new_error1",ERROR,SYSTEM,"security","System",null);
 		expectedSecurityErrors.add(1003L,"new_error2",WARNING,APPLICATION,"security","System",null);
 
 		assertErrorConstants(destDir, expectedSecurityErrors);
 		assertErrorDataCollection(destDir, expectedSecurityErrors);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testReportingErrorForTwoDomains() throws Exception {
 		testingdir.ensureEmpty();
 		File rootDir = testingdir.getDir();
 		File destDir = new File(rootDir, "gen-src");
 
 		// @formatter:off
 		String[] inputArgs1 = { 
 			"-gentype", "genTypeErrorLibAll", 
 			"-pr", rootDir.getAbsolutePath(), 
 			"-domain", "runtime,security", 
 			"-errorlibname","TestErrorLibrary",
 			"-dest", destDir.getAbsolutePath()
 		};
 		// @formatter:on
 
 		copyErrorXmlToProjectRoot("ErrorAcrossDomain1ErrorData_QA.xml", rootDir,
 				"runtime");
 		copyErrorPropertiesToProjectRoot(
 				"ErrorAcrossDomain1QAErrors.properties", rootDir, "runtime");
 
 		copyErrorXmlToProjectRoot("ErrorAcrossDomain2ErrorData_QA.xml", rootDir,
 				"security");
 		copyErrorPropertiesToProjectRoot("QAErrors2.properties", rootDir,
 				"security");
 
 		File propFile = createDomainPropertiesFile(rootDir, "TestErrorLibrary");
 		Properties props = new Properties();
 		props.setProperty("listOfDomains", "runtime,security");
 		storeProps(propFile, props);
 
 		try {
 			performDirectCodeGen(inputArgs1); 
 			Assert.fail("Expected an Exception of type: " + CodeGenFailedException.class.getName());
 		} catch (CodeGenFailedException e) {
 			String expected1 = "ErrorConstants generation for the domain \"runtime\" failed";
 			String expected2 = "ErrorConstants generation for the domain \"security\" failed";
 			Assert.assertThat(e.getMessage(),
 					allOf(containsString(expected1), containsString(expected2)));
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testUnparsableErrorDataXml() throws Exception {
 		testingdir.ensureEmpty();
 		File rootDir = testingdir.getDir();
 		File destDir = new File(rootDir, "gen-src");
 
 		// @formatter:off
 		String[] inputArgs1 = { 
 			"-gentype", "genTypeErrorLibAll", 
 			"-pr", rootDir.getAbsolutePath(), 
 			"-domain", "security", 
 			"-errorlibname","TestErrorLibrary",
 			"-dest", destDir.getAbsolutePath()
 		};
 		// @formatter:on
 
 		copyErrorXmlToProjectRoot("UnparsableErrorData_QA.xml", rootDir,
 				"security");
 		copyErrorPropertiesToProjectRoot("QAErrors2.properties", rootDir,
 				"security");
 
 		File propFile = createDomainPropertiesFile(rootDir, "TestErrorLibrary");
 		Properties props = new Properties();
 		props.setProperty("listOfDomains", "security");
 		storeProps(propFile, props);
 		
 		try{
 			performDirectCodeGen(inputArgs1); 
 			Assert.fail("Expected an Exception of type: " + CodeGenFailedException.class.getName());
 		} catch (CodeGenFailedException e) {
 			String expected1 = "SAXParseException";
 			String expected2 = "The element type \"errorlist\" must be terminated by the matching end-tag";
 			Assert.assertThat(e.getMessage(),
 					allOf(containsString(expected1), containsString(expected2)));
 		}
 	}
 
 	@Test
 	public void testWithNoErrorDataXml() throws Exception {
 		testingdir.ensureEmpty();
 		File rootDir = testingdir.getDir();
 		File destDir = new File(rootDir, "gen-src");
 
 		// @formatter:off
 		String[] inputArgs1 = { 
 			"-gentype", "genTypeErrorLibAll", 
 			"-pr", rootDir.getAbsolutePath(), 
 			"-domain", "security", 
 			"-errorlibname","TestErrorLibrary",
 			"-dest", destDir.getAbsolutePath()
 		};
 		// @formatter:on
 
 		copyErrorPropertiesToProjectRoot("QAErrors2.properties", rootDir,
 				"security");
 
 		File propFile = createDomainPropertiesFile(rootDir, "TestErrorLibrary");
 		Properties props = new Properties();
 		props.setProperty("listOfDomains", "security");
 		storeProps(propFile, props);
 
 		try{
 			performDirectCodeGen(inputArgs1); 
 			Assert.fail("Expected an Exception of type: " + CodeGenFailedException.class.getName());
 		} catch (CodeGenFailedException e) {
 			String expected = "ErrorData.xml for domain [security] not found";
 			Assert.assertThat(e.getMessage(), containsString(expected));
 		}
 	}
 
 	@Test
 	public void testWithNoErrorProperties() throws Exception {
 		testingdir.ensureEmpty();
 		File rootDir = testingdir.getDir();
 		File destDir = new File(rootDir, "gen-src");
 
 		// @formatter:off
 		String[] inputArgs1 = { 
 			"-gentype", "genTypeErrorLibAll", 
 			"-pr", rootDir.getAbsolutePath(), 
 			"-domain", "security", 
 			"-errorlibname","TestErrorLibrary",
 			"-dest", destDir.getAbsolutePath()
 		};
 		// @formatter:on
 
 		copyErrorXmlToProjectRoot("ErrorData2_QA.xml", rootDir, "security");
 
 		File propFile = createDomainPropertiesFile(rootDir, "TestErrorLibrary");
 		Properties props = new Properties();
 		props.setProperty("listOfDomains", "security");
 		storeProps(propFile, props);
 		
 		try{
 			performDirectCodeGen(inputArgs1); 
 			Assert.fail("Expected an Exception of type: " + CodeGenFailedException.class.getName());
 		} catch (CodeGenFailedException e) {
 			String expected1 = "Properties file not found in the location";
 			Assert.assertThat(e.getMessage(),
 				containsString(expected1));
 		}
 	}
 
 	@Test
 	public void testWithoutDomainListInPropsFileForCommandLineBuild() throws Exception {
 		testingdir.ensureEmpty();
 		File rootDir = testingdir.getDir();
 		File destDir = new File(rootDir, "gen-src");
 
 		// @formatter:off
 		String[] inputArgs1 = { 
 			"-gentype", "genTypeCommandLineAll", 
 			"-pr", rootDir.getAbsolutePath(), 
 			"-errorlibname","TestErrorLibrary",
 			"-dest", destDir.getAbsolutePath()
 		};
 		// @formatter:on
 
 		copyErrorXmlToProjectRoot("ErrorData2_QA.xml", rootDir, "security");
 		copyErrorPropertiesToProjectRoot("QAErrors2.properties", rootDir,
 				"security");
 
 		File propFile = createDomainPropertiesFile(rootDir, "TestErrorLibrary");
 		Properties props = new Properties();
 		storeProps(propFile, props);
 
 
 		try{
 			performDirectCodeGen(inputArgs1); 
 			Assert.fail("Expected an Exception of type: " + MissingInputOptionException.class.getName());
 		} catch (MissingInputOptionException e) {
 			String expectedmsg = "List of domains is missing which is mandatory. " +
 				"Pls provide the value for this option -domain";
 			Assert.assertThat(e.getMessage(), containsString(expectedmsg));
 		}
 	}
 
 	@Test
 	//Error library properties file is removed.
 	public void testWithoutErrorLibPropsFile() throws Exception {
 		testingdir.ensureEmpty();
 		File rootDir = testingdir.getDir();
 		File destDir = new File(rootDir, "gen-src");
 
 		// @formatter:off
 		String[] inputArgs1 = { 
 			"-gentype", "genTypeErrorLibAll", 
 			"-pr", rootDir.getAbsolutePath(), 
 			"-domain", "security",
 			"-errorlibname","TestErrorLibrary",
 			"-dest", destDir.getAbsolutePath()
 		};
 		// @formatter:on
 
 		copyErrorXmlToProjectRoot("ErrorData2_QA.xml", rootDir, "security");
 		// INTENTIONALY DONT USE PROPERTIES FILES
 		// copyErrorPropertiesToProjectRoot("QAErrors2.properties", rootDir, "security");
 		// File propFile = createTestSpecificPropFile("testWithoutErrorLibPropsFile");
 		// Properties props = new Properties();
 		// storeProps(propFile, props);
 
 		try{
 			performDirectCodeGen(inputArgs1); 
 			Assert.fail("Expected an Exception of type: " + CodeGenFailedException.class.getName());
 		} catch (CodeGenFailedException e) {
 			String expectedmsg = "Properties file not found in the location" +
 					" <errorLibrary>/meta-src/META-INF/errorlibrary/security";
 			Assert.assertThat(e.getMessage(), containsString(expectedmsg));
 		}
 	}
 
 	@Test
 	public void testForMultipleEntryForErrorInMetadataFile() throws Exception {
 		testingdir.ensureEmpty();
 		File rootDir = testingdir.getDir();
 		File destDir = new File(rootDir, "gen-src");
 
 		// @formatter:off
 		String[] inputArgs1 = { 
 			"-gentype", "genTypeErrorLibAll", 
 			"-pr", rootDir.getAbsolutePath(), 
 			"-domain", "runtime,security", 
 			"-errorlibname","TestErrorLibrary",
 			"-dest", destDir.getAbsolutePath()
 		};
 		// @formatter:on
 
 		String errorPropertiesFilename = "QAErrors2.properties";
 
 		copyErrorXmlToProjectRoot("ErrorData2_QA.xml", rootDir, "runtime");
 		copyErrorPropertiesToProjectRoot(errorPropertiesFilename, rootDir, "runtime");
 
 		copyErrorXmlToProjectRoot("ErrorData2_QA.xml", rootDir, "security");
 		copyErrorPropertiesToProjectRoot(errorPropertiesFilename, rootDir, "security");
 
 		File propFile = createDomainPropertiesFile(rootDir, "TestErrorLibrary");
 		Properties props = new Properties();
 		props.setProperty("listOfDomains", "runtime,security");
 		storeProps(propFile, props);
 
 		performDirectCodeGen(inputArgs1);
 
 		ExpectedErrors expectedErrors = new ExpectedErrors();
 		expectedErrors.setExpectedErrorDomain("security");
 		expectedErrors.setExpectedOrganization("eBay");
 		expectedErrors.setExpectedPackageName("org.suhua.errorlibrary.security");
 		expectedErrors.setErrorProperties(errorPropertiesFilename);
 		expectedErrors.add(1002L,"new_error1",ERROR,SYSTEM,"security","System",null);
 		expectedErrors.add(1003L,"new_error2",WARNING,APPLICATION,"security","System",null);
 
 		assertErrorConstants(destDir, expectedErrors);
 		assertErrorDataCollection(destDir, expectedErrors);
 	}
 
 	@Test
 	public void testCommandLintGentype() throws Exception {
 		testingdir.ensureEmpty();
 		File rootDir = testingdir.getDir();
 		File destDir = new File(rootDir, "gen-src");
 
 		// @formatter:off
 		String[] inputArgs = { 
 			"-gentype", "genTypeCommandLineAll", 
 			"-pr", rootDir.getAbsolutePath(),
 			"-errorlibname","TestErrorLibrary",
 			"-dest", destDir.getAbsolutePath() 
 		};
 		// @formatter:on
 
 		copyErrorXmlToProjectRoot("ErrorData2_QA_Runtime.xml", rootDir, "runtime");
 		copyErrorPropertiesToProjectRoot("QAErrors2.properties", rootDir,
 				"runtime");
 		copyErrorXmlToProjectRoot("ErrorData2_QA.xml", rootDir, "security");
 		copyErrorPropertiesToProjectRoot("QAErrors2.properties", rootDir,
 				"security");
 
 		File propFile = createDomainPropertiesFile(rootDir, "TestErrorLibrary");
 		Properties props = new Properties();
 		props.setProperty("listOfDomains", "runtime,security");
 		storeProps(propFile, props);
 
 		performDirectCodeGen(inputArgs);
 		
 		// Assert Runtime Domain Generation
 		ExpectedErrors expectedRuntimeErrors = new ExpectedErrors();
 		expectedRuntimeErrors.setExpectedErrorDomain("runtime");
 		expectedRuntimeErrors.setExpectedOrganization("eBay");
 		expectedRuntimeErrors.setExpectedPackageName("org.suhua.errorlibrary.runtime");
 		expectedRuntimeErrors.setErrorProperties("QAErrors2.properties");
 		expectedRuntimeErrors.add(1002L,"new_error1",ERROR,SYSTEM,"runtime","System",null);
 		expectedRuntimeErrors.add(1003L,"new_error2",WARNING,APPLICATION,"runtime","System",null);
 
 		assertErrorConstants(destDir, expectedRuntimeErrors);
 		assertErrorDataCollection(destDir, expectedRuntimeErrors);
 
 		// Assert Security Domain Generation
 		ExpectedErrors expectedSecurityErrors = new ExpectedErrors();
 		expectedSecurityErrors.setExpectedErrorDomain("security");
 		expectedSecurityErrors.setExpectedOrganization("eBay");
 		expectedSecurityErrors.setExpectedPackageName("org.suhua.errorlibrary.security");
 		expectedSecurityErrors.setErrorProperties("QAErrors2.properties");
 		expectedSecurityErrors.add(1002L,"new_error1",ERROR,SYSTEM,"security","System",null);
 		expectedSecurityErrors.add(1003L,"new_error2",WARNING,APPLICATION,"security","System",null);
 
 		assertErrorConstants(destDir, expectedSecurityErrors);
 		assertErrorDataCollection(destDir, expectedSecurityErrors);
 	}
 
 	@Test
 	public void testCommandLineGentypeWithoutDest() throws Exception {
 		testingdir.ensureEmpty();
 		File rootDir = testingdir.getDir();
 		File destDir = new File(rootDir, "gen-src");
 		MavenTestingUtils.ensureEmpty(destDir);
 
 		// @formatter:off
 		String[] inputArgs = { 
 			"-gentype", "genTypeCommandLineAll",
 			"-errorlibname","TestErrorLibrary",
 			"-pr", rootDir.getAbsolutePath()
 		};
 		// @formatter:on
 
 		copyErrorXmlToProjectRoot("ErrorData2_QA_Runtime.xml", rootDir, "runtime");
 		copyErrorPropertiesToProjectRoot("QAErrors2.properties", rootDir,
 				"runtime");
 		copyErrorXmlToProjectRoot("ErrorData2_QA.xml", rootDir, "security");
 		copyErrorPropertiesToProjectRoot("QAErrors2.properties", rootDir,
 				"security");
 		
 		File propFile = createDomainPropertiesFile(rootDir, "TestErrorLibrary");
 		Properties props = new Properties();
 		props.setProperty("listOfDomains", "runtime,security");
 		storeProps(propFile, props);
 
 		performDirectCodeGen(inputArgs);
 		
 		// Assert Runtime Domain Generation
 		ExpectedErrors expectedRuntimeErrors = new ExpectedErrors();
 		expectedRuntimeErrors.setExpectedErrorDomain("runtime");
 		expectedRuntimeErrors.setExpectedOrganization("eBay");
 		expectedRuntimeErrors.setExpectedPackageName("org.suhua.errorlibrary.runtime");
 		expectedRuntimeErrors.setErrorProperties("QAErrors2.properties");
 		expectedRuntimeErrors.add(1002L,"new_error1",ERROR,SYSTEM,"runtime","System",null);
 		expectedRuntimeErrors.add(1003L,"new_error2",WARNING,APPLICATION,"runtime","System",null);
 
 		assertErrorConstants(destDir, expectedRuntimeErrors);
 		assertErrorDataCollection(destDir, expectedRuntimeErrors);
 
 		// Assert Security Domain Generation
 		ExpectedErrors expectedSecurityErrors = new ExpectedErrors();
 		expectedSecurityErrors.setExpectedErrorDomain("security");
 		expectedSecurityErrors.setExpectedOrganization("eBay");
 		expectedSecurityErrors.setExpectedPackageName("org.suhua.errorlibrary.security");
 		expectedSecurityErrors.setErrorProperties("QAErrors2.properties");
 		expectedSecurityErrors.add(1002L,"new_error1",ERROR,SYSTEM,"security","System",null);
 		expectedSecurityErrors.add(1003L,"new_error2",WARNING,APPLICATION,"security","System",null);
 
 		assertErrorConstants(destDir, expectedSecurityErrors);
 		assertErrorDataCollection(destDir, expectedSecurityErrors);
 	}
 
 	@Test
 	public void testCommandLineGentypeWithoutPR() throws Exception {
 
 		File rootDir = testingdir.getDir();
 		File destDir = new File(rootDir, "gen-src");
 		MavenTestingUtils.ensureEmpty(destDir);
 
 		// @formatter:off
 		String[] inputArgs = { 
 			"-gentype", "genTypeCommandLineAll",
 			"-errorlibname","TestErrorLibrary",
 		};
 		// @formatter:on
 
 		// Empty properties file
 		createTestSpecificPropFile("testCommandLineGentypeWithoutPR");
 
 		try {
 			performDirectCodeGen(inputArgs);
 			Assert.fail("Expected an Exception");
 			Assert.fail("Expected an Exception of type: " + MissingInputOptionException.class.getName());
 		} catch (MissingInputOptionException e) {
 			String expected = "Project Root is missing. Please provide the value for this option -pr ";
 			Assert.assertThat(e.getMessage(), containsString(expected));
 		}
 	}
 
 	/**
 	 * Class files are generated under the same dir as java files
 	 * 
 	 * @param javaFile the java file to compile.
 	 * @throws Exception
 	 */
 	private void compileJavaFiles(File binDir, File javaFile) throws Exception {
 		PathAssert.assertFileExists("Java Source", javaFile);
 		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
 		LinkedList<File> classpath = ClassPathUtil.getClassPath();
 		classpath.add(0, binDir);
 		StringBuilder cp = new StringBuilder();
 		ClassPathUtil.appendClasspath(cp, classpath);
 		if (compiler.run(null, System.out, System.err, "-cp", cp.toString(), javaFile.getAbsolutePath()) != 0) {
 			throw new Exception("Exception while compiling file");
 		}
 	}
 	
 	private Class<?> loadTestProjectClass(String classname, File ... srcDirs) throws Exception {
 		URL urls[] = new URL[srcDirs.length];
 		for(int i=0; i<srcDirs.length; i++) {
 			urls[i] = srcDirs[i].toURI().toURL();
 		}
 		URLClassLoader classloader = new URLClassLoader(urls);
 		return classloader.loadClass(classname);
 	}
 
 	class ExpectedErrors {
 		private Map<Long,CommonErrorData> expected = new HashMap<Long, CommonErrorData>();
 		private String expectedPackageName;
 		private String expectedOrganization;
 		private String expectedErrorDomain;
 		private Properties errorProperties;
 		private String errorPropertiesFilename;
 		
 		public Properties getErrorProperties() {
 			return errorProperties;
 		}
 
 		public void setErrorProperties(String propertyFilename) throws IOException {
 			File propFile = TestResourceUtil.getResource(
 					"org/ebayopensource/turmeric/test/tools/codegen/data/" + propertyFilename);
 			errorPropertiesFilename = propFile.getAbsolutePath();
 			this.errorProperties = loadProperties(propFile);
 		}
 
 		public String getExpectedPackageName() {
 			return expectedPackageName;
 		}
 
 		public void setExpectedPackageName(String expectedPackageName) {
 			this.expectedPackageName = expectedPackageName;
 		}
 
 		public String getExpectedOrganization() {
 			return expectedOrganization;
 		}
 
 		public void setExpectedOrganization(String expectedOrganization) {
 			this.expectedOrganization = expectedOrganization;
 		}
 		
 		public String getExpectedErrorDomain() {
 			return expectedErrorDomain;
 		}
 
 		public void setExpectedErrorDomain(String expectedErrorDomain) {
 			this.expectedErrorDomain = expectedErrorDomain;
 		}
 
 		public void add(long errorId, 
 				String errorName, 
 				ErrorSeverity severity, 
 				ErrorCategory category,
 				String domain, String subDomain, 
 				String errorGroup) {
 			CommonErrorData ced = new CommonErrorData();
 			ced.setErrorId(errorId);
 			ced.setErrorName(errorName);
 			ced.setSeverity(severity);
 			ced.setCategory(category);
 			ced.setDomain(domain);
 			ced.setSubdomain(subDomain);
 			ced.setErrorGroups(errorGroup);
 			expected.put(errorId, ced);
 		}
 		
 		public CommonErrorData get(long errorId) {
 			return expected.get(errorId);
 		}
 		
 		public int getCount() {
 			return expected.size();
 		}
 		
 		public Map<Long, CommonErrorData> getExpected() {
 			return expected;
 		}
 
 		public Collection<CommonErrorData> getExpectedErrors() {
 			return expected.values();
 		}
 
 		public void assertErrorMessageExists(String fieldname) {
 			if(errorProperties == null) {
 				return; // skip, do not validate against error properties.
 			}
 			
 			String propkey = fieldname.toLowerCase() + ".message";
 			Assert.assertTrue("Unable to find error message \"" + propkey + 
 					"\" in properties file: " + errorPropertiesFilename
 					, errorProperties.containsKey(propkey));
 		}
 	}
 	
 	public void assertErrorConstants(File srcDir, 
 			ExpectedErrors expectedErrors) throws Exception
 	{
 		String classname = expectedErrors.expectedPackageName + ".ErrorConstants";
 		File javaFile = CodeGenAssert.assertJavaSourceExists(srcDir, classname);
 		compileJavaFiles(srcDir, javaFile);
 		
 		Class<?> actualClass = loadTestProjectClass(classname, srcDir);
 		
 		CodeGenAssert.assertClassPackage(actualClass, expectedErrors.expectedPackageName);
 		CodeGenAssert.assertClassName(actualClass, "ErrorConstants");
 		CodeGenAssert.assertClassIsPublic(actualClass);
 		
 		// To store found field names for later assertion.
 		List<String> fieldnames = new ArrayList<String>();
 
 		// Walk fields
 		for(Field f: actualClass.getFields()) {
 			String fieldname = f.getName();
 			
 			CodeGenAssert.assertFieldIsPublicStaticFinal(f);
 
 			if(fieldname.equals("ERRORDOMAIN")) {
 				CodeGenAssert.assertFieldType(f, String.class);
 				String value = (String) f.get(null);
 				Assert.assertThat("ERRORDOMAIN", value, is(expectedErrors.expectedErrorDomain));
 				continue;
 			}
 			
 			fieldnames.add(fieldname);
 			expectedErrors.assertErrorMessageExists(fieldname);
 		}
 		
 		// Validate that all expected fields exist.
 		for(CommonErrorData expected: expectedErrors.getExpectedErrors())
 		{
 			String expectedName = expected.getErrorName().toLowerCase();
 			Assert.assertThat("Expected field exists: " + expectedName,
 					fieldnames, hasItem(expectedName.toUpperCase()));
 		}
 		
 		Assert.assertThat("Field Count", fieldnames.size(), is(expectedErrors.getCount()));
 	}
 	
 	public void assertErrorDataCollection(File srcDir, 
 			ExpectedErrors expectedErrors) throws Exception
 	{
 		String classname = expectedErrors.expectedPackageName + ".ErrorDataCollection";
 		File javaFile = CodeGenAssert.assertJavaSourceExists(srcDir, classname);
 		compileJavaFiles(srcDir, javaFile);
 		
 		Class<?> actualClass = loadTestProjectClass(classname, srcDir);
 		
 		CodeGenAssert.assertClassPackage(actualClass, expectedErrors.expectedPackageName);
 		CodeGenAssert.assertClassName(actualClass, "ErrorDataCollection");
 		CodeGenAssert.assertClassIsPublic(actualClass);
 
 		// To store found fields for later assertion.
 		Map<String, CommonErrorData> fields = new HashMap<String, CommonErrorData>();
 		
 		// Walk fields
 		for(Field f: actualClass.getFields()) {
 			String fieldname = f.getName();
 			
 			if(fieldname.equals("ORGANIZATION")) {
 				CodeGenAssert.assertFieldIsPrivateStaticFinal(f);
 				CodeGenAssert.assertFieldType(f, String.class);
 				f.setAccessible(true); // its private after all
 				String value = (String) f.get(null);
 				Assert.assertThat("ORGANIZATION", value, is(expectedErrors.expectedOrganization));
 				continue;
 			}
 			
 			CodeGenAssert.assertFieldIsPublicStaticFinal(f);
 			if(CommonErrorData.class.isAssignableFrom(f.getType())) {
 				CommonErrorData ced = (CommonErrorData) f.get(null);
 				fields.put(fieldname, ced);
 				expectedErrors.assertErrorMessageExists(fieldname);
 			}
 		}
 
 		// Validate that all expected fields exist.
 		for(CommonErrorData expected: expectedErrors.getExpectedErrors())
 		{
 			String expectedName = expected.getErrorName().toLowerCase();
 			Assert.assertThat("Expected field exists: " + expectedName,
 					fields.keySet(), hasItem(expectedName));
 			CommonErrorData actualError = fields.get(expectedName);
 			Assert.assertNotNull("Actual CommonErrorData found in class", actualError);
 			Assert.assertThat("CommonErrorData.errorId", 
 					actualError.getErrorId(), is(expected.getErrorId()));
 			Assert.assertThat("CommonErrorData.errorName", 
 					actualError.getErrorName(), is(expected.getErrorName()));
 			Assert.assertThat("CommonErrorData.severity", 
 					actualError.getSeverity(), is(expected.getSeverity()));
 			Assert.assertThat("CommonErrorData.category", 
 					actualError.getCategory(), is(expected.getCategory()));
 			Assert.assertThat("CommonErrorData.domain", 
 					actualError.getDomain(), is(expected.getDomain()));
 			Assert.assertThat("CommonErrorData.subdomain", 
 					actualError.getSubdomain(), is(expected.getSubdomain()));
 			Assert.assertThat("CommonErrorData.errorGroups", 
 					actualError.getErrorGroups(), is(expected.getErrorGroups()));
 		}
 		
 		Assert.assertThat("Field Count", fields.size(), is(expectedErrors.getCount()));
 	}
 	
 	public static void copyErrorXmlToProjectRoot(String sourceFileName, File projectRoot, String domain) throws Exception {
 		File sourceFile = TestResourceUtil.getResource("org/ebayopensource/turmeric/test/tools/codegen/data/" + sourceFileName);
 		String path = "meta-src/META-INF/errorlibrary/" + domain + "/ErrorData.xml";
 		File outputFile = new File(projectRoot, FilenameUtils.separatorsToSystem(path));
 		MavenTestingUtils.ensureDirExists(outputFile.getParentFile());
 		FileUtils.copyFile(sourceFile, outputFile);
 	}
 
 	public static void copyErrorPropertiesToProjectRoot(String sourceFileName, File projectRoot, String domain) throws Exception {
 		File sourceFile = TestResourceUtil.getResource("org/ebayopensource/turmeric/test/tools/codegen/data/" + sourceFileName);
 		String path = "meta-src/META-INF/errorlibrary/" + domain + "/Errors_en_US.properties";
 		File outputFile = new File(projectRoot, FilenameUtils.separatorsToSystem(path));
 		MavenTestingUtils.ensureDirExists(outputFile.getParentFile());
 		FileUtils.copyFile(sourceFile, outputFile);
 	}
 }
