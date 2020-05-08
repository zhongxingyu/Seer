 /*
  *	File: @(#)PafGenerationToHierarchyMigrationTest.java 	Package: com.pace.base.migration 	Project: Paf Base Libraries
  *	Created: Aug 13, 2009  		By: jmilliron
  *	Version: x.xx
  *
  * 	Copyright (c) 2005-2007 Palladium Group, Inc. All rights reserved.
  *
  *	This software is the confidential and proprietary information of Palladium Group, Inc.
  *	("Confidential Information"). You shall not disclose such Confidential Information and 
  * 	should use it only in accordance with the terms of the license agreement you entered into
  *	with Palladium Group, Inc.
  *
  *
  *
 	Date			Author			Version			Changes
 	xx/xx/xx		xxxxxxxx		x.xx			..............
  * 
  */
 package com.pace.base.migration;
 
 import java.io.File;
 import java.io.IOException;
 
 import com.pace.base.PafBaseConstants;
 import com.pace.base.project.InvalidPaceProjectInputException;
 import com.pace.base.project.PaceProjectCreationException;
 import com.pace.base.project.XMLPaceProject;
 import com.pace.base.utility.FileUtils;
 import com.pace.base.utility.PafZipUtil;
 
 /**
  * Unit test for PafGenerationToHierarchyMigration
  *
  * @author jmilliron
  * @version	x.xx
  *
  */
 public class PafGenerationToHierarchyMigrationTest extends MigrationActionTestCase {
 		
 	/* (non-Javadoc)
 	 * @see com.pace.base.migration.MigrationActionTestCase#setUp()
 	 */
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp("./test_files/pace1210.paf");
 	}
 
 	/**
 	 * Test method for {@link com.pace.base.migration.PafGenerationToHierarchyMigration#PafGenerationToHierarchyMigration(com.pace.base.project.XMLPaceProject)}.
 	 */
 	public void testPafGenerationToHierarchyMigrationXMLPaceProject() {
 		
 		MigrationAction action = new PafGenerationToHierarchyMigration(pp);
 		
 		assertNotNull(action);
 		assertNotNull(action.getXMLPaceProject());
 		assertEquals(action.getXMLPaceProject(), pp);
 	}
 
 	/**
 	 * Test method for {@link com.pace.base.migration.PafGenerationToHierarchyMigration#getStatus()}.
 	 */
 	public void testGetStatus() {
 		
 		MigrationAction action = new PafGenerationToHierarchyMigration(pp);
 		
 		File genFormatsFile = new File(tempConfDir, PafBaseConstants.FN_GenerationFormats);
 		
 		assertTrue(genFormatsFile.exists());
 		
 		File hierFormatsFile = new File(tempConfDir, PafBaseConstants.FN_HierarchyFormats);
 		
 		assertFalse(hierFormatsFile.exists());
 		
 		assertEquals(MigrationActionStatus.NotStarted, action.getStatus());
 		
 		FileUtils.deleteFilesInDir(tempConfDir, new String[] { PafBaseConstants.FN_GenerationFormats } );
 		
 		assertEquals(MigrationActionStatus.NotStarted, action.getStatus());
 		
 		File newHierFormatsFile = null;
 		
 		try {
 			newHierFormatsFile = File.createTempFile("test", "xml");
 		} catch (IOException e) {
 			fail(e.getMessage());
 		}
 		
 		assertNotNull(newHierFormatsFile);
 		
 		assertTrue(newHierFormatsFile.isFile());
 		
 		assertTrue(newHierFormatsFile.renameTo(hierFormatsFile));
 		
 		assertFalse(newHierFormatsFile.isFile());
 		
 		assertTrue(hierFormatsFile.isFile());
 		
 		assertEquals(MigrationActionStatus.Completed, action.getStatus());
 		
 		
 	}
 
 	/**
 	 * Test method for {@link com.pace.base.migration.PafGenerationToHierarchyMigration#run()}.
 	 */
 	public void testRun() {
 		
 		MigrationAction action = new PafGenerationToHierarchyMigration(pp);
 		
 		File genFormatsFile = new File(tempConfDir, PafBaseConstants.FN_GenerationFormats);
 		
 		assertTrue(genFormatsFile.exists());
 		
 		File hierFormatsFile = new File(tempConfDir, PafBaseConstants.FN_HierarchyFormats);
 		
 		assertFalse(hierFormatsFile.exists());
 		
 		assertEquals(MigrationActionStatus.NotStarted, action.getStatus());
 		
 		assertEquals(0, pp.getHierarchyFormats().size());
 		
 		action.run();
 				
 		assertFalse(genFormatsFile.exists());
 		
 		assertTrue(hierFormatsFile.exists());
 		
 		assertEquals(MigrationActionStatus.Completed, action.getStatus());
 		
 		FileUtils.deleteFilesInDir(tempConfDir, true);
 		
 		try {
 			PafZipUtil.unzipFile("./test_files/pace1210.paf", tempConfDir.toString());
 		} catch (IOException e) {
 			fail(e.getMessage());
 		}
 		
 		assertEquals(MigrationActionStatus.NotStarted, action.getStatus());
 		
 		try {
			pp = new XMLPaceProject(tempConfDir.getName(), true);
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		}
 		
 		assertNotNull(pp);
 		
 		action.setXMLPaceProject(pp);
 						
 		assertEquals(MigrationActionStatus.Completed, action.getStatus());
 		
 		FileUtils.deleteFilesInDir(tempConfDir, true);
 						
 		try {
 			PafZipUtil.unzipFile("./test_files/pace1210.paf", tempConfDir.toString());
 		} catch (IOException e) {
 			fail(e.getMessage());
 		}
 		
 		assertEquals(MigrationActionStatus.NotStarted, action.getStatus());
 		
 		try {
			pp = new XMLPaceProject(tempConfDir.getName(), false);
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		}
 		
 		assertNotNull(pp);
 		
 		action.setXMLPaceProject(pp);
 						
 		assertEquals(MigrationActionStatus.NotStarted, action.getStatus());		
 		
 		
 	}
 
 }
