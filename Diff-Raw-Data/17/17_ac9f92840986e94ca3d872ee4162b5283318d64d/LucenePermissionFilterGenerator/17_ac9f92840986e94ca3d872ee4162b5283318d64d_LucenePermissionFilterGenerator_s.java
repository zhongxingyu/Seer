 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the
  * Common Development and Distribution License, Version 1.0 only
  * (the "License").  You may not use this file except in compliance
  * with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE
  * or http://www.escidoc.de/license.
  * See the License for the specific language governing permissions
  * and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each
  * file and include the License file at license/ESCIDOC.LICENSE.
  * If applicable, add the following below this CDDL HEADER, with the
  * fields enclosed by brackets "[]" replaced with your own identifying
  * information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  */
 
 /*
  * Copyright 2010 Fachinformationszentrum Karlsruhe Gesellschaft
  * fuer wissenschaftlich-technische Information mbH and Max-Planck-
  * Gesellschaft zur Foerderung der Wissenschaft e.V.
  * All rights reserved.  Use is subject to license terms.
  */
 
 package de.escidoc.sb.srw.lucene;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Random;
 
 import de.escidoc.sb.srw.PermissionFilterGenerator;
 
 /**
  * generates a lucene-subquery for permission-filtering.
  * 
  * @author MIH
  */
 public class LucenePermissionFilterGenerator implements PermissionFilterGenerator {
 
     private String idsFile = "search/config/hierarchies1.txt";
 
     private String roleCountFile = "search/config/roleCounts.txt";
 
     private static HashMap<String, ArrayList<String>> idsArray = null;
     private static String[] idsList = null;
 
     private HashMap<String, String> roleCount = null;
     
     private HashMap<String, String> roleQueries = new HashMap<String, String>() {{
         put("administrator.role", 
                 "((permissions-filter.objecttype:item AND permissions-filter.context-id:escidoc\\:elibmcontext1) "
               + "OR "
               + "(permissions-filter.objecttype:container AND permissions-filter.context-id:escidoc\\:elibmcontext1) "
               + "OR "
               + "(permissions-filter.objecttype:context AND permissions-filter.PID=escidoc\\:elibmcontext1))");    
         put("Collaborator-Modifier-Container-Add-Remove-Any-Members.role", 
                 "((permissions-filter.objecttype:item AND permissions-filter.PID:(${hierarchicalContainerScope})) "
                 + "OR "
                 + "(permissions-filter.objecttype:container AND permissions-filter.PID:(${hierarchicalContainerScope}))) ");    
         put("Collaborator-Modifier-Container-Add-Remove-Members.role", 
                 "(permissions-filter.objecttype:container AND permissions-filter.PID:${containerScope})");    
         put("Collaborator-Modifier-Container-Update-Any-Members.role", 
                 "((permissions-filter.objecttype:item AND permissions-filter.PID:(${hierarchicalContainerScope})) "
                 + "OR "
                 + "(permissions-filter.objecttype:container AND permissions-filter.PID:(${hierarchicalContainerScope})))");    
         put("Collaborator-Modifier-Container-Update-Direct-Members.role", 
                 "((permissions-filter.objecttype:item AND permissions-filter.PID:${anyScope}) "
                 + "OR "
                 + "(permissions-filter.objecttype:container AND permissions-filter.PID:${anyScope}))");    
         put("Collaborator-Modifier.role", 
                 "((permissions-filter.objecttype:item AND (permissions-filter.PID:${anyScope} OR "
                 + "permissions-filter.component-id:${anyScope} OR permissions-filter.context-id:${anyScope})) "
                 + "OR "
                 + "(permissions-filter.objecttype:container AND (permissions-filter.PID:${anyScope} OR "
                 + "permissions-filter.context-id:${anyScope})))");    
         put("Collaborator.role", 
                 "((permissions-filter.objecttype:item AND (permissions-filter.PID:${anyScope} OR "
                 + "permissions-filter.component-id:${anyScope} OR permissions-filter.context-id:${anyScope} "
                 + "OR permissions-filter.PID:(${containerScope}))) "
                 + "OR "
                 + "(permissions-filter.objecttype:container AND (permissions-filter.PID:${anyScope} OR  "
                 + "permissions-filter.PID:(${containerScope}) OR permissions-filter.context-id:${anyScope})))");    
         put("Content-Relation-Manager.role", 
                 "(permissions-filter.objecttype:content-relation AND permissions-filter.created-by:escidoc\\:exuser1)");    
         put("Content-Relation-Modifier.role", 
                 "(permissions-filter.objecttype:content-relation AND permissions-filter.PID:escidoc\\:cr1)");    
         put("Context-Administrator.role", 
                 "(permissions-filter.objecttype:context AND permissions-filter.created-by:escidoc\\:exuser1)");    
         put("Context-Modifier.role", 
                 "(permissions-filter.objecttype:context AND permissions-filter.PID:escidoc\\:elibmcontext1)");    
         put("Default-User.role", 
                "((permissions-filter.objecttype:item AND permissions-filter.version-status:released)  "
                 + "OR "
                + "(permissions-filter.objecttype:container AND permissions-filter.version-status:released) "
                 + "OR "
                 + "(permissions-filter.objecttype:context AND permissions-filter.public-status:(opened closed)) "
                 + "OR "
                 + "(permissions-filter.objecttype:organizational-unit AND permissions-filter.public-status:(opened closed)) "
                 + "OR "
                 + "(permissions-filter.objecttype:content-relation AND permissions-filter.public-status:released))");    
         put("Depositor.role", 
                 "((permissions-filter.objecttype:item AND permissions-filter.context-id:${anyScope} AND  "
                 + "permissions-filter.created-by:escidoc\\:exuser1) "
                 + "OR "
                 + "(permissions-filter.objecttype:container AND permissions-filter.context-id:${anyScope} AND  "
                 + "permissions-filter.created-by:escidoc\\:exuser1))");    
         put("MD-Editor.role", 
                 "((permissions-filter.objecttype:item AND permissions-filter.context-id:${anyScope} AND  "
                 + "permissions-filter.public-status:(submitted released in-revision withdrawn) AND  "
                + "permissions-filter.version-status:(pending submitted released in-revision)) "
                 + "OR "
                 + "(permissions-filter.objecttype:container AND permissions-filter.context-id:${anyScope} AND  "
                 + "permissions-filter.public-status:(submitted released in-revision withdrawn) AND  "
                + "permissions-filter.version-status:(pending submitted released in-revision)))");    
         put("Moderator.role", 
                 "((permissions-filter.objecttype:item AND permissions-filter.context-id:${anyScope} AND  "
                 + "permissions-filter.public-status:(submitted released in-revision withdrawn) AND  "
                + "permissions-filter.version-status:(pending submitted released in-revision)) "
                 + "OR "
                 + "(permissions-filter.objecttype:container AND permissions-filter.context-id:${anyScope} AND  "
                 + "permissions-filter.public-status:(submitted released in-revision withdrawn) AND  "
                + "permissions-filter.version-status:(pending submitted released in-revision)))");    
         put("OU-Administrator.role", 
                 "(permissions-filter.objecttype:organizational-unit AND permissions-filter.PID:(${hierarchicalContainerScope}))");    
         put("System-Inspector.role", 
                 "(permissions-filter.objecttype:item OR permissions-filter.objecttype:container OR  "
                 + "permissions-filter.objecttype:context OR permissions-filter.objecttype:content-model OR  "
                 + "permissions-filter.objecttype:content-type OR permissions-filter.objecttype:content-relation OR  "
                 + "permissions-filter.objecttype:organizational-unit)");    
         put("Systemadministrator.role", 
                 "(permissions-filter.objecttype:item OR permissions-filter.objecttype:container OR  "
                 + "permissions-filter.objecttype:context OR permissions-filter.objecttype:content-model OR  "
                 + "permissions-filter.objecttype:content-type OR permissions-filter.objecttype:content-relation OR  "
                 + "permissions-filter.objecttype:organizational-unit)");    
     }};
 
     /**
      * get permission-filter subquery for user with given userId.
      * 
      * @param userId userId
      * @return permission-filter subquery
      * 
      */
     public String getPermissionFilter(final String userId) throws Exception {
         fillIdsArray();
         fillIdsList();
         fillRoleCountHash();
         StringBuffer queryBuf = new StringBuffer("");
         int counter = 0;
         for (String roleName : roleQueries.keySet()) {
             try {
                 String thisRoleCount = roleCount.get(roleName);
                 for (int i = 0; i < Integer.parseInt(thisRoleCount); i++) {
                     if (counter > 0) {
                         queryBuf.append("\n OR ");
                     }
                     counter++;
                     queryBuf.append(replacePlaceholders(roleQueries.get(roleName)));
                 }
             } catch (Exception e) {
                 System.out.println("caught a " + e.getClass()
                         + "\n with message: " + e.getMessage());
             }
 
         }
 
         return queryBuf.toString();
     }
     
     private static String replacePlaceholders(String strToReplace) {
         if (strToReplace.matches(".*\\$\\{hierarchicalContainerScope\\}.*")) {
             String ids = getHierarchicalContainerIds();
             strToReplace = strToReplace.replaceAll(
                     "\\$\\{hierarchicalContainerScope\\}", ids);
         }
         if (strToReplace.matches(".*\\$\\{containerScope\\}.*")) {
             strToReplace = strToReplace.replaceAll(
                     "\\$\\{containerScope\\}", getContainerId());
         }
         if (strToReplace.matches(".*\\$\\{anyScope\\}.*")) {
             strToReplace = strToReplace.replaceAll(
                     "\\$\\{anyScope\\}", getId());
         }
         return strToReplace;
     }
     
     private static String getHierarchicalContainerIds() {
         int i = 0;
         Random randomGenerator = new Random();
         int random = randomGenerator.nextInt(idsArray.size());
         StringBuffer buf = new StringBuffer("");
         for (String key : idsArray.keySet()) {
             if (i == random) {
                 ArrayList<String> ids = idsArray.get(key);
                 for (String id : ids) {
                     buf.append(id).append(" ");
                 }
                 idsArray.remove(key);
                 break;
             }
             i++;
         }
         return buf.toString();
     }
     
     private static String getContainerId() {
         int i = 0;
         Random randomGenerator = new Random();
         int random = randomGenerator.nextInt(idsArray.size());
         StringBuffer buf = new StringBuffer("");
         for (String key : idsArray.keySet()) {
             i++;
             if (i == random) {
                 buf.append(key);
             }
         }
         return buf.toString();
     }
     
     private static String getId() {
         Random randomGenerator = new Random();
         int random = randomGenerator.nextInt(32780);
         return idsList[random];
     }
     
     private void fillIdsArray() throws Exception {
         idsArray = new HashMap<String, ArrayList<String>>();
         BufferedReader in = new BufferedReader(new InputStreamReader(
                 getClass().getClassLoader().getResourceAsStream(idsFile), "UTF-8"));
         String str = new String("");
         ArrayList<String> ids = new ArrayList<String>();
         while ((str = in.readLine()) != null) {
             str = str.replaceAll(":", "\\\\\\\\:");
             if (str.startsWith("e")) {
                 if (!ids.isEmpty()) {
                     idsArray.put(str.trim(), ids);
                 }
                 ids = new ArrayList<String>();
             } else {
                 ids.add(str.trim());
             }
         }
         in.close();
     }
 
     private void fillIdsList() throws Exception {
         BufferedReader in = new BufferedReader(new InputStreamReader(
                 getClass().getClassLoader().getResourceAsStream(idsFile), "UTF-8"));
         String str = new String("");
         idsList = new String[32800];
         int i = 0;
         while ((str = in.readLine()) != null) {
             str = str.trim();
             str = str.replaceAll(":", "\\\\\\\\:");
             idsList[i] = str;
             i++;
         }
         in.close();
     }
 
     private void fillRoleCountHash() throws Exception {
         roleCount = new HashMap<String, String>();
         BufferedReader in = new BufferedReader(new InputStreamReader(
                 getClass().getClassLoader().getResourceAsStream(roleCountFile), "UTF-8"));
         String str = new String("");
         while ((str = in.readLine()) != null) {
             str = str.trim();
             String[] parts = str.split(",");
             roleCount.put(parts[0], parts[1]);
         }
         in.close();
     }
 
 }
