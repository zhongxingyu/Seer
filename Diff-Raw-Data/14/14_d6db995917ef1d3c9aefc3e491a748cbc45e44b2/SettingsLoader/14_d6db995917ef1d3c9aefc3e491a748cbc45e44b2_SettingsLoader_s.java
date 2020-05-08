 /*******************************************************************************
  * Copyright (c) Sam Bell.
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published
  * by the Free Software Foundation, either version 3 of the License,
  * or  any later version.
  * 
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
  * the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  * Contributors:
  *     Sam Bell - initial API and implementation
  ******************************************************************************/
 /**
  * 
  */
 package com.mimpidev.podsalinan.data.loader;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.tmatesoft.sqljet.core.table.SqlJetDb;
 
 import com.mimpidev.dev.sql.SqlException;
 import com.mimpidev.podsalinan.Podsalinan;
 import com.mimpidev.podsalinan.data.ProgSettings;
 
 /**
  * @author bugman
  *
  */
 public class SettingsLoader extends TableLoader {
 
 	private ProgSettings settings;
	/**
	 * @param podsalinanDB 
	 * 
	 */
 	public SettingsLoader(ProgSettings newSettings, SqlJetDb dbConnection) {
		setdbTable(dbConnection);
 		tableName = "settings";
		settings=newSettings;

 		String[] columnNames = {"id","name","value"};
 		String[] columnTypes = {"INTEGER PRIMARY KEY AUTOINCREMENT",
 				                "TEXT","TEXT"};
 		createColumnList (columnNames, columnTypes);
 	}
 
 	@Override
 	public void readTable() {
 		ArrayList<Map<String,String>> recordSet = readFromTable();
 		
 		if ((recordSet!=null)&&(recordSet.size()>0))
 		for (Map<String,String> record: recordSet){
 			settings.getMap().put(record.get("name"),record.get("value"));
 		}
 	}
 
 	@Override
 	public void updateDatabase() {
 		if (dbTable.isDbOpen()){
 			purgeTable();
 			
 			
 			for (final Map.Entry<String, String> entry : settings.getMap().entrySet()){
 				try {
 					dbTable.insert(new HashMap<String, Object>(){{
 						put("name",entry.getKey());
 						put("value",entry.getValue());
 					}});
 				} catch (SqlException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		} else {
 			Podsalinan.debugLog.logError("Error db connection is closed");
 		}
 	}
 
 }
