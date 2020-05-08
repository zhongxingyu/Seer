 /*
  *	File: @(#)ExcelPaceProjectTest.java 	Package: com.pace.base.project 	Project: Paf Base Libraries
  *	Created: Sep 9, 2009  		By: jmilliron
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
 package com.pace.base.project;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.nio.channels.FileChannel;
 import java.nio.channels.FileLock;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import junit.framework.TestCase;
 
 import com.pace.base.PafBaseConstants;
 import com.pace.base.PafException;
 import com.pace.base.app.*;
 import com.pace.base.comm.CustomMenuDef;
 import com.pace.base.comm.PafPlannerConfig;
 import com.pace.base.db.membertags.MemberTagDef;
 import com.pace.base.funcs.CustomFunctionDef;
 import com.pace.base.rules.MemberSet;
 import com.pace.base.rules.RoundingRule;
 import com.pace.base.rules.RuleSet;
 import com.pace.base.utility.FileUtils;
 import com.pace.base.utility.PafZipUtil;
 import com.pace.base.view.*;
 
 /**
  * Class_description_goes_here
  *
  * @author jmilliron
  * @version	x.xx
  *
  */
 public class ExcelPaceProjectTest extends TestCase {
 
 	private static final String TEST_FILES = "." + File.separator + "test_files" + File.separator;
 	
 	private static final String TEST_FILES_PROJECT_TEMPLATE = "project-template";
 	
 	private static final String TEST_FILES_PROJECT_TEMPLATE_XLSX = TEST_FILES + TEST_FILES_PROJECT_TEMPLATE + PafBaseConstants.XLSX_EXT;
 	
 	private static final String TEST_FILES_PROJECT_TEMPLATE_XLSM = TEST_FILES + TEST_FILES_PROJECT_TEMPLATE + PafBaseConstants.XLSM_EXT;
 	
 	private static final String TEST_FILES_PROJECT_WRITE_XLSX = TEST_FILES + "project-write.xlsx";
 	
 	private static final String TEST_FILES_PROJECT_TESTING_XLSX = TEST_FILES + "project-test.xlsx";
 	
 	private static final String TEST_FILES_PROJECT_TESTING2_XLSX = TEST_FILES + "project-test222.xlsx";
 	
 	private static final String TEST_FILES_PROJECT_TESTING2_XLSM = TEST_FILES + "project-test222.xlsm";
 
 	private ExcelPaceProject excelPP = null;
 	
 	String paceTestFldr = PafBaseConstants.DN_PaceTestFldr + File.separator;
 	
 	File testDir = new File(paceTestFldr);
 	
 	Set<ProjectElementId> projectElementIdSet;
 			
 	protected File tempDir = new File(PafBaseConstants.DN_PaceTestFldr+"2");
 	
 	String tempDirName = null;
 	
 	private PaceProject xmlPaceProject = null;
 			
 	
 	static {
 		
 		try {
 			
 			File writeFile = new File (TEST_FILES_PROJECT_WRITE_XLSX);
 			
 			if ( writeFile.exists()) {
 				
 				writeFile.delete();
 				
 			}
 			
 			FileUtils.copy(new File(TEST_FILES_PROJECT_TEMPLATE_XLSX), writeFile);
 									
 		} catch (IOException e) {
 			fail(e.getMessage());
 		}
 		
 	}
 
 	/* (non-Javadoc)
 	 * @see junit.framework.TestCase#setUp()
 	 */
 	protected void setUp() throws Exception {
 		super.setUp();
 		
 		if ( ! tempDir.exists() ) {
 						
 			boolean dirCreated = tempDir.mkdir();
 			
 			if ( ! dirCreated ) {
 				
 				fail("Directory " + tempDir.toString() + " could not be created.");
 				
 			}
 			
 		}		
 		
 		String pareArchiveFileName = "./test_files/pace.paf";
 		
 		File paceArchiveFile = new File(pareArchiveFileName);
 			
 		assertTrue(paceArchiveFile.isFile());
 			
 		tempDirName = tempDir.toString() + File.separator;
 		
 		PafZipUtil.unzipFile(paceArchiveFile.toString(), tempDir.toString());
 		
 		if ( ! testDir.exists())  {
 			
 			boolean createdNewDir = testDir.mkdir();
 			
 			if ( ! createdNewDir ) {
 				fail("Can't create test dir: " + testDir.toString());
 			}
 			
 		}
 		
 		projectElementIdSet = new HashSet<ProjectElementId>();
 		
 		try {
 			 xmlPaceProject = new XMLPaceProject(tempDirName, false);
 			 
 			 excelPP = (ExcelPaceProject) xmlPaceProject.convertTo(ProjectSerializationType.XLSX);
 			 
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		} catch (InvalidPaceProjectInputException e) {
 			// TODO Auto-generated catch block
 			fail(e.getMessage());
 		}
 						
 	}
 
 	/* (non-Javadoc)
 	 * @see junit.framework.TestCase#tearDown()
 	 */
 	protected void tearDown() throws Exception {
 		super.tearDown();
 						
 		FileUtils.deleteDirectory(tempDir);
 		
 		if ( testDir.exists() ) {
 			
 			FileUtils.deleteFilesInDir(testDir, true);
 			
 			boolean deleted = testDir.delete();
 			
 			if (! deleted) {
 				
 				System.out.println("Can't delete dir: " + testDir.getAbsolutePath());
 				
 			}
 			
 		}
 	}
 
 	
 	/**
 	 * @param name
 	 */
 	public ExcelPaceProjectTest(String name) {
 		super(name);
 	}
 	
 	
 	public void testSaveTo() {
 		
 		assertNotNull(excelPP);
 		
 		excelPP.clearProjectData();
 		
 		assertEquals(0, excelPP.getProjectDataMap().size());
 		
 		Set<ProjectElementId> elementSet = new HashSet<ProjectElementId>();
 						
 		excelPP.setApplicationDefinitions(xmlPaceProject.getApplicationDefinitions());
 		excelPP.setMeasures(xmlPaceProject.getMeasures());
 		
 		elementSet.addAll(excelPP.getLoadedProjectElementIdSet());
 		
 		try {
 			excelPP.saveTo("Invalid project data");
 			
 			fail("Should have validated the xlsx file.");
 			
 		} catch (ProjectSaveException e) {
 			//do nothing
 		}
 		
 		String validFileName = TEST_FILES + "valid_project_workbook.xlsx";
 		
 		File validFile = new File(validFileName);
 		
 		assertFalse(validFile.exists());
 		
 		try {
 			excelPP.saveTo(validFileName, elementSet);
 		} catch (ProjectSaveException e) {
 			fail(e.getMessage());
 		}
 		
 		assertTrue(validFile.exists());
 		
 		validFile.delete();
 		
 		assertFalse(validFile.exists());
 		
 	}
 	
 	public void testFullSave() {
 		
 		
 		PaceProject pp = xmlPaceProject.convertTo(ProjectSerializationType.XLSX);
 		
 		assertTrue(pp instanceof ExcelPaceProject);
 		
 		ExcelPaceProject newExcelPP = (ExcelPaceProject) pp;
 		
 		File excelFile = new File(TEST_FILES_PROJECT_TESTING2_XLSX);
 		
 		if ( excelFile.exists()) {
 			
 			excelFile.delete();
 			
 		}
 		
 		try {
 			newExcelPP.saveTo(TEST_FILES_PROJECT_TESTING2_XLSX);
 		} catch (ProjectSaveException e) {
 			fail(e.getMessage());
 		}
 		
 		assertTrue(excelFile.exists());
 		
 		excelFile = new File(TEST_FILES_PROJECT_TESTING2_XLSM);
 		
 		if ( excelFile.exists()) {
 			
 			excelFile.delete();
 			
 		}
 		
 		assertFalse( excelFile.exists());
 		
 		try {
 			newExcelPP.saveTo(TEST_FILES_PROJECT_TESTING2_XLSM);
 		} catch (ProjectSaveException e) {
 			fail(e.getMessage());
 		}
 		
 		assertTrue( excelFile.exists());
 		
 	}
 	
 	public void testReadApplications() {		
 
 		Set<ProjectElementId> elementSet = new HashSet<ProjectElementId>();
 		
 		elementSet.add(ProjectElementId.ApplicationDef);
 		
 		try {
 						
 			PaceProject testPP = new ExcelPaceProject(TEST_FILES_PROJECT_TEMPLATE_XLSX, elementSet);
 			
 			List<PafApplicationDef> appDefList = testPP.getApplicationDefinitions();
 			
 			assertNotNull(appDefList);
 			assertEquals(1, appDefList.size());
 		
 			List<PafApplicationDef> mockPafAppList = excelPP.getApplicationDefinitions();
 			
 			assertEquals(mockPafAppList.size(), appDefList.size());
 			
 			for (int i = 0; i < mockPafAppList.size(); i++ ) {
 				
 				PafApplicationDef mockPafApp = mockPafAppList.get(0);
 				PafApplicationDef excelPafApp = appDefList.get(0);
 				
 				assertNotNull(mockPafApp);
 				assertNotNull(excelPafApp);
 				
 				assertNotNull(mockPafApp.getPlanCycles());				
 				assertNotNull(excelPafApp.getPlanCycles());
 				
 				assertEquals(mockPafApp.getPlanCycles().length, excelPafApp.getPlanCycles().length);
 				
 				assertNotNull(mockPafApp.getSeasonList());
 				assertNotNull(excelPafApp.getSeasonList());
 				
 				assertEquals(mockPafApp.getAppId(), excelPafApp.getAppId());
 				
 				AppSettings mockAppSettings = mockPafApp.getAppSettings();
 				AppSettings excelAppSettings = excelPafApp.getAppSettings();
 				
 				assertNotNull(mockAppSettings);
 				assertNotNull(excelAppSettings);
 				
 				assertEquals(mockAppSettings.getAppTitle(), excelAppSettings.getAppTitle());
 				assertEquals(mockAppSettings.getGlobalUowSizeLarge(), excelAppSettings.getGlobalUowSizeLarge());
 				assertEquals(mockAppSettings.getGlobalUowSizeMax(), excelAppSettings.getGlobalUowSizeMax());
 				
 			}
 			
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		}
 					
 	}
 	
 	public void testReadVersions() {
 		
 
 		Set<ProjectElementId> elementSet = new HashSet<ProjectElementId>();
 		
 		elementSet.add(ProjectElementId.Versions);
 		
 		try {
 			PaceProject testPP = new ExcelPaceProject(TEST_FILES_PROJECT_TEMPLATE_XLSX, elementSet);
 						
 			List<VersionDef> versionDefList = testPP.getVersions();
 			
 			assertNotNull(versionDefList);
 			assertEquals(24, versionDefList.size());
 			
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		}
 						
 	}
 	
 	public void testReadMeasures() {
 		
 
 		Set<ProjectElementId> elementSet = new HashSet<ProjectElementId>();
 		
 		elementSet.add(ProjectElementId.Measures);
 		
 		try {
 			PaceProject testPP = new ExcelPaceProject(TEST_FILES_PROJECT_TEMPLATE_XLSX, elementSet);
 						
 			List<MeasureDef> list = testPP.getMeasures();
 			
 			assertNotNull(list);
 			assertEquals(88, list.size());
 			
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		}
 				
 	}
 	
 	public void testReadNumericFormats() {		
 
 		Set<ProjectElementId> elementSet = new HashSet<ProjectElementId>();
 		
 		elementSet.add(ProjectElementId.NumericFormats);
 		
 		try {
 			
 			PaceProject testPP = new ExcelPaceProject(TEST_FILES_PROJECT_TEMPLATE_XLSX, elementSet);
 						
 			Map<String, PafNumberFormat> numberFormatMap = testPP.getNumericFormats();
 			
 			assertNotNull(numberFormatMap);
 			assertEquals(14, numberFormatMap.size());
 			
 			for (String key : numberFormatMap.keySet()) {
 				
 				if ( key.equals("0D")) {
 					
 					assertTrue(numberFormatMap.get(key).isDefaultFormat());
 					
 				} else {
 					
 					assertFalse(numberFormatMap.get(key).isDefaultFormat());
 					
 				}
 				
 			}
 			
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		}
 				
 	}
 	
 	public void testReadGlobalStyles() {		
 
 		Set<ProjectElementId> elementSet = new HashSet<ProjectElementId>();
 		
 		elementSet.add(ProjectElementId.GlobalStyles);
 		
 		try {
 			
 			PaceProject testPP = new ExcelPaceProject(TEST_FILES_PROJECT_TEMPLATE_XLSX, elementSet);
 						
 			Map<String, PafStyle> map = testPP.getGlobalStyles();
 			
 			assertNotNull(map);
 			assertEquals(22, map.size());
 						
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		}
 				
 	}
 	
 	public void testReadUserSelections() {		
 
 		Set<ProjectElementId> elementSet = new HashSet<ProjectElementId>();
 		
 		elementSet.add(ProjectElementId.UserSelections);
 		
 		try {
 			
 			PaceProject testPP = new ExcelPaceProject(TEST_FILES_PROJECT_TEMPLATE_XLSX, elementSet);
 						
 			List<PafUserSelection> list = testPP.getUserSelections();
 			
 			assertNotNull(list);
 			assertEquals(22, list.size());
 						
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		}
 				
 	}
 	
 	public void testReadHierarchyFormats() {		
 
 		Set<ProjectElementId> elementSet = new HashSet<ProjectElementId>();
 		
 		elementSet.add(ProjectElementId.HierarchyFormats);
 		
 		try {
 			
 			PaceProject testPP = new ExcelPaceProject(TEST_FILES_PROJECT_TEMPLATE_XLSX, elementSet);
 						
 			Map<String, HierarchyFormat> map = testPP.getHierarchyFormats();
 			
 			assertNotNull(map);
 			assertEquals(9, map.size());
 			
 			assertEquals(3, map.get("LocProd").getDimensions().size());
 			assertEquals(3, map.get("LocProd").getDimensions().get("Time").getNumberOfLevelFormats());
 			assertEquals(4, map.get("LocProd").getDimensions().get("Product").getNumberOfLevelFormats());
 			assertEquals(2, map.get("LocProd").getDimensions().get("Location").getNumberOfLevelFormats());
 		
 			
 			
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		}
 				
 	}
 
 	public void testReadDynamicMembers() {		
 
 		Set<ProjectElementId> elementSet = new HashSet<ProjectElementId>();
 		
 		elementSet.add(ProjectElementId.DynamicMembers);
 		
 		try {
 			
 			PaceProject testPP = new ExcelPaceProject(TEST_FILES_PROJECT_TEMPLATE_XLSX, elementSet);
 						
 			List<DynamicMemberDef> list = testPP.getDynamicMembers();
 			
 			assertNotNull(list);
 			assertEquals(1, list.size());
 			assertEquals("Version", list.get(0).getDimension());
 			assertEquals(3, list.get(0).getMemberSpecs().length);
 			
 			for (String member : list.get(0).getMemberSpecs()) {
 				
 				assertTrue(member.contains(PafBaseConstants.PLAN_VERSION));
 				
 			}
 						
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		}
 				
 	}
 	
 	public void testReadPlanCycles() {		
 
 		Set<ProjectElementId> elementSet = new HashSet<ProjectElementId>();
 		
 		elementSet.add(ProjectElementId.PlanCycles);
 		
 		try {
 			
 			PaceProject testPP = new ExcelPaceProject(TEST_FILES_PROJECT_TEMPLATE_XLSX, elementSet);
 						
 			List<PlanCycle> list = testPP.getPlanCycles();
 			
 			assertNotNull(list);
 			assertEquals(3, list.size());
 									
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		}
 				
 	}
 	
 	public void testReadSeasons() {		
 
 		Set<ProjectElementId> elementSet = new HashSet<ProjectElementId>();
 		
 		elementSet.add(ProjectElementId.Seasons);
 		
 		try {
 			
 			PaceProject testPP = new ExcelPaceProject(TEST_FILES_PROJECT_TEMPLATE_XLSX, elementSet);
 						
 			List<Season> list = testPP.getSeasons();
 			
 			assertNotNull(list);
 			assertEquals(7, list.size());
 									
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		}
 				
 	}
 	
 	public void testReadRoles() {		
 
 		Set<ProjectElementId> elementSet = new HashSet<ProjectElementId>();
 		
 		elementSet.add(ProjectElementId.Roles);
 		
 		try {
 			
 			PaceProject testPP = new ExcelPaceProject(TEST_FILES_PROJECT_TEMPLATE_XLSX, elementSet);
 						
 			List<PafPlannerRole> list = testPP.getRoles();
 			
 			assertNotNull(list);
 			assertEquals(4, list.size());
 									
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		}
 				
 	}
 	
 	public void testReadCustomMenus() {		
 
 		Set<ProjectElementId> elementSet = new HashSet<ProjectElementId>();
 		
 		elementSet.add(ProjectElementId.CustomMenus);
 		
 		try {
 			
 			PaceProject testPP = new ExcelPaceProject(TEST_FILES_PROJECT_TEMPLATE_XLSX, elementSet);
 						
 			List<CustomMenuDef> list = testPP.getCustomMenus();
 			
 			assertNotNull(list);
 			assertEquals(4, list.size());
 			
 			for (CustomMenuDef cmd : list) {
 				
 				CustomActionDef[] actionDefAR = cmd.getCustomActionDefs();
 				
 				assertNotNull(actionDefAR);
 				assertEquals(actionDefAR.length, 1);
 				
 				assertNotNull(actionDefAR[0].getActionClassName());
 				assertNotNull(actionDefAR[0].getActionNamedParameters());
 				assertEquals(actionDefAR[0].getActionNamedParameters().length, 2);
 				
 			}
 			
 									
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		}
 				
 	}
 	
 	public void testReadCustomFunctions() {		
 
 		Set<ProjectElementId> elementSet = new HashSet<ProjectElementId>();
 		
 		elementSet.add(ProjectElementId.CustomFunctions);
 		
 		try {
 			
 			PaceProject testPP = new ExcelPaceProject(TEST_FILES_PROJECT_TEMPLATE_XLSX, elementSet);
 						
 			List<CustomFunctionDef> list = testPP.getCustomFunctions();
 			
 			assertNotNull(list);
 			assertEquals(12, list.size());
 												
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		}
 				
 	}
 
 	public void testReadViewGroups() {		
 
 		Set<ProjectElementId> elementSet = new HashSet<ProjectElementId>();
 		
 		elementSet.add(ProjectElementId.ViewGroups);
 		
 		try {
 			
 			PaceProject testPP = new ExcelPaceProject(TEST_FILES_PROJECT_TEMPLATE_XLSX, elementSet);
 						
 			Map<String, PafViewGroup> map = testPP.getViewGroups();
 			
 			assertNotNull(map);
 			assertEquals(8, map.size());
 						
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		}
 				
 	}
 	
 	public void testReadViews() {		
 
 		Set<ProjectElementId> elementSet = new HashSet<ProjectElementId>();
 		
 		elementSet.add(ProjectElementId.Views);
 		
 		try {
 			
 			PaceProject testPP = new ExcelPaceProject(TEST_FILES_PROJECT_TEMPLATE_XLSX, elementSet);
 						
 			List<PafView> list = testPP.getViews();
 			
 			assertNotNull(list);
 			assertEquals(82, list.size());
 												
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		}
 				
 	}
 	
 	public void testReadMemberTags() {		
 
 		Set<ProjectElementId> elementSet = new HashSet<ProjectElementId>();
 		
 		elementSet.add(ProjectElementId.MemberTags);
 		
 		try {
 			
 			PaceProject testPP = new ExcelPaceProject(TEST_FILES_PROJECT_TEMPLATE_XLSX, elementSet);
 						
 			List<MemberTagDef> list = testPP.getMemberTags();
 			
 			assertNotNull(list);
 			assertEquals(2, list.size());
 			
 			MemberTagDef m1 = list.get(0);
 			
 			assertNotNull(m1);
 			assertEquals("Measure_Desc", m1.getName());
 			assertEquals(1, m1.getDims().length);
 			
 			MemberTagDef m2 = list.get(1);
 			
 			assertNotNull(m2);
 			assertEquals("Dept_Desc", m2.getName());
 			assertEquals(2, m2.getDims().length);
 												
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		}
 				
 	}
 	
 	public void testReadUserSecurity() {		
 
 		Set<ProjectElementId> elementSet = new HashSet<ProjectElementId>();
 		
 		elementSet.add(ProjectElementId.UserSecurity);
 		
 		try {
 			
 			PaceProject testPP = new ExcelPaceProject(TEST_FILES_PROJECT_TEMPLATE_XLSX, elementSet);
 						
 			List<PafUserSecurity> list = testPP.getUserSecurity();
 			
 			assertNotNull(list);
 			assertEquals(6, list.size());
 			
 			/*for (PafUserSecurity userSecurity : list) {
 				
 				System.out.println("-------------------");
 				System.out.println(userSecurity.getUserName());
 				
 				for (String key : userSecurity.getRoleFilters().keySet() ) {
 					
 					PafWorkSpec[] workSpecAr = userSecurity.getRoleFilters().get(key);
 					
 					for (PafWorkSpec ws : workSpecAr ) {
 						
 						System.out.println(ws.getName());
 						
 						for (PafDimSpec pds : ws.getDimSpec()) {
 						
 							System.out.println("\t" + pds.getDimension() + ":" + pds.getExpressionList()[0]);
 							
 						}					
 						
 					}
 					
 				}				
 				
 			}*/
 												
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		}
 				
 	}
 	
 	public void testReadRoundingRules() {		
 
 		Set<ProjectElementId> elementSet = new HashSet<ProjectElementId>();
 		
 		elementSet.add(ProjectElementId.RoundingRules);
 		
 		try {
 			
 			PaceProject testPP = new ExcelPaceProject(TEST_FILES_PROJECT_TEMPLATE_XLSX, elementSet);
 						
 			List<RoundingRule> list = testPP.getRoundingRules();
 			
 			assertNotNull(list);
 			assertEquals(1, list.size());
 			assertNotNull(list.get(0).getMemberList());
 			assertEquals(1, list.get(0).getMemberList().size());
 												
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		}
 				
 	}
 
 	public void testReadViewSections() {		
 
 		Set<ProjectElementId> elementSet = new HashSet<ProjectElementId>();
 		
 		elementSet.add(ProjectElementId.ViewSections);
 		
 		try {
 			
 			PaceProject testPP = new ExcelPaceProject(TEST_FILES_PROJECT_TEMPLATE_XLSX, elementSet);
 						
 			List<PafViewSection> list = testPP.getViewSections();
 			
 			assertNotNull(list);
 			assertEquals(84, list.size());
 												
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		}
 				
 	}
 	
 	public void testReadRoleConfigs() {		
 
 		Set<ProjectElementId> elementSet = new HashSet<ProjectElementId>();
 		
 		elementSet.add(ProjectElementId.RoleConfigs);
 		
 		try {
 			
 			PaceProject testPP = new ExcelPaceProject(TEST_FILES_PROJECT_TEMPLATE_XLSX, elementSet);
 									
 			List<PafPlannerConfig> list = testPP.getRoleConfigurations();
 			
 			assertNotNull(list);
 			assertEquals(11, list.size());
 												
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		}
 				
 	}
 	
 	public void testReadRuleSets() {		
 
 		Set<ProjectElementId> elementSet = new HashSet<ProjectElementId>();
 		
 		elementSet.add(ProjectElementId.RuleSets);
 		
 		try {
 			
 			PaceProject testPP = new ExcelPaceProject(TEST_FILES_PROJECT_TEMPLATE_XLSX, elementSet);
 						
 			Map<String, RuleSet> map = testPP.getRuleSets();
 			
 			assertNotNull(map);
 			assertEquals(14, map.size());
 						
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		}
 				
 	}
 	
 	public void testWritePlanCycles() {		
 
 		Set<ProjectElementId> elementSet = new HashSet<ProjectElementId>();
 		
 		elementSet.add(ProjectElementId.PlanCycles);
 		
 		try {
 			
 			PaceProject testPP = new ExcelPaceProject(TEST_FILES_PROJECT_WRITE_XLSX, elementSet);
 						
 			List<PlanCycle> list = xmlPaceProject.getPlanCycles();
 			
 			for (PlanCycle pc : list ) {
 				
 				//pc.setLabel("TEST: " + pc.getLabel());
 				
 			}
 			
 			testPP.setPlanCycles(list);
 			
 			ExcelPaceProject excelPP = (ExcelPaceProject) testPP;
 			
 			excelPP.setCellReferencingEnabled(true);
 			
 			excelPP.save();
 			
 			assertNotNull(list);
 
 			assertEquals(3, list.size());
 			
 			//testPP.saveTo(saveToProjectDir)();
 			
 			//FileUtils.copy(new File(TEST_FILES_PROJECT_WRITE_XLSX), new File(TEST_FILES_PROJECT_TESTING_XLSX));
 			
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		} catch (ProjectSaveException e) {
 			fail(e.getMessage());
 		}
 				
 	}
 	
 	public void testWriteNumericFormats() {		
 
 		Set<ProjectElementId> elementSet = new HashSet<ProjectElementId>();
 		
 		elementSet.add(ProjectElementId.NumericFormats);
 		
 		try {
 			
 			PaceProject testPP = new ExcelPaceProject(TEST_FILES_PROJECT_WRITE_XLSX, elementSet);
 				
 			Map<String, PafNumberFormat> numericFormatMap = testPP.getNumericFormats();
 			
 			Set<String> keyList = numericFormatMap.keySet();
 			
 			for (String key : keyList ) {
 				
 				PafNumberFormat numberFormat = numericFormatMap.get(key);
 				
 				//numberFormat.setName("TEST: " + numberFormat.getName());
 				
 				numericFormatMap.put(key, numberFormat);
 				
 			}
 						
 			testPP.setNumericFormats(numericFormatMap);
 			
 			testPP.save();
 									
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		} catch (ProjectSaveException e) {
 			fail(e.getMessage());		
 		}
 				
 	}
 	
 	public void testWriteGlobalStyles() {		
 
 		Set<ProjectElementId> elementSet = new HashSet<ProjectElementId>();
 		
 		elementSet.add(ProjectElementId.GlobalStyles);
 		
 		try {
 			
 			PaceProject testPP = new ExcelPaceProject(TEST_FILES_PROJECT_WRITE_XLSX, elementSet);
 				
 			Map<String, PafStyle> globalStyleMap = testPP.getGlobalStyles();
 			
 			Set<String> keyList = globalStyleMap.keySet();
 			
 			for (String key : keyList ) {
 				
 				PafStyle pafStyle = globalStyleMap.get(key);
 				
 				//pafStyle.setName("TEST: " + pafStyle.getName());
 				
 				globalStyleMap.put(key, pafStyle);
 				
 			}
 						
 			testPP.setGlobalStyles(globalStyleMap);
 			
 			testPP.save();
 									
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		} catch (ProjectSaveException e) {
 			fail(e.getMessage());		
 		}
 				
 	}
 
 	public void testWriteMeasures() {		
 
 		Set<ProjectElementId> elementSet = new HashSet<ProjectElementId>();
 		
 		elementSet.add(ProjectElementId.Measures);
 		
 		try {
 			
 			PaceProject testPP = new ExcelPaceProject(TEST_FILES_PROJECT_WRITE_XLSX, elementSet);
 						
 			List<MeasureDef> measureList = xmlPaceProject.getMeasures();
 			
			assertEquals(88, measureList.size());
 			
 			MeasureDef newMeasure = new MeasureDef();
 			newMeasure.setName("TEST");
 			newMeasure.setType(MeasureType.Aggregate);
 			
 			Map<String, String> aliasMap = new HashMap<String, String>();
 			
 			aliasMap.put("key1", "value1");
 			aliasMap.put("key2", "value2");
 			
 			newMeasure.setAliases(aliasMap);
 			
 			measureList.add(newMeasure);
 			
 			testPP.setMeasures(measureList);
 			
 			ExcelPaceProject excelPP = (ExcelPaceProject) testPP;
 			
 			excelPP.setCellReferencingEnabled(true);
 			
 			excelPP.save();
 						
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		} catch (ProjectSaveException e) {
 			fail(e.getMessage());
 		}
 				
 	}
 	
 	public void testWriteVersions() {		
 
 		Set<ProjectElementId> elementSet = new HashSet<ProjectElementId>();
 		
 		elementSet.add(ProjectElementId.Versions);
 		
 		try {
 			
 			PaceProject testPP = new ExcelPaceProject(TEST_FILES_PROJECT_WRITE_XLSX, elementSet);
 						
 			List<VersionDef> versionList = xmlPaceProject.getVersions();
 						
 			VersionDef vd = new VersionDef();
 			vd.setName("Test Multi Row");
 			vd.setType(VersionType.Variance);
 			
 			Map<String, String> aliasMap = new HashMap<String, String>();
 			
 			aliasMap.put("key1", "value1");
 			aliasMap.put("key2", "value2");
 			
 			vd.setAliases(aliasMap);
 			
 			VersionFormula vf = new VersionFormula();
 			
 			vf.setBaseVersion("WP");
 			vf.setCompareVersion("LY");
 			vf.setVarianceType(VersionVarianceType.SimpleVariance);
 			
 			PafDimSpec pds1 = new PafDimSpec();
 			pds1.setDimension("Product");
 			pds1.setExpressionList(new String[] { "@PARENT"});
 			
 			PafDimSpec pds2 = new PafDimSpec();
 			pds2.setDimension("Measure");
 			pds2.setExpressionList(new String[] { "SLS_DLR", "SLS_UNIT"});
 			
 			PafDimSpec pds3 = new PafDimSpec();
 			pds3.setDimension("Time");
 			pds3.setExpressionList(new String[] { "@PARENT"});
 			
 			vf.setCompareIsSpec(new PafDimSpec[] { pds1, pds2, pds3 } );
 			
 			vd.setVersionFormula(vf);
 			
 			versionList.add(vd);			
 			
 			testPP.setVersions(versionList);
 			
 			ExcelPaceProject excelPP = (ExcelPaceProject) testPP;
 			
 			excelPP.setCellReferencingEnabled(true);
 			
 			excelPP.save();
 						
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		} catch (ProjectSaveException e) {
 			fail(e.getMessage());
 		}
 				
 	}
 	
 	public void testWriteHierarchyFormats() {		
 
 		Set<ProjectElementId> elementSet = new HashSet<ProjectElementId>();
 		
 		elementSet.add(ProjectElementId.HierarchyFormats);
 		
 		try {
 			
 			PaceProject testPP = new ExcelPaceProject(TEST_FILES_PROJECT_WRITE_XLSX, elementSet);
 				
 			Map<String, HierarchyFormat> hierarcyFormatMap = testPP.getHierarchyFormats();
 						
 			testPP.setHierarchyFormats(hierarcyFormatMap);
 			
 			ExcelPaceProject excelPP = (ExcelPaceProject) testPP;
 			
 			excelPP.setCellReferencingEnabled(true);
 			
 			excelPP.save();
 									
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		} catch (ProjectSaveException e) {
 			fail(e.getMessage());		
 		}
 				
 	}
 	
 	public void testWriteSeasons() {		
 
 		Set<ProjectElementId> elementSet = new HashSet<ProjectElementId>();
 		
 		elementSet.add(ProjectElementId.Seasons);
 		
 		try {
 			
 			PaceProject testPP = new ExcelPaceProject(TEST_FILES_PROJECT_WRITE_XLSX, elementSet);
 						
 			List<Season> list = xmlPaceProject.getSeasons();
 						
 			testPP.setSeasons(list);
 			
 			ExcelPaceProject excelPP = (ExcelPaceProject) testPP;
 			
 			excelPP.setCellReferencingEnabled(true);
 			
 			excelPP.save();
 						
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		} catch (ProjectSaveException e) {
 			fail(e.getMessage());
 		}
 				
 	}
 	
 	public void testWriteRoles() {		
 
 		Set<ProjectElementId> elementSet = new HashSet<ProjectElementId>();
 		
 		elementSet.add(ProjectElementId.Roles);
 		
 		try {
 			
 			PaceProject testPP = new ExcelPaceProject(TEST_FILES_PROJECT_WRITE_XLSX, elementSet);
 						
 			List<PafPlannerRole> list = xmlPaceProject.getRoles();
 						
 			testPP.setRoles(list);
 			
 			ExcelPaceProject excelPP = (ExcelPaceProject) testPP;
 			
 			excelPP.setCellReferencingEnabled(true);
 			
 			excelPP.save();
 						
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		} catch (ProjectSaveException e) {
 			fail(e.getMessage());
 		}
 				
 	}
 	
 	public void testWriteUserSecurity() {		
 
 		Set<ProjectElementId> elementSet = new HashSet<ProjectElementId>();
 		
 		elementSet.add(ProjectElementId.UserSecurity);
 		
 		try {
 			
 			PaceProject testPP = new ExcelPaceProject(TEST_FILES_PROJECT_WRITE_XLSX, elementSet);
 						
 			List<PafUserSecurity> list = xmlPaceProject.getUserSecurity();
 						
 			testPP.setUserSecurity(list);
 			
 			ExcelPaceProject excelPP = (ExcelPaceProject) testPP;
 			
 			excelPP.setCellReferencingEnabled(true);
 			
 			excelPP.save();
 						
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		} catch (ProjectSaveException e) {
 			fail(e.getMessage());
 		}
 				
 	}
 	
 	public void testWriteDynamicMembers() {		
 
 		Set<ProjectElementId> elementSet = new HashSet<ProjectElementId>();
 		
 		elementSet.add(ProjectElementId.DynamicMembers);
 		
 		try {
 			
 			PaceProject testPP = new ExcelPaceProject(TEST_FILES_PROJECT_WRITE_XLSX, elementSet);
 						
 			List<DynamicMemberDef> list = xmlPaceProject.getDynamicMembers();
 						
 			testPP.setDynamicMembers(list);
 			
 			ExcelPaceProject excelPP = (ExcelPaceProject) testPP;
 			
 			excelPP.setCellReferencingEnabled(true);
 			
 			excelPP.save();
 									
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		} catch (ProjectSaveException e) {
 			fail(e.getMessage());
 		}
 				
 	}
 	
 	public void testWriteViewSections() {		
 
 		Set<ProjectElementId> elementSet = new HashSet<ProjectElementId>();
 		
 		elementSet.add(ProjectElementId.ViewSections);
 		
 		try {
 			
 			PaceProject testPP = new ExcelPaceProject(TEST_FILES_PROJECT_WRITE_XLSX, elementSet);
 						
 			List<PafViewSection> list = xmlPaceProject.getViewSections();
 						
 			testPP.setViewSections(list);
 			
 			testPP.save();
 			
 			//reload data
 			testPP.loadData(elementSet);
 						
 			List<PafViewSection> readList = testPP.getViewSections();
 			
 			assertEquals(list.size(), readList.size());
 			
 			for (int i = 0; i < list.size(); i++ ) {
 				
 				assertEquals(list.get(i).getName(), readList.get(i).getName());
 				
 			}
 									
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		} catch (ProjectSaveException e) {
 			fail(e.getMessage());
 		} catch (PafException e) {
 			fail(e.getMessage());
 		}
 				
 	}
 	
 	public void testWriteViews() {		
 
 		Set<ProjectElementId> elementSet = new HashSet<ProjectElementId>();
 		
 		elementSet.add(ProjectElementId.Views);
 		
 		try {
 			
 			PaceProject testPP = new ExcelPaceProject(TEST_FILES_PROJECT_WRITE_XLSX, elementSet);
 						
 			List<PafView> list = xmlPaceProject.getViews();
 						
 			testPP.setViews(list);
 			
 			ExcelPaceProject excelPP = (ExcelPaceProject) testPP;
 			
 			excelPP.setCellReferencingEnabled(true);
 			
 			excelPP.save();
 												
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		} catch (ProjectSaveException e) {
 			fail(e.getMessage());		
 		}
 				
 	}
 	
 	public void testWriteViewGroups() {		
 
 		Set<ProjectElementId> elementSet = new HashSet<ProjectElementId>();
 		
 		elementSet.add(ProjectElementId.ViewGroups);
 		
 		try {
 			
 			PaceProject testPP = new ExcelPaceProject(TEST_FILES_PROJECT_WRITE_XLSX, elementSet);
 								
 			Map<String, PafViewGroup> map = xmlPaceProject.getViewGroups();
 			
 			testPP.setViewGroups(map);
 			
 			ExcelPaceProject excelPP = (ExcelPaceProject) testPP;
 			
 			excelPP.setCellReferencingEnabled(true);
 			
 			excelPP.save();
 			
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		} catch (ProjectSaveException e) {
 			fail(e.getMessage());
 		}
 				
 	}
 	
 	public void testWriteCustomMenus() {		
 
 		Set<ProjectElementId> elementSet = new HashSet<ProjectElementId>();
 		
 		elementSet.add(ProjectElementId.CustomMenus);
 		
 		try {
 			
 			PaceProject testPP = new ExcelPaceProject(TEST_FILES_PROJECT_WRITE_XLSX, elementSet);
 						
 			List<CustomMenuDef> list = xmlPaceProject.getCustomMenus();
 						
 			testPP.setCustomMenus(list);
 			
 			ExcelPaceProject excelPP = (ExcelPaceProject) testPP;
 			
 			excelPP.setCellReferencingEnabled(true);
 			
 			excelPP.save();
 												
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		} catch (ProjectSaveException e) {
 			fail(e.getMessage());		
 		}
 				
 	}
 	
 	public void testWriteCustomFunctions() {		
 
 		Set<ProjectElementId> elementSet = new HashSet<ProjectElementId>();
 		
 		elementSet.add(ProjectElementId.CustomFunctions);
 		
 		try {
 			
 			PaceProject testPP = new ExcelPaceProject(TEST_FILES_PROJECT_WRITE_XLSX, elementSet);
 						
 			List<CustomFunctionDef> list = xmlPaceProject.getCustomFunctions();
 						
 			for (CustomFunctionDef customFunction : list ) {
 				
 				customFunction.setClassName("TEST " + customFunction.getClassName());
 				
 			}
 			
 			testPP.setCustomFunctions(list);
 			
 			testPP.save();
 												
 			PaceProject testPP2	= new ExcelPaceProject(TEST_FILES_PROJECT_WRITE_XLSX, elementSet);
 			
 			List<CustomFunctionDef> readCustomFunctionList = testPP2.getCustomFunctions();
 			
 			assertEquals(list.size(), readCustomFunctionList.size());
 			
 			for (CustomFunctionDef customFunction : readCustomFunctionList ) {
 				
 				assertTrue(customFunction.getClassName().startsWith("TEST"));
 				
 			}
 			
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		} catch (ProjectSaveException e) {
 			fail(e.getMessage());		
 		}
 				
 	}
 	
 	public void testWriteRoundingRules() {		
 
 		Set<ProjectElementId> elementSet = new HashSet<ProjectElementId>();
 		
 		elementSet.add(ProjectElementId.RoundingRules);
 		
 		try {
 			
 			PaceProject testPP = new ExcelPaceProject(TEST_FILES_PROJECT_WRITE_XLSX, elementSet);
 						
 			List<RoundingRule> list = xmlPaceProject.getRoundingRules();
 						
 			for (RoundingRule roundingRule: list ) {
 				
 				roundingRule.setRoundingFunction("TEST " + roundingRule.getRoundingFunction());
 				
 			}
 			
 			RoundingRule rr = new RoundingRule();
 			
 			rr.setRoundingFunction("TEST Function name");
 			rr.setDigits(999);
 			
 			MemberSet ms1 = new MemberSet();
 			ms1.setDimension("DIM1");
 			ms1.setMember("Member 1");
 			
 			MemberSet ms2 = new MemberSet();
 			ms2.setDimension("DIM2");
 			ms2.setMember("Member 2");
 			
 			rr.setMemberList(Arrays.asList(ms1, ms2));
 			
 			list.add(rr);
 			
 			testPP.setRoundingRules(list);
 			
 			testPP.save();
 												
 			PaceProject testPP2	= new ExcelPaceProject(TEST_FILES_PROJECT_WRITE_XLSX, elementSet);
 			
 			List<RoundingRule> readList = testPP2.getRoundingRules();
 			
 			assertEquals(list.size(), readList.size());
 			
 			for (RoundingRule roundingRule: readList ) {
 				
 				assertTrue(roundingRule.getRoundingFunction().startsWith("TEST"));
 			
 				if ( roundingRule.getRoundingFunction().equals(rr.getRoundingFunction())) {
 					
 					assertEquals(2, rr.getMemberList().size());
 					
 				}
 				
 			}
 			
 			ExcelPaceProject excelPP = (ExcelPaceProject) testPP;
 			
 			excelPP.setCellReferencingEnabled(true);
 			
 			excelPP.save();
 			
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		} catch (ProjectSaveException e) {
 			fail(e.getMessage());		
 		}
 				
 	}
 	
 	public void testWriteUserSelections() {		
 
 		Set<ProjectElementId> elementSet = new HashSet<ProjectElementId>();
 		
 		elementSet.add(ProjectElementId.UserSelections);
 		
 		try {
 			
 			PaceProject testPP = new ExcelPaceProject(TEST_FILES_PROJECT_WRITE_XLSX, elementSet);
 						
 			List<PafUserSelection> list = xmlPaceProject.getUserSelections();
 						
 			testPP.setUserSelections(list);			
 					
 			ExcelPaceProject excelPP = (ExcelPaceProject) testPP;
 			
 			excelPP.setCellReferencingEnabled(true);
 			
 			excelPP.save();											
 						
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		} catch (ProjectSaveException e) {
 			fail(e.getMessage());		
 		}
 				
 	}
 	
 	public void testWriteMemberTags() {		
 
 		Set<ProjectElementId> elementSet = new HashSet<ProjectElementId>();
 		
 		elementSet.add(ProjectElementId.MemberTags);
 		
 		try {
 			
 			PaceProject testPP = new ExcelPaceProject(TEST_FILES_PROJECT_WRITE_XLSX, elementSet);
 						
 			List<MemberTagDef> list = xmlPaceProject.getMemberTags();
 						
 			testPP.setMemberTags(list);			
 					
 			ExcelPaceProject excelPP = (ExcelPaceProject) testPP;
 			
 			excelPP.setCellReferencingEnabled(true);
 			
 			excelPP.save();											
 						
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		} catch (ProjectSaveException e) {
 			fail(e.getMessage());		
 		}
 				
 	}
 	
 	public void testWriteApplications() {		
 
 		Set<ProjectElementId> elementSet = new HashSet<ProjectElementId>();
 		
 		elementSet.add(ProjectElementId.ApplicationDef);
 		
 		try {
 			
 			PaceProject testPP = new ExcelPaceProject(TEST_FILES_PROJECT_WRITE_XLSX, elementSet);
 						
 			List<PafApplicationDef> list = xmlPaceProject.getApplicationDefinitions();
 						
 			testPP.setApplicationDefinitions(list);		
 			
 			ExcelPaceProject excelPP = (ExcelPaceProject) testPP;
 			
 			excelPP.setCellReferencingEnabled(true);
 					
 			testPP.save();											
 						
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {								
 			fail(e.getMessage());
 		} catch (ProjectSaveException e) {
 			fail(e.getMessage());		
 		}
 				
 	}
 
 	public void testWriteRoleConfigs() {		
 
 		Set<ProjectElementId> elementSet = new HashSet<ProjectElementId>();
 		
 		elementSet.add(ProjectElementId.RoleConfigs);
 		
 		try {
 			
 			PaceProject testPP = new ExcelPaceProject(TEST_FILES_PROJECT_WRITE_XLSX, elementSet);
 						
 			List<PafPlannerConfig> list = xmlPaceProject.getRoleConfigurations();
 						
 			testPP.setRoleConfigurations(list);
 			
 			ExcelPaceProject excelPP = (ExcelPaceProject) testPP;
 			
 			excelPP.setCellReferencingEnabled(true);
 			
 			excelPP.save();
 												
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		} catch (ProjectSaveException e) {
 			fail(e.getMessage());		
 		}
 				
 	}
 	public void testWriteRuleSets() {		
 
 		Set<ProjectElementId> elementSet = new HashSet<ProjectElementId>();
 		
 		elementSet.add(ProjectElementId.RuleSets);
 		
 		try {
 			
 			PaceProject testPP = new ExcelPaceProject(TEST_FILES_PROJECT_WRITE_XLSX, elementSet);
 								
 			Map<String, RuleSet> map = xmlPaceProject.getRuleSets();
 			
 			testPP.setRuleSets(map);
 			
 			ExcelPaceProject excelPP = (ExcelPaceProject) testPP;
 			
 			excelPP.setCellReferencingEnabled(true);
 			
 			excelPP.save();
 			
 			assertEquals(map.size(), excelPP.getRuleSets().size());
 			
 			for (String key : map.keySet() ) {
 				
 				assertTrue(excelPP.getRuleSets().containsKey(key));
 				
 			}			
 									
 			String alpha = "abcdefghijklmnopqrstuvwxyz";
 			
 			RuleSet rs1 = new RuleSet();
 			rs1.setName(alpha + 1);
 			
 			RuleSet rs2 = new RuleSet();
 			rs2.setName(alpha + 2);
 			
 			RuleSet rs3 = new RuleSet();
 			rs3.setName(alpha + 3);
 			
 			map.clear();
 			
 			assertEquals(0, map.size());
 			
 			map.put(rs1.getName(), rs1);
 			map.put(rs2.getName(), rs2);
 			map.put(rs3.getName(), rs3);
 			
 			excelPP.setRuleSets(map);		
 			
 			excelPP.setCellReferencingEnabled(false);
 			
 			excelPP.save();
 			
 			excelPP = new ExcelPaceProject(TEST_FILES_PROJECT_WRITE_XLSX, elementSet);
 			
 			assertEquals(map.size(), excelPP.getRuleSets().size());
 			
 			for (String key : map.keySet() ) {
 				
 				assertTrue(excelPP.getRuleSets().containsKey(key));
 				
 			}
 			
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		} catch (ProjectSaveException e) {
 			fail(e.getMessage());
 		}
 				
 	}
 	
 	public void testFullWriteRead() {
 		
 		PaceProject pp = xmlPaceProject.convertTo(ProjectSerializationType.XLSX);
 		
 		assertTrue(pp instanceof ExcelPaceProject);
 		
 		ExcelPaceProject newExcelPP = (ExcelPaceProject) pp;
 		
 		File excelFile = new File(TEST_FILES_PROJECT_TESTING2_XLSX);
 		
 		if ( excelFile.exists()) {
 			
 			excelFile.delete();
 			
 		}
 		
 		//without cell referencing
 		try {
 			
 			newExcelPP.saveTo(TEST_FILES_PROJECT_TESTING2_XLSX);
 			newExcelPP.saveTo(TEST_FILES_PROJECT_TESTING2_XLSM);
 			
 		} catch (ProjectSaveException e) {
 			fail(e.getMessage());
 		}
 		
 		try {
 			
 			ExcelPaceProject excelPP = new ExcelPaceProject(TEST_FILES_PROJECT_TESTING2_XLSX);
 
 			assertEquals(0, excelPP.getProjectErrorList().size());
 			
 			//paf apps
 			assertEquals(xmlPaceProject.getApplicationDefinitions().size(), excelPP.getApplicationDefinitions().size());
 			testEquals(xmlPaceProject.getApplicationDefinitions().toArray(), excelPP.getApplicationDefinitions().toArray());
 				
 			//versions
 			assertEquals(xmlPaceProject.getVersions().size(), excelPP.getVersions().size());
 			testEquals(xmlPaceProject.getVersions().toArray(), excelPP.getVersions().toArray());
 			
 			//measures
 			assertEquals(xmlPaceProject.getMeasures().size(), excelPP.getMeasures().size());
 			testEquals(xmlPaceProject.getMeasures().toArray(), excelPP.getMeasures().toArray());
 			
 			//user selections
 			assertEquals(xmlPaceProject.getUserSelections().size(), excelPP.getUserSelections().size());
 			testEquals(xmlPaceProject.getUserSelections().toArray(), excelPP.getUserSelections().toArray());
 			
 			//dynamic members
 			assertEquals(xmlPaceProject.getDynamicMembers().size(), excelPP.getDynamicMembers().size());
 			testEquals(xmlPaceProject.getDynamicMembers().toArray(), excelPP.getDynamicMembers().toArray());
 			
 			
 			//plan cycles
 			assertEquals(xmlPaceProject.getPlanCycles().size(), excelPP.getPlanCycles().size());			
 			testEquals(xmlPaceProject.getPlanCycles().toArray(), excelPP.getPlanCycles().toArray());			
 			
 			//seasons
 			assertEquals(xmlPaceProject.getSeasons().size(), excelPP.getSeasons().size());
 			testEquals(xmlPaceProject.getSeasons().toArray(), excelPP.getSeasons().toArray());
 			
 			//roles
 			assertEquals(xmlPaceProject.getRoles().size(), excelPP.getRoles().size());
 			testEquals(xmlPaceProject.getRoles().toArray(), excelPP.getRoles().toArray());
 			
 			//custom menus
 			assertEquals(xmlPaceProject.getCustomMenus().size(), excelPP.getCustomMenus().size());
 			testEquals(xmlPaceProject.getCustomMenus().toArray(), excelPP.getCustomMenus().toArray());
 			
 			//custom functions
 			assertEquals(xmlPaceProject.getCustomFunctions().size(), excelPP.getCustomFunctions().size());
 			testEquals(xmlPaceProject.getCustomFunctions().toArray(), excelPP.getCustomFunctions().toArray());
 			
 			//views
 			assertEquals(xmlPaceProject.getViews().size(), excelPP.getViews().size());
 			testEquals(xmlPaceProject.getViews().toArray(), excelPP.getViews().toArray());
 			
 			//member tags
 			assertEquals(xmlPaceProject.getMemberTags().size(), excelPP.getMemberTags().size());
 			testEquals(xmlPaceProject.getMemberTags().toArray(), excelPP.getMemberTags().toArray());
 			
 			//user security
 			assertEquals(xmlPaceProject.getUserSecurity().size(), excelPP.getUserSecurity().size());
 			testEquals(xmlPaceProject.getUserSecurity().toArray(), excelPP.getUserSecurity().toArray());
 			
 			//rounding rules
 			assertEquals(xmlPaceProject.getRoundingRules().size(), excelPP.getRoundingRules().size());
 			testEquals(xmlPaceProject.getRoundingRules().toArray(), excelPP.getRoundingRules().toArray());
 			
 			//view sections
 			assertEquals(xmlPaceProject.getViewSections().size(), excelPP.getViewSections().size());
 			//TODO: testEquals for view section
 			
 			//role configurations
 			assertEquals(xmlPaceProject.getRoleConfigurations().size(), excelPP.getRoleConfigurations().size());
 			testEquals(xmlPaceProject.getRoleConfigurations().toArray(), excelPP.getRoleConfigurations().toArray());
 			
 			//numeric formats
 			assertEquals(xmlPaceProject.getNumericFormats().size(), excelPP.getNumericFormats().size());
 			assertTrue(testEquals(xmlPaceProject.getNumericFormats().keySet(), excelPP.getNumericFormats().keySet()));
 			assertTrue(testEquals(xmlPaceProject.getNumericFormats().values(), excelPP.getNumericFormats().values()));
 			
 			//global styles
 			assertEquals(xmlPaceProject.getGlobalStyles().size(), excelPP.getGlobalStyles().size());
 			assertTrue(testEquals(xmlPaceProject.getGlobalStyles().keySet(), excelPP.getGlobalStyles().keySet()));
 			assertTrue(testEquals(xmlPaceProject.getGlobalStyles().values(), excelPP.getGlobalStyles().values()));
 			
 			//hierarchy formats			
 			assertEquals(xmlPaceProject.getHierarchyFormats().size(), excelPP.getHierarchyFormats().size());
 			assertTrue(testEquals(xmlPaceProject.getHierarchyFormats().keySet(), excelPP.getHierarchyFormats().keySet()));
 			assertTrue(testEquals(xmlPaceProject.getHierarchyFormats().values(), excelPP.getHierarchyFormats().values()));
 						
 			//view groups
 			assertEquals(xmlPaceProject.getViewGroups().size(), excelPP.getViewGroups().size());
 			assertTrue(testEquals(xmlPaceProject.getViewGroups().keySet(), excelPP.getViewGroups().keySet()));
 			assertTrue(testEquals(xmlPaceProject.getViewGroups().values(), excelPP.getViewGroups().values()));
 			
 			
 			//rule sets
 			assertEquals(xmlPaceProject.getRuleSets().size(), excelPP.getRuleSets().size());
 			assertTrue(testEquals(xmlPaceProject.getRuleSets().keySet(), excelPP.getRuleSets().keySet()));
 			assertTrue(testEquals(xmlPaceProject.getRuleSets().values(), excelPP.getRuleSets().values()));
 			
 			
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		}
 		
 		//with cell referencing
 		try {
 			
 			((ExcelPaceProject) newExcelPP).setCellReferencingEnabled(true);
 			
 			newExcelPP.saveTo(TEST_FILES_PROJECT_TESTING2_XLSX);
 			newExcelPP.saveTo(TEST_FILES_PROJECT_TESTING2_XLSM);
 			
 		} catch (ProjectSaveException e) {
 			fail(e.getMessage());
 		}
 		
 		try {
 			
 			ExcelPaceProject excelPP = new ExcelPaceProject(TEST_FILES_PROJECT_TESTING2_XLSX);
 		
 			//paf apps
 			assertEquals(xmlPaceProject.getApplicationDefinitions().size(), excelPP.getApplicationDefinitions().size());
 			testEquals(xmlPaceProject.getApplicationDefinitions().toArray(), excelPP.getApplicationDefinitions().toArray());
 				
 			//versions
 			assertEquals(xmlPaceProject.getVersions().size(), excelPP.getVersions().size());
 			testEquals(xmlPaceProject.getVersions().toArray(), excelPP.getVersions().toArray());
 			
 			//measures
 			assertEquals(xmlPaceProject.getMeasures().size(), excelPP.getMeasures().size());
 			testEquals(xmlPaceProject.getMeasures().toArray(), excelPP.getMeasures().toArray());
 			
 			//user selections
 			assertEquals(xmlPaceProject.getUserSelections().size(), excelPP.getUserSelections().size());
 			testEquals(xmlPaceProject.getUserSelections().toArray(), excelPP.getUserSelections().toArray());
 			
 			//dynamic members
 			assertEquals(xmlPaceProject.getDynamicMembers().size(), excelPP.getDynamicMembers().size());
 			testEquals(xmlPaceProject.getDynamicMembers().toArray(), excelPP.getDynamicMembers().toArray());
 			
 			
 			//plan cycles
 			assertEquals(xmlPaceProject.getPlanCycles().size(), excelPP.getPlanCycles().size());			
 			testEquals(xmlPaceProject.getPlanCycles().toArray(), excelPP.getPlanCycles().toArray());			
 			
 			//seasons
 			assertEquals(xmlPaceProject.getSeasons().size(), excelPP.getSeasons().size());
 			testEquals(xmlPaceProject.getSeasons().toArray(), excelPP.getSeasons().toArray());
 			
 			//roles
 			assertEquals(xmlPaceProject.getRoles().size(), excelPP.getRoles().size());
 			testEquals(xmlPaceProject.getRoles().toArray(), excelPP.getRoles().toArray());
 			
 			//custom menus
 			assertEquals(xmlPaceProject.getCustomMenus().size(), excelPP.getCustomMenus().size());
 			testEquals(xmlPaceProject.getCustomMenus().toArray(), excelPP.getCustomMenus().toArray());
 			
 			//custom functions
 			assertEquals(xmlPaceProject.getCustomFunctions().size(), excelPP.getCustomFunctions().size());
 			testEquals(xmlPaceProject.getCustomFunctions().toArray(), excelPP.getCustomFunctions().toArray());
 			
 			//views
 			assertEquals(xmlPaceProject.getViews().size(), excelPP.getViews().size());
 			testEquals(xmlPaceProject.getViews().toArray(), excelPP.getViews().toArray());
 			
 			//member tags
 			assertEquals(xmlPaceProject.getMemberTags().size(), excelPP.getMemberTags().size());
 			testEquals(xmlPaceProject.getMemberTags().toArray(), excelPP.getMemberTags().toArray());
 			
 			//user security
 			assertEquals(xmlPaceProject.getUserSecurity().size(), excelPP.getUserSecurity().size());
 			testEquals(xmlPaceProject.getUserSecurity().toArray(), excelPP.getUserSecurity().toArray());
 			
 			//rounding rules
 			assertEquals(xmlPaceProject.getRoundingRules().size(), excelPP.getRoundingRules().size());
 			testEquals(xmlPaceProject.getRoundingRules().toArray(), excelPP.getRoundingRules().toArray());
 			
 			//view sections
 			assertEquals(xmlPaceProject.getViewSections().size(), excelPP.getViewSections().size());
 			//TODO: testEquals for view section
 			
 			//role configurations
 			assertEquals(xmlPaceProject.getRoleConfigurations().size(), excelPP.getRoleConfigurations().size());
 			testEquals(xmlPaceProject.getRoleConfigurations().toArray(), excelPP.getRoleConfigurations().toArray());
 			
 			//numeric formats
 			assertEquals(xmlPaceProject.getNumericFormats().size(), excelPP.getNumericFormats().size());
 			assertTrue(testEquals(xmlPaceProject.getNumericFormats().keySet(), excelPP.getNumericFormats().keySet()));
 			assertTrue(testEquals(xmlPaceProject.getNumericFormats().values(), excelPP.getNumericFormats().values()));
 			
 			//global styles
 			assertEquals(xmlPaceProject.getGlobalStyles().size(), excelPP.getGlobalStyles().size());
 			assertTrue(testEquals(xmlPaceProject.getGlobalStyles().keySet(), excelPP.getGlobalStyles().keySet()));
 			assertTrue(testEquals(xmlPaceProject.getGlobalStyles().values(), excelPP.getGlobalStyles().values()));
 			
 			//hierarchy formats			
 			assertEquals(xmlPaceProject.getHierarchyFormats().size(), excelPP.getHierarchyFormats().size());
 			assertTrue(testEquals(xmlPaceProject.getHierarchyFormats().keySet(), excelPP.getHierarchyFormats().keySet()));
 			assertTrue(testEquals(xmlPaceProject.getHierarchyFormats().values(), excelPP.getHierarchyFormats().values()));
 						
 			//view groups
 			assertEquals(xmlPaceProject.getViewGroups().size(), excelPP.getViewGroups().size());
 			assertTrue(testEquals(xmlPaceProject.getViewGroups().keySet(), excelPP.getViewGroups().keySet()));
 			assertTrue(testEquals(xmlPaceProject.getViewGroups().values(), excelPP.getViewGroups().values()));
 			
 			
 			//rule sets
 			assertEquals(xmlPaceProject.getRuleSets().size(), excelPP.getRuleSets().size());
 			assertTrue(testEquals(xmlPaceProject.getRuleSets().keySet(), excelPP.getRuleSets().keySet()));
 			assertTrue(testEquals(xmlPaceProject.getRuleSets().values(), excelPP.getRuleSets().values()));
 			
 			
 		} catch (InvalidPaceProjectInputException e) {
 			fail(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			fail(e.getMessage());
 		}		
 		
 	}
 	
 	private boolean testEquals(Collection set1, Collection set2) {
 		
 		if ( (set1 == null && set2 != null) || ( set1 != null && set2 == null) ) {
 			
 			return false;
 			
 		} else if ( set1.size() != set2.size() ) {
 			
 			return false;
 			
 		} else {
 			
 			for ( Object setItem : set1 ) {
 				
 				if ( ! set2.contains(setItem) ) {
 					
 					return false;
 					
 				}
 				
 			}
 			
 		}
 		
 		return true;
 		
 	}
 	
 	private void testEquals(Object[] ar1, Object[] ar2) {
 
 		if ( ar1 != null && ar2 != null && ar1.length == ar2.length) {
 			
 			for (int i = 0; i < ar1.length; i++ ) {				
 				
 				assertEquals(ar1[i], ar2[i]);
 				
 				assertEquals(ar1[i].hashCode(), ar2[i].hashCode());
 				
 			}
 		} else {
 			
 			fail("Problem with equals");
 			
 		}
 		
 	}
 
 	public void testIfFileOpen() {
 		
 		try {
 		
 			File file = new File(TEST_FILES_PROJECT_WRITE_XLSX);
 			FileChannel channel = new RandomAccessFile(file, "rw").getChannel();
 			
 			FileLock lock = channel.lock();
 			
 			try {
 				
 				ExcelPaceProject pp = (ExcelPaceProject) xmlPaceProject.convertTo(ProjectSerializationType.XLSX);				
 				pp.saveTo(TEST_FILES_PROJECT_WRITE_XLSX);
 				fail("Exception should be thrown");
 			} catch (ProjectSaveException e) {
 				//should get here
 				System.out.println(e.getMessage());
 			}
 			
 			lock.release();
 			
 			channel.close();
 		
 		} catch (Exception e) {
 			
 			fail(e.getMessage());
 			
 		}
 		
 		try {
 			
 			ExcelPaceProject pp = (ExcelPaceProject) xmlPaceProject.convertTo(ProjectSerializationType.XLSX);				
 			pp.saveTo(TEST_FILES_PROJECT_WRITE_XLSX);
 			
 		} catch (ProjectSaveException e) {
 			fail(e.getMessage());
 		}
 
 	}
 	
 	
 	public void testGetProjectIdSetDependencies() {
 		
 		assertNotNull(ExcelPaceProject.getProjectIdSetDependencies(null));
 		assertEquals(0, ExcelPaceProject.getProjectIdSetDependencies(null).size());
 		
 		Set<ProjectElementId> projectIdSet = new HashSet<ProjectElementId>();
 		projectIdSet.add(ProjectElementId.PlanCycles);
 						
 		assertNotNull(ExcelPaceProject.getProjectIdSetDependencies(projectIdSet));
 		assertEquals(12, ExcelPaceProject.getProjectIdSetDependencies(projectIdSet).size());
 		
 		projectIdSet.clear();
 		projectIdSet.add(ProjectElementId.ViewSections);
 		
 		assertNotNull(ExcelPaceProject.getProjectIdSetDependencies(projectIdSet));
 		assertEquals(13, ExcelPaceProject.getProjectIdSetDependencies(projectIdSet).size());
 		
 		
 	}
 	
 }
