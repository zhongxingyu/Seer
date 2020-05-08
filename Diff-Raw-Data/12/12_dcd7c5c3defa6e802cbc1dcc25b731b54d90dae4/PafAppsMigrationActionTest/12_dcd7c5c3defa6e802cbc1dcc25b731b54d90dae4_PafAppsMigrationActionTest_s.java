 /*
  *	File: @(#)PafAppsMigrationActionTest.java 	Package: com.pace.base.migration 	Project: Paf Base Libraries
  *	Created: Aug 12, 2009  		By: jmilliron
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
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import com.pace.base.PafException;
 import com.pace.base.app.AliasMapping;
 import com.pace.base.app.AppColors;
 import com.pace.base.app.PafApplicationDef;
 import com.pace.base.app.SuppressZeroSettings;
 import com.pace.base.project.InvalidPaceProjectInputException;
 import com.pace.base.project.PaceProjectCreationException;
 import com.pace.base.project.XMLPaceProject;
 import com.pace.base.utility.FileUtils;
 import com.pace.base.utility.PafZipUtil;
 
 /**
  * Class_description_goes_here
  *
  * @author jmilliron
  * @version	x.xx
  *
  */
 public class PafAppsMigrationActionTest extends MigrationActionTestCase {
 
 
 	/**
 	 * Test method for {@link com.pace.base.migration.PafAppsMigrationAction#PafAppsMigrationAction(com.pace.base.project.XMLPaceProject)}.
 	 */
 	public void testPafAppsMigrationActionXMLPaceProject() {
 		
 		PafAppsMigrationAction action = new PafAppsMigrationAction(pp);
 		
 		assertNotNull(action);
 		assertNotNull(action.getXMLPaceProject());
 		assertEquals(action.getXMLPaceProject(), pp);
 		
 	}
 
 	/**
 	 * Test method for {@link com.pace.base.migration.PafAppsMigrationAction#getStatus()}.
 	 */
 	public void testGetStatus() {
 		
 		for (Boolean upgradProject : Arrays.asList(Boolean.FALSE, Boolean.TRUE) ) {
 						
 			pp.setUpgradeProject(upgradProject);
 			
 			List<PafApplicationDef> pafAppList = pp.getApplicationDefinitions();
 			
 			assertNotNull(pp);
 			
 			PafApplicationDef pafApp = pafAppList.get(0);
 			
 			PafAppsMigrationAction action = new PafAppsMigrationAction(pp);
 			
 			assertEquals(MigrationActionStatus.Completed, action.getStatus());		
 			
 			AliasMapping[] aliasMappingAr = pafApp.getAppSettings().getGlobalAliasMappings();
 			
 			pafApp.getAppSettings().setGlobalAliasMappings(null);
 			
 			pp.setApplicationDefinitions(new ArrayList<PafApplicationDef>(Arrays.asList(pafApp)));
 			
 			assertEquals(MigrationActionStatus.NotStarted, action.getStatus());
 			
 			pafApp.getAppSettings().setGlobalAliasMappings(aliasMappingAr);
 			
 			pp.setApplicationDefinitions(new ArrayList<PafApplicationDef>(Arrays.asList(pafApp)));
 			
 			assertEquals(MigrationActionStatus.Completed, action.getStatus());
 			
 			String[] hierDimAr = pafApp.getMdbDef().getHierDims();
 			
 			List<String> hierDimList = new ArrayList<String>(Arrays.asList(hierDimAr));
 			
 			hierDimList.remove(hierDimList.size() - 1);
 			
 			pafApp.getMdbDef().setHierDims(hierDimList.toArray(new String[0]));
 			
 			pp.setApplicationDefinitions(new ArrayList<PafApplicationDef>(Arrays.asList(pafApp)));
 			
 			assertEquals(MigrationActionStatus.Completed, action.getStatus());	
 			
 			hierDimList = new ArrayList<String>(Arrays.asList(hierDimAr));
 			
 			hierDimList.add("NewDimension");
 			
 			pafApp.getMdbDef().setHierDims(hierDimList.toArray(new String[0]));
 			
 			pp.setApplicationDefinitions(new ArrayList<PafApplicationDef>(Arrays.asList(pafApp)));
 			
 			assertEquals(MigrationActionStatus.NotStarted, action.getStatus());	
 			
 			hierDimList = new ArrayList<String>(Arrays.asList(hierDimAr));
 							
 			pafApp.getMdbDef().setHierDims(hierDimList.toArray(new String[0]));
 			
 			pp.setApplicationDefinitions(new ArrayList<PafApplicationDef>(Arrays.asList(pafApp)));
 			
 			assertEquals(MigrationActionStatus.Completed, action.getStatus());	
 			
 			action.setAttributeDimensionNames(new String[] { "NewAttDimension1" } );
 			
 			assertEquals(MigrationActionStatus.NotStarted, action.getStatus());
 			
 			action.setAttributeDimensionNames(null);
 			
 			assertEquals(MigrationActionStatus.Completed, action.getStatus());
 			
 			AppColors appColors = pafApp.getAppSettings().getAppColors();
 			
 			assertNotNull(appColors);
 			assertNotNull(appColors.getForwardPlannableProtectedColor());
 			assertNotNull(appColors.getNonPlannableProtectedColor());
 			assertNotNull(appColors.getNoteColor());
 			assertNotNull(appColors.getProtectedColor());
 			assertNotNull(appColors.getSystemLockColor());
 			assertNotNull(appColors.getUserLockColor());		
 			
 			pafApp.getAppSettings().setAppColors(null);
 			
 			pp.setApplicationDefinitions(new ArrayList<PafApplicationDef>(Arrays.asList(pafApp)));
 			
 			assertEquals(MigrationActionStatus.NotStarted, action.getStatus());
 			
 			AppColors newAppColors = new AppColors();
 					
 			assertNotNull(newAppColors.getForwardPlannableProtectedColor());
 			assertNotNull(newAppColors.getNonPlannableProtectedColor());
 			assertNotNull(newAppColors.getNoteColor());
 			assertNotNull(newAppColors.getProtectedColor());
 			assertNotNull(newAppColors.getSystemLockColor());
 			assertNotNull(newAppColors.getUserLockColor());
 			
 			pafApp.getAppSettings().setAppColors(newAppColors);
 			
 			pp.setApplicationDefinitions(new ArrayList<PafApplicationDef>(Arrays.asList(pafApp)));
 			
 			assertEquals(MigrationActionStatus.Completed, action.getStatus());
 			
 			newAppColors.setForwardPlannableProtectedColor(null);
 			newAppColors.setNonPlannableProtectedColor(null); 
 			newAppColors.setNoteColor(null); 
 			newAppColors.setProtectedColor(null); 
 			newAppColors.setSystemLockColor(null); 
 			newAppColors.setUserLockColor(null);		
 			
 			pafApp.getAppSettings().setAppColors(newAppColors);
 			
 			pp.setApplicationDefinitions(new ArrayList<PafApplicationDef>(Arrays.asList(pafApp)));
 			
 			assertEquals(MigrationActionStatus.NotStarted, action.getStatus());
 			
 			newAppColors.setForwardPlannableProtectedColor("test");
 			
 			pafApp.getAppSettings().setAppColors(newAppColors);
 			
 			pp.setApplicationDefinitions(new ArrayList<PafApplicationDef>(Arrays.asList(pafApp)));
 			
 			assertEquals(MigrationActionStatus.NotStarted, action.getStatus());
 			
 			newAppColors.setNonPlannableProtectedColor("test");
 			
 			pafApp.getAppSettings().setAppColors(newAppColors);
 			
 			pp.setApplicationDefinitions(new ArrayList<PafApplicationDef>(Arrays.asList(pafApp)));
 			
 			assertEquals(MigrationActionStatus.NotStarted, action.getStatus());
 			
 			newAppColors.setNoteColor("test");
 			
 			pafApp.getAppSettings().setAppColors(newAppColors);
 			
 			pp.setApplicationDefinitions(new ArrayList<PafApplicationDef>(Arrays.asList(pafApp)));
 			
 			assertEquals(MigrationActionStatus.NotStarted, action.getStatus());
 			
 			newAppColors.setProtectedColor("test");
 			
 			pafApp.getAppSettings().setAppColors(newAppColors);
 			
 			pp.setApplicationDefinitions(new ArrayList<PafApplicationDef>(Arrays.asList(pafApp)));
 			
 			assertEquals(MigrationActionStatus.NotStarted, action.getStatus());
 			
 			newAppColors.setSystemLockColor("test");
 			
 			pafApp.getAppSettings().setAppColors(newAppColors);
 			
 			pp.setApplicationDefinitions(new ArrayList<PafApplicationDef>(Arrays.asList(pafApp)));
 			
 			assertEquals(MigrationActionStatus.NotStarted, action.getStatus());
 			
 			newAppColors.setUserLockColor("test");
 			
 			pafApp.getAppSettings().setAppColors(newAppColors);
 			
 			pp.setApplicationDefinitions(new ArrayList<PafApplicationDef>(Arrays.asList(pafApp)));
 			
 			assertEquals(MigrationActionStatus.Completed, action.getStatus());
 			
 			pafApp.getAppSettings().setAppColors(appColors);
 			
 			pp.setApplicationDefinitions(new ArrayList<PafApplicationDef>(Arrays.asList(pafApp)));
 			
 			assertEquals(MigrationActionStatus.Completed, action.getStatus());
 			
 			SuppressZeroSettings suppressZeros = pafApp.getAppSettings().getGlobalSuppressZeroSettings();
 			
 			assertNotNull(suppressZeros);
 			assertNotNull(suppressZeros.getColumnsSuppressed());
 			assertNotNull(suppressZeros.getEnabled());
 			assertNotNull(suppressZeros.getRowsSuppressed());
 			assertNotNull(suppressZeros.getVisible());
 			
 			pafApp.getAppSettings().setGlobalSuppressZeroSettings(null);
 			
 			pp.setApplicationDefinitions(new ArrayList<PafApplicationDef>(Arrays.asList(pafApp)));
 			
 			assertEquals(MigrationActionStatus.NotStarted, action.getStatus());
 			
 			SuppressZeroSettings newSuppressZeros = new SuppressZeroSettings();
 			
 			newSuppressZeros.setColumnsSuppressed(null);
 			newSuppressZeros.setEnabled(null);
 			newSuppressZeros.setRowsSuppressed(null);
 			newSuppressZeros.setVisible(null);
 			
 			pafApp.getAppSettings().setGlobalSuppressZeroSettings(newSuppressZeros);
 			
 			pp.setApplicationDefinitions(new ArrayList<PafApplicationDef>(Arrays.asList(pafApp)));
 			
 			assertEquals(MigrationActionStatus.NotStarted, action.getStatus());
 			
 			newSuppressZeros.setColumnsSuppressed(Boolean.TRUE);
 			
 			pafApp.getAppSettings().setGlobalSuppressZeroSettings(newSuppressZeros);
 			
 			pp.setApplicationDefinitions(new ArrayList<PafApplicationDef>(Arrays.asList(pafApp)));
 			
 			assertEquals(MigrationActionStatus.NotStarted, action.getStatus());
 			
 			newSuppressZeros.setEnabled(Boolean.TRUE);
 			
 			pafApp.getAppSettings().setGlobalSuppressZeroSettings(newSuppressZeros);
 			
 			pp.setApplicationDefinitions(new ArrayList<PafApplicationDef>(Arrays.asList(pafApp)));
 			
 			assertEquals(MigrationActionStatus.NotStarted, action.getStatus());
 			
 			newSuppressZeros.setRowsSuppressed(Boolean.TRUE);
 			
 			pafApp.getAppSettings().setGlobalSuppressZeroSettings(newSuppressZeros);
 			
 			pp.setApplicationDefinitions(new ArrayList<PafApplicationDef>(Arrays.asList(pafApp)));
 			
 			assertEquals(MigrationActionStatus.NotStarted, action.getStatus());
 			
 			newSuppressZeros.setVisible(Boolean.TRUE);
 			
 			pafApp.getAppSettings().setGlobalSuppressZeroSettings(newSuppressZeros);
 			
 			pp.setApplicationDefinitions(new ArrayList<PafApplicationDef>(Arrays.asList(pafApp)));
 			
 			assertEquals(MigrationActionStatus.Completed, action.getStatus());
 		}
 		
 	}
 
 	/**
 	 * Test method for {@link com.pace.base.migration.PafAppsMigrationAction#run()}.
 	 */
 	public void testRun() {
 						
 		XMLPaceProject defaultPP = null;
 
 		try {
 			
			defaultPP = new XMLPaceProject(tempConfDir.getName(), set, false);
 			
 		} catch (InvalidPaceProjectInputException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (PaceProjectCreationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		assertNotNull(defaultPP);
 		
 		PafAppsMigrationAction action = new PafAppsMigrationAction(defaultPP);
 		
 		assertEquals(MigrationActionStatus.Completed, action.getStatus());
 		
 		List<PafApplicationDef> pafAppList = pp.getApplicationDefinitions();
 		
 		//PafApplicationDef defaultPafApp = pafAppList.get(0);
 		
 		assertNotNull(pp);
 		
 		pafAppList = pp.getApplicationDefinitions();
 		
 		PafApplicationDef pafApp = pafAppList.get(0);
 		
 		pafApp.getAppSettings().setGlobalAliasMappings(null);
 		
 		pp.setApplicationDefinitions(new ArrayList<PafApplicationDef>(Arrays.asList(pafApp)));
 		
 		action = new PafAppsMigrationAction((XMLPaceProject) pp);
 		
 		assertEquals(MigrationActionStatus.NotStarted, action.getStatus());
 		
 		action.run();
 		
 		assertNotNull(pp.getApplicationDefinitions().get(0).getAppSettings().getGlobalAliasMappings());
 		
 		try {
 			pp.reloadData();
 		} catch (PafException e) {
 			fail(e.getMessage());
 		}
 		
 		assertNotNull(pp.getApplicationDefinitions().get(0).getAppSettings().getGlobalAliasMappings());
 		
 		assertEquals(MigrationActionStatus.Completed, action.getStatus());
 		
 		pafApp.getAppSettings().setAppColors(null);
 		
 		pp.setApplicationDefinitions(new ArrayList<PafApplicationDef>(Arrays.asList(pafApp)));
 		
 		action = new PafAppsMigrationAction(pp);
 		
 		assertEquals(MigrationActionStatus.NotStarted, action.getStatus());
 		
 		action.run();
 		
 		assertNotNull(pp.getApplicationDefinitions().get(0).getAppSettings().getAppColors());
 		
 		try {
 			pp.reloadData();
 		} catch (PafException e) {
 			fail(e.getMessage());
 		}
 		
 		assertNotNull(pp.getApplicationDefinitions().get(0).getAppSettings().getAppColors());
 		
 		assertEquals(MigrationActionStatus.Completed, action.getStatus());
 		
 		AppColors newAppColors = new AppColors();
 		
 		newAppColors.setForwardPlannableProtectedColor(null);
 		newAppColors.setNonPlannableProtectedColor(null); 
 		newAppColors.setNoteColor(null); 
 		newAppColors.setProtectedColor(null); 
 		newAppColors.setSystemLockColor(null); 
 		newAppColors.setUserLockColor(null);	
 		
 		pafApp.getAppSettings().setAppColors(newAppColors);
 		
 		pp.setApplicationDefinitions(new ArrayList<PafApplicationDef>(Arrays.asList(pafApp)));
 		
 		action = new PafAppsMigrationAction(pp);
 		
 		assertEquals(MigrationActionStatus.NotStarted, action.getStatus());
 		
 		action.run();
 		
 		assertNotNull(pp.getApplicationDefinitions().get(0).getAppSettings().getAppColors());
 		
 		try {
 			pp.reloadData();
 		} catch (PafException e) {
 			fail(e.getMessage());
 		}
 		
 		assertNotNull(pp.getApplicationDefinitions().get(0).getAppSettings().getAppColors());
 		
 		assertEquals(MigrationActionStatus.Completed, action.getStatus());
 
 		pafApp.getAppSettings().setGlobalSuppressZeroSettings(null);
 		
 		pp.setApplicationDefinitions(new ArrayList<PafApplicationDef>(Arrays.asList(pafApp)));
 		
 		action = new PafAppsMigrationAction(pp);
 		
 		assertEquals(MigrationActionStatus.NotStarted, action.getStatus());
 		
 		action.run();
 		
 		assertNotNull(pp.getApplicationDefinitions().get(0).getAppSettings().getGlobalSuppressZeroSettings());
 		
 		try {
 			pp.reloadData();
 		} catch (PafException e) {
 			fail(e.getMessage());
 		}
 		
 		assertNotNull(pp.getApplicationDefinitions().get(0).getAppSettings().getGlobalSuppressZeroSettings());
 		
 		assertEquals(MigrationActionStatus.Completed, action.getStatus());
 		
 		SuppressZeroSettings newSuppressZeros = new SuppressZeroSettings();
 		
 		newSuppressZeros.setColumnsSuppressed(null);
 		newSuppressZeros.setEnabled(null);
 		newSuppressZeros.setRowsSuppressed(null);
 		newSuppressZeros.setVisible(null);
 		
 		pafApp.getAppSettings().setGlobalSuppressZeroSettings(newSuppressZeros);
 		
 		pp.setApplicationDefinitions(new ArrayList<PafApplicationDef>(Arrays.asList(pafApp)));
 		
 		action = new PafAppsMigrationAction(pp);
 		
 		assertEquals(MigrationActionStatus.NotStarted, action.getStatus());
 		
 		action.run();
 		
 		assertNotNull(pp.getApplicationDefinitions().get(0).getAppSettings().getGlobalSuppressZeroSettings());
 		
 		try {
 			pp.reloadData();
 		} catch (PafException e) {
 			fail(e.getMessage());
 		}
 		
 		assertNotNull(pp.getApplicationDefinitions().get(0).getAppSettings().getGlobalSuppressZeroSettings());
 		
 		assertEquals(MigrationActionStatus.Completed, action.getStatus());
 		
 		//TEST with auto convert flag to false, load everything
 		
 		FileUtils.deleteFilesInDir(tempConfDir, true);
 		
 		try {
 			PafZipUtil.unzipFile("./test_files/pace1210.paf", tempConfDir.toString());
 		} catch (IOException e) {
 			fail(e.getMessage());
 		}
 		
 		try {
 			pp = new XMLPaceProject(tempConfDir.toString(), false);
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		}
 		
 		action.setXMLPaceProject(pp);
 		
 		assertEquals(MigrationActionStatus.NotStarted, action.getStatus());
 		
 		FileUtils.deleteFilesInDir(tempConfDir, true);
 		
 		try {
 			PafZipUtil.unzipFile("./test_files/pace1210.paf", tempConfDir.toString());
 		} catch (IOException e) {
 			fail(e.getMessage());
 		}
 		
 		try {
 			pp = new XMLPaceProject(tempConfDir.toString(), true);
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		}
 		
 		action.setXMLPaceProject(pp);
 		
 		assertEquals(MigrationActionStatus.Completed, action.getStatus());
 		
 		
 		
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 
 }
