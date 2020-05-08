 /*
  *	File: @(#)PafExcelElementItem.java 	Package: com.pace.base.project 	Project: Paf Base Libraries
  *	Created: Jan 6, 2010  		By: jmilliron
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
 package com.pace.base.project.excel.elements;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.apache.poi.ss.usermodel.Workbook;
 
 import com.pace.base.PafErrSeverity;
 import com.pace.base.PafException;
 import com.pace.base.project.*;
 import com.pace.base.project.excel.PafExcelInput;
 import com.pace.base.project.excel.PafExcelValueObject;
 
 
 /**
  * Class_description_goes_here
  *
  * @author jmilliron
  * @version	x.xx
  * @param <T>
  *
  */
 public abstract class PafExcelElementItem<T> {
 	
 	private Workbook workbook;
 	
 	private boolean isCellReferencingEnabled;
 	
 	private List<ProjectDataError> projectDataErrorList = null;
 	
 	protected static final Logger logger = Logger.getLogger(PafExcelElementItem.class);
 
 	protected T model;
 	
 	protected Map<String, List<String>> headerListMap;
 					
 	public PafExcelElementItem(Workbook workbook) {
 
 		this(workbook, false);
 		
 	}
 	
 	public PafExcelElementItem(Workbook workbook, boolean isCellReferencingEnabled) {
 
 		this.workbook = workbook;
 		this.isCellReferencingEnabled = isCellReferencingEnabled;
 		createHeaderListMapEntries();
 		
 	}
 	
 	
 	protected abstract T readExcelSheet() throws PaceProjectReadException, PafException;
 	protected abstract void writeExcelSheet(T t) throws PaceProjectWriteException, PafException;
 	protected abstract void createHeaderListMapEntries();
 	public abstract ProjectElementId getProjectElementId(); 
 	
 	/**
 	 * 
 	 *  Reads in using generics and handles exceptions.
 	 *
 	 * @return The generic object returned from readExcelSheet()
 	 * @throws PaceProjectReadException if a problem reading the excel sheet
 	 */
 	public T read() throws PaceProjectReadException {
 		
 		getProjectDataErrorList().clear();
 		
 		T t = null;
 		
 		try {
 			
 			t = readExcelSheet();
 			
 		} catch (PafException e) {
 			
 			//add error to project data error list
 			addProjectDataErrorToList(new ProjectDataError(getProjectElementId(), e.getMessage()));
 			
 			throw new PaceProjectReadException(e.getMessage());
 			
 		}
 	
 		//if errors exists, throw read exception
 		if ( getProjectDataErrorList().size() > 0 ) {
 			
 			throw new PaceProjectReadException(getSheetName());
 			
 		}
 		
 		return t;
 		
 	}
 	
 	/**
 	 * 
 	 *  Reads in using generics and handles exceptions.
 	 *
 	 * @return The generic object returned from readExcelSheet()
 	 * @throws PaceProjectReadException if a problem reading the excel sheet
 	 */
 	public void write(T t) throws PaceProjectWriteException {
 		
 		getProjectDataErrorList().clear();
 		
 		try {
 			
 			writeExcelSheet(t);
 			
 		} catch (PafException e) {
			
 			if ( e.getSeverity() != null && e.getSeverity().equals(PafErrSeverity.Fatal)) {
 						
 				throw new PaceProjectWriteException(e.getMessage(), true);
 				
 			} else {
 			
 				throw new PaceProjectWriteException(e.getMessage());
 				
 			}
 			
 		}
 			
 		
 	}
 	
 	public Map<String, List<String>> getHeaderListMap() {
 		
 		if ( headerListMap == null ) {
 			
 			 headerListMap = new HashMap<String, List<String>>();
 			
 		}
 		
 		return headerListMap;
 	}
 	
 	
 	/**
 	 * @return the workbook
 	 */
 	public Workbook getWorkbook() {
 		return workbook;
 	}
 
 	/**
 	 * 
 	 *  Gets the sheet name derived off of the project element id.  Returns null if getProjectElementId 
 	 *  returns null.
 	 *
 	 * @return excel sheet name
 	 */
 	public String getSheetName() {
 		
 		return (getProjectElementId() == null ? null : getProjectElementId().toString());
 		
 	}
 
 
 	/**
 	 * @return the projectDataErrorList
 	 */
 	public List<ProjectDataError> getProjectDataErrorList() {
 		
 		if ( projectDataErrorList == null ) {
 			
 			projectDataErrorList = new ArrayList<ProjectDataError>();
 			
 		}
 		
 		return projectDataErrorList;
 	}
 
 
 	/**
 	 * @param projectDataErrorList the projectDataErrorList to set
 	 */
 	protected void addProjectDataErrorToList(ProjectDataError projectDataError) {
 		
 		if ( logger.isDebugEnabled() ) {
 			
 			logger.debug(projectDataError);
 			
 		}
 		
 		getProjectDataErrorList().add(projectDataError);
 				
 	}
 
 	/**
 	 * @return the isCellReferencingEnabled
 	 */
 	public boolean isCellReferencingEnabled() {
 		return isCellReferencingEnabled;
 	}
 
 	/**
 	 * @param isCellReferencingEnabled the isCellReferencingEnabled to set
 	 */
 	public void setCellReferencingEnabled(boolean isCellReferencingEnabled) {
 		this.isCellReferencingEnabled = isCellReferencingEnabled;
 	}
 	
 	/**
 	 * 
 	 * Centerlizes the write pace excel input object.
 	 * 
 	 * @return
 	 */
 	protected PafExcelInput getWritePafExcelInput() {
 		
 		return new PafExcelInput.Builder(getWorkbook(), getSheetName(), getHeaderListMap().get(getSheetName()).size())
 			.endOfSheetIdnt(ExcelPaceProjectConstants.END_OF_SHEET_IDENT)
 			.build();
 		
 	}
 		
 }
