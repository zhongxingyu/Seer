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
 * $Id: ServicesDefaultValues.java,v 1.3 2006-08-15 05:04:10 veiming Exp $
  *
  * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.setup;
 
 import com.sun.identity.common.DNUtils;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.Set;
import netscape.ldap.util.DN;
 
 /**
  * This class holds the default values of service schema.
  */
 public class ServicesDefaultValues {
     private static ServicesDefaultValues instance = new ServicesDefaultValues();
     private static Set preappendSlash = new HashSet();
     private static Set trimSlash = new HashSet();
     private Map defValues = new HashMap();
     
     static {
         preappendSlash.add("IS_PRODNAME");
         preappendSlash.add("OLDCON_DEPLOY_URI");
         preappendSlash.add("CONSOLE_URI");
         preappendSlash.add("SERVER_URI");
         trimSlash.add("CONSOLE_URI");
         trimSlash.add("SERVER_URI");
     }
 
     private ServicesDefaultValues() {
         ResourceBundle bundle = ResourceBundle.getBundle(
             "serviceDefaultValues");
         Enumeration<String> e = bundle.getKeys();
         while (e.hasMoreElements()) {
             String key = e.nextElement();
             defValues.put(key, bundle.getString(key));
         }
     }
 
     /**
      * Returns the map of default attribute name to its value.
      *
      * @return the map of default attribute name to its value.
      */
     public static Map getDefaultValues() {
         return instance.defValues;
     }
     
     /**
      * Set the deploy URI.
      *
      * @param deployURI Deploy URI.
      * @param map Service attribute values.
      */
     public static void setDeployURI(String deployURI, Map map) {
         map.put("IS_PRODNAME", deployURI);
         map.put("OLDCON_DEPLOY_URI", deployURI);
         map.put("CONSOLE_URI", deployURI);
         map.put("SERVER_URI", deployURI);
     }
 
     /**
      * Returns the tag swapped string.
      *
      * @param orig String to be tag swapped.
      * @return the tag swapped string.
      */
     public static String tagSwap(String orig) {
         Map map = instance.defValues;
         for (Iterator i = map.keySet().iterator(); i.hasNext(); ) {
             String key = (String)i.next();
             String value = (String)map.get(key);
             
             if (preappendSlash.contains(key)) {
                 orig = orig.replaceAll("/@" + key + "@", value);
                 
                 if (trimSlash.contains(key)) {
                     orig = orig.replaceAll("@" + key + "@", value.substring(1));
                 }
            } else if (key.equals("ROOT_SUFFIX")) {
                String normalized = DNUtils.normalizeDN(value);
                orig = orig.replaceAll("@ROOT_SUFFIX_HAT@",
                    normalized.replaceAll(",", "^"));
                String rfced = (new DN(value)).toRFCString();
                orig = orig.replaceAll("@ROOT_SUFFIX@", rfced);
             } else {
                 orig = orig.replaceAll("@" + key + "@", value);
             }
         }
         return orig;
     }
 }
