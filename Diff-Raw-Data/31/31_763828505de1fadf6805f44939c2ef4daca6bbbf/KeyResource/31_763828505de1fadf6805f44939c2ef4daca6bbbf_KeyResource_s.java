 /*
 Copyright 2011 Matthew Kennedy
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 
 */
 package com.stinkymatt.helena;
 
 import java.util.Map;
 
 import org.restlet.data.Form;
 import org.restlet.data.Reference;
 import org.restlet.resource.Get;
 
//TODO Verify that this handles "page 2" of a query

 public final class KeyResource extends AbstractCassandraResource
 {	
 	@Get
 	public Map<String, Map<String, String>> getColumns() 
 	{
 		Reference ref = getRequest().getResourceRef();
 		StorageAccess storage = parentApp.getStorage();
 		
 		int numRows = Integer.parseInt(
 				ref.getQueryAsForm().getFirstValue(
 					storage.getNumRowsVar(), String.valueOf(storage.defaultNumRows)
 			)
 		);
 		Form theQuery = ref.getQueryAsForm();
 		theQuery.removeAll(storage.getNumRowsVar(), true);
 		String columnQuery = Reference.decode(theQuery.getQueryString());
 		String colRange = ref.getMatrix();
 		if (colRange != null)
 		{
 			key = key.substring(0, key.indexOf(';'));
 		}
		if (columnQuery == null) return storage.getRows(keyspace, cf, key, colRange, numRows);
 		else 
 		{
 			return storage.getQueriedRows(keyspace, cf, key, numRows, colRange, columnQuery);
 		}
		
		
/*		
		StorageAccess storage = parentApp.getStorage();
		Reference ref = getRequest().getResourceRef();
		String colRange = ref.getMatrix();
		if (colRange != null)
		{
			key = key.substring(0, key.indexOf(';'));
		}
		int numRows = Integer.parseInt(
			ref.getQueryAsForm().getFirstValue(
				storage.getNumRowsVar(), String.valueOf(storage.defaultNumRows)
			)
		);
		ref.getQueryAsForm().removeAll(storage.getNumRowsVar());
		return storage.getRows(keyspace, cf, key, colRange, numRows);
*/
 	}
 	
 }
