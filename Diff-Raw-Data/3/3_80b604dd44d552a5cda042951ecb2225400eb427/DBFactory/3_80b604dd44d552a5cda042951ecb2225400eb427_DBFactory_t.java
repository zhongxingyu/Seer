 /**
  * @contributor(s): Christian Skjetne (NTNU), Jacqueline Floch (SINTEF), Rune Sætre (NTNU)
  * @version: 		0.1
  * @date:			23 May 2011
  * @revised:
  *
  * Copyright (C) 2011 UbiCompForAll Consortium (SINTEF, NTNU)
  * for the UbiCompForAll project
  *
  * Licensed under the Apache License, Version 2.0.
  * You may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied.
  *
  * See the License for the specific language governing permissions
  * and limitations under the License.
  * 
  */
 
 /**
  * @description:
  *
  * 
  */
 
 package org.ubicompforall.CityExplorer.data;
 
 import android.content.Context;
 
 public class DBFactory{
 	/**
 	 * The Enum DBType.
 	 */
 	public enum DBType{
 		SQLITE;
 	}
 	
 	/** The DataBase connector instance. */
 	private static DatabaseInterface dbConnectorInstance;
 	
 	/** The database type. */
 	private static DBType databaseType = DBType.SQLITE; //change this to change the database type
 	
 	/**
 	 * Gets the single instance of DBFactory.
 	 *
 	 * @param context The context, that will be current from now to next getInstance
 	 * @return Single instance of DBFactory
 	 */
 	public static DatabaseInterface changeInstance( Context context, String new_DB_NAME ){
		if( dbConnectorInstance != null && dbConnectorInstance.isOpen() == true ){
			dbConnectorInstance.close();
		}
 		if(databaseType == DBType.SQLITE){
 			dbConnectorInstance = new SQLiteConnector( context, new_DB_NAME );
 		} // if right type
 		dbConnectorInstance.open();
 		dbConnectorInstance.setContext(context);
 		return dbConnectorInstance;
 	}//changeInstance
 
 	/**
 	 * Gets the single instance of DBFactory.
 	 *
 	 * @param context The context, that will be current from now to next getInstance
 	 * @return Single instance of DBFactory
 	 */
 	public static DatabaseInterface getInstance( Context context ){
 		if(dbConnectorInstance == null || dbConnectorInstance.isOpen() == false){
 			if(databaseType == DBType.SQLITE){
 				dbConnectorInstance = new SQLiteConnector(context);
 			} // if right type
 			dbConnectorInstance.open();
 		} // if DB not already open
 		dbConnectorInstance.setContext(context);
 		return dbConnectorInstance;
 	}//getInstance
 }//class DBFactory
