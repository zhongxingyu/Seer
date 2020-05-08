 /**
  * The contents of this file are subject to the Mozilla Public
  * License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of
  * the License at http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS
  * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * rights and limitations under the License.
  *
  * The Original Code is "EINRC-6 / Data Dictionary Project".
  *
  * The Initial Developer of the Original Code is TietoEnator.
  * The Original Code code was developed for the European
  * Environment Agency (EEA) under the IDA/EINRC framework contract.
  *
  * Copyright (C) 2000-2002 by European Environment Agency.  All
  * Rights Reserved.
  *
  * Original Code: Jaanus Heinlaid (TietoEnator)
  */
 
 package eionet.meta;
 
 import com.tee.xmlserver.*;
 import java.sql.*;
 import java.util.*;
 
 import eionet.directory.*;
 import com.tee.uit.security.*;
 
 /**
  * <P>Data Dictionary specific implementation of the <CODE>com.tee.xmlserver.AppUserIF</CODE> interface. 
  * Uses database to authenticate users.</P>
  *
  * @author  Rando Valt
  * @version 1.0
  */
 
 public class DDuser implements AppUserIF {
     
     public static final String ACL_UPDATE_PRM   = "u";
     public static final String ACL_SERVICE_NAME = "/";
     
     private boolean authented = false;
     private String user = null;
     private String password = null;
     private DBPoolIF dbPool = null;
     private String fullName = null;
        
     private String[] _roles = null;
     
     private HashMap acls = null;
        
     public DDuser() {
         dbPool = XDBApplication.getDBPool();
     }
    
     /**
     *
     */
     public boolean authenticate(String userName, String userPws) {
         
         invalidate();
 
         try {
             
             // JH040603
             // authentication is only required if context-param "authentication"
             // is missing or is equal to "true" (case insensitive)
             String auth = XDBApplication.getInstance().getInitParameter("authentication");
             if (auth==null || auth.equalsIgnoreCase("true")){
             	
 				Logger.log("Authenticating user '" + userName + "'", 5);
             	
                //DirectoryService.sessionLogin(userName, userPws);
                AuthMechanism.sessionLogin(userName, userPws);
                //fullName = DirectoryService.getFullName(userName);
				fullName = AuthMechanism.getFullName(userName);
             }
             
            /* user was found in LDAP, now check his rights in ACL            
			Logger.log("Authenticating user '" + userName + "'", 5);
            AccessControlListIF acl = getAcl(ACL_SERVICE_NAME );
            boolean isOK = acl.checkPermission(userName, ACL_UPDATE_PRM);
            if (!isOK)
                throw new Exception("User " + userName + " does not have " +
                                    "this permission: " + ACL_UPDATE_PRM);
            
            // no exceptions raised, so we must be authentic*/
             Logger.log("Authenticated!", 5);
                 
             authented = true;
             user = userName;
             password = userPws;
                        
         }
         catch (Exception e){
             Logger.log("User '" + userName + "' not authenticated", e);
         }
         
         return authented;
     }
     
 /**
  *
  */
    public boolean isAuthentic() {
       return authented;
    }
 /**
  *
  */
    public boolean isUserInRole(String role) {
       boolean b = false;
 
       if (_roles == null)
         getUserRoles();
         
       for (int i =0; i< _roles.length; i++)
         if ( _roles[i].equals(role))
           b = true;
           
       return b;
    }
 
 /**
 * FullName
 */
    public String getFullName() {
       return fullName;
    }
 
 /**
  *
  */
    public String getUserName() {
       return user;
    }
 /**
  *
  */
    public Connection getConnection() {
       //return dbPool.getConnection(user, password);
       return dbPool.getConnection();
    }
 /**
  * Returns a string array of roles the user is linked to.
  * Note that the method returns newly constructed array, leaving internal role list unrevealed.
  */
    public String[] getUserRoles() {
       //String[] roles;
       if (_roles == null) {
         try {
           
           Vector v = DirectoryService.getRoles(user);
           String[] roles = new String[v.size()];
           for ( int i=0; i< v.size(); i++)
               _roles[i] = (String)v.elementAt(i);
           
           } catch ( Exception e ) {
             //return empty String, no need for roles
             _roles = new String[]{};
           }
        }
      
       //return new String[]{};
       return _roles;
    }
 /**
  *
  */
    public void invalidate() {
       authented = false;
       user = null;
       password = null;
    }
 /** 
  *
  */
    public String toString() {
       return (user == null ? "" : user );
       //return user;
    }
    
     private AccessControlListIF getAcl(String name) throws SignOnException {
 
         if (acls == null)
             acls = AccessController.getAcls();
 
         return (AccessControlListIF)acls.get(name);
     }
     
     public static void main(String[] args){
     	
     	try{
 			Vector vello = DirectoryService.listOrganisations();
 			if (vello==null) return;
                                                                                			
 			Hashtable h = DirectoryService.getOrganisation("eea");
 			if (vello==null) return;
 			
 			Enumeration e = h.keys();
 			while (e.hasMoreElements()){
 				String key = (String)e.nextElement();
 				Object o = h.get(key);
 				String className = o.getClass().getName();
 				System.out.println(key + ": " + className);
 			}
     	}
     	catch (Exception e){
     		System.out.println(e.toString());
     	}
     }
 }
