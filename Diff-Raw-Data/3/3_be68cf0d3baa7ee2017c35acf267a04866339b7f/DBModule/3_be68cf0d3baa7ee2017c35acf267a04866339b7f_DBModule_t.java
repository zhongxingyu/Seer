 /*
  * Copyright 2000-2003 Oracle, Inc. This software was developed in conjunction with the National Cancer Institute, and so to the extent government employees are co-authors, any rights in such works shall be subject to Title 17 of the United States Code, section 105.
  *
  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the disclaimer of Article 3, below. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
  *
  * 2. The end-user documentation included with the redistribution, if any, must include the following acknowledgment:
  *
  * "This product includes software developed by Oracle, Inc. and the National Cancer Institute."
  *
  * If no such end-user documentation is to be included, this acknowledgment shall appear in the software itself, wherever such third-party acknowledgments normally appear.
  *
  * 3. The names "The National Cancer Institute", "NCI" and "Oracle" must not be used to endorse or promote products derived from this software.
  *
  * 4. This license does not authorize the incorporation of this software into any proprietary programs. This license does not authorize the recipient to use any trademarks owned by either NCI or Oracle, Inc.
  *
  * 5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE, ORACLE, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
  */
 package gov.nih.nci.ncicb.cadsr.jaas;
 
 import java.io.*;
 import java.util.*;
 
 import javax.sql.DataSource;
 import java.sql.Connection;
 import java.sql.Statement;
 
 import java.security.*;
 
 import java.sql.ResultSet;
 
 import javax.security.auth.spi.LoginModule;
 import javax.security.auth.login.LoginException;
 import javax.security.auth.Subject;
 import javax.security.auth.callback.*;
 
 import org.apache.log4j.Logger;
 
 /**
  * This LoginModule verifies user/password against a database user.<br/>
  * This module requires 2 initialization parameters: <ul>
  * <li>dataSource: Datasource name
  * <li>dataSourceProvider: implementation class of DataSourceprovider </ul>
  *
  * @author <a href="mailto:chris.ludet@oracle.com">Christophe Ludet</a>
  */
 public class DBModule implements LoginModule {
   private CallbackHandler callbackHandler;
   private Subject  subject;
   private Map      sharedState;
   private Map      options;
   
   // temporary state
   private List tempCredentials = new ArrayList();
   private List tempPrincipals = new ArrayList();
   
   // the authentication status
   private boolean  success;
 
   private static Logger logger = Logger.getLogger(DBModule.class.getName());
   
   public void initialize(Subject subject, CallbackHandler callbackHandler,
 			 Map sharedState, Map options) {
     
     // save the initial state
     this.callbackHandler = callbackHandler;
     this.subject     = subject;
     this.sharedState = sharedState;
     this.options     = options;
     
   }
   
 
   public boolean login() throws LoginException {
 
     LoaderPrincipal  p = null;
     Properties creds = new Properties();
     
     Callback[] callbacks = new Callback[] {
       new NameCallback("Username: "),
       new PasswordCallback("Password: ", false)
     };
     
     try {
       callbackHandler.handle(callbacks);
     } catch (Exception e){
       logger.error("Can't query user for password.");
       logger.error(e.getMessage());
     } // end of try-catch
     
     String username = ((NameCallback)callbacks[0]).getName();
     String password = new String(((PasswordCallback)callbacks[1]).getPassword());
     ((PasswordCallback)callbacks[1]).clearPassword();
     
     creds.setProperty("load", "load");
     this.tempCredentials.add(creds);
     this.tempPrincipals.add(new LoaderPrincipal(username));
     
     success = false;
     try
       {
         success = dbValidate(username, password);
       }
     catch(Exception e)
       {
         javax.swing.JOptionPane.showMessageDialog(null, e);
         logger.debug("Error while logging.", e);
       }
     if(!success)
       throw new LoginException("Authentication failed: Password does not match");
     else
       return true;
     
   }
 
   public boolean logout() throws javax.security.auth.login.LoginException {
 
     tempPrincipals.clear();
     tempCredentials.clear();
 	
     // remove the principals the login module added
     Iterator it = subject.getPrincipals(LoaderPrincipal.class).iterator();
     while (it.hasNext()) {
       LoaderPrincipal p = (LoaderPrincipal)it.next();
       subject.getPrincipals().remove(p);
     }
 
     // remove the credentials the login module added
     it = subject.getPublicCredentials(Properties.class).iterator();
     while (it.hasNext()) {
       Properties creds = (Properties)it.next();
       subject.getPrincipals().remove(creds);
     }
     
     return(true);
   }
  
   public boolean abort() throws javax.security.auth.login.LoginException {
     
     // Clean out state
     success = false;
     
     tempPrincipals.clear();
     tempCredentials.clear();
     
     logout();
     
     return(true);
   }
 
   public boolean commit() throws LoginException {
 
     if (success) {
       if (subject.isReadOnly()) {
 	throw new LoginException ("Subject is Readonly");
       }
       
       try {
 // 	Iterator it = tempPrincipals.iterator();
 	
 	subject.getPrincipals().addAll(tempPrincipals);
 	subject.getPublicCredentials().addAll(tempCredentials);
 	
 	tempPrincipals.clear();
 	tempCredentials.clear();
 	
 	
 	return(true);
       } catch (Exception ex) {
 	ex.printStackTrace(System.out);
 	throw new LoginException(ex.getMessage());
       }
     } else {
       tempPrincipals.clear();
       tempCredentials.clear();
       return(true);
     }
   }
 
   private boolean dbValidate(String username, String password)
     throws Exception
   {
     DataSourceProvider dsp = (DataSourceProvider)Class.forName((String)options.get("dataSourceProvider")).newInstance();
     DataSource ds = dsp.getDataSource((String)options.get("dataSource"));
     System.out.println("USER: " + System.getProperty("db.user") + "PASSWD: " + System.getProperty("db.passwd"));
//     Connection conn = ds.getConnection(System.getProperty("db.user"), System.getProperty("db.passwd"));
    Connection conn = ds.getConnection();
 //     conn.getMetaData();
     Statement stmt = conn.createStatement();
     try {
       
       ResultSet rs = stmt.executeQuery("select ua_name from user_accounts where ua_name = '" + username.toUpperCase() + "'");
       
       return rs.next();
     } finally  {
       conn.close();
     } 
   }
     
 
 }
