 /* 
  *   This file is part of the Open Database Audit Project (ODAP).
  *
  *   ODAP is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   Foobar is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
  *
  *   The code was developed by Rob Williams
  */
 
 package com.odap.common.hbase;
 
 
 //Used the gsql parser for testing. Will remove shortly.
 //import gudusoft.gsqlparser.EDbVendor;
 //import gudusoft.gsqlparser.TCustomSqlStatement;
 //import gudusoft.gsqlparser.TGSqlParser;
 
 import com.odap.common.thrift.*;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.client.Scan;
 import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
 import org.apache.hadoop.hbase.util.Bytes;
 import org.apache.log4j.Logger;
 
 
 import sun.misc.BASE64Decoder;
  
 
 
 public class EventHBaseHandler {
 	
 	static Logger logger = Logger.getLogger(EventHBaseHandler.class);
 
 	public static void formatRow(Event e,ImmutableBytesWritable row_immutable, Result res){
 		 
 		 byte[] row = row_immutable.get();
 		 e.setTimestamp(Integer.MAX_VALUE - byteArrayToInt(row,2));
 		 
 		 e.setSrc_ip(new String(res.getValue(Bytes.toBytes("core"), Bytes.toBytes("src"))).trim());
 		 
 		 if(res.getValue(Bytes.toBytes("core"), Bytes.toBytes("port")) != null){
 			 e.setSrc_port(res.getValue(Bytes.toBytes("core"), Bytes.toBytes("port"))[0]);
 		 }else{
 			 e.setSrc_port(Bytes.toBytes("0")[0]);
 		 }
 		 
 		 e.setStatement(new String(res.getValue(Bytes.toBytes("core"), Bytes.toBytes("statement"))).trim());
 		 e.setDbname(new String(res.getValue(Bytes.toBytes("core"), Bytes.toBytes("dbname"))).trim().toUpperCase());
 		 e.setUsername(new String(res.getValue(Bytes.toBytes("core"), Bytes.toBytes("username"))).trim().toUpperCase());
 		 e.setAppname(new String(res.getValue(Bytes.toBytes("core"), Bytes.toBytes("application"))).trim().toUpperCase());
		 
 		 if(e.getSrc_ip() == null){
 			 e.setSrc_ip("null");
 		 }
 		 
 		 if(e.getStatement() == null){
 			 e.setStatement("null");
 		 }
 		 
 		 if(e.getUsername() == null){
 			 e.setUsername("null");
 		 }
 		 
 		 if(e.getAppname() == null){
 			 e.setAppname("null");
 		 }
 
 	}
 	
 	public static Map<String,List<String>> parseStatement(Event event,String database_type){
 	/*    	TGSqlParser sqlparser = null;
 		
 		
 			switch(database_type){
 			case "MySQL":
 				sqlparser = new TGSqlParser(EDbVendor.dbvmysql);
 				break;
 			case "DB2 LUW":
 				sqlparser = new TGSqlParser(EDbVendor.dbvdb2);
 			case "":
 				System.err.println("Unsupported");
 			}
 		
 			sqlparser.sqltext = new String(event.getStatement());
 		
 		
 			Map<String,List<String>> result = new HashMap<>();
 			List<String> columns = new LinkedList<>();
 			List<String> tables = new LinkedList<>();
 		
 			int ret = sqlparser.parse();
 		
 		
 			if (ret == 0){
 		    for(int i=0;i<sqlparser.sqlstatements.size();i++){
 		    	TCustomSqlStatement stmt = sqlparser.sqlstatements.get(i);
 		    	
 		    	for(int x=0;x<   stmt.tables.size();x++){
 		    		tables.add(stmt.tables.getTable(x).getName().toUpperCase());
 		    		
 		    		
 		    		for(int z=0;z<   stmt.tables.getTable(x).getObjectNameReferences().size();z++){
 		        		columns.add(stmt.tables.getTable(x).getObjectNameReferences().getObjectName(z).getColumnNameOnly().toUpperCase());
 		        	} 
 		    	}
 		    	
 		    	           	
 		    }
 		    
 		    result.put("tables", tables);
 		    result.put("columns", columns);
 		}
 			return result;*/
 	return null;
 	}
 	
 	
 	 public static String byteArrayToHexString(byte[] b) {
 		    StringBuffer sb = new StringBuffer(b.length * 2);
 		    for (int i = 0; i < b.length; i++) {
 		      int v = b[i] & 0xff;
 		      if (v < 16) {
 		        sb.append('0');
 		      }
 		      sb.append(Integer.toHexString(v));
 		    }
 		    return sb.toString().toUpperCase();
 	 }
 	 
 	 public static int byteArrayToInt(byte[] b, int offset) {
 	     int value = 0;
 	     for (int i = 0; i < 4; i++) {
 	         int shift = (4 - 1 - i) * 8;
 	         value += (b[i + offset] & 0x000000FF) << shift;
 	     }
 	     return value;
 	 }
 	 
 	 public static int byteArrayToShort(byte[] b, int offset) {
 		 int c = (b[0] << 8) + b[1];
 		 return c;
 	 }
 	 
 	 public static Scan getStartStopFilter(short server_id,int start_time,int stop_time){
 			byte[] c = new byte[Bytes.toBytes(server_id).length +Bytes.toBytes(stop_time).length+3];
 			System.arraycopy(Bytes.toBytes(server_id), 0, c, 0, Bytes.toBytes(server_id).length);
 			System.arraycopy(Bytes.toBytes(Integer.MAX_VALUE-stop_time), 0, c, Bytes.toBytes(server_id).length, Bytes.toBytes(stop_time).length);
 			c[c.length-3] = 0x00;
 			c[c.length-2] = 0x00;
 			c[c.length-1] = 0x00;
 			
 			logger.info("Start scanning at:" + byteArrayToHexString(c) );
 			Scan scanner = new Scan();
 			scanner.setStartRow(c);
 			
 			byte[] d = new byte[Bytes.toBytes(server_id).length +Bytes.toBytes(start_time).length+3];
 			System.arraycopy(Bytes.toBytes(server_id), 0, d, 0, Bytes.toBytes(server_id).length);
 			System.arraycopy(Bytes.toBytes(Integer.MAX_VALUE-start_time), 0, d, Bytes.toBytes(server_id).length, Bytes.toBytes(start_time).length);
 			d[2] = (byte)0xFF;
 			d[3] = (byte)0xFF;
 			d[4] = (byte)0xFF;
 			d[5] = (byte)0xFF;
 			
 			d[6] = (byte)0xFF;
 			d[7] = (byte)0xFF;
 			d[8] = (byte)0xFF;
 			
 			logger.info("Stop scanning at:" + byteArrayToHexString(d) );
 			
 			scanner.setStopRow(d);	
 		  return scanner;
 	 }
 	 
 	 public static Scan getStopFilterByRowID(short server_id,String row_id){
 		logger.info("Entering getStopFilterByRowID(" +server_id +","+row_id + ")");
 		byte[] c = new byte[Bytes.toBytes(server_id).length +4+3];
 		System.arraycopy(Bytes.toBytes(server_id), 0, c, 0, Bytes.toBytes(server_id).length);
 		for(int i = 2; i < c.length; i++){
		 c[i] = 0x00;
 		}
 		 
 		Scan scanner = new Scan();
 		scanner.setStartRow(c);
 		
 		byte[] d =  new byte[Bytes.toBytes(server_id).length +4+3];
 		try {
 			byte[] decoded = new BASE64Decoder().decodeBuffer(row_id);
 			System.arraycopy(Bytes.toBytes(server_id), 0, d, 0, Bytes.toBytes(server_id).length);
 			System.arraycopy(decoded, 2, d, Bytes.toBytes(server_id).length, decoded.length-2);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		 
 		scanner.setStopRow(d);
 		logger.info("Exiting getStopFilterByRowID - " + byteArrayToHexString(c) + "," + byteArrayToHexString(d));
 		return scanner;
 			
 	 }
 }
