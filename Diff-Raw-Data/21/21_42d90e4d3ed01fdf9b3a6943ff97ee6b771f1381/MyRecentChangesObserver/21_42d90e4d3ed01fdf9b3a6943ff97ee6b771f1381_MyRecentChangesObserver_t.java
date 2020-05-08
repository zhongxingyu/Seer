 /**********************************************************************************
  * $URL: https://source.sakaiproject.org/contrib/tfd/trunk/sdata/sdata-tool/impl/src/java/org/sakaiproject/sdata/tool/JCRDumper.java $
  * $Id: JCRDumper.java 45207 2008-02-01 19:01:06Z ian@caret.cam.ac.uk $
  ***********************************************************************************
  *
  * Copyright (c) 2008 The Sakai Foundation.
  *
  * Licensed under the Educational Community License, Version 1.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.opensource.org/licenses/ecl1.php
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  **********************************************************************************/
 
 package org.sakaiproject.sdata.services.mra;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Properties;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.Kernel;
import org.sakaiproject.content.api.ContentHostingService;
 import org.sakaiproject.db.api.SqlReader;
 import org.sakaiproject.db.api.SqlService;
 import org.sakaiproject.event.api.Event;
 import org.sakaiproject.event.api.UsageSession;
 
 /**
  * Observes events and maintains a queue of logged in users and recent changes
  * 
  * @author
  */
 public class MyRecentChangesObserver implements Observer
 {
 
 	private static final Log log = LogFactory.getLog(MyRecentChangesObserver.class);
 	private String lastLoginCheckSQL;
 	private String lastLoginInsertSQL;
 	private String lastLoginUpdateSQL;
 	private String indexQueueInsertSQL;
 	
 	
 	
 	public MyRecentChangesObserver() {
 		SqlService sqlService = Kernel.sqlService();
 		boolean autoDDL = Kernel.serverConfigurationService().getBoolean("auto.ddl", false);
 		if (autoDDL) {
 			sqlService.ddl(this.getClass().getClassLoader(), "sdata_myrecentchanges");
 		}
 		
 		
 		// this should allow the SQL to go into property files.
 		String vendor = sqlService.getVendor();
 		try {
 			Properties p = new Properties();
 			p.load(this.getClass().getClassLoader().getResourceAsStream(vendor+"/sdata_myrecentchanges_queries.sql"));
 			lastLoginCheckSQL = p.getProperty("lastLoginCheckSQL");
 			lastLoginInsertSQL = p.getProperty("lastLoginInsertSQL");
 			lastLoginUpdateSQL = p.getProperty("lastLoginUpdateSQL");
 			indexQueueInsertSQL = p.getProperty("indexQueueInsertSQL");
 			
 		} catch (Exception e) {
 			lastLoginCheckSQL= "select userid from sdata_lastlogin where userid= ? ";
 			lastLoginInsertSQL="insert into sdata_lastlogin (userid, usereid, userdate) values( ?,?,? )";
 			lastLoginUpdateSQL = "update sdata_lastlogin set userdate= ? where userid= ?";
 			indexQueueInsertSQL = "insert into sdata_indexqueue (version, name, context, tool) values( ?,?,?,?)";
 		}
 
 	}
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
 	 */
 	public void update(Observable arg0, Object arg1)
 	{
 		
 
 		try
 		{
 
 			Event event = (org.sakaiproject.event.api.Event) arg1;
 			UsageSession session = Kernel.usageSessionService().getSession(
 					event.getSessionId());
 			SqlService sqlService = Kernel.sqlService();
			ContentHostingService contentHostingService = Kernel.contentHostingService();
 			String user = session.getUserId();
 			String euser = session.getUserEid();
 
 			Object[] params = new Object[4];
 			// log.info("MySakai Received : " + event.getEvent());
 			params[0] = new java.text.SimpleDateFormat(
 			           "yyyy-MM-dd HH:mm:ss")
 			        .format(new java.util.Date(System
 					     .currentTimeMillis()));
 			params[1] = event.getResource();
 			params[2] = getEventContext(event.getResource());
 
 			
 			if (event.getEvent().equals("user.login"))
 			{
 				
 				if (sqlService.dbRead(lastLoginCheckSQL
 						,new Object[] { user }, new SqlReader() {
 
 							public Object readSqlResultRecord(ResultSet result) {
 								try {
 									return result.getObject(1);
 								} catch (SQLException e) {
 									return null;
 								}
 							}
 							
 						})
 						.size() == 0)
 				{
 					sqlService
 							.dbWrite(lastLoginInsertSQL
 									,new Object[] {
 										user,
 										euser,
 										params[0]
 
 									});
 				}
 				else
 				{
 					sqlService
 							.dbWrite(lastLoginUpdateSQL
 									, new Object[] {
 									params[0],
 									user
 									});
 				}
 				// log.info("MySakai Last Login Updated for " + user);
 
 			}
 			else if (event.getEvent().startsWith("content")
 					&& !event.getEvent().equals("content.delete")
 					&& !event.getEvent().equals("content.read"))
 			{
				// nasty hack to not index dropbox without loading an entity from
				// the DB
				String resource = event.getResource();
				boolean ignore = event.getResource().endsWith("/");
				ignore = ignore && contentHostingService.isInDropbox(resource);
				if ( !ignore ) {
					// filter out assignemt attachements
					String[] parts = resource.split("/");
					if (parts.length > 3
						&& ContentHostingService.ATTACHMENTS_COLLECTION.equals("/"+parts[1]+"/")
						&& "Assignments".equals(parts[3])) {
					ignore = true;
				}
				}
 				// Ignore collections
				if (!ignore)
 				{
 
 					params[3] = "content";
 				}
 
 			}
 			else if (event.getEvent().startsWith("annc"))
 			{
 
 				params[3] = "announcement";					
 			}
 			if ( params[3] != null ) 
 			{
 			  sqlService
 			    .dbWrite(indexQueueInsertSQL, params);
 			}
 
 		}
 		catch (Exception ex)
 		{
 			log.error(ex.getMessage());
 		}
 
 	}
 
 	private String getEventContext(String resource) {
 		if ( resource != null ) {
 			String[] parts = resource.split("/");
 			if (parts.length > 3 ) {
 				return parts[3];
 			}
 		}
 		return "";
 	}
 }
