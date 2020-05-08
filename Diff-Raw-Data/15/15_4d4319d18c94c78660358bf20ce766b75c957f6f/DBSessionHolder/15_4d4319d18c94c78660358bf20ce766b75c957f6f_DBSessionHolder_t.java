 /*
 Dynamo Web Services is a web service project for administering LucidDB
 Copyright (C) 2010-2011 Dynamo Business Intelligence Corporation
 
 This program is free software; you can redistribute it and/or modify it
 under the terms of the GNU General Public License as published by the Free
 Software Foundation; either version 2 of the License, or (at your option)
 any later version approved by Dynamo Business Intelligence Corporation.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
 package com.dynamobi.ws.util;
 
 import java.sql.Connection;
 
 import javax.sql.DataSource;
 
 import org.springframework.security.Authentication;
 import org.springframework.security.context.SecurityContextHolder;
 import org.springframework.security.userdetails.UserDetails;
 import org.springframework.security.userdetails.UserDetailsService;
 import org.springframework.web.context.ContextLoader;
 import org.springframework.web.context.WebApplicationContext;
 
 public class DBSessionHolder {
 	
 	public DBSessionHolder() {
     busy_session = false;
 	}
 	
 	private Connection sessionConnection;
   private boolean busy_session;
 
 	public synchronized Connection getSessionConnection() {
     while (busy_session) {
       try {
         Thread.sleep(200);
       } catch (InterruptedException e) {
         // request could have been terminated, ignore.
         e.printStackTrace();
       }
     }
     busy_session = true;
     final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
     WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
     UserToDataSource ds = (UserToDataSource) wac.getBean("myDataSource");

    boolean badConnection = false;
    try {
      if (sessionConnection != null) {
        badConnection = (sessionConnection.isClosed() || sessionConnection.isReadOnly());
        sessionConnection.createStatement();
      }
    } catch (java.sql.SQLException e) {
      badConnection = true;
    }

 		if ( sessionConnection == null || auth.getName() != ds.getUsername() ||
        auth.getCredentials().toString() != ds.getPassword() || badConnection) {
 			try {
         if (sessionConnection != null) {
           sessionConnection.close();
         }
         System.out.println("Session Init");
         ds.setUsername(auth.getName());
         ds.setPassword(auth.getCredentials().toString());
 
         sessionConnection = ds.getConnection();
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		return sessionConnection;
 	}
 
   public void releaseSessionConnection() {
     busy_session = false;
   }
 
 	public void setSessionConnection(Connection sessionConnection) {
 		this.sessionConnection = sessionConnection;
 	}
 
   public void cleanup() throws Exception {
     System.out.println("Session Cleanup");
 
     if ( sessionConnection != null ) {
       sessionConnection.close();
     }
 
   }
 
 }
