 /* The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: AMSetupDSConfig.java,v 1.1 2006-11-22 00:56:36 ak138937 Exp $
  *
  * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.setup;
 
 import java.util.Enumeration;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 import java.io.File;
 import java.io.IOException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import netscape.ldap.LDAPAttribute;
 import netscape.ldap.LDAPAttributeSet;
 import netscape.ldap.LDAPConnection;
 import netscape.ldap.LDAPDN;
 import netscape.ldap.LDAPEntry;
 import netscape.ldap.LDAPException;
 import netscape.ldap.LDAPModification;
 import netscape.ldap.LDAPModificationSet;
 import netscape.ldap.LDAPSearchResults;
 import netscape.ldap.util.DN;
 import netscape.ldap.util.LDIF;
 import netscape.ldap.util.LDIFAttributeContent;
 import netscape.ldap.util.LDIFContent;
 import netscape.ldap.util.LDIFModifyContent;
 import netscape.ldap.util.LDIFRecord;
 import netscape.ldap.util.RDN;
 
 import com.iplanet.am.util.Debug;
 import com.sun.identity.sm.SMSSchema;
 
 
 /**
  * This class does Directory Server related tasks for 
  * Access Manager deployed as single web-application. 
  */
 public class AMSetupDSConfig {
     private String dsManager;
     private String suffix;
     private String dsHostName;
     private String dsPort;
     private String dsAdminPwd;
     private static LDAPConnection ld = null;
     private String basedir; 
     private String deployuri; 
     private static AMSetupDSConfig dsConfigInstance = null;
     private java.util.Locale locale = null;
 
     /**
      * Constructs a new instance.
      */
     private AMSetupDSConfig() {
         Map map = ServicesDefaultValues.getDefaultValues();
         dsManager = (String)map.get(SetupConstants.CONFIG_VAR_DS_MGR_DN);
         suffix = (String)map.get(SetupConstants.CONFIG_VAR_ROOT_SUFFIX);
         dsHostName = (String)map.get(
             SetupConstants.CONFIG_VAR_DIRECTORY_SERVER_HOST);
         dsPort = (String)map.get(
             SetupConstants.CONFIG_VAR_DIRECTORY_SERVER_PORT);
         dsAdminPwd = (String)map.get(SetupConstants.CONFIG_VAR_DS_MGR_PWD);
         basedir = (String)map.get(SetupConstants.CONFIG_VAR_BASE_DIR);
         deployuri = (String)map.get(SetupConstants.CONFIG_VAR_SERVER_URI);
     }
 
     /**
      * Returns a single instance if not already created.
      *
      * @return AMSetupDSConfig instance. 
      */
     public static AMSetupDSConfig getInstance() {
         synchronized (AMSetupDSConfig.class) {
             if (dsConfigInstance == null) {
                 dsConfigInstance = new AMSetupDSConfig();
             }
        }
        return dsConfigInstance;
     }
 
     public void setLocale (java.util.Locale locale) {
         this.locale = locale;
     }
 
    
     /**
      * Validates if directory server is running and can be
      * connected at the specified host and port.
      *
      * @return <code>true</code> if directory server is running.
      */
     public boolean isDServerUp() {
         return (getLDAPConnection() == null) ? false : true;
     }
 
     /**
      * Validates the directory server port and returns as an int value.
      *
      * @return port of directory server. 
      * @throws NumberFormatException if port specified is incorrect.
      */
     private int getPort() {
         try {
             return Integer.parseInt(dsPort);
         } catch (NumberFormatException e) {
             throw new ConfiguratorException("configurator.invalidport",
                 null, locale);
         }
     }
 
     /**
      * Returns the DB Name from the directory Server
      *
      * @return dBName from directory server. 
      */
     public String getDBName() { 
         String baseDN = "cn=mapping tree,cn=config";
        String filter = "cn=" + suffix;
         String[] attrs = { "nsslapd-backend" };
         LDAPSearchResults results = null;
         String dBName = null;
         try {
             LDAPConnection ldc = getLDAPConnection();
             results = ldc.search(baseDN, LDAPConnection.SCOPE_SUB, filter, 
                 attrs, false);
             LDAPEntry ldapEntry = results.next();
             LDAPAttribute attr = ldapEntry.getAttribute(attrs[0]);
             String[] attrValues  = attr.getStringValueArray();
             dBName = attrValues[0];
         } catch (LDAPException e) {
             //Use the default value in the property file
         }
         return dBName;
     }
 
     /**
      * Set the values required for Service Schema files.
      */
     public void setDSValues() {
         Map map = ServicesDefaultValues.getDefaultValues();
         if ((suffix != null) && (suffix.length() > 0)) {
             suffix = suffix.trim();
             String normalizedDN = LDAPDN.normalize(suffix); 
             String canonicalizedDN = canonicalize(normalizedDN);
             String escapedDN = SMSSchema.escapeSpecialCharacters(normalizedDN);
             String peopleNMDN = "People_" + canonicalizedDN;
             String underscoreDN = replaceDNDelimiter(normalizedDN, "_");
             map.put("People_" + SetupConstants.NORMALIZED_ROOT_SUFFIX, 
                 replaceDNDelimiter(peopleNMDN, "_"));
             map.put(SetupConstants.CONFIG_VAR_ROOT_SUFFIX_HAT, 
                 replaceDNDelimiter(escapedDN, "^"));
             map.put(SetupConstants.NORMALIZED_RS, escapedDN); 
             map.put(SetupConstants.NORMALIZED_ORG_BASE, escapedDN); 
             map.put(SetupConstants.ORG_ROOT_SUFFIX, suffix); 
             String rdn = getRDNfromDN(normalizedDN);
             map.put(SetupConstants.RS_RDN, LDAPDN.escapeRDN(rdn)); 
             map.put(SetupConstants.DEFAULT_ORG, canonicalizedDN); 
             map.put(SetupConstants.ORG_BASE, canonicalizedDN);
         }
     }
 
     /**
      * Returns the relative DN from the suffix.
      *
      * @param nSuffix Normalized suffix.
      * @return the last component of the suffix. 
      */
     private String getRDNfromDN(String nSuffix) {
         String [] doms = LDAPDN.explodeDN(nSuffix, true);
         return doms[0];
     }
 
     /**
      * Returns cannonicalized suffix. 
      *
      * @param nSuffix Normalized suffix.
      * @return the cannonicalized suffix. 
      */
     private String canonicalize(String nSuffix) {
         StringBuffer buff = new StringBuffer(1024);
         DN dn = new DN(nSuffix);
         Vector rdns = dn.getRDNs();
         int sz = rdns.size();
         for (int i = 0; i < sz; i++) {
             RDN rdn = (RDN)rdns.get(i);
             buff.append(LDAPDN.escapeRDN(rdn.toString()));
             if (i < sz - 1) {
                 buff.append(",");
             }
         }
         return buff.toString();
     }
 
     /**
      * Returns suffix with specified delimiter. 
      *
      * @param nSuffix Normalized suffix.
      * @param replaceWith the replacing delimiter to use.
      * @return the suffix with delimiter replaced with the string 
      *         specified as replaceWith. 
      */
     private String replaceDNDelimiter(String nSuffix, String replaceWith) {
         String DN = null;
         if (nSuffix.indexOf(",") != -1) {
             DN = nSuffix.replaceAll(",",replaceWith);
         }
         return DN.trim();
     }
 
     /**
      * Check if Directory Server has the suffix. 
      *
      * @return <code>true</code> if specified suffix exists. 
      */
     public boolean connectDSwithDN() {
        String filter = "cn=" + suffix;
         String[] attrs = { "" };
         LDAPSearchResults results = null;
         boolean isValidSuffix = true;
         try {
             results = getLDAPConnection().search(suffix, 
                 LDAPConnection.SCOPE_BASE, filter, attrs, false);
         } catch (LDAPException e) {
             isValidSuffix = false;
             disconnectDServer();
         }
         return isValidSuffix;
     }
 
     /**
      * Check if DS is loaded with AccessManager entries
      *
      * @return <code>true</code> if Service Schema is loaded into
      *         Directory Server.
      */
     public String isDITLoaded() {
         String baseDN = "ou=services," + suffix;
         String filter = "ou=DAI";
        String[] attrs = { "" };
         LDAPSearchResults results = null;
         String isLoaded = "false";
         try {
             results = getLDAPConnection().search(baseDN, 
                 LDAPConnection.SCOPE_SUB, filter, attrs, false);
             if (results.getCount() > 0) {
                 isLoaded = "true";
             }
         } catch (LDAPException e) {
              if (Debug.getInstance(
                  SetupConstants.DEBUG_NAME).messageEnabled()
              ) {
                  Debug.getInstance(SetupConstants.DEBUG_NAME).message(
                      "AMSetupDSConfig.isDITLoaded: " +
                      "LDAP Operation return code: " +
                      e.getLDAPResultCode());
             }
         }
         return isLoaded;
     }
 
     /**
      * Creates LDAP schema from ldif file.
      *
      * @param file LDIF file containing Access Manager entries.
      */
     public void createSchemaFromLDIF(String file) {
         LDAPModification mods[] = null;
         LDIFContent content = null; 
         String DN = null;
 
 
         try {
             LDIF ldif = new LDIF(file);
             LDIFRecord rec = ldif.nextRecord();
            
             for( ; rec != null; rec = ldif.nextRecord()) {
                 try {
                     content = rec.getContent();
                     DN = rec.getDN();
                     if (content instanceof LDIFModifyContent) {
                         mods = ((LDIFModifyContent)content).getModifications();
                         ld.modify(DN, mods);
                     } else if (content instanceof LDIFAttributeContent) {
                         LDAPAttributeSet attrSet = null;
                         LDAPAttribute[] attrs = 
                             ((LDIFAttributeContent)content).getAttributes();
                         LDAPEntry amEntry = new LDAPEntry(DN, 
                             new LDAPAttributeSet(attrs)); 
                         ld.add(amEntry); 
                     }
                 } catch (LDAPException e) {
                     switch (e.getLDAPResultCode()) {
                         case LDAPException.ATTRIBUTE_OR_VALUE_EXISTS:
                             if (Debug.getInstance(
                                 SetupConstants.DEBUG_NAME).messageEnabled()
                              ) {
                                 Debug.getInstance(
                                     SetupConstants.DEBUG_NAME).message(
                                         "AMSetupDSConfig.createSchemaFromLDIF: "+
                                         "LDAP Operation return code: " +
                                          e.getLDAPResultCode());
                             }
                             break;
                         case LDAPException.NO_SUCH_ATTRIBUTE:
                             if (Debug.getInstance(
                                 SetupConstants.DEBUG_NAME).messageEnabled()
                              ) {
                                 Debug.getInstance(
                                     SetupConstants.DEBUG_NAME).message(
                                         "AMSetupDSConfig.createSchemaFromLDIF: "+
                                         "LDAP Operation return code: " +
                                         e.getLDAPResultCode());
                             }
                             break;
                         case LDAPException.ENTRY_ALREADY_EXISTS: 
                             LDAPModificationSet modSet = 
                                 new LDAPModificationSet();
                             LDAPAttribute[] attrs = 
                                 ((LDIFAttributeContent)content).getAttributes();
                             for (int i = 0; i < attrs.length; i++) {
                                 modSet.add(LDAPModification.ADD, attrs[i]);
                             }
                             try {
                                 ld.modify(DN, modSet);
                             } catch (LDAPException ex) {
                                 //Ignore the exception
                             }
                             break;
                         default:
                             if (Debug.getInstance(
                                 SetupConstants.DEBUG_NAME).messageEnabled()
                              ) {
                                 Debug.getInstance(
                                     SetupConstants.DEBUG_NAME).message(
                                         "AMSetupDSConfig.createSchemaFromLDIF: "+
                                         "LDAP Operation return code: " +
                                         e.getLDAPResultCode());
                             }
                     }
                 }
             }
         } catch (IOException e) {
             throw new ConfiguratorException("configurator.ldiferror",
                 null, locale);
         }
     }  
 
     /**
      * Loads the schema files into the directory Server.
      *
      * @param schemaFiles Array of schema files to load.
      */
     public void loadSchemaFiles(List<String> schemaFiles) {
         for (String file :  schemaFiles) {
             int idx = file.lastIndexOf("/");
             String schemaFile = (idx != -1) ? file.substring(idx+1) : file;
             createSchemaFromLDIF(basedir + "/" + schemaFile);
         }
     }
   
     /**
      * Helper method to disconnect from Directory Server. 
      */
     private void disconnectDServer() {
         if ((ld != null) && ld.isConnected()) {
             try {
                 ld.disconnect();
                 ld = null;
                 dsConfigInstance = null;
             } catch (LDAPException e) {
                 if (Debug.getInstance(
                     SetupConstants.DEBUG_NAME).messageEnabled()
                 ) {
                     Debug.getInstance(SetupConstants.DEBUG_NAME).message(
                         "AMSetupDSConfig.disconnectDServer: " +
                         "LDAP Operation return code: " +
                         e.getLDAPResultCode());
                 }
             }
         }
     } 
     
     /**
      * Helper method to return Ldap connection 
      *
      * @return Ldap connection 
      */
     private LDAPConnection getLDAPConnection() {
         if (ld == null) {
             try {
                 ld = new LDAPConnection();
                 ld.setConnectTimeout(300);
                 ld.connect(3, dsHostName, getPort(), dsManager, dsAdminPwd);
             } catch (LDAPException e) {
                 disconnectDServer();
                 dsConfigInstance = null;
                 ld = null;
             }
         }
         return ld;
     }
 }
