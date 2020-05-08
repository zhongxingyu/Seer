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
 * $Id: AttributeValues.java,v 1.1 2006-05-31 21:49:40 veiming Exp $
  *
  * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.cli;
 
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.ResourceBundle;
 
 /**
  * This class provides utilities for process attribute values. The attribute
  * values are passed into the CLI via properties file.
  */
 public class AttributeValues {
     private AttributeValues() {
     }
 
     /**
      * Returns a map of attribute name to set of values.
      *
      * @param mgr Command Manager object.
      * @param fileName Name of file that contains the attribute values data.
      * @param listAttributeValues list of attribute values in the format.
      *        <code>&lt;attribute-name&gt;=&lt;attribute-value&gt;</code>.
      * @return service attribute values.
      * @throws CLIException if the file contains data of incorrect format.
      */
     public static Map<String, Set<String>> parse(
         CommandManager mgr,
         String fileName,
         List<String> listAttributeValues
     ) throws CLIException {
         Map<String, Set<String>> results = null;
         if (fileName != null) {
             results = parse(mgr, fileName);
         }
 
         if ((listAttributeValues != null) && !listAttributeValues.isEmpty()) {
             if (results != null) {
                 results.putAll(parse(mgr, listAttributeValues));
             } else {
                 results = parse(mgr, listAttributeValues);
             }
         }
 
         return (results == null) ? new HashMap<String, Set<String>>() : results;
     }
 
     /**
      * Returns a map of attribute name to set of values.
      *
      * @param mgr Command Manager object.
      * @param fileName Name of file that contains the attribute values data.
      * @param listAttributeValues list of attribute values in the format.
      *        <code>&lt;attribute-name&gt;=&lt;attribute-value&gt;</code>.
      * @return service attribute values.
      * @throws CLIException if the file contains data of incorrect format.
      */
     public static Map<String, Set<String>> parse(
         CommandManager mgr, 
         List<String> listAttributeValues
     ) throws CLIException {
         Map<String, Set<String>> attrValues = 
             new HashMap<String, Set<String>>();
 
         if ((listAttributeValues != null) && !listAttributeValues.isEmpty()) {
             for (String s: listAttributeValues) {
                 int idx = s.indexOf('=');
                 if (idx == -1) {
                     throw createIncorrectFormatException(mgr, s);
                 }
            }
 
            String attrName = s.substring(0, idx);
            String attrValue =  s.substring(idx+1);
 
            Set<String> set = attrValues.get(attrName);
            if (set == null) {
                set = new HashSet<String>();
                attrValues.put(attrName, set);
             }
            set.add(attrValue);
         }
         return attrValues;
     }
 
     /**
      * Returns a map of attribute name to set of values.
      *
      * @param mgr Command Manager object.
      * @param fileName Name of file that contains the attribute values data.
      * @return service attribute values.
      * @throws CLIException if the file contains data of incorrect format.
      */
     public static Map<String, Set<String>> parse(
         CommandManager mgr, 
         String fileName
     ) throws CLIException
     {
         BufferedReader in = null;
         Map<String, Set<String>> attrValues = 
             new HashMap<String, Set<String>>();
 
         try {
             in = new BufferedReader(new FileReader(fileName));
             String line = in.readLine();
             while (line != null) {
                 line = line.trim();
                 if (line.length() > 0) {
                     int idx = line.indexOf('=');
                     if ((idx == -1) || (idx == 0) || (idx == (line.length() -1))
                     ) {
                         throw createIncorrectFormatException(mgr, line);
                     }
 
                     String key = line.substring(0, idx).trim();
                     String value = line.substring(idx+1).trim();
                     Set<String> values = attrValues.get(key);
                     if (values == null) {
                         values = new HashSet<String>();
                         attrValues.put(key, values);
                     }
                     values.add(value);
                 }
                 line = in.readLine();
             }
         } catch (IOException e) {
             throw new CLIException(e, ExitCodes.IO_EXCEPTION);
         } finally {
             if (in !=null ) {
                 try {
                     in.close();
                 } catch (IOException e) {
                     //ignore cannot close input stream
                 }
             }
         }
         return attrValues;
     }
 
     /**
      * Returns a set of values.
      *
      * @param fileName Name of file that contains the values data.
      * @return values.
      * @throws CLIException if the file contains data of incorrect format.
      */
     public static List<String> parseValues(String fileName)
         throws CLIException
     {
         BufferedReader in = null;
         List<String> values = new ArrayList<String>();
 
         try {
             in = new BufferedReader(new FileReader(fileName));
             String line = in.readLine();
             while (line != null) {
                 line = line.trim();
                 if (line.length() > 0) {
                     values.add(line);
                 }
                 line = in.readLine();
             }
         } catch (IOException e) {
             throw new CLIException(e, ExitCodes.IO_EXCEPTION);
         } finally {
             if (in !=null ) {
                 try {
                     in.close();
                 } catch (IOException e) {
                     //ignore cannot close input stream
                 }
             }
         }
         return values;
     }
 
     private static CLIException createIncorrectFormatException(
         CommandManager mgr,
         String line
     ) {
         ResourceBundle rb = mgr.getResourceBundle();
         String[] param = {line};
         String msg = MessageFormat.format(rb.getString(
             "exception-incorrect-data-format"), (Object[])param);
         return new CLIException(msg,ExitCodes.INCORRECT_DATA_FORMAT);
     }
 }
