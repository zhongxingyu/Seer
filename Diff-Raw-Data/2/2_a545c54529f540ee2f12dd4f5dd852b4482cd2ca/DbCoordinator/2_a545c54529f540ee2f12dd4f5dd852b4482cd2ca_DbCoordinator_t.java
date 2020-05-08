 /** ==== BEGIN LICENSE =====
    Copyright 2012 - BeeQueue.org
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an &quot;AS IS&quot; BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
  
  *  ===== END LICENSE ====== */
 package org.beequeue.coordinator;
 
 import java.sql.Clob;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import org.beequeue.sql.DalException;
 import org.beequeue.util.ToStringUtil;
 
 public class DbCoordinator extends Coordiantor {
 	public String driver;
 	public String url;
 	public String user;
 	public String password;
 	public String[] initSql;
 
 	public Connection connection() {
 		try {
 			Class.forName(driver);
 			Connection connection = DriverManager.getConnection(url,user,password);
 			if(initSql!=null){
 				for (String query : initSql) {
 					connection.createStatement().execute(query);
 				}
 			}
 			return connection;
 		} catch (Exception e) {
 			throw new DalException(e);
 		}
 	}
 /* <xmp>
         "aaData": [
             [ "Trident", "Internet Explorer 4.0", "Win 95+", 4, "X" ],
             [ "Trident", "Internet Explorer 5.0", "Win 95+", 5, "C" ],
             [ "Trident", "Internet Explorer 5.5", "Win 95+", 5.5, "A" ],
             [ "Trident", "Internet Explorer 6.0", "Win 98+", 6, "A" ],
             [ "Trident", "Internet Explorer 7.0", "Win XP SP2+", 7, "A" ],
             [ "Gecko", "Firefox 1.5", "Win 98+ / OSX.2+", 1.8, "A" ],
             [ "Gecko", "Firefox 2", "Win 98+ / OSX.2+", 1.8, "A" ],
             [ "Gecko", "Firefox 3", "Win 2k+ / OSX.3+", 1.9, "A" ],
             [ "Webkit", "Safari 1.2", "OSX.3", 125.5, "A" ],
             [ "Webkit", "Safari 1.3", "OSX.3", 312.8, "A" ],
             [ "Webkit", "Safari 2.0", "OSX.4+", 419.3, "A" ],
             [ "Webkit", "Safari 3.0", "OSX.4+", 522.1, "A" ]
         ],
         "aoColumns": [
             { "sTitle": "Engine" },
             { "sTitle": "Browser" },
             { "sTitle": "Platform" },
             { "sTitle": "Version", },
             {  "sTitle": "Grade", }
         ]
     } );    
 } );
 </xmp>
 */
 	@Override
 	public String selectAnyTable(String table) {
 		Connection connection = null;
 		try {
 			connection = connection();
 			String t;
 			if( table != null && !table.equals("") && !table.equals("/") ){
 				t = table.substring(1);
 			}else{
				t = "sys.systables";
 			}
 			String q = "select * from "+t;
 			ResultSet rs = connection.createStatement().executeQuery(q );
 			Map<String,Object> d = new LinkedHashMap<String, Object>();
 			ResultSetMetaData md = rs.getMetaData();
 			ArrayList<Object> header = new ArrayList<Object>();
 			d.put("query", q);
 			d.put("aoColumns", header);
 			for (int i = 0; i < md.getColumnCount(); i++) {
 				header.add(buildColumn(md.getColumnName(i+1)));
 			}
 			ArrayList<Object> data = new ArrayList<Object>();
 			d.put("aaData", data);
 			while(rs.next()){
 				ArrayList<Object> row = new ArrayList<Object>();
 				data.add(row);
 				for (int i = 0; i < md.getColumnCount(); i++) {
 					Object object = rs.getObject(i+1);
 					if(object instanceof Clob){
 					Clob c = (Clob) object;
 					object = c.getSubString(1, (int)c.length());
 					}
 					row.add(object);
 					
 				}
 			}
 			rs.close();
 			return ToStringUtil.toString(d);
 		} catch (SQLException e) {
 			throw new DalException(e);
 		}finally{
 			try{connection.close();}catch (Exception ignore) {}
 		}
 	}
 	
 	public LinkedHashMap<String, Object> buildColumn(String columnName) {
 		LinkedHashMap<String, Object> col = new LinkedHashMap<String, Object>();
 		col.put("sTitle", columnName);
 		return col;
 	}
 }
