 /*
  * Copyright 2001-2005 The Apache Software Foundation.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.summit.i18nhelper;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.nio.charset.Charset;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import org.apache.maven.plugin.logging.Log;
 
 /**
  *
  * @author justin
  */
 public class BundleGenerator {
 
     Log mavenLog;
     Map<String, Map<String, Properties>> explicitProperties;
     private static final String WRITTEN_FILE_NOTICE = ""
             + "#**************************************************\r\n"
             + "#Resource automatically generated with i18nHelper *\r\n"
             //TODO put homepage here...
             //+ "#Resource automatically generated with i18nHelper *\r\n"
             + "#**************************************************";
 
     public BundleGenerator(Log log) {
         explicitProperties = new LinkedHashMap<String, Map<String, Properties>>();
         mavenLog = log;
     }
 
     public void generateResources(File baseDir, File inputFile, List<String> languageCodes, Log log) throws IOException {
         final Properties fullFile = new Properties();
         final Charset charset = Charset.forName("UTF-8");
         fullFile.load(new InputStreamReader(new FileInputStream(inputFile), charset));
 
         BufferedReader in = null;
 
         try {
             in = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), charset));
 
             String currentFile = "";
             String line;
             while ((line = in.readLine()) != null) {
                 //FIX ME this needs to be a better wildcard.
                 if (line.startsWith("#")) {
                     currentFile = line.substring(line.indexOf("#") + 1).replaceAll("\\\\", "/");
                     log.debug("Current base file: " + currentFile);
 
                 } else {
                     int splitIndex = line.indexOf("=");
                     //if it doesnt have "=" or its empty after "=", skip
                     if (splitIndex < 0 || splitIndex == line.length() - 1) {
                         continue;
                     } else {
                         String[] splitLine = line.split("=");
                         int pointIndex = splitLine[0].lastIndexOf(".");
                         if (pointIndex > 0) {
                             String possibleCode = splitLine[0].substring(pointIndex + 1);
                             String key = splitLine[0].substring(0, pointIndex);
                             if (languageCodes.contains(possibleCode)) {
                                 final String property = GoogleTranslateMojo.nativeToAscii(fullFile.getProperty(splitLine[0]));
                                 appendToFile(possibleCode, baseDir, currentFile, key, property);
                             }
                         }
                     }
                 }
             }
         } finally {
             try {
                 in.close();
             } catch (IOException ex) {
             }
         }
     }
 
     private void appendToFile(String locale, File baseDir, String baseFile, String key, String value) throws IOException {
         String localeFile = baseFile.replaceAll(".properties", "_" + locale + ".properties");
        mavenLog.info("BaseDir: " + baseDir + " Base File: " + baseFile);
         File dir;
         if (baseFile.lastIndexOf("/") > 0) {
             dir = new File(baseDir, baseFile.substring(0, baseFile.lastIndexOf("/")));
         }else{
             dir = new File(baseDir,baseFile);
         }
         if (!dir.exists()) {
             dir.mkdirs();
         }
         File f = new File(baseDir, localeFile);
         if (!f.exists()) {
             f.createNewFile();
             writeLineToFile(WRITTEN_FILE_NOTICE, f);
         }

 
 
         if (!propertyExplicitelySet(baseFile, f, locale, key)) {
             writeLineToFile("#Line Added by i18nHelper", f);
             writeLineToFile(key + "=" + value, f);
         }
     }
 
     private void writeLineToFile(String string, File f) throws IOException {
         OutputStreamWriter outFile = new OutputStreamWriter(new FileOutputStream(f, true), "UTF-8");
 
         outFile.write(string + "\r\n");
         outFile.close();
     }
 
     private boolean propertyExplicitelySet(String baseFile, File localeFile, String locale, String key) throws IOException {
         Map<String, Properties> propsMapForBaseFile = explicitProperties.get(baseFile);
         if (propsMapForBaseFile == null) {
             propsMapForBaseFile = new LinkedHashMap<String, Properties>();
             explicitProperties.put(baseFile, propsMapForBaseFile);
         }
         Properties p = propsMapForBaseFile.get(locale);
         if (p == null) {
             InputStreamReader in = null;
             try {
                 p = new Properties();
                 in = new InputStreamReader(new FileInputStream(localeFile), "UTF-8");
                 p.load(in);
 
                 propsMapForBaseFile.put(locale, p);
             } finally {
                 try {
                     in.close();
                 } catch (IOException ex) {
                 }
             }
         }
         return p.getProperty(key) != null;
     }
 }
