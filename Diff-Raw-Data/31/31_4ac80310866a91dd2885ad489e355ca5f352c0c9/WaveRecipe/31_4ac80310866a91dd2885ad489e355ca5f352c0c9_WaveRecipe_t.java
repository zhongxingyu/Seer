 // 
 //  WaveRecipe.java
 //  CalFitWaveProject
 //  
 //  Created by Philip Kuryloski on 2011-01-24.
 //  Copyright 2011 Philip Kuryloski. All rights reserved.
 // 
 
 package edu.berkeley.androidwave.waverecipe;
 
 import edu.berkeley.androidwave.waveexception.*;
 import edu.berkeley.androidwave.waverecipe.granularitytable.*;
 import edu.berkeley.androidwave.waverecipe.waverecipealgorithm.WaveRecipeAlgorithm;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.util.Xml;
 import java.io.InputStream;
 import java.security.cert.Certificate;
 import java.text.ParsePosition;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.jar.JarFile;
 import java.util.Vector;
 import org.xml.sax.SAXException;
 import org.xml.sax.Attributes;
 import org.xml.sax.helpers.DefaultHandler;
 
 /**
  * WaveRecipe
  * 
  * In memory representation of the recipe as provided by an authority.
  * 
  * Recipes are distributed as signed Android packages (.apk files), leveraging
  * the existing Android build and packaging system.  A recipe apk, denoted by
  * the .waverecipe extension, contains assets/description.xml, and
  * classes.dex.  description.xml contains recipe metadata, including the
  * fully qualified names of relevent classes contained in classes.dex.
  */
 public class WaveRecipe implements Parcelable {
     
     private static final String DESCRIPTION_XML_PATH = "assets/description.xml";
     
     protected String recipeId;
     protected Date version;
     protected String name;
     protected String description;
     
     protected WaveSensor[] sensors;
     protected WaveRecipeOutput[] recipeOutputs;
     
     protected GranularityTable granularityTable;
     
     protected Class<WaveRecipeAlgorithm> algorithmMainClass;
     
     /**
      * createFromUID
      * 
      * This should check for a cached version of the recipe, downloading it if
      * necessary, then instantiating from the downloaded version.
      */
     public static WaveRecipe createFromID(String recipeID, int version) {
         // null implementation
         return null;
     }
     
     /**
      * createFromDisk
      *
      * instantiate and return a WaveRecipe from an on disk location.  Should
      * throw an exception if the .waverecipe signature is invalid.
      */
     protected static WaveRecipe createFromDisk(String recipePath)
         throws Exception {
         
         // recipePath should point to an apk.  We need to examine the
         // signature of the apk somehow
         
         // for now we just see if it has a signature
         JarFile recipeApk = new JarFile(recipePath, true);
         if (recipeApk == null) {
             throw new InvalidSignatureException();
         }
         
         WaveRecipe recipe = new WaveRecipe();
         
         // Create a loader for this apk
         // http://yenliangl.blogspot.com/2009/11/dynamic-loading-of-classes-in-your.html
         // http://www.mail-archive.com/android-developers@googlegroups.com/msg07714.html
         dalvik.system.PathClassLoader recipePathClassLoader =
             new dalvik.system.PathClassLoader(recipePath, ClassLoader.getSystemClassLoader());
         
         // try to load the description.xml
         InputStream descriptionInputStream = recipePathClassLoader.getResourceAsStream(DESCRIPTION_XML_PATH);
         WaveRecipeXmlContentHandler contentHandler = new WaveRecipeXmlContentHandler(recipe);
         Xml.parse(descriptionInputStream, Xml.Encoding.UTF_8, contentHandler);
         
         String implementationClassName = contentHandler.getAlgorithmClassName();
         
         // try to load the WaveRecipeAlgorithm implementation
         try {
             recipe.algorithmMainClass = (Class<WaveRecipeAlgorithm>)Class.forName(implementationClassName, true, recipePathClassLoader);
         } catch (ClassNotFoundException cnfe) {
             throw new Exception("Could not find main recipe class "+implementationClassName+". Permissions for /data/dalvik-cache may be incorrect.");
         }
         
         return recipe;
     }
     
     /**
      * retrieveRecipe
      * 
      * Retrieve a recipe from a recipe authority.  Note that we need a
      * running recipe server for this.  This should cache the recipe locally.
      */
     protected static boolean retreiveRecipe(String recipeUID)
         throws InvalidSignatureException {
         // null implementation
         return false;
     }
     
     /**
      * -------------------------- Instance Methods ---------------------------
      */
     
     /**
      * WaveRecipe
      * 
      * Constructor
      */
     public WaveRecipe() {
         // initialize any complex fields
     }
     
     /**
      * getID
      */
     public String getID() {
         return recipeId;
     }
     
     /**
      * getVersion
      */
     public Date getVersion() {
         return version;
     }
     
     /**
      * getName
      */
     public String getName() {
         return name;
     }
     
     /**
      * getDescription
      */
     public String getDescription() {
         return description;
     }
     
     /**
      * getSensors
      * 
      * {@see WaveSensor}
      */
     public WaveSensor[] getSensors() {
         return sensors;
     }
     
     /**
      * getRecipeOutputs
      * 
      * @see WaveRecipeOutput
      */
     public WaveRecipeOutput[] getRecipeOutputs() {
         return recipeOutputs;
     }
     
     /**
      * getGranularityTable
      * 
      * @see GranularityTable
      */
     public GranularityTable getGranularityTable() {
         return granularityTable;
     }
     
     /**
      * getAlgorithmInstance
      */
     public WaveRecipeAlgorithm getAlgorithmInstance()
             throws IllegalAccessException, InstantiationException {
        System.out.println(""+WaveRecipeAlgorithm.class.hashCode());
        
        Object result = algorithmMainClass.newInstance();
        
        System.out.println(""+result.getClass().getSuperclass().hashCode());
        
        return (WaveRecipeAlgorithm)result;
     }
     
     /**
      * toString
      */
     @Override
     public String toString() {
         return String.format("%s(%s-%s)", this.getClass().getSimpleName(), recipeId, version);
     }
     
     /**
      * Parcelable Methods
      */
     public int describeContents() {
         return 0;
     }
     
     public void writeToParcel(Parcel dest, int flags) {
         
     }
     
     public static final Parcelable.Creator<WaveRecipe> CREATOR = new Parcelable.Creator<WaveRecipe>() {
         public WaveRecipe createFromParcel(Parcel in) {
             return new WaveRecipe(in);
         }
         
         public WaveRecipe[] newArray(int size) {
             return new WaveRecipe[size];
         }
     };
     
     private WaveRecipe(Parcel in) {
         
     }
 }
 
 
 class WaveRecipeXmlContentHandler extends DefaultHandler {
     private WaveRecipe recipe;
     protected String algorithmClassName;
     
     private String text;
     protected String textBuffer;
     boolean inRecipe;
     
     public enum SubTag { NONE, SENSORS, OUTPUTS, TABLE, ALG };
     SubTag stag = SubTag.NONE;
     
     HashMap<String, SpecifiesExpectedUnits> referenceMap;
     
     Vector<WaveSensor> sensors;
     protected WaveSensor currentSensor;
     
     Vector<WaveRecipeOutput> outputs;
     protected WaveRecipeOutput currentOutput;
     
     protected GranularityTable granularityTable;
     
     protected static Date dateFromXmlString(String s)
         throws SAXException {
         SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
         Date d = formatter.parse(s,new ParsePosition(0));
         if (d == null) {
             throw new SAXException("Error parsing date \""+s+"\"");
         }
         return d;
     }
     
     protected static String cleanDescriptionText(String s) {
         String result = s.replace('\n', ' ').replaceAll("  ", " ").trim();
         return result;
     }
     
     protected static WaveSensor.Type waveSensorTypeFromString(String s)
         throws Exception {
         if (s.equalsIgnoreCase("accelerometer")) {
             return WaveSensor.Type.ACCELEROMETER;
         } else {
             throw new Exception("Unknown sensor type \""+s+"\"");
         }
     }
     
     public WaveRecipeXmlContentHandler(WaveRecipe r) {
         recipe = r;
     }
     
     public String getAlgorithmClassName() {
         return algorithmClassName;
     }
     
     protected boolean isContinuousGranularityTable() {
         return (granularityTable == null ? false : granularityTable.getClass() == ContinuousGranularityTable.class);
     }
     
     /**
      * ContentHandler methods
      */
     @Override
     public void startDocument() throws SAXException {
         inRecipe = false;
         algorithmClassName = null;
         
         referenceMap = new HashMap<String, SpecifiesExpectedUnits>();
         
         sensors = new Vector();
         currentSensor = null;
         
         outputs = new Vector();
         currentOutput = null;
         
         granularityTable = null;
     }
     @Override
     public void startElement(String uri, String localName, String qName, Attributes atts)
             throws SAXException {
         
         //System.out.println(String.format("startElement(%s, %s, %s, %s)", uri, localName, qName, atts));
         
         if (inRecipe) {
             if (localName.equalsIgnoreCase("recipe")) {
                 throw new SAXException("Nested recipe - " + qName);
             } else if (stag == SubTag.NONE) {
                 if (localName.equalsIgnoreCase("name")) {
                     // wait for tag close
                 } else if (localName.equalsIgnoreCase("description")) {
                     // clear the textBuffer to capture multiple lines
                     textBuffer = "";
                 } else if (localName.equalsIgnoreCase("sensors")) {
                     stag = SubTag.SENSORS;
                 } else if (localName.equalsIgnoreCase("outputs")) {
                     stag = SubTag.OUTPUTS;
                 } else if (localName.equalsIgnoreCase("granularity-table")) {
                     String tableType = atts.getValue("type");
                     if (tableType.equalsIgnoreCase("continuous")) {
                         granularityTable = new ContinuousGranularityTable();
                     } else if (tableType.equalsIgnoreCase("discreet")) {
                         granularityTable = new DiscreetGranularityTable();
                     } else {
                         throw new SAXException("invalid granularity table type: "+tableType);
                     }
                     stag = SubTag.TABLE;
                 } else if (localName.equalsIgnoreCase("algorithm")) {
                     stag = SubTag.ALG;
                 }
             } else if (stag == SubTag.SENSORS) {
                 if (localName.equalsIgnoreCase("sensor")) {
                     try {
                         WaveSensor.Type t = waveSensorTypeFromString(atts.getValue("type"));
                         currentSensor = new WaveSensor(t, atts.getValue("units"));
                         String refId = atts.getValue("ref-id");
                         if (refId != null) {
                             referenceMap.put(refId, currentSensor);
                         }
                     } catch (Exception e) {
                         throw new SAXException(e);
                     }
                 } else if (localName.equalsIgnoreCase("channel")) {
                     String refId = atts.getValue("ref-id");
                     WaveSensorChannel currentChannel = new WaveSensorChannel(atts.getValue("name"), atts.getValue("units"));
                     currentSensor.addChannel(currentChannel);
                     if (refId != null) {
                         referenceMap.put(refId, currentChannel);
                     }
                 }
             } else if (stag == SubTag.OUTPUTS) {
                 if (localName.equalsIgnoreCase("output")) {
                     currentOutput = new WaveRecipeOutput(atts.getValue("name"));
                 } else if (localName.equalsIgnoreCase("channel")) {
                     currentOutput.addChannel(new WaveRecipeOutputChannel(atts.getValue("name"), atts.getValue("units")));
                 }
             } else if (stag == SubTag.TABLE) {
                 // the rate and precision tags currently have no attributes,
                 // so we handle them at tag close
             } else if (stag == SubTag.ALG) {
                 if (localName.equalsIgnoreCase("class")) {
                     if (atts.getValue("interface").equals("WaveRecipeAlgorithm")) {
                         algorithmClassName = atts.getValue("name");
                     }
                 }
             }
         } else {
             if (localName.equalsIgnoreCase("recipe")) {
                 recipe.recipeId = atts.getValue("id");
                 recipe.version = dateFromXmlString(atts.getValue("version"));
                 inRecipe = true;
             } else {
                 throw new SAXException("Root element "+localName+" is not a recipe");
             }
         }
     }
 
     @Override
     public void characters(char[] ch, int start, int length)
             throws SAXException {
         /* Gets called every time in between an opening tag and
          * a closing tag if characters are encountered. */
         text = new String(ch, start, length);
         //System.out.println(String.format("WaveRecipe->characters(...%s..., %d, %d)", text, start, length));
         textBuffer += (textBuffer == "" ? "" : " ") + text.trim();
         //System.out.println(textBuffer);
     }
 
     @Override
     public void endElement(String uri, String localName, String qName)
             throws SAXException {
         //System.out.println(String.format("endElement(%s, %s, %s)", uri, localName, qName));
         // Gets called every time a closing tag is encountered.
         if (inRecipe) {
             if (stag == SubTag.NONE) {
                 if (localName.equalsIgnoreCase("recipe")) {
                     inRecipe = false;
                 } else if (localName.equalsIgnoreCase("name")) {
                     recipe.name = text;
                 } else if (localName.equalsIgnoreCase("description")) {
                     // clean up the text buffer before storing as the description
                     recipe.description = cleanDescriptionText(textBuffer);
                     textBuffer = "";
                     System.out.println("Description assigned as \""+recipe.description+"\".");
                 }
             } else if (stag == SubTag.SENSORS) {
                 if (localName.equalsIgnoreCase("sensors")) {
                     // finalize the sensors array
                     recipe.sensors = sensors.toArray(new WaveSensor[0]);
                     stag = SubTag.NONE;
                 } else if (localName.equalsIgnoreCase("sensor")) {
                     // we could check that currentSensor is an accelerometer
                     // here, but it shouldn't be necessary
                     sensors.add(currentSensor);
                 }
             } else if (stag == SubTag.OUTPUTS) {
                 if (localName.equalsIgnoreCase("outputs")) {
                     // finalize outputs array
                     recipe.recipeOutputs = outputs.toArray(new WaveRecipeOutput[0]);
                     stag = SubTag.NONE;
                 } else if (localName.equalsIgnoreCase("output")) {
                     // finalize output reference
                     outputs.add(currentOutput);
                 }
             } else if (stag == SubTag.TABLE) {
                 if (localName.equalsIgnoreCase("granularity-table")) {
                     if (isContinuousGranularityTable()) {
                         ((ContinuousGranularityTable)granularityTable).setVariableMap(referenceMap);
                     }
                     recipe.granularityTable = granularityTable;
                     stag = SubTag.NONE;
                 } else if (localName.equalsIgnoreCase("rate")) {
                     if (isContinuousGranularityTable()) {
                         ((ContinuousGranularityTable)granularityTable).setRateFormulaString(text);
                     }
                 } else if (localName.equalsIgnoreCase("precision")) {
                     if (isContinuousGranularityTable()) {
                         ((ContinuousGranularityTable)granularityTable).setPrecisionFormulaString(text);
                     }
                 } else {
                     throw new SAXException("Unexpected tag in granularity-table: "+uri);
                 }
                 
             } else if (stag == SubTag.ALG) {
                 if (localName.equalsIgnoreCase("algorithm")) {
                     stag = SubTag.NONE;
                 }
             }
         } else {
             throw new SAXException("Root element is not a recipe");
         }
     }
 
     @Override
     public void endDocument() throws SAXException {
         /* You can perform some action in this method
          * for example to reset some sort of Collection
          * or any other variable you want. It gets called
          * every time a document end is reached. */
     }
 }
