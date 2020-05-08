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
 package org.openmrs.contrib.databaseexporter.transform;
 
 import org.openmrs.contrib.databaseexporter.ExportContext;
 import org.openmrs.contrib.databaseexporter.TableRow;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 /**
  * This transform does everything that the standard LocationTransform does,
  * as well as replacing any global property values that refer to locations as necessary
  */
 public class RwandaLocationTransform extends LocationTransform {
 
 	//***** INTERNAL VARIABLES *****
 
 	private Map<String, TableRow> rowsToReplace = new HashMap<String, TableRow>();
 
 	//***** CONSTRUCTORS *****
 
 	public RwandaLocationTransform() {}
 
 	//***** INSTANCE METHODS *****
 
 	@Override
 	public boolean canTransform(String tableName, ExportContext context) {
 		boolean ret = super.canTransform(tableName, context);
 		ret = ret || tableName.equals("global_property");
 		return ret;
 	}
 
 	@Override
 	public boolean transformRow(TableRow row, ExportContext context) {
 		boolean ret = super.transformRow(row, context);
 		if (ret) {
 			if (row.getTableName().equals("global_property")) {
 				String propertyName = row.getRawValue("property").toString();
 				if (propertyName.equals("reports.currentlocation") ||
 					propertyName.equals("registration.rwandaLocationCodes") ||
 					propertyName.equals("dataqualitytools.sitesToList") ||
 					propertyName.equals("dataqualitytools.sitesToTally")) {
 					rowsToReplace.put(propertyName, row);
 					return false;
 				}
 			}
 		}
 		return ret;
 	}
 
 	@Override
 	public List<TableRow> postProcess(String tableName, ExportContext context) {
 		List<TableRow> ret = new ArrayList<TableRow>();
 		if (tableName.equals("global_property")) {
 			{
 				TableRow tr = rowsToReplace.get("reports.currentlocation");
 				tr.setRawValue("property_value", (getUsedNames().isEmpty() ? "" : getUsedNames().iterator().next()));
 				ret.add(tr);
 			}
 
 			StringBuilder rwLocCodes = new StringBuilder();
 			StringBuilder sitesToList = new StringBuilder();
 			StringBuilder sitesToTally = new StringBuilder();
 
			int num=0;
 			for (Iterator<String> i = getUsedNames().iterator(); i.hasNext();) {
 				num++;
 				String name = i.next();
 				rwLocCodes.append(name).append(":").append(num).append(i.hasNext() ? "|" : "");
 				sitesToList.append(name).append(":").append(name).append(i.hasNext() ? "|" : "");
 				sitesToTally.append(name).append(i.hasNext() ? "|" : "");
 			}
 
 			{
 				TableRow tr = rowsToReplace.get("registration.rwandaLocationCodes");
 				tr.setRawValue("property_value", rwLocCodes.toString());
 				ret.add(tr);
 			}
 			{
 				TableRow tr = rowsToReplace.get("dataqualitytools.sitesToList");
 				tr.setRawValue("property_value", sitesToList.toString());
 				ret.add(tr);
 			}
 			{
 				TableRow tr = rowsToReplace.get("dataqualitytools.sitesToTally");
 				tr.setRawValue("property_value", sitesToTally.toString());
 				ret.add(tr);
 			}
 		}
 		return ret;
 	}
 }
