 /*******************************************************************************
  * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *******************************************************************************/
 package org.ebayopensource.turmeric.tools.codegen;
 
 import static org.hamcrest.Matchers.*;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.Properties;
 
 import org.ebayopensource.turmeric.junit.utils.MavenTestingUtils;
 import org.ebayopensource.turmeric.tools.GeneratedAssert;
 import org.ebayopensource.turmeric.tools.codegen.exception.CodeGenFailedException;
 import org.ebayopensource.turmeric.tools.codegen.util.CodeGenConstants;
 import org.junit.Assert;
 import org.junit.Test;
 
 
 public class ServiceGeneratorWSDLIntfTest extends AbstractServiceGeneratorTestCase {
 	
 	private File createServiceIntfPropertiesFile(File dir, Properties extra) throws IOException
 	{
 		File sipFile = new File(dir, "service_intf_project.properties");
 		
 		Properties props = loadProperties(sipFile);
 		
 		if(extra != null) {
 			@SuppressWarnings("unchecked")
 			Enumeration<String> names = (Enumeration<String>) extra.propertyNames();
 			while(names.hasMoreElements()) {
 				String name = names.nextElement();
 				props.setProperty(name, extra.getProperty(name));
 			}
 		}
 		
 		writeProperties(sipFile, props);
 		
 		return sipFile;
 	}
 	
 	@Test
 	public void serviceGeneratorWSDLWithIntfPropsHavingPropertiesCase1()
 			throws Exception {
 
 		// Initialize testing paths
 		MavenTestingUtils.ensureEmpty(testingdir);
 		File wsdl = getCodegenDataFileInput("CalcService.wsdl");
 		File destDir = getTestDestDir();
 		File srcDir = getTestDestPath("src");
 		File metaDir = getTestDestPath("meta-src");
 		File binDir = testingdir.getFile("bin");
 		File rootDir = testingdir.getDir();
 		
 		MavenTestingUtils.ensureEmpty(srcDir);
 		MavenTestingUtils.ensureEmpty(metaDir);
 
 		Properties extra = new Properties();
 		extra.setProperty(CodeGenConstants.ADMIN_NAME, "AdminNameWithSpace  ");
 		extra.setProperty(CodeGenConstants.INTERFACE_SOURCE_TYPE, "wsdl        ");
 		
 		createServiceIntfPropertiesFile(rootDir, extra);
 		
 		// Setup arguments
 		// @formatter:off
 		String args[] = {
 			"-servicename", "CalculatorService",
 			"-wsdl", wsdl.getAbsolutePath(),
 			"-gentype", "All",
 			"-src", srcDir.getAbsolutePath(),
 			"-dest", destDir.getAbsolutePath(),
 			"-pr", rootDir.getAbsolutePath(),
 			"-scv", "1.0.0",
 			"-bin", binDir.getAbsolutePath()
 		};
 		// @formatter:on
 		
 		performDirectCodeGen(args);
 
 		GeneratedAssert.assertFileExists(destDir, "gen-src/org/ebayopensource/turmeric/common/v1/services/AdminNameWithSpace.java");
 		GeneratedAssert.assertFileExists(destDir, "gen-src/org/ebayopensource/turmeric/common/v1/services/gen/AdminNameWithSpaceProxy.java");
 	}
 
 	@Test
 	public void serviceGeneratorWSDLWithIntfPropsHavingPropertiesCase2()
 			throws Exception {
 
 		// Initialize testing paths
 		MavenTestingUtils.ensureEmpty(testingdir);
 		File wsdl = getCodegenDataFileInput("CalcService.wsdl");
 		File destDir = getTestDestDir();
 		File srcDir = getTestDestPath("src");
 		File metaDir = getTestDestPath("meta-src");
 		File binDir = testingdir.getFile("bin");
 		File rootDir = testingdir.getDir();
 		
 		MavenTestingUtils.ensureDirExists(srcDir);
 		MavenTestingUtils.ensureDirExists(metaDir);
 
 		Properties extra = new Properties();
 		extra.setProperty(CodeGenConstants.ADMIN_NAME, "  **AdminNa   meW   i   thSpace  ");
 		extra.setProperty(CodeGenConstants.INTERFACE_SOURCE_TYPE, "w   s   d  l        ");
 		
 		createServiceIntfPropertiesFile(rootDir, extra);
 		
 		// Setup arguments
 		// @formatter:off
 		String args[] = {
 			"-servicename", "CalculatorService",
 			"-wsdl", wsdl.getAbsolutePath(),
 			"-gentype", "All",
 			"-src", srcDir.getAbsolutePath(),
 			"-dest", destDir.getAbsolutePath(),
 			"-pr", rootDir.getAbsolutePath(),
 			"-scv", "1.0.0",
 			"-bin", binDir.getAbsolutePath()
 		};
 		// @formatter:on
 
 		try {
 			performDirectCodeGen(args);
 			Assert.fail("Expected exception of type: " + CodeGenFailedException.class.getName());
 		} catch (CodeGenFailedException ex) {
			Assert.assertThat(ex.getMessage(), containsString("JAVAC Compile Failure"));
 		}
 	}
 }
