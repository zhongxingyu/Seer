 /*******************************************************************************
  * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *******************************************************************************/
 package org.ebayopensource.turmeric.tools.library;
 
 import static org.hamcrest.Matchers.*;
 
 import java.io.File;
 
 import org.ebayopensource.turmeric.junit.utils.MavenTestingUtils;
 import org.ebayopensource.turmeric.tools.GeneratedAssert;
 import org.ebayopensource.turmeric.tools.TestResourceUtil;
 import org.ebayopensource.turmeric.tools.codegen.AbstractServiceGeneratorTestCase;
 import org.ebayopensource.turmeric.tools.codegen.exception.BadInputOptionException;
 import org.ebayopensource.turmeric.tools.codegen.exception.BadInputValueException;
 import org.ebayopensource.turmeric.tools.codegen.exception.MissingInputOptionException;
 import org.junit.Assert;
 import org.junit.Assume;
 import org.junit.Test;
 
 
 public class TypeLibraryCodegenInterfaceTest extends AbstractServiceGeneratorTestCase { 
 	@Test
 	public void typeLibraryValidationsProjectEmpty() throws Exception{
         // @formatter:off
 		String pluginParameters[] = {
 			"-gentype", "genTypeAddType", 
 			"-pr","",
 			"-libname", "SampleTestLibrary", 
 			"-type", "aluconfigType.xsd"
 		};
         // @formatter:on
 		
 		try {
 			performDirectCodeGen(pluginParameters);
 			Assert.fail("Expecting exception of type: " + BadInputValueException.class.getName());
 		} catch(BadInputValueException e){
 			Assert.assertThat(e.getMessage(), containsString("provide a proper value for the option -pr"));
 		}
 	}
 	
 	@Test
 	public void typeLibraryValidationsProjectRootNull() throws Exception{
         // @formatter:off
 		String pluginParameters[] = {
 			"-gentype", "genTypeAddType", 
 			"-pr", /* null */
 			"-libname", "SampleTestLibrary", 
 			"-type", "aluconfigType.xsd"
 		};
         // @formatter:on
 		
 		try {
 			performDirectCodeGen(pluginParameters); 
 			Assert.fail("Expecting exception of type: " + BadInputValueException.class.getName());
 		} catch(BadInputValueException e){
 			Assert.assertThat(e.getMessage(), containsString("provide a value for the option -pr"));
 		}
 	}
 
 	@Test
 	public void typeLibraryValidationsLibraryNameNull() throws Exception{
 		File rootDir = testingdir.getDir(); // was ".\\projectRoot"
 
         // @formatter:off
 		String pluginParameters[] = {
 			"-gentype", "genTypeCreateTypeLibrary", 
 			"-pr", rootDir.getAbsolutePath(),
 			"-libname", /* null */
 			"-type", "aluconfigType.xsd"
 		};
         // @formatter:on
 		
 		try {
 			performDirectCodeGen(pluginParameters);
 			Assert.fail("Expecting exception of type: " + BadInputValueException.class.getName());
 		} catch(BadInputValueException e){
 			Assert.assertThat(e.getMessage(), containsString("provide a value for the option -libname"));
 		}
 	}
 	
 	
 	@Test
 	public void typeLibraryValidationsTypeNull() throws Exception{
 		File rootDir = testingdir.getDir(); // was ".\\projectRoot"
 
 		// @formatter:off
 		String pluginParameters[] = {
 			"-gentype", "genTypeCreateTypeLibrary", 
 			"-pr", rootDir.getAbsolutePath(),
 			"-libname", "TestLibrary",
 			"-type" /* null */
 		};
 		// @formatter:on
 		
 		try {
 			performDirectCodeGen(pluginParameters);
 			Assert.fail("Expecting exception of type: " + BadInputValueException.class.getName());
 		} catch(BadInputValueException e){
 			Assert.assertThat(e.getMessage(), containsString("Missing parameter for '-type' option"));
 		}
 	}
 	
 	@Test
 	public void typeLibraryValidationsLibraryVersionNull() throws Exception{
 		File rootDir = testingdir.getDir(); // was ".\\projectRoot"
 
         // @formatter:off
 		String pluginParameters[] = {
 			"-gentype", "genTypeCreateTypeLibrary", 
 			"-pr", rootDir.getAbsolutePath(),
 			"-libVersion", /* null */
 			"-libname", "TestLibrary"
 		};
         // @formatter:on
 		
 		try {
 			performDirectCodeGen(pluginParameters);
 			Assert.fail("Expecting exception of type: " + BadInputValueException.class.getName());
 		} catch(BadInputValueException e){
 			Assert.assertThat(e.getMessage(), containsString("provide a value for the option -libversion"));
 		}
 	}
 	
 	@Test
 	public void typeLibraryValidationsLibraryNamespaceNull() throws Exception{
 		File rootDir = testingdir.getDir(); // was ".\\projectRoot"
 
 		// @formatter:off
 		String pluginParameters[] = {
 			"-gentype", "genTypeCreateTypeLibrary", 
 			"-pr", rootDir.getAbsolutePath(),
 			"-libNamespace", /* null */ 
 			"-libname", "TestLibrary"
 		};
 		// @formatter:on
 
 		
 		try {
 			performDirectCodeGen(pluginParameters); 
 			Assert.fail("Expecting exception of type: " + BadInputValueException.class.getName());
 		} catch(BadInputValueException e){
 			Assert.assertThat(e.getMessage(), containsString("provide a value for the option -libnamespace"));
 		}
 	}
 
 	@Test
 	public void typeLibraryValidationsLibraryCategoryNull() throws Exception{
 		File rootDir = testingdir.getDir(); // was ".\\projectRoot"
 
 		// @formatter:off
         String pluginParameters[] = {
 			"-gentype", "genTypeCreateTypeLibrary", 
 			"-pr",rootDir.getAbsolutePath(),
 			"-libCategory", /* null */ 
 			"-libname", "TestLibrary"
 		};
         // @formatter:on
 		
 		try {
 			performDirectCodeGen(pluginParameters);
 			Assert.fail("Expecting exception of type: " + BadInputValueException.class.getName());
 		} catch(BadInputValueException e){
 			Assert.assertThat(e.getMessage(), containsString("provide a value for the option -libcategory"));
 		}
 	}
 
 	@Test
 	public void typeLibraryValidationsNoProjectRoot() throws Exception{
 		// @formatter:off
         String pluginParameters[] = {
 			"-gentype", "genTypeAddType", 
 			"-libname", "SampleTestLibrary", 
 			"-type", "aluconfigType.xsd"
 		};
         // @formatter:on
 
 		try {
 			performDirectCodeGen(pluginParameters);
 			Assert.fail("Expecting exception of type " + MissingInputOptionException.class.getName());
 		} catch(MissingInputOptionException e){
 			Assert.assertThat(e.getMessage(), containsString("Project Root is missing"));
 		}
 	}
 	
 	@Test
 	public void typeLibraryValidationsLibraryEmpty() throws Exception {
 		MavenTestingUtils.ensureEmpty(testingdir.getDir());
 		
 		File rootDir = testingdir.getDir(); // was "SampleTestLibrary"
 
 		// @formatter:off
         String pluginParameters[] = {
 			"-gentype", "genTypeAddType", 
 			"-pr", rootDir.getAbsolutePath(),
 			"-libname", "SampleTestLibrary", 
 			"-type", "aluconfigType.xsd"
 		};
         // @formatter:on
 
 		MavenTestingUtils.ensureDirExists(testingdir.getFile("gen-src"));
 		TestResourceUtil.copyResource("META-INF/SampleTestLibrary/TypeInformation.xml", testingdir, "gen-meta-src");
 		TestResourceUtil.copyResource("types/aluconfigType.xsd", testingdir, "meta-src");
 		
 		performDirectCodeGen(pluginParameters);
 	}
 	
 	@Test
 	public void typeLibraryValidationsLibraryNull() throws Exception {
 		File rootDir = testingdir.getDir(); // was "SampleTestLibrary"
 
 		// @formatter:off
         String pluginParameters[] = {
 			"-gentype", "genTypeAddType", 
 			"-pr", rootDir.getAbsolutePath(),
 			"-type", "aluconfigType.xsd"
 		};
         // @formatter:on
 
 		
 		try {
 			performDirectCodeGen(pluginParameters);
 			Assert.fail("Expecting exception of type " + MissingInputOptionException.class.getName());
 		} catch(MissingInputOptionException e){
 			Assert.assertThat(e.getMessage(), containsString("TypeLibrary Name is missing"));
 		}
 	}
 	
 	@Test
 	public void typeLibraryValidationsInvalidOption() throws Exception{
 		File rootDir = testingdir.getDir(); // was "SampleTestLibrary"
 
 		// @formatter:off
         String pluginParameters[] = {
 			"-gentype", "genTypeAddType", 
 			"-pr", rootDir.getAbsolutePath(),
 			"-type", "aluconfigType.xsd",
 			"-MyOwnOption","dummy"
 		};
         // @formatter:on
 
 		try {
 			performDirectCodeGen(pluginParameters);
 			Assert.fail("Expecting exception of type: " + BadInputOptionException.class.getName());
 		} catch(BadInputOptionException e){
 			Assert.assertThat(e.getMessage(), containsString("Invalid option -myownoption specified"));
 		}
 	}
 	
 	@Test
 	public void cleanBuildTypeLibrary() throws Exception{
 		MavenTestingUtils.ensureEmpty(testingdir.getDir());
 		
 		File rootDir = testingdir.getDir(); // was ".\\..\\..\\v4soa\\DevServices\\HardwareTypeLibrary"
 
 		// For HardwareTypeLibrary
 		// @formatter:off
         String pluginParametersCreate[] = {
 			"-gentype", "genTypeCreateTypeLibrary", 
 			"-pr", rootDir.getAbsolutePath(),
 			"-libname", "HardwareTypeLibrary",
 			"-libNamespace","http://www.ebayopensource.org/turmeric/common/v1/types"
 		};
         // @formatter:on
 
 		
 		String pluginParametersClean[] = {
 			"-gentype", "genTypeCleanBuildTypeLibrary", 
 			"-pr", rootDir.getAbsolutePath(), 
 			"-libname", "HardwareTypeLibrary"
 		};
 		
 		performDirectCodeGen(pluginParametersCreate);
 		performDirectCodeGen(pluginParametersClean);
 	}
 	
 	@Test
 	public void incrBuildTypeLibrary() throws Exception{
 		MavenTestingUtils.ensureEmpty(testingdir.getDir());
 		
 		File rootDir = testingdir.getDir(); // was ".\\..\\..\\v4soa\\DevServices\\HardwareTypeLibrary"
 		
 		MavenTestingUtils.ensureDirExists(testingdir.getFile("gen-src"));
 		TestResourceUtil.copyResource("META-INF/HardwareTypeLibrary/TypeInformation.xml", testingdir, "gen-meta-src");
 		TestResourceUtil.copyResource("types/monresType.xsd", testingdir, "meta-src");
 		TestResourceUtil.copyResource("types/ramType.xsd", testingdir, "meta-src");
 		
 		// @formatter:off
         String pluginParameters[] = {
 			"-gentype", "genTypeIncrBuildTypeLibrary", 
 			"-pr", rootDir.getAbsolutePath(), 
 			"-libname", "HardwareTypeLibrary", 
 			"-type", "ramType.xsd", 
 			"-type", "monresType.xsd"
 		};
         // @formatter:on
 
 
 		performDirectCodeGen(pluginParameters);
 	}
 	
 	@Test
 	public void addTypeWithNewSchemaStructure() throws Exception {
 		MavenTestingUtils.ensureEmpty(testingdir.getDir());
 		
 		File rootDir = testingdir.getDir(); // was ".\\tmp\\TypepLibraryTestCases\\TypeLibC"
 
 		// @formatter:off
         String pluginParameters[] = { 
 			"-gentype", "genTypeAddType", 
 			"-pr", rootDir.getAbsolutePath(), 
 			"-libname", "TypeLibC", 
 			"-type", "C1.xsd" 
 		};
         // @formatter:on
 
 		MavenTestingUtils.ensureDirExists(testingdir.getFile("gen-src"));
 		TestResourceUtil.copyResource("META-INF/TypeLibC/TypeInformation.xml", testingdir, "gen-meta-src");
 		TestResourceUtil.copyResource("types/TypeLibC/C1.xsd", testingdir, "meta-src");
 		TestResourceUtil.copyResource("types/TypeLibA/A4.xsd", testingdir, "meta-src");
 		
 		performDirectCodeGen(pluginParameters);
 	}
 	
 	@Test
 	public void testCleanBuildGenTypeWithNewSchemaStructure() throws Exception {
 		MavenTestingUtils.ensureEmpty(testingdir.getDir());
 		
 		File rootDir = testingdir.getDir(); // was ".\\tmp\\TypepLibraryTestCases\\TypeLibC"
 		MavenTestingUtils.ensureDirExists(testingdir.getFile("gen-src"));
 		TestResourceUtil.copyResource("META-INF/TypeLibC/TypeInformation.xml", testingdir, "gen-meta-src");
 		TestResourceUtil.copyResource("types/TypeLibC/C1.xsd", testingdir, "meta-src");
 		TestResourceUtil.copyResource("types/TypeLibA/A4.xsd", testingdir, "meta-src");
 
 		// @formatter:off
         String pluginParameters[] = { 
 			"-gentype", "genTypeAddType", 
 			"-pr", rootDir.getAbsolutePath(), 
 			"-libname", "TypeLibC", 
 			"-type", "C1.xsd" 
 		};
         // @formatter:on
 
 
 		// @formatter:off
         String pluginParametersForCleanBuild[] = { 
 			"-gentype", "genTypeCleanBuildTypeLibrary", 
 			"-pr", rootDir.getAbsolutePath(), 
 			"-libname", "TypeLibC"  
 		};
         // @formatter:on
 
 		
 		performDirectCodeGen(pluginParameters);
 		performDirectCodeGen(pluginParametersForCleanBuild);
 	}
 	
 	@Test
 	public void testDeleteGenTypeWithNewSchemaStructure() throws Exception {
 		MavenTestingUtils.ensureEmpty(testingdir.getDir());
 		
 		File rootDir = testingdir.getDir(); // was ".\\tmp\\TypepLibraryTestCases\\TypeLibC"
 		MavenTestingUtils.ensureDirExists(testingdir.getFile("gen-src"));
 		TestResourceUtil.copyResource("META-INF/TypeLibC/TypeInformation.xml", testingdir, "gen-meta-src");
 		TestResourceUtil.copyResource("types/TypeLibC/C1.xsd", testingdir, "meta-src");
 		TestResourceUtil.copyResource("types/TypeLibA/A4.xsd", testingdir, "meta-src");
 
 		// @formatter:off
         String pluginParameters[] = { 
 			"-gentype", "genTypeAddType", 
 			"-pr", rootDir.getAbsolutePath(), 
 			"-libname", "TypeLibC", 
 			"-type", "C1.xsd" 
 		};
         // @formatter:on
 
 		
 		// @formatter:off
        String pluginParametersForDeleteType[] = { 
 			"-gentype", "genTypeDeleteType", 
 			"-pr", rootDir.getAbsolutePath(), 
 			"-libname", "TypeLibC", 
 			"-type", "D1.xsd" 
 		};
         // @formatter:on
 
 		
 		performDirectCodeGen(pluginParameters);
		performDirectCodeGen(pluginParametersForDeleteType);
 	}
 	
 	@Test
 	public void testAddTypeWithTypesInNewStrucutreAndImportedFromAnother() throws Exception {
 		MavenTestingUtils.ensureEmpty(testingdir.getDir());
 		
 		File rootDir = testingdir.getDir(); // was ".\\tmp\\TypepLibraryTestCases\\TypeLibB"
 		MavenTestingUtils.ensureDirExists(testingdir.getFile("gen-src"));
 		TestResourceUtil.copyResource("META-INF/TypeLibB/TypeInformation.xml", testingdir, "gen-meta-src");
 		TestResourceUtil.copyResource("types/TypeLibB/B5.xsd", testingdir, "meta-src");
 		TestResourceUtil.copyResource("types/TypeLibA/A6.xsd", testingdir, "meta-src");
 
 		// @formatter:off
         String pluginParameters[] = { 
 			"-gentype", "genTypeAddType", 
 			"-pr", rootDir.getAbsolutePath(), 
 			"-libname", "TypeLibB", 
 			"-type", "B5.xsd" 
 		};
         // @formatter:on
 
 		
 		performDirectCodeGen(pluginParameters);
 	}
 	
 	@Test
 	public void addTypeLibrary() throws Exception{
 		MavenTestingUtils.ensureEmpty(testingdir.getDir());
 		
 		File rootDir = testingdir.getDir(); // was ".\\..\\..\\v4soa\\DevServices\\HardwareTypeLibrary"
 		MavenTestingUtils.ensureEmpty(new File(rootDir, "gen-src"));
 
 		// @formatter:off
         String pluginParameters[] = {
 			"-gentype", "genTypeAddType", 
 			"-pr", rootDir.getAbsolutePath(), 
 			"-libname", "HardwareTypeLibrary", 
 			"-type", "ramType.xsd"
 		};
         // @formatter:on
 
 		
 		TestResourceUtil.copyResource("META-INF/HardwareTypeLibrary/TypeInformation.xml", testingdir, "gen-meta-src");
 		
 		// @formatter:off
         String xsds[] = { 
 			"ramType.xsd", 
 			"hardwareType.xsd",
 			"rammemorydetailsType.xsd",
 			"speeddetailsramType.xsd"
 		};
         // @formatter:on
 
 		for(String xsd: xsds) {
 			TestResourceUtil.copyResource("types/" + xsd, testingdir, "meta-src");
 		}
 		
 		performDirectCodeGen(pluginParameters);
 	}
 	
 	@Test
 	public void checkObjectFactoryDeletioninTypelibrary() throws Exception {
 		MavenTestingUtils.ensureEmpty(testingdir.getDir());
 		
 		File rootDir = testingdir.getDir(); // was ".\\..\\..\\v4soa\\DevServices\\T1"
 		MavenTestingUtils.ensureEmpty(new File(rootDir, "gen-src"));
 		MavenTestingUtils.ensureEmpty(new File(rootDir, "gen-meta-src"));
 		
 		// @formatter:off
         String[] pluginParameters = { 
             "-libName","T1", 
             "-classPathToXJC", rootDir.getAbsolutePath(), 
             "-gentype", "genTypeCleanBuildTypeLibrary", 
             "-pr", rootDir.getAbsolutePath()
 		};
         // @formatter:on
 
 		performDirectCodeGen(pluginParameters);
 		
 		// Ensure generated content exists
 		GeneratedAssert.assertPathNotExists(rootDir, "gen-src/com/ebay/marketplace/services/test1/ObjectFactory.java");
 		GeneratedAssert.assertPathNotExists(rootDir, "gen-src/com/ebay/marketplace/services/test1/package-info.java");
 	}
 
 	@Test
 	public void addTypeWithUnderscoreCase1() throws Exception {
 		MavenTestingUtils.ensureEmpty(testingdir.getDir());
 		
 		File rootDir = testingdir.getDir(); // was ".\\tmp\\TypepLibraryTestCases\\TypeLib"
 		MavenTestingUtils.ensureEmpty(new File(rootDir, "gen-src"));
 		MavenTestingUtils.ensureEmpty(new File(rootDir, "gen-meta-src"));
 		
 		// @formatter:off
         String pluginParameters[] = { 
 			"-gentype", "genTypeAddType", 
 			"-pr", rootDir.getAbsolutePath(), 
 			"-libname", "TypeLib", 
 			"-type", "____r__am__Ty__pe_____.xsd" 
 		};
         // @formatter:on
 		
 		TestResourceUtil.copyResource("META-INF/TypeLib/TypeInformation.xml", testingdir, "gen-meta-src");
 		TestResourceUtil.copyResource("types/____r__am__Ty__pe_____.xsd", testingdir, "meta-src");
 
 		performDirectCodeGen(pluginParameters);
 	}
 	
 	@Test
 	public void genTypeCreateTypeLibrary() throws Exception{
 		File rootDir = testingdir.getDir(); // was ".\\..\\..\\v4soa\\DevServices\\SampleTestLibrary"
 		
 		// @formatter:off
         String pluginParameters[] = {
 			"-gentype", "genTypeCreateTypeLibrary", 
 			"-pr", rootDir.getAbsolutePath(),
 			"-libname", "SampleTestLibrary",
 			"-libNamespace","http://www.ebay.com/soaframework/examples/config"
 		};
         // @formatter:on
 
 		performDirectCodeGen(pluginParameters);
 	}
 	
 	@Test
 	public void genTypeCreateTypeLibraryProperVersion() throws Exception{
 		File rootDir = testingdir.getDir(); // was ".\\..\\..\\v4soa\\DevServices\\SampleTestLibrary"
 		
 		String version = "1.0.2";
 
 		// @formatter:off
         String pluginParameters[] = {
 			"-gentype", "genTypeCreateTypeLibrary", 
 			"-pr",rootDir.getAbsolutePath(),
 			"-libname", "SampleTestLibrary",
 			"-libNamespace","http://www.ebay.com/soaframework/examples/config",
 			"-libVersion",version
 		};
         // @formatter:on
 
 		
 		
 		performDirectCodeGen(pluginParameters);
 		//TODO use jaxb to read the typeinformation and verify the version 
 	}
 	
 	@Test
 	public void genTypeCreateTypeLibraryImProperVersion() throws Exception{
 		
 		String version = "1.0.2.4"; // Intentionally invalid version (X.Y.Z is proper)
 		
 		// @formatter:off
         String pluginParameters[] = {
 			"-gentype", "genTypeCreateTypeLibrary", 
 			"-pr", testingdir.getDir().getAbsolutePath(),
 			"-libname", "SampleTestLibrary",
 			"-libNamespace", "http://www.ebay.com/soaframework/examples/config",
 			"-libVersion", version
 		};
         // @formatter:on
 
 		try {
 			performDirectCodeGen(pluginParameters);
 			Assert.fail("Expecting exception of type: " + BadInputValueException.class.getName());
 		} catch(BadInputValueException e){
 			Assert.assertThat(e.getMessage(), containsString("The Library version should be in the format X.Y.Z"));
 		}
 	}
 	
 	@Test
 	//v4 tests to be cleaned up - Will be deleted
 	public void genTypeV4_failure_case_mandatory_params_absent_wsdl() throws Exception{
 		assumeV4TypeMappingGeneratorPresent();
 		
 		// @formatter:off
         String pluginParameters[] = {
             "-gentype", "V4"
 		};
         // @formatter:on
 		
 		try {
 			performDirectCodeGen(pluginParameters);
 			Assert.fail("Expecting exception of type " + MissingInputOptionException.class.getName());
 		} catch(MissingInputOptionException e){
 			Assert.assertThat(e.getMessage(), containsString("absent wsdl"));
 		}
 	}
 	
 	@Test
 	//v4 tests to be cleaned up - Will be deleted
 	public void genTypeV4_failure_case_mandatory_params_wsdl_not_exist() throws Exception{
 		assumeV4TypeMappingGeneratorPresent();
 		
 		MavenTestingUtils.ensureEmpty(testingdir.getDir());
 		File boguswsdl = testingdir.getFile("bogus-and-doesnt-exist.wsdl");
 		
 		// @formatter:off
         String pluginParameters[] = {
 			"-gentype", "V4",
 			"-wsdl",boguswsdl.getAbsolutePath()
 		};
         // @formatter:on
 		
 		try {
 			performDirectCodeGen(pluginParameters);
 			Assert.fail("Expecting exception of type: " + BadInputValueException.class.getName());
 		} catch(BadInputValueException e){
 			Assert.assertThat(e.getMessage(), containsString("wsdl not found"));
 		}
 	}
 	
 	@Test
 	//v4 tests to be cleaned up - Will be deleted
 	public void genTypeV4_failure_case_mandatory_params_dest_absent() throws Exception{
 		assumeV4TypeMappingGeneratorPresent();
 		File wsdl = TestResourceUtil.getResource("org/ebayopensource/turmeric/test/tools/codegen/data/CalcService.wsdl");
 		
 		// @formatter:off
         String pluginParameters[] = {
 			"-gentype", "V4",
 			"-wsdl",wsdl.getAbsolutePath()
 		};
         // @formatter:on
 		
 		try {
 			performDirectCodeGen(pluginParameters);
 			Assert.fail("Expecting exception of type " + MissingInputOptionException.class.getName());
 		} catch(MissingInputOptionException e){
 			Assert.assertThat(e.getMessage(), containsString("dest absent"));
 		}
 	}
 	
 	@Test
 	// v4 tests to be cleaned up - Will be deleted
 	public void genTypeV4_one_schema_wsdl() throws Exception{
 		assumeV4TypeMappingGeneratorPresent();
 		File wsdl = TestResourceUtil.getResource("org/ebayopensource/turmeric/test/tools/codegen/data/CalcService.wsdl");
 		File destDir = testingdir.getFile("tmp");
 
 		// @formatter:off
         String pluginParameters[] = {
 			"-gentype", "V4",
 			"-wsdl",wsdl.getAbsolutePath(),
 			"-dest",destDir.getAbsolutePath()
 		};
         // @formatter:on
 
 		performDirectCodeGen(pluginParameters);
 	}
 
 	@Test
 	// v4 tests to be cleaned up - Will be deleted
 	public void genTypeV4_one_schema_wsdl_user_given_pkg() throws Exception{
 		assumeV4TypeMappingGeneratorPresent();
 		File wsdl = TestResourceUtil.getResource("org/ebayopensource/turmeric/test/tools/codegen/data/CalcService.wsdl");
 		File destDir = testingdir.getFile("tmp");
 
 		// @formatter:off
         String pluginParameters[] = {
 			"-gentype", "V4",
 			"-wsdl",wsdl.getAbsolutePath(),
 			"-dest",destDir.getAbsolutePath(),
 			"-pkg","com.fun.rename.pkg.test"
 		};
         // @formatter:on
 		
 		performDirectCodeGen(pluginParameters);
 	}
 	
 	/**
 	 * Use the junit 4.8.x {@link Assume} class to handle future V4 Type
 	 * Mapping Generator tests.
 	 */
 	private void assumeV4TypeMappingGeneratorPresent() {
 		try {
 			Class<?> clazz = Class.forName("org.ebayopensource.turmeric.runtime.tools.codegen.external.V4TypeMappingsGenerator");
 			Assume.assumeNotNull(clazz);
 		} catch (ClassNotFoundException e) {
 			Assume.assumeNoException(e);
 		}
 	}
 
 	@Test
 	public void testAddTypeLibrary() throws Exception{
 		MavenTestingUtils.ensureEmpty(testingdir.getDir());
 		
 		File rootDir = testingdir.getDir(); // was "C:\\WORK\\SOA\\TypeLibrary\\libraries\\MixedLibrary2"
 		MavenTestingUtils.ensureEmpty(new File(rootDir, "gen-src"));
 		MavenTestingUtils.ensureEmpty(new File(rootDir, "gen-meta-src"));
 		
 		String libraryName = "MixedLibrary2";
 		String ns = "http://www.ebayopensource.org/turmeric/common/v1/types";
 		
 		// @formatter:off
         String pluginParametersCreate[] = {
 			"-gentype", "genTypeCreateTypeLibrary", 
 			"-pr", rootDir.getAbsolutePath(), 
 			"-libname", libraryName ,
 			"-libVersion","1.0.0",
 			"-libCategory","COMPLEX",
 			"-libNamespace",ns
 		};
         // @formatter:on
 		
 		// @formatter:off
         String pluginParametersClean[] = {
 			"-gentype", "genTypeCleanBuildTypeLibrary", 
 			"-pr", rootDir.getAbsolutePath(), 
 			"-libname", libraryName 
 		};
         // @formatter:on
 		
 		@SuppressWarnings("unused")
 		// @formatter:off
         String pluginParametersAdd[] = {
 			"-gentype", "genTypeAddType", 
 			"-pr", rootDir.getAbsolutePath(), 
 			"-libname", libraryName , 
 			"-type", "ProductName.xsd"
 		};
         // @formatter:on
 
 		@SuppressWarnings("unused")
 		// @formatter:off
         String pluginParametersDelete[] = {
 			"-gentype", "genTypeDeleteType", 
 			"-pr", rootDir.getAbsolutePath(), 
 			"-libname", libraryName  , 
 			"-type", "ProductName.xsd"
 		};
         // @formatter:on
 		
 		performDirectCodeGen(pluginParametersCreate);
 		performDirectCodeGen(pluginParametersClean);
 		// ProductName.xsd does not exist (yet)
 		// performDirectCodeGen(pluginParametersAdd);
 		// performDirectCodeGen(pluginParametersDelete);
 	}
 	
 }
 
