 /**
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 
 package org.openmrs.module.kenyametadatatools.mflsync;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.poi.hssf.usermodel.HSSFCell;
 import org.apache.poi.hssf.usermodel.HSSFRow;
 import org.apache.poi.hssf.usermodel.HSSFSheet;
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.apache.poi.poifs.filesystem.POIFSFileSystem;
 import org.openmrs.Location;
 import org.openmrs.LocationAttribute;
 import org.openmrs.LocationAttributeType;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.kenyametadatatools.MetadataToolsUtils;
 import org.openmrs.module.kenyametadatatools.task.TaskEngine;
 import org.openmrs.util.OpenmrsUtil;
 
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
 import java.net.URL;
import java.util.Date;
 import java.util.Collections;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 /**
  * Task to synchronize locations from a remote spreadsheet
  */
 public class MflSyncFromRemoteSpreadsheetTask extends BaseMflSyncTask {
 
 	protected URL spreadsheetUrl = null;
 
 	/**
 	 * Creates task from spreadsheet URL
 	 * @param mflCodeAttrType the location attribute type for the MFL code
 	 * @param spreadsheetUrl the spreadsheet URL
 	 */
 	public MflSyncFromRemoteSpreadsheetTask(LocationAttributeType mflCodeAttrType, URL spreadsheetUrl) {
 		super(mflCodeAttrType);
 
 		this.spreadsheetUrl = spreadsheetUrl;
 	}
 
 	/**
 	 * @see BaseMflSyncTask#doImport()
 	 */
 	@Override
 	public void doImport() throws Exception {
 
 		File tmpZip = null;
 
 		try {
 			TaskEngine.log("Downloading MFL archive. This may take a few minutes ...");
 
 			tmpZip = File.createTempFile("mfl", ".zip");
 
 			OutputStream out = new BufferedOutputStream(new FileOutputStream(tmpZip));
 			OpenmrsUtil.copyFile(spreadsheetUrl.openStream(), out);
 
 			TaskEngine.log("Downloaded archive " + tmpZip.getName());
 			TaskEngine.log("Looking for XLS files in archive ...");
 
 			ZipFile zipFile = new ZipFile(tmpZip);
 			for (ZipEntry entry : Collections.list(zipFile.entries())) {
 				if (entry.getName().toLowerCase().endsWith(".xls")) {
 					TaskEngine.log("Importing '" + entry.getName() + "' ...");
 					importXls(zipFile.getInputStream(entry));
 				}
 			}
 		}
 		finally {
 			if (tmpZip != null) {
 				tmpZip.delete();
 			}
 		}
 	}
 
 	/**
 	 * Imports a MFL spreadsheet from a stream
 	 * @param stream the input stream
 	 * @throws IOException if an error occurred
 	 */
 	protected void importXls(InputStream stream) throws IOException {
 		POIFSFileSystem poifs = new POIFSFileSystem(stream);
 		HSSFWorkbook wbook = new HSSFWorkbook(poifs);
 		HSSFSheet sheet = wbook.getSheetAt(0);
 
 		for (int r = sheet.getFirstRowNum() + 1; r <= sheet.getLastRowNum(); ++r) {
 			HSSFRow row = sheet.getRow(r);
 			String code = String.valueOf(((Double) cellValue(row.getCell(0))).intValue());
 			String name = (String) cellValue(row.getCell(1));
 			String province = (String) cellValue(row.getCell(2));
 			String type = (String) cellValue(row.getCell(6));
 
 			if (StringUtils.isEmpty(name)) {
 				TaskEngine.logError("Unable to import location " + code + " with empty name");
 			}
 			else if (StringUtils.isEmpty(code)) {
 				TaskEngine.logError("Unable to import location '" + name + "' with invalid code");
 			}
 			else {
 				importLocation(code, name.trim(), province, type);
 			}
 		}
 	}
 
 	/**
 	 * Imports a location
 	 * @param code the MFL code
 	 * @param name the location name
 	 * @param province the province
 	 * @param type location type
 	 */
 	protected void importLocation(String code, String name, String province, String type) {
 		// Map MFL fields to location properties
 		String locationName = name;
 		String locationDescription = type;
 		String locationStateProvince = province;
 		String locationCountry = "Kenya";
 
 		// Look for existing location with this code
 		Location location = lookupMflCodeCache(code);
 
 		boolean doCreate = false, doUpdate = false;
 
 		// Create new location if it doesn't exist
 		if (location == null) {
 			location = new Location();
			location.setCreator(Context.getAuthenticatedUser());
			location.setDateCreated(new Date());
 
 			// Create MFL code attribute for new location
 			LocationAttribute mfcAttr = new LocationAttribute();
 			mfcAttr.setAttributeType(getMflCodeAttributeType());
 			mfcAttr.setValue(code);
 			mfcAttr.setOwner(location);
 			location.addAttribute(mfcAttr);
 
 			doCreate = true;
 		}
 		else {
 			// Un-retire location if necessary
 			if (location.getRetired()) {
 				location.setRetired(false);
 				location.setRetiredBy(null);
 				location.setRetireReason(null);
 				doUpdate = true;
 			}
 			else {
 				// Compute hashes of existing location fields and incoming fields
 				String incomingHash = MetadataToolsUtils.hash(locationName, locationDescription, locationStateProvince, locationCountry);
 				String existingHash = MetadataToolsUtils.hash(location.getName(), location.getDescription(), location.getStateProvince(), location.getCountry());
 
 				// Only update if hashes are different
 				if (!incomingHash.equals(existingHash)) {
 					doUpdate = true;
 				}
 			}
 		}
 
 		if (doCreate) {
 			TaskEngine.log("Creating new location '" + locationName + "' with code " + code);
 			createdCount++;
 		}
 		else if (doUpdate) {
 			TaskEngine.log("Updating existing location '" + locationName + "' with code " + code);
 			updatedCount++;
 		}
 
 		if (doCreate || doUpdate) {
 			location.setName(locationName);
 			location.setDescription(locationDescription);
 			location.setStateProvince(locationStateProvince);
 			location.setCountry(locationCountry);
 
 			Context.getLocationService().saveLocation(location);
 			updateMflCodeCache(code, location);
 		}
 
 		markLocationSynced(location);
 
 		Context.flushSession();
 		Context.clearSession();
 	}
 
 	/**
 	 * Extracts the value of the given cell
 	 * @param cell the cell
 	 * @return the cell value
 	 */
 	private Object cellValue(HSSFCell cell) {
 		return cell.getCellType() == 0 ? cell.getNumericCellValue() : cell.getStringCellValue();
 	}
 }
