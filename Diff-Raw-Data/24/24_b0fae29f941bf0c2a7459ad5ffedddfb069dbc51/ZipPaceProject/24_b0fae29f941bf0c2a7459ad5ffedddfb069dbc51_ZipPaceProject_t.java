 /*
  *	File: @(#)ZipPaceProject.java 	Package: com.pace.base.project.alt 	Project: Paf Base Libraries
  *	Created: Jul 22, 2009  		By: jmilliron
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
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.zip.ZipOutputStream;
 
 import org.apache.log4j.Logger;
 
 import com.pace.base.PafBaseConstants;
 import com.pace.base.PafException;
 import com.pace.base.utility.FileUtils;
 import com.pace.base.utility.PafZipUtil;
 
 /**
  * Represents a Pace archive file (.paf).  This class takes a paf file and then extracts it to a temp directory, 
  * creates an internal xmlPaceProject that points to the temp directory.  This class is mostly wrappers around
  * that xmlPaceProject instance.
  *
  * @author jmilliron
  * @version	x.xx
  *
  */
 public class ZipPaceProject extends PaceProject {
 	
 	private static final Logger logger = Logger.getLogger(ZipPaceProject.class);
 	
 	private boolean upgradeProject;
 	
 	private String outputZipFile;
 	
 	private XMLPaceProject xmlPaceProject;
 	
 	//private File paceTempDir = new File(PafBaseConstants.DN_PaceTmpFldr);
 		
 	/**
 	 * @param projectDataMap
 	 */
 	protected ZipPaceProject(Map<ProjectElementId, Object> projectDataMap) {
 		super(projectDataMap);
 	}
 	
 	/**
 	 * Creates a zip pace project and only loads in the project element id's passed in.
 	 * 
 	 * @param projectInput zip file location
 	 * @param filterSet	   set to filter on
 	 * @param upgradeProject auto upgrade the project.
 	 * @throws InvalidPaceProjectInputException 
 	 * @throws PafException
 	 */
 	public ZipPaceProject(String projectInput, Set<ProjectElementId> filterSet, boolean upgradeProject)
 			throws PaceProjectCreationException, InvalidPaceProjectInputException {
 		
 		super(projectInput, filterSet);
 
 		this.upgradeProject = upgradeProject;
 		
 		//test project input for
 		if ( projectInput == null ) {
 			
 			throw new InvalidPaceProjectInputException("project input can't be null.");
 						
 		}
 		
 		if ( ! projectInput.endsWith(".paf")) {
 			
 			throw new InvalidPaceProjectInputException("project input has to be a Pace Archive File.");
 			
 		}
 		
 		File projectInputFile = new File(projectInput);
 		
 		if ( ! projectInputFile.isFile()) {
 			
 			throw new InvalidPaceProjectInputException("Pace archive file: " + projectInput + " does not exist or is not a file.");
 			
 		}
 				
 		read(filterSet);
 					
 		if ( getProjectErrorMap().size() > 0 ) {
 			
 			throw new PaceProjectCreationException(getProjectErrorMap());
 			
 		}
 	}
 	
 	/**
 	 * Creates a zip pace project and only loads in the project element id's passed in.
 	 * 
 	 * @param projectInput zip file location
 	 * @param filterSet	   set to filter on
 	 * @throws InvalidPaceProjectInputException 
 	 * @throws PafException
 	 */
 	public ZipPaceProject(String projectInput, Set<ProjectElementId> filterSet)
 			throws PaceProjectCreationException, InvalidPaceProjectInputException {
 		
 		this(projectInput, filterSet, false);
 	}
 
 	
 
 	/* (non-Javadoc)
 	 * @see com.pace.base.project.PaceProject#read(java.util.Set)
 	 */
 	@Override
 	protected void read(Set<ProjectElementId> filterSet) {
 		
 		File tempDirectory = FileUtils.createTempDirectory();
 		
 		String dynamicDirName = tempDirectory.getAbsolutePath();
 		
 		try {
 			PafZipUtil.unzipFile(getProjectInput(), dynamicDirName);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		//create empty xml pace project
 		try {
 			xmlPaceProject = new XMLPaceProject(dynamicDirName, new HashSet<ProjectElementId>(), upgradeProject);
 		} catch (InvalidPaceProjectInputException e) {
 			// TODO: do something
 			e.printStackTrace();
 		} catch (PaceProjectCreationException e) {
 			// TODO: do something
 			e.printStackTrace();
 		}
 		
 		super.read(filterSet);
 		
 		FileUtils.deleteDirectory(tempDirectory);
 		//FileUtils.deleteDirectory(paceTempDir);
 		
 		xmlPaceProject = null;
 	}
 
 	/**
 	 * Creates a zip pace project
 	 * 
 	 * @param projectInput zip file location
 	 * @throws InvalidPaceProjectInputException 
 	 * @throws PafException
 	 */
 	public ZipPaceProject(String projectInput) throws InvalidPaceProjectInputException, PaceProjectCreationException  {
 		this(projectInput, null);
 	}
 			
 	
 	/* (non-Javadoc)
 	 * @see com.pace.base.project.PaceProject#write(java.util.Set)
 	 */
 	@Override
 	protected void write(Set<ProjectElementId> filterSet) throws ProjectSaveException {
 
 		File tempDirectory = FileUtils.createTempDirectory();
 		
 		//File tempDirectory = FileUtils.createTempDirectory(paceTempDir);
 		
 		String dynamicDirName = tempDirectory.getAbsolutePath();
 						
 		//if project input = same as output zip, then include all contents of input
 		//by 1st extracting the files, overwriting the updates then zipping again.
 		if ( (getProjectInput() != null && getOutputZipFile() != null && getProjectInput().equals(getOutputZipFile())) ||
 				(getOutputZipFile() != null && (new File(getOutputZipFile()).exists() ))) {
 					
 			try {
 				PafZipUtil.unzipFile(getOutputZipFile(), dynamicDirName);
 			} catch (IOException e) {
 				
 				logger.error(e.getMessage());
 				e.printStackTrace();				
 				throw new ProjectSaveException(e.getMessage());
 				
 			}
 		
 		}
 	
 		try {
 			xmlPaceProject = new XMLPaceProject(dynamicDirName, new HashSet<ProjectElementId>(), false);
 		} catch (InvalidPaceProjectInputException e3) {
 			
 			logger.error(e3.getMessage());
 			e3.printStackTrace();
 			throw new ProjectSaveException(e3.getMessage());
 			
 		} catch (PaceProjectCreationException e3) {
 			
 			logger.error(e3.getMessage());
 			e3.printStackTrace();			
 			throw new ProjectSaveException(e3.getMessage());
 		}
 		
 		super.write(filterSet);
 		
 		File zipFile = new File(getOutputZipFile());
 		
 		File zipFileDir = zipFile.getParentFile();
 		
 		if ( ! zipFileDir.isDirectory() ) {
 			
 			boolean dirsCreated = zipFileDir.mkdirs();
 									
 			if ( dirsCreated ) {
 				
 				logger.info("Directory " + zipFileDir.toString() + " was successfully created.");
 				
 			} else {
 				
 				logger.error("Directory " + zipFileDir.toString() + " could NOT be created.");
 				
 			}
 			
 		}
 		
 		ZipOutputStream zout = null;
 		try {
 			zout = new ZipOutputStream (new BufferedOutputStream (new FileOutputStream (zipFile.toString())));
 		} catch (FileNotFoundException e2) {
 			
 			logger.error(e2.getMessage());
 			e2.printStackTrace();			
 			throw new ProjectSaveException(e2.getMessage());
 		}
 		
 		File xmlConfDir = new File(dynamicDirName);
 		
 		try {
 			PafZipUtil.zipDirectory(xmlConfDir.getAbsolutePath(), zout);
 		} catch (IOException e1) {
 			
 			logger.error(e1.getMessage());
 			e1.printStackTrace();
 			throw new ProjectSaveException(e1.getMessage());
 			
 		}
 
 		if ( zout != null ) {
 			
 			try {
 				zout.close();
 			} catch (IOException e) {
 				logger.error(e.getMessage());
 				e.printStackTrace();
 				throw new ProjectSaveException(e.getMessage());
 			}
 			
 		}
 		
 		FileUtils.deleteDirectory(tempDirectory);
 		//FileUtils.deleteDirectory(paceTempDir);
 		
 		xmlPaceProject = null;
 		
 	}
 
 	@Override
 	protected void write(String writeFile, Set<ProjectElementId> filterSet) throws ProjectSaveException {
 						
 		
 		if ( writeFile != null ) {
 			
 			outputZipFile = writeFile;
 	
 		}
 						
 		write(filterSet);
 		
 		outputZipFile = null;
 		
 	}
 
 	/**
 	 * @return the outputZipFile
 	 */
 	public String getOutputZipFile() {
 				
 		if ( outputZipFile == null || outputZipFile.trim().length() == 0 ) {
 		
 			return getProjectInput();			
 			
 		}		
 		
 		return outputZipFile;
 	}
 
 	protected String getTempExtractDir() {
 		
 		if ( xmlPaceProject != null)  {
 			
 			return xmlPaceProject.getProjectInputDir();
 			
 		}
 		
 		return null;
 	}
 	
 	@Override
 	protected void readApplicationDefinitions() throws PaceProjectReadException {
 		
 		if ( xmlPaceProject != null) {
 			
 			xmlPaceProject.readApplicationDefinitions();
 			
 			setApplicationDefinitions(xmlPaceProject.getApplicationDefinitions());
 			
 		}
 		
 	}
 
 	@Override
 	protected void readCustomFunctions() throws PaceProjectReadException {
 		
 		if ( xmlPaceProject != null) {
 				
 			xmlPaceProject.readCustomFunctions();
 			
 			setCustomFunctions(xmlPaceProject.getCustomFunctions());
 			
 		}
 		
 	}
 
 	@Override
 	protected void readCustomMenus() throws PaceProjectReadException {
 
 		if ( xmlPaceProject != null) {
 			
 			xmlPaceProject.readCustomMenus();
 			
 			setCustomMenus(xmlPaceProject.getCustomMenus());
 			
 		}
 	}
 
 	@Override
 	protected void readDynamicMembers() throws PaceProjectReadException {
 
 		if ( xmlPaceProject != null) {
 		
 			xmlPaceProject.readDynamicMembers();
 			
 			setDynamicMembers(xmlPaceProject.getDynamicMembers());
 			
 		}
 		
 	}
 
 	@Override
 	protected void readGlobalStyles() throws PaceProjectReadException {
 
 		if ( xmlPaceProject != null) {
 				
 			xmlPaceProject.readGlobalStyles();
 			
 			setGlobalStyles(xmlPaceProject.getGlobalStyles());
 			
 		}
 		
 	}
 
 	@Override
 	protected void readHierarchyFormats() throws PaceProjectReadException {
 
 		if ( xmlPaceProject != null) {
 		
 			xmlPaceProject.readHierarchyFormats();
 			
 			setHierarchyFormats(xmlPaceProject.getHierarchyFormats());
 			
 		}
 		
 	}
 
 	@Override
 	protected void readMeasures() throws PaceProjectReadException {
 
 		if ( xmlPaceProject != null) {
 		
 			xmlPaceProject.readMeasures();
 		
 			setMeasures(xmlPaceProject.getMeasures());
 			
 		}
 		
 	}
 
 	@Override
 	protected void readMemberTags() throws PaceProjectReadException {
 
 		if ( xmlPaceProject != null) {
 			
 			xmlPaceProject.readMemberTags();
 			
 			setMemberTags(xmlPaceProject.getMemberTags());
 			
 		}
 	}
 
 	@Override
 	protected void readNumericFormats() throws PaceProjectReadException {
 		
 		if ( xmlPaceProject != null) {
 		
 			xmlPaceProject.readNumericFormats();
 			
 			setNumericFormats(xmlPaceProject.getNumericFormats());
 			
 		}
 		
 	}
 
 	@Override
 	protected void readPlanCycles() throws PaceProjectReadException {
 		
 		if ( xmlPaceProject != null) {
 				
 			xmlPaceProject.readPlanCycles();
 			
 			setPlanCycles(xmlPaceProject.getPlanCycles());
 			
 		}
 		
 	}
 
 	@Override
 	protected void readRoleConfigurations() throws PaceProjectReadException {
 		
 		if ( xmlPaceProject != null) {
 		
 			xmlPaceProject.readRoleConfigurations();
 		
 			setRoleConfigurations(xmlPaceProject.getRoleConfigurations());
 			
 		}
 		
 	}
 
 	@Override
 	protected void readRoles() throws PaceProjectReadException {
 		
 		if ( xmlPaceProject != null) {
 		
 			xmlPaceProject.readRoles();
 		
 			setRoles(xmlPaceProject.getRoles());
 			
 		}
 		
 	}
 
 	@Override
 	protected void readRoundingRules() throws PaceProjectReadException {
 	
 		if ( xmlPaceProject != null) {
 			
 			xmlPaceProject.readRoundingRules();
 			
 			setRoundingRules(xmlPaceProject.getRoundingRules());
 			
 		}
 		
 	}
 
 	@Override
 	protected void readRuleSets() throws PaceProjectReadException {
 		
 		if ( xmlPaceProject != null) {
 		
 			xmlPaceProject.readRuleSets();
 		
 			setRuleSets(xmlPaceProject.getRuleSets());
 		}
 		
 	}
 
 	@Override
 	protected void readUserSecurity() throws PaceProjectReadException {
 		
 		if ( xmlPaceProject != null) {
 			
 			xmlPaceProject.readUserSecurity();
 			
 			setUserSecurity(xmlPaceProject.getUserSecurity());
 			
 		}
 	}
 
 	@Override
 	protected void readUserSelections() throws PaceProjectReadException {
 	
 		if ( xmlPaceProject != null) {
 		
 			xmlPaceProject.readUserSelections();
 		
 			setUserSelections(xmlPaceProject.getUserSelections());
 			
 		}
 		
 	}
 
 	@Override
 	protected void readVersions() throws PaceProjectReadException {
 
 		if ( xmlPaceProject != null) {
 		
 			xmlPaceProject.readVersions();
 		
 			setVersions(xmlPaceProject.getVersions());
 			
 		}
 		
 	}
 
 	@Override
 	protected void readViewGroups() throws PaceProjectReadException {
 	
 		if ( xmlPaceProject != null) {
 		
 			xmlPaceProject.readViewGroups();
 		
 			setViewGroups(xmlPaceProject.getViewGroups());
 			
 		}
 		
 	}
 
 	@Override
 	protected void readViewSections() throws PaceProjectReadException {
 		
 		if ( xmlPaceProject != null) {
 		
 			xmlPaceProject.readViewSections();
 		
 			setViewSections(xmlPaceProject.getViewSections());
 			
 		}
 		
 	}
 
 	@Override
 	protected void readViews() throws PaceProjectReadException {
 		
 		if ( xmlPaceProject != null) {
 			
 			xmlPaceProject.readViews();
 			
 			setViews(xmlPaceProject.getViews());
 			
			//copy the migration print styles generated by migration step in getView()
			xmlPaceProject.readPrintStyles();
			
			this.setPrintStyles(xmlPaceProject.getPrintStyles());
 		}
 	}
 	
 	@Override
 	protected void readSeasons() throws PaceProjectReadException {
 	
 		if ( xmlPaceProject != null) {
 			
 			xmlPaceProject.readSeasons();
 			
 			setSeasons(xmlPaceProject.getSeasons());
 			
 		}
 		
 	}
 
 	@Override
 	protected void readPrintStyles() throws PaceProjectReadException {
 		// TODO Auto-generated method stub
 		
 		if ( xmlPaceProject != null) {
 			
 			xmlPaceProject.readPrintStyles();
 			
 			setPrintStyles(xmlPaceProject.getPrintStyles());
 			
 		}
 	}
 	
 	@Override
 	protected void readUserMemberLists() throws PaceProjectReadException {
 		if ( xmlPaceProject != null) {
 			
 			xmlPaceProject.readUserMemberLists();
 			
 			setUserMemberLists(xmlPaceProject.getUserMemberLists());
 			
 		}
 		
 	}
 
 
 	@Override
 	protected void writeApplicationDefinitions() {
 
 		if ( xmlPaceProject != null) {
 			
 			//set xml pace project model with model from zip pace project
 			xmlPaceProject.setApplicationDefinitions(getApplicationDefinitions());
 			
 			xmlPaceProject.writeApplicationDefinitions();
 			
 		}
 				
 	}
 
 	@Override
 	protected void writeCustomFunctions() {
 
 		if ( xmlPaceProject != null) {
 			
 			xmlPaceProject.setCustomFunctions(getCustomFunctions());
 			
 			xmlPaceProject.writeCustomFunctions();
 			
 		}
 	}
 
 	@Override
 	protected void writeCustomMenus() {
 
 		if ( xmlPaceProject != null) {
 		
 			xmlPaceProject.setCustomMenus(getCustomMenus());
 			
 			xmlPaceProject.writeCustomMenus();
 			
 		}
 		
 	}
 
 	@Override
 	protected void writeDynamicMembers() {
 		
 		if ( xmlPaceProject != null) {
 				
 			xmlPaceProject.setDynamicMembers(getDynamicMembers());
 	
 			xmlPaceProject.writeDynamicMembers();
 		
 		}
 	}
 
 	@Override
 	protected void writeGlobalStyles() {
 		
 		if ( xmlPaceProject != null) {
 		
 			xmlPaceProject.setGlobalStyles(getGlobalStyles());
 		
 			xmlPaceProject.writeGlobalStyles();
 			
 		}
 		
 	}
 
 	@Override
 	protected void writeHierarchyFormats() {
 	
 		if ( xmlPaceProject != null) {
 		
 			xmlPaceProject.setHierarchyFormats(getHierarchyFormats());
 		
 			xmlPaceProject.writeHierarchyFormats();
 			
 		}
 		
 	}
 
 	@Override
 	protected void writeMeasures() {
 		
 		if ( xmlPaceProject != null) {
 		
 			xmlPaceProject.setMeasures(getMeasures());
 		
 			xmlPaceProject.writeMeasures();
 			
 		}
 		
 	}
 
 	@Override
 	protected void writeMemberTags() {
 		
 		if ( xmlPaceProject != null) {
 		
 			xmlPaceProject.setMemberTags(getMemberTags());
 		
 			xmlPaceProject.writeMemberTags();
 			
 		}
 		
 	}
 
 	@Override
 	protected void writeNumericFormats() {
 		
 		if ( xmlPaceProject != null) {
 		
 			xmlPaceProject.setNumericFormats(getNumericFormats());
 		
 			xmlPaceProject.writeNumericFormats();
 			
 		}
 		
 	}
 
 	@Override
 	protected void writePlanCycles() {
 		
 		if ( xmlPaceProject != null) {
 			
 			xmlPaceProject.setPlanCycles(getPlanCycles());
 		
 			xmlPaceProject.writePlanCycles();
 			
 		}
 		
 	}
 
 	@Override
 	protected void writeRoleConfigurations() {
 		
 		if ( xmlPaceProject != null) {
 		
 			xmlPaceProject.setRoleConfigurations(getRoleConfigurations());
 
 			xmlPaceProject.writeRoleConfigurations();
 			
 		}
 		
 	}
 
 	@Override
 	protected void writeRoles() {
 		
 		if ( xmlPaceProject != null) {
 		
 			xmlPaceProject.setRoles(getRoles());
 		
 			xmlPaceProject.writeRoles();
 			
 		}
 		
 	}
 
 	@Override
 	protected void writeRoundingRules() {
 
 		if ( xmlPaceProject != null) {
 		
 			xmlPaceProject.setRoundingRules(getRoundingRules());
 		
 			xmlPaceProject.writeRoundingRules();
 			
 		}
 		
 	}
 
 	@Override
 	protected void writeRuleSets() {
 
 		if ( xmlPaceProject != null) {
 			
 			xmlPaceProject.setRuleSets(getRuleSets());
 		
 			xmlPaceProject.writeRuleSets();
 			
 		}
 		
 	}
 
 	@Override
 	protected void writeUserSecurity() {
 		
 		if ( xmlPaceProject != null) {
 			
 			xmlPaceProject.setUserSecurity(getUserSecurity());
 		
 			xmlPaceProject.writeUserSecurity();
 			
 		}
 		
 	}
 
 	@Override
 	protected void writeUserSelections() {
 
 		if ( xmlPaceProject != null) {
 			
 			xmlPaceProject.setUserSelections(getUserSelections());
 		
 			xmlPaceProject.writeUserSelections();
 			
 		}
 		
 	}
 
 	@Override
 	protected void writeVersions() {
 
 		if ( xmlPaceProject != null) {
 			
 			xmlPaceProject.setVersions(getVersions());
 		
 			xmlPaceProject.writeVersions();
 			
 		}
 		
 	}
 
 	@Override
 	protected void writeViewGroups() {
 
 		if ( xmlPaceProject != null) {
 		
 			xmlPaceProject.setViewGroups(getViewGroups());
 		
 			xmlPaceProject.writeViewGroups();
 			
 		}
 		
 	}
 
 	@Override
 	protected void writeViewSections() {
 
 		if ( xmlPaceProject != null) {
 		
 			xmlPaceProject.setViewSections(getViewSections());
 		
 			xmlPaceProject.writeViewSections();
 			
 		}
 		
 	}
 
 	@Override
 	protected void writeViews() {
 		
 		if ( xmlPaceProject != null) {
 		
 			xmlPaceProject.setViews(getViews());
 		
 			xmlPaceProject.writeViews();
 			
 		}
 		
 	}
 
 	@Override
 	protected void writeSeasons() {
 	
 		if ( xmlPaceProject != null) {
 		
 			xmlPaceProject.setSeasons(getSeasons());
 		
 			xmlPaceProject.writeSeasons();
 			
 		}
 		
 	}
 
 	@Override
 	protected void writePrintStyles() throws PaceProjectWriteException {
 		// TODO Auto-generated method stub
 		if ( xmlPaceProject != null) {
 			
 			xmlPaceProject.setPrintStyles(getPrintStyles());
 		
 			xmlPaceProject.writePrintStyles();
 			
 		}
 		
 	}
 
 
 	@Override
 	protected void writeUserMemberLists() throws PaceProjectWriteException {
 		if ( xmlPaceProject != null) {
 			
 			xmlPaceProject.setUserMemberLists(getUserMemberLists());
 	
 			xmlPaceProject.writeUserMemberLists();
 		}
 	}
 		
 	
 }
