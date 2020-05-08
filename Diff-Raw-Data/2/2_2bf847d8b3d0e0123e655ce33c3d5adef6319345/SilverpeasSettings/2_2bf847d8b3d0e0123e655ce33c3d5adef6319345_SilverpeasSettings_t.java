 /**
  * Copyright (C) 2000 - 2009 Silverpeas
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * As a special exception to the terms and conditions of version 3.0 of
  * the GPL, you may redistribute this Program in connection with Free/Libre
  * Open Source Software ("FLOSS") applications as described in Silverpeas's
  * FLOSS exception.  You should have recieved a copy of the text describing
  * the FLOSS exception, and it is also available here:
  * "http://repository.silverpeas.com/legal/licensing"
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.silverpeas.SilverpeasSettings;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import org.jdom.Document;
 import org.jdom.Element;
 
 import com.silverpeas.FileUtil.BackupFile;
 import com.silverpeas.FileUtil.ElementMultiValues;
 import com.silverpeas.FileUtil.FileUtil;
 import com.silverpeas.FileUtil.GestionVariables;
 import com.silverpeas.FileUtil.ModifProperties;
 import com.silverpeas.FileUtil.ModifText;
 import com.silverpeas.FileUtil.ModifTextSilverpeas;
 import com.silverpeas.FileUtil.ModifXMLSilverpeas;
 import com.silverpeas.applicationbuilder.XmlDocument;
 import com.silverpeas.installedtree.DirectoryLocator;
 import com.silverpeas.xml.XmlTreeHandler;
 import com.silverpeas.xml.xpath.XPath;
 
 /**
  * @Description :
  *
  * @Copyright   : Copyright (c) 2001
  * @Socit     : Silverpeas
  * @author STR
  * @version 1.0
  */
 public class SilverpeasSettings {
 
   protected static File fileLog = null;
   protected static PrintWriter bufLog = null;
   protected static XPath _xpathEngine = null;
   private static final String[] TAGS_TO_MERGE = {
     "global-vars", "fileset"
   };
   private static ArrayList xmlFiles;
   private static final String SILVERPEAS_SETTINGS_VERSION = "SilverpeasSettings V5.0";
  protected static final String DIR_SETTINGS = DirectoryLocator.getSilverpeasHome() + "/setup/settings";
   protected static final String FIRST_FILE_SETTINGS = "SilverpeasSettings.xml";
   // ici nouveaut t003 : vrification des dpendances
   protected static final String DEPENDENCIES_TAG = "dependencies";
   protected static final String SETTINGSFILE_TAG = "settingsfile";
   protected static final String SETTINGSFILENAME_ATTRIB = "name";
   protected static final String CONFIG_FILE_TAG = "configfile";
   protected static final String TEXT_FILE_TAG = "textfile";
   protected static final String COPY_FILE_TAG = "copyfile";
   protected static final String COPY_DIR_TAG = "copydir";
   protected static final String XML_FILE_TAG = "xmlfile";
   protected static final String XPATH_MODE_ATTRIB = "mode";
   protected static Map _modeMap = null;
   protected static final String VALUE_LOCATION_ATTRIB = "location";
   protected static final String RELATIVE_VALUE_ATTRIB = "relative-to";
 
   static {
     _modeMap = new HashMap(5);
     _modeMap.put("select", new Character(XmlTreeHandler.MODE_SELECT));
     _modeMap.put("insert", new Character(XmlTreeHandler.MODE_INSERT));
     _modeMap.put("update", new Character(XmlTreeHandler.MODE_UPDATE));
     _modeMap.put("delete", new Character(XmlTreeHandler.MODE_DELETE));
     _modeMap.put("unique", new Character(XmlTreeHandler.MODE_UNIQUE));
   }
 
   protected static char getXmlMode(String textualMode) {
     if (textualMode == null || textualMode.equals("")) {
       return XmlTreeHandler.MODE_UPDATE;
     }
     return ((Character) _modeMap.get(textualMode.toLowerCase())).charValue();
   }
 
   protected static String getXmlModeString(String textualMode) {
     if (textualMode == null || textualMode.equals("")) {
       return "default(update)";
     }
     return textualMode;
   }
 
   protected static XPath getXPathEngine() {
     if (_xpathEngine == null) {
       _xpathEngine = new XPath();
     }
     return _xpathEngine;
   }
 
   protected static String getRelativePath(String base, String path) {
     String result = path;
     String relBase = base;
     String resultBase = null;
     boolean baseUnixSep;
     int nbLevel;
 
     // BASE (../.. etc)
 
     // removes drive
     if (relBase != null && relBase.length() >= 2 && relBase.charAt(1) == ':') {
       relBase = relBase.substring(2);
     }
     // detects file separator
     baseUnixSep = (relBase != null && relBase.indexOf('/') != -1);
     // removes starting file separator
     if (relBase != null && relBase.length() >= 1 && relBase.charAt(0) == (baseUnixSep ? '/' : '\\')) {
       relBase = relBase.substring(1);
     }
     // removes ending file separator
     if (relBase != null && relBase.length() >= 1 && relBase.endsWith(baseUnixSep ? "/" : "\\")) {
       relBase = relBase.substring(0, relBase.length() - 2);
     }
     // detects number of levels
     if (relBase == null || relBase.length() == 0) {
       nbLevel = 0;
     }
     else {
       StringTokenizer st = new StringTokenizer(relBase, baseUnixSep ? "/" : "\\");
       nbLevel = st.countTokens();
     }
     // creates the base (../.. etc)
     for (int i = 0; i < nbLevel; i++) {
       if (i == 0) {
         resultBase = "..";
       }
       else {
         resultBase += (baseUnixSep ? "/" : "\\") + "..";
       }
     }
 
     // PATH
 
     // removes drive
     if (result.length() >= 2 && result.charAt(1) == ':') {
       result = result.substring(2);
     }
     // detects file separator
     baseUnixSep = (result != null && result.indexOf('/') != -1);
     // adds starting file separator
     if (result != null && result.length() >= 1 && result.charAt(0) != (baseUnixSep ? '/' : '\\')) {
       result = (baseUnixSep ? "/" : "\\") + result;
     }
 
     // RESULT
 
     result = resultBase + result;
 
     return result;
   }
 
   /**
    * @param args
    * @see
    */
   public static void main(String[] args) {
     try {
       System.out.println("start settings of " + SILVERPEAS_SETTINGS_VERSION + " (" + new java.util.Date() + ").");
       fileLog = new File(DirectoryLocator.getLogHome() + "/SilverpeasSettings.log");
       bufLog = new PrintWriter(new BufferedWriter(new FileWriter(fileLog.getAbsolutePath(), true)));
       displayMessageln(System.getProperty("line.separator") + "************************************************************************");
       displayMessageln("start settings of Silverpeas (" + new java.util.Date() + ").");
       if (args.length != 0) {
         throw new Exception("parameters forbidden");
       }
       File dirXml = new File(DIR_SETTINGS);
       XmlDocument fileXml = new XmlDocument(dirXml, FIRST_FILE_SETTINGS);
       fileXml.load();
 
       // merge tous les fichiers de configurations
       displayMessageln(System.getProperty("line.separator") + "merged files with " + FIRST_FILE_SETTINGS + " :");
 
       //Tri par ordre alphabetique
       xmlFiles = new java.util.ArrayList();
       File[] listeFileXml = dirXml.listFiles();
       for (int i = 0; i < listeFileXml.length; i++) {
         xmlFiles.add(listeFileXml[i]);
         displayMessageln(xmlFiles.get(i).toString());
       }
       Collections.sort(xmlFiles);
 
       for (int i = 0; i < xmlFiles.size(); i++) {
         File f = (File) xmlFiles.get(i);
         displayMessageln("Is File = " + f.isFile() + " - Extension: " + FileUtil.getExtension(f) + " - Nom =" + f.getName());
         if (f.isFile() && FileUtil.getExtension(f).equals("xml") && !(f.getName().equalsIgnoreCase(FIRST_FILE_SETTINGS))) {
           displayMessageln(f.getName());
           XmlDocument fXml = new XmlDocument(dirXml, f.getName());
           fXml.load();
 
           // ici nouveaut t003 : vrification des dpendances
           boolean dependenciesOK = checkDependencies(xmlFiles, fXml);
 
           // prise en compte uniquement si dependences OK
           if (dependenciesOK) {
             fileXml.mergeWith(TAGS_TO_MERGE, fXml);
           }
           else {
             displayMessageln("Ignore " + f.getName() + " file because dependencies are not resolved.");
           }
         }
       }
 
       Document doc = fileXml.getDocument();
 
       // Get the root element
       Element root = doc.getRootElement();
       GestionVariables gv = new GestionVariables();
 
       // liste des var globales
       displayMessageln(System.getProperty("line.separator") + "var :");
       List listeGlobalVars = root.getChildren("global-vars");
       Iterator iterGlobalVars = listeGlobalVars.iterator();
       while (iterGlobalVars.hasNext()) {
         Element eltGlobalVar = (Element) iterGlobalVars.next();
         List listeVars = eltGlobalVar.getChildren("var");
         Iterator iterVars = listeVars.iterator();
         while (iterVars.hasNext()) {
           Element eltVar = (Element) iterVars.next();
           String name = eltVar.getAttributeValue("name");
           String value = eltVar.getAttributeValue("value");
           // ici nouveaut t005 : evaluation dynamique
           // value = gv.resolveString( value );
           value = gv.resolveAndEvalString(value);
 
           String relativePath = eltVar.getAttributeValue(RELATIVE_VALUE_ATTRIB);
           if (relativePath != null && !relativePath.equals("")) {
             // ici nouveaut t005 : evaluation dynamique
             // relativePath= gv.resolveString(relativePath);
             relativePath = gv.resolveAndEvalString(relativePath);
             value = getRelativePath(relativePath, value);
           }
           gv.addVariable(name, value);
           displayMessageln("nom : " + name + "\t value : " + value);
         }
       }
 
       // liste des chemins des fichiers
       displayMessageln(System.getProperty("line.separator") + "modified files :");
       List listeFileSet = root.getChildren("fileset");
       Iterator iterFileSet = listeFileSet.iterator();
       while (iterFileSet.hasNext()) {
         Element eltFileSet = (Element) iterFileSet.next();
         String dir = eltFileSet.getAttributeValue("root");
 
         List listeActions = eltFileSet.getChildren();
         Iterator iterActions = listeActions.iterator();
         while (iterActions.hasNext()) {
           try {
             Element action = (Element) iterActions.next();
             if (action.getName().equals(CONFIG_FILE_TAG)) {
               configfile(dir, action, gv);
             }
             else if (action.getName().equals(TEXT_FILE_TAG)) {
               textfile(dir, action, gv);
             }
             else if (action.getName().equals(COPY_FILE_TAG)) {
               copyfile(dir, action, gv);
             }
             else if (action.getName().equals(XML_FILE_TAG)) {
               xmlfile(dir, action, gv);
             }
             else {
               displayMessageln("Unknown setting action : " + action.getName());
             }
           }
           catch (Exception e) {
             printError(e.toString());
           }
         } // while actions
       } // while fileset
       displayMessageln(System.getProperty("line.separator") + "settings of Silverpeas successfuly done(" + new java.util.Date() + ").");
       bufLog.close();
       System.out.println(System.getProperty("line.separator") + "settings of Silverpeas successfuly done (" + new java.util.Date() + ").");
     }
     catch (Exception e) {
       printError(e.toString());
       e.printStackTrace();
     }
   }
 
   // ---------------------------------------------------------------------
   /**
    * @param errMsg
    * @see
    */
   private static void configfile(String dir, Element eltConfigFile, GestionVariables gv)
       throws Exception {
     String dirFile = dir + eltConfigFile.getAttributeValue("name");
     // ici nouveaut t005 : evaluation dynamique
     // dirFile = gv.resolveString( dirFile );
     dirFile = gv.resolveAndEvalString(dirFile);
     String typeFile = FileUtil.getExtension(dirFile);
     displayMessageln(dirFile);
 
     // fichiers xml
     if (typeFile.equalsIgnoreCase("xml")) {
       ModifXMLSilverpeas fic = new ModifXMLSilverpeas(dirFile);
 
       // liste des parametres a modifier
       List listeParameter = eltConfigFile.getChildren("parameter");
       Iterator iterParameter = listeParameter.iterator();
       while (iterParameter.hasNext()) {
         Element eltParameter = (Element) iterParameter.next();
         String key = eltParameter.getAttributeValue("key");
         String value = eltParameter.getTextTrim();
         // ici nouveaut t005 : evaluation dynamique
         // value = gv.resolveString( value );
         value = gv.resolveAndEvalString(value);
         fic.addModification(key, value);
         displayMessageln("\tkey = " + key + "\t value = " + value);
       }
 
       // liste des multiparametres a modifier
       List listeMultiparameter = eltConfigFile.getChildren("multiparameter");
       Iterator iterMultiparameter = listeMultiparameter.iterator();
       while (iterMultiparameter.hasNext()) {
         Element eltMultiparameter = (Element) iterMultiparameter.next();
         String key = eltMultiparameter.getAttributeValue("key");
         ElementMultiValues eltMultiValue = new ElementMultiValues(key);
 
         // liste des valeurs
         List listeValue = eltMultiparameter.getChildren("value");
         Iterator iterValue = listeValue.iterator();
         while (iterValue.hasNext()) {
           Element eltValue = (Element) iterValue.next();
           String value = eltValue.getTextTrim();
           // ici nouveaut t005 : evaluation dynamique
           // value = gv.resolveString( value );
           value = gv.resolveAndEvalString(value);
           eltMultiValue.addValue(value);
           displayMessageln("\tkey = " + key + "\t value = " + value);
         }
         fic.addModification(eltMultiValue);
       }
       fic.executeModification();
     }
     // fichiers properties
     else if (typeFile.equalsIgnoreCase("properties")) {
       ModifProperties fic = new ModifProperties(dirFile);
 
       // liste des parametres a modifier
       List listeParameter = eltConfigFile.getChildren("parameter");
       Iterator iterParameter = listeParameter.iterator();
       while (iterParameter.hasNext()) {
         Element eltParameter = (Element) iterParameter.next();
         String key = eltParameter.getAttributeValue("key");
         String value = eltParameter.getTextTrim();
         // ici nouveaut t005 : evaluation dynamique
         // value = gv.resolveString( value );
         value = gv.resolveAndEvalString(value);
         fic.addModification(key, value);
         displayMessageln("\tkey = " + key + "\t value = " + value);
       }
       fic.executeModification();
     }
     else {
 
       // throw new Exception("mauvais type de fichier : " + dirFile);
       // fichiers fonctionnants avec le mode key '=' value
       ModifTextSilverpeas fic = new ModifTextSilverpeas(dirFile);
 
       // liste des parametres a modifier
       List listeParameter = eltConfigFile.getChildren("parameter");
       Iterator iterParameter = listeParameter.iterator();
       while (iterParameter.hasNext()) {
         Element eltParameter = (Element) iterParameter.next();
         String key = eltParameter.getAttributeValue("key");
         String value = eltParameter.getTextTrim();
         // ici nouveaut t005 : evaluation dynamique
         // value = gv.resolveString( value );
         value = gv.resolveAndEvalString(value);
         fic.addModification(key, value);
         displayMessageln("\tkey = " + key + "\t value = " + value);
       }
       fic.executeModification();
     }
   }
 
   // ---------------------------------------------------------------------
   private static void textfile(String dir, Element eltTextFile, GestionVariables gv)
       throws Exception {
     String dirFile = dir + eltTextFile.getAttributeValue("name");
     // ici nouveaut t005 : evaluation dynamique
     dirFile = gv.resolveString(dirFile);
     dirFile = gv.resolveAndEvalString(dirFile);
     File modifFile = new File(dirFile);
     if (modifFile.exists()) {
       BackupFile bf = new BackupFile(modifFile);
       bf.makeBackup();
     }
     displayMessageln(dirFile);
     ModifText fic = new ModifText(dirFile);
 
     // liste des parametres a modifier
     List listeParameter = eltTextFile.getChildren("parameter");
     Iterator iterParameter = listeParameter.iterator();
     while (iterParameter.hasNext()) {
       Element eltParameter = (Element) iterParameter.next();
       String key = eltParameter.getAttributeValue("key");
       String option = eltParameter.getAttributeValue("use-regex");
       String value = eltParameter.getTextTrim();
       // ici nouveaut t005 : evaluation dynamique
       // value = gv.resolveString( value );
       value = gv.resolveAndEvalString(value);
       if (option != null && option.equalsIgnoreCase("true")) {
         displayMessageln("\tregex = " + key + "\t value = " + value);
         ElementMultiValues emv = new ElementMultiValues(key);
         emv.addValue(value);
         fic.addModification(emv);
       }
       else {
         fic.addModification(key, value);
         displayMessageln("\tkey = " + key + "\t value = " + value);
       }
     }
     fic.executeModification();
   }
   // ---------------------------------------------------------------------
 
   // ---------------------------------------------------------------------
   /**
    * @param errMsg
    * @see
    */
   private static void xmlfile(String dir, Element eltConfigFile, GestionVariables gv)
       throws Exception {
     String dirFile = dir + eltConfigFile.getAttributeValue("name");
     // ici nouveaut t005 : evaluation dynamique
     // dirFile = gv.resolveString( dirFile );
     dirFile = gv.resolveAndEvalString(dirFile);
     displayMessageln(dirFile);
     File dirFileFile = new File(dirFile);
     boolean backuped = false;
 
     XmlDocument xmlDoc = new XmlDocument(dirFileFile.getParentFile(), dirFileFile.getName());
     xmlDoc.load();
     getXPathEngine().setStartingElement(xmlDoc.getDocument().getRootElement());
 
     // liste des parametres a modifier
     List listeParameter = eltConfigFile.getChildren("parameter");
     Iterator iterParameter = listeParameter.iterator();
     while (iterParameter.hasNext()) {
       Element eltParameter = (Element) iterParameter.next();
       String key = eltParameter.getAttributeValue("key");
       // ici nouveaut t005 : evaluation dynamique
       // key = gv.resolveString(key);
       key = gv.resolveAndEvalString(key);
       String mode = eltParameter.getAttributeValue(XPATH_MODE_ATTRIB);
       displayMessageln("\t" + key + " (mode:" + getXmlModeString(mode) + ")");
       getXPathEngine().setXPath(key);
       // backup handling
       getXPathEngine().setMode(XmlTreeHandler.MODE_SELECT);
       getXPathEngine().parse();
       if (!backuped && !getXPathEngine().exists().booleanValue()) {
         BackupFile bf = new BackupFile(dirFileFile);
         bf.makeBackup();
         backuped = true;
       }
       // action
       getXPathEngine().setMode(getXmlMode(mode));
       getXPathEngine().parse();
       if (eltParameter.getChildren() != null && !eltParameter.getChildren().isEmpty()) {
         getXPathEngine().setNodeAsStart();
         List listeValue = eltParameter.getChildren("value");
         Iterator iterValue = listeValue.iterator();
         while (iterValue.hasNext()) {
           Element eltValue = (Element) iterValue.next();
           String location = eltValue.getAttributeValue(VALUE_LOCATION_ATTRIB);
           // ici nouveaut t005 : evaluation dynamique
           // location = gv.resolveString(location);
           location = gv.resolveAndEvalString(location);
           String childMode = eltValue.getAttributeValue(XPATH_MODE_ATTRIB);
           String value = eltValue.getTextTrim();
           // ici nouveaut t005 : evaluation dynamique
           // value = gv.resolveString( value );
           value = gv.resolveAndEvalString(value);
           String relativePath = eltValue.getAttributeValue(RELATIVE_VALUE_ATTRIB);
           if (relativePath != null && !relativePath.equals("")) {
             // ici nouveaut t005 : evaluation dynamique
             // relativePath= gv.resolveString(relativePath);
             relativePath = gv.resolveAndEvalString(relativePath);
             value = getRelativePath(relativePath, value);
           }
           displayMessage("\t\tlocation:" + location + "\tvalue:" + value);
           getXPathEngine().setXPath(location);
           // backup handling
           getXPathEngine().setMode(XmlTreeHandler.MODE_SELECT);
           getXPathEngine().parse();
           if (!backuped && (!getXPathEngine().exists().booleanValue() || !getXPathEngine().getValue().equals(value))) {
             BackupFile bf = new BackupFile(dirFileFile);
             bf.makeBackup();
             backuped = true;
           }
           // action
           if (childMode != null && !childMode.equals("")) {
             displayMessageln("\tmode:" + getXmlModeString(childMode));
             getXPathEngine().setMode(getXmlMode(childMode));
           }
           else {
             displayMessageln("\tmode:inherited(" + getXmlModeString(mode) + ")");
             getXPathEngine().setMode(getXmlMode(mode));
           }
           getXPathEngine().parse();
           getXPathEngine().setValue(value);
         }
       }
       else {
         String value = eltParameter.getTextTrim();
         // ici nouveaut t005 : evaluation dynamique
         // value = gv.resolveString( value );
         value = gv.resolveAndEvalString(value);
         String relativePath = eltParameter.getAttributeValue(RELATIVE_VALUE_ATTRIB);
         if (relativePath != null && !relativePath.equals("")) {
           // ici nouveaut t005 : evaluation dynamique
           // relativePath= gv.resolveString(relativePath);
           relativePath = gv.resolveAndEvalString(relativePath);
           value = getRelativePath(relativePath, value);
         }
         displayMessageln("\tvalue:" + value);
         // backup handling
         if (!backuped && !getXPathEngine().getValue().equals(value)) {
           BackupFile bf = new BackupFile(dirFileFile);
           bf.makeBackup();
           backuped = true;
         }
         // action
         getXPathEngine().setValue(value);
       }
     }
     xmlDoc.save();
   }
 
   // ---------------------------------------------------------------------
   private static void copyfile(String dir, Element eltTextFile, GestionVariables gv)
       throws Exception {
     String dirFile = dir + eltTextFile.getAttributeValue("name");
     // ici nouveaut t005 : evaluation dynamique
     // dirFile = gv.resolveString( dirFile );
     dirFile = gv.resolveAndEvalString(dirFile);
     File sourceFile = new File(dirFile);
     String destFile = eltTextFile.getTextTrim();
     // ici nouveaut t005 : evaluation dynamique
     // destFile = gv.resolveString( destFile );
     destFile = gv.resolveAndEvalString(destFile);
     File destFileFile = new File(destFile);
     if (!destFileFile.isAbsolute()) {
       destFile = dir + destFile;
       // ici nouveaut t005 : evaluation dynamique
       // destFile = gv.resolveString( destFile );
       destFile = gv.resolveAndEvalString(destFile);
       destFileFile = new File(destFile);
     }
     if (sourceFile.isDirectory()) {
       FileUtil.copyDir(sourceFile, destFileFile);
     }
     else {
       if (destFileFile.exists()) {
         BackupFile bf = new BackupFile(destFileFile);
         bf.makeBackup();
       }
       FileUtil.copyFile(sourceFile, destFileFile);
     }
     displayMessageln(dirFile + System.getProperty("line.separator") + "\tcopied to " + destFile);
   }
 
   // ---------------------------------------------------------------------
   /**
    * @param errMsg
    * @see
    */
   private static void printError(String errMsg) {
     if (bufLog != null) {
       displayMessageln(System.getProperty("line.separator") + errMsg);
       bufLog.close();
     }
     System.out.println(System.getProperty("line.separator") + errMsg + System.getProperty("line.separator"));
 //		System.exit( 1 );
   }
 
   // ---------------------------------------------------------------------
   /**
    * @param msg
    * @see
    */
   private static void displayMessageln(String msg) {
 
     // displayMessage(msg + "\n");
     displayMessage(msg + System.getProperty("line.separator"));
   }
 
   // ---------------------------------------------------------------------
   /**
    * @param msg
    * @see
    */
   private static void displayMessage(String msg) {
     if (bufLog != null) {
       bufLog.print(msg);
       System.out.print(".");
     }
     else {
       System.out.print(msg);
     }
   }
 
   // ---------------------------------------------------------------------
   // ici nouveaut t003 : vrification des dpendances
   private static boolean checkDependencies(ArrayList listeFileXml, XmlDocument fXml) {
 
     // liste des dpendences
     Element root = fXml.getDocument().getRootElement(); // Get the root element
     List listeDependencies = root.getChildren(DEPENDENCIES_TAG);
 
     if (listeDependencies != null) {
 
       Iterator iterDependencies = listeDependencies.iterator();
       while (iterDependencies.hasNext()) {
 
         Element eltDependencies = (Element) iterDependencies.next();
         List listeDependencyFiles = eltDependencies.getChildren(SETTINGSFILE_TAG);
         Iterator iterDependencyFiles = listeDependencyFiles.iterator();
 
         while (iterDependencyFiles.hasNext()) {
 
           Element eltDependencyFile = (Element) iterDependencyFiles.next();
           String name = eltDependencyFile.getAttributeValue(SETTINGSFILENAME_ATTRIB);
 
           boolean found = false;
           for (int i = 0; i < listeFileXml.size(); i++) {
             File f = (File) xmlFiles.get(i);
 
             if (f.getName().equals(name)) {
               found = true;
               i = listeFileXml.size();
             }
           }
 
           if (found == false) {
             return false;
           }
         } // while
 
       } // while
 
     } // if
 
     return true;
   }
 }
