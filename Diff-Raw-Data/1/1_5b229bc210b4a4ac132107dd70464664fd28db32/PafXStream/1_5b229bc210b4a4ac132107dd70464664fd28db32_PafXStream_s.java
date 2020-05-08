 /*
  *	File: @(#)PafXStream.java 	Package: com.pace.base.utility 	Project: Paf Base Libraries
  *	Created: Feb 13, 2005  		By: JWatkins
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
 package com.pace.base.utility;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.nio.charset.Charset;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import com.pace.base.PafBaseConstants;
 import com.pace.base.PafConfigFileNotFoundException;
 import com.pace.base.PafErrSeverity;
 import com.pace.base.app.*;
 import com.pace.base.comm.CustomMenuDef;
 import com.pace.base.comm.PafPlannerConfig;
 import com.pace.base.comm.PafViewTreeItem;
 import com.pace.base.db.membertags.MemberTagCommentEntry;
 import com.pace.base.db.membertags.MemberTagDef;
 import com.pace.base.funcs.CustomFunctionDef;
 import com.pace.base.rules.*;
 import com.pace.base.ui.PafProject;
 import com.pace.base.ui.PafServer;
 import com.pace.base.view.*;
 import com.thoughtworks.xstream.XStream;
 
 /**
  * 
  * PafXStream is a wrapper class for XStream.
  *
  * @author JWatkins
  * @version	x.xx
  *
  */
 public class PafXStream {
 	
 	private static final String UTF_8 = "UTF-8";
 
 	//instance of Logger
 	private static Logger logger = Logger.getLogger(PafXStream.class);
 
 	//instance of XStream
 	private static XStream xs = getXStream();
 	
 	//a map that is keyed off of root node name and has a value of matching namespace
 	private static Map<String, String> namespaceHeaderMap;
 
 	private static final String ARRAY = "-array";
 	
 	public static final String PAF_APPLICATION_DEF = "Application";
 	
 	public static final String PAF_APPLICATION_DEF_ARRAY = PAF_APPLICATION_DEF + ARRAY;
 
 	public static final String CUSTOM_MENU_DEF = "CustomMenuDef";
 	
 	public static final String CUSTOM_MENU_DEF_ARRAY = CUSTOM_MENU_DEF + ARRAY;
 	
 	public static final String CUSTOM_FUNCTION_DEF = "CustomFunctionDef";
 	
 	public static final String CUSTOM_FUNCTION_DEF_ARRAY = CUSTOM_FUNCTION_DEF + ARRAY;
 	
 	public static final String VERSION_DEF = "VersionDef";
 
 	public static final String VERSION_DEF_ARRAY = VERSION_DEF + ARRAY;
 	
 	public static final String RULE_SET = "RuleSet";
 
 	public static final String ROUNDING_RULE = "RoundingRule";
 	
 	public static final String ROUNDING_RULE_ARRAY = ROUNDING_RULE + ARRAY;
 
 	public static final String MEASURE_DEF = "MeasureDef";
 	
 	public static final String MEASURE_DEF_ARRAY = MEASURE_DEF + ARRAY;
 	
 	public static final String VIEW_SECTION_DEF = "ViewSection";
 	
 	// protected constructor to control access
 	protected PafXStream() {
 	}
 
 	/**
 	 * Returns singleton XStream object.  If XStream is null, initilizes it and creates it.
 	 * 
 	 * @return XStream object for custom serialization scenarios
 	 */
 	public static XStream getXStream() {
 		
 		if (xs == null) {			
 			initXStream();			
 		}
 		
 		return xs;
 	}
 
 	/**
 	 * Initializes the xstream class to standard paf aliases probably will expand to use either annotations or
 	 * load from property / constants file
 	 */
 	private static void initXStream() {
 		//xs = new XStream(new DomDriver("UTF-16"));
 		xs = new XStream();
 		
 		//writes out all xml
 		xs.setMode(XStream.NO_REFERENCES);
 		
 		xs.alias("PafView", PafView.class);
 		xs.alias("UserSelection", PafUserSelection.class);
 		xs.alias(VIEW_SECTION_DEF, PafViewSection.class);
 		xs.alias("ViewTuple", ViewTuple.class);
 		xs.alias("PageTuple", PageTuple.class);
 		xs.alias("ViewHeader", PafViewHeader.class);
 		xs.alias("NumberFormat", PafNumberFormat.class);
 		xs.alias("NumericMemberFormat", NumericMemberFormat.class);
 		xs.alias("PafBorder", PafBorder.class);
 
 		xs.alias(RULE_SET, RuleSet.class);
 		xs.alias("RuleGroup", RuleGroup.class);
 		xs.alias("Rule", Rule.class);
 
 		xs.alias("SeasonList", SeasonList.class);
 		xs.alias("Season", Season.class);
 
 		xs.alias("PlannerRole", PafPlannerRole.class);
 		xs.alias("PafUser", PafUserSecurity.class);
 
 		xs.alias(PAF_APPLICATION_DEF, PafApplicationDef.class);
 		xs.alias("WorkSpec", PafWorkSpec.class);
 		xs.alias("DimSpec", PafDimSpec.class);
 		xs.alias("PlanCycle", PlanCycle.class);
 
 		//xs.alias("ViewSectionUI", PafViewSectionUI.class);
 
 		xs.alias(MEASURE_DEF, MeasureDef.class);
 		xs.alias(VERSION_DEF, VersionDef.class);
 		xs.alias("VersionFormula", VersionFormula.class);
 
 		xs.alias("PafViewTreeItem", PafViewTreeItem.class);
 		xs.alias("PafPlannerConfig", PafPlannerConfig.class);
 
 		// notice this is hierarchy format now
 		xs.alias("GenerationFormat", HierarchyFormat.class);
 		xs.alias("HierarchyFormat", HierarchyFormat.class);
 
 		xs.alias("Dimension", Dimension.class);
 		xs.alias("LevelFormat", LevelFormat.class);
 		xs.alias("GenFormat", GenFormat.class);
 
 		xs.alias(CUSTOM_FUNCTION_DEF, CustomFunctionDef.class);
 		xs.alias("CustomActionDef", CustomActionDef.class);
 		xs.alias(CUSTOM_MENU_DEF, CustomMenuDef.class);
 
 		xs.alias("PafServer", PafServer.class);
 		xs.alias("PafProject", PafProject.class);
 
 		xs.alias(ROUNDING_RULE, RoundingRule.class);
 		xs.alias("MemberSet", MemberSet.class);
 
 		xs.alias("PafViewGroup", PafViewGroup.class);
 		xs.alias("PafViewGroupItem", PafViewGroupItem.class);
 		xs.alias("AliasMapping", AliasMapping.class);
 		
 		xs.alias("DynamicMemberDef", DynamicMemberDef.class);
 		xs.alias("MemberTagDef", MemberTagDef.class);
 		xs.alias("MemberTagCommentEntry", MemberTagCommentEntry.class);
 
 		namespaceHeaderMap = new HashMap<String, String>();
 		namespaceHeaderMap.put(PAF_APPLICATION_DEF_ARRAY, PAF_APPLICATION_DEF_ARRAY + " " + PafBaseConstants.HTTP_WWW_THEPALLADIUMGROUP_COM_PAF_APPS);
 		namespaceHeaderMap.put(CUSTOM_MENU_DEF_ARRAY, CUSTOM_MENU_DEF_ARRAY + " " + PafBaseConstants.HTTP_WWW_THEPALLADIUMGROUP_COM_PAF_CUSTOM_MENUS);
 		namespaceHeaderMap.put(CUSTOM_FUNCTION_DEF_ARRAY, CUSTOM_FUNCTION_DEF_ARRAY + " " + PafBaseConstants.HTTP_WWW_THEPALLADIUMGROUP_COM_PAF_FUNCTIONS);
 		namespaceHeaderMap.put(MEASURE_DEF_ARRAY, MEASURE_DEF_ARRAY + " " + PafBaseConstants.HTTP_WWW_THEPALLADIUMGROUP_COM_PAF_MEASURES);
 		namespaceHeaderMap.put(ROUNDING_RULE_ARRAY, ROUNDING_RULE_ARRAY + " " + PafBaseConstants.HTTP_WWW_THEPALLADIUMGROUP_COM_PAF_ROUNDING_RULES);
 		namespaceHeaderMap.put(RULE_SET, RULE_SET + " " + PafBaseConstants.HTTP_WWW_THEPALLADIUMGROUP_COM_PAF_RULE_SET);
 		namespaceHeaderMap.put(VERSION_DEF_ARRAY, VERSION_DEF_ARRAY + " " + PafBaseConstants.HTTP_WWW_THEPALLADIUMGROUP_COM_PAF_VERSIONS);
 
 	}
 
 	public static Object importObjectFromXml(String fullFilePath, boolean validateFileContents) throws PafConfigFileNotFoundException {
 	
 		logger.debug("Importing object from xml, " + fullFilePath);
 		
 		File f = new File(fullFilePath);
 		
 		Object o = null;
 		
 		//if file doesn't exist, throw not found exception
 		if (!f.exists()) {
 			throw new PafConfigFileNotFoundException("File " + f.getName()
 					+ " does not exist.", PafErrSeverity.Info);
 		}
 		
 		InputStream fis = null;
 		
 		InputStreamReader inputStreamReader = null;
 		
 		try {
 			
 			// Create input stream from file
 			fis = new FileInputStream(f);
 			
 			Charset charSet = Charset.forName(UTF_8);
 			
 			// Create reader using UTF-8
 			inputStreamReader = new InputStreamReader(fis, charSet);
 			
 			// Get the object from XML
 			o = getXStream().fromXML(inputStreamReader);
 			
 			logger.debug("Succesfully import: " + o.getClass().getSimpleName());
 		}
 
 		catch (RuntimeException re) {
 
 			logger.error(re.getMessage());
 
 			if ( validateFileContents ) {
 				
 				throw re;
 				
 			}
 			
 		} catch (FileNotFoundException e) {
 			
 			//should have already thrown a PafConfigFileNotFoundException
 		} 
 		
 		finally {
 			
 			if ( inputStreamReader != null ) {
 			
 				try {
 					inputStreamReader.close();
 				} catch (IOException e) {
 					//do nothing
 				}
 				
 			}
 			
 			if ( fis != null ) {
 				try {
 					fis.close();
 				} catch (IOException e) {
 					//do nothing
 				}
 			}
 			
 		}
 
 		//return imported object
 		return o;
 		
 	}
 	
 	/**
 	 * 
 	 *	Imports an object given the full file path.
 	 *
 	 * @param fullFilePath path + filename
 	 * @return imported object
 	 * @throws PafConfigFileNotFoundException
 	 */
 	public static Object importObjectFromXml(String fullFilePath)
 			throws PafConfigFileNotFoundException {
 		
 		
 		return importObjectFromXml(fullFilePath, false);
 		
 	}
 
 	/** 
 	 *	Imports an object given the file path and file name.
 	 *
 	 * @param filePath file path
 	 * @param fileName file name
 	 * @return imported object
 	 * @throws PafConfigFileNotFoundException
 	 */
 	public static Object importObjectFromXml(String filePath, String fileName)
 			throws PafConfigFileNotFoundException {
 		return importObjectFromXml(filePath + fileName);
 	}
 
 	/** 
 	 *	Exports an object to the provided full file path + name.
 	 *
 	 * @param object	object to export
 	 * @param fullFileName	path + file name
 	 */
 	public static void exportObjectToXml(Object object, String fullFileName) {
 		
 		PafXStream.setMode(XStream.NO_REFERENCES);
 		
 		logger.debug("Exporting object to xml. "
 				+ object.getClass().getSimpleName() + " to " + fullFileName);
 		
 		//serialize object to a string xml format
 		String s = xs.toXML(object);
 
 		s = addXSDHeader(s);
 		
 		//ref to file
 		File f = new File(fullFileName);
 
 		FileOutputStream fif = null;
 		OutputStreamWriter osw = null;
 		
 		try {
 			
 			//create new file 
 			f.createNewFile();
 			
 			//create file output stream
 			fif = new FileOutputStream(f);
 			
 			Charset charSet = Charset.forName(UTF_8);
 			
 			// Create a writer using UTF-8
 			osw = new OutputStreamWriter(fif, charSet);
 			
 			//write out bytes from string
 			osw.write(s);
 					
 			logger.debug("Succesfully exported "
 					+ object.getClass().getSimpleName());
 			
 		} catch (Exception ex) {
 			
 			//log warning
 			logger.warn(ex.getMessage());
 			
 		} finally {
 			
 			if ( osw != null ) {				
 				try {
 					osw.close();
 				} catch (IOException e) {
 					//do nothing
 				}				
 			}
 			
 			if ( fif != null ) {
 				try {
 					fif.close();
 				} catch (IOException e) {
 					//do nothing
 				}
 			}
 			
 		}
 		
 	}
 
 	/**
 	 * Addes a namespace to the string if the string starts with
 	 * one of the keys in the namespace map.
 	 * 
 	 * @param s string to attempt to add namespace to
 	 * @return s w/ added namespace if beginning of s matches key in map; returns 
 	 * 		   null if s is null. 
 	 */
 	public static String addXSDHeader(String s) {
 		
 		if ( s != null ) {
 			
 			for ( String key : namespaceHeaderMap.keySet() ) {
 				
 				String headerIdnt = PafBaseConstants.XML_OPEN_TAG + key; 
 				
 				if ( s.startsWith( headerIdnt )) {
 					
 					String xsdNamespace = namespaceHeaderMap.get(key);
 					
 					if ( ! s.contains(xsdNamespace)) {
 						s = s.replaceFirst(key, xsdNamespace);
 					}
 					
 					break;
 					
 				}
 				
 			}
 			
 		}
 		
 		
 		return s;
 	}
 
 	/** 
 	 *	Exports an object to the provided full file path + name.
 	 *
 	 * @param object	object to export
 	 * @param filePath	file path
 	 * @param fileName 	file name 
 	 */
 	public static void exportObjectToXml(Object object, String filePath,
 			String fileName) {
 		exportObjectToXml(object, filePath + fileName);
 	}
 
 	/** 
 	 *	Set's the internal XStream mod
 	 *
 	 * @param mode
 	 */
 	public static void setMode(int mode) {
 		
 		getXStream().setMode(mode);
 		
 	}
 
 	/** 
 	 *	Imports an object given the input stream
 	 *
 	 * @param is 	input stream
 	 * @return imported object
 	 */
 	public static Object importObjectFromXml(InputStream is) throws Exception {
 
 		logger.debug("Importing object from input stream");
 
 		StringBuffer sb = new StringBuffer();
 
 		Object o = null;
 
 		BufferedReader br = null;
 		
 		try {
 
 			Charset charSet = Charset.forName(UTF_8);
 			br = new BufferedReader(new InputStreamReader(is, charSet));
 			String thisLine;
 			while ((thisLine = br.readLine()) != null) {
 				sb.append(thisLine);
 			}
 
 			XStream xs = getXStream();
 			o = xs.fromXML(sb.toString());
 
 		}
 
 		catch (Exception ex) {
 
 			logger.error(ex.getMessage());
 
 		} finally {
 
 			if (br != null) {
 				try {
 					br.close();
 				} catch (IOException e) {
 					// do nothing
 				}
 			}
 		}
 
 		return o;
 	}
 }
