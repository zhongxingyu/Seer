 /**
  * Copyright 2010 Tim Azzopardi
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.sf.log4jdbc;
 
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class DefaultResultSetCollector implements ResultSetCollector {
 
   private boolean fillInUnreadValues = false;
   
   public DefaultResultSetCollector(boolean fillInUnreadValues) {
     this.fillInUnreadValues = fillInUnreadValues; 
   }
   
   private ResultSetMetaData metaData = null;
   private List<Object> row = null;
   private List<List<Object>> rows = null;
   private Map<String, Integer> colNameToColIndex;
   private int colIndex = -1; // Useful for wasNull calls
 	private static final List<String> GETTERS = Arrays.asList(new String[] {
 			"getString", "getLong", "getInt", "getDate", "getTimestamp",
 			"getTime", "getBigDecimal", "getFloat","getDouble", "getByte","getShort", "getObject", });
 
 
 
 	public List<List<Object>> getRows() {
 		return rows;
 	}
 
 	public int getColumnCount() {
 		try {
 			return metaData.getColumnCount();
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void reset() {
 		rows = null;
 		row = null;
 		metaData = null;
 		colNameToColIndex = null;
 		colIndex = -1;
 	}
 
 	private void getMetaDataIfNeeded(Object object) {
 		if (metaData == null) {
 			ResultSet rs = (ResultSet) object;
 			
 			try {
 				metaData = rs.getMetaData();
 			} catch (SQLException e) {
 				throw new RuntimeException(e);
 			}
			// TODO tidy this up!
			setupColNameToColIndexMap();
 		}
 	}
 
  private void setupColNameToColIndexMap() {
    int columnCount = getColumnCount();
    colNameToColIndex = new HashMap<String, Integer>(columnCount);
    for (int column = 1; column <= columnCount; column++) {
    	colNameToColIndex.put(getColumnName(column).toLowerCase(), column);
    }
  }

 
 
     
 	public String getColumnName(int column) {
 		try {
 			return metaData.getColumnName(column);
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 
 	
 
 	/* (non-Javadoc)
    * @see net.sf.log4jdbc.IResultSetCollector#methodReturned(net.sf.log4jdbc.ResultSetSpy, java.lang.String, java.lang.Object, java.lang.Object, java.lang.Object)
    */
 	public boolean methodReturned(ResultSetSpy resultSetSpy, String methodCall, Object returnValue, Object targetObject, Object... methodParams) {
 		if (methodCall.startsWith("get") && methodParams != null && methodParams.length == 1) {
 			String methodName = methodCall.substring(0,methodCall.indexOf('('));
 			if (GETTERS.contains(methodName)) {
 				setColIndexFromGetXXXMethodParams(methodParams);
 				makeRowIfNeeded();
 				row.set(colIndex-1,returnValue);
 			}
 		}
 		if (methodCall.equals("wasNull()")) {
 			if (returnValue.equals("true")) {
 				row.set(colIndex-1,"[null]");				
 			}
 		}
 		if ("next()".equals(methodCall)) {
 			getMetaDataIfNeeded(resultSetSpy.getRealResultSet());
 			boolean isEndOfResultSet = "false".equals(returnValue);
 			if (row != null) {
 				if (rows==null) rows = new ArrayList<List<Object>>();
 				rows.add(row);
 				row = null;
 			}
 			if (isEndOfResultSet) {
 				return true;
 			}
 		}
     if ("getMetaData()".equals(methodCall)) {
       // If the client code calls getMetaData then we don't have to 
       metaData = (ResultSetMetaData) returnValue;
      // TODO tidy this up!
      setupColNameToColIndexMap();
     }
 		return false;
 	}
 
 	private void makeRowIfNeeded() {
 		if (row==null) {
 			row = new ArrayList<Object>(getColumnCount());
 			for (int i = 0; i < getColumnCount(); ++i) {
 				row.add("[unread]");
 			}
 		}
 	}
 
 	private void setColIndexFromGetXXXMethodParams(Object... methodParams) {
 		Object param1 = methodParams[0];
 		if (param1 == null) {
 			throw new RuntimeException("ResultSet.getXXX() first param null? ");										
 		}
 		if (param1 instanceof Integer) {
 			colIndex = (Integer) param1;
 		} else if (param1 instanceof String) {
 			if (colNameToColIndex == null) {
 				throw new RuntimeException("ResultSet.getXXX(colName): colNameToColIndex null");						
 			}
 			Integer idx = colNameToColIndex.get(((String) param1).toLowerCase());
 			
 			if (idx == null) {
 				throw new RuntimeException("ResultSet.getXXX(colName): could not look up name");
 			}
 			colIndex = idx;
 		} else {
 			throw new RuntimeException("ResultSet.getXXX called with: " +param1.getClass().getName());
 		}
 	}
 
 	@Override
 	public void preMethod(ResultSetSpy resultSetSpy, String methodCall, Object... methodParams) {
 	  if (methodCall.equals("next()") && fillInUnreadValues) {
 	    if (getRows() != null) {
 	      for (List<Object> printRow : getRows()) {
 	        int colIndex = 0;
 	        for (Object v : printRow) {
 	          // TODO
 	          if (v != null && v.toString().equals("[unread]")) {
 	            // Fill in any unread data using Spring's  org.springframework.jdbc.support.JDBCUtils
 	            // JDBCUtils.getResultSetValue(resultSetSpy.getRealResultSet(),colIndex)
 	            // resultSetSpy.getRealResultSet();
 	          }
 	          colIndex++;
 	        }
 	      }
 	    }
 	    
 	  }
  	  
 	}
 
 }
