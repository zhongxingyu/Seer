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
 package com.odap.server.php;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.client.ResultScanner;
 import org.apache.hadoop.hbase.client.Scan;
 import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
 import org.apache.hadoop.hbase.util.Bytes; 
 import org.apache.log4j.Logger;
 import org.apache.thrift.TException;
 
 import sun.misc.BASE64Encoder;
 import com.odap.common.thrift.DisplayEvents;
 import com.odap.common.thrift.DisplayEvent;
 import com.odap.common.thrift.Event;
 import com.odap.common.hbase.*;
 
 public class PHPAppHBaseHandler implements DisplayEvents.Iface{
 	static Logger logger = Logger.getLogger(PHPAppHBaseHandler.class);
 	
 
 	public List<DisplayEvent> getEventsByRowID(short server_id, String row_id, String databaseType)	throws TException {
 		logger.info("Entering getEventsByRowID(" + server_id + "," + row_id + "," + databaseType +")");
 		//There are two cases we are dealing with here 
 		//
 		//1. User goes to statement pages and wishes to see most recent x statements in the system
 		//2. User turns on streaming. We recieve a row_id then return the rows from the start and stop 
 		//   and the row id provided. If over 50 statements of gone through since last refresh we don't
 		//   send everything. It's confusing as we always start scanning from the most recent and stop 
 		//   when we hit row id. On the security side we always replace the high 16 bits and then 
 		//   ensure the row id is the proper size
 		// 
 		
 		if(row_id == null){
 			row_id = new String();
 		}
 		
 		boolean row_id_m1 = (row_id.equals("-1"));
 		
 		if(row_id.length() != 12){
 			byte[] default_row_id = new byte[9];
 			System.arraycopy(Bytes.toBytes(server_id), 0, default_row_id, 0, Bytes.toBytes(server_id).length);
 			default_row_id[2] = (byte)0xFF;
 			default_row_id[3] = (byte)0xFF;
 			default_row_id[4] = (byte)0xFF;
 			default_row_id[5] = (byte)0xFF;
 			
 			default_row_id[6] = (byte)0xFF;
 			default_row_id[7] = (byte)0xFF;
 			default_row_id[8] = (byte)0xFF;
 			
 			row_id = new BASE64Encoder().encode(default_row_id);
 		}
 		
 		Scan scan1 = EventHBaseHandler.getStopFilterByRowID(server_id,row_id);
  
 		List<DisplayEvent> eventList = new LinkedList<DisplayEvent>();
 		 
 		 try{
 			 ResultScanner scanner1 = PHPAppHBaseServer.table.getScanner(scan1);
 			 
 			 
 			 int i = 1;
 			 for(Result res: scanner1){
 				 
 				 Event event = new Event();
 				 DisplayEvent evt = new DisplayEvent();
 				 
 				 EventHBaseHandler.formatRow(event, new ImmutableBytesWritable(res.getRow()), res);
 				 Map<String,List<String>> parsedStatement =  EventHBaseHandler.parseStatement(event, databaseType);
 				 
 				 evt.setRow_id(new BASE64Encoder().encode(res.getRow()));
 				 evt.setTimestamp(event.getTimestamp());
 				 evt.setSrc_ip(event.getSrc_ip());
 				 evt.setSrc_port(new Short((short)event.getSrc_port()).toString());
 				 evt.setStatement(event.getStatement());
 				 evt.setAppname(event.getAppname());
 				 evt.setDbname(event.getDbname().toLowerCase());
 				 evt.setUsername(event.getUsername().toLowerCase());
 				 evt.setFreqency("Frequent");
 				 evt.setSecurity_risk("Low");
 				 
 
 				 logger.debug("Returning row:" + evt);
 				 
 				 String str = "";
				 if(parsedStatement.get("columns") != null){
					 for(String s : parsedStatement.get("columns")){
						 str += s + ",";
					 }
				 }
 				 evt.setColumns((str.length() > 0) ? str.substring(0, str.length() -1): "");
				 str = "";
				 
				 if(parsedStatement.get("tables") != null){
					 for(String s : parsedStatement.get("tables")){
						 str += s + ",";
					 }
				 }
 				 evt.setTables((str.length() > 0) ? str.substring(0, str.length() -1): "");
 				 
 				 eventList.add(evt);
 				 
 				 if(i++ > 30){
 					 break;
 				 }
 			 }
 		 }catch(Exception e){
 			 e.printStackTrace();
 		 }
 		 
 		 if(eventList.size() == 0 && row_id_m1 == true){
 			 DisplayEvent display_event = new DisplayEvent();
 			 display_event.setRow_id("0");
 			 display_event.setStatement("No information for sever collected. When information is available it will auotmatically refresh unless automatic refresh button is disabled.");
 			 eventList.add(display_event);
 		 }
 		 logger.info("Exiting getEventsByRowID(" + server_id + "," + row_id + "," + databaseType +")");
 		return eventList;
 	}
 }
